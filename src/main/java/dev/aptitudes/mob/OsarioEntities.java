package dev.aptitudes.mob;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Registro del Osario Quebrado (v65). Se llama desde la inicialización del mod. */
public final class OsarioEntities {
	public static final EntityType<OsarioEntity> OSARIO = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("osario_quebrado"),
			EntityType.Builder.create(OsarioEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.75f, 1.8f).maxTrackingRange(8)
					.build("osario_quebrado"));

	private OsarioEntities() {}

	public static void register() {
		FabricDefaultAttributeRegistry.register(OSARIO, OsarioEntity.createAttributes());
	}
}
