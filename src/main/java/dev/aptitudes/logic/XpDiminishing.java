package dev.aptitudes.logic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rendimiento decreciente de XP por fuente repetida (anti-granjas y anti-AFK).
 *
 * <p>Cada par (jugador, fuente) guarda los instantes de sus últimos eventos de XP. Si en la
 * ventana deslizante hay más de {@code threshold} eventos, la XP se multiplica por
 * {@code threshold / eventos}, con un mínimo de {@code floor}. Ejemplo con umbral 20:
 * 20 eventos/min → 100 %, 40 → 50 %, 200 → 10 % (con suelo 0.1).</p>
 *
 * <p>Clase pura (sin Minecraft) para poder probarla con pruebas unitarias.</p>
 */
public final class XpDiminishing {
	/** Tope de eventos guardados por fuente: acota la memoria aunque una granja dispare miles. */
	static final int MAX_EVENTS_PER_SOURCE = 1024;

	private final Map<UUID, Map<String, Deque<Long>>> events = new HashMap<>();

	/** Multiplicador para {@code recentEvents} eventos (incluido el actual) en la ventana. */
	public static double multiplier(int recentEvents, int threshold, double floor) {
		if (threshold <= 0 || recentEvents <= threshold) return 1.0;
		double f = Math.max(0.0, Math.min(1.0, floor));
		return Math.max(f, (double) threshold / recentEvents);
	}

	/** Aplica el multiplicador a una cantidad de XP (redondeo al entero más cercano, nunca negativo). */
	public static int apply(int amount, double multiplier) {
		if (amount <= 0) return 0;
		return (int) Math.max(0L, Math.round(amount * Math.max(0.0, Math.min(1.0, multiplier))));
	}

	/**
	 * Registra un evento de {@code source} en el instante {@code now} (ticks) y devuelve cuántos
	 * eventos de esa fuente hay en la ventana {@code (now - window, now]}, contando este.
	 */
	public int record(UUID player, String source, long now, long window) {
		Deque<Long> queue = events.computeIfAbsent(player, k -> new HashMap<>())
				.computeIfAbsent(source, k -> new ArrayDeque<>());
		long from = now - Math.max(1L, window);
		while (!queue.isEmpty() && (queue.peekFirst() <= from || queue.peekFirst() > now)) queue.pollFirst();
		queue.addLast(now);
		while (queue.size() > MAX_EVENTS_PER_SOURCE) queue.pollFirst();
		return queue.size();
	}

	/** Registra el evento y devuelve la XP ya reducida. */
	public int scale(UUID player, String source, int amount, long now, long window, int threshold, double floor) {
		int count = record(player, source, now, window);
		return apply(amount, multiplier(count, threshold, floor));
	}

	public void forget(UUID player) {
		events.remove(player);
	}

	int trackedPlayers() {
		return events.size();
	}
}
