# Pendientes consolidados

**Revisado el 2026-11-24** contra el código y `git log` (rama `claude/fervent-hamilton-vieoz9`, 165 commits). Es un índice:
cada punto remite al documento o archivo donde se explica. Lo que no pude comprobar en el código lo marco **(sin
verificar)**. Estado de las tareas de Claude: `docs/PLAN_CLAUDE.md`; sinergias: `docs/C5_PENDIENTES.md`; cadena
narrativa: `docs/lore/10_PLAN_DE_IMPLEMENTACION.md`; inventario visual: `docs/PROMPTS_REDISENO.md`.

## 1. Funciones sin hacer o a medias

| # | Qué | Estado | Origen |
|---|---|---|---|
| F1 | **E10 / T5: retirar el calendario rotativo.** El Eclipse sigue programándose por calendario (`WorldEventManager.calendarWeights`, `kindForDay`). `eclipseFirstDay`, `eclipseIntervalDays` y `worldEventWeight*` siguen en la configuración como obsoletos, sin migración | Sin hacer | `docs/lore/09_CADENA_DE_PROGRESION.md` (A-6, E10); `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` (T5); `config/AptitudesConfig.java:161,200`; `config/ConfigMigration` |
| F2 | **T6: migración de los 3 sellos de invocación.** Sus `register(...)` están comentados en `ModItems` (183–185), sin alias ni conversión; los objetos de mundos viejos se perderían. `boss/SummonSealItem.java` sigue ahí | Parcial; política sin decidir | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` (T6, §4.7); `item/ModItems.java:183` |
| F3 | **C2 Rendimiento:** caché por jugador de `PathEffects.all` y `Keystones.active`, mapa id → piedra angular, medir con spark | Sin hacer | `docs/PLAN_CLAUDE.md` (C2); `ability/PathEffects.java:29`, `ability/Keystones.java:103` |
| F4 | **C7 Combate avanzado:** S3 parada y golpe de gracia, S5 mensajes en el suelo, S6 ecos de muerte (S4 estamina y rodar: aplazado por el autor) | Sin empezar | `docs/PLAN_CLAUDE.md` (C7) |
| F5 | **C1 Revisión de código de v42–v58:** no hay informe en el repositorio | Sin informe | `docs/PLAN_CLAUDE.md` (C1); solo existe `docs/REVISION_MIXINS.md` |
| F6 | Aviso de 60 s con campana antes del Eclipse convocado (el diseño del Astrolabio lo pedía; hoy empieza al instante) | Sin hacer | `docs/IDEAS_v47_v55.md` §2b |
| F7 | `TempleRecurrenceRules` (regeneración de templos) no está conectada al juego | Sin hacer | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` (T3, D-33); `temple/TempleRecurrenceRules.java` |
| F8 | Gran Osario: el cliente debe conectar capas, visibilidad de núcleos y brazos, postura y clips exclusivos al estado de juego | Parcial | `docs/V85_GRAN_OSARIO_MEDIDAS.md`; `client/render/BossRenderers` |
| F9 | Logro «Resonancia perfecta», estadística de bonificadores, sinergias en el tutorial, partículas al activarse una sinergia | Opcional | `docs/C5_PENDIENTES.md` |
| F10 | Armadura Ascendida: revisión de cifras y, si se quiere, modelo 3D propio | Opcional | `docs/V80_ARMADURA_ASCENDIDA.md` (Pendiente) |
| F11 | Texturas de Codex de los jefes (`codex/texturas-jefes`, no está en el remoto) y revisar el CI de GitHub en `main` | Sin verificar | `docs/ROADMAP_APTITUDES.md:86` |

## 2. Arte

