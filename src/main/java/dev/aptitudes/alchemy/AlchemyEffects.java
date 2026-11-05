package dev.aptitudes.alchemy;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.event.EclipseManager;
import dev.aptitudes.event.WorldEventManager;
import dev.aptitudes.logic.AttributeApplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

/** Efectos vanilla persistidos y sincronizados por el juego, con acciones solo en el servidor. */
public final class AlchemyEffects {
	public static final RegistryEntry<StatusEffect> MANA = register("mana", new ManaEffect());
	public static final RegistryEntry<StatusEffect> FRENZY = register("frenzy", new FrenzyEffect());
	public static final RegistryEntry<StatusEffect> ECLIPSE_VEIL = register("eclipse_veil",
			new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x9174c9) { });

	private AlchemyEffects() {
	}

	private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, Aptitudes.id(name), effect);
	}

	public static float incoming(LivingEntity target, DamageSource source, float amount) {
		// getAttacker conserva al tirador de un proyectil. Las etiquetas de un jugador no bastan.
		boolean eventCreature = source.getAttacker() instanceof MobEntity mob
				&& (mob.getCommandTags().contains(EclipseManager.TAG)
				|| mob.getCommandTags().contains(WorldEventManager.TAG));
		return AlchemyRules.eventDamage(amount, eventCreature, target.hasStatusEffect(ECLIPSE_VEIL));
	}

	private static final class ManaEffect extends StatusEffect {
		private ManaEffect() {
			super(StatusEffectCategory.BENEFICIAL, 0x49a6e8);
		}

		@Override
		public boolean canApplyUpdateEffect(int duration, int amplifier) {
			return true;
		}

		@Override
		public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
			// El reloj de la entidad evita ganar un pulso extra al renovar la duración bebiendo.
			if (entity instanceof ServerPlayerEntity player && player.age % 20 == 0) {
				AbilityManager.restoreEnergy(player, AlchemyRules.MANA_PER_SECOND);
			}
			return true;
		}
	}

	private static final class FrenzyEffect extends StatusEffect {
		private static final Identifier SPEED_ID = Aptitudes.id("alchemy/frenzy_speed");

		private FrenzyEffect() {
			super(StatusEffectCategory.BENEFICIAL, 0xd45d46);
		}

		@Override
		public void onApplied(AttributeContainer attributes, int amplifier) {
			AttributeApplier.set(attributes.getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED), SPEED_ID,
					AlchemyRules.FRENZY_ATTACK_SPEED, Operation.ADD_MULTIPLIED_BASE);
		}

		@Override
		public void onRemoved(AttributeContainer attributes) {
			AttributeApplier.set(attributes.getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED), SPEED_ID,
					0, Operation.ADD_MULTIPLIED_BASE);
		}

		@Override
		public void forEachAttributeModifier(int amplifier,
				BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer) {
			// El tooltip usa exactamente el mismo valor acotado que el servidor.
			consumer.accept(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(SPEED_ID,
					AlchemyRules.FRENZY_ATTACK_SPEED, Operation.ADD_MULTIPLIED_BASE));
		}
	}
}
