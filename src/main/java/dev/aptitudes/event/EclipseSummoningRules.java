package dev.aptitudes.event;

/**
 * C3: reglas puras para convocar el Eclipse en la Hoguera, con el Astrolabio del Ocaso o con el Behelit (Resonador
 * del Umbral). Sin clases de Minecraft: se prueban con JUnit ({@code EclipseSummoningRulesTest}).
 *
 * <p>Decisiones del autor: convocan con el Astrolabio los operadores o el líder del grupo (E1); el Behelit no pide
 * permiso (basta con tenerlo). Ambos comparten la espera de {@code eclipseSummonCooldownDays} días de juego por mundo,
 * contados desde el <b>fin</b> del último Eclipse (A-2), y la regla de «un evento a la vez». Solo en el Mundo Normal
 * (E6). Los tiempos son ticks del reloj del mundo ({@code World#getTime}).</p>
 */
public final class EclipseSummoningRules {
	private EclipseSummoningRules() {}

	/** Resultado de intentar convocar. Los rechazos están ordenados por prioridad. */
	public enum Verdict {
		ALLOWED,
		/** No es operador ni líder del grupo (solo el Astrolabio). */
		NOT_AUTHORIZED,
		/** El ritual solo vale en el Mundo Normal. */
		WRONG_WORLD,
		/** Ya hay un Eclipse u otro evento del mundo en curso. */
		BUSY,
		/** Aún no han pasado los días de espera desde el fin del último Eclipse. */
		COOLDOWN
	}

	/** ¿Puede este jugador usar el Astrolabio? Operadores (nivel 2+) o el líder de su grupo. */
	public static boolean authorized(boolean operator, boolean partyLeader) {
		return operator || partyLeader;
	}

	/** Astrolabio del Ocaso: permiso, mundo, evento activo y espera, en ese orden. */
	public static Verdict astrolabe(boolean operator, boolean partyLeader, boolean inOverworld, boolean eventActive,
			long lastEndTick, long now, int cooldownDays) {
		if (!authorized(operator, partyLeader)) return Verdict.NOT_AUTHORIZED;
		return behelit(inOverworld, eventActive, lastEndTick, now, cooldownDays);
	}

	/** Behelit: igual que el Astrolabio pero sin permisos. */
	public static Verdict behelit(boolean inOverworld, boolean eventActive, long lastEndTick, long now, int cooldownDays) {
		if (!inOverworld) return Verdict.WRONG_WORLD;
		if (eventActive) return Verdict.BUSY;
		if (!EventTriggerRules.ready(lastEndTick, now, cooldownDays)) return Verdict.COOLDOWN;
		return Verdict.ALLOWED;
	}

	/** Días de juego que faltan (redondeados hacia arriba) para poder convocar; 0 si ya se puede. */
	public static int remainingDays(long lastEndTick, long now, int cooldownDays) {
		long ticks = EventTriggerRules.remainingTicks(lastEndTick, now, cooldownDays);
		long days = (ticks + EventTriggerRules.TICKS_PER_DAY - 1) / EventTriggerRules.TICKS_PER_DAY;
		return (int) Math.min(Integer.MAX_VALUE, days);
	}
}
