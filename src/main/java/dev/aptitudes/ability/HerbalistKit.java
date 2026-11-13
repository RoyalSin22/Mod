package dev.aptitudes.ability;

import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.HerbalistRules.*;
import static dev.aptitudes.ability.BerserkerRules.windowOpen;

/**
 * v67 (F3): las 10 habilidades nuevas del Herbolista (N1–N10). Cifras en {@link HerbalistRules} (puro, JUnit); aquí
 * solo el comportamiento en el mundo. Estado en memoria por jugador: al reiniciar el servidor se pierden la espera de
 * Corteza viva, el tiempo quieto de Raíz profunda y las inmunidades de Antídoto universal.
 */
public final class HerbalistKit {
	private static final String N = "herbalist_n";

	private static final class State {
		long barkReadyAt;
		int stillTicks;
		Vec3d lastPos = Vec3d.ZERO;
	}

	/** Inmunidades de Antídoto universal: jugador → efecto → tick en que acaba. */
	private static final Map<UUID, Map<RegistryEntry<StatusEffect>, Long>> IMMUNITY = new HashMap<>();
	private static final Map<UUID, State> STATES = new HashMap<>();

	private HerbalistKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", HerbalistKit::explosiveSeed);
		Abilities.register(N + "4", HerbalistKit::healingRoot);
		Abilities.register(N + "6", HerbalistKit::locustPlague);
		Abilities.register(N + "8", HerbalistKit::colossalBloom);
		Abilities.register(N + "10", HerbalistKit::livingSeedRain);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Semilla explosiva: estalla a los 2 s, envenena y deja un matorral que cura a los aliados. */
	private static boolean explosiveSeed(ServerPlayerEntity p, int rank) {
		Vec3d spot = Targeting.lookEnd(p, SEED_RANGE);
		ServerWorld world = p.getServerWorld();
		Targeting.particleLine(world, ParticleTypes.COMPOSTER, p.getEyePos(), spot);
		KitSupport.sound(p, SoundEvents.ENTITY_SNOWBALL_THROW, 0.7f);
		float damage = (float) seedDamage(rank, KitSupport.scale());
		AbilityState.schedule(p.getServer(), SEED_FUSE_TICKS, () -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(spot, spot).expand(SEED_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.POISON, SEED_POISON_TICKS, 0);
			}
			world.spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, spot.x, spot.y + 0.5, spot.z, 50, 1.2, 0.6, 1.2, 0.02);
			world.spawnParticles(ParticleTypes.EXPLOSION, spot.x, spot.y + 0.3, spot.z, 2, 0.4, 0.1, 0.4, 0);
			KitSupport.sound(p, SoundEvents.BLOCK_SWEET_BERRY_BUSH_BREAK, 0.8f);
			// Matorral: cura a los aliados cercanos cada segundo (efecto, sin bloques).
			AbilityState.repeat(p.getServer(), BUSH_SECONDS, 20, i -> {
				for (ServerPlayerEntity ally : alliesAround(p, spot, SEED_RADIUS)) healAlly(p, ally, BUSH_HEAL);
				ParticleFx.ring(world, spot.add(0, 0.1, 0), SEED_RADIUS, ParticleTypes.HAPPY_VILLAGER, 16);
			});
		});
		return true;
	}

	/** N4 Raíz curativa: zona que cura a los aliados y frena a los enemigos. */
	private static boolean healingRoot(ServerPlayerEntity p, int rank) {
		Vec3d center = p.getPos();
		ServerWorld world = p.getServerWorld();
		AbilityState.repeat(p.getServer(), rootTicks(rank) / 20, 20, i -> {
			for (ServerPlayerEntity ally : alliesAround(p, center, ROOT_RADIUS)) healAlly(p, ally, ROOT_HEAL);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(ROOT_RADIUS, 2, ROOT_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 1);
			}
			ParticleFx.ring(world, center.add(0, 0.1, 0), ROOT_RADIUS, ParticleTypes.COMPOSTER, 32);
			world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y + 0.5, center.z, 12, ROOT_RADIUS * 0.4, 0.4, ROOT_RADIUS * 0.4, 0);
		});
		KitSupport.sound(p, SoundEvents.BLOCK_ROOTS_PLACE, 1.0f);
		return true;
	}

	/** N6 Plaga de langostas: un enjambre sigue al objetivo y lo debilita. */
	private static boolean locustPlague(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, 24);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) {
			target = Targeting.hostilesAround(p, 16).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
		}
		if (target == null) return false;
		LivingEntity victim = target;
		float damage = (float) locustDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), LOCUST_SECONDS, 20, i -> {
			if (!victim.isAlive()) return;
			victim.timeUntilRegen = 0;
			victim.damage(KitSupport.magic(p), damage);
			KitSupport.effect(victim, StatusEffects.WEAKNESS, 40, 0);
			p.getServerWorld().spawnParticles(ParticleTypes.ASH, victim.getX(), victim.getBodyY(0.7), victim.getZ(), 30, 0.4, 0.5, 0.4, 0.05);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_BEE_LOOP_AGGRESSIVE, 1.0f);
		return true;
	}

	/** N8 Floración colosal: cura una gran parte de la vida de los aliados cercanos y envenena a los enemigos. */
	private static boolean colossalBloom(ServerPlayerEntity p, int rank) {
		ServerWorld world = p.getServerWorld();
		Vec3d at = p.getPos();
		for (ServerPlayerEntity ally : alliesAround(p, at, BLOOM_RADIUS)) {
			healAlly(p, ally, (float) (ally.getMaxHealth() * bloomFraction(rank)));
			KitSupport.effect(ally, StatusEffects.ABSORPTION, BLOOM_ABSORPTION_TICKS, 1);
			Targeting.burst(world, ParticleTypes.HAPPY_VILLAGER, ally, 20, 0.6);
		}
		for (LivingEntity e : Targeting.hostilesAround(p, BLOOM_RADIUS)) KitSupport.effect(e, StatusEffects.POISON, 100, 0);
		ParticleFx.shockwave(p.getServer(), world, at, BLOOM_RADIUS, ParticleTypes.CHERRY_LEAVES, 6);
		KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f);
		return true;
	}

	/** N10 Lluvia de semillas vivas: 12 s curando, limpiando a los aliados y envenenando a los enemigos. */
	private static boolean livingSeedRain(ServerPlayerEntity p, int rank) {
		Vec3d center = p.getPos();
		ServerWorld world = p.getServerWorld();
		AbilityState.repeat(p.getServer(), RAIN_SECONDS, 20, second -> {
			for (ServerPlayerEntity ally : alliesAround(p, center, RAIN_RADIUS)) {
				healAlly(p, ally, RAIN_HEAL);
				if (rainCleanses(second)) cleanse(p, ally);
			}
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(RAIN_RADIUS, 3, RAIN_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				KitSupport.effect(e, StatusEffects.POISON, 40, 0);
			}
			world.spawnParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, center.x, center.y + 4, center.z, 80, RAIN_RADIUS * 0.5, 0.5, RAIN_RADIUS * 0.5, 0);
			ParticleFx.ring(world, center.add(0, 0.1, 0), RAIN_RADIUS, ParticleTypes.HAPPY_VILLAGER, 40);
		});
		KitSupport.sound(p, SoundEvents.WEATHER_RAIN, 1.4f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Primavera: multiplica la curación de tus habilidades de curar. */
	public static float heal(ServerPlayerEntity p, float amount) {
		return (float) (amount * healMultiplier(RoleManager.activeRank(p, N + "1")));
	}

	/** Polinización: los aliados curados ganan Velocidad I y algo de absorción. */
	public static void onHealed(ServerPlayerEntity p, LivingEntity ally) {
		int rank = RoleManager.activeRank(p, N + "2");
		if (rank <= 0) return;
		KitSupport.effect(ally, StatusEffects.SPEED, POLLEN_SPEED_TICKS, 0);
		ally.setAbsorptionAmount(Math.max(ally.getAbsorptionAmount(), pollenAbsorption(rank)));
	}

	/** Antídoto universal: al quitar un efecto negativo, 5 s de inmunidad a ese efecto. */
	public static void onCleanse(ServerPlayerEntity p, LivingEntity who, RegistryEntry<StatusEffect> effect) {
		if (!(who instanceof ServerPlayerEntity target) || RoleManager.activeRank(p, N + "7") <= 0) return;
		IMMUNITY.computeIfAbsent(target.getUuid(), u -> new HashMap<>()).put(effect, KitSupport.now(p) + IMMUNITY_TICKS);
	}

	/** Corteza viva (Regeneración al caer de vida) y Raíz profunda (menos daño y espinas estando quieto). */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		int bark = RoleManager.activeRank(p, N + "5");
		int rooted = RoleManager.activeRank(p, N + "9");
		if (bark <= 0 && rooted <= 0) return amount;
		State s = state(p);
		long now = KitSupport.now(p);
		if (rooted > 0 && s.stillTicks >= STILL_TICKS) {
			amount *= (float) (1.0 - rootedReduction(rooted));
			if (source.getAttacker() instanceof LivingEntity attacker && attacker != p && source.getSource() == attacker) {
				attacker.damage(p.getDamageSources().thorns(p), rootedThorns(rooted));
			}
		}
		if (bark > 0 && barkTriggers(p.getHealth() - amount, p.getMaxHealth(), now, s.barkReadyAt)) {
			s.barkReadyAt = now + BARK_COOLDOWN_TICKS;
			KitSupport.effect(p, StatusEffects.REGENERATION, BARK_TICKS, barkAmplifier(bark));
			Targeting.burst(p.getServerWorld(), ParticleTypes.COMPOSTER, p, 14, 0.5);
		}
		return amount;
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (RoleManager.activeRank(p, N + "9") > 0) {
				State s = state(p);
				boolean still = p.getPos().squaredDistanceTo(s.lastPos) < 0.0004;
				s.stillTicks = still ? s.stillTicks + 1 : 0;
				s.lastPos = p.getPos();
			}
			Map<RegistryEntry<StatusEffect>, Long> immune = IMMUNITY.get(p.getUuid());
			if (immune == null) continue;
			for (Iterator<Map.Entry<RegistryEntry<StatusEffect>, Long>> it = immune.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<RegistryEntry<StatusEffect>, Long> entry = it.next();
				if (!windowOpen(now, entry.getValue())) it.remove();
				else if (p.hasStatusEffect(entry.getKey())) p.removeStatusEffect(entry.getKey());
			}
			if (immune.isEmpty()) IMMUNITY.remove(p.getUuid());
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
		IMMUNITY.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** Jugadores aliados (y tú) a menos de {@code radius} de {@code center}. */
	private static List<ServerPlayerEntity> alliesAround(ServerPlayerEntity p, Vec3d center, double radius) {
		List<ServerPlayerEntity> out = new ArrayList<>();
		for (var ally : Targeting.playersInSupportArea(p, new Box(center, center).expand(radius, 3, radius))) {
			if (ally instanceof ServerPlayerEntity sp) out.add(sp);
		}
		return out;
	}

	/** Cura a un aliado con Primavera y le aplica Polinización. */
	private static void healAlly(ServerPlayerEntity p, ServerPlayerEntity ally, float amount) {
		ally.heal(heal(p, amount));
		onHealed(p, ally);
	}

	/** Quita los efectos negativos de un aliado y le da inmunidad breve (Antídoto universal). */
	private static void cleanse(ServerPlayerEntity p, ServerPlayerEntity ally) {
		for (StatusEffectInstance effect : new ArrayList<>(ally.getStatusEffects())) {
			if (effect.getEffectType().value().getCategory() != StatusEffectCategory.HARMFUL) continue;
			ally.removeStatusEffect(effect.getEffectType());
			onCleanse(p, ally, effect.getEffectType());
		}
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
