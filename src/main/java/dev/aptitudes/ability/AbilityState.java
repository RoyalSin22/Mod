package dev.aptitudes.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Estado transitorio de habilidades con duración: contraataques, invocaciones
 * temporales (torretas del Ingeniero y esbirros del Nigromante) y mobs
 * sobornados por el Mercader. Todo en memoria y solo en el hilo del servidor.
 *
 * <p>Las invocaciones llevan además una etiqueta de comando (persistida en NBT):
 * si quedaron guardadas en disco tras un reinicio, se retiran al cargarse.</p>
 */
public final class AbilityState {
	/** Etiqueta común de toda invocación temporal. */
	public static final String SUMMON_TAG = "aptitudes.summon";
	/** Etiqueta adicional de los esbirros del Nigromante. */
	public static final String MINION_TAG = "aptitudes.minion";

	private record Riposte(int untilTick, float multiplier) {}
	private record Summon(RegistryKey<World> world, int expiresTick, UUID owner, int spawnTick) {}

	private static final Map<UUID, Riposte> RIPOSTES = new HashMap<>();
	private static final Map<UUID, Summon> SUMMONS = new HashMap<>();
	/** mob → tick hasta el que no puede tomar como objetivo a ningún jugador. */
	private static final Map<UUID, Integer> PACIFIED = new HashMap<>();
	private static final List<Entity> PENDING_DISCARD = new ArrayList<>();
	/** Tareas diferidas (habilidades con varias fases o efectos por oleadas). */
	private record Scheduled(int runAt, Runnable task) {}
	private static final List<Scheduled> SCHEDULED = new ArrayList<>();
	/** jugador → tick hasta el que no puede morir (la vida no baja de 1). */
	private static final Map<UUID, Integer> UNDYING = new HashMap<>();
	/** jugador → tick hasta el que ignora todo ataque de criaturas. */
	private static final Map<UUID, Integer> UNTOUCHABLE = new HashMap<>();
	/** jugador → tick en el que vuelve a estar disponible la Inmortalidad carmesí. */
	private static final Map<UUID, Integer> CRIMSON_READY = new HashMap<>();

	private AbilityState() {
	}

	// --------------------------------------------------------- Contraataque

	public static void openRiposte(ServerPlayerEntity player, int ticks, float multiplier) {
		RIPOSTES.put(player.getUuid(), new Riposte(AbilityManager.now(player) + ticks, multiplier));
	}

	/** Consume la ventana de contraataque si está abierta. @return multiplicador o 0. */
	public static float consumeRiposte(ServerPlayerEntity player) {
		Riposte r = RIPOSTES.remove(player.getUuid());
		return r != null && r.untilTick() >= AbilityManager.now(player) ? r.multiplier() : 0f;
	}

	// ----------------------------------------------------------- Invocaciones

	public static void trackSummon(Entity summon, ServerPlayerEntity owner, int lifetimeTicks) {
		int now = AbilityManager.now(owner);
		summon.addCommandTag(SUMMON_TAG);
		SUMMONS.put(summon.getUuid(), new Summon(summon.getWorld().getRegistryKey(), now + lifetimeTicks,
				owner.getUuid(), now));
	}

	public static boolean isSummon(Entity entity) {
		return entity.getCommandTags().contains(SUMMON_TAG);
	}

	public static boolean isMinion(Entity entity) {
		return entity.getCommandTags().contains(MINION_TAG);
	}

	public static boolean isTracked(Entity entity) {
		return SUMMONS.containsKey(entity.getUuid());
	}

	@Nullable
	public static ServerPlayerEntity ownerOf(Entity summon) {
		Summon s = SUMMONS.get(summon.getUuid());
		if (s == null || summon.getServer() == null) return null;
		return summon.getServer().getPlayerManager().getPlayer(s.owner());
	}

