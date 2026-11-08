# Balance de Aptitudes — fórmulas, límites y decisiones

Documento de referencia de las fórmulas de progresión. Cada cambio indica fórmula anterior, nueva,
justificación, valores de ejemplo e impacto, como pide la política del proyecto.

## Tutorial guiado (v44)

- Recompensa nueva fija: **1 esmeralda por cada uno de los 6 pasos**, máximo **6 por jugador**.
  No hay XP adicional, objetos de evolución, bonificación de grupo ni escalado con nivel/configuración.
- Cada paso y su cobro se guardan por clave. Repetir, morir o reconectar no habilita otro pago.
  Si no cabe la esmeralda, el cobro espera; los pasos hechos antes de tiempo se recuerdan y se pagan en orden.
- La primera carga reconoce grimorio en inventario, nivel de alma positivo y rol principal guardados.
  Puede conceder hasta tres esmeraldas a un jugador anterior; los otros objetivos requieren acciones nuevas.
- Las acciones nuevas y los pagos solo cuentan para jugadores vivos fuera de creativo/espectador.
  La compra de nivel se valida en la Hoguera: no se observan comandos que añadan XP, aunque la procedencia
  histórica de la XP vanilla que paga una compra no puede distinguirse.
- No cambia ninguna fórmula ni valor por defecto existente. Para alcanzar cinco golpes se necesita
  `combosEnabled = true` y `comboFinisherHits >= 5`.
- Consultar `/aptitudes tutorial [status]` no cobra premios. `skip` conserva lo observado/cobrado,
  marca la omisión de forma irreversible y cancela las recompensas pendientes; no consume objetos ni XP.
  Se permite omitir también en creativo/espectador, pero nunca se paga por hacerlo.
- Ocultar el HUD es solo una preferencia local reversible. No omite el tutorial ni detiene sus ganchos.
  El HUD desaparece al terminar y cobrar todo, o al omitir; con todos los objetivos completos e inventario
  lleno muestra las recompensas pendientes. Se oculta en creativo, espectador, durante pantallas y al morir.

## Talentos por datapack (v55)

El piloto comparte el presupuesto de puntos de cada rol. Cada rango de `speed` cuesta un punto y
suma +2 % de velocidad de movimiento sobre la base del atributo. El modificador de ambos roles
activos tiene un tope propio de +6 %. Las Sendas conservan sus valores.

## Maestría (v43, T6 fase 1)

Antes se descartaba toda XP al máximo. Ahora solo la XP nueva elegible suma maestría, incluida la parte
sobrante de la acción que alcanza el máximo. Nunca se convierte la XP histórica de archivos antiguos,
comandos o cambios de curva: al cruzar el máximo, el crédito no puede superar la concesión natural nueva.
La XP normal restante se vacía al llegar al máximo como antes, sin acreditarla dos veces.

Coste del punto n (n empieza en 1): `base + step × (n−1)`. XP total para n puntos:
`base × n + step × n × (n−1) / 2`. Inicialmente 1000, 1250, 1500…; 20 puntos requieren 67 500 XP.
Se guarda la XP total por clave y se derivan los puntos, sin cobros ni conversiones al leer el guardado.
Cálculos en long, búsqueda binaria hasta 1000 puntos, XP saturada a 10¹² y bonus
`min(masteryMaxBonus, puntos × 0,005)` (por defecto 0,10).

El bonus multiplica la bonificación por nivel existente, no el valor base total ni los hitos:

| Habilidad | Pasivas que amplifica |
|---|---|
| Minería | Velocidad de rotura y armadura |
| Excavación | Velocidad de rotura y resistencia al empuje |
| Tala | Velocidad de rotura y vida máxima |
| Agricultura | Velocidad con azada, probabilidad de cosecha extra y suerte |
| Combate | Bonificación de daño cuerpo a cuerpo |
| Tiro con arco | Bonificación de daño base de flechas; la marca existente impide repetirla al cargar |
| Defensa | Armadura, dureza y resistencia al empuje por explosión |
| Agilidad | Velocidad de movimiento y caída segura |
| Pesca | Suerte y probabilidad de captura doble |
| Encantamiento | Maná máximo por nivel y probabilidad de reembolso; conserva el reembolso garantizado a 50 |

Ejemplo: Minería 50, `breakSpeedPerLevel=0,02`, 20 puntos: velocidad de `1+50×0,02=2`
a `1+50×0,02×1,10=2,10`. Las probabilidades conservan el límite de 1 y el daño conserva su circuito
y tope global existentes. Los modificadores usan los identificadores de siempre mediante `AttributeApplier.set`.

Roles al máximo (mínimo 50): eficiencia energética `1+bonus`. En la ejecución normal, característica
y definitiva se gasta `coste/(1+bonus)`, conservando fracciones. Con 20 puntos, 20 → 18,1818… de energía.
No aumenta rango, daño, curación, invocaciones ni reduce esperas. Las legendarias y objetos con su
propio consumo no cambian. El coste de sangre restante se calcula después del descuento de energía.

