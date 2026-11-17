# Prompts de rediseño — qué falta por mejorar, rediseñar, comprobar, rehacer y retocar (V82)

Inventario del estado visual del mod a 2026-11-17 y un prompt listo para pegar por cada sección. Los prompts de **modelado
3D** van pensados para **Codex + Blockbench**, los de **arte 2D** para **ChatGPT/Codex** y los de **comprobación** para
quien pruebe en partida. Cada prompt es autónomo: copia primero el *Bloque de contexto común* y después el de la sección.

## Estado y prioridad

| # | Sección | Estado hoy | Quién | Prioridad |
|---|---|---|---|---|
| M1 | Hacha de Bersérker y armas Maestro (modelo en mano) | Lámina plana limpia (v81); el modelo vóxel se retiró | Blockbench | **Alta** |
| M2 | Armas base (12) y herramientas de alma | Iconos 16×16 de plantilla; modelo vóxel generado | Blockbench | Alta |
| M3 | Armadura Ascendida (modelo 3D) | Solo textura sobre el modelo vanilla | Codex + Blockbench | **Alta** |
| M4 | Hoguera | Modelo generado de 32 elementos (v81), sin pulir | Blockbench | **Alta** |
| M5 | Bloques funcionales (Mesa del gremio, Tablón, Relicario, sello, puertas, trampilla) | Cubos con textura generada | Blockbench | Media |
| M6 | Jefes y criaturas (9 entidades) | Modelos GeckoLib provisionales generados por script | Blockbench + GeckoLib | **Alta** |
| A1 | Iconos de objetos que no son armas (≈70) | Plantillas 16×16 generadas | ChatGPT / Codex | Media |
| A2 | Aldeano del gremio y mob de la Niebla (texturas) | Texturas provisionales | ChatGPT | Media |
| A3 | Efectos de estado y partículas | Iconos generados | Codex | Baja |
| I1 | Grimorio (limpieza visual) | Cabecera e índice simplificados (v81); quedan 23 ilustraciones sin usar | Codex | Media |
| I2 | HUD (barras, combos, grupo, sinergias, amenaza) | Revisado parcialmente (v66, v81) | Codex | Media |
| I3 | Pantallas (K, Roles/árbol, Reforja, frascos, contratos, grupo, configuración) | Kit de arte aplicado a K y Roles; el resto sin kit | Codex | Media |
| I4 | Iconos de habilidades (F2–F7) | Provisionales; encargos escritos en `docs/PROMPT_ARTE_ICONOS_*.md` | ChatGPT | Media |
| Q1 | Ronda de pruebas en partida | Pendiente (templo hondo, cripta, Crónicas, Osario, F2–F7, armadura, armas) | Tú | **Alta** |
| Q2 | Balance con datos reales | Sin datos de partida | Tú + yo | Media |
| Q3 | Revisión nativa pt/fr/de | Traducción inicial sin revisar | Externa | Baja |
| Q4 | Sonidos propios | Generados por síntesis | Codex | Baja |
| Q5 | Procedencia del arte de IA y créditos | Sin declarar | Tú | Media |

---

## Bloque de contexto común (pegar siempre antes del prompt de la sección)

```
Proyecto: mod de Minecraft «Aptitudes» (Fabric, Minecraft 1.21.1, Java 21, paquete dev.aptitudes, rama de trabajo
claude/cronicas-grimorio). Dirección de arte: FANTASÍA OSCURA tipo Dark Souls / Elden Ring llevada a Minecraft:
paleta apagada (grises azulados, violetas oscuros, óxido), un acento brillante por objeto (brasa naranja, runa
turquesa del Velo, oro viejo), siluetas estoicas y dinámicas, sin colores planos saturados, volumen con luz arriba-izquierda
y sombra abajo-derecha, contorno oscuro selectivo.
Reglas del repositorio (AGENTS.md): responde en español; no cambies ids de registro existentes; no edites a mano los
PNG/JSON generados (se regeneran con los scripts de tools/); respeta .editorconfig y .gitattributes (LF); no reformatees
archivos; no hagas commit en main ni push a otra rama sin permiso; ejecuta ./gradlew build y los --check de tools/ antes
de decir que algo funciona; no inventes resultados. Documenta en CHANGELOG.md y docs/TESTING.md.
Prohibido proponer linajes, razas, pactos o herencias.
```

