package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente → servidor: usar la habilidad de la ranura 0-3 (0-1 principal, 2-3 secundario). */
public record UseAbilityPayload(int slot) implements CustomPayload {
	public static final Id<UseAbilityPayload> ID = new Id<>(Aptitudes.id("use_ability"));
	public static final PacketCodec<RegistryByteBuf, UseAbilityPayload> CODEC =
			PacketCodec.tuple(PacketCodecs.VAR_INT, UseAbilityPayload::slot, UseAbilityPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
