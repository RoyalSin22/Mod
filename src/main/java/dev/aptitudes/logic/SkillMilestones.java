package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.Skill;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hitos de las habilidades por uso: a los niveles 10, 20, 30, 40 y 50 cada
 * habilidad desbloquea una ventaja. Los textos están en {@code milestone.aptitudes.<habilidad>.<nivel>}.
 */
public final class SkillMilestones {
	public static final int[] THRESHOLDS = {10, 20, 30, 40, 50};
	private static final Map<UUID, Integer> SECOND_CHANCE_READY = new HashMap<>();
	private static final Map<UUID, Integer> LAST_ABSORPTION = new HashMap<>();

	private SkillMilestones() {
	}

	public static String key(Skill skill, int threshold) {
		return "milestone." + Aptitudes.MOD_ID + "." + skill.key() + "." + threshold;
	}

	/** Mayor hito cruzado al pasar de {@code oldLevel} a {@code newLevel} (0 si ninguno). */
	public static int crossed(int oldLevel, int newLevel) {
		int result = 0;
		for (int t : THRESHOLDS) if (oldLevel < t && newLevel >= t) result = t;
		return result;
	}

	public static boolean has(ServerPlayerEntity player, Skill skill, int threshold) {
		return ProgressManager.get(player).getLevel(skill) >= threshold;
	}

	// ------------------------------------------------------------ Atributos

	public static void applyAttributes(ServerPlayerEntity player) {
		PlayerProgress p = ProgressManager.get(player);
		int combat = p.getLevel(Skill.COMBAT);
		double damage = (combat >= 10 ? 1 : 0) + (combat >= 40 ? 2 : 0);
		AttributeApplier.set(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, Aptitudes.id("milestone/combat"), damage,
				Operation.ADD_VALUE);
		AttributeApplier.set(player, EntityAttributes.GENERIC_STEP_HEIGHT, Aptitudes.id("milestone/agility"),
				p.getLevel(Skill.AGILITY) >= 40 ? 0.5 : 0, Operation.ADD_VALUE);
	}

	// ---------------------------------------------------------------- Tick

