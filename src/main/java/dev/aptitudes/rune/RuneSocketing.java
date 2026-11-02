package dev.aptitudes.rune;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.weapon.RoleBladeItem;
import dev.aptitudes.item.weapon.RoleFocusItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Escritura autoritativa de las ranuras de runa persistentes en cada ItemStack y efectos al golpear.
 *
 * <p>v35: valores configurables, tres runas nuevas (Escarcha, Trueno, Guardia) y extracción de la última runa
 * en la Mesa del gremio (Mayús + clic derecho, la otra mano vacía; cuesta esmeraldas y la runa se pierde).
 * Las esperas de las runas viven en memoria por jugador: escribirlas en el arma en cada golpe hacía que el
 * objeto «se re-equipara» en la mano.</p>
 */
public final class RuneSocketing {
	private static final String DATA_KEY = "aptitudes_runes";
	private static final Identifier FORCE_ID = Aptitudes.id("rune_force");
	private static final Identifier SWIFT_ID = Aptitudes.id("rune_swift");
	/** Última activación de cada runa con espera, por jugador (tiempo del Mundo Normal). */
	private static final Map<UUID, EnumMap<RuneKind, Long>> LAST_USE = new HashMap<>();
	/** Evita que el daño de la descarga del Trueno vuelva a activar las runas (recursión). */
	private static boolean chaining;

