# V84 — Fase 2: geometría anatómica

Estado a 2026-11-18: **Fase 2 aprobada por el autor al autorizar la Fase 3**.
Autorización recibida: «Lo apruebo ahora avanza con la fase 2».
La aprobación posterior fue «Autorizo y avanza con la fase 3».
Se sustituyeron los 74 volúmenes de bloqueo por **169 cubos**, conservando los **30 huesos, sus UUID,
pivotes, rotaciones de reposo y jerarquía**. En esta entrega aún no se habían iniciado pintura ni animaciones.

## Archivos y preservación

- [Editable activo, continuado en fases posteriores](../tools/models/blockbench/osario_quebrado.bbmodel).
- [Copia de esta entrega](../tools/models/blockbench/studies/osario_v84/fase2/osario_geometria_fase2.bbmodel).
- [Fase 1 aprobada, conservada como bytes](../tools/models/blockbench/studies/osario_v84/fase1/osario_quebrado_aprobado.bbmodel).
- [Manifiesto de las 169 piezas](../tools/models/blockbench/studies/osario_v84/fase2/geometry_manifest.json):
  nombre, hueso, posición, dimensiones, rotación, material previsto y función de cada cubo.
- [Comprobaciones](../tools/models/blockbench/studies/osario_v84/fase2/asset_checks.json),
  [medidas](../tools/models/blockbench/studies/osario_v84/fase2/measurements.json) y
  [validación de contactos](../tools/models/blockbench/studies/osario_v84/fase2/validation.json).

Worktree: `C:/Users/Johan/Desktop/Gits/MOD/osario-quebrado-v84`; rama `codex/osario-quebrado-v84`;
base `20bc23030ff90660b2c50ac9e50e707a3f462ca9`. Los cambios de V84 siguen sin commit.
No se hicieron push, merge ni PR. Los respaldos históricos y el proyecto anterior de calibración se conservaron.

El juego sigue usando sus recursos anteriores. No se modificaron Java, dependencias, atributos, registros,
otros mobs ni generadores. Se mantiene el entorno Fabric 1.21.1 / Java 21 / GeckoLib 4.7.7.

## Construcción y presupuesto

| Región | Plan base | Entrega | Motivo de la diferencia |
|---|---:|---:|---|
| Cráneo y mandíbula | 22 | **27** | Casquete y ceja divididos para abrir la fractura; mandíbula en U en lugar de un bloque macizo |
| Torso, pelvis, cuello y núcleo | 48 | **49** | Arco cervicotorácico que conecta dorsal, cuello y costillas superiores |
| Brazo fino y mano | 13 | **18** | Clavícula medial y dos falanges por dedo, incluido el pulgar independiente |
| Masa funeraria y secundarios | 42 | **55** | Estratos de tierra separados, base irregular y dos extremos anatómicos del fémur incrustado |
| Piernas y pies | 20 | **20** | Anatomía de referencia, con fémur izquierdo 0,5 u más corto |
| **Total** | **145** | **169** | **+24**, dentro de la referencia 145–170 y del margen de +25 |

Cada adición tiene una función registrada. No quedan cubos `F1_*`, mallas libres ni huesos sin función;
`root` es el único hueso sin geometría.

- **Cabeza:** placas frontal y posterior, cuencas hundidas 1 u, puente nasal, pómulos, fractura superior
  abierta y diez dientes de alturas distintas, incluyendo un diente superior roto. Las barras orbitales
  se estrecharon para conservar cuencas legibles; las cotas del dossier son referencias conceptuales.
- **Mandíbula:** mentón, dos cuerpos laterales y dos ramas. El hueco central es geométrico y gira sobre
  el pivote aprobado. Se corrigieron los cruces entre dientes superiores e inferiores en reposo.
- **Torso:** seis pares de costillas, cada una con arco lateral y retorno frontal. Segunda izquierda
  acortada, quinta derecha fracturada; alturas, giros y longitudes diferentes. Vértebras lumbares y dorsales,
  disco, espolón, dos cervicales, esternón de tres piezas y pelvis asimétrica fracturada.
- **Extremidades:** radio y cúbito, codo y carpos separados; cuatro dedos con falanges curvadas y pulgar
  oponible. Fémures, trocánteres, tibias, peronés, rótulas, metatarsos y tres falanges por pie.
