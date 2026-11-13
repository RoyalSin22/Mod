# Prompt de arte — Fichas de categoría más altas (pantalla de roles, J)

Encargo para el generador de arte (ChatGPT). Sustituye a las 10 fichas `categoria_<categoría>_<estado>.png` del kit
«fantasía oscura v2» (24×14 px). Motivo: su hueco interior mide 16×6 px y no cabe la abreviatura de la categoría
(«MAR», «ARC», «SOM», «GUA», «OFI»… siempre 3 mayúsculas), que el juego dibuja encima con su fuente. Con la fuente
de Minecraft, 3 mayúsculas ocupan **17 px de ancho + 1 de sombra** y **8 px de alto + 1 de sombra**.

Restricciones de la pantalla (no negociables):

- **Ancho fijo de 24 px**: las cinco fichas van en fila cada 25 px dentro de la columna de 124 px.
- **Alto de 16 px**: van de y = 30 a 46 y las filas de roles empiezan en y = 48.
- Por tanto, el marco solo puede tener **un anillo de 2 px** (un píxel lógico): con dos anillos el interior no llega a
  los 18 px de ancho del texto.

Destino del arte fuente: `tools/textures/source/ui/roles/` (mismos nombres de archivo; se sobrescriben las 10 fichas
actuales). Las demás piezas del kit no cambian.

---

## Prompt para pegar en ChatGPT

> Necesito **10 piezas de interfaz en pixel art**: las «fichas de categoría» de la pantalla de roles de un mod de
> Minecraft (fantasía oscura, sobrio). Son botones pequeños que identifican cinco categorías por color; el juego
> escribe encima una abreviatura de 3 letras mayúsculas, así que **la zona del texto debe ser lisa**. Entrega cada pieza
> como un PNG separado, fondo transparente, **exactamente de 24×16 px**, con el nombre indicado.
>
> ### Reglas técnicas (obligatorias)
> 1. **Píxel lógico de 2×2**: cada bloque de 2×2 px es de un solo color (la pieza es una rejilla lógica de 12×8).
> 2. Sin antialiasing, degradados, desenfoques ni sombras suaves. Transparencia solo 0 % o 100 %.
> 3. Máximo **24 colores** por pieza (unos 6 bastarán).
> 4. **Sin letras, números ni símbolos**: el texto lo pone el juego.
> 5. Las 10 piezas tienen **la misma silueta**; solo cambian los colores.
>
> ### Geometría exacta (coordenadas en píxeles reales, origen arriba a la izquierda)
> | Zona | x | y | Contenido |
> |---|---|---|---|
> | Esquinas | (0–1, 0–1), (22–23, 0–1), (0–1, 14–15), (22–23, 14–15) | | **Transparentes** (esquinas redondeadas) |
> | Anillo del marco | borde exterior de 2 px | | Color de marco según el estado (abajo) |
> | Interior | 2–21 | 2–13 | 20×12 px |
> | **Zona del texto** | 2–21 | 2–11 | **Un solo color liso**, sin brillos, puntos ni textura |
> | Barra de acento | 2–21 | 12–13 | Color de acento de la categoría, de lado a lado |
>
> ### Estados
> - **`normal`**: anillo bronce `#6E5428`; zona del texto `#1A1C26`; barra de acento de la categoría.
> - **`activa`**: anillo oro `#EBBE5A`; zona del texto en el **tono oscuro** de la categoría; barra de acento de la
>   categoría.
>
> ### Colores por categoría
> | Categoría (archivo) | Acento (barra, ambos estados) | Tono oscuro (zona del texto en `activa`) |
> |---|---|---|
> | Marcial (`marcial`) | `#F0675C` | `#6E1A16` |
> | Arcano (`arcano`) | `#A99CF0` | `#3E3470` |
> | Sombra (`sombra`) | `#A07CC8` | `#34234A` |
> | Guardián (`guardian`) | `#4DE2A6` | `#1C5E46` |
> | Oficio (`oficio`) | `#F0AE55` | `#6B4214` |
>
> ### Archivos (10)
> `categoria_marcial_normal.png`, `categoria_marcial_activa.png`, `categoria_arcano_normal.png`,
> `categoria_arcano_activa.png`, `categoria_sombra_normal.png`, `categoria_sombra_activa.png`,
> `categoria_guardian_normal.png`, `categoria_guardian_activa.png`, `categoria_oficio_normal.png`,
> `categoria_oficio_activa.png`.
>
> ### Entrega
> - Los 10 PNG con esos nombres, más una **hoja de vista previa** (no se usa en el juego) con las fichas ampliadas ×4
>   sobre fondo `#14161F` y, encima de cada una, una abreviatura de ejemplo («MAR», «ARC», «SOM», «GUA», «OFI») en
>   blanco con sombra, para comprobar que se lee.
> - Repaso final: 24×16 exactos, rejilla 2×2, esquinas transparentes, zona del texto lisa y sin letras en los PNG.

---

## Notas para la implementación (Claude)

- **Contraste comprobado** (fórmula de luminancia relativa de WCAG 2.x), con el texto del juego `#E8E8F0`:
  - fondo normal `#1A1C26`: 13,6:1;
  - fondos activos: Marcial 9,2:1, Arcano 8,7:1, Sombra 11,3:1, Guardián 6,1:1, Oficio 7,0:1 (todos ≥ 4,5:1, AA).
- **Acentos ≥ 3:1** (WCAG 1.4.11, elementos gráficos) sobre los dos fondos de su ficha:
  - Marcial 5,5 / 3,7;
  - Arcano 7,0 / 4,5;
  - Sombra 5,0 / 4,2;
  - Guardián 10,3 / 4,7;
  - Oficio 8,8 / 4,5.

  Son más claros que los acentos del kit actual: el morado de Sombra (`#66457F`) daba 2,2:1 y el rojo de Marcial
  (`#C8281F`) 2,1:1 sobre su fondo activo.
- **Al recibir el arte**:
  - en `tools/textures/gen_roles_ui.py`, tamaño de las fichas a `(24, 16)`;
  - en `RolesUi.category`, alto 16;
  - en `RolesScreen.renderLeft`, dibujar la abreviatura (`<categoría>.short`) centrada en x + 12, y + 3, color
    `UiStyle.TEXT` con sombra, también con el kit;
  - comprobar con `--check` y en partida en los cinco idiomas (la más ancha es «WÄC», de alemán).
