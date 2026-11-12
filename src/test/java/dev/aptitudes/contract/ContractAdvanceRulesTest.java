package dev.aptitudes.contract;

import dev.aptitudes.contract.ContractRules.Kind;
import dev.aptitudes.contract.ContractRules.Offer;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractAdvanceRulesTest {
	/** Tipos cuyas ofertas no tienen objetivo: se generan con −1 y solo avanzan con un suceso sin objetivo. */
	private static final Set<Kind> UNTARGETED = EnumSet.of(Kind.NETHER, Kind.NEW_MOB, Kind.EXPEDITION, Kind.FISHING,
			Kind.ENCHANTING, Kind.FORGE);

	@Test void aDifferentKindNeverAdvances() {
		assertFalse(ContractRules.advances(Kind.HUNT, 2, Kind.BOSS, 2));
		assertFalse(ContractRules.advances(Kind.NETHER, -1, Kind.NEW_MOB, -1));
	}

	@Test void aTargetedOfferAdvancesOnlyWithItsOwnTargetOrAnyTarget() {
		assertTrue(ContractRules.advances(Kind.HUNT, 3, Kind.HUNT, 3));
		assertFalse(ContractRules.advances(Kind.HUNT, 3, Kind.HUNT, 4));
		assertTrue(ContractRules.advances(Kind.HUNT, 3, Kind.HUNT, -1));
	}

	@Test void anUntargetedEventAdvancesAnUntargetedOffer() {
		assertTrue(ContractRules.advances(Kind.NETHER, -1, Kind.NETHER, -1));
		assertTrue(ContractRules.advances(Kind.NEW_MOB, -1, Kind.NEW_MOB, -1));
		assertTrue(ContractRules.advances(Kind.EXPEDITION, -1, Kind.EXPEDITION, -1));
	}

	/** La regresión: con objetivo 0 estas ofertas (objetivo −1) no avanzaban nunca. */
	@Test void zeroIsNotAWildcardForOffersWithoutTarget() {
		assertFalse(ContractRules.advances(Kind.NETHER, -1, Kind.NETHER, 0));
		assertFalse(ContractRules.advances(Kind.NEW_MOB, -1, Kind.NEW_MOB, 0));
	}

	@Test void everyGeneratedOfferOfAnUntargetedKindCanAdvanceWithAnUntargetedEvent() {
		int seen = 0;
		for (int day = 0; day < 400; day++) {
			UUID player = new UUID(day * 7919L, day * 104729L + 1);
			for (Offer offer : ContractRules.dailyOffers(player, day, 300, 2)) {
				if (!UNTARGETED.contains(offer.kind())) continue;
				seen++;
				assertTrue(offer.target() < 0, offer.kind() + " debe generarse sin objetivo");
				assertTrue(ContractRules.advances(offer.kind(), offer.target(), offer.kind(), -1));
			}
		}
		assertTrue(seen > 0, "la muestra debe incluir ofertas sin objetivo");
	}

	@Test void theNewKindsAppearInTheDailyOffers() {
		Set<Kind> found = EnumSet.noneOf(Kind.class);
		for (int day = 0; day < 600; day++) {
			UUID player = new UUID(day * 31L, day * 17L + 5);
			for (Offer offer : ContractRules.dailyOffers(player, day, 300, 2)) found.add(offer.kind());
		}
		assertTrue(found.contains(Kind.NETHER));
		assertTrue(found.contains(Kind.NEW_MOB));
		assertTrue(found.contains(Kind.EXPEDITION));
	}
}
