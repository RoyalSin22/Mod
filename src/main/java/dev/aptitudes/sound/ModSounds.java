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

	public static final SoundEvent OSARIO_AMBIENT = register("osario_ambient");
	public static final SoundEvent OSARIO_ALERT = register("osario_alert");
	public static final SoundEvent OSARIO_HURT = register("osario_hurt");
	public static final SoundEvent OSARIO_DEATH = register("osario_death");
	public static final SoundEvent OSARIO_CLAW = register("osario_claw");
	public static final SoundEvent OSARIO_HEAVY = register("osario_heavy");
	public static final SoundEvent OSARIO_SLAM_CHARGE = register("osario_slam_charge");
	public static final SoundEvent OSARIO_SLAM_HIT = register("osario_slam_hit");
	public static final SoundEvent OSARIO_LUNGE = register("osario_lunge");
	public static final SoundEvent OSARIO_STEP = register("osario_step");
	// V77: efectos de batalla del Bersérker.
	public static final SoundEvent BERSERKER_ROAR = register("berserker_roar");
	public static final SoundEvent BERSERKER_WAR_CRY = register("berserker_war_cry");
	public static final SoundEvent BERSERKER_SLAM = register("berserker_slam");
	public static final SoundEvent BERSERKER_WHOOSH = register("berserker_whoosh");
	public static final SoundEvent BERSERKER_HEARTBEAT = register("berserker_heartbeat");
	public static final SoundEvent BERSERKER_LEAP = register("berserker_leap");
	// V83: efectos de batalla del Mago.
	public static final SoundEvent MAGE_CHARGE = register("mage_charge");
	public static final SoundEvent MAGE_BOLT = register("mage_bolt");
	public static final SoundEvent MAGE_BLINK = register("mage_blink");
	public static final SoundEvent MAGE_TIME = register("mage_time");
	public static final SoundEvent MAGE_MISSILE = register("mage_missile");
	public static final SoundEvent MAGE_IMPACT = register("mage_impact");
	public static final SoundEvent ASSASSIN_VANISH = register("assassin_vanish");
	public static final SoundEvent ASSASSIN_SLASH = register("assassin_slash");
	public static final SoundEvent ASSASSIN_STEP = register("assassin_step");
	public static final SoundEvent ASSASSIN_THROW = register("assassin_throw");
	public static final SoundEvent ASSASSIN_IMPACT = register("assassin_impact");
	public static final SoundEvent HERBALIST_BALM = register("herbalist_balm");
	public static final SoundEvent HERBALIST_RAIN = register("herbalist_rain");
	public static final SoundEvent HERBALIST_SPORE = register("herbalist_spore");
	public static final SoundEvent ENGINEER_GRAPPLE = register("engineer_grapple");
	public static final SoundEvent ENGINEER_THROW = register("engineer_throw");
	public static final SoundEvent ENGINEER_BLAST = register("engineer_blast");
	public static final SoundEvent LANCER_LEAP = register("lancer_leap");
	public static final SoundEvent LANCER_THRUST = register("lancer_thrust");
	public static final SoundEvent LANCER_SLAM = register("lancer_slam");
	public static final SoundEvent SWORDSMAN_DRAW = register("swordsman_draw");
	public static final SoundEvent SWORDSMAN_CUT = register("swordsman_cut");
	public static final SoundEvent PALADIN_CONSECRATE = register("paladin_consecrate");
	public static final SoundEvent PALADIN_HAMMER = register("paladin_hammer");
	public static final SoundEvent NECROMANCER_SPEAR = register("necromancer_spear");
	public static final SoundEvent NECROMANCER_SIPHON = register("necromancer_siphon");
	public static final SoundEvent DRUID_HOWL = register("druid_howl");
	public static final SoundEvent DRUID_THORNS = register("druid_thorns");
	public static final SoundEvent MERCHANT_COIN = register("merchant_coin");
	public static final SoundEvent MERCHANT_GUARD = register("merchant_guard");
	public static final SoundEvent MONK_KICK = register("monk_kick");
	public static final SoundEvent MONK_FIST = register("monk_fist");
	public static final SoundEvent GUNSLINGER_SHOT = register("gunslinger_shot");
	public static final SoundEvent GUNSLINGER_MARK = register("gunslinger_mark");
	public static final SoundEvent WEAPON_GREATSWORD = register("weapon_greatsword");
	public static final SoundEvent WEAPON_PRISON = register("weapon_prison");
	public static final SoundEvent SHAPESHIFTER_POUNCE = register("shapeshifter_pounce");
	public static final SoundEvent SHAPESHIFTER_HOWL = register("shapeshifter_howl");
	public static final SoundEvent HUNTER_SHOT = register("hunter_shot");
	public static final SoundEvent HUNTER_TRAP = register("hunter_trap");
	public static final SoundEvent HUNTER_FALCON = register("hunter_falcon");
	public static final SoundEvent BARD_SONG = register("bard_song");
	public static final SoundEvent BARD_DISSONANCE = register("bard_dissonance");
	public static final SoundEvent WARLOCK_CURSE = register("warlock_curse");
	public static final SoundEvent WARLOCK_BOLT = register("warlock_bolt");
	public static final SoundEvent WARLOCK_IMPS = register("warlock_imps");
	public static final SoundEvent WARLOCK_DOOM = register("warlock_doom");
	public static final SoundEvent TAMER_COMMAND = register("tamer_command");
	public static final SoundEvent TAMER_STAMPEDE = register("tamer_stampede");
	public static final SoundEvent ELEMENT_FIRE = register("element_fire");
	public static final SoundEvent ELEMENT_ICE = register("element_ice");
	public static final SoundEvent ELEMENT_LIGHTNING = register("element_lightning");
	public static final SoundEvent ELEMENT_EARTH = register("element_earth");
	public static final SoundEvent ELEMENT_WIND = register("element_wind");
	public static final SoundEvent ELEMENT_WATER = register("element_water");
	public static final SoundEvent ELEMENT_MAGIC = register("element_magic");
	public static final SoundEvent ELEMENT_DARK = register("element_dark");
	public static final SoundEvent FEEDBACK_HEAVY = register("feedback_heavy");
	public static final SoundEvent FEEDBACK_CRIT = register("feedback_crit");
	public static final SoundEvent FEEDBACK_BLOCK = register("feedback_block");
	public static final SoundEvent FEEDBACK_HEAL = register("feedback_heal");
	public static final SoundEvent FEEDBACK_DENIED = register("feedback_denied");

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
