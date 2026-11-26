package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.network.ServerPlayerEntity;
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

import java.nio.file.Files;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Dos clientes de desarrollo reciben los mismos cambios y disparos de GeckoLib por la conexión LAN. */
public final class ColosoClientQa implements ClientModInitializer {
	private static final Set<String> REQUIRED = Set.of("coloso_qa_0-idle", "coloso_qa_1-walk",
			"coloso_qa_2-sweep-impact", "coloso_qa_3-slam-impact", "coloso_qa_4-charge-impact",
			"coloso_qa_5-phase_two", "coloso_qa_6-stun", "coloso_qa_7-idle");
	private final String role = System.getProperty("coloso.qa.role", "");
	private final Set<String> captured = new HashSet<>();
	private final Map<String, Long> clipStarts = new HashMap<>();
	private boolean requested;
	private volatile boolean ready;
	private int ticks;
	private long origin;
	private int stage = -1;
	private ServerPlayerEntity dummy;
	private FacelessColossusEntity mob;
	private int frames;

	@Override
	public void onInitializeClient() {
		if (role.isEmpty()) return;
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		WorldRenderEvents.END.register(context -> frame(MinecraftClient.getInstance()));
	}

	private void tick(MinecraftClient client) {
		if (++ticks > 6000) throw new IllegalStateException("El laboratorio no terminó en cinco minutos");
		if (!requested && client.getOverlay() == null) {
			requested = true;
			client.options.pauseOnLostFocus = false;
			client.options.hudHidden = true;
			client.options.getViewDistance().setValue(4);
			client.options.getSimulationDistance().setValue(5);
			client.options.getMaxFps().setValue(60);
			client.options.getEnableVsync().setValue(false);
			GLFW.glfwHideWindow(client.getWindow().getHandle());
			System.out.println("COLOSO GPU " + GL11.glGetString(GL11.GL_RENDERER));
			if (role.equals("peer")) {
				ConnectScreen.connect(new TitleScreen(), client, ServerAddress.parse("127.0.0.1:25592"),
						new ServerInfo("Coloso QA", "127.0.0.1:25592", ServerInfo.ServerType.LAN), false, null);
			} else openWorld(client);
		}
		if (client.world == null || client.player == null) return;
		client.setScreen(null);
		if (role.equals("peer")) return;
		var server = client.getServer();
		if (server == null) return;
		if (!ready) {
			ready = true;
			server.execute(() -> {
				Aptitudes.config().threatEnabled = false;
				Aptitudes.config().eliteChance = 0;
				if (role.equals("reload")) {
					var restored = server.getOverworld().getEntitiesByClass(FacelessColossusEntity.class,
							new Box(-20, 65, -20, 20, 90, 25), entity -> entity.hasCustomName()
									&& entity.getName().getString().equals("coloso_qa_7"));
					if (restored.size() != 1 || !restored.getFirst().hasCrackedArmor() || restored.getFirst().phase() != 2)
						throw new IllegalStateException("La fase no se restauró desde el mundo guardado");
					System.out.println("COLOSO DISK RELOAD PASS phase=" + restored.getFirst().phase());
					client.execute(client::scheduleStop);
					return;
				}
				server.getOverworld().getEntitiesByClass(FacelessColossusEntity.class,
						new Box(-20, 65, -20, 20, 90, 25), entity -> entity.hasCustomName()
								&& entity.getName().getString().startsWith("coloso_qa_")).forEach(FacelessColossusEntity::discard);
				server.setOnlineMode(false);
				if (!server.openToLan(GameMode.SPECTATOR, false, 25592)) throw new IllegalStateException("No se abrió el puerto QA");
				var world = server.getOverworld();
				for (int x = -14; x <= 14; x++) for (int z = -14; z <= 20; z++)
					world.setBlockState(new BlockPos(x, 70, z), Blocks.DEEPSLATE_TILES.getDefaultState());
				System.out.println("COLOSO LAN READY");
			});
		}
		if (role.equals("reload")) return;
		server.execute(() -> {
			var world = server.getOverworld();
			if (server.getPlayerManager().getPlayerList().size() < 2 && origin == 0) return;
			if (origin == 0) origin = world.getTime();
			int next = (int) ((world.getTime() - origin) / 100);
			if (next == stage) return;
			stage = next;
			if (stage >= 8) {
				client.execute(() -> complete(client));
				return;
			}
			if (mob != null) mob.discard();
			if (dummy != null) server.getPlayerManager().remove(dummy);
			var selected = stage == 3 ? FacelessColossusEntity.SLAM
					: stage == 4 ? FacelessColossusEntity.CHARGE : FacelessColossusEntity.SWEEP;
			var probe = new ColosoGameTests.Probe(world, selected);
			probe.refreshPositionAndAngles(.5, 71, .5, 0, 0);
			probe.setAiDisabled(true);
			probe.setPersistent();
			probe.setCustomName(net.minecraft.text.Text.literal("coloso_qa_" + stage));
			if (stage == 5 || stage == 7 || stage == 4) probe.restorePhase(2);
			world.spawnEntity(probe);
			mob = probe;
			for (var player : server.getPlayerManager().getPlayerList()) {
				player.changeGameMode(GameMode.SPECTATOR);
				player.networkHandler.requestTeleport(player.getName().getString().endsWith("peer") ? 5 : -5,
						72, 8.5, player.getName().getString().endsWith("peer") ? 150 : -150, 4);
			}
			if (stage == 1 || stage == 2 || stage == 3 || stage == 4) {
				dummy = ColosoLabPlayer.connect(world);
				dummy.refreshPositionAndAngles(.5, 71, stage == 4 ? 9 : 4, 0, 0);
				probe.setTarget(dummy);
				probe.setAiDisabled(false);
				if (stage == 1) probe.started = 1;
			} else if (stage == 5) probe.triggerAnim(AnimatedBossEntity.ACTION_CONTROLLER, "phase_two");
			else if (stage == 6) probe.triggerAnim(AnimatedBossEntity.ACTION_CONTROLLER, "stun");
			if (stage == 7) {
				var nbt = new net.minecraft.nbt.NbtCompound();
				probe.writeNbt(nbt);
				probe.discard();
				mob = BossEntities.FACELESS_COLOSSUS.create(world);
				mob.readNbt(nbt);
				mob.setUuid(java.util.UUID.randomUUID());
				world.spawnEntity(mob);
			}
			System.out.println("COLOSO STAGE " + stage + " serverTime=" + world.getTime());
		});
	}

