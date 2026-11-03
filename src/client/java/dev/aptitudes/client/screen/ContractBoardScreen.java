package dev.aptitudes.client.screen;

import dev.aptitudes.client.contract.ContractClientState;
import dev.aptitudes.contract.ContractRules;
import dev.aptitudes.contract.ContractTargets;
import dev.aptitudes.network.ContractActionPayload;
import dev.aptitudes.network.ContractViewPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

/**
 * Tablón de contratos (v30). Solo muestra lo que envía el servidor ({@link ContractViewPayload}) y manda acciones
 * C2S; los botones se reconstruyen cada vez que llega una vista nueva. Mide 420×230 (cabe en 426×240).
 */
public final class ContractBoardScreen extends HandledScreen<dev.aptitudes.contract.ContractBoardScreenHandler> {
	private static final int W = 420;
	private static final int H = 230;
	private static final int OFFERS_Y = 34;
	private static final int OFFER_H = 26;
	private static final int ACTIVE_Y = 124;
	private static final int ACTIVE_H = 32;
	private ContractViewPayload shown;
	private boolean requested;

	public ContractBoardScreen(dev.aptitudes.contract.ContractBoardScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		backgroundWidth = W;
		backgroundHeight = H;
		super.init();
		titleX = 10;
		titleY = 8;
		playerInventoryTitleY = 10_000; // sin inventario
		shown = ContractClientState.get();
		if (!requested) {
			requested = true;
			send(ContractActionPayload.REQUEST, 0);
		}
		if (shown == null) return;
		int activeCount = shown.active().size();
		for (ContractViewPayload.Row r : shown.offers()) {
			ButtonWidget accept = ButtonWidget.builder(Text.translatable("contract.aptitudes.action.accept"),
					b -> send(ContractActionPayload.ACCEPT, r.index())).dimensions(x + W - 90, y + OFFERS_Y + r.index() * OFFER_H + 2, 80, 18).build();
			accept.active = !r.taken() && activeCount < shown.activeLimit();
			addDrawableChild(accept);
		}
		for (int slot = 0; slot < shown.active().size(); slot++) {
			ContractViewPayload.Row r = shown.active().get(slot);
			int by = y + ACTIVE_Y + slot * ACTIVE_H + 2;
			final int s = slot;
			addDrawableChild(ButtonWidget.builder(Text.translatable("contract.aptitudes.action.abandon"),
					b -> send(ContractActionPayload.ABANDON, s)).dimensions(x + W - 186, by, 60, 18).build());
			if (r.kind() == ContractRules.Kind.DELIVERY.ordinal()) {
				ButtonWidget deliver = ButtonWidget.builder(Text.translatable("contract.aptitudes.action.deliver"),
						b -> send(ContractActionPayload.DELIVER, s)).dimensions(x + W - 124, by, 56, 18).build();
				deliver.active = r.progress() < r.goal();
				addDrawableChild(deliver);
			}
			ButtonWidget claim = ButtonWidget.builder(Text.translatable("contract.aptitudes.action.claim"),
					b -> send(ContractActionPayload.CLAIM, s)).dimensions(x + W - 66, by, 56, 18).build();
			claim.active = r.progress() >= r.goal();
			addDrawableChild(claim);
		}
	}

	private void send(int action, int index) {
		if (ClientPlayNetworking.canSend(ContractActionPayload.ID)) ClientPlayNetworking.send(new ContractActionPayload(action, index));
	}

	@Override
	protected void handledScreenTick() {
		super.handledScreenTick();
		if (ContractClientState.get() != shown) clearAndInit(); // llegó una vista nueva del servidor
	}

	@Override
	protected void drawBackground(DrawContext c, float delta, int mouseX, int mouseY) {
		c.fill(x, y, x + W, y + H, 0xE5221710);
		c.fill(x + 3, y + 3, x + W - 3, y + H - 3, 0xEE33251B);
		c.fill(x + 8, y + ACTIVE_Y - 6, x + W - 8, y + ACTIVE_Y - 5, 0x60F2D28A);
	}

