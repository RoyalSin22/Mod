package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.dungeon.DesertTempleLayout;
import dev.aptitudes.temple.ReliquaryBlockEntity;
import dev.aptitudes.temple.TempleControllerBlockEntity;
import dev.aptitudes.temple.TempleKeyRules;
import dev.aptitudes.temple.Temples;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** V89: cada chunk evalúa sus columnas del mismo templo de 112×112. No aparecen entidades durante worldgen. */
public final class DesertTempleFeature extends Feature<DefaultFeatureConfig> {
	private record Site(boolean valid, int y, boolean crypt, boolean red) {}
	private static final Map<ServerWorld, Map<Long, Site>> SITES = new ConcurrentHashMap<>();
	private static final String[] LOOT = {"custodian_copper", "custodian_iron", "custodian_gold", "custodian_diamond"};

	public DesertTempleFeature(Codec<DefaultFeatureConfig> codec) { super(codec); }
	public static void init() { ServerLifecycleEvents.SERVER_STOPPED.register(server -> SITES.clear()); }

	@Override public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		int cx=context.getOrigin().getX()>>4, cz=context.getOrigin().getZ()>>4;
		int[] xz=StructureLocateRules.templeOrigin(world.getSeed(),cx,cz);
		if (xz==null) return false;
		Site site=site(world.toServerWorld(),context.getGenerator(),xz[0],xz[1]);
		if (!site.valid) return false;
		BlockPos origin=new BlockPos(xz[0],site.y,xz[1]);
		buildChunk(world,origin,cx,cz,site);
		BlockPos controllerPos=at(origin,DesertTempleLayout.CONTROLLER);
		if (cx==controllerPos.getX()>>4 && cz==controllerPos.getZ()>>4)
			setupController(world,origin,site,context.getWorld().toServerWorld().getSpawnPos());
		BlockPos reliquaryPos=at(origin,DesertTempleLayout.RELIQUARY);
		if (cx==reliquaryPos.getX()>>4 && cz==reliquaryPos.getZ()>>4) linkReliquary(world,origin);
		if (cx==(xz[0]>>4) && cz==(xz[1]>>4)) {
			StructureIndex.record(world,"custodian_temple",origin);
		}
		return true;
	}

	/** Colocación administrativa sin depender del límite de 31×31 de Feature.generate. */
	public static BlockPos placeAt(ServerWorld world,ChunkGenerator generator,int x,int z) {
		Site site=site(world,generator,x,z);
		if (!site.valid) return null;
		BlockPos origin=new BlockPos(x,site.y,z);
		for(int cx=(x+DesertTempleLayout.MIN)>>4;cx<=(x+DesertTempleLayout.MAX)>>4;cx++)
			for(int cz=(z+DesertTempleLayout.MIN)>>4;cz<=(z+DesertTempleLayout.MAX)>>4;cz++) world.getChunk(cx,cz);
		for(int cx=(x+DesertTempleLayout.MIN)>>4;cx<=(x+DesertTempleLayout.MAX)>>4;cx++)
			for(int cz=(z+DesertTempleLayout.MIN)>>4;cz<=(z+DesertTempleLayout.MAX)>>4;cz++) buildChunk(world,origin,cx,cz,site);
		setupController(world,origin,site,world.getSpawnPos());
		linkReliquary(world,origin);
		StructureIndex.record(world,"custodian_temple",origin);
		return origin;
	}

	private static Site site(ServerWorld world,ChunkGenerator generator,int x,int z) {
		Map<Long,Site> sites=SITES.computeIfAbsent(world,k->new ConcurrentHashMap<>());
		if (sites.size()>256) sites.clear();
		return sites.computeIfAbsent(BlockPos.asLong(x,0,z),k->inspect(world,generator,x,z));
	}

	private static Site inspect(ServerWorld world,ChunkGenerator generator,int x,int z) {
		// Un candidato de pirámide vanilla a menos de seis chunks podría invadir la huella.
		var pyramid=world.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(StructureKeys.DESERT_PYRAMID).orElse(null);
		if (pyramid==null) return new Site(false,0,false,false);
		var placement=world.getChunkManager().getStructurePlacementCalculator();
		for(var p:placement.getPlacements(pyramid))
			for(int cx=(x-96)>>4;cx<=(x+96)>>4;cx++)
				for(int cz=(z-96)>>4;cz<=(z+96)>>4;cz++)
					if(p.shouldGenerate(placement,cx,cz)) return new Site(false,0,false,false);
		var noise=world.getChunkManager().getNoiseConfig();
		boolean red=false;
		for(int bx=(x+DesertTempleLayout.MIN)>>2;bx<=(x+DesertTempleLayout.MAX)>>2;bx++) {
			for(int bz=(z+DesertTempleLayout.MIN)>>2;bz<=(z+DesertTempleLayout.MAX)>>2;bz++) {
				var biome=generator.getBiomeSource().getBiome(bx,16,bz,noise.getMultiNoiseSampler());
				if (biome.matchesKey(BiomeKeys.BADLANDS) || biome.matchesKey(BiomeKeys.ERODED_BADLANDS)
						|| biome.matchesKey(BiomeKeys.WOODED_BADLANDS)) red=true;
				else if (!biome.matchesKey(BiomeKeys.DESERT)) return new Site(false,0,false,false);
			}
		}
		int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
		for(int dx=DesertTempleLayout.MIN;dx<=DesertTempleLayout.MAX;dx+=8) {
			for(int dz=DesertTempleLayout.MIN;dz<=DesertTempleLayout.MAX;dz+=8) {
				int h=generator.getHeight(x+dx,z+dz,Heightmap.Type.WORLD_SURFACE_WG,world,noise);
				int floor=generator.getHeight(x+dx,z+dz,Heightmap.Type.OCEAN_FLOOR_WG,world,noise);
				if(h-floor>1) return new Site(false,0,false,false);
				min=Math.min(min,h);max=Math.max(max,h);
				if(max-min>DesertTempleLayout.MAX_SLOPE) return new Site(false,0,false,false);
			}
		}
		int y=generator.getHeight(x,z+62,Heightmap.Type.WORLD_SURFACE_WG,world,noise)-1;
		if (y+DesertTempleLayout.TOP_Y>=world.getTopY()-1 || y-35<=world.getBottomY()+4) return new Site(false,0,false,false);
		boolean crypt=Aptitudes.config().cryptEnabled && y+DesertTempleLayout.BOTTOM_Y>world.getBottomY()+4;
		for(int dx=DesertTempleLayout.MIN;dx<=DesertTempleLayout.MAX;dx++) {
			for(int dz=DesertTempleLayout.MIN;dz<=DesertTempleLayout.MAX;dz++) {
				if (!DesertTempleLayout.occupied(dx,dz)) continue;
				int h=generator.getHeight(x+dx,z+dz,Heightmap.Type.WORLD_SURFACE_WG,world,noise);
				if (Math.abs(h-(y+1))>DesertTempleLayout.MAX_SLOPE) return new Site(false,0,false,false);
				var column=generator.getColumnSample(x+dx,z+dz,world,noise);
				for(int dy=-35;dy<=0;dy++) if (!column.getState(y+dy).getFluidState().isEmpty()) return new Site(false,0,false,false);
				if(crypt && dx>=-12 && dx<=12 && dz>=-10 && dz<=11)
					for(int dy=DesertTempleLayout.BOTTOM_Y;dy< -35;dy++)
						if(!column.getState(y+dy).getFluidState().isEmpty()) crypt=false;
			}
		}
		return new Site(true,y,crypt,red);
	}

	private static void buildChunk(StructureWorldAccess world,BlockPos origin,int cx,int cz,Site site) {
		StructureBuilder b=new StructureBuilder(world,origin,Random.create(world.getSeed()^origin.asLong()));
		int x0=Math.max(DesertTempleLayout.MIN,(cx<<4)-origin.getX());
		int x1=Math.min(DesertTempleLayout.MAX,(cx<<4)+15-origin.getX());
		int z0=Math.max(DesertTempleLayout.MIN,(cz<<4)-origin.getZ());
		int z1=Math.min(DesertTempleLayout.MAX,(cz<<4)+15-origin.getZ());
		for(int x=x0;x<=x1;x++) for(int z=z0;z<=z1;z++)
			for(int y=site.crypt?DesertTempleLayout.BOTTOM_Y:-35;y<=DesertTempleLayout.TOP_Y;y++) {
				DesertTempleLayout.Cell cell=DesertTempleLayout.cell(x,y,z,site.crypt);
				if(cell==null) continue;
				String loot=loot(cell);
				if(loot!=null) b.chest(x,y,z,loot);
				else b.set(x,y,z,state(cell,x,y,z,site.red));
			}
	}

	private static String loot(DesertTempleLayout.Cell c) {
		return switch(c) {
			case CHEST_SUPPLIES -> "custodian_supplies";
			case CHEST_HIDDEN -> "custodian_hidden";
			case CHEST_COPPER -> LOOT[0]; case CHEST_IRON -> LOOT[1];
			case CHEST_GOLD -> LOOT[2]; case CHEST_DIAMOND -> LOOT[3];
			default -> null;
		};
	}
	private static BlockState state(DesertTempleLayout.Cell c,int x,int y,int z,boolean red) {
		BlockState wall=red ? Blocks.RED_SANDSTONE.getDefaultState() : Blocks.SANDSTONE.getDefaultState();
		BlockState cut=red ? Blocks.CUT_RED_SANDSTONE.getDefaultState() : Blocks.CUT_SANDSTONE.getDefaultState();
		BlockState carved=red ? Blocks.CHISELED_RED_SANDSTONE.getDefaultState() : Blocks.CHISELED_SANDSTONE.getDefaultState();
		return switch(c) {
			case AIR -> Blocks.AIR.getDefaultState();
			case WALL -> (x*17+y*31+z*13&15)<3 ? carved:wall;
			case FLOOR, ROOF, BRIDGE -> cut;
			case RARE_WALL, ALTAR, STATUE -> carved;
			case RUBBLE -> wall;
			case WATER -> Blocks.WATER.getDefaultState();
			case LIGHT -> Blocks.SEA_LANTERN.getDefaultState();
			case GATE, EXIT_GATE -> Temples.GATE.getDefaultState();
			case DOOR -> Temples.CUSTODIAN_DOOR.getDefaultState();
			case CONTROLLER -> Temples.TEMPLE_CONTROLLER.getDefaultState();
			case RELIQUARY -> Temples.RELIQUARY.getDefaultState();
			case CRYPT_HATCH -> Temples.CRYPT_HATCH.getDefaultState();
			case LADDER -> Blocks.LADDER.getDefaultState().with(LadderBlock.FACING,Direction.NORTH);
			case CHAMBER_COPPER -> Temples.CHAMBER_DOOR_COPPER.getDefaultState();
			case CHAMBER_IRON -> Temples.CHAMBER_DOOR_IRON.getDefaultState();
			case CHAMBER_GOLD -> Temples.CHAMBER_DOOR_GOLD.getDefaultState();
			case CHAMBER_DIAMOND -> Temples.CHAMBER_DOOR_DIAMOND.getDefaultState();
			case BOOKSHELF -> Blocks.BOOKSHELF.getDefaultState();
			case FORGE -> Blocks.BLAST_FURNACE.getDefaultState();
			case BED -> Blocks.RED_TERRACOTTA.getDefaultState();
			case MIRROR -> Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
			case OBELISK -> red ? Blocks.RED_TERRACOTTA.getDefaultState() : Blocks.TERRACOTTA.getDefaultState();
			case CHEST_SUPPLIES,CHEST_HIDDEN,CHEST_COPPER,CHEST_IRON,CHEST_GOLD,CHEST_DIAMOND -> Blocks.CHEST.getDefaultState();
		};
	}

	private static BlockPos at(BlockPos origin,int[] p) { return origin.add(p[0],p[1],p[2]); }
	private static BlockPos relative(BlockPos origin,BlockPos controller,int[] p) { return at(origin,p).subtract(controller); }
	private static List<BlockPos> relatives(BlockPos origin,BlockPos controller,List<int[]> points) {
		List<BlockPos> out=new ArrayList<>(); for(int[] p:points) out.add(relative(origin,controller,p)); return out;
	}
	private static UUID templeId(StructureWorldAccess world,BlockPos origin) {
		return UUID.nameUUIDFromBytes((world.getSeed()+":"+origin.asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
	private static void linkReliquary(StructureWorldAccess world,BlockPos origin) {
		if(world.getBlockEntity(at(origin,DesertTempleLayout.RELIQUARY)) instanceof ReliquaryBlockEntity r)
			r.setTemple(templeId(world,origin));
	}
	private static void setupController(StructureWorldAccess world,BlockPos origin,Site site,BlockPos spawn) {
		BlockPos controllerPos=at(origin,DesertTempleLayout.CONTROLLER);
		UUID id=templeId(world,origin);
		var config=Aptitudes.config();
		int ring=dev.aptitudes.logic.ThreatRules.ring(origin.getX(),origin.getZ(),spawn.getX(),spawn.getZ(),config.threatRingSize,config.threatMaxRing);
		if(world.getBlockEntity(controllerPos) instanceof TempleControllerBlockEntity c) {
			c.setupCustodian(id,ring,List.of(),new boolean[0],List.of(),
				relatives(origin,controllerPos,DesertTempleLayout.gateTwo()),relative(origin,controllerPos,DesertTempleLayout.CUSTODIAN),
				relatives(origin,controllerPos,DesertTempleLayout.custodianDoor()),relative(origin,controllerPos,DesertTempleLayout.AUTOMATON),
				relative(origin,controllerPos,DesertTempleLayout.TRIGGER));
			c.setupDesert(site.crypt,relatives(origin,controllerPos,DesertTempleLayout.gateOne()),
				relatives(origin,controllerPos,DesertTempleLayout.TRIAL_SPAWNS),relative(origin,controllerPos,DesertTempleLayout.TRIAL_MIN),
				relative(origin,controllerPos,DesertTempleLayout.TRIAL_MAX),relatives(origin,controllerPos,DesertTempleLayout.exitGates()),
				relative(origin,controllerPos,DesertTempleLayout.CATACOMBS),relative(origin,controllerPos,DesertTempleLayout.CHAPEL));
			List<BlockPos> doors=new ArrayList<>(); List<TempleKeyRules.Metal> metals=new ArrayList<>();
			for(int[] p:DesertTempleLayout.chamberDoors()) {
				doors.add(relative(origin,controllerPos,p)); metals.add(TempleKeyRules.metalFromOrdinal(p[3]));
			}
			c.setupChambers(doors,metals);
			if(site.crypt) c.setupCrypt(relatives(origin,controllerPos,DesertTempleLayout.cryptHatch()),
				relatives(origin,controllerPos,DesertTempleLayout.CRYPT_SPAWNS),relative(origin,controllerPos,DesertTempleLayout.CRYPT_MIN),
				relative(origin,controllerPos,DesertTempleLayout.CRYPT_MAX),relative(origin,controllerPos,DesertTempleLayout.CRYPT_CLIMAX),
				relative(origin,controllerPos,DesertTempleLayout.CRYPT_CHEST));
		}
	}
}
