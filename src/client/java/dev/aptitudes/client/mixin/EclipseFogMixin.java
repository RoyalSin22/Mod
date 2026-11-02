package dev.aptitudes.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.client.ClientEclipse;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Eclipse (v34): color de fondo y de la niebla hacia rojo oscuro.
 * <ul>
 *   <li>Fondo: se modifican los argumentos de {@code RenderSystem.clearColor} dentro de {@code render}.</li>
 *   <li>Niebla: tras {@code applyFogColor} se vuelve a fijar el color de niebla del shader, ya teñido.</li>
 * </ul>
 * Sin {@code @Shadow} (un campo inexistente tumbaría el arranque) y con {@code require = 0} en cada inyector:
 * si algún nombre no coincide, el juego arranca igual y solo se pierde ese tinte.
 */
@Mixin(BackgroundRenderer.class)
public abstract class EclipseFogMixin {
	@ModifyArgs(method = "render", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"), require = 0)
	private static void aptitudes$eclipseClearColor(Args args) {
		if (!ClientEclipse.active()) return;
		float[] tinted = ClientEclipse.tintFog(args.get(0), args.get(1), args.get(2), 1f);
		args.set(0, tinted[0]);
		args.set(1, tinted[1]);
		args.set(2, tinted[2]);
	}

	@Inject(method = "applyFogColor", at = @At("TAIL"), require = 0)
	private static void aptitudes$eclipseFogColor(CallbackInfo ci) {
		if (!ClientEclipse.active()) return;
		float[] fog = RenderSystem.getShaderFogColor();
		if (fog == null || fog.length < 4) return;
		float[] tinted = ClientEclipse.tintFog(fog[0], fog[1], fog[2], 1f);
		RenderSystem.setShaderFogColor(tinted[0], tinted[1], tinted[2], fog[3]);
	}
}
