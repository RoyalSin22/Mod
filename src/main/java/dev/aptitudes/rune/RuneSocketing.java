package dev.aptitudes.rune;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.weapon.RoleBladeItem;
import dev.aptitudes.item.weapon.RoleFocusItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Escritura autoritativa de las ranuras de runa persistentes en cada ItemStack. */
public final class RuneSocketing {
	private static final String DATA_KEY = "aptitudes_runes";
	private RuneSocketing() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (blocked || damageTaken <= 0 || !(source.getAttacker() instanceof ServerPlayerEntity player)
					|| source.getSource() != player) return;
			ItemStack weapon = player.getMainHandStack();
			if (!eligible(weapon)) return;
			if (has(weapon, RuneKind.FLAME)) entity.setOnFireFor(RuneRules.flameSeconds());
			if (has(weapon, RuneKind.LEECH)) {
				long now = player.getServer().getOverworld().getTime();
				NbtCompound root = weapon.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
				long last = root.getLong("aptitudes_rune_leech_last");
				if (!root.contains("aptitudes_rune_leech_last") || now - last >= RuneRules.leechCooldownTicks()) {
					player.heal(RuneRules.leechAmount());
					root.putLong("aptitudes_rune_leech_last", now);
					weapon.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
				}
			}
		});
	}

	public static boolean eligible(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (stack.getItem() instanceof RoleBladeItem || stack.getItem() instanceof RoleFocusItem) return true;
		return stack.isOf(ModItems.BERSERKER_AXE) || stack.isOf(ModItems.DAGGER) || stack.isOf(ModItems.KATANA)
				|| stack.isOf(ModItems.SPEAR) || stack.isOf(ModItems.ARCANE_STAFF) || stack.isOf(ModItems.PLASMA_GUN)
				|| stack.isOf(ModItems.HOLY_HAMMER) || stack.isOf(ModItems.NECRO_TOME) || stack.isOf(ModItems.OAK_STAFF)
				|| stack.isOf(ModItems.HAND_WRAPS) || stack.isOf(ModItems.ARCANE_REVOLVER) || stack.isOf(ModItems.WEAPON_SIGIL)
				|| stack.isOf(ModItems.HUNTING_KNIFE) || stack.isOf(ModItems.LUTE) || stack.isOf(ModItems.CURSED_TALISMAN);
	}

	public static List<String> runes(ItemStack stack) {
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (data == null) return List.of();
		NbtCompound root = data.copyNbt();
		if (!root.contains(DATA_KEY, NbtCompound.COMPOUND_TYPE)) return List.of();
		NbtCompound stored = root.getCompound(DATA_KEY);
		List<String> ids = new ArrayList<>(RuneRules.SOCKETS);
		for (int i = 0; i < RuneRules.SOCKETS; i++) {
			String id = stored.getString("slot_" + i);
			if (RuneKind.byId(id) != null && !ids.contains(id)) ids.add(id);
		}
		return List.copyOf(ids);
	}

	public static boolean socket(ServerPlayerEntity player, ItemStack weapon, ItemStack runeStack) {
		if (!(runeStack.getItem() instanceof RuneItem rune) || !eligible(weapon)) return false;
		List<String> current = runes(weapon);
		if (!RuneRules.canSocket(current, rune.kind().id())) {
			player.sendMessage(Text.translatable(current.size() >= RuneRules.SOCKETS ? "aptitudes.rune.full" : "aptitudes.rune.duplicate").formatted(Formatting.RED), true);
			return true;
		}
		NbtCompound root = weapon.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		NbtCompound stored = root.contains(DATA_KEY, NbtCompound.COMPOUND_TYPE) ? root.getCompound(DATA_KEY) : new NbtCompound();
		stored.putString("slot_" + current.size(), rune.kind().id());
		root.put(DATA_KEY, stored);
		weapon.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		applyAttributeRune(weapon, rune.kind());
		if (!player.isCreative()) runeStack.decrement(1);
		player.sendMessage(Text.translatable("aptitudes.rune.socketed", Text.translatable("item.aptitudes.rune." + rune.kind().id()), current.size() + 1, RuneRules.SOCKETS).formatted(Formatting.GOLD), true);
		return true;
	}

	private static void applyAttributeRune(ItemStack stack, RuneKind rune) {
		AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
		if (rune == RuneKind.FORCE) {
			Identifier id = Aptitudes.id("rune_force");
			modifiers = modifiers.with(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(id, RuneRules.damageBonus(1), EntityAttributeModifier.Operation.ADD_VALUE), net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
		} else if (rune == RuneKind.SWIFT) {
			Identifier id = Aptitudes.id("rune_swift");
			modifiers = modifiers.with(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(id, RuneRules.attackSpeedBonus(1), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE), net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
		}
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers);
	}

	public static boolean has(ItemStack stack, RuneKind kind) { return runes(stack).contains(kind.id()); }
	public static float damageBonus(ItemStack stack) { return has(stack, RuneKind.FORCE) ? RuneRules.damageBonus(1) : 0.0f; }

	public static void appendTooltip(ItemStack stack, List<Text> tooltip) {
		List<String> ids = runes(stack);
		tooltip.add(Text.translatable("aptitudes.rune.sockets", ids.size(), RuneRules.SOCKETS).formatted(Formatting.GRAY));
		if (ids.isEmpty()) tooltip.add(Text.translatable("aptitudes.rune.hint").formatted(Formatting.DARK_GRAY));
		for (String id : ids) {
			tooltip.add(Text.translatable("item.aptitudes.rune." + id).formatted(Formatting.AQUA));
			tooltip.add(Text.translatable("item.aptitudes.rune." + id + ".desc").formatted(Formatting.GRAY));
		}
	}
}
