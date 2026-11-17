package dev.aptitudes.ability;

/** v65 C5: máscara de las cinco sinergias (Y1–Y5) activadas en una sesión. Pura. */
public final class SynergyMask {
	public static final int COUNT = 5;
	public static final int FULL = (1 << COUNT) - 1;

	private SynergyMask() {
	}

	public static int add(int mask, int index) {
		if (index < 0 || index >= COUNT) return mask;
		return mask | (1 << index);
	}

	public static boolean complete(int mask) {
		return (mask & FULL) == FULL;
	}

	public static int count(int mask) {
		return Integer.bitCount(mask & FULL);
	}

	public static boolean has(int mask, int index) {
		return index >= 0 && index < COUNT && (mask & (1 << index)) != 0;
	}
}
