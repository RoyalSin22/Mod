package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.role.Element;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * V91: efectos de batalla del Elementalista. El guion visual depende del elemento (14); el servidor manda el ordinal y el cliente
 * ({@code client/fx/ElementClientFx}) dibuja con el estilo de ese elemento. Los sonidos se agrupan por familia (fuego, hielo, rayo, tierra,
 * viento, agua, luz y cristal, sombra y vacío) y caen en el fotograma de impacto de la animación.
 */
public final class ElementFx {
	private static final Set<String> HANDLED = Set.of("elemental_bolt", "elemental_burst", "elemental_barrage", "elemental_aura");

	private ElementFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Sonido de la familia del elemento y su tono relativo. */
	private static SoundEvent soundOf(Element e) {
		return switch (e) {
			case FIRE, MAGMA -> ModSounds.ELEMENT_FIRE;
			case ICE -> ModSounds.ELEMENT_ICE;
			case LIGHTNING -> ModSounds.ELEMENT_LIGHTNING;
			case EARTH, PLANT -> ModSounds.ELEMENT_EARTH;
			case WIND, SOUND -> ModSounds.ELEMENT_WIND;
			case WATER, POISON -> ModSounds.ELEMENT_WATER;
			case LIGHT, CRYSTAL -> ModSounds.ELEMENT_MAGIC;
			case SHADOW, VOID -> ModSounds.ELEMENT_DARK;
		};
	}

	private static float pitchOf(Element e) {
		return switch (e) {
			case MAGMA, PLANT, POISON, VOID -> 0.85f;
			case SOUND, CRYSTAL -> 1.15f;
			default -> 1.0f;
		};
	}

	private static void play(ServerPlayerEntity p, Element e, Vec3d at, float volume, float pitch) {
		float jitter = (p.getRandom().nextFloat() - 0.5f) * 0.08f;
		p.getWorld().playSound(null, at.x, at.y, at.z, soundOf(e), SoundCategory.PLAYERS, volume, pitch * pitchOf(e) + jitter);
	}

	/** Proyectil elemental hasta {@code end}; suena en el tick 3. */
	public static void bolt(ServerPlayerEntity p, Element element, Vec3d end) {
		FxEvents.on(p, FxIds.ELEMENT_BOLT, element.color(), element.ordinal(), end.subtract(p.getEyePos()), p.getRandom().nextInt());
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ELEMENT_BOLT), () -> {
			if (p.isAlive()) play(p, element, p.getPos(), 0.9f, 1.0f);
		});
	}

	/** Un salto del rayo en cadena. */
	public static void arc(ServerPlayerEntity p, Element element, Vec3d from, Vec3d to) {
		FxEvents.onAt(p, from, FxIds.ELEMENT_ARC, element.color(), element.ordinal(), to.subtract(from), p.getRandom().nextInt());
	}

	/** Estallido elemental de radio {@code radius}; suena en el tick 4. */
	public static void burst(ServerPlayerEntity p, Element element, double radius) {
		FxEvents.on(p, FxIds.ELEMENT_BURST, element.color(), element.ordinal(), new Vec3d(radius, 0, 0), p.getRandom().nextInt());
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ELEMENT_BURST), () -> {
			if (p.isAlive()) play(p, element, p.getPos(), 1.2f, 0.8f);
		});
	}
}
