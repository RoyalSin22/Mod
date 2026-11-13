package dev.aptitudes.ability;

import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.aptitudes.ability.BerserkerRules.windowOpen;
import static dev.aptitudes.ability.ElementalistRules.*;

/**
 * v67 (F5): las 10 habilidades nuevas del Chamán (N1–N10). Cifras en {@link ElementalistRules} (puro, JUnit). Los
 * efectos por elemento viven en {@link #applyElement}: un único conmutador de 14 casos que usan todas las
 * habilidades. Estado en memoria: al reiniciar el servidor se pierden el cambio de marea, la armonía y la forma.
 */
public final class ElementalistKit {
	private static final String N = "elementalist_n";

	private static final class State {
		int lastIndex = -1;
		long tideUntil;
		long formUntil;
		long harmonyUntil;
		final Map<Element, Long> recent = new EnumMap<>(Element.class);
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private ElementalistKit() {
	}

	static void registerAll() {
		Abilities.register(N + "3", ElementalistKit::elementalLance);
		Abilities.register(N + "4", ElementalistKit::elementalWall);
		Abilities.register(N + "6", ElementalistKit::elementalTotem);
		Abilities.register(N + "8", ElementalistKit::elementalForm);
		Abilities.register(N + "10", ElementalistKit::elementalCollapse);
	}

	// ------------------------------------------------------------------------------------------------------ Activas

