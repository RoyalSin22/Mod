package dev.aptitudes.boss;

import dev.aptitudes.ability.AbilityState;
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
import java.util.Set;

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
	private long actionReadyAt;
	private boolean pendingPhaseAnimation;
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

	/** Los jefes que se comprometen con la dirección del aviso conservan su pose hasta acabar la recuperación. */
	protected boolean commitsToAttack() {
		return false;
	}

	protected int recoveryTicks(Move move) {
		return 0;
	}

	protected int phaseAnimationTicks() {
		return 0;
	}

	/** Primer tick posterior al impacto: elapsed = 1. */
	protected void followThrough(ServerWorld world, Move move, LivingEntity target, int elapsed) {
	}

	public int phase() {
		return phase;
	}

	/** Fase con la que vuelve un jefe reinvocado (paso 2 de la reinvocación). Nunca baja de la fase actual. */
	public void restorePhase(int restored) {
		phase = Math.max(phase, Math.max(1, Math.min(3, restored)));
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
			boolean firstArmorBreak = phase == 1;
			phase = next;
			if (commitsToAttack()) pendingPhaseAnimation |= firstArmorBreak;
			else triggerAnim(ACTION_CONTROLLER, "phase_two");
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1f, 0.6f + 0.1f * phase);
			for (ServerPlayerEntity p : bossBar.getPlayers()) {
				p.sendMessage(Text.translatable(next >= 3 ? "boss.aptitudes.phase_three" : "boss.aptitudes.phase_two",
						getDisplayName()).formatted(Formatting.RED), false);
			}
			onPhase(world, phase);
		}
		if (pendingPhaseAnimation && current == null && !posture.stunned(world.getTime())
				&& world.getTime() >= actionReadyAt) {
			pendingPhaseAnimation = false;
			actionReadyAt = world.getTime() + phaseAnimationTicks();
			getNavigation().stop();
			triggerAnim(ACTION_CONTROLLER, "phase_two");
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
			if (source.getAttacker() instanceof PlayerEntity player) {
				drainPosture(world, amount, AttackTags.playerHit(player.getAttackCooldownProgress(0.5f), source.getSource() == player));
			}
			mult *= (float) posture.damageMultiplier(now);
		}
		return super.damage(source, amount * mult);
	}

	/** Drena la postura con un golpe (sin etiquetas: NORMAL) y avisa si se rompe. */
	private void drainPosture(ServerWorld world, double amount, Set<AttackTags.Tag> tags) {
		PostureRules.Result r = PostureRules.drain(posture, amount, tags, world.getTime());
		posture = r.state();
		if (r.broke()) onPostureBroken(world);
	}

	/**
	 * Parry (B3): si el ataque es desviable y el jugador bloquea en el impacto, el golpe no llega y la postura del jefe
	 * baja {@link GuardRules#PARRY_POSTURE_DAMAGE}. Devuelve true si el golpe se desvió.
	 */
	protected boolean parried(LivingEntity e, Move move) {
		if (!(e instanceof ServerPlayerEntity player) || !(getWorld() instanceof ServerWorld world)) return false;
		int raised = GuardTracker.ticksSinceRaised(player.getUuid(), world.getServer().getTicks());
		if (GuardRules.resolve(move.tags(), raised) != GuardRules.Outcome.PARRY) return false;
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1f, 1.2f);
		drainPosture(world, GuardRules.PARRY_POSTURE_DAMAGE, AttackTags.of());
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
		if (commitsToAttack()) {
			cancelAttack();
			getNavigation().stop();
			actionReadyAt = world.getTime() + PostureRules.STUN_TICKS;
		}
		triggerAnim(ACTION_CONTROLLER, "stun");
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.HOSTILE, 1.2f, 0.6f);
		world.spawnParticles(ParticleTypes.CRIT, getX(), getBodyY(0.5), getZ(), 20, 0.5, 0.5, 0.5, 0.1);
	}

	private void cancelAttack() {
		Move interrupted = current;
		current = null;
		if (interrupted != null && commitsToAttack() && getWorld() instanceof ServerWorld) {
			stopTriggeredAnim(ACTION_CONTROLLER, interrupted.id());
		}
	}

	/** Jugadores y criaturas que pueden recibir un golpe del jefe (no otros jefes ni espectadores). */
	protected boolean hittable(LivingEntity e) {
		return e != this && e.isAlive() && !(e instanceof AnimatedBossEntity)
				&& !(e instanceof PlayerEntity p && (p.isCreative() || p.isSpectator()));
	}

	/** Golpe del jefe: daño con su fuente y empuje si el golpe llega. */
	protected boolean hit(LivingEntity e, float damage, double knockback) {
		if (!e.damage(getDamageSources().mobAttack(this), damage)) return false;
		if (knockback > 0) {
			Vec3d away = e.getPos().subtract(getPos()).multiply(1, 0, 1).normalize();
			e.takeKnockback(knockback, -away.x, -away.z);
		}
		return true;
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
		@Nullable private LivingEntity attackTarget;
		private float attackYaw;
		private long nextPathAt;

		BossAttackGoal() {
			setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			LivingEntity t = getTarget();
			return t != null && t.isAlive() && (!commitsToAttack() || validAttackTarget(t));
		}

		@Override
		public boolean shouldContinue() {
			return canStart() || current != null;
		}

		@Override
		public void stop() {
			cancelAttack();
			if (commitsToAttack()) getNavigation().stop();
			attackTarget = null;
			nextPathAt = 0;
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity target = getTarget();
			if (!(getWorld() instanceof ServerWorld world)) return;
			boolean committed = commitsToAttack();
			long now = world.getTime();
			// Aturdido por la postura: no empieza ni continúa ningún ataque.
			if (posture.stunned(now) || (committed && now < actionReadyAt)) {
				cancelAttack();
				getNavigation().stop();
				return;
			}
			if (committed && current != null) {
				tickCommittedAttack(world);
				return;
			}
			if (committed && pendingPhaseAnimation) return;
			if (target == null || !target.isAlive() || (committed && !validAttackTarget(target))) {
				cancelAttack();
				if (committed) getNavigation().stop();
				return;
			}
			getLookControl().lookAt(target, 30f, 30f);
			if (current == null) {
				Move move = !committed || getVisibilityCache().canSee(target)
						? BossMoveRules.choose(moves(), phase, Math.sqrt(squaredDistanceTo(target)), readyAt, now,
								getRandom().nextInt(1024))
						: null;
				if (move != null) {
					current = move;
					moveTicks = move.windup();
					readyAt.put(move.id(), now + move.windup() + move.cooldown());
					getNavigation().stop();
					if (committed) {
						attackTarget = target;
						attackYaw = (float) Math.toDegrees(Math.atan2(getX() - target.getX(), target.getZ() - getZ()));
						faceAttack();
						nextPathAt = 0;
					}
					triggerAnim(ACTION_CONTROLLER, move.id());
					if (committed) telegraph(world, move, target, moveTicks);
				} else if (!committed || now >= nextPathAt) {
					getNavigation().startMovingTo(target, 1.0);
					nextPathAt = now + 10;
				}
				return;
			}
			telegraph(world, current, target, moveTicks);
			if (--moveTicks <= 0) {
				strike(world, current, target);
				current = null;
			}
		}

		private boolean validAttackTarget(LivingEntity target) {
			return hittable(target) && target.getWorld() == getWorld() && !isTeammate(target) && canTarget(target)
					&& !AbilityState.shouldBlockTarget(AnimatedBossEntity.this, target);
		}

		private void faceAttack() {
			setYaw(attackYaw);
			setBodyYaw(attackYaw);
			setHeadYaw(attackYaw);
		}

		private void tickCommittedAttack(ServerWorld world) {
			Move move = current;
			LivingEntity target = attackTarget;
			if (move == null || target == null
					|| (moveTicks > 0 && (target != getTarget() || !validAttackTarget(target)))) {
				cancelAttack();
				attackTarget = null;
				return;
			}
			faceAttack();
			if (--moveTicks > 0) {
				telegraph(world, move, target, moveTicks);
				return;
			}
			int elapsed = -moveTicks;
			if (elapsed == 0) strike(world, move, target);
			else if (validAttackTarget(target)) followThrough(world, move, target, elapsed);
			if (elapsed >= recoveryTicks(move)) {
				current = null;
				attackTarget = null;
			}
		}
	}

	// ------------------------------------------------------------ Cliente

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, MOVE_CONTROLLER, 5,
				state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
		AnimationController<AnimatedBossEntity> actions = new AnimationController<>(this, ACTION_CONTROLLER,
				commitsToAttack() ? 0 : 2, state -> PlayState.STOP);
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
