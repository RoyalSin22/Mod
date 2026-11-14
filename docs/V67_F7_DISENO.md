# v67 · F7 — Habilidades nuevas de Bardo, Brujo abisal y Domador (diseño para aprobar)

Estado: **implementada (2026-11-14); diseño aprobado por el autor.** Mismo patrón que F2–F6 (`docs/V67_F6_DISENO.md`): 10 nodos por rol, 5 pasivas y 5
activas. Es la última tanda: con ella los 20 roles tienen sus 10 habilidades nuevas (30 habilidades esta vez).

| Nodos | Sección | Posición | Niveles |
|---|---|---|---|
| N1 (pasiva), N2 (pasiva) | Talentos | casillas (2,3) y (2,4), tras la pasiva B del rol y N1 | 8 y 16 |
| N3 (activa), N4 (activa) | Talentos | casillas (6,3) y (6,5), tras Presteza y N3 | 9 y 20 |
| N5 pasiva, N6 activa, N7 pasiva | Veterano | fila centrada | 34, 36, 38 |
| N8 activa, N9 pasiva, N10 activa | Ascenso (exige Maestro) | fila centrada | 42, 45, 48 |

Rangos: N1–N4, 3; N5, N6, N8 y N9, 2; N7 y N10, 1. Cada rol acaba con 13 activas (caben en el códec de 4 bits). El daño de
las activas lo escala `extraSkillDamageScale`. Pasivas B (de las que cuelga N1): Bardo `soothing_melody`, Brujo abisal
`soul_link`, Domador `pack_leader`.

**Reglas de diseño:** cada habilidad aporta una mecánica que el rol aún no tiene, no repite nombres existentes y se
resuelve en el servidor con piezas que ya existen. Ninguna toca las exclusiones del proyecto (nada de linajes, razas ni
pactos: el Brujo usa «sangre», «maldición» y «abismo», no «pacto»).

---

## 1. Bardo (canciones, notas, apoyo)

Ya tiene: Presencia inspiradora, Canción del valor, Melodía calmante, Disonancia, Crescendo, Bis, Nana, Balada protectora y Réquiem; firma Sinfonía de guerra; definitiva Himno de los héroes. Le falta: **proteger al grupo con el sonido**, **reducir el coste de las canciones**, **dormir a los enemigos** y **sacar maná del coro**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `bard_n1` | Armonía de grupo | Talentos | Pasiva | 8 | 3 | — | Tú y los aliados a 8 bloques de ti recibís −3 / 6 / 9 % de daño |
| `bard_n2` | Estribillo pegadizo | Talentos | Pasiva | 16 | 3 | — | Tras lanzar una canción, tu siguiente habilidad cuesta −10 / 20 / 30 % de maná (una vez) |
| `bard_n3` | Nota cortante | Talentos | Activa | 9 | 3 | 20 maná, 8 s | Onda sónica en línea (14 bloques): 5 / 7 / 9 de daño y Debilidad 3 s |
| `bard_n4` | Arrullo | Talentos | Activa | 20 | 3 | 30 maná, 20 s | Duermes a los enemigos a 8 bloques 3 / 4 / 5 s (Lentitud máxima y Debilidad) |
| `bard_n5` | Coro | Veterano | Pasiva | 34 | 2 | — | Por cada aliado a 10 bloques (hasta 3) recuperas 1 / 2 de maná cada 5 s |
| `bard_n6` | Estruendo | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 4 pulsos de sonido (8 bloques): 3 / 4 de daño y Lentitud I a los enemigos |
| `bard_n7` | Pulso del compás | Veterano | Pasiva | 38 | 1 | — | Cuando matas, los aliados a 8 bloques se curan 1 |
| `bard_n8` | Marcha de victoria | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: tú y los aliados a 10 bloques ganáis Velocidad II, Prisa II y Fuerza I |
| `bard_n9` | Contrapunto | Ascenso | Pasiva | 45 | 2 | — | Tras lanzar 3 canciones distintas en 10 s, +10 / 20 % de daño durante 6 s |
| `bard_n10` | Gran final | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 6 s, 12 bloques: cada segundo, 4 de daño y Debilidad a los enemigos y 2 de curación y Fuerza a los aliados |

