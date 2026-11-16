# Prompt de arte — Armas de evolución Maestro (V75)

Encargo para el generador de arte (ChatGPT), en la línea de `docs/PROMPT_ARTE_ICONOS_F7.md`. Son las **20 armas Maestro**
(`<rol>_weapon_master`), una por rol. Desde la V74 el Definitivo ya no entrega arma. Hoy el juego usa un diseño provisional
dibujado por código (`tools/textures/evolved_weapons.py`, vista previa en `docs/V73_armas_evolucion_preview.png`); el arte que llegue
**lo sustituye arma a arma** y el resto sigue con el provisional.

**Tamaño final:** 16×16 píxeles (como un objeto de Minecraft). El generador no dibuja bien a 16×16, así que se pide más grande
y el script de importación lo reduce. Pide el arte **como si cada píxel de la rejilla de 16×16 fuera un bloque de 8×8**
(imagen de 128×128 o 256×256 con bloques nítidos).

Conviene pedirlo **en 4 tandas de 5 armas** (una hoja de 5 en fila por tanda) para que cada tanda comparta estilo y paleta.

---

## Prompt para pegar en ChatGPT (una tanda de 5 armas)

> Necesito **5 armas en pixel art de máxima calidad** para un mod de Minecraft de fantasía oscura, en **una sola imagen**
> con las 5 armas en **una fila**, cada una centrada en su propia celda cuadrada de igual tamaño (sin solaparse). Imagen final de
> **1280×256 px** (5 celdas de 256×256). Cada arma está dibujada como pixel art de **16×16 píxeles ampliado ×16**: bloques
> nítidos de 16×16 px en la imagen, **sin antialiasing, sin degradados suaves, sin desenfoque, sin sombras proyectadas**.
>
> ### Formato (obligatorio)
> - **Fondo liso magenta puro #FF00FF** en toda la imagen (lo quita un script). Ningún píxel del arma puede ser magenta.
> - Cada arma **en diagonal**: la **empuñadura abajo a la izquierda** y la **punta o cabeza arriba a la derecha**, como las
>   espadas de Minecraft. Ocupa casi toda la celda (al menos 12 de los 16 píxeles de largo).
> - **Máximo 16 colores por arma.** Sin letras, números, marcos ni texto. Sin mano ni personaje.
> - Las 5 comparten estilo, grosor de contorno y nivel de detalle, como una familia.
>
> ### Reglas de pixel art
> 1. **Una sola luz**, arriba a la izquierda.
> 2. **Contorno oscuro de 1 píxel** por fuera; por dentro, el tono más oscuro de cada material.
> 3. **Rampas con desplazamiento de tono**: sombras hacia el violeta o azul frío, luces hacia el amarillo cálido.
> 4. **Un brillo especular de 1–2 píxeles** en el metal y en las gemas; ninguno en madera, cuero ni tela.
> 5. Materiales reconocibles (acero, oro, madera con veta, cuero, hueso, cristal), con volumen. Nada de siluetas planas.
> 6. Cada arma debe **reconocerse por su silueta** a 16×16 y distinguirse claramente de las otras cuatro.
> 7. Acento de color del rol en la gema, la cinta o el filo (el color de cada arma está en la lista).
>
> ### Las 5 armas de esta tanda, de izquierda a derecha
> (pega aquí las 5 filas de la tanda de la tabla de abajo)
>
> ### Entrega
> Una única imagen PNG con las 5 armas en el orden indicado. Si alguna no sale bien, regenérala sola con el mismo estilo.

---

## Tandas y lista de armas

El **orden de izquierda a derecha** es el de cada tanda. El nombre del rol es el que usará el script.

### Tanda 1 — cuerpo a cuerpo pesado
| # | Rol (id) | Color | Concepto |
|---|---|---|---|
| 1 | Bersérker (`berserker`) | `#c0392b` | Hacha de guerra de una pieza: hoja de acero con filo exterior curvo y barba, mango de madera oscura envuelto en cuero, remache y runa rojos. |
| 2 | Paladín (`paladin`) | `#f1c40f` | Maza de guerra con cabeza cuadrada de acero pulido, cruz dorada con gema en el centro y mango de madera con pomo dorado. |
| 3 | Lancero (`lancer`) | `#2980b9` | Lanza larga: punta de acero en forma de hoja, guarda dorada con gema y cinta azul ondeando bajo la punta. |
| 4 | Espadachín (`swordsman`) | `#bdc3c7` | Espada larga recta de acero brillante, guarda en cruz dorada con gema, empuñadura de cuero y pomo. |
| 5 | Monje (`monk`) | `#e67e22` | Bastón largo de madera con aros dorados, cuentas de oración y un contrapeso naranja en el extremo inferior. |

### Tanda 2 — cuerpo a cuerpo ágil y de caza
| # | Rol (id) | Color | Concepto |
|---|---|---|---|
| 1 | Asesino (`assassin`) | `#4b6584` | Daga curva tipo kris de acero oscuro, guarda doble, empuñadura azul grisácea y un reflejo largo en la hoja. |
| 2 | Cazador (`hunter`) | `#7cb342` | Cuchillo de caza grande: hoja ancha con el lomo serrado, mango de hueso y cuero, detalle verde oliva. |
| 3 | Cambiaformas (`shapeshifter`) | `#9b59b6` | Garra: tres cuchillas curvas de hueso y acero en haz, empuñadura de cuero y una gema morada. |
| 4 | Mercader táctico (`merchant`) | `#16a085` | Sable curvo de mercader con pomo en forma de moneda dorada y una gema turquesa en la guarda. |
| 5 | Domador (`tamer`) | `#a1887f` | Maza-tótem con cabeza de bestia: colmillos y cuernos de hueso sobre cuero, gema ámbar en la frente. |

