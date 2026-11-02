package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientEclipse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * HUD del Eclipse (v34): viñeta carmesí en los bordes, nombre de la fase y una barra fina de progreso arriba.
 * La viñeta funciona aunque los mixins del cielo no se apliquen o el jugador los haya desactivado.
 */
public final class EclipseHud {
	private static final String[] PHASES = {"none", "penumbra", "totality", "retreat"};
	private static final int[] VIGNETTE = {0x3A0000, 0x1A2630, 0x3A1A00};
	private static final String[] EVENT_KEYS = {"event.aptitudes.eclipse.name", "event.aptitudes.soul_fog.name",
			"event.aptitudes.ember_rain.name"};

	private EclipseHud() {
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden || !ClientEclipse.active()) return;
		float intensity = ClientEclipse.intensity(tickCounter.getTickDelta(true));
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();

		// Viñeta: degradados desde cada borde hacia el centro.
		int alpha = (int) (110 * intensity);
		if (alpha > 0) {
			int tint = VIGNETTE[Math.min(VIGNETTE.length - 1, ClientEclipse.kind())]; // v41: color por evento
			int edge = (alpha << 24) | tint;
			int clear = tint;
			int band = Math.max(24, h / 5);
			ctx.fillGradient(0, 0, w, band, edge, clear);
			ctx.fillGradient(0, h - band, w, h, clear, edge);
			for (int i = 0; i < band; i += 2) {
				int a = (int) (alpha * (1f - i / (float) band));
				int c = (a << 24) | tint;
				ctx.fill(i, 0, i + 2, h, c);
				ctx.fill(w - i - 2, 0, w - i, h, c);
			}
		}

		int phase = ClientEclipse.phase();
		if (phase <= 0 || phase >= PHASES.length) return;
		// v41: el Eclipse muestra su fase; los demás eventos, su nombre.
		Text label = ClientEclipse.kind() == 0
				? Text.translatable("eclipse.aptitudes.hud", Text.translatable("eclipse.aptitudes.phase_name." + PHASES[phase])).formatted(Formatting.RED)
				: Text.translatable(EVENT_KEYS[Math.min(EVENT_KEYS.length - 1, ClientEclipse.kind())]).formatted(Formatting.GOLD);
		int barWidth = 120;
		int x = w / 2 - barWidth / 2;
		// v39: por encima de la barra de acción (h − 72) y del nombre del objeto en mano (h − 59), para no tapar los
		// avisos de runas ni otros mensajes; tampoco choca con las barras de jefe (arriba) ni con los efectos (arriba a la derecha).
		int y = h - 92;
		int textWidth = client.textRenderer.getWidth(label);
		ctx.drawTextWithShadow(client.textRenderer, label, w / 2 - textWidth / 2, y - 10, 0xFFFF5555);
		ctx.fill(x - 1, y - 1, x + barWidth + 1, y + 3, 0xA0000000);
		ctx.fill(x, y, x + (int) (barWidth * ClientEclipse.progress()), y + 2, 0xFFB01515);
	}
}
