# V90 — Catálogo de decoración y variantes: Torre en ruinas y Campamentos

Estado: **banco de ideas concretas para elegir; sin código.** Desarrolla `docs/V90_DISENO_TORRE_Y_CAMPAMENTOS.md` (decisiones en su §1 y §8). Todos los bloques son **vanilla**;
lo que no se menciona lo decide Codex al proponer. Sin linajes, razas ni pactos (`AGENTS.md` §3): «culto», «ritual» y «orden» son solo ambientación.

## 1. Paletas (anillo de Amenaza × bioma)

Anillo (cuánto te alejas de la aparición, `ThreatRules.ring`): decide el **material base** y la **ruina**.

| Anillo | Muros | Detalle | Suelo | Ambiente |
|---|---|---|---|---|
| 0–I | ladrillos de piedra, musgosos, adoquín | piedra tallada, hiedra, musgo | tablones de abeto, grava | hierba, flores secas, luz cálida |
| II–III | ladrillos de pizarra profunda, agrietados | azulejos de pizarra, hierro | pizarra pulida | cadenas, linternas, telarañas |
| IV–V | ladrillos de piedra negra pulida, agrietados | piedra negra dorada, oro, obsidiana llorosa | piedra negra pulida | luz de alma, brasas, cadenas rotas |

Bioma (cambia madera, techo y vegetación sin cambiar la forma):

| Bioma | Madera | Piedra | Extras |
|---|---|---|---|
| Llanura / bosque | abeto, roble | ladrillos | flores, helechos |
| Nieve / taiga | abeto | ladrillos con nieve | nieve, hielo, abeto con nieve |
| Pantano | manglar | ladrillos con musgo | nenúfares, enredaderas, hongos |
| Jungla | jungla | ladrillos musgosos, tallada | enredaderas, bambú, cacao |
| Desierto / badlands | acacia | arenisca | cactus muertos, arena apoyada |
| Montaña | abeto oscuro | piedra, pizarra | nieve, helechos |

## 2. Torre en ruinas — decoración por cuarto

Dos formas: **cuadrada con contrafuertes** (2×2 de pilares en esquinas, arcos) y **redonda** (planta circular, ventanas altas, escalera de caracol). El **grado de ruina** (0 intacta, 1 gastada, 2 muy dañada) decide cuántos muros, techos y suelos faltan, y cuántos escombros hay.

| Cuarto | Decoración (bloques) | Utilidad |
|---|---|---|
| **Patio** | pozo (pared de adoquín, agua), cuerda y cubo (cadena y caldero), restos de establo (vallas, heno), huerto abandonado (tierra de cultivo con cultivos secos), escombros, barril con flechas | combate abierto con élites; pistas de dónde está el cofre oculto |
| **Portón roto** | arco derrumbado (escalones de ladrillo, muros con brechas), estandarte rasgado (cartel), antorchas apagadas | entrada principal; punto de aviso (alarma) |
| **Cuerpo de guardia** (anexo) | mesa de dibujo, barril, caldero con comida, estante con libros, rack de armas (marco de objeto con arma), mapa en la pared | cofre moderado |
| **Sótano: calabozo** | rejas de hierro, cadenas, jaula (barrotes), paja, antorchas de alma, esqueletos sentados | enemigos independientes; cofre |
| **Sótano: bodega** | barriles, estantes, jarras, telarañas, sacos | cofre de víveres |
| **P0: sala de guardia** | mesa larga y bancos (escaleras), chimenea (fogata + trampilla), estandarte, armadura en soporte | élites con armadura visible |
| **P1: armería** | racks de armas (marcos), yunque, forja (horno + lava en caldero cerrado), escudos en la pared (estandartes), cofre | cofre moderado |
| **P2: dormitorios** | literas (camas apiladas con escaleras), baúles, alfombras, velas, cofre; **generador de monstruos** | cofre `tower_floor` |
| **P3: taller / biblioteca** | estanterías de dos pisos, atril con libro, mesa de cartografía, telescopio (cristal tintado), pergaminos (papel en marcos); **generador** | cofre `tower_upper` |
| **P4: sala del vigía** | balcón con almenas, farol grande, mapa de ruta, cuerno de guerra (campana), nido de arqueros | élites con arco |
| **Azotea** | almenas con huecos, estandarte de la orden roto, brasero, bandera caída; **Centinela** | cofre `tower_top` |
| **Escalera** | rellanos con descansos, cadenas, antorchas, hiedra en el hueco | recorrido sin saltos |
| **Galerías** | pasarelas de tablones y escaleras de piedra a media altura, barandas de vallas | verticalidad |

