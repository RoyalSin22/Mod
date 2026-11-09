package dev.aptitudes.client.screen;

import dev.aptitudes.client.contract.ContractClientState;
import dev.aptitudes.client.contract.OrderClientState;
import dev.aptitudes.contract.ContractBoardScreenHandler;
import dev.aptitudes.contract.ContractRules;
import dev.aptitudes.contract.ContractTargets;
import dev.aptitudes.contract.OrderBookRules;
import dev.aptitudes.network.ContractActionPayload;
import dev.aptitudes.network.ContractViewPayload;
import dev.aptitudes.network.ContractViewPayload.Row;
import dev.aptitudes.network.OrderActionPayload;
import dev.aptitudes.network.OrderViewPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;

/**
 * Presentación del tablón de contratos validada por el servidor. Tiene dos páginas: contratos (v30) y pedidos entre
 * jugadores (F4). El cliente solo muestra datos y envía peticiones; el servidor valida cada acción.
 */
public final class ContractBoardScreen extends HandledScreen<ContractBoardScreenHandler> {
	private static final int W = 420;
	private static final int H = 230;
	private static final int OFFERS_X = 12;
	private static final int ACTIVE_X = 216;
	private static final int CARD_W = 192;
	private static final int FULL_W = W - 24;
	private static final int CARD_H = 52;
	private static final int FIRST_Y = 51;
	private static final int STEP = 56;
	private static final int PAD = 8;
	private static final int ACTION_Y = 34;
	private static final int ACTION_H = 16;
	private static final int ACTION_W = 56;
	private static final int ORDER_ACTION_W = 80;
	private static final int ORDER_ROWS = 3;
	private static final int TOP_Y = 4;
	private static final int TOP_H = 14;
	private static final int BRASS = 0xFFE1B978;
	private static final int PARCHMENT = 0xFFE4D5B8;
	private static final int MUTED = 0xFFB3A48B;
	private static final int JADE = 0xFF91CAAA;

	private ContractViewPayload shown;
	private OrderViewPayload ordersShown;
	private boolean requested;
	private boolean ordersPage;
	/** Nivel con el que se publica el siguiente pedido (1–4). */
	private int tier = 1;
	/** Página de la lista de pedidos (0 = primera). */
	private int orderPage;

	public ContractBoardScreen(ContractBoardScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = W;
		backgroundHeight = H;
	}

	@Override
	protected void init() {
		super.init();
		shown = ContractClientState.get();
		ordersShown = OrderClientState.get();
		orderPage = Math.min(orderPage, pageCount() - 1);
		if (!requested) {
			requested = true;
			send(ContractActionPayload.REQUEST, 0);
			sendOrder(OrderActionPayload.REQUEST, 0, 0);
		}
		addPageButtons();
		if (ordersPage) {
			initOrderButtons();
		} else {
			initContractButtons();
		}
	}

	// ------------------------------------------------------------------ Botones

	private void addPageButtons() {
		Text label = Text.translatable(ordersPage ? "contract.aptitudes.page.contracts" : "contract.aptitudes.page.orders");
		addTop(label, x + W - PAD - 80, 80, b -> {
			ordersPage = !ordersPage;
			clearAndInit();
		}, true);
		if (ordersPage) {
			addTop(Text.translatable("order.aptitudes.tier", tier), x + W - PAD - 80 - 6 - 40 - 6 - 70, 40, b -> {
				tier = tier % 4 + 1;
				clearAndInit();
			}, true);
			addTop(Text.translatable("order.aptitudes.publish"), x + W - PAD - 80 - 6 - 70, 70,
					b -> sendOrder(OrderActionPayload.PUBLISH, 0, tier), true);
			if (pageCount() > 1) {
				addBottom("<", x + W / 2 - 34, 14, b -> {
					orderPage = Math.max(0, orderPage - 1);
					clearAndInit();
				}, orderPage > 0);
				addBottom(">", x + W / 2 + 20, 14, b -> {
					orderPage = Math.min(pageCount() - 1, orderPage + 1);
					clearAndInit();
				}, orderPage < pageCount() - 1);
			}
		}
	}

	/** Pedidos de la página actual (tres por página). */
	private List<OrderViewPayload.Row> pageRows() {
		if (ordersShown == null) return List.of();
		List<OrderViewPayload.Row> all = ordersShown.rows();
		int from = Math.min(orderPage * ORDER_ROWS, all.size());
		return all.subList(from, Math.min(from + ORDER_ROWS, all.size()));
	}

	/** Número de páginas (al menos 1). */
	private int pageCount() {
		int size = ordersShown == null ? 0 : ordersShown.rows().size();
		return Math.max(1, (size + ORDER_ROWS - 1) / ORDER_ROWS);
	}