---

## M1 — Hacha de Bersérker y las 20 armas Maestro (modelo 3D en mano)

**Qué pasa hoy:** el arte (ChatGPT, 64×64) es ruidoso y su extrusión vóxel se veía con bultos y bordes; en la v81 se retiraron
los modelos y se ven como lámina plana de 32×32. El hacha es la que más molesta.

```
TAREA: modelar en Blockbench las armas Maestro, empezando por berserker_weapon_master (el hacha), y exportarlas como
models/item/<arma>_3d.json (formato Java Block/Item). El código (client/model/HandheldModels) ya carga cualquier
<arma>_3d.json y lo usa en mano, suelo y marcos; el icono de inventario sigue siendo textures/item/<arma>.png (32×32).

REQUISITOS POR ARMA:
- Modelo de bajo polígono (≤ 40 cubos), proporciones realistas de 1 bloque de largo máximo en mano, silueta legible a 3 m.
- Texturas: usa un atlas propio 64×64 en textures/item/<arma>_model.png (no sobrescribas <arma>.png ni <arma>_hd.png). Pinta a
  mano con paleta de máx. 24 colores, sin ruido de IA: filos con brillo arriba, muescas, empuñadura envuelta, un acento de color
  del rol (ver tabla de roles en tools/textures/gen_items.py → ROLE_COLORS).
- Bloque "display" completo: thirdperson_righthand, firstperson_righthand, gui, ground, fixed, head. Comprueba la pose en primera
  persona (que no tape la mira ni se salga de la pantalla) y en tercera.
- Nombre de archivo exacto: berserker_weapon_master_3d.json, mage_weapon_master_3d.json, … (los 20 ids están en
  tools/textures/gen_items.py → ITEMS terminados en _weapon_master).
- Si añades <arma>_3d.json, el cargador lo detecta solo; para volver a la lámina plana basta borrarlo.

ENTREGA: los JSON y PNG en src/main/resources/assets/aptitudes/, una captura por arma (primera persona, tercera persona,
suelo) y una nota por arma con la lista de cubos. Orden: berserker (hacha) → paladin (martillo) → swordsman → assassin →
resto.
VERIFICA: /give @s aptitudes:berserker_weapon_master y mira mano, suelo, marco de objetos y la mano izquierda.
```

## M2 — Armas base y herramientas de alma

**Qué pasa hoy:** 12 armas de plantilla (`berserker_axe`, `dagger`, `katana`, `spear`, `arcane_staff`, `arcane_revolver`,
`oak_staff`, `holy_hammer`, `plasma_gun`, `wrench`, `hand_wraps`, `hunting_knife`) más `lute`, `necro_tome`, `soul_pickaxe`,
`soul_axe`, `soul_shovel`, `soul_hoe`. Sus modelos vóxel salen de `tools/models/gen_weapon_models.py`.

```
TAREA: rediseñar en Blockbench el modelo 3D y la textura de cada arma base y herramienta de alma (lista arriba), sustituyendo
al modelo vóxel generado. Mantén ids y comportamiento. Entrega models/item/<id>_3d.json + textura 32×32 propia.
REQUISITOS: mismos que M1; las armas base son de gama media (hierro, madera, cuero): más sobrias que las Maestro. El Laúd y el
Tomo del nigromante deben tener tapa/cuerdas/páginas modeladas. La Pistola de plasma y el Revólver arcano deben leerse como armas
de fuego fantásticas, con cañón y empuñadura. Las 4 herramientas de alma comparten motivo (cabeza de piedra de alma, fragmento
turquesa incrustado).
ENTREGA Y VERIFICACIÓN: igual que M1. Después ejecuta python3 tools/models/gen_weapon_models.py … --check y actualiza la lista de
armas del generador para que NO sobrescriba los modelos hechos a mano.
```

## M3 — Armadura Ascendida (modelo 3D con renderer propio)

**Qué pasa hoy:** `item/AscendedArmor` registra 4 piezas con las capas vanilla `ascended_layer_1/2.png` (64×32, generadas con
`tools/textures/gen_ascended_armor.py`). No hay volumen: cuernos, hombreras y capa solo están pintados.

