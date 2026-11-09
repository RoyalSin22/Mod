package dev.aptitudes.boss;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;


/**
 * Fase A del plan de avance: etiquetas tácticas de los ataques de jefes, élites y campeones. Puro (sin Minecraft).
 *
 * <p>Cada ataque declara sus propiedades y las defensas las consultan; así no hace falta una condición por jefe.
 * Los valores de {@link Tag} solo se añaden al final.</p>
 */
public final class AttackTags {
	/** Propiedades de un ataque. */
	public enum Tag {
		/** Ataque corriente: ni pesado ni especial. */
		NORMAL,
		/** Pesado: causa más daño de postura. */
		HEAVY,
		/** Puede desviarse con parry. */
		PARRYABLE,
		/** Proyectil: el reflejo lo devuelve. */
		PROJECTILE,
		/** Impacto en el suelo: onda o área. */
		GROUND,
		/** Ni bloqueo ni guardia lo detienen. */
		UNBLOCKABLE
	}

	/** Multiplicador de daño de postura de un ataque pesado. */
	public static final double HEAVY_POSTURE_MULTIPLIER = 2.0;
	/** Carga del ataque cuerpo a cuerpo a partir de la cual el golpe cuenta como pesado (0–1). Provisional. */
	public static final float HEAVY_CHARGE = 0.9f;

	private AttackTags() {}

	/**
	 * Etiquetas de un golpe de jugador. Decisión del autor: cuenta como pesado el golpe cuerpo a cuerpo con la carga
	 * casi completa; el golpe a distancia o con la carga incompleta es NORMAL.
	 *
	 * @param chargeProgress carga del ataque del jugador (0–1)
	 * @param melee          el daño viene directamente del jugador (no de un proyectil)
	 */
	public static Set<Tag> playerHit(float chargeProgress, boolean melee) {
		return melee && chargeProgress >= HEAVY_CHARGE ? of(Tag.HEAVY) : of();
	}

	/** Conjunto de etiquetas; sin ninguna, el ataque es {@link Tag#NORMAL}. Inmutable. */
	public static Set<Tag> of(Tag... tags) {
		EnumSet<Tag> out = EnumSet.noneOf(Tag.class);
		if (tags != null) for (Tag t : tags) if (t != null) out.add(t);
		if (out.isEmpty()) out.add(Tag.NORMAL);
		return Collections.unmodifiableSet(out);
	}

	/** ¿Lo detiene el bloqueo normal? Los no bloqueables siempre llegan. */
	public static boolean blockable(Set<Tag> tags) {
		return !tags.contains(Tag.UNBLOCKABLE);
	}

	/** ¿Puede desviarse con parry? Un ataque no bloqueable no se desvía. */
	public static boolean parryable(Set<Tag> tags) {
		return tags.contains(Tag.PARRYABLE) && !tags.contains(Tag.UNBLOCKABLE);
	}

	/** Multiplicador del daño de postura que causa el ataque. */
	public static double postureMultiplier(Set<Tag> tags) {
		return tags.contains(Tag.HEAVY) ? HEAVY_POSTURE_MULTIPLIER : 1.0;
	}
}
