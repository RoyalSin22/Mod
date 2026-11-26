# Plan de identidad de roles (tras la auditoría de ChatGPT)

Origen: auditoría conceptual de ChatGPT (`docs/PROMPT_BRAINSTORM_ROLES.md` es el encargo). **ChatGPT no vio el código**: este documento
cruza sus propuestas con el repositorio real, separa lo que ya existe de lo que falta y lo ordena en versiones. Fecha de la revisión: 2026-11-25.

## 1. Lo que dijo ChatGPT y lo que el código dice

| Afirmación / propuesta | Realidad en el código | Decisión |
|---|---|---|
| «Dos ranuras de activas» (pregunta 3) | **4 casillas en total: 2 por rol** (`slot < 2 ? principal : secundario`, `RoleManager`). | Aclarado. El riesgo 13.1 (mecánica que obligue a equipar siempre las mismas 2 activas) es real pero está acotado a 2 por rol. |
| Combinaciones de la misma categoría «rotas» (Bersérker + Monje, Mago + Elementalista…) | Misma categoría está **permitida y bonificada** (`SPECIALIZATION_XP_BONUS`, +XP por especialización). | Mantener. Hay que medirlas, no prohibirlas. |
| Un «recurso único» nuevo por rol (furia, ki, almas, corrupción…) | **Muchos ya existen como nodos N1–N10** (`*Kit`): racha del Espadachín, combinaciones y Postura del tigre del Monje, Ira justa del Paladín, cadena de Concatenación y cargas de Maná cristalizado del Mago, marcas del Cazador, Ansia y Desesperación del Brujo, Presión del Mago pistolero, Armería viva del Invocador, Monedero y Crédito del Mercader, etc. Existe además la **sintonía de combate común** (`Trance`). | **No crear 20 barras nuevas.** Auditar cada rol: qué recurso tiene ya, si el HUD lo muestra y si el rol se juega distinto. Solo rellenar huecos reales. |
| Sistema central de eventos de combate | **No existe**: hay enganches repartidos (`Combos`, `RolePassives`, `AbilityManager`, `RoleEvents`, `ExtraKits`, `XpEvents`). | **Aceptada y es la prioridad técnica** (ver V93). Con distinción directo/derivado para evitar cadenas infinitas. |
| Telemetría | **No existe** ninguna. | Aceptada (V93), mínima y solo en servidor. |
| Presupuesto de invocaciones | **Parcial**: `Minions.MAX_MINIONS = 4` (con `Keystones.maxMinions`), y `AbilityState.trackSummon` registra dueño y duración de cualquier invocación. Mascotas del Domador y lobos del Druida no cuentan en ese tope. | Aceptada: presupuesto por jugador sobre el registro que ya existe (V94). |
| Accesibilidad: sliders de partículas, temblor, FOV, viñeteado, volumen | **Solo hay uno**: `fxIntensity` (0–1,5) + el ajuste de partículas de Minecraft. Cámara y pantalla se escalan juntas. | Aceptada (V92): sliders separados y distinción señal funcional / adorno. |
| Posturas de combate y locomoción por familia | No hay: solo gestos de habilidad (capa PAL prioridad 1500). | Aceptada pero **más adelante** (V97), primero un piloto. |
| Efectos para firmas, definitivas y piedras angulares antes que los 10 nodos extra (riesgo 13.2) | Cierto: solo las activas base tienen guion propio. | **Aceptada**, es la prioridad de contenido (V95–V96). |
| «Recurso del Druida depende de biomas verdes» | No es el caso de partida; es un aviso útil para el diseño. | Registrar como restricción. |
| Moneda del Mercader (pregunta 8) | Las activas gastan **esmeraldas reales** (`AbilitiesExtra.consume`; Monedero y Crédito ya amortiguan). | Decisión pendiente del autor (ver §3). |
| Estados «en memoria» | Cierto y ya documentado: al reiniciar se pierden rachas, marcas, ventanas. | Mantener; no persistir sin necesidad. |
| «Efectos de cámara no deben determinar impactos» | Se cumple: todo el FX es cliente; el servidor decide el daño. | Mantener como regla. |
| Mapa de recursos por rol de §2 y matriz de especialización | Es una **hipótesis de diseño** sin datos. | Usarla como objetivo a medir, no como especificación. |
| Nombre «Chamán» / «Elementalista» | `ElementalistKit` implementa los N1–N10 del Elementalista (llamados «Chamán» en nodos). | Sin acción; tenerlo presente al auditar. |

