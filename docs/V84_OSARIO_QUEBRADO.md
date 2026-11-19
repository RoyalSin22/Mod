# V84 — Osario Quebrado

**Fases 0–4 aprobadas. Integración de Fase 5 preparada para revisión.**
Este archivo conserva el informe inicial de la Fase 0. El bloqueo nuevo, la revisión posterior del dossier
completo y las resoluciones aplicadas se documentan en [V84_OSARIO_FASE1.md](V84_OSARIO_FASE1.md).
La geometría está en [Fase 2](V84_OSARIO_FASE2.md), los materiales y el ajuste del tórax en
[Fase 3](V84_OSARIO_FASE3.md), las diez animaciones en [Fase 4](V84_OSARIO_FASE4.md) y el código,
los recursos y las comprobaciones de integración en [Fase 5](V84_OSARIO_FASE5.md).

## Alcance autorizado

El autor confirmó «Ejecutar V84 por fases» tras entregar el encargo V2 y seis imágenes.
Se respeta su sección G: «Después de cada fase entrega capturas/evidencias y espera la aprobación del autor
antes de continuar». Las decisiones D-15 a D-23 se mantienen como decisiones de diseño; las contradicciones
de coordenadas se exponen antes de aplicarlas.

La Fase 0 comprueba el juego sin modificarlo. Sus únicas escrituras son documentación, copias históricas,
referencias y sondas de calibración en un proyecto independiente de Blockbench. No es el modelo del Osario.
No se ha hecho commit, push, merge ni PR. No se han creado o movido etiquetas.

## VERIFICADO — Base y dependencias

- Worktree: `C:/Users/Johan/Desktop/Gits/MOD/osario-quebrado-v84`.
- Rama: `codex/osario-quebrado-v84`.
- Base: `20bc23030ff90660b2c50ac9e50e707a3f462ca9` (`origin/main`, comprobado también mediante `git ls-remote`).
- La copia principal local estaba en `b2359b2`, dos commits por detrás. Se avanzó únicamente la rama nueva,
  todavía limpia y sin commits propios, mediante `git rebase origin/main`. El `main` local no se modificó.
- Minecraft 1.21.1, Yarn `1.21.1+build.3`, Loader 0.16.14, Fabric API `0.116.6+1.21.1`, GeckoLib 4.7.7,
  PAL `1.1.6+mc.1.21.1`, Mod Menu 11.0.3 opcional y Gradle 8.14.3 coinciden con el encargo.
- Java ejecutado: Temurin 21.0.13. Python 3.14.7 y Pillow 12.3.0 disponibles.
- **Diferencia en la declaración de Loom:** `build.gradle` pide `1.10-SNAPSHOT`; durante la compilación se
  resuelve a **1.10.5**. No se ha cambiado la declaración ni ninguna dependencia.
- Blockbench MCP confirma conexión, **Blockbench 5.2.2**, **puente 1.11.1.1**, formato `geckolib_model`,
  rig de huesos, animaciones y `box_uv`. Al conectar no había proyectos abiertos.

## VERIFICADO — Estado del Osario

| Pieza | Resultado observado |
|---|---|
| Registro | Una entidad `aptitudes:osario_quebrado` y su huevo; `OsarioEntities` |
| Atributos | Vida 24, daño 5, velocidad 0,27, seguimiento 24 |
| Caja física | Anchura 0,75 y altura 1,8; seguimiento de red a 8 chunks |
| IA y daño | `MeleeAttackGoal`; `tryAttack` dispara `heavy_swipe` y llama inmediatamente a `super.tryAttack` |
| Comentario desactualizado | Dice que el golpe llega al final de la animación; el código no lo demora |
| Recursos activos | **7 huesos, 8 cubos**, PNG 64×64 con 5 colores RGBA distintos |
| Clips | `idle` 2 s y `walk` 1 s en bucle; `heavy_swipe` 1,4 s |
| Editable y emisión | Sin `.bbmodel` del Osario activo ni glowmask |
| Renderizador | Está en **`src/client/java/.../MobRenderers.java`**; escala 1, sin capa emisiva ni anulación propia del giro de muerte |
| Apariciones | 10 % de la Niebla (`OsarioRules`/`FogMobRules`), oleadas de la Cripta y huevo |
| Campeón | `CryptWaves.spawnChampion` reutiliza el Osario, `Elites.makeElite` y su modificador de vida por anillo |
| Botín | `dropLoot` exige la etiqueta de evento para soltar el Núcleo; no se ha modificado |
| Sonido | Sin eventos propios del Osario común en `ModSounds`/`sounds.json` |
| Generador | Osario aún no pertenece a `HAND_MADE`; sí pertenecen Errante y Escorificador |

