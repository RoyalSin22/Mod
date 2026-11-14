package dev.aptitudes.temple;

import com.mojang.serialization.MapCodec;
import dev.aptitudes.achievement.Achievement;
import dev.aptitudes.achievement.Achievements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Bloques con entidad del templo: el Sello (puzle) y el Relicario (recompensa). Ambos son irrompibles. */
public final class TempleBlocks {
	private TempleBlocks() {
	}

	public static class Seal extends BlockWithEntity {
		public static final MapCodec<Seal> CODEC = createCodec(Seal::new);

		public Seal(Settings settings) {
			super(settings);
		}

		@Override
		protected MapCodec<? extends BlockWithEntity> getCodec() {
			return CODEC;
		}

		@Override
		protected BlockRenderType getRenderType(BlockState state) {
			return BlockRenderType.MODEL;
		}

		@Nullable
		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new TempleSealBlockEntity(pos, state);
		}

		@Nullable
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
			return world.isClient ? null : validateTicker(type, Temples.SEAL_ENTITY, TempleSealBlockEntity::tick);
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			if (!world.isClient && world.getBlockEntity(pos) instanceof TempleSealBlockEntity seal) {
				player.sendMessage(Text.translatable(seal.statusKey()).formatted(Formatting.GRAY), true);
			}
			return ActionResult.success(world.isClient);
		}
	}

	/** T3: Puerta del Custodio. Con el Ancla del Velo en la mano se abre (y se consume); sin ella, avisa. */
	public static class CustodianDoor extends Block {
		public static final MapCodec<CustodianDoor> CODEC = createCodec(CustodianDoor::new);

		public CustodianDoor(Settings settings) {
			super(settings);
		}

		@Override
		protected MapCodec<? extends Block> getCodec() {
			return CODEC;
		}

		@Override
		protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
												 Hand hand, BlockHitResult hit) {
			if (!stack.isOf(dev.aptitudes.item.ModItems.ANCLA_DEL_VELO)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (world.isClient) return ItemActionResult.SUCCESS;
			if (world instanceof ServerWorld server && player instanceof ServerPlayerEntity sp) Temples.useCustodianDoor(server, pos, sp, stack);
			return ItemActionResult.CONSUME;
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			if (!world.isClient && world instanceof ServerWorld server && player instanceof ServerPlayerEntity sp) {
				Temples.useCustodianDoor(server, pos, sp, ItemStack.EMPTY);
			}
			return ActionResult.success(world.isClient);
		}
	}

	/**
	 * T4: puerta de una cámara de recompensa. Solo la abre la llave del Custodio de su metal y de su templo, y solo con el
	 * Custodio vencido. Cada puerta es un bloque distinto por metal.
	 */
	public static class ChamberDoor extends Block {
		private final TempleKeyRules.Metal metal;

		public ChamberDoor(Settings settings, TempleKeyRules.Metal metal) {
			super(settings);
			this.metal = metal;
		}

		public TempleKeyRules.Metal metal() {
			return metal;
		}

		@Override
		protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
												 Hand hand, BlockHitResult hit) {
			if (!(stack.getItem() instanceof CustodianKeyItem)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (world.isClient) return ItemActionResult.SUCCESS;
			if (world instanceof ServerWorld server && player instanceof ServerPlayerEntity sp) Temples.useChamberDoor(server, pos, sp, stack, metal);
			return ItemActionResult.CONSUME;
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			if (!world.isClient && world instanceof ServerWorld server && player instanceof ServerPlayerEntity sp) {
				Temples.useChamberDoor(server, pos, sp, ItemStack.EMPTY, metal);
			}
			return ActionResult.success(world.isClient);
		}
	}

	public static class Reliquary extends BlockWithEntity {
		public static final MapCodec<Reliquary> CODEC = createCodec(Reliquary::new);

		public Reliquary(Settings settings) {
			super(settings);
		}

		@Override
		protected MapCodec<? extends BlockWithEntity> getCodec() {
			return CODEC;
		}

		@Override
		protected BlockRenderType getRenderType(BlockState state) {
			return BlockRenderType.MODEL;
		}

		@Nullable
		@Override
		public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
			return new ReliquaryBlockEntity(pos, state);
		}

		/** Con la llave en la mano: abrir. Sin ella, se pasa a {@link #onUse}. */
		@Override
		protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player,
												 Hand hand, BlockHitResult hit) {
			if (!stack.isOf(Temples.TEMPLE_KEY)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (world.isClient) return ItemActionResult.SUCCESS;
			if (!(world.getBlockEntity(pos) instanceof ReliquaryBlockEntity reliquary) || !(player instanceof ServerPlayerEntity sp)) {
				return ItemActionResult.CONSUME;
			}
			if (reliquary.unlocked) {
				claim(sp, reliquary);
			} else if (reliquary.templeId.equals(TempleKeyItem.templeOf(stack))) {
				reliquary.unlocked = true;
				reliquary.markDirty();
				stack.decrementUnlessCreative(1, player);
				world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1f, 0.9f);
				sp.sendMessage(Text.translatable("temple.aptitudes.reliquary.opened").formatted(Formatting.GOLD), true);
				Achievements.grant(sp, Achievement.TEMPLE_OPENED);
				claim(sp, reliquary);
			} else {
				sp.sendMessage(Text.translatable("temple.aptitudes.reliquary.wrong_key").formatted(Formatting.RED), true);
			}
			return ItemActionResult.CONSUME;
		}

		@Override
		protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
			if (world.isClient) return ActionResult.SUCCESS;
			if (world.getBlockEntity(pos) instanceof ReliquaryBlockEntity reliquary && player instanceof ServerPlayerEntity sp) {
				if (reliquary.unlocked) claim(sp, reliquary);
				else sp.sendMessage(Text.translatable("temple.aptitudes.reliquary.sealed").formatted(Formatting.GRAY), true);
			}
			return ActionResult.CONSUME;
		}

		/** Un vaso por jugador y relicario; el registro se guarda antes de entregar el objeto. */
		private static void claim(ServerPlayerEntity player, ReliquaryBlockEntity reliquary) {
			if (reliquary.claimed.contains(player.getUuid())) {
				player.sendMessage(Text.translatable("temple.aptitudes.reliquary.claimed").formatted(Formatting.GRAY), true);
				return;
			}
			reliquary.claimed.add(player.getUuid());
			reliquary.markDirty();
			ItemStack vessel = LifeVesselItem.create();
			if (!player.getInventory().insertStack(vessel)) player.dropItem(vessel, false);
			player.getServerWorld().playSound(null, reliquary.getPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
					SoundCategory.BLOCKS, 1.2f, 1.1f);
			player.sendMessage(Text.translatable("temple.aptitudes.reliquary.vessel").formatted(Formatting.LIGHT_PURPLE), true);
		}
	}
}