Comandos forzados no acreditan maestría. Creativo, espectador y jugadores muertos tampoco, incluso con
`awardXpInCreative`; una fuente creativa del grupo tampoco produce maestría para sus aliados. El reparto
legítimo existente sí cuenta para cada receptor una vez y mantiene sus fracciones/multiplicadores.
No hay una segunda distribución al convertirlo en maestría. Sin grupo, el atacante al máximo también gana.

El campo opcional `mastery` del attachment de progreso (`copyOnDeath`) conserva claves `skill/...`
y `role/...` al morir, reconectar y reiniciar. Cambiar de rol o reasignar atributos no lo borra.
`/roles admin reset` borra solo las maestrías de rol; `/aptitudes admin reset` borra todo el progreso,
incluida la maestría. Bajar niveles o desactivar la opción conserva el historial; el efecto espera al máximo.
Los hitos, puntos de árbol y nivel de alma no se incrementan con la maestría.

## Amenaza (v43, T6 fase 2)

Antes la distancia no modificaba estadísticas ni recompensas. El anillo nuevo usa solo X/Z:
`min(threatMaxRing, floor(hypot(x − apariciónX, z − apariciónZ) / threatRingSize))`.
Por defecto: 0 antes de 1000 bloques; I a 1000, II a 2000, III a 3000, IV a 4000 y V desde 5000.
Las coordenadas no finitas producen 0; no se cuadran/restan coordenadas en enteros que puedan desbordar.

| Anillo | Vida base × | Daño × | Probabilidad de tirada extra | Probabilidad natural de élite |
|---|---:|---:|---:|---:|
| 0 | 1 | 1 | 0 % | 5 % |
| I | 1,15 | 1,10 | 10 % | 6,25 % |
| II | 1,30 | 1,20 | 20 % | 7,50 % |
| III | 1,45 | 1,30 | 30 % | 8,75 % |
| IV | 1,60 | 1,40 | 40 % | 10 % |
| V | 1,75 | 1,50 | 50 % | 11,25 % |

- Vida: `1 + anillo × threatHealthPerRing`, tope ×3, mediante `AttributeApplier.set` e id
  `aptitudes:threat/health`. Es `ADD_MULTIPLIED_BASE`: se suma al incremento base de élite, sin multiplicarlo.
  Zombi base 20 → 35 en V; élite sin otros modificadores de vida: 40 → 55. Solo la primera aplicación
  conserva la fracción de vida; recargar, reactivar y comprobar exclusiones nunca cura.
- Daño: `1 + anillo × threatDamagePerRing`, tope ×2; se limita además al tope global positivo vigente.
  Se aplica una sola vez desde el gancho existente de daño de `Elites`, tanto a cuerpo a cuerpo como a
  proyectiles, magia o explosiones con atacante identificado. No se añade otro atributo de ataque.
  Solo modifica atacantes hostiles elegibles; no amplifica PvP ni ataques de esbirros aliados.
- Botín: probabilidad `anillo × threatLootPerRing`, máximo 1, de una tirada extra de la tabla real de la
  criatura, con contexto de daño, jugador, fortuna y Saqueo. Incluye tablas personalizadas: se capturan
  antes de que vanilla las vacíe al morir. Botín esperado ×`(1 + probabilidad)`, no garantiza objetos si
  esa tabla sale vacía. No vuelve a ejecutar recompensas de contratos/logros/élites, ni equipo ni XP.
  Solo una baja atribuida a un jugador fuera de creativo/espectador y con `doMobLoot=true` puede pagar.
  El attachment marca el cobro antes de generar objetos, también para tiradas fallidas/no elegibles.
  No hay consumo de inventario; en grupo existe una sola tirada por criatura, con drops físicos normales.
- Élite: `min(1, eliteChance × (1 + anillo × threatEliteChancePerRing))`, conservando el 10 % de esa
  probabilidad para campeón y el marcador persistente anterior de tirada comprobada. No añade una
  segunda tirada. En 0, desactivado o excluido conserva la probabilidad anterior.

Solo aparecen escaladas criaturas hostiles nuevas del Mundo Normal. Se excluyen Wither, Dragón,
Warden, Guardián anciano, jefes propios, invocaciones/esbirros, Eclipse/eventos y nombres previos.
Las criaturas convertidas por la tirada natural de élite mantienen la instantánea bajo su nombre generado.
Nombrar posteriormente una criatura ordinaria suspende vida/daño/botín. Las antiguas ya comprobadas sin
instantánea conservan sus estadísticas. El attachment de criatura `aptitudes:threat` guarda anillo,
vida, daño, probabilidad de botín y cobro; no usa `copyOnDeath` ni transfiere recompensas a jugadores.
La descarga/reconexión/reinicio no repite escalado ni tirada. Los cambios de curva afectan a apariciones
nuevas; desactivar retira la vida añadida en hasta 20 ticks (puede recortar vida actual al nuevo máximo),
desactiva daño/botín y conserva datos. Reactivar no cura. El HUD personal informa cada cambio de anillo
en un máximo de 20 ticks durante cuatro segundos; no modifica estadísticas ni recompensa al cruzar.