	/** Cada segundo: ventajas basadas en efectos y el sentido mineral. */
	public static void tick(MinecraftServer server) {
		int now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (!p.isAlive() || p.isSpectator()) continue;
			PlayerProgress prog = ProgressManager.get(p);
			ItemStack hand = p.getMainHandStack();
			int mining = prog.getLevel(Skill.MINING);
			int excavation = prog.getLevel(Skill.EXCAVATION);
			int woodcutting = prog.getLevel(Skill.WOODCUTTING);
			int farming = prog.getLevel(Skill.FARMING);
			int combat = prog.getLevel(Skill.COMBAT);
			int defense = prog.getLevel(Skill.DEFENSE);
			int agility = prog.getLevel(Skill.AGILITY);
			int fishing = prog.getLevel(Skill.FISHING);
			boolean lowHealth = p.getHealth() < p.getMaxHealth() * 0.3f;

			if (mining >= 10 && p.getBlockY() < 40) quiet(p, StatusEffects.NIGHT_VISION, 260, 0);
			if (hand.getItem() instanceof PickaxeItem && mining >= 20) quiet(p, StatusEffects.HASTE, 40, mining >= 50 ? 1 : 0);
			if (hand.getItem() instanceof ShovelItem && excavation >= 10) quiet(p, StatusEffects.HASTE, 40, excavation >= 40 ? 1 : 0);
			if (hand.getItem() instanceof AxeItem && woodcutting >= 10) quiet(p, StatusEffects.HASTE, 40, woodcutting >= 40 ? 1 : 0);
			if (mining >= 40 && hand.getItem() instanceof PickaxeItem && now % 60 == 0) oreSense(p);

			if (farming >= 10 && now % 1200 == 0) quiet(p, StatusEffects.SATURATION, 2, 0);
			if (farming >= 30 && now % 100 == 0) dev.aptitudes.ability.RolePassives.growAround(p, 4, 2);
			if (farming >= 50 && (p.getWorld().getBlockState(p.getBlockPos()).isOf(net.minecraft.block.Blocks.FARMLAND)
					|| p.getWorld().getBlockState(p.getBlockPos().down()).isOf(net.minecraft.block.Blocks.FARMLAND))) {
				quiet(p, StatusEffects.REGENERATION, 40, 0);
			}
			if (combat >= 30 && lowHealth) quiet(p, StatusEffects.STRENGTH, 40, 0);
			if (defense >= 40 && lowHealth) quiet(p, StatusEffects.RESISTANCE, 40, 0);
			if (defense >= 20 && now - LAST_ABSORPTION.getOrDefault(p.getUuid(), -100_000) >= 1200
					&& !p.hasStatusEffect(StatusEffects.ABSORPTION)) {
				LAST_ABSORPTION.put(p.getUuid(), now);
				p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 1200, 0, true, false));
			}
			if (agility >= 10) quiet(p, StatusEffects.JUMP_BOOST, 40, 0);
			if (agility >= 30 && p.isSprinting()) quiet(p, StatusEffects.SPEED, 40, 0);
			if (fishing >= 20 && p.isSwimming()) quiet(p, StatusEffects.DOLPHINS_GRACE, 40, 0);
			if (fishing >= 40 && p.isTouchingWater()) quiet(p, StatusEffects.CONDUIT_POWER, 260, 0);
		}
	}

	// ------------------------------------------------------------- Pesca

	/**
	 * Llamado por cada pez capturado (ver XpEvents#tickFishing). Captura doble por nivel,
	 * XP vanilla extra (10), tesoro hundido (30) y Señor de las mareas (50).
	 */
	public static void onFishCaught(ServerPlayerEntity p) {
		PlayerProgress prog = ProgressManager.get(p);
		int fishing = prog.getLevel(Skill.FISHING);
		if (fishing <= 0) return;
		World world = p.getWorld();
		double doubleChance = Math.min(1.0, fishing * Aptitudes.config().fishingDoubleCatchPerLevel);
		if (world.random.nextDouble() < doubleChance) give(p, randomFish(world));
		if (fishing >= 10) p.addExperience(2 + world.random.nextInt(3));
		if (fishing >= 30) {
			float chance = fishing >= 50 ? 0.10f : 0.05f;
			if (world.random.nextFloat() < chance) {
				give(p, seaTreasure(world, fishing >= 50));
				world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP,
						SoundCategory.PLAYERS, 0.5f, 1.8f);
			}
		}
	}

	/** Distribución aproximada de la pesca vanilla de peces (60/25/13/2). */
	private static ItemStack randomFish(World world) {
		int roll = world.random.nextInt(100);
		if (roll < 60) return new ItemStack(Items.COD);
		if (roll < 85) return new ItemStack(Items.SALMON);
		if (roll < 98) return new ItemStack(Items.TROPICAL_FISH);
		return new ItemStack(Items.PUFFERFISH);
	}

	private static ItemStack seaTreasure(World world, boolean lord) {
		int roll = world.random.nextInt(100);
		if (lord && roll < 3) return new ItemStack(Items.HEART_OF_THE_SEA);
		if (roll < 20) return new ItemStack(Items.NAUTILUS_SHELL);
		if (roll < 35) return new ItemStack(Items.NAME_TAG);
		if (roll < 50) return new ItemStack(Items.SADDLE);
		if (roll < 70) return new ItemStack(Items.PRISMARINE_CRYSTALS, 2 + world.random.nextInt(3));
		return new ItemStack(Items.EMERALD);
	}

	private static void give(ServerPlayerEntity p, ItemStack stack) {
		p.getInventory().offerOrDrop(stack);
	}

	/** Minería 40: resalta (solo para el jugador) los minerales en 5 bloques. */
	private static void oreSense(ServerPlayerEntity p) {
		ServerWorld world = p.getServerWorld();
		BlockPos center = p.getBlockPos();
		int shown = 0;
		for (BlockPos pos : BlockPos.iterate(center.add(-5, -5, -5), center.add(5, 5, 5))) {
			if (world.getBlockState(pos).isIn(ConventionalBlockTags.ORES)) {
				world.spawnParticles(p, ParticleTypes.WAX_ON, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						3, 0.3, 0.3, 0.3, 0);
				if (++shown >= 24) break;
			}
		}
	}

	// ---------------------------------------------------------- Bloques

	public static void onBlockBroken(ServerPlayerEntity p, World world, BlockPos pos, BlockState state, BlockEntity be) {
		PlayerProgress prog = ProgressManager.get(p);
		ItemStack tool = p.getMainHandStack();
		if (state.isIn(ConventionalBlockTags.ORES) && prog.getLevel(Skill.MINING) >= 30 && world.random.nextFloat() < 0.10f) {
			Block.dropStacks(state, world, pos, be, p, tool);
		}
		if (state.isIn(BlockTags.SHOVEL_MINEABLE) && prog.getLevel(Skill.EXCAVATION) >= 20) {
			float chance = prog.getLevel(Skill.EXCAVATION) >= 50 ? 0.06f : 0.03f;
			if (world.random.nextFloat() < chance) Block.dropStack(world, pos, treasure(world, prog.getLevel(Skill.EXCAVATION) >= 50));
		}
		if (state.isIn(BlockTags.LOGS)) {
			int wood = prog.getLevel(Skill.WOODCUTTING);
			if (wood >= 20 && world.random.nextFloat() < 0.10f) {
				Block.dropStack(world, pos, new ItemStack(world.random.nextBoolean() ? Items.APPLE : Items.OAK_SAPLING));
			}
			if (wood >= 30 && p.isSneaking() && tool.getItem() instanceof AxeItem) fellTree(p, world, pos, wood >= 50 ? 96 : 32);
		}
		if (state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
			int farming = prog.getLevel(Skill.FARMING);
			if (farming >= 40 && world.random.nextFloat() < 0.10f) {
				Block.dropStacks(state, world, pos, be, p, tool);
				Block.dropStacks(state, world, pos, be, p, tool);
			}
			if (farming >= 20) replant(p, world, pos, state);
		}
	}

	private static ItemStack treasure(World world, boolean rich) {
		int roll = world.random.nextInt(100);
		if (rich && roll < 5) return new ItemStack(Items.DIAMOND);
		if (roll < 20) return new ItemStack(Items.EMERALD);
		if (roll < 55) return new ItemStack(Items.GOLD_NUGGET, 2 + world.random.nextInt(4));
		return new ItemStack(Items.IRON_NUGGET, 2 + world.random.nextInt(5));
	}

	/** Tala el árbol entero: troncos conectados (también en diagonal), hasta {@code max}. */
	static void fellTree(ServerPlayerEntity p, World world, BlockPos origin, int max) {
		Deque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		queue.add(origin);
		seen.add(origin);
		int broken = 0;
		while (!queue.isEmpty() && broken < max) {
			BlockPos current = queue.poll();
			for (BlockPos next : BlockPos.iterate(current.add(-1, 0, -1), current.add(1, 1, 1))) {
				BlockPos n = next.toImmutable();
				if (!seen.add(n) || !world.getBlockState(n).isIn(BlockTags.LOGS)) continue;
				ItemStack tool = p.getMainHandStack();
				if (!(tool.getItem() instanceof AxeItem)) return;
				world.breakBlock(n, true, p);
				tool.damage(1, p, EquipmentSlot.MAINHAND);
				queue.add(n);
				if (++broken >= max) break;
			}
		}
		if (broken > 0) {
			world.playSound(null, origin, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0f, 0.6f);
		}
	}

	/** Replanta el cultivo cosechado gastando una semilla del inventario. */
	private static void replant(ServerPlayerEntity p, World world, BlockPos pos, BlockState state) {
		if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.down()).isOf(net.minecraft.block.Blocks.FARMLAND)) return;
		ItemStack seed = state.getBlock().getPickStack(world, pos, state);
		if (seed.isEmpty()) return;
		if (!p.isCreative()) {
			int slot = p.getInventory().getSlotWithStack(seed);
			if (slot < 0) return;
			p.getInventory().getStack(slot).decrement(1);
		}
		world.setBlockState(pos, state.getBlock().getDefaultState());
	}

	// ------------------------------------------------------------- Daño

	/** @return false para anular el daño (Agilidad 50: inmune a caídas). */
	public static boolean allowIncomingDamage(ServerPlayerEntity p, DamageSource source) {
		return !(source.isOf(DamageTypes.FALL) && has(p, Skill.AGILITY, 50));
	}

	/** Reducciones y espinas de los hitos. */
	public static float modifyIncoming(ServerPlayerEntity p, DamageSource source, float amount) {
		PlayerProgress prog = ProgressManager.get(p);
		if (source.isOf(DamageTypes.FALL)) {
			if (prog.getLevel(Skill.EXCAVATION) >= 30) amount *= 0.5f;
			if (prog.getLevel(Skill.AGILITY) >= 20) amount *= 0.5f;
		}
		if (source.isIn(DamageTypeTags.IS_FIRE) && prog.getLevel(Skill.DEFENSE) >= 10) amount *= 0.8f;
		if (prog.getLevel(Skill.DEFENSE) >= 30 && source.getAttacker() instanceof LivingEntity attacker
				&& attacker != p && source.getSource() == attacker) {
			attacker.damage(p.getDamageSources().thorns(p), amount * 0.10f);
		}
		return amount;
	}

	/** Golpes del jugador: Combate 20 (los críticos curan) y efectos de flecha de Tiro con arco. */
	public static void onOutgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source) {
		PlayerProgress prog = ProgressManager.get(p);
		boolean melee = source.getSource() == p;
		boolean vanillaCrit = melee && p.fallDistance > 0 && !p.isOnGround() && !p.isClimbing() && !p.isTouchingWater();
		if (vanillaCrit && prog.getLevel(Skill.COMBAT) >= 20) p.heal(2.0f);
		if (source.getSource() instanceof PersistentProjectileEntity) {
			int archery = prog.getLevel(Skill.ARCHERY);
			if (archery >= 10) target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
			if (archery >= 40) target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
		}
	}

	/** Tiro con arco al disparar: 20 = 25 % de recuperar la flecha; 30 = +30 % de velocidad; 50 = atraviesa. */
	public static void onArrowFired(ServerPlayerEntity p, PersistentProjectileEntity arrow) {
		int archery = ProgressManager.get(p).getLevel(Skill.ARCHERY);
		if (archery >= 20 && !p.isCreative() && p.getRandom().nextFloat() < 0.25f) {
			p.getInventory().insertStack(new ItemStack(Items.ARROW));
		}
		if (archery >= 30) arrow.setVelocity(arrow.getVelocity().multiply(1.3));
		if (archery >= 50) {
			((dev.aptitudes.mixin.PersistentProjectileAccessor) arrow).aptitudes$setPierceLevel((byte) 2);
		}
	}

	/** Combate 50: cada muerte da Velocidad y Regeneración breves. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		if (victim instanceof Monster && has(p, Skill.COMBAT, 50)) {
			quiet(p, StatusEffects.SPEED, 80, 0);
			quiet(p, StatusEffects.REGENERATION, 80, 0);
		}
	}

	/** Defensa 50 – Segunda oportunidad: una vez cada 10 min sobrevives con 2 corazones. @return true si salvó. */
	public static boolean trySecondChance(ServerPlayerEntity p) {
		if (!has(p, Skill.DEFENSE, 50)) return false;
		int now = p.getServer().getTicks();
		Integer ready = SECOND_CHANCE_READY.get(p.getUuid());
		if (ready != null && ready > now) return false;
		SECOND_CHANCE_READY.put(p.getUuid(), now + 10 * 60 * 20);
		p.setHealth(4.0f);
		p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 2));
		p.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getBodyY(0.5), p.getZ(), 40, 0.5, 0.8, 0.5, 0.3);
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 0.5f);
		p.sendMessage(Text.translatable("aptitudes.milestone.second_chance").formatted(Formatting.GOLD), true);
		return true;
	}

	public static void forget(ServerPlayerEntity p) {
		LAST_ABSORPTION.remove(p.getUuid());
	}

	private static void quiet(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
	}
}
