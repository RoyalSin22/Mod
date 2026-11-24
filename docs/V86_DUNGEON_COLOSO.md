# V86 · Dungeon del Coloso Sin Rostro (diseño propuesto)

**Estado: PROPUESTA. No hay código.** Decisiones ya tomadas por el autor (2026-11-24, `DECISIONES_PENDIENTES` D-37): los sellos de
invocación **no vuelven**; el **Coloso Sin Rostro** es el **jefe final** de un dungeon nuevo y el **Guardián Desmontado** su **subjefe**;
estructura **propia** (distinta del Templo del Custodio); ambos aparecen **dormidos al generarse** y **una sola vez por estructura**.

## 1. Lo que ya existe y se reutiliza

| Necesidad | Pieza existente |
|---|---|
| Distribución en una cuadrícula de celdas, pura y con JUnit | patrón de `temple/CustodianTempleLayout` (`Cell`, `Result`, `buildDeep`) |
| Traducir celdas a bloques y colocar cofres | `world/StructureBuilder` y el patrón de `world/CustodianTempleFeature` (radio máximo seguro 15) |
| Anillo de Amenaza, paleta por anillo | `TempleFeature.ring(...)`, `TempleFeature.Palette` |
| Registrar y repartir la estructura | `ModWorldGen`, JSON `worldgen/configured_feature` y `placed_feature`, `RegionGridPlacement` (una por región) |
| `/aptitudes locate` y `place` | `world/StructureIndex.record(...)`, `StructureSearch`, `StructureLocateRules` |
| Jefes dormidos que aparecen tras la generación (nunca durante) | `Temples.spawnAutomaton` y el bloque controlador `TempleSealBlockEntity` |
| Arena con niebla temporal | `world/BossArenas`, `TemporaryBlocks` |
| Jefes, fases, ataques y animaciones | `boss/FacelessColossusEntity`, `GuardianEntity` (ya jugables, GeckoLib) |
| Botín por anillo | `data/aptitudes/loot_table/chests/*` y el patrón de `chestLoot(cell)` |

## 2. Propuesta de estructura (a confirmar)

Subterráneo, como el Templo del Custodio hondo, pero **más largo que ancho** para dar espacio al Coloso (3,8 bloques de alto, ojos a 3,4):

| Nivel | Contenido |
|---|---|
| Acceso | Foso con rampa desde la superficie (reutiliza la solución del Custodio) |
| Piso 1 · Sala de guardias | Criaturas por anillo de Amenaza (oleada única). Sin puzle |
| Piso 2 · Cámara del Guardián | **Subjefe** dormido en el centro; al entrar se levanta la niebla de arena y se cierran las salidas; al vencerlo se abre la escalera |
| Piso 3 · Sala del Coloso | **Jefe final** dormido; techo de al menos 7 bloques; ruinas-columnas rotas (ya existen en `BossArenas`) |
| Cámara del tesoro | Cofres por anillo tras vencer al Coloso; el trofeo del jefe (ver §4) |

Todo dentro del radio 15 de un `Feature`, como el Custodio. El controlador (un bloque con entidad de bloque) guarda el estado
`DORMIDO → GUARDIÁN_VIVO → GUARDIÁN_VENCIDO → COLOSO_VIVO → VENCIDO` y **nunca vuelve atrás**: eso implementa «una sola vez por estructura».

## 3. Fases (cada una termina con `./gradlew build` en verde y su commit)

1. **Layout puro** (`dungeon/ColossusDungeonLayout` + pruebas): celdas, conectividad, alturas mínimas y cabida de los dos jefes.
2. **Feature y datos de mundo:** `ColossusDungeonFeature`, configured/placed feature, `RegionGridPlacement`, `StructureIndex.record`, `/aptitudes locate|place colossus_dungeon`.
3. **Controlador y jefes:** bloque controlador, despertar del Guardián y del Coloso, arenas, estado persistente, anillo de Amenaza.
4. **Botín, bestiario y lore:** tablas de botín, carta del Coloso en el bestiario (hoy falta), entradas de las Crónicas, textos en 5 idiomas, `CONFIGURATION.md`.
5. **Limpieza:** borrar los restos de los sellos (`boss/SummonSealItem`, `SummonRules`/`SummonState` si nada más los usa, filas de `gen_items.py`) y actualizar `docs/`.

## 4. Preguntas abiertas para el autor

| # | Pregunta | Mi recomendación |
|---|---|---|
| 1 | **Nombre** del dungeon (y su papel en el lore: D-07 sigue abierta, el Coloso no tiene papel fijado) | Un nombre provisional neutral («Ciudadela del Coloso») hasta decidir el lore |
| 2 | **Recompensa del Coloso:** trofeo propio (como el Cráneo del Gran Osario), Corazones de campeón, plantillas de ascensión, runas... | Tabla por anillo + un trofeo narrativo; sin poder nuevo hasta probar el balance |
| 3 | **Dificultad:** ¿escala con el anillo de Amenaza del lugar (como el Custodio) o es fija? | Escala con el anillo, con tope en el V |
| 4 | **Frecuencia:** ¿más rara que el Templo del Custodio (1 de cada 120 chunks)? | Más rara: una región por cada 2–3 del Custodio |
| 5 | **Acceso:** ¿abierto, o con una llave/objeto de la cadena (p. ej. el Ancla del Velo)? | Abierto, para no alargar la cadena actual sin probarla |
| 6 | ¿Se conserva el **ciclo de espera de 20 min** (`SummonRules`) para algo, o se borra con los sellos? | Borrarlo en la fase 5 |

## 5. Riesgos

- **Generación de mundo:** no se puede probar sin cargar un mundo; el único control automático es el layout puro. Se prueba con `/aptitudes place` y mundos nuevos y antiguos.
- **Tamaño de los jefes:** el Coloso (hitbox 1,6×3,8) y sus ataques de área piden una sala amplia; si el radio 15 no basta, habría que partir la sala en dos `Feature`s.
- **Sin validar en partida** el combate del Coloso dentro de una estructura cerrada (las pruebas actuales son en terreno abierto).
