package dev.aptitudes.client.hud;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.Signatures;
import dev.aptitudes.client.AptitudesClient;
import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientExtraState;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.network.UniqueSyncPayload;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.NodeIcons;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleResource;
import dev.aptitudes.role.RoleTree;
import dev.aptitudes.unique.UniqueSkill;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

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

	/** Orden visual de las ranuras y separación de grupos. */
	private static final int[] ORDER = {0, 1, 2, 3, AbilityManager.SIGNATURE_SLOT, AbilityManager.ULTIMATE_SLOT,
			AbilityManager.UNIQUE_SLOT};
	private static final boolean[] GAP_BEFORE = {false, false, true, false, true, false, true};

	private record SlotView(ItemStack icon, boolean learned, int cost, int border, @Nullable Role role) {}

	private AbilityHud() {
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		RoleSyncPayload data = ClientRoleState.data();
		if (client.player == null || client.options.hudHidden || client.player.isSpectator()) return;
		if (data == null || !ClientConfig.get().hudVisible) return;
		if (data.primaryRole() == null && ownUnique() == null) return;
		TextRenderer text = client.textRenderer;
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();

		renderBars(ctx, text, data, w, h);
		renderSlots(ctx, text, data, w, h, client.player.age);
	}

	// ------------------------------------------------------------ Barras

	private static void renderBars(DrawContext ctx, TextRenderer text, RoleSyncPayload data, int w, int h) {
		int right = w / 2 - 91 - 30; // deja sitio a la mano secundaria
		int x = Math.max(4, right - BAR_W);
		int y = h - 22;

		// Maná
		float mana = ClientRoleState.energy();
		float max = ClientRoleState.maxEnergy();
		bar(ctx, x, y, BAR_W, 9, mana / max, 0xFF2D7DDB, 0xFF5AB0FF);
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

	private static void renderSlots(DrawContext ctx, TextRenderer text, RoleSyncPayload data, int w, int h, int age) {
		int total = 0;
		for (int i = 0; i < ORDER.length; i++) total += SLOT + GAP + (GAP_BEFORE[i] ? GROUP_GAP : 0);
		int x = w / 2 + 91 + 12;
		int y = h - SLOT - 1;
		if (x + total > w - 2) { // pantalla estrecha: encima de la hotbar, a la derecha
			x = w - total - 2;
			y = h - SLOT - 46;
		}

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
			return skill == null ? null : new SlotView(skill.iconStack(), true, skill.energyCost(), 0xFFB388FF, null);
		}
		if (slot == AbilityManager.SIGNATURE_SLOT) {
			Role role = signatureRole(data);
			return role == null ? null : new SlotView(NodeIcons.signature(role), true, Signatures.ENERGY, 0xFFE6C36A, role);
		}
		if (slot == AbilityManager.ULTIMATE_SLOT) {
			Role role = ultimateRole(data);
			return role == null ? null
					: new SlotView(NodeIcons.ultimate(role), true, AbilityManager.ULTIMATE_ENERGY, 0xFFFFC107, role);
		}
		Role role = slot < 2 ? data.primaryRole() : data.secondaryRole();
		if (role == null) return null;
		RoleNode node = RoleTree.active(role, slot % 2);
		if (node == null) return null;
		return new SlotView(NodeIcons.of(node), data.rank(node.id()) > 0, node.energyCost(), 0, role);
	}

	private static void drawSlot(DrawContext ctx, TextRenderer text, RoleSyncPayload data, @Nullable SlotView view,
								 int slot, int x, int y, int age) {
		// Marco tipo hotbar
		ctx.fill(x - 1, y - 1, x + SLOT + 1, y + SLOT + 1, 0xC0000000);
		ctx.fill(x, y, x + SLOT, y + SLOT, 0xA0303648);
		ctx.drawBorder(x, y, SLOT, SLOT, 0xFF5A6078);
		Text key = AptitudesClient.abilityKey(slot).getBoundKeyLocalizedText();
		small(ctx, text, key, x + SLOT / 2 - (int) (text.getWidth(key) * 0.35f), y - 7, view == null ? 0xFF707070 : 0xFFE0E0E0);
		if (view == null) return;

		if (view.border() != 0) ctx.drawBorder(x - 1, y - 1, SLOT + 2, SLOT + 2, view.border());
		ctx.drawItem(view.icon(), x + 2, y + 2);

		if (!view.learned()) {
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0xB0101010);
			padlock(ctx, x + SLOT / 2 - 3, y + 6);
			return;
		}
		int cooldown = ClientRoleState.cooldown(slot);
		if (cooldown > 0) {
			int filled = (int) ((SLOT - 2) * ClientRoleState.cooldownFraction(slot));
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + 1 + filled, 0xB0000000);
			String secs = String.valueOf((cooldown + 19) / 20);
			ctx.drawTextWithShadow(text, secs, x + (SLOT - text.getWidth(secs)) / 2, y + 6, 0xFFFFFFFF);
		} else if (ClientRoleState.energy() < view.cost()) {
			ctx.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0x702050FF); // falta maná
		} else if (view.role() != null && empowerReady(data, view.role())) {
			int pulse = (age / 4) % 2 == 0 ? 0xFFFFE082 : 0xFFFFB300;
			ctx.drawBorder(x - 1, y - 1, SLOT + 2, SLOT + 2, pulse); // se potenciará
		}
		String cost = String.valueOf(view.cost());
		small(ctx, text, Text.literal(cost), x + SLOT - 1 - (int) (text.getWidth(cost) * 0.7f), y + SLOT - 6, 0xFF8FD3FF);
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
