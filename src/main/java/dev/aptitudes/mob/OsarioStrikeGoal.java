package dev.aptitudes.mob;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.event.WorldEventManager;
import dev.aptitudes.sound.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static dev.aptitudes.mob.OsarioStrikeRules.Action;
import static dev.aptitudes.mob.OsarioStrikeRules.Attack;

/** Ejecuta los ataques en el servidor. La navegación cede el control durante la acción. */
final class OsarioStrikeGoal extends Goal {
	private static final Map<ServerWorld, OsarioStrikeRules.Pressure> SLOTS = new WeakHashMap<>();
	private static final BlockStateParticleEffect EARTH = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState());
	private static final BlockStateParticleEffect BONE = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.getDefaultState());
	private static final DustParticleEffect ECTOPLASM = new DustParticleEffect(new Vector3f(77 / 255f, 226 / 255f, 166 / 255f), 1);
	private static final double PATH_SPEED = 1.0;
	private static final double DASH_STEP = OsarioStrikeRules.DASH_SPEED;
	private final OsarioEntity mob;
	private final long[] cooldown = new long[Attack.values().length];
	private OsarioStrikeRules.Execution execution;
	private LivingEntity lockedTarget;
	private UUID alertedTarget, pressureTarget;
	private long readyAt, repathAt, retryAt, chaseStart, lastSeenAt, cancelAt = Long.MAX_VALUE;
	private Vec3d lastSeen, dashFrom, dashVector = Vec3d.ZERO;
	private float attackYaw, previousTargetYaw;
	private double previousDistance;
	private int retreatTicks, repeats;
	private boolean dashStopped, eliteAtStart;
	private Attack last;

	OsarioStrikeGoal(OsarioEntity mob) {
		this.mob = mob;
		setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	static void registerLifecycle() {
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof OsarioEntity osario) osario.releaseStrike(world);
			if (entity instanceof PlayerEntity) {
				var pressure = SLOTS.get(world);
				if (pressure != null) pressure.removeTarget(entity.getUuid());
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SLOTS.keySet().removeIf(world -> world.getServer() == server));
	}

	@Override
	public boolean canStart() {
		return mob.isAlive() && (execution != null || valid(mob.getTarget())
				|| lastSeen != null && mob.getWorld().getTime() - lastSeenAt < 40);
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
		mob.getNavigation().stop();
		repathAt = 0;
	}

	void serverTick(ServerWorld world) {
		long now = world.getTime();
		LivingEntity target = mob.getTarget();
		mob.setAttacking(valid(target) || execution != null);
		if (execution != null) {
			tickAttack(world, now);
			return;
		}

		long stamp = mob.actionStamp();
		Action action = Action.from(stamp);
		if (action != Action.NONE && now - (stamp >>> Action.BITS) >= action.duration) {
			mob.setAction(Action.NONE, now);
			action = Action.NONE;
		}
		if (!valid(target)) {
			if (now - lastSeenAt >= 40) {
				lastSeen = null;
				alertedTarget = null;
			}
			if (lastSeen != null && action == Action.NONE && now >= repathAt) {
				repathAt = now + 8;
				mob.getNavigation().startMovingTo(lastSeen.x, lastSeen.y, lastSeen.z, PATH_SPEED);
			}
			return;
		}

		double dx = target.getX() - mob.getX(), dz = target.getZ() - mob.getZ();
		double distance = Math.hypot(dx, dz) / mob.getScale();
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float angle = Math.abs(MathHelper.wrapDegrees(yaw - mob.getBodyYaw()));
		boolean seen = mob.getVisibilityCache().canSee(target);
		if (seen) {
			lastSeen = target.getPos();
			lastSeenAt = now;
		}
		if (action != Action.NONE) {
			if (action == Action.ALERT) turnToward(target, yaw);
			return;
		}
		if (!target.getUuid().equals(alertedTarget)) {
			alertedTarget = target.getUuid();
			chaseStart = now + Action.ALERT.duration;
			previousDistance = distance;
			previousTargetYaw = yaw;
			retreatTicks = 0;
			mob.getNavigation().stop();
			turnToward(target, yaw);
			mob.setAction(Action.ALERT, now);
			sound(world, ModSounds.OSARIO_ALERT, .73f);
			return;
		}

		double angular = Math.abs(MathHelper.wrapDegrees(yaw - previousTargetYaw));
		retreatTicks = distance > previousDistance + .02 ? Math.min(4, retreatTicks + 1) : 0;
		previousDistance = distance;
		previousTargetYaw = yaw;
		if (distance < 7) turnToward(target, yaw);
		else mob.getLookControl().lookAt(target, 30, 30);
		if (seen && angle <= 60 && now >= readyAt && now >= retryAt && distance <= 6.5) {
			retryAt = now + 4;
			Attack attack = choose(world, target, now, distance, angle, angular, yaw);
			if (attack != null && begin(world, target, attack, now, yaw)) return;
		}
		if (now >= repathAt) {
			repathAt = now + 8;
			if (seen && distance <= 1.6) {
				mob.getNavigation().stop();
			} else if (seen) {
				mob.getNavigation().startMovingTo(target, PATH_SPEED);
			} else if (lastSeen != null) {
				mob.getNavigation().startMovingTo(lastSeen.x, lastSeen.y, lastSeen.z, PATH_SPEED);
			} else {
				mob.getNavigation().startMovingTo(target, PATH_SPEED);
			}
		}
	}

	private Attack choose(ServerWorld world, LivingEntity target, long now, double distance, float angle, double angular, float yaw) {
		EnumSet<Attack> available = EnumSet.noneOf(Attack.class);
		for (Attack attack : Attack.values()) if (now >= cooldown[attack.ordinal()]) available.add(attack);
		if (angle > 40) available.remove(Attack.CLAW);
		boolean dashReady = distance >= 3.2 && now - chaseStart >= 40 && Math.abs(target.getY() - mob.getY()) <= 1.2
				&& mob.isOnGround() && !inFluid() && clearDash(world, Vec3d.fromPolar(0, yaw), DASH_STEP * OsarioStrikeRules.DASH_TICKS);
		if (!dashReady) available.remove(Attack.LUNGE);
		if (available.isEmpty()) return null;
		boolean crowded = distance <= 2.4 && available.contains(Attack.SLAM)
				&& world.getEntitiesByClass(PlayerEntity.class, mob.getBoundingBox().expand(3),
						player -> valid(player) && player.squaredDistanceTo(mob) <= 9).size() >= 2;
		boolean denseFog = mob.getCommandTags().contains(WorldEventManager.TAG)
				&& world.getEntitiesByClass(MobEntity.class, mob.getBoundingBox().expand(8),
						entity -> entity.isAlive() && entity.getCommandTags().contains(WorldEventManager.TAG)
								&& entity.squaredDistanceTo(mob) <= 64).size() >= 6;
		var context = new OsarioStrikeRules.Choice(distance, Elites.isElite(mob) || Elites.isChampion(mob), angular >= 4,
				crowded, retreatTicks >= 4, denseFog, enraged(), available, last, repeats);
		return OsarioStrikeRules.choose(context, mob.getUuid().getLeastSignificantBits() ^ now);
	}

	private boolean begin(ServerWorld world, LivingEntity target, Attack attack, long now, float yaw) {
		if (target instanceof PlayerEntity && !SLOTS.computeIfAbsent(world, key -> new OsarioStrikeRules.Pressure())
				.acquire(target.getUuid(), mob.getUuid())) {
			retryAt = now + 10;
			var away = mob.getPos().subtract(target.getPos()).multiply(1, 0, 1);
			if (away.horizontalLengthSquared() > 1e-6) {
				var stand = target.getPos().add(away.normalize().multiply(2.1));
				mob.getNavigation().startMovingTo(stand.x, stand.y, stand.z, PATH_SPEED);
				repathAt = now + 10;
			}
			return false;
		}
		pressureTarget = target instanceof PlayerEntity ? target.getUuid() : null;
		lockedTarget = target;
		execution = new OsarioStrikeRules.Execution(attack, now, enraged());
		eliteAtStart = Elites.isElite(mob) || Elites.isChampion(mob);
		lockYaw(yaw);
		mob.setAction(Action.of(attack), now);
		mob.getNavigation().stop();
		dashStopped = false;
		dashVector = Vec3d.ZERO;
		cancelAt = Long.MAX_VALUE;
		return true;
	}

	private void lockYaw(float yaw) {
		attackYaw = yaw;
		face(yaw);
	}

	private void turnToward(LivingEntity target, float yaw) {
		mob.getLookControl().lookAt(target, 30, 30);
		float delta = MathHelper.wrapDegrees(yaw - mob.getBodyYaw());
		face(mob.getBodyYaw() + Math.clamp(delta, -30, 30));
	}

	private void face(float yaw) {
		mob.setYaw(yaw);
		mob.setBodyYaw(yaw);
		mob.setHeadYaw(yaw);
	}

	private boolean valid(LivingEntity target) {
		if (target == null || target == mob || target instanceof OsarioEntity || target.isRemoved() || !target.isAlive()
				|| target.getWorld() != mob.getWorld() || !target.isAttackable() || mob.isTeammate(target)
				|| !mob.canTarget(target) || AbilityState.shouldBlockTarget(mob, target)) return false;
		if (target instanceof PlayerEntity player) return !player.isCreative() && !player.isSpectator();
		return target == mob.getTarget() || target == lockedTarget
				|| target instanceof MobEntity summon && AbilityState.isSummon(summon) && summon.getTarget() == mob;
	}

	private boolean enraged() {
		return Aptitudes.config().threatEnabled && Aptitudes.config().threatTactics
				&& mob.getCommandTags().contains(Aptitudes.MOD_ID + ".threat_enraged")
				&& mob.hasStatusEffect(StatusEffects.SPEED) && mob.hasStatusEffect(StatusEffects.STRENGTH);
	}

	private void tickAttack(ServerWorld world, long now) {
		var strike = execution;
		Attack attack = strike.attack;
		int age = strike.age(now);
		mob.getNavigation().stop();
		face(attackYaw);
		if (now >= cancelAt || age >= strike.end) {
			finish(world, now, cancelAt != Long.MAX_VALUE);
			return;
		}
		if (age < attack.cancelBefore && !valid(lockedTarget)) {
			if (cancelAt == Long.MAX_VALUE) {
				cancelAt = now + 4;
				release(world);
			}
			return;
		}
		if (cancelAt != Long.MAX_VALUE) return;
		if (age == attack.warning) warning(world, attack);
		if (attack == Attack.HEAVY && age >= attack.warning && age <= attack.impact) trail(world, age);
		if (age > attack.lastActive) release(world);
		if (attack == Attack.LUNGE) {
			tickDash(world, age);
		} else if (attack.active(age)) {
			if (age == attack.impact) impact(world, attack);
			sweep(world, strike);
		}
	}

	private void tickDash(ServerWorld world, int age) {
		int start = Attack.LUNGE.impact;
		if (age < start) return;
		if (age == start && valid(lockedTarget)) {
			dashVector = lockedTarget.getPos().subtract(mob.getPos()).multiply(1, 0, 1).normalize();
			if (dashVector.horizontalLengthSquared() > 0) {
				lockYaw((float) Math.toDegrees(Math.atan2(-dashVector.x, dashVector.z)));
				impact(world, Attack.LUNGE);
			}
		}
		if (Attack.LUNGE.active(age) && !dashStopped) {
			if (!valid(lockedTarget) || inFluid() || mob.horizontalCollision || dashVector.horizontalLengthSquared() == 0
					|| !clearDash(world, dashVector, DASH_STEP)) {
				stopDash(world);
				return;
			}
			dashFrom = mob.getPos();
			mob.setVelocity(dashVector.x * DASH_STEP, mob.getVelocity().y, dashVector.z * DASH_STEP);
			mob.velocityModified = true;
		} else if (age >= start + OsarioStrikeRules.DASH_TICKS && age < start + OsarioStrikeRules.DASH_TICKS + 3) {
			if (age == start + OsarioStrikeRules.DASH_TICKS && !dashStopped) impactDust(world, mob.getPos(), 5);
			if (!dashStopped) mob.setVelocity(mob.getVelocity().multiply(.4, 1, .4));
		}
	}

	private boolean inFluid() {
		return mob.isTouchingWater() || mob.isInLava();
	}

	/** Comprueba suelo y obstáculos antes del impulso y delante de cada paso, sin cargar chunks. */
	private boolean clearDash(ServerWorld world, Vec3d direction, double distance) {
		var body = mob.getBoundingBox();
		int steps = (int) Math.ceil(distance / .23);
		for (int i = 1; i <= steps; i++) {
			Vec3d advance = direction.multiply(distance * i / steps);
			Box projected = body.offset(advance);
			Vec3d leading = mob.getPos().add(advance).add(direction.multiply(mob.getWidth() * .45));
			if (!world.isChunkLoaded(BlockPos.ofFloored(leading)) || !world.getWorldBorder().contains(projected)
					|| !world.getFluidState(BlockPos.ofFloored(leading.add(0, .02, 0))).isEmpty()) return false;
			if (world.getBlockCollisions(mob, projected.contract(1e-5)).iterator().hasNext()) return false;
			Box ground = new Box(leading.x - .12, projected.minY - .38, leading.z - .12,
					leading.x + .12, projected.minY + .005, leading.z + .12);
			if (!world.getBlockCollisions(mob, ground).iterator().hasNext()) return false;
		}
		return true;
	}

	void afterMovement(ServerWorld world) {
		if (execution != null) face(attackYaw);
		Vec3d from = dashFrom;
		dashFrom = null;
		if (execution == null || from == null || dashStopped) return;
		if (inFluid() || mob.horizontalCollision || !valid(lockedTarget)) {
			stopDash(world);
			return;
		}
		Vec3d delta = mob.getPos().subtract(from);
		double radius = Attack.LUNGE.radius * mob.getScale();
		Box area = new Box(from, mob.getPos()).expand(radius, 0, radius)
				.stretch(0, Attack.LUNGE.maxY * mob.getScale(), 0).expand(1e-6);
		OsarioStrikeRules.DashContact best = null;
		LivingEntity victim = null;
		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area, this::valid)) {
			var contact = OsarioStrikeRules.dashContact(bounds(target.getBoundingBox(), from), delta.x, delta.y, delta.z, mob.getScale());
			if (contact == null || !visible(world, from, contact.point())) continue;
			if (best == null || contact.fraction() < best.fraction()
					|| contact.fraction() == best.fraction() && target.getId() < victim.getId()) {
				best = contact;
				victim = target;
			}
		}
		if (victim != null) {
			hit(execution, victim, dashVector);
			stopDash(world);
		}
	}

	private void stopDash(ServerWorld world) {
		if (dashStopped) return;
		dashStopped = true;
		mob.setVelocity(0, mob.getVelocity().y, 0);
		mob.velocityModified = true;
		impactDust(world, mob.getPos(), 5);
		release(world);
	}

	private void sweep(ServerWorld world, OsarioStrikeRules.Execution strike) {
		Attack attack = strike.attack;
		double scale = mob.getScale(), reach = (attack.radius + (attack == Attack.SLAM ? 1.5 : 0)) * scale;
		Vec3d center = mob.getPos(), forward = Vec3d.fromPolar(0, attackYaw);
		Box area = new Box(center.x - reach, center.y + attack.minY * scale, center.z - reach,
				center.x + reach, center.y + attack.maxY * scale, center.z + reach).expand(1e-6);
		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area, this::valid)) {
			if (execution != strike || !mob.isAlive()) return;
			var point = OsarioStrikeRules.contact(attack, bounds(target.getBoundingBox(), center), forward.x, forward.z, scale);
			if (point == null || !visible(world, center, point)) continue;
			Vec3d away = target.getPos().subtract(attack == Attack.SLAM ? center.add(forward.multiply(1.5 * scale)) : center);
			hit(strike, target, away.horizontalLengthSquared() <= 1e-6 ? forward : away);
		}
	}

	private void hit(OsarioStrikeRules.Execution strike, LivingEntity target, Vec3d direction) {
		if (!strike.firstAttempt(target.getUuid(), mob.getWorld().getTime())) return;
		float damage = (float) (mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * strike.attack.damage);
		if (target.damage(mob.getDamageSources().mobAttack(mob), damage)) {
			target.takeKnockback(strike.attack.knockback, -direction.x, -direction.z);
			if (strike.attack.lift > 0) target.addVelocity(0, strike.attack.lift, 0);
			target.velocityModified = true;
		}
	}

	private static OsarioStrikeRules.Bounds bounds(Box box, Vec3d origin) {
		return new OsarioStrikeRules.Bounds(box.minX - origin.x, box.minY - origin.y, box.minZ - origin.z,
				box.maxX - origin.x, box.maxY - origin.y, box.maxZ - origin.z);
	}

	private boolean visible(ServerWorld world, Vec3d from, OsarioStrikeRules.Point point) {
		Vec3d start = from.add(0, point.y(), 0), end = from.add(point.x(), point.y(), point.z());
		return world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, mob)).getType() == HitResult.Type.MISS;
	}

	private void warning(ServerWorld world, Attack attack) {
		Vec3d base = mob.getPos().add(Vec3d.fromPolar(0, attackYaw).multiply(.55 * mob.getScale()));
		SoundEvent sound = switch (attack) {
			case CLAW -> ModSounds.OSARIO_CLAW;
			case HEAVY -> ModSounds.OSARIO_HEAVY;
			case SLAM -> ModSounds.OSARIO_SLAM_CHARGE;
			case LUNGE -> ModSounds.OSARIO_LUNGE;
		};
		sound(world, sound, .64f);
		impactDust(world, base, 4);
	}

	private void trail(ServerWorld world, int age) {
		double progress = (age - Attack.HEAVY.warning) / 11.0;
		Vec3d direction = Vec3d.fromPolar(0, attackYaw + (float) (80 - 160 * progress));
		Vec3d point = mob.getPos().add(direction.multiply((1.1 + progress * .7) * mob.getScale()))
				.add(0, (1.72 - progress * 1.00) * mob.getScale(), 0);
		world.spawnParticles(ECTOPLASM, point.x, point.y, point.z, 1, .03, .03, .03, 0);
		if (age == 13 || age == 19) world.spawnParticles(ParticleTypes.SOUL, point.x, point.y, point.z, 1, 0, 0, 0, .01);
	}

	private void impact(ServerWorld world, Attack attack) {
		Vec3d forward = Vec3d.fromPolar(0, attackYaw);
		Vec3d point = mob.getPos().add(forward.multiply((attack == Attack.SLAM ? 1.5 : .75) * mob.getScale()));
		if (attack == Attack.SLAM) {
			sound(world, ModSounds.OSARIO_SLAM_HIT, .84f);
			for (int i = 0; i < 8; i++) {
				double a = i * Math.PI / 4;
				impactDust(world, point.add(Math.cos(a) * mob.getScale(), 0, Math.sin(a) * mob.getScale()), 2);
			}
		} else if (attack == Attack.CLAW) {
			world.spawnParticles(BONE, point.x, point.y + .65, point.z, 3, .16, .15, .16, .04);
			impactDust(world, point, 3);
		} else {
			impactDust(world, point, 6);
		}
	}

	private void impactDust(ServerWorld world, Vec3d point, int count) {
		world.spawnParticles(EARTH, point.x, point.y + .12, point.z, count, .20, .07, .20, .04);
	}

	private void sound(ServerWorld world, SoundEvent sound, float volume) {
		if (!mob.isSilent()) world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), sound, mob.getSoundCategory(), volume, 1);
	}

	private void finish(ServerWorld world, long now, boolean cancelled) {
		Attack attack = execution.attack;
		long cooldownStart = cancelled ? cancelAt - 4 : now;
		cooldown[attack.ordinal()] = cooldownStart + (cancelled ? attack.cooldown(eliteAtStart) / 2 : attack.cooldown(eliteAtStart));
		readyAt = cooldownStart + (cancelled ? OsarioStrikeRules.GLOBAL_COOLDOWN / 2 : OsarioStrikeRules.GLOBAL_COOLDOWN);
		if (!cancelled) {
			repeats = attack == last ? repeats + 1 : 1;
			last = attack;
		}
		reset(world);
		mob.setAction(Action.NONE, now);
	}

	void reset(ServerWorld world) {
		release(world);
		execution = null;
		lockedTarget = null;
		dashFrom = null;
	}

	private void release(ServerWorld world) {
		if (pressureTarget == null) return;
		var pressure = SLOTS.get(world);
		if (pressure != null) pressure.release(pressureTarget, mob.getUuid());
		pressureTarget = null;
	}

	boolean busy() {
		return execution != null;
	}
}
