package dev.aptitudes.boss;

import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * El Coloso Sin Rostro (v63, C4): guerrero gigante con espadón. Tajo en arco, golpe descendente con onda y, desde
 * la fase 2 (armadura agrietada: +30 % de velocidad y +25 % de daño recibido), una embestida. Diseño completo en
 * {@code docs/V63_JEFES.md}.
 */
public class FacelessColossusEntity extends AnimatedBossEntity {
	static final Move SWEEP = new Move("sweep", 1, 0, 4.5, 18, 30, 3,
			AttackTags.of(AttackTags.Tag.PARRYABLE));
	static final Move SLAM = new Move("slam", 1, 0, 5.5, 28, 70, 2,
			AttackTags.of(AttackTags.Tag.HEAVY, AttackTags.Tag.GROUND));
	static final Move CHARGE = new Move("charge", 2, 5, 18, 16, 90, 2,
			AttackTags.of(AttackTags.Tag.HEAVY));
	private static final List<Move> MOVES = List.of(SWEEP, SLAM, CHARGE);
	private static final double SWEEP_ARC = 140;

	public FacelessColossusEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world, BossBar.Color.RED);
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
		var speed = getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed != null) {
			dev.aptitudes.logic.AttributeApplier.set(speed, dev.aptitudes.Aptitudes.id("colossus_cracked"), 0.3,
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		}
		world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getBodyY(0.6), getZ(), 3, 0.6, 0.6, 0.6, 0);
	}

	@Override
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
		if (ticksLeft % 4 != 0) return;
		if (move == SLAM) {
			for (int i = 0; i < 16; i++) {
				double a = i * Math.PI / 8;
				world.spawnParticles(ParticleTypes.CRIT, getX() + Math.cos(a) * 4, getY() + 0.1, getZ() + Math.sin(a) * 4, 1, 0, 0, 0, 0);
			}
		} else if (move == SWEEP) {
			world.spawnParticles(ParticleTypes.SWEEP_ATTACK, getX(), getBodyY(0.5), getZ(), 1, 0.8, 0.1, 0.8, 0);
		} else if (move == CHARGE) {
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.2, getZ(), 4, 0.5, 0.1, 0.5, 0.01);
		}
	}

	@Override
	protected void strike(ServerWorld world, Move move, LivingEntity target) {
		float base = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (move == SWEEP) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2f, 0.6f);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(4.5), this::hittable)) {
				if (BossMoveRules.inArc(e.getX() - getX(), e.getZ() - getZ(), getYaw(), SWEEP_ARC, 4.5)
						&& !parried(e, move)) {
					hit(e, base, 0.8);
				}
			}
		} else if (move == SLAM) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 0.8f, 0.7f);
			world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, 2, 0.1, 2, 0);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(5.5), this::hittable)) {
				if (e.squaredDistanceTo(this) <= 5.5 * 5.5) {
					hit(e, base * 1.2f, 0.4);
					e.addVelocity(0, 0.55, 0);
					e.velocityModified = true;
				}
			}
		} else if (move == CHARGE) {
			Vec3d dir = target.getPos().subtract(getPos()).multiply(1, 0, 1).normalize();
			setVelocity(dir.multiply(1.6).add(0, 0.15, 0));
			velocityModified = true;
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 1f, 0.8f);
			if (target.squaredDistanceTo(this) <= 9) hit(target, base * 0.9f, 1.4);
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
