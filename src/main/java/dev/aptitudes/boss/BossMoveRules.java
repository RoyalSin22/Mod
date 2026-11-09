package dev.aptitudes.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * v63: elección y geometría de los ataques de los jefes animados (GeckoLib). Puro (sin Minecraft): se prueba con
 * JUnit y lo usan {@code AnimatedBossEntity} y sus jefes.
 */
public final class BossMoveRules {
	/**
	 * Un ataque del repertorio.
	 *
	 * @param id       nombre (también el de la animación que se dispara)
	 * @param minPhase fase mínima (1–3) en la que se usa
	 * @param minRange distancia mínima al objetivo
	 * @param maxRange distancia máxima al objetivo
	 * @param windup   ticks de aviso antes del golpe (el jugador tiene ese margen para esquivar)
	 * @param cooldown ticks hasta poder repetirlo
	 * @param weight   peso relativo en el sorteo (≥ 1)
	 */
	public record Move(String id, int minPhase, double minRange, double maxRange, int windup, int cooldown, int weight) {
		public Move {
			if (id == null || id.isBlank()) throw new IllegalArgumentException("id");
			windup = Math.max(1, windup);
			cooldown = Math.max(0, cooldown);
			weight = Math.max(1, weight);
		}
	}

	private BossMoveRules() {}

	/** Ataques posibles ahora: fase alcanzada, objetivo a distancia válida y fuera de enfriamiento. */
	public static List<Move> eligible(List<Move> moves, int phase, double distance, Map<String, Long> readyAt, long now) {
		List<Move> out = new ArrayList<>();
		for (Move m : moves) {
			if (phase < m.minPhase() || distance < m.minRange() || distance > m.maxRange()) continue;
			if (readyAt.getOrDefault(m.id(), Long.MIN_VALUE) > now) continue;
			out.add(m);
		}
		return out;
	}

	/** Sorteo ponderado con una tirada no negativa; null si no hay ninguno. */
	public static Move choose(List<Move> moves, int phase, double distance, Map<String, Long> readyAt, long now, int roll) {
		List<Move> options = eligible(moves, phase, distance, readyAt, now);
		if (options.isEmpty()) return null;
		int total = 0;
		for (Move m : options) total += m.weight();
		int pick = Math.floorMod(roll, total);
		for (Move m : options) {
			pick -= m.weight();
			if (pick < 0) return m;
		}
		return options.get(options.size() - 1);
	}

	/**
	 * ¿Está el punto (dx, dz) dentro de un arco frente al jefe? {@code yaw} en grados de Minecraft (0 = sur, +z),
	 * {@code arc} es la apertura total en grados.
	 */
	public static boolean inArc(double dx, double dz, double yaw, double arc, double radius) {
		double dist = Math.hypot(dx, dz);
		if (dist > radius) return false;
		if (dist < 1e-6) return true;
		double facingX = -Math.sin(Math.toRadians(yaw));
		double facingZ = Math.cos(Math.toRadians(yaw));
		double cos = (dx * facingX + dz * facingZ) / dist;
		return cos >= Math.cos(Math.toRadians(Math.max(0, Math.min(360, arc)) / 2.0));
	}

	/** Multiplicador del daño recibido: en la fase 2+ la armadura agrietada recibe más. */
	public static float damageTaken(int phase, double crackedBonus) {
		return phase >= 2 ? (float) (1.0 + Math.max(0, Math.min(1, crackedBonus))) : 1f;
	}
}
