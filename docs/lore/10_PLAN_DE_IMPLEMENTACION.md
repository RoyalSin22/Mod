# 10 · Plan de implementación de la cadena (por tandas)

**Estado (revisado el 2026-11-24 contra el código y `git log`): T1–T4, T6, T7a y T7b están implementadas; T5 está implementada con una carencia (la espera de 3 días del Behelit no existe en el código, ver «Estado real»). Ninguna tanda está validada en partida.** Convierte el diseño de `09_CADENA_DE_PROGRESION.md` en
trabajo de código, arte y Grimorio. Los puntos de enganche se leyeron en el código el 2026-11-14; lo marcado
«a verificar» se comprueba con Context7 o el jar antes de escribir una línea (`AGENTS.md` §9).

## 1. ¿Todo de una vez o por tandas?

**Por tandas.** Una sola entrega gigante obligaría a tocar a la vez eventos, generación de mundo, botín, la
Hoguera, la configuración y el Grimorio. Eso hace imposible aislar un fallo y deja el juego sin poder probarse
hasta el final. Además `AGENTS.md` pide **una versión por tarea y por rama**.

Lo que sí se puede hacer es **ordenar las tandas para entregar en tres versiones jugables**:

| Entrega | Tandas | Qué puede probar el autor |
|---|---|---|
| **R1** | T1 + T2 | La Niebla por aldeano, la Lluvia por portal y los materiales |
| **R2** | T3 + T4 + T5 | El templo nuevo, las 4 llaves, el Behelit y el Eclipse por sacrificio (la cadena cerrada) |
| **R3** | T6 + T7 | Retirada de los sellos y el Grimorio rediseñado |

El **arte** (pista A) corre en paralelo desde el principio y no bloquea el código.

## 2. Tandas

Tamaños relativos (S < M < L < XL); no son horas.

| Tanda | Contenido | Tareas de `09` | Tamaño | Depende de |
|---|---|---|---|---|
| **T0** | Cerrar decisiones abiertas y fijar nombres, recetas y cifras (§4) | — | S | — |
| **T1** (implementada, pendiente de prueba en partida) | Eventos por activación: aldeano del gremio inicia la Niebla (24 000 ticks, 10 días, global); Lluvia de brasas al encender un portal (5 días); el calendario deja de elegir Niebla y Lluvia | E1, E2, E7 | L | T0 |
| **T2** (implementada, pendiente de prueba en partida) | Botín del Osario, objeto de acceso y su receta, objetos nuevos y sus texturas | E3 | M | T0 |
| **T3** (implementada, pendiente de prueba en partida) | Templo nuevo: estructura, Autómata interno, puzle, puerta del Custodio que consume el objeto | E4 | XL | T2 |
| **T4** (implementada, pendiente de prueba en partida) | Custodio con 4 llaves (cobre, hierro, oro, diamante) en una tirada y 4 cámaras de recompensa | E5 | L | T3 |
| **T5** (implementada **con carencias**: sin espera de 3 días ni migración de configuración; ver «Estado real») | Behelit, sacrificio en la Hoguera, Eclipse con espera de 3 días por mundo, fin del calendario rotativo | E6, E10 | L | T4 (T1 para la parte de calendario) |
| **T6** (implementada **sin migración**, contra D-25/D-26; ver «Estado real») | Retirada de los 3 sellos de invocación, con migración | E8 | M | T3 |
| **T7a** (implementada) | Grimorio, infraestructura: estado de lectura por jugador, desbloqueos, entrada de bestiario del Custodio | E9 | L | T1, T4, T5 |
| **T7b** (implementada como V71) | Grimorio, interfaz: tarjetas de bestiario, páginas de lore | E9 | XL | A, T7a |
| **A** | Pista de arte, en paralelo | — | L | T0 |

### Estado real (2026-11-24)

Comprobado leyendo el código y `git log`; la columna «Prueba en partida» es **pendiente** en todas salvo que se
indique (el autor confirmó las pruebas de la numeración antigua T4–T6 de `PLAN_CODEX`, que no son estas tandas).

