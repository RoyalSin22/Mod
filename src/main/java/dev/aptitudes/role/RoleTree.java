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
 * Las activas A/B de cada rol ocupan las ranuras 1/2 (principal) o 3/4 (secundario).
 */
public final class RoleTree {
	private static final Map<String, RoleNode> BY_ID = new LinkedHashMap<>();
	private static final Map<Role, List<RoleNode>> BY_ROLE = new EnumMap<>(Role.class);
	private static final Map<Role, RoleNode[]> ACTIVES = new EnumMap<>(Role.class);

	static {
		// Bersérker: aguanta, se enfurece y castiga más cuanto menos vida tiene.
		define(Role.BERSERKER,
				passiveNode("tough_skin", Role.BERSERKER, 1, null),
				activeNode("rage", Role.BERSERKER, 3, null, 35, 45),
				passiveNode("bloodlust", Role.BERSERKER, 8, "tough_skin"),
				activeNode("war_cry", Role.BERSERKER, 12, "rage", 30, 30));
		// Mago: gran reserva de energía y hechizos a distancia.
		define(Role.MAGE,
				passiveNode("arcane_affinity", Role.MAGE, 1, null),
				activeNode("fireball", Role.MAGE, 3, null, 25, 6),
				passiveNode("clear_mind", Role.MAGE, 8, "arcane_affinity"),
				activeNode("blink", Role.MAGE, 12, "fireball", 30, 12));
		// Asesino: sigilo y golpes por la espalda.
		define(Role.ASSASSIN,
				passiveNode("shadow", Role.ASSASSIN, 1, null),
				activeNode("vanish", Role.ASSASSIN, 3, null, 40, 40),
				passiveNode("backstab", Role.ASSASSIN, 8, "shadow"),
				activeNode("shadow_step", Role.ASSASSIN, 12, "vanish", 35, 18));
		// Herbolista: curación, regeneración y venenos.
		define(Role.HERBALIST,
				passiveNode("vital_sap", Role.HERBALIST, 1, null),
				activeNode("balm", Role.HERBALIST, 3, null, 30, 25),
				passiveNode("herbal_immunity", Role.HERBALIST, 8, "vital_sap"),
				activeNode("poison_thorns", Role.HERBALIST, 12, "balm", 30, 25));
		// Ingeniero (Tecnólogo + Futurista): ingeniería, propulsores, baterías y torreta.
		define(Role.ENGINEER,
				passiveNode("engineering", Role.ENGINEER, 1, null),
				activeNode("thrusters", Role.ENGINEER, 3, null, 20, 8),
				passiveNode("battery", Role.ENGINEER, 8, "engineering"),
				activeNode("turret", Role.ENGINEER, 12, "thrusters", 60, 90));
		// Lancero: alcance y cargas.
		define(Role.LANCER,
				passiveNode("reach", Role.LANCER, 1, null),
				activeNode("lunge", Role.LANCER, 3, null, 30, 14),
				passiveNode("charge", Role.LANCER, 8, "reach"),
				activeNode("sweep", Role.LANCER, 12, "lunge", 30, 12));
		// Espadachín: filo, ritmo y contraataques.
		define(Role.SWORDSMAN,
				passiveNode("keen_edge", Role.SWORDSMAN, 1, null),
				activeNode("blade_dance", Role.SWORDSMAN, 3, null, 30, 40),
				passiveNode("parry", Role.SWORDSMAN, 8, "keen_edge"),
				activeNode("riposte", Role.SWORDSMAN, 12, "blade_dance", 25, 15));
		// Paladín: luz contra los no-muertos, escudo divino y aura protectora.
		define(Role.PALADIN,
				passiveNode("holy_smite", Role.PALADIN, 1, null),
				activeNode("divine_shield", Role.PALADIN, 3, null, 40, 35),
				passiveNode("protective_aura", Role.PALADIN, 8, "holy_smite"),
				activeNode("judgement", Role.PALADIN, 12, "divine_shield", 40, 30));
		// Nigromante: esbirros aliados que escalan con su nivel y drenaje de vida.
		define(Role.NECROMANCER,
				passiveNode("dark_pact", Role.NECROMANCER, 1, null),
				activeNode("raise_minions", Role.NECROMANCER, 3, null, 50, 60),
				passiveNode("life_drain", Role.NECROMANCER, 8, "dark_pact"),
				activeNode("death_touch", Role.NECROMANCER, 12, "raise_minions", 35, 20));
		// Druida: bosque, crecimiento, raíces y forma de oso.
		define(Role.DRUID,
				passiveNode("forest_child", Role.DRUID, 1, null),
				activeNode("entangling_roots", Role.DRUID, 3, null, 30, 20),
				passiveNode("growth", Role.DRUID, 8, "forest_child"),
				activeNode("bear_form", Role.DRUID, 12, "entangling_roots", 45, 60));
		// Mercader: descuentos, tasación, ganancias extra y soborno.
		define(Role.MERCHANT,
				passiveNode("haggling", Role.MERCHANT, 1, null),
				activeNode("appraiser_eye", Role.MERCHANT, 3, null, 25, 60),
				passiveNode("deep_pockets", Role.MERCHANT, 8, "haggling"),
				activeNode("bribe", Role.MERCHANT, 12, "appraiser_eye", 30, 45));
		// Monje: artes marciales sin armas.
		define(Role.MONK,
				passiveNode("iron_fists", Role.MONK, 1, null),
				activeNode("chi_palm", Role.MONK, 3, null, 25, 8),
				passiveNode("evasion", Role.MONK, 8, "iron_fists"),
				activeNode("meditation", Role.MONK, 12, "chi_palm", 0, 60));
		// Elementalista: domina hasta 3 elementos; sus nodos actúan según el elemento activo.
		define(Role.ELEMENTALIST,
				passiveNode("elemental_affinity", Role.ELEMENTALIST, 1, null),
				activeNode("elemental_bolt", Role.ELEMENTALIST, 3, null, 15, 3),
				passiveNode("elemental_dominion", Role.ELEMENTALIST, 8, "elemental_affinity"),
				activeNode("elemental_burst", Role.ELEMENTALIST, 12, "elemental_bolt", 35, 18));
		// Cambiaformas: se transforma en mobs vanilla (desbloqueados al derrotarlos) y usa sus poderes.
		define(Role.SHAPESHIFTER,
				passiveNode("mimicry", Role.SHAPESHIFTER, 1, null),
				activeNode("transform", Role.SHAPESHIFTER, 1, null, 15, 3),
				passiveNode("trickster", Role.SHAPESHIFTER, 8, "mimicry"),
				activeNode("form_power", Role.SHAPESHIFTER, 5, "transform", 25, 8));
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
	}

