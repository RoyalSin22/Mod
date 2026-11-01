package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;

import java.util.Locale;

/**
 * Recurso propio de cada rol (además del Maná común). Se carga jugando al estilo
 * del rol (toda XP de rol lo carga) y algunos tienen cargas extra por segundo.
 * Los recursos "de combate" se disipan si pasas unos segundos sin cargarlos.
 *
 * <p>Al lanzar una habilidad del rol con el recurso al {@link #EMPOWER_COST} o más,
 * se consume y la habilidad sale potenciada (+1 rango).</p>
 */
public enum RoleResource {
	FURY(0xE53935, true),        // Bersérker: golpear y RECIBIR golpes
	FOCUS(0x7E57C2, false),      // Mago: se acumula mientras no te hieren
	COMBO(0x90A4AE, true),       // Asesino
	SAP(0x66BB6A, false),        // Herbolista
	CHARGE(0xFF9800, false),     // Ingeniero
	MOMENTUM(0x42A5F5, true),    // Lancero: también esprintando
	RHYTHM(0xCFD8DC, true),      // Espadachín
	DEVOTION(0xFFD54F, false),   // Paladín: también al recibir golpes
	BLOOD(0xB71C1C, false),      // Nigromante: daño propio, de esbirros y muertes cercanas
	VERDANCE(0x558B2F, false),   // Druida: también estando en bosques
	GOLD(0xFFC107, false),       // Mercader
	CHI(0xFF7043, true),         // Monje: también meditando (agachado e inmóvil)
	RESONANCE(0x29B6F6, false),  // Elementalista
	ESSENCE(0xBA68C8, false),    // Cambiaformas: también estando transformado
	CYLINDER(0x26C6DA, true),    // Mago pistolero
	ARSENAL(0xB0BEC5, false),    // Invocador de armas
	TRACKING(0x9CCC65, true),    // Cazador
	HARMONY(0xF48FB1, false),    // Bardo
	CORRUPTION(0xC2185B, false), // Brujo
	BOND(0xBCAAA4, false);       // Domador

	public static final float MAX = 100f;
	public static final float EMPOWER_COST = 50f;

	private final int color;
	private final boolean decays;

	RoleResource(int color, boolean decays) {
		this.color = color;
		this.decays = decays;
	}

	public int color() { return color; }
	public boolean decays() { return decays; }

	public String translationKey() {
		return "resource." + Aptitudes.MOD_ID + "." + name().toLowerCase(Locale.ROOT);
	}

	public static RoleResource of(Role role) {
		return switch (role) {
			case BERSERKER -> FURY;
			case MAGE -> FOCUS;
			case ASSASSIN -> COMBO;
			case HERBALIST -> SAP;
			case ENGINEER -> CHARGE;
			case LANCER -> MOMENTUM;
			case SWORDSMAN -> RHYTHM;
			case PALADIN -> DEVOTION;
			case NECROMANCER -> BLOOD;
			case DRUID -> VERDANCE;
			case MERCHANT -> GOLD;
			case MONK -> CHI;
			case ELEMENTALIST -> RESONANCE;
			case SHAPESHIFTER -> ESSENCE;
			case GUNSLINGER -> CYLINDER;
			case WEAPON_SUMMONER -> ARSENAL;
			case HUNTER -> TRACKING;
			case BARD -> HARMONY;
			case WARLOCK -> CORRUPTION;
			case TAMER -> BOND;
		};
	}
}
