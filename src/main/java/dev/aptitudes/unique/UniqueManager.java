package dev.aptitudes.unique;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.data.Skill;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.morph.MorphForm;
import dev.aptitudes.morph.MorphManager;
import dev.aptitudes.network.CooldownPayload;
import dev.aptitudes.network.UniqueSyncPayload;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Asignación y uso de habilidades únicas.
 *
 * <p>Reglas: cada habilidad tiene un único dueño en el servidor y cada jugador
 * puede tener como máximo una. El primero en completar la gesta la obtiene.
 * Si el dueño no se conecta en {@code uniqueReleaseAfterDays} días, se libera
 * para que otro pueda conseguirla (diversidad, nadie la acapara para siempre).</p>
 */
public final class UniqueManager {
	private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;
	/** Jugadores ya avisados de que una gesta completada tiene dueño (evita spam). */
	private static final Map<UUID, Set<UniqueSkill>> NOTIFIED = new HashMap<>();
	private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();
	private static final Map<UUID, int[]> LAST_PROGRESS = new HashMap<>();

	private UniqueManager() {
	}

	public static UniqueProgress progressData(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.UNIQUE_PROGRESS);
	}

	/**
	 * Legendaria activa del jugador: la del rol principal si tiene aprendido su nodo
	 * legendario; si no, la del secundario.
	 */
	@Nullable
	public static UniqueSkill skillOf(ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		for (Role role : new Role[]{data.primary(), data.secondary()}) {
			if (role != null && data.rank(legendNode(role)) > 0) return UniqueSkill.forRole(role);
		}
		return null;
	}

	/** Id del nodo legendario de un rol. */
	public static String legendNode(Role role) {
		return role.key() + "_legend";
	}

	public static boolean has(ServerPlayerEntity player, UniqueSkill skill) {
		return skillOf(player) == skill;
	}

	// ------------------------------------------------------------- Gestas

	/** Progreso actual de la gesta de {@code skill} para {@code player} (se compara con {@link UniqueSkill#target}). */
	public static int progress(ServerPlayerEntity player, UniqueSkill skill) {
		UniqueProgress p = progressData(player);
		var stats = player.getStatHandler();
		return switch (skill) {
			case ADAPTATION -> p.damageTypeCount();
			case GREED -> player.getInventory().count(Items.EMERALD_BLOCK);
			case VOID_ARCHIVE -> {
				RoleData data = RoleManager.get(player);
				yield (int) Arrays.stream(Role.values()).filter(r -> data.level(r) >= 10).count();
			}
			case FUNGAL -> p.myceliumKills;
			case PARASITE -> p.lowHpKills;
			case HELLFIRE -> p.lavaSeconds;
			case CHOSEN_KING -> (p.wither ? 1 : 0) + (p.dragon ? 1 : 0);
			case SPACETIME -> stats.getStat(Stats.USED, Items.ENDER_PEARL);
			case NEGATION -> stats.getStat(Stats.USED, Items.MILK_BUCKET);
			case GENETICIST -> stats.getStat(Stats.CUSTOM, Stats.ANIMALS_BRED);
			case MIRROR -> (int) Arrays.stream(MorphForm.values()).filter(f -> MorphManager.isUnlocked(player, f)).count();
			case EXPLOITER -> ProgressManager.get(player).getLevel(Skill.MINING);
			case PENDRAGON -> {
				RoleData data = RoleManager.get(player);
				yield Math.max(data.tier(Role.SWORDSMAN), data.tier(Role.PALADIN));
			}
			case SKY_LORD -> stats.getStat(Stats.CUSTOM, Stats.AVIATE_ONE_CM) / 100_000; // km
			case STILLNESS -> stats.getStat(Stats.CUSTOM, Stats.SNEAK_TIME) / 1200;      // minutos
			case HARBINGER -> stats.getStat(Stats.CUSTOM, Stats.DEATHS);
			case SKYPIERCER, WORLD_TREE, LAST_BULLET, GATE_OF_ARMS -> 0;
		};
	}

	/** Cada 5 s: liberaciones por inactividad, nuevas asignaciones y progreso a los clientes. */
	public static void checkAll(MinecraftServer server) {
		// v12: las legendarias se aprenden en el árbol; ya no hay gestas ni asignación por servidor.
		if (true) return;
		UniqueState state = UniqueState.get(server);
		boolean changed = releaseInactive(server, state);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			UniqueSkill owned = state.skillOf(player.getUuid());
			if (owned != null) state.touch(owned);
			for (UniqueSkill skill : UniqueSkill.values()) {
				if (progress(player, skill) < skill.target()) continue;
				if (owned == null && state.holder(skill) == null) {
					grant(server, state, player, skill);
					owned = skill;
					changed = true;
				} else if (owned != skill && NOTIFIED.computeIfAbsent(player.getUuid(), k -> new HashSet<>()).add(skill)) {
					UniqueState.Holder holder = state.holder(skill);
					player.sendMessage(Text.translatable(holder != null ? "aptitudes.unique.taken" : "aptitudes.unique.already_own",
							Text.translatable(skill.translationKey()), holder != null ? holder.name() : "")
							.formatted(Formatting.GRAY), false);
				}
			}
			sendProgressIfChanged(player);
		}
		if (changed) syncAll(server);
	}

	private static boolean releaseInactive(MinecraftServer server, UniqueState state) {
		int days = Aptitudes.config().uniqueReleaseAfterDays;
		if (days <= 0) return false;
		long limit = System.currentTimeMillis() - days * DAY_MILLIS;
		boolean changed = false;
		for (UniqueSkill skill : UniqueSkill.values()) {
			UniqueState.Holder h = state.holder(skill);
			if (h != null && h.lastSeenMillis() < limit && server.getPlayerManager().getPlayer(h.uuid()) == null) {
				state.release(skill);
				server.getPlayerManager().broadcast(Text.translatable("aptitudes.unique.released",
						Text.translatable(skill.translationKey()), h.name()).formatted(Formatting.AQUA), false);
				changed = true;
			}
		}
		return changed;
	}

	private static void grant(MinecraftServer server, UniqueState state, ServerPlayerEntity player, UniqueSkill skill) {
		state.assign(skill, player.getUuid(), player.getGameProfile().getName());
		server.getPlayerManager().broadcast(Text.translatable("aptitudes.unique.granted",
				player.getDisplayName(), Text.translatable(skill.translationKey()))
				.withColor(skill.color()), false);
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
					SoundCategory.MASTER, 0.6f, 0.7f);
		}
		refreshAttributes(player);
	}

	// ------------------------------------------------------------- Admin

	/** Comando: concede el nodo legendario de {@code skill} (rango 1 en el árbol de su rol). */
	public static boolean give(ServerPlayerEntity player, UniqueSkill skill) {
		RoleManager.get(player).setRank(legendNode(skill.role()), 1);
		RoleManager.refresh(player);
		return true;
	}

	/** Comando: quita el nodo legendario de {@code skill} a todos los jugadores conectados. */
	public static void revoke(MinecraftServer server, UniqueSkill skill) {
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			RoleManager.get(p).setRank(legendNode(skill.role()), 0);
			RoleManager.refresh(p);
		}
	}

	private static boolean giveLegacy(ServerPlayerEntity player, UniqueSkill skill) {
		MinecraftServer server = player.getServer();
		UniqueState state = UniqueState.get(server);
		UniqueSkill previous = state.skillOf(player.getUuid());
		if (previous != null) state.release(previous);
		UniqueState.Holder holder = state.holder(skill);
		if (holder != null) {
			state.release(skill);
			ServerPlayerEntity old = server.getPlayerManager().getPlayer(holder.uuid());
			if (old != null) refreshAttributes(old);
		}
		grant(server, state, player, skill);
		syncAll(server);
		return true;
	}

	private static void revokeLegacy(MinecraftServer server, UniqueSkill skill) {
		UniqueState state = UniqueState.get(server);
		UniqueState.Holder holder = state.holder(skill);
		state.release(skill);
		if (holder != null) {
			ServerPlayerEntity old = server.getPlayerManager().getPlayer(holder.uuid());
			if (old != null) refreshAttributes(old);
		}
		syncAll(server);
	}

	// --------------------------------------------------------------- Uso

	public static void use(ServerPlayerEntity player) {
		UniqueSkill skill = skillOf(player);
		if (skill == null) {
			fail(player, Text.translatable("aptitudes.unique.none"));
			return;
		}
		if (UniqueEffects.isNegated(player)) {
			fail(player, Text.translatable("aptitudes.unique.negated"));
			return;
		}
		int now = AbilityManager.now(player);
		Integer until = COOLDOWNS.get(player.getUuid());
		if (until != null && until > now) {
			fail(player, Text.translatable("aptitudes.ability.cooldown", Text.translatable(skill.translationKey()),
					(until - now + 19) / 20));
			return;
		}
		RoleData data = RoleManager.get(player);
		if (!player.isCreative() && data.energy() < skill.energyCost()) {
			fail(player, Text.translatable("aptitudes.ability.no_energy", skill.energyCost()));
			return;
		}
		if (!UniqueEffects.activate(player, skill)) {
			fail(player, Text.translatable("aptitudes.ability.no_effect"));
			return;
		}
		dev.aptitudes.ability.CastFx.onCast(player, null, dev.aptitudes.ability.CastFx.Kind.UNIQUE);
		if (!player.isCreative()) data.setEnergy(data.energy() - skill.energyCost());
		int cooldown = skill.cooldownSecs() * 20;
		COOLDOWNS.put(player.getUuid(), now + cooldown);
		if (ServerPlayNetworking.canSend(player, CooldownPayload.ID)) {
			ServerPlayNetworking.send(player, new CooldownPayload(AbilityManager.UNIQUE_SLOT, cooldown));
		}
		AbilityManager.sendEnergy(player, false);
	}

	private static void fail(ServerPlayerEntity player, Text text) {
		player.sendMessage(text.copy().formatted(Formatting.RED), true);
	}

	// --------------------------------------------------------- Ciclo de vida

	public static void onJoin(ServerPlayerEntity player) {
		UniqueState state = UniqueState.get(player.getServer());
		UniqueSkill owned = state.skillOf(player.getUuid());
		if (owned != null) state.touch(owned);
		refreshAttributes(player);
		LAST_PROGRESS.remove(player.getUuid());
		sync(player);
	}

	public static void onLeave(ServerPlayerEntity player) {
		UniqueState state = UniqueState.get(player.getServer());
		UniqueSkill owned = state.skillOf(player.getUuid());
		if (owned != null) state.touch(owned);
		NOTIFIED.remove(player.getUuid());
		LAST_PROGRESS.remove(player.getUuid());
		UniqueEffects.forget(player);
	}

	public static void refreshAttributes(ServerPlayerEntity player) {
		UniqueEffects.applyAttributes(player, skillOf(player));
	}

	// -------------------------------------------------------------- Red

	public static void syncAll(MinecraftServer server) {
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) sync(p);
	}

	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, UniqueSyncPayload.ID)) return;
		UniqueState state = UniqueState.get(player.getServer());
		UniqueSkill[] skills = UniqueSkill.values();
		String[] holders = new String[skills.length];
		int[] progress = new int[skills.length];
		for (UniqueSkill s : skills) {
			UniqueState.Holder h = state.holder(s);
			holders[s.ordinal()] = h == null ? "" : h.name();
			progress[s.ordinal()] = Math.min(progress(player, s), s.target());
		}
		UniqueSkill own = skillOf(player);
		LAST_PROGRESS.put(player.getUuid(), progress);
		ServerPlayNetworking.send(player, new UniqueSyncPayload(holders, progress, own == null ? -1 : own.ordinal()));
	}

	private static void sendProgressIfChanged(ServerPlayerEntity player) {
		int[] last = LAST_PROGRESS.get(player.getUuid());
		for (UniqueSkill s : UniqueSkill.values()) {
			int current = Math.min(progress(player, s), s.target());
			if (last == null || last.length <= s.ordinal() || last[s.ordinal()] != current) {
				sync(player);
				return;
			}
		}
	}
}
