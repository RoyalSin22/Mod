# V84 — Fase 5. Integración del Osario Quebrado

Las Puertas 4 y 5 fueron aprobadas por el autor. El Osario integrado sustituye al modelo de prueba
y resuelve sus cuatro ataques como acciones de servidor. La validación en Minecraft continúa en la
[Fase 6](V84_OSARIO_FASE6.md).

[Editable animado](../tools/models/blockbench/osario_quebrado.bbmodel) ·
[Capturas y claves de las diez animaciones](V84_OSARIO_FASE4.md) ·
[Compilación local](../build/libs/aptitudes-3.8.8.jar)

## Arte y empaquetado

Se exportaron a la carpeta de estudio de Fase 5 los archivos de Blockbench y se compararon con la versión
activa antes de reemplazarla. El modelo pasó de 7 huesos y 8 cubos a **30 huesos y 169 cubos**. Los tres
clips anteriores dieron paso a los **diez aprobados**. El atlas pasó de 64² al **PNG 256²** sobre una UV
lógica de 128². La máscara independiente contiene 84 píxeles del núcleo. Se conservaron los PNG de estudio
y la salida nativa de animaciones byte por byte. En el geo de producción se fijaron las cotas visibles
del contrato a 2,5 × 2,4 con desplazamiento vertical de 1,0.

`MobRenderers` añade `AutoGlowingGeoLayer` al Osario y anula el giro de muerte para que GeckoLib
mantenga la pose final. El generador de modelos reconoce el Osario como recurso de autoría. Su comprobación
de PNG compara píxeles RGBA si los bytes de compresión difieren. Así se resolvió la discrepancia de
codificación detectada en Fase 0 sobre el Custodio, cuyo archivo de textura sigue siendo el mismo.

## Combate

El registro conserva `aptitudes:osario_quebrado`, su huevo, la caja de 0,75 × 1,8 bloques y el alcance
de seguimiento de 8 chunks. La altura de los ojos es 1,45. El daño usa el atributo vigente y la fuente
`mobAttack`, por lo que pasa por las reglas existentes de daño del mod.

| Acción | Aviso | Ventana activa | Duración | Enfriamiento |
|---|---:|---|---:|---:|
| Zarpazo | t4 | t10 y t11. Sector de 60°, alcance 2,0 | 18 t | 24 t |
| Barrido | t8 | t19 a t21. Sector de 160°, alcance 2,7 | 28 t | 70 t |
| Impacto | t10 | t23 y t24. Disco de radio 2,1 centrado 1,5 delante | 36 t | 200 t común, 120 t élite o campeón |
| Embestida | t4 | Desde t14 hasta antes de t21. Cápsula de radio 0,55 sobre el recorrido | 27 t | 160 t |

`OsarioStrikeRules` calcula contactos con cajas, reloj, selección por pesos e impactos únicos por objetivo.
`OsarioStrikeGoal` fija la orientación del ataque, comprueba los obstáculos y recupera la navegación tras
la acción. La embestida fija su dirección al arrancar, comprueba suelo y bloques y mide el tramo
que el mob ha recorrido. Se detiene al primer contacto válido. Los sectores y el disco permiten varios
objetivos y una sola resolución por objetivo y acción. Los rangos usan la escala de la criatura.

La selección distingue distancia, orientación, línea de visión, movimiento del objetivo, persecución,
jugadores cercanos, densidad del evento, recuperación y enfriamientos. Una criatura repite como máximo
dos veces seguidas el mismo ataque. Las fichas limitan a tres Osarios por jugador durante aviso e impacto.
La cuenta pertenece a la dimensión y se libera al terminar la ventana, morir o descargarse. Se mantienen
el tratamiento del campeón de la Cripta y el botín que depende del origen en el evento.

El cliente usa un sello sincronizado de tick y acción de cuatro bits. El controlador de locomoción distingue
reposo, marcha y persecución, con 10 ticks de histéresis al pasar entre marcha y persecución. El controlador
de acción se alinea con el sello tras su transición de dos ticks. La reacción al daño espera a que termine
la ejecución ofensiva y la muerte toma el control al recibir daño letal.

## Audio y textos

Se generaron **diez OGG Vorbis mono a 44,1 kHz** mediante síntesis original. Incluyen aviso y golpe de masa
como sonidos separados y un arrastre ligado al paso. Los chasquidos de zarpazo, barrido y embestida se sitúan
dentro de la pista según el tiempo entre el aviso y el contacto. La estela verde del barrido emite hasta
14 partículas. El bestiario explica las señales de combate y la sepultura incompleta en los 11 idiomas,
con subtítulos propios para los diez sonidos.

## Verificación

- `gradlew.bat build --offline --console=plain` con Java 21 y la caché local de Gradle 8.14.3.
  **BUILD SUCCESSFUL**, 924 pruebas JUnit en 153 clases, sin fallos ni omisiones.
- `OsarioStrikeRulesTest` y `OsarioResourcesTest` aportan nueve pruebas. Los cargadores de GeckoLib 4.7.7
  construyeron el rig y los diez clips desde los recursos instalados. Se comprobaron los tiempos y modos,
  UV y máscara, sectores, disco, volumen barrido, contactos únicos, selección, fichas y subtítulos.
- `gen_mob_models.py --check`, `check_lang.py`, los siete generadores de traducción y las 59 pruebas de
  idiomas pasaron. `git diff --check` pasó.
- Se comprobaron los OGG como Vorbis mono y su presencia en el JAR remapeado, junto a la geometría,
  animaciones, atlas, máscara y código compilado.

La Puerta 5 verificó la compilación y los recursos. Las pruebas de combate, emisión y rendimiento
se registran en la [Fase 6](V84_OSARIO_FASE6.md).

Rama de trabajo `codex/osario-quebrado-v84`, basada en `20bc230`. La entrega está en el árbol de trabajo.
