package dev.aptitudes.item.weapon;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.ability.Targeting;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleResources;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Foco de evolución de un rol mágico o a distancia: disparo instantáneo del color del
 * rol que gasta maná. Daño 7 (Maestro) o 10 (Definitivo) + 0,2 por nivel del rol, solo
 * con el rol activo. El del Elementalista aplica además el efecto del elemento activo.
 */
public class RoleFocusItem extends Item {
	private final Role role;
	private final int tier;

	public RoleFocusItem(Role role, int tier, Settings settings) {
		super(settings);
		this.role = role;
		this.tier = tier;
	}

	public Role role() {
		return role;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient() || !(user instanceof ServerPlayerEntity player)) return TypedActionResult.success(stack, true);
		RoleData data = RoleManager.get(player);
		if (!data.isActive(role)) {
			player.sendMessage(Text.translatable("item.aptitudes.role_weapon.needs_role",
					Text.translatable(role.translationKey())).formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}
		double cost = tier >= Evolution.ULTIMATE ? 10 : 8;
		if (!AbilityManager.tryConsume(player, cost)) return TypedActionResult.fail(stack);
		player.getItemCooldownManager().set(this, tier >= Evolution.ULTIMATE ? 10 : 12);

		LivingEntity target = Targeting.raycastLiving(player, 32);
		Vec3d from = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.5)).add(0, -0.15, 0);
		Vec3d to = target != null ? target.getPos().add(0, target.getHeight() / 2, 0) : Targeting.lookEnd(player, 32);
		var sworld = player.getServerWorld();
		// Efectos: anillo en la boca, rayo del color del rol e impacto
		ParticleFx.ring(sworld, from, 0.35, ParticleFx.dust(role.color(), 0.9f), 10);
		Targeting.particleLine(sworld, ParticleFx.dust(role.color(), 1.1f), from, to);
		sworld.spawnParticles(ParticleTypes.ENCHANTED_HIT, to.x, to.y, to.z, 12, 0.3, 0.3, 0.3, 0.2);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
				SoundCategory.PLAYERS, 0.8f, tier >= Evolution.ULTIMATE ? 0.9f : 1.3f);

		if (target != null) {
			float damage = (tier >= Evolution.ULTIMATE ? 10f : 7f) + data.level(role) * 0.2f;
			target.damage(player.getDamageSources().indirectMagic(player, player), damage);
			if (role == Role.ELEMENTALIST) {
				Element element = data.activeElement();
				if (element != null) sworld.spawnParticles(element.particle(), to.x, to.y, to.z, 15, 0.4, 0.4, 0.4, 0.05);
			}
			RoleManager.addRoleXp(player, role, 2);
			RoleResources.gain(player, role, 3);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.role_weapon.role", Text.translatable(role.translationKey()))
				.withColor(role.color()));
		tooltip.add(Text.translatable("item.aptitudes.role_weapon.focus", tier >= Evolution.ULTIMATE ? 10 : 7,
				tier >= Evolution.ULTIMATE ? 10 : 8).formatted(Formatting.GRAY));
	}
}
