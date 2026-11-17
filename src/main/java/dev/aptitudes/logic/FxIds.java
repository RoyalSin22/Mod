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
	public static final int ASSASSIN_VANISH = 12;
	/** Origen en x,y,z y destino = origen + dirección. */
	public static final int ASSASSIN_STEP = 13;
	/** {@code power} = radio del abanico. */
	public static final int ASSASSIN_FAN = 14;
	/** Una cuchilla que vuela del jugador a {@code eye + dir}; {@code power} = índice. */
	public static final int ASSASSIN_KNIFE = 15;
	/** Shuriken: {@code power} 1 si acierta, 0 si falla. */
	public static final int ASSASSIN_SHURIKEN = 16;
	/** Mayor id válido; el cliente descarta lo desconocido. */
	public static final int MAX = 16;

	private FxIds() {
	}

	public static boolean valid(int id) {
		return id >= 1 && id <= MAX;
	}
}
