package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Cliente → servidor (v40): petición de cambiar un campo de la configuración. El servidor comprueba el permiso,
 * interpreta el valor, lo valida con {@code AptitudesConfig.validated} y responde con la configuración final.
 */
public record ConfigEditPayload(String field, String value) implements CustomPayload {
	public static final Id<ConfigEditPayload> ID = new Id<>(Aptitudes.id("config_edit"));
	public static final PacketCodec<RegistryByteBuf, ConfigEditPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.string(64), ConfigEditPayload::field,
			PacketCodecs.string(128), ConfigEditPayload::value,
			ConfigEditPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
