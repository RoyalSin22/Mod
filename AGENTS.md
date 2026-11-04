# AGENTS.md — Instrucciones para agentes de código (Codex y similares)

Lee este archivo completo antes de tocar nada (la versión extendida, con dependencias exactas y la
carpeta local del autor, está en `docs/CONTEXTO_CODEX.md`). Resume el estado del proyecto, las reglas que el autor
exige y lo que **no** debes hacer. Responde al autor en **español**, con rigor técnico.

## 1. Proyecto

- Mod de Minecraft **Aptitudes**: Fabric, Minecraft **1.21.1**, Java **21**, paquete `dev.aptitudes`.
- Versiones fijadas (no las cambies): Loom 1.10.5, Yarn `1.21.1+build.3`, Fabric Loader 0.16.14,
  Fabric API `0.116.6+1.21.1`, Gradle 8.14.3. Mod Menu `11.0.3` solo como `modCompileOnly` (v40, opcional).
- Compilar y probar: `.\gradlew.bat build` en Windows (o `./gradlew build`). **Nunca digas que algo
  compila o que las pruebas pasan sin haberlo ejecutado**; si no pudiste ejecutarlo, dilo.
- Documentación viva: `CHANGELOG.md` (añade una entrada por versión), `README.md`, `docs/ROADMAP_APTITUDES.md` (estado real y pendientes),
  `docs/BALANCE.md`, `docs/CONFIGURATION.md` y `docs/TESTING.md`. Actualízalas cuando cambies algo.

## 2. Estado (lo que YA existe: no lo reimplementes ni lo dupliques)

