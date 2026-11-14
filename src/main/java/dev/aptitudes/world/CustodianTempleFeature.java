package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.temple.CustodianTempleLayout;
import dev.aptitudes.temple.CustodianTempleLayout.Cell;
import dev.aptitudes.temple.ReliquaryBlockEntity;
import dev.aptitudes.temple.TempleKeyRules;
import dev.aptitudes.temple.TempleRules;
import dev.aptitudes.temple.TempleSealBlockEntity;
import dev.aptitudes.temple.Temples;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Templo del Custodio (T3b): 31×31 en dos niveles. En la superficie, el atrio (donde dormirá el Autómata) y la sala del
 * puzle; bajo ella, una escalera, la antesala, la Puerta del Custodio y la cámara del Custodio con el Relicario.
 *
 * <p>La distribución la calcula {@link CustodianTempleLayout} (pura y con pruebas); aquí solo se traduce a bloques. Escribe
 * dentro del radio 15 (el máximo seguro de un {@code Feature}) y no crea entidades durante la generación: el Autómata y
 * el Custodio aparecen después, en el hilo del servidor, desde el sello.</p>
 */
public class CustodianTempleFeature extends Feature<DefaultFeatureConfig> {
	/** Desnivel máximo entre los puntos de muestreo del terreno. */
	private static final int MAX_SLOPE = 4;

	public CustodianTempleFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		if (!StructureBuilder.suitable(world, origin, CustodianTempleLayout.R, MAX_SLOPE)) return false;
		// Debe caber por encima y por debajo: el casco llega 9 bloques bajo el suelo.
		if (origin.getY() + CustodianTempleLayout.ROOF_Y >= world.getTopY() - 1
				|| origin.getY() + CustodianTempleLayout.HULL_BOTTOM_Y <= world.getBottomY() + 4) return false;

		StructureBuilder b = new StructureBuilder(world, origin, context.getRandom());
		int ring = TempleRules.clampRing(TempleFeature.ring(context, origin));
		TempleFeature.Palette palette = TempleFeature.Palette.of(ring);

		boolean[] target = TempleRules.pattern(b.random.nextInt(16), TempleRules.leverCount(ring));
		int plateCount = TempleRules.plateCount(ring);
		CustodianTempleLayout.Result layout = CustodianTempleLayout.build(target, plateCount,
				CustodianTempleLayout.shuffledPlateOrder(plateCount, b.random.nextLong()));

		for (int y = CustodianTempleLayout.HULL_BOTTOM_Y; y <= CustodianTempleLayout.ROOF_Y; y++) {
			for (int x = -CustodianTempleLayout.R; x <= CustodianTempleLayout.R; x++) {
				for (int z = -CustodianTempleLayout.R; z <= CustodianTempleLayout.R; z++) {
					Cell cell = layout.get(x, y, z);
					if (cell == null) continue;
					String loot = chestLoot(cell);
					if (loot != null) b.chest(x, y, z, loot);
					else b.set(x, y, z, state(cell, palette, ring, b));
				}
			}
		}
		// Telarañas en algunas esquinas altas, como en el templo antiguo.
		for (int[] c : new int[][] {{-14, 7, 14}, {14, 7, 14}, {-14, 7, 5}, {14, 7, 5}, {-14, 7, -14}, {14, 7, -14}}) {
			if (layout.get(c[0], c[1], c[2]) == Cell.AIR && b.random.nextFloat() < 0.5f) b.set(c[0], c[1], c[2], Blocks.COBWEB.getDefaultState());
		}

