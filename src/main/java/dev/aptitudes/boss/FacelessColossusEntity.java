package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.AttributeApplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static dev.aptitudes.boss.FacelessColossusMoves.CHARGE;
import static dev.aptitudes.boss.FacelessColossusMoves.SLAM;
import static dev.aptitudes.boss.FacelessColossusMoves.SWEEP;

/**
 * El Coloso Sin Rostro (v63, C4): guerrero gigante con espadón. Tajo en arco, golpe descendente con onda y, desde
 * la fase 2 (armadura agrietada: +30 % de velocidad y +25 % de daño recibido), una embestida. Diseño completo en
 * {@code docs/V63_JEFES.md}.
 */
public class FacelessColossusEntity extends AnimatedBossEntity {
	private static final TrackedData<Boolean> CRACKED_ARMOR = DataTracker.registerData(
			FacelessColossusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final Identifier CRACKED_SPEED = Aptitudes.id("colossus_cracked");

	private static final double SWEEP_ARC = 140;
	private static final double CHARGE_STEP = 1.6;
	private boolean charging;

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
		boolean cracked = phase() >= 2;
		getDataTracker().set(CRACKED_ARMOR, cracked);
		AttributeApplier.set(getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED),
				CRACKED_SPEED, cracked ? 0.3 : 0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
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
		return FacelessColossusMoves.ALL;
	}

	@Override
	protected boolean commitsToAttack() {
		return true;
	}

	@Override
	protected int recoveryTicks(Move move) {
		return FacelessColossusMoves.recoveryTicks(move);
	}

	@Override
	protected int phaseAnimationTicks() {
		return FacelessColossusMoves.PHASE_ANIMATION_TICKS;
	}

	@Override
	protected double crackedBonus() {
		return 0.25;
	}

	@Override
	protected void onPhase(ServerWorld world, int newPhase) {
		syncArmorPhase();
		world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getBodyY(0.6), getZ(), 3, 0.6, 0.6, 0.6, 0);
	}

	@Override
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
		if (ticksLeft % 4 != 0) return;
		if (move == SLAM) {
			for (int i = 0; i < 16; i++) {
				double a = i * Math.PI / 8;
				world.spawnParticles(ParticleTypes.CRIT, getX() + Math.cos(a) * SLAM.maxRange(), getY() + 0.1,
						getZ() + Math.sin(a) * SLAM.maxRange(), 1, 0, 0, 0, 0);
			}
		} else if (move == SWEEP) {
			for (int degrees = -70; degrees <= 70; degrees += 14) {
				Vec3d point = Vec3d.fromPolar(0, getYaw() + degrees).multiply(SWEEP.maxRange()).add(getPos());
				world.spawnParticles(ParticleTypes.CRIT, point.x, point.y + 0.15, point.z, 1, 0, 0, 0, 0);
			}
		} else if (move == CHARGE) {
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.2, getZ(), 4, 0.5, 0.1, 0.5, 0.01);
			Vec3d direction = Vec3d.fromPolar(0, getYaw());
			for (int distance = 2; distance <= 6; distance += 2) {
				Vec3d point = getPos().add(direction.multiply(distance));
				world.spawnParticles(ParticleTypes.SMOKE, point.x, point.y + 0.2, point.z, 1, 0, 0, 0, 0);
			}
		}
	}

	@Override
	protected void strike(ServerWorld world, Move move, LivingEntity target) {
		float base = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (move == SWEEP) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2f, 0.6f);
			var area = getBoundingBox().expand(SWEEP.maxRange(), 0, SWEEP.maxRange());
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, area, this::hittable)) {
				if (BossMoveRules.inArc(e.getX() - getX(), e.getZ() - getZ(), getYaw(), SWEEP_ARC, SWEEP.maxRange())
						&& canSee(e) && !parried(e, move)) {
					hit(e, base, 0.8);
				}
			}
		} else if (move == SLAM) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 0.8f, 0.7f);
			world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, 2, 0.1, 2, 0);
			for (LivingEntity e : victims(world, SLAM.maxRange())) {
				if (e.squaredDistanceTo(this) <= SLAM.maxRange() * SLAM.maxRange() && canSee(e)
						&& hit(e, base * 1.2f, 0.4)) {
					e.addVelocity(0, 0.55, 0);
					e.velocityModified = true;
				}
			}
		} else if (move == CHARGE) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 1f, 0.8f);
			charging = true;
			advanceCharge(target);
		}
	}

	@Override
	protected void followThrough(ServerWorld world, Move move, LivingEntity target, int elapsed) {
		if (move == CHARGE && elapsed < FacelessColossusMoves.CHARGE_TRAVEL_TICKS && charging) advanceCharge(target);
	}

	private void advanceCharge(LivingEntity target) {
		Vec3d start = getBoundingBox().getCenter();
		Vec3d delta = Vec3d.fromPolar(0, getYaw()).multiply(CHARGE_STEP);
		setVelocity(0, getVelocity().y, 0);
		move(MovementType.SELF, delta);
		Vec3d end = getBoundingBox().getCenter();
		// La caja expandida convierte el barrido de la caja del Coloso en un segmento.
		var contact = target.getBoundingBox().expand(getWidth() / 2.0, getHeight() / 2.0, getWidth() / 2.0);
		if ((contact.contains(start) || contact.contains(end) || contact.raycast(start, end).isPresent()) && canSee(target)) {
			hit(target, (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 0.9f, 1.4);
			charging = false;
		} else if (horizontalCollision) {
			charging = false;
		}
	}

	@Override
	protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
		super.dropEquipment(world, source, causedByPlayer);
		dropStack(new ItemStack(ModItems.SOUL_SHARD, 8 + getRandom().nextInt(5)));
		dropStack(new ItemStack(ModItems.CHAMPION_HEART));
		dropStack(new ItemStack(ModItems.ELITE_ESSENCE, 3));
	}
}
