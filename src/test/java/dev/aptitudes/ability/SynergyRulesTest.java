package dev.aptitudes.ability;

import dev.aptitudes.role.Element;
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
		float bonus = SynergyRules.armorSynergyBonus(null, Role.PALADIN);
		assertEquals(1.0f, bonus, "Default bonus should be 1.0 (no player/armor)");
	}

	@Test
	void bonusValuesAreNonNegative() {
		// Todos los bonificadores deben ser ≥ 0.
		assertTrue(SynergyRules.radiusModifierForPath(null, Role.BERSERKER) >= 0.0f);
		assertTrue(SynergyRules.damageModifierForPath(null, Role.BERSERKER) >= 0.0f);
		assertTrue(SynergyRules.energyCostModifierForPath(null, Role.BERSERKER) >= 0.0f);
		assertTrue(SynergyRules.armorSynergyBonus(null, Role.BERSERKER) >= 0.0f);
	}

	@Test
	void contextForAbilityReturnsNoneWhenNoSynergies() {
		// Sin sinergias mapadas, debe retornar NONE.
		AbilitySynergyContext ctx = SynergyRules.contextForAbility(null, Role.BERSERKER, "any_ability");
		assertEquals(AbilitySynergyContext.NONE, ctx, "Should return NONE when no synergies apply");
	}

	@Test
	void groupResonanceBonusReturnsValidBonus() {
		// Y2: +15% XP si dos miembros comparten plantilla de piedra angular.
		// Placeholder: sin miembros de grupo aún.
		float bonus = SynergyRules.groupResonanceBonus(null);
		assertEquals(1.0f, bonus, "Default bonus should be 1.0 (no resonance yet)");
	}

	@Test
	void groupResonanceBonusIsNonNegative() {
		// Bonificador debe ser ≥ 1.0 (nunca penaliza).
		assertTrue(SynergyRules.groupResonanceBonus(null) >= 1.0f, "Bonus must be >= 1.0");
	}

	@Test
	void elementRuneSynergyBonusReturnsValidBonus() {
		// Y3: +20% daño si runa del mismo elemento potencia la fusión.
		// Placeholder: sin runas aún. Usar null para elemento (no bootstrappable en tests).
		float bonus = SynergyRules.elementRuneSynergyBonus(null, null, null);
		assertEquals(1.0f, bonus, "Default bonus should be 1.0 (no synergy)");
	}

	@Test
	void elementRuneSynergyBonusIsNonNegative() {
		// Bonificador debe ser ≥ 1.0 (nunca penaliza).
		assertTrue(SynergyRules.elementRuneSynergyBonus(null, null, null) >= 1.0f,
				"Bonus must be >= 1.0");
	}

	@Test
	void canTriggerKeystoneOnComboReturnsFalseByDefault() {
		// Y5: Sin lógica de cooldown aún, debe retornar false.
		boolean canTrigger = SynergyRules.canTriggerKeystoneOnCombo(null, "any_finisher");
		assertFalse(canTrigger, "Placeholder should return false");
	}

	@Test
	void keystoneComboTrigerCooldownReturnsValidTicks() {
		// Y5: Espera debe ser un número positivo de ticks (≥ 0).
		int cooldown = SynergyRules.keystoneComboTrigerCooldown();
		assertTrue(cooldown >= 0, "Cooldown must be >= 0 ticks");
		// 20 segundos = 400 ticks
		assertEquals(400, cooldown, "Default cooldown should be 20 seconds (400 ticks)");
	}
}
