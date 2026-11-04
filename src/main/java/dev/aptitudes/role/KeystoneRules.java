package dev.aptitudes.role;

import java.util.HashMap;
import java.util.Map;

/**
 * Piedras angulares de las Sendas (v46.1): el escalón S7 (nivel 50, 1 rango) de cada senda. Puro (sin Minecraft),
 * con pruebas ({@code role/KeystoneRulesTest}).
 *
 * <p>Cada senda tiene una de 10 {@link Template plantillas}. La tabla de qué plantilla lleva cada senda está en
 * {@code docs/V46_DISENO_SENDAS.md} §10. Los números (curación, esperas, daño…) vienen de la configuración y se
 * acotan aquí.</p>
 */
public final class KeystoneRules {
	/** Plantillas. Solo se añade al final. */
	public enum Template {
		/** Al matar: cura una fracción de la vida máxima y da Prisa unos segundos. */
		CARNAGE,
		/** Con poca vida: absorción y resistencia (con espera). */
		BULWARK,
		/** Cada N-ésima habilidad no gasta maná y su espera se reduce a la mitad. */
		OVERLOAD,
		/** Al matar: explosión que daña a los enemigos cercanos (sin romper bloques ni dañar aliados). */
		DETONATION,
		/** Al matar: los efectos al golpear del objetivo pasan a enemigos cercanos. */
		CONTAGION,
		/** +1 esbirro máximo; los esbirros curan al dueño con parte de su daño. */
		LEGION,
		/** Tras esquivar, el siguiente golpe en unos segundos es crítico seguro. */
		SPECTER,
		/** Probabilidad de una tirada de botín extra al matar criaturas; esmeralda al matar élites. */
		FORTUNE,
		/** Los críticos lanzan una chispa a un enemigo cercano (con espera corta). */
		TEMPEST,
		/** Cada pocos segundos cura a los aliados del grupo cercanos (y al jugador). */
		SANCTUARY;

		public String key() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	private static final Map<String, Template[]> TABLE = new HashMap<>();

	static {
		// Rol → plantilla de las sendas A, B y C (docs/V46_DISENO_SENDAS.md §10).
			entry("berserker", Template.CARNAGE, Template.BULWARK, Template.TEMPEST);
			entry("mage", Template.TEMPEST, Template.OVERLOAD, Template.SPECTER);
			entry("assassin", Template.SPECTER, Template.DETONATION, Template.CONTAGION);
			entry("herbalist", Template.SANCTUARY, Template.CONTAGION, Template.FORTUNE);
			entry("engineer", Template.DETONATION, Template.BULWARK, Template.OVERLOAD);
			entry("lancer", Template.TEMPEST, Template.BULWARK, Template.CARNAGE);
			entry("swordsman", Template.SPECTER, Template.CARNAGE, Template.BULWARK);
			entry("paladin", Template.DETONATION, Template.BULWARK, Template.SANCTUARY);
			entry("necromancer", Template.LEGION, Template.CONTAGION, Template.DETONATION);
			entry("druid", Template.SANCTUARY, Template.CARNAGE, Template.CONTAGION);
			entry("merchant", Template.FORTUNE, Template.DETONATION, Template.SPECTER);
			entry("monk", Template.TEMPEST, Template.SANCTUARY, Template.CARNAGE);
			entry("elementalist", Template.DETONATION, Template.OVERLOAD, Template.TEMPEST);
			entry("gunslinger", Template.TEMPEST, Template.OVERLOAD, Template.SPECTER);
			entry("weapon_summoner", Template.TEMPEST, Template.BULWARK, Template.OVERLOAD);
			entry("hunter", Template.CONTAGION, Template.FORTUNE, Template.CARNAGE);
			entry("bard", Template.SANCTUARY, Template.TEMPEST, Template.BULWARK);
			entry("warlock", Template.CONTAGION, Template.CARNAGE, Template.OVERLOAD);
			entry("tamer", Template.LEGION, Template.CARNAGE, Template.SANCTUARY);
			entry("shapeshifter", Template.SPECTER, Template.FORTUNE, Template.BULWARK);
	}

