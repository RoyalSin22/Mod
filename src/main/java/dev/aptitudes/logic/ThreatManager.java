package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.data.ThreatData;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.event.EclipseManager;
import dev.aptitudes.event.WorldEventManager;
import dev.aptitudes.network.ThreatPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Escalado por criatura; el cliente solo recibe el anillo personal calculado por el servidor. */
@SuppressWarnings("UnstableApiUsage")
public final class ThreatManager {
	private static final Identifier HEALTH = Aptitudes.id("threat/health");
	private static final Map<UUID, MobEntity> LOADED = new HashMap<>();
	private static final Map<UUID, Integer> PLAYER_RINGS = new HashMap<>();
	private static final Map<UUID, RegistryKey<LootTable>> DEATH_LOOT = new HashMap<>();

	private ThreatManager() {
	}

	public static void register() {
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			LOADED.remove(entity.getUuid());
			DEATH_LOOT.remove(entity.getUuid());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PLAYER_RINGS.remove(handler.getPlayer().getUuid()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> { LOADED.clear(); PLAYER_RINGS.clear(); DEATH_LOOT.clear(); });
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			// MobEntity vacía su tabla personalizada al soltar el botín vanilla, antes de AFTER_DEATH.
			// Capturarla no concede nada; si otro sistema evita la muerte, se sustituye en el siguiente intento.
			if (entity instanceof MobEntity mob && mob.getAttached(ModAttachments.THREAT) != null)
				DEATH_LOOT.put(mob.getUuid(), mob.getLootTable());
			return true;
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob) dropBonus(mob, source);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 != 0) return;
			// Retira el atributo al desactivar Amenaza o nombrar una criatura ya cargada, sin curarla.
			for (MobEntity mob : LOADED.values()) updateHealth(mob, mob.getAttached(ModAttachments.THREAT), false);
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				int current = Aptitudes.config().threatEnabled && player.isAlive() && !player.isCreative()
						&& !player.isSpectator() && player.getWorld().getRegistryKey() == World.OVERWORLD
						? ring(player.getServerWorld(), player.getX(), player.getZ()) : -1;
				Integer previous = PLAYER_RINGS.put(player.getUuid(), current);
				if ((previous == null || previous != current) && ServerPlayNetworking.canSend(player, ThreatPayload.ID)) {
					ServerPlayNetworking.send(player, new ThreatPayload(current));
				}
			}
		});
	}

	/** Se llama antes de la única tirada natural de élite, mientras el mob aún no tiene su nombre de élite. */
	public static void onLoad(MobEntity mob, ServerWorld world) {
		ThreatData data = mob.getAttached(ModAttachments.THREAT);
		boolean fresh = data == null && !mob.getCommandTags().contains(Elites.CHECKED);
		if (fresh) {
			AptitudesConfig c = Aptitudes.config();
			int level = c.threatEnabled && eligible(mob, false) ? ring(world, mob.getX(), mob.getZ()) : 0;
			data = new ThreatData(level, ThreatRules.healthMultiplier(level, c.threatHealthPerRing),
					ThreatRules.damageMultiplier(level, c.threatDamagePerRing, c.maxDamageMultiplier),
					ThreatRules.lootChance(level, c.threatLootPerRing), false);
			mob.setAttached(ModAttachments.THREAT, data);
		}
		if (data == null) return; // Los monstruos antiguos ya comprobados conservan su balance.
		LOADED.put(mob.getUuid(), mob);
		updateHealth(mob, data, fresh);
	}

	private static int ring(ServerWorld world, double x, double z) {
		var spawn = world.getSpawnPos();
		AptitudesConfig c = Aptitudes.config();
		return ThreatRules.ring(x, z, spawn.getX(), spawn.getZ(), c.threatRingSize, c.threatMaxRing);
	}

	private static boolean eligible(MobEntity mob, boolean ownEliteName) {
		boolean boss = mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON
				|| mob.getType() == EntityType.WARDEN || mob.getType() == EntityType.ELDER_GUARDIAN
				|| mob.getCommandTags().stream().anyMatch(tag -> BossKind.fromTag(tag) != null);
		boolean event = mob.getCommandTags().contains(EclipseManager.TAG) || mob.getCommandTags().contains(WorldEventManager.TAG);
		return ThreatRules.eligible(mob.getWorld().getRegistryKey() == World.OVERWORLD, mob instanceof Monster,
				boss, AbilityState.isSummon(mob) || AbilityState.isMinion(mob), event,
				mob.hasCustomName() && !(ownEliteName && Elites.isElite(mob)));
	}

	private static void updateHealth(MobEntity mob, ThreatData data, boolean fresh) {
		if (data == null || !mob.isAlive()) return;
		var attribute = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (attribute == null) return;
		double bonus = Aptitudes.config().threatEnabled && eligible(mob, true) ? data.health() - 1 : 0;
		var old = attribute.getModifier(HEALTH);
		if ((old == null && bonus == 0) || (old != null && old.value() == bonus
				&& old.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)) return;
		float fraction = mob.getHealth() / Math.max(1, mob.getMaxHealth());
		AttributeApplier.set(attribute, HEALTH, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		mob.setHealth(fresh ? fraction * mob.getMaxHealth() : Math.min(mob.getHealth(), mob.getMaxHealth()));
	}

	public static double eliteChance(MobEntity mob, double original) {
		ThreatData data = mob.getAttached(ModAttachments.THREAT);
		return data == null || !Aptitudes.config().threatEnabled || !eligible(mob, false) ? original
				: ThreatRules.eliteChance(original, data.ring(), Aptitudes.config().threatEliteChancePerRing);
	}

	/** Una sola multiplicación para golpes, proyectiles y explosiones; no añade daño de ataque por separado. */
	public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
		if (target.getWorld().isClient() || amount <= 0 || !Float.isFinite(amount) || !Aptitudes.config().threatEnabled
				|| !(source.getAttacker() instanceof MobEntity mob) || !eligible(mob, true)) return amount;
		ThreatData data = mob.getAttached(ModAttachments.THREAT);
		if (data == null || data.ring() == 0) return amount;
		double multiplier = data.damage();
		double cap = Aptitudes.config().maxDamageMultiplier;
		if (cap > 0 && Double.isFinite(cap)) multiplier = Math.min(multiplier, cap);
		return (float) Math.min(Float.MAX_VALUE, amount * multiplier);
	}

	private static void dropBonus(MobEntity mob, DamageSource source) {
		RegistryKey<LootTable> table = DEATH_LOOT.remove(mob.getUuid());
		ThreatData data = mob.getAttached(ModAttachments.THREAT);
		if (data == null || data.claimed()) return;
		// Se sella incluso una tirada fallida o una baja no válida: jamás puede volver a cobrarse.
		mob.setAttached(ModAttachments.THREAT, data.claim());
		if (!(mob.getWorld() instanceof ServerWorld world)) return;
		boolean valid = eligible(mob, true) && world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)
				&& source.getAttacker() instanceof ServerPlayerEntity player && !player.isCreative() && !player.isSpectator();
		if (!ThreatRules.bonusLoot(Aptitudes.config().threatEnabled, valid, data.claimed(), data.loot(), world.random.nextDouble())) return;
		ServerPlayerEntity killer = (ServerPlayerEntity) source.getAttacker();
		var context = new LootContextParameterSet.Builder(world)
				.add(LootContextParameters.THIS_ENTITY, mob).add(LootContextParameters.ORIGIN, mob.getPos())
				.add(LootContextParameters.DAMAGE_SOURCE, source).add(LootContextParameters.LAST_DAMAGE_PLAYER, killer)
				.addOptional(LootContextParameters.ATTACKING_ENTITY, source.getAttacker())
				.addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, source.getSource())
				.luck(killer.getLuck()).build(LootContextTypes.ENTITY);
		world.getServer().getReloadableRegistries().getLootTable(table == null ? mob.getLootTable() : table)
				.generateLoot(context, mob::dropStack);
	}
}
