package dev.aptitudes.ability;

import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
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

import static dev.aptitudes.ability.AssassinRules.*;
import static dev.aptitudes.ability.BerserkerRules.windowOpen;

/**
 * v67 (F3): las 10 habilidades nuevas del Asesino (N1–N10). Cifras en {@link AssassinRules} (puro, JUnit); aquí solo
 * el comportamiento en el mundo. Estado en memoria por jugador: al reiniciar el servidor se pierden el velo, la noche
 * eterna, la ventana de Sombra persistente y la última activa recordada por Eslabón.
 */
public final class AssassinKit {
	private static final String N = "assassin_n";

	private static final class State {
		String lastActive;
		boolean wasInvisible;
		long persistentUntil;
		double persistentBonus;
		long veilUntil;
		long nightUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private AssassinKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", AssassinKit::smokeBomb);
		Abilities.register(N + "4", AssassinKit::lightningSlash);
		Abilities.register(N + "6", AssassinKit::shadowChain);
		Abilities.register(N + "8", AssassinKit::veil);
		Abilities.register(N + "10", AssassinKit::eternalNight);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Humo cegador: nube de 4 bloques durante 4 s; los enemigos dentro pierden el objetivo y quedan ciegos. */
	private static boolean smokeBomb(ServerPlayerEntity p, int rank) {
		Vec3d center = p.getPos();
		ServerWorld world = p.getServerWorld();
		KitSupport.effect(p, StatusEffects.SPEED, SMOKE_PULSES * 10, 0);
		AbilityState.repeat(p.getServer(), SMOKE_PULSES, 10, i -> {
			blind(p, world.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(SMOKE_RADIUS, 2, SMOKE_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e)), SMOKE_BLIND_TICKS);
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 1, center.z, 30, SMOKE_RADIUS * 0.5, 0.8,
					SMOKE_RADIUS * 0.5, 0.01);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.8f);
		return true;
	}

