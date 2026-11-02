package dev.aptitudes.item;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleResources;
import net.minecraft.entity.effect.StatusEffectInstance;
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
 * Laúd (Bardo): tocar da Velocidad I a los jugadores cercanos y carga Armonía.
 * Un Bardo activo alarga la canción con su nivel.
 */
public class LuteItem extends Item {
	public LuteItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient() && user instanceof ServerPlayerEntity player) {
			var data = RoleManager.get(player);
			int level = data.isActive(Role.BARD) ? data.level(Role.BARD) : 0;
			int ticks = 100 + level * 6;
			for (PlayerEntity ally : world.getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(8),
					PlayerEntity::isAlive)) {
				ally.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, ticks, 0));
			}
			float pitch = 0.6f + world.random.nextInt(12) * 0.1f;
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(),
					SoundCategory.PLAYERS, 1.0f, pitch);
			player.getServerWorld().spawnParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 2.1, player.getZ(),
					6, 0.6, 0.3, 0.6, 1.0);
			ParticleFx.ring(player.getServerWorld(), player.getPos().add(0, 0.1, 0), 3, ParticleTypes.NOTE, 12);
			player.getItemCooldownManager().set(this, 200);
			RoleManager.addRoleXp(player, Role.BARD, 4);
			RoleManager.recordTrial(player, Role.BARD, dev.aptitudes.role.Evolution.TrialType.ROLE_ACTIONS, 1);
			RoleResources.gain(player, Role.BARD, 10);
		}
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.lute.tooltip").formatted(Formatting.GRAY));
	}
}
