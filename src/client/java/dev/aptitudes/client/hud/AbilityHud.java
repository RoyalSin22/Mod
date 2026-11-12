package dev.aptitudes.client.hud;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.Signatures;
import dev.aptitudes.client.AptitudesClient;
import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientExtraState;
import dev.aptitudes.client.ClientEclipse;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.network.UniqueSyncPayload;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.client.screen.HudConfigScreen;
import dev.aptitudes.client.screen.NodeIconRenderer;
import dev.aptitudes.client.screen.UiStyle;
import dev.aptitudes.logic.AbilityHudLayout;
import dev.aptitudes.logic.HudMotion;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleResource;
import dev.aptitudes.role.RoleTree;
import dev.aptitudes.unique.UniqueSkill;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import dev.aptitudes.network.MechanicPayload;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * HUD junto a la hotbar (tecla O para ocultarlo):
 * <ul>
 *   <li><b>Derecha</b>: barra de habilidades V B · N M · Z R · H. Cada ranura muestra
 *       el icono de la habilidad, su tecla encima, el coste de maná, el enfriamiento
 *       (barrido + segundos), un candado si no está aprendida, tinte azul si falta
 *       maná y marco dorado si el recurso de rol la potenciará.</li>
 *   <li><b>Izquierda</b>: barras de Maná y de los recursos del rol principal y secundario.</li>
 * </ul>
 */
public final class AbilityHud {
	private static final int SLOT = 20;
	private static final int GAP = 2;
	private static final int GROUP_GAP = 6;
	private static final int BAR_W = 92;
	private static final int KEY_Y = -9;
	private static final int MECHANIC_Y = -24;

	/** Orden visual de las ranuras y separación de grupos. */
	private static final int[] ORDER = {0, 1, 2, 3, AbilityManager.SIGNATURE_SLOT, AbilityManager.ULTIMATE_SLOT,
			AbilityManager.UNIQUE_SLOT};
	private static final boolean[] GAP_BEFORE = {false, false, true, false, true, false, true};

	private record SlotView(@Nullable RoleNode node, @Nullable UniqueSkill unique, boolean learned, int cost, int border,
							@Nullable Role role) {}

	private AbilityHud() {
	}

	/** Estado de las mecánicas de rol recibido del servidor. */
	private static List<MechanicPayload.Entry> mechanics = List.of();
	/** Animación de "lista": tick hasta el que brilla cada ranura tras terminar su enfriamiento. */
	private static final int[] READY_FLASH = new int[AbilityManager.SLOTS];
	private static final int[] LAST_COOLDOWN = new int[AbilityManager.SLOTS];
	private static final HudMotion.Fill HEALTH = new HudMotion.Fill();
	private static final HudMotion.Fill HEALTH_TRAIL = new HudMotion.Fill();
	private static final HudMotion.Fill MANA = new HudMotion.Fill();
	private static ClientPlayerEntity barsPlayer;

	public static void onMechanics(MechanicPayload payload) {
		mechanics = List.copyOf(payload.entries());
	}

	public static void clear() {
		mechanics = List.of();
		resetBars(null);
		KeystoneHud.clear();
	}

	private static void resetBars(ClientPlayerEntity player) {
		barsPlayer = player;
		HEALTH.clear();
		HEALTH_TRAIL.clear();
		MANA.clear();
		java.util.Arrays.fill(READY_FLASH, 0);
		java.util.Arrays.fill(LAST_COOLDOWN, 0);
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		RoleSyncPayload data = ClientRoleState.data();
		if (client.player == null || client.options.hudHidden || client.player.isSpectator()) return;
		if (data == null || !ClientConfig.get().hudVisible) return;
		TextRenderer text = client.textRenderer;
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();
		ClientConfig cfg = ClientConfig.get();

		if (barsPlayer != client.player) resetBars(client.player);
		long now = Util.getMeasuringTimeMs();
		float manaFill = MANA.update(ClientRoleState.energy() / Math.max(1f, ClientRoleState.maxEnergy()),
				now, cfg.hudAnimations ? 180 : 0);
		if (cfg.soulsBars) renderSoulsBars(ctx, text, data, client, now, manaFill);
		if (!hasAbilities(data)) return;
		if (!cfg.soulsBars) renderBars(ctx, text, data, w, h, manaFill);
		if (!(client.currentScreen instanceof HudConfigScreen)) renderAbilityBar(ctx, text, data, w, h, client.player.age);
	}

