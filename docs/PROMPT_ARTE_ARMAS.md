# Prompt de arte — Armas de evolución Maestro (V75, versión 2)

Encargo para el generador de arte (ChatGPT), en la línea de `docs/PROMPT_ARTE_ICONOS_F7.md`. Son las **20 armas Maestro**
(`<rol>_weapon_master`), una por rol. Desde la V74 el Definitivo ya no entrega arma. El juego usa hoy un diseño provisional
dibujado por código (`docs/V73_armas_evolucion_preview.png`): es deliberadamente simple. El arte que llegue **lo sustituye arma a
arma** y el resto sigue con el provisional.

**Resolución final: 64×64 píxeles** (cuatro veces más detalle por lado que un objeto de vanilla). En el modelo 3D de la mano, en el
suelo y en los marcos se ve a plena resolución; en el inventario y el HUD el script genera una versión reducida a 32×32
(`<arma>.png`) para que no «vibre», y la de 64×64 queda como `<arma>_hd.png` para el modelo 3D. El generador de imágenes no dibuja
bien a 64×64 directamente: se le pide **como una rejilla de 64×64 ampliada ×4** (celdas de 256×256 px con bloques nítidos de 4×4
px) y el script la reduce. Si hiciera falta menos detalle, se importa con `--size 32` o `--size 16` (aspecto vanilla).

Conviene pedirlo **en 4 tandas de 5 armas** (una hoja de 5 en fila por tanda) para que cada tanda comparta estilo y paleta. Si una
hoja sale floja, es mejor pedir **cada arma por separado** repitiendo el bloque de estilo.

---

## 1. Dirección artística (léela entera: es lo que separa un icono genérico de uno del mod)

**El mundo.** Aptitudes es fantasía oscura y melancólica: un mundo cuyo Velo se ha roto, donde las almas vagan en la Niebla, las
Hogueras mantienen viva la última luz y un Custodio de piedra guarda un juramento que nadie recuerda. Piensa en la sensación de
los objetos de *Dark Souls*, *Elden Ring* y *Bloodborne*: armas **viejas, usadas, pesadas, con historia**, hechas por manos que ya
no existen. No son juguetes ni espadas de videojuego brillantes.

**Tono: estoico y dinámico a la vez.**
- *Estoico:* masas sólidas y graves, siluetas rotundas, paleta contenida y desaturada (acero frío, oro deslucido, hueso, ceniza,
  cuero oscuro, sangre seca, madera quemada). Nada de colores de caramelo ni de purpurina.
- *Dinámico:* la diagonal de la empuñadura a la punta, una asimetría intencionada (una muesca, una cinta rasgada, una cadena que
  cuelga), pequeñas chispas, ascuas o jirones de energía que acompañan al arma, y **un único acento de luz** que parece
  *respirar* (la gema, la runa o el filo encendido) en el color del rol.

**Cada arma tiene que sentirse usada:** filos mellados, óxido o pátina en las juntas, cuero cosido con costuras visibles, tela
deshilachada, runas grabadas a medio borrar, restos de sangre seca o ceniza en la hoja. Y **una sola cosa especial** que cuente su
historia (el ojo del cráneo, la moneda del pomo, la cuerda rota del arpa…).

**Que no parezca:** un ícono de móvil, un arma plástica reluciente, un diseño simétrico y limpio, un degradado suave, un dibujo
vectorial reescalado.

## 2. Técnica de pixel art (obligatoria)

1. **Una sola luz, arriba a la izquierda.** Borde iluminado de 1 píxel en los cantos superiores izquierdos de cada pieza.
2. **Rampas de 4–5 tonos por material** con *desplazamiento de tono*: las sombras se enfrían hacia el azul o violeta, las luces se
   calientan hacia el amarillo o el naranja pálido. Nunca oscurecer solo con negro.
3. **Contorno selectivo (sel-out):** por fuera, un tono muy oscuro y teñido del color del material (no negro puro); por dentro,
   el contorno se funde con el tono más oscuro de la pieza. En las zonas iluminadas el contorno se aclara.
4. **Agrupa los píxeles en grupos limpios.** Líneas con escalón regular (1:1, 1:2), sin «dientes» ni píxeles sueltos (*jaggies*,
   *orphan pixels*), sin tramas de ruido. Se permite **tramado ligero** de 2 tonos solo en superficies grandes de cuero, tela o
   piedra, y con moderación.
