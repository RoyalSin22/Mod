package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.network.CooldownPayload;
import dev.aptitudes.network.EnergyPayload;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleResources;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Uso de habilidades activas: valida ranura, rango, enfriamiento y energía,
 * ejecuta la habilidad y solo entonces cobra la energía y aplica el
 * enfriamiento (una habilidad sin objetivo no se cobra).
 *
 * <p>Los enfriamientos son transitorios (en memoria, por tick del servidor): al
 * reiniciar el servidor se reinician, lo cual es aceptable para tiempos de
 * segundos.</p>
 */
public final class AbilityManager {
	/** 0-1: activas del principal · 2-3: del secundario · 4: definitiva (R) · 5: única (H) · 6: característica (Z). */
	public static final int SLOTS = 7;
	public static final int ULTIMATE_SLOT = 4;
	public static final int UNIQUE_SLOT = 5;
	/** Habilidad característica (tecla Z), desde la evolución a Maestro. */
	public static final int SIGNATURE_SLOT = 6;
	public static final int ULTIMATE_ENERGY = 80;
	public static final int ULTIMATE_COOLDOWN_SECS = 180;
	/** Clave de la espera de la legendaria activa (UniqueManager) en el almacén persistente. */
	public static final String LEGENDARY_COOLDOWN_KEY = "legendary";
	/** Magia de sangre: puntos de energía que cubre cada punto de vida. */
	private static final double ENERGY_PER_HEALTH = 10.0;

	/** jugador → (id de nodo → tick del servidor en que termina el enfriamiento). */
	/** Último valor de energía enviado (redondeado) para no inundar la red. */
	private static final Map<UUID, Integer> LAST_SENT_ENERGY = new HashMap<>();

	private AbilityManager() {
	}

	public static void forget(ServerPlayerEntity player) {
		RoleResources.forget(player);
		LAST_SENT_ENERGY.remove(player.getUuid());
		AbilityState.forget(player);
	}

	public static void use(ServerPlayerEntity player, int slot) {
		if (slot < 0 || slot >= SLOTS || !player.isAlive() || player.isSpectator()) return;
		if (slot == ULTIMATE_SLOT) {
			useUltimate(player);
			return;
		}
		if (slot == UNIQUE_SLOT) {
			dev.aptitudes.unique.UniqueManager.use(player);
			return;
		}
		if (slot == SIGNATURE_SLOT) {
			useSignature(player);
			return;
		}
		RoleData data = RoleManager.get(player);
		Role role = RoleManager.roleInSlot(data, slot);
		if (role == null) {
			fail(player, Text.translatable(slot < 2 ? "aptitudes.ability.no_primary" : "aptitudes.ability.no_secondary"));
			return;
		}
		RoleNode node = RoleTree.active(role, data.equipped(role, slot % 2));
		int learned = node == null ? 0 : RoleManager.activeRank(player, node.id()); // incluye la bonificación de evolución
		if (node == null || learned <= 0) {
			fail(player, Text.translatable("aptitudes.ability.not_learned"));
			return;
		}
		int rank = learned + rawRank(data, role, "_mastery"); // Maestría
		int cost = (int) Math.ceil(node.energyCost() * (1.0 - 0.10 * rawRank(data, role, "_efficiency")) // Eficiencia
				* RoleArmorSets.costMultiplier(player)); // Set del Mago
		Ability ability = Abilities.get(node.id());
		execute(player, role, node.id(), Text.translatable(node.translationKey()), cost,
				cooldownTicks(player, node.cooldownSecs(), role), slot, ability == null ? null : (p, bonus) -> ability.use(p, capRank(rank + bonus)));
	}

	/** Habilidad definitiva: la del rol principal si es Definitivo; si no, la del secundario. */
	private static void useUltimate(ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		Role role = ultimateRole(data);
		if (role == null) {
			fail(player, Text.translatable("aptitudes.ability.no_ultimate"));
			return;
		}
		Ability ability = Ultimates.get(role);
		execute(player, role, "ultimate." + role.key(), Text.translatable(Evolution.ultimateKey(role)), ULTIMATE_ENERGY,
				cooldownTicks(player, ULTIMATE_COOLDOWN_SECS, null), ULTIMATE_SLOT,
				ability == null ? null : (p, bonus) -> ability.use(p, capRank(3 + data.tier(role) + bonus)));
	}

