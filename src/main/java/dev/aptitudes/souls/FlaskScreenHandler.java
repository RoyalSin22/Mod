package dev.aptitudes.souls;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.block.ModBlocks;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.network.FlaskQuotePayload;
import dev.aptitudes.network.ForgeActionPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/** Ranuras de trabajo privadas. Una confirmación válida actualiza el mismo frasco. */
public final class FlaskScreenHandler extends ScreenHandler {
	public static final int WIDTH = 300;
	public static final int HEIGHT = 226;
	public static final int FLASK = 0;
	public static final int CORE = 1;
	public static final int SHARDS = 2;
	private static final int INPUTS = 3;

	public static final ScreenHandlerType<FlaskScreenHandler> TYPE = Registry.register(
			Registries.SCREEN_HANDLER, Aptitudes.id("flask_upgrade"),
			new ScreenHandlerType<>((id, inv) -> new FlaskScreenHandler(id, inv, ScreenHandlerContext.EMPTY),
					FeatureFlags.VANILLA_FEATURES));
	private static final AtomicLong REVISIONS = new AtomicLong();

	private final SimpleInventory inputs = new SimpleInventory(INPUTS);
	private final PlayerEntity owner;
	private final ScreenHandlerContext context;
	private Quote quote;
	private FlaskQuotePayload sent;
	private boolean closed;

	private record Quote(long revision, ItemStack flask, ItemStack core, ItemStack shards) {
		boolean matches(SimpleInventory inputs) {
			return ItemStack.areEqual(flask, inputs.getStack(FLASK)) && ItemStack.areEqual(core, inputs.getStack(CORE))
					&& ItemStack.areEqual(shards, inputs.getStack(SHARDS));
		}
	}

