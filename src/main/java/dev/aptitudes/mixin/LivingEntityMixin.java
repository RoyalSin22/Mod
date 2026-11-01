package dev.aptitudes.mixin;

import dev.aptitudes.ability.RolePassives;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fabric API (1.21.1) no tiene un evento para <em>modificar</em> el daño, solo
 * para permitirlo/cancelarlo. Este mixin ajusta el argumento {@code amount} al
 * principio de {@code damage}, antes de armadura y encantamientos, igual que
 * haría un efecto de Fuerza.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float aptitudes$modifyIncomingAmount(float amount, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		float modified = RolePassives.modifyOutgoingDamage(self, source, amount);
		return dev.aptitudes.unique.UniqueEffects.modifyDamage(self, source, modified);
	}
}
