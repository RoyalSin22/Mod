# AGENTS.md — Instrucciones para agentes de código (Codex y similares)

Lee este archivo completo antes de tocar nada (la versión extendida, con dependencias exactas y la
carpeta local del autor, está en `docs/CONTEXTO_CODEX.md`). Resume el estado del proyecto, las reglas que el autor
exige y lo que **no** debes hacer. Responde al autor en **español**, con rigor técnico.

## 1. Proyecto

- Mod de Minecraft **Aptitudes**: Fabric, Minecraft **1.21.1**, Java **21**, paquete `dev.aptitudes`.
- Versiones fijadas (no las cambies): Loom 1.10.5, Yarn `1.21.1+build.3`, Fabric Loader 0.16.14,
  Fabric API `0.116.6+1.21.1`, Gradle 8.14.3. Mod Menu `11.0.3` solo como `modCompileOnly` (v40, opcional).
  **GeckoLib `4.7.7`** (v63, aprobado por el autor): dependencia **obligatoria** para los jefes animados
  (`modImplementation` sin transitivas, repositorio `dl.cloudsmith.io`); no la subas a 4.8+ (pide Loader 0.17).
- **Versión del mod (v67):** sale del número de commits de la rama (`git rev-list --count HEAD`): 123 → 1.2.3,
  240 → 2.4.0. Sin historial de git (ZIP de GitHub o clon superficial) se usa `mod_version` de `gradle.properties`;
  actualízalo al publicar. El CI hace el checkout con `fetch-depth: 0`.
- Compilar y probar: `.\gradlew.bat build` en Windows (o `./gradlew build`). **Nunca digas que algo
  compila o que las pruebas pasan sin haberlo ejecutado**; si no pudiste ejecutarlo, dilo.
- Documentación viva: `CHANGELOG.md` (añade una entrada por versión), `README.md`, `docs/ROADMAP_APTITUDES.md` (estado real y pendientes),
  `docs/BALANCE.md`, `docs/CONFIGURATION.md` y `docs/TESTING.md`. Actualízalas cuando cambies algo.

## 2. Estado (lo que YA existe: no lo reimplementes ni lo dupliques)

