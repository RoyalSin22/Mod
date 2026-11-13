package dev.aptitudes.ability;

import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
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
import static dev.aptitudes.ability.GunslingerRules.*;

/**
 * v67 (F6): las 10 habilidades nuevas del Mago pistolero (N1–N10). Cifras en {@link GunslingerRules} (puro, JUnit).
 * «A distancia» es todo daño que no es un golpe directo, igual que en el resto del rol. Los disparos de las propias
 * habilidades no encadenan Presión, Cabeza fría ni Doble pistola. Estado en memoria.
 */
public final class GunslingerKit {
	private static final String N = "gunslinger_n";

	private static final class State {
		long lastShot = -100_000;
		long doubleUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();
	private static boolean inExtra;

	private GunslingerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", GunslingerKit::stunBullet);
		Abilities.register(N + "4", GunslingerKit::powderGrenade);
		Abilities.register(N + "6", GunslingerKit::throughShot);
		Abilities.register(N + "8", GunslingerKit::doublePistol);
		Abilities.register(N + "10", GunslingerKit::execution);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Bala aturdidora: daño y Lentitud IV. */
	private static boolean stunBullet(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, STUN_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		Targeting.particleLine(p.getServerWorld(), ParticleTypes.CRIT, p.getEyePos(), target.getPos().add(0, target.getHeight() / 2, 0));
		hit(p, target, (float) stunDamage(rank, KitSupport.scale()));
		KitSupport.effect(target, StatusEffects.SLOWNESS, STUN_TICKS, 3);
		KitSupport.sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f);
		return true;
	}