	/** Dibuja la barra y sus dos filas auxiliares con la misma escala y el mismo anclaje. */
	public static void renderAbilityBar(DrawContext ctx, TextRenderer text, RoleSyncPayload data, int w, int h, int age) {
		ClientConfig cfg = ClientConfig.get();
		AbilityHudLayout layout = barLayout(w, h);
		ctx.getMatrices().push();
		ctx.getMatrices().translate(layout.x(), layout.y(), 0);
		ctx.getMatrices().scale(layout.scale(), layout.scale(), 1f);
		renderSlots(ctx, text, data, age);
		if (cfg.showMechanic && !cfg.hudMinimal) renderMechanics(ctx, text);
		KeystoneHud.draw(ctx);
		ctx.getMatrices().pop();
	}

	/** Ancho total (sin escala) de la barra de habilidades. */
	public static int barWidth() {
		return ORDER.length * SLOT + (ORDER.length - 1) * GAP + 3 * GROUP_GAP;
	}

	@Nullable
	static AbilityHudLayout activeBarLayout(int w, int h) {
		var client = MinecraftClient.getInstance();
		var data = ClientRoleState.data();
		ClientConfig cfg = ClientConfig.get();
		if (client.player == null || client.player.isSpectator() || !cfg.hudVisible || data == null || !hasAbilities(data)) {
			return null;
		}
		var layout = barLayout(w, h);
		boolean showMechanic = cfg.showMechanic && !cfg.hudMinimal && !mechanics.isEmpty();
		int above = Math.max(showMechanic ? 1 - MECHANIC_Y : -KEY_Y, KeystoneHud.occupiedAbove());
		int top = layout.y() - (int) Math.ceil(above * layout.scale());
		return new AbilityHudLayout(layout.x(), layout.y(), layout.scale(), layout.left(), top, layout.right(), layout.bottom());
	}

	private static boolean hasAbilities(RoleSyncPayload data) {
		return data.primaryRole() != null || data.secondaryRole() != null || ownUnique() != null;
	}

	public static AbilityHudLayout barLayout(int w, int h) {
		ClientConfig cfg = ClientConfig.get();
		return barLayout(w, h, cfg.hudOffsetX, cfg.hudOffsetY);
	}

	public static AbilityHudLayout barLayout(int w, int h, int offsetX, int offsetY) {
		ClientConfig cfg = ClientConfig.get();
		return AbilityHudLayout.place(w, h, barWidth(), cfg.hudScale, offsetX, offsetY, cfg.preset(),
				ClientEclipse.active() && ClientEclipse.phase() > 0);
	}

	// ----------------------------------------------------- Barras Souls

