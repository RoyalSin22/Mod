package dev.aptitudes.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bestiario del grimorio (v61, G1): catálogo cerrado de criaturas y jefes, qué entradas desbloquea una
 * muerte y saneado de los recuentos guardados o recibidos. Puro: sin clases de Minecraft.
 *
 * <p>Los ids de entrada se guardan en el jugador: no se renombran; las entradas nuevas se añaden al final.</p>
 */
public final class BestiaryRules {
	/** Tope de cada recuento: evita desbordamientos con granjas de criaturas. */
	public static final int MAX_COUNT = 999_999;
	/** Tope defensivo de entradas al leer datos guardados o un paquete. */
	public static final int MAX_ENTRIES = 256;

	public enum Kind { CREATURE, ELITE, BOSS }

	public enum Region { OVERWORLD, NETHER, END, ANY }

	/**
	 * Una página del bestiario.
	 *
	 * @param id       clave guardada y sufijo de los textos ({@code bestiary.aptitudes.<id>.tip})
	 * @param entity   tipo de entidad que se dibuja y del que salen las estadísticas (null: sin modelo)
	 * @param bossKind id de {@code BossKind} para los jefes propios (null en los demás)
	 * @param icon     objeto del índice y del buscador
	 * @param drops    botín principal (ids de objeto), solo informativo
	 */
	public record Entry(String id, Kind kind, Region region, String entity, String bossKind, String icon, List<String> drops) {
		public Entry {
			drops = List.copyOf(drops);
		}

		public String tipKey() { return "bestiary.aptitudes." + id + ".tip"; }
	}

	private static final List<Entry> ENTRIES;
	private static final Map<String, Entry> BY_ID = new LinkedHashMap<>();
	private static final Map<String, Entry> BY_ENTITY = new LinkedHashMap<>();
	private static final Map<String, Entry> BY_BOSS = new LinkedHashMap<>();

