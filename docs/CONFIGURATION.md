# Configuración de Aptitudes

Archivos (se crean en el primer arranque):

| Archivo | Lado | Contenido |
|---|---|---|
| `config/aptitudes.json` | servidor (y un jugador) | Reglas de juego: XP, atributos, roles, élites… |
| `config/aptitudes-client.json` | cliente | HUD, efectos de pantalla, números de daño |

El servidor envía al cliente las reglas que necesita (curvas de XP, costes…), así que el cliente
siempre muestra lo que valida el servidor aunque su archivo local sea distinto.

## Asalto del Eclipse (v47)

| Campo del servidor | Defecto | Límites | Descripción |
|---|---:|---:|---|
| `eclipsePenumbraWaveSize` | 4 | [0, 16] | Refuerzos solicitados por jugador en Penumbra; cero desactiva las oleadas de esta fase. |
| `eclipseTotalityWaveSize` | 8 | [0, 24] | Refuerzos por jugador en Totalidad; cero desactiva las oleadas de esta fase. |
| `eclipseZoneMobCap` | 24 | [0, 160] | Tope local adicional de criaturas del Eclipse en el área de 48 bloques de cada jugador. Se comparte entre jugadores próximos, con o sin grupo; cero impide nuevos refuerzos. |
| `eclipseWorldMobCap` | 128 | [0, 512] | Tope para apariciones nuevas contando criaturas del Eclipse cargadas en el Mundo Normal, incluidos Heraldos. Si está justo lleno, un Heraldo puede sustituir una unidad ordinaria sin botín; cero impide también Heraldos nuevos. |
| `eclipseMinElitesPerWave` | 1 | [0, 4] | Primeras unidades élite garantizadas por jugador y oleada, después de los campeones. Solo si `eclipseEliteChance > 0`, cabe la criatura y queda presupuesto. Cero conserva la tirada aleatoria. |
| `eclipseMinChampionsTotality` | 1 | [0, 2] | Campeones garantizados al comienzo de cada oleada de Totalidad, si `eclipseChampionChance > 0` y queda espacio/presupuesto. Cero conserva la tirada aleatoria. |
| `eclipseGuaranteedEliteArmor` | true | booleano | Élites/campeones de oleadas con cuerpo de zombi/esqueleto y equipo completo visible (malla/hierro por fase; campeones, diamante), irrompible y sin soltarlo. Con false se conservan tipos y probabilidad de equipo anteriores. |

Las opciones existentes no cambian de valor: intervalo 30 s, tope 12 por jugador, vida/daño ×1,5,
élite 0,5, campeón 0,1, armadura 4, probabilidad de piezas normales 0,35 y bonus de Totalidad 0,25.
Fuera de las unidades garantizadas se conservan las tiradas de grado. `eclipseArmorChance = 0` solo
desactiva equipo aleatorio; para desactivar también el equipo garantizado usa su interruptor.
Los enteros nuevos pasan por `validated(...)`: negativos inválidos recuperan el defecto y los demás
se acotan al rango indicado. Se pueden editar por la pantalla existente con validación del servidor.

No se fuerzan chunks. Al cargar o reiniciar se cuentan las criaturas existentes antes de generar otras;
si cargar criaturas guardadas supera un límite, se suspenden las nuevas apariciones, sin borrar las antiguas.
Las probabilidades, tamaños y presupuestos se vuelven a acotar en las reglas puras.

## Frascos (v60)

| Campo | Defecto | Dominio | Descripción |
|---|---:|---|---|
| `flaskUpgradeMaxRank` | 2 | 0–3 | Rango máximo que se puede comprar en la Hoguera. 0 desactiva compras. Los rangos ya guardados conservan su capacidad y recuperación. |

## HUD (cliente)

