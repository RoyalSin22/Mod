# Prompt para Codex — v29 (Grupos), v30 (Contratos) y v31 (Logros) sobre el `main` actual

> Copia TODO este archivo como primer mensaje en una tarea NUEVA de Codex. Responde siempre en **español**.
> Sustituye a `docs/PROMPT_CODEX_v29_v30.md` (escrito cuando `main` estaba en la v28; ya no vale como base).

---

## 0. Antes de escribir una sola línea

1. Actualiza: `git pull origin main`. Debes estar en la **v41 (eventos rotativos)** o posterior
   (`git log --oneline -1` → «feat: v41 …» o más nuevo).
2. Lee completos, en este orden: `AGENTS.md`, `docs/CONTEXTO_CODEX.md`, `docs/FLUJO_GIT.md` y
   `docs/V28_PROPUESTA.md` (sección «Cambios del autor»: manda sobre el resto de ese documento).
3. Compila ANTES de cambiar nada: `.\gradlew.bat build`. Si falla sin tus cambios, **para y avisa** con el error
   completo. (La primera compilación descarga Mod Menu: necesita internet.)
4. **Una versión por tarea y por rama**, creadas desde el `main` actual:
   - v29 → `codex/v29-grupos-v2`
   - v30 → `codex/v30-contratos-v2` (después de que la v29 esté en `main`)
   - v31 → `codex/v31-logros-v2` (después de la v30)

   Las ramas antiguas `codex/v29-grupos`, `codex/v30-contratos` y `codex/v31-logros` están **vacías** y apuntan a
   la v28: **no las uses**.
5. Al terminar cada versión, **no cierres la tarea**: el autor hará el commit y el push desde tu worktree
   (ver `docs/FLUJO_GIT.md`). Indica en tu informe la **ruta del worktree** y la **rama**.

---

## 1. Lo que NO debes tocar (ya causó errores reales)

| No toques | Por qué |
|---|---|
| `build.gradle`: bloque de pruebas y `test { useJUnitPlatform() }` | Las pruebas usan **JUnit 5 y JUnit 4 a la vez**. Ya se rompió el build por faltar Jupiter. Debe quedar exactamente: `testImplementation platform("org.junit:junit-bom:5.10.2")`, `testImplementation "org.junit.jupiter:junit-jupiter"`, `testImplementation "junit:junit:4.13.2"`, `testRuntimeOnly "org.junit.vintage:junit-vintage-engine"`, `testRuntimeOnly "org.junit.platform:junit-platform-launcher"` |
| Versiones (`gradle.properties`, wrapper, Loom) | Minecraft **1.21.1**, Yarn **1.21.1+build.3**, Loader **0.16.14**, Fabric API **0.116.6+1.21.1**, Gradle **8.14.3**, Java **21**, Mod Menu **11.0.3** (solo `modCompileOnly`). No subas ni bajes nada |
| Dependencias nuevas | **Ninguna**. Mod Menu es la única excepción aprobada (v40) |
| Mixins | Evítalos. `defaultRequire = 1`: un nombre de método equivocado **tumba el juego al arrancar**. Si uno es imprescindible: `require = 0` y **sin `@Shadow`** de campos (un `@Shadow` que no existe tumba el arranque aunque haya `require = 0`) |
| Ids de registro, enums y su ORDEN, claves guardadas | Se guardan en los mundos. Solo se **añade al final** (ver `BossKind`, `RuneKind`, `WorldEventKind`) |
| Nombres de campos de `AptitudesConfig` | Los servidores ya los tienen en `config/aptitudes.json`. Añade campos; no renombres |
| Pruebas existentes | No sobrescribas ni repitas nombre y paquete (lista en `AGENTS.md` §6) |
| Archivos generados | Texturas (`tools/textures/*.py`), sonidos (`tools/audio/gen_sounds.py`), textos de nodos evolucionados (`tools/lang/gen_evolved_lang.py`) |
| **Formato** | `.editorconfig` y `.gitattributes`: LF; Java con tabulación; **idiomas JSON con 2 espacios**. **No reformatees** archivos existentes: 17 claves nuevas acabaron siendo 5 000 líneas de diferencia |

