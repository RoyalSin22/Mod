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

	/**
	 * Tick (20 por segundo) en que cada animación llega a su golpe. Los efectos de partículas, cámara y pantalla se retrasan esos ticks
	 * para caer justo en el fotograma de impacto. Deben coincidir con {@code IMPACT_TICKS} de gen_player_animations.py.
	 */
	public static final Map<String, Integer> IMPACT_TICKS = Map.of(BERSERKER_RAGE, 4, BERSERKER_WAR_CRY, 8, BERSERKER_SLAM, 3);

	public static final List<String> ALL = List.of(BERSERKER_RAGE, BERSERKER_WAR_CRY, BERSERKER_LEAP, BERSERKER_SLAM, BERSERKER_WHIRL);

	private PlayerAnimIds() {
	}
}
