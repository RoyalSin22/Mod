# V84 — Fase 4. Animaciones del Osario Quebrado

La Puerta 3 se cerró con el ajuste de tórax autorizado por el autor. La Puerta 4 fue aprobada y los diez clips
pasaron a [integración](V84_OSARIO_FASE5.md).

- [Editable de revisión](../tools/models/blockbench/studies/osario_v84/fase4/osario_animado_fase4.bbmodel).
- [Tabla completa de claves finales](../tools/models/blockbench/studies/osario_v84/fase4/claves_finales.json).
  Cada fila es `[segundos, x, y, z]`. Las rotaciones son deltas nativos en grados respecto a la pose de reposo.
- [Mediciones sobre las animaciones de Blockbench](../tools/models/blockbench/studies/osario_v84/fase4/validacion_clips.json).
- [Ajuste de tórax, textura y capturas de la Puerta 3](V84_OSARIO_FASE3.md#ajuste-autorizado-y-cierre-de-puerta-3).

## Clips y poses

| Clip | Duración | Instantes para revisar | Capturas |
|---|---:|---|---|
| `idle` | 3,20 s | Bucle. Pulso irregular desde 0,60 s, respiración, mandíbula y transferencia de peso | [Reposo](../tools/models/blockbench/studies/osario_v84/fase4/idle.png) |
| `walk` | 1,20 s | Bucle. Contactos en 0 y 0,60 s, apoyo alterno y arrastre | [Caminata](../tools/models/blockbench/studies/osario_v84/fase4/walk.png) |
| `chase` | 0,85 s | Bucle. Contactos en 0 y 0,425 s, inclinación mayor y zancada más alta | [Persecución](../tools/models/blockbench/studies/osario_v84/fase4/chase.png) |
| `alert` | 0,65 s | Pico en 0,15 s. Cráneo elevado, mandíbula abierta, pulso a 1,35 | [Alerta](../tools/models/blockbench/studies/osario_v84/fase4/alert.png) |
| `claw_swipe` | 0,90 s | Carga en 0,35 s, impacto en 0,50 s (t10), extensión y recogida | [Zarpazo](../tools/models/blockbench/studies/osario_v84/fase4/claw_swipe.png) |
| `heavy_swipe` | 1,40 s | Mano en alto en 0,80 s, golpe en 0,95 s (t19), torsión y contrapeso | [Barrido](../tools/models/blockbench/studies/osario_v84/fase4/heavy_swipe.png) |
| `ground_slam` | 1,80 s | Carga sobre el hombro en 0,90 s, contacto en 1,15 s (t23), rebote | [Impacto del Osario](../tools/models/blockbench/studies/osario_v84/fase4/ground_slam.png) |
| `lunge` | 1,35 s | Impulso en 0,70 s (t14), extensión en 0,875 s, frenado desde 1,05 s (t21) | [Embestida](../tools/models/blockbench/studies/osario_v84/fase4/lunge.png) |
| `hurt` | 0,40 s | Retroceso máximo en 0,08 s y restitución en 0,40 s | [Reacción al daño](../tools/models/blockbench/studies/osario_v84/fase4/hurt.png) |
| `death` | 0,95 s | Flexión, caída y reducción del núcleo a 0,2. Conserva la última pose | [Muerte](../tools/models/blockbench/studies/osario_v84/fase4/death.png) |

Las claves de dirección se adaptaron a los ejes del rig. El brazo se guía por una trayectoria de muñeca
resuelta con el húmero, el radio y el cúbito ya modelados. Así la mano pasa por delante en los tiempos de golpe,
y el barrido se distingue del zarpazo por su carga sobre la cabeza y su arco bajo. La embestida conserva una
traslación horizontal de raíz igual a cero. El avance de la entidad y la ventana de daño se conectarán en la Fase 5.

La marcha resuelve cada apoyo con las longitudes reales de muslo y pierna. La masa queda a ras en reposo
y roza el suelo al caminar. Sus tramos retrasan la respuesta 0,12 s cada uno en las curvas de seguimiento.
Las raíces y fragmentos añaden oscilación secundaria. En el impacto de masa y en la muerte se ajustó la
cadena con objetivos propios, porque retrasar las rotaciones sin verificar la posición global hacía atravesar
el suelo o levantaba el cuerpo. Los clips conservan curvas Catmull-Rom. Se corrigieron de forma selectiva
las pequeñas penetraciones que aparecían entre claves.

## Verificación

El validador de Blockbench recorrió **2042 poses a unos 160 Hz**, aplicando a las esquinas de los **169 cubos**
las matrices de sus cadenas de huesos. La menor Y medida fue **−0,00601 u**. Las claves de inicio y cierre
de los tres bucles coinciden. En los contactos de caminata y persecución ambas suelas quedaron a menos de
0,07 u del plano. Durante la persecución el extremo de la masa no superó **0,721 u** de altura.

En el impacto de masa de **1,15 s**, el terrón inferior toca el plano y el borde delantero del extremo llega
a **z = −24,662 u**, aproximadamente 1,54 bloques por delante, con **y = 0,064 u**. En ese fotograma las
suelas quedan a **0,461 u** del plano mientras la masa soporta el golpe. En el cierre de la muerte,
la caja horizontal ocupa **1,378 × 1,370 bloques**. Suelas y base funeraria quedan a menos de 0,001 u
del suelo. La mano termina recogida, con su punto más bajo a **3,07 u**.

El archivo guardado contiene **10 042 claves**. La comprobación posterior lee el editable, compara sus claves con
la tabla, confirma los fotogramas de contacto, los modos de reproducción y que la geometría, el rig y las
texturas sean iguales a la instantánea ajustada de la Fase 3.

```text
python tools/models/blockbench/studies/osario_v84/fase3/check_assets.py --adjusted
python tools/models/blockbench/studies/osario_v84/fase4/check_clips.py
```

Ambos comandos pasaron. SHA-256 del editable de revisión
`bdc81ffb5212bf8b344e50ac279ffa167201aff518ac2d6069a6a37be026b060`.

La revisión de esta fase corresponde al editor. La carga de los clips exportados se comprobó en
[Fase 5](V84_OSARIO_FASE5.md). La cadencia en partida, las transiciones, el daño y las partículas
se evaluarán durante las pruebas de Fase 6.
