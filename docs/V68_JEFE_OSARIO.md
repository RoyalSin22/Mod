# V68 — El Gran Osario Quebrado: jefe de la Niebla y su trofeo

Estado: **diseño aprobado por el autor; en implementación** (rama `claude/jefe-osario`). Cierra el pendiente del ROADMAP v65
(«definir y registrar el jefe y el trofeo del Osario Quebrado»). La receta del trofeo y la etapa interna de la mazmorra del
Custodio **quedan fuera** (tarea posterior, con su propio diseño).

## 1. Decisiones del autor (cerradas)

| Tema | Decisión |
|---|---|
| Alcance | Solo jefe y trofeo. Sin receta ni mazmorra interna. |
| Aparición | Al final de la Niebla de almas, ligado a la sesión de Niebla en curso. |
| Trofeo | Material narrativo: **no** es un núcleo de arma (el tope de +1,5 de daño por núcleos no cambia). |
| Combate | Jefe de almas y Niebla, con fases (la base de `AnimatedBossEntity`). |
| Nombre del jefe | **Gran Osario Quebrado** (`gran_osario_quebrado`). |
| Nombre del trofeo | **Cráneo del Gran Osario** (`craneo_gran_osario`), en inglés «Great Ossuary Skull». |
| Fin de la Niebla con el jefe vivo | Se disuelve a los 5 minutos (6000 ticks), sin botín ni XP. |
| Reparto | Un trofeo por cada jugador que le hizo al menos el 10 % de su vida en daño. |

Se aprobó el cambio de «Cráneo del Osario Mayor» a «Cráneo del Gran Osario» para que el objeto nombre al jefe.

## 2. Concepto

El Osario Quebrado común es un resto que la Esencia mantiene en pie sin conciencia (bestiario). El Gran Osario es lo que
queda cuando la Niebla ha reunido demasiados: una osamenta enorme que **guarda y cosecha almas**. No tiene voluntad; la
Niebla lo levanta y lo retira. Identidad de combate: **zona, drenaje y apariciones**, no golpe pesado, para distinguirlo
del Coloso (cuerpo a cuerpo), el Guardián (piezas) y el Autómata (proyectiles).

Reglas de lore: no se nombra la Conciencia reservada, no se usa «ancestral» ni «primordial» y no se afirma el origen de
la Niebla (el lore la deja abierta).

## 3. Aparición

Lógica pura en `boss/OsarioBossRules` (con JUnit); el enganche vive en `WorldEventManager`, que ya conoce la sesión de
Niebla (`state.fogSession`, `eventTime`, `duration`).

| Regla | Valor |
|---|---|
| Condición | Niebla activa, dificultad distinta de Pacífica, al menos un jugador en el Overworld, y la sesión **sin jefe aún** |
| Momento | `eventTime ≥ 75 %` de la duración (config `osarioBossFogPercent`) |
| Una vez por sesión | `WorldEventState.bossSession` (UUID, en NBT; ausente en mundos anteriores = ninguna) |
| Dónde | Jugador del Overworld al azar; 24–36 bloques de distancia, suelo sólido, 3 de aire, sin fluido. Hasta 8 intentos por tick-control (cada 100 ticks); si no encuentra sitio, reintenta hasta el final de la Niebla |
| Aviso | Mensaje global (`event.aptitudes.soul_fog.boss`) y barra de jefe |
| Fin de la Niebla | Si sigue vivo: a los `osarioBossLingerTicks` (6000) se disuelve con partículas, **sin botín ni XP** |
| Mundo reiniciado | Al cargar, el jefe conserva su sesión; si la sesión ya cerró hace más de 6000 ticks, se disuelve |
| Interruptor | `osarioBossEnabled` (por defecto `true`) |

No aplica Amenaza ni afijos de élite (la base ya añade `Elites.CHECKED`). No usa sellos de invocación (retirados en T6).

## 4. Estadísticas y ataques

Base `AnimatedBossEntity` (fases 1 > 66 %, 2 ≤ 66 %, 3 ≤ 33 %; barra de jefe; postura; ataques avisados).

| Valor | Gran Osario | Referencia |
|---|---|---|
| Vida | 340 | Coloso 320, Autómata 300 |
| Armadura | 8 | Coloso 12, Autómata 16 (menos armadura: es espectral) |
| Daño base | 8 | Coloso 10, Autómata 7 |
| Velocidad | 0,20 | Coloso 0,23 |
| Resistencia al empuje | 0,6 | |
| Tamaño | 1,6 × 3,2 | El Osario común es menor |

| Ataque | Fase | Distancia | Aviso | Espera | Peso | Efecto |
|---|---|---|---|---|---|---|
| `rake` zarpazo de huesos | 1+ | 0–4,5 | 16 t | 30 t | 3 | Arco de 120° delante, daño ×1, empuje corto. Se puede desviar (parry) |
| `mist_ring` anillo de niebla | 1+ | 0–6 | 28 t | 70 t | 2 | Círculo de radio 5,5 centrado en el jefe, daño ×1,1 y Lentitud I 40 t |
| `soul_lash` latigazo de almas | 1+ | 4–14 | 24 t | 80 t | 2 | Haz en línea de 14 bloques y 1,2 de ancho, daño ×0,9. Cura al jefe un 1,5 % de su vida por jugador alcanzado (máximo 3 % por golpe) |
| `wail` lamento | 2+ | 0–32 | 34 t | 400 t | 1 | Invoca 2 Errantes Velados (máximo 4 vivos del jefe). Solo se usa si hay menos de 2 vivos |
| `harvest` cosecha de almas | 3+ | 0–8 | 40 t | 220 t | 2 | Onda de radio 8, daño ×1,4 y empuje largo. Por cada Errante invocado en 12 bloques el jefe se cura un 2 % (máximo 6 %) y ese Errante se consume |

