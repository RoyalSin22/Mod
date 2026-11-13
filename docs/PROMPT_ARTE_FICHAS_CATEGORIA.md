# Prompt de arte — Fichas de categoría con emblema, alto detalle (pantalla de roles, J)

Encargo para el generador de arte (ChatGPT). Sustituye a las fichas `categoria_<categoría>_<estado>.png`.

**Historial (por qué esta versión):**

| Versión | Qué pedía | Problema |
|---|---|---|
| 1 | Barra de color | No identificaba la categoría |
| 2 | Emblema a 24×16 con rejilla de 2×2 | Solo 6×6 puntos para el emblema: silueta plana |
| 3 | Emblema a 24×16 con detalle de 1 px | 14×10 puntos: el autor lo considera aún demasiado simple |

El hueco en pantalla no puede crecer (24×16 px de interfaz: cinco fichas cada 25 px en una columna de 124). Esta
versión **duplica la densidad**: las fichas se dibujan a **48×32** y el juego las muestra en el mismo hueco de 24×16.

- A **escala de interfaz 2**, cada texel es un píxel de pantalla.
- A **escala 4**, cada texel ocupa 2×2 píxeles.
- A **escala 3** (1,5 píxeles por texel) puede verse alguna irregularidad leve.

Así el emblema dispone de unos **30×20 texels**, con sitio para materiales, luces, contorno selectivo y detalles
pequeños.

Destino del arte fuente: `tools/textures/source/ui/roles/`. Son 15 piezas: 5 categorías × 3 estados.

---

## Prompt para pegar en ChatGPT

