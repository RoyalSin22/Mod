# Contexto para Codex — mod «Aptitudes»

Eres un agente de código que continúa un proyecto en marcha. **Antes de tocar nada**, lee este texto y el
archivo `AGENTS.md` de la raíz del repositorio. Responde siempre en **español**, con rigor técnico,
explicando las decisiones, ventajas, limitaciones y riesgos. Si te falta información para hacer algo bien,
**pregunta antes** en lugar de suponer.

---

## 1. Qué es el proyecto

- Mod de Minecraft **Aptitudes** (sistema RPG): habilidades que suben con el uso, atributos estilo Souls
  comprados en una Hoguera, 20 roles con árbol de talentos, evoluciones, legendarias, accesorios, élites,
  estructuras y un grimorio interactivo.
- Repositorio: `RoyalSin22/aptitudes` (rama principal `main`). La copia local del autor está en
  `C:\Users\Johan\Downloads\aptitudes\aptitudes` (Windows).
- Paquete Java: `dev.aptitudes`. Id del mod: `aptitudes`.

## 2. Dependencias y versiones (FIJAS: no las cambies ni añadas otras)

| Componente | Versión |
|---|---|
| Minecraft | **1.21.1** |
| Java | **21** (`options.release = 21`) |
| Fabric Loader | **0.16.14** |
| Fabric API | **0.116.6+1.21.1** |
| Mod Menu (v40, solo `modCompileOnly`, opcional) | **11.0.3** (repositorio `maven.terraformersmc.com/releases`) |
| Mappings | **Yarn 1.21.1+build.3** (nombres Yarn, NO Mojang) |
| Fabric Loom | 1.10 (`fabric-loom` `1.10-SNAPSHOT` en `build.gradle`) |
| Gradle | **8.14.3** (wrapper incluido: usa siempre `gradlew`) |
| Pruebas | JUnit Platform: **Jupiter 5.10.2** (JUnit 5) + **Vintage** con **JUnit 4.13.2** |

- **No añadas bibliotecas** (ni Cloth Config, ni GeckoLib, ni Mod Menu, ni nada) sin pedir permiso.
- **No cambies** `gradle.properties`, la versión de Loom ni el wrapper de Gradle.
- Usa solo la API de Minecraft 1.21.1 con nombres Yarn. Ojo con métodos de versiones posteriores: por
  ejemplo, en 1.21.1 se usa `DrawContext.drawItemInSlot(...)` (no `drawStackOverlay`, que es de 1.21.2+).
  Si no estás seguro de un nombre, dilo y verifica antes de usarlo.

## 3. Cómo se compila y se prueba

```
.\gradlew.bat build        (Windows; compila, ejecuta las pruebas y genera el .jar en build\libs)
.\gradlew.bat test         (solo pruebas)
.\gradlew.bat runClient    (cliente de desarrollo)
.\gradlew.bat runServer    (servidor de desarrollo)
```

- **Nunca digas que algo compila o que las pruebas pasan si no lo has ejecutado.** Si no pudiste
  ejecutarlo, dilo claramente.
- El `.jar` válido es el de `build\libs` que **no** termina en `-sources`.

## 4. Política de Git (OBLIGATORIA)

- Puedes inspeccionar el repositorio, crear ramas, consultar el historial y usar `git add`.
- **Trabaja siempre en una rama propia** (`codex/<tema>`), nunca directamente en `main`.
- **Commit SOLO con autorización explícita del autor**, una autorización por commit. Antes de pedirla,
  informa de:
  1. archivos que se incluirán;
  2. resumen de los cambios;
  3. resultado REAL de compilación y pruebas;
  4. mensaje de commit propuesto (convencional: `feat:`, `fix:`, `docs:`, `build:`…).
- **Nunca** hagas `git push`, merge a `main` ni abras PR sin autorización explícita.
- **Nunca** uses `reset`, `clean`, `checkout` ni `restore` para descartar cambios, ni sobrescribas
  trabajo existente. No reinicies el proyecto ni borres trabajo válido.
- **Si `git commit` o `git push` fallan, no improvises**: no reintentes con otros comandos, no cambies la
  configuración de Git ni amplíes permisos. Copia el error exacto, deja los cambios preparados (`git add`)
  y avisa al autor, que hará el commit desde su terminal.
- **Trabajas en un worktree propio** (la app de Codex lo crea en `%USERPROFILE%\.codex\worktrees\…`). Al
  terminar, indica en el informe la **ruta del worktree** y la **rama**, para que el autor haga el commit
  y el push ahí antes de cerrar la tarea (si la tarea se cierra sin commit, el trabajo se pierde).
