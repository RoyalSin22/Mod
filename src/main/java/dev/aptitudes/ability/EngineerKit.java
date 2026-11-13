package dev.aptitudes.ability;

import dev.aptitudes.party.PartyManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.EngineerRules.*;
import static dev.aptitudes.ability.BerserkerRules.windowOpen;

/**
 * v67 (F3): las 10 habilidades nuevas del Ingeniero (N1–N10). Cifras en {@link EngineerRules} (puro, JUnit); aquí
 * solo el comportamiento en el mundo. Estado en memoria: al reiniciar el servidor se pierden las minas, los domos, el
 * exotraje (sus atributos son temporales) y las marcas de Reciclaje. Ningún artilugio rompe bloques.
 */
public final class EngineerKit {
	private static final String N = "engineer_n";
	private static final Identifier EXO_ARMOR_ID = dev.aptitudes.Aptitudes.id("engineer/exo_armor");
	private static final Identifier EXO_KNOCKBACK_ID = dev.aptitudes.Aptitudes.id("engineer/exo_knockback");

	private record Mine(ServerWorld world, UUID owner, Vec3d pos, long until, double damage) {}

	private record Dome(ServerWorld world, UUID owner, Vec3d center, long until, double reduction) {}

	private static final List<Mine> MINES = new ArrayList<>();
	private static final List<Dome> DOMES = new ArrayList<>();
	/** Jugador → tick en que acaba el exotraje. */
	private static final Map<UUID, Long> EXO = new HashMap<>();
	/** Víctimas de un artilugio → tick del golpe (Reciclaje). */
	private static final Map<UUID, Long> GADGET_HITS = new HashMap<>();