**Matiz de V83:** las tácticas ya existen en esta base. `ThreatManager.tactical` exige elegibilidad y un anillo
positivo. Los ejemplares etiquetados como evento están excluidos por `eligible`; no todos los Osarios reciben
furia, esquiva o llamada de auxilio. Se conservará esa regla. Además, el multiplicador de daño por Amenaza se
aplica en `ThreatManager.modifyDamage`, no añadiendo por separado el mismo multiplicador a `GENERIC_ATTACK_DAMAGE`.
La integración futura debe leer el atributo vigente y dejar que el gancho global haga su trabajo una sola vez.

El patrón del Errante se confirmó en lectura: `tryAttack = false`, `RendGoal`, reloj puro, sello de tick en
`TrackedData<Long>`, reproducción de acciones desde ese sello y giro de muerte anulado. Su `hurt` dura 9 ticks
(el encargo lo aproxima a 0,43 s); V84 propone 8 ticks para el Osario. No se cambia el Errante.

Las fuentes locales de **GeckoLib 4.7.7** confirman:

- `BakedModelFactory` y `BakedAnimationsAdapter` niegan X e Y al cargar modelo y animaciones.
- `AnimationProcessor` suma las rotaciones animadas a la instantánea inicial: las claves son deltas.
- `GeoEntityRenderer` aplica la escala de la entidad y permite anular `getDeathMaxRotation`.
- El UV usa la resolución declarada en el geo; PNG 256² / UV 128² da 2 px/u.
- `AutoGlowingTexture` busca `_glowmask`; `GeoGlowingTextureMeta.createImageMask` toma el color de la base,
  conserva el alfa indicado para la máscara y borra esos píxeles de la textura base cargada en memoria.
  La comprobación con/sin capa en partida sigue pendiente; ocultar la capa después de cargar la máscara
  no equivale necesariamente a cargar de nuevo una textura base intacta.

## VERIFICADO — Preservación histórica

Se copiaron blobs de Git como bytes, conservando sus rutas originales bajo cada carpeta; no se importaron al juego.
Cada instantánea tiene `manifest.json` con commit, hash de blob, SHA-256 y tamaño de cada archivo.
`python tools/models/archive_osario_v84.py` verifica los archivos ya presentes y rechaza sobrescribir contenido distinto.

| Carpeta bajo `tools/models/blockbench/ref/` | Origen | Archivos |
|---|---|---|
| `osario_v65_codex/` | `1534389a752513da383ea9bc5a0f172644cac6bd`, punta comprobada de `origin/faceless-brillo` | 34 |
| `placeholder_main/` | Base `20bc23030ff90660b2c50ac9e50e707a3f462ca9` | 13 |
| `osario_v84_fase0/referencias/` | Encargo V2 y las seis imágenes recibidas, sin alterarlas | 7 |

El archivo previo confirma 15 huesos, 21 cubos, editable `bedrock` con `box_uv`, PNG 256² sobre UV 128²,
glowmask y cuatro clips (`idle`, `walk`, `heavy_swipe`, `sweep`). Sus atributos **60 / 12 / 0,22** y caja
**1,2 × 2,9** divergen del común. Se conservan también sus hojas, generador de textura y especificación.

**Diferencia con el encargo:** la etiqueta `archive/osario-faceless-brillo-1534389` no existe entre las referencias
locales ni fue devuelta por la consulta exacta a GitHub. No se ha creado ni movido. El respaldo usa el hash
completo comprobado, por lo que no depende de esa etiqueta ausente.

## VERIFICADO — Lectura de las seis referencias

Copias y hashes: `../tools/models/blockbench/ref/osario_v84_fase0/referencias/manifest.json`.

| Referencia | Lectura y prioridad |
|---|---|
| 01 — Ortográficas | Refuerza las medidas: unos 29 u, torso/caja de 12 u y extensión visual de unos 22 u. Masa a la izquierda frontal, derecha posterior, anclada del hombro al suelo |
| 02 — Concepto | Cráneo agrietado, costillas separadas, núcleo único verde, masa granular con raíces, huesos y musgo localizado. Materiales y silueta; no copiar iluminación ni densidad de detalle del render |
| 03 — Ficha voxel | Referencia principal de anatomía, cuatro dedos, piernas finas y asimetría. La tabla y la miniatura UV son indicativas; la imagen no contiene un atlas utilizable |
| 04 — Maqueta | Referencia secundaria coherente con columna, costillas, núcleo y masa del hombro al suelo; las capturas no son un proyecto real ni prueban pivotes o tiempos |
| 05 — Pergamino | Referencia principal de identidad, masa lateral y núcleo verde. Los otros enemigos y los afijos del panel no forman parte del trabajo |
| 06 — Postura | Solo encorvamiento y peso. La masa reducida al antebrazo y el brazo muy largo divergen de la silueta aprobada; no se adoptan |

