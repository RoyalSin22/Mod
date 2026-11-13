package dev.aptitudes.logic;

import dev.aptitudes.logic.TreeScreenLayout.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometría del árbol de habilidades unificado (v67): Talentos, Sendas, Veterano, Maestro, Ascenso, Definitivo y
 * Maestría apilados en un solo lienzo. Lógica pura (sin Minecraft), con pruebas. Coordenadas del lienzo, antes del zoom
 * y del desplazamiento; el origen es la esquina superior izquierda del contenido.
 *
 * <p>Cada sección es un bloque con una cabecera de {@value #HEADER_H} px y su cuerpo debajo. Las secciones sin nodos
 * (Veterano y Ascenso hasta que existan sus habilidades) no ocupan sitio.</p>
 */
public final class UnifiedTreeLayout {
	public static final int WIDTH = 336;
	public static final int NODE = 24;
	public static final int HEADER_H = 20;
	public static final int GAP = 14;
	/** Alto del cuerpo de Talentos: la cuadrícula clásica (núcleo arriba y seis filas de 32 px). */
	public static final int TALENTS_H = 242;
	/** Sendas: cabeceras de columna de 96×24 y siete escalones cada 32 px (nodo de 24 y 8 de hueco). */
	public static final int PATH_HEAD_H = 28;
	public static final int PITCH = 32;
	public static final int PATH_ROWS = 7;
	public static final int BIFURCATION_ROW = 4;
	/** Separación de las dos opciones de S5 respecto al centro de la columna: las patas de la bifurcación del kit. */
	public static final int BRANCH = 22;
	public static final int PATHS_H = PATH_HEAD_H + PATH_ROWS * PITCH;
	/** Maestro y Definitivo: firma y arma arriba, título del carril y fila de mejoras debajo. */
	public static final int EVO_H = 116;
	public static final int EVO_TOP_Y = 8;
	public static final int EVO_LANE_Y = 52;
	public static final int EVO_ROW_Y = 76;
	public static final int EVO_PITCH = 44;
	/** Secciones puente (Veterano y Ascenso): una fila de nodos. */
	public static final int BRIDGE_H = 40;
	/** Maestría: barra de 20 casillas (320×18) y el número de puntos. */
	public static final int MASTERY_H = 30;
	public static final int MASTERY_W = 320;

	public enum Section { TALENTS, PATHS, VETERAN, MASTER, ASCENT, ULTIMATE, MASTERY }

	/** Bloque de una sección: {@code y} es la parte superior de su cabecera. */
	public record Block(Section section, int y, int height) {
		/** Parte superior del cuerpo, bajo la cabecera. */
		public int bodyY() { return y + HEADER_H + 4; }
		public int bottom() { return y + height; }
	}

	private UnifiedTreeLayout() {
	}

	/** Bloques en orden; Veterano y Ascenso solo aparecen si tienen nodos. */
	public static List<Block> blocks(int veteranNodes, int ascentNodes) {
		List<Block> out = new ArrayList<>();
		int y = 0;
		for (Section s : Section.values()) {
			int body = switch (s) {
				case TALENTS -> TALENTS_H;
				case PATHS -> PATHS_H;
				case VETERAN -> veteranNodes > 0 ? BRIDGE_H : 0;
				case MASTER, ULTIMATE -> EVO_H;
				case ASCENT -> ascentNodes > 0 ? BRIDGE_H : 0;
				case MASTERY -> MASTERY_H;
			};
			if (body == 0) continue;
			int h = HEADER_H + 4 + body;
			out.add(new Block(s, y, h));
			y += h + GAP;
		}
		return out;
	}

	public static int contentHeight(List<Block> blocks) {
		return blocks.isEmpty() ? 0 : blocks.getLast().bottom();
	}

	/** Bloque de una sección, o {@code null} si no está (sin nodos). */
	public static Block block(List<Block> blocks, Section section) {
		for (Block b : blocks) if (b.section() == section) return b;
		return null;
	}

	// ------------------------------------------------------------ Sendas

	/** Centro horizontal de la columna de la senda {@code path} (0–2). */
	public static int pathCenterX(int path) {
		return WIDTH * (2 * path + 1) / 6;
	}

	/** Cabecera de la senda (pieza de 96×24 del kit), centrada en su columna. */
	public static Rect pathHeader(Block paths, int path) {
		return new Rect(pathCenterX(path) - 48, paths.bodyY(), 96, 24);
	}

	/**
	 * Nodo de una senda: {@code row} 0–6 (S1–S7); en la fila de la bifurcación, {@code slot} 0 es S5x (izquierda) y 1
	 * S5y (derecha).
	 */
	public static Rect pathNode(Block paths, int path, int row, int slot) {
		int cx = pathCenterX(path);
		if (row == BIFURCATION_ROW) cx += slot == 0 ? -BRANCH : BRANCH;
		return new Rect(cx - NODE / 2, paths.bodyY() + PATH_HEAD_H + row * PITCH, NODE, NODE);
	}

	/** Conector vertical (12×32) entre el escalón {@code row} y el siguiente: línea en los px 6–7 sobre el centro. */
	public static Rect link(Block paths, int path, int row) {
		Rect upper = pathNode(paths, path, row, 0);
		return new Rect(upper.x() + 5, upper.y() + 12, 12, 32);
	}

	/** Bifurcación S4 → S5x/S5y (64×32): tallo en el px 32, patas en 10 y 54. */
	public static Rect fork(Block paths, int path) {
		Rect s4 = pathNode(paths, path, BIFURCATION_ROW - 1, 0);
		return new Rect(s4.x() - 21, s4.y() + 12, 64, 32);
	}

	/** Unión S5x/S5y → S6: la bifurcación volteada. */
	public static Rect join(Block paths, int path) {
		Rect s5 = pathNode(paths, path, BIFURCATION_ROW, 0);
		Rect s6 = pathNode(paths, path, BIFURCATION_ROW + 1, 0);
		return new Rect(s6.x() - 21, s5.y() + 12, 64, 32);
	}

	// ------------------------------------------------------------ Filas centradas

	/** X de {@code count} nodos repartidos cada {@code pitch} px y centrados en el ancho del lienzo. */
	public static int[] rowXs(int count, int pitch) {
		int[] xs = new int[Math.max(0, count)];
		int start = (WIDTH - count * pitch) / 2 + (pitch - NODE) / 2;
		for (int i = 0; i < xs.length; i++) xs[i] = start + i * pitch;
		return xs;
	}

	/** Nodo {@code index} de una sección puente (Veterano o Ascenso): fila centrada, cada {@link #EVO_PITCH} px. */
	public static Rect bridgeNode(Block bridge, int index, int count) {
		int[] xs = rowXs(count, EVO_PITCH);
		return new Rect(xs[index], bridge.bodyY() + (BRIDGE_H - NODE) / 2, NODE, NODE);
	}

	/** Barra de maestría centrada en su bloque. */
	public static Rect masteryBar(Block mastery) {
		return new Rect((WIDTH - MASTERY_W) / 2, mastery.bodyY() + 4, MASTERY_W, 18);
	}
}
