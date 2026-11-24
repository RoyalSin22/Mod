# Pendientes consolidados de Aptitudes

**Revisado el 2026-11-24** (rama `claude/practical-feynman-id8op1`, 165 commits). Reúne en un solo lugar lo que sigue abierto y dice
de qué archivo sale cada punto. Se elaboró leyendo el código y `git log`, no solo la documentación: donde un documento
decía una cosa y el código otra, se anota. **No es un plan aprobado:** ordenar o descartar puntos es decisión del autor.

Convenciones: **Hecho** = existe y compila; **Parcial** = existe con carencias descritas; **Sin hacer** = no hay código.
«Sin validar» = el autor aún no lo ha probado en partida. Las exclusiones de `AGENTS.md` §3 (linajes, razas, pactos)
no se proponen en ningún punto.

---

## 1. Funciones sin hacer

| Punto | Detalle | Origen |
|---|---|---|
| C7 · S3 Parada y golpe de gracia | Para jugadores. El «parry» que existe es el de los jefes (`boss/AnimatedBossEntity`, `GuardRules`) | `docs/PLAN_CLAUDE.md`, `docs/IDEAS_v47_v55.md` (S3) |
| C7 · S5 Mensajes en el suelo | Frases predefinidas, sin texto libre | `docs/IDEAS_v47_v55.md` (S5) |
| C7 · S6 Ecos de muerte | Silueta con los últimos segundos de otro jugador; exige red | `docs/IDEAS_v47_v55.md` (S6) |
| C7 · S4 Estamina y rodar | **Aplazado por decisión del autor** (2026-11-08) | `docs/PLAN_CLAUDE.md` |
| C2 · Rendimiento R1–R3 | Caché por jugador de `PathEffects.all` y `Keystones.active`; mapa id → piedra angular. `PathEffects.java:29` y `Keystones.java:103` recalculan en cada llamada | `docs/PLAN_CLAUDE.md`, `docs/IDEAS_v47_v55.md` (R1–R3) |
| R4 · Medir con spark | Servidor con 5–10 jugadores y eventos activos | `docs/IDEAS_v47_v55.md` (R4) |
| C1 · Revisión de código v42–v58 | No hay informe ni rama `fix/…` | `docs/PLAN_CLAUDE.md` |
| ~~T5 · Espera de 3 días del Behelit~~ | **Hecha** (`event/BeheritRules`, `EclipseState.lastEndTick`, `beheritCooldownDays`); sin validar en partida | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md`, D-20/D-25 |
| ~~T5 · Migración de configuración del calendario~~ | **Hecha** (revisión 84 de `ConfigMigration`; calendario apagado en `WorldEventManager`); los campos antiguos quedan solo por compatibilidad | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` (T5), `config/ConfigMigration` |
| T6 · Migración de los 3 sellos | Retirados sin alias ni conversión (`fe4f766`); D-25/D-26 pedían migración | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` (T6) |
| Regeneración de templos | `temple/TempleRecurrenceRules` no está conectada al juego; se compensó subiendo la frecuencia a 1/120 chunks (D-35). Se reabre si los Behelit no bastan | `docs/lore/DECISIONES_PENDIENTES.md` (D-33, D-35) |
| Mazmorra de las Cuatro Llaves «como tal» | Solo existen las cuatro cámaras y la Cripta de la Niebla | `docs/ROADMAP_APTITUDES.md` (v65, V69) |
| Cripta en templos antiguos | Solo los templos generados tras V69 la tienen | `docs/ROADMAP_APTITUDES.md` (V69), `docs/V69_CRIPTA_DE_LA_NIEBLA.md` |
| Logro «Resonancia perfecta» y estadística de bonificadores | Opcional de C5 | `docs/C5_PENDIENTES.md` |
| Tutorial: explicar las sinergias | Opcional de C5 | `docs/C5_PENDIENTES.md` |
| Y4 con más bonificadores (daño, velocidad) | Hoy solo defensa; decisión de balance | `docs/C5_PENDIENTES.md` |
| Entrada de bestiario del Custodio | D-10 sigue abierta | `docs/lore/DECISIONES_PENDIENTES.md` (D-10) |
| Pesca: hito que acorte la picada | Descartado en v15: exigiría un mixin | `docs/ROADMAP_APTITUDES.md` (Propuestas) |

## 2. Implementado con carencias (hallazgos de esta revisión)

| Punto | Carencia | Origen |
|---|---|---|
| Astrolabio del Ocaso (C3) | (a) Solo operadores; la decisión E1 añadía al líder del grupo. (b) Mensajes fijos en inglés (`EclipseSummoningRules`), no son claves de idioma. (c) Sin textura ni modelo. (d) El logro `summon_eclipse` existe como JSON pero nadie lo concede. (e) Posible solapamiento con el Behelit | `event/EclipseSummoningRules`, `block/BonfireBlock:49`, `item/SunsetAstralabeItem`, `data/aptitudes/advancement/aptitudes_achievements/summon_eclipse.json` |
| ~~Behelit con Eclipse activo~~ | **Resuelto**: no se consume y avisa en la barra de acción | `item/BeheritItem`, `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` §4 |
| ~~Pesos de eventos a 0~~ | **Corregido**: dejaba la Niebla y la Lluvia inutilizables en configuraciones nuevas; el defecto vuelve a 1 con migración | `config/AptitudesConfig:202-204` |
| Restos de los sellos retirados | `boss/SummonSealItem` ya no se usa; `tools/textures/gen_items.py` aún regenera sus PNG; comentario falso en `ModItems:183` («se mantienen para compatibilidad»); menciones en `docs/PLAN_AVANCE.md`, `docs/lore/09_CADENA_DE_PROGRESION.md`, `docs/PROMPT_ARTE_ARMAS.md` y en la entrada «v65 — Ruinas de batalla» del CHANGELOG | `fe4f766`, `item/ModItems`, `AGENTS.md` §2 |

## 3. Arte

| Punto | Estado | Origen |
|---|---|---|
| Iconos de las 200 habilidades nuevas F2–F7 (10 por rol) | Encargos redactados, arte sin entregar | `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`, `_F3` … `_F7`; `docs/ROADMAP_APTITUDES.md` (v67) |
| Arte del Grimorio restante | «Siguiente: el arte de los iconos y del grimorio» | `docs/ROADMAP_APTITUDES.md` (v67), `docs/PROMPT_ARTE_GRIMORIO.md` |
| Arte propio de la pestaña «Crónicas» | Pendiente | `docs/ROADMAP_APTITUDES.md` (V71) |
| Textura y modelo del Astrolabio del Ocaso | No existen | ver §2 |
| Fichas de categoría, pantalla K, roles | Encargos hechos y arte integrado en v66/v67; no consta pendiente | `docs/PROMPT_ARTE_FICHAS_CATEGORIA.md`, `PROMPT_ARTE_PANTALLA_K.md` |
| Sonidos propios por grabaciones reales | Opcional; mismos nombres de archivo | `docs/ROADMAP_APTITUDES.md` (v37) |
| Texto desfasado: «80 piezas de armadura» | Las 20 armaduras de rol se borraron en V80 (Armadura Ascendida); corregir ese pendiente | `docs/ROADMAP_APTITUDES.md` (v23, línea ~423) |

## 4. Validación en partida (todo lo siguiente: sin validar)

Los guiones están en `docs/TESTING.md`. El autor confirmó en partida v15–v19 y v29–v41 (2026-11-03), T4–T6 de
`PLAN_CODEX` (2026-11-06; **otra numeración que las tandas T1–T7 de la cadena narrativa**), el Errante Velado y su aparición
en la Niebla, y la Fase 6 del Osario Quebrado (V84).

| Bloque | Origen |
|---|---|
| Cadena narrativa T1–T7 (aldeano, portal, materiales, Templo del Custodio, llaves y cámaras, Behelit, Crónicas) | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md`, `docs/TESTING.md` |
| V69 Cripta, V70 Templo hondo (también balance), V71 Crónicas, V72 | `docs/ROADMAP_APTITUDES.md` (v65) |
| V68 Gran Osario y su trofeo; equilibrio estimado a partir del Coloso y el Autómata | `docs/V68_JEFE_OSARIO.md` (línea 137), `docs/TESTING.md` |
| C5 sinergias Y1–Y5 | `docs/V65_C5_TESTING_PLAN.md` |
| v67 F1 árbol unificado y F2–F7 (habilidades nuevas), grimorio G1–G5, tooltips de configuración | `docs/ROADMAP_APTITUDES.md` (v67) |
| v65: trance, pedidos, expediciones, arena y fase 3 de Guardián y Autómata; Coloso | `docs/ROADMAP_APTITUDES.md` (v65) |
| V64 templos (placas II–V, guardián evocador), V62 con Sodium/Indium | `docs/ROADMAP_APTITUDES.md` (v64, v62) |
| V47 Asalto del Eclipse y V52 Persecución | `docs/TESTING.md` (~927), `docs/V47_ECLIPSE.md` |
| V54 animaciones (prueba multijugador), V50, V49, V44 tutorial (muerte, reinicio, inventario lleno), V48 Reforja | `docs/ROADMAP_APTITUDES.md` |
| V77 efectos de batalla y V78 animaciones del cuerpo; V80 Armadura Ascendida; V83 Amenaza más dura | `docs/V77_EFECTOS_BATALLA.md`, `docs/V80_ARMADURA_ASCENDIDA.md`; **no consta confirmación del autor** |
| Escorificador de Hierro | `docs/TESTING.md` (~495) |

