package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * v65 C5: HUD de sinergias activas. Muestra 5 indicadores (Y1-Y5) con animación de pulsación
 * cuando están activos. Los datos se envían desde el servidor periódicamente.
 */
public final class SynergyHud {
	private static final int SIZE = 16;
	private static final int ROW_Y = -65;
	private static final Map<Integer, Long> ACTIVE_UNTIL = new HashMap<>();
	private static final int FADE_TIME_MS = 500;

	private static final int COLOR_Y1 = 0xFF4FC3F7;  // Azul (Senda ↔ Activas)
	private static final int COLOR_Y2 = 0xFFFFA726;  // Naranja (Resonancia grupo)
	private static final int COLOR_Y3 = 0xFFEF5350;  // Rojo (Elementos ↔ Runas)
	private static final int COLOR_Y4 = 0xFF66BB6A;  // Verde (Armadura ↔ Senda)
	private static final int COLOR_Y5 = 0xFFAB47BC;  // Púrpura (Combos ↔ Keystones)

	private static final int[] COLORS = {COLOR_Y1, COLOR_Y2, COLOR_Y3, COLOR_Y4, COLOR_Y5};
	private static final String[] LABELS = {"Y1", "Y2", "Y3", "Y4", "Y5"};

	private SynergyHud() {
	}

	/** v65 C5: Activar indicador de sinergia (se desvanece después de 500ms). */
	public static void activateSynergy(int synergyIndex) {
		if (synergyIndex < 0 || synergyIndex >= 5) return;
		ACTIVE_UNTIL.put(synergyIndex, Util.getMeasuringTimeMs() + FADE_TIME_MS);
	}

	static void clear() {
		ACTIVE_UNTIL.clear();
	}

	static int occupiedAbove() {
		return ACTIVE_UNTIL.isEmpty() ? 0 : -ROW_Y;
	}

	static void draw(DrawContext ctx) {
		long now = Util.getMeasuringTimeMs();
		int x = 0;
		int y = ROW_Y;

		for (int i = 0; i < 5; i++) {
			Long activeUntil = ACTIVE_UNTIL.get(i);
			if (activeUntil == null || now > activeUntil) {
				ACTIVE_UNTIL.remove(i);
				continue;
			}

			// Calcular animación de desvanecimiento
			long timeLeft = activeUntil - now;
			float progress = Math.min(1f, timeLeft / (float) FADE_TIME_MS);

			// Fondo oscuro
			ctx.fill(x, y, x + SIZE, y + SIZE, 0xC0101018);

			// Relleno de color con animación
			int alpha = (int) (200 * progress);
			int fillColor = (alpha << 24) | (COLORS[i] & 0xFFFFFF);
			ctx.fill(x + 2, y + 2, x + SIZE - 2, y + SIZE - 2, fillColor);

			// Borde animado
			ctx.getMatrices().push();
			ctx.getMatrices().translate(0, 0, 200);
			int borderAlpha = (int) (255 * progress);
			int borderColor = (borderAlpha << 24) | (COLORS[i] & 0xFFFFFF);

			// Pulso de animación
			if (ClientConfig.get().hudAnimations) {
				float pulse = 0.5f + 0.5f * (float) Math.sin(now / 100.0);
				borderColor = ((int) (borderAlpha * pulse) << 24) | (COLORS[i] & 0xFFFFFF);
			}

			ctx.drawBorder(x, y, SIZE, SIZE, borderColor);
			ctx.getMatrices().pop();

			x += SIZE + 2;
		}
	}
}
