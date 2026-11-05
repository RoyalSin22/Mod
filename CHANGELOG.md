# Registro de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones con
[Versionado Semántico](https://semver.org/lang/es/). Las «vNN» son las etapas internas de desarrollo.

## [Sin publicar]

Compilado y probado en partida por el autor (v15–v19 y v29–v41 ✔).

### Corregido
- **T2 — Nombres de grupo al cargar**: `PartyState` conserva el nombre guardado del líder al leer el NBT.
  Antes lo omitía porque el constructor ya lo había añadido como miembro; sin el líder conectado podía
  aparecer «?». Los UUID repetidos siguen sin duplicarse y conservan el primer nombre leído.
- **v41.1** — Ajustes del HUD (Mayús + O): el texto de escala y la ayuda ya no se montan sobre los botones (la
  distribución se calcula por filas).

### Añadido
- **T3 — Grimorio v46**: capítulos «Sendas y piedras angulares» (siete escalones, exclusividad,
  bifurcación y las diez plantillas con sus valores por defecto) y «Logros» (los 17 objetivos y su
  concesión por el servidor). El índice conserva el orden anterior y ajusta sus filas para 22 capítulos.
  «Eventos del mundo» explica la Niebla de no muertos, los tres tamaños de meteorito y la armadura del
  Eclipse; el árbol refleja el nivel 50. Textos en inglés y las siete variantes de español, sin copiar recetas.
- **T2 — Pruebas de guardado**: pruebas nuevas de `ContractData.CODEC` con `JsonOps` (ofertas completas,
  progreso, reputación, cobros únicos, cambio de día y datos corruptos) y de `PartyState` con NBT
  (varios grupos, líder, nombres, opciones, datos incompletos y límites de pertenencia).
- **T1 — Iconos S1–S3, fase 2**: los nodos de senda de la cuadrícula de `RolesScreen` se dibujan con
  `NodeIconRenderer.draw`, con respaldo al icono de objeto y caché existente. Conservan su posición,
  tamaño, marcos, indicadores de rango y superposiciones de bloqueo.
- **T1 — Iconos S1–S3, fase 1**: 180 PNG nuevos para las 60 sendas, con los efectos de `paths(...)`
  y un marco de esquinas más sencillo que S4. El generador, `--check` y la hoja `--preview` cubren
  ahora los 480 iconos. Los 300 S4–S7 conservan sus píxeles. Integración en `RolesScreen`: véase fase 2.
- **Iconos de Sendas — fases 1–3**: 300 iconos originales de 16×16 RGBA para S4, S5x, S5y, S6 y S7,
  generados con Pillow desde los efectos de `RoleTree`. Incluye 19 glifos, marcos por escalón y color de
  senda, hoja de vista previa ×4 y comprobación `--check`. Los 60 S7 usan 10 glifos de piedra angular,
  emblema morado de borde dorado y franja de senda; asignaciones de la tabla de diseño §10 validadas.
  Clase de cliente `NodeIconRenderer`: dibuja el PNG o el objeto de `NodeIcons` si falta, con caché que se
  vacía al recargar recursos y atajo S7 que busca su nodo real. Pendiente la integración en las pantallas.
- **v46.1 — Piedras angulares**: el escalón S7 (nivel 50, 1 rango) de cada senda, con 10 plantillas (Carnicería,
  Baluarte, Sobrecarga, Detonación, Contagio, Legión, Espectro, Fortuna, Tempestad y Santuario), un sonido propio
  para cada una e indicador en el HUD (encima de la barra de habilidades). Todos los valores son configurables.
- **v46 — Nivel 50 y Sendas exclusivas**: el nivel máximo de rol sube a 50; las sendas pasan a ser exclusivas
  (una por rol) y cada una crece con S4 (nivel 26), una bifurcación de dos ramas en el 32 (se elige una) y S6
  (40, ×1,5): 240 nodos nuevos. Vista de sendas (botón «Sendas» en la pantalla de roles) con pestañas A/B/C,
  raíl de niveles, bifurcación y confirmación al elegir. Quien tenía dos sendas conserva la de más rangos y
  recupera los puntos de la otra. Las piedras angulares del nivel 50 llegan en la v46.1 (ver arriba).
- **v31 — Logros**: pestaña «Aptitudes» en la pantalla de avances con 17 logros (Hoguera, roles y evoluciones,
  combos, habilidades, runas, jefes, eventos del mundo, grupos y contratos). Los logros de estado se conceden también
  al entrar, así que los mundos anteriores no pierden lo ya conseguido.
- **v30 — Contratos del gremio**: Tablón de contratos con 3 ofertas diarias por jugador (fijas por día), 2–3
  activos, 8 tipos (Caza, Entrega, Pesca, Encantamiento, Oficio, Forja, Evento y Jefe), dificultad por
  reputación y por evolución (Élite y Legendario) y recompensas con reputación. La Caza y los Jefes cuentan
  también para el grupo.
- **v29 — Grupos**: menú con tecla P, invitaciones clicables con caducidad, XP compartida configurable,
  bonificación de grupo, protección contra fuego amigo, apoyo a aliados, HUD y persistencia en el Mundo Normal.
- **v41** — Eventos rotativos: Niebla de almas y Lluvia de brasas se alternan al azar con el Eclipse (sin
  repetir el anterior); `/aptitudes event`; pesos configurables.
- **v40** — Pantalla de configuración (`/aptitudes config`, pestaña ⚙ de la pantalla K o Mod Menu opcional): los
  operadores cambian la configuración del servidor en el juego; los tooltips de las runas muestran los valores reales.

### Cambiado
- **v41.2** — Eclipse más duro: armadura (+4 y piezas de malla, hierro en la Totalidad y diamante los campeones),
  +25 % de vida y daño en la Totalidad y esqueletos wither en sus oleadas. Lluvia de brasas con meteoritos
  pequeños, grandes y enormes (más explosión, más botín, restos de magma temporales) y criaturas del Nether
  (cubos de magma, blazes, piglins brutos, hoglins y esqueletos wither). Todo configurable.
- **v41.1** — La Niebla de almas solo trae no muertos a pie: zombis, zombis aldeanos, esqueletos y, según el
  bioma, ahogados y esqueletos pantanosos junto al agua, momias en climas cálidos y errantes en los fríos (ya no
  aparecen fantasmas).
- **v39** — La pantalla K se desplaza con la rueda cuando no cabe (escala de interfaz grande); la barra del
  Eclipse sube para no tapar los avisos de la barra de acción.

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
