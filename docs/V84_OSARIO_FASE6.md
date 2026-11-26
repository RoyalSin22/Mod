# V84 — Fase 6. Validación del Osario Quebrado

El autor aprobó la Puerta Final y autorizó integrar V84 en `main` el 18 de noviembre de 2026.
Confirmó haber completado las comprobaciones de rendimiento sin límite de FPS, latencia de 100 y 200 ms
y aviso visual del zarpazo. El repositorio conserva las mediciones del banco local. La confirmación del
autor cubre las comprobaciones manuales y no aporta cifras adicionales a las tablas.

Rama de trabajo `codex/osario-quebrado-v84`, base inicial `20bc23030ff90660b2c50ac9e50e707a3f462ca9`.
Minecraft 1.21.1, Java 21, Fabric Loader 0.16.14 y GeckoLib 4.7.7. El editable, el atlas,
la máscara, las animaciones y las reglas de juego corresponden a la entrega integrada de
[Fase 5](V84_OSARIO_FASE5.md).

## Combate en servidor

Fabric inició Minecraft y ejecutó catorce escenarios de combate más una entrada inactiva del banco
de rendimiento. Los catorce escenarios pasaron. Los jugadores son `ServerPlayerEntity` conectados al
gestor de jugadores mediante canales locales de prueba. Cada jugador juega en supervivencia y
entra en el escenario tras los 70 ticks de espera del banco. La dificultad es normal y se desactivan
Amenaza y las tiradas naturales de élite en memoria. En una ejecución diagnóstica previa, el
anillo V del mundo de pruebas multiplicó el daño por 1,75, tal como prevé el sistema de Amenaza.

| Caso | Resultado observado |
|---|---|
| Alerta externa | Activó la alerta y el estado de persecución al recibir objetivo |
| Zarpazo | Dos jugadores frontales perdieron 4,5 de vida cada uno. El jugador a la espalda quedó intacto |
| Barrido | El ejemplar a escala 1,35 impactó al jugador expuesto por 6,5. La pared protegió al otro jugador |
| Impacto | Perdió 8 de vida el jugador dentro del disco desplazado. Quedaron intactos los objetivos por encima de la banda y fuera del radio |
| Embestida | Pasos de hasta 0,55 bloques en el escenario medido. El primer jugador perdió 5 de vida y el del fondo quedó intacto |
| Pared, desnivel y agua | Tres escenarios independientes. El obstáculo apareció después de iniciar la acción y el mob frenó antes de cruzarlo |
| Objetivo no elegible | Cuatro escenarios. El aviso se canceló al pasar a espectador, morir, desconectarse o cambiar de dimensión. El segundo jugador conservó toda su vida |
| Campeón de la Cripta | La fábrica real creó un Osario campeón. El impacto pudo iniciarse y el jugador recibió daño dentro de la ventana |
| Presión colectiva | Tres fichas concedidas. La cuarta se rechazó y pudo usarse al morir un ocupante |

Los ataques concretos se seleccionan por reflexión en el banco. El servidor ejecuta sus ventanas,
su movimiento, la colisión y el daño reales. Las reglas puras cubren pesos y deduplicación en JUnit.
Los canales locales permiten combinar entidades de jugador en el servidor. La reproducción en
clientes remotos y los retardos de 100 y 200 ms quedaron a cargo de la verificación manual del autor.

[Registro XML de combate](../tools/models/blockbench/studies/osario_v84/fase6/combate.xml).

## Carga del servidor

Tres procesos separados en el mismo equipo (Core i5-10400, Java 21, `-Xms1G -Xmx2G`). Mundo plano
de GameTest con semilla 0, arena cerrada y chunks activos, dificultad normal y aparición natural
desactivada. Se simularon 1200 ticks de calentamiento y 2400 ticks de muestra por caso. Se leen
los tiempos del servidor en cada tick. Todas las criaturas recibieron los 2399 intervalos esperados
entre el primer y el último tick medidos. En la primera serie con jugador, el banco mantuvo
un objetivo invulnerable a 15 bloques de la referencia. El objetivo seguía asignado en el
100 % de las observaciones, pero Minecraft excluía al jugador de los objetivos que podían
recibir daño. Esas filas se conservan como diagnóstico. Para medir combate se sustituyó
la invulnerabilidad por reposición de vida y se exigió daño durante cada muestra.