	/**
	 * Barras arriba a la izquierda: icono + barra redondeada con degradado y brillo, valor
	 * dentro de la barra. La longitud crece con el máximo (como en Souls).
	 */
	private static void renderSoulsBars(DrawContext ctx, TextRenderer text, RoleSyncPayload data, MinecraftClient client,
										long now, float manaFill) {
		ClientConfig cfg = ClientConfig.get();
		var player = client.player;
		int age = player.age;
		ctx.getMatrices().push();
		ctx.getMatrices().translate(6, 6, 0);
		float scale = cfg.hudScale * cfg.preset().scaleFactor();
		ctx.getMatrices().scale(scale, scale, 1f);
		int y = 0;

		// Vida: trozo retrasado (daño reciente), absorción y latido con poca vida.
		float health = player.getHealth();
		float maxHealth = Math.max(1f, player.getMaxHealth());
		float healthFill = HEALTH.update(health / maxHealth, now, cfg.hudAnimations ? 160 : 0);
		float trail = HEALTH_TRAIL.update(health / maxHealth, now, cfg.hudAnimations ? 440 : 0);
		int hw = Math.min(200, 60 + (int) (maxHealth * 3));
		boolean low = health / maxHealth < 0.25f;
		icon(ctx, HEART, 0, y, low && (!cfg.hudAnimations || (age / 5) % 2 == 0) ? 0xFFFF8A80 : 0xFFE53935);
		pill(ctx, 12, y, hw, 9, 0f, 0, 0, true);
		if (trail > healthFill) pill(ctx, 12, y, hw, 9, trail, 0xFFFFF59D, 0xFFFFCA28, false);
		pill(ctx, 12, y, hw, 9, healthFill, 0xFFFF6E6E, 0xFFB71C1C, false);
		float absorption = player.getAbsorptionAmount();
		if (absorption > 0) {
			int aw = (int) ((hw - 2) * Math.min(1f, absorption / maxHealth));
			ctx.fill(13, y + 7, 13 + aw, y + 8, 0xFFFFD54F);
		}
		if (low) {
			int alpha = !cfg.hudAnimations || age % 20 < 10 ? 0xC0 : 0x60;
			ctx.drawBorder(11, y - 1, hw + 2, 11, (alpha << 24) | 0xFF1744);
		}
		centered(ctx, text, (int) Math.ceil(health) + " / " + (int) maxHealth, 12, y + 2, hw);
		y += 12;

		// Maná
		float mana = ClientRoleState.energy();
		float maxMana = Math.max(1f, ClientRoleState.maxEnergy());
		int mw = Math.min(200, 50 + (int) (maxMana * 0.8f));
		icon(ctx, GEM, 0, y, 0xFF42A5F5);
		pill(ctx, 12, y, mw, 8, manaFill, 0xFF82CFFF, 0xFF1565C0, true);
		centered(ctx, text, (int) mana + " / " + (int) maxMana, 12, y + 1, mw);
		y += 11;

		// Aguante (hambre: en Minecraft es lo que limita esprintar)
		int food = player.getHungerManager().getFoodLevel();
		icon(ctx, BOLT, 0, y - 2, 0xFF9CCC65);
		pill(ctx, 12, y, 90, 4, food / 20f, 0xFFC5E1A5, 0xFF558B2F, true);
		y += 8;

		// Recursos de rol: píldoras finas con el color del recurso; brillan al poder potenciar.
		for (Role role : new Role[]{data.primaryRole(), data.secondaryRole()}) {
			if (role == null) continue;
			RoleResource resource = RoleResource.of(role);
			float value = ClientRoleState.resource(role == data.primaryRole());
			int color = 0xFF000000 | resource.color();
			boolean ready = value >= RoleResource.EMPOWER_COST;
			ctx.fill(2, y, 8, y + 4, ready && (!cfg.hudAnimations || (age / 4) % 2 == 0) ? 0xFFFFE082 : color);
			pill(ctx, 12, y, 70, 4, value / RoleResource.MAX, lighter(color), darker(color), true);
			small(ctx, text, Text.translatable(resource.translationKey()), 86, y - 1, ready ? 0xFFFFE082 : color);
			y += 7;
		}
		// Elemento activo
		Element element = data.activeElementValue();
		if (element != null && (data.primaryRole() == Role.ELEMENTALIST || data.secondaryRole() == Role.ELEMENTALIST)) {
			small(ctx, text, Text.translatable("hud.aptitudes.element", Text.translatable(element.translationKey())),
					12, y + 1, 0xFF000000 | element.color());
		}
		ctx.getMatrices().pop();
	}

	/** Barra redondeada: fondo oscuro, relleno con degradado vertical y brillo superior. */
	private static void pill(DrawContext ctx, int x, int y, int width, int height, float fraction, int light, int dark,
							 boolean background) {
		if (background) {
			ctx.fill(x + 1, y, x + width - 1, y + height, 0xB0000000);
			ctx.fill(x, y + 1, x + width, y + height - 1, 0xB0000000);
		}
		int filled = (int) ((width - 2) * Math.max(0, Math.min(1, fraction)));
		if (filled <= 0) return;
		ctx.fillGradient(x + 1, y + 1, x + 1 + filled, y + height - 1, light, dark);
		if (height >= 6) ctx.fill(x + 2, y + 1, x + filled, y + 2, 0x60FFFFFF); // brillo
	}

	private static void centered(DrawContext ctx, TextRenderer text, String value, int x, int y, int width) {
		int w = (int) (text.getWidth(value) * 0.7f);
		small(ctx, text, Text.literal(value), x + (width - w) / 2, y, 0xFFFFFFFF);
	}

