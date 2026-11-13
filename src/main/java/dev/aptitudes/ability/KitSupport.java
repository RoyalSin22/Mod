package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

/**
 * v67 (F3): ayudas comunes de los kits de habilidades nuevas ({@code MageKit}, {@code AssassinKit},
 * {@code HerbalistKit}, {@code EngineerKit}). Solo funciones sin estado.
 */
final class KitSupport {
	private KitSupport() {
	}

	/** Tick del servidor (0 si el jugador no tiene servidor, p. ej. en pruebas). */
	static long now(Entity e) {
		return e.getServer() == null ? 0 : e.getServer().getTicks();
	}

	/** Multiplicador de daño de las habilidades nuevas (opción {@code extraSkillDamageScale}). */
	static double scale() {
		return Aptitudes.config().extraSkillDamageScale;
	}

	/**
	 * ¿El daño es mágico? Coincide con el criterio de {@code RolePassives}: hechizos (daño mágico indirecto),
	 * explosiones y cualquier daño que no sea un golpe directo.
	 */
	static boolean isMagic(DamageSource source, boolean direct) {
		return !direct || source.isOf(DamageTypes.INDIRECT_MAGIC) || source.isOf(DamageTypes.MAGIC)
				|| source.isIn(DamageTypeTags.IS_EXPLOSION);
	}

	static DamageSource magic(ServerPlayerEntity p) {
		return p.getDamageSources().indirectMagic(p, p);
	}

	static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier), true, true));
	}

	/** Suma maná (sin pasar del máximo) y avisa al cliente. */
	static void addEnergy(ServerPlayerEntity p, double amount) {
		RoleData data = RoleManager.get(p);
		data.setEnergy(Math.max(0, Math.min(AbilityManager.maxEnergy(p), data.energy() + amount)));
		AbilityManager.sendEnergy(p, false);
	}

	/** Distancia de un punto al segmento {@code a}–{@code b}. */
	static double distanceToSegment(Vec3d point, Vec3d a, Vec3d b) {
		Vec3d ab = b.subtract(a);
		double len = ab.lengthSquared();
		double t = len == 0 ? 0 : Math.max(0, Math.min(1, point.subtract(a).dotProduct(ab) / len));
		return point.distanceTo(a.add(ab.multiply(t)));
	}
}
