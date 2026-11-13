package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.morph.MorphForm;
import dev.aptitudes.morph.MorphManager;
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
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.ShapeshifterRules.*;

/**
 * v67 (F6): las 10 habilidades nuevas del Cambiaformas (N1–N10). Cifras en {@link ShapeshifterRules} (puro, JUnit).
 * Estado en memoria: al reiniciar el servidor se pierden Presa fácil, Tercer cambio y la Furia de la bestia.
 */
public final class ShapeshifterKit {
	private static final String N = "shapeshifter_n";
	private static final Identifier FURY_ID = Aptitudes.id("shapeshifter/fury_attack_speed");

	private static final class State {
		boolean wasMorphed;
		long easyPreyUntil;
		boolean cheapPower;
		long furyUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private ShapeshifterKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", ShapeshifterKit::savageBite);
		Abilities.register(N + "4", ShapeshifterKit::quickChange);
		Abilities.register(N + "6", ShapeshifterKit::multiSwipe);
		Abilities.register(N + "8", ShapeshifterKit::beastFury);
		Abilities.register(N + "10", ShapeshifterKit::primalStampede);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Mordisco salvaje: muerde al enemigo que miras y te curas. */
	private static boolean savageBite(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, BITE_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		target.damage(p.getDamageSources().playerAttack(p), (float) biteDamage(rank, KitSupport.scale()));
		p.heal(biteHeal(rank));
		p.getServerWorld().spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.6), target.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
		KitSupport.sound(p, SoundEvents.ENTITY_WOLF_GROWL, 1.0f);
		return true;
	}

	/** N4 Cambio relámpago: pasas a la forma siguiente (o vuelves a humano) y ganas Velocidad II. */
	private static boolean quickChange(ServerPlayerEntity p, int rank) {
		List<MorphForm> forms = Arrays.stream(MorphForm.values())
				.filter(f -> f.selectable() && MorphManager.isUnlocked(p, f)).toList();
		if (forms.isEmpty()) return false;
		MorphForm current = MorphManager.currentForm(p);
		int next = nextForm(current == null ? -1 : forms.indexOf(current), forms.size());
		if (MorphManager.get(p).isMorphed()) MorphManager.unmorph(p);
		if (next >= 0) {
			MorphForm form = forms.get(next);
			MorphManager.select(p, form);
			MorphManager.morph(p, form.type(), 0, 0f);
		}
		KitSupport.effect(p, StatusEffects.SPEED, swiftTicks(rank), 1);
		Targeting.burst(p.getServerWorld(), ParticleTypes.POOF, p, 20, 0.6);
		KitSupport.sound(p, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.2f);
		return true;
	}