## 5. Traducciones

| Punto | Estado | Origen |
|---|---|---|
| pt_br / fr_fr / de_de | Cubren las 3512 claves por generadores; **revisión visual y con hablantes nativos pendiente**; no se presentan como revisadas | `README.md` (Limitaciones), `docs/ROADMAP_APTITUDES.md` (v45, v61, v67), `docs/TESTING.md` (~1018, ~1077) |
| Variantes `es_*` | Copias idénticas de `es_es`; sin adaptación regional | `AGENTS.md` §5, `README.md` |
| Textos fijos fuera de los JSON | `EclipseSummoningRules` (inglés) | ver §2 |
| Tooltips de configuración (192 opciones) en pt/fr/de | Revisión nativa pendiente | `tools/lang/config_catalog.py` |
| Traducciones nativas | **Aplazadas por decisión del autor** (no hay hablantes) | `docs/PLAN_AVANCE.md` |

## 6. Decisiones abiertas (las toma el autor)

| Decisión | Contexto | Origen |
|---|---|---|
| ¿Se acepta la retirada de los sellos sin migración? | D-25/D-26 pedían migración; `PLAN_AVANCE` §1 dice que no hay mundos que preservar | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` |
| ¿Se implementa la espera de 3 días del Behelit y qué hace con un Eclipse activo? | Hoy se pueden encadenar | ver §1 y §2 |
| ¿Se vuelven a subir los pesos de eventos o se migra la configuración? | Valor por defecto cambiado a 0 en `240484f` | `config/AptitudesConfig` |
| Astrolabio del Ocaso: ¿se mantiene, se amplía al líder del grupo o se retira? | Convive con el Behelit | `docs/PLAN_CLAUDE.md` (E1) |
| C1 y C7: ¿se hacen, se reprograman o se descartan? | C7 reutiliza el número «v67», ya ocupado por el árbol unificado | `docs/PLAN_CLAUDE.md` |
| `attributeScaling = SOFTCAP` por defecto | Decisión del autor tras probar | `AGENTS.md` §7 |
| Balance de los multiplicadores de C5 (1,20 / 1,15 / 0,90 / 1,15 / 1,20 / 1,10) | Tras validar en partida | `docs/C5_PENDIENTES.md` |
| D-07 papel del Coloso · D-08 Sellos y llaves · D-09 Eclipses posteriores · D-11 nombre del Behelit | Abiertas en el lore | `docs/lore/DECISIONES_PENDIENTES.md` |
| D-13, D-15, D-16 quién recibe los desbloqueos de lore; dónde viven las crónicas; definiciones de «primer encuentro» | Abiertas | `docs/lore/DECISIONES_PENDIENTES.md` |
| D-23 (10 días de la Niebla ¿desde el inicio o el fin?) y D-30 | Provisionales | `docs/lore/DECISIONES_PENDIENTES.md` |
| D-24, D-27, D-29 | Figuran como abiertas pero parecen cerradas por trabajo posterior (referencia del bestiario, T0/T1 autorizadas); confirmar y cerrar | `docs/lore/DECISIONES_PENDIENTES.md` |

## 7. Limitaciones conocidas

| Limitación | Origen |
|---|---|
| El mod se necesita en cliente y servidor | `README.md` |
| El filtro para mobs con IA Brain actúa cada 5 ticks (el warden puede llegar a golpear) | `README.md`, `AGENTS.md` §7 |
| Rangos de evolución y potenciación: activas combinadas limitadas a rango 6 y reducción de esperas al 60 % | `README.md` |
| Fuego del Averno y explosiones del Creeper modifican bloques (`uniqueGriefing`) | `README.md` |
| Quietud Eterna usa `NoAI` | `README.md` |
| La Amenaza se fija al aparecer la criatura; no se recalcula | `README.md` |
| El destilado mejorado exige un Herbolista elegible cerca | `README.md` |
| La transformación solo la ven los clientes que tienen el mod (el `README.md` aún dice que la caja de colisión es la del jugador, lo que contradice v26: revisar) | `README.md` (línea ~466), `docs/ROADMAP_APTITUDES.md` (v26) |
| Solo los templos nuevos tienen cripta; los antiguos (`layout = 0`) siguen con la lógica vieja | `docs/V69_CRIPTA_DE_LA_NIEBLA.md` |

## 8. Documentación desfasada o contradictoria detectada

| Dónde | Problema |
|---|---|
| `docs/CONTEXTO_CODEX.md` §4 frente a `AGENTS.md` §4 | CONTEXTO exige autorización por cada commit; AGENTS permite commit y push a la rama de trabajo sin pedirla. Manda `AGENTS.md`, pero el otro no está actualizado |
| `docs/CONTEXTO_CODEX.md` §6, §8 | Describe 8 activas por rol con 3 bits (`LoadoutCodec` va a 4 bits, hasta 16), «armaduras de rol» (sustituidas en V80) y una lista de pruebas muy anterior a las 133 clases actuales |
| `AGENTS.md` §4 | Nombra la rama `claude/v63d-correcciones`; la rama de esta sesión es otra. Conviene actualizarlo cuando el autor decida |
| `AGENTS.md` §7 | «Siguiente: v42–v45» y «T4–T6 ✔» usan una numeración distinta de las tandas T1–T7 de la cadena narrativa; fácil de confundir |
| `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` | Cabecera y tabla actualizadas en esta revisión; §7 («qué se necesita para empezar») y la mención a la rama `claude/aptitudes-lore-audit-htenjq` son históricas |
| `docs/C5_PENDIENTES.md` | Reescrito en esta revisión; la versión anterior describía C5 como no funcional |
