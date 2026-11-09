package dev.aptitudes.souls;

/**
 * Fase F3 del plan de avance (cambio de balance aprobado): mancha de XP. Puro (sin Minecraft).
 *
 * <p>Si el jugador vuelve a morir antes de recuperar su mancha, la mancha pierde un 25 %. Se usa división entera para
 * no desbordar con valores grandes.</p>
 */
public final class SoulStainRules {
	/** Parte de la mancha que se pierde al volver a morir (25 %). */
	public static final int LOSS_PERCENT = 25;

	private SoulStainRules() {}

	/** Mancha que queda tras volver a morir antes de recuperarla. Nunca negativa. */
	public static long afterDeathWhileStained(long stain) {
		return afterDeathWhileStained(stain, LOSS_PERCENT);
	}

	/** Igual que {@link #afterDeathWhileStained(long)} con el porcentaje de la configuración. */
	public static long afterDeathWhileStained(long stain, int lossPercent) {
		if (stain <= 0) return 0;
		int loss = Math.max(0, Math.min(100, lossPercent));
		return stain - stain / 100 * loss - stain % 100 * loss / 100;
	}
}
