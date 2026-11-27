# Encargo V91 (auditado) — Aspecto visual de las Tres Torres Señoriales

Versión corregida del encargo visual V91, auditada contra `main` (`6507345`, 2026-11-27). Dos partes con **destinatarios distintos**:

- **Parte A** — prompt de **concepto**: lo ejecuta el **autor** en un generador de imágenes (p. ej. ChatGPT). Codex no genera imágenes.
- **Parte B** — encargo para **Codex**: herramientas de vista previa, plano, pruebas y, solo tras la aprobación del autor, la nueva geometría.

## Resultado de la auditoría (qué se corrigió y por qué)

| # | Hallazgo | Corrección |
|---|---|---|
| 1 | El prompt original mezclaba quién hace qué (concepto, prototipo en WorldEdit y código) | Partes A y B con responsables |
| 2 | Retirar la Reja (`vigil_gate`) de la fortaleza: el Coloso despierta solo si `doorOpen` (`TowersFlowRules.shouldWakeColossus`), y la puerta se abre al vencer a los dos Guardianes; los cofres de recompensa solo aparecen en `finish()`. Por tanto **saltarse la puerta rompiendo un muro no salta ningún combate** | Se puede retirar. Pero la Torre del Mariscal quedará **accesible desde el principio** (D-38 decía «no entra hasta vencer a los dos subjefes»): decisión del autor, ver B.3 |
| 3 | `TowersControllerBlockEntity.openGate(...)` y `FortressLayoutTest` (líneas con `Cell.GATE` y `Cell.REWARD_GATE`) dependen de la Reja | Hay que reescribirlos: se enumeran en B.4 |
| 4 | La geometría actual cumple un **contrato técnico** (origen, entrada, coordenadas de jefes, cofres de recompensa, 8×8 chunks, presupuesto) del que dependen el controlador y las pruebas | Se lista en B.2 como invariantes |
| 5 | El `Feature` actual usa **una paleta fija** (ladrillos de piedra); no hay paleta por anillo ni bioma, ni más de ~25 tipos de celda | Se pide una **paleta pura** (`FortressPalette`) compartida por el `Feature` y la herramienta de vista previa (B.1) |
| 6 | Presupuesto: v88 mide **9535 columnas y 179 813 celdas**; un diseño más rico las multiplica | Tope explícito y medición (B.5) |
| 7 | «5–10 decoraciones con modelo propio» implica bloques/ítems nuevos (registro, texturas, modelos, textos) | Se limita a las que justifique la vista previa y se exige el canal de generación del proyecto (B.6) |
| 8 | Codex no puede capturar pantallas de Minecraft; el prototipo WorldEdit lo construiría el autor a mano | Herramienta de **exportación a `.schem`** y **vistas previas** generadas desde el plano puro (B.1) |
| 9 | La fortaleza v88 **no se ha probado en partida**; decorar sobre una geometría sin validar es arriesgado | Precondición: probar `/aptitudes place three_towers` y la lista de `docs/V88_REDISENO_ESTRUCTURAS.md` (B.0) |
| 10 | La rama `codex/locate-estructuras` (commit local `128eb6f`, `locate` solo con índice) **no está en `main`** | No tocar `StructureLocateRules` ni `StructureSearch`; rebase si esa rama entra primero |
| 11 | El Templo V89 (ya en `main`) también usa `Temples.GATE`/`EXIT_GATE`; la Reja **no se puede borrar del registro** todavía | Solo se quita de la fortaleza; el registro y los usos del templo se conservan |
| 12 | El cofre oculto de la torre y los campamentos (V90) es un bloque llamativo con el cofre debajo; los de la fortaleza son cofres normales | Mismo criterio también en la fortaleza (B.2) |
| 13 | El encargo no mencionaba el campeón opcional del monasterio (`FortressLayout.MONASTERY_ELITE`) ni las oleadas por planta (`SPAWNS`) | Se conservan como invariantes (B.2) |

---

## Parte A — Prompt de concepto (para el generador de imágenes; lo ejecuta el autor)

