package dev.aptitudes.ability;

import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Habilidades definitivas (una por rol, requieren evolución Definitiva).
 * Las que duran varios segundos se implementan con tareas programadas
 * ({@link AbilityState#repeat}); cada fase comprueba que el lanzador siga vivo.
 * El parámetro {@code r} es la potencia (3 + nivel de evolución = 5).
 */
public final class Ultimates {
	/** Partícula de "sangre" (polvo rojo oscuro) de las artes de sangre. */
	public static final DustParticleEffect BLOOD = new DustParticleEffect(new Vector3f(0.55f, 0.0f, 0.05f), 1.3f);
	private static final Map<Role, Ability> REGISTRY = new EnumMap<>(Role.class);

	static {
		// Bersérker – Ira imparable: no puede morir durante 8 s; Fuerza II, Velocidad II, Resistencia.
		REGISTRY.put(Role.BERSERKER, (p, r) -> {
			int ticks = 8 * 20;
			AbilityState.setUndying(p, ticks);
			effect(p, StatusEffects.STRENGTH, ticks, 1);
			effect(p, StatusEffects.SPEED, ticks, 1);
			effect(p, StatusEffects.RESISTANCE, ticks, 0);
			Targeting.burst(world(p), ParticleTypes.ANGRY_VILLAGER, p, 30, 1.0);
			sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.6f);
			return true;
		});

		// Mago – Singularidad: un agujero negro atrae y daña a los enemigos durante 5 s.
		REGISTRY.put(Role.MAGE, (p, r) -> {
			Vec3d center = Targeting.lookEnd(p, 24);
			ServerWorld world = world(p);
			sound(p, SoundEvents.BLOCK_PORTAL_TRIGGER, 0.5f);
			AbilityState.repeat(server(p), 20, 5, i -> {
				if (!p.isAlive()) return;
				world.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 30, 1.5, 1.5, 1.5, 0.1);
				world.spawnParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 8, 0.3, 0.3, 0.3, 0.0);
				for (LivingEntity e : hostilesNear(p, center, 9)) {
					Vec3d pull = center.subtract(e.getPos()).normalize().multiply(0.35);
					e.addVelocity(pull.x, pull.y * 0.5 + 0.05, pull.z);
					e.velocityModified = true;
					if (i % 4 == 0) e.damage(p.getDamageSources().indirectMagic(p, p), 1.0f + r * 0.4f);
				}
			});
			return true;
		});

		// Asesino – Ejecución sombría: salta tras hasta 5 enemigos; los que tienen menos del 30 % de vida mueren.
		REGISTRY.put(Role.ASSASSIN, (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 16).stream()
					.sorted(Comparator.comparingDouble(p::squaredDistanceTo)).limit(5).toList();
			if (targets.isEmpty()) return false;
			AbilityState.setUntouchable(p, targets.size() * 6 + 10);
			AbilityState.repeat(server(p), targets.size(), 6, i -> {
				LivingEntity target = targets.get(i);
				if (!p.isAlive() || !target.isAlive()) return;
				Vec3d behind = target.getPos().subtract(Targeting.horizontalFacing(target).multiply(1.2));
				Vec3d spot = Targeting.findFreeSpot(p, behind);
				if (spot != null) p.networkHandler.requestTeleport(spot.x, spot.y, spot.z, target.getYaw(), 10.0f);
				boolean execute = target.getHealth() < target.getMaxHealth() * 0.3f;
				target.damage(p.getDamageSources().playerAttack(p), execute ? 1000.0f : 6.0f + r);
				Targeting.burst(world(p), ParticleTypes.LARGE_SMOKE, target, 12, 0.4);
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 0.7f);
			});
			return true;
		});

		// Herbolista – Jardín del Edén: 10 s de zona que regenera y purifica aliados y envenena enemigos.
		REGISTRY.put(Role.HERBALIST, (p, r) -> {
			Vec3d center = p.getPos();
			ServerWorld world = world(p);
			AbilityState.repeat(server(p), 10, 20, i -> {
				world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y + 0.5, center.z, 40, 4, 0.5, 4, 0);
				world.spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 2, center.z, 20, 4, 1, 4, 0);
				for (PlayerEntity ally : world.getEntitiesByClass(PlayerEntity.class,
						new net.minecraft.util.math.Box(center, center).expand(8), PlayerEntity::isAlive)) {
					quiet(ally, StatusEffects.REGENERATION, 40, 1);
					ally.getStatusEffects().stream().filter(e -> !e.getEffectType().value().isBeneficial())
							.map(StatusEffectInstance::getEffectType).toList().forEach(ally::removeStatusEffect);
				}
				for (LivingEntity e : hostilesNear(p, center, 8)) effect(e, StatusEffects.POISON, 40, 1);
			});
			sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f);
			return true;
		});

		// Ingeniero – Bombardeo orbital: 12 impactos en 3 s alrededor del punto señalado (no rompen bloques).
		REGISTRY.put(Role.ENGINEER, (p, r) -> {
			Vec3d center = Targeting.lookEnd(p, 40);
			ServerWorld world = world(p);
			sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 0.5f);
			AbilityState.repeat(server(p), 12, 5, i -> {
				double x = center.x + (world.random.nextDouble() - 0.5) * 8;
				double z = center.z + (world.random.nextDouble() - 0.5) * 8;
				world.spawnParticles(ParticleTypes.END_ROD, x, center.y + 6, z, 15, 0.1, 3, 0.1, 0);
				world.createExplosion(p, x, center.y, z, 2.5f, World.ExplosionSourceType.NONE);
			});
			return true;
		});

		// Lancero – Lanza celestial: salta muy alto y cae en picado; onda de choque al aterrizar.
		REGISTRY.put(Role.LANCER, (p, r) -> {
			p.setVelocity(p.getVelocity().x, 1.6, p.getVelocity().z);
			p.velocityModified = true;
			AbilityState.setUndying(p, 60);
			sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.6f);
			AbilityState.schedule(server(p), 18, () -> {
				if (!p.isAlive()) return;
				p.setVelocity(0, -3.0, 0);
				p.velocityModified = true;
			});
			AbilityState.schedule(server(p), 26, () -> {
				if (!p.isAlive()) return;
				p.fallDistance = 0;
				for (LivingEntity e : Targeting.hostilesAround(p, 6)) {
					e.damage(p.getDamageSources().playerAttack(p), 8.0f + r * 1.5f);
					e.takeKnockback(1.5, p.getX() - e.getX(), p.getZ() - e.getZ());
				}
				world(p).spawnParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 6, 2, 0.2, 2, 0);
				sound(p, SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 0.5f);
			});
			return true;
		});

		// Espadachín – Mil cortes: 15 tajos en 3 s al enemigo más cercano.
		REGISTRY.put(Role.SWORDSMAN, (p, r) -> {
			if (Targeting.hostilesAround(p, 6).isEmpty()) return false;
			AbilityState.repeat(server(p), 15, 4, i -> {
				if (!p.isAlive()) return;
				Targeting.hostilesAround(p, 6).stream().min(Comparator.comparingDouble(p::squaredDistanceTo)).ifPresent(e -> {
					e.timeUntilRegen = 0; // ignora la invulnerabilidad breve tras cada golpe
					e.damage(p.getDamageSources().playerAttack(p), 2.0f + r * 0.4f);
					world(p).spawnParticles(ParticleTypes.SWEEP_ATTACK, e.getX(), e.getBodyY(0.5), e.getZ(), 1, 0.2, 0.2, 0.2, 0);
				});
				if (i % 3 == 0) sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.6f);
			});
			return true;
		});

		// Paladín – Reino eterno: 8 s de zona donde los aliados no pueden morir; los no-muertos arden.
		REGISTRY.put(Role.PALADIN, (p, r) -> {
			Vec3d center = p.getPos();
			ServerWorld world = world(p);
			sound(p, SoundEvents.BLOCK_BELL_RESONATE, 1.0f);
			AbilityState.repeat(server(p), 8, 20, i -> {
				world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 0.2, center.z, 50, 6, 0.1, 6, 0.01);
				for (PlayerEntity ally : world.getEntitiesByClass(PlayerEntity.class,
						new net.minecraft.util.math.Box(center, center).expand(8), PlayerEntity::isAlive)) {
					if (ally instanceof ServerPlayerEntity sp) AbilityState.setUndying(sp, 25);
					quiet(ally, StatusEffects.RESISTANCE, 30, 1);
				}
				for (LivingEntity e : hostilesNear(p, center, 8)) {
					if (e.getType().isIn(EntityTypeTags.UNDEAD)) {
						e.damage(p.getDamageSources().indirectMagic(p, p), 4.0f);
						e.setOnFireFor(2);
					}
				}
			});
			return true;
		});

		// Nigromante – Marea carmesí: drena a todos los enemigos en 10 bloques, completa tu ejército y lo enfurece.
		REGISTRY.put(Role.NECROMANCER, (p, r) -> {
			float total = 0;
			for (LivingEntity e : Targeting.hostilesAround(p, 10)) {
				e.damage(p.getDamageSources().indirectMagic(p, p), 6.0f);
				effect(e, StatusEffects.WITHER, 100, 1);
				Targeting.particleLine(world(p), BLOOD, e.getPos().add(0, 1, 0), p.getPos().add(0, 1, 0));
				total += 6.0f;
			}
			p.heal(total * 0.5f);
			int missing = Minions.MAX_MINIONS - AbilityState.minionsOf(p).size();
			for (int i = 0; i < missing; i++) {
				Minions.spawn(p, i % 2 == 0 ? EntityType.ZOMBIE : EntityType.SKELETON, i);
			}
			for (MobEntity minion : AbilityState.minionsOf(p)) {
				effect(minion, StatusEffects.STRENGTH, 400, 1);
				effect(minion, StatusEffects.SPEED, 400, 0);
			}
			world(p).spawnParticles(BLOOD, p.getX(), p.getBodyY(0.5), p.getZ(), 120, 5, 1, 5, 0);
			sound(p, SoundEvents.ENTITY_WITHER_SPAWN, 0.8f);
			return true;
		});

		// Druida – Furia de la naturaleza: 3 lobos aliados (30 s) y raíces en área.
		REGISTRY.put(Role.DRUID, (p, r) -> {
			for (int i = 0; i < 3; i++) {
				WolfEntity wolf = EntityType.WOLF.create(p.getWorld());
				if (wolf == null) continue;
				Vec3d pos = p.getPos().add(Targeting.horizontalFacing(p).rotateY((float) Math.toRadians(-60 + 60 * i)).multiply(2));
				wolf.refreshPositionAndAngles(pos.x, pos.y, pos.z, p.getYaw(), 0);
				wolf.setTamed(true, true);
				wolf.setOwner(p);
				wolf.setCustomName(Text.translatable("entity.aptitudes.spirit_wolf"));
				effect(wolf, StatusEffects.STRENGTH, 600, 1);
				effect(wolf, StatusEffects.RESISTANCE, 600, 0);
				AbilityState.trackSummon(wolf, p, 30 * 20);
				p.getWorld().spawnEntity(wolf);
			}
			for (LivingEntity e : Targeting.hostilesAround(p, 8)) effect(e, StatusEffects.SLOWNESS, 100, 4);
			Targeting.burst(world(p), ParticleTypes.COMPOSTER, p, 60, 3);
			sound(p, SoundEvents.ENTITY_WOLF_HOWL, 1.0f);
			return true;
		});

		// Mercader – Casino de la fortuna: tirada de la ruleta con resultados ponderados.
		REGISTRY.put(Role.MERCHANT, (p, r) -> {
			int roll = p.getRandom().nextInt(100);
			String result;
			if (roll < 8) { // 8 %: premio gordo
				p.getInventory().offerOrDrop(new ItemStack(Items.EMERALD, 8));
				effect(p, StatusEffects.LUCK, 2400, 2);
				effect(p, StatusEffects.HERO_OF_THE_VILLAGE, 2400, 4);
				result = "jackpot";
			} else if (roll < 30) { // 22 %: furia del jugador
				effect(p, StatusEffects.STRENGTH, 400, 1);
				effect(p, StatusEffects.SPEED, 400, 1);
				result = "power";
			} else if (roll < 50) { // 20 %: curación total
				p.setHealth(p.getMaxHealth());
				p.getHungerManager().add(20, 20.0f);
				result = "heal";
			} else if (roll < 70) { // 20 %: lluvia de oro
				p.getInventory().offerOrDrop(new ItemStack(Items.GOLD_INGOT, 4 + p.getRandom().nextInt(5)));
				result = "gold";
			} else if (roll < 90) { // 20 %: maldición a los enemigos
				for (LivingEntity e : Targeting.hostilesAround(p, 12)) {
					effect(e, StatusEffects.WEAKNESS, 300, 1);
					effect(e, StatusEffects.SLOWNESS, 300, 1);
					effect(e, StatusEffects.GLOWING, 300, 0);
				}
				result = "curse";
			} else { // 10 %: la casa gana
				effect(p, StatusEffects.SLOWNESS, 200, 1);
				effect(p, StatusEffects.HUNGER, 200, 1);
				result = "house";
			}
			p.sendMessage(Text.translatable("aptitudes.casino." + result).formatted(Formatting.GOLD), false);
			Targeting.burst(world(p), ParticleTypes.TOTEM_OF_UNDYING, p, 40, 0.8);
			sound(p, SoundEvents.ENTITY_PLAYER_LEVELUP, result.equals("house") ? 0.5f : 1.5f);
			return true;
		});

		// Monje – Nirvana: 10 s intocable por criaturas, Velocidad III y Salto.
		REGISTRY.put(Role.MONK, (p, r) -> {
			int ticks = 10 * 20;
			AbilityState.setUntouchable(p, ticks);
			effect(p, StatusEffects.SPEED, ticks, 2);
			effect(p, StatusEffects.JUMP_BOOST, ticks, 1);
			effect(p, StatusEffects.STRENGTH, ticks, 1);
			Targeting.burst(world(p), ParticleTypes.END_ROD, p, 50, 1.0);
			sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.8f);
			return true;
		});

		// Cambiaformas – Forma primordial: te conviertes en Warden durante 20 s (gratis, sin gasto de energía).
		REGISTRY.put(Role.SHAPESHIFTER, (p, r) -> {
			dev.aptitudes.morph.MorphManager.morph(p, EntityType.WARDEN, 20 * 20, 20.0f);
			p.heal(20.0f);
			sound(p, SoundEvents.ENTITY_WARDEN_EMERGE, 1.0f);
			return true;
		});

		// Elementalista – Convergencia elemental: estallido de todos los elementos dominados a la vez.
		REGISTRY.put(Role.ELEMENTALIST, (p, r) -> {
			List<Element> elements = RoleManager.get(p).elements();
			if (elements.isEmpty()) return false;
			for (Element element : elements) ElementEffects.burst(p, element, r);
			sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f);
			return true;
		});
	}

	private Ultimates() {
	}

	@Nullable
	public static Ability get(Role role) {
		return REGISTRY.get(role);
	}

	private static List<LivingEntity> hostilesNear(ServerPlayerEntity p, Vec3d center, double radius) {
		double r2 = radius * radius;
		return p.getWorld().getEntitiesByClass(LivingEntity.class, new net.minecraft.util.math.Box(center, center).expand(radius),
				e -> Targeting.isHostileTo(p, e) && e.squaredDistanceTo(center) <= r2);
	}

	private static ServerWorld world(ServerPlayerEntity p) {
		return p.getServerWorld();
	}

	private static MinecraftServer server(ServerPlayerEntity p) {
		return p.getServer();
	}

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void quiet(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	private static void sound(ServerPlayerEntity p, RegistryEntry<SoundEvent> sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}
}
