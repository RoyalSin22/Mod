package dev.aptitudes.temple;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Fase C2 del plan de avance: templos que pueden volver a generarse y recompensas individuales. Puro (sin Minecraft).
 *
 * <p>Regeneración: tras resolverse un templo, no puede volver antes de {@link #MIN_GAP_TICKS}. Después, la probabilidad
 * sube de forma lineal hasta {@link #MAX_PERMILLE} por mil en {@link #FULL_CHANCE_TICKS}. Se usa una tirada de 0 a 999
 * para que la decisión sea determinista y comprobable. Valores provisionales, pendientes de validar con el autor.</p>
 *
 * <p>Recompensa: cada jugador puede reclamar una vez por instancia del templo, aunque el templo se regenere.</p>
 */
public final class TempleRecurrenceRules {
	/** Espera mínima tras la resolución (3 días de 24 000 ticks). Provisional. */
	public static final long MIN_GAP_TICKS = 72_000L;
	/** Momento en el que la probabilidad llega a su máximo (6 días). Provisional. */
	public static final long FULL_CHANCE_TICKS = 144_000L;
	/** Probabilidad máxima de regeneración, por mil. Provisional. */
	public static final int MAX_PERMILLE = 250;
	/** Sin resolución registrada. */
	public static final long NEVER = -1L;

	private TempleRecurrenceRules() {}

	/** Probabilidad de regeneración en este instante, por mil (0 = no puede volver todavía). */
	public static int chancePermille(long resolvedTick, long now) {
		if (resolvedTick == NEVER) return 0;
		long elapsed = now - resolvedTick;
		if (elapsed < MIN_GAP_TICKS) return 0;
		if (elapsed >= FULL_CHANCE_TICKS) return MAX_PERMILLE;
		return (int) ((elapsed - MIN_GAP_TICKS) * MAX_PERMILLE / (FULL_CHANCE_TICKS - MIN_GAP_TICKS));
	}

	/** ¿Se regenera? {@code roll} es una tirada de 0 a 999; fuera de ese rango se trata como 0 o 999. */
	public static boolean shouldRegenerate(long resolvedTick, long now, int roll) {
		int clamped = Math.max(0, Math.min(999, roll));
		return clamped < chancePermille(resolvedTick, now);
	}

	/** ¿Puede este jugador reclamar la recompensa de esta instancia del templo? */
	public static boolean canClaim(Set<UUID> claimed, UUID player) {
		return player != null && !claimed.contains(player);
	}

	/** Conjunto de reclamaciones con el jugador añadido (copia inmutable). */
	public static Set<UUID> claim(Set<UUID> claimed, UUID player) {
		if (!canClaim(claimed, player)) return claimed;
		Set<UUID> next = new HashSet<>(claimed);
		next.add(player);
		return Collections.unmodifiableSet(next);
	}
}
