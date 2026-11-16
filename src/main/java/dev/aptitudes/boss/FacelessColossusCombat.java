package dev.aptitudes.boss;

/** Tiempos de los clips y altura de contacto de los ataques del Coloso. */
final class FacelessColossusCombat {
	static final float BODY_WIDTH = 1.6f;
	static final float BODY_HEIGHT = 3.4f;
	static final float EYE_HEIGHT = 3.14f;

	static final int TRANSITION_TICKS = 2;
	static final int CHARGE_TRAVEL_TICKS = 8;
	static final int PHASE_CLIP_TICKS = 30;
	static final double SWEEP_LOW = 1.2;
	static final double SWEEP_HIGH = 2.6;
	static final double SLAM_LOW = 0;
	static final double SLAM_HIGH = 1.25;

	private FacelessColossusCombat() {}

	static int attackClipTicks(String attack) {
		return switch (attack) {
			case "sweep", "charge" -> 28;
			case "slam" -> 40;
			default -> throw new IllegalArgumentException(attack);
		};
	}

	static int recoveryTicks(BossMoveRules.Move move) {
		return attackClipTicks(move.id()) + TRANSITION_TICKS - move.windup();
	}

	/** Altura de la línea del golpe dentro del objetivo, medida desde los pies del jefe. */
	static double contactHeight(double bottom, double top, double low, double high) {
		double overlapBottom = Math.max(bottom, low);
		double overlapTop = Math.min(top, high);
		return overlapTop > overlapBottom ? (overlapBottom + overlapTop) * 0.5 : Double.NaN;
	}
}
