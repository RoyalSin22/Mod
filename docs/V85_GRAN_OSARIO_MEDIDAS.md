# Gran Osario Quebrado V85 · Medidas de la exportación

El editable de Blockbench está en `tools/models/blockbench/gran_osario_quebrado.bbmodel`. Los recursos Bedrock se exportaron con el identificador `geometry.gran_osario_quebrado` a `geo/entity`, `animations/entity` y `textures/entity` bajo `src/main/resources/assets/aptitudes`. La textura base, la máscara emisiva y el mapa separado de fisuras tienen las mismas dimensiones. El modelo, las animaciones y la textura anteriores se conservaron byte por byte en `tools/models/blockbench/ref/gran_osario_v68/`.

## Correcciones comprobadas

- Extendí los cuatro anclajes proximales de raíz y la viga principal de la jaula. La carcasa conserva los **315 cubos** y los pivotes de los **47 huesos**.
- Separé con desplazamientos pequeños las caras coincidentes de antebrazo, tibia, jaulas y cráneos secundarios. El muestreo de exposición registró **0 pares coplanares expuestos**, frente a 11 en Fase 6. Quedan siete coincidencias bajo las suelas.
- Ajusté la elevación y la mandíbula en el Anillo y la Cosecha, y la mandíbula al comienzo de la muerte. Repetí el barrido de **2.863 poses** y pasó de 924 muestras con piezas separadas a **0**. No se detectaron penetraciones de **0,025 u o más** en los cristales vivos en esas muestras. El muestreo también conserva las fusiones estructurales previstas entre tierra, raíces y osamenta.

## Cotas de reposo

| Fase | Ancho × alto × fondo | Cubos visibles en la pose | Nudillo derecho |
| --- | ---: | ---: | ---: |
| I | 39,282 × 53,043 × 31,735 u | 297 | 2,889 u |
| II | 39,283 × 53,075 × 32,046 u | 298 | 2,981 u |
| III | 40,139 × 52,929 × 31,078 u | 315 | 2,626 u |

El volumen esencial de cabeza, mandíbula, jaulas y tórax queda dentro de la hitbox existente de **1,6 × 3,2 bloques**. Las extensiones laterales siguen siendo visuales. La marcha conserva su cadencia y el máximo error horizontal de apoyo de **0,0549 u** medido en el clip. El ajuste de recuperación en los ataques conserva los apoyos verificados.

## Textura y UV

El atlas y ambas capas son **512 × 512 px**, con **256 × 256 UV lógicas**. Se verificaron las **1.890 caras** a **2 texeles/u**, sin solapes involuntarios. El área reservada es **54,59 %** y el verde luminoso con todos los cristales y fisuras alcanza **0,55 %** de la superficie UV opaca. Las tres imágenes tienen alfa binario.

## Animaciones

| Clip | Aviso (ticks) | Impacto | Diferencia |
| --- | ---: | ---: | ---: |
| `rake` | 16 | 0,80 s | 0,00 s |
| `mist_ring` | 28 | 1,40 s | 0,00 s |
| `soul_lash` | 24 | 1,20 s | 0,00 s |
| `wail` | 34 | 1,70 s | 0,00 s |
| `harvest` | 40 | 2,00 s | 0,00 s |

Se validaron **13 clips** y **4.022 claves** contra la exportación de Blockbench. `hurt` sigue aislado en `shock` y `death` conserva el último fotograma. La mano aparece antes del cráneo en `spawn`.

El Zarpazo mantiene **63,974 u de radio** y **+54,328°** a **0,80 s**, medidos en Blockbench. Su proyección frontal máxima en ese fotograma es **37,687 u**. Se toma el radio como magnitud operativa para comparar con el sector de **120°** y radio **72 u** del código. La estela todavía debe cubrir los bordes y las **8,026 u** restantes en fase I.

## Build y estado de integración

El wrapper de **Gradle 8.14.3** ejecutó `build` con **Java 21**, en modo offline, y terminó con **BUILD SUCCESSFUL**. Se registraron **925 pruebas**, **0 fallos** y **0 omitidas**. Los **5 casos** de `GreatOssuaryResourcesTest` pasaron con los recursos V85. `gen_boss_models.py --check` comprobó los cuatro jefes y la ruta de generación conserva los recursos de Blockbench.

La exportación se preparó en un worktree basado en la referencia local que incluye el Osario V84. Los resultados de rendimiento P1 y P3 de la Fase 6 permanecen abiertos. El banco anterior quedó en **86,27 %** con un jefe frente al Coloso y **29,67 %** con tres jefes frente al escenario vacío. La comparación se repetirá cuando el renderizador aplique la visibilidad correcta y una pasada emisiva limitada a las caras activas.

El cliente existente aún debe conectar las capas, la visibilidad de núcleos y brazos, la postura y los clips exclusivos al estado de juego. Al cargar estos recursos con el renderizador actual, las ramas exportadas de fase III pueden dibujarse desde el comienzo. La salida del build valida el paquete y el contrato de recursos. La activación por fase y la aceptación en partida siguen pendientes.
