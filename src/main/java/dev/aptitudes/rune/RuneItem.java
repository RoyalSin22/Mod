package dev.aptitudes.rune;

import net.minecraft.item.Item;

public final class RuneItem extends Item {
	private final RuneKind kind;
	public RuneItem(RuneKind kind, Settings settings) { super(settings); this.kind = kind; }
	public RuneKind kind() { return kind; }
}
