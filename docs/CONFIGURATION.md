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
