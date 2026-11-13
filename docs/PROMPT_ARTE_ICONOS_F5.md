# Prompt de arte — Iconos de las 40 habilidades nuevas de Druida, Mercader táctico, Monje y Chamán (v67, F5)

Encargo para el generador de arte (ChatGPT), igual que el de `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`. Son los iconos de los
nodos `druid_n1`…`druid_n10`, `merchant_n1`…, `monk_n1`… y `elementalist_n1`… del árbol unificado (J). Mientras no
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

## Tanda 1 — Druida · naturaleza · fondo #182519

**{ROL}** = Druida; **{DESCRIPCIÓN}** = un guardián de la naturaleza que cambia de forma y resiste.

| Rampa | Oscuro → claro |
|---|---|
| Verde bosque | #0F2415 · #24502A · #4C8F3F · #96D06A · #E2F5BE |
| Corteza | #2A1B10 · #55391F · #8A5E33 · #C79257 |
| Luz de luna | #2A3A55 · #6A86B8 · #C8D8F5 · #FFFFFF |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `druid_n1.png` | Hierba alta | Pasiva | **Pie** o huella sobre un mechón de **hierba alta** con una pequeña hoja protectora |
| `druid_n2.png` | Zarpas desgarradoras | Pasiva | **Tres zarpas** con gotas rojas y briznas de hierba |
| `druid_n3.png` | Látigo de lianas | Activa | **Liana** enrollada que agarra una silueta y la arrastra |
| `druid_n4.png` | Rebrote | Activa | **Brote** verde que crece de un tocón, con destellos de luz |
| `druid_n5.png` | Letargo | Pasiva | **Oso dormido** hecho un ovillo con una «z» de hojas |
| `druid_n6.png` | Rugido del oso | Activa | **Cabeza de oso** rugiendo con ondas de choque |
| `druid_n7.png` | Roble firme | Pasiva | **Tronco de roble** con raíces profundas y corteza gruesa |
| `druid_n8.png` | Forma de huargo | Activa | **Cabeza de lobo** de pelaje oscuro con ojos luminosos |
| `druid_n9.png` | Flujo estacional | Pasiva | **Hoja** dividida en cuatro estaciones (primavera, verano, otoño, invierno) |
| `druid_n10.png` | Despertar del bosque | Activa | **Árbol antiguo** con ojos luminosos y raíces que brotan del suelo; el más brillante |

---

## Tanda 2 — Mercader táctico · comercio · fondo #2A2512

**{ROL}** = Mercader táctico; **{DESCRIPCIÓN}** = un comerciante que convierte esmeraldas en poder.

| Rampa | Oscuro → claro |
|---|---|
| Oro | #3A2A08 · #7A5A14 · #C8962E · #F7D96A · #FFF6C8 |
| Esmeralda | #07301C · #0E6B3B · #17B25E · #6FE8A0 · #D4FFE6 |
| Cuero | #2A1B12 · #5A3A22 · #9A6A3A |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `merchant_n1.png` | Monedero | Pasiva | **Bolsa de cuero** atada con moneda de oro asomando |
| `merchant_n2.png` | Dividendos | Pasiva | **Esmeralda** con una flecha circular de interés dorada |
| `merchant_n3.png` | Moneda rebotona | Activa | **Moneda** de oro con tres trazos de rebote |
| `merchant_n4.png` | Subasta | Activa | **Mazo de subasta** golpeando sobre una esmeralda |
| `merchant_n5.png` | Crédito | Pasiva | **Pagaré** o papel con sello de cera y una esmeralda rota |
| `merchant_n6.png` | Caravana | Activa | **Carreta** de mercader con velas y polvo de camino |
| `merchant_n7.png` | Libro mayor | Pasiva | **Libro de cuentas** abierto con columnas de números y esmeraldas |
| `merchant_n8.png` | Inversión | Activa | **Gráfico** de barras ascendente hecho de monedas apiladas |
| `merchant_n9.png` | Cacería de recompensas | Pasiva | **Cartel** de «se busca» con una calavera y monedas |
| `merchant_n10.png` | Gran liquidación | Activa | **Montón de esmeraldas** estallando en una nube dorada; el más brillante |

