package dev.aptitudes.mob;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Tiempos, selección y volúmenes ofensivos del Osario en coordenadas relativas a sus pies. */
public final class OsarioStrikeRules {
	public static final int GLOBAL_COOLDOWN = 20;
	public static final double DASH_SPEED = 0.42;
	public static final int DASH_TICKS = 7;
	private static final double EPSILON = 1e-9;

	private OsarioStrikeRules() {}

	public enum Attack {
		CLAW("claw_swipe", 18, 4, 10, 11, 7, 24, 2, .2, 1.7, 30, .9, .35, 0),
		HEAVY("heavy_swipe", 28, 8, 19, 21, 14, 70, 2.7, 0, 1.7, 80, 1.3, .6, .15),
		SLAM("ground_slam", 36, 10, 23, 24, 16, 200, 2.1, 0, 1.4, 0, 1.6, .5, .3),
		LUNGE("lunge", 27, 4, 14, 20, 14, 160, .55, 0, 1.8, 0, 1, .8, 0);

		public final String clip;
		public final int duration, warning, impact, lastActive, cancelBefore;
		public final double radius, minY, maxY, halfAngle, damage, knockback, lift;
		private final int cooldown;

		Attack(String clip, int duration, int warning, int impact, int lastActive, int cancelBefore, int cooldown,
				double radius, double minY, double maxY, double halfAngle, double damage, double knockback, double lift) {
			this.clip = clip;
			this.duration = duration;
			this.warning = warning;
			this.impact = impact;
			this.lastActive = lastActive;
			this.cancelBefore = cancelBefore;
			this.cooldown = cooldown;
			this.radius = radius;
			this.minY = minY;
			this.maxY = maxY;
			this.halfAngle = halfAngle;
			this.damage = damage;
			this.knockback = knockback;
			this.lift = lift;
		}

		int cooldown(boolean elite) {
			return this == SLAM && elite ? 120 : cooldown;
		}

		int end(boolean enraged) {
			int recovery = duration - lastActive - 1;
			return duration - (enraged ? (int) Math.floor(recovery * .2) : 0);
		}

		boolean active(int age) {
			return age >= impact && age <= lastActive;
		}
	}

	enum Action {
		NONE(null, null, 0), ALERT("alert", null, 13),
		CLAW(Attack.CLAW.clip, Attack.CLAW, Attack.CLAW.duration),
		HEAVY(Attack.HEAVY.clip, Attack.HEAVY, Attack.HEAVY.duration),
		SLAM(Attack.SLAM.clip, Attack.SLAM, Attack.SLAM.duration),
		LUNGE(Attack.LUNGE.clip, Attack.LUNGE, Attack.LUNGE.duration),
		HURT("hurt", null, 8), DEATH("death", null, 19);

		private static final Action[] ACTIONS = values();
		static final int BITS = 4;
		final String clip;
		final Attack attack;
		final int duration;

		Action(String clip, Attack attack, int duration) {
			this.clip = clip;
			this.attack = attack;
			this.duration = duration;
		}

		long stamp(long tick) {
			return this == NONE ? 0L : (tick << BITS) | ordinal();
		}

		static Action from(long stamp) {
			int id = (int) (stamp & ((1 << BITS) - 1));
			return id < ACTIONS.length ? ACTIONS[id] : NONE;
		}

		static Action of(Attack attack) {
			return switch (attack) {
				case CLAW -> CLAW;
				case HEAVY -> HEAVY;
				case SLAM -> SLAM;
				case LUNGE -> LUNGE;
			};
		}
	}

	record Choice(double distance, boolean elite, boolean circling, boolean crowded, boolean retreating,
			boolean denseFog, boolean enraged, EnumSet<Attack> available, Attack last, int repeats) {}

