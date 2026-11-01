package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import dev.aptitudes.network.MechanicPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mecánica propia de cada rol (nodo central de la fila 0) y sus dos pasivas
 * reactivas (efectos que saltan solos al golpear, recibir daño, matar…).
 *
 * <table>
 *   <tr><th>Rol</th><th>Mecánica</th><th>Reactiva 1</th><th>Reactiva 2</th></tr>
 *   <tr><td>Bersérker</td><td>Frenesí: golpes seguidos acumulan +4 % de daño</td><td>Inquebrantable</td><td>Carnicería</td></tr>
 *   <tr><td>Mago</td><td>Cargas arcanas: cada N hechizos, uno gratis</td><td>Escudo de maná</td><td>Eco arcano</td></tr>
 *   <tr><td>Asesino</td><td>Ritmo letal: 3.er golpe al mismo objetivo = remate</td><td>Huida en humo</td><td>Desvanecerse</td></tr>
 *   <tr><td>Herbolista</td><td>Floración: quieto 3 s, regeneración en área</td><td>Antídoto</td><td>Piel espinosa</td></tr>
 *   <tr><td>Ingeniero</td><td>Sobrecarga: calor por habilidad → modo potenciado</td><td>Escudo automático</td><td>Reparación de campo</td></tr>
 *   <tr><td>Lancero</td><td>Ímpetu: esprintar carga el siguiente golpe</td><td>Afianzarse</td><td>Caída del dragontino</td></tr>
 *   <tr><td>Espadachín</td><td>Kata: el 3.er golpe encadenado barre</td><td>Reflejos</td><td>Ráfaga</td></tr>
 *   <tr><td>Paladín</td><td>Luz sagrada: 5 cargas → explosión sagrada</td><td>Intercesión</td><td>Luz purificadora</td></tr>
 *   <tr><td>Nigromante</td><td>Cosecha de almas: muertes = +3 % daño</td><td>Escudo de hueso</td><td>Estallido de cadáver</td></tr>
 *   <tr><td>Druida</td><td>Ciclo: de día te curas, de noche pegas más</td><td>Piel de corteza</td><td>Zarzas</td></tr>
 *   <tr><td>Mercader</td><td>Interés compuesto: esmeraldas = suerte y daño</td><td>Seguro</td><td>Recompensa</td></tr>
 *   <tr><td>Monje</td><td>Posturas: Tigre (esprintando) / Grulla (agachado)</td><td>Desviar proyectiles</td><td>Paz interior</td></tr>
 *   <tr><td>Elementalista</td><td>Reacción: cambiar de elemento potencia 5 s</td><td>Guarda elemental</td><td>Muerte resonante</td></tr>
 *   <tr><td>Cambiaformas</td><td>Instinto: transformado acumulas daño</td><td>Escurridizo</td><td>Devorar</td></tr>
 * </table>
 */
public final class RoleMechanics {
	/** Estado en memoria por jugador (se pierde al salir: son efectos de combate). */
	private static final class State {
		int frenzy, lastHit = -1000, lastTarget = -1, combo, kata, lastKata = -1000;
		int mageCharges, heat, overdriveUntil, holy, souls, lastSoulGain, instinct, reactionUntil;
		int sprintSeconds, stillSeconds;
		boolean counterReady, overheated;
		Vec3d lastPos = Vec3d.ZERO;
		final Map<String, Integer> cooldowns = new HashMap<>();
	}

	private static final Map<UUID, State> STATES = new HashMap<>();
	/** Último estado enviado al HUD de cada jugador (para no reenviar lo mismo). */
	private static final Map<UUID, List<MechanicPayload.Entry>> LAST_SENT = new HashMap<>();
	private static final Identifier MERCHANT_LUCK = Aptitudes.id("mech/compound_interest");

	private RoleMechanics() {
	}

	private static State state(ServerPlayerEntity p) {
		return STATES.computeIfAbsent(p.getUuid(), k -> new State());
	}

	/** Rango (tope 5) del nodo si su rol está activo. */
	private static int rank(ServerPlayerEntity p, String nodeId) {
		return Math.min(5, RoleManager.activeRank(p, nodeId));
	}

	private static int now(ServerPlayerEntity p) {
		return p.getServer().getTicks();
	}

