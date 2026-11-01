package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleRules;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor → cliente: estado completo de roles. Incluye las reglas para que la
 * GUI calcule puntos y XP con los valores del servidor.
 *
 * @param slotCooldowns ticks restantes de enfriamiento de cada ranura (0-3)
 * @param roleChangeCooldown ticks restantes hasta poder cambiar un rol ocupado
 */
public record RoleSyncPayload(int primary, int secondary, int[] levels, int[] xp, Map<String, Integer> ranks,
							  RoleRules rules, int[] slotCooldowns, int roleChangeCooldown,
							  int[] elements, int activeElement,
							  int[] tiers, int[] trials, int bossMask, int[] loadout) implements CustomPayload {

	public static final Id<RoleSyncPayload> ID = new Id<>(Aptitudes.id("role_sync"));
	public static final PacketCodec<PacketByteBuf, RoleSyncPayload> CODEC =
			PacketCodec.of(RoleSyncPayload::write, RoleSyncPayload::read);
	private static final int MAX_ENTRIES = 512;

	public static RoleSyncPayload of(RoleData data, RoleRules rules, int[] slotCooldowns, int roleChangeCooldown) {
		Role[] roles = Role.values();
		int[] levels = new int[roles.length];
		int[] xp = new int[roles.length];
		for (Role r : roles) {
			levels[r.ordinal()] = data.level(r);
			xp[r.ordinal()] = data.xp(r);
		}
		return new RoleSyncPayload(
				data.primary() == null ? -1 : data.primary().ordinal(),
				data.secondary() == null ? -1 : data.secondary().ordinal(),
				levels, xp, new HashMap<>(data.ranks()), rules, slotCooldowns, roleChangeCooldown,
				data.elements().stream().mapToInt(Element::ordinal).toArray(), data.activeElementIndex(),
				perRole(data::tier), perRole(data::trialProgress), bossMask(data),
				perRole(r -> data.equipped(r, 0) | (data.equipped(r, 1) << 2)));
	}

	private static int[] perRole(java.util.function.ToIntFunction<Role> f) {
		Role[] roles = Role.values();
		int[] out = new int[roles.length];
		for (Role r : roles) out[r.ordinal()] = f.applyAsInt(r);
		return out;
	}

	private static int bossMask(RoleData data) {
		int mask = 0;
		for (Role r : Role.values()) if (data.hasSlainBoss(r)) mask |= 1 << r.ordinal();
		return mask;
	}

	public int tier(Role r) { return r.ordinal() < tiers.length ? tiers[r.ordinal()] : 0; }
	public int trial(Role r) { return r.ordinal() < trials.length ? trials[r.ordinal()] : 0; }
	public boolean bossSlain(Role r) { return (bossMask & (1 << r.ordinal())) != 0; }

	public List<Element> elementList() {
		List<Element> out = new ArrayList<>();
		for (int index : elements) {
			Element e = Element.byIndex(index);
			if (e != null) out.add(e);
		}
		return out;
	}

	@Nullable
	public Element activeElementValue() {
		List<Element> list = elementList();
		return list.isEmpty() ? null : list.get(Math.floorMod(activeElement, list.size()));
	}

	@Nullable public Role primaryRole() { return Role.byIndex(primary); }
	@Nullable public Role secondaryRole() { return Role.byIndex(secondary); }
	public int level(Role r) { return r.ordinal() < levels.length ? levels[r.ordinal()] : 0; }
	public int xp(Role r) { return r.ordinal() < xp.length ? xp[r.ordinal()] : 0; }
	public int rank(String nodeId) { return ranks.getOrDefault(nodeId, 0); }

	/** Índice (0-3) de la activa equipada en la ranura {@code which} (0/1) del rol. */
	public int equipped(Role r, int which) {
		int packed = r.ordinal() < loadout.length ? loadout[r.ordinal()] : (which == 0 ? 0 : 4);
		return which == 0 ? packed & 3 : (packed >> 2) & 3;
	}

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(primary);
		buf.writeVarInt(secondary);
		writeArray(buf, levels);
		writeArray(buf, xp);
		buf.writeVarInt(ranks.size());
		ranks.forEach((k, v) -> {
			buf.writeString(k, 64);
			buf.writeVarInt(v);
		});
		buf.writeVarInt(rules.maxLevel());
		buf.writeDouble(rules.xpBase());
		buf.writeDouble(rules.xpExponent());
		buf.writeVarInt(rules.pointsEveryLevels());
		writeArray(buf, slotCooldowns);
		buf.writeVarInt(roleChangeCooldown);
		writeArray(buf, elements);
		buf.writeVarInt(activeElement);
		writeArray(buf, tiers);
		writeArray(buf, trials);
		buf.writeVarInt(bossMask);
		writeArray(buf, loadout);
	}

	private static RoleSyncPayload read(PacketByteBuf buf) {
		int primary = buf.readVarInt();
		int secondary = buf.readVarInt();
		int[] levels = readArray(buf);
		int[] xp = readArray(buf);
		int count = buf.readVarInt();
		if (count < 0 || count > MAX_ENTRIES) throw new IllegalArgumentException("Demasiados rangos: " + count);
		Map<String, Integer> ranks = new HashMap<>();
		for (int i = 0; i < count; i++) ranks.put(buf.readString(64), buf.readVarInt());
		RoleRules rules = new RoleRules(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readVarInt());
		int[] cooldowns = readArray(buf);
		int roleChange = buf.readVarInt();
		int[] elements = readArray(buf);
		int activeElement = buf.readVarInt();
		int[] tiers = readArray(buf);
		int[] trials = readArray(buf);
		int bossMask = buf.readVarInt();
		int[] loadout = readArray(buf);
		return new RoleSyncPayload(primary, secondary, levels, xp, ranks, rules, cooldowns, roleChange,
				elements, activeElement, tiers, trials, bossMask, loadout);
	}

	private static void writeArray(PacketByteBuf buf, int[] values) {
		buf.writeVarInt(values.length);
		for (int v : values) buf.writeVarInt(v);
	}

	private static int[] readArray(PacketByteBuf buf) {
		int length = buf.readVarInt();
		if (length < 0 || length > 64) throw new IllegalArgumentException("Longitud inválida: " + length);
		int[] values = new int[length];
		for (int i = 0; i < length; i++) values[i] = buf.readVarInt();
		return values;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
