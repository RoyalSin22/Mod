package dev.aptitudes.role;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * V96 (5.ª tanda): a qué guion visual de batalla pertenece cada una de las 100 activas de los nodos extra (N3, N4, N6, N8 y N10 de los
 * 20 roles). Cada una se reduce a una de las diez formas de las activas de evolución ({@link Shape}, mismos nombres que
 * {@code EvolvedTree.Template}), que ya tienen gesto, sonido y capa de partículas. Puro (sin Minecraft), con pruebas.
 */
public final class ExtraFxShapes {
	/** Mismas formas y mismo orden que {@code EvolvedTree.Template}. */
	public enum Shape { NOVA, BEAM, CONE, DASH, ZONE, VORTEX, AURA, HEAL, SHIELD, SUMMON }

	private static final Map<String, Shape[]> TABLE = new HashMap<>();

	static {
		// Orden: N3, N4, N6, N8, N10.
		row("berserker", Shape.BEAM, Shape.VORTEX, Shape.CONE, Shape.AURA, Shape.NOVA);
		row("mage", Shape.BEAM, Shape.SHIELD, Shape.ZONE, Shape.AURA, Shape.NOVA);
		row("assassin", Shape.NOVA, Shape.DASH, Shape.BEAM, Shape.AURA, Shape.AURA);
		row("herbalist", Shape.ZONE, Shape.HEAL, Shape.VORTEX, Shape.HEAL, Shape.ZONE);
		row("engineer", Shape.ZONE, Shape.SUMMON, Shape.SHIELD, Shape.AURA, Shape.SUMMON);
		row("lancer", Shape.BEAM, Shape.ZONE, Shape.CONE, Shape.BEAM, Shape.ZONE);
		row("swordsman", Shape.CONE, Shape.NOVA, Shape.DASH, Shape.AURA, Shape.BEAM);
		row("paladin", Shape.BEAM, Shape.CONE, Shape.SHIELD, Shape.AURA, Shape.ZONE);
		row("necromancer", Shape.BEAM, Shape.AURA, Shape.ZONE, Shape.AURA, Shape.VORTEX);
		row("druid", Shape.CONE, Shape.HEAL, Shape.NOVA, Shape.AURA, Shape.ZONE);
		row("merchant", Shape.BEAM, Shape.ZONE, Shape.SUMMON, Shape.AURA, Shape.NOVA);
		row("monk", Shape.NOVA, Shape.CONE, Shape.BEAM, Shape.AURA, Shape.VORTEX);
		row("elementalist", Shape.BEAM, Shape.ZONE, Shape.SUMMON, Shape.AURA, Shape.VORTEX);
		row("shapeshifter", Shape.DASH, Shape.AURA, Shape.CONE, Shape.AURA, Shape.DASH);
		row("gunslinger", Shape.BEAM, Shape.ZONE, Shape.BEAM, Shape.AURA, Shape.BEAM);
		row("weapon_summoner", Shape.BEAM, Shape.SHIELD, Shape.NOVA, Shape.SUMMON, Shape.ZONE);
		row("hunter", Shape.ZONE, Shape.BEAM, Shape.CONE, Shape.AURA, Shape.BEAM);
		row("bard", Shape.BEAM, Shape.AURA, Shape.NOVA, Shape.AURA, Shape.NOVA);
		row("warlock", Shape.BEAM, Shape.VORTEX, Shape.ZONE, Shape.AURA, Shape.VORTEX);
		row("tamer", Shape.SUMMON, Shape.AURA, Shape.SHIELD, Shape.AURA, Shape.SUMMON);
	}

	private static final int[] NUMBERS = {3, 4, 6, 8, 10};

	private ExtraFxShapes() {
	}

	private static void row(String role, Shape... shapes) {
		TABLE.put(role, shapes);
	}

	/** Forma de la activa ({@code <rol>_n<N>}), o null si no es una activa extra conocida. */
	public static Shape shapeOf(String abilityId) {
		if (abilityId == null) return null;
		int cut = abilityId.lastIndexOf("_n");
		if (cut <= 0) return null;
		Shape[] shapes = TABLE.get(abilityId.substring(0, cut).toLowerCase(Locale.ROOT));
		if (shapes == null) return null;
		int n;
		try {
			n = Integer.parseInt(abilityId.substring(cut + 2));
		} catch (NumberFormatException e) {
			return null;
		}
		for (int i = 0; i < NUMBERS.length; i++) if (NUMBERS[i] == n) return shapes[i];
		return null;
	}

	/** ¿Es una de las activas grandes (N8 y N10, filas de Ascenso)? Suben la intensidad del guion. */
	public static boolean big(String abilityId) {
		return abilityId != null && (abilityId.endsWith("_n8") || abilityId.endsWith("_n10"));
	}

	/** Roles con tabla (para las pruebas). */
	public static java.util.Set<String> roles() {
		return java.util.Collections.unmodifiableSet(TABLE.keySet());
	}
}
