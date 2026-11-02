package dev.aptitudes.client.mixin;

import dev.aptitudes.client.ClientEclipse;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Eclipse (v34): durante la totalidad el sol se dibuja con la textura del sol eclipsado (anillo carmesí).
 * El primer {@code setShaderTexture} de {@code renderSky} es el del sol. {@code require = 0}: si no
 * coincidiera, se dibuja el sol normal y el resto del evento sigue funcionando.
 */
@Mixin(WorldRenderer.class)
public abstract class EclipseSunMixin {
	@ModifyArg(method = "renderSky", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V",
			ordinal = 0), index = 1, require = 0)
	private Identifier aptitudes$eclipseSun(Identifier texture) {
		return ClientEclipse.eclipsedSun() ? ClientEclipse.ECLIPSE_SUN : texture;
	}
}