| Tanda | Estado | Evidencia | Pendiente |
|---|---|---|---|
| T1 | Implementada | `01ed9ff`; `event/EventTriggerRules`, `WorldEventManager.talkToGuildmaster`, `watchPortalLighting` | Partida; D-23 (¿10 días desde el inicio o el fin?) y D-30 siguen provisionales |
| T2 | Implementada | `61e29d1`; `NUCLEO_OSARIO`, `ANCLA_DEL_VELO` | Partida |
| T3 | Implementada | `989f967`…`d0a27c2`; `temple/CustodianTempleLayout`, `world/CustodianTempleFeature`; ampliada por V70 (templo hondo) y V77 (`locate`/`place`) | Partida y ajuste de balance |
| T4 | Implementada | `958d1e4`, `353d310`; frecuencia 1/120 (`5c0f3a5`, D-35) | Partida |
| **T5** | Implementada (espera de 3 días y migración añadidas el 2026-11-24; sin validar) | Behelit = `resonador_del_umbral` (`item/BeheritItem`, `ModItems.RESONADOR_DEL_UMBRAL`), en `chests/custodian_diamond.json`; el sacrificio ocurre en la Hoguera (`BonfireBlock` deja pasar el ítem) y llama a `EclipseManager.startByBeherit` (`EclipseManager.java:614`); desbloqueo de lore en `EclipseManager:235` (`09f6a58`). Calendario: `eclipseIntervalDays`, `eclipseFirstDay` y `worldEventWeight*` marcados DEPRECATED y los pesos por defecto bajados de 1 a 0 (`240484f`) | Ver abajo |
| T6 | Implementada, desviada | `fe4f766` borra registro, recetas, modelos, texturas y claves de los tres sellos de los 11 idiomas | Ver abajo |
| T7a | Implementada | `75906c6`; `guide/LoreRules`, `LoreManager`, attachment `aptitudes:lore` | Partida |
| T7b | Implementada (V71) | `b512c10`, `325b8f7`; `guide/LoreCatalog` (18 entradas), pestaña «Crónicas» en `GuideScreen` | Partida; arte propio de la pestaña |

**T5, qué falta o es dudoso**

1. ~~Resuelto 2026-11-24: `EclipseSummoningRules`, `EclipseState.lastEndTick`, `eclipseSummonCooldownDays`.~~ **No existía la espera de 3 días por mundo** (D-20, D-25). `EclipseManager.canStartEclipse` solo comprueba
   `!state.active && !WorldEventManager.active(...)`, aunque su Javadoc y el mensaje de `09f6a58` hablan de «cooldown».
   `EclipseState` no guarda el fin del último Eclipse. Hoy se puede encadenar un Behelit tras otro.
2. ~~Resuelto: revisión 84 de `ConfigMigration` y calendario apagado.~~ **Sin migración de configuración.** El plan pedía migrar en `config/ConfigMigration`; no se hizo. Un `config`
   antiguo con `worldEventWeightEclipse > 0` sigue programando Eclipses por calendario (`WorldEventManager.calendarWeights`).
   Además `240484f` cambió el valor por defecto de los pesos de 1 a 0 sin que conste la decisión del autor (regla de balance).
3. ~~Resuelto: con un evento activo el Behelit no se consume y avisa.~~ **Decisión nº 6 de §4 sin registrar:** qué ocurre si hay un Eclipse activo (hoy, el Behelit no se consume y no avisa).
4. `BeheritItem` usa `useOnBlock` sobre la Hoguera; el plan proponía el patrón de `onUseWithItem` de la ofrenda. Funcional, pero distinto.
5. Sin pruebas JUnit de la regla de espera (no hay una clase pura que la contenga).

**T6, qué es dudoso**

- D-25 y D-26 dicen «los sellos se retirarán **con migración**, sin eliminarlos». El commit `fe4f766` los eliminó **sin
  alias de registro ni conversión**: en un mundo guardado con alguno de los tres objetos, Minecraft descarta el
  objeto desconocido al cargar. `docs/PLAN_AVANCE.md` §1 dice que «no hay mundos de jugadores que preservar», pero
  D-25/D-26 son posteriores y piden migración: hace falta que el autor confirme cuál manda.
