# Pruebas de Aptitudes

## v52 — Persecución del Eclipse

1. En el Mundo Normal, dificultad normal y supervivencia, inicia `/aptitudes eclipse start`.
   En campo abierto, deja que una criatura de la oleada te vea. Dobla una esquina y cambia
   de dirección detrás de un muro. Debe intentar llegar al último punto visible.
2. Quédate oculto más de seis segundos, aléjate más de 48 bloques, vuelve a mostrarte y cambia de
   dimensión. Comprueba la caducidad de la búsqueda y la adquisición de un objetivo visible.
   Repite al morir, entrar en creativo/espectador, desconectar y reconectar.
3. Prueba con dos jugadores, primero juntos y luego separados. Oculta a uno y deja visible al
   otro. Repite con Soborno, camuflaje e invisibilidad, con un objetivo no jugador y cerca del borde de chunks
   cargados. Comprueba que los mobs ajenos al Eclipse conservan su IA y que el asedio no activa
   ninguna nueva acción sobre el terreno.
4. Reinicia durante el evento y prueba `/aptitudes eclipse stop`. La criatura cargada debe
   adquirir objetivos de nuevo según su entorno al reiniciar. Al terminar se disuelve por la
   ruta existente. Repite con el Heraldo en Totalidad.

`EclipsePursuitRulesTest` comprueba intervalos, alcance, llegada, caducidad y relojes extremos.
La navegación y la visibilidad se comprueban en partida.

## v51 — Mesa y Tablón del gremio

1. Abre la Mesa a escala de interfaz 4. Revisa la separación de la cuadrícula, resultado e inventario.
   Prueba arrastrar, Mayús + clic y recoger un lote de dos objetos. Con v48, abre Reforja desde el botón
   y comprueba que cabe debajo del resultado.
2. Abre el Tablón con tres ofertas y con el máximo de contratos activos. Incluye un contrato de entrega
   y una oferta legendaria con una recompensa larga. Verifica los rótulos, barras y botones en español
   y alemán. Al pasar sobre el objetivo, comprueba que se lean todas las recompensas.
3. Acepta, entrega, abandona y cobra en un mundo de prueba. Verifica que cada botón corresponda a su
   ficha y que los límites y cobros del servidor sigan aplicándose. Cierra y vuelve a abrir el Tablón.
4. Abre J. Pasivas, Sendas y Activas deben compartir el fondo. La cabecera de Sendas debe indicar
   que se elige una. Revisa que los nodos, conexiones, controles y su foco funcionen al hacer zoom.

## v50 — HUD e iconos del árbol

1. Abre J con varios roles. Recorre nodos base, legendarios y las filas Maestro y Definitivo.
   Comprueba sellos, marcas y estados bloqueados. Los iconos S1–S7 se conservan en las dos vistas.
2. Equipa dos activas de cada rol y prueba características, definitivas y legendarias.
   Compara los iconos del árbol con la barra. Agota maná, lanza una activa y espera su recarga.
   Comprueba candados, cuenta atrás, coste de referencia y destellos.
3. Activa una piedra angular y una mecánica simultáneamente. Comprueba las dos filas en
   1080p a escala de interfaz 4, en ventana estrecha y con escala HUD 50/100/200 %.
   Desde Mayús + O, arrastra a las cuatro esquinas, recarga recursos con F3 + T y reconecta.
   Comprueba que O, F1, espectador y Restablecer se comporten correctamente.
4. Ejecuta `.\gradlew.bat build` y `python -X utf8 tools/textures/gen_tree_atlas.py src/main/resources/assets/aptitudes/textures/gui/tree --check`.
   `AbilityHudLayoutTest` cubre tamaños, escala y desplazamientos fuera de rango.

## v47 — Asalto del Eclipse (fase 1)

```powershell
.\gradlew.bat build
python -X utf8 -m unittest discover -s tools/lang -p 'test_*_lang.py'
python -X utf8 tools/lang/check_lang.py
python -X utf8 tools/lang/gen_interface_lang.py --check
python -X utf8 tools/lang/gen_guide_lang.py --check
python -X utf8 tools/lang/gen_nodes_lang.py --check
python -X utf8 tools/lang/gen_evolved_lang.py --check
python -X utf8 tools/lang/gen_path_lang.py --check
```

Regresión automática: `event/EclipseAssaultRulesTest` (9 pruebas) comprueba participación de supervivencia,
oleadas por fase, presupuesto compartido y mundial, cero/negativos/extremos sin desbordamiento, composición
garantizada, desactivación por probabilidad cero, NaN/infinito y tipos que muestran armadura. Los pesos
originales siguen comprobados por `StrongEventRulesTest`. `config/EclipseAssaultConfigTest` (3 pruebas)
comprueba nuevos defectos, límites y ceros, y lectura de una configuración antigua sin las claves nuevas.
Las 35 pruebas Python y los cinco `--check` comprueban los once idiomas y sus catálogos; solo cambia
`guide.aptitudes.eclipse.body`, con el mismo orden, formato y marcadores. Español y sus seis variantes
deben ser idénticos. No hay claves nuevas, dependencias, mixins ni IDs de registro modificados.

Ejecutado por el agente: build final `BUILD SUCCESSFUL in 28s`, con 229 pruebas JUnit sin fallos,
errores ni omitidas. Las 35 pruebas Python, el comprobador de idiomas y los cinco `--check` pasan.
Un primer build detectó un error de sintaxis en el bloque del Heraldo; corregido antes de esta validación.
JAR: `build/libs/aptitudes-1.4.0.jar`; la versión pública se conserva. No se ejecutaron pruebas en partida.

Autorrevisión del código:

- `end` comprueba `state.active` antes de finalizar; repetir la llamada no vuelve a pagar XP, contratos
  ni logros. El estado se desactiva antes de pagar. Los muertos durante el evento siguen en el conjunto
  persistente `fallen`, identificado por UUID; morir, reconectar o reiniciar no lo limpia.
- Los nuevos refuerzos y el pago excluyen muertos, creativo y espectador. Esta fase no concede XP
  mediante comandos nuevos ni cambia la ruta administrativa de XP. Los comandos de evento existentes
  conservan sus permisos; iniciar por comando sigue simulando un evento con sus recompensas habituales.
- No hay consumo de objetos ni nuevas reclamaciones. La armadura equipada no se suelta; las recompensas
  normales de élites/campeones siguen siendo las existentes. Disolver una criatura para hacer hueco al
  Heraldo no causa una muerte ni concede XP/botín.
- Las garantías cuentan apariciones exitosas; un fallo de posición no registra una criatura como viva.
  Los modificadores usan IDs estables con quitar/añadir. Las peticiones de configuración siguen la
  validación y el control de permisos del servidor; el cliente no decide oleadas, objetivos ni atributos.
- Recuento restaurado después de cargar chunks iniciales del servidor, sin reaplicar atributos. Los
  límites frenan apariciones nuevas y no borran criaturas guardadas por recargar o bajar la configuración.
  Una migración antigua puede quedar temporalmente por encima del tope hasta eliminar criaturas.
- El presupuesto local depende de jugadores próximos y no de su grupo; miembros lejanos no suman
  refuerzos locales ni absorben el Heraldo de otra región. `setTarget` conserva el filtro común de roles.
- El premio de supervivencia conserva su condición anterior de estar en el Mundo Normal al finalizar;
  esta fase no introduce un mínimo de tiempo de participación ni rehace la economía de los contratos.

Pruebas manuales pendientes (cliente y servidor dedicado; guardar copia del mundo de prueba):

1. En superficie abierta, supervivencia, iniciar `/aptitudes eclipse start` como operador. Verificar
   4 criaturas por oleada de Penumbra y 8 en Totalidad, cada 30 s después de la primera espera. Retirada
   sin refuerzos. Verificar élites y campeones con equipo completo y cuerpos visibles de zombi/esqueleto.
2. Comprobar malla/hierro según fase, diamante en campeones y sus modificadores/barra. Matar unidades:
   no sueltan el equipo puesto; sí pueden dar su botín habitual, una sola vez. Medir dificultad con equipo
   inicial, Maestro y Definitivo, y el rendimiento económico de campeones garantizados.
3. Dejar vivas las criaturas: un jugador no debe recibir refuerzos por encima de 12 locales. Con dos
   jugadores próximos, con y sin grupo, comprobar el límite 24. Con jugadores dispersos y topes pequeños,
   comprobar el límite mundial y el orden rotativo, incluyendo Heraldos en el recuento.
4. Al mediodía, dos compañeros de grupo en regiones distintas reciben encuentros separados; próximos,
   comparten un Heraldo con vida escalada. Probar el tope mundial lleno: sustituye como máximo una
   unidad ordinaria por Heraldo, sin su botín. No reemplaza otro Heraldo. Sin suelo/altura segura no
   aparece sobre el jugador; no se reintenta el encuentro más tarde durante ese Eclipse.
5. Poner probabilidades de élite/campeón a cero: no aparecen esas garantías. Poner mínimos a cero:
   vuelve la tirada original. Poner tamaños/límites a cero y activar/desactivar armadura garantizada;
   probar valores enormes, negativos y NaN en probabilidades mediante archivo y pantalla de operador.
6. Creativo, espectador, pacífico, jugador muerto y otra dimensión no generan refuerzos. Probar Soborno
   y camuflaje: el objetivo inicial debe seguir respetando el filtro del rol.
7. Morir y reaparecer, reconectar y reiniciar a mitad del evento. Las criaturas cargadas cuentan antes
   de nuevas oleadas y se retiran al finalizar. Quien murió no cobra supervivencia; detener dos veces
   no paga, finalizar una vez no permite un segundo pago al detener o reconectar.
8. Descargar y recargar chunks con enemigos y bajar límites durante el evento: suspende refuerzos sin
   curar, duplicar ni reaplicar modificadores. Cambiar al Nether y volver tampoco duplica el Heraldo.
9. En los once idiomas, leer el capítulo de eventos completo y desplazar hasta su final. Comprobar
   cantidades, acentos, porcentajes y ausencia de claves visibles. La revisión visual no la sustituye JUnit.

## v49 — Mesa del gremio y Hoguera

1. En un mundo nuevo, fabrica una Mesa del gremio en una mesa de trabajo. Comprueba que la receta
   de Hoguera requiere la Mesa del gremio y que el Grimorio se fabrica allí en cualquier orden.
   Verifica que las dos recetas queden sin resultado en la mesa de trabajo. La Mesa del gremio
   tampoco debe dar resultados de recetas vanilla.
2. Fabrica una armadura de rol, una runa y una receta trasladada. Usa el grimorio para confirmar
   la estación y la disposición de ingredientes. Prueba un patrón con forma desplazado o reflejado
   y uno sin forma en varias posiciones de la cuadrícula.
