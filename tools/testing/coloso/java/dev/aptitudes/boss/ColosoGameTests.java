package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.MovementType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public final class ColosoGameTests implements FabricGameTest {
	private static final String EMPTY = FabricGameTest.EMPTY_STRUCTURE;
	private static final int START = 75;

	@GameTest(templateName = EMPTY, tickLimit = 240, batchId = "colosoSweep")
	public void sweepUsesServerTickAndFixedDirection(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.SWEEP);
		var front = arena.player(3.5, 1, 6.5);
		var rear = arena.player(3.5, 1, .5);
		context.runAtTick(START, () -> arena.start(mob, front));
		arena.after(mob, 17, () -> context.assertEquals(0, mob.hits, "Aviso completo antes del tajo"));
		arena.after(mob, 20, () -> {
			context.assertEquals(18L, mob.impactAt - mob.startedAt, "Impacto en el tick 18");
			context.assertEquals(1, mob.hits, "Un golpe frontal");
			context.assertTrue(front.getHealth() < 20 && rear.getHealth() == 20, "El arco protege a quien está detrás");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 250, batchId = "colosoSlam")
	public void slamRespectsWallsAndSeparatedFloors(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.SLAM);
		var front = arena.player(3.5, 1, 6.5);
		var high = arena.player(1.5, 3, 6.5);
		var blocked = arena.player(6.5, 1, 3.5);
		for (int y = 1; y <= 4; y++) for (int z = 2; z <= 4; z++) context.setBlockState(5, y, z, Blocks.STONE);
		context.runAtTick(START, () -> arena.start(mob, front));
		arena.after(mob, 27, () -> context.assertEquals(0, mob.hits, "Aviso completo antes del golpe"));
		arena.after(mob, 30, () -> {
			context.assertEquals(28L, mob.impactAt - mob.startedAt, "Impacto en el tick 28");
			context.assertTrue(front.getHealth() < 20, "La onda alcanza el suelo");
			context.assertTrue(high.getHealth() == 20 && blocked.getHealth() == 20, "La pared y el piso elevado protegen");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 260, batchId = "colosoRecovery")
	public void recoveryKeepsTheAnnouncedDirection(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.SWEEP);
		var player = arena.player(3.5, 1, 6.5);
		context.runAtTick(START, () -> arena.start(mob, player));
		arena.after(mob, 19, () -> player.refreshPositionAndAngles(mob.getX() + 3, mob.getY(), mob.getZ(), 0, 0));
		arena.after(mob, 25, () -> {
			context.assertTrue(Math.abs(mob.getBodyYaw()) < 1, "La recuperación conserva la dirección del tajo");
			context.assertEquals(1, mob.started, "La recuperación no empieza otro ataque");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 255, batchId = "colosoCharge")
	public void chargeReachesItsTargetAndHitsOnce(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.CHARGE);
		var player = arena.player(3.5, 1, 10.5);
		context.runAtTick(START, () -> arena.start(mob, player));
		arena.after(mob, 15, () -> context.assertEquals(0, mob.hits, "La embestida respeta el aviso"));
		arena.after(mob, 30, () -> {
			context.assertEquals(16L, mob.impactAt - mob.startedAt, "La carga empieza en el tick 16");
			context.assertEquals(1, mob.hits, "La embestida alcanza al objetivo una sola vez");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 255, batchId = "colosoChargeWall")
	public void chargeStopsAtWall(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.CHARGE);
		var player = arena.player(3.5, 1, 10.5);
		context.runAtTick(START, () -> arena.start(mob, player));
		arena.after(mob, 12, () -> {
			for (int y = 1; y <= 5; y++) for (int x = 1; x <= 6; x++) context.setBlockState(x, y, 6, Blocks.STONE);
		});
		arena.after(mob, 26, () -> {
			context.assertEquals(0, mob.hits, "La pared impide el daño");
			context.assertTrue(mob.getZ() < context.getAbsolute(new Vec3d(0, 0, 6)).z, "La pared detiene el cuerpo");
			context.assertTrue(mob.getVelocity().horizontalLength() < .02, "La embestida no conserva impulso");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 255, batchId = "colosoDodge")
	public void movingSidewaysDodgesCharge(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.CHARGE);
		var player = arena.player(3.5, 1, 10.5);
		context.runAtTick(START, () -> arena.start(mob, player));
		arena.after(mob, 10, () -> player.refreshPositionAndAngles(mob.getX() + 4, mob.getY(), mob.getZ() + 7, 0, 0));
		arena.after(mob, 30, () -> {
			context.assertEquals(0, mob.hits, "La carga mantiene su dirección y puede esquivarse");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 240, batchId = "colosoInvalidTarget")
	public void creativeTargetCancelsWindup(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.SLAM);
		var player = arena.player(3.5, 1, 6.5);
		context.runAtTick(START, () -> arena.start(mob, player));
		arena.after(mob, 10, () -> player.changeGameMode(GameMode.CREATIVE));
		arena.after(mob, 32, () -> {
			context.assertEquals(0, mob.hits, "Se cancela el golpe al invalidarse el objetivo");
			context.assertTrue(mob.getNavigation().isIdle(), "Se cancela la persecución");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 190, batchId = "colosoPersistence")
	public void restoredPhaseKeepsArmorAndSingleSpeedModifier(TestContext context) {
		var arena = new Arena(context);
		var original = arena.mob(FacelessColossusEntity.SWEEP);
		original.restorePhase(2);
		original.restorePhase(2);
		var nbt = new NbtCompound();
		original.writeNbt(nbt);
		var restored = BossEntities.FACELESS_COLOSSUS.create(context.getWorld());
		context.assertTrue(restored != null, "La entidad registrada puede restaurarse");
		restored.readNbt(nbt);
		context.assertEquals(2, restored.phase(), "La fase persiste en NBT");
		context.assertTrue(restored.hasCrackedArmor(), "La armadura agrietada persiste");
		context.assertTrue(Math.abs(restored.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) - .299) < .0001,
				"El bono de velocidad se aplica una sola vez");
		context.assertTrue(Math.abs(restored.getWidth() - 1.6) < .001 && Math.abs(restored.getHeight() - 3.4) < .001,
				"Caja física 1,6 por 3,4");
		arena.complete();
	}

	@GameTest(templateName = EMPTY, tickLimit = 240, batchId = "colosoSelection")
	public void crownIsSelectableAndLowCeilingBlocksTheBody(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.SWEEP);
		Box box = mob.getBoundingBox();
		var eye = mob.getPos().add(0, 3.3, 5);
		context.assertTrue(box.raycast(eye, eye.add(0, 0, -10)).isPresent(), "El rayo selecciona la corona");
		for (int x = 2; x <= 4; x++) for (int z = 2; z <= 4; z++) context.setBlockState(x, 4, z, Blocks.STONE);
		context.assertFalse(context.getWorld().isSpaceEmpty(mob, box), "Un techo de tres bloques impide el paso");
		arena.complete();
	}

	@GameTest(templateName = EMPTY, tickLimit = 255, batchId = "colosoStun")
	public void stunCancelsChargeDuringPhaseChange(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.CHARGE);
		var player = arena.player(3.5, 1, 12.5);
		context.runAtTick(START, () -> arena.start(mob, player));
		arena.after(mob, 17, () -> {
			mob.damage(mob.getDamageSources().playerAttack(player), 100);
			mob.setHealth(50);
		});
		arena.after(mob, 29, () -> {
			context.assertTrue(mob.isPostureStunned(context.getWorld().getTime()), "El aturdimiento prevalece al cambiar de fase");
			context.assertEquals(3, mob.phase(), "La fase de daño también se actualiza");
			context.assertEquals(0, mob.hits, "El aturdimiento cancela la carga");
			context.assertTrue(mob.getVelocity().horizontalLength() < .02, "Se retira el impulso de la carga");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY, tickLimit = 240, batchId = "colosoPassage")
	public void bodyFitsTwoByThreeAndHalfPassage(TestContext context) {
		var arena = new Arena(context);
		var mob = arena.mob(FacelessColossusEntity.SWEEP);
		var base = context.getAbsolute(new Vec3d(0, 0, 0));
		mob.refreshPositionAndAngles(base.x + 3, base.y + 1, base.z + 2, 0, 0);
		for (int z = 1; z <= 8; z++) {
			for (int y = 1; y <= 4; y++) {
				context.setBlockState(1, y, z, Blocks.STONE);
				context.setBlockState(4, y, z, Blocks.STONE);
			}
			for (int x = 2; x <= 3; x++) context.setBlockState(x, 4, z,
					Blocks.STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP));
		}
		context.assertTrue(context.getWorld().isSpaceEmpty(mob, mob.getBoundingBox()), "Cabe bajo el techo de 3,5 bloques");
		mob.move(MovementType.SELF, new Vec3d(0, 0, 4));
		context.assertTrue(Math.abs(mob.getZ() - (base.z + 6)) < .01, "La física permite atravesar el pasillo");
		arena.complete();
	}

	@GameTest(templateName = EMPTY, tickLimit = 280, batchId = "colosoNavigation")
	public void registeredMobAcquiresAndApproachesSurvivalPlayer(TestContext context) {
		var arena = new Arena(context);
		var mob = context.spawnEntity(BossEntities.FACELESS_COLOSSUS, new Vec3d(3.5, 1, 3.5));
		mob.setAiDisabled(true);
		arena.mobs.add(mob);
		var player = arena.player(3.5, 1, 14.5);
		double start = mob.squaredDistanceTo(player);
		context.runAtTick(START, () -> mob.setAiDisabled(false));
		context.runAtTick(START + 100, () -> {
			context.assertTrue(mob.getTarget() == player, "La entidad registrada adquiere al jugador de supervivencia");
			context.assertTrue(mob.squaredDistanceTo(player) < start * .7, "La navegación acerca al Coloso al objetivo");
			arena.complete();
		});
	}

	static final class Probe extends FacelessColossusEntity {
		final BossMoveRules.Move selected;
		int started;
		int hits;
		long startedAt;
		long impactAt;

		Probe(ServerWorld world, BossMoveRules.Move selected) {
			super(BossEntities.FACELESS_COLOSSUS, world);
			this.selected = selected;
			targetSelector.clear(goal -> true);
		}

		@Override
		protected List<BossMoveRules.Move> moves() { return List.of(selected); }

		@Override
		protected boolean canStartAttack(LivingEntity target) { return started == 0 && super.canStartAttack(target); }

		@Override
		protected void onAttackStarted(BossMoveRules.Move move, LivingEntity target) {
			super.onAttackStarted(move, target);
			started++;
			startedAt = getWorld().getTime();
			System.out.println("COLOSO START " + move.id() + " t=" + startedAt + " target=" + target.getPos());
		}

		@Override
		protected void strike(ServerWorld world, BossMoveRules.Move move, LivingEntity target) {
			impactAt = world.getTime();
			System.out.println("COLOSO IMPACT " + move.id() + " elapsed=" + (impactAt - startedAt));
			super.strike(world, move, target);
		}

		@Override
		protected boolean hit(LivingEntity target, float damage, double knockback) {
			boolean applied = super.hit(target, damage, knockback);
			if (applied) hits++;
			return applied;
		}
	}

	private static final class Arena {
		private final TestContext context;
		private final List<FacelessColossusEntity> mobs = new ArrayList<>();
		private final List<ServerPlayerEntity> players = new ArrayList<>();

		Arena(TestContext context) {
			this.context = context;
			Aptitudes.config().threatEnabled = false;
			Aptitudes.config().eliteChance = 0;
			context.getWorld().getServer().setDifficulty(Difficulty.NORMAL, true);
			for (int x = -2; x <= 15; x++) for (int z = -2; z <= 18; z++) {
				context.setBlockState(x, 0, z, Blocks.STONE);
				for (int y = 1; y <= 6; y++) context.setBlockState(x, y, z, Blocks.AIR);
			}
		}

		Probe mob(BossMoveRules.Move move) {
			var mob = new Probe(context.getWorld(), move);
			Vec3d pos = context.getAbsolute(new Vec3d(3.5, 1, 3.5));
			mob.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
			mob.setPersistent();
			mob.setAiDisabled(true);
			if (move == FacelessColossusEntity.CHARGE) mob.restorePhase(2);
			context.getWorld().spawnEntity(mob);
			mobs.add(mob);
			return mob;
		}

		ServerPlayerEntity player(double x, double y, double z) {
			var player = ColosoLabPlayer.connect(context.getWorld());
			Vec3d pos = context.getAbsolute(new Vec3d(x, y, z));
			player.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
			player.setNoGravity(true);
			players.add(player);
			return player;
		}

		void start(Probe mob, ServerPlayerEntity player) {
			mob.setTarget(player);
			mob.setAiDisabled(false);
		}

		void after(Probe mob, int elapsed, Runnable action) {
			boolean[] ran = {false};
			context.runAtEveryTick(() -> {
				if (!ran[0] && mob.startedAt > 0 && context.getWorld().getTime() >= mob.startedAt + elapsed) {
					ran[0] = true;
					action.run();
				}
			});
		}

		void complete() {
			for (var player : players) player.getServer().getPlayerManager().remove(player);
			for (var mob : mobs) mob.discard();
			context.complete();
		}
	}
}
