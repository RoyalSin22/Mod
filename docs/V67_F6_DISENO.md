# v67 · F6 — Habilidades nuevas de Cambiaformas, Mago pistolero, Invocador de armas y Cazador (diseño para aprobar)

Estado: **propuesta, sin código.** Mismo patrón que F2–F5 (`docs/V67_F5_DISENO.md`): 10 nodos por rol, 5 pasivas y 5
activas. Quedarán para F7 el Bardo, el Brujo abisal y el Domador (30 habilidades).

| Nodos | Sección | Posición | Niveles |
|---|---|---|---|
| N1 (pasiva), N2 (pasiva) | Talentos | casillas (2,3) y (2,4), tras la pasiva B del rol y N1 | 8 y 16 |
| N3 (activa), N4 (activa) | Talentos | casillas (6,3) y (6,5), tras Presteza y N3 | 9 y 20 |
| N5 pasiva, N6 activa, N7 pasiva | Veterano | fila centrada | 34, 36, 38 |
| N8 activa, N9 pasiva, N10 activa | Ascenso (exige Maestro) | fila centrada | 42, 45, 48 |

Rangos: N1–N4, 3; N5, N6, N8 y N9, 2; N7 y N10, 1. Cada rol acaba con 13 activas (caben en el códec de 4 bits). El daño de
las activas lo escala `extraSkillDamageScale`. Pasivas B (de las que cuelga N1): Cambiaformas `trickster`, Mago pistolero
`eagle_eye`, Invocador de armas `blade_guard`, Cazador `tracker`.

**Reglas de diseño:** cada habilidad aporta una mecánica que el rol aún no tiene, no repite nombres existentes y se
resuelve en el servidor con piezas que ya existen. Ninguna toca las exclusiones del proyecto.

---

## 1. Cambiaformas (formas, instinto)

Ya tiene: Mimetismo, Transformarse, Bromista, Poder de la forma, Instinto, Escurridizo, Devorar, Abalanzarse y Aullido de la bestia; firma Caos de formas; definitiva Forma primordial. Le falta: **ventajas por estar transformado**, **morder y curarse**, **cambiar de forma al instante** y **un arranque tras cada cambio**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `shapeshifter_n1` | Metabolismo animal | Talentos | Pasiva | 8 | 3 | — | Transformado, recuperas 0,5 / 1 / 1,5 de vida cada 3 s |
| `shapeshifter_n2` | Sangre fría | Talentos | Pasiva | 16 | 3 | — | Transformado, recibes −4 / 8 / 12 % de daño |
| `shapeshifter_n3` | Mordisco salvaje | Talentos | Activa | 9 | 3 | 20 maná, 8 s | Muerdes al enemigo que miras (4 bloques): 6 / 8 / 10 de daño y te curas 2 / 3 / 4 |
| `shapeshifter_n4` | Cambio relámpago | Talentos | Activa | 20 | 3 | 25 maná, 20 s | Pasas al instante a tu forma siguiente (o vuelves a humano) y ganas Velocidad II 3 / 4 / 5 s |
| `shapeshifter_n5` | Piel de repuesto | Veterano | Pasiva | 34 | 2 | — | Al volver a humano, ganas 4 / 6 de absorción durante 5 s |
| `shapeshifter_n6` | Zarpazo múltiple | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 4 zarpazos en cono de 5 bloques durante 1 s: 5 / 6 de daño cada uno |
| `shapeshifter_n7` | Presa fácil | Veterano | Pasiva | 38 | 1 | — | Tu primer golpe tras transformarte hace +40 % de daño |
| `shapeshifter_n8` | Furia de la bestia | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: +30 % de daño y +20 % de velocidad de ataque |
| `shapeshifter_n9` | Tercer cambio | Ascenso | Pasiva | 45 | 2 | — | Tras transformarte, tu siguiente Poder de la forma cuesta un 20 / 40 % menos de maná |
| `shapeshifter_n10` | Estampida primigenia | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | 5 s de embestidas: cada 0,5 s hieres a los enemigos a 4 bloques con 8 de daño y los empujas |

**Riesgo:** Necesita leer la forma actual y el cambio de forma (`morph/MorphManager`); Cambio relámpago reutiliza `select`/`morph`. Furia de la bestia + Presa fácil + Instinto apilan daño (`maxDamageMultiplier`).

---

## 2. Mago pistolero (disparos, tambor)

