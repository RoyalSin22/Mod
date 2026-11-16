# Prompt de arte — Grimorio y bestiario de pergamino (v67, G5)

Encargo para el arte definitivo del libro. Hoy el juego dibuja piezas **provisionales con rellenos** (`GuideUi`: paleta
de pergamino oscuro con marco dorado) que se sustituyen sin cambiar el código que las llama. El autor tiene una **imagen de
referencia** del bestiario y cree que otro chat ya la está produciendo: este documento solo fija **qué piezas** hacen
falta y a qué tamaño, para integrarlas cuando lleguen.

**Importante:** el libro usa texto **claro** (crema y dorado) sobre fondo oscuro. Si el arte final es de pergamino claro,
hay que cambiar también los colores de texto del libro (`UiStyle`/`GuideScreen`) en la misma tanda.

## Piezas

| Pieza | Dónde se usa | Tamaño de la ficha | Estiramiento |
|---|---|---|---|
| `window` | Marco exterior del libro | 24×24 texels | 9-slice con esquinas de 8 |
| `panel` | Hoja de pergamino de la página (todas las páginas) | 24×24 | 9-slice con esquinas de 6; el centro se repite, no se estira |
| `portrait` | Marco del retrato de la criatura | 24×24 | 9-slice con esquinas de 8 |
| `ribbon` | Cinta con el nombre de la criatura | 24×14 | 3-slice horizontal con remates de 6 |
| `divider` | Filete con rombo entre secciones | 48×5 | horizontal; el centro es el rombo |
| `corner` | Remate dorado de esquina | 8×8 | sin estirar |

Densidad doble: se dibujan al doble y el juego los muestra a la mitad (igual que el resto de kits del proyecto).
Fuente del arte en `tools/textures/source/ui/guide/` (no se editan los PNG de salida) y un generador con `--check`
(`tools/textures/gen_guide_ui.py`) los valida y copia a `textures/gui/guide_ui/`.

## Carta de criatura (referencia de composición)

Pergamino a dos columnas: a la izquierda el retrato (marco `portrait`) con el sello de muertes y región debajo; a la
derecha, la cinta con el nombre, el tipo, las estadísticas, los consejos, el botín (casillas de 18×18) y el lore en
cursiva. La maqueta numérica está en `logic/GuideCardLayout` (con pruebas): el arte debe respetar esas proporciones o
actualizar la maqueta a la vez.

## Estado: integrado (G5)

El arte llegó como imágenes grandes (1254 px y más). Se redujo con `tools/textures/prepare_guide_art.py` a piezas del
tamaño de uso, **más pequeñas que las de la tabla de arriba** porque la maqueta del libro es ajustada (pestañas a 4 px del
borde, lista a 6 px): ventana 34×34 (esquina 14, marco de unos 6 px), panel 20×20 (esquina 8), retrato 34×34 (esquina 14),
cinta 48×14 (remates de 19), divisor 56×7, esquina 12×13 y los emblemas del ojo (25×8) y del libro (21×9). No usan la
rejilla de 2×2: se dibujan 1:1. Los originales no están en el repositorio; el script los lee de una carpeta.
Para un arte nuevo: vuelve a ejecutar `prepare_guide_art.py <carpeta>` y después `gen_guide_ui.py`.

## Integración (histórico)

1. Copiar los PNG a `tools/textures/source/ui/guide/`.
2. Crear el generador con `--check` y `GuideUi` leerá las texturas (con el dibujo actual como respaldo si faltan).
3. Revisar los colores de texto del libro si el pergamino es claro.