5. **Metal:** acero con picaduras (2–3 píxeles más oscuros), un brillo especular de 1–2 píxeles en la arista y un reflejo del
   color del acento. Ranuras de sangre (*fullers*) en las hojas. **Oro:** deslucido, con tono ocre y un único punto de luz.
6. **Madera y cuero:** veta con líneas de 1 píxel, nudos, remaches y costuras; **tela:** pliegues con 3 tonos y bordes rasgados.
7. **Gemas, runas y filos encendidos:** 3 tonos del color del rol + **un píxel de núcleo casi blanco**. La luz que emiten tiñe
   con 1–2 píxeles los materiales vecinos (luz de rebote). **No uses transparencia ni halo difuminado**: el brillo se dibuja con
   píxeles opacos.
8. **Textura y detalle:** al menos 10–14 detalles pequeños por arma (remaches, runas de 2×2, eslabones, muescas, hilos, cabezas de
   clavo…), siempre subordinados a la silueta: **la silueta se tiene que reconocer incluso rellena de un solo color.**

## 3. Prompt para pegar en ChatGPT (una tanda de 5 armas)

> Necesito **5 armas en pixel art de máxima calidad** para un mod de Minecraft de **fantasía oscura**, al estilo de los objetos de
> *Dark Souls* / *Elden Ring*: armas viejas, usadas, pesadas, con historia; tono **estoico y dinámico**, paleta contenida y
> desaturada con **un único acento de luz** por arma. Quiero un acabado **profesional, trabajado píxel a píxel**, como el de un
> pixel artist veterano: nada de siluetas planas, iconos genéricos, plástico brillante ni dibujo vectorial reescalado.
>
> ### Formato (obligatorio)
> - **Una sola imagen PNG de 1280×256 px** con las 5 armas **en una fila**, cada una centrada en su propia celda cuadrada de
>   256×256 px, sin solaparse.
> - Cada arma es un **pixel art de 64×64 píxeles ampliado ×4**: bloques nítidos de 4×4 px, **sin antialiasing, sin degradados
>   suaves, sin desenfoque, sin sombras proyectadas, sin transparencias parciales**.
> - **Fondo liso magenta puro #FF00FF** en toda la imagen (un script lo elimina). Ningún píxel del arma puede ser magenta.
> - Cada arma **en diagonal**: **empuñadura abajo a la izquierda** y **punta o cabeza arriba a la derecha**. Ocupa casi toda la
>   celda (al menos 54 de los 64 píxeles de largo). Un margen de 1 píxel.
> - **Máximo 48 colores por arma.** Sin letras, números, marcos ni texto. Sin manos ni personajes.
> - Las 5 comparten estilo, grosor de contorno, nivel de detalle y calidad de luz, como una familia coherente.
>
> ### Técnica
> 1. **Una sola luz arriba a la izquierda**, con un borde iluminado de 1 píxel en los cantos superiores.
> 2. **Rampas de 4–5 tonos por material con desplazamiento de tono**: sombras frías (azul/violeta), luces cálidas (amarillo
>    pálido). Nunca oscurecer solo con negro.
> 3. **Contorno selectivo**: tono muy oscuro teñido del material por fuera; por dentro se funde con el tono más oscuro. Sin negro
>    puro, sin píxeles sueltos, líneas con escalón regular.
> 4. **Metal** con picaduras, brillo especular de 1–2 px y ranuras de sangre; **oro deslucido**; **madera** con veta y nudos;
>    **cuero** con costuras y remaches; **tela** rasgada con 3 tonos; **hueso** amarillento con grietas.
> 5. **Gemas, runas y filos encendidos**: 3 tonos del color del rol y **un píxel de núcleo casi blanco**; su luz tiñe 1–2 píxeles
>    de los materiales vecinos. Nada de halos difuminados.
> 6. **Desgaste y carácter**: filos mellados, pátina en las juntas, tela deshilachada, runas medio borradas, ceniza o sangre seca.
>    Entre 10 y 14 detalles pequeños por arma y **una cosa especial** que cuente su historia.
> 7. **La silueta debe reconocerse incluso rellena de un solo color** y distinguirse con claridad de las otras cuatro.
> 8. Estoico y dinámico: masas graves y sólidas, pero con la diagonal, una asimetría intencionada y pequeñas chispas o ascuas.
>
> ### Las 5 armas de esta tanda, de izquierda a derecha
> (pega aquí las 5 fichas de la tanda, de la sección 4)
>
> ### Entrega
> Una única imagen PNG con las 5 armas en el orden indicado. Si alguna no cumple, regenérala **sola** manteniendo el mismo estilo.

