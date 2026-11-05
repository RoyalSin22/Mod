package dev.aptitudes.contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.aptitudes.contract.ContractRules.Difficulty;
import dev.aptitudes.contract.ContractRules.Kind;
import dev.aptitudes.contract.ContractRules.Offer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Guardado de contratos con el codec real, sin cargar registros de Minecraft. */
class ContractDataCodecTest {
	@Test
	void roundTripPreservesOffersProgressReputationAndCompletedCount() {
		ContractData original = new ContractData();
		original.refreshDay(42, 2);
		Offer claimed = new Offer(Kind.HUNT, Difficulty.HARD, 3, 10, 8, 0, 45, 350, false, false);
		assertTrue(original.accept(0, claimed));
		assertTrue(original.progress(0, claimed.goal()));
		assertEquals(claimed, original.claim(0));
		Offer elite = new Offer(Kind.DELIVERY, Difficulty.ELITE, 4, 64, 10, 2, 55, 50, true, false);
		Offer boss = new Offer(Kind.BOSS, Difficulty.LEGENDARY, 2, 1, 18, 3, 65, 60, false, true);
		assertTrue(original.accept(1, elite));
		assertTrue(original.accept(2, boss));
		assertTrue(original.progress(0, 27));
		assertTrue(original.progress(1, 1));

		ContractData loaded = reload(original);
		assertEquals(350, loaded.reputation());
		assertEquals(42, loaded.offerDay());
		assertEquals(0, loaded.offerReputation());
		assertEquals(2, loaded.offerEvolution());
		assertEquals(1, loaded.completed());
		assertEquals(original.active(), loaded.active());
		for (int index = 0; index < ContractRules.OFFERS_PER_DAY; index++) assertTrue(loaded.isTaken(index));
		assertEquals(encode(original), encode(loaded));
	}

	@Test
	void claimedOfferCannotBeAcceptedOrClaimedAgainAfterReload() {
		ContractData data = new ContractData();
		data.refreshDay(7, 0);
		Offer offer = new Offer(Kind.FISHING, Difficulty.EASY, -1, 5, 4, 0, 25, 20, false, false);
		assertTrue(data.accept(0, offer));
		assertTrue(data.progress(0, Integer.MAX_VALUE));
		data = reload(data);
		assertEquals(offer, data.claim(0));
		data = reload(data);
		data.refreshDay(7, 2);
		assertNull(data.claim(0));
		assertFalse(data.accept(0, offer));
		assertEquals(20, data.reputation());
		assertEquals(1, data.completed());
		assertEquals(0, data.offerEvolution());
	}

	@Test
	void nextDayPreservesActiveOfferButRefreshesDailySnapshot() {
		ContractData data = new ContractData();
		data.refreshDay(8, 0);
		Offer offer = new Offer(Kind.PROFESSION, Difficulty.EASY, 9, 150, 4, 0, 25, 20, false, false);
		assertTrue(data.accept(2, offer));
		assertTrue(data.progress(0, 67));
		data = reload(data);
		data.refreshDay(9, 2);
		data = reload(data);
		assertEquals(9, data.offerDay());
		assertEquals(2, data.offerEvolution());
		assertEquals(new ContractData.Active(offer, 67), data.active().getFirst());
		assertFalse(data.isTaken(2));
		assertNull(data.claim(0));
	}

	@Test
	void emptyJsonUsesSafeDefaults() {
		ContractData data = decode(new JsonObject());
		assertEquals(0, data.reputation());
		assertEquals(Long.MIN_VALUE, data.offerDay());
		assertEquals(0, data.offerReputation());
		assertEquals(0, data.offerEvolution());
		assertEquals(0, data.completed());
		assertTrue(data.active().isEmpty());
		for (int i = 0; i < ContractRules.OFFERS_PER_DAY; i++) assertFalse(data.isTaken(i));
		assertEquals(encode(data), encode(reload(data)));
	}

