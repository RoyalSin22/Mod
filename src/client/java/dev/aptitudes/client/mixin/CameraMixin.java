package dev.aptitudes.client.mixin;

import dev.aptitudes.client.hud.ClientFx;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Temblor de cámara: suma un pequeño desplazamiento aleatorio a la rotación tras calcularla. */
@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	public abstract float getYaw();

	@Shadow
	public abstract float getPitch();

	@Inject(method = "update", at = @At("TAIL"), require = 0)
	private void aptitudes$shake(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
								 float tickDelta, CallbackInfo ci) {
		float yaw = ClientFx.shakeYaw();
		float pitch = ClientFx.shakePitch();
		if (yaw != 0 || pitch != 0) setRotation(getYaw() + yaw, getPitch() + pitch);
	}
}
