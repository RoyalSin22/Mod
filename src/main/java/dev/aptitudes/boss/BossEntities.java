package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

/**
 * v63 (C4): registro de los jefes animados (GeckoLib). No aparecen de forma natural: los colocarán sus
 * estructuras (ver {@code docs/V63_JEFES.md}); de momento, con su huevo o {@code /summon}.
 */
public final class BossEntities {
	public static final EntityType<FacelessColossusEntity> FACELESS_COLOSSUS = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("faceless_colossus"),
			EntityType.Builder.create(FacelessColossusEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.6f, 3.4f).eyeHeight(3.0f).maxTrackingRange(10).makeFireImmune()
					.build("faceless_colossus"));

	public static final Item FACELESS_COLOSSUS_EGG = Registry.register(Registries.ITEM, Aptitudes.id("faceless_colossus_spawn_egg"),
			new SpawnEggItem(FACELESS_COLOSSUS, 0x2B2D38, 0xB0423A, new Item.Settings()));

	private BossEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(FACELESS_COLOSSUS, FacelessColossusEntity.createAttributes());
		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Aptitudes.id("items")))
				.register(entries -> entries.add(FACELESS_COLOSSUS_EGG));
	}
}