	private void addBottom(String label, int left, int width, ButtonWidget.PressAction action, boolean active) {
		ButtonWidget widget = ButtonWidget.builder(Text.literal(label), action)
				.dimensions(left, this.y + H - 14, width, 12).build();
		widget.active = active;
		addDrawableChild(widget);
	}

	private void addTop(Text label, int left, int width, ButtonWidget.PressAction action, boolean active) {
		ButtonWidget widget = ButtonWidget.builder(UiStyle.fit(textRenderer, label, width - 8), action)
				.dimensions(this.x + left, this.y + TOP_Y, width, TOP_H).tooltip(Tooltip.of(label)).build();
		widget.active = active;
		addDrawableChild(widget);
	}

	private void initContractButtons() {
		if (shown == null) return;
		boolean hasRoom = shown.active().size() < shown.activeLimit();
		for (int i = 0; i < shown.offers().size(); i++) {
			Row row = shown.offers().get(i);
			addAction("accept", OFFERS_X + CARD_W - PAD - 80, i, 80,
					ContractActionPayload.ACCEPT, row.index(), !row.taken() && hasRoom);
		}
		for (int i = 0; i < shown.active().size(); i++) {
			Row row = shown.active().get(i);
			boolean done = row.progress() >= row.goal();
			addAction("abandon", ACTIVE_X + PAD, i, ACTION_W, ContractActionPayload.ABANDON, row.index(), true);
			if (row.kind() == ContractRules.Kind.DELIVERY.ordinal()) {
				addAction("deliver", ACTIVE_X + PAD + 60, i, ACTION_W, ContractActionPayload.DELIVER, row.index(), !done);
			}
			addAction("claim", ACTIVE_X + CARD_W - PAD - ACTION_W, i, ACTION_W,
					ContractActionPayload.CLAIM, row.index(), done);
		}
	}

	private void initOrderButtons() {
		if (ordersShown == null) return;
		List<OrderViewPayload.Row> page = pageRows();
		for (int i = 0; i < page.size(); i++) {
			OrderViewPayload.Row row = page.get(i);
			int left = FULL_W - PAD - ORDER_ACTION_W + 12;
			Text label;
			int action;
			boolean active;
			if (row.mine() && !row.closed()) {
				label = Text.translatable("order.aptitudes.button.cancel");
				action = OrderActionPayload.CANCEL;
				active = true;
			} else if (row.mine()) {
				label = Text.translatable("order.aptitudes.button.claim");
				action = OrderActionPayload.CLAIM;
				active = row.delivered() > row.claimed();
			} else {
				label = Text.translatable("order.aptitudes.button.deliver");
				action = OrderActionPayload.DELIVER;
				active = !row.closed() && row.delivered() < row.count();
			}
			ButtonWidget widget = ButtonWidget.builder(UiStyle.fit(textRenderer, label, ORDER_ACTION_W - 8),
							b -> sendOrder(action, row.id(), 0))
					.dimensions(x + left, y + rowY(i) + ACTION_Y, ORDER_ACTION_W, ACTION_H).tooltip(Tooltip.of(label)).build();
			widget.active = active;
			addDrawableChild(widget);
		}
	}

	private void addAction(String action, int left, int i, int buttonWidth, int packet, int index, boolean active) {
		Text label = Text.translatable("contract.aptitudes.action." + action);
		ButtonWidget widget = ButtonWidget.builder(UiStyle.fit(textRenderer, label, buttonWidth - 8), b -> send(packet, index))
				.dimensions(x + left, y + rowY(i) + ACTION_Y, buttonWidth, ACTION_H).tooltip(Tooltip.of(label)).build();
		widget.active = active;
		addDrawableChild(widget);
	}

	private static int rowY(int i) {
		return FIRST_Y + i * STEP;
	}

	private void send(int action, int index) {
		if (ClientPlayNetworking.canSend(ContractActionPayload.ID)) ClientPlayNetworking.send(new ContractActionPayload(action, index));
	}

	private void sendOrder(int action, long id, int tierValue) {
		if (ClientPlayNetworking.canSend(OrderActionPayload.ID)) {
			ClientPlayNetworking.send(new OrderActionPayload(action, id, tierValue));
		}
	}

	@Override
	protected void handledScreenTick() {
		super.handledScreenTick();
		if (ContractClientState.get() != shown || OrderClientState.get() != ordersShown) clearAndInit();
	}

