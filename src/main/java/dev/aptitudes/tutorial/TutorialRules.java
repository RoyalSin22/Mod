package dev.aptitudes.tutorial;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Orden, progreso y cobro del tutorial. Puro: no depende de Minecraft ni concede objetos. */
public final class TutorialRules {
	/** Claves guardadas: no renombrar; los pasos nuevos solo se añaden al final. */
	public enum Step {
		GUIDE("guide"), BONFIRE("bonfire"), PRIMARY_ROLE("primary_role"),
		ACTIVE("active"), COMBO("combo"), GUILD_CRAFT("guild_craft");

		private final String key;

		Step(String key) { this.key = key; }
		public String key() { return key; }
	}

	public static final List<Step> ORDER = List.of(Step.values());
	public static final int COMBO_GOAL = 5;

	/** Observaciones fuera de orden y cobros separados: una recompensa puede esperar a que haya espacio. */
	public record State(Set<Step> observed, Set<Step> rewarded, boolean skipped) {
		public State {
			EnumSet<Step> done = copy(observed);
			EnumSet<Step> paid = copy(rewarded);
			// Un cobro guardado prueba que el paso se completó; nunca se borra para volver a pagarlo.
			done.addAll(paid);
			observed = Set.copyOf(done);
			rewarded = Set.copyOf(paid);
		}
	}

	private TutorialRules() {
	}

	public static State empty() { return new State(Set.of(), Set.of(), false); }

	/** Reconoce hechos ya guardados sin inventar una habilidad usada, un combo o una fabricación. */
	public static State existingProgress(State state, boolean hasGuide, int soulLevel, boolean hasPrimaryRole) {
		if (hasGuide) state = observe(state, Step.GUIDE);
		if (soulLevel > 0) state = observe(state, Step.BONFIRE);
		if (hasPrimaryRole) state = observe(state, Step.PRIMARY_ROLE);
		return state;
	}

	/** Ignora claves desconocidas de archivos antiguos o corruptos, sin guardar índices de enum. */
	public static State restore(Collection<String> observed, Collection<String> rewarded, boolean skipped) {
		return new State(keys(observed), keys(rewarded), skipped);
	}

	public static State observe(State state, Step step) {
		if (state.skipped() || step == null || state.observed().contains(step)) return state;
		EnumSet<Step> observed = copy(state.observed());
		observed.add(step);
		return new State(observed, state.rewarded(), false);
	}

	/** Primer paso pendiente en el orden del tutorial; null si terminó o fue omitido. */
	public static Step next(State state) {
		if (!state.skipped()) for (Step step : ORDER) if (!state.observed().contains(step)) return step;
		return null;
	}

	/** Solo paga pasos observados cuyo prefijo ya se completó, incluso si se hicieron fuera de orden. */
	public static Step pendingReward(State state) {
		if (state.skipped()) return null;
		for (Step step : ORDER) {
			if (!state.observed().contains(step)) return null;
			if (!state.rewarded().contains(step)) return step;
		}
		return null;
	}

	/** Reserva un único cobro: pedir otra vez el mismo paso no cambia el estado. */
	public static State claim(State state, Step step) {
		if (step == null || step != pendingReward(state)) return state;
		EnumSet<Step> paid = copy(state.rewarded());
		paid.add(step);
		return new State(state.observed(), paid, state.skipped());
	}

	/** Omisión irreversible: no borra lo hecho ni habilita recompensas pendientes. */
	public static State skip(State state) {
		return state.skipped() ? state : new State(state.observed(), state.rewarded(), true);
	}

	/** Recompensa pequeña fija: una esmeralda por paso, nunca XP ni objetos de evolución. */
	public static int emeralds(Step step) { return step == null ? 0 : 1; }

	public static boolean comboReached(int count) { return count >= COMBO_GOAL; }

	private static EnumSet<Step> copy(Collection<Step> steps) {
		EnumSet<Step> out = EnumSet.noneOf(Step.class);
		if (steps != null) for (Step step : steps) if (step != null) out.add(step);
		return out;
	}

	private static Set<Step> keys(Collection<String> keys) {
		EnumSet<Step> out = EnumSet.noneOf(Step.class);
		if (keys != null) for (String key : keys) for (Step step : ORDER) if (step.key().equals(key)) out.add(step);
		return out;
	}
}