Las cuatro imágenes pedidas originalmente están recibidas, junto con las dos hojas adicionales. No falta arte
para empezar el bloqueo tras aprobar la Puerta 0. Las discrepancias entre miniaturas no justifican espejar
la criatura de un clip a otro, vestirla ni convertir la masa en una garra o una capa de tela.

## VERIFICADO — Calibración y contradicciones de las tablas

Proyecto independiente: `../tools/models/blockbench/ref/osario_v84_fase0/calibracion_ejes.bbmodel`.
Contiene **tres grupos y cuatro sondas**, sin anatomía del Osario. Se dejó en pose neutra.
Las medidas proceden de las matrices reales de Blockbench (Euler ZYX); están en
`calibration_results.json` y `geometry_measurements.json` dentro de la misma carpeta.

Pivote de prueba `[0,8,0]`; extremo superior neutro `[0,20,0]`; marca frontal neutra `[0,12,-7]`.

| Prueba aislada | Coordenadas tras rotar | Efecto observado |
|---|---|---|
| `spine_upper` X +10° | Extremo superior `[0,19.817693,2.083778]` | La parte superior va **hacia atrás (+Z)**, al contrario de la descripción del torso |
| `body` Y +10° | Marca frontal `[-1.215537,12,-6.893654]` | El frente gira hacia −X, como indica el encargo |
| `body` Z +10° | Extremo superior `[-2.083778,19.817693,0]` | La parte alta cae hacia −X, como indica el encargo |

Una sonda **colgante** con X +10° se mueve hacia −Z: torso y extremidad están a lados opuestos del pivote.
Por eso «X positivo = hacia delante» no es una regla universal para todas las cadenas. Invertir todas las
claves X sin considerar la anatomía podría corregir el torso e invertir una rodilla o un barrido correcto.

Evidencias con cámara fija:

- [Neutro](../tools/models/blockbench/ref/osario_v84_fase0/00_neutro.png).
- [Columna X +10°](../tools/models/blockbench/ref/osario_v84_fase0/01_columna_x_10.png).
- [Cuerpo Y +10°](../tools/models/blockbench/ref/osario_v84_fase0/02_cuerpo_y_10.png).
- [Cuerpo Z +10°](../tools/models/blockbench/ref/osario_v84_fase0/03_cuerpo_z_10.png).
- [Frente norte](../tools/models/blockbench/ref/osario_v84_fase0/04_frente_north.png): la sonda **−X violeta
  queda a la derecha de pantalla**; la **+X amarilla queda a la izquierda**. El frente mira a −Z.

### PROPUESTA — Correcciones para la Puerta 0

1. **Conservar la imagen aprobada y corregir la equivalencia lateral:** en vista frontal norte, masa a
   la izquierda de pantalla significa **masa en +X**, y brazo delgado a la derecha significa **brazo en −X**.
   Mantener nombres semánticos y `*_left = +X` / `*_right = −X` para costillas y piernas; reflejar las
   coordenadas de las cadenas asimétricas de §6.2 al construirlas. No cambiar el frente a +Z para ocultar el error.
2. **Corregir rotaciones por su efecto medido:** usar X negativo para inclinar hacia delante la columna
   erguida. Revisar cada cadena y sus deltas con el rig visible, conservar la flexión anatómica de las rodillas
   y documentar los valores finales. Las tablas numéricas son puntos de partida, no sustituyen la medición global.
3. **Respetar un solo cráneo reconocible:** §4 prohíbe cráneos extra, pero §5/§6.3 incluyen `skull_small`
   en la masa. Sustituirlo por un fragmento óseo sin rasgos de cráneo, con la misma función de romper la caja.

Estas correcciones se presentaron para la Puerta 0 y fueron aceptadas al autorizar la Fase 1.
El bloqueo nuevo usa 30 huesos al independizar el pulgar; atlas 256² sobre UV 128², núcleo único,
cuatro ataques y caja física intacta siguen siendo el contrato de las fases posteriores.

### PENDIENTE — Precisiones de fases posteriores

- El presupuesto de §6.3 **sí suma 145 cubos en 28 huesos con geometría**, más `root` = 29 huesos.
  Las áreas UV y el empaquetado anunciados deben comprobarse sobre la geometría final, en la Fase 3.
- §5 dice pierna derecha más corta; §6.2 sitúa la diferencia en `thigh_left`. En la Fase 1 se debe fijar
  qué pierna cojea y anotarlo junto con la corrección lateral; no cambiar nombres anatómicos para disimularlo.
