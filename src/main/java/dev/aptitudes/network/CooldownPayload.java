package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente: la ranura {@code slot} queda en enfriamiento {@code ticks} ticks. */
public record CooldownPayload(int slot, int ticks) implements CustomPayload {
	public static final Id<CooldownPayload> ID = new Id<>(Aptitudes.id("cooldown"));
	public static final PacketCodec<RegistryByteBuf, CooldownPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, CooldownPayload::slot,
			PacketCodecs.VAR_INT, CooldownPayload::ticks,
			CooldownPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
