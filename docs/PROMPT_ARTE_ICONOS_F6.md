# Prompt de arte — Iconos de las 40 habilidades nuevas de Cambiaformas, Mago pistolero, Invocador de armas y Cazador (v67, F6)

Encargo para el generador de arte (ChatGPT), igual que el de `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`. Son los iconos de los
nodos `shapeshifter_n1`…`shapeshifter_n10`, `gunslinger_n1`…, `weapon_summoner_n1`… y `hunter_n1`… del árbol unificado (J). Mientras no
lleguen, el juego dibuja un objeto vanilla de respaldo (`role/NodeIcons`).

**Densidad doble:** cada icono se dibuja a **32×32 texels** y el juego lo muestra en el hueco de 16×16 del nodo. El marco
del nodo (categoría y estado) lo pone el juego: **el icono no lleva marco**, solo el emblema sobre fondo transparente.

Conviene pedirlo **por rol** (cuatro tandas de 10) para que cada rol mantenga una paleta coherente.

Destino del arte fuente: `tools/textures/source/nodes/<rol>/` (un PNG por habilidad, con el id como nombre).

---

## Prompt para pegar en ChatGPT (una tanda por rol)

> Necesito **10 iconos de habilidad en pixel art de máxima calidad** para un mod de Minecraft de fantasía oscura. Son
> las habilidades nuevas del **{ROL}**: {DESCRIPCIÓN DEL ROL}. Quiero un acabado **profesional, trabajado píxel a
> píxel**, con materiales reconocibles, volumen con luz y sombra y contornos limpios. Nada de siluetas planas ni iconos
> genéricos: piensa en los iconos de habilidad de *Diablo II* o *Darkest Dungeon* reducidos a este tamaño.
>
> ### Formato (obligatorio)
> - Cada icono: PNG RGBA, fondo transparente, **exactamente 32×32 texels**. El juego lo muestra a la mitad (16×16): la
>   silueta tiene que leerse también a esa reducción.
> - **Detalle a 1 texel**, sin antialiasing, sin desenfoques ni degradados suaves. Transparencia solo 0 % o 100 %.
> - Máximo **32 colores** por icono. **Sin marco, sin letras, sin números.** Deja 1 texel libre en cada borde.
> - Los 10 comparten estilo, grosor de contorno y paleta base, como una familia.
>
> ### Reglas de pixel art
> 1. **Una sola luz**, arriba a la izquierda.
> 2. **Rampas con desplazamiento de tono**: sombras hacia el violeta o azul frío, luces hacia el amarillo cálido.
> 3. **Contorno selectivo**: oscuro fuera; dentro, el tono más oscuro del material.
> 4. **Un brillo especular de 1–2 texels** en el metal y el cristal; ninguno en tela, cuero ni plantas.
> 5. Silueta clara: el objeto principal ocupa al menos el 60 % del icono.
>
> ### Paleta base y lista de iconos
> (pega aquí la tabla del rol, de las de abajo)
>
> ### Entrega
> Un ZIP con los 10 PNG con **esos nombres exactos**, y una **hoja de vista previa** (los 10 en fila, ×4, sobre el
> fondo del rol).

---

## Tanda 1 — Cambiaformas · formas · fondo #251E2A

**{ROL}** = Cambiaformas; **{DESCRIPCIÓN}** = un cambiaformas que toma la forma de las criaturas que derrota.

| Rampa | Oscuro → claro |
|---|---|
| Morado bestial | #1E1236 · #3F2A78 · #6B4FC4 · #A78BFA · #E4D7FF |
| Pelaje y colmillo | #2A1B12 · #5A3A22 · #9A6A3A · #E8D9B0 |
| Sangre | #4A1010 · #8F1F1F · #E04A3A |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `shapeshifter_n1.png` | Metabolismo animal | Pasiva | **Corazón** con una hoja y un colmillo, latiendo |
| `shapeshifter_n2.png` | Sangre fría animal | Pasiva | **Gota** de sangre azulada con escarcha, sobre una escama |
| `shapeshifter_n3.png` | Mordisco salvaje | Activa | **Mandíbula** con colmillos cerrándose y gotas rojas |
| `shapeshifter_n4.png` | Cambio relámpago | Activa | **Dos siluetas** (humano y bestia) unidas por un rayo |
| `shapeshifter_n5.png` | Piel de repuesto | Pasiva | **Piel** mudada, abierta, con un brillo dorado dentro |
| `shapeshifter_n6.png` | Zarpazo múltiple | Activa | **Cuatro zarpazos** paralelos con estela |
| `shapeshifter_n7.png` | Presa fácil | Pasiva | **Diana** con una huella de zarpa en el centro |
| `shapeshifter_n8.png` | Furia de la bestia | Activa | **Cabeza de bestia** rugiendo con ojos rojos y aura de fuego |
| `shapeshifter_n9.png` | Tercer cambio | Pasiva | **Tres** formas en círculo (lobo, ave, felino) en rotación |
| `shapeshifter_n10.png` | Estampida primigenia | Activa | **Manada** de bestias corriendo en nube de polvo; la más brillante |

---

## Tanda 2 — Mago pistolero · disparos · fondo #1A202A

**{ROL}** = Mago pistolero; **{DESCRIPCIÓN}** = un pistolero que dispara balas arcanas y las explosivas.

