package dev.aptitudes.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Esfera de energía del Autómata Antiguo (v63). Vuela recta a velocidad constante y hiere al primero que toca,
 * salvo a su dueño. Si un jugador la golpea, se devuelve: la siguiente vez que alcance al Autómata lo aturde.
 */
public class AutomatonOrbEntity extends ProjectileEntity implements FlyingItemEntity {
	private static final int LIFETIME = 200;
	private static final float DAMAGE = 6f;
	private static final float DEFLECTED_DAMAGE = 4f;
	private int age;
	private boolean deflected;

	public AutomatonOrbEntity(EntityType<? extends AutomatonOrbEntity> type, World world) {
		super(type, world);
		setNoGravity(true);
	}

	/** Esfera lanzada por el Autómata con su velocidad ya calculada. */
	static AutomatonOrbEntity launch(World world, LivingEntity owner, Vec3d position, Vec3d velocity) {
		AutomatonOrbEntity orb = new AutomatonOrbEntity(dev.aptitudes.boss.BossEntities.AUTOMATON_ORB, world);
		orb.setOwner(owner);
		orb.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);
		orb.setVelocity(velocity);
		return orb;
	}

	public boolean deflected() {
		return deflected;
	}

	/** Se dibuja como una bola de fuego: brillante y lo bastante grande para verla venir. */
	@Override
	public ItemStack getStack() {
		return new ItemStack(Items.FIRE_CHARGE);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(getWorld() instanceof ServerWorld world)) return;
		if (++age > LIFETIME) {
			discard();
			return;
		}
		world.spawnParticles(deflected ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.ELECTRIC_SPARK,
				getX(), getY(), getZ(), 1, 0.05, 0.05, 0.05, 0);
	}

	@Override
	public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
		// Un jugador que la golpea la devuelve (una sola vez).
		if (deflected || !(source.getAttacker() instanceof PlayerEntity player)) return false;
		deflected = true;
		setOwner(player);
		setVelocity(getVelocity().negate().multiply(1.3));
		age = 0;
		if (getWorld() instanceof ServerWorld world) {
			world.playSound(null, getBlockPos(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1.4f);
		}
		return true;
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		if (!(getWorld() instanceof ServerWorld world)) return;
		Entity target = hit.getEntity();
		if (target == getOwner()) return;
		if (deflected && target instanceof AutomatonEntity automaton) {
			automaton.stun();
			automaton.damage(getDamageSources().mobProjectile(this, null), DEFLECTED_DAMAGE);
		} else if (!deflected && target instanceof LivingEntity living) {
			living.damage(getDamageSources().mobProjectile(this, null), DAMAGE);
		} else {
			return;
		}
		world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 10, 0.3, 0.3, 0.3, 0.1);
		discard();
	}

	@Override
	protected void onBlockHit(BlockHitResult hit) {
		super.onBlockHit(hit);
		discard();
	}

	@Override
	protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
	}

	/** Las esferas no se guardan: al recargar el chunk desaparecen con su jefe. */
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
	}
}
