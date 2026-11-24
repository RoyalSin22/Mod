package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.dungeon.TowersLayout;
import dev.aptitudes.dungeon.TowersLayout.Cell;
import dev.aptitudes.dungeon.TowersLayout.Tower;
import dev.aptitudes.temple.Temples;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * V86 · fase 2: Las Tres Torres Señoriales. Traduce a bloques la distribución de {@link TowersLayout} (pura y con pruebas): dos torres
 * vasallas y la Torre del Señor sobre un patio con cimiento, de piedra musgosa. Escribe dentro del radio 15 y <b>no crea entidades</b>:
 * los Guardianes y el Coloso los despertará el controlador de la fase 3. Hasta entonces el bloque controlador es un marcador
 * ({@code lodestone}) y la puerta del Señor es una piedra sellada que se puede romper; ver {@code docs/V86_DUNGEON_COLOSO.md}.
 */
public class ThreeTowersFeature extends Feature<DefaultFeatureConfig> {
	/** Desnivel máximo entre los puntos de muestreo del terreno. */
	private static final int MAX_SLOPE = 4;

	public ThreeTowersFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		if (!Aptitudes.config().threeTowersEnabled) return false;
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		if (!StructureBuilder.suitable(world, origin, TowersLayout.R, MAX_SLOPE)) return false;
		// Debe caber en altura (la empuñadura de la Torre del Señor llega a TOP_Y) y por debajo (cimiento).
		if (origin.getY() + TowersLayout.TOP_Y >= world.getTopY() - 1 || origin.getY() + TowersLayout.BOTTOM_Y <= world.getBottomY() + 4) return false;
		// El patio no puede asentarse sobre agua ni lava.
		for (int x = -14; x <= 14; x += 2) {
			for (int z = -14; z <= 14; z += 2) {
				if (!world.getFluidState(origin.add(x, 0, z)).isEmpty() || !world.getFluidState(origin.add(x, -1, z)).isEmpty()) return false;
			}
		}

		StructureBuilder b = new StructureBuilder(world, origin, context.getRandom());
		TowersLayout.Result layout = TowersLayout.build();

		// El terreno por encima del patio (hasta 8 de alto) se despeja; fuera de las torres, la capa de aire evita que la hierba y los
		// árboles se queden dentro de la plaza. Dentro de las torres lo despeja el propio layout.
		for (int x = -14; x <= 14; x++) {
			for (int z = -14; z <= 14; z++) {
				if (TowersLayout.towerAt(x, z) == null) for (int y = 1; y <= 8; y++) b.air(x, y, z);
				// Cimiento hacia abajo mientras el terreno sea sustituible (hasta 6 bloques bajo el layout).
				for (int y = TowersLayout.BOTTOM_Y - 1; y >= TowersLayout.BOTTOM_Y - 6; y--) {
					BlockState current = world.getBlockState(b.at(x, y, z));
					if (!current.isReplaceable() && current.getFluidState().isEmpty()) break;
					b.set(x, y, z, Blocks.DEEPSLATE_BRICKS.getDefaultState());
				}
			}
		}
		for (int y = TowersLayout.BOTTOM_Y; y <= TowersLayout.TOP_Y; y++) {
			for (int x = -TowersLayout.R; x <= TowersLayout.R; x++) {
				for (int z = -TowersLayout.R; z <= TowersLayout.R; z++) {
					Cell cell = layout.get(x, y, z);
					if (cell != null) b.set(x, y, z, state(cell, x, z, b));
				}
			}
		}
		StructureIndex.record(world, "three_towers", origin); // V77: /aptitudes locate
		return true;
	}

	/** Bloque real de cada celda. Musgo y grietas repartidos al azar, como en las referencias del autor. */
	private static BlockState state(Cell cell, int x, int z, StructureBuilder b) {
		return switch (cell) {
			case AIR -> Blocks.AIR.getDefaultState();
			case WALL -> wall(b);
			case PLINTH -> Blocks.DEEPSLATE_BRICKS.getDefaultState();
			case FLOOR, ROOF -> floor(b);
			case CORBEL -> Blocks.CHISELED_STONE_BRICKS.getDefaultState();
			case MERLON -> wall(b);
			case WINDOW -> Blocks.IRON_BARS.getDefaultState();
			case LANTERN -> {
				// Antorcha de almas pegada al muro de la torre: mira hacia el centro de la torre a la que pertenece.
				Tower t = TowersLayout.towerAt(x, z);
				Direction face = t != null && x < t.cx() ? Direction.EAST : Direction.WEST;
				yield Blocks.SOUL_WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, face);
			}
			case STAIR_EAST -> Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
			case STAIR_WEST -> Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);
			case SEALED -> Temples.SEALED_STONE.getDefaultState();
			case GRIP -> Blocks.POLISHED_DEEPSLATE.getDefaultState();
			case GUARD -> Blocks.DEEPSLATE_BRICKS.getDefaultState();
			case POMMEL -> Blocks.CHISELED_DEEPSLATE.getDefaultState();
			case CONTROLLER -> Blocks.LODESTONE.getDefaultState(); // marcador hasta la fase 3
		};
	}

	/** Piedra antigua y musgosa: ladrillo, musgoso, agrietado y adoquín musgoso. */
	private static BlockState wall(StructureBuilder b) {
		float r = b.random.nextFloat();
		if (r < 0.50f) return Blocks.STONE_BRICKS.getDefaultState();
		if (r < 0.75f) return Blocks.MOSSY_STONE_BRICKS.getDefaultState();
		if (r < 0.90f) return Blocks.CRACKED_STONE_BRICKS.getDefaultState();
		return Blocks.MOSSY_COBBLESTONE.getDefaultState();
	}

	private static BlockState floor(StructureBuilder b) {
		return b.random.nextFloat() < 0.8f ? Blocks.STONE_BRICKS.getDefaultState() : Blocks.CRACKED_STONE_BRICKS.getDefaultState();
	}
}
