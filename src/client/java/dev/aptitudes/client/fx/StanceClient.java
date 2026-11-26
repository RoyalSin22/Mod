package dev.aptitudes.client.fx;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.StanceRules;
import dev.aptitudes.logic.StanceRules.Stance;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * V97: posturas de combate del piloto (Bersérker, Lancero, Monje y Mago). Cada tick recorre a los jugadores cercanos que sostienen el
 * arma de uno de esos roles y decide con {@link StanceRules} si muestran la postura; la capa de {@link PlayerAnims} la reproduce en
 * bucle por debajo de los gestos de habilidad. Se basa en el arma y no en el rol, así que también la ven en los demás jugadores sin
 * sincronizar nada nuevo. Solo cliente: no afecta al juego.
 */
final class StanceClient {
	private static final double RANGE = 48;
	private static final double ENEMY_RANGE = 10;
	private static final int CHECK_EVERY = 5;
	private static final Map<Integer, State> STATES = new HashMap<>();
	private static int tick;

	private static final class State {
		int timer;
		Stance shown;
	}

	private StanceClient() {
	}

	static void register() {
		ClientTickEvents.END_WORLD_TICK.register(StanceClient::tick);
	}

	private static boolean enabled() {
		ClientConfig c = ClientConfig.get();
		return !Boolean.FALSE.equals(c.combatStances) && !Boolean.FALSE.equals(c.playerAnimations);
	}

	private static Stance stanceOf(Item item) {
		if (item == ModItems.BERSERKER_AXE) return Stance.BERSERKER;
		if (item == ModItems.SPEAR) return Stance.LANCER;
		if (item == ModItems.HAND_WRAPS) return Stance.MONK;
		if (item == ModItems.ARCANE_STAFF) return Stance.MAGE;
		return null;
	}

	private static void tick(ClientWorld world) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.isPaused()) return;
		tick++;
		if (!enabled()) {
			clearAll(world);
			return;
		}
		boolean check = tick % CHECK_EVERY == 0;
		for (AbstractClientPlayerEntity player : world.getPlayers()) {
			if (player.squaredDistanceTo(mc.player) > RANGE * RANGE) continue;
			State s = STATES.computeIfAbsent(player.getId(), id -> new State());
			Stance stance = stanceOf(player.getMainHandStack().getItem());
			if (check) {
				boolean engaged = stance != null && (player.handSwinging || player.hurtTime > 0 || enemyNear(world, player));
				// Se actualiza cada CHECK_EVERY ticks: la cuenta atrás baja de golpe en esa cantidad.
				int t = s.timer;
				for (int i = 0; i < CHECK_EVERY; i++) t = StanceRules.nextTimer(t, engaged && i == 0);
				s.timer = t;
			}
			boolean blocked = StanceRules.blocked(player.isSprinting(), player.isSwimming(), player.isFallFlying(), player.hasVehicle(),
					player.isSleeping(), player.isClimbing(), player.isUsingItem(), !player.isAlive());
			Stance wanted = stance != null && StanceRules.active(s.timer, true, blocked) ? stance : null;
			if (wanted != s.shown) {
				s.shown = wanted;
				PlayerAnims.setStance(player, wanted == null ? null : wanted.animation());
			}
		}
		if (check) prune(world);
	}

	private static boolean enemyNear(ClientWorld world, AbstractClientPlayerEntity player) {
		Box box = player.getBoundingBox().expand(ENEMY_RANGE);
		return !world.getEntitiesByClass(HostileEntity.class, box, e -> e.isAlive()).isEmpty();
	}

	/** Olvida a los jugadores que ya no están en el mundo. */
	private static void prune(ClientWorld world) {
		for (Iterator<Integer> it = STATES.keySet().iterator(); it.hasNext(); ) {
			if (world.getEntityById(it.next()) == null) it.remove();
		}
	}

	private static void clearAll(ClientWorld world) {
		for (Map.Entry<Integer, State> e : STATES.entrySet()) {
			if (e.getValue().shown != null && world.getEntityById(e.getKey()) instanceof AbstractClientPlayerEntity p) PlayerAnims.setStance(p, null);
		}
		STATES.clear();
	}
}
