# Prompt de arte — Iconos de las 40 habilidades nuevas de Lancero, Espadachín, Paladín y Nigromante (v67, F4)

Encargo para el generador de arte (ChatGPT), igual que el de `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`. Son los iconos de los
nodos `lancer_n1`…`lancer_n10`, `swordsman_n1`…, `paladin_n1`… y `necromancer_n1`… del árbol unificado (J). Mientras no
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

## Tanda 1 — Lancero · alcance · fondo #1A2530

**{ROL}** = Lancero; **{DESCRIPCIÓN}** = un guerrero de asta larga que golpea desde lejos.

| Rampa | Oscuro → claro |
|---|---|
| Acero frío | #1B2430 · #3B4F66 · #6E8CA8 · #B4CBDD · #F0F8FF |
| Madera de asta | #2B1D12 · #55391F · #8A5E33 · #C79257 |
| Marca roja | #4A1010 · #8F1F1F · #E04A3A · #FFB0A0 |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `lancer_n1.png` | Mantener la distancia | Pasiva | **Lanza larga** apuntando al frente con una **flecha de distancia** doble sobre ella |
| `lancer_n2.png` | Punta clavada | Pasiva | **Punta de lanza** clavada en un blanco con **marca roja** brillando alrededor |
| `lancer_n3.png` | Jabalina | Activa | **Jabalina** en vuelo con **estela** de aire y un pequeño remolino en la punta |
| `lancer_n4.png` | Muro de picas | Activa | **Tres picas** clavadas en el suelo en abanico, con tierra levantada en la base |
| `lancer_n5.png` | Ensartar | Pasiva | **Dos siluetas** atravesadas en fila por una sola lanza |
| `lancer_n6.png` | Abanico de picas | Activa | **Cinco puntas** de lanza abiertas en abanico desde un punto común |
| `lancer_n7.png` | Primer contacto | Pasiva | **Punta de lanza** chocando con un **destello** blanco de impacto |
| `lancer_n8.png` | Lanza ancestral | Activa | **Lanza antigua** con runas doradas y **aura** que la alarga más allá del marco |
| `lancer_n9.png` | Doble estocada | Pasiva | **Dos puntas** paralelas, la segunda semitransparente en tonos, como un eco |
| `lancer_n10.png` | Ráfaga de jabalinas | Activa | **Lluvia de jabalinas** cayendo en diagonal sobre un círculo de blancos; la más intensa |

---

## Tanda 2 — Espadachín · esgrima · fondo #251A1A

**{ROL}** = Espadachín; **{DESCRIPCIÓN}** = un duelista ágil que depende del ritmo y de la espada.

| Rampa | Oscuro → claro |
|---|---|
| Acero pulido | #1B2430 · #4A5E78 · #8AA2BE · #CFE0F0 · #FFFFFF |
| Cuero y empuñadura | #2A1B12 · #5A3A22 · #9A6A3A |
| Corte de viento | #14304A · #2F6FA8 · #7FC4F0 · #E6F6FF |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `swordsman_n1.png` | Duelo | Pasiva | **Dos espadas cruzadas** con un círculo estrecho alrededor, solo dos figuras |
| `swordsman_n2.png` | Filo penetrante | Pasiva | **Hoja** que atraviesa una **placa de armadura** abriéndola con una grieta |
| `swordsman_n3.png` | Tajo ascendente | Activa | **Arco de corte** ascendente con una silueta lanzada hacia arriba |
| `swordsman_n4.png` | Molinete | Activa | **Espada girando** con **círculos** de movimiento y una flecha desviada |
| `swordsman_n5.png` | Racha de victorias | Pasiva | **Cinco muescas** en una hoja, la última brillando |
| `swordsman_n6.png` | Salto de espadachín | Activa | **Espadachín** en el aire con la espada bajada hacia un blanco; estela de salto |
| `swordsman_n7.png` | Voluntad de acero | Pasiva | **Corazón** envuelto en una **armadura** de acero con una grieta que no se rompe |
| `swordsman_n8.png` | Estado de flujo | Activa | **Espada** en una espiral de **viento** azul con trazos de velocidad |
| `swordsman_n9.png` | Desenvainado | Pasiva | **Espada** saliendo a medias de una **vaina** con un destello de filo |
| `swordsman_n10.png` | Corte final | Activa | **Tajo gigante** en diagonal que parte el icono en dos; el más brillante |

---

## Tanda 3 — Paladín · luz · fondo #2A2514