	static {
		List<Entry> out = new ArrayList<>();
		creature(out, "zombie", Region.OVERWORLD, "rotten_flesh", "iron_ingot", "carrot");
		creature(out, "husk", Region.OVERWORLD, "rotten_flesh", "iron_ingot");
		creature(out, "drowned", Region.OVERWORLD, "rotten_flesh", "copper_ingot", "trident");
		creature(out, "zombie_villager", Region.OVERWORLD, "rotten_flesh", "iron_ingot");
		creature(out, "skeleton", Region.OVERWORLD, "bone", "arrow", "bow");
		creature(out, "stray", Region.OVERWORLD, "bone", "arrow", "tipped_arrow");
		creature(out, "bogged", Region.OVERWORLD, "bone", "arrow", "tipped_arrow");
		creature(out, "spider", Region.OVERWORLD, "string", "spider_eye");
		creature(out, "cave_spider", Region.OVERWORLD, "string", "spider_eye");
		creature(out, "creeper", Region.OVERWORLD, "gunpowder");
		creature(out, "enderman", Region.ANY, "ender_pearl");
		creature(out, "witch", Region.OVERWORLD, "glowstone_dust", "redstone", "glass_bottle", "sugar");
		creature(out, "slime", Region.OVERWORLD, "slime_ball");
		creature(out, "phantom", Region.OVERWORLD, "phantom_membrane");
		creature(out, "silverfish", Region.OVERWORLD);
		creature(out, "pillager", Region.OVERWORLD, "arrow", "crossbow");
		creature(out, "vindicator", Region.OVERWORLD, "emerald", "iron_axe");
		creature(out, "evoker", Region.OVERWORLD, "totem_of_undying", "emerald");
		creature(out, "ravager", Region.OVERWORLD, "saddle");
		creature(out, "guardian", Region.OVERWORLD, "prismarine_shard", "prismarine_crystals", "cod");
		creature(out, "elder_guardian", Region.OVERWORLD, "wet_sponge", "prismarine_shard");
		creature(out, "breeze", Region.OVERWORLD, "breeze_rod");
		creature(out, "warden", Region.OVERWORLD, "sculk_catalyst");
		creature(out, "zombified_piglin", Region.NETHER, "rotten_flesh", "gold_nugget", "gold_ingot");
		creature(out, "piglin_brute", Region.NETHER, "golden_axe");
		creature(out, "hoglin", Region.NETHER, "porkchop", "leather");
		creature(out, "blaze", Region.NETHER, "blaze_rod");
		creature(out, "ghast", Region.NETHER, "ghast_tear", "gunpowder");
		creature(out, "magma_cube", Region.NETHER, "magma_cream");
		creature(out, "wither_skeleton", Region.NETHER, "coal", "bone", "wither_skeleton_skull");
		creature(out, "endermite", Region.END);
		creature(out, "shulker", Region.END, "shulker_shell");
		out.add(new Entry("wither", Kind.BOSS, Region.ANY, "minecraft:wither", null, "minecraft:nether_star",
				List.of("minecraft:nether_star")));
		out.add(new Entry("ender_dragon", Kind.BOSS, Region.END, "minecraft:ender_dragon", null, "minecraft:dragon_egg",
				List.of("minecraft:dragon_egg", "minecraft:experience_bottle")));
		out.add(new Entry("elite", Kind.ELITE, Region.ANY, null, null, "aptitudes:elite_essence",
				List.of("aptitudes:elite_essence", "aptitudes:soul_shard")));
		out.add(new Entry("champion", Kind.ELITE, Region.ANY, null, null, "aptitudes:champion_heart",
				List.of("aptitudes:champion_heart", "aptitudes:elite_essence", "aptitudes:soul_shard")));
		boss(out, "tower_sentinel", Region.OVERWORLD, "minecraft:wither_skeleton", "aptitudes:tower_core");
		boss(out, "shrine_oracle", Region.OVERWORLD, "minecraft:evoker", "aptitudes:shrine_sigil");
		boss(out, "camp_champion", Region.OVERWORLD, "minecraft:vindicator", "aptitudes:warlord_medallion");
		boss(out, "eclipse_herald", Region.OVERWORLD, "minecraft:wither_skeleton", "aptitudes:eclipse_core");
		// v63: jefes animados (GeckoLib); se añaden al final.
		out.add(new Entry("faceless_colossus", Kind.BOSS, Region.OVERWORLD, "aptitudes:faceless_colossus", null,
				"aptitudes:faceless_colossus_spawn_egg",
				List.of("aptitudes:champion_heart", "aptitudes:soul_shard", "aptitudes:elite_essence")));
		out.add(new Entry("dismantled_guardian", Kind.BOSS, Region.OVERWORLD, "aptitudes:dismantled_guardian", null,
				"aptitudes:dismantled_guardian_spawn_egg", List.of("aptitudes:soul_shard", "aptitudes:elite_essence")));
		out.add(new Entry("ancient_automaton", Kind.BOSS, Region.OVERWORLD, "aptitudes:ancient_automaton", null,
				"aptitudes:ancient_automaton_spawn_egg", List.of("aptitudes:soul_shard", "aptitudes:elite_essence")));
		// v65: criaturas nuevas, al final.
		out.add(new Entry("osario_quebrado", Kind.CREATURE, Region.OVERWORLD, "aptitudes:osario_quebrado", null,
				"aptitudes:osario_quebrado_spawn_egg", List.of("aptitudes:nucleo_osario", "aptitudes:soul_shard")));
		out.add(new Entry("errante_velado", Kind.CREATURE, Region.OVERWORLD, "aptitudes:errante_velado", null,
				"aptitudes:errante_velado_spawn_egg", List.of("aptitudes:jiron_velo_espectral", "aptitudes:soul_shard")));
		// v65: escorificador de hierro (Nether y Lluvia de brasas).
		out.add(new Entry("escorificador_hierro", Kind.CREATURE, Region.NETHER, "aptitudes:escorificador_hierro", null,
				"aptitudes:escorificador_hierro_spawn_egg", List.of("aptitudes:soul_shard")));
		ENTRIES = Collections.unmodifiableList(out);
		for (Entry e : ENTRIES) {
			if (BY_ID.put(e.id(), e) != null) throw new IllegalStateException("Entrada duplicada: " + e.id());
			if (e.bossKind() != null) BY_BOSS.put(e.bossKind(), e);
			else if (e.entity() != null) BY_ENTITY.put(e.entity(), e);
		}
	}

