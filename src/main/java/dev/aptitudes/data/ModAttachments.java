package dev.aptitudes.data;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.morph.MorphData;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.souls.SoulsData;
import dev.aptitudes.unique.UniqueProgress;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * Persistencia mediante la Data Attachment API de Fabric: los datos se guardan
 * dentro del NBT del jugador (playerdata/&lt;uuid&gt;.dat), sin archivos propios,
 * y {@code copyOnDeath} los conserva al morir.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ModAttachments {
	public static final AttachmentType<PlayerProgress> PROGRESS = AttachmentRegistry.<PlayerProgress>builder()
			.persistent(PlayerProgress.CODEC)
			.copyOnDeath()
			.initializer(PlayerProgress::new)
			.buildAndRegister(Aptitudes.id("progress"));

	public static final AttachmentType<RoleData> ROLES = AttachmentRegistry.<RoleData>builder()
			.persistent(RoleData.CODEC)
			.copyOnDeath()
			.initializer(RoleData::new)
			.buildAndRegister(Aptitudes.id("roles"));

	public static final AttachmentType<MorphData> MORPH = AttachmentRegistry.<MorphData>builder()
			.persistent(MorphData.CODEC)
			.copyOnDeath()
			.initializer(MorphData::new)
			.buildAndRegister(Aptitudes.id("morph"));

	public static final AttachmentType<UniqueProgress> UNIQUE_PROGRESS = AttachmentRegistry.<UniqueProgress>builder()
			.persistent(UniqueProgress.CODEC)
			.copyOnDeath()
			.initializer(UniqueProgress::new)
			.buildAndRegister(Aptitudes.id("unique_progress"));

	public static final AttachmentType<SoulsData> SOULS = AttachmentRegistry.<SoulsData>builder()
			.persistent(SoulsData.CODEC)
			.copyOnDeath()
			.initializer(SoulsData::new)
			.buildAndRegister(Aptitudes.id("souls"));

	/**
	 * Tiempos de espera de habilidades (v18): instante de juego en que vuelven a estar listas. Persisten
	 * al desconectarse, al reiniciar el servidor y al morir (antes se perdían y salir/entrar los reiniciaba).
	 */
	public static final AttachmentType<dev.aptitudes.ability.CooldownBook> COOLDOWNS =
			AttachmentRegistry.<dev.aptitudes.ability.CooldownBook>builder()
					.persistent(com.mojang.serialization.Codec.unboundedMap(com.mojang.serialization.Codec.STRING,
									com.mojang.serialization.Codec.LONG)
							.xmap(dev.aptitudes.ability.CooldownBook::new, dev.aptitudes.ability.CooldownBook::raw))
					.copyOnDeath()
					.initializer(dev.aptitudes.ability.CooldownBook::new)
					.buildAndRegister(Aptitudes.id("cooldowns"));

	public static final AttachmentType<dev.aptitudes.contract.ContractData> CONTRACTS =
			AttachmentRegistry.<dev.aptitudes.contract.ContractData>builder()
				.persistent(dev.aptitudes.contract.ContractData.CODEC).copyOnDeath()
				.initializer(dev.aptitudes.contract.ContractData::new)
				.buildAndRegister(Aptitudes.id("contracts"));

	public static final AttachmentType<dev.aptitudes.tutorial.TutorialData> TUTORIAL =
			AttachmentRegistry.<dev.aptitudes.tutorial.TutorialData>builder()
				.persistent(dev.aptitudes.tutorial.TutorialData.CODEC).copyOnDeath()
				.initializer(dev.aptitudes.tutorial.TutorialData::new)
				.buildAndRegister(Aptitudes.id("tutorial"));

	public static final AttachmentType<dev.aptitudes.alchemy.AlchemyXpData> ALCHEMY_XP =
			AttachmentRegistry.<dev.aptitudes.alchemy.AlchemyXpData>builder()
					.persistent(dev.aptitudes.alchemy.AlchemyXpData.CODEC).copyOnDeath()
					.initializer(() -> new dev.aptitudes.alchemy.AlchemyXpData(0, 0))
					.buildAndRegister(Aptitudes.id("alchemy_xp"));

	/** Último Herbolista que abrió realmente el soporte, guardado en la entidad de bloque. */
	public static final AttachmentType<String> HERBAL_BREWER = AttachmentRegistry.<String>builder()
			.persistent(com.mojang.serialization.Codec.STRING)
			.buildAndRegister(Aptitudes.id("herbal_brewer"));

	/** Solo criaturas: instantánea persistente del escalado y del cobro de botín, sin copiar al morir. */
	public static final AttachmentType<ThreatData> THREAT = AttachmentRegistry.<ThreatData>builder()
			.persistent(ThreatData.CODEC)
			.buildAndRegister(Aptitudes.id("threat"));

	/** v61: bestiario del grimorio (id de entrada → criaturas vencidas); se conserva al morir. */
	public static final AttachmentType<java.util.Map<String, Integer>> BESTIARY =
			AttachmentRegistry.<java.util.Map<String, Integer>>builder()
					.persistent(com.mojang.serialization.Codec.unboundedMap(com.mojang.serialization.Codec.STRING,
									com.mojang.serialization.Codec.INT)
							.xmap(dev.aptitudes.guide.BestiaryRules::sanitize, java.util.Map::copyOf))
					.copyOnDeath()
					.initializer(java.util.TreeMap::new)
					.buildAndRegister(Aptitudes.id("bestiary"));

	private ModAttachments() {
	}

	/** Fuerza la carga de la clase (y por tanto el registro) durante la inicialización. */
	public static void init() {
	}
}
