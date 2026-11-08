package dev.aptitudes.forge;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.block.ModBlocks;
import dev.aptitudes.forge.ForgeRules.Profile;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.network.ForgeActionPayload;
import dev.aptitudes.network.ForgePreviewPayload;
import dev.aptitudes.network.ForgePreviewPayload.Preview;
import dev.aptitudes.network.ForgePreviewPayload.Status;
import dev.aptitudes.souls.SoulsManager;
import dev.aptitudes.world.GuildVillagers;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
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

import java.util.concurrent.atomic.AtomicLong;

/** Inventario privado de una sesión. La confirmación modifica el arma en su propia ranura. */
public final class ForgeScreenHandler extends ScreenHandler {
	public static final int WIDTH = 340;
	public static final int HEIGHT = 238;
	public enum Mode { REFORGE, TEMPER }

	public static final ScreenHandlerType<ForgeScreenHandler> REFORGE = register("reforge", Mode.REFORGE);
	public static final ScreenHandlerType<ForgeScreenHandler> TEMPER = register("temper", Mode.TEMPER);
	private static final int WEAPON = 0;
	private static final int TROPHY = 1;
	private static final int SHARDS = 2;
	private static final int INPUTS = 3;
	private static final AtomicLong REVISIONS = new AtomicLong();

	private final SimpleInventory inputs = new SimpleInventory(INPUTS);
	private final PlayerEntity owner;
	private final Mode mode;
	private final ScreenHandlerContext context;
	private Profile selected = Profile.RESERVE;
	private Preview lastPreview;
	private Quote quote;
	private long lastRevision;
	private boolean closed;

	private record Quote(long revision, ItemStack weapon, ItemStack trophy, ItemStack shards) {
		boolean matches(SimpleInventory inventory) {
			return ItemStack.areEqual(weapon, inventory.getStack(WEAPON))
					&& ItemStack.areEqual(trophy, inventory.getStack(TROPHY))
					&& ItemStack.areEqual(shards, inventory.getStack(SHARDS));
		}
	}

	private static ScreenHandlerType<ForgeScreenHandler> register(String key, Mode mode) {
		return Registry.register(Registries.SCREEN_HANDLER, Aptitudes.id(key),
				new ScreenHandlerType<>((id, inv) -> new ForgeScreenHandler(id, inv, mode, ScreenHandlerContext.EMPTY),
						FeatureFlags.VANILLA_FEATURES));
	}

	public static void init() {
	}