| Rampa | Oscuro → claro |
|---|---|
| Acero y pólvora | #1B2430 · #3B4F66 · #6E8CA8 · #B4CBDD · #F0F8FF |
| Latón | #3A2A08 · #7A5A14 · #C8962E · #F7D96A |
| Arcano | #1E1236 · #6B4FC4 · #A78BFA |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `gunslinger_n1.png` | Manos firmes | Pasiva | **Mira** de pistola con una mano estable y un punto central |
| `gunslinger_n2.png` | Cabeza fría | Pasiva | **Cráneo** de hielo con una gota de maná azul |
| `gunslinger_n3.png` | Bala aturdidora | Activa | **Bala** con anillos de aturdimiento y estrellas |
| `gunslinger_n4.png` | Granada de pólvora | Activa | **Granada** de pólvora con mecha encendida |
| `gunslinger_n5.png` | Presión | Pasiva | **Bala** con una cadena que ata al objetivo |
| `gunslinger_n6.png` | Disparo a través | Activa | **Bala** atravesando tres siluetas en línea |
| `gunslinger_n7.png` | Sin remordimientos | Pasiva | **Bala** explosiva con reloj de arena roto |
| `gunslinger_n8.png` | Doble pistola | Activa | **Dos pistolas** cruzadas, ambas disparando |
| `gunslinger_n9.png` | Pulso de tirador | Pasiva | **Dedo** sobre el gatillo con una onda de pulso |
| `gunslinger_n10.png` | Ejecución | Activa | **Cráneo** con un agujero de bala y mira roja; el más intenso |

---

## Tanda 3 — Invocador de armas · espadas · fondo #1E242A

**{ROL}** = Invocador de armas; **{DESCRIPCIÓN}** = un invocador que rodea su cuerpo de espadas flotantes.

| Rampa | Oscuro → claro |
|---|---|
| Acero espectral | #1B2430 · #4A5E78 · #8AA2BE · #CFE0F0 · #FFFFFF |
| Rúnico azul | #12334A · #2C6A96 · #5FB0DA · #A9E3F5 |
| Oro viejo | #3A2A08 · #7A5A14 · #C8962E |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `weapon_summoner_n1.png` | Espadas afiladas | Pasiva | **Espada** brillante con una piedra de afilar y chispas |
| `weapon_summoner_n2.png` | Equilibrio | Pasiva | **Balanza** con una espada en cada platillo |
| `weapon_summoner_n3.png` | Espada boomerang | Activa | **Espada** con una flecha circular de ida y vuelta |
| `weapon_summoner_n4.png` | Muro de espadas | Activa | **Fila** de espadas clavadas formando una pared |
| `weapon_summoner_n5.png` | Hoja de repuesto | Pasiva | **Espada** brotando de una vaina vacía |
| `weapon_summoner_n6.png` | Danza de acero | Activa | **Espadas** girando en círculo alrededor de una figura |
| `weapon_summoner_n7.png` | Filo perfecto | Pasiva | **Filo** brillante con el número 10 grabado |
| `weapon_summoner_n8.png` | Armería viva | Activa | **Arsenal** de espadas flotantes con runas vivas |
| `weapon_summoner_n9.png` | Eco de acero | Pasiva | **Espada** con tres ecos translúcidos que regresan |
| `weapon_summoner_n10.png` | Juicio de acero | Activa | **Lluvia** de espadas cayendo sobre un círculo; la más brillante |

---

## Tanda 4 — Cazador · caza · fondo #1E2418

**{ROL}** = Cazador; **{DESCRIPCIÓN}** = un cazador que marca presas, usa trampas y tiene un halcón.

| Rampa | Oscuro → claro |
|---|---|
| Verde cazador | #0F2415 · #24502A · #4C8F3F · #96D06A |
| Cuero | #2A1B12 · #5A3A22 · #9A6A3A · #C79257 |
| Marca roja | #4A1010 · #8F1F1F · #E04A3A · #FFB0A0 |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `hunter_n1.png` | Mira certera | Pasiva | **Arco** con una mira de precisión y una flecha lejana |
| `hunter_n2.png` | Presa herida | Pasiva | **Ciervo** con una herida y gotas |
| `hunter_n3.png` | Red de caza | Activa | **Red** lanzada abierta sobre una zona |
| `hunter_n4.png` | Flecha explosiva | Activa | **Flecha** con una carga explosiva en la punta |
| `hunter_n5.png` | Trampero experto | Pasiva | **Trampa** de dientes con un reloj |
| `hunter_n6.png` | Disparo en abanico | Activa | **Cinco flechas** abiertas en abanico |
| `hunter_n7.png` | Cazador paciente | Pasiva | **Ojo** entre hojas, quieto |
| `hunter_n8.png` | Vista de halcón | Activa | **Ojo de halcón** con un destello dorado |
| `hunter_n9.png` | Botín de caza | Pasiva | **Cuerno** y orbes de experiencia |
| `hunter_n10.png` | Cacería final | Activa | **Halcón** en picado sobre varias dianas; el más brillante |

---

## Integración (cuando llegue el arte)

1. Copiar los PNG a `tools/textures/source/nodes/<rol>/`.
2. Un generador con `--check` (como `gen_role_icons.py`) los valida (32×32, RGBA, sin semitransparencias) y los copia a
   `textures/gui/node/` con el id como nombre.
3. `NodeIconRenderer` dibuja los de 32×32 a la mitad; si falta el PNG, sigue el objeto de respaldo de `NodeIcons`.