	/** N4 Granada de pólvora: explota a los dos segundos. */
	private static boolean powderGrenade(ServerPlayerEntity p, int rank) {
		Vec3d at = Targeting.lookEnd(p, GRENADE_RANGE);
		float damage = (float) grenadeDamage(rank, KitSupport.scale());
		Targeting.particleLine(p.getServerWorld(), ParticleTypes.SMOKE, p.getEyePos(), at);
		KitSupport.sound(p, SoundEvents.ENTITY_SNOWBALL_THROW, 0.8f);
		AbilityState.schedule(p.getServer(), GRENADE_FUSE_TICKS, () -> {
			if (!p.isAlive()) return;
			var world = p.getServerWorld();
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(GRENADE_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e) && e.getPos().distanceTo(at) <= GRENADE_RADIUS)) {
				hit(p, e, damage);
			}
			world.spawnParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 1, 0, 0, 0, 0);
			KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.2f);
		});
		return true;
	}

	/** N6 Disparo a través: una bala que atraviesa todo lo que hay en línea. */
	private static boolean throughShot(ServerPlayerEntity p, int rank) {
		var world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, THROUGH_RANGE);
		float damage = (float) throughDamage(rank, KitSupport.scale());
		Targeting.particleLine(world, ParticleTypes.CRIT, start, end);
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(THROUGH_WIDTH),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			if (KitSupport.distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) > THROUGH_WIDTH) continue;
			hit(p, e, damage);
		}
		KitSupport.sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 0.8f);
		return true;
	}

	/** N8 Doble pistola: cada disparo lanza una bala extra. */
	private static boolean doublePistol(ServerPlayerEntity p, int rank) {
		state(p).doubleUntil = KitSupport.now(p) + doubleTicks(rank);
		Targeting.burst(p.getServerWorld(), ParticleTypes.CRIT, p, 20, 0.6);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.6f);
		return true;
	}

	/** N10 Ejecución: apuntas un segundo y rematas a hasta tres enemigos. */
	private static boolean execution(ServerPlayerEntity p, int rank) {
		if (Targeting.hostilesAround(p, EXECUTE_RANGE).isEmpty()) return false;
		KitSupport.effect(p, StatusEffects.SLOWNESS, EXECUTE_CHARGE_TICKS, 5);
		Targeting.burst(p.getServerWorld(), ParticleTypes.CRIT, p, 25, 0.6);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.4f);
		float damage = (float) executeDamage(KitSupport.scale());
		AbilityState.schedule(p.getServer(), EXECUTE_CHARGE_TICKS, () -> {
			if (!p.isAlive()) return;
			List<LivingEntity> targets = new ArrayList<>(Targeting.hostilesAround(p, EXECUTE_RANGE));
			targets.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
			for (LivingEntity e : targets.subList(0, Math.min(EXECUTE_TARGETS, targets.size()))) {
				Targeting.particleLine(p.getServerWorld(), ParticleTypes.CRIT, p.getEyePos(), e.getPos().add(0, e.getHeight() / 2, 0));
				if (executes(e.getHealth(), e.getMaxHealth(), isBoss(e))) hit(p, e, e.getHealth() + 1000f);
				else hit(p, e, damage);
			}
			KitSupport.sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Manos firmes y Pulso de tirador. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (direct || inExtra) return 0f;
		float bonus = 0f;
		int steady = RoleManager.activeRank(p, N + "1");
		if (steady > 0) bonus += (float) steadyBonus(steady, p.distanceTo(target));
		int pulse = RoleManager.activeRank(p, N + "9");
		if (pulse > 0) {
			State s = state(p);
			long now = KitSupport.now(p);
			if (pulseReady(now, s.lastShot)) bonus += (float) pulseBonus(pulse);
			s.lastShot = now;
		}
		return bonus;
	}

	/** Cabeza fría, Presión y Doble pistola. */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, boolean direct, float damage) {
		if (direct || inExtra || damage <= 0) return;
		int cool = RoleManager.activeRank(p, N + "2");
		if (cool > 0 && p.getRandom().nextDouble() < COOL_CHANCE) KitSupport.addEnergy(p, coolMana(cool));
		int pressure = RoleManager.activeRank(p, N + "5");
		if (pressure > 0 && p.getRandom().nextDouble() < pressureChance(pressure)) {
			KitSupport.effect(target, StatusEffects.SLOWNESS, PRESSURE_TICKS, 0);
		}
		State s = STATES.get(p.getUuid());
		if (s != null && windowOpen(KitSupport.now(p), s.doubleUntil)) {
			float extra = (float) (damage * DOUBLE_FRACTION);
			AbilityState.schedule(p.getServer(), DOUBLE_DELAY_TICKS, () -> {
				if (!p.isAlive() || !target.isAlive()) return;
				target.timeUntilRegen = 0;
				hit(p, target, extra);
			});
		}
	}

	/** Sin remordimientos: la 6.ª bala recorta la espera de tus activas (la llama {@code NewRoles}). */
	static void onExplosiveBullet(ServerPlayerEntity p) {
		if (RoleManager.activeRank(p, N + "7") <= 0) return;
		for (var node : dev.aptitudes.role.RoleTree.actives(dev.aptitudes.role.Role.GUNSLINGER)) {
			AbilityManager.reduceCooldown(p, node.id(), REMORSE_TICKS);
		}
		Targeting.burst(p.getServerWorld(), ParticleTypes.CRIT, p, 10, 0.5);
	}

	public static void tick(MinecraftServer server) {
		if (STATES.isEmpty() || server.getTicks() % 200 != 0) return;
		long now = server.getTicks();
		STATES.values().removeIf(s -> !windowOpen(now, s.doubleUntil) && now - s.lastShot > 600);
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** Daño de una habilidad: no encadena los efectos de disparo. */
	private static void hit(ServerPlayerEntity p, LivingEntity e, float damage) {
		boolean previous = inExtra;
		inExtra = true;
		try {
			e.damage(KitSupport.magic(p), damage);
		} finally {
			inExtra = previous;
		}
	}

	private static boolean isBoss(Entity entity) {
		if (entity instanceof AnimatedBossEntity) return true;
		for (String tag : entity.getCommandTags()) {
			if (BossKind.fromTag(tag) != null) return true;
		}
		return false;
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
