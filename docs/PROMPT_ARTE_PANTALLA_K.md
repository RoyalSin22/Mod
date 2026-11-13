# Prompt de arte — Pantalla K (Atributos y Habilidades)

Encargo para el generador de arte (ChatGPT). Decisiones del autor (2026-11-13): se conservan las **10 tarjetas de
habilidad**, **sin habilidades nuevas**, y **solo las dos pestañas actuales** (Atributos y Habilidades). Este kit sigue
la misma dirección de arte, paleta y reglas técnicas que el kit del árbol (`tools/textures/source/ui/`), para que las
dos pantallas parezcan de la misma familia.

Destino del arte fuente: `tools/textures/source/ui/k/<grupo>/<pieza>.png`. Un generador (`gen_k_ui.py`, con `--check`)
lo copiará a `textures/gui/k_ui/`. No se editan a mano los PNG de salida.

---

## Prompt para pegar en ChatGPT

> Necesito un kit de piezas de interfaz en **pixel art** para la pantalla de personaje de un mod de Minecraft
> (estilo fantasía oscura, sobrio, legible; inspiración «hoguera» de los juegos souls, pero original). El kit debe
> coincidir con un kit ya existente cuyas reglas y paleta te doy abajo. Entrega **cada pieza como un PNG separado**,
> con fondo transparente, **exactamente** al tamaño en píxeles indicado y con el nombre de archivo indicado.
>
> ### Reglas técnicas (obligatorias)
> 1. **Píxel lógico de 2×2**: cada bloque de 2×2 px del PNG es de un solo color. Todos los tamaños son pares.
> 2. **Sin antialiasing, sin degradados suaves, sin desenfoque ni sombras difusas.** Bordes duros.
> 3. **Máximo 24 colores por pieza** (contando la transparencia). Transparencia solo 0 % o 100 %, salvo el relleno
>    interior de los paneles, que puede ser semitransparente (alfa 220 = `DC`).
> 4. **Sin texto, letras, números ni iconos de objetos** dentro de las piezas: el juego dibuja el texto y los iconos
>    encima. Deja zonas lisas donde irá el texto.
> 5. Los paneles son **9-slice** con esquinas de **12 px**: las esquinas se copian tal cual y los bordes y el centro
>    se repiten. Los bordes y el centro deben ser **uniformes o con un patrón que se repita cada 2 px**, sin adornos
>    en medio de los lados.
> 6. Las piezas que comparten estados (normal/hover/activo…) deben tener **exactamente la misma silueta**; solo
>    cambia el color o el brillo.
>
> ### Paleta base (del kit existente; úsala como núcleo)
> | Uso | Color |
> |---|---|
> | Relleno de panel (semitransparente) | `#1E1D24` alfa 220 |
> | Relleno opaco | `#1E1D24` |
> | Sombra profunda / contorno exterior | `#111017` |
> | Bisel interior oscuro | `#2B2A33` |
> | Bisel medio | `#3A3644` |
> | Acero / remaches | `#4E545E` |
> | Oro viejo (marco) | `#967438` |
> | Oro brillante (realces, estado activo) | `#EBBE5A` |
> | Brasa (acento de la Hoguera) | `#FF5430`, `#C8281F`, `#801E2A` |
> | Verde XP (barra, nuevo) | `#2E7D32`, `#4CAF50`, `#8BE08F` |
> | Azul alma (acento de atributos, nuevo) | `#2A3A66`, `#4A6BB5`, `#9DB8F0` |
>
> ### Piezas
>
> **A. Paneles (9-slice, esquinas de 12 px)**
> | Archivo | Tamaño | Descripción |
> |---|---|---|
> | `paneles/k_panel_principal_9slice.png` | 192×128 | Marco de la ventana entera. Contorno `#111017`, marco de oro viejo con remaches de acero en las cuatro esquinas, relleno semitransparente. |
> | `paneles/k_panel_hoguera_9slice.png` | 192×128 | Igual que el principal pero con las esquinas adornadas con pequeñas brasas (`#FF5430`/`#C8281F`) y un bisel cálido: se usa cuando se abre desde la Hoguera. |
> | `paneles/k_panel_columna_9slice.png` | 64×64 | Panel interior hundido para cada columna de Atributos (bisel oscuro arriba/izquierda, claro abajo/derecha). Sin oro. |
> | `paneles/k_panel_detalle_9slice.png` | 64×64 | Panel de la ficha del atributo seleccionado: como el de columna pero con un filete de oro viejo de 2 px. |
>
> **B. Pestañas (3 piezas, 96×22; la misma forma sirve para las dos pestañas)**
> Archivos `pestanas/pestana_<estado>.png` con estado `inactiva`, `hover`, `activa`.
> Forma de pestaña de carpeta con la parte inferior abierta; la `activa` se une visualmente con el panel (sin borde
> inferior) y lleva un filete superior de oro brillante; `hover` sube el brillo del marco; `inactiva` más oscura y
> 2 px más baja visualmente (deja 2 px transparentes arriba). Sin texto: el nombre lo pone el juego.
>
> **C. Tarjeta de habilidad (200×38, 4 estados)**
> Archivos `tarjetas/tarjeta_<estado>.png` con estado `normal`, `hover`, `maxima`, `subida`.
> - Zona del icono: casilla cuadrada de **22×22** con su propio marco, a 2 px del borde izquierdo y centrada en
>   vertical (el juego dibuja dentro un objeto de 16×16).
> - A la derecha del icono, una franja lisa para el nombre (arriba) y un hueco para la barra de XP (ver D) a
>   14 px del borde superior.
> - Franja inferior lisa de 10 px para una línea de texto (pasivas).
> - `normal`: marco de acero/bisel oscuro. `hover`: marco de oro viejo. `maxima` (habilidad al nivel máximo): marco de
>   oro brillante con dos pequeños adornos en las esquinas superiores. `subida` (acaba de subir de nivel): como
>   `maxima` pero con chispas doradas en el borde (sin salirse del tamaño).
>
> **D. Barra de XP (3 piezas, 160×6)**
> | Archivo | Descripción |
> |---|---|
> | `barras/xp_vacia.png` | Canal hundido con contorno `#111017` y fondo `#202433`. |
> | `barras/xp_llena.png` | Relleno verde (`#4CAF50`) con reflejo superior de 2 px (`#8BE08F`) y sombra inferior (`#2E7D32`). El juego la recorta por la izquierda según el progreso. |
> | `barras/xp_maxima.png` | Relleno dorado (`#EBBE5A` con reflejo claro y sombra `#967438`). |
>
> **E. Hitos de habilidad (2 piezas, 8×8)**
> `hitos/hito_apagado.png` y `hitos/hito_encendido.png`: rombo pequeño; apagado en bronce oscuro (`#3A3427`),
> encendido en oro brillante con un píxel lógico de brillo. Hay cinco por tarjeta (niveles 10, 20, 30, 40 y 50).
>
> **F. Filas de atributo (140×16, 3 estados)**
> `filas/fila_<estado>.png` con estado `normal`, `seleccionada`, `alterna` (para filas pares, un tono más claro).
> Fondo liso con bisel muy sutil; `seleccionada` con marco de 2 px en azul alma y punta de flecha a la izquierda.
> Deja libres los 4 px de cada extremo.
>
> **G. Flechas de reparto (4 piezas, 8×10)**
> `flechas/menos_<estado>.png` y `flechas/mas_<estado>.png` con estado `activa` y `desactivada`. Chevrons de pixel
> art (‹ y ›): activa en oro brillante, desactivada en gris acero `#4E545E`.
>
> **H. Cabecera de sección (1 pieza, 140×12)**
> `cabeceras/cabecera_seccion.png`: filete horizontal con un pequeño rombo dorado en el extremo izquierdo y una
> línea de oro viejo que se desvanece **por píxeles** (patrón de puntos cada 2 px, no degradado) hacia la derecha.
> Zona central lisa para el título.
>
> **I. Botones (3 tipos × 3 estados = 9 piezas, 80×18)**
> `botones/<tipo>_<estado>.png` con tipo `confirmar`, `deshacer`, `cerrar` y estado `normal`, `pulsado`,
> `desactivado`. Misma silueta que los botones del kit existente: marco de oro viejo, interior `#2B2A33`; `confirmar`
> con un filete de brasa; `pulsado` desplazado 2 px hacia abajo con bisel invertido; `desactivado` desaturado
> (acero y gris). Sin texto.
>
> **J. Emblema de la Hoguera (1 pieza, 48×48)**
> `hoguera/emblema_hoguera.png`: espada clavada en un montón de cenizas con brasas (`#FF5430`, `#C8281F`, `#801E2A`)
> y halo de oro viejo, para la cabecera cuando la pantalla se abre en la Hoguera. Original, sin copiar ningún
> juego existente.
>
> ### Entrega
> - 34 PNG en total, con los nombres y carpetas indicados.
> - Además, una **hoja de vista previa** (no se usará en el juego) con todas las piezas sobre fondo `#3A3644` y una
>   maqueta de la pantalla montada (ventana de 430×262 px a escala ×2) con las dos pestañas, para revisar la
>   coherencia.
> - Repaso final antes de entregar: tamaños exactos, rejilla 2×2, ≤24 colores, sin texto, siluetas idénticas entre
>   estados.

