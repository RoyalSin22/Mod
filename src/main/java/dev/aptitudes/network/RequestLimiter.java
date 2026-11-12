package dev.aptitudes.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de frecuencia por jugador y por acción para los payloads cliente → servidor (v66, auditoría). Un cliente
 * modificado puede enviar cientos de peticiones por segundo; las acciones que construyen un paquete de respuesta, abren
 * una pantalla o avisan a otro jugador se limitan con esto. Lógica pura (sin Minecraft): el tiempo lo aporta quien llama.
 */
public final class RequestLimiter {
	private final Map<String, Long> last = new ConcurrentHashMap<>();

	/**
	 * @param player      jugador que pide
	 * @param action      nombre de la acción (cada una tiene su propio contador)
	 * @param now         tiempo actual en ticks (si retrocede, p. ej. al recargar el mundo, se acepta y se reinicia)
	 * @param minInterval ticks mínimos entre dos peticiones aceptadas de la misma acción
	 * @return verdadero si la petición se acepta (y queda registrada); falso si llega demasiado pronto
	 */
	public boolean tryAcquire(UUID player, String action, long now, long minInterval) {
		String id = player + "/" + action;
		Long previous = last.get(id);
		if (previous != null && now >= previous && now - previous < Math.max(0L, minInterval)) return false;
		last.put(id, now);
		return true;
	}

	/** Olvida al jugador al desconectar para no acumular entradas. */
	public void forget(UUID player) {
		String prefix = player + "/";
		last.keySet().removeIf(key -> key.startsWith(prefix));
	}

	int size() {
		return last.size();
	}
}
