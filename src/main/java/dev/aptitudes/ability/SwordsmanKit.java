package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.party.PartyManager;
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
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.SwordsmanRules.*;

/**
 * v67 (F4): las 10 habilidades nuevas del Espadachín (N1–N10). Cifras en {@link SwordsmanRules} (puro, JUnit); aquí
 * solo el comportamiento en el mundo. Estado en memoria: al reiniciar el servidor se pierden la racha, la espera de
 * Voluntad de acero y el Estado de flujo (su velocidad de ataque es un atributo temporal, no se guarda).
 */
public final class SwordsmanKit {
	private static final String N = "swordsman_n";
	private static final Identifier FLOW_ID = Aptitudes.id("swordsman/flow_attack_speed");

	private static final class State {
		int streak;
		long lastKill = -100_000;
		long steelReadyAt;
		long flowUntil;
		long lastAttack = -100_000;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private SwordsmanKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", SwordsmanKit::risingSlash);
		Abilities.register(N + "4", SwordsmanKit::windmill);
		Abilities.register(N + "6", SwordsmanKit::swordLeap);
		Abilities.register(N + "8", SwordsmanKit::flowState);
		Abilities.register(N + "10", SwordsmanKit::finalCut);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Tajo ascendente: lanza por los aires a los enemigos que tienes delante. */
	private static boolean risingSlash(ServerPlayerEntity p, int rank) {
		Vec3d facing = Targeting.horizontalFacing(p);
		float damage = (float) risingDamage(rank, KitSupport.scale());
		boolean any = false;
		for (LivingEntity e : Targeting.hostilesAround(p, RISING_RANGE)) {
			Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
			if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < 0) continue;
			e.damage(p.getDamageSources().playerAttack(p), damage);
			e.addVelocity(0, 0.9, 0);
			e.velocityModified = true;
			KitSupport.effect(e, StatusEffects.WEAKNESS, RISING_WEAKNESS_TICKS, 0);
			any = true;
		}
		Vec3d at = p.getPos().add(facing.multiply(1.5)).add(0, 1, 0);
		p.getServerWorld().spawnParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.5, at.z, 3, 0.4, 0.4, 0.4, 0);
		KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.2f);
		return any;
	}

	/** N4 Molinete: dos segundos girando; desvía proyectiles enemigos y golpea a los cercanos. */
	private static boolean windmill(ServerPlayerEntity p, int rank) {
		float damage = (float) windmillDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), WINDMILL_PULSES, WINDMILL_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			ServerWorld world = p.getServerWorld();
			for (LivingEntity e : Targeting.hostilesAround(p, WINDMILL_RADIUS)) {
				e.damage(p.getDamageSources().playerAttack(p), damage);
				e.takeKnockback(0.3, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			for (ProjectileEntity projectile : world.getEntitiesByClass(ProjectileEntity.class,
					p.getBoundingBox().expand(WINDMILL_RADIUS), pr -> true)) {
				if (projectile.getOwner() != null && PartyManager.isAlly(p, projectile.getOwner())) continue;
				world.spawnParticles(ParticleTypes.CRIT, projectile.getX(), projectile.getY(), projectile.getZ(), 6, 0.1, 0.1, 0.1, 0.1);
				projectile.discard();
			}
			ParticleFx.ring(world, p.getPos().add(0, 1, 0), WINDMILL_RADIUS * 0.8, ParticleTypes.SWEEP_ATTACK, 8);
			KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f + 0.1f * i);
		});
		return true;
	}

	/** N6 Salto de espadachín: saltas sobre un enemigo y lo golpeas desde arriba al caer. */
	private static boolean swordLeap(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, LEAP_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) {
			target = Targeting.hostilesAround(p, LEAP_RANGE).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
		}
		if (target == null) return false;
		LivingEntity victim = target;
		Vec3d delta = victim.getPos().subtract(p.getPos()).multiply(1, 0, 1);
		Vec3d velocity = delta.multiply(1 / 8.0);
		if (velocity.length() > 1.3) velocity = velocity.normalize().multiply(1.3);
		p.setVelocity(velocity.x, 0.7, velocity.z);
		p.velocityModified = true;
		p.fallDistance = 0;
		float damage = (float) leapDamage(rank, KitSupport.scale());
		AbilityState.schedule(p.getServer(), 14, () -> {
			p.fallDistance = 0;
			if (!p.isAlive() || !victim.isAlive() || p.squaredDistanceTo(victim) > 25) return;
			victim.damage(p.getDamageSources().playerAttack(p), damage);
			KitSupport.effect(victim, StatusEffects.SLOWNESS, LEAP_SLOW_TICKS, 1);
			p.getServerWorld().spawnParticles(ParticleTypes.CRIT, victim.getX(), victim.getBodyY(0.6), victim.getZ(), 20, 0.4, 0.4, 0.4, 0.3);
			KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 0.9f);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f);
		return true;
	}

	/** N8 Estado de flujo: más velocidad de ataque y cada golpe recorta las esperas de tus activas. */
	private static boolean flowState(ServerPlayerEntity p, int rank) {
		state(p).flowUntil = KitSupport.now(p) + flowTicks(rank);
		EntityAttributeInstance speed = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		if (speed != null) {
			speed.removeModifier(FLOW_ID);
			speed.addTemporaryModifier(new EntityAttributeModifier(FLOW_ID, FLOW_ATTACK_SPEED,
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
		Targeting.burst(p.getServerWorld(), ParticleTypes.CRIT, p, 25, 0.7);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.6f);
		return true;
	}

	/** N10 Corte final: tras una breve carga, un tajo enorme en cono. */
	private static boolean finalCut(ServerPlayerEntity p, int rank) {
		ServerWorld world = p.getServerWorld();
		ParticleFx.runeCircle(world, p.getPos().add(0, 0.1, 0), 2.5, ParticleTypes.CRIT);
		KitSupport.sound(p, SoundEvents.BLOCK_ANVIL_PLACE, 1.6f);
		AbilityState.schedule(p.getServer(), FINAL_CHARGE_TICKS, () -> {
			if (!p.isAlive()) return;
			Vec3d facing = Targeting.horizontalFacing(p);
			for (LivingEntity e : Targeting.hostilesAround(p, FINAL_RANGE)) {
				Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
				if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < FINAL_MIN_DOT) continue;
				e.damage(p.getDamageSources().playerAttack(p), (float) finalDamage(e.getMaxHealth(), KitSupport.scale()));
				e.addVelocity(facing.x * 0.6, 0.4, facing.z * 0.6);
				e.velocityModified = true;
			}
			for (int i = 1; i <= 5; i++) {
				Vec3d at = p.getPos().add(facing.multiply(i * 2)).add(0, 1, 0);
				world.spawnParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 2, 0.8, 0.3, 0.8, 0);
			}
			KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.6f);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Duelo, Filo penetrante, Racha de victorias y Desenvainado (bonificaciones); Estado de flujo (recorta esperas). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		int duel = RoleManager.activeRank(p, N + "1");
		int pierce = RoleManager.activeRank(p, N + "2");
		int streak = RoleManager.activeRank(p, N + "5");
		int sheathe = RoleManager.activeRank(p, N + "9");
		State s = STATES.get(p.getUuid());
		boolean flow = s != null && windowOpen(KitSupport.now(p), s.flowUntil);
		if (duel <= 0 && pierce <= 0 && streak <= 0 && sheathe <= 0 && !flow) return 0f;
		long now = KitSupport.now(p);
		float bonus = 0f;
		if (duel > 0) bonus += (float) SwordsmanRules.duel(duel, Targeting.hostilesAround(p, DUEL_RANGE).size());
		if (direct && pierce > 0) bonus += (float) pierceBonus(pierce, target.getArmor());
		if (streak > 0 && s != null && windowOpen(now, s.lastKill + STREAK_WINDOW_TICKS)) {
			bonus += (float) streakBonus(streak, s.streak);
		}
		if (direct && sheathe > 0) {
			s = state(p);
			if (sheathed(s.lastAttack, now)) {
				bonus += (float) sheatheBonus(sheathe);
				p.getServerWorld().spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
			}
			s.lastAttack = now;
		}
		if (direct && flow) {
			for (RoleNode node : RoleTree.actives(Role.SWORDSMAN)) {
				AbilityManager.reduceCooldown(p, node.id(), FLOW_COOLDOWN_CUT_TICKS);
			}
		}
		return bonus;
	}

	/** Duelo: con un solo enemigo cerca recibes menos daño. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		int duel = RoleManager.activeRank(p, N + "1");
		if (duel <= 0 || amount <= 0) return amount;
		return amount * (float) (1.0 - SwordsmanRules.duel(duel, Targeting.hostilesAround(p, DUEL_RANGE).size()));
	}

	/** Voluntad de acero: si el golpe fuera mortal y está lista, te deja con 1 de vida. Va la última de la cadena. */
	public static float lethal(ServerPlayerEntity p, float amount) {
		if (amount <= 0 || RoleManager.activeRank(p, N + "7") <= 0) return amount;
		State s = state(p);
		long now = KitSupport.now(p);
		float result = surviveLethal(p.getHealth() + p.getAbsorptionAmount(), amount, now >= s.steelReadyAt);
		if (result < amount) {
			s.steelReadyAt = now + STEEL_COOLDOWN_TICKS;
			KitSupport.effect(p, StatusEffects.RESISTANCE, STEEL_RESISTANCE_TICKS, STEEL_AMPLIFIER);
			Targeting.burst(p.getServerWorld(), ParticleTypes.TOTEM_OF_UNDYING, p, 30, 0.6);
			KitSupport.sound(p, SoundEvents.ITEM_TOTEM_USE, 1.0f);
		}
		return result;
	}

	/** Racha de victorias: cada muerte suma una carga. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		if (RoleManager.activeRank(p, N + "5") <= 0) return;
		State s = state(p);
		long now = KitSupport.now(p);
		s.streak = streakAfterKill(s.streak, s.lastKill, now);
		s.lastKill = now;
	}

	public static void tick(MinecraftServer server) {
		if (STATES.isEmpty()) return;
		long now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			State s = STATES.get(p.getUuid());
			if (s != null && s.flowUntil > 0 && !windowOpen(now, s.flowUntil)) {
				s.flowUntil = 0;
				clearFlow(p);
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		if (STATES.remove(p.getUuid()) != null) clearFlow(p);
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static void clearFlow(ServerPlayerEntity p) {
		EntityAttributeInstance speed = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		if (speed != null) speed.removeModifier(FLOW_ID);
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