| Sistema | Dónde |
|---|---|
| 10 habilidades por uso (Minería, Excavación, Tala, Agricultura, Combate, Tiro con arco, Defensa, Agilidad, Pesca, Encantamiento) con hitos 10–50; la pantalla K está diseñada para 5 filas × 2 | `data/Skill`, `logic/XpEvents`, `logic/SkillMilestones` |
| Rendimiento decreciente de XP por fuente repetida | `logic/XpDiminishing` (puro, con pruebas) |
| Bloques colocados por jugadores (sin XP al romperlos) y colocaciones confirmadas; guardado por mundo (v36) | `logic/PlacementTracker`, `logic/PlacedBlockLog`, `logic/PlacedBlocksState` |
| Atributos estilo Souls en la Hoguera, mancha de XP, frascos | `souls/`, `logic/AttributeApplier`, `logic/AttributeScaling` |
| 20 roles, árbol de 23 nodos + legendario + 10 de evolución (filas Maestro/Definitivo), sendas, evoluciones, armas de evolución | `role/`, `ability/`, `item/weapon/`; filas de evolución en `role/EvolvedTree` (datos) y `ability/EvolvedAbilities` (10 plantillas) |
| Activas equipadas (8 por rol) | `role/LoadoutCodec` (3 bits + bit de formato; lee el formato antiguo) |
| Textos de los 200 nodos evolucionados (generados) | `tools/lang/gen_evolved_lang.py` (no los edites a mano en los JSON) |
| Elementalista: 14 elementos y 10 fusiones | `role/Element`, `ability/ElementEffects`, `ability/ElementFusions` |
| Legendarias (exclusividad opcional `legendaryExclusive`) | `unique/` |
| Esbirros y torretas | `ability/Minions`, `ability/MinionBrain`, `ability/AbilityState` |
| Accesorios, élites/campeones, estructuras, grimorio (18 capítulos) | `accessory/`, `elite/`, `world/`, `item/GuideBookItem`, `client/screen/GuideScreen` |
| Efectos de lanzamiento e impacto de TODAS las habilidades (sello animado, remate por categoría, gesto) | `ability/CastFx` (`onCast`, `impact`); no dupliques estos efectos en cada habilidad |
| Combos de combate (contador, bonificación y remates por categoría) | `ability/Combos` + `logic/ComboCounter`; HUD `client/hud/ComboHud` |
| Nivel 50 y Sendas exclusivas (v46): una senda por rol, escalones S4–S6 con bifurcación (S5x/S5y), vista de sendas | `role/PathRules` (puro), `RoleTree` (`deeper(...)`, `nodeLocked`, `prerequisiteMet`, nodos nuevos **al final** de la lista), `RoleManager` (migración); cliente `screen/PathsScreen`; textos con `tools/lang/gen_path_lang.py` (no editar a mano); migración de configuración en `config/ConfigMigration` |
| Piedras angulares (v46.1): S7 de cada senda, 10 plantillas, sonidos e indicador en el HUD | `role/KeystoneRules` (puro, tabla rol/senda → plantilla), `ability/Keystones` (ganchos y estado en memoria), `network/KeystonePayload`, cliente `hud/KeystoneHud`; campos `keystone*` |
| Logros (v31): pestaña de avances «Aptitudes», 17 logros concedidos por el servidor y revisados al entrar | `achievement/Achievement` (catálogo puro), `achievement/Achievements` (`grant`, `checkState`); JSON generados por `tools/advancements/gen_advancements.py` (no editar a mano) |
| Contratos del gremio (v30): tablón, ofertas diarias deterministas, reputación | `contract/ContractRules` (puro), `ContractData` (attachment `aptitudes:contracts`), `ContractManager`, `ContractTargets`; cliente `screen/ContractBoardScreen` |
| Grupos (v29): invitaciones, reparto de XP, aliados y persistencia en Mundo Normal | `party/PartyState`, `party/PartyManager`, `party/PartyRules`; UI `client/screen/PartyScreen`, HUD `client/hud/PartyHud` |
| Fabricación en la Mesa del gremio (armaduras de rol) | `world/GuildCrafting` (tipo `aptitudes:guild_crafting`), `GuildShapedRecipe`, `GuildDeskScreenHandler`, `GuildDeskBlock`; cliente `GuildDeskScreen` |
| Configuración validada al cargar; pantalla de configuración con edición validada en el servidor (v40) | `config/AptitudesConfig.validated`, `config/ConfigSync`, `config/ConfigFieldRules`; cliente `ClientServerConfig`, `screen/ConfigScreen`, `compat/ModMenuCompat` |
| Sonidos propios (v37, generados; no editar a mano los .ogg) | `sound/ModSounds`, `assets/aptitudes/sounds.json`; ejecuta `python3 tools/audio/gen_sounds.py src/main/resources/assets/aptitudes` |
| Texturas de armas y objetos (generadas, no editar a mano los PNG de la lista) | `tools/textures/gen_items.py` (`ITEMS` y plantillas `T`); ejecuta `python3 tools/textures/gen_items.py src/main/resources/assets/aptitudes/textures/item` |
| Texturas de armaduras de rol: iconos y capas del modelo (generadas) | `tools/textures/gen_armor.py`; ejecuta `python3 tools/textures/gen_armor.py src/main/resources/assets/aptitudes/textures` |
| Iconos de Sendas: 480 PNG S1/S2/S3/S4/S5x/S5y/S6/S7 generados, no editar a mano; S1–S3 con marco de esquinas (T1 fase 1), integración en `RolesScreen` pendiente; S4–S7 integrados en la vista de sendas (v46.2) | `tools/textures/gen_node_icons.py` (lee `paths`/`deeper` de `RoleTree`; diccionario S7 de `docs/V46_DISENO_SENDAS.md` §10); ejecuta `python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node` (`--preview hoja.png`, `--check`); cliente `screen/NodeIconRenderer` (`draw`, `drawKeystone`; respaldo y caché vaciada al recargar recursos) |
| Tiempos de espera persistentes (habilidades, definitiva, firma, legendaria, Inmortalidad carmesí) | `ability/CooldownBook` + attachment `COOLDOWNS`; usa `AbilityManager.cooldowns(p)` y `gameTime(p)` |
| Filtro de objetivos: `MobEntityMixin` (setTarget) + `BrainTargetFilter` (mobs con IA Brain) | `ability/AbilityState.shouldBlockTarget` es la única regla: amplíala ahí |
| Bloques temporales de habilidades (persistentes, sin drops) | `world/TemporaryBlocks.place(...)`; nunca `setBlockState` + `schedule` para bloques temporales |
| Runas de armas v32 | `rune/RuneSocketing`, `rune/RuneRules`; hasta dos por arma, engarzadas en la Mesa del gremio |
| Runas+ v35 (Escarcha, Trueno, Guardia; quitar runas; valores configurables) | `rune/RuneSocketing` (esperas en memoria, `UseBlockCallback` para Mayús + clic en la Mesa), `rune/RuneRules` |
| Jefes propios v33 | `boss/BossManager` y `boss/BossRules`; fases guardadas en etiquetas del mob y tres núcleos que mejoran armas de evolución |
| Eventos rotativos v41 (Eclipse, Niebla de almas, Lluvia de brasas; uno al azar por fecha) | `event/WorldEventManager`, `event/WorldEventRules` (puro), `event/SoulFogRules` (criaturas de la Niebla, puro), `event/StrongEventRules` (v41.2: Eclipse y meteoritos más fuertes, puro), `event/WorldEventState`, `event/WorldEventKind`; `EclipsePayload` lleva el tipo de evento |
| Eclipse v34 (evento del mundo cada 10 días) | `event/EclipseManager`, `event/EclipseRules` (puro), `event/EclipseState`; cliente `ClientEclipse`, `hud/EclipseHud` y mixins `Eclipse*Mixin` (`require = 0`). Textura del sol: `tools/textures/gen_eclipse.py` |

