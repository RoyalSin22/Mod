package dev.aptitudes.data;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * Atributos estilo Souls en los que el jugador reparte los niveles comprados en
 * una Hoguera. Cada uno se traduce a un
 * modificador sobre un atributo vanilla, así el resto del juego (y otros mods)
 * los respetan sin código adicional.
 */
public enum PlayerAttribute {
	/** Vigor: vida máxima. */
	VIGOR(EntityAttributes.GENERIC_MAX_HEALTH, EntityAttributeModifier.Operation.ADD_VALUE, c -> c.vigorHealthPerPoint),
	/** Mente: maná máximo y regeneración (efecto propio, sin atributo vanilla). */
	MIND(null, null, c -> c.mindManaPerPoint),
	/** Aguante: armadura. */
	ENDURANCE(EntityAttributes.GENERIC_ARMOR, EntityAttributeModifier.Operation.ADD_VALUE, c -> c.enduranceArmorPerPoint),
	/** Fuerza: daño cuerpo a cuerpo. */
	STRENGTH(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.ADD_VALUE, c -> c.strengthDamagePerPoint),
	/** Destreza: velocidad de ataque. */
	DEXTERITY(EntityAttributes.GENERIC_ATTACK_SPEED, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, c -> c.dexterityAttackSpeedPerPoint),
	/** Inteligencia: daño de hechizos y habilidades a distancia (% por punto). */
	INTELLIGENCE(null, null, c -> c.intelligenceSpellDamagePerPoint),
	/** Fe: reducción de enfriamientos (% por punto). */
	FAITH(null, null, c -> c.faithCooldownPerPoint),
	/** Suerte: botín y pesca. */
	LUCK(EntityAttributes.GENERIC_LUCK, EntityAttributeModifier.Operation.ADD_VALUE, c -> c.luckPerPoint);

	@Nullable private final RegistryEntry<EntityAttribute> attribute;
	@Nullable private final EntityAttributeModifier.Operation operation;
	private final ToDoubleFunction<AptitudesConfig> perPoint;
	private final String key;

	PlayerAttribute(@Nullable RegistryEntry<EntityAttribute> attribute,
					@Nullable EntityAttributeModifier.Operation operation,
					ToDoubleFunction<AptitudesConfig> perPoint) {
		this.attribute = attribute;
		this.operation = operation;
		this.perPoint = perPoint;
		this.key = name().toLowerCase(Locale.ROOT);
	}

	public String key() {
		return key;
	}

	public String translationKey() {
		return "attribute." + Aptitudes.MOD_ID + "." + key;
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	/** Atributo vanilla que modifica, o {@code null} si su efecto es propio (Mente, Inteligencia, Fe). */
	@Nullable
	public RegistryEntry<EntityAttribute> attribute() {
		return attribute;
	}

	@Nullable
	public EntityAttributeModifier.Operation operation() {
		return operation;
	}

	public double perPoint(AptitudesConfig config) {
		return perPoint.applyAsDouble(config);
	}

	public Identifier modifierId() {
		return Aptitudes.id("attribute/" + key);
	}

	public static Optional<PlayerAttribute> byKey(String key) {
		return Arrays.stream(values()).filter(a -> a.key.equalsIgnoreCase(key)).findFirst();
	}
}
