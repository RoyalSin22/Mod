package dev.aptitudes.contract;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ContractBoardBlock extends Block {
	public ContractBoardBlock(Settings settings) { super(settings); }
	@Override protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient()) return ActionResult.SUCCESS;
		player.openHandledScreen(createScreenHandlerFactory(state, world, pos));
		return ActionResult.CONSUME;
	}
	@Override protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory((id, inv, player) -> new ContractBoardScreenHandler(id), Text.translatable("contract.board.title"));
	}
}
