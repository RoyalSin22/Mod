package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.BossMoveRules.Move;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.event.WorldEventManager;
import dev.aptitudes.mob.ErranteEntity;
import dev.aptitudes.mob.NewMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * El Gran Osario Quebrado (V68): osamenta que la Niebla de almas levanta cuando el evento está avanzado. Guarda y cosecha
 * almas. Cinco ataques avisados (zarpazo, anillo de niebla, latigazo que lo cura, lamento que invoca Errantes y cosecha
 * de almas) y un rasgo pasivo, el pacto de almas: recibe un 35 % menos de daño mientras vivan sus Errantes invocados.
 * Las cifras y reglas puras están en {@link OsarioBossRules}; el diseño, en {@code docs/V68_JEFE_OSARIO.md}.
 *
 * <p>Si el servidor lo creó una sesión de Niebla ({@link #setFogSession}), se disuelve sin botín ni XP pasado el margen
 * configurado desde que la Niebla cierra. Un jefe invocado a mano (sin sesión) no se disuelve nunca.</p>
 */
public class GreatOssuaryEntity extends AnimatedBossEntity {
	/** Etiqueta de los Errantes que invoca: no dan botín de la Niebla ni XP, y el jefe los cuenta. */
	public static final String ADD_TAG = Aptitudes.MOD_ID + ".boss_add";

	static final Move RAKE = new Move("rake", 1, 0, 4.5, 16, 30, 3, AttackTags.of(AttackTags.Tag.PARRYABLE));
	static final Move MIST_RING = new Move("mist_ring", 1, 0, 6, 28, 70, 2, AttackTags.of(AttackTags.Tag.GROUND));
	static final Move SOUL_LASH = new Move("soul_lash", 1, 4, 14, 24, 80, 2, AttackTags.of(AttackTags.Tag.PROJECTILE));
	static final Move WAIL = new Move("wail", 2, 0, 32, 34, 400, 1);
	static final Move HARVEST = new Move("harvest", 3, 0, 8, 40, 220, 2,
			AttackTags.of(AttackTags.Tag.HEAVY, AttackTags.Tag.GROUND));
	private static final List<Move> ALL = List.of(RAKE, MIST_RING, SOUL_LASH, WAIL, HARVEST);
	private static final List<Move> WITHOUT_WAIL = List.of(RAKE, MIST_RING, SOUL_LASH, HARVEST);

	private static final double RAKE_RANGE = 4.5, RAKE_ARC = 120;
	private static final double RING_RADIUS = 5.5;
	private static final double LASH_LENGTH = 14, LASH_WIDTH = 1.2;
	private static final double HARVEST_RADIUS = 8, HARVEST_ABSORB = 12;
	private static final String NBT_SESSION = "aptitudes_fog_session", NBT_ADDS = "aptitudes_adds", NBT_CREDIT = "aptitudes_credit";

	/** Sesión de Niebla que lo levantó (null = invocado a mano). */
	@Nullable private UUID fogSession;
	private final Set<UUID> adds = new HashSet<>();
	/** Daño que cada jugador le ha hecho (decide quién recibe el trofeo). */
	private final Map<UUID, Float> credit = new LinkedHashMap<>();

	public GreatOssuaryEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world, BossBar.Color.PURPLE);
		this.experiencePoints = 250;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 340)
				.add(EntityAttributes.GENERIC_ARMOR, 8)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0);
	}

	// ------------------------------------------------------------------------------------------- Sesión y estado

	public void setFogSession(@Nullable UUID session) {
		this.fogSession = session;
	}

	@Nullable
	public UUID fogSession() {
		return fogSession;
	}

	/** Daño acumulado por jugador (copia de solo lectura). */
	public Map<UUID, Float> damageCredit() {
		return Map.copyOf(credit);
	}

	/** Errantes invocados que siguen vivos (poda los que ya no existen). */
	public int livingAdds(ServerWorld world) {
		adds.removeIf(id -> {
			Entity e = world.getEntity(id);
			return e == null || !e.isAlive();
		});
		return adds.size();
	}

	@Override
	protected List<Move> moves() {
		// En el cliente (registro de animaciones) siempre el repertorio completo.
		if (!(getWorld() instanceof ServerWorld world)) return ALL;
		return OsarioBossRules.canWail(livingAdds(world)) ? ALL : WITHOUT_WAIL;
	}

	// ------------------------------------------------------------------------------------------------ Combate

	/** Pacto de almas: menos daño mientras haya acompañantes. Anota el daño de cada jugador para repartir el trofeo. */
	@Override
	public boolean damage(DamageSource source, float amount) {
		float mult = getWorld() instanceof ServerWorld world ? OsarioBossRules.damageMultiplier(livingAdds(world)) : 1.0f;
		float before = getHealth();
		boolean result = super.damage(source, amount * mult);
		if (getWorld() instanceof ServerWorld && source.getAttacker() instanceof ServerPlayerEntity player) {
			float dealt = before - getHealth();
			if (dealt > 0) credit.merge(player.getUuid(), dealt, Float::sum);
		}
		return result;
	}

	@Override
	protected boolean hittable(LivingEntity e) {
		return super.hittable(e) && !e.getCommandTags().contains(ADD_TAG);
	}

	@Override
	protected void telegraph(ServerWorld world, Move move, LivingEntity target, int ticksLeft) {
		if (ticksLeft % 4 != 0) return;
		if (move.id().equals(MIST_RING.id())) {
			ring(world, RING_RADIUS, ParticleTypes.SOUL);
		} else if (move.id().equals(HARVEST.id())) {
			ring(world, HARVEST_RADIUS, ParticleTypes.SOUL_FIRE_FLAME);
		} else if (move.id().equals(SOUL_LASH.id())) {
			Vec3d dir = horizontalDirection(target);
			for (double t = 1; t <= LASH_LENGTH; t += 1.5) {
				Vec3d p = chest().add(dir.multiply(t));
				world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0, 0, 0, 0);
			}
		} else if (move.id().equals(WAIL.id())) {
			world.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 1.5, getZ(), 6, 1.0, 1.2, 1.0, 0.03);
		} else if (move.id().equals(RAKE.id())) {
			world.spawnParticles(ParticleTypes.CRIT, getX(), getBodyY(0.6), getZ(), 3, 0.8, 0.3, 0.8, 0.05);
		}
	}

	@Override
	protected void strike(ServerWorld world, Move move, LivingEntity target) {
		float base = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (move.id().equals(RAKE.id())) {
			world.playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.1f, 0.5f);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(RAKE_RANGE), this::hittable)) {
				if (BossMoveRules.inArc(e.getX() - getX(), e.getZ() - getZ(), getYaw(), RAKE_ARC, RAKE_RANGE)
						&& !parried(e, move)) {
					hit(e, base, 0.6);
				}
			}
		} else if (move.id().equals(MIST_RING.id())) {
			world.playSound(null, getBlockPos(), SoundEvents.PARTICLE_SOUL_ESCAPE.value(), SoundCategory.HOSTILE, 1.4f, 0.6f);
			world.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 0.3, getZ(), 40, 2.5, 0.2, 2.5, 0.05);
			for (LivingEntity e : victims(world, RING_RADIUS)) {
				if (Math.hypot(e.getX() - getX(), e.getZ() - getZ()) <= RING_RADIUS) {
					hit(e, base * 1.1f, 0.3);
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0));
				}
			}
		} else if (move.id().equals(SOUL_LASH.id())) {
			lash(world, target, base);
		} else if (move.id().equals(WAIL.id())) {
			wail(world);
		} else if (move.id().equals(HARVEST.id())) {
			harvest(world, base);
		}
	}

	private void lash(ServerWorld world, LivingEntity target, float base) {
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.0f, 0.7f);
		Vec3d from = chest();
		Vec3d dir = horizontalDirection(target);
		double length = Math.min(LASH_LENGTH, blockDistance(world, from, dir));
		int hits = 0;
		for (LivingEntity e : victims(world, LASH_LENGTH + 1)) {
			Vec3d rel = e.getPos().add(0, 1, 0).subtract(from);
			double along = rel.dotProduct(dir);
			double across = rel.subtract(dir.multiply(along)).length();
			if (along >= 0 && along <= length && across <= LASH_WIDTH) {
				hit(e, base * 0.9f, 0.3);
				if (e instanceof ServerPlayerEntity) hits++;
			}
		}
		restoreHealth(OsarioBossRules.lashHeal(hits, getMaxHealth()));
	}

	/** Invoca Errantes (sin botín de la Niebla ni XP) alrededor del jefe, hasta el tope de vivos. */
	private void wail(ServerWorld world) {
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.4f, 0.5f);
		int count = OsarioBossRules.addsToSummon(livingAdds(world));
		for (int i = 0; i < count; i++) {
			ErranteEntity add = NewMobs.ERRANTE.create(world);
			if (add == null) continue;
			double angle = getRandom().nextDouble() * Math.PI * 2;
			double radius = 3.0 + getRandom().nextDouble() * 1.5;
			double x = getX() + Math.cos(angle) * radius, z = getZ() + Math.sin(angle) * radius;
			add.refreshPositionAndAngles(x, getY(), z, getRandom().nextFloat() * 360f, 0);
			if (!world.isSpaceEmpty(add, add.getBoundingBox())) {
				add.refreshPositionAndAngles(x, getY() + 1, z, getRandom().nextFloat() * 360f, 0);
				if (!world.isSpaceEmpty(add, add.getBoundingBox())) continue;
			}
			add.initialize(world, world.getLocalDifficulty(add.getBlockPos()), SpawnReason.EVENT, null);
			add.addCommandTag(ADD_TAG);
			add.addCommandTag(Elites.CHECKED);
			add.dropNoExperience();
			add.setPersistent();
			world.spawnEntityAndPassengers(add);
			adds.add(add.getUuid());
			world.spawnParticles(ParticleTypes.SOUL, add.getX(), add.getBodyY(0.5), add.getZ(), 14, 0.3, 0.6, 0.3, 0.03);
		}
	}

	/** Onda de radio 8 que además absorbe a los Errantes invocados cercanos y se cura con ellos. */
	private void harvest(ServerWorld world, float base) {
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 1.0f, 0.6f);
		world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.3, getZ(), 60, 3.5, 0.3, 3.5, 0.08);
		for (LivingEntity e : victims(world, HARVEST_RADIUS)) {
			if (Math.hypot(e.getX() - getX(), e.getZ() - getZ()) <= HARVEST_RADIUS) {
				hit(e, base * 1.4f, 1.2);
			}
		}
		int consumed = 0;
		for (UUID id : new ArrayList<>(adds)) {
			Entity e = world.getEntity(id);
			if (e != null && e.isAlive() && e.squaredDistanceTo(this) <= HARVEST_ABSORB * HARVEST_ABSORB) {
				world.spawnParticles(ParticleTypes.SOUL, e.getX(), e.getBodyY(0.5), e.getZ(), 16, 0.3, 0.6, 0.3, 0.05);
				e.discard();
				consumed++;
			}
		}
		adds.removeIf(id -> world.getEntity(id) == null);
		restoreHealth(OsarioBossRules.harvestHeal(consumed, getMaxHealth()));
	}

	private void restoreHealth(float amount) {
		setHealth(OsarioBossRules.healedTo(getHealth(), amount, getMaxHealth()));
	}

	// ------------------------------------------------------------------------------ Ciclo de vida y recompensa

	@Override
	protected void mobTick() {
		super.mobTick();
		if (!(getWorld() instanceof ServerWorld world) || fogSession == null || world.getTime() % 20 != 0) return;
		long closed = WorldEventManager.fogClosedAt(world, fogSession);
		AptitudesConfig cfg = Aptitudes.config();
		if (OsarioBossRules.shouldDissolve(world.getTime(), closed, cfg.osarioBossLingerTicks)) dissolve(world);
	}

	/** Se va con la Niebla: sin botín ni XP (no es una muerte) y retirando a sus Errantes. */
	private void dissolve(ServerWorld world) {
		world.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 1.5, getZ(), 60, 1.0, 1.5, 1.0, 0.05);
		world.playSound(null, getBlockPos(), SoundEvents.PARTICLE_SOUL_ESCAPE.value(), SoundCategory.HOSTILE, 1.6f, 0.5f);
		dismissAdds(world);
		discard();
	}

	private void dismissAdds(ServerWorld world) {
		for (UUID id : new ArrayList<>(adds)) {
			Entity e = world.getEntity(id);
			if (e != null) {
				world.spawnParticles(ParticleTypes.SOUL, e.getX(), e.getBodyY(0.5), e.getZ(), 10, 0.3, 0.5, 0.3, 0.03);
				e.discard();
			}
		}
		adds.clear();
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
		if (getWorld() instanceof ServerWorld world) {
			rewardPlayers(world);
			dismissAdds(world);
		}
	}

	/** Reparto del trofeo y los fragmentos entre los jugadores con daño suficiente (F4). */
	protected void rewardPlayers(ServerWorld world) {
	}

	// ----------------------------------------------------------------------------------------------- Persistencia

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (fogSession != null) nbt.putUuid(NBT_SESSION, fogSession);
		NbtList addList = new NbtList();
		for (UUID id : adds) addList.add(new net.minecraft.nbt.NbtIntArray(Uuids.toIntArray(id)));
		nbt.put(NBT_ADDS, addList);
		NbtList creditList = new NbtList();
		for (Map.Entry<UUID, Float> e : credit.entrySet()) {
			NbtCompound c = new NbtCompound();
			c.putUuid("id", e.getKey());
			c.putFloat("damage", e.getValue());
			creditList.add(c);
		}
		nbt.put(NBT_CREDIT, creditList);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		fogSession = nbt.containsUuid(NBT_SESSION) ? nbt.getUuid(NBT_SESSION) : null;
		adds.clear();
		for (NbtElement element : nbt.getList(NBT_ADDS, NbtElement.INT_ARRAY_TYPE)) {
			if (element instanceof net.minecraft.nbt.NbtIntArray array && array.getIntArray().length == 4) {
				adds.add(Uuids.toUuid(array.getIntArray()));
			}
		}
		credit.clear();
		for (NbtElement element : nbt.getList(NBT_CREDIT, NbtElement.COMPOUND_TYPE)) {
			NbtCompound c = (NbtCompound) element;
			if (c.containsUuid("id") && c.getFloat("damage") > 0) credit.put(c.getUuid("id"), c.getFloat("damage"));
		}
	}

	// ------------------------------------------------------------------------------------------------- Ayudas

	private void ring(ServerWorld world, double radius, net.minecraft.particle.ParticleEffect particle) {
		for (int i = 0; i < 20; i++) {
			double a = i * Math.PI / 10;
			world.spawnParticles(particle, getX() + Math.cos(a) * radius, getY() + 0.1, getZ() + Math.sin(a) * radius, 1, 0, 0, 0, 0);
		}
	}

	private Vec3d chest() {
		return getPos().add(0, getStandingEyeHeight() * 0.8, 0);
	}

	private Vec3d horizontalDirection(LivingEntity target) {
		Vec3d dir = target.getPos().subtract(getPos()).multiply(1, 0, 1);
		return dir.lengthSquared() < 1e-6 ? new Vec3d(0, 0, 1) : dir.normalize();
	}

	/** Distancia hasta el primer bloque sólido en la dirección del latigazo (o el máximo si no hay ninguno). */
	private double blockDistance(ServerWorld world, Vec3d from, Vec3d dir) {
		Vec3d to = from.add(dir.multiply(LASH_LENGTH));
		BlockHitResult hit = world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, this));
		return hit.getType() == HitResult.Type.MISS ? LASH_LENGTH : hit.getPos().distanceTo(from);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WITHER_DEATH;
	}
}
