# Encargo para Codex: textos de «Las Tres Torres Señoriales»

**Reparto acordado con el autor (2026-11-24):** Claude hace el **código** (layout, `Feature`, controlador, jefes, pruebas) y **Codex los
textos**: lore de las Crónicas, cartas del bestiario y mensajes. Lee antes `AGENTS.md`, `docs/V86_DUNGEON_COLOSO.md` y
`docs/PROMPT_DUNGEON_TRES_TORRES.md` (§1: lore y reglas). Responde en español. No toques código Java salvo que este encargo lo diga; no
toques `gen_*` ajenos. **El nombre ya está decidido (D-39): «Vigilia de las Tres Almenas»**, y su líder es el **Mariscal** (sin nombre propio). Puedes empezar.

## 1. Qué entregas

| Entregable | Detalle |
|---|---|
| **3 entradas de Crónicas** | Nuevas entradas de lore para el Grimorio (`guide/LoreCatalog`, `LoreRules`; fuente de textos `tools/lang/lore_catalog.py`): (a) la orden y su guarnición antes del Primer Eclipse, (b) la corrupción del Mariscal y su armadura, (c) los caballeros «desmontados». Cada una con título, 2–4 párrafos y el desbloqueo que ya cablea Claude (llegar a las torres, vencer a un Guardián, vencer al Coloso) |
| **Carta de bestiario del Coloso Sin Rostro** | Hoy **no tiene** carta. Estadísticas las rellena el código; tú: nombre, consejos de combate, lore con **revelado progresivo por muertes** (mira una carta existente, p. ej. la del Gran Osario, para el formato) |
| **Carta de bestiario del Guardián Desmontado** | Ídem |
| **Mensajes del juego** | Aviso al despertar un Guardián, al abrirse la puerta del Señor, al despertar el Coloso, al vencerlo y el texto del logro (título y descripción; Claude añade el logro a `Achievement`) |

## 1b. Claves que ya existen con texto PROVISIONAL (reescríbelas)

Claude las creó en la fase 3 con una redacción de trabajo en en_us y es_es (y traducciones iniciales pt/fr/de en `tools/lang/ui_catalog.py`). Reescríbelas
con el tono final; **no cambies las claves**:

| Clave | Cuándo sale |
|---|---|
| `towers.aptitudes.guardian_woke` | Despierta un Guardián (barra de acción) |
| `towers.aptitudes.guardian_down` (`%s` = los que quedan) | Cae un Guardián y falta otro |
| `towers.aptitudes.lord_open` | Cae el segundo Guardián y se abre la puerta del Señor |
| `towers.aptitudes.colossus_woke` | Despierta el Coloso |
| `towers.aptitudes.colossus_down` | Cae el Coloso |
| `advancement.aptitudes.three_towers.title` / `.description` | Logro «Fin de la Vigilia» (id `three_towers`, ya en `Achievement`) |
| `block.aptitudes.three_towers_controller` | Nombre del bloque controlador («Piedra de la Vigilia») |
| `aptitudes.structure.three_towers` | Nombre de la estructura en `/aptitudes locate` |

El id del logro y las claves de arriba están **cerrados**. Las entradas de las Crónicas y las cartas de bestiario aún no existen: pídeme los ids que necesites.

## 2. Reglas de redacción (de `docs/lore/`)

- Tono y vocabulario de los textos ya existentes (`lore_catalog.py`): sobrio, de archivo; **sin** explicar de más; sin dar por seguro lo que el lore
  deja abierto (la Conciencia, la Fractura: D-07 a D-09).
- El «Primer Eclipse» es **siempre** el hecho histórico de la Primera Era (período 8). El Eclipse del jugador se llama «Eclipse».
- **Prohibido:** linajes, razas, pactos, herencias, ascendencia; «casa», «estirpe», «dinastía», «herederos»; cualquier guiño reconocible a Juego de
  Tronos (casas con animales heráldicos, «Guardia de la Noche», «Mano del Rey»…). Es una **orden** y su líder tiene un **cargo** (Mariscal).
- El Mariscal **no tiene nombre propio**. Los caballeros tampoco. Evita nombres propios nuevos.
- Que el alma quede *infundida en una armadura* es un hecho de la Primera Era, **no** un pacto que el jugador pueda hacer ni heredar.

## 3. Formato técnico

- Claves nuevas en `en_us.json` y `es_es.json` **sin reformatear**; después copia `es_es.json` a `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy`, `es_ve`.
  `pt_br`, `fr_fr` y `de_de` salen de los generadores de `tools/lang/` (`lore_catalog.py` + `gen_lore_lang.py`, `ui_catalog.py` +
  `gen_interface_lang.py`); ejecuta sus `--check`. Un `%` literal se escribe `%%`.
- Textos de lore: sigue `tools/lore/lore_entries.json` y la lista de claves `lore.aptitudes.*`; ejecuta las pruebas de Python de `tools/lang/`.
- `./gradlew build` real antes de decir que algo funciona. Commits convencionales con `Co-Authored-By: Claude <noreply@anthropic.com>` solo si los
  hace Claude; los tuyos, según `docs/FLUJO_GIT.md`. Push solo a tu rama de trabajo.

## 4. Coordinación

Claude toca `dungeon/`, `world/`, `boss/`, `achievement/Achievement` y los bloques; tú tocas `tools/lang/lore_catalog.py`, `tools/lore/`,
`guide/LoreCatalog` y `guide/BestiaryRules` (solo textos y entradas). Si necesitas un id de entrada o de logro, pídelo en lugar de inventarlo.
