package dev.aptitudes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.AbilityHudLayout;
import dev.aptitudes.logic.HudPreset;
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
	/** Remates geométricos de las habilidades en el mundo. */
	public Boolean castAnimations = true;
	/** Números de daño flotantes. */
	public boolean damageNumbers = true;
	/** Escala del HUD de habilidades (0,5 – 2). */
	public float hudScale = 1.0f;
	/** Desplazamiento de la barra de habilidades respecto a su posición por defecto (píxeles de GUI). */
	public int hudOffsetX = 0;
	public int hudOffsetY = 0;
	/** Barras de vida/maná/aguante largas arriba a la izquierda (estilo Souls) en lugar de junto a la hotbar. */
	public boolean soulsBars = true;
	/** v66: pantalla del árbol con las piezas de la dirección de arte (tres sendas en columnas). false: la vista anterior. */
	public boolean newTreeScreen = true;
	/** Indicador de la mecánica del rol (acumulaciones, estados) sobre la barra de habilidades. */
	public boolean showMechanic = true;
	/** Eclipse (v34): cielo, niebla y nubes teñidos de rojo y sol eclipsado. Desactívalo si los destellos molestan. */
	public boolean eclipseSkyEffects = true;
	/** Recordatorio del tutorial. Boolean permite recuperar de forma segura un null escrito a mano. */
	public Boolean showTutorial = true;
	/** Reduce avisos y adornos secundarios sin cambiar los interruptores individuales. */
	public Boolean hudMinimal = false;
	public Boolean hudAnimations = true;
	public String hudPreset = HudPreset.CLASSIC.id();
	/** v61: marcadores del grimorio ({@code chapter:…}, {@code item:…}, {@code beast:…}, {@code role:…}). */
	public java.util.List<String> guideBookmarks = new java.util.ArrayList<>();

	public HudPreset preset() {
		return HudPreset.fromId(hudPreset);
	}

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
		instance = validated(instance);
	}

	private static ClientConfig validated(ClientConfig config) {
		if (config == null) config = new ClientConfig();
		// Su dominio es booleano; un null explícito conserva el valor seguro por defecto.
		if (config.showTutorial == null) config.showTutorial = true;
		if (config.castAnimations == null) config.castAnimations = true;
		if (config.hudMinimal == null) config.hudMinimal = false;
		if (config.hudAnimations == null) config.hudAnimations = true;
		config.hudPreset = config.preset().id();
		config.guideBookmarks = dev.aptitudes.guide.GuideRules.sanitizeBookmarks(config.guideBookmarks);
		config.hudScale = AbilityHudLayout.validatedScale(config.hudScale);
		config.hudOffsetX = Math.clamp(config.hudOffsetX, -4096, 4096);
		config.hudOffsetY = Math.clamp(config.hudOffsetY, -4096, 4096);
		return config;
	}

	public static void save() {
		instance = validated(instance);
		try (Writer w = Files.newBufferedWriter(path())) {
			GSON.toJson(instance, w);
		} catch (Exception e) {
			Aptitudes.LOGGER.warn("No se pudo guardar la configuración del cliente", e);
		}
	}
}
