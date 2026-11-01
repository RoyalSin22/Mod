package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente: XP ganada en una habilidad por uso (notificación junto al HUD). */
public record SkillXpPayload(int skill, int amount, int xp, int needed, int level) implements CustomPayload {
	public static final Id<SkillXpPayload> ID = new Id<>(Aptitudes.id("skill_xp"));
	public static final PacketCodec<RegistryByteBuf, SkillXpPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, SkillXpPayload::skill,
			PacketCodecs.VAR_INT, SkillXpPayload::amount,
			PacketCodecs.VAR_INT, SkillXpPayload::xp,
			PacketCodecs.VAR_INT, SkillXpPayload::needed,
			PacketCodecs.VAR_INT, SkillXpPayload::level,
			SkillXpPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
