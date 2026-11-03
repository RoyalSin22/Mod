package dev.aptitudes.party;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PartyRulesTest {
	@Test
	public void shareIsClampedAndRoundedDownToFivePercentSteps() {
		assertEquals(0, PartyRules.sharePercent(-5));
		assertEquals(10, PartyRules.sharePercent(14));
		assertEquals(50, PartyRules.sharePercent(75));
	}

	@Test
	public void sharedXpUsesLongArithmeticAndNeverExceedsTheSourceAmount() {
		assertEquals(0, PartyRules.sharedXp(-10, 50));
		assertEquals(12, PartyRules.sharedXp(25, 50));
		assertEquals(Integer.MAX_VALUE / 2, PartyRules.sharedXp(Integer.MAX_VALUE, 50));
	}

	@Test
	public void sourceKeepsFullXpAndEachOtherMemberReceivesAnAdditionalShareOnce() {
		assertEquals(125, PartyRules.memberXp(100, 25, true, 25));
		assertEquals(50, PartyRules.memberXp(100, 25, false, 25));
		assertEquals(Integer.MAX_VALUE, PartyRules.memberXp(Integer.MAX_VALUE, 50, true, 50));
	}

	@Test
	public void sourceXpAddsTheGroupBonusExactlyOnce() {
		assertEquals(125, PartyRules.sourceXp(100, 25));
		assertEquals(Integer.MAX_VALUE, PartyRules.sourceXp(Integer.MAX_VALUE, 25));
		assertEquals(0, PartyRules.sourceXp(-1, 25));
	}

	@Test
	public void partyBonusCountsConnectedMembersBeyondTheLeaderAndCaps() {
		assertEquals(0.0, PartyRules.bonus(0, 0.05, 0.20), 1e-9);
		assertEquals(0.0, PartyRules.bonus(1, 0.05, 0.20), 1e-9);
		assertEquals(0.15, PartyRules.bonus(4, 0.05, 0.20), 1e-9);
		assertEquals(0.20, PartyRules.bonus(5, 0.05, 0.20), 1e-9);
	}

	@Test
	public void malformedBonusValuesCannotCreateXp() {
		assertEquals(0, PartyRules.bonusXp(100, Double.NaN));
		assertEquals(0, PartyRules.bonusXp(100, -1));
	}
}