	/** Iconos de 9×9 en pixel art (corazón, gema, rayo). */
	private static final String[] HEART = {".XX.XX...", "XXXXXXX..", "XXXXXXX..", ".XXXXX...", "..XXX....", "...X....."};
	private static final String[] GEM = {"..XXX....", ".XXXXX...", "XXXXXXX..", ".XXXXX...", "..XXX....", "...X....."};
	private static final String[] BOLT = {"...XX....", "..XX.....", ".XXXX....", "...XX....", "..XX.....", ".X......."};

	private static void icon(DrawContext ctx, String[] shape, int x, int y, int color) {
		for (int row = 0; row < shape.length; row++) {
			for (int col = 0; col < shape[row].length(); col++) {
				if (shape[row].charAt(col) == 'X') {
					ctx.fill(x + col + 1, y + row + 1, x + col + 2, y + row + 2, 0xA0000000); // sombra
					ctx.fill(x + col, y + row, x + col + 1, y + row + 1, color);
				}
			}
		}
	}

	private static int lighter(int color) {
		int r = Math.min(255, (color >> 16 & 0xFF) + 70);
		int g = Math.min(255, (color >> 8 & 0xFF) + 70);
		int b = Math.min(255, (color & 0xFF) + 70);
		return 0xFF000000 | r << 16 | g << 8 | b;
	}

	// --------------------------------------------------------- Mecánica

	/** Iconos de la mecánica sobre la barra: pips de acumulación o el estado con nombre. */
	private static void renderMechanics(DrawContext ctx, TextRenderer text) {
		int x = 0;
		int y = MECHANIC_Y;
		for (MechanicPayload.Entry e : mechanics) {
			if (x + 22 >= barWidth()) break;
			RoleNode node = RoleTree.get(e.node());
			if (node == null) continue;
			int color = 0xFF000000 | node.role().color();
			ctx.fill(x - 1, y - 1, x + 13, y + 13, 0xC0000000);
			ctx.drawBorder(x - 1, y - 1, 14, 14, color);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(x, y, 0);
			ctx.getMatrices().scale(0.75f, 0.75f, 1f);
			NodeIconRenderer.draw(ctx, node, 0, 0);
			ctx.getMatrices().pop();
			int px = x + 16;
			int available = barWidth() - px;
			if (e.max() > 0 && e.max() <= available / 7) {
				for (int i = 0; i < e.max(); i++) {
					boolean on = i < e.value();
					int c = on ? (e.value() >= e.max() ? 0xFFFFE082 : color) : 0xFF2A2A34;
					ctx.fill(px + 1, y + 3, px + 4, y + 9, c);
					ctx.fill(px, y + 4, px + 5, y + 8, c);
					px += 7;
				}
			} else {
				Text label = e.max() > 0 ? Text.literal(e.value() + "/" + e.max())
						: e.label().isEmpty() ? Text.empty()
						: e.value() > 0 ? Text.translatable(e.label(), e.value()) : Text.translatable(e.label());
				Text fitted = UiStyle.fit(text, label, (int) (available / 0.7f));
				small(ctx, text, fitted, px, y + 4, color);
				px += (int) (text.getWidth(fitted) * 0.7f) + 2;
			}
			x = px + 6;
		}
	}

	// ------------------------------------------------------------ Barras

	private static void renderBars(DrawContext ctx, TextRenderer text, RoleSyncPayload data, int w, int h, float manaFill) {
		int right = w / 2 - 91 - 30; // deja sitio a la mano secundaria
		int x = Math.max(4, right - BAR_W);
		int y = h - 22;

		// Maná
		float mana = ClientRoleState.energy();
		float max = Math.max(1f, ClientRoleState.maxEnergy());
		bar(ctx, x, y, BAR_W, 9, manaFill, 0xFF2D7DDB, 0xFF5AB0FF);
		small(ctx, text, Text.translatable("hud.aptitudes.mana", (int) mana, (int) max), x + 3, y + 2, 0xFFFFFFFF);

		// Recursos de rol
		int ry = y + 11;
		for (Role role : new Role[]{data.primaryRole(), data.secondaryRole()}) {
			if (role == null) continue;
			RoleResource resource = RoleResource.of(role);
			float value = ClientRoleState.resource(role == data.primaryRole());
			int color = 0xFF000000 | resource.color();
			bar(ctx, x, ry, BAR_W, 5, value / RoleResource.MAX, darker(color), color);
			if (value >= RoleResource.EMPOWER_COST) ctx.drawBorder(x - 1, ry - 1, BAR_W + 2, 7, 0xFFFFE082);
			Text label = Text.translatable(resource.translationKey());
			small(ctx, text, label, x - 3 - (int) (text.getWidth(label) * 0.7f), ry, color);
			ry += 6;
		}

		// Elemento activo del Elementalista
		Element element = data.activeElementValue();
		if (element != null && (data.primaryRole() == Role.ELEMENTALIST || data.secondaryRole() == Role.ELEMENTALIST)) {
			small(ctx, text, Text.translatable("hud.aptitudes.element", Text.translatable(element.translationKey())),
					x, y - 8, 0xFF000000 | element.color());
		}
	}

