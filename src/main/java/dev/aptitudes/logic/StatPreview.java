package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.network.StatsPayload;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Cálculo de estadísticas para la pantalla de atributos. Reproduce la fórmula de
 * Minecraft: {@code (base + ΣADD) × (1 + ΣMULT_BASE) × Π(1 + MULT_TOTAL)}, con tope.
 */
public final class StatPreview {
	/** Orden de las estadísticas base que muestra la pantalla. */
	public static final List<RegistryEntry<EntityAttribute>> STATS = List.of(
			EntityAttributes.GENERIC_MAX_HEALTH,
			EntityAttributes.GENERIC_ATTACK_DAMAGE,
			EntityAttributes.GENERIC_ATTACK_SPEED,
			EntityAttributes.GENERIC_ARMOR,
			EntityAttributes.GENERIC_MOVEMENT_SPEED,
			EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
			EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
			EntityAttributes.GENERIC_LUCK);

	private StatPreview() {
	}

	/** Estadísticas propias añadidas tras las vanilla: maná máx., daño de hechizos %, reducción de enfriamiento %. */
	public static final int MANA = 8;
	public static final int SPELL_DAMAGE = 9;
	public static final int COOLDOWN_REDUCTION = 10;
	public static final int COUNT = 11;

	public static StatsPayload build(ServerPlayerEntity player) {
		double[] stats = new double[COUNT];
		for (int i = 0; i < STATS.size(); i++) {
			EntityAttributeInstance inst = player.getAttributeInstance(STATS.get(i));
			stats[i] = inst == null ? 0 : inst.getValue();
		}
		stats[MANA] = AbilityManager.maxEnergy(player);
		stats[SPELL_DAMAGE] = 100 * AbilityManager.attributeValue(player, PlayerAttribute.INTELLIGENCE);
		stats[COOLDOWN_REDUCTION] = 100 * AbilityManager.attributeValue(player, PlayerAttribute.FAITH);

		PlayerAttribute[] attrs = PlayerAttribute.values();
		double[] slopes = new double[attrs.length];
		for (PlayerAttribute a : attrs) {
			double perPoint = ProgressManager.effectivePerPoint(player, a);
			switch (a) {
				case MIND -> { slopes[a.ordinal()] = perPoint; continue; }
				case INTELLIGENCE, FAITH -> { slopes[a.ordinal()] = 100 * perPoint; continue; }
				default -> { }
			}
			EntityAttributeInstance inst = player.getAttributeInstance(a.attribute());
			if (inst == null) continue;
			EntityAttributeModifier own = inst.getModifier(a.modifierId());
			double current = own == null ? 0 : own.value();
			slopes[a.ordinal()] = valueWith(inst, a, current + perPoint) - valueWith(inst, a, current);
		}
		return new StatsPayload(stats, slopes);
	}

	/** Valor del atributo si el modificador del mod tuviera el valor {@code ownValue}. */
	private static double valueWith(EntityAttributeInstance inst, PlayerAttribute attr, double ownValue) {
		double add = 0, multBase = 0, multTotal = 1;
		boolean seen = false;
		for (EntityAttributeModifier m : inst.getModifiers()) {
			double v = m.value();
			if (m.id().equals(attr.modifierId())) {
				v = ownValue;
				seen = true;
			}
			switch (m.operation()) {
				case ADD_VALUE -> add += v;
				case ADD_MULTIPLIED_BASE -> multBase += v;
				case ADD_MULTIPLIED_TOTAL -> multTotal *= 1 + v;
			}
		}
		if (!seen) {
			switch (attr.operation()) {
				case ADD_VALUE -> add += ownValue;
				case ADD_MULTIPLIED_BASE -> multBase += ownValue;
				case ADD_MULTIPLIED_TOTAL -> multTotal *= 1 + ownValue;
			}
		}
		double value = (inst.getBaseValue() + add) * (1 + multBase) * multTotal;
		return inst.getAttribute().value().clamp(value);
	}
}
