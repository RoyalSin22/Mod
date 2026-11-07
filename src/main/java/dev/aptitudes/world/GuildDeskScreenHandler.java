package dev.aptitudes.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

/**
 * Cuadrícula 3×3 que busca únicamente recetas de {@link GuildCrafting#RECIPE_TYPE}.
 * El resultado y el consumo se calculan en el servidor.
 *
 * <p>Ranuras: 0 = resultado, 1-9 = cuadrícula, 10-36 = inventario, 37-45 = barra rápida.</p>
 */
public class GuildDeskScreenHandler extends ScreenHandler {
	private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
	private final CraftingResultInventory result = new CraftingResultInventory();
	private final ScreenHandlerContext context;
	private boolean crafting;

	/** Cliente: el servidor sincroniza el contenido. */
	public GuildDeskScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	public GuildDeskScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(GuildCrafting.SCREEN, syncId);
		this.context = context;
		addSlot(new ResultSlot(result, 0, 124, 35));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) addSlot(new Slot(input, col + row * 3, 30 + col * 18, 17 + row * 18));
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
	}

	@Override
	public void onContentChanged(Inventory inventory) {
		if (crafting) return;
		context.run((world, pos) -> {
			if (world.isClient()) return;
			CraftingRecipeInput recipeInput = input.createRecipeInput();
			ItemStack out = findRecipe(world, recipeInput)
					.map(entry -> entry.value().craft(recipeInput, world.getRegistryManager()))
					.orElse(ItemStack.EMPTY);
			result.setStack(0, out);
			sendContentUpdates();
		});
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack moved = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (!slot.hasStack() || !slot.canTakeItems(player)) return moved;
		ItemStack stack = slot.getStack();
		moved = stack.copy();
		if (index == 0) {
			if (!canFitResult(stack) || !insertItem(stack, 10, 46, true)) return ItemStack.EMPTY;
		} else if (index >= 10) {
			if (!insertItem(stack, 1, 10, false)) {
				boolean ok = index < 37 ? insertItem(stack, 37, 46, false) : insertItem(stack, 10, 37, false);
				if (!ok) return ItemStack.EMPTY;
			}
		} else if (!insertItem(stack, 10, 46, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
		else slot.markDirty();
		if (stack.getCount() == moved.getCount()) return ItemStack.EMPTY;
		slot.onTakeItem(player, index == 0 ? moved : stack);
		return moved;
	}

	private static Optional<RecipeEntry<CraftingRecipe>> findRecipe(World world, CraftingRecipeInput input) {
		return world.getRecipeManager().getFirstMatch(GuildCrafting.RECIPE_TYPE, input, world);
	}

	/** Mayús + clic solo fabrica si cabe el resultado entero. */
	private boolean canFitResult(ItemStack resultStack) {
		int remaining = resultStack.getCount();
		for (int i = 10; i < 46; i++) {
			Slot slot = slots.get(i);
			ItemStack stored = slot.getStack();
			if (stored.isEmpty() || ItemStack.areItemsAndComponentsEqual(stored, resultStack)) {
				remaining -= Math.max(0, slot.getMaxItemCount(resultStack) - stored.getCount());
				if (remaining <= 0) return true;
			}
		}
		return false;
	}

	@Override
	public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
		return slot.inventory != result && super.canInsertIntoSlot(stack, slot);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return canUse(context, player, GuildVillagers.GUILD_DESK);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		context.run((world, pos) -> dropInventory(player, input));
	}

	/** Cada extracción válida consume un juego de ingredientes y devuelve sus restos. */
	private class ResultSlot extends Slot {
		ResultSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}

		@Override
		public boolean canTakeItems(PlayerEntity player) {
			if (!player.isAlive() || player.isSpectator() || !canUse(player) || crafting) return false;
			World world = player.getWorld();
			if (world.isClient()) return true;
			CraftingRecipeInput recipeInput = input.createRecipeInput();
			return findRecipe(world, recipeInput)
					.map(entry -> ItemStack.areEqual(getStack(), entry.value().craft(recipeInput, world.getRegistryManager())))
					.orElse(false);
		}

		@Override
		public void onTakeItem(PlayerEntity player, ItemStack stack) {
			if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)) return;
			World world = player.getWorld();
			var positioned = input.createPositionedRecipeInput();
			CraftingRecipeInput recipeInput = positioned.input();
			var match = findRecipe(world, recipeInput);
			if (match.isEmpty()) return;

			var remainder = match.get().value().getRemainder(recipeInput);
			crafting = true;
			try {
				for (int row = 0; row < recipeInput.getHeight(); row++) {
					for (int col = 0; col < recipeInput.getWidth(); col++) {
						int index = positioned.left() + col + (positioned.top() + row) * input.getWidth();
						if (!input.getStack(index).isEmpty()) input.removeStack(index, 1);
						ItemStack rest = remainder.get(col + row * recipeInput.getWidth());
						if (rest.isEmpty()) continue;
						if (input.getStack(index).isEmpty()) input.setStack(index, rest);
						else player.getInventory().offerOrDrop(rest);
					}
				}
			} finally {
				crafting = false;
			}
			onContentChanged(input);
			super.onTakeItem(player, stack);
			stack.onCraftByPlayer(world, player, stack.getCount());
			if (player.isCreative()) return;
			// Los contratos conservan los resultados que ya contaban antes de ampliar la Mesa.
			if (stack.getItem() instanceof dev.aptitudes.item.RoleArmorItem
					|| stack.getItem() instanceof dev.aptitudes.rune.RuneItem) {
				dev.aptitudes.contract.ContractManager.forge(serverPlayer);
			}
			GuildCrafting.onCrafted(serverPlayer);
		}
	}
}
