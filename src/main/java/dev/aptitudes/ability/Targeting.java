package dev.aptitudes.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Selección de objetivos común a habilidades e ítems.
 *
 * <p>Las habilidades de área solo afectan a entidades <em>hostiles</em> (monstruos
 * o mobs que te tienen como objetivo) para no herir por accidente a aldeanos,
 * mascotas u otros jugadores.</p>
 */
public final class Targeting {
	private Targeting() {
	}

	public static boolean isHostileTo(ServerPlayerEntity player, Entity entity) {
		if (dev.aptitudes.party.PartyManager.isAlly(player, entity)) return false;
		if (!(entity instanceof LivingEntity living) || entity == player || !living.isAlive()) return false;
		if (entity instanceof PlayerEntity || entity instanceof ArmorStandEntity) return false;
		if (AbilityState.isSummon(entity)) return false; // esbirros y torretas son aliados
		return entity instanceof Monster || (entity instanceof MobEntity mob && mob.getTarget() == player);
	}

	public static List<LivingEntity> hostilesAround(ServerPlayerEntity player, double radius) {
		double r2 = radius * radius;
		return player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius),
				e -> isHostileTo(player, e) && e.squaredDistanceTo(player) <= r2);
	}

	public static List<PlayerEntity> playersAround(ServerPlayerEntity player, double radius) {
		double r2 = radius * radius;
		return playersInSupportArea(player, player.getBoundingBox().expand(radius)).stream()
				.filter(p -> p.squaredDistanceTo(player) <= r2).toList();
	}

	/** Jugadores válidos para una cura, aura o escudo en una caja centrada en el efecto. */
	public static List<PlayerEntity> playersInSupportArea(ServerPlayerEntity player, Box area) {
		boolean grouped = dev.aptitudes.party.PartyManager.hasParty(player);
		return player.getWorld().getEntitiesByClass(PlayerEntity.class, area,
				p -> p.isAlive() && !p.isSpectator()
						&& (!grouped || dev.aptitudes.party.PartyManager.isAlly(player, p)));
	}

	/** Primera entidad viva en la línea de visión, sin atravesar bloques. */
	@Nullable
	public static LivingEntity raycastLiving(ServerPlayerEntity player, double range) {
		Vec3d start = player.getEyePos();
		Vec3d direction = player.getRotationVec(1.0f);
		Vec3d end = start.add(direction.multiply(range));
		HitResult blockHit = player.getWorld().raycast(new RaycastContext(start, end,
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		if (blockHit.getType() != HitResult.Type.MISS) end = blockHit.getPos();

		Box searchBox = player.getBoundingBox().stretch(direction.multiply(range)).expand(1.0);
		EntityHitResult hit = ProjectileUtil.raycast(player, start, end, searchBox,
				e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != player
						&& !(e instanceof ArmorStandEntity),
				start.squaredDistanceTo(end));
		return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
	}

	/** Punto final de la línea de visión (choque con bloque o alcance máximo). */
	public static Vec3d lookEnd(ServerPlayerEntity player, double range) {
		Vec3d start = player.getEyePos();
		Vec3d end = start.add(player.getRotationVec(1.0f).multiply(range));
		HitResult hit = player.getWorld().raycast(new RaycastContext(start, end,
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		return hit.getType() == HitResult.Type.MISS ? end : hit.getPos();
	}

	/** Dirección horizontal hacia la que mira una entidad (convención de Minecraft). */
	public static Vec3d horizontalFacing(Entity entity) {
		double yaw = Math.toRadians(entity.getYaw());
		return new Vec3d(-Math.sin(yaw), 0.0, Math.cos(yaw));
	}

	/** {@code true} si {@code attacker} está a la espalda de {@code target}. */
	public static boolean isBehind(Entity attacker, Entity target) {
		Vec3d toAttacker = attacker.getPos().subtract(target.getPos()).multiply(1, 0, 1);
		if (toAttacker.lengthSquared() < 1.0e-4) return false;
		return horizontalFacing(target).dotProduct(toAttacker.normalize()) < -0.3;
	}

	public static void particleLine(ServerWorld world, ParticleEffect particle, Vec3d from, Vec3d to) {
		Vec3d delta = to.subtract(from);
		int steps = (int) Math.min(64, Math.ceil(delta.length() * 2));
		for (int i = 0; i <= steps; i++) {
			Vec3d p = from.add(delta.multiply(i / (double) Math.max(1, steps)));
			world.spawnParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}
	}

	public static void burst(ServerWorld world, ParticleEffect particle, Entity at, int count, double spread) {
		world.spawnParticles(particle, at.getX(), at.getBodyY(0.5), at.getZ(), count, spread, spread * 0.5, spread, 0.02);
	}

	/** Busca una posición libre para los pies cerca de {@code feet} (prueba hasta 2 bloques arriba). */
	@Nullable
	public static Vec3d findFreeSpot(ServerPlayerEntity player, Vec3d feet) {
		for (int dy = 0; dy <= 2; dy++) {
			Vec3d candidate = feet.add(0, dy, 0);
			Box box = player.getBoundingBox().offset(candidate.subtract(player.getPos()));
			if (player.getWorld().isSpaceEmpty(player, box)) return candidate;
		}
		return null;
	}
}
