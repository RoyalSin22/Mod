package dev.aptitudes.data;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * Habilidades que suben por uso.
 *
 * <p>Algunas tienen una bonificación pasiva implementada como modificador de
 * atributo vanilla (combate, defensa, agilidad). El resto se aplica por lógica
 * propia (velocidad de rotura vía mixin, cosecha extra, daño de flechas).</p>
 */
public enum Skill {
	MINING(Items.IRON_PICKAXE,
			bonus("break_speed", null, null, c -> c.breakSpeedPerLevel, true),
			bonus("armor", EntityAttributes.GENERIC_ARMOR, Operation.ADD_VALUE, c -> c.miningArmorPerLevel, false)),
	EXCAVATION(Items.IRON_SHOVEL,
			bonus("break_speed", null, null, c -> c.breakSpeedPerLevel, true),
			bonus("knockback_resistance", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Operation.ADD_VALUE,
					c -> c.excavationKnockbackPerLevel, true)),
	WOODCUTTING(Items.IRON_AXE,
			bonus("break_speed", null, null, c -> c.breakSpeedPerLevel, true),
			bonus("max_health", EntityAttributes.GENERIC_MAX_HEALTH, Operation.ADD_VALUE, c -> c.woodcuttingHealthPerLevel, false)),
	FARMING(Items.IRON_HOE,
			bonus("double_harvest", null, null, c -> c.farmingExtraDropChancePerLevel, true),
			bonus("luck", EntityAttributes.GENERIC_LUCK, Operation.ADD_VALUE, c -> c.farmingLuckPerLevel, false)),
	COMBAT(Items.IRON_SWORD,
			bonus("melee_damage", EntityAttributes.GENERIC_ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL,
					c -> c.combatDamagePerLevel, true)),
	ARCHERY(Items.BOW,
			bonus("arrow_damage", null, null, c -> c.archeryDamagePerLevel, true)),
	DEFENSE(Items.SHIELD,
			bonus("armor", EntityAttributes.GENERIC_ARMOR, Operation.ADD_VALUE, c -> c.defenseArmorPerLevel, false),
			bonus("toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS, Operation.ADD_VALUE, c -> c.defenseToughnessPerLevel, false),
			bonus("explosion_resistance", EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, Operation.ADD_VALUE,
					c -> c.defenseExplosionPerLevel, true)),
	AGILITY(Items.FEATHER,
			bonus("movement_speed", EntityAttributes.GENERIC_MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE,
					c -> c.agilitySpeedPerLevel, true),
			bonus("safe_fall", EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, Operation.ADD_VALUE,
					c -> c.agilitySafeFallPerLevel, false)),
	// Al final del enum: los datos se guardan por nombre y la sincronización por orden, así que
	// añadirla aquí no altera a las demás habilidades.
	FISHING(Items.FISHING_ROD,
			bonus("luck", EntityAttributes.GENERIC_LUCK, Operation.ADD_VALUE, c -> c.fishingLuckPerLevel, false),
			bonus("double_catch", null, null, c -> c.fishingDoubleCatchPerLevel, true));

	/**
	 * Bonificación pasiva de una habilidad: {@code nivel × perLevel}. Si {@code attribute}
	 * es null, el efecto lo aplica lógica propia (rotura, cosecha doble, flechas).
	 *
	 * @param stat    clave de texto ({@code skillbonus.aptitudes.<stat>})
	 * @param percent si se muestra como porcentaje (valor × 100)
	 */
	public record SkillBonus(String stat, @Nullable RegistryEntry<EntityAttribute> attribute,
							 @Nullable Operation operation, ToDoubleFunction<AptitudesConfig> perLevel, boolean percent) {
		public String translationKey() {
			return "skillbonus." + Aptitudes.MOD_ID + "." + stat;
		}
	}

	private static SkillBonus bonus(String stat, @Nullable RegistryEntry<EntityAttribute> attribute,
									@Nullable Operation operation, ToDoubleFunction<AptitudesConfig> perLevel, boolean percent) {
		return new SkillBonus(stat, attribute, operation, perLevel, percent);
	}

	private final Item icon;
	private final List<SkillBonus> bonuses;
	private final String key;

	Skill(Item icon, SkillBonus... bonuses) {
		this.icon = icon;
		this.bonuses = List.of(bonuses);
		this.key = name().toLowerCase(Locale.ROOT);
	}

	public List<SkillBonus> bonuses() {
		return bonuses;
	}

	public String key() {
		return key;
	}

	public String translationKey() {
		return "skill." + Aptitudes.MOD_ID + "." + key;
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	public ItemStack iconStack() {
		return new ItemStack(icon);
	}

	public Identifier modifierId(int bonusIndex) {
		return Aptitudes.id("skill/" + key + "/" + bonusIndex);
	}

	public static Optional<Skill> byKey(String key) {
		return Arrays.stream(values()).filter(s -> s.key.equalsIgnoreCase(key)).findFirst();
	}
}
