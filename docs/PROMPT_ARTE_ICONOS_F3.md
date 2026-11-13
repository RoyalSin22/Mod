# Prompt de arte — Iconos de las 40 habilidades nuevas de Mago, Asesino, Herbolista e Ingeniero (v67, F3)

Encargo para el generador de arte (ChatGPT), igual que el de `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`. Son los iconos de los
nodos `mage_n1`…`mage_n10`, `assassin_n1`…, `herbalist_n1`… y `engineer_n1`… del árbol unificado (J). Mientras no
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

## Tanda 1 — Mago · arcano · fondo #201B30

**{ROL}** = Mago; **{DESCRIPCIÓN}** = un lanzador de hechizos que vive de su reserva de maná.

| Rampa | Oscuro → claro |
|---|---|
| Violeta arcano | #1E1236 · #3F2A78 · #6B4FC4 · #A78BFA · #E4D7FF |
| Hielo | #12334A · #2C6A96 · #5FB0DA · #A9E3F5 · #EAFBFF |
| Cristal y estrella | #3A3560 · #7F78B8 · #C8C3F0 · #FFFFFF |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `mage_n1.png` | Plenitud arcana | Pasiva | Un **cáliz o frasco completamente lleno** de energía violeta que rebosa, con chispas arriba |
| `mage_n2.png` | Escarcha latente | Pasiva | **Copo de nieve** cristalino con un núcleo violeta, rodeado de esquirlas de hielo |
| `mage_n3.png` | Rayo encadenado | Activa | **Rayo en zigzag** que salta entre tres nodos brillantes unidos por arcos eléctricos |
| `mage_n4.png` | Escudo especular | Activa | **Escudo redondo de cristal** que refleja un destello; una flecha de rebote en el borde |
| `mage_n5.png` | Concatenación | Pasiva | **Tres runas** distintas unidas por un hilo de luz formando un eslabón |
| `mage_n6.png` | Tormenta de granizo | Activa | **Nube oscura** con piedras de granizo cayendo en diagonal y escarcha en los bordes |
| `mage_n7.png` | Reserva de emergencia | Pasiva | **Frasco pequeño casi vacío** con una única gota brillante y una **chispa** de rescate |
| `mage_n8.png` | Forma de arcano puro | Activa | **Silueta de mago** hecha de luz blanco-violeta, sin detalles, con aura de runas flotantes |
| `mage_n9.png` | Maná cristalizado | Pasiva | **Cristal grande** facetado con una grieta luminosa y fragmentos que estallan alrededor |
| `mage_n10.png` | Nova estelar | Activa | **Estrella de ocho puntas** en el centro de un anillo de explosión; el icono más brillante |

---

## Tanda 2 — Asesino · sigilo · fondo #1A202A

**{ROL}** = Asesino; **{DESCRIPCIÓN}** = un cazador de sombras que golpea por la espalda y desaparece.

| Rampa | Oscuro → claro |
|---|---|
| Sombra azulada | #0B0E16 · #1C2233 · #36405A · #5F6D8E · #B0BBD4 |
| Acero frío | #20232B · #454B58 · #787F8E · #B1B7C3 · #E8ECF2 |
| Veneno | #0F2A12 · #1F5E24 · #3FA047 · #8FDC7E |
| Acento sangre (poco) | #5A0A12 · #A3161E · #D4352B |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `assassin_n1.png` | Punto débil | Pasiva | **Daga** señalando una **mancha verde** (veneno) sobre una silueta; un blanco pequeño |
| `assassin_n2.png` | Eslabón | Pasiva | **Dos eslabones de cadena** enlazados, uno ya abierto, con un reloj de arena diminuto |
| `assassin_n3.png` | Humo cegador | Activa | **Bomba de humo** negra estallando en volutas, con unos ojos tachados dentro |
| `assassin_n4.png` | Corte relámpago | Activa | **Tajo diagonal** de dos filos con estela blanca, y una silueta que desaparece al lado |
| `assassin_n5.png` | Sombra persistente | Pasiva | **Silueta de capucha** disolviéndose a trozos, con un destello de filo apareciendo |
| `assassin_n6.png` | Cadena de sombras | Activa | **Tres siluetas** en arco, unidas por una línea discontinua, cada una con una daga |
| `assassin_n7.png` | Veneno concentrado | Pasiva | **Frasco** con veneno verde muy oscuro y espeso, con una gota densa cayendo |
| `assassin_n8.png` | Velo de muerte | Activa | **Capa traslúcida** (por tramado) con una **calavera** apenas visible dentro |
| `assassin_n9.png` | Cosecha silenciosa | Pasiva | **Hoz** pequeña con un **pétalo negro** y un hilo de alma subiendo |
| `assassin_n10.png` | Noche eterna | Activa | **Luna nueva** negra con halo, rodeada de un **círculo de oscuridad**; el icono más oscuro y con más contraste |

