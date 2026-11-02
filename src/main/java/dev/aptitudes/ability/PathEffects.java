package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.EvolvedTree;
import dev.aptitudes.role.PathEffect;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.Map;

/** Aplicación de los efectos de las Sendas (ver {@link PathEffect} y {@code RoleTree.pathSpecs()}). */
public final class PathEffects {
	private PathEffects() {
	}

	/** Valor total de cada efecto para el jugador (rango efectivo × valor por rango, solo roles activos). */
	public static EnumMap<PathEffect, Double> all(ServerPlayerEntity player) {
		EnumMap<PathEffect, Double> out = new EnumMap<>(PathEffect.class);
		for (Map.Entry<String, RoleTree.PathSpec> e : RoleTree.pathSpecs().entrySet()) {
			int rank = RoleManager.activeRank(player, e.getKey());
			if (rank > 0) out.merge(e.getValue().effect(), rank * e.getValue().effect().perRank(), Double::sum);
		}
		// v23: pasivas de las filas de Maestro y Definitivo (mismo efecto, más valor por rango).
		for (EvolvedTree.Passive p : EvolvedTree.passives().values()) {
			int rank = RoleManager.learnedRank(player, p.id());
			if (rank > 0) out.merge(p.effect(), rank * p.perRank(), Double::sum);
		}
		return out;
	}

	public static double value(ServerPlayerEntity player, PathEffect effect) {
		return all(player).getOrDefault(effect, 0.0);
	}

	public static void applyAttributes(ServerPlayerEntity player) {
		EnumMap<PathEffect, Double> values = all(player);
		for (PathEffect effect : PathEffect.values()) {
			if (effect.attribute() == null) continue;
			AttributeApplier.set(player, effect.attribute(), Aptitudes.id("path/" + effect.name().toLowerCase()),
					values.getOrDefault(effect, 0.0), effect.operation());
		}
	}

	/** Daño saliente del jugador: críticos, ejecución, robo de vida y efectos al golpear. */
	public static float outgoing(ServerPlayerEntity player, LivingEntity target, boolean direct, float amount) {
		EnumMap<PathEffect, Double> v = all(player);
		if (v.isEmpty()) return amount;
		float result = amount;
		if (player.getRandom().nextDouble() < v.getOrDefault(PathEffect.CRIT, 0.0)) {
			result *= 1.5f;
			player.getServerWorld().spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(),
					10, 0.3, 0.3, 0.3, 0.2);
			player.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
					SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.8f, 1.0f);
		}
		if (target.getHealth() < target.getMaxHealth() * 0.35f) {
			result *= 1.0f + v.getOrDefault(PathEffect.EXECUTE, 0.0).floatValue();
		}
		double lifesteal = v.getOrDefault(PathEffect.LIFESTEAL, 0.0);
		if (lifesteal > 0) player.heal(result * (float) lifesteal);
		if (direct) {
			onHit(player, target, v, PathEffect.ON_HIT_POISON, StatusEffects.POISON);
			onHit(player, target, v, PathEffect.ON_HIT_SLOW, StatusEffects.SLOWNESS);
			onHit(player, target, v, PathEffect.ON_HIT_WITHER, StatusEffects.WITHER);
			if (player.getRandom().nextDouble() < v.getOrDefault(PathEffect.ON_HIT_FIRE, 0.0)) target.setOnFireFor(3);
		}
		return result;
	}

	private static void onHit(ServerPlayerEntity player, LivingEntity target, EnumMap<PathEffect, Double> v,
							  PathEffect effect, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> status) {
		if (player.getRandom().nextDouble() < v.getOrDefault(effect, 0.0)) {
			target.addStatusEffect(new StatusEffectInstance(status, 60, 0));
		}
	}

	/** Daño entrante al jugador: reducción y espinas. */
	public static float incoming(ServerPlayerEntity player, DamageSource source, float amount) {
		EnumMap<PathEffect, Double> v = all(player);
		if (v.isEmpty()) return amount;
		double thorns = v.getOrDefault(PathEffect.THORNS, 0.0);
		if (thorns > 0 && source.getAttacker() instanceof LivingEntity attacker && attacker != player
				&& source.getSource() == attacker) {
			attacker.damage(player.getDamageSources().thorns(player), amount * (float) thorns);
		}
		double reduction = Math.min(0.4, v.getOrDefault(PathEffect.DAMAGE_REDUCTION, 0.0));
		return amount * (float) (1.0 - reduction);
	}

	/** @return true si el jugador esquiva el ataque de una criatura. */
	public static boolean dodges(ServerPlayerEntity player) {
		double dodge = value(player, PathEffect.DODGE);
		if (dodge <= 0 || player.getRandom().nextDouble() >= dodge) return false;
		player.getServerWorld().spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getBodyY(0.5), player.getZ(),
				6, 0.3, 0.3, 0.3, 0.02);
		return true;
	}

	/** Cada segundo: regeneración de vida (cada 3 s) y de maná. */
	public static void tick(MinecraftServer server) {
		int ticks = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (!p.isAlive()) continue;
			EnumMap<PathEffect, Double> v = all(p);
			if (v.isEmpty()) continue;
			double regen = v.getOrDefault(PathEffect.REGEN, 0.0);
			if (regen > 0 && ticks % 60 == 0 && p.getHealth() < p.getMaxHealth()) p.heal((float) regen);
			double mana = v.getOrDefault(PathEffect.MANA_REGEN, 0.0);
			if (mana > 0) {
				RoleData data = RoleManager.get(p);
				data.setEnergy(Math.min(AbilityManager.maxEnergy(p), data.energy() + mana));
			}
		}
	}
}
