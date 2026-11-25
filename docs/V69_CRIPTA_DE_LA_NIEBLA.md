# V69 — La llave del Velo y la Cripta de la Niebla: etapa interna del Templo del Custodio

Nota (V70): desde la V70 la cripta es el cuarto nivel del templo hondo (layout 4); ver `docs/V70_TEMPLO_HONDO.md`.

Estado: **aprobado e implementado en la rama `claude/cripta-niebla` (fases C1–C7); pendiente de prueba en partida.** Cierra el pendiente del ROADMAP v65/V68
(«la receta con el Cráneo del Gran Osario y la etapa interna de la mazmorra del Custodio»). Reglas del autor que no se
rompen: la receta **no sustituye las cuatro llaves** ni altera los templos existentes.

## 1. Decisiones del autor (cerradas)

| Tema | Decisión |
|---|---|
| Dónde encaja | **Tras el Custodio, opcional.** Una puerta interior en la cámara del Custodio que solo abre el objeto nuevo. Quien no lo tenga completa el templo como hoy. |
| Qué es | **Reto de oleadas de la Niebla** (Errantes, Osarios y no muertos) con un clímax, sin jefe nuevo. |
| Recompensa | **Botín y lore, sin poder nuevo.** Sin Behelit extra, sin objeto único, sin cambios de equilibrio de combate o de progresión. |
| El objeto | **Llave de un solo uso: se consume** al abrir la puerta interior. No queda ligada a un templo. |
| Materiales | Jirón de Velo Espectral, fragmento de alma y Cráneo del Gran Osario (decidido en V68). |

## 2. Punto de partida (verificado en el código)

- **Templo del Custodio** (`layout = 1`, T3/T4): `world/CustodianTempleFeature`, plano en `temple/CustodianTempleLayout`
  (31×31, radio 15). Superficie: atrio con el Autómata y puzle. Subsuelo (y −9…−1, casco de piedra): escalera,
  antesala, Puerta del Custodio (consume el Ancla del Velo) y cámara del Custodio (x −10…10, z 6…14, suelo en y −8) con el
  Relicario y las cuatro cámaras de recompensa (cobre, hierro, oro y diamante, la última con el Embrión del Umbral).
- **Flujo** en `temple/TempleFlowRules`: `DORMANT → AUTOMATON → PUZZLE → DOOR_LOCKED → CUSTODIAN → CLEARED`. Se guarda como
  número en `TempleSealBlockEntity` (solo se añaden valores al final). Los templos antiguos tienen `layout = 0`.
- **Bajo la cámara del Custodio no hay nada**: del casco (y −9) hacia abajo el volumen está libre y la profundidad no tiene
  el límite de chunks de un `Feature` (el radio horizontal de 15 sí se mantiene).
- **Reparto de la Niebla** (`mob/FogMobRules`): 10 % Osario, 10 % Errante y el resto no muertos vanilla.
- **Elites** (`elite/Elites.makeElite`): permite hacer campeón a una criatura. Una criatura sin sesión de Niebla no suelta
  Jirón ni Núcleo (`WorldEventManager.canDropVeil`), lo que evita que la cripta sea una granja de esos materiales.

## 3. La receta

| Cosa | Propuesta |
|---|---|
| Objeto | **Llave del Velo** (`llave_del_velo`), «Veil Key». Rareza rara, apila hasta 16 |
| Receta | Mesa del gremio, **sin forma** (`aptitudes:guild_crafting_shapeless`), 8 ingredientes: **1 Cráneo del Gran Osario + 3 Jirón de Velo Espectral + 4 Fragmentos de alma** |
| Resultado | 1 Llave del Velo |
| Uso | Clic derecho sobre la **Trampilla de la Cripta** en la cámara del Custodio, con el templo en `CLEARED`. Se consume (también en creativo, como el Ancla) |
| Fuera de la puerta | Sin función: no abre cámaras, no sustituye ninguna de las cuatro llaves ni el Ancla |

Ritmo de obtención: el Gran Osario aparece **una vez por Niebla** y la Niebla tiene 10 días de espera, así que cada jugador
elegible consigue como mucho **un Cráneo cada 10 días**, y por tanto una llave. El coste en Jirón (3, el mismo número que
el Ancla) y fragmentos es asequible; el limitador real es el jefe.

## 4. La Cripta de la Niebla