	/** N3 Lanza elemental: línea que atraviesa y aplica el efecto del elemento activo a cada enemigo. */
	private static boolean elementalLance(ServerPlayerEntity p, int rank) {
		Element element = active(p);
		if (element == null) return false;
		var world = p.getServerWorld();
		Vec3d start = p.getEyePos();
		Vec3d end = Targeting.lookEnd(p, LANCE_RANGE);
		float damage = (float) lanceDamage(rank, KitSupport.scale());
		Targeting.particleLine(world, element.particle(), start, end);
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(LANCE_WIDTH),
				e -> Targeting.isHostileImpactTarget(p, e))) {
			if (KitSupport.distanceToSegment(e.getPos().add(0, e.getHeight() / 2, 0), start, end) > LANCE_WIDTH) continue;
			applyElement(p, element, e, damage);
		}
		KitSupport.sound(p, SoundEvents.ENTITY_BLAZE_SHOOT, 1.2f);
		return true;
	}

	/** N4 Muro elemental: franja delante de ti que daña y aplica el efecto a quien la cruza. */
	private static boolean elementalWall(ServerPlayerEntity p, int rank) {
		Element element = active(p);
		if (element == null) return false;
		Vec3d facing = Targeting.horizontalFacing(p);
		Vec3d center = p.getPos().add(facing.multiply(WALL_DISTANCE));
		Vec3d right = new Vec3d(-facing.z, 0, facing.x);
		float damage = (float) wallDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), WALL_PULSES, WALL_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			var world = p.getServerWorld();
			for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
					new Box(center, center).expand(WALL_HALF_WIDTH + 1, 3, WALL_HALF_WIDTH + 1),
					e -> Targeting.isHostileImpactTarget(p, e))) {
				Vec3d rel = e.getPos().subtract(center);
				if (Math.abs(rel.dotProduct(right)) > WALL_HALF_WIDTH || Math.abs(rel.dotProduct(facing)) > WALL_HALF_THICKNESS) continue;
				applyElement(p, element, e, damage);
			}
			for (double w = -WALL_HALF_WIDTH; w <= WALL_HALF_WIDTH; w += 1.0) {
				Vec3d at = center.add(right.multiply(w));
				world.spawnParticles(element.particle(), at.x, at.y + 1, at.z, 2, 0.1, 0.8, 0.1, 0.01);
			}
		});
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.4f);
		return true;
	}

	/** N6 Tótem elemental: un tótem que lanza el elemento al enemigo más cercano cada segundo. */
	private static boolean elementalTotem(ServerPlayerEntity p, int rank) {
		Element element = active(p);
		if (element == null) return false;
		Vec3d at = Targeting.lookEnd(p, TOTEM_RANGE);
		float damage = (float) totemDamage(rank, KitSupport.scale());
		AbilityState.repeat(p.getServer(), TOTEM_PULSES, TOTEM_INTERVAL_TICKS, i -> {
			if (!p.isAlive()) return;
			var world = p.getServerWorld();
			world.spawnParticles(element.particle(), at.x, at.y + 1.2, at.z, 6, 0.2, 0.6, 0.2, 0.02);
			LivingEntity target = world.getEntitiesByClass(LivingEntity.class, new Box(at, at).expand(TOTEM_RADIUS),
							e -> Targeting.isHostileImpactTarget(p, e)).stream()
					.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(at))).orElse(null);
			if (target == null) return;
			Targeting.particleLine(world, element.particle(), at.add(0, 1.2, 0), target.getPos().add(0, target.getHeight() / 2, 0));
			applyElement(p, element, target, damage);
		});
		KitSupport.sound(p, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, 1.0f);
		return true;
	}

	/** N8 Forma elemental: tus golpes llevan el efecto del elemento activo y te resguardas. */
	private static boolean elementalForm(ServerPlayerEntity p, int rank) {
		if (active(p) == null) return false;
		int ticks = formTicks(rank);
		state(p).formUntil = KitSupport.now(p) + ticks;
		KitSupport.effect(p, StatusEffects.RESISTANCE, ticks, 0);
		Targeting.burst(p.getServerWorld(), active(p).particle(), p, 30, 0.8);
		KitSupport.sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.2f);
		return true;
	}

	/** N10 Colapso de elementos: una explosión con el efecto de cada elemento que dominas. */
	private static boolean elementalCollapse(ServerPlayerEntity p, int rank) {
		List<Element> mastered = RoleManager.get(p).elements();
		if (mastered.isEmpty()) return false;
		float damage = (float) collapseDamage(KitSupport.scale());
		var world = p.getServerWorld();
		for (LivingEntity e : Targeting.hostilesAround(p, COLLAPSE_RADIUS)) {
			e.damage(KitSupport.magic(p), damage);
			for (Element element : mastered) applyElement(p, element, e, 0f);
		}
		for (Element element : mastered) {
			ParticleFx.ring(world, p.getPos().add(0, 0.3 + 0.4 * mastered.indexOf(element), 0), COLLAPSE_RADIUS, element.particle(), 36);
		}
		world.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
		KitSupport.sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------ Ganchos

	/** Cambio de marea, Forma elemental (sin daño propio) y Armonía. */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		State s = STATES.get(p.getUuid());
		if (s == null) return 0f;
		long now = KitSupport.now(p);
		float bonus = 0f;
		int tide = RoleManager.activeRank(p, N + "1");
		if (tide > 0 && windowOpen(now, s.tideUntil)) bonus += (float) tideBonus(tide);
		int harmony = RoleManager.activeRank(p, N + "9");
		if (harmony > 0 && windowOpen(now, s.harmonyUntil)) bonus += (float) harmonyBonus(harmony);
		return bonus;
	}

	/** Eco del elemento y efecto de la Forma elemental en cada golpe directo. */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, boolean direct, float damage) {
		if (!direct || damage <= 0) return;
		Element element = active(p);
		if (element == null) return;
		State s = STATES.get(p.getUuid());
		long now = KitSupport.now(p);
		if (s != null && windowOpen(now, s.formUntil)) applyElement(p, element, target, 0f);
		int echo = RoleManager.activeRank(p, N + "2");
		if (echo > 0 && p.getRandom().nextDouble() < echoChance(echo)) {
			applyElement(p, element, target, 0f);
			Targeting.burst(p.getServerWorld(), element.particle(), target, 8, 0.4);
		}
	}

	/** Reacción en cadena, Fusión estable y Armonía: se evalúan tras lanzar una activa del Chamán. */
	public static void onCast(ServerPlayerEntity p, String key, double spent) {
		RoleNode node = RoleTree.get(key);
		if (node == null || node.role() != Role.ELEMENTALIST) return;
		Element element = active(p);
		if (element == null) return;
		long now = KitSupport.now(p);
		int harmony = RoleManager.activeRank(p, N + "9");
		if (harmony > 0) {
			State s = state(p);
			s.recent.put(element, now);
			s.recent.values().removeIf(at -> now - at > HARMONY_WINDOW_TICKS);
			if (harmonyReached(s.recent.size())) {
				s.harmonyUntil = now + HARMONY_TICKS;
				s.recent.clear();
				Targeting.burst(p.getServerWorld(), element.particle(), p, 25, 0.8);
				KitSupport.sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.4f);
			}
		}
		if (!key.equals("elemental_burst")) return;
		RoleData data = RoleManager.get(p);
		boolean fusion = false;
		for (var f : ElementFusions.values()) {
			if (f.involves(element) && f.unlocked(data.elements())) {
				fusion = true;
				break;
			}
		}
		if (!fusion) return;
		int chain = RoleManager.activeRank(p, N + "5");
		if (chain > 0 && spent > 0) KitSupport.addEnergy(p, spent * chainRefund(chain));
		if (RoleManager.activeRank(p, N + "7") > 0) {
			KitSupport.effect(p, StatusEffects.ABSORPTION, STEADY_TICKS, STEADY_ABSORPTION_AMPLIFIER);
			KitSupport.effect(p, StatusEffects.RESISTANCE, STEADY_TICKS, 0);
		}
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		if (now % 4 != 0) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (RoleManager.activeRank(p, N + "1") <= 0 && !STATES.containsKey(p.getUuid())) continue;
			RoleData data = RoleManager.get(p);
			if (!data.isActive(Role.ELEMENTALIST)) continue;
			State s = state(p);
			int index = data.activeElementIndex();
			if (s.lastIndex >= 0 && s.lastIndex != index && data.elements().size() > 1) s.tideUntil = now + TIDE_TICKS;
			s.lastIndex = index;
		}
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
	}

	// ----------------------------------------------------------------------------------------------------- Ayudas

	private static Element active(ServerPlayerEntity p) {
		RoleData data = RoleManager.get(p);
		return data.isActive(Role.ELEMENTALIST) ? data.activeElement() : null;
	}

	/** Efecto del elemento sobre {@code e}, con {@code damage} de daño mágico (0 = solo el efecto). */
	private static void applyElement(ServerPlayerEntity p, Element element, LivingEntity e, float damage) {
		DamageSource magic = KitSupport.magic(p);
		boolean undead = e.getType().isIn(EntityTypeTags.UNDEAD);
		Vec3d away = e.getPos().subtract(p.getPos()).multiply(1, 0, 1);
		Vec3d dir = away.lengthSquared() > 0.01 ? away.normalize() : Targeting.horizontalFacing(p);
		float dealt = damage;
		switch (element) {
			case FIRE -> e.setOnFireFor(4);
			case ICE -> {
				KitSupport.effect(e, StatusEffects.SLOWNESS, 60, 1);
				e.setFrozenTicks(e.getMinFreezeDamageTicks() + 40);
			}
			case LIGHTNING -> dealt = damage * 1.2f;
			case EARTH -> {
				e.addVelocity(0, 0.5, 0);
				e.velocityModified = true;
			}
			case WIND -> e.takeKnockback(1.4, -dir.x, -dir.z);
			case WATER -> {
				e.extinguish();
				p.heal(1f);
			}
			case PLANT -> KitSupport.effect(e, StatusEffects.POISON, 100, 0);
			case MAGMA -> {
				e.setOnFireFor(3);
				e.takeKnockback(0.5, -dir.x, -dir.z);
			}
			case SHADOW -> {
				KitSupport.effect(e, StatusEffects.WITHER, 60, 0);
				KitSupport.effect(e, StatusEffects.BLINDNESS, 40, 0);
			}
			case LIGHT -> {
				KitSupport.effect(e, StatusEffects.GLOWING, 100, 0);
				if (undead) dealt = damage * 2f;
			}
			case VOID -> {
				e.addVelocity(-dir.x * 0.9, 0.25, -dir.z * 0.9);
				e.velocityModified = true;
			}
			case POISON -> {
				KitSupport.effect(e, StatusEffects.POISON, 100, 0);
				KitSupport.effect(e, StatusEffects.NAUSEA, 80, 0);
			}
			case CRYSTAL -> {
				KitSupport.effect(e, StatusEffects.SLOWNESS, 40, 1);
				dealt = damage * 1.3f;
			}
			case SOUND -> {
				e.takeKnockback(1.0, -dir.x, -dir.z);
				KitSupport.effect(e, StatusEffects.NAUSEA, 60, 0);
				KitSupport.effect(e, StatusEffects.SLOWNESS, 30, 2);
			}
		}
		if (dealt > 0) e.damage(magic, dealt);
		Targeting.burst(p.getServerWorld(), element.particle(), e, 6, 0.3);
	}

	private static State state(Entity p) {
		return STATES.computeIfAbsent(p.getUuid(), u -> new State());
	}
}