- Quedan menciones en `docs/PLAN_AVANCE.md`, `docs/lore/09_CADENA_DE_PROGRESION.md`, `docs/PROMPT_ARTE_ARMAS.md`,
  `tools/textures/gen_items.py` (que aún regenera los PNG; `AGENTS.md` §2 manda borrar tres PNG tras cada ejecución) y
  la entrada «v65 — Ruinas de batalla» del CHANGELOG («al invocar al Coloso con el sello»).
- `ModItems.java:183-185` conserva las tres líneas comentadas con un texto que dice que «los ítems se mantienen
  para compatibilidad»; es falso desde `fe4f766`.

### Orden y paralelismo

```
T0 ─┬─ T1 ───────────────┐
    ├─ T2 ─ T3 ─ T4 ─ T5 ┼─ T7a ─ T7b
    │       └─ T6        │
    └─ A (arte) ─────────┘
```

T1 y T2 se pueden hacer a la vez (tocan archivos distintos). T3 en adelante es una cadena.

### Estado intermedio coherente

- **Tras T1:** el calendario solo programa el **Eclipse** (como hoy). Niebla y Lluvia ya no salen por calendario.
- **Tras T3:** los templos antiguos de mundos existentes siguen funcionando con su lógica actual; los nuevos se
  generan con la estructura nueva. Decisión del autor: ¿se mantiene o se retira el templo viejo? (§4).
- **Tras T5:** el Eclipse deja de ser de calendario y pasa a depender del Behelit.

### Estado real de T5, T6 y T7 (2026-11-24)

| Tanda | Hecho (evidencia) | Falta |
|---|---|---|
| T5 | Objeto Behelit (`item/BeheritItem`, `ModItems.RESONADOR_DEL_UMBRAL`) y sacrificio en la Hoguera, commit `09f6a58` («T5a»); `EclipseManager.start` anota el lore (V71). **C3 (2026-11-24):** espera de 3 días **por mundo desde el fin del Eclipse** (`EclipseState.lastEndTick`, `event/EclipseSummoningRules`, opción `eclipseSummonCooldownDays`), compartida con el Astrolabio del Ocaso | **E10 sin hacer:** el calendario sigue programando Eclipses (`WorldEventManager.calendarWeights`, `kindForDay`); `eclipseFirstDay`, `eclipseIntervalDays` y `worldEventWeight*` siguen en `AptitudesConfig` solo marcados como obsoletos, sin migración en `ConfigMigration`. Sin modelo ni textura del Behelit (`models/item` y `textures/item` no tienen `resonador_del_umbral`). Sin pruebas en partida |
| T6 | Los tres `register(...)` de los sellos están comentados en `ModItems` (líneas 183–185) | El comentario dice «se mantienen por compatibilidad», pero **ya no están registrados** y no hay alias ni migración (`grep` de `ancient_automaton_seal` solo da el comentario): los objetos de mundos viejos se perderían. `boss/SummonSealItem.java` sigue en el repositorio. Política de migración sin decidir (§4, decisión 7) |
| T7a/T7b | `guide/LoreRules`, `LoreCatalog` (18 entradas), `LoreManager`, `LoreState`; pestaña «Crónicas» y desbloqueos cableados (commit `325b8f7`, V71) | Validar en partida; arte propio de la pestaña |

## 3. Contenido de cada tanda

### T1 · Eventos por activación

- **Aldeano:** engancharse a la profesión propia `GUILDMASTER` (`world/GuildVillagers`) con `UseEntityCallback`
  (a verificar), validando todo en el servidor. Si la Niebla está en espera, el aldeano responde «los muertos se
  han ido, pero volverán» (texto exacto por definir).
- **Reglas puras** (`event/`, con JUnit): espera de 10 días, duración de 24 000 ticks, espera de 5 días de la Lluvia.
  Nombres nuevos, sin repetir los de las pruebas existentes.
- **Estado de mundo:** campos persistentes nuevos en `WorldEventState` (inicio de la última Niebla y de la
  última Lluvia).
- **Portal:** evitar mixin. Propuesta: `UseBlockCallback` con yesca y pedernal o descarga de fuego sobre
  obsidiana, y comprobar al tick siguiente si se formó el portal. Solo cuenta «un jugador lo enciende». A
  verificar; si no basta, un mixin con `defaultRequire = 1` exigiría comprobar el nombre exacto en Yarn 1.21.1.
