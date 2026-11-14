package dev.aptitudes.ability;

import dev.aptitudes.party.PartyManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.BardRules.*;

/**
 * v67 (F7): las 10 habilidades nuevas del Bardo (N1–N10). Cifras en {@link BardRules} (puro, JUnit). Cualquier activa
 * del Bardo cuenta como «canción». Estado en memoria: al reiniciar el servidor se pierden Estribillo y Contrapunto.
 */
public final class BardKit {
	private static final String N = "bard_n";

	private static final class State {
		boolean refrain;
		long counterUntil;
		final Map<String, Long> recent = new HashMap<>();
	}

	private static final Map<UUID, State> STATES = new HashMap<>();
	/** Bardos con Armonía de grupo → rango (se refresca cada segundo). */
	private static final Map<UUID, Integer> HARMONISTS = new HashMap<>();

	private BardKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", BardKit::sharpNote);
		Abilities.register(N + "4", BardKit::lullaby);
		Abilities.register(N + "6", BardKit::rumble);
		Abilities.register(N + "8", BardKit::victoryMarch);
		Abilities.register(N + "10", BardKit::grandFinale);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Nota cortante: onda sónica en línea. */
	private static boolean sharpNote(ServerPlayerEntity p, int rank) {
		var world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, NOTE_RANGE);
		float damage = (float) noteDamage(rank, KitSupport.scale());
		Targeting.particleLine(world, ParticleTypes.NOTE, start, end);
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(NOTE_WIDTH),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			if (KitSupport.distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) > NOTE_WIDTH) continue;
			e.damage(KitSupport.magic(p), damage);
			KitSupport.effect(e, StatusEffects.WEAKNESS, NOTE_WEAKNESS_TICKS, 0);
		}
		KitSupport.sound(p, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.8f);
		return true;
	}

	/** N4 Arrullo: duerme a los enemigos cercanos. */
	private static boolean lullaby(ServerPlayerEntity p, int rank) {
		var hostiles = Targeting.hostilesAround(p, LULL_RADIUS);
		if (hostiles.isEmpty()) return false;
		int ticks = lullTicks(rank);
		for (LivingEntity e : hostiles) {
			KitSupport.effect(e, StatusEffects.SLOWNESS, ticks, 6);
			KitSupport.effect(e, StatusEffects.WEAKNESS, ticks, 1);
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.5, 0), LULL_RADIUS, ParticleTypes.NOTE, 24);
		KitSupport.sound(p, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), 0.6f);
		return true;
	}

	/** N6 Estruendo: cuatro pulsos de sonido. */
	private static boolean rumble(ServerPlayerEntity p, int rank) {
		float damage = (float) rumbleDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), RUMBLE_PULSES, RUMBLE_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, RUMBLE_RADIUS)) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 0);
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.5, 0), RUMBLE_RADIUS, ParticleTypes.SONIC_BOOM, 3);
			KitSupport.sound(p, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.6f);
		});
		return true;
	}

	/** N8 Marcha de victoria: velocidad, prisa y fuerza para el grupo. */
	private static boolean victoryMarch(ServerPlayerEntity p, int rank) {
		int ticks = marchTicks(rank);
		KitSupport.effect(p, StatusEffects.SPEED, ticks, 1);
		KitSupport.effect(p, StatusEffects.HASTE, ticks, 1);
		KitSupport.effect(p, StatusEffects.STRENGTH, ticks, 0);
		for (var ally : Targeting.playersAround(p, MARCH_RADIUS)) {
			if (ally == p) continue;
			KitSupport.effect(ally, StatusEffects.SPEED, ticks, 1);
			KitSupport.effect(ally, StatusEffects.HASTE, ticks, 1);
			KitSupport.effect(ally, StatusEffects.STRENGTH, ticks, 0);
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), MARCH_RADIUS, ParticleTypes.NOTE, 36);
		KitSupport.sound(p, SoundEvents.EVENT_RAID_HORN.value(), 1.0f);
		return true;
	}

	/** N10 Gran final: daña a los enemigos y fortalece a los aliados durante seis segundos. */
	private static boolean grandFinale(ServerPlayerEntity p, int rank) {
		float damage = (float) finaleDamage(KitSupport.scale());
		AbilityState.repeat(p.getServer(), FINALE_PULSES, FINALE_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			for (LivingEntity e : Targeting.hostilesAround(p, FINALE_RADIUS)) {
				e.damage(KitSupport.magic(p), damage);
				KitSupport.effect(e, StatusEffects.WEAKNESS, 40, 0);
			}
			p.heal(FINALE_HEAL);
			KitSupport.effect(p, StatusEffects.STRENGTH, 40, 0);
			for (var ally : Targeting.playersAround(p, FINALE_RADIUS)) {
				if (ally == p) continue;
				ally.heal(FINALE_HEAL);
				KitSupport.effect(ally, StatusEffects.STRENGTH, 40, 0);
			}
			ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.3, 0), FINALE_RADIUS, ParticleTypes.NOTE, 40);
			KitSupport.sound(p, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1.0f + 0.1f * i);
		});
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Armonía de grupo: tú y los aliados cercanos a un bardo recibís menos daño. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		if (amount <= 0 || HARMONISTS.isEmpty() || p.getServer() == null) return amount;
		double best = 0;
		for (Map.Entry<UUID, Integer> e : HARMONISTS.entrySet()) {
			ServerPlayerEntity bard = p.getServer().getPlayerManager().getPlayer(e.getKey());
			if (bard == null || bard.getWorld() != p.getWorld()) continue;
			if (bard != p && (!PartyManager.isAlly(bard, p) || bard.squaredDistanceTo(p) > HARMONY_RADIUS * HARMONY_RADIUS)) continue;
			best = Math.max(best, harmonyReduction(e.getValue()));
		}
		return best > 0 ? amount * (float) (1.0 - best) : amount;
	}

	/** Estribillo pegadizo: la habilidad que sigue a una canción cuesta menos. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		State s = STATES.get(p.getUuid());
		if (s == null || !s.refrain) return 1.0;
		return refrainCost(RoleManager.activeRank(p, N + "2"));
	}

	/** Contrapunto: más daño tras lanzar canciones distintas. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		State s = STATES.get(p.getUuid());
		int rank = RoleManager.activeRank(p, N + "9");
		return s != null && rank > 0 && windowOpen(KitSupport.now(p), s.counterUntil) ? (float) counterBonus(rank) : 0f;
	}

	/** Tras lanzar una activa: Estribillo pegadizo y Contrapunto. */
	public static void onCast(ServerPlayerEntity p, String key) {
		if (!isSong(key)) return;
		long now = KitSupport.now(p);
		int refrain = RoleManager.activeRank(p, N + "2");
		int counter = RoleManager.activeRank(p, N + "9");
		if (refrain <= 0 && counter <= 0) return;
		State s = state(p);
		if (refrain > 0) s.refrain = !s.refrain; // gasta el descuento si estaba; si no, lo arma
		if (counter > 0) {
			s.recent.put(key, now);
			s.recent.values().removeIf(at -> now - at > COUNTER_WINDOW_TICKS);
			if (counterReached(s.recent.size())) {
				s.counterUntil = now + COUNTER_TICKS;
				s.recent.clear();
				Targeting.burst(p.getServerWorld(), ParticleTypes.NOTE, p, 25, 0.8);
				KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.6f);
			}
		}
	}

	/** Pulso del compás: matar cura a los aliados cercanos. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		if (RoleManager.activeRank(p, N + "7") <= 0) return;
		for (var ally : Targeting.playersAround(p, BEAT_RADIUS)) ally.heal(BEAT_HEAL);
		Targeting.burst(p.getServerWorld(), ParticleTypes.NOTE, p, 6, 0.5);
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 20 == 0) {
			HARMONISTS.clear();
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				int rank = RoleManager.activeRank(p, N + "1");
				if (rank > 0) HARMONISTS.put(p.getUuid(), rank);
			}
		}
		if (now % CHOIR_INTERVAL_TICKS != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			int rank = RoleManager.activeRank(p, N + "5");
			if (rank <= 0) continue;
			int allies = (int) Targeting.playersAround(p, CHOIR_RADIUS).stream().filter(a -> a != p).count();
			double mana = choirMana(rank, allies);
			if (mana > 0) KitSupport.addEnergy(p, mana);
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
		HARMONISTS.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static boolean isSong(String key) {
		if (key.startsWith(N) || key.endsWith(".bard")) return true;
		RoleNode node = RoleTree.get(key);
		return node != null && node.role() == Role.BARD && node.active();
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
