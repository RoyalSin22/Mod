package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Comportamiento de cada elemento en los cuatro nodos del Elementalista:
 * <ul>
 *   <li><b>Proyectil</b> (activa A): impacto a distancia con el efecto del elemento.</li>
 *   <li><b>Estallido</b> (activa B): efecto en área alrededor del jugador.</li>
 *   <li><b>Pasiva</b>: la del elemento activo usa el rango de <i>Afinidad</i>; las de los
 *       demás elementos dominados usan el rango de <i>Dominio</i>.</li>
 * </ul>
 */
public final class ElementEffects {
	private ElementEffects() {
	}

	// ------------------------------------------------------------- Proyectil

	public static boolean bolt(ServerPlayerEntity p, Element element, int r) {
		LivingEntity target = Targeting.raycastLiving(p, 24);
		Vec3d end = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.lookEnd(p, 24);
		Targeting.particleLine(p.getServerWorld(), element.particle(), p.getEyePos(), end);
		if (target == null) return false;
		float damage = 5 + r;
		DamageSource source = p.getDamageSources().indirectMagic(p, p);

		switch (element) {
			case FIRE -> {
				target.damage(source, damage);
				target.setOnFireFor(3 + r);
			}
			case ICE -> {
				target.damage(source, damage);
				effect(target, StatusEffects.SLOWNESS, 60, r - 1);
				target.setFrozenTicks(target.getMinFreezeDamageTicks() + 40);
			}
			case LIGHTNING -> {
				target.damage(source, damage * 1.2f);
				// Encadena a r enemigos cercanos al objetivo con el 60 % del daño.
				List<LivingEntity> chain = p.getWorld().getEntitiesByClass(LivingEntity.class,
								target.getBoundingBox().expand(5), e -> e != target && Targeting.isHostileTo(p, e))
						.stream().sorted(Comparator.comparingDouble(target::squaredDistanceTo)).limit(r).toList();
				Vec3d from = end;
				for (LivingEntity next : chain) {
					Vec3d to = next.getPos().add(0, next.getHeight() / 2, 0);
					Targeting.particleLine(p.getServerWorld(), element.particle(), from, to);
					next.damage(source, damage * 0.6f);
					from = to;
				}
			}
			case EARTH -> {
				target.damage(source, damage);
				launch(target, 0.5 + 0.15 * r);
			}
			case WIND -> {
				target.damage(source, damage * 0.8f);
				Vec3d dir = Targeting.horizontalFacing(p);
				target.takeKnockback(1.5 + 0.3 * r, -dir.x, -dir.z);
			}
			case WATER -> {
				target.damage(source, damage);
				target.extinguish();
				p.heal(1 + r);
			}
			case PLANT -> {
				target.damage(source, damage * 0.8f);
				effect(target, StatusEffects.POISON, 100, r - 1);
			}
			case MAGMA -> {
				target.damage(source, damage * 1.1f);
				target.setOnFireFor(2);
				Vec3d dir = Targeting.horizontalFacing(p);
				target.takeKnockback(0.5, -dir.x, -dir.z);
			}
			case SHADOW -> {
				target.damage(source, damage);
				effect(target, StatusEffects.WITHER, (2 + r) * 20, 0);
				effect(target, StatusEffects.BLINDNESS, 40, 0);
			}
			case LIGHT -> {
				// Doble contra no-muertos; marca al objetivo.
				target.damage(source, target.getType().isIn(EntityTypeTags.UNDEAD) ? damage * 2 : damage);
				effect(target, StatusEffects.GLOWING, 100, 0);
			}
			case VOID -> {
				// Atrae al objetivo hacia ti.
				target.damage(source, damage * 1.1f);
				Vec3d pull = p.getPos().subtract(target.getPos()).normalize().multiply(0.9 + 0.15 * r);
				target.addVelocity(pull.x, 0.25, pull.z);
				target.velocityModified = true;
			}
			case POISON -> {
				target.damage(source, damage * 0.7f);
				effect(target, StatusEffects.POISON, 100, r >= 3 ? 1 : 0);
				effect(target, StatusEffects.NAUSEA, 80, 0);
			}
			case CRYSTAL -> {
				// Fragmentos: más daño y ralentiza.
				target.damage(source, damage * 1.3f);
				effect(target, StatusEffects.SLOWNESS, 40, 1);
			}
			case SOUND -> {
				// Onda sónica: empuja y aturde.
				target.damage(source, damage * 0.9f);
				Vec3d dir = Targeting.horizontalFacing(p);
				target.takeKnockback(1.2 + 0.2 * r, -dir.x, -dir.z);
				effect(target, StatusEffects.NAUSEA, 60, 0);
				effect(target, StatusEffects.SLOWNESS, 30, 2);
			}
		}
		Targeting.burst(p.getServerWorld(), element.particle(), target, 10, 0.4);
		return true;
	}