---

## Tanda 3 — Herbolista · curación · fondo #182720

**{ROL}** = Herbolista; **{DESCRIPCIÓN}** = un sanador del bosque que cura con plantas y envenena con ellas.

| Rampa | Oscuro → claro |
|---|---|
| Verde bosque | #0E2A18 · #1F5A2C · #3E9A4D · #84D17C · #D9F5B8 |
| Madera y raíz | #2B1A12 · #4E3020 · #7A4B2E · #A8703F |
| Flor | #6A1B3E · #C2457C · #F0A6C8 · #FFE6F0 |
| Veneno | #2A3A0A · #5E8A1A · #A6D13F |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `herbalist_n1.png` | Primavera | Pasiva | **Brote** con dos hojas y una **flor rosada** abriéndose; rayos de luz de sol |
| `herbalist_n2.png` | Polinización | Pasiva | **Abeja** sobre una flor, con polvo dorado y una chispa que sube |
| `herbalist_n3.png` | Semilla explosiva | Activa | **Semilla grande** con espinas que estalla en esporas y pequeñas hojas |
| `herbalist_n4.png` | Raíz curativa | Activa | **Raíces** que emergen del suelo formando un círculo, con motas de luz verde |
| `herbalist_n5.png` | Corteza viva | Pasiva | **Tronco** con una cara de corteza gruesa y un brote verde naciendo de una grieta |
| `herbalist_n6.png` | Plaga de langostas | Activa | **Tres langostas** verde oscuro en vuelo sobre una hoja mordida |
| `herbalist_n7.png` | Antídoto universal | Pasiva | **Frasco** con un líquido claro y una **hoja** de menta dentro; una cruz verde sutil (sin letras) |
| `herbalist_n8.png` | Floración colosal | Activa | **Flor enorme** abierta con muchos pétalos y polen en estallido; la más grande y luminosa |
| `herbalist_n9.png` | Raíz profunda | Pasiva | **Árbol pequeño** con raíces largas que se hunden y se ramifican bajo el suelo |
| `herbalist_n10.png` | Lluvia de semillas vivas | Activa | **Lluvia** de semillas brillantes cayendo desde una nube, cada una con un brote diminuto |

---

## Tanda 4 — Ingeniero · artilugios · fondo #2A211A

**{ROL}** = Ingeniero; **{DESCRIPCIÓN}** = un inventor con artilugios de cobre y engranajes que vive de su energía.

| Rampa | Oscuro → claro |
|---|---|
| Cobre | #2E160C · #6B3418 · #B05B2A · #E88B45 · #FFC58A |
| Hierro | #1F2229 · #444A55 · #767D8A · #AEB4BF · #E6EAF0 |
| Energía azul | #0A2A44 · #1C6AA8 · #48B4F0 · #B8EAFF |
| Chispa | #6B4A00 · #E0A000 · #FFE066 |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `engineer_n1.png` | Aislante | Pasiva | **Cable de cobre** envuelto en capas de goma negra, con una chispa que rebota en la superficie |
| `engineer_n2.png` | Reciclaje | Pasiva | **Flecha circular** hecha de engranajes, con una pequeña pila azul en el centro |
| `engineer_n3.png` | Mina de proximidad | Activa | **Mina** plana de hierro con una **luz roja** y tres antenas cortas |
| `engineer_n4.png` | Dron de apoyo | Activa | **Dron** con dos hélices, un ojo-cámara azul y un haz de disparo |
| `engineer_n5.png` | Calor residual | Pasiva | **Engranaje** al rojo vivo con vapor y brasas que se desprenden |
| `engineer_n6.png` | Domo protector | Activa | **Semicúpula** de energía azul sobre un pequeño generador, con proyectiles rebotando |
| `engineer_n7.png` | Modo ahorro | Pasiva | **Pila** con una **hoja verde** o un indicador de carga completo (sin letras) |
| `engineer_n8.png` | Exotraje | Activa | **Pechera mecánica** con articulaciones, tubos de cobre y un núcleo azul brillante |
| `engineer_n9.png` | Cadena de montaje | Pasiva | **Cinta transportadora** con tres piezas, y un **brazo robótico** que ensambla una |
| `engineer_n10.png` | Torre Tesla | Activa | **Torre** de cobre con una esfera en la punta que descarga **rayos** azules; la más brillante |

---

## Integración (cuando llegue el arte)

1. Copiar los PNG a `tools/textures/source/nodes/<rol>/`.
2. Un generador con `--check` (como `gen_role_icons.py`) los valida (32×32, RGBA, sin semitransparencias) y los copia a
   `textures/gui/node/` con el id como nombre.
3. `NodeIconRenderer` dibuja los de 32×32 a la mitad; si falta el PNG, sigue el objeto de respaldo de `NodeIcons`.
