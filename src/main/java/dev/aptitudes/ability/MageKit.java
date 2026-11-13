package dev.aptitudes.ability;

import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.MageRules.*;
import static dev.aptitudes.ability.BerserkerRules.windowOpen;

/**
 * v67 (F3): las 10 habilidades nuevas del Mago (N1–N10). Cifras en {@link MageRules} (puro, JUnit); aquí solo el
 * comportamiento en el mundo. Estado en memoria por jugador: al reiniciar el servidor se pierden la cadena de
 * Concatenación, las cargas de Maná cristalizado, el Escudo especular y la Forma de arcano puro.
 */
public final class MageKit {
	private static final String N = "mage_n";

	private static final class State {
		final ConcatTracker concat = new ConcatTracker();
		final CrystalMeter crystal = new CrystalMeter();
		long mirrorUntil;
		int mirrorRank;
		long pureUntil;
		long emergencyReadyAt;
		/** La explosión de Maná cristalizado no puede desencadenar otra. */
		boolean exploding;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private MageKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", MageKit::chainLightning);
		Abilities.register(N + "4", MageKit::mirrorShield);
		Abilities.register(N + "6", MageKit::hailstorm);
		Abilities.register(N + "8", MageKit::purePower);
		Abilities.register(N + "10", MageKit::stellarNova);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Rayo encadenado: salta entre varios enemigos cercanos entre sí. */
	private static boolean chainLightning(ServerPlayerEntity p, int rank) {
		LivingEntity first = Targeting.raycastLiving(p, CHAIN_START_RANGE);
		if (first == null || !Targeting.isHostileImpactTarget(p, first)) {
			first = Targeting.hostilesAround(p, 12).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
		}
		if (first == null) return false;
		ServerWorld world = p.getServerWorld();
		List<LivingEntity> chain = new ArrayList<>();
		chain.add(first);
		while (chain.size() < chainTargets(rank)) {
			LivingEntity last = chain.getLast();
			LivingEntity next = world.getEntitiesByClass(LivingEntity.class,
							new Box(last.getPos(), last.getPos()).expand(CHAIN_RANGE),
							e -> !chain.contains(e) && Targeting.isHostileImpactTarget(p, e)).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(last))).orElse(null);
			if (next == null) break;
			chain.add(next);
		}
		float damage = (float) chainDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), chain.size(), 3, i -> {
			LivingEntity e = chain.get(i);
			if (!e.isAlive()) return;
			Vec3d from = i == 0 ? p.getEyePos() : chain.get(i - 1).getPos().add(0, chain.get(i - 1).getHeight() / 2, 0);
			Targeting.particleLine(world, ParticleTypes.ELECTRIC_SPARK, from, e.getPos().add(0, e.getHeight() / 2, 0));
			e.damage(KitSupport.magic(p), damage);
			KitSupport.sound(p, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 1.6f);
		});
		return true;
	}

	/** N4 Escudo especular: el siguiente golpe se anula y vuelve multiplicado. */
	private static boolean mirrorShield(ServerPlayerEntity p, int rank) {
		State s = state(p);
		s.mirrorUntil = KitSupport.now(p) + MIRROR_TICKS;
		s.mirrorRank = rank;
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 1, 0), 1.2, ParticleTypes.END_ROD, 20);
		KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f);
		return true;
	}

	/** N6 Tormenta de granizo: seis pulsos de daño y Lentitud sobre el punto que miras. */
	private static boolean hailstorm(ServerPlayerEntity p, int rank) {
		Vec3d center = Targeting.lookEnd(p, 16);
		ServerWorld world = p.getServerWorld();
		float damage = (float) hailDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), HAIL_PULSES, 20, i -> {
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
					new Box(center, center).expand(HAIL_RADIUS, 3, HAIL_RADIUS), e -> Targeting.isHostileImpactTarget(p, e))) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 1);
			}
			world.spawnParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 3, center.z, 60, HAIL_RADIUS * 0.6, 0.5,
					HAIL_RADIUS * 0.6, 0.02);
			ParticleFx.ring(world, center.add(0, 0.1, 0), HAIL_RADIUS, ParticleTypes.SNOWFLAKE, 28);
			KitSupport.sound(p, SoundEvents.BLOCK_GLASS_BREAK, 1.2f);
		});
		return true;
	}

	/** N8 Forma de arcano puro: tus activas cuestan la mitad y haces más daño mágico; al acabar, casi sin maná. */
	private static boolean purePower(ServerPlayerEntity p, int rank) {
		state(p).pureUntil = KitSupport.now(p) + pureTicks(rank);
		Targeting.burst(p.getServerWorld(), ParticleTypes.ENCHANT, p, 40, 0.9);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.4f);
		return true;
	}

	/** N10 Nova estelar: tras una breve carga, estalla alrededor, lanza a los enemigos y devuelve maná. */
	private static boolean stellarNova(ServerPlayerEntity p, int rank) {
		ServerWorld world = p.getServerWorld();
		ParticleFx.runeCircle(world, p.getPos().add(0, 0.1, 0), 3, ParticleTypes.ENCHANT);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.6f);
		AbilityState.schedule(p.getServer(), NOVA_CHARGE_TICKS, () -> {
			if (!p.isAlive()) return;
			Vec3d at = p.getPos();
			float damage = (float) (NOVA_DAMAGE * KitSupport.scale());
			int hit = 0;
			for (LivingEntity e : Targeting.hostilesAround(p, NOVA_RADIUS)) {
				e.damage(KitSupport.magic(p), damage);
				e.addVelocity(0, 0.7, 0);
				e.velocityModified = true;
				hit++;
			}
			KitSupport.addEnergy(p, novaRefund(hit));
			ParticleFx.shockwave(p.getServer(), world, at, NOVA_RADIUS, ParticleTypes.END_ROD, 8);
			world.spawnParticles(ParticleTypes.FLASH, at.x, at.y + 1, at.z, 1, 0, 0, 0, 0);
			KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.2f);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Bonificación de daño mágico: Plenitud arcana y Forma de arcano puro; además Escarcha latente y Maná cristalizado. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!KitSupport.isMagic(source, direct)) return 0f;
		RoleData data = RoleManager.get(p);
		if (!data.isActive(Role.MAGE)) return 0f;
		float bonus = (float) plenitudeBonus(RoleManager.activeRank(p, N + "1"), data.energy(), AbilityManager.maxEnergy(p));
		State s = STATES.get(p.getUuid());
		long now = KitSupport.now(p);
		if (s != null && windowOpen(now, s.pureUntil)) bonus += (float) PURE_DAMAGE;
		int frost = RoleManager.activeRank(p, N + "2");
		if (frost > 0) KitSupport.effect(target, StatusEffects.SLOWNESS, frostTicks(frost), 0);
		int crystal = RoleManager.activeRank(p, N + "9");
		if (crystal > 0 && s != null && !s.exploding && s.crystal.take()) {
			// Un tick después: la explosión no se mezcla con el golpe que la provoca.
			AbilityState.schedule(p.getServer(), 1, () -> crystalBurst(p, target, crystal));
		}
		return bonus;
	}

	private static void crystalBurst(ServerPlayerEntity p, LivingEntity center, int rank) {
		State s = state(p);
		if (!p.isAlive() || s.exploding) return;
		s.exploding = true;
		try {
			ServerWorld world = p.getServerWorld();
			float damage = (float) crystalDamage(rank, KitSupport.scale());
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
					new Box(center.getPos(), center.getPos()).expand(CRYSTAL_RADIUS), e -> Targeting.isHostileImpactTarget(p, e))) {
				e.damage(KitSupport.magic(p), damage);
			}
			world.spawnParticles(ParticleTypes.END_ROD, center.getX(), center.getBodyY(0.5), center.getZ(), 30, 0.8, 0.6, 0.8, 0.15);
			KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, 1.4f);
		} finally {
			s.exploding = false;
		}
	}

	/** Escudo especular: si está activo, anula el golpe de una criatura y se lo devuelve como daño mágico. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		State s = STATES.get(p.getUuid());
		if (s == null || amount <= 0 || !windowOpen(KitSupport.now(p), s.mirrorUntil)) return amount;
		if (!(source.getAttacker() instanceof LivingEntity attacker) || attacker == p) return amount;
		s.mirrorUntil = 0;
		float reflected = (float) (amount * mirrorMultiplier(s.mirrorRank) * KitSupport.scale());
		attacker.damage(KitSupport.magic(p), reflected);
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 1, 0), 1.4, ParticleTypes.CRIT, 16);
		Targeting.particleLine(p.getServerWorld(), ParticleTypes.END_ROD, p.getEyePos(), attacker.getEyePos());
		KitSupport.sound(p, SoundEvents.ITEM_SHIELD_BLOCK, 1.5f);
		return 0f;
	}

	/** Forma de arcano puro: mitad de coste en las activas del Mago. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		State s = STATES.get(p.getUuid());
		if (s == null || !windowOpen(KitSupport.now(p), s.pureUntil)) return 1.0;
		RoleNode node = RoleTree.get(nodeId);
		return node != null && node.role() == Role.MAGE ? PURE_COST : 1.0;
	}

	/** Concatenación: ¿la próxima activa sale gratis? */
	public static boolean concatReady(ServerPlayerEntity p) {
		State s = STATES.get(p.getUuid());
		return s != null && s.concat.ready() && RoleManager.activeRank(p, N + "5") > 0;
	}

	public static boolean concatHalves(ServerPlayerEntity p) {
		return RoleManager.activeRank(p, N + "5") >= 2;
	}

	/** Tras lanzar una activa: cuenta para Concatenación y para Maná cristalizado. */
	public static void onCast(ServerPlayerEntity p, String key, double spent, boolean concat) {
		int concatRank = RoleManager.activeRank(p, N + "5");
		if (concatRank > 0 || concat) {
			State s = state(p);
			if (concat) s.concat.take();
			if (concatRank > 0) s.concat.record(key, KitSupport.now(p));
		}
		if (spent > 0 && RoleManager.activeRank(p, N + "9") > 0) state(p).crystal.add(spent);
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		boolean emergencyCheck = now % 10 == 0;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			State s = STATES.get(p.getUuid());
			if (s != null && s.pureUntil > 0 && !windowOpen(now, s.pureUntil)) endPureForm(p, s);
			if (!emergencyCheck || !p.isAlive() || p.isSpectator() || RoleManager.activeRank(p, N + "7") <= 0) continue;
			RoleData data = RoleManager.get(p);
			s = state(p);
			double max = AbilityManager.maxEnergy(p);
			if (emergency(data.energy(), max, now, s.emergencyReadyAt)) {
				s.emergencyReadyAt = now + EMERGENCY_COOLDOWN_TICKS;
				KitSupport.addEnergy(p, max * EMERGENCY_RESTORE);
				Targeting.burst(p.getServerWorld(), ParticleTypes.ENCHANT, p, 30, 0.8);
				KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f);
			}
		}
	}

	private static void endPureForm(ServerPlayerEntity p, State s) {
		s.pureUntil = 0;
		RoleData data = RoleManager.get(p);
		data.setEnergy(pureEndEnergy(data.energy(), AbilityManager.maxEnergy(p)));
		AbilityManager.sendEnergy(p, false);
		Targeting.burst(p.getServerWorld(), ParticleTypes.SMOKE, p, 20, 0.6);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1.2f);
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