**Propuestas descartadas o dejadas fuera por ahora:** más roles; bonificaciones individuales para las 380 parejas (se usarán etiquetas
funcionales); minijuego de ritmo del Bardo con precisión de milisegundos (latencia); modificar la velocidad de ticks para el Mago
(ya se hace con estados de entidad).

## 2. Plan por versiones

Una versión por tarea y por rama (`AGENTS.md`). Orden por dependencias: lo que mide y protege antes de lo que añade.

| Versión | Contenido | Entrega | Esfuerzo |
|---|---|---|---|
| **V92 – Validación y accesibilidad** | (1) Pasar la **hoja de pruebas en partida** por los 20 roles (el autor) y corregir P0/P1. (2) Sliders de cliente separados: partículas, temblor, FOV, viñeteado, volumen de habilidades, efectos de otros jugadores (completos/reducidos/mínimos) y alto contraste de señales. (3) Marcar qué efectos son **señal funcional** (avisos de ataque enemigo) y no se atenúan. | Opciones en `ClientConfig` + `ConfigScreen`; sin tocar el servidor | B–M |
| **V93 – Eventos de combate y telemetría** ✅ *primera entrega hecha (2026-11-25); ver «Estado de V93» al final* (incluye línea base vanilla, tiempo en solitario por rol, esmeraldas consumidas y, si se decide, PvP) | Bus central en servidor: `AbilityCast`, `AbilityImpact`, `DamageDealt`, `DamageMitigated`, `EffectiveHeal`, `TargetMarked`, `CompanionCommand`, `CombatEnded`, con propietario, habilidad, objetivo y marca **directo/derivado**. Los `*Kit` se suscriben en vez de enganchar cada uno su evento. Telemetría mínima (daño efectivo/s, daño por recurso, curación efectiva y sobrecuración, activaciones derivadas, MSPT, entidades activas) con comando de operador para volcarla. | `ability/CombatEvents` (puro donde se pueda, con pruebas), migración gradual de enganches existentes, `/aptitudes telemetry` | M–A |
| **V94 – Presupuesto de invocaciones** ✅ *hecha (2026-11-26); ver §7* | Presupuesto ponderado por jugador sobre `AbilityState.trackSummon`: esbirros, lobos, mascotas, torretas, diablillos y espadas; tope configurable; política al desconectar, morir y cambiar de dimensión; validar PvP y grupo. Auditoría de rendimiento antes de fijar números. | `ability/SummonBudget` + configuración | M |
| **V95 – Feedback compartido** ✅ *hecha (2026-11-26); ver §8* | Lenguaje común de impacto: normal, pesado, crítico, bloqueo, interrupción, curación efectiva, recurso completo/insuficiente, definitiva lista. Se reutiliza en habilidades base, extras y pasivas (con confirmación breve, sin partículas constantes). | Extender `FxIds`/`FxClient` con un puñado de eventos genéricos | M |
| **V96 – Progresión audiovisual** | Firmas, definitivas y piedras angulares con motivo propio del rol (empezando por los 5 pilotos). Luego evoluciones Maestro/Definitiva. Los 10 nodos extra de cada rol al final. | `*Fx` por rol ampliados | A |
| **V97 – Posturas** | Piloto con 4 roles (Bersérker, Lancero, Monje, Mago) de postura de combate sobre la capa PAL; respaldo a la locomoción vanilla; precedencia muerte > reacción > habilidad > postura > locomoción. | PAL, validar versión 1.1.6 | M–A |
| **V98 – Identidad mecánica (pilotos)** | Auditar los recursos que ya existen y diseñar solo los huecos, empezando por los 5 pilotos: **Bersérker, Elementalista, Cambiaformas, Paladín, Ingeniero** (cinco problemas arquitectónicos distintos). | Cambios en los `*Kit` y HUD | A |
| **V100 – Activas en PvP** | Opción B (ver §5): `PvpRules`, escalado de daño de habilidad, rendimientos decrecientes del control, contrajuego del sigilo, invocaciones con dueño y límites anti-abuso, tras una opción de configuración. Empieza **después de V93**. | `ability/PvpRules`, ajustes en `Targeting` y `LivingEntityMixin` (sin mixin nuevo), configuración | A |
| **V99 – Equilibrio** | Pruebas por anillo de amenaza 0–V, jefes y eventos, y por parejas principal+secundario con las etiquetas funcionales (`BURST`, `SUSTAIN`, `CONTROL`, `SUMMON`, `SUPPORT`, `DEFENSE`, `MOBILITY`, `REACTION`, `RESOURCE`). | Con los datos de V93 | M |

