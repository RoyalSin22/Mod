package dev.aptitudes.item.weapon;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Armas de evolución: cada rol desbloquea un arma nueva al evolucionar a Maestro y otra
 * al llegar a Definitivo. Los roles de combate cuerpo a cuerpo reciben una hoja
 * ({@link RoleBladeItem}); los de magia o distancia, un foco ({@link RoleFocusItem}).
 * Aparecen en la fila superior del árbol y se pueden volver a reclamar si se pierden.
 */
public final class RoleWeapons {
	/** Tipo de arma de cada rol. */
	public static boolean melee(Role role) {
		return switch (role) {
			case MAGE, ENGINEER, NECROMANCER, DRUID, ELEMENTALIST, GUNSLINGER, BARD, WARLOCK -> false;
			default -> true;
		};
	}

	/** Velocidad de ataque de las hojas según el estilo del rol. */
	private static float speed(Role role) {
		return switch (role) {
			case ASSASSIN, MONK, HUNTER, SHAPESHIFTER -> -1.8f;
			case BERSERKER, PALADIN, LANCER -> -3.0f;
			default -> -2.4f;
		};
	}

	private static final Map<Role, Item[]> WEAPONS = new EnumMap<>(Role.class);
	private static final List<Item> ALL = new ArrayList<>();

	private RoleWeapons() {
	}

	public static void register() {
		if (!ALL.isEmpty()) return;
		for (Role role : Role.values()) {
			Item[] pair = new Item[2];
			for (int i = 0; i < 2; i++) {
				int tier = i + 1; // 1 = Maestro, 2 = Definitivo
				String path = role.key() + (tier == Evolution.MASTER ? "_weapon_master" : "_weapon_ultimate");
				Item.Settings settings = new Item.Settings().rarity(tier == Evolution.MASTER ? Rarity.RARE : Rarity.EPIC);
				if (tier == Evolution.ULTIMATE) settings.fireproof();
				Item item;
				if (melee(role)) {
					ToolMaterial material = tier == Evolution.MASTER ? ToolMaterials.DIAMOND : ToolMaterials.NETHERITE;
					item = new RoleBladeItem(role, tier, material, settings.attributeModifiers(
							SwordItem.createAttributeModifiers(material, tier == Evolution.MASTER ? 4 : 5, speed(role))));
				} else {
					item = new RoleFocusItem(role, tier, settings.maxCount(1));
				}
				pair[i] = Registry.register(Registries.ITEM, Aptitudes.id(path), item);
				ALL.add(pair[i]);
			}
			WEAPONS.put(role, pair);
		}
	}

	/** Arma del rol para el nivel de evolución (1 = Maestro, 2 = Definitivo). */
	@Nullable
	public static Item get(Role role, int tier) {
		Item[] pair = WEAPONS.get(role);
		return pair == null || tier < 1 || tier > 2 ? null : pair[tier - 1];
	}

	public static List<Item> all() {
		return List.copyOf(ALL);
	}

	/** Entrega el arma al evolucionar (o al reclamarla de nuevo desde el árbol). */
	public static void give(ServerPlayerEntity player, Role role, int tier) {
		Item weapon = get(role, tier);
		if (weapon == null) return;
		ItemStack stack = new ItemStack(weapon);
		if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
		player.sendMessage(Text.translatable("aptitudes.weapon.unlocked", new ItemStack(weapon).getName())
				.formatted(Formatting.GOLD), false);
	}

	/** ¿Tiene ya el arma (inventario o cofre de Ender)? Evita duplicarla al reclamarla. */
	public static boolean owns(ServerPlayerEntity player, Item weapon) {
		return player.getInventory().count(weapon) > 0 || player.getEnderChestInventory().count(weapon) > 0;
	}
}
