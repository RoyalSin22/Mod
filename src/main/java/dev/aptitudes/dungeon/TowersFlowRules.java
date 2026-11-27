package dev.aptitudes.dungeon;

import dev.aptitudes.dungeon.TowersLayout.Role;
import dev.aptitudes.dungeon.TowersLayout.Tower;

/**
 * V86 · fase 3: reglas puras del flujo de Las Tres Torres Señoriales (sin Minecraft, con JUnit en {@code TowersFlowRulesTest}).
 *
 * <p>Los jefes están «dormidos» porque <b>no existen</b> hasta que un jugador entra en su sala (como el Autómata del Custodio).
 * Cada jefe pasa de {@link Boss#ASLEEP} a {@link Boss#AWAKE} y a {@link Boss#DEFEATED} y <b>nunca retrocede</b>: una sola vez por
 * estructura. Tras vencer a los dos Guardianes se habilita el Coloso; solo las fortalezas V88 ya
 * generadas tenían una reja física. La dificultad es alta: el anillo de Amenaza efectivo
 * sube uno sobre el del lugar con un mínimo de {@link #MIN_RING}.</p>
 */
public final class TowersFlowRules {
	/** Anillo de Amenaza mínimo y máximo de la estructura (decisión de diseño; ver {@code docs/V86_DUNGEON_COLOSO.md} §6). */
	public static final int MIN_RING = 3;
	public static final int MAX_RING = 5;
	/** Vida extra por anillo (multiplicador de la base): jefes y vasallos. */
	public static final double BOSS_HEALTH_PER_RING = 0.25;
	public static final double MINION_HEALTH_PER_RING = 0.15;
	/** Comprobaciones seguidas sin ver a un jefe vivo antes de volver a hacerlo aparecer (cada una dura medio segundo). */
	public static final int MISSING_CHECKS = 6;
	/** Cofres de recompensa como mucho: uno por jugador cercano. */
	public static final int MAX_REWARD_CHESTS = 5;

	private TowersFlowRules() {}

	/** Estado de un jefe. Se guarda por ordinal: solo se añade al final. */
	public enum Boss { ASLEEP, AWAKE, DEFEATED }

	/** Tipos de criatura de las oleadas (los equipa y escala el código con Minecraft). */
	public enum MobKind { ZOMBIE, SKELETON, HUSK, STRAY }

	/** Anillo efectivo: +1 sobre el del lugar, entre {@link #MIN_RING} y {@link #MAX_RING}. */
	public static int effectiveRing(int placeRing) {
		return Math.max(MIN_RING, Math.min(MAX_RING, Math.max(0, placeRing) + 1));
	}

	public static double bossHealthBonus(int ring) {
		return Math.max(0, Math.min(MAX_RING, ring)) * BOSS_HEALTH_PER_RING;
	}

	public static double minionHealthBonus(int ring) {
		return Math.max(0, Math.min(MAX_RING, ring)) * MINION_HEALTH_PER_RING;
	}

	/** ¿Despierta el jefe? Solo si duerme y hay un jugador en su sala. */
	public static boolean shouldWake(Boss state, boolean playerInRoom) {
		return state == Boss.ASLEEP && playerInRoom;
	}

	/** El encuentro del Mariscal se habilita cuando los dos Guardianes han caído. */
	public static boolean lordDoorOpen(Boss west, Boss east) {
		return west == Boss.DEFEATED && east == Boss.DEFEATED;
	}

	/** El Coloso despierta si el controlador ha habilitado el encuentro, duerme y hay un jugador en su sala. */
	public static boolean shouldWakeColossus(boolean doorOpen, Boss colossus, boolean playerInRoom) {
		return doorOpen && shouldWake(colossus, playerInRoom);
	}

	/** Un estado solo avanza: un valor menor o igual que el actual no lo cambia (protege ante mensajes repetidos). */
	public static Boss advance(Boss current, Boss next) {
		return next.ordinal() > current.ordinal() ? next : current;
	}

	// ------------------------------------------------------------------------------------------ Salas

	/** ¿Está el punto, relativo al controlador, dentro de la sala interior de la torre {@code t} a la altura {@code floorY}..{@code topY}? */
	public static boolean inRoom(Tower t, int dx, int dy, int dz, int floorY, int topY) {
		int inner = t.half() - 1;
		return Math.abs(dx - t.cx()) <= inner && Math.abs(dz - t.cz()) <= inner && dy >= floorY + 1 && dy <= topY;
	}

	/** Planta de combate (0 = baja) en la que está el punto, o -1 si no está en ninguna sala de combate de la torre. */
	public static int combatLevel(Tower t, int dx, int dy, int dz) {
		for (int level = 0; level < t.combatLevels(); level++) {
			int floor = level * TowersLayout.FLOOR_H;
			if (inRoom(t, dx, dy, dz, floor, floor + TowersLayout.FLOOR_H - 1)) return level;
		}
		return -1;
	}

	/** Sala del jefe de la torre (Guardián o Coloso). */
	public static boolean inBossRoom(Tower t, int dx, int dy, int dz) {
		return inRoom(t, dx, dy, dz, t.bossFloorY(), t.roofY() - 1);
	}

	// ------------------------------------------------------------------------------------------ Oleadas

	/** Índice de bit de la planta {@code level} de la torre {@code role}, para guardar qué oleadas ya salieron. */
	public static int waveBit(Role role, int level) {
		int base = switch (role) {
			case VASSAL_WEST -> 0;
			case VASSAL_EAST -> 3;
			case LORD -> 6;
		};
		return base + level;
	}

	public static boolean waveDone(int mask, Role role, int level) {
		return (mask & (1 << waveBit(role, level))) != 0;
	}

	public static int withWave(int mask, Role role, int level) {
		return mask | (1 << waveBit(role, level));
	}

	/** Criaturas que salen en cada punto de aparición de una planta: más en las plantas altas y con el anillo máximo. */
	public static int mobsPerSpawn(int level, int ring) {
		return 1 + (level >= 2 ? 1 : 0) + (ring >= MAX_RING ? 1 : 0);
	}

	/** Tipo de criatura de una oleada: alterna cuerpo a cuerpo y arco, y las plantas altas usan las variantes del frío y del desierto. */
	public static MobKind mobKind(int level, int index) {
		boolean melee = Math.floorMod(level + index, 2) == 0;
		boolean variant = level >= 1;
		return melee ? (variant ? MobKind.HUSK : MobKind.ZOMBIE) : (variant ? MobKind.STRAY : MobKind.SKELETON);
	}
}
