package dev.aptitudes.item;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.Targeting;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Arma de impacto instantáneo (rayo) que gasta energía: Bastón arcano (Mago),
 * Pistola de plasma (Ingeniero) y Tomo nigromántico (Nigromante). Su daño crece con el nivel del rol asociado
 * si ese rol está activo.
 */
public class HitscanWeaponItem extends Item {
	public enum Kind {
		ARCANE(Role.MAGE, 24.0, 4.0f, 8.0, 14, 0.0f, ParticleTypes.WITCH, SoundEvents.ENTITY_EVOKER_CAST_SPELL),
		PLASMA(Role.ENGINEER, 32.0, 5.0f, 10.0, 10, 0.0f, ParticleTypes.ELECTRIC_SPARK, SoundEvents.BLOCK_BEACON_DEACTIVATE),
		/** Tomo nigromántico: rayo de almas que roba el 30 % del daño como vida. */
		NECROTIC(Role.NECROMANCER, 20.0, 4.0f, 10.0, 16, 0.3f, ParticleTypes.SOUL, SoundEvents.ENTITY_WITHER_AMBIENT);

		final Role role;
		final double range;
		final float baseDamage;
		final double energyCost;
		final int cooldownTicks;
		final float lifesteal;
		final ParticleEffect particle;
		final SoundEvent sound;

		Kind(Role role, double range, float baseDamage, double energyCost, int cooldownTicks, float lifesteal,
			 ParticleEffect particle, SoundEvent sound) {
			this.role = role;
			this.range = range;
			this.baseDamage = baseDamage;
			this.energyCost = energyCost;
			this.cooldownTicks = cooldownTicks;
			this.lifesteal = lifesteal;
			this.particle = particle;
			this.sound = sound;
		}
	}

	private final Kind kind;

	public HitscanWeaponItem(Kind kind, Settings settings) {
		super(settings);
		this.kind = kind;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient() || !(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.success(stack, world.isClient());
		}
		if (!AbilityManager.tryConsume(player, kind.energyCost)) return TypedActionResult.fail(stack);

		player.getItemCooldownManager().set(this, kind.cooldownTicks);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), kind.sound, SoundCategory.PLAYERS, 0.7f, 1.4f);

		LivingEntity target = Targeting.raycastLiving(player, kind.range);
		Vec3d from = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.5)).add(0, -0.15, 0);
		Vec3d to = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.lookEnd(player, kind.range);
		Targeting.particleLine(player.getServerWorld(), kind.particle, from, to);

		if (target != null) {
			var data = RoleManager.get(player);
			int level = data.isActive(kind.role) ? data.level(kind.role) : 0;
			float damage = kind.baseDamage + level * 0.15f;
			boolean hurt = target.damage(player.getDamageSources().indirectMagic(player, player), damage);
			if (hurt && kind.lifesteal > 0) player.heal(damage * kind.lifesteal);
			RoleManager.addRoleXp(player, kind.role, 2);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.hitscan.tooltip", (int) kind.energyCost,
				Text.translatable(kind.role.translationKey())).formatted(Formatting.GRAY));
	}
}
