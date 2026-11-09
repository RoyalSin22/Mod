package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientEclipse;
import dev.aptitudes.client.screen.UiStyle;
import dev.aptitudes.logic.BossHudLayout;
import dev.aptitudes.network.BossHudPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

public final class BossHud {
	private static final String[] PHASES = {"", "I", "II", "III"};
	private static BossHudPayload view = BossHudPayload.EMPTY;
	private static ClientWorld world;
	private static ClientPlayerEntity player;

	private BossHud() {}

	public static void onPayload(MinecraftClient client, BossHudPayload payload) {
		view = payload;
		world = client.world;
		player = client.player;
	}

	public static void clear() {
		view = BossHudPayload.EMPTY;
		world = null;
		player = null;
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != world || client.player != player) clear();
		if (player == null || !player.isAlive() || client.options.hudHidden || view.entries().isEmpty()) return;
		int width = ctx.getScaledWindowWidth();
		int height = ctx.getScaledWindowHeight();
		var layout = BossHudLayout.place(width, height, view.entries().size(), view.total(),
				ClientEclipse.active() && ClientEclipse.phase() > 0, AbilityHud.activeBarLayout(width, height));
		if (layout.rows() == 0) return;

		int x = layout.x();
		int y = layout.y();
		ctx.fill(x, y, x + layout.width(), y + layout.height(), 0xE0171012);
		ctx.drawBorder(x, y, layout.width(), layout.height(), 0xFF89694D);
		TextRenderer text = client.textRenderer;
		int rowY = y + BossHudLayout.PADDING;
		int rowX = x + 6;
		int rowWidth = layout.width() - 12;
		for (int i = 0; i < layout.rows(); i++) {
			drawEntry(ctx, text, view.entries().get(i), rowX, rowY, rowWidth);
			rowY += BossHudLayout.ROW_HEIGHT;
		}
		if (view.total() > layout.rows()) {
			Text more = Text.translatable("hud.aptitudes.boss.more", view.total() - layout.rows());
			ctx.drawCenteredTextWithShadow(text, UiStyle.fit(text, more, rowWidth), x + layout.width() / 2,
					rowY + 1, UiStyle.MUTED);
		}
	}

	private static void drawEntry(DrawContext ctx, TextRenderer text, BossHudPayload.Entry entry, int x, int y, int width) {
		Text health = Text.translatable("hud.aptitudes.boss.health", Math.round(entry.health() * 100));
		Text details = entry.phase() == 0 ? health
				: Text.translatable("hud.aptitudes.boss.phase", PHASES[entry.phase()]).append(" · ").append(health);
		details = UiStyle.fit(text, details, width * 2 / 3);
		int detailsX = x + width - text.getWidth(details);
		UiStyle.label(ctx, text, entry.name(), x, y, detailsX - x - 5, UiStyle.TEXT);
		ctx.drawText(text, details, detailsX, y, UiStyle.GOLD, false);

		ctx.fill(x, y + 10, x + width, y + 15, 0xFF442124);
		int filled = Math.round(width * entry.health());
		if (filled > 0) ctx.fillGradient(x, y + 10, x + filled, y + 15, 0xFFD95452, 0xFF8C171F);
	}
}