| # | Qué | Origen |
|---|---|---|
| A1 | ~~Astrolabio del Ocaso y Embrión del Umbral sin modelo ni textura~~ **Hecho (2026-11-25):** plantillas `astrolabe` y `embryo` en `tools/textures/gen_items.py`, PNG 16×16 y modelos `item/generated`. El Astrolabio tiene además versión de 32×32 (`tools/textures/hd_items.py`). Arte provisional, como el resto de objetos (ver A3) | `item/ModItems.java:66,136`; `tools/textures/gen_items.py` |
| A2 | Modelos 3D: hacha de Bersérker y armas Maestro (M1), armas base (M2), Armadura Ascendida (M3), Hoguera (M4), bloques funcionales (M5), jefes y criaturas con GeckoLib (M6; «modelos definitivos K5») | `docs/PROMPTS_REDISENO.md`; `docs/V63_JEFES.md` |
| A3 | Iconos de objetos que no son armas (≈70), texturas del aldeano del gremio y del mob de la Niebla, efectos de estado y partículas | `docs/PROMPTS_REDISENO.md` (A1–A3) |
| A4 | Interfaz: limpieza del Grimorio (23 ilustraciones sin usar), HUD, pantallas sin kit (Reforja, frascos, contratos, grupo, configuración) | `docs/PROMPTS_REDISENO.md` (I1–I3) |
| A5 | Iconos de las habilidades nuevas F2–F7 (provisionales) | `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`, `…_F3` a `…_F7`; `docs/PROMPTS_REDISENO.md` (I4) |
| A6 | Arte propio de la pestaña «Crónicas» y arte del bestiario (`docs/PROMPT_ARTE_BESTIARIO.md` era una propuesta, no existe) | `docs/ROADMAP_APTITUDES.md:68`; `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` §5 |
| A7 | Sonidos propios generados por síntesis (sustituibles por grabaciones) y procedencia/créditos del arte de IA | `docs/PROMPTS_REDISENO.md` (Q4, Q5); `docs/ROADMAP_APTITUDES.md:495`; `CREDITS.md` |

## 3. Validación en partida (nada de esto se ha probado en el juego)

