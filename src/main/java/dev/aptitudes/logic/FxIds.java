package dev.aptitudes.logic;

/** V77: identificadores de los eventos de efectos que el servidor manda a los clientes ({@code FxEventPayload}). */
public final class FxIds {
	public static final int BERSERKER_RAGE = 1;
	public static final int BERSERKER_WAR_CRY = 3;
	public static final int BERSERKER_LEAP_TAKEOFF = 4;
	public static final int BERSERKER_LEAP_TRAIL = 5;
	public static final int BERSERKER_SLAM = 6;
	public static final int BERSERKER_WHIRL = 7;
	public static final int MAGE_BOLT = 8;
	public static final int MAGE_BLINK = 9;
	public static final int MAGE_TIME_WARP = 10;
	/** Un evento por misil: {@code power} = índice del misil (alterna el brazo). */
	public static final int MAGE_MISSILE = 11;
	/** Mayor id válido; el cliente descarta lo desconocido. */
	public static final int MAX = 11;

	private FxIds() {
	}

	public static boolean valid(int id) {
		return id >= 1 && id <= MAX;
	}
}
