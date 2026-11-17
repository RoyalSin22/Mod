package dev.aptitudes.client.fx;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * V77: calendario de pistas de los efectos del cliente (la onda que se expande, las grietas que avanzan). Avanza un
 * paso por tick del cliente y se detiene con el juego en pausa. Una pista que falla no tumba el cliente.
 */
public final class FxTimeline {
	private record Task(long due, Runnable run) {}

	private static final int MAX_TASKS = 1500;
	private static final List<Task> TASKS = new ArrayList<>();
	private static long tick;
	private static int failures;

	private FxTimeline() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.isPaused()) return;
			tick++;
			if (TASKS.isEmpty()) return;
			List<Task> due = new ArrayList<>();
			for (var it = TASKS.iterator(); it.hasNext(); ) {
				Task t = it.next();
				if (t.due() <= tick) {
					due.add(t);
					it.remove();
				}
			}
			for (Task t : due) {
				try {
					t.run().run();
				} catch (RuntimeException e) {
					// Un efecto visual nunca debe tumbar el cliente; se avisa unas pocas veces para no inundar el registro.
					if (failures++ < 5) Aptitudes.LOGGER.warn("[Aptitudes] Falló una pista de efectos", e);
				}
			}
		});
	}

	/** Ejecuta {@code task} dentro de {@code ticks} ticks (0 = ya). */
	public static void later(int ticks, Runnable task) {
		if (ticks <= 0) {
			task.run();
			return;
		}
		if (TASKS.size() >= MAX_TASKS) return;
		TASKS.add(new Task(tick + ticks, task));
	}

	/** Ejecuta {@code body(i)} para i = 0 … times−1, una vez cada {@code interval} ticks, empezando ahora. */
	public static void every(int times, int interval, IntConsumer body) {
		for (int i = 0; i < times; i++) {
			int index = i;
			later(i * Math.max(1, interval), () -> body.accept(index));
		}
	}

	public static void clear() {
		TASKS.clear();
	}
}
