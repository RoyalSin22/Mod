# v67 · F5 — Habilidades nuevas de Druida, Mercader táctico, Monje y Chamán (diseño para aprobar)

Estado: **propuesta, sin código.** Mismo patrón que F2–F4 (`docs/V67_F4_DISENO.md`): 10 nodos por rol, 5 pasivas y 5
activas. En el juego, el rol `elementalist` se llama **Chamán**.

| Nodos | Sección | Posición | Niveles |
|---|---|---|---|
| N1 (pasiva), N2 (pasiva) | Talentos | casillas (2,3) y (2,4), tras la pasiva B del rol y N1 | 8 y 16 |
| N3 (activa), N4 (activa) | Talentos | casillas (6,3) y (6,5), tras Presteza y N3 | 9 y 20 |
| N5 pasiva, N6 activa, N7 pasiva | Veterano | fila centrada | 34, 36, 38 |
| N8 activa, N9 pasiva, N10 activa | Ascenso (exige Maestro) | fila centrada | 42, 45, 48 |

Rangos: N1–N4, 3; N5, N6, N8 y N9, 2; N7 y N10, 1. Cada rol acaba con 13 activas (caben en el códec de 4 bits). El daño de
las activas lo escala `extraSkillDamageScale`. Pasivas B (de las que cuelga N1): Druida `growth`, Mercader `deep_pockets`,
Monje `evasion`, Chamán `elemental_dominion`.

**Reglas de diseño:** cada habilidad aporta una mecánica que el rol aún no tiene, no repite nombres existentes y se
resuelve en el servidor con piezas que ya existen. Ninguna toca las exclusiones del proyecto.

---

## 1. Druida (naturaleza, formas, ciclo)

Ya tiene: Raíces, Crecimiento, Forma de oso, Ciclo natural, Piel de corteza, Zarzas, Manada y Tormenta de espinas; firma Renacer del bosque; definitiva Furia de la naturaleza. Le falta: **terreno**, **marcar presas**, **tirar de los enemigos** y **resistir al límite**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `druid_n1` | Hierba alta | Talentos | Pasiva | 8 | 3 | — | Sobre hierba, tierra, musgo o podzol recibes −4 / 8 / 12 % de daño |
| `druid_n2` | Zarpas desgarradoras | Talentos | Pasiva | 16 | 3 | — | Tus golpes hacen 1 / 2 / 3 de daño extra por segundo durante 3 s (no se acumula) |
| `druid_n3` | Látigo de lianas | Talentos | Activa | 9 | 3 | 20 maná, 12 s | Una liana agarra al enemigo que miras (16 bloques) y lo arrastra hasta ti: 4 / 5 / 6 de daño y Lentitud II 2 s |
| `druid_n4` | Rebrote | Talentos | Activa | 20 | 3 | 30 maná, 25 s | Durante 5 s recuperas el 25 / 35 / 45 % de tu vida máxima (repartido por segundos) |
| `druid_n5` | Letargo | Veterano | Pasiva | 34 | 2 | — | Al bajar del 30 % de vida te curas el 20 / 30 % de golpe, una vez cada 90 s |
| `druid_n6` | Rugido del oso | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Rugido de 8 bloques: 4 / 5 de daño, empuje y Debilidad 4 s a los enemigos |
| `druid_n7` | Roble firme | Veterano | Pasiva | 38 | 1 | — | No te empujan los golpes y recibes −10 % de daño |
| `druid_n8` | Forma de huargo | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: Velocidad II, +30 % de daño y tus golpes te curan 1 |
| `druid_n9` | Flujo estacional | Ascenso | Pasiva | 45 | 2 | — | Tras 20 s sin recibir daño, tu siguiente golpe te cura 3 / 5 |
| `druid_n10` | Despertar del bosque | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 6 s, 10 bloques: raíces inmovilizan a los enemigos (4 de daño por segundo) y curan 2 por segundo a los aliados |

**Riesgo:** Rebrote + Letargo + Forma de huargo hacen al Druida muy duro; las esperas largas (25–90 s) y el mínimo de 30 % de vida de Letargo lo frenan.

---

## 2. Mercader táctico (esmeraldas, botín, apuestas)

