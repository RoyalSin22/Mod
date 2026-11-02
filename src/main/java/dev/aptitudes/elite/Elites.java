package dev.aptitudes.elite;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Monstruos élite y campeones.
 * <ul>
 *   <li><b>Élite</b> (≈5 % de los monstruos): 1-2 modificadores, ×2 de vida, nombre visible.
 *       Suelta Fragmentos de alma, Esencia de élite (50 %) y más XP.</li>
 *   <li><b>Campeón</b> (≈0,5 %, y siempre uno en la azotea de las torres): 3 modificadores,
 *       ×4 de vida, barra de jefe para los jugadores cercanos. Suelta un Corazón de campeón,
 *       Esencias y una pieza de armadura de rol.</li>
 * </ul>
 * Los datos van en etiquetas de comando del mob, así sobreviven al guardar el mundo.
 */
public final class Elites {
	/** Modificadores de élite. */
	public enum Mod {
		FAST, ARMORED, VAMPIRIC, BRUTAL, REGENERATING, EXPLOSIVE, FROST, BLINKING, GIANT;

		public String tag() {
			return Aptitudes.MOD_ID + ".elite." + name().toLowerCase(Locale.ROOT);
		}

		public String translationKey() {
			return "elite." + Aptitudes.MOD_ID + "." + name().toLowerCase(Locale.ROOT);
		}
	}

	public static final String CHECKED = Aptitudes.MOD_ID + ".elite_checked";
	public static final String ELITE = Aptitudes.MOD_ID + ".elite";
	public static final String CHAMPION = Aptitudes.MOD_ID + ".champion";

	private static final Map<UUID, ServerBossBar> BARS = new HashMap<>();
	private static final Map<UUID, Integer> BLINK_READY = new HashMap<>();

	private Elites() {
	}