```
TAREA: crear un modelo 3D para la Armadura Ascendida (un único set de 4 piezas: helmet, chestplate, leggings, boots) con
geometría real por encima del cuerpo: cuernos y cresta en el casco, hombreras de placas, gorjal, faldón/capa corta, rodilleras y
botas con punta. Usa Blockbench para el modelo y la textura 128×128 (formato GeckoLib armor o un ArmorRenderer de Fabric API
con un modelo propio basado en BipedEntityModel). Mantén los ids ascended_* y las capas vanilla como respaldo si falla.

REQUISITOS: base de netherita violeta oscura, ribetes de oro brasa, runas turquesa del Velo que brillan (capa emissive). Las runas
y el emblema NO cambian de color; el aspecto del rol se muestra solo con partículas (ya existe en RoleArmorSets.announce).
Debe verse bien en primera y tercera persona, con elytra y con la mano vacía; sin recortes en el cuello ni en los brazos.
Rendimiento: ≤ 120 cubos por pieza.
ENTREGA: modelo, textura, renderer (src/client/java/…/render/AscendedArmorRenderer), registro en el cliente, capturas, y una prueba
JUnit de recursos (existencia y tamaño de los archivos, sin cargar Minecraft). Actualiza docs/V80_ARMADURA_ASCENDIDA.md.
VERIFICA: /give @s aptitudes:ascended_helmet (y las otras tres) y equípalas con y sin elytra.
```

## M4 — Hoguera

**Qué pasa hoy:** `models/block/bonfire.json` (generado por `tools/models/gen_bonfire_model.py`: 14 piedras, brasas, troncos,
espada con espiral y 5 planos de llama). Falta pulir.

```
TAREA: abre models/block/bonfire.json en Blockbench y rediséñala como la hoguera de espada en espiral de Dark Souls: círculo
irregular de piedras caídas, ceniza y huesos entre las piedras, troncos quemados, la espada clavada con la espiral de hierro
(más alta, ~20 unidades, puede sobresalir del bloque hasta y=24 con "display" correcto) y llama animada alrededor de la hoja.
REQUISITOS: usa solo las texturas de bonfire_stone/bonfire_coal/bonfire_flame (puedes añadir bonfire_bones y bonfire_sword de 16×16
generadas desde tools/textures/gen_workstations.py, no a mano). La llama debe poder parpadear (textura animada con .mcmeta).
Mantén la caja de colisión y el outline de 16×16×16 (no cambies el blockstate ni la forma de colisión en ModBlocks).
Cuando termines, borra o actualiza tools/models/gen_bonfire_model.py y la línea de CI que lo comprueba.
ENTREGA: JSON, texturas, captura de día y de noche (con luz de bloque), y nota de cubos. VERIFICA: coloca una Hoguera en un
templo y rodéala; comprueba que no hay z-fighting.
```

## M5 — Bloques funcionales

**Qué pasa hoy:** `guild_desk`, `contract_board`, `reliquary`, `sealed_stone`, `temple_seal`, `crypt_hatch`, `custodian_door` y las 4
`chamber_door_*` son cubos o modelos simples con textura generada (`tools/textures/gen_workstations.py`, `gen_temple_blocks.py`).

```
TAREA: rediseñar en Blockbench cada bloque funcional con volumen y detalle de fantasía oscura, manteniendo su caja de colisión y
su comportamiento:
- Mesa del gremio: mesa de trabajo de artesano con yunque pequeño, pergaminos, un sello y herramientas colgadas.
- Tablón de contratos: tablero de madera con pergaminos clavados, una linterna y un cuerno.
- Relicario: arca de piedra y hierro con una grieta de luz turquesa.
- Sello del templo y piedra sellada: placa de piedra con runas incrustadas que brillan al desbloquearse.
- Puertas de cámara (cobre, hierro, oro, diamante) y Puerta del Custodio: portones de varias hojas con cerradura de runas.
- Trampilla de la cripta: losa con anillo de hierro y símbolo del Velo.
REQUISITOS: texturas generadas en tools/textures/ (no a mano en los PNG); no cambies los ids ni los blockstates; ≤ 40 cubos por
bloque; mantén el POI del aldeano del gremio (data/minecraft/tags/point_of_interest_type).
ENTREGA: modelos, texturas, capturas y las pruebas de recursos existentes en verde (world/GuildRecipeResourcesTest, temple/*).
```

