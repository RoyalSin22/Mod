# Encargo: «Las Tres Torres Señoriales» (dungeon de superficie)

Brief **autocontenido** para quien lo ejecute (Claude o Codex). Lee antes `AGENTS.md` y `docs/V86_DUNGEON_COLOSO.md` (decisiones D-37 y D-38).
Responde en español. Una versión por tarea y rama. No cambies ids de registro existentes ni valores de balance por defecto sin preguntar.

## 1. Qué es

Una estructura de **superficie** formada por **tres torres señoriales** de piedra musgosa en triángulo, con un patio central. Dos son
**torres vasallas**, cada una con un **Guardián Desmontado** (subjefe) dormido; la tercera, la **Torre del Señor**, la más alta, guarda al
**Coloso Sin Rostro** (jefe final). Es de **alta dificultad**, **más rara que el Templo del Custodio**, y su recompensa es **un logro y una
Plantilla de ascensión** (`aptitudes:ascended_template`, ya existe).

**Lore (ambientación, sin mecánica):** durante el **Primer Eclipse** (hecho histórico de la Primera Era, `docs/lore/02_CRONOLOGIA.md`, período 8)
el **Mariscal** de la orden que guardaba estas torres vio corrompida su alma, y esta quedó infundida en su armadura: es el Coloso «Sin Rostro», un
yelmo vacío que aún se cree el jefe de la guarnición (su nombre se perdió con su rostro). Sus caballeros corrieron la misma suerte: son los
Guardianes «Desmontados». **Reglas:** nunca llamar «Primer Eclipse» al que invoca el jugador; **no** introducir linajes, razas, pactos, herencias
ni progresión por ascendencia (`AGENTS.md` §3); **no** hablar de «casa», «estirpe» ni «herederos», y **no** inspirarse en Juego de Tronos. Es una
**orden** (institución, como la Orden de los Cuatro Sellos) y el líder tiene un **cargo**, no un título hereditario. El nombre de la orden
**no está fijado** (lluvia de ideas en `V86_DUNGEON_COLOSO.md` §7): usa claves de idioma, no cadenas en el código.

## 2. Referencias visuales (solo inspiración de silueta y proporción)

El autor aportó tres imágenes. No se copian bloque a bloque (son obra de otros constructores) ni se suben al repositorio.

| Ref. | Qué enseña |
|---|---|
| 1 · Torre musgosa escalonada | Cuerpo estrecho que se retranquea en 3–4 tramos; **almenas en cada retranqueo**; ventanas altas en arco; puertas de madera en dos alturas; musgo y piedra mezclada (piedra, adoquín, ladrillo musgoso); base con escalera lateral y vegetación |
| 2 · Torre de ladrillo claro | Zócalo ancho con contrafuertes y escalera frontal; puerta en arco; **pilastras verticales** en las esquinas; ventanas con marco saliente; **parapeto en voladizo** con almenas y aberturas |
| 3 · «Solas»: tres espadas gigantes | **Tres monumentos verticales** en forma de espada hincada (guarda en cruz, pomo), sobre una base escalonada. Aplícalo como **remate de las torres**: guarda en cruz y pomo gigantes coronando cada una |

**Paleta de bloques** (musgosa, antigua, oscura): `stone_bricks`, `mossy_stone_bricks`, `cracked_stone_bricks`, `cobblestone`, `mossy_cobblestone`,
`stone_brick_stairs/slab/wall`, `chiseled_stone_bricks`, `deepslate_bricks` en la base, `dark_oak` para puertas y vigas, `lantern`/`soul_lantern`,
`iron_bars` en ventanas; musgo y `vine` repartidos. Usa la paleta por anillo de Amenaza de `TempleFeature.Palette` como respaldo.

## 3. Medidas y generación (restricciones duras)

- Un `Feature` solo escribe dentro del **radio 15** del origen (como `CustodianTempleFeature`). Tres torres de ≈ 9×9 en triángulo caben:
  p. ej. centros en (−9,−6), (9,−6) y (0,9). Verifica con una prueba que **ningún bloque cae fuera del radio**.
- Medidas ya fijadas por `dungeon/TowersLayout` (fase 1 hecha): vasallas 11×11 con techo en y 26, Señor 13×13 con techo en y 34; con la
  empuñadura la cota máxima es 44. Comprueba `origin.getY() + TowersLayout.TOP_Y < world.getTopY() - 1`.
- Terreno: `StructureBuilder.suitable(world, origin, R, MAX_SLOPE)`; nada de agua ni lava en la huella; el patio se nivela con un zócalo.
- **No crear entidades durante la generación.** Los Guardianes y el Coloso los despierta después un **bloque controlador** (patrón de
  `TempleSealBlockEntity` / `Temples.spawnAutomaton`), en el hilo del servidor, etiquetando las entidades para no duplicarlas.
