package dev.aptitudes.boss;

import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.boss.PieceBossRules.Guardian;
import dev.aptitudes.boss.PieceBossRules.Piece;
import dev.aptitudes.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * El Guardián Desmontado (v63, C4): armadura vacía con tres piezas (yelmo, escudo, guanteletes). Solo la pieza
 * activa absorbe daño; al romperla pasa a estar activa la siguiente. Con las tres rotas, el núcleo queda expuesto
 * y recibe ×1,5 de daño. Las piezas son estado de esta entidad (no entidades hijas).
 *
 * <p>Pendiente: la fase 3 (repetir ataques con menos enfriamiento) aún no tiene variante propia.</p>
 */
public class GuardianEntity extends AnimatedBossEntity {
	static final Move VOLLEY = new Move("volley", 1, 4, 14, 20, 50, 3);
	static final Move RING = new Move("ring", 1, 0, 6, 24, 60, 2);
	static final Move DIVE = new Move("dive", 2, 4, 16, 26, 80, 2);
	private static final List<Move> MOVES = List.of(VOLLEY, RING, DIVE);
	private static final double BEAM_LENGTH = 12;
	private static final double BEAM_WIDTH = 1.2;
	private static final double RING_INNER = 3;
	private static final double RING_OUTER = 6;
	private static final double DIVE_RADIUS = 3;

	private Guardian pieces = Guardian.fresh();
	private double pieceHealth = PieceBossRules.PIECE_HEALTH;