## M6 — Jefes y criaturas (GeckoLib)

**Qué pasa hoy:** 9 entidades con modelos/animaciones provisionales generados por `tools/models/gen_boss_models.py`
(`geo/entity`, `animations/entity`): Coloso Sin Rostro, Guardián Desmontado, Autómata Antiguo (+ orbe), Gran Osario Quebrado,
Osario Quebrado, Errante velado, Escorificador de hierro y Custodio de las cuatro llaves. Contrato en `docs/V63_JEFES.md`.

```
TAREA: sustituir los modelos provisionales por modelos de Blockbench (plugin GeckoLib) con animaciones reales. Una entidad por
iteración, en este orden: Coloso Sin Rostro → Gran Osario Quebrado → Autómata Antiguo → Guardián Desmontado → Custodio de las cuatro
llaves → Osario → Errante → Escorificador.
REQUISITOS por entidad: huesos y nombres de animación EXACTOS a los que usa la clase Java (revisa AnimatedBossEntity y
docs/V63_JEFES.md; si necesitas uno nuevo, añádelo al código y a la prueba de recursos); animaciones: idle, walk, anticipación y golpe
por cada ataque avisado (los nombres salen de BossMoveRules/OsarioBossRules), fase 2, muerte. Textura con capa emisiva
(glowmask) en runas y ojos. Escala acorde a la hitbox declarada.
Dirección: Coloso = gigante de piedra sin rostro agrietado; Gran Osario = montaña de huesos con cráneo coronado; Autómata = engranajes y
latón corroído; Guardián = armadura desmontada flotante; Custodio = sacerdote encapuchado con cuatro llaves; Osario/Errante = no-muertos
de la Niebla; Escorificador = golem de escoria de hierro.
ENTREGA: geo, animaciones, texturas, glowmask, capturas y GIF de cada ataque. Las pruebas boss/*ResourcesTest y --check de
gen_boss_models.py deben pasar o actualizarse (si el modelo ya no sale del script, quítalo del script).
VERIFICA: /summon aptitudes:<entidad> y comprueba cada ataque y la muerte.
```

## A1 — Iconos de objetos que no son armas

**Qué pasa hoy:** ≈70 iconos 16×16 de plantilla (`tools/textures/gen_items.py`): accesorios (6 anillos, 3 amuletos), 8 runas, 5
herramientas de alma, 2 frascos, llaves (`custodian_key_*`, `temple_key`, `llave_del_velo`), materiales de la Niebla
(`jiron_velo_espectral`, `nucleo_osario`, `ancla_del_velo`, `craneo_gran_osario`), consumibles y fichas (`rebirth_offering`,
`armory_voucher`, `mastery_emblem`, `ultimate_core`, `elite_essence`, `champion_heart`, `eclipse_*`, `tower_core`, `shrine_sigil`,
`warlord_medallion`, `expedition_token`, `hunters_trophy`, `life_vessel`, `ascended_template`, `guild_seal`, `mimic_mask`,
`beast_whistle`, `herb_pouch`, `herb_petals`, `guide_book`).

```
TAREA: rediseñar los iconos por GRUPOS (uno por tanda): 1) anillos y amuletos, 2) runas, 3) llaves, 4) materiales de la Niebla y
jefes, 5) frascos y consumibles, 6) fichas y emblemas, 7) herramientas de alma, 8) objetos de rol (laúd, máscara, silbato, bolsa,
tomo).
FORMATO: PNG 16×16 con fondo transparente (si pides arte a ChatGPT, pide 1024×1024 con fondo magenta #FF00FF y cada objeto ampliado ×16 con
rejilla de píxeles visible, como en docs/PROMPT_ARTE_ARMAS.md, y normaliza con el importador de tools/textures/import_weapon_art.py
adaptado a 16×16). Un objeto por imagen.
ESTILO: silueta clara a 16 px, máx. 12 colores por icono, contorno oscuro, un acento brillante, mismos materiales entre objetos del
mismo grupo (los anillos comparten aro y cambian la gema; las runas comparten forma y cambian el glifo y el color).
INTEGRACIÓN: pon las fuentes en tools/textures/source/items/<id>.png y haz que gen_items.py use la fuente si existe (como ya hace con
las armas) o vuelva a la plantilla si no. No edites a mano los PNG de salida. Actualiza el catálogo del grimorio si cambia algo
visible. VERIFICA: python3 tools/textures/gen_items.py … y /give de cada objeto; revisa inventario, mano y tooltip.
```

