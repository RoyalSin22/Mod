package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.RoleManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.DruidRules.*;

/**
 * v67 (F5): las 10 habilidades nuevas del Druida (N1–N10). Cifras en {@link DruidRules} (puro, JUnit); aquí el
 * comportamiento. Estado en memoria: al reiniciar el servidor se pierden los desgarros, la calma y las esperas.
 */
public final class DruidKit {
	private static final String N = "druid_n";
	private static final Identifier OAK_ID = Aptitudes.id("druid/oak_knockback");

	private static final class State {
		final Map<UUID, Long> clawed = new HashMap<>();
		long lethargyReadyAt;
		long lastHurt = -100_000;
		long lastProc = -100_000;
		long direUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private DruidKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", DruidKit::vineWhip);
		Abilities.register(N + "4", DruidKit::regrowth);
		Abilities.register(N + "6", DruidKit::bearRoar);
		Abilities.register(N + "8", DruidKit::direForm);
		Abilities.register(N + "10", DruidKit::forestAwakening);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Látigo de lianas: arrastra al enemigo que miras hasta ti. */
	private static boolean vineWhip(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, WHIP_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		Targeting.particleLine(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, p.getEyePos(),
				target.getPos().add(0, target.getHeight() / 2, 0));
		target.damage(KitSupport.magic(p), (float) whipDamage(rank, KitSupport.scale()));
		Vec3d pull = p.getPos().subtract(target.getPos()).multiply(1, 0, 1);
		if (pull.lengthSquared() > 0.01) {
			pull = pull.normalize().multiply(Math.min(1.6, 0.5 + pull.length() * 0.12));
			target.addVelocity(pull.x, 0.25, pull.z);
			target.velocityModified = true;
		}
		KitSupport.effect(target, StatusEffects.SLOWNESS, WHIP_SLOW_TICKS, 1);
		KitSupport.sound(p, SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, 0.8f);
		return true;
	}

