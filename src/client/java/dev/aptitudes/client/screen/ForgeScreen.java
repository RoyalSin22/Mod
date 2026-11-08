package dev.aptitudes.client.screen;

import dev.aptitudes.forge.ForgeRules.Affix;
import dev.aptitudes.forge.ForgeRules.Profile;
import dev.aptitudes.forge.ForgeScreenHandler;
import dev.aptitudes.forge.ForgeWeapons;
import dev.aptitudes.network.ForgeActionPayload;
import dev.aptitudes.network.ForgePreviewPayload;
import dev.aptitudes.network.ForgePreviewPayload.Status;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.Locale;

public final class ForgeScreen extends HandledScreen<ForgeScreenHandler> {
	private static final int INK = 0xFF10121C;
	private static final int STEEL = 0xFF8AC4B0;
	private static final int CARD_X = 12;
	private static final int CARD_W = 104;
	private static final int CARD_STEP = 106;
	private static final int PROFILE_Y = 72;
	private static final int PROFILE_W = 152;
	private static final int PREVIEW_Y = 98;
	private final boolean reforge;
	private ForgePreviewPayload view;
	private ButtonWidget reserve;
	private ButtonWidget impetus;
	private ButtonWidget confirm;

	public ForgeScreen(ForgeScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = ForgeScreenHandler.WIDTH;
		backgroundHeight = ForgeScreenHandler.HEIGHT;
		reforge = handler.mode() == ForgeScreenHandler.Mode.REFORGE;
	}

	@Override
	protected void init() {
		super.init();
		if (reforge) {
			reserve = profileButton(Profile.RESERVE, ForgeActionPayload.SELECT_RESERVE, 12);
			impetus = profileButton(Profile.IMPETUS, ForgeActionPayload.SELECT_IMPETUS, 176);
		}
		confirm = addDrawableChild(ButtonWidget.builder(Text.translatable("forge.aptitudes.confirm"), button -> {
			if (view != null && view.revision() != 0) {
				send(ForgeActionPayload.CONFIRM, view.revision());
				button.active = false;
			}
		}).dimensions(x + 216, y + 129, 112, 18).build());
		updateButtons();
	}

	private ButtonWidget profileButton(Profile profile, int action, int offset) {
		return addDrawableChild(ButtonWidget.builder(ForgeWeapons.profileName(profile), button -> {
			send(action, 0);
			confirm.active = false;
		}).dimensions(x + offset, y + PROFILE_Y, PROFILE_W, 18).tooltip(Tooltip.of(ForgeWeapons.hint(profile))).build());
	}

	private void send(int action, long revision) {
		if (ClientPlayNetworking.canSend(ForgeActionPayload.ID)) {
			ClientPlayNetworking.send(new ForgeActionPayload(handler.syncId, action, revision));
		}
	}

	public void setPreview(ForgePreviewPayload payload) {
		if (handler.syncId != payload.syncId()) return;
		view = payload;
		if (confirm != null) updateButtons();
	}

	private void updateButtons() {
		if (reforge) {
			reserve.active = view != null && view.preview().selected() != Profile.RESERVE;
			impetus.active = view != null && view.preview().selected() != Profile.IMPETUS;
		}
		confirm.active = view != null && view.revision() != 0;
		confirm.setTooltip(view != null && !confirm.active ? Tooltip.of(status(view.preview().status())) : null);
	}

