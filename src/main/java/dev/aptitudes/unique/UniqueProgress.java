package dev.aptitudes.unique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contadores persistentes por jugador para las gestas que no se pueden leer de
 * las estadísticas vanilla.
 */
public final class UniqueProgress {
	public static final Codec<UniqueProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().optionalFieldOf("damage_types", List.of()).forGetter(p -> List.copyOf(p.damageTypes)),
			Codec.INT.optionalFieldOf("mycelium_kills", 0).forGetter(p -> p.myceliumKills),
			Codec.INT.optionalFieldOf("low_hp_kills", 0).forGetter(p -> p.lowHpKills),
			Codec.INT.optionalFieldOf("lava_seconds", 0).forGetter(p -> p.lavaSeconds),
			Codec.BOOL.optionalFieldOf("wither", false).forGetter(p -> p.wither),
			Codec.BOOL.optionalFieldOf("dragon", false).forGetter(p -> p.dragon)
	).apply(i, UniqueProgress::new));

	private final Set<String> damageTypes;
	public int myceliumKills;
	public int lowHpKills;
	public int lavaSeconds;
	public boolean wither;
	public boolean dragon;

	public UniqueProgress() {
		this(List.of(), 0, 0, 0, false, false);
	}

	private UniqueProgress(List<String> damageTypes, int myceliumKills, int lowHpKills, int lavaSeconds,
						   boolean wither, boolean dragon) {
		this.damageTypes = new HashSet<>(damageTypes);
		this.myceliumKills = myceliumKills;
		this.lowHpKills = lowHpKills;
		this.lavaSeconds = lavaSeconds;
		this.wither = wither;
		this.dragon = dragon;
	}

	public void addDamageType(String type) {
		if (damageTypes.size() < 256) damageTypes.add(type);
	}

	public int damageTypeCount() {
		return damageTypes.size();
	}
}
