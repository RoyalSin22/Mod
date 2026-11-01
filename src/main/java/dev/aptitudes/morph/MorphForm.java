package dev.aptitudes.morph;

import dev.aptitudes.Aptitudes;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Formas del Cambiaformas. Se desbloquean derrotando a ese mob {@link #killsToUnlock}
 * veces, o por nivel de rol (Aldeano). El Warden solo se obtiene con la definitiva.
 *
 * @param hostile si es {@code true}, con el pasivo Bromista los monstruos te ignoran
 */
public enum MorphForm {
	CREEPER(EntityType.CREEPER, Items.CREEPER_SPAWN_EGG, 10, 0, true),
	ENDERMAN(EntityType.ENDERMAN, Items.ENDERMAN_SPAWN_EGG, 10, 0, true),
	SPIDER(EntityType.SPIDER, Items.SPIDER_SPAWN_EGG, 10, 0, true),
	BLAZE(EntityType.BLAZE, Items.BLAZE_SPAWN_EGG, 10, 0, true),
	BAT(EntityType.BAT, Items.BAT_SPAWN_EGG, 5, 0, false),
	SLIME(EntityType.SLIME, Items.SLIME_SPAWN_EGG, 10, 0, true),
	CHICKEN(EntityType.CHICKEN, Items.CHICKEN_SPAWN_EGG, 10, 0, false),
	IRON_GOLEM(EntityType.IRON_GOLEM, Items.IRON_GOLEM_SPAWN_EGG, 3, 0, false),
	WITCH(EntityType.WITCH, Items.WITCH_SPAWN_EGG, 10, 0, true),
	VILLAGER(EntityType.VILLAGER, Items.VILLAGER_SPAWN_EGG, 0, 5, false),
	/** Solo con la habilidad definitiva del Cambiaformas. */
	WARDEN(EntityType.WARDEN, Items.WARDEN_SPAWN_EGG, -1, 0, true);

	private final EntityType<?> type;
	private final Item icon;
	private final int killsToUnlock;
	private final int levelToUnlock;
	private final boolean hostile;

	MorphForm(EntityType<?> type, Item icon, int killsToUnlock, int levelToUnlock, boolean hostile) {
		this.type = type;
		this.icon = icon;
		this.killsToUnlock = killsToUnlock;
		this.levelToUnlock = levelToUnlock;
		this.hostile = hostile;
	}

	public EntityType<?> type() { return type; }
	public ItemStack iconStack() { return new ItemStack(icon); }
	public int killsToUnlock() { return killsToUnlock; }
	public int levelToUnlock() { return levelToUnlock; }
	public boolean hostile() { return hostile; }
	/** Las formas que se pueden elegir en la pantalla (todas salvo las exclusivas de la definitiva). */
	public boolean selectable() { return killsToUnlock >= 0; }

	public String key() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String translationKey() {
		return "form." + Aptitudes.MOD_ID + "." + key();
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	public Identifier typeId() {
		return Registries.ENTITY_TYPE.getId(type);
	}

	@Nullable
	public static MorphForm byType(EntityType<?> type) {
		for (MorphForm f : values()) if (f.type == type) return f;
		return null;
	}

	@Nullable
	public static MorphForm byIndex(int index) {
		MorphForm[] values = values();
		return index >= 0 && index < values.length ? values[index] : null;
	}
}
