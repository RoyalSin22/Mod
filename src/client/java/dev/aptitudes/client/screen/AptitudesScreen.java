package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientExtraState;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.ProgressRules;
import dev.aptitudes.data.Skill;
import dev.aptitudes.logic.KScreenLayout;
import dev.aptitudes.logic.TreeScreenLayout.Rect;
import dev.aptitudes.network.AllocatePointsPayload;
import dev.aptitudes.network.ForgeActionPayload;
import dev.aptitudes.network.RequestStatsPayload;
import dev.aptitudes.network.StatsPayload;
import dev.aptitudes.network.SyncProgressPayload;
import dev.aptitudes.sync.ClientCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Pantalla principal (tecla K) con pestañas:
 * <ul>
 *   <li><b>Atributos</b>: los puntos se PREPARAN (no se gastan al momento) y se
 *       ven en vivo sus efectos en las estadísticas base ("actual → nuevo").
 *       Confirmar los envía al servidor; Reiniciar los descarta.</li>
 *   <li><b>Habilidades</b>: las 8 habilidades por uso con su barra de XP.</li>
 * </ul>
 * Control por teclado: ↑↓ seleccionar, ←→ ajustar, Mayús ×5, Enter confirmar, Tab cambiar de página.
 */
public class AptitudesScreen extends Screen {
	/** Estadística (índice en StatPreview) que modifica cada atributo: Vigor, Mente, Aguante, Fuerza, Destreza, Inteligencia, Fe, Suerte. */
	private static final int[] STAT_OF_ATTRIBUTE = {0, 8, 3, 1, 2, 9, 10, 7};
	private static final String[] STAT_KEYS = {"health", "damage", "attack_speed", "armor", "movement_speed",
			"knockback_resistance", "toughness", "luck", "mana", "spell_damage", "cooldown_reduction"};

	private enum Tab { ATTRIBUTES, SKILLS }

	private Tab tab = Tab.ATTRIBUTES;

	// ---- Animaciones de la pestaña de habilidades (v21) ----
	/** Momento en que se mostró la pestaña (entrada en cascada de las tarjetas). */
	private long skillsShownAt;
	/** Relleno mostrado de cada barra de XP (se acerca al real fotograma a fotograma). */
	private final float[] shownFill = new float[Skill.values().length];
	private long lastFrame;
	/** Último nivel visto de cada habilidad (entre aperturas) y fin del brillo de subida de nivel. */
	private static final int[] LAST_SEEN_LEVEL = new int[Skill.values().length];
	private static final long[] GLOW_UNTIL = new long[Skill.values().length];
	private static boolean seenInitialized;
	private final int[] staged = new int[PlayerAttribute.values().length];
	private int selected = 0;
	private KScreenLayout layout = KScreenLayout.place(KScreenLayout.WIDTH, KScreenLayout.HEIGHT, 0);
	/** v39: desplazamiento vertical de la ventana cuando no cabe (p. ej., 720p a escala 3 o 1080p a escala 4). */
	private int pageScroll;
	private int ticks;
	private ButtonWidget confirmButton;
	private ButtonWidget resetButton;
	private ButtonWidget temperButton;
	private ButtonWidget flaskButton;

	/** {@code true} si se abrió descansando en una Hoguera: permite comprar niveles de alma. */
	private final boolean bonfire;

	public AptitudesScreen() {
		this(false);
	}

	public AptitudesScreen(boolean bonfire) {
		super(Text.translatable("screen.aptitudes.title"));
		this.bonfire = bonfire;
	}

	/** Puntos preparados que no cubren los puntos libres y hay que comprar como niveles. */
	private int levelsToBuy(SyncProgressPayload data) {
		return data.levelsForPoints(Math.max(0, stagedTotal() - Math.max(0, data.availablePoints())));
	}

	/** Coste total en niveles de XP de comprar {@code n} niveles de alma. */
	private static int costOf(SyncProgressPayload data, int n) {
		int cost = 0;
		for (int i = 0; i < n; i++) cost += data.levelCost(data.soulLevel() + i);
		return cost;
	}