| Sistema | Dónde |
|---|---|
| 10 habilidades por uso (Minería, Excavación, Tala, Agricultura, Combate, Tiro con arco, Defensa, Agilidad, Pesca, Encantamiento) con hitos 10–50; la pantalla K está diseñada para 5 filas × 2 | `data/Skill`, `logic/XpEvents`, `logic/SkillMilestones` |
| Rendimiento decreciente de XP por fuente repetida | `logic/XpDiminishing` (puro, con pruebas) |
| Maestría (v43, T6 fase 1): XP sobrante al máximo, +0,5 % por punto con tope inicial +10 %, pasivas de habilidades y eficiencia energética de roles | `logic/MasteryRules` (puro), `MasteryManager`, `data/MasteryProgress` dentro de `PlayerProgress.mastery` (`copyOnDeath`); ganchos `ProgressManager`, `RoleManager`, `PartyManager`; S2C `MasteryPayload`, `sync/ClientMastery`; K conserva 10 tarjetas 5×2 y 430×262 |
| Amenaza (v43, T6 fase 2): anillos 0–V desde la aparición del mundo; vida/daño, botín y probabilidad de élite acotados; excluye jefes, invocaciones, eventos y nombres previos | `logic/ThreatRules` (puro), `ThreatManager`, `data/ThreatData` (attachment de criatura `aptitudes:threat`, instantánea y cobro); gancho antes de la tirada natural de `elite/Elites`; S2C `ThreatPayload`, cliente `hud/ThreatHud`; campos `threat*` |
| Bloques colocados por jugadores (sin XP al romperlos) y colocaciones confirmadas; guardado por mundo (v36) | `logic/PlacementTracker`, `logic/PlacedBlockLog`, `logic/PlacedBlocksState` |
| Atributos estilo Souls en la Hoguera, mancha de XP, frascos | `souls/`, `logic/AttributeApplier`, `logic/AttributeScaling` |
| 20 roles, árbol de 23 nodos + legendario + 10 de evolución (filas Maestro/Definitivo), sendas, evoluciones, armas de evolución | `role/`, `ability/`, `item/weapon/`; filas de evolución en `role/EvolvedTree` (datos) y `ability/EvolvedAbilities` (10 plantillas) |
| Activas equipadas (2 ranuras por rol, hasta 16 activas) | `role/LoadoutCodec` (4 bits por ranura + marca de formato desde v67; lee los formatos de 2 y 3 bits) |
| Textos de los 200 nodos evolucionados (generados) | `tools/lang/gen_evolved_lang.py` (no los edites a mano en los JSON) |
| Traducciones iniciales (v45, T8 fases 1–3): pt_br/fr_fr/de_de, las 3512 claves traducidas por idioma, mismo orden y marcadores que en_us; revisión visual y nativa pendiente | `tools/lang/ui_catalog.py`, `guide_catalog.py`, `node_catalog.py`; `gen_interface_lang.py`, `gen_guide_lang.py`, `gen_nodes_lang.py` y generadores originales `gen_evolved_lang.py`/`gen_path_lang.py`, todos con `--check`; `generator_cli.py` conserva formato y variantes; `check_lang.py` y 35 pruebas Python en CI verifican claves, marcadores, números, cobertura y regeneración |
| Elementalista: 14 elementos y 10 fusiones | `role/Element`, `ability/ElementEffects`, `ability/ElementFusions` |
| Legendarias (exclusividad opcional `legendaryExclusive`) | `unique/` |
| Esbirros y torretas | `ability/Minions`, `ability/MinionBrain`, `ability/AbilityState` |
| Accesorios, élites/campeones, estructuras, grimorio (23 capítulos; sendas, piedras angulares, logros y alquimia) | `accessory/`, `elite/`, `world/`, `item/GuideBookItem`, `client/screen/GuideScreen` |
| Grimorio v61 (K3) y v67 (G1–G4): bestiario por jugador **solo de criaturas del mod** (13 entradas) con cartas de pergamino (modelo, estadísticas, consejos, botín y lore con revelado progresivo por muertes), progreso, buscador global, marcadores, sendas por rol, enlaces desde tutorial y tooltips (tecla U), cabeceras generadas; piezas de pergamino provisionales en `client/screen/GuideUi` (arte pendiente: `docs/PROMPT_ARTE_GRIMORIO.md`), maqueta en `logic/GuideCardLayout`, diseño en `docs/V67_GRIMORIO_DISENO.md` | `guide/BestiaryRules` y `GuideRules` (puros, JUnit), `guide/GrimoireManager`, attachment `BESTIARY` (`copyOnDeath`), `GrimoirePayload` (S2C) y `GrimoireRequestPayload` (C2S, limitada); cliente `ClientGrimoire`, `GuideLookup` (access widener de `HandledScreen.focusedSlot`), `ClientConfig.guideBookmarks`; cabeceras con `tools/textures/gen_guide_headers.py` (no editar los PNG) |
| Armas 3D en la mano (v62, K4 A1): icono 2D en la interfaz y modelo vóxel en mano, suelo y marcos | `tools/models/gen_weapon_models.py` (lee las plantillas de `gen_items.py`; no editar los `*_3d.json`), cliente `client/model/HandheldModels` y `HandheldModel` (API de carga de modelos y de renderizado de Fabric) |
| Tooltips de objetos (v62, K4 A6): sello de rol, daño por segundo, cifras de disparo y rareza | `item/RoleGlyphs` (fuente `aptitudes:role_icons` de `tools/textures/gen_role_glyphs.py`), `logic/ItemStatRules` (puro, JUnit), cliente `ItemTooltips`; texturas con detalle en `gen_items.py` (`SHINY`, `GRAINED`) |
| Templos sellados y Vasos de vida (v64, K6): puzle por anillo de Amenaza, guardián, llave del templo, Relicario y +1 corazón por vaso | `temple/TempleRules` y `VesselRules` (puros, JUnit), `Temples` (registro, guardián, vida extra), `TempleSealBlockEntity`, `ReliquaryBlockEntity`, `LifeVesselItem` + `LifeVesselState`, attachment `LIFE_VESSELS` (`copyOnDeath`), `world/TempleFeature`; texturas con `tools/textures/gen_temple_blocks.py` y `gen_items.py`; campo `lifeVesselMax` |
| Jefes animados con GeckoLib (v63): base con fases, ataques avisados y animaciones disparadas; Coloso Sin Rostro, Guardián Desmontado y Autómata Antiguo jugables | `boss/AnimatedBossEntity`, `BossMoveRules` y `PieceBossRules` (puros, JUnit), `FacelessColossusEntity`, `GuardianEntity`, `AutomatonEntity` y `AutomatonOrbEntity`, `BossEntities`; cliente `client/render/BossRenderers`; recursos de prueba con `tools/models/gen_boss_models.py` (`--check`); diseño y contrato de modelos en `docs/V63_JEFES.md` |
| Efectos de lanzamiento e impacto de TODAS las habilidades (sello animado, remate por categoría, gesto) | `ability/CastFx` (`onCast`, `impact`); no dupliques estos efectos en cada habilidad |
| Remates 3D al lanzar habilidades (v54): cinco categorías, alcance de 40 bloques, ajuste solo visual | `ability/CastFx`, `logic/CastAnimationRules` (puro), `network/CastAnimationPayload`, cliente `fx/CastAnimations` y `ClientConfig.castAnimations` |
| Combos de combate (contador, bonificación y remates por categoría) | `ability/Combos` + `logic/ComboCounter`; HUD `client/hud/ComboHud` |
| Nivel 50 y Sendas exclusivas (v46): una senda por rol, escalones S4–S6 con bifurcación (S5x/S5y), vista de sendas | `role/PathRules` (puro), `RoleTree` (`deeper(...)`, `nodeLocked`, `prerequisiteMet`, nodos nuevos **al final** de la lista), `RoleManager` (migración); cliente: árbol unificado de `screen/RolesScreen`; textos con `tools/lang/gen_path_lang.py` (no editar a mano); migración de configuración en `config/ConfigMigration` |
| Piedras angulares (v46.1): S7 de cada senda, 10 plantillas, sonidos e indicador en el HUD | `role/KeystoneRules` (puro, tabla rol/senda → plantilla), `ability/Keystones` (ganchos y estado en memoria), `network/KeystonePayload`, cliente `hud/KeystoneHud`; campos `keystone*` |
| Logros (v31): pestaña de avances «Aptitudes», 17 logros concedidos por el servidor y revisados al entrar | `achievement/Achievement` (catálogo puro), `achievement/Achievements` (`grant`, `checkState`); JSON generados por `tools/advancements/gen_advancements.py` (no editar a mano) |
| Contratos del gremio (v30): tablón, ofertas diarias deterministas, reputación | `contract/ContractRules` (puro), `ContractData` (attachment `aptitudes:contracts`), `ContractManager`, `ContractTargets`; cliente `screen/ContractBoardScreen` |
| Grupos (v29): invitaciones, reparto de XP, aliados y persistencia en Mundo Normal | `party/PartyState`, `party/PartyManager`, `party/PartyRules`; UI `client/screen/PartyScreen`, HUD `client/hud/PartyHud` |
| Tutorial guiado (v44): seis pasos por jugador, cobros persistentes, HUD desactivable y `/aptitudes tutorial status\|skip`; omisión irreversible | `tutorial/TutorialRules`, `TutorialView` (puros), `TutorialData` (attachment `aptitudes:tutorial`, `copyOnDeath`), `TutorialManager`, `command/TutorialCommand`, S2C `TutorialPayload`; cliente `ClientTutorial`, `hud/TutorialHud`, `ClientConfig.showTutorial`; ganchos tras acciones válidas en Hoguera, roles, activas, combos y Mesa del gremio |
| Alquimia (v42): cuatro pociones, pétalos, destilado rápido y duración +50 % para Herbolista, XP decreciente persistente y capítulo del grimorio con recetas reales | `alchemy/AlchemyRules`, `BrewingRules` (puros), `AlchemyEffects`, `AlchemyPotions`, `AlchemyBrewing`, `AlchemyXpData` (`copyOnDeath`); `aptitudes.accesswidener` expone solo `BrewingStandBlockEntity.propertyDelegate`; iconos con `tools/textures/gen_status_effects.py`, pétalos con `gen_items.py`; cliente `AlchemyTooltips` |
| Fabricación en la Mesa del gremio (armaduras de rol) | `world/GuildCrafting` (tipo `aptitudes:guild_crafting`), `GuildShapedRecipe`, `GuildDeskScreenHandler`, `GuildDeskBlock`; cliente `GuildDeskScreen` |
| Reforja y Templado (v48): Reserva e Impulso para armas de evolución, dos rangos y confirmación de un solo uso | `forge/ForgeRules` (puro), `ForgeWeapons` (`CUSTOM_DATA.aptitudes_forge`), `ForgeScreenHandler`, `ForgeActionPayload`, `ForgePreviewPayload`; cliente `screen/ForgeScreen` |
| Configuración validada al cargar; pantalla de configuración con edición validada en el servidor (v40) y tooltips con descripción y valor por defecto de cada opción (v67; cada opción editable nueva necesita su texto `config.aptitudes.field.<campo>` en en_us y es_es) | `config/AptitudesConfig.validated`, `config/ConfigSync`, `config/ConfigFieldRules`, `config/ConfigTooltipRules`; cliente `ClientServerConfig`, `screen/ConfigScreen`, `compat/ModMenuCompat` |
| Sonidos propios (v37, generados; no editar a mano los .ogg) | `sound/ModSounds`, `assets/aptitudes/sounds.json`; ejecuta `python3 tools/audio/gen_sounds.py src/main/resources/assets/aptitudes` |
| Texturas de armas y objetos (generadas, no editar a mano los PNG de la lista) | `tools/textures/gen_items.py` (`ITEMS` y plantillas `T`); ejecuta `python3 tools/textures/gen_items.py src/main/resources/assets/aptitudes/textures/item` |
| Mesa del gremio (v49): cuadrícula exclusiva para la fabricación de Aptitudes salvo la propia Mesa, con recetas con forma y sin forma | `world/GuildCrafting` (tipo `aptitudes:guild_crafting`), `GuildShapedRecipe`, `GuildShapelessRecipe`, `GuildDeskScreenHandler`; cliente `GuildDeskScreen` |
| Texturas de la Mesa del gremio y la Hoguera (generadas, incluida la llama animada) | `python3 tools/textures/gen_workstations.py src/main/resources/assets/aptitudes/textures/block` (admite `--check`) |
| Texturas de armaduras de rol: iconos y capas del modelo (generadas) | `tools/textures/gen_armor.py`; ejecuta `python3 tools/textures/gen_armor.py src/main/resources/assets/aptitudes/textures` |
| HUD e iconos del árbol (v50): atlas del árbol y HUD, sellos de rol, filas separadas y barra adaptable | `tools/textures/gen_tree_atlas.py` (admite `--check`), `NodeIconRenderer`, `hud/AbilityHud`, `logic/AbilityHudLayout` (puro), `screen/HudConfigScreen` |
| Editor del HUD (v59, K1 fase 2): presets, modo minimalista y animaciones locales | `screen/HudConfigScreen`, `ClientConfig`, `logic/HudPreset`, `logic/HudMotion` (puro, JUnit), cliente `hud/` |
| Atlas de talentos (v53): cuatro entradas, ficha adaptable, 20 paletas y grabados de rol | `screen/RolesScreen`, `screen/RoleAtlasTheme`; los grabados se añaden al final del atlas generado |
| Marcos de casilla y esfera de maná del HUD (v66): 25 marcos (5 categorías × 5 estados) a 20×20 px en la barra de habilidades y esfera de maná de 28×28 px dibujada a 14×14 con el maná actual dentro (vacía/llena recortada por nivel); el antiguo panel «Souls» ya no muestra la vida: aguante y recursos de rol van en un bloque sobre la barra de hambre («Barras de estado») | Arte fuente en `tools/textures/source/hud/casillas/` (no edites los PNG de salida); `tools/textures/gen_hud_sprites.py` genera `textures/gui/hud/slot/` y `hud/orb/` (admite `--check`); cliente `hud/AbilityHud` (`drawFrame`, `renderManaOrb`) y `NodeIconRenderer` (`drawSlotFrame`, `drawManaOrb`; respaldo al dibujo anterior) |
| Piezas del kit «Berserker» del árbol (v66): marcos de nodo por categoría y estado, conectores (vertical, bifurcación y unión volteada), cabeceras y paneles estirados desde el centro con `anchoredTiles` y periodo de ladrillo. La pantalla propia (`TreeScreen`, `PathsScreen`, `SpriteButton`) se borró en v67: las piezas las usa el árbol unificado | `client/screen/TreeUi` (dibujo de piezas a tamaño real, 9-slice con tramos repetidos), `logic/TreeScreenLayout` (puro, JUnit); arte fuente en `tools/textures/source/ui/` (no edites los PNG de salida); `tools/textures/gen_tree_ui.py` genera `textures/gui/tree_ui/` (admite `--check`) |
| Pantalla K con la dirección de arte (v66): kit de 34 piezas en Atributos y Habilidades; 430×262, 10 tarjetas 5×2 y las pestañas de siempre | `client/screen/AptitudesScreen` (respaldo al dibujo anterior), `KUi` y `KButton` (piezas a tamaño real, 9-slice y tramos repetidos), `logic/KScreenLayout` (puro, JUnit); arte fuente en `tools/textures/source/ui/k/` (no edites los PNG de salida); `tools/textures/gen_k_ui.py` genera `textures/gui/k_ui/` (admite `--check`); encargo en `docs/PROMPT_ARTE_PANTALLA_K.md` |
| Pantalla de roles con la dirección de arte (v66, kit «fantasía oscura v2»): botones largos, pestañas, filas de roles, fichas de categoría con emblema (48×32 a doble densidad en 24×16, estados normal/hover/activa; excepción a la rejilla de 2×2), filete con rombo y franja de título | `client/screen/RolesScreen` y `RoleAtlasTheme.Tab` (respaldo al dibujo anterior), `RolesUi` y `RolesButton` (9-slice de 6 px con `KScreenLayout.tiles`); arte fuente en `tools/textures/source/ui/roles/` (no edites los PNG de salida); `tools/textures/gen_roles_ui.py` genera `textures/gui/roles_ui/` (admite `--check`) |
| Árbol de habilidades unificado (v67, F1): Talentos, Sendas, Maestro, Definitivo y Maestría en un solo lienzo de J; pestañas como accesos directos, minimapa, rueda para desplazar y Ctrl + rueda para el zoom; confirmación de senda y rama; «Reiniciar» en el pie. Diseño y fases en `docs/V67_ARBOL_UNIFICADO.md` | `client/screen/RolesScreen` (`buildNodes`, `renderSections`, `renderPaths`, `learn`, `respec`), `logic/UnifiedTreeLayout` (puro, JUnit) |
| Habilidades nuevas por rol (v67: F2 Bersérker; F3 Mago, Asesino, Herbolista e Ingeniero; F4 Lancero, Espadachín, Paladín y Nigromante; F5 Druida, Mercader táctico, Monje y Chamán; F6 Cambiaformas, Mago pistolero, Invocador de armas y Cazador; F7 Bardo, Brujo abisal y Domador): 10 nodos N1–N10 por rol en Talentos, Veterano y Ascenso (los 20 roles ya los tienen) | Datos en `role/RoleExtras` (al final de la lista del rol; activas en los índices 8+), `RoleTree.extraSection`; cifras en `ability/<Rol>Rules` (puros, JUnit) y comportamiento en `ability/<Rol>Kit`; **un único punto de enganche, `ability/ExtraKits`**: un rol nuevo añade su kit allí y no toca más ganchos (`RoleMechanics`, `RolePassives`, `AbilityManager`, `RoleEvents` y desconexión ya llaman a `ExtraKits`); ayudas comunes en `KitSupport`; recortar esperas con `AbilityManager.reduceCooldown`; equipamiento con `LoadoutCodec` de 4 bits; opción `extraSkillDamageScale`; diseños en `docs/V67_ARBOL_UNIFICADO.md` §8 y `docs/V67_F3_DISENO.md` `docs/V67_F4_DISENO.md`, `docs/V67_F5_DISENO.md` y `docs/V67_F6_DISENO.md` y `docs/V67_F7_DISENO.md`; encargos de iconos en `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`, `docs/PROMPT_ARTE_ICONOS_F3.md`, `docs/PROMPT_ARTE_ICONOS_F4.md`, `docs/PROMPT_ARTE_ICONOS_F5.md`, `docs/PROMPT_ARTE_ICONOS_F6.md` y `docs/PROMPT_ARTE_ICONOS_F7.md` |
| Descripciones de habilidades en estilo llano (v67): qué hace, hablándole al jugador, y al final «Rango 1 / 2 / 3: …»; sin coletillas | Nodos base: `tools/lang/base_desc.py` (fuente única en los 5 idiomas; `--apply` escribe en/es y variantes, `node_catalog` toma pt/fr/de; `--check` en CI). Sendas y piedras angulares: `gen_path_lang.py` (`SENTENCE`, `RANKS`). Maestro/Definitivo: `gen_evolved_lang.py`. No edites estas descripciones a mano en los JSON |
| Iconos de rol (v66): 20 iconos de la dirección de arte (32×32 y 16×16), usados por `NodeIconRenderer.drawRole` para la insignia principal | Arte fuente en `tools/textures/source/roles/` (PNG 64×64 con rejilla 2×2; no los edites aquí); `tools/textures/gen_role_icons.py` genera `textures/gui/role/` y `role/small/` (no editar a mano; admite `--check`) |
| Iconos de Sendas: 480 PNG S1/S2/S3/S4/S5x/S5y/S6/S7 generados, no editar a mano; S1–S3 con marco de esquinas e integrados en la cuadrícula de `RolesScreen` sin cambiar la distribución (T1); S4–S7 integrados en el árbol unificado (v67) | `tools/textures/gen_node_icons.py` (lee `paths`/`deeper` de `RoleTree`; diccionario S7 de `docs/V46_DISENO_SENDAS.md` §10); ejecuta `python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node` (`--preview hoja.png`, `--check`); cliente `screen/NodeIconRenderer` (`draw`, `drawKeystone`; respaldo y caché vaciada al recargar recursos) |
| Barra de jefes (v59, K1 fase 1): panel inferior común para jefes y campeones cercanos, fase, vida y recuento | `elite/Elites` (audiencia), `network/BossHudPayload` (S2C), `logic/BossHudLayout` (puro), cliente `hud/BossHud` |
| Tiempos de espera persistentes (habilidades, definitiva, firma, legendaria, Inmortalidad carmesí) | `ability/CooldownBook` + attachment `COOLDOWNS`; usa `AbilityManager.cooldowns(p)` y `gameTime(p)` |
| Filtro de objetivos: `MobEntityMixin` (setTarget) + `BrainTargetFilter` (mobs con IA Brain) | `ability/AbilityState.shouldBlockTarget` es la única regla: amplíala ahí |
| Mejora de frascos (v60, K2 fase 1): Estus y Éter, confirmación de un solo uso y rango guardado en el objeto | `souls/FlaskRules` (puro, JUnit), `FlaskUpgrades`, `FlaskScreenHandler`, `item/SoulFlaskItem`, cliente `screen/FlaskScreen` |
| Bloques temporales de habilidades (persistentes, sin drops) | `world/TemporaryBlocks.place(...)`; nunca `setBlockState` + `schedule` para bloques temporales |
| Runas de armas v32 | `rune/RuneSocketing`, `rune/RuneRules`; hasta dos por arma, engarzadas en la Mesa del gremio |
| Runas+ v35 (Escarcha, Trueno, Guardia; quitar runas; valores configurables) | `rune/RuneSocketing` (esperas en memoria, `UseBlockCallback` para Mayús + clic en la Mesa), `rune/RuneRules` |
| Arenas de jefe (v60, K2 fase 2): niebla temporal anclada a la estructura y tema para participantes | `world/BossArenas`, `BossArenaLayout` (puro, JUnit), `TemporaryBlocks`, `tools/audio/gen_sounds.py`, `tools/textures/gen_arena_fog.py` |
| Jefes propios (v33 y v56): fases persistentes, núcleos y ataques finales con círculo, anillo y cono | `boss/BossManager`, `boss/BossRules` (JUnit), `world/StructureBuilder`. La generación usa la región del chunk y el registro de jefes se hace al cargar las entidades |
| Eventos rotativos v41 (Eclipse, Niebla de almas, Lluvia de brasas; uno al azar por fecha) | `event/WorldEventManager`, `event/WorldEventRules` (puro), `event/SoulFogRules` (criaturas de la Niebla, puro), `event/StrongEventRules` (v41.2: Eclipse y meteoritos más fuertes, puro), `event/WorldEventState`, `event/WorldEventKind`; `EclipsePayload` lleva el tipo de evento |
| Eclipse v34 (evento del mundo cada 10 días) | `event/EclipseManager`, `event/EclipseRules` (puro), `event/EclipseState`; cliente `ClientEclipse`, `hud/EclipseHud` y mixins `Eclipse*Mixin` (`require = 0`). Textura del sol: `tools/textures/gen_eclipse.py` |
| Persecución del Eclipse (v52): adquisición escalonada de objetivos y búsqueda del último punto visible | `event/EclipsePursuit`, `EclipsePursuitRules` (puro, JUnit), `EclipseManager`; validación con `AbilityState.shouldBlockTarget` y memoria de sesión |
| Asalto del Eclipse v47 (fase 1): oleadas 4/8, garantías de élite/campeón, equipo completo visible, presupuestos locales/globales y encuentros próximos del Heraldo | `event/EclipseAssaultRules` (puro, JUnit), `EclipseManager`; opciones `eclipsePenumbraWaveSize`, `eclipseTotalityWaveSize`, `eclipseZoneMobCap`, `eclipseWorldMobCap`, `eclipseMinElitesPerWave`, `eclipseMinChampionsTotality`, `eclipseGuaranteedEliteArmor`; diseño `docs/V47_ECLIPSE.md`; mantiene guardado y multiplicadores previos |

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
- **Commit y push a la rama de trabajo `claude/v63d-correcciones` están permitidos sin pedir autorización
  por cada commit** (decisión del autor). Antes de commitear, compila y ejecuta las pruebas; el mensaje
  debe ser convencional (`feat:`, `fix:`, `docs:`, `build:`…) e incluir solo el trailer de sesión.
