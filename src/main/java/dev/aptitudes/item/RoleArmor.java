package dev.aptitudes.item;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Role;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Un set de armadura de 4 piezas por rol. Cada rol tiene un "peso" (ligero, medio,
 * pesado) que fija su defensa, y un material base que se usa para fabricarla y
 * repararla. La bonificación de set completo está en {@code RoleArmorSets}.
 *
 * <p>Las texturas de capa están en {@code textures/models/armor/<rol>_layer_1/2.png}.</p>
 */
public final class RoleArmor {
	/** Defensa por pieza en orden botas, grebas, peto, casco (como los materiales vanilla). */
	public enum Weight {
		LIGHT(new int[]{2, 4, 5, 2}, 0.0f, 0.0f, 18, 18),
		MEDIUM(new int[]{2, 5, 6, 2}, 1.0f, 0.0f, 22, 14),
		HEAVY(new int[]{3, 6, 8, 3}, 2.0f, 0.05f, 28, 10);

		final int[] defense;
		final float toughness;
		final float knockbackResistance;
		final int durability;
		final int enchantability;

		Weight(int[] defense, float toughness, float knockbackResistance, int durability, int enchantability) {
			this.defense = defense;
			this.toughness = toughness;
			this.knockbackResistance = knockbackResistance;
			this.durability = durability;
			this.enchantability = enchantability;
		}
	}

	/** Orden de las piezas en las listas: casco, peto, grebas, botas. */
	public static final ArmorItem.Type[] TYPES = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE,
			ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};

	private static final Map<Role, RegistryEntry<ArmorMaterial>> MATERIALS = new EnumMap<>(Role.class);
	private static final Map<Role, List<Item>> PIECES = new EnumMap<>(Role.class);
	private static final List<Item> ALL = new ArrayList<>();

	private RoleArmor() {
	}

	public static Weight weight(Role role) {
		return switch (role) {
			case BERSERKER, LANCER, PALADIN -> Weight.HEAVY;
			case SWORDSMAN, ENGINEER, DRUID, NECROMANCER -> Weight.MEDIUM;
			default -> Weight.LIGHT;
		};
	}

	/** Material base para fabricar y reparar el set (único por rol, así las recetas no chocan). */
	public static Item baseMaterial(Role role) {
		return switch (role) {
			case BERSERKER -> Items.IRON_INGOT;
			case MAGE -> Items.AMETHYST_SHARD;
			case ASSASSIN -> Items.PHANTOM_MEMBRANE;
			case HERBALIST -> Items.HONEYCOMB;
			case ENGINEER -> Items.COPPER_INGOT;
			case LANCER -> Items.PRISMARINE_SHARD;
			case SWORDSMAN -> Items.QUARTZ;
			case PALADIN -> Items.GOLD_INGOT;
			case NECROMANCER -> Items.BONE;
			case DRUID -> Items.MOSS_BLOCK;
			case MERCHANT -> Items.EMERALD;
			case MONK -> Items.WHITE_WOOL;
			case ELEMENTALIST -> Items.BREEZE_ROD;
			case SHAPESHIFTER -> Items.RABBIT_HIDE;
		};
	}

	private static RegistryEntry<SoundEvent> equipSound(Weight weight) {
		return switch (weight) {
			case HEAVY -> SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
			case MEDIUM -> SoundEvents.ITEM_ARMOR_EQUIP_IRON;
			case LIGHT -> SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
		};
	}

	private static String pieceName(ArmorItem.Type type) {
		return switch (type) {
			case HELMET -> "helmet";
			case CHESTPLATE -> "chestplate";
			case LEGGINGS -> "leggings";
			case BOOTS -> "boots";
			default -> "body";
		};
	}

	public static String itemPath(Role role, ArmorItem.Type type) {
		return role.key() + "_" + pieceName(type);
	}

	/** Registra los 14 materiales y las 56 piezas. Llamar una vez durante la inicialización. */
	public static void register() {
		if (!ALL.isEmpty()) return;
		for (Role role : Role.values()) {
			Weight w = weight(role);
			Item base = baseMaterial(role);
			ArmorMaterial material = new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
						map.put(ArmorItem.Type.BOOTS, w.defense[0]);
						map.put(ArmorItem.Type.LEGGINGS, w.defense[1]);
						map.put(ArmorItem.Type.CHESTPLATE, w.defense[2]);
						map.put(ArmorItem.Type.HELMET, w.defense[3]);
						map.put(ArmorItem.Type.BODY, w.defense[2]);
					}),
					w.enchantability, equipSound(w), () -> Ingredient.ofItems(base),
					List.of(new ArmorMaterial.Layer(Aptitudes.id(role.key()))),
					w.toughness, w.knockbackResistance);
			RegistryEntry<ArmorMaterial> entry = Registry.registerReference(Registries.ARMOR_MATERIAL,
					Aptitudes.id(role.key()), material);
			MATERIALS.put(role, entry);

			List<Item> pieces = new ArrayList<>(4);
			for (ArmorItem.Type type : TYPES) {
				Item item = Registry.register(Registries.ITEM, Aptitudes.id(itemPath(role, type)),
						new RoleArmorItem(role, entry, type, new Item.Settings()
								.maxDamage(type.getMaxDamage(w.durability))
								.rarity(Rarity.UNCOMMON)));
				pieces.add(item);
				ALL.add(item);
			}
			PIECES.put(role, Collections.unmodifiableList(pieces));
		}
	}

	public static List<Item> pieces(Role role) {
		return PIECES.getOrDefault(role, List.of());
	}

	public static List<Item> all() {
		return Collections.unmodifiableList(ALL);
	}

	@Nullable
	public static RegistryEntry<ArmorMaterial> material(Role role) {
		return MATERIALS.get(role);
	}
}
