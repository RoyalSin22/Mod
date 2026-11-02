package dev.aptitudes.boss;

import net.minecraft.item.Item;

/** Material único de jefe, aplicable una vez a cada arma de evolución. */
public final class BossCoreItem extends Item {
	private final String coreId;
	public BossCoreItem(String coreId, Settings settings) { super(settings); this.coreId = coreId; }
	public String coreId() { return coreId; }
}
