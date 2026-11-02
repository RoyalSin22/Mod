package dev.aptitudes.item;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.block.ModBlocks;
import dev.aptitudes.network.OpenRolesPayload;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Ofrenda de renacimiento: se quema en una Hoguera (clic derecho sobre ella) y
 * concede un cambio de rol. Sustituye al antiguo tiempo de espera de 30 minutos.
 */
public class RebirthOfferingItem extends Item {
	public RebirthOfferingItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		BlockPos pos = context.getBlockPos();
		if (!context.getWorld().getBlockState(pos).isOf(ModBlocks.BONFIRE)) return ActionResult.PASS;
		if (context.getWorld().isClient()) return ActionResult.SUCCESS;
		if (!(context.getPlayer() instanceof ServerPlayerEntity player)) return ActionResult.PASS;

		ItemStack stack = context.getStack();
		stack.decrementUnlessCreative(1, player);
		RoleManager.grantRebirth(player);

		Vec3d center = Vec3d.ofBottomCenter(pos).add(0, 0.6, 0);
		var world = player.getServerWorld();
		ParticleFx.runeCircle(world, Vec3d.ofBottomCenter(pos).add(0, 0.05, 0), 2.2, ParticleFx.dust(0xFFB74D, 1.3f));
		ParticleFx.spiral(world, Vec3d.ofBottomCenter(pos), 1.0, 3.0, ParticleTypes.SOUL_FIRE_FLAME, 60, 3);
		world.spawnParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 15, 0.3, 0.3, 0.3, 0.05);
		world.playSound(null, pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1.0f, 0.5f);
		world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 0.7f);
		player.sendMessage(Text.translatable("aptitudes.role.rebirth_granted").formatted(Formatting.GOLD), false);
		if (ServerPlayNetworking.canSend(player, OpenRolesPayload.ID)) {
			ServerPlayNetworking.send(player, new OpenRolesPayload());
		}
		return ActionResult.CONSUME;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.rebirth_offering.tooltip").formatted(Formatting.GRAY));
	}
}
