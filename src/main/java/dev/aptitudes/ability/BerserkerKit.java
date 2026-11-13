package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.*;

/**
 * v67 (F2): las 10 habilidades nuevas del Bersérker (N1–N10). Cifras en {@link BerserkerRules} (puro, JUnit); aquí
 * solo el comportamiento en el mundo. Estado en memoria por jugador: si el servidor se reinicia, las ventanas, las
 * cicatrices y los sangrados se pierden, y los atributos de Avatar son temporales (no se guardan con el jugador).
 *
 * <p>Ganchos: {@code RoleMechanics.outgoing} ({@link #outgoing}), {@code RolePassives} ({@link #incoming} y
 * {@link #afterOutgoing}), {@code AbilityManager.execute} ({@link #onCast}), {@code RoleEvents} ({@link #tick}) y la
 * desconexión ({@link #forget}).</p>
 */
public final class BerserkerKit {
	private static final String N = "berserker_n";
	private static final Identifier AVATAR_SCALE_ID = Aptitudes.id("berserker/avatar_scale");
	private static final Identifier AVATAR_KNOCKBACK_ID = Aptitudes.id("berserker/avatar_knockback");

	private static final class State {
		final Deque<Long> scars = new ArrayDeque<>();
		final Map<Integer, Long> roared = new HashMap<>();
		long chainUntil;
		double chainBonus;
		long echoUntil;
		int echoRank;
		long oathUntil;
		long avatarUntil;
		long leapStarted = -1;
		Vec3d leapFacing = Vec3d.ZERO;
	}

	/** Sangrado sobre una criatura: lo aplica un jugador; daño cada segundo hasta {@code until}. */
	private static final class Bleed {
		final LivingEntity target;
		final UUID owner;
		int stacks;
		double perStack;
		long until;

		Bleed(LivingEntity target, UUID owner) {
			this.target = target;
			this.owner = owner;
		}
	}

	/** Fisura ardiente de Ragnarök: puntos del suelo que prenden a los enemigos hasta {@code until}. */
	private record Fissure(ServerWorld world, UUID owner, List<Vec3d> points, long until) {}

	private static final Map<UUID, State> STATES = new HashMap<>();
	private static final Map<UUID, Bleed> BLEEDS = new HashMap<>();
	private static final List<Fissure> FISSURES = new ArrayList<>();
	/** Daño de las propias habilidades: no consume Cadena ni Eco, ni prende con Sangre hirviente. */
	private static boolean skillDamage;

