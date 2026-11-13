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

/** Atlas de roles (J). Las pestañas solo organizan los talentos existentes. */
public class RolesScreen extends Screen {
	private int W;
	private int H;
	private static final int LIST_W = 124;
	private static final int ROLE_ROW_H = 18;
	private static final int INSPECTOR_W = 174;
	private static final int INSPECTOR_H = 116;
	private static final int FOOTER_H = 34;
	private static final int CANVAS_TOP = 50;

	private enum NodeKind { CORE, NODE, SIGNATURE, ULTIMATE, WEAPON_MASTER, WEAPON_ULTIMATE }

	private enum TalentTab {
		TALENTS("screen.aptitudes.tree.tab_talents", Evolution.BASE, NodeKind.CORE, 242),
		PATHS("screen.aptitudes.paths.button", Evolution.BASE, NodeKind.CORE, 0),
		MASTER("screen.aptitudes.tree.tier_master", Evolution.MASTER, NodeKind.SIGNATURE, 140),
		ULTIMATE("screen.aptitudes.tree.tier_ultimate", Evolution.ULTIMATE, NodeKind.ULTIMATE, 140);

		final String key;
		final int tier;
		final NodeKind first;
		final int contentHeight;

		TalentTab(String key, int tier, NodeKind first, int contentHeight) {
			this.key = key;
			this.tier = tier;
			this.first = first;
			this.contentHeight = contentHeight;
		}
	}

	private record NodeView(NodeKind kind, @Nullable RoleNode node, int x, int y) {
		String key() {
			return node != null ? node.id() : kind.name();
		}
	}

	// Tres carriles del atlas base. Las sendas tienen su propio grafo en PathsScreen.
	private static final int[] COL_X = {20, 64, 108, 132, 156, 180, 204, 248, 292};
	private static final int FIRST_TALENT_Y = 48;
	private static final int CONTENT_W = 336;
	/** Maestro y Definitivo: disposición compacta (arriba la firma y el arma, abajo las mejoras) centrada en el lienzo. */
	private static final int EVO_TOP_Y = 14;
	private static final int EVO_LANE_Y = 58;
	private static final int EVO_ROW_Y = 82;
	private static final int EVO_H = 116;
	private static final int EVO_PITCH = 44;
	private int contentW = CONTENT_W;

	private int left;
	private int top;
	/**
	 * Desplazamiento vertical de la ventana cuando no cabe (p. ej., escala de interfaz 4 en 1080p: 270 px
	 * útiles frente a 322 de ventana). 0 si cabe. Se cambia con la rueda fuera del lienzo o RePág/AvPág.
	 */
	private int pageScroll;
	/** true mientras se reconstruye la pantalla por desplazamiento: conserva zoom y posición del árbol. */
	private boolean keepView;
	private RoleCategory category = RoleCategory.MARTIAL;
	private Role selected = Role.BERSERKER;
	private RoleAtlasTheme theme;
	/** Clave de la casilla seleccionada: id del nodo o nombre de {@link NodeKind}. */
	private String selectedNode = NodeKind.CORE.name();
	private boolean extraView;
	private TalentTab tab = TalentTab.TALENTS;
	private List<NodeView> visibleNodes = List.of();

	private ButtonWidget attributesButton;
	private ButtonWidget doneButton;
	private ButtonWidget primaryButton;
	private ButtonWidget secondaryButton;
	private ButtonWidget evolveButton;
	private ButtonWidget upgradeButton;
	private ButtonWidget extraButton;
	private ButtonWidget equip1Button;
	private ButtonWidget claimButton;
	private ButtonWidget equip2Button;

	public RolesScreen() {
		super(Text.translatable("screen.aptitudes.roles.title"));
		RoleSyncPayload data = ClientRoleState.data();
		if (data != null && data.primaryRole() != null) {
			selected = data.primaryRole();
			category = selected.category();
		}
		theme = RoleAtlasTheme.of(selected);
	}

	private int canvasX() { return left + LIST_W + 16; }
	private int canvasW() { return W - LIST_W - 24; }
	private boolean sideInspector() { return W >= 610; }
	private int treeW() { return canvasW() - (sideInspector() ? INSPECTOR_W + 6 : 0); }
	private int infoX() { return sideInspector() ? canvasX() + treeW() + 6 : canvasX(); }
	private int infoY() { return sideInspector() ? top + CANVAS_TOP : infoBottom() - INSPECTOR_H; }
	private int infoW() { return sideInspector() ? INSPECTOR_W : canvasW(); }
	private int infoBottom() { return top + H - FOOTER_H; }

	/** v24: último rango visto de cada nodo y momento en que subió (animación de «aprendido»). */
	private static final java.util.Map<String, Integer> LAST_RANK = new java.util.HashMap<>();
	private static final java.util.Map<String, Long> LEARNED_AT = new java.util.HashMap<>();

	// Vista del árbol: desplazamiento (arrastrar) y zoom (rueda).
	private double panX;
	private double panY;
	private float zoom = 1.0f;
	private boolean dragging;

	private int viewX1() { return canvasX() + 1; }
	private int viewY1() { return top + CANVAS_TOP + 1; }
	private int viewX2() { return canvasX() + treeW() - 1; }
	private int viewY2() { return (sideInspector() ? infoBottom() : infoY() - 4) - 13; }

	private boolean inView(double mx, double my) {
		return mx >= viewX1() && mx < viewX2() && my >= viewY1() && my < viewY2();
	}

	private double toLocalX(double mx) { return (mx - viewX1() - panX) / zoom; }
	private double toLocalY(double my) { return (my - viewY1() - panY) / zoom; }

