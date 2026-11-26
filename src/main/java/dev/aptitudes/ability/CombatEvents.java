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

	/**
	 * {@code autonomous}: el golpe lo dio una invocación o una mascota del jugador ({@code attacker} es su dueño), no el propio jugador.
	 */
	public record DamageEvent(ServerPlayerEntity attacker, LivingEntity victim, float amount, String abilityId, boolean derived,
							  int depth, boolean pvp, boolean vanillaSource, boolean autonomous, int tick) {
	}

	public interface Listener {
		default void onCast(ServerPlayerEntity player, String abilityId, double energySpent) {
		}

		default void onDamage(DamageEvent event) {
		}

		default void onKill(ServerPlayerEntity player, LivingEntity victim, int tick) {
		}

		/** Una curación aplicada con {@link CombatEvents#heal}: {@code effective} es lo que subió la vida, {@code over} lo que sobró. */
		default void onHeal(ServerPlayerEntity healer, LivingEntity target, double effective, double over, int tick) {
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

	/**
	 * V95: cura a {@code target} y avisa a los oyentes de cuánto fue curación efectiva y cuánto sobrecuración. Es el punto común para las
	 * habilidades de curación; si no se usa, la curación funciona igual pero no cuenta en la telemetría ni da señal.
	 *
	 * @return la curación efectiva (puntos de vida que subieron de verdad)
	 */
	public static double heal(ServerPlayerEntity healer, LivingEntity target, float amount) {
		float before = target.getHealth();
		target.heal(amount);
		double effective = dev.aptitudes.logic.ImpactFeedbackRules.effectiveHeal(before, target.getMaxHealth(), amount);
		double over = dev.aptitudes.logic.ImpactFeedbackRules.overheal(before, target.getMaxHealth(), amount);
		if (healer.getServer() == null) return effective;
		int tick = healer.getServer().getTicks();
		for (Listener l : LISTENERS) {
			try {
				l.onHeal(healer, target, effective, over, tick);
			} catch (RuntimeException e) {
				Aptitudes.LOGGER.error("Error en un oyente de combate (curación)", e);
			}
		}
		return effective;
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (damageTaken <= 0) return;
			ServerPlayerEntity attacker = ownerOf(source.getAttacker());
			if (attacker == null || entity == attacker || entity.getServer() == null) return;
			boolean autonomous = source.getAttacker() != attacker;
			Scope scope = !autonomous && current != null && attacker.getUuid().equals(current.owner()) ? current : null;
			boolean vanilla = !autonomous && isVanillaSource(source);
			boolean derived = !autonomous && (scope != null ? scope.derived() : !vanilla);
			String ability = scope != null ? scope.abilityId() : null;
			int depth = scope != null ? scope.depth() : 0;
			DamageEvent event = new DamageEvent(attacker, entity, damageTaken, ability, derived, depth,
					entity instanceof PlayerEntity, vanilla && scope == null, autonomous, entity.getServer().getTicks());
			dispatchDamage(event, scope);
		});
		// V94: política de invocaciones. Si el dueño muere, sus invocaciones se retiran; si cambia de dimensión, las que quedaron atrás.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity dead) AbilityState.discardSummonsOf(dead, null);
		});
		net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
				(player, origin, destination) -> AbilityState.discardSummonsOf(player, destination.getRegistryKey()));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			ServerPlayerEntity attacker = ownerOf(source.getAttacker());
			if (attacker == null || entity.getServer() == null) return;
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

	/**
	 * V94: el jugador al que se atribuye un golpe: el propio jugador, el dueño de una invocación registrada ({@link AbilityState}) o el
	 * dueño de una mascota domesticada. {@code null} si el atacante no es de ningún jugador.
	 */
	static ServerPlayerEntity ownerOf(net.minecraft.entity.Entity attacker) {
		if (attacker instanceof ServerPlayerEntity player) return player;
		if (attacker == null) return null;
		if (AbilityState.isTracked(attacker)) return AbilityState.ownerOf(attacker);
		if (attacker instanceof net.minecraft.entity.passive.TameableEntity pet && pet.getOwner() instanceof ServerPlayerEntity owner) return owner;
		return null;
	}

	/** Golpe de cuerpo a cuerpo, flecha, tridente o proyectil lanzado: lo que haría un jugador sin rol. */
	static boolean isVanillaSource(DamageSource source) {
		return source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.ARROW) || source.isOf(DamageTypes.TRIDENT)
				|| source.isOf(DamageTypes.THROWN);
	}
}
