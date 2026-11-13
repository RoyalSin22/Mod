# Prompt de arte — Fichas de categoría con emblema, versión detallada (pantalla de roles, J)

Encargo para el generador de arte (ChatGPT). Sustituye a las 10 fichas `categoria_<categoría>_<estado>.png`.

**Historial:**

- 1.ª versión (barra de color): no identificaba la categoría.
- 2.ª versión (emblema con rejilla de 2×2): el resultado fue demasiado simple. Con píxel lógico de 2×2 en 24×16 px, el
  emblema solo tenía 6×6 puntos, sin sitio para contorno, sombreado ni detalle.

Esta versión pide **detalle de 1 px**, la misma densidad que los iconos de rol de 16×16 que aparecen justo debajo, en
la lista de roles. También pide emblemas descritos con precisión, marco con relieve y un tercer estado (`hover`).

Restricciones de la pantalla (no negociables):

- **24×16 px por ficha**: cinco fichas en fila cada 25 px (columna de 124 px), de y = 30 a 46; la lista de roles empieza
  en y = 48.

Destino: `tools/textures/source/ui/roles/` (se sobrescriben las 10 fichas y se añaden las 5 de `hover`).

---

## Prompt para pegar en ChatGPT

> Necesito **15 piezas de interfaz en pixel art de alta calidad**: las «fichas de categoría» de la pantalla de roles de
> un mod de Minecraft de fantasía oscura. Son botones pequeños; cada uno muestra el **emblema** de una de cinco
> categorías de personaje. Deben verse **ricas en detalle y legibles a tamaño real**, al nivel de los iconos de objetos
> de Minecraft (16×16 dibujados a mano, con contorno, luces y sombras). No quiero siluetas planas.
>
> ### Formato (obligatorio)
> - Cada pieza: PNG RGBA, fondo transparente, **exactamente 24×16 px**.
> - **Detalle a 1 px** (cada píxel cuenta, como un icono de Minecraft). Sin antialiasing, sin desenfoques ni
>   degradados suaves. Transparencia solo 0 % o 100 %.
> - Máximo **32 colores** por pieza.
> - **Sin letras, números ni símbolos tipográficos.**
> - Las 15 piezas comparten exactamente la **misma silueta exterior y el mismo marco**. En cada categoría, el emblema es
>   el mismo dibujo en los tres estados; solo cambian colores, brillo y halo.
>
> ### Marco (igual en todas)
> - **Esquinas recortadas**: el píxel de cada esquina es transparente.
> - **Contorno exterior** de 1 px `#101117`.
> - **Anillo metálico** de 1 px con bisel: lado superior e izquierdo más claros, inferior y derecho más oscuros (luz
>   desde arriba a la izquierda).
> - **Remache** de 1 px en cada esquina interior del anillo.
> - **Filete interior** de 1 px de sombra (`#0C0D12`) que separa el anillo del fondo.
> - Zona útil del interior: **x 3–20, y 3–12**. El emblema ocupa como máximo **14×10 px**, centrado y con 1 px de
>   aire.
>
> | Estado | Anillo (claro / medio / oscuro) | Fondo | Emblema |
> |---|---|---|---|
> | `normal` | `#9C7A35` / `#6E5428` / `#4B391F` | `#1A1C26`, con una banda de 1 px `#20232F` arriba | Colores de la categoría |
> | `hover` | `#C8A050` / `#9C7A35` / `#6E5428` | `#20232F` | Colores de la categoría, un tono más claro |
> | `activa` | `#F9D98B` / `#EBBE5A` / `#9C7A35` | Tono oscuro de la categoría, con banda de 1 px más clara arriba | Colores de la categoría más **halo** de 1 px alrededor del emblema (claro de la categoría, a puntos alternos) |
>
> ### Emblemas (descríbelos con contorno oscuro y tres tonos: claro, medio y oscuro)
> 1. **Marcial (`marcial`) — Espada y hacha cruzadas.** Espada en diagonal (abajo-izquierda a arriba-derecha):
>    - hoja de 2 px de ancho en acero (`#E6E6F0` filo, `#A8B0BC` medio, `#5C6470` sombra);
>    - guarda dorada (`#EBBE5A` / `#967438`);
>    - empuñadura de cuero (`#7A4A2A`) y pomo dorado.
>
>    Detrás, cruzada, un hacha con la cabeza en el rojo de la categoría (`#FFB3A8` / `#F0675C` / `#A3302A`) y mango de
>    madera.
> 2. **Arcano (`arcano`) — Estrella arcana.** Estrella de cuatro puntas largas en violeta (`#E3DDFF` / `#A99CF0` /
>    `#5B4DA8`) con núcleo blanco brillante (`#FFFFFF`, 2×2). Alrededor:
>    - un anillo fino incompleto de runas (puntos y trazos de 1 px);
>    - dos chispas pequeñas en diagonal.
> 3. **Sombra (`sombra`) — Luna y daga.** Luna creciente abierta hacia la derecha en morado (`#D9C4F0` / `#A07CC8` /
>    `#5E3F82`), con dos o tres cráteres de 1 px. Atravesándola en vertical, una daga fina de acero (hoja de 1 px con
>    filo claro y guarda oscura). Dos o tres píxeles de bruma bajo la luna.
> 4. **Guardián (`guardian`) — Escudo con hoja.** Escudo heráldico de punta:
>    - borde metálico de 1 px (oro viejo `#9C7A35` con luz `#EBBE5A` arriba);
>    - campo en verde (`#B8F5DA` / `#4DE2A6` / `#1F8A62`) partido en vertical con una franja central más clara;
>    - una hoja o brote de 3×4 px en el centro;
>    - un brillo de 2 px en la esquina superior izquierda.
> 5. **Oficio (`oficio`) — Engranaje y martillo.** Engranaje de 8 dientes en ámbar (`#FFD99A` / `#F0AE55` / `#A8641F`)
>    con tuerca central oscura y su agujero. Delante, cruzado en diagonal, un martillo pequeño:
>    - cabeza de acero (`#E6E6F0` / `#5C6470`);
>    - mango de madera (`#7A4A2A`).
>
> Todos los emblemas llevan **contorno exterior de 1 px `#101117`** (sin cerrar en las zonas de brillo, como los iconos
> de Minecraft) y deben distinguirse entre sí **por la silueta**, no solo por el color.
>
> ### Fondos de `activa` por categoría
> | Categoría | Fondo | Banda superior |
> |---|---|---|
> | Marcial | `#6E1A16` | `#842420` |
> | Arcano | `#3E3470` | `#4A3F86` |
> | Sombra | `#34234A` | `#402C5A` |
> | Guardián | `#1C5E46` | `#237255` |
> | Oficio | `#6B4214` | `#80501A` |
>
> ### Archivos (15)
> `categoria_<categoría>_<estado>.png` con categoría `marcial`, `arcano`, `sombra`, `guardian`, `oficio` y estado
> `normal`, `hover`, `activa`.
>
> ### Entrega
> - Los 15 PNG.
> - Una **hoja de vista previa** (no se usa en el juego) con:
>   1. las cinco fichas en fila a tamaño real (cada 25 px) en sus tres estados;
>   2. las mismas ampliadas ×6, sin interpolación, sobre fondo `#14161F`.
> - Repaso final: 24×16 exactos, esquinas transparentes, mismo marco en las 15, emblemas distinguibles por la silueta,
>   sin letras, ≤ 32 colores.

---

## Notas para la implementación (Claude)

- **Excepción a la rejilla de 2×2:** estas fichas se validan sin esa regla (detalle de 1 px, como los iconos de rol de
  16×16 de la misma lista). En `tools/textures/gen_roles_ui.py`:
  - las fichas pasan a `(24, 16)`, sin comprobación de rejilla y con un máximo de 32 colores;
  - se añade el estado `hover`.
- **Código:**
  - `RolesUi.category(ctx, category, state, x, y)` con estado `normal` / `hover` / `activa` y alto 16;
  - en `RolesScreen.renderLeft`, la ficha bajo el ratón usa `hover` y la zona de clic y del tooltip mide 16 px de alto.
- **Contraste:** el tono medio de cada emblema sobre su fondo cumple ≥ 3:1 (calculado en la versión anterior: entre
  3,7:1 y 10,3:1). El contorno `#101117` y las luces refuerzan la lectura.