| Campo de `aptitudes-client.json` | Defecto | Dominio | Descripción |
|---|---:|---|---|
| `hudScale` | 1.0 | 0.5–2.0 | Escala solicitada en Mayús + O. La barra usa una escala menor cuando lo exige la ventana. Un valor no finito se sustituye por 1.0. |
| `hudOffsetX`, `hudOffsetY` | 0 | −4096–4096 | Desplazamiento en píxeles de interfaz. La posición efectiva se acota al área visible junto con las filas de mecánicas y piedras angulares. |
| `hudPreset` | `classic` | `classic` / `centered` / `compact` | Clásico conserva el anclaje de v50. Centrado coloca las habilidades sobre los avisos de la hotbar y del evento. Compacto las alinea a la derecha y usa el 75 % de la escala en las habilidades y barras Souls. Seleccionar un preset en el editor borra el desplazamiento manual y conserva la escala solicitada. Un valor desconocido o `null` recupera Clásico. |
| `hudMinimal` | false | true / false | Oculta avisos de XP y tutorial, contador de combos, mecánicas de rol y números de daño. Conserva las barras, habilidades, piedras angulares, avisos de amenaza, eventos, jefes y grupo. `null` recupera false. |
| `hudAnimations` | true | true / false | Transiciones de vida, maná, jefes y XP, entrada de avisos y pulso de nivel en la tarjeta de XP. Al desactivarlas, el HUD muestra los cambios de inmediato y sus indicadores de estado dejan de latir. `null` recupera true. Los efectos del mundo y del cielo tienen sus propios interruptores. |
| `guideBookmarks` | `[]` | hasta 24 ids `chapter:` / `item:` / `beast:` / `role:` | Marcadores del grimorio (v61). Se guardan al pulsar ☆; los ids no válidos o repetidos se descartan al cargar. |
| `castAnimations` | true | true / false | Remates 3D de las habilidades, desde Mayús + O → Animaciones 3D. Un `null` explícito recupera true en `ClientConfig.validated(...)`. |

## Tutorial (v44)

| Campo del cliente | Defecto | Dominio | Descripción |
|---|---|---|---|
| `showTutorial` | true | true / false | Recordatorio del tutorial en el HUD. Mayús + O → Tutorial. Si falta o es `null`, `ClientConfig.validated(...)` conserva true. Ocultarlo no altera progreso ni cobros. |

Las seis recompensas siguen siendo una esmeralda por paso y se guardan por jugador, sin opciones nuevas
del servidor. `hudVisible = false` y F1 también ocultan el recordatorio. `/aptitudes tutorial skip`
es distinto del interruptor: omite permanentemente el tutorial y cancela los cobros pendientes.
El objetivo de cinco golpes requiere las opciones existentes `combosEnabled = true` y
`comboFinisherHits >= 5`; un remate con umbral inferior reinicia el contador antes de cinco.
No se cambian los valores por defecto (combos activados y remate a cinco golpes).

## Maestría (v43, T6 fase 1)

| Campo del servidor | Defecto | Límites | Descripción |
|---|---:|---:|---|
| `masteryEnabled` | true | booleano | Activa ganancia y efectos de maestría; desactivarlo conserva el historial. |
| `masteryXpBase` | 1000 | [1, 1 000 000] | XP del primer punto, con curva acumulada propia. |
| `masteryXpStep` | 250 | [0, 1 000 000] | Aumento del coste por punto siguiente; 0 mantiene coste constante. |
| `masteryMaxBonus` | 0.10 | [0, 0.25] | Tope del bonus. Cada punto aporta 0,005; 0 anula efectos sin borrar ni detener la XP si sigue activada. |

Se aplican `validated(...)` y la validación general de negativos/NaN/infinitos. Se sincronizan los valores
reales al cliente. La XP acumulada no cambia al editar la curva; se recalculan puntos y bonus desde ella.
Límites internos: 10¹² XP por clave y 1000 puntos; nunca se supera el tope configurado de bonificación.
Las habilidades requieren `maxSkillLevel`; los roles, `max(50, maxRoleLevel)`. Aumentar el nivel máximo
conserva la maestría, pero suspende su efecto hasta volver a alcanzarlo.

