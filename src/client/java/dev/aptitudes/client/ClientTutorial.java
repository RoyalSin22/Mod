package dev.aptitudes.client;

import dev.aptitudes.network.TutorialPayload;
import dev.aptitudes.tutorial.TutorialView;

/** Copia visual recibida del servidor; nunca modifica el attachment ni concede recompensas. */
public final class ClientTutorial {
	private static TutorialView view;

	private ClientTutorial() {
	}

	public static void onPayload(TutorialPayload payload) { view = payload.view(); }
	public static TutorialView view() { return view; }
	public static void clear() { view = null; }
}
