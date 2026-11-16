package dev.aptitudes.ability;

import dev.aptitudes.role.KeystoneRules;
import dev.aptitudes.role.KeystoneRules.Template;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cache per-player de Keystones.active() (v59, R2).
 *
 * <p>Invalida al cambiar rol principal/secundario o aprender S7 de una senda. Keystones.has() se
 * llama en varios puntos críticos (PathEffects.outgoing, PathEffects.incoming, AbilityManager);
 * sin caché, cada acceso recalcula 2 búsquedas de rol.</p>
 */
public final class CachedKeystones {
	private static final Map<UUID, CachedValue> CACHE = new HashMap<>();
	/** Mapa global id (role + path) → Template (v59, R3). */
	private static final Map<String, Template> TEMPLATE_INDEX = buildIndex();

	private static final class CachedValue {
		final Set<Template> value;
		final long generatedAt;

		CachedValue(Set<Template> value, long generatedAt) {
			this.value = value;
			this.generatedAt = generatedAt;
		}
	}

	private CachedKeystones() {
	}

	private static Map<String, Template> buildIndex() {
		Map<String, Template> out = new HashMap<>();
		for (Role role : Role.values()) {
			// Sendas 1–6 (S7 es la piedra angular, las demás son TreeRules)
			for (int path = 1; path <= 6; path++) {
				Template t = KeystoneRules.template(role.key(), path);
				if (t != null) {
					out.put(role.key() + "/" + path, t);
				}
			}
		}
		return out;
	}

	/**
	 * Plantillas activas: la piedra angular aprendida de la senda elegida en rol principal y
	 * secundario. Cachea por 1 tick o hasta invalidación por cambio de rol/senda.
	 */
	public static Set<Template> active(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		CachedValue cached = CACHE.get(id);
		long now = player.getServerWorld().getTime();

		if (cached != null && cached.generatedAt == now) {
			return EnumSet.copyOf(cached.value);
		}

		// Recalcular
		Set<Template> out = EnumSet.noneOf(Template.class);
		var data = RoleManager.get(player);
		for (Role role : new Role[] {data.primary(), data.secondary()}) {
			if (role == null) continue;
			int path = PathRules.chosenPath(data::rank, role.key());
			if (path <= 0 || data.rank(PathRules.nodeId(role.key(), path, PathRules.Step.S7)) <= 0) {
				continue;
			}
			Template t = TEMPLATE_INDEX.get(role.key() + "/" + path);
			if (t != null) out.add(t);
		}

		CACHE.put(id, new CachedValue(out, now));
		return out;
	}

	/** Invalida el caché al cambiar rol o aprender senda. */
	public static void invalidate(UUID player) {
		CACHE.remove(player);
	}

	/** Limpia al desconectar. */
	public static void forget(UUID player) {
		CACHE.remove(player);
	}

	/** Retorna la Template para un rol + senda (v59, R3). */
	public static Template template(String roleKey, int path) {
		return TEMPLATE_INDEX.get(roleKey + "/" + path);
	}
}
