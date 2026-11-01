package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientExtraState;
import dev.aptitudes.network.UniqueSyncPayload;
import dev.aptitudes.unique.UniqueSkill;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Catálogo de habilidades únicas (tecla U): 16 tarjetas con su dueño ("Libre" si
 * nadie la tiene), tu progreso en la gesta y, al pasar el ratón, sus efectos.
 * La tuya aparece resaltada.
 */
public class UniquesScreen extends Screen {
	private static final int W = 440;
	private static final int H = 272;
	private static final int CARD_W = 104;
	private static final int CARD_H = 50;

	private int left;
	private int top;

	public UniquesScreen() {
		super(Text.translatable("screen.aptitudes.uniques.title"));
	}

	@Override
	protected void init() {
		left = (width - W) / 2;
		top = (height - H) / 2;
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.tab.attributes"),
				b -> client.setScreen(new AptitudesScreen())).dimensions(width / 2 - 154, top + H - 26, 100, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.tab.roles"),
				b -> client.setScreen(new RolesScreen())).dimensions(width / 2 - 50, top + H - 26, 100, 18).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(width / 2 + 54, top + H - 26, 100, 18).build());
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			client.setScreen(hasShiftDown() ? new RolesScreen() : new AptitudesScreen());
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, W, H);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawTextWithShadow(textRenderer, title.copy().formatted(Formatting.BOLD), left + 12, top + 9, UiStyle.GOLD);
		Text rule = Text.translatable("screen.aptitudes.uniques.rule");
		ctx.drawText(textRenderer, rule, left + W - 12 - textRenderer.getWidth(rule), top + 10, UiStyle.MUTED, false);
		UniqueSyncPayload data = ClientExtraState.uniques();
		if (data == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.waiting"),
					width / 2, top + H / 2, UiStyle.MUTED);
			return;
		}

		List<Text> tooltip = null;
		UniqueSkill[] skills = UniqueSkill.values();
		for (int i = 0; i < skills.length; i++) {
			UniqueSkill skill = skills[i];
			int x = left + 10 + (i % 4) * (CARD_W + 4);
			int y = top + 28 + (i / 4) * (CARD_H + 4);
			boolean mine = data.ownSkill() == i;
			String holder = i < data.holders().length ? data.holders()[i] : "";
			boolean free = holder.isEmpty();

			UiStyle.panel(ctx, x, y, CARD_W, CARD_H);
			if (mine) ctx.drawBorder(x, y, CARD_W, CARD_H, 0xFFB388FF);
			UiStyle.frame(ctx, x + 3, y + 3, UiStyle.Frame.UNIQUE, mine || free, false);
			ctx.drawItem(skill.iconStack(), x + 7, y + 7);
			List<OrderedText> name = textRenderer.wrapLines(Text.translatable(skill.translationKey()), CARD_W - 32);
			for (int l = 0; l < Math.min(2, name.size()); l++) {
				ctx.drawText(textRenderer, name.get(l), x + 30, y + 4 + l * 9, 0xFF000000 | skill.color(), false);
			}
			Text owner = free ? Text.translatable("aptitudes.unique.free").formatted(Formatting.GREEN)
					: Text.literal(holder).formatted(mine ? Formatting.LIGHT_PURPLE : Formatting.GOLD);
			ctx.drawText(textRenderer, owner, x + 30, y + 24, UiStyle.TEXT, false);

			int progress = i < data.progress().length ? data.progress()[i] : 0;
			int barW = CARD_W - 8;
			ctx.fill(x + 4, y + CARD_H - 8, x + 4 + barW, y + CARD_H - 5, 0xFF202433);
			ctx.fill(x + 4, y + CARD_H - 8, x + 4 + (int) (barW * Math.min(1.0, progress / (double) skill.target())),
					y + CARD_H - 5, progress >= skill.target() ? UiStyle.GOLD : 0xFF7E57C2);

			if (mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H) {
				tooltip = new java.util.ArrayList<>();
				tooltip.add(Text.translatable(skill.translationKey()).withColor(skill.color()));
				tooltip.add(Text.translatable(skill.descriptionKey()).formatted(Formatting.GRAY));
				tooltip.add(Text.translatable(skill.conditionKey(), progress, skill.target()).formatted(Formatting.AQUA));
				tooltip.add(Text.translatable("screen.aptitudes.uniques.cost", skill.energyCost(), skill.cooldownSecs())
						.formatted(Formatting.DARK_AQUA));
			}
		}
		if (tooltip != null) ctx.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
	}
}