		// Controlador y Relicario: todas las posiciones del sello son relativas a él.
		BlockPos seal = b.at(layout.seal[0], layout.seal[1], layout.seal[2]);
		UUID temple = new UUID(b.random.nextLong(), b.random.nextLong());
		if (world.getBlockEntity(seal) instanceof TempleSealBlockEntity entity) {
			entity.setupCustodian(temple, ring, relative(b, seal, layout.levers), target, relative(b, seal, layout.plates),
					relative(b, seal, layout.hatch), rel(b, seal, layout.custodianSpawn), relative(b, seal, layout.custodianDoor),
					rel(b, seal, layout.automatonSpawn), rel(b, seal, layout.triggerCenter));
		}
		if (world.getBlockEntity(seal) instanceof TempleSealBlockEntity entity) {
			// T4: puertas de las cuatro cámaras, con el metal de cada una.
			List<BlockPos> doors = new ArrayList<>();
			List<TempleKeyRules.Metal> metals = new ArrayList<>();
			for (int[] d : layout.chamberDoors) {
				TempleKeyRules.Metal metal = TempleKeyRules.metalFromOrdinal(d[3]);
				if (metal == null) continue;
				doors.add(rel(b, seal, d));
				metals.add(metal);
			}
			entity.setupChambers(doors, metals);
		}
		if (world.getBlockEntity(b.at(layout.reliquary[0], layout.reliquary[1], layout.reliquary[2])) instanceof ReliquaryBlockEntity reliquary) {
			reliquary.setTemple(temple);
		}
		return true;
	}

	/** Tabla de botín del cofre de cada cámara (T4), o {@code null} si la celda no es un cofre. */
	private static String chestLoot(Cell cell) {
		return switch (cell) {
			case CHEST_COPPER -> "custodian_copper";
			case CHEST_IRON -> "custodian_iron";
			case CHEST_GOLD -> "custodian_gold";
			case CHEST_DIAMOND -> "custodian_diamond";
			default -> null;
		};
	}

	private static BlockPos rel(StructureBuilder b, BlockPos seal, int[] p) {
		return b.at(p[0], p[1], p[2]).subtract(seal);
	}

	private static List<BlockPos> relative(StructureBuilder b, BlockPos seal, List<int[]> points) {
		List<BlockPos> out = new ArrayList<>();
		for (int[] p : points) out.add(rel(b, seal, p));
		return out;
	}

	/** Bloque real de cada celda de la distribución. */
	private static BlockState state(Cell cell, TempleFeature.Palette palette, int ring, StructureBuilder b) {
		return switch (cell) {
			case AIR -> Blocks.AIR.getDefaultState();
			case HULL, WALL -> palette.brick(b);
			case FLOOR, ROOF -> palette.main();
			case ACCENT, PILLAR -> palette.accent();
			case STAIR_NORTH -> stairs(ring).with(StairsBlock.FACING, Direction.NORTH);
			case SEALED -> Temples.SEALED_STONE.getDefaultState();
			case DOOR -> Temples.CUSTODIAN_DOOR.getDefaultState();
			case LEVER -> Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.WALL).with(LeverBlock.FACING, Direction.SOUTH);
			case LIGHT_ON -> Blocks.SEA_LANTERN.getDefaultState();
			case LIGHT_OFF -> Blocks.CHISELED_POLISHED_BLACKSTONE.getDefaultState();
			case PLATE -> Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE.getDefaultState();
			case CANDLE_1 -> candle(1);
			case CANDLE_2 -> candle(2);
			case CANDLE_3 -> candle(3);
			case CANDLE_4 -> candle(4);
			case SEAL -> Temples.TEMPLE_SEAL.getDefaultState();
			case RELIQUARY -> Temples.RELIQUARY.getDefaultState();
			case LANTERN -> Blocks.SOUL_LANTERN.getDefaultState();
			case CHAMBER_DOOR_COPPER -> Temples.CHAMBER_DOOR_COPPER.getDefaultState();
			case CHAMBER_DOOR_IRON -> Temples.CHAMBER_DOOR_IRON.getDefaultState();
			case CHAMBER_DOOR_GOLD -> Temples.CHAMBER_DOOR_GOLD.getDefaultState();
			case CHAMBER_DOOR_DIAMOND -> Temples.CHAMBER_DOOR_DIAMOND.getDefaultState();
			// Los cofres se colocan con su botín en generate (chestLoot); aquí solo se completa el switch.
			case CHEST_COPPER, CHEST_IRON, CHEST_GOLD, CHEST_DIAMOND -> Blocks.CHEST.getDefaultState();
		};
	}

	private static BlockState candle(int count) {
		return Blocks.CANDLE.getDefaultState().with(CandleBlock.CANDLES, count).with(CandleBlock.LIT, true);
	}

	/** Escalones a juego con la paleta por anillo (piedra, pizarra profunda, piedra negra). */
	private static BlockState stairs(int ring) {
		if (ring >= 4) return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.getDefaultState();
		if (ring >= 2) return Blocks.DEEPSLATE_BRICK_STAIRS.getDefaultState();
		return Blocks.STONE_BRICK_STAIRS.getDefaultState();
	}
}
