package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.RoleArmorItem;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.Evolution;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bonificaciones de set completo (4 piezas del mismo rol) y escalado de las armas
 * de rol. El set solo se activa si ese rol es tu principal o secundario: cualquiera
 * puede llevar la armadura por su defensa, pero el poder es del rol.
 *
 * <table>
 *   <tr><th>Rol</th><th>Bonificación</th></tr>
 *   <tr><td>Berserker</td><td>+10 % daño; Fuerza I bajo el 50 % de vida</td></tr>
 *   <tr><td>Mago</td><td>−25 % coste de maná; +15 % daño mágico</td></tr>
 *   <tr><td>Asesino</td><td>+10 % velocidad; +30 % daño a quien no te tiene como objetivo</td></tr>
 *   <tr><td>Herbolario</td><td>Inmune a veneno y hambre; cura 1 PV cada 5 s</td></tr>
 *   <tr><td>Ingeniero</td><td>−40 % daño de explosiones; −15 % enfriamientos</td></tr>
 *   <tr><td>Lancero</td><td>+0,75 alcance; +20 % daño esprintando o montado</td></tr>
 *   <tr><td>Espadachín</td><td>+10 % velocidad de ataque; +15 % daño con espadas</td></tr>
 *   <tr><td>Paladín</td><td>+4 vida máx.; −30 % daño de no-muertos; +25 % contra ellos</td></tr>
 *   <tr><td>Nigromante</td><td>Robo de vida 10 %; inmune a Descomposición</td></tr>
 *   <tr><td>Druida</td><td>Regeneración bajo el sol; inmune a cactus y arbustos</td></tr>
 *   <tr><td>Mercader</td><td>+2 suerte; Héroe de la aldea permanente</td></tr>
 *   <tr><td>Monje</td><td>+30 % daño a mano vacía; Salto I</td></tr>
 *   <tr><td>Elementalista</td><td>Resistencia al fuego; sin congelación; +15 % daño mágico</td></tr>
 *   <tr><td>Cambiaformas</td><td>−15 % enfriamientos; +10 % velocidad; visión nocturna</td></tr>
 * </table>
 */
public final class RoleArmorSets {
	private record AttrBonus(RegistryEntry<EntityAttribute> attribute, double value, Operation operation) {}

	private static final Map<Role, AttrBonus> ATTRIBUTES = new HashMap<>();
	private static final Map<UUID, Role> ANNOUNCED = new HashMap<>();
	private static final Identifier MODIFIER = Aptitudes.id("set_bonus");

	static {
		ATTRIBUTES.put(Role.BERSERKER, new AttrBonus(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.10, Operation.ADD_MULTIPLIED_TOTAL));
		ATTRIBUTES.put(Role.ASSASSIN, new AttrBonus(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10, Operation.ADD_MULTIPLIED_BASE));
		ATTRIBUTES.put(Role.LANCER, new AttrBonus(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 0.75, Operation.ADD_VALUE));
		ATTRIBUTES.put(Role.SWORDSMAN, new AttrBonus(EntityAttributes.GENERIC_ATTACK_SPEED, 0.10, Operation.ADD_MULTIPLIED_BASE));
		ATTRIBUTES.put(Role.PALADIN, new AttrBonus(EntityAttributes.GENERIC_MAX_HEALTH, 4.0, Operation.ADD_VALUE));
		ATTRIBUTES.put(Role.MERCHANT, new AttrBonus(EntityAttributes.GENERIC_LUCK, 2.0, Operation.ADD_VALUE));
		ATTRIBUTES.put(Role.SHAPESHIFTER, new AttrBonus(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10, Operation.ADD_MULTIPLIED_BASE));
	}

	private RoleArmorSets() {
	}

