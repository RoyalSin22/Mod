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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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
	/** T4: celdas de las puertas de las cámaras (relativas al sello), con el metal de cada una, y las cámaras ya abiertas. */
	final List<BlockPos> chamberDoors = new ArrayList<>();
	final List<TempleKeyRules.Metal> chamberMetals = new ArrayList<>();
	int chambersOpen;
	// V69: Cripta de la Niebla (etapa interna opcional; solo con layout 2). Todo se guarda al final del NBT.
	CryptRules.Stage crypt = CryptRules.Stage.LOCKED;
	/** Celdas de la trampilla, puntos de aparición y esquinas de la sala (relativos al sello). */
	final List<BlockPos> cryptHatch = new ArrayList<>();
	final List<BlockPos> cryptSpawns = new ArrayList<>();
	BlockPos cryptMin = BlockPos.ORIGIN, cryptMax = BlockPos.ORIGIN, cryptClimax = BlockPos.ORIGIN, cryptChest = BlockPos.ORIGIN;
	/** Estado de la oleada en curso: solo en memoria (tras un reinicio del servidor la oleada empieza de nuevo). */
	private final java.util.Set<UUID> cryptMobs = new java.util.HashSet<>();
	private int cryptRemaining;
	private boolean cryptWaveLive;
	private long cryptIdleTicks;

	// V70: Templo hondo (layout 3 y 4). Todo al final del NBT.
	/** Piedra sellada de la escalera del Piso 1 al 2 y puntos y esquinas de la sala de pruebas (relativos al sello). */
	final List<BlockPos> gateOne = new ArrayList<>();
	final List<BlockPos> trialSpawns = new ArrayList<>();
	BlockPos trialMin = BlockPos.ORIGIN, trialMax = BlockPos.ORIGIN;
	/** Oleada actual de la sala de pruebas (1..{@link TempleFlowRules#TRIAL_WAVES}); 0 = aún sin empezar. Se guarda. */
	int trialWave;
	private final java.util.Set<UUID> trialMobs = new java.util.HashSet<>();
	private int trialRemaining;
	private boolean trialLive;
	private long trialIdleTicks;

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
		if (stage == Stage.CLEARED) {
			if (hasCrypt()) serverTickCrypt(world);
			return;
		}
		if (layout >= TempleFlowRules.LAYOUT_CUSTODIAN) {
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
			case TRIAL -> serverTickTrial(world);
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
	void automatonDefeated(ServerWorld world) {
		flow = TempleFlowRules.afterAutomatonDown(flow, layout);
		if (flow == Flow.TRIAL) {
			breakSealed(world, gateOne);
			trialWave = 0;
			world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.2f, 0.7f);
			tell(nearby(world, 40), "temple.aptitudes.trial.gate_open");
		}
		automaton = null;
		missingAutomatonChecks = 0;
		markDirty();
	}

	/** Rompe la piedra sellada de unas posiciones relativas al sello. */
	private void breakSealed(ServerWorld world, List<BlockPos> rels) {
		for (BlockPos rel : rels) {
			BlockPos p = pos.add(rel);
			if (world.getBlockState(p).isOf(Temples.SEALED_STONE)) world.breakBlock(p, false);
		}
	}

	private Box trialRoom() {
		return new Box(Vec3d.of(pos.add(trialMin)), Vec3d.of(pos.add(trialMax).add(1, 1, 1)));
	}

	/**
	 * V70: sala de pruebas del Piso 2. Con jugadores dentro salen las oleadas (guardianes sin botín); al vencerlas se abre la
	 * escalera al Piso 3. Sin jugadores vivos en la sala, la oleada en curso se reinicia al cabo de {@code cryptIdleResetSeconds}.
	 */
	private void serverTickTrial(ServerWorld world) {
		if (trialSpawns.isEmpty()) return;
		Box room = trialRoom();
		List<ServerPlayerEntity> inside = world.getPlayers(p -> p.isAlive() && !p.isSpectator() && room.contains(p.getPos()));
		if (!trialLive) {
			if (inside.isEmpty()) return;
			if (trialWave < 1) trialWave = 1;
			startTrialWave(world, inside, room);
			return;
		}
		if (inside.isEmpty()) {
			trialIdleTicks += CHECK_EVERY;
			if (CryptRules.shouldResetWave(trialIdleTicks, dev.aptitudes.Aptitudes.config().cryptIdleResetSeconds)) {
				CryptWaves.discardAll(world, room);
				trialMobs.clear();
				trialLive = false;
				trialIdleTicks = 0;
			}
			return;
		}
		trialIdleTicks = 0;
		trialMobs.removeIf(id -> {
			net.minecraft.entity.Entity e = world.getEntity(id);
			return e == null || !e.isAlive();
		});
		int toSpawn = CryptRules.toSpawn(trialRemaining, trialMobs.size());
		for (int i = 0; i < toSpawn; i++) {
			BlockPos spot = pos.add(trialSpawns.get(world.random.nextInt(trialSpawns.size())));
			net.minecraft.entity.mob.MobEntity mob = CryptWaves.spawn(world, spot, CryptRules.Variant.VANILLA,
					inside.get(world.random.nextInt(inside.size())));
			if (mob == null) continue;
			trialMobs.add(mob.getUuid());
			trialRemaining--;
		}
		if (!CryptRules.waveCleared(trialRemaining, trialMobs.size())) return;
		trialLive = false;
		if (trialWave >= TempleFlowRules.TRIAL_WAVES) {
			breakSealed(world, door);
			flow = TempleFlowRules.afterTrialCleared(flow);
			world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.2f, 0.7f);
			tell(inside, "temple.aptitudes.trial.cleared");
		} else {
			trialWave++;
			tell(inside, "temple.aptitudes.trial.next_wave");
		}
		markDirty();
	}

	private void startTrialWave(ServerWorld world, List<ServerPlayerEntity> inside, Box room) {
		CryptWaves.discardAll(world, room);
		trialMobs.clear();
		trialLive = true;
		trialIdleTicks = 0;
		trialRemaining = CryptRules.waveSize(trialWave, inside.size(), ring, dev.aptitudes.Aptitudes.config().cryptWaveSizePercent);
		tell(inside, "temple.aptitudes.trial.wave_" + Math.min(trialWave, TempleFlowRules.TRIAL_WAVES));
	}

	/** La llama la generación del templo hondo (después de {@link #setupCustodian}). Posiciones relativas al sello. */
	public void setupDeep(boolean withCrypt, List<BlockPos> gateOne, List<BlockPos> trialSpawns, BlockPos trialMin, BlockPos trialMax) {
		this.layout = withCrypt ? TempleFlowRules.LAYOUT_DEEP_CRYPT : TempleFlowRules.LAYOUT_DEEP;
		this.gateOne.clear();
		this.gateOne.addAll(gateOne);
		this.trialSpawns.clear();
		this.trialSpawns.addAll(trialSpawns);
		this.trialMin = trialMin;
		this.trialMax = trialMax;
		this.trialWave = 0;
		markDirty();
	}

	/** ¿Esta posición absoluta es un bloque de alguna puerta (la del Custodio o la de una cámara) de este templo? */
	public boolean ownsDoor(BlockPos absolute) {
		return ownsInnerDoor(absolute) || chamberMetalAt(absolute) != null || ownsCryptHatch(absolute);
	}

	/** Metal de la puerta de cámara en esa posición absoluta, o {@code null} si no es de este templo. */
	@Nullable
	public TempleKeyRules.Metal chamberMetalAt(BlockPos absolute) {
		if (!TempleFlowRules.isCustodian(layout)) return null;
		int i = chamberDoors.indexOf(absolute.subtract(pos));
		return i < 0 || i >= chamberMetals.size() ? null : chamberMetals.get(i);
	}

	/** Abre la cámara de ese metal: quita sus bloques de puerta. Lo llama {@link Temples#useChamberDoor} tras gastar la llave. */
	void openChamber(ServerWorld world, TempleKeyRules.Metal metal) {
		for (int i = 0; i < chamberDoors.size() && i < chamberMetals.size(); i++) {
			if (chamberMetals.get(i) != metal) continue;
			BlockPos p = pos.add(chamberDoors.get(i));
			if (world.getBlockState(p).getBlock() == Temples.chamberDoorBlock(metal)) world.breakBlock(p, false);
		}
		chambersOpen = TempleKeyRules.withOpened(chambersOpen, metal);
		world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1.2f, 0.9f);
		tell(nearby(world, 40), "temple.aptitudes.chamber.opened");
		markDirty();
	}

	/** La llama la generación: puertas de cámara, relativas al sello, con su metal. */
	public void setupChambers(List<BlockPos> doors, List<TempleKeyRules.Metal> metals) {
		this.chamberDoors.clear();
		this.chamberDoors.addAll(doors);
		this.chamberMetals.clear();
		this.chamberMetals.addAll(metals);
		this.chambersOpen = 0;
		markDirty();
	}

	// ------------------------------------------------------- Cripta de la Niebla (V69)

	public boolean hasCrypt() {
		return TempleFlowRules.hasCrypt(layout);
	}

	public CryptRules.Stage cryptStage() {
		return crypt;
	}

	/** ¿Esta posición absoluta es un bloque de la trampilla de la cripta de este templo? */
	public boolean ownsCryptHatch(BlockPos absolute) {
		return hasCrypt() && cryptHatch.contains(absolute.subtract(pos));
	}

	/** La llama la generación de un templo con cripta; todas las posiciones son relativas al sello. */
	public void setupCrypt(List<BlockPos> hatch, List<BlockPos> spawns, BlockPos min, BlockPos max, BlockPos climax, BlockPos chest) {
		this.layout = TempleFlowRules.isDeep(layout) ? TempleFlowRules.LAYOUT_DEEP_CRYPT : TempleFlowRules.LAYOUT_CRYPT;
		this.crypt = CryptRules.Stage.LOCKED;
		this.cryptHatch.clear();
		this.cryptHatch.addAll(hatch);
		this.cryptSpawns.clear();
		this.cryptSpawns.addAll(spawns);
		this.cryptMin = min;
		this.cryptMax = max;
		this.cryptClimax = climax;
		this.cryptChest = chest;
		markDirty();
	}

	/** Abre la trampilla. Lo llama {@link Temples#useCryptHatch} tras comprobar el estado y gastar la Llave del Velo. */
	void openCrypt(ServerWorld world) {
		for (BlockPos rel : cryptHatch) {
			BlockPos p = pos.add(rel);
			if (world.getBlockState(p).isOf(Temples.CRYPT_HATCH)) world.breakBlock(p, false);
		}
		world.playSound(null, pos.add(cryptHatch.isEmpty() ? BlockPos.ORIGIN : cryptHatch.get(0)), SoundEvents.BLOCK_VAULT_OPEN_SHUTTER,
				SoundCategory.BLOCKS, 1.4f, 0.5f);
		crypt = CryptRules.afterKeyUsed(crypt);
		tell(nearby(world, 40), "temple.aptitudes.crypt.opened");
		markDirty();
	}

	private Box cryptRoom() {
		return new Box(Vec3d.of(pos.add(cryptMin)), Vec3d.of(pos.add(cryptMax).add(1, 1, 1)));
	}

	private List<ServerPlayerEntity> playersInCrypt(ServerWorld world) {
		Box room = cryptRoom();
		return world.getPlayers(p -> p.isAlive() && !p.isSpectator() && room.contains(p.getPos()));
	}

	/** Oleadas y clímax. Corre cada {@code CHECK_EVERY} ticks una vez el templo está limpio. */
	private void serverTickCrypt(ServerWorld world) {
		if (crypt != CryptRules.Stage.OPEN && !CryptRules.isFighting(crypt)) return;
		List<ServerPlayerEntity> inside = playersInCrypt(world);
		if (crypt == CryptRules.Stage.OPEN) {
			if (inside.isEmpty()) return;
			crypt = CryptRules.afterPlayerEntered(crypt);
			markDirty();
			startWave(world, inside);
			return;
		}
		if (inside.isEmpty()) {
			// Sin nadie vivo en la sala: tras el tiempo configurado, la oleada en curso empieza de nuevo (la llave no se gasta).
			cryptIdleTicks += CHECK_EVERY;
			if (CryptRules.countsIdle(crypt) && CryptRules.shouldResetWave(cryptIdleTicks, dev.aptitudes.Aptitudes.config().cryptIdleResetSeconds)) {
				CryptWaves.discardAll(world, cryptRoom());
				cryptMobs.clear();
				cryptWaveLive = false;
				cryptIdleTicks = 0;
			}
			return;
		}
		cryptIdleTicks = 0;
		if (!cryptWaveLive) {
			startWave(world, inside);
			return;
		}
		cryptMobs.removeIf(id -> {
			net.minecraft.entity.Entity e = world.getEntity(id);
			return e == null || !e.isAlive();
		});
		int toSpawn = CryptRules.toSpawn(cryptRemaining, cryptMobs.size());
		for (int i = 0; i < toSpawn; i++) {
			if (spawnWaveMob(world, inside)) cryptRemaining--;
		}
		if (!CryptRules.waveCleared(cryptRemaining, cryptMobs.size())) return;
		if (crypt == CryptRules.Stage.CLIMAX) {
			finishCrypt(world, inside);
		} else {
			crypt = CryptRules.afterWaveCleared(crypt);
			cryptWaveLive = false;
			markDirty();
			tell(inside, crypt == CryptRules.Stage.CLIMAX ? "temple.aptitudes.crypt.climax" : "temple.aptitudes.crypt.next_wave");
		}
	}

	/** Empieza (o reinicia) la oleada de la fase actual: retira restos y prepara lo que va a salir. */
	private void startWave(ServerWorld world, List<ServerPlayerEntity> inside) {
		CryptWaves.discardAll(world, cryptRoom());
		cryptMobs.clear();
		cryptWaveLive = true;
		cryptIdleTicks = 0;
		if (crypt == CryptRules.Stage.CLIMAX) {
			cryptRemaining = 0;
			net.minecraft.entity.mob.MobEntity champion = CryptWaves.spawnChampion(world, pos.add(cryptClimax), ring, inside.get(0));
			if (champion != null) cryptMobs.add(champion.getUuid());
			for (int i = 0; i < CryptRules.ESCORTS; i++) {
				net.minecraft.entity.mob.MobEntity escort = CryptWaves.spawn(world, spawnSpot(world), CryptRules.Variant.ERRANTE,
						inside.get(world.random.nextInt(inside.size())));
				if (escort != null) cryptMobs.add(escort.getUuid());
			}
			if (cryptMobs.isEmpty()) cryptWaveLive = false; // sin sitio: se reintenta en el siguiente control
		} else {
			cryptRemaining = CryptRules.waveSize(CryptRules.waveIndex(crypt), inside.size(), ring,
					dev.aptitudes.Aptitudes.config().cryptWaveSizePercent);
			tell(inside, "temple.aptitudes.crypt.wave_" + CryptRules.waveIndex(crypt));
		}
	}

	private BlockPos spawnSpot(ServerWorld world) {
		return pos.add(cryptSpawns.get(world.random.nextInt(cryptSpawns.size())));
	}

	private boolean spawnWaveMob(ServerWorld world, List<ServerPlayerEntity> inside) {
		if (cryptSpawns.isEmpty()) return false;
		net.minecraft.entity.mob.MobEntity mob = CryptWaves.spawn(world, spawnSpot(world),
				CryptRules.choose(world.random.nextInt(100)), inside.get(world.random.nextInt(inside.size())));
		if (mob == null) return false;
		cryptMobs.add(mob.getUuid());
		return true;
	}

	/** Cae el clímax: la cripta queda superada, emerge el cofre y se desbloquea el lore. */
	private void finishCrypt(ServerWorld world, List<ServerPlayerEntity> inside) {
		crypt = CryptRules.afterClimaxDown(crypt);
		cryptWaveLive = false;
		cryptMobs.clear();
		CryptWaves.discardAll(world, cryptRoom());
		BlockPos chest = pos.add(cryptChest);
		CryptWaves.placeChest(world, chest);
		world.playSound(null, chest, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1.4f, 0.8f);
		world.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL, chest.getX() + 0.5, chest.getY() + 1, chest.getZ() + 0.5, 40, 0.5, 0.6, 0.5, 0.05);
		tell(inside, "temple.aptitudes.crypt.done");
		for (ServerPlayerEntity p : inside) dev.aptitudes.guide.LoreManager.unlocksOnCryptCleared(p);
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
		return TempleFlowRules.isCustodian(layout) && innerDoor.contains(absolute.subtract(pos));
	}

	/** Clave de texto del estado actual (para el mensaje del Sello). */
	String statusKey() {
		if (TempleFlowRules.isDeep(layout) && flow == Flow.AUTOMATON) return "temple.aptitudes.seal.flow_automaton_deep";
		if (TempleFlowRules.isCustodian(layout) && flow != Flow.PUZZLE) {
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
		if (TempleFlowRules.isCustodian(layout)) flow = TempleFlowRules.afterCustodianDown(flow);
		if (hasCrypt()) crypt = CryptRules.afterCustodianCleared(crypt);
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
		// T4
		nbt.putLongArray("chamber_doors", encode(chamberDoors));
		byte[] metals = new byte[chamberMetals.size()];
		for (int i = 0; i < metals.length; i++) metals[i] = (byte) chamberMetals.get(i).ordinal();
		nbt.putByteArray("chamber_metals", metals);
		nbt.putInt("chambers_open", chambersOpen);
		// V69: un templo anterior no tiene estas claves y se lee con la cripta bloqueada y sin trampilla.
		nbt.putInt("crypt", crypt.ordinal());
		nbt.putLongArray("crypt_hatch", encode(cryptHatch));
		nbt.putLongArray("crypt_spawns", encode(cryptSpawns));
		nbt.putLong("crypt_min", cryptMin.asLong());
		nbt.putLong("crypt_max", cryptMax.asLong());
		nbt.putLong("crypt_climax", cryptClimax.asLong());
		nbt.putLong("crypt_chest", cryptChest.asLong());
		// V70
		nbt.putLongArray("gate_one", encode(gateOne));
		nbt.putLongArray("trial_spawns", encode(trialSpawns));
		nbt.putLong("trial_min", trialMin.asLong());
		nbt.putLong("trial_max", trialMax.asLong());
		nbt.putInt("trial_wave", trialWave);
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
		// T4: un templo sin estas claves se lee sin cámaras.
		decode(nbt.getLongArray("chamber_doors"), chamberDoors, 32);
		chamberMetals.clear();
		byte[] metals = nbt.getByteArray("chamber_metals");
		for (int i = 0; i < metals.length && chamberMetals.size() < chamberDoors.size(); i++) {
			TempleKeyRules.Metal metal = TempleKeyRules.metalFromOrdinal(metals[i]);
			if (metal == null) break;
			chamberMetals.add(metal);
		}
		while (chamberDoors.size() > chamberMetals.size()) chamberDoors.remove(chamberDoors.size() - 1);
		chambersOpen = TempleKeyRules.sanitizeMask(nbt.getInt("chambers_open"));
		// V69
		crypt = nbt.contains("crypt") ? CryptRules.stageFromOrdinal(nbt.getInt("crypt")) : CryptRules.Stage.LOCKED;
		decode(nbt.getLongArray("crypt_hatch"), cryptHatch, 16);
		decode(nbt.getLongArray("crypt_spawns"), cryptSpawns, 32);
		cryptMin = BlockPos.fromLong(nbt.getLong("crypt_min"));
		cryptMax = BlockPos.fromLong(nbt.getLong("crypt_max"));
		cryptClimax = BlockPos.fromLong(nbt.getLong("crypt_climax"));
		cryptChest = BlockPos.fromLong(nbt.getLong("crypt_chest"));
		cryptMobs.clear();
		cryptWaveLive = false;
		cryptIdleTicks = 0;
		// V70: un templo anterior no tiene estas claves y se lee sin sala de pruebas.
		decode(nbt.getLongArray("gate_one"), gateOne, 32);
		decode(nbt.getLongArray("trial_spawns"), trialSpawns, 32);
		trialMin = BlockPos.fromLong(nbt.getLong("trial_min"));
		trialMax = BlockPos.fromLong(nbt.getLong("trial_max"));
		trialWave = Math.max(0, Math.min(TempleFlowRules.TRIAL_WAVES, nbt.getInt("trial_wave")));
		trialMobs.clear();
		trialLive = false;
		trialIdleTicks = 0;
	}
}