	/** Ajusta el zoom para que quepa el árbol entero y lo centra. */
	private void resetView() {
		int vw = viewX2() - viewX1();
		int vh = viewY2() - viewY1();
		int ch = contentH();
		float fit = Math.min(vw / (float) contentW, vh / (float) ch);
		// Por encima de 1× el zoom avanza en medios pasos para que el píxel quede nítido; por debajo, ajuste continuo.
		zoom = fit >= 1f ? MathHelper.clamp((float) Math.floor(fit * 2) / 2f, 1f, 2.5f) : Math.max(0.5f, fit);
		panX = (vw - contentW * zoom) / 2.0;
		panY = Math.max(0, (vh - ch * zoom) / 2.0);
	}

	private int contentH() {
		return tab == TalentTab.TALENTS ? tab.contentHeight : EVO_H;
	}

	@Override
	protected void init() {
		int previousW = W, previousH = H;
		W = MathHelper.clamp(width - 16, 344, 760);
		H = MathHelper.clamp(height - 16, sideInspector() ? 306 : 350, 460);
		left = (width - W) / 2;
		pageScroll = MathHelper.clamp(pageScroll, 0, maxPageScroll());
		top = maxPageScroll() > 0 ? 8 - pageScroll : (height - H) / 2;
		int lx = left + 8;
		int actionX = infoX() + infoW() - 78;
		int actionY = infoBottom() - 22;
		TalentTab[] options = TalentTab.values();
		for (int i = 0; i < options.length; i++) {
			TalentTab option = options[i];
			int start = i * canvasW() / options.length;
			int end = (i + 1) * canvasW() / options.length;
			Text label = Text.translatable(option.key);
			var button = new RoleAtlasTheme.Tab(canvasX() + start, top + 29, end - start - 4,
					UiStyle.fit(textRenderer, label, end - start - 8), b -> openTab(option),
					() -> !extraView && option == tab, () -> theme.accent());
			button.setTooltip(Tooltip.of(label));
			addDrawableChild(button);
		}
		primaryButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.set_primary"),
				b -> send(RoleActionPayload.SET_PRIMARY, "")).dimensions(lx, top + 182, LIST_W, 16).build());
		secondaryButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.set_secondary"),
				b -> send(RoleActionPayload.SET_SECONDARY, "")).dimensions(lx, top + 200, LIST_W, 16).build());
		evolveButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.evolve"),
				b -> send(RoleActionPayload.EVOLVE, "")).dimensions(lx, top + 218, LIST_W, 16).build());
		if (!keepView || W != previousW || H != previousH) {
			visibleNodes = buildNodes();
			resetView();
		}
		keepView = false;
		upgradeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.roles.upgrade"), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.UPGRADE_NODE, v.node().id());
		}).dimensions(actionX, actionY, 70, 16).build());
		claimButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.weapon.claim"), b -> {
			NodeView v = nodeView(selectedNode);
			if (v == null) return;
			String tier = v.kind() == NodeKind.WEAPON_MASTER ? "1" : "2";
			ClientPlayNetworking.send(new RoleActionPayload(RoleActionPayload.CLAIM_WEAPON, selected.ordinal(), tier));
		}).dimensions(actionX, actionY, 70, 16).build());
		equip1Button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.equip", 1), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.EQUIP_1, v.node().id());
		}).dimensions(actionX, actionY - 18, 34, 16).build());
		equip2Button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.equip", 2), b -> {
			NodeView v = nodeView(selectedNode);
			if (v != null && v.node() != null) send(RoleActionPayload.EQUIP_2, v.node().id());
		}).dimensions(actionX + 36, actionY - 18, 34, 16).build());
		extraButton = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
			extraView = !extraView;
			updateButtons();
		}).dimensions(lx, top + 236, LIST_W, 16).build());

		int footer = top + H - 26;
		attributesButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.tab.attributes"),
				b -> client.setScreen(new AptitudesScreen())).dimensions(0, footer, 100, 18).build());
		doneButton = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(0, footer, 100, 18).build());
		updateButtons();
	}

	private void openTab(TalentTab option) {
		if (!extraView && option == tab) return;
		extraView = false;
		if (option == TalentTab.PATHS) {
			keepView = true;
			client.setScreen(TreeScreen.open(this, selected));
			return;
		}
		tab = option;
		selectTabStart();
	}

	private void selectTabStart() {
		visibleNodes = buildNodes();
		selectedNode = tab.first.name();
		dragging = false;
		resetView();
		updateButtons();
	}

	/** Cuánto se puede desplazar la ventana (0 si cabe con 8 px de margen arriba y abajo). */
	private int maxPageScroll() {
		return Math.max(0, H + 16 - height);
	}

	/** Desplaza la ventana y recoloca los botones sin perder el zoom ni la posición del árbol. */
	private boolean scrollPage(int amount) {
		int target = MathHelper.clamp(pageScroll + amount, 0, maxPageScroll());
		if (target == pageScroll) return false;
		pageScroll = target;
		keepView = true;
		clearAndInit();
		return true;
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
		attributesButton.setX(width / 2 - 104);
		doneButton.setX(width / 2 + 4);
		primaryButton.active = ready && data.primaryRole() != selected;
		secondaryButton.active = ready && data.secondaryRole() != selected;
		updateEvolveButton(data);

		NodeView v = nodeView(selectedNode);
		upgradeButton.visible = !extraView && v != null && v.node() != null;
		upgradeButton.active = ready && v != null && v.node() != null && canUpgrade(data, v.node());
		Text upgrade = Text.translatable(v != null && v.node() != null && ready && data.rank(v.node().id()) == 0
				? "screen.aptitudes.paths.learn" : "screen.aptitudes.roles.upgrade");
		upgradeButton.setMessage(UiStyle.fit(textRenderer, upgrade, 62));
		upgradeButton.setTooltip(Tooltip.of(upgrade));
		boolean weapon = !extraView && v != null && (v.kind() == NodeKind.WEAPON_MASTER || v.kind() == NodeKind.WEAPON_ULTIMATE);
		claimButton.visible = weapon;
		claimButton.active = weapon && ready && lit(data, v)
				&& (selected == data.primaryRole() || selected == data.secondaryRole());
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
		return active && rank < node.maxRank() && data.tier(role) >= node.requiredTier()
				&& data.level(role) >= node.levelForRank(rank + 1)
				&& RoleTree.prerequisiteMet(data::rank, node)
				&& availablePoints(data, role) > 0
				&& !pathLocked(data, node)
				&& legendBlock(node) == null;
	}

	/**
	 * Legendarias exclusivas: motivo por el que no se puede aprender (null si se puede). Solo es una
	 * ayuda visual con los datos sincronizados; el servidor vuelve a validar al mejorar.
	 */
	@Nullable
	private static Text legendBlock(RoleNode node) {
		if (node.kind() != RoleNode.Kind.LEGENDARY) return null;
		dev.aptitudes.network.UniqueSyncPayload uniques = ClientExtraState.uniques();
		if (uniques == null || !uniques.exclusive()) return null;
		dev.aptitudes.unique.UniqueSkill skill = dev.aptitudes.unique.UniqueSkill.forRole(node.role());
		if (skill == null) return null;
		int i = skill.ordinal();
		String holder = i < uniques.holders().length ? uniques.holders()[i] : "";
		var player = net.minecraft.client.MinecraftClient.getInstance().player;
		String self = player != null ? player.getGameProfile().getName() : "";
		if (!holder.isEmpty() && !holder.equals(self)) {
			return Text.translatable("screen.aptitudes.node.legend_taken", holder);
		}
		if (uniques.ownSkill() >= 0 && uniques.ownSkill() != i) {
			return Text.translatable("aptitudes.role.upgrade.legend_limit");
		}
		return null;
	}

	/** v46: sendas exclusivas (y una sola rama de la bifurcación): el nodo queda bloqueado. */
	private static boolean pathLocked(RoleSyncPayload data, @Nullable RoleNode node) {
		return node != null && RoleTree.nodeLocked(data::rank, node);
	}

	private static int availablePoints(RoleSyncPayload data, Role role) {
		return ClientRoleState.availablePoints(data, role);
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

	private List<NodeView> buildNodes() {
		List<NodeView> out = new ArrayList<>();
		if (tab == TalentTab.TALENTS) {
			contentW = CONTENT_W;
			out.add(new NodeView(NodeKind.CORE, null, COL_X[4], 6));
			for (RoleNode node : RoleTree.nodes(selected)) {
				RoleTree.GridPos pos = RoleTree.position(node.id());
				if (pos == null || node.requiredTier() != tab.tier || node.kind() == RoleNode.Kind.PATH) continue;
				out.add(new NodeView(NodeKind.NODE, node, COL_X[pos.col()], FIRST_TALENT_Y + (pos.row() - 1) * 32));
			}
			return out;
		}
		// Maestro y Definitivo: las mejoras se reparten a distancia fija y centradas, sin los huecos de la cuadrícula.
		List<RoleNode> upgrades = new ArrayList<>();
		for (RoleNode node : RoleTree.nodes(selected)) {
			RoleTree.GridPos pos = RoleTree.position(node.id());
			if (pos == null || node.requiredTier() != tab.tier || node.kind() == RoleNode.Kind.PATH) continue;
			upgrades.add(node);
		}
		upgrades.sort(java.util.Comparator.comparingInt((RoleNode n) -> RoleTree.position(n.id()).col())
				.thenComparingInt(n -> RoleTree.position(n.id()).row()));
		contentW = Math.max(220, upgrades.size() * EVO_PITCH + 40);
		int mid = contentW / 2 - 12;
		out.add(new NodeView(tab.first, null, mid - 36, EVO_TOP_Y));
		out.add(new NodeView(tab == TalentTab.MASTER ? NodeKind.WEAPON_MASTER : NodeKind.WEAPON_ULTIMATE, null, mid + 36, EVO_TOP_Y));
		int startX = (contentW - upgrades.size() * EVO_PITCH) / 2 + (EVO_PITCH - 24) / 2;
		for (int i = 0; i < upgrades.size(); i++) {
			out.add(new NodeView(NodeKind.NODE, upgrades.get(i), startX + i * EVO_PITCH, EVO_ROW_Y));
		}
		return out;
	}

	@Nullable
	private NodeView nodeView(String key) {
		for (NodeView v : visibleNodes) if (v.key().equals(key)) return v;
		return null;
	}

	// ------------------------------------------------------------ Entrada

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);
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
				for (NodeView v : visibleNodes) {
					if (lx >= v.x() && lx < v.x() + 24 && ly >= v.y() && ly < v.y() + 24) {
						if (tab != TalentTab.PATHS) {
							RoleSyncPayload clickData = ClientRoleState.data();
							boolean open = clickData != null && (lit(clickData, v) || available(clickData, v));
							playUi(open ? dev.aptitudes.sound.ModSounds.UI_METAL_SELECT : dev.aptitudes.sound.ModSounds.UI_METAL_DENIED);
						}
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
			zoom = MathHelper.clamp(zoom * (verticalAmount > 0 ? 1.15f : 1 / 1.15f), 0.5f, 2.4f);
			// El punto bajo el cursor se queda fijo.
			double ax = mouseX - viewX1();
			double ay = mouseY - viewY1();
			panX = ax - (ax - panX) * zoom / old;
			panY = ay - (ay - panY) * zoom / old;
			return true;
		}
		// Fuera del lienzo: desplaza la ventana si no cabe en la pantalla.
		if (verticalAmount != 0 && maxPageScroll() > 0 && scrollPage(verticalAmount > 0 ? -24 : 24)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_C && !extraView) {
			resetView();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN && scrollPage(80)) return true;
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP && scrollPage(-80)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private static void playUi(net.minecraft.sound.SoundEvent sound) {
		net.minecraft.client.MinecraftClient.getInstance().getSoundManager()
				.play(net.minecraft.client.sound.PositionedSoundInstance.master(sound, 1.0f));
	}

	private void selectRole(Role role) {
		selected = role;
		theme = RoleAtlasTheme.of(role);
		category = role.category();
		extraView = false;
		selectTabStart();
	}

	private boolean clickExtra(double mouseX, double mouseY) {
		int x0 = canvasX() + 8;
		int y0 = top + 78;
		int stride = (canvasW() - 12) / 3;
		if (selected == Role.ELEMENTALIST) {
			Element[] elements = Element.values();
			for (int i = 0; i < elements.length; i++) {
				int x = x0 + (i % 3) * stride;
				int y = y0 + (i / 3) * 34;
				if (mouseX >= x && mouseX < x + stride - 4 && mouseY >= y && mouseY < y + 30) {
					ClientPlayNetworking.send(new RoleActionPayload(RoleActionPayload.CHOOSE_ELEMENT, i, ""));
					return true;
				}
			}
		} else {
			MorphForm[] forms = MorphForm.values();
			int shown = 0;
			for (MorphForm form : forms) {
				if (!form.selectable()) continue;
				int x = x0 + (shown % 3) * stride;
				int y = y0 + (shown / 3) * 34;
				shown++;
				if (mouseX >= x && mouseX < x + stride - 4 && mouseY >= y && mouseY < y + 30) {
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
		theme.panel(ctx, canvasX(), top + 4, canvasW(), 22);
		if (extraView) {
			theme.panel(ctx, canvasX(), top + CANVAS_TOP, canvasW(), infoBottom() - top - CANVAS_TOP);
		} else {
			int treeBottom = viewY2() + 13;
			theme.panel(ctx, canvasX(), top + CANVAS_TOP, treeW(), treeBottom - top - CANVAS_TOP);
			theme.panel(ctx, infoX(), infoY(), infoW(), infoBottom() - infoY());

		}
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		// Barra de desplazamiento de la ventana (solo si no cabe en la pantalla).
		int maxScroll = maxPageScroll();
		if (maxScroll > 0) {
			int trackH = height - 16;
			int thumbH = Math.max(20, trackH * height / (H + 16));
			int thumbY = 8 + (trackH - thumbH) * pageScroll / maxScroll;
			ctx.fill(width - 5, 8, width - 2, 8 + trackH, 0x60000000);
			ctx.fill(width - 5, thumbY, width - 2, thumbY + thumbH, UiStyle.GOLD);
		}
		ctx.drawTextWithShadow(textRenderer, title.copy().formatted(Formatting.BOLD), left + 12, top + 9, UiStyle.GOLD);
		RoleSyncPayload data = ClientRoleState.data();
		if (data == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.waiting"),
					width / 2, top + H / 2, UiStyle.MUTED);
			return;
		}
		renderAtlasHeader(ctx, data);
		renderSynergy(ctx, data);
		List<Text> tooltip = renderLeft(ctx, data, mouseX, mouseY);
		if (extraView) {
			List<Text> t = selected == Role.ELEMENTALIST ? renderElements(ctx, data, mouseX, mouseY) : renderForms(ctx, data, mouseX, mouseY);
			if (t != null) tooltip = t;
		} else {
			List<Text> t = renderTree(ctx, data, mouseX, mouseY);
			if (t != null) tooltip = t;
			List<Text> detail = renderNodeInfo(ctx, data, mouseX, mouseY);
			if (detail != null) tooltip = detail;
		}
		if (tooltip != null) {
			int wrap = Math.min(248, width - 20);
			List<OrderedText> lines = new ArrayList<>();
			for (Text line : tooltip) lines.addAll(textRenderer.wrapLines(line, wrap));
			ctx.drawOrderedTooltip(textRenderer, lines, mouseX, mouseY);
		}
	}

	private void renderAtlasHeader(DrawContext ctx, RoleSyncPayload data) {
		int x = canvasX();
		int statsX = x + canvasW() - 112;
		NodeIconRenderer.drawRole(ctx, selected, NodeIconRenderer.Badge.CORE, x + 4, top + 7);
		UiStyle.label(ctx, textRenderer, Text.translatable(Evolution.titleKey(selected, data.tier(selected))),
				x + 25, top + 11, statsX - x - 31, theme.accent());
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.level",
				data.level(selected), data.rules().maxLevel()), statsX, top + 6, 108, UiStyle.TEXT);
		int points = availablePoints(data, selected);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.points", points),
				statsX, top + 16, 108, points > 0 ? UiStyle.GOOD : UiStyle.MUTED);
	}

	private void renderSynergy(DrawContext ctx, RoleSyncPayload data) {
		Role p = data.primaryRole();
		Role s = data.secondaryRole();
		if (p == null || s == null) return;
		boolean same = p.category() == s.category();
		Text text = Text.translatable(same ? "screen.aptitudes.synergy.specialization" : "screen.aptitudes.synergy.versatility");
		UiStyle.label(ctx, textRenderer, text, left + 8, top + 261, LIST_W, same ? UiStyle.GOOD : UiStyle.UP);
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
			if (role == selected) {
				theme.panel(ctx, left + 8, y, LIST_W, ROLE_ROW_H - 2);
				ctx.fill(left + 8, y, left + 10, y + ROLE_ROW_H - 2, theme.accent());
			} else {
				UiStyle.row(ctx, left + 8, y, LIST_W, ROLE_ROW_H - 2, i, false);
			}
			NodeIconRenderer.drawRole(ctx, role, NodeIconRenderer.Badge.CORE, left + 9, y);
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
		ctx.fill(left + 8, y + 36, left + 8 + (int) (LIST_W * fill), y + 39, theme.accent());
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
		// Si la ventana se puede desplazar, una pista corta que lo indica (la normal no cabría).
		Text hint = Text.translatable(maxPageScroll() > 0 ? "screen.aptitudes.tree.hint_scroll" : "screen.aptitudes.tree.hint");
		UiStyle.label(ctx, textRenderer, hint, viewX1() + 4, viewY2() + 2, treeW() - 10, UiStyle.MUTED);
		return tooltip;
	}

	private List<Text> renderTreeLocal(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		int tier = data.tier(selected);
		NodeView root = visibleNodes.getFirst();
		for (NodeView v : visibleNodes) {
			if (v.node() == null) continue;
			String pre = v.node().prerequisite();
			NodeView parent = pre != null ? nodeView(pre)
					: tab == TalentTab.TALENTS && v.y() == FIRST_TALENT_Y ? root : null;
			if (parent == null) continue;
			elbow(ctx, parent, v, lit(data, v) ? theme.accent() : available(data, v) ? 0xFF7A8098 : 0xFF566077);
		}

		renderLaneTitles(ctx, tier);
		List<Text> tooltip = null;
		long pulseNow = net.minecraft.util.Util.getMeasuringTimeMs();
		for (NodeView v : visibleNodes) {
			boolean hover = mouseX >= v.x() && mouseX < v.x() + 24 && mouseY >= v.y() && mouseY < v.y() + 24;
			UiStyle.Frame frame = switch (v.kind()) {
				case CORE -> UiStyle.Frame.CORE;
				case SIGNATURE -> UiStyle.Frame.SIGNATURE;
				case ULTIMATE, WEAPON_ULTIMATE -> UiStyle.Frame.ULTIMATE;
				case WEAPON_MASTER -> UiStyle.Frame.SIGNATURE;
				case NODE -> v.node().kind() == RoleNode.Kind.LEGENDARY ? UiStyle.Frame.UNIQUE
						: v.node().active() || v.node().kind() == RoleNode.Kind.HASTE
						|| v.node().kind() == RoleNode.Kind.EFFICIENCY || v.node().kind() == RoleNode.Kind.MASTERY
						? UiStyle.Frame.ACTIVE : UiStyle.Frame.PASSIVE;
			};
			boolean lit = lit(data, v);
			boolean available = !lit && available(data, v);
			// v24: si el rango subió desde la última vez, el nodo late en dorado y lanza una onda (0,9 s).
			long learnedFor = Long.MAX_VALUE;
			if (v.node() != null) {
				int rankNow = data.rank(v.node().id());
				Integer before = LAST_RANK.put(v.node().id(), rankNow);
				long nowMs = net.minecraft.util.Util.getMeasuringTimeMs();
				if (before != null && rankNow > before) {
					LEARNED_AT.put(v.node().id(), nowMs);
					if (tab != TalentTab.PATHS) playUi(dev.aptitudes.sound.ModSounds.UI_METAL_UNLOCK);
				}
				Long at = LEARNED_AT.get(v.node().id());
				if (at != null) learnedFor = nowMs - at;
			}
			if (learnedFor < 900) {
				float t = learnedFor / 900f;
				int grow = (int) (t * 12);
				int a = (int) (230 * (1f - t));
				ctx.drawBorder(v.x() - grow, v.y() - grow, 24 + grow * 2, 24 + grow * 2, (a << 24) | 0xE6C36A);
				ctx.fill(v.x() - 2, v.y() - 2, v.x() + 26, v.y() + 26, ((int) (120 * (1f - t)) << 24) | 0xFFF3B0);
			}
			if (tab != TalentTab.PATHS && hover) {
				ctx.fill(v.x() - 4, v.y() - 4, v.x() + 28, v.y() + 28, 0x30FFD56A);
				ctx.fill(v.x() - 2, v.y() - 2, v.x() + 26, v.y() + 26, 0x50FFD56A);
				ctx.drawBorder(v.x() - 2, v.y() - 2, 28, 28, 0xDDFFE9A0);
			}
			if (tab != TalentTab.PATHS && available && !hover) {
				int pulse = (int) (16 + 14 * (0.5 + 0.5 * Math.sin(pulseNow / 450.0)));
				ctx.drawBorder(v.x() - 1, v.y() - 1, 26, 26, (pulse << 24) | 0xF0C060);
			}
			UiStyle.frame(ctx, v.x(), v.y(), frame, lit, hover);
			if (v.key().equals(selectedNode)) {
				ctx.drawBorder(v.x() - 3, v.y() - 3, 30, 30, theme.accent());
			}
			drawNodeIcon(ctx, v);
			if (!lit && !available) ctx.fill(v.x() + 2, v.y() + 2, v.x() + 22, v.y() + 22, 0x88101018);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(0, 0, 200);
			if (!lit && !available) {
				ctx.fill(v.x() - 2, v.y() - 2, v.x() + 9, v.y() + 11, 0xFF141820);
				UiStyle.lockIcon(ctx, v.x(), v.y() - 1);
			} else if (available) {
				ctx.fill(v.x(), v.y(), v.x() + 4, v.y() + 4, theme.accent());
			}
			if (v.node() != null) {
				int rank = data.rank(v.node().id());
				if (rank > 0) {
					String badge = Integer.toString(rank);
					ctx.fill(v.x() + 13, v.y() + 15, v.x() + 24, v.y() + 25, 0xFF141820);
					ctx.drawText(textRenderer, badge, v.x() + 21 - textRenderer.getWidth(badge), v.y() + 16,
							rank >= v.node().maxRank() ? UiStyle.GOLD : UiStyle.TEXT, false);
				}
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
			if (hover) tooltip = nodeDetails(data, v);
		}
		return tooltip;
	}

	private void renderLaneTitles(DrawContext ctx, int tier) {
		if (tab == TalentTab.TALENTS) {
			laneTitle(ctx, "screen.aptitudes.tree.passives", 12, 128, 15, UiStyle.MUTED);
			laneTitle(ctx, "screen.aptitudes.tree.actives", 212, 324, 15, UiStyle.MUTED);
		} else {
			boolean master = tab == TalentTab.MASTER;
			boolean unlocked = tier >= tab.tier;
			String key = master
					? (unlocked ? "screen.aptitudes.tree.row_master" : "screen.aptitudes.tree.requires_master")
					: (unlocked ? "screen.aptitudes.tree.row_ultimate" : "screen.aptitudes.tree.requires_ultimate");
			ctx.fill(8, EVO_LANE_Y + 5, contentW - 8, EVO_LANE_Y + 6, UiStyle.BORDER);
			laneTitle(ctx, key, 12, contentW - 12, EVO_LANE_Y, unlocked ? theme.accent() : UiStyle.MUTED);
		}
	}

	private void laneTitle(DrawContext ctx, String key, int x1, int x2, int y, int color) {
		Text title = UiStyle.fit(textRenderer, Text.translatable(key), x2 - x1 - 6);
		int tw = textRenderer.getWidth(title);
		int x = (x1 + x2 - tw) / 2;
		ctx.fill(x - 3, y - 1, x + tw + 3, y + 9, theme.slate());
		ctx.drawText(textRenderer, title, x, y, color, false);
	}

	/** Conector en ángulo recto: baja desde el padre, cruza en horizontal a media altura y baja al hijo. */
	private void elbow(DrawContext ctx, NodeView parent, NodeView child, int color) {
		int px = parent.x() + 11;
		int cx = child.x() + 11;
		int py = parent.y() + 24;
		int cy = child.y();
		// El tramo horizontal queda entre filas para no atravesar otros nodos.
		int mid = Math.min(py + 5, cy - 4);
		ctx.fill(px, py, px + 1, mid + 1, color);
		ctx.fill(Math.min(px, cx), mid, Math.max(px, cx) + 1, mid + 1, color);
		ctx.fill(cx, mid, cx + 1, cy, color);
	}

	private boolean available(RoleSyncPayload data, NodeView v) {
		return v.node() != null && canUpgrade(data, v.node());
	}

	private void drawNodeIcon(DrawContext ctx, NodeView v) {
		int x = v.x() + 4;
		int y = v.y() + 4;
		switch (v.kind()) {
			case CORE -> NodeIconRenderer.drawRole(ctx, selected, NodeIconRenderer.Badge.CORE, x, y);
			case SIGNATURE -> NodeIconRenderer.drawRole(ctx, selected, NodeIconRenderer.Badge.SIGNATURE, x, y);
			case ULTIMATE -> NodeIconRenderer.drawRole(ctx, selected, NodeIconRenderer.Badge.ULTIMATE, x, y);
			case WEAPON_MASTER, WEAPON_ULTIMATE -> ctx.drawItem(new net.minecraft.item.ItemStack(
					dev.aptitudes.item.weapon.RoleWeapons.get(selected, v.kind() == NodeKind.WEAPON_MASTER ? 1 : 2)), x, y);
			case NODE -> NodeIconRenderer.draw(ctx, v.node(), x, y);
		}
	}

	private boolean lit(RoleSyncPayload data, NodeView v) {
		return switch (v.kind()) {
			case CORE -> true;
			case SIGNATURE -> data.tier(selected) >= Evolution.MASTER;
			case ULTIMATE, WEAPON_ULTIMATE -> data.tier(selected) >= Evolution.ULTIMATE;
			case WEAPON_MASTER -> data.tier(selected) >= Evolution.MASTER;
			case NODE -> data.rank(v.node().id()) > 0;
		};
	}

	private Text nodeName(NodeView v) {
		return switch (v.kind()) {
			case CORE -> Text.translatable(selected.translationKey());
			case SIGNATURE -> Text.translatable(Evolution.signatureKey(selected));
			case ULTIMATE -> Text.translatable(Evolution.ultimateKey(selected));
			case WEAPON_MASTER -> dev.aptitudes.item.weapon.RoleWeapons.get(selected, 1).getName();
			case WEAPON_ULTIMATE -> dev.aptitudes.item.weapon.RoleWeapons.get(selected, 2).getName();
			case NODE -> Text.translatable(v.node().translationKey());
		};
	}

	private Text nodeType(NodeView v) {
		if (v.kind() != NodeKind.NODE) {
			return Text.translatable(switch (v.kind()) {
				case SIGNATURE -> "screen.aptitudes.node.signature";
				case ULTIMATE -> "screen.aptitudes.node.ultimate";
				case WEAPON_MASTER, WEAPON_ULTIMATE -> "screen.aptitudes.node.weapon";
				default -> "screen.aptitudes.node.core";
			});
		}
		RoleNode node = v.node();
		if (node.evolved()) { // v23: «Maestro · Activa», «Definitivo · Pasiva»…
			return Text.translatable("screen.aptitudes.node.evolved",
					Text.translatable(node.requiredTier() >= Evolution.ULTIMATE ? "screen.aptitudes.tree.tier_ultimate"
							: "screen.aptitudes.tree.tier_master"),
					Text.translatable(node.active() ? "screen.aptitudes.roles.active" : "screen.aptitudes.roles.passive"));
		}
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

	private Text nodeDescription(NodeView v) {
		return switch (v.kind()) {
			case CORE -> Text.translatable(selected.descriptionKey());
			case SIGNATURE -> Text.translatable(Evolution.signatureKey(selected) + ".desc");
			case ULTIMATE -> Text.translatable(Evolution.ultimateKey(selected) + ".desc");
			case WEAPON_MASTER, WEAPON_ULTIMATE -> Text.translatable(dev.aptitudes.item.weapon.RoleWeapons.melee(selected)
					? "screen.aptitudes.node.weapon_blade" : "screen.aptitudes.node.weapon_focus");
			case NODE -> Text.translatable(v.node().descriptionKey());
		};
	}

	private List<Text> nodeDetails(RoleSyncPayload data, NodeView v) {
		List<Text> lines = new ArrayList<>();
		lines.add(nodeName(v).copy().withColor(theme.accent() & 0xFFFFFF));
		lines.add(nodeType(v).copy().formatted(Formatting.GRAY));
		RoleNode node = v.node();
		if (node != null) {
			int rank = data.rank(node.id());
			int bonus = rank > 0 && (!node.evolved() || node.active()) ? data.tier(selected) : 0;
			lines.add(bonus > 0 ? Text.translatable("screen.aptitudes.node.rank", rank, node.maxRank(), bonus)
					: Text.translatable("screen.aptitudes.tree.rank", rank, node.maxRank()));
			RoleNode pre = node.prerequisite() == null ? null : RoleTree.get(node.prerequisite());
			if (pre != null) {
				lines.add(check(RoleTree.prerequisiteMet(data::rank, node),
						Text.translatable("screen.aptitudes.node.needs", Text.translatable(pre.translationKey()))));
			}
			if (rank < node.maxRank()) {
				int nextLevel = node.levelForRank(rank + 1);
				lines.add(check(data.level(selected) >= nextLevel,
						Text.translatable("screen.aptitudes.roles.requires", nextLevel)));
				if (data.tier(selected) < node.requiredTier()) {
					lines.add(Text.translatable(node.requiredTier() == Evolution.MASTER
							? "screen.aptitudes.tree.requires_master" : "screen.aptitudes.tree.requires_ultimate")
							.formatted(Formatting.RED));
				}
				if (data.primaryRole() != selected && data.secondaryRole() != selected) {
					lines.add(check(false, Text.translatable("screen.aptitudes.evolve.active")));
				}
				if (availablePoints(data, selected) <= 0) {
					lines.add(check(false, Text.translatable("screen.aptitudes.paths.points", 0)));
				}
				Text blocked = legendBlock(node);
				if (blocked != null) lines.add(blocked.copy().formatted(Formatting.RED));
			}
			if (node.active()) lines.add(Text.translatable("screen.aptitudes.node.cost", node.energyCost(), node.cooldownSecs()));
		} else if (v.kind() != NodeKind.CORE) {
			lines.add(Text.translatable(switch (v.kind()) {
				case SIGNATURE -> "screen.aptitudes.node.signature_req";
				case ULTIMATE -> "screen.aptitudes.node.ultimate_req";
				case WEAPON_MASTER -> "screen.aptitudes.node.weapon_master_req";
				case WEAPON_ULTIMATE -> "screen.aptitudes.node.weapon_ultimate_req";
				default -> throw new IllegalStateException();
			}).formatted(lit(data, v) ? Formatting.GRAY : Formatting.RED));
		}
		lines.add(nodeDescription(v));
		return lines;
	}

	/** El texto completo queda disponible al pasar por la ficha o por el nodo. */
	private List<Text> renderNodeInfo(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		NodeView v = nodeView(selectedNode);
		if (v == null) return null;
		int x = infoX() + 6;
		int y = infoY() + 6;
		int w = infoW() - 12;
		UiStyle.frame(ctx, x, y, UiStyle.Frame.PASSIVE, lit(data, v), false);
		drawNodeIcon(ctx, new NodeView(v.kind(), v.node(), x, y));
		List<Text> lines = nodeDetails(data, v);
		UiStyle.label(ctx, textRenderer, lines.get(0), x + 30, y + 2, w - 30, theme.accent());
		UiStyle.label(ctx, textRenderer, lines.get(1), x + 30, y + 13, w - 30, UiStyle.MUTED);
		ctx.fill(x, y + 29, x + w, y + 30, UiStyle.BORDER);
		int bodyW = sideInspector() ? w : w - 76;
		int bodyBottom = infoBottom() - (sideInspector() ? 42 : 4);
		int lineY = y + 35;
		for (int i = 2; i < lines.size() - 1 && lineY <= bodyBottom - 30; i++) {
			UiStyle.label(ctx, textRenderer, lines.get(i), x, lineY, bodyW, UiStyle.TEXT);
			lineY += 10;
		}
		for (OrderedText line : textRenderer.wrapLines(lines.getLast(), bodyW)) {
			if (lineY > bodyBottom - 9) break;
			ctx.drawText(textRenderer, line, x, lineY, UiStyle.TEXT, false);
			lineY += 10;
		}
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < bodyBottom
				&& (sideInspector() || mouseX < x + bodyW || mouseY < infoBottom() - 42) ? lines : null;
	}

	private List<Text> renderElements(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		int x0 = canvasX() + 8;
		int y0 = top + 78;
		int stride = (canvasW() - 12) / 3;
		int cardW = stride - 4;
		List<Element> mastered = data.elementList();
		Element active = data.activeElementValue();
		int allowed = Element.allowedCount(data.level(Role.ELEMENTALIST));
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.elements.header", mastered.size(), allowed,
				Element.MAX_MASTERED), x0, top + 59, 200);
		List<Text> tooltip = null;
		Element[] elements = Element.values();
		for (int i = 0; i < elements.length; i++) {
			Element e = elements[i];
			int x = x0 + (i % 3) * stride;
			int y = y0 + (i / 3) * 34;
			boolean own = mastered.contains(e);
			ctx.fill(x, y, x + stride - 4, y + 30, own ? 0x70000000 | (e.color() & 0xFFFFFF) : 0x50202433);
			ctx.drawBorder(x, y, cardW, 30, e == active ? 0xFFFFFFFF : own ? 0xFF000000 | e.color() : UiStyle.BORDER);
			UiStyle.label(ctx, textRenderer, Text.translatable(e.translationKey()), x + 6, y + 6, cardW - 12, 0xFF000000 | e.color());
			Text state = Text.translatable(e == active ? "screen.aptitudes.elements.active"
					: own ? "screen.aptitudes.elements.mastered" : "screen.aptitudes.elements.available");
			UiStyle.label(ctx, textRenderer, state, x + 6, y + 18, cardW - 12, UiStyle.MUTED);
			if (mouseX >= x && mouseX < x + stride - 4 && mouseY >= y && mouseY < y + 30) {
				List<Text> lines = new ArrayList<>();
				lines.add(Text.translatable(e.translationKey()).withColor(e.color()));
				lines.add(Text.translatable(e.descriptionKey()).formatted(Formatting.GRAY));
				// Fusiones en las que participa este elemento
				for (dev.aptitudes.ability.ElementFusions f : dev.aptitudes.ability.ElementFusions.values()) {
					if (!f.involves(e)) continue;
					Element partner = f.first() == e ? f.second() : f.first();
					boolean on = f.unlocked(mastered);
					lines.add(Text.literal(on ? "✔ " : "✦ ").formatted(on ? Formatting.GREEN : Formatting.DARK_GRAY)
							.append(Text.translatable("screen.aptitudes.elements.fusion", Text.translatable(f.translationKey()),
									Text.translatable(partner.translationKey()).withColor(partner.color())))
							.formatted(on ? Formatting.WHITE : Formatting.GRAY));
					lines.add(Text.literal("   ").append(Text.translatable(f.descriptionKey())).formatted(Formatting.DARK_GRAY));
				}
				tooltip = lines;
			}
		}
		if (mastered.size() < Element.MAX_MASTERED) {
			Text hint = mastered.size() < allowed ? Text.translatable("screen.aptitudes.elements.choose")
					: Text.translatable("screen.aptitudes.elements.next", Element.UNLOCK_LEVELS[mastered.size()]);
			UiStyle.label(ctx, textRenderer, hint, x0, infoBottom() - 16, canvasW() - 16, UiStyle.UP);
		}
		return tooltip;
	}

	private List<Text> renderForms(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		FormSyncPayload forms = ClientExtraState.forms();
		int x0 = canvasX() + 8;
		int y0 = top + 78;
		int stride = (canvasW() - 12) / 3;
		int cardW = stride - 4;
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.forms.header"), x0, top + 59, 200);
		List<Text> tooltip = null;
		int shown = 0;
		for (MorphForm form : MorphForm.values()) {
			if (!form.selectable()) continue;
			int x = x0 + (shown % 3) * stride;
			int y = y0 + (shown / 3) * 34;
			shown++;
			boolean unlocked = forms != null && forms.unlocked(form.ordinal());
			boolean chosen = forms != null && forms.selected() == form.ordinal();
			ctx.fill(x, y, x + stride - 4, y + 30, unlocked ? 0x70303850 : 0x40202433);
			ctx.drawBorder(x, y, cardW, 30, chosen ? UiStyle.GOLD : UiStyle.BORDER);
			ctx.drawItem(form.iconStack(), x + 4, y + 7);
			UiStyle.label(ctx, textRenderer, Text.translatable(form.translationKey()), x + 24, y + 5, cardW - 28,
					unlocked ? UiStyle.TEXT : UiStyle.MUTED);
			Text progress;
			if (unlocked) {
				progress = Text.translatable(chosen ? "screen.aptitudes.forms.selected" : "screen.aptitudes.forms.unlocked");
			} else if (form.levelToUnlock() > 0) {
				progress = Text.translatable("screen.aptitudes.roles.requires", form.levelToUnlock());
			} else {
				int kills = forms == null || form.ordinal() >= forms.kills().length ? 0 : forms.kills()[form.ordinal()];
				progress = Text.translatable("screen.aptitudes.forms.kills", kills, form.killsToUnlock());
			}
			UiStyle.label(ctx, textRenderer, progress, x + 24, y + 17, cardW - 28, unlocked ? UiStyle.GOOD : UiStyle.MUTED);
			if (mouseX >= x && mouseX < x + stride - 4 && mouseY >= y && mouseY < y + 30) {
				tooltip = List.of(Text.translatable(form.translationKey()).formatted(Formatting.YELLOW),
						Text.translatable(form.descriptionKey()).formatted(Formatting.GRAY));
			}
		}
		return tooltip;
	}
}
