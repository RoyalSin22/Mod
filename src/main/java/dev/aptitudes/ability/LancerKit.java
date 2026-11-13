package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.LancerRules.*;

/**
 * v67 (F4): las 10 habilidades nuevas del Lancero (N1–N10). Cifras en {@link LancerRules} (puro, JUnit); aquí solo el
 * comportamiento en el mundo. Estado en memoria: al reiniciar el servidor se pierden las marcas, los primeros
 * contactos y la Lanza ancestral (su alcance extra es un atributo temporal, no se guarda con el jugador).
 */
public final class LancerKit {
	private static final String N = "lancer_n";
	private static final Identifier REACH_ID = Aptitudes.id("lancer/ancestral_reach");

	private static final class State {
		/** Enemigo marcado por Punta clavada → tick en que acaba. */
		final Map<UUID, Long> marks = new HashMap<>();
		/** Enemigo → último golpe tuyo (Primer contacto). */
		final Map<UUID, Long> firstHit = new HashMap<>();
		long ancestralUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();
	/** Los golpes de las propias habilidades no encadenan Ensartar ni Doble estocada. */
	private static boolean inExtra;

	private LancerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", LancerKit::javelin);
		Abilities.register(N + "4", LancerKit::spikeWall);
		Abilities.register(N + "6", LancerKit::spearFan);
		Abilities.register(N + "8", LancerKit::ancestralSpear);
		Abilities.register(N + "10", LancerKit::javelinBarrage);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Jabalina: una lanza en línea que atraviesa a los enemigos y los frena. */
	private static boolean javelin(ServerPlayerEntity p, int rank) {
		ServerWorld world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, JAVELIN_RANGE);
		float damage = (float) javelinDamage(rank, KitSupport.scale());
		ItemStackParticleEffect spear = new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.TRIDENT));
		Targeting.particleLine(world, ParticleTypes.CRIT, start, end);
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(JAVELIN_WIDTH),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			if (KitSupport.distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) > JAVELIN_WIDTH) continue;
			hit(p, e, damage);
			KitSupport.effect(e, StatusEffects.SLOWNESS, JAVELIN_SLOW_TICKS, 1);
		}
		world.spawnParticles(spear, end.x, end.y, end.z, 4, 0.1, 0.1, 0.1, 0.05);
		KitSupport.sound(p, SoundEvents.ITEM_TRIDENT_THROW.value(), 1.0f);
		return true;
	}

	/** N4 Muro de picas: cuatro segundos de picas que golpean y apartan a los enemigos cercanos. */
	private static boolean spikeWall(ServerPlayerEntity p, int rank) {
		float damage = (float) wallDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), WALL_PULSES, WALL_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, WALL_RADIUS)) {
				hit(p, e, damage);
				e.takeKnockback(0.7, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), WALL_RADIUS, ParticleTypes.CRIT, 24);
			KitSupport.sound(p, SoundEvents.ITEM_TRIDENT_HIT, 1.4f);
		});
		return true;
	}

	/** N6 Abanico de picas: cinco estocadas en cono durante un segundo. */
	private static boolean spearFan(ServerPlayerEntity p, int rank) {
		float damage = (float) fanDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), FAN_THRUSTS, FAN_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			Vec3d facing = Targeting.horizontalFacing(p);
			for (LivingEntity e : Targeting.hostilesAround(p, FAN_RANGE)) {
				Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
				if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < FAN_MIN_DOT) continue;
				hit(p, e, damage);
			}
			Vec3d at = p.getPos().add(facing.multiply(2.2)).add(0, 1, 0);
			p.getServerWorld().spawnParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 1, 0.5, 0.2, 0.5, 0);
			KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f + 0.1f * i);
		});
		return true;
	}

	/** N8 Lanza ancestral: más alcance y más daño; tus golpes atraviesan. */
	private static boolean ancestralSpear(ServerPlayerEntity p, int rank) {
		state(p).ancestralUntil = KitSupport.now(p) + ancestralTicks(rank);
		EntityAttributeInstance reach = p.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
		if (reach != null) {
			reach.removeModifier(REACH_ID);
			reach.addTemporaryModifier(new EntityAttributeModifier(REACH_ID, ANCESTRAL_REACH,
					EntityAttributeModifier.Operation.ADD_VALUE));
		}
		Targeting.burst(p.getServerWorld(), ParticleTypes.END_ROD, p, 30, 0.8);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.2f);
		return true;
	}

	/** N10 Ráfaga de jabalinas: una jabalina para cada uno de los enemigos más cercanos. */
	private static boolean javelinBarrage(ServerPlayerEntity p, int rank) {
		List<LivingEntity> targets = new ArrayList<>(Targeting.hostilesAround(p, BARRAGE_RANGE));
		if (targets.isEmpty()) return false;
		targets.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
		List<LivingEntity> chosen = targets.subList(0, Math.min(BARRAGE_TARGETS, targets.size()));
		float damage = (float) (BARRAGE_DAMAGE * KitSupport.scale());
		AbilityState.repeat(p.getServer(), chosen.size(), BARRAGE_INTERVAL_TICKS, i -> {
			LivingEntity e = chosen.get(i);
			if (!p.isAlive() || !e.isAlive()) return;
			Targeting.particleLine(p.getServerWorld(), ParticleTypes.CRIT, p.getEyePos(), e.getPos().add(0, e.getHeight() / 2, 0));
			hit(p, e, damage);
			KitSupport.sound(p, SoundEvents.ITEM_TRIDENT_THROW.value(), 1.0f + 0.05f * i);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Mantener la distancia, Punta clavada, Primer contacto y Lanza ancestral (bonificaciones de golpe directo). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!direct) return 0f;
		int distance = RoleManager.activeRank(p, N + "1");
		int mark = RoleManager.activeRank(p, N + "2");
		int first = RoleManager.activeRank(p, N + "7");
		State s = STATES.get(p.getUuid());
		if (distance <= 0 && mark <= 0 && first <= 0 && (s == null || s.ancestralUntil == 0)) return 0f;
		long now = KitSupport.now(p);
		float bonus = (float) distanceBonus(distance, p.distanceTo(target));
		if (mark > 0 && s != null) {
			Long until = s.marks.get(target.getUuid());
			if (until != null && windowOpen(now, until)) bonus += (float) markBonus(mark);
		}
		if (first > 0) {
			s = state(p);
			if (isFirstContact(s.firstHit.get(target.getUuid()), now)) {
				bonus += (float) FIRST_CONTACT_BONUS;
				p.getServerWorld().spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(), 8, 0.3, 0.3, 0.3, 0.2);
			}
			s.firstHit.put(target.getUuid(), now);
		}
		if (s != null && windowOpen(now, s.ancestralUntil)) bonus += (float) ANCESTRAL_DAMAGE;
		return bonus;
	}

	/** Ensartar (y la Lanza ancestral) atraviesan al enemigo de detrás; Doble estocada repite el golpe. */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, boolean direct, float damage) {
		if (!direct || inExtra || damage <= 0) return;
		State s = STATES.get(p.getUuid());
		double fraction = cleaveFraction(RoleManager.activeRank(p, N + "5"));
		if (s != null && windowOpen(KitSupport.now(p), s.ancestralUntil)) fraction = Math.max(fraction, ANCESTRAL_CLEAVE);
		if (fraction > 0) {
			Vec3d dir = target.getPos().subtract(p.getPos()).multiply(1, 0, 1);
			if (dir.lengthSquared() > 0.01) {
				Vec3d forward = dir.normalize();
				LivingEntity behind = p.getServerWorld().getEntitiesByClass(LivingEntity.class,
								new Box(target.getPos(), target.getPos()).expand(CLEAVE_RANGE, 1.5, CLEAVE_RANGE),
								e -> e != target && Targeting.isHostileImpactTarget(p, e)
										&& e.getPos().subtract(target.getPos()).multiply(1, 0, 1).dotProduct(forward) > 0).stream()
						.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(target))).orElse(null);
				if (behind != null) {
					hit(p, behind, (float) (damage * fraction));
					Targeting.particleLine(p.getServerWorld(), ParticleTypes.CRIT, target.getEyePos(), behind.getEyePos());
				}
			}
		}
		double chance = doubleChance(RoleManager.activeRank(p, N + "9"));
		if (chance > 0 && p.getRandom().nextDouble() < chance) {
			float repeated = (float) (damage * DOUBLE_FRACTION);
			AbilityState.schedule(p.getServer(), DOUBLE_DELAY_TICKS, () -> {
				if (!p.isAlive() || !target.isAlive() || p.squaredDistanceTo(target) > 64) return;
				hit(p, target, repeated);
				p.getServerWorld().spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getBodyY(0.6), target.getZ(), 1, 0.2, 0.2, 0.2, 0);
			});
		}
	}

	/** Punta clavada: tras una activa de Lancero, los enemigos que acabas de herir quedan marcados. */
	public static void onCast(ServerPlayerEntity p, String key) {
		if (RoleManager.activeRank(p, N + "2") <= 0) return;
		RoleNode node = RoleTree.get(key);
		if (node == null || node.role() != Role.LANCER) return;
		ServerWorld world = p.getServerWorld();
		State s = state(p);
		AbilityState.repeat(p.getServer(), MARK_SCAN_TICKS / 2, 2, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(24),
					e -> e.getAttacker() == p && e.hurtTime > 0 && Targeting.isHostileImpactTarget(p, e))) {
				s.marks.put(e.getUuid(), KitSupport.now(p) + MARK_TICKS);
				world.spawnParticles(ParticleTypes.CRIT, e.getX(), e.getBodyY(0.9), e.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
			}
		});
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (STATES.isEmpty()) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			State s = STATES.get(p.getUuid());
			if (s == null) continue;
			if (s.ancestralUntil > 0 && !windowOpen(now, s.ancestralUntil)) {
				s.ancestralUntil = 0;
				clearReach(p);
			}
			if (now % 200 == 0) {
				s.marks.values().removeIf(until -> !windowOpen(now, until));
				s.firstHit.values().removeIf(at -> now - at > FIRST_CONTACT_RESET_TICKS);
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		if (STATES.remove(p.getUuid()) != null) clearReach(p);
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** Daño de una habilidad: golpe directo del jugador, sin encadenar Ensartar ni Doble estocada. */
	private static void hit(ServerPlayerEntity p, LivingEntity e, float damage) {
		boolean previous = inExtra;
		inExtra = true;
		try {
			e.damage(p.getDamageSources().playerAttack(p), damage);
		} finally {
			inExtra = previous;
		}
	}

	private static void clearReach(ServerPlayerEntity p) {
		EntityAttributeInstance reach = p.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
		if (reach != null) reach.removeModifier(REACH_ID);
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