	/** Característica: la del principal si es Maestro o más; si no, la del secundario. */
	private static void useSignature(ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		Role role = signatureRole(data);
		if (role == null) {
			fail(player, Text.translatable("aptitudes.ability.no_signature"));
			return;
		}
		Ability ability = Signatures.get(role);
		execute(player, role, "signature." + role.key(), Text.translatable(Evolution.signatureKey(role)),
				Signatures.ENERGY, cooldownTicks(player, Signatures.COOLDOWN_SECS, null), SIGNATURE_SLOT,
				ability == null ? null : (p, bonus) -> ability.use(p, capRank(3 + data.tier(role) + bonus)));
	}

	public static Role signatureRole(RoleData data) {
		for (Role role : new Role[]{data.primary(), data.secondary()}) {
			if (role != null && data.tier(role) >= Evolution.MASTER && !Signatures.isPassive(role)) return role;
		}
		return null;
	}

	public static Role ultimateRole(RoleData data) {
		if (data.primary() != null && data.tier(data.primary()) >= Evolution.ULTIMATE) return data.primary();
		if (data.secondary() != null && data.tier(data.secondary()) >= Evolution.ULTIMATE) return data.secondary();
		return null;
	}

	/** Comprobaciones comunes (enfriamiento y coste), ejecución y cobro. */
	private static void execute(ServerPlayerEntity player, Role role, String cooldownKey, Text name, int energyCost,
								int cooldown, int slot, java.util.function.BiPredicate<ServerPlayerEntity, Integer> action) {
		if (dev.aptitudes.unique.UniqueEffects.isNegated(player)) {
			fail(player, Text.translatable("aptitudes.unique.negated"));
			return;
		}
		long now = gameTime(player);
		long left = cooldowns(player).remaining(cooldownKey, now);
		if (left > 0) {
			fail(player, Text.translatable("aptitudes.ability.cooldown", name, (left + 19) / 20));
			return;
		}
		RoleData data = RoleManager.get(player);
		boolean free = player.isCreative();
		double missingEnergy = Math.max(0, energyCost - data.energy());
		float bloodCost = 0;
		if (!free && missingEnergy > 0) {
			// Nigromante Maestro+: la magia de sangre cubre la energía que falta con vida.
			if (!canUseBloodMagic(data, role)) {
				fail(player, Text.translatable("aptitudes.ability.no_energy", energyCost));
				return;
			}
			bloodCost = (float) Math.ceil(missingEnergy / ENERGY_PER_HEALTH);
			if (player.getHealth() <= bloodCost + 2.0f) {
				fail(player, Text.translatable("aptitudes.ability.no_blood", (int) bloodCost));
				return;
			}
		}
		// Recurso de rol al 50 % o más → habilidad potenciada (+1 rango). Se decide ANTES de lanzarla.
		boolean empowered = RoleResources.canEmpower(player, role);
		if (action == null || !action.test(player, empowered ? 1 : 0)) {
			fail(player, Text.translatable("aptitudes.ability.no_effect"));
			return;
		}

		if (!free) {
			data.setEnergy(data.energy() - (energyCost - missingEnergy));
			if (bloodCost > 0) {
				player.setHealth(player.getHealth() - bloodCost);
				player.getServerWorld().spawnParticles(new net.minecraft.particle.DustParticleEffect(
								new org.joml.Vector3f(0.6f, 0.0f, 0.05f), 1.2f),
						player.getX(), player.getBodyY(0.5), player.getZ(), 20, 0.3, 0.5, 0.3, 0);
			}
		}
		CastFx.onCast(player, role, slot == ULTIMATE_SLOT ? CastFx.Kind.ULTIMATE
				: slot == SIGNATURE_SLOT ? CastFx.Kind.SIGNATURE : CastFx.Kind.ACTIVE);
		if (empowered) RoleResources.consumeEmpower(player, role);
		cooldowns(player).start(cooldownKey, now, cooldown);
		if (ServerPlayNetworking.canSend(player, CooldownPayload.ID)) {
			ServerPlayNetworking.send(player, new CooldownPayload(slot, cooldown));
		}
		RoleMechanics.onCast(player, role, energyCost);
		NewRoles.onCast(player, role, cooldownKey, slot);
		sendEnergy(player, false);
		RoleManager.addRoleXp(player, role, Aptitudes.config().roleXpPerAbilityUse);
		RoleManager.recordTrial(player, role, Evolution.TrialType.ABILITY_USES, 1);
		dev.aptitudes.unique.UniqueEffects.recordAbility(player, cooldownKey);
	}

