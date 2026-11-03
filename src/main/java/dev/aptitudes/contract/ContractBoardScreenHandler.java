package dev.aptitudes.contract;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public final class ContractBoardScreenHandler extends ScreenHandler {
	public static final ScreenHandlerType<ContractBoardScreenHandler> TYPE = net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.SCREEN_HANDLER, dev.aptitudes.Aptitudes.id("contract_board"),
			new ScreenHandlerType<>(ContractBoardScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES));
	public ContractBoardScreenHandler(int syncId, PlayerInventory inventory) { this(syncId); }
	public ContractBoardScreenHandler(int syncId) { super(TYPE, syncId); }
	public static void init() { }
	@Override public net.minecraft.item.ItemStack quickMove(PlayerEntity player, int slot) { return net.minecraft.item.ItemStack.EMPTY; }
	@Override public boolean canUse(PlayerEntity player) { return true; }
}
