package dev.aptitudes.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometría de la pantalla del árbol de habilidades (v66): tres sendas en columnas con sus siete escalones, cabecera del
 * rol, maestría, habilidad definitiva y ficha de detalle. Lógica pura (sin Minecraft) para poder comprobar con pruebas
 * que nada se sale de la ventana ni se solapa. Todas las coordenadas son de pantalla (píxeles de GUI).
 *
 * <p>La ventana mide hasta {@value #MAX_WIDTH} px de ancho y {@value #HEIGHT} de alto; si la pantalla es más baja se
 * desplaza con la rueda. Las piezas del kit (cabeceras de 96 px, nodos de 24 px, botones de 80×18…) se dibujan a su
 * tamaño real, sin escalar, para conservar el píxel lógico de 2×2.</p>
 */
public final class TreeScreenLayout {
	public static final int MAX_WIDTH = 520;
	public static final int MIN_WIDTH = 418;
	public static final int HEIGHT = 300;
	public static final int PATHS = 3;
	/** S1, S2, S3, S4, S5 (dos opciones), S6 y S7. */
	public static final int ROWS = 7;
	public static final int BIFURCATION_ROW = 4;
	public static final int NODE = 24;
	public static final int PITCH = 26;
	public static final int COLUMN_WIDTH = 96;

	private static final int MARGIN = 6;
	private static final int LABEL_WIDTH = 24;
	private static final int COLUMN_GAP = 2;
	private static final int FIRST_COLUMN_X = MARGIN + LABEL_WIDTH + 2;
	private static final int COLUMNS_END = FIRST_COLUMN_X + PATHS * COLUMN_WIDTH + (PATHS - 1) * COLUMN_GAP;
	private static final int ROWS_Y = 84;
	/** Distancia del centro de la columna al centro de cada opción de la bifurcación de S5. */
	private static final int BRANCH_OFFSET = 15;

	public record Rect(int x, int y, int w, int h) {
		public int right() { return x + w; }
		public int bottom() { return y + h; }
		public boolean contains(double px, double py) { return px >= x && px < right() && py >= y && py < bottom(); }
		public boolean overlaps(Rect o) { return x < o.right() && o.x < right() && y < o.bottom() && o.y < bottom(); }
		public boolean inside(Rect o) { return x >= o.x && y >= o.y && right() <= o.right() && bottom() <= o.bottom(); }
	}

	private final int x;
	private final int y;
	private final int width;

	private TreeScreenLayout(int x, int y, int width) {
		this.x = x;
		this.y = y;
		this.width = width;
	}

	/** Alto que se desplaza cuando la pantalla es más baja que la ventana. */
	public static int maxScroll(int screenHeight) {
		return Math.max(0, HEIGHT + 8 - screenHeight);
	}

	public static TreeScreenLayout place(int screenWidth, int screenHeight, int scroll) {
		int width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, screenWidth - 8));
		int s = Math.max(0, Math.min(maxScroll(screenHeight), scroll));
		int top = maxScroll(screenHeight) > 0 ? 4 - s : (screenHeight - HEIGHT) / 2;
		return new TreeScreenLayout((screenWidth - width) / 2, top, width);
	}

	private Rect at(int rx, int ry, int w, int h) {
		return new Rect(x + rx, y + ry, w, h);
	}

	public Rect window() { return at(0, 0, width, HEIGHT); }

	// ------------------------------------------------------------ Cabecera

	public Rect roleIcon() { return at(MARGIN + 4, 6, 32, 32); }
	/** Zona del nombre y el nivel, a la derecha del icono. */
	public Rect nameBox() { return at(MARGIN + 42, 6, 110, 32); }
	public Rect xpBar() { return at(MARGIN + 160, 12, width - MARGIN - 160 - MARGIN - 4, 14); }
	public Rect banner() { return at(FIRST_COLUMN_X, 42, COLUMNS_END - FIRST_COLUMN_X, 12); }

	// ------------------------------------------------------------ Sendas

	public int columnX(int path) {
		return FIRST_COLUMN_X + path * (COLUMN_WIDTH + COLUMN_GAP);
	}

	public Rect columnHeader(int path) { return at(columnX(path), 56, COLUMN_WIDTH, 24); }

	private int rowY(int row) { return ROWS_Y + row * PITCH; }

	public Rect label(int row) { return at(MARGIN, rowY(row) + (NODE - 14) / 2 + 1, LABEL_WIDTH, 14); }

	/**
	 * Nodo de una senda. {@code slot} es 0 para el nodo único (o la opción X de la bifurcación, a la izquierda) y 1 para
	 * la opción Y (a la derecha). Los conectores se dibujan entre los centros de los nodos.
	 */
	public Rect node(int path, int row, int slot) {
		int cx = columnX(path) + COLUMN_WIDTH / 2;
		if (row == BIFURCATION_ROW) {
			int center = cx + (slot == 0 ? -BRANCH_OFFSET : BRANCH_OFFSET);
			return at(center - NODE / 2, rowY(row), NODE, NODE);
		}
		return at(cx - NODE / 2, rowY(row), NODE, NODE);
	}

	/** Centro horizontal de la columna (pantalla), donde se alinean los nodos únicos y los conectores. */
	public int columnCenterX(int path) {
		return x + columnX(path) + COLUMN_WIDTH / 2;
	}

	/** Nodos del tipo de escalón: S5 tiene dos opciones, el resto una. */
	public static int slots(int row) {
		return row == BIFURCATION_ROW ? 2 : 1;
	}

	// ------------------------------------------------------------ Parte baja

	public Rect mastery() { return at(FIRST_COLUMN_X, ROWS_Y + ROWS * PITCH + 4, 248, 24); }

	// ------------------------------------------------------------ Columna derecha

	private int rightX() { return COLUMNS_END + 4; }

	private int rightWidth() { return width - rightX() - MARGIN; }

	public Rect detail() { return at(rightX(), 44, rightWidth(), 130); }

	public Rect definitive() { return at(rightX() + (rightWidth() - 48) / 2, 178, 48, 48); }

	public Rect definitiveLabel() { return at(rightX(), 228, rightWidth(), 10); }

	public Rect primaryButton() { return at(rightX() + (rightWidth() - 80) / 2, 242, 80, 18); }

	public Rect closeButton() { return at(rightX() + (rightWidth() - 80) / 2, 264, 80, 18); }

	/** Todos los elementos que no deben solaparse entre sí (los conectores van detrás de los nodos y se excluyen). */
	public List<Rect> solidElements() {
		List<Rect> out = new ArrayList<>();
		out.add(roleIcon());
		out.add(nameBox());
		out.add(xpBar());
		out.add(banner());
		out.add(mastery());
		out.add(detail());
		out.add(definitive());
		out.add(definitiveLabel());
		out.add(primaryButton());
		out.add(closeButton());
		for (int row = 0; row < ROWS; row++) out.add(label(row));
		for (int path = 0; path < PATHS; path++) {
			out.add(columnHeader(path));
			for (int row = 0; row < ROWS; row++) {
				for (int slot = 0; slot < slots(row); slot++) out.add(node(path, row, slot));
			}
		}
		return out;
	}
}
