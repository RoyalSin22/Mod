package dev.aptitudes.dungeon;

import com.mojang.serialization.MapCodec;
import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** V86 · fase 3: el bloque controlador de Las Tres Torres Señoriales y el aviso de muerte de sus jefes. */
public final class TowersBlocks {
	/** Etiquetas de los jefes: el controlador al que pertenecen (posición) y su papel. */
	public static final String CONTROLLER_TAG = Aptitudes.MOD_ID + ".towers:";
	public static final String ROLE_TAG = Aptitudes.MOD_ID + ".towers_role:";

	public static final Block CONTROLLER = Registry.register(Registries.BLOCK, Aptitudes.id("three_towers_controller"),
			new Controller(AbstractBlock.Settings.create().mapColor(MapColor.DEEPSLATE_GRAY).strength(-1f, 3_600_000f).dropsNothing()
					.pistonBehavior(PistonBehavior.BLOCK).sounds(BlockSoundGroup.DEEPSLATE_BRICKS).luminance(state -> 4)));

	public static final BlockEntityType<TowersControllerBlockEntity> CONTROLLER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			Aptitudes.id("three_towers_controller"), BlockEntityType.Builder.create(TowersControllerBlockEntity::new, CONTROLLER).build(null));

	private TowersBlocks() {}

	/** Fuerza la carga de la clase (y con ella el registro) y engancha la muerte de los jefes. */
	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob && mob.getWorld() instanceof ServerWorld world) onBossDeath(world, mob);
		});
	}

	private static void onBossDeath(ServerWorld world, MobEntity mob) {
		BlockPos controller = null;
		TowersLayout.Role role = null;
		for (String tag : mob.getCommandTags()) {
			try {
				if (tag.startsWith(CONTROLLER_TAG)) controller = BlockPos.fromLong(Long.parseLong(tag.substring(CONTROLLER_TAG.length())));
				else if (tag.startsWith(ROLE_TAG)) role = TowersLayout.Role.valueOf(tag.substring(ROLE_TAG.length()));
			} catch (IllegalArgumentException ignored) {
				// Etiqueta manipulada: se ignora.
			}
		}
		if (controller == null || role == null || !world.isChunkLoaded(controller)) return;
		if (world.getBlockEntity(controller) instanceof TowersControllerBlockEntity be) be.bossDefeated(world, role);
	}

	/** Bloque irrompible; su entidad lleva todo el estado de la estructura. */
	public static class Controller extends BlockWithEntity {
		public static final MapCodec<Controller> CODEC = createCodec(Controller::new);

		public Controller(Settings settings) {
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
			return new TowersControllerBlockEntity(pos, state);
		}

		@Nullable
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
			return world.isClient ? null : validateTicker(type, CONTROLLER_ENTITY, TowersControllerBlockEntity::tick);
		}
	}
}
