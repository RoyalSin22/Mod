package dev.aptitudes.morph;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.ability.Targeting;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.network.FormSyncPayload;
import dev.aptitudes.network.MorphPayload;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lógica de transformación (servidor). La forma se ve gracias a {@link MorphPayload},
 * que se envía a todos los jugadores; el cliente dibuja el mob en lugar del
 * jugador. La caja de colisión sigue siendo la del jugador.
 */
public final class MorphManager {
	private static final Identifier HEALTH_MODIFIER = Aptitudes.id("morph_health");
	/** Transformaciones temporales (definitiva, Espejo Perfecto): no gastan energía. jugador → tick fin. */
	private static final Map<UUID, Integer> TIMED = new HashMap<>();
	private static final Map<UUID, Object> BAT_FLIGHTS = new HashMap<>();

	private MorphManager() {
	}

	public static MorphData get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.MORPH);
	}

	// --------------------------------------------------------- Desbloqueo

	public static boolean isUnlocked(ServerPlayerEntity player, MorphForm form) {
		if (form.killsToUnlock() < 0) return false;
		if (form.levelToUnlock() > 0) return RoleManager.get(player).level(Role.SHAPESHIFTER) >= form.levelToUnlock();
		return get(player).kills(form) >= form.killsToUnlock();
	}

	public static int unlockedCount(ServerPlayerEntity player) {
		int n = 0;
		for (MorphForm f : MorphForm.values()) if (isUnlocked(player, f)) n++;
		return n;
	}

	/** Cuenta muertes para desbloquear formas (con o sin el rol activo). */
	public static void onKill(ServerPlayerEntity player, EntityType<?> type) {
		MorphForm form = MorphForm.byType(type);
		if (form == null || form.killsToUnlock() <= 0) return;
		MorphData data = get(player);
		boolean was = isUnlocked(player, form);
		data.addKill(form);
		if (!was && isUnlocked(player, form)) {
			player.sendMessage(Text.translatable("aptitudes.morph.unlocked", Text.translatable(form.translationKey()))
					.formatted(Formatting.LIGHT_PURPLE), false);
		}
		syncForms(player);
	}

	public static void select(ServerPlayerEntity player, MorphForm form) {
		if (!form.selectable()) return;
		get(player).setSelected(form);
		syncForms(player);
	}

	// ---------------------------------------------------- Transformación

	/** Activa "Transformarse": si ya está transformado, vuelve a la forma humana. */
	public static boolean toggle(ServerPlayerEntity player) {
		MorphData data = get(player);
		if (data.isMorphed()) {
			unmorph(player);
			return true;
		}
		MorphForm form = data.selected();
		if (!isUnlocked(player, form)) {
			player.sendMessage(Text.translatable("aptitudes.morph.locked", Text.translatable(form.translationKey()))
					.formatted(Formatting.RED), true);
			return false;
		}
		morph(player, form.type(), 0, 0f);
		return true;
	}

	/**
	 * @param durationTicks 0 = indefinida (gasta energía); &gt;0 = temporal y gratuita
	 * @param bonusHealth   vida máxima extra mientras dure (Espejo Perfecto)
	 */
	public static void morph(ServerPlayerEntity player, EntityType<?> type, int durationTicks, float bonusHealth) {
		BAT_FLIGHTS.remove(player.getUuid());
		MorphData data = get(player);
		data.setActiveType(Registries.ENTITY_TYPE.getId(type).toString());
		data.setBonusHealth(bonusHealth);
		applyHealth(player, bonusHealth);
		if (durationTicks > 0) {
			int until = AbilityManager.now(player) + durationTicks;
			TIMED.put(player.getUuid(), until);
			AbilityState.schedule(player.getServer(), durationTicks, () -> {
				Integer current = TIMED.get(player.getUuid());
				if (current != null && current == until && !player.isRemoved()) unmorph(player);
			});
		} else {
			TIMED.remove(player.getUuid());
		}
		player.getServerWorld().spawnParticles(ParticleTypes.POOF, player.getX(), player.getBodyY(0.5), player.getZ(),
				25, 0.4, 0.6, 0.4, 0.02);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.0f, 1.2f);
		broadcast(player);
	}

	public static void unmorph(ServerPlayerEntity player) {
		BAT_FLIGHTS.remove(player.getUuid());
		MorphData data = get(player);
		if (!data.isMorphed()) return;
		data.setActiveType("");
		data.setBonusHealth(0f);
		applyHealth(player, 0f);
		TIMED.remove(player.getUuid());
		player.getServerWorld().spawnParticles(ParticleTypes.POOF, player.getX(), player.getBodyY(0.5), player.getZ(),
				25, 0.4, 0.6, 0.4, 0.02);
		broadcast(player);
	}

	private static void applyHealth(ServerPlayerEntity player, float bonus) {
		AttributeApplier.set(player, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_MODIFIER, bonus,
				EntityAttributeModifier.Operation.ADD_VALUE);
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	/** Forma actual si es una de las {@link MorphForm} (las copias arbitrarias devuelven null). */
	@Nullable
	public static MorphForm currentForm(ServerPlayerEntity player) {
		MorphData data = get(player);
		if (!data.isMorphed()) return null;
		Identifier id = Identifier.tryParse(data.activeType());
		return id == null ? null : MorphForm.byType(Registries.ENTITY_TYPE.get(id));
	}

	/** Bromista: los monstruos no atacan a un jugador transformado en un mob hostil. */
	public static boolean isCamouflaged(PlayerEntity player) {
		if (!(player instanceof ServerPlayerEntity sp)) return false;
		MorphForm form = currentForm(sp);
		return form != null && form.hostile() && RoleManager.activeRank(sp, "trickster") >= 1;
	}

	// ------------------------------------------------------------- Daño

	/** @return false para anular daño propio de la forma (explosión del Creeper, caídas del Slime). */
	public static boolean allowIncomingDamage(ServerPlayerEntity player, DamageSource source) {
		MorphForm form = currentForm(player);
		if (form == MorphForm.CREEPER && source.isIn(DamageTypeTags.IS_EXPLOSION) && source.getAttacker() == player) {
			return false;
		}
		return !(form == MorphForm.SLIME && source.isOf(DamageTypes.FALL));
	}

	// -------------------------------------------------------------- Tick

	/** Cada segundo: gasto de energía, pasivas de forma y sonidos de broma. */
	public static void tick(MinecraftServer server) {
		int now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			MorphData data = get(p);
			if (!data.isMorphed() || !p.isAlive()) continue;

			if (!TIMED.containsKey(p.getUuid()) && !p.isCreative()) {
				RoleData roles = RoleManager.get(p);
				double drain = Math.max(0.5, 3.0 - 0.5 * RoleManager.activeRank(p, "mimicry"));
				if (!roles.isActive(Role.SHAPESHIFTER) || roles.energy() < drain) {
					unmorph(p);
					p.sendMessage(Text.translatable("aptitudes.morph.exhausted").formatted(Formatting.RED), true);
					continue;
				}
				roles.setEnergy(roles.energy() - drain);
				AbilityManager.sendEnergy(p, false);
			}

			MorphForm form = currentForm(p);
			if (form != null) applyFormPassive(p, form);

			// Bromista rango 2+: de vez en cuando emites los sonidos del mob (también los oyen otros).
			if (form != null && RoleManager.activeRank(p, "trickster") >= 2 && now % 160 == 0) {
				SoundEvent ambient = ambientSound(form);
				if (ambient != null) {
					p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), ambient, SoundCategory.HOSTILE, 1.0f, 1.0f);
				}
			}
		}
	}

	private static void applyFormPassive(ServerPlayerEntity p, MorphForm form) {
		switch (form) {
			case ENDERMAN, SPIDER -> quiet(p, StatusEffects.NIGHT_VISION, 260, 0);
			case BLAZE -> quiet(p, StatusEffects.FIRE_RESISTANCE, 40, 0);
			case BAT -> {
				quiet(p, StatusEffects.NIGHT_VISION, 260, 0);
				if (p.isSneaking()) quiet(p, StatusEffects.SLOW_FALLING, 40, 0);
			}
			case CHICKEN -> {
				if (!p.isOnGround() && p.getVelocity().y < 0) quiet(p, StatusEffects.SLOW_FALLING, 30, 0);
			}
			case IRON_GOLEM -> {
				quiet(p, StatusEffects.RESISTANCE, 40, 0);
				quiet(p, StatusEffects.SLOWNESS, 40, 0);
			}
			case WITCH -> p.removeStatusEffect(StatusEffects.POISON);
			case WARDEN -> {
				quiet(p, StatusEffects.STRENGTH, 40, 1);
				quiet(p, StatusEffects.RESISTANCE, 40, 1);
				// Troll: oscuridad para los demás jugadores cercanos.
				for (PlayerEntity other : Targeting.playersAround(p, 12)) {
					if (other != p) other.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0));
				}
			}
			default -> {
			}
		}
	}

	@Nullable
	private static SoundEvent ambientSound(MorphForm form) {
		return switch (form) {
			case CREEPER -> SoundEvents.ENTITY_CREEPER_PRIMED;
			case ENDERMAN -> SoundEvents.ENTITY_ENDERMAN_AMBIENT;
			case SPIDER -> SoundEvents.ENTITY_SPIDER_AMBIENT;
			case BLAZE -> SoundEvents.ENTITY_BLAZE_AMBIENT;
			case BAT -> SoundEvents.ENTITY_BAT_AMBIENT;
			case CHICKEN -> SoundEvents.ENTITY_CHICKEN_AMBIENT;
			case WITCH -> SoundEvents.ENTITY_WITCH_AMBIENT;
			case VILLAGER -> SoundEvents.ENTITY_VILLAGER_AMBIENT;
			case WARDEN -> SoundEvents.ENTITY_WARDEN_HEARTBEAT;
			default -> null;
		};
	}

	// -------------------------------------------------------- Poderes

	/** Poder de la forma actual. @param r rango efectivo. @return false si no hubo efecto. */
	public static boolean usePower(ServerPlayerEntity p, int r) {
		MorphForm form = currentForm(p);
		if (form == null) return false;
		World world = p.getWorld();
		switch (form) {
			case CREEPER -> {
				world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1.0f, 0.5f);
				AbilityState.schedule(p.getServer(), 30, () -> {
					if (!p.isAlive() || currentForm(p) != MorphForm.CREEPER) return;
					world.createExplosion(p, p.getX(), p.getY(), p.getZ(), 3.0f + 0.3f * r,
							Aptitudes.config().morphExplosionsBreakBlocks ? World.ExplosionSourceType.TNT : World.ExplosionSourceType.NONE);
				});
				return true;
			}
			case ENDERMAN -> {
				Vec3d direction = p.getRotationVec(1.0f);
				Vec3d eye = Targeting.lookEnd(p, 16 + 4 * r).subtract(direction.multiply(0.6));
				Vec3d spot = Targeting.findFreeSpot(p, eye.subtract(0, p.getStandingEyeHeight(), 0));
				if (spot == null) return false;
				p.requestTeleport(spot.x, spot.y, spot.z);
				p.fallDistance = 0;
				sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT);
				return true;
			}
			case SPIDER -> {
				LivingEntity target = Targeting.raycastLiving(p, 5);
				if (target == null) return false;
				target.damage(p.getDamageSources().playerAttack(p), 3 + r);
				effect(target, StatusEffects.POISON, 100, 1);
				sound(p, SoundEvents.ENTITY_SPIDER_HURT);
				return true;
			}
			case BLAZE -> {
				for (int i = 0; i < 3; i++) {
					Vec3d dir = p.getRotationVec(1.0f).rotateY((float) Math.toRadians((i - 1) * 6));
					SmallFireballEntity fireball = new SmallFireballEntity(world, p, dir.normalize());
					fireball.setPosition(p.getX() + dir.x, p.getEyeY() - 0.1, p.getZ() + dir.z);
					world.spawnEntity(fireball);
				}
				sound(p, SoundEvents.ENTITY_BLAZE_SHOOT);
				return true;
			}
			case BAT -> {
				return flyAsBat(p);
			}
			case SLIME -> {
				p.setVelocity(p.getVelocity().x, 1.2 + 0.1 * r, p.getVelocity().z);
				p.velocityModified = true;
				p.fallDistance = 0;
				sound(p, SoundEvents.ENTITY_SLIME_JUMP);
				return true;
			}
			case CHICKEN -> {
				for (int i = 0; i < 4 + r; i++) {
					EggEntity egg = new EggEntity(world, p);
					egg.setVelocity(p, p.getPitch(), p.getYaw(), 0.0f, 1.5f, 8.0f);
					world.spawnEntity(egg);
				}
				sound(p, SoundEvents.ENTITY_CHICKEN_EGG);
				return true;
			}
			case IRON_GOLEM -> {
				for (LivingEntity e : Targeting.hostilesAround(p, 4)) {
					e.damage(p.getDamageSources().playerAttack(p), 6 + r);
					e.addVelocity(0, 0.8, 0);
					e.velocityModified = true;
				}
				sound(p, SoundEvents.ENTITY_IRON_GOLEM_ATTACK);
				return true;
			}
			case WITCH -> {
				LivingEntity target = Targeting.raycastLiving(p, 16);
				if (target == null) return false;
				effect(target, StatusEffects.POISON, 160, 0);
				effect(target, StatusEffects.SLOWNESS, 160, 0);
				effect(target, StatusEffects.WEAKNESS, 160, 0);
				Targeting.burst(p.getServerWorld(), ParticleTypes.WITCH, target, 20, 0.4);
				sound(p, SoundEvents.ENTITY_WITCH_THROW);
				return true;
			}
			case VILLAGER -> {
				effect(p, StatusEffects.SPEED, 120, 2);
				sound(p, SoundEvents.ENTITY_VILLAGER_NO);
				return true;
			}
			case WARDEN -> {
				LivingEntity target = Targeting.raycastLiving(p, 20);
				Vec3d end = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.lookEnd(p, 20);
				Targeting.particleLine(p.getServerWorld(), ParticleTypes.SONIC_BOOM, p.getEyePos(), end);
				sound(p, SoundEvents.ENTITY_WARDEN_SONIC_BOOM);
				if (target != null) target.damage(p.getDamageSources().sonicBoom(p), 10 + 2 * r);
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------- Red

	/** Envía la forma de {@code player} a todos los jugadores con el mod. */
	public static void broadcast(ServerPlayerEntity player) {
		MorphSize.refresh(player); // v26: la caja de colisión sigue a la forma (también al entrar)
		MorphPayload payload = new MorphPayload(player.getUuid(), get(player).activeType());
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			if (ServerPlayNetworking.canSend(other, MorphPayload.ID)) ServerPlayNetworking.send(other, payload);
		}
	}

	/** Al entrar: el recién llegado recibe las formas de todos y los demás la suya. */
	public static void onJoin(ServerPlayerEntity player) {
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			MorphData data = get(other);
			if (other != player && data.isMorphed() && ServerPlayNetworking.canSend(player, MorphPayload.ID)) {
				ServerPlayNetworking.send(player, new MorphPayload(other.getUuid(), data.activeType()));
			}
		}
		// Las transformaciones temporales no sobreviven a una desconexión.
		if (get(player).isMorphed() && get(player).bonusHealth() > 0) unmorph(player);
		broadcast(player);
		syncForms(player);
	}

	public static void onLeave(ServerPlayerEntity player) {
		BAT_FLIGHTS.remove(player.getUuid());
		TIMED.remove(player.getUuid());
		MorphPayload payload = new MorphPayload(player.getUuid(), "");
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			if (other != player && ServerPlayNetworking.canSend(other, MorphPayload.ID)) ServerPlayNetworking.send(other, payload);
		}
	}

	public static void syncForms(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, FormSyncPayload.ID)) return;
		MorphData data = get(player);
		MorphForm[] forms = MorphForm.values();
		int[] kills = new int[forms.length];
		int mask = 0;
		for (MorphForm f : forms) {
			kills[f.ordinal()] = data.kills(f);
			if (isUnlocked(player, f)) mask |= 1 << f.ordinal();
		}
		ServerPlayNetworking.send(player, new FormSyncPayload(kills, mask, data.selected().ordinal()));
	}

	private static boolean flyAsBat(ServerPlayerEntity player) {
		if (!canFlyAsBat(player)) return false;
		UUID id = player.getUuid();
		Object token = new Object();
		BAT_FLIGHTS.put(id, token);
		ServerWorld world = player.getServerWorld();
		effect(player, StatusEffects.SLOW_FALLING, 200, 0);
		sound(player, SoundEvents.ENTITY_BAT_TAKEOFF);
		AbilityState.repeat(world.getServer(), MorphFlightRules.PULSES, MorphFlightRules.INTERVAL, pulse -> {
			if (BAT_FLIGHTS.get(id) != token) return;
			if (player.isRemoved() || player.getWorld() != world || !canFlyAsBat(player)) {
				BAT_FLIGHTS.remove(id);
				return;
			}
			Vec3d look = player.getRotationVec(1.0f);
			player.setVelocity(look.x * MorphFlightRules.SPEED, MorphFlightRules.verticalSpeed(look.y),
					look.z * MorphFlightRules.SPEED);
			player.velocityModified = true;
			player.fallDistance = 0;
			if (pulse == MorphFlightRules.PULSES - 1) BAT_FLIGHTS.remove(id);
		});
		return true;
	}

	private static boolean canFlyAsBat(ServerPlayerEntity player) {
		return player.isAlive() && !player.isSpectator() && !player.hasVehicle() && !player.isTouchingWater()
				&& !player.getAbilities().flying && currentForm(player) == MorphForm.BAT
				&& RoleManager.get(player).isActive(Role.SHAPESHIFTER);
	}

	// ----------------------------------------------------- Utilidades

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void quiet(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}

	private static void sound(ServerPlayerEntity p, RegistryEntry<SoundEvent> sound) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}
}