	// ---------------------------------------------------------------- Sendas

	/** Nodo de senda: rol, senda (1 = A, 2 = B) y efecto. */
	public record PathSpec(Role role, int path, PathEffect effect) {}

	private static final Map<String, PathSpec> PATH_SPECS = new LinkedHashMap<>();
	private static final Map<Role, PathEffect[][]> PATHS = new EnumMap<>(Role.class);

	private static void paths(Role role, PathEffect[] a, PathEffect[] b) {
		PATHS.put(role, new PathEffect[][]{a, b});
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
		paths(Role.BERSERKER, of(L, C, X), of(T, K, R));           // Carnicero / Coloso
		paths(Role.MAGE, of(C, F, X), of(M, G, R));                // Piromante / Erudito
		paths(Role.ASSASSIN, of(C, X, P), of(D, V, S));            // Verdugo / Fantasma
		paths(Role.HERBALIST, of(H, HP, R), of(P, S, X));          // Sanador / Envenenador
		paths(Role.ENGINEER, of(C, X, M), of(R, K, HP));           // Artillero / Acorazado
		paths(Role.LANCER, of(C, V, S), of(T, K, R));              // Dragón / Falange
		paths(Role.SWORDSMAN, of(D, C, AS), of(L, X, S));          // Duelista / Kensei
		paths(Role.PALADIN, of(X, L, F), of(R, T, H));             // Templario / Guardián
		paths(Role.NECROMANCER, of(N, G, M), of(L, W, HP));        // Señor de esbirros / Hematomante
		paths(Role.DRUID, of(HP, R, H), of(C, V, P));              // Guardián salvaje / Cazador lunar
		paths(Role.MERCHANT, of(G, M, LK), of(C, L, X));           // Tesorero / Mercenario
		paths(Role.MONK, of(C, AS, S), of(D, H, M));               // Puño de hierro / Loto
		paths(Role.ELEMENTALIST, of(C, X, F), of(M, G, R));        // Catalizador / Conductor
		paths(Role.SHAPESHIFTER, of(C, L, V), of(D, G, M));        // Depredador / Mimo
	}

