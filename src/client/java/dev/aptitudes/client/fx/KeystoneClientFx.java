package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.network.FxEventPayload;
import dev.aptitudes.role.KeystoneRules.Template;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V96: guion visual de las diez plantillas de piedra angular (el escalón S7). Una piedra angular se dispara muchas veces por
 * partida, así que cada guion es breve y barato: un destello propio de la plantilla con el color del rol, sin cámara salvo en las
 * defensivas (Baluarte) y en la explosión (Detonación) de quien las dispara. {@code power} = ordinal de la plantilla; {@code dx,dy,dz} =
 * dirección hacia el segundo objetivo (Tempestad, Contagio) o radio en {@code dx} (Detonación, Santuario).
 */
final class KeystoneClientFx {
	private KeystoneClientFx() {
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	private static void ring(ParticleEffect e, Vec3d c, double speed, int base, double y) {
		int n = FxClient.n(base);
		for (int i = 0; i < n; i++) {
			double a = Math.PI * 2 * i / n;
			FxClient.spawn(e, c.x, c.y + y, c.z, Math.cos(a) * speed, 0.02, Math.sin(a) * speed);
		}
	}

	static void fire(ClientWorld world, FxEventPayload p) {
		Template[] all = Template.values();
		int idx = Math.round(p.power());
		if (idx < 0 || idx >= all.length) return;
		Random rng = new Random(p.seed());
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		Vec3d dir = new Vec3d(p.dx(), p.dy(), p.dz());
		ParticleEffect tint = ParticleFx.transition(p.color(), 0xFFFFFF, 1.1f);
		boolean local = FxClient.isLocal(p.entityId());
		switch (all[idx]) {
			case CARNAGE -> {
				// Vitalidad robada: motas rojas que suben hacia el pecho.
				int n = FxClient.n(14);
				for (int i = 0; i < n; i++) {
					Vec3d d = randomDirection(rng);
					FxClient.spawn(tint, c.x + d.x, c.y + 0.8 + d.y * 0.6, c.z + d.z, -d.x * 0.1, 0.07, -d.z * 0.1);
				}
				ring(ParticleTypes.CRIMSON_SPORE, c, 0.18, 14, 0.2);
			}
			case BULWARK -> {
				// Escudo de tótem: anillo doble y destello dorado.
				ring(ParticleTypes.WAX_ON, c, 0.25, 24, 0.3);
				ring(tint, c, 0.14, 18, 1.1);
				FxClient.spawn(ParticleTypes.FLASH, c.x, c.y + 1.0, c.z, 0, 0, 0);
				if (local) {
					ScreenDynamics.fovPunch(-3f, 50, 40, 300);
					ScreenDynamics.vignette(p.color(), 0.3f, 40, 120, 400, 0f);
				}
			}
			case OVERLOAD -> {
				// Descarga gratuita: chispas en espiral hacia arriba.
				int n = FxClient.n(24);
				for (int i = 0; i < n; i++) {
					double a = i * 0.6;
					FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x + Math.cos(a) * 0.5, c.y + 0.2 + i * 0.07, c.z + Math.sin(a) * 0.5, 0, 0.05, 0);
				}
				FxClient.spawn(tint, c.x, c.y + 1.0, c.z, 0, 0.04, 0);
			}
			case DETONATION -> {
				double radius = Math.max(1.5, p.dx());
				FxClient.spawn(ParticleTypes.FLASH, c.x, c.y + 0.5, c.z, 0, 0, 0);
				ring(ParticleTypes.FLAME, c, radius * 0.11, 26, 0.2);
				ring(tint, c, radius * 0.08, 20, 0.3);
				int n = FxClient.n(18);
				for (int i = 0; i < n; i++) {
					Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.2);
					FxClient.spawn(ParticleTypes.LAVA, c.x, c.y + 0.5, c.z, d.x, Math.abs(d.y) + 0.1, d.z);
				}
				float f = local ? 0.6f : FxMath.falloff(FxClient.distanceTo(c), 10);
				if (f > 0) ScreenDynamics.shake(0.25f * f, 200, 20f);
			}
			case CONTAGION -> {
				// Los males saltan de un enemigo a otro: línea de motas.
				double len = dir.length();
				int n = FxClient.n(Math.max(6, (int) (len * 5)));
				for (int i = 0; i < n; i++) {
					double t = i / (double) n;
					Vec3d q = c.add(dir.multiply(t)).add((rng.nextDouble() - 0.5) * 0.2, Math.sin(t * Math.PI) * 0.4, (rng.nextDouble() - 0.5) * 0.2);
					FxClient.spawn(tint, q.x, q.y + 0.8, q.z, 0, 0.01, 0);
					if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.SQUID_INK, q.x, q.y + 0.8, q.z, 0, 0.02, 0);
				}
			}
			case LEGION -> {
				// Sin disparo propio; por si algún día lo tiene: sello breve.
				ring(tint, c, 0.12, 12, 0.2);
			}
			case SPECTER -> {
				// Posimagen: runas y niebla pálida donde estuvo.
				int n = FxClient.n(14);
				for (int i = 0; i < n; i++) {
					Vec3d d = randomDirection(rng).multiply(0.4);
					FxClient.spawn(ParticleTypes.ENCHANT, c.x + d.x, c.y + 1.0 + d.y, c.z + d.z, -d.x * 0.3, 0.05, -d.z * 0.3);
				}
				ring(ParticleTypes.CLOUD, c, 0.08, 10, 0.1);
			}
			case FORTUNE -> {
				// Monedas de suerte: destellos dorados que saltan.
				int n = FxClient.n(14);
				for (int i = 0; i < n; i++) {
					Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.12);
					FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x, c.y + 0.6, c.z, d.x, Math.abs(d.y) + 0.12, d.z);
				}
				FxClient.spawn(ParticleTypes.GLOW, c.x, c.y + 0.8, c.z, 0, 0.05, 0);
			}
			case TEMPEST -> {
				// Rayo corto, quebrado, entre el golpe y el segundo objetivo.
				double len = Math.max(0.5, dir.length());
				int n = FxClient.n(Math.max(8, (int) (len * 7)));
				for (int i = 0; i <= n; i++) {
					double t = i / (double) n;
					Vec3d q = c.add(dir.multiply(t)).add((rng.nextDouble() - 0.5) * 0.35, (rng.nextDouble() - 0.5) * 0.35, (rng.nextDouble() - 0.5) * 0.35);
					FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, q.x, q.y + 0.9, q.z, 0, 0, 0);
				}
				Vec3d end = c.add(dir);
				FxClient.spawn(ParticleTypes.FLASH, end.x, end.y + 0.9, end.z, 0, 0, 0);
			}
			case SANCTUARY -> {
				// Pulso de luz que se expande por el suelo y motas verdes que suben.
				double radius = Math.max(2.0, p.dx());
				ring(tint, c, radius * 0.07, 30, 0.15);
				int n = FxClient.n(16);
				for (int i = 0; i < n; i++) {
					double a = rng.nextDouble() * Math.PI * 2, r = rng.nextDouble() * radius * 0.7;
					FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r, 0, 0.06, 0);
				}
			}
		}
	}
}