## 1. Atributos (Souls): escalado de puntos

**v27 — valores por punto −30 %** (decisión del autor: los atributos altos estaban demasiado fuertes):

| Atributo | Antes | Ahora |
|---|---|---|
| Vigor (vida) | 1,0 | 0,7 |
| Mente (maná) | 5 | 3,5 |
| Aguante (armadura) | 0,5 | 0,35 |
| Fuerza (daño) | 0,25 | 0,175 |
| Destreza (vel. ataque) | 1 % | 0,7 % |
| Inteligencia (daño de hechizos) | 2 % | 1,4 % |
| Fe (espera) | −1 % | −0,7 % |
| Suerte | 0,2 | 0,14 |

`attributeScaling` sigue en LINEAR por defecto (SOFTCAP queda como opción). Un `config/aptitudes.json` ya
existente conserva los valores antiguos: hay que borrar esas líneas (o el archivo) para recibir los nuevos.


### Estado actual medido (configuración por defecto, escalado LINEAR)

| Atributo | Por punto | 20 pt | 40 pt | 60 pt | 99 pt | 99 pt con afinidad ×1,5 |
|---|---|---|---|---|---|---|
| Vigor (vida) | 1 | 20 | 40 | 60 | 99 | 148,5 |
| Mente (maná) | 5 | 100 | 200 | 300 | 495 | 742,5 |
| Aguante (armadura) | 0,5 | 10 | 20 | 30 | 49,5 | 74,2 |
| Fuerza (daño) | 0,25 | 5 | 10 | 15 | 24,8 | 37,1 |
| Destreza (vel. ataque %) | 1 % | 20 | 40 | 60 | 99 | 148,5 |
| Inteligencia (daño mágico %) | 2 % | 40 | 80 | 120 | 198 | 297 |
| Fe (−enfriamiento %) | 1 % | 20 | 40 | 60 | 99* | 148,5* |
| Suerte | 0,2 | 4 | 8 | 12 | 19,8 | 29,7 |

\* La reducción de enfriamientos tiene un **tope global del 60 %** (todas las fuentes juntas).

Coste: 1 punto cada 3 niveles de alma; cada nivel cuesta `min(10, 1 + nivel/6)` niveles de XP.
99 puntos en un atributo ≈ 297 niveles de alma ≈ **2.700 niveles de XP vanilla** (alcanzable con
granjas de XP en un servidor de larga duración).

**Problemas detectados**
- Fuerza: +25 de daño plano (+37 con afinidad) rompe cualquier encuentro.
- Aguante: el atributo de armadura de Minecraft tiene un tope de 30. Por encima de ~40 puntos
  (más la armadura equipada) los puntos **no hacen nada**, aunque la pantalla siga sumando.
- Destreza +150 % e Inteligencia +300 % al máximo.

### Opción `attributeScaling` (nueva, implementada)

| Modo | Fórmula de puntos efectivos e(p) | Efecto |
|---|---|---|
| `LINEAR` (**por defecto**) | `e(p) = p` | Comportamiento original, sin cambios. |
| `SOFTCAP` | `p` hasta S1; `S1 + 0,5·(p−S1)` hasta S2; luego `+0,2` por punto | Idéntico hasta S1 = 40: no altera el juego temprano ni medio. |
| `SATURATION` | `L·(1 − e^(−p/L))`, L = 50 | Misma pendiente inicial; nunca supera L. Reduce también el juego medio (−15 % a 20 pt). |

Valor final = `e(p) × valorPorPunto × afinidad`.

| Puntos | LINEAR | SOFTCAP | SATURATION |
|---|---|---|---|
| 10 | 10,0 | 10,0 | 9,1 |
| 20 | 20,0 | 20,0 | 16,5 |
| 40 | 40,0 | 40,0 | 27,5 |
| 60 | 60,0 | 50,0 | 34,9 |
| 80 | 80,0 | 54,0 | 39,9 |
| 99 | 99,0 | 57,8 | 43,1 |

Ejemplo Fuerza (0,25/pt) a 99 puntos: LINEAR 24,8 · SOFTCAP 14,4 · SATURATION 10,8.

**Recomendación:** `SOFTCAP`. No cambia nada hasta 40 puntos por atributo (la inmensa mayoría de la
partida) y frena el apilamiento extremo. `SATURATION` es más agresivo y penaliza también a jugadores
medios. Se mantiene `LINEAR` por defecto hasta que lo pruebes en el servidor (regla: no cambiar el
balance general sin validación). Para activarlo: `"attributeScaling": "SOFTCAP"` en `config/aptitudes.json`.

## 2. Tope del multiplicador de daño (nuevo)

- **Antes:** sin tope. Las fuentes del mod se multiplican entre sí: rol (aditivo), set de armadura +
  arma de rol, mecánica de rol, Marca de muerte (×3), crítico de Senda (×1,5).
