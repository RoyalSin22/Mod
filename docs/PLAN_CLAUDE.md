# Plan de trabajo de Claude (v59 en adelante)

Tareas que tocan el núcleo (combate, red, eventos, progresión) o que necesitan diseño. Base:
`docs/IDEAS_v47_v55.md` (§10 y §10b). En paralelo, Codex sigue `docs/PLAN_CODEX_2.md`. **Regla de reparto**: nunca
trabajamos a la vez sobre los mismos archivos; la columna «Archivos» marca lo que Codex no debe tocar mientras
Claude tiene la tarea abierta.

| Orden | Tarea | Versión | Archivos principales | Depende de |
|---|---|---|---|---|
| C1 | **Revisión de código de v42–v58** (Codex, integradas sin revisar): Alquimia, tutorial, Maestría, Amenaza, Asalto y Persecución del Eclipse, Reforja y Templado, remates 3D, jefes | — | Solo lectura; correcciones en ramas `fix/…` | Compilación de `main` |
| C2 | **Rendimiento** R1–R3: caché por jugador de `PathEffects.all` y `Keystones.active`, mapa id → piedra angular; medir con spark | v59 | `ability/PathEffects`, `ability/Keystones`, `role/RoleTree` | C1 |
| C3 | **Convocar el Eclipse** (§2b, Astrolabio del Ocaso; lo autorizan operadores o el líder del grupo) y **Lluvia de brasas por portal** (§10b) | v60b | `event/*`, `souls/SoulsManager` (uso en Hoguera), objeto nuevo, logro | C1 |
| C4 ✔ | **Base de GeckoLib**: dependencia fijada, excepción en `AGENTS.md`, entidad de jefe base, renderizador y una animación de prueba; diseño detallado de los 3 primeros jefes (fases, ataques, arenas, botín) en `docs/V63_JEFES.md` | v63a | `build.gradle`, `fabric.mod.json`, `boss/*` | C1 |
| C5 | **Sinergias** Y1–Y5 (senda ↔ activas, resonancia de grupo, elementos ↔ runas, armadura ↔ senda, combo ↔ piedra angular) | v65 | `ability/*`, `rune/*`, `role/*` | C2 |
| C7 | **Combate avanzado**: S3 parada y golpe de gracia, S5 mensajes en el suelo, S6 ecos de muerte; S4 estamina y rodar queda **para más adelante** (decisión del autor) | v67 | `ability/Combos`, `logic/*`, HUD de combate | C5 |

## Estado real (revisado el 2026-11-24)

Comprobado leyendo el código y `git log`. «Hecho» significa implementado y compilando; **ninguna de estas tareas está
validada en partida**, salvo que se indique.

| Tarea | Estado | Evidencia | Qué falta |
|---|---|---|---|
| C1 Revisión de v42–v58 | **Sin evidencia** | No hay informe, rama `fix/…` ni commit de revisión de esas versiones. Los `fix:` recientes (`37da946`, `01590fc`) corrigen jefes y Errante, no son la revisión | Hacerla, o dar la tarea por descartada (decisión del autor) |
| C2 Rendimiento R1–R3 | **No hecho** | `PathEffects.all` (`PathEffects.java:29`) construye un `EnumMap` y recorre `RoleTree.pathSpecs()` en cada llamada; `Keystones.active` (`Keystones.java:103`) recalcula las piedras activas en cada llamada y `Combos` la consulta en cada remate; no hay caché por jugador ni mapa id → piedra angular; no hay medición con spark en la documentación | Las tres cachés (con invalidación al cambiar roles o rangos) y medir con spark (R4 de `IDEAS_v47_v55.md`) |
| C3 Convocar el Eclipse y Lluvia por portal | **Parcial** | Lluvia por portal: hecha dentro de T1 (`01ed9ff`, `WorldEventManager.watchPortalLighting`). Astrolabio del Ocaso: `ModItems.SUNSET_ASTROLABE`, `item/SunsetAstralabeItem`, receta `sunset_astrolabe.json`, integración en `BonfireBlock` (`747ef50`, `d9bd3ac`) | Ver abajo |
| C4 ✔ GeckoLib | Hecho | Marcado en la tabla; `boss/AnimatedBossEntity` | — |
| C5 Sinergias Y1–Y5 | **Hecho, sin validar en partida** | Y1 `Targeting.java:46` y `RolePassives.java:275`; Y2 `PartyManager.java:131`; Y3 `ElementFusions.java:79`; Y4 `RolePassives`; Y5 `Keystones.triggerFromCombo` llamado en `Combos.java:173`; PR #3 (`27b2c40`). Detalle en `docs/C5_PENDIENTES.md` | Partida (`V65_C5_TESTING_PLAN.md`) |
| C7 Combate avanzado S3/S5/S6 | **No hecho** | No hay código de parada con golpe de gracia para jugadores, mensajes en el suelo ni ecos de muerte. El único «parry» es el de los jefes (`AnimatedBossEntity`, `GuardRules`, tarea B3), distinto de S3. Los commits con «C7» (`b0b1003`) son de la Cripta, otra numeración. Nota: el número de versión «v67» de la tabla ya lo ocupa el árbol unificado | Diseño y código; S4 sigue aplazado por decisión del autor |

**C3 — lo que falta del Astrolabio**

- La regla de permiso (`event/EclipseSummoningRules.canSummon`) solo admite **operadores**; la decisión E1 (2026-11-08)
  también admitía al **líder del grupo**.
- Los mensajes de `EclipseSummoningRules` son texto fijo en inglés (`Text.literal`), no claves de idioma, contra la
  regla de idiomas de `AGENTS.md`.
- No existe textura ni modelo del objeto (`textures/item` y `models/item` no tienen `sunset_astrolabe`), aunque el
  mensaje de `d9bd3ac` lo daba por diferido.
- El logro `summon_eclipse` existe como JSON (`data/aptitudes/advancement/aptitudes_achievements/`, carpeta distinta de
  la que genera `gen_advancements.py`) pero ningún código lo concede (`grep` sin resultados en `src/main/java`).
- Con el Behelit (T5) como vía normal de iniciar el Eclipse, el Astrolabio podría estar solapado; el autor debe decidir
  si se mantiene como vía de operadores, se amplía al líder del grupo o se retira.

## Criterios (los de siempre)

- Rama propia por tarea, commit solo con autorización, informe con resultado **real** de `gradlew build` (desde la
  sesión con el entorno «mod (Aptitudes)», que ya permite compilar aquí).
- Reglas puras con pruebas, configuración validada y documentada, textos en los 11 idiomas (en_us, es_* y
  pt_br/fr_fr/de_de), sin reformatear archivos.
- Exclusiones de `AGENTS.md` §3 y criterio de inspiración sin copia (§0 de las ideas).

## Decisiones del autor (2026-11-08)

- E1: convocan el Eclipse los operadores o el líder del grupo (party), es decir, quien lo creó.
- S4 estamina y rodar: todavía no.
