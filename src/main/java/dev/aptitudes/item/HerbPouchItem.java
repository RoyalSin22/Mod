package dev.aptitudes.item;

import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Saquito de hierbas (Herbolista): consumible que cura y quita el veneno.
 * Un herbolista activo cura más según su nivel.
 */
public class HerbPouchItem extends Item {
	private static final int COOLDOWN_TICKS = 20 * 20;

	public HerbPouchItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getHealth() >= user.getMaxHealth() && !user.hasStatusEffect(StatusEffects.POISON)) {
			return TypedActionResult.pass(stack);
		}
		if (!world.isClient() && user instanceof ServerPlayerEntity player) {
			var data = RoleManager.get(player);
			int level = data.isActive(Role.HERBALIST) ? data.level(Role.HERBALIST) : 0;
			player.heal(4.0f + level * 0.2f);
			player.removeStatusEffect(StatusEffects.POISON);
			player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
			stack.decrementUnlessCreative(1, player);
			player.getServerWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER,
					player.getX(), player.getBodyY(0.5), player.getZ(), 12, 0.4, 0.4, 0.4, 0.0);
			world.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.8f, 1.2f);
			RoleManager.addRoleXp(player, Role.HERBALIST, 3);
		}
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.herb_pouch.tooltip").formatted(Formatting.GRAY));
	}
}