## 4. Fichas de las 20 armas (nombre oficial del juego, silueta, materiales, acento y carácter)

El **orden de izquierda a derecha** es el de cada tanda. El id es el que usa el script de importación.

### Tanda 1 — cuerpo a cuerpo pesado
| # | Arma (id) | Acento | Ficha |
|---|---|---|---|
| 1 | **Hacha del Señor de la Guerra** (`berserker`) | rojo `#c0392b` | Hacha de guerra a dos manos de una cabeza: hoja ancha de acero ennegrecido con filo exterior en media luna, barba larga que baja hacia el mango y espolón trasero. Mango de madera oscura envuelto en tiras de cuero atadas con remaches; en el pomo, un gancho. Filo mellado y manchado de sangre seca; una runa roja grabada en la hoja que late como ascua. Carácter: brutal y cansada, arrastra tiras de cuero rasgado. |
| 2 | **Martillo del Cruzado** (`paladin`) | oro `#f1c40f` | Martillo de guerra sagrado: cabeza maciza de acero con una cruz grabada en relieve, un lado plano y otro con espiga; engarce dorado deslucido con una gema ámbar en el centro de la cruz. Mango largo de madera con refuerzos metálicos y pomo con anilla; una cadena corta con medallón cuelga del extremo. Carácter: solemne, abollado por mil batallas, con un rastro de luz dorada en la cruz. |
| 3 | **Lanza del Lancero Real** (`lancer`) | azul `#2980b9` | Lanza larga de torneo: punta de acero en hoja de laurel con ranura central, collar de oro deslucido con gema azul y una banderola de tela azul oscuro, rasgada, ondeando bajo la punta. Asta de madera clara con empuñadura de cuero y contrapeso en la base. Carácter: noble y dinámica, la tela sigue la diagonal como si hubiera viento. |
| 4 | **Katana del Maestro de la Espada** (`swordsman`) | plata `#bdc3c7` | Katana de hoja ligeramente curva, línea de temple (hamon) visible como una onda más clara a lo largo del filo, filo de una sola arista con brillo especular. Guarda (tsuba) circular calada de hierro oscuro con un rastro de oro; empuñadura envuelta en cordón negro en rombos y pomo de hierro. Una cinta de seda roja deshilachada. Carácter: serena y letal, la más limpia de la tanda. |
| 5 | **Bastón del Gran Maestro** (`monk`) | naranja `#e67e22` | Bastón largo de madera de bambú o roble con nudos marcados, tres anillos de oro viejo con inscripciones y cuentas de oración colgando de un cordón; contrapesos de hierro en ambos extremos, uno con un fulgor naranja como brasa. Cinta de tela naranja anudada a media vara. Carácter: austero y disciplinado, con una ascua de energía en la punta superior. |

### Tanda 2 — cuerpo a cuerpo ágil y de caza
| # | Arma (id) | Acento | Ficha |
|---|---|---|---|
| 1 | **Daga del Maestro de las Sombras** (`assassin`) | azul gris `#4b6584` | Daga curva tipo kris con hoja ondulada de acero oscuro (casi negro) con un único reflejo largo y frío; guarda en dos garras; empuñadura de cuero negro con hilo azul grisáceo; pomo con una gema azul apagada. Una cinta oscura la rodea. Carácter: sigilosa y letal, silueta delgada, una gota de veneno verdoso en la punta. |
| 2 | **Machete del Maestro Cazador** (`hunter`) | verde oliva `#7cb342` | Machete grande de caza: hoja ancha con el lomo serrado y la punta cortada en diagonal, acero gris con óxido. Mango de hueso de ciervo y cuero con cuerda, guarda corta de latón, una pluma y un colmillo atados al pomo. Carácter: rústico, de supervivencia, con muescas y manchas de campo. |
| 3 | **Garras del Metamorfo** (`shapeshifter`) | violeta `#9b59b6` | Tres cuchillas curvas de hueso y acero en abanico, fijadas a una empuñadura de cuero con correas; vetas violetas en el hueso como si algo respirara dentro; una gema violeta en la muñequera. Carácter: salvaje e inestable, ligeramente deformada, con un destello violeta que recorre las cuchillas. |
| 4 | **Estoque del Magnate** (`merchant`) | turquesa `#16a085` | Estoque (espada de esgrima) de hoja larga y fina con ranura central; guarda de copa ornamentada en oro deslucido con filigrana, una moneda de oro como pomo y una gema turquesa engarzada en la copa. Empuñadura de cuero fino. Carácter: elegante y calculadora, con un pequeño brillo de monedas, la más refinada de la tanda. |
| 5 | **Látigo del Señor de las Bestias** (`tamer`) | ámbar `#a1887f` | Látigo largo de cuero trenzado que sale de un mango de madera y hueso con anilla de hierro; la cola serpentea en una curva en S hacia la esquina superior derecha, terminando en una punta de hueso afilada con un colmillo. Remaches y una cadena fina; una gema ámbar en el pomo. Carácter: **dinámico por excelencia**, la curva del látigo da movimiento a toda la imagen. |

