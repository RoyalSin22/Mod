# Créditos y procedencia del arte

Aptitudes se publica bajo licencia **MIT** (ver `LICENSE`). Este documento dice de dónde sale cada parte visual y de
audio, para que cualquiera pueda comprobarlo. Actualízalo cuando cambie algo.

## Código y dependencias

| Componente | Uso | Licencia |
|---|---|---|
| Minecraft 1.21.1 (Mojang) | Juego base; el mod no incluye sus recursos | EULA de Minecraft |
| Fabric Loader, Fabric API, Yarn | Carga, API y mappings | Apache-2.0 / CC0 |
| GeckoLib 4.7.7 | Jefes animados (dependencia obligatoria) | MIT |
| Mod Menu 11.0.3 | Pantalla de configuración (opcional, solo compilación) | MIT |

## Arte

| Parte | Procedencia | Dónde está la fuente |
|---|---|---|
| 20 armas Maestro (iconos) | **Generadas con IA (ChatGPT)** a partir de un encargo escrito por el autor; normalizadas por script | `tools/textures/source/weapons/` y `docs/PROMPT_ARTE_ARMAS.md` |
| Kit de interfaz (pantalla K, roles, árbol, grimorio, casillas y esfera de maná del HUD) | **Arte entregado por el autor** (el autor debe confirmar su procedencia: IA, propio o de terceros), recortado y normalizado por script | `tools/textures/source/ui/`, `source/hud/`, `source/roles/` |
| Iconos de rol, de sendas y del árbol | Generados por scripts propios a partir del arte anterior | `tools/textures/gen_role_icons.py`, `gen_node_icons.py`, `gen_tree_atlas.py` |
| Armas base, accesorios, runas, llaves, frascos, materiales y fichas | **Dibujo propio por programa** (plantillas de píxeles y paletas) | `tools/textures/gen_items.py` |
| Armadura Ascendida (iconos y capas) | **Dibujo propio por programa** | `tools/textures/gen_ascended_armor.py` |
| Bloques (Hoguera, Mesa del gremio, Tablón, Relicario, puertas…) | **Dibujo propio por programa**; la Hoguera es un modelo generado por script | `tools/textures/gen_workstations.py`, `gen_temple_blocks.py`, `tools/models/gen_bonfire_model.py` |
| Jefes y criaturas | **Modelos provisionales generados por script** (pendientes de rehacer en Blockbench) | `tools/models/gen_boss_models.py` |
| Texturas vanilla reutilizadas | Mojang (hierro, roble oscuro, ladrillos de piedra…); no se redistribuyen | — |

## Audio

Todos los sonidos propios se **sintetizan por programa** (sin muestras de terceros): `tools/audio/gen_sounds.py`.

## Textos

Español e inglés escritos por el autor. Portugués, francés y alemán: **traducción inicial automática, pendiente de
revisión por hablantes nativos** (`tools/lang/`).

## Notas

- El arte generado con IA no incluye datos personales ni marcas de terceros. Si alguna imagen se parece a una obra protegida,
  repórtalo y se sustituye.
- Antes de publicar el mod en una web (Modrinth, CurseForge…) conviene revisar las condiciones de cada plataforma sobre contenido
  generado con IA y declararlo en la descripción.
