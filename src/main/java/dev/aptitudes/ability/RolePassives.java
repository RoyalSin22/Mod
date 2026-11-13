package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.List;

/**
 * Las pasivas de todos los roles, agrupadas por mecanismo:
 * <ul>
 *   <li><b>Atributos</b>: modificadores vanilla con id fijo (idempotentes).</li>
 *   <li><b>Tick</b>: efectos periódicos (regeneración, inmunidades, auras, crecimiento).</li>
 *   <li><b>Daño saliente</b>: multiplicadores y robo de vida (mixin de {@code LivingEntity#damage}).</li>
 *   <li><b>Daño entrante</b>: parada, esquiva y contraataque (cancelan el golpe).</li>
 *   <li><b>Eventos</b>: muertes cercanas (Comunión oscura) y comercio (Mercader).</li>
 *   <li><b>Energía/enfriamiento</b>: en {@link AbilityManager} (afinidad arcana, batería, mente clara).</li>
 * </ul>
 */
public final class RolePassives {
	private record AttributeBonus(String node, RegistryEntry<EntityAttribute> attribute, Operation operation,
								  double perRank) {
	}

	private static final List<AttributeBonus> ATTRIBUTE_BONUSES = List.of(
			new AttributeBonus("tough_skin", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, 0.1),
			new AttributeBonus("tough_skin", EntityAttributes.GENERIC_ARMOR_TOUGHNESS, Operation.ADD_VALUE, 1.0),
			new AttributeBonus("shadow", EntityAttributes.PLAYER_SNEAKING_SPEED, Operation.ADD_VALUE, 0.1),
			// Ingeniero (fusión): Ingeniería absorbe Blindaje; Batería absorbe Exoesqueleto.
			new AttributeBonus("engineering", EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, Operation.ADD_VALUE, 1.0),
			new AttributeBonus("engineering", EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.1),
			new AttributeBonus("engineering", EntityAttributes.GENERIC_ARMOR, Operation.ADD_VALUE, 1.0),
			new AttributeBonus("battery", EntityAttributes.GENERIC_JUMP_STRENGTH, Operation.ADD_VALUE, 0.04),
			new AttributeBonus("battery", EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, Operation.ADD_VALUE, 2.0),
			new AttributeBonus("reach", EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, Operation.ADD_VALUE, 0.5));

	private RolePassives() {
	}

	// ------------------------------------------------------------ Atributos

	public static void applyAttributes(ServerPlayerEntity player) {
		for (AttributeBonus bonus : ATTRIBUTE_BONUSES) {
			int rank = RoleManager.activeRank(player, bonus.node());
			String path = "node/" + bonus.node() + "/" + bonus.attribute().getKey()
					.map(k -> k.getValue().getPath()).orElse("unknown").replace('.', '_');
			AttributeApplier.set(player, bonus.attribute(), Aptitudes.id(path), rank * bonus.perRank(), bonus.operation());
		}
		// Entrenamientos de estadística (2 por rol; los de maná se aplican en AbilityManager.maxEnergy).
		for (dev.aptitudes.role.RoleTree.StatSpec spec : dev.aptitudes.role.RoleTree.statSpecs()) {
			if (spec.attribute() == null) continue;
			int rank = RoleManager.activeRank(player, spec.nodeId());
			AttributeApplier.set(player, spec.attribute(), Aptitudes.id("node/" + spec.nodeId()), rank * spec.perRank(),
					spec.operation());
		}
		ElementEffects.applyAttributes(player);
		PathEffects.applyAttributes(player);
	}

	// ----------------------------------------------------------------- Tick