- **Medido:** un remate de Asesino bien equipado llegaba a **×29,5** (≈530 de daño con espada de
  netherita): mata de un golpe al Wither (300) o al Warden (500).
- **Ahora:** `resultado = min(resultado, daño_base × maxDamageMultiplier)` con `maxDamageMultiplier = 6`
  (0 lo desactiva). Se aplica a la suma de las fuentes del mod en `RolePassives.modifyOutgoingDamage`.
- **Impacto:** solo afecta a apilamientos extremos. Marca de muerte (×3) + set (×1,6) = ×4,8 → sin cambio.

## 3. Otros límites revisados

| Mecánica | Valor | Decisión |
|---|---|---|
| Reducción de enfriamientos (todas las fuentes) | tope 60 % | Correcto, sin cambios (incluye set de armadura y Amuleto arcano). |
| Reducción de daño de Sendas | tope 40 % | Correcto. |
| Desviar proyectiles (Monje) | 15 %·rango; con evolución el rango llega a 5 → **75 %** | **Cambiado:** tope 50 % (solo afecta a Monjes Definitivos). |
| Esquiva Monje (7 %·rango) + Esquiva de Senda | hasta ~35 % + 15 % | Alto pero aceptable; se vigila. |
| Probabilidades de reactivas (10 %·rango) | máx. 50 % con evolución | Aceptable. |

## 4. Habilidades por uso

- XP para subir: `xpBase · (nivel+1)^xpExponent` (20 · n^1,35), nivel máximo 50.
- Las fuentes de XP son acciones reales (picar, talar, matar, recibir daño, esprintar…).
- **Rendimiento decreciente por fuente repetida (v15, implementado).** Por jugador y fuente
  (`combat/<criatura>`, `archery/<criatura>`, `defense/<atacante>`, `fishing`) se cuentan los eventos de
  la ventana (60 s). Multiplicador: `m = 1` si `n ≤ U`; si no, `m = max(suelo, U / n)`, con U = 20 y
  suelo = 0,1. Ejemplos: 20 eventos/min → 100 %; 40 → 50 %; 200 → 10 %. En una granja de 200 muertes
  por minuto se obtiene aproximadamente un 33 % de la XP (medido en `XpDiminishingTest`). El juego normal
  (menos de 20 muertes del mismo tipo por minuto) no cambia. Memoria acotada: 1024 eventos por fuente y
  limpieza al desconectar.
- **Encantamiento (v21).** XP por objeto encantado (25), pasivas de reembolso de nivel y maná máximo, hitos
  10–50 (lapislázuli, +10 de maná, botellas de experiencia, −10 % de espera, reembolso garantizado). El
  −10 % entra en el tope global de reducción de esperas (60 %). Por defecto las habilidades no dan puntos
  de atributo (`pointsPerSkillLevel = 0`), así que la 10.ª habilidad no cambia los puntos; solo si un servidor
  activa esa opción subirían en `maxSkillLevel × pointsPerSkillLevel`.
- **Pesca (v15).** XP por pez (12), pasivas de suerte y captura doble, hitos 10–50. Lee la estadística
  vanilla `FISH_CAUGHT` una vez por segundo (sin mixins). Por defecto no cambia los puntos de atributo
  (`pointsPerSkillLevel = 0`); solo si un servidor activa esa opción subirían en
  `maxSkillLevel × pointsPerSkillLevel`.

## 4b. Combos (v28)

+3 % de daño por golpe de combo hasta +15 %, sumado al multiplicador del golpe directo y por tanto sujeto al
tope global ×6. Remate: 6 + golpes del combo de daño (de 11 en adelante), ×1,5 en la ejecución bajo el 40 % de
vida, ×0,6 a los objetivos secundarios. Espera de 4 s entre remates. No cuenta contra jugadores.

## 5. Árbol de roles

**v23 — filas de evolución.** Pasivas: valor de senda ×1,5 por rango en Maestro (2 rangos ⇒ el de una senda
completa) y ×3 en Definitivo (1 rango), sin el extra por evolución de `activeRank` (usan `learnedRank`). Los
topes globales siguen vigentes (reducción de daño ≤ 40 %, multiplicador de daño ≤ 6). Activas: daño base 6
(Maestro) / 10 (Definitivo) + 1,5 por rango efectivo (incluye Maestría y evolución, tope 6); coste 35/50 de
maná (+10 en invocaciones); espera 16/30 s (×2 en auras, escudos y curas; ×3 en invocaciones), afectadas por
Presteza, Eficiencia y el tope del 60 %. Puntos: +10 con Maestro y +5 con Definitivo.


- Puntos: `1 + nivel·3 / rolePointsEveryLevels` → 31 a nivel 30, frente a 69 rangos posibles en
  23 nodos (+1 legendario): obliga a elegir.
- Sendas: 3 por rol; **v46: exclusivas** (una por rol, validado en el servidor). Antes se podían seguir 2.

