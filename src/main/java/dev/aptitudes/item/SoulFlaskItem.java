package dev.aptitudes.item;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.souls.FlaskRules;
import dev.aptitudes.souls.FlaskUpgrades;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

/**
 * Frascos recargables al estilo Souls. Las cargas son la durabilidad: beber gasta
 * una y, al descansar en una Hoguera, se rellenan todas. Nunca se rompen.
 * <ul>
 *   <li><b>Estus</b>: cura 8 de vida de base y da Regeneración I 3 s.</li>
 *   <li><b>Éter</b>: recupera el 40 % del maná máximo de base.</li>
 * </ul>
 */
public class SoulFlaskItem extends Item {
	public enum Kind { ESTUS, ETHER }

	public static final int CHARGES = FlaskRules.BASE_CHARGES;
	private final Kind kind;

	public SoulFlaskItem(Kind kind, Settings settings) {
		super(settings.maxDamage(CHARGES));
		this.kind = kind;
	}

	public static int charges(ItemStack stack) {
		return FlaskRules.remaining(FlaskUpgrades.rank(stack), stack.getDamage());
	}

	public Kind kind() {
		return kind;
	}

	/** Rellena todas las cargas (Hoguera). */
	public static void refill(ItemStack stack) {
		setSpent(stack, FlaskUpgrades.rank(stack), 0);
	}

	private static void setSpent(ItemStack stack, int rank, int spent) {
		int capacity = FlaskRules.capacity(rank);
		if (stack.getMaxDamage() != capacity) stack.set(DataComponentTypes.MAX_DAMAGE, capacity);
		stack.setDamage(spent);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (charges(stack) <= 0) {
			if (!world.isClient()) user.sendMessage(Text.translatable("item.aptitudes.flask.empty")
					.formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}
		return ItemUsage.consumeHeldItem(world, user, hand);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		int rank = FlaskUpgrades.rank(stack);
		if (!world.isClient() && user instanceof ServerPlayerEntity player && player.isAlive() && !player.isSpectator()
				&& FlaskRules.remaining(rank, stack.getDamage()) > 0) {
			if (kind == Kind.ESTUS) {
				player.heal(FlaskRules.estusHealing(rank));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0));
				ParticleFx.spiral(player.getServerWorld(), player.getPos(), 0.6, 2.0, ParticleFx.dust(0xFFA726, 1.0f), 30, 2);
			} else {
				RoleData data = RoleManager.get(player);
				double max = AbilityManager.maxEnergy(player);
				data.setEnergy(Math.min(max, data.energy() + max * (FlaskRules.etherPercent(rank) / 100.0)));
				AbilityManager.sendEnergy(player, true);
				ParticleFx.spiral(player.getServerWorld(), player.getPos(), 0.6, 2.0, ParticleFx.dust(0x4FC3F7, 1.0f), 30, 2);
			}
			if (!player.isCreative()) setSpent(stack, rank, Math.max(0, stack.getDamage()) + 1);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_DRINK,
					SoundCategory.PLAYERS, 0.8f, kind == Kind.ESTUS ? 0.9f : 1.3f);
		}
		return stack;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return 24;
	}

	@Override
	public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		int rank = FlaskUpgrades.rank(stack);
		tooltip.add(Text.translatable("item.aptitudes.flask.charges", FlaskRules.remaining(rank, stack.getDamage()),
				FlaskRules.capacity(rank)).formatted(kind == Kind.ESTUS ? Formatting.GOLD : Formatting.AQUA));
		if (rank > 0) tooltip.add(Text.translatable("item.aptitudes.flask.rank", rank).formatted(Formatting.GOLD));
		tooltip.add(recovery(kind, rank).copy().formatted(Formatting.GRAY));
		if (kind == Kind.ESTUS) {
			tooltip.add(Text.translatable("item.aptitudes.flask.regeneration").formatted(Formatting.GRAY));
		}
		tooltip.add(Text.translatable("item.aptitudes.flask.refill").formatted(Formatting.DARK_GRAY));
	}

	public static Text recovery(Kind kind, int rank) {
		return kind == Kind.ESTUS
				? Text.translatable("item.aptitudes.flask.estus_recovery", FlaskRules.estusHealing(rank))
				: Text.translatable("item.aptitudes.flask.ether_recovery", FlaskRules.etherPercent(rank));
	}
}