	/** Esbirros vivos de un jugador, del más antiguo al más reciente. */
	public static List<MobEntity> minionsOf(ServerPlayerEntity owner) {
		List<Map.Entry<UUID, Summon>> entries = new ArrayList<>();
		for (Map.Entry<UUID, Summon> e : SUMMONS.entrySet()) {
			if (e.getValue().owner().equals(owner.getUuid())) entries.add(e);
		}
		entries.sort(Comparator.comparingInt(e -> e.getValue().spawnTick()));
		List<MobEntity> out = new ArrayList<>();
		for (Map.Entry<UUID, Summon> e : entries) {
			ServerWorld world = owner.getServer() == null ? null : owner.getServer().getWorld(e.getValue().world());
			Entity entity = world == null ? null : world.getEntity(e.getKey());
			if (entity instanceof MobEntity mob && isMinion(mob) && mob.isAlive()) out.add(mob);
		}
		return out;
	}

	public static void scheduleDiscard(Entity entity) {
		PENDING_DISCARD.add(entity);
	}

	// ---------------------------------------------------------------- Soborno

	public static void pacify(MobEntity mob, int untilTick) {
		PACIFIED.put(mob.getUuid(), untilTick);
		mob.setTarget(null);
	}

	/**
	 * Reglas de objetivo aplicadas desde {@code MobEntityMixin#setTarget}:
	 * <ul>
	 *   <li>las invocaciones nunca atacan a jugadores ni a otras invocaciones, y solo
	 *       eligen monstruos o mobs agresivos (no aldeanos ni animales);</li>
	 *   <li>los gólems de hierro no atacan a esbirros;</li>
	 *   <li>un mob sobornado no ataca a jugadores mientras dure el soborno.</li>
	 * </ul>
	 */
	public static boolean shouldBlockTarget(MobEntity mob, LivingEntity target) {
		if (isSummon(mob)) {
			if (target instanceof PlayerEntity || isSummon(target)) return true;
			// Solo monstruos o mobs que estén atacando a alguien (nunca aldeanos ni animales pacíficos).
			boolean aggressive = target instanceof Monster || (target instanceof MobEntity m && m.getTarget() != null);
			if (!aggressive) return true;
		}
		if (mob instanceof IronGolemEntity && isMinion(target)) return true;
		// Cambiaformas (Bromista): los monstruos no atacan a quien parece uno de ellos.
		if (mob instanceof Monster && target instanceof PlayerEntity player && dev.aptitudes.morph.MorphManager.isCamouflaged(player)) {
			return true;
		}
		// Única "Heraldo de la Muerte": los no-muertos no atacan a su portador.
		if (target instanceof net.minecraft.server.network.ServerPlayerEntity sp
				&& mob.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)
				&& dev.aptitudes.unique.UniqueManager.has(sp, dev.aptitudes.unique.UniqueSkill.HARBINGER)) {
			return true;
		}
		if (target instanceof PlayerEntity && mob.getServer() != null) {
			Integer until = PACIFIED.get(mob.getUuid());
			if (until != null) {
				if (until >= mob.getServer().getTicks()) return true;
				PACIFIED.remove(mob.getUuid());
			}
		}
		return false;
	}

	// ----------------------------------------------------- Tareas y estados

	public static void schedule(MinecraftServer server, int delayTicks, Runnable task) {
		SCHEDULED.add(new Scheduled(server.getTicks() + Math.max(1, delayTicks), task));
	}

	/** Ejecuta {@code body(i)} {@code times} veces, cada {@code interval} ticks, empezando ya. */
	public static void repeat(MinecraftServer server, int times, int interval, java.util.function.IntConsumer body) {
		for (int i = 0; i < times; i++) {
			final int index = i;
			if (i == 0) body.accept(0);
			else schedule(server, i * interval, () -> body.accept(index));
		}
	}

	public static void setUndying(ServerPlayerEntity player, int ticks) {
		UNDYING.merge(player.getUuid(), AbilityManager.now(player) + ticks, Math::max);
	}

	public static boolean isUndying(ServerPlayerEntity player) {
		Integer until = UNDYING.get(player.getUuid());
		return until != null && until >= AbilityManager.now(player);
	}

	/** jugador → tick hasta el que no recibe NINGÚN daño (Bastión sagrado). */
	private static final Map<UUID, Integer> INVULNERABLE = new HashMap<>();
	/** jugador → tick en que vuelve a estar disponible Renacer del bosque (Druida). */
	private static final Map<UUID, Integer> FOREST_REBIRTH_READY = new HashMap<>();

	public static void setInvulnerable(ServerPlayerEntity player, int ticks) {
		INVULNERABLE.put(player.getUuid(), AbilityManager.now(player) + ticks);
	}

	public static boolean isInvulnerable(ServerPlayerEntity player) {
		Integer until = INVULNERABLE.get(player.getUuid());
		return until != null && until >= AbilityManager.now(player);
	}

	/** @return true si Renacer del bosque estaba listo (y lo pone en enfriamiento). */
	public static boolean tryForestRebirth(ServerPlayerEntity player, int cooldownTicks) {
		int now = AbilityManager.now(player);
		Integer ready = FOREST_REBIRTH_READY.get(player.getUuid());
		if (ready != null && ready > now) return false;
		FOREST_REBIRTH_READY.put(player.getUuid(), now + cooldownTicks);
		return true;
	}

	public static void setUntouchable(ServerPlayerEntity player, int ticks) {
		UNTOUCHABLE.put(player.getUuid(), AbilityManager.now(player) + ticks);
	}

	public static boolean isUntouchable(ServerPlayerEntity player) {
		Integer until = UNTOUCHABLE.get(player.getUuid());
		return until != null && until >= AbilityManager.now(player);
	}

	/** @return true si la Inmortalidad carmesí estaba lista (y la pone en enfriamiento). */
	public static boolean tryCrimsonImmortality(ServerPlayerEntity player, int cooldownTicks) {
		int now = AbilityManager.now(player);
		Integer ready = CRIMSON_READY.get(player.getUuid());
		if (ready != null && ready > now) return false;
		CRIMSON_READY.put(player.getUuid(), now + cooldownTicks);
		return true;
	}

	// ------------------------------------------------------------------- Tick

	public static void tick(MinecraftServer server) {
		if (!SCHEDULED.isEmpty()) {
			int nowTick = server.getTicks();
			List<Scheduled> due = new ArrayList<>();
			SCHEDULED.removeIf(t -> {
				if (t.runAt() <= nowTick) {
					due.add(t);
					return true;
				}
				return false;
			});
			// Se copian antes de ejecutar: una tarea puede programar otras.
			for (Scheduled t : due) {
				try {
					t.task().run();
				} catch (RuntimeException e) {
					dev.aptitudes.Aptitudes.LOGGER.error("Error en una tarea de habilidad", e);
				}
			}
		}
		if (!PENDING_DISCARD.isEmpty()) {
			for (Entity e : PENDING_DISCARD) e.discard();
			PENDING_DISCARD.clear();
		}
		int now = server.getTicks();
		boolean aiTick = now % 10 == 0;
		Iterator<Map.Entry<UUID, Summon>> it = SUMMONS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Summon> entry = it.next();
			Summon summon = entry.getValue();
			ServerWorld world = server.getWorld(summon.world());
			Entity entity = world == null ? null : world.getEntity(entry.getKey());
			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(summon.owner());
			// Caduca, murió, o el dueño se desconectó: se retira.
			if (summon.expiresTick() <= now || owner == null || (entity != null && !entity.isAlive())) {
				if (entity != null) entity.discard();
				it.remove();
				continue;
			}
			if (aiTick && entity instanceof MobEntity mob && isMinion(mob)) {
				MinionBrain.tick(mob, owner);
			}
		}
		if (now % 200 == 0) PACIFIED.values().removeIf(until -> until < now);
	}

	public static void forget(ServerPlayerEntity player) {
		RIPOSTES.remove(player.getUuid());
		UNDYING.remove(player.getUuid());
		UNTOUCHABLE.remove(player.getUuid());
		INVULNERABLE.remove(player.getUuid());
	}
}
