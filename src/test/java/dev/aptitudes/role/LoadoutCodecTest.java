package dev.aptitudes.role;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoadoutCodecTest {

	@Test
	public void oldTwoBitFormatIsReadExactlyAsBefore() {
		// Formato de la v22: (primera & 3) | ((segunda & 3) << 2), sin bit de formato.
		for (int first = 0; first < 4; first++) {
			for (int second = 0; second < 4; second++) {
				int legacy = first | (second << 2);
				assertEquals(first, LoadoutCodec.decode(legacy, 0));
				assertEquals(second, LoadoutCodec.decode(legacy, 1));
			}
		}
	}

	@Test
	public void newFormatStoresEightActives() {
		for (int first = 0; first <= LoadoutCodec.MAX_INDEX; first++) {
			for (int second = 0; second <= LoadoutCodec.MAX_INDEX; second++) {
				int packed = LoadoutCodec.encode(first, second);
				assertEquals(first, LoadoutCodec.decode(packed, 0));
				assertEquals(second, LoadoutCodec.decode(packed, 1));
			}
		}
	}

	@Test
	public void outOfRangeIndicesAreClamped() {
		int packed = LoadoutCodec.encode(-3, 99);
		assertEquals(0, LoadoutCodec.decode(packed, 0));
		assertEquals(LoadoutCodec.MAX_INDEX, LoadoutCodec.decode(packed, 1));
	}
}
