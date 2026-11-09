package dev.aptitudes.boss;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

/**
 * Fase C del plan de avance (cableado de la reinvocación, paso 1): registro por mundo de la última derrota y de la
 * última fase de cada jefe animado. Se guarda en {@code data/aptitudes_summon.dat} del Overworld. Lo lee
 * {@link SummonRules} para decidir si el jefe puede reinvocarse y en qué fase vuelve.
 */
public final class SummonState extends PersistentState {
	/** Última derrota de un jefe: tick del mundo y fase en la que estaba. */
	public record Defeat(long tick, int phase) {}

	private static final Type<SummonState> TYPE = new Type<>(SummonState::new, SummonState::fromNbt, null);
	private static final String NAME = "aptitudes_summon";

	private final Map<String, Defeat> defeats = new HashMap<>();

	public static SummonState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	/** Tick de la última derrota, o {@link SummonRules#NEVER}. */
	public long lastDefeatTick(String bossId) {
		Defeat d = defeats.get(bossId);
		return d == null ? SummonRules.NEVER : d.tick();
	}

	/** Fase en la que estaba al morir; 1 si nunca se derrotó. */
	public int lastPhase(String bossId) {
		Defeat d = defeats.get(bossId);
		return d == null ? 1 : d.phase();
	}

	public void recordDefeat(String bossId, long tick, int phase) {
		defeats.put(bossId, new Defeat(tick, clampPhase(phase)));
		markDirty();
	}

	private static int clampPhase(int phase) {
		return Math.max(1, Math.min(3, phase));
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtCompound list = new NbtCompound();
		defeats.forEach((id, d) -> {
			NbtCompound entry = new NbtCompound();
			entry.putLong("tick", d.tick());
			entry.putInt("phase", d.phase());
			list.put(id, entry);
		});
		nbt.put("defeats", list);
		return nbt;
	}

	private static SummonState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		SummonState state = new SummonState();
		NbtCompound list = nbt.getCompound("defeats");
		for (String id : list.getKeys()) {
			NbtCompound entry = list.getCompound(id);
			state.defeats.put(id, new Defeat(entry.getLong("tick"), clampPhase(entry.getInt("phase"))));
		}
		return state;
	}
}
