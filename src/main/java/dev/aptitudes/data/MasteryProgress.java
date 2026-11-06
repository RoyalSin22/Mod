package dev.aptitudes.data;

import com.mojang.serialization.Codec;
import dev.aptitudes.logic.MasteryRules;

import java.util.LinkedHashMap;
import java.util.Map;

/** Historial por claves skill/... y role/...; puro y compatible con progresos antiguos sin este campo. */
public final class MasteryProgress {
	public static final Codec<MasteryProgress> CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG)
			.xmap(MasteryProgress::new, MasteryProgress::saved);
	private final Map<String, Long> xp = new LinkedHashMap<>();

	public MasteryProgress() { }
	public MasteryProgress(Map<String, Long> saved) {
		saved.forEach((key, value) -> {
			if (valid(key) && value != null && xp.size() < 64) xp.put(key, MasteryRules.sanitize(value));
		});
	}

	private static boolean valid(String key) {
		return key != null && key.length() <= 48 && key.matches("(skill|role)/[a-z_]+");
	}

	public long xp(String key) { return xp.getOrDefault(key, 0L); }
	public void add(String key, long amount) {
		if (amount <= 0 || !valid(key) || (xp.size() >= 64 && !xp.containsKey(key))) return;
		xp.put(key, MasteryRules.add(xp(key), amount));
	}
	public Map<String, Long> saved() { return Map.copyOf(xp); }
	public void clearRoles() { xp.keySet().removeIf(key -> key.startsWith("role/")); }
}
