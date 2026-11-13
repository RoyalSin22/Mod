package dev.aptitudes.ability;

import dev.aptitudes.party.PartyManager;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.aptitudes.ability.PaladinRules.*;

/**
 * v67 (F4): las 10 habilidades nuevas del Paladín (N1–N10). Cifras en {@link PaladinRules} (puro, JUnit); aquí solo
 * el comportamiento en el mundo. Estado en memoria: al reiniciar el servidor se pierden la carga de Ira justa, la
 * oración y la lista de Paladines con Vínculo de escudo (se rehace cada segundo).
 */
public final class PaladinKit {
	private static final String N = "paladin_n";

	private static final class State {
		int stillTicks;
		Vec3d lastPos = Vec3d.ZERO;
		double wrath;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();
	/** Jugadores conectados con Vínculo de escudo, refrescados cada segundo. */
	private static final Set<UUID> BONDS = new HashSet<>();

	private PaladinKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", PaladinKit::lightRay);
		Abilities.register(N + "4", PaladinKit::shieldBash);
		Abilities.register(N + "6", PaladinKit::lightWall);
		Abilities.register(N + "8", PaladinKit::holyBanner);
		Abilities.register(N + "10", PaladinKit::lightPillar);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Rayo de luz: cura al aliado que miras o daña al enemigo que miras. */
	private static boolean lightRay(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, RAY_RANGE);
		if (target == null) return false;
		ServerWorld world = p.getServerWorld();
		Vec3d end = target.getPos().add(0, target.getHeight() / 2, 0);
		if (target instanceof ServerPlayerEntity ally && PartyManager.isAlly(p, ally)) {
			ally.heal(rayHeal(rank));
			Targeting.particleLine(world, ParticleFx.dust(0xFFF59D, 1.3f), p.getEyePos(), end);
			Targeting.burst(world, ParticleTypes.HAPPY_VILLAGER, ally, 14, 0.5);
		} else if (Targeting.isHostileImpactTarget(p, target)) {
			boolean undead = target.getType().isIn(EntityTypeTags.UNDEAD);
			target.damage(KitSupport.magic(p), (float) rayDamage(rank, undead, KitSupport.scale()));
			Targeting.particleLine(world, ParticleFx.dust(0xFFE082, 1.3f), p.getEyePos(), end);
			world.spawnParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0, 0, 0, 0);
		} else {
			return false;
		}
		KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.4f);
		return true;
	}

	/** N4 Golpe de escudo: daña, empuja y aturde al enemigo que tienes delante. */
	private static boolean shieldBash(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, BASH_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) {
			target = Targeting.hostilesAround(p, BASH_RANGE).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
		}
		if (target == null) return false;
		target.damage(p.getDamageSources().playerAttack(p), (float) bashDamage(rank, KitSupport.scale()));
		target.takeKnockback(1.0, p.getX() - target.getX(), p.getZ() - target.getZ());
		int stun = bashStunTicks(rank);
		KitSupport.effect(target, StatusEffects.SLOWNESS, stun, 6);
		KitSupport.effect(target, StatusEffects.WEAKNESS, stun, 2);
		p.getServerWorld().spawnParticles(ParticleTypes.FLASH, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0, 0, 0, 0);
		KitSupport.sound(p, SoundEvents.ITEM_SHIELD_BLOCK, 0.7f);
		return true;
	}

	/** N6 Muro de luz: un muro delante de ti que detiene proyectiles y frena y daña a los enemigos que lo cruzan. */
	private static boolean lightWall(ServerPlayerEntity p, int rank) {
		Vec3d facing = Targeting.horizontalFacing(p);
		Vec3d center = p.getPos().add(facing.multiply(3));
		Vec3d side = new Vec3d(-facing.z, 0, facing.x);
		ServerWorld world = p.getServerWorld();
		float damage = (float) wallDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), WALL_TICKS / 2, 2, i -> {
			if (!p.isAlive()) return;
			Box area = new Box(center, center).expand(WALL_WIDTH / 2 + 1, 3, WALL_WIDTH / 2 + 1);
			for (ProjectileEntity projectile : world.getEntitiesByClass(ProjectileEntity.class, area, pr -> true)) {
				if (!inWall(projectile.getPos(), center, facing, side)) continue;
				if (projectile.getOwner() != null && PartyManager.isAlly(p, projectile.getOwner())) continue;
				world.spawnParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 6, 0.1, 0.1, 0.1, 0.1);
				projectile.discard();
			}
			boolean pulse = i % 10 == 9;
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, area, e -> Targeting.isHostileImpactTarget(p, e))) {
				if (!inWall(e.getPos(), center, facing, side)) continue;
				KitSupport.effect(e, StatusEffects.SLOWNESS, 30, 1);
				if (pulse) e.damage(KitSupport.magic(p), damage);
			}
			if (i % 5 == 0) {
				for (double offset = -WALL_WIDTH / 2; offset <= WALL_WIDTH / 2; offset += 1.0) {
					Vec3d at = center.add(side.multiply(offset));
					world.spawnParticles(ParticleTypes.END_ROD, at.x, at.y + 1.2, at.z, 2, 0.05, 0.8, 0.05, 0);
				}
			}
		});
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.5f);
		return true;
	}

	/** N8 Estandarte sagrado: refuerza a los aliados y debilita a los enemigos alrededor. */
	private static boolean holyBanner(ServerPlayerEntity p, int rank) {
		Vec3d center = p.getPos();
		ServerWorld world = p.getServerWorld();
		AbilityState.repeat(p.getServer(), bannerTicks(rank) / 20, 20, i -> {
			for (ServerPlayerEntity ally : alliesAround(p, center, BANNER_RADIUS)) {
				KitSupport.effect(ally, StatusEffects.STRENGTH, 40, 0);
				KitSupport.effect(ally, StatusEffects.REGENERATION, 40, 0);
			}
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(BANNER_RADIUS, 3, BANNER_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				KitSupport.effect(e, StatusEffects.WEAKNESS, 40, 0);
			}
			ParticleFx.column(world, center, 4, ParticleTypes.END_ROD, 12);
			ParticleFx.ring(world, center.add(0, 0.1, 0), BANNER_RADIUS, ParticleTypes.END_ROD, 40);
		});
		KitSupport.sound(p, SoundEvents.EVENT_RAID_HORN.value(), 1.0f);
		return true;
	}

	/** N10 Columna de luz: durante seis segundos daña a los enemigos y cura a los aliados que están dentro. */
	private static boolean lightPillar(ServerPlayerEntity p, int rank) {
		Vec3d spot = Targeting.lookEnd(p, 24);
		ServerWorld world = p.getServerWorld();
		AbilityState.repeat(p.getServer(), PILLAR_PULSES, 20, i -> {
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(spot, spot).expand(PILLAR_RADIUS, 6, PILLAR_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				boolean undead = e.getType().isIn(EntityTypeTags.UNDEAD);
				e.damage(KitSupport.magic(p), (float) pillarDamage(undead, KitSupport.scale()));
			}
			for (ServerPlayerEntity ally : alliesAround(p, spot, PILLAR_RADIUS)) ally.heal(PILLAR_HEAL);
			ParticleFx.column(world, spot, 8, ParticleTypes.END_ROD, 24);
			ParticleFx.ring(world, spot.add(0, 0.1, 0), PILLAR_RADIUS, ParticleTypes.END_ROD, 32);
		});
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.0f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Ira justa: el golpe siguiente a haber recibido daño sale cargado (se consume). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		if (!direct) return 0f;
		State s = STATES.get(p.getUuid());
		if (s == null || s.wrath <= 0) return 0f;
		float bonus = (float) wrathBonus(s.wrath, RoleManager.activeRank(p, N + "9"));
		if (bonus > 0) {
			s.wrath = 0;
			p.getServerWorld().spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getBodyY(0.6), target.getZ(), 14, 0.3, 0.4, 0.3, 0.2);
		}
		return bonus;
	}

	/** Fe contra la oscuridad y Vínculo de escudo reducen el daño; Ira justa guarda lo que pierdes. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		if (amount <= 0) return amount;
		int faith = RoleManager.activeRank(p, N + "1");
		if (faith > 0 && source.getAttacker() != null && source.getAttacker().getType().isIn(EntityTypeTags.UNDEAD)) {
			amount *= (float) (1.0 - undeadReduction(faith));
		}
		if (!BONDS.isEmpty() && p.getServer() != null) {
			for (UUID id : BONDS) {
				ServerPlayerEntity paladin = p.getServer().getPlayerManager().getPlayer(id);
				if (paladin == null || paladin.getWorld() != p.getWorld()) continue;
				if (paladin.squaredDistanceTo(p) > BOND_RADIUS * BOND_RADIUS || !PartyManager.isAlly(paladin, p)) continue;
				amount *= (float) (1.0 - BOND_REDUCTION);
				break;
			}
		}
		if (RoleManager.activeRank(p, N + "9") > 0) {
			State s = state(p);
			s.wrath = wrathCharge(s.wrath, amount, p.getMaxHealth());
		}
		return amount;
	}

	/** Triunfo compartido: matar cura a los aliados cercanos. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		int rank = RoleManager.activeRank(p, N + "2");
		if (rank <= 0) return;
		float heal = triumphHeal(rank);
		for (ServerPlayerEntity ally : alliesAround(p, p.getPos(), TRIUMPH_RADIUS)) {
			ally.heal(heal);
			Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, ally, 6, 0.4);
		}
	}

	/** Oración (agachado y quieto) y lista de Paladines con Vínculo de escudo. */
	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		boolean refresh = now % 20 == 0;
		if (refresh) BONDS.clear();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (refresh && p.isAlive() && RoleManager.activeRank(p, N + "7") > 0) BONDS.add(p.getUuid());
			int prayer = RoleManager.activeRank(p, N + "5");
			if (prayer <= 0) continue;
			State s = state(p);
			boolean still = p.isSneaking() && p.getPos().squaredDistanceTo(s.lastPos) < 0.0004;
			s.stillTicks = still ? s.stillTicks + 1 : 0;
			s.lastPos = p.getPos();
			if (s.stillTicks >= PRAYER_STILL_TICKS && s.stillTicks % 20 == 0) {
				for (ServerPlayerEntity ally : alliesAround(p, p.getPos(), PRAYER_RADIUS)) {
					ally.heal(prayerHeal(prayer));
					Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, ally, 4, 0.3);
				}
				ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.1, 0), PRAYER_RADIUS, ParticleTypes.END_ROD, 24);
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
		BONDS.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** ¿Está el punto dentro del muro? (de ancho {@code WALL_WIDTH}, fino y de 3 bloques de alto). */
	private static boolean inWall(Vec3d point, Vec3d center, Vec3d forward, Vec3d side) {
		Vec3d rel = point.subtract(center);
		return Math.abs(rel.dotProduct(side)) <= WALL_WIDTH / 2 && Math.abs(rel.dotProduct(forward)) <= WALL_THICKNESS
				&& Math.abs(rel.y) <= 3;
	}

	private static List<ServerPlayerEntity> alliesAround(ServerPlayerEntity p, Vec3d center, double radius) {
		List<ServerPlayerEntity> out = new ArrayList<>();
		for (var ally : Targeting.playersInSupportArea(p, new Box(center, center).expand(radius, 3, radius))) {
			if (ally instanceof ServerPlayerEntity sp) out.add(sp);
		}
		return out;
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
