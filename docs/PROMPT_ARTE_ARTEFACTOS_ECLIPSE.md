# Prompt de arte — Artefactos del Eclipse (Resonador del Umbral y Astrolabio del Ocaso)

Encargo para el generador de arte (ChatGPT), en la línea de `docs/PROMPT_ARTE_ARMAS.md` (de ahí salen la dirección artística y la técnica de
píxel: léelas, aquí solo se resume). Son **los dos objetos que convocan el Eclipse desde la Hoguera** y hoy **no tienen textura ni modelo**
(se ven como «modelo ausente»). Sin ellos el momento más importante de la cadena narrativa se ve roto.

| Objeto | Id de registro (**no cambia**, es el nombre del archivo) | Nombre en el juego |
|---|---|---|
| Artefacto que se sacrifica en la Hoguera | `resonador_del_umbral` | «Resonador del Umbral» — **provisional, se va a cambiar** (ver §6) |
| Disco de convocatoria | `sunset_astrolabe` | «Astrolabio del Ocaso» |

---

## 1. Regla de oro: diseño ORIGINAL

El Resonador cumple la función de un objeto icónico de otra obra (un artefacto que se sacrifica para abrir una puerta a algo que no debería
abrirse). **Su diseño no puede parecerse a él.** Por eso el lore lo describe como *«disco de metal pálido con un anillo de luz hueco en el centro;
no un ojo, no una cara ni un huevo»*. Antes de entregar cada imagen, haz estas tres comprobaciones; si falla alguna, se descarta:

1. **Test del ojo:** ¿el centro parece una pupila, un iris o una mirada? → mal. El centro del Resonador está **vacío** (se ve el fondo), no hay «pupila».
2. **Test del rostro/huevo:** ¿hay dos puntos y una raya que sugieran una cara?, ¿la silueta es ovoide, de lágrima o de huevo con ornamentos? → mal. La silueta es un **disco redondo**.
3. **Test de la copia:** ¿se parece a un amuleto con pinchos o rasgos orgánicos alrededor de un núcleo? → mal. Nada orgánico: es metal grabado.

## 2. Dirección artística (resumen de `PROMPT_ARTE_ARMAS.md` §1–2)

Fantasía oscura, melancólica y estoica: objetos **viejos, usados, pesados, con historia**, de manos que ya no existen. Paleta contenida y desaturada
(metal frío, bronce quemado, hueso, ceniza) con **un único acento de luz** que parece *respirar*. Nada de brillo de plástico, simetría limpia ni degradados.
Técnica obligatoria: **luz única arriba a la izquierda**, rampas de 4–5 tonos con desplazamiento de tono (sombras frías, luces cálidas), contorno selectivo (no negro puro),
píxeles agrupados sin *jaggies* ni píxeles huérfanos, **sin transparencia ni halos difuminados** (el brillo se dibuja con píxeles opacos), 8–12 detalles pequeños
subordinados a la silueta. **La silueta debe reconocerse rellena de un solo color y reducida a 16×16.**

## 3. Ficha: Resonador del Umbral

- **Qué es (lore):** artefacto de comunicación de la Primera Era; la conexión externa que algo en el Velo necesitaba. Se **sacrifica** en la Hoguera para llamar al Eclipse.
- **Silueta:** **anillo grueso** (un disco con el centro hueco), con un asa pequeña arriba para colgarlo. Que se lea como **un aro**, no como un disco macizo.
- **Material:** metal **pálido y frío** (acero grisáceo azulado, plata ennegrecida en las juntas), picado, con pátina oscura en los huecos.
- **Acento de luz:** un **anillo fino de luz blanco-dorada** (la corona de un eclipse) que recorre el borde interior del aro; núcleo de 1 píxel casi blanco; **un solo píxel de luz violeta** (el Velo) en el lado opuesto.
- **El rasgo único (la historia):** el anillo de luz **está roto**: tiene una **abertura de 2–3 píxeles** hacia la posición de las 2 en punto, y por ella «se escapa» una chispa suelta. Es lo que lo distingue de un ojo y de un adorno cerrado.
- **Detalles:** 3 muescas radiales en el borde exterior (no simétricas), runas grabadas de 2×2 medio borradas, una grieta fina, un eslabón de cadena roto en el asa, ceniza en las juntas.
- **Fondo del hueco:** el color de fondo (en la entrega, magenta transparente), **nunca** un círculo oscuro relleno.

## 4. Ficha: Astrolabio del Ocaso

- **Qué es:** un instrumento de navegación de los gremios, reconvertido para señalar el instante del eclipse. Se **rompe** al usarlo en la Hoguera.
- **Silueta:** **disco macizo** con un **anillo de suspensión arriba** y un **brazo puntero (alidada) en diagonal** que lo cruza y asoma por un lado. (Así se distingue a 16 px del Resonador, que es un aro hueco.)
- **Material:** **bronce ennegrecido** (marrón oscuro, verdín en las juntas, ocre en los cantos), con una pieza de hierro (el eje central) y **un fragmento de Eclipse** engastado: una esquirla roja oscura con núcleo casi blanco.
- **Acento de luz:** el **sol eclipsado grabado**: un disco negro que tapa a medias un sol, con una **corona fina naranja-brasa** asomando por un borde. Un solo punto de luz cálida (la esquirla).
- **Detalles:** escala de rayitas en el borde (como un reloj de sol), una **retícula calada** (*rete*) con 2–3 puntas de estrella, remaches, una muesca en el borde, el puntero ligeramente torcido.
- **Qué NO es:** no un reloj, no una brújula de bolsillo moderna, no un ojo (el sol eclipsado es una **corona**, no una pupila).

