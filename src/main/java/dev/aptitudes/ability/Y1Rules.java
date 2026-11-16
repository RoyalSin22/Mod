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
			"lunge", "dragon_leap", "pounce", "flying_kick");
	private static final Set<String> DAMAGE = Set.of("whirlwind", "fireball", "arcane_missiles", "fan_of_knives",
			"shuriken", "pulse_grenade", "sweep", "piercing_thrust", "elemental_bolt", "elemental_burst",
			"elemental_barrage", "arcane_shot", "fan_the_hammer", "ricochet", "deadeye", "arrow_volley", "falcon_strike",
			"chi_palm", "hundred_fists", "death_touch", "bone_spear", "judgement", "hammer_of_justice", "blade_dance",
			"iaido", "thousand_cuts", "curse_of_agony", "abyssal_bolt", "doom", "blade_volley", "sword_rain",
			"spectral_greatsword", "thorn_storm");
	private static final Set<String> SUMMON = Set.of("raise_minions", "summon_wolves", "summon_imps", "turret", "sic_em",
			"stampede");

	private Y1Rules() {
	}

	public static Category categoryOf(String abilityId) {
		if (abilityId == null) return Category.NONE;
		if (MOVEMENT.contains(abilityId)) return Category.MOVEMENT;
		if (DAMAGE.contains(abilityId)) return Category.DAMAGE;
		if (SUMMON.contains(abilityId)) return Category.SUMMON;
		return Category.NONE;
	}

	/** SPEED en la senda: más radio en las activas de movimiento. */
	public static float radius(PathProfile p, String abilityId, double configured) {
		return p.speed() && categoryOf(abilityId) == Category.MOVEMENT ? (float) configured : 1.0f;
	}

	/** CRIT o EXECUTE: más daño en las ofensivas; MINION_POWER: más daño en las invocaciones. */
	public static float damage(PathProfile p, String abilityId, double configured) {
		Category c = categoryOf(abilityId);
		if (c == Category.DAMAGE && (p.crit() || p.execute())) return (float) configured;
		if (c == Category.SUMMON && p.minionPower()) return (float) configured;
		return 1.0f;
	}

	/** (REGEN de maná o vida máxima) y ganancia de recurso: las activas cuestan menos. */
	public static float energy(PathProfile p, double configured) {
		return (p.manaRegen() || p.maxHealth()) && p.resourceGain() ? (float) configured : 1.0f;
	}
}
