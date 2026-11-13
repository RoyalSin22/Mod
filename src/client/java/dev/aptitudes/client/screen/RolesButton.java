package dev.aptitudes.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Botón de la pantalla de roles con el botón largo del kit (normal, pulsado y desactivado), a cualquier tamaño. */
final class RolesButton extends ButtonWidget {
	private boolean pressed;

	RolesButton(int x, int y, int width, int height, Text label, PressAction action) {
		super(x, y, width, height, label, action, DEFAULT_NARRATION_SUPPLIER);
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
		RolesUi.button(ctx, !active ? "desactivado" : down ? "pulsado" : "normal", getX(), getY(), getWidth(), getHeight());
		int color = !active ? 0xFF9AA0B5 : isHovered() || isFocused() ? 0xFFFFE8A0 : UiStyle.TEXT;
		var tr = MinecraftClient.getInstance().textRenderer;
		Text label = UiStyle.fit(tr, getMessage(), getWidth() - 8);
		int textY = getY() + (getHeight() - 8) / 2 + (down ? 1 : 0);
		ctx.drawCenteredTextWithShadow(tr, label, getX() + getWidth() / 2, textY, color);
	}
}
