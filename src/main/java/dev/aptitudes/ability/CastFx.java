package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.network.DamageNumberPayload;
import dev.aptitudes.network.ScreenFxPayload;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Efectos visuales comunes al lanzar habilidades, por categoría:
 * <ul>
 *   <li><b>Activa</b>: círculo rúnico bajo los pies + espiral del color del rol.</li>
 *   <li><b>Característica</b>: círculo grande, doble espiral, onda expansiva, destello y sacudida suave.</li>
 *   <li><b>Definitiva</b>: todo lo anterior a mayor escala, columna de luz y sacudida fuerte.</li>
 * </ul>
 * El Elementalista usa el color de su elemento activo.
 */
public final class CastFx {
	public enum Kind { ACTIVE, SIGNATURE, ULTIMATE, UNIQUE }

	private CastFx() {
	}

	public static int colorOf(ServerPlayerEntity player, @Nullable Role role) {
		if (role == Role.ELEMENTALIST) {
			Element element = RoleManager.get(player).activeElement();
			if (element != null) return element.color();
		}
		return role == null ? 0xB388FF : role.color();
	}

	public static void onCast(ServerPlayerEntity player, @Nullable Role role, Kind kind) {
		ServerWorld world = player.getServerWorld();
		int color = colorOf(player, role);
		DustParticleEffect dust = ParticleFx.dust(color, kind == Kind.ACTIVE ? 1.0f : 1.6f);
		DustParticleEffect bright = ParticleFx.dust(lighten(color), 1.2f);
		Vec3d feet = player.getPos();
		switch (kind) {
			case ACTIVE -> {
				ParticleFx.runeCircle(world, feet, 1.3, dust);
				ParticleFx.spiral(world, feet, 0.8, 2.0, bright, 24, 1.5);
			}
			case SIGNATURE, UNIQUE -> {
				ParticleFx.runeCircle(world, feet, 2.6, dust);
				ParticleFx.spiral(world, feet, 1.1, 2.6, bright, 40, 2);
				ParticleFx.spiral(world, feet.add(0, 0.3, 0), 1.1, 2.6, dust, 40, -2);
				ParticleFx.shockwave(player.getServer(), world, feet, 5, dust, 8);
				screen(player, ScreenFxPayload.FLASH, color, 0.35f);
				screen(player, ScreenFxPayload.SHAKE, color, 0.3f);
			}
			case ULTIMATE -> {
				ParticleFx.runeCircle(world, feet, 3.6, dust);
				ParticleFx.runeCircle(world, feet.add(0, 0.6, 0), 2.4, bright);
				ParticleFx.spiral(world, feet, 1.4, 4.0, bright, 70, 3);
				ParticleFx.spiral(world, feet, 1.4, 4.0, dust, 70, -3);
				ParticleFx.column(world, feet, 8, ParticleTypes.END_ROD, 24);
				ParticleFx.shockwave(player.getServer(), world, feet, 9, dust, 12);
				screen(player, ScreenFxPayload.FLASH, color, 0.55f);
				screen(player, ScreenFxPayload.SHAKE, color, 0.7f);
			}
		}
	}

	/** Golpe fuerte recibido: sacudida proporcional al daño (desde 6 de daño). */
	public static void onHeavyHit(ServerPlayerEntity victim, float amount) {
		if (amount >= 6) screen(victim, ScreenFxPayload.SHAKE, 0xFF0000, Math.min(1f, amount / 20f));
	}

	public static void screen(ServerPlayerEntity player, int type, int color, float strength) {
		if (ServerPlayNetworking.canSend(player, ScreenFxPayload.ID)) {
			ServerPlayNetworking.send(player, new ScreenFxPayload(type, color, strength));
		}
	}

	/** Número de daño flotante (solo lo ve el atacante). */
	public static void damageNumber(ServerPlayerEntity attacker, LivingEntity target, float amount) {
		if (!Aptitudes.config().damageNumbers || amount <= 0) return;
		if (!ServerPlayNetworking.canSend(attacker, DamageNumberPayload.ID)) return;
		int color = amount >= 15 ? 0xFF5252 : amount >= 8 ? 0xFFC107 : 0xFFFFFF;
		double jitter = (attacker.getRandom().nextDouble() - 0.5) * 0.6;
		ServerPlayNetworking.send(attacker, new DamageNumberPayload(target.getX() + jitter,
				target.getY() + target.getHeight() + 0.25, target.getZ() + jitter, amount, color));
	}

	/**
	 * Impacto visible al golpear: estallido del color del rol en el objetivo, proporcional al daño.
	 * Habilidades/disparos (daño indirecto) siempre; cuerpo a cuerpo solo con un arma de tu rol
	 * (ítem característico o arma de evolución), para no recargar los golpes normales.
	 */
	public static void impact(ServerPlayerEntity hitter, LivingEntity target, net.minecraft.entity.damage.DamageSource source,
							  float amount) {
		if (amount <= 0) return;
		var data = RoleManager.get(hitter);
		boolean direct = source.getSource() == hitter;
		Role role = null;
		if (direct) {
			var hand = hitter.getMainHandStack();
			for (Role r : new Role[]{data.primary(), data.secondary()}) {
				if (r == null) continue;
				boolean roleWeapon = hand.isOf(r.iconStack().getItem())
						|| (hand.getItem() instanceof dev.aptitudes.item.weapon.RoleBladeItem blade && blade.role() == r);
				if (roleWeapon) {
					role = r;
					break;
				}
			}
			if (role == null) return;
		} else {
			role = data.primary();
		}
		ServerWorld world = hitter.getServerWorld();
		int color = colorOf(hitter, role);
		int count = 6 + (int) Math.min(14, amount);
		double y = target.getBodyY(0.55);
		world.spawnParticles(ParticleFx.dust(color, 1.1f), target.getX(), y, target.getZ(), count, 0.3, 0.35, 0.3, 0.02);
		if (!direct && amount >= 6) {
			ParticleFx.ring(world, new net.minecraft.util.math.Vec3d(target.getX(), y, target.getZ()), 0.8,
					ParticleFx.dust(lighten(color), 0.9f), 12);
		}
		if (amount >= 10) {
			world.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT, target.getX(), y, target.getZ(), 10, 0.3, 0.3, 0.3, 0.3);
		}
	}

	private static int lighten(int rgb) {
		int r = Math.min(255, (rgb >> 16 & 0xFF) + 80);
		int g = Math.min(255, (rgb >> 8 & 0xFF) + 80);
		int b = Math.min(255, (rgb & 0xFF) + 80);
		return r << 16 | g << 8 | b;
	}
}