	private boolean canAfford(SyncProgressPayload data, int levels) {
		if (client.player == null) return false;
		return client.player.isCreative() || client.player.experienceLevel >= costOf(data, levels);
	}

	@Override
	protected void init() {
		pageScroll = MathHelper.clamp(pageScroll, 0, maxPageScroll());
		layout = KScreenLayout.place(width, height, pageScroll);
		int buttons = bonfire ? 4 : 3;
		confirmButton = addDrawableChild(new SpriteButton(layout.button(0, buttons), Text.translatable(bonfire
				? "screen.aptitudes.level_up" : "screen.aptitudes.confirm"), "confirmar", true, 4, b -> confirm()));
		resetButton = addDrawableChild(new SpriteButton(layout.button(1, buttons),
				Text.translatable("screen.aptitudes.reset"), "deshacer", true, 0, b -> reset()));
		if (bonfire) {
			temperButton = addDrawableChild(new SpriteButton(layout.button(2, buttons),
					Text.translatable("forge.aptitudes.temper"), "generico", b -> {
				if (client.player != null && ClientPlayNetworking.canSend(ForgeActionPayload.ID)) {
					ClientPlayNetworking.send(new ForgeActionPayload(client.player.playerScreenHandler.syncId,
							ForgeActionPayload.OPEN_TEMPER, 0));
				}
			}));
			flaskButton = addDrawableChild(new SpriteButton(layout.button(3, buttons),
					Text.translatable("flask.aptitudes.open"), "generico", b -> {
				if (client.player != null && ClientPlayNetworking.canSend(ForgeActionPayload.ID)) {
					ClientPlayNetworking.send(new ForgeActionPayload(client.player.playerScreenHandler.syncId,
							ForgeActionPayload.OPEN_FLASKS, 0));
				}
			}));
		} else {
			addDrawableChild(new SpriteButton(layout.button(2, buttons), net.minecraft.screen.ScreenTexts.BACK, "cerrar",
					true, 0, b -> close()));
		}
		requestStats();
		updateButtons();
	}