- **Configuración** (valor por defecto seguro, límite en `validated(...)`, fila en `docs/CONFIGURATION.md`):
  `fogCooldownDays = 10`, `fogDurationTicks = 24000`, `emberRainCooldownDays = 5`.
- **Pruebas en partida:** hablar con el aldeano, esperar, volver a hablar en espera; encender un portal en
  Overworld; dos jugadores en el mismo mundo.

### T2 · Materiales

- Botín del Osario (objeto distinto del Jirón; nombre pendiente) y su probabilidad «baja».
- Objeto de acceso al Custodio: receta en la **Mesa del gremio** (tipo `aptitudes:guild_crafting`; la cuadrícula
  de esa mesa es exclusiva para la fabricación del mod). Ejemplo del autor: fragmentos de alma y diamantes.
- Texturas con `tools/textures/gen_items.py`; claves de idioma en `en_us` y `es_es` (copias a las variantes);
  entradas en el grimorio de recetas (se leen del juego, no se copian).

### T3 · Templo nuevo

- Reutilizar `temple/TempleRules` (puzle). `TempleRecurrenceRules` (regeneración) es solo una clase de reglas y **no está conectada al juego** (D-33).
- Nueva estructura en `world/TempleFeature` con `StructureBuilder`; Autómata Antiguo como subjefe fijo dentro.
- Puerta de la sala del Custodio: se abre al resolver el puzle y **consumir** el objeto de T2.
- Valores por defecto de dificultad ligados al anillo de Amenaza (ya existe).
- **Riesgo alto:** generación de mundo. Se prueba con `/place` o `/locate` y mundos nuevos y existentes.

### T4 · Llaves y cámaras

- Cuatro objetos llave, una **sola tirada** del Custodio. Cada una abre una cámara; la de diamante contiene el
  Behelit (T5).
- Recompensas (objetos vanilla, manzanas doradas encantadas, bloques de minerales y objetos del mod): tabla de
  botín por cámara, por definir en T0.
- En multijugador, el Relicario ya guarda qué jugadores reclamaron (`claimed`); T4 debe repetir ese patrón en cada cámara.
- Migración de templos con la `temple_key` y el Relicario actuales: decisión pendiente (§4).

### T5 · Behelit y Eclipse

- Objeto Behelit (nombre de trabajo «Resonador del Umbral»). Se consume al sacrificarlo.
- Hoguera: ya acepta una ofrenda (`BonfireBlock.onUseWithItem` con `REBIRTH_OFFERING`); mismo patrón.
- `EclipseManager`: arranque por sacrificio y **espera de 3 días por mundo desde el fin del Eclipse**
  (`EclipseState` persistente). Se retiran `eclipseFirstDay`, `eclipseIntervalDays` y los
  `worldEventWeight*` con migración en `config/ConfigMigration`, sin perder la configuración de los demás campos
  del Eclipse (`eclipseEnabled`, multiplicadores, oleadas…).
- Se mantienen el Asalto (v47), la persecución (v52) y la barra de jefes.

### T6 · Retirada de los sellos

- Los tres sellos de invocación (`ancient_automaton_seal`, `dismantled_guardian_seal`, `faceless_colossus_seal`):
  retirar **con migración**, sin eliminarlos antes de T3. Política por decidir: alias de registro (a verificar en
  1.21.1), conversión a otro objeto o ítem inerte. Afecta a recetas, logros, bestiario, 11 idiomas, modelos y
  pruebas. No se toca el bloque `temple_seal`.

### T7 · Grimorio

- **T7a:** attachment por jugador (`aptitudes:lore`, `copyOnDeath`, mismo patrón que `BESTIARY`), clase pura
  `LoreRules` con JUnit, payload S2C, ganchos de desbloqueo (aldeano, Custodio, puerta, cámara de diamante,
  Eclipse), y entrada de bestiario del Custodio (cambio en `BestiaryRules`; el tope es de 256 entradas).
- Reutilizar el bestiario existente: el texto de criatura se muestra si `BESTIARY[id] > 0`; no hace falta estado
  nuevo para eso.
- **T7b:** interfaz. Debe caber en 426×240 o desplazarse (`AGENTS.md`). Ver pista de arte.
- Actualizar `tools/lore/lore_entries.json` (verificación de cada desbloqueo) y las pruebas de Python.

