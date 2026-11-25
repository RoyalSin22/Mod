package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.boss.GuardTracker;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.logic.ParryRules;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * C7 · S3 Parada y contraataque. Las reglas puras y su descripción están en {@link ParryRules}.
 *
 * <p>Una criatura golpea cuerpo a cuerpo al jugador, el escudo lo bloquea y llevaba como mucho
 * {@code PERFECT_WINDOW_TICKS} alzado: parada. La criatura queda aturdida y el jugador tiene un plazo para asestarle un
 * golpe de gracia con daño extra ({@link #finisher}). No afecta a jugadores (sin PvP), jefes ni campeones. Toda la
 * lógica es del servidor; no hay cliente nuevo. Sin mixins: usa {@code AFTER_DAMAGE} de Fabric.</p>
 */
public final class Parry {
	private static final ParryRules.Book BOOK = new ParryRules.Book();

	private Parry() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (blocked && entity instanceof ServerPlayerEntity player) onBlocked(player, source);
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> BOOK.forget(entity.getUuid()));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 100 == 0) BOOK.prune(server.getTicks());
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> BOOK.clear());
	}

	private static void onBlocked(ServerPlayerEntity player, DamageSource source) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.parryEnabled || !(player.getWorld() instanceof ServerWorld world) || world.getServer() == null) return;
		if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
		boolean directMelee = source.getSource() == attacker && !source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE);
		if (!ParryRules.canParry(attacker instanceof PlayerEntity, directMelee, isBoss(attacker), Elites.isChampion(attacker))) return;
		int now = world.getServer().getTicks();
		if (!ParryRules.inWindow(GuardTracker.ticksSinceRaised(player.getUuid(), now))) return;

		BOOK.open(attacker.getUuid(), player.getUuid(), ParryRules.expiresAt(now, cfg.parryFinisherTicks));
		if (cfg.parryStunTicks > 0) {
			int ticks = Math.min(ParryRules.MAX_STUN_TICKS, cfg.parryStunTicks);
			attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 6)); // inmóvil
			attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ticks, 4)); // no hace daño
			if (attacker instanceof MobEntity mob) mob.getNavigation().stop();
		}
		world.spawnParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getBodyY(0.6), attacker.getZ(), 14, 0.35, 0.4, 0.35, 0.3);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 1.7f);
		player.sendMessage(Text.translatable("parry.aptitudes.success").formatted(Formatting.GOLD, Formatting.BOLD), true);
	}

	/**
	 * Golpe de gracia: si {@code attacker} acaba de parar a {@code target} y lo golpea directamente a tiempo, multiplica
	 * el daño y gasta la ventana. Llamado desde {@code RolePassives.modifyOutgoingDamage}.
	 */
	public static float finisher(ServerPlayerEntity attacker, LivingEntity target, DamageSource source, float amount) {
		if (source.getSource() != attacker || !Aptitudes.config().parryEnabled || !(attacker.getWorld() instanceof ServerWorld world)
				|| world.getServer() == null) return amount;
		if (!BOOK.consume(target.getUuid(), attacker.getUuid(), world.getServer().getTicks())) return amount;
		world.spawnParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getBodyY(0.6), target.getZ(), 18, 0.3, 0.4, 0.3, 0.4);
		world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 0.8f);
		attacker.sendMessage(Text.translatable("parry.aptitudes.finisher").formatted(Formatting.RED, Formatting.BOLD), true);
		return ParryRules.finisherDamage(amount, Aptitudes.config().parryFinisherMultiplier);
	}

	/** Jefes con su propio sistema: los animados del mod, los de Aptitudes con etiqueta y los tres de vanilla. */
	private static boolean isBoss(LivingEntity e) {
		return e instanceof AnimatedBossEntity || e instanceof WitherEntity || e instanceof EnderDragonEntity
				|| e instanceof WardenEntity || e.getCommandTags().stream().anyMatch(tag -> BossKind.fromTag(tag) != null);
	}
}