### Tanda 3 — magia y naturaleza
| # | Rol (id) | Color | Concepto |
|---|---|---|---|
| 1 | Mago (`mage`) | `#8e44ad` | Báculo de madera con un cristal violeta grande engarzado en garras doradas, con un halo suave. |
| 2 | Nigromante (`necromancer`) | `#6c3483` | Guadaña-báculo oscura: hoja de acero curva, calavera de hueso y una gema violeta, vara negra. |
| 3 | Druida (`druid`) | `#6e8b3d` | Rama viva retorcida con hojas; una horquilla sostiene una gema verde dorada. |
| 4 | Herbolista (`herbalist`) | `#27ae60` | Hoz de acero de hoja curva, mango de madera con hojas y lianas verdes y una gema verde. |
| 5 | Elementalista (`elementalist`) | `#3498db` | Báculo con un orbe de cristal azul, rodeado de cuatro pequeños fragmentos de colores (fuego, agua, tierra, aire). |

### Tanda 4 — distancia, apoyo y arcanos
| # | Rol (id) | Color | Concepto |
|---|---|---|---|
| 1 | Mago pistolero (`gunslinger`) | `#00bcd4` | Revólver arcano de cañón largo y tambor, empuñadura de madera, una gema cian en el cañón (orientado con el cañón arriba a la derecha). |
| 2 | Ingeniero (`engineer`) | `#d35400` | Llave inglesa gigante de acero cuya cabeza es un pequeño cañón con engranaje; cuerpo naranja y cables. |
| 3 | Invocador de armas (`weapon_summoner`) | `#90a4ae` | Empuñadura con tres espadas pequeñas y etéreas flotando en abanico, azul acero con brillo. |
| 4 | Bardo (`bard`) | `#ec407a` | Laúd de cuerpo redondo rosa con boca oscura, mástil largo con clavijas doradas y cuerdas (mástil hacia arriba a la derecha). |
| 5 | Brujo abisal (`warlock`) | `#ad1457` | Báculo oscuro rematado por un grimorio maldito de cubierta negra y esquinas de metal, con un ojo carmesí brillante. |

---

## Qué hacer con lo que entregue ChatGPT

1. Guarda cada hoja en una carpeta fuera del repositorio, p. ej. `entregas/tanda1.png`.
2. Impórtala (los roles se leen de izquierda a derecha en el orden de la tanda):

```bash
python3 tools/textures/import_weapon_art.py entregas/tanda1.png --sheet 5x1 --roles berserker,paladin,lancer,swordsman,monk
python3 tools/textures/import_weapon_art.py entregas/tanda2.png --sheet 5x1 --roles assassin,hunter,shapeshifter,merchant,tamer
python3 tools/textures/import_weapon_art.py entregas/tanda3.png --sheet 5x1 --roles mage,necromancer,druid,herbalist,elementalist
python3 tools/textures/import_weapon_art.py entregas/tanda4.png --sheet 5x1 --roles gunslinger,engineer,weapon_summoner,bard,warlock
```

   Si el generador devolvió armas sueltas, llámalas `<rol>.png` y pásalas directamente
   (`import_weapon_art.py entregas/*.png`). Si una sale al revés usa `--flip` (o `--rotate 90`); si el fondo no se quita bien,
   sube `--tolerance`; si quedan demasiados colores, baja `--colors`. Con `--dry-run` solo se valida.
3. Revisa el resultado: `python3 tools/textures/import_weapon_art.py --preview vista.png` y
   `python3 tools/textures/import_weapon_art.py --check` (tamaño, solo 0 % o 100 % de opacidad, ≤ 24 colores, largo mínimo y
   orientación).
4. Regenera texturas y modelos 3D de la mano (los modelos se extruyen del propio PNG; lo oscuro tiene más grosor):

```bash
python3 tools/textures/gen_items.py src/main/resources/assets/aptitudes/textures/item
python3 tools/models/gen_weapon_models.py src/main/resources/assets/aptitudes/models/item
```

   (`gen_items.py` regenera además los tres sellos retirados: borra `ancient_automaton_seal.png`, `dismantled_guardian_seal.png` y
   `faceless_colossus_seal.png` de `textures/item/` después.)
5. Un arma con arte externo se guarda en `tools/textures/source/weapons/<rol>_weapon_master.png`. Para volver al diseño
   provisional de una, borra su archivo de esa carpeta y regenera.

## Criterios de aceptación

- Se distingue cada arma por su silueta a 16×16, sin leer el nombre.
- Mismo estilo y paleta dentro de la familia; el color del rol se nota sin ser el único recurso.
- Diagonal correcta (empuñadura abajo a la izquierda, punta arriba a la derecha) y sin píxeles semitransparentes.
- En la mano (modelo 3D) se ve con volumen y sin huecos raros: revisarlo en partida con
  `/give @s aptitudes:<rol>_weapon_master`.
