package dev.aptitudes.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * v67 (F2+): punto único de enganche de las habilidades nuevas de cada rol ({@link BerserkerKit}, {@link MageKit},
 * {@link AssassinKit}, {@link HerbalistKit} y {@link EngineerKit}). El resto del código llama solo aquí, así que un
 * rol nuevo (F4–F7) añade su kit en esta clase y no toca más ganchos.
 */
public final class ExtraKits {
	private ExtraKits() {
	}

	static void registerAll() {
		BerserkerKit.registerAll();
		MageKit.registerAll();
		AssassinKit.registerAll();
		HerbalistKit.registerAll();
		EngineerKit.registerAll();
	}

	/** Bonificación de daño (se suma a las de {@code RoleMechanics.outgoing}). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		return BerserkerKit.outgoing(p, target, direct) + MageKit.outgoing(p, target, source, direct)
				+ AssassinKit.outgoing(p, target, source, direct);
	}

	/** Daño recibido por el jugador, después del resto de reducciones. */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		amount = BerserkerKit.incoming(p, source, amount);
		amount = MageKit.incoming(p, source, amount);
		amount = HerbalistKit.incoming(p, source, amount);
		amount = EngineerKit.incoming(p, source, amount);
		return amount;
	}

	public static void afterOutgoing(ServerPlayerEntity p, float damage) {
		BerserkerKit.afterOutgoing(p, damage);
	}

	/** Multiplicador del coste de maná de la activa {@code nodeId}. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		return MageKit.costMultiplier(p, nodeId) * EngineerKit.costMultiplier(p, nodeId);
	}

	/** ¿La próxima activa sale gratis? (Concatenación). Solo consulta: se gasta en {@link #onCast}. */
	public static boolean concatReady(ServerPlayerEntity p) {
		return MageKit.concatReady(p);
	}

	/** ¿La activa gratis de Concatenación también tarda la mitad en recargarse? */
	public static boolean concatHalvesCooldown(ServerPlayerEntity p) {
		return MageKit.concatHalves(p);
	}

	/**
	 * Tras lanzar una activa con éxito.
	 *
	 * @param key   id del nodo (o {@code signature.<rol>} / {@code ultimate.<rol>})
	 * @param spent maná que costó de verdad (0 si salió gratis)
	 * @param concat si salió gratis por Concatenación
	 */
	public static void onCast(ServerPlayerEntity p, String key, double spent, boolean concat) {
		BerserkerKit.onCast(p, key);
		MageKit.onCast(p, key, spent, concat);
		AssassinKit.onCast(p, key);
		EngineerKit.onCast(p, key);
	}

	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		AssassinKit.onKill(p, victim);
		EngineerKit.onKill(p, victim);
	}

	/** Fin de Sobremarcha del Ingeniero (Calor residual). */
	public static void onOverdriveEnd(ServerPlayerEntity p) {
		EngineerKit.onOverdriveEnd(p);
	}

	/** Cada tick del servidor. */
	public static void tick(MinecraftServer server) {
		BerserkerKit.tick(server);
		MageKit.tick(server);
		AssassinKit.tick(server);
		HerbalistKit.tick(server);
		EngineerKit.tick(server);
	}

	/** Al desconectarse un jugador. */
	public static void forget(ServerPlayerEntity p) {
		BerserkerKit.forget(p);
		MageKit.forget(p);
		AssassinKit.forget(p);
		HerbalistKit.forget(p);
		EngineerKit.forget(p);
	}
}
