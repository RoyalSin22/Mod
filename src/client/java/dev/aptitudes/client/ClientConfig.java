package dev.aptitudes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aptitudes.Aptitudes;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Preferencias solo del cliente ({@code config/aptitudes-client.json}): HUD, efectos de pantalla y números de daño. */
public final class ClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ClientConfig instance = new ClientConfig();

	public boolean hudVisible = true;
	/** Destellos de color y temblor de cámara al lanzar habilidades o recibir golpes fuertes. */
	public boolean screenEffects = true;
	/** Números de daño flotantes. */
	public boolean damageNumbers = true;

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(Aptitudes.MOD_ID + "-client.json");
	}

	public static ClientConfig get() {
		return instance;
	}

	public static void load() {
		try {
			if (Files.exists(path())) {
				try (Reader r = Files.newBufferedReader(path())) {
					ClientConfig loaded = GSON.fromJson(r, ClientConfig.class);
					if (loaded != null) instance = loaded;
				}
			}
		} catch (Exception e) {
			Aptitudes.LOGGER.warn("No se pudo leer la configuración del cliente", e);
		}
	}

	public static void save() {
		try (Writer w = Files.newBufferedWriter(path())) {
			GSON.toJson(instance, w);
		} catch (Exception e) {
			Aptitudes.LOGGER.warn("No se pudo guardar la configuración del cliente", e);
		}
	}
}
