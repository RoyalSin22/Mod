# Registro de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones con
[Versionado Semántico](https://semver.org/lang/es/). Las «vNN» son las etapas internas de desarrollo.

## [Sin publicar]

### Añadido
- **v65 — Enemigos nuevos.** El Errante Velado aparece en la Niebla de almas (10 % de las apariciones, junto al
  Osario Quebrado con otro 10 %) y tiene huevo de aparición y entrada en el bestiario. El Custodio de las Cuatro
  Llaves sustituye al guardián vanilla de los templos sellados. Ambos tienen modelo y animaciones generados por
  `tools/models/gen_mob_models.py` (`--check`). Sin validar en partida.
- **v65 — Rediseño de jefes (hoja de arte).** El Coloso Sin Rostro pasa a 3,8 bloques de alto y adopta netherita,
  pizarra y energía roja. El Guardián Desmontado tiene escudo carmesí con cruz dorada y cuerpo de piedra lisa. El
  Autómata Antiguo usa piedra tallada, bronce, cobre, ojo ámbar y dos esferas auxiliares. La esfera del Autómata tiene
  sprite propio: bronce y núcleo ámbar, y cian al ser devuelta (indicador de aturdimiento). Sin validar en partida.
- **v65 — Trance de combate (F2).** La sintonía se llena con golpes críticos a criaturas (+10) y con habilidades (+15).
  Al llegar a 100 se entra en trance durante 10 s con +20 % de daño saliente. Se muestra en la barra de acción. Sin
  validar en partida.
- **v65 — Pedidos del gremio (F4, servidor).** Estado por mundo (`OrderBookState`), acciones de servidor (`OrderBook`:
  publicar, entregar, recoger y cancelar) y reglas puras con pruebas (`OrderBookRules`).
- **v65 — Pedidos del gremio (F4, pantalla).** El tablón tiene una página «Pedidos» con tres filas por página, botón
  de publicar con el objeto de la mano y selector de nivel. Entregar, recoger y cancelar pasan por el servidor
  (`OrderViewPayload`, `OrderActionPayload`). La lista se pagina de tres en tres. Sin validar en partida.
- **v65 — Mancha de XP (F3).** Al volver a morir antes de recuperar la mancha, se pierde un 25 % y el resto se suma a
  la nueva mancha, en el lugar de la muerte. Antes se perdía entera. Sin validar en partida.
- **v65 — Expediciones de Amenaza (F5, primera parte).** La ficha de expedición inicia un recorrido de cuatro módulos
  (combate, élite, jefe, evento o recolección de fragmentos) dentro de 48 bloques, con límite de 15 minutos y
  recompensa provisional. Los miembros de la party en la zona comparten la expedición. La ficha está en la pestaña
  creativa. Cada expedición tiene dos de seis modificadores (niebla, lluvia de brasas, oleada élite, hambre, jefe
  aturdido, botín abundante). Sin validar en partida.

- **v65 — Huevos y Osario.** Los huevos del Osario Quebrado y del Errante Velado aparecen en la pestaña creativa de
  Aptitudes. El Osario emite ectoplasma y polvo de tierra alrededor de los pies. Sin validar en partida.

- **v65 — Fase 3 de los jefes.** El Guardián Desmontado acelera `volley` y `dive` (enfriamientos de 30 y 50 ticks).
  El Autómata Antiguo lanza dos esferas en abanico y su rayo barre un cono de 60°. Sin validar en partida.

### Corregido
- **v56 — Generación de jefes.** Torres, santuarios y campamentos guardan sus jefes con el terreno al
  generarse. Esto corrige el bloqueo que podía detener el mundo e impedir abrir o romper bloques.
- **v58 — Cambiaformas.** El murciélago bate las alas al verse en tercera persona. La gallina
  agita las alas al caer y el slime se estira durante el salto. El poder del murciélago da un
  vuelo corto que se dirige con la mirada.

### Corregido
- **Aldeano del gremio.** Un aldeano sin trabajo ahora se convierte en Maestro del gremio al encontrar una Mesa
  del gremio. Vanilla solo ofrece como puesto de trabajo los tipos de su etiqueta
  `minecraft:acquirable_job_site`; el mod la amplía con un dato de etiqueta, sin mixins.
- **Modelos de los jefes.** Los tres jefes tienen más piezas y detalle (placas, remaches, capa, púas,
  pistones, lente y cañón). La textura pasa a 256 px.

### Añadido
- **v63 — Jefes animados.** El mod requiere ahora **GeckoLib 4.7.7**. Tres jefes animados: el Coloso Sin
  Rostro (tajo en arco, golpe descendente con onda y, con la armadura agrietada, embestida), el Guardián
  Desmontado (piezas que hay que romper en orden; con las tres caídas, el núcleo recibe más daño) y el
  Autómata Antiguo (esferas que se pueden devolver para aturdirlo, pisotón y, en fase 2, un rayo que se corta
  en los bloques). De momento se invocan con sus huevos; sus estructuras llegarán después. Tienen página en el
  bestiario.
- **v64 — Templos sellados y Vasos de vida.** Un templo de 15×15 aparece en el Mundo Normal con el
  aspecto y el puzle de su anillo de Amenaza: imita con las palancas las luces sobre la puerta sellada
  y, desde el anillo II, pisa las placas en el orden de sus velas. Al abrirse despierta el Guardián del
  templo, cuya llave abre el Relicario: cada jugador recibe un Vaso de vida (+1 corazón permanente,
  hasta 5, `lifeVesselMax`). Un vaso no se puede usar dos veces ni copiar. Dos logros nuevos.

### Cambiado
- **v62 — Armas y objetos.** Las 40 armas de evolución y 12 armas de rol tienen volumen en la mano, en
  el suelo y en los marcos; el inventario conserva su icono plano. Las texturas ganan destellos en
  metales y gemas y veta en madera y cuero. Los tooltips muestran el sello del rol, el daño por
  segundo de las armas cuerpo a cuerpo, las cifras de los disparos y la rareza. Los objetos de rol
  comunes pasan a poco comunes (solo cambia el color del nombre).
- **v61 — Grimorio.** Nueva pestaña Bestiario: cada criatura o jefe abre su página al vencerla, con
  modelo, estadísticas, debilidades, consejos y botín; el grupo cercano también la desbloquea.
  Pestaña Progreso con logros, bestiario, jefes, escalones de senda y piedras angulares. Buscador
  global (capítulos, objetos, criaturas descubiertas y roles) y marcadores con ☆. En Sendas, cada rol
  muestra sus tres sendas con iconos y piedra angular. Con el grimorio en el inventario, U sobre un
  objeto de Aptitudes abre su ficha. Si hay un paso de tutorial pendiente, una pestaña lleva a su
  capítulo. Los capítulos estrenan ilustraciones de cabecera.
- **v60 — Arenas de jefe.** La niebla rodea al Centinela, al Oráculo y al Campeón en estructuras
  nuevas cuando entras a luchar. Al sellarse la arena suena un tema original. La niebla se retira
  al vencer al jefe o si no queda nadie combatiendo dentro.
- **v60 — Mejora de frascos.** Descansa en una Hoguera y abre Frascos. Usa Fragmentos de alma y
  un núcleo de jefe para añadir una carga y aumentar la recuperación de Estus o Éter. La mejora
  queda guardada en el propio frasco.
- **v59 — Editor del HUD.** Mayús + O ofrece tres disposiciones para la barra de habilidades y un
  modo minimalista. Las barras de vida, maná y XP y los avisos cambian con transiciones suaves.
  La tarjeta de XP se ilumina al subir de nivel. Puedes desactivar estas animaciones.
- **v59 — Barra de jefes.** Los jefes y campeones de Aptitudes comparten un panel centrado cerca de la
  parte inferior. Muestra su vida, la fase de los jefes y un recuento si hay más enemigos cerca.
- **v57 — Atlas y Sendas.** Las dos opciones de la bifurcación tienen filas separadas y el rango
  queda alineado a la derecha. La ficha envuelve los requisitos y el lienzo de Talentos despeja
  el fondo y usa conectores más finos.
- **v56 — Encuentros en estructuras.** Los jefes dan un segundo de aviso antes del golpe final.
  Sal del círculo del Centinela, cruza al centro seguro del anillo del Oráculo o esquiva de lado
  el cono del Campeón. Una pared sólida detiene el golpe.
- **v54 — Animaciones de lanzamiento.** Cinco remates 3D por categoría de rol, desde el tajo marcial
  hasta el engranaje de Oficio. Se ven en primera o tercera persona según el ángulo y también los
  ven los jugadores cercanos. El interruptor Animaciones 3D está en Mayús + O.
- **v53 — Atlas de talentos.** En J, abre Talentos, Sendas, Maestro o Definitivo desde la cabecera
  del rol. Los sellos, iconos y acentos identifican cada rol. La ficha muestra el rango y los
  requisitos del nodo seleccionado y el cursor revela la descripción completa.
  Los 20 roles tienen paleta y emblema propios en el atlas y en Sendas. Las pestañas distinguen
  selección y foco, y las de Sendas muestran el nombre completo al pasar el cursor.
- **v52 — Persecución del Eclipse.** Las criaturas del evento y su Heraldo buscan jugadores visibles
  a su alrededor. Si pierden de vista al objetivo, intentan llegar al último punto en el que lo vieron
  durante un máximo de seis segundos. Soborno y camuflaje siguen protegiendo al jugador.
- **v51 — Mesa y Tablón del gremio.** Separamos la cuadrícula, el resultado y el inventario de la Mesa.
  El Tablón muestra ofertas y contratos activos en columnas, con una fila de acciones por ficha.
  Al pasar el cursor se pueden leer el objetivo y la recompensa completos. El árbol usa un lienzo común e indica que se elige una senda.
- **v50 — HUD e iconos del árbol.** El árbol y la barra de habilidades comparten iconos propios.
  Los sellos identifican el rol, las activas muestran sus marcas y los nodos evolucionados distinguen
  Maestro y Definitivo. La barra separa piedras angulares y mecánicas, muestra teclas y costes con
  fondo de contraste y se ajusta al espacio disponible incluso al arrastrarla en Mayús + O.
- **v47 — Asalto del Eclipse, fase 1**: oleadas de 4 criaturas por jugador en Penumbra y 8 en Totalidad,
  con élites y campeones garantizados cuando sus probabilidades están activadas y hay espacio/presupuesto.
  Equipo completo visible en unidades de élite, sin soltarlo al morir; conservan los multiplicadores previos.
  Presupuestos compartidos por proximidad, tope por zona y por mundo, turnos rotativos y búsqueda del jugador.
  El Heraldo se organiza por encuentros próximos, incluso con grupos dispersos; al llegar al límite puede
  sustituir una unidad ordinaria sin botín. No aparece sobre el jugador si falta un emplazamiento seguro.
  Finalización protegida contra cobros repetidos, exclusión de creativo/espectador y recuento restaurado
  tras reiniciar. Siete opciones nuevas validadas y capítulo del grimorio actualizado en once idiomas.
- **v49 — Mesa del gremio y Hoguera.** La Mesa del gremio se fabrica en la mesa de trabajo y reúne
  el resto de las recetas de fabricación de Aptitudes, incluidas la Hoguera y el Grimorio. La Mesa
  estrena un panel 3×3 de madera, latón y jade. La Hoguera incorpora un brasero de piedra, llamas
  animadas y una cabecera propia al descansar. El grimorio indica qué mesa requiere cada receta.

### Añadido
- **v55 — Talentos por datapack.** El pack de ejemplo añade Paso ligero y Paso firme al Espadachín.
  Con el pack activo, abre **Talentos del pack** desde J para consultar y aprender la rama con puntos
  del rol. Cada rango da +2 % de velocidad de movimiento, hasta +6 % entre ambos roles activos.
  Al retirar el pack o perder un requisito, recuperas los puntos de los rangos afectados.
- **v48 — Reforja y Templado.** Aplica Reserva o Impulso a una hoja o foco de evolución en la Mesa del
  gremio. Templa el arma hasta el rango II en la Hoguera con Corazones de campeón y Fragmentos de alma.
  Los talleres muestran el efecto actual, el resultado y los ingredientes requeridos antes de confirmar.
  El arma conserva su rango al cambiar de afijo y mantiene sus runas, núcleos y encantamientos.
- **v45 — Traducciones, T8 fase 1**: traducción inicial de interfaz y objetos a portugués de Brasil
  (`pt_br`), francés (`fr_fr`) y alemán (`de_de`), que conviene revisar con hablantes nativos.
  Cada idioma contiene las 3512 claves de inglés en el mismo orden: 1386 traducidas en esta fase;
  grimorio y nodos/sendas (2126 claves) conservan el inglés hasta las fases 2 y 3.
  Los rótulos de interfaz generados se mantienen en sus generadores. Nuevo comprobador de duplicados,
  claves, orden y marcadores de formato, con pruebas de errores y ejecución en GitHub Actions.
- **v45 — Traducciones, T8 fase 2**: traducción inicial de los 23 capítulos del grimorio a los tres
  idiomas, que conviene revisar con hablantes nativos. Incluye sendas, las diez piedras angulares,
  logros, contratos, eventos y alquimia; las recetas se consultan en el juego.
  Catálogo y generador reproducibles, con el párrafo de evolución en su generador original, comprobación
  de ancho con la fuente de Minecraft y pruebas de conservación de las demás traducciones.
  Acumulado: 1432 claves traducidas por idioma; los 2080 textos de nodos/sendas esperan la fase 3.
- **v45 — Traducciones, T8 fase 3**: traducción inicial de los nodos base, legendarios, evolucionados
  y sendas completas S1–S7 a portugués de Brasil, francés y alemán, que conviene revisar con hablantes
  nativos. Las 3512 claves de cada idioma están cubiertas, con el mismo orden y marcadores que el inglés.
  Los dos generadores originales incorporan los tres idiomas y `--check`; se comprueban los números,
  las bifurcaciones y las piedras angulares. Inglés y los siete archivos de español no cambian.
  El generador de evolución reconoce el párrafo vigente del grimorio y evita duplicar el antiguo al regenerar.

## [1.4.0] — 2026-11-06

Más opciones para jugar en grupo y seguir progresando: contratos, logros, sendas hasta el nivel 50,
piedras angulares, alquimia, maestría y zonas de amenaza. Incluye un tutorial guiado y el grimorio actualizado.

### Añadido
- **Grupos**: abre el menú con P, invita a tus compañeros, comparte XP y presta apoyo a aliados.
  El grupo conserva sus miembros, opciones y nombres al reiniciar el mundo; el fuego amigo es configurable.
- **Contratos del gremio**: tres ofertas diarias por jugador, con encargos de caza, entrega, pesca,
  encantamiento, oficio, forja, eventos y jefes. Completa contratos para ganar recompensas y reputación.
- **17 logros** en la pestaña «Aptitudes» de los avances, con reconocimiento al entrar de los objetivos
  de estado que ya habías conseguido.
- **Roles hasta el nivel 50** y una senda exclusiva por rol, elegida entre tres. Siete escalones con una
  bifurcación de elección única y una piedra angular final; la vista «Sendas» permite consultar la progresión.
  Si tenías dos sendas, se conserva la de más rangos y se devuelven los puntos de la otra.
- **Piedras angulares**: 60 nodos finales con diez plantillas de efectos, sonidos e indicador en el HUD.
  Carnicería, Baluarte, Sobrecarga, Detonación, Contagio, Legión, Espectro, Fortuna, Tempestad y Santuario.
- **Maestría**: la XP nueva que sobra al máximo mejora las pasivas de habilidades y la eficiencia
  energética de roles. Cada punto aporta +0,5 %, hasta +10 % por defecto. K muestra los puntos en dorado;
  conserva sus diez tarjetas. La XP administrativa y los modos creativo/espectador no generan maestría.
- **Amenaza**: los hostiles nuevos del Mundo Normal se fortalecen por anillos de 1000 bloques desde la
  aparición del mundo, hasta V. Por anillo: +15 % de vida base, +10 % de daño, más probabilidad de élite
  y 10 % de probabilidad de una tirada extra de botín normal por baja de supervivencia. El HUD avisa al
  cambiar de anillo. Se excluyen jefes, invocaciones, criaturas de eventos y nombres previos.
- **Alquimia del Herbolista**: pociones de Maná, Frenesí, Velo del eclipse y Vigor del bosque; pétalos
  extraíbles del saquito de hierbas. Un Herbolista elegible acelera el destilado un 50 % y amplía su
  duración un 50 %. La XP por destilar tiene rendimiento decreciente; no se acumulan ampliaciones.
- **Tutorial de seis pasos**, con una esmeralda por paso y cobro único. El recordatorio se puede ocultar
  en Mayús + O; `/aptitudes tutorial status` consulta el progreso y `skip` lo omite de forma irreversible.
- **Grimorio actualizado**, con 23 capítulos, sendas, piedras angulares, logros y alquimia; consulta las
  recetas reales del juego. Los nodos de senda tienen iconos propios en el árbol y en la vista de sendas.
- **Configuración en el juego** para operadores, desde `/aptitudes config`, K o Mod Menu opcional.
  Los cambios pasan por validación del servidor y se sincronizan con los clientes.

### Cambiado
- **Eventos rotativos**: el Eclipse se alterna con la Niebla de almas y la Lluvia de brasas, sin repetir
  el evento anterior. La Niebla solo trae no muertos a pie, con variantes según el bioma.
- **Eclipse reforzado**: armadura y equipo en las oleadas, +25 % de vida y daño durante la Totalidad y
  esqueletos wither. La Lluvia de brasas incorpora meteoritos grandes/enormes y criaturas del Nether.
- **Interfaz**: K se desplaza si no cabe; el indicador del Eclipse deja libre la barra de acción.
  Textos nuevos en inglés y español, con seis variantes regionales idénticas al español de España.
  Portugués, francés y alemán quedan para una actualización posterior.

### Corregido
- El nombre guardado del líder del grupo se conserva al cargar, aunque no esté conectado.
- En Mayús + O, el texto de escala y la ayuda ya no se superponen a los botones.

### Validación
- T4–T6 probadas en partida según confirmación del autor el 2026-11-06; v15–v19 y v29–v41, el 2026-11-03.
  Las pruebas de regresión y limitaciones siguen documentadas en `docs/TESTING.md` y `README.md`.

## [1.3.7] — 2026-11-02

Primera versión preparada para publicar. Las etapas v34–v37 aún no se han probado en partida
(ver `docs/TESTING.md`).

### Añadido
- **v37 — Sonidos propios**: aviso, inicio y totalidad del Eclipse, llegada del Heraldo, remate de combo y
  engarzar/quitar runas (sintetizados por programa, con subtítulos).
- **v36 — Anti-granjas persistente**: el registro de bloques colocados se guarda con cada mundo.
- **v35 — Runas+**: runas de Escarcha, Trueno y Guardia; quitar la última runa en la Mesa del gremio
  (Mayús + clic derecho, cuesta esmeraldas); valores de las runas configurables.
- **v34 — El Eclipse**: evento cada 10 días con cielo rojo, oleadas de élites y campeones, el Heraldo del
  Eclipse y recompensas (Fragmentos, Núcleo y Runa del Eclipse). Comando `/aptitudes eclipse`.
- **v33 — Jefes propios** con fases en torres, santuarios y campamentos; sus núcleos mejoran armas de evolución.
- **v32 — Runas** engarzables en armas de rol (dos por arma).
- **v28 — Combos** de combate con bonificación de daño y remates por categoría de rol.
- **v27 — Mesa del gremio**: las armaduras de rol se fabrican en ella.
- **v26** — Colisión de las transformaciones ajustada al tamaño de la forma.
- **v25** — Texturas de las 80 piezas de armadura de rol.
- **v24** — Animaciones de lanzamiento e impacto de todas las habilidades.
- **v23** — El árbol crece al evolucionar: filas Maestro y Definitivo (200 nodos nuevos).
- **v22** — Texturas nuevas de armas y objetos clave.
- **v20–v21** — Grimorio interactivo con recetas reales; habilidad de Encantamiento; pantalla K animada.
- **v19** — Desplazamiento en la pantalla de roles; filtro de objetivos para criaturas con IA Brain.
- **v18** — Tiempos de espera persistentes y muros temporales seguros.
- **v17** — Anti-granjas de colocar y romper bloques.
- **v15** — Habilidad de Pesca y rendimiento decreciente de XP por fuente repetida.
- **v14** — Grimorio, elementos con fusiones y armas de evolución.
- **v10–v13** — Árbol ramificado, 20 roles, legendarias, estructuras, aldeano del gremio, élites, campeones
  y accesorios.

### Cambiado
- **v27** — Valores de los atributos −30 % por defecto.
- **v35** — Las esperas de las runas viven en memoria (el arma ya no se re-equipa al curar).
- **v37** — Los sonidos del Eclipse usan la categoría «Criaturas hostiles».

### Corregido
- Auditoría de seguridad: desbordamiento de enteros en la Hoguera, validación de la configuración y tope
  global del multiplicador de daño.
- v16: la Defensa solo da XP por daño recibido de verdad; los esbirros no se convierten en ahogados; el
  Bombardeo orbital no hiere a aliados.
- v15.1–v15.2: bloqueo de legendarias exclusivas en el árbol; la marca del grimorio sobrevive a la muerte.

### Pendiente
- Decidir si `attributeScaling = SOFTCAP` pasa a ser el valor por defecto.
