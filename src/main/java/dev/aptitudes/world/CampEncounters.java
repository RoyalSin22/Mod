package dev.aptitudes.world;

import dev.aptitudes.dungeon.CampLayout;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** Etiquetas persistentes y alarma local del campamento. No añade IA global ni mixins. */
public final class CampEncounters {
	private static final String PREFIX = "aptitudes.camp.";
	private static final String SITE = PREFIX + "site.";
	private static final String KIND = PREFIX + "kind.";
	private static final String ROLE = PREFIX + "role.";
	private static final String ALARMED = PREFIX + "alarmed";
	private CampEncounters() {}

	static void mark(MobEntity mob, CampLayout.Kind kind, String role, BlockPos origin) {
		if (mob == null) return;
		mob.addCommandTag(KIND + kind.name().toLowerCase(java.util.Locale.ROOT));
		mob.addCommandTag(ROLE + role);
		mob.addCommandTag(SITE + origin.getX() + "." + origin.getZ());
		if (role.equals("sleeper") || role.equals("guard")) mob.setAiDisabled(true);
	}

	public static int kindIndex(MobEntity mob) {
		if (!mob.getCommandTags().contains(ROLE + "captain")) return -1;
		for (int i = 0; i < CampLayout.Kind.values().length; i++)
			if (mob.getCommandTags().contains(KIND + CampLayout.Kind.values()[i].name().toLowerCase(java.util.Locale.ROOT)))
				return i;
		return -1;
	}

	public static void init() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getTime() % 20 != 0) return;
			for (ServerPlayerEntity player : world.getPlayers()) {
				if (player.isSpectator()) continue;
				for (MobEntity sentry : world.getEntitiesByClass(MobEntity.class,
						player.getBoundingBox().expand(24), mob -> mob.isAlive()
								&& mob.getCommandTags().stream().anyMatch(tag -> tag.startsWith(ROLE))
								&& !mob.getCommandTags().contains(ALARMED))) {
					boolean watch = sentry.getCommandTags().contains(ROLE + "captain")
							|| sentry.getCommandTags().contains(ROLE + "sentry");
					boolean guard = sentry.getCommandTags().contains(ROLE + "guard")
							&& sentry.squaredDistanceTo(player) < 8 * 8;
					boolean sleeper = sentry.getCommandTags().contains(ROLE + "sleeper")
							&& (sentry.squaredDistanceTo(player) < 4 * 4 || sentry.getHealth() < sentry.getMaxHealth());
					if (guard || sleeper) {
						sentry.setAiDisabled(false);
						sentry.setTarget(player);
					}
					if (!watch || sentry.squaredDistanceTo(player) >= 24 * 24
							|| !sentry.getVisibilityCache().canSee(player)) continue;
					String site = sentry.getCommandTags().stream().filter(tag -> tag.startsWith(SITE)).findFirst().orElse(null);
					if (site == null) continue;
					for (MobEntity member : world.getEntitiesByClass(MobEntity.class,
							new Box(sentry.getBlockPos()).expand(32), mob -> mob.isAlive()
									&& mob.getCommandTags().contains(site))) {
						member.addCommandTag(ALARMED);
						member.setAiDisabled(false);
						member.setTarget(player);
					}
				}
			}
		});
	}
}
