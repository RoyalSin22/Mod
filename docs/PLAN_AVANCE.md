# Plan de avance de Aptitudes

Estado: **borrador** (no integrado en `main`). Fuente: decisiones del autor tras los informes de arquitectura y
diseño. Cada decisión que no esté confirmada aparece en la sección «Pendiente de decisión».

## 0. Dirección elegida

Combinación confirmada por el autor: **B como base + núcleo de combate de A + contenido cooperativo de C**.

- **B (base):** sistemas complementarios existentes (roles, habilidades, Amenaza, eventos, contratos, grupos,
  alquimia, bestiario), mejorados y conectados entre sí. Sin rediseño del juego base.
- **A (núcleo de combate, solo jefes, élites y campeones):** postura, guardia perfecta junto al parry pasivo del
  Espadachín, y etiquetas explícitas de ataque. El combate contra mobs comunes no cambia.
- **C (contenido):** templos, expediciones y jefes reinvocables pensados para grupo, con recompensas individuales.
  Un jugador en solitario puede completarlos.
- **Objetivo:** una experiencia más completa, acorde con la concepción original del mod.

Fuera de alcance: rediseño del combate contra mobs comunes, progreso infinito, tablas globales, economía entre
jugadores, traducciones nativas (aplazadas).

## 1. Decisiones registradas

| Tema | Decisión |
|---|---|
| Destinatarios | Pack en solitario o con amigos; servidor de amigos (5–10 jugadores). No se diseña para servidores públicos masivos. |
| PvP | Existe y puede mantenerse. Las mecánicas nuevas deben indicar si afectan a jugadores. |
| Endgame | Experiencia **finita**: completar roles, jefes, templos y logros. No se diseña progreso infinito. |
| Runas | Pueden duplicarse; se fabrican o se consiguen como botín. |
| Legendarias exclusivas | Exclusividad **por mundo**. |
| Jefes | **Reinvocables**. |
| Templos | Una sola resolución por mundo; puede volver a generarse con probabilidad y las recompensas son **individuales**. |
| Friendly fire en áreas | Cada jugador puede desactivarlo para sí mismo. |
| Guardia perfecta y parry | Conviven: el parry pasivo del Espadachín y la guardia nueva. |
| Postura | Aplica a **jefes, élites y campeones**. |
| Mundos reales | No hay mundos de jugadores que preservar. La migración de datos puede priorizarse solo si cambia la estructura. |
| Árbol de habilidades | Se planea moverlo a **datapack** para no sobrecargar el mod. |
| Pruebas en partida | El autor probará los Game Tests; cuando haya un fallo, la IA puede reproducirlo y probarlo. |
| Traducciones | Se aplazan. No hay hablantes nativos disponibles por ahora. |
| Nombres | Se renombra la ascensión y se elimina cualquier referencia a «pacto». |
| Referencias externas | Sirven como inspiración de diseño, no como especificación. |

## 2. Pendiente de decisión

1. **Objetivo del proyecto.** Pedido por el autor: explicación con pros y contras de «RPG de acción completo» frente a
   «conjunto de sistemas complementarios». Pendiente de presentarlo antes de decidir.
2. **Cambios de balance.** El autor respondió «no se, tendrías que verlo tú o preguntarme antes». Regla: antes de
   tocar un valor por defecto de balance, preguntar.
3. **Balance de los templos regenerables.** Hay que fijar la probabilidad de reaparición y el límite por mundo.
4. **Reglas de postura.** Valores de daño de postura por ataque, recuperación y ventana de inmunidad.
5. **Reinvocación de jefes.** Coste (si lo hay), tiempo de espera y si el jefe conserva fase al reinvocarse.
6. **Parry frente a guardia perfecta.** Ventana, coste y relación entre ambos para el mismo rol.

## 3. Pendiente de validación en partida (v63d)

- Esferas del Autómata visibles en vuelo.
- Devolución y aturdimiento del Autómata.
- Orden de piezas del Guardián y ×1,5 cuando el núcleo queda expuesto.
- Corte del rayo del Autómata por bloques sólidos.
- Aldeano del gremio: verificado en partida.

## 4. Fases propuestas

Orden sugerido. Cada fase se integra por separado y con autorización explícita.

### Fase A — Infraestructura de combate (sin contenido nuevo) — **completada, sin commit**
Implementada: `boss/AttackTags` (etiquetas y reglas de bloqueo, parry y postura), `boss/PostureRules` (postura, aturdimiento
de 100 ticks, inmunidad de 300 ticks, vulnerabilidad ×1,5, recuperación) y sus pruebas `AttackTagsTest` y
`PostureRulesTest`. Los valores están elegidos sin playtest y quedan pendientes de validar en partida.
- Etiquetas tácticas de ataque (NORMAL, HEAVY, PARRYABLE, PROJECTILE, GROUND, UNBLOCKABLE) como datos puros.
- Reglas puras de postura: `posture`, `maxPosture`, `recovery`, `immuneUntil`, con pruebas JUnit.
- Pruebas parametrizadas para escenarios extremos de combate, sin cambiar balance.