## A2 — Texturas del aldeano del gremio y mobs de la Niebla

```
TAREA: dibujar las texturas definitivas del Aldeano del gremio (textures/entity/villager/…) y revisar las de Osario quebrado y Errante
velado. El aldeano debe leerse como herrero-escribano del gremio: delantal de cuero, sello al cuello, manguitos, capucha corta y
la insignia del gremio (compás y espada cruzados) en el pecho; paleta marrón, verde bosque y oro viejo.
REQUISITOS: mantén los tamaños de textura vanilla (64×64 para el aldeano) y el UV estándar; capa de profesión por nivel
(Novato→Maestro) con un detalle que crece (botón, galón, medalla). No cambies ids ni el POI.
VERIFICA: spawn del aldeano y de cada nivel de comercio.
```

## A3 — Efectos de estado y partículas

```
TAREA: revisar los iconos de efectos de estado propios (tools/textures/gen_status_effects.py: pociones de alquimia y efectos del
Eclipse/Niebla) y las partículas de habilidades (ability/ParticleFx y CastFx): coherencia de paleta con la dirección de arte,
legibilidad a 18×18 px, y que el color de cada partícula identifique su categoría (daño, apoyo, control, invocación, movilidad).
ENTREGA: iconos nuevos, tabla de colores por categoría en docs/ y pruebas de recursos.
```

## I1 — Grimorio (limpieza visual)

**Qué pasa hoy:** la v81 quitó la ilustración de cabecera, los iconos del índice y las cajas de las pestañas. Siguen 23
ilustraciones generadas en `textures/gui/guide/*.png` ya sin uso, y las cartas del bestiario aún usan retratos, cintas y esquinas.

```
TAREA: terminar de bajar el ruido visual del grimorio (client/screen/GuideScreen + GuideUi): 1) elimina las ilustraciones
textures/gui/guide/<capítulo>.png que ya no se dibujan y su generador (tools/textures/gen_guide_headers.py) si procede; 2) en las
cartas del bestiario deja solo: retrato, nombre, 3 estadísticas, consejos y lore (quita cintas y esquinas decorativas si compiten con el
texto); 3) limita el color: texto principal crema, títulos dorados, enlaces turquesa, nada más; 4) interlineado de 11 px y márgenes de 8 px;
5) comprueba escala 1×, 2× y 3× y la resolución mínima 426×240.
REQUISITOS: sin cambiar ids de capítulo ni claves de idioma; las pruebas guide/* y logic/GuideCardLayoutTest en verde.
ENTREGA: capturas antes/después de 5 capítulos y de una carta del bestiario. VERIFICA: abre el grimorio, recorre los 23 capítulos, el
Recetario, el Bestiario, las Crónicas y el buscador.
```

## I2 — HUD

```
TAREA: revisar todo el HUD con la dirección de arte y simplificarlo: barra de habilidades (marcos 20×20), esfera de maná, bloque de
estado (barras de recurso de rol con su nombre), barra de jefes/campeones (ahora arriba y centrada), combos, grupo, sinergias,
amenaza, tutorial y piedra angular.
OBJETIVOS: que cada elemento diga qué es sin explicarlo (etiqueta o icono), que ninguno se solape con la vida, el hambre, la barra
de experiencia ni los avisos de acción, y que todos respeten la opción hudAnimations y el editor del HUD (screen/HudConfigScreen).
REQUISITOS: lógica de colocación en clases puras con JUnit (logic/AbilityHudLayout, BossHudLayout); nada de textos sin claves de
idioma. VERIFICA: 426×240, 854×480 y 1920×1080, con Eclipse activo, con 4 campeones cerca y en grupo de 4.
```

## I3 — Pantallas sin kit de arte

