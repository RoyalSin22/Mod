package dev.aptitudes.guide;

import java.util.List;
import java.util.function.Predicate;

/**
 * V71 (T7b): catálogo puro de las entradas de lore que el Grimorio muestra en «Crónicas». Cada entrada tiene un texto
 * ({@code lore.aptitudes.<id>.title/.body}) y una fuente: el desbloqueo de {@link LoreRules} que la revela, o ninguna
 * (siempre visible) o imposible (aún sin fuente). Los textos del bestiario no entran aquí: viven en las cartas del bestiario.
 *
 * <p>La tabla puede cambiar sin tocar el almacenamiento: el estado guardado son los ids de {@link LoreRules}, no las entradas.</p>
 */
public final class LoreCatalog {
	private LoreCatalog() {
	}

	public enum Kind { LORE, CHRONICLE }

	/** Fuente de las entradas visibles desde el principio. */
	public static final String STARTER = "";
	/** Fuente de las entradas que todavía no tienen forma de conseguirse. */
	public static final String NEVER = "!";

	/** @param id parte central de la clave de texto ({@code lore.aptitudes.<id>.title}); @param source id de {@link LoreRules}, {@link #STARTER} o {@link #NEVER} */
	public record Entry(String id, Kind kind, String source) {
		public String titleKey() {
			return "lore.aptitudes." + id + ".title";
		}

		public String bodyKey() {
			return "lore.aptitudes." + id + ".body";
		}
	}

	/** En orden de lectura: de la cosmología al último arco. */
	public static final List<Entry> ENTRIES = List.of(
			new Entry("cosmology.essence_within", Kind.LORE, STARTER),
			new Entry("guild.twenty_paths", Kind.LORE, STARTER),
			new Entry("doctrine.three_doctrines", Kind.LORE, STARTER),
			new Entry("chronology.council_three_ways", Kind.LORE, LoreRules.LORE_CRYPT_CLEARED),
			new Entry("faction.concordance", Kind.LORE, LoreRules.LORE_CRYPT_CLEARED),
			new Entry("cosmology.echoes", Kind.LORE, LoreRules.LORE_SOUL_FOG_TRIGGER),
			new Entry("cosmology.veil_frontier", Kind.LORE, LoreRules.LORE_CRYPT_CLEARED),
			new Entry("faction.permanence", Kind.LORE, LoreRules.LORE_CRYPT_CLEARED),
			new Entry("chronology.first_answer", Kind.LORE, LoreRules.LORE_CUSTODIAN_DEFEATED),
			new Entry("artifact.sealed_artifact", Kind.LORE, LoreRules.LORE_ARTIFACT_FOUND),
			new Entry("event.first_eclipse", Kind.LORE, LoreRules.LORE_ECLIPSE_SACRIFICE),
			new Entry("temple.last_mandate", Kind.LORE, LoreRules.LORE_CUSTODIAN_DEFEATED),
			new Entry("event.current_eclipse", Kind.LORE, LoreRules.LORE_ECLIPSE_SACRIFICE),
			new Entry("event.silent_fracture", Kind.LORE, LoreRules.LORE_ECLIPSE_AFTERMATH),
			new Entry("arc2.unfinished_oath", Kind.LORE, LoreRules.LORE_UNFINISHED_OATH),
			new Entry("event.soul_fog_chronicle", Kind.CHRONICLE, LoreRules.LORE_SOUL_FOG_TRIGGER),
			new Entry("event.ember_rain_chronicle", Kind.CHRONICLE, LoreRules.LORE_EMBER_RAIN_TRIGGER),
			new Entry("event.eclipse_chronicle", Kind.CHRONICLE, LoreRules.LORE_ECLIPSE_SACRIFICE));

	/** ¿Está la entrada desbloqueada? {@code has} dice si el jugador tiene un id de {@link LoreRules}. */
	public static boolean unlocked(Entry entry, Predicate<String> has) {
		if (entry.source().equals(STARTER)) return true;
		if (entry.source().equals(NEVER)) return false;
		return has.test(entry.source());
	}

	/** Cuántas entradas están desbloqueadas. */
	public static int unlockedCount(Predicate<String> has) {
		int n = 0;
		for (Entry e : ENTRIES) if (unlocked(e, has)) n++;
		return n;
	}

	public static int total() {
		return ENTRIES.size();
	}

	/** Entrada por id, o {@code null}. */
	public static Entry byId(String id) {
		for (Entry e : ENTRIES) if (e.id().equals(id)) return e;
		return null;
	}
}
