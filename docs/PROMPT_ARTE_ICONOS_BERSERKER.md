# Prompt de arte — Iconos de las 10 habilidades nuevas del Bersérker (v67, F2)

Encargo para el generador de arte (ChatGPT). Son los iconos de los nodos `berserker_n1` … `berserker_n10` del árbol
unificado (J). Mientras no lleguen, el juego dibuja un objeto vanilla de respaldo (`role/NodeIcons`).

**Densidad doble, como las fichas de categoría:** cada icono se dibuja a **32×32 texels** y el juego lo muestra en el
hueco de 16×16 del nodo. A escala de interfaz 2 cada texel es un píxel de pantalla; a escala 4, 2×2.

El marco del nodo (categoría y estado) lo pone el juego: **el icono no lleva marco**, solo el emblema sobre fondo
transparente.

Destino del arte fuente: `tools/textures/source/nodes/berserker/` (un PNG por habilidad, con el id como nombre).

---

## Prompt para pegar en ChatGPT

> Necesito **10 iconos de habilidad en pixel art de máxima calidad** para un mod de Minecraft de fantasía oscura. Son
> las habilidades nuevas del **Bersérker**, un guerrero salvaje de hacha que se hace más peligroso cuanto más sangra.
> Quiero un acabado **profesional, trabajado píxel a píxel**, con materiales reconocibles (acero mellado, cuero, sangre,
> brasas, hueso), volumen con luz y sombra y contornos limpios. Nada de siluetas planas ni iconos genéricos: piensa en
> los iconos de habilidad de *Diablo II* o *Darkest Dungeon* reducidos a este tamaño.
>
> ### Formato (obligatorio)
> - Cada icono: PNG RGBA, fondo transparente, **exactamente 32×32 texels**. El juego lo muestra a la mitad (16×16):
>   la silueta tiene que leerse también a esa reducción.
> - **Detalle a 1 texel**, sin antialiasing, sin desenfoques ni degradados suaves. Transparencia solo 0 % o 100 %.
> - Máximo **32 colores** por icono.
> - **Sin marco, sin letras, sin números.** Deja 1 texel libre en cada borde (el emblema ocupa 30×30 como mucho).
> - Los 10 comparten estilo, grosor de contorno y paleta base, como una familia.
>
> ### Reglas de pixel art
> 1. **Una sola luz**, arriba a la izquierda.
> 2. **Rampas con desplazamiento de tono**: sombras hacia el violeta frío, luces hacia el amarillo cálido.
> 3. **Contorno selectivo**: oscuro (#1A0E10) fuera; dentro, el contorno toma el tono más oscuro del material.
> 4. **Un brillo especular de 1–2 texels** en el metal; ninguno en la tela ni en el cuero.
> 5. Silueta clara: el objeto principal ocupa al menos el 60 % del icono.
>
> ### Paleta base del Bersérker
>
> | Uso | Rampa (oscuro → claro) |
> |---|---|
> | Sangre | #3A0508 · #6E0B12 · #A3161E · #D4352B · #F3A087 |
> | Acero mellado | #23262E · #464B57 · #767D8C · #AEB4C0 · #E8ECF2 |
> | Cuero y madera | #2B1A12 · #4E3020 · #7A4B2E · #A8703F · #D39A5C |
> | Brasas y fuego | #4A1206 · #9C2B0C · #E0601A · #F5A623 · #FFE08A |
> | Hueso | #4A4236 · #7E7461 · #B5AA90 · #E2D9C0 |
> | Acento del rol | #F3A087 (salmón) sobre fondo #271A1D |
>
> ### Los 10 iconos
>
> | Archivo | Habilidad | Tipo | Qué dibujar |
> |---|---|---|---|
> | `berserker_n1.png` | Cicatrices | Pasiva | Antebrazo o torso de perfil con **tres cicatrices** cosidas en diagonal, piel curtida; una de ellas aún roja. Transmite «cuanto más me golpean, más duro soy». |
> | `berserker_n2.png` | Sangre hirviente | Pasiva | **Gota de sangre** grande que hierve: burbujas arriba y **llamas** saliendo de la parte superior. Rojo intenso con brasas naranjas. |
> | `berserker_n3.png` | Hacha arrojadiza | Activa | **Hacha de una mano girando** en diagonal, con 2–3 estelas curvas de movimiento y gotas de sangre despedidas. |
> | `berserker_n4.png` | Cadena de sangre | Activa | **Garfio de hierro** con una cadena tensa que cruza el icono, chorreando sangre; la cadena forma una curva que tira hacia abajo a la izquierda. |
> | `berserker_n5.png` | Rugido intimidante | Pasiva | **Cabeza de bestia** (lobo o jabalí) de frente, con las fauces abiertas y **ondas de sonido** en arco a los lados. |
> | `berserker_n6.png` | Tajos desgarradores | Activa | **Tres zarpazos paralelos** en diagonal, rojos y abiertos, con salpicaduras; detrás, el filo de un hacha apenas visible. |
> | `berserker_n7.png` | Juramento de sangre | Pasiva | **Mano** con la palma cortada que gotea sobre un **hacha** clavada; la sangre forma una runa simple (un trazo en «V»). Sin letras. |
> | `berserker_n8.png` | Avatar de la guerra | Activa | **Yelmo con cuernos** grande y amenazante, ojos brillando en rojo, rodeado de un **aura** de brasas. Debe parecer el más poderoso del grupo. |
> | `berserker_n9.png` | Eco de batalla | Pasiva | **Hacha** en horizontal con **dos siluetas fantasma** desplazadas detrás (eco), en tonos más fríos y transparentes por tramado (no por alfa). |
> | `berserker_n10.png` | Ragnarök | Activa | **Hacha clavada en el suelo** abriendo una **grieta de lava** que se ensancha hacia delante; esquirlas de roca saltando. Es la habilidad más fuerte: máximo contraste y brillo. |
>
> ### Entrega
> - Un ZIP con los 10 PNG con **esos nombres exactos**.
> - Además, una **hoja de vista previa** (los 10 iconos en fila, ampliados ×4, sobre fondo #271A1D) para revisarlos.

---

## Integración (cuando llegue el arte)

1. Copiar los PNG a `tools/textures/source/nodes/berserker/`.
2. Un generador con `--check` (como `gen_role_icons.py`) los valida (32×32, RGBA, sin semitransparencias) y los copia
   a `textures/gui/node/` con el id como nombre.
3. `NodeIconRenderer` dibuja los de 32×32 a la mitad; si falta el PNG, sigue el objeto de respaldo de `NodeIcons`.
