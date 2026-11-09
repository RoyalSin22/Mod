# Plan de trabajo para Codex — segunda ronda (v59 en adelante)

Igual que `docs/PLAN_CODEX.md`: **una tarea por vez, una rama por tarea**, en orden; cada tarea empieza con el
**bloque común** de `docs/PLAN_CODEX.md` (reglas, Git y autorrevisión) y después el bloque de la tarea. Integración
en `main`: sección «Cómo integrar una rama en `main` tú mismo» de ese mismo archivo.

En paralelo, Claude trabaja `docs/PLAN_CLAUDE.md`. **No toques** mientras Claude tenga abierta su tarea:
`ability/PathEffects`, `ability/Keystones`, `role/RoleTree`, `event/*`, `boss/*`, `build.gradle`,
`fabric.mod.json`, `ability/Combos`.

| Orden | Tarea | Rama | Tamaño |
|---|---|---|---|
| K1 | HUD: barra de jefe, modo minimalista, presets | `codex/v59-hud` | Medio |
| K2 | Mejora de frascos y arenas de jefe con muro de niebla | `codex/v60-frascos-arenas` | Medio |
| K3 | Grimorio: bestiario, buscador, progreso e ilustraciones — **hecho por Claude** (rama `claude/v61-grimorio`); Codex no la repite | `codex/v61-grimorio` | Medio |
| K4 | Armas 3D en mano y rediseño de objetos — **hecho por Claude** (rama `claude/v62-objetos`); Codex no la repite | `codex/v62-objetos` | Medio-grande |
| K5 | Modelos y animaciones de los 3 primeros jefes (GeckoLib) | `codex/v63-jefes-modelos` | Grande — **después de C4 de Claude** |
| K6 | Templos de puzle y Vasos de vida | `codex/v64-templos` | Grande |

---

## K1 — HUD

```text
TAREA K1 (rama codex/v59-hud). Según docs/IDEAS_v47_v55.md §5 (H2–H5), sobre el HUD rediseñado en v50:
- Fase 1: barra de jefe estilo Souls abajo y centrada (nombre, fase y vida; una sola barra que lista varios
  jefes si hay más de uno), usando los datos que ya llegan al cliente; si hace falta un payload nuevo,
  valídalo en el servidor y envíalo solo a jugadores cercanos.
- Fase 2: modo minimalista (oculta lo secundario), presets de disposición (clásico, centrado, compacto) en el editor de Mayús + O, y
  animaciones suaves de avisos y barras. Todo en config/aptitudes-client.json, con textos en los 11 idiomas.
No cambies la pantalla K (5×2, 430×262).
```

## K2 — Frascos y arenas

```text
TAREA K2 (rama codex/v60-frascos-arenas). Según docs/IDEAS_v47_v55.md §2 (S2 y S7):
- Fase 1 (S2): mejorar los frascos de Estus y Éter en la Hoguera con Fragmentos de alma y núcleos de jefe:
  más cargas y más curación, con tope configurable. Regla pura con pruebas; la mejora se guarda en el objeto;
  todo o nada al consumir materiales.
- Fase 2 (S7): al entrar en la sala de un jefe propio (v33), un muro de niebla temporal cierra la arena
  (TemporaryBlocks, sin romper bloques; se abre al morir el jefe o si no queda nadie dentro) y suena un tema
  generado con tools/audio/gen_sounds.py. Si BossManager necesita un gancho, añade solo un método público
  pequeño y descríbelo en el informe: Claude puede estar trabajando en boss/*.
```

## K3 — Grimorio

```text
TAREA K3 (rama codex/v61-grimorio). Según docs/IDEAS_v47_v55.md §4 (G1–G5): bestiario que se desbloquea al
vencer cada criatura o jefe (guardado por jugador, con copyOnDeath), buscador y marcadores, enlaces desde el
tutorial y los tooltips, página de progreso (logros, sendas, jefes) e ilustraciones de cabecera generadas por
programa en el estilo del mod. El grimorio sigue leyendo las recetas del juego. Textos en los 11 idiomas.
```

## K4 — Objetos y armas 3D

```text
TAREA K4 (rama codex/v62-objetos). Según docs/IDEAS_v47_v55.md §6 (A1 y A6), sin dependencias nuevas:
- Fase 1 (A1): modelos 3D de las armas de rol y de evolución EN LA MANO (JSON de Blockbench generados por un
  script en tools/models/), conservando el icono 2D en el inventario (separación de modelos por contexto con
  la API de carga de modelos de Fabric). Arte original.
- Fase 2 (A6): texturas de objetos con más detalle (actualiza tools/textures/gen_items.py; no edites PNG a
  mano), color de nombre por rareza y tooltips con estadísticas e icono de rol.
```

## K5 — Jefes con GeckoLib (después de C4 de Claude)

```text
TAREA K5 (rama codex/v63-jefes-modelos). Solo cuando la tarea C4 de Claude (base de GeckoLib y
docs/V63_JEFES.md) esté en main. Crea los modelos y animaciones (.geo.json y .animation.json, arte original)
de los tres jefes de docs/V63_JEFES.md siguiendo la entidad base de C4, sus texturas generadas por programa y
las animaciones de cada ataque y fase descritas en el diseño. No cambies la lógica de combate de C4.
```

## K6 — Templos y Vasos de vida

```text
TAREA K6 (rama codex/v64-templos). Según docs/IDEAS_v47_v55.md §3 (párrafo «Templos»): estructuras de puzle
por anillo de Amenaza (palancas, placas, puertas, llave de jefe propia del templo) y cofre final con un Vaso de
vida (+1 corazón permanente por jugador, tope 5, con AttributeApplier y copyOnDeath; no se puede usar dos veces
el mismo vaso ni duplicarlo). Generación con el patrón de world/*Feature; estructuras sin romper el terreno al
generarse (ver el arreglo de v56). Logros nuevos con tools/advancements/gen_advancements.py.
```