- Distribución en una **clase pura** (`dungeon/TowersLayout`, sin Minecraft) con celdas, como `CustodianTempleLayout`, con JUnit.

## 4. Flujo y jefes

Estado del controlador (persistente, **nunca retrocede**): `DORMIDO → VASALLOS(0/2 → 2/2) → SEÑOR_ABIERTO → COLOSO_VIVO → VENCIDO`.

1. Cada torre vasalla: varios pisos con **vasallos corrompidos** y arriba un **Guardián** dormido que despierta al entrar en su sala (arena con
   niebla de `world/BossArenas`). Vencerlo cuenta 1/2.
2. Con 2/2, **se abre la puerta de la Torre del Señor** (bloque que desaparece o se retrae; sin objeto nuevo).
3. Torre del Señor: pisos de combate más duros y, arriba, el **Coloso** dormido. Al vencerlo: cámara con la **Plantilla de ascensión**
   garantizada, un cofre por jugador cercano (patrón de `claimed` del Relicario) y el **logro**.
4. **Una sola vez por estructura:** sin reaparición, sin sellos, sin reinvocación. El estado `VENCIDO` es definitivo.

**Dificultad alta:** anillo de Amenaza mínimo III y +1 sobre el del lugar, con tope V (confirmar, ver `V86_DUNGEON_COLOSO.md` §6). Los vasallos son
criaturas existentes con equipo completo visible (mismo mecanismo que las élites del Eclipse, `eclipseGuaranteedEliteArmor`), con afijos de élite.
Un nuevo tipo de criatura «Vasallo corrompido» **no** entra en esta tarea (exigiría modelo, textura y bestiario).

## 5. Rareza

Reutiliza el modificador `aptitudes:region_grid` con una región **más grande que la del Custodio** (propuesta: 4 veces más rara) y un
`placed_feature` propio. Registra con `StructureIndex.record` para que `/aptitudes locate three_towers` y `place` funcionen
(`StructureLocateRules`, textos `aptitudes.structure.*`).

## 6. Entregables por fase (cada una con `./gradlew build` real y su commit)

1. **Layout puro + pruebas — HECHA** (`dungeon/TowersLayout`, `TowersLayoutTest`). La traducción de celdas a bloques es de la fase 2.
2. **Feature y datos de mundo:** `ThreeTowersFeature`, `configured_feature` y `placed_feature`, registro en `ModWorldGen`, `locate|place`.
3. **Controlador y jefes:** bloque + entidad de bloque, despertar, arenas, estado persistente, dificultad.
4. **Recompensa y narrativa:** plantilla de ascensión, **logro** (añadir a `achievement/Achievement` y regenerar con
   `tools/advancements/gen_advancements.py`; no editar los JSON), cartas de bestiario del **Coloso** y del **Guardián** (`guide/BestiaryRules`),
   entradas de las **Crónicas** (`guide/LoreCatalog`, `LoreRules`), textos en en_us y es_es + variantes `es_*` y `pt/fr/de` con los generadores.
5. **Limpieza de sellos:** borrar `boss/SummonSealItem`, `SummonRules`, `SummonState` y las filas de `gen_items.py` si ya nada los usa, y
   actualizar `AGENTS.md`, `CHANGELOG.md`, `docs/ROADMAP_APTITUDES.md`, `docs/PENDIENTES.md`, `docs/TESTING.md`.

## 7. Reglas de la casa (resumen)

Autoridad del servidor; fórmulas en clases puras con JUnit; configuración nueva con valor seguro, límite en `validated(...)` y fila en
`docs/CONFIGURATION.md` (propuestas: `threeTowersEnabled`, región de rareza, anillo mínimo); sin mixins; APIs de Fabric/Yarn comprobadas en el
jar antes de usarlas; **no reformatear archivos**; copiar `es_es` a las seis variantes `es_*`; commits convencionales con
`Co-Authored-By: Claude <noreply@anthropic.com>` y `Claude-Session`; push solo a la rama de la sesión; PR en borrador. **Nunca afirmar que
algo compila o funciona sin haberlo ejecutado.** Entregar el informe de `AGENTS.md` §8.

## 8. Qué NO hacer

No traer de vuelta los sellos ni la reinvocación. No crear entidades en la generación. No añadir un mob nuevo, un objeto nuevo ni una dependencia.
No cambiar `ascended_template` ni sus recetas. No copiar las imágenes de referencia. No introducir mecánicas de casa, linaje o pacto.
