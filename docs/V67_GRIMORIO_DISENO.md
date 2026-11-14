# v67 · Grimorio y bestiario rediseñados (diseño para aprobar)

Estado: **propuesta, sin código.** Decisiones ya tomadas por el autor (2026-11-14): el bestiario queda **solo con criaturas
del mod**; cada carta muestra **modelo y nombre, estadísticas y región, botín y consejos, y lore**; el rediseño cubre
**todo el libro** con estética de pergamino (la lógica y la estructura de capítulos no cambian).

## 1. Estado actual (lo que ya existe y se reutiliza)

| Pieza | Dónde |
|---|---|
| Catálogo cerrado de entradas, desbloqueo por muerte y saneado de recuentos | `guide/BestiaryRules` (puro, JUnit), `GrimoireManager`, attachment `BESTIARY` (`copyOnDeath`), `GrimoirePayload` (S2C) y `GrimoireRequestPayload` (C2S, limitada) |
| Pantalla del libro (24 capítulos, ítems, recetas, buscador, marcadores, progreso, sendas por rol) | `client/screen/GuideScreen` (1416 líneas; páginas `CHAPTER`, `ITEM`, `BESTIARY`, `BEAST`, `PROGRESS`, `SEARCH`, `BOOKMARKS`, `ROLE`) |
| Cabeceras de capítulo generadas | `tools/textures/gen_guide_headers.py` → `textures/gui/guide/*.png` (384×34) |
| Estilo y piezas de pantallas con la dirección de arte | `KUi`/`KButton`, `TreeUi`, `RolesUi` (9-slice con respaldo al dibujo anterior), `logic/KScreenLayout` |

Hoy el bestiario tiene **57 entradas**: 36 criaturas de Minecraft, 2 jefes de Minecraft (Wither y Dragón), 2 de élite,
4 jefes propios reutilizando modelos de Minecraft, 3 jefes animados y 3 criaturas del mod (Osario quebrado, Errante
velado y Escorificador de hierro).

## 2. Catálogo nuevo (solo criaturas del mod)

| Id | Tipo | Región | Nota |
|---|---|---|---|
| `osario_quebrado` | Criatura | Superficie | ya existe |
| `errante_velado` | Criatura | Superficie (Niebla de almas) | ya existe |
| `escorificador_hierro` | Criatura | Nether (Lluvia de brasas) | ya existe |
| `custodio` | Criatura | (por confirmar) | **falta**: la entidad `CustodioEntity` existe pero no tiene página |
| `faceless_colossus`, `dismantled_guardian`, `ancient_automaton` | Jefe animado | Superficie | ya existen |
| `boss_tower_sentinel`, `boss_shrine_oracle`, `boss_camp_champion`, `boss_eclipse_herald` | Jefe propio | Superficie | ya existen |
| `elite`, `champion` | Élite | cualquiera | ya existen |

Total: **14 entradas** (las 43 de Minecraft se retiran). Los ids se conservan (no se renombran); las entradas nuevas se
añaden al final. Los recuentos guardados de las criaturas de Minecraft se descartan al cargar (`sanitize` ya ignora los
ids desconocidos): los jugadores que solo tenían muertes de Minecraft empiezan con el bestiario vacío.

## 3. La carta de criatura (página `BEAST`)

Pergamino a dos columnas dentro del libro (cabe en 426×240 o se desplaza):

| Zona | Contenido |
|---|---|
| Izquierda | Retrato: modelo de la criatura girando (o su icono si no tiene modelo), nombre sobre una cinta, sello de región y recuento de muertes |
| Derecha, arriba | **Estadísticas**: vida, daño, armadura y anillo de Amenaza donde suele aparecer |
| Derecha, centro | **Botín**: objetos que suelta (iconos con tooltip) |
| Derecha, abajo | **Consejos** de combate (1–2 líneas) y **lore** (un párrafo en cursiva) |

**Revelado progresivo por muertes** (propuesta, a aprobar): 1 muerte → nombre, modelo y región; 3 → estadísticas;
5 → botín y consejos; 10 → lore. Antes del primer encuentro, la carta muestra solo la silueta y «???». Para los jefes
(que se matan pocas veces) los umbrales son 1 / 1 / 1 / 2.

Las estadísticas se leen en el cliente de los atributos por defecto del tipo de entidad (`DefaultAttributeRegistry`), sin
copiar cifras en los textos (como pide AGENTS: el grimorio no duplica datos del juego). Los jefes muestran la vida base.

## 4. Estilo de pergamino en todo el libro

Un kit de piezas (`client/screen/GuideUi`, 9-slice con tramos repetidos, con respaldo al dibujo actual): fondo de página,
marco dorado con esquinas, cinta de capítulo, separadores, pestañas como marcapáginas y panel de carta. Se aplica a las
ocho páginas del libro sin cambiar su lógica. Diseño de la maqueta en `logic/GuideLayout` (puro, JUnit, como
`KScreenLayout`).

## 5. Arte

- El autor tiene una **imagen de referencia** para el bestiario y cree que **otro chat ya la está produciendo**: no se
  inventa arte final. Primero van **huecos con piezas provisionales** (generadas por script, sustituibles) y un encargo
  de arte en `docs/PROMPT_ARTE_GRIMORIO.md`; cuando llegue el arte se integra sin tocar código.
- Fuente del arte en `tools/textures/source/ui/guide/` (no se editan los PNG de salida) y generador
  `tools/textures/gen_guide_ui.py` con `--check`, como el resto de kits.

## 6. Coordinación con la sesión «Aptitudes narrative architecture audit»

Hay otra sesión en marcha (auditoría de arquitectura narrativa; según el autor, la IA habría generado un mob más). Puede
tocar **lore, textos y el catálogo de criaturas**, justo lo que usa el bestiario. Para no pisarnos: esta tanda **no
escribe lore definitivo** (deja las claves `bestiary.aptitudes.<id>.lore` con texto provisional corto) y **no cambia las
entidades**; cualquier criatura nueva se añade al final de `BestiaryRules` cuando esa sesión la confirme.

## 7. Fases

| Fase | Contenido | Pruebas |
|---|---|---|
| G1 | Datos y reglas: catálogo de 14 entradas, `custodio`, umbrales de revelado, saneado | `BestiaryRulesTest` (actualizada), `BestiaryRevealRulesTest` (nueva) |
| G2 | Carta de criatura (modelo, estadísticas, botín, consejos, lore) con revelado progresivo | `GuideLayout` + `GuideLayoutTest` |
| G3 | Kit de pergamino (`GuideUi`) y aplicación a todo el libro, con respaldo | `--check` del generador |
| G4 | Textos es/en (nombres, consejos, lore provisional) y pt/fr/de por catálogo | `check_lang` y pruebas de idiomas |
| G5 | Integración del arte cuando llegue | — |

Sin opciones de configuración nuevas. Documentación: CHANGELOG, TESTING, ROADMAP, AGENTS.

## 8. Qué necesito que apruebes

1. El **catálogo de 14 entradas** y añadir **`custodio`** (¿en qué región aparece?).
2. Los **umbrales de revelado** (1 / 3 / 5 / 10 muertes; jefes 1 / 1 / 1 / 2) o los que prefieras.
3. Que el **lore** sea provisional hasta que la sesión narrativa lo defina.
4. Que el arte quede en **huecos provisionales** y que me pases la **imagen de referencia** cuando la tengas.
5. ¿Hacemos **G1–G4 de una vez** y G5 cuando llegue el arte?
