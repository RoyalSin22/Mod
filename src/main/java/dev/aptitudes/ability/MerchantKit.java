package dev.aptitudes.ability;

import dev.aptitudes.elite.Elites;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.MerchantRules.*;

/**
 * v67 (F5): las 10 habilidades nuevas del Mercader táctico (N1–N10). Cifras en {@link MerchantRules} (puro, JUnit).
 * Monedero y Crédito se enganchan en {@code AbilitiesExtra.consume}, el único sitio donde las activas gastan
 * esmeraldas. Estado en memoria: al reiniciar el servidor se pierden subastas, cargas del libro e inversión.
 */
public final class MerchantKit {
	private static final String N = "merchant_n";

	private static final class State {
		/** Enemigo subastado → tick en que acaba. */
		final Map<UUID, Long> auctions = new HashMap<>();
		long nextDividend;
		int ledger;
		long investUntil;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private MerchantKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", MerchantKit::bouncingCoin);
		Abilities.register(N + "4", MerchantKit::auction);
		Abilities.register(N + "6", MerchantKit::caravan);
		Abilities.register(N + "8", MerchantKit::investment);
		Abilities.register(N + "10", MerchantKit::liquidation);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Moneda rebotona: la moneda salta entre hasta tres enemigos. */
	private static boolean bouncingCoin(ServerPlayerEntity p, int rank) {
		LivingEntity first = Targeting.raycastLiving(p, COIN_RANGE);
		if (first == null || !Targeting.isHostileImpactTarget(p, first)) return false;
		float damage = (float) coinDamage(rank, KitSupport.scale());
		Set<UUID> hit = new HashSet<>();
		LivingEntity current = first;
		Vec3d from = p.getEyePos();
		for (int i = 0; i < COIN_TARGETS && current != null; i++) {
			Vec3d to = current.getPos().add(0, current.getHeight() / 2, 0);
			Targeting.particleLine(p.getServerWorld(), ParticleTypes.GLOW, from, to);
			current.damage(KitSupport.magic(p), damage);
			hit.add(current.getUuid());
			from = to;
			LivingEntity at = current;
			current = p.getServerWorld().getEntitiesByClass(LivingEntity.class, at.getBoundingBox().expand(COIN_BOUNCE_RADIUS),
							e -> !hit.contains(e.getUuid()) && Targeting.isHostileImpactTarget(p, e)).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(at))).orElse(null);
		}
		KitSupport.sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.6f);
		return true;
	}

	/** N4 Subasta: el objetivo recibe más daño y suelta esmeraldas si lo matas. */
	private static boolean auction(ServerPlayerEntity p, int rank) {
		LivingEntity target = Targeting.raycastLiving(p, AUCTION_RANGE);
		if (target == null || !Targeting.isHostileImpactTarget(p, target)) return false;
		state(p).auctions.put(target.getUuid(), KitSupport.now(p) + AUCTION_TICKS);
		KitSupport.effect(target, StatusEffects.GLOWING, AUCTION_TICKS, 0);
		Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, target, 15, 0.5);
		KitSupport.sound(p, SoundEvents.ENTITY_VILLAGER_TRADE, 1.0f);
		return true;
	}

	/** N6 Caravana: Velocidad II y Salto para ti y tus aliados cercanos. */
	private static boolean caravan(ServerPlayerEntity p, int rank) {
		int ticks = caravanTicks(rank);
		List<LivingEntity> group = new ArrayList<>();
		group.add(p);
		for (var ally : Targeting.playersAround(p, CARAVAN_RADIUS)) if (ally != p) group.add(ally);
		for (LivingEntity member : group) {
			KitSupport.effect(member, StatusEffects.SPEED, ticks, 1);
			KitSupport.effect(member, StatusEffects.JUMP_BOOST, ticks, 0);
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), CARAVAN_RADIUS, ParticleTypes.HAPPY_VILLAGER, 36);
		KitSupport.sound(p, SoundEvents.ENTITY_HORSE_GALLOP, 1.0f);
		return true;
	}

	/** N8 Inversión: recuperas parte del maná que gastes. */
	private static boolean investment(ServerPlayerEntity p, int rank) {
		state(p).investUntil = KitSupport.now(p) + investTicks(rank);
		Targeting.burst(p.getServerWorld(), ParticleTypes.GLOW, p, 20, 0.6);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.4f);
		return true;
	}

	/** N10 Gran liquidación: gastas hasta 10 esmeraldas para una explosión de área. */
	private static boolean liquidation(ServerPlayerEntity p, int rank) {
		int spent = p.isCreative() ? LIQUIDATION_MAX_EMERALDS : takeEmeralds(p, LIQUIDATION_MAX_EMERALDS);
		float damage = (float) liquidationDamage(spent, KitSupport.scale());
		for (LivingEntity e : Targeting.hostilesAround(p, LIQUIDATION_RADIUS)) {
			e.damage(KitSupport.magic(p), damage);
			e.takeKnockback(1.2, p.getX() - e.getX(), p.getZ() - e.getZ());
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.4, 0), LIQUIDATION_RADIUS, ParticleTypes.HAPPY_VILLAGER, 48);
		p.getServerWorld().spawnParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
		KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.2f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Monedero: ¿esta activa no gasta su esmeralda? Se llama desde {@code AbilitiesExtra.consume}. */
	public static boolean waive(ServerPlayerEntity p, int count) {
		int rank = RoleManager.activeRank(p, N + "1");
		if (rank <= 0 || p.getRandom().nextDouble() >= waiveChance(rank)) return false;
		Targeting.burst(p.getServerWorld(), ParticleTypes.HAPPY_VILLAGER, p, 6, 0.4);
		return true;
	}

	/** Crédito: sin esmeraldas, paga la activa con maná. Se llama cuando faltan. */
	public static boolean credit(ServerPlayerEntity p, int missing) {
		int rank = RoleManager.activeRank(p, N + "5");
		if (rank <= 0 || missing <= 0) return false;
		double cost = creditCost(rank) * missing;
		RoleData data = RoleManager.get(p);
		if (data.energy() < cost) return false;
		data.setEnergy(data.energy() - cost);
		AbilityManager.sendEnergy(p, false);
		return true;
	}

	/** Libro mayor: cada esmeralda gastada carga el siguiente golpe. */
	public static void onSpend(ServerPlayerEntity p, int count) {
		if (RoleManager.activeRank(p, N + "7") <= 0) return;
		State s = state(p);
		s.ledger = ledgerCharges(s.ledger, count);
	}

	/** Subasta y Libro mayor suman daño al golpe. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		State s = STATES.get(p.getUuid());
		if (s == null) return 0f;
		float bonus = 0f;
		Long until = s.auctions.get(target.getUuid());
		int auction = RoleManager.activeRank(p, N + "4");
		if (until != null && auction > 0 && windowOpen(KitSupport.now(p), until)) bonus += (float) auctionBonus(auction);
		if (direct && s.ledger > 0) bonus += (float) ledgerBonus(s.ledger);
		return bonus;
	}

	/** El libro mayor se gasta con el golpe. */
	public static void afterOutgoing(ServerPlayerEntity p, boolean direct, float damage) {
		if (!direct || damage <= 0) return;
		State s = STATES.get(p.getUuid());
		if (s != null) s.ledger = 0;
	}

	/** Inversión: devuelve parte del maná gastado. */
	public static void onCast(ServerPlayerEntity p, String key, double spent) {
		State s = STATES.get(p.getUuid());
		if (s == null || spent <= 0 || key.equals(N + "8") || !windowOpen(KitSupport.now(p), s.investUntil)) return;
		KitSupport.addEnergy(p, spent * INVEST_REFUND);
	}

	/** Subasta (esmeraldas al morir) y Cacería de recompensas (élites y campeones). */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		State s = STATES.get(p.getUuid());
		if (s != null) {
			Long until = s.auctions.remove(victim.getUuid());
			if (until != null && windowOpen(KitSupport.now(p), until) && RoleManager.activeRank(p, N + "4") > 0) {
				victim.dropStack(new ItemStack(Items.EMERALD, AUCTION_DROP));
			}
		}
		int bounty = bountyDrop(RoleManager.activeRank(p, N + "9"));
		if (bounty > 0 && (Elites.isElite(victim) || Elites.isChampion(victim))) {
			victim.dropStack(new ItemStack(Items.EMERALD, bounty));
		}
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 20 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			int rank = RoleManager.activeRank(p, N + "2");
			if (rank <= 0) continue;
			State s = state(p);
			if (now < s.nextDividend) continue;
			s.nextDividend = now + DIVIDEND_INTERVAL_TICKS;
			if (dividendDue(rank, p.getInventory().count(Items.EMERALD))) {
				p.getInventory().offerOrDrop(new ItemStack(Items.EMERALD));
				KitSupport.sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f);
			}
		}
		if (now % 200 == 0) {
			for (State s : STATES.values()) s.auctions.values().removeIf(until -> !windowOpen(now, until));
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	/** Quita hasta {@code max} esmeraldas del inventario y devuelve cuántas quitó. */
	private static int takeEmeralds(ServerPlayerEntity p, int max) {
		var inv = p.getInventory();
		int left = max;
		for (int i = 0; i < inv.size() && left > 0; i++) {
			ItemStack stack = inv.getStack(i);
			if (!stack.isOf(Items.EMERALD)) continue;
			int take = Math.min(left, stack.getCount());
			stack.decrement(take);
			left -= take;
		}
		return max - left;
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