### Fase B — Postura y guardia en jefes, élites y campeones — **en curso, sin commit**
Hecho (B1): postura en los jefes animados (`AnimatedBossEntity`). Solo drenan los golpes de jugadores; al romperse
el jefe queda aturdido 5 s con daño ×1,5 y no empieza ataques nuevos; se recupera 2 por segundo. Compila y las
353 pruebas pasan, pero el comportamiento no está validado en partida.
Pendiente:
- B2 (hecho en código, sin commit): `BossMoveRules.Move` lleva `tags` (`AttackTags`). Coloso: tajo NORMAL, golpe
  descendente HEAVY+GROUND, embestida HEAVY. Guardián: volley PROJECTILE, anillo GROUND, picado HEAVY+GROUND.
  Autómata: orbe PROJECTILE, pisotón GROUND, rayo PROJECTILE. Estas asignaciones son provisionales.
  Decisión abierta: la postura que causa un golpe del **jugador** (¿qué ataque del jugador es pesado?) no depende de
  las etiquetas del jefe. Por ahora el drenaje usa NORMAL; hay que definir la regla antes de usar HEAVY en ese caso.
- B3 (hecho en código, sin commit): parry contra el tajo del Coloso (etiqueta PARRYABLE). Si el jugador bloquea en
  el impacto, el golpe no llega y la postura del jefe baja 25. Decisión mía: sin ventana medida desde que se levantó
  el escudo, porque el proyecto no tiene seguimiento de ese tiempo. Pendiente: ventana de tiempo real (guardia
  perfecta), guardia con contraataque, y ampliar PARRYABLE a otros ataques.
- B4 (hecho en código, sin commit): `elite/ElitePosture`. Postura 60 para élites y campeones (ambos llevan la
  etiqueta de élite); solo la drenan los golpes de jugadores; al romperse se pausa la IA del mob 5 s y después se
  reanuda. Limitación: sin vulnerabilidad ×1,5 (los mobs no permiten multiplicar el daño sin mixins) y sin
  persistencia. Pendiente de validar en partida.
- B5 (hecho en código, sin commit): decisión mía. Los **jefes** guardan su postura actual en NBT
  (`aptitudes_posture`); el aturdimiento y la inmunidad no se guardan, así que al recargar no hay aturdimiento. Los
  **élites y campeones** no persisten su postura: son mobs comunes y su estado se pierde al recargar.
- B6 (hecho en código, sin commit): animación `stun` (2 s, un disparo: cabeza que se sacude y cuerpo que se hunde)
  en los tres jefes, generada por `tools/models/gen_boss_models.py` (`--check` correcto). Se dispara al romper la
  postura. Indicador: partículas de encantamiento sobre la cabeza mientras dura el aturdimiento. Las animaciones
  definitivas siguen pendientes de K5; esta es de prueba.
- Integrar la postura en los tres jefes animados y en élites y campeones.
- Guardia perfecta y parry: ventana y respuesta del servidor; sin invulnerabilidad prolongada.
- Validación en partida con el autor.

### Fase C — Reinvocación y templos regenerables
- C1 (hecho en código, sin commit): `boss/SummonRules` (puro) fija la espera de reinvocación: 20 minutos desde la
  última derrota, y solo si no hay otra instancia viva. Provisional.
  Decisiones del autor: se invoca con un **objeto**; el jefe **conserva su fase** al reinvocarse. El coste en materiales
  lo fija el proyecto: provisional, 4 Fragmentos de alma y 1 Corazón de campeón (Coloso), 1 Esencia de élite (Guardián y
  Autómata). Cableado en curso, en pasos. **Paso 1 hecho** (sin commit): `boss/SummonState` (estado persistente por
  mundo con la última derrota, la fase y la instancia viva de cada jefe) y `AnimatedBossEntity.onDeath`, que lo
  registra. **Paso 2 hecho** (sin commit): `boss/SummonSealItem` y tres objetos (`faceless_colossus_seal`,
  `dismantled_guardian_seal`, `ancient_automaton_seal`). Usar un sello comprueba `SummonRules`, cobra 4 Fragmentos de
  alma y el material propio, invoca al jefe junto al jugador y lo deja en la fase guardada. Compila, 383 pruebas.
  Limitaciones: la instancia viva solo se busca en el mundo donde se usó el sello; un jefe que nunca se derrotó
  también puede invocarse con el sello. **Paso 3 pendiente**: texturas, receta, nombres y textos en los 11 idiomas
  (sin ellos los objetos aparecen sin textura ni nombre y no se pueden fabricar).
- C2 (hecho en código, sin commit): `temple/TempleRecurrenceRules` (puro). Un templo resuelto no vuelve antes de 3
  días; luego la probabilidad sube linealmente hasta 25 % en 6 días. La tirada es determinista (0–999). Cada jugador
  reclama una vez por instancia (`claim`/`canClaim`). Provisional. Pendiente: el punto 3 del plan (probabilidad por
  mundo, que decides tú) y el cableado en `TempleFeature` y en el bloque del templo.

### Fase D — Contenido de endgame finito
- Templos ominosos (variante opt-in de un templo ya resuelto).
- Crónicas progresivas del grimorio (información por hitos, sin efecto de balance).
- Contratos rotativos (ampliación de `ContractRules`).

### Fase E — Infraestructura de pruebas y datos
- Game Tests mínimos: registro, persistencia y networking. El autor los prueba; yo reproduzco los fallos.
- Árbol de habilidades en datapack (diseño y migración a planificar tras la fase B).
- Desglose de build para depuración y para el jugador.

## 5. Ideas descartadas o aplazadas

- Casa de subastas global: descartada para un servidor de amigos.
- Progreso infinito y tablas globales: descartados por la decisión de endgame finito.
- Traducciones nativas: aplazadas.
- Modo daltónico: descartado por el autor.
- Ascensión con referencia a pactos: descartada por la exclusión de pactos.

## 6. Verificación

Cada fase debe cerrar con `./gradlew build` real y su salida. Si no se ejecuta, se indica explícitamente.
