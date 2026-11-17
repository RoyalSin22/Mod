package dev.aptitudes.client.fx;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;

/**
 * V77: trozo de bloque con física propia. Cae con la gravedad que se le da, rebota al chocar con el suelo perdiendo
 * energía, gira en el aire, se frena al rodar y se encoge al final de su vida. Usa la textura real del bloque del suelo.
 */
public final class ChunkParticle extends SpriteBillboardParticle {
	private final float spin;
	private final float bounceFactor;
	private final float sampleU;
	private final float sampleV;
	private final float baseScale;
	private int bouncesLeft;

	public ChunkParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, BlockState state,
						 float scale, int life, float gravity, float bounce) {
		super(world, x, y, z, vx, vy, vz);
		// El constructor de Particle perturba la velocidad al azar: aquí se fija la exacta.
		this.velocityX = vx;
		this.velocityY = vy;
		this.velocityZ = vz;
		this.setSprite(MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(state));
		this.gravityStrength = gravity;
		this.collidesWithWorld = true;
		this.baseScale = scale;
		this.scale = scale;
		this.maxAge = life;
		float shade = 0.5f + this.random.nextFloat() * 0.25f;
		this.setColor(shade, shade, shade);
		this.angle = this.random.nextFloat() * (float) Math.PI * 2f;
		this.prevAngle = this.angle;
		this.spin = (this.random.nextFloat() - 0.5f) * 0.9f;
		this.bounceFactor = bounce;
		this.bouncesLeft = bounce > 0 ? 3 : 0;
		this.sampleU = this.random.nextFloat() * 3f;
		this.sampleV = this.random.nextFloat() * 3f;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.TERRAIN_SHEET;
	}

	@Override
	public void tick() {
		double before = this.velocityY;
		this.prevAngle = this.angle;
		this.angle += this.onGround ? this.spin * 0.15f : this.spin;
		super.tick();
		if (this.dead || !this.onGround) return;
		if (before < -0.12 && this.bouncesLeft > 0) {
			// Impacto real: rebota con parte de la energía y pierde empuje lateral.
			this.velocityY = -before * this.bounceFactor;
			this.velocityX *= 0.8;
			this.velocityZ *= 0.8;
			this.bouncesLeft--;
		} else {
			// Apoyado: Minecraft no anula la velocidad vertical y se acumularía como falsos rebotes.
			this.velocityY = 0;
		}
	}

	@Override
	public float getSize(float tickDelta) {
		float life = (this.age + tickDelta) / this.maxAge;
		return life < 0.7f ? this.baseScale : this.baseScale * Math.max(0f, (1f - life) / 0.3f);
	}

	@Override
	protected float getMinU() {
		return this.sprite.getFrameU(this.sampleU / 4f);
	}

	@Override
	protected float getMaxU() {
		return this.sprite.getFrameU((this.sampleU + 1f) / 4f);
	}

	@Override
	protected float getMinV() {
		return this.sprite.getFrameV(this.sampleV / 4f);
	}

	@Override
	protected float getMaxV() {
		return this.sprite.getFrameV((this.sampleV + 1f) / 4f);
	}
}
