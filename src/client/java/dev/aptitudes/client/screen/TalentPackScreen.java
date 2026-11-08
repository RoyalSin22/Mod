package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.network.TalentCatalogPayload;
import dev.aptitudes.network.TalentPurchasePayload;
import dev.aptitudes.role.PathEffect;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleTree;
import dev.aptitudes.role.datapack.TalentProgressRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class TalentPackScreen extends Screen {
	private static final String KEY = "screen.aptitudes.datapack.";
	private static final int ROW_HEIGHT = 18;

	private final RolesScreen parent;
	private final Role role;
	private final List<ButtonWidget> rows = new ArrayList<>();
	private TalentCatalogPayload catalog = TalentCatalogPayload.EMPTY;
	private List<Map.Entry<String, TalentCatalogPayload.Entry>> nodes = List.of();
	private String selected;
	private int firstRow;
	private int left;
	private int top;
	private int panelWidth;
	private int panelHeight;
	private int listWidth;
	private int detailX;
	private int detailWidth;
	private ButtonWidget up;
	private ButtonWidget down;
	private ButtonWidget learn;

	TalentPackScreen(RolesScreen parent, Role role) {
		super(Text.translatable(KEY + "tab"));
		this.parent = parent;
		this.role = role;
	}

	@Override
	protected void init() {
		panelWidth = Math.min(420, width - 8);
		panelHeight = Math.min(222, height - 8);
		left = (width - panelWidth) / 2;
		top = (height - panelHeight) / 2;
		listWidth = panelWidth * 42 / 100;
		detailX = left + listWidth + 18;
		detailWidth = left + panelWidth - detailX - 8;
		rows.clear();
		int rowCount = Math.max(1, (panelHeight - 62) / ROW_HEIGHT);
		for (int i = 0; i < rowCount; i++) {
			int row = i;
			rows.add(addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
				selected = nodes.get(firstRow + row).getKey();
				updateButtons();
			}).dimensions(left + 10, top + 31 + i * ROW_HEIGHT, listWidth - 4, 16).build()));
		}
		int footer = top + panelHeight - 24;
		up = addDrawableChild(ButtonWidget.builder(Text.literal("▲"), b -> scroll(-1))
				.dimensions(left + 10, footer, 18, 16).build());
		down = addDrawableChild(ButtonWidget.builder(Text.literal("▼"), b -> scroll(1))
				.dimensions(left + 30, footer, 18, 16).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.back"), b -> close())
				.dimensions(left + 50, footer, listWidth - 44, 16).build());
		learn = addDrawableChild(ButtonWidget.builder(ScreenTexts.EMPTY, b -> learn())
				.dimensions(detailX + 2, footer, detailWidth - 4, 16).build());
		updateCatalog();
	}

	private void updateCatalog() {
		catalog = ClientRoleState.talents();
		nodes = catalog.entries().entrySet().stream()
				.filter(node -> node.getValue().role() == role)
				.sorted(Comparator.<Map.Entry<String, TalentCatalogPayload.Entry>>comparingInt(node -> depth(node.getValue()))
						.thenComparingInt(node -> node.getValue().level()).thenComparing(Map.Entry::getKey))
				.toList();
		if (selection() == null || selection().role() != role) selected = nodes.isEmpty() ? null : nodes.getFirst().getKey();
		firstRow = Math.min(firstRow, maxFirstRow());
		updateButtons();
	}

	private int depth(TalentCatalogPayload.Entry node) {
		int depth = 0;
		while (depth < catalog.entries().size()) {
			node = catalog.entries().get(node.prerequisite());
			if (node == null) break;
			depth++;
		}
		return depth;
	}

	private TalentCatalogPayload.Entry selection() {
		return selected == null ? null : catalog.entries().get(selected);
	}

	private int maxFirstRow() {
		return Math.max(0, nodes.size() - rows.size());
	}

	private void scroll(int amount) {
		firstRow = Math.clamp(firstRow + amount, 0, maxFirstRow());
		updateButtons();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (vertical == 0 || maxFirstRow() == 0) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
		scroll(vertical > 0 ? -1 : 1);
		return true;
	}

	private void updateButtons() {
		for (int i = 0; i < rows.size(); i++) {
			ButtonWidget button = rows.get(i);
			int index = firstRow + i;
			button.visible = index < nodes.size();
			if (!button.visible) continue;
			var row = nodes.get(index);
			var node = row.getValue();
			String mark = row.getKey().equals(selected) ? "◆ " : "  ";
			Text name = Text.literal(mark).append(name(row.getKey())).append("  " + node.rank() + "/" + node.ranks());
			button.setMessage(UiStyle.fit(textRenderer, name, button.getWidth() - 8));
			button.setTooltip(Tooltip.of(name));
		}
		up.active = firstRow > 0;
		down.active = firstRow < maxFirstRow();
		var node = selection();
		learn.visible = node != null;
		learn.active = blocked(node, ClientRoleState.data()) == null;
		learn.setMessage(Text.translatable(node != null && node.rank() > 0
				? "screen.aptitudes.roles.upgrade" : "screen.aptitudes.paths.learn"));
	}

	private String blocked(TalentCatalogPayload.Entry node, RoleSyncPayload data) {
		if (node == null || data == null || client.player == null || client.player.isSpectator() || !client.player.isAlive()
				|| !ClientPlayNetworking.canSend(TalentPurchasePayload.ID)) return "datapack_unavailable";
		if (data.primaryRole() != role && data.secondaryRole() != role) return "role_not_active";
		if (node.rank() >= node.ranks()) return "max_rank";
		if (data.level(role) < node.levelForNextRank()) return "level_too_low";
		var custom = catalog.entries().get(node.prerequisite());
		int preRank = custom == null ? data.rank(node.prerequisite()) : custom.rank();
		if (preRank == 0) return "missing_prerequisite";
		return ClientRoleState.availablePoints(data, role) <= 0 ? "no_points" : null;
	}

	private void learn() {
		var node = selection();
		if (catalog == ClientRoleState.talents() && blocked(node, ClientRoleState.data()) == null) {
			ClientPlayNetworking.send(new TalentPurchasePayload(catalog.revision(), selected, node.rank()));
		}
	}

	@Override
	public void tick() {
		if (catalog != ClientRoleState.talents()) updateCatalog();
		else updateButtons();
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, panelWidth, panelHeight);
		UiStyle.header(ctx, textRenderer, Text.translatable(KEY + "title", Text.translatable(role.translationKey())),
				left + 8, top + 9, panelWidth - 16);
		UiStyle.panel(ctx, left + 8, top + 27, listWidth, panelHeight - 56);
		UiStyle.panel(ctx, detailX - 2, top + 27, detailWidth + 2, panelHeight - 56);
		renderDetails(ctx, selection(), ClientRoleState.data());
		super.render(ctx, mouseX, mouseY, delta);
	}

	private void renderDetails(DrawContext ctx, TalentCatalogPayload.Entry node, RoleSyncPayload data) {
		int y = top + 34;
		if (node == null || data == null) {
			line(ctx, Text.translatable(KEY + "empty"), y, UiStyle.MUTED);
			return;
		}
		y = line(ctx, name(selected), y, UiStyle.GOLD);
		int perRank = (int) Math.round(PathEffect.SPEED.perRank() * 100);
		y = line(ctx, Text.translatable(KEY + "speed", perRank, perRank * TalentProgressRules.MAX_SPEED_RANKS), y + 4, UiStyle.TEXT);
		y = line(ctx, Text.translatable("screen.aptitudes.tree.rank", node.rank(), node.ranks()), y + 6, UiStyle.TEXT);
		y = line(ctx, Text.translatable("screen.aptitudes.paths.points", ClientRoleState.availablePoints(data, role)), y, UiStyle.TEXT);
		if (node.rank() < node.ranks()) {
			y = line(ctx, Text.translatable("screen.aptitudes.roles.requires", node.levelForNextRank()), y,
					data.level(role) >= node.levelForNextRank() ? UiStyle.TEXT : UiStyle.DOWN);
			y = line(ctx, Text.translatable(KEY + "prerequisite", name(node.prerequisite())), y, UiStyle.TEXT);
		}
		String reason = blocked(node, data);
		if (reason != null) line(ctx, Text.translatable("aptitudes.role.upgrade." + reason), y + 5, UiStyle.MUTED);
	}

	private int line(DrawContext ctx, Text text, int y, int color) {
		for (var row : textRenderer.wrapLines(text, detailWidth - 12)) {
			if (y + textRenderer.fontHeight > top + panelHeight - 31) break;
			ctx.drawText(textRenderer, row, detailX + 6, y, color, false);
			y += 11;
		}
		return y;
	}

	private static Text name(String id) {
		var builtin = RoleTree.get(id);
		if (builtin != null) return Text.translatable(builtin.translationKey());
		return Text.translatableWithFallback("talent." + id.replace(':', '.') + ".name", id);
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
