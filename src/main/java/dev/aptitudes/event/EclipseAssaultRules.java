package dev.aptitudes.event;

import dev.aptitudes.event.EclipseRules.Phase;
import dev.aptitudes.event.StrongEventRules.EclipseMob;

/** Presupuesto y composición del asalto. Puro: no depende de entidades, registros ni reloj de Minecraft. */
public final class EclipseAssaultRules {
	public enum Grade { NORMAL, ELITE, CHAMPION }

	private EclipseAssaultRules() {}

	public static boolean eligible(boolean alive, boolean creative, boolean spectator) {
		return alive && !creative && !spectator;
	}

	public static int waveSize(Phase phase, int penumbra, int totality) {
		return switch (phase) {
			case PENUMBRA -> clamp(penumbra, 16);
			case TOTALITY -> clamp(totality, 24);
			default -> 0;
		};
	}

	/** El presupuesto es de la zona, aunque los jugadores no pertenezcan al mismo grupo. */
	public static int localCap(int perPlayer, int nearbyPlayers, int zoneCap) {
		long personal = (long) clamp(perPlayer, 40) * Math.max(0, nearbyPlayers);
		return (int) Math.min(personal, clamp(zoneCap, 160));
	}

	public static int spawnBudget(int localAlive, int localCap, int worldAlive, int worldCap, int requested) {
		long localFree = (long) clamp(localCap, 160) - Math.max(0, localAlive);
		long worldFree = (long) clamp(worldCap, 512) - Math.max(0, worldAlive);
		return (int) Math.max(0L, Math.min(clamp(requested, 24), Math.min(localFree, worldFree)));
	}

	/** Las garantías respetan el cero de las probabilidades y se ordenan por apariciones exitosas. */
	public static Grade grade(Phase phase, int spawned, int eliteMinimum, int championMinimum,
			double eliteChance, double championChance, double roll) {
		if (phase != Phase.PENUMBRA && phase != Phase.TOTALITY) return Grade.NORMAL;
		double champion = probability(championChance), elite = probability(eliteChance);
		int champions = phase == Phase.TOTALITY && champion > 0 ? clamp(championMinimum, 2) : 0;
		int elites = elite > 0 ? clamp(eliteMinimum, 4) : 0;
		int index = Math.max(0, spawned);
		if (index < champions) return Grade.CHAMPION;
		if (index < champions + elites) return Grade.ELITE;
		if (!Double.isFinite(roll) || roll < 0 || roll >= 1) return Grade.NORMAL;
		if (roll < champion) return Grade.CHAMPION;
		return roll < Math.min(1.0, champion + elite) ? Grade.ELITE : Grade.NORMAL;
	}

	public static double armorChance(Grade grade, boolean guaranteed, double configured) {
		return guaranteed && grade != Grade.NORMAL ? 1.0 : probability(configured);
	}

	/** Las unidades con armadura garantizada usan cuerpos que pueden mostrarla. */
	public static EclipseMob mob(Phase phase, Grade grade, boolean guaranteedArmor, int roll) {
		if (!guaranteedArmor || grade == Grade.NORMAL) return StrongEventRules.eclipseMob(phase, roll);
		EclipseMob[] types = phase == Phase.TOTALITY
				? new EclipseMob[] {EclipseMob.ZOMBIE, EclipseMob.SKELETON, EclipseMob.WITHER_SKELETON}
				: new EclipseMob[] {EclipseMob.ZOMBIE, EclipseMob.SKELETON};
		return types[Math.floorMod(roll, types.length)];
	}

	private static double probability(double value) {
		return Double.isFinite(value) ? Math.max(0.0, Math.min(1.0, value)) : 0.0;
	}

	private static int clamp(int value, int maximum) {
		return Math.max(0, Math.min(maximum, value));
	}
}