**Cofre oculto con pista visual (torre):** una sala secreta de 2×2 tras un **muro de ladrillos agrietados** en el piso 1 o el 3, con **pistas**: una grieta que sube por la pared, **polvo (telarañas) y un hueco de hiedra** y una **vela encendida** en el suelo frente al muro. Se abre con cualquier herramienta, sin mecanismo.

## 3. Campamentos — catálogo de tipos

Hoy: **cazadores**, **brujos**, **bandidos**. **Tipos nuevos propuestos** (a podar):

| Tipo | Idea | Estructuras y decoración propias | Enemigos | Botín |
|---|---|---|---|---|
| **Cazadores** (actual) | gremio de caza | tiendas verdes, **ahumadero** (ahumador + cadenas con carne), **secadero de pieles** (vallas con lana), **polígono de tiro** (dianas, fardos), tótem de huesos | arqueros y rastreadores | `camp_hunter`: arcos, flechas, cuero, set del Cazador |
| **Brujos** (actual) | ocultistas del pantano | tiendas negras y moradas, **círculo de piedras** (ladrillos de piedra con velas), **caldero** y mesa de **alquimia**, setas gigantes, hongos | brujas y hechiceros | `camp_warlock`: maldito |
| **Bandidos** (actual) | ladrones de camino | tiendas rojas, **barricadas** (vallas y pinchos), **botín apilado** (barriles y cofres abiertos), **jaula** con prisionero decorativo, mesa de mapas | vindicadores y saqueadores | `camp_bandit`: armas y armaduras |
| **Mercaderes asaltados** (nuevo) | caravana saqueada | **carros volcados** (carros de vallas y tablones con ruedas), mulas muertas, mercancías esparcidas, toldos rotos, baúles abiertos | bandidos que aún saquean, élites | botín **comercial**: esmeraldas, papel, lana, especias |
| **Desertores** (nuevo) | soldados de la orden que huyeron | tiendas militares con **estandartes rotos**, trincheras de tierra, **armas hincadas**, fogata de campaña, mapa del frente | soldados con armadura visible (élites) | armas y piezas medias |
| **Cultistas del Velo** (nuevo) | seguidores de la Niebla | **círculo de velas** con obsidiana llorosa, **estandartes del Velo**, altar de piedra negra, antorchas de alma, huesos | oscuros y errantes; sin pactos ni mecánica nueva | botín «del Velo»: fragmentos de alma, Jirón (sin poder nuevo) |
| **Buscadores de reliquias** (nuevo, opcional) | excavadores | **zanjas** (tierra y grava), **andamios**, picos y palas hincados, tiendas con mapas, baúles de hallazgos | mineros hostiles y arañas | minerales, fragmentos, curiosidades |

**Formas (independiente del tipo, elegidas con la semilla):** círculo de tiendas, fortín de empalizada con torre de vigía, línea de camino con barricadas, terraza sobre una elevación.

## 4. Decoración de las formas (qué cambia)

| Forma | Dónde va | Props |
|---|---|---|
| **Círculo de tiendas** | claro | hoguera central, troncos-asiento, tendedero, barriles, vallas bajas |
| **Fortín de empalizada** | cualquier terreno | muro de troncos con puntas, **puerta de vallas**, **torre de vigía** de 3 pisos con arqueros, patio interior, pozo |
| **Línea de camino** | llano | carros y barricadas, farolillos, cartel de dirección, hoguera cada ~8 bloques |
| **Terraza** | pendiente suave | rampa y plataformas de madera, caballete, tienda del jefe arriba |

Elementos comunes: **tiendas grandes** de lana (camas, cofre, mesa), **cocina** (calderos, ahumador, barriles), **almacén** (cofres, sacos de heno), **corral** (vallas con animales decorativos), **establo**, **forja** (yunque, horno), **tablón de anuncios** (cartel), **estandarte del tipo**.