Ya tiene: Desenfunde rápido, Disparo arcano, Ojo certero, Vaciar el tambor, Tambor arcano, Rodar, Recarga, Rebote y Ojo de la muerte; firma Ráfaga sin fin; definitiva Mediodía. Le falta: **distancia y puntería**, **control** (aturdir y granadas), **disparo penetrante** y **un remate de ejecución**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `gunslinger_n1` | Manos firmes | Talentos | Pasiva | 8 | 3 | — | Tus ataques a distancia a 15 bloques o más hacen +5 / 10 / 15 % de daño |
| `gunslinger_n2` | Cabeza fría | Talentos | Pasiva | 16 | 3 | — | Tus críticos a distancia te devuelven 2 / 3 / 4 de maná |
| `gunslinger_n3` | Bala aturdidora | Talentos | Activa | 9 | 3 | 20 maná, 10 s | Disparo que hace 6 / 8 / 10 de daño y deja al objetivo con Lentitud IV 2 s |
| `gunslinger_n4` | Granada de pólvora | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Lanzas una granada; a los 2 s explota en 4 bloques: 8 / 10 / 12 de daño |
| `gunslinger_n5` | Presión | Veterano | Pasiva | 34 | 2 | — | Tus ataques a distancia tienen un 10 / 20 % de dejar al objetivo con Lentitud I 2 s |
| `gunslinger_n6` | Disparo a través | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Bala de 24 bloques que atraviesa a todos los enemigos de la línea: 12 / 16 de daño |
| `gunslinger_n7` | Sin remordimientos | Veterano | Pasiva | 38 | 1 | — | La 6.ª bala (explosiva) recorta 2 s las esperas de tus activas |
| `gunslinger_n8` | Doble pistola | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: tus ataques a distancia disparan una bala extra con el 50 % del daño |
| `gunslinger_n9` | Pulso de tirador | Ascenso | Pasiva | 45 | 2 | — | Tras 3 s sin disparar, tu primer disparo hace +40 / 60 % de daño |
| `gunslinger_n10` | Ejecución | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Apuntas 1 s y disparas hasta 3 enemigos en 24 bloques: matan a los que tengan menos del 25 % de vida (no a jefes); 25 de daño al resto |

**Riesgo:** Los ataques a distancia no son «golpe directo»: hay que reconocer disparos propios (como en el Tambor arcano). Ejecución no actúa sobre jefes.

---

## 3. Invocador de armas (espadas flotantes)

Ya tiene: Armería, Andanada, Guardia de espadas, Lluvia de espadas, Arsenal flotante, Hoja interceptora, Reforjar, Espadón espectral y Prisión de espadas; firma Mil espadas; definitiva Arsenal infinito. Le falta: **mejorar las espadas**, **espadas que vuelven**, **defensa con espadas** y **orbitar para dañar**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `weapon_summoner_n1` | Espadas afiladas | Talentos | Pasiva | 8 | 3 | — | Tus espadas flotantes hacen +10 / 20 / 30 % de daño |
| `weapon_summoner_n2` | Equilibrio | Talentos | Pasiva | 16 | 3 | — | Con 3 o más espadas flotantes, tus golpes cuerpo a cuerpo hacen +4 / 8 / 12 % de daño |
| `weapon_summoner_n3` | Espada boomerang | Talentos | Activa | 9 | 3 | 20 maná, 8 s | Lanzas una espada en línea (16 bloques) que atraviesa y vuelve: 6 / 8 / 10 de daño a la ida y a la vuelta |
| `weapon_summoner_n4` | Muro de espadas | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Una pared de espadas de 6 bloques durante 4 s: daña y frena a quien la cruza (3 / 4 / 5 por segundo) |
| `weapon_summoner_n5` | Hoja de repuesto | Veterano | Pasiva | 34 | 2 | — | Al bajar del 30 % de vida, invocas 3 / 5 espadas flotantes (una vez cada 60 s) |
| `weapon_summoner_n6` | Danza de acero | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 5 s: tus espadas giran y hieren a los enemigos a 4 bloques cada 0,5 s (3 / 4 de daño) |
| `weapon_summoner_n7` | Filo perfecto | Veterano | Pasiva | 38 | 1 | — | Cada 10.º golpe hace el doble de daño |
| `weapon_summoner_n8` | Armería viva | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: +2 espadas máximas y las espadas atacan al doble de ritmo |
| `weapon_summoner_n9` | Eco de acero | Ascenso | Pasiva | 45 | 2 | — | Un 30 / 50 % de que tus espadas vuelvan a tu lado en vez de gastarse al impactar |
| `weapon_summoner_n10` | Juicio de acero | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Todas tus espadas flotantes (y 5 más) caen sobre el objetivo y los enemigos a 4 bloques: 8 de daño cada una |

**Riesgo:** Hay que leer y consumir las espadas flotantes (clase del rol: pendiente de revisar al implementar). Espadas afiladas + Armería viva + Eco de acero podrían duplicar el daño continuo; se limitan con `maxDamageMultiplier` y las esperas.

