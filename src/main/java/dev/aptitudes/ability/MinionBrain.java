package dev.aptitudes.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Comparator;

/**
 * "Cerebro" de los esbirros del Nigromante, ejecutado cada 10 ticks.
 *
 * <p>No sustituye la IA vanilla del zombi/esqueleto: solo le elige el objetivo
 * (lo que ataca a su dueño, o el hostil más cercano) y lo mantiene cerca del
 * dueño. Que nunca ataquen a jugadores lo garantiza el mixin de
 * {@code MobEntity#setTarget} mediante {@link AbilityState#shouldBlockTarget}.</p>
 */
final class MinionBrain {
	private static final double GUARD_RADIUS = 16.0;
	private static final double FOLLOW_DISTANCE = 6.0;
	private static final double TELEPORT_DISTANCE = 28.0;

	private MinionBrain() {
	}

	static void tick(MobEntity minion, ServerPlayerEntity owner) {
		if (minion.getWorld() != owner.getWorld()) {
			minion.discard();
			return;
		}
		double distance = minion.distanceTo(owner);
		// Un zombi sumergido acabaría convirtiéndose en ahogado (entidad nueva, sin etiqueta de esbirro).
		if (distance > TELEPORT_DISTANCE || minion.isSubmergedInWater()) {
			minion.requestTeleport(owner.getX(), owner.getY(), owner.getZ());
			minion.setTarget(null);
			return;
		}

		LivingEntity target = minion.getTarget();
		boolean invalid = target == null || !target.isAlive() || target instanceof PlayerEntity
				|| AbilityState.isSummon(target) || target.distanceTo(owner) > GUARD_RADIUS;
		if (invalid) {
			LivingEntity next = pickTarget(minion, owner);
			minion.setTarget(next);
			target = next;
		}

		if (target == null && distance > FOLLOW_DISTANCE) {
			minion.getNavigation().startMovingTo(owner, 1.2);
		}
	}

	private static LivingEntity pickTarget(MobEntity minion, ServerPlayerEntity owner) {
		// Prioridad 1: quien está atacando al dueño.
		LivingEntity attacker = owner.getAttacker();
		if (attacker != null && attacker.isAlive() && !(attacker instanceof PlayerEntity)
				&& !AbilityState.isSummon(attacker) && attacker.distanceTo(owner) <= GUARD_RADIUS) {
			return attacker;
		}
		// Prioridad 2: el hostil más cercano al esbirro dentro del radio de guardia del dueño.
		return Targeting.hostilesAround(owner, GUARD_RADIUS).stream()
				.min(Comparator.comparingDouble(minion::squaredDistanceTo))
				.orElse(null);
	}
}