3. Prueba clic, tecla numérica, doble clic, Mayús + clic y Q. Con inventario lleno y espacio para
   solo uno de los dos objetos, Mayús + clic en una receta de dos unidades debe conservar los
   ingredientes. Al liberar espacio, cada lote de dos debe gastar un solo juego de ingredientes.
   Con un datapack de prueba, comprueba que el cubo vacío vuelve a la cuadrícula o al inventario.
4. Abre la Mesa con dos jugadores. Rompe el bloque, aléjate, desconecta o muere con ingredientes
   dentro. Solo se deben devolver o soltar los ingredientes de cada sesión. Verifica que el
   tutorial avanza con fabricación válida y que los contratos de forja cuentan armaduras y runas,
   una vez por lote y solo para quien fabrica en supervivencia o aventura.
5. Coloca ambos bloques, recarga recursos y vuelve a entrar al mundo. Revisa modelos en inventario
   y colocados, llama animada, interacción con la Hoguera, Ofrenda, puesto del aldeano y atajos
   de runas y núcleos. Revisa el panel 3×3 y la cabecera al descansar a escala de interfaz 4.
   Tras integrar v48, prueba también los botones de Reforja y Templado.

`GuildRecipeResourcesTest` comprueba los tipos de receta sin iniciar Minecraft. El generador
`tools/textures/gen_workstations.py` admite `--check` sobre `src/main/resources/assets/aptitudes/textures/block`.

## v45 — Traducciones T8, fase 3 (nodos y sendas)

```powershell
python -X utf8 tools/lang/gen_interface_lang.py
python -X utf8 tools/lang/gen_guide_lang.py
python -X utf8 tools/lang/gen_nodes_lang.py
python -X utf8 tools/lang/gen_evolved_lang.py
python -X utf8 tools/lang/gen_path_lang.py
python -X utf8 tools/lang/gen_interface_lang.py --check
python -X utf8 tools/lang/gen_guide_lang.py --check
python -X utf8 tools/lang/gen_nodes_lang.py --check
python -X utf8 tools/lang/gen_evolved_lang.py --check
python -X utf8 tools/lang/gen_path_lang.py --check
python -X utf8 tools/lang/check_lang.py
python -X utf8 -m unittest discover -s tools/lang -p 'test_*_lang.py'
.\gradlew.bat build
```

- Las 2080 claves de esta fase se reparten en 600 de nodos base, 400 de evolución y 1080 de sendas.
  `node_catalog.py` mantiene las frases base completas, sin traducción automática por fragmentos ni
  respaldo en inglés. Las otras dos familias proceden de sus generadores originales ampliados.
- `gen_nodes_lang.py` exige cobertura exacta y conserva las fases anteriores. Los generadores de
  evolución y sendas admiten `--check` y `--locales pt_br fr_fr de_de`; por defecto incluyen inglés,
  español y los tres idiomas nuevos. `generator_cli.py` mantiene orden, LF y 2 espacios, y copia
  español a sus variantes. No duplica el párrafo antiguo de evolución al regenerar el grimorio actual.
- Las 12 pruebas nuevas verifican cobertura, ausencia de descripciones en inglés, números de cada
  descripción, conservación de otras fases, idempotencia, claves nuevas sin traducir, textos vacíos,
  porcentajes rotos, duplicados, nombres faltantes, bifurcaciones, S6 ×1.5, plantillas S7 y caracteres
  invisibles accidentales. La regeneración de inglés y español se compara byte a byte, incluidas variantes.
- Resultado local del 2026-11-06: `BUILD SUCCESSFUL in 42s`, 217 pruebas JUnit sin fallos, errores ni
  omitidas; 35 pruebas Python aprobadas. Los tres idiomas tienen las 3512 claves de `en_us` en el mismo
  orden y con los mismos marcadores, incluidos argumentos repetidos, índices y porcentajes escapados.
- Autorrevisión: solo cambian recursos de idioma en el mod; recompensas, consumo, XP, modos de juego,
  muerte, reconexión, reinicio, grupos, aliados y límites del servidor conservan su lógica. No se añaden
  configuraciones ni se altera el balance. Los ocho idiomas anteriores permanecen idénticos a la base.
  Comparación del JAR final con T8 fase 1: mismos archivos, 362 clases idénticas byte a byte y solo
  tres recursos distintos (`pt_br.json`, `fr_fr.json`, `de_de.json`).

Pruebas en partida y revisión con hablantes nativos pendientes (traducción inicial):

1. En cada idioma, abrir J y recorrer los veinte roles: nodos base, estadísticas, mecánica, reactivas,
   activas y legendario. Revisar números, porcentajes y tecla H; las claves internas no deben verse.
2. Evolucionar un personaje de prueba y revisar los diez nodos de Maestro/Definitivo por rol: nombres,
   efectos, requisitos y tooltips. Comprobar que cada fila conserva sus valores anteriores.
3. Abrir Sendas para cada rol y las tres pestañas. Revisar S1–S7, ambas opciones de S5, el aumento de S6
   y las diez plantillas de piedra angular. Comparar los valores con la configuración y `docs/BALANCE.md`.
4. Usar escala 4 y resolución 426×240: comprobar recorte de nombres en listados y lectura de detalles.
   Se conserva la distribución y el ajuste de texto existentes; esta fase no atribuye una prueba visual al agente.
5. Regenerar con los comandos anteriores y ejecutar los cinco `--check`: no debe cambiar ningún archivo.
   Los nombres compartidos entre idiomas (p. ej., Kensei, Shuriken o nombres musicales) son intencionados;
   ninguna descripción de nodo/senda conserva el inglés como texto provisional.

## v45 — Traducciones T8, fase 2 (grimorio)

Registro de la fase 2; la fase 3 anterior completa sus nodos pendientes.

```powershell
python -X utf8 tools/lang/gen_guide_lang.py
python -X utf8 tools/lang/gen_guide_lang.py --check
python -X utf8 tools/lang/gen_interface_lang.py --check
python -X utf8 tools/lang/check_lang.py
python -X utf8 -m unittest discover -s tools/lang -p 'test_*_lang.py'
.\gradlew.bat build
```

- `guide_catalog.py` traduce las 46 claves de los 23 capítulos a `pt_br`, `fr_fr` y `de_de`.
  `gen_guide_lang.py` exige cobertura completa y conserva interfaz, objetos y nodos de otras fases.
  El párrafo de filas evolucionadas procede de `GUIDE` en `gen_evolved_lang.py`, conservando Unicode.
- Las ocho pruebas nuevas comprueban cuerpos traducidos, nuevas claves sin traducción, claves de más,
  cuerpo vacío, marcadores alterados, conservación de las demás fases y del orden, regeneración
  idempotente y presencia única del párrafo generado. CI ejecuta las 23 pruebas Python y ambos `--check`.
- Resultado ejecutado el 2026-11-06: `BUILD SUCCESSFUL in 13s`, 217 pruebas JUnit con cero fallos,
  errores u omitidas; 23 pruebas Python aprobadas. Los tres JSON tienen exactamente las 3512 claves,
  el orden y los marcadores del inglés; 1432 claves traducidas por idioma en las fases 1–2.
  Las 2080 claves de nodos/sendas conservan el inglés hasta la fase 3.
- Frente a fase 1, solo cambian valores del grimorio: 46 en portugués/alemán y 45 en francés, donde
  el título «Structures» coincide con el inglés. Interfaz, objetos y nodos se conservan; los ocho idiomas
  anteriores son idénticos byte a byte. No se añaden claves ni se reformatean archivos completos.
- Comparación del JAR con la fase 1: mismos archivos y 362 clases idénticas byte a byte; solo cambian
  `assets/aptitudes/lang/{pt_br,fr_fr,de_de}.json`. La autorrevisión no identifica cambios de recompensas,
  consumo, XP, creativo/espectador, muerte, reconexión, reinicio, grupos, aliados o límites del servidor:
  la lógica de juego y la persistencia conservan exactamente su código anterior.

Ancho comprobado estáticamente con los glifos de la fuente predeterminada de Minecraft 1.21.1:

| Idioma | Cabecera más ancha, con margen conservador para negrita | Palabra más ancha del cuerpo |
|---|---|---|
| `pt_br` | 176 px | 92 px |
| `fr_fr` | 166 px | 90 px |
| `de_de` | 163 px | 119 px |

Se usa el panel mínimo de 380 px: 192 px reservados para cabecera e ilustración y 224 px para el
cuerpo. Todas las cabeceras caben; las líneas se parten dentro de esos 224 px, sin palabras demasiado
largas. `GuideScreen` ya desplaza el cuerpo y las recetas de alquimia; no se cambia su distribución.
El índice conserva su recorte existente a 107 px (uno/tres/tres títulos largos respectivamente).
En 426×240 hay más ancho disponible que en esta medición. No es una prueba visual en partida.

Pruebas manuales pendientes (traducción inicial que conviene revisar con hablantes nativos):

1. En cada idioma nuevo, abrir los 23 capítulos a escala de interfaz 4 y en 426×240. Revisar cabeceras,
   índice, acentos y porcentajes; recorrer hasta la última línea con rueda y teclado. No deben verse claves.
2. Revisar los siete escalones, la bifurcación y las diez piedras angulares; contrastar los valores con
   `docs/BALANCE.md`. Leer también logros, contratos y los tres eventos completos.
3. En Alquimia, llegar al final y comprobar las recetas sincronizadas y los tooltips de las pociones;
   pulsar los pétalos para su extracción. En los otros capítulos, abrir las fichas y volver con Retroceso.
   Los textos nuevos remiten a las recetas del juego y no mantienen listas propias de ingredientes.
4. Regenerar interfaz y grimorio, ejecutar ambos `--check` y el comprobador global: no debe cambiar nada.
   Revisar la calidad de los tres idiomas con hablantes nativos antes de presentarlos como completos.

## v45 — Traducciones T8, fase 1

Registro de la fase 1; la fase 2 anterior sustituye su estado provisional del grimorio.

```powershell
python tools/lang/gen_interface_lang.py
python tools/lang/gen_interface_lang.py --check
python tools/lang/check_lang.py
python -m unittest discover -s tools/lang -p test_check_lang.py
.\gradlew.bat build
```

- Los tres JSON tienen las 3512 claves de `en_us`, en el mismo orden, con 2 espacios y LF.
  Se traducen 1386 claves de interfaz/objetos; `guide.aptitudes.*` (46) y
  `node.aptitudes.*` / `path.aptitudes.*` (2080) conservan provisionalmente el inglés.
- El catálogo `ui_catalog.py` cubre cada clave de la fase 1, salvo los rótulos generados: se mantienen
  en `UI` de `gen_evolved_lang.py` y `gen_path_lang.py`. La traducción completa de sus nodos se hará en
  la fase 3. El generador de interfaz conserva las traducciones de fases posteriores al regenerar.
