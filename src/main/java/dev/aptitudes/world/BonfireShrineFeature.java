package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.block.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Santuario de Hoguera: suelo circular en ruinas, columnas rotas y una Hoguera
 * ya encendida en el centro, con un cofre (Fragmentos de alma, frascos y, a
 * veces, una Ofrenda de renacimiento). Sirve de punto de descanso en el mundo.
 */
public class BonfireShrineFeature extends Feature<DefaultFeatureConfig> {
	public BonfireShrineFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		BlockPos origin = context.getOrigin();
		if (!StructureBuilder.suitable(context.getWorld(), origin, 4, 2)) return false;
		StructureBuilder b = new StructureBuilder(context.getWorld(), origin, context.getRandom());
		for (int x = -4; x <= 4; x++) {
			for (int z = -4; z <= 4; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 4.4) continue;
				b.foundation(x, z, Blocks.COBBLESTONE.getDefaultState());
				// Suelo con huecos de hierba (ruina)
				if (b.random.nextFloat() < 0.85f) {
					b.set(x, -1, z, d < 2 ? Blocks.CHISELED_STONE_BRICKS.getDefaultState()
							: b.random.nextBoolean() ? b.bricks() : Blocks.MOSSY_COBBLESTONE.getDefaultState());
				}
				for (int y = 0; y <= 4; y++) b.air(x, y, z);
			}
		}
		// Cuatro columnas rotas de altura aleatoria
		int[][] pillars = {{3, 3}, {-3, 3}, {3, -3}, {-3, -3}};
		for (int[] p : pillars) {
			int h = 1 + b.random.nextInt(4);
			for (int y = 0; y < h; y++) b.set(p[0], y, p[1], b.bricks());
			if (h >= 3) b.set(p[0], h, p[1], Blocks.STONE_BRICK_SLAB.getDefaultState());
		}
		b.set(0, 0, 0, ModBlocks.BONFIRE.getDefaultState());
		b.chest(2, 0, 0, "bonfire_shrine");
		b.set(-2, 0, 0, Blocks.CANDLE.getDefaultState().with(net.minecraft.block.CandleBlock.LIT, true));
		return true;
	}
}
