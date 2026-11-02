package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Torre en ruinas de 4 pisos (7×7, octogonal). Cada piso tiene un cofre con botín
 * del mod; los pisos 2 y 3 tienen un generador de monstruos; en la azotea, entre
 * almenas, el cofre con mejor botín (armaduras de rol, herramientas de alma…).
 */
public class TowerFeature extends Feature<DefaultFeatureConfig> {
	private static final int FLOORS = 4;
	private static final int FLOOR_H = 5;

	public TowerFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		BlockPos origin = context.getOrigin();
		if (!StructureBuilder.suitable(context.getWorld(), origin, 3, 3)) return false;
		StructureBuilder b = new StructureBuilder(context.getWorld(), origin, context.getRandom());
		int top = FLOORS * FLOOR_H;

		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				boolean corner = Math.abs(x) == 3 && Math.abs(z) == 3;
				if (corner) continue;
				boolean wall = Math.abs(x) == 3 || Math.abs(z) == 3;
				b.foundation(x, z, Blocks.COBBLESTONE.getDefaultState());
				for (int y = 0; y <= top; y++) {
					if (wall) {
						b.set(x, y, z, b.bricks());
					} else if (y % FLOOR_H == 0) {
						b.set(x, y, z, Blocks.SPRUCE_PLANKS.getDefaultState()); // suelo de cada piso
					} else {
						b.air(x, y, z);
					}
				}
				// Almenas en la azotea
				if (wall && (x + z) % 2 == 0) b.set(x, top + 1, z, b.bricks());
				else if (wall) b.air(x, top + 1, z);
				else b.air(x, top + 1, z);
			}
		}
		// Puerta y ventanas (barrotes) en el centro de cada pared
		b.air(0, 1, -3);
		b.air(0, 2, -3);
		for (int floor = 0; floor < FLOORS; floor++) {
			int y = floor * FLOOR_H + 3;
			b.set(3, y, 0, Blocks.IRON_BARS.getDefaultState());
			b.set(-3, y, 0, Blocks.IRON_BARS.getDefaultState());
			if (floor > 0) b.set(0, y, -3, Blocks.IRON_BARS.getDefaultState());
			b.set(0, y, 3, Blocks.IRON_BARS.getDefaultState());
		}
		// Escalera de mano en la esquina (2, 2), pegada a la pared sur; huecos en los suelos.
		BlockState ladder = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.NORTH);
		for (int y = 1; y <= top; y++) b.set(2, y, 2, ladder);
		// Contenido de cada piso
		for (int floor = 0; floor < FLOORS; floor++) {
			int y = floor * FLOOR_H + 1;
			b.chest(-2, y, -2, floor == FLOORS - 1 ? "tower_upper" : "tower_floor");
			b.set(-2, y, 2, Blocks.LANTERN.getDefaultState());
			if (floor == 1) b.spawner(0, y, 0, b.random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON);
			if (floor == 2) b.spawner(0, y, 0, b.random.nextBoolean() ? EntityType.SPIDER : EntityType.SKELETON);
			if (floor >= 1 && b.random.nextFloat() < 0.5f) b.set(2, y, -2, Blocks.COBWEB.getDefaultState());
		}
		// Azotea: el mejor cofre
		b.chest(0, top + 1, 0, "tower_top");
		// Centinela de la torre: jefe de tres fases en la azotea.
		b.boss(EntityType.WITHER_SKELETON, 1, top + 1, 1, dev.aptitudes.boss.BossKind.TOWER_SENTINEL);
		return true;
	}
}
