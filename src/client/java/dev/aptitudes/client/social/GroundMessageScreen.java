package dev.aptitudes.client.social;

import dev.aptitudes.network.GroundMessageWritePayload;
import dev.aptitudes.social.MessageCatalog;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * C7 (S5): pantalla para escribir una nota en el suelo (plantilla + palabra del catálogo). Cabe en 426×240: 400×212
 * como mucho. Solo envía la elección; el servidor comprueba la tiza, el sitio y los límites.
 */
public final class GroundMessageScreen extends Screen {
	private static final int W = 400;
	private static final int H = 212;
	private static final int ROW = 18;
	private static final int BTN = 16;
	private static final int TEMPLATE_W = 90;
	private static final int WORD_W = 66;
	private static final int WORD_COLS = 3;

	private final BlockPos pos;
	private String template = MessageCatalog.TEMPLATES.get(0);
	private String word;
	private MessageCatalog.Category category = MessageCatalog.Category.CREATURE;
	private ButtonWidget writeButton;

	public GroundMessageScreen(BlockPos pos) {
		super(Text.translatable("screen.aptitudes.ground_message.title"));
		this.pos = pos.toImmutable();
	}

	private int left() { return (width - W) / 2; }

	private int top() { return Math.max(4, (height - H) / 2); }

	@Override
	protected void init() {
		int x0 = left();
		int y0 = top() + 26;
		// Plantillas: dos columnas de seis.
		for (int i = 0; i < MessageCatalog.TEMPLATES.size(); i++) {
			String t = MessageCatalog.TEMPLATES.get(i);
			int x = x0 + (i / 6) * (TEMPLATE_W + 4);
			int y = y0 + (i % 6) * ROW;
			ButtonWidget b = ButtonWidget.builder(templateLabel(t), btn -> {
				template = t;
				clearAndInit();
			}).dimensions(x, y, TEMPLATE_W, BTN).build();
			b.active = !t.equals(template);
			addDrawableChild(b);
		}
		// Categorías y palabras.
		int rx = x0 + 2 * (TEMPLATE_W + 4) + 12;
		int rw = WORD_COLS * (WORD_W + 2) - 2;
		MessageCatalog.Category[] cats = MessageCatalog.Category.values();
		int tabW = (rw - (cats.length - 1) * 2) / cats.length;
		for (int i = 0; i < cats.length; i++) {
			MessageCatalog.Category c = cats[i];
			ButtonWidget b = ButtonWidget.builder(Text.translatable(MessageCatalog.categoryKey(c)), btn -> {
				category = c;
				clearAndInit();
			}).dimensions(rx + i * (tabW + 2), top() + 6, tabW, BTN).build();
			b.active = c != category;
			addDrawableChild(b);
		}
		List<String> words = new ArrayList<>();
		for (Map.Entry<String, MessageCatalog.Category> e : MessageCatalog.words().entrySet()) {
			if (e.getValue() == category) words.add(e.getKey());
		}
		for (int i = 0; i < words.size(); i++) {
			String w = words.get(i);
			int x = rx + (i % WORD_COLS) * (WORD_W + 2);
			int y = y0 + (i / WORD_COLS) * ROW;
			ButtonWidget b = ButtonWidget.builder(Text.translatable(MessageCatalog.wordKey(w)), btn -> {
				word = w;
				clearAndInit();
			}).dimensions(x, y, WORD_W, BTN).build();
			b.active = !w.equals(word);
			addDrawableChild(b);
		}
		int by = top() + H - 22;
		writeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.ground_message.write"), b -> {
			if (MessageCatalog.isValid(template, word)) ClientPlayNetworking.send(new GroundMessageWritePayload(pos, template, word));
			close();
		}).dimensions(width / 2 - 104, by, 100, 20).build());
		writeButton.active = MessageCatalog.isValid(template, word);
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.ground_message.cancel"), b -> close())
				.dimensions(width / 2 + 4, by, 100, 20).build());
	}

	/** La plantilla sin el hueco, para que quepa en el botón («Cuidado: …»). */
	private static Text templateLabel(String t) {
		return Text.translatable(MessageCatalog.templateKey(t), "…");
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		int x0 = left();
		ctx.drawTextWithShadow(textRenderer, title, x0, top() + 2, 0xF2E8D5);
		ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.ground_message.templates").formatted(Formatting.GRAY),
				x0, top() + 14, 0xAAAAAA);
		Text preview = word == null
				? Text.translatable(MessageCatalog.templateKey(template), "…").formatted(Formatting.DARK_GRAY)
				: ClientGroundMessages.phrase(template, word).copy().formatted(Formatting.WHITE);
		ctx.drawCenteredTextWithShadow(textRenderer, preview, width / 2, top() + H - 38, 0xFFFFFF);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
