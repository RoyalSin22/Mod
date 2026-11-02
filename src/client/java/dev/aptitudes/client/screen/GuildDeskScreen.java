package dev.aptitudes.client.screen;

import dev.aptitudes.world.GuildDeskScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Pantalla de la Mesa del gremio (v27): usa la textura de la mesa de trabajo vanilla (misma disposición). */
public class GuildDeskScreen extends HandledScreen<GuildDeskScreenHandler> {
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/crafting_table.png");

	public GuildDeskScreen(GuildDeskScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