Antes de V96–V99 conviene tener los datos de V92–V93; sin ellos se equilibra a ciegas.

## 3. Respuestas a las 10 preguntas de ChatGPT

| # | Pregunta | Estado |
|---|---|---|
| 1 | Eficacia en solitario del apoyo | **Decidido: todos los roles deben estar bien equilibrados en solitario** (incluidos Herbolista, Bardo, Paladín y Druida). Ver §5. |
| 2 | Qué puede usar el rol secundario | **Por verificar en el código** (casillas 2/2 sí; pasivas, firma y evoluciones del secundario hay que auditarlas). Es la pregunta más condicionante. |
| 3 | Casillas | **Respondida: 2 por rol, 4 en total.** |
| 4 | Recursos exclusivos actuales | **Parcialmente respondida** (ver §1): hay que hacer el inventario completo por rol. |
| 5 | Firma, definitiva, evolución Definitiva y legendaria | Existen las cuatro; falta documentar la jerarquía de poder en `docs/BALANCE.md`. |
| 6 | Armas por rol | Cada rol tiene arma de evolución propia; falta confirmar qué roles admiten armas vanilla. |
| 7 | PvP | **Decidido: PvP siempre activo, como en Minecraft normal.** Hallazgo: hoy las activas **nunca golpean a jugadores** (`Targeting.isHostileImpactTarget` excluye `PlayerEntity`). Ver §5. |
| 8 | Monedas del Mercader | **Decidido: las esmeraldas siguen siendo economía real.** Se descarta el «capital operativo» de ChatGPT como sustituto. Ver §5. |
| 9 | Límites de entidades | `MAX_MINIONS = 4` para esbirros; el resto sin tope común. Se resuelve en V94. |
| 10 | Combate vanilla vs activas del mod | **Decidido: el combate vanilla debe seguir siendo competitivo.** Ver §5. |

## 5. Decisiones del autor (2026-11-25) y consecuencias

| Decisión | Consecuencias para el plan |
|---|---|
| **Todos los roles bien equilibrados en solitario** | Se elimina la hipótesis de «apoyo = bajo daño». Herbolista, Bardo, Paladín y Druida necesitan una vía ofensiva propia suficiente para matar y una vía de auto-sostén; sus mejoras de grupo son un extra, no su razón de ser. La matriz de especialización de ChatGPT se reinterpreta: la columna *Apoyo* sube el techo en grupo, pero no baja el suelo en solitario. Métrica de V93/V99: **tiempo para derrotar a un mismo enemigo en solitario** comparable entre roles (banda objetivo a fijar con datos), no solo DPS. |
| **PvP siempre activo, como en Minecraft normal** | **Hallazgo: hoy las activas nunca golpean a jugadores** (`Targeting.isHostileImpactTarget` excluye `PlayerEntity`, igual que `hostilesAround`). En PvP un jugador con rol solo pelea con combate vanilla + atributos, pasivas, armadura Ascendida (+4 % cuerpo a cuerpo) y runas, contra jugadores sin rol o con otro rol. Hay que decidir (**pregunta nueva, ver abajo**) si las activas pasan a afectar a jugadores. Si lo hacen: reducción de daño y control contra jugadores, rendimientos decrecientes del control (inmovilización, ceguera, aturdimiento), sigilo del Asesino y Cambiaformas con contrajuego, esbirros y torretas con reglas de objetivo, respeto de grupo/aliados (`PartyManager.isAlly`) y anti-spawn-kill. Si no lo hacen: documentarlo en el Grimorio y asegurar que ningún efecto pasivo o buff dé ventaja desproporcionada frente a un jugador sin rol. |
| **Las esmeraldas siguen siendo economía real** | El Mercader gasta esmeraldas de verdad (`AbilitiesExtra.consume`). Se descarta la barra «capital operativo». Reglas: (1) el coste por daño debe ser razonable frente a lo que una esmeralda vale en el comercio vanilla; (2) **ninguna habilidad puede generar esmeraldas** (ni «rendimiento táctico» ni reembolsos que den ganancia neta) para no abrir granjas; (3) Monedero y Crédito son amortiguadores, no fuentes; (4) en solitario debe ser viable con las esmeraldas que un jugador razonable obtiene (hay que medirlo: telemetría de esmeraldas consumidas por combate). |
| **El combate vanilla debe seguir siendo competitivo** | Las activas no pueden dejar obsoleto el golpe, el arco, el escudo ni las pociones. Métrica nueva en V93: **línea base vanilla** (espada de diamante/netherita + armadura + escudo + arco/ballesta) contra los mismos escenarios, a distintos niveles de progresión. Un rol que mate mucho más rápido que la línea base en todas las condiciones sale de banda; uno que mate mucho más lento, también. Las bonificaciones del rol deben **potenciar** el combate vanilla (p. ej. críticos, bloqueos, flechas) y no solo sustituirlo; vigilar `extraSkillDamageScale` y el bonus de +10 %/+4 % de la Ascendida. |

