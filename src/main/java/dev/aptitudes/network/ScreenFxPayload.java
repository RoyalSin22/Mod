package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente: efecto de pantalla. {@code type}: 0 = destello de color,
 * 1 = sacudida de cámara. {@code strength} entre 0 y 1.
 */
public record ScreenFxPayload(int type, int color, float strength) implements CustomPayload {
	public static final int FLASH = 0;
	public static final int SHAKE = 1;

	public static final Id<ScreenFxPayload> ID = new Id<>(Aptitudes.id("screen_fx"));
	public static final PacketCodec<RegistryByteBuf, ScreenFxPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, ScreenFxPayload::type,
			PacketCodecs.INTEGER, ScreenFxPayload::color,
			PacketCodecs.FLOAT, ScreenFxPayload::strength,
			ScreenFxPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
