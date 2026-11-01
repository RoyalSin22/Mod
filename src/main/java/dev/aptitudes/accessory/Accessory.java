package dev.aptitudes.accessory;

import dev.aptitudes.Aptitudes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Accesorios: 2 ranuras de anillo y 1 de amuleto. Los que dan un atributo usan
 * {@link #attribute}; el resto tienen lógica propia en {@link Accessories}.
 */
public enum Accessory {
	// Anillos
	RING_OF_STRENGTH(true, 0xE53935, EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.5, Operation.ADD_VALUE),
	RING_OF_VITALITY(true, 0xEF5350, EntityAttributes.GENERIC_MAX_HEALTH, 4.0, Operation.ADD_VALUE),
	RING_OF_SWIFTNESS(true, 0x81D4FA, EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.08, Operation.ADD_MULTIPLIED_BASE),
	RING_OF_MANA(true, 0x42A5F5, null, 0, null),          // +30 de maná máximo
	RING_OF_THE_VAMPIRE(true, 0x8E0000, null, 0, null),   // robo de vida 5 %
	RING_OF_FORTUNE(true, 0x66BB6A, EntityAttributes.GENERIC_LUCK, 2.0, Operation.ADD_VALUE),
	// Amuletos
	AMULET_OF_THE_GUARDIAN(false, 0xB0BEC5, EntityAttributes.GENERIC_ARMOR, 3.0, Operation.ADD_VALUE),
	AMULET_OF_THE_PHOENIX(false, 0xFF7043, null, 0, null), // te salva de la muerte (cada 10 min)
	ARCANE_AMULET(false, 0xB388FF, null, 0, null),         // −10 % de enfriamientos
	AMULET_OF_THE_GROVE(false, 0x8BC34A, null, 0, null),   // regeneración fuera de combate
	HUNTERS_TROPHY(false, 0xFFD54F, null, 0, null);        // +25 % de daño a élites y campeones

	private final boolean ring;
	private final int color;
	@Nullable private final RegistryEntry<EntityAttribute> attribute;
	private final double value;
	@Nullable private final Operation operation;

	Accessory(boolean ring, int color, @Nullable RegistryEntry<EntityAttribute> attribute, double value,
			  @Nullable Operation operation) {
		this.ring = ring;
		this.color = color;
		this.attribute = attribute;
		this.value = value;
		this.operation = operation;
	}

	public boolean ring() { return ring; }
	public int color() { return color; }
	@Nullable public RegistryEntry<EntityAttribute> attribute() { return attribute; }
	public double value() { return value; }
	@Nullable public Operation operation() { return operation; }

	public String key() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String descriptionKey() {
		return "item." + Aptitudes.MOD_ID + "." + key() + ".desc";
	}
}
