package dev.aptitudes.item;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Sello del gremio (Mercader): repone las ofertas de un aldeano. Solo lo puede
 * usar un Mercader activo; tiene un enfriamiento largo para no romper la economía.
 */
public class GuildSealItem extends Item {
	private static final double ENERGY_COST = 30.0;
	private static final int COOLDOWN_TICKS = 5 * 60 * 20;

	public GuildSealItem(Settings settings) {
		super(settings);
	}

	/**
	 * Se invoca desde {@code UseEntityCallback} (ver {@code RoleEvents}) porque el
	 * aldeano abre su pantalla de comercio antes de que el ítem reciba {@code useOnEntity}.
	 */
	public static ActionResult applyTo(ServerPlayerEntity player, VillagerEntity villager, ItemStack stack) {
		if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return ActionResult.FAIL;
		if (!RoleManager.get(player).isActive(Role.MERCHANT)) {
			player.sendMessage(Text.translatable("item.aptitudes.guild_seal.not_merchant").formatted(Formatting.RED), true);
			return ActionResult.FAIL;
		}
		if (!AbilityManager.tryConsume(player, ENERGY_COST)) return ActionResult.FAIL;

		villager.restock();
		player.getItemCooldownManager().set(stack.getItem(), COOLDOWN_TICKS);
		player.getServerWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getBodyY(0.8),
				villager.getZ(), 12, 0.4, 0.4, 0.4, 0);
		player.getWorld().playSound(null, villager.getBlockPos(), SoundEvents.ENTITY_VILLAGER_CELEBRATE,
				SoundCategory.NEUTRAL, 1.0f, 1.0f);
		RoleManager.addRoleXp(player, Role.MERCHANT, 5);
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.guild_seal.tooltip").formatted(Formatting.GRAY));
	}
}
