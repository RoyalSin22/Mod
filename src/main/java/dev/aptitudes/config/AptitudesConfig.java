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

	// ---------- Valor de cada punto de atributo ----------
	public double vigorHealthPerPoint = 1.0;
	public double mindManaPerPoint = 5.0;
	public double enduranceArmorPerPoint = 0.5;
	public double strengthDamagePerPoint = 0.25;
	public double dexterityAttackSpeedPerPoint = 0.01;
	/** Inteligencia: +X de daño de hechizos por punto (0.02 = +2 %). */
	public double intelligenceSpellDamagePerPoint = 0.02;
	/** Fe: −X de enfriamiento por punto (0.01 = −1 %). La reducción total (Fe + Mente clara) se limita al 60 %. */
	public double faithCooldownPerPoint = 0.01;
	public double luckPerPoint = 0.2;

	// ---------- Roles ----------
	public int maxRoleLevel = 30;
	public double roleXpBase = 30.0;
	public double roleXpExponent = 1.4;
	/** El rol secundario recibe esta fracción de la XP de rol. */
	public double secondaryRoleXpMultiplier = 0.5;
	/** Puntos de árbol de un rol = 1 + nivel / rolePointsEveryLevels. */
	public int rolePointsEveryLevels = 3;
	/** Obsoleto: ya no se usa (el cambio de rol se hace con el ritual de la Hoguera). */
	public int roleChangeCooldownMinutes = 0;
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

	/** Corrige valores fuera de rango para que el juego nunca entre en un estado inválido. */
	private static AptitudesConfig validated(AptitudesConfig c) {
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
		c.roleChangeCooldownMinutes = clamp(c.roleChangeCooldownMinutes, 0, 100_000);
		c.baseMaxEnergy = Math.max(1.0, c.baseMaxEnergy);
		c.baseEnergyRegenPerSecond = Math.max(0.0, c.baseEnergyRegenPerSecond);
		return c;
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
