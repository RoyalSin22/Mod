package dev.aptitudes.temple;

import dev.aptitudes.temple.TempleFlowRules.Flow;
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
import java.util.function.Consumer;

/**
 * Corazón del Templo sellado (v64): guarda el puzle (palancas y placas), la puerta sellada y el guardián.
 * Todo se comprueba en el servidor cada medio segundo y solo con jugadores cerca; las posiciones se guardan
 * relativas al sello, así que el estado viaja con el chunk desde la generación.
 *
 * <p>T3: además del templo antiguo ({@code layout = 0}, sin cambios), controla el Templo del Custodio
 * ({@code layout = 1}) con las fases de {@link TempleFlowRules.Flow}: Autómata en el atrio, puzle, escalera,
 * Puerta del Custodio y Custodio. Los campos nuevos se guardan al final y un templo antiguo los lee como valores
 * por defecto.</p>
 */
public class TempleSealBlockEntity extends BlockEntity {
	private static final int CHECK_EVERY = 10;
	private static final double PLAYER_RANGE = 24;
	/** El Templo del Custodio mide 31×31 y tiene un subsuelo: el radio de actividad es mayor. */
	private static final double CUSTODIAN_RANGE = 32;

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

	// ---- T3: Templo del Custodio (layout 1) ----
	int layout = TempleFlowRules.LAYOUT_OLD;
	Flow flow = Flow.DORMANT;
	/** Bloques de la Puerta del Custodio, relativos al sello. */
	final List<BlockPos> innerDoor = new ArrayList<>();
	BlockPos automatonSpawn = BlockPos.ORIGIN;
	/** Centro del umbral del atrio (relativo al sello): el Autómata despierta al acercarse. */
	BlockPos triggerCenter = BlockPos.ORIGIN;
	@Nullable UUID automaton;
	private int missingAutomatonChecks;

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
		if (layout == TempleFlowRules.LAYOUT_CUSTODIAN) {
			serverTickCustodian(world);
			return;
		}
		List<ServerPlayerEntity> players = nearby(world, PLAYER_RANGE);
		if (players.isEmpty()) return;
		switch (stage) {
			case LEVERS -> leversTick(world, players, p -> awaken(world, p));
			case PLATES -> platesTick(world, players, p -> awaken(world, p));
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

	/** Palancas: al coincidir con el patrón pasa a las placas, o termina el puzle si no hay placas. */
	private void leversTick(ServerWorld world, List<ServerPlayerEntity> players, Consumer<List<ServerPlayerEntity>> solved) {
		boolean[] current = new boolean[levers.size()];
		for (int i = 0; i < levers.size(); i++) {
			BlockState s = world.getBlockState(pos.add(levers.get(i)));
			current[i] = s.isOf(Blocks.LEVER) && s.get(LeverBlock.POWERED);
		}
		if (TempleRules.leversSolved(current, target)) {
			tell(players, "temple.aptitudes.levers_solved");
			world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1f, 0.6f);
			if (plates.isEmpty()) solved.accept(players);
			else stage = Stage.PLATES;
			markDirty();
		}
	}

	/** Placas: se pisan en el orden de sus velas; al completarlas termina el puzle. */
	private void platesTick(ServerWorld world, List<ServerPlayerEntity> players, Consumer<List<ServerPlayerEntity>> solved) {
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
			if (plateProgress >= plates.size()) solved.accept(players);
			markDirty();
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

	// ------------------------------------------------------- Templo del Custodio (T3)

	private void serverTickCustodian(ServerWorld world) {
		List<ServerPlayerEntity> players = nearby(world, CUSTODIAN_RANGE);
		if (players.isEmpty()) return;
		switch (flow) {
			case DORMANT -> wakeAutomaton(world, players);
			case AUTOMATON -> {
				// Igual que los guardianes: si el Autómata no se encuentra, vuelve a aparecer (sin duplicarlo).
				if (Temples.automatonPresent(world, this)) {
					missingAutomatonChecks = 0;
				} else if (TempleFlowRules.shouldRespawn(++missingAutomatonChecks)) {
					missingAutomatonChecks = 0;
					automaton = Temples.spawnAutomaton(world, this);
					markDirty();
				}
			}
			case PUZZLE -> {
				if (!TempleFlowRules.puzzleActive(flow)) return;
				switch (stage) {
					case LEVERS -> leversTick(world, players, p -> openStairs(world, p));
					case PLATES -> platesTick(world, players, p -> openStairs(world, p));
					default -> { }
				}
			}
			case CUSTODIAN -> {
				// Igual que el guardián antiguo: si el Custodio desaparece sin morir, vuelve a aparecer.
				if (guardian != null && world.getEntity(guardian) != null) {
					missingChecks = 0;
				} else if (TempleFlowRules.shouldRespawn(++missingChecks)) {
					missingChecks = 0;
					guardian = Temples.spawnGuardian(world, this);
					markDirty();
				}
			}
			default -> { }
		}
	}

	/** Un jugador cruza el umbral del atrio: despierta el Autómata y el puzle queda bloqueado hasta que caiga. */
	private void wakeAutomaton(ServerWorld world, List<ServerPlayerEntity> players) {
		double cx = pos.getX() + triggerCenter.getX() + 0.5;
		double cy = pos.getY() + triggerCenter.getY();
		double cz = pos.getZ() + triggerCenter.getZ() + 0.5;
		boolean inside = false;
		for (ServerPlayerEntity p : players) {
			if (TempleFlowRules.insideTrigger(p.getX() - cx, p.getY() - cy, p.getZ() - cz)) {
				inside = true;
				break;
			}
		}
		if (!TempleFlowRules.shouldWake(flow, inside)) return;
		if (Temples.automatonPresent(world, this)) {
			// Ya existía (por ejemplo, tras una recarga): solo se retoma la fase.
			flow = Flow.AUTOMATON;
		} else {
			UUID spawned = Temples.spawnAutomaton(world, this);
			if (spawned == null) return;
			automaton = spawned;
			flow = Flow.AUTOMATON;
			tell(nearby(world, 40), "temple.aptitudes.automaton.woke");
		}
		missingAutomatonChecks = 0;
		markDirty();
	}

	/** Puzle resuelto: se abre la escalera (la puerta sellada) y queda la Puerta del Custodio. */
	private void openStairs(ServerWorld world, List<ServerPlayerEntity> players) {
		for (BlockPos rel : door) {
			BlockPos p = pos.add(rel);
			if (world.getBlockState(p).isOf(Temples.SEALED_STONE)) world.breakBlock(p, false);
		}
		world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.2f, 0.7f);
		flow = TempleFlowRules.afterPuzzleSolved(flow);
		tell(nearby(world, 40), "temple.aptitudes.stairs_open");
		markDirty();
	}

