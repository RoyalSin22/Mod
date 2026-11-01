package dev.aptitudes.logic;

import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.Skill;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Traduce el progreso a modificadores de atributos vanilla.
 *
 * <p>Cada fuente usa un {@link Identifier} fijo, por lo que la operación es
 * idempotente: quitar + volver a poner. Se usan modificadores persistentes para
 * que, al reconectar, la vida máxima ya esté aplicada cuando vanilla restaura la
 * vida actual (si no, se recortaría a 20).</p>
 */
public final class AttributeApplier {
	private AttributeApplier() {
	}

	/** Modificadores de versiones anteriores del mod que ya no existen: se retiran al aplicar. */
	private static final List<Identifier> LEGACY = List.of(
			dev.aptitudes.Aptitudes.id("skill/combat"), dev.aptitudes.Aptitudes.id("skill/defense"),
			dev.aptitudes.Aptitudes.id("skill/agility"), dev.aptitudes.Aptitudes.id("attribute/vitality"),
			dev.aptitudes.Aptitudes.id("attribute/swiftness"));

	public static void apply(ServerPlayerEntity player, PlayerProgress progress, AptitudesConfig config) {
		for (Identifier legacy : LEGACY) {
			for (RegistryEntry<EntityAttribute> attr : List.of(
					net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE,
					net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
					net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED,
					net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH)) {
				EntityAttributeInstance instance = player.getAttributeInstance(attr);
				if (instance != null) instance.removeModifier(legacy);
			}
		}
		for (PlayerAttribute a : PlayerAttribute.values()) {
			if (a.attribute() == null) continue; // Mente, Inteligencia y Fe se aplican en su propia lógica
			set(player, a.attribute(), a.modifierId(),
					progress.getAllocated(a) * ProgressManager.effectivePerPoint(player, a), a.operation());
		}
		for (Skill s : Skill.values()) {
			List<Skill.SkillBonus> bonuses = s.bonuses();
			for (int i = 0; i < bonuses.size(); i++) {
				Skill.SkillBonus b = bonuses.get(i);
				if (b.attribute() == null) continue; // rotura, cosecha doble, flechas: lógica propia
				set(player, b.attribute(), s.modifierId(i), progress.getLevel(s) * b.perLevel().applyAsDouble(config),
						b.operation());
			}
		}
		SkillMilestones.applyAttributes(player);
		// Tras un respec la vida máxima puede bajar.
		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}

	/** Quita y, si {@code value != 0}, vuelve a añadir el modificador con ese id (idempotente). */
	public static void set(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute, Identifier id,
							double value, EntityAttributeModifier.Operation operation) {
		EntityAttributeInstance instance = player.getAttributeInstance(attribute);
		if (instance == null) return;
		instance.removeModifier(id);
		if (value != 0.0) {
			instance.addPersistentModifier(new EntityAttributeModifier(id, value, operation));
		}
	}
}