	private static void bar(DrawContext ctx, int x, int y, int width, int height, float fraction, int dark, int light) {
		ctx.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xC0000000);
		ctx.fill(x, y, x + width, y + height, 0xFF1A1D28);
		int filled = (int) (width * Math.max(0, Math.min(1, fraction)));
		ctx.fill(x, y, x + filled, y + height, dark);
		ctx.fill(x, y, x + filled, y + Math.max(1, height / 2), light);
	}

	private static int darker(int color) {
		int r = (color >> 16 & 0xFF) * 6 / 10;
		int g = (color >> 8 & 0xFF) * 6 / 10;
		int b = (color & 0xFF) * 6 / 10;
		return 0xFF000000 | r << 16 | g << 8 | b;
	}

	// ----------------------------------------------------------- Ranuras

	private static void renderSlots(DrawContext ctx, TextRenderer text, RoleSyncPayload data, int age) {
		int x = 0;
		int y = 0;

		for (int i = 0; i < ORDER.length; i++) {
			int slot = ORDER[i];
			if (GAP_BEFORE[i]) x += GROUP_GAP;
			SlotView view = view(data, slot);
			drawSlot(ctx, text, data, view, slot, x, y, age);
			x += SLOT + GAP;
		}
	}

	@Nullable
	private static SlotView view(RoleSyncPayload data, int slot) {
		if (slot == AbilityManager.UNIQUE_SLOT) {
			UniqueSkill skill = ownUnique();
			return skill == null ? null : new SlotView(null, skill, true, skill.energyCost(), 0xFFB388FF, null);
		}
		if (slot == AbilityManager.SIGNATURE_SLOT) {
			Role role = signatureRole(data);
			return role == null ? null : new SlotView(null, null, true, Signatures.ENERGY, 0xFFE6C36A, role);
		}
		if (slot == AbilityManager.ULTIMATE_SLOT) {
			Role role = ultimateRole(data);
			return role == null ? null
					: new SlotView(null, null, true, AbilityManager.ULTIMATE_ENERGY, 0xFFFFC107, role);
		}
		Role role = slot < 2 ? data.primaryRole() : data.secondaryRole();
		if (role == null) return null;
		RoleNode node = RoleTree.active(role, data.equipped(role, slot % 2));
		if (node == null) return null;
		return new SlotView(node, null, data.rank(node.id()) > 0, node.energyCost(), 0, role);
	}

	private static void drawSlot(DrawContext ctx, TextRenderer text, RoleSyncPayload data, @Nullable SlotView view,
								 int slot, int x, int y, int age) {
		long castAge = ClientRoleState.sinceCast(slot);
		// v66: marco de la dirección de arte (categoría del rol y estado de la casilla); si falta, el dibujo anterior.
		if (view == null || !drawFrame(ctx, view, slot, x, y, castAge)) {
			int accent = view == null ? 0xFF565260 : view.border() != 0 ? view.border()
					: lighter(0xFF000000 | view.role().color());
			ctx.fill(x - 1, y - 1, x + SLOT + 1, y + SLOT + 1, 0xD0000000);
			ctx.fillGradient(x, y, x + SLOT, y + SLOT, 0xF02B2734, 0xEF12151F);
			ctx.drawBorder(x, y, SLOT, SLOT, 0xFF4D4655);
			int corner = view != null && view.learned() ? accent : darker(accent);
			ctx.fill(x - 1, y - 1, x + 3, y, corner);
			ctx.fill(x - 1, y - 1, x, y + 3, corner);
			ctx.fill(x + SLOT - 3, y - 1, x + SLOT + 1, y, corner);
			ctx.fill(x + SLOT, y - 1, x + SLOT + 1, y + 3, corner);
			ctx.fill(x - 1, y + SLOT, x + 3, y + SLOT + 1, corner);
			ctx.fill(x - 1, y + SLOT - 3, x, y + SLOT + 1, corner);
			ctx.fill(x + SLOT - 3, y + SLOT, x + SLOT + 1, y + SLOT + 1, corner);
			ctx.fill(x + SLOT, y + SLOT - 3, x + SLOT + 1, y + SLOT + 1, corner);
		}
		Text key = UiStyle.fit(text, AptitudesClient.abilityKey(slot).getBoundKeyLocalizedText(), (int) ((SLOT - 2) / 0.7f));
		ctx.fill(x + 1, y + KEY_Y, x + SLOT - 1, y - 1, 0xEC151620);
		small(ctx, text, key, x + SLOT / 2 - (int) (text.getWidth(key) * 0.35f), y - 8,
				view == null ? UiStyle.MUTED : UiStyle.TEXT);
		if (view == null) return;
		// v24: al lanzar, el icono rebota (×1,35 → ×1) y la ranura destella con el color del rol (350 ms).
		long since = castAge;
		if (ClientConfig.get().hudAnimations && since < 350) {
			float t = since / 350f;
			float scale = 1f + 0.35f * (1f - t) * (1f - t);
			int rgb = view.role() != null ? view.role().color() : 0xB388FF;
			int alpha = (int) (200 * (1f - t));
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, (alpha << 24) | (rgb & 0xFFFFFF));
			int grow = (int) (t * 5);
			ctx.drawBorder(x - 1 - grow, y - 1 - grow, SLOT + 2 + grow * 2, SLOT + 2 + grow * 2,
					((int) (255 * (1f - t)) << 24) | (rgb & 0xFFFFFF));
			ctx.getMatrices().push();
			ctx.getMatrices().translate(x + SLOT / 2f, y + SLOT / 2f, 0);
			ctx.getMatrices().scale(scale, scale, 1f);
			drawSlotIcon(ctx, view, slot, -8, -8);
			ctx.getMatrices().pop();
		} else {
			drawSlotIcon(ctx, view, slot, x + 2, y + 2);
		}

		ctx.getMatrices().push();
		ctx.getMatrices().translate(0, 0, 200);
		if (view.learned()) {
			drawStatus(ctx, text, data, view, slot, x, y, age);
		} else {
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0xB0101010);
			padlock(ctx, x + SLOT / 2 - 3, y + 6);
		}
		ctx.getMatrices().pop();
	}

	/** Dibuja el marco de la casilla; falso si la categoría o el sprite no están disponibles. */
	private static boolean drawFrame(DrawContext ctx, SlotView view, int slot, int x, int y, long castAge) {
		Role owner = view.role() != null ? view.role() : view.unique() != null ? view.unique().role() : null;
		if (owner == null) return false;
		String state;
		if (ClientConfig.get().hudAnimations && castAge < 350) state = "seleccionada";
		else if (ClientRoleState.cooldown(slot) > 0) state = "espera";
		else if (slot == AbilityManager.ULTIMATE_SLOT) state = "definitiva";
		else if (ClientRoleState.energy() < view.cost()) state = "sin_energia";
		else state = "lista";
		return NodeIconRenderer.drawSlotFrame(ctx, owner.category(), state, x, y, SLOT);
	}

	private static void drawSlotIcon(DrawContext ctx, SlotView view, int slot, int x, int y) {
		if (view.node() != null) {
			NodeIconRenderer.draw(ctx, view.node(), x, y);
		} else if (view.unique() != null) {
			RoleNode legend = RoleTree.get(view.unique().role().key() + "_legend");
			if (legend != null) NodeIconRenderer.draw(ctx, legend, x, y);
			else ctx.drawItem(view.unique().iconStack(), x, y);
		} else {
			NodeIconRenderer.drawRole(ctx, view.role(), slot == AbilityManager.SIGNATURE_SLOT
					? NodeIconRenderer.Badge.SIGNATURE : NodeIconRenderer.Badge.ULTIMATE, x, y);
		}
	}

	private static void drawStatus(DrawContext ctx, TextRenderer text, RoleSyncPayload data, SlotView view,
								   int slot, int x, int y, int age) {
		int cooldown = ClientRoleState.cooldown(slot);
		// Destello al quedar lista
		if (LAST_COOLDOWN[slot] > 0 && cooldown == 0) READY_FLASH[slot] = age + 8;
		LAST_COOLDOWN[slot] = cooldown;
		if (ClientConfig.get().hudAnimations && READY_FLASH[slot] > age) {
			int alpha = (READY_FLASH[slot] - age) * 24;
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, (Math.min(200, alpha) << 24) | 0xFFF3C0);
			ctx.drawBorder(x - 2, y - 2, SLOT + 4, SLOT + 4, (Math.min(255, alpha + 60) << 24) | 0xFFE082);
		}
		if (cooldown > 0) {
			int filled = (int) ((SLOT - 2) * ClientRoleState.cooldownFraction(slot));
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + 1 + filled, 0xB0000000);
			String secs = String.valueOf((cooldown + 19) / 20);
			ctx.drawTextWithShadow(text, secs, x + (SLOT - text.getWidth(secs)) / 2, y + 6, 0xFFFFFFFF);
		} else if (ClientRoleState.energy() < view.cost()) {
			ctx.drawBorder(x, y, SLOT, SLOT, 0xFF537AC8); // falta maná
		} else if (view.role() != null && empowerReady(data, view.role())) {
			int pulse = !ClientConfig.get().hudAnimations || (age / 4) % 2 == 0 ? 0xFFFFE082 : 0xFFFFB300;
			ctx.drawBorder(x - 1, y - 1, SLOT + 2, SLOT + 2, pulse); // se potenciará
		}
		String cost = String.valueOf(view.cost());
		int costX = x + SLOT - 2 - (int) (text.getWidth(cost) * 0.7f);
		ctx.fill(costX - 1, y + SLOT - 7, x + SLOT - 1, y + SLOT - 1, 0xE0121520);
		small(ctx, text, Text.literal(cost), costX, y + SLOT - 7, 0xFFA9DCFF);
	}

	private static boolean empowerReady(RoleSyncPayload data, Role role) {
		return ClientRoleState.resource(role == data.primaryRole()) >= RoleResource.EMPOWER_COST;
	}

	/** Candado dibujado con rectángulos (la fuente de Minecraft no tiene emojis). */
	private static void padlock(DrawContext ctx, int x, int y) {
		int c = 0xFFB0B0B0;
		ctx.fill(x + 1, y, x + 5, y + 1, c);
		ctx.fill(x, y + 1, x + 1, y + 4, c);
		ctx.fill(x + 5, y + 1, x + 6, y + 4, c);
		ctx.fill(x - 1, y + 4, x + 7, y + 9, c);
		ctx.fill(x + 2, y + 5, x + 4, y + 7, 0xFF303030);
	}

	/** Texto al 70 % de tamaño. */
	private static void small(DrawContext ctx, TextRenderer text, Text value, int x, int y, int color) {
		ctx.getMatrices().push();
		ctx.getMatrices().translate(x, y, 0);
		ctx.getMatrices().scale(0.7f, 0.7f, 1f);
		ctx.drawText(text, value, 0, 0, color, true);
		ctx.getMatrices().pop();
	}

	// ------------------------------------------------------------ Datos

	@Nullable
	private static UniqueSkill ownUnique() {
		UniqueSyncPayload uniques = ClientExtraState.uniques();
		return uniques == null ? null : UniqueSkill.byIndex(uniques.ownSkill());
	}

	@Nullable
	private static Role signatureRole(RoleSyncPayload data) {
		for (Role role : new Role[]{data.primaryRole(), data.secondaryRole()}) {
			if (role != null && data.tier(role) >= Evolution.MASTER && !Signatures.isPassive(role)) return role;
		}
		return null;
	}

	@Nullable
	private static Role ultimateRole(RoleSyncPayload data) {
		Role primary = data.primaryRole();
		if (primary != null && data.tier(primary) >= Evolution.ULTIMATE) return primary;
		Role secondary = data.secondaryRole();
		if (secondary != null && data.tier(secondary) >= Evolution.ULTIMATE) return secondary;
		return null;
	}
}
