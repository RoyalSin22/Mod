package dev.aptitudes.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fase F5 del plan de avance: expediciones de Amenaza. Puro (sin Minecraft).
 *
 * <p>Una expedición es una secuencia corta (3 a 5 módulos hechos a mano) que siempre empieza con combate y termina con
 * un jefe. Los módulos del centro (puzle, élite y evento) se eligen con una semilla determinista, para que todos los
 * jugadores de un servidor vean la misma expedición. Los modificadores también salen de la semilla y se toman de un
 * catálogo fijo. Sin generación procedural libre.</p>
 */
public final class ExpeditionRules {
	/** Módulos de una expedición. Se añaden al final: el orden guarda índices. */
	public enum Module { COMBAT, PUZZLE, ELITE, EVENT, BOSS }

	/** Modificadores disponibles. Valores de catálogo, no de balance. */
	public static final List<String> MODIFIERS = List.of(
			"fog", "ember_rain", "elite_surge", "reduced_healing", "boss_stagger", "loot_boost");

	/** Módulos del centro entre el combate inicial y el jefe final. */
	private static final List<Module> MIDDLE = List.of(Module.PUZZLE, Module.ELITE, Module.EVENT);

	private ExpeditionRules() {}

	/**
	 * Módulos de la expedición. La dificultad 0–2 fija la longitud: 3, 4 o 5 módulos.
	 */
	public static List<Module> compose(long seed, int difficulty) {
		int length = 3 + Math.max(0, Math.min(2, difficulty));
		Random random = new Random(seed);
		List<Module> out = new ArrayList<>(length);
		out.add(Module.COMBAT);
		for (int i = 0; i < length - 2; i++) out.add(MIDDLE.get(random.nextInt(MIDDLE.size())));
		out.add(Module.BOSS);
		return out;
	}

	/** Modificadores distintos de la semilla (1–3). Nunca repite ni inventa uno. */
	public static List<String> modifiers(long seed, int count) {
		int n = Math.max(1, Math.min(3, count));
		List<String> pool = new ArrayList<>(MODIFIERS);
		Random random = new Random(seed);
		List<String> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) out.add(pool.remove(random.nextInt(pool.size())));
		return out;
	}
}
