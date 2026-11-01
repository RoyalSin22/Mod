package dev.aptitudes.ability;

import net.minecraft.server.network.ServerPlayerEntity;

/** Habilidad activa. */
@FunctionalInterface
public interface Ability {
	/**
	 * @param rank rango aprendido (1-3); escala la potencia
	 * @return {@code true} si tuvo efecto (se cobra energía y enfriamiento)
	 */
	boolean use(ServerPlayerEntity player, int rank);
}
