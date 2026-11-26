package dev.aptitudes.mob;

import dev.aptitudes.event.WorldEventManager;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumMap;
import java.util.Map;

import static dev.aptitudes.mob.OsarioStrikeRules.Action;

/** Osario Quebrado. El servidor resuelve los impactos; el cliente anima a partir del sello de la acción. */
public class OsarioEntity extends HostileEntity implements GeoEntity {
	private static final String CLIP_PREFIX = "animation.osario_quebrado.";
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(CLIP_PREFIX + "idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop(CLIP_PREFIX + "walk");
	private static final RawAnimation CHASE = RawAnimation.begin().thenLoop(CLIP_PREFIX + "chase");
	private static final TrackedData<Long> ACTION = DataTracker.registerData(OsarioEntity.class, TrackedDataHandlerRegistry.LONG);
	private static final Map<Action, RawAnimation> ACTION_CLIPS = actionClips();

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private OsarioStrikeGoal strikeGoal;

	public OsarioEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 24)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ACTION, 0L);
	}

	@Override
	protected void initGoals() {
		strikeGoal = new OsarioStrikeGoal(this);
		goalSelector.add(2, strikeGoal);
		goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 12f));
		goalSelector.add(8, new LookAroundGoal(this));
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	public boolean tryAttack(Entity target) {
		return false;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (getWorld() instanceof ServerWorld world && isAlive()) strikeGoal.serverTick(world);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!(getWorld() instanceof ServerWorld world) || !isAlive()) return;
		if (isAiDisabled() && actionStamp() != 0) {
			releaseStrike(world);
			setAttacking(false);
			setAction(Action.NONE, world.getTime());
		}
		strikeGoal.afterMovement(world);
	}

	long actionStamp() {
		return dataTracker.get(ACTION);
	}

	void setAction(Action action, long now) {
		dataTracker.set(ACTION, action.stamp(now));
	}

	void releaseStrike(ServerWorld world) {
		if (strikeGoal != null) strikeGoal.reset(world);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		boolean hit = super.damage(source, amount);
		if (hit && isAlive() && getWorld() instanceof ServerWorld world && !strikeGoal.busy()
				&& Action.from(actionStamp()) != Action.HURT && !isAiDisabled()) {
			getNavigation().stop();
			setAction(Action.HURT, world.getTime());
		}
		return hit;
	}

	@Override
	public void onDeath(DamageSource source) {
		if (getWorld() instanceof ServerWorld world && Action.from(actionStamp()) != Action.DEATH) {
			releaseStrike(world);
			setAttacking(false);
			getNavigation().stop();
			setAction(Action.DEATH, world.getTime());
		}
		super.onDeath(source);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.OSARIO_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.OSARIO_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.OSARIO_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		if (!getWorld().isClient()) playSound(ModSounds.OSARIO_STEP, .36f, .91f + random.nextFloat() * .16f);
	}

	private static Map<Action, RawAnimation> actionClips() {
		var animations = new EnumMap<Action, RawAnimation>(Action.class);
		for (Action action : Action.values()) {
			if (action.clip == null) continue;
			String name = CLIP_PREFIX + action.clip;
			animations.put(action, action == Action.DEATH ? RawAnimation.begin().thenPlayAndHold(name) : RawAnimation.begin().thenPlay(name));
		}
		return animations;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new MoveController(), new ActionController());
	}

	private final class MoveController extends AnimationController<OsarioEntity> {
		private boolean chasing;
		private long switchedAt;

		MoveController() {
			super(OsarioEntity.this, "move", 5, OsarioEntity.this::animateMove);
		}
	}

	private PlayState animateMove(AnimationState<OsarioEntity> state) {
		if (isDead() || actionStamp() != 0) return PlayState.STOP;
		var controller = (MoveController) state.getController();
		boolean aggro = isAttacking();
		long now = getWorld().getTime();
		if (aggro == controller.chasing) controller.switchedAt = now;
		else if (now - controller.switchedAt >= 10) controller.chasing = aggro;
		return state.setAndContinue(!state.isMoving() ? IDLE : controller.chasing ? CHASE : WALK);
	}

	private final class ActionController extends AnimationController<OsarioEntity> {
		private long lastStamp;
		private float partialTick;

		ActionController() {
			super(OsarioEntity.this, "action", 2, OsarioEntity.this::animateAction);
		}

		@Override
		protected double adjustTick(double tick) {
			double local = super.adjustTick(tick);
			long stamp = actionStamp();
			Action action = Action.from(stamp);
			var current = getCurrentAnimation();
			if (action == Action.NONE || current == null || getAnimationState() == State.TRANSITIONING
					|| !current.animation().name().equals(CLIP_PREFIX + action.clip)) return local;
			double elapsed = getWorld().getTime() - (stamp >>> Action.BITS) + partialTick;
			return Math.clamp(elapsed, 0, current.animation().length() - 1e-5);
		}
	}

	private PlayState animateAction(AnimationState<OsarioEntity> state) {
		var controller = (ActionController) state.getController();
		controller.partialTick = state.getPartialTick();
		long stamp = actionStamp();
		Action action = isDead() ? Action.DEATH : Action.from(stamp);
		if (stamp != controller.lastStamp) {
			controller.lastStamp = stamp;
			if (action != Action.NONE) controller.forceAnimationReset();
		}
		return action == Action.NONE ? PlayState.STOP : state.setAndContinue(ACTION_CLIPS.get(action));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	/** El Núcleo del Osario se obtiene de criaturas nacidas durante el evento. */
	@Override
	protected void dropLoot(DamageSource source, boolean causedByPlayer) {
		if (getCommandTags().contains(WorldEventManager.TAG)) super.dropLoot(source, causedByPlayer);
	}
}
