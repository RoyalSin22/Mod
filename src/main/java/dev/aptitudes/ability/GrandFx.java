package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * V96 (primera tanda): guion de batalla de la firma (Z) y la definitiva (R) de los cinco roles piloto: Bersérker, Elementalista,
 * Cambiaformas, Paladín e Ingeniero. Un solo evento por lanzamiento; el cliente ({@code client/fx/GrandClientFx}) dibuja el
 * encuadre (aura, anillos, pilar, cámara) y el sonido cae en el tick de impacto del gesto. Las partículas del mundo que ya hacía
 * cada firma o definitiva se conservan: este guion solo sustituye al efecto genérico de {@link CastFx}.
 */
public final class GrandFx {
	private static final Set<Role> PILOTS = Set.of(Role.BERSERKER, Role.ELEMENTALIST, Role.SHAPESHIFTER, Role.PALADIN, Role.ENGINEER);

	private GrandFx() {
	}

	/** Claves de espera {@code signature.<rol>} y {@code ultimate.<rol>} de los roles piloto. */
	public static boolean handles(String abilityId) {
		if (abilityId == null) return false;
		int dot = abilityId.indexOf('.');
		if (dot < 0) return false;
		String kind = abilityId.substring(0, dot);
		if (!kind.equals("signature") && !kind.equals("ultimate")) return false;
		for (Role r : PILOTS) if (r.key().equals(abilityId.substring(dot + 1))) return true;
		return false;
	}

	public static boolean handles(Role role, boolean ultimate) {
		return PILOTS.contains(role);
	}

	private static SoundEvent motif(Role role) {
		return switch (role) {
			case BERSERKER -> ModSounds.GRAND_BERSERKER;
			case ELEMENTALIST -> ModSounds.GRAND_ELEMENTALIST;
			case SHAPESHIFTER -> ModSounds.GRAND_SHAPESHIFTER;
			case PALADIN -> ModSounds.GRAND_PALADIN;
			default -> ModSounds.GRAND_ENGINEER;
		};
	}

	private static void sound(ServerPlayerEntity p, SoundEvent s, float volume, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundCategory.PLAYERS, volume, pitch);
	}

	/** Firma: más breve y aguda. */
	public static void signature(ServerPlayerEntity p, Role role) {
		play(p, role, false);
	}

	/** Definitiva: más grave, con retumbo común y encuadre mayor. */
	public static void ultimate(ServerPlayerEntity p, Role role) {
		play(p, role, true);
	}

	private static void play(ServerPlayerEntity p, Role role, boolean ultimate) {
		if (!PILOTS.contains(role)) return;
		int variant = role == Role.ELEMENTALIST && RoleManager.get(p).activeElement() != null ? RoleManager.get(p).activeElement().ordinal() : 0;
		int id = ultimate ? FxIds.GRAND_ULTIMATE : FxIds.GRAND_SIGNATURE;
		FxEvents.on(p, id, CastFx.colorOf(p, role), role.ordinal(), new Vec3d(variant, 0, 0), p.getRandom().nextInt());
		String anim = ultimate ? PlayerAnimIds.GRAND_ULTIMATE : PlayerAnimIds.GRAND_SIGNATURE;
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(anim), () -> {
			if (!p.isAlive()) return;
			float j = 1f + (p.getRandom().nextFloat() - 0.5f) * 0.06f;
			sound(p, motif(role), ultimate ? 1.3f : 1.0f, (ultimate ? 0.85f : 1.1f) * j);
			if (ultimate) sound(p, ModSounds.GRAND_RUMBLE, 1.0f, 1.0f);
		});
	}
}
