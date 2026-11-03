package dev.aptitudes.event;

/**
 * Qué criatura aparece en cada oleada de la Niebla de almas (v41.1). Puro (sin Minecraft), con pruebas.
 *
 * <p>Solo no muertos que caminan: zombis, esqueletos y sus variantes. El clima del bioma donde aparece la criatura
 * decide la variante dominante (ahogados junto al agua, momias en climas cálidos y errantes en los fríos), pero
 * siempre pueden salir zombis y esqueletos comunes.</p>
 */
public final class SoulFogRules {
	/** Criaturas de la Niebla. Solo se añade al final. */
	public enum FogMob { ZOMBIE, ZOMBIE_VILLAGER, DROWNED, HUSK, SKELETON, STRAY, BOGGED }

	/** Clima del punto de aparición. */
	public enum Climate { TEMPERATE, WATER, HOT, COLD }

	/** Temperatura de bioma a partir de la cual el clima es cálido (desierto y badlands = 2,0; sabana = 1,2). */
	public static final float HOT_TEMPERATURE = 1.5f;
	/** Temperatura por debajo de la cual el clima es frío (nieva: taiga nevada, llanuras nevadas…). */
	public static final float COLD_TEMPERATURE = 0.15f;

	// Pesos (suman 100) por clima, en el orden de FogMob.
	private static final int[][] WEIGHTS = {
			//              ZOM ZVIL DRO HUSK SKE STRAY BOG
			/* TEMPERATE */ {40, 20, 0, 0, 40, 0, 0},
			/* WATER     */ {15, 0, 50, 0, 15, 0, 20},
			/* HOT       */ {15, 10, 0, 50, 25, 0, 0},
			/* COLD      */ {20, 15, 0, 0, 15, 50, 0},
	};

	private SoulFogRules() {}

	/** Clima según el agua y la temperatura del bioma. El agua tiene prioridad (un río helado da ahogados). */
	public static Climate climate(boolean water, float temperature) {
		if (water) return Climate.WATER;
		if (Float.isNaN(temperature)) return Climate.TEMPERATE;
		if (temperature >= HOT_TEMPERATURE) return Climate.HOT;
		if (temperature < COLD_TEMPERATURE) return Climate.COLD;
		return Climate.TEMPERATE;
	}

	/** Elige la criatura con una tirada en [0, 100); fuera de rango se acota. */
	public static FogMob choose(Climate climate, int roll) {
		int r = Math.max(0, Math.min(99, roll));
		int[] weights = WEIGHTS[climate.ordinal()];
		for (int i = 0; i < weights.length; i++) {
			if (r < weights[i]) return FogMob.values()[i];
			r -= weights[i];
		}
		return FogMob.ZOMBIE;
	}

	/** Peso (sobre 100) de una criatura en un clima, para las pruebas y la documentación. */
	public static int weight(Climate climate, FogMob mob) {
		return WEIGHTS[climate.ordinal()][mob.ordinal()];
	}
}
