# v67 · F3 — Habilidades nuevas de Mago, Asesino, Herbolista e Ingeniero (diseño para aprobar)

Estado: **propuesta, sin código.** Sigue el patrón de F2 (`docs/V67_ARBOL_UNIFICADO.md` §8): 10 nodos por rol, 5 pasivas y
5 activas, en el mismo reparto.

| Nodos | Sección | Posición | Niveles |
|---|---|---|---|
| N1 (pasiva), N2 (pasiva) | Talentos | casillas (2,3) y (2,4), tras la Pasiva B del rol y N1 | 8 y 16 |
| N3 (activa), N4 (activa) | Talentos | casillas (6,3) y (6,5), tras Presteza y N3 | 9 y 20 |
| N5 pasiva, N6 activa, N7 pasiva | Veterano | fila centrada | 34, 36, 38 |
| N8 activa, N9 pasiva, N10 activa | Ascenso (exige Maestro) | fila centrada | 42, 45, 48 |

Rangos: N1–N4 tienen 3; N5, N6, N8 y N9 tienen 2; N7 y N10 tienen 1. Las activas son 5 por rol, así que cada rol acaba
con 13 (el equipamiento admite 16). El daño de las activas lo escala `extraSkillDamageScale`, como en el Bersérker.

**Criterio de poder** (el mismo que en F2): las activas rinden por debajo de la firma, de la definitiva y de
Terremoto (10 + 1,5 por rango); las pasivas compiten por puntos con las Sendas. Las cifras son un punto de partida
para ajustar en partida.

**Reglas de diseño:** cada habilidad aporta una mecánica que el rol aún no tiene, no repite nombres existentes (los de
Maestro/Definitivo, firmas, definitivas y piedras angulares) y se puede resolver en el servidor con las piezas que ya
existen (`Targeting`, `AbilityState`, `CastFx`, ganchos de `RoleMechanics`).

---

## 1. Mago (arcano, maná)

Ya tiene: Afinidad arcana, Bola de fuego, Mente clara, Parpadeo, Cargas arcanas, Escudo de maná, Eco de hechizo,
Distorsión temporal, Misiles arcanos; Maestro/Definitivo (Rayo prismático, Nova de escarcha, Lluvia de meteoros,
Singularidad…). Lo que le falta: **recompensar tener el maná lleno**, **control con hielo** fuera de la evolución,
**encadenar** objetivos y **combinar** habilidades distintas.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `mage_n1` | Plenitud arcana | Talentos | Pasiva | 8 | 3 | — | Con el maná al 80 % o más, tu daño mágico sube +4 / 8 / 12 % |
| `mage_n2` | Escarcha latente | Talentos | Pasiva | 16 | 3 | — | Tu daño mágico ralentiza (Lentitud I) 2 / 3 / 4 s |
| `mage_n3` | Rayo encadenado | Talentos | Activa | 9 | 3 | 22 maná, 12 s | Rayo que salta entre 3 / 4 / 5 enemigos (6 bloques entre saltos): 4 / 5 / 6 de daño |
| `mage_n4` | Escudo especular | Talentos | Activa | 20 | 3 | 25 maná, 25 s | 3 s: el siguiente golpe que recibas se anula y se devuelve como daño mágico ×1,5 / 2 / 2,5 |
| `mage_n5` | Concatenación | Veterano | Pasiva | 34 | 2 | — | Tras usar 3 activas distintas en 8 s, la siguiente no gasta maná (rango 2: y espera −50 %) |
| `mage_n6` | Tormenta de granizo | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 6 s sobre el punto que miras (5 bloques): 6 pulsos de 3 / 4 de daño y Lentitud II |
| `mage_n7` | Reserva de emergencia | Veterano | Pasiva | 38 | 1 | — | Si tu maná baja del 10 %, recuperas el 40 % (una vez cada 60 s) |
| `mage_n8` | Forma de arcano puro | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: tus activas cuestan la mitad y haces +20 % de daño mágico; al acabar, tu maná baja al 20 % |
| `mage_n9` | Maná cristalizado | Ascenso | Pasiva | 45 | 2 | — | Cada 100 de maná gastado, tu siguiente hechizo estalla alrededor del objetivo: 6 / 9 de daño |
| `mage_n10` | Nova estelar | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Tras 1,5 s de carga, nova de 10 bloques: 18 de daño y lanza; devuelve 5 de maná por enemigo (máx. 40) |

**Riesgo:** Forma de arcano puro y Concatenación juntas podrían encadenar habilidades casi gratis; la mitad de coste
(no gratis) y el vaciado de maná final lo limitan.

---

## 2. Asesino (sigilo, golpes por la espalda)

