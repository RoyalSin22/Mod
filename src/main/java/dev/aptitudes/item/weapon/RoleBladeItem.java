package dev.aptitudes.item.weapon;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.ability.Targeting;
import dev.aptitudes.forge.ForgeWeapons;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleResources;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Hoja de evolución de un rol cuerpo a cuerpo. Con el rol activo, cada golpe deja un
 * barrido del color del rol y carga su recurso; la definitiva además cura un poco.
 */
public class RoleBladeItem extends SwordItem {
	private final Role role;
	private final int tier;

	public RoleBladeItem(Role role, int tier, ToolMaterial material, Settings settings) {
		super(material, settings);
		this.role = role;
		this.tier = tier;
	}

	public Role role() {
		return role;
	}

	public int tier() {
		return tier;
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof ServerPlayerEntity player && RoleManager.get(player).isActive(role)) {
			var world = player.getServerWorld();
			world.spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getBodyY(0.6), target.getZ(), 1, 0, 0, 0, 0);
			world.spawnParticles(ParticleFx.dust(role.color(), 1.2f), target.getX(), target.getBodyY(0.6), target.getZ(),
					10, 0.35, 0.35, 0.35, 0);
			RoleResources.gain(player, role, ForgeWeapons.impactGain(stack, player, Targeting.isHostileImpactTarget(player, target)));
			if (tier >= Evolution.ULTIMATE && player.getHealth() < player.getMaxHealth()) player.heal(0.5f);
		}
		return super.postHit(stack, target, attacker);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(dev.aptitudes.item.RoleGlyphs.line(role,
				Text.translatable("item.aptitudes.role_weapon.role", Text.translatable(role.translationKey()))));
		tooltip.add(Text.translatable(tier >= Evolution.ULTIMATE ? "item.aptitudes.role_weapon.blade_ultimate"
				: "item.aptitudes.role_weapon.blade_master").formatted(Formatting.GRAY));
	}
}
