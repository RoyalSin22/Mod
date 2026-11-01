package dev.aptitudes.ability;

import dev.aptitudes.morph.MorphManager;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Habilidades características ("rotas"): una por rol, disponible desde la
 * evolución a Maestro (tecla Z). Son la seña de identidad de cada rol; las
 * definitivas (tecla R, Definitivo) son el segundo poder roto.
 */
public final class Signatures {
	public static final int ENERGY = 60;
	public static final int COOLDOWN_SECS = 120;

	private static final Map<Role, Ability> REGISTRY = new EnumMap<>(Role.class);
	/** Ingeniero: punto de teletransporte guardado. */
	private record Beacon(RegistryKey<World> world, Vec3d pos) {}
	private static final Map<UUID, Beacon> BEACONS = new HashMap<>();
	/** Asesino: objetivo marcado → (asesino, tick fin). */
	private static final Map<UUID, Object[]> DEATH_MARKS = new HashMap<>();

	static {
		// Paladín – Bastión sagrado: 10 s sin recibir NINGÚN daño, +50 energía, vida extra, defensa.
		REGISTRY.put(Role.PALADIN, (p, r) -> {
			AbilityState.setInvulnerable(p, 10 * 20);
			RoleData data = RoleManager.get(p);
			data.setEnergy(Math.min(AbilityManager.maxEnergy(p), data.energy() + 50));
			effect(p, StatusEffects.HEALTH_BOOST, 30 * 20, 2);
			effect(p, StatusEffects.ABSORPTION, 30 * 20, 2);
			effect(p, StatusEffects.RESISTANCE, 30 * 20, 1);
			p.heal(p.getMaxHealth());
			burst(p, ParticleTypes.END_ROD, 60);
			sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.8f);
			return true;
		});