- El comprobador rechaza JSON duplicado, claves ausentes/sobrantes, distinto orden, valores no textuales,
  pérdida de argumentos, cambios de índice, conversión, anchura o precisión y porcentajes perdidos.
  Conserva `%%` y `%` literal. Permite reordenar argumentos numerados, pero conserva la secuencia de
  los no numerados. Las pruebas mutan cada caso y verifican la regeneración sin borrar fases posteriores.
- GitHub Actions ejecuta pruebas del comprobador, validación y `--check` antes del build.
  Los ocho idiomas anteriores, IDs, balance y código de juego permanecen intactos.

Ejecutado en esta fase: build `BUILD SUCCESSFUL in 1m 14s`, 217 pruebas JUnit sin fallos, errores ni
omitidas; 15 pruebas Python aprobadas; comprobador y `--check` correctos. El JAR contiene exactamente
los tres JSON nuevos adicionales y conserva byte a byte las clases y recursos del JAR de T7.
Medición estática con glifos vanilla: el paso más ancho del tutorial ocupa 189/205/194 px en
pt_br/fr_fr/de_de (210 disponibles), y el aviso de Amenaza 115/101/120 px (143 disponibles en 426×240).
Estas medidas no sustituyen la prueba visual ni la revisión con hablantes nativos.

Prueba visual pendiente (traducción inicial que conviene revisar con hablantes nativos):

1. Elegir Português (Brasil), Français y Deutsch; abrir K/J/U/P, accesorios, configuración, Mayús + O,
   Hoguera, inventario, Mesa del gremio, tablón y avances. No deben aparecer claves crudas.
2. Comprobar botones y valores a escala de interfaz 4 y en 426×240: textos largos se recortan o parten
   según el comportamiento existente; tooltips de objetos, runas y efectos conservan todos los argumentos.
3. Dos jugadores con idiomas distintos: invitaciones, contratos, recompensas y tutorial son personales;
   no hay cambios de estado por cambiar idioma. Morir, reconectar y reiniciar conserva el progreso previo.
4. Mostrar avisos de eventos, combos, piedras angulares, maestría y amenaza. Revisar argumentos repetidos,
   números, porcentajes y caracteres acentuados. No modificar reglas ni generar XP para probar textos.
5. El grimorio y los nodos/sendas aún se muestran en inglés: es una fase pendiente, no una clave perdida.
   Regenerar y repetir `--check`: no altera idiomas previos ni claves de etapas ya traducidas.

## Validación de la candidata 1.4.0 (T7, 2026-11-06)

El autor confirmó en esta fecha sus pruebas en partida de **T4–T6** (tutorial, alquimia, maestría y
amenaza). Esta confirmación complementa la de v15–v19 y v29–v41 del 2026-11-03. Los guiones siguientes
se conservan para regresión; no se atribuye al agente la ejecución en partida de cada caso ni se marcan
como ejecutados los guiones de T1–T3 que no han recibido una confirmación específica.

Ejecutado por el agente: `.\gradlew.bat build` → `BUILD SUCCESSFUL in 1m 10s`, con 217 pruebas,
cero fallos, errores u omitidas. Verificado `fabric.mod.json` dentro de `aptitudes-1.4.0.jar` y comparado
con el JAR de T6: mismos archivos, clases y recursos; solo cambia la versión de ese metadato.

1. Ejecutar `.\gradlew.bat build` desde `codex/publicar-1.4.0` y comprobar las pruebas y la generación de
   `build/libs/aptitudes-1.4.0.jar`. El archivo `aptitudes-1.4.0-sources.jar` no se instala en el juego.
2. Abrir el JAR como ZIP y revisar `fabric.mod.json`: `id=aptitudes`, `version=1.4.0`, Minecraft 1.21.1,
   Java 21, Fabric Loader y Fabric API; presencia de entradas de cliente/servidor, idiomas y recursos.
3. En la base T7, «Sin publicar» estaba vacío antes de `[1.4.0] — 2026-11-06`; las novedades posteriores
   de T8 se anotan en esa sección. Comprobar la fecha, la descripción pública de lo integrado en `main`
   y las limitaciones conocidas del README.
4. Al instalar la candidata, usar el JAR normal en cliente y servidor con Fabric API para 1.21.1.
   Para futuras regresiones, repetir los casos de esta guía sobre una copia del mundo. Registrar por
   separado quién ejecutó cada comprobación; T7 solo cambia versión y documentación, sin reglas nuevas.

## Pruebas automatizadas (nuevas)

v43 — maestría, T6 fase 1:

```powershell
.\gradlew.bat test --tests dev.aptitudes.logic.MasteryRulesTest --tests dev.aptitudes.data.MasteryProgressCodecTest --tests dev.aptitudes.config.MasteryConfigValidationTest
.\gradlew.bat build
```

- `MasteryRulesTest`: curva creciente/constante, umbrales exactos, concesiones pequeñas frente a una grande,
  lecturas sin duplicación, saturación en long, bonus con topes y no finitos, exceso elegible y coste fraccional.
- `MasteryProgressCodecTest`: codec real con JsonOps, claves separadas de habilidades/roles, guardado y carga,
  negativos, valores enormes, claves inválidas, máximo de entradas y aislamiento entre jugadores.
- `MasteryConfigValidationTest`: cuatro valores por defecto y sus límites, NaN, negativos y desactivación.
  Son clases puras. El codec completo de `PlayerProgress` carga enums con registros de Minecraft y requiere
  partida; no se afirma haber probado su migración con JUnit simple.

Guion de regresión en servidor dedicado y dos clientes (copia de un mundo anterior):

1. Cargar datos sin campo `mastery`: conserva niveles, XP, atributos, alma y roles, con maestría a cero.
   Para acelerar las pruebas se pueden usar los campos nuevos `masteryXpBase=1`, `masteryXpStep=0`;
   restaurar 1000/250 al terminar. No cambiar otros valores de balance.
2. `/aptitudes admin setlevel @s mining 49` y `/aptitudes admin addxp @s mining 2147483647`:
   llega al máximo sin maestría. Repetir al máximo y usar `/xp` no genera puntos. Una nueva acción
   legítima sí convierte solo su XP. Probar también el cruce natural del máximo con exceso de XP.
3. Entrenar las diez habilidades al máximo, una por una: verificar efectos descritos en `BALANCE.md`.
   En Minería, Excavación y Tala la predicción cliente debe coincidir con la rotura del servidor, sin
   bloques que reaparezcan. Agricultura/Pesca nunca superan 100 % de probabilidad; Encantamiento conserva
   el reembolso anterior. No se amplifican hitos ni se añaden niveles, puntos de atributo o árbol.
4. `/roles admin setlevel @s mage 50` y `/roles admin addxp @s mage 2147483647`: cero maestría de esa XP.
   Usar una activa con éxito genera XP nueva; sin objetivo, en espera o sin energía, no. Con +10 % de
   eficiencia, comprobar gasto real 20/1,10, incluyendo fracciones, definitiva y característica. Las
   legendarias y objetos con consumo independiente conservan su coste; el rango y la espera no cambian.
5. Solo y en grupo: el atacante al máximo recibe su XP una vez y los integrantes su parte existente,
   cada uno en su maestría propia. No se vuelve a repartir al convertirla. Dos jugadores con distintos
   roles/niveles: no copian ni borran el progreso ajeno. El fuego amigo conserva el filtro anterior.
6. En creativo/espectador no se gana maestría, incluso con `awardXpInCreative=true`. Con una baja en
   creativo y un aliado superviviente, tampoco se convierte la parte compartida en maestría. Repetir
   acciones, volver a supervivencia y esperar las lecturas de estadísticas de Pesca/Encantamiento/Agilidad.
7. Morir, reconectar, reiniciar y recargar chunks: XP/puntos intactos, sin reconvertir el mismo saldo.
   Una flecha existente no recibe de nuevo el multiplicador al recargar. Reasignar atributos o cambiar
   de rol conserva la maestría. Los resets administrativos borran el historial indicado en `BALANCE.md`.
8. Editar las cuatro opciones por archivo/pantalla: límites, NaN y valores enormes seguros. Desactivar
   deja pasivas/costes en su valor previo y conserva el saldo; reactivar no añade XP. Elevar el máximo
   pausa el bonus hasta alcanzarlo. Un cliente sin permisos no puede modificar estas reglas.
9. K: 430×262, diez tarjetas 5×2, M y número en dorado sin solapar nombre/hitos; tooltips de puntos,
   bonus y XP. Resumen principal/secundario en la cabecera. Comprobar español/inglés y seis variantes,
   escala de interfaz 4 y desplazamiento de la ventana en 426×240. No debe aparecer ninguna clave cruda.
   Comprobación estática con glifos vanilla: M1000 ocupa 30 px y la cabecera de roles 95 px en ambos
   idiomas; nombre, cinco hitos y marca disponen de reservas separadas. La inspección en partida corresponde al autor.
10. Probar varias capas de daño (arma, Combate, rol, combo) y confirmar que el tope global existente se
    sigue aplicando. La maestría no introduce multiplicadores después de ese tope.

v43 — Amenaza, T6 fase 2:

```powershell
.\gradlew.bat test --tests dev.aptitudes.logic.ThreatRulesTest --tests dev.aptitudes.data.ThreatDataCodecTest --tests dev.aptitudes.config.ThreatConfigValidationTest
.\gradlew.bat build
```

- `ThreatRulesTest`: umbrales exactos, X/Z/diagonales, aparición desplazada, coordenadas negativas,
  distancias enormes/no finitas, límites, exclusiones, probabilidades, tope de daño y cobro único elegible.
- `ThreatDataCodecTest`: ida/vuelta JsonOps, instantánea y cobro conservados, datos vacíos/incompletos,
  negativos, enormes y no finitos acotados. La llamada repetida a `claim()` mantiene el estado cobrado.
- `ThreatConfigValidationTest`: siete opciones con sus valores por defecto y límites, desactivación,
  cero, negativos y NaN/infinitos; `eliteChance=0.05` sigue intacto.
- No se cargan entidades ni registros con JUnit simple. La integración de atributos, botín, red y HUD
  requiere Minecraft; la confirmación de partida es del autor, no una ejecución en partida del agente.

Guion de regresión en una copia de mundo, servidor dedicado y dos clientes:

1. Fijar `/setworldspawn 0 64 0`; en supervivencia ir a X/Z 999/0, 1000/0, 2000/0, 3000/0, 4000/0,
   5000/0 y 9000/0: aviso zona tranquila/I/II/III/IV/V/V. Comprobar -1000/0, 0/-1000 y diagonal
   600/600 (zona tranquila). Cambiar Y no altera el nivel; desplazar la aparición traslada los umbrales.
   El aviso aparece en hasta 20 ticks, dura cuatro segundos y no se repite mientras se permanece en el anillo.
