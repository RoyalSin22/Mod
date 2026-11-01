package dev.aptitudes.item;

import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Llave inglesa (Tecnólogo): gira 90° bloques orientables (tolvas, pistones,
 * observadores, escaleras…). Excluye bloques de varias partes (camas, puertas,
 * cofres dobles, pistones extendidos) para no romperlos.
 */
public class WrenchItem extends Item {
	public WrenchItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if (!canRotate(state)) return ActionResult.PASS;
		if (world.isClient()) return ActionResult.SUCCESS;

		BlockState rotated = state.rotate(BlockRotation.CLOCKWISE_90);
		if (rotated == state || !rotated.canPlaceAt(world, pos)) return ActionResult.FAIL;
		world.setBlockState(pos, rotated);
		world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.6f, 1.6f);
		if (context.getPlayer() instanceof ServerPlayerEntity player) {
			RoleManager.addRoleXp(player, Role.ENGINEER, 1);
		}
		return ActionResult.CONSUME;
	}

	private static boolean canRotate(BlockState state) {
		if (state.isIn(BlockTags.BEDS) || state.isIn(BlockTags.DOORS)) return false;
		if (state.getBlock() instanceof ChestBlock || state.isOf(Blocks.PISTON_HEAD) || state.isOf(Blocks.MOVING_PISTON)) {
			return false;
		}
		if (state.getBlock() instanceof PistonBlock && state.get(PistonBlock.EXTENDED)) return false;
		if (state.contains(Properties.BED_PART) || state.contains(Properties.DOUBLE_BLOCK_HALF)) return false;
		return state.contains(Properties.FACING) || state.contains(Properties.HORIZONTAL_FACING)
				|| state.contains(Properties.HOPPER_FACING) || state.contains(Properties.AXIS);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.wrench.tooltip").formatted(Formatting.GRAY));
	}
}
