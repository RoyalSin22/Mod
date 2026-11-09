package dev.aptitudes.event;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.event.ExpeditionState.Expedition;
import dev.aptitudes.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Expediciones de Amenaza (F5). Un jugador usa la ficha de expedición en el Overworld: se fija el punto de inicio y la
 * lista de módulos (semilla del día). Cada módulo se completa con una acción dentro de la zona; al terminar todos, hay
 * recompensa. Caducan a los 15 minutos. Reglas puras en {@link ExpeditionRules} y {@link ExpeditionProgressRules}.
 *
 * <p>Solo cuenta el progreso del propio dueño (las expediciones de grupo quedan para más adelante).</p>
 */
public final class Expeditions {
	/** Ficha que inicia la expedición. */
	public static final Item TOKEN = Registry.register(Registries.ITEM, Aptitudes.id("expedition_token"),
			new Token(new Item.Settings().maxCount(1)));

	/** Dificultad fija del MVP: 4 módulos. */
	private static final int DIFFICULTY = 1;

	private Expeditions() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> onKill(entity, source));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) tick(server);
		});
	}

	/** Ficha de expedición: al usarla en el Overworld inicia una expedición. */
	public static final class Token extends Item {
		Token(Settings settings) {
			super(settings);
		}

		@Override
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
			ItemStack stack = user.getStackInHand(hand);
			if (!world.isClient() && user instanceof ServerPlayerEntity player && start(player) && !player.isCreative()) {
				stack.decrement(1);
			}
			return TypedActionResult.success(stack, world.isClient());
		}
	}

	// ----------------------------------------------------------- Inicio

	/** Inicia la expedición del jugador en su posición. Devuelve {@code false} si no se pudo. */
	public static boolean start(ServerPlayerEntity player) {
		ServerWorld overworld = player.getServer().getOverworld();
		if (player.getWorld() != overworld) {
			fail(player, "expedition.aptitudes.overworld_only");
			return false;
		}
		ExpeditionState state = ExpeditionState.get(overworld);
		if (state.get(player.getUuid()) != null) {
			fail(player, "expedition.aptitudes.already");
			return false;
		}
		long day = Math.max(0L, overworld.getTimeOfDay() / 24_000L);
		long seed = overworld.getSeed() ^ day;
		List<ExpeditionRules.Module> modules = ExpeditionRules.compose(seed, DIFFICULTY);
		Expedition expedition = new Expedition(player.getUuid(), player.getX(), player.getY(), player.getZ(), modules,
				overworld.getTime());
		state.put(expedition);
		player.sendMessage(Text.translatable("expedition.aptitudes.started", modules.size(), moduleName(modules.get(0)))
				.formatted(Formatting.GOLD), true);
		return true;
	}

	// ----------------------------------------------------------- Progreso

	private static void onKill(LivingEntity entity, DamageSource source) {
		if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;
		if (!(player.getWorld() instanceof ServerWorld world)) return;
		ExpeditionState state = ExpeditionState.get(world.getServer().getOverworld());
		Expedition e = state.get(player.getUuid());
		if (e == null || !ExpeditionProgressRules.inZone(entity.squaredDistanceTo(e.x, e.y, e.z))) return;
		ExpeditionRules.Module current = e.current();
		if (current == ExpeditionRules.Module.COMBAT && entity instanceof Monster) {
			advance(player, e, state);
		} else if (current == ExpeditionRules.Module.ELITE && entity instanceof MobEntity mob && Elites.isElite(mob)) {
			advance(player, e, state);
		} else if (current == ExpeditionRules.Module.BOSS && isBoss(entity)) {
			advance(player, e, state);
		}
	}

	/** Lo llama {@link dev.aptitudes.contract.ContractManager}: sobrevivir a un evento del mundo cuenta en la zona. */
	public static void onEventSurvived(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world)) return;
		ExpeditionState state = ExpeditionState.get(world.getServer().getOverworld());
		Expedition e = state.get(player.getUuid());
		if (e == null || e.current() != ExpeditionRules.Module.EVENT) return;
		if (!ExpeditionProgressRules.inZone(player.squaredDistanceTo(e.x, e.y, e.z))) return;
		advance(player, e, state);
	}

	private static void advance(ServerPlayerEntity player, Expedition e, ExpeditionState state) {
		e.progress++;
		if (e.progress >= ExpeditionProgressRules.goal(e.current())) {
			nextModule(player, e, state);
		} else {
			state.markDirty();
		}
	}

	private static void nextModule(ServerPlayerEntity player, Expedition e, ExpeditionState state) {
		e.index++;
		e.progress = 0;
		if (e.index >= e.modules.size()) {
			state.remove(e.owner);
			reward(player);
			player.sendMessage(Text.translatable("expedition.aptitudes.complete", ExpeditionProgressRules.REWARD_EMERALDS,
					ExpeditionProgressRules.REWARD_SOUL_SHARDS).formatted(Formatting.GREEN), false);
			return;
		}
		state.markDirty();
		player.sendMessage(Text.translatable("expedition.aptitudes.module_next", e.index + 1, e.modules.size(),
				moduleName(e.current())).formatted(Formatting.GOLD), true);
	}

	/** Cada 20 ticks: caduca las expediciones y cuenta los fragmentos de alma recogidos en la zona (módulo de puzle). */
	private static void tick(MinecraftServer server) {
		ExpeditionState state = ExpeditionState.get(server.getOverworld());
		long now = server.getOverworld().getTime();
		List<Expedition> snapshot = new ArrayList<>();
		for (Expedition e : state.all()) snapshot.add(e);
		for (Iterator<Expedition> it = snapshot.iterator(); it.hasNext(); ) {
			Expedition e = it.next();
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(e.owner);
			if (ExpeditionProgressRules.expired(e.startTick, now)) {
				state.remove(e.owner);
				if (player != null) player.sendMessage(Text.translatable("expedition.aptitudes.timeout").formatted(Formatting.GRAY), true);
				continue;
			}
			if (player == null) continue;
			int shards = countItem(player, ModItems.SOUL_SHARD);
			if (e.current() == ExpeditionRules.Module.PUZZLE && shards > e.shardsSeen
					&& ExpeditionProgressRules.inZone(player.squaredDistanceTo(e.x, e.y, e.z))) {
				e.progress = Math.min(ExpeditionProgressRules.goal(e.current()), e.progress + (shards - e.shardsSeen));
				if (e.progress >= ExpeditionProgressRules.goal(e.current())) {
					nextModule(player, e, state);
					e = state.get(e.owner);
					if (e == null) continue;
				}
			}
			if (e != null) e.shardsSeen = shards;
		}
		state.markDirty();
	}

	// ----------------------------------------------------------- Utilidades

	private static void reward(ServerPlayerEntity player) {
		player.getInventory().offerOrDrop(new ItemStack(Items.EMERALD, ExpeditionProgressRules.REWARD_EMERALDS));
		player.getInventory().offerOrDrop(new ItemStack(ModItems.SOUL_SHARD, ExpeditionProgressRules.REWARD_SOUL_SHARDS));
	}

	private static boolean isBoss(Entity entity) {
		if (entity instanceof AnimatedBossEntity) return true;
		for (String tag : entity.getCommandTags()) {
			if (BossKind.fromTag(tag) != null) return true;
		}
		return false;
	}

	private static int countItem(ServerPlayerEntity player, Item item) {
		int total = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.isOf(item)) total += stack.getCount();
		}
		return total;
	}

	private static Text moduleName(ExpeditionRules.Module module) {
		return Text.translatable("expedition.aptitudes.module." + module.name().toLowerCase(Locale.ROOT));
	}

	private static void fail(ServerPlayerEntity player, String key) {
		player.sendMessage(Text.translatable(key).formatted(Formatting.RED), true);
	}
}
