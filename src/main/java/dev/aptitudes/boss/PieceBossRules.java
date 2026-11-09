package dev.aptitudes.boss;

import java.util.ArrayList;
import java.util.List;

/**
 * v63b (C4 siguiente): reglas puras de los jefes con mecánica de piezas y de devolución (sin Minecraft).
 *
 * <p>Guardián Desmontado: tres piezas en orden fijo (yelmo, escudo, guanteletes). Solo la pieza activa absorbe
 * daño; al romperla, la siguiente pasa a estar activa. Sin piezas, el núcleo queda expuesto.</p>
 *
 * <p>Autómata Antiguo: una esfera devuelta que lo alcanza lo aturde durante un tiempo y multiplica el daño recibido.</p>
 */
public final class PieceBossRules {
	/** Piezas del Guardián en el orden en que deben romperse. Solo se añaden al final. */
	public enum Piece { HELM, SHIELD, GAUNTLETS }

	public static final List<Piece> ORDER = List.of(Piece.HELM, Piece.SHIELD, Piece.GAUNTLETS);
	public static final int PIECE_HEALTH = 40;
	public static final double CORE_DAMAGE_MULTIPLIER = 1.5;

	/** Estado de las piezas: cuántas quedan (las rotas son las primeras de {@link #ORDER}). */
	public record Guardian(int intact) {
		public Guardian {
			intact = Math.max(0, Math.min(ORDER.size(), intact));
		}

		public static Guardian fresh() {
			return new Guardian(ORDER.size());
		}

		/** Pieza que absorbe el daño ahora, o null si el núcleo está expuesto. */
		public Piece active() {
			return intact == 0 ? null : ORDER.get(ORDER.size() - intact);
		}

		public boolean coreExposed() {
			return intact == 0;
		}

		/** Aplica un golpe: el daño a la pieza activa la rompe si se supera su vida; el excedente no pasa. */
		public Result hit(Piece target, double damage, double pieceHealthLeft) {
			if (intact == 0 || target != active() || damage <= 0) return new Result(this, false);
			if (damage < pieceHealthLeft) return new Result(this, false);
			return new Result(new Guardian(intact - 1), true);
		}
	}

	/** Resultado de un golpe a una pieza: nuevo estado y si la pieza se rompió. */
	public record Result(Guardian state, boolean broke) {}

	/** Daño al núcleo: x1,5 cuando está expuesto. Un golpe a una pieza no daña al núcleo. */
	public static double coreDamage(Guardian state, double damage) {
		if (!state.coreExposed() || damage <= 0) return 0;
		return damage * CORE_DAMAGE_MULTIPLIER;
	}

	/** Piezas que flotan alrededor en el orden de anillo, para las animaciones y el reparto de la pieza activa. */
	public static List<Piece> remaining(Guardian state) {
		List<Piece> out = new ArrayList<>();
		for (int i = ORDER.size() - state.intact(); i < ORDER.size(); i++) out.add(ORDER.get(i));
		return out;
	}

	// ----------------------------------------------------------- Autómata

	public static final int STUN_TICKS = 100;
	public static final double STUN_DAMAGE_MULTIPLIER = 2.0;
	public static final double ORB_SPEED = 0.35;
	public static final double ORB_DEFLECT_BONUS = 1.5;

	/** Estado de aturdimiento: ticks restantes (0 = activo). */
	public record Stun(int ticksLeft) {
		public Stun {
			ticksLeft = Math.max(0, ticksLeft);
		}

		public boolean stunned() {
			return ticksLeft > 0;
		}

		public Stun tick() {
			return new Stun(ticksLeft - 1);
		}

		/** Una esfera devuelta lo alcanza: aturdimiento completo, no se acumula. */
		public static Stun hit() {
			return new Stun(STUN_TICKS);
		}
	}

	/** Daño recibido por el Autómata: x2 mientras está aturdido. */
	public static double automatonDamage(Stun stun, double damage) {
		if (damage <= 0) return 0;
		return stun.stunned() ? damage * STUN_DAMAGE_MULTIPLIER : damage;
	}

	/**
	 * Velocidad de la esfera devuelta: el golpe del jugador la invierte y la acelera, pero nunca supera {@code maxSpeed}.
	 */
	public static double deflectedSpeed(double speed, double maxSpeed) {
		double v = Math.abs(speed) * ORB_DEFLECT_BONUS;
		return Math.min(Math.max(0, maxSpeed), v);
	}
}
