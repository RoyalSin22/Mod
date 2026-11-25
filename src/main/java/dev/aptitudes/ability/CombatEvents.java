package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * V93: bus central de eventos de combate en el servidor. Un único punto detecta los lanzamientos aceptados y el daño que causan los
 * jugadores, y los reparte a los oyentes ({@link CombatTelemetry} y, a medida que se migren, los {@code *Kit}), en vez de que cada rol
 * enganche por su cuenta los mismos acontecimientos.
 *
 * <p><b>Alcance de una habilidad (<i>scope</i>).</b> {@code AbilityManager} abre un alcance mientras ejecuta la activa; todo el daño que
 * ocurre dentro (y en las tareas que esa activa programa con {@link AbilityState#schedule}, que conservan el alcance) se atribuye a esa
 * habilidad. Los oyentes se invocan dentro de un alcance <i>derivado</i> de profundidad +1: el daño que causen a su vez llega marcado como
 * derivado y con su profundidad, para que una pasiva pueda negarse a encadenar más allá de {@link #MAX_DEPTH}.
 *
 * <p>Limitación conocida: el daño de pasivas que no pasa por un oyente y se produce fuera de cualquier alcance se clasifica por su tipo de
 * daño (mágico, explosión…) como «derivado», y el cuerpo a cuerpo o el arco como vanilla.
 */
public final class CombatEvents {
	/** Profundidad máxima de encadenado: pasar de aquí es una cadena de pasivas que conviene cortar. */
	public static final int MAX_DEPTH = 3;

	/** Habilidad en curso: quién la lanza, cuál es, si es derivada y a qué profundidad de encadenado va. */
	public record Scope(UUID owner, String abilityId, boolean derived, int depth) {
	}

	public record DamageEvent(ServerPlayerEntity attacker, LivingEntity victim, float amount, String abilityId, boolean derived,
							  int depth, boolean pvp, boolean vanillaSource, int tick) {
	}

	public interface Listener {
		default void onCast(ServerPlayerEntity player, String abilityId, double energySpent) {
		}

		default void onDamage(DamageEvent event) {
		}

		default void onKill(ServerPlayerEntity player, LivingEntity victim, int tick) {
		}
	}

	private static final List<Listener> LISTENERS = new ArrayList<>();
	private static Scope current;

	private CombatEvents() {
	}

	public static void addListener(Listener listener) {
		LISTENERS.add(listener);
	}

	// ------------------------------------------------------------------ Alcance

	public static Scope current() {
		return current;
	}

	/** Abre el alcance de una activa lanzada por {@code owner}; devuelve el anterior para restaurarlo con {@link #restore}. */
	public static Scope enter(ServerPlayerEntity owner, String abilityId) {
		Scope previous = current;
		current = new Scope(owner.getUuid(), abilityId, false, 0);
		return previous;
	}

	public static void restore(Scope previous) {
		current = previous;
	}

	/** Envuelve una tarea diferida para que se ejecute en el alcance que había al programarla. */
	public static Runnable wrap(Scope captured, Runnable task) {
		if (captured == null) return task;
		return () -> {
			Scope previous = current;
			current = captured;
			try {
				task.run();
			} finally {
				current = previous;
			}
		};
	}

	// ------------------------------------------------------------------ Emisión

	/** Un lanzamiento aceptado: la habilidad se ejecutó y se cobró {@code energySpent}. */
	public static void castAccepted(ServerPlayerEntity player, String abilityId, double energySpent) {
		for (Listener l : LISTENERS) {
			try {
				l.onCast(player, abilityId, energySpent);
			} catch (RuntimeException e) {
				Aptitudes.LOGGER.error("Error en un oyente de combate (lanzamiento)", e);
			}
		}
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (damageTaken <= 0 || !(source.getAttacker() instanceof ServerPlayerEntity attacker)) return;
			if (entity == attacker || entity.getServer() == null) return;
			Scope scope = current != null && attacker.getUuid().equals(current.owner()) ? current : null;
			boolean vanilla = isVanillaSource(source);
			boolean derived = scope != null ? scope.derived() : !vanilla;
			String ability = scope != null ? scope.abilityId() : null;
			int depth = scope != null ? scope.depth() : 0;
			DamageEvent event = new DamageEvent(attacker, entity, damageTaken, ability, derived, depth,
					entity instanceof PlayerEntity, vanilla && scope == null, entity.getServer().getTicks());
			dispatchDamage(event, scope);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(source.getAttacker() instanceof ServerPlayerEntity attacker) || entity.getServer() == null) return;
			int tick = entity.getServer().getTicks();
			for (Listener l : LISTENERS) {
				try {
					l.onKill(attacker, entity, tick);
				} catch (RuntimeException e) {
					Aptitudes.LOGGER.error("Error en un oyente de combate (baja)", e);
				}
			}
		});
	}

	private static void dispatchDamage(DamageEvent event, Scope scope) {
		Scope previous = current;
		// Lo que causen los oyentes cuenta como derivado y un nivel más profundo.
		current = new Scope(event.attacker().getUuid(), event.abilityId(), true, Math.min(event.depth() + 1, MAX_DEPTH + 1));
		try {
			for (Listener l : LISTENERS) {
				try {
					l.onDamage(event);
				} catch (RuntimeException e) {
					Aptitudes.LOGGER.error("Error en un oyente de combate (daño)", e);
				}
			}
		} finally {
			current = previous;
		}
	}

	/** Golpe de cuerpo a cuerpo, flecha, tridente o proyectil lanzado: lo que haría un jugador sin rol. */
	static boolean isVanillaSource(DamageSource source) {
		return source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.ARROW) || source.isOf(DamageTypes.TRIDENT)
				|| source.isOf(DamageTypes.THROWN);
	}
}
