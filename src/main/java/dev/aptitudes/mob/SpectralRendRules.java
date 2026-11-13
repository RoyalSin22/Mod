package dev.aptitudes.mob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Reloj y volumen del Rasgado Espectral, independientes del cliente. */
public final class SpectralRendRules {
	public static final int WARNING_TICK = 6;
	public static final int IMPACT_TICK = 10;
	public static final int END_TICK = 20;
	public static final int WEAKNESS_TICKS = 40;
	public static final double REACH = 1.75;
	public static final double MIN_HEIGHT = 0.25;
	public static final double MAX_HEIGHT = 1.55;
	private static final double HALF_ANGLE_COS = 0.5;

	private SpectralRendRules() {}

	/** El punto de contacto es relativo a los pies y el vector frontal está normalizado. */
	public static boolean contains(double x, double y, double z, double forwardX, double forwardZ, double scale) {
		double distance = x * x + z * z;
		return y >= MIN_HEIGHT * scale && y <= MAX_HEIGHT * scale
				&& distance <= square(REACH * scale)
				&& x * forwardX + z * forwardZ >= HALF_ANGLE_COS * Math.sqrt(distance) - 1e-9;
	}

	record Point(double x, double y, double z) {}

	/** Interseca la caja horizontal con ambos lados del sector y busca el punto más cercano del polígono. */
	static Point contact(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
			double forwardX, double forwardZ, double scale) {
		double lower = Math.max(minY, MIN_HEIGHT * scale);
		double upper = Math.min(maxY, MAX_HEIGHT * scale);
		if (lower > upper) return null;
		double y = Math.clamp(1.1 * scale, lower, upper);
		if (minX <= 0 && maxX >= 0 && minZ <= 0 && maxZ >= 0) return new Point(0, y, 0);
		List<Point> corners = List.of(new Point(minX, y, minZ), new Point(maxX, y, minZ),
				new Point(maxX, y, maxZ), new Point(minX, y, maxZ));
		double sin = Math.sqrt(1 - HALF_ANGLE_COS * HALF_ANGLE_COS);
		for (int side : new int[] {-1, 1}) {
			corners = clip(corners, sin * forwardX + side * HALF_ANGLE_COS * forwardZ,
					sin * forwardZ - side * HALF_ANGLE_COS * forwardX);
			if (corners.isEmpty()) return null;
		}
		Point closest = null;
		double best = square(REACH * scale);
		for (int i = 0; i < corners.size(); i++) {
			Point a = corners.get(i), b = corners.get((i + 1) % corners.size());
			double dx = b.x - a.x, dz = b.z - a.z;
			double length = dx * dx + dz * dz;
			double t = length == 0 ? 0 : Math.clamp(-(a.x * dx + a.z * dz) / length, 0, 1);
			Point candidate = new Point(a.x + t * dx, y, a.z + t * dz);
			double distance = square(candidate.x) + square(candidate.z);
			if (distance <= best + 1e-9) {
				closest = candidate;
				best = distance;
			}
		}
		return closest;
	}

	private static List<Point> clip(List<Point> polygon, double nx, double nz) {
		List<Point> result = new ArrayList<>(polygon.size() + 1);
		Point previous = polygon.getLast();
		double previousSide = previous.x * nx + previous.z * nz;
		for (Point p : polygon) {
			double side = p.x * nx + p.z * nz;
			if ((side >= 0) != (previousSide >= 0)) {
				double t = previousSide / (previousSide - side);
				result.add(new Point(previous.x + t * (p.x - previous.x), p.y, previous.z + t * (p.z - previous.z)));
			}
			if (side >= 0) result.add(p);
			previous = p;
			previousSide = side;
		}
		return result;
	}

	private static double square(double value) {
		return value * value;
	}

	static final class Execution {
		private final long started;
		private final Set<UUID> attempted = new HashSet<>();
		private boolean warned;
		private boolean struck;
		private boolean cancelled;

		Execution(long started) {
			this.started = started;
		}

		boolean warn(long now) {
			if (cancelled || warned || now - started != WARNING_TICK) return false;
			warned = true;
			return true;
		}

		boolean strike(long now) {
			if (cancelled || struck || now - started != IMPACT_TICK) return false;
			struck = true;
			return true;
		}

		boolean firstAttempt(UUID id) {
			return struck && !cancelled && attempted.add(id);
		}

		boolean finished(long now) {
			return cancelled || now - started >= END_TICK;
		}

		boolean preparing(long now) {
			return now - started < WARNING_TICK;
		}

		void cancel() {
			cancelled = true;
		}
	}
}
