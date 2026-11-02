package dev.aptitudes.role;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.aptitudes.role.RoleNode.activeNode;
import static dev.aptitudes.role.RoleNode.passiveNode;

/**
 * Definición de los árboles. Cada rol sigue la misma estructura para que sea
 * fácil de equilibrar y de leer en la GUI:
 *
 * <pre>
 *  [Pasiva A] (nv 1) ──► [Pasiva B] (nv 8)
 *  [Activa A] (nv 3) ──► [Activa B] (nv 12)
 * </pre>
 *
 * Cada rol tiene 4 activas; el jugador equipa 2 por rol (ranuras 1/2 del principal
 * y 3/4 del secundario, ver {@code RoleData#equipped}).
 */
public final class RoleTree {
	private static final Map<String, RoleNode> BY_ID = new LinkedHashMap<>();
	private static final Map<Role, List<RoleNode>> BY_ROLE = new EnumMap<>(Role.class);
	private static final Map<Role, RoleNode[]> ACTIVES = new EnumMap<>(Role.class);
	/** Declarado antes del bloque static que llama a {@code define()} (orden de inicialización). */
	private static final Map<String, GridPos> POSITIONS = new LinkedHashMap<>();

	static {
		// Bersérker: aguanta, se enfurece y castiga más cuanto menos vida tiene.
		define(Role.BERSERKER,
				passiveNode("tough_skin", Role.BERSERKER, 1, null),
				activeNode("rage", Role.BERSERKER, 3, null, 35, 45),
				passiveNode("bloodlust", Role.BERSERKER, 8, "tough_skin"),
				activeNode("war_cry", Role.BERSERKER, 12, "rage", 30, 30),
				passiveNode("frenzy", Role.BERSERKER, 1, null),
				passiveNode("unbreakable", Role.BERSERKER, 10, null),
				passiveNode("carnage", Role.BERSERKER, 18, null),
				activeNode("leap_slam", Role.BERSERKER, 14, null, 35, 16),
				activeNode("whirlwind", Role.BERSERKER, 22, null, 40, 20));
		// Mago: gran reserva de energía y hechizos a distancia.
		define(Role.MAGE,
				passiveNode("arcane_affinity", Role.MAGE, 1, null),
				activeNode("fireball", Role.MAGE, 3, null, 25, 6),
				passiveNode("clear_mind", Role.MAGE, 8, "arcane_affinity"),
				activeNode("blink", Role.MAGE, 12, "fireball", 30, 12),
				passiveNode("arcane_charges", Role.MAGE, 1, null),
				passiveNode("mana_shield", Role.MAGE, 10, null),
				passiveNode("spell_echo", Role.MAGE, 18, null),
				activeNode("time_warp", Role.MAGE, 14, null, 35, 18),
				activeNode("arcane_missiles", Role.MAGE, 22, null, 40, 14));
		// Asesino: sigilo y golpes por la espalda.
		define(Role.ASSASSIN,
				passiveNode("shadow", Role.ASSASSIN, 1, null),
				activeNode("vanish", Role.ASSASSIN, 3, null, 40, 40),
				passiveNode("backstab", Role.ASSASSIN, 8, "shadow"),
				activeNode("shadow_step", Role.ASSASSIN, 12, "vanish", 35, 18),
				passiveNode("lethal_tempo", Role.ASSASSIN, 1, null),
				passiveNode("smoke_escape", Role.ASSASSIN, 10, null),
				passiveNode("vanish_on_kill", Role.ASSASSIN, 18, null),
				activeNode("fan_of_knives", Role.ASSASSIN, 14, null, 30, 12),
				activeNode("shuriken", Role.ASSASSIN, 22, null, 20, 6));
		// Herbolista: curación, regeneración y venenos.
		define(Role.HERBALIST,
				passiveNode("vital_sap", Role.HERBALIST, 1, null),
				activeNode("balm", Role.HERBALIST, 3, null, 30, 25),
				passiveNode("herbal_immunity", Role.HERBALIST, 8, "vital_sap"),
				activeNode("poison_thorns", Role.HERBALIST, 12, "balm", 30, 25),
				passiveNode("bloom", Role.HERBALIST, 1, null),
				passiveNode("antidote", Role.HERBALIST, 10, null),
				passiveNode("thorny_skin", Role.HERBALIST, 18, null),
				activeNode("healing_rain", Role.HERBALIST, 14, null, 45, 30),
				activeNode("spore_cloud", Role.HERBALIST, 22, null, 35, 22));
		// Ingeniero (Tecnólogo + Futurista): ingeniería, propulsores, baterías y torreta.
		define(Role.ENGINEER,
				passiveNode("engineering", Role.ENGINEER, 1, null),
				activeNode("thrusters", Role.ENGINEER, 3, null, 20, 8),
				passiveNode("battery", Role.ENGINEER, 8, "engineering"),
				activeNode("turret", Role.ENGINEER, 12, "thrusters", 60, 90),
				passiveNode("overdrive", Role.ENGINEER, 1, null),
				passiveNode("auto_shield", Role.ENGINEER, 10, null),
				passiveNode("field_repair", Role.ENGINEER, 18, null),
				activeNode("grapple", Role.ENGINEER, 14, null, 15, 6),
				activeNode("pulse_grenade", Role.ENGINEER, 22, null, 35, 14));
		// Lancero: alcance y cargas.
		define(Role.LANCER,
				passiveNode("reach", Role.LANCER, 1, null),
				activeNode("lunge", Role.LANCER, 3, null, 30, 14),
				passiveNode("charge", Role.LANCER, 8, "reach"),
				activeNode("sweep", Role.LANCER, 12, "lunge", 30, 12),
				passiveNode("impetus", Role.LANCER, 1, null),
				passiveNode("brace", Role.LANCER, 10, null),
				passiveNode("dragoon_fall", Role.LANCER, 18, null),
				activeNode("dragon_leap", Role.LANCER, 14, null, 35, 16),
				activeNode("piercing_thrust", Role.LANCER, 22, null, 30, 10));
		// Espadachín: filo, ritmo y contraataques.
		define(Role.SWORDSMAN,
				passiveNode("keen_edge", Role.SWORDSMAN, 1, null),
				activeNode("blade_dance", Role.SWORDSMAN, 3, null, 30, 40),
				passiveNode("parry", Role.SWORDSMAN, 8, "keen_edge"),
				activeNode("riposte", Role.SWORDSMAN, 12, "blade_dance", 25, 15),
				passiveNode("kata", Role.SWORDSMAN, 1, null),
				passiveNode("reflexes", Role.SWORDSMAN, 10, null),
				passiveNode("flurry", Role.SWORDSMAN, 18, null),
				activeNode("iaido", Role.SWORDSMAN, 14, null, 30, 12),
				activeNode("thousand_cuts", Role.SWORDSMAN, 22, null, 40, 18));
		// Paladín: luz contra los no-muertos, escudo divino y aura protectora.
		define(Role.PALADIN,
				passiveNode("holy_smite", Role.PALADIN, 1, null),
				activeNode("divine_shield", Role.PALADIN, 3, null, 40, 35),
				passiveNode("protective_aura", Role.PALADIN, 8, "holy_smite"),
				activeNode("judgement", Role.PALADIN, 12, "divine_shield", 40, 30),
				passiveNode("holy_light", Role.PALADIN, 1, null),
				passiveNode("intercession", Role.PALADIN, 10, null),
				passiveNode("purifying_light", Role.PALADIN, 18, null),
				activeNode("consecration", Role.PALADIN, 14, null, 45, 30),
				activeNode("hammer_of_justice", Role.PALADIN, 22, null, 35, 16));
		// Nigromante: esbirros aliados que escalan con su nivel y drenaje de vida.
		define(Role.NECROMANCER,
				passiveNode("dark_pact", Role.NECROMANCER, 1, null),
				activeNode("raise_minions", Role.NECROMANCER, 3, null, 50, 60),
				passiveNode("life_drain", Role.NECROMANCER, 8, "dark_pact"),
				activeNode("death_touch", Role.NECROMANCER, 12, "raise_minions", 35, 20),
				passiveNode("soul_harvest", Role.NECROMANCER, 1, null),
				passiveNode("bone_shield", Role.NECROMANCER, 10, null),
				passiveNode("corpse_burst", Role.NECROMANCER, 18, null),
				activeNode("bone_spear", Role.NECROMANCER, 14, null, 30, 8),
				activeNode("soul_siphon", Role.NECROMANCER, 22, null, 45, 22));
		// Druida: bosque, crecimiento, raíces y forma de oso.
		define(Role.DRUID,
				passiveNode("forest_child", Role.DRUID, 1, null),
				activeNode("entangling_roots", Role.DRUID, 3, null, 30, 20),
				passiveNode("growth", Role.DRUID, 8, "forest_child"),
				activeNode("bear_form", Role.DRUID, 12, "entangling_roots", 45, 60),
				passiveNode("day_night_cycle", Role.DRUID, 1, null),
				passiveNode("barkskin", Role.DRUID, 10, null),
				passiveNode("bramble", Role.DRUID, 18, null),
				activeNode("summon_wolves", Role.DRUID, 14, null, 50, 60),
				activeNode("thorn_storm", Role.DRUID, 22, null, 40, 20));
		// Mercader: descuentos, tasación, ganancias extra y soborno.
		define(Role.MERCHANT,
				passiveNode("haggling", Role.MERCHANT, 1, null),
				activeNode("appraiser_eye", Role.MERCHANT, 3, null, 25, 60),
				passiveNode("deep_pockets", Role.MERCHANT, 8, "haggling"),
				activeNode("bribe", Role.MERCHANT, 12, "appraiser_eye", 30, 45),
				passiveNode("compound_interest", Role.MERCHANT, 1, null),
				passiveNode("insurance", Role.MERCHANT, 10, null),
				passiveNode("bounty", Role.MERCHANT, 18, null),
				activeNode("coin_toss", Role.MERCHANT, 14, null, 10, 4),
				activeNode("golden_guard", Role.MERCHANT, 22, null, 20, 40));
		// Monje: artes marciales sin armas.
		define(Role.MONK,
				passiveNode("iron_fists", Role.MONK, 1, null),
				activeNode("chi_palm", Role.MONK, 3, null, 25, 8),
				passiveNode("evasion", Role.MONK, 8, "iron_fists"),
				activeNode("meditation", Role.MONK, 12, "chi_palm", 0, 60),
				passiveNode("stances", Role.MONK, 1, null),
				passiveNode("deflect", Role.MONK, 10, null),
				passiveNode("inner_peace", Role.MONK, 18, null),
				activeNode("flying_kick", Role.MONK, 14, null, 25, 10),
				activeNode("hundred_fists", Role.MONK, 22, null, 40, 16));
		// Elementalista: domina hasta 3 elementos; sus nodos actúan según el elemento activo.
		define(Role.ELEMENTALIST,
				passiveNode("elemental_affinity", Role.ELEMENTALIST, 1, null),
				activeNode("elemental_bolt", Role.ELEMENTALIST, 3, null, 15, 3),
				passiveNode("elemental_dominion", Role.ELEMENTALIST, 8, "elemental_affinity"),
				activeNode("elemental_burst", Role.ELEMENTALIST, 12, "elemental_bolt", 35, 18),
				passiveNode("elemental_reaction", Role.ELEMENTALIST, 1, null),
				passiveNode("elemental_ward", Role.ELEMENTALIST, 10, null),
				passiveNode("resonant_kill", Role.ELEMENTALIST, 18, null),
				activeNode("elemental_barrage", Role.ELEMENTALIST, 14, null, 40, 14),
				activeNode("elemental_aura", Role.ELEMENTALIST, 22, null, 60, 40));
		// Cambiaformas: se transforma en mobs vanilla (desbloqueados al derrotarlos) y usa sus poderes.
		define(Role.SHAPESHIFTER,
				passiveNode("mimicry", Role.SHAPESHIFTER, 1, null),
				activeNode("transform", Role.SHAPESHIFTER, 1, null, 15, 3),
				passiveNode("trickster", Role.SHAPESHIFTER, 8, "mimicry"),
				activeNode("form_power", Role.SHAPESHIFTER, 5, "transform", 25, 8),
				passiveNode("instinct", Role.SHAPESHIFTER, 1, null),
				passiveNode("slippery", Role.SHAPESHIFTER, 10, null),
				passiveNode("devour", Role.SHAPESHIFTER, 18, null),
				activeNode("pounce", Role.SHAPESHIFTER, 14, null, 25, 10),
				activeNode("beast_howl", Role.SHAPESHIFTER, 22, null, 35, 22));
		// ---- Roles nuevos (v11)
		define(Role.GUNSLINGER,
				passiveNode("quickdraw", Role.GUNSLINGER, 1, null),
				activeNode("arcane_shot", Role.GUNSLINGER, 3, null, 20, 5),
				passiveNode("eagle_eye", Role.GUNSLINGER, 8, "quickdraw"),
				activeNode("fan_the_hammer", Role.GUNSLINGER, 12, "arcane_shot", 35, 14),
				passiveNode("arcane_cylinder", Role.GUNSLINGER, 1, null),
				passiveNode("dodge_roll", Role.GUNSLINGER, 10, null),
				passiveNode("reload", Role.GUNSLINGER, 18, null),
				activeNode("ricochet", Role.GUNSLINGER, 14, null, 30, 10),
				activeNode("deadeye", Role.GUNSLINGER, 22, null, 50, 30));
		define(Role.WEAPON_SUMMONER,
				passiveNode("armory", Role.WEAPON_SUMMONER, 1, null),
				activeNode("blade_volley", Role.WEAPON_SUMMONER, 3, null, 25, 8),
				passiveNode("blade_guard", Role.WEAPON_SUMMONER, 8, "armory"),
				activeNode("sword_rain", Role.WEAPON_SUMMONER, 12, "blade_volley", 40, 18),
				passiveNode("floating_arsenal", Role.WEAPON_SUMMONER, 1, null),
				passiveNode("intercepting_blade", Role.WEAPON_SUMMONER, 10, null),
				passiveNode("reforge", Role.WEAPON_SUMMONER, 18, null),
				activeNode("spectral_greatsword", Role.WEAPON_SUMMONER, 14, null, 35, 12),
				activeNode("blade_prison", Role.WEAPON_SUMMONER, 22, null, 45, 24));
		define(Role.HUNTER,
				passiveNode("predator", Role.HUNTER, 1, null),
				activeNode("hunters_mark", Role.HUNTER, 3, null, 15, 10),
				passiveNode("tracker", Role.HUNTER, 8, "predator"),
				activeNode("bear_trap", Role.HUNTER, 12, "hunters_mark", 25, 16),
				passiveNode("the_hunt", Role.HUNTER, 1, null),
				passiveNode("camouflage", Role.HUNTER, 10, null),
				passiveNode("trophy", Role.HUNTER, 18, null),
				activeNode("arrow_volley", Role.HUNTER, 14, null, 35, 14),
				activeNode("falcon_strike", Role.HUNTER, 22, null, 40, 18));
		define(Role.BARD,
				passiveNode("inspiring_presence", Role.BARD, 1, null),
				activeNode("song_of_valor", Role.BARD, 3, null, 30, 20),
				passiveNode("soothing_melody", Role.BARD, 8, "inspiring_presence"),
				activeNode("dissonance", Role.BARD, 12, "song_of_valor", 25, 12),
				passiveNode("crescendo", Role.BARD, 1, null),
				passiveNode("encore", Role.BARD, 10, null),
				passiveNode("lullaby", Role.BARD, 18, null),
				activeNode("ballad_of_shielding", Role.BARD, 14, null, 40, 30),
				activeNode("requiem", Role.BARD, 22, null, 50, 30));
		define(Role.WARLOCK,
				passiveNode("dark_bargain", Role.WARLOCK, 1, null),
				activeNode("curse_of_agony", Role.WARLOCK, 3, null, 20, 8),
				passiveNode("soul_link", Role.WARLOCK, 8, "dark_bargain"),
				activeNode("abyssal_bolt", Role.WARLOCK, 12, "curse_of_agony", 25, 6),
				passiveNode("demonic_pact", Role.WARLOCK, 1, null),
				passiveNode("demonic_ward", Role.WARLOCK, 10, null),
				passiveNode("soul_feast", Role.WARLOCK, 18, null),
				activeNode("summon_imps", Role.WARLOCK, 14, null, 50, 60),
				activeNode("doom", Role.WARLOCK, 22, null, 45, 30));
		define(Role.TAMER,
				passiveNode("beast_bond", Role.TAMER, 1, null),
				activeNode("sic_em", Role.TAMER, 3, null, 15, 10),
				passiveNode("pack_leader", Role.TAMER, 8, "beast_bond"),
				activeNode("vital_bond", Role.TAMER, 12, "sic_em", 30, 25),
				passiveNode("companion", Role.TAMER, 1, null),
				passiveNode("protective_instinct", Role.TAMER, 10, null),
				passiveNode("feral_feast", Role.TAMER, 18, null),
				activeNode("stampede", Role.TAMER, 14, null, 35, 16),
				activeNode("alpha_roar", Role.TAMER, 22, null, 45, 40));
	}

