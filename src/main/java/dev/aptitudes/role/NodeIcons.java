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
		put("berserker_n9", Items.ECHO_SHARD); put("berserker_n10", Items.MACE); put("arcane_charges", Items.AMETHYST_CLUSTER);
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
