# V87 — Retirada de la Piedra sellada, el Sello del templo y la Llave del templo

Decisión del autor (2026-11-26): se retiran los tres del mod; el templo pasa a «jefe y cofre directos»; se pueden romper los mundos de prueba.

## Qué cambió

| Antes | Ahora |
|---|---|
| Bloque `sealed_stone` («Piedra sellada») | Bloque `vigil_gate` («Reja de la Vigilia»): irrompible, textura propia de barrotes (`gen_temple_blocks.py`). **Interino**: el diseño definitivo de las puertas entre fases es parte del rediseño de Codex |
| Bloque `temple_seal` («Sello del templo», interactivo, con aviso de estado) | Bloque `temple_controller`: **oculto** (textura de pizarra), irrompible, sin interacción ni objeto. `TempleSealBlockEntity` → `TempleControllerBlockEntity` |
| Objeto `temple_key` («Llave del templo»), que soltaba el guardián y abría el Relicario | Eliminado. El Relicario se abre al hacer clic derecho **cuando el guardián del templo ha caído** (`Temples.templeCleared`, busca el controlador en los chunks cargados de alrededor y compara el id del templo) |
| `world/TempleFeature` (templo antiguo de 15×15 con puzle) y su `configured/placed_feature` | Borrados. La paleta por anillo y el cálculo del anillo se mueven a `CustodianTempleFeature` |
| Tipo `temple` de `/aptitudes locate` | Quitado (`StructureLocateRules.KINDS` pasa a 5) |
| Celdas `Cell.SEALED` y `Cell.SEAL` de los planos | `Cell.GATE` y `Cell.CONTROLLER` (`CustodianTempleLayout`, `TowersLayout`) |
| Icono del logro «Sin sello» y del bestiario del Custodio (`temple_key`) | `life_vessel` y la llave de diamante del Custodio |

Textos: se quitan los mensajes `temple.aptitudes.seal.*`, `reliquary.wrong_key`, `block.aptitudes.sealed_stone|temple_seal`, `item.aptitudes.temple_key*` y
`aptitudes.structure.temple`; se añaden `block.aptitudes.vigil_gate` y `temple_controller`; cambian `guardian_defeated`, `reliquary.sealed`, la descripción de
«Sin sello» y el capítulo de estructuras del grimorio (en los 5 idiomas con sus generadores).

## Lo que NO se tocó (código muerto a retirar en el rediseño)

El Templo del Custodio **nuevo siempre genera el diseño hondo** (`layout` 3 y 4). Siguen en el código, sin generarse nunca en mundos nuevos, los
diseños 0–2 y su puzle de palancas y placas (`TempleControllerBlockEntity.leversTick/platesTick/awaken/openStairs`, `TempleRules`,
`CustodianTempleLayout.build` con `LEVER`/`PLATE`, `Flow.PUZZLE`). Los retirará Codex al rediseñar `CustodianTempleLayout`.

## Verificación

`./gradlew build`, `check_lang.py`, los `--check` de los generadores de idiomas y `gen_temple_blocks.py --check`. **Sin probar en partida.**
