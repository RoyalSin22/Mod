package dev.aptitudes.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Botón de la pantalla K con las piezas del kit (normal, pulsado y desactivado), ajustado a cualquier ancho repitiendo
 * su tramo central. El texto lo dibuja el juego.
 */
final class KButton extends ButtonWidget {
	static final int HEIGHT = 18;
	private String base;
	private boolean pressed;

	KButton(int x, int y, int width, Text label, String base, PressAction action) {
		super(x, y, width, HEIGHT, label, action, DEFAULT_NARRATION_SUPPLIER);
		this.base = base;
	}

	/** Cambia la pieza del botón (p. ej., un interruptor encendido usa «confirmar» y apagado «cerrar»). */
	void setBase(String base) {
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
		boolean down = active && pressed && isHovered();
		KUi.button(ctx, base, !active ? "desactivado" : down ? "pulsado" : "normal", getX(), getY(), getWidth());
		int color = !active ? UiStyle.MUTED : isHovered() || isFocused() ? 0xFFFFE8A0 : UiStyle.TEXT;
		var tr = MinecraftClient.getInstance().textRenderer;
		Text label = UiStyle.fit(tr, getMessage(), getWidth() - 16);
		ctx.drawCenteredTextWithShadow(tr, label, getX() + getWidth() / 2 + 2, getY() + (down ? 6 : 5), color);
	}
}
