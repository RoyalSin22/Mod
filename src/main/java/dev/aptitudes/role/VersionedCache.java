package dev.aptitudes.role;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * C2 (rendimiento R1/R2): caché por jugador que se invalida sola. Guarda el valor junto con la <b>identidad</b> del
 * objeto de origen (p. ej. el {@code RoleData} del attachment) y su <b>versión</b>: si el objeto se sustituye (reinicio,
 * copia al morir) o su versión cambia, el valor se recalcula. Así no hace falta acordarse de invalidar en cada sitio
 * que modifica el estado, solo de subir la versión en sus setters.
 *
 * <p>Pura (sin Minecraft) y sin sincronización: solo se usa desde el hilo del servidor. Los valores devueltos se
 * comparten entre llamadas: quien los recibe no debe modificarlos. Hay que llamar a {@link #forget} al desconectarse
 * para no retener datos de jugadores que ya no están.</p>
 */
public final class VersionedCache<T> {
	private record Entry<T>(Object owner, int version, T value) {}

	private final Map<UUID, Entry<T>> entries = new HashMap<>();

	/** Devuelve el valor en caché si {@code owner} y {@code version} coinciden; si no, lo calcula y lo guarda. */
	public T get(UUID id, Object owner, int version, Supplier<T> compute) {
		Entry<T> hit = entries.get(id);
		if (hit != null && hit.owner() == owner && hit.version() == version) return hit.value();
		T value = compute.get();
		entries.put(id, new Entry<>(owner, version, value));
		return value;
	}

	public void forget(UUID id) {
		entries.remove(id);
	}

	public int size() {
		return entries.size();
	}
}
