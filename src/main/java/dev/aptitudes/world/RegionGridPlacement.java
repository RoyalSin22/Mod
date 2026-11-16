package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

/**
 * V77: modificador de colocación por cuadrícula de regiones (ver {@link StructureLocateRules#designatedChunk}). Deja pasar solo el
 * chunk designado de cada región, de modo que {@code /aptitudes locate} pueda saber dónde buscar sin generar todo el terreno.
 * En los JSON: {@code {"type": "aptitudes:region_grid", "kind": "custodian_temple", "region_size": 32}}.
 */
public final class RegionGridPlacement extends AbstractConditionalPlacementModifier {
	public static final MapCodec<RegionGridPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("kind").forGetter(p -> p.kind),
			Codec.intRange(2, 512).fieldOf("region_size").forGetter(p -> p.size)
	).apply(i, RegionGridPlacement::new));
	public static final PlacementModifierType<RegionGridPlacement> TYPE = () -> CODEC;

	private final String kind;
	private final int size;

	public RegionGridPlacement(String kind, int size) {
		this.kind = kind;
		this.size = size;
	}

	@Override
	protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
		return StructureLocateRules.isDesignated(context.getWorld().getSeed(), kind, pos.getX() >> 4, pos.getZ() >> 4, size);
	}

	@Override
	public PlacementModifierType<?> getType() {
		return TYPE;
	}
}