Historial reciente en `main`: auditoría de seguridad y balance, v14 (grimorio, elementos y armas), v15
(Pesca y rendimiento decreciente), v15.1 (árbol con legendarias exclusivas), v15.2 (grimorio tras la
muerte y pruebas de configuración) y v16 (Defensa con `AFTER_DAMAGE`, esbirros sin conversión a
ahogado, Bombardeo orbital sin fuego amigo), v17 (anti-granjas de colocar y romper), v18 (esperas y muros persistentes) y v19 (desplazamiento de la
pantalla de roles y filtro para mobs con IA Brain), v20 (grimorio interactivo con recetas reales) y
v21 (Encantamiento y pantalla K animada) , v22 (texturas de armas y objetos clave) , v23 (árbol que crece al evolucionar) , v24 (animaciones de habilidades), v25 (texturas de armaduras) , v26 (colisión de las transformaciones) , v27 (Mesa del gremio y atributos −30 %) y v28 (combos). Después: v32 (runas), v33 (jefes), v34 (Eclipse), v35 (Runas+), v36 (anti-granjas persistente), v37 (sonidos), v38 (versión publicable 1.3.7; ver `CHANGELOG.md`), v39 (interfaz con escala grande), v40 (pantalla de configuración), v41 (eventos rotativos), v29 (grupos), v30 (contratos), v31 (logros) y v46.0 (nivel 50 y sendas exclusivas).

## 3. Exclusiones absolutas (no negociables)

**NO implementes ni propongas**: linajes, sistemas de razas, pactos, herencias raciales o de linaje,
árboles de progresión basados en ascendencia, ni mecánicas que introduzcan estos conceptos de forma
indirecta. Tampoco los sugieras como futuras expansiones. Los antiguos conceptos de «pacto» ya se
retematizaron (p. ej., «Talismán maldito»). **No cambies los ids de registro existentes**
(`pact_talisman` se mantiene por compatibilidad con mundos guardados).

## 4. Política de Git (obligatoria)

Flujo práctico con la app de Codex y worktrees: `docs/FLUJO_GIT.md`.

- Puedes inspeccionar el repositorio, crear ramas, consultar el historial y usar `git add`.
- **Commit solo con autorización explícita del autor**, una vez por commit. Antes de pedirla, informa:
  1) archivos incluidos, 2) resumen, 3) resultado REAL de compilación y pruebas, 4) mensaje propuesto
  (convencional: `feat:`, `fix:`, `docs:`, `build:`…).
- **Nunca** hagas `git push`, merge a `main` ni abras PR sin autorización explícita.
- **Nunca** uses `reset`, `clean`, `checkout` ni `restore` para descartar cambios, ni sobrescribas
  trabajo existente sin autorización. No reinicies el proyecto ni borres trabajo válido.
- Trabaja en una rama propia (p. ej., `codex/<tema>`), nunca directamente en `main`.
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

## 5. Reglas técnicas

- **Autoridad del servidor**: toda petición del cliente (payloads C2S) se valida en el servidor. El
  cliente solo muestra datos sincronizados.
- **Modificadores de atributo**: usa siempre `AttributeApplier.set` (quita y añade, con ids estables). Nunca
  añadas un modificador sin quitar antes el anterior.
- **Persistencia**: Data Attachments (`ModAttachments`, `copyOnDeath`) y `PersistentState`. Los datos se
  guardan casi siempre **por clave** (habilidades, roles, elementos y nodos), pero algunos campos son
  índices (`active_element` o las ranuras equipadas). Añadir valores al final de un enum o de una lista
  es seguro; renombrar, reordenar o borrar no.
- **Prefiere no añadir mixins.** Si hacen falta, `defaultRequire = 1` hace que un nombre de método
  equivocado tumbe el juego al arrancar: comprueba el nombre exacto en Yarn 1.21.1.
