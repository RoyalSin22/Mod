package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.ProgressRules;
import dev.aptitudes.data.Skill;
import dev.aptitudes.network.SyncProgressPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Única puerta de entrada para modificar el progreso de un jugador (servidor).
 * Toda mutación termina en {@link #refresh} o {@link #sync}, garantizando que
 * atributos y cliente quedan coherentes con los datos.
 */
public final class ProgressManager {
	private ProgressManager() {
	}

	public static void registerLifecycleEvents() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> refresh(handler.getPlayer()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			XpEvents.forget(handler.getPlayer());
			SkillMilestones.forget(handler.getPlayer());
			dev.aptitudes.ability.RoleArmorSets.forget(handler.getPlayer());
			dev.aptitudes.ability.RoleMechanics.forget(handler.getPlayer());
			dev.aptitudes.accessory.Accessories.forget(handler.getPlayer());
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// La entidad nueva no hereda modificadores de atributos: hay que reaplicarlos.
			refresh(newPlayer);
			if (!alive) newPlayer.setHealth(newPlayer.getMaxHealth());
		});
	}

	public static PlayerProgress get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.PROGRESS);
	}

	public static ProgressRules rules() {
		return ProgressRules.from(Aptitudes.config());
	}

	/** Multiplicador de afinidad de rol para un atributo: ×1,5 (principal) o ×1,25 (secundario). */
	public static double affinityMultiplier(ServerPlayerEntity player, PlayerAttribute attribute) {
		dev.aptitudes.role.RoleData roles = player.getAttached(ModAttachments.ROLES);
		if (roles == null) return 1.0;
		AptitudesConfig c = Aptitudes.config();
		if (roles.primary() != null && roles.primary().hasAffinity(attribute)) return 1.0 + c.primaryAffinityBonus;
		if (roles.secondary() != null && roles.secondary().hasAffinity(attribute)) return 1.0 + c.secondaryAffinityBonus;
		return 1.0;
	}

	/** Valor de un punto de atributo para este jugador (con afinidad de rol). */
	/** Puntos efectivos tras el escalado configurado ({@code attributeScaling}). */
	public static double effectivePoints(int points) {
		AptitudesConfig c = Aptitudes.config();
		AttributeScaling.Mode mode = AttributeScaling.Mode.parse(c.attributeScaling);
		return AttributeScaling.effectivePoints(mode == null ? AttributeScaling.Mode.LINEAR : mode, points,
				c.attributeSoftCapFirst, c.attributeSoftCapSecond, c.attributeSaturationPoints);
	}

	/** Valor marginal (en puntos efectivos) del siguiente punto invertido. */
	public static double marginalPoint(int points) {
		return effectivePoints(points + 1) - effectivePoints(points);
	}

	public static double effectivePerPoint(ServerPlayerEntity player, PlayerAttribute attribute) {
		return attribute.perPoint(Aptitudes.config()) * affinityMultiplier(player, attribute);
	}

	public static int availablePoints(PlayerProgress progress) {
		AptitudesConfig c = Aptitudes.config();
		long earned = (long) c.startingPoints
				+ (long) progress.totalLevels() * c.pointsPerSkillLevel
				+ progress.getBonusPoints()
				+ progress.getSoulLevel() / Math.max(1, c.soulLevelsPerPoint);
		return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, earned - progress.totalAllocated()));
	}

	// ------------------------------------------------------------------ XP

	public static void addXp(ServerPlayerEntity player, Skill skill, int amount) {
		addXp(player, skill, amount, false);
	}

	/** @param force si es {@code true} ignora las restricciones de modo de juego (comandos de admin). */
	public static void addXp(ServerPlayerEntity player, Skill skill, int amount, boolean force) {
		if (amount <= 0) return;
		AptitudesConfig cfg = Aptitudes.config();
		if (!force && (player.isSpectator() || (player.isCreative() && !cfg.awardXpInCreative))) return;

		PlayerProgress progress = get(player);
		ProgressRules rules = rules();
		int level = progress.getLevel(skill);
		if (level >= rules.maxLevel()) return;

		long xp = (long) progress.getXp(skill) + amount;
		int gained = 0;
		while (level < rules.maxLevel() && xp >= rules.xpToNext(level)) {
			xp -= rules.xpToNext(level);
			level++;
			gained++;
		}
		if (level >= rules.maxLevel()) xp = 0;

		progress.setLevel(skill, level);
		progress.setXp(skill, (int) xp);

		boolean notified = false;
		if (ServerPlayNetworking.canSend(player, dev.aptitudes.network.SkillXpPayload.ID)) {
			ServerPlayNetworking.send(player, new dev.aptitudes.network.SkillXpPayload(skill.ordinal(), amount, (int) xp,
					level >= rules.maxLevel() ? 0 : rules.xpToNext(level), level));
			notified = true;
		}
		if (gained > 0) {
			onLevelUp(player, skill, level - gained, level, gained * cfg.pointsPerSkillLevel);
			refresh(player);
		} else {
			if (cfg.showXpInActionBar && !notified) {
				player.sendMessage(Text.translatable("aptitudes.xp_gain", amount,
						Text.translatable(skill.translationKey()), xp, rules.xpToNext(level))
						.formatted(Formatting.GRAY), true);
			}
			sync(player);
		}
	}

	private static void onLevelUp(ServerPlayerEntity player, Skill skill, int oldLevel, int newLevel, int pointsGained) {
		// Hito alcanzado (10, 20, 30, 40, 50): aviso propio con la ventaja desbloqueada.
		int milestone = SkillMilestones.crossed(oldLevel, newLevel);
		if (Aptitudes.config().levelUpTitles) {
			Text title = Text.translatable("title.aptitudes.skill_up", Text.translatable(skill.translationKey()), newLevel)
					.formatted(Formatting.GOLD);
			Text subtitle = milestone > 0
					? Text.translatable("title.aptitudes.milestone", Text.translatable(SkillMilestones.key(skill, milestone)))
					.formatted(Formatting.LIGHT_PURPLE)
					: Text.translatable("title.aptitudes.skill_up_sub").formatted(Formatting.GRAY);
			player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(5, 40, 15));
			player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitle));
			player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(title));
		}
		var world = player.getServerWorld();
		var feet = player.getPos();
		dev.aptitudes.ability.ParticleFx.runeCircle(world, feet, 1.6, dev.aptitudes.ability.ParticleFx.dust(0xFFD54F, 1.2f));
		dev.aptitudes.ability.ParticleFx.spiral(world, feet, 0.9, 2.4, net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, 30, 2);
		if (milestone > 0) {
			player.sendMessage(Text.translatable("aptitudes.milestone.unlocked", Text.translatable(skill.translationKey()),
					milestone, Text.translatable(SkillMilestones.key(skill, milestone) + ".desc"))
					.formatted(Formatting.LIGHT_PURPLE), false);
			world.spawnParticles(net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getBodyY(0.5),
					player.getZ(), 50, 0.6, 1.0, 0.6, 0.3);
		}
		if (Aptitudes.config().levelUpMessages) {
			player.sendMessage((pointsGained > 0
					? Text.translatable("aptitudes.levelup", Text.translatable(skill.translationKey()), newLevel, pointsGained)
					: Text.translatable("aptitudes.levelup_skill", Text.translatable(skill.translationKey()), newLevel))
					.formatted(Formatting.GOLD), false);
		}
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.3f);
	}

	// ------------------------------------------------------------ Atributos

	/** @return puntos realmente asignados (puede ser menor que lo pedido). */
	public static int allocate(ServerPlayerEntity player, PlayerAttribute attribute, int requested) {
		PlayerProgress progress = get(player);
		int current = progress.getAllocated(attribute);
		int room = Aptitudes.config().maxPointsPerAttribute - current;
		int amount = Math.min(requested, Math.min(room, availablePoints(progress)));
		if (amount <= 0) return 0;
		progress.setAllocated(attribute, current + amount);
		refresh(player);
		return amount;
	}

	public static void respec(ServerPlayerEntity player) {
		get(player).clearAllocations();
		refresh(player);
	}

	// ---------------------------------------------------------------- Admin

	public static void setLevel(ServerPlayerEntity player, Skill skill, int level) {
		PlayerProgress progress = get(player);
		progress.setLevel(skill, Math.min(level, Aptitudes.config().maxSkillLevel));
		progress.setXp(skill, 0);
		refresh(player);
	}

	public static void addBonusPoints(ServerPlayerEntity player, int amount) {
		PlayerProgress progress = get(player);
		progress.setBonusPoints(adjustBonusPoints(progress.getBonusPoints(), amount));
		refresh(player);
	}

	/** Suma saturada en [0, Integer.MAX_VALUE]: /aptitudes admin addpoints nunca desborda a negativo. */
	public static int adjustBonusPoints(int current, int delta) {
		return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, (long) current + delta));
	}

	public static void reset(ServerPlayerEntity player) {
		player.setAttached(ModAttachments.PROGRESS, new PlayerProgress());
		refresh(player);
	}

	public static void refreshAll(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			refresh(player);
		}
	}

	// ------------------------------------------------------- Coherencia/sync

	/** Valida invariantes, reaplica modificadores y sincroniza con el cliente. */
	public static void refresh(ServerPlayerEntity player) {
		PlayerProgress progress = get(player);
		ensureConsistent(player, progress);
		AttributeApplier.apply(player, progress, Aptitudes.config());
		sync(player);
	}

	private static void ensureConsistent(ServerPlayerEntity player, PlayerProgress progress) {
		AptitudesConfig cfg = Aptitudes.config();
		for (Skill s : Skill.values()) {
			if (progress.getLevel(s) > cfg.maxSkillLevel) progress.setLevel(s, cfg.maxSkillLevel);
		}
		for (PlayerAttribute a : PlayerAttribute.values()) {
			if (progress.getAllocated(a) > cfg.maxPointsPerAttribute) {
				progress.setAllocated(a, cfg.maxPointsPerAttribute);
			}
		}
		// Si la configuración cambió (o un admin bajó niveles) y hay más puntos
		// asignados de los ganados, se devuelven todos en lugar de quitar
		// puntos de forma arbitraria.
		if (availablePoints(progress) < 0) {
			progress.clearAllocations();
			player.sendMessage(Text.translatable("aptitudes.allocations_reset").formatted(Formatting.YELLOW), false);
		}
	}

	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, SyncProgressPayload.ID)) return; // cliente sin el mod
		PlayerProgress progress = get(player);
		int[] affinity = new int[PlayerAttribute.values().length];
		for (PlayerAttribute a : PlayerAttribute.values()) {
			affinity[a.ordinal()] = (int) Math.round((affinityMultiplier(player, a) - 1.0) * 100);
		}
		ServerPlayNetworking.send(player, SyncProgressPayload.of(progress, availablePoints(progress), rules(), affinity));
	}
}