	@Test
	void corruptScalarsAndTakenIndicesAreClampedAndDeduplicated() {
		JsonObject json = JsonParser.parseString("""
				{"reputation": -2147483648, "offer_day": 23, "offer_reputation": -1,
				 "offer_evolution": -2, "completed": -3,
				 "taken": [-2147483648, -1, 0, 0, 1, 2, 2, 3, 2147483647]}
				""").getAsJsonObject();
		ContractData data = decode(json);
		assertEquals(0, data.reputation());
		assertEquals(23, data.offerDay());
		assertEquals(0, data.offerReputation());
		assertEquals(0, data.offerEvolution());
		assertEquals(0, data.completed());
		assertFalse(data.isTaken(-1));
		assertFalse(data.isTaken(ContractRules.OFFERS_PER_DAY));
		assertEquals(JsonParser.parseString("[0,1,2]"), encode(data).getAsJsonObject().get("taken"));
		assertEquals(encode(data), encode(reload(data)));
	}

	@Test
	void corruptOfferEnumsRewardsAndProgressAreBounded() {
		JsonObject first = rawActive(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
		JsonObject second = rawActive(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
		JsonArray active = new JsonArray();
		active.add(first);
		active.add(second);
		JsonObject json = new JsonObject();
		json.add("active", active);
		ContractData data = decode(json);
		assertEquals(2, data.active().size());
		assertEquals(new Offer(Kind.HUNT, Difficulty.LEGENDARY, -1, 1, 0, 0, 0, 0, true, true),
				data.active().get(0).offer());
		assertEquals(Kind.BOSS, data.active().get(1).offer().kind());
		assertEquals(Difficulty.EASY, data.active().get(1).offer().difficulty());
		assertEquals(0, data.active().get(0).progress());
		assertEquals(1, data.active().get(1).progress());
		assertEquals(encode(data), encode(reload(data)));
	}

	@Test
	void overlongActiveListKeepsOnlyFirstThreeAndBoundsEachProgress() {
		JsonArray active = new JsonArray();
		for (int i = 0; i < ContractRules.MAX_ACTIVE + 5; i++) {
			JsonObject entry = rawActive(0, 0, i == 0 ? -5 : Integer.MAX_VALUE);
			entry.getAsJsonObject("offer").addProperty("target", i);
			entry.getAsJsonObject("offer").addProperty("goal", 10);
			active.add(entry);
		}
		JsonObject json = new JsonObject();
		json.add("active", active);
		ContractData data = decode(json);
		assertEquals(ContractRules.MAX_ACTIVE, data.active().size());
		for (int i = 0; i < ContractRules.MAX_ACTIVE; i++) {
			assertEquals(i, data.active().get(i).offer().target());
			assertEquals(i == 0 ? 0 : 10, data.active().get(i).progress());
		}
		assertEquals(encode(data), encode(reload(data)));
	}

	@Test
	void missingProgressDefaultsToZeroWithoutLosingOffer() {
		JsonObject active = rawActive(0, 0, 9);
		active.remove("progress");
		JsonArray entries = new JsonArray();
		entries.add(active);
		JsonObject json = new JsonObject();
		json.add("active", entries);
		ContractData data = decode(json);
		assertEquals(1, data.active().size());
		assertEquals(0, data.active().getFirst().progress());
		assertFalse(data.active().getFirst().complete());
	}

	private static JsonObject rawActive(int kind, int difficulty, int progress) {
		JsonObject entry = JsonParser.parseString("""
				{"offer": {"kind": 0, "difficulty": 0, "target": -1, "goal": -1,
				 "emeralds": -2, "fragments": -3, "role_xp": -4, "reputation": -5,
				 "voucher": true, "emblem": true}, "progress": 0}
				""").getAsJsonObject();
		entry.getAsJsonObject("offer").addProperty("kind", kind);
		entry.getAsJsonObject("offer").addProperty("difficulty", difficulty);
		entry.addProperty("progress", progress);
		return entry;
	}

	private static JsonElement encode(ContractData data) {
		return ContractData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
	}

	private static ContractData decode(JsonElement json) {
		return ContractData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}

	private static ContractData reload(ContractData data) {
		// Texto JSON intermedio: la copia no comparte objetos ni listas con el original.
		return decode(JsonParser.parseString(encode(data).toString()));
	}
}
