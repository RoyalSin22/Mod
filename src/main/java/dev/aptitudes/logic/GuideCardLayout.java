package dev.aptitudes.logic;

/**
 * v67 (G2): maqueta de la carta de criatura del bestiario. Lógica pura (sin Minecraft), con pruebas: reparte el alto de
 * la página entre las secciones (retrato, estadísticas, consejos, botín y lore) para que nada se solape en ninguna
 * escala de la interfaz.
 */
public final class GuideCardLayout {
	public static final int RIBBON_HEIGHT = 14;
	public static final int SLOT = 18;
	public static final int MIN_PORTRAIT_WIDTH = 60;
	public static final int MAX_PORTRAIT_WIDTH = 84;
	/** Líneas reservadas para estadísticas y debilidades. */
	public static final int STATS_LINES = 3;

	private GuideCardLayout() {
	}

	public record Rect(int x, int y, int w, int h) {
		public int right() { return x + w; }
		public int bottom() { return y + h; }
	}

	/**
	 * Carta ya repartida. Todas las coordenadas son relativas a la esquina superior izquierda de la página.
	 *
	 * @param portrait  recuadro del modelo de la criatura
	 * @param seal      sello bajo el retrato (muertes y región)
	 * @param textX     columna de texto, a la derecha del retrato
	 * @param textW     ancho de la columna de texto
	 * @param ribbonY   cinta con el nombre
	 * @param kindY     línea de tipo y región
	 * @param statsY    estadísticas y debilidades ({@link #STATS_LINES} líneas)
	 * @param tipsY     título de los consejos; el texto empieza una línea más abajo
	 * @param tipsLines líneas de consejos
	 * @param lootY     título del botín; las casillas empiezan una línea más abajo
	 * @param loreY     título del lore; el texto empieza una línea más abajo
	 * @param loreLines líneas de lore
	 */
	public record Card(Rect portrait, Rect seal, int textX, int textW, int ribbonY, int kindY, int statsY, int tipsY,
					   int tipsLines, int lootY, int loreY, int loreLines) {}

	public static Card layout(int pageW, int pageH, int lineH) {
		int line = Math.max(6, lineH);
		int portraitW = Math.max(MIN_PORTRAIT_WIDTH, Math.min(MAX_PORTRAIT_WIDTH, Math.round(pageW * 0.34f)));
		int sealH = 2 * line + 4;
		int portraitH = Math.max(40, Math.min(Math.round(portraitW * 1.25f), pageH - sealH - 4));
		Rect portrait = new Rect(0, 0, portraitW, portraitH);
		Rect seal = new Rect(0, portraitH + 4, portraitW, sealH);
		int textX = portraitW + 8;
		int textW = Math.max(40, pageW - textX);

		int y = 0;
		int ribbonY = y;
		y += RIBBON_HEIGHT + 2;
		int kindY = y;
		y += line + 2;
		int statsY = y;
		y += STATS_LINES * line + 4;
		int tipsY = y;
		// Lo que queda para consejos y lore, descontando los títulos y la fila del botín.
		int lootBlock = line + SLOT + 4;
		int titles = 2 * line + 4;
		int rest = pageH - y - lootBlock - titles;
		int lines = Math.max(2, rest / line);
		int tipsLines = Math.max(1, (lines + 1) / 2);
		int loreLines = Math.max(1, lines - tipsLines);
		int lootY = tipsY + line + tipsLines * line + 4;
		int loreY = lootY + line + SLOT + 4;
		return new Card(portrait, seal, textX, textW, ribbonY, kindY, statsY, tipsY, tipsLines, lootY, loreY, loreLines);
	}

	/** Alto que ocupa toda la carta (hasta el final de la última línea de lore). */
	public static int height(Card card, int lineH) {
		return card.loreY() + Math.max(6, lineH) + card.loreLines() * Math.max(6, lineH);
	}
}