2. Crear zombis nuevos sin nombre en cada anillo y consultar `/attribute <uuid> minecraft:generic.max_health get`:
   20/23/26/29/32/35 sin élite ni otros efectos. Vida inicial completa y un solo id `aptitudes:threat/health`.
   Un monstruo antiguo con `aptitudes.elite_checked` y sin attachment no cambia al recargar el chunk.
3. Herir una criatura, descargar y cargar el chunk, reconectar y reiniciar: misma vida actual y máxima,
   anillo y multiplicadores. Transportarla cerca de la aparición no recalcula ni vuelve a sortear élite.
   Al desactivar `threatEnabled` desaparece el aumento de vida en hasta 20 ticks y se anulan daño/botín;
   al reactivar no se cura ni se repite recompensa. Los parámetros nuevos solo afectan a nuevas apariciones.
4. Probar golpes de zombi, flechas de esqueleto, magia con atacante y explosiones de creeper:
   daño ×1,5 en V antes de defensas, una sola vez, sin superar el `maxDamageMultiplier` positivo.
   No cambia el daño de un jugador contra otro ni el de esbirros/invocaciones aliados. Restituir la
   configuración tras las pruebas del tope. Dos clientes en anillos distintos reciben sus propios avisos.
5. Nombres previos (`CustomName`), Wither/Dragón/Warden/Guardián anciano, jefes de estructuras, esbirros
   y criaturas del Eclipse/Niebla/Lluvia: no reciben atributo, daño ni botín de Amenaza. Nether y End no
   escalan. Nombrar un monstruo ordinario ya escalado retira su atributo en hasta 20 ticks, sin curarlo.
   Un élite generado naturalmente conserva el anillo pese a su nombre generado; sin otros modificadores
   de vida, el élite de V tiene 55 de vida, no 70. No se acumulan escalados de eventos.
6. Para comprobar botín sin azar, usar temporalmente `threatLootPerRing=0.2` y criaturas nuevas en V:
   una sola tirada adicional por baja de jugador en supervivencia, tabla real/contexto de Saqueo y fortuna.
   Repetir con tabla personalizada o vacía: se respeta, sin caer en la tabla predeterminada al morir.
   No duplica equipo, XP vanilla/de rol, recompensas de élite, contratos ni logros. Guardar/cargar no
   habilita otra tirada. En grupo los objetos físicos aparecen una vez y la XP conserva el reparto previo.
7. Bajas en creativo, espectador, por entorno/comandos o con `doMobLoot=false`: cero botín adicional.
   Volver a supervivencia no habilita otro cobro. Muerte impedida por otro sistema no paga; la siguiente
   muerte real solo puede pagar una vez. No consume objetos ni XP de jugadores.
8. Probar la única tirada natural de élite: base 5 %, V 11,25 % (campeones: 10 % de esa probabilidad).
   Recargar chunks/reiniciar no repite la tirada. Con Amenaza desactivada o anillo 0, base anterior intacta.
9. Pantalla/archivo de configuración: límites de los siete campos, negativos, no finitos y enormes;
   cliente sin permisos rechazado. `threatMaxRing=2` no produce III–V, tamaño mínimo 100.
10. HUD en ocho idiomas, F1, `hudVisible=false`, muerte, creativo/espectador, cambio de dimensión,
    desconexión y nuevo servidor: no quedan avisos de otra sesión. Entrada al Mundo Normal avisa el
    anillo actual. Probar 426×240 y escala 4 con tutorial, cinco miembros, combo y Eclipse activos;
    el aviso ocupa una línea a la derecha, por encima del Eclipse, y se recorta si falta ancho. Se aparta
    del tutorial si su escala lo desplaza. Verificación estática de glifos vanilla: máximo 89 px en inglés
    y 121 px en español, frente a 143 px disponibles en 426×240. Los rótulos cortos mantienen caja mínima
    de 60 px y no deben desaparecer. Esta medición no sustituye la inspección visual en partida.

v44 — tutorial, T4 fases 1 y 2:

```powershell
.\gradlew.bat test --tests dev.aptitudes.tutorial.TutorialRulesTest --tests dev.aptitudes.tutorial.TutorialDataCodecTest
.\gradlew.bat test --tests dev.aptitudes.tutorial.TutorialViewTest
.\gradlew.bat build
```

- `TutorialRulesTest`: orden de seis claves estables, acciones adelantadas, cobros únicos, recompensas
  pendientes, progreso previo, omisión irreversible, listas corruptas, aislamiento entre jugadores y
  umbral de cinco golpes (incluidos enteros extremos).
- `TutorialDataCodecTest`: codec real con `JsonOps`, campos ausentes, ida y vuelta de observaciones y cobros,
  claves desconocidas/duplicadas, omisión persistente y ausencia de un segundo pago después de recargar.
  Las pruebas son puras y no arrancan registros de Minecraft.

Guion de regresión manual (supervivencia, dos jugadores y copia de un mundo anterior):

1. Entrar con un jugador nuevo y obtener el grimorio (también vale el de bienvenida): una esmeralda.
   Cambiar el libro de ranura, usarlo y reconectar no debe repetirla. El servidor revisa inventarios cada 20 ticks.
2. Comprar un nivel de alma en una Hoguera: segunda esmeralda. Petición inválida, XP insuficiente,
   descansar sin subir o asignar solo puntos libres no completan este paso. `/xp` por sí solo tampoco;
   el tutorial reconoce la compra real, no puede conocer el origen de la XP vanilla usada para pagarla.
3. Elegir rol principal, aprender/equipar una activa y usarla con éxito: una esmeralda por cada paso.
   Un rol secundario sin principal, repetir la misma elección, una ranura inválida, falta de maná,
   enfriamiento o habilidad sin efecto no deben inventar progreso. Característica y definitiva no son este paso.
4. Encadenar cinco golpes válidos a criaturas: una esmeralda al quinto, no al remate siguiente.
   Golpes bloqueados, proyectiles, jugadores e invocaciones no deben contar como combo nuevo. Repetir no paga.
5. Fabricar una receta de la Mesa del gremio y retirar el resultado (clic normal y Mayús + clic): última
   esmeralda. Solo previsualizar el resultado o mover ingredientes no completa el paso. Repetir la fabricación
   no vuelve a pagar; confirmar que los ingredientes y el resultado siguen funcionando como antes.
6. Hacer pasos fuera de orden: se recuerdan pero el pago espera a completar los anteriores. Llenar el
   inventario sin huecos ni espacio para esmeraldas, completar un paso y reiniciar: liberar un hueco debe
   entregar la recompensa pendiente una vez. Morir con pasos hechos/cobrados no debe borrar el historial.
7. En un mundo anterior, entrar con rol principal y nivel de alma > 0: se reconocen sin repetirlos, incluso
   si falta el grimorio (su recompensa y las siguientes esperan a recuperarlo). La migración solo ocurre
   al crear el attachment. También puede leer estados antiguos al entrar en creativo/espectador: no hay
   información histórica de su origen; ningún objeto se entrega hasta volver vivo a un modo elegible.
8. Tras inicializar el tutorial, realizar acciones nuevas en creativo/espectador: no avanzan ni pagan.
   Volver a supervivencia no transforma esos eventos en premios. El grimorio sí se detecta por posesión real.
   Comprobar que el segundo jugador y el grupo no reciben el progreso ni las esmeraldas del autor.

La fase 2 añade HUD y comandos sobre el mismo estado. `TutorialViewTest` prueba lecturas sin cobros,
acciones adelantadas, vista de premios pendientes, límites de índices y omisión con el codec real.
El paso de combo depende de `combosEnabled` y `comboFinisherHits >= 5`;
no se cambian esos valores. Las pruebas puras no verifican la inserción real en inventarios, los ganchos
de Fabric ni el guardado/copias reales de jugadores: esas comprobaciones requieren la partida anterior.

Fase 2 — guion de regresión manual (cliente y servidor dedicado, ocho idiomas):

Comprobación estática con fuente bitmap vanilla: los textos del recordatorio ocupan como máximo
171 px en español y 170 px en inglés (210 disponibles). El contenido de Mayús + O mide 224 px de alto
y cabe en 426×240. No sustituye la inspección visual en partida.

1. Entrar con dos jugadores: cada HUD muestra su propio objetivo. Completar un paso y comprobar
   la actualización inmediata; sin cambios de estado no se reenvían paquetes cada segundo.
   Reconectar, cambiar de servidor y morir/reaparecer: no queda el estado del otro jugador/servidor.
2. Mayús + O → Tutorial desactiva y reactiva solo el aviso. Reiniciar cliente conserva la elección;
   con `showTutorial` ausente o `null`, queda activado. F1 y ocultar todo el HUD también lo ocultan.
   En creativo/espectador, pantallas abiertas o muerto no aparece; vuelve al regresar a supervivencia.
3. Ejecutar `/aptitudes tutorial` y `status` repetidamente: informa pasos, cobros y siguiente objetivo,
   sin insertar esmeraldas, consumir objetos ni modificar XP. La consola no actúa por un jugador;
   argumentos ajenos o un objetivo de otro jugador son rechazados por Brigadier.
4. Inventario lleno y pasos observados sin cobrar: `status` informa premios pendientes. Completar todos
   los objetivos muestra el aviso de espacio en el HUD; liberar un hueco entrega una sola vez y lo oculta.
5. Con premios pendientes, ejecutar `skip` y repetirlo: omisión permanente, sin cobros y conservando
   recompensas anteriores. Liberar huecos, hacer acciones, morir, reconectar y reiniciar no reactivan nada.
   Repetir en creativo/espectador. El otro jugador y los integrantes del grupo no se alteran.
6. Escala de interfaz 4 y 426×240: los siete interruptores, escala, ayuda y botones de Mayús + O caben;
   la previsualización de la barra se dibuja detrás de los botones. El recordatorio queda a la derecha,
   bajo los efectos de estado; revisar con grupos, eventos, barras Souls y sus dos modos, sin solapamientos.
   Los seis objetivos y el aviso de espacio deben leerse completos, sin claves visibles.

T3 — textos y encaje del grimorio:

- Verificar JSON válido y sin claves duplicadas en `en_us` y `es_es`; cada capítulo registrado en
  `GuideScreen.CHAPTERS` necesita `.title` y `.body`. Las seis variantes regionales deben ser idénticas
  byte a byte a `es_es`. T3 modifica siete claves por idioma y no reformatea los archivos completos.
- Medición estática con los glifos bitmap de la fuente vanilla de Minecraft 1.21.1: a W=380,
  página de 234 px, cuerpo de 224 px y cabecera con 196 px libres antes de la ilustración. El título
  «Sendas y piedras angulares» ocupa 169 px en negrita; «Paths and keystones», 124 px. Los cuerpos
  usan `textRenderer.wrapLines` y el desplazamiento existente para acceder al texto completo.
