package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.CastAnimationRules;
import dev.aptitudes.logic.CastAnimationRules.Kind;
import dev.aptitudes.network.CastAnimationPayload;
import dev.aptitudes.network.DamageNumberPayload;
import dev.aptitudes.network.ScreenFxPayload;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleCategory;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
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
 *
 * <p>El gesto y el sello rúnico se mantienen en el servidor. Un paquete inicia el remate 3D en cada
 * cliente cercano, que anima la geometría sin solicitar más datos al servidor.</p>
 */
public final class CastFx {
	private CastFx() {
	}

	public static int colorOf(ServerPlayerEntity player, @Nullable Role role) {
		if (role == Role.ELEMENTALIST) {
			Element element = RoleManager.get(player).activeElement();
			if (element != null) return element.color();
		}
		return role == null ? 0xB388FF : role.color();
	}

	public static void onCast(ServerPlayerEntity player, Role role, Kind kind) {
		ServerWorld world = player.getServerWorld();
		int color = colorOf(player, role);
		DustParticleEffect dust = ParticleFx.dust(color, kind == Kind.ACTIVE ? 1.0f : 1.6f);
		DustParticleEffect bright = ParticleFx.dust(lighten(color), 1.2f);
		Vec3d feet = player.getPos();
		// Gesto y sello compartidos, con un remate 3D por categoría.
		player.swingHand(net.minecraft.util.Hand.MAIN_HAND, true);
		animatedSigil(player, color, kind == Kind.ACTIVE ? 1.6 : kind == Kind.ULTIMATE ? 3.2 : 2.4,
				kind == Kind.ACTIVE ? 12 : 20);
		sendAnimation(player, role.category(), kind, color);
		if (kind != Kind.UNIQUE) castSound(player, role.category(), kind != Kind.ACTIVE);
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

	/**
	 * Sello rúnico animado: anillo exterior y una estrella de 6 puntas que gira, siguiendo al jugador
	 * durante {@code ticks} ticks (se apaga si muere o cambia de mundo).
	 */
	private static void animatedSigil(ServerPlayerEntity player, int color, double radius, int ticks) {
		ServerWorld origin = player.getServerWorld();
		DustParticleEffect dust = ParticleFx.dust(color, 0.9f);
		DustParticleEffect light = ParticleFx.dust(lighten(color), 0.7f);
		AbilityState.repeat(player.getServer(), ticks, 1, i -> {
			if (!player.isAlive() || player.getServerWorld() != origin) return;
			Vec3d c = player.getPos().add(0, 0.05, 0);
			double spin = i * Math.toRadians(9);
			double fade = 1.0 - i / (double) ticks;
			int ringPoints = (int) (10 + 14 * fade);
			for (int k = 0; k < ringPoints; k++) {
				double a = spin + 2 * Math.PI * k / ringPoints;
				origin.spawnParticles(dust, c.x + Math.cos(a) * radius, c.y, c.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
			}
			// Estrella de 6 puntas: dos triángulos que giran en sentidos opuestos.
			for (int tri = 0; tri < 2; tri++) {
				double rot = tri == 0 ? spin : -spin + Math.PI / 3;
				for (int v = 0; v < 3; v++) {
					double a1 = rot + 2 * Math.PI * v / 3;
					double a2 = rot + 2 * Math.PI * (v + 1) / 3;
					Vec3d p1 = c.add(Math.cos(a1) * radius * 0.85, 0, Math.sin(a1) * radius * 0.85);
					Vec3d p2 = c.add(Math.cos(a2) * radius * 0.85, 0, Math.sin(a2) * radius * 0.85);
					ParticleFx.line(origin, light, p1, p2, 4);
				}
			}
		});
	}

	private static void sendAnimation(ServerPlayerEntity player, RoleCategory category, Kind kind, int color) {
		var payload = new CastAnimationPayload(player.getId(), player.getUuid(), category.ordinal(),
				kind.ordinal(), color, player.getYaw());
		if (ServerPlayNetworking.canSend(player, CastAnimationPayload.ID)) {
			ServerPlayNetworking.send(player, payload);
		}
		for (ServerPlayerEntity watcher : PlayerLookup.tracking(player)) {
			if (watcher != player && watcher.squaredDistanceTo(player) <= CastAnimationRules.RANGE_SQUARED
					&& !player.isInvisibleTo(watcher)
					&& ServerPlayNetworking.canSend(watcher, CastAnimationPayload.ID)) {
				ServerPlayNetworking.send(watcher, payload);
			}
		}
	}

	private static void castSound(ServerPlayerEntity player, RoleCategory category, boolean big) {
		float volume = big ? 0.9f : 0.6f;
		switch (category) {
			case MARTIAL -> sound(player, net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, volume, 0.9f);
			case ARCANE -> sound(player, net.minecraft.sound.SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, volume, 1.2f);
			case SHADOW -> sound(player, net.minecraft.sound.SoundEvents.ENTITY_PHANTOM_FLAP, volume, 0.7f);
			case GUARDIAN -> sound(player, net.minecraft.sound.SoundEvents.BLOCK_BEACON_POWER_SELECT, volume, 1.5f);
			case CRAFT -> sound(player, net.minecraft.sound.SoundEvents.BLOCK_SMITHING_TABLE_USE, volume, 1.3f);
		}
	}

	private static void sound(ServerPlayerEntity p, net.minecraft.sound.SoundEvent sound, float volume, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, net.minecraft.sound.SoundCategory.PLAYERS,
				volume, pitch);
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