## 2. Errores que ya ocurrieron

1. **Pruebas que llaman a métodos que no existen**: si una prueba necesita una API nueva, impleméntala en el mismo cambio.
2. **Acceso privado y tipos**: pruebas que llamaban a métodos `private` o pasaban `long` a `int`. Compila las pruebas.
3. **API de otra versión**: en 1.21.1 existen `NbtComponent` (no `CustomData`), `NbtCompound.getCompound` (no
   `getCompoundOrEmpty`), `AttributeModifiersComponent.with(EntityAttributes.GENERIC_…, …)` (el primer argumento es
   el atributo, no un `Identifier`) y `DrawContext.drawItemInSlot`. Si dudas: `.\gradlew.bat genSources`.
4. **Idiomas**: claves en `en_us.json` y `es_es.json`; **copia `es_es.json` tal cual** a `es_mx`, `es_ar`, `es_cl`,
   `es_ec`, `es_uy` y `es_ve`. `%` literal con argumentos → `%%`. Sin `\\n` dobles. Sin reformatear.
5. **Commits que no llegan**: trabajabas en un worktree y la tarea se cerró sin commit (se perdieron v29–v31). Sigue
   `docs/FLUJO_GIT.md`. Si `git commit` falla, **no improvises**: copia el error y avisa.
6. **Nunca digas que compila o que las pruebas pasan sin haberlo ejecutado.**

## 3. Política de Git

Commit solo con autorización del autor, uno por autorización; antes informa de archivos, resumen, resultado REAL de
`.\gradlew.bat build` (número de pruebas) y mensaje convencional. Nunca `push`, merge ni PR sin autorización. Nunca
`reset`, `clean`, `checkout` o `restore` para descartar cambios.

## 4. Exclusiones absolutas

Nada de linajes, razas, pactos, herencias raciales o de linaje ni progresión por ascendencia, ni sacrificios a cambio
de poder (tampoco de forma indirecta).

## 5. Sistemas que ya existen y con los que debes integrarte (no los dupliques)

| Sistema | Dónde | Integración que se espera |
|---|---|---|
| Combos (v28) | `ability/Combos` | Remate de Guardián cura también al grupo cercano (v29) |
| Runas (v32, v35) | `rune/RuneSocketing` | Logros: primera runa engarzada (v31) |
| Jefes (v33) | `boss/BossManager`, `BossKind` | Contratos Legendarios «derrota a un jefe» (v30); logro por jefe (v31) |
| Eclipse y eventos (v34, v41) | `event/EclipseManager`, `event/WorldEventManager` | Oleadas y Heraldo escalan con el **grupo** (en `EclipseManager.spawnHeralds` ya se agrupan jugadores a 64 bloques: usa el grupo si existe) (v29); contratos «sobrevive a un evento» (v30); logros de evento (v31) |
| Configuración (v40) | `config/ConfigSync`, pantalla `ConfigScreen` | Tus campos nuevos aparecen solos en la pantalla; solo necesitan límite en `validated(...)` y fila en `docs/CONFIGURATION.md` |
| Interfaz | — | Toda pantalla nueva cabe en **426×240** o se desplaza (ver `AptitudesScreen.mouseScrolled`) |

---

## 6. TAREA v29 — Grupos (party)

| Aspecto | Requisito (decidido por el autor) |
|---|---|
| Abrir | Tecla nueva (por defecto `P`, configurable) que abre el **menú de grupo** |
| Menú | Jugadores conectados con botón **Invitar**; miembros (líder marcado); **Salir** y, para el líder, **Expulsar** |
| Invitación | Mensaje con **[Aceptar] [Rechazar]** clicables, caduca a los 60 s. Comandos alternativos `/party invite|accept|leave|kick|list` |
| Tamaño | Máximo 5 (`partyMaxSize`) |
| XP compartida | El **líder elige el %** (0–50 %, de 5 en 5). Sobre la XP de combate y de rol del que mata; llega a **todos los miembros conectados, estén donde estén**; no se resta al que mata; no se reparte XP que ya viene de un reparto |
| Bonificación | +5 % de XP por miembro conectado (máx. +20 %), para todos |
| Fuego amigo | Los miembros no se dañan entre sí (tampoco con habilidades ni runas); el líder puede permitirlo |
| Apoyo | Curas, auras y escudos afectan a **tu grupo** si estás en uno; si no, como ahora |
| HUD | Marcos a la izquierda: nombre, icono de rol, vida; atenuados si está desconectado o en otra dimensión |
| Persistencia | `PersistentState` en el Mundo Normal |

