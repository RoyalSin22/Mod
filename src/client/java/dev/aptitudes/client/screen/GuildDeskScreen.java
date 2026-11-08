package dev.aptitudes.client.screen;

import dev.aptitudes.world.GuildDeskScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

/** Las zonas de trabajo y de inventario conservan los índices de ranura del contenedor. */
public class GuildDeskScreen extends HandledScreen<GuildDeskScreenHandler> {
	private static final int JADE = 0xFF8AC4B0;
	private static final int BRASS = 0xFFB9824B;
	private static final int WOOD = 0xFF302329;
	private static final int RECESS = 0xFF151922;
	private static final int EDGE = 0xFF63494A;

	public GuildDeskScreen(GuildDeskScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = GuildDeskScreenHandler.WIDTH;
		backgroundHeight = GuildDeskScreenHandler.HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		Text label = Text.translatable("forge.aptitudes.reforge");
		var output = handler.getSlot(0);
		boolean wide = backgroundWidth >= 240;
		int buttonWidth = wide ? 104 : 85;
		int left = Math.min(backgroundWidth - buttonWidth - 5, output.x + 8 - buttonWidth / 2);
		int top = output.y + (wide ? 28 : 20);
		addDrawableChild(ButtonWidget.builder(UiStyle.fit(textRenderer, label, buttonWidth - 8), button -> {
			if (client != null && client.interactionManager != null) {
				client.interactionManager.clickButton(handler.syncId, GuildDeskScreenHandler.OPEN_REFORGE);
			}
		}).dimensions(x + left, y + top, buttonWidth, 16).tooltip(Tooltip.of(label)).build());
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		UiStyle.window(ctx, x, y, backgroundWidth, backgroundHeight);
		ctx.fill(x + 2, y + 3, x + backgroundWidth - 2, y + 23, WOOD);
		ctx.fill(x + 12, y + 24, x + backgroundWidth - 12, y + 25, BRASS);

		ctx.fill(x + 27, y + 31, x + 115, y + 104, WOOD);
		ctx.drawBorder(x + 27, y + 31, 88, 73, EDGE);
		ctx.fill(x + 128, y + 31, x + 240, y + 104, WOOD);
		ctx.drawBorder(x + 128, y + 31, 112, 73, EDGE);

		UiStyle.panel(ctx, x + 10, y + 111, backgroundWidth - 20, 94);
		boolean ready = handler.getSlot(0).hasStack();
		Slot output = handler.getSlot(0);
		ctx.drawBorder(x + output.x - 4, y + output.y - 4, 24, 24, ready ? BRASS : EDGE);
		for (int i = 0; i < handler.slots.size(); i++) {
			Slot slot = handler.getSlot(i);
			int sx = x + slot.x - 1;
			int sy = y + slot.y - 1;
			ctx.fill(sx, sy, sx + 18, sy + 18, RECESS);
			ctx.drawBorder(sx, sy, 18, 18, i == 0 ? BRASS : i < 10 ? 0xFF416557 : UiStyle.BORDER);
		}
		int arrow = ready ? JADE : UiStyle.MUTED;
		ctx.fill(x + 116, y + 61, x + 124, y + 63, arrow);
		UiStyle.line(ctx, x + 121, y + 57, x + 125, y + 61, arrow);
		UiStyle.line(ctx, x + 125, y + 61, x + 121, y + 65, arrow);
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		UiStyle.label(ctx, textRenderer, title, 12, 9, backgroundWidth - 24, UiStyle.GOLD);
		Text result = Text.translatable("container.aptitudes.guild_desk.result");
		ctx.drawCenteredTextWithShadow(textRenderer, UiStyle.fit(textRenderer, result, 96), 184, 39, JADE);
		UiStyle.label(ctx, textRenderer, playerInventoryTitle, 16, 116, backgroundWidth - 32, UiStyle.TEXT);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
