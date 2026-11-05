package dev.aptitudes.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

/**
 * Cuadrícula 3×3 de la Mesa del gremio (v27). Misma disposición que la mesa de trabajo, pero solo busca
 * recetas de {@link GuildCrafting#RECIPE_TYPE} (las armaduras de rol). El resultado lo calcula el servidor.
 *
 * <p>Ranuras: 0 = resultado, 1-9 = cuadrícula, 10-36 = inventario, 37-45 = barra rápida.</p>
 */
public class GuildDeskScreenHandler extends ScreenHandler {
	private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
	private final CraftingResultInventory result = new CraftingResultInventory();
	private final ScreenHandlerContext context;

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
		context.run((world, pos) -> {
			if (world.isClient()) return;
			CraftingRecipeInput recipeInput = input.createRecipeInput();
			ItemStack out = world.getRecipeManager().getFirstMatch(GuildCrafting.RECIPE_TYPE, recipeInput, world)
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
		if (!slot.hasStack()) return moved;
		ItemStack stack = slot.getStack();
		moved = stack.copy();
		if (index == 0) {
			if (!insertItem(stack, 10, 46, true)) return ItemStack.EMPTY;
			slot.onQuickTransfer(stack, moved);
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
		slot.onTakeItem(player, stack);
		return moved;
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

	/** Ranura de resultado: no admite objetos; al sacar el resultado se gasta 1 de cada ingrediente. */
	private class ResultSlot extends Slot {
		ResultSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}

		@Override
		public void onTakeItem(PlayerEntity player, ItemStack stack) {
			boolean validCraft = player instanceof net.minecraft.server.network.ServerPlayerEntity
					&& canUse(player) && player.getWorld().getRecipeManager()
					.getFirstMatch(GuildCrafting.RECIPE_TYPE, input.createRecipeInput(), player.getWorld()).isPresent();
			if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) dev.aptitudes.contract.ContractManager.forge(serverPlayer);
			stack.onCraftByPlayer(player.getWorld(), player, stack.getCount());
			// Las recetas del gremio no usan ingredientes con «resto» (cubos, botellas): se gastan sin más.
			for (int i = 0; i < input.size(); i++) {
				if (!input.getStack(i).isEmpty()) input.removeStack(i, 1);
			}
			super.onTakeItem(player, stack);
			if (validCraft) GuildCrafting.onCrafted((net.minecraft.server.network.ServerPlayerEntity) player);
		}
	}
}
