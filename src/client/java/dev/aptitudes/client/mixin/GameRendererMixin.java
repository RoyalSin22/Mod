package dev.aptitudes.client.mixin;

import dev.aptitudes.client.fx.ScreenDynamics;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** V77: pulso del campo de visión de los efectos de batalla. Solo el del mundo, no el de la mano. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
	private void aptitudes$fovPunch(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		if (!changingFov) return;
		float offset = ScreenDynamics.fovOffset();
		if (offset != 0f) cir.setReturnValue(cir.getReturnValue() + offset);
	}
}