Ya tiene: Sombra, Desaparecer, Puñalada, Paso sombrío, Ritmo letal, Huida en humo, Desvanecer al matar, Abanico de
cuchillas, Shuriken; Maestro/Definitivo (Danza de cuchillas, Salto de sombra, Marca de muerte, Eclipse…). Lo que le
falta: **explotar enemigos debilitados**, **zona de humo**, **cadenas de objetivos** y **sigilo que no se rompe**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `assassin_n1` | Punto débil | Talentos | Pasiva | 8 | 3 | — | +5 / 10 / 15 % de daño a enemigos con algún efecto negativo (veneno, lentitud, debilidad…) |
| `assassin_n2` | Eslabón | Talentos | Pasiva | 16 | 3 | — | Matar reduce un 15 / 25 / 35 % la espera de la última activa que usaste |
| `assassin_n3` | Humo cegador | Talentos | Activa | 9 | 3 | 20 maná, 14 s | Bomba de humo de 4 bloques durante 4 s: los enemigos dentro pierden el objetivo y sufren Ceguera; tú ganas Velocidad |
| `assassin_n4` | Corte relámpago | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Te lanzas por la espalda del enemigo más cercano (8 bloques) y lo golpeas: 8 / 10 / 12 de daño, crítico seguro |
| `assassin_n5` | Sombra persistente | Veterano | Pasiva | 34 | 2 | — | Tras salir de la invisibilidad, tu siguiente golpe hace +40 / 60 % |
| `assassin_n6` | Cadena de sombras | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Saltas entre hasta 3 enemigos en 3 s: 5 / 6 de daño por salto |
| `assassin_n7` | Veneno concentrado | Veterano | Pasiva | 38 | 1 | — | Tus golpes con veneno aplican Veneno II y dobles de duración |
| `assassin_n8` | Velo de muerte | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 6 / 8 s: tu invisibilidad no se rompe al atacar y haces +30 % de daño |
| `assassin_n9` | Cosecha silenciosa | Ascenso | Pasiva | 45 | 2 | — | Matar desde sigilo o invisible alarga 2 / 3 s esa invisibilidad y te cura 2 |
| `assassin_n10` | Noche eterna | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 6 s, 12 bloques: los enemigos quedan cegados y no te ven; tu daño contra ellos sube +40 % y siempre cuenta como por la espalda |

**Riesgo:** Velo de muerte, Noche eterna y Sombra persistente apilan multiplicadores; `maxDamageMultiplier` los limita.

---

## 3. Herbolista (curación, venenos)

Ya tiene: Savia vital, Bálsamo, Inmunidad herbal, Espinas venenosas, Floración, Antídoto, Piel espinosa, Lluvia
curativa, Nube de esporas; Maestro/Definitivo (Flor curativa, Jardín sagrado, Enredadera…). Lo que le falta: **potenciar
toda su curación**, **efectos sobre los aliados curados**, **daño por zona de plantas** y un **gran golpe de curación**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `herbalist_n1` | Primavera | Talentos | Pasiva | 8 | 3 | — | Toda tu curación (Bálsamo, Lluvia, Floración) +10 / 20 / 30 % |
| `herbalist_n2` | Polinización | Talentos | Pasiva | 16 | 3 | — | Los aliados que curas ganan Velocidad I y 2 / 3 / 4 de absorción durante 5 s |
| `herbalist_n3` | Semilla explosiva | Talentos | Activa | 9 | 3 | 20 maná, 12 s | Lanzas una semilla que explota a los 2 s: 5 / 6,5 / 8 de daño y Veneno; deja un matorral que cura 1 / s a los aliados 4 s |
| `herbalist_n4` | Raíz curativa | Talentos | Activa | 20 | 3 | 40 maná, 40 s | Zona de 5 bloques durante 10 / 12 / 14 s: cura 1 / s a los aliados e inmoviliza a medias (Lentitud II) a los enemigos |
| `herbalist_n5` | Corteza viva | Veterano | Pasiva | 34 | 2 | — | Bajo el 35 % de vida, Regeneración II (III en rango 2) durante 6 s (una vez cada 45 s) |
| `herbalist_n6` | Plaga de langostas | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Enjambre que sigue al objetivo 5 s: 2 / 3 de daño por segundo y Debilidad |
| `herbalist_n7` | Antídoto universal | Veterano | Pasiva | 38 | 1 | — | Cuando quitas un efecto negativo (tuyo o ajeno), 5 s de inmunidad a ese efecto |
| `herbalist_n8` | Floración colosal | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | Los aliados a 8 bloques recuperan el 40 / 60 % de su vida y ganan Absorción II; los enemigos cercanos quedan envenenados |
| `herbalist_n9` | Raíz profunda | Ascenso | Pasiva | 45 | 2 | — | Quieto 2 s: −10 / 20 % de daño recibido y espinas de 1 / 2 |
| `herbalist_n10` | Lluvia de semillas vivas | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 12 s, 12 bloques: los aliados curan 2 / s y se limpian de efectos negativos cada 3 s; los enemigos reciben Veneno |