	@Override
	protected void drawForeground(DrawContext c, int mouseX, int mouseY) {
		c.drawText(textRenderer, title, titleX, titleY, 0xF2D28A, false);
	}

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		renderBackground(c, mouseX, mouseY, delta);
		super.render(c, mouseX, mouseY, delta);
		ContractViewPayload view = shown;
		if (view == null) {
			c.drawCenteredTextWithShadow(textRenderer, Text.translatable("contract.aptitudes.loading"), x + W / 2, y + 60, 0xFFFFFF);
			return;
		}
		Text rank = Text.translatable("contract.aptitudes.rank." + Math.max(0, Math.min(3, view.rank())));
		c.drawTextWithShadow(textRenderer, Text.translatable("contract.aptitudes.header", rank, view.reputation(),
				view.active().size(), view.activeLimit()), x + 10, y + 20, 0xF2D28A);

		c.drawTextWithShadow(textRenderer, Text.translatable("contract.aptitudes.offers").formatted(Formatting.BOLD), x + 10, y + OFFERS_Y - 4, 0xE0C9A0);
		for (ContractViewPayload.Row r : view.offers()) {
			int ry = y + OFFERS_Y + 6 + r.index() * OFFER_H;
			int color = r.taken() ? 0x8A8070 : 0xFFFFFF;
			c.drawText(textRenderer, line(r), x + 10, ry, color, false);
			c.drawText(textRenderer, rewards(r), x + 18, ry + 10, r.taken() ? 0x6E665A : 0xBFB59A, false);
		}
		c.drawTextWithShadow(textRenderer, Text.translatable("contract.aptitudes.active").formatted(Formatting.BOLD), x + 10, y + ACTIVE_Y, 0xE0C9A0);
		if (view.active().isEmpty()) {
			c.drawText(textRenderer, Text.translatable("contract.aptitudes.none"), x + 18, y + ACTIVE_Y + 14, 0x8A8070, false);
		}
		for (int slot = 0; slot < view.active().size(); slot++) {
			ContractViewPayload.Row r = view.active().get(slot);
			int ry = y + ACTIVE_Y + 12 + slot * ACTIVE_H;
			boolean done = r.progress() >= r.goal();
			c.drawText(textRenderer, line(r), x + 10, ry, done ? 0x9CF29C : 0xBFE9C7, false);
			int barW = 150;
			float frac = r.goal() <= 0 ? 0f : Math.min(1f, r.progress() / (float) r.goal());
			c.fill(x + 18, ry + 11, x + 18 + barW, ry + 15, 0x80000000);
			c.fill(x + 18, ry + 11, x + 18 + (int) (barW * frac), ry + 15, done ? 0xFF5EC75E : 0xFFC9A54C);
			c.drawText(textRenderer, r.progress() + "/" + r.goal(), x + 24 + barW, ry + 9, 0xD8D0C0, false);
		}
		drawMouseoverTooltip(c, mouseX, mouseY);
	}

	/** «[Difícil] Caza: Zombi ×14» o «[Normal] Forja ×1». */
	private static Text line(ContractViewPayload.Row r) {
		ContractRules.Kind kind = ContractRules.Kind.values()[Math.max(0, Math.min(ContractRules.Kind.values().length - 1, r.kind()))];
		ContractRules.Difficulty diff = ContractRules.Difficulty.values()[Math.max(0, Math.min(ContractRules.Difficulty.values().length - 1, r.difficulty()))];
		Text d = Text.translatable("contract.aptitudes.difficulty." + diff.name().toLowerCase(Locale.ROOT));
		Text k = Text.translatable("contract.aptitudes.kind." + kind.name().toLowerCase(Locale.ROOT));
		Text target = ContractTargets.name(kind, r.target());
		return target.getString().isEmpty()
				? Text.translatable("contract.aptitudes.line_simple", d, k, r.goal())
				: Text.translatable("contract.aptitudes.line", d, k, target, r.goal());
	}

	private static Text rewards(ContractViewPayload.Row r) {
		MutableText t = Text.translatable("contract.aptitudes.rewards", r.emeralds(), r.roleXp(), r.reputation());
		if (r.fragments() > 0) t.append(Text.translatable("contract.aptitudes.reward.fragments", r.fragments()));
		if (r.voucher()) t.append(Text.translatable("contract.aptitudes.reward.voucher"));
		if (r.emblem()) t.append(Text.translatable("contract.aptitudes.reward.emblem"));
		return t;
	}

	@Override
	public void removed() {
		super.removed();
		ContractClientState.clear();
	}
}
