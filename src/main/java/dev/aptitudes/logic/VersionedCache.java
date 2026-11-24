package dev.aptitudes.logic;

import java.util.function.Function;

/**
 * Caché de un único valor validada por número de versión (C2, R1–R2). Clase pura (sin Minecraft) y probada.
 *
 * <p>El dueño de los datos lleva un contador que sube en cada mutación; el valor guardado solo sirve mientras el
 * contador no cambie. Así la invalidación está en un único sitio (los mutadores del dueño) y no depende de
 * enganchar eventos (aprender, reiniciar, morir…): si los datos cambian, la versión cambia y se recalcula.</p>
 *
 * <p>No es segura entre hilos: cada instancia pertenece a un objeto que solo usa el hilo del servidor.</p>
 */
public final class VersionedCache<T> {
	private boolean filled;
	private long version;
	private T value;

	/**
	 * Valor vigente para {@code current}; si no hay o se calculó con otra versión, lo recalcula con
	 * {@code compute.apply(context)}. Se pasa el contexto aparte para no crear una lambda en cada llamada.
	 */
	public <C> T get(long current, C context, Function<? super C, ? extends T> compute) {
		if (!filled || version != current) {
			value = compute.apply(context);
			version = current;
			filled = true;
		}
		return value;
	}

	/** ¿Hay un valor calculado con la versión {@code current}? */
	public boolean isValidFor(long current) {
		return filled && version == current;
	}

	/** Olvida el valor (el siguiente {@link #get} recalcula). */
	public void invalidate() {
		filled = false;
		value = null;
	}
}
