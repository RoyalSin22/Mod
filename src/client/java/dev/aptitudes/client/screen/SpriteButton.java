package dev.aptitudes.client.screen;

import dev.aptitudes.logic.TreeScreenLayout.Rect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Botón de 80×18 con las piezas del kit en tres estados (normal, pulsado y desactivado); el texto se dibuja en el juego. */
final class SpriteButton extends ButtonWidget {
	private final String base;
	private boolean pressed;

	SpriteButton(Rect rect, Text label, String base, PressAction action) {
		super(rect.x(), rect.y(), rect.w(), rect.h(), label, action, DEFAULT_NARRATION_SUPPLIER);
		this.base = base;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		pressed = true;
		super.onClick(mouseX, mouseY);
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		pressed = false;
		super.onRelease(mouseX, mouseY);
	}

	@Override
	protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
		String state = !active ? "desactivado" : pressed && isHovered() ? "pulsado" : "normal";
		TreeUi.sprite(ctx, "botones/" + base + "_" + state + ".png", getX(), getY(), 80, 18);
		int color = !active ? UiStyle.MUTED : isHovered() || isFocused() ? 0xFFFFE8A0 : UiStyle.TEXT;
		var tr = MinecraftClient.getInstance().textRenderer;
		int textY = getY() + (pressed && active ? 6 : 5);
		// Los botones «cerrar» y «reiniciar» llevan una X pintada a la izquierda: el texto se desplaza para no taparla.
		int textX = getX() + getWidth() / 2 + ("cerrar".equals(base) || "reiniciar".equals(base) ? 6 : 0);
		ctx.drawCenteredTextWithShadow(tr, getMessage(), textX, textY, color);
	}
}
