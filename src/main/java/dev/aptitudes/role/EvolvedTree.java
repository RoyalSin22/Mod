package dev.aptitudes.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.aptitudes.role.PathEffect.*;

/**
 * Filas del árbol que se desbloquean al evolucionar (v23): +5 nodos con Maestro y +5 con Definitivo en
 * cada rol (3 pasivas y 2 activas). Todo es DATOS: las pasivas reutilizan {@link PathEffect} y las activas
 * son una {@link Template} con parámetros que ejecuta {@code dev.aptitudes.ability.EvolvedAbilities}.
 *
 * <p>Ids estables (no renombrar: se guardan en el mundo): {@code <rol>_m1..m5} (Maestro) y
 * {@code <rol>_u1..u5} (Definitivo); 1-3 pasivas, 4-5 activas.</p>
 *
 * <ul>
 *   <li>Maestro: nivel 20, 2 rangos (el 2.º a nivel 25); valor por rango = ×1,5 el de senda.</li>
 *   <li>Definitivo: nivel 30 (el máximo), 1 rango; valor = ×3 el de senda.</li>
 * </ul>
 */
public final class EvolvedTree {

	/** Plantillas de las activas evolucionadas. */
	public enum Template {
		/** Explosión alrededor del jugador. */
		NOVA,
		/** Rayo en línea recta. */
		BEAM,
		/** Barrido en cono frontal. */
		CONE,
		/** Embestida hacia delante. */
		DASH,
		/** Lluvia de impactos sobre el punto señalado. */
		ZONE,
		/** Atrae a los enemigos a un punto. */
		VORTEX,
		/** Mejora al jugador y a sus aliados. */
		AURA,
		/** Cura al jugador y a sus aliados. */
		HEAL,
		/** Absorción y resistencia. */
		SHIELD,
		/** Invoca aliados temporales. */
		SUMMON
	}

	/** Efecto adicional de una activa (o tipo de aliado/mejora, según la plantilla). */
	public enum Extra {
		NONE, SLOW, FIRE, WITHER, POISON, LAUNCH, KNOCKBACK,
		/** AURA: Fuerza y Resistencia. */
		FORCE,
		/** AURA: Invisibilidad y Velocidad. */
		SHADOW,
		/** AURA: Velocidad, Fuerza y Regeneración. */
		SONG,
		/** SUMMON: zombis. */
		ZOMBIES,
		/** SUMMON: esqueletos. */
		SKELETONS,
		/** SUMMON: lobos. */
		WOLVES,
		/** SUMMON: vexes. */
		VEXES
	}

	public record Passive(String id, Role role, int tier, PathEffect effect) {
		/** Multiplicador sobre el valor por rango de la senda. */
		public double multiplier() {
			return tier >= Evolution.ULTIMATE ? 3.0 : 1.5;
		}

		public double perRank() {
			return effect.perRank() * multiplier();
		}
	}

	public record Active(String id, Role role, int tier, Template template, Extra extra, int color) {
		/** Daño base (sin rango); las plantillas lo escalan con el rango efectivo. */
		public float baseDamage() {
			return tier >= Evolution.ULTIMATE ? 14f : 8f;
		}

		public int energyCost() {
			int base = tier >= Evolution.ULTIMATE ? 50 : 35;
			return template == Template.SUMMON ? base + 10 : base;
		}

		public int cooldownSecs() {
			int base = tier >= Evolution.ULTIMATE ? 30 : 16;
			return switch (template) {
				case SUMMON -> base * 3;
				case AURA, SHIELD, HEAL -> base * 2;
				default -> base;
			};
		}
	}

	public static final int MASTER_LEVEL = Evolution.MASTER_LEVEL;   // 20
	public static final int ULTIMATE_LEVEL = Evolution.ULTIMATE_LEVEL; // 30

	private static final Map<String, Passive> PASSIVES = new LinkedHashMap<>();
	private static final Map<String, Active> ACTIVES = new LinkedHashMap<>();
	private static final Map<Role, List<String>> ORDER = new EnumMap<>(Role.class);

	private EvolvedTree() {
	}

	private record A(Template t, Extra e) {}

	private static A a(Template t, Extra e) {
		return new A(t, e);
	}

