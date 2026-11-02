package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.RolePassives;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.network.RoleSyncPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * Única vía de mutación de {@link RoleData} en el servidor. Mismo patrón que
 * {@code ProgressManager}: toda operación termina en {@link #refresh} o {@link #sync}.
 */
public final class RoleManager {
	/** Resultado de intentar cambiar un rol (para mostrar mensajes concretos). */
	public enum ChangeResult { OK, SAME, NEEDS_RITUAL }

	/** Resultado de intentar subir un rango. */
	public enum UpgradeResult { OK, UNKNOWN_NODE, ROLE_NOT_ACTIVE, MAX_RANK, LEVEL_TOO_LOW, MISSING_PREREQUISITE, NO_POINTS,
		PATH_LOCKED, LEGEND_TAKEN, LEGEND_LIMIT }

	private RoleManager() {
	}

	public static void registerLifecycleEvents() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			refresh(handler.getPlayer());
			dev.aptitudes.morph.MorphManager.onJoin(handler.getPlayer());
			dev.aptitudes.unique.UniqueManager.onJoin(handler.getPlayer());
			// Grimorio: una sola vez por jugador (la etiqueta se guarda con el jugador).
			var joined = handler.getPlayer();
			if (joined.addCommandTag(dev.aptitudes.Aptitudes.MOD_ID + ".got_guide")) {
				net.minecraft.item.ItemStack guide = new net.minecraft.item.ItemStack(dev.aptitudes.item.ModItems.GUIDE_BOOK);
				if (!joined.getInventory().insertStack(guide)) joined.dropItem(guide, false);
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			AbilityManager.forget(handler.getPlayer());
			dev.aptitudes.morph.MorphManager.onLeave(handler.getPlayer());
			dev.aptitudes.unique.UniqueManager.onLeave(handler.getPlayer());
			dev.aptitudes.souls.SoulsManager.forget(handler.getPlayer());
		});
		// La marca del grimorio debe sobrevivir a la muerte aunque vanilla no copiara las etiquetas:
		// se copia explícitamente (idempotente) para que nunca se entregue un segundo libro.
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			String tag = dev.aptitudes.Aptitudes.MOD_ID + ".got_guide";
			if (oldPlayer.getCommandTags().contains(tag)) newPlayer.addCommandTag(tag);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			refresh(newPlayer);
			// Al morir se pierde la transformación.
			if (!alive) dev.aptitudes.morph.MorphManager.unmorph(newPlayer);
			dev.aptitudes.unique.UniqueManager.refreshAttributes(newPlayer);
		});
	}

	public static RoleData get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.ROLES);
	}

	public static RoleRules rules() {
		return RoleRules.from(Aptitudes.config());
	}

	/**
	 * Rango efectivo de un nodo: 0 si su rol no es el principal ni el secundario
	 * o si no se ha aprendido; si no, rango comprado + nivel de evolución del rol
	 * (Maestro +1, Definitivo +2).
	 */
	public static int activeRank(ServerPlayerEntity player, String nodeId) {
		RoleNode node = RoleTree.get(nodeId);
		if (node == null) return 0;
		RoleData data = player.getAttached(ModAttachments.ROLES);
		if (data == null || !data.isActive(node.role())) return 0;
		int rank = data.rank(nodeId);
		return rank > 0 ? rank + data.tier(node.role()) : 0;
	}

	// ------------------------------------------------------------- Evolución

	public enum EvolveResult { OK, ROLE_NOT_ACTIVE, MAX_TIER, LEVEL_TOO_LOW, TRIAL_INCOMPLETE, NO_BOSS, MISSING_ITEM }

	/** Suma progreso a la prueba de Maestro si el rol está activo, aún es Base y la prueba es de ese tipo. */
	public static void recordTrial(ServerPlayerEntity player, Role role, Evolution.TrialType type, int amount) {
		RoleData data = get(player);
		Evolution.Trial trial = Evolution.trial(role);
		if (trial == null || trial.type() != type || !data.isActive(role) || data.tier(role) != Evolution.BASE) return;
		int before = data.trialProgress(role);
		if (before >= trial.target()) return;
		data.addTrialProgress(role, amount);
		if (data.trialProgress(role) >= trial.target()) {
			player.sendMessage(Text.translatable("aptitudes.evolve.trial_done",
					Text.translatable(role.translationKey())).formatted(Formatting.GOLD), false);
		}
		sync(player);
	}

	public static void markBossSlain(ServerPlayerEntity player) {
		RoleData data = get(player);
		boolean changed = false;
		for (Role role : new Role[]{data.primary(), data.secondary()}) {
			if (role != null && !data.hasSlainBoss(role)) {
				data.markBossSlain(role);
				changed = true;
				player.sendMessage(Text.translatable("aptitudes.evolve.boss_done",
						Text.translatable(role.translationKey())).formatted(Formatting.GOLD), false);
			}
		}
		if (changed) sync(player);
	}

	public static EvolveResult evolve(ServerPlayerEntity player, Role role) {
		RoleData data = get(player);
		if (!data.isActive(role)) return EvolveResult.ROLE_NOT_ACTIVE;
		int tier = data.tier(role);
		if (tier >= Evolution.ULTIMATE) return EvolveResult.MAX_TIER;
		boolean toMaster = tier == Evolution.BASE;
		if (data.level(role) < (toMaster ? Evolution.MASTER_LEVEL : Evolution.ULTIMATE_LEVEL)) {
			return EvolveResult.LEVEL_TOO_LOW;
		}
		if (toMaster && data.trialProgress(role) < Evolution.trial(role).target()) return EvolveResult.TRIAL_INCOMPLETE;
		if (!toMaster && !data.hasSlainBoss(role)) return EvolveResult.NO_BOSS;
		net.minecraft.item.Item item = toMaster ? dev.aptitudes.item.ModItems.MASTERY_EMBLEM
				: dev.aptitudes.item.ModItems.ULTIMATE_CORE;
		if (!player.isCreative() && !consumeOne(player, item)) return EvolveResult.MISSING_ITEM;

		data.setTier(role, tier + 1);
		dev.aptitudes.item.weapon.RoleWeapons.give(player, role, tier + 1); // arma nueva de la evolución
		player.getServer().getPlayerManager().broadcast(Text.translatable("aptitudes.evolve.broadcast",
				player.getDisplayName(), Text.translatable(Evolution.titleKey(role, tier + 1)))
				.formatted(Formatting.GOLD), false);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 0.8f);
		player.getServerWorld().spawnParticles(net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING,
				player.getX(), player.getBodyY(0.5), player.getZ(), 60, 0.6, 1.0, 0.6, 0.3);
		refresh(player);
		return EvolveResult.OK;
	}

	private static boolean consumeOne(ServerPlayerEntity player, net.minecraft.item.Item item) {
		var inventory = player.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			var stack = inventory.getStack(i);
			if (stack.isOf(item)) {
				stack.decrement(1);
				return true;
			}
		}
		return false;
	}

	public static int availablePoints(RoleData data, Role role) {
		return rules().earnedPoints(data.level(role)) - data.spentPoints(role);
	}

	// ------------------------------------------------------------------- XP

	public static void addRoleXp(ServerPlayerEntity player, Role role, int baseAmount) {
		addRoleXp(player, role, baseAmount, false);
	}

	/** @param force ignora el modo de juego y el multiplicador de secundario (comandos de admin). */
	public static void addRoleXp(ServerPlayerEntity player, Role role, int baseAmount, boolean force) {
		if (baseAmount <= 0) return;
		AptitudesConfig cfg = Aptitudes.config();
		RoleData data = get(player);
		double multiplier;
		if (force) {
			multiplier = 1.0;
		} else {
			if (player.isSpectator() || (player.isCreative() && !cfg.awardXpInCreative)) return;
			if (role == data.primary()) multiplier = 1.0;
			else if (role == data.secondary()) multiplier = cfg.secondaryRoleXpMultiplier;
			else return; // los roles no activos no progresan
		}
		// Especialización: principal y secundario de la misma categoría.
		if (!force && data.primary() != null && data.secondary() != null
				&& data.primary().category() == data.secondary().category()) {
			multiplier *= 1.0 + RoleCategory.SPECIALIZATION_XP_BONUS;
		}
		int amount = (int) Math.round(baseAmount * multiplier);
		if (amount <= 0) return;
		if (!force) RoleResources.gain(player, role, amount); // toda XP de rol carga su recurso

		RoleRules rules = rules();
		int level = data.level(role);
		if (level >= rules.maxLevel()) return;
		long xp = (long) data.xp(role) + amount;
		int before = level;
		while (level < rules.maxLevel() && xp >= rules.xpToNext(level)) {
			xp -= rules.xpToNext(level);
			level++;
		}
		if (level >= rules.maxLevel()) xp = 0;
		data.setLevel(role, level);
		data.setXp(role, (int) xp);

		if (level > before) {
			int newPoints = rules.earnedPoints(level) - rules.earnedPoints(before);
			player.sendMessage(Text.translatable("aptitudes.role.levelup",
					Text.translatable(role.translationKey()), level, newPoints).formatted(Formatting.LIGHT_PURPLE), false);
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.5f, 1.2f);
		}
		sync(player);
	}

	// ---------------------------------------------------------- Elección

	/**
	 * Asigna {@code role} como principal o secundario. Ocupar una ranura vacía o
	 * intercambiar principal y secundario es gratis; sustituir un rol ya elegido
	 * exige el ritual de la Hoguera (Ofrenda de renacimiento), que concede un cambio.
	 */
	public static ChangeResult setRole(ServerPlayerEntity player, Role role, boolean primary) {
		RoleData data = get(player);
		Role current = primary ? data.primary() : data.secondary();
		if (current == role) return ChangeResult.SAME;

		Role other = primary ? data.secondary() : data.primary();
		boolean replacing = current != null && other != role;
		boolean ritual = replacing && Aptitudes.config().roleChangeRequiresRitual && !player.isCreative();
		if (ritual && data.rebirths() <= 0) return ChangeResult.NEEDS_RITUAL;

		if (other == role) { // intercambio
			if (primary) { data.setSecondary(current); data.setPrimary(role); }
			else { data.setPrimary(current); data.setSecondary(role); }
		} else if (primary) {
			data.setPrimary(role);
		} else {
			data.setSecondary(role);
		}
		if (data.level(role) == 0) data.setLevel(role, 1); // permite aprender el primer nodo al instante
		if (replacing) data.setLastRoleChange(worldTime(player));
		if (ritual) data.setRebirths(data.rebirths() - 1);
		refresh(player);
		return ChangeResult.OK;
	}

	/**
	 * Reclamar de nuevo el arma de evolución (si se perdió). Validado en el servidor: el rol debe estar
	 * activo, haber alcanzado esa evolución y no tener ya el arma (inventario o cofre de Ender).
	 */
	public static void claimWeapon(ServerPlayerEntity player, Role role, int tier) {
		RoleData data = get(player);
		net.minecraft.item.Item weapon = dev.aptitudes.item.weapon.RoleWeapons.get(role, tier);
		if (weapon == null || !data.isActive(role) || data.tier(role) < tier) {
			player.sendMessage(Text.translatable("aptitudes.weapon.locked").formatted(Formatting.RED), true);
			return;
		}
		if (dev.aptitudes.item.weapon.RoleWeapons.owns(player, weapon)) {
			player.sendMessage(Text.translatable("aptitudes.weapon.already").formatted(Formatting.RED), true);
			return;
		}
		dev.aptitudes.item.weapon.RoleWeapons.give(player, role, tier);
	}

	/** Ritual completado en la Hoguera: concede un cambio de rol. */
	public static void grantRebirth(ServerPlayerEntity player) {
		RoleData data = get(player);
		data.setRebirths(data.rebirths() + 1);
		sync(player);
	}

	private static long worldTime(ServerPlayerEntity player) {
		// Tiempo total del mundo: avanza siempre y no lo altera /time set.
		return player.getServer() != null ? player.getServer().getOverworld().getTime() : 0L;
	}

	// ---------------------------------------------------------------- Árbol

	public static UpgradeResult upgrade(ServerPlayerEntity player, String nodeId) {
		RoleNode node = RoleTree.get(nodeId);
		if (node == null) return UpgradeResult.UNKNOWN_NODE;
		RoleData data = get(player);
		if (!data.isActive(node.role())) return UpgradeResult.ROLE_NOT_ACTIVE;
		int rank = data.rank(nodeId);
		if (rank >= node.maxRank()) return UpgradeResult.MAX_RANK;
		if (data.level(node.role()) < node.levelForRank(rank + 1)) return UpgradeResult.LEVEL_TOO_LOW;
		if (node.prerequisite() != null && data.rank(node.prerequisite()) <= 0) return UpgradeResult.MISSING_PREREQUISITE;
		if (availablePoints(data, node.role()) <= 0) return UpgradeResult.NO_POINTS;
		RoleTree.PathSpec path = RoleTree.pathSpec(nodeId);
		if (path != null && RoleTree.pathLocked(data::rank, node.role(), path.path())) {
			return UpgradeResult.PATH_LOCKED; // solo 2 de las 3 sendas
		}
		// Legendarias exclusivas (opcional): validación autoritativa en el servidor y registro del dueño.
		if (node.kind() == RoleNode.Kind.LEGENDARY && Aptitudes.config().legendaryExclusive) {
			UpgradeResult legend = dev.aptitudes.unique.UniqueManager.claimLegend(player, node.role());
			if (legend != UpgradeResult.OK) return legend;
		}
		data.setRank(nodeId, rank + 1);
		refresh(player);
		return UpgradeResult.OK;
	}

	/**
	 * Equipa una activa aprendida del rol en su ranura 1 o 2 ({@code which} 0/1).
	 * @return false si el nodo no es una activa del rol o no está aprendida.
	 */
	public static boolean equip(ServerPlayerEntity player, String nodeId, int which) {
		RoleNode node = RoleTree.get(nodeId);
		if (node == null || !node.active() || which < 0 || which > 1) return false;
		RoleData data = get(player);
		if (data.rank(nodeId) <= 0) return false;
		int index = RoleTree.activeIndex(node);
		if (index < 0) return false;
		data.equip(node.role(), which, index);
		player.sendMessage(Text.translatable("aptitudes.role.equipped", Text.translatable(node.translationKey()),
				which + 1).formatted(net.minecraft.util.Formatting.GOLD), true);
		refresh(player);
		return true;
	}

	// ------------------------------------------------------------- Elementos

	public enum ElementResult { OK, NOT_ELEMENTALIST, ALREADY_MASTERED, NO_SLOT }

	/** Domina un nuevo elemento si el nivel de Elementalista lo permite. La elección es permanente. */
	public static ElementResult chooseElement(ServerPlayerEntity player, Element element) {
		RoleData data = get(player);
		if (!data.isActive(Role.ELEMENTALIST)) return ElementResult.NOT_ELEMENTALIST;
		if (data.masters(element)) return ElementResult.ALREADY_MASTERED;
		if (data.elements().size() >= Element.allowedCount(data.level(Role.ELEMENTALIST))) return ElementResult.NO_SLOT;
		data.addElement(element);
		refresh(player);
		return ElementResult.OK;
	}

	/** Cambia al siguiente elemento dominado (tecla G). */
	public static void cycleElement(ServerPlayerEntity player) {
		RoleData data = get(player);
		if (!data.isActive(Role.ELEMENTALIST) || data.elements().size() < 2) return;
		data.cycleElement();
		dev.aptitudes.ability.RoleMechanics.onElementSwitch(player);
		Element active = data.activeElement();
		if (active != null) {
			player.sendMessage(Text.translatable("aptitudes.element.switched",
					Text.translatable(active.translationKey())).withColor(active.color()), true);
		}
		refresh(player); // los atributos pasivos dependen del elemento activo
	}

	public static void respecTree(ServerPlayerEntity player, Role role) {
		get(player).clearRanks(role);
		refresh(player);
	}

	// ---------------------------------------------------------------- Admin

	public static void setLevel(ServerPlayerEntity player, Role role, int level) {
		RoleData data = get(player);
		data.setLevel(role, Math.min(level, Aptitudes.config().maxRoleLevel));
		data.setXp(role, 0);
		refresh(player);
	}

	public static void reset(ServerPlayerEntity player) {
		player.setAttached(ModAttachments.ROLES, new RoleData());
		refresh(player);
	}

	public static void clearChangeCooldown(ServerPlayerEntity player) {
		get(player).setLastRoleChange(Long.MIN_VALUE);
		sync(player);
	}

	// ------------------------------------------------------ Coherencia/sync

	public static void refresh(ServerPlayerEntity player) {
		RoleData data = get(player);
		ensureConsistent(player, data);
		RolePassives.applyAttributes(player);
		// La afinidad de atributos depende del rol: se recalculan los atributos y la pantalla.
		dev.aptitudes.logic.AttributeApplier.apply(player, dev.aptitudes.logic.ProgressManager.get(player), Aptitudes.config());
		dev.aptitudes.logic.ProgressManager.sync(player);
		data.setEnergy(Math.min(data.energy(), AbilityManager.maxEnergy(player)));
		sync(player);
		AbilityManager.sendEnergy(player, true);
		RoleResources.send(player, true);
		// Legendarias: dependen de los nodos aprendidos y de los roles activos.
		dev.aptitudes.unique.UniqueManager.refreshAttributes(player);
		dev.aptitudes.accessory.Accessories.apply(player);
		dev.aptitudes.unique.UniqueManager.sync(player);
	}

	private static void ensureConsistent(ServerPlayerEntity player, RoleData data) {
		RoleRules rules = rules();
		for (Role role : Role.values()) {
			if (data.level(role) > rules.maxLevel()) data.setLevel(role, rules.maxLevel());
			// Si la configuración redujo los puntos, se devuelven los del árbol afectado.
			if (availablePoints(data, role) < 0
					|| (role == Role.ELEMENTALIST && data.elements().size() > Element.allowedCount(data.level(role)))) {
				data.clearRanks(role);
				player.sendMessage(Text.translatable("aptitudes.role.tree_reset",
						Text.translatable(role.translationKey())).formatted(Formatting.YELLOW), false);
			}
		}
	}

	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, RoleSyncPayload.ID)) return;
		RoleData data = get(player);
		ServerPlayNetworking.send(player, RoleSyncPayload.of(data, rules(),
				AbilityManager.slotCooldowns(player), data.rebirths()));
	}

	@Nullable
	public static Role roleInSlot(RoleData data, int slot) {
		return slot < 2 ? data.primary() : data.secondary();
	}
}