---

## Notas para la implementación (Claude)

**Estado:** kit recibido (34 piezas, validadas: tamaños, rejilla 2×2, ≤24 colores, alfa 0/220/255) e integrado en v66.
La pantalla conserva sus cuatro pestañas (Configuración, Roles, Habilidades y Atributos): la misma pieza de pestaña se
ajusta al ancho de cada texto. Los botones de Templado y Frascos de la Hoguera usan la pieza «cerrar» (neutra).

- Pantalla: `src/client/java/dev/aptitudes/client/screen/AptitudesScreen.java` (hoy dibujada con `fill` y `UiStyle`).
  Se conservan W = 430 y H = 262, las 10 tarjetas en 5 × 2 (paso de 40 px; la tarjeta pasa de 37 a 38 px de alto,
  que sigue cabiendo) y las pestañas Atributos/Habilidades.
- Geometría en una clase pura con JUnit (patrón de `logic/TreeScreenLayout`); respaldo al dibujo actual si faltan las
  piezas (como `TreeUi`).
- Generador `tools/textures/gen_k_ui.py` con `--check`, mismo patrón que `gen_tree_ui.py` (comprueba tamaños y rejilla).
- Recuento: 4 paneles + 3 pestañas + 4 tarjetas + 3 barras + 2 hitos + 3 filas + 4 flechas + 1 cabecera + 9 botones +
  1 emblema = 34 PNG, más la hoja de vista previa (no entra en el juego).
