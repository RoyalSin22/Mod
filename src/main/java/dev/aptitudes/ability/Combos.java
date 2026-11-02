package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.logic.ComboCounter;
import dev.aptitudes.network.ComboPayload;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleCategory;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleResources;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema de combos (v28). Golpes cuerpo a cuerpo directos a CRIATURAS (no jugadores, ni invocaciones) dentro
 * de la ventana suman combo: +3 % de daño por golpe (máx. +15 %, dentro del tope global ×6). Al llegar a 5,
 * el siguiente golpe es un remate según la categoría del rol principal. Recibir más de 6 de daño rompe el combo.
 * Cada golpe de combo carga +2 del recurso del rol (el «Combo» del Asesino, ×2).
 */
public final class Combos {
	private static final Map<UUID, ComboCounter> COUNTERS = new HashMap<>();
	/** Evita que el daño del propio remate cuente como golpe de combo. */
	private static boolean finishing;

	private Combos() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			AptitudesConfig cfg = Aptitudes.config();
			if (!cfg.combosEnabled) return;
			// Un golpe fuerte recibido rompe el combo de la víctima.
			if (entity instanceof ServerPlayerEntity victim && damageTaken > 6f) {
				ComboCounter c = COUNTERS.get(victim.getUuid());
				if (c != null && c.count(now(victim), window(cfg)) > 0) {
					c.reset();
					send(victim, 0, false);
				}
			}
			if (finishing || blocked || damageTaken <= 0) return;
			if (!(source.getAttacker() instanceof ServerPlayerEntity player) || source.getSource() != player) return;
			// Solo criaturas: ni jugadores (sin combos en PvP), ni invocaciones, ni soportes de armadura.
			if (entity == player || entity instanceof PlayerEntity || AbilityState.isSummon(entity)
					|| entity instanceof net.minecraft.entity.decoration.ArmorStandEntity) return;
			onHit(player, entity, cfg);
		});
	}

	/** Bonificación de daño actual (fracción). La aplica {@code RolePassives.modifyOutgoingDamage}. */
	public static float damageBonus(ServerPlayerEntity player, LivingEntity target) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.combosEnabled || target instanceof PlayerEntity) return 0f;
		ComboCounter c = COUNTERS.get(player.getUuid());
		if (c == null) return 0f;
		return (float) ComboCounter.bonus(c.count(now(player), window(cfg)), cfg.comboDamagePerHit, cfg.comboMaxBonus);
	}

	private static void onHit(ServerPlayerEntity player, LivingEntity target, AptitudesConfig cfg) {
		ComboCounter c = COUNTERS.computeIfAbsent(player.getUuid(), k -> new ComboCounter());
		long now = now(player);
		int consumed = c.tryFinisher(now, window(cfg), cfg.comboFinisherHits,
				Math.round(cfg.comboFinisherCooldownSeconds * 20));
		if (consumed > 0) {
			finisher(player, target, consumed);
			send(player, 0, true);
			return;
		}
		int count = c.hit(now, window(cfg));
		Role primary = RoleManager.get(player).primary();
		if (primary != null) RoleResources.gain(player, primary, primary == Role.ASSASSIN ? 4f : 2f);
		send(player, count, false);
	}

	// ---------------------------------------------------------------- Remates

	private static void finisher(ServerPlayerEntity player, LivingEntity target, int combo) {
		Role primary = RoleManager.get(player).primary();
		RoleCategory category = primary == null ? RoleCategory.MARTIAL : primary.category();
		int color = CastFx.colorOf(player, primary);
		ServerWorld world = player.getServerWorld();
		float damage = 6f + combo;
		DamageSource src = player.getDamageSources().playerAttack(player);
		finishing = true;
		sound(player, dev.aptitudes.sound.ModSounds.COMBO_FINISHER, 0.9f, 1.0f); // v37: golpe común del remate
		try {
			switch (category) {
				case MARTIAL -> { // Golpe sísmico: área de 3 bloques y lentitud
					for (LivingEntity e : near(player, target.getPos(), 3.0)) {
						e.damage(src, e == target ? damage : damage * 0.6f);
						e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
					}
					ParticleFx.shockwave(player.getServer(), world, target.getPos().add(0, 0.1, 0), 3.0,
							ParticleFx.dust(color, 1.5f), 6);
					sound(player, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.6f, 1.4f);
				}
				case SHADOW -> { // Ejecución: +50 % bajo el 40 % de vida y sangrado
					boolean low = target.getHealth() < target.getMaxHealth() * 0.4f;
					target.damage(src, low ? damage * 1.5f : damage);
					target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0));
					world.spawnParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getBodyY(0.5), target.getZ(),
							12, 0.3, 0.4, 0.3, 0.02);
					world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(),
							16, 0.3, 0.3, 0.3, 0.3);
					sound(player, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);
				}
				case ARCANE -> chain(player, target, damage, 2, color, false); // Descarga arcana
				case CRAFT -> chain(player, target, damage, 3, 0x7FDBFF, true);  // Sobrecarga eléctrica
				case GUARDIAN -> { // Golpe sagrado: cura el 30 % del daño
					target.damage(src, damage);
					player.heal(damage * 0.3f);
					for (ServerPlayerEntity ally : dev.aptitudes.party.PartyManager.onlineMembers(player)) {
						if (ally != player && ally.getWorld() == world && ally.squaredDistanceTo(player) <= 9.0) {
							ally.heal(damage * 0.3f);
						}
					}
					world.spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getBodyY(0.5), target.getZ(),
							14, 0.3, 0.5, 0.3, 0.05);
					world.spawnParticles(ParticleTypes.HEART, player.getX(), player.getBodyY(1.0), player.getZ(),
							3, 0.3, 0.2, 0.3, 0);
					sound(player, SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.6f);
				}
			}
		} finally {
			finishing = false;
		}
		CastFx.screen(player, dev.aptitudes.network.ScreenFxPayload.SHAKE, color, 0.35f);
	}

	/** Rayo que salta del objetivo a los {@code jumps} enemigos más cercanos (6 bloques). */
	private static void chain(ServerPlayerEntity player, LivingEntity first, float damage, int jumps, int color, boolean stun) {
		ServerWorld world = player.getServerWorld();
		DamageSource src = player.getDamageSources().playerAttack(player);
		first.damage(src, damage);
		if (stun) first.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 4));
		LivingEntity from = first;
		List<LivingEntity> rest = near(player, first.getPos(), 6.0);
		rest.remove(first);
		rest.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(first)));
		for (int i = 0; i < Math.min(jumps, rest.size()); i++) {
			LivingEntity to = rest.get(i);
			ParticleFx.line(world, ParticleFx.dust(color, 1.1f), from.getEyePos(), to.getEyePos(), 12);
			if (stun) world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, to.getX(), to.getBodyY(0.6), to.getZ(),
					8, 0.2, 0.3, 0.2, 0.1);
			to.damage(src, damage * 0.6f);
			if (stun) to.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 4));
			from = to;
		}
		sound(player, stun ? SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT : SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1.6f);
	}

	private static List<LivingEntity> near(ServerPlayerEntity p, Vec3d center, double radius) {
		double r2 = radius * radius;
		return new java.util.ArrayList<>(p.getWorld().getEntitiesByClass(LivingEntity.class,
				new Box(center, center).expand(radius), e -> Targeting.isHostileTo(p, e) && e.squaredDistanceTo(center) <= r2));
	}

	// ------------------------------------------------------------- Utilidades

	private static long window(AptitudesConfig cfg) {
		return Math.round(cfg.comboWindowSeconds * 20);
	}

	private static long now(ServerPlayerEntity player) {
		return player.getServer() != null ? player.getServer().getTicks() : 0L;
	}

	private static void send(ServerPlayerEntity player, int count, boolean finisher) {
		if (ServerPlayNetworking.canSend(player, ComboPayload.ID)) {
			ServerPlayNetworking.send(player, new ComboPayload(count, finisher));
		}
	}

	private static void sound(ServerPlayerEntity p, net.minecraft.sound.SoundEvent sound, float volume, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, volume, pitch);
	}

	public static void forget(ServerPlayerEntity player) {
		COUNTERS.remove(player.getUuid());
	}
}
