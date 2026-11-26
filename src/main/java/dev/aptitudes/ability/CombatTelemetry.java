package dev.aptitudes.ability;

import dev.aptitudes.logic.CombatTelemetryRules.Kind;
import dev.aptitudes.logic.CombatTelemetryRules.Sample;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * V93: telemetría de combate en memoria (se pierde al reiniciar). Acumula por jugador, etiqueta de escenario y rol principal el daño por
 * origen, el gasto de energía, las bajas y el tiempo medio hasta derrotar. Los jugadores <b>sin rol principal</b> forman el grupo
 * {@code VANILLA}: es la línea base del combate normal contra la que comparar a cada rol. No toca ningún valor de juego.
 */
public final class CombatTelemetry implements CombatEvents.Listener {
	public static final String VANILLA = "VANILLA";
	private static final int MAX_TRACKED_VICTIMS = 4096;
	private static final CombatTelemetry INSTANCE = new CombatTelemetry();

	private record Key(UUID player, String label, String group) {
	}

	private record FirstHit(int tick, UUID attacker) {
	}

	private final Map<Key, Sample> samples = new HashMap<>();
	private final Map<UUID, FirstHit> firstHits = new HashMap<>();
	private String label = "general";

	private CombatTelemetry() {
	}

	public static void register() {
		CombatEvents.addListener(INSTANCE);
	}

	public static CombatTelemetry get() {
		return INSTANCE;
	}

	private static String groupOf(ServerPlayerEntity player) {
		Role primary = RoleManager.get(player).primary();
		return primary == null ? VANILLA : primary.name();
	}

	private Sample sampleOf(ServerPlayerEntity player) {
		return samples.computeIfAbsent(new Key(player.getUuid(), label, groupOf(player)), k -> new Sample());
	}

	@Override
	public void onCast(ServerPlayerEntity player, String abilityId, double energySpent) {
		sampleOf(player).addCast(energySpent);
	}

	@Override
	public void onDamage(CombatEvents.DamageEvent e) {
		Kind kind = e.autonomous() ? Kind.SUMMON
				: e.abilityId() != null || e.derived() ? (e.derived() ? Kind.DERIVED : Kind.ABILITY) : Kind.VANILLA;
		sampleOf(e.attacker()).addDamage(kind, e.amount(), e.pvp(), e.tick());
		if (firstHits.size() >= MAX_TRACKED_VICTIMS) firstHits.clear();
		firstHits.putIfAbsent(e.victim().getUuid(), new FirstHit(e.tick(), e.attacker().getUuid()));
	}

	@Override
	public void onHeal(ServerPlayerEntity healer, LivingEntity target, double effective, double over, int tick) {
		sampleOf(healer).addHeal(effective, over);
	}

	@Override
	public void onKill(ServerPlayerEntity player, LivingEntity victim, int tick) {
		FirstHit first = firstHits.remove(victim.getUuid());
		long ttk = first != null && first.attacker().equals(player.getUuid()) ? tick - first.tick() : -1;
		sampleOf(player).addKill(victim.getMaxHealth(), ttk);
	}

	// ------------------------------------------------------------------ Consulta

	public String label() {
		return label;
	}

	public void setLabel(String value) {
		label = value == null || value.isBlank() ? "general" : value.trim();
	}

	public void reset() {
		samples.clear();
		firstHits.clear();
	}

	/** Muestra del jugador en la etiqueta actual, sumando todos los grupos que haya tenido (cambió de rol principal). */
	public Sample ofPlayer(UUID player) {
		Sample total = new Sample();
		for (Map.Entry<Key, Sample> entry : samples.entrySet()) {
			if (entry.getKey().player().equals(player) && entry.getKey().label().equals(label)) total.merge(entry.getValue());
		}
		return total;
	}

	/** Muestras de la etiqueta actual agrupadas por rol principal (y {@link #VANILLA}), ordenadas por nombre. */
	public Map<String, Sample> byGroup() {
		Map<String, Sample> out = new TreeMap<>();
		for (Map.Entry<Key, Sample> entry : samples.entrySet()) {
			if (!entry.getKey().label().equals(label)) continue;
			out.computeIfAbsent(entry.getKey().group(), g -> new Sample()).merge(entry.getValue());
		}
		return out;
	}
}