| Escena | Grupo | Placeholder, ms/t | V84, ms/t | Errante, ms/t | V84 vs. placeholder | V84 − Errante, ms/t |
|---|---:|---:|---:|---:|---:|---:|
| Sin jugador | 1 | 0,5607 | 0,5667 | 0,5677 | +1,07 % | −0,0010 |
| Sin jugador | 5 | 0,5823 | 0,5975 | 0,8275 | +2,61 % | −0,2300 |
| Sin jugador | 10 | 0,5725 | 0,5768 | 0,5555 | +0,75 % | +0,0213 |
| Sin jugador | 20 | 0,6958 | 0,6434 | 0,6247 | −7,53 % | +0,0187 |
| Sin jugador | 30 | 0,6944 | 0,6654 | 0,7523 | −4,18 % | −0,0869 |
| Con objetivo | 1 | 0,3751 | 0,3778 | 0,3910 | +0,72 % | −0,0132 |
| Con objetivo | 5 | 0,3695 | 0,3953 | 0,3704 | +6,98 % | +0,0249 |
| Con objetivo | 10 | 0,3616 | 0,3955 | 0,3729 | +9,38 % | +0,0226 |
| Con objetivo | 20 | 0,4558 | 0,4236 | 0,4615 | −7,06 % | −0,0379 |
| Con objetivo | 30 | 0,6237 | 0,4858 | 0,4947 | −22,11 % | −0,0089 |

La diferencia sin jugador observada a 30 está dentro del margen de +0,5 ms/t frente al Errante.
Se usó el modo acelerado de Minecraft. Las muestras duraron entre 0,91 y 2,05 s de reloj real.
GameTest cambia la ubicación de la arena entre arranques. Se necesita la pasada de 120 s de
reloj real en la misma partida para cerrar el criterio formal.

Los CSV conservan percentiles, heap utilizado al final de cada caso y recuentos de ticks por mob.
[Placeholder](../tools/models/blockbench/studies/osario_v84/fase6/mspt-placeholder.csv) ·
[V84](../tools/models/blockbench/studies/osario_v84/fase6/mspt-v84.csv) ·
[Errante](../tools/models/blockbench/studies/osario_v84/fase6/mspt-errante.csv).

Una segunda serie suprimió además la tirada de élite. Con 30 mobs sin jugador, el placeholder,
V84 y el Errante dieron respectivamente **1,513**, **1,203** y **2,178 ms/t**. Con objetivo dieron
**1,341**, **1,904** y **1,260 ms/t** en el diagnóstico aún con objetivo invulnerable.
Los valores variaron entre procesos. Las tablas de ambas series quedan
conservadas para distinguir el comportamiento repetido de una observación aislada.
[Placeholder de control](../tools/models/blockbench/studies/osario_v84/fase6/mspt-control-placeholder.csv) ·
[V84 de control](../tools/models/blockbench/studies/osario_v84/fase6/mspt-control-v84.csv) ·
[Errante de control](../tools/models/blockbench/studies/osario_v84/fase6/mspt-control-errante.csv).

En la tercera serie se alternaron 30 Osarios y 30 Errantes en la misma arena y JVM, en tres
pares por escena, con 600 ticks de calentamiento y 1200 de muestra por grupo. El jugador en
supervivencia recibió daño y recuperó la vida mediante el banco. En los tres grupos de V84 se
registraron 103, 122 y 109 ticks con daño. En los del Errante se registraron 114, 115 y 115.

| Escena | Mediana V84, ms/t | Mediana Errante, ms/t | Diferencias V84 − Errante por par, ms/t |
|---|---:|---:|---|
| Sin jugador | 2,359 | 2,626 | +0,954 / −0,392 / −0,280 |
| Con objetivo que recibe daño | 2,848 | 2,751 | +0,358 / +0,040 / +0,315 |

En persecución, los tres pares quedaron dentro del margen de +0,5 ms/t. El primer par sin
jugador superó el margen. La serie usó ticks acelerados y sirve de comprobación reproducible
de carga, todavía pendiente de la pasada nominal a 20 TPS.
[Mediciones pareadas](../tools/models/blockbench/studies/osario_v84/fase6/mspt-pareado.csv).

## Render en Minecraft

Se capturaron un frontal diurno y nocturno, tres cuartos, lateral y posterior a 8 bloques con
partículas mínimas. De día se distinguen el esqueleto, su inclinación y el arrastre de la masa.
El núcleo tiene menos presencia que en el arte maestro, sobre todo al anochecer. La comparativa
retira la capa antes de que GeckoLib extraiga los texeles emisivos del atlas, así que en ambas
vistas el material del núcleo conserva su color base.

