package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Element;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Fusiones elementales del Elementalista. Si dominas los DOS elementos de una pareja y
 * lanzas el Estallido elemental con uno de ellos activo, el estallido añade el efecto de
 * la fusión. Las fusiones no cuestan nada extra: premian elegir bien tus 3 elementos.
 */
public enum ElementFusions {
	FIRESTORM(Element.FIRE, Element.WIND),         // Tormenta ígnea
	ELECTROCUTION(Element.WATER, Element.LIGHTNING), // Electrocución
	BLIZZARD(Element.ICE, Element.WATER),          // Ventisca
	ERUPTION(Element.EARTH, Element.MAGMA),        // Erupción
	PLAGUE(Element.PLANT, Element.POISON),         // Plaga
	ECLIPSE(Element.LIGHT, Element.SHADOW),        // Eclipse
	CRYSTAL_SINGULARITY(Element.VOID, Element.CRYSTAL), // Singularidad cristalina
	RESONANCE(Element.SOUND, Element.WIND),        // Resonancia
	PRISM(Element.LIGHTNING, Element.CRYSTAL),     // Prisma
	THERMAL_SHOCK(Element.FIRE, Element.ICE);      // Choque térmico

	private final Element a;
	private final Element b;

	ElementFusions(Element a, Element b) {
		this.a = a;
		this.b = b;
	}

	public Element first() { return a; }
	public Element second() { return b; }

	public boolean involves(Element e) {
		return e == a || e == b;
	}

	public String translationKey() {
		return "fusion." + Aptitudes.MOD_ID + "." + name().toLowerCase(Locale.ROOT);
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	/** ¿Están desbloqueadas (domina ambos elementos)? */
	public boolean unlocked(List<Element> mastered) {
		return mastered.contains(a) && mastered.contains(b);
	}

	/** Aplica las fusiones del elemento activo tras un Estallido. */
	static void apply(ServerPlayerEntity p, Element active, int r, List<LivingEntity> hostiles) {
		RoleData data = RoleManager.get(p);
		List<Element> mastered = data.elements();
		ServerWorld world = p.getServerWorld();
		DamageSource source = p.getDamageSources().indirectMagic(p, p);
		for (ElementFusions f : values()) {
			if (!f.involves(active) || !f.unlocked(mastered)) continue;
			switch (f) {
				case FIRESTORM -> hostiles.forEach(e -> {
					e.setOnFireFor(4);
					e.damage(source, 2 + r);
				});
				case ELECTROCUTION -> hostiles.forEach(e -> {
					e.damage(source, 3 + r);
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 4)); // aturdido
				});
				case BLIZZARD -> hostiles.forEach(e -> {
					e.setFrozenTicks(e.getMinFreezeDamageTicks() + 100);
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
				});
				case ERUPTION -> hostiles.forEach(e -> {
					e.addVelocity(0, 0.8, 0);
					e.velocityModified = true;
					e.setOnFireFor(3);
				});
				case PLAGUE -> hostiles.forEach(e -> {
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 120, 1));
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
				});
				case ECLIPSE -> {
					hostiles.forEach(e -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0)));
					p.heal(4);
				}
				case CRYSTAL_SINGULARITY -> hostiles.forEach(e -> {
					Vec3d pull = p.getPos().subtract(e.getPos()).normalize().multiply(1.2);
					e.addVelocity(pull.x, 0.1, pull.z);
					e.velocityModified = true;
					e.damage(source, 4);
				});
				case RESONANCE -> hostiles.forEach(e -> {
					e.takeKnockback(2.5, p.getX() - e.getX(), p.getZ() - e.getZ());
					e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0));
				});
				case PRISM -> hostiles.stream().sorted(Comparator.comparingDouble(p::squaredDistanceTo)).limit(3)
						.forEach(e -> {
							Targeting.particleLine(world, ParticleTypes.GLOW, p.getEyePos(), e.getEyePos());
							e.damage(source, 4 + r);
						});
				case THERMAL_SHOCK -> hostiles.forEach(e -> {
					e.damage(source, 3 + r);
					world.spawnParticles(ParticleTypes.CLOUD, e.getX(), e.getBodyY(0.6), e.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
				});
			}
			// Aviso y efecto visual de la fusión (mezcla de los dos colores)
			ParticleFx.ring(world, p.getPos().add(0, 0.2, 0), 3.5, ParticleFx.dust(f.a.color(), 1.3f), 28);
			ParticleFx.ring(world, p.getPos().add(0, 0.6, 0), 3.0, ParticleFx.dust(f.b.color(), 1.3f), 24);
			world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
					SoundCategory.PLAYERS, 0.8f, 1.6f);
			p.sendMessage(Text.translatable("aptitudes.fusion.triggered", Text.translatable(f.translationKey()))
					.withColor(f.a.color()), true);
		}
	}
}
