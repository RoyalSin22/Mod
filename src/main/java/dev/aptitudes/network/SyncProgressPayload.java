package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.ProgressRules;
import dev.aptitudes.data.Skill;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente: estado completo del jugador + reglas relevantes.
 * Se envía el estado completo (≈ 30 enteros) en vez de deltas: es más simple,
 * idempotente y el coste de red es despreciable.
 */
public record SyncProgressPayload(int[] levels, int[] xp, int[] allocated,
								  int availablePoints, ProgressRules rules, int soulLevel,
								  int costBase, int costDivisor, int costMax, int soulLevelsPerPoint,
								  int[] affinityPercent, double[] skillPerLevel) implements CustomPayload {

	public static final Id<SyncProgressPayload> ID = new Id<>(Aptitudes.id("sync_progress"));
	public static final PacketCodec<PacketByteBuf, SyncProgressPayload> CODEC =
			PacketCodec.of(SyncProgressPayload::write, SyncProgressPayload::read);

	public static SyncProgressPayload of(PlayerProgress progress, int availablePoints, ProgressRules rules) {
		return of(progress, availablePoints, rules, new int[PlayerAttribute.values().length]);
	}

	/** @param affinityPercent bonificación de afinidad de rol por atributo (50 = +50 %). */
	public static SyncProgressPayload of(PlayerProgress progress, int availablePoints, ProgressRules rules,
										 int[] affinityPercent) {
		Skill[] skills = Skill.values();
		int[] levels = new int[skills.length];
		int[] xp = new int[skills.length];
		for (Skill s : skills) {
			levels[s.ordinal()] = progress.getLevel(s);
			xp[s.ordinal()] = progress.getXp(s);
		}
		PlayerAttribute[] attrs = PlayerAttribute.values();
		int[] allocated = new int[attrs.length];
		for (PlayerAttribute a : attrs) {
			allocated[a.ordinal()] = progress.getAllocated(a);
		}
		var c = dev.aptitudes.Aptitudes.config();
		return new SyncProgressPayload(levels, xp, allocated, availablePoints, rules, progress.getSoulLevel(),
				c.levelCostBase, c.levelCostDivisor, c.levelCostMax, c.soulLevelsPerPoint, affinityPercent,
				skillPerLevel(c));
	}

	/** Valores por nivel de las bonificaciones de habilidad, en orden (habilidad, bonificación). */
	private static double[] skillPerLevel(dev.aptitudes.config.AptitudesConfig c) {
		java.util.List<Double> values = new java.util.ArrayList<>();
		for (Skill s : Skill.values()) for (Skill.SkillBonus b : s.bonuses()) values.add(b.perLevel().applyAsDouble(c));
		return values.stream().mapToDouble(Double::doubleValue).toArray();
	}

	/** Total actual de la bonificación {@code index} de {@code skill} (nivel × valor por nivel). */
	public double skillBonus(Skill skill, int index) {
		int offset = 0;
		for (Skill s : Skill.values()) {
			if (s == skill) break;
			offset += s.bonuses().size();
		}
		int i = offset + index;
		return i < skillPerLevel.length ? level(skill) * skillPerLevel[i] : 0;
	}

	/** Niveles de alma a comprar para ganar {@code points} puntos (mismo cálculo que el servidor). */
	public int levelsForPoints(int points) {
		if (points <= 0) return 0;
		int per = Math.max(1, soulLevelsPerPoint);
		return per * (soulLevel / per + points) - soulLevel;
	}

	public int affinity(PlayerAttribute attribute) {
		return attribute.ordinal() < affinityPercent.length ? affinityPercent[attribute.ordinal()] : 0;
	}

	/** Niveles de XP vanilla que cuesta pasar del nivel de alma {@code level} al siguiente. */
	public int levelCost(int level) {
		return Math.min(costMax, costBase + level / Math.max(1, costDivisor));
	}

	public int level(Skill skill) {
		return skill.ordinal() < levels.length ? levels[skill.ordinal()] : 0;
	}

	public int xp(Skill skill) {
		return skill.ordinal() < xp.length ? xp[skill.ordinal()] : 0;
	}

	public int allocated(PlayerAttribute attribute) {
		return attribute.ordinal() < allocated.length ? allocated[attribute.ordinal()] : 0;
	}

	private void write(PacketByteBuf buf) {
		writeArray(buf, levels);
		writeArray(buf, xp);
		writeArray(buf, allocated);
		buf.writeVarInt(availablePoints);
		buf.writeVarInt(rules.maxLevel());
		buf.writeDouble(rules.xpBase());
		buf.writeDouble(rules.xpExponent());
		buf.writeVarInt(rules.maxPointsPerAttribute());
		buf.writeDouble(rules.breakSpeedPerLevel());
		buf.writeVarInt(soulLevel);
		buf.writeVarInt(costBase);
		buf.writeVarInt(costDivisor);
		buf.writeVarInt(costMax);
		buf.writeVarInt(soulLevelsPerPoint);
		writeArray(buf, affinityPercent);
		buf.writeVarInt(skillPerLevel.length);
		for (double d : skillPerLevel) buf.writeDouble(d);
	}

	private static SyncProgressPayload read(PacketByteBuf buf) {
		int[] levels = readArray(buf);
		int[] xp = readArray(buf);
		int[] allocated = readArray(buf);
		int available = buf.readVarInt();
		ProgressRules rules = new ProgressRules(buf.readVarInt(), buf.readDouble(), buf.readDouble(),
				buf.readVarInt(), buf.readDouble());
		return new SyncProgressPayload(levels, xp, allocated, available, rules,
				buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), readArray(buf),
				readDoubles(buf));
	}

	private static double[] readDoubles(PacketByteBuf buf) {
		int n = buf.readVarInt();
		if (n < 0 || n > 64) throw new IllegalArgumentException("Longitud inválida: " + n);
		double[] out = new double[n];
		for (int i = 0; i < n; i++) out[i] = buf.readDouble();
		return out;
	}

	private static void writeArray(PacketByteBuf buf, int[] values) {
		buf.writeVarInt(values.length);
		for (int v : values) buf.writeVarInt(v);
	}

	private static int[] readArray(PacketByteBuf buf) {
		int length = buf.readVarInt();
		if (length < 0 || length > 64) {
			throw new IllegalArgumentException("Longitud de array inválida: " + length);
		}
		int[] values = new int[length];
		for (int i = 0; i < length; i++) values[i] = buf.readVarInt();
		return values;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