**v46 — nivel 50 y sendas más profundas** (`role/PathRules`, diseño en `docs/V46_DISENO_SENDAS.md`):

| Aspecto | Valor |
|---|---|
| Nivel máximo de rol | 50 (antes 30). La curva de XP no cambia: llegar al 30 cuesta lo mismo; del 30 al 50 hacen falta ≈ 107 400 XP más (≈ 2,4 veces todo lo anterior) |
| Puntos | La misma fórmula: 51 en el nivel 50 + 10 (Maestro) + 5 (Definitivo) = **66** |
| Escalones de cada senda | S1 10, S2 14, S3 20 (los de antes), S4 26, bifurcación S5x/S5y 32 (una sola), S6 40; 3 rangos cada uno, +5 niveles por rango (el 3.º de S6 llega al 50) |
| Valores | S4, S5x y S5y: el valor por rango de su `PathEffect`; **S6 = efecto de S4 ×1,5** |
| Coste de una build completa | 43 (nodos base) + 18 (S1–S6 de una senda) + 15 (filas de evolución) = 76 rangos para 66 puntos |
| Migración | Quien tenía 2 sendas conserva la de más rangos (empate: A, luego B) y recupera los puntos de la otra, con aviso en el chat |

**v46.1 — piedras angulares** (`role/KeystoneRules`, `ability/Keystones`; valores por defecto, configurables):

| Plantilla | Efecto | Sendas |
|---|---|---|
| Carnicería | Al matar: +10 % de la vida máxima y Prisa II 5 s | 8 |
| Baluarte | Con < 30 % de vida: Absorción II (8) y Resistencia I 6 s; espera 60 s | 8 |
| Tempestad | Críticos y 15 % de los golpes directos: chispa de 3 a otro enemigo a 5 bloques; espera 1 s | 8 |
| Sobrecarga | Cada 4.ª habilidad sin maná y con la mitad de espera | 6 |
| Espectro | +5 % de esquiva; tras esquivar, crítico seguro en 3 s | 6 |
| Detonación | Al matar: 4 de daño mágico a 3 bloques (sin cadenas, sin bloques, sin aliados) | 6 |
| Contagio | Al matar: veneno, lentitud, marchitamiento y fuego pasan a 2 enemigos a 4 bloques | 6 |
| Santuario | Cada 5 s: +2 de vida a ti y a los aliados a 8 bloques | 6 |
| Fortuna | 25 % de tirada de botín extra al matar; 10 % de esmeralda en élites | 4 |
| Legión | +1 esbirro máximo; los esbirros curan al dueño un 10 % de su daño | 2 (Nigromante, Domador) |

Notas: Tempestad y Espectro se ajustaron para que funcionen en sendas sin crítico o sin esquiva (chispa
también en el 15 % de los golpes; esquiva propia del +5 %). Fortuna aumenta el botín de las granjas de criaturas
(+25 % de media); si molesta en un servidor, baja `keystoneFortuneChance`. Con rol principal y secundario en el
nivel 50 se pueden tener dos piedras angulares a la vez.

Efecto de balance: con una sola senda, la suma de efectos de senda de un jugador al nivel 30 baja (antes 2
sendas × 9 rangos = 18; ahora 1 senda, 9 rangos hasta S3); a partir del 26 la profundidad lo compensa y en el 50
la senda completa da 18 rangos, con S6 al ×1,5. La piedra angular (S7) llega en la v46.1.
- Rol secundario: gana XP al 50 % (`secondaryRoleXpMultiplier`) y sus activas ocupan las teclas N/M.

# Runas para armas (v32)

Cada arma de rol admite dos runas distintas. Fuego aplica 3 segundos de ignición al golpear; Robo de vida cura
1 punto de salud y tiene enfriamiento de 4 segundos; Aceleración suma 10 % a la velocidad de ataque o reduce
2 ticks el enfriamiento del foco; Fuerza suma 1 punto de daño. Son valores fijos y no escalan con XP ni atributos.

# Jefes propios (v33 y v56)

El Centinela, el Oráculo y el Campeón anuncian su golpe final 20 ticks antes. El círculo queda fijo
con radio de 4,5 bloques para el Centinela y 6 para el Oráculo y el Campeón, con un margen vertical de
2 bloques. La cobertura sólida detiene el golpe. El daño de 5 / 4 / 4,5 puntos y la cadencia de
60 / 80 / 60 ticks, respectivamente, son los mismos. El Heraldo conserva su ataque.

Centinela de la Torre, Oráculo del Santuario y Campeón del Campamento aparecen en las estructuras actuales y conservan tres fases en sus etiquetas de entidad. Los tres comparten el escalado y la barra de campeón existente. En fase final, cada 3–4 s hacen un golpe de área telegráfico: 5, 4 o 4,5 puntos de daño respectivamente; el Oráculo también aplica lentitud.

Cada jefe deja un núcleo distinto. Aplicar un núcleo en la Mesa del gremio añade +0,5 de daño a un arma de evolución. Cada arma acepta una vez cada uno de los tres núcleos, hasta +1,5 total; no cambia la potencia de armas que no sean evolutivas.