	/**
	 * Se construye al primer uso: {@code define()} se ejecuta en el primer bloque
	 * static, antes de que se inicialicen estas tablas (declaradas más abajo).
	 */
	private static synchronized void ensurePathSpecs() {
		if (!PATH_SPECS.isEmpty()) return;
		PATHS.forEach((role, sets) -> {
			for (int path = 1; path <= 2; path++) {
				for (int i = 0; i < 3; i++) {
					String id = role.key() + (path == 1 ? "_a" : "_b") + (i + 1);
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

	/** Senda elegida en un rol según los rangos: 0 = ninguna, 1 = A, 2 = B. */
	public static int chosenPath(RoleData data, Role role) {
		for (int i = 1; i <= 3; i++) {
			if (data.rank(role.key() + "_a" + i) > 0) return 1;
			if (data.rank(role.key() + "_b" + i) > 0) return 2;
		}
		return 0;
	}

	/** Clave de texto del nombre de una senda ({@code path.aptitudes.<rol>_a|b}). */
	public static String pathKey(Role role, int path) {
		return "path.aptitudes." + role.key() + (path == 1 ? "_a" : "_b");
	}

	public static List<StatSpec> statSpecs() {
		List<StatSpec> out = new ArrayList<>();
		for (StatSpec[] pair : STATS.values()) out.addAll(List.of(pair));
		return out;
	}

	/**
	 * Cada rol: 4 nodos propios + 5 comunes (2 entrenamientos, Presteza, Eficiencia, Maestría).
	 * Orden en la lista: pasiva A, activa A, pasiva B, activa B, entrenamiento 1, entrenamiento 2,
	 * Presteza, Eficiencia, Maestría.
	 */
	private static void define(Role role, RoleNode passiveA, RoleNode activeA, RoleNode passiveB, RoleNode activeB) {
		String k = role.key();
		List<RoleNode> nodes = List.of(passiveA, activeA, passiveB, activeB,
				RoleNode.extraNode(k + "_stat1", role, RoleNode.Kind.STAT, 5, passiveA.id()),
				RoleNode.extraNode(k + "_stat2", role, RoleNode.Kind.STAT, 15, k + "_stat1"),
				RoleNode.extraNode(k + "_haste", role, RoleNode.Kind.HASTE, 6, activeA.id()),
				RoleNode.extraNode(k + "_efficiency", role, RoleNode.Kind.EFFICIENCY, 10, k + "_haste"),
				RoleNode.extraNode(k + "_mastery", role, RoleNode.Kind.MASTERY, 18, activeB.id()),
				// Sendas excluyentes: A (a1→a2→a3) y B (b1→b2→b3), niveles 10, 14 y 20.
				RoleNode.extraNode(k + "_a1", role, RoleNode.Kind.PATH, 10, null),
				RoleNode.extraNode(k + "_a2", role, RoleNode.Kind.PATH, 14, k + "_a1"),
				RoleNode.extraNode(k + "_a3", role, RoleNode.Kind.PATH, 20, k + "_a2"),
				RoleNode.extraNode(k + "_b1", role, RoleNode.Kind.PATH, 10, null),
				RoleNode.extraNode(k + "_b2", role, RoleNode.Kind.PATH, 14, k + "_b1"),
				RoleNode.extraNode(k + "_b3", role, RoleNode.Kind.PATH, 20, k + "_b2"));
		for (RoleNode n : nodes) {
			if (BY_ID.put(n.id(), n) != null) throw new IllegalStateException("Nodo duplicado: " + n.id());
		}
		BY_ROLE.put(role, nodes);
		ACTIVES.put(role, new RoleNode[]{activeA, activeB});
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

	/** @param index 0 = activa A, 1 = activa B */
	@Nullable
	public static RoleNode active(Role role, int index) {
		RoleNode[] actives = ACTIVES.get(role);
		return actives != null && index >= 0 && index < actives.length ? actives[index] : null;
	}
}
