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
 *
 * <p>v24: además, en toda habilidad (antiguas y evolucionadas): gesto del brazo visible para todos, sello
 * rúnico animado que gira y sigue al jugador, y un remate propio de la CATEGORÍA del rol (marcial: tajo en
 * arco; arcana: orbes que orbitan; sombra: humo y estela; guardián: pilar de luz; oficio: chispas y
 * engranaje), con su sonido. Todo son partículas del servidor: lo ven todos los jugadores cercanos.</p>
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
		// v24: gesto de lanzamiento (lo ven todos), sello animado y remate por categoría.
		player.swingHand(net.minecraft.util.Hand.MAIN_HAND, true);
		animatedSigil(player, color, kind == Kind.ACTIVE ? 1.6 : kind == Kind.ULTIMATE ? 3.2 : 2.4,
				kind == Kind.ACTIVE ? 12 : 20);
		if (role != null) categoryFlourish(player, role.category(), color, kind != Kind.ACTIVE);
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

	/** Remate visual y sonoro según la categoría del rol. {@code big}: característica, definitiva o legendaria. */
	private static void categoryFlourish(ServerPlayerEntity player, dev.aptitudes.role.RoleCategory category, int color,
										 boolean big) {
		ServerWorld world = player.getServerWorld();
		Vec3d eye = player.getEyePos();
		Vec3d look = Targeting.horizontalFacing(player);
		DustParticleEffect dust = ParticleFx.dust(color, big ? 1.4f : 1.1f);
		float volume = big ? 0.9f : 0.6f;
		switch (category) {
			case MARTIAL -> { // tajo en arco delante del pecho
				for (int k = -6; k <= 6; k++) {
					Vec3d dir = look.rotateY((float) Math.toRadians(k * 12));
					Vec3d at = player.getPos().add(0, 1.1 + k * 0.03, 0).add(dir.multiply(1.6));
					world.spawnParticles(dust, at.x, at.y, at.z, 1, 0, 0, 0, 0);
				}
				world.spawnParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + look.x * 1.5, player.getBodyY(0.6),
						player.getZ() + look.z * 1.5, 1, 0, 0, 0, 0);
				sound(player, net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, volume, 0.9f);
			}
			case ARCANE -> { // orbes que orbitan y suben
				AbilityState.repeat(player.getServer(), 10, 1, i -> {
					if (!player.isAlive()) return;
					for (int o = 0; o < 3; o++) {
						double a = i * 0.6 + o * 2 * Math.PI / 3;
						Vec3d at = player.getPos().add(Math.cos(a) * 0.9, 0.4 + i * 0.16, Math.sin(a) * 0.9);
						world.spawnParticles(ParticleTypes.ENCHANT, at.x, at.y, at.z, 2, 0.02, 0.02, 0.02, 0.0);
						world.spawnParticles(dust, at.x, at.y, at.z, 1, 0, 0, 0, 0);
					}
				});
				sound(player, net.minecraft.sound.SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, volume, 1.2f);
			}
			case SHADOW -> { // humo y estela oscura
				world.spawnParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getBodyY(0.5), player.getZ(),
						big ? 24 : 12, 0.4, 0.6, 0.4, 0.01);
				world.spawnParticles(ParticleTypes.SQUID_INK, player.getX(), player.getBodyY(0.4), player.getZ(),
						big ? 10 : 5, 0.3, 0.4, 0.3, 0.02);
				ParticleFx.column(world, player.getPos().subtract(look.multiply(0.8)), 1.8, dust, 10);
				sound(player, net.minecraft.sound.SoundEvents.ENTITY_PHANTOM_FLAP, volume, 0.7f);
			}
			case GUARDIAN -> { // pilar de luz que sube
				AbilityState.repeat(player.getServer(), 8, 1, i -> {
					if (!player.isAlive()) return;
					world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + i * 0.35, player.getZ(),
							3, 0.15, 0.05, 0.15, 0.01);
				});
				ParticleFx.ring(world, player.getPos().add(0, 0.2, 0), 1.0, ParticleFx.dust(0xFFE082, 1.2f), 16);
				sound(player, net.minecraft.sound.SoundEvents.BLOCK_BEACON_POWER_SELECT, volume, 1.5f);
			}
			case CRAFT -> { // chispas eléctricas y engranaje
				world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, eye.x, eye.y - 0.4, eye.z, big ? 30 : 14, 0.5, 0.4, 0.5, 0.2);
				ParticleFx.ring(world, player.getPos().add(0, 1.0, 0), 0.9, dust, 12);
				for (int t = 0; t < 8; t++) { // dientes del engranaje
					double a = t * Math.PI / 4;
					Vec3d at = player.getPos().add(Math.cos(a) * 1.15, 1.0, Math.sin(a) * 1.15);
					world.spawnParticles(dust, at.x, at.y, at.z, 1, 0, 0, 0, 0);
				}
				sound(player, net.minecraft.sound.SoundEvents.BLOCK_SMITHING_TABLE_USE, volume, 1.3f);
			}
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
