# Prompt para Codex — continuar «Aptitudes» con la v29 (Grupos) y la v30 (Contratos del gremio)

> Copia TODO este archivo como primer mensaje en Codex. Responde siempre en **español**.

---

## 0. Antes de escribir una sola línea

1. En la carpeta del proyecto (`C:\Users\Johan\Downloads\aptitudes\aptitudes`):
   `git pull origin main` → debe quedar en el commit de la **v28 (combos)** o posterior.
2. Lee completos, en este orden: `AGENTS.md`, `docs/CONTEXTO_CODEX.md` y `docs/V28_PROPUESTA.md`
   (la sección «Cambios del autor» manda sobre el resto del documento).
3. Compila ANTES de cambiar nada: `.\gradlew.bat build`. Si falla sin tus cambios, avisa y no sigas.
4. Crea tu rama: `git checkout -b codex/v29-grupos` (y después `codex/v30-contratos`).

---

## 1. Lo que NO debes tocar (causó errores reales en este proyecto)

| No toques | Por qué (qué pasó o qué pasaría) |
|---|---|
| `build.gradle` → bloque `dependencies` y `test { useJUnitPlatform() }` | Las pruebas usan **JUnit 5 (Jupiter) y JUnit 4 a la vez**. Ya hubo un build roto con «package org.junit.jupiter.api does not exist» porque faltaba Jupiter. La configuración correcta es exactamente esta y **no se cambia**:<br>`testImplementation platform("org.junit:junit-bom:5.10.2")`<br>`testImplementation "org.junit.jupiter:junit-jupiter"`<br>`testImplementation "junit:junit:4.13.2"`<br>`testRuntimeOnly "org.junit.vintage:junit-vintage-engine"`<br>`testRuntimeOnly "org.junit.platform:junit-platform-launcher"` |
| `gradle.properties`, `gradle/wrapper/*`, versión de Loom en `build.gradle` | Versiones fijas: Minecraft **1.21.1**, Yarn **1.21.1+build.3**, Loader **0.16.14**, Fabric API **0.116.6+1.21.1**, Loom 1.10, Gradle **8.14.3**, Java **21**. No subas ni bajes nada. |
| Dependencias nuevas | **No añadas ninguna** (ni Cloth Config, ni Mod Menu, ni GeckoLib, ni librerías de UI). Todo se hace con Fabric API y Minecraft. |
| `fabric.mod.json` (`depends`, entrypoints) y `aptitudes.mixins.json` (`defaultRequire = 1`) | Un mixin con nombre de método equivocado **tumba el juego al arrancar**. Evita mixins; si uno es imprescindible, usa `require = 0` y avisa (ver `PlayerEntity#getBaseDimensions` en `PlayerEntityMixin`). |
| Ids de registro, nombres de enums y su ORDEN, claves guardadas | Se guardan en los mundos. `pact_talisman`, los nodos `<rol>_m1..m5` / `<rol>_u1..u5`, los attachments (`progress`, `roles`, `morph`, `souls`, `cooldowns`…), las habilidades (`Skill`) y el formato de `LoadoutCodec`. Solo se puede **añadir al final**. |
| Nombres de campos existentes de `AptitudesConfig` | Los servidores ya tienen `config/aptitudes.json` con esos nombres. Añade campos nuevos; no renombres. |
| Pruebas existentes | No sobrescribas ni crees otra con el **mismo nombre y paquete**: `souls/SoulsMathTest`, `logic/AttributeScalingTest`, `logic/XpDiminishingTest`, `logic/PlacedBlockLogTest`, `logic/ComboCounterTest`, `ability/CooldownBookTest`, `world/TemporaryBlockScheduleTest`, `role/RoleRulesTest`, `role/LoadoutCodecTest`, `role/EvolutionPointsTest`, `morph/MorphSizeRulesTest`, `data/ProgressRulesTest`, `config/AptitudesConfigValidationTest`. |
| Archivos GENERADOS | PNG de `textures/item` y `textures/models/armor` (usa `tools/textures/gen_items.py` y `gen_armor.py`) y los textos `node.aptitudes.<rol>_m*/_u*` (usa `tools/lang/gen_evolved_lang.py`). |
| `__pycache__/`, `*.pyc`, `build/`, `run/` | No los subas (ya están en `.gitignore`). |

## 2. Errores que ya ocurrieron y cómo evitarlos

