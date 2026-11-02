package dev.aptitudes.client.mixin;

import dev.aptitudes.client.ClientEclipse;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Eclipse (v34): tiñe de carmesí el color del cielo y de las nubes según la intensidad del evento.
 * {@code require = 0}: si el método no existiera con este nombre, el juego arranca igual y solo se pierde el tinte.
 */
@Mixin(ClientWorld.class)
public abstract class EclipseSkyMixin {
	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void aptitudes$eclipseSky(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (ClientEclipse.active()) cir.setReturnValue(ClientEclipse.tintSky(cir.getReturnValue(), tickDelta));
	}

	@Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true, require = 0)
	private void aptitudes$eclipseClouds(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		if (ClientEclipse.active()) cir.setReturnValue(ClientEclipse.tintClouds(cir.getReturnValue(), tickDelta));
	}
}