## Amenaza (v43, T6 fase 2)

| Campo del servidor | Defecto | Límites | Descripción |
|---|---:|---:|---|
| `threatEnabled` | true | booleano | Activa escalado, botín y aviso. Desactivarlo retira vida añadida en hasta 20 ticks y pausa daño/botín; conserva instantáneas. |
| `threatRingSize` | 1000 | [100, 1 000 000] | Anchura horizontal de cada anillo desde la aparición del mundo. La zona inicial tiene nivel 0. |
| `threatMaxRing` | 5 | [1, 5] | Máximo I–V; la distancia mayor conserva ese nivel. |
| `threatHealthPerRing` | 0.15 | [0, 0.4] | Vida base adicional por anillo; máximo interno +200 %. Se suma a otros modificadores de base, sin multiplicar élites. |
| `threatDamagePerRing` | 0.10 | [0, 0.2] | Daño adicional por anillo, golpes/proyectiles/explosiones; máximo ×2 y sujeto a `maxDamageMultiplier` cuando es positivo. |
| `threatLootPerRing` | 0.10 | [0, 0.2] | Probabilidad por anillo de una única tirada adicional de la tabla de botín; máximo 100 %, sin XP ni equipo extra. |
| `threatEliteChancePerRing` | 0.25 | [0, 1] | Incremento relativo por anillo de `eliteChance`: `base × (1 + anillo × valor)`, limitado a 100 %. |

Negativos y no finitos vuelven al defecto antes de aplicar límites. Las siete opciones pasan por la
pantalla existente y la validación del servidor. Cada criatura nueva conserva anillo y multiplicadores
de aparición: cambiar parámetros o mover la aparición del mundo afecta a nuevas criaturas y al aviso,
sin volver a sortear élites ni curar las antiguas. Desactivar/reactivar no borra ni vuelve a pagar botín;
reactivar el atributo no cura. Cambiar de dimensión o nombrar una criatura ordinaria suspende su escalado.
El nombre generado por la conversión natural de élite conserva su instantánea. Los monstruos antiguos
ya comprobados y sin instantánea no reciben escalado retroactivo. El aviso usa `hudVisible` y F1;
se oculta en creativo/espectador, al morir o salir del Mundo Normal. No se añaden opciones del cliente.

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
| `maxRoleLevel` / `rolePointsEveryLevels` | 50 / 3 | Nivel máximo de rol y frecuencia de puntos de árbol. v46: el valor por defecto pasa de 30 a 50; un archivo anterior con 30 se migra solo a 50 (un valor distinto, elegido a mano, se respeta). |
| `configRevision` | 46 | Interno: revisión del archivo para las migraciones (`config/ConfigMigration`). No aparece en la pantalla de configuración; no lo edites. |
| `secondaryRoleXpMultiplier` | 0.5 | XP del rol secundario respecto al principal. |
| `damageNumbers`, `levelUpTitles` | true | Efectos visuales enviados desde el servidor. |

### Piedras angulares (v46.1)

