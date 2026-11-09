package dev.aptitudes.mob;

import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Osario Quebrado (v65): enemigo común de la Niebla de almas. Cuerpo de hueso y tierra, con un brazo que arrastra. Se
 * mueve y ataca cuerpo a cuerpo; la animación de caminar y de reposo es de GeckoLib. El barrido ({@code heavy_swipe})
 * queda definido en el modelo, pendiente de dispararse desde el ataque.
 */
public class OsarioEntity extends HostileEntity implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
	protected void initGoals() {
		goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
		goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 12f));
		goalSelector.add(8, new LookAroundGoal(this));
		targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	public boolean tryAttack(net.minecraft.entity.Entity target) {
		// El barrido del brazo largo se dispara al atacar (el golpe llega al final de la animación).
		if (!getWorld().isClient()) triggerAnim("action", "heavy_swipe");
		return super.tryAttack(target);
	}

	/** Ectoplasma y polvo de tierra alrededor de los pies (v65): se ve que no descansa. */
	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!(getWorld() instanceof ServerWorld world) || random.nextInt(4) != 0) return;
		world.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 0.2, getZ(), 1, 0.2, 0.3, 0.2, 0.01);
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
				getX(), getY() + 0.1, getZ(), 2, 0.3, 0.05, 0.3, 0);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "move", 5,
				state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
		AnimationController<OsarioEntity> actions = new AnimationController<>(this, "action", 2,
				state -> software.bernie.geckolib.animation.PlayState.STOP);
		actions.triggerableAnim("heavy_swipe", RawAnimation.begin().thenPlay("heavy_swipe"));
		controllers.add(actions);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
