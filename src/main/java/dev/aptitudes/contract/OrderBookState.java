package dev.aptitudes.contract;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Pedidos entre jugadores guardados por mundo (F4). El estado completo vive aquí; la lógica está en {@link OrderBook}. */
public final class OrderBookState extends PersistentState {
	private static final String NAME = "aptitudes_orders";
	private static final Type<OrderBookState> TYPE = new Type<>(OrderBookState::new, OrderBookState::fromNbt, null);
	private static final int MAX_LOADED = 10_000;

	/** Pedido abierto o cerrado a la espera de que el dueño recoja. Campos mutables: los cambia {@link OrderBook}. */
	public static final class Order {
		public final long id;
		public final UUID owner;
		/** Identificador del objeto pedido (p. ej. «minecraft:iron_ingot»). */
		public final String item;
		public final int count;
		public final int tier;
		/** Unidades entregadas hasta ahora. */
		public int delivered;
		/** Unidades ya recogidas por el dueño. */
		public int claimed;
		/** Esmeraldas que quedan en reserva para las entregas futuras. */
		public long escrow;
		/** Ya no admite entregas: completado o cancelado. */
		public boolean closed;
		/** Jugadores que han entregado. */
		public final Set<UUID> participants = new LinkedHashSet<>();

		public Order(long id, UUID owner, String item, int count, int tier) {
			this.id = id;
			this.owner = owner;
			this.item = item;
			this.count = count;
			this.tier = tier;
		}
	}

	private final Map<Long, Order> orders = new LinkedHashMap<>();
	private long nextId = 1;

	public static OrderBookState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	public Order get(long id) {
		return orders.get(id);
	}

	public Iterable<Order> all() {
		return orders.values();
	}

	public long nextId() {
		return nextId++;
	}

	public void add(Order order) {
		orders.put(order.id, order);
		markDirty();
	}

	public void remove(long id) {
		orders.remove(id);
		markDirty();
	}

	/** Pedidos que aún no están cerrados y pertenecen a este dueño. */
	public int openBy(UUID owner) {
		int open = 0;
		for (Order o : orders.values()) if (o.owner.equals(owner) && !o.closed) open++;
		return open;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (Order o : orders.values()) {
			NbtCompound entry = new NbtCompound();
			entry.putLong("id", o.id);
			entry.putUuid("owner", o.owner);
			entry.putString("item", o.item);
			entry.putInt("count", o.count);
			entry.putInt("tier", o.tier);
			entry.putInt("delivered", o.delivered);
			entry.putInt("claimed", o.claimed);
			entry.putLong("escrow", o.escrow);
			entry.putBoolean("closed", o.closed);
			NbtList participants = new NbtList();
			for (UUID p : o.participants) participants.add(NbtString.of(p.toString()));
			entry.put("participants", participants);
			list.add(entry);
		}
		nbt.put("orders", list);
		nbt.putLong("next_id", nextId);
		return nbt;
	}

	private static OrderBookState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		OrderBookState state = new OrderBookState();
		NbtList list = nbt.getList("orders", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < Math.min(list.size(), MAX_LOADED); i++) {
			NbtCompound entry = list.getCompound(i);
			if (!entry.containsUuid("owner") || !entry.contains("item")) continue;
			long id = entry.getLong("id");
			if (id <= 0 || state.orders.containsKey(id)) continue;
			int count = Math.max(1, Math.min(OrderBookRules.MAX_COUNT, entry.getInt("count")));
			int tier = Math.max(1, Math.min(4, entry.getInt("tier")));
			Order o = new Order(id, entry.getUuid("owner"), entry.getString("item"), count, tier);
			o.delivered = Math.max(0, Math.min(count, entry.getInt("delivered")));
			o.claimed = Math.max(0, Math.min(o.delivered, entry.getInt("claimed")));
			o.escrow = Math.max(0, entry.getLong("escrow"));
			o.closed = entry.getBoolean("closed");
			NbtList participants = entry.getList("participants", NbtElement.STRING_TYPE);
			for (int j = 0; j < Math.min(participants.size(), 5); j++) {
				try {
					o.participants.add(UUID.fromString(participants.getString(j)));
				} catch (IllegalArgumentException ignored) {
					// Entrada corrupta: se omite.
				}
			}
			state.orders.put(id, o);
		}
		state.nextId = Math.max(1, nbt.getLong("next_id"));
		for (Long id : state.orders.keySet()) state.nextId = Math.max(state.nextId, id + 1);
		return state;
	}
}
