package dev.aptitudes.social;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * C7 (S5): catálogo de las notas en el suelo. Puro (sin Minecraft) y probado.
 *
 * <p>Una nota es una plantilla con un hueco ({@code %s}) y una palabra. Se guardan por clave de texto, nunca por índice:
 * añadir piezas al final es seguro; renombrar o borrar una clave deja ilegibles las notas que la usen (se descartan al
 * cargar). Los textos viven en los archivos de idioma ({@link #templateKey}, {@link #wordKey}); por eso la nota se lee
 * en el idioma de cada jugador y no hay texto libre que moderar.</p>
 */
public final class MessageCatalog {
	/** Categorías de palabras, en el orden de la pantalla. */
	public enum Category { CREATURE, PLACE, ITEM, ACTION, CONCEPT }

	/** Plantillas, en el orden de la pantalla. Todas con la forma «etiqueta: %s» para no depender de la gramática. */
	public static final List<String> TEMPLATES = List.of(
			"danger", "ahead", "need", "try", "weak_point", "hidden",
			"keep_ready", "turn_back", "worth_it", "remember", "at_night", "this_way");

	private static final Map<String, Category> WORDS = new LinkedHashMap<>();

	static {
		words(Category.CREATURE, "ossuary", "great_ossuary", "wanderer", "automaton", "colossus", "guardian",
				"custodian", "slagger", "elite", "champion", "undead", "creeper", "many_enemies");
		words(Category.PLACE, "bonfire", "temple", "crypt", "pit", "trap", "chest", "door", "stairs", "water", "lava",
				"shortcut", "dead_end");
		words(Category.ITEM, "veil_anchor", "veil_key", "flask", "shield", "torches", "bow", "potion", "food", "armor",
				"soul_shards");
		words(Category.ACTION, "parry", "dodge", "retreat", "group", "jump", "wait", "run", "sneak", "from_behind",
				"ranged", "light", "dig");
		words(Category.CONCEPT, "eclipse", "soul_fog", "ember_rain", "fire", "poison", "darkness", "magic", "fall",
				"treasure", "danger", "safe_spot", "nothing");
	}

	private MessageCatalog() {}

	private static void words(Category category, String... keys) {
		for (String k : keys) {
			if (WORDS.put(k, category) != null) throw new IllegalStateException("Palabra repetida: " + k);
		}
	}

	/** Palabras y su categoría, en el orden de la pantalla. */
	public static Map<String, Category> words() {
		return Collections.unmodifiableMap(WORDS);
	}

	public static boolean isTemplate(String key) {
		return TEMPLATES.contains(key);
	}

	public static boolean isWord(String key) {
		return WORDS.containsKey(key);
	}

	/** ¿Plantilla y palabra existen? Lo comprueba el servidor antes de guardar una nota que llega del cliente. */
	public static boolean isValid(String template, String word) {
		return template != null && word != null && isTemplate(template) && isWord(word);
	}

	public static String templateKey(String template) {
		return "message.aptitudes.ground.template." + template;
	}

	public static String wordKey(String word) {
		return "message.aptitudes.ground.word." + word;
	}

	public static String categoryKey(Category category) {
		return "screen.aptitudes.ground_message.category." + category.name().toLowerCase(java.util.Locale.ROOT);
	}
}
