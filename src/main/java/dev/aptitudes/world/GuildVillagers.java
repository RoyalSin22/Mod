package dev.aptitudes.world;

import com.google.common.collect.ImmutableSet;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.RoleArmor;
import dev.aptitudes.role.Role;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Optional;

/**
 * Aldeano de gremio: profesión nueva cuyo bloque de trabajo es la Mesa del gremio.
 * Vende Fragmentos de alma, frascos, materiales de rol, Vales de armería (una pieza
 * del set de tu rol principal), Ofrendas de renacimiento y emblemas de evolución.
 */
public final class GuildVillagers {
	public static final Block GUILD_DESK = Registry.register(Registries.BLOCK, Aptitudes.id("guild_desk"),
			new Block(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(2.5f)
					.sounds(BlockSoundGroup.WOOD)));
	public static final Item GUILD_DESK_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("guild_desk"),
			new BlockItem(GUILD_DESK, new Item.Settings()));

	public static final RegistryKey<PointOfInterestType> POI_KEY =
			RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Aptitudes.id("guild_desk"));
	public static final PointOfInterestType POI = PointOfInterestHelper.register(Aptitudes.id("guild_desk"), 1, 1, GUILD_DESK);

	public static final VillagerProfession GUILDMASTER = Registry.register(Registries.VILLAGER_PROFESSION,
			Aptitudes.id("guildmaster"),
			new VillagerProfession("guildmaster", entry -> entry.matchesKey(POI_KEY), entry -> entry.matchesKey(POI_KEY),
					ImmutableSet.of(), ImmutableSet.of(), SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER));

	private GuildVillagers() {
	}

	public static void init() {
		// Nivel 1 – Novato
		TradeOfferHelper.registerVillagerOffers(GUILDMASTER, 1, f -> {
			f.add(sell(Items.EMERALD, 6, ModItems.SOUL_SHARD, 1, 12, 2));
			f.add(buy(Items.LEATHER, 6, 1, 16, 2));
			f.add(buy(Items.BONE, 12, 1, 16, 2));
		});
		// Nivel 2 – Aprendiz
		TradeOfferHelper.registerVillagerOffers(GUILDMASTER, 2, f -> {
			f.add(sell(Items.EMERALD, 10, ModItems.ESTUS_FLASK, 1, 3, 10));
			f.add(sell(Items.EMERALD, 10, ModItems.ETHER_FLASK, 1, 3, 10));
			f.add(roleMaterial());
		});
		// Nivel 3 – Oficial
		TradeOfferHelper.registerVillagerOffers(GUILDMASTER, 3, f -> {
			f.add((entity, random) -> new TradeOffer(new TradedItem(Items.EMERALD, 12),
					Optional.of(new TradedItem(ModItems.SOUL_SHARD, 1)), new ItemStack(ModItems.ARMORY_VOUCHER), 6, 15, 0.05f));
			f.add(roleMaterial());
		});
		// Nivel 4 – Experto
		TradeOfferHelper.registerVillagerOffers(GUILDMASTER, 4, f -> {
			f.add((entity, random) -> new TradeOffer(new TradedItem(Items.EMERALD, 20),
					Optional.of(new TradedItem(ModItems.SOUL_SHARD, 2)), new ItemStack(ModItems.REBIRTH_OFFERING), 3, 20, 0.05f));
			f.add(sell(Items.EMERALD, 24, ModItems.MASTERY_EMBLEM, 1, 2, 20));
		});
		// Nivel 5 – Maestro
		TradeOfferHelper.registerVillagerOffers(GUILDMASTER, 5, f -> {
			f.add((entity, random) -> new TradeOffer(new TradedItem(Items.EMERALD, 30),
					Optional.of(new TradedItem(ModItems.SOUL_SHARD, 3)), new ItemStack(ModItems.ARMORY_VOUCHER, 2), 4, 30, 0.05f));
			f.add(sell(Items.EMERALD_BLOCK, 5, ModItems.ULTIMATE_CORE, 1, 1, 30));
		});
	}

	private static TradeOffers.Factory sell(Item price, int count, Item result, int resultCount, int maxUses, int xp) {
		return (entity, random) -> new TradeOffer(new TradedItem(price, count), new ItemStack(result, resultCount),
				maxUses, xp, 0.05f);
	}

	private static TradeOffers.Factory buy(Item item, int count, int emeralds, int maxUses, int xp) {
		return (entity, random) -> new TradeOffer(new TradedItem(item, count), new ItemStack(Items.EMERALD, emeralds),
				maxUses, xp, 0.05f);
	}

	/** Material base de un rol al azar (para fabricar su set de armadura). */
	private static TradeOffers.Factory roleMaterial() {
		return (entity, random) -> {
			Role role = Role.values()[random.nextInt(Role.values().length)];
			return new TradeOffer(new TradedItem(Items.EMERALD, 4), new ItemStack(RoleArmor.baseMaterial(role), 8), 8, 5, 0.05f);
		};
	}
}
