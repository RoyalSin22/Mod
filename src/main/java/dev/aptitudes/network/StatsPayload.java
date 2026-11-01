package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente: valores reales de las estadísticas base (medidos en el
 * servidor, con equipo, efectos y otros mods incluidos) y, por cada atributo
 * del mod, cuánto cambia su estadística por cada punto adicional.
 *
 * <p>Como el valor de un atributo vanilla es lineal respecto al valor de un único
 * modificador, {@code actual + puntos × pendiente} es exacto (salvo los topes).</p>
 */
public record StatsPayload(double[] stats, double[] slopes) implements CustomPayload {
	public static final Id<StatsPayload> ID = new Id<>(Aptitudes.id("stats"));
	public static final PacketCodec<PacketByteBuf, StatsPayload> CODEC = PacketCodec.of(StatsPayload::write, StatsPayload::read);

	private void write(PacketByteBuf buf) {
		writeArray(buf, stats);
		writeArray(buf, slopes);
	}

	private static StatsPayload read(PacketByteBuf buf) {
		return new StatsPayload(readArray(buf), readArray(buf));
	}

	private static void writeArray(PacketByteBuf buf, double[] values) {
		buf.writeVarInt(values.length);
		for (double v : values) buf.writeDouble(v);
	}

	private static double[] readArray(PacketByteBuf buf) {
		int n = buf.readVarInt();
		if (n < 0 || n > 32) throw new IllegalArgumentException("Longitud inválida: " + n);
		double[] out = new double[n];
		for (int i = 0; i < n; i++) out[i] = buf.readDouble();
		return out;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
