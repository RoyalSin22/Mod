# V84 — Fase 3: UV, materiales y emisión

**Puerta 3 aprobada.** El ajuste del tórax autorizado está aplicado y verificado. El informe conserva
la comparación anterior al ajuste como historial. [Resultado final](#ajuste-autorizado-y-cierre-de-puerta-3).
Las Fases 0–2 fueron aprobadas. La autorización para esta fase fue «Autorizo y avanza con la fase 3»,
reiterada mediante «Continúa con la fase 3». La siguiente entrega está en [Fase 4](V84_OSARIO_FASE4.md).

La textura y la máscara cumplen las comprobaciones de datos descritas abajo. La revisión visual revela
que el manubrio, el esternón y la mandíbula de la geometría anterior ocultaban demasiado el núcleo.
La propuesta virtual de las secciones iniciales precedió al ajuste que se documenta al final.

## Archivos y alcance

- [Editable activo](../tools/models/blockbench/osario_quebrado.bbmodel), con cinco capas nativas de pintura.
- [Instantánea de esta entrega](../tools/models/blockbench/studies/osario_v84/fase3/osario_texturizado_fase3.bbmodel).
- [Textura base 256²](../tools/models/blockbench/studies/osario_v84/fase3/osario_quebrado.png).
- [Máscara independiente 256²](../tools/models/blockbench/studies/osario_v84/fase3/osario_quebrado_glowmask.png).
- [Comprobaciones y SHA-256](../tools/models/blockbench/studies/osario_v84/fase3/asset_checks.json).

Worktree: `C:/Users/Johan/Desktop/Gits/MOD/osario-quebrado-v84`; rama `codex/osario-quebrado-v84`;
base `20bc23030ff90660b2c50ac9e50e707a3f462ca9`. Los cambios siguen sin commit, push, merge ni PR.
Las instantáneas aprobadas de Fase 1 y Fase 2 conservan sus bytes y hashes anteriores.

Se conservaron **169 cubos, 30 huesos, UUID, dimensiones, posiciones, rotaciones, pivotes y jerarquía**.
Hay **cero animaciones**. Las escrituras de autoría están en `tools/models/blockbench/` y la documentación.
No se exportó geometría de producción ni se escribieron recursos en `src/` durante esta fase.
El juego sigue usando el placeholder anterior; Java, atributos, dependencias y los demás mobs permanecen intactos.

## Atlas y UV

El PNG físico es de **256 × 256**, sobre una resolución lógica de **128 × 128**: **2 texeles por unidad**.
La rejilla geométrica de 0,5 u corresponde a un píxel físico, sin dimensiones que requieran medio píxel.
Hay una isla desplegada propia por cubo, sin compartir superficies entre lados o regiones.

| Banda | Filas físicas del atlas |
|---|---:|
| Hueso: cráneo y mandíbula | 1–28 |
| Hueso: torso, pelvis y cuello | 30–64 |
| Hueso: extremidades | 66–90 |
| Hueso incrustado en la masa | 92–108 |
| Tierra y barro | 110–164 |
| Raíces | 166–180 |
| Musgo geométrico | 182–188 |
| Ectoplasma | 190–198 |

Las **1014 caras** tienen densidad mínima, mediana y máxima de **2 px/u**. Se mantienen 49 cubos
con `box_uv` y 120 cubos fraccionarios con UV por cara. Ningún cubo usa `mirror_uv`; no hay islas
compartidas ni reutilización especular de pintura entre costillas, manos o piernas.
Los signos invertidos de las tapas de los 49 `box_uv` son la orientación normal del desplegado de Blockbench,
no una textura compartida entre piezas.

El margen entre desplegados es de **un píxel físico**. No se detectan solapes, caras degeneradas,
coordenadas físicas fraccionarias, píxeles transparentes dentro de una cara ni pintura fuera de las caras.
Las caras ocupan **17 232 píxeles**, el **26,294 %** del atlas. La medida `uv_area_used_pct = 76` del inspector
es la extensión rectangular del empaquetado; no representa la ocupación real de las caras.
El espacio restante conserva transparencia y permite ampliar el trabajo sin aumentar la resolución.

Evidencia: [inspección UV](../tools/models/blockbench/studies/osario_v84/fase3/uv_inspection.json),
[desplegados](../tools/models/blockbench/studies/osario_v84/fase3/uv_layout.json),
[registro de caras](../tools/models/blockbench/studies/osario_v84/fase3/caras_pintadas.json) y
[guía visual 256²](../tools/models/blockbench/studies/osario_v84/fase3/uv_islas_256.png).

## Pintura y materiales

La autoría se realizó mediante Blockbench MCP sobre capas de píxeles nativas. Las pinceladas discretas
se asignan por región, pieza y cara; las cavidades, fracturas y fibras se sitúan según la anatomía.
No se utiliza ruido aleatorio, fotografías, interpolación suave ni antialiasing en la textura.
Los archivos JavaScript del estudio registran las operaciones de autoría; no forman parte del generador del juego.

| Capa editable | Contenido | Píxeles con pintura |
|---|---|---:|
| 01 — Base de materiales | Masas cromáticas y orientación de las superficies | 17 070 |
| 02 — Porosidad y sedimentos | Desgaste, manchas, humedad, fibras y suciedad articular | 6813 |
| 03 — Grietas y cavidades | Cuencas, nariz, fracturas y oclusión junto a raíces | 972 |
| 04 — Musgo localizado | Parches sobre hombro, juntas y base | 107 |
| 05 — Ectoplasma | Núcleo y esquirlas de su único conjunto de energía | 162 |

Las capas se superponen: sus recuentos no se suman para calcular superficie del atlas.
La composición de las cinco capas coincide exactamente con el PNG de revisión y con la textura embebida del editable.

- **Hueso:** seis tonos de marfil, desgaste por agrupaciones, extremos articulares sucios, diferencias entre
  superficies externas e internas, dientes de tonos distintos, sutura superior y cavidades oscuras.
  Pelvis, tibia, rótula y fragmentos incrustados tienen fracturas localizadas. Los huesos enterrados reciben
  suciedad de contacto con la tierra; los pies tienen manchas diferentes en cada lado.
- **Tierra:** terrones de 2–4 px, bandas discontinuas de sedimento, variación seca/húmeda y cavidades junto
  a las raíces. Las superficies inferiores y cercanas al suelo son más oscuras.
- **Raíz:** fibras siguiendo el eje largo de cada pieza, nudos de 2 × 2 px y cortes oscuros. No se emplea
  una dirección de veta común para todos los cubos.
- **Musgo:** tres tonos en parches discretos, sin musgo en cabeza ni cuencas. Son **107 píxeles**;
  aproximadamente **1,063 % de la superficie expuesta muestreada de la masa**, frente al límite del 10 %.
  El cálculo descarta muestras enterradas en otros volúmenes y el contacto inferior con el suelo.
- **Ectoplasma:** cinco tonos, borde oscuro, cuerpo verde y seis píxeles de pico `#C9FFF0`. El verde luminoso
  ocupa **162 píxeles**, un **0,247 % del atlas** o **0,940 % de los píxeles pintados**, por debajo del 4 %.

La paleta utiliza exactamente los **25 colores** del contrato, sin colores adicionales:

| Material | HEX |
|---|---|
| Hueso | `#F0EEDD` · `#D9D6C3` · `#B7B5A6` · `#8F8B7C` · `#5F5B50` · `#3B372F` |
| Tierra | `#6B5642` · `#5A4838` · `#4A3B2E` · `#382C22` · `#2A211A` |
| Raíz | `#8A6B4F` · `#6B4E3A` · `#4D3828` · `#33261C` |
| Grietas | `#2F221F` · `#1A1210` |
| Musgo | `#5C7142` · `#4A5F35` · `#3B4D28` |
| Ectoplasma | `#C9FFF0` · `#7FFFD4` · `#4DE2A6` · `#2FB985` · `#1E8F6A` |

El contraste de luminancia entre `#B7B5A6` y `#3B372F` es **5,737:1**. Este valor compara los colores fuente;
no sustituye la evaluación con la iluminación del juego. El HEX escrito tiene prioridad sobre la saturación
de las ilustraciones conceptuales.

La búsqueda de caras con dibujos idénticos no encuentra duplicados entre caras de igual orientación de imagen
y dimensiones, con ancho y alto ≥ 4 px y área ≥ 24 px. Las caras minúsculas pueden compartir un color o una
agrupación inevitable; ese resultado no se presenta como prueba de singularidad absoluta de cada píxel.

## Emisión y sus límites

La máscara independiente contiene **84 píxeles**: **26 con alfa 255 y 58 con alfa 160**.
Los marcados tienen RGB blanco; los apagados son exactamente **RGBA 0,0,0,0**, sin RGB residual.
Todos pertenecen a caras del conjunto `chest_core`; no hay emisión en ojos, huesos, tierra ni musgo.
Su ocupación es **0,128 % del atlas**. El color procede de la base, no de la máscara.

La comparación ON/OFF usa copias temporales de las cinco superficies de energía con el shader del editor,
sin sombreado y con el alfa de la máscara. La escena oscura reduce la iluminación de la base a 0,22.
Las imágenes pareadas mantienen la misma cámara: difieren en 735 píxeles de pantalla, dentro del rectángulo
del núcleo `[582, 440, 614, 502]`; el resto del cuerpo permanece idéntico. No se añade iluminación ambiental,
resplandor de posprocesado ni geometría al editable. Todos los ayudantes se retiraron después de las capturas.

**Pendiente en Fase 5:** validar `AutoGlowingGeoLayer` con GeckoLib 4.7.7 en el juego. Su cargador retira
los píxeles emisivos de la base cargada en memoria. Desactivar la capa tras cargar la máscara no equivale
a volver a cargar una base intacta. Esta comparación de Blockbench acredita la máscara y su localización;
no acredita todavía el comportamiento de ese cambio de estado en partida.

## Capturas de la entrega real

| Revisión | Archivos |
|---|---|
| Vistas generales | [Frente](../tools/models/blockbench/studies/osario_v84/fase3/frente.png), [espalda](../tools/models/blockbench/studies/osario_v84/fase3/espalda.png), [perfil de la masa](../tools/models/blockbench/studies/osario_v84/fase3/perfil_masa.png), [perfil del brazo](../tools/models/blockbench/studies/osario_v84/fase3/perfil_brazo.png) |
| Tres cuartos | [Desde el brazo](../tools/models/blockbench/studies/osario_v84/fase3/tres_cuartos.png), [desde la masa](../tools/models/blockbench/studies/osario_v84/fase3/tres_cuartos_masa.png) |
| Emisión con luz ambiente | [OFF](../tools/models/blockbench/studies/osario_v84/fase3/emision_off.png), [ON](../tools/models/blockbench/studies/osario_v84/fase3/emision_on.png) |
| Emisión en oscuridad | [OFF](../tools/models/blockbench/studies/osario_v84/fase3/oscuro_emision_off.png), [ON](../tools/models/blockbench/studies/osario_v84/fase3/oscuro_emision_on.png) |
| Pintura | [Atlas nativo](../tools/models/blockbench/studies/osario_v84/fase3/osario_quebrado.png), [ampliación ×8 NEAREST](../tools/models/blockbench/studies/osario_v84/fase3/textura_x8.png), [paleta rotulada](../tools/models/blockbench/studies/osario_v84/fase3/paleta.png) |

Las vistas del modelo son renders de Blockbench, sin modificar las proporciones para asemejarlas artificialmente
al dossier. La ampliación de textura es exactamente de 2048², con cada píxel repetido en un bloque de 8 × 8.

## Observación artística y propuesta antes de cerrar la Puerta 3

**VERIFICADO:** el núcleo queda demasiado tapado en las vistas frontal y de tres cuartos. La ausencia de
intersecciones geométricas comprobada en Fase 2 no garantiza su visibilidad. El muestreo por rayos de las caras
del núcleo estima una exposición del **15,7 % frontal** y **29,7 % en tres cuartos**; los principales oclusores
son manubrio, esternón y mandíbula. Son estimaciones ponderadas por proyección, con posibles muestras
superpuestas del propio núcleo; no son porcentajes exactos de píxeles de pantalla.
Evidencia: [visibilidad actual](../tools/models/blockbench/studies/osario_v84/fase3/visibilidad_nucleo.json).

**PROPUESTA, no aplicada al modelo:** conservar el recuento y las articulaciones, estrechar el manubrio
de 2 a **1 u** y el cuerpo del esternón de 1,5 a **0,5 u**; desplazar el conjunto del núcleo **−0,5 u en Y**,
conservando su profundidad, en el sistema local del tórax y manteniendo centrado su pivote de escala.
Las copias virtuales estiman **42,5 % frontal** y **49,9 % en tres cuartos**. La variante que también adelantaba
el núcleo 0,25 u se descarta: produce penetraciones de hasta 0,230 u durante el pulso. Un descenso de 1 u
con ese adelanto tampoco se recomienda.

| Comparación | Geometría aprobada y texturizada | Hipótesis sin aplicar |
|---|---|---|
| Frente | [Actual](../tools/models/blockbench/studies/osario_v84/fase3/frente.png) | [Propuesta](../tools/models/blockbench/studies/osario_v84/fase3/propuesta_frente.png) |
| Tres cuartos | [Actual](../tools/models/blockbench/studies/osario_v84/fase3/tres_cuartos.png) | [Propuesta](../tools/models/blockbench/studies/osario_v84/fase3/propuesta_tres_cuartos.png) |

Las dos capturas de propuesta muestran únicamente copias temporales de renderizado; el editable y la
instantánea conservan la geometría aprobada. La UV de las dos piezas estrechadas está provisionalmente
comprimida en esas imágenes. Si se autoriza la corrección, deben ajustarse sus UV/pintura para mantener 2 px/u,
comprobar las uniones costales y repetir las pruebas de contactos y pulso a escalas 1 / 1,12 / 1,35.
La propuesta final sin adelanto ya pasa una comprobación virtual SAT de 15 ejes a esas tres escalas,
sin penetración del núcleo en la estructura mayor de 0,01 u. Esa prueba estática no acredita las uniones
costales estrechadas ni los contactos durante animaciones futuras.
Evidencia: [visibilidad y alternativas](../tools/models/blockbench/studies/osario_v84/fase3/propuestas_visibilidad.json),
[holgura virtual](../tools/models/blockbench/studies/osario_v84/fase3/propuestas_holgura.json).

## Comprobaciones ejecutadas y puerta de aprobación

Se ejecutó:

```text
python tools/models/blockbench/studies/osario_v84/fase3/check_assets.py
```

Comprueba conservación exacta de geometría y rig respecto a Fase 2, hashes de las fases anteriores,
igualdad del editable y su instantánea, composición de capas, textura embebida, paleta, alfa, densidad,
ocupación UV, márgenes, transparencia, duplicados significativos, distribución de musgo, máscara y ampliación.
Resultado: **sin fallos**. Las comprobaciones nativas de UV, superficie de musgo y oclusión están registradas
por separado. La validación de datos no reemplaza la aprobación artística.

No se ejecutó Gradle en esta fase: no se modificó código ni recursos activos del juego. La compilación de
referencia ejecutada en Fase 0 está documentada en [el informe inicial](V84_OSARIO_QUEBRADO.md).
No se declara una nueva compilación, validación de GeckoLib en partida ni superada la calidad final de los otros mobs.

La revisión original se entregó con el editable, la máscara y la propuesta comparativa. La autorización posterior
se registra en la sección siguiente.

Commit propuesto, sin ejecutar: `feat: pinta los materiales y la máscara emisiva del Osario V84`.

## Ajuste autorizado y cierre de Puerta 3

El autor aprobó el ajuste e indicó que se continuara. Se estrechó el manubrio a **1 u** y el esternón a
**0,5 u**, y se bajó el núcleo y su pivote **0,5 u**. El desplazamiento de **0,25 u hacia el lado de la masa**
de manubrio y esternón recupera el contacto con las costillas tercera y cuarta, que quedaban separadas en
la maqueta centrada. La inspección del editable ajustado confirma esos contactos y la continuidad entre
manubrio, esternón y xifoides. El núcleo conserva holgura a escalas 1, 1,12 y 1,35.

El muestreo proyectado estima una exposición del **53,3 % de frente** y **54,7 % en tres cuartos** en la cámara
de comparación, frente al 15,7 % y 29,7 % originales. Se reajustaron las dos islas a **2 px/u**. El atlas tiene
17 172 píxeles pintados, los mismos 25 colores y la máscara de 84 píxeles. La comprobación revisó 169 cubos,
30 huesos, 1014 caras, 48 cajas UV y 121 desplegados por cara. Pasó `check_assets.py --adjusted`.

- [Editable ajustado](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/osario_texturizado_ajustado.bbmodel).
- [Frente](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/frente.png) y [tres cuartos desde la masa](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/tres_cuartos.png).
- [Emisión apagada](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/oscuro_emision_off.png) y [encendida](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/oscuro_emision_on.png).
- [Atlas corregido](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/osario_quebrado.png), [máscara](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/osario_quebrado_glowmask.png) y [verificación de archivos](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/asset_checks.json).
- [Medidas de visibilidad](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/visibilidad_nucleo.json) y [uniones y pulso](../tools/models/blockbench/studies/osario_v84/fase3/ajuste/holgura_y_uniones.json).