	private BestiaryRules() {}

	private static void creature(List<Entry> out, String name, Region region, String... drops) {
		List<String> ids = new ArrayList<>();
		for (String d : drops) ids.add("minecraft:" + d);
		out.add(new Entry(name, Kind.CREATURE, region, "minecraft:" + name, null, "minecraft:" + name + "_spawn_egg", ids));
	}

	private static void boss(List<Entry> out, String kind, Region region, String entity, String core) {
		out.add(new Entry("boss_" + kind, Kind.BOSS, region, entity, kind, core, List.of(core, "aptitudes:soul_shard")));
	}

	public static List<Entry> entries() { return ENTRIES; }

	public static Entry byId(String id) { return id == null ? null : BY_ID.get(id); }

	public static boolean known(String id) { return BY_ID.containsKey(id); }

	/** Clave de nombre: la del juego para criaturas, la del jefe propio o una propia para élites. */
	public static String nameKey(Entry entry) {
		if (entry.bossKind() != null) return "boss.aptitudes." + entry.bossKind();
		if (entry.kind() == Kind.ELITE) return "bestiary.aptitudes." + entry.id() + ".name";
		String entity = entry.entity();
		int colon = entity.indexOf(':');
		return "entity." + entity.substring(0, colon) + "." + entity.substring(colon + 1);
	}

	/**
	 * Entradas que desbloquea una muerte. Un jefe propio desbloquea su página y la de la criatura base;
	 * un campeón cuenta también como élite.
	 *
	 * @param entityId id del tipo ({@code minecraft:zombie})
	 * @param bossKind id del jefe propio o null
	 */
	public static List<String> unlockedBy(String entityId, String bossKind, boolean elite, boolean champion) {
		List<String> out = new ArrayList<>(3);
		if (bossKind != null) {
			Entry boss = BY_BOSS.get(bossKind);
			if (boss != null) out.add(boss.id());
		}
		Entry base = entityId == null ? null : BY_ENTITY.get(entityId);
		if (base != null) out.add(base.id());
		// Los jefes propios se generan como campeones: su página ya cuenta, no la de los élites.
		if (bossKind == null) {
			if (elite || champion) out.add("elite");
			if (champion) out.add("champion");
		}
		return out;
	}

	/** Suma uno con tope. */
	public static int increment(int current) {
		return current >= MAX_COUNT ? MAX_COUNT : Math.max(0, current) + 1;
	}

	/** Quita ids desconocidos y recuentos no válidos; orden estable. */
	public static Map<String, Integer> sanitize(Map<String, Integer> raw) {
		Map<String, Integer> out = new TreeMap<>();
		if (raw == null) return out;
		for (Map.Entry<String, Integer> e : raw.entrySet()) {
			if (out.size() >= MAX_ENTRIES) break;
			Integer count = e.getValue();
			if (!known(e.getKey()) || count == null || count <= 0) continue;
			out.put(e.getKey(), Math.min(MAX_COUNT, count));
		}
		return out;
	}

	public static int unlocked(Map<String, Integer> kills, Kind kind) {
		int n = 0;
		for (Entry e : ENTRIES) if ((kind == null || e.kind() == kind) && kills.getOrDefault(e.id(), 0) > 0) n++;
		return n;
	}

	public static int total(Kind kind) {
		int n = 0;
		for (Entry e : ENTRIES) if (kind == null || e.kind() == kind) n++;
		return n;
	}
}
