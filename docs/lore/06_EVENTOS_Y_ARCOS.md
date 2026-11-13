# 06 · Eventos y arcos narrativos

Este archivo separa **lo que el juego ya hace** de **lo que el lore propone**. Ningún evento se modifica.

## Resumen de estado real (VERIFICADO EN CÓDIGO)

| Evento | Código | Cómo se activa hoy |
|---|---|---|
| Niebla de almas | `event/WorldEventManager`, `FogErranteRules`, `SoulFogRules` | Calendario rotativo por fecha; comienza a las 18:00 (tick 12000) |
| Lluvia de brasas | `event/WorldEventManager`, `EmberMobRules` | Calendario rotativo; comienza a las 6:00 (tick 0) |
| Eclipse | `event/EclipseManager`, `EclipseRules`, `EclipseAssaultRules`, `EclipsePursuit` | Calendario: `eclipseIntervalDays = 10`, `eclipseFirstDay = 10` |
| Selección | `WorldEventManager` | Un evento al azar por fecha entre los tres, sin repetir el anterior; pesos configurables |

No he encontrado en el código: activación de la Niebla mediante aldeano, sacrificio en la Hoguera como gatillo
del Eclipse, activación de la Lluvia por portal. Se registran como **NO VERIFICADO / PENDIENTE** (ver C-01 a C-04).

## Progresión del jugador (diseño narrativo)

| Paso | Contenido | Estado |
|---|---|---|
| Inicio | El jugador aparece con el Grimorio; bestiario vacío | Bestiario por jugador: VERIFICADO (`guide/BestiaryRules`); «aparece con Grimorio»: NO VERIFICADO |
| Primeros objetivos | Obtener Hoguera y Mesa del Gremio | Objetos VERIFICADOS (`BONFIRE_ITEM`, `GUILD_DESK_ITEM`); guiado por el tutorial (v44) |
| Niebla de almas | Primer evento; un aldeano advierte del levantamiento de los muertos; cada tres días (diseño propuesto) | Evento VERIFICADO; gatillo por aldeano y frecuencia de 3 días: PENDIENTE (hoy es calendario rotativo) |
| Materiales de la Niebla | Errante Velado suelta `jiron_velo_espectral` (85 % si lo mata un jugador); existe `soul_shard`; el material del Osario Quebrado está sin definir | VERIFICADO / pendiente de rediseño (decisión del autor D-05) |
| Objeto especial | Se fabrica con materiales de la Niebla para interactuar con mecanismos del templo | **PENDIENTE: receta no definida**; no se inventa id ni receta |
| Templo | Exploración, subjefe (Autómata Antiguo), puzle, uso del objeto, cámara principal, Custodio de las Cuatro Llaves, cuatro llaves, cuatro cámaras de recompensa | Estructura base VERIFICADA (`temple/`); correspondencia exacta NO VERIFICADA |
| Eclipse | Ver abajo | Ver abajo |
| Lluvia de brasas | Ruta paralela vinculada a un portal al Nether; Escorificador de Hierro | Evento y mob VERIFICADOS; gatillo por portal NO VERIFICADO |

## Eclipse (decisión del autor integrada)

**Dos acontecimientos distintos que no deben confundirse:**

| | Primer Eclipse (histórico) | Eclipse invocado (actual) |
|---|---|---|
| Cuándo | Período 8, Primera Era | Aventura del jugador, período 12 en adelante |
| Quién | La gente, **por ignorancia** de lo que desataba | El jugador, con conocimiento y objetivos cumplidos |
| Resultado | Catástrofe: casi toda la humanidad perece; sobreviven unos pocos | Evento temporal que el jugador debe sobrevivir |
| Estado | Solo narrativo | Evento VERIFICADO (`EclipseManager`); la invocación por sacrificio es PENDIENTE |

**ESTABLECIDO (autor):** cuando el jugador cumple ciertos objetivos, **puede invocar el Eclipse cada vez que
tenga el material para sacrificar en la Hoguera**, con 10 días de espera mínima. Esto sustituye la idea del
borrador de un Eclipse «único por mundo».

Consecuencias para el lore (PROPUESTA salvo lo indicado):

1. El Eclipse invocado **repite** la conexión que causó el Primer Eclipse, pero de forma temporal y acotada.
   Por eso las entradas dicen que el sacrificio «permite que aquella conexión vuelva a establecerse».
2. La **Fractura Silenciosa** (LORE-014) se desbloquea una sola vez por jugador, al **finalizar un Eclipse**.
   Si los Eclipses posteriores la ensanchan o solo la «pulsan» sigue abierto (D-09). Recomendación: la pulsan sin
   ensanchar, para no contradecir el cierre del segundo arco.
3. **Qué objetivos exige** invocarlo y **qué material** se sacrifica: sin definir (D-18, D-02). Los textos usan
   «ofrenda» de forma genérica, salvo LORE-013 («la reliquia»), que se mantiene como texto del autor.
4. El «cada 10 días» del código es hoy un intervalo de **calendario** (`eclipseIntervalDays = 10`). Pasarlo a
   «espera tras un sacrificio» es un cambio de mecánica **fuera de esta tarea**.

Mientras la mecánica no exista, el Grimorio **no** debe afirmar «cada Eclipse exige un sacrificio».

### Durante el Eclipse (PROPUESTA narrativa + estado)

- Aparecen o se potencian élites y campeones; la Conciencia ejerce **influencia temporal** sobre su conducta.
- Persecución avanzada y búsqueda del último punto visible: **VERIFICADO** (`event/EclipsePursuit`,
  `EclipsePursuitRules`; v52). Asalto por oleadas, equipo completo visible, presupuestos locales/globales:
  **VERIFICADO** (`EclipseAssaultRules`, v47).
- Superar obstáculos, romper o colocar bloques de forma regulada: **NO VERIFICADO**; no se implementa aquí.
- Al terminar: la influencia cesa, la Conciencia sigue presa, la prisión queda debilitada (Fractura Silenciosa).
- La **muerte de un jugador no es** una liberación de la entidad.

## Segundo arco: El Camino hacia la Liberación (PROPUESTA)

Comienza cuando el jugador **completa un Eclipse**. La Fractura no provoca invasiones inmediatas; el mundo recupera una
normalidad aparente. El jugador investiga consecuencias mediante documentos y ruinas.

Estructuras conceptuales (**no existen, no se crean**): Archivo de los Sellos, Observatorio del Umbral, Cripta del
Último Testigo y Santuario de las Dos Verdades.

Verdades que el jugador acaba comprendiendo:

1. La prisión surgió **naturalmente** durante la Gran Separación.
2. La Conciencia desea escapar.
3. Sus comunicaciones pudieron manipular a los investigadores **con información verdadera**.
4. El Eclipse debilitó una parte del confinamiento.
5. La Fractura **no** equivale a una puerta transitable.
6. **Ninguna facción** conocía toda la verdad.

El arco termina sin repetir el Eclipse como clímax y **sin liberar** a la entidad. Como el Eclipse se puede
invocar de nuevo, los siguientes siguen existiendo como mecánica sin contradecir este cierre.

## Reglas multijugador

- **Mundo:** el estado de eventos (incluido el Eclipse) pertenece al mundo/servidor (`WorldEventState`,
  `EclipseState`).
- **Jugador:** el progreso de lectura y los descubrimientos pertenecen a cada jugador (el bestiario ya es así:
  attachment `BESTIARY`).
- Un descubrimiento individual **no** cambia un estado global.
