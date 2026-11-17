package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.FxIds;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente (V77): un efecto de batalla. El servidor solo manda QUÉ pasa y DÓNDE (un paquete, no cientos de
 * partículas); cada cliente genera las partículas, la cámara y la pantalla con su propio ajuste de calidad.
 *
 * @param id       {@link FxIds}
 * @param entityId entidad de origen o seguida (−1 si no hay)
 * @param dx,dy,dz dirección (normalizada) o vector auxiliar según el efecto
 * @param color    color RGB del efecto
 * @param power    intensidad/radio/duración según el efecto
 * @param seed     semilla para que todos los clientes dibujen el mismo desorden (o un índice, según el efecto)
 */
public record FxEventPayload(int id, int entityId, double x, double y, double z, float dx, float dy, float dz, int color,
							 float power, int seed) implements CustomPayload {
	public static final Id<FxEventPayload> ID = new Id<>(Aptitudes.id("fx_event"));
	public static final PacketCodec<PacketByteBuf, FxEventPayload> CODEC = PacketCodec.of(FxEventPayload::write, FxEventPayload::read);
	private static final float MAX_POWER = 512f;

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(id);
		buf.writeInt(entityId);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeFloat(dx);
		buf.writeFloat(dy);
		buf.writeFloat(dz);
		buf.writeInt(color);
		buf.writeFloat(power);
		buf.writeInt(seed);
	}

	private static FxEventPayload read(PacketByteBuf buf) {
		int id = buf.readVarInt();
		int entityId = buf.readInt();
		double x = finite(buf.readDouble());
		double y = finite(buf.readDouble());
		double z = finite(buf.readDouble());
		float dx = (float) finite(buf.readFloat());
		float dy = (float) finite(buf.readFloat());
		float dz = (float) finite(buf.readFloat());
		int color = buf.readInt() & 0xFFFFFF;
		float power = (float) Math.max(0, Math.min(MAX_POWER, finite(buf.readFloat())));
		int seed = buf.readInt();
		return new FxEventPayload(FxIds.valid(id) ? id : 0, entityId, x, y, z, dx, dy, dz, color, power, seed);
	}

	/** Un valor no finito (corrupto o malicioso) se descarta como 0. */
	private static double finite(double v) {
		return Double.isFinite(v) ? v : 0.0;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