	private RuneSocketing() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (chaining || blocked || damageTaken <= 0 || !(source.getAttacker() instanceof ServerPlayerEntity player)
					|| source.getSource() != player || entity == player) return;
			ItemStack weapon = player.getMainHandStack();
			if (!eligible(weapon)) return;
			List<String> ids = runes(weapon);
			if (ids.isEmpty()) return;
			onHit(player, entity, ids);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_USE.remove(handler.getPlayer().getUuid()));
		// Mayús + clic derecho en la Mesa del gremio: con objetos en la mano el juego no llama a onUse del bloque
		// al agacharse, así que se intercepta antes. Mismo criterio en cliente y servidor (el cliente no dispara
		// el arma); el coste y la extracción los decide solo el servidor.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (hand != Hand.MAIN_HAND || !player.isSneaking() || !player.getOffHandStack().isEmpty()) return ActionResult.PASS;
			if (!(world.getBlockState(hit.getBlockPos()).getBlock() instanceof dev.aptitudes.world.GuildDeskBlock)) return ActionResult.PASS;
			ItemStack weapon = player.getMainHandStack();
			if (!eligible(weapon) || runes(weapon).isEmpty()) return ActionResult.PASS;
			if (player instanceof ServerPlayerEntity serverPlayer) removeLast(serverPlayer, weapon);
			return ActionResult.SUCCESS;
		});
	}

	private static void onHit(ServerPlayerEntity player, LivingEntity target, List<String> ids) {
		AptitudesConfig cfg = Aptitudes.config();
		long now = player.getServer().getOverworld().getTime();
		ServerWorld world = player.getServerWorld();
		if (ids.contains(RuneKind.FLAME.id()) && cfg.runeFlameSeconds > 0) target.setOnFireFor(cfg.runeFlameSeconds);
		if (ids.contains(RuneKind.LEECH.id()) && cfg.runeLeechAmount > 0
				&& tryUse(player, RuneKind.LEECH, now, RuneRules.secondsToTicks(cfg.runeLeechCooldownSeconds, 0.5, 60))) {
			player.heal((float) cfg.runeLeechAmount);
		}
		if (ids.contains(RuneKind.FROST.id()) && cfg.runeFrostSeconds > 0) {
			target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, cfg.runeFrostSeconds * 20, cfg.runeFrostLevel));
			world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.6), target.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
		}
		if (ids.contains(RuneKind.GUARD.id()) && cfg.runeGuardAbsorption > 0
				&& tryUse(player, RuneKind.GUARD, now, RuneRules.secondsToTicks(cfg.runeGuardCooldownSeconds, 0.5, 60))) {
			player.setAbsorptionAmount(RuneRules.absorptionAfter(player.getAbsorptionAmount(),
					(float) cfg.runeGuardAbsorption, (float) cfg.runeGuardMaxAbsorption));
			world.spawnParticles(ParticleTypes.WAX_ON, player.getX(), player.getBodyY(0.6), player.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
		}
		if (ids.contains(RuneKind.THUNDER.id()) && RuneRules.roll(cfg.runeThunderChance, world.random.nextDouble())
				&& tryUse(player, RuneKind.THUNDER, now, RuneRules.secondsToTicks(cfg.runeThunderCooldownSeconds, 0.5, 60))) {
			thunder(player, target, world, cfg);
		}
	}

	/** Descarga en cadena: salta del objetivo a los monstruos más cercanos (4 bloques), nunca a jugadores ni invocaciones. */
	private static void thunder(ServerPlayerEntity player, LivingEntity origin, ServerWorld world, AptitudesConfig cfg) {
		int max = RuneRules.thunderTargets(cfg.runeThunderTargets);
		float damage = (float) cfg.runeThunderDamage;
		world.playSound(null, origin.getX(), origin.getY(), origin.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
				SoundCategory.PLAYERS, 0.6f, 1.6f);
		world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, origin.getX(), origin.getBodyY(0.6), origin.getZ(), 14, 0.3, 0.4, 0.3, 0.1);
		if (max <= 0 || damage <= 0) return;
		List<LivingEntity> targets = new ArrayList<>(world.getEntitiesByClass(LivingEntity.class,
				origin.getBoundingBox().expand(4.0), e -> e != origin && e != player && e.isAlive() && e instanceof Monster
						&& !(e instanceof PlayerEntity) && !dev.aptitudes.ability.AbilityState.isSummon(e)));
		targets.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(origin)));
		chaining = true;
		try {
			Vec3d from = origin.getPos().add(0, origin.getHeight() * 0.6, 0);
			for (int i = 0; i < Math.min(max, targets.size()); i++) {
				LivingEntity next = targets.get(i);
				Vec3d to = next.getPos().add(0, next.getHeight() * 0.6, 0);
				for (int s = 1; s <= 6; s++) {
					Vec3d p = from.lerp(to, s / 6.0);
					world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0);
				}
				next.damage(player.getDamageSources().indirectMagic(player, player), damage);
				from = to;
			}
		} finally {
			chaining = false;
		}
	}

	private static boolean tryUse(ServerPlayerEntity player, RuneKind kind, long now, long cooldownTicks) {
		EnumMap<RuneKind, Long> map = LAST_USE.computeIfAbsent(player.getUuid(), k -> new EnumMap<>(RuneKind.class));
		if (!RuneRules.ready(now, map.get(kind), cooldownTicks)) return false;
		map.put(kind, now);
		return true;
	}

	public static boolean eligible(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (stack.getItem() instanceof RoleBladeItem || stack.getItem() instanceof RoleFocusItem) return true;
		return stack.isOf(ModItems.BERSERKER_AXE) || stack.isOf(ModItems.DAGGER) || stack.isOf(ModItems.KATANA)
				|| stack.isOf(ModItems.SPEAR) || stack.isOf(ModItems.ARCANE_STAFF) || stack.isOf(ModItems.PLASMA_GUN)
				|| stack.isOf(ModItems.HOLY_HAMMER) || stack.isOf(ModItems.NECRO_TOME) || stack.isOf(ModItems.OAK_STAFF)
				|| stack.isOf(ModItems.HAND_WRAPS) || stack.isOf(ModItems.ARCANE_REVOLVER) || stack.isOf(ModItems.WEAPON_SIGIL)
				|| stack.isOf(ModItems.HUNTING_KNIFE) || stack.isOf(ModItems.LUTE) || stack.isOf(ModItems.CURSED_TALISMAN);
	}

	public static List<String> runes(ItemStack stack) {
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (data == null) return List.of();
		NbtCompound root = data.copyNbt();
		if (!root.contains(DATA_KEY, NbtCompound.COMPOUND_TYPE)) return List.of();
		NbtCompound stored = root.getCompound(DATA_KEY);
		List<String> ids = new ArrayList<>(RuneRules.SOCKETS);
		for (int i = 0; i < RuneRules.SOCKETS; i++) {
			String id = stored.getString("slot_" + i);
			if (RuneKind.byId(id) != null && !ids.contains(id)) ids.add(id);
		}
		return List.copyOf(ids);
	}

	/** Guarda las runas compactadas en slot_0, slot_1… (sin huecos ni entradas inválidas). */
	private static void store(ItemStack weapon, List<String> ids) {
		NbtCompound root = weapon.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		NbtCompound stored = new NbtCompound();
		for (int i = 0; i < ids.size() && i < RuneRules.SOCKETS; i++) stored.putString("slot_" + i, ids.get(i));
		root.put(DATA_KEY, stored);
		weapon.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}

	public static boolean socket(ServerPlayerEntity player, ItemStack weapon, ItemStack runeStack) {
		if (!(runeStack.getItem() instanceof RuneItem rune) || !eligible(weapon)) return false;
		List<String> current = runes(weapon);
		if (!RuneRules.canSocket(current, rune.kind().id())) {
			player.sendMessage(Text.translatable(current.size() >= RuneRules.SOCKETS ? "aptitudes.rune.full" : "aptitudes.rune.duplicate").formatted(Formatting.RED), true);
			return true;
		}
		List<String> updated = new ArrayList<>(current);
		updated.add(rune.kind().id());
		store(weapon, updated);
		applyAttributeRune(weapon, rune.kind());
		if (!player.isCreative()) runeStack.decrement(1);
		player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), dev.aptitudes.sound.ModSounds.RUNE_SOCKET,
				SoundCategory.PLAYERS, 0.9f, 1.0f);
		player.sendMessage(Text.translatable("aptitudes.rune.socketed", Text.translatable("item.aptitudes.rune." + rune.kind().id()), updated.size(), RuneRules.SOCKETS).formatted(Formatting.GOLD), true);
		return true;
	}

	/** v35: quita la última runa engarzada a cambio de esmeraldas; la runa se pierde. */
	public static void removeLast(ServerPlayerEntity player, ItemStack weapon) {
		List<String> current = runes(weapon);
		if (current.isEmpty()) {
			player.sendMessage(Text.translatable("aptitudes.rune.none").formatted(Formatting.RED), true);
			return;
		}
		int cost = RuneRules.removalCost(Aptitudes.config().runeRemovalEmeraldCost, player.isCreative());
		if (countEmeralds(player) < cost) {
			player.sendMessage(Text.translatable("aptitudes.rune.remove_cost", cost).formatted(Formatting.RED), true);
			return;
		}
		takeEmeralds(player, cost);
		String removed = current.get(current.size() - 1);
		store(weapon, RuneRules.afterRemoval(current));
		RuneKind kind = RuneKind.byId(removed);
		if (kind == RuneKind.FORCE) removeAttribute(weapon, FORCE_ID);
		else if (kind == RuneKind.SWIFT) removeAttribute(weapon, SWIFT_ID);
		player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), dev.aptitudes.sound.ModSounds.RUNE_REMOVE,
				SoundCategory.PLAYERS, 0.8f, 1.0f);
		player.sendMessage(Text.translatable("aptitudes.rune.removed", Text.translatable("item.aptitudes.rune." + removed), cost)
				.formatted(Formatting.GOLD), true);
	}

	private static int countEmeralds(ServerPlayerEntity player) {
		int total = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack s = player.getInventory().getStack(i);
			if (s.isOf(Items.EMERALD)) total += s.getCount();
		}
		return total;
	}

	private static void takeEmeralds(ServerPlayerEntity player, int amount) {
		int left = amount;
		for (int i = 0; i < player.getInventory().size() && left > 0; i++) {
			ItemStack s = player.getInventory().getStack(i);
			if (!s.isOf(Items.EMERALD)) continue;
			int take = Math.min(left, s.getCount());
			s.decrement(take);
			left -= take;
		}
		player.getInventory().markDirty();
	}

	private static void applyAttributeRune(ItemStack stack, RuneKind rune) {
		AptitudesConfig cfg = Aptitudes.config();
		AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
		if (rune == RuneKind.FORCE) {
			modifiers = modifiers.with(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(FORCE_ID, cfg.runeForceDamage, EntityAttributeModifier.Operation.ADD_VALUE), net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
		} else if (rune == RuneKind.SWIFT) {
			modifiers = modifiers.with(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(SWIFT_ID, cfg.runeSwiftAttackSpeed, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE), net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
		} else {
			return;
		}
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers);
	}

	/** Quita del arma el modificador de atributo de una runa (al extraerla). */
	private static void removeAttribute(ItemStack stack, Identifier id) {
		AttributeModifiersComponent modifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
		if (modifiers == null) return;
		List<AttributeModifiersComponent.Entry> kept = new ArrayList<>();
		for (AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
			if (!entry.modifier().id().equals(id)) kept.add(entry);
		}
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(kept, modifiers.showInTooltip()));
	}

	/** Valores que rellenan el texto de cada runa (en el mismo orden que los %s de su clave de idioma). */
	private static Object[] descArgs(RuneKind kind, AptitudesConfig c) {
		if (kind == null) return new Object[0];
		return switch (kind) {
			case FLAME -> new Object[] {c.runeFlameSeconds};
			case LEECH -> new Object[] {num(c.runeLeechAmount), num(c.runeLeechCooldownSeconds)};
			case SWIFT -> new Object[] {pct(c.runeSwiftAttackSpeed), c.runeSwiftFocusTicks};
			case FORCE -> new Object[] {num(c.runeForceDamage)};
			case ECLIPSE -> new Object[] {pct(c.runeEclipseBonus)};
			case FROST -> new Object[] {roman(c.runeFrostLevel + 1), c.runeFrostSeconds};
			case THUNDER -> new Object[] {pct(c.runeThunderChance), RuneRules.thunderTargets(c.runeThunderTargets),
					num(c.runeThunderDamage), num(c.runeThunderCooldownSeconds)};
			case GUARD -> new Object[] {num(c.runeGuardAbsorption), num(c.runeGuardMaxAbsorption), num(c.runeGuardCooldownSeconds)};
		};
	}

	private static String num(double v) {
		return dev.aptitudes.config.ConfigFieldRules.format(Math.round(v * 100.0) / 100.0);
	}

	private static String pct(double v) {
		return Long.toString(Math.round(v * 100.0));
	}

	private static String roman(int n) {
		return switch (n) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> Integer.toString(n); };
	}

	public static boolean has(ItemStack stack, RuneKind kind) { return runes(stack).contains(kind.id()); }

	/** Bonificación de la Runa del Eclipse contra élites, campeones y jefes (multiplicador aditivo). */
	public static float eclipseBonus(ServerPlayerEntity player, LivingEntity target) {
		if (!dev.aptitudes.elite.Elites.isElite(target) || !has(player.getMainHandStack(), RuneKind.ECLIPSE)) return 0f;
		return (float) Aptitudes.config().runeEclipseBonus;
	}

	/** Daño extra de la Runa de Fuerza para los focos (en armas cuerpo a cuerpo va en el modificador del arma). */
	public static float damageBonus(ItemStack stack) { return has(stack, RuneKind.FORCE) ? (float) Aptitudes.config().runeForceDamage : 0.0f; }

	/** Ticks menos de espera de los focos con la Runa de Aceleración. */
	public static int focusCooldownReduction(ItemStack stack) { return has(stack, RuneKind.SWIFT) ? Aptitudes.config().runeSwiftFocusTicks : 0; }

	/**
	 * Tooltip de las runas de un arma. v40: los números salen de {@code cfg}; en el cliente se pasa la configuración
	 * sincronizada del servidor, así que el texto muestra los valores reales y no los de por defecto.
	 */
	public static void appendTooltip(ItemStack stack, List<Text> tooltip, AptitudesConfig cfg) {
		List<String> ids = runes(stack);
		tooltip.add(Text.translatable("aptitudes.rune.sockets", ids.size(), RuneRules.SOCKETS).formatted(Formatting.GRAY));
		if (ids.isEmpty()) tooltip.add(Text.translatable("aptitudes.rune.hint").formatted(Formatting.DARK_GRAY));
		for (String id : ids) {
			tooltip.add(Text.translatable("item.aptitudes.rune." + id).formatted(Formatting.AQUA));
			tooltip.add(Text.translatable("item.aptitudes.rune." + id + ".desc", descArgs(RuneKind.byId(id), cfg)).formatted(Formatting.GRAY));
		}
		if (!ids.isEmpty()) tooltip.add(Text.translatable("aptitudes.rune.remove_hint").formatted(Formatting.DARK_GRAY));
	}
}
