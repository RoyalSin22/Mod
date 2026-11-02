package dev.aptitudes.ability;

import java.util.HashMap;
import java.util.Map;

/**
 * Tiempos de espera como "instante en que vuelve a estar disponible", medido en tiempo de juego
 * absoluto ({@code World#getTime}: se guarda con el mundo, no se reinicia con el servidor y no lo cambia
 * {@code /time set}). Clase pura (sin Minecraft) para poder probarla.
 */
public final class CooldownBook {
	/** Tope de espera aceptado al leer datos (1 día real = 1 728 000 ticks): protege de datos corruptos. */
	static final long MAX_COOLDOWN_TICKS = 24L * 60 * 60 * 20;
	/** Entradas máximas guardadas (evita que crezca sin límite si cambian las claves). */
	static final int MAX_ENTRIES = 256;

	private final Map<String, Long> readyAt;

	public CooldownBook() {
		this(new HashMap<>());
	}

	public CooldownBook(Map<String, Long> readyAt) {
		this.readyAt = new HashMap<>(readyAt);
	}

	/** Ticks que faltan para {@code key} (0 si está lista). Corrige esperas imposibles (reloj atrasado). */
	public long remaining(String key, long now) {
		Long ready = readyAt.get(key);
		if (ready == null) return 0;
		long left = ready - now;
		if (left <= 0) {
			readyAt.remove(key);
			return 0;
		}
		if (left > MAX_COOLDOWN_TICKS) { // p. ej., mundo copiado con otro reloj: no bloquear para siempre
			readyAt.remove(key);
			return 0;
		}
		return left;
	}

	public boolean ready(String key, long now) {
		return remaining(key, now) == 0;
	}

	/** Inicia la espera de {@code key} durante {@code ticks}. */
	public void start(String key, long now, long ticks) {
		if (ticks <= 0) {
			readyAt.remove(key);
			return;
		}
		readyAt.put(key, now + Math.min(ticks, MAX_COOLDOWN_TICKS));
		if (readyAt.size() > MAX_ENTRIES) {
			prune(now);
			// Tope estricto: si siguen vigentes demasiadas, se descartan las que vencen antes.
			while (readyAt.size() > MAX_ENTRIES) {
				String soonest = null;
				long min = Long.MAX_VALUE;
				for (Map.Entry<String, Long> e : readyAt.entrySet()) {
					if (!e.getKey().equals(key) && e.getValue() < min) {
						min = e.getValue();
						soonest = e.getKey();
					}
				}
				if (soonest == null) break;
				readyAt.remove(soonest);
			}
		}
	}

	/** Si {@code key} está lista, inicia su espera y devuelve true; si no, false. */
	public boolean tryStart(String key, long now, long ticks) {
		if (!ready(key, now)) return false;
		start(key, now, ticks);
		return true;
	}

	public void clear(String key) {
		readyAt.remove(key);
	}

	/** Elimina las esperas ya cumplidas. */
	public void prune(long now) {
		readyAt.values().removeIf(r -> r <= now);
	}

	/** Copia para guardar (solo esperas vigentes). */
	public Map<String, Long> snapshot(long now) {
		prune(now);
		return new HashMap<>(readyAt);
	}

	/** Copia sin podar (para el códec, que no conoce el instante actual). */
	public Map<String, Long> raw() {
		return new HashMap<>(readyAt);
	}

	public int size() {
		return readyAt.size();
	}
}
