package dev.aptitudes.ability;

import dev.aptitudes.role.PathEffect;
import dev.aptitudes.role.RoleTree;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.EvolvedTree;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cache per-player de PathEffects.all() (v59, R1).
 *
 * <p>Invalida al cambiar rol principal/secundario (RoleManager.setRole) o aprender una senda/pasiva
 * (RoleManager.upgrade). PathEffects.applyAttributes() se llama cada 20 ticks en RolePassives;
 * sin caché, recalcula 4+ mapas por tick × jugadores.</p>
 */
public final class CachedPathEffects {
	private static final Map<UUID, CachedValue> CACHE = new HashMap<>();

	private static final class CachedValue {
		final EnumMap<PathEffect, Double> value;
		final long generatedAt;

		CachedValue(EnumMap<PathEffect, Double> value, long generatedAt) {
			this.value = value;
			this.generatedAt = generatedAt;
		}
	}

	private CachedPathEffects() {
	}

	/**
	 * Retorna los efectos de senda del jugador. Cachea por 1 tick o hasta invalidación por cambio
	 * de rol.
	 */
	public static EnumMap<PathEffect, Double> all(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		CachedValue cached = CACHE.get(id);
		long now = player.getServerWorld().getTime();

		if (cached != null && cached.generatedAt == now) {
			return new EnumMap<>(cached.value);
		}

		// Recalcular
		EnumMap<PathEffect, Double> out = new EnumMap<>(PathEffect.class);
		for (Map.Entry<String, RoleTree.PathSpec> e : RoleTree.pathSpecs().entrySet()) {
			int rank = RoleManager.activeRank(player, e.getKey());
			if (rank > 0) out.merge(e.getValue().effect(), rank * e.getValue().perRank(), Double::sum);
		}
		for (EvolvedTree.Passive p : EvolvedTree.passives().values()) {
			int rank = RoleManager.learnedRank(player, p.id());
			if (rank > 0) out.merge(p.effect(), rank * p.perRank(), Double::sum);
		}

		CACHE.put(id, new CachedValue(out, now));
		return out;
	}

	/** Invalida el caché al cambiar rol. */
	public static void invalidate(UUID player) {
		CACHE.remove(player);
	}

	/** Limpia al desconectar. */
	public static void forget(UUID player) {
		CACHE.remove(player);
	}
}
