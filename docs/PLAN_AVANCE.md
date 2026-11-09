# Plan de avance de Aptitudes

Estado: **borrador** (no integrado en `main`). Fuente: decisiones del autor tras los informes de arquitectura y
diseño. Cada decisión que no esté confirmada aparece en la sección «Pendiente de decisión».

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

### Fase A — Infraestructura de combate (sin contenido nuevo)
- Etiquetas tácticas de ataque (NORMAL, HEAVY, PARRYABLE, PROJECTILE, GROUND, UNBLOCKABLE) como datos puros.
- Reglas puras de postura: `posture`, `maxPosture`, `recovery`, `immuneUntil`, con pruebas JUnit.
- Pruebas parametrizadas para escenarios extremos de combate, sin cambiar balance.

### Fase B — Postura y guardia en jefes, élites y campeones
- Integrar la postura en los tres jefes animados y en élites y campeones.
- Guardia perfecta y parry: ventana y respuesta del servidor; sin invulnerabilidad prolongada.
- Validación en partida con el autor.

### Fase C — Reinvocación y templos regenerables
- Reinvocación de jefes con coste y espera definidos (pendiente, punto 5).
- Templos regenerables con probabilidad por mundo y recompensa individual (pendiente, punto 3).

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
