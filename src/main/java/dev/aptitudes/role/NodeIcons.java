package dev.aptitudes.role;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Icono (ítem vanilla) de cada nodo, característica y definitiva. Lo usan el HUD
 * y el árbol para que cada habilidad sea reconocible de un vistazo.
 */
public final class NodeIcons {
	private static final Map<String, Item> BY_NODE = new HashMap<>();
	private static final Map<PathEffect, Item> BY_EFFECT = new EnumMap<>(PathEffect.class);
	private static final Map<Role, Item> SIGNATURES = new EnumMap<>(Role.class);
	private static final Map<Role, Item> ULTIMATES = new EnumMap<>(Role.class);

	static {
		put("tough_skin", Items.LEATHER_CHESTPLATE); put("rage", Items.BLAZE_POWDER);
		put("bloodlust", Items.REDSTONE); put("war_cry", Items.GOAT_HORN);
		put("arcane_affinity", Items.LAPIS_LAZULI); put("fireball", Items.AMETHYST_SHARD);
		put("clear_mind", Items.CLOCK); put("blink", Items.ENDER_PEARL);
		put("shadow", Items.BLACK_DYE); put("vanish", Items.GLASS_BOTTLE);
		put("backstab", Items.IRON_SWORD); put("shadow_step", Items.ENDER_EYE);
		put("vital_sap", Items.GLISTERING_MELON_SLICE); put("balm", Items.HONEY_BOTTLE);
		put("herbal_immunity", Items.MILK_BUCKET); put("poison_thorns", Items.POISONOUS_POTATO);
		put("engineering", Items.PISTON); put("thrusters", Items.FIREWORK_ROCKET);
		put("battery", Items.REDSTONE_BLOCK); put("turret", Items.CARVED_PUMPKIN);
		put("reach", Items.STICK); put("lunge", Items.TRIDENT);
		put("charge", Items.SADDLE); put("sweep", Items.IRON_AXE);
		put("keen_edge", Items.GRINDSTONE); put("blade_dance", Items.SUGAR);
		put("parry", Items.SHIELD); put("riposte", Items.NETHERITE_SWORD);
		put("holy_smite", Items.GOLDEN_SWORD); put("divine_shield", Items.GOLDEN_APPLE);
		put("protective_aura", Items.BEACON); put("judgement", Items.BELL);
		put("dark_pact", Items.SOUL_LANTERN); put("raise_minions", Items.ZOMBIE_HEAD);
		put("life_drain", Items.FERMENTED_SPIDER_EYE); put("death_touch", Items.WITHER_ROSE);
		put("forest_child", Items.OAK_SAPLING); put("entangling_roots", Items.HANGING_ROOTS);
		put("growth", Items.BONE_MEAL); put("bear_form", Items.RABBIT_HIDE);
		put("haggling", Items.EMERALD); put("appraiser_eye", Items.SPYGLASS);
		put("deep_pockets", Items.BUNDLE); put("bribe", Items.GOLD_INGOT);
		put("iron_fists", Items.IRON_INGOT); put("chi_palm", Items.WIND_CHARGE);
		put("evasion", Items.FEATHER); put("meditation", Items.CANDLE);
		put("elemental_affinity", Items.PRISMARINE_CRYSTALS); put("elemental_bolt", Items.BLAZE_ROD);
		put("elemental_dominion", Items.HEART_OF_THE_SEA); put("elemental_burst", Items.FIRE_CHARGE);
		put("mimicry", Items.SLIME_BALL); put("transform", Items.CREEPER_HEAD);
		put("trickster", Items.JACK_O_LANTERN); put("form_power", Items.GUNPOWDER);
		// Roles nuevos (v11)
		put("quickdraw", Items.FEATHER); put("arcane_shot", Items.PRISMARINE_CRYSTALS);
		put("eagle_eye", Items.SPYGLASS); put("fan_the_hammer", Items.FIREWORK_STAR);
		put("arcane_cylinder", Items.HEART_OF_THE_SEA); put("dodge_roll", Items.RABBIT_FOOT);
		put("reload", Items.GUNPOWDER); put("ricochet", Items.AMETHYST_SHARD);
		put("deadeye", Items.ENDER_EYE); put("armory", Items.IRON_SWORD);
		put("blade_volley", Items.GOLDEN_SWORD); put("blade_guard", Items.IRON_CHESTPLATE);
		put("sword_rain", Items.DIAMOND_SWORD); put("floating_arsenal", Items.NETHERITE_SWORD);
		put("intercepting_blade", Items.SHIELD); put("reforge", Items.ANVIL);
		put("spectral_greatsword", Items.STONE_SWORD); put("blade_prison", Items.CHAIN);
		put("predator", Items.BOW); put("hunters_mark", Items.TARGET);
		put("tracker", Items.COMPASS); put("bear_trap", Items.TRIPWIRE_HOOK);
		put("the_hunt", Items.CROSSBOW); put("camouflage", Items.FERN);
		put("trophy", Items.LEATHER); put("arrow_volley", Items.SPECTRAL_ARROW);
		put("falcon_strike", Items.FEATHER); put("inspiring_presence", Items.BELL);
		put("song_of_valor", Items.GOAT_HORN); put("soothing_melody", Items.NOTE_BLOCK);
		put("dissonance", Items.JUKEBOX); put("crescendo", Items.MUSIC_DISC_CAT);
		put("encore", Items.MUSIC_DISC_13); put("lullaby", Items.WHITE_WOOL);
		put("ballad_of_shielding", Items.SHIELD); put("requiem", Items.MUSIC_DISC_11);
		put("dark_bargain", Items.NETHER_WART); put("curse_of_agony", Items.CRIMSON_FUNGUS);
		put("soul_link", Items.CHAIN); put("abyssal_bolt", Items.ENDER_PEARL);
		put("demonic_pact", Items.REDSTONE); put("demonic_ward", Items.CRYING_OBSIDIAN);
		put("soul_feast", Items.SOUL_TORCH); put("summon_imps", Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
		put("doom", Items.WITHER_SKELETON_SKULL); put("beast_bond", Items.LEAD);
		put("sic_em", Items.BONE); put("pack_leader", Items.WOLF_ARMOR);
		put("vital_bond", Items.GOLDEN_CARROT); put("companion", Items.NAME_TAG);
		put("protective_instinct", Items.TURTLE_HELMET); put("feral_feast", Items.MUTTON);
		put("stampede", Items.GOAT_HORN); put("alpha_roar", Items.SADDLE);
		// Mecánicas, reactivas y activas C/D
		put("frenzy", Items.REDSTONE_TORCH); put("unbreakable", Items.OBSIDIAN);
		put("carnage", Items.BEEF); put("leap_slam", Items.ANVIL);
		put("whirlwind", Items.IRON_AXE);
		// v67 (F2): iconos provisionales de las habilidades nuevas del Bersérker (hasta tener los PNG).
		put("berserker_n1", Items.CHAINMAIL_CHESTPLATE); put("berserker_n2", Items.FIRE_CHARGE);
		put("berserker_n3", Items.GOLDEN_AXE); put("berserker_n4", Items.CHAIN);
		put("berserker_n5", Items.BELL); put("berserker_n6", Items.SHEARS);
		put("berserker_n7", Items.REDSTONE_BLOCK); put("berserker_n8", Items.NETHERITE_CHESTPLATE);
		put("berserker_n9", Items.ECHO_SHARD); put("berserker_n10", Items.MACE);
		// v67 (F3): iconos provisionales de Mago, Asesino, Herbolista e Ingeniero (hasta tener los PNG).
		put("mage_n1", Items.AMETHYST_SHARD); put("mage_n2", Items.BLUE_ICE);
		put("mage_n3", Items.LIGHTNING_ROD); put("mage_n4", Items.SHIELD);
		put("mage_n5", Items.CHAIN); put("mage_n6", Items.SNOWBALL);
		put("mage_n7", Items.EXPERIENCE_BOTTLE); put("mage_n8", Items.END_CRYSTAL);
		put("mage_n9", Items.PRISMARINE_CRYSTALS); put("mage_n10", Items.NETHER_STAR);
		put("assassin_n1", Items.SPIDER_EYE); put("assassin_n2", Items.LEAD);
		put("assassin_n3", Items.GUNPOWDER); put("assassin_n4", Items.STONE_SWORD);
		put("assassin_n5", Items.BLACK_DYE); put("assassin_n6", Items.ENDER_PEARL);
		put("assassin_n7", Items.POISONOUS_POTATO); put("assassin_n8", Items.PHANTOM_MEMBRANE);
		put("assassin_n9", Items.WITHER_ROSE); put("assassin_n10", Items.SCULK);
		put("herbalist_n1", Items.AZURE_BLUET); put("herbalist_n2", Items.HONEY_BOTTLE);
		put("herbalist_n3", Items.BEETROOT_SEEDS); put("herbalist_n4", Items.HANGING_ROOTS);
		put("herbalist_n5", Items.STRIPPED_OAK_LOG); put("herbalist_n6", Items.FERMENTED_SPIDER_EYE);
		put("herbalist_n7", Items.MILK_BUCKET); put("herbalist_n8", Items.SPORE_BLOSSOM);
		put("herbalist_n9", Items.MANGROVE_ROOTS); put("herbalist_n10", Items.GLOW_BERRIES);
		put("engineer_n1", Items.COPPER_INGOT); put("engineer_n2", Items.COMPOSTER);
		put("engineer_n3", Items.STONE_PRESSURE_PLATE); put("engineer_n4", Items.FIREWORK_ROCKET);
		put("engineer_n5", Items.MAGMA_BLOCK); put("engineer_n6", Items.GLASS);
		put("engineer_n7", Items.CLOCK); put("engineer_n8", Items.IRON_CHESTPLATE);
		put("engineer_n9", Items.STICKY_PISTON); put("engineer_n10", Items.BEACON);
		// v67 F4: iconos provisionales (objetos del juego) hasta que llegue el arte de docs/PROMPT_ARTE_ICONOS_F4.md.
		put("lancer_n1", Items.SPYGLASS); put("lancer_n2", Items.TARGET); put("lancer_n3", Items.TRIDENT);
		put("lancer_n4", Items.BAMBOO); put("lancer_n5", Items.ARROW); put("lancer_n6", Items.SPECTRAL_ARROW);
		put("lancer_n7", Items.IRON_NUGGET); put("lancer_n8", Items.NETHERITE_SCRAP); put("lancer_n9", Items.STICK);
		put("lancer_n10", Items.TIPPED_ARROW);
		put("swordsman_n1", Items.IRON_SWORD); put("swordsman_n2", Items.IRON_NUGGET); put("swordsman_n3", Items.FEATHER);
		put("swordsman_n4", Items.COMPASS); put("swordsman_n5", Items.GOLDEN_SWORD); put("swordsman_n6", Items.RABBIT_FOOT);
		put("swordsman_n7", Items.SHIELD); put("swordsman_n8", Items.CLOCK); put("swordsman_n9", Items.DIAMOND_SWORD);
		put("swordsman_n10", Items.NETHERITE_SWORD);
		put("paladin_n1", Items.GLOWSTONE_DUST); put("paladin_n2", Items.GOLDEN_APPLE); put("paladin_n3", Items.END_ROD);
		put("paladin_n4", Items.IRON_CHESTPLATE); put("paladin_n5", Items.BOOK); put("paladin_n6", Items.GLASS);
		put("paladin_n7", Items.SHIELD); put("paladin_n8", Items.WHITE_BANNER); put("paladin_n9", Items.BLAZE_POWDER);
		put("paladin_n10", Items.SEA_LANTERN);
		put("necromancer_n1", Items.BONE); put("necromancer_n2", Items.ROTTEN_FLESH); put("necromancer_n3", Items.SKELETON_SKULL);
		put("necromancer_n4", Items.TNT); put("necromancer_n5", Items.SPIDER_EYE); put("necromancer_n6", Items.SOUL_SOIL);
		put("necromancer_n7", Items.TOTEM_OF_UNDYING); put("necromancer_n8", Items.WITHER_ROSE); put("necromancer_n9", Items.BONE_BLOCK);
		put("necromancer_n10", Items.SOUL_LANTERN);
		// v67 F5: iconos provisionales (objetos del juego) hasta que llegue el arte de docs/PROMPT_ARTE_ICONOS_F5.md.
		put("druid_n1", Items.GRASS_BLOCK); put("druid_n2", Items.BONE); put("druid_n3", Items.VINE);
		put("druid_n4", Items.OAK_SAPLING); put("druid_n5", Items.HONEY_BOTTLE); put("druid_n6", Items.BROWN_DYE);
		put("druid_n7", Items.OAK_LOG); put("druid_n8", Items.WOLF_SPAWN_EGG); put("druid_n9", Items.CLOCK);
		put("druid_n10", Items.MOSS_BLOCK);
		put("merchant_n1", Items.GOLD_NUGGET); put("merchant_n2", Items.EMERALD); put("merchant_n3", Items.GOLD_INGOT);
		put("merchant_n4", Items.NAME_TAG); put("merchant_n5", Items.PAPER); put("merchant_n6", Items.SADDLE);
		put("merchant_n7", Items.WRITABLE_BOOK); put("merchant_n8", Items.EMERALD_BLOCK); put("merchant_n9", Items.SKELETON_SKULL);
		put("merchant_n10", Items.TNT_MINECART);
		put("monk_n1", Items.LEATHER); put("monk_n2", Items.CHAIN); put("monk_n3", Items.LEATHER_BOOTS);
		put("monk_n4", Items.HEART_OF_THE_SEA); put("monk_n5", Items.LILY_OF_THE_VALLEY); put("monk_n6", Items.FIRE_CHARGE);
		put("monk_n7", Items.ACACIA_BUTTON); put("monk_n8", Items.ORANGE_DYE); put("monk_n9", Items.ECHO_SHARD);
		put("monk_n10", Items.ENDER_EYE);
		put("elementalist_n1", Items.PRISMARINE_SHARD); put("elementalist_n2", Items.AMETHYST_SHARD);
		put("elementalist_n3", Items.BLAZE_ROD); put("elementalist_n4", Items.PACKED_ICE); put("elementalist_n5", Items.REDSTONE);
		put("elementalist_n6", Items.TOTEM_OF_UNDYING); put("elementalist_n7", Items.NETHER_STAR);
		put("elementalist_n8", Items.DRAGON_BREATH); put("elementalist_n9", Items.MUSIC_DISC_CAT);
		put("elementalist_n10", Items.END_CRYSTAL);
		// v67 F6: iconos provisionales (objetos del juego) hasta que llegue el arte de docs/PROMPT_ARTE_ICONOS_F6.md.
		put("shapeshifter_n1", Items.GOLDEN_CARROT); put("shapeshifter_n2", Items.SNOWBALL); put("shapeshifter_n3", Items.RABBIT_FOOT);
		put("shapeshifter_n4", Items.ENDER_PEARL); put("shapeshifter_n5", Items.LEATHER_CHESTPLATE); put("shapeshifter_n6", Items.FLINT);
		put("shapeshifter_n7", Items.BEEF); put("shapeshifter_n8", Items.BLAZE_POWDER); put("shapeshifter_n9", Items.CLOCK);
		put("shapeshifter_n10", Items.GOAT_HORN);
		put("gunslinger_n1", Items.SPYGLASS); put("gunslinger_n2", Items.ICE); put("gunslinger_n3", Items.COBWEB);
		put("gunslinger_n4", Items.FIRE_CHARGE); put("gunslinger_n5", Items.IRON_NUGGET); put("gunslinger_n6", Items.CROSSBOW);
		put("gunslinger_n7", Items.GUNPOWDER); put("gunslinger_n8", Items.TRIPWIRE_HOOK); put("gunslinger_n9", Items.REPEATER);
		put("gunslinger_n10", Items.WITHER_SKELETON_SKULL);
		put("weapon_summoner_n1", Items.GRINDSTONE); put("weapon_summoner_n2", Items.BELL); put("weapon_summoner_n3", Items.TRIDENT);
		put("weapon_summoner_n4", Items.IRON_BARS); put("weapon_summoner_n5", Items.IRON_SWORD); put("weapon_summoner_n6", Items.GOLDEN_SWORD);
		put("weapon_summoner_n7", Items.DIAMOND_SWORD); put("weapon_summoner_n8", Items.SMITHING_TABLE); put("weapon_summoner_n9", Items.ECHO_SHARD);
		put("weapon_summoner_n10", Items.NETHERITE_SWORD);
		put("hunter_n1", Items.BOW); put("hunter_n2", Items.ROTTEN_FLESH); put("hunter_n3", Items.STRING);
		put("hunter_n4", Items.TNT); put("hunter_n5", Items.IRON_TRAPDOOR); put("hunter_n6", Items.ARROW);
		put("hunter_n7", Items.FERN); put("hunter_n8", Items.FEATHER); put("hunter_n9", Items.EXPERIENCE_BOTTLE);
		put("hunter_n10", Items.TARGET);
		// v67 F7: iconos provisionales (objetos del juego) hasta que llegue el arte de docs/PROMPT_ARTE_ICONOS_F7.md.
		put("bard_n1", Items.NOTE_BLOCK); put("bard_n2", Items.MUSIC_DISC_13); put("bard_n3", Items.GOAT_HORN);
		put("bard_n4", Items.WHITE_WOOL); put("bard_n5", Items.JUKEBOX); put("bard_n6", Items.BELL);
		put("bard_n7", Items.CLOCK); put("bard_n8", Items.WHITE_BANNER); put("bard_n9", Items.MUSIC_DISC_CAT);
		put("bard_n10", Items.FIREWORK_STAR);
		put("warlock_n1", Items.FERMENTED_SPIDER_EYE); put("warlock_n2", Items.REDSTONE); put("warlock_n3", Items.ENDER_EYE);
		put("warlock_n4", Items.CHAIN); put("warlock_n5", Items.SOUL_SAND); put("warlock_n6", Items.SLIME_BALL);
		put("warlock_n7", Items.WITHER_ROSE); put("warlock_n8", Items.NETHERITE_HELMET); put("warlock_n9", Items.WHEAT);
		put("warlock_n10", Items.CRYING_OBSIDIAN);
		put("tamer_n1", Items.RABBIT_HIDE); put("tamer_n2", Items.BONE); put("tamer_n3", Items.LEAD);
		put("tamer_n4", Items.COOKED_BEEF); put("tamer_n5", Items.GOLDEN_CARROT); put("tamer_n6", Items.LEATHER_HORSE_ARMOR);
		put("tamer_n7", Items.SPIDER_EYE); put("tamer_n8", Items.WOLF_ARMOR); put("tamer_n9", Items.FEATHER);
		put("tamer_n10", Items.GOLDEN_HORSE_ARMOR);
		put("arcane_charges", Items.AMETHYST_CLUSTER);
		put("mana_shield", Items.LAPIS_BLOCK); put("spell_echo", Items.ECHO_SHARD);
		put("time_warp", Items.CLOCK); put("arcane_missiles", Items.END_ROD);
		put("lethal_tempo", Items.IRON_SWORD); put("smoke_escape", Items.GRAY_DYE);
		put("vanish_on_kill", Items.FERMENTED_SPIDER_EYE); put("fan_of_knives", Items.SHEARS);
		put("shuriken", Items.NETHER_STAR); put("bloom", Items.PINK_PETALS);
		put("antidote", Items.MILK_BUCKET); put("thorny_skin", Items.SWEET_BERRIES);
		put("healing_rain", Items.WATER_BUCKET); put("spore_cloud", Items.SPORE_BLOSSOM);
		put("overdrive", Items.BLAZE_POWDER); put("auto_shield", Items.SHIELD);
		put("field_repair", Items.ANVIL); put("grapple", Items.FISHING_ROD);
		put("pulse_grenade", Items.FIRE_CHARGE); put("impetus", Items.FEATHER);
		put("brace", Items.IRON_BLOCK); put("dragoon_fall", Items.PHANTOM_MEMBRANE);
		put("dragon_leap", Items.DRAGON_BREATH); put("piercing_thrust", Items.ARROW);
		put("kata", Items.GOLDEN_SWORD); put("reflexes", Items.CLOCK);
		put("flurry", Items.SUGAR); put("iaido", Items.DIAMOND_SWORD);
		put("thousand_cuts", Items.NETHERITE_SWORD); put("holy_light", Items.GLOWSTONE_DUST);
		put("intercession", Items.GOLDEN_CARROT); put("purifying_light", Items.GHAST_TEAR);
		put("consecration", Items.BEACON); put("hammer_of_justice", Items.MACE);
		put("soul_harvest", Items.SOUL_LANTERN); put("bone_shield", Items.BONE_BLOCK);
		put("corpse_burst", Items.ROTTEN_FLESH); put("bone_spear", Items.BONE);
		put("soul_siphon", Items.SCULK); put("day_night_cycle", Items.DAYLIGHT_DETECTOR);
		put("barkskin", Items.OAK_LOG); put("bramble", Items.SWEET_BERRIES);
		put("summon_wolves", Items.WOLF_ARMOR); put("thorn_storm", Items.CACTUS);
		put("compound_interest", Items.EMERALD_BLOCK); put("insurance", Items.PAPER);
		put("bounty", Items.GOLD_NUGGET); put("coin_toss", Items.EMERALD);
		put("golden_guard", Items.GOLDEN_CHESTPLATE); put("stances", Items.BAMBOO);
		put("deflect", Items.SHIELD); put("inner_peace", Items.LILY_PAD);
		put("flying_kick", Items.LEATHER_BOOTS); put("hundred_fists", Items.IRON_INGOT);
		put("elemental_reaction", Items.REDSTONE); put("elemental_ward", Items.PRISMARINE_SHARD);
		put("resonant_kill", Items.AMETHYST_SHARD); put("elemental_barrage", Items.BLAZE_ROD);
		put("elemental_aura", Items.CONDUIT); put("instinct", Items.RABBIT_FOOT);
		put("slippery", Items.SLIME_BALL); put("devour", Items.COOKED_BEEF);
		put("pounce", Items.LEAD); put("beast_howl", Items.GOAT_HORN);

		BY_EFFECT.put(PathEffect.LIFESTEAL, Items.GHAST_TEAR); BY_EFFECT.put(PathEffect.CRIT, Items.DIAMOND_SWORD);
		BY_EFFECT.put(PathEffect.EXECUTE, Items.WITHER_SKELETON_SKULL); BY_EFFECT.put(PathEffect.THORNS, Items.CACTUS);
		BY_EFFECT.put(PathEffect.DAMAGE_REDUCTION, Items.IRON_CHESTPLATE); BY_EFFECT.put(PathEffect.DODGE, Items.PHANTOM_MEMBRANE);
		BY_EFFECT.put(PathEffect.MANA_REGEN, Items.AMETHYST_SHARD); BY_EFFECT.put(PathEffect.RESOURCE_GAIN, Items.EXPERIENCE_BOTTLE);
		BY_EFFECT.put(PathEffect.REGEN, Items.GOLDEN_CARROT); BY_EFFECT.put(PathEffect.ON_HIT_POISON, Items.SPIDER_EYE);
		BY_EFFECT.put(PathEffect.ON_HIT_SLOW, Items.COBWEB); BY_EFFECT.put(PathEffect.ON_HIT_FIRE, Items.MAGMA_CREAM);
		BY_EFFECT.put(PathEffect.ON_HIT_WITHER, Items.WITHER_ROSE); BY_EFFECT.put(PathEffect.MINION_POWER, Items.BONE);
		BY_EFFECT.put(PathEffect.KNOCKBACK, Items.ANVIL); BY_EFFECT.put(PathEffect.SPEED, Items.RABBIT_FOOT);
		BY_EFFECT.put(PathEffect.MAX_HEALTH, Items.APPLE); BY_EFFECT.put(PathEffect.ATTACK_SPEED, Items.GOLDEN_SWORD);
		BY_EFFECT.put(PathEffect.LUCK, Items.RABBIT_FOOT);

		sig(Role.BERSERKER, Items.GOAT_HORN, Items.NETHERITE_AXE);
		sig(Role.MAGE, Items.AMETHYST_CLUSTER, Items.END_CRYSTAL);
		sig(Role.ASSASSIN, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL);
		sig(Role.HERBALIST, Items.HONEY_BOTTLE, Items.FLOWERING_AZALEA);
		sig(Role.ENGINEER, Items.LODESTONE, Items.TNT);
		sig(Role.LANCER, Items.SADDLE, Items.TRIDENT);
		sig(Role.SWORDSMAN, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
		sig(Role.PALADIN, Items.TOTEM_OF_UNDYING, Items.BEACON);
		sig(Role.NECROMANCER, Items.ZOMBIE_HEAD, Items.WITHER_SKELETON_SKULL);
		sig(Role.DRUID, Items.OAK_SAPLING, Items.BONE);
		sig(Role.MERCHANT, Items.EMERALD_BLOCK, Items.GOLD_BLOCK);
		sig(Role.MONK, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
		sig(Role.ELEMENTALIST, Items.COBBLESTONE_WALL, Items.NETHER_STAR);
		sig(Role.SHAPESHIFTER, Items.EGG, Items.SCULK_SHRIEKER);
		sig(Role.GUNSLINGER, Items.FIREWORK_ROCKET, Items.CLOCK);
		sig(Role.WEAPON_SUMMONER, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
		sig(Role.HUNTER, Items.CROSSBOW, Items.SPECTRAL_ARROW);
		sig(Role.BARD, Items.NOTE_BLOCK, Items.JUKEBOX);
		sig(Role.WARLOCK, Items.END_PORTAL_FRAME, Items.WITHER_SKELETON_SKULL);
		sig(Role.TAMER, Items.BONE, Items.IRON_BLOCK);
	}

	private NodeIcons() {
	}

	private static void put(String id, Item item) {
		BY_NODE.put(id, item);
	}

	private static void sig(Role role, Item signature, Item ultimate) {
		SIGNATURES.put(role, signature);
		ULTIMATES.put(role, ultimate);
	}

	/** v46.1: icono de cada plantilla de piedra angular (nodo S7 e indicador del HUD). */
	public static ItemStack keystone(KeystoneRules.Template template) {
		Item item = switch (template) {
			case CARNAGE -> Items.REDSTONE;
			case BULWARK -> Items.SHIELD;
			case OVERLOAD -> Items.LIGHTNING_ROD;
			case DETONATION -> Items.TNT;
			case CONTAGION -> Items.FERMENTED_SPIDER_EYE;
			case LEGION -> Items.SKELETON_SKULL;
			case SPECTER -> Items.PHANTOM_MEMBRANE;
			case FORTUNE -> Items.EMERALD;
			case TEMPEST -> Items.BLAZE_ROD;
			case SANCTUARY -> Items.GLISTERING_MELON_SLICE;
		};
		return new ItemStack(item);
	}

	/** Icono de un nodo del árbol. */
	public static ItemStack of(RoleNode node) {
		RoleTree.KeystoneSpec key = RoleTree.keystone(node.id());
		if (key != null) return keystone(key.template());
		if (node.kind() == RoleNode.Kind.LEGENDARY) {
			dev.aptitudes.unique.UniqueSkill legend = dev.aptitudes.unique.UniqueSkill.forRole(node.role());
			if (legend != null) return legend.iconStack();
		}
		Item item = BY_NODE.get(node.id());
		if (item == null && node.evolved()) { // v23: por efecto (pasivas) o por plantilla (activas)
			EvolvedTree.Passive p = EvolvedTree.passive(node.id());
			EvolvedTree.Active a = EvolvedTree.active(node.id());
			if (p != null) item = BY_EFFECT.get(p.effect());
			else if (a != null) item = switch (a.template()) {
				case NOVA -> Items.FIRE_CHARGE;
				case BEAM -> Items.END_ROD;
				case CONE -> Items.NETHERITE_SWORD;
				case DASH -> Items.RABBIT_FOOT;
				case ZONE -> Items.LIGHTNING_ROD;
				case VORTEX -> Items.ENDER_EYE;
				case AURA -> Items.BEACON;
				case HEAL -> Items.GLISTERING_MELON_SLICE;
				case SHIELD -> Items.SHIELD;
				case SUMMON -> a.extra() == EvolvedTree.Extra.ZOMBIES ? Items.ROTTEN_FLESH
						: a.extra() == EvolvedTree.Extra.VEXES ? Items.TOTEM_OF_UNDYING : Items.BONE;
			};
		}
		if (item == null) {
			item = switch (node.kind()) {
				case STAT -> Items.IRON_CHESTPLATE;
				case HASTE -> Items.CLOCK;
				case EFFICIENCY -> Items.LAPIS_LAZULI;
				case MASTERY -> Items.NETHER_STAR;
				case PATH -> {
					RoleTree.PathSpec spec = RoleTree.pathSpec(node.id());
					yield spec == null ? Items.BOOK : BY_EFFECT.getOrDefault(spec.effect(), Items.BOOK);
				}
				default -> null;
			};
		}
		return item == null ? node.role().iconStack() : new ItemStack(item);
	}

	public static ItemStack signature(Role role) {
		return new ItemStack(SIGNATURES.getOrDefault(role, Items.GOLDEN_APPLE));
	}

	public static ItemStack ultimate(Role role) {
		return new ItemStack(ULTIMATES.getOrDefault(role, Items.ENCHANTED_GOLDEN_APPLE));
	}
}
