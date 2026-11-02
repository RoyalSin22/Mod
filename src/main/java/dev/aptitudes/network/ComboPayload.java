package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente (v28): combo actual del jugador y si el último golpe fue un remate. */
public record ComboPayload(int count, boolean finisher) implements CustomPayload {
	public static final Id<ComboPayload> ID = new Id<>(Aptitudes.id("combo"));
	public static final PacketCodec<RegistryByteBuf, ComboPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, ComboPayload::count,
			PacketCodecs.BOOL, ComboPayload::finisher,
			ComboPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
