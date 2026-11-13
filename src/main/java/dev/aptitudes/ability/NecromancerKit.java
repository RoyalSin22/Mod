package dev.aptitudes.ability;

import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.NecromancerRules.*;

/**
 * v67 (F4): las 10 habilidades nuevas del Nigromante (N1–N10). Cifras en {@link NecromancerRules} (puro, JUnit); aquí
 * solo el comportamiento en el mundo. Estado en memoria: al reiniciar el servidor se pierde la espera de Resurgir.
 * Los esbirros ya son efímeros (no se guardan), así que nada de lo que toca a los esbirros sobrevive a un reinicio.
 */
public final class NecromancerKit {
	private static final String N = "necromancer_n";

	/** Jugador → tick en que Resurgir vuelve a estar lista. */
	private static final Map<UUID, Long> RESURGE_READY = new HashMap<>();
	/** Sacrificio mata a su propio esbirro: Resurgir no debe salvarlo. */
	private static boolean sacrificing;

	private NecromancerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", NecromancerKit::boneHand);
		Abilities.register(N + "4", NecromancerKit::sacrifice);
		Abilities.register(N + "6", NecromancerKit::accursedGround);
		Abilities.register(N + "8", NecromancerKit::necroticFrenzy);
		Abilities.register(N + "10", NecromancerKit::soulStorm);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Mano de huesos: una mano agarra al enemigo que miras y lo deja inmóvil. */
	private static boolean boneHand(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, HAND_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		target.damage(KitSupport.magic(p), (float) handDamage(rank, KitSupport.scale()));
		KitSupport.effect(target, StatusEffects.SLOWNESS, rootTicks(rank), 6);
		ServerWorld world = p.getServerWorld();
		ParticleFx.column(world, target.getPos(), 2, ParticleTypes.SOUL_FIRE_FLAME, 14);
		world.spawnParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 0.2, target.getZ(), 14, 0.3, 0.1, 0.3, 0.03);
		Targeting.particleLine(world, ParticleTypes.SOUL, p.getEyePos(), target.getPos().add(0, 0.5, 0));
		KitSupport.sound(p, SoundEvents.ENTITY_SKELETON_AMBIENT, 0.6f);
		return true;
	}

	/** N4 Sacrificio: hace estallar al esbirro más cercano; hiere a los enemigos alrededor y te cura. */
	private static boolean sacrifice(ServerPlayerEntity p, int rank) {
		List<MobEntity> minions = AbilityState.minionsOf(p);
		MobEntity minion = minions.stream().min(Comparator.comparingDouble(m -> m.squaredDistanceTo(p))).orElse(null);
		if (minion == null) return false;
		Vec3d at = minion.getPos();
		ServerWorld world = p.getServerWorld();
		float damage = (float) sacrificeDamage(rank, KitSupport.scale());
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(SACRIFICE_RADIUS, 2, SACRIFICE_RADIUS),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			e.damage(p.getDamageSources().explosion(p, p), damage);
		}
		p.heal(SACRIFICE_HEAL);
		sacrificing = true;
		try {
			minion.kill();
		} finally {
			sacrificing = false;
		}
		world.spawnParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 2, 0.4, 0.2, 0.4, 0);
		world.spawnParticles(ParticleTypes.SOUL, at.x, at.y + 1, at.z, 30, 0.6, 0.6, 0.6, 0.1);
		KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.3f);
		return true;
	}

	/** N6 Tierra maldita: una zona donde los enemigos van lentos y pierden vida; tú recuperas parte de ella. */
	private static boolean accursedGround(ServerPlayerEntity p, int rank) {
		Vec3d center = Targeting.lookEnd(p, GROUND_RANGE);
		ServerWorld world = p.getServerWorld();
		float damage = (float) groundDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), GROUND_PULSES, 20, i -> {
			float dealt = 0;
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(GROUND_RADIUS, 3, GROUND_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 1);
				dealt += damage;
			}
			if (dealt > 0 && p.isAlive()) p.heal((float) (dealt * GROUND_LIFESTEAL));
			ParticleFx.ring(world, center.add(0, 0.1, 0), GROUND_RADIUS, ParticleTypes.SOUL_FIRE_FLAME, 32);
			world.spawnParticles(ParticleTypes.SOUL, center.x, center.y + 0.3, center.z, 10, GROUND_RADIUS * 0.4, 0.2, GROUND_RADIUS * 0.4, 0.02);
		});
		KitSupport.sound(p, SoundEvents.PARTICLE_SOUL_ESCAPE.value(), 1.0f);
		return true;
	}

	/** N8 Frenesí necrótico: tus esbirros cercanos ganan Fuerza, Velocidad y Resistencia. */
	private static boolean necroticFrenzy(ServerPlayerEntity p, int rank) {
		int ticks = frenzyTicks(rank);
		boolean any = false;
		for (MobEntity minion : AbilityState.minionsOf(p)) {
			if (minion.squaredDistanceTo(p) > FRENZY_RANGE * FRENZY_RANGE) continue;
			KitSupport.effect(minion, StatusEffects.STRENGTH, ticks, 1);
			KitSupport.effect(minion, StatusEffects.SPEED, ticks, 1);
			KitSupport.effect(minion, StatusEffects.RESISTANCE, ticks, 0);
			Targeting.burst(p.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, minion, 12, 0.5);
			any = true;
		}
		if (any) KitSupport.sound(p, SoundEvents.ENTITY_WITHER_AMBIENT, 1.2f);
		return any;
	}

	/** N10 Tormenta de almas: cinco segundos de almas que giran, hieren y te curan. */
	private static boolean soulStorm(ServerPlayerEntity p, int rank) {
		ServerWorld world = p.getServerWorld();
		float damage = (float) (STORM_DAMAGE * KitSupport.scale());
		float[] healed = {0f};
		AbilityState.repeat(p.getServer(), STORM_PULSES, STORM_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, STORM_RADIUS)) {
				e.timeUntilRegen = 0;
				e.damage(KitSupport.magic(p), damage);
				float heal = stormHeal(healed[0]);
				if (heal > 0) {
					p.heal(heal);
					healed[0] += heal;
				}
			}
			ParticleFx.spiral(world, p.getPos(), STORM_RADIUS * 0.7, 3, ParticleTypes.SOUL, 40, 2);
			ParticleFx.ring(world, p.getPos().add(0, 0.3, 0), STORM_RADIUS, ParticleTypes.SOUL_FIRE_FLAME, 40);
			KitSupport.sound(p, SoundEvents.PARTICLE_SOUL_ESCAPE.value(), 0.8f + 0.05f * i);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Orden de ataque: tus esbirros cercanos acuden al enemigo que acabas de golpear. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!direct) return 0f;
		int rank = RoleManager.activeRank(p, N + "1");
		if (rank <= 0 || !Targeting.isHostileImpactTarget(p, target)) return 0f;
		double range = orderRange(rank);
		for (MobEntity minion : AbilityState.minionsOf(p)) {
			if (minion == target || minion.getTarget() == target || minion.squaredDistanceTo(p) > range * range) continue;
			minion.setTarget(target);
		}
		return 0f;
	}

	/** Banquete: cuando un esbirro tuyo mata, te curas y recuperas maná. */
	public static void onMinionKill(ServerPlayerEntity owner, LivingEntity victim) {
		int rank = RoleManager.activeRank(owner, N + "2");
		if (rank <= 0) return;
		owner.heal(banquetHeal(rank));
		KitSupport.addEnergy(owner, banquetMana(rank));
		Targeting.burst(owner.getServerWorld(), ParticleTypes.SOUL, owner, 8, 0.4);
	}

	/** Resurgir: un esbirro que iba a morir sobrevive con la mitad de su vida (una vez cada minuto). */
	public static float minionIncoming(LivingEntity minion, float amount) {
		if (sacrificing || !(AbilityState.ownerOf(minion) instanceof ServerPlayerEntity owner)) return amount;
		if (RoleManager.activeRank(owner, N + "7") <= 0) return amount;
		long now = KitSupport.now(owner);
		boolean ready = now >= RESURGE_READY.getOrDefault(owner.getUuid(), 0L);
		if (!resurges(minion.getHealth(), amount, ready)) return amount;
		RESURGE_READY.put(owner.getUuid(), now + RESURGE_COOLDOWN_TICKS);
		minion.setHealth((float) (minion.getMaxHealth() * RESURGE_HEALTH));
		Targeting.burst(owner.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, minion, 20, 0.6);
		KitSupport.sound(owner, SoundEvents.ENTITY_WITHER_SPAWN, 1.8f);
		return 0f;
	}

	/** Vínculo de huesos: tu esbirro más cercano recibe una parte del daño que te harían. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		int rank = RoleManager.activeRank(p, N + "9");
		if (rank <= 0 || amount <= 0) return amount;
		MobEntity nearest = AbilityState.minionsOf(p).stream()
				.min(Comparator.comparingDouble(m -> m.squaredDistanceTo(p))).orElse(null);
		if (nearest == null) return amount;
		float shared = (float) (amount * boneShare(rank));
		nearest.damage(p.getDamageSources().generic(), shared);
		Targeting.particleLine(p.getServerWorld(), ParticleTypes.SOUL, p.getEyePos(), nearest.getEyePos());
		return amount - shared;
	}

	/** Aura putrefacta: cada segundo, los enemigos a 4 bloques sufren Debilidad y pierden vida. */
	public static void tick(MinecraftServer server) {
		if (server.getTicks() % 20 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			int rank = RoleManager.activeRank(p, N + "5");
			if (rank <= 0 || !p.isAlive() || p.isSpectator()) continue;
			float damage = (float) auraDamage(rank, KitSupport.scale());
			for (LivingEntity e : Targeting.hostilesAround(p, AURA_RADIUS)) {
				KitSupport.effect(e, StatusEffects.WEAKNESS, AURA_WEAKNESS_TICKS, 0);
				e.timeUntilRegen = 0;
				e.damage(KitSupport.magic(p), damage);
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.2, 0), AURA_RADIUS, ParticleTypes.SOUL_FIRE_FLAME, 20);
		}
	}

	public static void forget(ServerPlayerEntity p) {
		RESURGE_READY.remove(p.getUuid());
	}
}