> Diseña una fortaleza medieval gótica en ruinas, construible con bloques de Minecraft 1.21.1, dentro de una huella aproximada de 128×128 bloques.
> Conserva un anillo irregular de murallas rotas con barbacana y entrada principal al sur. Deben destacar tres torres de silueta distinta: la Torre del Mariscal,
> más alta y pesada; dos torres de vigilancia, más esbeltas, cada una con su atalaya. Evita edificios con forma de caja y superficies planas extensas. Usa contrafuertes
> escalonados, arcos apuntados, ventanas estrechas con tracería, cornisas, balcones dañados, tejados parciales, almenas irregulares, bóvedas, escaleras de piedra, vigas
> expuestas, pavimento con patrones, escombros estructurales, vegetación medida y puntos de luz integrados. Cada fachada debe tener profundidad y una composición legible.
> Salas: dormitorios con literas y baúles; barbacana con rastrillo decorativo y puestos de guardia; estudio con mesas, anaqueles y atriles; sala de botín; monasterio o
> capilla con altar y bancos; ala de pociones y encantamientos con mesas, estanterías y material de laboratorio.
> Presenta: (1) vista isométrica general con etiquetas y escala; (2) planos por nivel y recorrido; (3) cuatro vistas a altura del jugador (entrada, patio, capilla y ala de
> estudio); (4) secciones de las tres torres; (5) paleta de bloques de Minecraft y cinco a diez elementos de decoración que requerirían modelo propio.
> Muestra daño coherente: un muro derrumbado deja escombros debajo, una bóveda rota conserva apoyos, la vegetación no tapa el camino. Evita aspecto de pirámide, castillo
> intacto, mazmorra de habitaciones vacías o detalles imposibles de construir con bloques.

Qué hacer con el resultado: elegir entre las variantes, dejar la imagen en `docs/` y marcar el motivo arquitectónico que se quiere conservar. Las imágenes fijan la
**dirección artística**, no son el plano.

---

## Parte B — Encargo para Codex

### B.0 Precondiciones

1. Leer `AGENTS.md`, `docs/V88_REDISENO_ESTRUCTURAS.md`, `dungeon/FortressLayout`, `dungeon/TowersControllerBlockEntity`, `dungeon/TowersFlowRules`, `world/ThreeTowersFeature` y `FortressLayoutTest`.
2. El autor debe haber probado **la fortaleza actual** en un mundo (`/aptitudes place three_towers`: cortes entre chunks, columnas flotantes, escaleras, Coloso en sala alta) o haber aceptado
   expresamente decorar sin probar. Si no lo sabes, **pregúntalo antes de cambiar la geometría**.
3. No tocar `world/StructureLocateRules` ni `StructureSearch` (hay un cambio local pendiente de `locate`). Rama propia: `codex/fortaleza-visual-v91`. Sin merge a `main` ni PR sin autorización.

### B.1 Fase 1 — Herramientas de vista previa (sin cambiar la generación)

1. **`dungeon/FortressPalette`** (pura, sin Minecraft): dado `(Cell, x, y, z, anillo, bioma)` devuelve un **id de bloque vanilla** (p. ej. `minecraft:deepslate_bricks`) y sus propiedades. El `Feature` lo traduce a `BlockState`;
   la herramienta de vista previa lo usa tal cual. Paleta inicial por anillo (piedra → pizarra profunda → piedra negra) y bioma, como en `docs/V90_DECORACION_Y_VARIANTES.md` §1.
2. **Exportador a `.schem`** (formato Sponge, compatible con WorldEdit): una clase Java ejecutable con Gradle (`./gradlew exportFortress`) o un test marcado, que escribe `build/fortress.schem` desde `FortressLayout` + `FortressPalette`.
   Así el autor la pega en un mundo creativo con `//schem load` y la recorre a altura de jugador sin generar el mundo.
3. **Vistas previas**: PNG de la planta por nivel (con el recorrido y las salas etiquetadas) y una isométrica, generadas con Pillow desde el mismo plano (`tools/models/preview_fortress.py` o equivalente) para revisar sin Minecraft.
4. Entregar la Fase 1 y **esperar la revisión del autor** (concepto de la Parte A + `.schem` + vistas).

### B.2 Invariantes (no cambiar sin avisar y sin actualizar controlador y pruebas)

- Huella **128×128** (`MIN=-64`, `MAX=63`), 8×8 chunks, origen y `CONTROLLER` en `(0,0,0)`, **entrada sur** `ENTRANCE = {0,1,62}`, `BOTTOM_Y=-3`, `TOP_Y=62`.
- Torres (`FortressLayout.WEST/EAST/MARSHAL`) y coordenadas de aparición de jefes `WEST_BOSS`, `EAST_BOSS`, `COLOSSUS`; lista `SPAWNS` (oleadas únicas por planta); `MONASTERY_ELITE` (campeón opcional); `REWARD = {28,25,-58}` (hasta cinco cofres personales).
- Sala del Coloso de **37×37 interiores y 15 de alto**; recorrido a pie **sin saltos obligatorios**; todos los cofres y apariciones con suelo estable (`everyChestAndCombatSpawnHasAStableFloor`).
- Ningún enemigo se genera al crear el chunk; el controlador despierta jefes y oleadas solo cerca de jugadores. Sin poderes ni objetos nuevos de poder.
- Cofres ocultos: **un bloque llamativo en el suelo (lana u otro) y el cofre debajo**, como en la torre y los campamentos de V90.
- Ids de registro, jefes, logro `three_towers` y recompensas sin cambios.