**Pacto de almas** (rasgo pasivo): mientras haya Errantes invocados vivos, el jefe recibe un 35 % menos de daño. Matar a
los acompañantes es el modo de abrirlo; en solitario es manejable y en grupo hay que repartirse.

Los Errantes invocados llevan la etiqueta `aptitudes.boss_add`: **no sueltan Jirón ni dan XP**, para que el jefe no sea una
granja del material de la Niebla. Se retiran con el jefe (mismo código de retirada que los de la Niebla).

## 5. Trofeo y botín

| Cosa | Detalle |
|---|---|
| Objeto | `aptitudes:craneo_gran_osario`, «Cráneo del Gran Osario» / «Great Ossuary Skull». Apila hasta 16, rareza épica |
| Función | Ninguna mecánica: se guarda para la receta futura. No se aplica a armas |
| Reparto | `OsarioBossRules.eligible(daño, vidaMáx, 10 %)`. El jefe guarda el daño por jugador (UUID → daño, en NBT) y, al morir, cada elegible recibe 1 trofeo directo al inventario (si no cabe, cae a sus pies) |
| Además | 6–10 `aptitudes:soul_shard` por jugador elegible. No suelta Núcleo del Osario |
| Bestiario | Entrada `gran_osario_quebrado` (jefe, Overworld), botín trofeo y fragmento; se desbloquea con la primera muerte (los jefes enseñan todo salvo el lore ya en la primera) |

Efecto en el logro «Cronista»: el bestiario pasa de 13 a **14** entradas. Quien ya tiene el logro lo conserva. Quien no
lo tiene necesita también el jefe.

## 6. Configuración

Cada campo con valor por defecto seguro, límite en `validated(...)`, fila en `docs/CONFIGURATION.md`, tooltip
`config.aptitudes.field.<campo>` en los 5 idiomas y fila en `tools/lang/config_catalog.py`.

| Campo | Defecto | Límites | Qué hace |
|---|---|---|---|
| `osarioBossEnabled` | `true` | — | Activa la aparición del jefe |
| `osarioBossFogPercent` | 75 | 10–100 | Porcentaje de la Niebla a partir del cual aparece |
| `osarioBossLingerTicks` | 6000 | 0–72000 | Ticks que sigue vivo tras cerrarse la Niebla |
| `osarioBossMinDamagePercent` | 10 | 1–100 | Daño mínimo (% de su vida) para recibir trofeo |

## 7. Piezas del código

| Pieza | Archivo |
|---|---|
| Reglas puras (momento, elegibilidad, curas, escudo, tope de acompañantes) | `boss/OsarioBossRules` + `OsarioBossRulesTest` |
| Entidad | `boss/GreatOssuaryEntity` (extiende `AnimatedBossEntity`), registrada en `boss/BossEntities` con huevo de aparición |
| Enganche de aparición y cierre | `event/WorldEventManager` (cambio mínimo; lo comparte la sesión de narrativa) y `event/WorldEventState` (`bossSession`) |
| Trofeo | `item/ModItems` (`CRANEO_GRAN_OSARIO`), textura con `tools/textures/gen_items.py` |
| Bestiario | `guide/BestiaryRules` (entrada nueva) + actualizar `BestiaryRulesTest` y `BestiaryRevealRulesTest` |
| Modelo y animaciones | `geo/entity`, `animations/entity` y textura; provisionales con `tools/models/gen_boss_models.py`; el arte definitivo se puede sustituir sin tocar código (contrato en `docs/V63_JEFES.md`) |
| Cliente | `client/render/BossRenderers` |
| Idiomas | Nombre, fases, aviso del evento, objeto, tooltip, bestiario (nombre, consejo, lore provisional) y config, en los 5 idiomas |

El lore del Grimorio (T7, `lore_entries.json`) **no se toca**: añadir una entrada exige metadatos y ganchos de la sesión de
narrativa. El bestiario usa su texto provisional propio (`bestiary.aptitudes.<id>.lore`), como las otras 13 criaturas.

## 8. Pruebas

- JUnit: `OsarioBossRulesTest` (momento de aparición y una sola vez por sesión, elegibilidad por daño con 0, justo en el
  umbral y por encima, tope de curas, escudo con y sin acompañantes, tope de 4 vivos, disolución tras el cierre) y
  actualización de las pruebas del bestiario (14 entradas).
- Recursos: existencia de textura, modelo, animaciones, objeto y textos en los 5 idiomas.
- Arranque real: servidor dedicado con `/summon`, 1200 ticks, sin excepciones (procedimiento de `docs/TESTING.md`), y un
  ciclo de Niebla forzada para ver que aparece al 75 % y se disuelve al cierre.
- En partida (autor): sensaciones de combate, equilibrio en solitario y en grupo, y legibilidad de los avisos.

## 9. Fases de implementación

1. Reglas puras y pruebas. 2. Entidad, ataques y registro. 3. Enganche en la Niebla y estado persistente. 4. Trofeo,
reparto, bestiario y botín. 5. Modelo provisional y cliente. 6. Textos, configuración y documentación. 7. Build, pruebas y
arranque de servidor. Una rama (`claude/jefe-osario`) y un commit por fase.

## 10. Riesgos

- `WorldEventManager` está en uso por la sesión de narrativa: el cambio será pequeño y aislado para evitar conflictos.
- El equilibrio es una estimación a partir del Coloso y el Autómata; hace falta tu prueba en partida.
- Registrar daño por jugador añade estado al jefe (NBT). Se acota y se descarta al morir.
- El jefe aparece solo si hay Niebla: en servidores sin aldeano del gremio activo la Niebla no sale y el jefe tampoco.
