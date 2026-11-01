package dev.aptitudes.mixin;

import dev.aptitudes.ability.AbilityState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Filtra la elección de objetivo de los mobs: esbirros y torretas nunca atacan
 * a jugadores ni entre sí, y los mobs sobornados ignoran a los jugadores.
 * Se intercepta {@code setTarget} porque todas las metas de IA basadas en
 * objetivos (ActiveTargetGoal, RevengeGoal…) pasan por él.
 *
 * <p>Limitación: los mobs con IA de "Brain" (piglins, hoglins, warden) no usan
 * siempre este método.</p>
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void aptitudes$filterTarget(LivingEntity target, CallbackInfo ci) {
		MobEntity self = (MobEntity) (Object) this;
		if (target != null && !self.getWorld().isClient() && AbilityState.shouldBlockTarget(self, target)) {
			ci.cancel();
		}
	}
}
