package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Elementos del Elementalista. Puede dominar hasta 3 (1 al empezar, otro al
 * nivel 10 y otro al 20); el comportamiento de cada uno está en
 * {@code ElementEffects}.
 */
public enum Element {
	FIRE(0xFF6A00, ParticleTypes.FLAME),
	ICE(0x9BE7FF, ParticleTypes.SNOWFLAKE),
	LIGHTNING(0xFFF176, ParticleTypes.ELECTRIC_SPARK),
	EARTH(0x8D6E63, ParticleTypes.MYCELIUM),
	WIND(0xE0F7FA, ParticleTypes.CLOUD),
	WATER(0x2196F3, ParticleTypes.SPLASH),
	PLANT(0x66BB6A, ParticleTypes.HAPPY_VILLAGER),
	MAGMA(0xBF360C, ParticleTypes.LAVA),
	SHADOW(0x4A148C, ParticleTypes.SQUID_INK),
	// v14: elementos nuevos
	LIGHT(0xFFF59D, ParticleTypes.END_ROD),
	VOID(0x5E35B1, ParticleTypes.REVERSE_PORTAL),
	POISON(0x76FF03, ParticleTypes.ITEM_SLIME),
	CRYSTAL(0xE1BEE7, ParticleTypes.GLOW),
	SOUND(0x80DEEA, ParticleTypes.NOTE);

	/** Nivel de Elementalista necesario para dominar el 1.º, 2.º y 3.º elemento. */
	public static final int[] UNLOCK_LEVELS = {1, 10, 20};
	public static final int MAX_MASTERED = UNLOCK_LEVELS.length;

	private final int color;
	private final ParticleEffect particle;
	private final String key;

	Element(int color, ParticleEffect particle) {
		this.color = color;
		this.particle = particle;
		this.key = name().toLowerCase(Locale.ROOT);
	}

	public String key() {
		return key;
	}

	public int color() {
		return color;
	}

	public ParticleEffect particle() {
		return particle;
	}

	public String translationKey() {
		return "element." + Aptitudes.MOD_ID + "." + key;
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	/** Cuántos elementos puede dominar un Elementalista de ese nivel. */
	public static int allowedCount(int elementalistLevel) {
		int count = 0;
		for (int required : UNLOCK_LEVELS) {
			if (elementalistLevel >= required) count++;
		}
		return count;
	}

	public static Optional<Element> byKey(String key) {
		return Arrays.stream(values()).filter(e -> e.key.equalsIgnoreCase(key)).findFirst();
	}

	public static Element byIndex(int index) {
		Element[] values = values();
		return index >= 0 && index < values.length ? values[index] : null;
	}
}
