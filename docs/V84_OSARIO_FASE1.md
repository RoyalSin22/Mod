# V84 — Fase 1: bloqueo de volúmenes y rig

Estado a 2026-11-18: **Fase 1 aprobada por el autor**. Entrega posterior en [Fase 2](V84_OSARIO_FASE2.md).
La autorización recibida fue «Autorizo la fase 1 con las resoluciones propuestas».
No se ha iniciado la Fase 2. Este bloqueo sirve para aprobar proporciones, apoyos y articulaciones;
los 74 cubos provisionales no representan la geometría final de 145–170 cubos.

## Entrega y alcance

- Copia exacta del editable aprobado: [osario_quebrado_aprobado.bbmodel](../tools/models/blockbench/studies/osario_v84/fase1/osario_quebrado_aprobado.bbmodel).
- Evidencias: [carpeta de Fase 1](../tools/models/blockbench/studies/osario_v84/fase1/).
- Trabajo en `C:/Users/Johan/Desktop/Gits/MOD/osario-quebrado-v84`, rama `codex/osario-quebrado-v84`,
  base `20bc23030ff90660b2c50ac9e50e707a3f462ca9`.
- Construcción mediante Blockbench MCP en un proyecto GeckoLib nuevo. La pestaña anterior `calibracion_ejes`
  permanece abierta y conservada; la calibración de esta fase está en un segundo proyecto, `preflight`.
- No se modificaron Java, recursos activos, dependencias, atributos, otros mobs ni generadores del juego.
  No se realizaron commits, push, merge ni PR. Los respaldos anteriores se conservaron sin sobrescribir.

Los dos Markdown de `C:/Users/Johan/Downloads/Osario/Documentacion/` y las 19 imágenes fueron revisados
en la inspección anterior a esta autorización: cuatro originales de `Referencias Originales/` y las hojas
01–15 de `Dossier_Visual/`. La tabla de seis imágenes del informe inicial describe la primera recepción;
no es el inventario del dossier completo posterior.

## Resoluciones aplicadas

1. **Identidad frontal:** masa a la izquierda de pantalla y brazo fino a la derecha mirando desde el norte.
   En el editable nativo esto corresponde a masa en +X y brazo en −X. Los lados simétricos conservan
   `*_left = +X` y `*_right = −X` en Blockbench. El exportador GeckoLib refleja X en pivotes y orígenes;
   el geo de diagnóstico y su reapertura confirman la misma silueta. Se distinguen ambos espacios.
2. **Rotaciones por efecto anatómico:** X positivo lleva hacia atrás la parte alta de una columna erguida,
   pero adelanta una extremidad colgante. El encorvamiento usa signos negativos; no se invierten indiscriminadamente
   todas las claves. Abrir la mandíbula de este rig también requiere delta X negativo.
3. **Cojera izquierda:** el fémur izquierdo tiene 5,5 u frente a 6 u del derecho. Los apoyos compensan la
   diferencia sin penetrar el suelo. Se adoptan la tabla de pivotes y la hoja 07 frente al texto contradictorio.
4. **Un solo cráneo:** el satélite óseo de la masa es un fragmento sin rasgos faciales. No hay cráneos secundarios.
5. **UV por cara cuando hay fracciones:** el proyecto conserva `box_uv` como ajuste general; 44 cubos
   requieren caras explícitas. GeckoLib 4.7.7 trunca tamaños al construir BOX UV; la prueba de una dimensión
   de 0,5 u conserva las seis caras mediante UV explícita. La pintura definitiva queda para Fase 3.
6. **Pulgar independiente:** `bone_thumb`, hijo de `bone_hand`, aumenta el rig de 29 a 30 huesos.
   Permite oponer el pulgar mientras los otros tres dedos se cierran como grupo. Una prueba estática
   desplazó el pulgar 0,466 u sin desplazar el dedo medio; se restauró el reposo y no se creó un clip.

## Medidas verificadas

Las medidas proceden de las matrices globales de Blockbench, con la pose de reposo aplicada.
Véanse [measurements.json](../tools/models/blockbench/studies/osario_v84/fase1/measurements.json),
[final_rig.json](../tools/models/blockbench/studies/osario_v84/fase1/final_rig.json) y
[asset_checks.json](../tools/models/blockbench/studies/osario_v84/fase1/asset_checks.json).

