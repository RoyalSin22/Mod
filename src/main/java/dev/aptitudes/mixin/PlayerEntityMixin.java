package dev.aptitudes.mixin;

import dev.aptitudes.logic.SkillEffects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Único mixin del mod: Fabric API no ofrece un evento para modificar la
 * velocidad de rotura. Se inyecta al final (RETURN) y multiplica el resultado,
 * de modo que se compone con otros mods que también lo modifiquen.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
	private void aptitudes$applySkillBreakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		float multiplier = SkillEffects.breakSpeedMultiplier((PlayerEntity) (Object) this, state);
		if (multiplier != 1.0f) {
			cir.setReturnValue(cir.getReturnValueF() * multiplier);
		}
	}
}
