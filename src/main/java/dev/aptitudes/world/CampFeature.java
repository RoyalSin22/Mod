package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.dungeon.CampLayout;
import dev.aptitudes.dungeon.CampLayout.Cell;
import dev.aptitudes.dungeon.CampLayout.Kind;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Campamentos V90: cuatro formas, siete tipos, encuentros por anillo y alarma. */
public class CampFeature extends Feature<DefaultFeatureConfig> {
	public CampFeature(Codec<DefaultFeatureConfig> codec) { super(codec); }

	@Override public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		BlockPos origin = context.getOrigin();
		var world = context.getWorld();
		if (!StructureBuilder.suitable(world, origin, CampLayout.R, 2)
				|| origin.getY() + CampLayout.TOP >= world.getTopY() - 1
				|| origin.getY() + CampLayout.BOTTOM <= world.getBottomY() + 2) return false;
		CampLayout layout = new CampLayout(
				CampLayout.Shape.values()[context.getRandom().nextInt(CampLayout.Shape.values().length)],
				Kind.values()[context.getRandom().nextInt(Kind.values().length)],
				context.getRandom().nextLong());
		for (int x = -CampLayout.R; x <= CampLayout.R; x++) {
			for (int z = -CampLayout.R; z <= CampLayout.R; z++) {
				if (layout.cell(x, -1, z) == null) continue;
				int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX() + x, origin.getZ() + z);
				if (Math.abs(surface - origin.getY()) > 2
						|| !world.getFluidState(origin.add(x, 0, z)).isEmpty()
						|| !world.getFluidState(origin.add(x, -1, z)).isEmpty()) return false;
			}
		}
		StructureBuilder b = new StructureBuilder(world, origin, context.getRandom());
		int ring = Math.min(5, CustodianTempleFeature.ring(context, origin));
		MinorStructurePalette p = MinorStructurePalette.of(world, origin, ring);
		String loot = loot(layout.kind());
		for (int x = -CampLayout.R; x <= CampLayout.R; x++) {
			for (int z = -CampLayout.R; z <= CampLayout.R; z++) {
				if (layout.cell(x, -1, z) != null) b.foundation(x, z, p.ground());
				for (int y = CampLayout.BOTTOM; y <= CampLayout.TOP; y++) {
					Cell c = layout.cell(x, y, z);
					if (c == null) continue;
					switch (c) {
						case AIR -> b.air(x, y, z);
						case FLOOR -> b.set(x, y, z, p.ground());
						case FOUNDATION -> b.set(x, y, z, p.stone());
						case FENCE -> b.set(x, y, z, Blocks.OAK_FENCE.getDefaultState());
						case WALL -> b.set(x, y, z, p.wood());
						case WOOD, STAIR -> b.set(x, y, z, p.wood());
						case LADDER -> b.set(x, y, z, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH));
						case WOOL, TENT -> b.set(x, y, z, tentColor(layout.kind()));
						case RAMPART -> b.set(x, y, z, p.accent());
						case CAMPFIRE -> b.set(x, y, z, Blocks.CAMPFIRE.getDefaultState());
						case ALTAR -> b.set(x, y, z, layout.kind() == Kind.WARLOCK
								? Blocks.CAULDRON.getDefaultState() : Blocks.CRYING_OBSIDIAN.getDefaultState());
						case WORKBENCH -> b.set(x, y, z, Blocks.CRAFTING_TABLE.getDefaultState());
						case CARGO -> b.set(x, y, z, Blocks.BARREL.getDefaultState());
						case CHEST -> b.chest(x, y, z, loot);
						case HIDDEN_CHEST -> b.chest(x, y, z, loot);
						case CLUE -> b.set(x, y, z, Blocks.YELLOW_WOOL.getDefaultState());
						case LANTERN -> b.set(x, y, z, p.light());
						case RUBBLE -> b.air(x, y, z);
						case TARGET -> b.set(x, y, z, Blocks.TARGET.getDefaultState());
						case SMOKER -> b.set(x, y, z, Blocks.SMOKER.getDefaultState());
						case BREWING -> b.set(x, y, z, Blocks.BREWING_STAND.getDefaultState());
						case CAGE -> b.set(x, y, z, Blocks.IRON_BARS.getDefaultState());
						case BARRICADE -> b.set(x, y, z, p.cracked());
						case TRENCH -> b.air(x, y, z);
						case SCAFFOLD -> b.set(x, y, z, Blocks.SCAFFOLDING.getDefaultState());
						case CANDLE -> b.set(x, y, z, Blocks.CANDLE.getDefaultState());
						case ARMORY -> b.set(x, y, z, Blocks.SMITHING_TABLE.getDefaultState());
					}
				}
			}
		}
		int count = ring >= 4 ? 8 : ring >= 2 ? 6 : 4;
		int elites = ring >= 4 ? 2 : ring >= 2 ? 1 : 0;
		int[][] positions = {{0, 5}, {3, 6}, {-4, 1}, {4, 0}, {-3, -6}, {3, -6}, {-1, 7}, {1, -3}};
		for (int i = 0; i < count; i++) {
			int x = positions[i][0], z = positions[i][1];
			int y = layout.shape() == CampLayout.Shape.TERRACE && z < -2 ? 3 : 0;
			if (layout.shape() == CampLayout.Shape.PALISADE && i == 1) { x = 11; y = 7; z = -11; }
			String role = i < 2 ? "sentry" : i == 2 || i == 6 ? "sleeper" : i == 3 ? "patrol" : "guard";
			MobEntity mob = i < elites ? b.elite(mobType(layout.kind(), i), x, y, z)
					: b.mob(mobType(layout.kind(), i), x, y, z);
			if (i < elites) TowerFeature.armor(mob, ring);
			CampEncounters.mark(mob, layout.kind(), role, origin);
		}
		int[] captain = layout.captain();
		MobEntity boss = b.boss(mobType(layout.kind(), 0), captain[0], captain[1], captain[2],
				dev.aptitudes.boss.BossKind.CAMP_CHAMPION);
		CampEncounters.mark(boss, layout.kind(), "captain", origin);
		StructureIndex.record(world, "camp", origin);
		return true;
	}

	private static String loot(Kind kind) {
		return switch (kind) {
			case HUNTER -> "camp_hunter";
			case WARLOCK -> "camp_warlock";
			case BANDIT -> "camp_bandit";
			case RAIDED_MERCHANT -> "camp_raided_merchant";
			case DESERTER -> "camp_deserter";
			case VEIL_CULTIST -> "camp_veil_cultist";
			case RELIC_SEEKER -> "camp_relic_seeker";
		};
	}

	private static net.minecraft.block.BlockState tentColor(Kind kind) {
		return switch (kind) {
			case HUNTER -> Blocks.GREEN_WOOL.getDefaultState();
			case WARLOCK -> Blocks.PURPLE_WOOL.getDefaultState();
			case BANDIT -> Blocks.RED_WOOL.getDefaultState();
			case RAIDED_MERCHANT -> Blocks.YELLOW_WOOL.getDefaultState();
			case DESERTER -> Blocks.GRAY_WOOL.getDefaultState();
			case VEIL_CULTIST -> Blocks.BLACK_WOOL.getDefaultState();
			case RELIC_SEEKER -> Blocks.BROWN_WOOL.getDefaultState();
		};
	}

	private static EntityType<? extends MobEntity> mobType(Kind kind, int index) {
		return switch (kind) {
			case HUNTER -> EntityType.PILLAGER;
			case WARLOCK -> EntityType.WITCH;
			case BANDIT, RAIDED_MERCHANT -> index % 2 == 0 ? EntityType.VINDICATOR : EntityType.PILLAGER;
			case DESERTER -> EntityType.ZOMBIE;
			case VEIL_CULTIST -> index % 2 == 0 ? EntityType.WITHER_SKELETON : EntityType.SKELETON;
			case RELIC_SEEKER -> index % 3 == 0 ? EntityType.SPIDER : EntityType.ZOMBIE;
		};
	}
}
