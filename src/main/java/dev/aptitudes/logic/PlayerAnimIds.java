package dev.aptitudes.logic;

import java.util.List;

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

	public static final List<String> ALL = List.of(BERSERKER_RAGE, BERSERKER_WAR_CRY, BERSERKER_LEAP, BERSERKER_SLAM, BERSERKER_WHIRL);

	private PlayerAnimIds() {
	}
}