> Necesito **15 fichas de categoría en pixel art de máxima calidad** para la pantalla de roles de un mod de Minecraft
> de fantasía oscura. Cada ficha es un pequeño botón con el **emblema** de una categoría de personaje. Quiero un
> acabado de **pixel art profesional, trabajado píxel a píxel**: materiales reconocibles (acero, oro, madera, cuero,
> magia), volumen con luz y sombra, brillos especulares y contornos limpios. No quiero siluetas planas ni iconos
> genéricos. Piensa en el nivel de detalle de los iconos de objetos de *Terraria* o *Dead Cells*, adaptado a este
> tamaño.
>
> ### Formato (obligatorio)
> - Cada pieza: PNG RGBA, fondo transparente, **exactamente 48×32 texels**. El juego la muestra a la mitad (24×16), así
>   que todo el detalle debe leerse también a esa reducción.
> - **Detalle a 1 texel**, sin antialiasing, sin desenfoques ni degradados suaves. Transparencia solo 0 % o 100 %
>   (sin semitransparencias).
> - Máximo **48 colores** por pieza.
> - **Sin letras, números ni símbolos tipográficos.**
> - Las 15 piezas comparten **exactamente el mismo marco y la misma silueta exterior**. En cada categoría, el emblema
>   es el mismo dibujo en los tres estados.
>
> ### Reglas de pixel art (aplícalas en todo)
> 1. **Una sola fuente de luz**, arriba a la izquierda: caras superiores e izquierdas claras, inferiores y derechas en
>    sombra.
> 2. **Rampas con desplazamiento de tono**: las sombras tiran hacia el violeta o azul frío y las luces hacia el amarillo
>    cálido. Usa las rampas de 4–5 tonos de la tabla.
> 3. **Contorno selectivo** (*sel-out*):
>    - contorno exterior de 1 texel `#101117` en el lado de sombra;
>    - en el lado iluminado, el contorno es el tono más oscuro del propio material, nunca negro puro.
> 4. **Nada de *pillow shading*** (sombra que rodea la figura por todos los lados); el volumen sigue a la luz.
> 5. **Sin píxeles sueltos** ni ruido: cada mancha de color tiene al menos 2 texels, salvo los brillos especulares
>    (1 texel, a propósito).
> 6. **Sin bandas**: los cambios de tono no forman escalones paralelos al contorno.
> 7. **Tramado (*dithering*)** solo en brillos mágicos, bruma y halos, y en patrón regular de tablero.
> 8. **Silueta legible**: reducidas a 24×16, las cinco fichas deben distinguirse por la forma del emblema, no solo por
>    el color.
>
> ### Rampas de color (de claro a oscuro)
> | Material | Tonos |
> |---|---|
> | Acero | `#F4F6FA` `#C9D0DA` `#9AA3B2` `#646C7C` `#3A3F4C` |
> | Oro | `#FFF0B8` `#F2CD6A` `#C8973A` `#8A5F24` `#4E3416` |
> | Madera | `#C08A55` `#93633A` `#6B4428` `#45291A` |
> | Cuero | `#A8653F` `#7A4A2A` `#4F2C1C` |
> | Rojo marcial | `#FFC9BE` `#F7867A` `#E0483D` `#A8282A` `#61151F` |
> | Violeta arcano | `#F1EDFF` `#C9BEFF` `#9A88EE` `#6550C2` `#372A78` |
> | Morado sombra | `#E8DAF7` `#BC9BDD` `#8E66B8` `#5E3F82` `#33204D` |
> | Verde guardián | `#D6FFEE` `#86F0C4` `#3FCF93` `#1F8A62` `#0F4A36` |
> | Ámbar oficio | `#FFE9BF` `#F8C676` `#E3963F` `#A8641F` `#5C3412` |
>
> ### Marco (idéntico en las 15; coordenadas en texels sobre 48×32)
> - **Esquinas** recortadas en escalón de 2 texels (transparentes).
> - **Contorno exterior** de 1 texel `#101117`.
> - **Anillo metálico** de 2 texels con bisel:
>   - borde superior e izquierdo con el tono claro y una línea de brillo de 1 texel;
>   - borde inferior y derecho con el tono oscuro;
>   - las esquinas del anillo, con inglete (transición diagonal).
> - **Cuatro remaches** de 2×2 texels en las esquinas interiores del anillo: tono medio, 1 texel de brillo arriba a la
>   izquierda y 1 de sombra abajo a la derecha.
> - **Filete interior** de 1 texel `#0C0D12` (sombra proyectada del anillo sobre el fondo).
> - **Fondo**:
>   - en `normal` y `hover`, piedra oscura: base `#1A1C26` con un 6–8 % de texels sueltos en `#20232F` y `#15161E`
>     formando vetas horizontales sutiles (no ruido aleatorio);
>   - en `activa`, la misma textura en el tono oscuro de la categoría (tabla de fondos).
> - **Zona del emblema**: x 8–39, y 6–25 (32×20 texels), centrada, con al menos 1 texel de aire respecto al filete.
>
> | Estado | Anillo | Emblema |
> |---|---|---|
> | `normal` | Bronce: rampa de oro desplazada a tonos 3–5 (`#C8973A` `#8A5F24` `#4E3416`) | Normal |
> | `hover` | Oro medio: tonos 2–4 | Un paso de rampa más claro y un brillo extra en el punto más alto |
> | `activa` | Oro brillante: tonos 1–3 y remaches con brillo `#FFFFFF` | Normal, más un **halo** de 1–2 texels alrededor de la silueta, en el tono claro de la categoría y tramado en tablero hacia fuera; y 2–3 destellos de 1 texel en cruz sobre el halo |
>
> | Categoría | Fondo de `activa` (base, veta clara, veta oscura) |
> |---|---|
> | Marcial | `#6E1A16`, `#842420`, `#5A1411` |
> | Arcano | `#3E3470`, `#4A3F86`, `#32295C` |
> | Sombra | `#34234A`, `#402C5A`, `#2A1B3D` |
> | Guardián | `#1C5E46`, `#237255`, `#154A37` |
> | Oficio | `#6B4214`, `#80501A`, `#57350F` |
>
> ### Emblemas (descripción pieza a pieza)
>
> **1. Marcial (`marcial`) — espada larga y hacha de guerra cruzadas en aspa.**
> - **Espada**, de abajo-izquierda a arriba-derecha (unos 26 texels de largo):
>   - hoja de 3 texels de ancho en acero, con **acanaladura** central de 1 texel en el tono 4 y filo superior de 1
>     texel en el tono 1;
>   - punta afilada en 2 escalones;
>   - **guarda** de oro de 9 texels, con una gema roja de 2×2 en el centro (tono 2 con brillo de 1 texel);
>   - **empuñadura** de cuero de 5 texels con vendaje: franjas alternas de tonos 1 y 2 cada 2 texels;
>   - **pomo** redondo de oro de 3×3 con brillo de 1 texel.
> - **Hacha**, cruzada por detrás, de abajo-derecha a arriba-izquierda:
>   - mango de madera de 2 texels con veta (1 texel del tono 3 cada 3 texels) y regatón de acero;
>   - cabeza en media luna de 9×8 texels en rojo marcial, con filo de acero de 1 texel en el borde curvo;
>   - un remache de oro de 1 texel donde la cabeza se une al mango;
>   - una muesca de batalla de 1 texel en el filo.
>
> **2. Arcano (`arcano`) — estrella arcana con órbita de runas.**
> - **Estrella** de cuatro puntas largas (vertical de 18 texels, horizontal de 14). Cada punta está facetada como una
>   gema: la mitad que mira a la luz en tonos 1–2 y la otra en tonos 3–4, con una arista central de 1 texel.
> - **Núcleo**: 4×4 en blanco `#FFFFFF` con aro de 1 texel en el tono 1 y un halo tramado de 1 texel.
> - **Órbita**: elipse inclinada de 1 texel en el tono 3, interrumpida en tres puntos; sobre ella, tres marcas rúnicas
>   abstractas de 2×3 texels (trazos, nunca letras).
> - **Chispas**: dos destellos en cruz de 3×3 texels (centro blanco, brazos en el tono 1) en las diagonales libres.
>
> **3. Sombra (`sombra`) — luna creciente atravesada por una daga.**
> - **Luna** creciente de 18×18 texels, abierta hacia la derecha, en morado sombra:
>   - sombra interior en el tono 4;
>   - **luz de borde** de 1 texel en el tono 1 a lo largo de la curva interior;
>   - tres cráteres de 2×2 (tono 4 con brillo de 1 texel en su borde inferior).
> - **Daga**, vertical por delante de la luna (unos 20 texels):
>   - hoja de 2 texels en acero, con filo claro a la izquierda y punta en 2 escalones;
>   - guarda oscura de 5 texels (acero, tonos 4–5);
>   - mango de cuero con 2 vueltas;
>   - pomo con una gema morada de 1 texel.
> - **Bruma**: dos franjas horizontales tramadas en los tonos 4 y 5 bajo la luna.
> - **Estrellas**: dos de 1 texel blanco en el cielo.
>
> **4. Guardián (`guardian`) — escudo heráldico con brote.**
> - **Escudo** de punta (*heater*) de 20×22 texels:
>   - borde de oro de 2 texels con bisel (claro arriba a la izquierda, oscuro abajo a la derecha);
>   - cuatro clavos de 1 texel en el borde.
> - **Campo** en verde guardián, partido en vertical: mitad izquierda en tonos 2–3 y derecha en tonos 3–4, con una
>   **franja central** de oro de 2 texels.
> - **Brote** de dos hojas de 4×5 texels cada una, en verde claro (tonos 1–2):
>   - nervio central de 1 texel en el tono 3;
>   - tallo de 1 texel que baja hasta la punta del escudo.
> - **Brillo** diagonal de 3 texels en el tono 1 en la esquina superior izquierda del campo.
> - Dos **arañazos** de batalla de 2 texels en el tono 4 en la mitad derecha.
>
> **5. Oficio (`oficio`) — engranaje de bronce con martillo de herrero.**
> - **Engranaje** de 20×20 texels, con 8 dientes de 3×3 en ámbar:
>   - cada diente sombreado según su orientación respecto a la luz;
>   - corona interior con bisel de 1 texel;
>   - **tuerca hexagonal** central de 6×6 en acero con agujero de 2×2 `#101117`.
> - **Martillo**, cruzado por delante en diagonal (abajo-izquierda a arriba-derecha):
>   - mango de madera de 2 texels con veta y cuña de acero;
>   - cabeza de acero de 7×4 texels con cara de golpe brillante (tono 1), canto oscuro y un remache de 1 texel.
> - **Chispas**: tres de 1–2 texels en ámbar claro saltando de la cara del martillo.
>
> ### Archivos (15)
> `categoria_<categoría>_<estado>.png` con categoría `marcial`, `arcano`, `sombra`, `guardian`, `oficio` y estado
> `normal`, `hover`, `activa`.
>
> ### Entrega y autocomprobación
> - Los 15 PNG de 48×32.
> - Una **hoja de vista previa** (no se usa en el juego) sobre fondo `#14161F` con:
>   1. las cinco fichas en fila reducidas a 24×16 (como en el juego), en los tres estados;
>   2. las mismas a 48×32;
>   3. las mismas ampliadas ×4 sin interpolación.
> - Antes de entregar, revisa cada ficha contra esta lista:
>   - una sola luz;
>   - ningún píxel suelto;
>   - contorno selectivo;
>   - materiales reconocibles;
>   - emblemas distinguibles por la silueta a 24×16;
>   - mismo marco en las 15;
>   - ≤ 48 colores;
>   - sin letras.

---

## Notas para la implementación (Claude)

- **Densidad doble (excepción a la rejilla de 2×2):** fichas de 48×32 texels dibujadas en 24×16 px de interfaz.
  - `tools/textures/gen_roles_ui.py`: las fichas pasan a `(48, 32)`, sin comprobación de rejilla, con un máximo de 48
    colores y el estado `hover`.
  - `RolesUi.category(ctx, category, state, x, y)`: `drawTexture(id, x, y, 24, 16, 0, 0, 48, 32, 48, 32)`, la misma
    técnica que el emblema de la Hoguera en K. Estados `normal`, `hover` y `activa`.
  - `RolesScreen.renderLeft`: estado `hover` bajo el ratón y zona de clic y tooltip de 16 px de alto.
- **Contraste:** los tonos medios de cada rampa de categoría sobre su fondo superan 3:1, igual que en la versión
  anterior. El contorno `#101117` y las luces de borde refuerzan la silueta.
- **Escala de interfaz 3:** 1,5 píxeles por texel; puede verse alguna irregularidad leve. Si molesta en partida, se
  puede pedir una variante de 72×48 para esa escala.