		// Bersérker – Grito del Juicio: los mobs cercanos pierden la MITAD de su vida actual; tú, Fuerza III.
		REGISTRY.put(Role.BERSERKER, (p, r) -> {
			List<LivingEntity> targets = Targeting.hostilesAround(p, 8);
			for (LivingEntity e : targets) {
				float half = e.getHealth() / 2.0f;
				e.damage(p.getDamageSources().playerAttack(p), half); // pasa por armadura: no ignora defensas
				e.takeKnockback(1.0, p.getX() - e.getX(), p.getZ() - e.getZ());
			}
			effect(p, StatusEffects.STRENGTH, 15 * 20, 2);
			effect(p, StatusEffects.SPEED, 15 * 20, 1);
			world(p).spawnParticles(ParticleTypes.SONIC_BOOM, p.getX(), p.getBodyY(0.5), p.getZ(), 1, 0, 0, 0, 0);
			sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.5f);
			return true;
		});

		// Nigromante – Ejército de sangre: 4 esbirros con Fuerza II y Resistencia II.
		REGISTRY.put(Role.NECROMANCER, (p, r) -> {
			for (int i = 0; i < Minions.MAX_MINIONS; i++) {
				Minions.spawn(p, i % 2 == 0 ? EntityType.ZOMBIE : EntityType.SKELETON, i);
			}
			for (MobEntity minion : AbilityState.minionsOf(p)) {
				effect(minion, StatusEffects.STRENGTH, 45 * 20, 1);
				effect(minion, StatusEffects.RESISTANCE, 45 * 20, 1);
			}
			sound(p, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 0.6f);
			return true;
		});

		// Monje – Bendición del maestro: cura totalmente a los aliados cercanos y les sube las estadísticas.
		REGISTRY.put(Role.MONK, (p, r) -> {
			for (PlayerEntity ally : Targeting.playersAround(p, 10)) {
				ally.heal(ally.getMaxHealth());
				ally.extinguish();
				effect(ally, StatusEffects.STRENGTH, 20 * 20, 1);
				effect(ally, StatusEffects.SPEED, 20 * 20, 1);
				effect(ally, StatusEffects.RESISTANCE, 20 * 20, 1);
				effect(ally, StatusEffects.REGENERATION, 20 * 20, 1);
				Targeting.burst(world(p), ParticleTypes.TOTEM_OF_UNDYING, ally, 20, 0.6);
			}
			sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.6f);
			return true;
		});

		// Elementalista – Bastión elemental: muro protector + dominio de su elemento principal durante 10 s.
		REGISTRY.put(Role.ELEMENTALIST, (p, r) -> {
			List<Element> elements = RoleManager.get(p).elements();
			if (elements.isEmpty()) return false;
			Element main = elements.get(0);
			raiseWall(p, wallBlock(main), 10 * 20);
			Vec3d center = p.getPos();
			AbilityState.repeat(p.getServer(), 10, 20, i -> {
				if (!p.isAlive()) return;
				world(p).spawnParticles(main.particle(), center.x, center.y + 0.5, center.z, 80, 4, 0.6, 4, 0.02);
				for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(8),
						e -> Targeting.isHostileTo(p, e))) {
					e.timeUntilRegen = 0;
					e.damage(p.getDamageSources().indirectMagic(p, p), 3.0f);
				}
				if (i % 3 == 0) ElementEffects.burst(p, main, 2);
			});
			sound(p, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 0.8f);
			return true;
		});

		// Ingeniero – Baliza táctica: 1.er uso fija un punto de TP; 2.º uso te teletransporta con blindaje.
		REGISTRY.put(Role.ENGINEER, (p, r) -> {
			Beacon beacon = BEACONS.remove(p.getUuid());
			if (beacon == null || beacon.world() != p.getWorld().getRegistryKey()) {
				BEACONS.put(p.getUuid(), new Beacon(p.getWorld().getRegistryKey(), p.getPos()));
				p.sendMessage(Text.translatable("aptitudes.signature.beacon_set").formatted(Formatting.AQUA), true);
				sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.4f);
				return true;
			}
			burst(p, ParticleTypes.ELECTRIC_SPARK, 40);
			p.requestTeleport(beacon.pos().x, beacon.pos().y, beacon.pos().z);
			p.fallDistance = 0;
			effect(p, StatusEffects.ABSORPTION, 20 * 20, 3);
			effect(p, StatusEffects.RESISTANCE, 20 * 20, 1);
			burst(p, ParticleTypes.ELECTRIC_SPARK, 40);
			sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.8f);
			return true;
		});

		// Mago – Lluvia rúnica: 8 runas arcanas caen sobre el punto señalado (daño mágico, sin fuego ni explosiones).
		REGISTRY.put(Role.MAGE, (p, r) -> {
			Vec3d target = Targeting.lookEnd(p, 40);
			AbilityState.repeat(p.getServer(), 8, 6, i -> {
				double x = target.x + (p.getRandom().nextDouble() - 0.5) * 7;
				double z = target.z + (p.getRandom().nextDouble() - 0.5) * 7;
				Vec3d hit = new Vec3d(x, target.y, z);
				Targeting.particleLine(world(p), ParticleFx.dust(0xB388FF, 1.4f), hit.add(0, 8, 0), hit);
				ParticleFx.runeCircle(world(p), hit, 1.6, ParticleFx.dust(0xE1BEE7, 1.0f));
				world(p).spawnParticles(ParticleTypes.WITCH, x, target.y + 0.5, z, 20, 0.6, 0.4, 0.6, 0.1);
				for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class,
						new net.minecraft.util.math.Box(hit, hit).expand(2.2), e -> Targeting.isHostileTo(p, e))) {
					e.damage(p.getDamageSources().indirectMagic(p, p), 5 + r);
					e.takeKnockback(0.4, x - e.getX(), z - e.getZ());
				}
				world(p).playSound(null, x, target.y, z, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE,
						net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.8f + 0.1f * i);
			});
			return true;
		});

		// Asesino – Marca de muerte: el objetivo recibe el TRIPLE de tu daño durante 10 s.
		REGISTRY.put(Role.ASSASSIN, (p, r) -> {
			LivingEntity target = Targeting.raycastLiving(p, 24);
			if (target == null) return false;
			DEATH_MARKS.put(target.getUuid(), new Object[]{p.getUuid(), p.getServer().getTicks() + 200});
			effect(target, StatusEffects.GLOWING, 200, 0);
			Targeting.burst(world(p), ParticleTypes.SOUL, target, 20, 0.4);
			sound(p, SoundEvents.ENTITY_WITHER_AMBIENT, 1.4f);
			return true;
		});

		// Herbolista – Elixir de vida: purifica, Regeneración III y vida extra a los aliados cercanos.
		REGISTRY.put(Role.HERBALIST, (p, r) -> {
			for (PlayerEntity ally : Targeting.playersAround(p, 10)) {
				List.copyOf(ally.getStatusEffects()).stream().filter(e -> !e.getEffectType().value().isBeneficial())
						.forEach(e -> ally.removeStatusEffect(e.getEffectType()));
				effect(ally, StatusEffects.REGENERATION, 200, 2);
				effect(ally, StatusEffects.HEALTH_BOOST, 600, 1);
				Targeting.burst(world(p), ParticleTypes.HAPPY_VILLAGER, ally, 20, 0.6);
			}
			sound(p, SoundEvents.ENTITY_WITCH_DRINK, 1.2f);
			return true;
		});

		// Lancero – Carga imparable: 3 s de embestidas continuas, inmune a golpes de criaturas.
		REGISTRY.put(Role.LANCER, (p, r) -> {
			AbilityState.setUntouchable(p, 60);
			AbilityState.repeat(p.getServer(), 6, 10, i -> {
				if (!p.isAlive()) return;
				Vec3d facing = Targeting.horizontalFacing(p);
				p.setVelocity(facing.multiply(1.3).add(0, 0.05, 0));
				p.velocityModified = true;
				for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class,
						p.getBoundingBox().stretch(facing.multiply(3)).expand(0.8), e -> Targeting.isHostileTo(p, e))) {
					e.damage(p.getDamageSources().playerAttack(p), 7.0f);
					e.takeKnockback(1.2, -facing.x, -facing.z);
				}
				sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.7f);
			});
			return true;
		});

		// Espadachín – Corte dimensional: tajo de 14 bloques que atraviesa a todos los enemigos (20 de daño).
		REGISTRY.put(Role.SWORDSMAN, (p, r) -> {
			Vec3d start = p.getEyePos();
			Vec3d dir = p.getRotationVec(1.0f);
			Vec3d end = start.add(dir.multiply(14));
			Targeting.particleLine(world(p), ParticleTypes.SWEEP_ATTACK, start, end);
			for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(1.5),
					e -> Targeting.isHostileTo(p, e))) {
				e.damage(p.getDamageSources().playerAttack(p), 20.0f);
			}
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f);
			return true;
		});

		// Cambiaformas – Caos de formas (troll): transforma a los jugadores cercanos en gallinas durante 10 s.
		REGISTRY.put(Role.SHAPESHIFTER, (p, r) -> {
			int count = 0;
			for (PlayerEntity other : Targeting.playersAround(p, 10)) {
				if (other != p && other instanceof ServerPlayerEntity sp) {
					MorphManager.morph(sp, EntityType.CHICKEN, 10 * 20, 0f);
					count++;
				}
			}
			for (MobEntity mob : p.getWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(10),
					m -> Targeting.isHostileTo(p, m))) {
				effect(mob, StatusEffects.LEVITATION, 60, 0);
				count++;
			}
			sound(p, SoundEvents.ENTITY_CHICKEN_AMBIENT, 0.5f);
			return count > 0;
		});

		// Druida y Mercader tienen características PASIVAS (ver RolePassives): Renacer del bosque y Señor del mercado.
	}

	private Signatures() {
	}

	@Nullable
	public static Ability get(Role role) {
		return REGISTRY.get(role);
	}

	/** Roles cuya característica es pasiva (no ocupan la tecla Z). */
	public static boolean isPassive(Role role) {
		return role == Role.DRUID || role == Role.MERCHANT;
	}

	/** Multiplicador de daño por Marca de muerte (×3 si el atacante es quien marcó). */
	public static float markMultiplier(LivingEntity target, ServerPlayerEntity attacker) {
		Object[] mark = DEATH_MARKS.get(target.getUuid());
		if (mark == null) return 1.0f;
		if ((Integer) mark[1] < attacker.getServer().getTicks()) {
			DEATH_MARKS.remove(target.getUuid());
			return 1.0f;
		}
		return attacker.getUuid().equals(mark[0]) ? 3.0f : 1.0f;
	}

	// ------------------------------------------------------- Muro temporal

	private static BlockState wallBlock(Element element) {
		return switch (element) {
			case FIRE, MAGMA -> Blocks.MAGMA_BLOCK.getDefaultState();
			case ICE, WATER -> Blocks.PACKED_ICE.getDefaultState();
			case EARTH -> Blocks.COBBLESTONE.getDefaultState();
			case PLANT -> Blocks.OAK_LEAVES.getDefaultState();
			case SHADOW -> Blocks.BLACKSTONE.getDefaultState();
			case LIGHTNING -> Blocks.COPPER_BLOCK.getDefaultState();
			case WIND -> Blocks.WHITE_STAINED_GLASS.getDefaultState();
		};
	}

	/**
	 * Levanta un muro de 5×3 a 3 bloques delante. Solo ocupa aire y se retira
	 * pasado el tiempo; si el servidor se cierra antes, el muro se queda.
	 */
	private static void raiseWall(ServerPlayerEntity p, BlockState state, int ticks) {
		ServerWorld world = world(p);
		Vec3d facing = Targeting.horizontalFacing(p);
		Vec3d side = new Vec3d(-facing.z, 0, facing.x);
		Vec3d base = p.getPos().add(facing.multiply(3));
		List<BlockPos> placed = new ArrayList<>();
		for (int w = -2; w <= 2; w++) {
			for (int h = 0; h < 3; h++) {
				BlockPos pos = BlockPos.ofFloored(base.add(side.multiply(w)).add(0, h, 0));
				if (world.getBlockState(pos).isAir()) {
					world.setBlockState(pos, state);
					placed.add(pos);
				}
			}
		}
		AbilityState.schedule(p.getServer(), ticks, () -> {
			for (BlockPos pos : placed) {
				if (world.getBlockState(pos).isOf(state.getBlock())) world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		});
	}

	// ----------------------------------------------------------- Utilidades

	private static ServerWorld world(ServerPlayerEntity p) {
		return p.getServerWorld();
	}

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void burst(ServerPlayerEntity p, net.minecraft.particle.ParticleEffect particle, int count) {
		world(p).spawnParticles(particle, p.getX(), p.getBodyY(0.5), p.getZ(), count, 0.6, 0.8, 0.6, 0.05);
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	private static void sound(ServerPlayerEntity p, RegistryEntry<SoundEvent> sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}
}