**Dónde.** Un nivel nuevo **bajo la cámara del Custodio**: sala de 21×9 (x −10…10, z 6…14) con suelo en y −17, techo en
y −11 y casco de piedra hasta y −19. Se entra por una **Trampilla de la Cripta** (3×3, bloque nuevo, en el suelo de la cámara
del Custodio, x −1…1 y z 10…12, junto al Relicario) y una **escalera de 8 escalones** hacia el oeste, bajo la cámara.

**Quién genera qué.** Solo los templos **generados después de este cambio** (`layout = 2`) tienen cripta. Los templos del
Custodio ya generados (`layout = 1`) y los antiguos (`layout = 0`) no cambian; en un `layout = 1` la trampilla no existe y
la llave no tiene dónde usarse (el mensaje lo dice si se intenta con otro bloque). El flujo de `layout = 2` hasta `CLEARED`
es idéntico al de `layout = 1`.

**Estado propio, sin tocar `Flow`.** Se añade `crypt` al controlador (NBT al final, ausente = bloqueada):

```
LOCKED ──Custodio cae (CLEARED)──▶ READY ──Llave del Velo (se consume)──▶ OPEN ──un jugador baja──▶ WAVE_1 → WAVE_2 → WAVE_3 → CLIMAX ──cae──▶ DONE (cofre)
```

- **Oleadas:** 3 oleadas y un clímax. Cada oleada aparece de golpe en los puntos de la sala y termina al matar a todas.
- **Composición** (`CryptRules.choose`, propia para no tocar `FogMobRules`): 30 % Errante Velado, 25 % Osario Quebrado y 45 %
  no muertos vanilla. Las criaturas llevan `Elites.CHECKED` y no sueltan Jirón ni Núcleo (sin sesión de Niebla).
- **Tamaño:** `base = 5 / 7 / 9` por oleada (4 / 6 / 8 hasta la V70.1), **+2 por jugador adicional** dentro de la sala, **×(1 + 0,25 × anillo de Amenaza)**
  (el mismo escalado que el Autómata del templo, `TempleRules.clampRing`). Tope de vivas a la vez: 12.
- **Clímax:** un **Campeón de la Cripta**, Osario Quebrado hecho campeón (`Elites.makeElite(…, champion = true)`) con vida
  +25 % por anillo y **2 Errantes de escolta**. No es un jefe nuevo: sin modelo, fases ni barra propias más allá de las del
  campeón. No suelta el trofeo ni Núcleo (para que la etapa no pueda ser una fuente de llaves).
- **Sin atascos ni trampas:** el progreso (oleada completada) se guarda en el bloque. Si **no queda ningún jugador vivo en la
  sala durante 30 s**, la oleada en curso se reinicia (las criaturas se retiran y vuelve a empezar *esa* oleada); la llave
  no se vuelve a gastar. Si el servidor se reinicia, la oleada en curso se reinicia igual. La escalera nunca se sella.
- **Fin de la etapa:** al caer el clímax aparece un cofre (`custodian_crypt`) en el centro de la sala y se desbloquea el lore.

## 5. Recompensa (botín y lore, sin poder nuevo)

**Cofre `custodian_crypt`** (cofre normal, como las cámaras de T4: en grupo se reparte lo que haya). Por encima de la cámara de
diamante en cantidad, **sin Embrión del Umbral ni objetos nuevos**:

| Tiradas | Contenido (peso) |
|---|---|
| 5–7 | Bloque de diamante (3), manzana dorada encantada (2), fragmento de netherita (3), bloque de esmeralda (3), Fragmento de alma 8–16 (5), Corazón de campeón (2), Cupón de armería (2), Emblema de maestría (1), frascos de experiencia (3) |

(Los valores provisionales se ajustan en `docs/BALANCE.md` tras la prueba; con `diamond` como techo de referencia: 4–6 tiradas.)

**Lore.** Los tres documentos de la Primera Era que el catálogo de lore marca hoy como *bloqueados por falta de documentos
coleccionables* encajan con esta etapa: `lore.faction.concordance` (hallazgo de documentos), `lore.cosmology.veil_frontier`
(documentos antiguos del templo) y `lore.faction.permanence` (archivos del Primer Eclipse). Se propone un nuevo desbloqueo
`crypt_cleared_lore` en `LoreRules` (y su gancho al completar la etapa). **Esto toca el lore de la sesión de narrativa**:
se coordina con ella antes de implementar y puede quedarse en una sola entrada.

## 6. Piezas de código

