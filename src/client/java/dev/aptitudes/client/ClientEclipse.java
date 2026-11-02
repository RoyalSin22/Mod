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
	/**
	 * Colores de cielo, niebla y nubes por evento (v41), en el orden de {@code WorldEventKind}: Eclipse (carmesí),
	 * Niebla de almas (gris azulado) y Lluvia de brasas (naranja ceniza).
	 */
	private static final Vec3d[] SKY = {rgb(0x5A0A0A), rgb(0x2E3A44), rgb(0x8A4A22)};
	private static final Vec3d[] FOG = {rgb(0x3C0808), rgb(0x56646E), rgb(0x6A3A1E)};
	private static final Vec3d[] CLOUDS = {rgb(0x4A1214), rgb(0x46525A), rgb(0x5E3A2A)};

	private static Vec3d rgb(int c) {
		return new Vec3d(((c >> 16) & 0xFF) / 255.0, ((c >> 8) & 0xFF) / 255.0, (c & 0xFF) / 255.0);
	}

	private static int phase;
	/** Evento en curso ({@code WorldEventKind#ordinal()}); 0 = Eclipse. */
	private static int kind;
	private static float target;
	private static float current;
	private static float previous;
	private static float progress;

	private ClientEclipse() {
	}

	public static void onPayload(EclipsePayload payload) {
		phase = Math.max(0, payload.phase());
		if (phase != 0) kind = MathHelper.clamp(payload.kind(), 0, SKY.length - 1);
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
			switch (kind) {
				case 1 -> { // Niebla de almas: velo blanco y alguna llama de alma
					if (random.nextInt(4) == 0) client.world.addParticle(ParticleTypes.SOUL, x, y, z, 0, 0.01, 0);
					else client.world.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0, -0.01, 0);
				}
				case 2 -> { // Lluvia de brasas: chispas que caen
					if (random.nextInt(3) == 0) client.world.addParticle(ParticleTypes.LAVA, x, y + 6, z, 0, 0, 0);
					else client.world.addParticle(ParticleTypes.ASH, x, y, z, 0, -0.02, 0);
				}
				default -> client.world.addParticle(random.nextInt(3) == 0 ? ParticleTypes.WHITE_ASH : ParticleTypes.ASH, x, y, z, 0, -0.02, 0);
			}
		}
	}

	public static boolean active() {
		return phase != 0 || current > 0.001f;
	}

	public static int phase() {
		return phase;
	}

	/** Evento en curso ({@code WorldEventKind#ordinal()}). */
	public static int kind() {
		return kind;
	}

	/** ¿Se debe dibujar el sol eclipsado? Solo durante el Eclipse. */
	public static boolean eclipsedSun() {
		return kind == 0 && skyIntensity(1f) >= 0.5f;
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
		return blend(original, SKY[kind], skyIntensity(tickDelta));
	}

	public static Vec3d tintClouds(Vec3d original, float tickDelta) {
		return blend(original, CLOUDS[kind], skyIntensity(tickDelta));
	}

	/** Color de la niebla (componentes 0–1) mezclado hacia rojo oscuro. */
	public static float[] tintFog(float red, float green, float blue, float tickDelta) {
		float t = skyIntensity(tickDelta);
		return new float[] {
				MathHelper.lerp(t, red, (float) FOG[kind].x),
				MathHelper.lerp(t, green, (float) FOG[kind].y),
				MathHelper.lerp(t, blue, (float) FOG[kind].z)};
	}

	private static Vec3d blend(Vec3d from, Vec3d to, float t) {
		if (t <= 0f) return from;
		return from.lerp(to, MathHelper.clamp(t, 0f, 1f));
	}
}
