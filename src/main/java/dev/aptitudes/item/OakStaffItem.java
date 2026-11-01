package dev.aptitudes.item;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/** Cayado de roble (Druida): abona un área de 3×3 a cambio de energía. */
public class OakStaffItem extends Item {
	private static final double ENERGY_COST = 8.0;

	public OakStaffItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld().isClient()) return ActionResult.SUCCESS;
		if (!(context.getWorld() instanceof ServerWorld world) || !(context.getPlayer() instanceof ServerPlayerEntity player)) {
			return ActionResult.PASS;
		}
		if (!AbilityManager.tryConsume(player, ENERGY_COST)) return ActionResult.FAIL;

		int grown = 0;
		for (BlockPos pos : BlockPos.iterate(context.getBlockPos().add(-1, -1, -1), context.getBlockPos().add(1, 1, 1))) {
			BlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof Fertilizable f && f.isFertilizable(world, pos, state)
					&& f.canGrow(world, world.random, pos, state)) {
				f.grow(world, world.random, pos.toImmutable(), state);
				world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
						5, 0.3, 0.3, 0.3, 0);
				grown++;
			}
		}
		player.getItemCooldownManager().set(this, 20);
		world.playSound(null, context.getBlockPos(), SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		if (grown > 0) {
			RoleManager.addRoleXp(player, Role.DRUID, 1 + grown / 3);
			RoleManager.recordTrial(player, Role.DRUID, dev.aptitudes.role.Evolution.TrialType.ROLE_ACTIONS, 1);
		}
		return ActionResult.CONSUME;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.oak_staff.tooltip", (int) ENERGY_COST).formatted(Formatting.GRAY));
	}
}
