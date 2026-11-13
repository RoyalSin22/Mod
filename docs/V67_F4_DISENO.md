# v67 · F4 — Habilidades nuevas de Lancero, Espadachín, Paladín y Nigromante (diseño para aprobar)

Estado: **implementada (2026-11-13); diseño aprobado por el autor.** Mismo patrón que F2 y F3 (`docs/V67_F3_DISENO.md`): 10 nodos por rol, 5 pasivas y 5
activas.

| Nodos | Sección | Posición | Niveles |
|---|---|---|---|
| N1 (pasiva), N2 (pasiva) | Talentos | casillas (2,3) y (2,4), tras la pasiva B del rol y N1 | 8 y 16 |
| N3 (activa), N4 (activa) | Talentos | casillas (6,3) y (6,5), tras Presteza y N3 | 9 y 20 |
| N5 pasiva, N6 activa, N7 pasiva | Veterano | fila centrada | 34, 36, 38 |
| N8 activa, N9 pasiva, N10 activa | Ascenso (exige Maestro) | fila centrada | 42, 45, 48 |

Rangos: N1–N4, 3; N5, N6, N8 y N9, 2; N7 y N10, 1. Cada rol acaba con 13 activas (caben en el códec de 4 bits). El daño de
las activas lo escala `extraSkillDamageScale`. Criterio de poder: las activas rinden por debajo de la firma, la definitiva
y Terremoto (10 + 1,5 por rango).

**Reglas de diseño:** cada habilidad aporta una mecánica que el rol aún no tiene, no repite nombres existentes (nodos
base, Maestro/Definitivo, firmas, definitivas, legendarias, sendas y piedras angulares) y se resuelve en el servidor con las
piezas que ya existen. Ningún nombre ni mecánica toca las exclusiones del proyecto (nada de linajes, razas ni pactos).

---

## 1. Lancero (alcance, cargas, estocadas)

Ya tiene: Alcance, Embestida, Carga, Barrido, Ímpetu, Afianzarse, Caída del dragontino, Salto del dragón, Estocada
perforante; Maestro/Definitivo (Postura firme, Perforar, Furia del dragón, Falange, Rayo celeste…); firma Carga imparable;
definitiva Lanza celestial. Le falta: **jugar con la distancia**, **marcar presas**, **lanzar la lanza** y **atravesar**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `lancer_n1` | Mantener la distancia | Talentos | Pasiva | 8 | 3 | — | Tus golpes a 3 bloques o más del enemigo hacen +5 / 10 / 15 % de daño |
| `lancer_n2` | Punta clavada | Talentos | Pasiva | 16 | 3 | — | Los enemigos que alcanza una activa tuya reciben +4 / 8 / 12 % de tus golpes durante 5 s |
| `lancer_n3` | Jabalina | Talentos | Activa | 9 | 3 | 20 maná, 10 s | Lanzas una lanza en línea (20 bloques) que atraviesa: 6 / 8 / 10 de daño y Lentitud II 2 s |
| `lancer_n4` | Muro de picas | Talentos | Activa | 20 | 3 | 30 maná, 18 s | 4 s: un anillo de picas de 3 bloques golpea y empuja cada 0,5 s: 3 / 4 / 5 por pulso |
| `lancer_n5` | Ensartar | Veterano | Pasiva | 34 | 2 | — | Tus golpes cuerpo a cuerpo atraviesan y dañan al enemigo de detrás (2 bloques): 40 / 60 % |
| `lancer_n6` | Abanico de picas | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 5 estocadas en cono de 5 bloques durante 1 s: 4 / 5 de daño cada una |
| `lancer_n7` | Primer contacto | Veterano | Pasiva | 38 | 1 | — | El primer golpe a cada enemigo hace +30 % (se reinicia tras 10 s sin golpearlo) |
| `lancer_n8` | Lanza ancestral | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: +2 de alcance, +20 % de daño y tus golpes atraviesan (50 %) |
| `lancer_n9` | Doble estocada | Ascenso | Pasiva | 45 | 2 | — | 10 / 20 % de que un golpe se repita con el 60 % del daño |
| `lancer_n10` | Ráfaga de jabalinas | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Lanzas jabalinas a hasta 8 enemigos en 12 bloques durante 1 s: 12 de daño cada una |

