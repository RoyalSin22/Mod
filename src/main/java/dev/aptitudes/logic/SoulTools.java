package dev.aptitudes.logic;

import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.SoulToolItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Herramientas de alma (nivel diamante) y botín de Fragmentos de alma.
 * <ul>
 *   <li><b>Pico / Pala</b>: agachado, rompe un área de 3×3 perpendicular a la cara golpeada.</li>
 *   <li><b>Hacha</b>: agachado, tala el árbol entero (hasta 64 troncos).</li>
 *   <li><b>Azada</b>: al cosechar un cultivo maduro, cosecha y replanta los maduros en 3×3.</li>
 * </ul>
 */
public final class SoulTools {
	private SoulTools() {
	}

	public static void onBlockBroken(ServerPlayerEntity p, World world, BlockPos pos, BlockState state) {
		ItemStack tool = p.getMainHandStack();
		if (!(tool.getItem() instanceof SoulToolItem)) return;
		if (tool.isOf(ModItems.SOUL_AXE)) {
			if (p.isSneaking() && state.isIn(BlockTags.LOGS)) SkillMilestones.fellTree(p, world, pos, 64);
			return;
		}
		if (tool.isOf(ModItems.SOUL_HOE)) {
			if (state.getBlock() instanceof CropBlock crop && crop.isMature(state)) harvestArea(p, world, pos, state);
			return;
		}
		if (!p.isSneaking() || !tool.isSuitableFor(state)) return;
		Direction face = lookedFace(p);
		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				if (a == 0 && b == 0) continue;
				BlockPos target = switch (face.getAxis()) {
					case X -> pos.add(0, a, b);
					case Y -> pos.add(a, 0, b);
					case Z -> pos.add(a, b, 0);
				};
				BlockState other = world.getBlockState(target);
				if (other.isAir() || world.getBlockEntity(target) != null) continue;
				if (other.getHardness(world, target) < 0 || !tool.isSuitableFor(other)) continue;
				if (tool.isEmpty() || !(tool.getItem() instanceof SoulToolItem)) return;
				world.breakBlock(target, !p.isCreative(), p);
				tool.damage(1, p, EquipmentSlot.MAINHAND);
			}
		}
	}

	private static Direction lookedFace(ServerPlayerEntity p) {
		HitResult hit = p.raycast(6.0, 0f, false);
		if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) return block.getSide();
		return Direction.getEntityFacingOrder(p)[0].getOpposite();
	}

	private static void harvestArea(ServerPlayerEntity p, World world, BlockPos center, BlockState broken) {
		for (BlockPos target : BlockPos.iterate(center.add(-1, 0, -1), center.add(1, 0, 1))) {
			if (target.equals(center)) continue;
			BlockState other = world.getBlockState(target);
			if (!(other.getBlock() instanceof CropBlock crop) || !crop.isMature(other)) continue;
			BlockPos immutable = target.toImmutable();
			world.breakBlock(immutable, true, p);
			world.setBlockState(immutable, crop.getDefaultState());
		}
		// El cultivo central también se replanta (si no lo hizo ya el hito de Agricultura 20).
		if (world.getBlockState(center).isAir() && world.getBlockState(center.down()).isOf(net.minecraft.block.Blocks.FARMLAND)) {
			world.setBlockState(center, broken.getBlock().getDefaultState());
		}
	}

	/** Fragmentos de alma: 5 % de los monstruos que mata un jugador; los jefes sueltan 4. */
	public static void onKill(ServerPlayerEntity killer, LivingEntity victim) {
		EntityType<?> type = victim.getType();
		if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON || type == EntityType.WARDEN
				|| type == EntityType.ELDER_GUARDIAN) {
			victim.dropStack(new ItemStack(ModItems.SOUL_SHARD, 4));
		} else if (victim instanceof Monster) {
			float chance = 0.05f + 0.01f * (float) Math.max(0, killer.getLuck());
			if (killer.getRandom().nextFloat() < chance) victim.dropStack(new ItemStack(ModItems.SOUL_SHARD));
		}
	}
}