	static Attack choose(Choice c, long sample) {
		int[] weights = new int[Attack.values().length];
		int sum = 0;
		for (Attack attack : Attack.values()) {
			if (!c.available.contains(attack) || attack == c.last && c.repeats >= 2) continue;
			double w = switch (attack) {
				case CLAW -> c.distance <= 2 ? (c.distance < 1.7 ? 55 : 10) : 0;
				case HEAVY -> c.distance <= 2.7 ? (c.distance < 1.7 ? 25 : 55) + (c.circling ? 20 : 0) : 0;
				case SLAM -> c.distance <= 2.4 ? (c.elite ? (c.distance < 1.7 ? 30 : 22)
						: (c.distance < 1.7 ? 8 : 6)) + (c.crowded ? 25 : 0) : 0;
				case LUNGE -> c.distance >= 3.2 && c.distance <= 6.5 ? 45 * (c.retreating ? 1.4 : 1) : 0;
			};
			if (c.denseFog && (attack == Attack.SLAM || attack == Attack.LUNGE)) w *= .5;
			if (c.enraged && (attack == Attack.HEAVY || attack == Attack.LUNGE)) w *= 1.25;
			if (attack == c.last) w *= .35;
			int weight = (int) Math.round(w * 100);
			weights[attack.ordinal()] = weight;
			sum += weight;
		}
		if (sum == 0) return null;
		// Se mezcla una semilla estable de criatura con el turno de selección en el servidor.
		long mixed = sample ^ (sample >>> 33);
		mixed *= 0xff51afd7ed558ccdL;
		mixed ^= mixed >>> 33;
		int pick = Math.floorMod(mixed, sum);
		for (Attack attack : Attack.values()) {
			pick -= weights[attack.ordinal()];
			if (pick < 0) return attack;
		}
		throw new IllegalStateException("Selección de ataque fuera de rango");
	}

	record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {}
	record Point(double x, double y, double z) {}
	record DashContact(double fraction, Point point) {}

	static Point contact(Attack attack, Bounds box, double fx, double fz, double scale) {
		if (attack == Attack.LUNGE) throw new IllegalArgumentException("La embestida necesita un tramo");
		double lower = Math.max(box.minY, attack.minY * scale), upper = Math.min(box.maxY, attack.maxY * scale);
		if (lower > upper) return null;
		double y = Math.clamp((attack == Attack.SLAM ? .30 : 1.1) * scale, lower, upper);
		if (attack == Attack.SLAM) {
			double cx = fx * 1.5 * scale, cz = fz * 1.5 * scale;
			double x = Math.clamp(cx, box.minX, box.maxX), z = Math.clamp(cz, box.minZ, box.maxZ);
			return square(x - cx) + square(z - cz) <= square(attack.radius * scale) + EPSILON ? new Point(x, y, z) : null;
		}
		if (box.minX <= 0 && box.maxX >= 0 && box.minZ <= 0 && box.maxZ >= 0) return new Point(0, y, 0);
		List<Point> polygon = List.of(new Point(box.minX, y, box.minZ), new Point(box.maxX, y, box.minZ),
				new Point(box.maxX, y, box.maxZ), new Point(box.minX, y, box.maxZ));
		double sin = Math.sin(Math.toRadians(attack.halfAngle)), cos = Math.cos(Math.toRadians(attack.halfAngle));
		for (int side : new int[] {-1, 1}) {
			polygon = clip(polygon, sin * fx + side * cos * fz, sin * fz - side * cos * fx);
			if (polygon.isEmpty()) return null;
		}
		Point closest = null;
		double best = square(attack.radius * scale) + EPSILON;
		for (int i = 0; i < polygon.size(); i++) {
			Point a = polygon.get(i), b = polygon.get((i + 1) % polygon.size());
			double dx = b.x - a.x, dz = b.z - a.z, size = dx * dx + dz * dz;
			double t = size == 0 ? 0 : Math.clamp(-(a.x * dx + a.z * dz) / size, 0, 1);
			Point p = new Point(a.x + t * dx, y, a.z + t * dz);
			double distance = square(p.x) + square(p.z);
			if (distance <= best) {
				best = distance;
				closest = p;
			}
		}
		return closest;
	}

	private static List<Point> clip(List<Point> polygon, double nx, double nz) {
		List<Point> result = new ArrayList<>(polygon.size() + 1);
		Point previous = polygon.getLast();
		double previousDistance = previous.x * nx + previous.z * nz;
		for (Point p : polygon) {
			double distance = p.x * nx + p.z * nz;
			if ((distance >= 0) != (previousDistance >= 0)) {
				double t = previousDistance / (previousDistance - distance);
				result.add(new Point(previous.x + t * (p.x - previous.x), p.y, previous.z + t * (p.z - previous.z)));
			}
			if (distance >= 0) result.add(p);
			previous = p;
			previousDistance = distance;
		}
		return result;
	}

