package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.AttributeApplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

import java.util.List;

/**
 * El Coloso Sin Rostro (v63, C4): guerrero gigante con espadón. Tajo en arco, golpe descendente con onda y, desde
 * la fase 2 (armadura agrietada: +30 % de velocidad y +25 % de daño recibido), una embestida. Diseño completo en
 * {@code docs/V63_JEFES.md}.
 */
public class FacelessColossusEntity extends AnimatedBossEntity {
	private static final TrackedData<Boolean> CRACKED_ARMOR = DataTracker.registerData(
			FacelessColossusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	static final Move SWEEP = new Move("sweep", 1, 0, 4.5, 18, 30, 3,
			AttackTags.of(AttackTags.Tag.PARRYABLE));
	static final Move SLAM = new Move("slam", 1, 0, 5.5, 28, 70, 2,
			AttackTags.of(AttackTags.Tag.HEAVY, AttackTags.Tag.GROUND));
	static final Move CHARGE = new Move("charge", 2, 5, 18, 16, 90, 2,
			AttackTags.of(AttackTags.Tag.HEAVY));
	private static final List<Move> MOVES = List.of(SWEEP, SLAM, CHARGE);
	private static final double SWEEP_ARC = 140;

	private float attackYaw;
	@Nullable private LivingEntity chargeTarget;
	private Vec3d chargePosition = Vec3d.ZERO;
	private long chargeUntil;

	public FacelessColossusEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world, BossBar.Color.RED);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(CRACKED_ARMOR, false);
	}

	public boolean hasCrackedArmor() {
		return getDataTracker().get(CRACKED_ARMOR);
	}

	@Override
	public void restorePhase(int restored) {
		super.restorePhase(restored);
		syncArmorPhase();
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		syncArmorPhase();
	}

	private void syncArmorPhase() {
		if (getWorld().isClient) return;
		boolean cracked = phase() >= 2;
		getDataTracker().set(CRACKED_ARMOR, cracked);
		AttributeApplier.set(getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED),
				Aptitudes.id("colossus_cracked"), cracked ? 0.3 : 0,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 320)
				.add(EntityAttributes.GENERIC_ARMOR, 12)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.9)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.5);
	}

	@Override
	protected List<Move> moves() {
		return MOVES;
	}

	@Override
	protected double crackedBonus() {
		return 0.25;
	}

	@Override
	protected void onPhase(ServerWorld world, int newPhase) {
		finishCharge();
		syncArmorPhase();
		world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getBodyY(0.6), getZ(), 3, 0.6, 0.6, 0.6, 0);
	}

	@Override
	protected int phaseTransitionTicks() {
		return FacelessColossusCombat.PHASE_CLIP_TICKS + FacelessColossusCombat.TRANSITION_TICKS;
	}

	@Override
	protected int attackRecoveryTicks(Move move) {
		return FacelessColossusCombat.recoveryTicks(move);
	}

	@Override
	protected boolean validAttackTarget(LivingEntity target) {
		return target != null && target.getWorld() == getWorld() && hittable(target)
				&& canTarget(target) && !AbilityState.shouldBlockTarget(this, target);
	}

	@Override
	protected boolean canStartAttack(LivingEntity target) {
		return isOnGround() && canSee(target);
	}

	@Override
	protected int chaseRepathTicks() {
		return 8;
	}

	@Override
	protected void aimAttack(LivingEntity target, boolean windingUp) {
		if (windingUp) {
			faceAttack();
		} else {
			super.aimAttack(target, false);
		}
	}

	@Override
	protected void onAttackStarted(Move move, LivingEntity target) {
		double dx = target.getX() - getX();
		double dz = target.getZ() - getZ();
		attackYaw = dx == 0 && dz == 0 ? getYaw() : (float) Math.toDegrees(MathHelper.atan2(-dx, dz));
		faceAttack();
	}

	private void faceAttack() {
		setYaw(attackYaw);
		setBodyYaw(attackYaw);
		setHeadYaw(attackYaw);
	}

	@Override
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
		if (ticksLeft % 4 != 0) return;
		if (move == SLAM) {
			for (int i = 0; i < 16; i++) {
				double a = i * Math.PI / 8;
				world.spawnParticles(ParticleTypes.CRIT, getX() + Math.cos(a) * move.maxRange(), getY() + 0.1,
						getZ() + Math.sin(a) * move.maxRange(), 1, 0, 0, 0, 0);
			}
		} else if (move == SWEEP) {
			world.spawnParticles(ParticleTypes.SWEEP_ATTACK, getX(), getBodyY(0.5), getZ(), 1, 0.8, 0.1, 0.8, 0);
		} else if (move == CHARGE) {
			Vec3d forward = Vec3d.fromPolar(0, attackYaw);
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, getX() + forward.x * 1.5, getY() + 0.2,
					getZ() + forward.z * 1.5, 4, 0.3, 0.1, 0.3, 0.01);
		}
	}

	@Override
	protected void strike(ServerWorld world, Move move, LivingEntity target) {
		float base = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (move == SWEEP) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2f, 0.6f);
			for (LivingEntity e : meleeTargets(world, move.maxRange(), FacelessColossusCombat.SWEEP_LOW,
					FacelessColossusCombat.SWEEP_HIGH)) {
				if (BossMoveRules.inArc(e.getX() - getX(), e.getZ() - getZ(), attackYaw, SWEEP_ARC, move.maxRange())
						&& unobstructedContact(world, e, FacelessColossusCombat.SWEEP_LOW, FacelessColossusCombat.SWEEP_HIGH)
						&& !parried(e, move)) {
					hit(e, base, 0.8);
				}
				if (isPostureStunned(world.getTime())) break;
			}
		} else if (move == SLAM) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 0.8f, 0.7f);
			world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, 2, 0.1, 2, 0);
			for (LivingEntity e : meleeTargets(world, move.maxRange(), FacelessColossusCombat.SLAM_LOW,
					FacelessColossusCombat.SLAM_HIGH)) {
				if (e.squaredDistanceTo(this) <= move.maxRange() * move.maxRange()
						&& unobstructedContact(world, e, FacelessColossusCombat.SLAM_LOW, FacelessColossusCombat.SLAM_HIGH)
						&& hit(e, base * 1.2f, 0.4)) {
					e.addVelocity(0, 0.55, 0);
					e.velocityModified = true;
				}
			}
		} else if (move == CHARGE) {
			chargeTarget = target;
			chargePosition = getPos();
			chargeUntil = world.getTime() + FacelessColossusCombat.CHARGE_TRAVEL_TICKS;
			setVelocity(Vec3d.fromPolar(0, attackYaw).multiply(1.6).add(0, 0.15, 0));
			velocityModified = true;
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 1f, 0.8f);
		}
	}

	private List<LivingEntity> meleeTargets(ServerWorld world, double radius, double low, double high) {
		Box area = new Box(getX() - radius, getY() + low, getZ() - radius,
				getX() + radius, getY() + high, getZ() + radius);
		return world.getEntitiesByClass(LivingEntity.class, area, this::hittable);
	}

	private boolean unobstructedContact(ServerWorld world, LivingEntity target, double low, double high) {
		return unobstructedContact(world, target, getPos(), low, high);
	}

	private boolean unobstructedContact(ServerWorld world, LivingEntity target, Vec3d origin, double low, double high) {
		Box box = target.getBoundingBox();
		double contact = FacelessColossusCombat.contactHeight(box.minY - origin.y, box.maxY - origin.y, low, high);
		if (Double.isNaN(contact)) return false;
		double y = origin.y + contact;
		Vec3d start = new Vec3d(origin.x, y, origin.z);
		Vec3d end = new Vec3d(target.getX(), y, target.getZ());
		return world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
	}

	@Override
	public void tickMovement() {
		if (getWorld() instanceof ServerWorld world && isPostureStunned(world.getTime())) finishCharge();
		super.tickMovement();
		if (chargeTarget == null || !(getWorld() instanceof ServerWorld world)) return;
		LivingEntity target = chargeTarget;
		if (!isAlive() || isPostureStunned(world.getTime()) || !validAttackTarget(target)) {
			finishCharge();
			return;
		}
		faceAttack();
		// La caja ampliada convierte el barrido de dos cajas en un segmento contra una sola caja.
		Box bounds = getBoundingBox();
		double halfWidth = bounds.getLengthX() * 0.5;
		double halfHeight = bounds.getLengthY() * 0.5;
		Vec3d start = chargePosition.add(0, halfHeight, 0);
		Vec3d end = getPos().add(0, halfHeight, 0);
		Box contact = target.getBoundingBox().expand(halfWidth, halfHeight, halfWidth);
		Vec3d impact = contact.contains(start) ? start
				: contact.raycast(start, end).orElse(contact.contains(end) ? end : null);
		if (impact != null && unobstructedContact(world, target, impact.subtract(0, halfHeight, 0), 0, bounds.getLengthY())) {
			finishCharge();
			hit(target, (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 0.9f, 1.4);
		} else if (horizontalCollision || world.getTime() >= chargeUntil) {
			finishCharge();
		} else {
			chargePosition = getPos();
		}
	}

	private void finishCharge() {
		if (chargeTarget == null) return;
		chargeTarget = null;
		setVelocity(0, getVelocity().y, 0);
		velocityModified = true;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		// Un solo controlador conserva la última pose para interpolar entre acciones y locomoción.
		var animation = new AnimationController<>(this, ACTION_CONTROLLER, FacelessColossusCombat.TRANSITION_TICKS,
				state -> state.setAndContinue(state.isMoving() ? WALK : IDLE));
		registerActionAnimations(animation);
		controllers.add(animation);
	}

	@Override
	protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
		super.dropEquipment(world, source, causedByPlayer);
		dropStack(new ItemStack(ModItems.SOUL_SHARD, 8 + getRandom().nextInt(5)));
		dropStack(new ItemStack(ModItems.CHAMPION_HEART));
		dropStack(new ItemStack(ModItems.ELITE_ESSENCE, 3));
	}
}