- **Nunca** hagas `git push` a otra rama, merge a `main`, ni abras PR sin autorización explícita del autor
  para esa acción concreta.
- **Nunca** uses `reset`, `clean`, `checkout` ni `restore` para descartar cambios, ni sobrescribas
  trabajo existente sin autorización. No reinicies el proyecto ni borres trabajo válido.
- Trabaja en una rama propia (p. ej., `codex/<tema>`), nunca directamente en `main`.
- **Si `git commit` o `git push` fallan, no improvises**: no reintentes con otros comandos, no cambies la
  configuración de Git ni amplíes permisos. Copia el error exacto, deja los cambios preparados (`git add`)
  y avisa al autor, que hará el commit desde su terminal.
- **Atribución**: el trailer de sesión (`Claude-Session: …`) es lo único que se añade a los commits. No se
  incluye ningún nombre o identificador de modelo en commits, PRs, código ni documentación del repositorio.
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
- **Puestos de aldeano**: para que un bloque sea puesto de trabajo de un aldeano vanilla, añade su tipo de punto
  de interés a la etiqueta `minecraft:acquirable_job_site` (archivo `data/minecraft/tags/point_of_interest_type/`
  con `"replace": false`). No hace falta mixin (el aldeano del gremio lo usa).
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
- Pruebas existentes (no las sobrescribas ni crees otras con el **mismo nombre y paquete**; lista revisada en v67 (tooltips), 118 clases): `ability/AssassinRulesTest`, `ability/BardRulesTest`, `ability/BerserkerRulesTest`, `ability/CooldownBookReduceTest`, `ability/CooldownBookTest`, `ability/DruidRulesTest`, `ability/ElementalistRulesTest`, `ability/EngineerRulesTest`, `ability/GunslingerRulesTest`, `ability/HerbalistRulesTest`, `ability/HunterRulesTest`, `ability/LancerRulesTest`, `ability/MageRulesTest`, `ability/MerchantRulesTest`, `ability/MonkRulesTest`, `ability/NecromancerRulesTest`, `ability/PaladinRulesTest`, `ability/ShapeshifterRulesTest`, `ability/SwordsmanRulesTest`, `ability/TamerRulesTest`, `ability/TauntRulesTest`, `ability/TranceRulesTest`, `ability/WarlockRulesTest`, `ability/WeaponSummonerRulesTest`, `achievement/AchievementCatalogTest`, `alchemy/AlchemyRulesTest`, `alchemy/AlchemyXpPersistenceTest`, `alchemy/BrewingRulesTest`, `boss/AttackTagsTest`, `boss/BossMoveRulesTest`, `boss/BossRulesTest`, `boss/GuardRulesTest`, `boss/MoveTagsTest`, `boss/PieceBossRulesTest`, `boss/PostureRulesTest`, `boss/SummonRulesTest`, `config/AlchemyConfigValidationTest`, `config/AptitudesConfigValidationTest`, `config/ConfigFieldRulesTest`, `config/ConfigTooltipRulesTest`, `config/ConfigMigrationTest`, `config/ConfigUpperBoundsTest`, `config/EclipseAssaultConfigTest`, `config/MasteryConfigValidationTest`, `config/PartyConfigValidationTest`, `config/ThreatConfigValidationTest`, `config/V65ConfigValidationTest`, `contract/ContractAdvanceRulesTest`, `contract/ContractDataCodecTest`, `contract/ContractRulesTest`, `contract/ContractTierRewardsTest`, `data/MasteryProgressCodecTest`, `data/ProgressRulesTest`, `data/ThreatDataCodecTest`, `elite/GlyphRulesTest`, `event/EclipseAssaultRulesTest`, `event/EclipsePursuitRulesTest`, `event/EclipseRulesTest`, `event/EmberMobRulesTest`, `event/FogErranteRulesTest`, `event/ExpeditionProgressRulesTest`, `event/ExpeditionRulesTest`, `event/SoulFogRulesTest`, `event/StrongEventRulesTest`, `event/WorldEventRulesTest`, `forge/ForgeRulesTest`, `forge/ForgeWeaponsNbtTest`, `guide/BestiaryRevealRulesTest`, `guide/BestiaryRulesTest`, `guide/GuideRulesTest`, `logic/AbilityHudLayoutTest`, `logic/AttributeScalingTest`, `logic/BossHudLayoutTest`, `logic/CastAnimationRulesTest`, `logic/ComboCounterTest`, `logic/GuideCardLayoutTest`, `logic/HudMotionTest`, `logic/ItemStatRulesTest`, `logic/MasteryRulesTest`, `logic/PlacedBlockLogTest`, `logic/PlacedBlockPersistenceTest`, `logic/KScreenLayoutTest`, `logic/ThreatRulesTest`, `logic/TreeScreenLayoutTest`, `logic/UnifiedTreeLayoutTest`, `logic/XpDiminishingTest`, `mob/FogMobRulesTest`, `mob/OsarioRulesTest`, `mob/SpectralRendRulesTest`, `morph/MorphFlightRulesTest`, `morph/MorphSizeRulesTest`, `network/RequestLimiterTest`, `party/PartyRulesTest`, `party/PartyStateNbtTest`, `role/ElementRulesTest`, `role/EvolutionPointsTest`, `role/KeystoneRulesTest`, `role/LoadoutCodecTest`, `role/LoadoutCodecV4Test`, `role/PathRulesTest`, `role/RoleExtrasTest`, `role/RoleMigrationRulesTest`, `role/RespecRulesTest`, `role/RoleRulesTest`, `rune/RuneBalanceTest`, `rune/RuneRulesTest`, `souls/FlaskRulesTest`, `souls/SoulStainRulesTest`, `souls/SoulsMathTest`, `temple/TempleRecurrenceRulesTest`, `temple/TempleRulesTest`, `temple/VesselRulesTest`, `tutorial/TutorialDataCodecTest`, `tutorial/TutorialRulesTest`, `tutorial/TutorialViewTest`, `world/BossArenaLayoutTest`, `world/GuildRecipeResourcesTest`, `world/TemporaryBlockScheduleTest`.
- **No escribas pruebas que llamen a métodos que no existen.** Si una prueba necesita una API nueva,
  impleméntala en el código principal en el mismo cambio y explícalo.
