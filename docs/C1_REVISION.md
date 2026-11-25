# C1 — Revisión de código de v42–v58 (auditoría transversal)

**Fecha:** 2026-11-24 · **Alcance real:** auditoría **transversal** por clases de fallo, no una lectura línea a línea de
las 17 versiones (Alquimia, tutorial, Maestría, Amenaza, Asalto y Persecución del Eclipse, Reforja y Templado, remates 3D,
jefes). Lo que **no** se revisó está en la última sección. Las correcciones pequeñas y seguras se aplicaron en esta rama.

## Qué se comprobó y qué se encontró

| Comprobación | Resultado |
|---|---|
| Los 11 receptores C2S (`ModNetworking`, `ConfigSync`, `GrimoireManager`) validan el origen, y los índices y cantidades | **Bien.** Índices con límites, cantidad de `AllocatePoints` acotada, `levelUp` valida con `long` (`SoulsMath`), formas y roles por `byIndex` (nulo si no existe) |
| Tamaño máximo de los datos que envía el cliente | **Bien.** `string(64)`, `string(128)`; `LevelUpPayload` rechaza más de 32 enteros; el resto son enteros o UUID |
| Límite de frecuencia | **Aceptable.** Contratos, invitaciones, estadísticas, accesorios y respec usan `RequestLimiter`; la sincronización de grupo (10 ticks) y la del grimorio se limitan por dentro. Sin límite: `AllocatePoints`, `UseAbility`, `RoleAction` (mejora, equipar, evolucionar) y `ForgeAction`. Todas hacen trabajo acotado y validan; no se consideran un riesgo, pero son el sitio para añadir `allowed(...)` si hace falta |
| Modificadores de atributo | **Bien.** `Elites` y `Minions` quitan el modificador antes de añadirlo |
| `catch` que tragan errores | **Corregido.** `SynergyRules` capturaba `Exception` en 4 sitios y devolvía `false`/0 sin registrar nada: si Y2, Y3 o Y4 fallaban en servidor, el bonus dejaba de aplicarse sin dejar rastro. Ahora captura `RuntimeException` y lo registra una sola vez por lugar |
| Mapas estáticos sin limpieza | **Corregido.** `Elites.BLINK_READY` está indexado por el UUID de **cada élite** y nunca se vaciaba: crecía sin tope. Ahora se retira en `ENTITY_UNLOAD` (cubre muerte y descarga de chunk) y se vacía al parar el servidor |
| Coste por tick de la Persecución del Eclipse | **Bien.** Escalonada por mob con `EclipsePursuitRules.due(hash, tiempo)`; solo corre con el Eclipse activo |

## Observaciones que NO se han cambiado (decisión del autor)

| # | Observación | Dónde |
|---|---|---|
| 1 | **Esperas que se pierden al reiniciar el servidor.** El Fénix (10 min), el renacer del bosque y la segunda oportunidad (10 min) viven en `HashMap` en memoria; reiniciar los reinicia. `CooldownBook` ya guarda esperas de forma persistente | `accessory/Accessories` (`PHOENIX_READY`), `ability/AbilityState` (`FOREST_REBIRTH_READY`), `logic/SkillMilestones` (`SECOND_CHANCE_READY`), `ability/Combos` (`KEYSTONE_COMBO_COOLDOWN`, Y5) |
| 2 | **El parpadeo de la élite teletransporta a la altura máxima del mapa de altura.** En cuevas o en el Nether deja al monstruo en la superficie o sobre el techo. Es una decisión de diseño | `elite/Elites` (modificador `BLINKING`, `getTopY`) |
| 3 | Los mapas por jugador de la observación 1 no se vacían al desconectar (crecen con jugadores únicos, no con el tiempo; irrelevante en la práctica) | los mismos |
| 4 | Pruebas con JUnit simple imposibles para `RoleTree` y `RoleData.setRank`: cargan registros de Minecraft en su inicialización estática. La versión de `RoleData` tras `setRank` y `clearRanks` se comprobó por lectura | `role/RoleTree`, `role/RoleData` |

## Lo que NO se revisó

Lógica interna de Alquimia (`AlchemyRules`, `BrewingRules`), tutorial, matemáticas de Maestría y Amenaza más allá de lo que
cubren sus pruebas, `ForgeRules`, remates 3D (`CastFx`), equilibrio de jefes y de Asalto. Tampoco hay medición real de
rendimiento: R4 (spark con 5–10 jugadores) sigue pendiente porque exige un servidor.