- **Una versión por tarea y por rama.** No mezcles en el mismo directorio cambios de dos versiones.
- **Formato**: respeta `.editorconfig` y `.gitattributes` (LF; Java con tabulación; idiomas JSON con
  2 espacios). **No reformatees archivos existentes**: un cambio de sangría convierte 17 claves nuevas en
  5 000 líneas de diferencia y provoca conflictos al integrar.
- GitHub Actions (`.github/workflows/build.yml`) compila y prueba cada push: una rama solo se integra en
  `main` con la marca ✔.

## 5. Exclusiones absolutas (no negociables)

**NO implementes ni propongas**: linajes, sistemas de razas, pactos, herencias raciales o de linaje,
árboles de progresión basados en ascendencia, ni mecánicas que introduzcan estos conceptos de forma
indirecta. Tampoco los sugieras como futuras expansiones. Los antiguos conceptos de «pacto» ya se
retematizaron (p. ej., «Talismán maldito»).

## 6. Lo que YA existe (no lo reimplementes ni lo dupliques)

| Sistema | Dónde |
|---|---|
| 10 habilidades por uso con hitos 10–50 (Minería, Excavación, Tala, Agricultura, Combate, Tiro con arco, Defensa, Agilidad, Pesca, Encantamiento) | `data/Skill`, `logic/XpEvents`, `logic/SkillMilestones` |
| Rendimiento decreciente de XP por fuente repetida | `logic/XpDiminishing` |
| Bloques colocados por jugadores (no dan XP al romperse) | `logic/PlacementTracker`, `logic/PlacedBlockLog` |
| Atributos Souls, Hoguera, mancha de XP, frascos | `souls/`, `logic/AttributeApplier`, `logic/AttributeScaling` |
| 20 roles, árbol de 23 nodos + legendario, sendas, evoluciones, armas de evolución | `role/`, `ability/`, `item/weapon/` |
| Filas de evolución del árbol (v23): +5 nodos con Maestro y +5 con Definitivo por rol (200 en total), bloqueadas visibles hasta evolucionar, +10/+5 puntos | `role/EvolvedTree` (datos), `ability/EvolvedAbilities` (10 plantillas: NOVA, BEAM, CONE, DASH, ZONE, VORTEX, AURA, HEAL, SHIELD, SUMMON) |
| Activas equipadas: 8 por rol, se equipan 2 | `role/LoadoutCodec` (3 bits + bit de formato; lee el formato antiguo) |
| Colisión de las transformaciones (v26): caja y altura de ojos según la forma | `morph/MorphSize` + `MorphSizeRules`; mixin `PlayerEntity#getBaseDimensions` con `require = 0` |
| Animaciones de lanzamiento de TODAS las habilidades (gesto, sello rúnico animado, remate por categoría de rol) e impactos | `ability/CastFx` (`onCast`, `impact`) — no dupliques estos efectos en cada habilidad |
| Elementalista: 14 elementos y 10 fusiones | `role/Element`, `ability/ElementEffects`, `ability/ElementFusions` |
| Legendarias (exclusividad opcional `legendaryExclusive`) | `unique/` |
| Esbirros y torretas | `ability/Minions`, `ability/MinionBrain`, `ability/AbilityState` |
| Filtro de objetivos (regla única) | `AbilityState.shouldBlockTarget` + `MobEntityMixin` + `BrainTargetFilter` |
| Tiempos de espera persistentes | `ability/CooldownBook` + `AbilityManager.cooldowns(p)` / `gameTime(p)` |
| Bloques temporales de habilidades | `world/TemporaryBlocks.place(...)` |
| Accesorios, élites/campeones, estructuras, aldeano de gremio | `accessory/`, `elite/`, `world/` |
| Grimorio interactivo (lee las recetas reales del juego) | `item/GuideBookItem`, `client/screen/GuideScreen` |
| Pantalla K (atributos y habilidades, 10 tarjetas 5×2 animadas) | `client/screen/AptitudesScreen` |
| Pantalla de roles (árbol con zoom, arrastre y desplazamiento) | `client/screen/RolesScreen` |
| Combos de combate (contador, bonificación y remates por categoría) | `ability/Combos` + `logic/ComboCounter`; HUD `client/hud/ComboHud` |
| Runas para armas de rol (v32) | `rune/RuneSocketing`, `rune/RuneRules`, NBT persistente en `ItemStack.CUSTOM_DATA` |
| Jefes propios en estructuras (v33) | `boss/BossManager`, `BossKind`, `BossRules`; etiquetas persistentes, barra de campeón existente y mejora de armas con núcleos |
| Eclipse, evento del mundo (v34) | `event/EclipseManager`, `EclipseRules` (puro, con pruebas), `EclipseState`; cliente `ClientEclipse`, `hud/EclipseHud`, mixins `Eclipse*Mixin` con `require = 0` y sin `@Shadow`; diseño en `docs/V34_ECLIPSE.md` |
| Runas+ (v35) | `rune/RuneSocketing`: 8 runas, extracción con Mayús + clic en la Mesa del gremio (`UseBlockCallback`), esperas en memoria; valores en la configuración (`rune*`) |
| Fabricación en la Mesa del gremio (armaduras de rol) | `world/GuildCrafting` (tipo `aptitudes:guild_crafting`), `GuildShapedRecipe`, `GuildDeskScreenHandler`, `GuildDeskBlock`; cliente `GuildDeskScreen` |
| Configuración validada al cargar | `config/AptitudesConfig.validated` |
| Texturas de armas y objetos (generadas) | `tools/textures/gen_items.py` |
| Texturas de armaduras: 80 iconos y 40 capas del modelo puesto (generadas) | `tools/textures/gen_armor.py` |
| Textos de los 200 nodos evolucionados (generados) | `tools/lang/gen_evolved_lang.py` |

