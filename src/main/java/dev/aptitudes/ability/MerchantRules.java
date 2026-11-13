package dev.aptitudes.ability;

/**
 * v67 (F5): cifras de las 10 habilidades nuevas del Mercader táctico. Lógica pura (sin Minecraft), con pruebas; la
 * usa {@link MerchantKit}. {@code rank} es el rango efectivo. El daño se multiplica después por
 * {@code extraSkillDamageScale}.
 */
public final class MerchantRules {
	// N2 Dividendos
	public static final int DIVIDEND_INTERVAL_TICKS = 1200;
	public static final int DIVIDEND_MAX_EMERALDS = 64;
	// N3 Moneda rebotona
	public static final double COIN_RANGE = 24;
	public static final double COIN_BOUNCE_RADIUS = 8;
	public static final int COIN_TARGETS = 3;
	// N4 Subasta
	public static final double AUCTION_RANGE = 24;
	public static final int AUCTION_TICKS = 120;
	public static final int AUCTION_DROP = 2;
	// N6 Caravana
	public static final double CARAVAN_RADIUS = 10;
	// N7 Libro mayor
	public static final double LEDGER_PER_EMERALD = 0.03;
	public static final int LEDGER_MAX_CHARGES = 5;
	// N8 Inversión
	public static final double INVEST_REFUND = 0.30;
	// N10 Gran liquidación
	public static final int LIQUIDATION_MAX_EMERALDS = 10;
	public static final double LIQUIDATION_RADIUS = 6;
	public static final double LIQUIDATION_BASE = 20;
	public static final double LIQUIDATION_PER_EMERALD = 3;

	private MerchantRules() {
	}

	/** N1: probabilidad de que una activa no gaste su esmeralda (15 / 30 / 45 %). */
	public static double waiveChance(int rank) {
		return 0.15 * Math.max(0, Math.min(3, rank));
	}

	/** N2: esmeraldas mínimas que hay que llevar para cobrar el dividendo (30 / 20 / 10). */
	public static int dividendThreshold(int rank) {
		return rank <= 0 ? Integer.MAX_VALUE : 40 - 10 * rank;
	}

	/** N2: ¿se paga un dividendo? Lleva el mínimo y no el máximo. */
	public static boolean dividendDue(int rank, int emeralds) {
		return rank > 0 && emeralds >= dividendThreshold(rank) && emeralds < DIVIDEND_MAX_EMERALDS;
	}

	/** N3: daño de la moneda (6 / 8 / 10). */
	public static double coinDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: bonificación contra el objetivo subastado (10 / 15 / 20 %). */
	public static double auctionBonus(int rank) {
		return rank <= 0 ? 0 : 0.05 + 0.05 * rank;
	}

	/** N5: maná que se paga por esmeralda que falta (10 / 5). */
	public static double creditCost(int rank) {
		return rank <= 0 ? 0 : rank == 1 ? 10 : 5;
	}

	/** N6: duración de la caravana (8 / 10 s). */
	public static int caravanTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N7: nuevas cargas del libro tras gastar esmeraldas, con tope. */
	public static int ledgerCharges(int current, int spent) {
		return Math.max(0, Math.min(LEDGER_MAX_CHARGES, current + Math.max(0, spent)));
	}

	/** N7: bonificación del siguiente golpe con {@code charges} cargas. */
	public static double ledgerBonus(int charges) {
		return LEDGER_PER_EMERALD * Math.max(0, Math.min(LEDGER_MAX_CHARGES, charges));
	}

	/** N8: duración de la inversión (10 / 14 s). */
	public static int investTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 4 * rank) * 20;
	}

	/** N9: esmeraldas por élite o campeón (2 / 4). */
	public static int bountyDrop(int rank) {
		return rank <= 0 ? 0 : 2 * rank;
	}

	/** N10: daño de la liquidación con las esmeraldas gastadas. */
	public static double liquidationDamage(int spent, double scale) {
		return (LIQUIDATION_BASE + LIQUIDATION_PER_EMERALD * Math.max(0, Math.min(LIQUIDATION_MAX_EMERALDS, spent))) * scale;
	}
}
