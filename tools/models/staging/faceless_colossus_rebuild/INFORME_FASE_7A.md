# Coloso Sin Rostro · Fase 7A

## A. Entorno y origen del editable

- Repositorio `C:/Users/Johan/Desktop/Gits/MOD/Mod`, rama `faceless-brillo`, un commit por delante de `origin/faceless-brillo` al iniciar. Había cambios de otro trabajo en el Osario, documentación, `MobRenderers.java` y `gen_mob_models.py`. La preparación solo escribió en staging y en una copia temporal para Gradle.
- Blockbench 5.2.2 conectado por MCP. Se verificaron las dos pestañas y se usó la que mostraba las texturas, cuyo archivo de origen estaba en `C:/Users/Johan/Pictures/Camera Roll/faceless_colossus_rebuild_animated.bbmodel`. La geometría, las UV y las dos texturas de nueve capas coinciden con el paquete corregido de Fase 6. Se trasplantaron y verificaron las siete animaciones con las claves corregidas y se guardó esa pestaña como [editable texturizado](faceless_colossus_rebuild_textured_animated.bbmodel). El archivo de Camera Roll conserva su SHA-256 original.
- Se usaron las herramientas MCP de inspección y ejecución, el códec Bedrock de Blockbench, el códec de animación y el lienzo del viewport para las capturas. La pestaña de staging quedó visible, en modo texturizado y en neutral.

## B. Archivos preparados

Los siguientes nombres reproducen las rutas finales bajo `src/main/resources/assets/aptitudes/`. En esta fase se escribieron bajo `assets/aptitudes/` de staging.

| Archivo provisional | Acción que requiere autorización |
|---|---|
| [Geometría](assets/aptitudes/geo/entity/faceless_colossus.geo.json) · `geo/entity/faceless_colossus.geo.json` | Respaldar y sustituir |
| [Animaciones](assets/aptitudes/animations/entity/faceless_colossus.animation.json) · `animations/entity/faceless_colossus.animation.json` | Respaldar y sustituir |
| [Textura de fase 1](assets/aptitudes/textures/entity/faceless_colossus.png) · `textures/entity/faceless_colossus.png` | Respaldar y sustituir |
| [Máscara de fase 1](assets/aptitudes/textures/entity/faceless_colossus_glowmask.png) · `textures/entity/faceless_colossus_glowmask.png` | Respaldar y sustituir |
| [Textura de fase 2](assets/aptitudes/textures/entity/faceless_colossus_phase2.png) · `textures/entity/faceless_colossus_phase2.png` | Añadir |
| [Máscara de fase 2](assets/aptitudes/textures/entity/faceless_colossus_phase2_glowmask.png) · `textures/entity/faceless_colossus_phase2_glowmask.png` | Añadir |

Staging también contiene el editable, este informe, [la validación y las huellas SHA-256](validacion.json) y trece capturas de Blockbench. El archivo `tools/models/blockbench/faceless_colossus.bbmodel`, los paquetes de Fase 6, los recursos de producción y los archivos de otros enemigos se conservaron.

## C. Geometría y aspecto

Medidas en reposo desde el JSON, con rotaciones y pivotes aplicados. La envolvente corporal incluye los brazos y el faldón. La capa y el espadón tienen su propia envolvente.

| Elemento | Producción actual | Reconstrucción |
|---|---:|---:|
| Altura corporal | 62 u / 3,875 bloques | 54,4 u / 3,4 bloques |
| Anchura corporal | 39 u | 27,14 u |
| Profundidad corporal | 11,7 u | 17,08 u |
| Profundidad con capa | 12,3 u | 20,61 u |
| Suelo | Y = 0 | Y = 0 en ambas botas |
| Huesos / cubos | 9 / 68 | 37 / 278 |
| Atlas | 512 × 512 | 512 × 512 por fase |
| Clips | 7 | 7 |
| Espadón | `sword` hijo de `right_arm` | 51,2 u, hijo de `right_hand` |
| Capa | Un hueso | `cape_root → cape_mid → cape_lower`, bajo `cape` |
| Fases visuales | Un atlas con emisión | Dos atlas con emisión sobre una sola geometría |

La anchura y la profundidad medidas superan en 1,54 u y 1,08 u las referencias de 25,6 u y 16 u. La mano armada adelantada influye en la envolvente. Las vistas verifican corona, visor hundido, coraza y núcleo, hombreras escalonadas, hebilla, faldón, botas, capa carmesí con cruz y espada. La pose neutral sujeta el espadón en diagonal, visible en la frontal y en tres cuartos.

**APROBADO** para exportación geométrica. GeckoLib 4.7.7 construyó los 37 huesos y los 278 cubos desde el JSON 1.12.0. Se comprobaron pivotes, rotaciones, jerarquía, ausencia de duplicados de cubos en sus huesos, correspondencia con el editable y las 1.668 caras UV. Se conservó `geometry.faceless_colossus` como identificador de producción y la caja visible aprobada del paquete anterior.

## D. Java y renderer

