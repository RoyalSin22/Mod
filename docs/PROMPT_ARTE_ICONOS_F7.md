# Prompt de arte — Iconos de las 40 habilidades nuevas de Bardo, Brujo abisal y Domador (v67, F7)

Encargo para el generador de arte (ChatGPT), igual que el de `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`. Son los iconos de los
nodos `bard_n1`…`bard_n10`, `warlock_n1`… y `tamer_n1`… del árbol unificado (J). Mientras no
lleguen, el juego dibuja un objeto vanilla de respaldo (`role/NodeIcons`).

**Densidad doble:** cada icono se dibuja a **32×32 texels** y el juego lo muestra en el hueco de 16×16 del nodo. El marco
del nodo (categoría y estado) lo pone el juego: **el icono no lleva marco**, solo el emblema sobre fondo transparente.

Conviene pedirlo **por rol** (tres tandas de 10) para que cada rol mantenga una paleta coherente.

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

## Tanda 1 — Bardo · música · fondo #2A1E2A

**{ROL}** = Bardo; **{DESCRIPCIÓN}** = un bardo que apoya al grupo con canciones y notas.

| Rampa | Oscuro → claro |
|---|---|
| Rosa musical | #3A1230 · #7A2A62 · #C85A9A · #F48FB1 · #FFE0EE |
| Oro | #3A2A08 · #7A5A14 · #C8962E · #F7D96A |
| Blanco sonoro | #B8C8E0 · #E6EEFA · #FFFFFF |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `bard_n1.png` | Armonía de grupo | Pasiva | **Tres notas** en arco sobre un escudo de luz |
| `bard_n2.png` | Estribillo pegadizo | Pasiva | **Partitura** con una nota repetida y una flecha de bucle |
| `bard_n3.png` | Nota cortante | Activa | **Nota musical** afilada lanzando una onda en línea |
| `bard_n4.png` | Arrullo | Activa | **Luna** con una «z» y una cuna de notas |
| `bard_n5.png` | Coro | Pasiva | **Tres siluetas** cantando con notas de colores |
| `bard_n6.png` | Estruendo | Activa | **Tambor** con ondas de choque concéntricas |
| `bard_n7.png` | Pulso del compás | Pasiva | **Metrónomo** con un corazón en el péndulo |
| `bard_n8.png` | Marcha de victoria | Activa | **Estandarte** y trompeta con notas doradas |
| `bard_n9.png` | Contrapunto | Pasiva | **Dos pentagramas** cruzados formando una trenza |
| `bard_n10.png` | Gran final | Activa | **Lira** estallando en notas y luz; la más brillante |

---

## Tanda 2 — Brujo abisal · abismo · fondo #1A1228

**{ROL}** = Brujo abisal; **{DESCRIPCIÓN}** = un brujo que maldice y usa su propia sangre.

| Rampa | Oscuro → claro |
|---|---|
| Violeta abisal | #150A26 · #3B1D6B · #6A3FB0 · #A98BE8 |
| Sangre | #3A0A0A · #7A1414 · #C02A2A · #FF6A5A |
| Alma verde | #0E2A1A · #1F6A3C · #4EC070 · #B8F5C8 |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `warlock_n1.png` | Sed de maldiciones | Pasiva | **Sello** de maldición con una gota roja que lo alimenta |
| `warlock_n2.png` | Sacrificio de sangre | Pasiva | **Cáliz** de sangre con una gota de maná azul |
| `warlock_n3.png` | Mal de ojo | Activa | **Ojo** violeta con pupila hendida y una grieta |
| `warlock_n4.png` | Cadenas de sombra | Activa | **Cadenas** de sombra emergiendo del suelo |
| `warlock_n5.png` | Ansia de alma | Pasiva | **Alma** verde atraída hacia una boca abierta |
| `warlock_n6.png` | Nube de pestilencia | Activa | **Nube** verdosa con una calavera difusa |
| `warlock_n7.png` | Desesperación | Pasiva | **Corazón** casi vacío con una llama negra |
| `warlock_n8.png` | Forma demoníaca | Activa | **Máscara** demoníaca con cuernos y ojos ardientes |
| `warlock_n9.png` | Cosecha oscura | Pasiva | **Guadaña** cosechando almas en espiral |
| `warlock_n10.png` | Fosa abisal | Activa | **Abismo** circular con zarcillos de sombra; el más intenso |

---

## Tanda 3 — Domador · bestias · fondo #1E2418

**{ROL}** = Domador; **{DESCRIPCIÓN}** = un domador que lucha junto a sus mascotas.

| Rampa | Oscuro → claro |
|---|---|
| Pelaje | #2A1B12 · #5A3A22 · #9A6A3A · #E8D9B0 |
| Verde salvaje | #0F2415 · #24502A · #4C8F3F · #96D06A |
| Rojo de furia | #4A1010 · #8F1F1F · #E04A3A |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `tamer_n1.png` | Pelaje denso | Pasiva | **Manto** de pelaje espeso con un destello protector |
| `tamer_n2.png` | Frenesí de caza | Pasiva | **Huella** de lobo con líneas de velocidad |
| `tamer_n3.png` | Convocar a la manada | Activa | **Cuerno** de caza con siluetas de animales acudiendo |
| `tamer_n4.png` | Zarpazos coordinados | Activa | **Tres zarpazos** convergiendo en un mismo punto |
| `tamer_n5.png` | Cuidado veterano | Pasiva | **Vendaje** sobre una pata con una cruz verde |
| `tamer_n6.png` | Manto protector | Activa | **Escudo** de hojas envolviendo a un animal |
| `tamer_n7.png` | Olfato de la manada | Pasiva | **Hocico** olfateando con ondas de olor |
| `tamer_n8.png` | Furia animal | Activa | **Cabeza** de lobo rugiendo con aura roja |
| `tamer_n9.png` | Vínculo de sangre | Pasiva | **Dos manos** unidas por un hilo de sangre y un corazón |
| `tamer_n10.png` | Llamada del rey | Activa | **Corona** sobre una cabeza de bestia con rayos; la más brillante |

---

## Integración (cuando llegue el arte)

1. Copiar los PNG a `tools/textures/source/nodes/<rol>/`.
2. Un generador con `--check` (como `gen_role_icons.py`) los valida (32×32, RGBA, sin semitransparencias) y los copia a
   `textures/gui/node/` con el id como nombre.
3. `NodeIconRenderer` dibuja los de 32×32 a la mitad; si falta el PNG, sigue el objeto de respaldo de `NodeIcons`.
