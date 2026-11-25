package dev.aptitudes.logic;

/**
 * V93: aritmética pura de la telemetría de combate (sin Minecraft, probada con JUnit). Una {@link Sample} acumula el daño de un jugador
 * por origen (habilidad, derivado de otra, o vanilla), el tiempo de combate activo, las bajas y el tiempo medio hasta derrotar.
 */
public final class CombatTelemetryRules {
	/** Ticks sin dañar tras los que el combate se considera terminado. */
	public static final int IDLE_TICKS = 100;
	/** Tiempo que se cuenta al abrir un combate nuevo con un primer golpe (1 s). */
	public static final int OPENING_TICKS = 20;

	private CombatTelemetryRules() {
	}

	/** Origen del daño: la habilidad lanzada directamente, un efecto derivado de otra, o un golpe/disparo vanilla. */
	public enum Kind {
		ABILITY, DERIVED, VANILLA
	}

	public static final class Sample {
		public double abilityDamage;
		public double derivedDamage;
		public double vanillaDamage;
		/** Parte del daño total que cayó sobre jugadores (PvP). */
		public double pvpDamage;
		public int casts;
		public double energySpent;
		public int kills;
		public double hpKilled;
		public long ttkTicks;
		public int ttkCount;
		public long activeTicks;
		private long lastHitTick = Long.MIN_VALUE;

		/** Registra un golpe en el tick {@code tick}. */
		public void addDamage(Kind kind, double amount, boolean pvp, long tick) {
			if (!(amount > 0) || Double.isInfinite(amount)) return;
			switch (kind) {
				case ABILITY -> abilityDamage += amount;
				case DERIVED -> derivedDamage += amount;
				case VANILLA -> vanillaDamage += amount;
			}
			if (pvp) pvpDamage += amount;
			long gap = tick - lastHitTick;
			if (lastHitTick == Long.MIN_VALUE || gap > IDLE_TICKS || gap < 0) activeTicks += OPENING_TICKS;
			else activeTicks += gap;
			lastHitTick = tick;
		}

		public void addCast(double energy) {
			casts++;
			if (energy > 0) energySpent += energy;
		}

		public void addKill(double maxHealth, long ticksToKill) {
			kills++;
			if (maxHealth > 0) hpKilled += maxHealth;
			if (ticksToKill >= 0) {
				ttkTicks += ticksToKill;
				ttkCount++;
			}
		}

		public double totalDamage() {
			return abilityDamage + derivedDamage + vanillaDamage;
		}

		public double activeSeconds() {
			return activeTicks / 20.0;
		}

		/** Daño efectivo por segundo de combate activo; 0 si no hubo combate. */
		public double dps() {
			return activeTicks <= 0 ? 0 : totalDamage() / activeSeconds();
		}

		/** Parte del daño que viene de habilidades (directas o derivadas), entre 0 y 1. */
		public double abilityShare() {
			double total = totalDamage();
			return total <= 0 ? 0 : (abilityDamage + derivedDamage) / total;
		}

		/** Parte del daño de habilidad que es derivada (cadenas de pasivas), entre 0 y 1. */
		public double derivedShare() {
			double fromAbilities = abilityDamage + derivedDamage;
			return fromAbilities <= 0 ? 0 : derivedDamage / fromAbilities;
		}

		/** Daño de habilidad por punto de energía gastado; 0 si no se gastó. */
		public double damagePerEnergy() {
			return energySpent <= 0 ? 0 : (abilityDamage + derivedDamage) / energySpent;
		}

		/** Segundos medios desde el primer golpe hasta la baja; 0 si no hubo bajas medidas. */
		public double averageTimeToKillSeconds() {
			return ttkCount <= 0 ? 0 : ttkTicks / 20.0 / ttkCount;
		}

		/** Vida derrotada por segundo de combate activo (métrica de «tiempo para derrotar» comparable entre roles). */
		public double hpKilledPerSecond() {
			return activeTicks <= 0 ? 0 : hpKilled / activeSeconds();
		}

		/** Suma otra muestra a esta (el tiempo activo se suma: úsese para agrupar jugadores distintos). */
		public void merge(Sample other) {
			abilityDamage += other.abilityDamage;
			derivedDamage += other.derivedDamage;
			vanillaDamage += other.vanillaDamage;
			pvpDamage += other.pvpDamage;
			casts += other.casts;
			energySpent += other.energySpent;
			kills += other.kills;
			hpKilled += other.hpKilled;
			ttkTicks += other.ttkTicks;
			ttkCount += other.ttkCount;
			activeTicks += other.activeTicks;
		}
	}
}
