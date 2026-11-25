package dev.aptitudes.client.social;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.item.AshChalkItem;
import dev.aptitudes.network.GroundMessageRatePayload;
import dev.aptitudes.network.GroundMessageSyncPayload;
import dev.aptitudes.social.GroundMessageRules;
import dev.aptitudes.social.MessageCatalog;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * C7 (S5): notas en el suelo en el cliente. Copia local de las notas cercanas (la envía el servidor), glifo en el
 * mundo, línea del HUD al mirar una nota y tecla para valorarla. Todo es visual: el servidor valida cada acción.
 */
public final class ClientGroundMessages {
	private static final Identifier GLYPH = Aptitudes.id("textures/misc/ground_glyph.png");
	private static List<GroundMessageSyncPayload.Entry> notes = List.of();
	private static GroundMessageSyncPayload.Entry looked;
	private static KeyBinding rateKey;

	private ClientGroundMessages() {}

	public static void register(String category) {
		rateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.aptitudes.rate_message", InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V, category));
		AshChalkItem.opener = pos -> MinecraftClient.getInstance().setScreen(new GroundMessageScreen(pos));
		ClientPlayNetworking.registerGlobalReceiver(GroundMessageSyncPayload.ID, (payload, context) -> notes = payload.notes());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			notes = List.of();
			looked = null;
		});
		ClientTickEvents.END_CLIENT_TICK.register(ClientGroundMessages::tick);
		HudRenderCallback.EVENT.register(ClientGroundMessages::renderHud);
		WorldRenderEvents.AFTER_ENTITIES.register(ClientGroundMessages::renderWorld);
	}

	private static boolean visible() {
		return !Boolean.FALSE.equals(ClientConfig.get().showGroundMessages);
	}

	private static void tick(MinecraftClient client) {
		looked = client.player == null || client.currentScreen != null || !visible() ? null : lookedAt(client);
		while (rateKey.wasPressed()) {
			GroundMessageSyncPayload.Entry e = looked;
			if (e != null && !e.mine() && !e.rated()) ClientPlayNetworking.send(new GroundMessageRatePayload(e.id()));
		}
	}

	/** Nota del suelo a la que apunta la mirada (corte del rayo con el plano de la nota), sin atravesar bloques. */
	private static GroundMessageSyncPayload.Entry lookedAt(MinecraftClient client) {
		Vec3d eye = client.player.getCameraPosVec(1.0f);
		Vec3d dir = client.player.getRotationVec(1.0f);
		if (dir.y > -1.0E-3) return null;
		double blocked = Double.MAX_VALUE;
		HitResult hit = client.crosshairTarget;
		if (hit != null && hit.getType() != HitResult.Type.MISS) blocked = hit.getPos().distanceTo(eye);
		GroundMessageSyncPayload.Entry best = null;
		double bestT = Double.MAX_VALUE;
		for (GroundMessageSyncPayload.Entry e : notes) {
			double planeY = e.pos().getY() + 0.02;
			double t = (planeY - eye.y) / dir.y;
			if (t <= 0 || t > GroundMessageRules.READ_REACH || t > blocked + 0.3 || t >= bestT) continue;
			double x = eye.x + dir.x * t;
			double z = eye.z + dir.z * t;
			if (x < e.pos().getX() || x > e.pos().getX() + 1 || z < e.pos().getZ() || z > e.pos().getZ() + 1) continue;
			best = e;
			bestT = t;
		}
		return best;
	}

	/** Texto de la nota en el idioma del jugador. */
	public static Text phrase(String template, String word) {
		return Text.translatable(MessageCatalog.templateKey(template), Text.translatable(MessageCatalog.wordKey(word)));
	}

	private static void renderHud(DrawContext ctx, RenderTickCounter counter) {
		GroundMessageSyncPayload.Entry e = looked;
		MinecraftClient client = MinecraftClient.getInstance();
		if (e == null || client.options.hudHidden) return;
		TextRenderer tr = client.textRenderer;
		int cx = ctx.getScaledWindowWidth() / 2;
		int y = ctx.getScaledWindowHeight() / 2 + 14;
		Text phrase = phrase(e.template(), e.word());
		ctx.drawCenteredTextWithShadow(tr, phrase, cx, y, 0xF2E8D5);
		y += 11;
		Text info = Text.translatable("hud.aptitudes.ground_message.ratings", e.ratings());
		if (!e.author().isEmpty()) {
			info = Text.translatable("hud.aptitudes.ground_message.author", e.author()).append(" · ").append(info);
		}
		ctx.drawCenteredTextWithShadow(tr, info.copy().formatted(Formatting.GRAY), cx, y, 0xAAAAAA);
		y += 11;
		Text hint = e.mine() ? Text.translatable("hud.aptitudes.ground_message.own")
				: e.rated() ? Text.translatable("hud.aptitudes.ground_message.rated")
				: Text.translatable("hud.aptitudes.ground_message.rate_hint", rateKey.getBoundKeyLocalizedText());
		ctx.drawCenteredTextWithShadow(tr, hint.copy().formatted(Formatting.DARK_GRAY), cx, y, 0x888888);
	}

	private static void renderWorld(WorldRenderContext context) {
		if (notes.isEmpty() || !visible()) return;
		VertexConsumerProvider consumers = context.consumers();
		if (consumers == null) return;
		Vec3d cam = context.camera().getPos();
		MatrixStack matrices = context.matrixStack() != null ? context.matrixStack() : new MatrixStack();
		VertexConsumer vc = consumers.getBuffer(RenderLayer.getEntityTranslucent(GLYPH));
		for (GroundMessageSyncPayload.Entry e : notes) {
			float glow = GroundMessageRules.glow(e.ratings());
			if (e == looked) glow = Math.min(1f, glow + 0.2f);
			int alpha = (int) (255 * (0.45f + 0.5f * glow));
			int light = LightmapTextureManager.pack((int) (6 + 9 * glow), 15);
			float x0 = (float) (e.pos().getX() + 0.08 - cam.x);
			float x1 = (float) (e.pos().getX() + 0.92 - cam.x);
			float y = (float) (e.pos().getY() + 0.015 - cam.y);
			float z0 = (float) (e.pos().getZ() + 0.08 - cam.z);
			float z1 = (float) (e.pos().getZ() + 0.92 - cam.z);
			MatrixStack.Entry entry = matrices.peek();
			vertex(vc, entry, x0, y, z0, 0, 0, alpha, light);
			vertex(vc, entry, x0, y, z1, 0, 1, alpha, light);
			vertex(vc, entry, x1, y, z1, 1, 1, alpha, light);
			vertex(vc, entry, x1, y, z0, 1, 0, alpha, light);
		}
		// Igual que los números de daño (ClientFx): se vuelca ya para no depender del orden del resto del pase.
		if (consumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw();
	}

	private static void vertex(VertexConsumer vc, MatrixStack.Entry entry, float x, float y, float z, float u, float v,
							   int alpha, int light) {
		vc.vertex(entry, x, y, z).color(210, 200, 185, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV)
				.light(light).normal(entry, 0f, 1f, 0f);
	}
}