	private RoleTree() {
	}

	/**
	 * Entrenamientos de estadística de cada rol (2 por rol). {@code attribute == null}
	 * significa maná máximo (efecto propio).
	 */
	public record StatSpec(String nodeId, @Nullable RegistryEntry<EntityAttribute> attribute, Operation operation,
						   double perRank) {}

	private static final Map<Role, StatSpec[]> STATS = new EnumMap<>(Role.class);

	private static void stats(Role role, StatSpec a, StatSpec b) {
		STATS.put(role, new StatSpec[]{a, b});
	}

	private static StatSpec spec(Role role, int index, @Nullable RegistryEntry<EntityAttribute> attribute, Operation op,
								 double perRank) {
		return new StatSpec(role.key() + "_stat" + index, attribute, op, perRank);
	}

	static {
		Operation add = Operation.ADD_VALUE;
		Operation mult = Operation.ADD_MULTIPLIED_BASE;
		stats(Role.BERSERKER, spec(Role.BERSERKER, 1, EntityAttributes.GENERIC_MAX_HEALTH, add, 2),
				spec(Role.BERSERKER, 2, EntityAttributes.GENERIC_ATTACK_DAMAGE, add, 0.5));
		stats(Role.MAGE, spec(Role.MAGE, 1, null, add, 15), spec(Role.MAGE, 2, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.03));
		stats(Role.ASSASSIN, spec(Role.ASSASSIN, 1, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.04),
				spec(Role.ASSASSIN, 2, EntityAttributes.GENERIC_ATTACK_DAMAGE, add, 0.4));
		stats(Role.HERBALIST, spec(Role.HERBALIST, 1, EntityAttributes.GENERIC_MAX_HEALTH, add, 2),
				spec(Role.HERBALIST, 2, EntityAttributes.GENERIC_LUCK, add, 0.5));
		stats(Role.ENGINEER, spec(Role.ENGINEER, 1, EntityAttributes.GENERIC_ARMOR, add, 1),
				spec(Role.ENGINEER, 2, EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, mult, 0.1));
		stats(Role.LANCER, spec(Role.LANCER, 1, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, add, 0.3),
				spec(Role.LANCER, 2, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, add, 0.1));
		stats(Role.SWORDSMAN, spec(Role.SWORDSMAN, 1, EntityAttributes.GENERIC_ATTACK_SPEED, mult, 0.05),
				spec(Role.SWORDSMAN, 2, EntityAttributes.GENERIC_ATTACK_DAMAGE, add, 0.4));
		stats(Role.PALADIN, spec(Role.PALADIN, 1, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, add, 1),
				spec(Role.PALADIN, 2, EntityAttributes.GENERIC_MAX_HEALTH, add, 2));
		stats(Role.NECROMANCER, spec(Role.NECROMANCER, 1, null, add, 15),
				spec(Role.NECROMANCER, 2, EntityAttributes.GENERIC_MAX_HEALTH, add, 1));
		stats(Role.DRUID, spec(Role.DRUID, 1, EntityAttributes.GENERIC_MAX_HEALTH, add, 2),
				spec(Role.DRUID, 2, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.03));
		stats(Role.MERCHANT, spec(Role.MERCHANT, 1, EntityAttributes.GENERIC_LUCK, add, 1), spec(Role.MERCHANT, 2, null, add, 10));
		stats(Role.MONK, spec(Role.MONK, 1, EntityAttributes.GENERIC_ATTACK_SPEED, mult, 0.05),
				spec(Role.MONK, 2, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.03));
		stats(Role.ELEMENTALIST, spec(Role.ELEMENTALIST, 1, null, add, 15),
				spec(Role.ELEMENTALIST, 2, EntityAttributes.GENERIC_ARMOR, add, 1));
		stats(Role.SHAPESHIFTER, spec(Role.SHAPESHIFTER, 1, null, add, 10),
				spec(Role.SHAPESHIFTER, 2, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.03));
		stats(Role.GUNSLINGER, spec(Role.GUNSLINGER, 1, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.03),
				spec(Role.GUNSLINGER, 2, null, add, 10));
		stats(Role.WEAPON_SUMMONER, spec(Role.WEAPON_SUMMONER, 1, EntityAttributes.GENERIC_ATTACK_DAMAGE, add, 0.4),
				spec(Role.WEAPON_SUMMONER, 2, EntityAttributes.GENERIC_ARMOR, add, 1));
		stats(Role.HUNTER, spec(Role.HUNTER, 1, EntityAttributes.GENERIC_MOVEMENT_SPEED, mult, 0.04),
				spec(Role.HUNTER, 2, EntityAttributes.GENERIC_LUCK, add, 0.5));
		stats(Role.BARD, spec(Role.BARD, 1, EntityAttributes.GENERIC_MAX_HEALTH, add, 2), spec(Role.BARD, 2, null, add, 15));
		stats(Role.WARLOCK, spec(Role.WARLOCK, 1, null, add, 15),
				spec(Role.WARLOCK, 2, EntityAttributes.GENERIC_MAX_HEALTH, add, 1));
		stats(Role.TAMER, spec(Role.TAMER, 1, EntityAttributes.GENERIC_MAX_HEALTH, add, 2),
				spec(Role.TAMER, 2, EntityAttributes.GENERIC_ARMOR, add, 1));
	}

