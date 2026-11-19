package dev.aptitudes.validation;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.mob.NewMobs;
import dev.aptitudes.mob.OsarioEntities;
import dev.aptitudes.mob.OsarioEntity;
import dev.aptitudes.sound.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OsarioRenderQa implements ClientModInitializer {
	private static final int[] COUNTS = {1, 5, 10, 20, 30};
	private static final int[] VISUAL_ANGLES = {0, 0, -35, 90, 180, -25};
	private static final int FLOOR = 70;
	private final String variant = System.getProperty("osario.qa.render", "");
	private final int warmSeconds = Integer.getInteger("osario.qa.clientWarm", 60);
	private final int sampleSeconds = Integer.getInteger("osario.qa.clientSamples", 120);
	private final int fixedCount = Integer.getInteger("osario.qa.clientCount", 0);
	private final List<MobEntity> mobs = new ArrayList<>();
	private final List<Long> spans = new ArrayList<>();
	private final List<Long> worldSpans = new ArrayList<>();
	private final StringBuilder output = new StringBuilder(
			"case,count,warm_s,sample_s,width,height,frames,mean_fps,mean_ms,p99_ms,p999_ms,heap_bytes,p50_ms,focused,world_render_mean_ms\n");
	private volatile boolean prepared;
	private boolean caseCaptured;
	private boolean requestedWorld;
	private boolean glowRemoved;
	private int audioTick;
	private int soundIndex;
	private ServerPlayerEntity clipTarget;
	private int clipFrame;
	private int caseIndex = -1;
	private long requestedAt;
	private long startedAt;
	private long previousFrame;
	private long worldFrameStartedAt;

	@Override
	public void onInitializeClient() {
		if (variant.isEmpty()) return;
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		WorldRenderEvents.START.register(context -> worldFrameStartedAt = System.nanoTime());
		if (variant.contains("off")) {
			WorldRenderEvents.START.register(context -> {
				var client = MinecraftClient.getInstance();
				if (client.player == null || glowRemoved) return;
				for (var entity : candidates(client)) {
					if (client.getEntityRenderDispatcher().getRenderer(entity) instanceof GeoEntityRenderer<?> renderer) {
						renderer.getRenderLayers().removeIf(layer -> layer instanceof AutoGlowingGeoLayer<?>);
						glowRemoved = true;
						break;
					}
				}
			});
		}
		WorldRenderEvents.END.register(context -> frame(MinecraftClient.getInstance()));
	}

	private void openWorld(MinecraftClient client) {
		client.options.pauseOnLostFocus = false;
		client.options.hudHidden = true;
		client.options.getViewDistance().setValue(12);
		client.options.getSimulationDistance().setValue(10);
		client.options.getMaxFps().setValue(260);
		client.options.getEnableVsync().setValue(false);
		client.options.getParticles().setValue(variant.contains("minimal") || variant.contains("visual") ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
		client.getWindow().setVsync(false);
		client.getWindow().setWindowedSize(1280, 720);
		System.out.println("OSARIO RENDER GPU=" + GL11.glGetString(GL11.GL_RENDERER));
		var rules = new GameRules();
		rules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
		rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
		rules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
		var loader = client.createIntegratedServerLoader();
		Path path = client.runDirectory.toPath().resolve("saves").resolve("osario-qa");
		if (Files.exists(path.resolve("level.dat"))) {
			loader.start("osario-qa", () -> client.setScreen(new TitleScreen()));
		} else {
			var info = new LevelInfo("Osario QA (aislado)", GameMode.CREATIVE, false, Difficulty.NORMAL,
					true, rules, DataConfiguration.SAFE_MODE);
			loader.createAndStart("osario-qa", info, new GeneratorOptions(0, false, false),
					registries -> registries.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.FLAT).value()
							.createDimensionsRegistryHolder(), new TitleScreen());
		}
	}

	private void tick(MinecraftClient client) {
		if (variant.equals("audio")) {
			tickAudio(client);
			return;
		}
		if (!requestedWorld && client.getOverlay() == null) {
			requestedWorld = true;
			System.out.println("OSARIO RENDER recursos cargados; preparando mundo de pruebas");
			client.send(() -> openWorld(client));
			return;
		}
		if (client.world == null || client.player == null || client.getServer() == null) return;
		if (caseIndex < 0) {
			next(client);
			return;
		}
		if (caseIndex >= cases()) return;
		if (prepared && startedAt == 0 && System.nanoTime() - requestedAt >= 2_000_000_000L) {
			if (candidates(client).size() == count()) {
				startedAt = System.nanoTime();
				System.out.println("OSARIO RENDER inicio " + variant + " grupo=" + count());
				if (clips()) startClip(client);
			}
		}
		if (startedAt == 0 && System.nanoTime() - requestedAt > 30_000_000_000L) {
			throw new IllegalStateException("Los mobs del laboratorio no han llegado al cliente");
		}
	}

	private static final List<SoundEvent> AUDIO = List.of(
			ModSounds.OSARIO_AMBIENT, ModSounds.OSARIO_ALERT, ModSounds.OSARIO_HURT, ModSounds.OSARIO_DEATH,
			ModSounds.OSARIO_CLAW, ModSounds.OSARIO_HEAVY, ModSounds.OSARIO_SLAM_CHARGE, ModSounds.OSARIO_SLAM_HIT,
			ModSounds.OSARIO_LUNGE, ModSounds.OSARIO_STEP);

	private void tickAudio(MinecraftClient client) {
		if (client.getOverlay() != null || ++audioTick % 30 != 0) return;
		if (soundIndex >= AUDIO.size()) {
			client.scheduleStop();
			return;
		}
		client.options.getSoundVolumeOption(SoundCategory.MASTER).setValue(0.5);
		var event = AUDIO.get(soundIndex++);
		var instance = PositionedSoundInstance.master(event, 1);
		var manager = client.getSoundManager();
		manager.play(instance);
		if (!manager.isPlaying(instance)) throw new IllegalStateException("No se pudo reproducir " + event.getId());
		System.out.println("OSARIO AUDIO " + event.getId() + " master=0.50");
	}

	private List<MobEntity> candidates(MinecraftClient client) {
		var type = variant.contains("errante") ? NewMobs.ERRANTE : OsarioEntities.OSARIO;
		return client.world.getEntitiesByClass(MobEntity.class, new Box(-14, FLOOR, -14, 14, FLOOR + 5, 14),
				entity -> entity.getType() == type);
	}

	private void next(MinecraftClient client) {
		prepared = false;
		startedAt = 0;
		clipFrame = 0;
		previousFrame = 0;
		caseCaptured = false;
		spans.clear();
		worldSpans.clear();
		requestedAt = System.nanoTime();
		caseIndex++;
		if (caseIndex >= cases()) {
			if (clips()) client.getServer().execute(this::removeClipTarget);
			client.scheduleStop();
			return;
		}
		final int size = count(), stage = caseIndex;
		var server = client.getServer();
		var profile = client.player.getUuid();
		server.execute(() -> {
			Aptitudes.config().threatEnabled = false;
			Aptitudes.config().eliteChance = 0;
			var world = server.getOverworld();
			var player = server.getPlayerManager().getPlayer(profile);
			if (player == null) return;
			world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
			world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
			removeClipTarget();
			boolean visual = variant.contains("visual");
			world.setTimeOfDay(visual && (stage == 1 || stage == 5) ? 18000 : 6000);
			for (var mob : mobs) mob.discard();
			mobs.clear();
			if (stage == 0) {
				world.getEntitiesByClass(MobEntity.class, new Box(-14, FLOOR, -14, 14, FLOOR + 5, 14),
						e -> e.getCommandTags().contains("osario_qa")).forEach(MobEntity::discard);
				for (int x = -11; x <= 13; x++) for (int z = -12; z <= 11; z++)
					world.setBlockState(new BlockPos(x, FLOOR, z), Blocks.DEEPSLATE_TILES.getDefaultState());
			}
			for (int i = 0; i < size; i++) {
				var type = variant.contains("errante") ? NewMobs.ERRANTE : OsarioEntities.OSARIO;
				var mob = type.create(world);
				if (mob == null) throw new IllegalStateException("No se creó el mob");
				double x = i == 0 ? .5 : (i % 6 - 2.5) * 1.75;
				double z = i == 0 ? 0 : (i / 6 - 2) * 1.7 - 1;
				mob.refreshPositionAndAngles(x, FLOOR + 1, z, 0, 0);
				mob.addCommandTag("osario_qa");
				mob.setAiDisabled(true);
				mob.setPersistent();
				if (!world.spawnEntity(mob)) throw new IllegalStateException("Falló la aparición del mob");
				mobs.add(mob);
			}
			player.changeGameMode(GameMode.SPECTATOR);
			double dist = clips() ? 6 : visual ? (stage == 5 ? 3.5 : 8) : 12;
			double angle = Math.toRadians(clips() ? -55 : visual ? VISUAL_ANGLES[stage] : -35);
			Vec3d eye = new Vec3d(.5 + Math.sin(angle) * dist, FLOOR + 2.62, Math.cos(angle) * dist);
			Vec3d target = new Vec3d(.5, FLOOR + 1.9, 0);
			Vec3d delta = target.subtract(eye);
			float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
			float pitch = (float) Math.toDegrees(Math.atan2(-delta.y, delta.horizontalLength()));
			player.networkHandler.requestTeleport(eye.x, eye.y - player.getStandingEyeHeight(), eye.z, yaw, pitch);
			prepared = true;
		});
	}

	private int cases() {
		if (clips()) return Clip.values().length;
		return variant.contains("visual") ? VISUAL_ANGLES.length : fixedCount > 0 ? 1 : COUNTS.length;
	}

	private int count() {
		if (clips()) return 1;
		return variant.contains("visual") ? 1 : fixedCount > 0 ? fixedCount : COUNTS[caseIndex];
	}

	private void frame(MinecraftClient client) {
		if (startedAt == 0 || caseIndex >= cases() || !prepared || client.currentScreen != null) return;
		if (clips()) {
			captureClip(client);
			return;
		}
		long now = System.nanoTime();
		double elapsed = (now - startedAt) / 1e9;
		int warm = variant.contains("visual") ? 4 : warmSeconds;
		int duration = variant.contains("visual") ? 2 : sampleSeconds;
		if (!caseCaptured && elapsed >= Math.max(0, warm - 2)) {
			caseCaptured = true;
			ScreenshotRecorder.saveScreenshot(client.runDirectory, "osario-" + variant + "-" + caseIndex + ".png",
					client.getFramebuffer(), message -> System.out.println(message.getString()));
			return;
		}
		if (elapsed < warm) return;
		if (previousFrame != 0) {
			spans.add(now - previousFrame);
			worldSpans.add(now - worldFrameStartedAt);
		}
		previousFrame = now;
		if (elapsed < warm + duration || spans.size() < 5) return;
		double mean = spans.stream().mapToLong(n -> n).average().orElseThrow() / 1_000_000;
		var sorted = spans.stream().sorted().toList();
		var vm = Runtime.getRuntime();
		double worldMean = worldSpans.stream().mapToLong(n -> n).average().orElseThrow() / 1_000_000;
		String line = String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%d,%.4f,%.4f,%.4f,%.4f,%d,%.4f,%b,%.4f%n",
				variant, count(), warm, duration, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(),
				sorted.size(), 1000 / mean, mean, percentile(sorted, .99), percentile(sorted, .999),
				vm.totalMemory() - vm.freeMemory(), percentile(sorted, .5),
				GLFW.glfwGetWindowAttrib(client.getWindow().getHandle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE, worldMean);
		output.append(line);
		System.out.print("OSARIO RENDER " + line);
		try {
			Files.writeString(client.runDirectory.toPath().resolve("client-fps.csv"), output);
		} catch (IOException ex) {
			throw new IllegalStateException("Guardar mediciones de cliente", ex);
		}
		next(client);
	}

	private boolean clips() {
		return variant.equals("clips") || variant.startsWith("clips-");
	}

	private enum Clip {
		CLAW(2, 4, 8, 10, 13), HEAVY(3, 8, 16, 19, 23), SLAM(4, 10, 18, 23, 29),
		LUNGE(5, 4, 12, 14, 18, 24), DEATH(7, 2, 6, 9, 14, 18);

		final int action;
		final int[] ticks;

		Clip(int action, int... ticks) {
			this.action = action;
			this.ticks = ticks;
		}
	}

	private void startClip(MinecraftClient client) {
		var clip = Clip.values()[caseIndex];
		var server = client.getServer();
		server.execute(() -> {
			var mob = (OsarioEntity) mobs.getFirst();
			removeClipTarget();
			if (clip == Clip.DEATH) {
				mob.damage(mob.getDamageSources().genericKill(), 500);
				return;
			}
			clipTarget = LabPlayer.connect(server.getOverworld());
			clipTarget.setNoGravity(true);
			clipTarget.refreshPositionAndAngles(.5, FLOOR + 1, clip == Clip.LUNGE ? 4.4 : 1.8, 0, 0);
			if (!OsarioCombatGameTests.begin(mob, clipTarget, clip.name())) {
				throw new IllegalStateException("No se inició " + clip.name());
			}
		});
	}

	private void removeClipTarget() {
		if (clipTarget == null) return;
		clipTarget.getServer().getPlayerManager().remove(clipTarget);
		clipTarget = null;
	}

	private void captureClip(MinecraftClient client) {
		var clip = Clip.values()[caseIndex];
		for (MobEntity entity : candidates(client)) {
			long stamp = OsarioCombatGameTests.stamp((OsarioEntity) entity);
			long tick = client.world.getTime() - (stamp >>> 4);
			if ((stamp & 15) != clip.action || tick < clip.ticks[clipFrame]) continue;
			String name = String.format(Locale.ROOT, "osario-%s-t%02d.png", clip.name().toLowerCase(Locale.ROOT), tick);
			ScreenshotRecorder.saveScreenshot(client.runDirectory, name, client.getFramebuffer(),
					message -> System.out.println("OSARIO CLIP " + message.getString()));
			if (++clipFrame == clip.ticks.length) next(client);
			return;
		}
		if (System.nanoTime() - startedAt > 6_000_000_000L) {
			throw new IllegalStateException("No se recibió en el cliente el tick " + clip.ticks[clipFrame] + " de " + clip);
		}
	}

	private static double percentile(List<Long> sorted, double q) {
		return sorted.get((int) Math.ceil(q * sorted.size()) - 1) / 1_000_000.0;
	}
}
