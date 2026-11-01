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

	private ModAttachments() {
	}

	/** Fuerza la carga de la clase (y por tanto el registro) durante la inicialización. */
	public static void init() {
	}
}
