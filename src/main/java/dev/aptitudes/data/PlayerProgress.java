package dev.aptitudes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Estado persistente de un jugador.
 *
 * <p>Decisión de diseño: los puntos disponibles NO se guardan, se derivan:
 * {@code inicial + niveles * puntosPorNivel + bonus - asignados}. Así, cambios
 * de configuración, comandos de admin o reseteos nunca dejan el estado
 * incoherente (no hay contador que "desincronizar").</p>
 *
 * <p>Se serializa con claves de texto (no ordinales) para que añadir o
 * reordenar habilidades en futuras versiones no corrompa partidas existentes.</p>
 */
public final class PlayerProgress {
	private static final Codec<Map<String, Integer>> INT_MAP = Codec.unboundedMap(Codec.STRING, Codec.INT);

	public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			INT_MAP.optionalFieldOf("levels", Map.of()).forGetter(p -> toNames(p.levels)),
			INT_MAP.optionalFieldOf("xp", Map.of()).forGetter(p -> toNames(p.xp)),
			INT_MAP.optionalFieldOf("allocated", Map.of()).forGetter(p -> toNames(p.allocated)),
			Codec.INT.optionalFieldOf("bonus_points", 0).forGetter(p -> p.bonusPoints),
			Codec.INT.optionalFieldOf("soul_level", 0).forGetter(p -> p.soulLevel)
	).apply(instance, PlayerProgress::new));

	private final EnumMap<Skill, Integer> levels;
	private final EnumMap<Skill, Integer> xp;
	private final EnumMap<PlayerAttribute, Integer> allocated;
	private int bonusPoints;
	/** Nivel de alma: niveles comprados en una Hoguera (cada uno = 1 punto de atributo). */
	private int soulLevel;

	public PlayerProgress() {
		this(Map.of(), Map.of(), Map.of(), 0, 0);
	}

	private PlayerProgress(Map<String, Integer> levels, Map<String, Integer> xp,
						   Map<String, Integer> allocated, int bonusPoints, int soulLevel) {
		this.levels = fromNames(Skill.class, levels);
		this.xp = fromNames(Skill.class, xp);
		this.allocated = fromNames(PlayerAttribute.class, allocated);
		this.bonusPoints = bonusPoints;
		this.soulLevel = Math.max(0, soulLevel);
	}

	public int getSoulLevel() {
		return soulLevel;
	}

	public void setSoulLevel(int soulLevel) {
		this.soulLevel = Math.max(0, soulLevel);
	}

	public int getLevel(Skill skill) {
		return levels.getOrDefault(skill, 0);
	}

	public void setLevel(Skill skill, int level) {
		levels.put(skill, Math.max(0, level));
	}

	public int getXp(Skill skill) {
		return xp.getOrDefault(skill, 0);
	}

	public void setXp(Skill skill, int value) {
		xp.put(skill, Math.max(0, value));
	}

	public int getAllocated(PlayerAttribute attribute) {
		return allocated.getOrDefault(attribute, 0);
	}

	public void setAllocated(PlayerAttribute attribute, int value) {
		allocated.put(attribute, Math.max(0, value));
	}

	public void clearAllocations() {
		allocated.clear();
	}

	public int getBonusPoints() {
		return bonusPoints;
	}

	public void setBonusPoints(int bonusPoints) {
		this.bonusPoints = bonusPoints;
	}

	public int totalLevels() {
		int sum = 0;
		for (int v : levels.values()) sum += v;
		return sum;
	}

	public int totalAllocated() {
		int sum = 0;
		for (int v : allocated.values()) sum += v;
		return sum;
	}

	private static <E extends Enum<E>> Map<String, Integer> toNames(EnumMap<E, Integer> map) {
		Map<String, Integer> out = new HashMap<>();
		map.forEach((k, v) -> {
			if (v != 0) out.put(k.name().toLowerCase(Locale.ROOT), v);
		});
		return out;
	}

	private static <E extends Enum<E>> EnumMap<E, Integer> fromNames(Class<E> type, Map<String, Integer> map) {
		EnumMap<E, Integer> out = new EnumMap<>(type);
		map.forEach((k, v) -> {
			try {
				out.put(Enum.valueOf(type, k.toUpperCase(Locale.ROOT)), Math.max(0, v));
			} catch (IllegalArgumentException ignored) {
				// Clave de una versión anterior/posterior del mod: se descarta.
			}
		});
		return out;
	}
}
