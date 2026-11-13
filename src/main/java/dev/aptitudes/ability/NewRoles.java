package dev.aptitudes.ability;

import dev.aptitudes.network.MechanicPayload;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Los 6 roles añadidos en la v11: Mago pistolero, Invocador de armas, Cazador,
 * Bardo, Brujo y Domador. Aquí están sus 24 activas, sus características y
 * definitivas, y sus pasivas, mecánicas y reactivas (enganchadas desde
 * {@link RoleMechanics}).
 */
public final class NewRoles {
	private static final class State {
		int cylinder, blades, lastBladeGain, prey, preyTarget = -1, notes, corruption, corruptionUntil;
		int stillSeconds, avatarUntil;
		Vec3d lastPos = Vec3d.ZERO;
		final Map<String, Integer> cooldowns = new HashMap<>();
	}

	/** Marcas del Cazador: objetivo → [dueño, tick de fin, bonificación ×100]. */
	private static final Map<UUID, Object[]> MARKS = new HashMap<>();
	private static final Map<UUID, State> STATES = new HashMap<>();
	private static boolean registered;

	private NewRoles() {
	}

	private static State state(ServerPlayerEntity p) {
		return STATES.computeIfAbsent(p.getUuid(), k -> new State());
	}

	private static int rank(ServerPlayerEntity p, String node) {
		return Math.min(5, RoleManager.activeRank(p, node));
	}

	private static int now(ServerPlayerEntity p) {
		return p.getServer().getTicks();
	}

