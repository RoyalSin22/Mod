package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.data.Skill;
import dev.aptitudes.network.DamageNumberPayload;
import dev.aptitudes.network.ScreenFxPayload;
import dev.aptitudes.network.SkillXpPayload;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Efectos visuales del cliente:
 * <ul>
 *   <li><b>Destello</b> de pantalla (viñeta de color en los bordes) al lanzar habilidades grandes.</li>
 *   <li><b>Temblor</b> de cámara (lo aplica {@code CameraMixin} leyendo {@link #shakeYaw}/{@link #shakePitch}).</li>
 *   <li><b>Números de daño</b> flotantes en el mundo, que suben y se desvanecen.</li>
 *   <li><b>Notificaciones de XP</b> de habilidades: tarjeta con icono, cantidad y barra de progreso.</li>
 * </ul>
 * Todo usa tiempo real (ms) para que la animación sea suave aunque bajen los TPS.
 */
public final class ClientFx {
	private static final Random RANDOM = new Random();

	// Destello
	private static int flashColor;
	private static float flashStrength;
	private static long flashStart;
	private static final long FLASH_MS = 450;

	// Temblor
	private static float shakeStrength;
	private static long shakeStart;
	private static final long SHAKE_MS = 380;

	// Números de daño
	private record DamageNumber(Vec3d pos, String text, int color, long start, double driftX, double driftZ) {}
	private static final List<DamageNumber> NUMBERS = new ArrayList<>();
	private static final long NUMBER_MS = 1100;

	// Notificaciones de XP
	private static final class XpToast {
		final Skill skill;
		int amount, xp, needed, level;
		long start;
		XpToast(Skill skill) {
			this.skill = skill;
		}
	}
	private static final List<XpToast> TOASTS = new ArrayList<>();
	private static final long TOAST_MS = 3000;
	private static final int MAX_TOASTS = 4;

	private ClientFx() {
	}

	// ------------------------------------------------------------ Receptores

	public static void onScreenFx(ScreenFxPayload p) {
		if (!ClientConfig.get().screenEffects) return;
		long now = Util.getMeasuringTimeMs();
		if (p.type() == ScreenFxPayload.FLASH) {
			flashColor = p.color() & 0xFFFFFF;
			flashStrength = MathHelper.clamp(p.strength(), 0f, 1f);
			flashStart = now;
		} else if (p.type() == ScreenFxPayload.SHAKE) {
			// Si ya hay un temblor más fuerte en curso, no lo pisa.
			if (currentShake(now) < p.strength()) {
				shakeStrength = Math.min(p.strength(), 3.0f);
				shakeStart = now;
			}
		}
	}

	public static void onDamageNumber(DamageNumberPayload p) {
		if (!ClientConfig.get().damageNumbers) return;
		String text = p.amount() >= 10 ? String.valueOf(Math.round(p.amount()))
				: String.format(java.util.Locale.ROOT, "%.1f", p.amount());
		if (NUMBERS.size() > 40) NUMBERS.remove(0);
		NUMBERS.add(new DamageNumber(new Vec3d(p.x(), p.y(), p.z()), text, p.color() & 0xFFFFFF,
				Util.getMeasuringTimeMs(), (RANDOM.nextDouble() - 0.5) * 0.6, (RANDOM.nextDouble() - 0.5) * 0.6));
	}

	public static void onSkillXp(SkillXpPayload p) {
		if (p.skill() < 0 || p.skill() >= Skill.values().length) return;
		Skill skill = Skill.values()[p.skill()];
		XpToast toast = null;
		for (XpToast t : TOASTS) if (t.skill == skill) toast = t;
		if (toast == null) {
			toast = new XpToast(skill);
			TOASTS.add(toast);
			if (TOASTS.size() > MAX_TOASTS) TOASTS.remove(0);
		}
		toast.amount += p.amount(); // se acumula mientras la tarjeta siga visible
		toast.xp = p.xp();
		toast.needed = p.needed();
		toast.level = p.level();
		toast.start = Util.getMeasuringTimeMs();
	}

	public static void clear() {
		NUMBERS.clear();
		TOASTS.clear();
		flashStrength = 0;
		shakeStrength = 0;
	}

	// ------------------------------------------------------------ Temblor

	private static float currentShake(long now) {
		long age = now - shakeStart;
		if (shakeStrength <= 0 || age >= SHAKE_MS) return 0;
		return shakeStrength * (1f - age / (float) SHAKE_MS);
	}

	/** Desplazamiento de guiñada (grados) para el temblor actual. */
	public static float shakeYaw() {
		float s = currentShake(Util.getMeasuringTimeMs());
		return s <= 0 ? 0 : (RANDOM.nextFloat() - 0.5f) * 2f * s;
	}

	public static float shakePitch() {
		float s = currentShake(Util.getMeasuringTimeMs());
		return s <= 0 ? 0 : (RANDOM.nextFloat() - 0.5f) * 1.4f * s;
	}

	// --------------------------------------------------------------- HUD

	public static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) return;
		long now = Util.getMeasuringTimeMs();
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();
		renderFlash(ctx, now, w, h);
		renderToasts(ctx, client.textRenderer, now, w, h);
	}

	/** Viñeta de color: bordes con degradado hacia el centro, que se apaga en ~0,45 s. */
	private static void renderFlash(DrawContext ctx, long now, int w, int h) {
		long age = now - flashStart;
		if (flashStrength <= 0 || age >= FLASH_MS) return;
		float t = 1f - age / (float) FLASH_MS;
		int alpha = (int) (170 * flashStrength * t * t);
		if (alpha <= 2) return;
		int edge = (alpha << 24) | flashColor;
		int clear = flashColor; // alfa 0
		int band = Math.max(24, h / 4);
		ctx.fillGradient(0, 0, w, band, edge, clear);
		ctx.fillGradient(0, h - band, w, h, clear, edge);
		// Laterales: degradado horizontal dibujado por franjas finas.
		int sideBand = Math.max(24, w / 6);
		for (int i = 0; i < sideBand; i += 2) {
			int a = (int) (alpha * (1f - i / (float) sideBand));
			int c = (a << 24) | flashColor;
			ctx.fill(i, 0, i + 2, h, c);
			ctx.fill(w - i - 2, 0, w - i, h, c);
		}
		// Toque central muy suave para los destellos fuertes.
		if (flashStrength > 0.6f) ctx.fill(0, 0, w, h, ((int) (alpha * 0.18f) << 24) | flashColor);
	}

	/** Tarjetas de XP apiladas en el lado derecho de la pantalla, con entrada deslizante. */
	private static void renderToasts(DrawContext ctx, TextRenderer text, long now, int w, int h) {
		Iterator<XpToast> it = TOASTS.iterator();
		while (it.hasNext()) if (now - it.next().start >= TOAST_MS) it.remove();
		int cardW = 132;
		int cardH = 26;
		int y = h / 2 - 40;
		for (int i = TOASTS.size() - 1; i >= 0; i--) {
			XpToast t = TOASTS.get(i);
			long age = now - t.start;
			float in = MathHelper.clamp(age / 180f, 0f, 1f);
			float out = MathHelper.clamp((TOAST_MS - age) / 400f, 0f, 1f);
			float vis = Math.min(in, out);
			int x = w - (int) ((cardW + 6) * easeOut(vis));
			int a = (int) (230 * vis);
			if (a < 8) continue;
			// Marco oscuro con borde dorado.
			ctx.fill(x, y, x + cardW, y + cardH, (a << 24) | 0x14110C);
			int gold = (a << 24) | 0xC9A227;
			ctx.fill(x, y, x + cardW, y + 1, gold);
			ctx.fill(x, y + cardH - 1, x + cardW, y + cardH, gold);
			ctx.fill(x, y, x + 1, y + cardH, gold);
			ctx.drawItem(t.skill.iconStack(), x + 4, y + 5);
			int textAlpha = Math.max(4, a) << 24;
			ctx.drawText(text, Text.literal("+" + t.amount + " ").append(Text.translatable(t.skill.translationKey())),
					x + 24, y + 4, textAlpha | 0xFFE08A, true);
			ctx.drawText(text, Text.translatable("aptitudes.toast.level", t.level), x + cardW - 4
					- text.getWidth(Text.translatable("aptitudes.toast.level", t.level)), y + 4, textAlpha | 0xBFB49A, false);
			// Barra de progreso hacia el siguiente nivel.
			int barX = x + 24;
			int barW = cardW - 30;
			int barY = y + 16;
			ctx.fill(barX, barY, barX + barW, barY + 4, (a << 24) | 0x2A241A);
			float frac = t.needed <= 0 ? 1f : MathHelper.clamp(t.xp / (float) t.needed, 0f, 1f);
			int fill = (int) (barW * frac);
			ctx.fill(barX, barY, barX + fill, barY + 4, (a << 24) | 0x7CC24A);
			ctx.fill(barX, barY, barX + fill, barY + 1, (a << 24) | 0xB6F07A);
			y += cardH + 3;
		}
	}

	private static float easeOut(float t) {
		float inv = 1f - t;
		return 1f - inv * inv * inv;
	}

	// ------------------------------------------------------------- Mundo

	/** Números de daño como carteles orientados a la cámara (como las etiquetas de nombre). */
	public static void renderWorld(WorldRenderContext context) {
		if (NUMBERS.isEmpty()) return;
		MinecraftClient client = MinecraftClient.getInstance();
		VertexConsumerProvider consumers = context.consumers();
		if (consumers == null) return;
		Camera camera = context.camera();
		Vec3d cam = camera.getPos();
		MatrixStack matrices = context.matrixStack() != null ? context.matrixStack() : new MatrixStack();
		TextRenderer text = client.textRenderer;
		long now = Util.getMeasuringTimeMs();

		Iterator<DamageNumber> it = NUMBERS.iterator();
		while (it.hasNext()) {
			DamageNumber n = it.next();
			long age = now - n.start;
			if (age >= NUMBER_MS) {
				it.remove();
				continue;
			}
			float t = age / (float) NUMBER_MS;
			double rise = 0.9 * easeOut(t);
			// "Pop": crece al aparecer y luego se encoge un poco.
			float pop = t < 0.12f ? 0.6f + t / 0.12f * 0.7f : 1.3f - Math.min(0.3f, (t - 0.12f) * 0.6f);
			int alpha = (int) (255 * (t > 0.65f ? 1f - (t - 0.65f) / 0.35f : 1f));
			if (alpha < 8) continue;
			double x = n.pos.x + n.driftX * t - cam.x;
			double y = n.pos.y + rise - cam.y;
			double z = n.pos.z + n.driftZ * t - cam.z;
			matrices.push();
			matrices.translate(x, y, z);
			matrices.multiply(camera.getRotation());
			float scale = 0.028f * pop;
			matrices.scale(scale, -scale, scale);
			Matrix4f matrix = matrices.peek().getPositionMatrix();
			float tx = -text.getWidth(n.text) / 2f;
			text.draw(n.text, tx, 0, (alpha << 24) | n.color, true, matrix, consumers,
					TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
			matrices.pop();
		}
		if (consumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw();
	}
}
