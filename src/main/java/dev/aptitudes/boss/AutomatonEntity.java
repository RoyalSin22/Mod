package dev.aptitudes.boss;

import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.boss.PieceBossRules.Stun;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/**
 * El Autómata Antiguo (v63, C4): constructo de piedra y bronce. Lanza esferas que pueden devolverse (ver
 * {@link AutomatonOrbEntity}), pisotea en un radio corto y, desde la fase 2, dispara un rayo que se corta contra los
 * bloques sólidos. Aturdido recibe el doble de daño. No se deja empujar.
 *
 * <p>Pendiente: la fase 3 (dos esferas seguidas y rayo en barrido de 60°) aún no tiene variante propia.</p>
 */
public class AutomatonEntity extends AnimatedBossEntity {
	static final Move ORB = new Move("orb", 1, 4, 20, 20, 60, 3,
			AttackTags.of(AttackTags.Tag.PROJECTILE));
	static final Move STOMP = new Move("stomp", 1, 0, 3.5, 18, 50, 2,
			AttackTags.of(AttackTags.Tag.GROUND));
	static final Move BEAM = new Move("beam", 2, 4, 22, 30, 120, 2,
			AttackTags.of(AttackTags.Tag.PROJECTILE));
	private static final List<Move> MOVES = List.of(ORB, STOMP, BEAM);
	private static final double STOMP_RADIUS = 3.5;
	private static final double BEAM_WIDTH = 1.2;
	private static final double BEAM_MAX = 22;

	private Stun stun = new Stun(0);

	public AutomatonEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world, BossBar.Color.BLUE);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 300)
				.add(EntityAttributes.GENERIC_ARMOR, 16)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.12)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0);
	}

	@Override
	protected List<Move> moves() {
		return MOVES;
	}

	public boolean stunned() {
		return stun.stunned();
	}

	/** La llama una esfera devuelta que lo alcanza: aturdimiento completo. */
	public void stun() {
		stun = Stun.hit();
		if (getWorld() instanceof ServerWorld world) {
			world.playSound(null, getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1.2f, 1.6f);
			for (ServerPlayerEntity p : world.getPlayers(p -> p.squaredDistanceTo(this) < 40 * 40)) {
				p.sendMessage(Text.translatable("automaton.aptitudes.stunned").formatted(Formatting.AQUA), true);
			}
		}
	}

	@Override
	public void mobTick() {
		super.mobTick();
		if (stun.stunned()) stun = stun.tick();
	}

	/** Aturdido recibe el doble de daño (las reglas puras lo calculan). */
	@Override
	public boolean damage(DamageSource source, float amount) {
		return super.damage(source, (float) PieceBossRules.automatonDamage(stun, amount));
	}

	/** Inmune al empuje: un golpe no lo desplaza. */
	@Override
	public void takeKnockback(double strength, double x, double z) {
	}

	@Override
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
		if (ticksLeft % 4 != 0) return;
		if (is(move, ORB)) {
			Vec3d p = headPosition();
			world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 4, 0.2, 0.2, 0.2, 0.02);
		} else if (is(move, STOMP)) {
			for (int i = 0; i < 16; i++) {
				double a = i * Math.PI / 8;
				world.spawnParticles(ParticleTypes.CRIT, getX() + Math.cos(a) * STOMP_RADIUS, getY() + 0.1,
						getZ() + Math.sin(a) * STOMP_RADIUS, 1, 0, 0, 0, 0);
			}
		} else if (is(move, BEAM)) {
			Vec3d dir = beamDirection(target);
			for (double t = 1; t <= BEAM_MAX; t += 1.5) {
				Vec3d p = headPosition().add(dir.multiply(t));
				world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0, 0, 0, 0);
			}
		}
	}

	@Override
	protected void strike(ServerWorld world, Move move, LivingEntity target) {
		float base = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (is(move, ORB)) {
			world.playSound(null, getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.HOSTILE, 1f, 1.2f);
			Vec3d from = headPosition();
			Vec3d aim = target.getEyePos().subtract(from);
			Vec3d velocity = aim.normalize().multiply(PieceBossRules.ORB_SPEED);
			world.spawnEntity(AutomatonOrbEntity.launch(world, this, from, velocity));
			if (phase() >= 3) { // fase 3 (v65): segunda esfera en abanico de 8 grados
				double rad = Math.toRadians(8);
				Vec3d turned = new Vec3d(velocity.x * Math.cos(rad) - velocity.z * Math.sin(rad), velocity.y,
						velocity.x * Math.sin(rad) + velocity.z * Math.cos(rad));
				world.spawnEntity(AutomatonOrbEntity.launch(world, this, from, turned));
			}
		} else if (is(move, STOMP)) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_STEP, SoundCategory.HOSTILE, 1.4f, 0.6f);
			world.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 3, 1.5, 0.1, 1.5, 0);
			for (LivingEntity e : victims(world, STOMP_RADIUS)) {
				if (Math.hypot(e.getX() - getX(), e.getZ() - getZ()) <= STOMP_RADIUS) hit(e, base * 1.1f, 0.8);
			}
		} else if (is(move, BEAM)) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.3f, 1.0f);
			Vec3d from = headPosition();
			Vec3d dir = beamDirection(target);
			double length = Math.min(BEAM_MAX, blockDistance(world, from, dir));
			if (phase() >= 3) { // fase 3 (v65): barrido de 60 grados al frente del Autómata
				for (LivingEntity e : victims(world, BEAM_MAX + 1)) {
					if (BossMoveRules.inArc(e.getX() - getX(), e.getZ() - getZ(), getYaw(), 60, BEAM_MAX)) hit(e, base * 1.4f, 0.4);
				}
				return;
			}
			for (LivingEntity e : victims(world, BEAM_MAX + 1)) {
				Vec3d rel = e.getPos().add(0, 1, 0).subtract(from);
				double along = rel.dotProduct(dir);
				double across = rel.subtract(dir.multiply(along)).length();
				if (along >= 0 && along <= length && across <= BEAM_WIDTH) hit(e, base * 1.4f, 0.4);
			}
		}
	}

	/** Distancia hasta el primer bloque sólido en la dirección del rayo (o el máximo si no hay ninguno). */
	private double blockDistance(ServerWorld world, Vec3d from, Vec3d dir) {
		Vec3d to = from.add(dir.multiply(BEAM_MAX));
		BlockHitResult hit = world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, this));
		return hit.getType() == HitResult.Type.MISS ? BEAM_MAX : hit.getPos().distanceTo(from);
	}

	private Vec3d headPosition() {
		return getPos().add(0, getStandingEyeHeight() * 0.9, 0);
	}

	private Vec3d beamDirection(LivingEntity target) {
		Vec3d dir = target.getPos().subtract(getPos()).multiply(1, 0, 1);
		return dir.lengthSquared() < 1e-6 ? new Vec3d(0, 0, 1) : dir.normalize();
	}

	@Override
	protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
		super.dropEquipment(world, source, causedByPlayer);
		dropStack(new ItemStack(dev.aptitudes.item.ModItems.SOUL_SHARD, 6 + getRandom().nextInt(4)));
		dropStack(new ItemStack(dev.aptitudes.item.ModItems.ELITE_ESSENCE, 2));
	}

	/** Compara por id: la fase 3 usa movimientos distintos con el mismo id (y la misma animación). */
	private static boolean is(Move move, Move reference) {
		return move.id().equals(reference.id());
	}
}