- Comprobar alturas H=236..300: `24 + 22 × rowH <= H - 8`; en la ventana mínima, 22 filas de 9 px.
  Los iconos del índice se ajustan a la fila. Ejecutar `.\gradlew.bat build`.

Prueba manual de T3 pendiente (español e inglés):

1. Abrir el grimorio a 426×240 y en 1080p con escala de interfaz 4. Seleccionar con ratón y flechas los
   capítulos finales «Sendas y piedras angulares» y «Logros»; todo el índice debe quedar dentro de la ventana.
2. Revisar la cabecera larga y recorrer con la rueda ambos cuerpos hasta el final. Comprobar las diez
   plantillas, S7 al 50, las dos alternativas S5 y los porcentajes con un único signo `%`, sin claves visibles.
3. Abrir «Eventos del mundo»: Niebla solo con no muertos, meteoritos grandes y enormes, criaturas del Nether,
   armadura por fase y campeones del Eclipse. Revisar también «Árbol de talentos» (nivel 50, 66 puntos).
4. Volver al Recetario y abrir una ficha: las recetas deben seguir leyéndose del juego y conservar su navegación.
   Contrastar «Logros» con la pestaña de avances «Aptitudes» y «Sendas» con la vista del rol seleccionado.

T2 — persistencia de contratos y grupos (sin iniciar un cliente ni un servidor):

```powershell
.\gradlew.bat test --tests dev.aptitudes.contract.ContractDataCodecTest --tests dev.aptitudes.party.PartyStateNbtTest
.\gradlew.bat build
```

- `ContractDataCodecTest` usa `ContractData.CODEC` con `JsonOps` y texto JSON intermedio. Comprueba
  ofertas completas, progreso parcial/completo, reputación, contador de completados y marcas de ofertas
  aceptadas. Tras recargar, una oferta cobrada no se acepta ni se cobra otra vez en el mismo día; el
  cambio de día conserva los contratos activos. Cubre JSON vacío, progreso ausente, índices extremos,
  negativos, duplicados y listas mayores que los límites, sin imponer límites nuevos al constructor.
- `PartyStateNbtTest` escribe y carga `NbtCompound` con varios grupos y compara líderes, miembros,
  nombres (incluidos caracteres acentuados), fuego amigo y reparto. Cubre NBT vacío/incompleto, tipos
  incorrectos, UUID ausentes, porcentajes extremos, miembros repetidos y máximo de cinco integrantes.
  Regresión: el líder preinsertado por el constructor debe recuperar su nombre guardado, y las entradas
  repetidas no deben reemplazar el primer nombre ni duplicar la pertenencia.
  Invoca el lector privado mediante reflexión para evitar añadir una API principal solo para las pruebas;
  el `WrapperLookup` es nulo porque estos campos no consultan registros.

Comprobación manual complementaria pendiente: con dos jugadores, aceptar y avanzar contratos distintos,
cobrar uno y formar un grupo con nombres y opciones reconocibles. Reconectar y reiniciar el servidor:
deben conservarse el progreso y el grupo, incluida la presentación del líder desconectado; la oferta ya
cobrada no debe poder aceptarse de nuevo ese día. Morir con un contrato activo debe conservarlo mediante
`copyOnDeath`. Estas pruebas unitarias no ejercitan inventarios, concesión real de XP/recompensas ni
el ciclo de vida de jugadores en creativo/espectador o de los attachments de Fabric.

Iconos de Sendas (incluida T1 fase 1, S1–S3; Python 3 + Pillow, sin dependencias Java nuevas):

```sh
python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node --preview build/previews/node-icons-s1-s7.png
python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node --check
```

En Windows puede usarse `python` si el alias `python3` no está disponible. `--check` es de solo lectura:
exige los 480 PNG RGBA de 16×16 y falla ante archivos faltantes, ilegibles o con tamaño/modo incorrecto.
El generador exige los 20 roles en `paths(...)` y `deeper(...)`, sin duplicados ni efectos desconocidos.
Comprobar que hay 180 ids `<rol>_<a|b|c><1|2|3>` nuevos y que sus efectos proceden de `paths(...)`,
no de `deeper(...)` (por ejemplo, `berserker_b1` es Espinas y `berserker_b4` es Reducción de daño).
La hoja ×4 tiene 20 filas y 24 columnas: ocho iconos por senda, en orden S1/S2/S3/S4/S5x/S5y/S6/S7.
Revisar los colores A/B/C, el marco de solo esquinas de S1–S3, las muescas opuestas de S5x/S5y y el marco doble dorado de S6
(mismo glifo que S4, con destello adicional). Para S7, exige un diccionario completo de 60 pares rol/senda
y glifos/colores para las 10 plantillas. Cotejar sus asignaciones con la columna «Piedra angular» de
`docs/V46_DISENO_SENDAS.md` §10 (antes de los dos puntos). Revisar el emblema morado, borde dorado y
franja A/B/C; Espectro usa una capa translúcida. `PathsScreen` dibuja todos los escalones con el renderizador;
la cuadrícula de `RolesScreen` lo usa para S1–S3 desde T1 fase 2.

Para revisar T1 fase 1 antes del commit, comparar con Pillow los 300 PNG de `HEAD` (extraídos con
`git archive --format=zip HEAD src/main/resources/assets/aptitudes/textures/gui/node`) con los regenerados:
las dimensiones, modo RGBA y todos los bytes de píxeles (`Image.tobytes()`) deben coincidir. Confirmar
también que `--check` no cambia archivos y que detecta en una carpeta temporal un PNG faltante, corrupto,
RGB o de tamaño incorrecto. La hoja va en `build/previews/` y no se incluye en Git.

`client/screen/NodeIconRenderer` se verifica al compilar el cliente con `gradlew build`: usa la firma
Yarn 1.21.1 de `drawTexture` con tamaño de textura 16×16. S7 es un `RoleNode`; `drawKeystone` lo busca
mediante `RoleTree.get(PathRules.nodeId(..., S7))` y delega en `draw`, incluido el respaldo `NodeIcons.of`.
Comprobar en partida el PNG, un nodo sin PNG (icono de objeto), el respaldo
de S7 (objeto de su plantilla) y la recarga F3+T/cambio de paquete (la caché debe vaciarse). Estas pruebas
visuales siguen pendientes; no se prueban registros de Minecraft con JUnit simple.

T1 fase 2 — comprobación manual pendiente en `RolesScreen` (tecla J):

1. Recorrer los 20 roles y comprobar los nueve nodos S1–S3 de A/B/C: PNG de 16×16 en la misma casilla
   de 24×24, con desplazamiento interior de 4 px. El resto de nodos conserva sus iconos de objeto.
2. Revisar nodos aprendidos, disponibles y bloqueados: marcos, oscurecimiento, color de senda, rangos,
   selección y tooltip deben conservarse. Probar zoom, arrastre y desplazamiento a escala de interfaz 4.
3. En una copia de desarrollo, mover temporalmente un PNG S1–S3 fuera de los recursos del cliente,
   recargar con F3+T y comprobar el respaldo al objeto. Devolver el PNG, recargar y comprobar que reaparece.
4. Abrir «Sendas» y volver al árbol; repetir al reconectar para comprobar la presentación.

Pruebas unitarias de las fórmulas en Java puro, en `src/test/java`. Gradle usa **JUnit Platform** con dos
motores: Jupiter (JUnit 5) y Vintage (JUnit 4), así que se pueden escribir pruebas nuevas con JUnit 5 sin
reescribir las existentes (JUnit 4):

| Clase | Qué comprueba |
|---|---|
| `souls/SoulsMathTest` | Niveles de alma por punto, coste de nivel (creciente y con tope), validación de peticiones de subida de nivel, **regresión del desbordamiento de enteros** (`{MAX, MAX, 50}`) |
| `logic/AttributeScalingTest` | LINEAR = original; SOFTCAP idéntico hasta 40; SATURATION con pendiente inicial ≈ 1 y asíntota; monotonía y nunca por encima del lineal; umbrales inválidos |
| `role/RoleRulesTest` | Puntos de árbol (31 a nivel 30), crecimiento de XP, intervalo 0 sin división por cero, niveles enormes sin desbordamiento |
| `data/ProgressRulesTest` | Curva de XP de habilidades positiva y creciente; configuración extrema limitada al rango de `int` |
| `config/AptitudesConfigValidationTest` | Valores por defecto intactos; negativos, NaN e infinitos vuelven al defecto; límites (probabilidades ≤ 1, divisores y ventanas ≥ 1, nivel máximo ≤ 1000); `attributeScaling` inválido → LINEAR y umbrales ordenados |
| `logic/PlacedBlockLogTest` | Bloque colocado se consume una vez; mundos independientes; tope que olvida el más antiguo; tope inválido ≥ 1 |
| `ability/CooldownBookTest` | Cuenta atrás y caducidad; sobrevive a guardar/cargar; `tryStart`; valores corruptos no bloquean para siempre; tope de entradas |
| `world/TemporaryBlockScheduleTest` | Caducidad por tiempo de juego (también tras reinicio); quitar/consultar; tope por mundo |
| `morph/MorphSizeRulesTest` | Mob pequeño conserva su tamaño; mob enorme acotado; mínimos con valores diminutos o inválidos |
| `logic/ComboCounterTest` | Cadena dentro de la ventana y caducidad; remate con umbral y espera; bonificación con tope; contador acotado |
| `role/LoadoutCodecTest` | Formato antiguo de 2 bits leído igual; formato nuevo con 8 activas; índices fuera de rango |
| `role/EvolutionPointsTest` | Puntos extra por evolución (+10/+5), constructor antiguo sin extra, negativos ignorados, sin desbordamiento |
| `logic/XpDiminishingTest` | Multiplicador (completo hasta el umbral, decrece hasta el suelo), redondeo, ventana deslizante por jugador y fuente, granja frente a juego normal, memoria acotada |
| `party/PartyRulesTest` | Cuota de XP en pasos válidos, aritmética acotada, bonificación por integrantes conectados y valores corruptos |
| `config/PartyConfigValidationTest` | Valores predeterminados y límites seguros de los cuatro campos de grupo |
| `contract/ContractRulesTest` | Ofertas fijas por jugador y día, oferta destacada, límites por reputación y evolución, tipos por dificultad, objetivos alcanzables y entregas completas |
| `achievement/AchievementCatalogTest` | Árbol de logros (ids únicos, padres declarados), JSON de avances coherentes con el enum y sin sobrantes, traducciones en los 8 idiomas |
| `event/SoulFogRulesTest` | Pesos que suman 100 por clima, clima por agua y temperatura, distribución exacta de las tiradas, variantes por clima y tiradas fuera de rango |
| `event/StrongEventRulesTest` | Pesos del Eclipse por fase y del Nether por tamaño (suman 100 y siguen las tiradas), bonificación y armadura acotadas, material por fase, tamaños crecientes en potencia/caída/botín/restos, criaturas por tamaño y probabilidad |
| `role/PathRulesTest` | Ids de los escalones, niveles aprobados y multiplicador de S6, exclusividad de sendas, bifurcación (una rama; S6 acepta cualquiera), migración (conserva la de más rangos; empate A) y rangos corruptos |
| `role/KeystoneRulesTest` | Tabla de piedras angulares (3 distintas por rol, Legión solo con esbirros), Sobrecarga cada 4.ª, Baluarte (umbral y espera), esperas acotadas y progreso, valores acotados |
| `config/ConfigMigrationTest` | El nivel máximo antiguo (30) pasa a 50 una sola vez; los valores elegidos a mano se respetan |

