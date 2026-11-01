package dev.aptitudes.item;

import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleResources;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
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
 * Silbato del domador: llama a tus mascotas domesticadas (hasta 48 bloques), las
 * cura y les da Velocidad. Carga el Vínculo del Domador.
 */
public class BeastWhistleItem extends Item {
	public BeastWhistleItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient() && user instanceof ServerPlayerEntity player) {
			List<TameableEntity> pets = world.getEntitiesByClass(TameableEntity.class, player.getBoundingBox().expand(48),
					t -> t.isAlive() && t.isOwner(player));
			for (TameableEntity pet : pets) {
				if (pet.isSitting()) continue;
				if (pet.squaredDistanceTo(player) > 64) pet.requestTeleport(player.getX(), player.getY(), player.getZ());
				pet.heal(4.0f);
				pet.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
				player.getServerWorld().spawnParticles(ParticleTypes.HEART, pet.getX(), pet.getBodyY(0.8), pet.getZ(),
						3, 0.3, 0.3, 0.3, 0);
			}
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WOLF_WHINE,
					SoundCategory.PLAYERS, 1.0f, 1.6f);
			player.getItemCooldownManager().set(this, 200);
			if (!pets.isEmpty()) {
				RoleManager.addRoleXp(player, Role.TAMER, 3 + pets.size());
				RoleManager.recordTrial(player, Role.TAMER, dev.aptitudes.role.Evolution.TrialType.ROLE_ACTIONS, 1);
				RoleResources.gain(player, Role.TAMER, 5f * pets.size());
			}
		}
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.beast_whistle.tooltip").formatted(Formatting.GRAY));
	}
}
