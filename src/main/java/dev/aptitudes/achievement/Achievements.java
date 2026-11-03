package dev.aptitudes.achievement;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.contract.ContractManager;
import dev.aptitudes.contract.ContractRules;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.Skill;
import dev.aptitudes.event.WorldEventKind;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Concesión de logros (v31). Solo el servidor concede criterios; el cliente muestra la pestaña de avances
 * vanilla. Conceder un logro ya obtenido no hace nada, así que los ganchos pueden llamarse sin comprobarlo.
 *
 * <p>Los logros que dependen de un estado guardado (rol, evolución, nivel de alma, habilidades y contratos) se
 * revisan también al entrar ({@link #checkState}), de modo que los jugadores de mundos anteriores a la v31 los
 * reciben sin repetir nada.</p>
 */
public final class Achievements {
	private Achievements() {}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> checkState(handler.getPlayer()));
	}

	/** Concede el logro (idempotente). Si el avance no existe (datapack que lo quita), no hace nada. */
	public static void grant(ServerPlayerEntity player, Achievement achievement) {
		if (player == null || achievement == Achievement.ROOT) return;
		MinecraftServer server = player.getServer();
		if (server == null) return;
		AdvancementEntry entry = server.getAdvancementLoader().get(Aptitudes.id(achievement.path()));
		if (entry == null) return;
		player.getAdvancementTracker().grantCriterion(entry, Achievement.CRITERION);
	}

	/** Revisa los logros de estado (retroactivos). Barato: solo lee datos ya cargados. */
	public static void checkState(ServerPlayerEntity player) {
		RoleData roles = RoleManager.get(player);
		if (roles.primary() != null || roles.secondary() != null) grant(player, Achievement.CHOOSE_ROLE);
		int bestTier = Evolution.BASE;
		for (Role role : Role.values()) bestTier = Math.max(bestTier, roles.tier(role));
		if (bestTier >= Evolution.MASTER) grant(player, Achievement.EVOLVE_MASTER);
		if (bestTier >= Evolution.ULTIMATE) grant(player, Achievement.EVOLVE_ULTIMATE);

		PlayerProgress progress = ProgressManager.get(player);
		if (progress.getSoulLevel() >= Achievement.SOUL_LEVEL_TARGET) grant(player, Achievement.SOUL_LEVEL);
		for (Skill skill : Skill.values()) {
			if (progress.getLevel(skill) >= Achievement.SKILL_LEVEL_TARGET) {
				grant(player, Achievement.SKILL_MASTERY);
				break;
			}
		}
		checkContracts(player);
	}

	/** Sobrevivir hasta el final a un evento del mundo. */
	public static void survived(ServerPlayerEntity player, WorldEventKind kind) {
		grant(player, switch (kind) {
			case ECLIPSE -> Achievement.SURVIVE_ECLIPSE;
			case SOUL_FOG -> Achievement.SURVIVE_SOUL_FOG;
			case EMBER_RAIN -> Achievement.SURVIVE_EMBER_RAIN;
		});
	}

	/** Contratos cobrados y rango del gremio. */
	public static void checkContracts(ServerPlayerEntity player) {
		int completed = ContractManager.completed(player);
		if (completed >= 1) grant(player, Achievement.FIRST_CONTRACT);
		if (completed >= Achievement.CONTRACTS_TARGET) grant(player, Achievement.CONTRACT_VETERAN);
		if (ContractRules.rankIndex(ContractManager.reputation(player)) >= Achievement.GUILD_RANK_TARGET) {
			grant(player, Achievement.GUILD_MASTER);
		}
	}
}
