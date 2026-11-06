package dev.aptitudes.tutorial;

/** Vista acotada del progreso; no concede recompensas ni acepta observaciones del cliente. */
public record TutorialView(int nextStep, int completed, int rewarded, boolean skipped) {
	public TutorialView {
		int total = TutorialRules.ORDER.size();
		nextStep = nextStep >= 0 && nextStep < total ? nextStep : -1;
		completed = Math.max(0, Math.min(total, completed));
		rewarded = Math.max(0, Math.min(completed, rewarded));
	}

	public static TutorialView from(TutorialRules.State state) {
		TutorialRules.Step next = TutorialRules.next(state);
		return new TutorialView(next == null ? -1 : TutorialRules.ORDER.indexOf(next),
				state.observed().size(), state.rewarded().size(), state.skipped());
	}

	public int pendingRewards() { return skipped ? 0 : completed - rewarded; }
	public boolean visible() { return !skipped && (nextStep >= 0 || pendingRewards() > 0); }
}