---

## 4. Cazador (marcas, trampas, arco)

Ya tiene: Depredador, Marca del cazador, Rastreador, Trampa, La cacería, Camuflaje, Trofeo, Lluvia de flechas y Ataque del halcón; firma Disparo del cazador; definitiva Cacería salvaje. Le falta: **distancia y paciencia**, **inmovilizar presas**, **disparos explosivos** y **una cacería final**.

| Id | Nombre | Sección | Tipo | Nv. | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `hunter_n1` | Mira certera | Talentos | Pasiva | 8 | 3 | — | Tus ataques a distancia a 12 bloques o más hacen +6 / 12 / 18 % de daño |
| `hunter_n2` | Presa herida | Talentos | Pasiva | 16 | 3 | — | La presa con menos de la mitad de vida recibe +4 / 8 / 12 % de tus golpes |
| `hunter_n3` | Red de caza | Talentos | Activa | 9 | 3 | 20 maná, 12 s | Lanzas una red: los enemigos a 3 bloques del punto que miras quedan inmóviles 2 / 3 / 4 s |
| `hunter_n4` | Flecha explosiva | Talentos | Activa | 20 | 3 | 30 maná, 18 s | Flecha que hace 6 / 8 / 10 de daño y explota en 3 bloques (la mitad a los de alrededor) |
| `hunter_n5` | Trampero experto | Veterano | Pasiva | 34 | 2 | — | Tus trampas duran un 50 / 100 % más y puedes tener 2 a la vez |
| `hunter_n6` | Disparo en abanico | Veterano | Activa | 36 | 2 | 45 maná, 30 s | 5 flechas en cono de 8 bloques: 5 / 6 de daño cada una |
| `hunter_n7` | Cazador paciente | Veterano | Pasiva | 38 | 1 | — | Agachado y quieto 3 s, tu siguiente disparo hace +50 % de daño |
| `hunter_n8` | Vista de halcón | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | 8 / 10 s: tus presas marcadas brillan y reciben +25 % de daño de todos tus ataques |
| `hunter_n9` | Botín de caza | Ascenso | Pasiva | 45 | 2 | — | Las presas marcadas que matas sueltan 1 / 2 objetos extra de su botín |
| `hunter_n10` | Cacería final | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Marcas a todos los enemigos a 20 bloques durante 6 s y tu halcón los ataca: 8 de daño cada uno |

**Riesgo:** Necesita leer el estado de la Marca del cazador y las trampas (clases del rol); Botín de caza duplica botín (revisar con `Elites`). Vista de halcón + Marca + La cacería apilan daño contra una presa.

---

## 5. Coste técnico (para la implementación)

| Elemento | Detalle |
|---|---|
| Datos | `RoleExtras.of(...)`: 4 casos nuevos con `standard(...)` (pasivas B `trickster`, `eagle_eye`, `blade_guard`, `tracker`); `RoleExtrasTest` suma `shapeshifter`, `gunslinger`, `weapon_summoner`, `hunter` |
| Lógica | `ShapeshifterKit`, `GunslingerKit`, `WeaponSummonerKit` y `HunterKit` con sus `*Rules` puras y pruebas; se registran solo en `ExtraKits` |
| Ganchos nuevos | Posiblemente ninguno. A confirmar al implementar: (1) cómo identificar un **disparo propio** del Pistolero y del Cazador (flechas y balas) en `outgoing`; (2) el estado de las **espadas flotantes** y de la **marca del cazador** y las trampas (hoy en `RoleMechanics`/`AbilityState`), que habrá que exponer con métodos de consulta, no duplicar |
| Cuidado | Cambio relámpago reutiliza `MorphManager.select/morph`; Ejecución no actúa sobre jefes (`Elites`/`BossManager`); Botín de caza no debe duplicar botín de élites ni campeones |
| Textos | 80 claves en 5 idiomas vía `base_desc.py` y 40 nombres en `node_catalog.py` |
| Arte | `docs/PROMPT_ARTE_ICONOS_F6.md`; iconos provisionales vanilla |
| Documentación | CHANGELOG, BALANCE, TESTING, AGENTS, ROADMAP; sin opciones de configuración nuevas |

## 6. Qué necesito que apruebes

1. Los **4 roles** (Cambiaformas, Mago pistolero, Invocador de armas, Cazador) como tanda F6, o prefieres cambiar alguno.
2. Las **40 habilidades**; dime cuáles cambiar. Las del Invocador de armas y el Cazador dependen de estado que ya existe
   (espadas, marca, trampas) y las revisaré primero para no duplicarlo.
3. ¿Implementarlas **todas de una vez**, como en F4 y F5?