| Campo | Defecto | Límites | Descripción |
|---|---|---|---|
| `keystoneCarnageHeal` | 0.10 | [0, 1] | Carnicería: fracción de la vida máxima curada al matar. |
| `keystoneCarnageHasteSeconds` | 5.0 | [0, 60] | Carnicería: segundos de Prisa II. |
| `keystoneBulwarkThreshold` | 0.30 | [0, 1] | Baluarte: umbral de vida. |
| `keystoneBulwarkAbsorption` | 8.0 | [0, 40] | Baluarte: puntos de absorción (4 por nivel de Absorción). |
| `keystoneBulwarkSeconds` | 6.0 | [0, 60] | Baluarte: duración. |
| `keystoneBulwarkCooldownSeconds` | 60.0 | [5, 600] | Baluarte: espera. |
| `keystoneOverloadEvery` | 4 | [2, 20] | Sobrecarga: cada cuántas habilidades llega la gratuita. |
| `keystoneDetonationDamage` | 4.0 | [0, 40] | Detonación: daño (radio 3). |
| `keystoneContagionTargets` | 2 | [0, 8] | Contagio: enemigos que reciben los efectos (radio 4). |
| `keystoneLegionLifesteal` | 0.10 | [0, 1] | Legión: fracción del daño de los esbirros que cura al dueño. |
| `keystoneSpecterSeconds` | 3.0 | [0.5, 30] | Espectro: ventana de crítico seguro tras esquivar. |
| `keystoneSpecterDodge` | 0.05 | [0, 0.5] | Espectro: esquiva propia. |
| `keystoneFortuneChance` | 0.25 | [0, 1] | Fortuna: probabilidad de una tirada de botín extra. |
| `keystoneFortuneEliteEmerald` | 0.10 | [0, 1] | Fortuna: probabilidad de esmeralda al matar élites. |
| `keystoneTempestDamage` | 3.0 | [0, 40] | Tempestad: daño de la chispa (radio 5). |
| `keystoneTempestCooldownSeconds` | 1.0 | [0.25, 60] | Tempestad: espera. |
| `keystoneTempestChance` | 0.15 | [0, 1] | Tempestad: probabilidad en golpes directos sin crítico. |
| `keystoneSanctuaryHeal` | 2.0 | [0, 20] | Santuario: vida curada (radio 8). |
| `keystoneSanctuaryIntervalSeconds` | 5.0 | [1, 60] | Santuario: cada cuántos segundos. |

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

## Eclipse (v34)

| Campo | Defecto | Límites | Descripción |
|---|---|---|---|
| `eclipseEnabled` | true | — | Activa el evento del Eclipse. Si se desactiva durante un eclipse, termina sin recompensas. |
| `eclipseIntervalDays` | 10 | [1, 365] | Cada cuántos días de juego hay eclipse (días múltiplos del intervalo). |
| `eclipseFirstDay` | 10 | [1, 3650] | Primer día en el que puede haber eclipse. |
| `eclipseWaveIntervalSeconds` | 30 | [10, 300] | Segundos entre oleadas. |
| `eclipseMaxMobsPerPlayer` | 12 | [0, 40] | Tope de criaturas del eclipse vivas a 48 bloques de cada jugador. 0 = sin oleadas. |
| `eclipseMobHealthMultiplier` | 1.5 | [1, 5] | Vida de las criaturas del eclipse (antes del escalado de élite). |
| `eclipseMobDamageMultiplier` | 1.5 | [1, 5] | Daño cuerpo a cuerpo de las criaturas del eclipse. |
| `eclipseEliteChance` | 0.5 | [0, 1] | Probabilidad de élite. |
| `eclipseChampionChance` | 0.1 | [0, 1] | Probabilidad de campeón (se comprueba antes que la de élite). |
| `eclipseHeraldEnabled` | true | — | Aparición del Heraldo del Eclipse a mediodía. |
| `eclipseSurvivorRoleXp` | 300 | [0, 100 000] | XP del rol principal para quien no muere durante el eclipse. |
| `eclipseSurvivorCombatXp` | 200 | [0, 100 000] | XP de Combate para quien no muere durante el eclipse. |
| `eclipseTotalityBonus` | 0.25 | [0, 2] | v41.2: vida y daño extra en la Totalidad, multiplicando a los dos anteriores (0,25 → ×1,875). 0 = como antes. |
| `eclipseMobArmor` | 4.0 | [0, 20] | v41.2: puntos de armadura extra de cada criatura del eclipse. 0 = como antes. |
| `eclipseArmorChance` | 0.35 | [0, 1] | v41.2: probabilidad de cada pieza visible (pecho, piernas, botas) en zombis, esqueletos, esqueletos wither y vindicadores: malla, hierro en la Totalidad, diamante los campeones. No se sueltan. |

