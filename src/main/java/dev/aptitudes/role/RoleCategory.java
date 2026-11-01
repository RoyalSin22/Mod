package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Categorías de roles. Además de agrupar la pantalla de roles, dan una sinergia:
 * <ul>
 *   <li><b>Especialización</b>: principal y secundario de la MISMA categoría → +15 % de XP de rol.</li>
 *   <li><b>Versatilidad</b>: de categorías DISTINTAS → +15 de energía máxima.</li>
 * </ul>
 */
public enum RoleCategory {
	MARTIAL(0xC0392B),
	ARCANE(0x8E44AD),
	SHADOW(0x34495E),
	GUARDIAN(0x27AE60),
	CRAFT(0xD35400);

	public static final double SPECIALIZATION_XP_BONUS = 0.15;
	public static final double VERSATILITY_ENERGY_BONUS = 15.0;

	private final int color;

	RoleCategory(int color) {
		this.color = color;
	}

	public int color() {
		return color;
	}

	public String translationKey() {
		return "category." + Aptitudes.MOD_ID + "." + name().toLowerCase(Locale.ROOT);
	}

	public List<Role> roles() {
		return Arrays.stream(Role.values()).filter(r -> r.category() == this).toList();
	}
}
