package dev.aptitudes.ability;

import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
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
import static dev.aptitudes.ability.WarlockRules.*;

/**
 * v67 (F7): las 10 habilidades nuevas del Brujo abisal (N1–N10). Cifras en {@link WarlockRules} (puro, JUnit). Sed de
 * maldiciones y Ansia de alma suman a todo daño mágico. Estado en memoria: al reiniciar el servidor se pierden el
 * Ansia, la Desesperación y la Forma demoníaca.
 */
public final class WarlockKit {
	private static final String N = "warlock_n";

	private static final class State {
		long cravingUntil;
		long freeUntil;
		long desperationReadyAt;
		long demonUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();
	private static boolean inHarvest;

	private WarlockKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", WarlockKit::evilEye);
		Abilities.register(N + "4", WarlockKit::shadowChains);
		Abilities.register(N + "6", WarlockKit::pestilenceCloud);
		Abilities.register(N + "8", WarlockKit::demonicForm);
		Abilities.register(N + "10", WarlockKit::abyssalPit);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Mal de ojo: ceguera ahora y daño a los tres segundos. */
	private static boolean evilEye(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, EVIL_EYE_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		KitSupport.effect(target, StatusEffects.BLINDNESS, EVIL_EYE_BLIND_TICKS, 0);
		Targeting.particleLine(p.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, p.getEyePos(), target.getEyePos());
		float damage = (float) evilEyeDamage(rank, KitSupport.scale());
		AbilityState.schedule(p.getServer(), EVIL_EYE_DELAY_TICKS, () -> {
			if (!p.isAlive() || !target.isAlive()) return;
			target.damage(KitSupport.magic(p), damage);
			Targeting.burst(p.getServerWorld(), ParticleTypes.SOUL, target, 12, 0.4);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_WITHER_AMBIENT, 1.4f);
		return true;
	}

	/** N4 Cadenas de sombra: inmoviliza y marchita a los enemigos del punto que miras. */
	private static boolean shadowChains(ServerPlayerEntity p, int rank) {
		Vec3d at = Targeting.lookEnd(p, CHAINS_RANGE);
		int ticks = chainsTicks(rank);
		var world = p.getServerWorld();
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(CHAINS_RADIUS),
				e -> Targeting.isHostileImpactTarget(p, e) && e.getPos().distanceTo(at) <= CHAINS_RADIUS)) {
			KitSupport.effect(e, StatusEffects.SLOWNESS, ticks, 6);
			KitSupport.effect(e, StatusEffects.WITHER, CHAINS_WITHER_TICKS, 0);
		}
		ParticleFx.ring(world, at.add(0, 0.2, 0), CHAINS_RADIUS, ParticleTypes.SQUID_INK, 24);
		KitSupport.sound(p, SoundEvents.BLOCK_CHAIN_PLACE, 0.8f);
		return true;
	}