- §14 dice «9 eventos» de sonido, pero enumera **10** si paso/arrastre comparten un evento, u 11 si se separan.
  Resolver el catálogo al preparar la Fase 5, antes de registrar ids o subtítulos.
- Dash: 7 ticks y el intervalo inclusivo «t14–t21» no equivalen. La implementación debe definir los siete
  pasos explícitamente, por ejemplo `14 <= tick < 21`, y comprobarlos con pruebas; no dar ocho impulsos.
- `P.y = 0` local en `dragging_end` no garantiza contacto mundial con el suelo si sus padres rotan o se trasladan.
  Comprobar y compensar en espacio global. No se han validado aún pies, alturas, animaciones ni impactos.

## PROPUESTA — Boceto textual de silueta

- **Frente:** cabeza cúbica inclinada hacia el brazo fino, cuencas hundidas y mandíbula suelta; costillas de
  hueso claro que dejan ver una sola cruz verde; masa oscura a la izquierda de pantalla, desde el hombro
  hasta una base irregular apoyada, más ancha abajo. Mano fina de cuatro dedos por debajo de la rodilla.
- **Perfil:** torso inclinado hacia delante, cráneo adelantado, rodillas dobladas y pies de tres falanges.
  La masa queda retrasada y arrastra raíces y huesos hasta el suelo; sin extremidad rectangular uniforme.
- **Espalda:** columna segmentada legible y costillas alrededor; cráneo posterior sobrio; masa a la derecha
  de pantalla en esta vista. Musgo localizado en juntas húmedas y escombros, sin tapizar el cuerpo.
- **Tres cuartos:** hombro de tierra y raíz junto al cuello que conduce la mirada a la base; brazo delgado
  libre hacia delante. Lectura pecho → masa → cráneo, reconocible también sin partículas.

Se mantienen: silueta original y nombres semánticos (D-15), atlas 256² a 2 px/u, diferencia con el Gran Osario,
trabajo previo conservado y fuera de integración, estela de partículas y presión colectiva limitada a Osarios.
No se alteran otros enemigos, atributos base, registros, namespace ni balance global.

## VERIFICADO — Comprobaciones de referencia

| Comando ejecutado | Resultado real |
|---|---|
| `.\gradlew.bat build --offline --console=plain` | **BUILD SUCCESSFUL**, 1 min 50 s; 12 tareas ejecutadas |
| JUnit del build | **915 pruebas, 151 clases, 0 fallos, 0 errores, 0 omitidas** |
| `python -m unittest discover -s tools/lang -p 'test_*_lang.py'` | **59 pruebas, OK** |
| `python tools/lang/check_lang.py` | Correcto; pt/fr/de con 4447 claves, orden y marcadores correctos |
| `gen_interface_lang`, `gen_guide_lang`, `gen_nodes_lang`, `gen_evolved_lang`, `gen_path_lang`, `base_desc`, `gen_lore_lang`, todos con `--check` | Todos devuelven 0 |
| `python tools/models/gen_mob_models.py src/main/resources/assets/aptitudes --check` | **Devuelve 1**: PNG del Osario y del Custodio distintos en bytes |
| Comparación RGBA de ambos PNG con la salida del generador | **0 píxeles distintos en ambos**; son diferencias de codificación PNG, no diferencias de imagen |
| `python tools/models/archive_osario_v84.py` | 34 + 13 archivos verificados, sin sobrescrituras |

El build avisó de un fallo al remapear por completo las **fuentes** de PAL (`MochaEngine`), además de avisos
de deprecación; continuó y completó compilación y pruebas. No se tocaron dependencias para ocultar el aviso.
El `--check` binario del generador de mobs permanece fallido; no se regeneró el Custodio fuera de alcance.

Evidencias legibles por máquina: `checks.json`, `junit_summary.json`, `png_comparison.json`, `inventory.json`,
`calibration_results.json`, `geometry_measurements.json` y los manifiestos, bajo las carpetas anteriores.
Los informes completos de JUnit del worktree están en `build/reports/tests/test/` (salida local de compilación).

## HISTORIAL — Cierre de la Puerta 0

El autor autorizó expresamente la Fase 1 con las resoluciones propuestas. Se creó el editable nuevo
`tools/models/blockbench/osario_quebrado.bbmodel` con el rig y volúmenes básicos. La entrega de la
Puerta 1 y sus medidas están en [V84_OSARIO_FASE1.md](V84_OSARIO_FASE1.md).

**No verificado:** calidad de la geometría final, UV final, emisión en juego, los diez clips,
sincronización, cuatro ataques, navegación, élites, campeón, latencia, multijugador y rendimiento con
1/5/10/20/30 Osarios. No se asignan notas ni cifras de FPS/MSPT sin medición.