## 5. Encuentros por anillo (propuesta de partida, el balance no se toca sin preguntar)

| Anillo | Torre (enemigos por piso + patio) | Campamento (grupo total) |
|---|---|---|
| 0–I | 1 élite por piso; patio con 1–2 guardias | 3–4 enemigos + capitán |
| II–III | 1–2 élites por piso; patio con 2–3 guardias; campeón opcional en el patio | 5–6 enemigos (1–2 élites) + capitán |
| IV–V | 2 élites por piso; patio con campeón y 3–4 guardias | 7–8 enemigos (2–3 élites) + capitán campeón reforzado |

Siempre: el **Centinela** (torre) o el **capitán** (campamento) como jefe final, y los **generadores** de la torre con valores moderados.

## 6. Comportamiento: alarma y enemigos independientes

| Rol | Comportamiento |
|---|---|
| **Capitán** | si te ve (línea de visión a menos de 24 bloques) da la **alarma**: el resto del campamento se activa y se une |
| **Centinelas** (vigía, torre de vigilancia) | vigilan desde un punto fijo; si te ven, **dan la alarma** (sonido de cuerno o campana) |
| **Durmientes** | dormidos en tiendas y literas hasta que se hace ruido o hay alarma |
| **Patrulleros** | recorren un camino corto entre dos puntos, independientes de la alarma |
| **Guardias fijos** | se quedan en su puesto aunque haya alarma (protegen el cofre) |

Implementación posible: etiquetas de rol en las entidades y una pequeña IA que las lee (sin mixins; Codex decide el mecanismo y lo justifica).

## 7. Cofre oculto con pista visual (campamentos)

| Pista | Cómo se ve | Dónde |
|---|---|---|
| **Tierra suelta** | un parche de **tierra gruesa** distinto al camino, con un **hueso** o un **cartel** de «excava aquí» (pista diegética) | cofre **enterrado** 1–2 bloques bajo el suelo |
| **Doble pared de lona** | una tienda con la pared posterior **más gruesa** (alfombra extra y velas) | cofre tras la lona |
| **Barril marcado** | un barril con un **marco de objeto** de pista (mapa) sobre él | cofre bajo el barril |

La pista cambia por tipo (cazadores: huesos y pieles; brujos: velas y setas; bandidos: mapa y marcas).

## 8. Contratos por tipo de campamento

- Un contrato **por cada tipo** (los 3 actuales + los nuevos que se queden): «Limpia un campamento de bandidos», «…de brujos», etc.
- El capitán lleva la **etiqueta de su tipo**; al morir, el gestor de contratos suma el progreso si coincide.
- `ContractRules.Kind` y la lista de objetivos **solo se amplían al final** (se guardan como número); recompensa de un contrato del mismo nivel; claves en los 5 idiomas y `ContractRulesTest` ampliado. Lo hace **Codex** (decisión del autor).

## 9. Lista de comprobaciones del diseño final (para Codex)

1. Cada variante de torre y de campamento es **alcanzable a pie** (cofres, jefe, cofre oculto) y cabe en el radio 15 (pruebas con búsqueda de caminos).
2. Paleta por anillo y bioma sin cambiar la forma; ruina como grado (0–2).
3. Generadores conservan el tipo por piso; encuentros por anillo según §5.
4. Alarma e independientes según §6; cofre oculto con su pista (§2 y §7).
5. `StructureIndex.record`, `/aptitudes locate` y `place` funcionan para `tower` y `camp`; ids y frecuencias sin cambios.
6. Contratos por tipo (§8), textos en los 5 idiomas, `CHANGELOG.md` y `docs/TESTING.md` actualizados.
7. **Sin probar en partida**: lo avisa el informe final.

## 10. Para elegir antes del prompt

1. De los **tipos nuevos**, ¿cuáles dejamos? (mercaderes asaltados, desertores, cultistas del Velo, buscadores de reliquias).
2. ¿Te sirven los **números de encuentros** de §5 como punto de partida?
3. ¿Qué pistas del **cofre oculto** prefieres (tierra suelta, doble pared de lona, barril marcado) o las tres según el tipo?
