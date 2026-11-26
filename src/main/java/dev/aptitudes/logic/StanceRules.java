package dev.aptitudes.logic;

/**
 * V97: reglas puras de las posturas de combate (piloto con Bersérker, Lancero, Monje y Mago). La postura se muestra mientras el jugador
 * sostiene el arma del rol y está «en combate» (acaba de golpear, de recibir daño o hay un enemigo cerca), y se mantiene {@link #HOLD_TICKS}
 * ticks tras el último motivo para que no parpadee. Nunca se muestra corriendo, nadando, planeando, montado, durmiendo, trepando,
 * usando un objeto ni muerto: ahí manda la locomoción vanilla. La precedencia completa (muerte &gt; reacción &gt; habilidad &gt; postura &gt;
 * locomoción) la da la prioridad de las capas de animación ({@code PlayerAnims}); estas reglas solo deciden «postura sí o no».
 */
public final class StanceRules {
	/** Ticks que se mantiene la postura tras el último motivo de combate (3 s). */
	public static final int HOLD_TICKS = 60;

	/** Una postura por arma de rol del piloto. */
	public enum Stance {
		BERSERKER(PlayerAnimIds.STANCE_BERSERKER),
		LANCER(PlayerAnimIds.STANCE_LANCER),
		MONK(PlayerAnimIds.STANCE_MONK),
		MAGE(PlayerAnimIds.STANCE_MAGE);

		private final String animation;

		Stance(String animation) {
			this.animation = animation;
		}

		public String animation() {
			return animation;
		}
	}

	private StanceRules() {
	}

	/** Cuenta atrás tras un tick: vuelve a {@link #HOLD_TICKS} si hay motivo de combate y, si no, baja de uno en uno hasta 0. */
	public static int nextTimer(int timer, boolean engaged) {
		return engaged ? HOLD_TICKS : Math.max(0, timer - 1);
	}

	/**
	 * ¿Se muestra la postura?
	 *
	 * @param timer cuenta atrás actual (ver {@link #nextTimer})
	 * @param holdsWeapon sostiene el arma del rol en la mano principal
	 * @param blocked cualquiera de los estados que impiden la postura (ver {@link #blocked})
	 */
	public static boolean active(int timer, boolean holdsWeapon, boolean blocked) {
		return holdsWeapon && !blocked && timer > 0;
	}

	/** Estados en los que manda la locomoción o la acción vanilla. */
	public static boolean blocked(boolean sprinting, boolean swimming, boolean gliding, boolean riding, boolean sleeping,
								  boolean climbing, boolean usingItem, boolean dead) {
		return sprinting || swimming || gliding || riding || sleeping || climbing || usingItem || dead;
	}
}
