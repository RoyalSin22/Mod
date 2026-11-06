package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.tutorial.TutorialView;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Solo servidor → cliente: estado personal, sin canales para reclamar ni marcar pasos. */
public record TutorialPayload(TutorialView view) implements CustomPayload {
	public static final Id<TutorialPayload> ID = new Id<>(Aptitudes.id("tutorial_view"));
	public static final PacketCodec<PacketByteBuf, TutorialPayload> CODEC = PacketCodec.of(TutorialPayload::write,
			buf -> new TutorialPayload(new TutorialView(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean())));

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(view.nextStep());
		buf.writeVarInt(view.completed());
		buf.writeVarInt(view.rewarded());
		buf.writeBoolean(view.skipped());
	}

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
