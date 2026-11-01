package dev.aptitudes.ability;

import dev.aptitudes.role.Element;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Comparator;
import java.util.List;

/**
 * Activas C y D de cada rol (28 habilidades nuevas). Escalan con el rango {@code r}
 * (1-3, hasta 6 con Maestría, evolución y recurso). Se registran en {@link Abilities}.
 */
final class AbilitiesExtra {
	private AbilitiesExtra() {
	}

	static void registerAll() {
		// ---------------- Bersérker ----------------
		// Salto demoledor: saltas hacia delante y, al caer, golpeas todo a tu alrededor.
		Abilities.register("leap_slam", (p, r) -> {
			Vec3d facing = Targeting.horizontalFacing(p);
			push(p, facing.multiply(1.1).add(0, 0.9, 0));
			sound(p, SoundEvents.ENTITY_RAVAGER_STEP, 0.7f);
			AbilityState.schedule(server(p), 16, () -> {
				if (!p.isAlive()) return;
				for (LivingEntity e : Targeting.hostilesAround(p, 3 + 0.5 * r)) {
					e.damage(p.getDamageSources().playerAttack(p), 4 + 2 * r);
					e.addVelocity(0, 0.5, 0);
					e.velocityModified = true;
				}
				ParticleFx.shockwave(server(p), world(p), p.getPos(), 3 + 0.5 * r, ParticleTypes.CRIT, 6);
				world(p).spawnParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 3, 0.5, 0.1, 0.5, 0);
				sound(p, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.2f);
			});
			return true;
		});
		// Torbellino: 3 giros en 1,5 s que golpean a todo lo cercano.
		Abilities.register("whirlwind", (p, r) -> {
			AbilityState.repeat(server(p), 3, 10, i -> {
				if (!p.isAlive()) return;
				for (LivingEntity e : Targeting.hostilesAround(p, 3.2)) {
					e.damage(p.getDamageSources().playerAttack(p), 2 + r);
					e.takeKnockback(0.3, p.getX() - e.getX(), p.getZ() - e.getZ());
				}
				ParticleFx.ring(world(p), p.getPos().add(0, 1, 0), 2.6, ParticleTypes.SWEEP_ATTACK, 10);
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f + 0.15f * i);
			});
			return true;
		});

		// ---------------- Mago ----------------
		// Distorsión temporal: el tiempo se frena para los enemigos y se acelera para ti.
		Abilities.register("time_warp", (p, r) -> {
			double radius = 4 + r;
			for (LivingEntity e : Targeting.hostilesAround(p, radius)) {
				e.damage(magic(p), 2 + r);
				effect(e, StatusEffects.SLOWNESS, 60 + 20 * r, 3);
				effect(e, StatusEffects.MINING_FATIGUE, 60 + 20 * r, 2);
			}
			effect(p, StatusEffects.SPEED, 60 + 20 * r, 1);
			effect(p, StatusEffects.HASTE, 60 + 20 * r, 1);
			ParticleFx.shockwave(server(p), world(p), p.getPos(), radius, ParticleFx.dust(0x80DEEA, 1.2f), 8);
			ParticleFx.runeCircle(world(p), p.getPos(), radius * 0.6, ParticleTypes.ENCHANT);
			sound(p, SoundEvents.BLOCK_BELL_RESONATE, 1.6f);
			return true;
		});
		// Misiles arcanos: 3 + r proyectiles teledirigidos a los enemigos más cercanos.
		Abilities.register("arcane_missiles", (p, r) -> {
			List<LivingEntity> targets = nearest(p, 16, 3 + r);
			if (targets.isEmpty()) return false;
			AbilityState.repeat(server(p), 3 + r, 4, i -> {
				LivingEntity t = targets.get(i % targets.size());
				if (!t.isAlive()) return;
				Targeting.particleLine(world(p), ParticleFx.dust(0xB388FF, 1.0f), p.getEyePos(),
						t.getPos().add(0, t.getHeight() / 2, 0));
				t.damage(magic(p), 3.0f);
				sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 1.6f);
			});
			return true;
		});

		// ---------------- Asesino ----------------
		// Abanico de cuchillas: daño y veneno a todos los enemigos cercanos.
		Abilities.register("fan_of_knives", (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 5);
			for (LivingEntity e : targets) {
				e.damage(p.getDamageSources().playerAttack(p), 3 + r);
				effect(e, StatusEffects.POISON, 60 + 20 * r, 0);
				Targeting.particleLine(world(p), ParticleTypes.CRIT, p.getEyePos(), e.getEyePos());
			}
			sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 1.6f);
			return !targets.isEmpty();
		});
		// Shuriken: impacto a distancia que marca y ralentiza.
		Abilities.register("shuriken", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 20);
			Vec3d end = target != null ? target.getEyePos() : Targeting.lookEnd(p, 20);
			Targeting.particleLine(world(p), ParticleTypes.CRIT, p.getEyePos(), end);
			sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 1.8f);
			if (target == null) return false;
			target.damage(p.getDamageSources().playerAttack(p), 5 + 2 * r);
			effect(target, StatusEffects.GLOWING, 100, 0);
			effect(target, StatusEffects.SLOWNESS, 60, 1);
			return true;
		});

		// ---------------- Herbolista ----------------
		// Lluvia sanadora: 5 s curando a los jugadores cercanos (y a ti).
		Abilities.register("healing_rain", (p, r) -> {
			Vec3d center = p.getPos();
			AbilityState.repeat(server(p), 5, 20, i -> {
				for (PlayerEntity ally : world(p).getEntitiesByClass(PlayerEntity.class,
						new Box(center, center).expand(5 + r), PlayerEntity::isAlive)) {
					ally.heal(1.0f + 0.5f * r);
				}
				world(p).spawnParticles(ParticleTypes.FALLING_WATER, center.x, center.y + 3, center.z, 40, 3, 0.2, 3, 0);
				ParticleFx.ring(world(p), center.add(0, 0.1, 0), 5 + r, ParticleTypes.HAPPY_VILLAGER, 24);
			});
			sound(p, SoundEvents.WEATHER_RAIN, 1.2f);
			return true;
		});
		// Nube de esporas: veneno y debilidad en área durante 6 s.
		Abilities.register("spore_cloud", (p, r) -> {
			Vec3d center = Targeting.lookEnd(p, 10);
			AbilityState.repeat(server(p), 6, 20, i -> {
				for (LivingEntity e : world(p).getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(3 + r),
						e -> Targeting.isHostileTo(p, e))) {
					effect(e, StatusEffects.POISON, 60, r >= 3 ? 1 : 0);
					effect(e, StatusEffects.WEAKNESS, 60, 0);
				}
				world(p).spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 1, center.z, 50, 2, 1, 2, 0);
			});
			sound(p, SoundEvents.BLOCK_AZALEA_LEAVES_BREAK, 0.6f);
			return true;
		});

		// ---------------- Ingeniero ----------------
		// Garfio: te impulsa hacia el bloque que miras (hasta 24 bloques).
		Abilities.register("grapple", (p, r) -> {
			Vec3d eye = p.getEyePos();
			Vec3d end = eye.add(p.getRotationVec(1.0f).multiply(16 + 4 * r));
			BlockHitResult hit = p.getWorld().raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE, p));
			if (hit.getType() != HitResult.Type.BLOCK) return false;
			Vec3d delta = hit.getPos().subtract(p.getPos());
			push(p, delta.normalize().multiply(Math.min(3.0, 1.0 + delta.length() * 0.12)).add(0, 0.3, 0));
			Targeting.particleLine(world(p), ParticleTypes.CRIT, eye, hit.getPos());
			p.fallDistance = 0;
			sound(p, SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, 0.8f);
			return true;
		});
		// Granada de pulso: explota al segundo donde miras, sin romper bloques.
		Abilities.register("pulse_grenade", (p, r) -> {
			Vec3d point = Targeting.lookEnd(p, 16);
			Targeting.particleLine(world(p), ParticleTypes.SMOKE, p.getEyePos(), point);
			sound(p, SoundEvents.ENTITY_SNOWBALL_THROW, 0.6f);
			AbilityState.schedule(server(p), 20, () -> {
				for (LivingEntity e : world(p).getEntitiesByClass(LivingEntity.class, new Box(point, point).expand(3.5),
						e -> Targeting.isHostileTo(p, e))) {
					e.damage(p.getDamageSources().explosion(p, p), 6 + 2 * r);
					Vec3d away = e.getPos().subtract(point).normalize();
					e.addVelocity(away.x * 0.8, 0.5, away.z * 0.8);
					e.velocityModified = true;
				}
				world(p).spawnParticles(ParticleTypes.EXPLOSION_EMITTER, point.x, point.y, point.z, 1, 0, 0, 0, 0);
				world(p).spawnParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y + 0.5, point.z, 40, 1.5, 1, 1.5, 0.2);
				world(p).playSound(null, point.x, point.y, point.z, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE,
						SoundCategory.PLAYERS, 1.0f, 1.3f);
			});
			return true;
		});

		// ---------------- Lancero ----------------
		// Salto del dragón: saltas muy alto y caes en picado sobre los enemigos.
		Abilities.register("dragon_leap", (p, r) -> {
			push(p, new Vec3d(0, 1.4, 0));
			sound(p, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.2f);
			AbilityState.schedule(server(p), 14, () -> {
				if (!p.isAlive()) return;
				push(p, Targeting.horizontalFacing(p).multiply(0.6).add(0, -2.2, 0));
			});
			AbilityState.schedule(server(p), 24, () -> {
				if (!p.isAlive()) return;
				p.fallDistance = 0;
				for (LivingEntity e : Targeting.hostilesAround(p, 3.5)) {
					e.damage(p.getDamageSources().playerAttack(p), 5 + 2 * r);
					effect(e, StatusEffects.SLOWNESS, 40, 2);
				}
				ParticleFx.shockwave(server(p), world(p), p.getPos(), 3.5, ParticleTypes.DRAGON_BREATH, 6);
				sound(p, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.8f);
			});
			return true;
		});
		// Estocada perforante: atraviesa a todos los enemigos en línea recta (8 bloques).
		Abilities.register("piercing_thrust", (p, r) -> {
			Vec3d start = p.getEyePos();
			Vec3d end = Targeting.lookEnd(p, 8);
			Box path = new Box(start, end).expand(0.8);
			Vec3d dir = end.subtract(start).normalize();
			int hits = 0;
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, path, e -> Targeting.isHostileTo(p, e))) {
				Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0).subtract(start);
				double along = to.dotProduct(dir);
				if (along < 0 || to.subtract(dir.multiply(along)).lengthSquared() > 1.5) continue;
				e.damage(p.getDamageSources().playerAttack(p), 6 + 2 * r);
				e.takeKnockback(0.4, -dir.x, -dir.z);
				hits++;
			}
			Targeting.particleLine(world(p), ParticleTypes.SWEEP_ATTACK, start, end);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.8f);
			return hits > 0;
		});

		// ---------------- Espadachín ----------------
		// Iaido: te desplazas como un rayo y cortas a todo lo que atraviesas.
		Abilities.register("iaido", (p, r) -> {
			Vec3d facing = Targeting.horizontalFacing(p);
			Box path = p.getBoundingBox().stretch(facing.multiply(6)).expand(0.6);
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, path, e -> Targeting.isHostileTo(p, e))) {
				e.damage(p.getDamageSources().playerAttack(p), 5 + 2 * r);
				world(p).spawnParticles(ParticleTypes.SWEEP_ATTACK, e.getX(), e.getBodyY(0.5), e.getZ(), 1, 0, 0, 0, 0);
			}
			push(p, facing.multiply(2.0).add(0, 0.1, 0));
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.6f);
			return true;
		});
		// Mil cortes: 5 golpes rápidos al objetivo.
		Abilities.register("thousand_cuts", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 6);
			if (target == null) return false;
			AbilityState.repeat(server(p), 5, 4, i -> {
				if (!target.isAlive()) return;
				target.timeUntilRegen = 0; // ignora la invulnerabilidad entre golpes
				target.damage(p.getDamageSources().playerAttack(p), 2 + r);
				world(p).spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getBodyY(0.5), target.getZ(),
						1, 0.2, 0.2, 0.2, 0);
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f + 0.1f * i);
			});
			return true;
		});

		// ---------------- Paladín ----------------
		// Consagración: suelo sagrado 6 s que daña enemigos (doble a no-muertos) y cura aliados.
		Abilities.register("consecration", (p, r) -> {
			Vec3d center = p.getPos();
			double radius = 4 + 0.5 * r;
			AbilityState.repeat(server(p), 6, 20, i -> {
				for (LivingEntity e : world(p).getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(radius),
						LivingEntity::isAlive)) {
					if (e instanceof PlayerEntity) e.heal(1.0f);
					else if (Targeting.isHostileTo(p, e)) {
						e.damage(magic(p), (e.getType().isIn(EntityTypeTags.UNDEAD) ? 2 : 1) * (1.5f + 0.5f * r));
					}
				}
				ParticleFx.runeCircle(world(p), center, radius, ParticleFx.dust(0xFFD54F, 1.0f));
			});
			sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.4f);
			return true;
		});
		// Martillo de la justicia: aturde al objetivo (3 s) y lo golpea.
		Abilities.register("hammer_of_justice", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 16);
			Vec3d end = target != null ? target.getEyePos() : Targeting.lookEnd(p, 16);
			Targeting.particleLine(world(p), ParticleFx.dust(0xFFE082, 1.3f), p.getEyePos(), end);
			if (target == null) return false;
			target.damage(magic(p), 6 + 2 * r);
			effect(target, StatusEffects.SLOWNESS, 60, 6);
			effect(target, StatusEffects.WEAKNESS, 60, 2);
			world(p).spawnParticles(ParticleTypes.FLASH, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0, 0, 0, 0);
			sound(p, SoundEvents.BLOCK_ANVIL_LAND, 1.2f);
			return true;
		});

		// ---------------- Nigromante ----------------
		// Lanza de hueso: atraviesa en línea y aplica Descomposición.
		Abilities.register("bone_spear", (p, r) -> {
			Vec3d start = p.getEyePos();
			Vec3d end = Targeting.lookEnd(p, 16);
			Vec3d dir = end.subtract(start).normalize();
			int hits = 0;
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(0.8),
					e -> Targeting.isHostileTo(p, e))) {
				Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0).subtract(start);
				double along = to.dotProduct(dir);
				if (along < 0 || to.subtract(dir.multiply(along)).lengthSquared() > 1.5) continue;
				e.damage(magic(p), 5 + 2 * r);
				effect(e, StatusEffects.WITHER, 60, 0);
				hits++;
			}
			Targeting.particleLine(world(p), ParticleFx.dust(0xE0E0D0, 1.2f), start, end);
			sound(p, SoundEvents.ENTITY_SKELETON_SHOOT, 0.6f);
			return hits > 0;
		});
		// Sifón de almas: drena a todos los enemigos cercanos y te cura la mitad.
		Abilities.register("soul_siphon", (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 5 + r);
			float total = 0;
			for (LivingEntity e : targets) {
				float dmg = 2 + r;
				e.damage(magic(p), dmg);
				total += dmg;
				Targeting.particleLine(world(p), ParticleTypes.SOUL, e.getEyePos(), p.getEyePos());
			}
			p.heal(total * 0.5f);
			sound(p, SoundEvents.ENTITY_VEX_CHARGE, 0.6f);
			return !targets.isEmpty();
		});

		// ---------------- Druida ----------------
		// Manada: invoca 2 (3 en rango 3+) lobos aliados durante 40 s.
		Abilities.register("summon_wolves", (p, r) -> {
			int count = r >= 3 ? 3 : 2;
			for (int i = 0; i < count; i++) {
				WolfEntity wolf = EntityType.WOLF.create(p.getWorld());
				if (wolf == null) continue;
				Vec3d side = Targeting.horizontalFacing(p).rotateY((float) Math.toRadians(-60 + 60 * i));
				Vec3d pos = Targeting.findFreeSpot(p, p.getPos().add(side.multiply(1.5)));
				wolf.refreshPositionAndAngles(pos.x, pos.y, pos.z, p.getYaw(), 0);
				wolf.setTamed(true, true);
				wolf.setOwner(p);
				wolf.setHealth(wolf.getMaxHealth());
				wolf.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 800, r >= 2 ? 1 : 0, true, false));
				AbilityState.trackSummon(wolf, p, 800); // registrar ANTES de aparecer
				p.getWorld().spawnEntity(wolf);
				world(p).spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 0.5, pos.z, 10, 0.3, 0.3, 0.3, 0);
			}
			sound(p, SoundEvents.ENTITY_WOLF_HOWL, 1.0f);
			return true;
		});
		// Tormenta de espinas: daño y lentitud en un radio de 6.
		Abilities.register("thorn_storm", (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 6);
			for (LivingEntity e : targets) {
				e.damage(p.getDamageSources().cactus(), 3 + r);
				effect(e, StatusEffects.SLOWNESS, 80, 1);
			}
			ParticleFx.spiral(world(p), p.getPos(), 3, 2.5, ParticleFx.dust(0x6E8B3D, 1.2f), 60, 3);
			sound(p, SoundEvents.BLOCK_SWEET_BERRY_BUSH_BREAK, 0.7f);
			return !targets.isEmpty();
		});

		// ---------------- Mercader ----------------
		// Lanzamiento de moneda: gasta 1 esmeralda y la dispara con fuerza.
		Abilities.register("coin_toss", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 20);
			if (target == null) return false;
			if (!p.isCreative() && !consume(p, Items.EMERALD, 1)) return false;
			Targeting.particleLine(world(p), ParticleFx.dust(0x50E080, 1.0f), p.getEyePos(), target.getEyePos());
			target.damage(p.getDamageSources().playerAttack(p), 6 + 3 * r);
			sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f);
			return true;
		});
		// Guardia dorada: gasta 2 esmeraldas; Absorción II y Resistencia I.
		Abilities.register("golden_guard", (p, r) -> {
			if (!p.isCreative() && !consume(p, Items.EMERALD, 2)) return false;
			effect(p, StatusEffects.ABSORPTION, 200 + 40 * r, 1);
			effect(p, StatusEffects.RESISTANCE, 100 + 20 * r, 0);
			ParticleFx.spiral(world(p), p.getPos(), 0.9, 2.2, ParticleFx.dust(0xFFD54F, 1.2f), 40, 2);
			sound(p, SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value(), 1.0f);
			return true;
		});

		// ---------------- Monje ----------------
		// Patada voladora: te lanzas y el primer enemigo sale despedido.
		Abilities.register("flying_kick", (p, r) -> {
			Vec3d facing = Targeting.horizontalFacing(p);
			push(p, facing.multiply(1.5).add(0, 0.25, 0));
			Box path = p.getBoundingBox().stretch(facing.multiply(5)).expand(0.5);
			LivingEntity first = p.getWorld().getEntitiesByClass(LivingEntity.class, path, e -> Targeting.isHostileTo(p, e))
					.stream().min(Comparator.comparingDouble(p::squaredDistanceTo)).orElse(null);
			if (first != null) {
				first.damage(p.getDamageSources().playerAttack(p), 5 + 2 * r);
				first.takeKnockback(1.5 + 0.3 * r, -facing.x, -facing.z);
				world(p).spawnParticles(ParticleTypes.EXPLOSION, first.getX(), first.getBodyY(0.5), first.getZ(), 1, 0, 0, 0, 0);
			}
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f);
			return true;
		});
		// Cien puños: 8 golpes rápidos al objetivo cercano.
		Abilities.register("hundred_fists", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 4);
			if (target == null) return false;
			AbilityState.repeat(server(p), 8, 2, i -> {
				if (!target.isAlive()) return;
				target.timeUntilRegen = 0;
				target.damage(p.getDamageSources().playerAttack(p), 1.5f + 0.5f * r);
				world(p).spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(), 4, 0.3, 0.3, 0.3, 0.2);
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, 1.0f + 0.08f * i);
			});
			return true;
		});

		// ---------------- Elementalista ----------------
		// Ráfaga elemental: 3 proyectiles del elemento activo, uno cada 0,5 s.
		Abilities.register("elemental_barrage", (p, r) -> {
			Element element = ElementEffects.activeElement(p);
			if (element == null) return false;
			AbilityState.repeat(server(p), 3, 10, i -> {
				if (p.isAlive()) ElementEffects.bolt(p, element, Math.max(1, r - 1));
			});
			sound(p, SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.2f);
			return true;
		});
		// Aura elemental: 4 pulsos (cada 1,5 s) del estallido del elemento activo, más débiles.
		Abilities.register("elemental_aura", (p, r) -> {
			Element element = ElementEffects.activeElement(p);
			if (element == null) return false;
			AbilityState.repeat(server(p), 4, 30, i -> {
				if (p.isAlive()) ElementEffects.burst(p, element, Math.max(1, r - 2));
			});
			ParticleFx.spiral(world(p), p.getPos(), 1.2, 2.5, element.particle(), 50, 3);
			sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.2f);
			return true;
		});

		// ---------------- Cambiaformas ----------------
		// Abalanzarse: saltas hacia el objetivo y lo golpeas al llegar.
		Abilities.register("pounce", (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 14);
			if (target == null) return false;
			Vec3d delta = target.getPos().subtract(p.getPos());
			push(p, delta.normalize().multiply(Math.min(2.2, 0.8 + delta.length() * 0.12)).add(0, 0.35, 0));
			AbilityState.schedule(server(p), 8, () -> {
				if (target.isAlive() && p.squaredDistanceTo(target) < 9) {
					target.damage(p.getDamageSources().playerAttack(p), 4 + 2 * r);
					effect(target, StatusEffects.SLOWNESS, 40, 1);
				}
			});
			sound(p, SoundEvents.ENTITY_CAT_HISS, 0.7f);
			return true;
		});
		// Aullido de la bestia: daño, debilidad y empuje a los enemigos cercanos.
		Abilities.register("beast_howl", (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 6);
			for (LivingEntity e : targets) {
				e.damage(magic(p), 3 + r);
				effect(e, StatusEffects.WEAKNESS, 100, 0);
				e.takeKnockback(1.0, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			ParticleFx.shockwave(server(p), world(p), p.getPos(), 6, ParticleTypes.SONIC_BOOM, 3);
			sound(p, SoundEvents.ENTITY_WOLF_GROWL, 0.6f);
			return true;
		});
	}

	// ------------------------------------------------------------ Utilidades

	private static ServerWorld world(ServerPlayerEntity p) {
		return p.getServerWorld();
	}

	private static MinecraftServer server(ServerPlayerEntity p) {
		return p.getServer();
	}

	private static DamageSource magic(ServerPlayerEntity p) {
		return p.getDamageSources().indirectMagic(p, p);
	}

	private static void push(ServerPlayerEntity p, Vec3d velocity) {
		p.setVelocity(velocity);
		p.velocityModified = true;
	}

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	/** Los {@code max} enemigos más cercanos en {@code radius}. */
	private static List<LivingEntity> nearest(ServerPlayerEntity p, double radius, int max) {
		return Targeting.hostilesAround(p, radius).stream()
				.sorted(Comparator.comparingDouble(p::squaredDistanceTo)).limit(max).toList();
	}

	/** Gasta {@code count} unidades de {@code item}; todo o nada. */
	static boolean consume(ServerPlayerEntity p, net.minecraft.item.Item item, int count) {
		var inv = p.getInventory();
		if (inv.count(item) < count) return false;
		int left = count;
		for (int i = 0; i < inv.size() && left > 0; i++) {
			ItemStack stack = inv.getStack(i);
			if (!stack.isOf(item)) continue;
			int take = Math.min(left, stack.getCount());
			stack.decrement(take);
			left -= take;
		}
		return true;
	}
}
