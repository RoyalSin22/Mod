# 09 · Cadena de progresión: Niebla → templo → Behelit → Eclipse

Diseño **ESTABLECIDO por el autor**. Casi todo es **mecánica nueva o rediseñada**: este archivo es
documentación, no implementa nada. «Hoy» describe lo que hay en el código (verificado el 2026-11-13).

## La cadena

| # | Paso (diseño del autor) | Hoy en el código | Estado |
|---|---|---|---|
| 1 | **Un jugador habla con el aldeano del gremio** y este avisa de que los muertos se levantan. Eso inicia la Niebla de almas para **todo el mundo**. Se repite cada **10 días**, pero solo si se vuelve a hablar con él | La Niebla sale por calendario rotativo junto al Eclipse y la Lluvia (`WorldEventManager`). El aldeano del gremio existe (`world/GuildVillagers`) pero no inicia eventos | PENDIENTE DE IMPLEMENTAR |
| 2 | **La Niebla dura 1 día entero.** Aparecen zombis, esqueletos y sus variantes, el Errante Velado (aparición alta, solo durante la Niebla) y el Osario Quebrado (aparición más baja) | Dura 12 000 ticks desde las 18:00 (`DURATION`, `FOG_START`). Existen las reglas de criaturas de la Niebla (`event/FogMobRules`, `FogErranteRules`, `SoulFogRules`) | Parcial: duración y proporciones por ajustar |
| 3 | **Cada criatura suelta un objeto distinto**, con probabilidad «normal» | Errante Velado: Jirón de Velo Espectral al 85 % (loot table). Osario Quebrado: sin objeto propio | Errante: VERIFICADO. Osario: PENDIENTE (D-05) |
| 4 | **Se crafteán los dos objetos con otros materiales** y se obtiene un objeto especial de acceso. El autor cita como ejemplo fragmentos de alma y diamantes | `soul_shard` existe; no hay receta ni objeto especial | PENDIENTE. Receta y nombre sin definir |
| 5 | **Estructura de templo nueva.** El **Autómata Antiguo aparece dentro**, como subjefe | El templo actual (v64, `world/TempleFeature`, `temple/`) tiene sello con puzle y guardián; el Autómata se invoca con un sello de jefe | PENDIENTE (rediseño) |
| 6 | Se resuelve el **puzle**, se avanza hasta la sala previa y el objeto especial abre la **puerta de la sala del Custodio** | Puzle de palancas y placas por anillo de Amenaza (`TempleRules`) | PENDIENTE. Punto exacto de uso por confirmar (A-1) |
| 7 | **El Custodio suelta 4 llaves** (cobre, hierro, oro y diamante) en **una sola tirada**. Cada llave abre una cámara de recompensas | Suelta una única `temple_key` que abre el Relicario de ese templo (`TempleKeyItem`, `ReliquaryBlockEntity`) | PENDIENTE |
| 8 | **Recompensas:** objetos vanilla (manzanas doradas encantadas, bloques de minerales…), objetos del mod y similares | Relicario existente; contenido de las 4 cámaras sin definir | PENDIENTE |
| 9 | **La cámara de diamante guarda el Behelit** (nombre de trabajo: *Resonador del Umbral*). Se puede tener **más de uno**, pero solo se usa **uno cada 3 días**. Se consigue en los templos del Custodio | No existe | PENDIENTE |
| 10 | **Se sacrifica en la Hoguera** (se consume) y empieza el **Eclipse** | El Eclipse es de calendario (`eclipseIntervalDays = 10`) | PENDIENTE |
| 11 | **Lluvia de brasas:** se activa cuando un jugador **enciende un portal al Nether en el Overworld**, cada vez que se abre uno | Evento de calendario rotativo | PENDIENTE |

Esperas resultantes: **Niebla, 10 días** (y solo con el aldeano); **Eclipse, 3 días** (un Behelit cada 3 días);
**Lluvia de brasas, sin espera propia** (cada portal nuevo).

## Cómo cambia lo ya documentado

| Antes | Ahora |
|---|---|
| D-18: objetivos para invocar el Eclipse sin definir | Los objetivos son esta cadena |
| D-02: material del sacrificio sin definir | El Behelit, que se consume |
| D-12: ¿Niebla con aldeano? | Sí: aldeano del gremio, efecto global |
| «Eclipse cada 10 días» (docs 02 y 06) | **3 días** entre Eclipses |
| Niebla «cada 3 días» (borrador del contexto) | **10 días**, solo con el aldeano |
| Cuatro llaves y cuatro cámaras: NO VERIFICADO (C-13) | Diseño aprobado, aún sin implementar |
| «La reliquia» de LORE-013 | Corresponde al Behelit; el texto del autor se mantiene |