### Tanda 3 — magia y naturaleza
| # | Arma (id) | Acento | Ficha |
|---|---|---|---|
| 1 | **Vara del Archimago** (`mage`) | violeta `#8e44ad` | Vara larga de madera oscura y nudosa, rematada por garras de oro viejo que sostienen un gran cristal violeta tallado en facetas, con un núcleo casi blanco; un anillo de runas flota tenue alrededor (dibujado con píxeles opacos sueltos). Tela violeta atada bajo el cristal. Carácter: arcana y solemne, el cristal ilumina la madera próxima. |
| 2 | **Cetro del Noble de Sangre** (`necromancer`) | violeta oscuro `#6c3483` | Cetro de vara negra y fina rematado por un cáliz de hueso y metal oscuro que contiene una gota carmesí-violeta, con pequeñas gárgolas o espinas alrededor; un colgante de cadena y un rubí oscuro. Detalles de venas rojas en la vara. Carácter: aristocrático y macabro, una gota de sangre a punto de caer. |
| 3 | **Báculo del Archidruida** (`druid`) | verde dorado `#6e8b3d` | Báculo de una rama viva retorcida, corteza rugosa con musgo, que termina en una horquilla de raíces que sostiene una gema verde dorada; hojas jóvenes y bayas, una enredadera que baja en espiral. Carácter: antiguo y vivo, con motas de luz verde que flotan (píxeles opacos sueltos). |
| 4 | **Hoz del Alquimista Verde** (`herbalist`) | verde `#27ae60` | Hoz de hoja curva de acero con pátina verde, mango de madera con hojas, lianas y un frasquito de cristal verde atado; una gema esmeralda en el engarce. Pétalos secos adheridos a la hoja. Carácter: de cosecha y de laboratorio, con un hilo de vapor verde. |
| 5 | **Orbe del Archimago Elemental** (`elementalist`) | azul `#3498db` | Orbe de cristal azul profundo sostenido por un armazón de metal oscuro con garras, y cuatro fragmentos de cristal menores orbitando alrededor (fuego rojizo, agua azul claro, tierra ocre, aire blanco), uno por cuadrante. Mango corto de hueso y metal. Carácter: contenido pero cargado, el orbe parece tener corrientes dentro. |

### Tanda 4 — distancia, apoyo y arcanos
| # | Arma (id) | Acento | Ficha |
|---|---|---|---|
| 1 | **Revólver del Pistolero Rúnico** (`gunslinger`) | cian `#00bcd4` | Revólver de cañón largo apuntando hacia la esquina superior derecha, tambor con runas cian que brillan en las cámaras, culata de madera oscura con una pequeña placa, mira y gatillo de latón. Una gema cian en el cañón. Carácter: hecho a mano, gastado, un hilo de energía cian en el cañón. |
| 2 | **Cañón del Maestro Ingeniero** (`engineer`) | naranja `#d35400` | Cañón de mano industrial: boca ancha de acero con remaches y un engranaje lateral, depósito de cobre con un tubo y cables, mango de cuero, una válvula con luz naranja. Manchas de grasa y hollín. Carácter: pesada y ruidosa, con una chispa o una pequeña llama naranja en la boca. |
| 3 | **Hoja del Señor de las Hojas** (`weapon_summoner`) | gris azulado `#90a4ae` | Una empuñadura de espada sin hoja, de cuero y acero, de la que emergen y flotan **tres espadas pequeñas etéreas** en abanico, de acero azulado con un brillo espectral. Fragmentos de acero girando alrededor. Carácter: un arsenal que obedece, la silueta es el abanico de hojas. |
| 4 | **Arpa del Maestro de Capilla** (`bard`) | rosa `#ec407a` | Arpa pequeña de mano de madera oscura y marfil, con un marco curvo en forma de hombro, cuerdas finas y una rota que cuelga; filigrana de oro viejo y una gema rosa en la columna. Cintas rosas rasgadas. Carácter: melancólica y elegante, una nota hecha de luz rosa sale de las cuerdas (píxeles opacos). |
| 5 | **Cráneo del Archibrujo** (`warlock`) | carmesí `#ad1457` | Un cráneo humano amarillento y agrietado montado sobre un bastón oscuro (cráneo arriba a la derecha), con un cuerno, una corona de hierro y **una luz carmesí intensa dentro de las cuencas**. Cadenas finas y un talismán maldito colgando. Carácter: el más siniestro, la luz carmesí tiñe el hueso. |

