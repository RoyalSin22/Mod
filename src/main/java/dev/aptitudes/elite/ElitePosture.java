package dev.aptitudes.elite;

import dev.aptitudes.boss.AttackTags;
import dev.aptitudes.boss.PostureRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Fase B4 del plan de avance: postura de élites y campeones (mobs con la etiqueta de élite; los campeones también la
 * llevan). Solo drenan los golpes de jugadores. Al romperse, la IA del mob se pausa {@link PostureRules#STUN_TICKS} ticks
 * y después se reanuda.
 *
 * <p>Limitaciones: los mobs no tienen forma de multiplicar el daño recibido sin mixins, así que no hay vulnerabilidad
 * ×1,5 como en los jefes. El estado vive en memoria: no persiste al recargar el mundo. Valores provisionales, pendientes
 * de validar en partida.</p>
 */
public final class ElitePosture {
	/** Postura máxima de un élite o campeón (menor que la de los jefes). Provisional. */
	public static final double MAX = 60;
	/** Recuperación por tick (2 por segundo). */
	public static final double RECOVERY_PER_TICK = 0.1;

	/** Un mob con postura activa o con la IA pausada por un aturdimiento. */
	private record Tracked(MobEntity mob, PostureRules.Posture posture, boolean aiPaused) {}

	private static final Map<UUID, Tracked> TRACKED = new HashMap<>();

	private ElitePosture() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (!(entity instanceof MobEntity mob) || !Elites.isElite(mob) || blocked || damageTaken <= 0) return;
			if (!(source.getAttacker() instanceof PlayerEntity)) return;
			if (!(mob.getWorld() instanceof ServerWorld world)) return;
			Tracked previous = TRACKED.get(mob.getUuid());
			PostureRules.Posture posture = previous == null ? PostureRules.Posture.fresh(MAX) : previous.posture();
			boolean paused = previous != null && previous.aiPaused();
			Set<AttackTags.Tag> tags = AttackTags.playerHit(((PlayerEntity) source.getAttacker()).getAttackCooldownProgress(0.5f), source.getSource() == source.getAttacker());
			PostureRules.Result r = PostureRules.drain(posture, damageTaken, tags, world.getTime());
			if (r.broke()) {
				mob.setAiDisabled(true);
				paused = true;
				world.spawnParticles(ParticleTypes.CRIT, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
				world.playSound(null, mob.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.HOSTILE, 1f, 0.8f);
			}
			TRACKED.put(mob.getUuid(), new Tracked(mob, r.state(), paused));
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> TRACKED.clear());
	}

	/** Recupera postura, reanuda la IA al acabar el aturdimiento y olvida los mobs que ya no hacen falta. */
	private static void tick() {
		// Se recogen los cambios y se aplican al final: no se modifica el mapa mientras se recorre.
		List<UUID> finished = new ArrayList<>();
		Map<UUID, Tracked> updated = new HashMap<>();
		for (Map.Entry<UUID, Tracked> entry : TRACKED.entrySet()) {
			Tracked t = entry.getValue();
			MobEntity mob = t.mob();
			if (!mob.isAlive() || mob.isRemoved()) {
				finished.add(entry.getKey());
				continue;
			}
			long now = mob.getWorld().getTime();
			PostureRules.Posture p = PostureRules.recover(t.posture(), RECOVERY_PER_TICK, now);
			boolean paused = t.aiPaused();
			if (paused && !p.stunned(now)) {
				mob.setAiDisabled(false);
				paused = false;
			}
			if (!paused && p.current() >= p.max()) finished.add(entry.getKey());
			else updated.put(entry.getKey(), new Tracked(mob, p, paused));
		}
		finished.forEach(TRACKED::remove);
		TRACKED.putAll(updated);
	}
}
