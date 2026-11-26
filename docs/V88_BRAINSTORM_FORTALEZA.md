# V88 — Lluvia de ideas: la Fortaleza de las Tres Torres (ruinas)

Estado: **ideas para elegir, sin decisiones ni código.** Pedido del autor del 2026-11-26. Complementa `docs/PROMPT_CODEX_REDISENO_ESTRUCTURAS.md`
y reemplaza la forma de `docs/V86_DUNGEON_COLOSO.md` §3 (la mecánica de jefes, recompensa, una vez por estructura y D-37/D-39 se conserva).

## 1. Concepto

Una **fortaleza de la Vigilia de las Tres Almenas**, medieval-gótica, **arruinada por las batallas**. Se ve de lejos por **tres torres**: la **Torre del Mariscal**
(la más alta, jefe final) y **dos Torres de Vigilancia** (subjefes). Entre ellas, un recinto amurallado con patios, salas, puentes y plataformas. El jugador
puede recorrerlo en el orden que quiera, pero **no entra a la Torre del Mariscal hasta vencer a los dos subjefes** (como hoy, D-38).

## 2. Escala y plano (propuesta)

| Parte | Propuesta | Nota |
|---|---|---|
| Huella total | ~96 × 96 (rango a discutir: 80–128) | hoy 31 × 31 |
| Torres de Vigilancia | 2, en vértices opuestos del recinto, ~13 × 13 y ~38 de alto | atalayas con sala de vigía arriba |
| Torre del Mariscal | 1, al fondo, ~17 × 17 y ~64 de alto | la más alta; silueta inconfundible desde 100+ bloques |
| Distancia entre torres | 45–60 bloques | recorridos por el recinto, murallas y puentes |
| Recinto | muralla con brechas, patio de armas central, barbacana (entrada) | la entrada principal, sur |

Esquema orientativo (vista cenital, no a escala):

```
        N
   [Torre Vigilancia O]  ~~ muralla ~~  [Torre Vigilancia E]
        |   capilla   patio de armas   cuarteles  |
        |   biblioteca       (combate)    comedor  |
        +---- puente / adarve ----+---- escalinata ---+
                       |   Gran Salón   |
                  [TORRE DEL MARISCAL]  (jefe final)
        S: barbacana (entrada)
```

(Dónde va cada cosa es una propuesta; Codex presentará 2–3 variantes.)

## 3. Cómo generar algo tan grande (decisión técnica clave)

Un `Feature` del mod solo escribe en 31×31. Opciones:

| Opción | Cómo | Pros | Contras |
|---|---|---|---|
| **A. Plano puro por columna (recomendada)** | Una clase pura `FortressLayout` responde «¿qué bloque va en (x,y,z) relativo al origen?». Cada chunk que toca la fortaleza escribe su parte; el origen sale de `RegionGridPlacement` (un punto por región) | Encaja con el patrón del mod (puro + JUnit); determinista; sin cambiar el sistema de generación | Hay que cuidar el orden de chunks y la cimentación en terreno desigual; el controlador debe colocarse en el chunk que contiene su posición |
| **B. `Structure` propia (piezas)** | Un `StructureType` con piezas, como las aldeas o fortalezas vanilla | Escala bien, soporta chunks arbitrarios, `/locate` nativo | Mucho código nuevo, aún sin precedente en el mod; rompe `StructureIndex`/`StructureLocateRules` actuales |
| **C. Plantillas NBT unidas** | Piezas guardadas en archivos `.nbt` y un generador que las une | Se editan con herramientas visuales | Sin precedente; los NBT binarios no se revisan en un diff |

Recomendación: **A**, con la cimentación rellena y un tope de desnivel para no generar donde no cabe.

## 4. Zonas y cuartos

Cada cuarto tiene función narrativa (restos de la vida de la orden) y casi todos tienen **combate o cofre**.

| Zona | Cuartos | Contenido de juego |
|---|---|---|
| Barbacana y patio de armas | puerta quebrada, torreones de entrada, establos, herrería caída, pozo | combate abierto con oleadas de élites; primer cofre |
| Ala de servicio | cocina, despensa, **comedor** (mesas largas, mesa de honor), bodega | combate en pasillos; botín menor y comida |
| Ala residencial | **dormitorios** (literas, cofres), **baño** (tinas de cobre, pilas), sala de estar, capilla íntima | descanso/«sala segura» opcional, enemigos emboscados |
| Ala de estudio | **biblioteca** (estanterías de dos pisos, escalera de caracol, atril con diario), sala de mapas, scriptorium | puzle ligero/lore (nota legible), cofre de pergaminos |
| Ala militar | armería, cuarteles, sala de entrenamiento, calabozos | combate de élites con armadura visible; cofre de armas |
| Capilla gótica | nave con arcos apuntados y vidrieras rotas, coro, cripta pequeña | mini-jefe opcional **sin** recompensa de poder; solo decoración y lore |
| Adarves y puentes | murallas con almenas, **plataformas** colgantes, puentes de madera/piedra, escaleras | verticalidad, combate con arqueros, caídas controladas |
| Torres de Vigilancia (×2) | escalera, sala del vigía, celda del capitán, balcón | **subjefe** (Guardián Desmontado) arriba, un cofre de torre |
| Torre del Mariscal | gran salón, trono vacío, sala de guerra, ascenso por plantas, azotea | jefe final (Coloso Sin Rostro) y **gran botín** |

