package dev.aptitudes.contract;

import dev.aptitudes.contract.ContractRules.Difficulty;
import dev.aptitudes.contract.ContractRules.Kind;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** La Niebla de almas conserva su ordinal guardado aunque se añadan tipos al final. */
class SoulFogContractRulesTest {
	@Test void kindIsAppendedAtTheEndSoSavedContractsKeepTheirOrdinals() {
		Kind[] kinds = Kind.values();
		assertEquals(Kind.CAMP_CLEAR, kinds[kinds.length - 1]);
		assertEquals(10, Kind.EXPEDITION.ordinal(), "los anteriores no se mueven");
		assertEquals(11, Kind.SOUL_FOG.ordinal());
	}

	@Test void onlyNormalOrHarderDifficultiesOfferIt() {
		assertFalse(ContractRules.kindsFor(Difficulty.EASY).contains(Kind.SOUL_FOG));
		assertFalse(ContractRules.kindsFor(Difficulty.LEGENDARY).contains(Kind.SOUL_FOG));
		for (Difficulty d : new Difficulty[] {Difficulty.NORMAL, Difficulty.HARD, Difficulty.ELITE}) {
			assertTrue(ContractRules.kindsFor(d).contains(Kind.SOUL_FOG), d.name());
		}
	}

	@Test void itNeedsASingleSuccessAndHasNoTarget() {
		for (Difficulty d : Difficulty.values()) assertEquals(1, ContractRules.goal(Kind.SOUL_FOG, d, 2));
		// Sin objetivo (−1): cualquier Niebla superada lo hace avanzar.
		assertTrue(ContractRules.advances(Kind.SOUL_FOG, -1, Kind.SOUL_FOG, -1));
		assertFalse(ContractRules.advances(Kind.SOUL_FOG, -1, Kind.EVENT, 1));
	}

	@Test void offersWithItAreGeneratedWithoutATargetAndDeterministically() {
		boolean seen = false;
		for (long day = 0; day < 400; day++) {
			UUID player = new UUID(7, 11);
			var offers = ContractRules.dailyOffers(player, day, 120, 0);
			assertEquals(offers, ContractRules.dailyOffers(player, day, 120, 0));
			for (var o : offers) {
				if (o.kind() != Kind.SOUL_FOG) continue;
				seen = true;
				assertEquals(-1, o.target());
				assertEquals(1, o.goal());
			}
		}
		assertTrue(seen, "sale con frecuencia razonable en 400 días");
	}
}
