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
	/** Disparo del Mago pistolero desde el punto del evento (la boca del arma) hasta {@code dx,dy,dz}; {@code power}: bits 0-1 variante
	 * (0 arcano, 1 rebote, 2 certero, 3 ráfaga) y bit 4 (16) = inicia la animación. */
	public static final int GUNSLINGER_SHOT = 40;
	/** Marca de Ojo de halcón sobre el punto del evento; {@code power} 16 inicia la animación. */
	public static final int GUNSLINGER_MARK = 41;
	/** Espada espectral que vuela desde el punto del evento hasta {@code dx,dy,dz}; {@code power}: 0 andanada, 1 lluvia; +16 inicia la animación. */
	public static final int WEAPON_BLADE = 42;
	/** Tajo del espadón: {@code dx,dy,dz} = del ojo al final. */
	public static final int WEAPON_GREATSWORD = 43;
	/** Prisión de espadas sobre el punto del evento; {@code power} 16 inicia la animación. */
	public static final int WEAPON_PRISON = 44;
	/** Abalanzarse: {@code dx,dy,dz} = desplazamiento hacia el objetivo. */
	public static final int SHAPE_POUNCE = 45;
	/** Aullido de la bestia en quien lo lanza; {@code power} = radio. */
	public static final int SHAPE_HOWL = 46;
	/** Marca del cazador sobre el punto del evento (la presa); {@code dx,dy,dz} = del ojo a la presa. */
	public static final int HUNTER_MARK = 47;
	/** Trampa de oso en el punto del evento; {@code power} 0 = colocada, 1 = se cierra. */
	public static final int HUNTER_TRAP = 48;
	/** Una flecha de la lluvia desde el punto del evento hasta {@code dx,dy,dz}; {@code power} 16 = inicia la animación. */
	public static final int HUNTER_ARROW = 49;
	/** Un picotazo del halcón sobre el punto del evento; {@code power} = índice (el 0 inicia la animación). */
	public static final int HUNTER_FALCON = 50;
	/** Canción del bardo en quien la toca; {@code power} = radio, {@code dx} = variante (0 valor, 1 escudo, 2 réquiem). */
	public static final int BARD_SONG = 51;
	/** Disonancia en quien la toca; {@code power} = radio. */
	public static final int BARD_DISSONANCE = 52;
	/** Maldición de agonía: {@code dx,dy,dz} del ojo a la víctima (punto del evento). */
	public static final int WARLOCK_CURSE = 53;
	/** Un latido de la maldición sobre el punto del evento. */
	public static final int WARLOCK_PULSE = 54;
	/** Rayo abismal: {@code dx,dy,dz} del ojo al final; {@code power} 1 si acertó. */
	public static final int WARLOCK_BOLT = 55;
	/** Diablillos en quien los invoca. */
	public static final int WARLOCK_IMPS = 56;
	/** Perdición sobre el punto del evento; {@code power} 0 = marca, 1 = sentencia. */
	public static final int WARLOCK_DOOM = 57;
	/** Orden del domador en quien la da; {@code power} = variante (0 acosar, 1 vínculo, 2 rugido del alfa). */
	public static final int TAMER_COMMAND = 58;
	/** Efecto sobre una mascota en el punto del evento; {@code power} 0 = azuzada, 1 = vinculada. */
	public static final int TAMER_PET = 59;
	/** Estampida: {@code dx,dy,dz} del ojo al final; {@code power} 1 si acertó. */
	public static final int TAMER_STAMPEDE = 60;
	/** Mayor id válido; el cliente descarta lo desconocido. */
	public static final int MAX = 60;

	private FxIds() {
	}

	public static boolean valid(int id) {
		return id >= 1 && id <= MAX;
	}
}
