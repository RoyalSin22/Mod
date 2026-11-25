package dev.aptitudes.combat;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.AttackTags;
import dev.aptitudes.boss.GuardRules;
import dev.aptitudes.boss.GuardTracker;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.elite.ElitePosture;
import dev.aptitudes.elite.Elites;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * C7 (S3): parada con escudo contra criaturas y golpe de gracia (reglas en {@link ParryRules}).
 *
 * <p>Estado en memoria y solo en el hilo del servidor: aturdimientos y exposiciones duran unos segundos, se limpian
 * cada segundo y al parar el servidor. No se guardan: al recargar el mundo no queda ninguna criatura aturdida (el
 * efecto de lentitud sí sigue hasta caducar).</p>
 */
public final class Parries {
	/** Amplificador de la Lentitud del aturdimiento (V): la criatura apenas se mueve. */
	private static final int STUN_SLOWNESS = 4;

	private record Stun(long until, long immuneUntil) {}

	private record Exposure(UUID player, long until) {}

	private static final Map<UUID, Stun> STUNNED = new HashMap<>();
	private static final Map<UUID, Exposure> EXPOSED = new HashMap<>();

	private Parries() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(Parries::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			STUNNED.clear();
			EXPOSED.clear();
		});
	}

	private static long now(Entity e) {
		MinecraftServer server = e.getServer();
		return server == null ? 0 : server.getTicks();
	}

	/**
	 * Llamado desde el {@code ALLOW_DAMAGE} de {@code RoleEvents} antes que el resto de defensas.
	 *
	 * @return false si el golpe no debe llegar (parada, o la criatura está aturdida)
	 */
	public static boolean allowIncoming(ServerPlayerEntity victim, DamageSource source) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.combatParryEnabled || !(source.getAttacker() instanceof MobEntity mob) || AbilityState.isSummon(mob)) {
			return true;
		}
		boolean melee = source.getSource() == mob;
		long now = now(victim);
		// Una criatura aturdida no puede golpear (sí siguen sus explosiones y proyectiles ya lanzados).
		Stun stun = STUNNED.get(mob.getUuid());
		if (melee && stun != null && ParryRules.stunned(now, stun.until())) return false;

		ParryRules.Attacker kind = mob instanceof AnimatedBossEntity ? ParryRules.Attacker.BOSS
				: Elites.isElite(mob) ? ParryRules.Attacker.ELITE : ParryRules.Attacker.NORMAL;
		int raised = GuardTracker.ticksSinceRaised(victim.getUuid(), (int) now);
		boolean front = ParryRules.fromFront(mob.getX(), mob.getZ(), victim.getX(), victim.getZ(), victim.getHeadYaw());
		ParryRules.Outcome outcome = ParryRules.resolve(true, new ParryRules.Hit(kind, melee,
				source.isIn(DamageTypeTags.BYPASSES_SHIELD), front, raised));
		if (outcome == ParryRules.Outcome.NONE) return true;

		ServerWorld world = victim.getServerWorld();
		if (outcome == ParryRules.Outcome.STUN) {
			long immune = stun == null ? 0 : stun.immuneUntil();
			if (ParryRules.canStun(now, immune) && cfg.parryStunTicks > 0) {
				mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, cfg.parryStunTicks, STUN_SLOWNESS, false, true, true));
				mob.getNavigation().stop();
				STUNNED.put(mob.getUuid(), new Stun(now + cfg.parryStunTicks, ParryRules.immuneUntil(now, cfg.parryStunTicks)));
				world.spawnParticles(ParticleTypes.CRIT, mob.getX(), mob.getBodyY(1.0) + 0.3, mob.getZ(), 8, 0.3, 0.2, 0.3, 0.05);
			}
		} else {
			ElitePosture.drain(mob, world, GuardRules.PARRY_POSTURE_DAMAGE, AttackTags.of());
		}
		mob.takeKnockback(0.4, victim.getX() - mob.getX(), victim.getZ() - mob.getZ());
		if (cfg.parryGraceTicks > 0) EXPOSED.put(mob.getUuid(), new Exposure(victim.getUuid(), now + cfg.parryGraceTicks));
		world.playSound(null, victim.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 1.4f);
		world.spawnParticles(ParticleTypes.ENCHANTED_HIT, mob.getX(), mob.getBodyY(0.6), mob.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
		return false;
	}

	/**
	 * Golpe de gracia: multiplicador del siguiente golpe cuerpo a cuerpo de {@code player} contra {@code target} tras
	 * pararlo. Se consume al usarse. Lo aplica {@code RolePassives.modifyOutgoingDamage} (dentro del tope del mod).
	 */
	public static float graceMultiplier(ServerPlayerEntity player, LivingEntity target) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.combatParryEnabled) return 1.0f;
		Exposure e = EXPOSED.get(target.getUuid());
		if (e == null) return 1.0f;
		double m = ParryRules.graceMultiplier(e.player().equals(player.getUuid()), now(player), e.until(), cfg.parryGraceMultiplier);
		if (m <= 1.0) return 1.0f;
		EXPOSED.remove(target.getUuid());
		ServerWorld world = player.getServerWorld();
		world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 0.7f);
		world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(), 16, 0.4, 0.4, 0.4, 0.3);
		return (float) m;
	}

	/** Cada segundo: olvida aturdimientos (con su inmunidad) y exposiciones caducados. */
	private static void tick(MinecraftServer server) {
		if (server.getTicks() % 20 != 0) return;
		long now = server.getTicks();
		STUNNED.values().removeIf(s -> now >= s.immuneUntil());
		EXPOSED.values().removeIf(e -> now > e.until());
	}
}