	private ForgeScreenHandler(int id, PlayerInventory inventory, Mode mode, ScreenHandlerContext context) {
		super(mode == Mode.REFORGE ? REFORGE : TEMPER, id);
		this.mode = mode;
		this.context = context;
		this.owner = inventory.player;
		inputs.addListener(inv -> quote = null);
		addSlot(new Slot(inputs, WEAPON, 20, 37) {
			@Override public boolean canInsert(ItemStack stack) { return ForgeWeapons.isWeapon(stack); }
			@Override public int getMaxItemCount() { return 1; }
		});
		addSlot(ingredient(TROPHY, 126, trophyItem()));
		addSlot(ingredient(SHARDS, 232, ModItems.SOUL_SHARD));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, 9 + row * 9 + col, 90 + col * 18, 164 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 90 + col * 18, 220));
	}

	private Slot ingredient(int index, int x, Item item) {
		return new Slot(inputs, index, x, 37) {
			@Override public boolean canInsert(ItemStack stack) { return stack.isOf(item); }
		};
	}

	public Mode mode() {
		return mode;
	}

	private Item trophyItem() {
		return mode == Mode.REFORGE ? ModItems.ELITE_ESSENCE : ModItems.CHAMPION_HEART;
	}

	private static boolean available(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Mode mode) {
		if (!player.isAlive() || player.isCreative() || player.isSpectator()
				|| player.getWorld() != world || !world.isChunkLoaded(pos)) return false;
		if (mode == Mode.TEMPER) {
			return world.getBlockState(pos).isOf(ModBlocks.BONFIRE) && SoulsManager.canTemperAt(player, pos);
		}
		return world.getBlockState(pos).isOf(GuildVillagers.GUILD_DESK)
				&& player.canInteractWithBlockAt(pos, 4.0);
	}

	public static boolean open(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Mode mode) {
		if (!available(player, world, pos, mode) || !ServerPlayNetworking.canSend(player, ForgePreviewPayload.ID)) return false;
		ScreenHandlerContext context = ScreenHandlerContext.create(world, pos);
		Text title = Text.translatable(mode == Mode.REFORGE ? "forge.aptitudes.reforge" : "forge.aptitudes.temper");
		var opened = player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
				(id, inv, p) -> new ForgeScreenHandler(id, inv, mode, context), title));
		if (opened.isEmpty()) return false;
		if (player.currentScreenHandler instanceof ForgeScreenHandler handler) handler.sendContentUpdates();
		return true;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		if (closed || player != owner) return false;
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;
		return context.get((world, pos) -> world instanceof ServerWorld serverWorld
				&& available(serverPlayer, serverWorld, pos, mode), false);
	}

	public void handle(ServerPlayerEntity player, ForgeActionPayload action) {
		if (player.currentScreenHandler != this || action.syncId() != syncId || !canUse(player)) return;
		switch (action.action()) {
			case ForgeActionPayload.SELECT_RESERVE -> select(Profile.RESERVE);
			case ForgeActionPayload.SELECT_IMPETUS -> select(Profile.IMPETUS);
			case ForgeActionPayload.CONFIRM -> confirm(player, action.revision());
			default -> { }
		}
	}

	private void select(Profile profile) {
		if (mode != Mode.REFORGE || profile == selected) return;
		selected = profile;
		quote = null;
		sendContentUpdates();
	}

	private Preview preview(ServerPlayerEntity player) {
		ItemStack weapon = inputs.getStack(WEAPON);
		var current = ForgeWeapons.read(weapon);
		if (weapon.getCount() != 1 || !ForgeWeapons.isWeapon(weapon)) return blocked(null, Status.NO_WEAPON);
		if (!ForgeWeapons.canUpgrade(player, weapon)) return blocked(current, Status.WRONG_ROLE);
		var upgrade = mode == Mode.REFORGE ? ForgeRules.reforge(current, selected) : ForgeRules.temper(current);
		if (upgrade == null) {
			Status status = mode == Mode.REFORGE ? Status.SAME_AFFIX : current == null ? Status.NEEDS_REFORGE : Status.MAX_RANK;
			return blocked(current, status);
		}
		ItemStack trophies = inputs.getStack(TROPHY);
		ItemStack shards = inputs.getStack(SHARDS);
		boolean hasMaterials = trophies.isOf(trophyItem()) && trophies.getCount() >= upgrade.trophies()
				&& shards.isOf(ModItems.SOUL_SHARD) && shards.getCount() >= upgrade.shards();
		return new Preview(selected, current, upgrade.result(), upgrade.trophies(), upgrade.shards(),
				hasMaterials ? Status.READY : Status.MISSING);
	}

	private Preview blocked(ForgeRules.Affix current, Status status) {
		return new Preview(selected, current, null, 0, 0, status);
	}

	@Override
	public void sendContentUpdates() {
		super.sendContentUpdates();
		if (!(owner instanceof ServerPlayerEntity player) || player.currentScreenHandler != this || !canUse(player)) return;
		Preview current = preview(player);
		if (current.status() != Status.READY) {
			quote = null;
		} else if (!current.equals(lastPreview) || quote == null || !quote.matches(inputs)) {
			quote = new Quote(REVISIONS.incrementAndGet(), inputs.getStack(WEAPON).copy(),
					inputs.getStack(TROPHY).copy(), inputs.getStack(SHARDS).copy());
		}
		long revision = quote == null ? 0 : quote.revision();
		if (current.equals(lastPreview) && revision == lastRevision) return;
		lastPreview = current;
		lastRevision = revision;
		ServerPlayNetworking.send(player, new ForgePreviewPayload(syncId, revision, current));
	}

	private void confirm(ServerPlayerEntity player, long revision) {
		Preview current = preview(player);
		if (quote == null || quote.revision() != revision || !quote.matches(inputs)
				|| !current.equals(lastPreview) || current.status() != Status.READY || current.result() == null) {
			sendContentUpdates();
			return;
		}
		ItemStack weapon = ForgeWeapons.upgraded(inputs.getStack(WEAPON), current.result());
		ItemStack trophies = inputs.getStack(TROPHY).copy();
		ItemStack shards = inputs.getStack(SHARDS).copy();
		trophies.decrement(current.trophies());
		shards.decrement(current.shards());
		quote = null;
		inputs.setStack(WEAPON, weapon);
		inputs.setStack(TROPHY, trophies.isEmpty() ? ItemStack.EMPTY : trophies);
		inputs.setStack(SHARDS, shards.isEmpty() ? ItemStack.EMPTY : shards);
		sendContentUpdates();
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
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
		super.onClosed(player);
		context.run((world, pos) -> dropInventory(player, inputs));
	}
}
