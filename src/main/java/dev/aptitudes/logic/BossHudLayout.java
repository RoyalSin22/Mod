package dev.aptitudes.logic;

/** Reserva espacio sobre la barra de acción y separa el panel de los avisos del evento. */
public record BossHudLayout(int x, int y, int width, int height, int rows) {
	public static final int ROW_HEIGHT = 18;
	public static final int PADDING = 4;
	private static final int SUMMARY_HEIGHT = 11;
	private static final int GAP = 4;

	public static BossHudLayout place(int width, int height, int entries, int total, boolean event, AbilityHudLayout abilities) {
		if (width < 128) return new BossHudLayout(0, 0, 0, 0, 0);
		int panelWidth = Math.min(236, width - 16);
		int x = (width - panelWidth) / 2;
		int bottom = height - (event ? 108 : 78);
		for (int rows = entries; rows > 0; rows--) {
			int panelHeight = 2 * PADDING + rows * ROW_HEIGHT + (total > rows ? SUMMARY_HEIGHT : 0);
			int y = bottom - panelHeight;
			if (abilities != null && x < abilities.right() + GAP && x + panelWidth > abilities.left() - GAP
					&& y < abilities.bottom() + GAP && bottom > abilities.top() - GAP) {
				y = abilities.top() - GAP - panelHeight;
			}
			if (y >= GAP) return new BossHudLayout(x, y, panelWidth, panelHeight, rows);
		}
		return new BossHudLayout(0, 0, 0, 0, 0);
	}
}