	/** Rol del set completo que lleva puesto (4 piezas del mismo rol), o null. */
	@Nullable
	public static Role wornSet(LivingEntity entity) {
		Role found = null;
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
				EquipmentSlot.FEET}) {
			ItemStack stack = entity.getEquippedStack(slot);
			if (!(stack.getItem() instanceof RoleArmorItem piece)) return null;
			if (found == null) found = piece.role();
			else if (found != piece.role()) return null;
		}
		return found;
	}

	/** Rol cuyo set está activo: set completo puesto y ese rol es principal o secundario. */
	@Nullable
	public static Role activeSet(ServerPlayerEntity player) {
		Role role = wornSet(player);
		return role != null && RoleManager.get(player).isActive(role) ? role : null;
	}

	public static boolean has(ServerPlayerEntity player, Role role) {
		return activeSet(player) == role;
	}

	// ---------------------------------------------------------------- Tick

	/** Cada segundo: modificadores de atributo, efectos permanentes y aviso al activarse. */
	public static void tick(MinecraftServer server) {
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			Role active = p.isAlive() ? activeSet(p) : null;
			AttrBonus bonus = active == null ? null : ATTRIBUTES.get(active);
			// Un único id de modificador: se quita de todos los atributos posibles y se pone en el que toca.
			for (AttrBonus any : ATTRIBUTES.values()) {
				if (bonus == null || any.attribute() != bonus.attribute()) {
					AttributeApplier.set(p, any.attribute(), MODIFIER, 0, any.operation());
				}
			}
			if (bonus != null) AttributeApplier.set(p, bonus.attribute(), MODIFIER, bonus.value(), bonus.operation());
			if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());

			Role previous = active == null ? ANNOUNCED.remove(p.getUuid()) : ANNOUNCED.put(p.getUuid(), active);
			if (active != null && previous != active) announce(p, active);
			if (active == null) continue;

			switch (active) {
				case BERSERKER -> {
					if (p.getHealth() < p.getMaxHealth() * 0.5f) quiet(p, StatusEffects.STRENGTH, 40, 0);
				}
				case HERBALIST -> {
					p.removeStatusEffect(StatusEffects.POISON);
					p.removeStatusEffect(StatusEffects.HUNGER);
					if (server.getTicks() % 100 < 20 && p.getHealth() < p.getMaxHealth()) p.heal(1.0f);
				}
				case NECROMANCER -> p.removeStatusEffect(StatusEffects.WITHER);
				case DRUID -> {
					if (p.getWorld().isDay() && p.getWorld().isSkyVisible(p.getBlockPos().up())) {
						quiet(p, StatusEffects.REGENERATION, 60, 0);
					}
				}
				case MERCHANT -> quiet(p, StatusEffects.HERO_OF_THE_VILLAGE, 60, 0);
				case MONK -> quiet(p, StatusEffects.JUMP_BOOST, 40, 0);
				case ELEMENTALIST -> {
					quiet(p, StatusEffects.FIRE_RESISTANCE, 60, 0);
					p.setFrozenTicks(0);
				}
				case SHAPESHIFTER -> quiet(p, StatusEffects.NIGHT_VISION, 260, 0);
				default -> {
				}
			}
		}
	}

	private static void announce(ServerPlayerEntity p, Role role) {
		p.sendMessage(Text.translatable("aptitudes.set.activated", Text.translatable("setbonus.aptitudes." + role.key()))
				.withColor(role.color()), true);
		Vec3d pos = p.getPos();
		ParticleFx.spiral(p.getServerWorld(), pos, 0.8, 2.2, ParticleFx.dust(role.color(), 1.2f), 40, 2.5);
		ParticleFx.ring(p.getServerWorld(), pos.add(0, 0.1, 0), 1.0, ParticleTypes.ENCHANT, 24);
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(),
				SoundCategory.PLAYERS, 1.0f, 1.2f);
	}

	public static void forget(ServerPlayerEntity p) {
		ANNOUNCED.remove(p.getUuid());
	}

	// ------------------------------------------------------- Habilidades

	/** Multiplicador del coste de maná de las activas. */
	public static double costMultiplier(ServerPlayerEntity player) {
		return has(player, Role.MAGE) ? 0.75 : 1.0;
	}

	/** Reducción adicional de enfriamientos (se suma a Fe, Presteza, etc.). */
	public static double cooldownReduction(ServerPlayerEntity player) {
		Role set = activeSet(player);
		return set == Role.ENGINEER || set == Role.SHAPESHIFTER ? 0.15 : 0.0;
	}

	// ----------------------------------------------------------------- Daño

	/** Multiplicador de daño saliente del set y del arma de rol. */
	public static float outgoingMultiplier(ServerPlayerEntity player, LivingEntity target, DamageSource source, boolean direct) {
		float mult = 1.0f;
		Role set = activeSet(player);
		if (set != null) {
			boolean magic = !direct || source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.INDIRECT_MAGIC);
			switch (set) {
				case MAGE, ELEMENTALIST -> {
					if (magic) mult += 0.15f;
				}
				case ASSASSIN -> {
					if (direct && !(target instanceof MobEntity mob && mob.getTarget() == player)) mult += 0.30f;
				}
				case LANCER -> {
					if (direct && (player.isSprinting() || player.hasVehicle())) mult += 0.20f;
				}
				case SWORDSMAN -> {
					if (direct && player.getMainHandStack().getItem() instanceof SwordItem) mult += 0.15f;
				}
				case PALADIN -> {
					if (target.getType().isIn(EntityTypeTags.UNDEAD)) mult += 0.25f;
				}
				case MONK -> {
					if (direct && player.getMainHandStack().isEmpty()) mult += 0.30f;
				}
				default -> {
				}
			}
		}
		if (direct) mult += weaponBonus(player);
		return mult;
	}

	/**
	 * Armas de rol: +1 % de daño por nivel del rol, +10 % en Maestro y +20 % en Definitivo,
	 * si el rol está activo y se empuña su ítem característico.
	 */
	public static float weaponBonus(ServerPlayerEntity player) {
		ItemStack hand = player.getMainHandStack();
		if (hand.isEmpty()) return 0f;
		RoleData data = RoleManager.get(player);
		for (Role role : new Role[]{data.primary(), data.secondary()}) {
			if (role == null || !hand.isOf(role.iconStack().getItem())) continue;
			int tier = data.tier(role);
			return 0.01f * data.level(role) + (tier >= Evolution.ULTIMATE ? 0.20f : tier >= Evolution.MASTER ? 0.10f : 0f);
		}
		return 0f;
	}

	/** Tras calcular el daño saliente: robo de vida del Nigromante. */
	public static void afterOutgoing(ServerPlayerEntity player, float damage) {
		if (damage > 0 && has(player, Role.NECROMANCER) && player.getHealth() < player.getMaxHealth()) {
			player.heal(damage * 0.10f);
		}
	}

	/** @return false si el set anula el daño (Druida: cactus y arbustos). */
	public static boolean allowIncoming(ServerPlayerEntity player, DamageSource source) {
		if (source.isOf(DamageTypes.CACTUS) || source.isOf(DamageTypes.SWEET_BERRY_BUSH)) return !has(player, Role.DRUID);
		return true;
	}

	public static float modifyIncoming(ServerPlayerEntity player, DamageSource source, float amount) {
		Role set = activeSet(player);
		if (set == Role.ENGINEER && source.isIn(DamageTypeTags.IS_EXPLOSION)) return amount * 0.6f;
		if (set == Role.PALADIN && source.getAttacker() instanceof LivingEntity attacker
				&& attacker.getType().isIn(EntityTypeTags.UNDEAD)) {
			return amount * 0.7f;
		}
		return amount;
	}

	private static void quiet(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
	}
}
