package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.network.KeystonePayload;
import dev.aptitudes.role.KeystoneRules;
import dev.aptitudes.role.KeystoneRules.Template;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.sound.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Efectos de las piedras angulares (v46.1): el escalón S7 de la senda elegida en cada rol activo (principal y
 * secundario). Reglas y números en {@link KeystoneRules} y en la configuración ({@code keystone*}).
 *
 * <p>Los estados (esperas, contador de Sobrecarga, ventana de Espectro) viven en memoria, como las esperas de las
 * runas: tras reiniciar el servidor empiezan listos.</p>
 */
public final class Keystones {
	private static final Map<UUID, Integer> OVERLOAD = new HashMap<>();
	private static final Map<UUID, Long> BULWARK_READY = new HashMap<>();
	private static final Map<UUID, Long> SPECTER_UNTIL = new HashMap<>();
	private static final Map<UUID, Long> TEMPEST_READY = new HashMap<>();
	private static final Map<UUID, Long> SANCTUARY_NEXT = new HashMap<>();
	private static final Map<UUID, List<KeystonePayload.Entry>> LAST_SENT = new HashMap<>();
	private static final double DETONATION_RADIUS = 3.0;
	private static final double CONTAGION_RADIUS = 4.0;
	private static final double TEMPEST_RADIUS = 5.0;
	private static final double SANCTUARY_RADIUS = 8.0;
	/** Evita cadenas: una Detonación o una chispa no dispara otra en el mismo golpe. */
	private static boolean inEffect;