**{ROL}** = Paladín; **{DESCRIPCIÓN}** = un caballero sagrado que protege y cura al grupo.

| Rampa | Oscuro → claro |
|---|---|
| Oro sagrado | #3A2A08 · #7A5A14 · #C8962E · #F7D96A · #FFF6C8 |
| Acero bendito | #26303C · #566A82 · #A4B8D0 · #EEF4FA |
| Luz blanca | #B8C8E0 · #E6EEFA · #FFFFFF |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `paladin_n1.png` | Fe contra la oscuridad | Pasiva | **Escudo** dorado con un **sol** que ahuyenta una calavera en sombra |
| `paladin_n2.png` | Triunfo compartido | Pasiva | **Tres manos** unidas con un **corazón** dorado en el centro |
| `paladin_n3.png` | Rayo de luz | Activa | **Rayo** blanco-dorado que sale de una mano hacia delante, con partículas |
| `paladin_n4.png` | Golpe de escudo | Activa | **Escudo** golpeando con **ondas de choque** y estrellitas de aturdimiento |
| `paladin_n5.png` | Oración | Pasiva | **Manos juntas** de rodillas con un **halo** y motas de luz que suben |
| `paladin_n6.png` | Muro de luz | Activa | **Muro** de luz translúcida con runas, una flecha rebotando en él |
| `paladin_n7.png` | Vínculo de escudo | Pasiva | **Dos escudos** unidos por un hilo de luz dorada |
| `paladin_n8.png` | Estandarte sagrado | Activa | **Estandarte** blanco y dorado clavado en el suelo, con rayos de luz |
| `paladin_n9.png` | Ira justa | Pasiva | **Martillo** o puño envuelto en **llamas** blancas que sube con un corazón herido |
| `paladin_n10.png` | Columna de luz | Activa | **Columna** de luz celestial que cae del cielo sobre un círculo en el suelo; la más brillante |

---

## Tanda 4 — Nigromante · sombra · fondo #14221A

**{ROL}** = Nigromante; **{DESCRIPCIÓN}** = un invocador oscuro que domina esbirros y almas.

| Rampa | Oscuro → claro |
|---|---|
| Verde necrótico | #0E1F14 · #1F4A2C · #3E8A4E · #8AD08A · #D4F5D4 |
| Hueso | #3A3326 · #8A7A5A · #CDBF9A · #F2EBD0 |
| Sombra violeta | #1A1030 · #3B2468 · #6A44B0 · #A98BE8 |

| Archivo | Habilidad | Tipo | Qué dibujar |
|---|---|---|---|
| `necromancer_n1.png` | Orden de ataque | Pasiva | **Mano esquelética** señalando con una **calavera** pequeña corriendo hacia el blanco |
| `necromancer_n2.png` | Banquete | Pasiva | **Costilla** o hueso mordido con **gotas** verdes y una chispa de maná |
| `necromancer_n3.png` | Mano de huesos | Activa | **Mano de huesos** emergiendo del suelo y agarrando algo, con tierra |
| `necromancer_n4.png` | Sacrificio | Activa | **Calavera** que estalla en esquirlas verdes con un **destello** central |
| `necromancer_n5.png` | Aura putrefacta | Pasiva | **Nube verde** podrida con moscas y **una calavera** pequeña desvaneciéndose |
| `necromancer_n6.png` | Tierra maldita | Activa | **Círculo ritual** en el suelo con **manos** de sombra y humo verde |
| `necromancer_n7.png` | Resurgir | Pasiva | **Calavera** saliendo de un **ataúd** o tumba con **chispas** de vida |
| `necromancer_n8.png` | Frenesí necrótico | Activa | **Dos cráneos** con ojos rojos brillantes y **rayos** de energía verde |
| `necromancer_n9.png` | Vínculo de huesos | Pasiva | **Hueso** largo atado a otro con un **hilo** de luz verde, como una cadena |
| `necromancer_n10.png` | Tormenta de almas | Activa | **Remolino** de almas verdosas girando alrededor de una calavera; el más brillante |

---

## Integración (cuando llegue el arte)

1. Copiar los PNG a `tools/textures/source/nodes/<rol>/`.
2. Un generador con `--check` (como `gen_role_icons.py`) los valida (32×32, RGBA, sin semitransparencias) y los copia a
   `textures/gui/node/` con el id como nombre.
3. `NodeIconRenderer` dibuja los de 32×32 a la mitad; si falta el PNG, sigue el objeto de respaldo de `NodeIcons`.