- **Aritmética**: valores que vienen del cliente o de la configuración → cálculo en `long` y límites
  (ya hubo un exploit por desbordamiento en la Hoguera).
- **Fórmulas**: ponlas en clases puras (sin Minecraft), como `SoulsMath`, `AttributeScaling` o
  `XpDiminishing`, para poder probarlas.
- **Configuración**: cada campo nuevo necesita valor por defecto seguro, límite en `validated(...)` y
  una fila en `docs/CONFIGURATION.md`. No cambies valores por defecto de balance sin preguntar.
- **Idiomas**: añade cada clave en `en_us.json` y `es_es.json`; después **copia `es_es.json`** a `es_mx`,
  `es_ar`, `es_cl`, `es_ec`, `es_uy` y `es_ve` (son idénticos). Un `%` literal en un texto con argumentos
  se escribe `%%`. Añade las claves **sin reformatear** el archivo (2 espacios, como está).
- **Interfaz**: la pantalla K (`AptitudesScreen`) mide 430×262 (se desplaza si no cabe) y tiene exactamente 10 tarjetas (5×2): una
  habilidad nueva rompe la simetría. Toda pantalla nueva debe caber en 426×240 o desplazarse. El grimorio lee las recetas del juego: no las copies en los textos.

## 6. Pruebas

- JUnit Platform con Jupiter (JUnit 5) y Vintage (JUnit 4): ambos estilos valen.
- Pruebas existentes (no las sobrescribas ni crees otras con el **mismo nombre y paquete**):
  `souls/SoulsMathTest`, `logic/AttributeScalingTest`, `logic/XpDiminishingTest`,
  `logic/PlacedBlockLogTest`, `ability/CooldownBookTest`, `world/TemporaryBlockScheduleTest`,
  `role/LoadoutCodecTest`, `role/EvolutionPointsTest`, `morph/MorphSizeRulesTest`, `logic/ComboCounterTest`,
  `role/RoleRulesTest`, `data/ProgressRulesTest` y
  `rune/RuneRulesTest`, `boss/BossRulesTest`, `event/EclipseRulesTest`, `rune/RuneBalanceTest`, `logic/PlacedBlockPersistenceTest`, `config/ConfigFieldRulesTest`, `event/WorldEventRulesTest`, `party/PartyRulesTest`, `contract/ContractRulesTest`, `achievement/AchievementCatalogTest`, `event/SoulFogRulesTest`, `event/StrongEventRulesTest`, `role/PathRulesTest`, `config/ConfigMigrationTest`, `role/KeystoneRulesTest`,
  `config/AptitudesConfigValidationTest`.
- **No escribas pruebas que llamen a métodos que no existen.** Si una prueba necesita una API nueva,
  impleméntala en el código principal en el mismo cambio y explícalo.
- Prueba lógica pura; las clases que cargan registros de Minecraft no se prueban con JUnit simple.

## 7. Pendientes conocidos (consulta antes de empezar algo grande)

- v29 (grupos), v30 (contratos) y v31 (logros) ya están hechos. Siguiente: v42–v45 de `docs/PLAN_v40_v45.md`.
- **Trabajo pendiente para Codex**: `docs/PLAN_CODEX.md` (tareas T1–T8 en orden, una rama por tarea, con su
  prompt, la autorrevisión y cómo integrarlas en `main`).
- Iconos de las Sendas (Codex, fases 1–3) integrados en `main` y usados en la vista de sendas (v46.2). El
  generador `tools/textures/gen_node_icons.py` lleva una copia de la tabla de plantillas: si cambia
  `KeystoneRules`, actualízala y regenera los iconos.

- **Plan aprobado v40–v45** (configuración, eventos rotativos, alquimia, maestría y amenaza, tutorial,
  traducciones): `docs/PLAN_v40_v45.md`. Excepción a «sin dependencias»: Mod Menu como `modCompileOnly` (v40).
- Activar `attributeScaling = SOFTCAP` por defecto: **decisión del autor** tras probar en partida.
- Pruebas en partida: v15–v19 y v29–v41 ✔ (autor, 2026-11-03). v41.1 corrige el solapamiento de `HudConfigScreen`.
- Limitaciones abiertas: tabla «Limitaciones conocidas» del `README.md` (el filtro Brain actúa cada 5 ticks).

## 8. Al terminar una tarea

Entrega un informe breve con: qué cambiaste y por qué, archivos tocados, resultado real de
`gradlew build`, riesgos o efectos de balance, y el commit propuesto (sin ejecutarlo).
