package dev.aptitudes.ability;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.WeaponSummonerRules.*;

/**
 * v67 (F6): las 10 habilidades nuevas del Invocador de armas (N1–N10). Cifras en {@link WeaponSummonerRules} (puro,
 * JUnit). Las espadas flotantes viven en {@code NewRoles}; aquí se leen y se modifican a través de sus accesores.
 * Estado en memoria: al reiniciar el servidor se pierden la cuenta del Filo perfecto y la Armería viva.
 */
public final class WeaponSummonerKit {
	private static final String N = "weapon_summoner_n";

	private static final class State {
		int hits;
		long spareReadyAt;
		long livingUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private WeaponSummonerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", WeaponSummonerKit::boomerang);
		Abilities.register(N + "4", WeaponSummonerKit::swordWall);
		Abilities.register(N + "6", WeaponSummonerKit::steelDance);
		Abilities.register(N + "8", WeaponSummonerKit::livingArmory);
		Abilities.register(N + "10", WeaponSummonerKit::steelJudgment);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Espada boomerang: va y vuelve atravesando a los enemigos. */
	private static boolean boomerang(ServerPlayerEntity p, int rank) {
		var world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, BOOMERANG_RANGE);
		float damage = (float) boomerangDamage(rank, KitSupport.scale()) * (1f + sharpBonus(p));
		Runnable pass = () -> {
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(BOOMERANG_WIDTH),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				if (KitSupport.distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) > BOOMERANG_WIDTH) continue;
				e.timeUntilRegen = 0;
				e.damage(KitSupport.magic(p), damage);
			}
			Targeting.particleLine(world, ParticleTypes.ENCHANTED_HIT, start, end);
		};
		pass.run();
		AbilityState.schedule(p.getServer(), BOOMERANG_RETURN_TICKS, () -> {
			if (p.isAlive()) {
				pass.run();
				KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f);
			}
		});
		KitSupport.sound(p, SoundEvents.ITEM_TRIDENT_THROW.value(), 1.2f);
		return true;
	}

	/** N4 Muro de espadas: una pared que daña y frena a quien la cruza. */
	private static boolean swordWall(ServerPlayerEntity p, int rank) {
		Vec3d facing = Targeting.horizontalFacing(p);
		Vec3d center = p.getPos().add(facing.multiply(WALL_DISTANCE));
		Vec3d right = new Vec3d(-facing.z, 0, facing.x);
		float damage = (float) wallDamage(rank, KitSupport.scale()) * (1f + sharpBonus(p));
		AbilityState.repeat(p.getServer(), WALL_PULSES, WALL_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			var world = p.getServerWorld();
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
					new Box(center, center).expand(WALL_HALF_WIDTH + 1, 3, WALL_HALF_WIDTH + 1),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				Vec3d rel = e.getPos().subtract(center);
				if (Math.abs(rel.dotProduct(right)) > WALL_HALF_WIDTH || Math.abs(rel.dotProduct(facing)) > WALL_HALF_THICKNESS) continue;
				e.timeUntilRegen = 0;
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 30, 1);
			}
			for (double w = -WALL_HALF_WIDTH; w <= WALL_HALF_WIDTH; w += 1.0) {
				Vec3d at = center.add(right.multiply(w));
				world.spawnParticles(ParticleTypes.ENCHANTED_HIT, at.x, at.y + 1, at.z, 3, 0.1, 0.7, 0.1, 0.02);
			}
		});
		KitSupport.sound(p, SoundEvents.BLOCK_CHAIN_PLACE, 1.0f);
		return true;
	}

	/** N6 Danza de acero: las espadas giran y hieren a los enemigos cercanos. */
	private static boolean steelDance(ServerPlayerEntity p, int rank) {
		float damage = (float) danceDamage(rank, KitSupport.scale()) * (1f + sharpBonus(p));
		AbilityState.repeat(p.getServer(), DANCE_PULSES, DANCE_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, DANCE_RADIUS)) {
				e.timeUntilRegen = 0;
				e.damage(KitSupport.magic(p), damage);
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 1.0, 0), DANCE_RADIUS, ParticleTypes.ENCHANTED_HIT, 20);
			KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f + 0.05f * i);
		});
		return true;
	}

	/** N8 Armería viva: más espadas máximas, que se forjan más deprisa. */
	private static boolean livingArmory(ServerPlayerEntity p, int rank) {
		state(p).livingUntil = KitSupport.now(p) + livingTicks(rank);
		NewRoles.setBladeCount(p, Math.min(NewRoles.bladeCap(p), NewRoles.bladeCount(p) + LIVING_EXTRA_CAP));
		Targeting.burst(p.getServerWorld(), ParticleTypes.ENCHANTED_HIT, p, 25, 0.8);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.3f);
		return true;
	}

	/** N10 Juicio de acero: todas tus espadas y cinco más caen sobre el objetivo. */
	private static boolean steelJudgment(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, JUDGMENT_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		int swords = judgmentSwords(NewRoles.bladeCount(p));
		NewRoles.setBladeCount(p, 0);
		float damage = (float) judgmentDamage(KitSupport.scale()) * (1f + sharpBonus(p));
		float splash = (float) judgmentSplash(KitSupport.scale()) * (1f + sharpBonus(p));
		AbilityState.repeat(p.getServer(), swords, JUDGMENT_INTERVAL_TICKS, i -> {
			if (!p.isAlive() || !target.isAlive()) return;
			var world = p.getServerWorld();
			Vec3d at = target.getPos();
			Targeting.particleLine(world, ParticleTypes.ENCHANTED_HIT, at.add(0, 6, 0), at.add(0, 1, 0));
			target.timeUntilRegen = 0;
			target.damage(KitSupport.magic(p), damage);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(JUDGMENT_RADIUS),
					e -> e != target && Targeting.isHostileImpactTarget(p, e))) {
				e.timeUntilRegen = 0;
				e.damage(KitSupport.magic(p), splash);
			}
			KitSupport.sound(p, SoundEvents.ENTITY_ARROW_HIT, 1.0f + 0.03f * i);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Equilibrio y Filo perfecto (golpes directos). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!direct) return 0f;
		float bonus = 0f;
		int balance = RoleManager.activeRank(p, N + "2");
		if (balance > 0) bonus += (float) balanceBonus(balance, NewRoles.bladeCount(p));
		if (RoleManager.activeRank(p, N + "7") > 0) {
			State s = state(p);
			if (perfectHit(s.hits)) {
				bonus += (float) PERFECT_BONUS;
				p.getServerWorld().spawnParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getBodyY(0.6), target.getZ(), 12, 0.3, 0.3, 0.3, 0.2);
			}
			s.hits = perfectCount(s.hits);
		}
		return bonus;
	}

	/** Hoja de repuesto: al caer por debajo del 30 % invocas espadas. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		int rank = RoleManager.activeRank(p, N + "5");
		if (amount <= 0 || rank <= 0) return amount;
		State s = state(p);
		long now = KitSupport.now(p);
		if (spareTriggers(p.getHealth() + p.getAbsorptionAmount(), amount, p.getMaxHealth(), now >= s.spareReadyAt)) {
			s.spareReadyAt = now + SPARE_COOLDOWN_TICKS;
			NewRoles.setBladeCount(p, NewRoles.bladeCount(p) + spareBlades(rank));
			Targeting.burst(p.getServerWorld(), ParticleTypes.ENCHANTED_HIT, p, 20, 0.7);
			KitSupport.sound(p, SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 1.2f);
		}
		return amount;
	}

	// Usados desde NewRoles (ganchos de una línea).

	static float sharpBonus(ServerPlayerEntity p) {
		return (float) WeaponSummonerRules.sharpBonus(RoleManager.activeRank(p, N + "1"));
	}

	static boolean echoReturns(ServerPlayerEntity p) {
		int rank = RoleManager.activeRank(p, N + "9");
		return rank > 0 && p.getRandom().nextDouble() < echoChance(rank);
	}

	static int extraCap(ServerPlayerEntity p) {
		State s = STATES.get(p.getUuid());
		return s != null && windowOpen(KitSupport.now(p), s.livingUntil) ? LIVING_EXTRA_CAP : 0;
	}

	static boolean fastBlades(ServerPlayerEntity p) {
		State s = STATES.get(p.getUuid());
		return s != null && windowOpen(KitSupport.now(p), s.livingUntil);
	}

	public static void tick(MinecraftServer server) {
		if (STATES.isEmpty() || server.getTicks() % 600 != 0) return;
		long now = server.getTicks();
		STATES.values().removeIf(s -> !windowOpen(now, s.livingUntil) && now >= s.spareReadyAt && s.hits == 0);
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