Opción del cliente (`config/aptitudes-client.json`, también en Mayús + O): `eclipseSkyEffects` (true) tiñe el
cielo, la niebla y las nubes y cambia la textura del sol. Desactívala si los efectos molestan; la viñeta y la
ceniza se siguen viendo.

## Runas (v35)

Antes eran valores fijos en el código. Fuerza (en armas cuerpo a cuerpo) y Aceleración (velocidad de ataque)
se **fijan en el arma al engarzar** la runa: cambiar el valor solo afecta a las runas engarzadas después.

| Campo | Defecto | Límites | Descripción |
|---|---|---|---|
| `runeFlameSeconds` | 3 | [0, 20] | Segundos de fuego de la Runa de Fuego. |
| `runeLeechAmount` | 1.0 | [0, 10] | Salud que cura el Robo de vida. |
| `runeLeechCooldownSeconds` | 4.0 | [0,5, 60] | Espera del Robo de vida. |
| `runeSwiftAttackSpeed` | 0.10 | [0, 1] | Velocidad de ataque de la Aceleración (al engarzar). |
| `runeSwiftFocusTicks` | 2 | [0, 10] | Ticks menos de espera de los focos con Aceleración (mínimo 4 ticks). |
| `runeForceDamage` | 1.0 | [0, 10] | Daño de la Fuerza (al engarzar en cuerpo a cuerpo; en cada disparo en focos). |
| `runeEclipseBonus` | 0.15 | [0, 1] | Daño extra de la Runa del Eclipse contra élites, campeones y jefes. |
| `runeFrostSeconds` | 3 | [0, 20] | Segundos de Lentitud de la Escarcha. |
| `runeFrostLevel` | 1 | [0, 4] | Nivel de Lentitud (0 = I, 1 = II). |
| `runeThunderChance` | 0.25 | [0, 1] | Probabilidad de descarga del Trueno. |
| `runeThunderDamage` | 3.0 | [0, 20] | Daño de la descarga a cada objetivo extra. |
| `runeThunderTargets` | 2 | [0, 5] | Monstruos a los que salta la descarga (4 bloques). |
| `runeThunderCooldownSeconds` | 3.0 | [0,5, 60] | Espera entre descargas. |
| `runeGuardAbsorption` | 2.0 | [0, 20] | Absorción que da la Guardia al golpear. |
| `runeGuardMaxAbsorption` | 6.0 | [0, 40] | Tope de absorción de la Guardia (no reduce una absorción mayor). |
| `runeGuardCooldownSeconds` | 6.0 | [0,5, 60] | Espera de la Guardia. |
| `runeRemovalEmeraldCost` | 4 | [0, 64] | Esmeraldas para quitar la última runa (gratis en creativo). |

Los textos de las runas muestran los valores por defecto: si un servidor los cambia, el tooltip no lo refleja.

## Pantalla de configuración (v40)

- Se abre con `/aptitudes config`, con la pestaña ⚙ de la pantalla K o desde **Mod Menu** (si está instalado).
- Lista **todas** las opciones de esta página, con buscador. Los valores son los del **servidor** (se sincronizan al
  entrar y tras cada cambio).
- **Solo un operador** (permiso 2) puede cambiarlos: ✔ o Intro envía el valor; el servidor lo interpreta, lo
  limita con las mismas reglas que al cargar el archivo, lo guarda en `config/aptitudes.json` y lo reenvía a
  todos. El mensaje de confirmación muestra el valor final (por si se limitó). Cada cambio queda en el registro.
- Un jugador sin permiso la ve en solo lectura. El botón «HUD y efectos» abre las opciones del cliente.
- Los tooltips de las runas muestran ahora los valores reales del servidor.
- `/aptitudes admin reload` también reenvía la configuración a los clientes.

