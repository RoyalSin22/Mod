package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;

import java.util.EnumMap;
import java.util.Map;

/**
 * Evolución de roles: Base (0) → Maestro (1) → Definitivo (2).
 *
 * <p>Requisitos para evolucionar:</p>
 * <ul>
 *   <li><b>Maestro</b>: nivel {@link #MASTER_LEVEL} + prueba del rol + Emblema de maestría.</li>
 *   <li><b>Definitivo</b>: nivel {@link #ULTIMATE_LEVEL} + haber derrotado a un jefe (Wither, Dragón
 *       o Warden) con el rol activo + Núcleo definitivo.</li>
 * </ul>
 *
 * <p>Efectos: cada nivel de evolución suma +1 al rango efectivo de todos los nodos
 * del rol (pueden superar el rango 3). El Definitivo desbloquea además la
 * habilidad definitiva (tecla R).</p>
 */
public final class Evolution {
	public static final int BASE = 0;
	public static final int MASTER = 1;
	public static final int ULTIMATE = 2;
	public static final int MASTER_LEVEL = 20;
	public static final int ULTIMATE_LEVEL = 30;

	/** Qué cuenta para la prueba de Maestro de cada rol. */
	public enum TrialType {
		/** Muertes causadas con el arma del rol (hacha, daga, lanza, espada, puños). */
		WEAPON_KILLS,
		/** No-muertos derrotados. */
		UNDEAD_KILLS,
		/** Criaturas muertas a menos de 16 bloques (almas cosechadas). */
		SOULS,
		/** Usos de habilidades activas del rol. */
		ABILITY_USES,
		/** Acciones propias del rol (cosechar, plantar, criar, comerciar…). */
		ROLE_ACTIONS
	}

	public record Trial(TrialType type, int target) {
		public String translationKey() {
			return "trial." + Aptitudes.MOD_ID + "." + type.name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	private static final Map<Role, Trial> TRIALS = new EnumMap<>(Role.class);

	static {
		TRIALS.put(Role.BERSERKER, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.MAGE, new Trial(TrialType.ABILITY_USES, 300));
		TRIALS.put(Role.ASSASSIN, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.HERBALIST, new Trial(TrialType.ROLE_ACTIONS, 500));
		TRIALS.put(Role.ENGINEER, new Trial(TrialType.ABILITY_USES, 200));
		TRIALS.put(Role.LANCER, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.SWORDSMAN, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.PALADIN, new Trial(TrialType.UNDEAD_KILLS, 150));
		TRIALS.put(Role.NECROMANCER, new Trial(TrialType.SOULS, 250));
		TRIALS.put(Role.DRUID, new Trial(TrialType.ROLE_ACTIONS, 300));
		TRIALS.put(Role.MERCHANT, new Trial(TrialType.ROLE_ACTIONS, 200));
		TRIALS.put(Role.MONK, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.ELEMENTALIST, new Trial(TrialType.ABILITY_USES, 300));
		TRIALS.put(Role.SHAPESHIFTER, new Trial(TrialType.ABILITY_USES, 250));
		TRIALS.put(Role.GUNSLINGER, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.WEAPON_SUMMONER, new Trial(TrialType.ABILITY_USES, 300));
		TRIALS.put(Role.HUNTER, new Trial(TrialType.WEAPON_KILLS, 150));
		TRIALS.put(Role.BARD, new Trial(TrialType.ROLE_ACTIONS, 300));
		TRIALS.put(Role.WARLOCK, new Trial(TrialType.ABILITY_USES, 300));
		TRIALS.put(Role.TAMER, new Trial(TrialType.ROLE_ACTIONS, 300));
	}

	private Evolution() {
	}

	public static Trial trial(Role role) {
		return TRIALS.get(role);
	}

	/** Título del rol según su nivel de evolución. */
	public static String titleKey(Role role, int tier) {
		return tier <= BASE ? role.translationKey() : "title." + Aptitudes.MOD_ID + "." + role.key() + "." + tier;
	}

	public static String signatureKey(Role role) {
		return "signature." + Aptitudes.MOD_ID + "." + role.key();
	}

	public static String ultimateKey(Role role) {
		return "ultimate." + Aptitudes.MOD_ID + "." + role.key();
	}
}