### Decisión B (2026-11-25): las activas afectan a jugadores

El autor elige la **opción B**: las activas pueden golpear a otros jugadores con escalado propio. Nueva versión **V100 – Activas en PvP**, que
no debe empezar hasta tener V93 (telemetría y línea base vanilla), porque necesita datos para fijar números.

**Superficie del cambio (medida en el código):** 119 usos de `isHostileTo`/`hostilesAround` y varios `isHostileImpactTarget` en los `*Kit`
(Herbolista, Mercader, Espadachín, Cambiaformas…). La puerta central es `Targeting.isHostileImpactTarget`, que hoy excluye `PlayerEntity`;
abrirla afecta a todas las activas a la vez, así que debe ir **detrás de una opción de configuración** y con pruebas.

**Piezas del diseño:**

| Pieza | Cómo |
|---|---|
| Regla de objetivo (`PvpRules`, puro, JUnit) | Un jugador es objetivo válido si el PvP del servidor está activo (`server.isPvpEnabled()`), no es aliado de grupo (`PartyManager.isAlly`), no es el propio jugador, no está en espectador/creativo y no tiene protección de reaparición. Los mobs hostiles siguen igual. |
| Escalado de daño contra jugadores | Factor configurable (`pvpAbilityDamageScale`). El hueco existe: `LivingEntityMixin` ya modifica el daño entrante al principio de `damage`, **sin mixin nuevo**. Falta distinguir daño de activa de golpe vanilla: hoy muchas activas usan `playerAttack`, igual que el cuerpo a cuerpo, así que hace falta un **contexto de «daño de habilidad»** (marca temporal en el servidor durante el lanzamiento y en los temporizadores de las activas con repeticiones). |
| Control contra jugadores | Duración máxima por tipo de control, **rendimientos decrecientes** (cada control seguido del mismo tipo dura menos) e inmunidad breve tras un control duro (inmovilización, ceguera, aturdimiento, levitación). Un punto central en `effect(...)` de cada kit o un contenedor común. |
| Sigilo e invisibilidad (Asesino, Cambiaformas, Sombra) | Contrajuego: partículas o brillo al revelarse, y el sigilo no anula la detección por el nombre del jugador en un radio corto. |
| Invocaciones y torretas | Los esbirros, lobos, mascotas y torretas solo atacan a jugadores si su dueño está en PvP con ellos; nunca a aliados de grupo. Se enlaza con V94. |
| Esmeraldas (Mercader) | Las monedas no se pueden «recuperar» al golpear jugadores (sin ganancia neta); no se puede robar el botín con una activa. |
| Anti-abuso | Protección tras reaparecer, límite de ráfaga de daño por segundo contra jugadores y registro de la telemetría de PvP. |
| Feedback | Los efectos visuales ya se envían a todos los cercanos; añadir avisos funcionales (marca de ataque entrante) que no se atenúen con `fxIntensity` bajo. |

**Decisiones de configuración (2026-11-25):** la opción de PvP con activas (`pvpAbilities`) nace **activada**; la protección tras reaparecer
se aplica a **todos los jugadores**, tengan rol o no. Como nace activada sin datos previos, V100 debe incluir una **válvula de seguridad**:
`pvpAbilityDamageScale` conservador al principio y la opción editable en caliente desde la pantalla de configuración para poder apagarla
sin reiniciar.

**Pruebas necesarias:** pares de jugadores con y sin rol, principal+secundario, con y sin Ascendida; objetivo en grupo; en creativo; en
espectador; con escudo; con Totem; en la Niebla, la Lluvia y el Eclipse (eventos con muchos mobs y jugadores).

**Riesgos:** dominancia de burst y de control en PvP; los roles sigilosos y los de invocación pueden ser inaceptables sin límites; la moneda
del Mercader da ventaja a quien tenga más esmeraldas.