Historial en `main`: auditoría de seguridad y balance, v14–v28 (grimorio,
elementos, armas de evolución, Pesca, rendimiento decreciente, anti-granjas, esperas y muros persistentes, IA
Brain, pantalla de roles con desplazamiento, grimorio interactivo con recetas reales, Encantamiento, pantalla
K animada, texturas de armas y objetos, árbol que crece al evolucionar, animaciones de habilidades, texturas
de armaduras, colisión de las transformaciones, Mesa del gremio, atributos −30 % y combos). **Todas las texturas de objetos del mod ya están rehechas con los generadores.**

## 7. Reglas técnicas

1. **Autoridad del servidor**: toda petición del cliente (payloads C2S) se valida en el servidor. El
   cliente solo muestra datos sincronizados. Nunca confíes en valores enviados por el cliente.
2. **Atributos**: usa siempre `AttributeApplier.set` (quita y añade, con identificadores estables). Nunca
   añadas un modificador sin quitar antes el anterior.
3. **Persistencia**: Data Attachments (`data/ModAttachments`, con `copyOnDeath`) y `PersistentState`.
   Los datos se guardan casi siempre **por clave**. Añadir valores **al final** de un enum o lista es
   seguro; **renombrar, reordenar o borrar no** (rompe mundos guardados).
4. **No cambies ids de registro existentes** (p. ej., `pact_talisman` se mantiene aunque el objeto se llame
   «Talismán maldito»).
5. **Mixins: evítalos.** Usa eventos de Fabric API siempre que se pueda. Si un mixin es imprescindible,
   `defaultRequire = 1` hace que un nombre de método equivocado tumbe el juego al arrancar: verifica el
   nombre exacto en Yarn 1.21.1 y avisa del riesgo.
6. **Aritmética**: valores que vienen del cliente o de la configuración → cálculo en `long` y con límites
   (ya hubo un exploit por desbordamiento en la Hoguera).
7. **Fórmulas en clases puras** (sin Minecraft), como `SoulsMath`, `AttributeScaling`, `XpDiminishing`,
   `CooldownBook`, `PlacedBlockLog` o `TemporaryBlockSchedule`, para poder probarlas con JUnit.
8. **Configuración**: cada campo nuevo de `AptitudesConfig` necesita un valor por defecto seguro, un
   límite en `validated(...)` y una fila en `docs/CONFIGURATION.md`. **No cambies valores por defecto de
   balance sin preguntar** (p. ej., activar `attributeScaling = SOFTCAP` es decisión del autor).
9. **Idiomas**: cada texto visible es una clave traducible. Añádela en `en_us.json` y `es_es.json` y
   después **copia `es_es.json`** tal cual a `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy` y `es_ve` (son
   idénticos). Un `%` literal en un texto con argumentos se escribe `%%`. Valida que el JSON sea correcto.
   **No reformatees** el archivo: se guarda con 2 espacios de sangría y LF (lo mismo que escribe
   `tools/lang/gen_evolved_lang.py`).
10. **Interfaz**:
    - La pantalla K mide 430×262 y tiene exactamente 10 tarjetas (5×2): una habilidad nueva rompe la simetría.
    - Comprueba que lo nuevo cabe con escala de interfaz 4 (1080p ⇒ unos 480×270 px útiles).
    - El grimorio lee las recetas del juego: no copies recetas en los textos.
