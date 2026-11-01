package dev.aptitudes.accessory;

import dev.aptitudes.Aptitudes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

/**
 * Contenedor de accesorios: ranuras 0-1 (anillos) y 2 (amuleto) + inventario del
 * jugador. En el servidor el inventario está respaldado por el adjunto del jugador.
 */
public class AccessoryScreenHandler extends ScreenHandler {
	public static final ScreenHandlerType<AccessoryScreenHandler> TYPE = Registry.register(Registries.SCREEN_HANDLER,
			Aptitudes.id("accessories"), new ScreenHandlerType<>(AccessoryScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	/** Posiciones (x, y) de las ranuras de accesorio dentro de la pantalla. */
	public static final int[][] SLOT_POS = {{62, 26}, {98, 26}, {80, 50}};

	private final Inventory accessories;

	/** Cliente: inventario vacío que el servidor sincroniza. */
	public AccessoryScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(3));
	}

	public AccessoryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory accessories) {
		super(TYPE, syncId);
		this.accessories = accessories;
		for (int i = 0; i < 3; i++) {
			boolean ringSlot = i < 2;
			addSlot(new Slot(accessories, i, SLOT_POS[i][0], SLOT_POS[i][1]) {
				@Override
				public boolean canInsert(ItemStack stack) {
					return stack.getItem() instanceof AccessoryItem a && a.accessory().ring() == ringSlot;
				}

				@Override
				public int getMaxItemCount() {
					return 1;
				}
			});
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasStack()) return ItemStack.EMPTY;
		ItemStack stack = slot.getStack();
		ItemStack copy = stack.copy();
		if (index < 3) {
			if (!insertItem(stack, 3, slots.size(), true)) return ItemStack.EMPTY;
		} else {
			boolean moved = false;
			for (int i = 0; i < 3 && !moved; i++) {
				Slot target = slots.get(i);
				if (!target.hasStack() && target.canInsert(stack)) {
					target.setStack(stack.split(1));
					moved = true;
				}
			}
			if (!moved) return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
		else slot.markDirty();
		return copy;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	public static void init() {
	}
}