	/** Cuánto se puede desplazar la ventana (0 si cabe con 8 px de margen arriba y abajo). */
	private int maxPageScroll() {
		return KScreenLayout.maxScroll(height);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (verticalAmount != 0 && maxPageScroll() > 0) {
			int target = MathHelper.clamp(pageScroll + (verticalAmount > 0 ? -24 : 24), 0, maxPageScroll());
			if (target != pageScroll) {
				pageScroll = target;
				clearAndInit(); // recoloca los botones con el nuevo desplazamiento
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void requestStats() {
		if (ClientPlayNetworking.canSend(RequestStatsPayload.ID)) ClientPlayNetworking.send(new RequestStatsPayload());
	}

	private int stagedTotal() {
		int sum = 0;
		for (int v : staged) sum += v;
		return sum;
	}

	private void adjust(int attribute, int delta) {
		SyncProgressPayload data = ClientCache.get();
		if (data == null) return;
		PlayerAttribute attr = PlayerAttribute.values()[attribute];
		int max = data.rules().maxPointsPerAttribute() - data.allocated(attr);
		int target = Math.max(0, Math.min(max, staged[attribute] + delta));
		if (bonfire) {
			// En la Hoguera se pueden comprar niveles mientras alcance la XP.
			while (target > staged[attribute]) {
				int old = staged[attribute];
				staged[attribute] = target;
				boolean ok = canAfford(data, levelsToBuy(data));
				staged[attribute] = old;
				if (ok) break;
				target--;
			}
		} else {
			int free = Math.max(0, data.availablePoints()) - stagedTotal() + staged[attribute];
			target = Math.min(target, free);
		}
		staged[attribute] = Math.max(0, target);
		updateButtons();
	}

	private void confirm() {
		if (stagedTotal() <= 0) return;
		if (bonfire) {
			ClientPlayNetworking.send(new dev.aptitudes.network.LevelUpPayload(staged.clone()));
		} else {
			for (int i = 0; i < staged.length; i++) {
				if (staged[i] > 0) ClientPlayNetworking.send(new AllocatePointsPayload(i, staged[i]));
			}
		}
		reset();
	}

	private void reset() {
		java.util.Arrays.fill(staged, 0);
		updateButtons();
	}

	private void updateButtons() {
		boolean attributes = tab == Tab.ATTRIBUTES;
		confirmButton.visible = resetButton.visible = attributes;
		if (temperButton != null) temperButton.visible = attributes;
		if (flaskButton != null) {
			flaskButton.visible = attributes;
			flaskButton.active = client.player != null && !client.player.isCreative() && !client.player.isSpectator();
		}
		SyncProgressPayload data = ClientCache.get();
		confirmButton.active = stagedTotal() > 0 && data != null && (!bonfire || canAfford(data, levelsToBuy(data)));
		resetButton.active = stagedTotal() > 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (++ticks % 20 == 0) requestStats(); // el equipo o los efectos pueden cambiar
		SyncProgressPayload data = ClientCache.get();
		if (data != null && !bonfire && stagedTotal() > Math.max(0, data.availablePoints())) reset(); // cambió en el servidor
		updateButtons();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ------------------------------------------------------------- Entrada

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		int step = hasShiftDown() ? 5 : 1;
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			if (hasShiftDown()) {
				client.setScreen(new RolesScreen());
			} else if (tab == Tab.ATTRIBUTES) {
				showSkills();
			} else {
				client.setScreen(new RolesScreen());
			}
			updateButtons();
			return true;
		}
		if (tab == Tab.ATTRIBUTES) {
			switch (keyCode) {
				case GLFW.GLFW_KEY_UP -> { selected = Math.floorMod(selected - 1, staged.length); return true; }
				case GLFW.GLFW_KEY_DOWN -> { selected = (selected + 1) % staged.length; return true; }
				case GLFW.GLFW_KEY_RIGHT -> { adjust(selected, step); return true; }
				case GLFW.GLFW_KEY_LEFT -> { adjust(selected, -step); return true; }
				case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> { confirm(); return true; }
				default -> { }
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Pestañas (de izquierda a derecha: Atributos, Habilidades, Roles, Configuración)
		for (int i = 0; i < KScreenLayout.TABS; i++) {
			if (!layout.tab(i).contains(mouseX, mouseY)) continue;
			switch (i) {
				case 0 -> tab = Tab.ATTRIBUTES;
				case 1 -> showSkills();
				case 2 -> client.setScreen(new RolesScreen());
				default -> client.setScreen(new ConfigScreen(this)); // v40
			}
			updateButtons();
			return true;
		}
		// Filas de atributos: clic = seleccionar; zonas ‹ › = ajustar
		if (tab == Tab.ATTRIBUTES) {
			for (int i = 0; i < staged.length; i++) {
				Rect row = layout.attributeRow(i);
				if (!row.contains(mouseX, mouseY)) continue;
				selected = i;
				if (mouseX >= row.x() + 126) adjust(i, hasShiftDown() ? 5 : 1);
				else if (mouseX >= row.x() + 66 && mouseX < row.x() + 80) adjust(i, hasShiftDown() ? -5 : -1);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	// ------------------------------------------------------------ Dibujo

	private boolean atHearth() {
		return bonfire && tab == Tab.ATTRIBUTES;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		KUi.panelMain(ctx, atHearth(), layout.window());
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		// Barra de desplazamiento de la ventana (solo si no cabe en la pantalla), como en la pantalla de roles.
		int maxScroll = maxPageScroll();
		if (maxScroll > 0) {
			int trackH = height - 16;
			int thumbH = Math.max(20, trackH * height / (KScreenLayout.HEIGHT + 16));
			int thumbY = 8 + (trackH - thumbH) * pageScroll / maxScroll;
			ctx.fill(width - 5, 8, width - 2, 8 + trackH, 0x60000000);
			ctx.fill(width - 5, thumbY, width - 2, thumbY + thumbH, UiStyle.GOLD);
		}
		boolean hearth = atHearth();
		Text heading = Text.translatable(hearth ? "block.aptitudes.bonfire" : tab == Tab.ATTRIBUTES
				? "screen.aptitudes.tab.attributes" : "screen.aptitudes.tab.skills");
		ctx.drawTextWithShadow(textRenderer, heading.copy().formatted(net.minecraft.util.Formatting.BOLD),
				layout.titleX(hearth), layout.titleY(), UiStyle.GOLD);
		if (hearth) {
			Rect e = layout.emblem();
			// El emblema es de 48×48 (píxel lógico de 2×2): a 24×24 cada píxel lógico ocupa 1 px de GUI.
			com.mojang.blaze3d.systems.RenderSystem.enableBlend();
			com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
			ctx.drawTexture(KUi.id("hoguera/emblema_hoguera.png"), e.x(), e.y(), e.w(), e.h(), 0f, 0f, 48, 48, 48, 48);
			com.mojang.blaze3d.systems.RenderSystem.disableBlend();
		}
		renderTabs(ctx, mouseX, mouseY);

		SyncProgressPayload data = ClientCache.get();
		if (data == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.waiting"),
					width / 2, layout.y() + KScreenLayout.HEIGHT / 2, UiStyle.MUTED);
			return;
		}
		if (tab == Tab.ATTRIBUTES) {
			renderAttributes(ctx, data, mouseX, mouseY);
			Rect hint = layout.hint();
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("screen.aptitudes.hint.attributes"),
					hint.w()), hint.x(), hint.y(), UiStyle.MUTED, false);
		} else {
			renderSkills(ctx, data, mouseX, mouseY);
		}
	}

	private void renderTabs(DrawContext ctx, int mouseX, int mouseY) {
		String[] keys = {"screen.aptitudes.tab.attributes", "screen.aptitudes.tab.skills", "screen.aptitudes.tab.roles",
				"screen.aptitudes.tab.config"};
		for (int i = 0; i < keys.length; i++) {
			Rect r = layout.tab(i);
			boolean active = (i == 0 && tab == Tab.ATTRIBUTES) || (i == 1 && tab == Tab.SKILLS);
			boolean hover = r.contains(mouseX, mouseY);
			KUi.sprite(ctx, "pestanas/pestana_" + (active ? "activa" : hover ? "hover" : "inactiva") + ".png",
					r.x(), r.y(), r.w(), r.h());
			Text label = UiStyle.fit(textRenderer, Text.translatable(keys[i]), r.w() - 20);
			ctx.drawCenteredTextWithShadow(textRenderer, label, r.x() + r.w() / 2, r.y() + 7,
					active ? UiStyle.GOLD : hover ? UiStyle.TEXT : UiStyle.MUTED);
		}
		Rect line = layout.divider();
		ctx.fill(line.x(), line.y(), line.right(), line.bottom(), UiStyle.GOLD_DARK);
	}

	private void sectionTitle(DrawContext ctx, Text text, int x, int y, int width) {
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer, text, width), x, y, UiStyle.GOLD);
	}

	private void renderAttributes(DrawContext ctx, SyncProgressPayload data, int mouseX, int mouseY) {
		StatsPayload stats = ClientExtraState.stats();
		PlayerAttribute[] attrs = PlayerAttribute.values();
		int max = data.rules().maxPointsPerAttribute();

		// ---- Franja de datos: nivel de alma a la izquierda; puntos o coste a la derecha
		Rect strip = layout.infoStrip();
		int free = Math.max(0, data.availablePoints());
		int buy = levelsToBuy(data);
		int after = Math.max(0, free - stagedTotal());
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.soul_level"), strip.x(), strip.y(), 110, UiStyle.TEXT);
		UiStyle.valueArrow(ctx, textRenderer, String.valueOf(data.soulLevel()), String.valueOf(data.soulLevel() + buy),
				strip.x() + 170, strip.y(), buy > 0);
		int pairX = strip.x() + 200;
		if (bonfire) {
			int cost = costOf(data, buy);
			int have = client.player == null ? 0 : client.player.experienceLevel;
			UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.xp_cost"), pairX, strip.y(), 100, UiStyle.TEXT);
			String costText = cost + " / " + have;
			ctx.drawText(textRenderer, costText, strip.right() - textRenderer.getWidth(costText), strip.y(),
					cost > have ? UiStyle.DOWN : UiStyle.GOOD, false);
		} else if (free > 0) {
			UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.attribute_points"), pairX, strip.y(), 100, UiStyle.TEXT);
			UiStyle.valueArrow(ctx, textRenderer, String.valueOf(free), String.valueOf(after), strip.right(), strip.y(), after != free);
		} else {
			UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.need_bonfire"), pairX, strip.y(),
					strip.right() - pairX, UiStyle.MUTED);
		}

		// ---- Columna 1: atributos
		KUi.panelColumn(ctx, layout.columnA());
		Rect h0 = layout.columnHeader(0);
		sectionTitle(ctx, Text.translatable("screen.aptitudes.attributes"), h0.x(), h0.y(), h0.w());
		for (int i = 0; i < attrs.length; i++) {
			Rect row = layout.attributeRow(i);
			KUi.sprite(ctx, "filas/fila_" + (i == selected ? "seleccionada" : i % 2 == 0 ? "normal" : "alterna") + ".png",
					row.x(), row.y(), row.w(), row.h());
			int ty = row.y() + 4;
			int affinity = data.affinity(attrs[i]);
			UiStyle.label(ctx, textRenderer, Text.translatable(attrs[i].translationKey()), row.x() + 6, ty,
					affinity > 0 ? 52 : 62, affinity > 0 ? UiStyle.GOLD : UiStyle.TEXT);
			if (affinity > 0) ctx.drawText(textRenderer, "★", row.x() + 58, ty, UiStyle.GOLD, false);
			int cur = data.allocated(attrs[i]);
			boolean canAdd = cur + staged[i] < max && (bonfire || after > 0);
			KUi.sprite(ctx, "flechas/menos_" + (staged[i] > 0 ? "activa" : "desactivada") + ".png", row.x() + 70, row.y() + 3, 8, 10);
			UiStyle.valueArrow(ctx, textRenderer, String.valueOf(cur), String.valueOf(cur + staged[i]), row.x() + 124, ty, staged[i] > 0);
			KUi.sprite(ctx, "flechas/mas_" + (canAdd ? "activa" : "desactivada") + ".png", row.x() + 128, row.y() + 3, 8, 10);
		}

		// ---- Columna 2: estadísticas base
		KUi.panelColumn(ctx, layout.columnB());
		Rect h1 = layout.columnHeader(1);
		sectionTitle(ctx, Text.translatable("screen.aptitudes.base_stats"), h1.x(), h1.y(), h1.w());
		int affected = STAT_OF_ATTRIBUTE[selected];
		for (int sIdx = 0; sIdx < STAT_KEYS.length; sIdx++) {
			Rect row = layout.statRow(sIdx);
			UiStyle.row(ctx, row.x(), row.y(), row.w(), row.h(), sIdx, sIdx == affected);
			if (sIdx == affected) ctx.drawText(textRenderer, "+", row.x() - 3, row.y() + 2, UiStyle.GOLD, false);
			Text statName = Text.translatable("stat.aptitudes." + STAT_KEYS[sIdx]);
			if (stats == null || sIdx >= stats.stats().length) {
				UiStyle.label(ctx, textRenderer, statName, row.x() + 4, row.y() + 2, row.w() - 8, UiStyle.TEXT);
				continue;
			}
			double cur = stats.stats()[sIdx];
			double next = cur;
			for (int a = 0; a < attrs.length; a++) {
				if (STAT_OF_ATTRIBUTE[a] == sIdx && a < stats.slopes().length) next += staged[a] * stats.slopes()[a];
			}
			String curText = UiStyle.fmt(cur);
			String nextText = UiStyle.fmt(next);
			int valueW = textRenderer.getWidth(curText + " → " + nextText);
			UiStyle.label(ctx, textRenderer, statName, row.x() + 4, row.y() + 2, row.w() - 14 - valueW, UiStyle.TEXT);
			UiStyle.valueArrow(ctx, textRenderer, curText, nextText, row.right() - 4, row.y() + 2, Math.abs(next - cur) > 1e-6);
		}

		// ---- Columna 3: detalle del atributo seleccionado
		KUi.panelDetail(ctx, layout.columnC());
		Rect h2 = layout.columnHeader(2);
		sectionTitle(ctx, Text.translatable("screen.aptitudes.details"), h2.x(), h2.y(), h2.w());
		Rect in = layout.detailInner();
		PlayerAttribute attr = attrs[selected];
		ctx.drawTextWithShadow(textRenderer, Text.translatable(attr.translationKey()), in.x(), in.y(), UiStyle.GOLD);
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("screen.aptitudes.points_invested",
				data.allocated(attr)), in.w()), in.x(), in.y() + 11, UiStyle.MUTED, false);
		List<OrderedText> lines = textRenderer.wrapLines(Text.translatable(attr.descriptionKey()), in.w());
		int ly = in.y() + 26;
		for (OrderedText line : lines) {
			if (ly + 8 > in.y() + 96) break;
			ctx.drawText(textRenderer, line, in.x(), ly, UiStyle.TEXT, false);
			ly += 10;
		}
		if (data.affinity(attr) > 0) {
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("screen.aptitudes.affinity",
					data.affinity(attr)), in.w()), in.x(), in.y() + 100, UiStyle.GOLD, false);
		}
		double slope = stats != null && selected < stats.slopes().length ? stats.slopes()[selected] : 0;
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.per_point"), in.x(), in.y() + 112, UiStyle.MUTED, false);
		String perPoint = (slope >= 0 ? "+" : "") + UiStyle.fmt(slope);
		ctx.drawText(textRenderer, perPoint, in.right() - textRenderer.getWidth(perPoint), in.y() + 112, UiStyle.UP, false);
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.limit"), in.x(), in.y() + 124, UiStyle.MUTED, false);
		ctx.drawText(textRenderer, String.valueOf(max), in.right() - textRenderer.getWidth(String.valueOf(max)),
				in.y() + 124, UiStyle.TEXT, false);
	}

	private void showSkills() {
		if (tab != Tab.SKILLS) {
			tab = Tab.SKILLS;
			skillsShownAt = net.minecraft.util.Util.getMeasuringTimeMs();
			java.util.Arrays.fill(shownFill, 0f); // las barras se llenan al entrar
		}
	}

	/** Suavizado: 0→1 con desaceleración. */
	private static float easeOut(float t) {
		t = MathHelper.clamp(t, 0f, 1f);
		return 1f - (1f - t) * (1f - t) * (1f - t);
	}

	private void renderSkills(DrawContext ctx, SyncProgressPayload data, int mouseX, int mouseY) {
		ProgressRules rules = data.rules();
		Rect strip = layout.infoStrip();
		sectionTitle(ctx, Text.translatable("screen.aptitudes.skills_passives"), strip.x(), strip.y(), 220);
		var mastery = dev.aptitudes.sync.ClientMastery.get();
		var roles = dev.aptitudes.client.ClientRoleState.data();
		if (mastery != null && roles != null && roles.primaryRole() != null) {
			Text label = Text.translatable("mastery.aptitudes.roles", mastery.points(roles.primaryRole()),
					roles.secondaryRole() == null ? 0 : mastery.points(roles.secondaryRole()));
			ctx.drawTextWithShadow(textRenderer, label, strip.right() - textRenderer.getWidth(label), strip.y(), UiStyle.GOLD);
		}
		Skill[] skills = Skill.values();
		long now = net.minecraft.util.Util.getMeasuringTimeMs();
		float dt = lastFrame == 0 ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
		lastFrame = now;
		// Subidas de nivel desde la última vez: brillo dorado durante 2,5 s.
		for (Skill s : skills) {
			int lvl = data.level(s);
			if (seenInitialized && lvl > LAST_SEEN_LEVEL[s.ordinal()]) GLOW_UNTIL[s.ordinal()] = now + 2500;
			LAST_SEEN_LEVEL[s.ordinal()] = lvl;
		}
		seenInitialized = true;

		List<Text> tooltip = null;
		final int cardW = KScreenLayout.CARD_W;
		for (int i = 0; i < skills.length; i++) {
			Skill skill = skills[i];
			Rect cell = layout.card(i);
			int col = i % 2;
			float enter = easeOut((now - skillsShownAt - i * 35L) / 260f);
			int x = cell.x() + Math.round((1f - enter) * (col == 0 ? -14 : 14));
			int y = cell.y();
			boolean hover = cell.contains(mouseX, mouseY);
			int level = data.level(skill);
			boolean maxed = level >= rules.maxLevel();
			long glow = GLOW_UNTIL[i] - now;
			// Estado de la tarjeta: subida de nivel reciente (2,5 s), nivel máximo, cursor encima o normal.
			String state = glow > 0 ? "subida" : maxed ? "maxima" : hover ? "hover" : "normal";
			KUi.sprite(ctx, "tarjetas/tarjeta_" + state + ".png", x, y, cardW, KScreenLayout.CARD_H);
			ctx.drawItem(skill.iconStack(), x + 7, y + 11);

			Text lv = maxed && mastery != null ? Text.translatable("mastery.aptitudes.card", mastery.points(skill))
					: maxed ? Text.translatable("screen.aptitudes.max") : Text.translatable("screen.aptitudes.level", level);
			Text name = UiStyle.fit(textRenderer, Text.translatable(skill.translationKey()),
					cardW - 98 - textRenderer.getWidth(lv));
			ctx.drawTextWithShadow(textRenderer, name, x + 34, y + 3, UiStyle.TEXT);
			// Hitos (10/20/30/40/50): marcas encendidas si se alcanzaron.
			int px = x + 34 + textRenderer.getWidth(name) + 5;
			for (int m : dev.aptitudes.logic.SkillMilestones.THRESHOLDS) {
				KUi.sprite(ctx, "hitos/hito_" + (level >= m ? "encendido" : "apagado") + ".png", px, y + 2, 8, 8);
				px += 9;
			}
			ctx.drawTextWithShadow(textRenderer, lv, x + cardW - 8 - textRenderer.getWidth(lv), y + 3, maxed ? UiStyle.GOLD : UiStyle.TEXT);

			// Barra de XP animada
			int need = rules.xpToNext(level);
			float target = maxed ? 1f : (float) Math.min(1.0, data.xp(skill) / (double) Math.max(1, need));
			shownFill[i] += (target - shownFill[i]) * Math.min(1f, dt * 6f);
			if (Math.abs(target - shownFill[i]) < 0.002f) shownFill[i] = target;
			int barX = x + 34;
			int barY = y + 14;
			KUi.xpBar(ctx, barX, barY, shownFill[i], maxed);
			if (maxed) { // destello que recorre la barra
				int sx = barX + (int) ((now / 6) % (KUi.BAR_W + 20)) - 20;
				ctx.fill(Math.max(barX, sx), barY, Math.min(barX + KUi.BAR_W, sx + 8), barY + KUi.BAR_H, 0x60FFFFFF);
			}

			// Pasivas: una línea resumida (el detalle completo, en la ficha)
			MutableText summary = Text.empty();
			List<Skill.SkillBonus> bonuses = skill.bonuses();
			boolean anyActive = false;
			for (int b = 0; b < bonuses.size(); b++) {
				Skill.SkillBonus bonus = bonuses.get(b);
				double value = data.skillBonus(skill, b) * (bonus.percent() ? 100 : 1);
				if (b > 0) summary.append(Text.literal(" · ").formatted(net.minecraft.util.Formatting.DARK_GRAY));
				summary.append(Text.translatable(bonus.translationKey(), UiStyle.fmt(value)));
				anyActive |= value > 0;
			}
			UiStyle.label(ctx, textRenderer, summary, x + 34, y + 24, cardW - 42, anyActive ? UiStyle.UP : UiStyle.MUTED);

			if (hover) {
				List<Text> lines = new java.util.ArrayList<>();
				lines.add(Text.translatable(skill.translationKey()).formatted(net.minecraft.util.Formatting.GOLD));
				lines.add(Text.translatable(skill.descriptionKey()).formatted(net.minecraft.util.Formatting.GRAY));
				lines.add(Text.translatable("screen.aptitudes.xp", data.xp(skill), need));
				if (mastery != null) {
					int points = mastery.points(skill);
					lines.add(Text.translatable("mastery.aptitudes.skill", points,
							UiStyle.fmt((maxed ? mastery.bonus(skill) : 0) * 100)).formatted(net.minecraft.util.Formatting.GOLD));
					lines.add(points >= dev.aptitudes.logic.MasteryRules.MAX_POINTS ? Text.translatable("mastery.aptitudes.limit")
							: Text.translatable("mastery.aptitudes.xp", mastery.skills()[skill.ordinal()],
							dev.aptitudes.logic.MasteryRules.required(points + 1, mastery.base(), mastery.step())));
				}
				lines.add(Text.translatable("screen.aptitudes.passives").formatted(net.minecraft.util.Formatting.AQUA));
				for (int b = 0; b < bonuses.size(); b++) {
					Skill.SkillBonus bonus = bonuses.get(b);
					double value = data.skillBonus(skill, b) * (bonus.percent() ? 100 : 1);
					lines.add(Text.literal(" • ").append(Text.translatable(bonus.translationKey(), UiStyle.fmt(value)))
							.formatted(value > 0 ? net.minecraft.util.Formatting.BLUE : net.minecraft.util.Formatting.GRAY));
				}
				lines.add(Text.translatable("screen.aptitudes.milestones").formatted(net.minecraft.util.Formatting.GOLD));
				for (int m : dev.aptitudes.logic.SkillMilestones.THRESHOLDS) {
					boolean got = level >= m;
					String key = dev.aptitudes.logic.SkillMilestones.key(skill, m);
					lines.add(Text.literal(got ? "✔ " : "✖ ").formatted(got ? net.minecraft.util.Formatting.GREEN
									: net.minecraft.util.Formatting.DARK_GRAY)
							.append(Text.translatable("screen.aptitudes.milestone_line", m, Text.translatable(key),
									Text.translatable(key + ".desc")).formatted(got ? net.minecraft.util.Formatting.WHITE
									: net.minecraft.util.Formatting.GRAY)));
				}
				tooltip = lines;
			}
		}
		if (mastery != null && roles != null && roles.primaryRole() != null && layout.infoStrip().contains(mouseX, mouseY)
				&& mouseX >= layout.infoStrip().right() - 115) {
			tooltip = new java.util.ArrayList<>();
			tooltip.add(Text.translatable("mastery.aptitudes.roles_hint"));
			for (var role : new dev.aptitudes.role.Role[]{roles.primaryRole(), roles.secondaryRole()}) {
				if (role == null) continue;
				int points = mastery.points(role);
				double bonus = mastery.enabled() && roles.level(role) >= Math.max(50, roles.rules().maxLevel())
						? dev.aptitudes.logic.MasteryRules.bonus(points, mastery.cap()) : 0;
				tooltip.add(Text.translatable("mastery.aptitudes.role", Text.translatable(role.translationKey()), points,
						UiStyle.fmt(bonus * 100)).formatted(net.minecraft.util.Formatting.GOLD));
				if (points < dev.aptitudes.logic.MasteryRules.MAX_POINTS) tooltip.add(Text.translatable("mastery.aptitudes.xp",
						mastery.roles()[role.ordinal()], dev.aptitudes.logic.MasteryRules.required(points + 1, mastery.base(), mastery.step())));
			}
		}
		if (tooltip != null) ctx.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
	}

}