## 5. Ruinas y gótico (lenguaje visual)

- **Ruina legible:** muros con brechas y sin techo en partes; vigas colgando; escombros de piedra y grava; telarañas; musgo y raíces.
- **Gótico:** arcos apuntados, ventanales con vidrieras rotas, contrafuertes, pináculos, gárgolas, rosetón en la capilla, columnas con haces.
- **Medieval:** estandartes desgarrados, barriles, mesas largas, chimeneas con ceniza, armaduras decorativas, antorchas de alma ya usadas en el mod.
- **Luz:** pocas fuentes; braseros apagados; luz azul de «alma» solo en las salas de jefe y mini-jefe (para que se lean).
- **Materiales:** piedra, piedra musgosa, deepslate, ladrillos de piedra agrietados, madera oscura; acentos de hierro y cobre oxidado.

## 6. Combate y ritmo

| Tramo | Enemigos | Intención |
|---|---|---|
| Entrada y patio | oleadas de élites (cap del mod) | calentamiento; no se puede evitar todo |
| Alas (opcionales) | grupos pequeños y emboscadas | recompensa de botín por explorar |
| Torres de Vigilancia | **subjefe** (Guardián Desmontado, escalado por anillo) | 2 de 2 para abrir la torre del Mariscal |
| Gran Salón | élites + ruinas del Coloso | antesala de la batalla final |
| Azotea / sala alta | **Coloso Sin Rostro** | sala amplia: el jefe es grande y no debe atravesar muros |

Dificultad alta, como ya decidió el autor. **El orden de las torres de vigilancia es libre.**

## 7. Botín (propuesta, a decidir; no cambia los defaults de balance sin preguntar)

| Dónde | Qué |
|---|---|
| Cofres de las alas | botín moderado (materiales, comida, pergaminos) |
| Cofre de cada Torre de Vigilancia | botín mayor + Corazón de campeón |
| **Gran botín del Coloso** | Plantilla de ascensión (como hoy) **más** un cofre grande de la torre con materiales raros, Fragmentos de alma y Corazones de campeón; un cofre por jugador cercano (hasta 5) |
| Logro | `three_towers` (ya existe) |

## 8. Riesgos y vigilancias

1. **Escala:** una fortaleza de ~96×96 cambia generación, `/aptitudes locate|place`, el controlador y el reparto de chunks; es la parte más cara (§3).
2. **Terreno:** colocación solo en zonas casi planas o con cimentación/rampas generosas; sin océano; rechazo seguro si no cabe.
3. **Rendimiento:** presupuesto de mobs por zona (como `eclipseZoneMobCap`), enemigos despiertos solo cerca del jugador y **una sola vez por planta o cuarto**.
4. **Jefe en espacio cerrado:** la sala del Coloso se diseña para su tamaño real y se prueba en partida (duda abierta del autor).
5. **Distancia y carga de chunks:** los jefes y su controlador deben vivir en chunks que se carguen al acercarse.
6. **Exclusiones:** nada de linajes, razas ni pactos (AGENTS §3); «Vigilia», «Mariscal» y «orden» son solo ambientación.
7. **Lore y textos** (biblioteca con notas, carteles, diario del Mariscal): los escribe Codex, en `lore.aptitudes.*`.
8. **Sin probar en partida:** el diseño actual de las torres tampoco se ha jugado; lo aprendido en las pruebas (escaleras, Coloso) puede cambiar este plano.

## 9. Decisiones para el autor

1. ¿Escala objetivo? (grande ~96×96 recomendado; mediana ~64×64 si prefieres algo más ligero).
2. ¿Generación por plano puro por columna (A, recomendada) o `Structure` propia (B)?
3. ¿Qué cuartos son **imprescindibles** y cuáles quitamos (biblioteca, baño, dormitorios y comedor ya los pediste)?
4. ¿Hay una capilla con mini-jefe opcional o todo el combate de jefe es solo el Coloso y los dos Guardianes?
5. ¿El «gran botín» solo tras el Coloso (como dices) o también un cofre en cada torre de vigilancia?
