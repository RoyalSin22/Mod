package dev.aptitudes.client.screen;

import dev.aptitudes.Aptitudes;
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

import java.util.HashMap;
import java.util.Map;

/** Iconos propios de nodos de Sendas, con respaldo al objeto actual si falta la textura. */
public final class NodeIconRenderer {
	private static final Map<String, Boolean> HAS_TEXTURE = new HashMap<>();

	static {
		// Fabric ejecuta esta recarga síncrona en el hilo principal, como el dibujado.
		// Evita conservar resultados obsoletos tras F3+T o un cambio de paquetes de recursos.
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return Aptitudes.id("node_icon_cache");
					}

					@Override
					public void reload(ResourceManager manager) {
						HAS_TEXTURE.clear();
					}
				});
	}

	private NodeIconRenderer() {
	}

	/** Dibuja el PNG propio de 16×16 del nodo (también S7) o su icono de objeto si no existe. */
	public static void draw(DrawContext ctx, RoleNode node, int x, int y) {
		Identifier texture = Aptitudes.id("textures/gui/node/" + node.id() + ".png");
		boolean present = HAS_TEXTURE.computeIfAbsent(node.id(), id ->
				MinecraftClient.getInstance().getResourceManager().getResource(texture).isPresent());
		if (present) {
			// Yarn 1.21.1: textura, x, y, u, v, ancho, alto, anchoTextura, altoTextura.
			ctx.drawTexture(texture, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
		} else {
			ctx.drawItem(NodeIcons.of(node), x, y);
		}
	}

	/** Busca la piedra angular S7 de una senda (1–3) y la dibuja como cualquier otro nodo. */
	public static void drawKeystone(DrawContext ctx, Role role, int path, int x, int y) {
		String id = PathRules.nodeId(role.key(), path, PathRules.Step.S7);
		RoleNode node = RoleTree.get(id);
		if (node == null) throw new IllegalArgumentException("Piedra angular desconocida: " + id);
		draw(ctx, node, x, y);
	}
}