	// ---------------------------------------------------------------- Sendas

	/** Nodo de senda: rol, senda (1 = A, 2 = B) y efecto. */
	public record PathSpec(Role role, int path, PathEffect effect) {}

	private static final Map<String, PathSpec> PATH_SPECS = new LinkedHashMap<>();
	private static final Map<Role, PathEffect[][]> PATHS = new EnumMap<>(Role.class);

	private static void paths(Role role, PathEffect[] a, PathEffect[] b, PathEffect[] c) {
		PATHS.put(role, new PathEffect[][]{a, b, c});
	}

	private static PathEffect[] of(PathEffect... effects) {
		return effects;
	}

	static {
		PathEffect L = PathEffect.LIFESTEAL, C = PathEffect.CRIT, X = PathEffect.EXECUTE, T = PathEffect.THORNS,
				R = PathEffect.DAMAGE_REDUCTION, D = PathEffect.DODGE, M = PathEffect.MANA_REGEN,
				G = PathEffect.RESOURCE_GAIN, H = PathEffect.REGEN, P = PathEffect.ON_HIT_POISON,
				S = PathEffect.ON_HIT_SLOW, F = PathEffect.ON_HIT_FIRE, W = PathEffect.ON_HIT_WITHER,
				N = PathEffect.MINION_POWER, K = PathEffect.KNOCKBACK, V = PathEffect.SPEED,
				HP = PathEffect.MAX_HEALTH, AS = PathEffect.ATTACK_SPEED, LK = PathEffect.LUCK;
		paths(Role.BERSERKER, of(L, C, X), of(T, K, R), of(AS, V, H));           // Carnicero / Coloso
		paths(Role.MAGE, of(C, X, W), of(M, G, R), of(S, D, M));                // Arcanista / Erudito / Cronomante
		paths(Role.ASSASSIN, of(C, X, P), of(D, V, S), of(W, L, AS));            // Verdugo / Fantasma
		paths(Role.HERBALIST, of(H, HP, R), of(P, S, X), of(G, M, LK));          // Sanador / Envenenador
		paths(Role.ENGINEER, of(C, X, M), of(R, K, HP), of(AS, V, G));           // Artillero / Acorazado
		paths(Role.LANCER, of(C, V, S), of(T, K, R), of(L, HP, X));              // Dragón / Falange
		paths(Role.SWORDSMAN, of(D, C, AS), of(L, X, S), of(R, T, HP));          // Duelista / Kensei
		paths(Role.PALADIN, of(X, L, F), of(R, T, H), of(C, AS, G));             // Templario / Guardián
		paths(Role.NECROMANCER, of(N, G, M), of(L, W, HP), of(P, S, X));        // Señor de esbirros / Hematomante
		paths(Role.DRUID, of(HP, R, H), of(C, V, P), of(T, K, S));              // Guardián salvaje / Cazador lunar
		paths(Role.MERCHANT, of(G, M, LK), of(C, L, X), of(D, V, R));           // Tesorero / Mercenario
		paths(Role.MONK, of(C, AS, S), of(D, H, M), of(L, K, X));               // Puño de hierro / Loto
		paths(Role.ELEMENTALIST, of(C, X, F), of(M, G, R), of(S, D, V));        // Catalizador / Conductor
		paths(Role.GUNSLINGER, of(C, AS, X), of(M, G, D), of(L, V, S));        // Pistolero / Tirador arcano / Forajido
		paths(Role.WEAPON_SUMMONER, of(C, AS, L), of(R, T, HP), of(G, M, X));   // Espadas danzantes / Bastión / Forja rúnica
		paths(Role.HUNTER, of(C, X, S), of(P, D, V), of(L, HP, K));            // Francotirador / Trampero / Bestia
		paths(Role.BARD, of(G, M, LK), of(C, AS, F), of(H, R, HP));            // Juglar / Trovador de guerra / Cantor sagrado
		paths(Role.WARLOCK, of(W, P, X), of(L, HP, H), of(C, M, G));           // Maldito / Sangre maldita / Abismo
		paths(Role.TAMER, of(K, HP, R), of(C, AS, L), of(H, LK, G));           // Jauría / Salvaje / Pastor
		paths(Role.SHAPESHIFTER, of(C, L, V), of(D, G, M), of(HP, R, T));        // Depredador / Mimo
	}

