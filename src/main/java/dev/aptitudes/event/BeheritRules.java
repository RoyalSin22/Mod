package dev.aptitudes.event;

/**
 * T5 de la cadena narrativa: reglas puras del sacrificio del Behelit (Resonador del Umbral) en la Hoguera.
 * Sin clases de Minecraft: se prueban con JUnit ({@code BeheritRulesTest}).
 *
 * <p>Decisión D-25: el Behelit solo puede usarse <b>una vez cada {@code beheritCooldownDays} días por mundo,
 * contados desde el fin del Eclipse anterior</b> (no desde su inicio). La espera usa el reloj del mundo
 * ({@code World#getTime}), que no cambia con {@code /time set}; un registro en el futuro se trata como lista,
 * igual que en {@link EventTriggerRules#ready}, para no bloquear el ritual para siempre.</p>
 */
public final class BeheritRules {
	/** Ticks de un «día» en horas de juego (una hora de juego son 1000 ticks). */
	public static final long TICKS_PER_HOUR = 1_000L;

	private BeheritRules() {}

	/** Resultado de intentar sacrificar el Behelit. */
	public enum Result {
		/** Se puede: el Eclipse empieza y el objeto se consume. */
		READY,
		/** El Eclipse está desactivado en la configuración ({@code eclipseEnabled = false}). */
		DISABLED,
		/** Ya hay un Eclipse u otro evento del mundo en curso. */
		BUSY,
		/** La espera desde el fin del último Eclipse no ha terminado. */
		COOLDOWN
	}

	/**
	 * Decide si se puede sacrificar. Orden de prioridad: desactivado, ocupado, espera. Así el aviso que ve el jugador
	 * es el que primero tendría que resolver.
	 *
	 * @param lastEndTick tick del fin del último Eclipse, o {@link EventTriggerRules#NEVER}
	 */
	public static Result check(boolean eclipseEnabled, boolean busy, long lastEndTick, long now, int cooldownDays) {
		if (!eclipseEnabled) return Result.DISABLED;
		if (busy) return Result.BUSY;
		return EventTriggerRules.ready(lastEndTick, now, cooldownDays) ? Result.READY : Result.COOLDOWN;
	}

	/** Horas de juego que faltan, redondeadas hacia arriba (mínimo 1 si aún hay espera; 0 si está lista). */
	public static long hoursLeft(long lastEndTick, long now, int cooldownDays) {
		long remaining = EventTriggerRules.remainingTicks(lastEndTick, now, cooldownDays);
		return remaining <= 0 ? 0 : (remaining + TICKS_PER_HOUR - 1) / TICKS_PER_HOUR;
	}
}