	private static void role(Role role, int color, PathEffect m1, PathEffect m2, PathEffect m3, A m4, A m5,
							 PathEffect u1, PathEffect u2, PathEffect u3, A u4, A u5) {
		String k = role.key();
		List<String> ids = new ArrayList<>();
		PathEffect[] mp = {m1, m2, m3};
		PathEffect[] up = {u1, u2, u3};
		for (int i = 0; i < 3; i++) {
			put(new Passive(k + "_m" + (i + 1), role, Evolution.MASTER, mp[i]), ids);
		}
		put(new Active(k + "_m4", role, Evolution.MASTER, m4.t(), m4.e(), color), ids);
		put(new Active(k + "_m5", role, Evolution.MASTER, m5.t(), m5.e(), color), ids);
		for (int i = 0; i < 3; i++) {
			put(new Passive(k + "_u" + (i + 1), role, Evolution.ULTIMATE, up[i]), ids);
		}
		put(new Active(k + "_u4", role, Evolution.ULTIMATE, u4.t(), u4.e(), color), ids);
		put(new Active(k + "_u5", role, Evolution.ULTIMATE, u5.t(), u5.e(), color), ids);
		ORDER.put(role, List.copyOf(ids));
	}

	private static void put(Passive p, List<String> ids) {
		PASSIVES.put(p.id(), p);
		ids.add(p.id());
	}

	private static void put(Active a, List<String> ids) {
		ACTIVES.put(a.id(), a);
		ids.add(a.id());
	}