Ejecución: `.\gradlew.bat test` (también se ejecutan en `.\gradlew.bat build`).

### Prueba en partida (autor, 2026-11-03)

`main` en `6c709f9` compilado y probado en partida por el autor: **v15–v19 y v29–v41 ✔ según lo esperado**
(guías `docs/PLAYTEST_v15-v19.md` y `docs/PLAYTEST_v29-v41.md`). Defecto encontrado: la pantalla de ajustes
del HUD solapa el texto de escala con el primer botón y la ayuda con Restablecer/Hecho.

### Resultado real en este bloque

En el entorno de desarrollo (sin acceso a los repositorios de Minecraft/Fabric) se compilaron las
clases probadas y las pruebas con el JDK 21 y JUnit 4.13.2 local, y se ejecutaron con `JUnitCore`:

```
JUnit version 4.13.2
OK (17 tests)
```

La primera ejecución dio 2 fallos que eran **errores de las propias pruebas** (expectativas mal
calculadas), no del código; se corrigieron y se documentan en el informe del bloque.

**No verificado aquí:** `gradlew build`/`test` completos (requiere descargar Minecraft y Fabric).
`RoleRules`/`ProgressRules` se compilaron localmente con un *stub* mínimo de `AptitudesConfig`
(fuera del repositorio); en Gradle se usa la clase real.

## Comprobaciones estáticas usadas en cada cambio

- `javac` de todo el código (sin las librerías de Minecraft): detecta errores de sintaxis y de tipos
  que no dependen de Minecraft. **No** detecta errores de API de Minecraft ni `switch` incompletos.
- Script de imports: busca clases usadas sin importar.
- Validación JSON de todos los recursos y comprobación de que toda clave de traducción usada en Java
  existe en `es_es` y `en_us`, y de que no hay `%` inválidos.

## Pruebas manuales recomendadas (no automatizables aquí)

Protocolo detallado, con comandos y resultados esperados para la v15–v19: [PLAYTEST_v15-v19.md](PLAYTEST_v15-v19.md).

1. Servidor dedicado + 2 clientes: subir de nivel en la Hoguera, desconectar y reconectar → atributos
   iguales y sin modificadores duplicados (`/attribute @s minecraft:generic.max_health get`).
2. Morir y reaparecer: roles, árbol, accesorios y almas se conservan.
3. `legendaryExclusive = true`: dos jugadores intentan aprender la misma legendaria → solo uno.
4. `attributeScaling = SOFTCAP`: con más de 40 puntos, la pantalla y el efecto real crecen más despacio.
5. Romper bloques con niveles altos de Minería: sin bloques fantasma.
6. Pesca: pescar peces sube Pesca (toast de XP); la basura no. `/aptitudes admin` para subir a 50 y
   comprobar Gracia del delfín al nadar, Poder del conducto en el agua y tesoros.
7. Rendimiento decreciente: matar más de 20 mobs del mismo tipo en un minuto → el toast de XP baja;
   cambiar de tipo de mob → XP completa.
8. Pantalla K: las 10 tarjetas (5×2) caben sin salirse del panel (escalas de GUI 2, 3 y automática).
9. `legendaryExclusive = true` con 2 jugadores: A aprende la legendaria de su rol → en el árbol de B
   (mismo rol) el botón Mejorar queda inactivo y la ficha dice «La tiene A». A pierde el nodo (reinicio
   del árbol) → B puede aprenderla sin reabrir el juego.
10. Colocar un tronco y romperlo → sin toast de XP de Tala; talar un árbol natural → XP normal.
    Plantar un brote, dejar que crezca y talarlo → XP normal (el tronco de la base no cuenta como colocado).
11. Colocar redstone sobre un bloque donde no se puede (p. ej., en el aire con clic en agua) → sin XP de
    Ingeniero; colocarla bien → XP. Colocar y romper la misma pieza 40 veces en un minuto → la XP baja.
12. Lanzar la definitiva, salir y volver a entrar → la espera sigue (HUD incluido). Repetir cerrando el
    servidor: la espera continúa donde estaba.
13. Levantar el Muro elemental y cerrar el servidor antes de 10 s → al volver, el muro desaparece en los
    segundos que le quedaban. Picar un bloque del muro → no suelta nada ni da XP.
16. Árbol v23: un rol base muestra al final del árbol las filas «Maestro» y «Definitivo» bloqueadas (velo,
    candado, ficha «Bloqueado…»). `/roles admin settier J <rol> 1` → la fila Maestro brilla y se puede aprender
    (2 rangos, puntos +10). Equipa una activa nueva (índice 5-8) y úsala; sal y entra: sigue equipada.
    `/roles admin settier J <rol> 0` → los nodos evolucionados vuelven a 0.
17. Mundo guardado antes de la v23 con activas equipadas en V/B → siguen siendo las mismas tras actualizar.
18. Animaciones v24: lanza una activa de cada categoría (Bersérker, Mago, Asesino, Paladín, Ingeniero) →
    gesto del brazo, sello que gira bajo los pies y remate propio con sonido; otro jugador cerca también lo ve.
    En el HUD la ranura destella y el icono rebota. En el árbol, aprender un rango hace latir el nodo.
19. Colisión v26: transfórmate en murciélago o pollo → cabes por un hueco de 1 bloque de alto y la cámara
    baja; F3+B muestra una caja pequeña. Transfórmate en un mob grande → caja de 1,2 de ancho como máximo.
    Otro jugador ve la caja correcta. `logs/latest.log` no debe tener el aviso «La colisión de las
    transformaciones no se aplicó».
20. Mesa del gremio v27: en la mesa de trabajo, el patrón de una pieza de armadura de rol no da nada.
    Clic derecho en la Mesa del gremio → cuadrícula 3×3; el mismo patrón da la pieza; mayús+clic fabrica
    varias; al cerrar, los ingredientes vuelven al inventario. Un aldeano sigue tomándola como bloque de
    trabajo. En el grimorio, la receta dice «Mesa del gremio».
21. Atributos v27 (con un `config/aptitudes.json` nuevo): 10 puntos en Vigor dan +7 de vida (antes +10).
22. Combos v28: golpea zombis seguidos → «×2…×5» junto a la mira y más daño; el 6.º golpe hace el remate de tu
    categoría y aparece «¡REMATE!». Espera 3 s → el contador desaparece. Recibe un golpe fuerte → se rompe.
    Contra otro jugador no cuenta. Con el Asesino, el recurso Combo sube el doble.
14. Escala de interfaz 4 (o ventana pequeña): abrir la pantalla de roles (J) → aparece una barra a la
    derecha; rueda fuera del árbol o RePág/AvPág hasta ver «Hecho»; el zoom del árbol no cambia.
15. En el Nether, sobornar a un piglin (Soborno) → deja de atacar en menos de medio segundo. Con el
    camuflaje del Cambiaformas, los piglins brutos y hoglins no te persiguen.

# v32 — runas de armas (verificación manual)

1. Fabrica las cuatro runas en la Mesa del gremio; verifica que no se fabriquen en mesa de trabajo.
2. Sostén un arma de rol y una runa en manos opuestas, usa la Mesa del gremio y confirma consumo y mensaje de ranura.
3. Engarza una segunda runa diferente; una tercera y un duplicado deben rechazarse. Cierra/abre el mundo y verifica persistencia.
4. Prueba Fuego, Robo de vida, Aceleración y Fuerza; comprueba magnitudes y enfriamiento indicados en `docs/BALANCE.md`.
5. Confirma que una espada vanilla no admite runas.

# v33 — jefes propios (verificación manual)

1. En una torre nueva, llega a la azotea y derrota al Centinela; revisa barra, nombre y su núcleo.
2. En un santuario de Hoguera nuevo, lucha con el Oráculo. En cada tipo de campamento nuevo, aparece el Campeón con la variante correspondiente.
3. Reduce la vida de un jefe por debajo de 2/3 y 1/3: cada fase debe activarse una sola vez, incluso tras salir y volver a cargar el chunk.
4. En fase final, observa el aviso/partículas antes del golpe de área; el Oráculo aplica lentitud. Comprueba alcance y tiempos de 3–4 s.
5. En la Mesa del gremio, aplica cada núcleo una vez a una hoja o foco de evolución: consume el material y sube +0,5 daño. Duplicados y núcleos adicionales se rechazan; bonificación máxima +1,5.
6. Reinicia el mundo y confirma que no reaparecen jefes ya vencidos y que el progreso/núcleos de armas persiste.

# v34 — el Eclipse (verificación manual)

1. `/aptitudes eclipse` muestra los días hasta el próximo. `/aptitudes eclipse start` (operador): mensaje,
   sonido, el cielo enrojece poco a poco y aparece la barra «Eclipse · Penumbra».
2. A los ~2,5 min (Totalidad): sol con anillo carmesí, ceniza, relámpagos sin daño y oleadas cada 30 s. Las
   criaturas llevan casco oscuro y no arden. No más de 12 vivas alrededor de cada jugador.
3. A mitad del evento aparece el Heraldo del Eclipse con barra de jefe. Con dos jugadores juntos, un solo
   Heraldo con más vida; separados (>64 bloques), uno cada uno. Suelta el Núcleo del eclipse y fragmentos.
4. Fabrica la Runa del Eclipse en la Mesa del gremio y engárzala: más daño contra élites (no contra criaturas normales).
5. Muere durante el evento: al terminar no recibes la recompensa de superviviente; otro jugador que no murió sí.
6. `/aptitudes eclipse stop`: las criaturas del eclipse se disuelven, el cielo vuelve a la normalidad, sin recompensas.
7. Eclipse natural: con `/time set` ponte en el día 9 al atardecer (18:00): aviso con campana. Al amanecer del
   día 10 empieza solo. Reinicia el servidor a mitad: el evento continúa y las criaturas cargadas siguen.
8. En el Nether durante un eclipse: sin cielo rojo ni HUD; al volver, el estado se restablece.
9. Mayús + O → «Cielo del Eclipse» desactivado: cielo normal, pero viñeta y ceniza visibles.
10. Dificultad Pacífica: hay cielo y avisos, pero no aparecen criaturas ni Heraldo.

