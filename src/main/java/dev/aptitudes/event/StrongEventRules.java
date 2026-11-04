package dev.aptitudes.event;

import dev.aptitudes.event.EclipseRules.Phase;

/**
 * v41.2: criaturas más fuertes en el Eclipse y meteoritos de varios tamaños en la Lluvia de brasas. Puro (sin
 * Minecraft), con pruebas. Los valores que se pueden configurar llegan como parámetros y se acotan aquí.
 */
public final class StrongEventRules {
	/** Criaturas de las oleadas del Eclipse. Solo se añade al final. */
	public enum EclipseMob { ZOMBIE, SKELETON, SPIDER, CREEPER, VINDICATOR, WITCH, WITHER_SKELETON }

	/** Armadura que viste una criatura del Eclipse. */
	public enum ArmorTier { CHAINMAIL, IRON, DIAMOND }

	/** Tamaño de un meteorito. Solo se añade al final. */
	public enum MeteorSize { SMALL, LARGE, HUGE }

	/** Criaturas del Nether que trae un meteorito. Solo se añade al final. */
	public enum NetherMob { MAGMA_CUBE, BLAZE, PIGLIN_BRUTE, HOGLIN, WITHER_SKELETON }

	// Pesos (suman 100) en el orden de EclipseMob. En la Penumbra, las mismas criaturas que antes de la v41.2;
	// en la Totalidad se suman esqueletos wither y más vindicadores.
	private static final int[] PENUMBRA_MOBS = {25, 25, 15, 15, 10, 10, 0};
	private static final int[] TOTALITY_MOBS = {20, 15, 10, 10, 20, 10, 15};

	// Pesos (suman 100) en el orden de NetherMob, por tamaño de meteorito.
	private static final int[][] NETHER_MOBS = {
			/* SMALL */ {70, 30, 0, 0, 0},
			/* LARGE */ {35, 35, 15, 15, 0},
			/* HUGE  */ {0, 30, 25, 20, 25},
	};

	private StrongEventRules() {}

	// ------------------------------------------------------------------ Eclipse

	/** Bonificación extra de vida y daño según la fase: 0 en la Penumbra, {@code totalityBonus} (0–2) en la Totalidad. */
	public static double phaseBonus(Phase phase, double totalityBonus) {
		if (phase != Phase.TOTALITY) return 0.0;
		return EclipseRules.boundedMultiplier(totalityBonus, 0.0, 2.0);
	}

	/** Puntos de armadura extra (acotados a 0–20). */
	public static double armorPoints(double configured) {
		return EclipseRules.boundedMultiplier(configured, 0.0, 20.0);
	}

	/** Material de la armadura: malla en la Penumbra, hierro en la Totalidad y diamante para los campeones. */
	public static ArmorTier armorTier(Phase phase, boolean champion) {
		if (champion) return ArmorTier.DIAMOND;
		return phase == Phase.TOTALITY ? ArmorTier.IRON : ArmorTier.CHAINMAIL;
	}

	/** Criatura de una oleada con una tirada en [0, 100) (se acota). */
	public static EclipseMob eclipseMob(Phase phase, int roll) {
		int[] weights = phase == Phase.TOTALITY ? TOTALITY_MOBS : PENUMBRA_MOBS;
		return EclipseMob.values()[pick(weights, roll)];
	}

	public static int eclipseWeight(Phase phase, EclipseMob mob) {
		return (phase == Phase.TOTALITY ? TOTALITY_MOBS : PENUMBRA_MOBS)[mob.ordinal()];
	}

	// ------------------------------------------------------------------ Lluvia de brasas

	/** Tamaño según una tirada en [0, 1): primero enorme, luego grande; probabilidades acotadas a 0–1. */
	public static MeteorSize meteorSize(double roll, double largeChance, double hugeChance) {
		double huge = EclipseRules.boundedMultiplier(hugeChance, 0.0, 1.0);
		double large = EclipseRules.boundedMultiplier(largeChance, 0.0, 1.0);
		if (roll < huge) return MeteorSize.HUGE;
		if (roll < Math.min(1.0, huge + large)) return MeteorSize.LARGE;
		return MeteorSize.SMALL;
	}

	/** Potencia de la explosión (no rompe bloques): 1,5 / 3 / 4,5. */
	public static float explosionPower(MeteorSize size) {
		return switch (size) {
			case SMALL -> 1.5f;
			case LARGE -> 3.0f;
			case HUGE -> 4.5f;
		};
	}

	/** Ticks de caída: los grandes se ven venir antes (2 / 3 / 4 s). */
	public static int fallTicks(MeteorSize size) {
		return switch (size) {
			case SMALL -> 40;
			case LARGE -> 60;
			case HUGE -> 80;
		};
	}

	/** Tiradas de botín mineral: 1 / 3 / 6. */
	public static int lootRolls(MeteorSize size) {
		return switch (size) {
			case SMALL -> 1;
			case LARGE -> 3;
			case HUGE -> 6;
		};
	}

	/** Radio de los restos ardientes (bloques de magma temporales): ninguno / 1 / 2. */
	public static int craterRadius(MeteorSize size) {
		return switch (size) {
			case SMALL -> 0;
			case LARGE -> 1;
			case HUGE -> 2;
		};
	}

	/**
	 * Criaturas del Nether que trae un impacto. Con una tirada en [0, 1): si supera la probabilidad (la mitad para
	 * los pequeños), ninguna; si no, 1 / 2 / 3 según el tamaño.
	 */
	public static int netherMobCount(MeteorSize size, double roll, double mobChance) {
		double chance = EclipseRules.boundedMultiplier(mobChance, 0.0, 1.0) * (size == MeteorSize.SMALL ? 0.5 : 1.0);
		if (roll >= chance) return 0;
		return size.ordinal() + 1;
	}

	/** Qué criatura del Nether, con una tirada en [0, 100) (se acota). */
	public static NetherMob netherMob(MeteorSize size, int roll) {
		return NetherMob.values()[pick(NETHER_MOBS[size.ordinal()], roll)];
	}

	public static int netherWeight(MeteorSize size, NetherMob mob) {
		return NETHER_MOBS[size.ordinal()][mob.ordinal()];
	}

	private static int pick(int[] weights, int roll) {
		int r = Math.max(0, Math.min(99, roll));
		for (int i = 0; i < weights.length; i++) {
			if (r < weights[i]) return i;
			r -= weights[i];
		}
		return 0;
	}
}
