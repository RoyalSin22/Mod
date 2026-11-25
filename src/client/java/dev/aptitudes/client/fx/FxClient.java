package dev.aptitudes.client.fx;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.hud.ClientFx;
import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.network.FxEventPayload;
import dev.aptitudes.network.ScreenFxPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * V77: receptor de los eventos de efectos de batalla y utilidades comunes (calidad de partículas, suelo, distancias).
 * Cada rol añade su coreografía en su propia clase y la enlaza en {@link #dispatch}.
 */
public final class FxClient {
	private static int failures;

	private FxClient() {
	}

	public static void register() {
		FxTimeline.register();
		PlayerAnims.register();
		FxDebugCommand.register();
		ClientPlayNetworking.registerGlobalReceiver(FxEventPayload.ID, (payload, context) -> onEvent(payload));
	}

	public static void clear() {
		FxTimeline.clear();
		ScreenDynamics.clear();
	}

	private static void onEvent(FxEventPayload p) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null || mc.player == null || !FxIds.valid(p.id()) || intensity() <= 0f) return;
		try {
			dispatch(mc.world, p);
		} catch (RuntimeException e) {
			// Un efecto visual nunca debe tumbar el cliente; se avisa unas pocas veces para no inundar el registro.
			if (failures++ < 5) Aptitudes.LOGGER.warn("[Aptitudes] Falló el efecto {}", p.id(), e);
		}
	}

	private static void dispatch(ClientWorld world, FxEventPayload p) {
		switch (p.id()) {
			case FxIds.BERSERKER_RAGE -> BerserkerClientFx.rage(world, p);
			case FxIds.BERSERKER_WAR_CRY -> BerserkerClientFx.warCry(world, p);
			case FxIds.BERSERKER_LEAP_TAKEOFF -> BerserkerClientFx.leapTakeoff(world, p);
			case FxIds.BERSERKER_LEAP_TRAIL -> BerserkerClientFx.leapTrail(world, p);
			case FxIds.BERSERKER_SLAM -> BerserkerClientFx.slam(world, p);
			case FxIds.BERSERKER_WHIRL -> BerserkerClientFx.whirl(world, p);
			case FxIds.MAGE_BOLT -> MageClientFx.bolt(world, p);
			case FxIds.MAGE_BLINK -> MageClientFx.blink(world, p);
			case FxIds.MAGE_TIME_WARP -> MageClientFx.timeWarp(world, p);
			case FxIds.MAGE_MISSILE -> MageClientFx.missile(world, p);
			case FxIds.ASSASSIN_VANISH -> AssassinClientFx.vanish(world, p);
			case FxIds.ASSASSIN_STEP -> AssassinClientFx.step(world, p);
			case FxIds.ASSASSIN_FAN -> AssassinClientFx.fan(world, p);
			case FxIds.ASSASSIN_KNIFE -> AssassinClientFx.knife(world, p);
			case FxIds.ASSASSIN_SHURIKEN -> AssassinClientFx.shuriken(world, p);
			case FxIds.HERBALIST_BALM -> HerbalistClientFx.balm(world, p);
			case FxIds.HERBALIST_BALM_ALLY -> HerbalistClientFx.balmAlly(world, p);
			case FxIds.HERBALIST_RAIN -> HerbalistClientFx.rain(world, p);
			case FxIds.HERBALIST_SPORE -> HerbalistClientFx.spore(world, p);
			case FxIds.ENGINEER_GRAPPLE -> EngineerClientFx.grapple(world, p);
			case FxIds.ENGINEER_GRENADE -> EngineerClientFx.grenade(world, p);
			case FxIds.LANCER_LEAP -> LancerClientFx.leap(world, p);
			case FxIds.LANCER_SLAM -> LancerClientFx.slam(world, p);
			case FxIds.LANCER_THRUST -> LancerClientFx.thrust(world, p);
			case FxIds.SWORDSMAN_IAIDO -> SwordsmanClientFx.iaido(world, p);
			case FxIds.SWORDSMAN_CUT -> SwordsmanClientFx.cut(world, p);
			case FxIds.PALADIN_CONSECRATE -> PaladinClientFx.consecrate(world, p);
			case FxIds.PALADIN_HAMMER -> PaladinClientFx.hammer(world, p);
			case FxIds.NECRO_SPEAR -> NecromancerClientFx.spear(world, p);
			case FxIds.NECRO_SIPHON -> NecromancerClientFx.siphon(world, p);
			case FxIds.NECRO_SIPHON_LINK -> NecromancerClientFx.link(world, p);
			case FxIds.DRUID_WOLVES -> DruidClientFx.howl(world, p);
			case FxIds.DRUID_WOLF_SPAWN -> DruidClientFx.wolf(world, p);
			case FxIds.DRUID_THORNS -> DruidClientFx.thorns(world, p);
			case FxIds.MERCHANT_COIN -> MerchantClientFx.coin(world, p);
			case FxIds.MERCHANT_GUARD -> MerchantClientFx.guard(world, p);
			case FxIds.MONK_KICK -> MonkClientFx.kick(world, p);
			case FxIds.MONK_FIST -> MonkClientFx.fist(world, p);
			case FxIds.GUNSLINGER_SHOT -> GunslingerClientFx.shot(world, p);
			case FxIds.GUNSLINGER_MARK -> GunslingerClientFx.mark(world, p);
			case FxIds.WEAPON_BLADE -> WeaponClientFx.blade(world, p);
			case FxIds.WEAPON_GREATSWORD -> WeaponClientFx.greatsword(world, p);
			case FxIds.WEAPON_PRISON -> WeaponClientFx.prison(world, p);
			case FxIds.SHAPE_POUNCE -> ShapeshifterClientFx.pounce(world, p);
			case FxIds.SHAPE_HOWL -> ShapeshifterClientFx.howl(world, p);
			case FxIds.HUNTER_MARK -> HunterClientFx.mark(world, p);
			case FxIds.HUNTER_TRAP -> HunterClientFx.trap(world, p);
			case FxIds.HUNTER_ARROW -> HunterClientFx.arrow(world, p);
			case FxIds.HUNTER_FALCON -> HunterClientFx.falcon(world, p);
			case FxIds.BARD_SONG -> BardClientFx.song(world, p);
			case FxIds.BARD_DISSONANCE -> BardClientFx.dissonance(world, p);
			case FxIds.WARLOCK_CURSE -> WarlockClientFx.curse(world, p);
			case FxIds.WARLOCK_PULSE -> WarlockClientFx.pulse(world, p);
			case FxIds.WARLOCK_BOLT -> WarlockClientFx.bolt(world, p);
			case FxIds.WARLOCK_IMPS -> WarlockClientFx.imps(world, p);
			case FxIds.WARLOCK_DOOM -> WarlockClientFx.doom(world, p);
			case FxIds.TAMER_COMMAND -> TamerClientFx.command(world, p);
			case FxIds.TAMER_PET -> TamerClientFx.pet(world, p);
			case FxIds.TAMER_STAMPEDE -> TamerClientFx.stampede(world, p);
			case FxIds.ELEMENT_BOLT -> ElementClientFx.bolt(world, p);
			case FxIds.ELEMENT_ARC -> ElementClientFx.arc(world, p);
			case FxIds.ELEMENT_BURST -> ElementClientFx.burst(world, p);
			default -> { }
		}
	}

	// ------------------------------------------------------------------ Calidad

	public static float intensity() {
		return ClientConfig.get().fxIntensity;
	}

	/** Factor del ajuste de partículas del juego: todas, reducidas o mínimas. */
	public static float particleFactor() {
		ParticlesMode mode = MinecraftClient.getInstance().options.getParticles().getValue();
		return mode == ParticlesMode.ALL ? 1f : mode == ParticlesMode.DECREASED ? 0.5f : 0.2f;
	}

	/** Cantidad de partículas tras aplicar intensidad y ajuste de partículas. */
	public static int n(int base) {
		return FxMath.scaledCount(base, intensity(), particleFactor());
	}

	// ------------------------------------------------------------------ Partículas

	public static Particle spawn(ParticleEffect effect, double x, double y, double z, double vx, double vy, double vz) {
		return MinecraftClient.getInstance().particleManager.addParticle(effect, x, y, z, vx, vy, vz);
	}

	public static void chunk(ClientWorld world, BlockState state, double x, double y, double z, double vx, double vy, double vz,
							 float scale, int life, float gravity, float bounce) {
		MinecraftClient.getInstance().particleManager.addParticle(
				new ChunkParticle(world, x, y, z, vx, vy, vz, state, scale, life, gravity, bounce));
	}

	/** Bloque del suelo bajo el punto (hasta 3 bloques); piedra si no hay nada con textura. */
	public static BlockState groundState(ClientWorld world, double x, double y, double z) {
		BlockPos pos = BlockPos.ofFloored(x, y - 0.3, z);
		for (int i = 0; i < 3; i++) {
			BlockState state = world.getBlockState(pos.down(i));
			if (!state.isAir() && state.getFluidState().isEmpty()) return state;
		}
		return Blocks.STONE.getDefaultState();
	}

	// ------------------------------------------------------------------ Entidades y distancias

	/** Posición actual de la entidad ligada al evento, o la del paquete si ya no existe. */
	public static Vec3d entityPos(ClientWorld world, FxEventPayload p) {
		Entity e = p.entityId() >= 0 ? world.getEntityById(p.entityId()) : null;
		return e != null ? e.getPos() : new Vec3d(p.x(), p.y(), p.z());
	}

	public static Entity entity(ClientWorld world, int id) {
		return id >= 0 ? world.getEntityById(id) : null;
	}

	/** Posición aproximada de la mano (derecha o izquierda) de una entidad, en el mundo: delante del ojo, al lado y algo por debajo. */
	public static Vec3d handPos(Entity entity, boolean right) {
		Vec3d forward = entity.getRotationVec(1.0f);
		Vec3d side = forward.crossProduct(new Vec3d(0, 1, 0));
		side = side.lengthSquared() < 1e-6 ? new Vec3d(-1, 0, 0) : side.normalize();
		return entity.getEyePos().add(forward.multiply(0.55)).add(side.multiply(right ? 0.32 : -0.32)).add(0, -0.30, 0);
	}

	public static boolean isLocal(int entityId) {
		var player = MinecraftClient.getInstance().player;
		return player != null && player.getId() == entityId;
	}

	public static double distanceTo(Vec3d pos) {
		var player = MinecraftClient.getInstance().player;
		return player == null ? Double.MAX_VALUE : player.getPos().distanceTo(pos);
	}

	/** Destello de pantalla escalado por la intensidad. */
	public static void flash(int rgb, float strength) {
		float s = strength * intensity();
		if (s > 0.01f) ClientFx.onScreenFx(new ScreenFxPayload(ScreenFxPayload.FLASH, rgb, Math.min(1f, s)));
	}
}
