package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.network.RoleActionPayload;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.PathRules.Step;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista de sendas (v46): una senda cada vez (pestañas A/B/C), con sus 7 escalones en vertical, el raíl de niveles a la
 * izquierda, la bifurcación del nivel 32 en dos filas y el detalle del escalón elegido a la derecha. Diseño en
 * {@code docs/V46_DISENO_SENDAS.md} §5. Solo muestra datos sincronizados: el servidor valida al aprender.
 *
 * <p>Mide hasta 426×232 y se desplaza con la rueda si la ventana es más baja.</p>
 */
public class PathsScreen extends Screen {
	private static final int MAX_WIDTH = 426;
	private static final int H = 232;
	private static final int ROW_H = 19;
	private static final int ROWS_Y = 49;
	private static final int ROWS = 8;
	private static final int RAIL_X = 35;
	private static final int NODE_X = 46;
	private static final int BRANCH_INDENT = 12;
	private static final int GAP = 8;
	private static final int FOOTER_Y = 207;
	/** Colores de las sendas A, B y C (los mismos que en el árbol). */
	private static final int[] PATH_COLORS = {0xFFE57373, 0xFF64B5F6, 0xFF81C784};

	private final Screen parent;
	private final Role role;
	private final RoleAtlasTheme theme;
	private int path;
	@Nullable private Step selected = Step.S1;
	private int windowWidth;
	private int panelX;
	private int left;
	private int top;
	private int scroll;
	private ButtonWidget learnButton;

	public PathsScreen(Screen parent, Role role) {
		super(Text.translatable("screen.aptitudes.paths.title", Text.translatable(role.translationKey())));
		this.parent = parent;
		this.role = role;
		this.theme = RoleAtlasTheme.of(role);
		RoleSyncPayload data = ClientRoleState.data();
		int chosen = data == null ? 0 : PathRules.chosenPath(data::rank, role.key());
		this.path = chosen > 0 ? chosen : 1;
	}

	private int maxScroll() {
		return Math.max(0, H + 8 - height);
	}

