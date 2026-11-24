package dev.aptitudes.logic;

import java.util.List;
import java.util.Map;

/**
 * V78: nombres de las animaciones del cuerpo del jugador (Player Animation Library). Cada una es una entrada de
 * {@code assets/aptitudes/player_animations/*.json}, generado por {@code tools/models/gen_player_animations.py}.
 * Una prueba comprueba que cada nombre existe en los archivos y que PAL los acepta.
 */
public final class PlayerAnimIds {
	public static final String BERSERKER_RAGE = "berserker_rage";
	public static final String BERSERKER_WAR_CRY = "berserker_war_cry";
	public static final String BERSERKER_LEAP = "berserker_leap";
	public static final String BERSERKER_SLAM = "berserker_slam";
	public static final String BERSERKER_WHIRL = "berserker_whirl";
	public static final String MAGE_BOLT = "mage_bolt";
	public static final String MAGE_BLINK = "mage_blink";
	public static final String MAGE_TIME_WARP = "mage_time_warp";
	public static final String MAGE_MISSILE_RIGHT = "mage_missile_r";
	public static final String MAGE_MISSILE_LEFT = "mage_missile_l";
	public static final String ASSASSIN_VANISH = "assassin_vanish";
	public static final String ASSASSIN_STEP = "assassin_step";
	public static final String ASSASSIN_FAN = "assassin_fan";
	public static final String ASSASSIN_SHURIKEN = "assassin_shuriken";
	public static final String HERBALIST_BALM = "herbalist_balm";
	public static final String HERBALIST_RAIN = "herbalist_rain";
	public static final String HERBALIST_SPORE = "herbalist_spore";
	public static final String ENGINEER_GRAPPLE = "engineer_grapple";
	public static final String ENGINEER_GRENADE = "engineer_grenade";
	public static final String LANCER_LEAP = "lancer_leap";
	public static final String LANCER_SLAM = "lancer_slam";
	public static final String LANCER_THRUST = "lancer_thrust";
	public static final String SWORDSMAN_IAIDO = "swordsman_iaido";
	public static final String SWORDSMAN_CUTS = "swordsman_cuts";
	public static final String PALADIN_CONSECRATE = "paladin_consecrate";
	public static final String PALADIN_HAMMER = "paladin_hammer";
	public static final String NECRO_SPEAR = "necromancer_spear";
	public static final String NECRO_SIPHON = "necromancer_siphon";
	public static final String DRUID_WOLVES = "druid_wolves";
	public static final String DRUID_THORNS = "druid_thorns";
	public static final String MERCHANT_COIN = "merchant_coin";
	public static final String MERCHANT_GUARD = "merchant_guard";
	public static final String MONK_KICK = "monk_kick";
	public static final String MONK_FISTS = "monk_fists";
	public static final String GUNSLINGER_SHOT = "gunslinger_shot";
	public static final String GUNSLINGER_DEADEYE = "gunslinger_deadeye";
	public static final String WEAPON_SUMMON = "weapon_summon";
	public static final String WEAPON_GREATSWORD = "weapon_greatsword";
	public static final String SHAPE_POUNCE = "shapeshifter_pounce";
	public static final String SHAPE_HOWL = "shapeshifter_howl";

	/**
	 * Tick (20 por segundo) en que cada animación llega a su golpe. Los efectos de partículas, cámara y pantalla se retrasan esos ticks
	 * para caer justo en el fotograma de impacto. Deben coincidir con {@code IMPACT_TICKS} de gen_player_animations.py.
	 */
	public static final Map<String, Integer> IMPACT_TICKS = Map.ofEntries(
			Map.entry(BERSERKER_RAGE, 4), Map.entry(BERSERKER_WAR_CRY, 8), Map.entry(BERSERKER_SLAM, 3),
			Map.entry(MAGE_BOLT, 4), Map.entry(MAGE_BLINK, 3), Map.entry(MAGE_TIME_WARP, 6), Map.entry(MAGE_MISSILE_RIGHT, 3),
			Map.entry(MAGE_MISSILE_LEFT, 3), Map.entry(ASSASSIN_VANISH, 3), Map.entry(ASSASSIN_STEP, 3), Map.entry(ASSASSIN_FAN, 4),
			Map.entry(ASSASSIN_SHURIKEN, 3), Map.entry(HERBALIST_BALM, 4), Map.entry(HERBALIST_RAIN, 5), Map.entry(HERBALIST_SPORE, 3),
			Map.entry(ENGINEER_GRAPPLE, 3), Map.entry(ENGINEER_GRENADE, 3),
			Map.entry(LANCER_LEAP, 3), Map.entry(LANCER_SLAM, 2), Map.entry(LANCER_THRUST, 3), Map.entry(SWORDSMAN_IAIDO, 3), Map.entry(SWORDSMAN_CUTS, 2), Map.entry(PALADIN_CONSECRATE, 5), Map.entry(PALADIN_HAMMER, 4),
			Map.entry(NECRO_SPEAR, 4), Map.entry(NECRO_SIPHON, 4), Map.entry(DRUID_WOLVES, 5), Map.entry(DRUID_THORNS, 4), Map.entry(MERCHANT_COIN, 3), Map.entry(MERCHANT_GUARD, 4), Map.entry(MONK_KICK, 3), Map.entry(MONK_FISTS, 2),
			Map.entry(GUNSLINGER_SHOT, 2), Map.entry(GUNSLINGER_DEADEYE, 4), Map.entry(WEAPON_SUMMON, 4), Map.entry(WEAPON_GREATSWORD, 4), Map.entry(SHAPE_POUNCE, 3), Map.entry(SHAPE_HOWL, 5));

	public static final List<String> ALL = List.of(BERSERKER_RAGE, BERSERKER_WAR_CRY, BERSERKER_LEAP, BERSERKER_SLAM, BERSERKER_WHIRL,
			MAGE_BOLT, MAGE_BLINK, MAGE_TIME_WARP, MAGE_MISSILE_RIGHT, MAGE_MISSILE_LEFT, ASSASSIN_VANISH, ASSASSIN_STEP, ASSASSIN_FAN, ASSASSIN_SHURIKEN,
			HERBALIST_BALM, HERBALIST_RAIN, HERBALIST_SPORE,
			ENGINEER_GRAPPLE, ENGINEER_GRENADE,
			LANCER_LEAP, LANCER_SLAM, LANCER_THRUST, SWORDSMAN_IAIDO, SWORDSMAN_CUTS, PALADIN_CONSECRATE, PALADIN_HAMMER,
			NECRO_SPEAR, NECRO_SIPHON, DRUID_WOLVES, DRUID_THORNS, MERCHANT_COIN, MERCHANT_GUARD, MONK_KICK, MONK_FISTS,
			GUNSLINGER_SHOT, GUNSLINGER_DEADEYE, WEAPON_SUMMON, WEAPON_GREATSWORD, SHAPE_POUNCE, SHAPE_HOWL);

	private PlayerAnimIds() {
	}
}
