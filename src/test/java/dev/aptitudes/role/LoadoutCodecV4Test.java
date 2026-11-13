package dev.aptitudes.role;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67: equipamiento con 4 bits por ranura (hasta 16 activas) sin romper los formatos guardados. */
class LoadoutCodecV4Test {

	@Test
	void v23ThreeBitValuesAreReadExactlyAsBefore() {
		// Formato de la v23: bit 8 + (primera & 7) | ((segunda & 7) << 3).
		for (int first = 0; first < 8; first++) {
			for (int second = 0; second < 8; second++) {
				int saved = LoadoutCodec.V3 | first | (second << 3);
				assertEquals(first, LoadoutCodec.decode(saved, 0));
				assertEquals(second, LoadoutCodec.decode(saved, 1));
			}
		}
	}

	@Test
	void newFormatHoldsSixteenActives() {
		assertEquals(15, LoadoutCodec.MAX_INDEX);
		int packed = LoadoutCodec.encode(12, 9);
		assertEquals(12, LoadoutCodec.decode(packed, 0));
		assertEquals(9, LoadoutCodec.decode(packed, 1));
		assertTrue((packed & LoadoutCodec.V4) != 0, "se escribe siempre con la marca nueva");
	}

	@Test
	void formatMarksNeverCollideWithTheSlotBits() {
		int full = LoadoutCodec.encode(15, 15);
		assertEquals(LoadoutCodec.V4 | 0xFF, full);
		assertEquals(0, full & LoadoutCodec.V3, "el formato nuevo no activa la marca de la v23");
	}
}