1. **Pruebas que llaman a métodos que no existen** (rompió `compileTestJava`). Si una prueba necesita una API
   nueva, impleméntala en el código principal **en el mismo cambio**. Nunca escribas una prueba «para el futuro».
2. **Acceso privado y tipos**: una prueba llamaba a un método `private` y otra pasaba `long` a un `int`
   («possible lossy conversion»). Compila las pruebas (`.\gradlew.bat test`) antes de entregarlas.
3. **API de otra versión de Minecraft**: en 1.21.1 se usa `DrawContext.drawItemInSlot` (no `drawStackOverlay`,
   que es de 1.21.2+), `Item.TooltipContext.create(world)`, `ItemStack.VALIDATED_CODEC`, etc. Si dudas de un
   nombre, búscalo en las fuentes de Yarn 1.21.1 (`.\gradlew.bat genSources`) antes de usarlo.
4. **Idiomas**: cada texto nuevo va en `en_us.json` y `es_es.json`; después **copia `es_es.json` tal cual** a
   `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy` y `es_ve`. Un `%` literal se escribe `%%`. Valida que el JSON sea
   correcto. No uses saltos de línea escapados dos veces (`\\n`).
5. **Desbordamientos**: valores del cliente o de la configuración → cálculo en `long` y límites (hubo un exploit
   en la Hoguera).
6. **Nunca digas que compila o que las pruebas pasan sin haberlo ejecutado.**

## 3. Política de Git (obligatoria)

- Trabaja en tu rama `codex/...`. **Commit solo cuando el autor lo autorice**, uno por autorización, y antes
  informa: 1) archivos, 2) resumen, 3) resultado REAL de `.\gradlew.bat build` (número de pruebas), 4) mensaje
  convencional (`feat:`, `fix:`, `docs:`…).
- **Nunca** `git push`, merge a `main` ni PR sin autorización. **Nunca** `reset`, `clean`, `checkout` o `restore`
  para descartar cambios.

## 4. Exclusiones absolutas

No implementes ni propongas linajes, razas, pactos, herencias raciales o de linaje, ni progresión por ascendencia
(tampoco de forma indirecta).

---

## 5. TAREA v29 — Grupos (party)

Especificación (decidida por el autor):

| Aspecto | Requisito |
|---|---|
| Abrir | **Tecla nueva** (por defecto `P`, configurable en Controles) que abre el **menú de grupo** |
| Menú | Lista de **jugadores conectados** con botón **Invitar**; lista de miembros (líder marcado); botones **Salir** y, para el líder, **Expulsar** |
| Invitación | Al invitado le llega un mensaje con **[Aceptar] [Rechazar]** clicables (caduca a los 60 s). Comandos alternativos: `/party invite|accept|leave|kick|list` |
| Tamaño | Máximo 5 (`partyMaxSize`) |
| XP compartida | **El líder elige el porcentaje** (0–50 %, de 5 en 5) en el menú. Se aplica a la XP de combate y de rol del que mata, y llega a **todos los miembros conectados, estén donde estén** (no se resta al que mata). No se reparte XP que ya viene de un reparto (evita bucles) |
| Bonificación | **+5 % de XP por cada miembro conectado** (máx. +20 %), para **todos** los miembros |
| Fuego amigo | Los miembros no se pueden dañar entre sí (tampoco con habilidades). El líder puede permitirlo (botón en el menú) |
| Apoyo | Curas, auras y escudos (p. ej. `EvolvedAbilities.allies`, Bardo, Paladín, Herbolista) afectan a **tu grupo** si estás en uno; si no, como ahora |
| Combos | El remate de Guardián (`Combos.finisher`, caso `GUARDIAN`) cura también a los miembros cercanos |
| HUD | Marcos de grupo a la izquierda: nombre, icono del rol principal y barra de vida; atenuados si el miembro está desconectado o en otra dimensión |
| Persistencia | El grupo sobrevive a reinicios y desconexiones (`PersistentState` en el mundo principal) |

Guía de implementación:
- Paquete nuevo `dev.aptitudes.party`: `PartyState` (PersistentState), `PartyManager` (servidor), `PartyRules`
  (**puro**: reparto de XP, bonificación por miembros, tope de tamaño) con prueba `party/PartyRulesTest`.
- Red: payloads C2S validados en el servidor (invitar, aceptar, salir, expulsar, cambiar %; solo el líder puede
  cambiar el % y expulsar; comprobar que el objetivo existe y está conectado) y un S2C de sincronización (miembros,
  vida, rol, %), enviado como mucho cada 10 ticks.
