# Prompt de arte — Fichas de categoría con emblema (pantalla de roles, J)

Encargo para el generador de arte (ChatGPT). Sustituye a las 10 fichas `categoria_<categoría>_<estado>.png` del kit
«fantasía oscura v2» (24×14 px), que solo llevaban una barra de color. Decisión del autor (2026-11-13): cada categoría
debe tener un **emblema distintivo**, no una barra. Con emblema no hace falta la abreviatura de texto; el nombre
completo sigue en el tooltip.

Restricciones de la pantalla (no negociables):

- **Ancho fijo de 24 px**: las cinco fichas van en fila cada 25 px dentro de la columna de 124 px.
- **Alto de 16 px**: van de y = 30 a 46 y las filas de roles empiezan en y = 48.

Destino del arte fuente: `tools/textures/source/ui/roles/` (mismos nombres; se sobrescriben las 10 fichas actuales).

---

## Prompt para pegar en ChatGPT

> Necesito **10 piezas de interfaz en pixel art**: las «fichas de categoría» de la pantalla de roles de un mod de
> Minecraft (fantasía oscura, sobrio). Son botones pequeños; cada uno lleva el **emblema** de una de cinco categorías de
> personaje. Entrega cada pieza como un PNG separado, fondo transparente, **exactamente de 24×16 px**, con el nombre
> indicado.
>
> ### Reglas técnicas (obligatorias)
> 1. **Píxel lógico de 2×2**: cada bloque de 2×2 px es de un solo color (la pieza es una rejilla lógica de 12×8).
> 2. Sin antialiasing, degradados, desenfoques ni sombras suaves. Transparencia solo 0 % o 100 %.
> 3. Máximo **24 colores** por pieza (bastan unos 7).
> 4. **Sin letras, números ni símbolos tipográficos**: solo el emblema dibujado.
> 5. Las 10 piezas comparten **la misma silueta exterior**; en cada categoría, el emblema es idéntico en sus dos
>    estados (solo cambian colores).
>
> ### Geometría (píxeles reales, origen arriba a la izquierda)
> | Zona | Posición | Contenido |
> |---|---|---|
> | Esquinas | los cuatro bloques de 2×2 de las esquinas | **Transparentes** (esquinas redondeadas) |
> | Anillo del marco | borde exterior de 2 px | Color del marco según el estado |
> | Interior | x 2–21, y 2–13 (20×12) | Fondo liso según el estado |
> | **Emblema** | centrado: x 6–17, y 2–13 (12×12 = 6×6 lógicos) | Silueta del emblema, legible a tamaño real |
>
> ### Emblemas (uno por categoría; silueta clara, reconocible a 12×12)
> | Categoría (archivo) | Emblema | Notas |
> |---|---|---|
> | Marcial (`marcial`) | **Espada en diagonal** con guarda | De abajo-izquierda a arriba-derecha |
> | Arcano (`arcano`) | **Estrella de cuatro puntas** | Con un píxel de brillo en el centro |
> | Sombra (`sombra`) | **Luna creciente** | Abierta hacia la derecha |
> | Guardián (`guardian`) | **Escudo** de punta | Con una franja vertical central |
> | Oficio (`oficio`) | **Engranaje** | Cuatro dientes y hueco central |
>
> ### Colores
> | Categoría | Emblema | Fondo `activa` |
> |---|---|---|
> | Marcial | `#F0675C` | `#6E1A16` |
> | Arcano | `#A99CF0` | `#3E3470` |
> | Sombra | `#A07CC8` | `#34234A` |
> | Guardián | `#4DE2A6` | `#1C5E46` |
> | Oficio | `#F0AE55` | `#6B4214` |
>
> - **`normal`**: anillo bronce `#6E5428`, fondo `#1A1C26`, emblema en el color de la categoría.
> - **`activa`**: anillo oro `#EBBE5A`, fondo en el tono oscuro de la categoría, emblema en el color de la categoría con
>   uno o dos píxeles lógicos de brillo `#F9D98B` en su parte superior.
>
> ### Archivos (10)
> `categoria_marcial_normal.png`, `categoria_marcial_activa.png`, `categoria_arcano_normal.png`,
> `categoria_arcano_activa.png`, `categoria_sombra_normal.png`, `categoria_sombra_activa.png`,
> `categoria_guardian_normal.png`, `categoria_guardian_activa.png`, `categoria_oficio_normal.png`,
> `categoria_oficio_activa.png`.
>
> ### Entrega
> - Los 10 PNG, más una **hoja de vista previa** (no se usa en el juego) con las fichas a tamaño real y ampliadas ×4
>   sobre fondo `#14161F`, en fila como en el juego (cada 25 px), para comprobar que los cinco emblemas se distinguen.
> - Repaso final: 24×16 exactos, rejilla 2×2, esquinas transparentes, sin letras, emblemas distintos entre sí.

---

## Notas para la implementación (Claude)

- **Contraste del emblema** (WCAG 1.4.11, elementos gráficos ≥ 3:1), calculado con la luminancia relativa:

  | Categoría | Sobre fondo normal | Sobre fondo activo |
  |---|---|---|
  | Marcial | 5,5 | 3,7 |
  | Arcano | 7,0 | 4,5 |
  | Sombra | 5,0 | 4,2 |
  | Guardián | 10,3 | 4,7 |
  | Oficio | 8,8 | 4,5 |

  Los acentos del kit actual no llegaban: Sombra `#66457F` daba 2,2:1 y Marcial `#C8281F` 2,1:1 sobre su fondo activo.
- **Elección de emblemas:** que no se confundan entre sí ni con las armas de rol. Por eso Sombra lleva luna y no daga
  (chocaría con la espada de Marcial) y Oficio engranaje y no martillo (el Paladín, de Guardián, usa martillo).
- **Al recibir el arte:**
  - en `tools/textures/gen_roles_ui.py`, tamaño de las fichas a `(24, 16)`;
  - en `RolesUi.category`, alto 16;
  - en `RolesScreen.renderLeft`, la zona de clic y del tooltip de 14 a 16 px de alto;
  - comprobar con `--check` y en partida.