## 5. Prompt para pegar en ChatGPT (uno por imagen; empieza una conversación nueva)

> Necesito **UN objeto en pixel art de máxima calidad** para un mod de Minecraft de **fantasía oscura**, al estilo de los objetos de *Dark Souls* /
> *Elden Ring*: viejo, usado, pesado, con historia; tono **estoico y dinámico**, paleta desaturada con **un único acento de luz**. Acabado
> profesional, trabajado píxel a píxel: nada de iconos genéricos, plástico brillante, degradados ni vectores reescalados.
>
> **FORMATO (muy importante):** imagen de **1024×1024 px** con fondo **magenta liso #FF00FF** (será transparente). Dibuja el objeto como una
> **rejilla de 32×32 píxeles ampliada ×32**: cada «píxel» es un bloque nítido de **32×32 px de un solo color**, con la rejilla de píxeles
> visible. El objeto, **centrado**, ocupa unas **26×26 celdas**. **Un solo objeto, una sola vista frontal**, sin texto, sin marco, sin sombra proyectada, sin
> ningún elemento más.
>
> **TÉCNICA:** una única luz arriba a la izquierda (borde iluminado de 1 píxel); rampas de 4–5 tonos por material con desplazamiento de tono
> (sombras hacia azul/violeta, luces hacia amarillo/naranja pálido); contorno selectivo oscuro teñido del color del material (no negro puro);
> píxeles agrupados con líneas de escalón regular, **sin píxeles sueltos ni dientes**; **sin transparencia ni halos difuminados** (el brillo se dibuja
> con píxeles opacos); entre 8 y 12 detalles pequeños que no estropeen la silueta. **La silueta debe reconocerse rellena de un solo color y
> reducida a 16×16.**
>
> **PROHIBIDO:** ojos, pupilas, rostros, huevos, lágrimas, rasgos orgánicos, pinchos alrededor de un núcleo, símbolos de otras obras, texto.
>
> **EL OBJETO:** *(pega aquí la ficha de §3 o §4, sin cambiar nada)*

Genera **4 variantes** de cada objeto y elige la que pase los tres tests de §1 y la comprobación de 16 px (reduce la imagen a 16×16 y mírala: ¿se sigue entendiendo qué es?).

## 6. Entrega y nombre

**Archivos:** `resonador_del_umbral.png` y `sunset_astrolabe.png` (el nombre del archivo es el **id de registro**, que **no cambia aunque cambie el nombre en el juego**).
Entrega la imagen de 1024×1024 tal cual; no la reduzcas tú. Claude escribe el importador (`tools/textures/import_item_art.py`, con `--check` y `--preview`), que
detecta el objeto, lo normaliza a **32×32** (inventario y mano; los iconos de armas ya usan 32) y deja la fuente en `tools/textures/source/items/`. Después de
importarlas hay que añadir su modelo (`models/item/<id>.json`, `item/generated`) y comprobar con `/give` en inventario, mano, suelo y marco.

**El nombre del Resonador se va a cambiar** (D-11, abierta; la decisión es del autor). El nombre del juego son solo claves de idioma (`item.aptitudes.resonador_del_umbral` y su
`.tooltip`, en los 5 idiomas con los generadores), así que cambiarlo es barato. Aparte, el término interno **«Behelit»** (clase `BeheritItem`, comentarios, documentos y
constantes de lore) debe sustituirse por uno propio. **No cambian**: el id de registro `resonador_del_umbral` ni el id de lore ya guardado por los jugadores (`behelit_lore_unlocked`, constante `LoreRules.LORE_BEHELIT_FOUND`),
porque están en mundos y progresos existentes; solo cambian los nombres visibles y los internos de clase.

Candidatas de nombre (el diseño de §3 es un **aro con el anillo de luz abierto**, y conviene que el nombre no lo contradiga):

| Nombre | Por qué | Riesgo |
|---|---|---|
| **Corona Quebrada** | El anillo de luz roto es una corona de eclipse partida | Suena a objeto de rey |
| **Anillo Hueco** | Descripción literal del diseño y de la «puerta» | Sencillo, poco épico |
| **Llamador del Velo** | Dice lo que hace (llama a lo del Velo) | «Llamador» es poco elegante |
| **Disco de la Fractura** | Usa un término del lore (la Fractura) | Contradice el aro hueco («disco») |
| Aguja del Silencio / Ancla de Eclipse (alternativas del lore) | Ya figuran en `07_BESTIARIO_TEMPLOS_ARTEFACTOS.md` | La aguja no es un aro; **ya existe el «Ancla del Velo»** (confusión) |