# Reforja y Templado (v48)

| Operación | Trofeo | Fragmentos de alma |
|---|---|---:|
| Aplicar o cambiar un afijo en la Mesa del gremio | 1 Esencia de élite | 8 |
| Templado I en la Hoguera | 1 Corazón de campeón | 12 |
| Templado II en la Hoguera | 2 Corazones de campeón | 16 |

**Reserva** reduce el coste de activas equipadas en 2/4/6 % tras Maestría, según el rango 0/I/II.
Requiere sostener el arma del rol de la activa en la mano principal. El coste previo de 1 o más
tiene un mínimo de 1. Los costes inferiores a 1 conservan su valor.

**Impulso** incrementa la carga de recurso del golpe de hoja o disparo de foco en 5/10/15 %.
Ante un impacto válido contra un enemigo, la ganancia de 3 pasa a 3,15/3,30/3,45 antes del
tope del recurso. Los dos afijos son excluyentes y cambiar de afijo conserva el rango.

# Eclipse (v34)

| Elemento | Valor por defecto | Notas |
|---|---|---|
| Frecuencia | Cada 10 días, desde el día 10 | De 6:00 a 18:00 (10 min reales) |
| Oleada (v47) | 4 criaturas (Penumbra) / 8 (Totalidad) por jugador cada 30 s | Solicitudes acotadas por espacio y presupuesto; Retirada sin refuerzos |
| Población (v47) | 12 por jugador próximo, 24 por zona y 128 cargadas en el Mundo Normal | Área de 48 bloques; presupuesto compartido con o sin grupo y orden rotativo. Cargar criaturas previas por encima del límite bloquea nuevas apariciones |
| Criaturas | Vida y daño ×1,5 | Antes del escalado de élite (×2/×4 de vida) |
| Élites / campeones | 50 % / 10 % fuera de las garantías | Una élite por oleada y, en Totalidad, un campeón primero, si sus probabilidades son positivas y hay presupuesto/espacio. Las unidades restantes mantienen la tirada anterior |
| Equipo visible (v47) | Completo en élites: malla en Penumbra, hierro en Totalidad; campeones de diamante | Usa zombis/esqueletos. Irrompible y no se suelta; las unidades normales conservan 35 % por pieza. Interruptor de configuración para volver al equipo aleatorio |
| Heraldo | Campeón esqueleto wither con armadura de netherita | Vida ×(1 + 0,5 por jugador extra), máx. ×4; fases y golpe de área de la v33 |
| Fragmentos | Élite 1, campeón 3, Heraldo 8, normal 10 % → 1 | — |
| Runa del Eclipse | +15 % de daño contra élites, campeones y jefes | Multiplicador aditivo dentro del tope global `maxDamageMultiplier` |
| Núcleo del eclipse | +0,5 de daño en un arma de evolución | Cuenta para el tope de 3 núcleos por arma |
| Superviviente | 300 XP de rol principal y 200 de Combate | Solo si no mueres durante el evento |

El asalto aumenta la frecuencia efectiva de bajas élite/campeón y, por tanto, sus recompensas normales;
no aumenta la recompensa individual ni los multiplicadores base. Los límites y mínimos son configurables.
El Heraldo cuenta en el tope global y, si está justo lleno, sustituye una unidad ordinaria sin soltar botín;
no sustituye otros Heraldos. Si no hay espacio seguro o todos los huecos están ocupados por Heraldos,
no se fuerza una aparición. El límite no borra criaturas guardadas al volver a cargar sus chunks.

«Morir es fácil» por diseño (decisión del autor). Para suavizarlo en un servidor: bajar
`eclipseMobDamageMultiplier`, `eclipseEliteChance` o `eclipseMaxMobsPerPlayer`.

**v41.2 — criaturas más fuertes** (`event/StrongEventRules`, configurable):

| Fase | Criaturas (pesos) | Vida y daño | Armadura |
|---|---|---|---|
| Penumbra | zombi 25, esqueleto 25, araña 15, creeper 15, vindicador 10, bruja 10 (las de siempre) | ×1,5 (sin cambio) | +4 puntos; piezas de malla (35 % c/u) |
| Totalidad | zombi 20, esqueleto 15, araña 10, creeper 10, vindicador 20, bruja 10, **esqueleto wither 15** | ×1,5 × 1,25 = ×1,875 | +4 puntos; piezas de hierro (35 % c/u); +25 % resistencia al empuje |
| Campeones | — | escalado de campeón | piezas de diamante |

Efecto de balance: en la Totalidad las criaturas resisten ≈ +25 % más y la armadura (+4, ≈ 14 % menos daño
recibido sin contar las piezas) alarga los combates. Para volver a la v41: `eclipseTotalityBonus = 0`,
`eclipseMobArmor = 0`, `eclipseArmorChance = 0`; desde v47, también `eclipseGuaranteedEliteArmor = false`.

# Runas+ (v35)

