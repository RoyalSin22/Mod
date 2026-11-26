package dev.aptitudes.client.fx;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.client.ClientConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * V78: animaciones del cuerpo del jugador con Player Animation Library. Una sola capa de prioridad de jugabilidad que solo
 * reproduce animaciones disparadas por los efectos de batalla; así no pisa a otros mods (la librería apila las capas).
 * Los archivos están en {@code assets/aptitudes/player_animations/} y sus nombres en {@code PlayerAnimIds}.
 */
public final class PlayerAnims {
	/** Prioridad de «animaciones importantes de jugabilidad» según la documentación de la librería (1500 o más). */
	private static final int PRIORITY = 1500;
	private static final Identifier LAYER = Aptitudes.id("body");
	/** V97: capa de posturas de combate, por debajo de los gestos de habilidad (1500), que la tapan mientras suenan. */
	private static final int STANCE_PRIORITY = 1100;
	private static final Identifier STANCE_LAYER = Aptitudes.id("stance");
	private static int failures;

	private PlayerAnims() {
	}

	/** Registra la capa para todos los jugadores del cliente. Se llama una vez en el arranque del cliente. */
	public static void register() {
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, PRIORITY, player -> {
			PlayerAnimationController controller = new PlayerAnimationController(player, (c, state, setter) -> PlayState.STOP);
			// Por defecto PAL deja la animación «transparente» en primera persona (modo NONE) y no se ve nada. Mientras suena una
			// animación se dibujan los brazos y objetos del modelo desde la cámara; en reposo se devuelve el control a las manos vanilla.
			controller.setFirstPersonModeHandler(c -> c.isActive() ? FirstPersonMode.THIRD_PERSON_MODEL : FirstPersonMode.NONE);
			controller.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, true, true));
			return controller;
		});
	}

	/**
	 * V97: registra la capa de las posturas. En primera persona no hace nada (la cámara y las manos siguen siendo las de vanilla): las
	 * posturas se ven en tercera persona y en los demás jugadores.
	 */
	static void registerStance() {
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(STANCE_LAYER, STANCE_PRIORITY, player -> {
			PlayerAnimationController controller = new PlayerAnimationController(player, (c, state, setter) -> PlayState.STOP);
			controller.setFirstPersonModeHandler(c -> FirstPersonMode.NONE);
			return controller;
		});
	}

	/** Pone (o cambia) la postura del jugador; {@code name} null la quita. */
	static void setStance(AbstractClientPlayerEntity player, String name) {
		try {
			if (!(PlayerAnimationAccess.getPlayerAnimationLayer(player, STANCE_LAYER) instanceof PlayerAnimationController controller)) return;
			if (name == null) {
				controller.stopTriggeredAnimation();
			} else {
				controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(6, EasingType.EASE_IN_OUT_SINE), Aptitudes.id(name));
			}
		} catch (IllegalArgumentException e) {
			if (failures++ < 3) Aptitudes.LOGGER.warn("[Aptitudes] No se pudo cambiar la postura {}", name, e);
		}
	}

	/** Dispara la animación en el jugador si {@code entity} lo es y el jugador no las ha desactivado. */
	public static void play(Entity entity, String name) {
		trigger(entity, name, false);
	}

	/** Igual, pero con una transición suave desde lo que estuviera sonando (de la pose del salto al golpe, por ejemplo). */
	public static void playWithFade(Entity entity, String name) {
		trigger(entity, name, true);
	}

	private static void trigger(Entity entity, String name, boolean fade) {
		if (!(entity instanceof AbstractClientPlayerEntity player) || !ClientConfig.get().playerAnimations) return;
		try {
			IAnimation layer = PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER);
			if (!(layer instanceof PlayerAnimationController controller)) return;
			Identifier id = Aptitudes.id(name);
			if (fade) {
				controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(3, EasingType.EASE_IN_OUT_SINE), id);
			} else {
				controller.triggerAnimation(id);
			}
		} catch (IllegalArgumentException e) {
			// La librería avisa así si sus mixins no se aplicaron; las animaciones son un adorno y no deben tumbar el cliente.
			if (failures++ < 3) Aptitudes.LOGGER.warn("[Aptitudes] No se pudo reproducir la animación {}", name, e);
		}
	}

	/** Corta la animación disparada (p. ej. el salto, que se mantiene hasta que otra la sustituye). */
	public static void stop(Entity entity) {
		if (!(entity instanceof AbstractClientPlayerEntity player)) return;
		try {
			if (PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER) instanceof PlayerAnimationController controller) {
				controller.stopTriggeredAnimation();
			}
		} catch (IllegalArgumentException e) {
			if (failures++ < 3) Aptitudes.LOGGER.warn("[Aptitudes] No se pudo detener la animación", e);
		}
	}
}