	@Override
	protected void init() {
		scroll = MathHelper.clamp(scroll, 0, maxScroll());
		windowWidth = Math.min(MAX_WIDTH, width - 8);
		panelX = windowWidth * 59 / 100;
		left = (width - windowWidth) / 2;
		top = maxScroll() > 0 ? 4 - scroll : (height - H) / 2;
		for (int p = 1; p <= PathRules.PATHS; p++) {
			final int target = p;
			Text letter = Text.literal(String.valueOf((char) ('A' + p - 1)));
			var button = new RoleAtlasTheme.Tab(left + windowWidth - 112 + (p - 1) * 36, top + 6, 32, letter, b -> {
				if (path == target) return;
				path = target;
				selected = Step.S1;
				updateButtons();
			}, () -> path == target, () -> PATH_COLORS[target - 1]);
			button.setTooltip(Tooltip.of(Text.translatable(RoleTree.pathKey(role, target))));
			addDrawableChild(button);
		}
		learnButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.learn"),
				b -> learn()).dimensions(left + panelX, top + FOOTER_Y, windowWidth - panelX - GAP, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.back"),
				b -> close()).dimensions(left + GAP, top + FOOTER_Y, 100, 18).build());
		updateButtons();
	}

	private void updateButtons() {
		RoleSyncPayload data = ClientRoleState.data();
		RoleNode node = selectedNode();
		learnButton.visible = node != null;
		learnButton.active = data != null && node != null && canLearn(data, node);
	}

	@Nullable
	private RoleNode node(Step step) {
		return RoleTree.get(PathRules.nodeId(role.key(), path, step));
	}

	@Nullable
	private RoleNode selectedNode() {
		return selected == null ? null : node(selected);
	}

	/** Misma comprobación que el servidor, con los datos sincronizados (solo para activar el botón). */
	private boolean canLearn(RoleSyncPayload data, RoleNode node) {
		boolean active = role == data.primaryRole() || role == data.secondaryRole();
		int rank = data.rank(node.id());
		return active && rank < node.maxRank()
				&& data.level(role) >= node.levelForRank(rank + 1)
				&& RoleTree.prerequisiteMet(data::rank, node)
				&& !RoleTree.nodeLocked(data::rank, node)
				&& availablePoints(data) > 0;
	}

	private int availablePoints(RoleSyncPayload data) {
		return ClientRoleState.availablePoints(data, role);
	}

	private void learn() {
		RoleSyncPayload data = ClientRoleState.data();
		RoleNode node = selectedNode();
		if (data != null && node != null && canLearn(data, node)) playUi(dev.aptitudes.sound.ModSounds.UI_METAL_UNLOCK);
		if (data == null || node == null || selected == null || !canLearn(data, node)) return;
		// Confirmación: elegir senda (la primera vez en este rol) o elegir rama en la bifurcación es permanente.
		Text name = Text.translatable(node.translationKey());
		Text message = null;
		if (PathRules.pathsStarted(data::rank, role.key()) == 0) {
			message = Text.translatable("screen.aptitudes.paths.confirm_path", name, Text.translatable(RoleTree.pathKey(role, path)));
		} else if (selected.branch() && PathRules.chosenBranch(data::rank, role.key(), path) == null) {
			message = Text.translatable("screen.aptitudes.paths.confirm_branch", name);
		}
		if (message == null) {
			send(node);
			return;
		}
		client.setScreen(new ConfirmScreen(ok -> {
			if (ok) send(node);
			client.setScreen(this);
		}, Text.translatable("screen.aptitudes.paths.confirm_title"), message));
	}

	private static void playUi(net.minecraft.sound.SoundEvent sound) {
		net.minecraft.client.MinecraftClient.getInstance().getSoundManager()
				.play(net.minecraft.client.sound.PositionedSoundInstance.master(sound, 1.0f));
	}

	private void send(RoleNode node) {
		ClientPlayNetworking.send(new RoleActionPayload(RoleActionPayload.UPGRADE_NODE, role.ordinal(), node.id()));
	}

	@Override
	public void tick() {
		updateButtons(); // los rangos llegan del servidor tras aprender
	}

	// ------------------------------------------------------------ Dibujo

	private int rowY(int row) {
		return top + ROWS_Y + row * ROW_H;
	}

	/** Cada opción de la bifurcación ocupa una fila con su propio nombre y rango. */
	private static int rowOf(Step step) {
		return switch (step) {
			case S1 -> 0;
			case S2 -> 1;
			case S3 -> 2;
			case S4 -> 3;
			case S5X -> 4;
			case S5Y -> 5;
			case S6 -> 6;
			case S7 -> 7;
		};
	}

	private int nodeX(Step step) {
		return left + NODE_X + (step.branch() ? BRANCH_INDENT : 0);
	}

	private boolean overStep(double mouseX, double mouseY, Step step) {
		int y = rowY(rowOf(step));
		return mouseX >= nodeX(step) && mouseX < left + panelX - GAP - 3
				&& mouseY >= y && mouseY < y + ROW_H - 1;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, windowWidth, H);
		theme.panel(ctx, left + GAP, top + 4, panelX - 2 * GAP, 24);
		theme.panel(ctx, left + GAP, rowY(0) - 2, panelX - 2 * GAP, ROWS * ROW_H + 3);
		theme.panel(ctx, left + panelX, rowY(0) - 2, windowWidth - panelX - GAP, ROWS * ROW_H + 3);
		if (windowWidth >= 390) NodeIconRenderer.drawCrest(ctx, role, left + panelX + 22, top + 7, 16);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		RoleSyncPayload data = ClientRoleState.data();
		int color = PATH_COLORS[path - 1];
		Text pathName = Text.translatable(RoleTree.pathKey(role, path));
		NodeIconRenderer.drawRole(ctx, role, NodeIconRenderer.Badge.CORE, left + 11, top + 7);
		UiStyle.label(ctx, textRenderer, Text.translatable(role.translationKey()).append(" · ").append(pathName),
				left + 31, top + 7, panelX - 44, theme.accent());
		UiStyle.label(ctx, textRenderer, Text.translatable(RoleTree.pathKey(role, path) + ".desc"),
				left + 31, top + 17, panelX - 44, UiStyle.MUTED);
		if (data == null) return;
		int level = data.level(role);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.level", level, data.rules().maxLevel()),
				left + 9, top + 34, panelX / 2 - 14, UiStyle.TEXT);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.points", Math.max(0, availablePoints(data))),
				left + panelX / 2, top + 34, panelX / 2 - GAP, UiStyle.TEXT);
		Text status = statusLine(data);
		int statusColor = PathRules.pathLocked(data::rank, role.key(), path) ? UiStyle.DOWN : UiStyle.GOLD;
		int sy = top + 28;
		for (OrderedText line : textRenderer.wrapLines(status, windowWidth - panelX - GAP)) {
			if (sy > top + 37) break;
			ctx.drawText(textRenderer, line, left + panelX, sy, statusColor, false);
			sy += 9;
		}

		int railX = left + RAIL_X;
		ctx.fill(railX, rowY(0) + 8, railX + 1, rowY(ROWS - 1) + 9, UiStyle.BORDER);
		int branchRail = left + NODE_X;
		int branchTop = rowY(rowOf(Step.S5X)) + 8;
		int branchBottom = rowY(rowOf(Step.S5Y)) + 8;
		int junction = (branchTop + branchBottom) / 2;
		ctx.fill(railX, junction, branchRail, junction + 1, UiStyle.BORDER);
		ctx.fill(branchRail, branchTop, branchRail + 1, branchBottom + 1, UiStyle.BORDER);
		RoleNode hovered = null;
		for (Step step : Step.values()) {
			RoleNode node = node(step);
			if (node == null) continue;
			int y = rowY(rowOf(step));
			int x = nodeX(step);
			int rank = data.rank(node.id());
			boolean excluded = RoleTree.nodeLocked(data::rank, node);
			boolean available = rank == 0 && canLearn(data, node);
			if (step != Step.S5Y) {
				int required = node.requiredLevel();
				int ly = step == Step.S5X ? y + ROW_H / 2 + 4 : y + 5;
				ctx.drawText(textRenderer, Integer.toString(required), left + 15, ly,
						level >= required ? UiStyle.GOLD : UiStyle.MUTED, false);
			}

			int end = left + panelX - GAP - 3;
			int rankColor = rank > 0 ? color : UiStyle.MUTED;
			ctx.fill(step.branch() ? branchRail : railX, y + 8, x, y + 9, rank > 0 || available ? color : UiStyle.BORDER);
			if (!excluded && overStep(mouseX, mouseY, step)) ctx.fill(x - 2, y - 2, end + 2, y + ROW_H + 1, 0x30FFD56A);
			UiStyle.row(ctx, x, y, end - x, ROW_H - 1, rowOf(step), step == selected);
			int frame = step == Step.S7 ? 0xFF7E57C2 : UiStyle.BORDER;
			ctx.drawBorder(x + 1, y + 1, 16, 16, rank > 0 || available ? color : frame);
			if (available) {
				int pulse = (int) (16 + 14 * (0.5 + 0.5 * Math.sin(net.minecraft.util.Util.getMeasuringTimeMs() / 450.0)));
				ctx.drawBorder(x, y, 18, 18, (pulse << 24) | 0xF0C060);
			}
			NodeIconRenderer.draw(ctx, node, x + 1, y + 1);
			if (rank == 0 && !available) {
				ctx.fill(x + 1, y + 1, x + 17, y + 17, excluded ? 0x90401010 : 0x60101018);
				UiStyle.lockIcon(ctx, x + 1, y);
			} else if (available) {
				ctx.fill(x + 1, y + 1, x + 5, y + 5, color);
			}
			String rankText = rank + "/" + node.maxRank();
			int rankX = end - 6 - textRenderer.getWidth(rankText);
			int nameWidth = rankX - x - 28;
			Text name = Text.translatable(node.translationKey());
			if (overStep(mouseX, mouseY, step) && (mouseX < x + 18
					|| (mouseX >= x + 22 && mouseX < x + 22 + nameWidth && textRenderer.getWidth(name) > nameWidth))) {
				hovered = node;
			}
			UiStyle.label(ctx, textRenderer, name, x + 22, y + 5, nameWidth, excluded ? UiStyle.MUTED : UiStyle.TEXT);
			ctx.drawText(textRenderer, rankText, rankX, y + 5, rankColor, false);
		}
		renderPanel(ctx, data, mouseX, mouseY, hovered);
	}

	private Text statusLine(RoleSyncPayload data) {
		int chosen = PathRules.chosenPath(data::rank, role.key());
		if (chosen == 0) return Text.translatable("screen.aptitudes.paths.free");
		if (chosen == path) return Text.translatable("screen.aptitudes.paths.chosen");
		return Text.translatable("screen.aptitudes.paths.locked", Text.translatable(RoleTree.pathKey(role, chosen)));
	}

	private List<Text> stepDetails(RoleSyncPayload data, RoleNode node) {
		int rank = data.rank(node.id());
		List<Text> lines = new ArrayList<>();
		lines.add(Text.translatable(node.translationKey()).formatted(Formatting.GOLD));
		if (RoleTree.keystone(node.id()) != null) {
			lines.add(Text.translatable("screen.aptitudes.paths.keystone").formatted(Formatting.LIGHT_PURPLE));
		}
		lines.add(Text.translatable("screen.aptitudes.tree.rank", rank, node.maxRank()));
		RoleTree.PathSpec pathSpec = RoleTree.pathSpec(node.id());
		if (pathSpec != null && pathSpec.step().branch()) {
			lines.add(Text.translatable("screen.aptitudes.paths.branch").formatted(Formatting.YELLOW));
		}
		if (rank < node.maxRank()) {
			RoleNode pre = node.prerequisite() == null ? null : RoleTree.get(node.prerequisite());
			if (pre != null) {
				Text requirement = Text.translatable(pre.translationKey());
				if (pathSpec != null && pathSpec.step() == Step.S6) {
					RoleNode alternative = node(Step.S5Y);
					if (alternative != null) {
						requirement = Text.translatable("screen.aptitudes.tree.either", requirement,
								Text.translatable(alternative.translationKey()));
					}
				}
				lines.add(Text.translatable("screen.aptitudes.node.needs", requirement)
						.formatted(RoleTree.prerequisiteMet(data::rank, node) ? Formatting.GREEN : Formatting.RED));
			}
			int level = node.levelForRank(rank + 1);
			lines.add(Text.translatable("screen.aptitudes.roles.requires", level)
					.formatted(data.level(role) >= level ? Formatting.GREEN : Formatting.RED));
		}
		lines.add(Text.translatable(node.descriptionKey()));
		return lines;
	}

	/** La ficha mantiene los requisitos a la vista y el cursor muestra la descripción completa. */
	private void renderPanel(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY, @Nullable RoleNode hovered) {
		int x = left + panelX + 7, y = top + ROWS_Y + 5;
		int w = windowWidth - panelX - GAP - 14;
		int bottom = rowY(ROWS) - 3;
		RoleNode node = selectedNode();
		if (node == null) return;
		List<Text> lines = stepDetails(data, node);
		int ty = y;
		for (int i = 0; i < lines.size(); i++) {
			if (i == lines.size() - 1) ty += 4;
			for (OrderedText part : textRenderer.wrapLines(lines.get(i), w)) {
				if (ty + 8 > bottom) break;
				ctx.drawText(textRenderer, part, x, ty, UiStyle.TEXT, false);
				ty += 10;
			}
			ty += 2;
		}
		if (hovered != null) {
			List<OrderedText> tooltip = new ArrayList<>();
			for (Text line : hovered == node ? lines : stepDetails(data, hovered)) {
				tooltip.addAll(textRenderer.wrapLines(line, Math.min(248, width - 20)));
			}
			ctx.drawOrderedTooltip(textRenderer, tooltip, mouseX, mouseY);
		}
	}

	// ------------------------------------------------------------ Entrada

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		for (Step step : Step.values()) {
			if (node(step) != null && overStep(mouseX, mouseY, step)) {
				RoleSyncPayload clickData = ClientRoleState.data();
				RoleNode clicked = node(step);
				boolean open = clickData != null && (clickData.rank(clicked.id()) > 0 || canLearn(clickData, clicked));
				playUi(open ? dev.aptitudes.sound.ModSounds.UI_METAL_SELECT : dev.aptitudes.sound.ModSounds.UI_METAL_DENIED);
				selected = step;
				updateButtons();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll() <= 0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		int target = MathHelper.clamp(scroll - (int) (verticalAmount * 16), 0, maxScroll());
		if (target != scroll) {
			scroll = target;
			clearAndInit();
		}
		return true;
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