	/** N6 Zarpazo múltiple: cuatro zarpazos en cono. */
	private static boolean multiSwipe(ServerPlayerEntity p, int rank) {
		float damage = (float) swipeDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), CLAW_SWIPES, CLAW_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			Vec3d facing = Targeting.horizontalFacing(p);
			for (LivingEntity e : Targeting.hostilesAround(p, CLAW_RANGE)) {
				Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
				if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < CLAW_MIN_DOT) continue;
				e.damage(p.getDamageSources().playerAttack(p), damage);
			}
			Vec3d at = p.getPos().add(facing.multiply(2.0)).add(0, 1, 0);
			p.getServerWorld().spawnParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 1, 0.5, 0.2, 0.5, 0);
			KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f + 0.1f * i);
		});
		return true;
	}

	/** N8 Furia de la bestia: más daño y más velocidad de ataque. */
	private static boolean beastFury(ServerPlayerEntity p, int rank) {
		state(p).furyUntil = KitSupport.now(p) + furyTicks(rank);
		AttributeApplier.set(p, EntityAttributes.GENERIC_ATTACK_SPEED, FURY_ID, FURY_ATTACK_SPEED,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		Targeting.burst(p.getServerWorld(), ParticleTypes.ANGRY_VILLAGER, p, 15, 0.7);
		KitSupport.sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.9f);
		return true;
	}

	/** N10 Estampida primigenia: embestidas que hieren y empujan a los enemigos cercanos. */
	private static boolean primalStampede(ServerPlayerEntity p, int rank) {
		float damage = (float) stampedeDamage(KitSupport.scale());
		AbilityState.repeat(p.getServer(), STAMPEDE_PULSES, STAMPEDE_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, STAMPEDE_RADIUS)) {
				e.damage(p.getDamageSources().playerAttack(p), damage);
				e.takeKnockback(0.9, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), STAMPEDE_RADIUS, ParticleTypes.CLOUD, 24);
			KitSupport.sound(p, SoundEvents.ENTITY_RAVAGER_STEP, 1.0f);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Presa fácil y Furia de la bestia. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		State s = STATES.get(p.getUuid());
		if (s == null) return 0f;
		long now = KitSupport.now(p);
		float bonus = 0f;
		if (windowOpen(now, s.easyPreyUntil) && RoleManager.activeRank(p, N + "7") > 0) {
			s.easyPreyUntil = 0;
			bonus += (float) EASY_PREY_BONUS;
			Targeting.burst(p.getServerWorld(), ParticleTypes.CRIT, target, 8, 0.3);
		}
		if (windowOpen(now, s.furyUntil)) bonus += (float) FURY_DAMAGE;
		return bonus;
	}

	/** Sangre fría: transformado, recibes menos daño. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		int rank = RoleManager.activeRank(p, N + "2");
		if (amount <= 0 || rank <= 0 || !MorphManager.get(p).isMorphed()) return amount;
		return amount * (float) (1.0 - coldBlood(rank));
	}

	/** Tercer cambio: el siguiente poder de forma cuesta menos. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		State s = STATES.get(p.getUuid());
		if (s == null || !s.cheapPower || !"form_power".equals(nodeId)) return 1.0;
		return thirdChangeCost(RoleManager.activeRank(p, N + "9"));
	}

	public static void onCast(ServerPlayerEntity p, String key) {
		if (!"form_power".equals(key)) return;
		State s = STATES.get(p.getUuid());
		if (s != null) s.cheapPower = false;
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 5 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			boolean morphed = MorphManager.get(p).isMorphed();
			State s = STATES.get(p.getUuid());
			if (s == null) {
				boolean relevant = RoleManager.activeRank(p, N + "5") > 0 || RoleManager.activeRank(p, N + "7") > 0
						|| RoleManager.activeRank(p, N + "9") > 0 || RoleManager.activeRank(p, N + "1") > 0;
				if (!relevant) continue;
				s = state(p);
				s.wasMorphed = morphed;
			}
			if (morphed && !s.wasMorphed) {
				if (RoleManager.activeRank(p, N + "7") > 0) s.easyPreyUntil = now + EASY_PREY_WINDOW_TICKS;
				if (RoleManager.activeRank(p, N + "9") > 0) s.cheapPower = true;
			} else if (!morphed && s.wasMorphed) {
				int skin = RoleManager.activeRank(p, N + "5");
				if (skin > 0) giveSkin(p, skinAbsorption(skin));
				s.easyPreyUntil = 0;
				s.cheapPower = false;
			}
			s.wasMorphed = morphed;
			if (morphed && now % 60 == 0 && RoleManager.activeRank(p, N + "1") > 0) {
				p.heal(metabolismHeal(RoleManager.activeRank(p, N + "1")));
			}
			if (s.furyUntil > 0 && !windowOpen(now, s.furyUntil)) {
				s.furyUntil = 0;
				clearFury(p);
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		if (STATES.remove(p.getUuid()) != null) clearFury(p);
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static void giveSkin(ServerPlayerEntity p, float amount) {
		float before = p.getAbsorptionAmount();
		p.setAbsorptionAmount(Math.max(before, amount));
		AbilityState.schedule(p.getServer(), SKIN_TICKS, () -> {
			if (p.isAlive() && p.getAbsorptionAmount() > before) p.setAbsorptionAmount(Math.max(before, p.getAbsorptionAmount() - amount));
		});
		Targeting.burst(p.getServerWorld(), ParticleTypes.POOF, p, 10, 0.5);
	}

	private static void clearFury(ServerPlayerEntity p) {
		EntityAttributeInstance speed = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		if (speed != null) speed.removeModifier(FURY_ID);
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
