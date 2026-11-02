package dev.aptitudes.item;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

/**
 * Un ítem característico por rol. Las armas cuerpo a cuerpo reutilizan las
 * clases vanilla ({@link SwordItem}/{@link AxeItem}) con estadísticas propias;
 * los ítems con comportamiento (bastón, pistola, saquito, llave) tienen clase.
 */
public final class ModItems {
	public static final Item BERSERKER_AXE = register("berserker_axe", new AxeItem(ToolMaterials.IRON,
			new Item.Settings().attributeModifiers(AxeItem.createAttributeModifiers(ToolMaterials.IRON, 7.0f, -3.0f))));

	public static final Item DAGGER = register("dagger", new SwordItem(ToolMaterials.IRON,
			new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 1, -1.6f))));

	public static final Item KATANA = register("katana", new SwordItem(ToolMaterials.IRON,
			new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 3, -2.1f))));

	/** Lanza: más daño, más lenta y +1 bloque de alcance contra entidades. */
	public static final Item SPEAR = register("spear", new SwordItem(ToolMaterials.IRON,
			new Item.Settings().attributeModifiers(
					SwordItem.createAttributeModifiers(ToolMaterials.IRON, 4, -2.8f)
							.with(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
									new EntityAttributeModifier(Aptitudes.id("spear_reach"), 1.0,
											EntityAttributeModifier.Operation.ADD_VALUE),
									AttributeModifierSlot.MAINHAND))));

	public static final Item ARCANE_STAFF = register("arcane_staff",
			new HitscanWeaponItem(HitscanWeaponItem.Kind.ARCANE, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

	public static final Item PLASMA_GUN = register("plasma_gun",
			new HitscanWeaponItem(HitscanWeaponItem.Kind.PLASMA, new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	public static final Item HERB_POUCH = register("herb_pouch", new HerbPouchItem(new Item.Settings().maxCount(16)));

	public static final Item WRENCH = register("wrench", new WrenchItem(new Item.Settings().maxCount(1)));

	/** Martillo sagrado (Paladín): lento y contundente. */
	public static final Item HOLY_HAMMER = register("holy_hammer", new SwordItem(ToolMaterials.IRON,
			new Item.Settings().rarity(Rarity.UNCOMMON)
					.attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 6, -3.2f))));

	public static final Item NECRO_TOME = register("necro_tome",
			new HitscanWeaponItem(HitscanWeaponItem.Kind.NECROTIC, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

	public static final Item OAK_STAFF = register("oak_staff", new OakStaffItem(new Item.Settings().maxCount(1)));

	public static final Item GUILD_SEAL = register("guild_seal", new GuildSealItem(new Item.Settings().maxCount(1)));

	/** Vendas de puño (Monje): en la mano secundaria dan daño y velocidad de ataque. */
	public static final Item HAND_WRAPS = register("hand_wraps", new Item(new Item.Settings().maxCount(1)
			.attributeModifiers(AttributeModifiersComponent.builder()
					.add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
							new EntityAttributeModifier(Aptitudes.id("hand_wraps_damage"), 1.0,
									EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.OFFHAND)
					.add(EntityAttributes.GENERIC_ATTACK_SPEED,
							new EntityAttributeModifier(Aptitudes.id("hand_wraps_speed"), 0.1,
									EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE), AttributeModifierSlot.OFFHAND)
					.build())));

	public static final Item ELEMENTAL_ORB = register("elemental_orb",
			new ElementalOrbItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	public static final Item MIMIC_MASK = register("mimic_mask",
			new MimicMaskItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	// ------------------------------------------------- Ítems de los roles nuevos

	public static final Item ARCANE_REVOLVER = register("arcane_revolver",
			new HitscanWeaponItem(HitscanWeaponItem.Kind.REVOLVER, new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	public static final Item WEAPON_SIGIL = register("weapon_sigil",
			new HitscanWeaponItem(HitscanWeaponItem.Kind.BLADE, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

	/** Cuchillo de caza (Cazador): rápido y ligero. */
	public static final Item HUNTING_KNIFE = register("hunting_knife", new SwordItem(ToolMaterials.IRON,
			new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 2, -1.8f))));

	public static final Item LUTE = register("lute", new LuteItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

	public static final Item PACT_TALISMAN = register("pact_talisman",
			new HitscanWeaponItem(HitscanWeaponItem.Kind.CURSE, new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	public static final Item BEAST_WHISTLE = register("beast_whistle",
			new BeastWhistleItem(new Item.Settings().maxCount(1)));

	/** Requisito para evolucionar un rol a Maestro (se consume). */
	public static final Item MASTERY_EMBLEM = register("mastery_emblem",
			new Item(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));

	/** Requisito para evolucionar un rol a Definitivo (se consume). */
	public static final Item ULTIMATE_CORE = register("ultimate_core",
			new Item(new Item.Settings().maxCount(16).rarity(Rarity.EPIC).fireproof()));

	// ------------------------------------------------------- Objetos de alma

	/** Fragmento de alma: 5 % de los monstruos, 4 de los jefes. Material de sets, frascos y herramientas. */
	public static final Item SOUL_SHARD = register("soul_shard", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

	public static final Item ESTUS_FLASK = register("estus_flask",
			new SoulFlaskItem(SoulFlaskItem.Kind.ESTUS, new Item.Settings().rarity(Rarity.RARE)));

	public static final Item ETHER_FLASK = register("ether_flask",
			new SoulFlaskItem(SoulFlaskItem.Kind.ETHER, new Item.Settings().rarity(Rarity.RARE)));

	public static final Item SOUL_PICKAXE = register("soul_pickaxe", new SoulToolItem.Pickaxe(new Item.Settings()
			.rarity(Rarity.RARE).attributeModifiers(net.minecraft.item.PickaxeItem.createAttributeModifiers(
					ToolMaterials.DIAMOND, 1.0f, -2.8f))));

	public static final Item SOUL_SHOVEL = register("soul_shovel", new SoulToolItem.Shovel(new Item.Settings()
			.rarity(Rarity.RARE).attributeModifiers(net.minecraft.item.ShovelItem.createAttributeModifiers(
					ToolMaterials.DIAMOND, 1.5f, -3.0f))));

	public static final Item SOUL_AXE = register("soul_axe", new SoulToolItem.Axe(new Item.Settings()
			.rarity(Rarity.RARE).attributeModifiers(AxeItem.createAttributeModifiers(ToolMaterials.DIAMOND, 5.0f, -3.0f))));

	public static final Item SOUL_HOE = register("soul_hoe", new SoulToolItem.Hoe(new Item.Settings()
			.rarity(Rarity.RARE).attributeModifiers(net.minecraft.item.HoeItem.createAttributeModifiers(
					ToolMaterials.DIAMOND, -3.0f, 0.0f))));

	/** Se quema en una Hoguera para poder cambiar un rol ya elegido. */
	public static final Item REBIRTH_OFFERING = register("rebirth_offering",
			new RebirthOfferingItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));

	/** Vale de armería (aldeano de gremio): una pieza del set de tu rol principal. */
	public static final Item ARMORY_VOUCHER = register("armory_voucher",
			new ArmoryVoucherItem(new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)));

	// ---------------------------------------------------- Élites y accesorios

	/** Esencia de élite: la sueltan élites y campeones; material de los accesorios. */
	public static final Item ELITE_ESSENCE = register("elite_essence", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
	/** Corazón de campeón: solo lo sueltan los campeones. */
	public static final Item CHAMPION_HEART = register("champion_heart",
			new Item(new Item.Settings().rarity(Rarity.EPIC).fireproof()));

	public static final java.util.Map<dev.aptitudes.accessory.Accessory, Item> ACCESSORIES = registerAccessories();

	private static java.util.Map<dev.aptitudes.accessory.Accessory, Item> registerAccessories() {
		java.util.Map<dev.aptitudes.accessory.Accessory, Item> map = new java.util.EnumMap<>(dev.aptitudes.accessory.Accessory.class);
		for (dev.aptitudes.accessory.Accessory a : dev.aptitudes.accessory.Accessory.values()) {
			map.put(a, register(a.key(), new dev.aptitudes.accessory.AccessoryItem(a,
					new Item.Settings().rarity(a.ring() ? Rarity.RARE : Rarity.EPIC))));
		}
		return map;
	}

	static {
		RoleArmor.register(); // un set de 4 piezas por rol
	}

	public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, Aptitudes.id("items"),
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(KATANA))
					.displayName(Text.translatable("itemGroup.aptitudes.items"))
					.entries((context, entries) -> {
						entries.add(BERSERKER_AXE);
						entries.add(ARCANE_STAFF);
						entries.add(DAGGER);
						entries.add(HERB_POUCH);
						entries.add(WRENCH);
						entries.add(PLASMA_GUN);
						entries.add(SPEAR);
						entries.add(KATANA);
						entries.add(HOLY_HAMMER);
						entries.add(NECRO_TOME);
						entries.add(OAK_STAFF);
						entries.add(GUILD_SEAL);
						entries.add(HAND_WRAPS);
						entries.add(ELEMENTAL_ORB);
						entries.add(MIMIC_MASK);
						entries.add(ARCANE_REVOLVER);
						entries.add(WEAPON_SIGIL);
						entries.add(HUNTING_KNIFE);
						entries.add(LUTE);
						entries.add(PACT_TALISMAN);
						entries.add(BEAST_WHISTLE);
						entries.add(dev.aptitudes.block.ModBlocks.BONFIRE_ITEM);
						entries.add(MASTERY_EMBLEM);
						entries.add(ULTIMATE_CORE);
						entries.add(SOUL_SHARD);
						entries.add(REBIRTH_OFFERING);
						entries.add(ARMORY_VOUCHER);
						entries.add(ELITE_ESSENCE);
						entries.add(CHAMPION_HEART);
						for (Item accessory : ACCESSORIES.values()) entries.add(accessory);
						entries.add(dev.aptitudes.world.GuildVillagers.GUILD_DESK_ITEM);
						entries.add(ESTUS_FLASK);
						entries.add(ETHER_FLASK);
						entries.add(SOUL_PICKAXE);
						entries.add(SOUL_SHOVEL);
						entries.add(SOUL_AXE);
						entries.add(SOUL_HOE);
						for (Item piece : RoleArmor.all()) entries.add(piece);
					})
					.build());

	private ModItems() {
	}

	private static Item register(String path, Item item) {
		return Registry.register(Registries.ITEM, Aptitudes.id(path), item);
	}

	/** Fuerza la carga de la clase durante la inicialización del mod. */
	public static void init() {
	}
}
