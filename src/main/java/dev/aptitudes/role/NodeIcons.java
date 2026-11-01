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
		put("arcane_affinity", Items.LAPIS_LAZULI); put("fireball", Items.FIRE_CHARGE);
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
		sig(Role.MAGE, Items.FIRE_CHARGE, Items.END_CRYSTAL);
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

	/** Icono de un nodo del árbol. */
	public static ItemStack of(RoleNode node) {
		Item item = BY_NODE.get(node.id());
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
