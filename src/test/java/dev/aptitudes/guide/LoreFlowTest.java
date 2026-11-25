package dev.aptitudes.guide;

import dev.aptitudes.network.LorePayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/** V71: del hecho del juego a lo que lee el jugador: desbloqueo → estado → sincronización → Crónicas. */
class LoreFlowTest {
	private static LoreState apply(LoreState state, Set<String> unlocks) {
		return state.unlockAll(unlocks);
	}

	@Test void eachGameplayHookRevealsSomethingNewInTheChronicles() {
		LoreState state = new LoreState();
		int count = LoreCatalog.unlockedCount(state::isUnlocked);
		assertEquals(3, count, "arranca con las tres entradas iniciales");
		for (Set<String> hook : new Set[] {LoreRules.unlocksOnGuildmasterTalk("fog"), LoreRules.unlocksOnGuildmasterTalk("rain"),
				LoreRules.unlocksOnCustodianEncounter(), LoreRules.unlocksOnDiamondChamberComplete(), LoreRules.unlocksOnDamnorathSacrifice(),
				LoreRules.unlocksOnEclipseEnd(), LoreRules.unlocksOnCryptCleared()}) {
			assertFalse(hook.isEmpty());
			state = apply(state, hook);
			int now = LoreCatalog.unlockedCount(state::isUnlocked);
			assertTrue(now > count, "el desbloqueo " + hook + " debe revelar entradas nuevas");
			count = now;
		}
		assertEquals(LoreCatalog.total() - 1, count, "solo queda bloqueado el juramento: pide sobrevivir a dos Eclipses");
		state = apply(state, LoreRules.unlocksOnEclipseSurvived(state::isUnlocked));
		assertEquals(LoreCatalog.total() - 1, LoreCatalog.unlockedCount(state::isUnlocked), "la primera supervivencia solo anota la marca");
		state = apply(state, LoreRules.unlocksOnEclipseSurvived(state::isUnlocked));
		assertEquals(LoreCatalog.total(), LoreCatalog.unlockedCount(state::isUnlocked), "la segunda revela el juramento");
	}

	@Test void theOathNeedsTwoSurvivalsNotJustOne() {
		LoreState once = new LoreState().unlockAll(LoreRules.unlocksOnEclipseSurvived(id -> false));
		assertTrue(once.isUnlocked(LoreRules.MARKER_ECLIPSE_SURVIVED));
		assertFalse(once.isUnlocked(LoreRules.LORE_UNFINISHED_OATH));
		LoreState twice = once.unlockAll(LoreRules.unlocksOnEclipseSurvived(once::isUnlocked));
		assertTrue(twice.isUnlocked(LoreRules.LORE_UNFINISHED_OATH));
		LoreState thrice = twice.unlockAll(LoreRules.unlocksOnEclipseSurvived(twice::isUnlocked));
		assertEquals(twice, thrice, "una tercera vez no cambia nada");
	}

	@Test void aRepeatedHookChangesNothing() {
		LoreState state = apply(new LoreState(), LoreRules.unlocksOnCryptCleared());
		assertSame(state, apply(state, LoreRules.unlocksOnCryptCleared()), "sin cambios no se reescribe ni se sincroniza");
	}

	@Test void unknownGuildmasterEventUnlocksNothing() {
		assertTrue(LoreRules.unlocksOnGuildmasterTalk("otro").isEmpty());
	}

	@Test void syncPayloadRoundTripsTheWholeState() {
		LoreState state = new LoreState();
		for (String id : LoreRules.validLoreIds()) state = state.unlock(id);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		LorePayload.CODEC.encode(buf, new LorePayload(state));
		LoreState read = LorePayload.CODEC.decode(buf).loreState();
		assertEquals(state.unlocked(), read.unlocked());
		assertEquals(LoreCatalog.unlockedCount(state::isUnlocked), LoreCatalog.unlockedCount(read::isUnlocked));
	}

	@Test void oversizedPayloadIsRejected() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeVarInt(1000);
		assertThrows(IllegalArgumentException.class, () -> LorePayload.CODEC.decode(buf));
	}

	@Test void everyIdFitsTheWireLimit() {
		Map<String, Boolean> all = new TreeMap<>();
		for (String id : LoreRules.validLoreIds()) {
			assertTrue(id.length() <= 64, id);
			all.put(id, true);
		}
		assertDoesNotThrow(() -> LorePayload.CODEC.encode(new PacketByteBuf(Unpooled.buffer()), new LorePayload(new LoreState(all))));
	}
}
