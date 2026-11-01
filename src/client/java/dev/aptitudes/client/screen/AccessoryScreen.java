package dev.aptitudes.client.screen;

import dev.aptitudes.accessory.AccessoryScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/** Pantalla de accesorios (tecla Y): 2 anillos y 1 amuleto, con el estilo oscuro y dorado del mod. */
public class AccessoryScreen extends HandledScreen<AccessoryScreenHandler> {
	public AccessoryScreen(AccessoryScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = 176;
		backgroundHeight = 166;
		playerInventoryTitleY = backgroundHeight - 94;
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		UiStyle.window(ctx, x, y, backgroundWidth, backgroundHeight);
		// Ranuras de accesorio con marco dorado y etiqueta
		String[] labels = {"screen.aptitudes.accessories.ring", "screen.aptitudes.accessories.ring",
				"screen.aptitudes.accessories.amulet"};
		for (int i = 0; i < 3; i++) {
			int sx = x + AccessoryScreenHandler.SLOT_POS[i][0];
			int sy = y + AccessoryScreenHandler.SLOT_POS[i][1];
			ctx.fill(sx - 2, sy - 2, sx + 18, sy + 18, UiStyle.GOLD_DARK);
			ctx.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF14110C);
		}
		ctx.drawText(textRenderer, Text.translatable(labels[0]), x + 8, y + 30, UiStyle.MUTED, false);
		Text amulet = Text.translatable(labels[2]);
		ctx.drawText(textRenderer, amulet, x + 168 - textRenderer.getWidth(amulet), y + 54, UiStyle.MUTED, false);
		// Inventario del jugador
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) slot(ctx, x + 8 + col * 18, y + 84 + row * 18);
		}
		for (int col = 0; col < 9; col++) slot(ctx, x + 8 + col * 18, y + 142);
	}

	private static void slot(DrawContext ctx, int x, int y) {
		ctx.fill(x - 1, y - 1, x + 17, y + 17, 0xFF3A3F55);
		ctx.fill(x, y, x + 16, y + 16, 0xFF1B1E2A);
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		ctx.drawText(textRenderer, title, titleX, titleY, UiStyle.GOLD, false);
		ctx.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, UiStyle.MUTED, false);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