	@Override
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
		int accent = reforge ? STEEL : UiStyle.GOLD;
		UiStyle.window(ctx, x, y, backgroundWidth, backgroundHeight);
		ctx.fill(x + 1, y + 1, x + backgroundWidth - 1, y + 3, accent);
		ctx.fill(x + 2, y + 3, x + backgroundWidth - 2, y + 22, 0xFF25232A);
		for (int col = 0; col < 3; col++) {
			UiStyle.panel(ctx, x + CARD_X + col * CARD_STEP, y + 28, CARD_W, 35);
		}
		UiStyle.panel(ctx, x + 12, y + PREVIEW_Y, backgroundWidth - 24, 27);
		UiStyle.panel(ctx, x + 12, y + 152, backgroundWidth - 24, 85);
		for (int index = 0; index < handler.slots.size(); index++) {
			var slot = handler.getSlot(index);
			ctx.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, INK);
			ctx.drawBorder(x + slot.x - 1, y + slot.y - 1, 18, 18, index < 3 ? accent : UiStyle.BORDER);
		}
		if (reforge && view != null) {
			int selectedX = view.preview().selected() == Profile.RESERVE ? 12 : 176;
			ctx.drawBorder(x + selectedX - 1, y + PROFILE_Y - 1, PROFILE_W + 2, 20, accent);
		}
	}

	@Override
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
		int accent = reforge ? STEEL : UiStyle.GOLD;
		UiStyle.label(ctx, textRenderer, title, 12, 9, backgroundWidth - 24, accent);
		UiStyle.label(ctx, textRenderer, playerInventoryTitle, 22, 154, 155, UiStyle.MUTED);
		drawInput(ctx, 0, "weapon", 0);
		drawInput(ctx, 1, reforge ? "essence" : "heart", view == null ? 0 : view.preview().trophies());
		drawInput(ctx, 2, "shards", view == null ? 0 : view.preview().shards());
		if (view == null) return;
		var preview = view.preview();
		drawAffix(ctx, "current", preview.current(), PREVIEW_Y + 4, UiStyle.TEXT);
		drawAffix(ctx, "result", preview.result(), PREVIEW_Y + 16, accent);
		var lines = textRenderer.wrapLines(status(preview.status()), 193);
		for (int i = 0; i < Math.min(2, lines.size()); i++) {
			ctx.drawText(textRenderer, lines.get(i), 15, 130 + 9 * i,
					preview.status() == Status.READY ? UiStyle.GOOD : UiStyle.MUTED, false);
		}
	}

	private void drawInput(DrawContext ctx, int index, String key, int required) {
		int slotX = handler.getSlot(index).x;
		UiStyle.label(ctx, textRenderer, Text.translatable("forge.aptitudes.input." + key),
				slotX + 22, required > 0 ? 34 : 40, 68, UiStyle.TEXT);
		if (required > 0) {
			int count = handler.getSlot(index).getStack().getCount();
			ctx.drawText(textRenderer, count + "/" + required, slotX + 22, 46,
					count >= required ? UiStyle.GOOD : UiStyle.MUTED, false);
		}
	}

	private void drawAffix(DrawContext ctx, String key, Affix affix, int atY, int color) {
		Text description = Text.translatable("forge.aptitudes." + key,
				affix == null && key.equals("result") ? Text.literal("—") : ForgeWeapons.describe(affix));
		UiStyle.label(ctx, textRenderer, description, 16, atY, affix == null ? 300 : 178, color);
		if (affix != null) UiStyle.label(ctx, textRenderer, ForgeWeapons.effect(affix), 202, atY, 120, color);
	}

	private static Text status(Status status) {
		return Text.translatable("forge.aptitudes.status." + status.name().toLowerCase(Locale.ROOT));
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
		int localX = mouseX - x;
		int localY = mouseY - y;
		if (localY >= 28 && localY < 63 && localX >= CARD_X) {
			int input = (localX - CARD_X) / CARD_STEP;
			if (input < 3 && (localX - CARD_X) % CARD_STEP < CARD_W && handler.getSlot(input).getStack().isEmpty()) {
				String key = switch (input) {
					case 0 -> "weapon";
					case 1 -> reforge ? "essence" : "heart";
					default -> "shards";
				};
				ctx.drawTooltip(textRenderer, Text.translatable("forge.aptitudes.input." + key), mouseX, mouseY);
			}
		}
		if (view != null && mouseX >= x + 12 && mouseX < x + backgroundWidth - 12) {
			int row = mouseY - y;
			if (row >= PREVIEW_Y && row < PREVIEW_Y + 27) {
				Affix affix = row < PREVIEW_Y + 15 ? view.preview().current() : view.preview().result();
				if (affix != null) {
					ctx.drawTooltip(textRenderer, java.util.List.of(ForgeWeapons.describe(affix), ForgeWeapons.effect(affix),
									ForgeWeapons.hint(affix.profile())),
							mouseX, mouseY);
				}
			} else if (row >= 128 && row < 148 && mouseX < x + 211) {
				ctx.drawTooltip(textRenderer, status(view.preview().status()), mouseX, mouseY);
			}
		}
	}
}