En la región del tórax de la captura nocturna de frente (x 615–674, y 315–354), los píxeles con
verde ≥ 1,2 × rojo y ≥ 1,1 × azul alcanzaron G = 118 con la capa y G = 30 al retirarla. Cinco
píxeles cumplieron además G ≥ 100 con la capa y cero al retirarla. La emisión se comprobó, y
el criterio de igual legibilidad sin emisión en oscuridad queda abierto.
[Frente diurno](../tools/models/blockbench/studies/osario_v84/fase6/frente-8m.png) ·
[Frente nocturno](../tools/models/blockbench/studies/osario_v84/fase6/frente-8m-noche.png) ·
[Frente nocturno sin emisión](../tools/models/blockbench/studies/osario_v84/fase6/frente-8m-noche-sin-emision.png) ·
[Tres cuartos](../tools/models/blockbench/studies/osario_v84/fase6/tres-cuartos-8m.png) ·
[Lateral](../tools/models/blockbench/studies/osario_v84/fase6/lateral-8m.png) ·
[Posterior](../tools/models/blockbench/studies/osario_v84/fase6/posterior-8m.png).

### Acciones en el cliente

El cliente integrado recibió el sello de cada acción y guardó 22 fotogramas en total. Los nombres
contienen el tick observado desde el sello. Se ve el contacto de la masa con el suelo, el movimiento
de la embestida y el colapso sin vuelco lateral en la toma de muerte a t18. Las capturas documentan
el resultado de GeckoLib en ejecución. El GameTest compara la vida antes y después de cada ventana.

| Acción | Ticks capturados | Fotograma |
|---|---|---|
| Zarpazo | 4, 8, 10, 14 | [t10](../tools/models/blockbench/studies/osario_v84/fase6/clips/osario-claw-t10.png) |
| Barrido | 8, 16, 19, 23 | [t19](../tools/models/blockbench/studies/osario_v84/fase6/clips/osario-heavy-t19.png) |
| Impacto | 10, 18, 23, 29 | [t23](../tools/models/blockbench/studies/osario_v84/fase6/clips/osario-slam-t23.png) |
| Embestida | 4, 12, 14, 18, 24 | [t18](../tools/models/blockbench/studies/osario_v84/fase6/clips/osario-lunge-t18.png) |
| Muerte | 2, 6, 9, 14, 18 | [t18](../tools/models/blockbench/studies/osario_v84/fase6/clips/osario-death-t18.png) |

Entre el aviso sonoro y la primera ventana de impacto transcurren 0,30 s en zarpazo, 0,55 s en
barrido, 0,65 s en impacto y 0,50 s en embestida. La acción de zarpazo empieza 0,50 s antes de su
primer tick activo. Falta evaluar en vídeo continuo a partir de qué fotograma se reconoce el aviso
visual, en especial para el zarpazo. El cliente local acredita recepción de los estados de acción.
La latencia de 100 y 200 ms se confirmó mediante la prueba manual del autor.

### Primera pasada de FPS

Cliente con servidor integrado, mundo aislado plano de semilla 0, cámara fija a 12 bloques en
tres cuartos, render 12 y simulación 10, framebuffer 1280 × 720, VSync desactivado por el banco,
entidades con IA desactivada. Calentamiento de 6 s y muestra de 10 s. Las tiradas de élite
seguían habilitadas en esta primera pasada. El resultado es exploratorio.

| Grupo | Placeholder | V84 | Errante | V84 sin emisión | V84 partículas mínimas | V84 vs. placeholder |
|---:|---:|---:|---:|---:|---:|---:|
| 1 | 643,8 | 501,4 | 705,0 | 599,1 | 503,3 | −22,1 % |
| 5 | 658,7 | 269,7 | 569,7 | 464,7 | 271,8 | −59,1 % |
| 10 | 626,6 | 165,9 | 350,8 | 281,1 | 171,2 | −73,5 % |
| 20 | 592,8 | 96,4 | 232,6 | 178,5 | 98,0 | −83,7 % |
| 30 | 572,9 | 68,7 | 172,4 | 131,0 | 70,0 | −88,0 % |