	// ------------------------------------------------------------------ Dibujo

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		UiStyle.window(ctx, x, y, W, H);
		ctx.fill(x + 2, y + 2, x + W - 2, y + H - 2, 0xFF221B1A);
		ctx.fill(x + 2, y + 3, x + W - 2, y + 32, 0xFF382822);
		ctx.fill(x + OFFERS_X, y + 32, x + W - OFFERS_X, y + 33, 0xFF7D5841);
		if (ordersPage) {
			drawOrderBackground(ctx);
			return;
		}
		ctx.fill(x + 209, y + 42, x + 211, y + H - 9, 0xFF4E3931);
		if (shown == null) return;
		for (int i = 0; i < shown.offers().size(); i++) {
			drawCard(ctx, OFFERS_X, CARD_W, i, shown.offers().get(i).taken() ? 0xFF857564 : BRASS);
		}
		for (int i = 0; i < shown.active().size(); i++) {
			Row row = shown.active().get(i);
			drawCard(ctx, ACTIVE_X, CARD_W, i, row.progress() >= row.goal() ? JADE : BRASS);
		}
	}

	private void drawOrderBackground(DrawContext ctx) {
		if (ordersShown == null) return;
		List<OrderViewPayload.Row> page = pageRows();
		for (int i = 0; i < page.size(); i++) {
			OrderViewPayload.Row row = page.get(i);
			int accent = row.closed() ? 0xFF857564 : row.mine() ? JADE : BRASS;
			drawCard(ctx, OFFERS_X, FULL_W, i, accent);
		}
	}

	private void drawCard(DrawContext ctx, int col, int width, int index, int accent) {
		int rx = x + col;
		int ry = y + rowY(index);
		ctx.fill(rx, ry, rx + width, ry + CARD_H, index % 2 == 0 ? 0xFF372B26 : 0xFF302723);
		ctx.drawBorder(rx, ry, width, CARD_H, 0xFF624D3F);
		ctx.fill(rx + 1, ry + 1, rx + 3, ry + CARD_H - 1, accent);
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		UiStyle.label(ctx, textRenderer, title, 12, 8, W - 24 - 200, BRASS);
		if (ordersPage) {
			drawOrders(ctx);
			return;
		}
		if (shown == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("contract.aptitudes.loading"), W / 2, 57, PARCHMENT);
			return;
		}
		Text rank = Text.translatable("contract.aptitudes.rank." + Math.clamp(shown.rank(), 0, 3));
		UiStyle.label(ctx, textRenderer, Text.translatable("contract.aptitudes.header", rank, shown.reputation(),
				shown.active().size(), shown.activeLimit()), 12, 20, W - 24, PARCHMENT);
		UiStyle.label(ctx, textRenderer, Text.translatable("contract.aptitudes.offers"), OFFERS_X, 38, CARD_W, BRASS);
		UiStyle.label(ctx, textRenderer, Text.translatable("contract.aptitudes.active"), ACTIVE_X, 38, CARD_W, JADE);
		for (int i = 0; i < shown.offers().size(); i++) {
			Row row = shown.offers().get(i);
			int ry = rowY(i);
			int tx = OFFERS_X + PAD;
			UiStyle.label(ctx, textRenderer, line(row), tx, ry + 5, CARD_W - 2 * PAD, row.taken() ? MUTED : PARCHMENT);
			var lines = textRenderer.wrapLines(rewards(row), CARD_W - 2 * PAD);
			for (int j = 0; j < Math.min(2, lines.size()); j++) {
				ctx.drawText(textRenderer, lines.get(j), tx, ry + 15 + j * 9, MUTED, false);
			}
		}
		if (shown.active().isEmpty()) {
			UiStyle.label(ctx, textRenderer, Text.translatable("contract.aptitudes.none"), ACTIVE_X + PAD, FIRST_Y + 8,
					CARD_W - 2 * PAD, MUTED);
		}
		for (int i = 0; i < shown.active().size(); i++) {
			Row row = shown.active().get(i);
			int ry = rowY(i);
			int tx = ACTIVE_X + PAD;
			int color = row.progress() >= row.goal() ? JADE : PARCHMENT;
			UiStyle.label(ctx, textRenderer, line(row), tx, ry + 5, CARD_W - 2 * PAD, color);
			String progress = row.progress() + "/" + row.goal();
			int progressX = ACTIVE_X + CARD_W - PAD - textRenderer.getWidth(progress);
			int barW = Math.max(0, progressX - tx - 6);
			float fraction = row.goal() <= 0 ? 0f : Math.clamp(row.progress() / (float) row.goal(), 0f, 1f);
			ctx.fill(tx, ry + 20, tx + barW, ry + 25, 0xFF181618);
			ctx.fill(tx, ry + 20, tx + Math.round(barW * fraction), ry + 25, color);
			ctx.drawText(textRenderer, progress, progressX, ry + 18, MUTED, false);
		}
	}

	private void drawOrders(DrawContext ctx) {
		if (ordersShown == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("contract.aptitudes.loading"), W / 2, 57, PARCHMENT);
			return;
		}
		UiStyle.label(ctx, textRenderer, Text.translatable("order.aptitudes.header", ordersShown.rows().size()),
				12, 20, W - 24, PARCHMENT);
		if (ordersShown.rows().isEmpty()) {
			UiStyle.label(ctx, textRenderer, Text.translatable("order.aptitudes.empty"), OFFERS_X + PAD, FIRST_Y + 8,
					FULL_W - 2 * PAD, MUTED);
			return;
		}
		List<OrderViewPayload.Row> page = pageRows();
		for (int i = 0; i < page.size(); i++) {
			OrderViewPayload.Row row = page.get(i);
			int ry = rowY(i);
			int tx = OFFERS_X + PAD;
			int color = row.closed() ? MUTED : PARCHMENT;
			UiStyle.label(ctx, textRenderer, orderLine(row), tx, ry + 5, FULL_W - 2 * PAD - ORDER_ACTION_W, color);
			ctx.drawText(textRenderer, Text.translatable("order.aptitudes.unit_price", OrderBookRules.unitPrice(row.tier())),
					tx, ry + 15, MUTED, false);
			String progress = row.delivered() + "/" + row.count();
			int barRight = OFFERS_X + FULL_W - PAD - ORDER_ACTION_W - 8;
			int barW = Math.max(0, barRight - tx);
			float fraction = row.count() <= 0 ? 0f : Math.clamp(row.delivered() / (float) row.count(), 0f, 1f);
			ctx.fill(tx, ry + 30, tx + barW, ry + 35, 0xFF181618);
			ctx.fill(tx, ry + 30, tx + Math.round(barW * fraction), ry + 35, color);
			ctx.drawText(textRenderer, progress, tx + barW + 4, ry + 28, MUTED, false);
		}
		int pages = pageCount();
		if (pages > 1) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("order.aptitudes.page", orderPage + 1, pages),
					W / 2, H - 14, MUTED);
		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		if (ordersPage || shown == null) return;
		int rx = mouseX - x;
		int ry = mouseY - y - FIRST_Y;
		if (ry < 0 || ry % STEP >= ACTION_Y) return;
		var rows = rx >= OFFERS_X && rx < OFFERS_X + CARD_W ? shown.offers()
				: rx >= ACTIVE_X && rx < ACTIVE_X + CARD_W ? shown.active() : null;
		int i = ry / STEP;
		if (rows != null && i < rows.size()) {
			Row row = rows.get(i);
			ctx.drawOrderedTooltip(textRenderer, textRenderer.wrapLines(line(row).copy().append("\n").append(rewards(row)), 250),
					mouseX, mouseY);
		}
	}

	private static Text line(Row row) {
		ContractRules.Kind[] kinds = ContractRules.Kind.values();
		ContractRules.Difficulty[] difficulties = ContractRules.Difficulty.values();
		ContractRules.Kind kind = kinds[Math.clamp(row.kind(), 0, kinds.length - 1)];
		ContractRules.Difficulty difficulty = difficulties[Math.clamp(row.difficulty(), 0, difficulties.length - 1)];
		Text d = Text.translatable("contract.aptitudes.difficulty." + difficulty.name().toLowerCase(Locale.ROOT));
		Text k = Text.translatable("contract.aptitudes.kind." + kind.name().toLowerCase(Locale.ROOT));
		Text target = ContractTargets.name(kind, row.target());
		return target.getString().isEmpty()
				? Text.translatable("contract.aptitudes.line_simple", d, k, row.goal())
				: Text.translatable("contract.aptitudes.line", d, k, target, row.goal());
	}

	private static Text orderLine(OrderViewPayload.Row row) {
		Identifier id = Identifier.tryParse(row.item());
		Item item = id == null ? Items.AIR : Registries.ITEM.get(id);
		Text name = item.getName();
		Text base = Text.translatable("order.aptitudes.line", name, row.count(), row.tier());
		if (row.closed()) return base.copy().append(Text.translatable("order.aptitudes.state.closed"));
		if (row.mine()) return base.copy().append(Text.translatable("order.aptitudes.state.mine"));
		return base;
	}

	private static Text rewards(Row row) {
		MutableText text = Text.translatable("contract.aptitudes.rewards", row.emeralds(), row.roleXp(), row.reputation());
		if (row.fragments() > 0) text.append(Text.translatable("contract.aptitudes.reward.fragments", row.fragments()));
		if (row.voucher()) text.append(Text.translatable("contract.aptitudes.reward.voucher"));
		if (row.emblem()) text.append(Text.translatable("contract.aptitudes.reward.emblem"));
		return text;
	}

	@Override
	public void removed() {
		super.removed();
		ContractClientState.clear();
		OrderClientState.clear();
	}
}
