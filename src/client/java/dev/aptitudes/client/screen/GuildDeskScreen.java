package dev.aptitudes.client.screen;

import dev.aptitudes.world.GuildDeskScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

/** Cuadrícula de fabricación con la madera, el latón y el jade de la Mesa del gremio. */
public class GuildDeskScreen extends HandledScreen<GuildDeskScreenHandler> {
	private static final int JADE = 0xFF82BEAB;
	private static final int BRASS = 0xFFB9824B;
	private static final int WOOD = 0xFF302328;
	private static final int RECESS = 0xFF171B21;
	private static final int EDGE = 0xFF584143;

	public GuildDeskScreen(GuildDeskScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		UiStyle.window(ctx, x, y, backgroundWidth, backgroundHeight);
		ctx.fill(x + 2, y + 2, x + 174, y + 75, WOOD);
		ctx.drawBorder(x + 25, y + 15, 61, 57, EDGE);
		ctx.drawBorder(x + 120, y + 31, 24, 24, handler.getSlot(0).hasStack() ? BRASS : EDGE);
		for (int i = 0; i < handler.slots.size(); i++) {
			Slot slot = handler.getSlot(i);
			int sx = x + slot.x - 1;
			int sy = y + slot.y - 1;
			ctx.fill(sx, sy, sx + 18, sy + 18, RECESS);
			ctx.drawBorder(sx, sy, 18, 18, i == 0 ? BRASS : i < 10 ? 0xFF416557 : UiStyle.BORDER);
		}
		int arrow = handler.getSlot(0).hasStack() ? JADE : UiStyle.MUTED;
		ctx.fill(x + 94, y + 42, x + 109, y + 44, arrow);
		UiStyle.line(ctx, x + 105, y + 38, x + 110, y + 42, arrow);
		UiStyle.line(ctx, x + 110, y + 42, x + 105, y + 46, arrow);
		for (int dx = 4; dx <= 168; dx += 164) {
			ctx.fill(x + dx, y + 5, x + dx + 3, y + 8, BRASS);
			ctx.fill(x + dx, y + 68, x + dx + 3, y + 71, BRASS);
		}
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		UiStyle.label(ctx, textRenderer, title, 10, 5, 156, UiStyle.GOLD);
		Text result = Text.translatable("container.aptitudes.guild_desk.result");
		ctx.drawCenteredTextWithShadow(textRenderer, UiStyle.fit(textRenderer, result, 76), 132, 20, JADE);
		UiStyle.label(ctx, textRenderer, playerInventoryTitle, 8, 75, 160, UiStyle.TEXT);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