	/** N4 Rebrote: recuperas una parte de tu vida máxima durante cinco segundos. */
	private static boolean regrowth(ServerPlayerEntity p, int rank) {
		float perPulse = (float) (regrowFraction(rank) * p.getMaxHealth() / REGROW_PULSES);
		AbilityState.repeat(p.getServer(), REGROW_PULSES, REGROW_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			p.heal(perPulse);
			Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, p, 8, 0.5);
		});
		KitSupport.sound(p, SoundEvents.BLOCK_GRASS_BREAK, 0.8f);
		return true;
	}

	/** N6 Rugido del oso: daño, empuje y Debilidad a los enemigos cercanos. */
	private static boolean bearRoar(ServerPlayerEntity p, int rank) {
		float damage = (float) roarDamage(rank, KitSupport.scale());
		for (LivingEntity e : Targeting.hostilesAround(p, ROAR_RADIUS)) {
			e.damage(KitSupport.magic(p), damage);
			e.takeKnockback(1.0, p.getX() - e.getX(), p.getZ() - e.getZ());
			KitSupport.effect(e, StatusEffects.WEAKNESS, ROAR_WEAKNESS_TICKS, 0);
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), ROAR_RADIUS, ParticleTypes.CLOUD, 36);
		KitSupport.sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.7f);
		return true;
	}

	/** N8 Forma de huargo: velocidad, daño y curación con cada golpe. */
	private static boolean direForm(ServerPlayerEntity p, int rank) {
		int ticks = direTicks(rank);
		state(p).direUntil = KitSupport.now(p) + ticks;
		KitSupport.effect(p, StatusEffects.SPEED, ticks, 1);
		Targeting.burst(p.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, p, 25, 0.7);
		KitSupport.sound(p, SoundEvents.ENTITY_WOLF_HOWL, 0.8f);
		return true;
	}

	/** N10 Despertar del bosque: raíces que inmovilizan y dañan a los enemigos y curan a los aliados. */
	private static boolean forestAwakening(ServerPlayerEntity p, int rank) {
		float damage = (float) awakenDamage(KitSupport.scale());
		AbilityState.repeat(p.getServer(), AWAKEN_PULSES, AWAKEN_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, AWAKEN_RADIUS)) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.SLOWNESS, AWAKEN_ROOT_TICKS, 6);
			}
			p.heal(AWAKEN_HEAL);
			for (var ally : Targeting.playersAround(p, AWAKEN_RADIUS)) {
				if (ally != p) ally.heal(AWAKEN_HEAL);
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.2, 0), AWAKEN_RADIUS, ParticleTypes.HAPPY_VILLAGER, 40);
		});
		KitSupport.sound(p, SoundEvents.BLOCK_MOSS_PLACE, 0.8f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Forma de huargo: +30 % de daño. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!direct) return 0f;
		State s = STATES.get(p.getUuid());
		return s != null && windowOpen(KitSupport.now(p), s.direUntil) ? (float) DIRE_DAMAGE : 0f;
	}

	/** Zarpas desgarradoras, curación de la Forma de huargo y Flujo estacional. */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, boolean direct, float damage) {
		if (!direct || damage <= 0) return;
		long now = KitSupport.now(p);
		int claws = RoleManager.activeRank(p, N + "2");
		if (claws > 0 && !isClawed(p, target, now)) {
			state(p).clawed.put(target.getUuid(), now + (long) CLAW_PULSES * CLAW_INTERVAL_TICKS);
			float dps = (float) clawDamage(claws, KitSupport.scale());
			AbilityState.repeat(p.getServer(), CLAW_PULSES, CLAW_INTERVAL_TICKS, i -> {
				if (!p.isAlive() || !target.isAlive()) return;
				target.damage(KitSupport.magic(p), dps);
				p.getServerWorld().spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.6), target.getZ(),
						2, 0.2, 0.2, 0.2, 0.05);
			});
		}
		State s = STATES.get(p.getUuid());
		if (s != null && windowOpen(now, s.direUntil)) p.heal(DIRE_HEAL);
		int season = RoleManager.activeRank(p, N + "9");
		if (season > 0) {
			s = state(p);
			if (seasonReady(now, s.lastHurt, s.lastProc)) {
				s.lastProc = now;
				p.heal(seasonHeal(season));
				Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, p, 10, 0.5);
			}
		}
	}

	/** Hierba alta, Roble firme y Letargo; apunta el último daño para Flujo estacional. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		if (amount <= 0) return amount;
		int grass = RoleManager.activeRank(p, N + "1");
		if (grass > 0 && onNature(p)) amount *= (float) (1.0 - grassReduction(grass));
		if (RoleManager.activeRank(p, N + "7") > 0) amount *= (float) (1.0 - OAK_REDUCTION);
		long now = KitSupport.now(p);
		State s = state(p);
		s.lastHurt = now;
		int lethargy = RoleManager.activeRank(p, N + "5");
		if (lethargy > 0 && lethargyTriggers(p.getHealth() + p.getAbsorptionAmount(), amount, p.getMaxHealth(), now >= s.lethargyReadyAt)) {
			s.lethargyReadyAt = now + LETHARGY_COOLDOWN_TICKS;
			float heal = (float) (lethargyHeal(lethargy) * p.getMaxHealth());
			AbilityState.schedule(p.getServer(), 1, () -> {
				if (p.isAlive()) {
					p.heal(heal);
					Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, p, 20, 0.6);
				}
			});
		}
		return amount;
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 40 == 0) {
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				boolean oak = RoleManager.activeRank(p, N + "7") > 0;
				var kb = p.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
				if (oak || (kb != null && kb.hasModifier(OAK_ID))) {
					AttributeApplier.set(p, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, OAK_ID, oak ? 1.0 : 0.0,
							EntityAttributeModifier.Operation.ADD_VALUE);
				}
			}
		}
		if (now % 200 == 0) {
			for (State s : STATES.values()) s.clawed.values().removeIf(until -> !windowOpen(now, until));
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static boolean isClawed(ServerPlayerEntity p, LivingEntity target, long now) {
		State s = STATES.get(p.getUuid());
		if (s == null) return false;
		Long until = s.clawed.get(target.getUuid());
		return until != null && windowOpen(now, until);
	}

	/** ¿Pisas hierba, tierra, musgo o similar? */
	private static boolean onNature(ServerPlayerEntity p) {
		BlockState below = p.getWorld().getBlockState(p.getBlockPos().down());
		return below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.DIRT) || below.isOf(Blocks.COARSE_DIRT)
				|| below.isOf(Blocks.PODZOL) || below.isOf(Blocks.MOSS_BLOCK) || below.isOf(Blocks.ROOTED_DIRT)
				|| below.isOf(Blocks.MYCELIUM);
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