## Efecto en el lore

- **LORE-010 y LORE-012** (cuatro cámaras, cuatro llaves, Custodio con las llaves): ya concuerdan con el diseño.
- **LORE-013:** «la llama consumió la reliquia» concuerda con el sacrificio del Behelit.
- **Desbloqueos plausibles (PROPUESTA, no implementados):** EVENT-001 al hablar con el aldeano o vivir la Niebla;
  BESTIARY-004 y LORE-012 al derrotar al Custodio; LORE-009 al abrir la puerta de su sala; LORE-010 al abrir la
  cámara de diamante; LORE-013 y EVENT-003 al sacrificar el Behelit; LORE-014 al terminar el Eclipse.

## Qué NO se toca en esta tarea

Aldeano, eventos, calendario, entidades, loot, recetas, templo, llaves, Hoguera, estructuras, interfaz.

### Los «sellos»

El autor pidió eliminar los sellos si es posible. Hay **dos cosas distintas** con ese nombre:

| Qué | Ids | Uso actual |
|---|---|---|
| Sellos de invocación de jefes | `ancient_automaton_seal`, `dismantled_guardian_seal`, `faceless_colossus_seal` (`boss/SummonSealItem`) | Invocar a los jefes; con receta, textos y modelos |
| Bloque Sello del templo | `temple_seal` (`temple/Temples`) | Puzle y guardián del templo actual |

**No se eliminó nada.** Retirar un objeto registrado afecta a mundos y jugadores existentes (ítems perdidos,
recetas, bestiario, logros, textos en 11 idiomas, modelos y pruebas), y `AGENTS.md` pide no cambiar ids de
registro existentes. Queda como tarea de código aparte, a decidir entre ocultarlos o retirarlos con migración
(A-3).

## Tareas de implementación que se derivan (ninguna autorizada)

| Tarea | Áreas |
|---|---|
| E1 Aldeano del gremio inicia la Niebla (cooldown de 10 días, global) | `world/GuildVillagers`, `event/WorldEventManager`, configuración |
| E2 Duración y apariciones de la Niebla | `event/` reglas puras con pruebas |
| E3 Botín del Osario, receta y objeto de acceso | loot, `data/aptitudes/recipe`, `item/` |
| E4 Templo nuevo con Autómata interno | `world/TempleFeature`, `temple/`, `boss/` |
| E5 Custodio con 4 llaves y 4 cámaras | `temple/`, loot |
| E6 Behelit, sacrificio en la Hoguera y Eclipse cada 3 días | `event/EclipseManager`, Hoguera, configuración |
| E7 Lluvia de brasas al encender un portal | `event/WorldEventManager` |
| E8 Retirada de los sellos | `item/`, `boss/`, recursos, idiomas |
| E9 Grimorio: estado de lectura, entradas e interfaz | `guide/`, `client/screen/GuideScreen` |

Cada una necesita su diseño, valores por defecto en configuración, filas en `docs/CONFIGURATION.md`, pruebas y
autorización del autor.

## Riesgos de diseño a vigilar (PROPUESTA)

- **Efecto global:** cualquier jugador con el aldeano puede iniciar un evento de un día completo para todos.
- **Multijugador:** los Behelit y las llaves son por jugador o por grupo; hoy el reparto de botín no está definido.
- **Lluvia por portal:** abrir y cerrar portales podría encadenar eventos; conviene un tope.
- **Eclipse cada 3 días:** el valor actual del código es 10 (`AptitudesConfig.eclipseIntervalDays`).

## Pendientes de aclarar

| Id | Pregunta |
|---|---|
| A-1 | ¿El objeto especial se usa en la puerta de la sala del Custodio, o también para entrar al templo? ¿Se consume? |
| A-2 | Los 3 días del Behelit: ¿por mundo o por jugador? ¿Se cuentan desde el inicio o desde el fin del Eclipse? |
| A-3 | Sellos: ¿se retiran los tres de invocación, solo el del Autómata, o el bloque del templo? ¿Ocultar o retirar con migración? |
| A-4 | Lluvia de brasas: ¿hay espera o tope por portal o por jugador? |
| A-5 | «1 día entero» de Niebla: ¿24 000 ticks desde que se inicia, o hasta el amanecer? |
| A-6 | ¿El calendario rotativo actual desaparece por completo? |
| A-7 | ¿Qué dice el aldeano mientras la Niebla está en espera? |
