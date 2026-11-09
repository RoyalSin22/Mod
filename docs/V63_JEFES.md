# V63 — Jefes animados (GeckoLib): diseño de los tres primeros

Estado: **C4 hecho** (base y Coloso Sin Rostro jugable con modelo de prueba). Pendiente: Guardián Desmontado y
Autómata Antiguo (lógica, tarea siguiente de Claude), modelos y animaciones definitivos (K5, Codex) y sus
estructuras con arena.

Criterio (§0 de `IDEAS_v47_v55.md`): inspiración de tono, nombres, formas y ataques **originales**.

## 1. Base común (`boss/AnimatedBossEntity`)

| Pieza | Detalle |
|---|---|
| Dependencia | GeckoLib **4.7.7** (última que admite Fabric Loader 0.16.x; las 4.8+ piden 0.17). Obligatoria en cliente y servidor. Repositorio `dl.cloudsmith.io/public/geckolib3/geckolib/maven/`, sin dependencias transitivas |
| Fases | `BossRules.phase(vida)`: 1 (> 66 %), 2 (≤ 66 %), 3 (≤ 33 %). Al entrar: animación `phase_two`, sonido, aviso a los jugadores de la barra y `onPhase` del jefe |
| Ataques | `BossMoveRules` (puro, JUnit): repertorio con fase mínima, distancias, aviso (`windup`), enfriamiento y peso; sorteo ponderado entre los posibles |
| Aviso | Durante el `windup` el jefe se detiene, mira al objetivo y emite partículas (`telegraph`); al acabar, `strike` |
| Animaciones | Controlador `move` (`idle`/`walk` según se mueva) y controlador `action` con animaciones disparadas por el servidor (`triggerAnim`) con el mismo nombre que el ataque |
| Barra | Barra de jefe vanilla (`ServerBossBar`) para quien lo ve; no recibe afijos de élite ni escalado de Amenaza (lleva nombre y la etiqueta `elite_checked`) |
| Persistencia | Fase en NBT (`aptitudes_phase`); no desaparece |
| Recursos | `geo/entity/<id>.geo.json`, `animations/entity/<id>.animation.json`, `textures/entity/<id>.png`; renderizador `client/render/BossRenderers` (`DefaultedEntityGeoModel`, el hueso `head` sigue la mirada) |

### Contrato para los modelos (K5)

- Huesos obligatorios: `root`, `body`, `head` (gira con la mirada), y los que use cada animación.
- Animaciones obligatorias: `idle` (bucle), `walk` (bucle), `phase_two` y una por ataque con el **mismo id**
  y una duración ≈ `windup` + 10 ticks; el golpe visual debe coincidir con el final del `windup`.
- Arte original hecho en Blockbench (plugin GeckoLib) o generado; los de prueba los genera
  `tools/models/gen_boss_models.py` (`--check`).

## 2. El Coloso Sin Rostro (`aptitudes:faceless_colossus`) — jugable

Guerrero gigante (1,6 × 3,4 bloques) de armadura oscura sin rostro, con espadón.

| Valor | Fase 1 | Fase 2–3 |
|---|---|---|
| Vida / armadura / daño | 320 / 12 / 10 | igual |
| Velocidad | 0,23 | +30 % (armadura agrietada) |
| Daño recibido | ×1 | ×1,25 |

| Ataque | Fase | Distancia | Aviso | Enfriamiento | Efecto |
|---|---|---|---|---|---|
| `sweep` (tajo) | 1+ | 0–4,5 | 18 t | 30 t | Arco de 140° delante, daño ×1, empuje |
| `slam` (golpe descendente) | 1+ | 0–5,5 | 28 t (círculo de críticos en el suelo) | 70 t | Onda de radio 5,5, daño ×1,2, lanza hacia arriba |
| `charge` (embestida) | 2+ | 5–18 | 16 t (humo) | 90 t | Se lanza hacia el objetivo; daño ×0,9 y gran empuje si llega |

- Botín: 8–12 Fragmentos de alma, Corazón de campeón, 3 Esencias de élite (núcleo propio con la estructura).
- Arena prevista: **Ruinas de batalla**, círculo de 21 bloques con columnas rotas como cobertura; muro de niebla
  de las arenas v60 (`BossArenas`).
- Cómo se vence: castigar tras el `slam` (largo enfriamiento) y salir del arco del `sweep`; en fase 2, romper la
  línea de la embestida con columnas.

## 3. El Guardián Desmontado (diseño)

Armadura vacía cuyas piezas flotan. **Mecánica central**: tres piezas (yelmo, escudo, guanteletes) orbitan y
absorben el daño; hay que romperlas **en orden** (la que brilla) para dejar el núcleo expuesto.

| Fase | Comportamiento |
|---|---|
| 1 | Invulnerable salvo la pieza activa (brillo). Ataques: `volley` (las piezas salen disparadas en línea, aviso 20 t) y `ring` (giro de piezas, anillo de radio 4–6, centro seguro, aviso 24 t) |
| 2 | Con una pieza menos: orbitan más rápido; nuevo `dive` (el núcleo cae sobre el objetivo, círculo de 3, aviso 26 t) |
| 3 | Núcleo expuesto, ×1,5 daño recibido; repite `volley` y `dive` con menos enfriamiento |

- Vida 260 (núcleo); cada pieza 40. Arena: **Santuario profundo**, sala de 17×17 con techo alto.
- Botín: Fragmentos de alma, Esencia de élite y el núcleo propio.
- Implementación prevista: las piezas como entidades ligeras hijas (sin IA) o huesos con estado sincronizado.

## 4. El Autómata Antiguo (diseño)

Constructo de piedra y bronce con un ojo que carga un rayo. **Mecánica central**: lanza esferas lentas que el
jugador puede **devolver golpeándolas**; si una devuelta lo alcanza queda **aturdido** 5 s y recibe ×2 de daño.

| Fase | Comportamiento |
|---|---|
| 1 | `orb` (esfera lenta devolvible, aviso 20 t) y `stomp` (pisotón, radio 3, aviso 18 t) |
| 2 | `beam` (rayo cargado en línea recta, aviso 30 t con línea de partículas; lo corta un bloque sólido) |
| 3 | Dos esferas seguidas y `beam` en barrido de 60° |

- Vida 300, armadura 16, inmune al empuje. Arena: **Templo mecánico**, nave rectangular con pilares que cortan
  el rayo. Botín: Fragmentos de alma, Esencia de élite y el núcleo propio.

## 5. Pendientes

1. Lógica del Guardián y del Autómata sobre la base (Claude).
2. Modelos, texturas y animaciones definitivos de los tres (K5, Codex) respetando el contrato del §1.
3. Estructuras (Ruinas de batalla, Santuario profundo, Templo mecánico) con arena de niebla y núcleo propio.