# v35 — Runas+ (verificación manual)

1. Fabrica Escarcha, Trueno y Guardia en la Mesa del gremio; no se fabrican en la mesa de trabajo.
2. Escarcha: el objetivo queda con Lentitud II 3 s y salen copos de nieve.
3. Trueno: contra un grupo de zombis, a veces salta una chispa a 2 cercanos (3 de daño). Nunca a otro jugador,
   a tu lobo ni a tus esbirros. No se encadena sin fin.
4. Guardia: corazones dorados al golpear, hasta 6 de absorción; una manzana de oro no se reduce.
5. Robo de vida: el arma ya no «parpadea» en la mano al curar.
6. Quitar runa: Mayús + clic derecho en la Mesa del gremio con el arma y la otra mano vacía. Sin esmeraldas →
   aviso; con 4 → se quita la última, sonido de afilador y mensaje. Con Fuerza o Aceleración, el daño o la
   velocidad del tooltip vuelven al valor sin runa. El foco no dispara al hacerlo.
7. Sin agacharse, la Mesa sigue abriendo la fabricación y engarzando runas como antes.
8. Cambia `runeThunderChance` a 1.0 y `/aptitudes admin reload`: la descarga sale en cada golpe (con su espera).

# v36 — anti-granjas persistente (verificación manual)

1. Coloca piedra, rómpela: sin XP de Minería (como antes).
2. Coloca piedra, guarda y sal del mundo, vuelve a entrar y rómpela: **sigue sin dar XP** (antes sí daba).
3. Reinicia el servidor dedicado y repite: tampoco da XP.
4. Con dos partidas distintas en el mismo cliente: un bloque colocado en la primera no afecta a la misma
   posición en la segunda.

# v37 — sonidos propios (verificación manual)

1. Activa los subtítulos (Opciones → Accesibilidad). `/aptitudes eclipse start`: suena «El sol se apaga»;
   en la totalidad, «Totalidad del Eclipse»; a mediodía, «Llega el Heraldo».
2. Atardecer del día anterior a un eclipse: «Campana lejana del Eclipse».
3. Remate de combo: el golpe común suena junto al sonido de la categoría.
4. Engarzar una runa: campanilla. Quitarla (Mayús + clic en la Mesa): raspado y chasquido.
5. Con el volumen de «Criaturas hostiles» a 0 no se oyen los sonidos del Eclipse (categoría Hostil).

# v39 — interfaz con escala grande (verificación manual)

1. Opciones → Gráficos → Escala de interfaz **4** con ventana 1920×1080, y **3** con ventana 1280×720.
2. Pantalla K (tecla K): si no cabe, aparece una barra dorada a la derecha; la rueda del ratón mueve la
   ventana y los botones Confirmar/Reiniciar siguen funcionando en su sitio. Con escala 2 no hay barra.
3. Pantalla de roles, grimorio, Mesa del gremio y ajustes del HUD (Mayús + O): nada se sale ni se solapa.
4. Durante un Eclipse, engarza una runa: el aviso de la barra de acción ya no queda tapado por la barra del Eclipse.

# v40 — pantalla de configuración (verificación manual)

1. Mundo de un jugador (con trucos): `/aptitudes config` → «Servidor · puedes editar». Busca `eclipse`.
2. Cambia `eclipseIntervalDays` a 5 (✔): mensaje «Configuración: eclipseIntervalDays = 5»; `config/aptitudes.json`
   lo guarda; `/aptitudes eclipse` cuenta con el nuevo intervalo.
3. Escribe 9999 en `eclipseMaxMobsPerPlayer`: se aplica 40 (el tope). Escribe «abc» en un número: aviso en rojo.
4. Botón de un booleano (`combosEnabled`): cambia Sí/No al momento.
5. Servidor con un jugador no operador: la pantalla está en solo lectura y los botones desactivados.
6. Cambia `runeThunderChance` a 0.5: el tooltip de un arma con Trueno muestra «50 %».
7. Pestaña ⚙ en la pantalla K abre la configuración; «Hecho» vuelve a la pantalla K.
8. Con Mod Menu instalado: Mods → Aptitudes → Configurar abre la misma pantalla. Sin Mod Menu, el juego arranca igual.

# v41 — eventos rotativos (verificación manual)

1. `/aptitudes event`: «Próximo evento: X, dentro de N días».
2. `/aptitudes event start soul_fog`: cielo gris azulado, almas y ceniza blanca, barra «Niebla de almas»;
   oleadas pequeñas de no muertos a pie (zombis, esqueletos y sus variantes; sin fantasmas) que sueltan Fragmentos de alma. `/aptitudes event stop`: se disuelven.
3. `/aptitudes event start ember_rain`: cielo anaranjado; cada ~25 s una estela de fuego cae cerca y explota
   sin romper bloques, dejando lingotes. Recibir el impacto hace daño.
4. `/aptitudes event start eclipse`: igual que antes (sol con anillo rojo).
5. Con un evento en curso, `start` de otro → «Ya hay un evento en curso».
6. Pon `worldEventWeightEmberRain = 0` en la configuración (v40): `/aptitudes event` nunca anuncia la Lluvia de brasas.
7. Calendario natural: ve al día 9 al atardecer (`/time set`): la campana anuncia el evento elegido y al día
   siguiente empieza ese (la Niebla a las 18:00).
8. Mundo con Eclipses anteriores (v34–v40): carga sin errores y el calendario continúa.

# v29 — grupos (verificación manual)

1. Con dos clientes, pulsa `P`, invita desde la lista y comprueba que el segundo recibe botones clicables Aceptar y
   Rechazar. Rechaza una invitación; repite y acepta. Confirma que caduca tras 60 s.
2. Forma un grupo de cinco; una sexta invitación debe rechazarse. Comprueba el líder marcado, salir y expulsar.
   Reinicia el servidor y verifica que el grupo y sus opciones siguen guardados.
3. Con el líder a 25 % y luego 50 % de XP compartida, mata criaturas cuerpo a cuerpo y con arco. Los miembros
   conectados en otras dimensiones reciben XP de Combate/Tiro con arco y del rol de combate; el atacante conserva
   su XP. Comprueba que `xpDiminishing` se aplica antes del reparto y que la XP recibida no se reparte otra vez.
4. En un grupo de cinco conectados, comprueba el máximo de +20 %; desconecta integrantes y confirma que el extra
   disminuye en pasos de 5 %. Cambia los campos `party*` desde la configuración del servidor y comprueba los límites.
5. Con fuego amigo desactivado, golpea a un compañero con ataque, arco, habilidad y runa. No debe recibir daño;
   activa `/party pvp` y repite. Prueba también las invocaciones del grupo.
6. Lanza curas y auras con y sin grupo: con grupo no deben afectar a jugadores ajenos; sin grupo deben conservar
   el comportamiento anterior. Verifica el HUD en la misma dimensión, en otra dimensión y tras desconectar.
7. En Eclipse, confirma un Heraldo por grupo y escalado de vida hasta cinco integrantes; sin grupo, los jugadores
   a menos de 64 bloques siguen compartiendo un Heraldo. En Niebla de almas comprueba oleadas y tope según grupo.

# v46.1 — Piedras angulares (verificación manual)

Preparación: `/roles admin setlevel J <rol> 50`, aprende la senda hasta S6 y la piedra angular S7 en la vista de
sendas. Activa los subtítulos para oír cada sonido.

1. Encima de la barra de habilidades aparece el icono de la plantilla (borde morado).
2. Carnicería: al matar, corazones y Prisa II. Detonación: al matar junto a un grupo, los cercanos reciben daño;
   no se encadena sin fin y no daña a compañeros de grupo ni rompe bloques.
3. Baluarte: baja del 30 % de vida → absorción y Resistencia; el icono se oscurece 60 s.
4. Sobrecarga: la 4.ª habilidad no gasta maná y su espera es la mitad; el icono late en dorado antes de ella.
5. Contagio (Herbolista B): envenena a un zombi y mátalo junto a otros: los cercanos quedan envenenados.
6. Legión (Nigromante A): 5 esbirros a la vez; al golpear, curan al jugador.
7. Espectro: tras una esquiva (nube), el icono late; el siguiente golpe es crítico.
8. Fortuna: matar criaturas suelta más botín de media; las élites a veces sueltan esmeralda.
9. Tempestad: chispas a enemigos cercanos al hacer críticos (y a veces sin crítico).
10. Santuario: en grupo, cada 5 s te curas tú y los compañeros cercanos.
11. `/aptitudes config`: cambia un campo `keystone*` (p. ej., `keystoneOverloadEvery = 2`) y comprueba el efecto.

# v46.0 — Nivel 50 y Sendas exclusivas (verificación manual)

1. Arranque con un `config/aptitudes.json` antiguo (con `"maxRoleLevel": 30` y sin `configRevision`): al cargar
   pasa a 50 y aparece `"configRevision": 46`. Con `"maxRoleLevel": 40` se queda en 40.
2. Mundo anterior con un rol que tenga dos sendas empezadas: al entrar, mensaje dorado «Sendas exclusivas: en
   … conservas la senda … y recuperas N puntos»; la senda con más rangos sigue aprendida y los puntos vuelven.
3. Pantalla de roles (J): botón «Sendas» abajo. En la vista: pestañas A/B/C, raíl de niveles (dorado los
   alcanzados), escalones S1–S6 con la bifurcación lado a lado y la piedra angular con candado.
4. Sin senda elegida: al aprender S1 de A aparece la confirmación; al aceptar, B y C se ven bloqueadas (velo y
   candado) en la vista y en el árbol. Intentarlo por comando (`/roles learn <rol>_b1`) da «Ya sigues otra senda».
5. `/roles admin setlevel J <rol> 50`: aprende S4; en la bifurcación elige S5x (confirmación); S5y queda
   bloqueada («Ya elegiste la otra rama»). S6 se puede aprender con S5x **o** S5y.
6. Tooltip de S6: valor ×1,5 del de S4 (p. ej., Bersérker A: robo de vida +3 % por rango). Comprueba en combate
   que el efecto suma (más curación al golpear).
7. Reasignar el árbol (`/roles respec <rol>` si está permitido) libera la senda y la rama.
8. Escala de interfaz 3 y 4: la vista de sendas cabe o se desplaza con la rueda; nada se solapa.

# v41.2 — Eclipse y Lluvia de brasas más fuertes (verificación manual)

1. `/aptitudes event start eclipse` en supervivencia. En la Penumbra: zombis y esqueletos con algunas piezas de
   malla (y el casco oscuro); ninguna se suelta al matarlos.
2. `/time set 3000` durante el evento (Totalidad): piezas de hierro, esqueletos wither en las oleadas y criaturas
   que aguantan más golpes y apenas retroceden. Un campeón con piezas de diamante.
