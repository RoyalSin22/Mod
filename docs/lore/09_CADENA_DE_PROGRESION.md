# 09 · Cadena de progresión: Niebla → templo → Behelit → Eclipse

Diseño **ESTABLECIDO por el autor**. Casi todo es **mecánica nueva o rediseñada**: este archivo es
documentación, no implementa nada. «Hoy» describe lo que hay en el código (verificado el 2026-11-13).

## La cadena

| # | Paso (diseño del autor) | Hoy en el código | Estado |
|---|---|---|---|
| 1 | **Un jugador habla con el aldeano del gremio** y este avisa de que los muertos se levantan. Eso inicia la Niebla de almas para **todo el mundo**. Se repite cada **10 días**, pero solo si se vuelve a hablar con él | Antes de T1: la Niebla salía por calendario rotativo junto al Eclipse y la Lluvia (`WorldEventManager`). El aldeano del gremio existe (`world/GuildVillagers`) pero no inicia eventos | **IMPLEMENTADO en T1** (Mayús + clic, provisional); pendiente de prueba en partida |
| 2 | **La Niebla puede iniciarse en cualquier momento y dura un día de Minecraft** (24 000 ticks desde que se inicia). Aparecen zombis, esqueletos y sus variantes, el Errante Velado (aparición alta, solo durante la Niebla) y el Osario Quebrado (aparición más baja) | Antes de T1: duraba 12 000 ticks desde las 18:00 (`DURATION`, `FOG_START`). Existen las reglas de criaturas de la Niebla (`event/FogMobRules`, `FogErranteRules`, `SoulFogRules`) | **Duración e inicio implementados en T1**; proporciones de aparición por ajustar |
| 3 | **Cada criatura suelta un objeto distinto**, con probabilidad «normal» | Errante Velado: Jirón de Velo Espectral al 85 %. Osario Quebrado: **Núcleo del Osario** al 85 % (T2), solo si nació en la Niebla | **IMPLEMENTADO en T2** (nombre y cifras elegidos por el redactor); pendiente de prueba en partida |
| 4 | **Se crafteán los dos objetos con otros materiales** y se obtiene un objeto especial de acceso, que se **consume** al usarse en la puerta del Custodio. El autor cita como ejemplo fragmentos de alma y diamantes | **Ancla del Velo** (T2): 3 Jirón + 2 Núcleo + 3 Fragmentos de alma + 1 diamante, sin forma, en la Mesa del gremio. Aún no hace nada | **Objeto y receta IMPLEMENTADOS en T2**; la puerta que lo consume es T3 |
| 5 | **Estructura de templo nueva.** El **Autómata Antiguo aparece dentro**, como subjefe | El templo actual (v64, `world/TempleFeature`, `temple/`) tiene sello con puzle y guardián; el Autómata se invoca con un sello de jefe | PENDIENTE (rediseño) |
| 6 | Se resuelve el **puzle**, se avanza hasta la sala previa y el objeto especial abre la **puerta de la sala del Custodio** (solo ahí; no se usa para entrar al templo) | Puzle de palancas y placas por anillo de Amenaza (`TempleRules`) | PENDIENTE |
| 7 | **El Custodio suelta 4 llaves** (cobre, hierro, oro y diamante) en **una sola tirada**. Cada llave abre una cámara de recompensas | Templo nuevo (T3/T4): suelta las cuatro llaves a la vez, cada una ligada a su templo, y solo abre la cámara de su metal. Además suelta la llave del Relicario (Vaso de vida), que se mantiene | **IMPLEMENTADO en T4**; pendiente de prueba en partida |
| 8 | **Recompensas:** objetos vanilla (manzanas doradas encantadas, bloques de minerales…), objetos del mod y similares | Cuatro cofres con tablas `custodian_<metal>` que crecen en valor (ver `docs/BALANCE.md`); la de diamante lleva las manzanas encantadas y la netherita | **IMPLEMENTADO en T4** (contenido elegido por el redactor, provisional) |
| 9 | **La cámara de diamante guarda el Behelit** (nombre de trabajo: *Resonador del Umbral*). Se puede tener **más de uno**, pero solo se usa **uno cada 3 días por mundo, contados desde el fin del Eclipse**. Se consigue en los templos del Custodio | No existe | PENDIENTE |
| 10 | **Se sacrifica en la Hoguera** (se consume) y empieza el **Eclipse** | El Eclipse es de calendario (`eclipseIntervalDays = 10`) | PENDIENTE |
| 11 | **Lluvia de brasas:** se activa cuando un jugador **enciende un portal al Nether en el Overworld**, con una **espera de 5 días** entre activaciones | Antes de T1: evento de calendario rotativo | **IMPLEMENTADO en T1**; pendiente de prueba en partida |

