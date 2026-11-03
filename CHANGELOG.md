# Registro de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones con
[Versionado Semántico](https://semver.org/lang/es/). Las «vNN» son las etapas internas de desarrollo.

## [Sin publicar]

### Añadido
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