	public GuardianEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world, BossBar.Color.YELLOW);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 260)
				.add(EntityAttributes.GENERIC_ARMOR, 8)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.9)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.5);
	}

	@Override
	protected List<Move> moves() {
		return MOVES;
	}

	/** El daño extra de la fase 2 ya está en la mecánica de piezas: aquí no se suma nada más. */
	@Override
	protected double crackedBonus() {
		return 0;
	}

	/** Estado de las piezas (para el HUD y las pruebas). */
	public Guardian pieces() {
		return pieces;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (amount <= 0) return false;
		if (pieces.coreExposed()) {
			return super.damage(source, (float) (amount * PieceBossRules.CORE_DAMAGE_MULTIPLIER));
		}
		// Con piezas puestas, solo un jugador rompe piezas; el resto de fuentes no hace nada.
		if (!(source.getAttacker() instanceof PlayerEntity player)) return false;
		Piece piece = pieces.active();
		PieceBossRules.Result result = pieces.hit(piece, amount, pieceHealth);
		if (result.broke()) {
			pieces = result.state();
			pieceHealth = PieceBossRules.PIECE_HEALTH;
			announce(player, piece);
		} else {
			pieceHealth -= amount;
		}
		return false;
	}

	private void announce(PlayerEntity breaker, Piece broken) {
		if (!(getWorld() instanceof ServerWorld world)) return;
		Text name = Text.translatable("piece.aptitudes." + broken.name().toLowerCase(java.util.Locale.ROOT));
		Text message = Text.translatable("guardian.aptitudes.piece_broken", name).formatted(Formatting.GOLD);
		world.playSound(null, getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1f, 0.7f);
		for (ServerPlayerEntity p : bossBarPlayers()) p.sendMessage(message, true);
		if (pieces.coreExposed()) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1.2f, 0.6f);
			for (ServerPlayerEntity p : bossBarPlayers()) {
				p.sendMessage(Text.translatable("guardian.aptitudes.core_exposed").formatted(Formatting.RED), false);
			}
		}
		if (breaker != null) world.spawnParticles(ParticleTypes.CRIT, getX(), getBodyY(0.6), getZ(), 12, 0.6, 0.6, 0.6, 0.1);
	}

	/** Jugadores que ven la barra del jefe (los que lo siguen). */
	private List<ServerPlayerEntity> bossBarPlayers() {
		return getWorld() instanceof ServerWorld world
				? world.getPlayers(p -> p.squaredDistanceTo(this) < 40 * 40).stream().map(p -> (ServerPlayerEntity) p).toList()
				: List.of();
	}

	@Override
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
		if (ticksLeft % 4 != 0) return;
		if (move == VOLLEY) {
			Vec3d dir = beamDirection(target);
			for (double t = 1; t <= BEAM_LENGTH; t += 1.5) {
				Vec3d p = getPos().add(dir.multiply(t)).add(0, 1.2, 0);
				world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
			}
		} else if (move == RING) {
			circle(world, getX(), getY() + 0.1, getZ(), RING_INNER, ParticleTypes.CRIT);
			circle(world, getX(), getY() + 0.1, getZ(), RING_OUTER, ParticleTypes.CRIT);
		} else if (move == DIVE) {
			circle(world, target.getX(), target.getY() + 0.1, target.getZ(), DIVE_RADIUS, ParticleTypes.SOUL_FIRE_FLAME);
		}
	}

	@Override
	protected void strike(ServerWorld world, Move move, LivingEntity target) {
		float base = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (move == VOLLEY) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1f, 0.6f);
			Vec3d dir = beamDirection(target);
			for (LivingEntity e : victims(world, BEAM_LENGTH + 1)) {
				Vec3d rel = e.getPos().subtract(getPos()).multiply(1, 0, 1);
				double along = rel.dotProduct(dir);
				double across = rel.subtract(dir.multiply(along)).length();
				if (along >= 0 && along <= BEAM_LENGTH && across <= BEAM_WIDTH) hit(e, base * 0.7f, 0.5);
			}
		} else if (move == RING) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 1.1f, 0.8f);
			for (LivingEntity e : victims(world, RING_OUTER)) {
				double d = Math.hypot(e.getX() - getX(), e.getZ() - getZ());
				if (d >= RING_INNER && d <= RING_OUTER) hit(e, base * 0.7f, 0.6);
			}
		} else if (move == DIVE) {
			// Cae sobre el lugar marcado: el círculo del aviso está donde estaba el objetivo.
			world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_RAVAGER_STEP, SoundCategory.HOSTILE, 1.2f, 0.7f);
			world.spawnParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.2, target.getZ(), 2, 1, 0.1, 1, 0);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
					new net.minecraft.util.math.Box(target.getX() - DIVE_RADIUS, target.getY() - 1, target.getZ() - DIVE_RADIUS,
							target.getX() + DIVE_RADIUS, target.getY() + 2, target.getZ() + DIVE_RADIUS), this::hittable)) {
				hit(e, base * 1.1f, 0.5);
			}
		}
	}

	private Vec3d beamDirection(LivingEntity target) {
		Vec3d dir = target.getPos().subtract(getPos()).multiply(1, 0, 1);
		return dir.lengthSquared() < 1e-6 ? new Vec3d(0, 0, 1) : dir.normalize();
	}

	private static void circle(ServerWorld world, double x, double y, double z, double radius, net.minecraft.particle.ParticleEffect particle) {
		for (int i = 0; i < 16; i++) {
			double a = i * Math.PI / 8;
			world.spawnParticles(particle, x + Math.cos(a) * radius, y, z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("guardian_pieces", pieces.intact());
		nbt.putDouble("guardian_piece_hp", pieceHealth);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		pieces = nbt.contains("guardian_pieces") ? new Guardian(nbt.getInt("guardian_pieces")) : Guardian.fresh();
		pieceHealth = nbt.contains("guardian_piece_hp") ? nbt.getDouble("guardian_piece_hp") : PieceBossRules.PIECE_HEALTH;
	}

	@Override
	protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
		super.dropEquipment(world, source, causedByPlayer);
		dropStack(new ItemStack(ModItems.SOUL_SHARD, 6 + getRandom().nextInt(4)));
		dropStack(new ItemStack(ModItems.ELITE_ESSENCE, 2));
	}
}