| Runa | Efecto por defecto | Espera | Receta (Mesa del gremio) |
|---|---|---|---|
| Escarcha | Lentitud II 3 s al golpear | — | Hielo compacto ×4 + Fragmento de alma |
| Trueno | 25 %: descarga a 2 monstruos a ≤4 bloques, 3 de daño cada uno | 3 s | Cobre ×4 + pararrayos ×4 + Fragmento de alma |
| Guardia | +2 de absorción (tope 6) | 6 s | Escama de tortuga ×4 + Fragmento de alma |

- La descarga del Trueno nunca salta a jugadores ni a invocaciones y no vuelve a activar runas (sin cadenas infinitas).
- Quitar una runa cuesta 4 esmeraldas y la runa **se pierde**: cambiar de combinación tiene un coste real.
- Todos los valores de las runas (también los de la v32) son ahora configurables (`docs/CONFIGURATION.md`).

# Eventos rotativos (v41)

| Evento | Horario | Amenaza | Recompensa |
|---|---|---|---|
| Eclipse | 6:00–18:00 | Ver «Eclipse (v34)» | Fragmentos, Núcleo y Runa del Eclipse; XP de superviviente |
| Niebla de almas | 18:00–6:00 | Oleadas de 2 por jugador cada 30 s (solo no muertos a pie según el clima del bioma: templado 40 % zombi, 20 % zombi aldeano, 40 % esqueleto; agua 50 % ahogado, 20 % esqueleto pantanoso, 15 % zombi, 15 % esqueleto; cálido 50 % momia; frío 50 % errante — `event/SoulFogRules`); 15 % élites; tope = mitad del del Eclipse | 1 Fragmento de alma por criatura (25 %: 2) |
| Lluvia de brasas | 6:00–18:00 | Meteoritos cerca de cada jugador (≈ uno cada 25 s), sin romper bloques. v41.2: pequeño 70 % (potencia 1,5, aviso 2 s), grande 25 % (potencia 3, aviso 3 s, magma temporal radio 1) y enorme 5 % (potencia 4,5, aviso 4 s, magma radio 2); los grandes caen a 14–32 bloques del jugador. Criaturas del Nether con probabilidad 50 % (25 % en los pequeños): 1/2/3 según tamaño — pequeño: cubo de magma 70 %, blaze 30 %; grande: magma 35 %, blaze 35 %, piglin bruto 15 %, hoglin 15 %; enorme: blaze 30 %, esqueleto wither 25 %, piglin bruto 25 %, hoglin 20 % (25 % élites). Tope por zona = mitad del del Eclipse | Por tirada: hierro (50 %), cobre (30 %), oro (15 %) o diamante (5 %); 1 / 3 / 6 tiradas según tamaño; en los enormes, 8 % de chatarra de netherita por tirada |

Mismo calendario que antes (cada 10 días): la frecuencia total de eventos no cambia, pero el Eclipse pasa a ser
uno de cada tres eventos de media.

# Grupos (v29)

- Máximo de cinco integrantes, contando al líder. La invitación caduca tras 1200 ticks (60 s).
- En muertes que dan XP de Combate o Tiro con arco, el líder fija la fracción compartida del 0 al 50 % en pasos
  de cinco. El atacante conserva el 100 % del importe ya reducido por rendimiento decreciente; cada otro integrante
  conectado recibe además la fracción elegida. Esa XP adicional no vuelve a entrar en el reparto.
- La bonificación adicional usa el número de integrantes conectados: `min(tope, (miembros − 1) × 0,05)`, con
  máximo por defecto de 20 %. Se aplica a la XP de combate (Combate y Tiro con arco) y de rol de cada miembro
  conectado, incluido quien obtuvo la baja. No afecta a otras habilidades ni fuentes de XP.
- La XP de rol de combate se calcula después del multiplicador del rol principal/secundario y de especialización.
  Los demás miembros reciben la misma fracción para ese rol aunque no lo tengan activo. Se respeta su modo de
  juego y el tope de nivel del rol.
- El fuego amigo está desactivado inicialmente. El líder puede habilitarlo; el servidor bloquea ataques directos,
  proyectiles, habilidades y runas entre integrantes mediante la regla de aliados de `Targeting`.
- El apoyo con objetivos de jugadores cercanos conserva su radio y limita los objetivos al grupo cuando el lanzador
  pertenece a uno. Sin grupo, mantiene los objetivos anteriores.
- El Heraldo del Eclipse usa el tamaño del grupo persistente para escalar su vida y aparece una sola vez por grupo.
  Los grupos que no existen siguen agrupándose por cercanía de 64 bloques.

Efecto de balance a vigilar: compartir XP de rol permite que integrantes avancen el rol del atacante aunque no lo
tengan activo; es el comportamiento deliberado de la XP compartida. El rendimiento decreciente se calcula antes
del reparto para que las bajas repetidas no eludan el límite.

# Alquimia (v42)