```
TAREA: aplicar el kit «fantasía oscura» (client/screen/KUi, RolesUi, TreeUi, tools/textures/gen_k_ui.py y gen_roles_ui.py) a las pantallas
que aún usan el dibujo antiguo: Reforja y Templado (ForgeScreen), Mejora de frascos (FlaskScreen), Tablón de contratos
(ContractBoardScreen), Grupo (PartyScreen), Configuración (ConfigScreen), Mesa del gremio (GuildDeskScreen) y Editor del HUD.
REQUISITOS: caben en 426×240 o se desplazan; botones, marcos y filetes salen del kit (no inventes piezas nuevas sin añadirlas al
generador); cada pantalla con respaldo al dibujo anterior si falta el kit; textos con fit() y claves de idioma. VERIFICA: abre cada
pantalla a escala 1×–4× y con el idioma más largo (alemán).
```

## I4 — Iconos de habilidades F2–F7

```
TAREA: producir los iconos de las 60 habilidades nuevas (roles F2–F7) siguiendo los encargos ya escritos en docs/PROMPT_ARTE_ICONOS_BERSERKER.md,
PROMPT_ARTE_ICONOS_F3.md … F7.md. Importa cada hoja con la herramienta de tools/textures y sustituye los provisionales.
REQUISITOS: 32×32 y 16×16 por icono, marco por categoría ya aplicado por NodeIconRenderer (no lo pintes), paleta del rol. VERIFICA: árbol
unificado (J), barra de habilidades (V/B/N/M) y tooltips.
```

## Q1 — Ronda de pruebas en partida (checklist)

```
TAREA (para quien prueba): sigue docs/TESTING.md desde «v70» hasta «v81» y marca cada punto como ✔ / ✘ / no probado, con captura o
log del fallo. Prioridad: 1) /aptitudes locate custodian_temple y entrar al templo hondo (3 pisos, sala de pruebas, Puerta, cámara),
2) cripta de la Niebla (3 oleadas, clímax, cofre), 3) Crónicas del grimorio (18 entradas y sus desbloqueos), 4) Niebla de almas y
Gran Osario (trofeo, Llave del Velo), 5) un rol de cada tanda F2–F7 (todas sus activas), 6) Armadura Ascendida (mesa de herrería,
aspecto por rol, bonus común), 7) armas Maestro en mano, 8) HUD con campeones, 9) Hoguera.
FORMATO DE INFORME: tabla punto | resultado | versión | pasos para reproducir | captura | log (run/logs/latest.log).
```

## Q2 — Balance con datos reales

```
TAREA: tras la ronda Q1, recoge cifras en partida (tiempo hasta nivel 50, daño por segundo de cada rol con arma Maestro, vida de jefes,
oleadas del templo y la cripta, bonus de la Armadura Ascendida, XP por hora) y compáralas con docs/BALANCE.md. Propón cambios solo con
datos (tabla antes/después) y SIN tocar valores por defecto sin permiso del autor; cada cambio con su fila en docs/CONFIGURATION.md y su
prueba en logic/*RulesTest.
```

## Q3 — Revisión nativa de traducciones

```
TAREA: revisar pt_br, fr_fr y de_de (3512+ claves) por hablantes nativos. Edita SOLO tools/lang/*_catalog.py y los generadores (nunca los JSON), regenera con
los gen_*_lang.py y comprueba con check_lang.py y los --check. Prioriza: textos de interfaz, nombres de habilidades y sendas,
descripciones de jefes y crónicas. Conserva marcadores (%s, %%, \n) y el orden.
```

## Q4 — Sonidos

```
TAREA: revisar los sonidos generados (tools/audio/gen_sounds.py, assets/aptitudes/sounds.json): volumen relativo, solapes, subtítulos y
coherencia (ataques de jefe, activas, Hoguera, Niebla, templo, Armadura Ascendida al activarse). Sustituye por muestras propias
.ogg mono 44,1 kHz solo si el autor las aporta o si son de licencia libre documentada; si no, mejora la síntesis del script.
VERIFICA: cada evento con el volumen maestro al 50 %.
```

## Q5 — Procedencia del arte de IA y créditos

```
TAREA: documentar en README.md y en una pestaña de créditos del grimorio qué arte procede de IA (hojas de armas Maestro de ChatGPT,
64×64, en tools/textures/source/weapons/), qué es generado por scripts propios y qué es de terceros (Mod Menu, GeckoLib, Fabric API).
Añade LICENSES.md con licencias y fecha. No incluyas datos personales del autor.
```
