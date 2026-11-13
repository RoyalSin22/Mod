package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.logic.MasteryRules;
import dev.aptitudes.logic.TreeScreenLayout;
import dev.aptitudes.logic.TreeScreenLayout.Rect;
import dev.aptitudes.network.RoleActionPayload;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.PathRules.Step;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import dev.aptitudes.sync.ClientMastery;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla del árbol de habilidades (v66): las tres sendas del rol en columnas con sus siete escalones, cabecera con el
 * rol, nivel y experiencia, maestría, habilidad definitiva y ficha del escalón elegido. Sustituye a {@link PathsScreen}
 * con las piezas de la dirección de arte; esa pantalla sigue disponible si el paquete de recursos no las trae o si se
 * desactiva {@code newTreeScreen} en la configuración del cliente. La geometría está en {@link TreeScreenLayout}
 * (con pruebas). Solo muestra datos sincronizados: el servidor valida al aprender y al equipar.
 */
public class TreeScreen extends Screen {
	private static final int[] PATH_COLORS = {0xFFE05A47, 0xFFB8C2CC, 0xFF4DD6D0};
	private static final String[] HEADERS = {"senda_sangre", "senda_acero", "senda_trueno"};
	private static final int LINK_DIM = 0xFF3A3F55;
	/** Escalón de cada fila; la fila de S5 tiene las dos opciones de la bifurcación. */
	private static final Step[][] ROW_STEPS = {{Step.S1}, {Step.S2}, {Step.S3}, {Step.S4}, {Step.S5X, Step.S5Y}, {Step.S6},
			{Step.S7}};

	private final Screen parent;
	private final Role role;
	private final RoleAtlasTheme theme;
	private TreeScreenLayout layout;
	/** Senda elegida en pantalla (0-2); la numeración de {@link PathRules} empieza en 1. */
	private int selectedPath;
	@Nullable private Step selectedStep = Step.S1;
	private int scroll;
	private SpriteButton learnButton;
	private SpriteButton respecButton;
	private ButtonWidget equip1Button;
	private ButtonWidget equip2Button;

	/** Abre la pantalla nueva si el paquete de recursos la soporta y está activada; si no, la anterior. */
	public static Screen open(Screen parent, Role role) {
		boolean enabled = dev.aptitudes.client.ClientConfig.get().newTreeScreen;
		return enabled && TreeUi.available() ? new TreeScreen(parent, role) : new PathsScreen(parent, role);
	}

	public TreeScreen(Screen parent, Role role) {
		super(Text.translatable("screen.aptitudes.paths.title", Text.translatable(role.translationKey())));
		this.parent = parent;
		this.role = role;
		this.theme = RoleAtlasTheme.of(role);
		RoleSyncPayload data = ClientRoleState.data();
		int chosen = data == null ? 0 : PathRules.chosenPath(data::rank, role.key());
		this.selectedPath = chosen > 0 ? chosen - 1 : 0;
	}

	// ------------------------------------------------------------ Estado

	@Nullable
	private RoleNode nodeOf(int path, Step step) {
		return RoleTree.get(PathRules.nodeId(role.key(), path + 1, step));
	}

	@Nullable
	private RoleNode selectedNode() {
		return selectedStep == null ? null : nodeOf(selectedPath, selectedStep);
	}

	/** Misma comprobación que el servidor, con los datos sincronizados (solo para activar los botones). */
	private boolean canLearn(RoleSyncPayload data, RoleNode node) {
		boolean active = role == data.primaryRole() || role == data.secondaryRole();
		int rank = data.rank(node.id());
		return active && rank < node.maxRank()
				&& data.level(role) >= node.levelForRank(rank + 1)
				&& RoleTree.prerequisiteMet(data::rank, node)
				&& !RoleTree.nodeLocked(data::rank, node)
				&& ClientRoleState.availablePoints(data, role) > 0;
	}

	private boolean canEquip(RoleSyncPayload data, @Nullable RoleNode node) {
		return node != null && node.active() && data.rank(node.id()) > 0
				&& (role == data.primaryRole() || role == data.secondaryRole());
	}