| Poción | Efecto inicial | Duración bebible |
|---|---|---|
| Maná | +4 de energía por segundo, como máximo la energía máxima actual de `AbilityManager` | 10 s |
| Frenesí | +15 % de la velocidad de ataque base, con un único identificador de atributo | 60 s |
| Velo del eclipse | Daño ×0,80 si el atacante es una criatura marcada `aptitudes.eclipse` o `aptitudes.world_event`, incluidos sus proyectiles | 90 s |
| Vigor del bosque | Regeneración I (1 de vida cada 50 ticks), Saturación I instantánea (+1 de alimento y +2 de saturación, con los topes vanilla) | 30 s de regeneración |

Los tres efectos propios tienen potencia fija incluso si un comando les da un amplificador mayor.
Maná usa el reloj de la entidad: renovar la duración no fuerza un pulso de recuperación. No concede XP.
Frenesí solo cambia velocidad de ataque; no modifica daño ni el tope global `maxDamageMultiplier`.
El Velo no protege de jugadores, criaturas sin marca, caídas ni meteoritos sin atacante criatura.
Las reducciones existentes siguen aplicándose; el Velo por sí solo nunca da inmunidad.
Regeneración y Saturación conservan sus reglas vanilla, incluidos sus límites de vida y hambre.

Una dosis nueva usa la combinación de efectos vanilla (duración/potencia mayor, sin sumar modificadores).
Leche, caducidad y muerte retiran los efectos; duración y atributos se guardan por el mecanismo vanilla
al desconectar o reiniciar. No hay un attachment nuevo ni `copyOnDeath` para las pociones.
Las versiones arrojadizas, persistentes y flechas conservan la reducción de duración y aplicación de área
vanilla; pueden beneficiar a otras entidades, incluidos aliados o criaturas ajenas al grupo.

Se extrae 1 pétalo por saquito, consumiéndolo en la Mesa del gremio desde v49. Se conservan los
ingredientes y el uso medicinal del saquito. No se registran variantes prolongadas ni de potencia II.
Herbolista principal o secundario: velocidad ×1,5 (unos 13,35 s en lugar de 20 s) y duración ×1,5.
La ayuda cercana requiere 8 bloques; quien abrió realmente el soporte tiene prioridad hasta 32 bloques.
En ambos casos debe seguir conectado, vivo, en ese mundo y con el rol activo. No cuenta en creativo ni
espectador, incluso con `awardXpInCreative`. El propietario se guarda en el soporte, pero no evita que
otro Herbolista lo use y asuma la prioridad al abrirlo. Si no está disponible se elige al más cercano.
Dos Herbolistas no suman bonificaciones ni duplican la XP. No hay reparto de esta XP al grupo.

La duración se calcula desde los efectos de la receta original (no desde una duración ya ampliada),
con máximo 24 000 ticks; el amplificador no cambia y los instantáneos no se repiten. Se conserva el
identificador de poción para poder seguir destilando. Los efectos ampliados usan componentes vanilla
y se combinan por duración máxima, no por suma. El tooltip los presenta una sola vez. La elaboración
siguiente crea una poción nueva desde el registro vanilla y no arrastra la ampliación anterior.

XP: una concesión al terminar una operación real que produzca pociones con efectos, tanto de Aptitudes
como vanilla. Nada por abrir, recoger, introducir una poción ya hecha ni elaborar pociones sin efectos.
Por defecto, para el evento n en cinco minutos: `floor(4 × min(1, 3/n))`; desde el evento 13 da cero,
con un máximo de 25 XP base por ventana (antes del multiplicador de rol secundario/especialización).
La ventana empieza en la primera elaboración y se guarda en `alchemy_xp` con `copyOnDeath`.
Cambiar de soporte/rol, morir, reconectar o reiniciar no la borra; los comandos de XP no la alimentan.
Se conserva el consumo y cancelación vanilla. Un chunk descargado no se acelera ni fuerza su carga.

# Contratos del gremio (v30)

| Tipo | Objetivo por dificultad (s = 1 Fácil … 5 Legendario) | Desde |
|---|---|---|
| Caza (zombi, esqueleto, creeper, araña, enderman) | 6 + 4·s (+0–2) | Fácil |
| Entrega (hierro, trigo, zanahoria, bacalao, salmón, patata) | 16·s objetos, de golpe | Fácil |
| Pesca | 3 + 2·s (+0–2) | Fácil |
| Oficio (XP de una habilidad) | 150·s | Fácil |
| Encantamiento | 1 + s | Normal |
| Forja (armaduras de rol y runas en la Mesa del gremio) | 1 (2 en Élite y Legendario) | Normal |
| Evento (sobrevivir) | 1 | Difícil |
| Jefe | 1 | Legendario |

Recompensas: 2 + 2·s esmeraldas (+6 los de Jefe), 15 + 10·s XP de rol, +10 + 10·s reputación; Élite y
Legendario dan además Fragmentos de alma (1 + s/2), y a veces un vale de armería (Élite, 1/3) o un emblema de
maestría (Legendario, 1/4). Activos: 2 (3 desde Veterano, 300 de reputación).