	private KeystoneRules() {}

	private static void entry(String role, Template a, Template b, Template c) {
		TABLE.put(role, new Template[] {a, b, c});
	}

	/** Plantilla de la piedra angular de una senda, o null si el rol o la senda no existen. */
	public static Template template(String roleKey, int path) {
		Template[] t = TABLE.get(roleKey);
		return t == null || path < 1 || path > t.length ? null : t[path - 1];
	}

	/** Roles con tabla (para las pruebas). */
	public static java.util.Set<String> roles() {
		return java.util.Collections.unmodifiableSet(TABLE.keySet());
	}

	// ------------------------------------------------------------ Sobrecarga

	/** Cada cuántas habilidades llega la gratuita (acotado a 2–20). */
	public static int overloadEvery(int configured) {
		return Math.max(2, Math.min(20, configured));
	}

	/** ¿La próxima habilidad es la gratuita? {@code counter} = habilidades lanzadas desde la última gratuita. */
	public static boolean overloadReady(int counter, int every) {
		return counter >= overloadEvery(every) - 1;
	}

	/** Contador tras lanzar una habilidad: vuelve a 0 después de la gratuita. */
	public static int overloadAfterCast(int counter, int every) {
		return overloadReady(counter, every) ? 0 : Math.max(0, counter) + 1;
	}

	// ------------------------------------------------------------ Baluarte y esperas

	/** ¿Salta el Baluarte? Vida tras el golpe por debajo del umbral, viva, y sin espera pendiente. */
	public static boolean bulwarkTriggers(float health, float maxHealth, double threshold, long now, long readyAt) {
		if (health <= 0 || maxHealth <= 0 || now < readyAt) return false;
		double t = Math.max(0.0, Math.min(1.0, threshold));
		return health < maxHealth * t;
	}

	/** Momento (ticks de juego) en que vuelve a estar lista tras una espera en segundos. Sin desbordamiento. */
	public static long readyAt(long now, double seconds) {
		double s = Double.isFinite(seconds) ? Math.max(0.0, Math.min(3600.0, seconds)) : 0.0;
		return now + Math.round(s * 20.0);
	}

	/** Fracción de la espera ya cumplida (0–1), para el indicador del HUD. */
	public static float progress(long now, long readyAt, double seconds) {
		long total = Math.round(Math.max(0.0, seconds) * 20.0);
		if (total <= 0 || now >= readyAt) return 1f;
		return Math.max(0f, Math.min(1f, 1f - (readyAt - now) / (float) total));
	}

	// ------------------------------------------------------------ Valores

	/** Vida que cura la Carnicería: fracción (0–1) de la vida máxima. */
	public static float carnageHeal(float maxHealth, double fraction) {
		double f = Double.isFinite(fraction) ? Math.max(0.0, Math.min(1.0, fraction)) : 0.0;
		return (float) (Math.max(0f, maxHealth) * f);
	}

	/** Nivel del efecto de Absorción para dar al menos {@code hearts} puntos de vida (4 por nivel; 0 = ninguno). */
	public static int absorptionAmplifier(double points) {
		if (!Double.isFinite(points) || points < 4.0) return points >= 1.0 ? 0 : -1;
		return (int) Math.min(9, Math.ceil(points / 4.0) - 1);
	}

	/** Curación al dueño por el daño de un esbirro (Legión): fracción (0–1) del daño. */
	public static float legionHeal(float damage, double fraction) {
		double f = Double.isFinite(fraction) ? Math.max(0.0, Math.min(1.0, fraction)) : 0.0;
		return (float) (Math.max(0f, damage) * f);
	}

	/** Esbirros máximos con o sin Legión (el base sale de {@code Minions}). */
	public static int maxMinions(int base, boolean legion) {
		return Math.max(0, base) + (legion ? 1 : 0);
	}
}
