package dev.aptitudes.temple;

/**
 * T3 de la cadena narrativa: reglas puras del flujo del Templo del Custodio (distribución nueva, {@code layout = 1}).
 * Sin clases de Minecraft: se prueban con JUnit ({@code TempleFlowRulesTest}).
 *
 * <p>Orden del templo: el Autómata Antiguo duerme en el atrio y despierta al cruzar el umbral; hasta que cae, el puzle
 * está bloqueado; al resolverlo se abre la escalera, y la Puerta del Custodio solo cede al Ancla del Velo (que se
 * consume). Los templos antiguos ({@code layout = 0}) conservan su flujo de siempre y no usan estas reglas.</p>
 */
public final class TempleFlowRules {
	/** Distribución antigua (v64): un puzle y el guardián. */
	public static final int LAYOUT_OLD = 0;
	/** Distribución del Templo del Custodio (T3). */
	public static final int LAYOUT_CUSTODIAN = 1;
	/** Distancia (bloques) al umbral del atrio a la que despierta el Autómata. */
	public static final double TRIGGER_RADIUS = 9.0;
	/** Comprobaciones seguidas sin ver a un jefe antes de volver a hacerlo aparecer (cada una dura medio segundo). */
	public static final int MISSING_CHECKS = 6;
	/** Vida extra del Autómata del templo por anillo de Amenaza (misma fórmula que el guardián). */
	public static final double AUTOMATON_HEALTH_PER_RING = 0.25;

	private TempleFlowRules() {}

	/** Fases del templo nuevo. Se guardan como número: solo se añaden valores al final. */
	public enum Flow { DORMANT, AUTOMATON, PUZZLE, DOOR_LOCKED, CUSTODIAN, CLEARED }

	/** Resultado de usar la Puerta del Custodio. */
	public enum DoorUse {
		/** El Ancla abre la puerta (se consume). */
		OPEN,
		/** Todavía no toca: el puzle o el Autómata siguen pendientes. */
		NOT_READY,
		/** Está lista pero falta el Ancla del Velo. */
		NEEDS_ANCHOR,
		/** Ya se abrió. */
		ALREADY_OPEN
	}

	public static int clampLayout(int layout) {
		return layout == LAYOUT_CUSTODIAN ? LAYOUT_CUSTODIAN : LAYOUT_OLD;
	}

	/** Fase guardada → fase válida (un valor desconocido vuelve al principio, nunca lanza). */
	public static Flow flowFromOrdinal(int ordinal) {
		Flow[] all = Flow.values();
		return ordinal < 0 || ordinal >= all.length ? Flow.DORMANT : all[ordinal];
	}

	/** ¿Despierta el Autómata? Solo desde el reposo y con un jugador dentro del radio del umbral. */
	public static boolean shouldWake(Flow flow, boolean playerInsideTrigger) {
		return flow == Flow.DORMANT && playerInsideTrigger;
	}

	/** El puzle (palancas y placas) solo responde en su fase: no vale resolverlo con el Autómata en pie. */
	public static boolean puzzleActive(Flow flow) {
		return flow == Flow.PUZZLE;
	}

	/** Cae el Autómata: pasa al puzle. En cualquier otra fase no cambia nada. */
	public static Flow afterAutomatonDown(Flow flow) {
		return flow == Flow.AUTOMATON ? Flow.PUZZLE : flow;
	}

	/** Se resuelve el puzle: se abre la escalera y queda la puerta del Custodio. */
	public static Flow afterPuzzleSolved(Flow flow) {
		return flow == Flow.PUZZLE ? Flow.DOOR_LOCKED : flow;
	}

	/** Se abre la puerta del Custodio: despierta el Custodio. */
	public static Flow afterDoorOpened(Flow flow) {
		return flow == Flow.DOOR_LOCKED ? Flow.CUSTODIAN : flow;
	}

	/** Cae el Custodio: el templo queda limpio. */
	public static Flow afterCustodianDown(Flow flow) {
		return flow == Flow.CUSTODIAN ? Flow.CLEARED : flow;
	}

	/** Qué pasa al usar la Puerta del Custodio. */
	public static DoorUse useDoor(Flow flow, boolean holdsAnchor) {
		return switch (flow) {
			case DOOR_LOCKED -> holdsAnchor ? DoorUse.OPEN : DoorUse.NEEDS_ANCHOR;
			case CUSTODIAN, CLEARED -> DoorUse.ALREADY_OPEN;
			default -> DoorUse.NOT_READY;
		};
	}

	/** ¿Hay que volver a hacer aparecer a un jefe que no se encuentra? */
	public static boolean shouldRespawn(int missingChecks) {
		return missingChecks >= MISSING_CHECKS;
	}

	/** Bonificación multiplicativa de vida del Autómata del templo (+25 % por anillo). */
	public static double automatonHealthBonus(int ring) {
		return AUTOMATON_HEALTH_PER_RING * TempleRules.clampRing(ring);
	}

	/** ¿Está el jugador dentro del radio del umbral? Cálculo en {@code double}: las coordenadas vienen del mundo. */
	public static boolean insideTrigger(double dx, double dy, double dz) {
		double r = TRIGGER_RADIUS;
		return dx * dx + dy * dy + dz * dz <= r * r;
	}
}