- `BossEntities.java` registra `aptitudes:faceless_colossus` y `faceless_colossus_spawn_egg`. `FacelessColossusEntity.java` contiene ataques, atributos, botín y respuesta de fase. `AnimatedBossEntity.java` conserva estado, golpe de servidor, postura, controladores y persistencia NBT. Se mantienen el registro, huevo, sello, aparición, recompensas y mecánicas.
- En `BossRenderers.register()`, la entrada del Coloso usa `DefaultedEntityGeoModel`, `GeoEntityRenderer` y `AutoGlowingGeoLayer`, con escala **1,0**. La ruta de geometría y animaciones se mantiene. El render base de GeckoLib usa una capa de recorte de alfa. Las caras UV están completamente pintadas en las dos texturas. La emisión se compone en una segunda pasada a plena luz mediante la capa existente, sin iluminación del entorno.
- **OBSERVADO**. La opción de seguimiento de mirada del modelo actual asigna X/Y de `head` después de procesar las animaciones. Propuesta en el modelo del Coloso dentro de `BossRenderers.java` para mantener el seguimiento durante movimiento libre y respetar `head` durante una acción disparada. Se comprobará especialmente en `stun`, `slam` y `phase_two`.
- **OBSERVADO**. La caja registrada mide 1,6 × 3,8 bloques y los ojos están en Y = 3,4. La corona del nuevo modelo está en Y = 3,4, y la caja sobresale 0,4 bloques por arriba. Recomendación para esta sustitución visual: conservar hitbox, ojos y escala. En partida conviene medir selección, colisiones en pasos estrechos y cobertura de ataques. El alcance de los ataques sigue gobernado por las reglas del servidor.

## E. Animaciones y combate

| Clip | Segundos / ticks del clip | Bucle | Huesos | Claves |
|---|---:|---|---:|---:|
| `idle` | 2,8 / 56 | Sí | 11 | 61 |
| `walk` | 1,4 / 28 | Sí | 25 | 619 |
| `phase_two` | 1,5 / 30 | Una vez | 26 | 1.225 |
| `sweep` | 1,4 / 28 | Una vez | 25 | 2.154 |
| `slam` | 2,0 / 40 | Una vez | 25 | 1.320 |
| `charge` | 1,4 / 28 | Una vez | 25 | 1.215 |
| `stun` | 4,9 / 98 | Una vez | 25 | 1.289 |

**APROBADO** para carga por GeckoLib. El deserializador de 4.7.7 cargó las siete claves sin prefijo y los 7.883 fotogramas clave. Se verificaron referencias a huesos, bucles, transformaciones finitas, pistas y los tiempos exactos de los cuatro clips corregidos. `move` se procesa antes de `action` en la versión fijada. La acción tiene prioridad por canal compartido. La geometría de la mirada y la superposición en canales ausentes requieren la prueba del renderer.

| Ataque | Aviso de servidor desde el disparo | Instante del clip inspeccionado | Referencia nominal con transición |
|---|---:|---:|---:|
| `sweep` | 18 ticks | 0,80 s / 16 ticks | 18 ticks |
| `slam` | 28 ticks | 1,30 s / 26 ticks | 28 ticks |
| `charge` | 16 ticks | 0,70 s / 14 ticks | 16 ticks |

`BossAttackGoal.tick()` dispara la animación al seleccionar el ataque. A partir del siguiente tick decrementa el aviso y llama a `strike` al llegar a cero. En ese mismo tick libera el ataque actual. La recuperación es visual y cada ataque conserva su enfriamiento de 30, 70 o 90 ticks. El cliente usa una transición de 2 ticks antes de avanzar por el clip. La coincidencia nominal presupone recepción a tiempo, un cliente que está renderizando la entidad y una transición normal. **NO VERIFICADO** en partida, incluidos latencia, interrupciones por otra acción, recuperación y mezcla al caminar. La embestida aplica velocidad en `strike` y el chequeo de daño de esa implementación exige que el objetivo ya esté a 3 bloques o menos.

Las mediciones previas en Blockbench conservan el pico de retorno de `sweep` en 28,1°/tick y sitúan el solapamiento brazo/coraza en 0,055 u, 0,043 u y 0,051 u en `phase_two`, `slam` y `stun`. La punta del espadón queda a 0,024 u del suelo en el impacto de `slam`. Las capturas de staging usan las mismas curvas y el rig texturizado. El aturdimiento dura 100 ticks en el servidor y el clip más la transición nominal suma 100.

## F. Dos fases, texturas y emisión