**Riesgo:** Punta clavada + Primer contacto + Mantener la distancia suman multiplicadores contra un mismo objetivo
(`maxDamageMultiplier` los limita).

---

## 2. Espadachín (duelos, filo, contraataques)

Ya tiene: Filo, Danza de espadas, Parada, Contraataque, Kata, Reflejos, Ráfaga, Iaido, Mil cortes; Maestro/Definitivo
(Filo templado, Ritmo, Corte cruzado, Kensei, Onda de espada…); firma Corte dimensional; definitiva Mil cortes. Le falta:
**premiar el uno contra uno**, **perforar armadura**, **rachas** y **sobrevivir al golpe mortal**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `swordsman_n1` | Duelo | Talentos | Pasiva | 8 | 3 | — | Con un solo enemigo a 6 bloques: +5 / 10 / 15 % de daño y −5 / 10 / 15 % de daño recibido |
| `swordsman_n2` | Filo penetrante | Talentos | Pasiva | 16 | 3 | — | +1 / 2 / 3 % de daño por punto de armadura del enemigo (máx. +30 %) |
| `swordsman_n3` | Tajo ascendente | Talentos | Activa | 9 | 3 | 20 maná, 8 s | Un tajo hacia arriba lanza a los enemigos a 3 bloques: 4 / 5 / 6 de daño y Debilidad 3 s |
| `swordsman_n4` | Molinete | Talentos | Activa | 20 | 3 | 30 maná, 16 s | 2 s girando: desvías proyectiles a 3 bloques y golpeas cada 0,5 s: 3 / 4 / 5 |
| `swordsman_n5` | Racha de victorias | Veterano | Pasiva | 34 | 2 | — | Cada muerte en 8 s suma una carga de +4 / 6 % de daño (máx. 5) |
| `swordsman_n6` | Salto de espadachín | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Saltas hasta 10 bloques sobre un enemigo y lo golpeas desde arriba: 8 / 10 y Lentitud II 2 s |
| `swordsman_n7` | Voluntad de acero | Veterano | Pasiva | 38 | 1 | — | Una vez cada 120 s, un golpe mortal te deja con 1 de vida y Resistencia IV 3 s |
| `swordsman_n8` | Estado de flujo | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: +30 % de velocidad de ataque y cada golpe recorta 0,5 s la espera de tus activas |
| `swordsman_n9` | Desenvainado | Ascenso | Pasiva | 45 | 2 | — | Tras 3 s sin atacar, tu siguiente golpe hace +30 / 50 % |
| `swordsman_n10` | Corte final | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Tras 1,2 s de carga, un tajo en cono de 10 bloques: 24 de daño + el 10 % de la vida máxima del enemigo |

**Riesgo:** Voluntad de acero es una segunda vida; con una espera de 120 s y 1 de vida debería ser un respiro, no un
seguro. Se puede subir a 180 s.

---

## 3. Paladín (luz, protección de grupo)