	// ------------------------------------------------------------- Estallido

	public static boolean burst(ServerPlayerEntity p, Element element, int r) {
		ServerWorld world = p.getServerWorld();
		double radius = 4 + r;
		float damage = 3 + r;
		DamageSource source = p.getDamageSources().indirectMagic(p, p);
		List<LivingEntity> hostiles = Targeting.hostilesAround(p, radius);
		world.spawnParticles(element.particle(), p.getX(), p.getBodyY(0.5), p.getZ(), 60, radius / 2, 0.5, radius / 2, 0.05);

		switch (element) {
			case FIRE -> hostiles.forEach(e -> { e.damage(source, damage); e.setOnFireFor(4); });
			case ICE -> hostiles.forEach(e -> {
				e.damage(source, damage);
				effect(e, StatusEffects.SLOWNESS, 80, 1 + r / 2);
				e.setFrozenTicks(e.getMinFreezeDamageTicks() + 60);
			});
			case LIGHTNING -> {
				// Rayos solo visuales (no prenden fuego); el daño lo aplicamos nosotros.
				hostiles.stream().sorted(Comparator.comparingDouble(p::squaredDistanceTo)).limit(1 + r).forEach(e -> {
					LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
					if (bolt != null) {
						bolt.refreshPositionAfterTeleport(e.getPos());
						bolt.setCosmetic(true);
						world.spawnEntity(bolt);
					}
					e.damage(source, damage * 1.5f);
				});
			}
			case EARTH -> hostiles.forEach(e -> {
				e.damage(source, damage);
				launch(e, 0.7 + 0.1 * r);
				effect(e, StatusEffects.SLOWNESS, 60, 1);
			});
			case WIND -> {
				hostiles.forEach(e -> {
					e.takeKnockback(2.0, p.getX() - e.getX(), p.getZ() - e.getZ());
					effect(e, StatusEffects.LEVITATION, 20, 0);
				});
				effect(p, StatusEffects.SPEED, 100, 1);
			}
			case WATER -> {
				for (PlayerEntity ally : Targeting.playersAround(p, radius)) {
					ally.heal(2 + r);
					ally.extinguish();
				}
				hostiles.forEach(e -> {
					e.takeKnockback(1.0, p.getX() - e.getX(), p.getZ() - e.getZ());
					effect(e, StatusEffects.SLOWNESS, 60, 0);
				});
			}
			case PLANT -> hostiles.forEach(e -> {
				effect(e, StatusEffects.SLOWNESS, (2 + r) * 20, 3); // raíces
				effect(e, StatusEffects.POISON, 80, 0);
			});
			case MAGMA -> hostiles.forEach(e -> {
				e.damage(source, damage * 1.3f);
				e.setOnFireFor(3);
			});
			case SHADOW -> {
				hostiles.forEach(e -> {
					effect(e, StatusEffects.BLINDNESS, 80, 0);
					effect(e, StatusEffects.WEAKNESS, 100, 0);
					effect(e, StatusEffects.WITHER, 60, 0);
				});
				effect(p, StatusEffects.INVISIBILITY, 60, 0);
			}
			case LIGHT -> {
				for (PlayerEntity ally : Targeting.playersAround(p, radius)) ally.heal(2 + r);
				hostiles.forEach(e -> {
					e.damage(source, e.getType().isIn(EntityTypeTags.UNDEAD) ? damage * 2 : damage);
					effect(e, StatusEffects.GLOWING, 100, 0);
				});
			}
			case VOID -> hostiles.forEach(e -> {
				Vec3d pull = p.getPos().subtract(e.getPos()).normalize().multiply(0.8);
				e.addVelocity(pull.x, 0.2, pull.z);
				e.velocityModified = true;
				e.damage(source, damage);
				effect(e, StatusEffects.SLOWNESS, 60, 1);
			});
			case POISON -> hostiles.forEach(e -> {
				effect(e, StatusEffects.POISON, 120, 1);
				effect(e, StatusEffects.WEAKNESS, 100, 0);
			});
			case CRYSTAL -> {
				hostiles.forEach(e -> e.damage(source, damage * 1.2f));
				effect(p, StatusEffects.ABSORPTION, 200, Math.max(0, r - 1));
			}
			case SOUND -> hostiles.forEach(e -> {
				e.damage(source, damage);
				e.takeKnockback(1.8, p.getX() - e.getX(), p.getZ() - e.getZ());
				effect(e, StatusEffects.NAUSEA, 100, 0);
			});
		}
		ElementFusions.apply(p, element, r, hostiles);
		return true;
	}

