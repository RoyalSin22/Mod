package dev.aptitudes.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Mesa del gremio (v27): además de bloque de trabajo del aldeano de gremio, al usarla abre la cuadrícula
 * donde se fabrican las armaduras de rol. Mismo id que antes ({@code aptitudes:guild_desk}).
 */
public class GuildDeskBlock extends Block {
	private static final Text TITLE = Text.translatable("container.aptitudes.guild_desk");

	public GuildDeskBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient()) return ActionResult.SUCCESS;
		player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
		return ActionResult.CONSUME;
	}

	@Override
	protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
				new GuildDeskScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), TITLE);
	}
}