	/**
	 * Abre la Puerta del Custodio y despierta al Custodio. Lo llama {@link Temples#useCustodianDoor} después de
	 * comprobar el flujo y consumir el Ancla del Velo.
	 */
	void openCustodianDoor(ServerWorld world) {
		for (BlockPos rel : innerDoor) {
			BlockPos p = pos.add(rel);
			if (world.getBlockState(p).isOf(Temples.CUSTODIAN_DOOR)) world.breakBlock(p, false);
		}
		world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1.4f, 0.6f);
		flow = TempleFlowRules.afterDoorOpened(flow);
		missingChecks = 0;
		guardian = Temples.spawnGuardian(world, this);
		tell(nearby(world, 40), "temple.aptitudes.door.opened");
		markDirty();
	}

	/** Cae el Autómata del atrio: se desbloquea el puzle. */
	void automatonDefeated() {
		flow = TempleFlowRules.afterAutomatonDown(flow);
		automaton = null;
		missingAutomatonChecks = 0;
		markDirty();
	}

	public int layout() {
		return layout;
	}

	public Flow flow() {
		return flow;
	}

	/** ¿Esta posición absoluta es un bloque de la Puerta del Custodio de este templo? */
	public boolean ownsInnerDoor(BlockPos absolute) {
		return layout == TempleFlowRules.LAYOUT_CUSTODIAN && innerDoor.contains(absolute.subtract(pos));
	}

	/** Clave de texto del estado actual (para el mensaje del Sello). */
	String statusKey() {
		if (layout == TempleFlowRules.LAYOUT_CUSTODIAN && flow != Flow.PUZZLE) {
			return "temple.aptitudes.seal.flow_" + flow.name().toLowerCase(java.util.Locale.ROOT);
		}
		return "temple.aptitudes.seal." + stage.name().toLowerCase(java.util.Locale.ROOT);
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
		this.layout = TempleFlowRules.LAYOUT_OLD;
		markDirty();
	}

	/** La llama la generación del Templo del Custodio: igual que {@link #setup} más lo propio de la distribución nueva. */
	public void setupCustodian(UUID temple, int ring, List<BlockPos> levers, boolean[] target, List<BlockPos> plates,
							   List<BlockPos> stairsDoor, BlockPos guardianSpawn, List<BlockPos> custodianDoor,
							   BlockPos automatonSpawn, BlockPos triggerCenter) {
		setup(temple, ring, levers, target, plates, stairsDoor, guardianSpawn);
		this.layout = TempleFlowRules.LAYOUT_CUSTODIAN;
		this.flow = Flow.DORMANT;
		this.innerDoor.clear();
		this.innerDoor.addAll(custodianDoor);
		this.automatonSpawn = automatonSpawn;
		this.triggerCenter = triggerCenter;
		this.automaton = null;
		markDirty();
	}

	/** Lo llama el gestor al morir el guardián (si este chunk está cargado). */
	void cleared() {
		stage = Stage.CLEARED;
		if (layout == TempleFlowRules.LAYOUT_CUSTODIAN) flow = TempleFlowRules.afterCustodianDown(flow);
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
		// T3 (al final: un templo antiguo no tiene estas claves).
		nbt.putInt("layout", layout);
		nbt.putInt("flow", flow.ordinal());
		nbt.putLongArray("inner_door", encode(innerDoor));
		nbt.putLong("automaton_spawn", automatonSpawn.asLong());
		nbt.putLong("trigger_center", triggerCenter.asLong());
		if (automaton != null) nbt.putUuid("automaton", automaton);
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
		// T3: un templo antiguo no tiene estas claves y se lee como distribución 0.
		layout = TempleFlowRules.clampLayout(nbt.getInt("layout"));
		flow = TempleFlowRules.flowFromOrdinal(nbt.getInt("flow"));
		decode(nbt.getLongArray("inner_door"), innerDoor, 64);
		automatonSpawn = BlockPos.fromLong(nbt.getLong("automaton_spawn"));
		triggerCenter = BlockPos.fromLong(nbt.getLong("trigger_center"));
		automaton = nbt.containsUuid("automaton") ? nbt.getUuid("automaton") : null;
	}
}
