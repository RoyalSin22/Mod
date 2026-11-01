package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Cliente → servidor: en la Hoguera, asignar estos puntos por atributo. Los que
 * superen los puntos libres se compran como niveles de alma pagando XP vanilla.
 */
public record LevelUpPayload(int[] allocations) implements CustomPayload {
	public static final Id<LevelUpPayload> ID = new Id<>(Aptitudes.id("level_up"));
	public static final PacketCodec<PacketByteBuf, LevelUpPayload> CODEC = PacketCodec.of(LevelUpPayload::write, LevelUpPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(allocations.length);
		for (int a : allocations) buf.writeVarInt(a);
	}

	private static LevelUpPayload read(PacketByteBuf buf) {
		int n = buf.readVarInt();
		if (n < 0 || n > 32) throw new IllegalArgumentException("Longitud inválida: " + n);
		int[] values = new int[n];
		for (int i = 0; i < n; i++) values[i] = buf.readVarInt();
		return new LevelUpPayload(values);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
