package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientTutorial;
import dev.aptitudes.tutorial.TutorialRules;
import dev.aptitudes.tutorial.TutorialView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

/** Recordatorio pequeño bajo los efectos de estado, separado de los marcos del grupo. */
public final class TutorialHud {
	private TutorialHud() {
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientConfig cfg = ClientConfig.get();
		TutorialView view = ClientTutorial.view();
		if (!cfg.hudVisible || !cfg.showTutorial || client.player == null || client.options.hudHidden
				|| client.currentScreen != null || !client.player.isAlive() || client.player.isCreative()
				|| client.player.isSpectator() || view == null || !view.visible()) return;
		int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
		if (w < 80 || h < 100) return;
		int width = Math.min(220, w - 16), x = w - width - 8;
		float scale = Float.isFinite(cfg.hudScale) ? Math.max(0.5f, Math.min(2, cfg.hudScale)) : 1;
		int y = Math.min(h - 36, cfg.soulsBars ? Math.max(58, 6 + (int) Math.ceil(46 * scale)) : 58);
		Text goal = view.nextStep() < 0 ? Text.translatable("tutorial.aptitudes.hud.pending", view.pendingRewards())
				: Text.translatable("tutorial.aptitudes.step." + TutorialRules.ORDER.get(view.nextStep()).key());
		List<OrderedText> lines = client.textRenderer.wrapLines(goal, width - 10);
		int count = Math.min(2, lines.size());
		ctx.fill(x, y, x + width, y + 16 + count * 10, 0x90121820);
		ctx.drawTextWithShadow(client.textRenderer, Text.translatable("tutorial.aptitudes.hud",
				view.nextStep() < 0 ? TutorialRules.ORDER.size() : view.nextStep() + 1, TutorialRules.ORDER.size()),
				x + 5, y + 4, 0xFFE4C474);
		for (int i = 0; i < count; i++) ctx.drawText(client.textRenderer, lines.get(i), x + 5, y + 15 + i * 10, 0xFFDDDDDD, false);
	}
}