| # | Qué | Origen |
|---|---|---|
| V1 | Ronda general: templo hondo, cripta, Crónicas, Osario, F2–F7, armadura, armas | `docs/PROMPTS_REDISENO.md` (Q1) |
| V2 | Cadena narrativa T1–T4: Niebla por aldeano, Lluvia por portal, materiales, templo, llaves y cámaras | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md`; `docs/TESTING.md` |
| V3 | **C3 (este cambio):** Astrolabio como operador, como líder del grupo y como jugador normal; rechazo sin consumir; espera de 3 días tras terminar un Eclipse (y tras `/aptitudes eclipse stop`); Behelit con la misma espera; Nether/End rechazados; logro | `docs/CONFIGURATION.md` (`eclipseSummonCooldownDays`); `event/EclipseSummoningRules` |
| V4 | Sinergias Y1–Y5 (porcentajes observables) | `docs/V65_C5_TESTING_PLAN.md` |
| V5 | Asalto y persecución del Eclipse; Eclipse con pruebas visuales | `docs/V47_ECLIPSE.md`; `README.md` («Limitaciones conocidas») |
| V6 | Jefes: trance, pedidos, expediciones, arena y fase 3; Guardián y Autómata (fases 3); Osario V84 a 20 TPS, rendimiento P1/P3 y navegación con desniveles | `docs/ROADMAP_APTITUDES.md:86,94`; `docs/V84_OSARIO_FASE6.md:97,229` |
| V7 | Remates 3D y multijugador; compatibilidad con Sodium/Indium; configuración, tutorial y reforja en partida | `docs/ROADMAP_APTITUDES.md:123,175,516,541` |
| V8 | Equilibrio con datos reales (Q2) | `docs/PROMPTS_REDISENO.md` (Q2); `docs/BALANCE.md` |

## 4. Traducciones

| # | Qué | Origen |
|---|---|---|
| L1 | Revisión nativa de pt_br, fr_fr y de_de (cobertura completa de las claves, pero sin revisar por hablantes; incluye los textos de C3 y las 192 descripciones de configuración) | `README.md` (tabla «Limitaciones conocidas»); `docs/ROADMAP_APTITUDES.md:50,135,237` |
| L2 | Revisión visual de las traducciones en el juego (textos largos en botones y tooltips) | `docs/ROADMAP_APTITUDES.md:237` |
| L3 | Las seis variantes `es_*` copian es_es (decisión de diseño, no un fallo) | `AGENTS.md` §5 |

## 5. Decisiones abiertas

| # | Pregunta | Origen |
|---|---|---|
| D1 | A-8: los 10 días de la Niebla, ¿desde que se inicia o desde que termina? (hoy, desde que se inicia, provisional) | `docs/lore/09_CADENA_DE_PROGRESION.md` |
| D2 | Tabla §4 de T0 (nombre y receta del objeto de acceso, contenido de las cámaras, templos viejos, nombre del Behelit…): se implementó con valores elegidos por el redactor, sin confirmación expresa | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` §4 |
| D3 | Política de migración de los sellos (alias, conversión o ítem inerte) | `docs/lore/10_PLAN_DE_IMPLEMENTACION.md` §4.7 |
| D4 | **Jugador solo y sin permisos:** no puede usar el Astrolabio (no es operador ni líder de un grupo). ¿Se permite al jugador único, o se obliga a crear grupo? | `docs/PLAN_CLAUDE.md` (E1); `event/EclipseSummoningRules` |
| D5 | **Obtención del Astrolabio:** el diseño decía botín raro o trueque (`docs/IDEAS_v47_v55.md` §2b, E4), pero la receta actual lo fabrica en la Mesa del gremio con un Fragmento de Eclipse, bloques de hierro y de cobre (`recipe/sunset_astrolabe.json`). ¿Se mantiene? | `recipe/sunset_astrolabe.json` |
| D6 | ¿Debe el Behelit pedir permiso en multijugador, como el Astrolabio? Hoy no (cualquiera lo usa) | `docs/lore/09_CADENA_DE_PROGRESION.md` (riesgos de diseño) |
| D7 | C1: ¿se da por hecha la revisión o se rehace? C7: ¿se retoma? Ojo con el nombre «C7», usado también en el commit `b0b1003` (Cripta, V69) | `docs/PLAN_CLAUDE.md` |
| D8 | Balance de multiplicadores de sinergias (1,15 → ¿1,10?) | `docs/C5_PENDIENTES.md` |
| D9 | `AGENTS.md` §7 dice que activar `attributeScaling = SOFTCAP` es una decisión pendiente, pero el código ya lo trae por defecto (`config/AptitudesConfig.java:326`, commit `8e2ab95`). Actualizar `AGENTS.md` | `AGENTS.md` §7 |

## 6. Limitaciones conocidas (del `README.md`)

| Limitación | Origen |
|---|---|
| El mod se necesita en cliente y servidor | `README.md` («Limitaciones conocidas») |
| Mobs con IA Brain (piglins, hoglins, warden): el filtro de objetivos actúa cada 5 ticks | `README.md`; `ability/AbilityState.shouldBlockTarget` |
| Rangos con evolución y potenciación: activas combinadas limitadas a rango 6; reducción combinada de esperas limitada al 60 % | `README.md` |
| Fuego del Averno y explosiones del Creeper modifican bloques (`uniqueGriefing`) | `README.md` |
| Quietud Eterna usa `NoAI` (puede afectar a otros mods) | `README.md` |
| La Amenaza se fija al aparecer la criatura (no se recalcula) | `README.md`; `logic/ThreatRules` |
| El destilado mejorado depende de un Herbolista elegible cerca | `README.md`; `alchemy/` |
| Criaturas del Eclipse: ven a 48 bloques y siguen el último punto visto un máximo de 6 s | `README.md`; `event/EclipsePursuit` |
| Una convocatoria con el Astrolabio o el Behelit solo funciona en el Mundo Normal; el calendario rotativo sigue activo hasta F1 | `event/EclipseSummoningRules`; F1 |
