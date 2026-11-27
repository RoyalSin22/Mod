# V91 — Revisión visual de las Tres Torres, Fase 1

El autor probó la fortaleza V88 con `/aptitudes place three_towers` y confirmó que los cortes entre
chunks, las columnas, las escaleras y el Coloso funcionan. La geometría V88 no cambia en esta fase.
El objetivo es poder inspeccionarla antes de dibujar la versión medieval gótica detallada.

## Artefactos de revisión

```text
./gradlew exportFortressSchem
python tools/fortress/preview.py build
```

El exportador escribe `build/fortress.schem` en formato Sponge Schematic v3, `build/fortress.voxels`
y `build/fortress.palette`. El script de Pillow comprueba que el `.schem` contiene exactamente el
mismo volumen y genera `build/fortress_plan_y{1,7,13,19,25}.png` y
`build/fortress_isometric.png`. La ruta dorada de la planta baja se calcula con búsqueda de
caminos sobre el plano exportado; para mostrar el tramo final considera abierta la reja V88.

En el mundo creativo de Minecraft 1.21.1 con Aptitudes y WorldEdit compatibles, coloca el archivo
en la carpeta de esquemas de WorldEdit, usa `//schem load fortress` y pega el portapapeles con
`//paste` sobre terreno llano. El desplazamiento del esquema es `(-64,-3,-64)` desde el punto
de pegado. El esquema contiene el volumen del plano, sin cimientos adaptados al terreno ni datos
de botín, estados del controlador o enemigos; sirve para revisar forma y circulación.
Las capturas a altura del jugador tomadas allí deben ser aprobadas por el autor antes de Fase 2.

`FortressPalette` es la fuente común de estados para la generación y la vista previa: piedra en
anillos bajos, pizarra profunda en medios y piedra negra en altos, con desgaste y variación de
madera/vidrio según el bioma. El controlador y las rejas V88 son las dos excepciones funcionales
a los bloques vanilla. El bioma se consulta en el origen común de la fortaleza para evitar costuras
de material entre chunks. El exportador toma por defecto anillo 3 y `minecraft:plains`; puede recibir
otros valores como segundo y tercer argumento de su clase Java. La geometría no depende de esos
valores.

## Presupuesto del rediseño

La medición del plano que realmente coloca el `Feature` da **9535 columnas ocupadas, 179 813
celdas y un máximo de 11 653 celdas en un chunk**, repartidas en 64 chunks. Una exportación pura
local tardó 220 ms; no mide la generación del mundo ni TPS. Para la Fase 2 propongo los topes
iniciales de **19 000 columnas, 359 626 celdas y 23 306 celdas por chunk** (aproximadamente el
doble del plano actual). Superarlos exige medir tiempo de generación por chunk en partida antes
de aceptar el diseño.

## Decisiones y siguiente fase

El autor eligió **A: sin barrera en la entrada del Mariscal**. La topología guía al jugador y el
controlador mantiene al Coloso dormido hasta la derrota de ambos Guardianes; los cofres del tesoro
solo aparecen al vencer al Coloso. El Mariscal quedará físicamente accesible antes del combate,
un cambio de recorrido respecto de V88. La reja del tesoro también se retira en la Fase 2; el
tesoro sigue vacío hasta la derrota. `vigil_gate` permanece registrado para el templo y los mundos
antiguos. El layout 31×31 se conserva.

La Fase 2 espera el concepto del autor y sus capturas reales del `.schem` aprobadas a altura de
jugador. Solo entonces se sustituye la geometría, se eliminan las celdas `GATE`/`REWARD_GATE` de
la fortaleza y se actualizan controlador y pruebas. Las decoraciones propias quedan limitadas a
cinco piezas justificadas por la vista previa, con ids nuevos, recursos generados mediante `tools/`,
fuente `.bbmodel` si aplica y textos generados en `en_us` y `es_es`; ninguna añade poderes.

## Verificación pendiente

- Importar el `.schem` en WorldEdit y comprobar visualmente las cuatro fachadas y cada sala.
- Medir MSPT y tiempo de generación de la versión rediseñada por chunk.
- Comprobar en partida que la paleta por anillo y bioma produce los bloques esperados.

**No se ha probado en partida esta Fase 1.**
