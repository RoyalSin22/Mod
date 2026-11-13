package dev.aptitudes.ability;

import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.HunterRules.*;

/**
 * v67 (F6): las 10 habilidades nuevas del Cazador (N1–N10). Cifras en {@link HunterRules} (puro, JUnit). Las marcas
 * viven en {@code NewRoles} (se leen y escriben con sus accesores). «A distancia» es todo daño que no es un golpe
 * directo. Estado en memoria: al reiniciar el servidor se pierden la quietud y la Vista de halcón.
 */
public final class HunterKit {
	private static final String N = "hunter_n";

	private static final class State {
		int stillTicks;
		boolean patient;
		Vec3d lastPos = Vec3d.ZERO;
		long sightUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private HunterKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", HunterKit::huntingNet);
		Abilities.register(N + "4", HunterKit::explosiveArrow);
		Abilities.register(N + "6", HunterKit::arrowFan);
		Abilities.register(N + "8", HunterKit::falconSight);
		Abilities.register(N + "10", HunterKit::finalHunt);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Red de caza: los enemigos de la zona quedan inmóviles. */
	private static boolean huntingNet(ServerPlayerEntity p, int rank) {
		Vec3d at = Targeting.lookEnd(p, NET_RANGE);
		int ticks = netTicks(rank);
		var world = p.getServerWorld();
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(NET_RADIUS),
				e -> Targeting.isHostileImpactTarget(p, e) && e.getPos().distanceTo(at) <= NET_RADIUS)) {
			KitSupport.effect(e, StatusEffects.SLOWNESS, ticks, 6);
		}
		ParticleFx.ring(world, at.add(0, 0.2, 0), NET_RADIUS, ParticleTypes.CRIT, 24);
		world.spawnParticles(ParticleTypes.CLOUD, at.x, at.y + 0.5, at.z, 12, NET_RADIUS / 2, 0.2, NET_RADIUS / 2, 0.01);
		Targeting.particleLine(world, ParticleTypes.CRIT, p.getEyePos(), at);
		KitSupport.sound(p, SoundEvents.ENTITY_FISHING_BOBBER_THROW, 1.0f);
		return true;
	}

	/** N4 Flecha explosiva: una flecha que explota donde cae. */
	private static boolean explosiveArrow(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, ARROW_RANGE);
		Vec3d at = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.lookEnd(p, ARROW_RANGE);
		var world = p.getServerWorld();
		float damage = (float) arrowDamage(rank, KitSupport.scale());
		Targeting.particleLine(world, ParticleTypes.CRIT, p.getEyePos(), at);
		if (target != null && Targeting.isHostileImpactTarget(p, target)) target.damage(p.getDamageSources().arrow(null, p), damage);
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(ARROW_RADIUS),
				e -> e != target && Targeting.isHostileImpactTarget(p, e) && e.getPos().distanceTo(at) <= ARROW_RADIUS + 1)) {
			e.damage(KitSupport.magic(p), (float) (damage * arrowSplashFraction()));
		}
		world.spawnParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0, 0, 0, 0);
		KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.2f);
		return true;
	}

	/** N6 Disparo en abanico: cinco descargas de flechas en cono. */
	private static boolean arrowFan(ServerPlayerEntity p, int rank) {
		float damage = (float) fanDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), FAN_VOLLEYS, FAN_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			Vec3d facing = Targeting.horizontalFacing(p);
			for (LivingEntity e : Targeting.hostilesAround(p, FAN_RANGE)) {
				Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
				if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < FAN_MIN_DOT) continue;
				e.timeUntilRegen = 0;
				e.damage(p.getDamageSources().arrow(null, p), damage);
			}
			Vec3d at = p.getPos().add(facing.multiply(3)).add(0, 1.2, 0);
			p.getServerWorld().spawnParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 6, 1.2, 0.3, 1.2, 0.1);
			KitSupport.sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 1.0f + 0.05f * i);
		});
		return true;
	}

	/** N8 Vista de halcón: tus presas marcadas brillan y reciben más daño. */
	private static boolean falconSight(ServerPlayerEntity p, int rank) {
		state(p).sightUntil = KitSupport.now(p) + sightTicks(rank);
		Targeting.burst(p.getServerWorld(), ParticleTypes.CLOUD, p, 20, 0.7);
		KitSupport.sound(p, SoundEvents.ENTITY_PARROT_FLY, 0.6f);
		return true;
	}

	/** N10 Cacería final: marcas a todos los enemigos cercanos y tu halcón los ataca. */
	private static boolean finalHunt(ServerPlayerEntity p, int rank) {
		List<LivingEntity> targets = new ArrayList<>(Targeting.hostilesAround(p, FINAL_RADIUS));
		if (targets.isEmpty()) return false;
		targets.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
		List<LivingEntity> chosen = targets.subList(0, Math.min(FINAL_TARGETS, targets.size()));
		float damage = (float) finalDamage(KitSupport.scale());
		for (LivingEntity e : chosen) {
			NewRoles.markTarget(p, e, FINAL_MARK_TICKS, FINAL_MARK_PERCENT);
			KitSupport.effect(e, StatusEffects.GLOWING, FINAL_MARK_TICKS, 0);
		}
		AbilityState.repeat(p.getServer(), chosen.size(), FINAL_INTERVAL_TICKS, i -> {
			LivingEntity e = chosen.get(i);
			if (!p.isAlive() || !e.isAlive()) return;
			p.getServerWorld().spawnParticles(ParticleTypes.CLOUD, e.getX(), e.getBodyY(1.0), e.getZ(), 8, 0.3, 0.2, 0.3, 0.05);
			e.timeUntilRegen = 0;
			e.damage(p.getDamageSources().playerAttack(p), damage);
			KitSupport.sound(p, SoundEvents.ENTITY_PARROT_FLY, 0.6f);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Mira certera, Presa herida, Cazador paciente y Vista de halcón. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		float bonus = 0f;
		if (!direct) {
			int steady = RoleManager.activeRank(p, N + "1");
			if (steady > 0) bonus += (float) steadyBonus(steady, p.distanceTo(target));
			State s = STATES.get(p.getUuid());
			if (s != null && s.patient) {
				s.patient = false;
				s.stillTicks = 0;
				bonus += (float) PATIENT_BONUS;
				Targeting.burst(p.getServerWorld(), ParticleTypes.CRIT, target, 8, 0.3);
			}
		}
		int wounded = RoleManager.activeRank(p, N + "2");
		if (wounded > 0) bonus += (float) woundedBonus(wounded, target.getHealth(), target.getMaxHealth());
		State s = STATES.get(p.getUuid());
		if (s != null && windowOpen(KitSupport.now(p), s.sightUntil) && NewRoles.isMarkedBy(p, target)) {
			bonus += (float) FALCON_SIGHT_BONUS;
		}
		return bonus;
	}

	/** Duración de las trampas (la usa {@code NewRoles}). */
	static double trapFactor(ServerPlayerEntity p) {
		return HunterRules.trapFactor(RoleManager.activeRank(p, N + "5"));
	}

	/** Botín de caza: las presas marcadas dan experiencia extra. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		int rank = RoleManager.activeRank(p, N + "9");
		if (rank <= 0 || !NewRoles.isMarkedBy(p, victim)) return;
		ExperienceOrbEntity.spawn(p.getServerWorld(), victim.getPos(), lootExperience(rank));
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 4 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			boolean patient = RoleManager.activeRank(p, N + "7") > 0;
			State s = STATES.get(p.getUuid());
			if (s == null && !patient) continue;
			s = state(p);
			if (patient) {
				boolean still = p.isSneaking() && p.getPos().squaredDistanceTo(s.lastPos) < 0.01;
				s.stillTicks = still ? s.stillTicks + 4 : 0;
				if (!s.patient && s.stillTicks >= PATIENT_TICKS) s.patient = true;
			} else {
				s.patient = false;
			}
			s.lastPos = p.getPos();
			if (windowOpen(now, s.sightUntil) && now % 20 == 0) {
				for (LivingEntity e : Targeting.hostilesAround(p, 32)) {
					if (NewRoles.isMarkedBy(p, e)) KitSupport.effect(e, StatusEffects.GLOWING, 40, 0);
				}
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
