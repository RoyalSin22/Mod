package dev.aptitudes.souls;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.network.OpenBonfirePayload;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Progresión estilo Souls:
 * <ul>
 *   <li><b>Hoguera</b>: descansar cura, recarga el maná, fija el punto de reaparición
 *       y permite comprar niveles de alma con XP vanilla (cada nivel = 1 punto de atributo).</li>
 *   <li><b>Mancha de XP</b>: al morir, toda la XP queda en el lugar de la muerte. Si
 *       vuelves la recuperas; si mueres antes, la mancha anterior se pierde.</li>
 * </ul>
 */
public final class SoulsManager {
	private record Bonfire(RegistryKey<World> world, BlockPos pos) {}
	/** Última Hoguera en la que descansó cada jugador (para validar la subida de nivel). */
	private static final Map<UUID, Bonfire> RESTED = new HashMap<>();
	private static final double BONFIRE_RANGE_SQ = 8 * 8;
	private static final double PICKUP_RANGE_SQ = 2.5 * 2.5;

	private SoulsManager() {
	}

	public static SoulsData data(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.SOULS);
	}

	/**
	 * Niveles de alma que hay que comprar, desde {@code soulLevel}, para ganar
	 * {@code points} puntos nuevos (1 punto cada {@code soulLevelsPerPoint} niveles).
	 */
	public static int levelsForPoints(int soulLevel, int points) {
		return SoulsMath.levelsForPoints(soulLevel, points, Aptitudes.config().soulLevelsPerPoint);
	}

	/** Coste en niveles de XP de pasar del nivel de alma {@code level} al siguiente. */
	public static int levelCost(long level) {
		AptitudesConfig c = Aptitudes.config();
		return SoulsMath.levelCost(level, c.levelCostBase, c.levelCostDivisor, c.levelCostMax);
	}

	/**
	 * Debe registrarse DESPUÉS del resto de ALLOW_DEATH (los que evitan la muerte),
	 * para que la XP solo se deje en el suelo si la muerte es real.
	 */
	public static void register() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity player) onDeath(player);
			return true;
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			SoulsData stain = data(newPlayer);
			if (!alive && stain.hasStain()) {
				newPlayer.sendMessage(Text.translatable("aptitudes.souls.stain_left", stain.xp,
						(int) stain.x, (int) stain.y, (int) stain.z).formatted(Formatting.DARK_RED), false);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) tickStains(server);
		});
	}

	// ----------------------------------------------------------- Muerte

	private static void onDeath(ServerPlayerEntity player) {
		if (!Aptitudes.config().soulsBloodstain || player.isSpectator()) return;
		if (player.getServerWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;
		// Un tótem en la mano todavía puede salvarle: no tocamos su XP.
		if (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
		int xp = player.totalExperience;
		SoulsData stain = data(player);
		// F3: la mancha anterior no se pierde entera; al volver a morir antes de recuperarla pierde un 25 % y el resto
		// se suma a la nueva mancha, que queda donde ha caído el jugador.
		long kept = stain.hasStain() ? SoulStainRules.afterDeathWhileStained(stain.xp) : 0L;
		long lost = stain.hasStain() ? stain.xp - kept : 0L;
		if (lost > 0) {
			player.sendMessage(Text.translatable("aptitudes.souls.stain_lost", lost).formatted(Formatting.GRAY), false);
		}
		stain.clear();
		long total = Math.min(Integer.MAX_VALUE, (long) xp + kept);
		if (total > 0) {
			stain.world = player.getWorld().getRegistryKey().getValue().toString();
			stain.x = player.getX();
			stain.y = player.getY();
			stain.z = player.getZ();
			stain.xp = (int) total;
		}
		// Se vacía ANTES de que vanilla suelte orbes: toda la XP queda en la mancha.
		player.experienceLevel = 0;
		player.experienceProgress = 0;
		player.totalExperience = 0;
	}

	private static void tickStains(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			SoulsData stain = data(player);
			if (!stain.hasStain() || !player.isAlive()) continue;
			if (!player.getWorld().getRegistryKey().getValue().toString().equals(stain.world)) continue;
			// Solo el dueño ve su mancha.
			player.getServerWorld().spawnParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true,
					stain.x, stain.y + 0.3, stain.z, 8, 0.2, 0.3, 0.2, 0.01);
			if (player.squaredDistanceTo(stain.x, stain.y, stain.z) <= PICKUP_RANGE_SQ) {
				int xp = stain.xp;
				stain.clear();
				player.addExperience(xp);
				player.sendMessage(Text.translatable("aptitudes.souls.recovered", xp).formatted(Formatting.GOLD), true);
				player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 1.0f, 1.2f);
			}
		}
	}

	// ----------------------------------------------------------- Hoguera

	public static void rest(ServerPlayerEntity player, BlockPos pos) {
		RESTED.put(player.getUuid(), new Bonfire(player.getWorld().getRegistryKey(), pos.toImmutable()));
		player.setHealth(player.getMaxHealth());
		player.extinguish();
		// Frascos de Estus y Éter: se rellenan todas las cargas.
		for (int i = 0; i < player.getInventory().size(); i++) {
			net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
			if (stack.getItem() instanceof dev.aptitudes.item.SoulFlaskItem) dev.aptitudes.item.SoulFlaskItem.refill(stack);
		}
		player.getHungerManager().add(20, 20.0f);
		RoleData roles = RoleManager.get(player);
		roles.setEnergy(AbilityManager.maxEnergy(player));
		AbilityManager.sendEnergy(player, true);
		player.setSpawnPoint(player.getWorld().getRegistryKey(), pos.up(), player.getYaw(), true, false);
		player.getServerWorld().spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
				30, 0.3, 0.5, 0.3, 0.02);
		player.getWorld().playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.8f, 0.7f);
		player.sendMessage(Text.translatable("aptitudes.souls.rested").formatted(Formatting.GOLD), true);
		dev.aptitudes.achievement.Achievements.grant(player, dev.aptitudes.achievement.Achievement.REST_BONFIRE); // v31
		ProgressManager.sync(player);
		if (ServerPlayNetworking.canSend(player, OpenBonfirePayload.ID)) {
			ServerPlayNetworking.send(player, new OpenBonfirePayload());
		}
	}

	private static boolean nearBonfire(ServerPlayerEntity player) {
		Bonfire bonfire = RESTED.get(player.getUuid());
		return bonfire != null && bonfire.world() == player.getWorld().getRegistryKey()
				&& player.getBlockPos().getSquaredDistance(bonfire.pos()) <= BONFIRE_RANGE_SQ;
	}

	public static boolean canTemperAt(ServerPlayerEntity player, BlockPos pos) {
		Bonfire bonfire = RESTED.get(player.getUuid());
		return bonfire != null && bonfire.pos().equals(pos) && nearBonfire(player);
	}

	public static void openTempering(ServerPlayerEntity player) {
		Bonfire bonfire = RESTED.get(player.getUuid());
		if (bonfire == null || !dev.aptitudes.forge.ForgeScreenHandler.open(player, player.getServerWorld(),
				bonfire.pos(), dev.aptitudes.forge.ForgeScreenHandler.Mode.TEMPER)) {
			player.sendMessage(Text.translatable("forge.aptitudes.temper_unavailable"), true);
		}
	}

	public static void openFlaskUpgrades(ServerPlayerEntity player) {
		Bonfire bonfire = RESTED.get(player.getUuid());
		if (bonfire == null || !FlaskScreenHandler.open(player, bonfire.pos())) {
			player.sendMessage(Text.translatable("flask.aptitudes.unavailable"), true);
		}
	}

	/**
	 * Asigna los puntos pedidos. Los que excedan los puntos libres se compran como
	 * niveles de alma, pagando XP vanilla. Todo o nada.
	 */
	public static void levelUp(ServerPlayerEntity player, int[] requested) {
		PlayerAttribute[] attrs = PlayerAttribute.values();
		if (requested.length != attrs.length) return;
		if (!nearBonfire(player)) {
			player.sendMessage(Text.translatable("aptitudes.souls.need_bonfire").formatted(Formatting.RED), true);
			return;
		}
		PlayerProgress progress = ProgressManager.get(player);
		int max = Aptitudes.config().maxPointsPerAttribute;
		int[] allocated = new int[attrs.length];
		for (PlayerAttribute a : attrs) allocated[a.ordinal()] = progress.getAllocated(a);
		// Validación con long (ver SoulsMath): evita el desbordamiento con valores cercanos a Integer.MAX_VALUE.
		long total = SoulsMath.validRequestTotal(requested, allocated, max);
		if (total < 0) return;

		int free = Math.max(0, ProgressManager.availablePoints(progress));
		int toBuy = levelsForPoints(progress.getSoulLevel(), (int) Math.max(0, total - free)); // total ≤ 1000
		int cost = 0;
		for (int i = 0; i < toBuy; i++) cost += levelCost(progress.getSoulLevel() + i);
		if (!player.isCreative() && player.experienceLevel < cost) {
			player.sendMessage(Text.translatable("aptitudes.souls.not_enough", cost, player.experienceLevel)
					.formatted(Formatting.RED), true);
			return;
		}
		if (!player.isCreative() && cost > 0) player.addExperienceLevels(-cost);
		progress.setSoulLevel(progress.getSoulLevel() + toBuy);
		for (PlayerAttribute a : attrs) {
			progress.setAllocated(a, progress.getAllocated(a) + requested[a.ordinal()]);
		}
		ProgressManager.refresh(player);
		if (progress.getSoulLevel() >= dev.aptitudes.achievement.Achievement.SOUL_LEVEL_TARGET) {
			dev.aptitudes.achievement.Achievements.grant(player, dev.aptitudes.achievement.Achievement.SOUL_LEVEL); // v31
		}
		if (toBuy > 0) {
			dev.aptitudes.tutorial.TutorialManager.onStep(player, dev.aptitudes.tutorial.TutorialRules.Step.BONFIRE);
			player.sendMessage(Text.translatable("aptitudes.souls.leveled", progress.getSoulLevel(), cost)
					.formatted(Formatting.GOLD), false);
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 0.8f);
		}
	}

	public static void forget(ServerPlayerEntity player) {
		RESTED.remove(player.getUuid());
	}
}
