package dev.aptitudes.client.mixin;

import dev.aptitudes.client.ClientMorphs;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dibuja el mob de la transformación en lugar del jugador. Se intercepta el
 * despachador general, de modo que funciona en tercera persona y para los demás
 * jugadores; el propio jugador en primera persona sigue viendo sus manos.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void aptitudes$renderMorph(E entity, double x, double y, double z, float yaw,
														   float tickDelta, MatrixStack matrices,
														   VertexConsumerProvider vertexConsumers, int light,
														   CallbackInfo ci) {
		if (!(entity instanceof AbstractClientPlayerEntity player)) return;
		LivingEntity fake = ClientMorphs.fakeFor(player);
		if (fake == null || aptitudes$broken.contains(fake.getType())) return;
		try {
			((EntityRenderDispatcher) (Object) this).render(fake, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
			ci.cancel();
		} catch (RuntimeException e) {
			// Un renderizador de la forma que falla (p. ej., de otro mod) no debe cerrar el juego: se avisa una vez y se
			// dibuja el jugador normal. Ese tipo no se vuelve a intentar en la sesión (ni el error ni el coste se repiten
			// en cada fotograma).
			if (aptitudes$broken.add(fake.getType())) {
				dev.aptitudes.Aptitudes.LOGGER.error("[Aptitudes] No se pudo dibujar la forma {}; se muestra el jugador",
						fake.getType(), e);
			}
		}
	}

	private static final java.util.Set<net.minecraft.entity.EntityType<?>> aptitudes$broken = new java.util.HashSet<>();
}
