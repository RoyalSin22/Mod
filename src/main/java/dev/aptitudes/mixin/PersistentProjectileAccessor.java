package dev.aptitudes.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Acceso a {@code setPierceLevel}, privado en 1.21.1 (Tiro con arco 50: flechas perforantes). */
@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileAccessor {
	@Invoker("setPierceLevel")
	void aptitudes$setPierceLevel(byte level);
}
