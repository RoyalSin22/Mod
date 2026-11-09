package dev.aptitudes.client;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.client.screen.GuideScreen;
import dev.aptitudes.item.ModItems;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * v61 (G2): «ver en el grimorio» desde cualquier inventario. Al pasar el ratón por un objeto de Aptitudes,
 * el tooltip indica la tecla; al pulsarla se abre su ficha. Requiere llevar el Grimorio y no tener nada en el cursor.
 */
public final class GuideLookup {
	private static KeyBinding key;

	private GuideLookup() {
	}

	public static void register(String category) {
		key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.aptitudes.guide_lookup", InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_U, category));
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (key.isUnbound() || !(client.currentScreen instanceof HandledScreen<?>) || !eligible(stack) || !hasGuide(client)) return;
			lines.add(Text.translatable("screen.aptitudes.guide.lookup", key.getBoundKeyLocalizedText()).formatted(Formatting.DARK_AQUA));
		});
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof HandledScreen<?> handled)) return;
			ScreenKeyboardEvents.afterKeyPress(screen).register((s, keyCode, scanCode, modifiers) -> {
				if (key.matchesKey(keyCode, scanCode)) open(client, handled);
			});
		});
	}

	private static boolean eligible(ItemStack stack) {
		return !stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).getNamespace().equals(Aptitudes.MOD_ID);
	}

	private static boolean hasGuide(MinecraftClient client) {
		return client.player != null && client.player.getInventory().contains(new ItemStack(ModItems.GUIDE_BOOK));
	}

	private static void open(MinecraftClient client, HandledScreen<?> screen) {
		if (client.player == null || !hasGuide(client) || !screen.getScreenHandler().getCursorStack().isEmpty()) return;
		// Con un campo de texto activo (buscador del creativo, yunque) la tecla es una letra más.
		for (Element child : screen.children()) {
			if (child instanceof TextFieldWidget field && field.isFocused()) return;
		}
		Slot slot = screen.focusedSlot;
		if (slot == null || !slot.hasStack() || !eligible(slot.getStack())) return;
		Item item = slot.getStack().getItem();
		boolean playerInventory = screen instanceof InventoryScreen;
		// Cierra el contenedor como al pulsar Esc: el servidor queda sincronizado.
		screen.close();
		client.setScreen(GuideScreen.forItem(item, playerInventory ? () -> client.player == null ? null : new InventoryScreen(client.player) : null));
	}
}
