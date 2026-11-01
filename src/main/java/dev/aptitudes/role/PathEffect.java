package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Efectos con los que se construyen las Sendas. Cada nodo de senda aplica uno,
 * con {@link #perRank} por rango. Los que tienen atributo vanilla se aplican como
 * modificador; el resto los gestiona {@code PathEffects}.
 */
public enum PathEffect {
	LIFESTEAL(0.02, true, null, null),          // % del daño infligido que te cura
	CRIT(0.04, true, null, null),               // probabilidad de golpe crítico (×1,5)
	EXECUTE(0.06, true, null, null),            // +daño contra objetivos por debajo del 35 % de vida
	THORNS(0.06, true, null, null),             // % del daño cuerpo a cuerpo recibido devuelto al atacante
	DAMAGE_REDUCTION(0.03, true, null, null),   // −daño recibido (máx. 40 %)
	DODGE(0.03, true, null, null),              // probabilidad de esquivar ataques de criaturas
	MANA_REGEN(0.4, false, null, null),         // +maná por segundo
	RESOURCE_GAIN(0.12, true, null, null),      // +carga del recurso de rol
	REGEN(0.5, false, null, null),              // vida recuperada cada 3 s
	ON_HIT_POISON(0.08, true, null, null),      // probabilidad de envenenar al golpear
	ON_HIT_SLOW(0.08, true, null, null),        // probabilidad de ralentizar al golpear
	ON_HIT_FIRE(0.08, true, null, null),        // probabilidad de prender fuego al golpear
	ON_HIT_WITHER(0.08, true, null, null),      // probabilidad de marchitar al golpear
	MINION_POWER(0.10, true, null, null),       // +vida y daño de los esbirros
	KNOCKBACK(0.06, true, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Operation.ADD_VALUE),
	SPEED(0.02, true, EntityAttributes.GENERIC_MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE),
	MAX_HEALTH(1.5, false, EntityAttributes.GENERIC_MAX_HEALTH, Operation.ADD_VALUE),
	ATTACK_SPEED(0.03, true, EntityAttributes.GENERIC_ATTACK_SPEED, Operation.ADD_MULTIPLIED_BASE),
	LUCK(0.4, false, EntityAttributes.GENERIC_LUCK, Operation.ADD_VALUE);

	private final double perRank;
	private final boolean percent;
	@Nullable private final RegistryEntry<EntityAttribute> attribute;
	@Nullable private final Operation operation;

	PathEffect(double perRank, boolean percent, @Nullable RegistryEntry<EntityAttribute> attribute,
			   @Nullable Operation operation) {
		this.perRank = perRank;
		this.percent = percent;
		this.attribute = attribute;
		this.operation = operation;
	}

	public double perRank() { return perRank; }
	/** Si su valor se muestra como porcentaje. */
	public boolean percent() { return percent; }
	@Nullable public RegistryEntry<EntityAttribute> attribute() { return attribute; }
	@Nullable public Operation operation() { return operation; }

	public String translationKey() {
		return "path_effect." + Aptitudes.MOD_ID + "." + name().toLowerCase(Locale.ROOT);
	}
}
