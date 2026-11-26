package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.ImpactFeedbackRules;
import dev.aptitudes.logic.ImpactFeedbackRules.Kind;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.sound.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * V95: lenguaje común de feedback de combate. Una sola clase traduce los acontecimientos del bus ({@link CombatEvents}) a señales
 * visuales y sonoras compartidas por los 20 roles: golpe pesado, crítico, golpe de habilidad, bloqueo con escudo, curación efectiva y
 * recurso insuficiente. No decide ningún daño ni curación: solo comunica. El cliente las dibuja con su calidad y puede apagarlas.
 */
public final class ImpactFeedback implements CombatEvents.Listener {
	private static final ImpactFeedback INSTANCE = new ImpactFeedback();
	private static final int MAX_TRACKED = 512;

	private final Map<UUID, Integer> lastImpact = new HashMap<>();
	private final Map<UUID, Integer> lastBlock = new HashMap<>();
	private final Map<UUID, Integer> lastHeal = new HashMap<>();
	private final Map<UUID, Integer> lastDenied = new HashMap<>();

	private ImpactFeedback() {
	}

	public static void register() {
		CombatEvents.addListener(INSTANCE);
		// Bloqueo con escudo: lo vive la víctima, no el atacante, así que no pasa por el bus de daño de los jugadores.
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (!blocked || !(entity instanceof ServerPlayerEntity victim) || victim.getServer() == null) return;
			int now = victim.getServer().getTicks();
			if (!due(INSTANCE.lastBlock, victim.getUuid(), now, ImpactFeedbackRules.SLOW_INTERVAL_TICKS)) return;
			Vec3d at = new Vec3d(victim.getX(), victim.getBodyY(0.7), victim.getZ());
			FxEvents.onAt(victim, at, FxIds.FEEDBACK_BLOCK, colorOf(victim), 0f, victim.getRotationVec(1.0f), victim.getRandom().nextInt());
			victim.getWorld().playSound(null, at.x, at.y, at.z, ModSounds.FEEDBACK_BLOCK, SoundCategory.PLAYERS, 0.8f, 0.95f + victim.getRandom().nextFloat() * 0.1f);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity p) forget(p);
		});
	}

	public static void forget(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		INSTANCE.lastImpact.remove(id);
		INSTANCE.lastBlock.remove(id);
		INSTANCE.lastHeal.remove(id);
		INSTANCE.lastDenied.remove(id);
	}

	private static boolean due(Map<UUID, Integer> map, UUID id, int now, int interval) {
		if (map.size() > MAX_TRACKED) map.clear();
		Integer last = map.get(id);
		if (!ImpactFeedbackRules.due(last == null ? -1 : last, now, interval)) return false;
		map.put(id, now);
		return true;
	}

	private static int colorOf(ServerPlayerEntity player) {
		Role primary = RoleManager.get(player).primary();
		return CastFx.colorOf(player, primary);
	}

	/** Un golpe con crítico vanilla: el jugador cae, no corre, no está en el agua ni ciego y el golpe es un ataque directo. */
	static boolean isVanillaCritical(ServerPlayerEntity attacker, CombatEvents.DamageEvent e) {
		return e.vanillaSource() && attacker.fallDistance > 0 && !attacker.isOnGround() && !attacker.isClimbing()
				&& !attacker.isTouchingWater() && !attacker.hasStatusEffect(StatusEffects.BLINDNESS) && !attacker.hasVehicle()
				&& !attacker.isSprinting();
	}

	@Override
	public void onDamage(CombatEvents.DamageEvent e) {
		ServerPlayerEntity attacker = e.attacker();
		if (e.autonomous()) return; // el daño de invocaciones y mascotas no da señal (sería ruido)
		Kind kind = ImpactFeedbackRules.classify(e.amount(), e.victim().getMaxHealth(), isVanillaCritical(attacker, e), e.abilityId() != null);
		if (kind == null) return;
		if (!due(lastImpact, attacker.getUuid(), e.tick(), ImpactFeedbackRules.IMPACT_INTERVAL_TICKS)) return;
		LivingEntity victim = e.victim();
		Vec3d at = new Vec3d(victim.getX(), victim.getBodyY(0.6), victim.getZ());
		float weight = ImpactFeedbackRules.weight(e.amount(), victim.getMaxHealth());
		FxEvents.at(attacker.getServerWorld(), at, FxIds.FEEDBACK_IMPACT, colorOf(attacker), kind.ordinal(), new Vec3d(weight, 0, 0), attacker.getRandom().nextInt());
		SoundEvent sound = kind == Kind.CRITICAL ? ModSounds.FEEDBACK_CRIT : kind == Kind.HEAVY ? ModSounds.FEEDBACK_HEAVY : null;
		if (sound != null) {
			victim.getWorld().playSound(null, at.x, at.y, at.z, sound, SoundCategory.PLAYERS, 0.7f + 0.3f * weight, 0.92f + attacker.getRandom().nextFloat() * 0.16f);
		}
	}

	@Override
	public void onHeal(ServerPlayerEntity healer, LivingEntity target, double effective, double over, int tick) {
		if (effective < ImpactFeedbackRules.MIN_HEAL_SIGNAL) return;
		if (!due(lastHeal, target.getUuid(), tick, ImpactFeedbackRules.SLOW_INTERVAL_TICKS)) return;
		Vec3d at = new Vec3d(target.getX(), target.getBodyY(0.5), target.getZ());
		FxEvents.at(healer.getServerWorld(), at, FxIds.FEEDBACK_HEAL, colorOf(healer), (float) effective, Vec3d.ZERO, healer.getRandom().nextInt());
		target.getWorld().playSound(null, at.x, at.y, at.z, ModSounds.FEEDBACK_HEAL, SoundCategory.PLAYERS, 0.6f, 1.0f);
	}

	/** Recurso insuficiente: un golpecito discreto, solo para quien lo intenta y como mucho cada medio segundo. */
	public static void denied(ServerPlayerEntity player) {
		if (player.getServer() == null) return;
		if (!due(INSTANCE.lastDenied, player.getUuid(), player.getServer().getTicks(), ImpactFeedbackRules.SLOW_INTERVAL_TICKS)) return;
		player.playSoundToPlayer(ModSounds.FEEDBACK_DENIED, SoundCategory.PLAYERS, 0.5f, 1.0f);
	}
}
