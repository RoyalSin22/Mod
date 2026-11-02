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
	/** Escala del HUD de habilidades (0,5 – 2). */
	public float hudScale = 1.0f;
	/** Desplazamiento de la barra de habilidades respecto a su posición por defecto (píxeles de GUI). */
	public int hudOffsetX = 0;
	public int hudOffsetY = 0;
	/** Barras de vida/maná/aguante largas arriba a la izquierda (estilo Souls) en lugar de junto a la hotbar. */
	public boolean soulsBars = true;
	/** Indicador de la mecánica del rol (acumulaciones, estados) sobre la barra de habilidades. */
	public boolean showMechanic = true;
	/** Eclipse (v34): cielo, niebla y nubes teñidos de rojo y sol eclipsado. Desactívalo si los destellos molestan. */
	public boolean eclipseSkyEffects = true;

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