### B.3 Decisión del autor pendiente (Reja)

Quitar la Reja de la fortaleza deja la **Torre del Mariscal abierta desde el principio**. El Coloso sigue dormido hasta vencer a los dos Guardianes (`doorOpen`), pero el jugador puede entrar y pelear
las plantas de élites antes. Opciones:
- **A (recomendada y la del brief):** sin barrera; la topología (arcos, escaleras, derrumbes) guía y el controlador guarda el estado.
- **B:** una **barrera diegética** distinta de la Reja (portón de madera y hierro derribable) que el controlador retira; sin bloque irrompible nuevo.

Codex presenta las dos en la Fase 1 y el autor elige. La Reja **no se borra del registro** (la usan el Templo del Custodio y el Templo V89).

### B.4 Fase 2 — Solo tras aprobación: nueva geometría

1. Reescribir `FortressLayout` (y, si hace falta, dividirlo en clases puras) con el diseño aprobado: fachadas con profundidad, contrafuertes, arcos, ventanas con tracería, cornisas, tejados parciales, bóvedas, vigas, pavimento con patrones,
   escombros con **causa física legible**, vegetación medida que no tapa el camino, iluminación integrada y mobiliario por sala (dormitorios, barbacana con rastrillo decorativo, estudio, sala de botín, monasterio, ala de pociones y encantamientos).
2. Quitar `vigil_gate` de la fortaleza: actualizar `ThreeTowersFeature.state` (celdas `GATE`/`REWARD_GATE`), `TowersControllerBlockEntity.openGate/openLordDoor/finish`, `FortressLayoutTest` (`marshalArenaAndTreasury…`, `reachable…BeforeTheMarshalGate`) y `TowersLayoutTest` solo si
   se toca el camino antiguo de 31×31 (que se conserva para los mundos ya generados).
3. Pruebas nuevas o ajustadas con JUnit: alcanzabilidad a pie de todas las salas, de las tres torres y del tesoro; dimensiones del Coloso; suelo estable de cofres y apariciones; ausencia de celdas fuera de los 8×8 chunks; determinismo; **que la fortaleza no dependa de la Reja**.
4. Los demás cambios (textos, `CHANGELOG.md`, `docs/V88_REDISENO_ESTRUCTURAS.md` o un `V91_…` nuevo, `docs/TESTING.md`) con los generadores de idiomas y sin reformatear archivos.

### B.5 Presupuesto y rendimiento

- Medir en la Fase 1 las celdas ocupadas/escritas del plano actual (v88: 9535 columnas y 179 813 celdas) y **proponer un tope** para el nuevo diseño. Estimación orientativa del auditor: no pasar de ~2× la cantidad actual sin medir el tiempo de generación por chunk.
- No generar chunks vecinos ni consultar el terreno por celda; la altura y la admisión se calculan **una vez por origen** como hoy.

### B.6 Decoraciones con modelo propio (opcional)

Solo las que la vista previa demuestre que no se logran con bloques vanilla (estatuas sin rostro, capiteles, relicarios, mobiliario). Cada una exige: registro con id nuevo, textura y modelo generados con las herramientas del proyecto (`tools/`; fuente `.bbmodel` conservada si se usa Blockbench),
textos en `en_us` y `es_es` con los generadores, y que **no añada poderes**. Máximo cinco en esta versión; el resto, a vanilla.

### B.7 Criterios de aceptación

- Silueta reconocible desde fuera y profundidad en las cuatro fachadas; cada sala obligatoria con mobiliario, luz y un elemento arquitectónico que exprese su función.
- Ruinas con causa física legible; el jugador distingue entrada, rutas secundarias, tres torres y tesoro sin mapa.
- Capturas a altura de jugador (del `.schem` en un mundo creativo) **aprobadas por el autor antes** de la Fase 2.
- `./gradlew build` y `check_lang.py` ejecutados de verdad; informe final de `AGENTS.md` §8 sin afirmar lo que no se ejecutó, y **declarando que no se ha probado en partida**.