	/** Enfriamiento interno de una reactiva. @return true (y lo arranca) si estaba listo. */
	private static boolean ready(ServerPlayerEntity p, State s, String key, int ticks) {
		int now = now(p);
		Integer until = s.cooldowns.get(key);
		if (until != null && until > now) return false;
		s.cooldowns.put(key, now + ticks);
		return true;
	}

	public static void forget(ServerPlayerEntity p) {
		STATES.remove(p.getUuid());
		LAST_SENT.remove(p.getUuid());
	}

	// ------------------------------------------------------------- HUD

	/** Mecánica de cada rol: id del nodo (la fila 0 central del árbol). */
	private static String mechanicNode(Role role) {
		List<dev.aptitudes.role.RoleNode> nodes = dev.aptitudes.role.RoleTree.nodes(role);
		return nodes.size() >= 5 ? nodes.get(nodes.size() - 5).id() : "";
	}

	/** Envía al HUD el estado de las mecánicas de los roles activos (solo si cambió). */
	public static void sync(ServerPlayerEntity p) {
		if (!ServerPlayNetworking.canSend(p, MechanicPayload.ID)) return;
		State s = state(p);
		int now = now(p);
		RoleData data = RoleManager.get(p);
		List<MechanicPayload.Entry> entries = new ArrayList<>();
		for (Role role : new Role[]{data.primary(), data.secondary()}) {
			if (role == null) continue;
			String node = mechanicNode(role);
			int r = rank(p, node);
			if (r <= 0) continue;
			MechanicPayload.Entry e = switch (node) {
				case "frenzy" -> entry(node, s.frenzy, 2 + r);
				case "arcane_charges" -> entry(node, s.mageCharges, Math.max(2, 6 - r));
				case "lethal_tempo" -> entry(node, now - s.lastHit <= 40 ? s.combo : 0, 3);
				case "bloom" -> entry(node, Math.min(3, s.stillSeconds), 3);
				case "overdrive" -> s.overdriveUntil > now
						? new MechanicPayload.Entry(node, (s.overdriveUntil - now + 19) / 20, 0, "mech.aptitudes.overdrive_on")
						: entry(node, s.heat / 25, 4);
				case "impetus" -> entry(node, Math.min(2, s.sprintSeconds), 2);
				case "kata" -> entry(node, now - s.lastKata <= 30 ? s.kata : 0, 3);
				case "holy_light" -> entry(node, s.holy, 5);
				case "soul_harvest" -> entry(node, s.souls, 2 + r);
				case "day_night_cycle" -> new MechanicPayload.Entry(node, 0, 0,
						p.getWorld().isDay() ? "mech.aptitudes.day" : "mech.aptitudes.night");
				case "compound_interest" -> entry(node, Math.min(2 + r, p.getInventory().count(Items.EMERALD) / 10), 2 + r);
				case "stances" -> new MechanicPayload.Entry(node, 0, 0, p.isSprinting() ? "mech.aptitudes.tiger"
						: p.isSneaking() ? "mech.aptitudes.crane" : "mech.aptitudes.neutral");
				case "elemental_reaction" -> s.reactionUntil > now
						? new MechanicPayload.Entry(node, (s.reactionUntil - now + 19) / 20, 0, "mech.aptitudes.reaction_on")
						: new MechanicPayload.Entry(node, 0, 0, "mech.aptitudes.reaction_ready");
				case "instinct" -> entry(node, s.instinct, 2 + r);
				default -> null;
			};
			if (e != null) entries.add(e);
		}
		List<MechanicPayload.Entry> last = LAST_SENT.get(p.getUuid());
		if (entries.equals(last)) return;
		LAST_SENT.put(p.getUuid(), entries);
		ServerPlayNetworking.send(p, new MechanicPayload(entries));
	}

	private static MechanicPayload.Entry entry(String node, int value, int max) {
		return new MechanicPayload.Entry(node, Math.max(0, Math.min(value, max)), max, "");
	}

	// ------------------------------------------------------------ Golpes

