package dev.aptitudes.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * v67 (F2+): punto único de enganche de las habilidades nuevas de cada rol ({@link BerserkerKit}, {@link MageKit},
 * {@link AssassinKit}, {@link HerbalistKit}, {@link EngineerKit}, {@link LancerKit}, {@link SwordsmanKit},
 * {@link PaladinKit}, {@link NecromancerKit}, {@link DruidKit}, {@link MerchantKit}, {@link MonkKit},
 * {@link ElementalistKit}, {@link ShapeshifterKit}, {@link GunslingerKit}, {@link WeaponSummonerKit},
 * {@link HunterKit}, {@link BardKit}, {@link WarlockKit} y {@link TamerKit}). El resto del código llama solo aquí, así que un rol futuro
 * añade su kit en esta clase y no toca más ganchos.
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
		LancerKit.registerAll();
		SwordsmanKit.registerAll();
		PaladinKit.registerAll();
		NecromancerKit.registerAll();
		DruidKit.registerAll();
		MerchantKit.registerAll();
		MonkKit.registerAll();
		ElementalistKit.registerAll();
		ShapeshifterKit.registerAll();
		GunslingerKit.registerAll();
		WeaponSummonerKit.registerAll();
		HunterKit.registerAll();
		BardKit.registerAll();
		WarlockKit.registerAll();
		TamerKit.registerAll();
	}

	/** Bonificación de daño (se suma a las de {@code RoleMechanics.outgoing}). */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct) {
		return BerserkerKit.outgoing(p, target, direct) + MageKit.outgoing(p, target, source, direct)
				+ AssassinKit.outgoing(p, target, source, direct) + LancerKit.outgoing(p, target, source, direct)
				+ SwordsmanKit.outgoing(p, target, source, direct) + PaladinKit.outgoing(p, target, source, direct)
				+ NecromancerKit.outgoing(p, target, source, direct) + DruidKit.outgoing(p, target, source, direct)
				+ MerchantKit.outgoing(p, target, source, direct) + MonkKit.outgoing(p, target, source, direct)
				+ ElementalistKit.outgoing(p, target, source, direct) + ShapeshifterKit.outgoing(p, target, source, direct)
				+ GunslingerKit.outgoing(p, target, source, direct) + WeaponSummonerKit.outgoing(p, target, source, direct)
				+ HunterKit.outgoing(p, target, source, direct) + BardKit.outgoing(p, target, source, direct)
				+ WarlockKit.outgoing(p, target, source, direct);
	}

	/**
	 * Daño recibido por el jugador, después del resto de reducciones. Voluntad de acero (Espadachín) va la última:
	 * mira el daño ya reducido para decidir si el golpe sería mortal.
	 */
	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		amount = BerserkerKit.incoming(p, source, amount);
		amount = MageKit.incoming(p, source, amount);
		amount = HerbalistKit.incoming(p, source, amount);
		amount = EngineerKit.incoming(p, source, amount);
		amount = PaladinKit.incoming(p, source, amount);
		amount = NecromancerKit.incoming(p, source, amount);
		amount = DruidKit.incoming(p, source, amount);
		amount = ShapeshifterKit.incoming(p, source, amount);
		amount = WeaponSummonerKit.incoming(p, source, amount);
		amount = BardKit.incoming(p, source, amount);
		MonkKit.onHurt(p, amount);
		amount = SwordsmanKit.incoming(p, source, amount);
		return SwordsmanKit.lethal(p, amount);
	}

	/** Daño que recibe un esbirro (Resurgir). Devuelve el daño final. */
	public static float minionIncoming(LivingEntity minion, DamageSource source, float amount) {
		return NecromancerKit.minionIncoming(minion, amount);
	}

	/** Tras calcular el daño final de un golpe del jugador. */
	public static void afterOutgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct,
									 float damage) {
		BerserkerKit.afterOutgoing(p, damage);
		LancerKit.afterOutgoing(p, target, direct, damage);
		DruidKit.afterOutgoing(p, target, direct, damage);
		MerchantKit.afterOutgoing(p, direct, damage);
		MonkKit.afterOutgoing(p, target, direct, damage);
		ElementalistKit.afterOutgoing(p, target, direct, damage);
		GunslingerKit.afterOutgoing(p, target, direct, damage);
		TamerKit.afterOutgoing(p, target, direct, damage);
	}

	/** Multiplicador del coste de maná de la activa {@code nodeId}. */
	public static double costMultiplier(ServerPlayerEntity p, String nodeId) {
		return MageKit.costMultiplier(p, nodeId) * EngineerKit.costMultiplier(p, nodeId)
				* ShapeshifterKit.costMultiplier(p, nodeId) * BardKit.costMultiplier(p, nodeId)
				* WarlockKit.costMultiplier(p, nodeId);
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
		LancerKit.onCast(p, key);
		MerchantKit.onCast(p, key, spent);
		ElementalistKit.onCast(p, key, spent);
		ShapeshifterKit.onCast(p, key);
		BardKit.onCast(p, key);
		WarlockKit.onCast(p, key);
	}

	public static void onKill(ServerPlayerEntity p, LivingEntity victim) {
		AssassinKit.onKill(p, victim);
		EngineerKit.onKill(p, victim);
		SwordsmanKit.onKill(p, victim);
		PaladinKit.onKill(p, victim);
		MerchantKit.onKill(p, victim);
		HunterKit.onKill(p, victim);
		BardKit.onKill(p, victim);
		WarlockKit.onKill(p, victim);
	}

	/** Un esbirro de {@code owner} ha matado a {@code victim} (Banquete). */
	public static void onMinionKill(ServerPlayerEntity owner, LivingEntity victim) {
		NecromancerKit.onMinionKill(owner, victim);
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
		LancerKit.tick(server);
		SwordsmanKit.tick(server);
		PaladinKit.tick(server);
		NecromancerKit.tick(server);
		DruidKit.tick(server);
		MerchantKit.tick(server);
		MonkKit.tick(server);
		ElementalistKit.tick(server);
		ShapeshifterKit.tick(server);
		GunslingerKit.tick(server);
		WeaponSummonerKit.tick(server);
		HunterKit.tick(server);
		BardKit.tick(server);
		WarlockKit.tick(server);
		TamerKit.tick(server);
	}

	/** Al desconectarse un jugador. */
	public static void forget(ServerPlayerEntity p) {
		BerserkerKit.forget(p);
		MageKit.forget(p);
		AssassinKit.forget(p);
		HerbalistKit.forget(p);
		EngineerKit.forget(p);
		LancerKit.forget(p);
		SwordsmanKit.forget(p);
		PaladinKit.forget(p);
		NecromancerKit.forget(p);
		DruidKit.forget(p);
		MerchantKit.forget(p);
		MonkKit.forget(p);
		ElementalistKit.forget(p);
		ShapeshifterKit.forget(p);
		GunslingerKit.forget(p);
		WeaponSummonerKit.forget(p);
		HunterKit.forget(p);
		BardKit.forget(p);
		WarlockKit.forget(p);
		TamerKit.forget(p);
	}
}