**Riesgo:** Armonía de grupo reduce el daño de los aliados: se engancha como el Vínculo de escudo del Paladín (lista de bardos refrescada cada 20 ticks). Contrapunto + Marcha de victoria apilan daño.

---

## 2. Brujo abisal (maldiciones, corrupción)

Ya tiene: Trato oscuro, Maldición de agonía, Vínculo de almas, Rayo del abismo, Corrupción, Égida demoníaca, Festín de almas, Invocar diablillos y Condena; firma Puerta del abismo; definitiva Avatar demoníaco. Le falta: **aprovechar los efectos negativos**, **usar la sangre como recurso**, **controlar con cadenas** y **desesperación**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `warlock_n1` | Sed de maldiciones | Talentos | Pasiva | 8 | 3 | — | Los enemigos con algún efecto negativo (veneno, marchitamiento, lentitud o debilidad) reciben +4 / 8 / 12 % de tu daño mágico |
| `warlock_n2` | Sacrificio de sangre | Talentos | Pasiva | 16 | 3 | — | Cuando un hechizo te cuesta vida (Corrupción), recuperas 3 / 5 / 7 de maná |
| `warlock_n3` | Mal de ojo | Talentos | Activa | 9 | 3 | 20 maná, 8 s | Maldices al objetivo (24 bloques): Ceguera 2 s y, a los 3 s, 8 / 10 / 12 de daño |
| `warlock_n4` | Cadenas de sombra | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Cadenas atan a los enemigos a 4 bloques del punto que miras: inmóviles 3 / 4 / 5 s y Marchitamiento |
| `warlock_n5` | Ansia de alma | Veterano | Pasiva | 34 | 2 | — | Al matar, +20 / 30 % de daño mágico durante 6 s |
| `warlock_n6` | Nube de pestilencia | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 6 s, 5 bloques en el punto que miras: 3 / 4 de daño por segundo, Veneno y Lentitud |
| `warlock_n7` | Desesperación | Veterano | Pasiva | 38 | 1 | — | Por debajo del 30 % de vida, tus hechizos no cuestan maná durante 5 s (una vez cada 90 s) |
| `warlock_n8` | Forma demoníaca | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: +25 % de daño mágico, Resistencia I y cada hechizo te cura 1 |
| `warlock_n9` | Cosecha oscura | Ascenso | Pasiva | 45 | 2 | — | Los enemigos con efecto negativo que matas estallan: 3 / 5 de daño en 3 bloques |
| `warlock_n10` | Fosa abisal | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 6 s, 6 bloques en el punto que miras: 6 de daño por segundo, Marchitamiento y Lentitud; te curas 1 por enemigo herido |

**Riesgo:** Desesperación + Forma demoníaca + Ansia de alma hacen al Brujo casi imparable a poca vida; Desesperación es una vez cada 90 s y la forma dura 8–10 s. Sacrificio de sangre depende de la Corrupción del rol.

---

## 3. Domador (mascotas, manada)

Ya tiene: Vínculo animal, ¡A por él!, Líder de la manada, Vínculo vital, Compañero, Instinto protector, Festín salvaje, Estampida y Rugido alfa; firma Manada espectral; definitiva Rey de las bestias. Le falta: **blindar a las mascotas**, **ataques coordinados con ellas**, **traerlas a tu lado** y **curarlas con tus golpes**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `tamer_n1` | Pelaje denso | Talentos | Pasiva | 8 | 3 | — | Tus mascotas a 16 bloques tienen 2 / 4 / 6 de absorción (se renueva cada 10 s) |
| `tamer_n2` | Frenesí de caza | Talentos | Pasiva | 16 | 3 | — | Cuando golpeas a un enemigo, tus mascotas a 16 bloques ganan Velocidad II 3 s (hasta 3 mascotas) |
| `tamer_n3` | Convocar a la manada | Talentos | Activa | 9 | 3 | 20 maná, 12 s | Tus mascotas a 32 bloques se teletransportan a tu lado y ganan Resistencia 3 / 4 / 5 s |
| `tamer_n4` | Zarpazos coordinados | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Cada mascota cercana (hasta 3) golpea a tu objetivo: 4 / 5 / 6 de daño cada una |
| `tamer_n5` | Cuidado veterano | Veterano | Pasiva | 34 | 2 | — | Tus mascotas con menos de la mitad de vida a 8 bloques de ti recuperan 1 / 2 de vida por segundo |
| `tamer_n6` | Manto protector | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Tú y tus mascotas a 12 bloques ganáis 4 / 6 de absorción y Resistencia I 6 s |
| `tamer_n7` | Olfato de la manada | Veterano | Pasiva | 38 | 1 | — | Cada 10 s, los enemigos a 16 bloques brillan 3 s para ti y tus mascotas |
| `tamer_n8` | Furia animal | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: tus mascotas a 24 bloques ganan Fuerza II, Velocidad II y Resistencia I |
| `tamer_n9` | Vínculo de sangre | Ascenso | Pasiva | 45 | 2 | — | El 10 / 20 % del daño que haces cura a tu mascota herida más cercana |
| `tamer_n10` | Llamada del rey | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Tus mascotas a 24 bloques se curan del todo y ganan Fuerza III y Resistencia II durante 8 s |

