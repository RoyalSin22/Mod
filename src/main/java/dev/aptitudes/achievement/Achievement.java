package dev.aptitudes.achievement;

/**
 * Catálogo de logros (v31). Puro (sin Minecraft): cada logro es un avance de la pestaña «Aptitudes» en
 * {@code data/aptitudes/advancement/aptitudes/<id>.json}, generado por {@code tools/advancements/gen_advancements.py}.
 * Salvo la raíz, todos usan un criterio {@code minecraft:impossible} llamado {@link #CRITERION} que concede el
 * servidor desde el sistema correspondiente ({@link Achievements#grant}).
 *
 * <p>El id de cada logro forma parte del progreso guardado del jugador (advancements/*.json del mundo):
 * <b>no se renombran ni se borran</b>; se añaden al final.</p>
 */
public enum Achievement {
	ROOT("root", null, Frame.TASK, "aptitudes:guide_book"),
	REST_BONFIRE("rest_bonfire", ROOT, Frame.TASK, "aptitudes:bonfire"),
	SOUL_LEVEL("soul_level", REST_BONFIRE, Frame.GOAL, "aptitudes:soul_shard"),
	CHOOSE_ROLE("choose_role", ROOT, Frame.TASK, "aptitudes:weapon_sigil"),
	COMBO_FINISHER("combo_finisher", CHOOSE_ROLE, Frame.TASK, "minecraft:iron_sword"),
	EVOLVE_MASTER("evolve_master", CHOOSE_ROLE, Frame.GOAL, "aptitudes:mastery_emblem"),
	EVOLVE_ULTIMATE("evolve_ultimate", EVOLVE_MASTER, Frame.CHALLENGE, "aptitudes:ultimate_core"),
	SKILL_MASTERY("skill_mastery", ROOT, Frame.GOAL, "minecraft:diamond_pickaxe"),
	SOCKET_RUNE("socket_rune", ROOT, Frame.TASK, "aptitudes:rune_force"),
	SLAY_BOSS("slay_boss", SOCKET_RUNE, Frame.GOAL, "aptitudes:tower_core"),
	SLAY_HERALD("slay_herald", SLAY_BOSS, Frame.CHALLENGE, "aptitudes:eclipse_core"),
	SURVIVE_SOUL_FOG("survive_soul_fog", ROOT, Frame.TASK, "minecraft:soul_lantern"),
	SURVIVE_EMBER_RAIN("survive_ember_rain", SURVIVE_SOUL_FOG, Frame.TASK, "minecraft:magma_block"),
	SURVIVE_ECLIPSE("survive_eclipse", SURVIVE_EMBER_RAIN, Frame.CHALLENGE, "aptitudes:eclipse_fragment"),
	JOIN_PARTY("join_party", ROOT, Frame.TASK, "minecraft:cake"),
	FIRST_CONTRACT("first_contract", ROOT, Frame.TASK, "aptitudes:contract_board"),
	CONTRACT_VETERAN("contract_veteran", FIRST_CONTRACT, Frame.GOAL, "minecraft:emerald"),
	GUILD_MASTER("guild_master", CONTRACT_VETERAN, Frame.CHALLENGE, "aptitudes:guild_seal");

	/** Nombre del único criterio de cada logro (salvo la raíz, que usa {@code minecraft:tick}). */
	public static final String CRITERION = "done";
	/** Nivel de alma de «Alma templada». */
	public static final int SOUL_LEVEL_TARGET = 10;
	/** Nivel de habilidad de «Maestro de oficio» (el hito más alto). */
	public static final int SKILL_LEVEL_TARGET = 50;
	/** Contratos cobrados para «Veterano del gremio». */
	public static final int CONTRACTS_TARGET = 10;
	/** Rango del gremio (índice de {@code ContractRules.rankIndex}) para «Maestro del gremio». */
	public static final int GUILD_RANK_TARGET = 3;

	public enum Frame { TASK, GOAL, CHALLENGE }

	private final String id;
	private final Achievement parent;
	private final Frame frame;
	private final String icon;

	Achievement(String id, Achievement parent, Frame frame, String icon) {
		this.id = id;
		this.parent = parent;
		this.frame = frame;
		this.icon = icon;
	}

	public String id() { return id; }
	public Achievement parent() { return parent; }
	public Frame frame() { return frame; }
	public String icon() { return icon; }
	/** Ruta del avance dentro del espacio {@code aptitudes}: {@code aptitudes/<id>}. */
	public String path() { return "aptitudes/" + id; }
	public String titleKey() { return "advancement.aptitudes." + id + ".title"; }
	public String descriptionKey() { return "advancement.aptitudes." + id + ".description"; }
}
