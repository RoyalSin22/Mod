package dev.aptitudes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.aptitudes.logic.ThreatRules;

/** Instantánea al aparecer: recargar o mover una criatura no repite la tirada ni acumula atributos. */
public record ThreatData(int ring, double health, double damage, double loot, boolean claimed) {
	public static final Codec<ThreatData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.optionalFieldOf("ring", 0).forGetter(ThreatData::ring),
			Codec.DOUBLE.optionalFieldOf("health", 1.0).forGetter(ThreatData::health),
			Codec.DOUBLE.optionalFieldOf("damage", 1.0).forGetter(ThreatData::damage),
			Codec.DOUBLE.optionalFieldOf("loot", 0.0).forGetter(ThreatData::loot),
			Codec.BOOL.optionalFieldOf("claimed", false).forGetter(ThreatData::claimed)
	).apply(i, ThreatData::new));

	public ThreatData {
		ring = ThreatRules.boundedRing(ring);
		health = ring == 0 ? 1 : ThreatRules.bounded(health, 1, 3);
		damage = ring == 0 ? 1 : ThreatRules.bounded(damage, 1, 2);
		loot = ring == 0 ? 0 : ThreatRules.bounded(loot, 0, 1);
	}

	public ThreatData claim() { return new ThreatData(ring, health, damage, loot, true); }
}
