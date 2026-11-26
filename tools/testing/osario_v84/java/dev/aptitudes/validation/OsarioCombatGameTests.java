package dev.aptitudes.validation;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.mob.OsarioEntities;
import dev.aptitudes.mob.OsarioEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class OsarioCombatGameTests implements FabricGameTest {
	private static final String EMPTY = FabricGameTest.EMPTY_STRUCTURE;
	private static final int LOGIN_WARMUP = 70;

	@GameTest(templateName = EMPTY, tickLimit = 105, batchId = "externalTargetAlerts")
	public void externalTargetAlerts(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 3.5);
		var player = arena.player(3.5, 1, 6.5);
		context.runAtTick(LOGIN_WARMUP + 4, () -> {
			mob.setAiDisabled(false);
			mob.setTarget(player);
		});
		context.runAtTick(LOGIN_WARMUP + 7, () -> {
			context.assertEquals(1, action(mob), "La detección externa debe activar la alerta");
			context.assertTrue(mob.isAttacking(), "Persecución sincronizada");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 105, batchId = "clawHitsEachFrontPlayerOnceAndLeavesRearSafe")
	public void clawHitsEachFrontPlayerOnceAndLeavesRearSafe(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 3.5);
		var left = arena.player(2.95, 1, 5.1);
		var right = arena.player(4.05, 1, 5.1);
		var behind = arena.player(3.5, 1, 2.1);
		context.runAtTick(LOGIN_WARMUP + 4, () -> context.assertTrue(begin(mob, left, "CLAW"), "El zarpazo se inicia"));
		context.runAtTick(LOGIN_WARMUP + 13, () -> {
			health(context, left, 20);
			health(context, right, 20);
		});
		context.runAtTick(LOGIN_WARMUP + 16, () -> {
			health(context, left, 15.5f);
			health(context, right, 15.5f);
			health(context, behind, 20);
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 112, batchId = "heavyRespectsWallsAndGiantReach")
	public void heavyRespectsWallsAndGiantReach(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 2.5);
		mob.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(1.35);
		var open = arena.player(2.2, 1, 5.15);
		var behindWall = arena.player(4.85, 1, 5.15);
		context.setBlockState(4, 1, 4, Blocks.STONE);
		context.setBlockState(4, 2, 4, Blocks.STONE);
		context.runAtTick(LOGIN_WARMUP + 4, () -> context.assertTrue(begin(mob, open, "HEAVY"), "El barrido se inicia"));
		context.runAtTick(LOGIN_WARMUP + 22, () -> health(context, open, 20));
		context.runAtTick(LOGIN_WARMUP + 26, () -> {
			context.assertTrue(mob.getScale() > 1.34f && mob.getWidth() > 1, "Escala física 1,35");
			health(context, open, 13.5f);
			health(context, behindWall, 20);
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 113, batchId = "slamUsesShiftedDiskAndVerticalBand")
	public void slamUsesShiftedDiskAndVerticalBand(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 2.5);
		var low = arena.player(5.0, 1, 4.15);
		var high = arena.player(2.4, 2.65, 4.2);
		var outside = arena.player(.85, 1, 4);
		context.runAtTick(LOGIN_WARMUP + 4, () -> context.assertTrue(begin(mob, low, "SLAM"), "El impacto se inicia"));
		context.runAtTick(LOGIN_WARMUP + 26, () -> health(context, low, 20));
		context.runAtTick(LOGIN_WARMUP + 30, () -> {
			health(context, low, 12);
			health(context, high, 20);
			health(context, outside, 20);
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 112, batchId = "dashSweepsAndStopsAtFirstPlayer")
	public void dashSweepsAndStopsAtFirstPlayer(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 1.85);
		var near = arena.player(3.5, 1, 3.8);
		var far = arena.player(3.5, 1, 6.3);
		double origin = mob.getZ();
		double[] lastZ = {origin};
		context.runAtTick(LOGIN_WARMUP + 4, () -> context.assertTrue(begin(mob, far, "LUNGE"), "La embestida se inicia"));
		context.runAtEveryTick(() -> {
			double z = mob.getZ();
			context.assertTrue(Math.abs(z - lastZ[0]) <= .55, "Paso continuo de embestida");
			lastZ[0] = z;
		});
		context.runAtTick(LOGIN_WARMUP + 18, () -> health(context, near, 20));
		context.runAtTick(LOGIN_WARMUP + 27, () -> {
			health(context, near, 15);
			health(context, far, 20);
			context.assertTrue(mob.getZ() > origin + .6 && mob.getZ() < near.getZ(), "Avance y frenado al primer contacto");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 112, batchId = "dashAbortsAtLateWall")
	public void dashAbortsAtLateWall(TestContext context) {
		if (skip(context)) return;
		dashBlocked(context, "wall");
	}

	@GameTest(templateName = EMPTY, tickLimit = 112, batchId = "dashAbortsAtLedge")
	public void dashAbortsAtLedge(TestContext context) {
		if (skip(context)) return;
		dashBlocked(context, "ledge");
	}

	@GameTest(templateName = EMPTY, tickLimit = 112, batchId = "dashAbortsAtWater")
	public void dashAbortsAtWater(TestContext context) {
		if (skip(context)) return;
		dashBlocked(context, "water");
	}

	private void dashBlocked(TestContext context, String hazard) {
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 1.85);
		var far = arena.player(3.5, 1, 6.3);
		double threshold = context.getAbsolute(new Vec3d(3.5, 1, 3)).z;
		context.runAtTick(LOGIN_WARMUP + 4, () -> context.assertTrue(begin(mob, far, "LUNGE"), "La embestida se inicia"));
		context.runAtTick(LOGIN_WARMUP + 17, () -> {
			for (int x = 2; x <= 4; x++) {
				if (hazard.equals("wall")) {
					context.setBlockState(x, 1, 3, Blocks.STONE);
					context.setBlockState(x, 2, 3, Blocks.STONE);
				} else if (hazard.equals("ledge")) {
					context.setBlockState(x, 0, 3, Blocks.AIR);
				} else {
					context.setBlockState(x, 1, 3, Blocks.WATER);
				}
			}
		});
		context.runAtTick(LOGIN_WARMUP + 27, () -> {
			health(context, far, 20);
			context.assertTrue(mob.getZ() <= threshold, "Frena antes de " + hazard);
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 105, batchId = "invalidTargetCancelsWindupAndReleasesAction")
	public void invalidTargetCancelsWindupAndReleasesAction(TestContext context) {
		if (skip(context)) return;
		targetLoss(context, player -> player.changeGameMode(GameMode.SPECTATOR));
	}

	@GameTest(templateName = EMPTY, tickLimit = 105, batchId = "deadTargetCancelsWindup")
	public void deadTargetCancelsWindup(TestContext context) {
		if (skip(context)) return;
		targetLoss(context, player -> player.damage(player.getDamageSources().genericKill(), 500));
	}

	@GameTest(templateName = EMPTY, tickLimit = 105, batchId = "disconnectCancelsWindup")
	public void disconnectCancelsWindup(TestContext context) {
		if (skip(context)) return;
		targetLoss(context, player -> player.getServer().getPlayerManager().remove(player));
	}

	@GameTest(templateName = EMPTY, tickLimit = 105, batchId = "dimensionChangeCancelsWindup")
	public void dimensionChangeCancelsWindup(TestContext context) {
		if (skip(context)) return;
		var nether = java.util.Objects.requireNonNull(context.getWorld().getServer().getWorld(World.NETHER));
		targetLoss(context, player -> {
			player.teleport(nether, .5, 80, .5, 0, 0);
			context.assertTrue(player.getWorld() == nether, "El jugador debe cambiar de dimensión");
		});
	}

	private void targetLoss(TestContext context, Consumer<ServerPlayerEntity> loseTarget) {
		var arena = new Arena(context);
		var mob = arena.mob(3.5, 3.5);
		var target = arena.player(3.5, 1, 5.1);
		var other = arena.player(3.0, 1, 5.1);
		context.runAtTick(LOGIN_WARMUP + 4, () -> context.assertTrue(begin(mob, target, "CLAW"), "El zarpazo se inicia"));
		context.runAtTick(LOGIN_WARMUP + 8, () -> loseTarget.accept(target));
		context.runAtTick(LOGIN_WARMUP + 17, () -> {
			health(context, other, 20);
			context.assertTrue(action(mob) == 0 || action(mob) == 1, "Fin del golpe y posible alerta de otro objetivo");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 130, batchId = "cryptChampionUsesOssuaryCombat")
	public void cryptChampionUsesOssuaryCombat(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var target = arena.player(3.5, 1, 4.4);
		var champion = cryptChampion(context);
		champion.setAiDisabled(true);
		arena.mobs.add(champion);
		context.assertTrue(Elites.isChampion(champion), "La Cripta debe crear un Osario campeón");
		context.runAtTick(LOGIN_WARMUP + 4, () ->
				context.assertTrue(begin(champion, target, "SLAM"), "El campeón inicia el impacto"));
		context.runAtTick(LOGIN_WARMUP + 26, () -> health(context, target, 20));
		context.runAtTick(LOGIN_WARMUP + 34, () -> {
			context.assertTrue(target.getHealth() < 20, "El campeón resuelve el impacto en su ventana");
			arena.complete();
		});
	}

	private static OsarioEntity cryptChampion(TestContext context) {
		try {
			var factory = Class.forName("dev.aptitudes.temple.CryptWaves")
					.getDeclaredMethod("spawnChampion", ServerWorld.class, BlockPos.class, int.class, ServerPlayerEntity.class);
			factory.setAccessible(true);
			return (OsarioEntity) java.util.Objects.requireNonNull(
					factory.invoke(null, context.getWorld(), context.getAbsolutePos(new BlockPos(3, 1, 2)), 2, null));
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Aparición del campeón de la Cripta", e);
		}
	}

	@GameTest(templateName = EMPTY, tickLimit = 94, batchId = "threeAttackSlotsReleaseOnDeath")
	public void threeAttackSlotsReleaseOnDeath(TestContext context) {
		if (skip(context)) return;
		var arena = new Arena(context);
		var mobs = List.of(arena.mob(2, 2), arena.mob(3.5, 2), arena.mob(5, 2), arena.mob(6.5, 2));
		var player = arena.player(3.5, 1, 5.5);
		context.runAtTick(LOGIN_WARMUP + 4, () -> {
			for (int i = 0; i < 3; i++) context.assertTrue(begin(mobs.get(i), player, "HEAVY"), "Ficha reservada");
			context.assertFalse(begin(mobs.get(3), player, "HEAVY"), "La cuarta ficha se rechaza");
			mobs.getFirst().damage(context.getWorld().getDamageSources().genericKill(), 500);
			context.assertEquals(7, action(mobs.getFirst()), "Muerte prioritaria");
			context.assertTrue(begin(mobs.get(3), player, "HEAVY"), "La muerte libera la ficha");
			arena.complete();
		});
	}

	private static boolean skip(TestContext context) {
		if ("combat".equals(System.getProperty("osario.qa.suite", "combat"))) return false;
		context.complete();
		return true;
	}

	private static void health(TestContext ctx, ServerPlayerEntity player, float value) {
		ctx.assertTrue(Math.abs(player.getHealth() - value) < .02, "Vida " + player.getHealth() + ", esperada " + value);
	}

	private static int action(OsarioEntity mob) {
		return (int) stamp(mob) & 15;
	}

	static long stamp(OsarioEntity mob) {
		try {
			var stamp = OsarioEntity.class.getDeclaredMethod("actionStamp");
			stamp.setAccessible(true);
			return (Long) stamp.invoke(mob);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Lectura de la acción", e);
		}
	}

	/** Selecciona el ataque del escenario y deja que el servidor ejecute sus ticks reales. */
	static boolean begin(OsarioEntity mob, ServerPlayerEntity target, String selected) {
		try {
			var field = OsarioEntity.class.getDeclaredField("strikeGoal");
			field.setAccessible(true);
			Object goal = field.get(mob);
			Class<?> attack = Class.forName("dev.aptitudes.mob.OsarioStrikeRules$Attack");
			Object chosen = java.util.Arrays.stream(attack.getEnumConstants()).filter(a -> ((Enum<?>) a).name().equals(selected)).findFirst().orElseThrow();
			var method = goal.getClass().getDeclaredMethod("begin", ServerWorld.class, net.minecraft.entity.LivingEntity.class, attack, long.class, float.class);
			method.setAccessible(true);
			mob.setAiDisabled(false);
			mob.setTarget(target);
			return (Boolean) method.invoke(goal, mob.getWorld(), target, chosen, mob.getWorld().getTime(), 0f);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Ejecución del caso", e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Preparación del caso", e);
		}
	}

	private static final class Arena {
		private final TestContext context;
		private final List<OsarioEntity> mobs = new ArrayList<>();
		private final List<ServerPlayerEntity> players = new ArrayList<>();

		Arena(TestContext context) {
			this.context = context;
			Aptitudes.config().threatEnabled = false;
			Aptitudes.config().eliteChance = 0;
			context.getWorld().getServer().setDifficulty(Difficulty.NORMAL, true);
			for (int x = -1; x <= 8; x++) for (int z = -1; z <= 8; z++) context.setBlockState(x, 0, z, Blocks.STONE);
		}

		OsarioEntity mob(double x, double z) {
			var mob = context.spawnEntity(OsarioEntities.OSARIO, new Vec3d(x, 1, z));
			mob.setPersistent();
			mob.setAiDisabled(true);
			mob.setYaw(0);
			mob.setBodyYaw(0);
			mobs.add(mob);
			return mob;
		}

		ServerPlayerEntity player(double x, double y, double z) {
			var player = LabPlayer.connect(context.getWorld());
			Vec3d pos = context.getAbsolute(new Vec3d(x, y, z));
			player.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
			player.setNoGravity(true);
			players.add(player);
			return player;
		}

		void complete() {
			for (var player : players) {
				var manager = player.getWorld().getServer().getPlayerManager();
				if (manager.getPlayer(player.getUuid()) == player) manager.remove(player);
			}
			for (var mob : mobs) mob.discard();
			context.complete();
		}
	}
}