	/** Primer contacto del segmento recorrido por el mob contra la caja ampliada por un círculo horizontal. */
	static DashContact dashContact(Bounds box, double dx, double dy, double dz, double scale) {
		double minT = 0, maxT = 1, reach = Attack.LUNGE.radius * scale;
		double lower = box.minY - Attack.LUNGE.maxY * scale, upper = box.maxY;
		if (Math.abs(dy) < EPSILON) {
			if (lower > 0 || upper < 0) return null;
		} else {
			minT = Math.max(0, Math.min(lower / dy, upper / dy));
			maxT = Math.min(1, Math.max(lower / dy, upper / dy));
			if (minT > maxT) return null;
		}
		List<Double> cuts = new ArrayList<>(6);
		cuts.add(minT);
		cuts.add(maxT);
		if (Math.abs(dx) > EPSILON) {
			addCut(cuts, box.minX / dx, minT, maxT);
			addCut(cuts, box.maxX / dx, minT, maxT);
		}
		if (Math.abs(dz) > EPSILON) {
			addCut(cuts, box.minZ / dz, minT, maxT);
			addCut(cuts, box.maxZ / dz, minT, maxT);
		}
		cuts.sort(Double::compare);
		for (int i = 0; i + 1 < cuts.size(); i++) {
			double a = cuts.get(i), b = cuts.get(i + 1), mid = (a + b) * .5;
			double x = dx * mid, z = dz * mid;
			double vx = x >= box.minX && x <= box.maxX ? 0 : dx;
			double vz = z >= box.minZ && z <= box.maxZ ? 0 : dz;
			double cx = x < box.minX ? box.minX : x > box.maxX ? box.maxX : 0;
			double cz = z < box.minZ ? box.minZ : z > box.maxZ ? box.maxZ : 0;
			double atX = vx * a - cx, atZ = vz * a - cz;
			double constant = square(atX) + square(atZ) - square(reach);
			double fraction = a;
			if (constant > EPSILON) {
				double speed = square(vx) + square(vz);
				double dot = atX * vx + atZ * vz;
				double discriminant = dot * dot - speed * constant;
				if (speed < EPSILON || discriminant < 0) continue;
				fraction += (-dot - Math.sqrt(discriminant)) / speed;
				if (fraction < a - EPSILON || fraction > b + EPSILON) continue;
			}
			double y = Math.clamp(dy * fraction + .9 * scale, box.minY, box.maxY);
			return new DashContact(fraction, new Point(Math.clamp(dx * fraction, box.minX, box.maxX), y,
					Math.clamp(dz * fraction, box.minZ, box.maxZ)));
		}
		return null;
	}

	private static void addCut(List<Double> cuts, double t, double min, double max) {
		if (t > min && t < max) cuts.add(t);
	}

	private static double square(double x) {
		return x * x;
	}

	static final class Execution {
		final Attack attack;
		final long started;
		final int end;
		private final Set<UUID> attempted = new HashSet<>();

		Execution(Attack attack, long started, boolean enraged) {
			this.attack = attack;
			this.started = started;
			this.end = attack.end(enraged);
		}

		int age(long tick) {
			return (int) (tick - started);
		}

		boolean firstAttempt(UUID target, long tick) {
			return attack.active(age(tick)) && attempted.add(target);
		}
	}

	/** Fichas de presión por dimensión, usadas solo desde el hilo del servidor. */
	static final class Pressure {
		private final Map<UUID, Set<UUID>> slots = new HashMap<>();

		boolean acquire(UUID target, UUID osario) {
			Set<UUID> users = slots.computeIfAbsent(target, ignored -> new HashSet<>());
			if (users.contains(osario)) return true;
			return users.size() < 3 && users.add(osario);
		}

		void release(UUID target, UUID osario) {
			Set<UUID> users = slots.get(target);
			if (users == null) return;
			users.remove(osario);
			if (users.isEmpty()) slots.remove(target);
		}

		void removeTarget(UUID target) {
			slots.remove(target);
		}
	}
}