Las cifras son FPS medios. A 30, la pérdida inicial frente al Errante fue del **60,2 %**. Los
percentiles del tiempo por fotograma de V84 fueron 19,76 ms (p99) y 25,68 ms (p99,9), frente a
10,45 y 16,01 ms del Errante. Al retirar la capa emisiva en el banco, el tiempo medio bajó de
14,56 a 7,63 ms. Los modos de partículas se compararon en reposo. La simulación de estelas de
combate requiere su propia pasada. El atlas pesa 18 476 B y la máscara 2628 B, frente a 302 B del
placeholder. La geometría tiene 1014 quads nominales frente a 48 del placeholder.

[CSV placeholder](../tools/models/blockbench/studies/osario_v84/fase6/fps-inicial-placeholder.csv) ·
[CSV V84](../tools/models/blockbench/studies/osario_v84/fase6/fps-inicial-v84.csv) ·
[CSV Errante](../tools/models/blockbench/studies/osario_v84/fase6/fps-inicial-errante.csv) ·
[CSV sin emisión](../tools/models/blockbench/studies/osario_v84/fase6/fps-inicial-sin-emision.csv) ·
[CSV partículas mínimas](../tools/models/blockbench/studies/osario_v84/fase6/fps-inicial-particulas-minimas.csv).

### Ajuste del renderizador

`OsarioRenderer` conserva las matrices fijas de cada cubo en coordenadas de su hueso. El cliente sigue
aplicando la matriz animada, luz, UV, color y escala de la entidad al emitir los vértices. El caché se
reconstruye al cambiar el modelo horneado. La segunda pasada conserva la cadena de transformaciones del
núcleo y emite cinco cubos, 30 quads. En el render normal del mundo se agrupa la emisión de los Osarios
visibles en un lote. La biblioteca conserva su propia máscara y mantiene el camino individual para
previsualizaciones, contornos e invisibilidad. El criterio técnico de 169 cubos y los datos del rig se conservan.
La captura posterior confirma que los píxeles verdes siguen visibles.
[Detalle tras el ajuste](../tools/models/blockbench/studies/osario_v84/fase6/nucleo-render-optimizado.png).

Con las tiradas de élite desactivadas, 30 Osarios dieron **53,77 FPS** y 30 Errantes **59,00 FPS**
en muestras de 20 s con 15 s de calentamiento (−8,9 %). En una repetición de 12 s, el Osario dio
52,65 y el Errante 49,85 FPS (+5,6 %), con mayor irregularidad de frames en el Errante.
La mediana de control del Errante fue 16,61 ms y con un solo Osario fue 16,64 ms. Se observó
una cadencia de 60 FPS incluso con un solo mob en primer plano y VSync apagado en Minecraft.
Estas pasadas corresponden al banco local. El autor confirmó por separado la comprobación
sin límite de FPS de la Puerta Final. La GPU identificada por OpenGL fue la NVIDIA GeForce GTX 1050 Ti.

[V84 ajustado](../tools/models/blockbench/studies/osario_v84/fase6/fps-agrupado-30.csv) ·
[Errante de control](../tools/models/blockbench/studies/osario_v84/fase6/fps-control-errante-30.csv) ·
[Control con un Osario](../tools/models/blockbench/studies/osario_v84/fase6/fps-control-1.csv) ·
[Repetición V84](../tools/models/blockbench/studies/osario_v84/fase6/fps-repeticion-v84-30.csv) ·
[Repetición Errante](../tools/models/blockbench/studies/osario_v84/fase6/fps-repeticion-errante-30.csv).

## Audio y empaquetado

OpenAL cargó los diez eventos y Minecraft los marcó como activos con volumen maestro al 50 % al reproducirlos
individualmente. La prueba mide reproducción mediante el motor. La mezcla junto a otras entidades y el
balance percibido quedan por evaluar.
[Registro de reproducción](../tools/models/blockbench/studies/osario_v84/fase6/audio-50.txt).

`gradlew.bat build --offline --console=plain` volvió a pasar con **924 pruebas JUnit en 153 clases**,
sin fallos ni omisiones. El JAR remapeado contiene el renderizador y los diez OGG. El mod de laboratorio
queda fuera del JAR. Pasaron las 59 pruebas de idiomas, los siete generadores en modo `--check`,
`check_lang.py`, la comprobación de modelos de mobs y `git diff --check`.
[Compilación local](../build/libs/aptitudes-3.8.8.jar).

## Evaluación final

Las notas artísticas valoran el resultado observable en el editor y las capturas del cliente. La comprobación
frente a otros mobs, cuando existe, se indica en la evidencia.

