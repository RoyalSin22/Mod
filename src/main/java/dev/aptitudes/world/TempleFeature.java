package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.logic.ThreatRules;
import dev.aptitudes.temple.ReliquaryBlockEntity;
import dev.aptitudes.temple.TempleRules;
import dev.aptitudes.temple.TempleSealBlockEntity;
import dev.aptitudes.temple.Temples;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Templo sellado (v64, K6): 15×15 con vestíbulo y santuario. En el vestíbulo, palancas cuyo patrón indican las
 * luces sobre la puerta y, desde el anillo II de Amenaza, placas que se pisan en el orden de sus velas. Al
 * resolverlo se abre la puerta y despierta el guardián, que suelta la llave del Relicario.
 *
 * <p>Solo coloca bloques dentro de su radio (7) y no crea entidades durante la generación: el guardián aparece
 * al resolver el puzle, en el hilo del servidor (mismo criterio que el arreglo de v56).</p>
 */
public class TempleFeature extends Feature<DefaultFeatureConfig> {
	private static final int R = 7;
	private static final int H = 7;

	public TempleFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	private static int ring(FeatureContext<DefaultFeatureConfig> context, BlockPos origin) {
		AptitudesConfig c = Aptitudes.config();
		BlockPos spawn = context.getWorld().toServerWorld().getSpawnPos();
		return ThreatRules.ring(origin.getX(), origin.getZ(), spawn.getX(), spawn.getZ(), c.threatRingSize, c.threatMaxRing);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		BlockPos origin = context.getOrigin();
		if (!StructureBuilder.suitable(context.getWorld(), origin, R, 3)) return false;
		StructureBuilder b = new StructureBuilder(context.getWorld(), origin, context.getRandom());
		int ring = TempleRules.clampRing(ring(context, origin));
		Palette palette = Palette.of(ring);

		// Cascarón: suelo, muros, tabique entre vestíbulo (z ≥ 0) y santuario (z ≤ -2) y techo.
		for (int x = -R; x <= R; x++) {
			for (int z = -R; z <= R; z++) {
				b.foundation(x, z, Blocks.COBBLESTONE.getDefaultState());
				boolean wall = Math.abs(x) == R || Math.abs(z) == R;
				b.set(x, 0, z, z <= -2 && !wall ? palette.accent() : palette.brick(b));
				for (int y = 1; y < H; y++) {
					if (wall || z == -1) b.set(x, y, z, palette.brick(b));
					else b.air(x, y, z);
				}
				b.set(x, H, z, palette.brick(b));
			}
		}
		// Entrada al sur.
		for (int x = -1; x <= 1; x++) for (int y = 1; y <= 2; y++) b.air(x, y, R);

		// Puerta sellada (3×3) en el tabique.
		List<BlockPos> door = new ArrayList<>();
		BlockPos seal = b.at(0, 0, 1);
		for (int x = -1; x <= 1; x++) {
			for (int y = 1; y <= 3; y++) {
				b.set(x, y, -1, Temples.SEALED_STONE.getDefaultState());
				door.add(b.at(x, y, -1).subtract(seal));
			}
		}

		// Palancas en el tabique (cara del vestíbulo) y su patrón en luces encima.
		int leverCount = TempleRules.leverCount(ring);
		int[] leverX = leverCount == 3 ? new int[]{-5, -3, 3} : new int[]{-5, -3, 3, 5};
		boolean[] target = TempleRules.pattern(b.random.nextInt(16), leverCount);
		List<BlockPos> levers = new ArrayList<>();
		BlockState lever = Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.WALL)
				.with(LeverBlock.FACING, Direction.SOUTH);
		for (int i = 0; i < leverCount; i++) {
			b.set(leverX[i], 2, 0, lever);
			levers.add(b.at(leverX[i], 2, 0).subtract(seal));
			b.set(leverX[i], 4, -1, target[i] ? Blocks.SEA_LANTERN.getDefaultState() : Blocks.CHISELED_POLISHED_BLACKSTONE.getDefaultState());
		}

		// Placas (anillo II en adelante): el número de velas junto a cada placa marca su turno.
		int plateCount = TempleRules.plateCount(ring);
		int[][] spots = {{-4, 3}, {4, 3}, {-2, 5}, {2, 5}};
		List<int[]> order = new ArrayList<>(List.of(spots).subList(0, plateCount));
		Collections.shuffle(order, new java.util.Random(b.random.nextLong()));
		List<BlockPos> plates = new ArrayList<>();
		for (int i = 0; i < order.size(); i++) {
			int[] p = order.get(i);
			b.set(p[0], 1, p[1], Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE.getDefaultState());
			plates.add(b.at(p[0], 1, p[1]).subtract(seal));
			int side = p[0] < 0 ? -1 : 1;
			b.set(p[0] + side, 1, p[1], Blocks.CANDLE.getDefaultState().with(CandleBlock.CANDLES, i + 1).with(CandleBlock.LIT, true));
		}

		// Sello delante de la puerta.
		b.set(0, 0, 1, Temples.TEMPLE_SEAL.getDefaultState());
		if (b.world.getBlockEntity(seal) instanceof TempleSealBlockEntity entity) {
			UUID temple = new UUID(b.random.nextLong(), b.random.nextLong());
			entity.setup(temple, ring, levers, target, plates, door, b.at(0, 1, -3).subtract(seal));
			// Santuario: relicario, dos cofres y luces.
			b.set(0, 1, -5, Temples.RELIQUARY.getDefaultState());
			if (b.world.getBlockEntity(b.at(0, 1, -5)) instanceof ReliquaryBlockEntity reliquary) reliquary.setTemple(temple);
		}
		b.chest(-5, 1, -5, "tower_floor");
		b.chest(5, 1, -5, ring >= 3 ? "tower_top" : "tower_upper");
		for (int[] c : new int[][]{{-6, 6}, {6, 6}, {-6, -6}, {6, -6}}) b.set(c[0], 1, c[1], Blocks.SOUL_LANTERN.getDefaultState());
		for (int[] c : new int[][]{{-6, 1}, {6, 1}, {-6, 5}, {6, 5}}) {
			if (b.random.nextFloat() < 0.5f) b.set(c[0], 5, c[1], Blocks.COBWEB.getDefaultState());
		}
		return true;
	}

	/** Materiales por anillo: piedra (0–I), pizarra profunda (II–III) y piedra negra (IV–V). */
	private record Palette(BlockState main, BlockState cracked, BlockState accent) {
		static Palette of(int ring) {
			if (ring >= 4) return new Palette(Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(),
					Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState(), Blocks.GILDED_BLACKSTONE.getDefaultState());
			if (ring >= 2) return new Palette(Blocks.DEEPSLATE_BRICKS.getDefaultState(),
					Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), Blocks.DEEPSLATE_TILES.getDefaultState());
			return new Palette(Blocks.STONE_BRICKS.getDefaultState(), Blocks.MOSSY_STONE_BRICKS.getDefaultState(),
					Blocks.CHISELED_STONE_BRICKS.getDefaultState());
		}

		BlockState brick(StructureBuilder b) {
			return b.random.nextFloat() < 0.8f ? main : cracked;
		}
	}
}