	private EngineerKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", EngineerKit::proximityMine);
		Abilities.register(N + "4", EngineerKit::supportDrone);
		Abilities.register(N + "6", EngineerKit::protectiveDome);
		Abilities.register(N + "8", EngineerKit::exosuit);
		Abilities.register(N + "10", EngineerKit::teslaTower);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Mina de proximidad: hasta tres minas que estallan cuando un enemigo se acerca (sin romper bloques). */
	private static boolean proximityMine(ServerPlayerEntity p, int rank) {
		long now = KitSupport.now(p);
		MINES.add(new Mine(p.getServerWorld(), p.getUuid(), p.getPos(), now + duration(MINE_TICKS, thrift(p)),
				mineDamage(rank, KitSupport.scale())));
		int owned = 0;
		for (Mine m : MINES) if (m.owner().equals(p.getUuid())) owned++;
		for (Iterator<Mine> it = MINES.iterator(); it.hasNext() && owned > MINE_MAX; ) {
			if (it.next().owner().equals(p.getUuid())) {
				it.remove();
				owned--;
			}
		}
		p.getServerWorld().spawnParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 0.1, p.getZ(), 10, 0.2, 0.05, 0.2, 0.02);
		KitSupport.sound(p, SoundEvents.BLOCK_PISTON_EXTEND, 1.4f);
		return true;
	}

	/** N4 Dron de apoyo: dispara al enemigo más cercano cada medio segundo. */
	private static boolean supportDrone(ServerPlayerEntity p, int rank) {
		int ticks = duration(droneTicks(rank), thrift(p));
		ServerWorld world = p.getServerWorld();
		float damage = (float) (DRONE_DAMAGE * KitSupport.scale());
		AbilityState.repeat(p.getServer(), ticks / DRONE_INTERVAL_TICKS, DRONE_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			Vec3d drone = p.getPos().add(0, 2.4, 0);
			world.spawnParticles(ParticleTypes.END_ROD, drone.x, drone.y, drone.z, 1, 0.05, 0.05, 0.05, 0);
			LivingEntity target = Targeting.hostilesAround(p, DRONE_RANGE).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
			if (target == null) return;
			markGadgetHit(target);
			target.timeUntilRegen = 0;
			target.damage(KitSupport.magic(p), damage);
			Targeting.particleLine(world, ParticleTypes.ELECTRIC_SPARK, drone, target.getPos().add(0, target.getHeight() / 2, 0));
			KitSupport.sound(p, SoundEvents.ENTITY_ARROW_SHOOT, 1.8f);
		});
		KitSupport.sound(p, SoundEvents.ENTITY_BEE_LOOP, 1.5f);
		return true;
	}

	/** N6 Domo protector: bloquea proyectiles enemigos y reduce el daño de los aliados que están dentro. */
	private static boolean protectiveDome(ServerPlayerEntity p, int rank) {
		long until = KitSupport.now(p) + duration(DOME_TICKS, thrift(p));
		DOMES.add(new Dome(p.getServerWorld(), p.getUuid(), p.getPos(), until, domeReduction(rank)));
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.1, 0), DOME_RADIUS, ParticleTypes.END_ROD, 36);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.8f);
		return true;
	}

	/** N8 Exotraje: más armadura, absorción y salto, e inmune al empuje. */
	private static boolean exosuit(ServerPlayerEntity p, int rank) {
		int ticks = exoTicks(rank);
		EXO.put(p.getUuid(), KitSupport.now(p) + ticks);
		setTemporary(p, EntityAttributes.GENERIC_ARMOR, EXO_ARMOR_ID, EXO_ARMOR, EntityAttributeModifier.Operation.ADD_VALUE);
		setTemporary(p, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, EXO_KNOCKBACK_ID, 1.0, EntityAttributeModifier.Operation.ADD_VALUE);
		p.setAbsorptionAmount(Math.max(p.getAbsorptionAmount(), EXO_ABSORPTION));
		KitSupport.effect(p, StatusEffects.JUMP_BOOST, ticks, 1);
		Targeting.burst(p.getServerWorld(), ParticleTypes.END_ROD, p, 30, 0.8);
		KitSupport.sound(p, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(), 0.7f);
		return true;
	}

	/** N10 Torre Tesla: una torre que descarga cada segundo sobre hasta tres enemigos. */
	private static boolean teslaTower(ServerPlayerEntity p, int rank) {
		Vec3d base = p.getPos();
		ServerWorld world = p.getServerWorld();
		int seconds = duration(TESLA_TICKS, thrift(p)) / 20;
		float damage = (float) (TESLA_DAMAGE * KitSupport.scale());
		Vec3d top = base.add(0, 2.5, 0);
		AbilityState.repeat(p.getServer(), seconds, 20, i -> {
			ParticleFx.column(world, base, 2.5, ParticleTypes.END_ROD, 8);
			if (!p.isAlive()) return;
			List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, new Box(base, base).expand(TESLA_RADIUS, 4, TESLA_RADIUS),
					e -> Targeting.isHostileImpactTarget(p, e));
			targets.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(base)));
			for (int n = 0; n < Math.min(TESLA_TARGETS, targets.size()); n++) {
				LivingEntity e = targets.get(n);
				markGadgetHit(e);
				e.damage(KitSupport.magic(p), damage);
				Targeting.particleLine(world, ParticleTypes.ELECTRIC_SPARK, top, e.getPos().add(0, e.getHeight() / 2, 0));
			}
			if (!targets.isEmpty()) KitSupport.sound(p, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 1.8f);
		});
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.2f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Aislante (menos daño de explosiones, fuego y rayos) y Domo protector (menos daño de cualquier tipo). */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		int insulation = RoleManager.activeRank(p, N + "1");
		if (insulation > 0 && (source.isIn(DamageTypeTags.IS_EXPLOSION) || source.isIn(DamageTypeTags.IS_FIRE)
				|| source.isIn(DamageTypeTags.IS_LIGHTNING))) {
			amount *= (float) (1.0 - insulation(insulation));
		}
		if (DOMES.isEmpty() || p.getServer() == null) return amount;
		for (Dome dome : DOMES) {
			if (dome.world() != p.getWorld() || p.getPos().squaredDistanceTo(dome.center()) > DOME_RADIUS * DOME_RADIUS) continue;
			ServerPlayerEntity owner = p.getServer().getPlayerManager().getPlayer(dome.owner());
			if (owner != null && PartyManager.isAlly(owner, p)) amount *= (float) (1.0 - dome.reduction());
		}
		return amount;
	}

	/** Modo ahorro: los artilugios cuestan un 20 % menos de maná. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		return isGadget(nodeId) ? EngineerRules.costMultiplier(nodeId, thrift(p)) : 1.0;
	}

	/** Modo ahorro: los artilugios duran un 30 % más (1,0 si no lo tienes). */
	public static double durationFactor(ServerPlayerEntity p) {
		return thrift(p) ? THRIFT_DURATION : 1.0;
	}

	/** Cadena de montaje: cada activa del Ingeniero recorta la espera de tus otros artilugios. */
	public static void onCast(ServerPlayerEntity p, String key) {
		int rank = RoleManager.activeRank(p, N + "9");
		if (rank <= 0) return;
		RoleNode node = RoleTree.get(key);
		if (node == null || node.role() != Role.ENGINEER) return;
		for (String id : ASSEMBLY) {
			if (!id.equals(key)) AbilityManager.reduceCooldown(p, id, assemblyTicks(rank));
		}
	}

	/** Reciclaje: los enemigos muertos por tus artilugios te devuelven maná. */
	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		Long hit = GADGET_HITS.remove(victim.getUuid());
		int rank = RoleManager.activeRank(p, N + "2");
		if (hit == null || rank <= 0 || KitSupport.now(p) - hit > GADGET_KILL_TICKS) return;
		KitSupport.addEnergy(p, recycleMana(rank));
		Targeting.burst(p.getServerWorld(), ParticleTypes.ELECTRIC_SPARK, p, 8, 0.4);
	}

	/** Marca a una criatura como golpeada por un artilugio (Reciclaje). */
	public static void markGadgetHit(LivingEntity victim) {
		if (victim.getServer() != null) GADGET_HITS.put(victim.getUuid(), (long) victim.getServer().getTicks());
	}

	/** Calor residual: al acabar Sobremarcha, una explosión de calor a tu alrededor. */
	public static void onOverdriveEnd(ServerPlayerEntity p) {
		int rank = RoleManager.activeRank(p, N + "5");
		if (rank <= 0) return;
		float damage = (float) heatDamage(rank, KitSupport.scale());
		for (LivingEntity e : Targeting.hostilesAround(p, HEAT_RADIUS)) {
			e.damage(p.getDamageSources().explosion(p, p), damage);
		}
		ParticleFx.shockwave(p.getServer(), p.getServerWorld(), p.getPos(), HEAT_RADIUS, ParticleTypes.FLAME, 6);
		KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.5f);
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (!EXO.isEmpty()) {
			for (Iterator<Map.Entry<UUID, Long>> it = EXO.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<UUID, Long> entry = it.next();
				if (windowOpen(now, entry.getValue())) continue;
				it.remove();
				ServerPlayerEntity p = server.getPlayerManager().getPlayer(entry.getKey());
				if (p != null) clearExo(p);
			}
		}
		if (!DOMES.isEmpty()) tickDomes(server, now);
		if (now % 5 == 0 && !MINES.isEmpty()) tickMines(server, now);
		if (now % 100 == 0 && !GADGET_HITS.isEmpty()) GADGET_HITS.values().removeIf(at -> now - at > GADGET_KILL_TICKS);
	}

	private static void tickMines(MinecraftServer server, long now) {
		for (Iterator<Mine> it = MINES.iterator(); it.hasNext(); ) {
			Mine mine = it.next();
			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(mine.owner());
			if (owner == null || !windowOpen(now, mine.until())) {
				it.remove();
				continue;
			}
			if (now % 20 == 0) mine.world().spawnParticles(ParticleTypes.CRIT, mine.pos().x, mine.pos().y + 0.1, mine.pos().z, 2, 0.15, 0.02, 0.15, 0);
			boolean triggered = !mine.world().getEntitiesByClass(LivingEntity.class,
					new Box(mine.pos(), mine.pos()).expand(MINE_TRIGGER, 1.5, MINE_TRIGGER), e -> Targeting.isHostileImpactTarget(owner, e)).isEmpty();
			if (!triggered) continue;
			it.remove();
			for (LivingEntity e : mine.world().getEntitiesByClass(LivingEntity.class,
					new Box(mine.pos(), mine.pos()).expand(MINE_BLAST, 2, MINE_BLAST), e -> Targeting.isHostileImpactTarget(owner, e))) {
				markGadgetHit(e);
				e.damage(owner.getDamageSources().explosion(owner, owner), (float) mine.damage());
			}
			mine.world().spawnParticles(ParticleTypes.EXPLOSION, mine.pos().x, mine.pos().y + 0.4, mine.pos().z, 2, 0.3, 0.1, 0.3, 0);
			mine.world().playSound(null, mine.pos().x, mine.pos().y, mine.pos().z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
					net.minecraft.sound.SoundCategory.PLAYERS, 0.9f, 1.3f);
		}
	}

	private static void tickDomes(MinecraftServer server, long now) {
		for (Iterator<Dome> it = DOMES.iterator(); it.hasNext(); ) {
			Dome dome = it.next();
			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(dome.owner());
			if (owner == null || !windowOpen(now, dome.until())) {
				it.remove();
				continue;
			}
			if (now % 5 == 0) ParticleFx.ring(dome.world(), dome.center().add(0, 0.1, 0), DOME_RADIUS, ParticleTypes.END_ROD, 24);
			for (ProjectileEntity projectile : dome.world().getEntitiesByClass(ProjectileEntity.class,
					new Box(dome.center(), dome.center()).expand(DOME_RADIUS, DOME_RADIUS, DOME_RADIUS), pr -> true)) {
				if (projectile.getPos().squaredDistanceTo(dome.center()) > DOME_RADIUS * DOME_RADIUS) continue;
				if (projectile.getOwner() != null && PartyManager.isAlly(owner, projectile.getOwner())) continue;
				dome.world().spawnParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 6, 0.1, 0.1, 0.1, 0.1);
				projectile.discard();
			}
		}
	}

	public static void forget(ServerPlayerEntity p) {
		MINES.removeIf(m -> m.owner().equals(p.getUuid()));
		DOMES.removeIf(d -> d.owner().equals(p.getUuid()));
		if (EXO.remove(p.getUuid()) != null) clearExo(p);
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static boolean thrift(ServerPlayerEntity p) {
		return RoleManager.activeRank(p, N + "7") > 0;
	}

	private static void setTemporary(ServerPlayerEntity p, RegistryEntry<EntityAttribute> attribute, Identifier id, double value,
									 EntityAttributeModifier.Operation operation) {
		EntityAttributeInstance instance = p.getAttributeInstance(attribute);
		if (instance == null) return;
		instance.removeModifier(id);
		instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
	}

	private static void clearExo(ServerPlayerEntity p) {
		EntityAttributeInstance armor = p.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
		if (armor != null) armor.removeModifier(EXO_ARMOR_ID);
		EntityAttributeInstance knockback = p.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
		if (knockback != null) knockback.removeModifier(EXO_KNOCKBACK_ID);
		p.setAbsorptionAmount(Math.max(0f, p.getAbsorptionAmount() - EXO_ABSORPTION));
	}
}