| Pieza | Descripción |
|---|---|
| `temple/CryptRules` (puro) | Estados y transiciones, tamaño de oleada por oleada/jugadores/anillo, tope de vivas, reparto de variantes, reinicio por inactividad |
| `temple/CustodianTempleLayout` | Nuevo parámetro de plano: sala, escalera, casco, trampilla y cofre; celdas nuevas al final del `enum Cell` |
| `world/CustodianTempleFeature` | Excavar y construir la cripta; comprobar agua y lava en el volumen ampliado (si hay, no se coloca) |
| `temple/TempleSealBlockEntity` | Campos `crypt` y `crypt_wave` al final del NBT; tick de oleadas; vuelta a los valores por defecto con NBT antiguo |
| `temple/CryptHatchBlock` | Bloque de la trampilla: usa el objeto en el servidor, consume la llave y abre el hueco |
| `item/VeilKeyItem` + `ModItems` | El objeto con tooltip; textura generada en `gen_items.py` |
| `recipe/llave_del_velo.json` | Receta sin forma de la Mesa del gremio |
| `data/.../loot_table/chests/custodian_crypt.json` | Botín del cofre |
| `guide/LoreRules` (+ coordinación con narrativa) | Desbloqueo `crypt_cleared_lore` |
| `temple/TempleFlowRules` | `LAYOUT_CRYPT = 2` aceptado por `clampLayout`; el resto del flujo no cambia |

## 7. Configuración

Cada campo con valor por defecto seguro, límite en `validated(...)`, fila en `docs/CONFIGURATION.md`, tooltip en los 5 idiomas
y fila en `tools/lang/config_catalog.py`.

| Campo | Defecto | Límites | Qué hace |
|---|---|---|---|
| `cryptEnabled` | `true` | — | Los templos nuevos generan la cripta |
| `cryptIdleResetSeconds` | 30 | 5–300 | Segundos sin jugadores vivos en la sala antes de reiniciar la oleada |
| `cryptWaveSizePercent` | 100 | 25–300 | Porcentaje del tamaño de las oleadas (balance rápido) |

## 8. Pruebas

- **JUnit:** `CryptRulesTest` (transiciones, bloqueo antes de `CLEARED`, tamaño con 1…4 jugadores y anillos 0…5, tope de
  vivas, reinicio por inactividad, NBT antiguo ausente = bloqueada) y `CustodianTempleLayoutTest` ampliado (la cripta cabe
  en el radio 15, el casco la envuelve y la trampilla no pisa el Relicario ni el spawn del Custodio).
- **Recursos:** receta (8 ingredientes, caben en 3×3, mismo tipo que el Ancla), tabla de botín, textos en 5 idiomas,
  textura y modelo del objeto, bloque de la trampilla.
- **Servidor dedicado:** generar un templo con `/place feature`, comprobar el volumen, abrir la trampilla con la llave por
  consola, forzar las oleadas y el clímax y verificar el cofre. Sin jugador no se cubre el combate.
- **En partida (autor):** equilibrio de oleadas en solitario y en grupo, legibilidad de la sala, reinicio por inactividad,
  sensación de recompensa frente al coste de la llave y el lore desbloqueado.

## 9. Fases de implementación

1. `CryptRules` y pruebas. 2. Plano y generación de la cripta (`layout = 2`) con comprobaciones de terreno. 3. Bloque de la
trampilla, objeto, receta y textos. 4. Estado persistente y oleadas en el controlador. 5. Clímax, cofre y botín. 6. Lore
(coordinado con narrativa). 7. Configuración y documentación. 8. Verificación: build, pruebas y servidor.
Una rama (`claude/cripta-niebla`) y un commit por fase.

## 10. Riesgos

- **Terreno y worldgen:** el volumen excavado crece (21×9 más escalera) y baja hasta y −19 desde la superficie. Si cae sobre
  agua, lava o cuevas, el feature no se coloca, lo que baja la frecuencia efectiva del templo. Se mide en la prueba.
- **Templos ya generados:** los de T3/T4 no tendrán cripta. Si hay mundos de prueba con ellos, no ganan contenido.
- **Equilibrio:** el botín supera a la cámara de diamante pero se limita a una llave cada 10 días por jugador; las cifras de
  oleadas son estimaciones. Hace falta prueba en partida.
- **Botín de criaturas de oleada:** suprimir drops y XP en criaturas vanilla exige una técnica concreta (tabla de botín vacía
  por NBT); se verifica en la implementación y, si no funciona, se acepta el botín normal con el tope de vivas.
- **Lore compartido:** el desbloqueo toca `LoreRules` y los metadatos de la sesión de narrativa; se coordina antes de tocar.
- **Sin jefe nuevo:** si más adelante se quiere un jefe en el clímax, es una tarea aparte con su modelo y fases.