- Prueba lógica pura; las clases que cargan registros de Minecraft no se prueban con JUnit simple.

## 7. Pendientes conocidos (consulta antes de empezar algo grande)

- v29 (grupos), v30 (contratos) y v31 (logros) ya están hechos. Siguiente: v42–v45 de `docs/PLAN_v40_v45.md`.
- **Trabajo actual**: Codex sigue `docs/PLAN_CODEX_2.md` (K1–K6) y Claude `docs/PLAN_CLAUDE.md` (C1–C7); no
  toquéis a la vez los mismos archivos (ver cada plan). Plan anterior, ya completado:
  `docs/PLAN_CODEX.md` (tareas T1–T8 en orden, una rama por tarea, con su
  prompt, la autorrevisión y cómo integrarlas en `main`).
- Iconos de las Sendas (Codex, fases 1–3) integrados en `main` y usados en la vista de sendas (v46.2). El
  generador `tools/textures/gen_node_icons.py` lleva una copia de la tabla de plantillas: si cambia
  `KeystoneRules`, actualízala y regenera los iconos.

- **Plan aprobado v40–v45** (configuración, eventos rotativos, alquimia, maestría y amenaza, tutorial,
  traducciones): `docs/PLAN_v40_v45.md`. Excepción a «sin dependencias»: Mod Menu como `modCompileOnly` (v40).