	private String nodeState(RoleSyncPayload data, RoleNode node, Step step) {
		int rank = data.rank(node.id());
		if (rank > 0) return step.branch() ? "elegido" : "comprado";
		if (RoleTree.nodeLocked(data::rank, node)) return "descartado";
		return canLearn(data, node) ? "disponible" : "bloqueado";
	}

	// ------------------------------------------------------------ Inicio

	@Override
	protected void init() {
		scroll = MathHelper.clamp(scroll, 0, TreeScreenLayout.maxScroll(height));
		layout = TreeScreenLayout.place(width, height, scroll);
		learnButton = addDrawableChild(new SpriteButton(layout.primaryButton(),
				Text.translatable("screen.aptitudes.paths.learn"), "equipar", b -> learn()));
		respecButton = addDrawableChild(new SpriteButton(layout.respecButton(),
				Text.translatable("screen.aptitudes.respec.button"), "reiniciar", b -> respec()));
		addDrawableChild(new SpriteButton(layout.closeButton(), ScreenTexts.BACK, "cerrar", b -> close()));
		Rect d = layout.detail();
		int ex = d.x() + 9;
		int ey = d.bottom() - 27;
		equip1Button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.equip", 1),
				b -> equip(RoleActionPayload.EQUIP_1)).dimensions(ex, ey, 33, 16).build());
		equip2Button = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.equip", 2),
				b -> equip(RoleActionPayload.EQUIP_2)).dimensions(ex + 35, ey, 33, 16).build());
		updateButtons();
	}

	private void updateButtons() {
		RoleSyncPayload data = ClientRoleState.data();
		RoleNode node = selectedNode();
		boolean ready = data != null && node != null;
		learnButton.active = ready && canLearn(data, node);
		respecButton.active = data != null && respecState(data) == dev.aptitudes.role.RespecRules.Result.OK;
		boolean equippable = ready && canEquip(data, node);
		equip1Button.visible = equippable;
		equip2Button.visible = equippable;
	}

	@Override
	public void tick() {
		updateButtons(); // los rangos llegan del servidor tras aprender
	}

	// ------------------------------------------------------------ Reinicio de pago

	private int spentPoints(RoleSyncPayload data) {
		int spent = 0;
		for (RoleNode node : RoleTree.nodes(role)) spent += data.rank(node.id());
		return spent;
	}

	private dev.aptitudes.config.AptitudesConfig serverConfig() {
		return dev.aptitudes.client.ClientServerConfig.get();
	}

	private int respecCost(RoleSyncPayload data) {
		var c = serverConfig();
		return dev.aptitudes.role.RespecRules.cost(spentPoints(data), c.treeRespecLevelsBase, c.treeRespecLevelsPerPoint,
				c.treeRespecLevelsMax);
	}

	/** Misma comprobación que el servidor (solo para activar el botón y el tooltip). */
	private dev.aptitudes.role.RespecRules.Result respecState(RoleSyncPayload data) {
		boolean available = role == data.primaryRole() || role == data.secondaryRole();
		return dev.aptitudes.role.RespecRules.check(serverConfig().treeRespecEnabled, available, spentPoints(data),
				client.player == null ? 0 : client.player.experienceLevel, respecCost(data),
				client.player != null && client.player.isCreative());
	}

	private void respec() {
		RoleSyncPayload data = ClientRoleState.data();
		if (data == null || respecState(data) != dev.aptitudes.role.RespecRules.Result.OK) return;
		Text roleName = Text.translatable(role.translationKey());
		Text message = Text.translatable("screen.aptitudes.respec.confirm", roleName, spentPoints(data), respecCost(data));
		client.setScreen(new ConfirmScreen(ok -> {
			if (ok) send(RoleActionPayload.RESPEC_TREE, "");
			client.setScreen(this);
		}, Text.translatable("screen.aptitudes.respec.confirm_title"), message));
	}

	@Nullable
	private List<Text> respecTooltip(RoleSyncPayload data) {
		int cost = respecCost(data);
		return switch (respecState(data)) {
			case OK -> List.of(Text.translatable("screen.aptitudes.respec.cost", cost));
			case NOT_ENOUGH_LEVELS -> List.of(Text.translatable("screen.aptitudes.respec.missing", cost).formatted(Formatting.RED));
			case NOTHING_TO_RESET -> List.of(Text.translatable("screen.aptitudes.respec.nothing"));
			case DISABLED -> List.of(Text.translatable("aptitudes.respec.disabled"));
			case UNAVAILABLE -> List.of(Text.translatable("aptitudes.respec.unavailable"));
		};
	}

	// ------------------------------------------------------------ Acciones

	private static void playUi(net.minecraft.sound.SoundEvent sound) {
		net.minecraft.client.MinecraftClient.getInstance().getSoundManager()
				.play(net.minecraft.client.sound.PositionedSoundInstance.master(sound, 1.0f));
	}

	private void learn() {
		RoleSyncPayload data = ClientRoleState.data();
		RoleNode node = selectedNode();
		if (data == null || node == null || selectedStep == null || !canLearn(data, node)) return;
		playUi(dev.aptitudes.sound.ModSounds.UI_METAL_UNLOCK);
		Step step = selectedStep;
		// Confirmación: elegir senda (la primera vez en este rol) o elegir rama en la bifurcación es permanente.
		Text name = Text.translatable(node.translationKey());
		Text message = null;
		if (PathRules.pathsStarted(data::rank, role.key()) == 0) {
			message = Text.translatable("screen.aptitudes.paths.confirm_path", name,
					Text.translatable(RoleTree.pathKey(role, selectedPath + 1)));
		} else if (step.branch() && PathRules.chosenBranch(data::rank, role.key(), selectedPath + 1) == null) {
			message = Text.translatable("screen.aptitudes.paths.confirm_branch", name);
		}
		if (message == null) {
			send(RoleActionPayload.UPGRADE_NODE, node.id());
			return;
		}
		client.setScreen(new ConfirmScreen(ok -> {
			if (ok) send(RoleActionPayload.UPGRADE_NODE, node.id());
			client.setScreen(this);
		}, Text.translatable("screen.aptitudes.paths.confirm_title"), message));
	}

	private void equip(int action) {
		RoleNode node = selectedNode();
		RoleSyncPayload data = ClientRoleState.data();
		if (node != null && data != null && canEquip(data, node)) send(action, node.id());
	}

	private void send(int action, String nodeId) {
		ClientPlayNetworking.send(new RoleActionPayload(action, role.ordinal(), nodeId));
	}

	// ------------------------------------------------------------ Dibujo

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		TreeUi.panel9(ctx, "paneles/panel_principal_9slice.png", TreeUi.PANEL_MAIN_W, TreeUi.PANEL_MAIN_H,
				TreeUi.PANEL_MAIN_ORNAMENT, layout.window());
		TreeUi.panel9(ctx, "paneles/panel_detalle_9slice.png", TreeUi.PANEL_DETAIL_W, TreeUi.PANEL_DETAIL_H,
				TreeUi.PANEL_DETAIL_ORNAMENT, layout.detail());
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		RoleSyncPayload data = ClientRoleState.data();
		if (data == null) return;
		drawHeader(ctx, data);
		drawBanner(ctx, data);
		int chosen = PathRules.chosenPath(data::rank, role.key());
		List<OrderedText> tooltip = null;
		for (int row = 0; row < TreeScreenLayout.ROWS; row++) {
			Rect label = layout.label(row);
			TreeUi.sprite(ctx, "etiquetas/etiqueta_s" + (row + 1) + ".png", label.x(), label.y(), 24, 14);
		}
		RoleNode hovered = null;
		Step hoveredStep = null;
		int hoveredPath = 0;
		for (int path = 0; path < TreeScreenLayout.PATHS; path++) {
			drawColumnHeader(ctx, data, path, chosen);
			drawLinks(ctx, data, path);
			for (int row = 0; row < TreeScreenLayout.ROWS; row++) {
				for (int slot = 0; slot < TreeScreenLayout.slots(row); slot++) {
					Step step = ROW_STEPS[row][slot];
					RoleNode node = nodeOf(path, step);
					if (node == null) continue;
					Rect r = layout.node(path, row, slot);
					drawNode(ctx, data, node, step, path, r);
					if (r.contains(mouseX, mouseY)) {
						hovered = node;
						hoveredStep = step;
						hoveredPath = path;
					}
				}
			}
			if (chosen > 0 && chosen - 1 != path) {
				Rect top = layout.columnHeader(path);
				Rect last = layout.node(path, TreeScreenLayout.ROWS - 1, 0);
				ctx.fill(top.x(), top.bottom(), top.right(), last.bottom() + 2, 0x70101018); // senda bloqueada
			}
		}
		drawMastery(ctx, mouseX, mouseY);
		drawDefinitive(ctx, data, mouseX, mouseY);
		drawDetail(ctx, data);
		if (hovered != null) {
			List<OrderedText> lines = new ArrayList<>();
			for (Text line : stepDetails(data, hovered, hoveredPath, hoveredStep)) {
				lines.addAll(textRenderer.wrapLines(line, Math.min(248, width - 20)));
			}
			tooltip = lines;
		} else {
			tooltip = auxiliaryTooltip(data, mouseX, mouseY);
		}
		if (tooltip != null) ctx.drawOrderedTooltip(textRenderer, tooltip, mouseX, mouseY);
	}

	private void drawHeader(DrawContext ctx, RoleSyncPayload data) {
		Rect icon = layout.roleIcon();
		NodeIconRenderer.drawRoleLarge(ctx, role, icon.x(), icon.y());
		Rect box = layout.nameBox();
		Text name = Text.translatable(role.translationKey());
		ctx.getMatrices().push();
		ctx.getMatrices().translate(box.x(), box.y(), 0);
		ctx.getMatrices().scale(1.5f, 1.5f, 1f);
		ctx.drawText(textRenderer, name, 0, 1, theme.accent(), true);
		ctx.getMatrices().pop();
		int level = data.level(role);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.level", level, data.rules().maxLevel()),
				box.x(), box.y() + 15, box.w(), UiStyle.TEXT);
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.paths.points",
				Math.max(0, ClientRoleState.availablePoints(data, role))), box.x(), box.y() + 24, box.w(), UiStyle.GOLD);
		// Experiencia del rol hasta el siguiente nivel.
		Rect bar = layout.xpBar();
		ctx.fill(bar.x(), bar.y(), bar.right(), bar.bottom(), 0xFF14161F);
		boolean maxed = level >= data.rules().maxLevel();
		int needed = maxed ? 0 : Math.max(1, data.rules().xpToNext(level));
		float fraction = maxed ? 1f : MathHelper.clamp(data.xp(role) / (float) needed, 0f, 1f);
		int filled = Math.round((bar.w() - 2) * fraction);
		ctx.fill(bar.x() + 1, bar.y() + 1, bar.x() + 1 + filled, bar.bottom() - 1, 0xFFC8281F);
		ctx.fill(bar.x() + 1, bar.y() + 1, bar.x() + 1 + filled, bar.y() + 1 + (bar.h() - 2) / 2, 0x40FFFFFF);
		ctx.drawBorder(bar.x(), bar.y(), bar.w(), bar.h(), 0xFF9C7A35);
		if (!maxed) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(data.xp(role) + " / " + needed),
					bar.x() + bar.w() / 2, bar.y() + 3, UiStyle.TEXT);
		}
	}

	private void drawBanner(DrawContext ctx, RoleSyncPayload data) {
		Rect banner = layout.banner();
		int chosen = PathRules.chosenPath(data::rank, role.key());
		Text text = chosen == 0
				? Text.translatable("screen.aptitudes.paths.free")
				: Text.translatable("screen.aptitudes.paths.chosen").append(" · ")
						.append(Text.translatable(RoleTree.pathKey(role, chosen)));
		Text fitted = UiStyle.fit(textRenderer, text, banner.w());
		ctx.drawCenteredTextWithShadow(textRenderer, fitted, banner.x() + banner.w() / 2, banner.y() + 2,
				chosen == 0 ? UiStyle.GOLD : UiStyle.TEXT);
	}

	private void drawColumnHeader(DrawContext ctx, RoleSyncPayload data, int path, int chosen) {
		Rect h = layout.columnHeader(path);
		TreeUi.sprite(ctx, "cabeceras/" + HEADERS[path] + ".png", h.x(), h.y(), 96, 24);
		RoleNode first = nodeOf(path, Step.S1);
		if (first != null) NodeIconRenderer.draw(ctx, first, h.x() + 7, h.y() + 4); // hueco del emblema de la cabecera
		boolean locked = chosen > 0 && chosen - 1 != path;
		UiStyle.label(ctx, textRenderer, Text.translatable(RoleTree.pathKey(role, path + 1)), h.x() + 30, h.y() + 7, 58,
				locked ? UiStyle.MUTED : chosen - 1 == path ? UiStyle.GOLD : UiStyle.TEXT);
	}

	/** Conectores de 2 px entre los centros de los nodos, del color de la senda si el siguiente escalón está aprendido. */
	private void drawLinks(DrawContext ctx, RoleSyncPayload data, int path) {
		int cx = layout.columnCenterX(path);
		int lit = PATH_COLORS[path];
		int[] cy = new int[TreeScreenLayout.ROWS];
		for (int row = 0; row < TreeScreenLayout.ROWS; row++) cy[row] = layout.node(path, row, 0).y() + TreeScreenLayout.NODE / 2;
		int bifurcation = TreeScreenLayout.BIFURCATION_ROW;
		for (int row = 0; row < bifurcation - 1; row++) {
			link(ctx, cx, cy[row], cy[row + 1], learned(data, path, ROW_STEPS[row + 1][0]) ? lit : LINK_DIM);
		}
		int leftX = layout.node(path, bifurcation, 0).x() + TreeScreenLayout.NODE / 2;
		int rightX = layout.node(path, bifurcation, 1).x() + TreeScreenLayout.NODE / 2;
		int mid = cy[bifurcation - 1] + (cy[bifurcation] - cy[bifurcation - 1]) / 2;
		boolean leftOn = learned(data, path, Step.S5X);
		boolean rightOn = learned(data, path, Step.S5Y);
		// S4 → S5: tallo, travesaño y una bajada por opción
		link(ctx, cx, cy[bifurcation - 1], mid, leftOn || rightOn ? lit : LINK_DIM);
		ctx.fill(leftX - 1, mid - 1, rightX + 1, mid + 1, leftOn || rightOn ? lit : LINK_DIM);
		link(ctx, leftX, mid, cy[bifurcation], leftOn ? lit : LINK_DIM);
		link(ctx, rightX, mid, cy[bifurcation], rightOn ? lit : LINK_DIM);
		// S5 → S6: una subida por opción que converge en el tallo
		boolean s6 = learned(data, path, Step.S6);
		int mid2 = cy[bifurcation] + (cy[bifurcation + 1] - cy[bifurcation]) / 2;
		link(ctx, leftX, cy[bifurcation], mid2, leftOn && s6 ? lit : LINK_DIM);
		link(ctx, rightX, cy[bifurcation], mid2, rightOn && s6 ? lit : LINK_DIM);
		ctx.fill(leftX - 1, mid2 - 1, rightX + 1, mid2 + 1, s6 ? lit : LINK_DIM);
		link(ctx, cx, mid2, cy[bifurcation + 1], s6 ? lit : LINK_DIM);
		link(ctx, cx, cy[bifurcation + 1], cy[bifurcation + 2], learned(data, path, Step.S7) ? lit : LINK_DIM);
	}

	private static void link(DrawContext ctx, int x, int y1, int y2, int color) {
		ctx.fill(x - 1, Math.min(y1, y2), x + 1, Math.max(y1, y2), color);
	}

	private boolean learned(RoleSyncPayload data, int path, Step step) {
		RoleNode node = nodeOf(path, step);
		return node != null && data.rank(node.id()) > 0;
	}

	private void drawNode(DrawContext ctx, RoleSyncPayload data, RoleNode node, Step step, int path, Rect r) {
		String state = nodeState(data, node, step);
		TreeUi.node(ctx, role.category(), state, r.x(), r.y());
		if (!"bloqueado".equals(state)) {
			boolean dim = "descartado".equals(state);
			if (dim) ctx.setShaderColor(1f, 1f, 1f, 0.4f);
			NodeIconRenderer.draw(ctx, node, r.x() + 4, r.y() + 4);
			if (dim) ctx.setShaderColor(1f, 1f, 1f, 1f);
		}
		if ("disponible".equals(state)) {
			int pulse = (int) (40 + 60 * (0.5 + 0.5 * Math.sin(Util.getMeasuringTimeMs() / 450.0)));
			ctx.drawBorder(r.x() - 1, r.y() - 1, r.w() + 2, r.h() + 2, (pulse << 24) | 0xF0C060);
		}
		if (path == selectedPath && step == selectedStep) {
			ctx.drawBorder(r.x() - 1, r.y() - 1, r.w() + 2, r.h() + 2, 0xFFFFE08A);
		}
		int rank = data.rank(node.id());
		if (node.maxRank() > 1 && rank > 0) {
			ctx.getMatrices().push();
			ctx.getMatrices().translate(r.right() - 8, r.bottom() - 7, 200);
			ctx.getMatrices().scale(0.6f, 0.6f, 1f);
			ctx.drawText(textRenderer, Integer.toString(rank), 0, 0, 0xFFFFFFFF, true);
			ctx.getMatrices().pop();
		}
	}

	private int masteryPoints() {
		var mastery = ClientMastery.get();
		return mastery == null ? 0 : mastery.points(role);
	}

	private void drawMastery(DrawContext ctx, int mouseX, int mouseY) {
		Rect m = layout.mastery();
		int points = masteryPoints();
		TreeUi.masteryBar(ctx, m.x(), m.y(), points);
		ctx.drawText(textRenderer, Integer.toString(points), m.right() + 6, m.y() + 5, UiStyle.GOLD, true);
	}

	private void drawDefinitive(DrawContext ctx, RoleSyncPayload data, int mouseX, int mouseY) {
		Rect slot = layout.definitive();
		boolean unlocked = data.tier(role) >= Evolution.ULTIMATE;
		TreeUi.sprite(ctx, "definitiva/definitiva_" + (unlocked ? "desbloqueada" : "bloqueada") + ".png", slot.x(), slot.y(),
				48, 48);
		if (unlocked) {
			ctx.getMatrices().push();
			ctx.getMatrices().translate(slot.x() + 8, slot.y() + 8, 0);
			ctx.getMatrices().scale(2f, 2f, 1f);
			NodeIconRenderer.drawRole(ctx, role, NodeIconRenderer.Badge.ULTIMATE, 0, 0);
			ctx.getMatrices().pop();
		}
		Rect label = layout.definitiveLabel();
		Text name = unlocked ? Text.translatable("ultimate.aptitudes." + role.key())
				: Text.translatable("screen.aptitudes.tree.tier_ultimate");
		Text fitted = UiStyle.fit(textRenderer, name, label.w());
		ctx.drawCenteredTextWithShadow(textRenderer, fitted, label.x() + label.w() / 2, label.y() + 1,
				unlocked ? UiStyle.GOLD : UiStyle.MUTED);
	}

	/** Ficha del escalón elegido: nombre, rango, requisitos y descripción hasta donde quepa; el cursor da el detalle. */
	private void drawDetail(DrawContext ctx, RoleSyncPayload data) {
		RoleNode node = selectedNode();
		if (node == null || selectedStep == null) return;
		Rect d = layout.detail();
		int x = d.x() + 9, w = d.w() - 18;
		int bottom = d.bottom() - (equip1Button.visible ? 30 : 10);
		int y = d.y() + 9;
		List<Text> lines = stepDetails(data, node, selectedPath, selectedStep);
		for (int i = 0; i < lines.size(); i++) {
			for (OrderedText part : textRenderer.wrapLines(lines.get(i), w)) {
				if (y + 8 > bottom) return;
				ctx.drawText(textRenderer, part, x, y, UiStyle.TEXT, false);
				y += 9;
			}
			y += 2;
		}
	}

	private List<Text> stepDetails(RoleSyncPayload data, RoleNode node, int path, @Nullable Step step) {
		int rank = data.rank(node.id());
		List<Text> lines = new ArrayList<>();
		lines.add(Text.translatable(node.translationKey()).formatted(Formatting.GOLD));
		if (RoleTree.keystone(node.id()) != null) {
			lines.add(Text.translatable("screen.aptitudes.paths.keystone").formatted(Formatting.LIGHT_PURPLE));
		}
		lines.add(Text.translatable("screen.aptitudes.tree.rank", rank, node.maxRank()));
		if (step != null && step.branch()) {
			lines.add(Text.translatable("screen.aptitudes.paths.branch").formatted(Formatting.YELLOW));
		}
		if (rank < node.maxRank()) {
			RoleNode pre = node.prerequisite() == null ? null : RoleTree.get(node.prerequisite());
			if (pre != null) {
				Text requirement = Text.translatable(pre.translationKey());
				if (step == Step.S6) {
					RoleNode alternative = nodeOf(path, Step.S5Y);
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

	/** Cursor sobre la maestría o la habilidad definitiva. */
	@Nullable
	private List<OrderedText> auxiliaryTooltip(RoleSyncPayload data, int mouseX, int mouseY) {
		List<Text> lines = new ArrayList<>();
		if (layout.mastery().contains(mouseX, mouseY)) {
			var mastery = ClientMastery.get();
			if (mastery == null) return null;
			int points = mastery.points(role);
			double bonus = mastery.enabled() && data.level(role) >= Math.max(50, data.rules().maxLevel())
					? MasteryRules.bonus(points, mastery.cap()) : 0;
			lines.add(Text.translatable("mastery.aptitudes.role", Text.translatable(role.translationKey()), points,
					UiStyle.fmt(bonus * 100)).formatted(Formatting.GOLD));
			lines.add(Text.translatable("mastery.aptitudes.roles_hint"));
			if (points < MasteryRules.MAX_POINTS) {
				lines.add(Text.translatable("mastery.aptitudes.xp", mastery.roles()[role.ordinal()],
						MasteryRules.required(points + 1, mastery.base(), mastery.step())));
			}
		} else if (layout.definitive().contains(mouseX, mouseY) || layout.definitiveLabel().contains(mouseX, mouseY)) {
			boolean unlocked = data.tier(role) >= Evolution.ULTIMATE;
			if (unlocked) {
				lines.add(Text.translatable("ultimate.aptitudes." + role.key()).formatted(Formatting.GOLD));
				lines.add(Text.translatable("ultimate.aptitudes." + role.key() + ".desc"));
			} else {
				lines.add(Text.translatable("screen.aptitudes.tree.requires_ultimate").formatted(Formatting.RED));
			}
		} else if (respecButton.isMouseOver(mouseX, mouseY)) {
			List<Text> respec = respecTooltip(data);
			if (respec == null) return null;
			lines.addAll(respec);
		} else {
			return null;
		}
		List<OrderedText> out = new ArrayList<>();
		for (Text line : lines) out.addAll(textRenderer.wrapLines(line, Math.min(248, width - 20)));
		return out;
	}

	// ------------------------------------------------------------ Entrada

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		RoleSyncPayload data = ClientRoleState.data();
		for (int path = 0; path < TreeScreenLayout.PATHS; path++) {
			for (int row = 0; row < TreeScreenLayout.ROWS; row++) {
				for (int slot = 0; slot < TreeScreenLayout.slots(row); slot++) {
					Step step = ROW_STEPS[row][slot];
					RoleNode node = nodeOf(path, step);
					if (node == null || !layout.node(path, row, slot).contains(mouseX, mouseY)) continue;
					boolean open = data != null && (data.rank(node.id()) > 0 || canLearn(data, node));
					playUi(open ? dev.aptitudes.sound.ModSounds.UI_METAL_SELECT : dev.aptitudes.sound.ModSounds.UI_METAL_DENIED);
					selectedPath = path;
					selectedStep = step;
					updateButtons();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int max = TreeScreenLayout.maxScroll(height);
		if (max <= 0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		int target = MathHelper.clamp(scroll - (int) (verticalAmount * 16), 0, max);
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
