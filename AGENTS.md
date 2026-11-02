# AGENTS.md — Instrucciones para agentes de código (Codex y similares)

Lee este archivo completo antes de tocar nada. Resume el estado del proyecto, las reglas que el autor
exige y lo que **no** debes hacer. Responde al autor en **español**, con rigor técnico.

## 1. Proyecto

- Mod de Minecraft **Aptitudes**: Fabric, Minecraft **1.21.1**, Java **21**, paquete `dev.aptitudes`.
- Versiones fijadas (no las cambies): Loom 1.10.5, Yarn `1.21.1+build.3`, Fabric Loader 0.16.14,
  Fabric API `0.116.6+1.21.1`, Gradle 8.14.3.
- Compilar y probar: `.\gradlew.bat build` en Windows (o `./gradlew build`). **Nunca digas que algo
  compila o que las pruebas pasan sin haberlo ejecutado**; si no pudiste ejecutarlo, dilo.
- Documentación viva: `README.md`, `docs/ROADMAP_APTITUDES.md` (estado real y pendientes),
  `docs/BALANCE.md`, `docs/CONFIGURATION.md` y `docs/TESTING.md`. Actualízalas cuando cambies algo.

## 2. Estado (lo que YA existe: no lo reimplementes ni lo dupliques)

| Sistema | Dónde |
|---|---|
| 10 habilidades por uso (Minería, Excavación, Tala, Agricultura, Combate, Tiro con arco, Defensa, Agilidad, Pesca, Encantamiento) con hitos 10–50; la pantalla K está diseñada para 5 filas × 2 | `data/Skill`, `logic/XpEvents`, `logic/SkillMilestones` |
| Rendimiento decreciente de XP por fuente repetida | `logic/XpDiminishing` (puro, con pruebas) |
| Bloques colocados por jugadores (sin XP al romperlos) y colocaciones confirmadas | `logic/PlacementTracker`, `logic/PlacedBlockLog` |
| Atributos estilo Souls en la Hoguera, mancha de XP, frascos | `souls/`, `logic/AttributeApplier`, `logic/AttributeScaling` |
| 20 roles, árbol de 23 nodos + legendario, sendas, evoluciones, armas de evolución | `role/`, `ability/`, `item/weapon/` |
| Elementalista: 14 elementos y 10 fusiones | `role/Element`, `ability/ElementEffects`, `ability/ElementFusions` |
| Legendarias (exclusividad opcional `legendaryExclusive`) | `unique/` |
| Esbirros y torretas | `ability/Minions`, `ability/MinionBrain`, `ability/AbilityState` |
| Accesorios, élites/campeones, estructuras, grimorio (18 capítulos) | `accessory/`, `elite/`, `world/`, `item/GuideBookItem`, `client/screen/GuideScreen` |
| Configuración validada al cargar | `config/AptitudesConfig.validated` |
| Tiempos de espera persistentes (habilidades, definitiva, firma, legendaria, Inmortalidad carmesí) | `ability/CooldownBook` + attachment `COOLDOWNS`; usa `AbilityManager.cooldowns(p)` y `gameTime(p)` |
| Filtro de objetivos: `MobEntityMixin` (setTarget) + `BrainTargetFilter` (mobs con IA Brain) | `ability/AbilityState.shouldBlockTarget` es la única regla: amplíala ahí |
| Bloques temporales de habilidades (persistentes, sin drops) | `world/TemporaryBlocks.place(...)`; nunca `setBlockState` + `schedule` para bloques temporales |

Historial reciente en `main`: auditoría de seguridad y balance, v14 (grimorio, elementos y armas), v15
(Pesca y rendimiento decreciente), v15.1 (árbol con legendarias exclusivas), v15.2 (grimorio tras la
muerte y pruebas de configuración) y v16 (Defensa con `AFTER_DAMAGE`, esbirros sin conversión a
ahogado, Bombardeo orbital sin fuego amigo), v17 (anti-granjas de colocar y romper), v18 (esperas y muros persistentes) y v19 (desplazamiento de la
pantalla de roles y filtro para mobs con IA Brain), v20 (grimorio interactivo con recetas reales) y
v21 (Encantamiento y pantalla K animada).

## 3. Exclusiones absolutas (no negociables)

**NO implementes ni propongas**: linajes, sistemas de razas, pactos, herencias raciales o de linaje,
árboles de progresión basados en ascendencia, ni mecánicas que introduzcan estos conceptos de forma
indirecta. Tampoco los sugieras como futuras expansiones. Los antiguos conceptos de «pacto» ya se
retematizaron (p. ej., «Talismán maldito»). **No cambies los ids de registro existentes**
(`pact_talisman` se mantiene por compatibilidad con mundos guardados).

## 4. Política de Git (obligatoria)

- Puedes inspeccionar el repositorio, crear ramas, consultar el historial y usar `git add`.
- **Commit solo con autorización explícita del autor**, una vez por commit. Antes de pedirla, informa:
  1) archivos incluidos, 2) resumen, 3) resultado REAL de compilación y pruebas, 4) mensaje propuesto
  (convencional: `feat:`, `fix:`, `docs:`, `build:`…).
- **Nunca** hagas `git push`, merge a `main` ni abras PR sin autorización explícita.
- **Nunca** uses `reset`, `clean`, `checkout` ni `restore` para descartar cambios, ni sobrescribas
  trabajo existente sin autorización. No reinicies el proyecto ni borres trabajo válido.
- Trabaja en una rama propia (p. ej., `codex/<tema>`), nunca directamente en `main`.

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
  se escribe `%%`.
- **Interfaz**: la pantalla K (`AptitudesScreen`) mide 430×262 y tiene exactamente 10 tarjetas (5×2): una
  habilidad nueva rompe la simetría. El grimorio lee las recetas del juego: no las copies en los textos.

## 6. Pruebas

- JUnit Platform con Jupiter (JUnit 5) y Vintage (JUnit 4): ambos estilos valen.
- Pruebas existentes (no las sobrescribas ni crees otras con el **mismo nombre y paquete**):
  `souls/SoulsMathTest`, `logic/AttributeScalingTest`, `logic/XpDiminishingTest`,
  `logic/PlacedBlockLogTest`, `ability/CooldownBookTest`, `world/TemporaryBlockScheduleTest`,
  `role/RoleRulesTest`, `data/ProgressRulesTest` y
  `config/AptitudesConfigValidationTest`.
- **No escribas pruebas que llamen a métodos que no existen.** Si una prueba necesita una API nueva,
  impleméntala en el código principal en el mismo cambio y explícalo.
- Prueba lógica pura; las clases que cargan registros de Minecraft no se prueban con JUnit simple.

## 7. Pendientes conocidos (consulta antes de empezar algo grande)

- Activar `attributeScaling = SOFTCAP` por defecto: **decisión del autor** tras probar en partida.
- Pruebas en partida de la v15–v16 (`docs/TESTING.md`, puntos 6–9).
- Limitaciones abiertas: tabla «Limitaciones conocidas» del `README.md` (colisión de
  transformaciones; el filtro Brain actúa cada 5 ticks).

## 8. Al terminar una tarea

Entrega un informe breve con: qué cambiaste y por qué, archivos tocados, resultado real de
`gradlew build`, riesgos o efectos de balance, y el commit propuesto (sin ejecutarlo).
