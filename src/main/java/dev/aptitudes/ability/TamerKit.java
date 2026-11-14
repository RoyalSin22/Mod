package dev.aptitudes.ability;

import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.TamerRules.*;

/**
 * v67 (F7): las 10 habilidades nuevas del Domador (N1–N10). Cifras en {@link TamerRules} (puro, JUnit). Las mascotas
 * son las que ya consulta el rol ({@code NewRoles.pets}): domadas por el jugador y vivas; las invocaciones y las
 * mascotas de otros jugadores no cuentan. Estado en memoria: al reiniciar el servidor se pierde el enfriamiento del
 * Frenesí de caza.
 */
public final class TamerKit {
	private static final String N = "tamer_n";

	private static final Map<UUID, Long> FRENZY_READY = new HashMap<>();

	private TamerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", TamerKit::gatherPack);
		Abilities.register(N + "4", TamerKit::coordinatedStrikes);
		Abilities.register(N + "6", TamerKit::protectiveMantle);
		Abilities.register(N + "8", TamerKit::animalRage);
		Abilities.register(N + "10", TamerKit::kingsCall);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Convocar a la manada: las mascotas acuden a tu lado y se resguardan. */
	private static boolean gatherPack(ServerPlayerEntity p, int rank) {
		List<TameableEntity> pets = NewRoles.pets(p, GATHER_RADIUS);
		if (pets.isEmpty()) return false;
		int ticks = gatherTicks(rank);
		for (TameableEntity pet : pets) {
			pet.refreshPositionAndAngles(p.getX() + (p.getRandom().nextDouble() - 0.5) * 2, p.getY(),
					p.getZ() + (p.getRandom().nextDouble() - 0.5) * 2, pet.getYaw(), pet.getPitch());
			pet.getNavigation().stop();
			KitSupport.effect(pet, StatusEffects.RESISTANCE, ticks, 0);
			Targeting.burst(p.getServerWorld(), ParticleTypes.POOF, pet, 8, 0.4);
		}
		KitSupport.sound(p, SoundEvents.ENTITY_WOLF_HOWL, 1.0f);
		return true;
	}

