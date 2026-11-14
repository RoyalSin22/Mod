package dev.aptitudes.world;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

/**
 * Registro de las estructuras del mod. Son {@code Feature}s colocadas por los JSON
 * de {@code data/aptitudes/worldgen} (rareza en {@code placed_feature/*.json}) y
 * añadidas a todos los biomas del Overworld.
 */
public final class ModWorldGen {
	public static final Feature<DefaultFeatureConfig> TOWER = Registry.register(Registries.FEATURE,
			Aptitudes.id("tower"), new TowerFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> BONFIRE_SHRINE = Registry.register(Registries.FEATURE,
			Aptitudes.id("bonfire_shrine"), new BonfireShrineFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> CAMP = Registry.register(Registries.FEATURE,
			Aptitudes.id("camp"), new CampFeature(DefaultFeatureConfig.CODEC));
	/** v64: Templo sellado (puzle, guardián y Relicario). */
	public static final Feature<DefaultFeatureConfig> TEMPLE = Registry.register(Registries.FEATURE,
			Aptitudes.id("temple"), new TempleFeature(DefaultFeatureConfig.CODEC));

	/** T3b: Templo del Custodio (31×31, dos niveles). Se añade a la generación en T3d. */
	public static final Feature<DefaultFeatureConfig> CUSTODIAN_TEMPLE = Registry.register(Registries.FEATURE,
			Aptitudes.id("custodian_temple"), new CustodianTempleFeature(DefaultFeatureConfig.CODEC));

	private ModWorldGen() {
	}

	public static void init() {
		for (String id : new String[]{"tower", "bonfire_shrine", "camp", "temple"}) {
			BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.SURFACE_STRUCTURES,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Aptitudes.id(id)));
		}
	}
}
