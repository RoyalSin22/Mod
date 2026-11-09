package dev.aptitudes.contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookRulesTest {
	@ParameterizedTest
	@CsvSource({"1, 2", "2, 4", "3, 6", "4, 8", "0, 2", "9, 8"})
	void unitPriceGrowsWithTierAndIsClamped(int tier, long expected) {
		assertEquals(expected, OrderBookRules.unitPrice(tier));
	}

	@Test void escrowIsCountTimesUnitPriceAndCountIsCapped() {
		assertEquals(3 * 4, OrderBookRules.escrow(3, 2));
		assertEquals(OrderBookRules.MAX_COUNT * 2L, OrderBookRules.escrow(999, 1));
	}

	@Test void netDeductsTheCommission() {
		// 10 % de 100 = 10 de comisión; 90 para quien entrega.
		assertEquals(90, OrderBookRules.net(100));
		assertEquals(0, OrderBookRules.net(0));
	}

	@Test void publishNeedsOnePerRequiredParticipantAndRespectsTheOpenLimit() {
		assertFalse(OrderBookRules.canPublish(3, 3, 0)); // nivel 3 exige 4 jugadores: 3 unidades no bastan
		assertTrue(OrderBookRules.canPublish(4, 3, 0));
		assertFalse(OrderBookRules.canPublish(1, 1, OrderBookRules.MAX_OPEN_PER_OWNER));
		assertFalse(OrderBookRules.canPublish(OrderBookRules.MAX_COUNT + 1, 1, 0));
	}

	@Test void aNewParticipantLeavesRoomForTheMissingOnes() {
		// Nivel 3 (4 jugadores), 4 unidades: el primero solo puede entregar 1.
		assertEquals(1, OrderBookRules.allowedQuantity(4, 0, 4, false, 10));
		// Quedan 3 unidades y 1 participante: un nuevo entrega 1 y deja 2 para los 2 cupos que faltan.
		assertEquals(1, OrderBookRules.allowedQuantity(3, 1, 4, false, 10));
		// Quien ya participa no puede tomar los cupos reservados: con 2 cupos y 2 unidades, entrega 0.
		assertEquals(0, OrderBookRules.allowedQuantity(2, 2, 4, true, 10));
	}

	@Test void aPlayerCannotMonopolizeAHighTierOrder() {
		// Simulación: un solo jugador intenta entregarlo todo. Solo entrega la primera unidad.
		int remaining = 4;
		int participants = 0;
		int delivered = OrderBookRules.allowedQuantity(remaining, participants, 4, false, 4);
		assertEquals(1, delivered);
		remaining -= delivered;
		participants++;
		// Ya participa: no puede tomar las unidades reservadas para los demás.
		assertEquals(0, OrderBookRules.allowedQuantity(remaining, participants, 4, true, 4));
	}

	@Test void fourDistinctPlayersCompleteATierThreeOrder() {
		int remaining = 4;
		int participants = 0;
		for (int i = 0; i < 4; i++) {
			int got = OrderBookRules.allowedQuantity(remaining, participants, 4, false, 1);
			assertEquals(1, got);
			remaining -= got;
			participants++;
		}
		assertEquals(0, remaining);
	}

	@Test void refundReturnsOnlyWhatWasNotDelivered() {
		assertEquals(3 * 2, OrderBookRules.refund(3, 1));
		assertEquals(0, OrderBookRules.refund(0, 4));
	}

	@Test void claimableIsWhatWasDeliveredAndNotYetCollected() {
		assertEquals(3, OrderBookRules.claimable(5, 2));
		assertEquals(0, OrderBookRules.claimable(2, 2));
		assertEquals(0, OrderBookRules.claimable(0, 0));
	}

	@Test void claimingNeedsAClosedOrder() {
		assertFalse(OrderBookRules.canClaim(false));
		assertTrue(OrderBookRules.canClaim(true));
	}

	@Test void anOrderIsRemovedOnlyWhenClosedAndFullyCollected() {
		assertFalse(OrderBookRules.removable(false, 4, 4));  // abierto: aún admite entregas
		assertFalse(OrderBookRules.removable(true, 4, 3));   // cerrado, pero falta recoger una unidad
		assertTrue(OrderBookRules.removable(true, 4, 4));
		assertTrue(OrderBookRules.removable(true, 0, 0));    // cancelado sin entregas
	}
}
