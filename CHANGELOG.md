# Registro de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones con
[Versionado Semántico](https://semver.org/lang/es/). Las «vNN» son las etapas internas de desarrollo.

## [Sin publicar]

### Añadido
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