---

## 5. Qué hacer con lo que entregue ChatGPT

1. Guarda cada hoja fuera del repositorio, p. ej. `entregas/tanda1.png`.
2. Impórtala (los roles se leen de izquierda a derecha en el orden de la tanda; por defecto sale a **64×64**):

```bash
python3 tools/textures/import_weapon_art.py entregas/tanda1.png --sheet 5x1 --roles berserker,paladin,lancer,swordsman,monk
python3 tools/textures/import_weapon_art.py entregas/tanda2.png --sheet 5x1 --roles assassin,hunter,shapeshifter,merchant,tamer
python3 tools/textures/import_weapon_art.py entregas/tanda3.png --sheet 5x1 --roles mage,necromancer,druid,herbalist,elementalist
python3 tools/textures/import_weapon_art.py entregas/tanda4.png --sheet 5x1 --roles gunslinger,engineer,weapon_summoner,bard,warlock
```

   Si el generador devolvió armas sueltas, llámalas `<rol>.png` y pásalas directamente
   (`import_weapon_art.py entregas/*.png`). Si una sale al revés usa `--flip` (o `--rotate 90`); si el fondo no se quita bien,
   sube `--tolerance`; si quedan demasiados colores, baja `--colors`; para menos detalle usa `--size 32` o `--size 16`. Con
   `--dry-run` solo se valida.
3. Revisa el resultado: `python3 tools/textures/import_weapon_art.py --preview vista.png` y
   `python3 tools/textures/import_weapon_art.py --check` (tamaño, solo 0 % o 100 % de opacidad, ≤ 56 colores, largo mínimo y
   orientación).
4. Regenera texturas y modelos 3D de la mano (los modelos se extruyen del propio PNG, con bloques de ¼ de píxel; lo oscuro tiene
   más grosor). Se crean `<arma>.png` (32×32, para el inventario) y `<arma>_hd.png` (64×64, para el modelo 3D):

```bash
python3 tools/textures/gen_items.py src/main/resources/assets/aptitudes/textures/item
python3 tools/models/gen_weapon_models.py src/main/resources/assets/aptitudes/models/item
```

   (`gen_items.py` regenera además los tres sellos retirados: borra `ancient_automaton_seal.png`, `dismantled_guardian_seal.png` y
   `faceless_colossus_seal.png` de `textures/item/` después.)
5. Un arma con arte externo se guarda en `tools/textures/source/weapons/<rol>_weapon_master.png`. Para volver al diseño
   provisional de una, borra su archivo de esa carpeta y regenera.

## 6. Criterios de aceptación

- **Reconocible:** se distingue cada arma por su silueta rellena de un solo color, sin leer el nombre.
- **Coherente:** misma familia de estilo y luz dentro de la tanda; el acento de color del rol se nota sin ser el único recurso.
- **Dark fantasy:** se siente usada, vieja y pesada; no hay brillos plásticos ni colores saturados fuera del acento.
- **Dinámica y estoica:** diagonal clara, una asimetría intencionada y un único punto de luz que llama la atención.
- **Técnica:** diagonal correcta, sin píxeles semitransparentes ni sueltos, ≤ 48 colores, bordes nítidos.
- **En la mano:** el modelo 3D se ve con volumen y sin huecos raros; revisarlo en partida con
  `/give @s aptitudes:<rol>_weapon_master`.