	static {
		Template NOVA = Template.NOVA, BEAM = Template.BEAM, CONE = Template.CONE, DASH = Template.DASH,
				ZONE = Template.ZONE, VORTEX = Template.VORTEX, AURA = Template.AURA, HEAL = Template.HEAL,
				SHIELD = Template.SHIELD, SUMMON = Template.SUMMON;
		Extra N = Extra.NONE, SL = Extra.SLOW, FI = Extra.FIRE, WI = Extra.WITHER, PO = Extra.POISON,
				LA = Extra.LAUNCH, KB = Extra.KNOCKBACK;
		role(Role.BERSERKER, 0xC0392B, LIFESTEAL, DAMAGE_REDUCTION, ATTACK_SPEED, a(CONE, SL), a(DASH, N),
				EXECUTE, MAX_HEALTH, CRIT, a(NOVA, LA), a(AURA, Extra.FORCE));
		role(Role.MAGE, 0x8E44AD, MANA_REGEN, CRIT, DAMAGE_REDUCTION, a(BEAM, N), a(NOVA, SL),
				RESOURCE_GAIN, SPEED, EXECUTE, a(ZONE, FI), a(VORTEX, N));
		role(Role.ASSASSIN, 0x5D6D7E, CRIT, DODGE, ON_HIT_POISON, a(NOVA, PO), a(DASH, N),
				EXECUTE, SPEED, LIFESTEAL, a(CONE, N), a(AURA, Extra.SHADOW));
		role(Role.HERBALIST, 0x27AE60, REGEN, MAX_HEALTH, ON_HIT_POISON, a(HEAL, N), a(ZONE, PO),
				REGEN, DAMAGE_REDUCTION, LUCK, a(HEAL, N), a(VORTEX, SL));
		role(Role.ENGINEER, 0xD35400, DAMAGE_REDUCTION, ATTACK_SPEED, ON_HIT_FIRE, a(NOVA, SL), a(ZONE, N),
				MAX_HEALTH, CRIT, RESOURCE_GAIN, a(BEAM, FI), a(SHIELD, N));
		role(Role.LANCER, 0x2980B9, KNOCKBACK, CRIT, SPEED, a(DASH, N), a(NOVA, KB),
				EXECUTE, DAMAGE_REDUCTION, ATTACK_SPEED, a(ZONE, N), a(BEAM, N));
		role(Role.SWORDSMAN, 0xBDC3C7, CRIT, DODGE, ATTACK_SPEED, a(CONE, N), a(DASH, N),
				EXECUTE, LIFESTEAL, DAMAGE_REDUCTION, a(NOVA, N), a(BEAM, N));
		role(Role.PALADIN, 0xF1C40F, REGEN, DAMAGE_REDUCTION, ON_HIT_FIRE, a(NOVA, KB), a(HEAL, N),
				MAX_HEALTH, EXECUTE, THORNS, a(ZONE, FI), a(SHIELD, N));
		role(Role.NECROMANCER, 0x6C3483, MINION_POWER, LIFESTEAL, ON_HIT_WITHER, a(SUMMON, Extra.ZOMBIES), a(ZONE, WI),
				MINION_POWER, MAX_HEALTH, ON_HIT_SLOW, a(SUMMON, Extra.SKELETONS), a(VORTEX, WI));
		role(Role.DRUID, 0x6E8B3D, MAX_HEALTH, SPEED, CRIT, a(CONE, N), a(HEAL, N),
				REGEN, DAMAGE_REDUCTION, THORNS, a(SUMMON, Extra.WOLVES), a(ZONE, N));
		role(Role.MERCHANT, 0x16A085, LUCK, RESOURCE_GAIN, MANA_REGEN, a(ZONE, N), a(SUMMON, Extra.WOLVES),
				LUCK, CRIT, DAMAGE_REDUCTION, a(NOVA, KB), a(SHIELD, N));
		role(Role.MONK, 0xE67E22, ATTACK_SPEED, REGEN, DODGE, a(CONE, KB), a(DASH, N),
				MANA_REGEN, DAMAGE_REDUCTION, CRIT, a(NOVA, N), a(HEAL, N));
		role(Role.ELEMENTALIST, 0x3498DB, CRIT, MANA_REGEN, ON_HIT_FIRE, a(BEAM, N), a(ZONE, SL),
				RESOURCE_GAIN, ON_HIT_SLOW, DAMAGE_REDUCTION, a(NOVA, FI), a(VORTEX, N));
		role(Role.SHAPESHIFTER, 0x9B59B6, DODGE, CRIT, MAX_HEALTH, a(DASH, N), a(NOVA, KB),
				LIFESTEAL, SPEED, THORNS, a(SUMMON, Extra.WOLVES), a(CONE, N));
		role(Role.GUNSLINGER, 0x00BCD4, ATTACK_SPEED, CRIT, DODGE, a(CONE, N), a(BEAM, N),
				EXECUTE, SPEED, MANA_REGEN, a(ZONE, N), a(BEAM, FI));
		role(Role.WEAPON_SUMMONER, 0x90A4AE, RESOURCE_GAIN, CRIT, DAMAGE_REDUCTION, a(CONE, N), a(SHIELD, N),
				ATTACK_SPEED, EXECUTE, THORNS, a(ZONE, N), a(BEAM, N));
		role(Role.HUNTER, 0x7CB342, SPEED, CRIT, ON_HIT_SLOW, a(CONE, N), a(DASH, N),
				EXECUTE, DODGE, LIFESTEAL, a(ZONE, N), a(SUMMON, Extra.WOLVES));
		role(Role.BARD, 0xEC407A, REGEN, ATTACK_SPEED, RESOURCE_GAIN, a(NOVA, SL), a(HEAL, N),
				MANA_REGEN, MAX_HEALTH, ON_HIT_SLOW, a(AURA, Extra.SONG), a(NOVA, KB));
		role(Role.WARLOCK, 0xAD1457, ON_HIT_WITHER, LIFESTEAL, MANA_REGEN, a(BEAM, WI), a(ZONE, WI),
				EXECUTE, MAX_HEALTH, ON_HIT_POISON, a(VORTEX, WI), a(SUMMON, Extra.VEXES));
		role(Role.TAMER, 0xA1887F, MAX_HEALTH, MINION_POWER, SPEED, a(SUMMON, Extra.WOLVES), a(DASH, N),
				MINION_POWER, DAMAGE_REDUCTION, CRIT, a(AURA, Extra.FORCE), a(CONE, KB));
	}

	/** Ids de los 10 nodos evolucionados de un rol, en orden (m1..m5, u1..u5). */
	public static List<String> ids(Role role) {
		return ORDER.getOrDefault(role, List.of());
	}

	public static Passive passive(String id) {
		return PASSIVES.get(id);
	}

	public static Active active(String id) {
		return ACTIVES.get(id);
	}

	public static Map<String, Passive> passives() {
		return Collections.unmodifiableMap(PASSIVES);
	}

	public static Map<String, Active> actives() {
		return Collections.unmodifiableMap(ACTIVES);
	}
}
