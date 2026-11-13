package dev.aptitudes.mob;

import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.event.WorldEventManager;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.UUID;

/** Errante Velado de la Niebla de almas. El Rasgado se resuelve en un solo tick del servidor. */
public class ErranteEntity extends HostileEntity implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
	private static final TrackedData<Long> ACTION = DataTracker.registerData(ErranteEntity.class, TrackedDataHandlerRegistry.LONG);
	private static final DustParticleEffect VEIL_DUST = new DustParticleEffect(new Vector3f(0.396f, 0.839f, 0.816f), 0.75f);
	private static final String FOG_SESSION = "AptitudesFogSession";
	private static final String RETIRE_AT = "AptitudesFogRetireAt";
	private static final int ACTION_BITS = 3;
	private static final int ACTION_MASK = (1 << ACTION_BITS) - 1;

	private enum Action {
		NONE(null, 0), ALERT("alert", 13), REND("rasgado", SpectralRendRules.END_TICK), HURT("hurt", 9), DEATH("death", 19);

		final RawAnimation animation;
		final String name;
		final int ticks;

		Action(String name, int ticks) {
			this.name = name;
			this.ticks = ticks;
			this.animation = name == null ? null : RawAnimation.begin().thenPlay(name);
		}
	}

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private UUID fogSession;
	private long retireAt = -1;
	private UUID alertedTarget;
	private SpectralRendRules.Execution rend;
	private float attackYaw;

	public ErranteEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 26)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ACTION, 0L);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(2, new RendGoal());
		goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 12f));
		goalSelector.add(8, new LookAroundGoal(this));
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	public boolean tryAttack(Entity target) {
		return false;
	}

	private Action action(long stamp) {
		return Action.values()[(int) (stamp & ACTION_MASK)];
	}

	private void setAction(Action action, long now) {
		dataTracker.set(ACTION, action == Action.NONE ? 0L : (now << ACTION_BITS) | action.ordinal());
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (!(getWorld() instanceof ServerWorld world) || !isAlive() || isRetiring()) return;
		long now = world.getTime();
		LivingEntity target = getTarget();
		if (rend != null) {
			tickRend(world, now, target);
			return;
		}

		long stamp = dataTracker.get(ACTION);
		if (stamp != 0 && now - (stamp >>> ACTION_BITS) >= action(stamp).ticks) setAction(Action.NONE, now);
		if (!validTarget(target)) {
			alertedTarget = null;
		} else if (action(dataTracker.get(ACTION)) == Action.NONE && !target.getUuid().equals(alertedTarget)) {
			alertedTarget = target.getUuid();
			getNavigation().stop();
			setAction(Action.ALERT, now);
			playSpectralSound(ModSounds.ERRANTE_ALERT, 0.58f);
		}
	}

	private boolean validTarget(LivingEntity target) {
		if (target == null || target == this || !target.isAlive() || !target.isAttackable()
				|| target.getWorld() != getWorld() || isTeammate(target) || !canTarget(target)
				|| AbilityState.shouldBlockTarget(this, target)) return false;
		if (target instanceof PlayerEntity player) return !player.isCreative() && !player.isSpectator();
		return target instanceof MobEntity mob && AbilityState.isSummon(mob) && mob.getTarget() == this;
	}

	private void beginRend(ServerWorld world, float yaw) {
		long now = world.getTime();
		rend = new SpectralRendRules.Execution(now);
		attackYaw = yaw;
		getNavigation().stop();
		faceRend();
		setAction(Action.REND, now);
		playSpectralSound(ModSounds.ERRANTE_PREPARE, 0.55f);
	}

	private void faceRend() {
		setYaw(attackYaw);
		setBodyYaw(attackYaw);
		setHeadYaw(attackYaw);
	}

	private void tickRend(ServerWorld world, long now, LivingEntity target) {
		if (rend.finished(now) || (rend.preparing(now) && !validTarget(target))) {
			cancelRend();
			return;
		}
		faceRend();
		if (rend.warn(now)) {
			Vec3d wrist = new Vec3d(10.632, 15.901, -4.275).multiply(getScale() / 16.0)
					.rotateY((float) Math.toRadians(-attackYaw)).add(getPos());
			world.spawnParticles(VEIL_DUST, wrist.x, wrist.y, wrist.z, 5, 0.09, 0.09, 0.09, 0);
		}
		if (rend.strike(now)) {
			playSpectralSound(ModSounds.ERRANTE_REND, 0.75f);
			sweep(world);
		}
	}

	private void cancelRend() {
		if (rend == null) return;
		rend.cancel();
		rend = null;
		setAction(Action.NONE, 0);
	}

	private SpectralRendRules.Point contact(LivingEntity target, float yaw) {
		Box box = target.getBoundingBox().offset(-getX(), -getY(), -getZ());
		Vec3d forward = Vec3d.fromPolar(0, yaw);
		return SpectralRendRules.contact(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
				forward.x, forward.z, getScale());
	}

	private boolean unobstructed(ServerWorld world, LivingEntity target, SpectralRendRules.Point contact) {
		Vec3d end = getPos().add(contact.x(), contact.y(), contact.z());
		// El punto está en el borde de la caja. Avanza apenas hacia su centro para evitar empates con bloques.
		end = end.lerp(new Vec3d(target.getX(), end.y, target.getZ()), 0.001);
		Vec3d start = getPos().add(0, contact.y(), 0);
		return world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
	}

	private void sweep(ServerWorld world) {
		double scale = getScale(), reach = SpectralRendRules.REACH * scale;
		Box area = new Box(getX() - reach, getY() + SpectralRendRules.MIN_HEIGHT * scale, getZ() - reach,
				getX() + reach, getY() + SpectralRendRules.MAX_HEIGHT * scale, getZ() + reach).expand(1e-6);
		SpectralRendRules.Execution execution = rend;
		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area, this::validTarget)) {
			if (rend != execution || !isAlive() || isRetiring()) break;
			SpectralRendRules.Point point = contact(target, attackYaw);
			if (point == null || !unobstructed(world, target, point) || !execution.firstAttempt(target.getUuid())) continue;
			if (super.tryAttack(target) && target.isAlive()) {
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, SpectralRendRules.WEAKNESS_TICKS, 0), this);
			}
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (isRetiring()) {
			if (source.isOf(DamageTypes.GENERIC_KILL) || source.isOf(DamageTypes.OUT_OF_WORLD)) discard();
			return false;
		}
		boolean hit = super.damage(source, amount);
		if (hit && isAlive() && getWorld() instanceof ServerWorld world && !isRetiring() && rend == null) {
			Action current = action(dataTracker.get(ACTION));
			if (current != Action.HURT) setAction(Action.HURT, world.getTime());
		}
		return hit;
	}

	@Override
	public void onDeath(DamageSource source) {
		if (getWorld() instanceof ServerWorld world && !isRemoved()) {
			cancelRend();
			setAction(Action.DEATH, world.getTime());
		}
		super.onDeath(source);
	}

	@Override
	protected void dropLoot(DamageSource source, boolean causedByPlayer) {
		if (getWorld() instanceof ServerWorld world && causedByPlayer && !source.isOf(DamageTypes.GENERIC_KILL)
				&& !source.isOf(DamageTypes.OUT_OF_WORLD) && WorldEventManager.canDropVeil(this, world)) {
			super.dropLoot(source, true);
		}
	}

	public UUID fogSession() {
		return fogSession;
	}

	public void markFogOrigin(UUID session) {
		fogSession = session;
	}

	public boolean isRetiring() {
		return retireAt >= 0;
	}

	public long retireAt() {
		return retireAt;
	}

	public void beginFogRetirement(long deadline) {
		if (isRetiring()) return;
		retireAt = deadline;
		cancelRend();
		setAction(Action.NONE, 0);
		setTarget(null);
		getNavigation().stop();
		setAiDisabled(true);
	}

	public void finishFogRetirement(ServerWorld world) {
		if (isAlive() && !isRemoved()) {
			playSpectralSound(ModSounds.ERRANTE_DESPAWN, 0.53f);
			world.spawnParticles(ParticleTypes.SOUL, getX(), getBodyY(0.55), getZ(), 9, 0.24, 0.35, 0.24, 0.025);
			discard();
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (fogSession != null) nbt.putUuid(FOG_SESSION, fogSession);
		if (isRetiring()) nbt.putLong(RETIRE_AT, retireAt);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		fogSession = nbt.containsUuid(FOG_SESSION) ? nbt.getUuid(FOG_SESSION) : null;
		retireAt = nbt.contains(RETIRE_AT) ? nbt.getLong(RETIRE_AT) : -1;
		if (isRetiring()) setAiDisabled(true);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return isRetiring() ? null : ModSounds.ERRANTE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.ERRANTE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return isRetiring() ? null : ModSounds.ERRANTE_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.5f;
	}

	private void playSpectralSound(SoundEvent sound, float volume) {
		if (sound != null && getWorld() instanceof ServerWorld world) {
			world.playSound(null, getX(), getY(), getZ(), sound, getSoundCategory(), volume, 0.94f + random.nextFloat() * 0.12f);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new ErranteController());
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	private final class ErranteController extends AnimationController<ErranteEntity> {
		private long lastAction;
		private float partialTick;

		ErranteController() {
			super(ErranteEntity.this, "errante", 5, ErranteEntity.this::animate);
		}

		@Override
		protected double adjustTick(double tick) {
			long stamp = dataTracker.get(ACTION);
			Action action = action(stamp);
			// GeckoLib consulta la transición antes de llamar al selector de animación.
			transitionLength(action == Action.NONE ? 5 : 0);
			double local = super.adjustTick(tick);
			var current = getCurrentAnimation();
			if (action == Action.NONE || getAnimationState() == State.TRANSITIONING || current == null
					|| !current.animation().name().equals(action.name)) return local;
			double elapsed = getWorld().getTime() - (stamp >>> ACTION_BITS) + partialTick;
			return Math.clamp(elapsed, 0, current.animation().length() - 1e-5);
		}
	}

	private PlayState animate(AnimationState<ErranteEntity> state) {
		ErranteController controller = (ErranteController) state.getController();
		controller.partialTick = state.getPartialTick();
		long stamp = dataTracker.get(ACTION);
		Action action = action(stamp);
		if (stamp != controller.lastAction) {
			if (action != Action.NONE) controller.forceAnimationReset();
			controller.lastAction = stamp;
		}
		if (isDead() && action != Action.DEATH) return state.setAndContinue(Action.DEATH.animation);
		return state.setAndContinue(action == Action.NONE ? (state.isMoving() ? WALK : IDLE) : action.animation);
	}

	private final class RendGoal extends Goal {
		private long repathAt;

		RendGoal() {
			setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			return !isRetiring() && (rend != null || validTarget(getTarget()));
		}

		@Override
		public boolean shouldContinue() {
			return canStart();
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void stop() {
			getNavigation().stop();
			repathAt = 0;
		}

		@Override
		public void tick() {
			if (!(getWorld() instanceof ServerWorld world) || rend != null || isRetiring()) return;
			LivingEntity target = getTarget();
			if (!validTarget(target)) return;
			getLookControl().lookAt(target, 30, 30);
			if (!target.getUuid().equals(alertedTarget) || action(dataTracker.get(ACTION)) != Action.NONE) return;
			double dx = target.getX() - getX(), dz = target.getZ() - getZ();
			float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
			SpectralRendRules.Point point = contact(target, yaw);
			if (point != null && unobstructed(world, target, point)) {
				beginRend(world, yaw);
			} else if (world.getTime() >= repathAt) {
				repathAt = world.getTime() + 8;
				getNavigation().startMovingTo(target, 1);
			}
		}
	}
}
