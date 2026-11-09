package dev.aptitudes.guide;

import dev.aptitudes.role.PathRules;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

/**
 * Reglas puras del grimorio (v61): buscador (G2), marcadores (G2) y progreso (G3).
 * Sin clases de Minecraft: se prueban con JUnit.
 */
public final class GuideRules {
	/** Marcadores guardados en {@code config/aptitudes-client.json}. */
	public static final int MAX_BOOKMARKS = 24;
	public static final int MAX_QUERY = 40;
	private static final Pattern MARKS = Pattern.compile("\\p{M}+");
	private static final Pattern BOOKMARK = Pattern.compile("(chapter|beast|item|role):[a-z0-9_.:/-]{1,80}");

	private GuideRules() {}

	// ------------------------------------------------------------------ Buscador

	/** Minúsculas sin tildes ni espacios repetidos: «Hoguera» = «hoguera», «Éter» = «eter». */
	public static String normalize(String text) {
		if (text == null) return "";
		String plain = MARKS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
		return plain.toLowerCase(Locale.ROOT).replace("ß", "ss").trim().replaceAll("\\s+", " ");
	}

	/** Palabras de la consulta ya normalizadas (como mucho {@link #MAX_QUERY} caracteres). */
	public static List<String> terms(String query) {
		String q = normalize(query);
		if (q.length() > MAX_QUERY) q = q.substring(0, MAX_QUERY);
		List<String> out = new ArrayList<>();
		for (String t : q.split(" ")) if (!t.isEmpty()) out.add(t);
		return out;
	}

	/**
	 * Puntuación de un resultado: 0 si falta alguna palabra; más si coincide en el título y si el título
	 * empieza por la consulta. {@code title} y {@code body} deben venir normalizados.
	 */
	public static int score(List<String> terms, String title, String body) {
		if (terms.isEmpty()) return 0;
		int score = 0;
		for (String t : terms) {
			if (title.contains(t)) score += 10;
			else if (body.contains(t)) score += 1;
			else return 0;
		}
		if (title.startsWith(terms.get(0))) score += 5;
		return score;
	}

	// ---------------------------------------------------------------- Marcadores

	public static boolean validBookmark(String id) {
		return id != null && BOOKMARK.matcher(id).matches();
	}

	/** Copia válida, sin duplicados y con tope (los primeros se conservan). */
	public static List<String> sanitizeBookmarks(Collection<String> raw) {
		Set<String> out = new LinkedHashSet<>();
		if (raw != null) {
			for (String id : raw) {
				if (out.size() >= MAX_BOOKMARKS) break;
				if (validBookmark(id)) out.add(id);
			}
		}
		return new ArrayList<>(out);
	}

	/** Añade al final o quita si ya estaba; si está lleno, no añade. */
	public static List<String> toggle(List<String> current, String id) {
		List<String> out = sanitizeBookmarks(current);
		if (!validBookmark(id)) return out;
		if (!out.remove(id) && out.size() < MAX_BOOKMARKS) out.add(id);
		return out;
	}

	// ------------------------------------------------------------------ Progreso

	/** Porcentaje entero (redondeo hacia abajo), 0 si no hay total; nunca pasa de 100. */
	public static int percent(long done, long total) {
		if (total <= 0 || done <= 0) return 0;
		return (int) Math.min(100, done * 100 / total);
	}

	/** Progreso de las sendas: escalones obtenidos (máximo 7 por rol) y piedras angulares. */
	public record PathProgress(int steps, int maxSteps, int keystones, int maxKeystones) {}

	/** Escalones por senda contando una sola rama de la bifurcación. */
	public static final int STEPS_PER_PATH = PathRules.Step.values().length - 1;

	public static PathProgress pathProgress(ToIntFunction<String> rankOf, Collection<String> roleKeys) {
		int steps = 0;
		int keystones = 0;
		for (String role : roleKeys) {
			int path = PathRules.chosenPath(rankOf, role);
			if (path < 1) continue;
			boolean branch = false;
			for (PathRules.Step step : PathRules.Step.values()) {
				if (rankOf.applyAsInt(PathRules.nodeId(role, path, step)) <= 0) continue;
				if (step.branch()) {
					if (branch) continue; // las dos ramas son excluyentes: cuenta una
					branch = true;
				}
				steps++;
				if (step == PathRules.Step.S7) keystones++;
			}
		}
		return new PathProgress(steps, roleKeys.size() * STEPS_PER_PATH, keystones, roleKeys.size());
	}
}
