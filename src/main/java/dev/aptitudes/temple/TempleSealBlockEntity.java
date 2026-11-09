package dev.aptitudes.temple;

import dev.aptitudes.temple.TempleRules.Stage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Corazón del Templo sellado (v64): guarda el puzle (palancas y placas), la puerta sellada y el guardián.
 * Todo se comprueba en el servidor cada medio segundo y solo con jugadores cerca; las posiciones se guardan
 * relativas al sello, así que el estado viaja con el chunk desde la generación.
 */
public class TempleSealBlockEntity extends BlockEntity {
	private static final int CHECK_EVERY = 10;
	private static final double PLAYER_RANGE = 24;

	UUID templeId = new UUID(0, 0);
	int ring;
	Stage stage = Stage.LEVERS;
	final List<BlockPos> levers = new ArrayList<>();
	boolean[] target = new boolean[0];
	final List<BlockPos> plates = new ArrayList<>();
	int plateProgress;
	final List<BlockPos> door = new ArrayList<>();
	BlockPos guardianSpawn = BlockPos.ORIGIN;
	@Nullable UUID guardian;
	private int missingChecks;

	public TempleSealBlockEntity(BlockPos pos, BlockState state) {
		super(Temples.SEAL_ENTITY, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, TempleSealBlockEntity seal) {
		if (world instanceof ServerWorld server && server.getTime() % CHECK_EVERY == 0) seal.serverTick(server);
	}

	private List<ServerPlayerEntity> nearby(ServerWorld world, double range) {
		List<ServerPlayerEntity> out = new ArrayList<>();
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (!p.isSpectator() && p.squaredDistanceTo(pos.toCenterPos()) <= range * range) out.add(p);
		}
		return out;
	}

	private void serverTick(ServerWorld world) {
		if (stage == Stage.CLEARED) return;
		List<ServerPlayerEntity> players = nearby(world, PLAYER_RANGE);
		if (players.isEmpty()) return;
		switch (stage) {
			case LEVERS -> {
				boolean[] current = new boolean[levers.size()];
				for (int i = 0; i < levers.size(); i++) {
					BlockState s = world.getBlockState(pos.add(levers.get(i)));
					current[i] = s.isOf(Blocks.LEVER) && s.get(LeverBlock.POWERED);
				}
				if (TempleRules.leversSolved(current, target)) {
					tell(players, "temple.aptitudes.levers_solved");
					world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1f, 0.6f);
					if (plates.isEmpty()) awaken(world, players);
					else stage = Stage.PLATES;
					markDirty();
				}
			}
			case PLATES -> {
				boolean[] pressed = new boolean[plates.size()];
				for (int i = 0; i < plates.size(); i++) {
					BlockPos plate = pos.add(plates.get(i));
					for (ServerPlayerEntity p : players) if (p.getBlockPos().equals(plate)) pressed[i] = true;
				}
				int next = TempleRules.plateProgress(plateProgress, pressed);
				if (next > plateProgress) {
					world.playSound(null, pos.add(plates.get(next - 1)), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(),
							SoundCategory.BLOCKS, 1f, 0.8f + 0.2f * next);
				} else if (next == 0 && plateProgress > 0) {
					world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1f, 0.5f);
					tell(players, "temple.aptitudes.plates_reset");
				}
				if (next != plateProgress) {
					plateProgress = next;
					if (plateProgress >= plates.size()) awaken(world, players);
					markDirty();
				}
			}
			case AWAKENED -> {
				// Si el guardián desaparece sin morir (descarga o un fallo), vuelve a aparecer al cabo de unos segundos.
				if (guardian != null && world.getEntity(guardian) != null) {
					missingChecks = 0;
				} else if (++missingChecks >= 6) {
					missingChecks = 0;
					guardian = Temples.spawnGuardian(world, this);
					markDirty();
				}
			}
			default -> { }
		}
	}

	private void awaken(ServerWorld world, List<ServerPlayerEntity> players) {
		for (BlockPos rel : door) {
			BlockPos p = pos.add(rel);
			if (world.getBlockState(p).isOf(Temples.SEALED_STONE)) world.breakBlock(p, false);
		}
		world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.2f, 0.7f);
		stage = Stage.AWAKENED;
		guardian = Temples.spawnGuardian(world, this);
		tell(nearby(world, 40), "temple.aptitudes.awakened");
	}

	/** La llama la generación del templo; todas las posiciones son relativas al sello. */
	public void setup(UUID temple, int ring, List<BlockPos> levers, boolean[] target, List<BlockPos> plates,
					  List<BlockPos> door, BlockPos guardianSpawn) {
		this.templeId = temple;
		this.ring = TempleRules.clampRing(ring);
		this.levers.clear();
		this.levers.addAll(levers);
		this.target = target.clone();
		this.plates.clear();
		this.plates.addAll(plates);
		this.door.clear();
		this.door.addAll(door);
		this.guardianSpawn = guardianSpawn;
		this.stage = Stage.LEVERS;
		this.plateProgress = 0;
		markDirty();
	}

	/** Lo llama el gestor al morir el guardián (si este chunk está cargado). */
	void cleared() {
		stage = Stage.CLEARED;
		guardian = null;
		markDirty();
	}

	private static void tell(List<ServerPlayerEntity> players, String key) {
		for (ServerPlayerEntity p : players) p.sendMessage(Text.translatable(key).formatted(Formatting.GOLD), true);
	}

	// ----------------------------------------------------------------- Guardado

	private static long[] encode(List<BlockPos> list) {
		long[] out = new long[list.size()];
		for (int i = 0; i < out.length; i++) out[i] = list.get(i).asLong();
		return out;
	}

	private static void decode(long[] raw, List<BlockPos> into, int max) {
		into.clear();
		for (int i = 0; i < raw.length && i < max; i++) into.add(BlockPos.fromLong(raw[i]));
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putUuid("temple", templeId);
		nbt.putInt("ring", ring);
		nbt.putInt("stage", stage.ordinal());
		nbt.putLongArray("levers", encode(levers));
		byte[] t = new byte[target.length];
		for (int i = 0; i < t.length; i++) t[i] = (byte) (target[i] ? 1 : 0);
		nbt.putByteArray("target", t);
		nbt.putLongArray("plates", encode(plates));
		nbt.putInt("plate_progress", plateProgress);
		nbt.putLongArray("door", encode(door));
		nbt.putLong("guardian_spawn", guardianSpawn.asLong());
		if (guardian != null) nbt.putUuid("guardian", guardian);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		if (nbt.containsUuid("temple")) templeId = nbt.getUuid("temple");
		ring = TempleRules.clampRing(nbt.getInt("ring"));
		Stage[] stages = Stage.values();
		stage = stages[Math.max(0, Math.min(stages.length - 1, nbt.getInt("stage")))];
		decode(nbt.getLongArray("levers"), levers, TempleRules.MAX_LEVERS);
		byte[] t = nbt.getByteArray("target");
		target = new boolean[Math.min(t.length, levers.size())];
		for (int i = 0; i < target.length; i++) target[i] = t[i] != 0;
		decode(nbt.getLongArray("plates"), plates, TempleRules.MAX_PLATES);
		plateProgress = Math.max(0, Math.min(plates.size(), nbt.getInt("plate_progress")));
		decode(nbt.getLongArray("door"), door, 64);
		guardianSpawn = BlockPos.fromLong(nbt.getLong("guardian_spawn"));
		guardian = nbt.containsUuid("guardian") ? nbt.getUuid("guardian") : null;
	}
}
