package dev.aptitudes.mob;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registry;

/** Registro de los enemigos nuevos de v65: Errante Velado (Niebla) y Custodio de las Cuatro Llaves (templo). */
public final class NewMobs {
	public static final EntityType<ErranteEntity> ERRANTE = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("errante_velado"),
			EntityType.Builder.create(ErranteEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.75f, 1.85f).maxTrackingRange(8)
					.build("errante_velado"));

	public static final EntityType<CustodioEntity> CUSTODIO = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("custodio_cuatro_llaves"),
			EntityType.Builder.create(CustodioEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.15f, 2.35f).maxTrackingRange(12).makeFireImmune()
					.build("custodio_cuatro_llaves"));

	public static final Item ERRANTE_EGG = Registry.register(Registries.ITEM, Aptitudes.id("errante_velado_spawn_egg"),
			new SpawnEggItem(ERRANTE, 0x34383D, 0x65D6D0, new Settings()));

	private NewMobs() {}

	public static void register() {
		FabricDefaultAttributeRegistry.register(ERRANTE, ErranteEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(CUSTODIO, CustodioEntity.createAttributes());
		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Aptitudes.id("items")))
				.register(entries -> entries.add(ERRANTE_EGG)); // huevo en la pestaña creativa
	}
}