Ya tiene: Castigo sagrado, Escudo divino, Aura protectora, Juicio, Luz sagrada, Intercesión, Luz purificadora,
Consagración, Martillo de la justicia; Maestro/Definitivo (Fe, Égida, Bendición, Santo, Luz radiante…); firma Bastión
sagrado; definitiva Reino eterno. Le falta: **defenderse de los no-muertos**, **curar al matar**, **un rayo de luz**,
**protección pasiva de grupo** y **convertir el daño recibido en castigo**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `paladin_n1` | Fe contra la oscuridad | Talentos | Pasiva | 8 | 3 | — | Recibes −8 / 16 / 24 % de daño de los no-muertos |
| `paladin_n2` | Triunfo compartido | Talentos | Pasiva | 16 | 3 | — | Al matar a un enemigo, los aliados a 8 bloques se curan 2 / 3 / 4 |
| `paladin_n3` | Rayo de luz | Talentos | Activa | 9 | 3 | 25 maná, 14 s | Rayo de 24 bloques: cura 4 / 6 / 8 al primer aliado, o hace 6 / 8 / 10 al primer enemigo (el doble a no-muertos) |
| `paladin_n4` | Golpe de escudo | Talentos | Activa | 20 | 3 | 30 maná, 25 s | Golpeas a un enemigo a 4 bloques: 5 / 6 / 7 de daño, empuje y aturdimiento 1 / 1,5 / 2 s |
| `paladin_n5` | Oración | Veterano | Pasiva | 34 | 2 | — | Agachado y quieto 2 s, tú y los aliados a 6 bloques recuperáis 1 / 2 de vida por segundo |
| `paladin_n6` | Muro de luz | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Muro de 8 bloques de ancho durante 6 s: detiene proyectiles enemigos, ralentiza (Lentitud II) y hace 3 / 4 por segundo |
| `paladin_n7` | Vínculo de escudo | Veterano | Pasiva | 38 | 1 | — | Tú y los aliados a 4 bloques de ti recibís un 10 % menos de daño |
| `paladin_n8` | Estandarte sagrado | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 10 / 14 s, 8 bloques: los aliados ganan Fuerza y Regeneración; los enemigos, Debilidad |
| `paladin_n9` | Ira justa | Ascenso | Pasiva | 45 | 2 | — | El daño que recibes carga tu siguiente golpe: +50 % de lo recibido, hasta +30 / 50 % |
| `paladin_n10` | Columna de luz | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Columna de 5 bloques en el punto que miras durante 6 s: cada segundo 6 de daño a enemigos (el doble a no-muertos) y 3 de curación a aliados |

**Riesgo:** Vínculo de escudo junto a Aura protectora y Estandarte puede hacer al grupo casi inmortal; habrá que
probarlo con varios jugadores.

---

## 4. Nigromante (esbirros, drenaje, almas)

Ya tiene: Comunión oscura, Alzar esbirros, Drenaje vital, Toque mortal, Cosecha de almas, Escudo de hueso, Estallido de
cadáver, Lanza de hueso, Sifón de almas; Maestro/Definitivo (Legión, Plaga, Alzar caídos, Ejército eterno…); firma
Ejército de sangre; definitiva Marea carmesí. Le falta: **dirigir a los esbirros**, **ganar con sus muertes**,
**sacrificarlos**, **protegerlos** y **compartir daño con ellos**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `necromancer_n1` | Orden de ataque | Talentos | Pasiva | 8 | 3 | — | Cuando golpeas a un enemigo, tus esbirros a 12 / 16 / 20 bloques lo atacan |
| `necromancer_n2` | Banquete | Talentos | Pasiva | 16 | 3 | — | Cuando un esbirro tuyo mata, te curas 1 / 2 / 3 y recuperas 4 / 6 / 8 de maná |
| `necromancer_n3` | Mano de huesos | Talentos | Activa | 9 | 3 | 22 maná, 12 s | Una mano agarra al enemigo que miras (16 bloques): inmóvil 2 / 3 / 4 s y 4 / 5 / 6 de daño |
| `necromancer_n4` | Sacrificio | Talentos | Activa | 20 | 3 | 40 maná, 30 s | Haces estallar al esbirro más cercano: 8 / 10 / 12 de daño en 4 bloques y te curas 4 (falla si no tienes esbirros) |
| `necromancer_n5` | Aura putrefacta | Veterano | Pasiva | 34 | 2 | — | Los enemigos a 4 bloques de ti sufren Debilidad y 1 / 2 de daño por segundo |
| `necromancer_n6` | Tierra maldita | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 8 s, 5 bloques: los enemigos van lentos y reciben 3 / 4 por segundo; te curas el 25 % de ese daño |
| `necromancer_n7` | Resurgir | Veterano | Pasiva | 38 | 1 | — | Una vez cada 60 s, un esbirro que iba a morir sobrevive con el 50 % de su vida |
| `necromancer_n8` | Frenesí necrótico | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 10 / 14 s: tus esbirros a 16 bloques ganan Fuerza II, Velocidad II y Resistencia |
| `necromancer_n9` | Vínculo de huesos | Ascenso | Pasiva | 45 | 2 | — | El 15 / 25 % del daño que recibes lo recibe tu esbirro más cercano |
| `necromancer_n10` | Tormenta de almas | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 5 s, 10 bloques: almas que giran hacen 3 de daño cada 0,5 s; cada golpe te cura 0,5 (máx. 10 en total) |

