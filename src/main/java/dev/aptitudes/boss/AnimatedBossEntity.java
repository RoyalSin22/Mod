package dev.aptitudes.boss;

import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.elite.Elites;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * v63 (C4): base de los jefes animados con GeckoLib. El servidor decide (fase por vida, ataque con
 * {@link BossMoveRules}, aviso previo y golpe); el cliente solo reproduce animaciones: un controlador de
 * movimiento (idle/walk) y otro de acciones con animaciones disparadas por nombre ({@link #triggerAnim}).
 *
 * <p>Contrato para los modelos (K5): el archivo de animaciones de cada jefe debe tener {@code idle}, {@code walk},
 * {@code phase_two} y una animación por cada id de {@link #moves()}, con la misma duración aproximada que su
 * {@code windup} + 10 ticks.</p>
 */
public abstract class AnimatedBossEntity extends HostileEntity implements GeoEntity {
	public static final String MOVE_CONTROLLER = "move";
	public static final String ACTION_CONTROLLER = "action";
	protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
	/** Postura máxima del jefe (fase 2 del plan de avance). Provisional: validar en partida. */
	protected static final double POSTURE_MAX = 100;
	/** Recuperación de postura por tick (2 por segundo). */
	protected static final double POSTURE_RECOVERY_PER_TICK = 0.1;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final ServerBossBar bossBar;
	private final Map<String, Long> readyAt = new HashMap<>();
	private int phase = 1;
	@Nullable private Move current;
	private int moveTicks;
	private PostureRules.Posture posture = PostureRules.Posture.fresh(POSTURE_MAX);

	protected AnimatedBossEntity(EntityType<? extends HostileEntity> type, World world, BossBar.Color color) {
		super(type, world);
		this.bossBar = new ServerBossBar(Text.empty(), color, BossBar.Style.NOTCHED_10);
		this.experiencePoints = 200;
		// Sin afijos aleatorios de élite: el jefe ya tiene su diseño.
		addCommandTag(Elites.CHECKED);
	}

	/** Repertorio del jefe; el orden no importa (sorteo ponderado). */
	protected abstract List<Move> moves();

	/** Ejecuta el golpe de un ataque al final de su aviso. */
	protected abstract void strike(ServerWorld world, Move move, LivingEntity target);

	/** Partículas o sonido durante el aviso (cada tick del {@code windup}). */
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
	}

	/** Al entrar en una fase nueva (2 o 3). */
	protected void onPhase(ServerWorld world, int newPhase) {
	}

	public int phase() {
		return phase;
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(2, new BossAttackGoal());
		goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 16f));
		goalSelector.add(8, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	// ----------------------------------------------------------- Servidor

	@Override
	protected void mobTick() {
		super.mobTick();
		if (!(getWorld() instanceof ServerWorld world)) return;
		int next = BossRules.phase(getHealth() / getMaxHealth());
		if (next > phase) {
			phase = next;
			triggerAnim(ACTION_CONTROLLER, "phase_two");
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1f, 0.6f + 0.1f * phase);
			for (ServerPlayerEntity p : bossBar.getPlayers()) {
				p.sendMessage(Text.translatable(next >= 3 ? "boss.aptitudes.phase_three" : "boss.aptitudes.phase_two",
						getDisplayName()).formatted(Formatting.RED), false);
			}
			onPhase(world, phase);
		}
		bossBar.setName(getDisplayName());
		bossBar.setPercent(getHealth() / getMaxHealth());
		posture = PostureRules.recover(posture, POSTURE_RECOVERY_PER_TICK, world.getTime());
		// B6: indicador visible mientras dura el aturdimiento (estrellas sobre la cabeza, cada 10 ticks).
		if (posture.stunned(world.getTime()) && world.getTime() % 10 == 0) {
			world.spawnParticles(ParticleTypes.ENCHANT, getX(), getBodyY(1.1), getZ(), 8, 0.6, 0.3, 0.6, 0.1);
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		float mult = BossMoveRules.damageTaken(phase, crackedBonus());
		if (getWorld() instanceof ServerWorld world) {
			long now = world.getTime();
			// Solo los golpes de jugadores drenan la postura; por ahora sin etiquetas por ataque (NORMAL).
			if (source.getAttacker() instanceof PlayerEntity) drainPosture(world, amount);
			mult *= (float) posture.damageMultiplier(now);
		}
		return super.damage(source, amount * mult);
	}

	/** Drena la postura con un golpe (sin etiquetas: NORMAL) y avisa si se rompe. */
	private void drainPosture(ServerWorld world, double amount) {
		PostureRules.Result r = PostureRules.drain(posture, amount, AttackTags.of(), world.getTime());
		posture = r.state();
		if (r.broke()) onPostureBroken(world);
	}

	/**
	 * Parry (B3): si el ataque es desviable y el jugador bloquea en el impacto, el golpe no llega y la postura del jefe
	 * baja {@link GuardRules#PARRY_POSTURE_DAMAGE}. Devuelve true si el golpe se desvió.
	 */
	protected boolean parried(LivingEntity e, Move move) {
		if (!(e instanceof ServerPlayerEntity player) || !(getWorld() instanceof ServerWorld world)) return false;
		if (GuardRules.resolve(move.tags(), player.isBlocking()) != GuardRules.Outcome.PARRY) return false;
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1f, 1.2f);
		drainPosture(world, GuardRules.PARRY_POSTURE_DAMAGE);
		return true;
	}

	/** Registra la derrota (tick y fase) para la reinvocación (fase C, {@link SummonState}). */
	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
		if (getWorld() instanceof ServerWorld world && world.getServer() != null) {
			String id = Registries.ENTITY_TYPE.getId(getType()).toString();
			SummonState.get(world.getServer()).recordDefeat(id, world.getTime(), phase);
		}
	}

	/** Postura vaciada: aviso sonoro y de partículas. El aturdimiento lo aplica {@link PostureRules}. */
	private void onPostureBroken(ServerWorld world) {
		triggerAnim(ACTION_CONTROLLER, "stun");
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.HOSTILE, 1.2f, 0.6f);
		world.spawnParticles(ParticleTypes.CRIT, getX(), getBodyY(0.5), getZ(), 20, 0.5, 0.5, 0.5, 0.1);
	}

	/** Jugadores y criaturas que pueden recibir un golpe del jefe (no otros jefes ni espectadores). */
	protected boolean hittable(LivingEntity e) {
		return e != this && e.isAlive() && !(e instanceof AnimatedBossEntity)
				&& !(e instanceof PlayerEntity p && (p.isCreative() || p.isSpectator()));
	}

	/** Golpe del jefe: daño con su fuente y empuje si el golpe llega. */
	protected void hit(LivingEntity e, float damage, double knockback) {
		if (e.damage(getDamageSources().mobAttack(this), damage) && knockback > 0) {
			Vec3d away = e.getPos().subtract(getPos()).multiply(1, 0, 1).normalize();
			e.takeKnockback(knockback, -away.x, -away.z);
		}
	}

	/** Entidades golpeables dentro de un radio alrededor del jefe. */
	protected List<LivingEntity> victims(World world, double radius) {
		return world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(radius), this::hittable);
	}

	/** Daño extra recibido en la fase 2+ (0 = ninguno). */
	protected double crackedBonus() {
		return 0;
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		bossBar.removePlayer(player);
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("aptitudes_phase", phase);
		// B5: se guarda la postura actual. El aturdimiento y la inmunidad no se guardan: al recargar no hay aturdimiento.
		nbt.putDouble("aptitudes_posture", posture.current());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		phase = Math.max(1, Math.min(3, nbt.getInt("aptitudes_phase")));
		if (nbt.contains("aptitudes_posture")) {
			posture = new PostureRules.Posture(nbt.getDouble("aptitudes_posture"), POSTURE_MAX, 0, 0);
		}
		if (hasCustomName()) bossBar.setName(getDisplayName());
	}

	/** Objetivo cercano: elige ataque, avisa {@code windup} ticks y golpea; mientras, se mueve hacia él. */
	private final class BossAttackGoal extends Goal {
		BossAttackGoal() {
			setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			LivingEntity t = getTarget();
			return t != null && t.isAlive();
		}

		@Override
		public boolean shouldContinue() {
			return canStart() || current != null;
		}

		@Override
		public void stop() {
			current = null;
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity target = getTarget();
			if (!(getWorld() instanceof ServerWorld world)) return;
			if (target == null || !target.isAlive()) {
				current = null;
				return;
			}
			// Aturdido por la postura: no empieza ni continúa ningún ataque.
			if (posture.stunned(world.getTime())) {
				current = null;
				getNavigation().stop();
				return;
			}
			getLookControl().lookAt(target, 30f, 30f);
			double distance = Math.sqrt(squaredDistanceTo(target));
			if (current == null) {
				long now = world.getTime();
				Move move = BossMoveRules.choose(moves(), phase, distance, readyAt, now, getRandom().nextInt(1024));
				if (move != null) {
					current = move;
					moveTicks = move.windup();
					readyAt.put(move.id(), now + move.windup() + move.cooldown());
					getNavigation().stop();
					triggerAnim(ACTION_CONTROLLER, move.id());
				} else {
					getNavigation().startMovingTo(target, 1.0);
				}
				return;
			}
			telegraph(world, current, target, moveTicks);
			if (--moveTicks <= 0) {
				strike(world, current, target);
				current = null;
			}
		}
	}

	// ------------------------------------------------------------ Cliente

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, MOVE_CONTROLLER, 5,
				state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
		AnimationController<AnimatedBossEntity> actions = new AnimationController<>(this, ACTION_CONTROLLER, 2, state -> PlayState.STOP);
		actions.triggerableAnim("phase_two", RawAnimation.begin().thenPlay("phase_two"));
		actions.triggerableAnim("stun", RawAnimation.begin().thenPlay("stun"));
		for (Move move : moves()) actions.triggerableAnim(move.id(), RawAnimation.begin().thenPlay(move.id()));
		controllers.add(actions);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