	/**
	 * Se construye al primer uso: {@code define()} se ejecuta en el primer bloque
	 * static, antes de que se inicialicen estas tablas (declaradas más abajo).
	 */
	private static synchronized void ensurePathSpecs() {
		if (!PATH_SPECS.isEmpty()) return;
		PATHS.forEach((role, sets) -> {
			for (int path = 1; path <= 3; path++) {
				for (int i = 0; i < 3; i++) {
					String id = role.key() + PATH_LETTERS[path - 1] + (i + 1);
					PATH_SPECS.put(id, new PathSpec(role, path, sets[path - 1][i]));
				}
			}
		});
	}

	@Nullable
	public static PathSpec pathSpec(String nodeId) {
		ensurePathSpecs();
		return PATH_SPECS.get(nodeId);
	}

	public static Map<String, PathSpec> pathSpecs() {
		ensurePathSpecs();
		return Collections.unmodifiableMap(PATH_SPECS);
	}

	private static final String[] PATH_LETTERS = {"_a", "_b", "_c"};
	/** Sendas que se pueden seguir a la vez en un rol (de las 3). */
	public static final int MAX_PATHS = 2;

	/** ¿Tiene algún rango en la senda {@code path} (1 = A, 2 = B, 3 = C)? */
	public static boolean pathStarted(java.util.function.ToIntFunction<String> rankOf, Role role, int path) {
		for (int i = 1; i <= 3; i++) if (rankOf.applyAsInt(role.key() + PATH_LETTERS[path - 1] + i) > 0) return true;
		return false;
	}

