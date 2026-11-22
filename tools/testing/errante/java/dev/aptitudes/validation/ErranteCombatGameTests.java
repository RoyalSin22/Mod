package dev.aptitudes.validation;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.mob.ErranteEntity;
import dev.aptitudes.mob.NewMobs;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.unique.UniqueManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.List;

/** Regresiones del Errante con navegación y reloj del servidor reales. */
public final class ErranteCombatGameTests implements FabricGameTest {
	private static final int START = 74;

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 110, batchId = "erranteTargetSwitch")
	public void targetSwitchBeforeWarningCancelsOldAttack(TestContext context) {
		var arena = new Arena(context);
		var first = arena.player(3.5, 5);
		var second = arena.player(5, 3.5);
		Object[] previous = new Object[1];
		context.runAtTick(START, () -> {
			begin(arena.mob, first);
			previous[0] = field(arena.mob, "rend");
		});
		context.runAtTick(START + 2, () -> {
			arena.mob.setTarget(second);
			context.assertTrue(field(arena.mob, "rend") == null, "Cambio de jugador cancela inmediatamente");
		});
		context.runAtTick(START + 4, () -> {
			context.assertTrue(field(arena.mob, "rend") != previous[0], "La ejecución anterior no se recupera");
		});
		context.runAtTick(START + 11, () -> {
			context.assertEquals(20f, first.getHealth(), "El ataque cancelado deja intacto al primer jugador");
			context.assertEquals(20f, second.getHealth(), "El segundo jugador recibe su propia anticipación");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 110, batchId = "erranteWarningBoundary")
	public void targetSwitchOnWarningTickCancelsBeforeSignal(TestContext context) {
		var arena = new Arena(context);
		var first = arena.player(3.5, 5);
		var second = arena.player(5, 3.5);
		context.runAtTick(START, () -> {
			begin(arena.mob, first);
			arena.mob.setAiDisabled(true);
		});
		context.runAtTick(START + 6, () -> {
			arena.mob.setTarget(second);
			context.assertTrue(field(arena.mob, "rend") == null, "Cambio detectado antes de emitir el aviso");
			context.assertEquals(0, action(arena.mob), "La acción cancelada libera el controlador");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 110, batchId = "erranteLockedDirection")
	public void targetSwitchAfterWarningPreservesAnnouncedSector(TestContext context) {
		var arena = new Arena(context);
		var first = arena.player(3.5, 5);
		var second = arena.player(5, 3.5);
		context.runAtTick(START, () -> begin(arena.mob, first));
		context.runAtTick(START + 7, () -> arena.mob.setTarget(second));
		context.runAtTick(START + 11, () -> {
			context.assertEquals(14f, first.getHealth(), "El sector anunciado conserva el impacto de 6");
			context.assertEquals(20f, second.getHealth(), "El ataque anunciado no gira hacia el segundo jugador");
			context.assertTrue(Math.abs(arena.mob.getYaw()) < 0.01, "Dirección original conservada");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 110, batchId = "erranteHurtNavigation")
	public void hurtStopsPathAndResumesPursuit(TestContext context) {
		var arena = new Arena(context);
		var player = arena.player(3.5, 7);
		// Permite que la física registre el apoyo antes de solicitar una ruta de suelo.
		context.runAtTick(START - 2, () -> arena.mob.setAiDisabled(false));
		context.runAtTick(START, () -> {
			arena.mob.setAiDisabled(false);
			arena.mob.setTarget(player);
			setField(arena.mob, "alertedTarget", player.getUuid());
			context.assertTrue(arena.mob.getNavigation().startMovingTo(player, 1),
					"Ruta desde " + arena.mob.getPos() + " a " + player.getPos()
							+ "; apoyado " + arena.mob.isOnGround());
			context.assertTrue(arena.mob.damage(arena.mob.getDamageSources().magic(), 1), "Recibe daño sin retroceso");
			context.assertTrue(arena.mob.getNavigation().isIdle(), "La reacción detiene inmediatamente la ruta");
		});
		for (int tick = 1; tick <= 8; tick++) {
			context.runAtTick(START + tick, () -> {
				context.assertEquals(3, action(arena.mob), "Hurt conserva su duración de nueve ticks");
				context.assertTrue(arena.mob.getNavigation().isIdle(), "La persecución espera al final de hurt");
			});
		}
		context.runAtTick(START + 13, () -> {
			context.assertTrue(!arena.mob.getNavigation().isIdle(), "La persecución se reanuda tras la reacción");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 110, batchId = "erranteHurtWander")
	public void hurtAlsoPausesWanderingWithoutTarget(TestContext context) {
		var arena = new Arena(context);
		context.runAtTick(START, () -> {
			arena.mob.setAiDisabled(false);
			arena.mob.damage(arena.mob.getDamageSources().magic(), 1);
		});
		context.runAtTick(START + 5, () -> {
			context.assertEquals(3, action(arena.mob), "Reacción activa sin objetivo");
			context.assertTrue(arena.mob.getNavigation().isIdle(), "Paseo bloqueado durante hurt");
			arena.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 110, batchId = "erranteUndead")
	public void harbingerBlocksErranteAndKeepsVanillaTagMembers(TestContext context) {
		var arena = new Arena(context);
		var player = arena.player(3.5, 5);
		context.runAtTick(START, () -> {
			context.assertTrue(NewMobs.ERRANTE.isIn(EntityTypeTags.UNDEAD), "Errante clasificado como no muerto");
			context.assertTrue(EntityType.ZOMBIE.isIn(EntityTypeTags.UNDEAD), "Conserva las entradas vanilla");
			context.assertTrue(!EntityType.COW.isIn(EntityTypeTags.UNDEAD), "La clasificación sigue siendo selectiva");
			context.assertTrue(NewMobs.ERRANTE.isIn(EntityTypeTags.SENSITIVE_TO_SMITE), "Castigo reconoce al no muerto");
			context.assertTrue(NewMobs.ERRANTE.isIn(EntityTypeTags.IGNORES_POISON_AND_REGEN), "Hereda la regla de veneno y regeneración");
			context.assertTrue(arena.mob.hasInvertedHealingAndHarm(), "Hereda la inversión de curación y daño instantáneos");
			context.assertTrue(!AbilityState.shouldBlockTarget(arena.mob, player), "Un jugador ordinario es elegible");
			var roles = RoleManager.get(player);
			roles.setPrimary(Role.NECROMANCER);
			roles.setRank(UniqueManager.legendNode(Role.NECROMANCER), 1);
			context.assertTrue(AbilityState.shouldBlockTarget(arena.mob, player), "Heraldo de la Muerte bloquea el objetivo");
			arena.mob.setTarget(player);
			context.assertTrue(arena.mob.getTarget() == null, "El filtro real impide fijar al portador");
			arena.complete();
		});
	}

	private static void begin(ErranteEntity mob, ServerPlayerEntity target) {
		mob.setAiDisabled(false);
		mob.setTarget(target);
		setField(mob, "alertedTarget", target.getUuid());
		invoke(mob, "beginRend", new Class<?>[] {ServerWorld.class, float.class}, mob.getWorld(), 0f);
	}

	private static Object invoke(ErranteEntity mob, String name, Class<?>[] types, Object... args) {
		try {
			var method = ErranteEntity.class.getDeclaredMethod(name, types);
			method.setAccessible(true);
			return method.invoke(mob, args);
		} catch (ReflectiveOperationException error) {
			throw new IllegalStateException("Ejecución del escenario " + name, error);
		}
	}

	private static Object field(ErranteEntity mob, String name) {
		try {
			var field = ErranteEntity.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(mob);
		} catch (ReflectiveOperationException error) {
			throw new IllegalStateException("Lectura del escenario " + name, error);
		}
	}

	private static void setField(ErranteEntity mob, String name, Object value) {
		try {
			var field = ErranteEntity.class.getDeclaredField(name);
			field.setAccessible(true);
			field.set(mob, value);
		} catch (ReflectiveOperationException error) {
			throw new IllegalStateException("Preparación del escenario " + name, error);
		}
	}

	@SuppressWarnings("unchecked")
	private static int action(ErranteEntity mob) {
		return (int) (long) mob.getDataTracker().get((TrackedData<Long>) field(mob, "ACTION")) & 7;
	}

	private static final class Arena {
		private final TestContext context;
		private final ErranteEntity mob;
		private final List<ServerPlayerEntity> players = new ArrayList<>();

		Arena(TestContext context) {
			this.context = context;
			Aptitudes.config().threatEnabled = false;
			Aptitudes.config().eliteChance = 0;
			context.getWorld().getServer().setDifficulty(Difficulty.NORMAL, true);
			for (int x = -1; x <= 8; x++) for (int z = -1; z <= 8; z++) {
				context.setBlockState(x, 0, z, Blocks.STONE);
			}
			mob = context.spawnEntity(NewMobs.ERRANTE, new Vec3d(3.5, 1, 3.5));
			mob.setPersistent();
			mob.setAiDisabled(true);
		}

		ServerPlayerEntity player(double x, double z) {
			var player = LabPlayer.connect(context.getWorld());
			Vec3d pos = context.getAbsolute(new Vec3d(x, 1, z));
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
			mob.discard();
			context.complete();
		}
	}
}
