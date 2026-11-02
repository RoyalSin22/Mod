package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente (v40): configuración del servidor en JSON (para la pantalla de configuración y para que los
 * tooltips muestren los valores reales) y si el jugador puede editarla (permiso 2).
 */
public record ConfigSyncPayload(String json, boolean canEdit) implements CustomPayload {
	/** Tope del JSON: la configuración completa ocupa unos pocos KB. */
	public static final int MAX_JSON = 65_536;
	public static final Id<ConfigSyncPayload> ID = new Id<>(Aptitudes.id("config_sync"));
	public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.string(MAX_JSON), ConfigSyncPayload::json,
			PacketCodecs.BOOL, ConfigSyncPayload::canEdit,
			ConfigSyncPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
