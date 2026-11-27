# V91 — Revisión visual de las Tres Torres, Fases 1 a 3

El autor comprobó en partida la base V88: cortes entre chunks, columnas, escaleras y Coloso.
Después autorizó avanzar a la geometría V91 y pidió pulido y rendimiento para la Fase 3. La
prueba en partida y la aprobación de sus capturas se harán más adelante.

## Geometría y recorrido

La huella sigue siendo de 128×128 bloques en 64 chunks, con las tres torres, los mismos puntos de
aparición, el controlador y el tesoro en sus coordenadas anteriores. La nueva silueta incorpora
contrafuertes en las cuatro fachadas, cubiertas inclinadas y parcialmente hundidas, arcos de
escalera, ventanas altas, cornisa, atalayas, remate escalonado del Mariscal, barbacana y tres
brechas con escombros en el anillo de murallas. Las salas tienen mobiliario vanilla según su
función: bancos y atril en la capilla, estanterías y mesas en el estudio, camas en los dormitorios,
calderos y mesas de alquimia y encantamiento en el ala oriental. La Fase 3 añade una arcada
procesional con tramos hundidos y dos figuras sin rostro de muro y calcita en la entrada al patio.
Hay luz colgante, cofres modestos y cofres ocultos bajo lana morada marcada en el suelo.

La entrada del Mariscal y el tesoro están abiertos físicamente. El controlador solo despierta al
Coloso cuando han caído ambos Guardianes, y los cofres del tesoro aparecen al caer el Coloso. El
mensaje nuevo `marshal_ready` distingue esa apertura lógica. Los diseños V88 ya generados
conservan la rotura de sus rejas antiguas mediante el controlador. `vigil_gate` sigue registrado
para el templo y los mundos anteriores; el layout histórico 31×31 tampoco cambia. No se añadieron
bloques propios ni objetos de poder. La dificultad, el botín y la frecuencia siguen iguales.

## Artefactos de revisión

```text
.\gradlew.bat --offline exportFortressSchem
python tools/fortress/preview.py build
```

El exportador escribe `build/fortress.schem` en formato Sponge Schematic v3,
`build/fortress.voxels` y `build/fortress.palette`. El script comprueba que el esquema contiene
exactamente el mismo volumen y genera `build/fortress_plan_y{1,7,13,19,25}.png` e
`build/fortress_isometric.png`. La ruta dorada de planta baja sale de una búsqueda de caminos.

En un mundo creativo de Minecraft 1.21.1 con Aptitudes y WorldEdit compatibles, coloca el archivo
en la carpeta de esquemas de WorldEdit, ejecuta `//schem load fortress` y `//paste`. El origen del
esquema tiene desplazamiento `(-64,-3,-64)` respecto al punto de pegado. Este volumen puro no
incluye cimientos adaptados al terreno, inventarios de botín, controlador activo ni enemigos.
El render isométrico es un visor de bloques simplificado: las capturas a altura del jugador en
Minecraft son la evidencia necesaria para aprobar fachadas, ruinas y salas.

`FortressPalette` comparte estados entre la generación y la vista previa. Usa piedra en los
anillos bajos, pizarra profunda en los medios y piedra negra en los altos, con desgaste y madera
según el bioma consultado una sola vez en el origen de la fortaleza. El exportador toma por defecto
anillo 3 y `minecraft:plains`.

## Presupuesto y comprobación

El plano V88 medía 9535 columnas, 179 813 celdas y 11 653 celdas en su chunk más denso. V91 mide
**9772 columnas, 197 111 celdas y 11 725 celdas en el chunk más denso** antes de los cimientos,
en los mismos 64 chunks. La Fase 3 cambia 154 celdas de despeje por material decorativo sin
aumentar el total: **108 911 celdas de despeje y 88 200 de material**. El generador ahora consulta
el bloque existente antes de escribir una celda `AIR` y omite la escritura si ya es aire. Esto
mantiene el despeje de vegetación y terreno, y evita hasta 108 911 escrituras redundantes por
fortaleza en un sitio donde esas posiciones ya están vacías. No se ha medido la mejora de TPS ni
el tiempo de generación en Minecraft. Los topes de prueba son 19 000 columnas, 359 626 celdas y
23 306 por chunk.

JUnit comprueba el radio, el presupuesto, las salas y torres, el ascenso a cada jefe, el camino
entre el Coloso y el tesoro, la estabilidad de cofres y apariciones, la marca de cofres ocultos,
la ausencia de rejas nuevas y el determinismo tras consultas en orden aleatorio. El visor valida
los índices NBT del `.schem` contra el volumen antes de dibujar.

## Pendiente con las capturas del autor

- Revisar a altura de jugador la silueta y profundidad de las cuatro fachadas, de día y de noche.
- Recorrer la barbacana, las brechas, las tres torres, la capilla, el estudio, los dormitorios, el
  ala de alquimia y el tesoro; comprobar luz, mobiliario y lectura visual de las rutas.
- Medir MSPT y tiempo de generación por chunk en partida y revisar la paleta en varios biomas.
- Comprobar que dos Guardianes abatidos permiten despertar al Coloso y que su caída entrega el
  botín principal sin reja física.

**No se han probado en partida las Fases 2 y 3.**