- Activar `attributeScaling = SOFTCAP` por defecto: **decisión del autor** tras probar en partida.
- Revisión de mixins (v67): `docs/REVISION_MIXINS.md`.
- Pruebas en partida: v15–v19 y v29–v41 ✔ (autor, 2026-11-03); T4–T6 ✔ confirmadas por el autor el 2026-11-06. v41.1 corrige el solapamiento de `HudConfigScreen`.
- Limitaciones abiertas: tabla «Limitaciones conocidas» del `README.md` (el filtro Brain actúa cada 5 ticks).

## 8. Al terminar una tarea

Entrega un informe breve con: qué cambiaste y por qué, archivos tocados, resultado real de
`gradlew build`, riesgos o efectos de balance, y el commit realizado (hash y mensaje).

## 9. Herramientas del proyecto (plugins, skills y conectores)

Lista revisada el 2026-11-09. Úsalas cuando encajen; el resto de herramientas instaladas (Vercel, Expo, Figma,
Unity, Zapier, Supabase, Cloudflare, datos, diseño) **no aplican a este mod**: no las uses sin pedirlo.

**Disponibles y recomendadas**

| Herramienta | Para qué en este mod | Cuándo |
|---|---|---|
| `minecraft-modding` (skill) | Diagnóstico de Loom, mixins, GeckoLib, registro de bloques/objetos y pantallas en negro | Errores de arranque, render o registro |
| `headlessmc` (skill) | Lanzar cliente o servidor sin pantalla y pruebas de humo en CI (`MC-Runtime-Test`) | Sustituye las capturas manuales con Xvfb; evaluar antes de adoptar |
| `java-backend-skills:java-unit-testing` | Pruebas JUnit de las reglas puras (estilo y fixtures) | Cualquier regla nueva en `logic/`, `role/`, `boss/`… |
| `java-backend-skills:java-concurrency` | Ciclo de vida de jefes, temporizadores y colas por servidor | Tareas de servidor con estado en memoria |
| `superpowers:test-driven-development` | Escribir la prueba de la regla antes del código | Reglas puras nuevas |
| `superpowers:verification-before-completion` | Exigir la salida real de `./gradlew build` antes de decir que algo funciona | Siempre, antes de informar |
| `superpowers:using-git-worktrees` / `finishing-a-development-branch` | Worktrees por tarea, si se usan | Al empezar una tarea |
| `pr-review-toolkit:silent-failure-hunter`, `type-design-analyzer` | Revisión de catch vacíos y de tipos nuevos | Antes de integrar en `main` |
| `coderabbit:code-review` | Revisión previa al commit (requiere la CLI de CodeRabbit) | Si la CLI está instalada |
| `sonarqube:sonar-analyze` | Calidad y seguridad (requiere un proyecto en SonarQube) | Cuando el autor lo active |
| Context7 (`mcp__Context7__resolve-library-id`, `query-docs`, vía ToolSearch) | Documentación actual de Fabric, Yarn, GeckoLib y Loom | **Antes de escribir una API de Minecraft o de GeckoLib** |

**No conectadas desde esta sesión** (no las des por disponibles; avisa al autor si hacen falta)

- `engineering:github` y `github:github`: fallo de túnel del proxy. Para integrar se usa `git` (funciona) y `gh`
  no está autenticado (`GH_TOKEN` rechazado).
- `desktop-commander`: conecta desde esta sesión, pero no hace falta para este mod: usa las herramientas de archivos y `git` del propio repositorio.

**Reglas de uso**

- Antes de usar una API de Fabric, Yarn o GeckoLib, consulta Context7 o el jar en la caché de Gradle; no la
  deduzcas de memoria (ya hubo nombres de método equivocados).
- Las herramientas que escriben fuera del repositorio (publicar, PRs, comentarios, Slack, tableros) solo tras
  autorización explícita del autor.
- Si una herramienta necesita una cuenta o un host que no está permitido, pide el cambio con el procedimiento
  del entorno y no improvises credenciales.