### Pregunta nueva que condiciona V98–V99

**¿Las activas de los roles deben poder golpear a otros jugadores?** Hoy no. Opciones:

| Opción | Ventajas | Riesgos |
|---|---|---|
| A. Siguen siendo solo PvE | Equilibrio de PvP sigue siendo el de vanilla; menos trabajo y menos abuso. | Un rol es casi irrelevante en PvP salvo por pasivas/atributos; el Asesino, el Mago, etc. «no existen» frente a jugadores. |
| B. Afectan a jugadores con escalado propio | Coherencia: el rol sirve en PvP, como el resto del juego. | Mucho trabajo de equilibrio (daño, control, sigilo, invocaciones), riesgo de exploits y de dominancia de burst/control. |
| C. Mixta | Solo algunas activas (daño directo) afectan a jugadores; el control duro y las invocaciones no. | Reglas más difíciles de explicar. |

Mi recomendación provisional: **C**, con daño reducido y sin control duro contra jugadores, y decidir tras los datos de V93. Sin tu respuesta no empezaría V98.

## 4. Próximos pasos concretos

1. El autor prueba en partida los 20 roles con la hoja de QA (registrar P0–P3) y responde 1, 7, 8 y 10.
2. Yo verifico en el código la pregunta 2 y hago el inventario de recursos por rol (sin cambiar nada).
3. Empezar por **V92** y **V93**; no abrir V96 hasta tener datos.
4. ~~Decidir si las activas golpean a jugadores~~ **Decidido: B** (§5), con la opción de PvP **activada por defecto** y la protección de reaparición para **todos los jugadores** (no solo los de rol).

## 6. Estado de V93 (2026-11-25)

**Hecho:** `CombatEvents` (bus, alcance por habilidad que sobrevive a las tareas diferidas, daño derivado con profundidad), `CombatTelemetry`
(daño por origen, energía gastada, bajas y tiempo hasta derrotar; grupo `VANILLA` = línea base), `CombatTelemetryRules` (puro, con pruebas) y
`/aptitudes telemetry`.

**Cómo usarlo para equilibrar:** `/aptitudes telemetry label osario` (operador) antes de un escenario, jugar, y `/aptitudes telemetry roles`
para comparar dps, parte de daño de habilidad frente a vanilla, daño por energía, vida derrotada por segundo y bajas por rol, con la línea base
de jugadores sin rol en la misma etiqueta. `reset` vacía los datos.

**Limitaciones conocidas:**
- Es en memoria: se pierde al reiniciar.
- El daño de pasivas que no pasa por un oyente y ocurre fuera de una activa se clasifica por tipo de daño (mágico, explosión…) como derivado;
  el golpe, el arco y el tridente cuentan como vanilla. No es una atribución perfecta.
- ~~El daño de esbirros, lobos, mascotas y torretas no se atribuye al dueño~~ Resuelto en V94.
- Falta la **curación efectiva** (no hay un punto único de curación en el código) y las **esmeraldas consumidas** del Mercader.
- Los `*Kit` aún no usan el bus; se migrarán por roles en versiones posteriores sin cambiar su comportamiento.

## 7. Estado de V94 (2026-11-26)

**Hecho:** `SummonBudgetRules` (puro, con pruebas), opción `summonBudget` (16 por defecto, 0 = sin límite), `AbilityState.enforceBudget`
al registrar cada invocación, `discardSummonsOf` (al morir el dueño y al cambiar de dimensión) y atribución al dueño del daño y las bajas
de invocaciones y mascotas domesticadas (`CombatEvents.ownerOf`), visible como columna «autónomo» en `/aptitudes telemetry`.

**Costes:** esbirro y esqueleto 2, diablillo 1, lobo 3, golem y torreta 4, cualquier otro 2. Salen primero las más antiguas.

**Limitaciones y decisiones abiertas:**
- Los 16 puntos permiten, por ejemplo, 4 esbirros (8) + 2 lobos (6) + 1 diablillo (1) sin recortes; apilar tres roles de invocación sí recorta.
  Es un valor inicial: hay que medirlo con la telemetría («autónomo» por rol) y ajustarlo.
- No hay aviso al jugador cuando se retira una invocación por el presupuesto (como ya ocurre con el tope de esbirros).
- Las espadas del Invocador de armas, las marcas del Cazador y otros estados virtuales **no son entidades** y no cuentan.
- Las mascotas vanilla del Domador (domesticadas) no consumen presupuesto (no son invocaciones), pero su daño sí se atribuye.
- El límite de actualización de IA y la persistencia al reiniciar siguen como estaban (las invocaciones son efímeras).

