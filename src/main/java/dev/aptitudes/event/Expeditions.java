package dev.aptitudes.event;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.event.ExpeditionState.Expedition;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.party.PartyManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Expediciones de Amenaza (F5). Un jugador usa la ficha de expedición en el Overworld: se fija el punto de inicio, la
 * lista de módulos y dos modificadores (semilla del mundo y del día), y el grupo: los miembros de la party que están en
 * la zona. Cada módulo se completa con una acción de cualquier miembro dentro de la zona; al terminar todos, hay
 * recompensa para cada uno. Caducan a los 15 minutos. Reglas puras en {@link ExpeditionRules} y
 * {@link ExpeditionProgressRules}.
 *
 * <p>Efectos de los modificadores (decisión provisional, a validar): niebla = lentitud I a los miembros en la zona;
 * lluvia de brasas = fuego de 2 s; oleada élite = Fuerza I a los élites de la zona; hambre = Hambre I; jefe aturdido =
 * +20 % de daño a los jefes de la zona; botín abundante = recompensa doble.</p>
 */
public final class Expeditions {
	/** Ficha que inicia la expedición. Aparece en la pestaña creativa de Aptitudes. */
	public static final Item TOKEN = Registry.register(Registries.ITEM, Aptitudes.id("expedition_token"),
			new Token(new Item.Settings().maxCount(1)));

	/** Dificultad fija del MVP: 4 módulos. */
	private static final int DIFFICULTY = 1;
	/** Modificadores por expedición. */
	private static final int MODIFIER_COUNT = 2;
	/** Multiplicador de daño a los jefes con «jefe aturdido». */
	private static final float BOSS_STAGGER_DAMAGE = 1.2f;

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

