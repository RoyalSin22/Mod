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
import net.minecraft.util.math.MathHelper;
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
	/** Tamaño de la ventana: crece con la pantalla (mínimo 440×322). */
	private int W = 440;
	private int H = 322;
	private static final int LIST_W = 124;
	private static final int ROLE_ROW_H = 18;

	/** Tipo de casilla del lienzo: un nodo del árbol o una de las 3 especiales (núcleo, característica, definitiva). */
	private enum NodeKind { CORE, NODE, SIGNATURE, ULTIMATE }

	private record NodeView(NodeKind kind, @Nullable RoleNode node, int x, int y) {
		String key() {
			return node != null ? node.id() : kind.name();
		}
	}

	/** X local de cada columna: tres zonas (pasivas 0-2, sendas 3-5, activas 6-8) con separación entre zonas. */
	private static final int[] COL_X = {8, 46, 84, 136, 194, 252, 304, 342, 380};
	private static final int ROW_H = 38;
	/** Zonas del árbol: x inicial, x final, color y clave del título. */
	private static final int[][] ZONES = {{0, 112, 0x182A3A55}, {124, 288, 0x18C9A54C}, {296, 412, 0x18E57373}};
	private static final String[] ZONE_KEYS = {"screen.aptitudes.tree.passives", "screen.aptitudes.tree.paths",
			"screen.aptitudes.tree.actives"};
	/** Colores de las tres sendas (A, B, C). */
	private static final int[] PATH_COLORS = {0xFFE57373, 0xFF64B5F6, 0xFF81C784};

	private int left;
	private int top;
	private RoleCategory category = RoleCategory.MARTIAL;
	private Role selected = Role.BERSERKER;
	/** Clave de la casilla seleccionada: id del nodo o nombre de {@link NodeKind}. */
	private String selectedNode = NodeKind.CORE.name();
	private boolean extraView;

	private ButtonWidget primaryButton;
	private ButtonWidget secondaryButton;
	private ButtonWidget evolveButton;
	private ButtonWidget upgradeButton;
	private ButtonWidget extraButton;
	private ButtonWidget equip1Button;
	private ButtonWidget equip2Button;

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
	// Coordenadas LOCALES del árbol (antes de desplazar y escalar la vista).
	private int gridX() { return 40; }
	private int gridY() { return 8; }
	private static final int CONTENT_W = 416;
	private static final int CONTENT_H = 8 + ROW_H * 6 + 24 + 14;

	// Vista del árbol: desplazamiento (arrastrar) y zoom (rueda).
	private double panX;
	private double panY;
	private float zoom = 1.0f;
	private boolean dragging;

	private int infoTop() { return top + H - 100; }
	private int viewX1() { return canvasX() + 1; }
	private int viewY1() { return top + 27; }
	private int viewX2() { return canvasX() + canvasW() - 1; }
	private int viewY2() { return infoTop() - 4; }

	private boolean inView(double mx, double my) {
		return mx >= viewX1() && mx < viewX2() && my >= viewY1() && my < viewY2();
	}

	private double toLocalX(double mx) { return (mx - viewX1() - panX) / zoom; }
	private double toLocalY(double my) { return (my - viewY1() - panY) / zoom; }

	/** Ajusta el zoom para que quepa el árbol entero y lo centra. */
	private void resetView() {
		int vw = viewX2() - viewX1();
		int vh = viewY2() - viewY1();
		zoom = MathHelper.clamp(Math.min(vw / (float) CONTENT_W, vh / (float) CONTENT_H), 0.6f, 1.8f);
		panX = (vw - CONTENT_W * zoom) / 2.0;
		panY = Math.max(0, (vh - CONTENT_H * zoom) / 2.0);
	}

	@Override
	protected void init() {
		W = MathHelper.clamp(width - 20, 440, 760);
		H = MathHelper.clamp(height - 16, 322, 480);
		left = (width - W) / 2;
		top = (height - H) / 2;
		int lx = left + 8;
		primaryButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.set_primary"),
				b -> send(RoleActionPayload.SET_PRIMARY, "")).dimensions(lx, top + 182, LIST_W, 16).build());
		secondaryButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.set_secondary"),
				b -> send(RoleActionPayload.SET_SECONDARY, "")).dimensions(lx, top + 200, LIST_W, 16).build());
		evolveButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.evolve"),
				b -> send(RoleActionPayload.EVOLVE, "")).dimensions(lx, top + 218, LIST_W, 16).build());
		resetView();
		upgradeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.upgrade"), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.UPGRADE_NODE, v.node().id());
		}).dimensions(canvasX() + canvasW() - 76, top + H - 70, 70, 16).build());
		equip1Button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.equip", 1), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.EQUIP_1, v.node().id());
		}).dimensions(canvasX() + canvasW() - 76, top + H - 87, 34, 16).build());
		equip2Button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.equip", 2), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.EQUIP_2, v.node().id());
		}).dimensions(canvasX() + canvasW() - 40, top + H - 87, 34, 16).build());
		extraButton = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> extraView = !extraView)
				.dimensions(canvasX() + canvasW() - 150, top + H - 70, 70, 16).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.tab.attributes"),
				b -> client.setScreen(new AptitudesScreen())).dimensions(width / 2 - 102, top + H - 26, 100, 18).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(width / 2 + 2, top + H - 26, 100, 18).build());
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
		boolean equippable = !extraView && ready && v != null && v.node() != null && v.node().active()
				&& data.rank(v.node().id()) > 0;
		equip1Button.visible = equippable;
		equip2Button.visible = equippable;
		if (equippable) {
			int index = RoleTree.activeIndex(v.node());
			equip1Button.active = data.equipped(selected, 0) != index;
			equip2Button.active = data.equipped(selected, 1) != index;
		}

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
		return active && rank < node.maxRank() && data.level(role) >= node.levelForRank(rank + 1)
				&& (node.prerequisite() == null || data.rank(node.prerequisite()) > 0)
				&& availablePoints(data, role) > 0
				&& !pathLocked(data, node);
	}

	/** Solo 2 de las 3 sendas: un nodo de una tercera senda queda bloqueado. */
	private static boolean pathLocked(RoleSyncPayload data, @Nullable RoleNode node) {
		if (node == null) return false;
		RoleTree.PathSpec spec = RoleTree.pathSpec(node.id());
		return spec != null && RoleTree.pathLocked(data::rank, node.role(), spec.path());
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
		List<NodeView> out = new ArrayList<>();
		// Columna izquierda: núcleo, característica y definitiva.
		// Fila 0, centro: característica · núcleo · definitiva.
		out.add(new NodeView(NodeKind.CORE, null, COL_X[4], gridY()));
		out.add(new NodeView(NodeKind.SIGNATURE, null, COL_X[3], gridY()));
		out.add(new NodeView(NodeKind.ULTIMATE, null, COL_X[5], gridY()));
		for (RoleNode node : RoleTree.nodes(selected)) {
			RoleTree.GridPos pos = RoleTree.position(node.id());
			if (pos == null) continue;
			out.add(new NodeView(NodeKind.NODE, node, COL_X[Math.min(8, pos.col())], gridY() + pos.row() * ROW_H));
		}
		return out;
	}

	@Nullable
	private NodeView nodeView(String key) {
		for (NodeView v : nodes()) if (v.key().equals(key)) return v;
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
			if (inView(mouseX, mouseY)) {
				double lx = toLocalX(mouseX);
				double ly = toLocalY(mouseY);
				for (NodeView v : nodes()) {
					if (lx >= v.x() && lx < v.x() + 24 && ly >= v.y() && ly < v.y() + 24) {
						selectedNode = v.key();
						updateButtons();
						return true;
					}
				}
				dragging = true; // clic en vacío: empieza a arrastrar la vista
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!extraView && (dragging || (button == 1 && inView(mouseX, mouseY)))) {
			panX += deltaX;
			panY += deltaY;
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!extraView && inView(mouseX, mouseY) && verticalAmount != 0) {
			float old = zoom;
			zoom = MathHelper.clamp(zoom * (verticalAmount > 0 ? 1.15f : 1 / 1.15f), 0.6f, 2.4f);
			// El punto bajo el cursor se queda fijo.
			double ax = mouseX - viewX1();
			double ay = mouseY - viewY1();
			panX = ax - (ax - panX) * zoom / old;
			panY = ay - (ay - panY) * zoom / old;
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_C && !extraView) {
			resetView();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			client.setScreen(new AptitudesScreen());
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void selectRole(Role role) {
		selected = role;
		category = role.category();
		selectedNode = NodeKind.CORE.name();
		extraView = false;
		resetView();
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
			String marker = role == data.primaryRole() ? "P" : role == data.secondaryRole() ? "S" : "";
			String stars = "★".repeat(data.tier(role));
			String right = (marker.isEmpty() ? "" : marker + " ") + stars + data.level(role);
			UiStyle.label(ctx, textRenderer, Text.translatable(role.translationKey()), left + 28, y + 4,
					LIST_W - 26 - textRenderer.getWidth(right), UiStyle.TEXT);
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
		// Nivel y puntos en dos líneas: en una sola se pisaban (sobre todo traducidos).
		Text lv = level >= rules.maxLevel() ? Text.translatable("screen.aptitudes.max") : Text.translatable("screen.aptitudes.level", level);
		UiStyle.label(ctx, textRenderer, lv, left + 8, y + 15, LIST_W, UiStyle.TEXT);
		int pts = availablePoints(data, selected);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.points", pts), left + 8, y + 25, LIST_W,
				pts > 0 ? UiStyle.GOOD : UiStyle.MUTED);
		ctx.fill(left + 8, y + 36, left + 8 + LIST_W, y + 39, 0xFF202433);
		double fill = level >= rules.maxLevel() ? 1 : Math.min(1, data.xp(selected) / (double) rules.xpToNext(level));
		ctx.fill(left + 8, y + 36, left + 8 + (int) (LIST_W * fill), y + 39, 0xFF000000 | selected.color());
		// Ritual de la Hoguera: cambios de rol disponibles.
		Text rebirth = data.rebirths() > 0 ? Text.translatable("screen.aptitudes.roles.rebirths", data.rebirths())
				: Text.translatable("screen.aptitudes.roles.need_ritual");
		UiStyle.label(ctx, textRenderer, rebirth, left + 8, y + 44, LIST_W, data.rebirths() > 0 ? UiStyle.GOOD : UiStyle.MUTED);
		return tooltip;
	}

	private List<Text> renderTree(DrawContext ctx, RoleSyncPayload data, int screenMouseX, int screenMouseY) {
		ctx.enableScissor(viewX1(), viewY1(), viewX2(), viewY2());
		ctx.getMatrices().push();
		ctx.getMatrices().translate(viewX1() + panX, viewY1() + panY, 0);
		ctx.getMatrices().scale(zoom, zoom, 1);
		boolean over = inView(screenMouseX, screenMouseY);
		int mouseX = over ? (int) Math.floor(toLocalX(screenMouseX)) : -10_000;
		int mouseY = over ? (int) Math.floor(toLocalY(screenMouseY)) : -10_000;
		List<Text> tooltip = renderTreeLocal(ctx, data, mouseX, mouseY);
		ctx.getMatrices().pop();
		ctx.disableScissor();
		Text hint = Text.translatable("screen.aptitudes.tree.hint");
		UiStyle.label(ctx, textRenderer, hint, viewX2() - 4 - Math.min(textRenderer.getWidth(hint), canvasW() - 10),
				viewY2() - 10, canvasW() - 10, 0xFF5A6078);
		return tooltip;
	}

	private List<Text> renderTreeLocal(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		List<NodeView> views = nodes();
		int tier = data.tier(selected);
		renderZones(ctx);
		// Conexiones en ángulo recto: cada nodo con su requisito; las raíces (fila 1), con el núcleo.
		NodeView core = views.get(0);
		NodeView sig = views.get(1);
		NodeView ult = views.get(2);
		for (NodeView v : views) {
			if (v.node() == null) continue;
			String pre = v.node().prerequisite();
			NodeView parent = pre == null ? core : nodeView(pre);
			if (parent == null) continue;
			boolean on = data.rank(v.node().id()) > 0;
			boolean open = !on && available(data, v);
			elbow(ctx, parent, v, on ? UiStyle.GOLD : open ? 0xFF6A7090 : 0xFF2C3044);
		}
		hline(ctx, sig, core, tier >= Evolution.MASTER ? UiStyle.GOLD : 0xFF2C3044);
		hline(ctx, core, ult, tier >= Evolution.ULTIMATE ? UiStyle.GOLD : 0xFF2C3044);
		renderPathLabels(ctx, data);

		List<Text> tooltip = null;
		for (NodeView v : views) {
			boolean hover = mouseX >= v.x() && mouseX < v.x() + 24 && mouseY >= v.y() && mouseY < v.y() + 24;
			UiStyle.Frame frame = switch (v.kind()) {
				case CORE -> UiStyle.Frame.CORE;
				case SIGNATURE -> UiStyle.Frame.SIGNATURE;
				case ULTIMATE -> UiStyle.Frame.ULTIMATE;
				case NODE -> v.node().kind() == RoleNode.Kind.LEGENDARY ? UiStyle.Frame.UNIQUE
						: v.node().active() || v.node().kind() == RoleNode.Kind.HASTE
						|| v.node().kind() == RoleNode.Kind.EFFICIENCY || v.node().kind() == RoleNode.Kind.MASTERY
						? UiStyle.Frame.ACTIVE : UiStyle.Frame.PASSIVE;
			};
			boolean lit = lit(data, v);
			UiStyle.frame(ctx, v.x(), v.y(), frame, lit, hover || v.key().equals(selectedNode));
			ctx.drawItem(nodeIcon(v), v.x() + 4, v.y() + 4);
			if (!lit) ctx.fill(v.x() + 2, v.y() + 2, v.x() + 22, v.y() + 22, available(data, v) ? 0x60101018 : 0x90101018);
			if (pathLocked(data, v.node())) ctx.fill(v.x() + 2, v.y() + 2, v.x() + 22, v.y() + 22, 0x90801010);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(0, 0, 200);
			if (v.node() != null) {
				RoleTree.PathSpec spec = RoleTree.pathSpec(v.node().id());
				if (spec != null) ctx.fill(v.x() + 2, v.y() + 2, v.x() + 5, v.y() + 5, PATH_COLORS[spec.path() - 1]);
				int rank = data.rank(v.node().id());
				String r = rank + (rank > 0 && tier > 0 ? "+" + tier : "");
				ctx.drawTextWithShadow(textRenderer, r, v.x() + 22 - textRenderer.getWidth(r), v.y() + 15,
						rank >= v.node().maxRank() ? UiStyle.GOLD : rank > 0 ? UiStyle.TEXT : UiStyle.MUTED);
				// Activas equipadas: "1" / "2" en la esquina superior derecha.
				if (v.node().active()) {
					int index = RoleTree.activeIndex(v.node());
					String slot = data.equipped(selected, 0) == index ? "1" : data.equipped(selected, 1) == index ? "2" : "";
					if (!slot.isEmpty()) {
						ctx.fill(v.x() + 16, v.y() + 1, v.x() + 23, v.y() + 9, 0xE0201A10);
						ctx.drawText(textRenderer, slot, v.x() + 18, v.y() + 1, UiStyle.GOLD, false);
					}
				}
			}
			ctx.getMatrices().pop();
			if (hover) {
				List<Text> lines = new ArrayList<>();
				lines.add(nodeName(v).copy().formatted(Formatting.YELLOW));
				lines.add(nodeType(v).copy().formatted(Formatting.GRAY));
				if (v.node() != null) {
					RoleTree.PathSpec spec = RoleTree.pathSpec(v.node().id());
					if (spec != null) lines.add(Text.translatable(RoleTree.pathKey(selected, spec.path()))
							.withColor(PATH_COLORS[spec.path() - 1] & 0xFFFFFF));
					if (v.node().prerequisite() != null && data.rank(v.node().prerequisite()) <= 0) {
						RoleNode pre = RoleTree.get(v.node().prerequisite());
						if (pre != null) lines.add(Text.translatable("screen.aptitudes.node.needs",
								Text.translatable(pre.translationKey())).formatted(Formatting.RED));
					}
				}
				tooltip = lines;
			}
		}
		return tooltip;
	}

	/** Conector en ángulo recto de 2 px: baja desde el padre, cruza en horizontal a media altura y baja al hijo. */
	private void elbow(DrawContext ctx, NodeView parent, NodeView child, int color) {
		int px = parent.x() + 11;
		int cx = child.x() + 11;
		int py = parent.y() + 24;
		int cy = child.y();
		int mid = cy - 7;
		if (mid < py) mid = py + (cy - py) / 2;
		ctx.fill(px, py, px + 2, mid + 2, color);
		ctx.fill(Math.min(px, cx), mid, Math.max(px, cx) + 2, mid + 2, color);
		ctx.fill(cx, mid, cx + 2, cy, color);
	}

	/** Conector horizontal entre dos casillas de la misma fila. */
	private void hline(DrawContext ctx, NodeView a, NodeView b, int color) {
		int y = a.y() + 11;
		ctx.fill(a.x() + 24, y, b.x(), y + 2, color);
	}

	/** Fondos de las tres zonas del árbol con su título. */
	private void renderZones(DrawContext ctx) {
		int top = gridY() + ROW_H - 10;
		int bottom = gridY() + ROW_H * 6 + 30;
		for (int i = 0; i < ZONES.length; i++) {
			int[] z = ZONES[i];
			ctx.fill(z[0], top, z[1], bottom, z[2] | 0x20000000);
			ctx.drawBorder(z[0], top, z[1] - z[0], bottom - top, (z[2] & 0xFFFFFF) | 0x50000000);
			Text title = Text.translatable(ZONE_KEYS[i]);
			int tw = (int) (textRenderer.getWidth(title) * 0.75f);
			ctx.getMatrices().push();
			ctx.getMatrices().translate((z[0] + z[1]) / 2f - tw / 2f, top + 2, 0);
			ctx.getMatrices().scale(0.75f, 0.75f, 1f);
			ctx.drawText(textRenderer, title, 0, 0, 0xFF8A8FA3, false);
			ctx.getMatrices().pop();
		}
	}

	/** Un nodo no aprendido cuyo requisito ya se cumple se oscurece menos. */
	private boolean available(RoleSyncPayload data, NodeView v) {
		if (v.node() == null) return false;
		String pre = v.node().prerequisite();
		return (pre == null || data.rank(pre) > 0) && data.level(selected) >= v.node().requiredLevel();
	}

	/** Icono de cada nodo: el del rol para los propios y uno genérico para los comunes. */
	private net.minecraft.item.ItemStack nodeIcon(NodeView v) {
		return switch (v.kind()) {
			case CORE -> selected.iconStack();
			case SIGNATURE -> dev.aptitudes.role.NodeIcons.signature(selected);
			case ULTIMATE -> dev.aptitudes.role.NodeIcons.ultimate(selected);
			case NODE -> dev.aptitudes.role.NodeIcons.of(v.node());
		};
	}

	/** Nombre de cada senda encima de su columna (letra pequeña), en su color. */
	private void renderPathLabels(DrawContext ctx, RoleSyncPayload data) {
		int y = gridY() + ROW_H * 3 - 9;
		for (int path = 1; path <= 3; path++) {
			Text name = UiStyle.fit(textRenderer, Text.translatable(RoleTree.pathKey(selected, path)), 72);
			int cx = COL_X[2 + path] + 12;
			boolean locked = RoleTree.pathLocked(data::rank, selected, path);
			boolean started = RoleTree.pathStarted(data::rank, selected, path);
			int color = locked ? UiStyle.DOWN : started ? PATH_COLORS[path - 1] : (PATH_COLORS[path - 1] & 0xFFFFFF) | 0xA0000000;
			int w = (int) (textRenderer.getWidth(name) * 0.75f);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(cx - w / 2f, y, 0);
			ctx.getMatrices().scale(0.75f, 0.75f, 1f);
			ctx.drawText(textRenderer, name, 0, 0, color, false);
			ctx.getMatrices().pop();
		}
	}

	private boolean lit(RoleSyncPayload data, NodeView v) {
		return switch (v.kind()) {
			case CORE -> true;
			case SIGNATURE -> data.tier(selected) >= Evolution.MASTER;
			case ULTIMATE -> data.tier(selected) >= Evolution.ULTIMATE;
			case NODE -> data.rank(v.node().id()) > 0;
		};
	}

	private Text nodeName(NodeView v) {
		return switch (v.kind()) {
			case CORE -> Text.translatable(selected.translationKey());
			case SIGNATURE -> Text.translatable(Evolution.signatureKey(selected));
			case ULTIMATE -> Text.translatable(Evolution.ultimateKey(selected));
			case NODE -> Text.translatable(v.node().translationKey());
		};
	}

	private Text nodeType(NodeView v) {
		if (v.kind() != NodeKind.NODE) {
			return Text.translatable(switch (v.kind()) {
				case SIGNATURE -> "screen.aptitudes.node.signature";
				case ULTIMATE -> "screen.aptitudes.node.ultimate";
				default -> "screen.aptitudes.node.core";
			});
		}
		RoleNode node = v.node();
		List<RoleNode> tree = RoleTree.nodes(selected);
		int index = tree.indexOf(node);
		// Los 5 últimos de la lista: mecánica, reactiva 1, reactiva 2 y las activas C/D.
		// Orden fijo de RoleTree: 9 clásicos, 9 de senda, mecánica (18), reactivas (19, 20), activas C/D, legendario.
		if (index == 18) return Text.translatable("screen.aptitudes.node.mechanic");
		if (index == 19 || index == 20) return Text.translatable("screen.aptitudes.node.reactive");
		return Text.translatable(switch (node.kind()) {
			case PASSIVE -> "screen.aptitudes.roles.passive";
			case ACTIVE -> "screen.aptitudes.roles.active";
			case STAT -> "screen.aptitudes.node.stat";
			case PATH -> "screen.aptitudes.node.path";
			case HASTE, EFFICIENCY, MASTERY -> "screen.aptitudes.node.upgrade";
			case LEGENDARY -> "screen.aptitudes.node.legendary";
		});
	}

	/** Ficha del nodo seleccionado, bajo el árbol. */
	private void renderNodeInfo(DrawContext ctx, RoleSyncPayload data) {
		NodeView v = nodeView(selectedNode);
		if (v == null) return;
		int x = canvasX() + 8;
		int y = infoTop() + 4;
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
				Text rankText = Text.translatable("screen.aptitudes.node.rank", rank, node.maxRank(),
						data.tier(selected));
				Text req = rank >= node.maxRank() ? Text.empty()
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