	public static void register() {
		// Al aparecer por primera vez: tirada de élite / campeón.
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(entity instanceof MobEntity mob) || !(entity instanceof Monster)) return;
			if (mob.getCommandTags().contains(CHAMPION)) track(mob);
			if (mob.getCommandTags().contains(CHECKED)) return;
			mob.addCommandTag(CHECKED);
			double chance = Aptitudes.config().eliteChance;
			float roll = world.random.nextFloat();
			if (roll < chance * 0.1) makeElite(mob, true, world.random);
			else if (roll < chance) makeElite(mob, false, world.random);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob && isElite(mob)) onDeath(mob, source);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) tick(server);
		});
	}

	public static boolean isElite(Entity e) {
		return e.getCommandTags().contains(ELITE);
	}

	public static boolean isChampion(Entity e) {
		return e.getCommandTags().contains(CHAMPION);
	}

	public static boolean has(Entity e, Mod mod) {
		return e.getCommandTags().contains(mod.tag());
	}

	/** Actualiza el título de la barra compartida cuando un campeón tiene nombre propio. */
	public static void setChampionBarName(Entity entity, Text name) {
		ServerBossBar bar = BARS.get(entity.getUuid());
		if (bar != null) bar.setName(name);
	}

	/** Convierte un monstruo en élite (o campeón). Se puede llamar durante la generación del mundo. */
	public static void makeElite(MobEntity mob, boolean champion, Random random) {
		mob.addCommandTag(CHECKED);
		mob.addCommandTag(ELITE);
		if (champion) mob.addCommandTag(CHAMPION);
		List<Mod> pool = new ArrayList<>(List.of(Mod.values()));
		int count = champion ? 3 : 1 + random.nextInt(2);
		List<Mod> chosen = new ArrayList<>();
		for (int i = 0; i < count && !pool.isEmpty(); i++) chosen.add(pool.remove(random.nextInt(pool.size())));
		for (Mod m : chosen) mob.addCommandTag(m.tag());

		modifier(mob, EntityAttributes.GENERIC_MAX_HEALTH, "health", champion ? 3.0 : 1.0,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		modifier(mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, "damage", champion ? 0.5 : 0.2,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		if (chosen.contains(Mod.FAST)) modifier(mob, EntityAttributes.GENERIC_MOVEMENT_SPEED, "fast", 0.3,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		if (chosen.contains(Mod.ARMORED)) {
			modifier(mob, EntityAttributes.GENERIC_ARMOR, "armored", 8.0, EntityAttributeModifier.Operation.ADD_VALUE);
			modifier(mob, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "armored_kb", 0.5, EntityAttributeModifier.Operation.ADD_VALUE);
		}
		if (chosen.contains(Mod.BRUTAL)) modifier(mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, "brutal", 0.5,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		if (chosen.contains(Mod.GIANT)) {
			modifier(mob, EntityAttributes.GENERIC_SCALE, "giant", 0.35, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			modifier(mob, EntityAttributes.GENERIC_MAX_HEALTH, "giant_health", 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		}
		mob.setHealth(mob.getMaxHealth());
		mob.setPersistent();

		MutableText name = Text.empty();
		for (Mod m : chosen) name.append(Text.translatable(m.translationKey())).append(" ");
		name.append(mob.getType().getName());
		mob.setCustomName(Text.translatable(champion ? "elite.aptitudes.champion_name" : "elite.aptitudes.elite_name", name)
				.formatted(champion ? Formatting.GOLD : Formatting.LIGHT_PURPLE));
		mob.setCustomNameVisible(true);
		if (champion && mob.getWorld() instanceof ServerWorld) track(mob);
	}

	private static void modifier(MobEntity mob, RegistryEntry<EntityAttribute> attribute, String id, double value,
								 EntityAttributeModifier.Operation op) {
		EntityAttributeInstance instance = mob.getAttributeInstance(attribute);
		if (instance == null) return;
		instance.removeModifier(Aptitudes.id("elite/" + id));
		instance.addPersistentModifier(new EntityAttributeModifier(Aptitudes.id("elite/" + id), value, op));
	}

	// ------------------------------------------------------------- Daño

	/** Daño hecho o recibido por élites (desde el mixin de {@code LivingEntity#damage}). */
	public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
		if (amount <= 0 || target.getWorld().isClient()) return amount;
		Entity attacker = source.getAttacker();
		if (attacker instanceof MobEntity elite && isElite(elite)) {
			if (has(elite, Mod.VAMPIRIC)) elite.heal(amount * 0.3f);
			if (has(elite, Mod.FROST)) {
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
				target.setFrozenTicks(Math.max(target.getFrozenTicks(), target.getMinFreezeDamageTicks() + 20));
			}
		}
		if (target instanceof MobEntity elite && isElite(elite) && has(elite, Mod.BLINKING)
				&& elite.getWorld() instanceof ServerWorld world) {
			int now = world.getServer().getTicks();
			if (BLINK_READY.getOrDefault(elite.getUuid(), 0) <= now) {
				BLINK_READY.put(elite.getUuid(), now + 60);
				Vec3d pos = elite.getPos().add((world.random.nextDouble() - 0.5) * 8, 0, (world.random.nextDouble() - 0.5) * 8);
				world.spawnParticles(ParticleTypes.PORTAL, elite.getX(), elite.getBodyY(0.5), elite.getZ(), 20, 0.3, 0.5, 0.3, 0.2);
				elite.teleport(pos.x, world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, (int) pos.x, (int) pos.z), pos.z, true);
				world.playSound(null, elite.getX(), elite.getY(), elite.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
						SoundCategory.HOSTILE, 0.8f, 1.2f);
			}
		}
		return amount;
	}

	// ------------------------------------------------------------- Muerte

	private static void onDeath(MobEntity mob, DamageSource source) {
		World world = mob.getWorld();
		if (!(world instanceof ServerWorld server)) return;
		boolean champion = isChampion(mob);
		Random random = world.random;
		mob.dropStack(new ItemStack(ModItems.SOUL_SHARD, champion ? 4 + random.nextInt(3) : 1 + random.nextInt(2)));
		if (champion) {
			mob.dropStack(new ItemStack(ModItems.CHAMPION_HEART));
			mob.dropStack(new ItemStack(ModItems.ELITE_ESSENCE, 2 + random.nextInt(2)));
			List<net.minecraft.item.Item> armor = dev.aptitudes.item.RoleArmor.all();
			if (!armor.isEmpty()) mob.dropStack(new ItemStack(armor.get(random.nextInt(armor.size()))));
		} else if (random.nextFloat() < 0.5f) {
			mob.dropStack(new ItemStack(ModItems.ELITE_ESSENCE));
		}
		ExperienceOrbEntity.spawn(server, mob.getPos(), champion ? 120 : 25);
		if (has(mob, Mod.EXPLOSIVE)) {
			world.createExplosion(mob, mob.getX(), mob.getY(), mob.getZ(), 2.5f, World.ExplosionSourceType.NONE);
		}
		ServerBossBar bar = BARS.remove(mob.getUuid());
		if (bar != null) bar.clearPlayers();
		if (champion && source.getAttacker() instanceof ServerPlayerEntity killer) {
			server.getServer().getPlayerManager().broadcast(Text.translatable("elite.aptitudes.champion_slain",
					killer.getDisplayName(), mob.getDisplayName()).formatted(Formatting.GOLD), false);
		}
	}

	// ------------------------------------------------------------- Tick

	private static void track(MobEntity mob) {
		BARS.computeIfAbsent(mob.getUuid(), k -> {
			ServerBossBar bar = new ServerBossBar(mob.getDisplayName(), BossBar.Color.YELLOW, BossBar.Style.NOTCHED_10);
			bar.setDarkenSky(false);
			return bar;
		});
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, ServerBossBar>> it = BARS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, ServerBossBar> e = it.next();
			Entity entity = null;
			for (ServerWorld world : server.getWorlds()) {
				entity = world.getEntity(e.getKey());
				if (entity != null) break;
			}
			ServerBossBar bar = e.getValue();
			if (!(entity instanceof MobEntity mob) || !mob.isAlive()) {
				bar.clearPlayers();
				it.remove();
				continue;
			}
			bar.setPercent(mob.getHealth() / mob.getMaxHealth());
			for (ServerPlayerEntity p : List.copyOf(bar.getPlayers())) {
				if (p.isRemoved() || p.getWorld() != mob.getWorld() || p.squaredDistanceTo(mob) > 48 * 48) bar.removePlayer(p);
			}
			for (ServerPlayerEntity p : ((ServerWorld) mob.getWorld()).getPlayers(pl -> pl.squaredDistanceTo(mob) <= 32 * 32)) {
				bar.addPlayer(p);
			}
		}
		// Regeneración de las élites que la tienen
		java.util.Set<UUID> healed = new java.util.HashSet<>();
		for (ServerWorld world : server.getWorlds()) {
			for (ServerPlayerEntity p : world.getPlayers()) {
				for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(32),
						m -> m.isAlive() && has(m, Mod.REGENERATING))) {
					if (!healed.add(mob.getUuid())) continue;
					if (mob.getHealth() < mob.getMaxHealth()) mob.heal(1.0f);
					world.spawnParticles(ParticleTypes.HEART, mob.getX(), mob.getBodyY(1.0), mob.getZ(), 1, 0.2, 0.2, 0.2, 0);
				}
			}
		}
	}
}
