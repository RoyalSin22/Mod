package dev.aptitudes.client.screen;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.KeystoneRules;
import dev.aptitudes.role.NodeIcons;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Iconos propios del árbol, con respaldo al objeto si el paquete de recursos no aporta el icono. */
public final class NodeIconRenderer {
	public enum Badge { CORE, SIGNATURE, ULTIMATE }

	private static final int ATLAS_SIZE = 512;
	private static final int TILE = 16;
	private static final int COLUMNS = ATLAS_SIZE / TILE;
	private static final Identifier ATLAS = Aptitudes.id("textures/gui/tree/atlas.png");
	private static final Identifier INDEX = Aptitudes.id("textures/gui/tree/atlas.json");
	private static final Map<String, Boolean> HAS_TEXTURE = new HashMap<>();
	private static Map<String, Integer> atlas;
	private static final String[] KEYSTONES = java.util.Arrays.stream(KeystoneRules.Template.values())
			.map(template -> "keystone/" + template.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);

	static {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return Aptitudes.id("node_icon_cache");
					}

					@Override
					public void reload(ResourceManager manager) {
						HAS_TEXTURE.clear();
						atlas = null;
					}
				});
	}

	private NodeIconRenderer() {
	}

	private static Map<String, Integer> readAtlas() {
		ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
		var index = manager.getResource(INDEX);
		if (index.isEmpty() || manager.getResource(ATLAS).isEmpty()) return Map.of();
		try (var reader = index.get().getReader()) {
			Map<String, Integer> sprites = new HashMap<>();
			for (var entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
				int value = entry.getValue().getAsInt();
				if (value >= 0 && value < COLUMNS * COLUMNS) sprites.put(entry.getKey(), value);
			}
			return sprites;
		} catch (IOException | RuntimeException e) {
			Aptitudes.LOGGER.warn("No se pudo leer el atlas del árbol de roles", e);
			return Map.of();
		}
	}

	private static boolean drawSprite(DrawContext ctx, String key, int x, int y) {
		return drawSprite(ctx, key, x, y, TILE);
	}

	private static boolean drawSprite(DrawContext ctx, String key, int x, int y, int size) {
		if (atlas == null) atlas = readAtlas();
		Integer index = atlas.get(key);
		if (index == null) return false;
		ctx.drawTexture(ATLAS, x, y, size, size, (index % COLUMNS) * TILE, (index / COLUMNS) * TILE,
				TILE, TILE, ATLAS_SIZE, ATLAS_SIZE);
		return true;
	}

	public static void draw(DrawContext ctx, RoleNode node, int x, int y) {
		if (node.kind() == RoleNode.Kind.PATH) {
			Identifier texture = Aptitudes.id("textures/gui/node/" + node.id() + ".png");
			boolean present = HAS_TEXTURE.computeIfAbsent(node.id(), id ->
					MinecraftClient.getInstance().getResourceManager().getResource(texture).isPresent());
			if (present) {
				ctx.drawTexture(texture, x, y, 0.0F, 0.0F, TILE, TILE, TILE, TILE);
				return;
			}
		} else if (drawSprite(ctx, node.id(), x, y)) {
			return;
		}
		ctx.drawItem(NodeIcons.of(node), x, y);
	}

	public static void drawRole(DrawContext ctx, Role role, Badge badge, int x, int y) {
		if (badge == Badge.CORE && drawRoleIcon(ctx, role, x, y)) return;
		if (drawSprite(ctx, "role/" + role.key() + "/" + badge.name().toLowerCase(Locale.ROOT), x, y)) return;
		ctx.drawItem(switch (badge) {
			case CORE -> role.iconStack();
			case SIGNATURE -> NodeIcons.signature(role);
			case ULTIMATE -> NodeIcons.ultimate(role);
		}, x, y);
	}

	/** v66: icono de rol de la dirección de arte (16×16, generado por gen_role_icons.py); falso si el paquete no lo trae. */
	private static boolean drawRoleIcon(DrawContext ctx, Role role, int x, int y) {
		Identifier texture = Aptitudes.id("textures/gui/role/small/" + role.key() + ".png");
		boolean present = HAS_TEXTURE.computeIfAbsent("role-icon/" + role.key(), id ->
				MinecraftClient.getInstance().getResourceManager().getResource(texture).isPresent());
		if (!present) return false;
		// El medallón tiene esquinas transparentes: DrawContext.drawTexture en 1.21.1 no activa la mezcla por sí solo.
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			ctx.drawTexture(texture, x, y, 0.0F, 0.0F, TILE, TILE, TILE, TILE);
		} finally {
			RenderSystem.disableBlend();
		}
		return true;
	}

	/** La decoración es opcional para los paquetes de recursos con un índice anterior. */
	static void drawCrest(DrawContext ctx, Role role, int x, int y, int size) {
		// DrawContext.drawTexture en 1.21.1 no activa la mezcla para el alfa del grabado.
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			drawSprite(ctx, "role/" + role.key() + "/crest", x, y, size);
		} finally {
			RenderSystem.disableBlend();
		}
	}

	public static void drawKeystoneTemplate(DrawContext ctx, KeystoneRules.Template template, int x, int y) {
		if (!drawSprite(ctx, KEYSTONES[template.ordinal()], x, y)) {
			ctx.drawItem(NodeIcons.keystone(template), x, y);
		}
	}

	/** Busca la piedra angular S7 de una senda (1–3) y dibuja su PNG de senda. */
	public static void drawKeystone(DrawContext ctx, Role role, int path, int x, int y) {
		String id = PathRules.nodeId(role.key(), path, PathRules.Step.S7);
		RoleNode node = RoleTree.get(id);
		if (node == null) throw new IllegalArgumentException("Piedra angular desconocida: " + id);
		draw(ctx, node, x, y);
	}
}
