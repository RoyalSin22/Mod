package dev.aptitudes.client.hud;

import dev.aptitudes.client.PartyClientState;
import dev.aptitudes.network.PartySyncPayload;
import dev.aptitudes.role.Role;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/** Marcos del grupo en el margen izquierdo; datos procedentes solo de la sincronización del servidor. */
public final class PartyHud {
	private PartyHud() {
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) return;
		PartySyncPayload view = PartyClientState.get();
		if (view == null || !view.grouped()) return;
		int x = 7;
		int y = 8;
		for (PartySyncPayload.Member member : view.members()) {
			boolean dimmed = !member.online() || !member.sameDimension();
			int text = dimmed ? 0xFF929292 : 0xFFFFFFFF;
			context.fill(x, y, x + 142, y + 27, dimmed ? 0x78141A22 : 0xA8141A22);
			context.fill(x, y + 26, x + 142, y + 27, 0xFF52677D);
			Role role = Role.byIndex(member.role());
			if (role != null) {
				context.drawItem(role.iconStack(), x + 3, y + 4);
				if (dimmed) context.fill(x + 2, y + 3, x + 20, y + 21, 0x88666666);
			}
			String name = (member.leader() ? "★ " : "") + member.name();
			if (name.length() > 14) name = name.substring(0, 13) + "…";
			context.drawTextWithShadow(client.textRenderer, Text.literal(name), x + 22, y + 4, text);
			if (member.online() && member.sameDimension()) {
				context.fill(x + 22, y + 16, x + 135, y + 22, 0xFF412525);
				context.fill(x + 22, y + 16, x + 22 + Math.round(113 * member.health()), y + 22, 0xFF55BD62);
			} else {
				context.drawTextWithShadow(client.textRenderer,
						Text.translatable(member.online() ? "party.hud.other_dimension" : "party.hud.offline"),
						x + 22, y + 15, 0xFF999999);
			}
			y += 30;
		}
	}
}
