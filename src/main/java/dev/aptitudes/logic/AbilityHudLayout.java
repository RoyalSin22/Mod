package dev.aptitudes.logic;

/** Sitúa la barra y sus indicadores dentro de los límites de la ventana. */
public record AbilityHudLayout(int x, int y, float scale, int left, int top, int right, int bottom) {
	private static final int MARGIN = 6; // Incluye el destello que se expande al lanzar.
	private static final int ABOVE = 51;
	private static final int BELOW = 26;

	public static float validatedScale(float scale) {
		return Float.isFinite(scale) ? Math.clamp(scale, 0.5f, 2.0f) : 1.0f;
	}

	public static AbilityHudLayout place(int width, int height, int barWidth, float scale, int offsetX, int offsetY) {
		int horizontal = Math.max(1, width - 2);
		int vertical = Math.max(1, height - 2);
		float fit = Math.min(validatedScale(scale),
				Math.min(horizontal / (float) (barWidth + 2 * MARGIN), vertical / (float) (ABOVE + BELOW)));
		int margin = (int) Math.ceil(MARGIN * fit);
		int above = (int) Math.ceil(ABOVE * fit);
		int below = (int) Math.ceil(BELOW * fit);
		int rightExtent = (int) Math.ceil((barWidth + MARGIN) * fit);
		int farRight = width - rightExtent;
		int anchorX = width / 2 + 103;
		int anchorY = height - below - 2;
		if (anchorX > farRight) {
			anchorX = farRight - 2;
			anchorY -= 44;
		}
		anchorX = Math.clamp(anchorX, margin, Math.max(margin, farRight));
		anchorY = Math.clamp(anchorY, above, Math.max(above, height - below));
		int x = (int) Math.clamp((long) anchorX + offsetX, margin, Math.max(margin, farRight));
		int y = (int) Math.clamp((long) anchorY + offsetY, above, Math.max(above, height - below));
		return new AbilityHudLayout(x, y, fit, x - margin, y - above, x + rightExtent, y + below);
	}

	public boolean contains(double mouseX, double mouseY) {
		return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
	}
}
