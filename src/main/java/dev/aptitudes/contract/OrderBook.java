package dev.aptitudes.contract;

import dev.aptitudes.contract.OrderBookState.Order;
import dev.aptitudes.network.OrderActionPayload;
import dev.aptitudes.network.OrderViewPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Acciones de servidor de los pedidos entre jugadores (F4). Toda validación ocurre aquí; el cliente solo pide.
 *
 * <p>Flujo: el dueño publica con el objeto de la mano y paga su precio por adelantado. Otros jugadores entregan el objeto
 * y cobran al instante, descontando la comisión. El dueño recoge lo entregado cuando el pedido se completa o se cancela.
 * Al cancelar, recibe de vuelta las esmeraldas que no se usaron.</p>
 */
public final class OrderBook {
	private OrderBook() {}

	/** Publica un pedido con el objeto de la mano principal. */
	public static void publish(ServerPlayerEntity owner, int tier) {
		ItemStack hand = owner.getMainHandStack();
		if (hand.isEmpty()) {
			fail(owner, "order.aptitudes.no_item");
			return;
		}
		OrderBookState state = state(owner);
		int count = Math.min(hand.getCount(), cfg().orderMaxCount);
		int open = state.openBy(owner.getUuid());
		int required = OrderRules.requiredParticipants(tier);
		if (open >= cfg().orderMaxOpen) {
			fail(owner, "order.aptitudes.limit");
			return;
		}
		if (count < required) {
			fail(owner, "order.aptitudes.too_few");
			return;
		}
		long cost = OrderBookRules.escrow(count, tier, cfg().orderUnitPriceBase);
		if (countEmeralds(owner) < cost) {
			fail(owner, "order.aptitudes.no_emeralds");
			return;
		}
		if (!OrderBookRules.canPublish(count, tier, open, cfg().orderMaxOpen, cfg().orderMaxCount)) {
			fail(owner, "order.aptitudes.cannot_publish");
			return;
		}
		removeEmeralds(owner, cost);
		String itemId = Registries.ITEM.getId(hand.getItem()).toString();
		Order order = new Order(state.nextId(), owner.getUuid(), itemId, count, tier);
		order.escrow = cost;
		state.add(order);
		owner.sendMessage(Text.translatable("order.aptitudes.published", count, hand.getName(), cost)
				.formatted(Formatting.GREEN), true);
	}

	/** Entrega unidades del pedido {@code id} (hasta {@code requested}) y cobra al instante. */
	public static void deliver(ServerPlayerEntity fulfiller, long id, int requested) {
		OrderBookState state = state(fulfiller);
		Order order = state.get(id);
		if (order == null || order.closed) {
			fail(fulfiller, "order.aptitudes.not_open");
			return;
		}
		if (order.owner.equals(fulfiller.getUuid())) {
			fail(fulfiller, "order.aptitudes.own_order");
			return;
		}
		Item item = itemOf(order);
		int remaining = order.count - order.delivered;
		boolean already = order.participants.contains(fulfiller.getUuid());
		int allowed = OrderBookRules.allowedQuantity(remaining, order.participants.size(),
				OrderRules.requiredParticipants(order.tier), already, requested);
		int have = countItem(fulfiller, item);
		int quantity = Math.min(allowed, have);
		if (quantity <= 0) {
			fail(fulfiller, have == 0 ? "order.aptitudes.missing_items" : "order.aptitudes.reserved");
			return;
		}
		removeItem(fulfiller, item, quantity);
		order.delivered += quantity;
		order.participants.add(fulfiller.getUuid());
		long gross = OrderBookRules.gross(quantity, order.tier, cfg().orderUnitPriceBase);
		order.escrow = Math.max(0, order.escrow - gross);
		long pay = OrderBookRules.net(gross);
		giveEmeralds(fulfiller, pay);
		if (order.delivered >= order.count) order.closed = true;
		state.markDirty();
		fulfiller.sendMessage(Text.translatable("order.aptitudes.delivered", quantity, pay).formatted(Formatting.GREEN), true);
	}

	/** El dueño recoge lo entregado de un pedido cerrado. Si ya no queda nada, el pedido se borra. */
	public static void claim(ServerPlayerEntity owner, long id) {
		OrderBookState state = state(owner);
		Order order = state.get(id);
		if (order == null || !order.owner.equals(owner.getUuid())) return;
		if (!order.closed) {
			fail(owner, "order.aptitudes.not_ready");
			return;
		}
		int claimable = order.delivered - order.claimed;
		if (claimable > 0) {
			giveItems(owner, itemOf(order), claimable);
			order.claimed += claimable;
		}
		if (order.claimed >= order.delivered) state.remove(id);
		else state.markDirty();
		owner.sendMessage(Text.translatable("order.aptitudes.claimed", claimable).formatted(Formatting.GREEN), true);
	}

