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
					.dimensions(1.6f, 3.8f).eyeHeight(3.4f).maxTrackingRange(10).makeFireImmune()
					.build("faceless_colossus"));

	public static final Item FACELESS_COLOSSUS_EGG = Registry.register(Registries.ITEM, Aptitudes.id("faceless_colossus_spawn_egg"),
			new SpawnEggItem(FACELESS_COLOSSUS, 0x2B2D38, 0xB0423A, new Item.Settings()));

	public static final EntityType<IronColossusEntity> IRON_COLOSSUS = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("iron_colossus"),
			EntityType.Builder.create(IronColossusEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.6f, 3.8f).eyeHeight(3.4f).maxTrackingRange(10).makeFireImmune()
					.build("iron_colossus"));

	public static final Item IRON_COLOSSUS_EGG = Registry.register(Registries.ITEM, Aptitudes.id("iron_colossus_spawn_egg"),
			new SpawnEggItem(IRON_COLOSSUS, 0x4A4E58, 0xB07A2A, new Item.Settings()));

	public static final EntityType<GuardianEntity> DISMANTLED_GUARDIAN = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("dismantled_guardian"),
			EntityType.Builder.create(GuardianEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.2f, 3.0f).eyeHeight(2.7f).maxTrackingRange(10).makeFireImmune()
					.build("dismantled_guardian"));

	public static final EntityType<AutomatonEntity> ANCIENT_AUTOMATON = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("ancient_automaton"),
			EntityType.Builder.create(AutomatonEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.4f, 2.8f).eyeHeight(2.4f).maxTrackingRange(10).makeFireImmune()
					.build("ancient_automaton"));

	public static final EntityType<AutomatonOrbEntity> AUTOMATON_ORB = Registry.register(Registries.ENTITY_TYPE,
			Aptitudes.id("automaton_orb"),
			EntityType.Builder.<AutomatonOrbEntity>create(AutomatonOrbEntity::new, SpawnGroup.MISC)
					.dimensions(0.4f, 0.4f).maxTrackingRange(8).trackingTickInterval(5)
					.build("automaton_orb"));

	/** Aspecto de la esfera del Autómata (solo para dibujarla; no se entrega como objeto). v65. */
	public static final Item AUTOMATON_ORB_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("automaton_orb"),
			new Item(new Item.Settings()));
	/** Esfera devuelta: cian; al impactar aturde al Autómata. */
	public static final Item AUTOMATON_ORB_DEFLECTED_ITEM = Registry.register(Registries.ITEM,
			Aptitudes.id("automaton_orb_deflected"), new Item(new Item.Settings()));

	public static final Item DISMANTLED_GUARDIAN_EGG = Registry.register(Registries.ITEM, Aptitudes.id("dismantled_guardian_spawn_egg"),
			new SpawnEggItem(DISMANTLED_GUARDIAN, 0x3A3D48, 0xC9A54C, new Item.Settings()));

	public static final Item ANCIENT_AUTOMATON_EGG = Registry.register(Registries.ITEM, Aptitudes.id("ancient_automaton_spawn_egg"),
			new SpawnEggItem(ANCIENT_AUTOMATON, 0x6B5A3E, 0x3FB6C9, new Item.Settings()));

	private BossEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(FACELESS_COLOSSUS, FacelessColossusEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(IRON_COLOSSUS, IronColossusEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(DISMANTLED_GUARDIAN, GuardianEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ANCIENT_AUTOMATON, AutomatonEntity.createAttributes());
		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Aptitudes.id("items")))
				.register(entries -> {
					entries.add(FACELESS_COLOSSUS_EGG);
					entries.add(IRON_COLOSSUS_EGG);
					entries.add(DISMANTLED_GUARDIAN_EGG);
					entries.add(ANCIENT_AUTOMATON_EGG);
				});
	}
}
