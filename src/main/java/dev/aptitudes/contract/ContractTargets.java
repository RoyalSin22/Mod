package dev.aptitudes.contract;

import dev.aptitudes.boss.BossKind;
import dev.aptitudes.data.Skill;
import dev.aptitudes.event.WorldEventKind;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

/**
 * v30: qué significa el número de objetivo de cada tipo de contrato (criatura, objeto, habilidad, evento o jefe)
 * y su nombre para mostrarlo. El orden de las listas se guarda en los contratos: solo se añade al final.
 */
public final class ContractTargets {
	public static final List<EntityType<?>> HUNT = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER,
			EntityType.SPIDER, EntityType.ENDERMAN);
	public static final List<Item> DELIVERY = List.of(Items.IRON_INGOT, Items.WHEAT, Items.CARROT, Items.COD,
			Items.SALMON, Items.POTATO);

	/** Criaturas del Nether que cuentan para el contrato «Caza en el Nether» (v65). */
	public static final List<EntityType<?>> NETHER = List.of(EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE,
			EntityType.WITHER_SKELETON, EntityType.HOGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE);
	/** Criaturas nuevas de v65 para «Criaturas nuevas» (Osario, Errante, Escorificador y Custodio). */
	public static final List<EntityType<?>> NEW_MOBS = List.of(dev.aptitudes.mob.OsarioEntities.OSARIO,
			dev.aptitudes.mob.NewMobs.ERRANTE, dev.aptitudes.mob.NewMobs.ESCORIFICADOR, dev.aptitudes.mob.NewMobs.CUSTODIO);

	private ContractTargets() {
	}

	/** Índice de caza de una criatura, o −1 si no es objetivo de ningún contrato. */
	public static int huntIndex(EntityType<?> type) {
		return HUNT.indexOf(type);
	}

	public static boolean deliveryMatches(int target, ItemStack stack) {
		return target >= 0 && target < DELIVERY.size() && stack.isOf(DELIVERY.get(target));
	}

	/** Nombre traducible del objetivo (vacío si el tipo no tiene objetivo concreto). */
	public static Text name(ContractRules.Kind kind, int target) {
		return switch (kind) {
			case HUNT -> target >= 0 && target < HUNT.size() ? HUNT.get(target).getName() : Text.empty();
			case DELIVERY -> target >= 0 && target < DELIVERY.size() ? DELIVERY.get(target).getName() : Text.empty();
			case PROFESSION -> target >= 0 && target < Skill.values().length
					? Text.translatable(Skill.values()[target].translationKey()) : Text.empty();
			case EVENT -> target >= 0 && target < WorldEventKind.values().length
					? Text.translatable("event.aptitudes." + WorldEventKind.values()[target].id() + ".name") : Text.empty();
			case BOSS -> target >= 0 && target < BossKind.values().length
					? Text.translatable(BossKind.values()[target].nameKey()) : Text.empty();
			default -> Text.empty();
		};
	}
}