	/** El dueño cancela un pedido abierto: recupera las esmeraldas no usadas. Lo ya entregado queda para recoger. */
	public static void cancel(ServerPlayerEntity owner, long id) {
		OrderBookState state = state(owner);
		Order order = state.get(id);
		if (order == null || !order.owner.equals(owner.getUuid()) || order.closed) return;
		long refund = order.escrow;
		order.escrow = 0;
		order.closed = true;
		giveEmeralds(owner, refund);
		if (order.claimed >= order.delivered) state.remove(id);
		else state.markDirty();
		owner.sendMessage(Text.translatable("order.aptitudes.cancelled", refund).formatted(Formatting.GRAY), true);
	}

	/** Despacha una acción del cliente y devuelve la lista actualizada. Lo único que el cliente puede pedir. */
	public static void handle(ServerPlayerEntity player, int action, long id, int tier) {
		switch (action) {
			case OrderActionPayload.PUBLISH -> publish(player, tier);
			case OrderActionPayload.DELIVER -> deliver(player, id, Integer.MAX_VALUE);
			case OrderActionPayload.CLAIM -> claim(player, id);
			case OrderActionPayload.CANCEL -> cancel(player, id);
			default -> { }
		}
		sync(player);
	}

	/** Envía al jugador los pedidos que puede ver: los abiertos y los suyos con algo pendiente de recoger. */
	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, OrderViewPayload.ID)) return;
		List<OrderViewPayload.Row> rows = new ArrayList<>();
		for (Order o : state(player).all()) {
			boolean mine = o.owner.equals(player.getUuid());
			boolean pendingForOwner = mine && o.delivered > o.claimed;
			if (o.closed && !pendingForOwner) continue;
			rows.add(new OrderViewPayload.Row(o.id, o.item, o.count, o.tier, o.delivered, o.claimed, o.closed, mine,
					o.participants.contains(player.getUuid())));
			if (rows.size() >= OrderViewPayload.MAX_ROWS) break;
		}
		ServerPlayNetworking.send(player, new OrderViewPayload(rows));
	}

	// ------------------------------------------------------------- Utilidades

	private static dev.aptitudes.config.AptitudesConfig cfg() {
		return dev.aptitudes.Aptitudes.config();
	}

	private static OrderBookState state(ServerPlayerEntity player) {
		return OrderBookState.get(player.getServer().getOverworld());
	}

	private static Item itemOf(Order order) {
		Identifier id = Identifier.tryParse(order.item);
		return id == null ? Items.AIR : Registries.ITEM.get(id);
	}

	private static void fail(ServerPlayerEntity player, String key) {
		player.sendMessage(Text.translatable(key).formatted(Formatting.RED), true);
	}

	private static int countEmeralds(ServerPlayerEntity player) {
		return countItem(player, Items.EMERALD);
	}

	private static int countItem(ServerPlayerEntity player, Item item) {
		PlayerInventory inv = player.getInventory();
		int total = 0;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if (stack.isOf(item)) total += stack.getCount();
		}
		return total;
	}

	private static void removeEmeralds(ServerPlayerEntity player, long amount) {
		removeItem(player, Items.EMERALD, (int) amount);
	}

	private static void removeItem(ServerPlayerEntity player, Item item, int amount) {
		PlayerInventory inv = player.getInventory();
		int left = amount;
		for (int i = 0; i < inv.size() && left > 0; i++) {
			ItemStack stack = inv.getStack(i);
			if (!stack.isOf(item)) continue;
			int take = Math.min(left, stack.getCount());
			stack.decrement(take);
			left -= take;
		}
		inv.markDirty();
	}

	private static void giveEmeralds(ServerPlayerEntity player, long amount) {
		giveItems(player, Items.EMERALD, (int) Math.min(Integer.MAX_VALUE, Math.max(0, amount)));
	}

	private static void giveItems(ServerPlayerEntity player, Item item, int amount) {
		// Un objeto que ya no existe en el registro no se entrega (evita dar aire).
		if (item == Items.AIR) return;
		int left = amount;
		while (left > 0) {
			int chunk = Math.min(left, new ItemStack(item).getMaxCount());
			player.getInventory().offerOrDrop(new ItemStack(item, chunk));
			left -= chunk;
		}
	}
}