	/** N4 Corte relámpago: te lanzas a la espalda del enemigo más cercano y lo golpeas. */
	private static boolean lightningSlash(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, SLASH_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) {
			target = Targeting.hostilesAround(p, SLASH_RANGE).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
		}
		if (target == null || !teleportBehind(p, target)) return false;
		target.damage(p.getDamageSources().playerAttack(p), (float) slashDamage(rank, KitSupport.scale()));
		p.getServerWorld().spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(), 20, 0.3, 0.4, 0.3, 0.3);
		KitSupport.sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f);
		return true;
	}

	/** N6 Cadena de sombras: saltas entre hasta tres enemigos, uno por segundo. */
	private static boolean shadowChain(ServerPlayerEntity p, int rank) {
		List<LivingEntity> chain = new ArrayList<>();
		Vec3d from = p.getPos();
		for (int i = 0; i < CHAIN_JUMPS; i++) {
			Vec3d at = from;
			LivingEntity next = p.getServerWorld().getEntitiesByClass(LivingEntity.class,
							new Box(at, at).expand(CHAIN_RANGE), e -> !chain.contains(e) && Targeting.isHostileImpactTarget(p, e))
					.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(at))).orElse(null);
			if (next == null) break;
			chain.add(next);
			from = next.getPos();
		}
		if (chain.isEmpty()) return false;
		float damage = (float) chainDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), chain.size(), CHAIN_INTERVAL_TICKS, i -> {
			LivingEntity e = chain.get(i);
			if (!p.isAlive() || !e.isAlive() || !teleportBehind(p, e)) return;
			e.damage(p.getDamageSources().playerAttack(p), damage);
			p.getServerWorld().spawnParticles(ParticleTypes.SWEEP_ATTACK, e.getX(), e.getBodyY(0.6), e.getZ(), 2, 0.3, 0.3, 0.3, 0);
		});
		return true;
	}

	/** N8 Velo de muerte: invisible y con más daño; las criaturas que te perseguían te pierden. */
	private static boolean veil(ServerPlayerEntity p, int rank) {
		int ticks = veilTicks(rank);
		KitSupport.effect(p, StatusEffects.INVISIBILITY, ticks, 0);
		state(p).veilUntil = KitSupport.now(p) + ticks;
		for (MobEntity mob : p.getWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(16), m -> m.getTarget() == p)) {
			mob.setTarget(null);
		}
		Targeting.burst(p.getServerWorld(), ParticleTypes.LARGE_SMOKE, p, 25, 0.5);
		KitSupport.sound(p, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.8f);
		return true;
	}

	/** N10 Noche eterna: durante 6 s los enemigos cercanos quedan ciegos y no te ven; tu daño contra ellos sube. */
	private static boolean eternalNight(ServerPlayerEntity p, int rank) {
		state(p).nightUntil = KitSupport.now(p) + NIGHT_TICKS;
		ServerWorld world = p.getServerWorld();
		AbilityState.repeat(p.getServer(), NIGHT_TICKS / 20, 20, i -> {
			if (!p.isAlive()) return;
			blind(p, Targeting.hostilesAround(p, NIGHT_RADIUS), 60);
			ParticleFx.ring(world, p.getPos().add(0, 0.2, 0), NIGHT_RADIUS, ParticleTypes.SMOKE, 48);
		});
		Targeting.burst(world, ParticleTypes.SQUID_INK, p, 40, 1.2);
		KitSupport.sound(p, SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.0f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Punto débil, Sombra persistente, Velo de muerte y Noche eterna (bonificaciones); Veneno concentrado (efecto). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		int weak = RoleManager.activeRank(p, N + "1");
		float bonus = weak > 0 ? (float) weakPointBonus(weak, hasHarmfulEffect(target)) : 0f;
		State s = STATES.get(p.getUuid());
		long now = KitSupport.now(p);
		if (s != null) {
			if (direct && windowOpen(now, s.persistentUntil)) {
				s.persistentUntil = 0;
				bonus += (float) s.persistentBonus;
				p.getServerWorld().spawnParticles(ParticleTypes.SMOKE, target.getX(), target.getBodyY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
			}
			if (windowOpen(now, s.veilUntil)) bonus += (float) VEIL_DAMAGE;
			if (windowOpen(now, s.nightUntil) && target.squaredDistanceTo(p) <= NIGHT_RADIUS * NIGHT_RADIUS) {
				bonus += (float) NIGHT_DAMAGE;
				if (direct) {
					bonus += (float) nightBackstab(RoleManager.activeRank(p, "backstab"), Targeting.isBehind(p, target));
				}
			}
		}
		if (direct && RoleManager.activeRank(p, N + "7") > 0) {
			StatusEffectInstance poison = target.getStatusEffect(StatusEffects.POISON);
			if (poison != null) {
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, venomTicks(poison.getDuration()), VENOM_AMPLIFIER));
			}
		}
		return bonus;
	}

	/** Recuerda la última activa lanzada (Eslabón). */
	public static void onCast(ServerPlayerEntity p, String key) {
		if (key.startsWith("signature.") || key.startsWith("ultimate.")) return;
		if (RoleManager.activeRank(p, N + "2") > 0) state(p).lastActive = key;
	}

	/** Eslabón: matar recorta la espera de la última activa. Cosecha silenciosa: cura y alarga la invisibilidad. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		int link = RoleManager.activeRank(p, N + "2");
		State s = STATES.get(p.getUuid());
		if (link > 0 && s != null && s.lastActive != null) {
			long left = AbilityManager.cooldowns(p).remaining(s.lastActive, AbilityManager.gameTime(p));
			AbilityManager.reduceCooldown(p, s.lastActive, linkReduction(left, link));
		}
		int harvest = RoleManager.activeRank(p, N + "9");
		if (harvest > 0 && (p.isInvisible() || p.isSneaking())) {
			p.heal(HARVEST_HEAL);
			StatusEffectInstance invisibility = p.getStatusEffect(StatusEffects.INVISIBILITY);
			if (invisibility != null) {
				p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY,
						invisibility.getDuration() + harvestExtend(harvest), invisibility.getAmplifier(),
						invisibility.isAmbient(), invisibility.shouldShowParticles()));
			}
			Targeting.burst(p.getServerWorld(), ParticleTypes.SOUL, p, 6, 0.4);
		}
	}

	/** Sombra persistente: al terminar la invisibilidad se abre la ventana del golpe potenciado. */
	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			int persistent = RoleManager.activeRank(p, N + "5");
			State s = STATES.get(p.getUuid());
			if (persistent <= 0 && s == null) continue;
			if (persistent <= 0) {
				s.wasInvisible = false;
				continue;
			}
			s = state(p);
			boolean invisible = p.hasStatusEffect(StatusEffects.INVISIBILITY);
			if (s.wasInvisible && !invisible) {
				s.persistentUntil = now + PERSISTENT_WINDOW_TICKS;
				s.persistentBonus = persistentBonus(persistent);
			}
			s.wasInvisible = invisible;
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** Ciega a las criaturas y hace que dejen de atacar mientras dura el efecto. */
	private static void blind(ServerPlayerEntity p, List<LivingEntity> enemies, int ticks) {
		int until = AbilityManager.now(p) + 25;
		for (LivingEntity e : enemies) {
			KitSupport.effect(e, StatusEffects.BLINDNESS, ticks, 0);
			if (e instanceof MobEntity mob) AbilityState.pacify(mob, until);
		}
	}

	/** Te coloca a la espalda del objetivo, mirando en su misma dirección (como Paso sombrío). */
	private static boolean teleportBehind(ServerPlayerEntity p, LivingEntity target) {
		Vec3d behind = target.getPos().subtract(Targeting.horizontalFacing(target).multiply(1.2));
		Vec3d spot = Targeting.findFreeSpot(p, behind);
		if (spot == null) return false;
		Targeting.burst(p.getServerWorld(), ParticleTypes.LARGE_SMOKE, p, 12, 0.4);
		p.networkHandler.requestTeleport(spot.x, spot.y, spot.z, target.getYaw(), 10.0f);
		p.fallDistance = 0;
		KitSupport.sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.7f);
		return true;
	}

	private static boolean hasHarmfulEffect(LivingEntity e) {
		for (StatusEffectInstance effect : e.getStatusEffects()) {
			if (effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) return true;
		}
		return false;
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
