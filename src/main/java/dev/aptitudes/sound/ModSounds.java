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
	// Árbol de talentos (v65): sonidos metálicos de la interfaz.
	public static final SoundEvent UI_METAL_SELECT = register("ui_metal_select");
	public static final SoundEvent UI_METAL_UNLOCK = register("ui_metal_unlock");
	public static final SoundEvent UI_METAL_DENIED = register("ui_metal_denied");
	public static final SoundEvent RUNE_REMOVE = register("rune_remove");
	/** v46.1: un sonido por plantilla de piedra angular ({@code keystone_<plantilla>}). */
	public static final SoundEvent[] KEYSTONE = keystones();
	public static final SoundEvent ARENA_THEME = register("arena_theme");

	public static final SoundEvent ERRANTE_AMBIENT = register("errante_ambient");
	public static final SoundEvent ERRANTE_ALERT = register("errante_alert");
	public static final SoundEvent ERRANTE_PREPARE = register("errante_prepare");
	public static final SoundEvent ERRANTE_REND = register("errante_rend");
	public static final SoundEvent ERRANTE_HURT = register("errante_hurt");
	public static final SoundEvent ERRANTE_DEATH = register("errante_death");
	public static final SoundEvent ERRANTE_DESPAWN = register("errante_despawn");

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
