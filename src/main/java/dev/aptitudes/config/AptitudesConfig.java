package dev.aptitudes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.aptitudes.Aptitudes;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuración editable en {@code config/aptitudes.json}.
 *
 * <p>Se usa Gson (incluido en Minecraft) para no añadir dependencias. Los campos
 * que falten en el JSON conservan su valor por defecto y el archivo se reescribe
 * para incorporar campos nuevos tras una actualización del mod.</p>
 */
public class AptitudesConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = Aptitudes.MOD_ID + ".json";

	// ---------- Progresión ----------
	public int maxSkillLevel = 50;
	/** XP para pasar del nivel L al L+1 = xpBase * (L+1)^xpExponent. */
	public double xpBase = 20.0;
	public double xpExponent = 1.35;
	/** Puntos de atributo por nivel de habilidad (0: los atributos solo se suben en la Hoguera, estilo Souls). */
	public int pointsPerSkillLevel = 0;
	public int startingPoints = 0;
	public int maxPointsPerAttribute = 99;
	public boolean allowSelfRespec = false;

	// ---------- Comportamiento ----------
	public boolean awardXpInCreative = false;
	public boolean awardXpForPlayerKills = true;
	public boolean showXpInActionBar = true;
	public boolean levelUpMessages = true;

	// ---------- Fuentes de XP ----------
	public int miningXpStone = 1;
	public int miningXpOre = 10;
	public int excavationXpBlock = 1;
	public int woodcuttingXpLog = 4;
	public int farmingXpCrop = 5;
	public double combatXpPerMaxHealth = 0.5;
	public double archeryXpPerMaxHealth = 0.6;
	public double defenseXpPerDamage = 1.0;
	public int agilityXpPer10Blocks = 2;
	/** Pesca: XP por cada pez capturado (estadística vanilla FISH_CAUGHT; basura y tesoros no cuentan). */
	public int fishingXpCatch = 12;
	/** Encantamiento: XP por cada objeto encantado en la mesa (estadística vanilla ENCHANT_ITEM). */
	public int enchantingXpPerEnchant = 25;

	// ---------- Rendimiento decreciente por fuente repetida (anti-granjas / anti-AFK) ----------
	/** Si está activo, Combate/Tiro con arco (por tipo de criatura), Defensa (por tipo de atacante) y Pesca pierden XP al repetir la misma fuente. */
	public boolean xpDiminishingEnabled = true;
	/** Ventana deslizante en segundos. */
	public int xpDiminishingWindowSeconds = 60;
	/** Eventos de la misma fuente dentro de la ventana que dan XP completa. */
	public int xpDiminishingThreshold = 20;
	/** XP mínima (fracción) por evento aunque se repita mucho la fuente. */
	public double xpDiminishingFloor = 0.1;

	// ---------- Bonificaciones pasivas por nivel de habilidad ----------
	/** Minería/Excavación/Tala/Agricultura: +X velocidad de rotura por nivel (0.02 = +2 %). */
	public double breakSpeedPerLevel = 0.02;
	public double farmingExtraDropChancePerLevel = 0.01;
	public double combatDamagePerLevel = 0.01;
	public double archeryDamagePerLevel = 0.015;
	public double defenseToughnessPerLevel = 0.1;
	public double agilitySpeedPerLevel = 0.002;
	public double agilitySafeFallPerLevel = 0.06;
	public double miningArmorPerLevel = 0.1;
	public double excavationKnockbackPerLevel = 0.01;
	public double woodcuttingHealthPerLevel = 0.2;
	public double farmingLuckPerLevel = 0.04;
	public double defenseArmorPerLevel = 0.06;
	public double defenseExplosionPerLevel = 0.01;
	/** Pesca: +suerte por nivel (afecta al botín de pesca vanilla). */
	public double fishingLuckPerLevel = 0.02;
	/** Pesca: probabilidad de captura doble por nivel (0.004 = 20 % a nivel 50). */
	public double fishingDoubleCatchPerLevel = 0.004;
	/** Encantamiento: probabilidad por nivel de recuperar 1 nivel de XP al encantar (0.01 = 50 % a nivel 50). */
	public double enchantingRefundChancePerLevel = 0.01;
	/** Encantamiento: +maná máximo por nivel (0.4 = +20 a nivel 50). */
	public double enchantingManaPerLevel = 0.4;

	// ---------- Valor de cada punto de atributo ----------
	// v27: −30 % respecto a los valores anteriores (1,0 / 5 / 0,5 / 0,25 / 0,01 / 0,02 / 0,01 / 0,2) para que los
	// atributos altos no rompan el balance. OJO: un config/aptitudes.json ya creado conserva sus valores.
	public double vigorHealthPerPoint = 0.7;
	public double mindManaPerPoint = 3.5;
	public double enduranceArmorPerPoint = 0.35;
	public double strengthDamagePerPoint = 0.175;
	public double dexterityAttackSpeedPerPoint = 0.007;
	/** Inteligencia: +X de daño de hechizos por punto (0.014 = +1,4 %). */
	public double intelligenceSpellDamagePerPoint = 0.014;
	/** Fe: −X de enfriamiento por punto (0.007 = −0,7 %). La reducción total (Fe + Mente clara) se limita al 60 %. */
	public double faithCooldownPerPoint = 0.007;
	public double luckPerPoint = 0.14;

	// ---------- Roles ----------
	public int maxRoleLevel = 30;
	public double roleXpBase = 30.0;
	public double roleXpExponent = 1.4;
	/** El rol secundario recibe esta fracción de la XP de rol. */
	public double secondaryRoleXpMultiplier = 0.5;
	/** Puntos de árbol de un rol = 1 + nivel / rolePointsEveryLevels. */
	public int rolePointsEveryLevels = 3;
	// ---------- Combos (v28) ----------
	public boolean combosEnabled = true;
	/** Segundos máximos entre golpes para mantener el combo. */
	public double comboWindowSeconds = 2.5;
	/** Daño extra por golpe de combo (0.03 = +3 %). */
	public double comboDamagePerHit = 0.03;
	/** Tope de la bonificación de combo (0.15 = +15 %). */
	public double comboMaxBonus = 0.15;
	/** Golpes necesarios para que el siguiente sea un remate. */
	public int comboFinisherHits = 5;
	/** Espera mínima entre remates, en segundos. */
	public double comboFinisherCooldownSeconds = 4.0;

	/** v23: puntos de árbol extra al evolucionar a Maestro (para la fila nueva de 5 nodos × 2 rangos). */
	public int masterBonusPoints = 10;
	/** v23: puntos de árbol extra al evolucionar a Definitivo (fila nueva de 5 nodos × 1 rango). */
	public int ultimateBonusPoints = 5;
	/** Obsoleto: ya no se usa (el cambio de rol se hace con el ritual de la Hoguera). */
	public int roleChangeCooldownMinutes = 0;
	/**
	 * Escalado de los puntos de atributo: LINEAR (por defecto, comportamiento original), SOFTCAP (lineal hasta
	 * {@link #attributeSoftCapFirst}, 50 % hasta {@link #attributeSoftCapSecond}, 20 % después) o SATURATION
	 * (L·(1 − e^(−p/L)) con L = {@link #attributeSaturationPoints}). Ver docs/BALANCE.md.
	 */
	public String attributeScaling = "LINEAR";
	/** Tope del multiplicador de daño combinado de las fuentes del mod (×). 0 desactiva el tope. */
	public double maxDamageMultiplier = 6.0;
	public int attributeSoftCapFirst = 40;
	public int attributeSoftCapSecond = 60;
	public double attributeSaturationPoints = 50.0;

	/**
	 * Legendarias exclusivas: si es true, cada legendaria solo puede tenerla UN jugador del servidor y cada
	 * jugador solo una (regla de las antiguas "únicas"). Por defecto false (decisión de diseño de la v12).
	 */
	public boolean legendaryExclusive = false;
	/** Probabilidad de que un monstruo aparezca como élite (el 10 % de esas, campeón). 0 lo desactiva. */
	public double eliteChance = 0.05;
	/** Sustituir un rol ya elegido exige quemar una Ofrenda de renacimiento en una Hoguera. */
	public boolean roleChangeRequiresRitual = true;
	public int roleXpPerAbilityUse = 5;
	public double roleXpPerDamage = 0.5;
	public int roleXpPerAction = 2;

	// ---------- Energía (recurso de habilidades activas) ----------
	public double baseMaxEnergy = 100.0;
	public double baseEnergyRegenPerSecond = 2.0;

	// ---------- Subida de nivel estilo Souls (Hoguera) ----------
	/** Niveles de XP vanilla que cuesta el nivel N: base + N / divisor (con tope). */
	public int levelCostBase = 1;
	public int levelCostDivisor = 6;
	public int levelCostMax = 10;
	/** Cada cuántos niveles de alma se gana 1 punto de atributo. */
	public int soulLevelsPerPoint = 3;
	/** Bonificación de los atributos afines del rol principal / secundario (0.5 = +50 %). */
	public double primaryAffinityBonus = 0.5;
	public double secondaryAffinityBonus = 0.25;
	/** Al morir, toda la XP queda en una "mancha" recuperable en el lugar de la muerte. */
	public boolean soulsBloodstain = true;

	// ---------- Efectos visuales ----------
	/** Números de daño flotantes para el atacante. */
	public boolean damageNumbers = true;
	/** Títulos en pantalla al subir de nivel una habilidad o un rol. */
	public boolean levelUpTitles = true;

	// ---------- Cambiaformas y habilidades únicas ----------
	/** Las explosiones de la forma Creeper rompen bloques (trolleo sin límites). */
	public boolean morphExplosionsBreakBlocks = true;
	/** Las habilidades únicas con efectos de fuego pueden prender bloques. */
	public boolean uniqueGriefing = true;
	/** Una habilidad única se libera si su dueño no se conecta en estos días (0 = nunca). */
	public int uniqueReleaseAfterDays = 0;

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static AptitudesConfig load() {
		Path path = path();
		AptitudesConfig cfg = new AptitudesConfig();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				AptitudesConfig parsed = GSON.fromJson(reader, AptitudesConfig.class);
				if (parsed != null) {
					cfg = parsed;
				}
			} catch (IOException | JsonParseException e) {
				// No sobrescribimos un archivo roto: el usuario perdería sus cambios.
				Aptitudes.LOGGER.error("No se pudo leer {}; se usan valores por defecto", path, e);
				return validated(new AptitudesConfig());
			}
		}
		cfg = validated(cfg);
		save(cfg);
		return cfg;
	}

	public static void save(AptitudesConfig cfg) {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(cfg, writer);
			}
		} catch (IOException e) {
			Aptitudes.LOGGER.error("No se pudo guardar {}", path, e);
		}
	}

	/**
	 * Corrige valores fuera de rango para que el juego nunca entre en un estado inválido.
	 * Visible en el paquete para las pruebas unitarias.
	 */
	static AptitudesConfig validated(AptitudesConfig c) {
		// 1) Validación general: en esta configuración ningún número tiene sentido negativo, NaN o infinito.
		//    Un valor así vuelve al predeterminado y se avisa en el log con el nombre del campo.
		AptitudesConfig defaults = new AptitudesConfig();
		for (java.lang.reflect.Field f : AptitudesConfig.class.getFields()) {
			if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
			try {
				if (f.getType() == double.class) {
					double v = f.getDouble(c);
					if (!Double.isFinite(v) || v < 0) {
						Aptitudes.LOGGER.warn("[Aptitudes] Config '{}' inválido ({}); se usa el valor por defecto {}",
								f.getName(), v, f.getDouble(defaults));
						f.setDouble(c, f.getDouble(defaults));
					}
				} else if (f.getType() == int.class) {
					int v = f.getInt(c);
					if (v < 0) {
						Aptitudes.LOGGER.warn("[Aptitudes] Config '{}' inválido ({}); se usa el valor por defecto {}",
								f.getName(), v, f.getInt(defaults));
						f.setInt(c, f.getInt(defaults));
					}
				}
			} catch (IllegalAccessException ignored) {
				// los campos son públicos: no ocurre
			}
		}
		// 2) Límites específicos
		c.eliteChance = Math.min(1.0, c.eliteChance);
		c.primaryAffinityBonus = Math.min(5.0, c.primaryAffinityBonus);
		c.secondaryAffinityBonus = Math.min(5.0, c.secondaryAffinityBonus);
		c.farmingExtraDropChancePerLevel = Math.min(1.0, c.farmingExtraDropChancePerLevel);
		c.fishingDoubleCatchPerLevel = Math.min(1.0, c.fishingDoubleCatchPerLevel);
		c.enchantingRefundChancePerLevel = Math.min(1.0, c.enchantingRefundChancePerLevel);
		c.enchantingManaPerLevel = Math.min(10.0, c.enchantingManaPerLevel);
		c.xpDiminishingWindowSeconds = clamp(c.xpDiminishingWindowSeconds, 1, 86_400);
		c.xpDiminishingThreshold = clamp(c.xpDiminishingThreshold, 1, 100_000);
		c.xpDiminishingFloor = Math.min(1.0, c.xpDiminishingFloor);
		c.levelCostDivisor = Math.max(1, c.levelCostDivisor);
		c.levelCostMax = Math.max(1, c.levelCostMax);
		c.soulLevelsPerPoint = Math.max(1, c.soulLevelsPerPoint);
		if (dev.aptitudes.logic.AttributeScaling.Mode.parse(c.attributeScaling) == null) {
			Aptitudes.LOGGER.warn("[Aptitudes] Config 'attributeScaling' inválido ({}); válidos: LINEAR, SOFTCAP, SATURATION. Se usa LINEAR",
					c.attributeScaling);
			c.attributeScaling = "LINEAR";
		}
		c.attributeSoftCapSecond = Math.max(c.attributeSoftCapFirst, c.attributeSoftCapSecond);
		c.attributeSaturationPoints = Math.max(1.0, c.attributeSaturationPoints);
		c.maxSkillLevel = clamp(c.maxSkillLevel, 1, 1000);
		c.xpBase = Math.max(1.0, c.xpBase);
		c.xpExponent = Math.max(0.0, Math.min(4.0, c.xpExponent));
		c.pointsPerSkillLevel = clamp(c.pointsPerSkillLevel, 0, 100);
		c.startingPoints = clamp(c.startingPoints, 0, 10_000);
		c.maxPointsPerAttribute = clamp(c.maxPointsPerAttribute, 0, 10_000);
		c.breakSpeedPerLevel = Math.max(0.0, c.breakSpeedPerLevel);
		c.farmingExtraDropChancePerLevel = Math.max(0.0, c.farmingExtraDropChancePerLevel);
		c.maxRoleLevel = clamp(c.maxRoleLevel, 1, 1000);
		c.roleXpBase = Math.max(1.0, c.roleXpBase);
		c.roleXpExponent = Math.max(0.0, Math.min(4.0, c.roleXpExponent));
		c.secondaryRoleXpMultiplier = Math.max(0.0, Math.min(1.0, c.secondaryRoleXpMultiplier));
		c.rolePointsEveryLevels = clamp(c.rolePointsEveryLevels, 1, 100);
		c.masterBonusPoints = clamp(c.masterBonusPoints, 0, 100);
		c.comboWindowSeconds = Math.max(0.25, Math.min(30.0, c.comboWindowSeconds));
		c.comboDamagePerHit = Math.min(1.0, c.comboDamagePerHit);
		c.comboMaxBonus = Math.min(2.0, c.comboMaxBonus);
		c.comboFinisherHits = clamp(c.comboFinisherHits, 2, 50);
		c.comboFinisherCooldownSeconds = Math.min(600.0, c.comboFinisherCooldownSeconds);
		c.ultimateBonusPoints = clamp(c.ultimateBonusPoints, 0, 100);
		c.roleChangeCooldownMinutes = clamp(c.roleChangeCooldownMinutes, 0, 100_000);
		c.baseMaxEnergy = Math.max(1.0, c.baseMaxEnergy);
		c.baseEnergyRegenPerSecond = Math.max(0.0, c.baseEnergyRegenPerSecond);
		return c;
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
