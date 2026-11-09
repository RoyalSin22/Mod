package dev.aptitudes.event;

/**
 * Progreso de las expediciones de Amenaza (F5). Puro (sin Minecraft); los tiempos van en ticks.
 *
 * <p>Cada módulo se completa con una acción dentro de la zona de la expedición. Las metas y las recompensas son valores
 * provisionales de diseño, pendientes de validar en partida.</p>
 */
public final class ExpeditionProgressRules {
	/** Radio de la zona alrededor del punto de inicio (bloques). */
	public static final int ZONE_RADIUS = 48;
	/** Límite de tiempo de una expedición: 15 minutos. */
	public static final long TIME_LIMIT_TICKS = 15L * 60 * 20;
	/** Recompensa al completarla (provisional). */
	public static final int REWARD_EMERALDS = 8;
	public static final int REWARD_SOUL_SHARDS = 2;

	private ExpeditionProgressRules() {}

	/** Acciones necesarias para completar el módulo dentro de la zona. */
	public static int goal(ExpeditionRules.Module module) {
		return switch (module) {
			case COMBAT -> 10;  // criaturas hostiles
			case PUZZLE -> 5;   // fragmentos de alma recogidos (recolección en zona)
			case ELITE -> 1;    // un élite o campeón
			case EVENT -> 1;    // sobrevivir a un evento del mundo
			case BOSS -> 1;     // un jefe
		};
	}

	/** ¿Está el jugador dentro de la zona? Compara distancias al cuadrado para no usar raíces. */
	public static boolean inZone(double distanceSquared) {
		return inZone(distanceSquared, ZONE_RADIUS);
	}

	/** Igual, con el radio de la configuración. */
	public static boolean inZone(double distanceSquared, int radius) {
		return distanceSquared <= (double) radius * radius;
	}

	/** Fragmentos nuevos del grupo desde la última vez: nunca negativos (gastar fragmentos no resta progreso). */
	public static int shardGain(int groupTotal, int seen) {
		return Math.max(0, groupTotal - seen);
	}

	/** ¿Ha caducado la expedición? */
	public static boolean expired(long startTick, long now) {
		return expired(startTick, now, TIME_LIMIT_TICKS);
	}

	/** Igual, con el límite de la configuración (en ticks). */
	public static boolean expired(long startTick, long now, long limitTicks) {
		return now - startTick >= limitTicks;
	}
}
