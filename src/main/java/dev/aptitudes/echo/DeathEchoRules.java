package dev.aptitudes.echo;

/**
 * C7 · S6 Ecos de muerte: reglas puras (sin Minecraft, con JUnit en {@code DeathEchoRulesTest}).
 *
 * <p>El servidor anota la posición de cada jugador cada {@link #SAMPLE_INTERVAL} ticks en un búfer circular de
 * {@link #CAPACITY} muestras (los últimos 5 s). Al morir, el búfer se congela y un «eco» lo reproduce en bucle mientras
 * haya otro jugador cerca. Los tiempos van en ticks y los cálculos con valores de la configuración, en {@code long}.</p>
 */
public final class DeathEchoRules {
	/** Ticks entre muestras (10 por segundo). */
	public static final int SAMPLE_INTERVAL = 2;
	/** Muestras guardadas: 5 segundos. */
	public static final int CAPACITY = 5 * 20 / SAMPLE_INTERVAL;
	/** Muestras mínimas para que valga la pena un eco (1 s). */
	public static final int MIN_FRAMES = 10;
	/** Pausa entre repeticiones del recorrido. */
	public static final int LOOP_PAUSE_TICKS = 40;
	/** Distancia a la que alguien «despierta» el eco. */
	public static final double WAKE_RADIUS = 24.0;

	private DeathEchoRules() {}

	public static boolean shouldSample(int serverTick) {
		return serverTick % SAMPLE_INTERVAL == 0;
	}

	public static boolean withinWakeRadius(double distanceSquared) {
		return distanceSquared <= WAKE_RADIUS * WAKE_RADIUS;
	}

	public static boolean canSpawn(int active, int max) {
		return active < Math.max(0, max);
	}

	public static boolean expired(long createdTick, long now, int lifetimeSeconds) {
		return now - createdTick >= Math.max(1L, (long) lifetimeSeconds) * 20L;
	}

	/**
	 * Muestra a mostrar tras {@code elapsed} ticks de reproducción activa, o -1 durante la pausa entre repeticiones.
	 * El ciclo dura {@code frames * SAMPLE_INTERVAL + LOOP_PAUSE_TICKS}.
	 */
	public static int frameAt(long elapsed, int frames) {
		if (frames <= 0) return -1;
		long cycle = (long) frames * SAMPLE_INTERVAL + LOOP_PAUSE_TICKS;
		long t = Math.floorMod(elapsed, cycle);
		long run = (long) frames * SAMPLE_INTERVAL;
		return t >= run ? -1 : (int) (t / SAMPLE_INTERVAL);
	}

	/** Recorrido congelado: posiciones y giro en orden cronológico. */
	public record Frames(double[] x, double[] y, double[] z, float[] yaw) {
		public int size() {
			return x.length;
		}
	}

	/** Búfer circular por jugador. */
	public static final class Track {
		private final double[] x = new double[CAPACITY];
		private final double[] y = new double[CAPACITY];
		private final double[] z = new double[CAPACITY];
		private final float[] yaw = new float[CAPACITY];
		private int next;
		private int count;

		public void add(double px, double py, double pz, float pyaw) {
			x[next] = px;
			y[next] = py;
			z[next] = pz;
			yaw[next] = pyaw;
			next = (next + 1) % CAPACITY;
			if (count < CAPACITY) count++;
		}

		public int size() {
			return count;
		}

		public void clear() {
			next = 0;
			count = 0;
		}

		/** Copia las muestras en orden cronológico (la más antigua primero). */
		public Frames snapshot() {
			double[] ox = new double[count], oy = new double[count], oz = new double[count];
			float[] oyaw = new float[count];
			int start = count < CAPACITY ? 0 : next;
			for (int i = 0; i < count; i++) {
				int k = (start + i) % CAPACITY;
				ox[i] = x[k];
				oy[i] = y[k];
				oz[i] = z[k];
				oyaw[i] = yaw[k];
			}
			return new Frames(ox, oy, oz, oyaw);
		}
	}
}