---

## Tanda 3 — Monje · chi · fondo #2A1E14

**{ROL}** = Monje; **{DESCRIPCIÓN}** = un maestro de artes marciales sin armas.

| Rampa | Oscuro → claro |
|---|---|
| Naranja marcial | #3A1A08 · #8A3E14 · #D9772E · #FFB866 · #FFE8C8 |
| Chi blanco | #B8C8E0 · #E6EEFA · #FFFFFF |
| Tela | #2A2118 · #5A4A38 · #9A8668 |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `monk_n1.png` | Manos vacías | Pasiva | **Dos manos** abiertas, desnudas, con venda sencilla |
| `monk_n2.png` | Combinación | Pasiva | **Tres puños** consecutivos con estela creciente |
| `monk_n3.png` | Patada giratoria | Activa | **Pierna** en giro con trazo circular de viento |
| `monk_n4.png` | Onda de chi | Activa | **Esfera de energía** blanca lanzada desde una palma |
| `monk_n5.png` | Cuerpo sereno | Pasiva | **Flor de loto** sobre agua en calma |
| `monk_n6.png` | Palma explosiva | Activa | **Palma** con un símbolo que arde y empieza a estallar |
| `monk_n7.png` | Punto de presión | Pasiva | **Dedo** presionando un punto luminoso sobre un cuerpo |
| `monk_n8.png` | Postura del tigre | Activa | **Garra de tigre** en posición de combate con rayas de fuego |
| `monk_n9.png` | Eco de puño | Pasiva | **Puño** con tres ecos translúcidos que se expanden |
| `monk_n10.png` | Palma del vacío | Activa | **Palma** con un agujero negro y partículas absorbidas; la más brillante |

---

## Tanda 4 — Chamán · elementos · fondo #1A2030

**{ROL}** = Chamán; **{DESCRIPCIÓN}** = un maestro de los elementos que domina hasta tres.

| Rampa | Oscuro → claro |
|---|---|
| Fuego | #3A0E06 · #8F2A0E · #E8601E · #FFAA4A · #FFE08A |
| Hielo | #12334A · #2C6A96 · #5FB0DA · #A9E3F5 · #EAFBFF |
| Rayo y viento | #3A3560 · #7F78B8 · #FFF176 · #E0F7FA |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `elementalist_n1.png` | Cambio de marea | Pasiva | **Ola** que gira en círculo tomando colores de fuego y hielo |
| `elementalist_n2.png` | Eco del elemento | Pasiva | **Llama** con dos ecos concéntricos |
| `elementalist_n3.png` | Lanza elemental | Activa | **Lanza** envuelta en llama, hielo y rayo |
| `elementalist_n4.png` | Muro elemental | Activa | **Muro** de piedra con grietas de lava y escarcha |
| `elementalist_n5.png` | Reacción en cadena | Pasiva | **Eslabones** que se encienden uno tras otro con chispas |
| `elementalist_n6.png` | Tótem elemental | Activa | **Tótem** de piedra con tres runas de colores |
| `elementalist_n7.png` | Fusión estable | Pasiva | **Dos esferas** de colores distintos fundidas en una estable |
| `elementalist_n8.png` | Forma elemental | Activa | **Silueta** humana hecha de fuego, agua y viento |
| `elementalist_n9.png` | Armonía elemental | Pasiva | **Triángulo** con un elemento en cada vértice unidos por luz |
| `elementalist_n10.png` | Elementos desatados | Activa | **Explosión** con cuatro colores elementales en espiral; la más intensa |

---

## Integración (cuando llegue el arte)

1. Copiar los PNG a `tools/textures/source/nodes/<rol>/`.
2. Un generador con `--check` (como `gen_role_icons.py`) los valida (32×32, RGBA, sin semitransparencias) y los copia a
   `textures/gui/node/` con el id como nombre.
3. `NodeIconRenderer` dibuja los de 32×32 a la mitad; si falta el PNG, sigue el objeto de respaldo de `NodeIcons`.