	private Keystones() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity.getWorld().isClient() || !(source.getAttacker() instanceof ServerPlayerEntity player)) return;
			onKill(player, entity, source);
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (entity.getWorld().isClient() || damageTaken <= 0) return;
			if (entity instanceof ServerPlayerEntity player) onHurt(player);
			// Legión: los esbirros curan a su dueño con parte del daño que hacen.
			Entity attacker = source.getAttacker();
			if (attacker != null && AbilityState.isMinion(attacker)) {
				ServerPlayerEntity owner = AbilityState.ownerOf(attacker);
				if (owner != null && owner.isAlive() && has(owner, Template.LEGION)) {
					owner.heal(KeystoneRules.legionHeal(damageTaken, Aptitudes.config().keystoneLegionLifesteal));
				}
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(Keystones::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> forget(handler.getPlayer().getUuid()));
	}

	private static void forget(UUID id) {
		OVERLOAD.remove(id);
		BULWARK_READY.remove(id);
		SPECTER_UNTIL.remove(id);
		TEMPEST_READY.remove(id);
		SANCTUARY_NEXT.remove(id);
		LAST_SENT.remove(id);
	}

	private static long now(ServerPlayerEntity p) {
		return p.getServerWorld().getTime();
	}

	// ------------------------------------------------------------ Qué piedras angulares tiene

	/** Plantillas activas: la piedra angular aprendida de la senda elegida en el rol principal y en el secundario. */
	public static Set<Template> active(ServerPlayerEntity player) {
		Set<Template> out = EnumSet.noneOf(Template.class);
		RoleData data = RoleManager.get(player);
		for (Role role : new Role[] {data.primary(), data.secondary()}) {
			if (role == null) continue;
			int path = PathRules.chosenPath(data::rank, role.key());
			if (path <= 0 || data.rank(PathRules.nodeId(role.key(), path, PathRules.Step.S7)) <= 0) continue;
			Template t = KeystoneRules.template(role.key(), path);
			if (t != null) out.add(t);
		}
		return out;
	}

	public static boolean has(ServerPlayerEntity player, Template template) {
		return active(player).contains(template);
	}

	private static void sound(ServerPlayerEntity p, Template t, float volume) {
		p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.KEYSTONE[t.ordinal()],
				SoundCategory.PLAYERS, volume, 1.0f);
	}

	// ------------------------------------------------------------ Sobrecarga (AbilityManager.execute)

	/** ¿La próxima habilidad es la gratuita? (se consulta antes de cobrar el maná). */
	public static boolean overloadReady(ServerPlayerEntity player) {
		return has(player, Template.OVERLOAD)
				&& KeystoneRules.overloadReady(OVERLOAD.getOrDefault(player.getUuid(), 0), Aptitudes.config().keystoneOverloadEvery);
	}

	/** Tras lanzar una habilidad con éxito: avanza el contador. {@code free}: era la gratuita. */
	public static void afterCast(ServerPlayerEntity player, boolean free) {
		if (!has(player, Template.OVERLOAD)) return;
		int every = Aptitudes.config().keystoneOverloadEvery;
		OVERLOAD.put(player.getUuid(), KeystoneRules.overloadAfterCast(OVERLOAD.getOrDefault(player.getUuid(), 0), every));
		if (free) {
			sound(player, Template.OVERLOAD, 0.8f);
			player.getServerWorld().spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getBodyY(0.6),
					player.getZ(), 16, 0.4, 0.5, 0.4, 0.05);
		}
	}

	// ------------------------------------------------------------ Espectro y Tempestad (PathEffects)

	/** El jugador acaba de esquivar: abre la ventana de crítico seguro. */
	public static void onDodge(ServerPlayerEntity player) {
		if (!has(player, Template.SPECTER)) return;
		SPECTER_UNTIL.put(player.getUuid(), KeystoneRules.readyAt(now(player), Aptitudes.config().keystoneSpecterSeconds));
		sound(player, Template.SPECTER, 0.6f);
	}

	/** ¿Este golpe es crítico seguro por Espectro? Consume la ventana. */
	public static boolean consumeSpecter(ServerPlayerEntity player) {
		Long until = SPECTER_UNTIL.get(player.getUuid());
		if (until == null) return false;
		SPECTER_UNTIL.remove(player.getUuid());
		return now(player) <= until && has(player, Template.SPECTER);
	}

	/** Esquiva propia del Espectro (para que funcione en sendas sin esquiva). */
	public static double specterDodge(ServerPlayerEntity player) {
		return has(player, Template.SPECTER) ? Math.max(0.0, Aptitudes.config().keystoneSpecterDodge) : 0.0;
	}

	/**
	 * Tras un golpe directo: chispa a otro enemigo cercano (Tempestad) si fue crítico o, si no, con
	 * {@code keystoneTempestChance} (así funciona también en sendas sin crítico).
	 */
	public static void onStrike(ServerPlayerEntity player, LivingEntity target, boolean crit) {
		if (inEffect || !has(player, Template.TEMPEST)) return;
		AptitudesConfig cfg = Aptitudes.config();
		if (!crit && player.getRandom().nextDouble() >= cfg.keystoneTempestChance) return;
		long now = now(player);
		if (now < TEMPEST_READY.getOrDefault(player.getUuid(), 0L)) return;
		LivingEntity next = nearestHostile(player, target, TEMPEST_RADIUS);
		if (next == null) return;
		TEMPEST_READY.put(player.getUuid(), KeystoneRules.readyAt(now, cfg.keystoneTempestCooldownSeconds));
		ServerWorld world = player.getServerWorld();
		Targeting.particleLine(world, ParticleTypes.ELECTRIC_SPARK, target.getPos().add(0, target.getHeight() * 0.6, 0),
				next.getPos().add(0, next.getHeight() * 0.6, 0));
		damage(player, next, (float) cfg.keystoneTempestDamage);
		sound(player, Template.TEMPEST, 0.7f);
	}

	// ------------------------------------------------------------ Esbirros (Minions.spawn)

	public static int maxMinions(ServerPlayerEntity owner, int base) {
		return KeystoneRules.maxMinions(base, has(owner, Template.LEGION));
	}

	// ------------------------------------------------------------ Muertes

	private static void onKill(ServerPlayerEntity player, LivingEntity victim, DamageSource source) {
		Set<Template> active = active(player);
		if (active.isEmpty() || victim == player || AbilityState.isSummon(victim)) return;
		AptitudesConfig cfg = Aptitudes.config();
		ServerWorld world = player.getServerWorld();
		if (active.contains(Template.CARNAGE)) {
			player.heal(KeystoneRules.carnageHeal(player.getMaxHealth(), cfg.keystoneCarnageHeal));
			int ticks = (int) Math.round(cfg.keystoneCarnageHasteSeconds * 20);
			if (ticks > 0) player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, ticks, 1, false, true, true));
			world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getBodyY(0.6), player.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
			sound(player, Template.CARNAGE, 0.7f);
		}
		if (active.contains(Template.DETONATION) && !inEffect) {
			inEffect = true;
			try {
				world.spawnParticles(ParticleTypes.EXPLOSION, victim.getX(), victim.getBodyY(0.5), victim.getZ(), 1, 0, 0, 0, 0);
				for (LivingEntity e : hostilesNear(player, victim.getPos(), DETONATION_RADIUS, victim)) {
					damage(player, e, (float) cfg.keystoneDetonationDamage);
				}
			} finally {
				inEffect = false;
			}
			sound(player, Template.DETONATION, 0.9f);
		}
		if (active.contains(Template.CONTAGION)) contagion(player, victim, cfg.keystoneContagionTargets);
		if (active.contains(Template.FORTUNE) && victim instanceof MobEntity mob) fortune(player, mob, source, cfg);
	}

	/** Los efectos al golpear del objetivo (veneno, lentitud, marchitar y fuego) pasan a enemigos cercanos. */
	private static void contagion(ServerPlayerEntity player, LivingEntity victim, int targets) {
		List<StatusEffectInstance> carried = new ArrayList<>();
		for (var effect : List.of(StatusEffects.POISON, StatusEffects.SLOWNESS, StatusEffects.WITHER)) {
			StatusEffectInstance inst = victim.getStatusEffect(effect);
			if (inst != null && inst.getDuration() > 0) carried.add(inst);
		}
		boolean fire = victim.isOnFire();
		if ((carried.isEmpty() && !fire) || targets <= 0) return;
		List<LivingEntity> near = hostilesNear(player, victim.getPos(), CONTAGION_RADIUS, victim);
		near.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(victim)));
		ServerWorld world = player.getServerWorld();
		for (LivingEntity e : near.subList(0, Math.min(targets, near.size()))) {
			for (StatusEffectInstance inst : carried) {
				e.addStatusEffect(new StatusEffectInstance(inst.getEffectType(), inst.getDuration(), inst.getAmplifier()), player);
			}
			if (fire) e.setOnFireFor(3);
			Targeting.particleLine(world, ParticleTypes.SQUID_INK, victim.getPos().add(0, 0.8, 0), e.getPos().add(0, 0.8, 0));
		}
		sound(player, Template.CONTAGION, 0.6f);
	}

	/** Una tirada extra de la tabla de botín de la criatura y, en élites, una esmeralda. */
	private static void fortune(ServerPlayerEntity player, MobEntity mob, DamageSource source, AptitudesConfig cfg) {
		ServerWorld world = player.getServerWorld();
		boolean rolled = false;
		if (player.getRandom().nextDouble() < cfg.keystoneFortuneChance) {
			var key = mob.getLootTable();
			if (key != null) {
				var table = world.getServer().getReloadableRegistries().getLootTable(key);
				var params = new net.minecraft.loot.context.LootContextParameterSet.Builder(world)
						.add(net.minecraft.loot.context.LootContextParameters.THIS_ENTITY, mob)
						.add(net.minecraft.loot.context.LootContextParameters.ORIGIN, mob.getPos())
						.add(net.minecraft.loot.context.LootContextParameters.DAMAGE_SOURCE, source)
						.addOptional(net.minecraft.loot.context.LootContextParameters.ATTACKING_ENTITY, player)
						.addOptional(net.minecraft.loot.context.LootContextParameters.DIRECT_ATTACKING_ENTITY, source.getSource())
						.add(net.minecraft.loot.context.LootContextParameters.LAST_DAMAGE_PLAYER, player)
						.luck(player.getLuck())
						.build(net.minecraft.loot.context.LootContextTypes.ENTITY);
				table.generateLoot(params, mob::dropStack);
				rolled = true;
			}
		}
		if (dev.aptitudes.elite.Elites.isElite(mob) && player.getRandom().nextDouble() < cfg.keystoneFortuneEliteEmerald) {
			mob.dropStack(new ItemStack(Items.EMERALD));
			rolled = true;
		}
		if (rolled) {
			world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 10, 0.4, 0.4, 0.4, 0.02);
			sound(player, Template.FORTUNE, 0.6f);
		}
	}

	// ------------------------------------------------------------ Baluarte

	private static void onHurt(ServerPlayerEntity player) {
		if (!player.isAlive() || !has(player, Template.BULWARK)) return;
		AptitudesConfig cfg = Aptitudes.config();
		long now = now(player);
		if (!KeystoneRules.bulwarkTriggers(player.getHealth(), player.getMaxHealth(), cfg.keystoneBulwarkThreshold, now,
				BULWARK_READY.getOrDefault(player.getUuid(), 0L))) return;
		BULWARK_READY.put(player.getUuid(), KeystoneRules.readyAt(now, cfg.keystoneBulwarkCooldownSeconds));
		int ticks = (int) Math.round(cfg.keystoneBulwarkSeconds * 20);
		int amp = KeystoneRules.absorptionAmplifier(cfg.keystoneBulwarkAbsorption);
		if (ticks > 0) {
			if (amp >= 0) player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, ticks, amp, false, true, true));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, ticks, 0, false, true, true));
		}
		player.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getBodyY(0.5),
				player.getZ(), 24, 0.4, 0.6, 0.4, 0.15);
		sound(player, Template.BULWARK, 1.0f);
	}

	// ------------------------------------------------------------ Santuario y HUD (cada 10 ticks)

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % 10 != 0) return;
		AptitudesConfig cfg = Aptitudes.config();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			Set<Template> active = active(player);
			if (active.contains(Template.SANCTUARY) && player.isAlive()) sanctuary(player, cfg);
			sync(player, active, cfg);
		}
	}

	private static void sanctuary(ServerPlayerEntity player, AptitudesConfig cfg) {
		long now = now(player);
		Long next = SANCTUARY_NEXT.get(player.getUuid());
		if (next == null) {
			SANCTUARY_NEXT.put(player.getUuid(), KeystoneRules.readyAt(now, cfg.keystoneSanctuaryIntervalSeconds));
			return;
		}
		if (now < next) return;
		SANCTUARY_NEXT.put(player.getUuid(), KeystoneRules.readyAt(now, cfg.keystoneSanctuaryIntervalSeconds));
		float heal = (float) cfg.keystoneSanctuaryHeal;
		if (heal <= 0) return;
		boolean healed = false;
		List<PlayerEntity> targets = new ArrayList<>(Targeting.playersAround(player, SANCTUARY_RADIUS));
		if (!targets.contains(player)) targets.add(player);
		for (PlayerEntity p : targets) {
			if (p.getHealth() >= p.getMaxHealth()) continue;
			p.heal(heal);
			healed = true;
			player.getServerWorld().spawnParticles(ParticleTypes.HEART, p.getX(), p.getBodyY(1.0), p.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
		}
		if (healed) sound(player, Template.SANCTUARY, 0.5f);
	}

	private static void sync(ServerPlayerEntity player, Set<Template> active, AptitudesConfig cfg) {
		if (!ServerPlayNetworking.canSend(player, KeystonePayload.ID)) return;
		long now = now(player);
		UUID id = player.getUuid();
		List<KeystonePayload.Entry> entries = new ArrayList<>();
		for (Template t : active) {
			float progress = 1f;
			boolean armed = false;
			switch (t) {
				case BULWARK -> progress = KeystoneRules.progress(now, BULWARK_READY.getOrDefault(id, 0L), cfg.keystoneBulwarkCooldownSeconds);
				case TEMPEST -> progress = KeystoneRules.progress(now, TEMPEST_READY.getOrDefault(id, 0L), cfg.keystoneTempestCooldownSeconds);
				case SANCTUARY -> progress = KeystoneRules.progress(now, SANCTUARY_NEXT.getOrDefault(id, 0L), cfg.keystoneSanctuaryIntervalSeconds);
				case OVERLOAD -> {
					int every = KeystoneRules.overloadEvery(cfg.keystoneOverloadEvery);
					int counter = OVERLOAD.getOrDefault(id, 0);
					progress = Math.min(1f, counter / (float) Math.max(1, every - 1));
					armed = KeystoneRules.overloadReady(counter, every);
				}
				case SPECTER -> armed = now <= SPECTER_UNTIL.getOrDefault(id, -1L);
				default -> { }
			}
			// Redondeo para no reenviar por cambios mínimos.
			entries.add(new KeystonePayload.Entry(t.ordinal(), Math.round(progress * 20f) / 20f, armed));
		}
		if (entries.equals(LAST_SENT.get(id))) return;
		LAST_SENT.put(id, entries);
		ServerPlayNetworking.send(player, new KeystonePayload(entries));
	}

	// ------------------------------------------------------------ Utilidades

	private static void damage(ServerPlayerEntity player, LivingEntity target, float amount) {
		if (amount <= 0) return;
		boolean previous = inEffect;
		inEffect = true;
		try {
			target.damage(player.getDamageSources().indirectMagic(player, player), amount);
		} finally {
			inEffect = previous;
		}
	}

	private static List<LivingEntity> hostilesNear(ServerPlayerEntity player, Vec3d center, double radius, Entity except) {
		double r2 = radius * radius;
		return new ArrayList<>(player.getServerWorld().getEntitiesByClass(LivingEntity.class,
				new net.minecraft.util.math.Box(center, center).expand(radius),
				e -> e != except && Targeting.isHostileTo(player, e) && e.squaredDistanceTo(center) <= r2));
	}

	private static LivingEntity nearestHostile(ServerPlayerEntity player, LivingEntity from, double radius) {
		return hostilesNear(player, from.getPos(), radius, from).stream()
				.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(from))).orElse(null);
	}
}
