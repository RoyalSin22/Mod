package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.dungeon.FortressLayout;
import dev.aptitudes.dungeon.FortressPalette;
import dev.aptitudes.dungeon.TowersBlocks;
import dev.aptitudes.dungeon.TowersControllerBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BedBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** V88: cada chunk construye solo su parte del plano de 128×128, desde un origen común por región. */
public final class ThreeTowersFeature extends Feature<DefaultFeatureConfig> {
	private record Site(boolean valid, int y) {}
	private static final Map<ServerWorld, Map<Long, Site>> SITES = new ConcurrentHashMap<>();
	private static final Map<String, BlockState> PALETTE_STATES = new ConcurrentHashMap<>();

	public ThreeTowersFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	public static void init() {
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SITES.clear());
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		if (!Aptitudes.config().threeTowersEnabled) return false;
		StructureWorldAccess world = context.getWorld();
		BlockPos probe = context.getOrigin();
		int chunkX = probe.getX() >> 4, chunkZ = probe.getZ() >> 4;
		int[] xz = StructureLocateRules.fortressOrigin(world.getSeed(), chunkX, chunkZ);
		if (xz == null) return false;
		Site site = site(world.toServerWorld(), context.getGenerator(), xz[0], xz[1]);
		if (!site.valid()) return false;
		BlockPos origin = new BlockPos(xz[0], site.y(), xz[1]);
		int ring = placeRing(world.toServerWorld(), xz[0], xz[1]);
		buildChunk(world, origin, chunkX, chunkZ, ring);
		if (chunkX == xz[0] >> 4 && chunkZ == xz[1] >> 4) {
			if (world.getBlockEntity(origin) instanceof TowersControllerBlockEntity controller) {
				controller.setupFortress(ring);
			}
			StructureIndex.record(world, "three_towers", origin);
		}
		return true;
	}

	/** Colocación administrativa: usa la misma validación y construye los trozos por separado. */
	public static BlockPos placeAt(ServerWorld world, ChunkGenerator generator, int x, int z) {
		if (!Aptitudes.config().threeTowersEnabled) return null;
		Site site = site(world, generator, x, z);
		if (!site.valid()) return null;
		BlockPos origin = new BlockPos(x, site.y(), z);
		int ring = placeRing(world, x, z);
		for (int cx = (x + FortressLayout.MIN) >> 4; cx <= (x + FortressLayout.MAX) >> 4; cx++) {
			for (int cz = (z + FortressLayout.MIN) >> 4; cz <= (z + FortressLayout.MAX) >> 4; cz++) world.getChunk(cx, cz);
		}
		for (int cx = (x + FortressLayout.MIN) >> 4; cx <= (x + FortressLayout.MAX) >> 4; cx++) {
			for (int cz = (z + FortressLayout.MIN) >> 4; cz <= (z + FortressLayout.MAX) >> 4; cz++) buildChunk(world, origin, cx, cz, ring);
		}
		if (world.getBlockEntity(origin) instanceof TowersControllerBlockEntity controller) {
			controller.setupFortress(ring);
		}
		StructureIndex.record(world, "three_towers", origin);
		return origin;
	}

	private static Site site(ServerWorld world, ChunkGenerator generator, int x, int z) {
		Map<Long, Site> byOrigin = SITES.computeIfAbsent(world, ignored -> new ConcurrentHashMap<>());
		long key = BlockPos.asLong(x, 0, z);
		if (byOrigin.size() > 256) byOrigin.clear();
		return byOrigin.computeIfAbsent(key, ignored -> inspectSite(world, generator, x, z));
	}

	private static Site inspectSite(ServerWorld world, ChunkGenerator generator, int x, int z) {
		var noise = world.getChunkManager().getNoiseConfig();
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		// Criba barata antes de consultar todas las columnas ocupadas; también descarta agua en patios abiertos.
		for (int dx = FortressLayout.MIN; dx <= FortressLayout.MAX; dx += 16) {
			for (int dz = FortressLayout.MIN; dz <= FortressLayout.MAX; dz += 16) {
				int surface = generator.getHeight(x + dx, z + dz, Heightmap.Type.WORLD_SURFACE_WG, world, noise);
				int floor = generator.getHeight(x + dx, z + dz, Heightmap.Type.OCEAN_FLOOR_WG, world, noise);
				if (surface - floor > 1) return new Site(false, 0);
				min = Math.min(min, surface);
				max = Math.max(max, surface);
				if (max - min > FortressLayout.MAX_SLOPE) return new Site(false, 0);
			}
		}
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		for (int dx = FortressLayout.MIN; dx <= FortressLayout.MAX; dx++) {
			for (int dz = FortressLayout.MIN; dz <= FortressLayout.MAX; dz++) {
				if (!FortressLayout.occupied(dx, dz)) continue;
				int surface = generator.getHeight(x + dx, z + dz, Heightmap.Type.WORLD_SURFACE_WG, world, noise);
				int floor = generator.getHeight(x + dx, z + dz, Heightmap.Type.OCEAN_FLOOR_WG, world, noise);
				if (surface - floor > 1) return new Site(false, 0);
				min = Math.min(min, surface);
				max = Math.max(max, surface);
				if (max - min > FortressLayout.MAX_SLOPE) return new Site(false, 0);
			}
		}
		return new Site(min != Integer.MAX_VALUE && max + FortressLayout.TOP_Y < world.getTopY() - 1
				&& max - FortressLayout.MAX_FOUNDATION > world.getBottomY() + 4, max);
	}

	private static int placeRing(ServerWorld world, int x, int z) {
		var config = Aptitudes.config();
		BlockPos spawn = world.getSpawnPos();
		return dev.aptitudes.logic.ThreatRules.ring(x, z, spawn.getX(), spawn.getZ(), config.threatRingSize, config.threatMaxRing);
	}

	private static String biome(StructureWorldAccess world, BlockPos at) {
		var biome = world.getBiome(at);
		if (biome.matchesKey(BiomeKeys.DESERT)) return "minecraft:desert";
		if (biome.matchesKey(BiomeKeys.BADLANDS) || biome.matchesKey(BiomeKeys.ERODED_BADLANDS)
				|| biome.matchesKey(BiomeKeys.WOODED_BADLANDS)) return "minecraft:badlands";
		if (biome.isIn(BiomeTags.IS_TAIGA)) return "minecraft:taiga";
		if (biome.matchesKey(BiomeKeys.SNOWY_PLAINS)) return "minecraft:snowy_plains";
		return "minecraft:plains";
	}

	private static void buildChunk(StructureWorldAccess world, BlockPos origin, int chunkX, int chunkZ, int ring) {
		StructureBuilder b = new StructureBuilder(world, origin, Random.create(world.getSeed() ^ origin.asLong()));
		String biome = biome(world, new BlockPos((chunkX << 4) + 8, origin.getY(), (chunkZ << 4) + 8));
		int minX = Math.max(FortressLayout.MIN, (chunkX << 4) - origin.getX());
		int maxX = Math.min(FortressLayout.MAX, (chunkX << 4) + 15 - origin.getX());
		int minZ = Math.max(FortressLayout.MIN, (chunkZ << 4) - origin.getZ());
		int maxZ = Math.min(FortressLayout.MAX, (chunkZ << 4) + 15 - origin.getZ());
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!FortressLayout.occupied(x, z)) continue;
				for (int y = -4; y >= -FortressLayout.MAX_FOUNDATION; y--) {
					BlockPos p = b.at(x, y, z);
					BlockState current = world.getBlockState(p);
					if (!current.isReplaceable() && current.getFluidState().isEmpty()) break;
					b.set(x, y, z, Blocks.DEEPSLATE_BRICKS.getDefaultState());
				}
				for (int y = FortressLayout.BOTTOM_Y; y <= FortressLayout.TOP_Y; y++) {
					FortressLayout.Cell cell = FortressLayout.cell(x, y, z);
					if (cell == null) continue;
					if (cell == FortressLayout.Cell.HIDDEN_CHEST || cell == FortressLayout.Cell.SUPPLY_CHEST) b.chest(x, y, z, "three_towers_supplies");
					else b.set(x, y, z, state(cell, x, y, z, ring, biome));
				}
			}
		}
	}

	private static BlockState state(FortressLayout.Cell cell, int x, int y, int z, int ring, String biome) {
		String key = FortressPalette.state(cell, x, y, z, ring, biome);
		return PALETTE_STATES.computeIfAbsent(key, ThreeTowersFeature::resolveState);
	}

	private static BlockState resolveState(String key) {
		int bracket = key.indexOf('[');
		BlockState state = Registries.BLOCK.get(Identifier.of(bracket < 0 ? key : key.substring(0, bracket))).getDefaultState();
		if (bracket < 0) return state;
		if (state.getBlock() instanceof StairsBlock) {
			return state.with(StairsBlock.FACING, key.contains("facing=east") ? Direction.EAST : Direction.WEST);
		}
		if (state.getBlock() instanceof BedBlock) {
			return state.with(BedBlock.PART, key.contains("part=head") ? BedPart.HEAD : BedPart.FOOT)
					.with(HorizontalFacingBlock.FACING, Direction.SOUTH);
		}
		throw new IllegalArgumentException("Propiedades no admitidas en la paleta: " + key);
	}
}
