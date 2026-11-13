package dev.aptitudes.logic;

import dev.aptitudes.logic.TreeScreenLayout.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometría de la pantalla K (v67): pestañas, atributos (tres columnas), habilidades (tarjetas 5×2) y botones inferiores.
 * Lógica pura (sin Minecraft) para comprobar con pruebas que nada se sale de la ventana ni se solapa. Las coordenadas son
 * de pantalla (píxeles de GUI). Las piezas del kit se dibujan a su tamaño real (pestañas de 96×22, tarjetas de 200×38,
 * filas de 140×16, botones de 80×18) para conservar el píxel lógico de 2×2; solo los paneles se estiran (9-slice de 12 px).
 *
 * <p>La ventana mide {@value #WIDTH}×{@value #HEIGHT}; si la pantalla es más baja se desplaza con la rueda.</p>
 */
public final class KScreenLayout {
	public static final int WIDTH = 430;
	public static final int HEIGHT = 276;
	public static final int TABS = 4;
	public static final int ATTRIBUTES = 8;
	public static final int STATS = 11;
	public static final int SKILLS = 10;
	public static final int ATTR_ROW_H = 16;
	public static final int STAT_ROW_H = 12;
	public static final int CARD_W = 200;
	public static final int CARD_H = 38;
	public static final int BUTTON_W = 80;
	public static final int BUTTON_H = 18;
	/** Borde del 9-slice del panel principal: el contenido no debe invadir esta franja por abajo. */
	public static final int BORDER = 12;

	private static final int BUTTON_Y = 232;
	private static final int BUTTON_GAP = 10;

	private final int x;
	private final int y;

	private KScreenLayout(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/** Cuánto se desplaza la ventana cuando la pantalla es más baja (0 si cabe con 8 px de margen arriba y abajo). */
	public static int maxScroll(int screenHeight) {
		return Math.max(0, HEIGHT + 16 - screenHeight);
	}

	public static KScreenLayout place(int screenWidth, int screenHeight, int scroll) {
		int s = Math.max(0, Math.min(maxScroll(screenHeight), scroll));
		int top = maxScroll(screenHeight) > 0 ? 8 - s : (screenHeight - HEIGHT) / 2;
		return new KScreenLayout((screenWidth - WIDTH) / 2, top);
	}

	private Rect at(int rx, int ry, int w, int h) {
		return new Rect(x + rx, y + ry, w, h);
	}

	public int x() { return x; }
	public int y() { return y; }

	public Rect window() { return at(0, 0, WIDTH, HEIGHT); }

	// ------------------------------------------------------------ Cabecera

	/** Esquina del título (texto), a la derecha del emblema cuando lo hay. */
	public int titleX(boolean emblem) { return x + (emblem ? 38 : 14); }
	public int titleY() { return y + 10; }
	public Rect emblem() { return at(10, 2, 24, 24); }

	public Rect tab(int index) { return at(12 + index * 98, 26, 96, 22); }

	public Rect divider() { return at(10, 50, WIDTH - 20, 1); }

	/** Franja de datos bajo las pestañas (nivel de alma, puntos y coste; maestría en habilidades). */
	public Rect infoStrip() { return at(12, 54, WIDTH - 24, 10); }

	// ------------------------------------------------------------ Atributos

	public Rect columnA() { return at(10, 78, 152, 146); }
	public Rect columnB() { return at(166, 78, 132, 146); }
	public Rect columnC() { return at(302, 78, 118, 146); }

	/** Cabecera de cada columna (0, 1 o 2): texto sobre el panel. */
	public Rect columnHeader(int column) {
		Rect c = column == 0 ? columnA() : column == 1 ? columnB() : columnC();
		return new Rect(c.x() + 4, y + 67, c.w() - 8, 10);
	}

	public Rect attributeRow(int index) { return at(16, 84 + index * ATTR_ROW_H, 140, ATTR_ROW_H); }

	public Rect statRow(int index) { return at(170, 84 + index * STAT_ROW_H, 124, STAT_ROW_H); }

	/** Zona de texto de la ficha de detalle (dentro del panel de la columna C). */
	public Rect detailInner() { return at(308, 84, 106, 134); }

	// ------------------------------------------------------------ Habilidades

	public Rect card(int index) { return at(10 + (index % 2) * 210, 68 + (index / 2) * CARD_H, CARD_W, CARD_H); }

	// ------------------------------------------------------------ Parte baja

	/** Botón {@code index} de una fila de {@code count} botones de 80×18 centrada. */
	public Rect button(int index, int count) {
		int total = count * BUTTON_W + (count - 1) * BUTTON_GAP;
		return at((WIDTH - total) / 2 + index * (BUTTON_W + BUTTON_GAP), BUTTON_Y, BUTTON_W, BUTTON_H);
	}

	public Rect hint() { return at(12, 254, WIDTH - 24, 8); }

	/** Elementos de la pestaña de atributos que no deben solaparse (con {@code buttons} botones abajo). */
	public List<Rect> attributeElements(int buttons) {
		List<Rect> out = new ArrayList<>();
		out.add(emblem());
		for (int i = 0; i < TABS; i++) out.add(tab(i));
		out.add(infoStrip());
		for (int c = 0; c < 3; c++) out.add(columnHeader(c));
		out.add(columnA());
		out.add(columnB());
		out.add(columnC());
		for (int i = 0; i < buttons; i++) out.add(button(i, buttons));
		out.add(hint());
		return out;
	}

	/** Elementos de la pestaña de habilidades que no deben solaparse. */
	public List<Rect> skillElements() {
		List<Rect> out = new ArrayList<>();
		out.add(emblem());
		for (int i = 0; i < TABS; i++) out.add(tab(i));
		out.add(infoStrip());
		for (int i = 0; i < SKILLS; i++) out.add(card(i));
		return out;
	}
}
