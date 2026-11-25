package dev.aptitudes.item;

import net.minecraft.item.Item;

/**
 * Astrolabio del Ocaso (C3): disco de bronce ennegrecido con un sol eclipsado.
 *
 * <p>Se usa sobre la Hoguera ({@code BonfireBlock#onUseWithItem}) para convocar el Eclipse. Solo lo pueden usar los
 * operadores o el líder del grupo, y comparte con el Behelit la espera de {@code eclipseSummonCooldownDays} días
 * ({@code event/EclipseSummoningRules}). El objeto no tiene lógica propia: el ritual vive en la Hoguera.</p>
 */
public final class SunsetAstralabeItem extends Item {
	public SunsetAstralabeItem(Settings settings) {
		super(settings);
	}
}