## Eventos rotativos (v41)

El calendario es el de `eclipseIntervalDays` y `eclipseFirstDay` (nombres históricos): cada fecha toca **un**
evento elegido al azar entre los que tienen peso > 0, sin repetir el anterior (reproducible con la semilla del
mundo). `eclipseEnabled = false` equivale a peso 0 para el Eclipse.

| Campo | Defecto | Límites | Descripción |
|---|---|---|---|
| `worldEventWeightEclipse` | 1 | [0, 100] | Peso del Eclipse. |
| `worldEventWeightSoulFog` | 1 | [0, 100] | Peso de la Niebla de almas (0 = nunca). |
| `worldEventWeightEmberRain` | 1 | [0, 100] | Peso de la Lluvia de brasas (0 = nunca). |
| `emberRainLargeChance` | 0.25 | [0, 1] | v41.2: probabilidad de meteorito grande. |
| `emberRainHugeChance` | 0.05 | [0, 1] | v41.2: probabilidad de meteorito enorme (se comprueba antes que la de grande). Con ambas a 0, todos pequeños como antes. |
| `emberRainMobChance` | 0.5 | [0, 1] | v41.2: probabilidad de que un impacto traiga criaturas del Nether (la mitad en los pequeños); 0 = ninguna. |

La Niebla de almas usa `eclipseWaveIntervalSeconds` para sus oleadas y la mitad de `eclipseMaxMobsPerPlayer`
como tope por jugador.

## Grupos (v29)

| Campo | Defecto | Límites | Descripción |
|---|---:|---:|---|
| `partyMaxSize` | 5 | [2, 5] | Máximo de miembros por grupo, incluido el líder. |
| `partyMaxXpShare` | 0.5 | [0, 0.5] | Porcentaje máximo que el líder puede compartir. El menú y `/party share` lo ajustan en pasos de 5 puntos. |
| `partyXpBonusPerMember` | 0.05 | [0, 0.20] | Fracción de XP adicional por cada miembro conectado además del líder. |
| `partyXpBonusMax` | 0.20 | [0, 0.20] | Tope de bonificación de XP de grupo. |

La interfaz de configuración del servidor permite editar estos campos; sus valores pasan por `validated(...)` igual
que los demás.

## Alquimia (v42)

| Campo | Defecto | Límites | Descripción |
|---|---:|---:|---|
| `alchemyHerbalistEnabled` | true | booleano | Activa ventajas y XP de destilado; no desregistra pociones ni recetas. |
| `alchemyHerbalistRadius` | 8 | [0, 16] | Radio de detección cercana. Con 0 solo ayuda el Herbolista que abrió el soporte, hasta 32 bloques. |
| `alchemyBrewingSpeedBonus` | 0.5 | [0, 1] | Fracción de velocidad adicional: 0.5 = 50 % más rápido; 0 conserva el tiempo vanilla. |
| `alchemyDurationBonus` | 0.5 | [0, 1] | Duración adicional respecto de la receta original, sin aumentar potencia ni instantáneos. Tope de duración ampliada: 24 000 ticks. |
| `alchemyRoleXpPerBrew` | 4 | [0, 20] | XP base por elaboración con efectos, no por frasco ni por recogida. 0 desactiva la XP. |
| `alchemyXpWindowSeconds` | 300 | [10, 3600] | Ventana personal de rendimiento decreciente, persistente al morir/reconectar/reiniciar. |
| `alchemyXpFullRewardBrews` | 3 | [1, 20] | Elaboraciones con XP completa por ventana; después baja hasta cero, sin suelo positivo. |

Las potencias y duraciones base de las cuatro pociones se conservan en `AlchemyRules` y `docs/BALANCE.md`.
Los valores no finitos o negativos vuelven al defecto y luego se acotan en `validated(...)`.
Estas opciones se sincronizan y editan con la pantalla existente, con validación del servidor.