## 8. Estado de V95 (2026-11-26)

**Hecho:** `ImpactFeedbackRules` (puro, con pruebas), `ImpactFeedback` (oyente del bus de V93), `CombatEvents.heal` (curación efectiva y
sobrecuración, también en la telemetría), eventos `FEEDBACK_IMPACT`/`BLOCK`/`HEAL` (ids 64–66), `FeedbackClientFx` y cinco sonidos.

| Señal | Cuándo | Visual | Sonido |
|---|---|---|---|
| Golpe de habilidad | Daño de una activa que no es pesado ni crítico | Chispas del color del rol | El de la propia habilidad |
| Golpe pesado | ≥ 20 % de la vida máxima de la víctima o ≥ 8 de daño | Ráfaga, anillo y polvo | Golpe grave |
| Crítico | Crítico vanilla del jugador (cae, no corre, no está en agua ni ciego ni montado) | Destello y estrella de cuatro puntas | Chasquido agudo |
| Bloqueo | Un escudo bloquea un golpe al jugador (máx. cada 0,5 s) | Chispas metálicas hacia delante | Choque metálico |
| Curación efectiva | Una activa cura de verdad ≥ 0,5 de vida (máx. cada 0,5 s por objetivo) | Ascenso de luz verde y un corazón | Dos notas cálidas |
| Recurso insuficiente | Falta energía al lanzar (máx. cada 0,5 s) | — | Dos golpecitos sordos, solo para el jugador |

**Decisiones de diseño:** los golpes vanilla normales no dan señal (ya tienen la suya) y el daño autónomo de invocaciones tampoco; se
limita la frecuencia por atacante, víctima y jugador para que una ráfaga de golpes no sature.

**Limitaciones y pendiente:**
- La curación solo se cuenta en las habilidades que llaman a `CombatEvents.heal` (Bálsamo, Lluvia sanadora, Consagración, Sifón de
  almas); el resto (pasivas, curaciones de otros roles, Estus) sigue sin señal ni telemetría hasta que se migren.
- «Recurso completo» y «Definitiva disponible» **no** están hechos: necesitan un cambio del HUD (marco o pulso en el indicador) y un
  seguimiento del enfriamiento de la definitiva; se dejan para V92/V96.
- La detección del crítico es una aproximación de la regla vanilla (no hay evento de crítico).
- Las señales funcionales (aviso de ataque enemigo) siguen sin atenuación propia: es parte de V92.

## 9. Estado de V96 (2026-11-26) — 1.ª tanda

- Hecho: firma y definitiva de los cinco pilotos con gesto de cuerpo, encuadre de partículas, cámara y sonido propios (`GrandFx`, `GrandClientFx`, ids 67–68,
  `grand.json`, 6 sonidos). Documentado en `docs/V77_EFECTOS_BATALLA.md` (fase 11).
- Hecho (2.ª tanda): piedras angulares, guion por plantilla (10) con el id 69 (`KeystoneClientFx`), enganchado en `Keystones`.
- Hecho (3.ª tanda): firmas y definitivas de los otros 15 roles (`GrandFx` ya cubre los 20; 15 sonidos `grand_<rol>`).
- Hecho (4.ª tanda): activas de Maestro/Definitivo por plantilla (id 70, `EvolvedClientFx`, 10 sonidos `evolved_*`).
- Hecho (5.ª tanda): las 100 activas de los nodos extra se asignan a las formas de la 4.ª tanda (`ExtraFxShapes`). Las 100 pasivas no tienen guion.
- V98 (1.ª entrega, 2026-11-26): inventario de los cinco pilotos y recursos ocultos visibles en el HUD; ver `docs/V98_IDENTIDAD_MECANICA.md`. Mecánicas nuevas: ninguna hasta tener datos.
- V97 (piloto de posturas) hecha el 2026-11-26: ver `docs/V77_EFECTOS_BATALLA.md` fase 12. Falta verla en partida y decidir si se extiende a más roles.
- Pendiente de V96: revisar en partida la forma elegida para cada activa y, si hace falta, guiones propios para las más emblemáticas (N10).
- Limitaciones: sin probar en partida; la Baliza del Ingeniero no distingue «fijar» de «teletransportar»; los sonidos solo están medidos.
