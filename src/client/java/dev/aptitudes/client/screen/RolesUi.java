package dev.aptitudes.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.KScreenLayout;
import dev.aptitudes.logic.KScreenLayout.Tile;
import dev.aptitudes.role.RoleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Piezas de la pantalla de roles (J) de la dirección de arte (generadas por {@code tools/textures/gen_roles_ui.py}).
 * Botones, pestañas y filas son 9-slice con bordes de 6 px: los bordes se copian y los tramos centrales se repiten,
 * nunca se estiran, para conservar el píxel lógico de 2×2. Si el paquete de recursos no trae las piezas, la pantalla
 * usa el dibujo anterior.
 */
final class RolesUi {
	private static final String BASE = "textures/gui/roles_ui/";
	static final int EDGE = 6;

	private RolesUi() {
	}

	static Identifier id(String name) {
		return Aptitudes.id(BASE + name);
	}

	static boolean available() {
		var manager = MinecraftClient.getInstance().getResourceManager();
		return manager.getResource(id("boton_largo_normal.png")).isPresent()
				&& manager.getResource(id("pestana_activa.png")).isPresent()
				&& manager.getResource(id("fila_seleccionada.png")).isPresent()
				&& manager.getResource(id("categoria_marcial_activa.png")).isPresent()
				&& manager.getResource(id("divisor_base.png")).isPresent();
	}

	static String categoryKey(RoleCategory category) {
		return switch (category) {
			case MARTIAL -> "marcial";
			case ARCANE -> "arcano";
			case SHADOW -> "sombra";
			case GUARDIAN -> "guardian";
			case CRAFT -> "oficio";
		};
	}

	/** Pieza de 9 partes ajustada a {@code w}×{@code h} con bordes de {@link #EDGE} px. */
	static void slice9(DrawContext ctx, String name, int texW, int texH, int x, int y, int w, int h) {
		if (w <= 0 || h <= 0) return;
		Identifier t = id(name);
		List<Tile> cols = KScreenLayout.tiles(w, EDGE, EDGE, texW);
		List<Tile> rows = KScreenLayout.tiles(h, EDGE, EDGE, texH);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			for (Tile row : rows) {
				for (Tile col : cols) {
					ctx.drawTexture(t, x + col.dest(), y + row.dest(), (float) col.src(), (float) row.src(), col.len(),
							row.len(), texW, texH);
				}
			}
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Pieza completa a su tamaño real. */
	static void sprite(DrawContext ctx, String name, int x, int y, int w, int h) {
		slice9(ctx, name, w, h, x, y, w, h);
	}

	static void button(DrawContext ctx, String state, int x, int y, int w, int h) {
		slice9(ctx, "boton_largo_" + state + ".png", 48, 18, x, y, w, h);
	}

	static void tab(DrawContext ctx, String state, int x, int y, int w, int h) {
		slice9(ctx, "pestana_" + state + ".png", 48, 16, x, y, w, h);
	}

	static void row(DrawContext ctx, boolean selected, int x, int y, int w, int h) {
		slice9(ctx, selected ? "fila_seleccionada.png" : "fila_normal.png", 48, 18, x, y, w, h);
	}

	static void category(DrawContext ctx, RoleCategory category, boolean active, int x, int y) {
		sprite(ctx, "categoria_" + categoryKey(category) + "_" + (active ? "activa" : "normal") + ".png", x, y, 24, 14);
	}

	/** Filete con rombo central: la banda se ajusta al ancho y el rombo de 8×6 se dibuja encima, sin deformarse. */
	static void divider(DrawContext ctx, int x, int y, int w) {
		slice9(ctx, "divisor_base.png", 48, 6, x, y, w, 6);
		if (w >= 8) sprite(ctx, "divisor_rombo.png", x + (w - 8) / 2 / 2 * 2, y, 8, 6);
	}

	/** Franja de título con extremos afilados, bajo el título de la ventana. */
	static void titleStrip(DrawContext ctx, int x, int y, int w) {
		slice9(ctx, "titulo_franja.png", 96, 10, x, y, w, 10);
	}
}
