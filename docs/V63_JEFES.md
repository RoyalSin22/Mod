# V63 — Jefes animados (GeckoLib): diseño de los tres primeros

Estado: **base, Coloso Sin Rostro, Guardián Desmontado y Autómata Antiguo jugables** con modelos de prueba.
Fase 3 implementada en v65 (provisional, ver §3 y §4). Pendiente: validación de combate en partida (esferas, devolución,
aturdimiento, piezas), modelos y animaciones definitivos (K5, Codex) y las estructuras con arena.

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

Guerrero gigante (1,6 × 3,8 bloques; v65 lo subió desde 3,4 según la hoja de arte) de armadura oscura sin rostro, con espadón.

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
- Arena: **Ruinas de batalla** (v65), círculo de 21 bloques con columnas rotas como cobertura. Se levanta al invocar el sello (las columnas duran 20 min) y se cierra con niebla al entrar; muro de niebla
  de las arenas v60 (`BossArenas`).
- Cómo se vence: castigar tras el `slam` (largo enfriamiento) y salir del arco del `sweep`; en fase 2, romper la
  línea de la embestida con columnas.

## 3. El Guardián Desmontado (`aptitudes:dismantled_guardian`) — jugable

Implementado en `boss/GuardianEntity` con `PieceBossRules`: tres piezas en orden (yelmo, escudo, guanteletes),
solo la activa absorbe daño, sin piezas el núcleo recibe ×1,5. Ataques: `volley` (tajo lineal de 12 bloques,
fase 1+), `ring` (anillo entre 3 y 6 bloques, centro seguro, fase 1+), `dive` (caída sobre el objetivo, radio 3,
fase 2+). Vida 260, armadura 8, daño 9. Botín: fragmentos de alma y esencia de élite.

Diseño original:

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

## 4. El Autómata Antiguo (`aptitudes:ancient_automaton`) — jugable

Implementado en `boss/AutomatonEntity` y `boss/AutomatonOrbEntity` con `PieceBossRules`: las esferas vuelan rectas;
un jugador que las golpea las devuelve, y la esfera devuelta que alcanza al Autómata lo aturde 5 s (recibe ×2).
Ataques: `orb` (esfera, fase 1+), `stomp` (pisotón, radio 3,5, fase 1+), `beam` (rayo que se corta en bloques
sólidos, fase 2+). Vida 300, armadura 16, daño 7. Inmune al empuje.

Diseño original:

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

1. Hecho en v65: fase 3 del Guardián (`volley` 30 t y `dive` 50 t de enfriamiento) y del Autómata (dos esferas en abanico de 8° en vez de seguidas, rayo en barrido de 60°). El aviso del rayo sigue dibujando una línea y el barrido no corta por bloques sólidos.
2. Validar en partida el combate de ambos (piezas en orden, esferas devueltas, aturdimiento, rayo cortado por bloques).
3. Modelos, texturas y animaciones definitivos de los tres (K5, Codex) respetando el contrato del §1.
4. Estructuras (Ruinas de batalla, Santuario profundo, Templo mecánico) con arena de niebla y núcleo propio.


## 6. El Coloso de Hierro (`aptitudes:iron_colossus`) — v66, sin validar en partida

- **Clase:** `boss/IronColossusEntity` (extiende `AnimatedBossEntity`). Vida 280, armadura 10, daño 9, velocidad 0,22.
  Fase 2 (armadura agrietada): +25 % de velocidad y +20 % de daño recibido.
- **Ataques:** `sweep` (arco de 140°, desde la fase 1), `slam` (golpe vertical, radio 5,5, empuje hacia arriba) y
  `charge` (embestida a 5–18 bloques, desde la fase 2).
- **Modelo:** `geo/entity/iron_colossus.geo.json`, 20 huesos y textura de 512 px. Fuente: `tools/models/blockbench/iron_colossus.bbmodel`.
- **Contrato ampliado respecto del §1:** además de `root`, `body`, `head`, `cape`, `right_arm`, `left_arm`, `sword`,
  `right_leg` y `left_leg`, el modelo añade `chest` (cintura, pivote en y=28), `right_shoulder` y `left_shoulder`,
  `right_forearm` y `right_hand` (la espada cuelga de la mano), `left_forearm` y `left_hand`, `right_shin` y
  `left_shin` (rodillas), y `right_foot` y `left_foot` (tobillos, hijos de la espinilla).
- **Animaciones:** `idle` (2 s, bucle), `walk` (2 s, bucle), `phase_two` (2 s, mantiene el último fotograma), `sweep`
  (1 s), `slam` (1,1 s) y `charge` (1 s). En reposo y en los ataques las dos manos sujetan la espada.
- **Evento de partícula:** la animación `slam` dispara `aptitudes:iron_colossus_shockwave` en el locator `impact`
  (punto de contacto de la hoja, delante del personaje) y el sonido `minecraft:entity.generic.explode` en t≈0,58.
  La partícula está en `particles/iron_colossus_shockwave.json` con su textura de anillo (32×32).
- **Huevo:** `aptitudes:iron_colossus_spawn_egg` en la pestaña creativa. Sin estructura ni aparición natural.
- **Pendiente:** validar en partida la partícula disparada desde la animación, el aspecto de la textura y la postura
  del golpe; y convertir la onda visual en daño de área (hoy solo el empuje de `slam` hace daño).