- **Masa funeraria:** anclaje de hombro y cuatro segmentos, con estratos inclinados, raíces, costillas
  ajenas, fémur y vértebras incrustados. La base usa varios apoyos escalonados; conserva cinco raíces
  colgantes y seis fragmentos secundarios. El fragmento óseo inferior carece de rasgos de cráneo.
- **Núcleo:** una cruz de cinco cubos bajo `chest_core`. Se centró la geometría respecto al pivote de escala
  y se abrió la cavidad costal para admitir el pulso de combate. No se añadieron otros núcleos.

## Medidas y comprobaciones

| Criterio | Resultado |
|---|---|
| Geometría | **169 cubos / 1014 caras nominales**, todos exportables |
| Huesos | **30**, mismos nombres, padres, pivotes y rotaciones que la Fase 1 aprobada |
| Altura posada | **29,783 u**; objetivo 28–30 u |
| Anchura visual | **21,739 u** |
| Profundidad visual | **20,935 u** |
| Distancia entre hombros | **12,000 u** |
| Suelas izquierda y derecha | **y = 0** |
| Base funeraria | **y = 0**, sin hundimiento |
| Dimensiones de los cubos | Positivas y múltiplos de **0,5 u** |
| Texturas / clips | **0 / 0** |
| UV de diagnóstico | 128² lógico; futuro PNG 256² a 2 px/u; islas propias sin solapes |
| Cubos fraccionarios | **120**, con **720 caras UV explícitas** no degeneradas |
| Empaquetado provisional | Hasta la fila lógica **63,5**, con margen de 0,5 u UV = 1 píxel físico |
| Partes separadas, detector MCP | **0**, tolerancia 0,05 u |
| Comparación del geo reabierto | **169 piezas**, error máximo mundial **0,00000646 u** |
| Núcleo a S = 1 / 1,12 / 1,35 | Sin penetración externa mayor de **0,01 u** en las tres pruebas estáticas |

El geo `osario_geometria_diagnostico.geo.json` se exportó únicamente a la carpeta de estudio. Se reabrió
en una pestaña independiente, se compararon las cajas mundiales de las 169 piezas y se cerró esa pestaña.
La comparación mide la geometría real, excluyendo contornos invisibles del editor cuyos límites pueden
conservar cachés anteriores. Evidencia: [roundtrip_checks.json](../tools/models/blockbench/studies/osario_v84/fase2/roundtrip_checks.json).

Las pruebas de escala no crean clips: se aplicó temporalmente 1, 1,12 y 1,35 a la transformación del núcleo,
se comprobaron las cajas orientadas y se restauró la escala inicial. La primera comprobación reveló falta
de holgura; se centró la cruz, se adelantaron el esternón y varios retornos costales y se ajustaron los arcos.
El número de cubos, los pivotes y la envolvente exterior del modelo permanecieron estables.
Evidencia: [core_scale_checks.json](../tools/models/blockbench/studies/osario_v84/fase2/core_scale_checks.json).

El validador lista **114 encuentros entre huesos**: inserciones articulares, costillas unidas al esternón
y columna, ramas mandibulares, transición de los segmentos de tierra y piezas incrustadas. Se conservan
en `validation.json`; no se interpreta el indicador genérico `ok` como ausencia de solapes.
Las ramas mandibulares penetran en el casquete en sus articulaciones, y las cervicales entran bajo el cráneo.
El núcleo, los dientes opuestos y el pulgar respecto al índice quedaron despejados en reposo.

La comprobación independiente mediante SAT en 15 ejes encuentra **164 piezas estructurales conectadas**
a tolerancia 0,1 u. Los cinco cubos de energía forman sus propios conjuntos (4 + 1); la esquirla libre es
intencional. No son componentes esqueléticos desprendidos.
Evidencia: [obb_connectivity.json](../tools/models/blockbench/studies/osario_v84/fase2/obb_connectivity.json).

## Capturas

