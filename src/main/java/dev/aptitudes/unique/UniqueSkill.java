package dev.aptitudes.unique;

import dev.aptitudes.Aptitudes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Habilidades únicas: cada una existe UNA sola vez por servidor y cada jugador
 * puede tener como máximo una. Se obtienen siendo el primero en cumplir su gesta
 * ({@link #target}); el progreso se calcula en {@link UniqueManager#progress}.
 *
 * <p>Los nombres y efectos son originales; algunos arquetipos se inspiran en la
 * lista de referencia del usuario. Pendragón usa la leyenda artúrica, de dominio público.</p>
 */
public enum UniqueSkill {
	ADAPTATION(Items.TOTEM_OF_UNDYING, 0xB0BEC5, 40, 60, 12),
	GREED(Items.EMERALD_BLOCK, 0x2ECC71, 40, 90, 32),
	VOID_ARCHIVE(Items.ENCHANTED_BOOK, 0x7E57C2, 30, 20, 6),
	FUNGAL(Items.RED_MUSHROOM, 0x8D6E63, 40, 45, 50),
	PARASITE(Items.FERMENTED_SPIDER_EYE, 0x880E4F, 35, 30, 20),
	HELLFIRE(Items.BLAZE_POWDER, 0xFF5722, 50, 40, 600),
	CHOSEN_KING(Items.GOLDEN_HELMET, 0xFFD700, 50, 60, 2),
	SPACETIME(Items.CLOCK, 0x00BCD4, 40, 30, 200),
	NEGATION(Items.MILK_BUCKET, 0xECEFF1, 60, 90, 50),
	GENETICIST(Items.WHEAT, 0xAED581, 30, 30, 200),
	MIRROR(Items.GLASS_PANE, 0xE1BEE7, 40, 60, 10),
	EXPLOITER(Items.DIAMOND_PICKAXE, 0x4DD0E1, 40, 120, 40),
	PENDRAGON(Items.GOLDEN_SWORD, 0xFFF59D, 60, 45, 2),
	SKY_LORD(Items.ELYTRA, 0x90CAF9, 50, 120, 100),
	STILLNESS(Items.ICE, 0x80DEEA, 60, 90, 30),
	HARBINGER(Items.WITHER_SKELETON_SKULL, 0x37474F, 50, 40, 50);

	private final Item icon;
	private final int color;
	private final int energyCost;
	private final int cooldownSecs;
	private final int target;

	UniqueSkill(Item icon, int color, int energyCost, int cooldownSecs, int target) {
		this.icon = icon;
		this.color = color;
		this.energyCost = energyCost;
		this.cooldownSecs = cooldownSecs;
		this.target = target;
	}

	public ItemStack iconStack() { return new ItemStack(icon); }
	public int color() { return color; }
	public int energyCost() { return energyCost; }
	public int cooldownSecs() { return cooldownSecs; }
	/** Valor que debe alcanzar el progreso para reclamarla. */
	public int target() { return target; }

	public String key() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String translationKey() {
		return "unique." + Aptitudes.MOD_ID + "." + key();
	}

	/** Descripción de la pasiva y la activa. */
	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	/** Texto de la gesta para obtenerla (con %s/%s de progreso). */
	public String conditionKey() {
		return translationKey() + ".condition";
	}

	public static Optional<UniqueSkill> byKey(String key) {
		return Arrays.stream(values()).filter(s -> s.key().equalsIgnoreCase(key)).findFirst();
	}

	public static UniqueSkill byIndex(int index) {
		UniqueSkill[] values = values();
		return index >= 0 && index < values.length ? values[index] : null;
	}
}