	private BerserkerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", BerserkerKit::throwingAxe);
		Abilities.register(N + "4", BerserkerKit::bloodChain);
		Abilities.register(N + "6", BerserkerKit::rend);
		Abilities.register(N + "8", BerserkerKit::avatar);
		Abilities.register(N + "10", BerserkerKit::ragnarok);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Hacha arrojadiza: atraviesa a los enemigos de la línea, los desangra y vuelve a la mano. */
	private static boolean throwingAxe(ServerPlayerEntity p, int rank) {
		ServerWorld world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, AXE_RANGE);
		Vec3d path = end.subtract(start);
		List<LivingEntity> targets = new ArrayList<>();
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(1.2),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			if (distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) <= 1.2) targets.add(e);
		}
		double damage = axeDamage(rank, scale());
		ItemStackParticleEffect axe = new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.IRON_AXE));
		Set<Integer> hit = new HashSet<>();
		AbilityState.repeat(p.getServer(), 8, 1, i -> {
			Vec3d at = start.add(path.multiply((i + 1) / 8.0));
			world.spawnParticles(axe, at.x, at.y, at.z, 3, 0.1, 0.1, 0.1, 0.05);
			for (LivingEntity e : targets) {
				if (!e.isAlive() || e.squaredDistanceTo(at) > 2.5 || !hit.add(e.getId())) continue;
				skillHit(p, e, damage);
				bleed(p, e, 1, AXE_BLEED_PER_SECOND, AXE_BLEED_SECONDS * 20, false);
			}
		});
		// Vuelta a la mano: solo visual.
		AbilityState.repeat(p.getServer(), 8, 1, i -> {
			if (i == 0) sound(p, SoundEvents.ENTITY_WITCH_THROW, 0.7f);
			Vec3d at = end.add(p.getEyePos().subtract(end).multiply((i + 1) / 8.0));
			world.spawnParticles(axe, at.x, at.y, at.z, 2, 0.1, 0.1, 0.1, 0.02);
		});
		sound(p, SoundEvents.ENTITY_WITCH_THROW, 0.9f);
		return true;
	}

	/** N4 Cadena de sangre: arrastra al enemigo señalado y carga el siguiente golpe. */
	private static boolean bloodChain(ServerPlayerEntity p, int rank) {
		LivingEntity t = Targeting.raycastLiving(p, CHAIN_RANGE);
		if (t == null || !Targeting.isHostileImpactTarget(p, t)) return false;
		Vec3d pull = p.getPos().subtract(t.getPos());
		double distance = pull.length();
		if (distance > 1.5 && t.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) < 0.9) {
			t.setVelocity(pull.normalize().multiply(Math.min(2.2, 0.35 + distance * 0.16)).add(0, 0.35, 0));
			t.velocityModified = true;
		}
		ParticleFx.line(p.getServerWorld(), Ultimates.BLOOD, p.getPos().add(0, 1, 0), t.getPos().add(0, t.getHeight() / 2, 0),
				(int) (distance * 3) + 4);
		State s = state(p);
		s.chainUntil = now(p) + CHAIN_WINDOW_TICKS;
		s.chainBonus = chainBonus(rank);
		sound(p, SoundEvents.BLOCK_CHAIN_PLACE, 0.6f);
		return true;
	}

	/** N6 Desgarro: tres tajos en cono corto; cada uno añade una carga de sangrado. */
	private static boolean rend(ServerPlayerEntity p, int rank) {
		double damage = rendSlashDamage(rank, scale());
		double perStack = rendBleedPerStack(rank);
		AbilityState.repeat(p.getServer(), REND_SLASHES, 5, i -> {
			if (!p.isAlive()) return;
			Vec3d facing = Targeting.horizontalFacing(p);
			for (LivingEntity e : Targeting.hostilesAround(p, REND_RANGE)) {
				Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
				if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < 0.5) continue;
				skillHit(p, e, damage);
				bleed(p, e, 1, perStack, REND_BLEED_SECONDS * 20, true);
			}
			Vec3d center = p.getPos().add(facing.multiply(1.6)).add(0, 1, 0);
			p.getServerWorld().spawnParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 2, 0.4, 0.2, 0.4, 0);
			sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f + 0.2f * i);
		});
		return true;
	}

	/** N8 Avatar de la guerra: más grande, más daño y sin empuje ni Lentitud; al terminar, Debilidad. */
	private static boolean avatar(ServerPlayerEntity p, int rank) {
		state(p).avatarUntil = now(p) + avatarTicks(rank);
		setTemporary(p, EntityAttributes.GENERIC_SCALE, AVATAR_SCALE_ID, AVATAR_SCALE,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		setTemporary(p, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, AVATAR_KNOCKBACK_ID, 1.0,
				EntityAttributeModifier.Operation.ADD_VALUE);
		p.removeStatusEffect(StatusEffects.SLOWNESS);
		Targeting.burst(p.getServerWorld(), ParticleTypes.ANGRY_VILLAGER, p, 14, 0.8);
		sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.6f);
		return true;
	}

	/** N10 Ragnarök: salta al punto señalado; al caer abre una fisura en línea que lanza y quema. */
	private static boolean ragnarok(ServerPlayerEntity p, int rank) {
		Vec3d target = Targeting.lookEnd(p, RAGNAROK_RANGE);
		Vec3d flat = target.subtract(p.getPos()).multiply(1, 0, 1);
		State s = state(p);
		s.leapStarted = now(p);
		s.leapFacing = flat.lengthSquared() > 0.01 ? flat.normalize() : Targeting.horizontalFacing(p);
		p.setVelocity(flat.x / 10.0, 1.0, flat.z / 10.0);
		p.velocityModified = true;
		p.fallDistance = 0;
		Targeting.burst(p.getServerWorld(), ParticleTypes.CAMPFIRE_COSY_SMOKE, p, 12, 0.6);
		sound(p, SoundEvents.ENTITY_RAVAGER_STEP, 0.6f);
		return true;
	}

	private static void land(ServerPlayerEntity p, State s) {
		s.leapStarted = -1;
		p.fallDistance = 0;
		ServerWorld world = p.getServerWorld();
		double damage = ragnarokDamage(scale());
		List<Vec3d> points = new ArrayList<>();
		Set<Integer> hit = new HashSet<>();
		for (int i = 1; i <= RAGNAROK_FISSURE; i++) {
			Vec3d at = p.getPos().add(s.leapFacing.multiply(i));
			points.add(at);
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(1.5, 2, 1.5),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				if (!hit.add(e.getId())) continue;
				skillHit(p, e, damage);
				e.addVelocity(0, 0.8, 0);
				e.velocityModified = true;
			}
			world.spawnParticles(ParticleTypes.LAVA, at.x, at.y + 0.2, at.z, 4, 0.3, 0.1, 0.3, 0);
			world.spawnParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 8, 0.4, 0.2, 0.4, 0.02);
		}
		world.spawnParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 3, 0.6, 0.1, 0.6, 0);
		sound(p, SoundEvents.BLOCK_ANVIL_LAND, 0.5f);
		FISSURES.add(new Fissure(world, p.getUuid(), points, now(p) + RAGNAROK_FIRE_TICKS));
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/**
	 * Bonificación de daño que se suma en {@code RoleMechanics.outgoing}: Avatar (+30 %) siempre y, en golpes cuerpo
	 * a cuerpo propios, Cadena de sangre (se consume). También Sangre hirviente y Eco de batalla.
	 */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, boolean direct) {
		State s = STATES.get(p.getUuid());
		long now = now(p);
		float bonus = 0f;
		if (s != null && windowOpen(now, s.avatarUntil)) bonus += (float) AVATAR_DAMAGE;
		if (!direct || skillDamage) return bonus;
		int boiling = RoleManager.activeRank(p, N + "2");
		if (boiling > 0 && boiling(p.getHealth(), p.getMaxHealth())) {
			target.setOnFireFor(boilingFireSeconds(boiling));
		}
		if (s == null) return bonus;
		if (windowOpen(now, s.chainUntil)) {
			s.chainUntil = 0;
			bonus += (float) s.chainBonus;
			p.getServerWorld().spawnParticles(Ultimates.BLOOD, target.getX(), target.getBodyY(0.6), target.getZ(),
					14, 0.3, 0.4, 0.3, 0);
		}
		if (windowOpen(now, s.echoUntil)) {
			s.echoUntil = 0;
			int echoRank = s.echoRank;
			// Un tick después: así la onda no golpea dentro del daño que la provoca.
			AbilityState.schedule(p.getServer(), 1, () -> echoWave(p, echoRank, target));
		}
		return bonus;
	}

	private static void echoWave(ServerPlayerEntity p, int rank, LivingEntity first) {
		if (!p.isAlive()) return;
		Vec3d facing = Targeting.horizontalFacing(p);
		double damage = echoDamage(rank, scale());
		for (LivingEntity e : Targeting.hostilesAround(p, ECHO_RANGE)) {
			if (e == first) continue;
			Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
			if (to.lengthSquared() > 0.01 && to.normalize().dotProduct(facing) < 0.3) continue;
			skillHit(p, e, damage);
			e.takeKnockback(0.6, p.getX() - e.getX(), p.getZ() - e.getZ());
		}
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(facing.multiply(2)).add(0, 0.8, 0), 1.6,
				ParticleTypes.SWEEP_ATTACK, 8);
		sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.7f);
	}

	/** Daño recibido (tras las demás reducciones del jugador): Cicatrices y Rugido intimidante. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		if (!(source.getAttacker() instanceof LivingEntity attacker) || attacker == p || amount <= 0) return amount;
		int scarsRank = RoleManager.activeRank(p, N + "1");
		int roarRank = RoleManager.activeRank(p, N + "5");
		if (scarsRank <= 0 && roarRank <= 0) return amount;
		State s = state(p);
		long now = now(p);
		if (scarsRank > 0) {
			s.scars.removeIf(until -> !windowOpen(now, until));
			amount *= (float) (1.0 - scarReduction(s.scars.size()));
			if (scarTriggers(amount, p.getMaxHealth())) {
				s.scars.addLast(now + SCAR_TICKS);
				while (s.scars.size() > maxScars(scarsRank)) s.scars.removeFirst();
				p.getServerWorld().spawnParticles(Ultimates.BLOOD, p.getX(), p.getBodyY(0.6), p.getZ(), 8, 0.3, 0.4, 0.3, 0);
			}
		}
		if (roarRank > 0 && attacker.squaredDistanceTo(p) <= ROAR_RADIUS * ROAR_RADIUS) {
			s.roared.values().removeIf(at -> now - at >= ROAR_PER_ENEMY_TICKS);
			Long last = s.roared.get(attacker.getId());
			if (last == null || now - last >= ROAR_PER_ENEMY_TICKS) {
				s.roared.put(attacker.getId(), now);
				attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ROAR_TICKS, roarAmplifier(roarRank)));
				sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 1.4f);
			}
		}
		return amount;
	}

	/** Tras calcular el daño final: robo de vida del Juramento de sangre mientras dura Furia. */
	public static void afterOutgoing(ServerPlayerEntity p, float damage) {
		State s = STATES.get(p.getUuid());
		if (s != null && damage > 0 && windowOpen(now(p), s.oathUntil)) p.heal((float) (damage * OATH_LIFESTEAL));
	}

	/** Tras lanzar una activa (id del nodo en {@code key}): Juramento de sangre y Eco de batalla. */
	public static void onCast(ServerPlayerEntity p, String key) {
		if ("rage".equals(key)) {
			if (RoleManager.activeRank(p, N + "7") > 0) oath(p);
		} else if ("whirlwind".equals(key) || "berserker_m4".equals(key)) {
			int echo = RoleManager.activeRank(p, N + "9");
			if (echo > 0) {
				State s = state(p);
				s.echoUntil = now(p) + ECHO_WINDOW_TICKS;
				s.echoRank = echo;
			}
		}
	}

	/** N7: Furia cuesta el 10 % de la vida actual, dura un 50 % más y roba vida mientras dura. */
	private static void oath(ServerPlayerEntity p) {
		float cost = (float) oathCost(p.getHealth());
		if (p.getHealth() - cost >= 1.0f) p.setHealth(p.getHealth() - cost);
		int longest = 0;
		for (RegistryEntry<StatusEffect> effect : List.of(StatusEffects.STRENGTH, StatusEffects.SPEED)) {
			StatusEffectInstance current = p.getStatusEffect(effect);
			if (current == null) continue;
			int ticks = oathDuration(current.getDuration());
			longest = Math.max(longest, ticks);
			p.addStatusEffect(new StatusEffectInstance(effect, ticks, current.getAmplifier()));
		}
		state(p).oathUntil = now(p) + longest;
		p.getServerWorld().spawnParticles(Ultimates.BLOOD, p.getX(), p.getBodyY(0.5), p.getZ(), 20, 0.3, 0.5, 0.3, 0);
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (!STATES.isEmpty()) {
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				State s = STATES.get(p.getUuid());
				if (s == null) continue;
				if (s.avatarUntil > 0) {
					if (windowOpen(now, s.avatarUntil)) {
						p.removeStatusEffect(StatusEffects.SLOWNESS);
					} else {
						s.avatarUntil = 0;
						clearAvatar(p);
						p.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, AVATAR_WEAKNESS_TICKS, 0));
					}
				}
				if (s.leapStarted >= 0) {
					p.fallDistance = 0;
					long flying = now - s.leapStarted;
					if (!p.isAlive()) s.leapStarted = -1;
					else if ((flying > 5 && p.isOnGround()) || flying > 60) land(p, s);
				}
			}
		}
		if (now % 20 == 0) tickBleeds(server, now);
		if (now % 10 == 0) tickFissures(server, now);
	}

	private static void tickBleeds(MinecraftServer server, long now) {
		Iterator<Bleed> it = BLEEDS.values().iterator();
		while (it.hasNext()) {
			Bleed b = it.next();
			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(b.owner);
			if (owner == null || !b.target.isAlive() || b.target.isRemoved() || !windowOpen(now, b.until)) {
				it.remove();
				continue;
			}
			b.target.timeUntilRegen = 0;
			skillDamage = true;
			try {
				b.target.damage(bleedSource(owner), (float) bleedTick(b.stacks, b.perStack, scale()));
			} finally {
				skillDamage = false;
			}
			if (b.target.getWorld() instanceof ServerWorld world) {
				world.spawnParticles(Ultimates.BLOOD, b.target.getX(), b.target.getBodyY(0.6), b.target.getZ(),
						4 * b.stacks, 0.25, 0.3, 0.25, 0);
			}
		}
	}

	private static void tickFissures(MinecraftServer server, long now) {
		Iterator<Fissure> it = FISSURES.iterator();
		while (it.hasNext()) {
			Fissure f = it.next();
			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(f.owner());
			if (owner == null || !windowOpen(now, f.until())) {
				it.remove();
				continue;
			}
			for (Vec3d at : f.points()) {
				f.world().spawnParticles(ParticleTypes.FLAME, at.x, at.y + 0.1, at.z, 3, 0.4, 0.05, 0.4, 0.01);
				for (LivingEntity e : f.world().getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(0.9, 1, 0.9),
						e -> Targeting.isHostileImpactTarget(owner, e))) {
					e.setOnFireFor(2);
				}
			}
		}
	}

	/** Al desconectarse: se quitan los atributos de Avatar y se olvida el estado. */
	public static void forget(ServerPlayerEntity p) {
		clearAvatar(p);
		STATES.remove(p.getUuid());
		BLEEDS.values().removeIf(b -> b.owner.equals(p.getUuid()));
		FISSURES.removeIf(f -> f.owner().equals(p.getUuid()));
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static void bleed(ServerPlayerEntity p, LivingEntity e, int add, double perStack, int ticks, boolean stacking) {
		Bleed b = BLEEDS.get(e.getUuid());
		if (b == null || !b.owner.equals(p.getUuid())) {
			b = new Bleed(e, p.getUuid());
			BLEEDS.put(e.getUuid(), b);
		}
		b.stacks = stacking ? addBleedStack(b.stacks) : Math.max(b.stacks, add);
		b.perStack = Math.max(b.perStack, perStack);
		b.until = Math.max(b.until, now(p) + ticks);
	}

	/**
	 * Daño del sangrado: mágico, con el jugador como atacante (la muerte le cuenta) pero sin entidad de origen, para
	 * que no se trate como un golpe cuerpo a cuerpo (no suma Frenesí, combos ni bonificaciones de golpe directo).
	 */
	private static DamageSource bleedSource(ServerPlayerEntity owner) {
		return new DamageSource(owner.getServerWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE)
				.entryOf(DamageTypes.MAGIC), null, owner);
	}

	private static void skillHit(ServerPlayerEntity p, LivingEntity e, double damage) {
		skillDamage = true;
		try {
			e.damage(p.getDamageSources().playerAttack(p), (float) damage);
		} finally {
			skillDamage = false;
		}
	}

	private static void setTemporary(ServerPlayerEntity p, RegistryEntry<EntityAttribute> attribute, Identifier id,
									 double value, EntityAttributeModifier.Operation operation) {
		EntityAttributeInstance instance = p.getAttributeInstance(attribute);
		if (instance == null) return;
		instance.removeModifier(id);
		instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
	}

	private static void clearAvatar(ServerPlayerEntity p) {
		EntityAttributeInstance scale = p.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
		if (scale != null) scale.removeModifier(AVATAR_SCALE_ID);
		EntityAttributeInstance knockback = p.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
		if (knockback != null) knockback.removeModifier(AVATAR_KNOCKBACK_ID);
	}

	private static double distanceToSegment(Vec3d point, Vec3d a, Vec3d b) {
		Vec3d ab = b.subtract(a);
		double len = ab.lengthSquared();
		double t = len == 0 ? 0 : Math.max(0, Math.min(1, point.subtract(a).dotProduct(ab) / len));
		return point.distanceTo(a.add(ab.multiply(t)));
	}

	private static State state(ServerPlayerEntity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}

	private static long now(Entity e) {
		return e.getServer() == null ? 0 : e.getServer().getTicks();
	}

	private static double scale() {
		return Aptitudes.config().extraSkillDamageScale;
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}
}
