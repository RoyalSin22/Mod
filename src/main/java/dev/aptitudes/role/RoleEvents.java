package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.ability.RolePassives;
import dev.aptitudes.item.GuildSealItem;
import dev.aptitudes.morph.MorphManager;
import dev.aptitudes.unique.UniqueEffects;
import dev.aptitudes.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** Fuentes de XP de rol y conexión de las pasivas e invocaciones con los eventos del servidor. */
public final class RoleEvents {
	/** Componentes técnicos cuya colocación da XP de Tecnólogo. */
	private static final Set<Item> TECH_ITEMS = Set.of(
			Items.REDSTONE, Items.REPEATER, Items.COMPARATOR, Items.PISTON, Items.STICKY_PISTON,
			Items.OBSERVER, Items.HOPPER, Items.DROPPER, Items.DISPENSER, Items.RAIL, Items.POWERED_RAIL,
			Items.DETECTOR_RAIL, Items.ACTIVATOR_RAIL, Items.REDSTONE_TORCH, Items.TARGET, Items.DAYLIGHT_DETECTOR,
			Items.CRAFTER, Items.LEVER, Items.TRIPWIRE_HOOK);

	/** Rendimiento decreciente de la XP de Ingeniero por colocar piezas. */
	private static final dev.aptitudes.logic.XpDiminishing PLACE_DIMINISHING = new dev.aptitudes.logic.XpDiminishing();

	private RoleEvents() {
	}

	/** Libera la memoria del jugador al desconectarse. */
	public static void forget(ServerPlayerEntity player) {
		PLACE_DIMINISHING.forget(player.getUuid());
	}

	public static void register() {
		// Daño: primero se decide si se cancela (parada/contraataque); luego XP al atacante.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayerEntity victim && source.getAttacker() != null
					&& dev.aptitudes.party.PartyManager.blocksFriendlyDamage(source.getAttacker(), victim)) return false;
			if (entity instanceof ServerPlayerEntity victim && (!RolePassives.allowIncomingDamage(victim, source, amount)
					|| !MorphManager.allowIncomingDamage(victim, source)
					|| !UniqueEffects.allowIncomingDamage(victim, source, amount)
					|| !dev.aptitudes.logic.SkillMilestones.allowIncomingDamage(victim, source)
					|| !dev.aptitudes.ability.RoleArmorSets.allowIncoming(victim, source)
					|| !dev.aptitudes.ability.RoleMechanics.allowIncoming(victim, source))) {
				return false;
			}
			if (amount <= 0 || entity instanceof ArmorStandEntity) return true;
			if (entity instanceof ServerPlayerEntity victim && source.getAttacker() instanceof net.minecraft.entity.LivingEntity) {
				RoleResources.onHurt(victim, amount);
				dev.aptitudes.ability.CastFx.onHeavyHit(victim, amount);
			}
			if (source.getAttacker() instanceof ServerPlayerEntity hitter && entity != hitter) {
				dev.aptitudes.ability.CastFx.damageNumber(hitter, entity, amount);
				dev.aptitudes.ability.CastFx.impact(hitter, entity, source, amount);
				dev.aptitudes.logic.SkillMilestones.onOutgoing(hitter, entity, source);
			}
			int xp = (int) Math.max(1, Math.round(amount * Aptitudes.config().roleXpPerDamage));

