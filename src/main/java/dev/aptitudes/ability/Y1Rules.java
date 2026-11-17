package dev.aptitudes.ability;

import java.util.Set;

/**
 * v65 C5 Y1 (puro): qué activas reciben qué bonificación según los efectos de la senda elegida del rol.
 * Clasifica las 80 activas base por id exacto; las activas de las filas nuevas y evolucionadas no están
 * clasificadas todavía y no reciben bonificación (ver {@link Category#NONE}).
 */
public final class Y1Rules {
	public enum Category { MOVEMENT, DAMAGE, SUMMON, NONE }

	/** Efectos de senda relevantes para Y1 que tiene el rol (nodos de su senda con rango). */
	public record PathProfile(boolean speed, boolean crit, boolean execute, boolean minionPower,
							  boolean manaRegen, boolean maxHealth, boolean resourceGain) {
		public static final PathProfile EMPTY = new PathProfile(false, false, false, false, false, false, false);
	}

	private static final Set<String> MOVEMENT = Set.of("shadow_step", "blink", "thrusters", "grapple", "leap_slam",
			"lunge", "dragon_leap", "pounce", "flying_kick", "assassin_n4", "assassin_n6", "swordsman_n6", "berserker_n10");
	private static final Set<String> DAMAGE = Set.of("whirlwind", "fireball", "arcane_missiles", "fan_of_knives",
			"shuriken", "pulse_grenade", "sweep", "piercing_thrust", "elemental_bolt", "elemental_burst",
			"elemental_barrage", "arcane_shot", "fan_the_hammer", "ricochet", "deadeye", "arrow_volley", "falcon_strike",
			"chi_palm", "hundred_fists", "death_touch", "bone_spear", "judgement", "hammer_of_justice", "blade_dance",
			"iaido", "thousand_cuts", "curse_of_agony", "abyssal_bolt", "doom", "blade_volley", "sword_rain",
			"spectral_greatsword", "thorn_storm",
			// Saltos y embestidas que también hacen daño al caer.
			"leap_slam", "dragon_leap", "pounce", "flying_kick", "lunge", "assassin_n4", "assassin_n6", "swordsman_n6",
			"berserker_n10",
			// Activas nuevas (v67) ofensivas.
			"berserker_n3", "berserker_n6", "mage_n3", "mage_n6", "mage_n10", "herbalist_n3", "herbalist_n6", "engineer_n3",
			"engineer_n10", "lancer_n3", "lancer_n4", "lancer_n6", "lancer_n10", "swordsman_n3", "swordsman_n4",
			"swordsman_n10", "paladin_n3", "paladin_n4", "paladin_n10", "necromancer_n3", "necromancer_n6",
			"necromancer_n10", "druid_n3", "druid_n6", "druid_n10", "merchant_n3", "merchant_n10", "monk_n3", "monk_n4",
			"monk_n6", "monk_n10", "shapeshifter_n3", "shapeshifter_n6", "shapeshifter_n10", "gunslinger_n3",
			"gunslinger_n4", "gunslinger_n6", "gunslinger_n10", "weapon_summoner_n3", "weapon_summoner_n4",
			"weapon_summoner_n6", "weapon_summoner_n10", "hunter_n4", "hunter_n6", "hunter_n10", "bard_n3", "bard_n6",
			"bard_n10", "warlock_n3", "warlock_n6", "warlock_n10", "elementalist_n3", "elementalist_n4", "elementalist_n6",
			"elementalist_n10");
	private static final Set<String> SUMMON = Set.of("raise_minions", "summon_wolves", "summon_imps", "turret", "sic_em",
			"stampede", "engineer_n4", "tamer_n4");
	/** Plantillas de las activas evolucionadas (Maestro y Definitivo) según su naturaleza. */
	private static final Set<String> TEMPLATE_DAMAGE = Set.of("NOVA", "BEAM", "CONE", "ZONE", "DASH");
	private static final Set<String> TEMPLATE_MOVEMENT = Set.of("DASH");
	private static final Set<String> TEMPLATE_SUMMON = Set.of("SUMMON");

	private Y1Rules() {
	}

	/**
	 * ¿La activa pertenece a la categoría? Una activa puede tener varias (un salto que también daña).
	 *
	 * @param template nombre de la plantilla si es una activa evolucionada (NOVA, DASH…), o {@code null}
	 */
	public static boolean has(String abilityId, String template, Category category) {
		if (abilityId == null || category == Category.NONE) return false;
		boolean evolved = template != null;
		return switch (category) {
			case MOVEMENT -> MOVEMENT.contains(abilityId) || (evolved && TEMPLATE_MOVEMENT.contains(template));
			case DAMAGE -> DAMAGE.contains(abilityId) || (evolved && TEMPLATE_DAMAGE.contains(template));
			case SUMMON -> SUMMON.contains(abilityId) || (evolved && TEMPLATE_SUMMON.contains(template));
			case NONE -> false;
		};
	}

	/** SPEED en la senda: más radio en las activas de movimiento. */
	public static float radius(PathProfile p, String abilityId, String template, double configured) {
		return p.speed() && has(abilityId, template, Category.MOVEMENT) ? (float) configured : 1.0f;
	}

	/** CRIT o EXECUTE: más daño en las ofensivas; MINION_POWER: más daño en las invocaciones. Se aplican los dos si valen. */
	public static float damage(PathProfile p, String abilityId, String template, double configured) {
		float bonus = 1.0f;
		if ((p.crit() || p.execute()) && has(abilityId, template, Category.DAMAGE)) bonus *= (float) configured;
		if (p.minionPower() && has(abilityId, template, Category.SUMMON)) bonus *= (float) configured;
		return bonus;
	}

	/** (REGEN de maná o vida máxima) y ganancia de recurso: las activas cuestan menos. */
	public static float energy(PathProfile p, double configured) {
		return (p.manaRegen() || p.maxHealth()) && p.resourceGain() ? (float) configured : 1.0f;
	}
}
