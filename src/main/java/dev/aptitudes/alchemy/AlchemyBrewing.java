package dev.aptitudes.alchemy;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Observa soportes cargados; vanilla conserva la autoridad sobre receta, combustible y consumo. */
public final class AlchemyBrewing {
	public static final String BOOSTED_KEY = "aptitudes.herbal_brew";
	private record Helper(ServerPlayerEntity player, int radius) { }
	private static final class Stand {
		final BrewingStandBlockEntity block;
		double credit;
		Helper helper;
		Stand(BrewingStandBlockEntity block) { this.block = block; }
	}
	private static final Map<ServerWorld, Map<BrewingStandBlockEntity, Stand>> LOADED = new HashMap<>();
	private static final Map<UUID, Object> OPEN_SCREENS = new HashMap<>();

	private AlchemyBrewing() {
	}

	public static void register() {
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((block, world) -> {
			if (block instanceof BrewingStandBlockEntity stand) {
				LOADED.computeIfAbsent(world, w -> new HashMap<>()).put(stand, new Stand(stand));
			}
		});
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((block, world) -> {
			var stands = LOADED.get(world);
			if (stands != null) {
				stands.remove(block);
				if (stands.isEmpty()) LOADED.remove(world);
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> OPEN_SCREENS.remove(handler.player.getUuid()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> { LOADED.clear(); OPEN_SCREENS.clear(); });
		ServerTickEvents.START_WORLD_TICK.register(AlchemyBrewing::tick);
	}

	private static boolean qualified(ServerPlayerEntity player, ServerWorld world) {
		return player != null && player.getServerWorld() == world && player.isAlive()
				&& !player.isCreative() && !player.isSpectator()
				&& world.getServer().getPlayerManager().getPlayer(player.getUuid()) == player
				&& RoleManager.get(player).isActive(Role.HERBALIST);
	}

	private static boolean inRange(ServerPlayerEntity player, BrewingStandBlockEntity stand, int radius) {
		return player.squaredDistanceTo(stand.getPos().toCenterPos()) <= (long) radius * radius;
	}

	private static Helper helper(ServerWorld world, BrewingStandBlockEntity stand) {
		String owner = stand.getAttached(ModAttachments.HERBAL_BREWER);
		if (owner != null) {
			try {
				ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(UUID.fromString(owner));
				if (qualified(player, world) && inRange(player, stand, 32)) return new Helper(player, 32);
			} catch (IllegalArgumentException ignored) {
				// Una marca guardada corrupta no impide usar el soporte.
			}
		}
		int radius = Aptitudes.config().alchemyHerbalistRadius;
		ServerPlayerEntity nearest = null;
		double distance = (long) radius * radius;
		if (radius > 0) for (ServerPlayerEntity player : world.getPlayers()) {
			double d = player.squaredDistanceTo(stand.getPos().toCenterPos());
			if (qualified(player, world) && d <= distance) {
				nearest = player;
				distance = d;
			}
		}
		return nearest == null ? null : new Helper(nearest, radius);
	}

	private static void tick(ServerWorld world) {
		if (!Aptitudes.config().alchemyHerbalistEnabled) return;
		var stands = LOADED.get(world);
		if (stands == null) return;
		boolean refresh = world.getServer().getTicks() % 20 == 0;
		for (ServerPlayerEntity player : world.getPlayers()) {
			Object previous = OPEN_SCREENS.put(player.getUuid(), player.currentScreenHandler);
			if (previous == player.currentScreenHandler) continue;
			if (qualified(player, world) && player.currentScreenHandler instanceof BrewingStandScreenHandler handler
					&& handler.getSlot(0).inventory instanceof BrewingStandBlockEntity block && stands.containsKey(block)) {
				String owner = player.getUuidAsString();
				if (!owner.equals(block.getAttached(ModAttachments.HERBAL_BREWER))) {
					block.setAttached(ModAttachments.HERBAL_BREWER, owner);
					block.markDirty();
				}
			}
		}
		for (Stand state : List.copyOf(stands.values())) {
			BrewingStandBlockEntity stand = state.block;
			if (stand.isRemoved() || !world.isChunkLoaded(stand.getPos())) continue;
			int remaining = stand.propertyDelegate.get(BrewingStandBlockEntity.BREW_TIME_PROPERTY_INDEX);
			if (remaining <= 0) { state.credit = 0; continue; }
			if (remaining <= 2 || refresh) state.helper = helper(world, stand);
			Helper h = state.helper;
			if (h == null || !qualified(h.player(), world) || !inRange(h.player(), stand, h.radius())) {
				state.credit = 0;
				continue;
			}
			// Se ocupa el último tick ANTES del ticker normal, sin inferir cobros de cambios del inventario.
			if (remaining == 1) { finish(world, stand); state.credit = 0; continue; }
			state.credit = BrewingRules.speedCredit(state.credit, Aptitudes.config().alchemyBrewingSpeedBonus);
			if (state.credit >= 1) {
				state.credit -= 1;
				if (remaining == 2) finishAfterTick(world, stand);
				else BrewingStandBlockEntity.tick(world, stand.getPos(), stand.getCachedState(), stand);
			}
		}
	}

	private static void finishAfterTick(ServerWorld world, BrewingStandBlockEntity stand) {
		BrewingStandBlockEntity.tick(world, stand.getPos(), stand.getCachedState(), stand);
		if (stand.propertyDelegate.get(0) == 1) finish(world, stand);
	}

	private static void finish(ServerWorld world, BrewingStandBlockEntity stand) {
		ItemStack ingredient = stand.getStack(3).copy();
		var recipes = world.getBrewingRecipeRegistry();
		List<ItemStack> expected = new ArrayList<>();
		boolean[] changes = new boolean[3];
		for (int slot = 0; slot < 3; slot++) {
			ItemStack input = stand.getStack(slot);
			changes[slot] = recipes.hasRecipe(input, ingredient);
			expected.add(recipes.craft(ingredient, input));
		}
		BrewingStandBlockEntity.tick(world, stand.getPos(), stand.getCachedState(), stand);
		// Una receta cancelada o un cambio de ingrediente nunca concede ventajas ni XP.
		if (ingredient.isEmpty() || ItemStack.areEqual(ingredient, stand.getStack(3))) return;
		Helper h = helper(world, stand);
		if (h == null) return;
		boolean brewed = false;
		for (int slot = 0; slot < 3; slot++) {
			ItemStack result = stand.getStack(slot);
			if (!changes[slot] || !ItemStack.areEqual(expected.get(slot), result)) continue;
			PotionContentsComponent contents = result.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
			if (!contents.hasEffects()) continue;
			brewed = true;
			List<StatusEffectInstance> extra = new ArrayList<>(contents.customEffects());
			if (contents.potion().isPresent()) for (StatusEffectInstance effect : contents.potion().get().value().getEffects()) {
				if (effect.getEffectType().value().isInstant()) continue;
				int duration = BrewingRules.duration(effect.getDuration(), Aptitudes.config().alchemyDurationBonus);
				if (duration <= effect.getDuration()) continue;
				extra.removeIf(e -> e.getEffectType().equals(effect.getEffectType()));
				extra.add(new StatusEffectInstance(effect.getEffectType(), duration, effect.getAmplifier(),
						effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()));
			}
			result.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(contents.potion(), contents.customColor(), extra));
			net.minecraft.component.type.NbtComponent.set(DataComponentTypes.CUSTOM_DATA, result,
					nbt -> nbt.putBoolean(BOOSTED_KEY, true));
		}
		if (!brewed) return;
		stand.markDirty();
		ServerPlayerEntity player = h.player();
		AlchemyXpData data = player.getAttachedOrCreate(ModAttachments.ALCHEMY_XP).record(
				AbilityManager.gameTime(player), Aptitudes.config().alchemyXpWindowSeconds);
		// Se guarda el evento antes de conceder XP: abrir o recoger el resultado no lo vuelve a cobrar.
		player.setAttached(ModAttachments.ALCHEMY_XP, data);
		RoleManager.addRoleXp(player, Role.HERBALIST, BrewingRules.xp(Aptitudes.config().alchemyRoleXpPerBrew,
				data.brews(), Aptitudes.config().alchemyXpFullRewardBrews));
	}
}
