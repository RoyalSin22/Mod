package dev.aptitudes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * v60 (C3): Astrolabio del Ocaso — disco de bronce ennegrecido con sol eclipsado.
 *
 * <p>Consume en la Hoguera para invocar el Eclipse (solo si el jugador es operador o líder del grupo).
 * Uso: Hoguera → SoulsManager.trySummonEclipse() con verificación de autorización.</p>
 *
 * <p>TODO (C3 integración): wiring en souls/SoulsManager.onBonfireUse() o equivalente.</p>
 */
public final class SunsetAstralabeItem extends Item {
	public SunsetAstralabeItem(Settings settings) {
		super(settings);
	}

	@Override
	public ItemStack getDefaultStack() {
		ItemStack stack = super.getDefaultStack();
		return stack;
	}
}
