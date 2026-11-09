package dev.aptitudes.elite;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Marcas de campeón con partículas (enemigos, paso 1): dibuja la forma de cada afijo sobre los élites y campeones
 * cercanos a un jugador. No cambia el modelo ni el tamaño del mob. Cada 20 ticks, como máximo {@link GlyphRules#MAX_MARKS}
 * marcas por mob.
 */
public final class EliteGlyphs {
	/** Radio alrededor de cada jugador en el que se dibujan las marcas. */
	private static final double RANGE = 24.0;
	/** Intervalo de dibujo (1 s). */
	private static final int INTERVAL = 20;
	/** Escala de una unidad de la forma, en bloques. */
	private static final double SCALE = 0.22;

	private EliteGlyphs() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % INTERVAL != 0) return;
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!(player.getWorld() instanceof ServerWorld world)) continue;
				for (MobEntity mob : world.getEntitiesByClass(MobEntity.class,
						player.getBoundingBox().expand(RANGE), e -> e.isAlive() && Elites.isElite(e))) {
					draw(world, mob);
				}
			}
		});
	}

	private static void draw(ServerWorld world, MobEntity mob) {
		List<String> affixes = affixesOf(mob);
		for (int slot = 0; slot < Math.min(affixes.size(), GlyphRules.MAX_MARKS); slot++) {
			String affix = affixes.get(slot);
			ParticleEffect particle = particleOf(affix);
			double baseY = mob.getY() + GlyphRules.slotHeight(slot);
			for (GlyphRules.Point p : GlyphRules.shape(affix)) {
				world.spawnParticles(particle, mob.getX() + p.x() * SCALE, baseY + p.y() * SCALE, mob.getZ(), 1, 0, 0, 0, 0);
			}
		}
	}

	/** Afijos del mob, en el orden del enum; se leen de sus etiquetas de comando. */
	private static List<String> affixesOf(MobEntity mob) {
		List<String> out = new ArrayList<>();
		for (Elites.Mod mod : Elites.Mod.values()) {
			if (mob.getCommandTags().contains(mod.tag())) out.add(mod.name().toLowerCase(java.util.Locale.ROOT));
		}
		return out;
	}

	private static ParticleEffect particleOf(String affix) {
		return switch (affix) {
			case "fast" -> ParticleTypes.CLOUD;
			case "armored" -> ParticleTypes.ENCHANT;
			case "vampiric" -> ParticleTypes.HEART;
			case "brutal" -> ParticleTypes.CRIT;
			case "regenerating" -> ParticleTypes.HAPPY_VILLAGER;
			case "explosive" -> ParticleTypes.SMOKE;
			case "frost" -> ParticleTypes.SNOWFLAKE;
			case "thorny" -> ParticleTypes.ENCHANTED_HIT;
			case "searing" -> ParticleTypes.FLAME;
			case "blinking" -> ParticleTypes.PORTAL;
			default -> ParticleTypes.END_ROD;
		};
	}
}