	/** N6 Nube de pestilencia: una zona que envenena y frena. */
	private static boolean pestilenceCloud(ServerPlayerEntity p, int rank) {
		Vec3d at = Targeting.lookEnd(p, CLOUD_RANGE);
		float damage = (float) cloudDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), CLOUD_PULSES, CLOUD_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			var world = p.getServerWorld();
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(CLOUD_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e) && e.getPos().distanceTo(at) <= CLOUD_RADIUS)) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.POISON, 60, 0);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 0);
			}
			world.spawnParticles(ParticleTypes.SNEEZE, at.x, at.y + 0.5, at.z, 20, CLOUD_RADIUS / 2, 0.4, CLOUD_RADIUS / 2, 0.01);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_WITCH_THROW, 0.8f);
		return true;
	}

	/** N8 Forma demoníaca: más daño mágico, resistencia y curación al lanzar. */
	private static boolean demonicForm(ServerPlayerEntity p, int rank) {
		int ticks = demonTicks(rank);
		state(p).demonUntil = KitSupport.now(p) + ticks;
		KitSupport.effect(p, StatusEffects.RESISTANCE, ticks, 0);
		Targeting.burst(p.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, p, 30, 0.8);
		KitSupport.sound(p, SoundEvents.ENTITY_WITHER_SPAWN, 0.8f);
		return true;
	}

	/** N10 Fosa abisal: una zona que daña, marchita y te cura con cada enemigo herido. */
	private static boolean abyssalPit(ServerPlayerEntity p, int rank) {
		Vec3d at = Targeting.lookEnd(p, PIT_RANGE);
		float damage = (float) pitDamage(KitSupport.scale());
		AbilityState.repeat(p.getServer(), PIT_PULSES, PIT_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			var world = p.getServerWorld();
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(PIT_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e) && e.getPos().distanceTo(at) <= PIT_RADIUS)) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.WITHER, 40, 0);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 0);
				p.heal(PIT_HEAL);
			}
			ParticleFx.ring(world, at.add(0, 0.2, 0), PIT_RADIUS, ParticleTypes.SOUL, 36);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_WITHER_SPAWN, 1.2f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Sed de maldiciones, Ansia de alma y Forma demoníaca: bonificaciones al daño mágico. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!KitSupport.isMagic(source, direct)) return 0f;
		float bonus = 0f;
		int cursed = RoleManager.activeRank(p, N + "1");
		if (cursed > 0 && hasHarmfulEffect(target)) bonus += (float) cursedBonus(cursed);
		State s = STATES.get(p.getUuid());
		if (s == null) return bonus;
		long now = KitSupport.now(p);
		int craving = RoleManager.activeRank(p, N + "5");
		if (craving > 0 && windowOpen(now, s.cravingUntil)) bonus += (float) cravingBonus(craving);
		if (windowOpen(now, s.demonUntil)) bonus += (float) DEMON_DAMAGE;
		return bonus;
	}

	/** Desesperación: sin maná mientras dura. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		State s = STATES.get(p.getUuid());
		if (s == null || !windowOpen(KitSupport.now(p), s.freeUntil)) return 1.0;
		RoleNode node = RoleTree.get(nodeId);
		return node != null && node.role() == Role.WARLOCK ? 0.0 : 1.0;
	}

	/** Sacrificio de sangre y Forma demoníaca al lanzar un hechizo del Brujo. */
	public static void onCast(ServerPlayerEntity p, String key) {
		RoleNode node = RoleTree.get(key);
		boolean warlock = key.endsWith(".warlock") || (node != null && node.role() == Role.WARLOCK);
		if (!warlock) return;
		int blood = RoleManager.activeRank(p, N + "2");
		if (blood > 0 && RoleManager.activeRank(p, "demonic_pact") > 0) KitSupport.addEnergy(p, bloodMana(blood));
		State s = STATES.get(p.getUuid());
		if (s != null && windowOpen(KitSupport.now(p), s.demonUntil) && !key.equals(N + "8")) p.heal(DEMON_HEAL);
	}

	/** Ansia de alma y Cosecha oscura. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		long now = KitSupport.now(p);
		int craving = RoleManager.activeRank(p, N + "5");
		if (craving > 0) state(p).cravingUntil = now + CRAVING_TICKS;
		int harvest = RoleManager.activeRank(p, N + "9");
		if (harvest > 0 && !inHarvest && hasHarmfulEffect(victim)) {
			inHarvest = true;
			try {
				float damage = (float) harvestDamage(harvest, KitSupport.scale());
				var world = p.getServerWorld();
				Vec3d at = victim.getPos();
				for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(HARVEST_RADIUS),
						e -> e != victim && Targeting.isHostileImpactTarget(p, e))) {
					e.damage(KitSupport.magic(p), damage);
				}
				world.spawnParticles(ParticleTypes.SOUL, at.x, at.y + 0.8, at.z, 15, 0.8, 0.5, 0.8, 0.05);
			} finally {
				inHarvest = false;
			}
		}
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 4 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (RoleManager.activeRank(p, N + "7") <= 0) continue;
			State s = state(p);
			if (desperationTriggers(p.getHealth(), p.getMaxHealth(), now >= s.desperationReadyAt)) {
				s.desperationReadyAt = now + DESPERATION_COOLDOWN_TICKS;
				s.freeUntil = now + DESPERATION_FREE_TICKS;
				Targeting.burst(p.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, p, 25, 0.7);
				KitSupport.sound(p, SoundEvents.ENTITY_WITHER_HURT, 1.0f);
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static boolean hasHarmfulEffect(LivingEntity e) {
		return e.getStatusEffects().stream().anyMatch(i -> i.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL);
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