Implementación: paquete `dev.aptitudes.party` con `PartyState`, `PartyManager` y `PartyRules` (**puro**) + prueba
`party/PartyRulesTest`. Payloads C2S validados en el servidor y un S2C de sincronización (como mucho cada 10 ticks).
Regla única de aliados (p. ej. `Targeting.isAlly`). Configuración: `partyMaxSize` (5), `partyMaxXpShare` (0.5),
`partyXpBonusPerMember` (0.05), `partyXpBonusMax` (0.20).

## 7. TAREA v30 — Contratos del gremio

| Aspecto | Requisito |
|---|---|
| Dónde | Bloque nuevo **Tablón de contratos** (`aptitudes:contract_board`), receta, modelo, textura (generada por script) y loot table |
| Oferta | 3 por día y jugador, fija por jugador y día (semilla UUID + día); 2 activos (3 con Veterano); abandonar gratis |
| Tipos | Caza, Entrega, Pesca, Encantamiento, Oficio (XP en una habilidad), Forja (Mesa del gremio) y **Evento** (sobrevivir a un Eclipse, Niebla de almas o Lluvia de brasas) |
| Dificultad | Por reputación; con rol en **Maestro** aparecen de **Élite** y con **Definitivo**, **Legendarios** (incluye «derrota a un jefe») |
| Recompensas | Esmeraldas, Fragmentos de alma, XP de rol, reputación; Élite/Legendario: Vales de armería o Emblemas de maestría |
| Reputación | Aprendiz → Oficial (100) → Veterano (300) → Maestro (700) |
| Grupo | Los de Caza avanzan con las muertes del grupo (v29) |
| Persistencia | Attachment `aptitudes:contracts` con `copyOnDeath` |

Implementación: `ContractRules` (**puro**) + `contract/ContractRulesTest`. Todo validado en el servidor (aceptar,
entregar, cobrar).

## 8. TAREA v31 — Logros (advancements)

- Logros de Minecraft (JSON en `data/aptitudes/advancement/…`, pestaña propia con icono del grimorio). Disparadores:
  usa los criterios vanilla cuando basten (`inventory_changed`, `player_killed_entity`…) y, para el resto, un
  criterio propio sencillo o `/advancement`-equivalente desde el servidor (`PlayerAdvancementTracker.grantCriterion`).
- Lista mínima: encender la Hoguera; elegir rol; evolucionar a Maestro y a Definitivo; combo ×20; engarzar una runa;
  derrotar a cada jefe; sobrevivir a cada evento del mundo; completar 10 contratos; formar un grupo completo.
- Textos en idiomas (título y descripción) y una línea en el grimorio.

---

## 9. Antes de pedir el commit

- [ ] `.\gradlew.bat build` → **BUILD SUCCESSFUL** y cuántas pruebas pasaron.
- [ ] No tocaste versiones, el bloque de pruebas de `build.gradle` ni el formato de archivos existentes.
- [ ] Pruebas nuevas con nombres nuevos y solo métodos que existen.
- [ ] JSON válidos; 6 variantes = copia exacta de `es_es.json`.
- [ ] Documentación: `CHANGELOG.md` («Sin publicar»), `README.md`, `docs/ROADMAP_APTITUDES.md`,
      `docs/CONFIGURATION.md`, `docs/BALANCE.md`, `docs/TESTING.md`, `AGENTS.md`, `docs/CONTEXTO_CODEX.md`.
- [ ] Informe: cambios, archivos, build real, riesgos de balance, mensaje de commit, **ruta del worktree y rama**.