3. `/aptitudes event start ember_rain`: la mayoría de meteoritos son como antes; algunos tienen estela más gruesa,
   bramido de aviso y una explosión mayor que no rompe bloques. Alrededor quedan bloques de magma que
   desaparecen a los 30 s sin dejar agujeros. Más lingotes en el suelo.
4. Tras los impactos aparecen cubos de magma y blazes; en los grandes, piglins brutos y hoglins que **no** se
   convierten en zombis pasados 15 s; en los enormes, esqueletos wither. `/aptitudes event stop`: se disuelven.
5. `/aptitudes config`: `eclipseMobArmor = 0`, `eclipseArmorChance = 0`, `eclipseTotalityBonus = 0`,
   `emberRainLargeChance = 0`, `emberRainHugeChance = 0` y `emberRainMobChance = 0` → comportamiento de la v41.1.

# v41.1 — HUD y Niebla de no muertos (verificación manual)

1. Mayús + O con escala de interfaz 2, 3 y 4: el título, «Escala: N %» (entre − y +), los 6 botones, la ayuda y
   Restablecer/Hecho no se solapan.
2. `/aptitudes event start soul_fog` en una llanura: zombis, zombis aldeanos y esqueletos; ningún fantasma.
3. Repite junto a un río o en la playa (ahogados y esqueletos pantanosos), en un desierto (momias) y en una
   llanura nevada (errantes).

# v31 — logros (verificación manual)

Prueba automática: `achievement/AchievementCatalogTest` (enum, JSON y traducciones coherentes).

1. Entra en un mundo nuevo y abre los avances (L): aparece la pestaña «Aptitudes» con el grimorio de icono y los
   17 logros bloqueados (en gris los que dependen de otro).
2. Descansa en una Hoguera, elige un rol, haz un remate de combo y engarza una runa: un aviso por cada uno, y su
   mensaje en el chat.
3. `/aptitudes event start soul_fog` y espera al final: «A través de la bruma». Si lo detienes con `stop`, no cuenta.
4. Forma un grupo: el logro lo reciben quien invita y quien acepta.
5. Mundo anterior a la v31 con un rol en Maestro y un contrato cobrado: al entrar se conceden «Vocación», «Maestro
   del oficio» y «Firmado y sellado» sin hacer nada más.
6. Con un datapack que borre un avance de la pestaña, el juego no falla (el logro simplemente no se concede).

# v42 — Alquimia, fase 1

Automático: `AlchemyRulesTest` comprueba energía al límite, dosis repetidas, máximos que bajan,
valores enormes, negativos y no finitos; el Velo necesita protección y atacante de evento y nunca
aumenta un golpe ni lo vuelve negativo. Los registros, recetas y entidades requieren Minecraft.
Comprobado el arranque de inicialización dedicado con `runServer --args="--initSettings --nogui"`:
el mod registra su contenido y termina correctamente; esto no abre un mundo ni prueba el destilado.
Iconos: `python tools/textures/gen_status_effects.py src/main/resources/assets/aptitudes/textures/mob_effect --check`.
Para regenerarlos añade `--preview hoja.png` y omite `--check`. Los pétalos se generan con `gen_items.py`.

Guion de regresión en partida (servidor dedicado y dos clientes; repetir con escala de interfaz 4):

Para comprobar las duraciones base, prueba sin Herbolistas o desactiva `alchemyHerbalistEnabled`.

1. Fabrica un saquito con su receta anterior. Colócalo solo en la cuadrícula: 1 pétalo, consumo de
   1 saquito; sin recoger el resultado no se consume. Prueba clic normal, Mayús y libro de recetas.
   El saquito restante conserva su curación y retirada de veneno. El pétalo muestra icono y nombre.
2. Con combustible y pociones raras, destila pétalo → Maná, Esencia de élite → Frenesí y Fragmento
   del eclipse → Velo. Maná + otro pétalo → Vigor del bosque. Comprueba 1 y 3 frascos, tolvas,
   retirada/cambio del ingrediente o frasco durante el proceso y reinicio a mitad del destilado.
   Una operación completa consume un ingrediente; una operación cancelada no da ni cobra el resultado.
3. Gasta energía con una activa; bebe Maná: +4 por segundo además de la regeneración habitual.
   Repite dosis justo antes de un pulso y al máximo: nunca supera la energía máxima ni da XP.
   Repite con amplificador 255 por comando: sigue siendo +4 por segundo. En espectador o muerto
   no recupera por este efecto; en creativo puede recuperar energía pero no gana XP.
4. Mide `/attribute @s minecraft:generic.attack_speed get`, bebe Frenesí repetidamente y aplica
   `/effect give @s aptitudes:frenzy 60 255`: solo +15 % de la base, sin daño extra por golpe.
   La leche y la caducidad retiran exactamente ese modificador. Reconecta y reinicia con el efecto
   activo: conserva el tiempo guardado y una sola bonificación. Muere: el efecto no vuelve al reaparecer.
5. Con Velo, compara golpes idénticos de criaturas con/sin `aptitudes.eclipse` y
   `aptitudes.world_event`; usa `/tag` en criaturas de prueba y flechas de un esqueleto marcado.
   Solo las marcadas reducen el daño por el factor 0,80 (armadura y otras reducciones siguen vigentes).
   Tener las dos marcas o amplificador 255 no aumenta la protección. Un jugador con esas etiquetas
   no cuenta como criatura; caídas y meteoritos sin criatura atacante tampoco. Prueba con los tres
   eventos reales, incluyendo el Heraldo, no muertos de la Niebla y criaturas de la Lluvia de brasas.
6. Vigor: curación gradual, alimento/saturación con máximos vanilla. Con vida y hambre completas,
   repetir dosis no rebasa los topes. Nunca se toca la XP vanilla ni la XP de rol, tampoco por comandos.
7. Convierte las cuatro pociones con pólvora y aliento de dragón y fabrica flechas; comprueba
   nombres, iconos y duración reducida vanilla. Con dos jugadores y en grupo, las áreas beneficiosas
   pueden afectar a ambos; no hay reparto de XP, recompensas ni multiplicación por miembros.
8. Repite nombres y tooltip en `en_us` y las siete variantes de español; sin claves visibles,
   iconos ausentes ni texto solapado. Las ventajas y el capítulo se verifican a continuación.

## v42 — fase 2 (guion de regresión manual)

Automático: `BrewingRulesTest` (duración, instantáneos, velocidad, desbordamiento y XP hasta cero),
`AlchemyXpPersistenceTest` (codec real con JsonOps, límites y ventana tras guardar/cargar) y
`AlchemyConfigValidationTest` (siete valores por defecto, límites y no finitos). El build valida además
el único campo de `aptitudes.accesswidener` contra Yarn 1.21.1. No se añaden dependencias ni mixins.
Comprobación estática con los glifos bitmap vanilla: ambos textos de «Alquimia» se parten en 24 líneas
de hasta 224 px en la ventana mínima (380×236); cabecera de menos de 50 px, índice de 23 filas dentro
del borde y contenido desplazable. Esta medición no sustituye la inspección en el cliente real.

1. Mide una operación sin Herbolista (20 s) y con Herbolista principal/secundario dentro de 8 bloques
   (aproximadamente 13,35 s). Abre el soporte, ciérralo y aléjate a 20 bloques: conserva la ayuda; a más
   de 32 bloques, desconectado, muerto, en otro mundo, creativo/espectador o sin rol activo, deja de ayudar.
2. Comprueba tres frascos frente a uno: una sola concesión de XP por operación. Dos Herbolistas, incluso
   en grupo: un beneficiario y una bonificación; tiene prioridad quien abrió, luego el más cercano.
   Reabrir, clic rápido, recoger mediante tolva o volver a introducir el resultado nunca vuelve a cobrar.
3. Poción rara + pétalo con ayuda: Maná de 15 s. Frenesí 90 s, Velo 135 s y Vigor 45 s de regeneración;
   Saturación no aumenta. Pasa luego Maná a Vigor, a arrojadiza y a persistente: solo +50 % desde la
   nueva receta, sin acumulación de porcentajes ni potencia. Tooltip: una duración y un modificador,
   tanto en el inventario como en el grimorio. Repite con pociones vanilla y varios ingredientes.
4. Retira/cambia ingrediente o frascos durante el proceso; cancela antes del último tick, usa tolvas
   y prueba varios soportes a la vez: nunca XP sin consumir el ingrediente de una receta real.
   Configura el bono de velocidad a 0 y 1 para verificar ambos extremos y su finalización única.
5. Completa más de 12 operaciones con efectos en cinco minutos: 4/4/4/3/2/2/1…/0 XP base, según
   multiplicadores de rol. Cambia de soporte, reconecta y reinicia entre operaciones: el contador sigue.
   Muere y reaparece: tampoco se borra. Las pociones sin efectos y `/experience` no dan esta XP.
6. Guarda/carga un chunk a mitad de destilado y después de terminar; comprueba frascos, duración y
   ausencia de cobro repetido. No fuerza chunks descargados. La prioridad del soporte se conserva.
   Con dos Herbolistas abriendo alternadamente, solo la apertura nueva cambia la prioridad.
7. Edita las siete opciones por archivo y pantalla del servidor: límites documentados, NaN/infinitos
   rechazados. Con ventajas desactivadas queda el destilado vanilla. Un cliente sin permisos no cambia nada.
8. Grimorio con escala 4 y ventana útil 426×240: 23 capítulos dentro del índice, «Alquimia» con cuerpo
   desplazable y recetas del registro (iconos/tooltip con entradas y salidas reales). Pulsa los pétalos:
   abre su extracción desde la receta sincronizada. Repite en inglés y siete variantes de español.

# v30 — contratos del gremio (verificación manual)

1. Fabrica un Tablón (3 papel arriba, 3 tablones en medio y 1 tablón abajo en el centro) y úsalo: 3 ofertas con
   objetivo y recompensa legibles; cerrar y abrir no las cambia.
2. Acepta una de Caza y mata a la criatura indicada: progreso en la barra; al completarla, aviso «¡Contrato
   completado!». Cóbrala: esmeraldas, XP de rol y reputación. Esa oferta ya no se puede volver a aceptar hoy.
3. Entrega sin llevar suficiente: aviso y **no** se quita nada. Con la cantidad completa: se entrega.
4. Pasa al día siguiente (`/time add 24000`): ofertas nuevas; el contrato que tenías en curso sigue igual.
5. En grupo: la Caza de un miembro avanza el contrato del otro (conectado).
6. Con el rol en Maestro: aparecen ofertas Élite; en Definitivo, Legendarias (Jefe o Evento).
7. `/aptitudes event start soul_fog` y espera al final (o detenlo con `stop`: no cuenta); un contrato de Evento
   de Niebla avanza al terminar el evento con éxito.
