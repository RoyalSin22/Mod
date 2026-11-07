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
 * izquierda, la bifurcación del nivel 32 lado a lado y el detalle del escalón elegido a la derecha. Diseño en
 * {@code docs/V46_DISENO_SENDAS.md} §5. Solo muestra datos sincronizados: el servidor valida al aprender.
 *
 * <p>Mide 426×232 y se desplaza con la rueda si la ventana es más baja.</p>
 */
public class PathsScreen extends Screen {
	private static final int W = 426;
	private static final int H = 232;
	private static final int ROW_H = 24;
	private static final int ROWS_Y = 44;
	private static final int NODE_X = 46;
	private static final int BRANCH_X2 = 150;
	private static final int PANEL_X = 268;
	/** Colores de las sendas A, B y C (los mismos que en el árbol). */
	private static final int[] PATH_COLORS = {0xFFE57373, 0xFF64B5F6, 0xFF81C784};

	private final Screen parent;
	private final Role role;
	private final RoleAtlasTheme theme;
	private int path;
	@Nullable private Step selected = Step.S1;
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
		return Math.max(0, H + 16 - height);
	}

	@Override
	protected void init() {
		scroll = MathHelper.clamp(scroll, 0, maxScroll());
		left = (width - W) / 2;
		top = maxScroll() > 0 ? 8 - scroll : (height - H) / 2;
		for (int p = 1; p <= PathRules.PATHS; p++) {
			final int target = p;
			Text letter = Text.literal(String.valueOf((char) ('A' + p - 1)));
			var button = new RoleAtlasTheme.Tab(left + W - 112 + (p - 1) * 36, top + 6, 32, letter, b -> {
				if (path == target) return;
				path = target;
				selected = Step.S1;
				updateButtons();
			}, () -> path == target, () -> PATH_COLORS[target - 1]);
			button.setTooltip(Tooltip.of(Text.translatable(RoleTree.pathKey(role, target))));
			addDrawableChild(button);
		}
		learnButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.learn"),
				b -> learn()).dimensions(left + PANEL_X + 6, top + H - 50, W - PANEL_X - 12, 16).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.back"),
				b -> close()).dimensions(left + W / 2 - 50, top + H - 24, 100, 18).build());
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
		int spent = 0;
		for (RoleNode n : RoleTree.nodes(role)) spent += data.rank(n.id());
		return data.rules().earnedPoints(data.level(role), data.tier(role)) - spent;
	}

	private void learn() {
		RoleSyncPayload data = ClientRoleState.data();
		RoleNode node = selectedNode();
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

	/** Fila de cada escalón: S5x y S5y comparten la fila 4; S6 es la 5; la piedra angular, la 6. */
	private static int rowOf(Step step) {
		return switch (step) {
			case S1 -> 0;
			case S2 -> 1;
			case S3 -> 2;
			case S4 -> 3;
			case S5X, S5Y -> 4;
			case S6 -> 5;
			case S7 -> 6;
		};
	}

	private int nodeX(Step step) {
		return left + (step == Step.S5Y ? BRANCH_X2 : NODE_X);
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, W, H);
		theme.panel(ctx, left + 5, top + 4, PANEL_X - 9, 24);
		theme.panel(ctx, left + 5, rowY(0) - 2, PANEL_X - 9, rowY(6) + 24 - rowY(0));
		NodeIconRenderer.drawCrest(ctx, role, left + 151, top + 83, 80);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		RoleSyncPayload data = ClientRoleState.data();
		int color = PATH_COLORS[path - 1];
		// Cabecera: rol · senda, frase y nivel.
		Text pathName = Text.translatable(RoleTree.pathKey(role, path));
		NodeIconRenderer.drawRole(ctx, role, NodeIconRenderer.Badge.CORE, left + 8, top + 6);
		UiStyle.label(ctx, textRenderer, Text.translatable(role.translationKey()).append(" · ").append(pathName),
				left + 28, top + 6, PANEL_X - 36, theme.accent());
		UiStyle.label(ctx, textRenderer, Text.translatable(RoleTree.pathKey(role, path) + ".desc"),
				left + 28, top + 17, PANEL_X - 36, UiStyle.MUTED);
		if (data == null) return;
		int level = data.level(role);
		int max = data.rules().maxLevel();
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.level", level, max),
				left + 8, top + 30, 106, UiStyle.TEXT);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.points", Math.max(0, availablePoints(data))),
				left + 120, top + 30, PANEL_X - 126, UiStyle.TEXT);
		Text status = statusLine(data);
		UiStyle.label(ctx, textRenderer, status, left + PANEL_X, top + 30, W - PANEL_X - 8,
				PathRules.pathLocked(data::rank, role.key(), path) ? UiStyle.DOWN : UiStyle.GOLD);
		ctx.fill(left + 6, top + 41, left + W - 6, top + 42, theme.accent());

		// Raíl de niveles.
		int[] railLevels = {10, 14, 20, 26, 32, 40, 50};
		for (int row = 0; row < railLevels.length; row++) {
			int y = rowY(row) + 7;
			int c = level >= railLevels[row] ? UiStyle.GOLD : UiStyle.MUTED;
			ctx.drawText(textRenderer, Integer.toString(railLevels[row]), left + 15, y, c, false);
		}
		// Líneas entre escalones (en color si el de arriba está aprendido).
		for (Step step : Step.values()) {
			if (step == Step.S1) continue;
			for (Step from : parents(step)) {
				boolean lit = data.rank(PathRules.nodeId(role.key(), path, from)) > 0;
				int x1 = nodeX(from) + 9, y1 = rowY(rowOf(from)) + 18;
				int x2 = nodeX(step) + 9, y2 = rowY(rowOf(step)) + 2;
				UiStyle.line(ctx, x1, y1, x2, y2, lit ? color : 0xFF3A3F55);
			}
		}

		// Escalones.
		RoleNode hovered = null;
		for (Step step : Step.values()) {
			RoleNode node = node(step);
			if (node == null) continue;
			int x = nodeX(step), y = rowY(rowOf(step));
			int rank = data.rank(node.id());
			boolean excluded = RoleTree.nodeLocked(data::rank, node);
			boolean available = rank == 0 && canLearn(data, node);
			boolean hover = mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20;
			if (hover) hovered = node;
			boolean isSelected = step == selected;
			boolean keystone = step == Step.S7; // v46.1: piedra angular, marco morado
			ctx.fill(x, y, x + 20, y + 20, rank > 0 ? 0xF0262A3A : 0xF0151722);
			ctx.drawBorder(x, y, 20, 20, isSelected ? 0xFFFFFFFF : rank > 0 || available ? color : excluded ? 0xFF5A2A2A
					: keystone ? 0xFF7E57C2 : 0xFF50546A);
			NodeIconRenderer.draw(ctx, node, x + 2, y + 2);
			if (rank == 0 && !available) {
				ctx.fill(x + 1, y + 1, x + 19, y + 19, excluded ? 0x90401010 : 0x60101018);
				UiStyle.lockIcon(ctx, x + 1, y);
			} else if (available) {
				ctx.fill(x, y, x + 4, y + 4, color);
			}
			int textX = x + 24;
			int maxW = (step == Step.S5X ? BRANCH_X2 - NODE_X - 26 : PANEL_X - (x - left) - 30);
			UiStyle.label(ctx, textRenderer, Text.translatable(node.translationKey()), textX, y + 2, maxW,
					excluded ? UiStyle.MUTED : UiStyle.TEXT);
			ctx.drawText(textRenderer, rank + "/" + node.maxRank(), textX, y + 12, rank > 0 ? color : UiStyle.MUTED, false);
		}
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.branch"), left + BRANCH_X2 + 24,
				rowY(4) + 12, PANEL_X - BRANCH_X2 - 30, UiStyle.MUTED);

		renderPanel(ctx, data, mouseX, mouseY, hovered);
	}

	/** Escalones de los que sale la línea hacia {@code step}. */
	private static List<Step> parents(Step step) {
		return switch (step) {
			case S2 -> List.of(Step.S1);
			case S3 -> List.of(Step.S2);
			case S4 -> List.of(Step.S3);
			case S5X, S5Y -> List.of(Step.S4);
			case S6 -> List.of(Step.S5X, Step.S5Y);
			case S7 -> List.of(Step.S6);
			default -> List.of();
		};
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
		if (rank < node.maxRank()) {
			RoleNode pre = node.prerequisite() == null ? null : RoleTree.get(node.prerequisite());
			if (pre != null) {
				Text requirement = Text.translatable(pre.translationKey());
				RoleTree.PathSpec spec = RoleTree.pathSpec(node.id());
				if (spec != null && spec.step() == Step.S6) {
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
		int x = left + PANEL_X, y = top + ROWS_Y, w = W - PANEL_X - 8, h = H - ROWS_Y - 56;
		theme.panel(ctx, x, y, w, h);
		RoleNode node = selectedNode();
		if (node == null) return;
		List<Text> lines = stepDetails(data, node);
		int ty = y + 6;
		for (int i = 0; i < lines.size() - 1; i++) {
			UiStyle.label(ctx, textRenderer, lines.get(i), x + 5, ty, w - 10, UiStyle.TEXT);
			ty += 12;
		}
		ty += 3;
		for (OrderedText part : textRenderer.wrapLines(lines.getLast(), w - 10)) {
			if (ty > y + h - 10) break;
			ctx.drawText(textRenderer, part, x + 5, ty, UiStyle.TEXT, false);
			ty += 10;
		}
		if (hovered != null || (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h)) {
			List<OrderedText> tooltip = new ArrayList<>();
			for (Text line : hovered == null || hovered == node ? lines : stepDetails(data, hovered)) {
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
			int x = nodeX(step), y = rowY(rowOf(step));
			if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
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
