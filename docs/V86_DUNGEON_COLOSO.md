# V86 · Las Tres Torres Señoriales (diseño)

**Estado: DISEÑO con las decisiones del autor del 2026-11-24 (D-37 y D-38). No hay código todavía.** El encargo ejecutable, pensado para
que lo tome Claude o Codex, está en [`PROMPT_DUNGEON_TRES_TORRES.md`](PROMPT_DUNGEON_TRES_TORRES.md). (El nombre del archivo es el de la
primera propuesta, «Dungeon del Coloso»; se conserva para no romper enlaces.)

## 1. Decisiones del autor

| Tema | Decisión |
|---|---|
| Nombre | **Las Tres Torres Señoriales** |
| Jefes | **Coloso Sin Rostro** = jefe final (el Señor corrompido en su armadura). **Guardián Desmontado** = subjefe (un vasallo). Sin sellos de invocación; dormidos al generarse; **una sola vez por estructura** |
| Lore | Al caer el Primer Eclipse, el líder de la casa vio corrompida su alma y esta se infundió en su armadura: ahora es un ente corrompido. Sus vasallos, también |
| Forma | **Tres torres** sobre la superficie (referencias en el brief), no un templo subterráneo |
| Recompensa | **Un logro y una Plantilla de ascensión** (objeto que ya existe desde V80) |
| Rareza | **Más rara que el Templo del Custodio** (hoy 1 de cada 120 chunks) |
| Dificultad | **Alta**, también los enemigos (por encima del anillo de Amenaza del lugar) |

## 2. Cómo encaja con lo que ya existe

| Pieza | Papel |
|---|---|
| `boss/FacelessColossusEntity` | El Señor corrompido: «sin rostro» = el yelmo vacío. Jefe final |
| `boss/GuardianEntity` (Guardián Desmontado) | Vasallo corrompido: subjefe de cada torre vasalla |
| `item/AscendedTemplateItem` | Plantilla de ascensión: recompensa garantizada |
| Patrón de `temple/CustodianTempleLayout` + `world/CustodianTempleFeature` + controlador | Layout puro con pruebas, `Feature` que lo traduce a bloques, bloque controlador que despierta a los jefes tras la generación |
| `world/RegionGridPlacement`, `StructureIndex` | Rareza por región y `/aptitudes locate|place` |
| `world/BossArenas` | Niebla de arena y ruinas del Coloso |
| `achievement/Achievement` + `tools/advancements/gen_advancements.py` | El logro (los JSON se generan, no se editan) |
| `guide/LoreCatalog`, `LoreRules`, `LoreManager` | Entradas de las Crónicas |

## 3. Estructura propuesta (a confirmar)

Tres torres de piedra musgosa en triángulo dentro del radio 15 del `Feature`, unidas por un patio y escalinatas:

| Torre | Altura | Contenido |
|---|---|---|
| Vasalla Oeste | ≈ 26 | Pisos de combate (vasallos corrompidos) y, arriba, un **Guardián** dormido. Al vencerlo se abre la puerta del Señor |
| Vasalla Este | ≈ 26 | Igual, con otro Guardián (variante de fase avanzada) |
| **Torre del Señor** | ≈ 40 | Cerrada hasta vencer a los dos Guardianes. Piso alto con la sala del **Coloso** y la cámara con la Plantilla de ascensión |

Remates de las torres con **gárgolas/empuñaduras de espada gigantes** (guiño a la tercera referencia: tres espadas hincadas). El estado del
controlador es `DORMIDO → VASALLOS (0/2, 1/2) → SEÑOR_ABIERTO → COLOSO_VIVO → VENCIDO` y **nunca retrocede**.

## 4. Lo que hay que vigilar

- **Exclusión de `AGENTS.md` §3 (linajes, razas, pactos, herencias).** «Casa», «Señor» y «vasallos» son **solo ambientación**. Ninguna mecánica
  puede depender de ascendencia ni de linaje, ni darse por pertenecer a una casa. Que el alma quede *infundida en una armadura* no es un pacto
  del jugador: no se ofrece ningún pacto.
- **Cronología:** el «Primer Eclipse» es siempre el hecho histórico de la Primera Era (`lore/02_CRONOLOGIA.md`, período 8); nunca el Eclipse
  que invoca el jugador.
- **Referencias:** las tres imágenes son construcciones de otros autores (una lleva firma). Se usan **como inspiración de siluetas y
  proporciones**, no se copian bloque a bloque ni se suben al repositorio.
- **Generación de mundo:** sin un mundo cargado solo se prueba el layout puro. Una torre de 40 bloques exige comprobar la altura del mundo y
  el terreno (`StructureBuilder.suitable`).

## 5. Fases

1. **Layout puro** (`dungeon/TowersLayout` + pruebas: conectividad, alturas, cabida de los jefes, que ningún bloque salga del radio 15).
2. **Feature y datos de mundo:** `ThreeTowersFeature`, JSON de configured/placed feature, `RegionGridPlacement` más raro, `StructureIndex`, `locate|place`.
3. **Controlador y jefes:** vasallos, despertar de Guardianes y Coloso, arenas, estado persistente, dificultad alta.
4. **Recompensa y narrativa:** Plantilla de ascensión, logro, cartas de bestiario (el Coloso y el Guardián), Crónicas, textos en 5 idiomas, `CONFIGURATION.md`.
5. **Limpieza** de los restos de los sellos (`boss/SummonSealItem`, `SummonRules`, `SummonState`, filas de `gen_items.py`) y documentación.

## 6. Preguntas que siguen abiertas

| # | Pregunta | Recomendación |
|---|---|---|
| 1 | **Nombre de la casa y del Señor** (hoy «tal casa») | Dejar el nombre de la casa sin fijar en el código; solo claves de idioma para poder cambiarlo |
| 2 | ¿Las dos torres vasallas deben vencerse **antes** de abrir la del Señor? | Sí: da el recorrido de «tres torres» y justifica el subjefe |
| 3 | **Cuánto más rara:** frecuencia concreta | Región de 64×64 chunks-grid (≈4 veces más rara que el Custodio) |
| 4 | **Cuánto más difícil:** ¿anillo del lugar +2, o mínimo fijo III? | Mínimo fijo III y +1 sobre el anillo del lugar, con tope V |
| 5 | **Acceso:** ¿abierto o con objeto de la cadena? | Abierto |
| 6 | ¿Además de la Plantilla, una pequeña tabla de botín por anillo? | Sí, sin poder nuevo |
| 7 | **Quién lo ejecuta:** Claude (en código, con el brief) o Codex | Claude el código (layout, `Feature`, controlador, pruebas); Codex puede encargarse de los textos de lore y de las cartas del bestiario |
