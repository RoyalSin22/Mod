package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Campamento temático (11×11): dos tiendas, hoguera de campamento, cofres y
 * barriles con botín del tipo y enemigos de guardia.
 * <ul>
 *   <li><b>Cazadores</b>: tiendas verdes, pacas de heno; pillagers; botín de caza (arcos, flechas, cuero, set de Cazador).</li>
 *   <li><b>Brujos</b>: tiendas negras y moradas, hoguera de almas, obsidiana llorosa; brujas; botín maldito.</li>
 *   <li><b>Bandidos</b>: tiendas rojas, barriles; vindicadores y un pillager; armas y armaduras.</li>
 * </ul>
 */
public class CampFeature extends Feature<DefaultFeatureConfig> {
	private enum Kind { HUNTER, WARLOCK, BANDIT }

	public CampFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		BlockPos origin = context.getOrigin();
		if (!StructureBuilder.suitable(context.getWorld(), origin, 5, 2)) return false;
		StructureBuilder b = new StructureBuilder(context.getWorld(), origin, context.getRandom());
		Kind kind = Kind.values()[b.random.nextInt(Kind.values().length)];

		// Suelo: tierra apisonada y gruesa, y despeje de vegetación
		for (int x = -5; x <= 5; x++) {
			for (int z = -5; z <= 5; z++) {
				if (x * x + z * z > 30) continue;
				b.foundation(x, z, Blocks.DIRT.getDefaultState());
				b.set(x, -1, z, b.random.nextFloat() < 0.6f ? Blocks.COARSE_DIRT.getDefaultState()
						: kind == Kind.WARLOCK ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.DIRT_PATH.getDefaultState());
				for (int y = 0; y <= 4; y++) b.air(x, y, z);
			}
		}
		BlockState wallA = switch (kind) {
			case HUNTER -> Blocks.GREEN_WOOL.getDefaultState();
			case WARLOCK -> Blocks.BLACK_WOOL.getDefaultState();
			case BANDIT -> Blocks.RED_WOOL.getDefaultState();
		};
		BlockState wallB = switch (kind) {
			case HUNTER -> Blocks.BROWN_WOOL.getDefaultState();
			case WARLOCK -> Blocks.PURPLE_WOOL.getDefaultState();
			case BANDIT -> Blocks.GRAY_WOOL.getDefaultState();
		};
		tent(b, -3, wallA, wallB);
		tent(b, 3, wallB, wallA);

		// Centro: hoguera de campamento
		b.set(0, 0, 0, kind == Kind.WARLOCK ? Blocks.SOUL_CAMPFIRE.getDefaultState() : Blocks.CAMPFIRE.getDefaultState());
		String loot = switch (kind) {
			case HUNTER -> "camp_hunter";
			case WARLOCK -> "camp_warlock";
			case BANDIT -> "camp_bandit";
		};
		b.chest(-3, 0, 1, loot);
		b.chest(3, 0, -1, loot);

		switch (kind) {
			case HUNTER -> {
				b.set(0, 0, 4, Blocks.HAY_BLOCK.getDefaultState());
				b.set(1, 0, 4, Blocks.HAY_BLOCK.getDefaultState());
				b.set(-1, 0, -4, Blocks.TARGET.getDefaultState());
				b.boss(EntityType.PILLAGER, 1, 0, 2, dev.aptitudes.boss.BossKind.CAMP_CHAMPION);
				b.mob(EntityType.PILLAGER, -1, 0, -2);
			}
			case WARLOCK -> {
				b.set(0, 0, 4, Blocks.CRYING_OBSIDIAN.getDefaultState());
				b.set(0, 1, 4, Blocks.SOUL_LANTERN.getDefaultState());
				b.set(0, 0, -4, Blocks.BREWING_STAND.getDefaultState());
				b.boss(EntityType.WITCH, 1, 0, 2, dev.aptitudes.boss.BossKind.CAMP_CHAMPION);
				b.mob(EntityType.WITCH, -1, 0, -2);
			}
			case BANDIT -> {
				b.barrel(0, 0, 4, "camp_bandit");
				b.set(1, 0, 4, Blocks.BARREL.getDefaultState());
				b.boss(EntityType.VINDICATOR, 1, 0, 2, dev.aptitudes.boss.BossKind.CAMP_CHAMPION);
				b.mob(EntityType.VINDICATOR, -1, 0, -2);
				b.mob(EntityType.PILLAGER, 0, 0, -3);
			}
		}
		return true;
	}

	/** Tienda en forma de A de 5 de largo (eje z), centrada en x = {@code cx}, abierta por los extremos. */
	private static void tent(StructureBuilder b, int cx, BlockState a, BlockState c) {
		for (int z = -2; z <= 2; z++) {
			BlockState s = (z & 1) == 0 ? a : c;
			b.set(cx - 2, 0, z, s);
			b.set(cx + 2, 0, z, s);
			b.set(cx - 1, 1, z, s);
			b.set(cx + 1, 1, z, s);
			b.set(cx, 2, z, s);
		}
		b.set(cx, 0, -1, net.minecraft.block.Blocks.WHITE_CARPET.getDefaultState());
		b.set(cx, 0, 1, net.minecraft.block.Blocks.WHITE_CARPET.getDefaultState());
	}
}
