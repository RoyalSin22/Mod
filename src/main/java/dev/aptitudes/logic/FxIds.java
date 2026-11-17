package dev.aptitudes.logic;

/** V77: identificadores de los eventos de efectos que el servidor manda a los clientes ({@code FxEventPayload}). */
public final class FxIds {
	public static final int BERSERKER_RAGE = 1;
	public static final int BERSERKER_WAR_CRY = 3;
	public static final int BERSERKER_LEAP_TAKEOFF = 4;
	public static final int BERSERKER_LEAP_TRAIL = 5;
	public static final int BERSERKER_SLAM = 6;
	public static final int BERSERKER_WHIRL = 7;
	/** Mayor id válido; el cliente descarta lo desconocido. */
	public static final int MAX = 7;

	private FxIds() {
	}

	public static boolean valid(int id) {
		return id >= 1 && id <= MAX;
	}
}
