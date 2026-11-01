package dev.aptitudes.block;

import dev.aptitudes.souls.SoulsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Hoguera: al descansar (clic derecho) cura por completo, recarga el maná, fija
 * el punto de reaparición y abre la pantalla de subida de nivel. Los niveles de
 * alma solo se pueden comprar junto a una Hoguera.
 */
public class BonfireBlock extends Block {
	private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 6, 15);

	public BonfireBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
			SoulsManager.rest(serverPlayer, pos);
		}
		return ActionResult.success(world.isClient());
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.4;
		double z = pos.getZ() + 0.5;
		world.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5) * 0.3, y, z + (random.nextDouble() - 0.5) * 0.3, 0, 0.02, 0);
		if (random.nextInt(3) == 0) world.addParticle(ParticleTypes.SMOKE, x, y + 0.6, z, 0, 0.03, 0);
		if (random.nextInt(8) == 0) {
			world.playSound(x, y, z, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.6f, 1.0f, false);
		}
	}
}