	/** Número de sendas iniciadas en un rol. */
	public static int pathsStarted(java.util.function.ToIntFunction<String> rankOf, Role role) {
		int n = 0;
		for (int path = 1; path <= 3; path++) if (pathStarted(rankOf, role, path)) n++;
		return n;
	}

	/** ¿La senda está bloqueada porque ya se siguen otras dos? */
	public static boolean pathLocked(java.util.function.ToIntFunction<String> rankOf, Role role, int path) {
		return !pathStarted(rankOf, role, path) && pathsStarted(rankOf, role) >= MAX_PATHS;
	}

	/** Clave de texto del nombre de una senda ({@code path.aptitudes.<rol>_a|b|c}). */
	public static String pathKey(Role role, int path) {
		return "path.aptitudes." + role.key() + PATH_LETTERS[path - 1];
	}

	public static List<StatSpec> statSpecs() {
		List<StatSpec> out = new ArrayList<>();
		for (StatSpec[] pair : STATS.values()) out.addAll(List.of(pair));
		return out;
	}

	/** Posición de cada nodo en la cuadrícula del árbol (columna 0-8, fila 1-6; la fila 0 es del núcleo). */
	public record GridPos(int col, int row) {}

	private static RoleNode at(RoleNode node, int col, int row, int level, @Nullable String prerequisite) {
		RoleNode placed = new RoleNode(node.id(), node.role(), node.kind(), level, prerequisite, node.energyCost(),
				node.cooldownSecs());
		POSITIONS.put(placed.id(), new GridPos(col, row));
		return placed;
	}

