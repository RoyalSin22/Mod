package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.Skill;
import dev.aptitudes.logic.MasteryRules;
import dev.aptitudes.role.Role;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Datos de maestría personales y reglas para el cliente, sin peticiones de progreso C2S. */
public record MasteryPayload(long[] skills, long[] roles, int base, int step, double cap, boolean enabled) implements CustomPayload {
	public static final Id<MasteryPayload> ID = new Id<>(Aptitudes.id("mastery"));
	public static final PacketCodec<PacketByteBuf, MasteryPayload> CODEC = PacketCodec.of(MasteryPayload::write, MasteryPayload::read);
	public MasteryPayload {
		skills = safe(skills, Skill.values().length);
		roles = safe(roles, Role.values().length);
		base = Math.max(1, Math.min(1_000_000, base));
		step = Math.max(0, Math.min(1_000_000, step));
		cap = Double.isFinite(cap) ? Math.max(0, Math.min(0.25, cap)) : 0;
	}
	private static long[] safe(long[] values, int size) {
		long[] out = new long[size];
		for (int i = 0; i < Math.min(size, values.length); i++) out[i] = MasteryRules.sanitize(values[i]);
		return out;
	}
	public int points(Skill skill) { return MasteryRules.points(skills[skill.ordinal()], base, step); }
	public int points(Role role) { return MasteryRules.points(roles[role.ordinal()], base, step); }
	public double bonus(Skill skill) { return enabled ? MasteryRules.bonus(points(skill), cap) : 0; }
	private void write(PacketByteBuf buf) {
		buf.writeVarInt(skills.length); for (long xp : skills) buf.writeVarLong(xp);
		buf.writeVarInt(roles.length); for (long xp : roles) buf.writeVarLong(xp);
		buf.writeVarInt(base); buf.writeVarInt(step); buf.writeDouble(cap); buf.writeBoolean(enabled);
	}
	private static MasteryPayload read(PacketByteBuf buf) {
		long[] skills = readArray(buf), roles = readArray(buf);
		return new MasteryPayload(skills, roles, buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readBoolean());
	}
	private static long[] readArray(PacketByteBuf buf) {
		int size = buf.readVarInt();
		if (size < 0 || size > 64) throw new IllegalArgumentException("Longitud de maestría inválida");
		long[] out = new long[size]; for (int i = 0; i < size; i++) out[i] = buf.readVarLong();
		return out;
	}
	@Override public Id<? extends CustomPayload> getId() { return ID; }
}