| Comprobación | Resultado |
|---|---|
| Formato y resolución lógica | `geckolib_model`, 128×128, `box_uv` general |
| Altura posada | **29,783 u**; dentro de 28–30 u |
| Anchura visual global | **21,745 u** |
| Profundidad visual global | **20,840 u** |
| Distancia entre pivotes de hombro | **12,000 u**; proyección X de 11,993 u por el balanceo de 2° |
| Mínimo global | `[-8,995; 0; -11,375]` |
| Máximo global | `[12,750; 29,783; 9,465]` |
| Suelas y base de la masa | **y = 0**; sin hundimiento |
| Jerarquía | **30 huesos**, nombres únicos, árbol sin ciclos, 29 con geometría |
| Geometría provisional | **74 cubos**, dimensiones positivas y múltiplos de 0,5 u |
| Texturas y animaciones | **0 texturas, 0 clips, 0 claves** |
| UV fraccionaria exportada | **44 cubos, 264 caras**, ninguna degenerada; límites dentro de 128² |
| Densidad prevista | Coordenadas UV en pasos de 0,5: enteras a 2 px/u sobre el futuro PNG 256² |
| Reapertura del geo | Misma caja global que el editable, con todos los apoyos en y = 0 |
| Detector de partes separadas | **0** a tolerancia 0,1 u |

El pivote de `dragging_end` está a y = 1,75 u y su geometría apoya en y = 0. Son medidas diferentes:
igualar a cero la posición local del hueso no demuestra contacto mundial con el suelo.

## Construcción provisional por región

| Región | Cubos de bloqueo | Desarrollo reservado a Fase 2 |
|---|---:|---|
| Cráneo y mandíbula | 9 | 22 piezas de referencia: pómulos, dientes, fractura, ramas y desgaste individual |
| Torso, pelvis, cuello y núcleo | 24 | Arcos costales de dos tramos, vértebras, esternón segmentado, pelvis rota, cinco piezas del núcleo |
| Brazo fino y mano | 12 | Cabezas articulares, carpos, cuatro dedos diferenciados y silueta de garra ósea |
| Masa funeraria | 13 | Tierra irregular, satélites, huesos incrustados, raíces y escombros; romper las envolventes rectangulares |
| Piernas y pies | 16 | Trocánteres, articulaciones, metatarsos y deterioro asimétrico |
| **Total** | **74** | Presupuesto final orientativo 145–170; revisar intención de cada pieza |

La masa ya tiene anclaje, cuatro tramos y dos ramas secundarias. Se alargó la envolvente superior a
8,5 u para cerrar un hueco visible en perfil. Las costillas actuales son envolventes de sección;
los puentes dorsales del bloqueo se sustituirán por anatomía individual. No se han pintado materiales.
Gris, ocre, verde y rojo son colores de marcador del editor para distinguir regiones, no la paleta artística final.

## Pivotes y postura

Se ajustó la tabla inicial al medir la pose, sin trasladar todos sus valores literalmente:

- Pelvis a y = 12 u y Z = 0,75 u, sin rotación inicial propia; la asimetría se resuelve en los dos apoyos.
- Lumbar a y = 13,3 u; dorsal a y = 15,6 u y pecho a y = 16,2 u. Curvatura global dorsal de −27° en X,
  más balanceo de 2° en Z. Evita sumar rotaciones que eleven o desplacen indebidamente las cadenas hijas.
- Cabeza adelantada a Z = −7,5 u, inclinada 12° hacia el lado delgado. Se adelantaron y separaron
  cabeza y mandíbula hasta eliminar sus cruces con costillas, esternón y núcleo, manteniendo la altura permitida.
- Los dos hombros permanecen a 12 u. Se retrasó la **envolvente** de tierra 3 u, conservando el pivote
  en la articulación y los puntos globales de la cadena de arrastre.
- Rodillas flexionadas, tobillos a y = 1,25 u y bases de los pies a y = 0. El extremo de la masa
  se compensó en espacio global tras orientar sus padres.
- Se añadieron volúmenes articulares en codo y carpos para cerrar huecos detectados por el validador.
  La costilla superior derecha se retrajo y bajó para separarla del cráneo inclinado.

La tabla completa al final distingue **pivote guardado**, **pivote mundial posado** y **rotación local**.
Los valores del archivo no son coordenadas mundiales posadas si existen padres rotados.

## Verificación técnica y límites