## 4. Decisiones del autor antes de empezar (T0)

| # | Qué falta | Bloquea |
|---|---|---|
| 1 | Nombre y receta del objeto de acceso; objeto y probabilidad del Osario | T2 |
| 2 | Texto exacto del aldeano, y si los 10 días de la Niebla se cuentan desde el inicio o el fin (A-8) | T1 |
| 3 | Proporciones de aparición de la Niebla (cuánto Errante, cuánto Osario) | T1 |
| 4 | Contenido de las 4 cámaras y de la tirada del Custodio | T4 |
| 5 | Qué pasa con los templos viejos de mundos existentes (mantener o migrar) y con la `temple_key` y el Relicario | T3, T4 |
| 6 | Nombre definitivo del Behelit y qué ocurre si hay un Eclipse activo | T5 |
| 7 | Política de migración de los sellos | T6 |
| 8 | Alcance del rediseño del Grimorio y destino de las imágenes de referencia | T7 |

## 5. Pista de arte (A) y referencia del bestiario

Imagen del autor: 7 tarjetas de bestiario en pixel art, ~365×395 px cada una (proporción cercana a 1:1).

| Elemento de la referencia | Detalle |
|---|---|
| Marco | Dorado ornamentado con tachuelas en las esquinas, cinta roja en la esquina superior izquierda y rombo dorado en el centro superior |
| Fondo | Pergamino beige con manchas oscuras en los bordes |
| Ilustraciones | Osario Quebrado, Errante Velado, Custodio de las Cuatro Llaves (arco con halo, cerradura y cuatro llaves: oro, hierro, cobre y diamante), Escorificador de Hierro, Guardián Desmontado, Autómata Antiguo y un caballero oscuro coronado con espada de lava |
| Sin identificar | El caballero coronado: ¿es el Heraldo del Eclipse u otro jefe? Falta la tarjeta del Coloso Sin Rostro |

Plan de arte (PROPUESTA, mismo método que los `PROMPT_ARTE_*`):

1. Redactar `docs/PROMPT_ARTE_BESTIARIO.md` con las reglas técnicas del kit existente (píxel lógico de 2×2, sin
   antialiasing, paleta limitada) para que la tarjeta coincida con el resto de pantallas.
2. Piezas: marco 9-slice, cinta, rombo, fondo de pergamino, y **una ilustración por criatura** con tamaño fijo.
3. Fuente en `tools/textures/source/ui/bestiary/` y generador con `--check` (no se editan los PNG de salida).
4. La imagen de referencia es de generación asistida y no está en la cuadrícula de píxel real: se usa como
   **referencia**, no como asset final. No se copia al repositorio sin autorización del autor.

## 6. Reglas de proceso (`AGENTS.md`)

- Una versión por tarea y por rama; la versión sale del número de commits (no se edita a mano).
- Cada tanda actualiza `CHANGELOG.md`, `docs/ROADMAP_APTITUDES.md`, `docs/CONFIGURATION.md` y, si cambia el
  balance, `docs/BALANCE.md`; añade pruebas de lógica pura; no cambia ids de registro existentes.
- **Autoridad del servidor:** toda petición del cliente se valida; los desbloqueos se calculan en el servidor.
- Sin mixins salvo necesidad demostrada; las APIs de Fabric/Yarn/GeckoLib se consultan antes de usarlas.
- `./gradlew build` real y su resultado antes de dar una tanda por buena; si no se puede ejecutar, se dice.
- Revisar `docs/PLAN_CODEX_2.md` y `PLAN_CLAUDE.md` para no tocar a la vez los mismos archivos (p. ej.
  `GuideScreen`, tarea K3 de la v61).
- **Rama:** `AGENTS.md` pide una rama por tarea; la sesión actual solo permite subir a
  `claude/aptitudes-lore-audit-htenjq`. Hace falta la decisión del autor (§7).

## 7. Qué se necesita para empezar

1. Autorización de **T0 y la tanda inicial** (propuesta: T1 y T2 en paralelo).
2. Política de ramas: una rama por tanda (con permiso para crearlas y subirlas) o commits por tanda en la
   rama actual.
3. Respuestas a la tabla del §4 que correspondan a las tandas que se vayan a abrir.
