package dev.aptitudes.sound;

import dev.aptitudes.Aptitudes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Sonidos propios (v37). Los archivos .ogg se generan con {@code tools/audio/gen_sounds.py} (síntesis original,
 * semilla fija) y se declaran en {@code assets/aptitudes/sounds.json} con sus subtítulos.
 */
public final class ModSounds {
	public static final SoundEvent ECLIPSE_ANNOUNCE = register("eclipse_announce");
	public static final SoundEvent ECLIPSE_START = register("eclipse_start");
	public static final SoundEvent ECLIPSE_TOTALITY = register("eclipse_totality");
	public static final SoundEvent HERALD_SPAWN = register("herald_spawn");
	public static final SoundEvent COMBO_FINISHER = register("combo_finisher");
	public static final SoundEvent RUNE_SOCKET = register("rune_socket");
	public static final SoundEvent RUNE_REMOVE = register("rune_remove");
	/** v46.1: un sonido por plantilla de piedra angular ({@code keystone_<plantilla>}). */
	public static final SoundEvent[] KEYSTONE = keystones();

	private ModSounds() {
	}

	private static SoundEvent[] keystones() {
		dev.aptitudes.role.KeystoneRules.Template[] all = dev.aptitudes.role.KeystoneRules.Template.values();
		SoundEvent[] out = new SoundEvent[all.length];
		for (int i = 0; i < all.length; i++) out[i] = register("keystone_" + all[i].key());
		return out;
	}

	private static SoundEvent register(String name) {
		Identifier id = Aptitudes.id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	/** Fuerza la carga de la clase (y el registro) durante la inicialización del mod. */
	public static void init() {
	}
}
