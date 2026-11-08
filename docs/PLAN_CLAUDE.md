# Plan de trabajo de Claude (v59 en adelante)

Tareas que tocan el núcleo (combate, red, eventos, progresión) o que necesitan diseño. Base:
`docs/IDEAS_v47_v55.md` (§10 y §10b). En paralelo, Codex sigue `docs/PLAN_CODEX_2.md`. **Regla de reparto**: nunca
trabajamos a la vez sobre los mismos archivos; la columna «Archivos» marca lo que Codex no debe tocar mientras
Claude tiene la tarea abierta.

| Orden | Tarea | Versión | Archivos principales | Depende de |
|---|---|---|---|---|
| C1 | **Revisión de código de v42–v58** (Codex, integradas sin revisar): Alquimia, tutorial, Maestría, Amenaza, Asalto y Persecución del Eclipse, Reforja y Templado, talentos por datapack, remates 3D, jefes | — | Solo lectura; correcciones en ramas `fix/…` | Compilación de `main` |
| C2 | **Rendimiento** R1–R3: caché por jugador de `PathEffects.all` y `Keystones.active`, mapa id → piedra angular; medir con spark | v59 | `ability/PathEffects`, `ability/Keystones`, `role/RoleTree` | C1 |
| C3 | **Convocar el Eclipse** (§2b, Astrolabio del Ocaso; lo autorizan operadores o el líder del grupo) y **Lluvia de brasas por portal** (§10b) | v60b | `event/*`, `souls/SoulsManager` (uso en Hoguera), objeto nuevo, logro | C1 |
| C4 | **Base de GeckoLib**: dependencia fijada, excepción en `AGENTS.md`, entidad de jefe base, renderizador y una animación de prueba; diseño detallado de los 3 primeros jefes (fases, ataques, arenas, botín) en `docs/V63_JEFES.md` | v63a | `build.gradle`, `fabric.mod.json`, `boss/*` | C1 |
| C5 | **Sinergias** Y1–Y5 (senda ↔ activas, resonancia de grupo, elementos ↔ runas, armadura ↔ senda, combo ↔ piedra angular) | v65 | `ability/*`, `rune/*`, `role/*` | C2 |
| C6 | **Datapack D1–D2**: valores de balance y sendas por datapack, sobre la base de talentos de v55 | v66 | `role/datapack/*`, `role/RoleTree`, `config/*` | C2, C5 |
| C7 | **Combate avanzado**: S3 parada y golpe de gracia, S5 mensajes en el suelo, S6 ecos de muerte; S4 estamina y rodar queda **para más adelante** (decisión del autor) | v67 | `ability/Combos`, `logic/*`, HUD de combate | C5 |

## Criterios (los de siempre)

- Rama propia por tarea, commit solo con autorización, informe con resultado **real** de `gradlew build` (desde la
  sesión con el entorno «mod (Aptitudes)», que ya permite compilar aquí).
- Reglas puras con pruebas, configuración validada y documentada, textos en los 11 idiomas (en_us, es_* y
  pt_br/fr_fr/de_de), sin reformatear archivos.
- Exclusiones de `AGENTS.md` §3 y criterio de inspiración sin copia (§0 de las ideas).

## Decisiones del autor (2026-11-08)

- E1: convocan el Eclipse los operadores o el líder del grupo (party), es decir, quien lo creó.
- S4 estamina y rodar: todavía no.