**Riesgo:** Todo depende de tener mascotas domadas cerca (`NewRoles.pets`): sin ellas muchas habilidades no hacen nada. Las mascotas de otros jugadores y las invocaciones no deben contar. Furia animal + Llamada del rey las hacen muy fuertes (esperas de 45–90 s).

---

## 4. Coste técnico (para la implementación)

| Elemento | Detalle |
|---|---|
| Datos | `RoleExtras.of(...)`: 3 casos nuevos con `standard(...)` (pasivas B `soothing_melody`, `soul_link`, `pack_leader`); `RoleExtrasTest` suma `bard`, `warlock`, `tamer` y deja de esperar roles vacíos |
| Lógica | `BardKit`, `WarlockKit` y `TamerKit` con sus `*Rules` puras y pruebas; se registran solo en `ExtraKits` |
| Ganchos nuevos | Posiblemente ninguno en el resto del juego. Se añadirán accesores en `NewRoles` (notas del Bardo, acumulaciones de Corrupción, mascotas del Domador), como en F6. Armonía de grupo del Bardo sigue el patrón del Vínculo de escudo del Paladín; Estribillo pegadizo y Desesperación usan `ExtraKits.costMultiplier` |
| Cuidado | Sacrificio de sangre y Desesperación dependen de cómo cobra el Brujo su vida (a revisar al implementar); Domador no cuenta mascotas ajenas ni invocaciones |
| Textos | 60 claves en 5 idiomas vía `base_desc.py` y 30 nombres en `node_catalog.py` |
| Arte | `docs/PROMPT_ARTE_ICONOS_F7.md`; iconos provisionales vanilla |
| Documentación | CHANGELOG, BALANCE, TESTING, AGENTS, ROADMAP; sin opciones de configuración nuevas |

## 5. Qué necesito que apruebes

1. Los **3 roles** (Bardo, Brujo abisal, Domador) como tanda F7, o prefieres cambiar alguno.
2. Las **30 habilidades**; dime cuáles cambiar. Las del Domador dependen de las mascotas y las del Brujo, de cómo cobra la
   Corrupción; las revisaré primero para no duplicar nada.
3. ¿Implementarlas **todas de una vez**, como en F4–F6?

## 6. Diferencias entre el diseño aprobado y la implementación

| Habilidad | Diseño | Implementado | Motivo |
|---|---|---|---|
| `warlock_n2` Sacrificio de sangre | Cuando un hechizo te cuesta vida | Solo si tienes Corrupción (el nodo que cobra la vida) | Es esa pasiva la que descuenta la vida, no los hechizos por sí mismos |
| `bard_n2` Estribillo pegadizo | El descuento se aplica una vez | El descuento se gasta con la siguiente habilidad y esa no lo rearma | Evita una cadena de habilidades baratas |
| `warlock_n7` Desesperación | Hechizos sin coste de maná | Solo los hechizos del Brujo | Es una pasiva del rol |
| Todas las activas | Entidades de efecto | Efectos con partículas y sonidos, sin entidades | Menos riesgo de entidades huérfanas |