	/**
	 * Árbol de 23 nodos con requisitos (cada nodo exige al menos 1 rango de su padre):
	 *
	 * <pre>
	 *            PASIVAS (0-2)        SENDAS (3-5)          ACTIVAS (6-8)
	 * fila 0:                   [Caract.] [NÚCLEO] [Definit.]
	 * fila 1:      [Pasiva A]              [MECÁNICA]              [Activa A]
	 * fila 2:  [Entr.1]  [Pasiva B]                       [Presteza]       [Activa B]
	 * fila 3:      [Reactiva 1]    [A1]   [B1]   [C1]        [Eficiencia]
	 * fila 4:  [Entr.2]            [A2]   [B2]   [C2]                      [Activa C]
	 * fila 5:      [Reactiva 2]    [A3]   [B3]   [C3]        [Maestría]
	 * fila 6:                                                              [Activa D]
	 * </pre>
	 *
	 * Las tres Sendas (A, B, C) nacen de la mecánica del rol; solo se pueden seguir dos.
	 * Orden en la lista: los 9 nodos clásicos, las 9 de senda y los 5 nuevos
	 * (mecánica, reactiva 1, reactiva 2, activa C, activa D).
	 */
	private static void define(Role role, RoleNode passiveA, RoleNode activeA, RoleNode passiveB, RoleNode activeB,
							   RoleNode mechanic, RoleNode reactive1, RoleNode reactive2, RoleNode activeC,
							   RoleNode activeD) {
		String k = role.key();
		// Fila 0 de la cuadrícula: núcleo, característica y definitiva (los dibuja la GUI).
		RoleNode pA = at(passiveA, 1, 1, passiveA.requiredLevel(), null);
		RoleNode mech = at(mechanic, 4, 1, 1, null);
		RoleNode aA = at(activeA, 7, 1, activeA.requiredLevel(), null);
		RoleNode pB = at(passiveB, 2, 2, passiveB.requiredLevel(), pA.id());
		RoleNode aB = at(activeB, 8, 2, activeB.requiredLevel(), aA.id());
		RoleNode r1 = at(reactive1, 1, 3, 10, pB.id());
		RoleNode r2 = at(reactive2, 1, 5, 18, r1.id());
		RoleNode aC = at(activeC, 8, 4, 14, aB.id());
		RoleNode aD = at(activeD, 8, 6, 22, aC.id());
		List<RoleNode> nodes = new ArrayList<>(List.of(pA, aA, pB, aB,
				at(RoleNode.extraNode(k + "_stat1", role, RoleNode.Kind.STAT, 4, null), 0, 2, 4, pA.id()),
				at(RoleNode.extraNode(k + "_stat2", role, RoleNode.Kind.STAT, 15, null), 0, 4, 15, k + "_stat1"),
				at(RoleNode.extraNode(k + "_haste", role, RoleNode.Kind.HASTE, 5, null), 6, 2, 5, aA.id()),
				at(RoleNode.extraNode(k + "_efficiency", role, RoleNode.Kind.EFFICIENCY, 10, null), 7, 3, 10, k + "_haste"),
				at(RoleNode.extraNode(k + "_mastery", role, RoleNode.Kind.MASTERY, 18, null), 7, 5, 18, k + "_efficiency")));
		// Sendas: A (columna 3), B (4) y C (5), filas 3-5; niveles 10, 14 y 20; nacen de la mecánica.
		String[] letters = {"_a", "_b", "_c"};
		int[] levels = {10, 14, 20};
		for (int path = 0; path < 3; path++) {
			for (int i = 0; i < 3; i++) {
				String id = k + letters[path] + (i + 1);
				String pre = i == 0 ? mech.id() : k + letters[path] + i;
				nodes.add(at(RoleNode.extraNode(id, role, RoleNode.Kind.PATH, levels[i], null), 3 + path, 3 + i,
						levels[i], pre));
			}
		}
		nodes.addAll(List.of(mech, r1, r2, aC, aD));
		// Nodo legendario (antigua habilidad única): fila 6, centro; solo exige nivel 25.
		nodes.add(at(new RoleNode(k + "_legend", role, RoleNode.Kind.LEGENDARY, 25, null, 0, 0), 4, 6, 25, null));
		for (RoleNode n : nodes) {
			if (BY_ID.put(n.id(), n) != null) throw new IllegalStateException("Nodo duplicado: " + n.id());
		}
		BY_ROLE.put(role, List.copyOf(nodes));
		ACTIVES.put(role, new RoleNode[]{aA, aB, aC, aD});
	}

	/** Posición del nodo en la cuadrícula (o null si no es un nodo del árbol). */
	@Nullable
	public static GridPos position(String nodeId) {
		return POSITIONS.get(nodeId);
	}

	/** Las 4 activas del rol (A, B, C, D). */
	public static List<RoleNode> actives(Role role) {
		RoleNode[] actives = ACTIVES.get(role);
		return actives == null ? List.of() : List.of(actives);
	}

	/** Índice (0-3) de una activa dentro de su rol, o -1. */
	public static int activeIndex(RoleNode node) {
		List<RoleNode> list = actives(node.role());
		return list.indexOf(node);
	}

	@Nullable
	public static RoleNode get(String id) {
		return BY_ID.get(id);
	}

	public static List<RoleNode> nodes(Role role) {
		return BY_ROLE.getOrDefault(role, List.of());
	}

	public static List<RoleNode> all() {
		return Collections.unmodifiableList(new ArrayList<>(BY_ID.values()));
	}

	/** @param index 0-3 = activa A, B, C, D */
	@Nullable
	public static RoleNode active(Role role, int index) {
		RoleNode[] actives = ACTIVES.get(role);
		return actives != null && index >= 0 && index < actives.length ? actives[index] : null;
	}
}
