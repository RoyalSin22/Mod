package dev.aptitudes.validation;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.mob.NewMobs;
import dev.aptitudes.mob.OsarioEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class OsarioBenchmarkGameTests implements FabricGameTest {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 50000)
	public void serverMspt(TestContext context) {
		String suite = System.getProperty("osario.qa.suite", "combat");
		if (!suite.startsWith("bench")) {
			context.complete();
			return;
		}
		new Series(context, suite).start();
	}

	private static final class Series {
		private static final int[] COUNTS = {1, 5, 10, 20, 30};
		private final TestContext ctx;
		private final MinecraftServer server;
		private final String suite;
		private final boolean paired;
		private final int warm = Integer.getInteger("osario.qa.warm", 160);
		private final int samples = Integer.getInteger("osario.qa.samples", 320);
		private final List<MobEntity> mobs = new ArrayList<>();
		private final List<ChunkPos> forcedChunks = new ArrayList<>();
		private final StringBuilder table = new StringBuilder(
				"suite,case,count,warm_ticks,samples,wall_seconds,mean_ms,p95_ms,p99_ms,heap_bytes,min_mob_ticks,target_fraction,mob,round,damage_ticks\n");
		private final long[] values;
		private int[] mobAgesAtSampleStart;
		private int index, sampleIndex, scene;
		private int pair;
		private long started, wallStart;
		private int targetObservations;
		private int damageTicks;
		private ServerPlayerEntity player;

		Series(TestContext ctx, String suite) {
			this.ctx = ctx;
			this.server = ctx.getWorld().getServer();
			this.suite = suite;
			this.paired = suite.endsWith("_paired");
			if (paired) index = COUNTS.length - 1;
			this.values = new long[samples];
		}

		void start() {
			Aptitudes.config().threatEnabled = false;
			Aptitudes.config().eliteChance = 0;
			server.setDifficulty(Difficulty.NORMAL, true);
			ctx.getWorld().getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
			ctx.getWorld().getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
			ctx.getWorld().setTimeOfDay(18000);
			var min = new ChunkPos(BlockPos.ofFloored(ctx.getAbsolute(new Vec3d(-20, 0, -20))));
			var max = new ChunkPos(BlockPos.ofFloored(ctx.getAbsolute(new Vec3d(20, 0, 22))));
			for (int x = min.x - 1; x <= max.x + 1; x++) for (int z = min.z - 1; z <= max.z + 1; z++) {
				if (ctx.getWorld().setChunkForced(x, z, true)) forcedChunks.add(new ChunkPos(x, z));
			}
			for (int x = -20; x <= 20; x++) for (int z = -20; z <= 22; z++) {
				ctx.setBlockState(x, 0, z, Blocks.STONE);
				if (Math.abs(x) == 20 || z == -20 || z == 22) {
					for (int y = 1; y <= 3; y++) ctx.setBlockState(x, y, z, Blocks.STONE);
				}
			}
			if (suite.contains("sprint")) server.getTickManager().startSprint(50000);
			ctx.runAtTick(12, this::spawn);
			ctx.runAtEveryTick(this::tick);
		}

		void spawn() {
			started = ctx.getTick();
			sampleIndex = 0;
			targetObservations = 0;
			damageTicks = 0;
			if (scene == 1 && player == null) {
				player = LabPlayer.connect(ctx.getWorld());
				player.setNoGravity(true);
			}
			anchorPlayer();
			for (int i = 0; i < COUNTS[index]; i++) {
				var type = errante() ? NewMobs.ERRANTE : OsarioEntities.OSARIO;
				var mob = ctx.spawnEntity(type, new Vec3d(-8 + (i % 6) * 3, 1, -7 + (i / 6) * 3));
				mob.setPersistent();
				mob.setInvulnerable(true);
				if (player != null) mob.setTarget(player);
				mobs.add(mob);
			}
			System.out.printf("OSARIO QA inicio %s escena=%d grupo=%d%n", suite, scene, COUNTS[index]);
		}

		private void anchorPlayer() {
			if (player == null) return;
			Vec3d pos = ctx.getAbsolute(new Vec3d(.5, 1, 15));
			player.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
			player.setVelocity(Vec3d.ZERO);
		}

		void tick() {
			if (mobs.isEmpty()) return;
			if (player != null && player.getHealth() < player.getMaxHealth()) {
				if (ctx.getTick() - started >= warm) damageTicks++;
				player.setHealth(player.getMaxHealth());
			}
			anchorPlayer();
			long age = ctx.getTick() - started;
			if (age < warm) return;
			if (sampleIndex == 0) {
				wallStart = System.nanoTime();
				mobAgesAtSampleStart = mobs.stream().mapToInt(mob -> mob.age).toArray();
			}
			// Los GameTest corren durante el tick. El real recién escrito es el del tick anterior.
			long[] ring = server.getTickTimes();
			long nanos = ring[Math.floorMod(server.getTicks() - 1, ring.length)];
			if (nanos <= 0) return;
			values[sampleIndex++] = nanos;
			for (MobEntity mob : mobs) {
				if (mob.getTarget() != null) targetObservations++;
			}
			if (sampleIndex < samples) return;
			int minMobTicks = samples;
			for (int i = 0; i < mobs.size(); i++) {
				var mob = mobs.get(i);
				int entityTicks = mob.age - mobAgesAtSampleStart[i];
				minMobTicks = Math.min(minMobTicks, entityTicks);
				ctx.assertTrue(entityTicks >= samples - 2, "Ticks de muestra de la entidad " + i + ": "
						+ entityTicks + ". Posición " + mob.getPos() + ", edad " + mob.age + ", retirada " + mob.getRemovalReason());
			}
			var sorted = values.clone();
			Arrays.sort(sorted);
			double mean = Arrays.stream(values).average().orElseThrow() / 1_000_000;
			double elapsed = (System.nanoTime() - wallStart) / 1_000_000_000.0;
			Runtime vm = Runtime.getRuntime();
			long heap = vm.totalMemory() - vm.freeMemory();
			ctx.assertTrue(scene == 0 || damageTicks > 0, "La escena de persecución debe registrar daño real");
			String result = String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%.3f,%.4f,%.4f,%.4f,%d,%d,%.4f,%s,%d,%d%n",
					suite, scene == 0 ? "sin_jugador" : "persecucion", COUNTS[index], warm, samples, elapsed, mean,
					sorted[(int) Math.ceil(samples * .95) - 1] / 1_000_000.0,
					sorted[(int) Math.ceil(samples * .99) - 1] / 1_000_000.0, heap, minMobTicks,
					(double) targetObservations / (samples * mobs.size()), errante() ? "errante" : "osario", pair / 2 + 1, damageTicks);
			System.out.print("OSARIO QA " + result);
			table.append(result);
			try {
				Files.writeString(Path.of("server-mspt.csv"), table);
			} catch (IOException ex) {
				throw new IllegalStateException("Guardar medidas", ex);
			}
			for (MobEntity mob : mobs) mob.discard();
			mobs.clear();
			if (paired) {
				if (++pair == 6) {
					pair = 0;
					scene++;
				}
			} else if (++index == COUNTS.length) {
				index = 0;
				scene++;
			}
			if (scene >= 2) {
				if (player != null) server.getPlayerManager().remove(player);
				for (ChunkPos chunk : forcedChunks) ctx.getWorld().setChunkForced(chunk.x, chunk.z, false);
				ctx.complete();
			} else {
				ctx.waitAndRun(10, this::spawn);
			}
		}

		private boolean errante() {
			return suite.contains("errante") || (paired && (pair % 4 == 1 || pair % 4 == 2));
		}
	}
}
