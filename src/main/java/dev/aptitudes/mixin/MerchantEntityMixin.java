package dev.aptitudes.mixin;

import dev.aptitudes.ability.RolePassives;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric API 1.21.1 no tiene evento de "comercio completado". {@code trade} se
 * llama una vez por cada intercambio realizado con un aldeano o comerciante.
 */
@Mixin(MerchantEntity.class)
public abstract class MerchantEntityMixin {

	@Inject(method = "trade", at = @At("TAIL"))
	private void aptitudes$onTrade(TradeOffer offer, CallbackInfo ci) {
		MerchantEntity self = (MerchantEntity) (Object) this;
		if (!self.getWorld().isClient() && self.getCustomer() instanceof ServerPlayerEntity player) {
			RolePassives.onTrade(player);
		}
	}
}