- Regla de aliados: añade un método único (p. ej. `Targeting.isAlly(player, other)`) y úsalo donde ahora se usa
  `Targeting.playersAround` para curar o mejorar. El daño entre miembros se cancela en el `ALLOW_DAMAGE` de
  `RoleEvents` (respeta el orden de la cadena existente).
- La XP: engancha el reparto donde se da la XP por matar (`XpEvents` y `RoleManager.addRoleXp`), no en
  `ProgressManager.addXp` en general.
- Configuración nueva con límites en `validated(...)` y fila en `docs/CONFIGURATION.md`: `partyMaxSize` (5),
  `partyMaxXpShare` (0.5), `partyXpBonusPerMember` (0.05), `partyXpBonusMax` (0.20).
- Pantalla: debe caber con escala de interfaz 4 (≈ 480×270 px útiles).

## 6. TAREA v30 — Contratos del gremio

| Aspecto | Requisito |
|---|---|
| Dónde | **Bloque nuevo «Tablón de contratos»** (`aptitudes:contract_board`), con receta propia (en la Mesa de trabajo normal), modelo, textura y loot table (se suelta a sí mismo). Clic derecho abre el tablón |
| Oferta | 3 contratos nuevos **por día de Minecraft** y jugador; **2 activos** a la vez (3 con rango Veterano); abandonar es gratis. La oferta del día es **fija por jugador y día** (semilla = UUID + día): cerrar y abrir no la cambia |
| Tipos | Caza (matar N de un tipo), Entrega (N minerales, cultivos o peces del inventario), Pesca (N peces), Encantamiento (N objetos), Oficio (N de XP en una habilidad), Forja (una pieza de armadura en la Mesa del gremio) |
| Dificultad | Fácil, Normal y Difícil según la reputación; **además**, con el rol principal en **Maestro** aparecen contratos **de Élite** y con **Definitivo**, **Legendarios** (objetivos mayores y mejores recompensas) |
| Recompensas | Esmeraldas, Fragmentos de alma, XP del rol principal y reputación; Élite/Legendario pueden dar Vales de armería o Emblemas de maestría |
| Reputación | Aprendiz → Oficial (100) → Veterano (300) → Maestro (700) |
| Grupo | Los contratos de Caza avanzan también con las muertes de los miembros del grupo (v29) |
| Persistencia | Attachment nuevo `aptitudes:contracts` con `copyOnDeath` |

Guía de implementación:
- `ContractRules` (**puro**: generación determinista con semilla, objetivos por dificultad y evolución,
  recompensas, rangos de reputación) con prueba `contract/ContractRulesTest`.
- Progreso con los eventos que ya existen (muerte en `XpEvents`, estadísticas de pesca y encantamiento,
  `ProgressManager.addXp` para Oficio, `GuildDeskScreenHandler.ResultSlot.onTakeItem` para Forja). Entregar,
  aceptar y cobrar: **siempre validado en el servidor** (que el contrato exista, sea suyo, esté completo, que
  tenga los objetos).
- Textura del bloque: genérala con un script en `tools/textures/` (mismo estilo que los generadores existentes)
  o dibújala a mano en 16×16; no edites los PNG generados por otros scripts.
- Añádelo al grimorio (capítulo del gremio) y a `docs/TESTING.md`.

---

## 7. Antes de pedir el commit (lista de comprobación)

- [ ] `.\gradlew.bat build` termina en **BUILD SUCCESSFUL** y dices cuántas pruebas pasaron.
- [ ] No cambiaste `build.gradle`, `gradle.properties`, el wrapper ni `fabric.mod.json` (salvo que el autor lo pida).
- [ ] Las pruebas nuevas solo usan métodos que existen y tienen nombres nuevos.
- [ ] Los JSON de idioma son válidos y las 6 variantes son copias exactas de `es_es.json`.
- [ ] Documentación al día: `README.md`, `docs/ROADMAP_APTITUDES.md`, `docs/CONFIGURATION.md`,
      `docs/BALANCE.md`, `docs/TESTING.md`, `AGENTS.md` y `docs/CONTEXTO_CODEX.md`.
- [ ] Informe final: qué cambiaste y por qué, archivos, resultado real del build, riesgos y efectos de balance,
      y el mensaje de commit propuesto (sin ejecutarlo).
