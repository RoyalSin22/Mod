package dev.aptitudes.morph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Estado persistente de transformación de un jugador.
 * {@code activeType} es el id del tipo de entidad en el que está transformado
 * ("" = forma humana); puede ser cualquier mob (Espejo Perfecto copia cualquiera),
 * no solo las {@link MorphForm}.
 */
public final class MorphData {
	public static final Codec<MorphData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("kills", Map.of()).forGetter(d -> d.kills),
			Codec.INT.optionalFieldOf("selected", 0).forGetter(d -> d.selected),
			Codec.STRING.optionalFieldOf("active", "").forGetter(d -> d.activeType),
			Codec.FLOAT.optionalFieldOf("bonus_health", 0f).forGetter(d -> d.bonusHealth)
	).apply(i, MorphData::new));

	private final Map<String, Integer> kills;
	private int selected;
	private String activeType;
	/** Vida extra copiada por Espejo Perfecto (se aplica como modificador mientras dura). */
	private float bonusHealth;

	public MorphData() {
		this(Map.of(), 0, "", 0f);
	}

	private MorphData(Map<String, Integer> kills, int selected, String activeType, float bonusHealth) {
		this.kills = new HashMap<>(kills);
		this.selected = selected;
		this.activeType = activeType;
		this.bonusHealth = bonusHealth;
	}

	public int kills(MorphForm form) { return kills.getOrDefault(form.key(), 0); }
	public void addKill(MorphForm form) { kills.merge(form.key(), 1, Integer::sum); }
	public MorphForm selected() {
		MorphForm f = MorphForm.byIndex(selected);
		return f == null ? MorphForm.CREEPER : f;
	}
	public void setSelected(MorphForm form) { this.selected = form.ordinal(); }
	public String activeType() { return activeType; }
	public void setActiveType(String id) { this.activeType = id == null ? "" : id; }
	public boolean isMorphed() { return !activeType.isEmpty(); }
	public float bonusHealth() { return bonusHealth; }
	public void setBonusHealth(float value) { this.bonusHealth = Math.max(0f, value); }
}
