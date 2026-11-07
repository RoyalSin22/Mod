package dev.aptitudes.client.screen;

import dev.aptitudes.role.Role;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/** Paleta de presentación del atlas. El acento identifica al rol sin sustituir las marcas de estado. */
record RoleAtlasTheme(int accent, int slate) {
	static RoleAtlasTheme of(Role role) {
		return switch (role) {
			case BERSERKER -> new RoleAtlasTheme(0xFFF3A087, 0xFF271A1D);
			case MAGE -> new RoleAtlasTheme(0xFFC1A7FF, 0xFF201B30);
			case ASSASSIN -> new RoleAtlasTheme(0xFFB0BBD4, 0xFF1A202A);
			case HERBALIST -> new RoleAtlasTheme(0xFF9DDCB9, 0xFF182720);
			case ENGINEER -> new RoleAtlasTheme(0xFFF1AB78, 0xFF2A211A);
			case LANCER -> new RoleAtlasTheme(0xFF8FBFF7, 0xFF17243A);
			case SWORDSMAN -> new RoleAtlasTheme(0xFF9EC4E2, 0xFF192631);
			case PALADIN -> new RoleAtlasTheme(0xFFF5D883, 0xFF292519);
			case NECROMANCER -> new RoleAtlasTheme(0xFFBA9DDA, 0xFF211B28);
			case DRUID -> new RoleAtlasTheme(0xFFC0D493, 0xFF23271B);
			case MERCHANT -> new RoleAtlasTheme(0xFF8DDFC8, 0xFF172B28);
			case MONK -> new RoleAtlasTheme(0xFFEFC689, 0xFF30251A);
			case ELEMENTALIST -> new RoleAtlasTheme(0xFF9CD8FE, 0xFF172533);
			case SHAPESHIFTER -> new RoleAtlasTheme(0xFFE2A8E0, 0xFF2A1D2B);
			case GUNSLINGER -> new RoleAtlasTheme(0xFF89DDE8, 0xFF17282B);
			case WEAPON_SUMMONER -> new RoleAtlasTheme(0xFFC4CFDB, 0xFF20252D);
			case HUNTER -> new RoleAtlasTheme(0xFFABD38B, 0xFF202918);
			case BARD -> new RoleAtlasTheme(0xFFD6A5E5, 0xFF2B1D30);
			case WARLOCK -> new RoleAtlasTheme(0xFFEB95B3, 0xFF2C1B27);
			case TAMER -> new RoleAtlasTheme(0xFF8DC99F, 0xFF1B2820);
		};
	}

	void panel(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, slate);
		ctx.drawBorder(x, y, w, h, UiStyle.BORDER);
		int rim = 0x80000000 | (accent & 0xFFFFFF);
		ctx.fill(x + 1, y + 1, x + 10, y + 2, rim);
		ctx.fill(x + w - 10, y + 1, x + w - 1, y + 2, rim);
	}

	/** La pestaña actual conserva el foco de teclado y se distingue de una acción deshabilitada. */
	static final class Tab extends ButtonWidget {
		private final BooleanSupplier current;
		private final IntSupplier accent;

		Tab(int x, int y, int width, Text text, PressAction action, BooleanSupplier current, IntSupplier accent) {
			super(x, y, width, 16, text, action, DEFAULT_NARRATION_SUPPLIER);
			this.current = current;
			this.accent = accent;
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
			int x = getX(), y = getY(), w = getWidth();
			int color = accent.getAsInt();
			boolean selected = current.getAsBoolean();
			ctx.fill(x, y, x + w, y + getHeight(), selected ? 0xFF2D2D39 : 0xFF191C26);
			ctx.drawBorder(x, y, w, getHeight(), isFocused() ? UiStyle.TEXT : isHovered() ? color : UiStyle.BORDER);
			if (selected) ctx.fill(x + 2, y + 1, x + w - 2, y + 3, color);
			ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
					x + w / 2, y + 5, selected ? color : UiStyle.TEXT);
		}
	}
}