Esperas resultantes: **Niebla, 10 días** (y solo con el aldeano), que puede empezar en cualquier momento al hablar con él y dura un día de Minecraft (24 000 ticks); **Eclipse, 3 días
por mundo desde que termina el anterior**; **Lluvia de brasas, 5 días** entre activaciones. Mientras la
Niebla está en espera, el aldeano dice algo como «los muertos se han ido, pero volverán» (texto exacto por definir).

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

El autor indicó que los sellos serán **innecesarios** y que se **retirarán con migración**, pero que **no se
eliminen ahora**. Hay **dos cosas distintas** con ese nombre:

| Qué | Ids | Uso actual |
|---|---|---|
| Sellos de invocación de jefes | `ancient_automaton_seal`, `dismantled_guardian_seal`, `faceless_colossus_seal` (`boss/SummonSealItem`) | Invocar a los jefes; con receta, textos y modelos |
| Bloque Sello del templo | `temple_seal` (`temple/Temples`) | Puzle y guardián del templo actual |

**No se eliminó nada.** Retirar un objeto registrado afecta a mundos y jugadores existentes (ítems perdidos,
recetas, bestiario, logros, textos en 11 idiomas, modelos y pruebas), y `AGENTS.md` pide no cambiar ids de
registro existentes; por eso la retirada será una tarea de código con **migración** (E8). **Alcance por
confirmar:** cuáles de los cuatro se retiran (A-3).

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
| E10 Retirar el calendario rotativo (campos `worldEventWeight*`, `eclipseFirstDay`, `eclipseIntervalDays`) con migración de configuración | `config/AptitudesConfig`, `ConfigMigration`, `docs/CONFIGURATION.md`, `event/` |

Cada una necesita su diseño, valores por defecto en configuración, filas en `docs/CONFIGURATION.md`, pruebas y
autorización del autor.

## Riesgos de diseño a vigilar (PROPUESTA)

- **Efecto global:** cualquier jugador con el aldeano puede iniciar un evento de un día completo para todos.
- **Multijugador:** los Behelit y las llaves son por jugador o por grupo; hoy el reparto de botín no está definido.
- **Lluvia por portal:** abrir y cerrar portales podría encadenar eventos; conviene un tope.
- **Eclipse cada 3 días:** el valor actual del código es 10 (`AptitudesConfig.eclipseIntervalDays`).

## Aclaraciones

| Id | Pregunta | Respuesta del autor | Estado |
|---|---|---|---|
| A-1 | Uso del objeto especial | Solo en la puerta del Custodio, y se consume | Resuelta |
| A-2 | Espera del Behelit | 3 días, por mundo, desde el fin del Eclipse | Resuelta |
| A-3 | Sellos | Los 3 sellos de invocación, retirados con migración y sin eliminarlos ahora. El bloque «Sello del templo» queda fuera de esta decisión | Resuelta (la retirada es la tarea E8) |
| A-4 | Lluvia de brasas | Espera de 5 días | Resuelta |
| A-5 | Duración y momento de inicio de la Niebla | Puede comenzar en cualquier momento y dura lo que un día de Minecraft | Resuelta |
| A-6 | ¿Desaparece el calendario rotativo? | Sí, por completo | Resuelta (tarea E10) |
| A-7 | Texto del aldeano con la Niebla en espera | «Los muertos se han ido, pero volverán» | Resuelta (texto exacto por definir) |

Con duración fija (un día de Minecraft) desaparece el riesgo de una Niebla casi inexistente por iniciarla cerca
del amanecer. Queda una pregunta menor (A-8): los 10 días de espera, ¿se cuentan desde que **se inicia** la Niebla o
desde que **termina**? Al durar un día, la diferencia es de un día.
| A-8 | Los 10 días de la Niebla, ¿desde que se inicia o desde que termina? | Sin respuesta | Abierta |
