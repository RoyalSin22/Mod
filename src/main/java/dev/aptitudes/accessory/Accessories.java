package dev.aptitudes.accessory;

import com.mojang.serialization.Codec;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.logic.AttributeApplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Estado y efectos de los accesorios. Se guardan en un adjunto persistente del
 * jugador (se conservan al morir) y se aplican cada segundo y al cambiarlos.
 */
@SuppressWarnings("UnstableApiUsage")
public final class Accessories {
	public static final AttachmentType<List<ItemStack>> SLOTS = AttachmentRegistry.<List<ItemStack>>builder()
			.persistent(ItemStack.OPTIONAL_CODEC.listOf().xmap(ArrayList::new, l -> l))
			.copyOnDeath()
			.initializer(() -> new ArrayList<>(List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY)))
			.buildAndRegister(Aptitudes.id("accessories"));

	private static final Map<UUID, Integer> PHOENIX_READY = new HashMap<>();
	private static final Map<UUID, Integer> LAST_HURT = new HashMap<>();

	private Accessories() {
	}

	public static void init() {
		AccessoryScreenHandler.init();
	}

	public static List<ItemStack> stacks(ServerPlayerEntity player) {
		List<ItemStack> list = player.getAttachedOrCreate(SLOTS);
		while (list.size() < 3) list.add(ItemStack.EMPTY);
		return list;
	}

	/** Accesorios equipados (sin repetir: dos anillos iguales no se suman). */
	public static EnumSet<Accessory> equipped(ServerPlayerEntity player) {
		EnumSet<Accessory> out = EnumSet.noneOf(Accessory.class);
		for (ItemStack s : stacks(player)) {
			if (s.getItem() instanceof AccessoryItem a) out.add(a.accessory());
		}
		return out;
	}

	public static boolean has(ServerPlayerEntity player, Accessory accessory) {
		return equipped(player).contains(accessory);
	}

	/** Abre la pantalla de accesorios; los cambios se guardan al instante. */
	public static void open(ServerPlayerEntity player) {
		List<ItemStack> stored = stacks(player);
		SimpleInventory inv = new SimpleInventory(3);
		for (int i = 0; i < 3; i++) inv.setStack(i, stored.get(i).copy());
		inv.addListener(changed -> {
			List<ItemStack> list = new ArrayList<>();
			for (int i = 0; i < 3; i++) list.add(inv.getStack(i).copy());
			player.setAttached(SLOTS, list);
			apply(player);
		});
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
				(syncId, playerInv, p) -> new AccessoryScreenHandler(syncId, playerInv, inv),
				Text.translatable("screen.aptitudes.accessories")));
	}

	/** Atributos de los accesorios equipados (idempotente). */
	public static void apply(ServerPlayerEntity player) {
		EnumSet<Accessory> eq = equipped(player);
		for (Accessory a : Accessory.values()) {
			if (a.attribute() == null) continue;
			AttributeApplier.set(player, a.attribute(), Aptitudes.id("accessory/" + a.key()), eq.contains(a) ? a.value() : 0,
					a.operation());
		}
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	// ------------------------------------------------------------ Hooks

	/** +30 de maná máximo (Anillo de maná). */
	public static double bonusMana(ServerPlayerEntity player) {
		return has(player, Accessory.RING_OF_MANA) ? 30 : 0;
	}

	/** −10 % de enfriamientos (Amuleto arcano). */
	public static double cooldownReduction(ServerPlayerEntity player) {
		return has(player, Accessory.ARCANE_AMULET) ? 0.10 : 0;
	}

	/** Daño saliente: Trofeo del cazador (+25 % a élites) y Anillo del vampiro. */
	public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
		if (amount <= 0 || target.getWorld().isClient()) return amount;
		if (target instanceof ServerPlayerEntity victim) LAST_HURT.put(victim.getUuid(), victim.getServer().getTicks());
		if (source.getAttacker() instanceof ServerPlayerEntity attacker && attacker != target) {
			EnumSet<Accessory> eq = equipped(attacker);
			if (eq.contains(Accessory.HUNTERS_TROPHY) && Elites.isElite(target)) amount *= 1.25f;
			if (eq.contains(Accessory.RING_OF_THE_VAMPIRE) && attacker.getHealth() < attacker.getMaxHealth()) {
				attacker.heal(amount * 0.05f);
			}
		}
		return amount;
	}

	/** Amuleto del fénix: evita una muerte cada 10 minutos. @return true si salvó al jugador. */
	public static boolean tryPhoenix(ServerPlayerEntity player) {
		if (!has(player, Accessory.AMULET_OF_THE_PHOENIX)) return false;
		int now = player.getServer().getTicks();
		if (PHOENIX_READY.getOrDefault(player.getUuid(), 0) > now) return false;
		PHOENIX_READY.put(player.getUuid(), now + 10 * 60 * 20);
		player.setHealth(player.getMaxHealth() * 0.5f);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 0));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
		player.getServerWorld().spawnParticles(ParticleTypes.FLAME, player.getX(), player.getBodyY(0.5), player.getZ(),
				80, 0.6, 1.0, 0.6, 0.1);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TOTEM_USE,
				SoundCategory.PLAYERS, 1.0f, 1.3f);
		player.sendMessage(Text.translatable("aptitudes.accessory.phoenix").formatted(Formatting.GOLD), true);
		return true;
	}

	/** Cada segundo: Amuleto de la arboleda (regeneración tras 5 s sin recibir daño). */
	public static void tick(MinecraftServer server) {
		int now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (!p.isAlive()) continue;
			if (has(p, Accessory.AMULET_OF_THE_GROVE) && now - LAST_HURT.getOrDefault(p.getUuid(), -10_000) > 100
					&& p.getHealth() < p.getMaxHealth()) {
				p.heal(1.0f);
			}
		}
	}

	public static void forget(ServerPlayerEntity player) {
		LAST_HURT.remove(player.getUuid());
	}
}
