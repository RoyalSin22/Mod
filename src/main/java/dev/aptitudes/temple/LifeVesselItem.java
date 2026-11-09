package dev.aptitudes.temple;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.temple.VesselRules.Result;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Vaso de vida (v64): al usarlo da +1 corazón permanente (tope configurable, 5 por defecto). Cada vaso lleva un id
 * único que se registra en el mundo al consumirlo, así que una copia del mismo vaso no sirve.
 */
public class LifeVesselItem extends Item {
	private static final String TAG = "aptitudes_vessel";

	public LifeVesselItem(Settings settings) {
		super(settings);
	}

	public static ItemStack create() {
		return withId(new ItemStack(Temples.LIFE_VESSEL), UUID.randomUUID());
	}

	private static ItemStack withId(ItemStack stack, UUID id) {
		NbtCompound nbt = new NbtCompound();
		nbt.putUuid(TAG, id);
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		return stack;
	}

	@Nullable
	static UUID idOf(ItemStack stack) {
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (data == null) return null;
		NbtCompound nbt = data.copyNbt();
		return nbt.containsUuid(TAG) ? nbt.getUuid(TAG) : null;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient || !(user instanceof ServerPlayerEntity player)) return TypedActionResult.success(stack, world.isClient);
		UUID id = idOf(stack);
		// Un vaso sin id (creado con /give) recibe uno nuevo: solo puede ocurrir con permisos de operador.
		if (id == null) id = UUID.randomUUID();
		LifeVesselState used = LifeVesselState.get(player.getServer());
		int count = Temples.vessels(player);
		Result result = VesselRules.use(count, Aptitudes.config().lifeVesselMax, used.contains(id));
		switch (result) {
			case OK -> {
				used.add(id);
				Temples.setVessels(player, count + 1);
				player.heal((float) VesselRules.HEALTH_PER_VESSEL);
				stack.decrementUnlessCreative(1, player);
				world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.6f, 1.4f);
				player.sendMessage(Text.translatable("temple.aptitudes.vessel.used", count + 1,
						VesselRules.limit(Aptitudes.config().lifeVesselMax)).formatted(Formatting.LIGHT_PURPLE), true);
				dev.aptitudes.achievement.Achievements.grant(player, dev.aptitudes.achievement.Achievement.LIFE_VESSEL);
				return TypedActionResult.consume(stack);
			}
			case AT_LIMIT -> player.sendMessage(Text.translatable("temple.aptitudes.vessel.limit").formatted(Formatting.GRAY), true);
			case ALREADY_USED -> player.sendMessage(Text.translatable("temple.aptitudes.vessel.duplicate").formatted(Formatting.RED), true);
			case DISABLED -> player.sendMessage(Text.translatable("temple.aptitudes.vessel.disabled").formatted(Formatting.GRAY), true);
		}
		return TypedActionResult.fail(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.life_vessel.tooltip").formatted(Formatting.GRAY));
	}
}