	/** N4 Zarpazos coordinados: cada mascota cercana golpea a tu objetivo. */
	private static boolean coordinatedStrikes(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, STRIKE_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		List<TameableEntity> pets = NewRoles.pets(p, STRIKE_PET_RADIUS).stream()
				.sorted(Comparator.comparingDouble(t -> t.squaredDistanceTo(target))).limit(STRIKE_MAX_PETS).toList();
		if (pets.isEmpty()) return false;
		float damage = (float) strikeDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), pets.size(), STRIKE_INTERVAL_TICKS, i -> {
			TameableEntity pet = pets.get(i);
			if (!p.isAlive() || !target.isAlive() || !pet.isAlive()) return;
			Targeting.particleLine(p.getServerWorld(), ParticleTypes.CRIT, pet.getEyePos(), target.getPos().add(0, target.getHeight() / 2, 0));
			target.timeUntilRegen = 0;
			target.damage(KitSupport.magic(p), damage);
			KitSupport.sound(p, SoundEvents.ENTITY_WOLF_GROWL, 1.0f + 0.1f * i);
		});
		return true;
	}

	/** N6 Manto protector: absorción y resistencia para ti y tus mascotas. */
	private static boolean protectiveMantle(ServerPlayerEntity p, int rank) {
		float absorption = mantleAbsorption(rank);
		shield(p, absorption);
		for (TameableEntity pet : NewRoles.pets(p, MANTLE_RADIUS)) shield(pet, absorption);
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), MANTLE_RADIUS, ParticleTypes.HAPPY_VILLAGER, 36);
		KitSupport.sound(p, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), 1.0f);
		return true;
	}

	/** N8 Furia animal: fuerza, velocidad y resistencia para tus mascotas. */
	private static boolean animalRage(ServerPlayerEntity p, int rank) {
		List<TameableEntity> pets = NewRoles.pets(p, RAGE_RADIUS);
		if (pets.isEmpty()) return false;
		int ticks = rageTicks(rank);
		for (TameableEntity pet : pets) {
			KitSupport.effect(pet, StatusEffects.STRENGTH, ticks, 1);
			KitSupport.effect(pet, StatusEffects.SPEED, ticks, 1);
			KitSupport.effect(pet, StatusEffects.RESISTANCE, ticks, 0);
			Targeting.burst(p.getServerWorld(), ParticleTypes.ANGRY_VILLAGER, pet, 8, 0.4);
		}
		KitSupport.sound(p, SoundEvents.ENTITY_WOLF_HOWL, 0.7f);
		return true;
	}

	/** N10 Llamada del rey: tus mascotas se curan del todo y se vuelven formidables. */
	private static boolean kingsCall(ServerPlayerEntity p, int rank) {
		List<TameableEntity> pets = NewRoles.pets(p, KING_RADIUS);
		if (pets.isEmpty()) return false;
		for (TameableEntity pet : pets) {
			pet.setHealth(pet.getMaxHealth());
			KitSupport.effect(pet, StatusEffects.STRENGTH, KING_TICKS, 2);
			KitSupport.effect(pet, StatusEffects.RESISTANCE, KING_TICKS, 1);
			Targeting.burst(p.getServerWorld(), ParticleTypes.TOTEM_OF_UNDYING, pet, 14, 0.5);
		}
		KitSupport.sound(p, SoundEvents.EVENT_RAID_HORN.value(), 0.9f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Frenesí de caza y Vínculo de sangre (tras un golpe tuyo). */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, boolean direct, float damage) {
		if (damage <= 0) return;
		int frenzy = RoleManager.activeRank(p, N + "2");
		if (frenzy > 0) {
			long now = KitSupport.now(p);
			if (now >= FRENZY_READY.getOrDefault(p.getUuid(), 0L)) {
				List<TameableEntity> pets = NewRoles.pets(p, FRENZY_RADIUS);
				if (!pets.isEmpty()) {
					FRENZY_READY.put(p.getUuid(), now + FRENZY_COOLDOWN_TICKS);
					for (TameableEntity pet : pets.stream().limit(FRENZY_MAX_PETS).toList()) {
						KitSupport.effect(pet, StatusEffects.SPEED, FRENZY_TICKS, 1);
					}
				}
			}
		}
		int blood = RoleManager.activeRank(p, N + "9");
		if (blood > 0) {
			TameableEntity hurt = NewRoles.pets(p, BLOOD_RADIUS).stream().filter(t -> t.getHealth() < t.getMaxHealth())
					.min(Comparator.comparingDouble(TameableEntity::getHealth)).orElse(null);
			if (hurt != null) {
				hurt.heal(damage * bloodShare(blood));
				Targeting.burst(p.getServerWorld(), ParticleTypes.HEART, hurt, 2, 0.3);
			}
		}
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		boolean fur = now % FUR_INTERVAL_TICKS == 0;
		boolean care = now % CARE_INTERVAL_TICKS == 0;
		boolean scent = now % SCENT_INTERVAL_TICKS == 0;
		if (!fur && !care && !scent) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			int furRank = fur ? RoleManager.activeRank(p, N + "1") : 0;
			int careRank = care ? RoleManager.activeRank(p, N + "5") : 0;
			if (furRank > 0) {
				for (TameableEntity pet : NewRoles.pets(p, FUR_RADIUS)) shieldOnly(pet, furAbsorption(furRank));
			}
			if (careRank > 0) {
				for (TameableEntity pet : NewRoles.pets(p, CARE_RADIUS)) {
					if (wounded(pet.getHealth(), pet.getMaxHealth())) pet.heal(careHeal(careRank));
				}
			}
			if (scent && RoleManager.activeRank(p, N + "7") > 0) {
				for (LivingEntity e : Targeting.hostilesAround(p, SCENT_RADIUS)) KitSupport.effect(e, StatusEffects.GLOWING, SCENT_TICKS, 0);
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		FRENZY_READY.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** Absorción (sin quitar la que ya tuviera) y Resistencia I. */
	private static void shield(LivingEntity e, float absorption) {
		shieldOnly(e, absorption);
		KitSupport.effect(e, StatusEffects.RESISTANCE, MANTLE_TICKS, 0);
	}

	private static void shieldOnly(LivingEntity e, float absorption) {
		if (e.getAbsorptionAmount() < absorption) e.setAbsorptionAmount(absorption);
	}
}
