package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.network.RoleActionPayload;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.role.NodeIcons;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.PathRules.Step;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
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
	private int path;
	@Nullable private Step selected = Step.S1;
	private int left;
	private int top;
	private int scroll;
	private ButtonWidget learnButton;
	private final ButtonWidget[] tabs = new ButtonWidget[PathRules.PATHS];

	public PathsScreen(Screen parent, Role role) {
		super(Text.translatable("screen.aptitudes.paths.title", Text.translatable(role.translationKey())));
		this.parent = parent;
		this.role = role;
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
			tabs[p - 1] = addDrawableChild(ButtonWidget.builder(Text.literal(String.valueOf((char) ('A' + p - 1))), b -> {
				path = target;
				selected = Step.S1;
				updateButtons();
			}).dimensions(left + W - 112 + (p - 1) * 36, top + 6, 32, 16).build());
		}
		learnButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.learn"),
				b -> learn()).dimensions(left + PANEL_X + 6, top + H - 50, W - PANEL_X - 12, 16).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.paths.back"),
				b -> close()).dimensions(left + W / 2 - 50, top + H - 24, 100, 18).build());
		updateButtons();
	}

	private void updateButtons() {
		RoleSyncPayload data = ClientRoleState.data();
		for (int p = 1; p <= PathRules.PATHS; p++) {
			tabs[p - 1].active = p != path;
		}
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
		if (data == null || node == null || selected == null) return;
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
		UiStyle.window(ctx, left, top, W, H); // aquí: si se dibujara en render() taparía los botones
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		RoleSyncPayload data = ClientRoleState.data();
		int color = PATH_COLORS[path - 1];
		// Cabecera: rol · senda, frase y nivel.
		Text pathName = Text.translatable(RoleTree.pathKey(role, path));
		ctx.drawItem(role.iconStack(), left + 8, top + 6);
		UiStyle.label(ctx, textRenderer, Text.translatable(role.translationKey()).append(" · ").append(pathName),
				left + 28, top + 6, W - 150, UiStyle.GOLD);
		UiStyle.label(ctx, textRenderer, Text.translatable(RoleTree.pathKey(role, path) + ".desc"),
				left + 28, top + 17, W - 150, UiStyle.MUTED);
		if (data == null) return;
		int level = data.level(role);
		int max = data.rules().maxLevel();
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.paths.level", level, max), left + 8, top + 30, UiStyle.TEXT, false);
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.paths.points", Math.max(0, availablePoints(data))),
				left + 120, top + 30, UiStyle.TEXT, false);
		Text status = statusLine(data);
		UiStyle.label(ctx, textRenderer, status, left + PANEL_X, top + 30, W - PANEL_X - 8,
				PathRules.pathLocked(data::rank, role.key(), path) ? UiStyle.DOWN : UiStyle.GOLD);
		ctx.fill(left + 6, top + 41, left + W - 6, top + 42, UiStyle.GOLD_DARK);

		// Raíl de niveles.
		int[] railLevels = {10, 14, 20, 26, 32, 40, 50};
		for (int row = 0; row < railLevels.length; row++) {
			int y = rowY(row) + 7;
			int c = level >= railLevels[row] ? UiStyle.GOLD : UiStyle.MUTED;
			ctx.drawText(textRenderer, "Nv." + railLevels[row], left + 8, y, c, false);
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
			boolean locked = RoleTree.nodeLocked(data::rank, node);
			boolean hover = mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20;
			if (hover) hovered = node;
			boolean isSelected = step == selected;
			boolean keystone = step == Step.S7; // v46.1: piedra angular, marco morado
			ctx.fill(x, y, x + 20, y + 20, rank > 0 ? 0xF0262A3A : 0xF0151722);
			ctx.drawBorder(x, y, 20, 20, isSelected ? 0xFFFFFFFF : rank > 0 ? color : locked ? 0xFF5A2A2A
					: keystone ? 0xFF7E57C2 : 0xFF50546A);
			ctx.drawItem(NodeIcons.of(node), x + 2, y + 2);
			if (locked) {
				ctx.fill(x + 1, y + 1, x + 19, y + 19, 0x90401010);
				lockIcon(ctx, x + 6, y + 5);
			}
			int textX = x + 24;
			int maxW = (step == Step.S5X ? BRANCH_X2 - NODE_X - 26 : PANEL_X - (x - left) - 30);
			UiStyle.label(ctx, textRenderer, Text.translatable(node.translationKey()), textX, y + 2, maxW,
					locked ? UiStyle.MUTED : UiStyle.TEXT);
			ctx.drawText(textRenderer, rank + "/" + node.maxRank(), textX, y + 12, rank > 0 ? color : UiStyle.MUTED, false);
		}
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.paths.branch"), left + BRANCH_X2 + 24,
				rowY(4) + 12, UiStyle.MUTED, false);

		renderPanel(ctx, data);
		if (hovered != null) {
			ctx.drawTooltip(textRenderer, List.of(Text.translatable(hovered.translationKey()).formatted(Formatting.GOLD),
					Text.translatable(hovered.descriptionKey()).formatted(Formatting.GRAY)), mouseX, mouseY);
		}
	}

	/** Candado de 8×10 px dibujado con rectángulos (el mismo que en el árbol). */
	private static void lockIcon(DrawContext ctx, int x, int y) {
		int c = 0xFFB0B4C0;
		ctx.fill(x + 2, y, x + 6, y + 1, c);
		ctx.fill(x + 1, y + 1, x + 2, y + 5, c);
		ctx.fill(x + 6, y + 1, x + 7, y + 5, c);
		ctx.fill(x, y + 5, x + 8, y + 11, c);
		ctx.fill(x + 3, y + 7, x + 5, y + 9, 0xFF303440);
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

	/** Panel derecho: detalle del escalón elegido (también la piedra angular). */
	private void renderPanel(DrawContext ctx, RoleSyncPayload data) {
		int x = left + PANEL_X, y = top + ROWS_Y, w = W - PANEL_X - 8, h = H - ROWS_Y - 56;
		UiStyle.panel(ctx, x, y, w, h);
		List<Text> lines = new ArrayList<>();
		{
			RoleNode node = selectedNode();
			if (node == null) return;
			int rank = data.rank(node.id());
			if (selected == Step.S7) {
				lines.add(Text.translatable("screen.aptitudes.paths.keystone").formatted(Formatting.LIGHT_PURPLE));
			}
			lines.add(Text.translatable(node.translationKey()).formatted(Formatting.GOLD));
			lines.add(Text.translatable(node.descriptionKey()).formatted(Formatting.GRAY));
			int next = Math.min(node.maxRank(), rank + 1);
			if (rank < node.maxRank()) {
				lines.add(Text.translatable("screen.aptitudes.roles.requires", node.levelForRank(next))
						.formatted(data.level(role) >= node.levelForRank(next) ? Formatting.GREEN : Formatting.RED));
			}
		}
		int ty = y + 4;
		for (Text line : lines) {
			for (OrderedText part : textRenderer.wrapLines(line, w - 8)) {
				if (ty > y + h - 10) return;
				ctx.drawText(textRenderer, part, x + 4, ty, 0xFFFFFFFF, false);
				ty += 10;
			}
			ty += 2;
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
