package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.MonkRules.*;

/**
 * v67 (F5): las 10 habilidades nuevas del Monje (N1–N10). Cifras en {@link MonkRules} (puro, JUnit). Estado en
 * memoria: al reiniciar el servidor se pierden combinaciones, calma y la Postura del tigre.
 */
public final class MonkKit {
	private static final String N = "monk_n";
	private static final Identifier SERENE_ID = Aptitudes.id("monk/serene_attack_speed");
	private static final Identifier TIGER_ARMOR_ID = Aptitudes.id("monk/tiger_armor");

	private static final class State {
		int combo;
		long lastHit = -100_000;
		long lastHurt = -100_000;
		long tigerUntil;
		int hits;
		final Map<UUID, Integer> shredded = new HashMap<>();
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private MonkKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", MonkKit::whirlingKick);
		Abilities.register(N + "4", MonkKit::chiWave);
		Abilities.register(N + "6", MonkKit::explosivePalm);
		Abilities.register(N + "8", MonkKit::tigerStance);
		Abilities.register(N + "10", MonkKit::voidPalm);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Patada giratoria: golpea y aparta a los enemigos a tu alrededor. */
	private static boolean whirlingKick(ServerPlayerEntity p, int rank) {
		float damage = (float) kickDamage(rank, KitSupport.scale());
		for (LivingEntity e : Targeting.hostilesAround(p, KICK_RADIUS)) {
			e.damage(p.getDamageSources().playerAttack(p), damage);
			e.takeKnockback(0.9, p.getX() - e.getX(), p.getZ() - e.getZ());
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.8, 0), KICK_RADIUS, ParticleTypes.SWEEP_ATTACK, 8);
		KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.9f);
		return true;
	}

	/** N4 Onda de chi: proyectil de energía en línea. */
	private static boolean chiWave(ServerPlayerEntity p, int rank) {
		var world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, WAVE_RANGE);
		float damage = (float) waveDamage(rank, KitSupport.scale());
		Targeting.particleLine(world, ParticleTypes.END_ROD, start, end);
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(WAVE_WIDTH),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			if (KitSupport.distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) > WAVE_WIDTH) continue;
			e.damage(KitSupport.magic(p), damage);
			KitSupport.effect(e, StatusEffects.SLOWNESS, WAVE_SLOW_TICKS, 0);
		}
		KitSupport.sound(p, SoundEvents.ENTITY_BREEZE_SHOOT, 1.2f);
		return true;
	}

	/** N6 Palma explosiva: marca al objetivo y estalla pasados tres segundos. */
	private static boolean explosivePalm(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, PALM_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		KitSupport.effect(target, StatusEffects.GLOWING, PALM_DELAY_TICKS, 0);
		float direct = (float) palmDamage(rank, KitSupport.scale());
		float splash = (float) palmSplash(rank, KitSupport.scale());
		KitSupport.sound(p, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.8f);
		AbilityState.schedule(p.getServer(), PALM_DELAY_TICKS, () -> {
			if (!p.isAlive()) return;
			Vec3d at = target.getPos();
			var world = p.getServerWorld();
			if (target.isAlive()) target.damage(KitSupport.magic(p), direct);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(PALM_RADIUS),
					e -> e != target && Targeting.isHostileImpactTarget(p, e))) {
				e.damage(KitSupport.magic(p), splash);
			}
			world.spawnParticles(ParticleTypes.EXPLOSION, at.x, at.y + 1, at.z, 1, 0, 0, 0, 0);
			ParticleFx.ring(world, at.add(0, 0.3, 0), PALM_RADIUS, ParticleTypes.END_ROD, 24);
			KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.4f);
		});
		return true;
	}

	/** N8 Postura del tigre: más daño y la armadura del enemigo se resquebraja. */
	private static boolean tigerStance(ServerPlayerEntity p, int rank) {
		State s = state(p);
		s.tigerUntil = KitSupport.now(p) + tigerTicks(rank);
		s.shredded.clear();
		Targeting.burst(p.getServerWorld(), ParticleTypes.FLAME, p, 25, 0.7);
		KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.6f);
		return true;
	}

	/** N10 Palma del vacío: un segundo de carga y un solo golpe enorme. */
	private static boolean voidPalm(ServerPlayerEntity p, int rank) {
		if (Targeting.raycastLiving(p, VOID_RANGE) == null) return false;
		KitSupport.effect(p, StatusEffects.SLOWNESS, VOID_CHARGE_TICKS, 5);
		Targeting.burst(p.getServerWorld(), ParticleTypes.END_ROD, p, 30, 0.6);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.5f);
		AbilityState.schedule(p.getServer(), VOID_CHARGE_TICKS, () -> {
			if (!p.isAlive()) return;
			LivingEntity target = Targeting.raycastLiving(p, VOID_RANGE);
			if (target == null || !Targeting.isHostileImpactTarget(p, target)) return;
			target.damage(p.getDamageSources().playerAttack(p), (float) voidDamage(target.getMaxHealth(), KitSupport.scale()));
			target.takeKnockback(1.6, p.getX() - target.getX(), p.getZ() - target.getZ());
			p.getServerWorld().spawnParticles(ParticleTypes.EXPLOSION, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0, 0, 0, 0);
			KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Manos vacías, Combinación y Postura del tigre. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!direct) return 0f;
		float bonus = 0f;
		int bare = RoleManager.activeRank(p, N + "1");
		if (bare > 0 && p.getMainHandStack().isEmpty()) bonus += (float) bareHands(bare);
		int combo = RoleManager.activeRank(p, N + "2");
		long now = KitSupport.now(p);
		if (combo > 0) {
			State s = state(p);
			s.combo = comboCount(s.combo, s.lastHit, now);
			s.lastHit = now;
			if (s.combo >= COMBO_HIT) {
				s.combo = 0;
				bonus += (float) comboBonus(combo);
				p.getServerWorld().spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
			}
		}
		State s = STATES.get(p.getUuid());
		if (s != null && windowOpen(now, s.tigerUntil)) bonus += (float) TIGER_DAMAGE;
		return bonus;
	}

	/** Punto de presión, Postura del tigre (armadura) y Eco de puño. */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, boolean direct, float damage) {
		if (!direct || damage <= 0) return;
		if (RoleManager.activeRank(p, N + "7") > 0 && p.fallDistance > 0f && !p.isOnGround() && !p.isSprinting()) {
			KitSupport.effect(target, StatusEffects.SLOWNESS, PRESSURE_TICKS, 1);
			KitSupport.effect(target, StatusEffects.WEAKNESS, PRESSURE_TICKS, 0);
		}
		State s = STATES.get(p.getUuid());
		long now = KitSupport.now(p);
		if (s != null && windowOpen(now, s.tigerUntil)) {
			int armor = tigerArmor(s.shredded.getOrDefault(target.getUuid(), 0));
			s.shredded.put(target.getUuid(), armor);
			EntityAttributeInstance attr = target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
			if (attr != null) {
				attr.removeModifier(TIGER_ARMOR_ID);
				attr.addTemporaryModifier(new EntityAttributeModifier(TIGER_ARMOR_ID, -armor, EntityAttributeModifier.Operation.ADD_VALUE));
			}
		}
		int echo = RoleManager.activeRank(p, N + "9");
		if (echo > 0) {
			s = state(p);
			s.hits++;
			if (s.hits >= ECHO_EVERY) {
				s.hits = 0;
				float wave = (float) (damage * echoFraction(echo));
				for (LivingEntity e : p.getServerWorld().getEntitiesByClass(LivingEntity.class,
						new Box(target.getPos(), target.getPos()).expand(ECHO_RADIUS),
						e -> e != target && Targeting.isHostileImpactTarget(p, e))) {
					e.damage(KitSupport.magic(p), wave);
				}
				ParticleFx.ring(p.getServerWorld(), target.getPos().add(0, 0.4, 0), ECHO_RADIUS, ParticleTypes.CLOUD, 24);
				KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f);
			}
		}
	}

	/** Apunta el último golpe recibido para Cuerpo sereno. */
	public static void onHurt(ServerPlayerEntity p, float amount) {
		if (amount > 0 && RoleManager.activeRank(p, N + "5") > 0) state(p).lastHurt = KitSupport.now(p);
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 10 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			int serene = RoleManager.activeRank(p, N + "5");
			State s = STATES.get(p.getUuid());
			EntityAttributeInstance speedAttr = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
			if (serene > 0 || s != null || (speedAttr != null && speedAttr.hasModifier(SERENE_ID))) {
				boolean active = serene > 0 && sereneActive(now, s == null ? -100_000 : s.lastHurt);
				AttributeApplier.set(p, EntityAttributes.GENERIC_ATTACK_SPEED, SERENE_ID, active ? sereneSpeed(serene) : 0.0,
						EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			}
			if (s != null && !s.shredded.isEmpty() && !windowOpen(now, s.tigerUntil)) clearTiger(p, s);
		}
	}

	public static void forget(ServerPlayerEntity p) {
		State s = STATES.remove(p.getUuid());
		if (s != null) clearTiger(p, s);
		EntityAttributeInstance speed = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		if (speed != null) speed.removeModifier(SERENE_ID);
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static void clearTiger(ServerPlayerEntity p, State s) {
		var world = p.getServerWorld();
		for (UUID id : s.shredded.keySet()) {
			if (world.getEntity(id) instanceof LivingEntity e) {
				EntityAttributeInstance attr = e.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
				if (attr != null) attr.removeModifier(TIGER_ARMOR_ID);
			}
		}
		s.shredded.clear();
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