	/** Quita el enfriamiento de una habilidad (Bardo – Bis) y avisa al HUD. */
	public static void resetCooldown(ServerPlayerEntity player, String cooldownKey, int slot) {
		cooldowns(player).clear(cooldownKey);
		if (ServerPlayNetworking.canSend(player, CooldownPayload.ID)) {
			ServerPlayNetworking.send(player, new CooldownPayload(slot, 0));
		}
	}

	/** Tope del rango efectivo de las activas (evolución + recurso): evita valores absurdos en las fórmulas. */
	private static int capRank(int rank) {
		return Math.max(1, Math.min(6, rank));
	}

	/** Artes de sangre: el Nigromante Maestro o superior puede pagar con vida. */
	public static boolean canUseBloodMagic(RoleData data, Role role) {
		return role == Role.NECROMANCER && data.tier(Role.NECROMANCER) >= Evolution.MASTER;
	}

	/** Rango comprado de un nodo común del rol ({@code <rol>_mastery}, etc.), si el rol está activo. */
	private static int rawRank(RoleData data, Role role, String suffix) {
		return data.isActive(role) ? data.rank(role.key() + suffix) : 0;
	}

	/** @param role rol de la activa (para su Presteza) o {@code null} para característica/definitiva. */
	private static int cooldownTicks(ServerPlayerEntity player, int seconds, @org.jetbrains.annotations.Nullable Role role) {
		RoleData data = RoleManager.get(player);
		double haste = role == null ? 0 : 0.10 * rawRank(data, role, "_haste"); // Presteza
		// Mente clara (Mago) + Fe (atributo) + Presteza; máximo −60 %.
		double reduction = Math.min(0.6, 0.10 * RoleManager.activeRank(player, "clear_mind")
				+ attributeValue(player, dev.aptitudes.data.PlayerAttribute.FAITH) + haste
				+ RoleArmorSets.cooldownReduction(player)
				+ dev.aptitudes.accessory.Accessories.cooldownReduction(player)); // Amuleto arcano
		return (int) Math.max(10, Math.round(seconds * 20 * (1.0 - reduction)));
	}

	/** Enfriamiento restante de cada ranura; se usa al sincronizar tras cambios de rol. */
	public static int[] slotCooldowns(ServerPlayerEntity player) {
		int[] out = new int[SLOTS];
		RoleData data = RoleManager.get(player);
		long now = gameTime(player);
		for (int slot = 0; slot < SLOTS; slot++) {
			String key;
			if (slot == UNIQUE_SLOT) {
				key = LEGENDARY_COOLDOWN_KEY; // la legendaria se gestiona en UniqueManager, mismo almacén
			} else if (slot == SIGNATURE_SLOT) {
				Role role = signatureRole(data);
				key = role == null ? null : "signature." + role.key();
			} else if (slot == ULTIMATE_SLOT) {
				Role role = ultimateRole(data);
				key = role == null ? null : "ultimate." + role.key();
			} else {
				Role role = RoleManager.roleInSlot(data, slot);
				RoleNode node = role == null ? null : RoleTree.active(role, data.equipped(role, slot % 2));
				key = node == null ? null : node.id();
			}
			out[slot] = key == null ? 0 : (int) Math.min(Integer.MAX_VALUE, cooldowns(player).remaining(key, now));
		}
		return out;
	}

	// --------------------------------------------------------------- Energía