Ya tiene: Regateo, Ojo del tasador, Bolsillos profundos, Soborno, Interés compuesto, Seguro, Recompensa, Lanzamiento de moneda y Guardia dorada; firma Señor del mercado; definitiva Casino de la fortuna. Le falta: **ahorrar esmeraldas**, **marcar y cobrar presas**, **apoyar al grupo** y **liquidar** todo el capital.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `merchant_n1` | Monedero | Talentos | Pasiva | 8 | 3 | — | Las activas que gastan esmeraldas tienen un 15 / 30 / 45 % de no gastarlas |
| `merchant_n2` | Dividendos | Talentos | Pasiva | 16 | 3 | — | Cada 60 s ganas 1 esmeralda si llevas al menos 30 / 20 / 10 (máx. 64) |
| `merchant_n3` | Moneda rebotona | Talentos | Activa | 9 | 3 | 15 maná, 8 s | La moneda rebota entre hasta 3 enemigos a 8 bloques: 6 / 8 / 10 de daño cada uno |
| `merchant_n4` | Subasta | Talentos | Activa | 20 | 3 | 30 maná, 20 s | Marcas al enemigo que miras 6 s: recibe +10 / 15 / 20 % de daño y suelta 2 esmeraldas al morir |
| `merchant_n5` | Crédito | Veterano | Pasiva | 34 | 2 | — | Sin esmeraldas, tus activas se pagan con 10 / 5 de maná en vez de esmeraldas |
| `merchant_n6` | Caravana | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 8 / 10 s: tú y los aliados a 10 bloques ganáis Velocidad II y Salto |
| `merchant_n7` | Libro mayor | Veterano | Pasiva | 38 | 1 | — | Cada esmeralda gastada carga +3 % de daño en tu siguiente golpe (hasta +15 %) |
| `merchant_n8` | Inversión | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 10 / 14 s: recuperas el 30 % del maná que gastes |
| `merchant_n9` | Cacería de recompensas | Ascenso | Pasiva | 45 | 2 | — | Los élites y campeones que matas sueltan 2 / 4 esmeraldas |
| `merchant_n10` | Gran liquidación | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Gastas hasta 10 esmeraldas: explosión de 6 bloques con 20 de daño más 3 por esmeralda gastada |

**Riesgo:** Dividendos + Monedero generan esmeraldas casi sin límite; el tope de 64 y el requisito de llevar un mínimo lo frenan. La esmeralda es el recurso, no el daño.

---

## 3. Monje (artes marciales, chi)

Ya tiene: Puños de hierro, Chi, Palma de choque, Meditación, Patada voladora, Cien puños, Posturas, Desvío y Paz interior; firma Bendición del maestro; definitiva Nirvana. Le falta: **ritmo de golpes**, **contraataque tras esquivar**, **marcas que explotan** y **un golpe único cargado**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `monk_n1` | Manos vacías | Talentos | Pasiva | 8 | 3 | — | Sin nada en las manos haces +4 / 8 / 12 % de daño |
| `monk_n2` | Combinación | Talentos | Pasiva | 16 | 3 | — | El 3.er golpe seguido en 4 s hace +10 / 20 / 30 % de daño |
| `monk_n3` | Patada giratoria | Talentos | Activa | 9 | 3 | 25 maná, 10 s | Giras y golpeas a los enemigos a 3 bloques: 5 / 6 / 7 de daño y empuje |
| `monk_n4` | Onda de chi | Talentos | Activa | 20 | 3 | 30 maná, 14 s | Lanzas una onda de 12 bloques: 6 / 8 / 10 de daño y Lentitud I 2 s |
| `monk_n5` | Cuerpo sereno | Veterano | Pasiva | 34 | 2 | — | Sin recibir golpes durante 5 s, atacas un 15 / 25 % más rápido |
| `monk_n6` | Palma explosiva | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Marcas a un enemigo; a los 3 s explota: 12 / 16 de daño a él y 6 / 8 en 4 bloques |
| `monk_n7` | Punto de presión | Veterano | Pasiva | 38 | 1 | — | Tus golpes críticos dejan al enemigo con Lentitud II y Debilidad 3 s |
| `monk_n8` | Postura del tigre | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: +30 % de daño y tus golpes quitan 1 de armadura al enemigo (hasta 5) |
| `monk_n9` | Eco de puño | Ascenso | Pasiva | 45 | 2 | — | Cada 4.º golpe lanza una onda de 5 bloques con el 40 / 60 % del daño |
| `monk_n10` | Palma del vacío | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Cargas 1 s y golpeas con todo: 30 de daño más 15 % de la vida máxima del objetivo (hasta 20 extra) |