	/** Llamado cada 20 ticks (1 s). */
	public static void tick(MinecraftServer server) {
		int ticks = server.getTicks();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (!player.isAlive() || player.isSpectator()) continue;

			// Herbolista – Savia vital: 1 de vida cada 9/6/3 s (cada 1 s con rango efectivo ≥ 4).
			int sap = RoleManager.activeRank(player, "vital_sap");
			if (sap > 0 && player.getHealth() < player.getMaxHealth() && ticks % (Math.max(1, 12 - 3 * sap) * 20) == 0) {
				player.heal(1.0f);
			}

			// Herbolista – Inmunidad herbal: veneno (r1), hambre (r2), descomposición (r3).
			int immunity = RoleManager.activeRank(player, "herbal_immunity");
			if (immunity >= 1) player.removeStatusEffect(StatusEffects.POISON);
			if (immunity >= 2) player.removeStatusEffect(StatusEffects.HUNGER);
			if (immunity >= 3) player.removeStatusEffect(StatusEffects.WITHER);

			// Asesino – Sombra (rango 2+): invisible mientras se agacha.
			if (RoleManager.activeRank(player, "shadow") >= 2 && player.isSneaking()) {
				quietEffect(player, StatusEffects.INVISIBILITY, 30, 0);
			}

			// Paladín – Aura protectora: Resistencia a ti y a los jugadores cercanos (radio 4/6/8); r3 añade Regeneración.
			int aura = RoleManager.activeRank(player, "protective_aura");
			if (aura > 0) {
				for (PlayerEntity ally : Targeting.playersAround(player, 2 + 2 * aura)) {
					quietEffect(ally, StatusEffects.RESISTANCE, 40, 0);
					if (aura >= 3) quietEffect(ally, StatusEffects.REGENERATION, 40, 0);
				}
			}

			// Druida – Hijo del bosque: en bosques, junglas y taigas, Velocidad (r1), Regeneración (r2), Fuerza (r3).
			int forest = RoleManager.activeRank(player, "forest_child");
			if (forest > 0 && inWoodland(player)) {
				quietEffect(player, StatusEffects.SPEED, 40, 0);
				if (forest >= 2) quietEffect(player, StatusEffects.REGENERATION, 40, 0);
				if (forest >= 3) quietEffect(player, StatusEffects.STRENGTH, 40, 0);
			}

			// Druida – Crecimiento: cada 2 s hace crecer cultivos y retoños cercanos.
			int growth = RoleManager.activeRank(player, "growth");
			if (growth > 0 && ticks % 40 == 0) growPlants(player, 2 + growth, growth);

			// Mercader – Regateo: Héroe de la aldea (descuentos vanilla) de nivel I/II/III.
			int haggling = RoleManager.activeRank(player, "haggling");
			RoleData roleData = RoleManager.get(player);
			// Característica pasiva del Mercader Maestro+ – Señor del mercado: Héroe de la aldea V y Suerte.
			if (roleData.isActive(Role.MERCHANT) && roleData.tier(Role.MERCHANT) >= Evolution.MASTER) {
				quietEffect(player, StatusEffects.HERO_OF_THE_VILLAGE, 60, 4);
				quietEffect(player, StatusEffects.LUCK, 60, 0);
			} else if (haggling > 0) {
				quietEffect(player, StatusEffects.HERO_OF_THE_VILLAGE, 60, haggling - 1);
			}

			ElementEffects.tick(player, ticks);
		}
	}

	public static boolean inWoodland(ServerPlayerEntity player) {
		RegistryEntry<Biome> biome = player.getWorld().getBiome(player.getBlockPos());
		return biome.isIn(BiomeTags.IS_FOREST) || biome.isIn(BiomeTags.IS_JUNGLE) || biome.isIn(BiomeTags.IS_TAIGA);
	}

	/** Acelera cultivos alrededor del jugador (lo usan también los hitos de Agricultura). */
	public static void growAround(ServerPlayerEntity player, int radius, int attempts) {
		growPlants(player, radius, attempts);
	}

	private static void growPlants(ServerPlayerEntity player, int radius, int attempts) {
		ServerWorld world = player.getServerWorld();
		BlockPos center = player.getBlockPos();
		for (int i = 0; i < attempts * 3; i++) {
			BlockPos pos = center.add(world.random.nextInt(radius * 2 + 1) - radius,
					world.random.nextInt(3) - 1, world.random.nextInt(radius * 2 + 1) - radius);
			BlockState state = world.getBlockState(pos);
			boolean plant = state.getBlock() instanceof CropBlock || state.getBlock() instanceof SaplingBlock
					|| state.getBlock() instanceof StemBlock || state.getBlock() instanceof SweetBerryBushBlock;
			if (plant && state.getBlock() instanceof Fertilizable fertilizable
					&& fertilizable.isFertilizable(world, pos, state)
					&& fertilizable.canGrow(world, world.random, pos, state)) {
				fertilizable.grow(world, world.random, pos, state);
				world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						4, 0.3, 0.3, 0.3, 0);
			}
		}
	}

	// ------------------------------------------------------- Daño saliente

	/**
	 * Multiplica el daño de un ataque directo de un jugador y aplica robos de
	 * vida. Llamado desde {@code LivingEntityMixin} al inicio de {@code LivingEntity#damage}.
	 */
	public static float modifyOutgoingDamage(LivingEntity target, DamageSource source, float amount) {
		if (amount <= 0 || target.getWorld().isClient()) return amount;
		// v67 (F4): el daño que recibe un esbirro pasa por Resurgir antes que nada.
		if (!(target instanceof ServerPlayerEntity) && AbilityState.isMinion(target)) {
			amount = ExtraKits.minionIncoming(target, source, amount);
			if (amount <= 0) return amount;
		}
		// F2: el trance aumenta el daño que hace el jugador.
		if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
			amount *= Trance.damageMultiplier(attacker);
			amount *= dev.aptitudes.event.Expeditions.damageMultiplier(attacker, target); // F5: jefe aturdido
		}
		amount = dev.aptitudes.alchemy.AlchemyEffects.incoming(target, source, amount);
		// Sendas – daño entrante al jugador (reducción, espinas).
		if (target instanceof ServerPlayerEntity victim) {
			amount = PathEffects.incoming(victim, source, amount);
			amount = dev.aptitudes.logic.SkillMilestones.modifyIncoming(victim, source, amount);
			amount = RoleArmorSets.modifyIncoming(victim, source, amount);
			amount = RoleMechanics.incoming(victim, source, amount);
			amount = ExtraKits.incoming(victim, source, amount); // v67: habilidades nuevas por rol
		}

		// Nigromante – Drenaje vital también funciona con el daño de sus esbirros.
		if (source.getAttacker() != null && AbilityState.isMinion(source.getAttacker())) {
			ServerPlayerEntity owner = AbilityState.ownerOf(source.getAttacker());
			if (owner != null) {
				drain(owner, amount);
				// Nigromante Maestro+: el daño de tus esbirros te cura un 25 % adicional.
				if (RoleManager.get(owner).tier(Role.NECROMANCER) >= Evolution.MASTER) owner.heal(amount * 0.25f);
			}
			return amount;
		}

		if (!(source.getAttacker() instanceof ServerPlayerEntity player) || target == player) return amount;
		boolean direct = source.getSource() == player;
		float multiplier = 1.0f;

		// Inteligencia: hechizos, explosiones y daño indirecto (proyectiles, rayos) del jugador.
		if (!direct || source.isOf(net.minecraft.entity.damage.DamageTypes.INDIRECT_MAGIC)
				|| source.isOf(net.minecraft.entity.damage.DamageTypes.MAGIC)
				|| source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_EXPLOSION)) {
			multiplier += (float) AbilityManager.attributeValue(player, dev.aptitudes.data.PlayerAttribute.INTELLIGENCE);
		}

		// Paladín – Castigo sagrado: cualquier daño propio contra no-muertos.
		int smite = RoleManager.activeRank(player, "holy_smite");
		if (smite > 0 && target.getType().isIn(EntityTypeTags.UNDEAD)) multiplier += 0.2f * smite;

		if (direct) {
			int bloodlust = RoleManager.activeRank(player, "bloodlust");
			if (bloodlust > 0) {
				float missing = 1.0f - player.getHealth() / Math.max(1.0f, player.getMaxHealth());
				multiplier += missing * 0.15f * bloodlust;
			}
			int backstab = RoleManager.activeRank(player, "backstab");
			if (backstab > 0 && Targeting.isBehind(player, target)) multiplier += 0.25f * backstab;
			int charge = RoleManager.activeRank(player, "charge");
			if (charge > 0 && player.isSprinting()) multiplier += 0.15f * charge;
			int edge = RoleManager.activeRank(player, "keen_edge");
			if (edge > 0 && player.getMainHandStack().getItem() instanceof SwordItem) multiplier += 0.07f * edge;
			multiplier += Combos.damageBonus(player, target); // v28: combo (+3 % por golpe, máx. +15 %)
			multiplier += dev.aptitudes.rune.RuneSocketing.eclipseBonus(player, target); // v34: Runa del Eclipse
		}

		multiplier *= RoleArmorSets.outgoingMultiplier(player, target, source, direct); // Set de rol y arma de rol
		multiplier *= RoleMechanics.outgoing(player, target, source, direct, amount); // Mecánica del rol
		multiplier *= Signatures.markMultiplier(target, player); // Asesino: Marca de muerte
		float result = PathEffects.outgoing(player, target, direct, amount * multiplier); // Sendas
		// Tope del multiplicador combinado del mod (rol + set + mecánica + marca + críticos): evita remates
		// de ×30 al apilar todas las fuentes. 0 = sin tope. Ver docs/BALANCE.md.
		double cap = dev.aptitudes.Aptitudes.config().maxDamageMultiplier;
		if (cap > 0) result = (float) Math.min(result, amount * cap);
		// Nigromante Maestro+ (artes de sangre) – Sangrado: tus ataques desangran (Descomposición I, 3 s).
		RoleData data = RoleManager.get(player);
		if (data.isActive(Role.NECROMANCER) && data.tier(Role.NECROMANCER) >= Evolution.MASTER
				&& !target.hasStatusEffect(StatusEffects.WITHER)) {
			target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
			player.getServerWorld().spawnParticles(Ultimates.BLOOD, target.getX(), target.getBodyY(0.6), target.getZ(),
					6, 0.2, 0.3, 0.2, 0);
		}
		// Monje – Puños de hierro: +1,5 de daño plano por rango a mano vacía.
		if (direct && player.getMainHandStack().isEmpty()) {
			result += 1.5f * RoleManager.activeRank(player, "iron_fists");
		}
		drain(player, result);
		RoleArmorSets.afterOutgoing(player, result);
		NewRoles.afterOutgoing(player, result, direct);
		ExtraKits.afterOutgoing(player, target, source, direct, result); // v67: Juramento, Ensartar, Doble estocada
		return result;
	}

	/** Nigromante – Drenaje vital: cura el 5/10/15 % del daño infligido. */
	private static void drain(ServerPlayerEntity player, float damage) {
		int rank = RoleManager.activeRank(player, "life_drain");
		if (rank > 0 && player.getHealth() < player.getMaxHealth()) player.heal(damage * 0.05f * rank);
	}

	// ------------------------------------------------------- Daño entrante

	/** @return {@code false} si el golpe debe cancelarse. */
	public static boolean allowIncomingDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		// Elementalista (Magma): inmune al suelo ardiente.
		if (source.isOf(DamageTypes.HOT_FLOOR) && ElementEffects.ignoresHotFloor(player)) return false;

		// Paladín – Bastión sagrado: ningún daño (salvo el que ignora la invulnerabilidad, p. ej. /kill o el vacío).
		if (AbilityState.isInvulnerable(player)
				&& !source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		}

		// Las invocaciones nunca dañan a jugadores (p. ej. flechas perdidas de esqueletos esbirros).
		if (source.getAttacker() != null && AbilityState.isSummon(source.getAttacker())) return false;

		if (!(source.getAttacker() instanceof LivingEntity attacker) || attacker == player) return true;
		boolean melee = source.getSource() == attacker;

		// Monje – Nirvana / Asesino – Ejecución sombría: intocable por criaturas.
		if (AbilityState.isUntouchable(player)) return false;

		// Espadachín – Contraataque: anula el golpe y lo devuelve amplificado.
		float riposte = AbilityState.consumeRiposte(player);
		if (riposte > 0) {
			attacker.damage(player.getDamageSources().playerAttack(player), amount * riposte);
			playBlock(player, 0.8f);
			return false;
		}

		// Espadachín – Parada: 5/10/15 % de anular golpes cuerpo a cuerpo.
		int parry = RoleManager.activeRank(player, "parry");
		if (parry > 0 && melee && player.getRandom().nextFloat() < 0.05f * parry) {
			playBlock(player, 1.5f);
			return false;
		}

		// Sendas – Esquiva.
		if (PathEffects.dodges(player)) return false;

		// Monje – Esquiva: 7/14/21 % de evitar cualquier ataque de una criatura (también proyectiles).
		int evasion = RoleManager.activeRank(player, "evasion");
		if (evasion > 0 && player.getRandom().nextFloat() < 0.07f * evasion) {
			player.getServerWorld().spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getBodyY(0.5),
					player.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, SoundCategory.PLAYERS, 1.0f, 1.4f);
			return false;
		}
		return true;
	}

	private static void playBlock(ServerPlayerEntity player, float pitch) {
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, pitch);
	}

	// --------------------------------------------------------------- Eventos

	/** Nigromante – Comunión oscura: cada muerte en 16 bloques da 2/4/6 de energía y 1 de vida. */
	public static void onNearbyDeath(LivingEntity dead) {
		if (dead instanceof PlayerEntity || AbilityState.isSummon(dead)) return;
		for (ServerPlayerEntity player : dead.getWorld().getEntitiesByClass(ServerPlayerEntity.class,
				dead.getBoundingBox().expand(16), PlayerEntity::isAlive)) {
			RoleManager.recordTrial(player, Role.NECROMANCER, Evolution.TrialType.SOULS, 1); // con o sin Comunión oscura
			dev.aptitudes.role.RoleResources.gain(player, Role.NECROMANCER, 5); // Sangre
			int rank = RoleManager.activeRank(player, "dark_pact");
			if (rank <= 0) continue;
			RoleData data = RoleManager.get(player);
			data.setEnergy(Math.min(AbilityManager.maxEnergy(player), data.energy() + 2 * rank));
			player.heal(1.0f);
			AbilityManager.sendEnergy(player, false);
			player.getServerWorld().spawnParticles(ParticleTypes.SOUL, dead.getX(), dead.getBodyY(0.5), dead.getZ(),
					5, 0.2, 0.2, 0.2, 0.02);
		}
	}

	/**
	 * Nigromante Definitivo (Progenitor carmesí) – Inmortalidad carmesí: una vez
	 * cada 5 minutos, un golpe mortal te deja con la mitad de la vida.
	 * @return true si se evitó la muerte
	 */
	public static boolean tryCrimsonImmortality(ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		if (!data.isActive(Role.NECROMANCER) || data.tier(Role.NECROMANCER) < Evolution.ULTIMATE) return false;
		if (!AbilityState.tryCrimsonImmortality(player, 5 * 60 * 20)) return false;
		player.setHealth(player.getMaxHealth() * 0.5f);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 3));
		player.getServerWorld().spawnParticles(Ultimates.BLOOD, player.getX(), player.getBodyY(0.5), player.getZ(),
				80, 1.0, 1.0, 1.0, 0);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8f, 0.6f);
		player.sendMessage(net.minecraft.text.Text.translatable("aptitudes.blood.immortality")
				.formatted(net.minecraft.util.Formatting.DARK_RED), true);
		return true;
	}

	/**
	 * Característica pasiva del Druida Maestro+ – Renacer del bosque: como un tótem
	 * de la inmortalidad, una vez cada 5 minutos.
	 * @return true si se evitó la muerte
	 */
	public static boolean tryForestRebirth(ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		if (!data.isActive(Role.DRUID) || data.tier(Role.DRUID) < Evolution.MASTER) return false;
		if (!AbilityState.tryForestRebirth(player, 5 * 60 * 20)) return false;
		player.setHealth(1.0f);
		player.clearStatusEffects();
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
		player.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getBodyY(0.5),
				player.getZ(), 60, 0.6, 1.0, 0.6, 0.4);
		player.getServerWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getBodyY(0.5),
				player.getZ(), 40, 1.0, 1.0, 1.0, 0);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
		player.sendMessage(net.minecraft.text.Text.translatable("aptitudes.signature.forest_rebirth")
				.formatted(net.minecraft.util.Formatting.GREEN), true);
		return true;
	}

	/** Mercader: XP por comercio y, con Bolsillos profundos, 10/20/30 % de esmeralda extra. */
	public static void onTrade(ServerPlayerEntity player) {
		RoleManager.addRoleXp(player, Role.MERCHANT, Aptitudes.config().roleXpPerAction * 2);
		RoleManager.recordTrial(player, Role.MERCHANT, Evolution.TrialType.ROLE_ACTIONS, 1);
		int pockets = RoleManager.activeRank(player, "deep_pockets");
		if (pockets > 0 && player.getRandom().nextFloat() < 0.1f * pockets) {
			player.getInventory().offerOrDrop(new ItemStack(Items.EMERALD));
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 1.6f);
		}
	}

	// ------------------------------------------------------------ Utilidades

	private static void quietEffect(LivingEntity entity, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		entity.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
	}
}
