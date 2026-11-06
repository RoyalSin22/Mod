package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientExtraState;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.ProgressRules;
import dev.aptitudes.data.Skill;
import dev.aptitudes.network.AllocatePointsPayload;
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
	private static final int W = 430;
	private static final int H = 262;
	private static final int ATTR_ROW_H = 17;
	private static final int STAT_ROW_H = 14;
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
	private int left;
	private int top;
	/** v39: desplazamiento vertical de la ventana cuando no cabe (p. ej., 720p a escala 3 o 1080p a escala 4). */
	private int pageScroll;
	private int ticks;
	private ButtonWidget confirmButton;
	private ButtonWidget resetButton;

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
		left = (width - W) / 2;
		pageScroll = MathHelper.clamp(pageScroll, 0, maxPageScroll());
		top = maxPageScroll() > 0 ? 8 - pageScroll : (height - H) / 2;
		confirmButton = addDrawableChild(ButtonWidget.builder(Text.translatable(bonfire
						? "screen.aptitudes.level_up" : "screen.aptitudes.confirm"), b -> confirm())
				.dimensions(left + 10, top + H - 44, 140, 20).build());
		resetButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.reset"), b -> reset())
				.dimensions(left + 156, top + H - 44, 70, 20).build());
		requestStats();
		updateButtons();
	}

	/** Cuánto se puede desplazar la ventana (0 si cabe con 8 px de margen arriba y abajo). */
	private int maxPageScroll() {
		return Math.max(0, H + 16 - height);
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
		// Pestañas
		int tabX = left + W - 10;
		String[] tabs = {"screen.aptitudes.tab.config", "screen.aptitudes.tab.roles", "screen.aptitudes.tab.skills",
				"screen.aptitudes.tab.attributes"};
		for (String key : tabs) {
			int w = textRenderer.getWidth(Text.translatable(key)) + 10;
			tabX -= w;
			if (mouseX >= tabX && mouseX < tabX + w && mouseY >= top + 6 && mouseY < top + 20) {
				switch (key) {
					case "screen.aptitudes.tab.config" -> client.setScreen(new ConfigScreen(this)); // v40
					case "screen.aptitudes.tab.roles" -> client.setScreen(new RolesScreen());
					case "screen.aptitudes.tab.skills" -> showSkills();
					default -> tab = Tab.ATTRIBUTES;
				}
				updateButtons();
				return true;
			}
		}
		// Filas de atributos: clic = seleccionar; zonas ‹ › = ajustar
		if (tab == Tab.ATTRIBUTES) {
			int x = left + 10;
			int y0 = top + 66;
			for (int i = 0; i < staged.length; i++) {
				int y = y0 + i * ATTR_ROW_H;
				if (mouseX >= x && mouseX < x + 140 && mouseY >= y && mouseY < y + ATTR_ROW_H - 2) {
					selected = i;
					if (mouseX >= x + 128) adjust(i, hasShiftDown() ? 5 : 1);
					else if (mouseX >= x + 70 && mouseX < x + 80) adjust(i, hasShiftDown() ? -5 : -1);
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	// ------------------------------------------------------------ Dibujo

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, W, H);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		// Barra de desplazamiento de la ventana (solo si no cabe en la pantalla), como en la pantalla de roles.
		int maxScroll = maxPageScroll();
		if (maxScroll > 0) {
			int trackH = height - 16;
			int thumbH = Math.max(20, trackH * height / (H + 16));
			int thumbY = 8 + (trackH - thumbH) * pageScroll / maxScroll;
			ctx.fill(width - 5, 8, width - 2, 8 + trackH, 0x60000000);
			ctx.fill(width - 5, thumbY, width - 2, thumbY + thumbH, UiStyle.GOLD);
		}
		ctx.drawTextWithShadow(textRenderer, Text.translatable(tab == Tab.ATTRIBUTES
				? "screen.aptitudes.tab.attributes" : "screen.aptitudes.tab.skills").copy()
				.formatted(net.minecraft.util.Formatting.BOLD), left + 12, top + 9, UiStyle.GOLD);
		renderTabs(ctx, mouseX, mouseY);

		SyncProgressPayload data = ClientCache.get();
		if (data == null) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.waiting"),
					width / 2, top + H / 2, UiStyle.MUTED);
			return;
		}
		if (tab == Tab.ATTRIBUTES) renderAttributes(ctx, data, mouseX, mouseY);
		else renderSkills(ctx, data, mouseX, mouseY);

		ctx.drawText(textRenderer, Text.translatable(tab == Tab.ATTRIBUTES ? "screen.aptitudes.hint.attributes"
				: "screen.aptitudes.hint.tabs"), left + 10, top + H - 16, UiStyle.MUTED, false);
	}

	private void renderTabs(DrawContext ctx, int mouseX, int mouseY) {
		int tabX = left + W - 10;
		String[] tabs = {"screen.aptitudes.tab.config", "screen.aptitudes.tab.roles", "screen.aptitudes.tab.skills",
				"screen.aptitudes.tab.attributes"};
		for (String key : tabs) {
			Text label = Text.translatable(key);
			int w = textRenderer.getWidth(label) + 10;
			tabX -= w;
			boolean active = (key.endsWith("attributes") && tab == Tab.ATTRIBUTES) || (key.endsWith("skills") && tab == Tab.SKILLS);
			boolean hover = mouseX >= tabX && mouseX < tabX + w && mouseY >= top + 6 && mouseY < top + 20;
			ctx.drawText(textRenderer, label, tabX + 5, top + 9, active ? UiStyle.GOLD : hover ? UiStyle.TEXT : UiStyle.MUTED, false);
			if (active) ctx.fill(tabX + 5, top + 19, tabX + w - 5, top + 20, UiStyle.GOLD);
		}
		ctx.fill(left + 8, top + 23, left + W - 8, top + 24, UiStyle.BORDER);
	}

	private void renderAttributes(DrawContext ctx, SyncProgressPayload data, int mouseX, int mouseY) {
		StatsPayload stats = ClientExtraState.stats();
		PlayerAttribute[] attrs = PlayerAttribute.values();
		int max = data.rules().maxPointsPerAttribute();

		// ---- Columna 1: puntos de atributo
		int x = left + 10;
		int free = Math.max(0, data.availablePoints());
		int buy = levelsToBuy(data);
		int after = Math.max(0, free - stagedTotal());
		UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.soul_level"), x, top + 28, 90, UiStyle.TEXT);
		UiStyle.valueArrow(ctx, textRenderer, String.valueOf(data.soulLevel()), String.valueOf(data.soulLevel() + buy),
				x + 140, top + 28, buy > 0);
		if (bonfire) {
			int cost = costOf(data, buy);
			int have = client.player == null ? 0 : client.player.experienceLevel;
			UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.xp_cost"), x, top + 39, 80, UiStyle.TEXT);
			String costText = cost + " / " + have;
			ctx.drawText(textRenderer, costText, x + 140 - textRenderer.getWidth(costText), top + 39,
					cost > have ? UiStyle.DOWN : UiStyle.GOOD, false);
		} else if (free > 0) {
			UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.attribute_points"), x, top + 39, 90, UiStyle.TEXT);
			UiStyle.valueArrow(ctx, textRenderer, String.valueOf(free), String.valueOf(after), x + 140, top + 39, after != free);
		} else {
			UiStyle.label(ctx, textRenderer, Text.translatable("screen.aptitudes.need_bonfire"), x, top + 39, 140, UiStyle.MUTED);
		}
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.attributes"), x, top + 50, 140);
		for (int i = 0; i < attrs.length; i++) {
			int y = top + 66 + i * ATTR_ROW_H;
			UiStyle.row(ctx, x, y, 140, ATTR_ROW_H - 2, i, i == selected);
			int affinity = data.affinity(attrs[i]);
			UiStyle.label(ctx, textRenderer, Text.translatable(attrs[i].translationKey()), x + 4, y + 4,
					affinity > 0 ? 56 : 66, affinity > 0 ? UiStyle.GOLD : UiStyle.TEXT);
			if (affinity > 0) ctx.drawText(textRenderer, "★", x + 62, y + 4, UiStyle.GOLD, false);
			int cur = data.allocated(attrs[i]);
			boolean changed = staged[i] > 0;
			ctx.drawText(textRenderer, "‹", x + 72, y + 4, staged[i] > 0 ? UiStyle.GOLD : UiStyle.MUTED, false);
			UiStyle.valueArrow(ctx, textRenderer, String.valueOf(cur), String.valueOf(cur + staged[i]), x + 124, y + 4, changed);
			boolean canAdd = cur + staged[i] < max && (bonfire || after > 0);
			ctx.drawText(textRenderer, "›", x + 131, y + 4, canAdd ? UiStyle.GOLD : UiStyle.MUTED, false);
		}

		// ---- Columna 2: estadísticas base
		int sx = left + 158;
		int sw = 128;
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.base_stats"), sx, top + 30, sw);
		int affected = STAT_OF_ATTRIBUTE[selected];
		for (int s = 0; s < STAT_KEYS.length; s++) {
			int y = top + 46 + s * STAT_ROW_H;
			UiStyle.row(ctx, sx, y, sw, STAT_ROW_H - 2, s, s == affected);
			if (s == affected) ctx.drawText(textRenderer, "+", sx - 7, y + 3, UiStyle.GOLD, false);
			Text statName = Text.translatable("stat.aptitudes." + STAT_KEYS[s]);
			if (stats == null || s >= stats.stats().length) {
				UiStyle.label(ctx, textRenderer, statName, sx + 4, y + 3, sw - 8, UiStyle.TEXT);
				continue;
			}
			double cur = stats.stats()[s];
			double next = cur;
			for (int a = 0; a < attrs.length; a++) {
				if (STAT_OF_ATTRIBUTE[a] == s && a < stats.slopes().length) next += staged[a] * stats.slopes()[a];
			}
			String curText = UiStyle.fmt(cur);
			String nextText = UiStyle.fmt(next);
			int valueW = textRenderer.getWidth(curText + " → " + nextText);
			UiStyle.label(ctx, textRenderer, statName, sx + 4, y + 3, sw - 14 - valueW, UiStyle.TEXT);
			UiStyle.valueArrow(ctx, textRenderer, curText, nextText, sx + sw - 4, y + 3, Math.abs(next - cur) > 1e-6);
		}

		// ---- Columna 3: detalle del atributo seleccionado
		int dx = left + 294;
		PlayerAttribute attr = attrs[selected];
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.details"), dx, top + 30, 130);
		UiStyle.panel(ctx, dx, top + 46, 130, 120);
		ctx.drawTextWithShadow(textRenderer, Text.translatable(attr.translationKey()), dx + 6, top + 52, UiStyle.GOLD);
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.points_invested", data.allocated(attr)),
				dx + 6, top + 63, UiStyle.MUTED, false);
		if (data.affinity(attr) > 0) {
			ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.affinity", data.affinity(attr)),
					dx + 6, top + 126, UiStyle.GOLD, false);
		}
		List<OrderedText> lines = textRenderer.wrapLines(Text.translatable(attr.descriptionKey()), 118);
		int ly = top + 78;
		for (OrderedText line : lines) {
			ctx.drawText(textRenderer, line, dx + 6, ly, UiStyle.TEXT, false);
			ly += 10;
		}
		double slope = stats != null && selected < stats.slopes().length ? stats.slopes()[selected] : 0;
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.per_point"), dx + 6, top + 138, UiStyle.MUTED, false);
		String perPoint = (slope >= 0 ? "+" : "") + UiStyle.fmt(slope);
		ctx.drawText(textRenderer, perPoint, dx + 124 - textRenderer.getWidth(perPoint), top + 138, UiStyle.UP, false);
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.limit"), dx + 6, top + 150, UiStyle.MUTED, false);
		ctx.drawText(textRenderer, String.valueOf(max), dx + 124 - textRenderer.getWidth(String.valueOf(max)), top + 150,
				UiStyle.TEXT, false);
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
		UiStyle.header(ctx, textRenderer, Text.translatable("screen.aptitudes.skills_passives"), left + 10, top + 30, W - 20);
		var mastery = dev.aptitudes.sync.ClientMastery.get();
		var roles = dev.aptitudes.client.ClientRoleState.data();
		if (mastery != null && roles != null && roles.primaryRole() != null) {
			Text label = Text.translatable("mastery.aptitudes.roles", mastery.points(roles.primaryRole()),
					roles.secondaryRole() == null ? 0 : mastery.points(roles.secondaryRole()));
			ctx.drawTextWithShadow(textRenderer, label, left + W - 10 - textRenderer.getWidth(label), top + 30, UiStyle.GOLD);
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
		final int cardW = 200;
		final int cardH = 37;
		for (int i = 0; i < skills.length; i++) {
			Skill skill = skills[i];
			int col = i % 2;
			// 10 habilidades en 5 filas: paso 40 px, tarjetas de 37 px (caben en H = 262 con la línea de ayuda).
			int baseX = left + 10 + col * 210;
			int baseY = top + 44 + (i / 2) * 40;
			float enter = easeOut((now - skillsShownAt - i * 35L) / 260f);
			int x = baseX + Math.round((1f - enter) * (col == 0 ? -14 : 14));
			boolean hover = mouseX >= baseX && mouseX < baseX + cardW && mouseY >= baseY && mouseY < baseY + cardH;
			int y = baseY - (hover ? 1 : 0);
			int level = data.level(skill);
			boolean maxed = level >= rules.maxLevel();

			UiStyle.panel(ctx, x, y, cardW, cardH);
			long glow = GLOW_UNTIL[i] - now;
			if (glow > 0) {
				int a = (int) (110 + 110 * Math.sin(now / 120.0));
				ctx.drawBorder(x, y, cardW, cardH, (MathHelper.clamp(a, 0, 255) << 24) | 0xE6C36A);
			} else if (hover) {
				ctx.drawBorder(x, y, cardW, cardH, UiStyle.GOLD_DARK);
			}
			// Icono enmarcado
			ctx.fill(x + 3, y + 3, x + 23, y + 23, 0xFF1B1F2B);
			ctx.drawBorder(x + 3, y + 3, 20, 20, maxed ? UiStyle.GOLD : UiStyle.BORDER);
			ctx.drawItem(skill.iconStack(), x + 5, y + 5);

			Text lv = maxed && mastery != null ? Text.translatable("mastery.aptitudes.card", mastery.points(skill))
					: maxed ? Text.translatable("screen.aptitudes.max") : Text.translatable("screen.aptitudes.level", level);
			Text name = UiStyle.fit(textRenderer, Text.translatable(skill.translationKey()),
					cardW - 80 - textRenderer.getWidth(lv));
			ctx.drawTextWithShadow(textRenderer, name, x + 28, y + 4, UiStyle.TEXT);
			// Hitos (10/20/30/40/50): rombos dorados si se alcanzaron.
			int px = x + 28 + textRenderer.getWidth(name) + 5;
			for (int m : dev.aptitudes.logic.SkillMilestones.THRESHOLDS) {
				boolean got = level >= m;
				ctx.fill(px + 1, y + 5, px + 4, y + 10, got ? UiStyle.GOLD : 0xFF3A3427);
				ctx.fill(px, y + 6, px + 5, y + 9, got ? UiStyle.GOLD : 0xFF3A3427);
				if (got) ctx.fill(px + 2, y + 6, px + 3, y + 7, 0xFFFFF3B0);
				px += 7;
			}
			ctx.drawTextWithShadow(textRenderer, lv, x + cardW - 6 - textRenderer.getWidth(lv), y + 4, maxed ? UiStyle.GOLD : UiStyle.TEXT);

			// Barra de XP animada
			int need = rules.xpToNext(level);
			float target = maxed ? 1f : (float) Math.min(1.0, data.xp(skill) / (double) Math.max(1, need));
			shownFill[i] += (target - shownFill[i]) * Math.min(1f, dt * 6f);
			if (Math.abs(target - shownFill[i]) < 0.002f) shownFill[i] = target;
			int barX = x + 28;
			int barW = cardW - 34;
			ctx.fill(barX, y + 15, barX + barW, y + 19, 0xFF202433);
			int filled = Math.round(barW * shownFill[i]);
			ctx.fill(barX, y + 15, barX + filled, y + 19, maxed ? UiStyle.GOLD : 0xFF4CAF50);
			ctx.fill(barX, y + 15, barX + filled, y + 16, maxed ? 0xFFFFF3B0 : 0xFF8BE08F); // reflejo superior
			if (maxed) { // destello que recorre la barra
				int sx = barX + (int) ((now / 6) % (barW + 20)) - 20;
				ctx.fill(Math.max(barX, sx), y + 15, Math.min(barX + barW, sx + 8), y + 19, 0x90FFFFFF);
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
			UiStyle.label(ctx, textRenderer, summary, x + 6, y + 26, cardW - 12, anyActive ? UiStyle.UP : UiStyle.MUTED);

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
		if (mastery != null && roles != null && roles.primaryRole() != null && mouseY >= top + 28 && mouseY < top + 40
				&& mouseX >= left + W - 125 && mouseX < left + W - 10) {
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