	/**
	 * Multiplicador del daño saliente del jugador (se suma a 1).
	 * @param direct golpe cuerpo a cuerpo propio
	 */
	public static float outgoing(ServerPlayerEntity p, LivingEntity target, DamageSource source, boolean direct, float amount) {
		State s = state(p);
		int now = now(p);
		float bonus = 0f;

		if (direct) {
			// Bersérker – Frenesí
			int frenzy = rank(p, "frenzy");
			if (frenzy > 0) {
				s.frenzy = now - s.lastHit <= 60 ? Math.min(2 + frenzy, s.frenzy + 1) : 1;
				bonus += 0.04f * s.frenzy;
				if (s.frenzy >= 3) particles(p, target, ParticleTypes.ANGRY_VILLAGER, 2);
			}
			// Asesino – Ritmo letal
			int tempo = rank(p, "lethal_tempo");
			if (tempo > 0) {
				s.combo = target.getId() == s.lastTarget && now - s.lastHit <= 40 ? s.combo + 1 : 1;
				if (s.combo >= 3) {
					s.combo = 0;
					bonus += 0.25f + 0.25f * tempo;
					target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0));
					particles(p, target, ParticleTypes.CRIT, 15);
					sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 0.7f);
				}
			}
			// Lancero – Ímpetu
			int impetus = rank(p, "impetus");
			if (impetus > 0 && s.sprintSeconds >= 2) {
				s.sprintSeconds = 0;
				bonus += 0.15f + 0.15f * impetus;
				Vec3d dir = target.getPos().subtract(p.getPos()).normalize();
				target.takeKnockback(0.8, -dir.x, -dir.z);
				particles(p, target, ParticleTypes.CLOUD, 10);
			}
			// Espadachín – Kata y Reflejos
			int kata = rank(p, "kata");
			if (kata > 0 && p.getMainHandStack().getItem() instanceof SwordItem) {
				s.kata = now - s.lastKata <= 30 ? s.kata + 1 : 1;
				s.lastKata = now;
				if (s.kata >= 3) {
					s.kata = 0;
					float sweep = amount * (0.2f + 0.2f * kata);
					for (LivingEntity e : Targeting.hostilesAround(p, 3.0)) {
						if (e == target) continue;
						e.damage(p.getDamageSources().indirectMagic(p, p), sweep);
					}
					ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 1, 0), 2.5, ParticleTypes.SWEEP_ATTACK, 8);
					sound(p, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f);
				}
			}
			if (s.counterReady) {
				s.counterReady = false;
				bonus += 0.5f;
				particles(p, target, ParticleTypes.ENCHANTED_HIT, 10);
			}
			// Paladín – Luz sagrada
			int holy = rank(p, "holy_light");
			if (holy > 0) {
				s.holy += target.getType().isIn(EntityTypeTags.UNDEAD) ? 2 : 1;
				if (s.holy >= 5) {
					s.holy = 0;
					for (LivingEntity e : p.getWorld().getEntitiesByClass(LivingEntity.class,
							target.getBoundingBox().expand(3), e -> Targeting.isHostileTo(p, e) && e != target)) {
						e.damage(p.getDamageSources().indirectMagic(p, p), 3 + holy);
					}
					p.heal(2 + holy);
					ParticleFx.ring(p.getServerWorld(), target.getPos().add(0, 0.2, 0), 3, ParticleFx.dust(0xFFE082, 1.4f), 30);
					p.getServerWorld().spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getBodyY(0.5), target.getZ(),
							20, 0.5, 0.7, 0.5, 0.05);
					sound(p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.6f);
				}
			}
			// Monje – Postura del tigre (esprintando)
			int stances = rank(p, "stances");
			if (stances > 0 && p.isSprinting()) bonus += 0.05f + 0.05f * stances;
			s.lastHit = now;
			s.lastTarget = target.getId();
		}

		// Ingeniero – Sobrecarga activa
		if (s.overdriveUntil > now) bonus += 0.20f;
		// Nigromante – Cosecha de almas
		if (rank(p, "soul_harvest") > 0) bonus += 0.03f * s.souls;
		// Druida – Ciclo (de noche)
		int cycle = rank(p, "day_night_cycle");
		if (cycle > 0 && !p.getWorld().isDay()) bonus += 0.05f + 0.05f * cycle;
		// Mercader – Interés compuesto: +2 % por cada 10 esmeraldas (hasta el tope)
		int interest = rank(p, "compound_interest");
		if (interest > 0) bonus += 0.02f * Math.min(2 + interest, p.getInventory().count(Items.EMERALD) / 10);
		// Elementalista – Reacción (5 s tras cambiar de elemento; daño mágico)
		int reaction = rank(p, "elemental_reaction");
		if (reaction > 0 && !direct && s.reactionUntil > now) bonus += 0.15f + 0.15f * reaction;
		// Cambiaformas – Instinto
		if (rank(p, "instinct") > 0) bonus += 0.05f * s.instinct;
		sync(p);

		return 1.0f + bonus;
	}

	// ------------------------------------------------------- Daño recibido

	/** @return false si el golpe se anula (Monje: desviar proyectiles). */
	public static boolean allowIncoming(ServerPlayerEntity p, DamageSource source) {
		int deflect = rank(p, "deflect");
		if (deflect > 0 && source.isIn(DamageTypeTags.IS_PROJECTILE) && p.getRandom().nextFloat() < 0.15f * deflect) {
			particles(p, p, ParticleTypes.CRIT, 8);
			sound(p, SoundEvents.ITEM_SHIELD_BLOCK, 1.6f);
			return false;
		}
		return true;
	}

	public static float incoming(ServerPlayerEntity p, DamageSource source, float amount) {
		State s = state(p);
		Entity attacker = source.getAttacker();
		boolean melee = attacker instanceof LivingEntity && source.getSource() == attacker;
		float after = p.getHealth() - amount;

		// Mago – Escudo de maná: absorbe parte del golpe gastando 2 de maná por punto.
		int shield = rank(p, "mana_shield");
		if (shield > 0) {
			RoleData data = RoleManager.get(p);
			float absorbed = (float) Math.min(amount * 0.1f * shield, data.energy() / 2.0);
			if (absorbed > 0.1f) {
				data.setEnergy(data.energy() - absorbed * 2);
				amount -= absorbed;
				AbilityManager.sendEnergy(p, false);
				particles(p, p, ParticleTypes.ENCHANT, 6);
			}
		}
		// Lancero – Afianzarse (agachado) y Monje – Postura de la grulla (agachado)
		if (p.isSneaking()) {
			int brace = rank(p, "brace");
			if (brace > 0) amount *= 1f - 0.1f * brace;
			int stances = rank(p, "stances");
			if (stances > 0) amount *= 1f - (0.05f + 0.05f * stances);
		}
		// Lancero – Caída del dragontino
		int fall = rank(p, "dragoon_fall");
		if (fall > 0 && source.isOf(DamageTypes.FALL)) amount *= Math.max(0.1f, 1f - 0.3f * fall);
		// Elementalista – Guarda elemental
		int ward = rank(p, "elemental_ward");
		if (ward > 0 && (source.isIn(DamageTypeTags.IS_FIRE) || source.isIn(DamageTypeTags.IS_FREEZING)
				|| source.isIn(DamageTypeTags.IS_LIGHTNING))) {
			amount *= Math.max(0.1f, 1f - 0.2f * ward);
		}
		// Mercader – Seguro: golpes fuertes cuestan 1 esmeralda y se reducen.
		int insurance = rank(p, "insurance");
		if (insurance > 0 && amount >= 6 && (p.isCreative() || AbilitiesExtra.consume(p, Items.EMERALD, 1))) {
			amount *= 1f - (0.2f + 0.1f * insurance);
			sound(p, SoundEvents.ENTITY_VILLAGER_YES, 1.2f);
		}
		after = p.getHealth() - amount;

		// Bersérker – Inquebrantable
		int unbreakable = rank(p, "unbreakable");
		if (unbreakable > 0 && after < p.getMaxHealth() * 0.3f && ready(p, s, "unbreakable", 400)) {
			effect(p, StatusEffects.RESISTANCE, 60 + 20 * unbreakable, unbreakable >= 3 ? 1 : 0);
			particles(p, p, ParticleTypes.ANGRY_VILLAGER, 8);
		}
		// Asesino – Huida en humo
		int smoke = rank(p, "smoke_escape");
		if (smoke > 0 && after < p.getMaxHealth() * 0.3f && ready(p, s, "smoke_escape", Math.max(300, 900 - 100 * smoke))) {
			effect(p, StatusEffects.INVISIBILITY, 60, 0);
			effect(p, StatusEffects.SPEED, 60, 1);
			p.getServerWorld().spawnParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getBodyY(0.5), p.getZ(), 40, 0.6, 0.8, 0.6, 0.02);
		}
		// Herbolista – Piel espinosa / Druida – Zarzas y Piel de corteza
		if (melee) {
			LivingEntity a = (LivingEntity) attacker;
			int thorny = rank(p, "thorny_skin");
			if (thorny > 0) a.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * (1 + thorny), 0));
			int bramble = rank(p, "bramble");
			if (bramble > 0) a.damage(p.getDamageSources().thorns(p), bramble);
			int bark = rank(p, "barkskin");
			if (bark > 0 && p.getRandom().nextFloat() < 0.1f * bark && ready(p, s, "barkskin", 300)) {
				effect(p, StatusEffects.RESISTANCE, 60, 1);
				particles(p, p, ParticleTypes.COMPOSTER, 12);
			}
		}
		// Ingeniero – Escudo automático
		int auto = rank(p, "auto_shield");
		if (auto > 0 && amount >= 5 && ready(p, s, "auto_shield", 400)) {
			effect(p, StatusEffects.ABSORPTION, 100, auto - 1);
			particles(p, p, ParticleTypes.ELECTRIC_SPARK, 15);
		}
		// Espadachín – Reflejos
		int reflexes = rank(p, "reflexes");
		if (reflexes > 0 && p.getRandom().nextFloat() < 0.05f * reflexes) {
			s.counterReady = true;
			effect(p, StatusEffects.SPEED, 40, 1);
			p.sendMessage(Text.translatable("aptitudes.mech.counter_ready").formatted(net.minecraft.util.Formatting.AQUA), true);
		}
		// Paladín – Luz purificadora
		int purify = rank(p, "purifying_light");
		if (purify > 0 && p.getRandom().nextFloat() < 0.1f * purify) {
			p.removeStatusEffect(StatusEffects.WITHER);
			p.removeStatusEffect(StatusEffects.POISON);
			p.heal(1.0f);
			particles(p, p, ParticleTypes.END_ROD, 6);
		}
		// Nigromante – Escudo de hueso
		int bone = rank(p, "bone_shield");
		if (bone > 0 && after < p.getMaxHealth() * 0.5f && ready(p, s, "bone_shield", 600)) {
			effect(p, StatusEffects.ABSORPTION, 100, bone - 1);
			p.getServerWorld().spawnParticles(ParticleFx.dust(0xE0E0D0, 1.2f), p.getX(), p.getBodyY(0.5), p.getZ(),
					20, 0.5, 0.7, 0.5, 0);
		}
		// Cambiaformas – Escurridizo
		int slippery = rank(p, "slippery");
		if (slippery > 0 && p.getRandom().nextFloat() < 0.1f * slippery && ready(p, s, "slippery", 400)) {
			effect(p, StatusEffects.INVISIBILITY, 40, 0);
			p.getServerWorld().spawnParticles(ParticleTypes.POOF, p.getX(), p.getBodyY(0.5), p.getZ(), 15, 0.4, 0.6, 0.4, 0.02);
		}
		return Math.max(0f, amount);
	}

	// ------------------------------------------------------------ Muertes

	public static void onKill(ServerPlayerEntity p, LivingEntity victim, DamageSource source) {
		State s = state(p);
		int carnage = rank(p, "carnage");
		if (carnage > 0) p.heal(1 + carnage);
		int echo = rank(p, "spell_echo");
		if (echo > 0 && source.getSource() != p) addEnergy(p, 8 * echo);
		int vanish = rank(p, "vanish_on_kill");
		if (vanish > 0) {
			effect(p, StatusEffects.SPEED, 60, vanish >= 3 ? 1 : 0);
			effect(p, StatusEffects.INVISIBILITY, 20 * vanish, 0);
		}
		int flurry = rank(p, "flurry");
		if (flurry > 0) effect(p, StatusEffects.HASTE, 80, flurry >= 3 ? 1 : 0);
		int harvest = rank(p, "soul_harvest");
		if (harvest > 0) {
			s.souls = Math.min(2 + harvest, s.souls + 1);
			s.lastSoulGain = now(p);
			p.getServerWorld().spawnParticles(ParticleTypes.SOUL, victim.getX(), victim.getBodyY(0.5), victim.getZ(),
					6, 0.3, 0.4, 0.3, 0.02);
		}
		int bounty = rank(p, "bounty");
		if (bounty > 0 && victim instanceof Monster && p.getRandom().nextFloat() < 0.05f * bounty) {
			victim.dropStack(new ItemStack(Items.EMERALD));
		}
		int peace = rank(p, "inner_peace");
		if (peace > 0) {
			p.getHungerManager().add(2, 0.5f);
			p.heal(peace);
		}
		int resonant = rank(p, "resonant_kill");
		if (resonant > 0) addEnergy(p, 8 * resonant);
		int devour = rank(p, "devour");
		if (devour > 0) {
			p.heal(devour);
			p.getHungerManager().add(1, 0.3f);
		}
		sync(p);
	}

	/** Nigromante – Estallido de cadáver: al morir un esbirro, daña a los enemigos cercanos. */
	public static void onSummonDeath(LivingEntity summon) {
		ServerPlayerEntity owner = AbilityState.ownerOf(summon);
		if (owner == null || !AbilityState.isMinion(summon)) return;
		int burst = rank(owner, "corpse_burst");
		if (burst <= 0) return;
		for (LivingEntity e : summon.getWorld().getEntitiesByClass(LivingEntity.class, summon.getBoundingBox().expand(3),
				e -> e != summon && Targeting.isHostileTo(owner, e))) {
			e.damage(owner.getDamageSources().indirectMagic(owner, owner), 2 + burst);
		}
		owner.getServerWorld().spawnParticles(ParticleFx.dust(0x8B0000, 1.5f), summon.getX(), summon.getBodyY(0.5),
				summon.getZ(), 30, 0.8, 0.6, 0.8, 0);
		summon.getWorld().playSound(null, summon.getX(), summon.getY(), summon.getZ(), SoundEvents.ENTITY_SLIME_SQUISH,
				SoundCategory.PLAYERS, 1.0f, 0.6f);
	}

	// ------------------------------------------------------- Habilidades

	/** Tras lanzar una habilidad de rol (activa, característica o definitiva). */
	public static void onCast(ServerPlayerEntity p, Role role, int energyCost) {
		State s = state(p);
		// Mago – Cargas arcanas: cada (6 − rango) hechizos de Mago, el siguiente devuelve su coste.
		int charges = rank(p, "arcane_charges");
		if (role == Role.MAGE && charges > 0) {
			int needed = Math.max(2, 6 - charges);
			if (s.mageCharges >= needed) {
				s.mageCharges = 0;
				addEnergy(p, energyCost);
				p.sendMessage(Text.translatable("aptitudes.mech.free_cast").withColor(0xB388FF), true);
				ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.1, 0), 1.2, ParticleTypes.WITCH, 16);
			} else {
				s.mageCharges++;
			}
		}
		// Ingeniero – Sobrecarga: +25 de calor por habilidad; a 100 entras en modo potenciado.
		int overdrive = rank(p, "overdrive");
		if (role == Role.ENGINEER && overdrive > 0 && s.overdriveUntil <= now(p)) {
			s.heat += 25;
			if (s.heat >= 100) {
				s.heat = 0;
				s.overdriveUntil = now(p) + (4 + 2 * overdrive) * 20;
				s.overheated = true;
				p.sendMessage(Text.translatable("aptitudes.mech.overdrive").withColor(0xFF9800), true);
				ParticleFx.spiral(p.getServerWorld(), p.getPos(), 0.8, 2.2, ParticleTypes.ELECTRIC_SPARK, 40, 3);
				sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.8f);
			}
		}
		sync(p);
	}

	/** Elementalista – Reacción: cambiar de elemento abre una ventana de 5 s. */
	public static void onElementSwitch(ServerPlayerEntity p) {
		if (rank(p, "elemental_reaction") <= 0) return;
		state(p).reactionUntil = now(p) + 100;
		sync(p);
		ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.1, 0), 1.0, ParticleTypes.ELECTRIC_SPARK, 14);
	}

	// ---------------------------------------------------------------- Tick

	/** Cada segundo. */
	public static void tick(MinecraftServer server) {
		int now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (!p.isAlive() || p.isSpectator()) continue;
			State s = state(p);
			// Bersérker – el frenesí se pierde a los 3 s sin golpear.
			if (now - s.lastHit > 60) s.frenzy = 0;
			// Lancero – Ímpetu: segundos esprintando.
			s.sprintSeconds = p.isSprinting() ? s.sprintSeconds + 1 : 0;
			// Herbolista – Floración: 3 s quieto.
			int bloom = rank(p, "bloom");
			boolean still = p.getPos().squaredDistanceTo(s.lastPos) < 0.01;
			s.stillSeconds = still ? s.stillSeconds + 1 : 0;
			s.lastPos = p.getPos();
			if (bloom > 0 && s.stillSeconds >= 3) {
				for (PlayerEntity ally : p.getWorld().getEntitiesByClass(PlayerEntity.class,
						p.getBoundingBox().expand(2 + bloom), PlayerEntity::isAlive)) {
					ally.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 50, 0, true, true));
				}
				if (s.stillSeconds % 2 == 1) {
					ParticleFx.ring(p.getServerWorld(), p.getPos().add(0, 0.1, 0), 2 + bloom, ParticleTypes.HAPPY_VILLAGER, 20);
				}
			}
			// Herbolista – Antídoto: probabilidad de quitar un efecto negativo.
			int antidote = rank(p, "antidote");
			if (antidote > 0 && p.getRandom().nextFloat() < 0.15f * antidote) {
				for (StatusEffectInstance effect : new ArrayList<>(p.getStatusEffects())) {
					if (effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) {
						p.removeStatusEffect(effect.getEffectType());
						particles(p, p, ParticleTypes.HAPPY_VILLAGER, 6);
						break;
					}
				}
			}
			// Ingeniero – Sobrecarga: Prisa II durante el modo; Lentitud al terminar.
			if (s.overdriveUntil > now) {
				effect(p, StatusEffects.HASTE, 30, 1);
			} else if (s.overheated) {
				s.overheated = false;
				effect(p, StatusEffects.SLOWNESS, 60, 0);
				p.getServerWorld().spawnParticles(ParticleTypes.SMOKE, p.getX(), p.getBodyY(0.7), p.getZ(), 15, 0.3, 0.3, 0.3, 0.02);
			}
			// Ingeniero – Reparación de campo: cada 20 s.
			int repair = rank(p, "field_repair");
			if (repair > 0 && now % 400 < 20) {
				for (EquipmentSlot slot : EquipmentSlot.values()) {
					ItemStack stack = p.getEquippedStack(slot);
					if (stack.isDamageable() && stack.getDamage() > 0) stack.setDamage(Math.max(0, stack.getDamage() - 2 * repair));
				}
			}
			// Paladín – Intercesión: cura a un aliado en apuros (cada 30 s).
			int intercession = rank(p, "intercession");
			if (intercession > 0) {
				for (PlayerEntity ally : Targeting.playersAround(p, 8)) {
					if (ally == p || ally.getHealth() >= ally.getMaxHealth() * 0.3f) continue;
					if (!ready(p, s, "intercession", 600)) break;
					ally.heal(2 + 2 * intercession);
					Targeting.particleLine(p.getServerWorld(), ParticleTypes.END_ROD, p.getEyePos(), ally.getEyePos());
					sound(p, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f);
					break;
				}
			}
			// Nigromante – las almas se disipan de una en una cada 15 s.
			if (s.souls > 0 && now - s.lastSoulGain > 300) {
				s.souls--;
				s.lastSoulGain = now;
			}
			// Druida – Ciclo (de día): cura 0,5 × rango cada 3 s.
			int cycle = rank(p, "day_night_cycle");
			if (cycle > 0 && p.getWorld().isDay() && now % 60 < 20) p.heal(0.5f * cycle);
			// Mercader – Interés compuesto: suerte por cada 10 esmeraldas.
			int interest = rank(p, "compound_interest");
			int stacks = interest > 0 ? Math.min(2 + interest, p.getInventory().count(Items.EMERALD) / 10) : 0;
			AttributeApplier.set(p, EntityAttributes.GENERIC_LUCK, MERCHANT_LUCK, stacks, Operation.ADD_VALUE);
			// Cambiaformas – Instinto: +1 acumulación cada 10 s transformado.
			int instinct = rank(p, "instinct");
			if (instinct > 0 && dev.aptitudes.morph.MorphManager.currentForm(p) != null) {
				if (now % 200 < 20) s.instinct = Math.min(2 + instinct, s.instinct + 1);
			} else {
				s.instinct = 0;
			}
			sync(p);
		}
	}

	// ------------------------------------------------------------ Utilidades

	private static void addEnergy(ServerPlayerEntity p, double amount) {
		RoleData data = RoleManager.get(p);
		data.setEnergy(Math.min(AbilityManager.maxEnergy(p), data.energy() + amount));
		AbilityManager.sendEnergy(p, false);
	}

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier), true, true));
	}

	private static void particles(ServerPlayerEntity p, Entity at, net.minecraft.particle.ParticleEffect particle, int count) {
		p.getServerWorld().spawnParticles(particle, at.getX(), at.getBodyY(0.6), at.getZ(), count, 0.3, 0.4, 0.3, 0.05);
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 0.8f, pitch);
	}
}
