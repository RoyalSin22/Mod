package dev.aptitudes.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Ventana personal persistente: cambiar de soporte, morir o reconectar no reinicia el rendimiento. */
public record AlchemyXpData(long windowStart, int brews) {
	public static final Codec<AlchemyXpData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.LONG.optionalFieldOf("window_start", 0L).forGetter(AlchemyXpData::windowStart),
			Codec.INT.optionalFieldOf("brews", 0).forGetter(AlchemyXpData::brews)
	).apply(i, AlchemyXpData::new));

	public AlchemyXpData {
		windowStart = Math.max(0, windowStart);
		brews = Math.max(0, Math.min(1_000_000, brews));
	}

	public AlchemyXpData record(long now, int windowSeconds) {
		long time = Math.max(0, now);
		long window = (long) Math.max(10, Math.min(3600, windowSeconds)) * 20;
		if (brews == 0 || time < windowStart || time - windowStart >= window) return new AlchemyXpData(time, 1);
		return new AlchemyXpData(windowStart, brews + 1);
	}
}
