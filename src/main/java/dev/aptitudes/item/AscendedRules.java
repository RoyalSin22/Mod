package dev.aptitudes.item;

/**
 * Cifras de la Armadura Ascendida (V80): lógica pura, sin Minecraft, para poder probarla. Es una mejora de la netherita:
 * más defensa y dureza que ella, y con las cuatro piezas puestas un bonus común más el aspecto del rol principal
 * (los efectos por rol viven en {@code RoleArmorSets}).
 */
public final class AscendedRules {
	/** Defensa por pieza en orden botas, grebas, peto, casco (la netherita vale 3, 6, 8, 3). */
	public static final int[] DEFENSE = {4, 7, 9, 4};
	public static final float TOUGHNESS = 4.0f;
	public static final float KNOCKBACK_RESISTANCE = 0.15f;
	/** Multiplicador de durabilidad (la netherita vale 37). */
	public static final int DURABILITY = 42;
	public static final int ENCHANTABILITY = 20;

	/** Bonus común del set completo, sea cual sea el rol. */
	public static final double COST_MULTIPLIER = 0.90;
	public static final double COOLDOWN_REDUCTION = 0.10;
	public static final float ABILITY_DAMAGE_BONUS = 0.10f;
	public static final float MELEE_DAMAGE_BONUS = 0.04f;

	/** Probabilidad de que un campeón suelte una Plantilla de ascensión. */
	public static final float CHAMPION_TEMPLATE_CHANCE = 0.10f;

	private AscendedRules() {
	}

	public static int totalDefense() {
		int sum = 0;
		for (int value : DEFENSE) sum += value;
		return sum;
	}

	/** Daño saliente extra del set: más con habilidades y daño indirecto que cuerpo a cuerpo. */
	public static float damageBonus(boolean direct) {
		return direct ? MELEE_DAMAGE_BONUS : ABILITY_DAMAGE_BONUS;
	}

	/** Reduce un enfriamiento o coste con el bonus común; nunca baja del 50 % del valor original. */
	public static double reduce(double value, double fraction) {
		return Math.max(value * 0.5, value * (1.0 - fraction));
	}
}