	// ---------------------------------------------------------------- Pasivas

	/** Rango con el que se aplica la pasiva de un elemento (0 = inactiva). */
	public static int passiveRank(ServerPlayerEntity player, Element element) {
		RoleData data = RoleManager.get(player);
		if (!data.masters(element)) return 0;
		return element == data.activeElement()
				? RoleManager.activeRank(player, "elemental_affinity")
				: RoleManager.activeRank(player, "elemental_dominion");
	}

	private record ElementAttribute(Element element, RegistryEntry<EntityAttribute> attribute, Operation op,
									double perRank) {
	}

	private static final List<ElementAttribute> ATTRIBUTES = List.of(
			new ElementAttribute(Element.LIGHTNING, EntityAttributes.GENERIC_MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.03),
			new ElementAttribute(Element.EARTH, EntityAttributes.GENERIC_ARMOR, Operation.ADD_VALUE, 1.0),
			new ElementAttribute(Element.WIND, EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, Operation.ADD_VALUE, 3.0),
			new ElementAttribute(Element.MAGMA, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, Operation.ADD_VALUE, 1.0),
			new ElementAttribute(Element.VOID, EntityAttributes.GENERIC_GRAVITY, Operation.ADD_MULTIPLIED_BASE, -0.08),
			new ElementAttribute(Element.CRYSTAL, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.1));

	public static void applyAttributes(ServerPlayerEntity player) {
		for (ElementAttribute a : ATTRIBUTES) {
			int rank = passiveRank(player, a.element());
			AttributeApplier.set(player, a.attribute(), Aptitudes.id("element/" + a.element().key()),
					rank * a.perRank(), a.op());
		}
	}

	/** Pasivas basadas en efectos; llamado cada segundo. */
	public static void tick(ServerPlayerEntity p, int serverTicks) {
		if (passiveRank(p, Element.FIRE) > 0) quietEffect(p, StatusEffects.FIRE_RESISTANCE, 60);
		if (passiveRank(p, Element.ICE) > 0) p.setFrozenTicks(0);
		int water = passiveRank(p, Element.WATER);
		if (water > 0 && p.isSubmergedInWater()) {
			quietEffect(p, StatusEffects.WATER_BREATHING, 60);
			if (water >= 2) quietEffect(p, StatusEffects.DOLPHINS_GRACE, 60);
		}
		int plant = passiveRank(p, Element.PLANT);
		if (plant > 0 && serverTicks % (Math.max(1, 10 - 2 * plant) * 20) == 0 && p.getHealth() < p.getMaxHealth()) {
			var below = p.getWorld().getBlockState(p.getBlockPos().down());
			if (below.isIn(BlockTags.DIRT) || below.isIn(BlockTags.LEAVES)) p.heal(1.0f);
		}
		if (passiveRank(p, Element.LIGHT) > 0) {
			p.removeStatusEffect(StatusEffects.BLINDNESS);
			p.removeStatusEffect(StatusEffects.DARKNESS);
		}
		if (passiveRank(p, Element.POISON) > 0) p.removeStatusEffect(StatusEffects.POISON);
		int sound = passiveRank(p, Element.SOUND);
		if (sound > 0 && serverTicks % 80 == 0) { // ecolocalización: los hostiles cercanos brillan
			for (LivingEntity e : Targeting.hostilesAround(p, 8 + 4 * sound)) {
				e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, true, false));
			}
		}
		if (passiveRank(p, Element.SHADOW) > 0 && p.getWorld().getLightLevel(p.getBlockPos()) <= 7) {
			quietEffect(p, StatusEffects.NIGHT_VISION, 260); // >200 ticks evita el parpadeo de pantalla
		}
	}

	/** Magma: inmunidad al suelo ardiente (bloques de magma). */
	public static boolean ignoresHotFloor(ServerPlayerEntity player) {
		return passiveRank(player, Element.MAGMA) > 0;
	}

	// ------------------------------------------------------------ Utilidades

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void quietEffect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, 0, true, false));
	}

	private static void launch(LivingEntity e, double upward) {
		e.addVelocity(0, upward, 0);
		e.velocityModified = true;
	}

	@Nullable
	static Element activeElement(ServerPlayerEntity player) {
		return RoleManager.get(player).activeElement();
	}
}
