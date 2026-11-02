package dev.aptitudes.client;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.network.EclipsePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * Estado del Eclipse en el cliente (v34). Solo muestra lo que envía el servidor: fase, intensidad y progreso.
 * La intensidad se suaviza tick a tick para que el cielo no cambie a saltos.
 */
public final class ClientEclipse {
	/** Textura del sol eclipsado (generada por tools/textures/gen_eclipse.py). */
	public static final Identifier ECLIPSE_SUN = Aptitudes.id("textures/environment/eclipse_sun.png");
	/** Color del cielo en plena totalidad (carmesí oscuro) y de la niebla y las nubes. */
	private static final Vec3d SKY = new Vec3d(0x5A / 255.0, 0x0A / 255.0, 0x0A / 255.0);
	private static final Vec3d FOG = new Vec3d(0x3C / 255.0, 0x08 / 255.0, 0x08 / 255.0);
	private static final Vec3d CLOUDS = new Vec3d(0x4A / 255.0, 0x12 / 255.0, 0x14 / 255.0);

	private static int phase;
	private static float target;
	private static float current;
	private static float previous;
	private static float progress;

	private ClientEclipse() {
	}

	public static void onPayload(EclipsePayload payload) {
		phase = Math.max(0, payload.phase());
		target = MathHelper.clamp(payload.intensity(), 0f, 1f);
		progress = MathHelper.clamp(payload.progress(), 0f, 1f);
		if (phase == 0) target = 0f;
	}

	public static void clear() {
		phase = 0;
		target = current = previous = progress = 0f;
	}

	/** Cada tick: acerca la intensidad al objetivo y genera ceniza alrededor del jugador. */
	public static void tick(MinecraftClient client) {
		previous = current;
		current += MathHelper.clamp(target - current, -0.02f, 0.02f);
		if (client.world == null || client.player == null || client.isPaused() || current < 0.3f) return;
		Random random = client.world.random;
		int count = (int) (current * 4);
		for (int i = 0; i < count; i++) {
			double x = client.player.getX() + (random.nextDouble() - 0.5) * 24;
			double y = client.player.getY() + random.nextDouble() * 10;
			double z = client.player.getZ() + (random.nextDouble() - 0.5) * 24;
			client.world.addParticle(random.nextInt(3) == 0 ? ParticleTypes.WHITE_ASH : ParticleTypes.ASH, x, y, z, 0, -0.02, 0);
		}
	}

	public static boolean active() {
		return phase != 0 || current > 0.001f;
	}

	public static int phase() {
		return phase;
	}

	public static float progress() {
		return progress;
	}

	/** Intensidad interpolada para el fotograma actual (0–1). */
	public static float intensity(float tickDelta) {
		return MathHelper.lerp(MathHelper.clamp(tickDelta, 0f, 1f), previous, current);
	}

	/** Intensidad para los efectos del cielo: 0 si el jugador los desactivó en la configuración del cliente. */
	public static float skyIntensity(float tickDelta) {
		return ClientConfig.get().eclipseSkyEffects ? intensity(tickDelta) : 0f;
	}

	public static Vec3d tintSky(Vec3d original, float tickDelta) {
		return blend(original, SKY, skyIntensity(tickDelta));
	}

	public static Vec3d tintClouds(Vec3d original, float tickDelta) {
		return blend(original, CLOUDS, skyIntensity(tickDelta));
	}

	/** Color de la niebla (componentes 0–1) mezclado hacia rojo oscuro. */
	public static float[] tintFog(float red, float green, float blue, float tickDelta) {
		float t = skyIntensity(tickDelta);
		return new float[] {
				MathHelper.lerp(t, red, (float) FOG.x),
				MathHelper.lerp(t, green, (float) FOG.y),
				MathHelper.lerp(t, blue, (float) FOG.z)};
	}

	private static Vec3d blend(Vec3d from, Vec3d to, float t) {
		if (t <= 0f) return from;
		return from.lerp(to, MathHelper.clamp(t, 0f, 1f));
	}
}