	public static double maxEnergy(ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		// Versatilidad: principal y secundario de categorías distintas.
		double versatility = data.primary() != null && data.secondary() != null
				&& data.primary().category() != data.secondary().category()
				? dev.aptitudes.role.RoleCategory.VERSATILITY_ENERGY_BONUS : 0.0;
		double trained = 0; // entrenamientos de maná (Mago, Nigromante, Mercader, Elementalista, Cambiaformas)
		for (RoleTree.StatSpec spec : RoleTree.statSpecs()) {
			if (spec.attribute() == null) trained += spec.perRank() * RoleManager.activeRank(player, spec.nodeId());
		}
		return versatility + trained + attributeValue(player, dev.aptitudes.data.PlayerAttribute.MIND) // Mente
				+ Aptitudes.config().baseMaxEnergy
				+ dev.aptitudes.accessory.Accessories.bonusMana(player) // Anillo de maná
				+ 25.0 * RoleManager.activeRank(player, "arcane_affinity")
				+ 25.0 * RoleManager.activeRank(player, "battery");
	}

	/** Valor total de un atributo propio: puntos asignados × valor por punto. */
	public static double attributeValue(ServerPlayerEntity player, dev.aptitudes.data.PlayerAttribute attribute) {
		return dev.aptitudes.logic.ProgressManager.effectivePoints(
				dev.aptitudes.logic.ProgressManager.get(player).getAllocated(attribute))
				* dev.aptitudes.logic.ProgressManager.effectivePerPoint(player, attribute);
	}

	public static double regenPerSecond(ServerPlayerEntity player) {
		int mind = dev.aptitudes.logic.ProgressManager.get(player).getAllocated(dev.aptitudes.data.PlayerAttribute.MIND);
		return Aptitudes.config().baseEnergyRegenPerSecond + 0.05 * mind
				+ 0.5 * RoleManager.activeRank(player, "arcane_affinity")
				+ 0.5 * RoleManager.activeRank(player, "battery");
	}

	/** Gasta energía si hay suficiente (usado por ítems como el bastón o la pistola). */
	public static boolean tryConsume(ServerPlayerEntity player, double amount) {
		if (player.isCreative()) return true;
		RoleData data = RoleManager.get(player);
		if (data.energy() < amount) {
			fail(player, Text.translatable("aptitudes.ability.no_energy", (int) Math.ceil(amount)));
			return false;
		}
		data.setEnergy(data.energy() - amount);
		sendEnergy(player, false);
		return true;
	}

	/** Llamado cada 10 ticks: regenera energía. */
	public static void tickEnergy(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			RoleData data = RoleManager.get(player);
			double max = maxEnergy(player);
			if (data.energy() < max) {
				data.setEnergy(Math.min(max, data.energy() + regenPerSecond(player) / 2.0));
			} else if (data.energy() > max) {
				data.setEnergy(max);
			}
			sendEnergy(player, false);
		}
	}

	public static void sendEnergy(ServerPlayerEntity player, boolean force) {
		if (!ServerPlayNetworking.canSend(player, EnergyPayload.ID)) return;
		RoleData data = RoleManager.get(player);
		double max = maxEnergy(player);
		int key = (int) Math.floor(data.energy()) * 10_000 + (int) max;
		Integer last = LAST_SENT_ENERGY.put(player.getUuid(), key);
		if (force || last == null || last != key) {
			ServerPlayNetworking.send(player, new EnergyPayload((float) data.energy(), (float) max));
		}
	}

	// ------------------------------------------------------------- Utilidades

	/** Tiempos de espera persistentes del jugador (v18: Data Attachment, sobreviven a reinicios y desconexiones). */
	public static CooldownBook cooldowns(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(dev.aptitudes.data.ModAttachments.COOLDOWNS);
	}

	/**
	 * Tiempo de juego absoluto del mundo principal: se guarda con el mundo y no se reinicia con el servidor
	 * (a diferencia de {@link #now}), por eso se usa para las esperas persistentes.
	 */
	public static long gameTime(ServerPlayerEntity player) {
		return player.getServer() != null ? player.getServer().getOverworld().getTime() : player.getWorld().getTime();
	}

	public static int now(ServerPlayerEntity player) {
		return player.getServer() != null ? player.getServer().getTicks() : 0;
	}

	private static void fail(ServerPlayerEntity player, Text message) {
		player.sendMessage(message.copy().formatted(Formatting.RED), true);
	}
}