**Riesgo:** curación de grupo muy fuerte; su rendimiento depende de si el servidor juega en grupo (hay que probarlo).

---

## 4. Ingeniero (artilugios, energía)

Ya tiene: Ingeniería, Propulsores, Batería, Torreta, Sobremarcha, Escudo automático, Reparación de campo, Gancho,
Granada de pulso; Maestro/Definitivo (Pulso eléctrico, Mortero, Rayo láser, Escudo de energía, Bombardeo orbital…). Lo
que le falta: **minas**, **reciclar energía**, **cúpula protectora**, un **exotraje** y una **torre de rayos**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `engineer_n1` | Aislante | Talentos | Pasiva | 8 | 3 | — | −10 / 20 / 30 % de daño de explosiones, fuego y rayos |
| `engineer_n2` | Reciclaje | Talentos | Pasiva | 16 | 3 | — | Los enemigos que mueren por tu torreta, mina o granada te devuelven 4 / 6 / 8 de maná |
| `engineer_n3` | Mina de proximidad | Talentos | Activa | 9 | 3 | 20 maná, 10 s | Hasta 3 minas (30 s) que estallan cuando un enemigo se acerca a 2 bloques: 6 / 8 / 10, sin romper bloques |
| `engineer_n4` | Dron de apoyo | Talentos | Activa | 20 | 3 | 40 maná, 35 s | Dron virtual 15 / 20 / 25 s: dispara al enemigo más cercano (2 de daño por disparo, cada 0,5 s) |
| `engineer_n5` | Calor residual | Veterano | Pasiva | 34 | 2 | — | Al acabar Sobremarcha, una explosión a tu alrededor: 6 / 8 de daño |
| `engineer_n6` | Domo protector | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 5 s, radio de 4: bloquea proyectiles y los aliados dentro reciben −40 % (−50 % en rango 2) de daño |
| `engineer_n7` | Modo ahorro | Veterano | Pasiva | 38 | 1 | — | Tus artilugios (torreta, minas, dron, domo) cuestan un 20 % menos de maná y duran un 30 % más |
| `engineer_n8` | Exotraje | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 10 / 14 s: +4 de armadura, +4 corazones de absorción, salto mayor e inmune al empuje |
| `engineer_n9` | Cadena de montaje | Ascenso | Pasiva | 45 | 2 | — | Cada activa que lances reduce 1 / 2 s la espera de tus otros artilugios |
| `engineer_n10` | Torre Tesla | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 12 s: una torre que descarga cada segundo sobre hasta 3 enemigos a 10 bloques (3 de daño, encadena) |

**Riesgo:** «Bombardeo orbital» ya es la definitiva del rol; por eso no hay un segundo ataque orbital.

---

## 5. Coste técnico (para la implementación)

| Elemento | Detalle |
|---|---|
| Datos | `RoleExtras.of(...)`, 4 casos nuevos (`mage`, `assassin`, `herbalist`, `engineer`) con la misma estructura que `berserker`; `RoleExtrasTest` pasa a recorrer todos los roles que tengan datos |
| Lógica | Cuatro clases nuevas (`MageKit`, `AssassinKit`, `HerbalistKit`, `EngineerKit`), cada una con sus reglas puras (`MageRules`…) y pruebas JUnit, como `BerserkerKit`/`BerserkerRules` |
| Ganchos | Los mismos que el Bersérker: `RoleMechanics.outgoing`, `RolePassives` (`incoming`/`afterOutgoing`), `AbilityManager.onCast`, `RoleEvents.tick`, desconexión. Se pueden agrupar en una interfaz común para no repetir el cableado cuatro veces |
| Cuidado | Concatenación y Forma de arcano puro tocan el coste del maná (`AbilityManager.execute`); Eslabón y Cadena de montaje tocan las esperas (`CooldownBook`); Exotraje y Domo usan atributos temporales como Avatar |
| Textos | 80 claves nuevas (40 nombres y 40 descripciones) en los 5 idiomas, vía `base_desc.py` |
| Arte | 40 iconos nuevos: `docs/PROMPT_ARTE_ICONOS_F3.md` (a preparar tras aprobar), con los marcadores provisionales de objetos vanilla |
| Documentación | CHANGELOG, BALANCE, CONFIGURATION (sin opciones nuevas: usa `extraSkillDamageScale`), TESTING, AGENTS |

## 6. Qué necesito que apruebes

1. **Los 4 roles** (Mago, Asesino, Herbolista, Ingeniero) como tanda F3, en el orden en que aparecen en el juego.
2. Las **40 habilidades** (nombres, mecánicas y cifras); dime cuáles cambiar.
3. Si prefieres **implementarlas en dos mitades** (Mago + Asesino, luego Herbolista + Ingeniero) para probar antes, o las 4 de una vez.