	/** Inicia la expedición del grupo del jugador. Devuelve {@code false} si no se pudo. */
	public static boolean start(ServerPlayerEntity player) {
		ServerWorld overworld = player.getServer().getOverworld();
		if (player.getWorld() != overworld) {
			fail(player, "expedition.aptitudes.overworld_only");
			return false;
		}
		ExpeditionState state = ExpeditionState.get(overworld);
		// El grupo: quien está en la party y dentro de la zona de inicio (la posición del jugador).
		List<ServerPlayerEntity> group = new ArrayList<>();
		for (ServerPlayerEntity member : PartyManager.onlineMembers(player)) {
			if (member.getWorld() == overworld
					&& ExpeditionProgressRules.inZone(member.squaredDistanceTo(player.getX(), player.getY(), player.getZ()))) {
				group.add(member);
			}
		}
		if (group.isEmpty()) group.add(player);
		for (ServerPlayerEntity member : group) {
			if (state.get(member.getUuid()) != null) {
				fail(player, "expedition.aptitudes.already");
				return false;
			}
		}
		long day = Math.max(0L, overworld.getTimeOfDay() / 24_000L);
		long seed = overworld.getSeed() ^ day;
		List<ExpeditionRules.Module> modules = ExpeditionRules.compose(seed, DIFFICULTY);
		Expedition expedition = new Expedition(player.getUuid(), player.getX(), player.getY(), player.getZ(), modules,
				overworld.getTime());
		for (ServerPlayerEntity member : group) expedition.members.add(member.getUuid());
		expedition.modifiers.addAll(ExpeditionRules.modifiers(seed, MODIFIER_COUNT));
		state.put(expedition);
		say(player.getServer(), expedition, Text.translatable("expedition.aptitudes.started", modules.size(),
				moduleName(modules.get(0))).formatted(Formatting.GOLD), true);
		say(player.getServer(), expedition, Text.translatable("expedition.aptitudes.modifiers", modifierNames(expedition))
				.formatted(Formatting.GRAY), false);
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
			advance(world.getServer(), e, state);
		} else if (current == ExpeditionRules.Module.ELITE && entity instanceof MobEntity mob && Elites.isElite(mob)) {
			advance(world.getServer(), e, state);
		} else if (current == ExpeditionRules.Module.BOSS && isBoss(entity)) {
			advance(world.getServer(), e, state);
		}
	}

	/** Lo llama {@link dev.aptitudes.contract.ContractManager}: sobrevivir a un evento del mundo cuenta en la zona. */
	public static void onEventSurvived(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world)) return;
		ExpeditionState state = ExpeditionState.get(world.getServer().getOverworld());
		Expedition e = state.get(player.getUuid());
		if (e == null || e.current() != ExpeditionRules.Module.EVENT) return;
		if (!ExpeditionProgressRules.inZone(player.squaredDistanceTo(e.x, e.y, e.z))) return;
		advance(world.getServer(), e, state);
	}

	/**
	 * Multiplicador de daño que aporta la expedición del atacante: «jefe aturdido» en un jefe de la zona. Lo usa
	 * {@code RolePassives.modifyOutgoingDamage}.
	 */
	public static float damageMultiplier(ServerPlayerEntity attacker, LivingEntity target) {
		if (!(attacker.getWorld() instanceof ServerWorld world) || !isBoss(target)) return 1f;
		Expedition e = ExpeditionState.get(world.getServer().getOverworld()).get(attacker.getUuid());
		if (e == null || !e.modifiers.contains("boss_stagger")) return 1f;
		return ExpeditionProgressRules.inZone(target.squaredDistanceTo(e.x, e.y, e.z)) ? BOSS_STAGGER_DAMAGE : 1f;
	}

	private static void advance(MinecraftServer server, Expedition e, ExpeditionState state) {
		e.progress++;
		if (e.progress >= ExpeditionProgressRules.goal(e.current())) {
			nextModule(server, e, state);
		} else {
			state.markDirty();
		}
	}

	private static void nextModule(MinecraftServer server, Expedition e, ExpeditionState state) {
		e.index++;
		e.progress = 0;
		if (e.index >= e.modules.size()) {
			state.remove(e.owner);
			int multiplier = e.modifiers.contains("loot_boost") ? 2 : 1;
			for (ServerPlayerEntity member : onlineMembers(server, e)) reward(member, multiplier);
			say(server, e, Text.translatable("expedition.aptitudes.complete",
					ExpeditionProgressRules.REWARD_EMERALDS * multiplier,
					ExpeditionProgressRules.REWARD_SOUL_SHARDS * multiplier).formatted(Formatting.GREEN), false);
			return;
		}
		state.markDirty();
		say(server, e, Text.translatable("expedition.aptitudes.module_next", e.index + 1, e.modules.size(),
				moduleName(e.current())).formatted(Formatting.GOLD), true);
	}

	/**
	 * Cada 20 ticks: caduca las expediciones, aplica los efectos de los modificadores y cuenta la recolección del grupo
	 * (fragmentos de alma de los miembros conectados, si alguno está en la zona).
	 */
	private static void tick(MinecraftServer server) {
		ExpeditionState state = ExpeditionState.get(server.getOverworld());
		long now = server.getOverworld().getTime();
		for (Expedition e : new ArrayList<>(list(state))) {
			if (ExpeditionProgressRules.expired(e.startTick, now)) {
				state.remove(e.owner);
				say(server, e, Text.translatable("expedition.aptitudes.timeout").formatted(Formatting.GRAY), true);
				continue;
			}
			List<ServerPlayerEntity> members = onlineMembers(server, e);
			boolean anyInZone = false;
			int shards = 0;
			for (ServerPlayerEntity member : members) {
				shards += countItem(member, ModItems.SOUL_SHARD);
				if (ExpeditionProgressRules.inZone(member.squaredDistanceTo(e.x, e.y, e.z))) {
					anyInZone = true;
					applyPlayerEffects(member, e);
				}
			}
			if (e.modifiers.contains("elite_surge")) applyEliteSurge(server, e);
			if (e.current() == ExpeditionRules.Module.PUZZLE && anyInZone && shards > e.shardsSeen) {
				e.progress = Math.min(ExpeditionProgressRules.goal(e.current()), e.progress + (shards - e.shardsSeen));
				if (e.progress >= ExpeditionProgressRules.goal(e.current())) {
					e.shardsSeen = shards; // antes de cambiar de módulo: lo ya contado no puntúa en el siguiente
					nextModule(server, e, state);
					continue;
				}
			}
			e.shardsSeen = shards;
		}
		state.markDirty();
	}

	private static void applyPlayerEffects(ServerPlayerEntity player, Expedition e) {
		if (e.modifiers.contains("fog")) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0));
		}
		if (e.modifiers.contains("hunger")) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 0));
		}
		if (e.modifiers.contains("ember_rain") && !player.isFireImmune()) {
			player.setOnFireFor(2);
		}
	}

	private static void applyEliteSurge(MinecraftServer server, Expedition e) {
		ServerWorld world = server.getOverworld();
		Box zone = new Box(e.x - ExpeditionProgressRules.ZONE_RADIUS, e.y - ExpeditionProgressRules.ZONE_RADIUS,
				e.z - ExpeditionProgressRules.ZONE_RADIUS, e.x + ExpeditionProgressRules.ZONE_RADIUS,
				e.y + ExpeditionProgressRules.ZONE_RADIUS, e.z + ExpeditionProgressRules.ZONE_RADIUS);
		for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, zone, Elites::isElite)) {
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0));
		}
	}

	// ----------------------------------------------------------- Utilidades

	private static List<Expedition> list(ExpeditionState state) {
		List<Expedition> out = new ArrayList<>();
		for (Expedition e : state.all()) out.add(e);
		return out;
	}

	private static List<ServerPlayerEntity> onlineMembers(MinecraftServer server, Expedition e) {
		List<ServerPlayerEntity> out = new ArrayList<>();
		for (UUID id : e.members) {
			ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
			if (p != null) out.add(p);
		}
		return out;
	}

	/** Envía un mensaje a todos los miembros conectados. */
	private static void say(MinecraftServer server, Expedition e, Text text, boolean actionBar) {
		for (ServerPlayerEntity member : onlineMembers(server, e)) member.sendMessage(text, actionBar);
	}

	private static Text modifierNames(Expedition e) {
		MutableText out = Text.empty();
		for (int i = 0; i < e.modifiers.size(); i++) {
			if (i > 0) out.append(", ");
			out.append(Text.translatable("expedition.aptitudes.modifier." + e.modifiers.get(i)));
		}
		return out;
	}

	private static void reward(ServerPlayerEntity player, int multiplier) {
		player.getInventory().offerOrDrop(new ItemStack(Items.EMERALD, ExpeditionProgressRules.REWARD_EMERALDS * multiplier));
		player.getInventory().offerOrDrop(new ItemStack(ModItems.SOUL_SHARD,
				ExpeditionProgressRules.REWARD_SOUL_SHARDS * multiplier));
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
