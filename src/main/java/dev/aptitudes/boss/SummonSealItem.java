package dev.aptitudes.boss;

import dev.aptitudes.world.BossArenas;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 * Paso 2 de la reinvocación (fase C del plan): Sello de invocación de un jefe animado. Al usarlo, si no hay otra
 * instancia viva y se cumple la espera de {@link SummonRules}, cobra {@link #SOUL_COUNT} Fragmentos de alma y el
 * material propio del jefe, y lo invoca junto al jugador con la fase en la que cayó.
 */
public final class SummonSealItem extends Item {
	/** Fragmentos de alma que cuesta cada invocación. Provisional. */
	public static final int SOUL_COUNT = 4;

	private final String bossName;
	private final Item secondary;

	/**
	 * @param bossName  nombre del tipo de entidad sin espacio de nombres (p. ej. {@code faceless_colossus})
	 * @param secondary material propio del jefe (1 unidad)
	 */
	public SummonSealItem(String bossName, Item secondary, Settings settings) {
		super(settings);
		this.bossName = bossName;
		this.secondary = secondary;
	}

	/** Descripción breve: qué invoca, qué cuesta y cuándo vuelve a estar disponible. */
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes." + bossName + "_seal.tooltip").formatted(Formatting.GRAY));
	}

	/** Id del jefe en el registro de estado ({@link SummonState}). */
	public String bossId() {
		return Aptitudes.id(bossName).toString();
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!(world instanceof ServerWorld server) || !(player instanceof ServerPlayerEntity sp)) {
			return TypedActionResult.success(stack, world.isClient());
		}
		MinecraftServer ms = server.getServer();
		SummonState state = SummonState.get(ms);
		String id = bossId();
		UUID activeId = state.active(id);
		boolean alive = activeId != null && server.getEntity(activeId) instanceof LivingEntity l && l.isAlive();
		if (!SummonRules.canSummon(alive, state.lastDefeatTick(id), server.getTime())) {
			return TypedActionResult.fail(stack);
		}
		EntityType<?> type = Registries.ENTITY_TYPE.get(Aptitudes.id(bossName));
		Entity boss = type.create(server);
		if (boss == null || !hasCost(sp.getInventory())) return TypedActionResult.fail(stack);
		takeCost(sp.getInventory());
		boss.setPosition(player.getX(), player.getY(), player.getZ());
		if (boss instanceof AnimatedBossEntity animated) animated.restorePhase(state.lastPhase(id));
		if (boss instanceof FacelessColossusEntity colossus) { // v65: el Coloso aparece en sus ruinas
			BossArenas.mark(colossus, player.getBlockPos());
			BossArenas.raiseRuins(server, player.getBlockPos());
		}
		server.spawnEntity(boss);
		state.setActive(id, boss.getUuid());
		stack.decrement(1);
		return TypedActionResult.success(stack);
	}

	private boolean hasCost(PlayerInventory inv) {
		return count(inv, ModItems.SOUL_SHARD) >= SOUL_COUNT && count(inv, secondary) >= 1;
	}

	private void takeCost(PlayerInventory inv) {
		take(inv, ModItems.SOUL_SHARD, SOUL_COUNT);
		take(inv, secondary, 1);
	}

	private static int count(PlayerInventory inv, Item item) {
		int n = 0;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (s.isOf(item)) n += s.getCount();
		}
		return n;
	}

	private static void take(PlayerInventory inv, Item item, int amount) {
		int left = amount;
		for (int i = 0; i < inv.size() && left > 0; i++) {
			ItemStack s = inv.getStack(i);
			if (!s.isOf(item)) continue;
			int used = Math.min(left, s.getCount());
			s.decrement(used);
			left -= used;
		}
	}
}