El proyecto temporal `preflight.bbmodel` y su geo comprueban signos de X/Y/Z, reflexión de X al exportar,
parentado real bajo `root` y UV por cara con espesor de 0,5 u. Evidencia en `preflight_checks.json`.
Los archivos `build_blockout.js`, `refine_blockout.js` y `settle_pose.js` registran las operaciones de esta
fase mediante la API de Blockbench, en ese orden, sobre un proyecto vacío. No generan recursos activos del juego.

La exportación `blockout_diagnostic.geo.json` está **solo en la carpeta de estudio**. Se reabrió en una pestaña
independiente para medir la reconstrucción y luego se cerró. No sustituye al modelo que carga Minecraft.
La prueba de mandíbula y pulgar está en `articulation_checks.json`; ambas poses fueron restauradas.

El detector informa **57 pares solapados entre huesos**. Se revisaron como contactos de articulaciones,
ramas mandibulares insertadas en el cráneo, soportes dorsales, costillas absorbidas por el lado funerario,
raíces y fragmentos incrustados. El encuentro pulgar–índice queda en su unión proximal, junto a la palma.
No se usa el indicador genérico `ok` como prueba de ausencia de solapes. Los cruces involuntarios
cráneo–hombro, cráneo–costilla superior, mandíbula–esternón y mandíbula–núcleo se corrigieron.
`validation.json` conserva la lista completa para revisar de nuevo al sustituir las envolventes.

No se ejecutó Gradle en esta fase: no hay cambios de Java ni recursos activos. El build de referencia
ejecutado en Fase 0 sigue documentado en [el informe inicial](V84_OSARIO_QUEBRADO.md): 915 pruebas Java,
sin fallos. El fallo binario conocido del `--check` de dos PNG sigue abierto; no se regeneró otro mob.
Esta fase no acredita carga o emisión en GeckoLib en partida, animaciones finales, IA, daño, red ni rendimiento.

## Capturas de revisión

- [Frente](../tools/models/blockbench/studies/osario_v84/fase1/frente.png).
- [Perfil de la masa](../tools/models/blockbench/studies/osario_v84/fase1/perfil_masa.png).
- [Perfil del brazo fino](../tools/models/blockbench/studies/osario_v84/fase1/perfil_brazo.png).
- [Espalda](../tools/models/blockbench/studies/osario_v84/fase1/espalda.png).
- [Tres cuartos](../tools/models/blockbench/studies/osario_v84/fase1/tres_cuartos.png).
- [Huesos, frente](../tools/models/blockbench/studies/osario_v84/fase1/huesos_frente.png).
- [Huesos, perfil](../tools/models/blockbench/studies/osario_v84/fase1/huesos_perfil.png).

Las líneas verdes y los puntos amarillos son ayudas de captura calculadas a partir de las matrices reales
de los huesos. Se retiraron de la escena; no son cubos ni forman parte del editable o del geo.

## Puerta 1

Los criterios medibles de formato, jerarquía, dimensiones, suelo y exportación de diagnóstico están comprobados.
**El autor aprobó la silueta, proporciones y postura**, incluido el pulgar independiente, y autorizó la Fase 2.
No se asigna una nota artística al bloqueo ni se compara todavía con los modelos terminados.

La Fase 2 sustituyó las envolventes por geometría anatómica individual, conservando los apoyos y los pivotes;
su entrega está en [V84_OSARIO_FASE2.md](V84_OSARIO_FASE2.md). Pintura, glowmask, diez animaciones e integración
seguirán sus propias puertas.

## Tabla final de los 30 huesos

