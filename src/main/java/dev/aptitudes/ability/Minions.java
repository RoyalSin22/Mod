package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Creación de esbirros del Nigromante. Sus estadísticas escalan con el nivel
 * de Nigromante del dueño, así que "crecen" a medida que el jugador usa el rol.
 */
public final class Minions {
	public static final int MAX_MINIONS = 4;
	public static final int LIFETIME_TICKS = 45 * 20;

	private Minions() {
	}

	@Nullable
	public static MobEntity spawn(ServerPlayerEntity owner, EntityType<? extends MobEntity> type, int index) {
		// Respeta el máximo: retira el esbirro más antiguo.
		List<MobEntity> existing = AbilityState.minionsOf(owner);
		if (existing.size() >= MAX_MINIONS) existing.get(0).discard();

		MobEntity minion = type.create(owner.getWorld());
		if (minion == null) return null;
		Vec3d side = Targeting.horizontalFacing(owner).rotateY((float) Math.toRadians(index % 2 == 0 ? 60 : -60));
		Vec3d feet = owner.getPos().add(side.multiply(1.5 + index * 0.5));
		minion.refreshPositionAndAngles(feet.x, feet.y, feet.z, owner.getYaw(), 0);
		if (!owner.getWorld().isSpaceEmpty(minion)) {
			minion.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.getYaw(), 0);
		}

		// Equipo: casco (no arde al sol), arco para el esqueleto; nada se suelta al morir.
		minion.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		if (type == EntityType.SKELETON) minion.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		for (EquipmentSlot slot : EquipmentSlot.values()) minion.setEquipmentDropChance(slot, 0.0f);
		minion.setCanPickUpLoot(false);
		minion.setPersistent();

		int level = RoleManager.get(owner).level(Role.NECROMANCER);
		scale(minion, level, PathEffects.value(owner, dev.aptitudes.role.PathEffect.MINION_POWER));
		// Nigromante Maestro+: esbirros más resistentes (Resistencia I permanente y +50 % de vida).
		if (RoleManager.get(owner).tier(Role.NECROMANCER) >= dev.aptitudes.role.Evolution.MASTER) {
			minion.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
					net.minecraft.entity.effect.StatusEffects.RESISTANCE, LIFETIME_TICKS, 0, true, false));
			add(minion, EntityAttributes.GENERIC_MAX_HEALTH, "minion_blood_health", minion.getMaxHealth() * 0.5);
			minion.setHealth(minion.getMaxHealth());
		}
		minion.setCustomName(Text.translatable("entity.aptitudes.minion", owner.getDisplayName()));
		minion.addCommandTag(AbilityState.MINION_TAG);

		// Registrar ANTES de aparecer: ENTITY_LOAD retira invocaciones no registradas.
		AbilityState.trackSummon(minion, owner, LIFETIME_TICKS);
		owner.getWorld().spawnEntity(minion);
		owner.getServerWorld().spawnParticles(ParticleTypes.SOUL, minion.getX(), minion.getY() + 0.5, minion.getZ(),
				15, 0.3, 0.6, 0.3, 0.02);
		return minion;
	}

	/** +0,6 vida, +0,15 daño y +0,25 armadura por nivel de Nigromante (más con la senda Señor de esbirros). */
	private static void scale(MobEntity minion, int level, double power) {
		add(minion, EntityAttributes.GENERIC_MAX_HEALTH, "minion_health", 0.6 * level * (1 + power));
		add(minion, EntityAttributes.GENERIC_ATTACK_DAMAGE, "minion_damage", 0.15 * level * (1 + power));
		add(minion, EntityAttributes.GENERIC_ARMOR, "minion_armor", 0.25 * level);
		minion.setHealth(minion.getMaxHealth());
	}

	private static void add(MobEntity minion, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
							String path, double value) {
		EntityAttributeInstance instance = minion.getAttributeInstance(attribute);
		if (instance == null || value == 0) return;
		instance.removeModifier(Aptitudes.id(path)); // idempotente: añadir un id repetido lanza excepción en 1.21
		instance.addPersistentModifier(new EntityAttributeModifier(Aptitudes.id(path), value, Operation.ADD_VALUE));
	}
}
