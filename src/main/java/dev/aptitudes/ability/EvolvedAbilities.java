package dev.aptitudes.ability;

import dev.aptitudes.role.EvolvedTree;
import dev.aptitudes.role.EvolvedTree.Active;
import dev.aptitudes.role.EvolvedTree.Extra;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Activas de las filas de Maestro y Definitivo (v23): 10 plantillas parametrizadas con los datos de
 * {@link EvolvedTree}. Cada plantilla tiene su efecto visual y su sonido con el color del rol; todo el daño
 * va solo a enemigos ({@link Targeting#isHostileTo}) y las mejoras, al jugador y a sus aliados.
 *
 * <p>Escalado: daño = base (6 Maestro / 10 Definitivo) + 1,5 × rango efectivo (incluye Maestría y la
 * bonificación de evolución, con el tope de 6 de {@code AbilityManager.capRank}).</p>
 */
public final class EvolvedAbilities {
	private EvolvedAbilities() {
	}

	/** Registra las 80 activas (llamado desde {@link Abilities}). */
	static void registerAll() {
		for (Active spec : EvolvedTree.actives().values()) {
			Abilities.register(spec.id(), (p, rank) -> cast(p, rank, spec));
		}
	}

	private static float damage(Active spec, int rank) {
		float base = spec.tier() >= dev.aptitudes.role.Evolution.ULTIMATE ? 10f : 6f;
		return base + 1.5f * rank;
	}

	private static boolean cast(ServerPlayerEntity p, int rank, Active spec) {
		return switch (spec.template()) {
			case NOVA -> nova(p, rank, spec);
			case BEAM -> beam(p, rank, spec);
			case CONE -> cone(p, rank, spec);
			case DASH -> dash(p, rank, spec);
			case ZONE -> zone(p, rank, spec);
			case VORTEX -> vortex(p, rank, spec);
			case AURA -> aura(p, rank, spec);
			case HEAL -> heal(p, rank, spec);
			case SHIELD -> shield(p, rank, spec);
			case SUMMON -> summon(p, rank, spec);
		};
	}

	// ------------------------------------------------------------ Plantillas

	/** Explosión alrededor: anillos que se expanden y daño a todos los enemigos del radio. */
	private static boolean nova(ServerPlayerEntity p, int rank, Active spec) {
		ServerWorld world = p.getServerWorld();
		double radius = 4.5 + 0.5 * rank;
		ParticleEffect dust = ParticleFx.dust(spec.color(), 1.6f);
		ParticleFx.runeCircle(world, p.getPos().add(0, 0.1, 0), radius * 0.6, dust);
		ParticleFx.shockwave(p.getServer(), world, p.getPos().add(0, 0.3, 0), radius, dust, 6);
		world.spawnParticles(ParticleTypes.EXPLOSION, p.getX(), p.getBodyY(0.5), p.getZ(), 2, 0.6, 0.3, 0.6, 0);
		sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.7f, 1.3f);
		for (LivingEntity e : Targeting.hostilesAround(p, radius)) hit(p, e, spec, damage(spec, rank), magic(p));
		CastFx.screen(p, dev.aptitudes.network.ScreenFxPayload.SHAKE, spec.color(), 0.4f); // temblor breve
		return true;
	}

	/** Rayo recto de 24 bloques: atraviesa y daña a todos los enemigos de la línea. */
	private static boolean beam(ServerPlayerEntity p, int rank, Active spec) {
		ServerWorld world = p.getServerWorld();
		Vec3d from = p.getEyePos();
		Vec3d dir = p.getRotationVector();
		double range = 24;
		Set<UUID> hit = new HashSet<>();
		for (double d = 0.5; d <= range; d += 0.5) {
			Vec3d point = from.add(dir.multiply(d));
			if (!world.getBlockState(net.minecraft.util.math.BlockPos.ofFloored(point)).getCollisionShape(world,
					net.minecraft.util.math.BlockPos.ofFloored(point)).isEmpty()) {
				range = d;
				break;
			}
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(point, point).expand(0.6),
					e -> Targeting.isHostileTo(p, e))) {
				if (hit.add(e.getUuid())) hit(p, e, spec, damage(spec, rank) * 1.15f, magic(p));
			}
		}
		Vec3d end = from.add(dir.multiply(range));
		ParticleFx.line(world, ParticleFx.dust(spec.color(), 1.4f), from.add(dir.multiply(0.8)), end, (int) (range * 4));
		ParticleFx.line(world, ParticleTypes.END_ROD, from.add(dir.multiply(0.8)), end, (int) (range * 1.5));
		world.spawnParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0, 0, 0, 0);
		sound(p, SoundEvents.ENTITY_GUARDIAN_ATTACK, 0.8f, 1.6f);
		return true;
	}

	/** Barrido en cono frontal (70°, 6 bloques): daño y empuje. */
	private static boolean cone(ServerPlayerEntity p, int rank, Active spec) {
		ServerWorld world = p.getServerWorld();
		Vec3d look = Targeting.horizontalFacing(p);
		double range = 6;
		for (int i = -4; i <= 4; i++) {
			Vec3d dir = look.rotateY((float) Math.toRadians(i * 9));
			for (double d = 1; d <= range; d += 1) {
				Vec3d at = p.getPos().add(0, 1.0, 0).add(dir.multiply(d));
				world.spawnParticles(ParticleFx.dust(spec.color(), 1.3f), at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0);
			}
		}
		world.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + look.x * 2, p.getBodyY(0.6), p.getZ() + look.z * 2,
				3, 0.8, 0.1, 0.8, 0);
		sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
		double cos = Math.cos(Math.toRadians(35));
		for (LivingEntity e : Targeting.hostilesAround(p, range)) {
			Vec3d to = e.getPos().subtract(p.getPos());
			Vec3d flat = new Vec3d(to.x, 0, to.z);
			if (flat.lengthSquared() < 1.0e-4 || flat.normalize().dotProduct(look) >= cos) {
				hit(p, e, spec, damage(spec, rank), p.getDamageSources().playerAttack(p));
				e.takeKnockback(0.6, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
		}
		return true;
	}

	/** Embestida: impulso hacia delante y daño a lo que atraviesa durante medio segundo. */
	private static boolean dash(ServerPlayerEntity p, int rank, Active spec) {
		Vec3d look = Targeting.horizontalFacing(p);
		p.setVelocity(look.x * 1.7, 0.25, look.z * 1.7);
		p.velocityModified = true;
		p.fallDistance = 0;
		sound(p, SoundEvents.ENTITY_BREEZE_JUMP, 1.0f, 1.1f);
		Set<UUID> done = new HashSet<>();
		AbilityState.repeat(p.getServer(), 10, 1, i -> {
			if (!p.isAlive()) return;
			ServerWorld world = p.getServerWorld();
			world.spawnParticles(ParticleFx.dust(spec.color(), 1.5f), p.getX(), p.getBodyY(0.5), p.getZ(), 6, 0.3, 0.4, 0.3, 0);
			world.spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.1, p.getZ(), 2, 0.2, 0.05, 0.2, 0.01);
			p.fallDistance = 0;
			for (LivingEntity e : Targeting.hostilesAround(p, 2.2)) {
				if (done.add(e.getUuid())) hit(p, e, spec, damage(spec, rank), p.getDamageSources().playerAttack(p));
			}
		});
		return true;
	}

	/** Lluvia de 8 impactos en 4 s sobre el punto señalado (radio 4). */
	private static boolean zone(ServerPlayerEntity p, int rank, Active spec) {
		Vec3d center = Targeting.lookEnd(p, 30);
		ServerWorld world = p.getServerWorld();
		ParticleFx.runeCircle(world, center.add(0, 0.1, 0), 4.0, ParticleFx.dust(spec.color(), 1.6f));
		sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 0.6f, 1.4f);
		AbilityState.repeat(p.getServer(), 8, 5, i -> {
			if (!p.isAlive()) return;
			double x = center.x + (world.random.nextDouble() - 0.5) * 7;
			double z = center.z + (world.random.nextDouble() - 0.5) * 7;
			Vec3d at = new Vec3d(x, center.y, z);
			ParticleFx.column(world, at, 6, ParticleFx.dust(spec.color(), 1.4f), 18);
			world.spawnParticles(spec.extra() == Extra.FIRE ? ParticleTypes.LAVA : ParticleTypes.CRIT, x, center.y + 0.3, z,
					8, 0.4, 0.2, 0.4, 0.05);
			world.playSound(null, x, center.y, z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.4f, 1.6f);
			for (LivingEntity e : near(p, at, 2.4)) hit(p, e, spec, damage(spec, rank) * 0.45f, magic(p));
		});
		return true;
	}

	/** Remolino: atrae a los enemigos al punto señalado durante 2 s y explota al final. */
	private static boolean vortex(ServerPlayerEntity p, int rank, Active spec) {
		Vec3d center = Targeting.lookEnd(p, 20);
		ServerWorld world = p.getServerWorld();
		sound(p, SoundEvents.BLOCK_PORTAL_TRIGGER, 0.5f, 1.8f);
		AbilityState.repeat(p.getServer(), 10, 4, i -> {
			if (!p.isAlive()) return;
			ParticleFx.spiral(world, center, 3.5 - i * 0.3, 2.5, ParticleFx.dust(spec.color(), 1.3f), 24, 2);
			world.spawnParticles(ParticleTypes.PORTAL, center.x, center.y + 1, center.z, 20, 1.5, 1, 1.5, 0.4);
			for (LivingEntity e : near(p, center, 7)) {
				Vec3d pull = center.subtract(e.getPos()).normalize().multiply(0.35);
				e.addVelocity(pull.x, 0.04, pull.z);
				e.velocityModified = true;
				if (i == 9) hit(p, e, spec, damage(spec, rank), magic(p));
			}
			if (i == 9) {
				world.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 3, 1, 0.5, 1, 0);
				world.playSound(null, center.x, center.y, center.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
						SoundCategory.PLAYERS, 0.8f, 1.0f);
			}
		});
		return true;
	}

	/** Mejora al jugador, a los jugadores cercanos (10 bloques) y a sus esbirros. */
	private static boolean aura(ServerPlayerEntity p, int rank, Active spec) {
		ServerWorld world = p.getServerWorld();
		int ticks = (12 + 2 * rank) * 20;
		List<LivingEntity> allies = allies(p, 10);
		for (LivingEntity a : allies) {
			switch (spec.extra()) {
				case SHADOW -> {
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, ticks / 2, 0));
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, ticks / 2, 1));
				}
				case SONG -> {
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, ticks, 0));
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, ticks, 0));
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, ticks / 2, 0));
				}
				default -> { // FORCE
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, ticks, 0));
					a.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, ticks, 0));
				}
			}
			ParticleFx.spiral(world, a.getPos(), 0.8, 2.2, ParticleFx.dust(spec.color(), 1.2f), 20, 2);
		}
		ParticleFx.ring(world, p.getPos().add(0, 0.2, 0), 10, ParticleFx.dust(spec.color(), 1.5f), 64);
		sound(p, spec.extra() == Extra.SONG ? SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value() : SoundEvents.ENTITY_RAVAGER_ROAR,
				0.9f, 1.0f);
		return true;
	}

	/** Cura al jugador y a los jugadores cercanos (8 bloques) y les quita los efectos negativos. */
	private static boolean heal(ServerPlayerEntity p, int rank, Active spec) {
		ServerWorld world = p.getServerWorld();
		float amount = (spec.tier() >= dev.aptitudes.role.Evolution.ULTIMATE ? 8f : 4f) + 1.5f * rank;
		for (LivingEntity a : allies(p, 8)) {
			a.heal(amount);
			List<net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect>> bad = new ArrayList<>();
			for (StatusEffectInstance inst : a.getStatusEffects()) {
				if (inst.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) bad.add(inst.getEffectType());
			}
			bad.forEach(a::removeStatusEffect);
			world.spawnParticles(ParticleTypes.HEART, a.getX(), a.getBodyY(1.0), a.getZ(), 4, 0.4, 0.3, 0.4, 0);
			ParticleFx.spiral(world, a.getPos(), 0.7, 2.0, ParticleTypes.HAPPY_VILLAGER, 16, 1.5);
		}
		ParticleFx.ring(world, p.getPos().add(0, 0.2, 0), 8, ParticleFx.dust(spec.color(), 1.4f), 48);
		sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
		return true;
	}

	/** Escudo: absorción y Resistencia I para el jugador y sus aliados (6 bloques). */
	private static boolean shield(ServerPlayerEntity p, int rank, Active spec) {
		ServerWorld world = p.getServerWorld();
		int absorption = Math.min(4, 1 + rank / 2);
		for (LivingEntity a : allies(p, 6)) {
			a.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 240, absorption));
			a.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 0));
			ParticleFx.ring(world, a.getPos().add(0, 1.0, 0), 1.0, ParticleFx.dust(spec.color(), 1.2f), 16);
			ParticleFx.ring(world, a.getPos().add(0, 0.3, 0), 1.0, ParticleTypes.ENCHANTED_HIT, 12);
		}
		sound(p, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(), 1.0f, 1.0f);
		return true;
	}

	/** Invoca aliados temporales con las reglas de los esbirros (máx. 4, no atacan a jugadores). */
	private static boolean summon(ServerPlayerEntity p, int rank, Active spec) {
		EntityType<? extends MobEntity> type = switch (spec.extra()) {
			case ZOMBIES -> EntityType.ZOMBIE;
			case SKELETONS -> EntityType.SKELETON;
			case VEXES -> EntityType.VEX;
			default -> EntityType.WOLF;
		};
		int count = Math.min(3, 1 + rank / 2);
		ServerWorld world = p.getServerWorld();
		int spawned = 0;
		for (int i = 0; i < count; i++) {
			MobEntity m = Minions.spawn(p, type, i);
			if (m == null) continue;
			spawned++;
			ParticleFx.column(world, m.getPos(), 2.2, ParticleFx.dust(spec.color(), 1.4f), 16);
			world.spawnParticles(ParticleTypes.SOUL, m.getX(), m.getY() + 0.5, m.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
		}
		sound(p, SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
		return spawned > 0;
	}

	// ------------------------------------------------------------ Utilidades

	private static void hit(ServerPlayerEntity p, LivingEntity e, Active spec, float amount, DamageSource source) {
		if (e.damage(source, amount)) CastFx.impact(p, e, source, amount);
		switch (spec.extra()) {
			case SLOW -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));
			case FIRE -> e.setOnFireFor(4);
			case WITHER -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0));
			case POISON -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
			case LAUNCH -> {
				e.addVelocity(0, 0.9, 0);
				e.velocityModified = true;
			}
			case KNOCKBACK -> e.takeKnockback(1.2, p.getX() - e.getX(), p.getZ() - e.getZ());
			default -> {
			}
		}
	}

	private static DamageSource magic(ServerPlayerEntity p) {
		return p.getDamageSources().indirectMagic(p, p);
	}

	private static List<LivingEntity> near(ServerPlayerEntity p, Vec3d center, double radius) {
		double r2 = radius * radius;
		return p.getWorld().getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(radius),
				e -> Targeting.isHostileTo(p, e) && e.squaredDistanceTo(center) <= r2);
	}

	/** El jugador, los jugadores cercanos y sus esbirros. */
	private static List<LivingEntity> allies(ServerPlayerEntity p, double radius) {
		List<LivingEntity> out = new ArrayList<>();
		out.add(p);
		for (PlayerEntity other : Targeting.playersAround(p, radius)) if (other != p) out.add(other);
		out.addAll(AbilityState.minionsOf(p));
		return out;
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float volume, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, volume, pitch);
	}
}
