package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientExtraState;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.morph.MorphForm;
import dev.aptitudes.network.FormSyncPayload;
import dev.aptitudes.network.RoleActionPayload;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleCategory;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleRules;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de roles (tecla J):
 * <ul>
 *   <li>Izquierda: pestañas de categoría, roles de esa categoría y la ficha del rol
 *       (nivel, puntos, principal/secundario, evolución).</li>
 *   <li>Derecha: el árbol como grafo. Centro = rol; izquierda = pasivas; derecha =
 *       activas; arriba = característica (Maestro) y definitiva (Definitivo).
 *       Clic en un nodo → ficha con descripción, requisitos y botón Mejorar.</li>
 *   <li>Elementalista y Cambiaformas tienen una vista extra (elementos / formas).</li>
 * </ul>
 * Como el resto de pantallas, solo envía peticiones: el servidor valida todo.
 */
public class RolesScreen extends Screen {
	private static final int W = 440;
	private static final int H = 322;
	private static final int LIST_W = 124;
	private static final int ROLE_ROW_H = 18;

	private enum NodeKind { CORE, PASSIVE_A, ACTIVE_A, PASSIVE_B, ACTIVE_B, STAT_1, STAT_2, HASTE, EFFICIENCY, MASTERY,
		PATH_A1, PATH_A2, PATH_A3, PATH_B1, PATH_B2, PATH_B3, SIGNATURE, ULTIMATE }

	private record NodeView(NodeKind kind, @Nullable RoleNode node, int x, int y) {}

	private int left;
	private int top;
	private RoleCategory category = RoleCategory.MARTIAL;
	private Role selected = Role.BERSERKER;
	private NodeKind selectedNode = NodeKind.CORE;
	private boolean extraView;

	private ButtonWidget primaryButton;
	private ButtonWidget secondaryButton;
	private ButtonWidget evolveButton;
	private ButtonWidget upgradeButton;
	private ButtonWidget extraButton;

	public RolesScreen() {
		super(Text.translatable("screen.aptitudes.roles.title"));
		RoleSyncPayload data = ClientRoleState.data();
		if (data != null && data.primaryRole() != null) {
			selected = data.primaryRole();
			category = selected.category();
		}
	}

	private int canvasX() { return left + LIST_W + 16; }
	private int canvasW() { return W - LIST_W - 24; }
	private int canvasCenterX() { return canvasX() + canvasW() / 2 - 12; }
	private int coreY() { return top + 100; }

