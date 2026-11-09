package dev.aptitudes.client.screen;

import dev.aptitudes.item.SoulFlaskItem;
import dev.aptitudes.network.FlaskQuotePayload;
import dev.aptitudes.network.ForgeActionPayload;
import dev.aptitudes.souls.FlaskRules;
import dev.aptitudes.souls.FlaskScreenHandler;
import dev.aptitudes.souls.FlaskUpgrades;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class FlaskScreen extends HandledScreen<FlaskScreenHandler> {
	private static final String KEY = "flask.aptitudes.";
	private static final int CARD_WIDTH = 88;
	private FlaskQuotePayload quote;
	private long pending;
	private ButtonWidget confirm;

	public FlaskScreen(FlaskScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = FlaskScreenHandler.WIDTH;
		backgroundHeight = FlaskScreenHandler.HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		confirm = addDrawableChild(ButtonWidget.builder(Text.translatable("forge.aptitudes.confirm"), b -> {
			if (!canConfirm()) return;
			pending = quote.revision();
			ClientPlayNetworking.send(new ForgeActionPayload(handler.syncId, ForgeActionPayload.CONFIRM_FLASKS, pending));
			b.active = false;
		}).dimensions(x + 177, y + 109, 110, 18).build());
		confirm.active = canConfirm();
	}

	public void setQuote(FlaskQuotePayload payload) {
		if (payload.syncId() == handler.syncId) quote = payload;
	}

	private boolean canConfirm() {
		return quote != null && quote.revision() != 0 && quote.revision() != pending
				&& ClientPlayNetworking.canSend(ForgeActionPayload.ID);
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		UiStyle.window(ctx, x, y, backgroundWidth, backgroundHeight);
		UiStyle.panel(ctx, x + 12, y + 67, backgroundWidth - 24, 36);
		UiStyle.panel(ctx, x + 12, y + 138, backgroundWidth - 24, 86);
		for (int i = 0; i < handler.slots.size(); i++) {
			var slot = handler.getSlot(i);
			int sx = x + slot.x;
			int sy = y + slot.y;
			if (i < 3) UiStyle.panel(ctx, sx - 4, y + 24, CARD_WIDTH, 35);
			ctx.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF10121C);
			ctx.drawBorder(sx - 1, sy - 1, 18, 18, i < 3 ? UiStyle.GOLD_DARK : UiStyle.BORDER);
		}
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		UiStyle.label(ctx, textRenderer, title, 12, 9, backgroundWidth - 24, UiStyle.GOLD);
		UiStyle.label(ctx, textRenderer, playerInventoryTitle, 16, 140, 150, UiStyle.MUTED);
		ItemStack flask = handler.getSlot(FlaskScreenHandler.FLASK).getStack();
		int rank = FlaskUpgrades.rank(flask);
		var next = quote == null || !FlaskUpgrades.isFlask(flask) ? null : FlaskRules.next(rank, quote.limit());
		drawInput(ctx, FlaskScreenHandler.FLASK, KEY + "input.flask", 0);
		drawInput(ctx, FlaskScreenHandler.CORE, KEY + "input.core", next == null ? 0 : 1);
		drawInput(ctx, FlaskScreenHandler.SHARDS, "forge.aptitudes.input.shards", next == null ? 0 : next.shards());
		if (!flask.isEmpty() && flask.getItem() instanceof SoulFlaskItem item) {
			drawStats(ctx, item.kind(), "current", rank, 74, UiStyle.TEXT);
			if (next != null) drawStats(ctx, item.kind(), "next", next.rank(), 88, UiStyle.GOLD);
		}
		if (quote == null) return;
		Text status = Text.translatable(!FlaskUpgrades.isFlask(flask) ? KEY + "insert"
				: next == null ? KEY + "limit"
				: quote.revision() != 0 ? "forge.aptitudes.status.ready" : "forge.aptitudes.status.missing");
		UiStyle.label(ctx, textRenderer, status, 15, 114, 157, canConfirm() ? UiStyle.GOOD : UiStyle.MUTED);
	}

	private void drawInput(DrawContext ctx, int index, String key, int required) {
		var slot = handler.getSlot(index);
		UiStyle.label(ctx, textRenderer, Text.translatable(key), slot.x + 21, required > 0 ? 30 : 37, 58, UiStyle.TEXT);
		if (required > 0) {
			int count = slot.getStack().getCount();
			ctx.drawText(textRenderer, count + "/" + required, slot.x + 21, 43,
					count >= required ? UiStyle.GOOD : UiStyle.MUTED, false);
		}
	}

	private void drawStats(DrawContext ctx, SoulFlaskItem.Kind kind, String key, int rank, int y, int color) {
		Text recovery = SoulFlaskItem.recovery(kind, rank);
		Text stats = Text.translatable(KEY + key, rank, FlaskRules.capacity(rank), recovery);
		UiStyle.label(ctx, textRenderer, stats, 16, y, backgroundWidth - 32, color);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		confirm.active = canConfirm();
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
}
