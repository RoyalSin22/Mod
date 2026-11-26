package dev.aptitudes.mob;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registry;

/** Registro del Osario Quebrado (v65). Se llama desde la inicialización del mod. */
public final class OsarioEntities {
	public static final EntityType<OsarioEntity> OSARIO = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("osario_quebrado"),
			EntityType.Builder.create(OsarioEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.75f, 1.8f).maxTrackingRange(8)
					.build("osario_quebrado"));

	/** Huevo de aparición (para pruebas y para el bestiario). */
	public static final net.minecraft.item.Item OSARIO_EGG = Registry.register(Registries.ITEM,
			Aptitudes.id("osario_quebrado_spawn_egg"),
			new net.minecraft.item.SpawnEggItem(OSARIO, 0x4A3B2E, 0x4DE2A6, new net.minecraft.item.Item.Settings()));

	private OsarioEntities() {}

	public static void register() {
		FabricDefaultAttributeRegistry.register(OSARIO, OsarioEntity.createAttributes());
		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Aptitudes.id("items")))
				.register(entries -> entries.add(OSARIO_EGG)); // huevo en la pestaña creativa
	}
}