	public static void init() {
		// El inventario de trabajo se devuelve antes de guardar al jugador o apagar el servidor.
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				server.execute(() -> closeForSave(handler.getPlayer())));
		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
				server.getPlayerManager().getPlayerList().forEach(FlaskScreenHandler::closeForSave));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity player && player.currentScreenHandler instanceof FlaskScreenHandler) {
				player.closeHandledScreen();
			}
		});
	}

	private static void closeForSave(ServerPlayerEntity player) {
		if (player.currentScreenHandler instanceof FlaskScreenHandler) player.onHandledScreenClosed();
	}

	private FlaskScreenHandler(int id, PlayerInventory inventory, ScreenHandlerContext context) {
		super(TYPE, id);
		this.owner = inventory.player;
		this.context = context;
		inputs.addListener(inv -> quote = null);
		addSlot(input(FLASK, 16, 1, FlaskUpgrades::isFlask));
		addSlot(input(CORE, 108, 64, FlaskUpgrades::isCore));
		addSlot(input(SHARDS, 200, 64, stack -> stack.isOf(ModItems.SOUL_SHARD)));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, 9 + row * 9 + col, 70 + col * 18, 150 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 70 + col * 18, 206));
	}

	private Slot input(int index, int x, int maxCount, Predicate<ItemStack> accepts) {
		return new Slot(inputs, index, x, 33) {
			@Override public boolean canInsert(ItemStack stack) { return accepts.test(stack); }
			@Override public int getMaxItemCount() { return maxCount; }
		};
	}

	private static boolean available(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
		return player.isAlive() && !player.isCreative() && !player.isSpectator() && player.getWorld() == world
				&& world.isChunkLoaded(pos) && world.getBlockState(pos).isOf(ModBlocks.BONFIRE)
				&& player.canInteractWithBlockAt(pos, 0) && SoulsManager.canTemperAt(player, pos);
	}

	static boolean open(ServerPlayerEntity player, BlockPos pos) {
		var world = player.getServerWorld();
		if (!available(player, world, pos) || !ServerPlayNetworking.canSend(player, FlaskQuotePayload.ID)) return false;
		var context = ScreenHandlerContext.create(world, pos);
		var opened = player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
				(id, inv, p) -> new FlaskScreenHandler(id, inv, context), Text.translatable("flask.aptitudes.title")));
		if (opened.isEmpty()) return false;
		if (player.currentScreenHandler instanceof FlaskScreenHandler handler) handler.sendContentUpdates();
		return true;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		if (closed || player != owner) return false;
		return !(player instanceof ServerPlayerEntity serverPlayer) || context.get((world, pos) ->
				world instanceof ServerWorld serverWorld && available(serverPlayer, serverWorld, pos), false);
	}

	private FlaskRules.Upgrade offer(int limit) {
		ItemStack flask = inputs.getStack(FLASK);
		if (!FlaskUpgrades.isFlask(flask) || flask.getCount() != 1) return null;
		var upgrade = FlaskRules.next(FlaskUpgrades.rank(flask), limit);
		ItemStack shards = inputs.getStack(SHARDS);
		return upgrade != null && FlaskUpgrades.isCore(inputs.getStack(CORE))
				&& shards.isOf(ModItems.SOUL_SHARD) && shards.getCount() >= upgrade.shards() ? upgrade : null;
	}

	@Override
	public void sendContentUpdates() {
		super.sendContentUpdates();
		if (!(owner instanceof ServerPlayerEntity player) || player.currentScreenHandler != this || !canUse(player)) return;
		int limit = Aptitudes.config().flaskUpgradeMaxRank;
		if (offer(limit) == null) {
			quote = null;
		} else if (quote == null || !quote.matches(inputs)) {
			quote = new Quote(REVISIONS.incrementAndGet(), inputs.getStack(FLASK).copy(),
					inputs.getStack(CORE).copy(), inputs.getStack(SHARDS).copy());
		}
		var next = new FlaskQuotePayload(syncId, quote == null ? 0 : quote.revision(), limit);
		if (next.equals(sent)) return;
		sent = next;
		ServerPlayNetworking.send(player, next);
	}

	public void handle(ServerPlayerEntity player, ForgeActionPayload request) {
		if (request.action() != ForgeActionPayload.CONFIRM_FLASKS || request.syncId() != syncId
				|| player.currentScreenHandler != this || !canUse(player)) return;
		var upgrade = offer(Aptitudes.config().flaskUpgradeMaxRank);
		if (upgrade == null || quote == null || quote.revision() != request.revision() || !quote.matches(inputs)) {
			sendContentUpdates();
			return;
		}

		ItemStack result = FlaskUpgrades.upgraded(inputs.getStack(FLASK), upgrade);
		ItemStack core = inputs.getStack(CORE).copy();
		ItemStack shards = inputs.getStack(SHARDS).copy();
		core.decrement(1);
		shards.decrement(upgrade.shards());
		quote = null;
		inputs.setStack(FLASK, result);
		inputs.setStack(CORE, core.isEmpty() ? ItemStack.EMPTY : core);
		inputs.setStack(SHARDS, shards.isEmpty() ? ItemStack.EMPTY : shards);
		sendContentUpdates();
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		if (index < 0 || index >= slots.size() || !canUse(player)) return ItemStack.EMPTY;
		Slot slot = slots.get(index);
		ItemStack stack = slot.getStack();
		if (stack.isEmpty()) return ItemStack.EMPTY;
		ItemStack original = stack.copy();
		boolean moved = index < INPUTS ? insertItem(stack, INPUTS, slots.size(), true) : insertItem(stack, 0, INPUTS, false);
		if (!moved) return ItemStack.EMPTY;
		if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
		else slot.markDirty();
		slot.onTakeItem(player, stack);
		return original;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		if (closed) return;
		closed = true;
		quote = null;
		context.run((world, pos) -> {
			if (!player.isAlive() && player instanceof ServerPlayerEntity serverPlayer && !serverPlayer.isDisconnected()
					&& world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
				player.getInventory().offerOrDrop(getCursorStack());
				setCursorStack(ItemStack.EMPTY);
				for (int slot = 0; slot < INPUTS; slot++) player.getInventory().offerOrDrop(inputs.removeStack(slot));
			} else {
				dropInventory(player, inputs);
			}
		});
		super.onClosed(player);
	}
}
