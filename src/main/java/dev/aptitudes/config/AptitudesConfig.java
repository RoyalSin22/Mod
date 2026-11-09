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
	/** v46: 50 (antes 30). Los archivos anteriores con 30 se migran a 50 ({@link ConfigMigration}). */
	public int maxRoleLevel = 50;
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
	// ---------- Grupos (v29) ----------
	public int partyMaxSize = 5;
	public double partyMaxXpShare = 0.5;
	public double partyXpBonusPerMember = 0.05;
	public double partyXpBonusMax = 0.20;

	// ---------- Eclipse (v34) ----------
	/** Evento del mundo: eclipse solar con oleadas, élites y el Heraldo del Eclipse. */
	public boolean eclipseEnabled = true;
	/** Cada cuántos días de juego hay eclipse. */
	public int eclipseIntervalDays = 10;
	/** Primer día en el que puede haber eclipse (margen para mundos nuevos). */
	public int eclipseFirstDay = 10;
	/** Segundos entre oleadas. */
	public int eclipseWaveIntervalSeconds = 30;
	/** Tope de criaturas del eclipse vivas alrededor de cada jugador. */
	public int eclipseMaxMobsPerPlayer = 12;
	/** Multiplicador de vida de las criaturas del eclipse (1,5 = +50 %). */
	public double eclipseMobHealthMultiplier = 1.5;
	/** Multiplicador de daño de las criaturas del eclipse (sujeto a maxDamageMultiplier). */
	public double eclipseMobDamageMultiplier = 1.5;
	/** Probabilidad de que una criatura del eclipse sea élite. */
	public double eclipseEliteChance = 0.5;
	/** Probabilidad de que sea campeón (se comprueba antes que la de élite). */
	public double eclipseChampionChance = 0.1;
	/** Aparición del Heraldo del Eclipse a mediodía. */
	public boolean eclipseHeraldEnabled = true;
	/** XP de rol principal y de Combate para quien sobrevive al eclipse sin morir. */
	public int eclipseSurvivorRoleXp = 300;
	public int eclipseSurvivorCombatXp = 200;
	/** v41.2: vida y daño extra en la Totalidad, sobre los multiplicadores anteriores (0,25 = +25 %). */
	public double eclipseTotalityBonus = 0.25;
	/** v41.2: puntos de armadura extra de cada criatura del eclipse (0 = como antes). */
	public double eclipseMobArmor = 4.0;
	/** v41.2: probabilidad de cada pieza de armadura visible (malla, hierro en la Totalidad, diamante los campeones). */
	public double eclipseArmorChance = 0.35;
	/** v47: tamaño de oleada por jugador; cero desactiva refuerzos de esa fase. */
	public int eclipsePenumbraWaveSize = 4;
	public int eclipseTotalityWaveSize = 8;
	/** Límites compartidos de criaturas cargadas; cero impide nuevas oleadas. */
	public int eclipseZoneMobCap = 24;
	public int eclipseWorldMobCap = 128;
	/** Mínimos por oleada exitosa; requieren que la probabilidad de ese grado sea mayor que cero. */
	public int eclipseMinElitesPerWave = 1;
	public int eclipseMinChampionsTotality = 1;
	/** Las élites y campeones usan humanoides con equipo completo que no se suelta al morir. */
	public boolean eclipseGuaranteedEliteArmor = true;
	// ---------- Eventos rotativos (v41) ----------
	// El calendario es el de eclipseIntervalDays / eclipseFirstDay: cada fecha toca un evento al azar, sin repetir
	// el anterior. El peso es la probabilidad relativa; 0 desactiva ese evento.
	public int worldEventWeightEclipse = 1;
	public int worldEventWeightSoulFog = 1;
	public int worldEventWeightEmberRain = 1;
	/** v41.2: probabilidad de meteorito grande y enorme en la Lluvia de brasas (el resto, pequeños como antes). */
	public double emberRainLargeChance = 0.25;
	public double emberRainHugeChance = 0.05;
	/** v41.2: probabilidad de que un impacto traiga criaturas del Nether (la mitad en los pequeños). */
	public double emberRainMobChance = 0.5;
	// ---------- Piedras angulares de las Sendas (v46.1) ----------
	/** Carnicería: vida curada al matar (fracción de la vida máxima) y segundos de Prisa II. */
	public double keystoneCarnageHeal = 0.10;
	public double keystoneCarnageHasteSeconds = 5.0;
	/** Baluarte: umbral de vida (fracción), puntos de absorción, duración y espera en segundos. */
	public double keystoneBulwarkThreshold = 0.30;
	public double keystoneBulwarkAbsorption = 8.0;
	public double keystoneBulwarkSeconds = 6.0;
	public double keystoneBulwarkCooldownSeconds = 60.0;
	/** Sobrecarga: cada cuántas habilidades llega una gratuita con media espera. */
	public int keystoneOverloadEvery = 4;
	/** Detonación: daño de la explosión al matar (radio 3, sin romper bloques ni dañar aliados). */
	public double keystoneDetonationDamage = 4.0;
	/** Contagio: enemigos cercanos (radio 4) que reciben los efectos del objetivo muerto. */
	public int keystoneContagionTargets = 2;
	/** Legión: fracción del daño de los esbirros que cura al dueño. */
	public double keystoneLegionLifesteal = 0.10;
	/** Espectro: segundos tras esquivar en los que el siguiente golpe es crítico seguro. */
	public double keystoneSpecterSeconds = 3.0;
	/** Espectro: esquiva propia (fracción) para que funcione en sendas sin esquiva. */
	public double keystoneSpecterDodge = 0.05;
	/** Fortuna: probabilidad de una tirada de botín extra al matar y de esmeralda al matar élites. */
	public double keystoneFortuneChance = 0.25;
	public double keystoneFortuneEliteEmerald = 0.10;
	/** Tempestad: daño de la chispa (radio 5) y espera en segundos. */
	public double keystoneTempestDamage = 3.0;
	public double keystoneTempestCooldownSeconds = 1.0;
	/** Tempestad: probabilidad de chispa en golpes directos sin crítico (para sendas sin crítico). */
	public double keystoneTempestChance = 0.15;
	/** Santuario: vida curada a ti y a tus aliados a 8 bloques, cada cuántos segundos. */
	public double keystoneSanctuaryHeal = 2.0;
	public double keystoneSanctuaryIntervalSeconds = 5.0;

	// ---------- Runas (v35: antes eran valores fijos) ----------
	public int runeFlameSeconds = 3;
	public double runeLeechAmount = 1.0;
	public double runeLeechCooldownSeconds = 4.0;
	/** Aceleración: +velocidad de ataque (0.10 = +10 %). Se fija en el arma al engarzar la runa. */
	public double runeSwiftAttackSpeed = 0.10;
	/** Aceleración: ticks menos de espera de los focos. */
	public int runeSwiftFocusTicks = 2;
	/** Fuerza: +daño. En armas cuerpo a cuerpo se fija al engarzar; en focos se aplica en cada disparo. */
	public double runeForceDamage = 1.0;
	public double runeEclipseBonus = 0.15;
	public int runeFrostSeconds = 3;
	/** Nivel de Lentitud de la Escarcha (0 = Lentitud I, 1 = Lentitud II). */
	public int runeFrostLevel = 1;
	public double runeThunderChance = 0.25;
	public double runeThunderDamage = 3.0;
	public int runeThunderTargets = 2;
	public double runeThunderCooldownSeconds = 3.0;
	public double runeGuardAbsorption = 2.0;
	public double runeGuardMaxAbsorption = 6.0;
	public double runeGuardCooldownSeconds = 6.0;
	/** Esmeraldas para quitar la última runa de un arma (la runa se pierde). */
	public int runeRemovalEmeraldCost = 4;

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
	/** Rango máximo que se puede comprar para cada frasco en la Hoguera. */
	public int flaskUpgradeMaxRank = dev.aptitudes.souls.FlaskRules.DEFAULT_RANK_LIMIT;
	/** v64: Vasos de vida que cuentan por jugador (+1 corazón cada uno). 0 los desactiva. */
	public int lifeVesselMax = dev.aptitudes.temple.VesselRules.DEFAULT_MAX;
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

	/** Revisión del archivo (para migraciones; ver {@link ConfigMigration}). No se edita a mano. */
	public int configRevision = ConfigMigration.CURRENT;

	// ---------- Alquimia del Herbolista (v42) ----------
	public boolean alchemyHerbalistEnabled = true;
	public int alchemyHerbalistRadius = 8;
	public double alchemyBrewingSpeedBonus = 0.5;
	public double alchemyDurationBonus = 0.5;
	public int alchemyRoleXpPerBrew = 4;
	public int alchemyXpWindowSeconds = 300;
	public int alchemyXpFullRewardBrews = 3;

	// ---------- Maestría (v43) ----------
	public boolean masteryEnabled = true;
	public int masteryXpBase = 1000;
	public int masteryXpStep = 250;
	public double masteryMaxBonus = 0.10;

	// ---------- Amenaza por distancia (v43) ----------
	public boolean threatEnabled = true;
	public int threatRingSize = 1000;
	public int threatMaxRing = 5;
	public double threatHealthPerRing = 0.15;
	public double threatDamagePerRing = 0.10;
	public double threatLootPerRing = 0.10;
	public double threatEliteChancePerRing = 0.25;

	public static AptitudesConfig load() {
		Path path = path();
		AptitudesConfig cfg = new AptitudesConfig();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				com.google.gson.JsonElement json = com.google.gson.JsonParser.parseReader(reader);
				AptitudesConfig parsed = GSON.fromJson(json, AptitudesConfig.class);
				if (parsed != null) {
					cfg = parsed;
					// v46: los archivos sin revisión son anteriores a la v46.
					int from = revisionOf(json);
					cfg.maxRoleLevel = ConfigMigration.maxRoleLevel(from, cfg.maxRoleLevel);
					cfg.configRevision = ConfigMigration.CURRENT;
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

	/** Revisión guardada en el archivo; 0 si no está o no es un número (archivo anterior a la v46 o editado a mano). */
	private static int revisionOf(com.google.gson.JsonElement json) {
		try {
			if (json.isJsonObject() && json.getAsJsonObject().has("configRevision")) {
				return json.getAsJsonObject().get("configRevision").getAsInt();
			}
		} catch (RuntimeException e) {
			Aptitudes.LOGGER.warn("[Aptitudes] configRevision no válido; se trata como un archivo antiguo");
		}
		return 0;
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
		c.flaskUpgradeMaxRank = dev.aptitudes.souls.FlaskRules.rank(c.flaskUpgradeMaxRank);
		c.lifeVesselMax = dev.aptitudes.temple.VesselRules.limit(c.lifeVesselMax);
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
		c.partyMaxSize = clamp(c.partyMaxSize, 2, 5);
		c.partyMaxXpShare = Math.max(0.0, Math.min(0.5, c.partyMaxXpShare));
		c.partyXpBonusPerMember = Math.min(0.20, c.partyXpBonusPerMember);
		c.partyXpBonusMax = Math.min(0.20, c.partyXpBonusMax);
		c.ultimateBonusPoints = clamp(c.ultimateBonusPoints, 0, 100);
		c.eclipseIntervalDays = clamp(c.eclipseIntervalDays, 1, 365);
		c.eclipseFirstDay = clamp(c.eclipseFirstDay, 1, 3650);
		c.eclipseWaveIntervalSeconds = clamp(c.eclipseWaveIntervalSeconds, 10, 300);
		c.eclipseMaxMobsPerPlayer = clamp(c.eclipseMaxMobsPerPlayer, 0, 40);
		c.eclipseMobHealthMultiplier = Math.max(1.0, Math.min(5.0, c.eclipseMobHealthMultiplier));
		c.eclipseMobDamageMultiplier = Math.max(1.0, Math.min(5.0, c.eclipseMobDamageMultiplier));
		c.eclipseEliteChance = Math.min(1.0, c.eclipseEliteChance);
		c.eclipseChampionChance = Math.min(1.0, c.eclipseChampionChance);
		c.eclipseSurvivorRoleXp = clamp(c.eclipseSurvivorRoleXp, 0, 100_000);
		c.eclipseSurvivorCombatXp = clamp(c.eclipseSurvivorCombatXp, 0, 100_000);
		c.worldEventWeightEclipse = clamp(c.worldEventWeightEclipse, 0, 100);
		c.worldEventWeightSoulFog = clamp(c.worldEventWeightSoulFog, 0, 100);
		c.worldEventWeightEmberRain = clamp(c.worldEventWeightEmberRain, 0, 100);
		c.eclipseTotalityBonus = Math.min(2.0, c.eclipseTotalityBonus);
		c.eclipseMobArmor = Math.min(20.0, c.eclipseMobArmor);
		c.eclipseArmorChance = Math.min(1.0, c.eclipseArmorChance);
		c.eclipsePenumbraWaveSize = clamp(c.eclipsePenumbraWaveSize, 0, 16);
		c.eclipseTotalityWaveSize = clamp(c.eclipseTotalityWaveSize, 0, 24);
		c.eclipseZoneMobCap = clamp(c.eclipseZoneMobCap, 0, 160);
		c.eclipseWorldMobCap = clamp(c.eclipseWorldMobCap, 0, 512);
		c.eclipseMinElitesPerWave = clamp(c.eclipseMinElitesPerWave, 0, 4);
		c.eclipseMinChampionsTotality = clamp(c.eclipseMinChampionsTotality, 0, 2);
		c.emberRainLargeChance = Math.min(1.0, c.emberRainLargeChance);
		c.emberRainHugeChance = Math.min(1.0, c.emberRainHugeChance);
		c.emberRainMobChance = Math.min(1.0, c.emberRainMobChance);
		c.keystoneCarnageHeal = Math.min(1.0, c.keystoneCarnageHeal);
		c.keystoneCarnageHasteSeconds = Math.min(60.0, c.keystoneCarnageHasteSeconds);
		c.keystoneBulwarkThreshold = Math.min(1.0, c.keystoneBulwarkThreshold);
		c.keystoneBulwarkAbsorption = Math.min(40.0, c.keystoneBulwarkAbsorption);
		c.keystoneBulwarkSeconds = Math.min(60.0, c.keystoneBulwarkSeconds);
		c.keystoneBulwarkCooldownSeconds = Math.max(5.0, Math.min(600.0, c.keystoneBulwarkCooldownSeconds));
		c.keystoneOverloadEvery = clamp(c.keystoneOverloadEvery, 2, 20);
		c.keystoneDetonationDamage = Math.min(40.0, c.keystoneDetonationDamage);
		c.keystoneContagionTargets = clamp(c.keystoneContagionTargets, 0, 8);
		c.keystoneLegionLifesteal = Math.min(1.0, c.keystoneLegionLifesteal);
		c.keystoneSpecterSeconds = Math.max(0.5, Math.min(30.0, c.keystoneSpecterSeconds));
		c.keystoneSpecterDodge = Math.min(0.5, c.keystoneSpecterDodge);
		c.keystoneFortuneChance = Math.min(1.0, c.keystoneFortuneChance);
		c.keystoneFortuneEliteEmerald = Math.min(1.0, c.keystoneFortuneEliteEmerald);
		c.keystoneTempestDamage = Math.min(40.0, c.keystoneTempestDamage);
		c.keystoneTempestCooldownSeconds = Math.max(0.25, Math.min(60.0, c.keystoneTempestCooldownSeconds));
		c.keystoneTempestChance = Math.min(1.0, c.keystoneTempestChance);
		c.keystoneSanctuaryHeal = Math.min(20.0, c.keystoneSanctuaryHeal);
		c.keystoneSanctuaryIntervalSeconds = Math.max(1.0, Math.min(60.0, c.keystoneSanctuaryIntervalSeconds));
		c.runeFlameSeconds = clamp(c.runeFlameSeconds, 0, 20);
		c.runeLeechAmount = Math.min(10.0, c.runeLeechAmount);
		c.runeLeechCooldownSeconds = Math.max(0.5, Math.min(60.0, c.runeLeechCooldownSeconds));
		c.runeSwiftAttackSpeed = Math.min(1.0, c.runeSwiftAttackSpeed);
		c.runeSwiftFocusTicks = clamp(c.runeSwiftFocusTicks, 0, 10);
		c.runeForceDamage = Math.min(10.0, c.runeForceDamage);
		c.runeEclipseBonus = Math.min(1.0, c.runeEclipseBonus);
		c.runeFrostSeconds = clamp(c.runeFrostSeconds, 0, 20);
		c.runeFrostLevel = clamp(c.runeFrostLevel, 0, 4);
		c.runeThunderChance = Math.min(1.0, c.runeThunderChance);
		c.runeThunderDamage = Math.min(20.0, c.runeThunderDamage);
		c.runeThunderTargets = clamp(c.runeThunderTargets, 0, 5);
		c.runeThunderCooldownSeconds = Math.max(0.5, Math.min(60.0, c.runeThunderCooldownSeconds));
		c.runeGuardAbsorption = Math.min(20.0, c.runeGuardAbsorption);
		c.runeGuardMaxAbsorption = Math.min(40.0, c.runeGuardMaxAbsorption);
		c.runeGuardCooldownSeconds = Math.max(0.5, Math.min(60.0, c.runeGuardCooldownSeconds));
		c.runeRemovalEmeraldCost = clamp(c.runeRemovalEmeraldCost, 0, 64);
		c.roleChangeCooldownMinutes = clamp(c.roleChangeCooldownMinutes, 0, 100_000);
		c.baseMaxEnergy = Math.max(1.0, c.baseMaxEnergy);
		c.baseEnergyRegenPerSecond = Math.max(0.0, c.baseEnergyRegenPerSecond);
		c.alchemyHerbalistRadius = clamp(c.alchemyHerbalistRadius, 0, 16);
		c.alchemyBrewingSpeedBonus = Math.min(1.0, c.alchemyBrewingSpeedBonus);
		c.alchemyDurationBonus = Math.min(1.0, c.alchemyDurationBonus);
		c.alchemyRoleXpPerBrew = clamp(c.alchemyRoleXpPerBrew, 0, 20);
		c.alchemyXpWindowSeconds = clamp(c.alchemyXpWindowSeconds, 10, 3600);
		c.alchemyXpFullRewardBrews = clamp(c.alchemyXpFullRewardBrews, 1, 20);
		c.masteryXpBase = clamp(c.masteryXpBase, 1, 1_000_000);
		c.masteryXpStep = clamp(c.masteryXpStep, 0, 1_000_000);
		c.masteryMaxBonus = Math.min(0.25, c.masteryMaxBonus);
		c.threatRingSize = clamp(c.threatRingSize, 100, 1_000_000);
		c.threatMaxRing = clamp(c.threatMaxRing, 1, 5);
		c.threatHealthPerRing = Math.min(0.4, c.threatHealthPerRing);
		c.threatDamagePerRing = Math.min(0.2, c.threatDamagePerRing);
		c.threatLootPerRing = Math.min(0.2, c.threatLootPerRing);
		c.threatEliteChancePerRing = Math.min(1, c.threatEliteChancePerRing);
		return c;
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
