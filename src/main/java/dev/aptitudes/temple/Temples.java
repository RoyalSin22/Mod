package dev.aptitudes.temple;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.logic.AttributeApplier;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Templos sellados y Vasos de vida (v64, K6): registro de bloques, objetos y entidades de bloque, guardián,
 * llave y aplicación de la vida extra. La estructura la genera {@code world/TempleFeature}.
 */
public final class Temples {
	private static final String GUARDIAN_TAG = Aptitudes.MOD_ID + ".temple_guardian";
	private static final String SEAL_TAG = Aptitudes.MOD_ID + ".temple_seal:";
	private static final String TEMPLE_TAG = Aptitudes.MOD_ID + ".temple:";
	private static final Identifier VESSEL_MODIFIER = Aptitudes.id("life_vessels");
	private static final Identifier GUARDIAN_HEALTH = Aptitudes.id("temple_guardian_health");

	private static AbstractBlock.Settings unbreakable(MapColor color) {
		return AbstractBlock.Settings.create().mapColor(color).strength(-1f, 3_600_000f).dropsNothing()
				.pistonBehavior(PistonBehavior.BLOCK).sounds(BlockSoundGroup.DEEPSLATE_BRICKS);
	}

	/** Piedra de la puerta sellada: desaparece al resolver el puzle. */
	public static final Block SEALED_STONE = Registry.register(Registries.BLOCK, Aptitudes.id("sealed_stone"),
			new Block(unbreakable(MapColor.TERRACOTTA_CYAN)));
	public static final Block TEMPLE_SEAL = Registry.register(Registries.BLOCK, Aptitudes.id("temple_seal"),
			new TempleBlocks.Seal(unbreakable(MapColor.CYAN).luminance(state -> 7)));
	public static final Block RELIQUARY = Registry.register(Registries.BLOCK, Aptitudes.id("reliquary"),
			new TempleBlocks.Reliquary(unbreakable(MapColor.GOLD).luminance(state -> 5)));

	public static final BlockEntityType<TempleSealBlockEntity> SEAL_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			Aptitudes.id("temple_seal"), BlockEntityType.Builder.create(TempleSealBlockEntity::new, TEMPLE_SEAL).build(null));
	public static final BlockEntityType<ReliquaryBlockEntity> RELIQUARY_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			Aptitudes.id("reliquary"), BlockEntityType.Builder.create(ReliquaryBlockEntity::new, RELIQUARY).build(null));

	public static final Item SEALED_STONE_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("sealed_stone"),
			new BlockItem(SEALED_STONE, new Item.Settings()));
	public static final Item TEMPLE_SEAL_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("temple_seal"),
			new BlockItem(TEMPLE_SEAL, new Item.Settings().rarity(Rarity.RARE)));
	public static final Item RELIQUARY_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("reliquary"),
			new BlockItem(RELIQUARY, new Item.Settings().rarity(Rarity.RARE)));
	public static final Item TEMPLE_KEY = Registry.register(Registries.ITEM, Aptitudes.id("temple_key"),
			new TempleKeyItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.RARE)));
	public static final Item LIFE_VESSEL = Registry.register(Registries.ITEM, Aptitudes.id("life_vessel"),
			new LifeVesselItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC)));

	private Temples() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Aptitudes.id("items"))).register(entries -> {
			entries.add(TEMPLE_KEY);
			entries.add(LIFE_VESSEL);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob && mob.getCommandTags().contains(GUARDIAN_TAG)
					&& mob.getWorld() instanceof ServerWorld world) {
				onGuardianDeath(world, mob);
			}
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> apply(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> apply(newPlayer));
	}

	// ------------------------------------------------------------- Vasos

	public static int vessels(ServerPlayerEntity player) {
		Integer n = player.getAttached(ModAttachments.LIFE_VESSELS);
		return n == null ? 0 : Math.max(0, n);
	}

	static void setVessels(ServerPlayerEntity player, int count) {
		player.setAttached(ModAttachments.LIFE_VESSELS, Math.max(0, Math.min(VesselRules.MAX_LIMIT, count)));
		apply(player);
	}

	/** Vida extra de los vasos: modificador fijo que se quita y se vuelve a poner (idempotente). */
	public static void apply(ServerPlayerEntity player) {
		AttributeApplier.set(player, EntityAttributes.GENERIC_MAX_HEALTH, VESSEL_MODIFIER,
				VesselRules.bonus(vessels(player), Aptitudes.config().lifeVesselMax), EntityAttributeModifier.Operation.ADD_VALUE);
	}

	// ----------------------------------------------------------- Guardián

	/** Hace aparecer al guardián del sello (élite campeón, más vida por anillo) y devuelve su id. */
	@Nullable
	static UUID spawnGuardian(ServerWorld world, TempleSealBlockEntity seal) {
		// v65: el guardián del sello es el Custodio de las Cuatro Llaves; el bono de vida por anillo se mantiene.
		EntityType<? extends MobEntity> type = dev.aptitudes.mob.NewMobs.CUSTODIO;
		MobEntity mob = type.create(world);
		if (mob == null) return null;
		BlockPos at = seal.getPos().add(seal.guardianSpawn);
		mob.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
		Elites.makeElite(mob, true, world.random);
		for (EquipmentSlot slot : EquipmentSlot.values()) mob.setEquipmentDropChance(slot, 0f);
		var health = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (health != null) {
			AttributeApplier.set(health, GUARDIAN_HEALTH, TempleRules.guardianHealthBonus(seal.ring),
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			mob.setHealth(mob.getMaxHealth());
		}
		mob.addCommandTag(GUARDIAN_TAG);
		mob.addCommandTag(SEAL_TAG + seal.getPos().asLong());
		mob.addCommandTag(TEMPLE_TAG + seal.templeId);
		mob.setCustomName(Text.translatable("entity.aptitudes.temple_guardian").formatted(Formatting.GOLD));
		mob.setPersistent();
		world.spawnEntityAndPassengers(mob);
		Elites.setChampionBarName(mob, mob.getDisplayName());
		return mob.getUuid();
	}

	private static void onGuardianDeath(ServerWorld world, MobEntity mob) {
		UUID temple = null;
		BlockPos sealPos = null;
		for (String tag : mob.getCommandTags()) {
			try {
				if (tag.startsWith(TEMPLE_TAG)) temple = UUID.fromString(tag.substring(TEMPLE_TAG.length()));
				else if (tag.startsWith(SEAL_TAG)) sealPos = BlockPos.fromLong(Long.parseLong(tag.substring(SEAL_TAG.length())));
			} catch (IllegalArgumentException ignored) {
				// Etiqueta manipulada: se ignora.
			}
		}
		if (temple == null) return;
		if (sealPos != null && world.isChunkLoaded(sealPos) && world.getBlockEntity(sealPos) instanceof TempleSealBlockEntity seal
				&& seal.templeId.equals(temple)) {
			seal.cleared();
		}
		ItemEntity key = mob.dropStack(TempleKeyItem.forTemple(temple));
		if (key != null) key.setNeverDespawn();
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (p.squaredDistanceTo(mob) < 48 * 48) {
				p.sendMessage(Text.translatable("temple.aptitudes.guardian_defeated").formatted(Formatting.GOLD), true);
			}
		}
	}
}
