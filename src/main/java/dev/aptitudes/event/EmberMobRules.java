package dev.aptitudes.event;

/**
 * Lluvia de brasas: criaturas del Nether que aparecen durante el evento (decisión del autor). Puro (sin Minecraft).
 * Pesos sobre 100; la tirada va de 0 a 99.
 */
public final class EmberMobRules {
	/** Criaturas del Nether vanilla del evento. Se añaden al final: el orden no guarda datos. */
	public enum EmberMob { ZOMBIFIED_PIGLIN, HOGLIN, PIGLIN_BRUTE, BLAZE, GHAST, MAGMA_CUBE, WITHER_SKELETON }

	/** Tope de criaturas del evento cerca de cada jugador. */
	public static final int MAX_NEARBY = 4;

	private EmberMobRules() {}

	/** Criatura elegida con una tirada de 0 a 99 (fuera de rango se ajusta). */
	public static EmberMob choose(int roll) {
		int r = Math.max(0, Math.min(99, roll));
		int acc = 0;
		for (EmberMob mob : EmberMob.values()) {
			acc += weight(mob);
			if (r < acc) return mob;
		}
		return EmberMob.ZOMBIFIED_PIGLIN;
	}

	/** Pesos: suman 100. Los piglins y los hoglins son los más comunes; los jefes menores, los menos. */
	public static int weight(EmberMob mob) {
		return switch (mob) {
			case ZOMBIFIED_PIGLIN -> 25;
			case HOGLIN -> 15;
			case PIGLIN_BRUTE -> 10;
			case BLAZE -> 15;
			case GHAST -> 10;
			case MAGMA_CUBE -> 15;
			case WITHER_SKELETON -> 10;
		};
	}
}