**Riesgo:** Vínculo de huesos + Escudo de hueso + Resurgir hacen muy difícil matar al Nigromante si tiene esbirros;
el límite lo pone el número de esbirros (máx. 4) y la espera de Resurgir.

---

## 5. Coste técnico (para la implementación)

| Elemento | Detalle |
|---|---|
| Datos | `RoleExtras.of(...)`: 4 casos nuevos con la misma función `standard(...)`; `RoleExtrasTest` suma `lancer`, `swordsman`, `paladin`, `necromancer` |
| Lógica | `LancerKit`, `SwordsmanKit`, `PaladinKit` y `NecromancerKit` con sus `*Rules` puras y pruebas; se registran en `ExtraKits` |
| **Ganchos nuevos** | **Muertes de esbirros** (Banquete): `RoleEvents` hoy solo reacciona a muertes del jugador; hace falta llamar a `ExtraKits` cuando mata un esbirro. **Daño a esbirros** (Resurgir, Vínculo de huesos): un gancho para daño recibido por una entidad que no es jugador. **Daño a aliados** (Vínculo de escudo): un gancho al recibir daño que busque Paladines cercanos (se mantiene una lista de Paladines con el nodo, refrescada cada segundo) |
| Cuidado | Voluntad de acero reescribe el daño mortal (debe ir el último de la cadena); Estado de flujo y Lanza ancestral usan atributos temporales (velocidad de ataque, alcance); Estado de flujo usa `AbilityManager.reduceCooldown` |
| Textos | 80 claves en 5 idiomas vía `base_desc.py` y 40 nombres en `node_catalog.py` |
| Arte | `docs/PROMPT_ARTE_ICONOS_F4.md`; iconos provisionales vanilla |
| Documentación | CHANGELOG, BALANCE, TESTING, AGENTS, ROADMAP; sin opciones de configuración nuevas |

## 6. Qué necesito que apruebes

1. Los **4 roles** (Lancero, Espadachín, Paladín, Nigromante) como tanda F4, o prefieres cambiar alguno.
2. Las **40 habilidades**; dime cuáles cambiar.
3. ¿Los **tres ganchos nuevos** (muertes de esbirros, daño a esbirros, daño a aliados)? Tocan código que ya existe, así
   que son lo más delicado de la tanda.
4. ¿Implementarlas **todas de una vez** o en dos mitades?

## 7. Diferencias entre el diseño aprobado y la implementación

| Habilidad | Diseño | Implementado | Motivo |
|---|---|---|---|
| `paladin_n4` Golpe de escudo | Aturdimiento | Lentitud máxima y Debilidad durante el aturdimiento | El juego base no tiene un efecto de aturdimiento |
| `necromancer_n3` Mano de huesos | Inmóvil | Lentitud máxima (el efecto visual es de partículas) | Mismo motivo; sin entidades nuevas |
| `paladin_n9` Ira justa | +50 % de lo recibido | Por cada 10 % de vida máxima perdida, +20 % (tope +30 / 50 %) | Escala con la vida del jugador y no con el daño absoluto |
| `swordsman_n8` Estado de flujo | Velocidad de ataque | +30 % mediante un modificador temporal de atributo con id estable | Regla del proyecto para modificadores |
| `lancer_n8` Lanza ancestral | +2 de alcance | Modificador temporal de `PLAYER_ENTITY_INTERACTION_RANGE`; se retira al acabar o desconectarse | Regla del proyecto para modificadores |
| `necromancer_n9` Vínculo de huesos | Repartir el daño | El esbirro recibe el porcentaje como daño genérico; no se encadena | Evita bucles entre vínculos |
| Todas las activas | Entidades de efecto | Efectos con partículas y sonidos, sin entidades | Menos riesgo de entidades huérfanas |
