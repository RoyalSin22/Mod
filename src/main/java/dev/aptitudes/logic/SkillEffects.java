package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.Skill;
import dev.aptitudes.network.SyncProgressPayload;
import dev.aptitudes.sync.ClientCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import org.jetbrains.annotations.Nullable;

/**
 * Efectos que deben calcularse igual en cliente y servidor.
 *
 * <p>La velocidad de rotura la calcula el cliente (barra de progreso) y la valida
 * el servidor. Si ambos no coinciden el bloque "reaparece". Por eso el cliente
 * usa los niveles y la regla {@code breakSpeedPerLevel} recibidos del servidor.</p>
 */
public final class SkillEffects {
	private SkillEffects() {
	}

	@Nullable
	public static Skill skillForBlock(BlockState state) {
		if (state.isIn(BlockTags.PICKAXE_MINEABLE)) return Skill.MINING;
		if (state.isIn(BlockTags.AXE_MINEABLE)) return Skill.WOODCUTTING;
		if (state.isIn(BlockTags.SHOVEL_MINEABLE)) return Skill.EXCAVATION;
		if (state.isIn(BlockTags.HOE_MINEABLE)) return Skill.FARMING;
		return null;
	}

	public static float breakSpeedMultiplier(PlayerEntity player, BlockState state) {
		Skill skill = skillForBlock(state);
		if (skill == null) return 1.0f;

		int level;
		double perLevel;
		double mastery = 1;
		if (player.getWorld().isClient()) {
			SyncProgressPayload data = ClientCache.get();
			if (data == null || !player.getUuid().equals(ClientCache.owner())) return 1.0f;
			level = data.level(skill);
			perLevel = data.rules().breakSpeedPerLevel();
			var view = dev.aptitudes.sync.ClientMastery.get();
			if (view != null && player.getUuid().equals(dev.aptitudes.sync.ClientMastery.owner()) && level >= data.rules().maxLevel()) {
				mastery += view.bonus(skill);
			}
		} else {
			PlayerProgress progress = player.getAttached(ModAttachments.PROGRESS);
			if (progress == null) return 1.0f;
			level = progress.getLevel(skill);
			perLevel = Aptitudes.config().breakSpeedPerLevel;
			mastery = MasteryManager.skillMultiplier(progress, skill, Aptitudes.config());
		}
		return (float) (1.0 + level * perLevel * mastery);
	}
}