	private static boolean ready(ServerPlayerEntity p, State s, String key, int ticks) {
		Integer until = s.cooldowns.get(key);
		if (until != null && until > now(p)) return false;
		s.cooldowns.put(key, now(p) + ticks);
		return true;
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// =================================================================== Activas

	static void registerActives() {
		// ------------------------------------------------ Mago pistolero
		Abilities.register("arcane_shot", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 32);
			Vec3d end = t != null ? t.getEyePos() : Targeting.lookEnd(p, 32);
			Targeting.particleLine(world(p), ParticleFx.dust(0x26C6DA, 1.1f), muzzle(p), end);
			sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.6f);
			if (t == null) return false;
			t.damage(magic(p), 6 + 2 * r);
			t.takeKnockback(0.6, p.getX() - t.getX(), p.getZ() - t.getZ());
			return true;
		});
		Abilities.register("fan_the_hammer", (p, r) -> {
			List<LivingEntity> targets = nearest(p, 16, 6);
			if (targets.isEmpty()) return false;
			AbilityState.repeat(server(p), 6, 4, i -> {
				LivingEntity t = targets.get(i % targets.size());
				if (!t.isAlive()) return;
				Targeting.particleLine(world(p), ParticleFx.dust(0x26C6DA, 0.9f), muzzle(p), t.getEyePos());
				t.timeUntilRegen = 0;
				t.damage(magic(p), 2 + r);
				sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.8f);
			});
			return true;
		});
		Abilities.register("ricochet", (p, r) -> {
			LivingEntity first = Targeting.raycastLiving(p, 24);
			if (first == null) return false;
			List<LivingEntity> chain = new ArrayList<>();
			chain.add(first);
			LivingEntity current = first;
			for (int i = 0; i < 2 + r; i++) {
				LivingEntity from = current;
				LivingEntity next = p.getWorld().getEntitiesByClass(LivingEntity.class, from.getBoundingBox().expand(8),
								e -> Targeting.isHostileTo(p, e) && !chain.contains(e)).stream()
						.min(Comparator.comparingDouble(from::squaredDistanceTo)).orElse(null);
				if (next == null) break;
				chain.add(next);
				current = next;
			}
			Vec3d prev = muzzle(p);
			for (LivingEntity e : chain) {
				Targeting.particleLine(world(p), ParticleTypes.ELECTRIC_SPARK, prev, e.getEyePos());
				prev = e.getEyePos();
				e.damage(magic(p), 5 + r);
			}
			sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 1.8f);
			return true;
		});
		Abilities.register("deadeye", (p, r) -> {
			List<LivingEntity> targets = nearest(p, 24, 4 + r);
			if (targets.isEmpty()) return false;
			for (LivingEntity t : targets) effect(t, StatusEffects.GLOWING, 40, 0);
			sound(p, SoundEvents.BLOCK_BELL_USE, 1.8f);
			AbilityState.schedule(server(p), 30, () -> {
				for (LivingEntity t : targets) {
					if (!t.isAlive()) continue;
					Targeting.particleLine(world(p), ParticleFx.dust(0xFFE082, 1.2f), muzzle(p), t.getEyePos());
					t.damage(magic(p), 8 + 3 * r);
				}
				sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.6f);
			});
			return true;
		});

		// ------------------------------------------------ Invocador de armas
		Abilities.register("blade_volley", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 20);
			if (t == null) return false;
			State s = state(p);
			int count = 3 + s.blades;
			s.blades = 0;
			AbilityState.repeat(server(p), count, 3, i -> {
				if (!t.isAlive()) return;
				Targeting.particleLine(world(p), ParticleTypes.ENCHANTED_HIT, p.getEyePos().add(orbit(i, 1.2)), t.getEyePos());
				t.timeUntilRegen = 0;
				t.damage(magic(p), bladeDamage(p, 4 + r));
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f + 0.05f * i);
			});
			return true;
		});
		Abilities.register("sword_rain", (p, r) -> {
			Vec3d center = Targeting.lookEnd(p, 20);
			AbilityState.repeat(server(p), 8, 4, i -> {
				Vec3d hit = center.add((p.getRandom().nextDouble() - 0.5) * 5, 0, (p.getRandom().nextDouble() - 0.5) * 5);
				Targeting.particleLine(world(p), ParticleTypes.ENCHANTED_HIT, hit.add(0, 6, 0), hit);
				for (LivingEntity e : around(p, hit, 2.0)) e.damage(magic(p), bladeDamage(p, 3 + r));
				world(p).playSound(null, hit.x, hit.y, hit.z, SoundEvents.ENTITY_ARROW_HIT, SoundCategory.PLAYERS, 0.8f, 1.2f);
			});
			return true;
		});
		Abilities.register("spectral_greatsword", (p, r) -> {
			int hits = lineDamage(p, 7, 8 + 2 * r, ParticleTypes.SWEEP_ATTACK, true);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.6f);
			return hits > 0;
		});
		Abilities.register("blade_prison", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 16);
			if (t == null) return false;
			effect(t, StatusEffects.SLOWNESS, 60, 6);
			AbilityState.repeat(server(p), 6, 5, i -> {
				if (!t.isAlive()) return;
				ParticleFx.ring(world(p), t.getPos().add(0, 0.8, 0), 1.2, ParticleTypes.ENCHANTED_HIT, 10);
				t.timeUntilRegen = 0;
				t.damage(magic(p), bladeDamage(p, 2 + r));
			});
			sound(p, SoundEvents.BLOCK_CHAIN_PLACE, 0.8f);
			return true;
		});

		// ------------------------------------------------ Cazador
		Abilities.register("hunters_mark", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 32);
			if (t == null) return false;
			MARKS.put(t.getUuid(), new Object[]{p.getUuid(), now(p) + 200, 5 + 10 * r});
			effect(t, StatusEffects.GLOWING, 200, 0);
			Targeting.particleLine(world(p), ParticleFx.dust(0x9CCC65, 1.0f), p.getEyePos(), t.getEyePos());
			sound(p, SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.8f);
			return true;
		});
		Abilities.register("bear_trap", (p, r) -> {
			Vec3d spot = Targeting.lookEnd(p, 10);
			boolean[] sprung = {false};
			AbilityState.repeat(server(p), (int) (60 * HunterKit.trapFactor(p)), 10, i -> { // v67 F6: Trampero experto
				if (sprung[0]) return;
				if (i % 2 == 0) world(p).spawnParticles(ParticleFx.dust(0x8D6E63, 1.0f), spot.x, spot.y + 0.1, spot.z, 4, 0.3, 0, 0.3, 0);
				for (LivingEntity e : around(p, spot, 1.4)) {
					sprung[0] = true;
					effect(e, StatusEffects.SLOWNESS, 60, 6);
					e.damage(p.getDamageSources().playerAttack(p), 4 + 2 * r);
					world(p).playSound(null, spot.x, spot.y, spot.z, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE,
							SoundCategory.PLAYERS, 1.0f, 0.6f);
					break;
				}
			});
			sound(p, SoundEvents.BLOCK_TRIPWIRE_ATTACH, 1.0f);
			return true;
		});
		Abilities.register("arrow_volley", (p, r) -> {
			Vec3d center = Targeting.lookEnd(p, 24);
			AbilityState.repeat(server(p), 10, 2, i -> {
				Vec3d hit = center.add((p.getRandom().nextDouble() - 0.5) * 6, 0, (p.getRandom().nextDouble() - 0.5) * 6);
				Targeting.particleLine(world(p), ParticleTypes.CRIT, hit.add(1, 8, 0), hit);
				for (LivingEntity e : around(p, hit, 1.6)) {
					e.timeUntilRegen = 0;
					e.damage(p.getDamageSources().playerAttack(p), 2 + r);
				}
			});
			sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 0.7f);
			return true;
		});
		Abilities.register("falcon_strike", (p, r) -> {
			LivingEntity t = markedBy(p);
			if (t == null) t = Targeting.raycastLiving(p, 24);
			if (t == null) return false;
			LivingEntity target = t;
			AbilityState.repeat(server(p), 4, 8, i -> {
				if (!target.isAlive()) return;
				world(p).spawnParticles(ParticleTypes.CLOUD, target.getX(), target.getBodyY(1.0), target.getZ(), 8, 0.3, 0.2, 0.3, 0.05);
				target.timeUntilRegen = 0;
				target.damage(p.getDamageSources().playerAttack(p), 3 + r);
				world(p).playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_PARROT_FLY,
						SoundCategory.PLAYERS, 1.0f, 0.6f);
			});
			return true;
		});

		// ------------------------------------------------ Bardo
		Abilities.register("song_of_valor", (p, r) -> {
			for (PlayerEntity ally : allies(p, 10)) {
				effect(ally, StatusEffects.STRENGTH, 200, r >= 3 ? 1 : 0);
				ally.heal(2.0f);
			}
			notes(p);
			sound(p, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.2f);
			return true;
		});
		Abilities.register("dissonance", (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 8);
			for (LivingEntity e : targets) {
				e.damage(magic(p), 3 + r);
				effect(e, StatusEffects.NAUSEA, 100, 0);
				effect(e, StatusEffects.WEAKNESS, 100, 0);
				e.takeKnockback(0.6, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			ParticleFx.shockwave(server(p), world(p), p.getPos(), 8, ParticleTypes.NOTE, 5);
			sound(p, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.6f);
			return !targets.isEmpty();
		});
		Abilities.register("ballad_of_shielding", (p, r) -> {
			for (PlayerEntity ally : allies(p, 10)) {
				effect(ally, StatusEffects.ABSORPTION, 200, 1);
				effect(ally, StatusEffects.RESISTANCE, 100, 0);
			}
			notes(p);
			sound(p, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.9f);
			return true;
		});
		Abilities.register("requiem", (p, r) -> {
			for (LivingEntity e : Targeting.hostilesAround(p, 10)) {
				e.damage(magic(p), 4 + 2 * r);
				effect(e, StatusEffects.WITHER, 60, 0);
			}
			for (PlayerEntity ally : allies(p, 10)) effect(ally, StatusEffects.SPEED, 160, 1);
			ParticleFx.spiral(world(p), p.getPos(), 2.5, 3, ParticleTypes.NOTE, 50, 3);
			sound(p, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.5f);
			return true;
		});

		// ------------------------------------------------ Brujo
		Abilities.register("curse_of_agony", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 24);
			if (t == null) return false;
			effect(t, StatusEffects.WEAKNESS, 160, 0);
			Targeting.particleLine(world(p), ParticleTypes.CRIMSON_SPORE, p.getEyePos(), t.getEyePos());
			AbilityState.repeat(server(p), 8, 20, i -> {
				if (!t.isAlive()) return;
				t.timeUntilRegen = 0;
				t.damage(magic(p), 1 + 0.5f * r);
				world(p).spawnParticles(ParticleTypes.CRIMSON_SPORE, t.getX(), t.getBodyY(0.7), t.getZ(), 8, 0.3, 0.4, 0.3, 0);
			});
			sound(p, SoundEvents.ENTITY_VEX_CHARGE, 0.6f);
			return true;
		});
		Abilities.register("abyssal_bolt", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 24);
			Vec3d end = t != null ? t.getEyePos() : Targeting.lookEnd(p, 24);
			Targeting.particleLine(world(p), ParticleFx.dust(0x6A1B9A, 1.3f), p.getEyePos(), end);
			sound(p, SoundEvents.ENTITY_WITHER_SHOOT, 1.2f);
			if (t == null) return false;
			t.damage(magic(p), 7 + 2 * r);
			effect(t, StatusEffects.WITHER, 60, 0);
			return true;
		});
		Abilities.register("summon_imps", (p, r) -> {
			int count = r >= 3 ? 3 : 2;
			for (int i = 0; i < count; i++) Minions.spawn(p, EntityType.VEX, i);
			world(p).spawnParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getBodyY(0.5), p.getZ(), 40, 1, 1, 1, 0.02);
			sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f);
			return true;
		});
		Abilities.register("doom", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 24);
			if (t == null) return false;
			effect(t, StatusEffects.GLOWING, 100, 0);
			AbilityState.repeat(server(p), 5, 20, i -> {
				if (t.isAlive()) world(p).spawnParticles(ParticleTypes.SCULK_SOUL, t.getX(), t.getBodyY(1.1), t.getZ(), 5, 0.2, 0.2, 0.2, 0.02);
			});
			AbilityState.schedule(server(p), 100, () -> {
				if (!t.isAlive()) return;
				t.damage(magic(p), 12 + 4 * r);
				world(p).spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, t.getX(), t.getBodyY(0.5), t.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
				world(p).playSound(null, t.getX(), t.getY(), t.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.6f, 1.4f);
			});
			sound(p, SoundEvents.AMBIENT_CAVE.value(), 1.4f);
			return true;
		});

		// ------------------------------------------------ Domador
		Abilities.register("sic_em", (p, r) -> {
			LivingEntity t = Targeting.raycastLiving(p, 32);
			List<TameableEntity> pets = pets(p, 24);
			if (t == null || pets.isEmpty()) return false;
			for (TameableEntity pet : pets) {
				if (pet.isSitting()) continue;
				pet.setTarget(t);
				effect(pet, StatusEffects.SPEED, 200, 1);
				effect(pet, StatusEffects.STRENGTH, 200, r - 1);
			}
			effect(t, StatusEffects.GLOWING, 100, 0);
			sound(p, SoundEvents.ENTITY_WOLF_GROWL, 1.2f);
			return true;
		});
		Abilities.register("vital_bond", (p, r) -> {
			List<TameableEntity> pets = pets(p, 24);
			if (pets.isEmpty()) return false;
			for (TameableEntity pet : pets) {
				pet.setHealth(pet.getMaxHealth());
				effect(pet, StatusEffects.ABSORPTION, 600, 0);
				world(p).spawnParticles(ParticleTypes.HEART, pet.getX(), pet.getBodyY(0.9), pet.getZ(), 4, 0.3, 0.3, 0.3, 0);
			}
			p.heal(Math.min(8, 2 * pets.size()));
			sound(p, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.6f);
			return true;
		});
		Abilities.register("stampede", (p, r) -> {
			int hits = lineDamage(p, 8, 5 + 2 * r, ParticleTypes.CLOUD, true);
			sound(p, SoundEvents.ENTITY_GOAT_RAM_IMPACT, 0.8f);
			return hits > 0;
		});
		Abilities.register("alpha_roar", (p, r) -> {
			List<TameableEntity> pets = pets(p, 24);
			for (TameableEntity pet : pets) {
				effect(pet, StatusEffects.STRENGTH, 200, 1);
				effect(pet, StatusEffects.RESISTANCE, 200, 1);
				for (LivingEntity e : around(p, pet.getPos(), 6)) effect(e, StatusEffects.WEAKNESS, 160, 0);
			}
			effect(p, StatusEffects.STRENGTH, 200, 0);
			ParticleFx.shockwave(server(p), world(p), p.getPos(), 6, ParticleTypes.ANGRY_VILLAGER, 4);
			sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 1.2f);
			return true;
		});
	}

	// ============================================== Características y definitivas

	/** Registra (una vez) las características y definitivas de los roles nuevos. */
	static synchronized void ensureRegistered() {
		if (registered) return;
		registered = true;

		Signatures.register(Role.GUNSLINGER, (p, r) -> { // Ráfaga sin fin
			AbilityState.repeat(server(p), 20, 2, i -> {
				List<LivingEntity> t = nearest(p, 20, 8);
				if (t.isEmpty()) return;
				LivingEntity e = t.get(p.getRandom().nextInt(t.size()));
				Targeting.particleLine(world(p), ParticleFx.dust(0x26C6DA, 0.9f), muzzle(p), e.getEyePos());
				e.timeUntilRegen = 0;
				e.damage(magic(p), 3 + r * 0.5f);
				sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.9f);
			});
			return true;
		});
		Ultimates.register(Role.GUNSLINGER, (p, r) -> { // Mediodía
			List<LivingEntity> targets = Targeting.hostilesAround(p, 32);
			for (LivingEntity t : targets) effect(t, StatusEffects.GLOWING, 30, 0);
			sound(p, SoundEvents.BLOCK_BELL_USE, 0.6f);
			AbilityState.schedule(server(p), 20, () -> {
				for (LivingEntity t : targets) {
					if (!t.isAlive()) continue;
					Targeting.particleLine(world(p), ParticleFx.dust(0xFFD54F, 1.3f), muzzle(p), t.getEyePos());
					t.damage(magic(p), 12 + 3 * r);
				}
				CastFx.screen(p, 0, 0xFFD54F, 0.9f);
			});
			return !targets.isEmpty();
		});

		Signatures.register(Role.WEAPON_SUMMONER, (p, r) -> { // Mil espadas
			for (LivingEntity e : Targeting.hostilesAround(p, 5)) {
				e.damage(magic(p), 10 + 2 * r);
				e.takeKnockback(1.0, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			for (int i = 0; i < 3; i++) ParticleFx.ring(world(p), p.getPos().add(0, 0.6 + i * 0.5, 0), 3 + i, ParticleTypes.ENCHANTED_HIT, 36);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f);
			return true;
		});
		Ultimates.register(Role.WEAPON_SUMMONER, (p, r) -> { // Arsenal infinito
			AbilityState.repeat(server(p), 10, 10, i -> {
				for (LivingEntity e : Targeting.hostilesAround(p, 16)) {
					Targeting.particleLine(world(p), ParticleTypes.ENCHANTED_HIT, p.getEyePos().add(orbit(i, 1.5)), e.getEyePos());
					e.timeUntilRegen = 0;
					e.damage(magic(p), 3 + r);
				}
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f + 0.05f * i);
			});
			return true;
		});

		Signatures.register(Role.HUNTER, (p, r) -> { // Disparo del cazador
			int hits = lineDamage(p, 40, 12 + 2 * r, ParticleTypes.CRIT, false);
			sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 0.6f);
			return hits > 0;
		});
		Ultimates.register(Role.HUNTER, (p, r) -> { // Cacería salvaje
			List<LivingEntity> targets = Targeting.hostilesAround(p, 24);
			for (LivingEntity t : targets) {
				MARKS.put(t.getUuid(), new Object[]{p.getUuid(), now(p) + 300, 35});
				effect(t, StatusEffects.GLOWING, 300, 0);
			}
			AbilityState.repeat(server(p), 3, 10, i -> {
				for (LivingEntity t : targets) {
					if (!t.isAlive()) continue;
					Targeting.particleLine(world(p), ParticleTypes.CRIT, t.getPos().add(0, 6, 0), t.getEyePos());
					t.timeUntilRegen = 0;
					t.damage(p.getDamageSources().playerAttack(p), 3 + r);
				}
			});
			sound(p, SoundEvents.ENTITY_WOLF_HOWL, 0.8f);
			return !targets.isEmpty();
		});

		Signatures.register(Role.BARD, (p, r) -> { // Sinfonía de guerra
			for (PlayerEntity ally : allies(p, 16)) {
				effect(ally, StatusEffects.STRENGTH, 300, 1);
				effect(ally, StatusEffects.SPEED, 300, 1);
				effect(ally, StatusEffects.REGENERATION, 300, 0);
			}
			ParticleFx.spiral(world(p), p.getPos(), 3, 3, ParticleTypes.NOTE, 60, 4);
			sound(p, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f);
			return true;
		});
		Ultimates.register(Role.BARD, (p, r) -> { // Himno de los héroes
			for (PlayerEntity ally : allies(p, 20)) {
				ally.setHealth(ally.getMaxHealth());
				effect(ally, StatusEffects.STRENGTH, 300, 1);
				effect(ally, StatusEffects.RESISTANCE, 300, 1);
				effect(ally, StatusEffects.ABSORPTION, 300, 3);
			}
			ParticleFx.shockwave(server(p), world(p), p.getPos(), 20, ParticleTypes.NOTE, 10);
			sound(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f);
			return true;
		});

		Signatures.register(Role.WARLOCK, (p, r) -> { // Puerta del abismo
			Vec3d center = Targeting.lookEnd(p, 20);
			for (LivingEntity e : around(p, center, 8)) {
				e.damage(magic(p), 6 + 2 * r);
				effect(e, StatusEffects.WITHER, 100, 1);
				effect(e, StatusEffects.LEVITATION, 40, 0);
			}
			ParticleFx.runeCircle(world(p), center, 4, ParticleFx.dust(0xAD1457, 1.4f));
			world(p).spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1, center.z, 80, 2, 1, 2, 0.1);
			sound(p, SoundEvents.BLOCK_END_PORTAL_SPAWN, 1.4f);
			return true;
		});
		Ultimates.register(Role.WARLOCK, (p, r) -> { // Avatar demoníaco
			state(p).avatarUntil = now(p) + 300;
			effect(p, StatusEffects.STRENGTH, 300, 1);
			effect(p, StatusEffects.RESISTANCE, 300, 0);
			effect(p, StatusEffects.FIRE_RESISTANCE, 300, 0);
			effect(p, StatusEffects.SPEED, 300, 0);
			ParticleFx.spiral(world(p), p.getPos(), 1.0, 2.6, ParticleFx.dust(0xAD1457, 1.4f), 60, 3);
			sound(p, SoundEvents.ENTITY_WITHER_SPAWN, 1.4f);
			return true;
		});

		Signatures.register(Role.TAMER, (p, r) -> { // Manada espectral
			for (int i = 0; i < 4; i++) spawnWolf(p, i, 600, 1);
			sound(p, SoundEvents.ENTITY_WOLF_HOWL, 1.0f);
			return true;
		});
		Ultimates.register(Role.TAMER, (p, r) -> { // Rey de las bestias
			IronGolemEntity golem = EntityType.IRON_GOLEM.create(p.getWorld());
			if (golem != null) {
				Vec3d pos = Targeting.findFreeSpot(p, p.getPos().add(Targeting.horizontalFacing(p).multiply(2)));
				golem.refreshPositionAndAngles(pos.x, pos.y, pos.z, p.getYaw(), 0);
				golem.setPlayerCreated(true);
				AbilityState.trackSummon(golem, p, 600);
				p.getWorld().spawnEntity(golem);
			}
			for (int i = 0; i < 3; i++) spawnWolf(p, i, 600, 1);
			sound(p, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 0.8f);
			return true;
		});
	}

	// ============================================================ Pasivas (golpes)

	/** Bonificación de daño saliente (se suma a 1 en {@link RoleMechanics#outgoing}). */
	static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct, float amount) {
		State s = state(p);
		float bonus = 0f;
		boolean ranged = !direct;

		// Mago pistolero
		int quickdraw = rank(p, "quickdraw");
		if (quickdraw > 0 && ranged) bonus += 0.04f * quickdraw;
		int eagle = rank(p, "eagle_eye");
		if (eagle > 0 && ranged && p.getRandom().nextFloat() < 0.08f * eagle) {
			bonus += 0.5f;
			world(p).spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.7), target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
		}
		int cylinder = rank(p, "arcane_cylinder");
		if (cylinder > 0 && ranged) {
			s.cylinder++;
			if (s.cylinder >= 6) {
				s.cylinder = 0;
				GunslingerKit.onExplosiveBullet(p); // v67 F6: Sin remordimientos
				bonus += 0.25f + 0.25f * cylinder;
				for (LivingEntity e : around(p, target.getPos(), 2.5)) {
					if (e != target) e.damage(magic(p), 3 + cylinder);
				}
				world(p).spawnParticles(ParticleTypes.EXPLOSION, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0, 0, 0, 0);
				sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.2f);
			}
		}
		// Invocador de armas
		int armory = rank(p, "armory");
		if (armory > 0 && direct) bonus += 0.05f * armory;
		if (rank(p, "floating_arsenal") > 0 && direct && s.blades > 0) {
			s.blades--;
			if (WeaponSummonerKit.echoReturns(p)) s.blades++; // v67 F6: Eco de acero
			Targeting.particleLine(world(p), ParticleTypes.ENCHANTED_HIT, p.getEyePos().add(orbit(s.blades, 1.2)), target.getEyePos());
			LivingEntity t = target;
			AbilityState.schedule(server(p), 2, () -> {
				if (t.isAlive()) {
					t.timeUntilRegen = 0;
					t.damage(magic(p), bladeDamage(p, 3 + rank(p, "floating_arsenal")));
				}
			});
		}
		// Cazador
		int predator = rank(p, "predator");
		if (predator > 0 && ranged) bonus += 0.05f * predator;
		Object[] mark = MARKS.get(target.getUuid());
		if (mark != null) {
			if ((Integer) mark[1] < now(p)) MARKS.remove(target.getUuid());
			else if (p.getUuid().equals(mark[0])) bonus += (Integer) mark[2] / 100f;
		}
		int hunt = rank(p, "the_hunt");
		if (hunt > 0) {
			s.prey = target.getId() == s.preyTarget ? Math.min(2 + hunt, s.prey + 1) : 1;
			s.preyTarget = target.getId();
			bonus += 0.06f * s.prey;
		}
		// Brujo
		int bargain = rank(p, "dark_bargain");
		if (bargain > 0 && ranged) {
			bonus += 0.05f * bargain;
			if (p.getHealth() < p.getMaxHealth() * 0.5f) bonus += 0.10f;
		}
		if (rank(p, "demonic_pact") > 0 && ranged && s.corruptionUntil > now(p)) bonus += 0.06f * s.corruption;
		// Domador
		int pack = rank(p, "pack_leader");
		int companion = rank(p, "companion");
		if (pack > 0 || companion > 0) {
			List<TameableEntity> pets = pets(p, 16);
			bonus += 0.04f * pack * Math.min(3, pets.size());
			if (companion > 0 && pets.stream().anyMatch(pet -> pet.getTarget() == target)) bonus += 0.05f + 0.05f * companion;
		}
		return bonus;
	}

	/** Tras calcular el daño: robos de vida del Brujo. */
	static void afterOutgoing(ServerPlayerEntity p, float damage, boolean direct) {
		if (damage <= 0 || p.getHealth() >= p.getMaxHealth()) return;
		int link = rank(p, "soul_link");
		if (link > 0 && !direct) p.heal(damage * 0.04f * link);
		if (state(p).avatarUntil > now(p)) p.heal(damage * 0.20f);
	}

	// ======================================================== Pasivas (recibir)

	static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		State s = state(p);
		LivingEntity attacker = source.getAttacker() instanceof LivingEntity l ? l : null;
		boolean melee = attacker != null && source.getSource() == attacker;

		// Invocador – Guardia de espadas e Intercepción
		int guard = rank(p, "blade_guard");
		if (guard > 0 && s.blades > 0) amount *= Math.max(0.6f, 1f - 0.015f * guard * s.blades); // máx. −40 %
		int intercept = rank(p, "intercepting_blade");
		if (intercept > 0 && melee && s.blades > 0 && p.getRandom().nextFloat() < 0.1f * intercept) {
			s.blades--;
			amount *= 0.5f;
			world(p).spawnParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getBodyY(0.6), p.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
			sound(p, SoundEvents.ITEM_SHIELD_BLOCK, 1.4f);
		}
		// Pistolero – Rodar
		int roll = rank(p, "dodge_roll");
		if (roll > 0 && attacker != null && p.getRandom().nextFloat() < 0.1f * roll && ready(p, s, "dodge_roll", 160)) {
			Vec3d away = p.getPos().subtract(attacker.getPos()).normalize();
			p.setVelocity(away.x * 1.1, 0.25, away.z * 1.1);
			p.velocityModified = true;
			effect(p, StatusEffects.SPEED, 30, 1);
		}
		// Bardo – Nana
		int lullaby = rank(p, "lullaby");
		if (lullaby > 0 && melee && p.getRandom().nextFloat() < 0.1f * lullaby) {
			effect(attacker, StatusEffects.SLOWNESS, 60, 1);
			effect(attacker, StatusEffects.WEAKNESS, 60, 0);
			world(p).spawnParticles(ParticleTypes.NOTE, attacker.getX(), attacker.getBodyY(1.1), attacker.getZ(), 5, 0.3, 0.2, 0.3, 1);
		}
		// Brujo – Égida demoníaca
		int ward = rank(p, "demonic_ward");
		if (ward > 0 && attacker != null && p.getRandom().nextFloat() < 0.1f * ward) {
			effect(attacker, StatusEffects.WITHER, 40, 0);
			p.heal(1.0f);
		}
		// Domador – Instinto protector
		int protect = rank(p, "protective_instinct");
		if (protect > 0 && attacker != null && !(attacker instanceof PlayerEntity)) {
			for (TameableEntity pet : pets(p, 16)) {
				if (pet.isSitting() || pet == attacker) continue;
				pet.setTarget(attacker);
				effect(pet, StatusEffects.SPEED, 60, 0);
				if (protect >= 2) effect(pet, StatusEffects.STRENGTH, 60, protect - 2);
			}
		}
		return amount;
	}

	// ================================================================== Muertes

	static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		State s = state(p);
		int reload = rank(p, "reload");
		if (reload > 0) addEnergy(p, 8 * reload);
		if (rank(p, "reforge") > 0) s.blades = Math.min(maxBlades(p), s.blades + 1);
		int trophy = rank(p, "trophy");
		if (trophy > 0) p.getInventory().insertStack(new ItemStack(Items.ARROW, 1 + trophy));
		int feast = rank(p, "soul_feast");
		if (feast > 0) {
			p.heal(2 * feast);
			addEnergy(p, 10);
		}
		int feral = rank(p, "feral_feast");
		if (feral > 0) for (TameableEntity pet : pets(p, 16)) pet.heal(2 * feral);
		MARKS.remove(victim.getUuid());
	}

	// ============================================================ Habilidades

	/** Tras lanzar una habilidad: Crescendo y Bis (Bardo), Corrupción (Brujo). */
	static void onCast(ServerPlayerEntity p, Role role, String cooldownKey, int slot) {
		State s = state(p);
		if (role == Role.BARD) {
			notes(p);
			int encore = rank(p, "encore");
			if (encore > 0 && slot < 4 && p.getRandom().nextFloat() < 0.1f * encore) {
				AbilityManager.resetCooldown(p, cooldownKey, slot);
				p.sendMessage(net.minecraft.text.Text.translatable("aptitudes.mech.encore").withColor(0xF48FB1), true);
			}
		}
		int pact = rank(p, "demonic_pact");
		if (role == Role.WARLOCK && pact > 0) {
			if (p.getHealth() > 3) p.setHealth(p.getHealth() - 1);
			s.corruption = s.corruptionUntil > now(p) ? Math.min(2 + pact, s.corruption + 1) : 1;
			s.corruptionUntil = now(p) + 200;
			world(p).spawnParticles(ParticleFx.dust(0xAD1457, 1.0f), p.getX(), p.getBodyY(0.5), p.getZ(), 10, 0.3, 0.5, 0.3, 0);
		}
	}

	/** Bardo – Crescendo: cada canción suma una nota; con 4, final con Regeneración II y Resistencia. */
	private static void notes(ServerPlayerEntity p) {
		int crescendo = rank(p, "crescendo");
		if (crescendo <= 0) return;
		State s = state(p);
		s.notes++;
		if (s.notes >= 4) {
			s.notes = 0;
			for (PlayerEntity ally : allies(p, 10)) {
				effect(ally, StatusEffects.REGENERATION, 80 + 20 * crescendo, 1);
				effect(ally, StatusEffects.RESISTANCE, 80 + 20 * crescendo, 0);
			}
			ParticleFx.spiral(world(p), p.getPos(), 1.5, 3, ParticleTypes.NOTE, 40, 3);
			p.sendMessage(net.minecraft.text.Text.translatable("aptitudes.mech.crescendo").withColor(0xF48FB1), true);
		}
	}

	// ================================================================== Tick

	/** Cada segundo. */
	static void tick(MinecraftServer server, ServerPlayerEntity p) {
		State s = state(p);
		int now = server.getTicks();
		// Invocador – Arsenal flotante: una espada cada 4 s.
		if (rank(p, "floating_arsenal") > 0) {
			if (s.blades < maxBlades(p) && now - s.lastBladeGain >= (WeaponSummonerKit.fastBlades(p) ? 40 : 80)) {
				s.blades++;
				s.lastBladeGain = now;
			}
			for (int i = 0; i < s.blades; i++) {
				Vec3d pos = p.getPos().add(0, 1.2, 0).add(orbit(i + now / 10, 1.1));
				world(p).spawnParticles(ParticleTypes.ENCHANTED_HIT, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0);
			}
		} else {
			s.blades = 0;
		}
		// Cazador – Rastreador: cada 10 s los hostiles cercanos brillan.
		int tracker = rank(p, "tracker");
		if (tracker > 0 && now % 200 < 20) {
			for (LivingEntity e : Targeting.hostilesAround(p, 12 + 4 * tracker)) {
				e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0, true, false));
			}
		}
		// Cazador – Camuflaje: 2 s agachado e inmóvil → Invisibilidad.
		boolean still = p.getPos().squaredDistanceTo(s.lastPos) < 0.01;
		s.stillSeconds = still ? s.stillSeconds + 1 : 0;
		s.lastPos = p.getPos();
		if (rank(p, "camouflage") > 0 && p.isSneaking() && s.stillSeconds >= 2) {
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 40, 0, true, false));
		}
		// Bardo – Presencia inspiradora y Melodía calmante
		int presence = rank(p, "inspiring_presence");
		if (presence > 0) {
			for (PlayerEntity ally : allies(p, 8)) {
				ally.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false));
				if (presence >= 2) ally.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 0, true, false));
				if (presence >= 3) ally.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 0, true, false));
			}
		}
		int melody = rank(p, "soothing_melody");
		if (melody > 0 && now % 100 < 20) {
			for (PlayerEntity ally : allies(p, 8)) ally.heal(0.5f * melody);
		}
		// Brujo – la corrupción se pierde a los 10 s sin lanzar.
		if (s.corruptionUntil <= now) s.corruption = 0;
		// Domador – Vínculo animal: efectos a tus mascotas.
		int bond = rank(p, "beast_bond");
		if (bond > 0) {
			for (TameableEntity pet : pets(p, 24)) {
				pet.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0, true, false));
				if (bond >= 2) pet.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0, true, false));
				if (bond >= 3) pet.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0, true, false));
			}
		}
	}

	// ==================================================================== HUD

	/** Estado de la mecánica de un rol nuevo para el HUD, o null. */
	@Nullable
	static MechanicPayload.Entry mechanic(ServerPlayerEntity p, String node, int r) {
		State s = state(p);
		int now = now(p);
		return switch (node) {
			case "arcane_cylinder" -> new MechanicPayload.Entry(node, s.cylinder, 6, "");
			case "floating_arsenal" -> new MechanicPayload.Entry(node, s.blades, maxBlades(p), "");
			case "the_hunt" -> new MechanicPayload.Entry(node, Math.min(s.prey, 2 + r), 2 + r, "");
			case "crescendo" -> new MechanicPayload.Entry(node, s.notes, 4, "");
			case "demonic_pact" -> new MechanicPayload.Entry(node, s.corruptionUntil > now ? Math.min(s.corruption, 2 + r) : 0, 2 + r, "");
			case "companion" -> new MechanicPayload.Entry(node, Math.min(5, pets(p, 24).size()), 5, "");
			default -> null;
		};
	}

	// ============================================================== Utilidades

	/** v67 (F6): espadas flotantes actuales (para {@code WeaponSummonerKit}). */
	static int bladeCount(ServerPlayerEntity p) {
		return state(p).blades;
	}

	static void setBladeCount(ServerPlayerEntity p, int count) {
		state(p).blades = Math.max(0, count);
	}

	static int bladeCap(ServerPlayerEntity p) {
		return maxBlades(p);
	}

	/** Daño de una espada del Invocador, con Espadas afiladas. */
	private static float bladeDamage(ServerPlayerEntity p, float base) {
		return base * (1f + WeaponSummonerKit.sharpBonus(p));
	}

	/** v67 (F6): ¿{@code target} está marcado por {@code p} (Marca del cazador)? */
	static boolean isMarkedBy(ServerPlayerEntity p, LivingEntity target) {
		Object[] mark = MARKS.get(target.getUuid());
		return mark != null && p.getUuid().equals(mark[0]) && (Integer) mark[1] >= now(p);
	}

	/** v67 (F6): marca a {@code target} para {@code p} durante {@code ticks} con {@code percent} % de daño extra. */
	static void markTarget(ServerPlayerEntity p, LivingEntity target, int ticks, int percent) {
		MARKS.put(target.getUuid(), new Object[]{p.getUuid(), now(p) + ticks, percent});
	}

	private static int maxBlades(ServerPlayerEntity p) {
		// Legendaria Puerta del arsenal: +1 espada máxima.
		int legend = dev.aptitudes.unique.UniqueManager.has(p, dev.aptitudes.unique.UniqueSkill.GATE_OF_ARMS) ? 1 : 0;
		return 2 + rank(p, "floating_arsenal") + legend + WeaponSummonerKit.extraCap(p); // v67 F6: Armería viva
	}

	/** Punto de una órbita alrededor del jugador (para las espadas flotantes). */
	private static Vec3d orbit(int index, double radius) {
		double a = index * 2.1;
		return new Vec3d(Math.cos(a) * radius, 0.3 * Math.sin(index), Math.sin(a) * radius);
	}

	private static Vec3d muzzle(ServerPlayerEntity p) {
		return p.getEyePos().add(p.getRotationVec(1.0f).multiply(0.6)).add(0, -0.2, 0);
	}

	@Nullable
	private static LivingEntity markedBy(ServerPlayerEntity p) {
		for (LivingEntity e : Targeting.hostilesAround(p, 32)) {
			Object[] mark = MARKS.get(e.getUuid());
			if (mark != null && p.getUuid().equals(mark[0]) && (Integer) mark[1] >= now(p)) return e;
		}
		return null;
	}

	/** Daño en línea recta desde los ojos. @return número de enemigos alcanzados. */
	private static int lineDamage(ServerPlayerEntity p, double length, float damage, ParticleEffect particle, boolean launch) {
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, length);
		Vec3d dir = end.subtract(start).normalize();
		int hits = 0;
		for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(1.0),
				e -> Targeting.isHostileTo(p, e))) {
			Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0).subtract(start);
			double along = to.dotProduct(dir);
			if (along < 0 || to.subtract(dir.multiply(along)).lengthSquared() > 2.0) continue;
			e.damage(magic(p), damage);
			if (launch) {
				e.addVelocity(dir.x * 0.4, 0.5, dir.z * 0.4);
				e.velocityModified = true;
			}
			hits++;
		}
		Targeting.particleLine(world(p), particle, start, end);
		return hits;
	}

	private static List<LivingEntity> around(ServerPlayerEntity p, Vec3d center, double radius) {
		double r2 = radius * radius;
		return p.getWorld().getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(radius),
				e -> Targeting.isHostileTo(p, e) && e.squaredDistanceTo(center) <= r2);
	}

	private static List<LivingEntity> nearest(ServerPlayerEntity p, double radius, int max) {
		return Targeting.hostilesAround(p, radius).stream()
				.sorted(Comparator.comparingDouble(p::squaredDistanceTo)).limit(max).toList();
	}

	/** Jugadores cercanos, incluido el propio. */
	private static List<PlayerEntity> allies(ServerPlayerEntity p, double radius) {
		return Targeting.playersAround(p, radius);
	}

	/** Mascotas domesticadas del jugador en el radio. */
	static List<TameableEntity> pets(ServerPlayerEntity p, double radius) {
		return p.getWorld().getEntitiesByClass(TameableEntity.class, p.getBoundingBox().expand(radius),
				t -> t.isAlive() && t.isOwner(p));
	}

	private static void spawnWolf(ServerPlayerEntity p, int index, int lifetime, int strength) {
		WolfEntity wolf = EntityType.WOLF.create(p.getWorld());
		if (wolf == null) return;
		Vec3d side = Targeting.horizontalFacing(p).rotateY((float) Math.toRadians(-60 + 40 * index));
		Vec3d pos = Targeting.findFreeSpot(p, p.getPos().add(side.multiply(1.5)));
		wolf.refreshPositionAndAngles(pos.x, pos.y, pos.z, p.getYaw(), 0);
		wolf.setTamed(true, true);
		wolf.setOwner(p);
		wolf.setHealth(wolf.getMaxHealth());
		wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, lifetime, strength, true, false));
		AbilityState.trackSummon(wolf, p, lifetime);
		p.getWorld().spawnEntity(wolf);
		world(p).spawnParticles(ParticleTypes.SOUL, pos.x, pos.y + 0.5, pos.z, 10, 0.3, 0.3, 0.3, 0.02);
	}

	private static void addEnergy(ServerPlayerEntity p, double amount) {
		RoleData data = RoleManager.get(p);
		data.setEnergy(Math.min(AbilityManager.maxEnergy(p), data.energy() + amount));
		AbilityManager.sendEnergy(p, false);
	}

	private static ServerWorld world(ServerPlayerEntity p) {
		return p.getServerWorld();
	}

	private static MinecraftServer server(ServerPlayerEntity p) {
		return p.getServer();
	}

	private static DamageSource magic(ServerPlayerEntity p) {
		return p.getDamageSources().indirectMagic(p, p);
	}

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}
}
