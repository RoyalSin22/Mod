package dev.aptitudes.ability;

import dev.aptitudes.role.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * v65 (C5 Y1+Y4): Pruebas de sinergias senda ↔ activa y armadura ↔ senda.
 */
class SynergyRulesTest {

	@Test
	void radiusModifierForPathReturnsValidBonus() {
		// Y1: La senda modifica el radio de las activas.
		// Placeholder: sin endpoints mapeados aún.
		float modifier = SynergyRules.radiusModifierForPath(null, Role.BERSERKER);
		assertEquals(1.0f, modifier, "Default modifier should be 1.0 (no change)");
	}

	@Test
	void damageModifierForPathReturnsValidBonus() {
		// Y1: La senda modifica daño de activas.
		float modifier = SynergyRules.damageModifierForPath(null, Role.BERSERKER);
		assertEquals(1.0f, modifier, "Default modifier should be 1.0");
	}

	@Test
	void energyCostModifierReturnsValidBonus() {
		// Y1: Eficiencia reduce coste energético.
		float modifier = SynergyRules.energyCostModifierForPath(null, Role.BERSERKER);
		assertEquals(1.0f, modifier, "Default modifier should be 1.0");
	}

	@Test
	void armorSynergyBonusReturnsValidBonus() {
		// Y4: Armadura + senda coincidente = bonificación.
		float bonus = SynergyRules.armorSynergyBonus(null, "paladin", "defensa");
		assertEquals(1.0f, bonus, "Default bonus should be 1.0 (no synergy yet)");
	}

	@Test
	void bonusValuesAreNonNegative() {
		// Todos los bonificadores deben ser ≥ 0.
		assertTrue(SynergyRules.radiusModifierForPath(null, Role.BERSERKER) >= 0.0f);
		assertTrue(SynergyRules.damageModifierForPath(null, Role.BERSERKER) >= 0.0f);
		assertTrue(SynergyRules.energyCostModifierForPath(null, Role.BERSERKER) >= 0.0f);
		assertTrue(SynergyRules.armorSynergyBonus(null, "any", "any") >= 0.0f);
	}

	@Test
	void contextForAbilityReturnsNoneWhenNoSynergies() {
		// Sin sinergias mapadas, debe retornar NONE.
		AbilitySynergyContext ctx = SynergyRules.contextForAbility(null, Role.BERSERKER, "any_ability");
		assertEquals(AbilitySynergyContext.NONE, ctx, "Should return NONE when no synergies apply");
	}
}
