package dev.aptitudes.item;

import dev.aptitudes.Aptitudes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 * La Armadura Ascendida (V80): un único set de 4 piezas, mejora de la netherita que se hace en la mesa de herrería con una
 * Plantilla de ascensión y un Corazón de campeón por pieza. Sustituye a los 20 sets de rol: con las cuatro piezas puestas
 * adopta el aspecto de tu rol principal (ver {@code RoleArmorSets}).
 *
 * <p>Texturas de capa en {@code textures/models/armor/ascended_layer_1/2.png}.</p>
 */
public final class AscendedArmor {
	/** Orden de las piezas en la lista: casco, peto, grebas, botas. */
	public static final ArmorItem.Type[] TYPES = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE,
			ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private static final List<Item> PIECES = new ArrayList<>();
	private static RegistryEntry<ArmorMaterial> material;

	private AscendedArmor() {
	}

	public static String itemPath(ArmorItem.Type type) {
		return "ascended_" + switch (type) {
			case HELMET -> "helmet";
			case CHESTPLATE -> "chestplate";
			case LEGGINGS -> "leggings";
			case BOOTS -> "boots";
			default -> "body";
		};
	}

	/** Registra el material y las 4 piezas. Llamar una vez durante la inicialización. */
	public static void register() {
		if (!PIECES.isEmpty()) return;
		int[] d = AscendedRules.DEFENSE;
		ArmorMaterial armorMaterial = new ArmorMaterial(
				Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
					map.put(ArmorItem.Type.BOOTS, d[0]);
					map.put(ArmorItem.Type.LEGGINGS, d[1]);
					map.put(ArmorItem.Type.CHESTPLATE, d[2]);
					map.put(ArmorItem.Type.HELMET, d[3]);
					map.put(ArmorItem.Type.BODY, d[2]);
				}),
				AscendedRules.ENCHANTABILITY, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
				() -> Ingredient.ofItems(Items.NETHERITE_INGOT),
				List.of(new ArmorMaterial.Layer(Aptitudes.id("ascended"))),
				AscendedRules.TOUGHNESS, AscendedRules.KNOCKBACK_RESISTANCE);
		material = Registry.registerReference(Registries.ARMOR_MATERIAL, Aptitudes.id("ascended"), armorMaterial);
		for (ArmorItem.Type type : TYPES) {
			PIECES.add(Registry.register(Registries.ITEM, Aptitudes.id(itemPath(type)),
					new AscendedArmorItem(material, type, new Item.Settings()
							.maxDamage(type.getMaxDamage(AscendedRules.DURABILITY))
							.rarity(Rarity.EPIC).fireproof())));
		}
	}

	public static List<Item> pieces() {
		return Collections.unmodifiableList(PIECES);
	}

	/** Las cuatro piezas puestas. */
	public static boolean wearingFullSet(LivingEntity entity) {
		for (EquipmentSlot slot : SLOTS) {
			if (!(entity.getEquippedStack(slot).getItem() instanceof AscendedArmorItem)) return false;
		}
		return true;
	}
}
