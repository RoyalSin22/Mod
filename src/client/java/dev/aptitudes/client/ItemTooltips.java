package dev.aptitudes.client;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.HitscanWeaponItem;
import dev.aptitudes.item.RoleGlyphs;
import dev.aptitudes.item.weapon.RoleBladeItem;
import dev.aptitudes.item.weapon.RoleFocusItem;
import dev.aptitudes.logic.ItemStatRules;
import dev.aptitudes.role.Role;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * v62 (A6): tooltips de los objetos de Aptitudes. Añade el sello y el nombre del rol a sus objetos característicos,
 * el daño por segundo de las armas cuerpo a cuerpo de rol y una línea de rareza (el nombre ya va coloreado por
 * rareza en vanilla). Solo presenta datos del objeto: no cambia ninguna estadística.
 */
public final class ItemTooltips {
	private static Map<Item, Role> roles;

	private ItemTooltips() {
	}

	private static Role roleOf(Item item) {
		if (item instanceof RoleBladeItem blade) return blade.role();
		if (item instanceof RoleFocusItem focus) return focus.role();
		if (item instanceof HitscanWeaponItem hitscan) return hitscan.role();
		if (roles == null) {
			Map<Item, Role> map = new HashMap<>();
			for (Role role : Role.values()) map.put(role.iconStack().getItem(), role);
			roles = map;
		}
		return roles.get(item);
	}

	public static void append(ItemStack stack, List<Text> lines) {
		Item item = stack.getItem();
		if (lines.isEmpty() || !Registries.ITEM.getId(item).getNamespace().equals(Aptitudes.MOD_ID)) return;
		Role role = roleOf(item);
		// Las hojas y focos de evolución ya escriben su línea de rol (con el sello) en appendTooltip.
		if (role != null && !(item instanceof RoleBladeItem) && !(item instanceof RoleFocusItem)) {
			lines.add(1, RoleGlyphs.line(role, Text.translatable("item.aptitudes.role_item", Text.translatable(role.translationKey()))));
		}
		if (role != null) {
			double damage = 0;
			double speed = 0;
			boolean melee = false;
			AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
			for (AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
				if (entry.slot() != AttributeModifierSlot.MAINHAND || entry.modifier().operation() != EntityAttributeModifier.Operation.ADD_VALUE) continue;
				if (entry.attribute().value() == EntityAttributes.GENERIC_ATTACK_DAMAGE.value()) {
					damage += entry.modifier().value();
					melee = true;
				} else if (entry.attribute().value() == EntityAttributes.GENERIC_ATTACK_SPEED.value()) {
					speed += entry.modifier().value();
				}
			}
			if (melee) {
				lines.add(Text.translatable("item.aptitudes.stats.dps", ItemStatRules.fmt(ItemStatRules.dps(damage, speed)))
						.formatted(Formatting.DARK_GREEN));
			}
		}
		Rarity rarity = stack.getRarity();
		if (rarity != Rarity.COMMON) {
			lines.add(Text.translatable("item.aptitudes.rarity." + rarity.name().toLowerCase(Locale.ROOT)).formatted(rarity.getFormatting()));
		}
	}
}