| Conjunto | Vistas |
|---|---|
| General | [Frente](../tools/models/blockbench/studies/osario_v84/fase2/frente.png), [perfil de la masa](../tools/models/blockbench/studies/osario_v84/fase2/perfil_masa.png), [perfil del brazo](../tools/models/blockbench/studies/osario_v84/fase2/perfil_brazo.png), [espalda](../tools/models/blockbench/studies/osario_v84/fase2/espalda.png), [tres cuartos](../tools/models/blockbench/studies/osario_v84/fase2/tres_cuartos.png) |
| Geometría | [Sin colores regionales](../tools/models/blockbench/studies/osario_v84/fase2/volumen_sin_colores.png), [estructura](../tools/models/blockbench/studies/osario_v84/fase2/estructura.png) |
| Regiones aisladas | [Cráneo y mandíbula](../tools/models/blockbench/studies/osario_v84/fase2/detalle_craneo.png), [tórax](../tools/models/blockbench/studies/osario_v84/fase2/detalle_torax.png), [mano](../tools/models/blockbench/studies/osario_v84/fase2/detalle_mano.png), [masa funeraria](../tools/models/blockbench/studies/osario_v84/fase2/detalle_masa.png) |

Las once imágenes son renders de Blockbench. Gris, ocre, rojo y verdes son marcadores de región del editor;
no representan todavía la paleta artística, textura ni emisión. Se restauró la visibilidad después de aislar
regiones. Los dos pequeños parches de musgo son localizados; su cobertura texturizada se comprobará en Fase 3.

## Puerta 2 y trabajo pendiente

El autor aprobó la anatomía, la silueta, la asimetría y el presupuesto al autorizar la Fase 3.
La copia de Fase 1 conserva su SHA-256; la entrega actual también tiene copia y hashes en `asset_checks.json`.
Las operaciones de autoría se registran en los siete archivos JavaScript de `fase2/`, numerados del 01 al 07.
Se ejecutaron mediante Blockbench MCP; no se añadieron al generador de recursos del juego.

No se ejecutó Gradle en Fase 2: no hay cambios en código ni recursos activos. La compilación de referencia
de Fase 0 está documentada por separado; no acredita que este modelo haya sido integrado o probado en partida.
El `--check` binario conocido de los PNG anteriores tampoco se ha corregido fuera de alcance.

La entrega de **Fase 3 autorizada** está en [V84_OSARIO_FASE3.md](V84_OSARIO_FASE3.md): atlas artístico 256²,
pintura y glowmask independiente. Allí se documenta una oclusión visual del núcleo detectada al revisar
los materiales; no se ha modificado esta instantánea geométrica aprobada. Después seguirán las diez animaciones, los cuatro ataques, IA, red, compatibilidad con
élites/campeones y las mediciones en partida de sus fases correspondientes.
La holgura estática del núcleo no sustituye a la validación de contactos durante todas las animaciones.

No se ha asignado una nota final ni se ha declarado superada la calidad de los otros mobs: la comparación
completa incluye materiales y movimiento. No hay cambios de balance.

Commit propuesto, sin ejecutar: `feat: detalla la geometría del Osario Quebrado V84`.

## Recuento por hueso

| Hueso | Padre | Cubos |
|---|---|---:|
| `root` | — | 0 |
| `pelvis` | `root` | 6 |
| `body` | `pelvis` | 3 |
| `spine_upper` | `body` | 5 |
| `chest` | `spine_upper` | 4 |
| `ribs_left` | `chest` | 12 |
| `ribs_right` | `chest` | 12 |
| `chest_core` | `chest` | 5 |
| `neck` | `chest` | 2 |
| `head` | `neck` | 17 |
| `jaw` | `head` | 10 |
| `bone_shoulder` | `chest` | 3 |
| `bone_upper_arm` | `bone_shoulder` | 2 |
| `bone_forearm` | `bone_upper_arm` | 3 |
| `bone_hand` | `bone_forearm` | 2 |
| `bone_fingers` | `bone_hand` | 6 |
| `bone_thumb` | `bone_hand` | 2 |
| `mass_shoulder` | `chest` | 6 |
| `dragging_upper` | `mass_shoulder` | 10 |
| `dragging_middle` | `dragging_upper` | 9 |
| `dragging_lower` | `dragging_middle` | 9 |
| `dragging_end` | `dragging_lower` | 10 |
| `debris_roots` | `dragging_middle` | 5 |
| `debris_chips` | `dragging_lower` | 6 |
| `thigh_left` | `pelvis` | 2 |
| `shin_left` | `thigh_left` | 3 |
| `foot_left` | `shin_left` | 5 |
| `thigh_right` | `pelvis` | 2 |
| `shin_right` | `thigh_right` | 3 |
| `foot_right` | `shin_right` | 5 |
| **Total** | **30 huesos** | **169** |