| Hueso | Padre | Pivote guardado (u) | Pivote mundial posado (u) | Rotación local (°) |
|---|---|---|---|---|
| `root` | — | [0.000, 0.000, 0.000] | [0.000, 0.000, 0.000] | [0.000, 0.000, 0.000] |
| `pelvis` | `root` | [0.000, 12.000, 0.750] | [0.000, 12.000, 0.750] | [0.000, 0.000, 0.000] |
| `body` | `pelvis` | [0.000, 13.300, 0.650] | [0.000, 13.300, 0.650] | [-12.000, 0.000, 0.000] |
| `spine_upper` | `body` | [0.000, 15.643, 0.688] | [0.000, 15.600, 0.200] | [-15.007, -0.416, 1.956] |
| `chest` | `spine_upper` | [0.021, 16.336, 0.648] | [-0.000, 16.200, -0.150] | [0.000, 0.000, 0.000] |
| `ribs_left` | `chest` | [2.521, 20.336, 0.148] | [2.382, 19.622, -2.411] | [0.000, 0.000, 0.000] |
| `ribs_right` | `chest` | [-2.479, 20.336, 0.148] | [-2.615, 19.448, -2.411] | [0.000, 0.000, 0.000] |
| `chest_core` | `chest` | [0.021, 20.436, -1.252] | [-0.097, 18.989, -3.704] | [0.000, 0.000, 0.000] |
| `neck` | `chest` | [0.220, 22.751, 0.606] | [-0.000, 21.900, -3.100] | [11.986, 0.908, -1.782] |
| `head` | `neck` | [0.220, 25.387, -3.243] | [-0.000, 23.450, -7.500] | [10.687, -3.085, 11.602] |
| `jaw` | `head` | [0.085, 24.753, -3.287] | [-0.000, 22.800, -7.500] | [-6.000, 0.000, -0.000] |
| `bone_shoulder` | `chest` | [-5.979, 23.336, 0.648] | [-6.214, 22.224, -3.328] | [0.000, 0.000, 0.000] |
| `bone_upper_arm` | `bone_shoulder` | [-5.979, 23.336, 0.648] | [-6.214, 22.224, -3.328] | [31.572, 4.253, -7.749] |
| `bone_forearm` | `bone_upper_arm` | [-5.979, 17.147, 0.648] | [-6.950, 16.100, -3.850] | [-0.047, 0.187, 4.431] |
| `bone_hand` | `bone_forearm` | [-5.973, 11.021, 0.661] | [-7.200, 10.000, -4.350] | [3.200, -0.067, 0.405] |
| `bone_fingers` | `bone_hand` | [-5.973, 9.521, 0.561] | [-7.251, 8.529, -4.658] | [0.000, 0.000, 0.000] |
| `bone_thumb` | `bone_hand` | [-4.723, 9.771, 0.561] | [-5.993, 8.733, -4.623] | [5.000, 0.000, 45.000] |
| `mass_shoulder` | `chest` | [6.021, 23.336, 0.648] | [5.779, 22.643, -3.328] | [0.000, 0.000, 0.000] |
| `dragging_upper` | `mass_shoulder` | [6.391, 22.956, 1.272] | [6.150, 22.600, -2.600] | [6.378, -1.344, 5.722] |
| `dragging_middle` | `dragging_upper` | [6.391, 14.382, 1.272] | [7.300, 14.650, 0.400] | [-4.145, 0.421, -0.928] |
| `dragging_lower` | `dragging_middle` | [6.391, 6.228, 1.272] | [8.250, 7.300, 3.800] | [-2.809, 0.573, -2.077] |
| `dragging_end` | `dragging_lower` | [6.439, 1.100, -0.945] | [8.750, 1.750, 4.200] | [27.499, 1.118, -4.566] |
| `debris_roots` | `dragging_middle` | [7.731, 10.040, 2.729] | [9.100, 11.500, 3.500] | [6.455, -3.538, 4.220] |
| `debris_chips` | `dragging_lower` | [8.347, 2.339, 1.771] | [10.500, 4.250, 6.000] | [26.775, 6.607, -15.265] |
| `thigh_left` | `pelvis` | [2.600, 11.250, 0.750] | [2.600, 11.250, 0.750] | [24.702, 0.000, 0.000] |
| `shin_left` | `thigh_left` | [2.600, 5.746, 0.750] | [2.600, 6.250, -1.550] | [-58.127, 0.000, 0.000] |
| `foot_left` | `shin_left` | [2.600, -0.244, 0.750] | [2.600, 1.250, 1.750] | [33.425, 0.000, 0.000] |
| `thigh_right` | `pelvis` | [-2.600, 11.750, 0.750] | [-2.600, 11.750, 0.750] | [27.832, 0.474, -1.911] |
| `shin_right` | `thigh_right` | [-2.600, 5.753, 0.750] | [-2.800, 6.450, -2.050] | [-57.731, 0.474, 1.911] |
| `foot_right` | `shin_right` | [-2.600, -0.246, 0.750] | [-2.800, 1.250, 0.940] | [29.899, 0.000, 0.000] |

Las cifras de esta tabla se redondean a tres decimales; los JSON y el editable conservan mayor precisión.

Commit propuesto, sin ejecutar: `feat: inicia el rig y bloqueo del Osario Quebrado V84`.