**Riesgo:** Combinación + Manos vacías + Postura del tigre apilan daño contra un solo objetivo (`maxDamageMultiplier` los limita); Cuerpo sereno exige no recibir daño.

---

## 4. Chamán (elementos, fusiones)

Ya tiene: Afinidad, Dominio, Reacción, Resguardo, Muerte resonante, Descarga, Ráfaga, Aura elemental; 14 elementos y 10 fusiones; firma Bastión elemental; definitiva Convergencia elemental. Le falta: **el cambio de elemento como recurso**, **zonas elementales**, **fusiones más baratas** y **armonía** entre tres elementos.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `elementalist_n1` | Cambio de marea | Talentos | Pasiva | 8 | 3 | — | Al cambiar de elemento, haces +10 / 15 / 20 % de daño durante 5 s |
| `elementalist_n2` | Eco del elemento | Talentos | Pasiva | 16 | 3 | — | Tus golpes tienen un 8 / 12 / 16 % de repetir el efecto del elemento activo |
| `elementalist_n3` | Lanza elemental | Talentos | Activa | 9 | 3 | 20 maná, 8 s | Lanza del elemento activo (20 bloques): 6 / 8 / 10 de daño y su efecto |
| `elementalist_n4` | Muro elemental | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Muro de 8 bloques durante 4 s: daña y aplica el efecto del elemento a quien lo cruza (3 / 4 / 5 por segundo) |
| `elementalist_n5` | Reacción en cadena | Veterano | Pasiva | 34 | 2 | — | Una fusión tiene un 25 / 40 % de restituir su coste de maná |
| `elementalist_n6` | Tótem elemental | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 8 s: un tótem a 10 bloques lanza el elemento activo a los enemigos cercanos cada segundo (4 / 5 de daño) |
| `elementalist_n7` | Fusión estable | Veterano | Pasiva | 38 | 1 | — | Las fusiones duran un 30 % más |
| `elementalist_n8` | Forma elemental | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: tus golpes llevan el efecto del elemento y ganas Resistencia I |
| `elementalist_n9` | Armonía | Ascenso | Pasiva | 45 | 2 | — | Tras usar 3 elementos distintos en 10 s, +10 / 20 % de daño durante 6 s |
| `elementalist_n10` | Colapso de elementos | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Estalla con los 3 elementos a 8 bloques: 25 de daño y un efecto distinto por cada elemento |

**Riesgo:** Los efectos dependen del elemento activo (14 posibles): hay que probar que cada combinación funcione. Cambio de marea + Armonía + Forma elemental acumulan daño.

---

## 5. Coste técnico (para la implementación)

| Elemento | Detalle |
|---|---|
| Datos | `RoleExtras.of(...)`: 4 casos nuevos con `standard(...)` (pasivas B `growth`, `deep_pockets`, `evasion`, `elemental_dominion`); `RoleExtrasTest` suma `druid`, `merchant`, `monk`, `elementalist` |
| Lógica | `DruidKit`, `MerchantKit`, `MonkKit` y `ElementalistKit` con sus `*Rules` puras y pruebas; se registran solo en `ExtraKits` |
| Ganchos nuevos | Ninguno previsto. Los kits reutilizan los de F4 (golpe de salida/entrada, `onKill`, `onCast`, `tick`, daño posterior al golpe). Letargo y Gran liquidación necesitan leer el inventario y la vida actual, ya accesibles |
| Cuidado | Chamán: leer el elemento activo y el cambio de elemento (`role/Element`, `ElementEffects`, `ElementFusions`); Letargo y Roble firme usan los mismos puntos que Voluntad de acero; Mercader: gastar esmeraldas con el mismo helper que `coin_toss` |
| Textos | 80 claves en 5 idiomas vía `base_desc.py` y 40 nombres en `node_catalog.py` |
| Arte | `docs/PROMPT_ARTE_ICONOS_F5.md`; iconos provisionales vanilla |
| Documentación | CHANGELOG, BALANCE, TESTING, AGENTS, ROADMAP; sin opciones de configuración nuevas |

## 6. Qué necesito que apruebes

1. Los **4 roles** (Druida, Mercader táctico, Monje, Chamán) como tanda F5, o prefieres cambiar alguno.
2. Las **40 habilidades**; dime cuáles cambiar. El Chamán es el más delicado por depender del elemento activo.
3. ¿Implementarlas **todas de una vez**, como en F4?