	@Override
	protected void init() {
		left = (width - W) / 2;
		top = (height - H) / 2;
		int lx = left + 8;
		primaryButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.set_primary"),
				b -> send(RoleActionPayload.SET_PRIMARY, "")).dimensions(lx, top + 182, LIST_W, 16).build());
		secondaryButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.set_secondary"),
				b -> send(RoleActionPayload.SET_SECONDARY, "")).dimensions(lx, top + 200, LIST_W, 16).build());
		evolveButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.evolve"),
				b -> send(RoleActionPayload.EVOLVE, "")).dimensions(lx, top + 218, LIST_W, 16).build());
		upgradeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.upgrade"), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.UPGRADE_NODE, v.node().id());
		}).dimensions(canvasX() + canvasW() - 76, top + H - 70, 70, 16).build());
		extraButton = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> extraView = !extraView)
				.dimensions(canvasX() + canvasW() - 76, top + 30, 70, 14).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.tab.attributes"),
				b -> client.setScreen(new AptitudesScreen())).dimensions(width / 2 - 154, top + H - 26, 100, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.tab.uniques"),
				b -> client.setScreen(new UniquesScreen())).dimensions(width / 2 - 50, top + H - 26, 100, 18).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(width / 2 + 54, top + H - 26, 100, 18).build());
		updateButtons();
	}

	private void send(int action, String nodeId) {
		ClientPlayNetworking.send(new RoleActionPayload(action, selected.ordinal(), nodeId));
	}

	private boolean hasExtraView() {
		return selected == Role.ELEMENTALIST || selected == Role.SHAPESHIFTER;
	}

	// ------------------------------------------------------- Estado / botones

	private void updateButtons() {
		RoleSyncPayload data = ClientRoleState.data();
		boolean ready = data != null;
		primaryButton.active = ready && data.primaryRole() != selected;
		secondaryButton.active = ready && data.secondaryRole() != selected;
		updateEvolveButton(data);

		NodeView v = nodeView(selectedNode);
		upgradeButton.visible = !extraView && v != null && v.node() != null;
		upgradeButton.active = ready && v != null && v.node() != null && canUpgrade(data, v.node());

		extraButton.visible = hasExtraView();
		extraButton.setMessage(Text.translatable(extraView ? "screen.aptitudes.roles.tree"
				: selected == Role.ELEMENTALIST ? "screen.aptitudes.roles.elements" : "screen.aptitudes.roles.forms"));
		if (!hasExtraView()) extraView = false;
	}

	private void updateEvolveButton(@Nullable RoleSyncPayload data) {
		if (data == null) {
			evolveButton.active = false;
			return;
		}
		int tier = data.tier(selected);
		boolean active = selected == data.primaryRole() || selected == data.secondaryRole();
		if (tier >= Evolution.ULTIMATE) {
			evolveButton.active = false;
			evolveButton.setTooltip(Tooltip.of(Text.translatable("screen.aptitudes.evolve.maxed")));
			return;
		}
		boolean toMaster = tier == Evolution.BASE;
		int needLevel = toMaster ? Evolution.MASTER_LEVEL : Evolution.ULTIMATE_LEVEL;
		boolean levelOk = data.level(selected) >= needLevel;
		Evolution.Trial trial = Evolution.trial(selected);
		boolean challengeOk = toMaster ? data.trial(selected) >= trial.target() : data.bossSlain(selected);
		Text challenge = toMaster
				? Text.translatable(trial.translationKey(), Math.min(data.trial(selected), trial.target()), trial.target())
				: Text.translatable("screen.aptitudes.evolve.boss");
		Text item = Text.translatable(toMaster ? "item.aptitudes.mastery_emblem" : "item.aptitudes.ultimate_core");
		evolveButton.active = active && levelOk && challengeOk;
		Text tooltip = Text.translatable("screen.aptitudes.evolve.to",
						Text.translatable(Evolution.titleKey(selected, tier + 1))).formatted(Formatting.GOLD)
				.append("\n").append(check(active, Text.translatable("screen.aptitudes.evolve.active")))
				.append("\n").append(check(levelOk, Text.translatable("screen.aptitudes.roles.requires", needLevel)))
				.append("\n").append(check(challengeOk, challenge))
				.append("\n").append(Text.translatable("screen.aptitudes.evolve.item", item).formatted(Formatting.GRAY));
		evolveButton.setTooltip(Tooltip.of(tooltip));
	}

	private static Text check(boolean ok, Text text) {
		return Text.literal(ok ? "✔ " : "✘ ").formatted(ok ? Formatting.GREEN : Formatting.RED).append(text);
	}

	private static boolean canUpgrade(RoleSyncPayload data, RoleNode node) {
		Role role = node.role();
		boolean active = role == data.primaryRole() || role == data.secondaryRole();
		int rank = data.rank(node.id());
		return active && rank < RoleNode.MAX_RANK && data.level(role) >= node.levelForRank(rank + 1)
				&& (node.prerequisite() == null || data.rank(node.prerequisite()) > 0)
				&& availablePoints(data, role) > 0
				&& !otherPathChosen(data, node);
	}

	/** Sendas excluyentes: no se puede subir un nodo de la senda contraria a la elegida. */
	private static boolean otherPathChosen(RoleSyncPayload data, RoleNode node) {
		dev.aptitudes.role.RoleTree.PathSpec spec = dev.aptitudes.role.RoleTree.pathSpec(node.id());
		if (spec == null) return false;
		String other = node.role().key() + (spec.path() == 1 ? "_b" : "_a");
		for (int i = 1; i <= 3; i++) if (data.rank(other + i) > 0) return true;
		return false;
	}

	private static int availablePoints(RoleSyncPayload data, Role role) {
		int spent = 0;
		for (RoleNode n : RoleTree.nodes(role)) spent += data.rank(n.id());
		return data.rules().earnedPoints(data.level(role)) - spent;
	}

	@Override
	public void tick() {
		super.tick();
		updateButtons();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// --------------------------------------------------------- Geometría

	private List<NodeView> nodes() {
		List<RoleNode> tree = RoleTree.nodes(selected);
		int cx = canvasCenterX();
		int cy = coreY();
		List<NodeView> out = new ArrayList<>();
		out.add(new NodeView(NodeKind.CORE, null, cx, cy));
		out.add(new NodeView(NodeKind.PASSIVE_A, tree.get(0), cx - 58, cy));
		out.add(new NodeView(NodeKind.PASSIVE_B, tree.get(2), cx - 116, cy));
		out.add(new NodeView(NodeKind.ACTIVE_A, tree.get(1), cx + 58, cy));
		out.add(new NodeView(NodeKind.ACTIVE_B, tree.get(3), cx + 116, cy));
		// Fila inferior: entrenamientos bajo las pasivas; Presteza y Eficiencia bajo las activas.
		out.add(new NodeView(NodeKind.STAT_1, tree.get(4), cx - 58, cy + 38));
		out.add(new NodeView(NodeKind.STAT_2, tree.get(5), cx - 116, cy + 38));
		out.add(new NodeView(NodeKind.HASTE, tree.get(6), cx + 58, cy + 38));
		out.add(new NodeView(NodeKind.EFFICIENCY, tree.get(7), cx + 116, cy + 38));
		out.add(new NodeView(NodeKind.MASTERY, tree.get(8), cx + 116, cy - 40));
		out.add(new NodeView(NodeKind.SIGNATURE, null, cx, cy - 40));
		out.add(new NodeView(NodeKind.ULTIMATE, null, cx, cy - 76));
		// Sendas excluyentes: A hacia la izquierda y B hacia la derecha, bajo el núcleo.
		out.add(new NodeView(NodeKind.PATH_A1, tree.get(9), cx - 40, cy + 82));
		out.add(new NodeView(NodeKind.PATH_A2, tree.get(10), cx - 80, cy + 82));
		out.add(new NodeView(NodeKind.PATH_A3, tree.get(11), cx - 120, cy + 82));
		out.add(new NodeView(NodeKind.PATH_B1, tree.get(12), cx + 40, cy + 82));
		out.add(new NodeView(NodeKind.PATH_B2, tree.get(13), cx + 80, cy + 82));
		out.add(new NodeView(NodeKind.PATH_B3, tree.get(14), cx + 120, cy + 82));
		return out;
	}

	@Nullable
	private NodeView nodeView(NodeKind kind) {
		for (NodeView v : nodes()) if (v.kind() == kind) return v;
		return null;
	}

	// ------------------------------------------------------------ Entrada

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Pestañas de categoría
		RoleCategory[] cats = RoleCategory.values();
		for (int i = 0; i < cats.length; i++) {
			int x = left + 8 + i * 25;
			if (mouseX >= x && mouseX < x + 23 && mouseY >= top + 30 && mouseY < top + 44) {
				category = cats[i];
				List<Role> roles = category.roles();
				if (!roles.isEmpty()) selectRole(roles.get(0));
				return true;
			}
		}
		// Lista de roles
		List<Role> roles = category.roles();
		for (int i = 0; i < roles.size(); i++) {
			int y = top + 48 + i * ROLE_ROW_H;
			if (mouseX >= left + 8 && mouseX < left + 8 + LIST_W && mouseY >= y && mouseY < y + ROLE_ROW_H - 2) {
				selectRole(roles.get(i));
				return true;
			}
		}
		if (extraView) {
			if (clickExtra(mouseX, mouseY)) return true;
		} else {
			for (NodeView v : nodes()) {
				if (mouseX >= v.x() && mouseX < v.x() + 24 && mouseY >= v.y() && mouseY < v.y() + 24) {
					selectedNode = v.kind();
					updateButtons();
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			client.setScreen(hasShiftDown() ? new AptitudesScreen() : new UniquesScreen());
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void selectRole(Role role) {
		selected = role;
		category = role.category();
		selectedNode = NodeKind.CORE;
		extraView = false;
		updateButtons();
	}

	private boolean clickExtra(double mouseX, double mouseY) {
		int x0 = canvasX() + 8;
		int y0 = top + 52;
		if (selected == Role.ELEMENTALIST) {
			Element[] elements = Element.values();
			for (int i = 0; i < elements.length; i++) {
				int x = x0 + (i % 3) * 92;
				int y = y0 + (i / 3) * 34;
				if (mouseX >= x && mouseX < x + 88 && mouseY >= y && mouseY < y + 30) {
					ClientPlayNetworking.send(new RoleActionPayload(RoleActionPayload.CHOOSE_ELEMENT, i, ""));
					return true;
				}
			}
		} else {
			MorphForm[] forms = MorphForm.values();
			int shown = 0;
			for (MorphForm form : forms) {
				if (!form.selectable()) continue;
				int x = x0 + (shown % 3) * 92;
				int y = y0 + (shown / 3) * 34;
				shown++;
				if (mouseX >= x && mouseX < x + 88 && mouseY >= y && mouseY < y + 30) {
					ClientPlayNetworking.send(new RoleActionPayload(RoleActionPayload.SELECT_FORM, form.ordinal(), ""));
					return true;
				}
			}
		}
		return false;
	}

	// ------------------------------------------------------------- Dibujo

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, W, H);
		UiStyle.panel(ctx, canvasX(), top + 26, canvasW(), H - 58);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawTextWithShadow(textRenderer, title.copy().formatted(Formatting.BOLD), left + 12, top + 9, UiStyle.GOLD);
		RoleSyncPayload data = ClientRoleState.data();
		if (data == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.waiting"),
					width / 2, top + H / 2, UiStyle.MUTED);
			return;
		}
		renderSynergy(ctx, data);
		List<Text> tooltip = renderLeft(ctx, data, mouseX, mouseY);
		if (extraView) {
			List<Text> t = selected == Role.ELEMENTALIST ? renderElements(ctx, data, mouseX, mouseY) : renderForms(ctx, data, mouseX, mouseY);
			if (t != null) tooltip = t;
		} else {
			List<Text> t = renderTree(ctx, data, mouseX, mouseY);
			if (t != null) tooltip = t;
			renderNodeInfo(ctx, data);
		}
		if (tooltip != null) ctx.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
	}

	private void renderSynergy(DrawContext ctx, RoleSyncPayload data) {
		Role p = data.primaryRole();
		Role s = data.secondaryRole();
		if (p == null || s == null) return;
		boolean same = p.category() == s.category();
		Text text = Text.translatable(same ? "screen.aptitudes.synergy.specialization" : "screen.aptitudes.synergy.versatility");
		ctx.drawText(textRenderer, text, left + W - 12 - textRenderer.getWidth(text), top + 10, same ? UiStyle.GOOD : UiStyle.UP, false);
	}

	private List<Text> renderLeft(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		List<Text> tooltip = null;
		RoleCategory[] cats = RoleCategory.values();
		for (int i = 0; i < cats.length; i++) {
			int x = left + 8 + i * 25;
			boolean active = cats[i] == category;
			ctx.fill(x, top + 30, x + 23, top + 44, active ? 0xFF000000 | cats[i].color() : 0x60303850);
			ctx.drawBorder(x, top + 30, 23, 14, active ? UiStyle.GOLD : UiStyle.BORDER);
			Text label = Text.translatable(cats[i].translationKey() + ".short");
			ctx.drawText(textRenderer, label, x + 12 - textRenderer.getWidth(label) / 2, top + 33, UiStyle.TEXT, false);
			if (mouseX >= x && mouseX < x + 23 && mouseY >= top + 30 && mouseY < top + 44) {
				tooltip = List.of(Text.translatable(cats[i].translationKey()).withColor(cats[i].color()),
						Text.translatable(cats[i].translationKey() + ".desc").formatted(Formatting.GRAY));
			}
		}
		List<Role> roles = category.roles();
		for (int i = 0; i < roles.size(); i++) {
			Role role = roles.get(i);
			int y = top + 48 + i * ROLE_ROW_H;
			UiStyle.row(ctx, left + 8, y, LIST_W, ROLE_ROW_H - 2, i, role == selected);
			ctx.drawItem(role.iconStack(), left + 9, y);
			ctx.drawText(textRenderer, Text.translatable(role.translationKey()), left + 28, y + 4, UiStyle.TEXT, false);
			String marker = role == data.primaryRole() ? "P" : role == data.secondaryRole() ? "S" : "";
			String stars = "★".repeat(data.tier(role));
			String right = (marker.isEmpty() ? "" : marker + " ") + stars + data.level(role);
			ctx.drawText(textRenderer, right, left + 4 + LIST_W - textRenderer.getWidth(right), y + 4,
					marker.isEmpty() ? UiStyle.MUTED : UiStyle.GOLD, false);
			if (mouseX >= left + 8 && mouseX < left + 8 + LIST_W && mouseY >= y && mouseY < y + ROLE_ROW_H - 2) {
				tooltip = List.of(Text.translatable(Evolution.titleKey(role, data.tier(role))).withColor(role.color()),
						Text.translatable(role.descriptionKey()).formatted(Formatting.GRAY));
			}
		}

		// Ficha del rol seleccionado
		int y = top + 124;
		RoleRules rules = data.rules();
		int level = data.level(selected);
		UiStyle.header(ctx, textRenderer, Text.translatable(Evolution.titleKey(selected, data.tier(selected))),
				left + 8, y, LIST_W);
		Text lv = level >= rules.maxLevel() ? Text.translatable("screen.aptitudes.max") : Text.translatable("screen.aptitudes.level", level);
		ctx.drawText(textRenderer, lv, left + 8, y + 16, UiStyle.TEXT, false);
		int pts = availablePoints(data, selected);
		Text points = Text.translatable("screen.aptitudes.points", pts);
		ctx.drawText(textRenderer, points, left + 8 + LIST_W - textRenderer.getWidth(points), y + 16,
				pts > 0 ? UiStyle.GOOD : UiStyle.MUTED, false);
		ctx.fill(left + 8, y + 28, left + 8 + LIST_W, y + 31, 0xFF202433);
		double fill = level >= rules.maxLevel() ? 1 : Math.min(1, data.xp(selected) / (double) rules.xpToNext(level));
		ctx.fill(left + 8, y + 28, left + 8 + (int) (LIST_W * fill), y + 31, 0xFF000000 | selected.color());
		if (data.roleChangeCooldown() > 0) {
			ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.roles.change_cd",
					(data.roleChangeCooldown() + 1199) / 1200), left + 8, y + 36, UiStyle.DOWN, false);
		}
		return tooltip;
	}

	private List<Text> renderTree(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		List<NodeView> views = nodes();
		int tier = data.tier(selected);
		// Conexiones
		link(ctx, views, NodeKind.CORE, NodeKind.PASSIVE_A, lit(data, NodeKind.PASSIVE_A));
		link(ctx, views, NodeKind.PASSIVE_A, NodeKind.PASSIVE_B, lit(data, NodeKind.PASSIVE_B));
		link(ctx, views, NodeKind.CORE, NodeKind.ACTIVE_A, lit(data, NodeKind.ACTIVE_A));
		link(ctx, views, NodeKind.ACTIVE_A, NodeKind.ACTIVE_B, lit(data, NodeKind.ACTIVE_B));
		link(ctx, views, NodeKind.PASSIVE_A, NodeKind.STAT_1, lit(data, NodeKind.STAT_1));
		link(ctx, views, NodeKind.STAT_1, NodeKind.STAT_2, lit(data, NodeKind.STAT_2));
		link(ctx, views, NodeKind.ACTIVE_A, NodeKind.HASTE, lit(data, NodeKind.HASTE));
		link(ctx, views, NodeKind.HASTE, NodeKind.EFFICIENCY, lit(data, NodeKind.EFFICIENCY));
		link(ctx, views, NodeKind.ACTIVE_B, NodeKind.MASTERY, lit(data, NodeKind.MASTERY));
		link(ctx, views, NodeKind.CORE, NodeKind.PATH_A1, lit(data, NodeKind.PATH_A1));
		link(ctx, views, NodeKind.PATH_A1, NodeKind.PATH_A2, lit(data, NodeKind.PATH_A2));
		link(ctx, views, NodeKind.PATH_A2, NodeKind.PATH_A3, lit(data, NodeKind.PATH_A3));
		link(ctx, views, NodeKind.CORE, NodeKind.PATH_B1, lit(data, NodeKind.PATH_B1));
		link(ctx, views, NodeKind.PATH_B1, NodeKind.PATH_B2, lit(data, NodeKind.PATH_B2));
		link(ctx, views, NodeKind.PATH_B2, NodeKind.PATH_B3, lit(data, NodeKind.PATH_B3));
		renderPathLabels(ctx, data);
		link(ctx, views, NodeKind.CORE, NodeKind.SIGNATURE, tier >= Evolution.MASTER);
		link(ctx, views, NodeKind.SIGNATURE, NodeKind.ULTIMATE, tier >= Evolution.ULTIMATE);

		List<Text> tooltip = null;
		for (NodeView v : views) {
			boolean hover = mouseX >= v.x() && mouseX < v.x() + 24 && mouseY >= v.y() && mouseY < v.y() + 24;
			UiStyle.Frame frame = switch (v.kind()) {
				case CORE -> UiStyle.Frame.CORE;
				case PASSIVE_A, PASSIVE_B, STAT_1, STAT_2, PATH_A1, PATH_A2, PATH_A3, PATH_B1, PATH_B2, PATH_B3 ->
						UiStyle.Frame.PASSIVE;
				case ACTIVE_A, ACTIVE_B, HASTE, EFFICIENCY, MASTERY -> UiStyle.Frame.ACTIVE;
				case SIGNATURE -> UiStyle.Frame.SIGNATURE;
				case ULTIMATE -> UiStyle.Frame.ULTIMATE;
			};
			boolean lit = lit(data, v.kind());
			UiStyle.frame(ctx, v.x(), v.y(), frame, lit, hover || v.kind() == selectedNode);
			ctx.drawItem(nodeIcon(v), v.x() + 4, v.y() + 4);
			if (!lit) ctx.fill(v.x() + 2, v.y() + 2, v.x() + 22, v.y() + 22, 0x90101018);
			if (pathLocked(data, v)) ctx.fill(v.x() + 2, v.y() + 2, v.x() + 22, v.y() + 22, 0x90801010); // senda bloqueada
			if (v.node() != null) {
				int rank = data.rank(v.node().id());
				String r = rank + (rank > 0 && tier > 0 ? "+" + tier : "");
				ctx.drawText(textRenderer, r, v.x() + 12 - textRenderer.getWidth(r) / 2, v.y() + 26,
						rank >= RoleNode.MAX_RANK ? UiStyle.GOLD : UiStyle.MUTED, false);
			}
			if (hover) tooltip = List.of(nodeName(v).copy().formatted(Formatting.YELLOW), nodeType(v).copy().formatted(Formatting.GRAY));
		}
		return tooltip;
	}

	/** Icono de cada nodo: el del rol para los propios y uno genérico para los comunes. */
	private net.minecraft.item.ItemStack nodeIcon(NodeView v) {
		return switch (v.kind()) {
			case CORE -> selected.iconStack();
			case SIGNATURE -> dev.aptitudes.role.NodeIcons.signature(selected);
			case ULTIMATE -> dev.aptitudes.role.NodeIcons.ultimate(selected);
			default -> dev.aptitudes.role.NodeIcons.of(v.node());
		};
	}

	/** {@code true} si el nodo es de la senda contraria a la ya elegida. */
	private boolean pathLocked(RoleSyncPayload data, NodeView v) {
		if (v.node() == null) return false;
		dev.aptitudes.role.RoleTree.PathSpec spec = dev.aptitudes.role.RoleTree.pathSpec(v.node().id());
		if (spec == null) return false;
		int chosen = chosenPath(data);
		return chosen != 0 && chosen != spec.path();
	}

	private int chosenPath(RoleSyncPayload data) {
		for (int i = 1; i <= 3; i++) {
			if (data.rank(selected.key() + "_a" + i) > 0) return 1;
			if (data.rank(selected.key() + "_b" + i) > 0) return 2;
		}
		return 0;
	}

	private void renderPathLabels(DrawContext ctx, RoleSyncPayload data) {
		int cx = canvasCenterX();
		int y = coreY() + 70;
		int chosen = chosenPath(data);
		Text a = Text.translatable(dev.aptitudes.role.RoleTree.pathKey(selected, 1));
		Text b = Text.translatable(dev.aptitudes.role.RoleTree.pathKey(selected, 2));
		ctx.drawText(textRenderer, a, cx - 96 + 12 - textRenderer.getWidth(a) / 2, y,
				chosen == 2 ? UiStyle.DOWN : UiStyle.GOLD, false);
		ctx.drawText(textRenderer, b, cx + 104 + 12 - textRenderer.getWidth(b) / 2, y,
				chosen == 1 ? UiStyle.DOWN : UiStyle.GOLD, false);
		Text or = Text.translatable("screen.aptitudes.path.or");
		ctx.drawText(textRenderer, or, cx + 12 - textRenderer.getWidth(or) / 2, y + 16, UiStyle.MUTED, false);
	}

	private boolean lit(RoleSyncPayload data, NodeKind kind) {
		return switch (kind) {
			case CORE -> true;
			case SIGNATURE -> data.tier(selected) >= Evolution.MASTER;
			case ULTIMATE -> data.tier(selected) >= Evolution.ULTIMATE;
			default -> {
				NodeView v = nodeView(kind);
				yield v != null && v.node() != null && data.rank(v.node().id()) > 0;
			}
		};
	}

	private void link(DrawContext ctx, List<NodeView> views, NodeKind a, NodeKind b, boolean lit) {
		NodeView va = null, vb = null;
		for (NodeView v : views) {
			if (v.kind() == a) va = v;
			if (v.kind() == b) vb = v;
		}
		if (va == null || vb == null) return;
		UiStyle.line(ctx, va.x() + 11, va.y() + 11, vb.x() + 11, vb.y() + 11, lit ? UiStyle.GOLD_DARK : 0xFF2C3044);
	}

	private Text nodeName(NodeView v) {
		return switch (v.kind()) {
			case CORE -> Text.translatable(selected.translationKey());
			case SIGNATURE -> Text.translatable(Evolution.signatureKey(selected));
			case ULTIMATE -> Text.translatable(Evolution.ultimateKey(selected));
			default -> Text.translatable(v.node().translationKey());
		};
	}

	private Text nodeType(NodeView v) {
		return Text.translatable(switch (v.kind()) {
			case CORE -> "screen.aptitudes.node.core";
			case PASSIVE_A, PASSIVE_B -> "screen.aptitudes.roles.passive";
			case ACTIVE_A, ACTIVE_B -> "screen.aptitudes.roles.active";
			case STAT_1, STAT_2 -> "screen.aptitudes.node.stat";
			case PATH_A1, PATH_A2, PATH_A3, PATH_B1, PATH_B2, PATH_B3 -> "screen.aptitudes.node.path";
			case HASTE, EFFICIENCY, MASTERY -> "screen.aptitudes.node.upgrade";
			case SIGNATURE -> "screen.aptitudes.node.signature";
			case ULTIMATE -> "screen.aptitudes.node.ultimate";
		});
	}

	/** Ficha del nodo seleccionado, bajo el árbol. */
	private void renderNodeInfo(DrawContext ctx, RoleSyncPayload data) {
		NodeView v = nodeView(selectedNode);
		if (v == null) return;
		int x = canvasX() + 8;
		int y = coreY() + 116;
		int w = canvasW() - 16;
		ctx.fill(x, y - 4, x + w, y - 3, UiStyle.BORDER);
		ctx.drawTextWithShadow(textRenderer, nodeName(v), x, y, UiStyle.GOLD);
		Text type = nodeType(v);
		ctx.drawText(textRenderer, type, x + w - textRenderer.getWidth(type), y, UiStyle.MUTED, false);

		Text desc;
		Text extra = null;
		switch (v.kind()) {
			case CORE -> desc = Text.translatable(selected.descriptionKey());
			case SIGNATURE -> {
				desc = Text.translatable(Evolution.signatureKey(selected) + ".desc");
				extra = Text.translatable("screen.aptitudes.node.signature_req");
			}
			case ULTIMATE -> {
				desc = Text.translatable(Evolution.ultimateKey(selected) + ".desc");
				extra = Text.translatable("screen.aptitudes.node.ultimate_req");
			}
			default -> {
				RoleNode node = v.node();
				desc = Text.translatable(node.descriptionKey());
				int rank = data.rank(node.id());
				Text rankText = Text.translatable("screen.aptitudes.node.rank", rank, RoleNode.MAX_RANK,
						data.tier(selected));
				Text req = rank >= RoleNode.MAX_RANK ? Text.empty()
						: Text.translatable("screen.aptitudes.roles.requires", node.levelForRank(rank + 1));
				extra = node.active()
						? rankText.copy().append(" · ").append(Text.translatable("screen.aptitudes.node.cost",
						node.energyCost(), node.cooldownSecs())).append(" · ").append(req)
						: rankText.copy().append(" · ").append(req);
			}
		}
		int ly = y + 12;
		for (OrderedText line : textRenderer.wrapLines(desc, w - 80)) {
			ctx.drawText(textRenderer, line, x, ly, UiStyle.TEXT, false);
			ly += 10;
			if (ly > top + H - 64) break;
		}
		if (extra != null) ctx.drawText(textRenderer, extra, x, top + H - 50, UiStyle.UP, false);
	}

	private List<Text> renderElements(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		int x0 = canvasX() + 8;
		int y0 = top + 52;
		List<Element> mastered = data.elementList();
		Element active = data.activeElementValue();
		int allowed = Element.allowedCount(data.level(Role.ELEMENTALIST));
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.elements.header", mastered.size(), allowed,
				Element.MAX_MASTERED), x0, top + 34, 200);
		List<Text> tooltip = null;
		Element[] elements = Element.values();
		for (int i = 0; i < elements.length; i++) {
			Element e = elements[i];
			int x = x0 + (i % 3) * 92;
			int y = y0 + (i / 3) * 34;
			boolean own = mastered.contains(e);
			ctx.fill(x, y, x + 88, y + 30, own ? 0x70000000 | (e.color() & 0xFFFFFF) : 0x50202433);
			ctx.drawBorder(x, y, 88, 30, e == active ? 0xFFFFFFFF : own ? 0xFF000000 | e.color() : UiStyle.BORDER);
			ctx.drawTextWithShadow(textRenderer, Text.translatable(e.translationKey()), x + 6, y + 6, 0xFF000000 | e.color());
			Text state = Text.translatable(e == active ? "screen.aptitudes.elements.active"
					: own ? "screen.aptitudes.elements.mastered" : "screen.aptitudes.elements.available");
			ctx.drawText(textRenderer, state, x + 6, y + 18, UiStyle.MUTED, false);
			if (mouseX >= x && mouseX < x + 88 && mouseY >= y && mouseY < y + 30) {
				tooltip = List.of(Text.translatable(e.translationKey()).withColor(e.color()),
						Text.translatable(e.descriptionKey()).formatted(Formatting.GRAY));
			}
		}
		if (mastered.size() < Element.MAX_MASTERED) {
			Text hint = mastered.size() < allowed ? Text.translatable("screen.aptitudes.elements.choose")
					: Text.translatable("screen.aptitudes.elements.next", Element.UNLOCK_LEVELS[mastered.size()]);
			ctx.drawText(textRenderer, hint, x0, top + H - 50, UiStyle.UP, false);
		}
		return tooltip;
	}

	private List<Text> renderForms(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		FormSyncPayload forms = ClientExtraState.forms();
		int x0 = canvasX() + 8;
		int y0 = top + 52;
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.forms.header"), x0, top + 34, 200);
		List<Text> tooltip = null;
		int shown = 0;
		for (MorphForm form : MorphForm.values()) {
			if (!form.selectable()) continue;
			int x = x0 + (shown % 3) * 92;
			int y = y0 + (shown / 3) * 34;
			shown++;
			boolean unlocked = forms != null && forms.unlocked(form.ordinal());
			boolean chosen = forms != null && forms.selected() == form.ordinal();
			ctx.fill(x, y, x + 88, y + 30, unlocked ? 0x70303850 : 0x40202433);
			ctx.drawBorder(x, y, 88, 30, chosen ? UiStyle.GOLD : UiStyle.BORDER);
			ctx.drawItem(form.iconStack(), x + 4, y + 7);
			ctx.drawText(textRenderer, Text.translatable(form.translationKey()), x + 24, y + 5,
					unlocked ? UiStyle.TEXT : UiStyle.MUTED, false);
			Text progress;
			if (unlocked) {
				progress = Text.translatable(chosen ? "screen.aptitudes.forms.selected" : "screen.aptitudes.forms.unlocked");
			} else if (form.levelToUnlock() > 0) {
				progress = Text.translatable("screen.aptitudes.roles.requires", form.levelToUnlock());
			} else {
				int kills = forms == null || form.ordinal() >= forms.kills().length ? 0 : forms.kills()[form.ordinal()];
				progress = Text.translatable("screen.aptitudes.forms.kills", kills, form.killsToUnlock());
			}
			ctx.drawText(textRenderer, progress, x + 24, y + 17, unlocked ? UiStyle.GOOD : UiStyle.MUTED, false);
			if (mouseX >= x && mouseX < x + 88 && mouseY >= y && mouseY < y + 30) {
				tooltip = List.of(Text.translatable(form.translationKey()).formatted(Formatting.YELLOW),
						Text.translatable(form.descriptionKey()).formatted(Formatting.GRAY));
			}
		}
		return tooltip;
	}
}
