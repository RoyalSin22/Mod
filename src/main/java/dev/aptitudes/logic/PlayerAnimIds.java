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

	/**
	 * Tick (20 por segundo) en que cada animación llega a su golpe. Los efectos de partículas, cámara y pantalla se retrasan esos ticks
	 * para caer justo en el fotograma de impacto. Deben coincidir con {@code IMPACT_TICKS} de gen_player_animations.py.
	 */
	public static final Map<String, Integer> IMPACT_TICKS = Map.ofEntries(
			Map.entry(BERSERKER_RAGE, 4), Map.entry(BERSERKER_WAR_CRY, 8), Map.entry(BERSERKER_SLAM, 3),
			Map.entry(MAGE_BOLT, 4), Map.entry(MAGE_BLINK, 3), Map.entry(MAGE_TIME_WARP, 6), Map.entry(MAGE_MISSILE_RIGHT, 3),
			Map.entry(MAGE_MISSILE_LEFT, 3), Map.entry(ASSASSIN_VANISH, 3), Map.entry(ASSASSIN_STEP, 3), Map.entry(ASSASSIN_FAN, 4),
			Map.entry(ASSASSIN_SHURIKEN, 3));

	public static final List<String> ALL = List.of(BERSERKER_RAGE, BERSERKER_WAR_CRY, BERSERKER_LEAP, BERSERKER_SLAM, BERSERKER_WHIRL,
			MAGE_BOLT, MAGE_BLINK, MAGE_TIME_WARP, MAGE_MISSILE_RIGHT, MAGE_MISSILE_LEFT, ASSASSIN_VANISH, ASSASSIN_STEP, ASSASSIN_FAN, ASSASSIN_SHURIKEN);

	private PlayerAnimIds() {
	}
}
