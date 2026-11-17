package dev.aptitudes.logic;

/** Panel de jefes y campeones anclado arriba y centrado, como las barras de jefe del juego (V81: antes iba sobre la barra de acción). */
public record BossHudLayout(int x, int y, int width, int height, int rows) {
	public static final int ROW_HEIGHT = 18;
	public static final int PADDING = 4;
	public static final int TOP = 10;
	private static final int SUMMARY_HEIGHT = 11;

	public static BossHudLayout place(int width, int height, int entries, int total) {
		if (width < 128) return new BossHudLayout(0, 0, 0, 0, 0);
		int panelWidth = Math.min(236, width - 16);
		int x = (width - panelWidth) / 2;
		int limit = height / 2; // nunca baja de la mitad de la pantalla
		for (int rows = entries; rows > 0; rows--) {
			int panelHeight = 2 * PADDING + rows * ROW_HEIGHT + (total > rows ? SUMMARY_HEIGHT : 0);
			if (TOP + panelHeight <= limit) return new BossHudLayout(x, TOP, panelWidth, panelHeight, rows);
		}
		return new BossHudLayout(0, 0, 0, 0, 0);
	}
}
