package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.Skill;
import dev.aptitudes.network.MasteryPayload;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/** Única concesión de XP de maestría en el servidor; el historial vive en el progreso con copyOnDeath. */
public final class MasteryManager {
	private MasteryManager() { }
	public static boolean eligible(ServerPlayerEntity player, boolean gameplay) {
		return MasteryRules.canEarn(gameplay, Aptitudes.config().masteryEnabled, player.isAlive(), player.isCreative(), player.isSpectator());
	}
	public static double skillMultiplier(PlayerProgress progress, Skill skill, AptitudesConfig c) {
		return 1 + (c.masteryEnabled && MasteryRules.atCap(progress.getLevel(skill), c.maxSkillLevel, 1)
				? bonus(progress.mastery().xp("skill/" + skill.key()), c) : 0);
	}
	private static double bonus(long xp, AptitudesConfig c) {
		return MasteryRules.bonus(MasteryRules.points(xp, c.masteryXpBase, c.masteryXpStep), c.masteryMaxBonus);
	}
	public static double skillMultiplier(ServerPlayerEntity player, Skill skill) {
		return skillMultiplier(ProgressManager.get(player), skill, Aptitudes.config());
	}
	public static void gainSkill(ServerPlayerEntity player, Skill skill, int amount) {
		var c = Aptitudes.config();
		var progress = ProgressManager.get(player);
		double before = skillMultiplier(progress, skill, c);
		progress.mastery().add("skill/" + skill.key(), amount);
		if (skillMultiplier(progress, skill, c) != before) ProgressManager.refresh(player);
		else ProgressManager.sync(player);
	}
	public static double roleBonus(ServerPlayerEntity player, Role role) {
		var c = Aptitudes.config();
		return c.masteryEnabled && role != null && MasteryRules.atCap(RoleManager.get(player).level(role), c.maxRoleLevel, 50)
				? bonus(ProgressManager.get(player).mastery().xp("role/" + role.key()), c) : 0;
	}
	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, MasteryPayload.ID)) return;
		var saved = ProgressManager.get(player).mastery();
		long[] skills = new long[Skill.values().length], roles = new long[Role.values().length];
		for (Skill skill : Skill.values()) skills[skill.ordinal()] = saved.xp("skill/" + skill.key());
		for (Role role : Role.values()) roles[role.ordinal()] = saved.xp("role/" + role.key());
		var c = Aptitudes.config();
		ServerPlayNetworking.send(player, new MasteryPayload(skills, roles, c.masteryXpBase, c.masteryXpStep, c.masteryMaxBonus, c.masteryEnabled));
	}
}