11. **Archivos generados** (no los edites a mano; cambia el generador y vuelve a ejecutarlo):
    - Texturas de armas y objetos:
      `python3 tools/textures/gen_items.py src/main/resources/assets/aptitudes/textures/item`
      (plantillas `T`, colores en `ITEMS`).
    - Texturas de armaduras (iconos y capas 64×32):
      `python3 tools/textures/gen_armor.py src/main/resources/assets/aptitudes/textures`.
    - Textos de los nodos evolucionados: `python3 tools/lang/gen_evolved_lang.py` (sus números deben
      coincidir con `EvolvedTree` / `EvolvedAbilities`; si cambias uno, cambia el otro).
    - No subas cachés de Python (`__pycache__/`, `*.pyc` ya están en `.gitignore`).
12. **Rendimiento**: nada costoso cada tick por jugador. Usa intervalos (cada 5, 10 o 20 ticks),
    límites de memoria y limpieza al desconectar (`forget(...)`).
13. **Estilo del código**: Java 21, tabulaciones, comentarios y Javadoc **en español**, explicando el
    porqué de cada decisión. Sigue el estilo de los archivos vecinos.

## 8. Pruebas

- Pruebas existentes (no las sobrescribas ni crees otras con el **mismo nombre y paquete**):
  `souls/SoulsMathTest`, `logic/AttributeScalingTest`, `logic/XpDiminishingTest`,
  `logic/PlacedBlockLogTest`, `ability/CooldownBookTest`, `world/TemporaryBlockScheduleTest`,
  `role/RoleRulesTest`, `role/LoadoutCodecTest`, `role/EvolutionPointsTest`, `morph/MorphSizeRulesTest`, `logic/ComboCounterTest`, `data/ProgressRulesTest`,
  `config/AptitudesConfigValidationTest`.
- Puedes escribir pruebas en JUnit 5 o JUnit 4; ambos funcionan.
- **No escribas pruebas que llamen a métodos que no existen.** Si una prueba necesita una API nueva,
  impleméntala en el código principal en el mismo cambio y explícalo (ya pasó una vez y rompió el build).
- Prueba la lógica pura; las clases que cargan registros de Minecraft no se prueban con JUnit simple.
- Las pruebas en partida se describen en `docs/TESTING.md` (puntos 1–18) y `docs/PLAYTEST_v15-v19.md`.

## 9. Documentación que debes mantener al día

`README.md`, `AGENTS.md`, `docs/ROADMAP_APTITUDES.md` (estado real y pendientes),
`docs/BALANCE.md` (fórmulas y efectos de balance), `docs/CONFIGURATION.md` (cada opción) y
`docs/TESTING.md` (pruebas automáticas y manuales). Distingue siempre entre **implementado**,
**parcial** y **propuesto**.

## 10. Pendientes conocidos (consulta antes de empezar algo grande)

**Siguiente trabajo**: v29 (grupos) y v30 (contratos). Lee `docs/PROMPT_CODEX_v29_v30.md`.

| Pendiente | Estado |
|---|---|
| `attributeScaling = SOFTCAP` por defecto | Decisión del autor tras probar en partida |
| Pruebas en partida de la v15–v25 | `docs/TESTING.md`, puntos 6–18 |
| Filtro de mobs con IA Brain | Actúa cada 5 ticks (limitación documentada) |
| Diseño de cualquier contenido grande nuevo | Proponer primero al autor (como se hizo en `docs/V23_DISENO_ARBOL.md`) |

## 11. Qué NO hacer (resumen)

- No cambiar versiones ni añadir dependencias.
- No hacer commit, push, merge ni PR sin autorización explícita.
- No trabajar directamente en `main`.
- No descartar ni sobrescribir cambios (`reset`, `clean`, `checkout`, `restore`).
- No implementar ni proponer linajes, razas, pactos, herencias ni progresión por ascendencia.
- No renombrar ids de registro ni reordenar enums o claves guardadas.
- No reimplementar sistemas que ya existen.
- No añadir mixins si hay un evento de Fabric API que sirva.
- No cambiar valores de balance por defecto sin preguntar.
- No crear pruebas con nombres ya usados ni que llamen a métodos inexistentes.
- No editar a mano los PNG ni los textos generados (usa `tools/textures/` y `tools/lang/`).
- No duplicar efectos de lanzamiento en cada habilidad (ya los pone `CastFx`).
- No reordenar ni renombrar los ids de los nodos evolucionados (`<rol>_m1..m5`, `<rol>_u1..u5`): se guardan en el mundo.
- No afirmar resultados de compilación o pruebas que no hayas ejecutado.
- No reformatear archivos existentes (sangría, finales de línea u orden de claves).
- No cerrar una tarea con cambios sin commit: indica al autor la ruta del worktree y la rama.

## 12. Formato del informe al terminar cada tarea

1. Qué cambiaste y por qué.
2. Archivos tocados.
3. Resultado REAL de `gradlew build` / `gradlew test`.
4. Riesgos, limitaciones y efectos de balance.
5. Commit propuesto (sin ejecutarlo) y espera la autorización.
