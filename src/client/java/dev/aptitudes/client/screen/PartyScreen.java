package dev.aptitudes.client.screen;

import dev.aptitudes.client.PartyClientState;
import dev.aptitudes.network.PartyActionPayload;
import dev.aptitudes.network.PartySyncPayload;
import dev.aptitudes.role.Role;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Menú compacto para administrar invitaciones, miembros y reglas de grupo. */
public final class PartyScreen extends Screen {
	private int panelX;
	private int panelY;
	private int panelW;
	private int onlineScroll;
	private PartySyncPayload view;

	public PartyScreen() {
		super(Text.translatable("party.gui.title"));
	}

	@Override
	protected void init() {
		panelW = Math.min(420, width - 12);
		panelX = (width - panelW) / 2;
		panelY = Math.max(8, (height - 232) / 2);
		view = PartyClientState.get();
		int columnW = (panelW - 30) / 2;
		int top = panelY + 44;
		if (view != null && view.leader()) {
			addDrawableChild(ButtonWidget.builder(Text.literal("−5"), button -> send(PartyActionPayload.SET_SHARE, null,
					Math.max(0, view.sharePercent() - 5))).dimensions(panelX + 12, panelY + 24, 38, 18).build());
			addDrawableChild(ButtonWidget.builder(Text.literal("+5"), button -> send(PartyActionPayload.SET_SHARE, null,
					Math.min(50, view.sharePercent() + 5))).dimensions(panelX + 54, panelY + 24, 38, 18).build());
			addDrawableChild(ButtonWidget.builder(Text.translatable("party.gui.pvp"), button ->
					send(PartyActionPayload.TOGGLE_FRIENDLY_FIRE, null, 0))
					.dimensions(panelX + 102, panelY + 24, Math.max(65, columnW - 20), 18).build());
		}
		addDrawableChild(ButtonWidget.builder(Text.translatable("party.gui.leave"), button -> {
			send(PartyActionPayload.LEAVE, null, 0);
			close();
		}).dimensions(panelX + panelW - 94, panelY + 24, 82, 18).build());

		if (view != null) {
			int rowH = 22;
			for (int i = 0; i < view.members().size(); i++) {
				PartySyncPayload.Member member = view.members().get(i);
				if (view.leader() && !member.leader()) {
					int y = top + 15 + i * rowH;
					addDrawableChild(ButtonWidget.builder(Text.literal("×"), button ->
							send(PartyActionPayload.KICK, member.id(), 0))
						.dimensions(panelX + columnW - 25, y, 20, 18).build());
				}
			}
			int visible = Math.max(1, (height - (top + 46) - 30) / 22);
			var available = view.onlinePlayers().stream().filter(player -> !player.grouped()).toList();
			onlineScroll = Math.min(onlineScroll, Math.max(0, available.size() - visible));
			for (int i = 0; i < available.size(); i++) {
				PartySyncPayload.OnlinePlayer player = available.get(i);
				int y = top + 15 + (i - onlineScroll) * rowH;
				ButtonWidget invite = ButtonWidget.builder(Text.translatable("party.gui.invite"), button ->
						send(PartyActionPayload.INVITE, player.id(), 0))
					.dimensions(panelX + panelW - 64, y, 56, 18).build();
				invite.visible = i >= onlineScroll && i < onlineScroll + visible;
				invite.active = view.leader() || !view.grouped();
				addDrawableChild(invite);
			}
		}
		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
				.dimensions(width / 2 - 40, height - 26, 80, 18).build());
		if (ClientPlayNetworking.canSend(PartyActionPayload.ID)) ClientPlayNetworking.send(
				new PartyActionPayload(PartyActionPayload.REQUEST_SYNC, null, 0));
	}

	private void send(int action, java.util.UUID target, int value) {
		if (ClientPlayNetworking.canSend(PartyActionPayload.ID)) {
			ClientPlayNetworking.send(new PartyActionPayload(action, target, value));
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (PartyClientState.get() != null && PartyClientState.get() != view) {
			view = PartyClientState.get();
			clearAndInit();
		}
			renderBackground(context, mouseX, mouseY, delta);
			context.fill(panelX, panelY, panelX + panelW, panelY + 232, 0xE8101822);
			context.drawCenteredTextWithShadow(textRenderer, title, width / 2, panelY + 8, 0xFFFFD66B);
			if (view == null) {
				context.drawCenteredTextWithShadow(textRenderer, Text.translatable("party.gui.loading"), width / 2, panelY + 85, 0xFFFFFFFF);
			} else {
				if (!view.grouped()) context.drawTextWithShadow(textRenderer, Text.translatable("party.gui.none"),
						panelX + 10, panelY + 72, 0xFFCCCCCC);
				context.drawTextWithShadow(textRenderer, Text.translatable("party.gui.share", view.sharePercent()),
						panelX + 12, panelY + 29, view.leader() ? 0xFFFFD66B : 0xFFB0B0B0);
				context.drawTextWithShadow(textRenderer, Text.translatable(view.friendlyFire()
						? "party.gui.pvp_enabled" : "party.gui.pvp_disabled"), panelX + panelW - 180, panelY + 29, 0xFFCCCCCC);
			}
			int colW = (panelW - 30) / 2;
			int left = panelX + 10;
			int right = panelX + 20 + colW;
			int top = panelY + 47;
			context.drawTextWithShadow(textRenderer, Text.translatable("party.gui.members"), left, top, 0xFF90D6FF);
			context.drawTextWithShadow(textRenderer, Text.translatable("party.gui.players"), right, top, 0xFF90D6FF);
			if (view != null) {
				for (int i = 0; i < view.members().size(); i++) drawMember(context, view.members().get(i), left, top + 15 + i * 22);
				var available = view.onlinePlayers().stream().filter(player -> !player.grouped()).toList();
				int visible = Math.max(1, (height - (top + 46) - 30) / 22);
				for (int i = onlineScroll; i < Math.min(available.size(), onlineScroll + visible); i++) {
					int y = top + 15 + (i - onlineScroll) * 22;
					context.drawTextWithShadow(textRenderer, Text.literal(available.get(i).name()), right, y + 5, 0xFFFFFFFF);
				}
				if (available.size() > visible) context.drawTextWithShadow(textRenderer,
						Text.translatable("party.gui.scroll", onlineScroll + 1, Math.max(1, available.size() - visible + 1)),
						right, height - 44, 0xFFAAAAAA);
			}
			super.render(context, mouseX, mouseY, delta);
		}

	private void drawMember(DrawContext context, PartySyncPayload.Member member, int x, int y) {
		int color = !member.online() || !member.sameDimension() ? 0xFF888888 : 0xFFFFFFFF;
		Role role = Role.byIndex(member.role());
		String label = (member.leader() ? "★ " : "") + member.name();
		if (role != null) context.drawItem(role.iconStack(), x, y + 2);
		else context.drawTextWithShadow(textRenderer, Text.literal("·").formatted(Formatting.AQUA), x + 3, y + 4, color);
		context.drawTextWithShadow(textRenderer, Text.literal(label), x + 18, y + 4, color);
		if (member.online() && member.sameDimension()) {
			int barX = x + 18;
			int barY = y + 15;
			context.fill(barX, barY, barX + 72, barY + 3, 0xFF3B2020);
			context.fill(barX, barY, barX + Math.round(72 * member.health()), barY + 3, 0xFF62C86F);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (view == null) return false;
			int visible = Math.max(1, (height - (panelY + 93) - 30) / 22);
			int max = Math.max(0, view.onlinePlayers().stream().filter(p -> !p.grouped()).toList().size() - visible);
			onlineScroll = Math.max(0, Math.min(max, onlineScroll - (int) Math.signum(verticalAmount)));
			clearAndInit();
			return true;
		}

	@Override
	public boolean shouldPause() {
			return false;
		}
}
