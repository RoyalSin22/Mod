# Configuración de Aptitudes

Archivos (se crean en el primer arranque):

| Archivo | Lado | Contenido |
|---|---|---|
| `config/aptitudes.json` | servidor (y un jugador) | Reglas de juego: XP, atributos, roles, élites… |
| `config/aptitudes-client.json` | cliente | HUD, efectos de pantalla, números de daño |

El servidor envía al cliente las reglas que necesita (curvas de XP, costes…), así que el cliente
siempre muestra lo que valida el servidor aunque su archivo local sea distinto.

## Validación al cargar

1. **General (nuevo):** cualquier número negativo, `NaN` o infinito vuelve a su valor por defecto y se
   escribe un aviso en el log: `[Aptitudes] Config 'campo' inválido (valor); se usa el valor por defecto X`.
2. **Límites específicos:** probabilidades ≤ 1 (`eliteChance`, `farmingExtraDropChancePerLevel`),
   afinidades ≤ 5, divisores ≥ 1 (`levelCostDivisor`, `soulLevelsPerPoint`, `rolePointsEveryLevels`),
   `attributeSoftCapSecond ≥ attributeSoftCapFirst`, niveles máximos 1-1000, etc.
3. `attributeScaling` desconocido → `LINEAR` con aviso.

## Opciones añadidas en este bloque

| Opción | Defecto | Descripción |
|---|---|---|
| `attributeScaling` | `"LINEAR"` | `LINEAR`, `SOFTCAP` o `SATURATION` (ver docs/BALANCE.md). |
| `attributeSoftCapFirst` | 40 | SOFTCAP: puntos al 100 % hasta aquí. |
| `attributeSoftCapSecond` | 60 | SOFTCAP: puntos al 50 % hasta aquí; después al 20 %. |
| `attributeSaturationPoints` | 50 | SATURATION: límite L de puntos efectivos. |
| `maxDamageMultiplier` | 6.0 | Tope del multiplicador de daño combinado del mod (0 = sin tope). |
| `legendaryExclusive` | false | true: cada legendaria solo la puede tener un jugador del servidor y cada jugador solo una. |

## Opciones de Pesca y rendimiento decreciente (v15)

| Opción | Defecto | Validación | Descripción |
|---|---|---|---|
| `fishingXpCatch` | 12 | ≥ 0 | XP de Pesca por pez capturado (basura y tesoros no cuentan). |
| `fishingLuckPerLevel` | 0.02 | ≥ 0 | +suerte por nivel (+1 a nivel 50); mejora el botín de pesca vanilla. |
| `fishingDoubleCatchPerLevel` | 0.004 | [0, 1] | Probabilidad de captura doble por nivel (20 % a nivel 50). |
| `enchantingXpPerEnchant` | 25 | ≥ 0 | XP de Encantamiento por objeto encantado en la mesa (el yunque no cuenta). |
| `enchantingRefundChancePerLevel` | 0.01 | [0, 1] | Probabilidad por nivel de recuperar 1 nivel al encantar (50 % a nivel 50; 100 % con el hito 50). |
| `enchantingManaPerLevel` | 0.4 | [0, 10] | +maná máximo por nivel (+20 a nivel 50; +10 más con el hito 20). |
| `xpDiminishingEnabled` | true | — | Activa el rendimiento decreciente por fuente repetida. |
| `xpDiminishingWindowSeconds` | 60 | [1, 86 400] | Ventana deslizante. |
| `xpDiminishingThreshold` | 20 | [1, 100 000] | Eventos de la misma fuente con XP completa dentro de la ventana. |
| `xpDiminishingFloor` | 0.1 | [0, 1] | Fracción mínima de XP por evento. |

Fuentes afectadas: Combate y Tiro con arco (por tipo de criatura abatida), Defensa (por tipo de atacante),
Pesca y Encantamiento. Minería, Excavación, Tala, Agricultura y Agilidad no se ven afectadas: repetir la misma acción es
su forma normal de juego.

## Opciones de versiones anteriores (resumen)

| Opción | Defecto | Descripción |
|---|---|---|
| `eliteChance` | 0.05 | Probabilidad de élite (el 10 % de ellas, campeón). 0 desactiva. |
| `roleChangeRequiresRitual` | true | Sustituir un rol exige quemar una Ofrenda de renacimiento en una Hoguera. |
| `roleChangeCooldownMinutes` | 0 | Obsoleto (ya no se usa). |
| `maxPointsPerAttribute` | 99 | Puntos máximos por atributo. |
| `soulLevelsPerPoint` | 3 | Niveles de alma por punto de atributo. |
| `levelCostBase/Divisor/Max` | 1 / 6 / 10 | Coste en XP de cada nivel de alma: `min(Max, Base + nivel/Divisor)`. |
| `primaryAffinityBonus` / `secondaryAffinityBonus` | 0.5 / 0.25 | Bonificación de los atributos afines al rol. |
| `maxRoleLevel` / `rolePointsEveryLevels` | 30 / 3 | Nivel máximo de rol y frecuencia de puntos de árbol. |
| `secondaryRoleXpMultiplier` | 0.5 | XP del rol secundario respecto al principal. |
| `damageNumbers`, `levelUpTitles` | true | Efectos visuales enviados desde el servidor. |

Datos editables por datapack (sin recompilar): rareza de estructuras
(`data/aptitudes/worldgen/placed_feature/*.json`), botín (`data/aptitudes/loot_table/chests/*.json`)
y recetas (`data/aptitudes/recipe/*.json`).

## Árbol de evolución (v23)

| Opción | Defecto | Validación | Descripción |
|---|---|---|---|
| `masterBonusPoints` | 10 | [0, 100] | Puntos de árbol extra al evolucionar un rol a Maestro. |
| `ultimateBonusPoints` | 5 | [0, 100] | Puntos de árbol extra al evolucionar un rol a Definitivo. |

## Valor de los atributos (v27)

| Opción | Defecto (v27) | Antes |
|---|---|---|
| `vigorHealthPerPoint` | 0.7 | 1.0 |
| `mindManaPerPoint` | 3.5 | 5.0 |
| `enduranceArmorPerPoint` | 0.35 | 0.5 |
| `strengthDamagePerPoint` | 0.175 | 0.25 |
| `dexterityAttackSpeedPerPoint` | 0.007 | 0.01 |
| `intelligenceSpellDamagePerPoint` | 0.014 | 0.02 |
| `faithCooldownPerPoint` | 0.007 | 0.01 |
| `luckPerPoint` | 0.14 | 0.2 |

**Importante:** el archivo `config/aptitudes.json` se crea la primera vez con los valores de ese momento y
no se actualiza solo. Para pasar a los valores nuevos, borra esas 8 líneas (o el archivo entero, perdiendo
los ajustes propios) y usa `/aptitudes admin reload` o reinicia.

## Combos (v28)

| Opción | Defecto | Validación | Descripción |
|---|---|---|---|
| `combosEnabled` | true | — | Activa el sistema de combos. |
| `comboWindowSeconds` | 2.5 | [0,25, 30] | Segundos máximos entre golpes para mantener el combo. |
| `comboDamagePerHit` | 0.03 | [0, 1] | Daño extra por golpe de combo. |
| `comboMaxBonus` | 0.15 | [0, 2] | Tope de la bonificación de combo (dentro del tope global `maxDamageMultiplier`). |
| `comboFinisherHits` | 5 | [2, 50] | Golpes para que el siguiente sea un remate. |
| `comboFinisherCooldownSeconds` | 4.0 | [0, 600] | Espera mínima entre remates. |