- **APROBADO** para los archivos. Cuatro PNG RGBA de 512 × 512, un atlas UV compartido y cero texeles transparentes en las caras. La inspección frontal, posterior y de tres cuartos confirma aplicación de la paleta, material en todas las piezas principales y espadón pintado. En UV hay 13.006 texeles distintos entre fases. Las máscaras tienen 265 y 3.631 texeles activos en caras, respectivamente. El resto del alfa activo está fuera de las caras mapeadas. GeckoLib toma el RGB de la textura base y usa la máscara para seleccionar y determinar el alfa de la emisión.
- **BLOQUEADO para integración hasta autorización**. El renderer actual carga una sola textura. La fase existente sube al caer a 2/3 y a 1/3 de vida, dispara `phase_two` en ambos ascensos y se conserva en NBT como `aptitudes_phase`. La reinvocación puede recuperarla mediante `restorePhase`. Hoy `phase` es un campo local del servidor y el disparo de animación no sincroniza el estado persistente. La fase visual 2 debe cubrir las fases de combate 2 y 3, y permanecer si el jefe recupera vida.
- Propuesta limitada al Coloso. En `FacelessColossusEntity.java`, publicar un booleano de fase visual mediante `DataTracker` a partir de `phase() >= 2`, al subir de fase, cargar NBT y restaurar fase por reinvocación. En la entrada del Coloso de `BossRenderers.java`, hacer que `getTextureResource` elija el atlas por ese valor. Mantener `AutoGlowingGeoLayer` para resolver el sufijo de la máscara de cada atlas. Probar también un cliente que empieza a seguir al jefe en fase 2 y la recarga del mundo.

## G. Evidencias y ejecución

- Fase 1 · [Frontal](evidencias/fase1-frontal.png), [posterior](evidencias/fase1-posterior.png), [tres cuartos](evidencias/fase1-isometrica.png) y [espadón](evidencias/espadon.png).
- Fase 2 · [Frontal](evidencias/fase2-frontal.png) y [tres cuartos](evidencias/fase2-isometrica.png).
- Poses · [Walk](evidencias/walk-0_35.png), [sweep impacto](evidencias/sweep-0_8.png), [sweep retorno](evidencias/sweep-1_125.png), [slam](evidencias/slam-1_3.png), [charge](evidencias/charge-0_7.png), [transición](evidencias/phase_two-1_05.png) y [stun](evidencias/stun-4_2.png).
- GeckoLib 4.7.7 cargó la geometría y las animaciones mediante sus clases de carga reales, con Minecraft 1.21.1 en el classpath. Validación de datos, sin renderizar en Minecraft.
- **APROBADO** para la compilación provisional. El wrapper 8.14.3 con Java 21 y Loom 1.10.5 completó `build --offline` en una copia temporal, tanto con los recursos base como con staging. Pasaron 526 pruebas en 86 suites. El JAR provisional contiene los seis recursos con las mismas huellas que staging. El primer intento desde el directorio real quedó limitado por acceso a red/caché. La ejecución aislada completó la compilación con las advertencias de deprecación de Gradle. [Registro de la compilación provisional](evidencias/gradle-staging.log).
- **APROBADO para arranque e invocación**. El cliente temporal arrancó con Fabric 0.16.14 y GeckoLib 4.7.7, abrió un mundo temporal y registró la invocación de The Faceless Colossus. El control de la ventana se detuvo al pulsar Escape. **NO VERIFICADO para la prueba visual completa**. En un mundo de prueba después de la integración, invocar `aptitudes:faceless_colossus`, verificar escala, pies, cursor, idle/walk, casco, texturas y máscaras. Enfrentar los tres ataques y comparar golpe visual con daño/sonido. Bajar la vida por debajo de ambos umbrales, forzar stun y guardar y recargar. Repetir con seguimiento tardío, reinvocación y varios Colosos. Revisar FPS y registros de GeckoLib.

## H. Protección frente a generadores

**OBSERVADO**. La ejecución normal de `tools/models/gen_boss_models.py` escribe la animación anterior del Coloso. Su `--check` pasa en producción actual y detecta seis relaciones de padre nuevas en staging. Tampoco escribe el geo ni la textura base del Coloso. Gradle y CI no invocan ese generador en la configuración inspeccionada.

Al autorizar la sustitución, se propone excluir la animación manual del Coloso de la salida generada y validar su contrato con la jerarquía nueva. `tools/textures/gen_faceless_glowmask.py` debe dejar la máscara pintada como fuente de verdad. `tools/textures/paint_faceless_additions.py` usa las UV antiguas, por lo que debe quedar limitado al arte legado. Estas protecciones se limitan a los archivos del Coloso. Conviene volver a ejecutar `--check` y confirmar que las salidas de los demás jefes no cambian.

## I. Siguiente cambio sujeto a aprobación

Respaldar con hashes los cuatro recursos actuales, sustituirlos y añadir los dos PNG de fase 2. Modificar solamente la entrada del Coloso en `BossRenderers.java`, la sincronización visual en `FacelessColossusEntity.java` y las protecciones descritas de los generadores. Revisar al aplicar si hay cambios concurrentes. Los registros, estadísticas, IA, hitbox y archivos de los demás enemigos quedan fuera de la propuesta.

## J. Resultado

**APROBADO** para staging de geometría, UV, PNG, compatibilidad de identificadores y carga de animaciones. **OBSERVADO** en generación y prioridad de la mirada. **BLOQUEADO** para sustitución y selección en partida de la fase 2 hasta recibir autorización. **APROBADO** el arranque y la invocación en el cliente temporal. **NO VERIFICADO** en Minecraft para render, emisión, sincronización visual y jugabilidad.

