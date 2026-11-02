package dev.aptitudes.mixin;

import dev.aptitudes.logic.SkillEffects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixins del jugador. Velocidad de rotura: Fabric API no ofrece un evento para modificar la
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

	/**
	 * v26: caja de colisión de la forma (transformaciones). {@code require = 0}: si el nombre del método no
	 * existiera en esta versión, el juego arranca igual y {@code MorphSize.refresh} lo avisa en el registro.
	 */
	@Inject(method = "getBaseDimensions", at = @At("HEAD"), cancellable = true, require = 0)
	private void aptitudes$morphDimensions(net.minecraft.entity.EntityPose pose,
										   CallbackInfoReturnable<net.minecraft.entity.EntityDimensions> cir) {
		net.minecraft.entity.EntityDimensions dims = dev.aptitudes.morph.MorphSize.dimensions((PlayerEntity) (Object) this, pose);
		if (dims != null) cir.setReturnValue(dims);
	}
}
