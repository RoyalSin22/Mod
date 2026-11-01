package dev.aptitudes.souls;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Mancha de XP pendiente de recuperar (persistente). {@code xp == 0} = no hay mancha. */
public final class SoulsData {
	public static final Codec<SoulsData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("world", "").forGetter(d -> d.world),
			Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(d -> d.x),
			Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(d -> d.y),
			Codec.DOUBLE.optionalFieldOf("z", 0.0).forGetter(d -> d.z),
			Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp)
	).apply(i, SoulsData::new));

	public String world;
	public double x;
	public double y;
	public double z;
	public int xp;

	public SoulsData() {
		this("", 0, 0, 0, 0);
	}

	private SoulsData(String world, double x, double y, double z, int xp) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.xp = xp;
	}

	public boolean hasStain() {
		return xp > 0 && !world.isEmpty();
	}

	public void clear() {
		xp = 0;
		world = "";
	}
}
