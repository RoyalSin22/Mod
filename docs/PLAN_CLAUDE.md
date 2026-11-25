# Plan de trabajo de Claude (v59 en adelante)

Tareas que tocan el núcleo (combate, red, eventos, progresión) o que necesitan diseño. Base:
`docs/IDEAS_v47_v55.md` (§10 y §10b). En paralelo, Codex sigue `docs/PLAN_CODEX_2.md`. **Regla de reparto**: nunca
trabajamos a la vez sobre los mismos archivos; la columna «Archivos» marca lo que Codex no debe tocar mientras
Claude tiene la tarea abierta.

| Orden | Tarea | Versión | Archivos principales | Depende de | Estado real (2026-11-24) y evidencia |
|---|---|---|---|---|---|
| C1 | **Revisión de código de v42–v58** (Codex, integradas sin revisar): Alquimia, tutorial, Maestría, Amenaza, Asalto y Persecución del Eclipse, Reforja y Templado, remates 3D, jefes | — | Solo lectura; correcciones en ramas `fix/…` | Compilación de `main` | **Parcial / sin informe.** No hay un informe de revisión de C1 en el repositorio; solo `docs/REVISION_MIXINS.md` (mixins, v67) y correcciones sueltas (`ccbd990`). Decisión pendiente: ¿se da por hecha o se rehace? |
| C2 | **Rendimiento** R1–R3: caché por jugador de `PathEffects.all` y `Keystones.active`, mapa id → piedra angular; medir con spark | v59 | `ability/PathEffects`, `ability/Keystones`, `role/RoleTree` | C1 | **No hecha.** `PathEffects.all` y `Keystones.active` recalculan en cada llamada (sin caché; no hay commits de rendimiento) y no hay medición con spark |
| C3 | **Convocar el Eclipse** (§2b, Astrolabio del Ocaso; lo autorizan operadores o el líder del grupo) y **Lluvia de brasas por portal** (§10b) | v60b | `event/*`, `souls/SoulsManager` (uso en Hoguera), objeto nuevo, logro | C1 | **Hecha en código (2026-11-24, sin commit aún).** Astrolabio: `d9bd3ac` (objeto y Hoguera, solo operadores) completado con líder del grupo, espera común de 3 días con el Behelit, validación en el servidor, mensajes en 11 idiomas y logro `eclipse_caller` (`event/EclipseSummoningRules` + `EclipseSummoningRulesTest`). Lluvia de brasas por portal: hecha en T1 (`event/EventTriggerRules`, `WorldEventManager.watchPortalLighting`). Faltan el arte del Astrolabio y prueba en partida |
| C4 ✔ | **Base de GeckoLib**: dependencia fijada, excepción en `AGENTS.md`, entidad de jefe base, renderizador y una animación de prueba; diseño detallado de los 3 primeros jefes (fases, ataques, arenas, botín) en `docs/V63_JEFES.md` | v63a | `build.gradle`, `fabric.mod.json`, `boss/*` | C1 | Hecha (`boss/AnimatedBossEntity`) |
| C5 ✔ | **Sinergias** Y1–Y5 (senda ↔ activas, resonancia de grupo, elementos ↔ runas, armadura ↔ senda, combo ↔ piedra angular) | v65 | `ability/*`, `rune/*`, `role/*` | C2 | **Hecha en código** (PR #3, `27b2c40`; Y1 `a204ef1`/`ce7204e`, Y2 `PartyManager:131`, Y3 `ElementFusions:79`, Y4 `efa926b`, Y5 `372bb81`, logro «Sinergista» `7e0ab82`). Detalle en `docs/C5_PENDIENTES.md`; falta validar en partida. Nota: se hizo sin C2 |
| C7 | **Combate avanzado**: S3 parada y golpe de gracia, S5 mensajes en el suelo, S6 ecos de muerte; S4 estamina y rodar queda **para más adelante** (decisión del autor) | v67 | `ability/Combos`, `logic/*`, HUD de combate | C5 | **Fase 1 (S3) hecha en código** (2026-11-25, rama `claude/c7-combate-avanzado`): parada y golpe de gracia en `combat/ParryRules` y `combat/Parries`; sin validar en partida. **Faltan** S5 (mensajes en el suelo) y S6 (ecos de muerte), cada una con su documento de diseño. Ojo: el commit `b0b1003` usa «C7» para la Cripta de la Niebla (V69), que es otra cosa |

## Criterios (los de siempre)

- Rama propia por tarea, commit solo con autorización, informe con resultado **real** de `gradlew build` (desde la
  sesión con el entorno «mod (Aptitudes)», que ya permite compilar aquí).
- Reglas puras con pruebas, configuración validada y documentada, textos en los 11 idiomas (en_us, es_* y
  pt_br/fr_fr/de_de), sin reformatear archivos.
- Exclusiones de `AGENTS.md` §3 y criterio de inspiración sin copia (§0 de las ideas).

## Decisiones del autor (2026-11-08)

- E1: convocan el Eclipse los operadores o el líder del grupo (party), es decir, quien lo creó.
- S4 estamina y rodar: todavía no.
