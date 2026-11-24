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
	/** Bálsamo: en quien lanza ({@code power} = radio). */
	public static final int HERBALIST_BALM = 17;
	/** Bálsamo recibido por un aliado (la entidad es el aliado). */
	public static final int HERBALIST_BALM_ALLY = 18;
	/** Lluvia sanadora en el punto del evento; {@code power} = radio y {@code dx} = duración en ticks. */
	public static final int HERBALIST_RAIN = 19;
	/** Nube de esporas: el evento sale de la mano y {@code dx,dy,dz} apunta al centro; {@code power} = radio. */
	public static final int HERBALIST_SPORE = 20;
	/** Garfio: {@code dx,dy,dz} = del ojo al punto de anclaje. */
	public static final int ENGINEER_GRAPPLE = 21;
	/** Granada de pulso: {@code dx,dy,dz} = del ojo al punto de explosión; {@code power} = ticks de espoleta. */
	public static final int ENGINEER_GRENADE = 22;
	/** Salto del dragón: {@code power} = ticks de vuelo hasta el aterrizaje. */
	public static final int LANCER_LEAP = 23;
	/** Aterrizaje del dragón en el punto del evento; {@code power} = radio. */
	public static final int LANCER_SLAM = 24;
	/** Estocada perforante: {@code dx,dy,dz} = del ojo al final; {@code power} 1 si acertó. */
	public static final int LANCER_THRUST = 25;
	/** Iaido: {@code dx,dy,dz} = vector del tajo desde el origen. */
	public static final int SWORDSMAN_IAIDO = 26;
	/** Un corte de Mil cortes en el punto del evento; {@code power} = índice (el 0 inicia la animación). */
	public static final int SWORDSMAN_CUT = 27;
	/** Consagración en el punto del evento; {@code power} = radio y {@code dx} = duración en ticks. */
	public static final int PALADIN_CONSECRATE = 28;
	/** Martillo de la justicia: {@code dx,dy,dz} = del ojo al final; {@code power} 1 si acertó. */
	public static final int PALADIN_HAMMER = 29;
	/** Lanza de hueso: {@code dx,dy,dz} = del ojo al final; {@code power} 1 si acertó. */
	public static final int NECRO_SPEAR = 30;
	/** Sifón de almas: en quien lo lanza; {@code power} = radio. */
	public static final int NECRO_SIPHON = 31;
	/** Un hilo de alma desde el punto del evento (el enemigo) hacia {@code dx,dy,dz} (el nigromante). */
	public static final int NECRO_SIPHON_LINK = 32;
	/** Manada: en quien lo lanza. */
	public static final int DRUID_WOLVES = 33;
	/** Un lobo aparece en el punto del evento. */
	public static final int DRUID_WOLF_SPAWN = 34;
	/** Tormenta de espinas en quien la lanza; {@code power} = radio. */
	public static final int DRUID_THORNS = 35;
	/** Moneda: {@code dx,dy,dz} = del ojo al objetivo. */
	public static final int MERCHANT_COIN = 36;
	/** Guardia dorada en quien la lanza. */
	public static final int MERCHANT_GUARD = 37;
	/** Patada voladora: {@code dx,dy,dz} = vector del desplazamiento; {@code power} 1 si golpeó. */
	public static final int MONK_KICK = 38;
	/** Un puñetazo en el punto del evento; {@code power} = índice (el 0 inicia la animación). */
	public static final int MONK_FIST = 39;
	/** Mayor id válido; el cliente descarta lo desconocido. */
	public static final int MAX = 39;

	private FxIds() {
	}

	public static boolean valid(int id) {
		return id >= 1 && id <= MAX;
	}
}
