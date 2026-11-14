package dev.aptitudes.event;

/**
 * T1 de la cadena narrativa: reglas puras de los eventos que se activan por una acción del jugador en lugar de por
 * calendario (Niebla de almas por el aldeano del gremio y Lluvia de brasas al encender un portal al Nether).
 * Sin clases de Minecraft: se prueban con JUnit ({@code EventTriggerRulesTest}).
 *
 * <p>Los tiempos son ticks del reloj del mundo ({@code World#getTime}), que no cambia con {@code /time set}.
 * La espera se cuenta desde que el evento <b>empieza</b> (valor provisional: pendiente de confirmar con el autor).</p>
 */
public final class EventTriggerRules {
	/** Un día de Minecraft. */
	public static final long TICKS_PER_DAY = 24_000L;
	/** «Nunca se ha activado». */
	public static final long NEVER = -1L;
	/** Límites de la duración de la Niebla (1 minuto … 10 días de Minecraft). */
	public static final int MIN_FOG_TICKS = 1_200;
	public static final int MAX_FOG_TICKS = 240_000;
	/** Radio (en bloques) alrededor del obsidiana en el que se cuentan los bloques de portal. */
	public static final int PORTAL_SCAN_RADIUS = 4;
	/** Tope de comprobaciones de portal pendientes a la vez (protege contra clics repetidos). */
	public static final int MAX_PENDING_PORTAL_CHECKS = 16;

	private EventTriggerRules() {}

	/** Qué responde el aldeano del gremio al hablar con él. */
	public enum Reply {
		/** La Niebla empieza. */
		START,
		/** La Niebla está en espera: «los muertos se han ido, pero volverán». */
		WAIT,
		/** Ya hay otro evento en curso. */
		BUSY
	}

	/** Espera en ticks. Cálculo en {@code long}: el valor viene de la configuración. */
	public static long cooldownTicks(int days) {
		return Math.max(0, (long) days) * TICKS_PER_DAY;
	}

	/**
	 * ¿Ha pasado la espera desde {@code lastStartTick}? Sin registro previo está lista. Si el registro está en el
	 * futuro (reloj anómalo) también se considera lista, para no bloquear el evento para siempre.
	 */
	public static boolean ready(long lastStartTick, long now, int cooldownDays) {
		if (lastStartTick == NEVER) return true;
		long elapsed = now - lastStartTick;
		return elapsed < 0 || elapsed >= cooldownTicks(cooldownDays);
	}

	/** Ticks que faltan para que termine la espera (0 si ya está lista). */
	public static long remainingTicks(long lastStartTick, long now, int cooldownDays) {
		if (ready(lastStartTick, now, cooldownDays)) return 0;
		return cooldownTicks(cooldownDays) - (now - lastStartTick);
	}

	/** Duración de la Niebla acotada a los límites seguros. */
	public static long fogDuration(long configuredTicks) {
		return Math.max(MIN_FOG_TICKS, Math.min(MAX_FOG_TICKS, configuredTicks));
	}

	/**
	 * Respuesta del aldeano. Otro evento en curso tiene prioridad: no se puede iniciar la Niebla encima, y tampoco
	 * se hace creer al jugador que está en espera.
	 */
	public static Reply villagerReply(boolean otherEventActive, boolean cooldownReady) {
		if (otherEventActive) return Reply.BUSY;
		return cooldownReady ? Reply.START : Reply.WAIT;
	}

	/** Un portal se ha abierto si ahora hay más bloques de portal que antes del intento de encenderlo. */
	public static boolean portalOpened(int portalBlocksBefore, int portalBlocksAfter) {
		return portalBlocksAfter > Math.max(0, portalBlocksBefore);
	}

	/** ¿Puede añadirse otra comprobación de portal pendiente? */
	public static boolean canQueuePortalCheck(int pending) {
		return pending < MAX_PENDING_PORTAL_CHECKS;
	}
}
