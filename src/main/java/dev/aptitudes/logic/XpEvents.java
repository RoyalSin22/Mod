package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.Skill;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fuentes de experiencia y efectos pasivos que dependen de eventos del servidor.
 * Se usan exclusivamente eventos de Fabric API (sin mixins) para maximizar la
 * compatibilidad con otros mods.
 */
public final class XpEvents {
	private static final String BOOSTED_ARROW_TAG = Aptitudes.MOD_ID + ".boosted";
	private static final int CM_PER_10_BLOCKS = 1000;

	/** Último valor de la estadística vanilla SPRINT_ONE_CM ya convertido en XP. */
	private static final Map<UUID, Integer> sprintBaseline = new HashMap<>();
	/** Último valor de la estadística vanilla FISH_CAUGHT ya convertido en XP. */
	private static final Map<UUID, Integer> fishBaseline = new HashMap<>();
	/** Rendimiento decreciente por fuente repetida. */
	private static final XpDiminishing DIMINISHING = new XpDiminishing();

	private XpEvents() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register(XpEvents::onBlockBroken);

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(source.getAttacker() instanceof ServerPlayerEntity player) || entity == player) return;
			AptitudesConfig cfg = Aptitudes.config();
			if (entity instanceof PlayerEntity && !cfg.awardXpForPlayerKills) return;

			Entity direct = source.getSource();
			String victim = net.minecraft.entity.EntityType.getId(entity.getType()).toString();
			if (direct instanceof ProjectileEntity) {
				ProgressManager.addXp(player, Skill.ARCHERY, diminished(player, "archery/" + victim,
						scaled(entity.getMaxHealth(), cfg.archeryXpPerMaxHealth)));
			} else if (direct == player) {
				ProgressManager.addXp(player, Skill.COMBAT, diminished(player, "combat/" + victim,
						scaled(entity.getMaxHealth(), cfg.combatXpPerMaxHealth)));
			}
		});

		// Defensa: solo el daño REALMENTE recibido. AFTER_DAMAGE no se dispara si el golpe se ignora por los
		// ticks de invulnerabilidad, y "blocked" indica que lo paró el escudo (antes daba XP igualmente).
		// Se usa el daño base (antes de armadura) para no penalizar a quien lleva buena armadura.
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (blocked || baseDamageTaken <= 0) return;
			if (entity instanceof ServerPlayerEntity player
					&& source.getAttacker() instanceof LivingEntity attacker && attacker != player) {
				ProgressManager.addXp(player, Skill.DEFENSE, diminished(player,
						"defense/" + net.minecraft.entity.EntityType.getId(attacker.getType()).toString(),
						scaled(baseDamageTaken, Aptitudes.config().defenseXpPerDamage)));
			}
		});

		// Tiro con arco: aumenta el daño base de la flecha al aparecer en el mundo.
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(entity instanceof PersistentProjectileEntity arrow)) return;
			if (!(arrow.getOwner() instanceof ServerPlayerEntity player)) return;
			// La etiqueta se guarda en NBT: evita multiplicar de nuevo al recargar el chunk.
			if (arrow.getCommandTags().contains(BOOSTED_ARROW_TAG)) return;
			arrow.addCommandTag(BOOSTED_ARROW_TAG);
			SkillMilestones.onArrowFired(player, arrow);
			int level = ProgressManager.get(player).getLevel(Skill.ARCHERY);
			if (level > 0) {
				arrow.setDamage(arrow.getDamage() * (1.0 + level * Aptitudes.config().archeryDamagePerLevel));
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(XpEvents::tickAgility);
		ServerTickEvents.END_SERVER_TICK.register(XpEvents::tickFishing);
	}

	public static void forget(ServerPlayerEntity player) {
		sprintBaseline.remove(player.getUuid());
		fishBaseline.remove(player.getUuid());
		DIMINISHING.forget(player.getUuid());
	}

	/** XP reducida si la misma fuente se repite demasiado en la ventana (ver {@link XpDiminishing}). */
	static int diminished(ServerPlayerEntity player, String sourceKey, int amount) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.xpDiminishingEnabled || amount <= 0) return amount;
		long now = player.getServer() != null ? player.getServer().getTicks() : 0L;
		return DIMINISHING.scale(player.getUuid(), sourceKey, amount, now,
				cfg.xpDiminishingWindowSeconds * 20L, cfg.xpDiminishingThreshold, cfg.xpDiminishingFloor);
	}

	/**
	 * Cada segundo convierte los peces capturados (estadística vanilla FISH_CAUGHT) en XP de Pesca.
	 * Sin mixins: compatible con otros mods de pesca. La basura y los tesoros no suben la estadística.
	 */
	private static void tickFishing(MinecraftServer server) {
		if (server.getTicks() % 20 != 10) return; // desfasado de la agilidad
		int xpPerCatch = Aptitudes.config().fishingXpCatch;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			int caught = player.getStatHandler().getStat(Stats.CUSTOM, Stats.FISH_CAUGHT);
			Integer baseline = fishBaseline.put(player.getUuid(), caught);
			if (baseline == null || caught <= baseline) continue; // primera lectura o estadística reseteada
			int fish = Math.min(caught - baseline, 16); // tope defensivo por segundo
			for (int i = 0; i < fish; i++) {
				ProgressManager.addXp(player, Skill.FISHING, diminished(player, "fishing", xpPerCatch));
				SkillMilestones.onFishCaught(player);
			}
		}
	}

	private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos, BlockState state,
									  BlockEntity blockEntity) {
		if (!(player instanceof ServerPlayerEntity sp)) return;
		SkillMilestones.onBlockBroken(sp, world, pos, state, blockEntity);
		SoulTools.onBlockBroken(sp, world, pos, state);
		AptitudesConfig cfg = Aptitudes.config();
		ItemStack tool = sp.getMainHandStack();

		if (state.getBlock() instanceof CropBlock crop) {
			if (!crop.isMature(state)) return;
			ProgressManager.addXp(sp, Skill.FARMING, cfg.farmingXpCrop);
			double chance = ProgressManager.get(sp).getLevel(Skill.FARMING) * cfg.farmingExtraDropChancePerLevel;
			if (world.random.nextDouble() < chance) {
				Block.dropStacks(state, world, pos, blockEntity, sp, tool);
			}
			return;
		}

		if (state.isIn(BlockTags.LOGS)) {
			ProgressManager.addXp(sp, Skill.WOODCUTTING, cfg.woodcuttingXpLog);
		} else if (state.isIn(ConventionalBlockTags.ORES)) {
			// Con toque de seda el mineral podría recolocarse y romperse en bucle.
			if (!hasSilkTouch(world, tool)) ProgressManager.addXp(sp, Skill.MINING, cfg.miningXpOre);
		} else if (state.isIn(BlockTags.BASE_STONE_OVERWORLD) || state.isIn(BlockTags.BASE_STONE_NETHER)) {
			if (!hasSilkTouch(world, tool)) ProgressManager.addXp(sp, Skill.MINING, cfg.miningXpStone);
		} else if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
			ProgressManager.addXp(sp, Skill.EXCAVATION, cfg.excavationXpBlock);
		}
	}

	private static boolean hasSilkTouch(World world, ItemStack tool) {
		if (tool.isEmpty()) return false;
		return world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
				.getEntry(Enchantments.SILK_TOUCH)
				.map(entry -> EnchantmentHelper.getLevel(entry, tool) > 0)
				.orElse(false);
	}

	/** Cada segundo convierte la distancia esprintada (estadística vanilla) en XP de agilidad. */
	private static void tickAgility(MinecraftServer server) {
		if (server.getTicks() % 20 != 0) return;
		int xpPerChunk = Aptitudes.config().agilityXpPer10Blocks;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			int sprinted = player.getStatHandler().getStat(Stats.CUSTOM, Stats.SPRINT_ONE_CM);
			Integer baseline = sprintBaseline.putIfAbsent(player.getUuid(), sprinted);
			if (baseline == null) continue; // primera lectura: solo fija la referencia
			int chunks = (sprinted - baseline) / CM_PER_10_BLOCKS;
			if (chunks > 0) {
				sprintBaseline.put(player.getUuid(), baseline + chunks * CM_PER_10_BLOCKS);
				ProgressManager.addXp(player, Skill.AGILITY, chunks * xpPerChunk);
			} else if (sprinted < baseline) {
				sprintBaseline.put(player.getUuid(), sprinted); // estadísticas reseteadas
			}
		}
	}

	private static int scaled(double base, double factor) {
		return (int) Math.max(1, Math.round(base * factor));
	}
}