			// Paladín: bloquear golpes con el escudo.
			if (entity instanceof ServerPlayerEntity victim && victim.isBlocking() && source.getAttacker() != null) {
				RoleManager.addRoleXp(victim, Role.PALADIN, xp);
			}
			// Domador: el daño de tus mascotas te da XP.
			if (source.getAttacker() instanceof net.minecraft.entity.passive.TameableEntity pet
					&& pet.getOwner() instanceof ServerPlayerEntity owner && !AbilityState.isSummon(pet)) {
				RoleManager.addRoleXp(owner, Role.TAMER, xp);
			}
			// Nigromante: el daño de tus esbirros te da XP.
			if (source.getAttacker() != null && AbilityState.isMinion(source.getAttacker())) {
				ServerPlayerEntity owner = AbilityState.ownerOf(source.getAttacker());
				if (owner != null) RoleManager.addRoleXp(owner, Role.NECROMANCER, xp);
			}
			if (source.getAttacker() instanceof ServerPlayerEntity attacker && entity != attacker) {
				Role role = combatRole(attacker, source.getSource());
				if (role != null) dev.aptitudes.party.PartyManager.awardCombatRoleXp(attacker, role, xp);
				// Paladín: cualquier daño a no-muertos.
				if (role != Role.PALADIN && entity.getType().isIn(EntityTypeTags.UNDEAD)) {
					dev.aptitudes.party.PartyManager.awardCombatRoleXp(attacker, Role.PALADIN, xp);
				}
			}
			return true;
		});

		// Muertes: Comunión oscura, pruebas de evolución y jefes.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			RolePassives.onNearbyDeath(entity);
			if (source.getAttacker() instanceof ServerPlayerEntity killer && entity != killer
					&& !AbilityState.isSummon(entity)) {
				MorphManager.onKill(killer, entity.getType());
				dev.aptitudes.logic.SkillMilestones.onKill(killer, entity);
				dev.aptitudes.logic.SoulTools.onKill(killer, entity);
				dev.aptitudes.ability.RoleMechanics.onKill(killer, entity, source);
				UniqueEffects.onKill(killer, entity, source);
				Role role = combatRole(killer, source.getSource());
				if (role != null) RoleManager.recordTrial(killer, role, Evolution.TrialType.WEAPON_KILLS, 1);
				if (entity.getType().isIn(EntityTypeTags.UNDEAD)) {
					RoleManager.recordTrial(killer, Role.PALADIN, Evolution.TrialType.UNDEAD_KILLS, 1);
				}
			}
			// Jefes: cuentan para todos los jugadores que participaron (a menos de 96 bloques).
			if (entity.getType() == EntityType.WITHER || entity.getType() == EntityType.ENDER_DRAGON
					|| entity.getType() == EntityType.WARDEN) {
				for (ServerPlayerEntity player : entity.getWorld().getEntitiesByClass(ServerPlayerEntity.class,
						entity.getBoundingBox().expand(96), ServerPlayerEntity::isAlive)) {
					RoleManager.markBossSlain(player);
				}
			}
		});

		// Jugadores que no deben morir: Ira imparable / Reino eterno / Lanza celestial e Inmortalidad carmesí.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayerEntity player)) return true;
			if (AbilityState.isUndying(player)) {
				player.setHealth(1.0f);
				return false;
			}
			return !RolePassives.tryCrimsonImmortality(player) && !RolePassives.tryForestRebirth(player)
					&& !dev.aptitudes.logic.SkillMilestones.trySecondChance(player)
					&& !dev.aptitudes.accessory.Accessories.tryPhoenix(player);
		});

		// Las invocaciones mueren sin soltar botín ni XP (evita granjas de hierro o huesos).
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (AbilityState.isSummon(entity)) {
				dev.aptitudes.ability.RoleMechanics.onSummonDeath(entity);
				entity.discard();
				return false;
			}
			return true;
		});

		// Invocaciones guardadas en disco de una sesión anterior: se retiran.
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (AbilityState.isSummon(entity) && !AbilityState.isTracked(entity)) {
				AbilityState.scheduleDiscard(entity);
			}
		});

		// Herbolista: cosechar cultivos maduros, flores y bayas.
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!(player instanceof ServerPlayerEntity sp)) return;
			boolean herb = (state.getBlock() instanceof CropBlock crop && crop.isMature(state))
					|| state.isIn(BlockTags.FLOWERS)
					|| (state.getBlock() instanceof SweetBerryBushBlock && state.get(SweetBerryBushBlock.AGE) >= 2);
			if (herb) {
				RoleManager.addRoleXp(sp, Role.HERBALIST, Aptitudes.config().roleXpPerAction);
				RoleManager.recordTrial(sp, Role.HERBALIST, Evolution.TrialType.ROLE_ACTIONS, 1);
			}
		});

		// Ingeniero: colocar redstone y mecanismos. Druida: plantar retoños. v17: solo colocaciones
		// confirmadas (PlacementTracker), y el Ingeniero con rendimiento decreciente (colocar y romper
		// la misma pieza en bucle deja de dar XP).
		dev.aptitudes.logic.PlacementTracker.addListener((sp, item, pos) -> {
			if (TECH_ITEMS.contains(item)) {
				int xp = Aptitudes.config().xpDiminishingEnabled
						? PLACE_DIMINISHING.scale(sp.getUuid(), "engineer/place", 1, sp.getServer().getTicks(),
						Aptitudes.config().xpDiminishingWindowSeconds * 20L, Aptitudes.config().xpDiminishingThreshold,
						Aptitudes.config().xpDiminishingFloor)
						: 1;
				if (xp > 0) RoleManager.addRoleXp(sp, Role.ENGINEER, xp);
			}
			if (item.getRegistryEntry().isIn(ItemTags.SAPLINGS)) {
				RoleManager.addRoleXp(sp, Role.DRUID, Aptitudes.config().roleXpPerAction);
				RoleManager.recordTrial(sp, Role.DRUID, Evolution.TrialType.ROLE_ACTIONS, 1);
			}
		});

		// Mercader: el Sello del gremio repone las ofertas (antes de que se abra el comercio).
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(ModItems.GUILD_SEAL) || !(entity instanceof VillagerEntity villager)) return ActionResult.PASS;
			if (world.isClient()) return ActionResult.SUCCESS; // evita que el cliente abra la pantalla de comercio
			return player instanceof ServerPlayerEntity sp ? GuildSealItem.applyTo(sp, villager, stack) : ActionResult.PASS;
		});

		// Druida: alimentar animales para criarlos.
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient() && player instanceof ServerPlayerEntity sp && entity instanceof AnimalEntity animal
					&& animal.isBreedingItem(player.getStackInHand(hand)) && animal.canEat()) {
				RoleManager.addRoleXp(sp, Role.DRUID, Aptitudes.config().roleXpPerAction);
				RoleManager.recordTrial(sp, Role.DRUID, Evolution.TrialType.ROLE_ACTIONS, 1);
			}
			return ActionResult.PASS;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int ticks = server.getTicks();
			if (ticks % 10 == 0) AbilityManager.tickEnergy(server);
			dev.aptitudes.ability.BerserkerKit.tick(server); // v67: Avatar, Ragnarök, sangrados y fisuras
			if (ticks % 20 == 0) {
				RolePassives.tick(server);
				MorphManager.tick(server);
				RoleResources.tick(server);
				dev.aptitudes.ability.PathEffects.tick(server);
				UniqueEffects.tick(server);
				dev.aptitudes.logic.SkillMilestones.tick(server);
				dev.aptitudes.ability.RoleArmorSets.tick(server);
				dev.aptitudes.ability.RoleMechanics.tick(server);
				dev.aptitudes.accessory.Accessories.tick(server);
			}
			if (ticks % 100 == 0) dev.aptitudes.unique.UniqueManager.checkAll(server);
			AbilityState.tick(server);
		});
	}

	/** Rol que gana XP según el arma con que se golpea (o el proyectil). */
	@Nullable
	private static Role combatRole(ServerPlayerEntity attacker, @Nullable Entity direct) {
		if (direct instanceof TridentEntity) return Role.LANCER;
		// Flechas (arco o ballesta): Cazador.
		if (direct instanceof net.minecraft.entity.projectile.PersistentProjectileEntity) return Role.HUNTER;
		if (direct != attacker) return null;
		ItemStack stack = attacker.getMainHandStack();
		if (stack.isEmpty()) return Role.MONK;
		if (stack.isOf(ModItems.HOLY_HAMMER)) return Role.PALADIN;
		if (stack.isOf(ModItems.DAGGER)) return Role.ASSASSIN;
		if (stack.isOf(ModItems.HUNTING_KNIFE)) return Role.HUNTER;
		if (stack.isOf(ModItems.SPEAR) || stack.isOf(Items.TRIDENT)) return Role.LANCER;
		if (stack.getItem() instanceof AxeItem) return Role.BERSERKER;
		if (stack.getItem() instanceof SwordItem) return Role.SWORDSMAN;
		return null;
	}
}