| Criterio | Nota / 10 | Evidencia y límite |
|---|---:|---|
| Fidelidad al arte | 8 | Cráneo fracturado, masa unilateral, tórax abierto y núcleo. El núcleo destaca menos que en la Hoja 01 |
| Anatomía | 9 | Mandíbula, caja torácica, radio y cúbito, dedos y piernas diferenciadas |
| Geometría | 9 | 169 cubos con silueta propia, contactos de articulación e incrustaciones |
| Silueta | 8 | Esqueleto y masa distinguibles a 8 bloques de día. Menor contraste nocturno |
| Jerarquía | 9 | 30 huesos exportados y cargados por GeckoLib, incluida la cadena de la masa |
| Pivotes | 9 | 2042 poses medidas en Blockbench y apoyos dentro de la tolerancia del estudio |
| Textura | 8 | Atlas de 25 colores con cinco capas de autoría, hueso gastado y tierra diferenciada |
| UV | 10 | 1014 caras a 2 px/u, con márgenes y sin solapes en la comprobación |
| Emisión | 6 | Emite en oscuridad, con área visible reducida. La lectura nocturna cae al retirar la capa |
| Animación | 8 | Diez clips en el editor, cierre de los tres bucles y cinco acciones capturadas en el cliente |
| Fluidez | 8 | Interpolación y contactos medidos en el editor. Falta comprobar las transiciones sobre terreno irregular |
| Peso físico | 8 | Inercia escalonada en la masa, apoyos en el editor y embestida continua en servidor |
| Salvajismo | 8 | Postura caída, asimetría y gancho óseo durante el barrido |
| Dinamismo | 8 | Arcos distintos de zarpazo, barrido, impacto y embestida en el cliente |
| Legibilidad ofensiva | No verificado | Avisos y ventanas medidos en ticks. Falta certificar al menos 0,5 s de aviso visual de cada ataque |
| Detección de impactos | 9 | Daño y volumen observados en Minecraft, incluidas pared, banda vertical y primer contacto |
| Navegación | 8 | Adquisición de objetivo y frenado ante tres obstáculos. Recorridos con desniveles complejos pendientes |
| IA | 8 | Alerta externa, cancelación, reglas de selección y presión comprobadas. Falta la pasada completa de tácticas V83 y Niebla |
| Rendimiento | No verificado | MSPT acelerado y FPS exploratorios disponibles. Falta certificación de la matriz en condiciones nominales |
| Multijugador | No verificado | Varios jugadores del servidor en el banco. Quedan por medir clientes remotos y 100/200 ms |

Las referencias geométricas, UV y de animación están en [Fase 2](V84_OSARIO_FASE2.md),
[Fase 3](V84_OSARIO_FASE3.md) y [Fase 4](V84_OSARIO_FASE4.md).
Las marcas «No verificado» de la tabla indican los límites del banco local. El arranque además muestra incidencias preexistentes ajenas al Osario
en el avance `summon_eclipse` y en los modelos de `sunset_astrolabe` y `resonador_del_umbral`.

## Cómo reproducir

Desde el worktree, con Java 21 y la caché de Gradle del proyecto:

```powershell
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle runOsarioQa --offline --console=plain
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle -PosarioQaSuite=bench_sprint_v84 -PosarioQaWarm=1200 -PosarioQaSamples=2400 runOsarioQa --offline --console=plain
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle -PosarioQaSuite=bench_sprint_live_paired -PosarioQaWarm=600 -PosarioQaSamples=1200 runOsarioQa --offline --console=plain
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle -PosarioQaRender=visual runOsarioRenderQa --offline --console=plain
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle -PosarioQaRender=audio runOsarioRenderQa --offline --console=plain
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle -PosarioQaRender=clips runOsarioRenderQa --offline --console=plain
.\gradlew.bat -I tools/testing/osario_v84/runtime.init.gradle -PosarioQaRender=bench-v84 -PosarioQaClientCount=30 runOsarioRenderQa --offline --console=plain
```

El script de inicialización añade el mod de pruebas solo a esa invocación de Gradle. Los mundos,
XML, CSV, registros y capturas se crean bajo `build/osario-v84/`. Para comparar el placeholder
se usa una exportación local de la revisión base y se pasa a su Gradle el mismo script con
`-PosarioQaRoot=<ruta absoluta a tools/testing/osario_v84>`.