	private void openWorld(MinecraftClient client) {
		var rules = new GameRules();
		rules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
		rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
		rules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
		var loader = client.createIntegratedServerLoader();
		if (Files.exists(client.runDirectory.toPath().resolve("saves/coloso-qa/level.dat"))) {
			loader.start("coloso-qa", () -> client.setScreen(new TitleScreen()));
		} else {
			var info = new LevelInfo("Coloso QA", GameMode.SPECTATOR, false, Difficulty.NORMAL,
					true, rules, DataConfiguration.SAFE_MODE);
			loader.createAndStart("coloso-qa", info, new GeneratorOptions(0, false, false),
					registries -> registries.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.FLAT).value()
							.createDimensionsRegistryHolder(), new TitleScreen());
		}
	}

	@SuppressWarnings("unchecked")
	private void frame(MinecraftClient client) {
		if (client.world == null || client.player == null) return;
		for (var entity : client.world.getEntitiesByClass(FacelessColossusEntity.class,
				new Box(-20, 65, -20, 20, 90, 25), e -> e.hasCustomName() && e.getName().getString().startsWith("coloso_qa_"))) {
			var renderer = (GeoEntityRenderer<FacelessColossusEntity>) client.getEntityRenderDispatcher().getRenderer(entity);
			if (renderer.getRenderLayers().stream().noneMatch(layer -> layer instanceof AutoGlowingGeoLayer<?>))
				throw new IllegalStateException("Falta la emisión");
			var model = renderer.getGeoModel();
			if (client.getResourceManager().getResource(model.getTextureResource(entity)).isEmpty())
				throw new IllegalStateException("Falta la textura");
			var controller = entity.getAnimatableInstanceCache().getManagerForId(entity.getId())
					.getAnimationControllers().get(AnimatedBossEntity.ACTION_CONTROLLER);
			if (controller == null || controller.getCurrentAnimation() == null) continue;
			String clip = controller.getCurrentAnimation().animation().name();
			String key = entity.getName().getString() + "-" + clip;
			clipStarts.putIfAbsent(key, client.world.getTime());
			int impactTick = switch (clip) {
				case "sweep" -> 18;
				case "slam" -> 28;
				case "charge" -> 16;
				default -> -1;
			};
			String impact = key + "-impact";
			if (impactTick > 0 && client.world.getTime() - clipStarts.get(key) >= impactTick && captured.add(impact)) {
				ScreenshotRecorder.saveScreenshot(client.runDirectory, impact + ".png", client.getFramebuffer(),
						text -> System.out.println("COLOSO IMPACT CAPTURE " + impact));
			}
			if (!captured.add(key)) continue;
			System.out.println("COLOSO CLIENT " + role + " " + key + " cracked=" + entity.hasCrackedArmor()
					+ " box=" + entity.getWidth() + "x" + entity.getHeight() + " time=" + client.world.getTime());
			ScreenshotRecorder.saveScreenshot(client.runDirectory, key + ".png", client.getFramebuffer(),
					text -> System.out.println(text.getString()));
			frames++;
		}
		if (role.equals("peer") && captured.containsAll(REQUIRED)) {
			complete(client);
		}
	}

	private void complete(MinecraftClient client) {
		if (!captured.containsAll(REQUIRED)) throw new IllegalStateException("Falta una comprobación de cliente " + captured);
		System.out.println("COLOSO " + role.toUpperCase(java.util.Locale.ROOT) + " COMPLETE captures=" + frames);
		client.scheduleStop();
	}
}
