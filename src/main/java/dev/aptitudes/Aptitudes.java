package dev.aptitudes;

import dev.aptitudes.command.AptitudesCommand;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.command.RoleCommand;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.logic.XpEvents;
import dev.aptitudes.network.ModNetworking;
import dev.aptitudes.role.RoleEvents;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada común (cliente y servidor).
 *
 * <p>Todo lo que se registra aquí existe en ambos lados. La lógica de juego
 * (XP, atributos, persistencia) se ejecuta solo en el servidor lógico; el
 * cliente únicamente recibe una copia de los datos para dibujar la GUI y para
 * predecir la velocidad de minado.</p>
 */
public class Aptitudes implements ModInitializer {
	public static final String MOD_ID = "aptitudes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static volatile AptitudesConfig config = new AptitudesConfig();

	public static AptitudesConfig config() {
		return config;
	}

	public static void reloadConfig() {
		config = AptitudesConfig.load();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		reloadConfig();
		dev.aptitudes.block.ModBlocks.init();
		dev.aptitudes.sound.ModSounds.init();
		ModItems.init();
		dev.aptitudes.world.ModWorldGen.init();
		dev.aptitudes.world.GuildVillagers.init();
		dev.aptitudes.world.GuildCrafting.init();
		dev.aptitudes.accessory.Accessories.init();
		dev.aptitudes.elite.Elites.register();
		dev.aptitudes.boss.BossManager.register();
		ModAttachments.init();
		ModNetworking.registerCommon();
		dev.aptitudes.logic.PlacementTracker.register();
		dev.aptitudes.world.TemporaryBlocks.register();
		dev.aptitudes.ability.BrainTargetFilter.register();
		dev.aptitudes.ability.Combos.register();
		XpEvents.register();
		RoleEvents.register();
		dev.aptitudes.unique.UniqueEffects.register();
		// Al final: su ALLOW_DEATH solo debe actuar si ningún otro evitó la muerte.
		dev.aptitudes.souls.SoulsManager.register();
		ProgressManager.registerLifecycleEvents();
		RoleManager.registerLifecycleEvents();
		dev.aptitudes.rune.RuneSocketing.register();
		dev.aptitudes.event.EclipseManager.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			AptitudesCommand.register(dispatcher);
			RoleCommand.register(dispatcher);
			dev.aptitudes.command.UniqueCommand.register(dispatcher);
		});
		LOGGER.info("Aptitudes inicializado");
	}
}
