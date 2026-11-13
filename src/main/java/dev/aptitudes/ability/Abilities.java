package dev.aptitudes.ability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.EntityTypeTags;
import dev.aptitudes.role.Element;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de las 16 habilidades activas (2 por rol). Todas escalan con
 * el rango {@code r} (1-3). Los valores concretos están documentados en el README.
 */
public final class Abilities {
	private static final Map<String, Ability> REGISTRY = new HashMap<>();

	static {
		// ---------------- Bersérker ----------------
		register("rage", (p, r) -> {
			int ticks = (6 + 2 * r) * 20;
			effect(p, StatusEffects.STRENGTH, ticks, r >= 3 ? 1 : 0);
			effect(p, StatusEffects.SPEED, ticks, 0);
			Targeting.burst(world(p), ParticleTypes.ANGRY_VILLAGER, p, 10, 0.6);
			sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.8f);
			return true;
		});
		register("war_cry", (p, r) -> {
			var targets = Targeting.hostilesAround(p, 4 + r);
			for (LivingEntity e : targets) {
				effect(e, StatusEffects.WEAKNESS, 100, 0);
				effect(e, StatusEffects.SLOWNESS, 100, r - 1);
				e.takeKnockback(0.6 + 0.2 * r, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			Targeting.burst(world(p), ParticleTypes.SONIC_BOOM, p, 1, 0);
			sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 1.3f);
			return true;
		});

		// ---------------- Mago ----------------
		// Proyectil arcano (id "fireball" por compatibilidad con rangos guardados): sin fuego ni elementos.
		register("fireball", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 24);
			Vec3d end = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.lookEnd(p, 24);
			Targeting.particleLine(world(p), ParticleFx.dust(0xB388FF, 1.2f), p.getEyePos(), end);
			sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1.3f);
			if (target == null) return false;
			float damage = 4 + 2 * r;
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(2),
					e -> e == target || Targeting.isHostileTo(p, e))) {
				e.damage(p.getDamageSources().indirectMagic(p, p), e == target ? damage : damage / 2);
				if (e == target) effect(e, StatusEffects.GLOWING, 60, 0); // runa de rastreo
			}
			world(p).spawnParticles(ParticleTypes.WITCH, end.x, end.y, end.z, 20, 0.5, 0.5, 0.5, 0.1);
			ParticleFx.ring(world(p), end, 1.5, ParticleTypes.ENCHANT, 16);
			return true;
		});
		register("blink", (p, r) -> {
			double distance = 6 + 2 * r;
			Vec3d direction = p.getRotationVec(1.0f);
			Vec3d eyeTarget = Targeting.lookEnd(p, distance).subtract(direction.multiply(0.6));
			Vec3d feet = eyeTarget.subtract(0, p.getStandingEyeHeight(), 0);
			Vec3d spot = Targeting.findFreeSpot(p, feet);
			if (spot == null) return false;
			Targeting.burst(world(p), ParticleTypes.PORTAL, p, 30, 0.5);
			p.requestTeleport(spot.x, spot.y, spot.z);
			p.fallDistance = 0;
			Targeting.burst(world(p), ParticleTypes.REVERSE_PORTAL, p, 30, 0.5);
			sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.2f);
			return true;
		});

		// ---------------- Asesino ----------------
		register("vanish", (p, r) -> {
			int ticks = (4 + 2 * r) * 20;
			effect(p, StatusEffects.INVISIBILITY, ticks, 0);
			effect(p, StatusEffects.SPEED, ticks, 0);
			// Los mobs que te perseguían pierden el rastro.
			for (MobEntity mob : p.getWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(16),
					m -> m.getTarget() == p)) {
				mob.setTarget(null);
			}
			Targeting.burst(world(p), ParticleTypes.LARGE_SMOKE, p, 25, 0.5);
			sound(p, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f);
			return true;
		});
		register("shadow_step", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 12 + 4 * r);
			if (target == null) return false;
			Vec3d behind = target.getPos().subtract(Targeting.horizontalFacing(target).multiply(1.2));
			Vec3d spot = Targeting.findFreeSpot(p, behind);
			if (spot == null) return false;
			Targeting.burst(world(p), ParticleTypes.LARGE_SMOKE, p, 15, 0.4);
			// Aparece mirando en la misma dirección que el objetivo: justo a su espalda.
			p.networkHandler.requestTeleport(spot.x, spot.y, spot.z, target.getYaw(), 10.0f);
			p.fallDistance = 0;
			sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.6f);
			return true;
		});

		// ---------------- Herbolista ----------------
		register("balm", (p, r) -> {
			for (PlayerEntity ally : Targeting.playersAround(p, 6)) {
				ally.heal(HerbalistKit.heal(p, 2 + 2 * r));
				HerbalistKit.onHealed(p, ally);
				effect(ally, StatusEffects.REGENERATION, 100, 0);
				Targeting.burst(world(p), ParticleTypes.HAPPY_VILLAGER, ally, 12, 0.5);
			}
			sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f);
			return true;
		});
		register("poison_thorns", (p, r) -> {
			for (LivingEntity e : Targeting.hostilesAround(p, 4 + r)) {
				effect(e, StatusEffects.POISON, 120, Math.min(r - 1, 1));
				effect(e, StatusEffects.SLOWNESS, 80, 0);
				Targeting.burst(world(p), ParticleTypes.ITEM_SLIME, e, 8, 0.4);
			}
			sound(p, SoundEvents.BLOCK_SWEET_BERRY_BUSH_BREAK, 0.8f);
			return true;
		});

		// ---------------- Ingeniero ----------------
		register("turret", (p, r) -> {
			IronGolemEntity golem = EntityType.IRON_GOLEM.create(p.getWorld());
			if (golem == null) return false;
			Vec3d pos = p.getPos().add(Targeting.horizontalFacing(p).multiply(2));
			Vec3d spot = findSpotFor(golem, pos);
			if (spot == null) return false;
			golem.refreshPositionAndAngles(spot.x, spot.y, spot.z, p.getYaw(), 0);
			golem.setPlayerCreated(true); // nunca ataca a jugadores salvo que le peguen
			golem.setCustomName(Text.translatable("entity.aptitudes.turret"));
			// Registrar ANTES de aparecer: ENTITY_LOAD retira invocaciones no registradas.
			AbilityState.trackSummon(golem, p, (int) ((15 + 5 * r) * 20 * EngineerKit.durationFactor(p)));
			p.getWorld().spawnEntity(golem);
			sound(p, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0f);
			return true;
		});

		// ---------------- Paladín (Escudo divino: antes Escudo de energía) ----------------
		register("divine_shield", (p, r) -> {
			effect(p, StatusEffects.ABSORPTION, 160, r - 1);
			effect(p, StatusEffects.RESISTANCE, (3 + r) * 20, 0);
			Targeting.burst(world(p), ParticleTypes.END_ROD, p, 30, 0.7);
			sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.4f);
			return true;
		});
		register("thrusters", (p, r) -> {
			Vec3d facing = Targeting.horizontalFacing(p);
			Vec3d velocity = facing.multiply(1.2 + 0.3 * r).add(0, 0.5, 0);
			p.setVelocity(velocity);
			p.velocityModified = true; // envía la nueva velocidad al cliente
			p.fallDistance = 0;
			effect(p, StatusEffects.SLOW_FALLING, 40, 0);
			Targeting.burst(world(p), ParticleTypes.FIREWORK, p, 20, 0.3);
			sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f);
			return true;
		});

		// ---------------- Lancero ----------------
		register("lunge", (p, r) -> {
			Vec3d facing = Targeting.horizontalFacing(p);
			p.setVelocity(facing.multiply(1.4 + 0.2 * r).add(0, 0.1, 0));
			p.velocityModified = true;
			Box path = p.getBoundingBox().stretch(facing.multiply(4)).expand(0.5);
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, path,
					e -> Targeting.isHostileTo(p, e))) {
				e.damage(p.getDamageSources().playerAttack(p), 4 + 2 * r);
				e.takeKnockback(0.8, -facing.x, -facing.z);
			}
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.8f);
			return true;
		});
		register("sweep", (p, r) -> {
			for (LivingEntity e : Targeting.hostilesAround(p, 3 + 0.5 * r)) {
				e.damage(p.getDamageSources().playerAttack(p), 3 + 2 * r);
				e.takeKnockback(0.5, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			world(p).spawnParticles(ParticleTypes.SWEEP_ATTACK, p.getX(), p.getBodyY(0.5), p.getZ(), 8, 1.5, 0.1, 1.5, 0);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.9f);
			return true;
		});

		// ---------------- Espadachín ----------------
		register("blade_dance", (p, r) -> {
			int ticks = (6 + 2 * r) * 20;
			effect(p, StatusEffects.HASTE, ticks, r - 1);
			effect(p, StatusEffects.SPEED, ticks, 0);
			Targeting.burst(world(p), ParticleTypes.CRIT, p, 20, 0.6);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f);
			return true;
		});
		register("riposte", (p, r) -> {
			AbilityState.openRiposte(p, (1 + r) * 20, 1.0f + 0.5f * r);
			Targeting.burst(world(p), ParticleTypes.ENCHANTED_HIT, p, 15, 0.5);
			sound(p, SoundEvents.ITEM_SHIELD_BLOCK, 1.2f);
			return true;
		});

		// ---------------- Paladín ----------------
		register("judgement", (p, r) -> {
			float damage = 4 + 2 * r;
			for (LivingEntity e : Targeting.hostilesAround(p, 5 + r)) {
				boolean undead = e.getType().isIn(EntityTypeTags.UNDEAD);
				e.damage(p.getDamageSources().indirectMagic(p, p), undead ? damage * 2 : damage);
				Targeting.burst(world(p), ParticleTypes.END_ROD, e, 6, 0.3);
			}
			for (PlayerEntity ally : Targeting.playersAround(p, 5 + r)) ally.heal(2 + r);
			sound(p, SoundEvents.BLOCK_BELL_USE, 1.2f);
			return true;
		});

		// ---------------- Nigromante ----------------
		register("raise_minions", (p, r) -> {
			// r=1: 1 zombi · r=2: zombi + esqueleto · r=3: 2 zombis + esqueleto (máx. 4 a la vez)
			int zombies = r >= 3 ? 2 : 1;
			int skeletons = r >= 2 ? 1 : 0;
			int spawned = 0;
			for (int i = 0; i < zombies + skeletons; i++) {
				MobEntity minion = Minions.spawn(p, i < zombies ? EntityType.ZOMBIE : EntityType.SKELETON, i);
				if (minion != null) spawned++;
			}
			if (spawned == 0) return false;
			sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 0.8f);
			return true;
		});
		register("death_touch", (p, r) -> {
			// Drenaje en área: daña y marchita; te cura la mitad del daño total.
			float damage = 3 + r;
			float drained = 0;
			for (LivingEntity e : Targeting.hostilesAround(p, 5)) {
				e.damage(p.getDamageSources().indirectMagic(p, p), damage);
				effect(e, StatusEffects.WITHER, 60, 0);
				Targeting.particleLine(world(p), ParticleTypes.SOUL, e.getPos().add(0, 1, 0), p.getPos().add(0, 1, 0));
				drained += damage;
			}
			if (drained <= 0) return false;
			p.heal(drained * 0.5f);
			sound(p, SoundEvents.ENTITY_WITHER_SHOOT, 0.7f);
			return true;
		});

		// ---------------- Druida ----------------
		register("entangling_roots", (p, r) -> {
			var targets = Targeting.hostilesAround(p, 5 + r);
			for (LivingEntity e : targets) {
				effect(e, StatusEffects.SLOWNESS, (2 + r) * 20, 3);
				effect(e, StatusEffects.WEAKNESS, (2 + r) * 20, 0);
				Targeting.burst(world(p), ParticleTypes.COMPOSTER, e, 10, 0.4);
			}
			sound(p, SoundEvents.BLOCK_ROOTS_BREAK, 0.7f);
			return !targets.isEmpty();
		});
		register("bear_form", (p, r) -> {
			int ticks = (8 + 2 * r) * 20;
			effect(p, StatusEffects.STRENGTH, ticks, 0);
			effect(p, StatusEffects.RESISTANCE, ticks, 0);
			effect(p, StatusEffects.HEALTH_BOOST, ticks, r - 1);
			effect(p, StatusEffects.SLOWNESS, ticks, 0);
			p.heal(4 * r);
			Targeting.burst(world(p), ParticleTypes.CAMPFIRE_COSY_SMOKE, p, 8, 0.4);
			sound(p, SoundEvents.ENTITY_POLAR_BEAR_WARNING, 1.0f);
			return true;
		});

		// ---------------- Mercader ----------------
		register("appraiser_eye", (p, r) -> {
			effect(p, StatusEffects.LUCK, 1200, r - 1);
			// Revela durante 10 s las criaturas cercanas (efecto Brillo, temporal).
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class,
					p.getBoundingBox().expand(24), e -> e != p && e.isAlive())) {
				effect(e, StatusEffects.GLOWING, 200, 0);
			}
			sound(p, SoundEvents.ENTITY_VILLAGER_YES, 1.2f);
			return true;
		});
		register("bribe", (p, r) -> {
			// Cuesta 1 esmeralda del inventario además de la energía.
			if (!p.isCreative() && !consumeOne(p, Items.EMERALD)) return false;
			int until = AbilityManager.now(p) + (5 + 2 * r) * 20;
			int count = 0;
			for (MobEntity mob : p.getWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(6 + 2 * r),
					m -> Targeting.isHostileTo(p, m))) {
				AbilityState.pacify(mob, until);
				Targeting.burst(world(p), ParticleTypes.HAPPY_VILLAGER, mob, 6, 0.3);
				count++;
			}
			sound(p, SoundEvents.ENTITY_VILLAGER_TRADE, 1.0f);
			return count > 0;
		});

		// ---------------- Monje ----------------
		register("chi_palm", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 5);
			if (target == null) return false;
			Vec3d dir = Targeting.horizontalFacing(p);
			target.damage(p.getDamageSources().playerAttack(p), 4 + 2 * r);
			target.takeKnockback(1.2 + 0.3 * r, -dir.x, -dir.z);
			Targeting.burst(world(p), ParticleTypes.CLOUD, target, 15, 0.3);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f);
			return true;
		});
		register("meditation", (p, r) -> {
			effect(p, StatusEffects.REGENERATION, 80, 2);
			effect(p, StatusEffects.RESISTANCE, 80, 1);
			effect(p, StatusEffects.SLOWNESS, 80, 3);
			var data = dev.aptitudes.role.RoleManager.get(p);
			data.setEnergy(Math.min(AbilityManager.maxEnergy(p), data.energy() + 20 + 10 * r));
			Targeting.burst(world(p), ParticleTypes.ENCHANT, p, 40, 0.8);
			sound(p, SoundEvents.BLOCK_BEACON_AMBIENT, 1.5f);
			return true;
		});

		// ---------------- Elementalista ----------------
		register("elemental_bolt", (p, r) -> {
			Element element = ElementEffects.activeElement(p);
			if (element == null) return false;
			boolean hit = ElementEffects.bolt(p, element, r);
			if (hit) sound(p, SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.4f);
			return hit;
		});
		register("elemental_burst", (p, r) -> {
			Element element = ElementEffects.activeElement(p);
			if (element == null) return false;
			sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f);
			return ElementEffects.burst(p, element, r);
		});

		// ---------------- Cambiaformas ----------------
		register("transform", (p, r) -> dev.aptitudes.morph.MorphManager.toggle(p));
		register("form_power", (p, r) -> dev.aptitudes.morph.MorphManager.usePower(p, r));

		// Activas C y D de cada rol.
		AbilitiesExtra.registerAll();
		// Roles nuevos (v11): Mago pistolero, Invocador de armas, Cazador, Bardo, Brujo y Domador.
		NewRoles.registerActives();
		// v23: activas de las filas de Maestro y Definitivo (plantillas parametrizadas).
		EvolvedAbilities.registerAll();
		// v67: habilidades nuevas por rol (F2: Bersérker; F3: Mago, Asesino, Herbolista e Ingeniero).
		ExtraKits.registerAll();
	}

	private Abilities() {
	}

	static void register(String id, Ability ability) {
		REGISTRY.put(id, ability);
	}

	@Nullable
	public static Ability get(String id) {
		return REGISTRY.get(id);
	}

	private static ServerWorld world(ServerPlayerEntity player) {
		return player.getServerWorld();
	}

	private static void effect(LivingEntity entity, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		entity.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	private static void sound(ServerPlayerEntity p, RegistryEntry<SoundEvent> sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	/** Quita 1 unidad de {@code item} del inventario. @return false si no había. */
	private static boolean consumeOne(ServerPlayerEntity p, Item item) {
		var inventory = p.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isOf(item)) {
				stack.decrement(1);
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static Vec3d findSpotFor(IronGolemEntity golem, Vec3d feet) {
		for (int dy = 0; dy <= 2; dy++) {
			Vec3d candidate = feet.add(0, dy, 0);
			Box box = golem.getType().getDimensions().getBoxAt(candidate);
			if (golem.getWorld().isSpaceEmpty(golem, box)) return candidate;
		}
		return null;
	}
}
