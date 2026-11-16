package dev.aptitudes.boss;

import dev.aptitudes.boss.BossMoveRules.Move;

import java.util.List;

/** Tiempos del Coloso Sin Rostro en ticks. Los clips usan los mismos cortes de aviso y recuperación. */
final class FacelessColossusMoves {
	static final Move SWEEP = new Move("sweep", 1, 0, 4.5, 18, 30, 3,
			AttackTags.of(AttackTags.Tag.PARRYABLE));
	static final Move SLAM = new Move("slam", 1, 0, 5.5, 28, 70, 2,
			AttackTags.of(AttackTags.Tag.HEAVY, AttackTags.Tag.GROUND));
	static final Move CHARGE = new Move("charge", 2, 5, 18, 16, 90, 2,
			AttackTags.of(AttackTags.Tag.HEAVY));
	static final List<Move> ALL = List.of(SWEEP, SLAM, CHARGE);
	static final int CHARGE_TRAVEL_TICKS = 12;
	static final int PHASE_ANIMATION_TICKS = 30;

	private FacelessColossusMoves() {
	}

	static int recoveryTicks(Move move) {
		if (move == SWEEP) return 10;
		if (move == SLAM) return 12;
		return CHARGE_TRAVEL_TICKS + 8;
	}
}
