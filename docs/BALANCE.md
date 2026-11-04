# Balance de Aptitudes — fórmulas, límites y decisiones

Documento de referencia de las fórmulas de progresión. Cada cambio indica fórmula anterior, nueva,
justificación, valores de ejemplo e impacto, como pide la política del proyecto.

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
- Sendas: 3 por rol, solo se pueden seguir 2 (validado en servidor y persistido en los rangos).
- Rol secundario: gana XP al 50 % (`secondaryRoleXpMultiplier`) y sus activas ocupan las teclas N/M.

# Runas para armas (v32)

Cada arma de rol admite dos runas distintas. Fuego aplica 3 segundos de ignición al golpear; Robo de vida cura
1 punto de salud y tiene enfriamiento de 4 segundos; Aceleración suma 10 % a la velocidad de ataque o reduce
2 ticks el enfriamiento del foco; Fuerza suma 1 punto de daño. Son valores fijos y no escalan con XP ni atributos.

# Jefes propios (v33)

Centinela de la Torre, Oráculo del Santuario y Campeón del Campamento aparecen en las estructuras actuales y conservan tres fases en sus etiquetas de entidad. Los tres comparten el escalado y la barra de campeón existente. En fase final, cada 3–4 s hacen un golpe de área telegráfico: 5, 4 o 4,5 puntos de daño respectivamente; el Oráculo también aplica lentitud.

Cada jefe deja un núcleo distinto. Aplicar un núcleo en la Mesa del gremio añade +0,5 de daño a un arma de evolución. Cada arma acepta una vez cada uno de los tres núcleos, hasta +1,5 total; no cambia la potencia de armas que no sean evolutivas.

# Eclipse (v34)

| Elemento | Valor por defecto | Notas |
|---|---|---|
| Frecuencia | Cada 10 días, desde el día 10 | De 6:00 a 18:00 (10 min reales) |
| Oleada | 2 criaturas (Penumbra) / 4 (Totalidad) por jugador cada 30 s | Tope de 12 vivas por jugador |
| Criaturas | Vida y daño ×1,5 | Antes del escalado de élite (×2/×4 de vida) |
| Élites / campeones | 50 % / 10 % | Recompensas normales de élite y campeón, más fragmentos |
| Heraldo | Campeón esqueleto wither con armadura de netherita | Vida ×(1 + 0,5 por jugador extra), máx. ×4; fases y golpe de área de la v33 |
| Fragmentos | Élite 1, campeón 3, Heraldo 8, normal 10 % → 1 | — |
| Runa del Eclipse | +15 % de daño contra élites, campeones y jefes | Multiplicador aditivo dentro del tope global `maxDamageMultiplier` |
| Núcleo del eclipse | +0,5 de daño en un arma de evolución | Cuenta para el tope de 3 núcleos por arma |
| Superviviente | 300 XP de rol principal y 200 de Combate | Solo si no mueres durante el evento |

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
`eclipseMobArmor = 0`, `eclipseArmorChance = 0`.

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

# Contratos del gremio (v30)

| Tipo | Objetivo por dificultad (s = 1 Fácil … 5 Legendario) | Desde |
|---|---|---|
| Caza (zombi, esqueleto, creeper, araña, enderman) | 6 + 4·s (+0–2) | Fácil |
| Entrega (hierro, trigo, zanahoria, bacalao, salmón, patata) | 16·s objetos, de golpe | Fácil |
| Pesca | 3 + 2·s (+0–2) | Fácil |
| Oficio (XP de una habilidad) | 150·s | Fácil |
| Encantamiento | 1 + s | Normal |
| Forja (Mesa del gremio) | 1 (2 en Élite y Legendario) | Normal |
| Evento (sobrevivir) | 1 | Difícil |
| Jefe | 1 | Legendario |

Recompensas: 2 + 2·s esmeraldas (+6 los de Jefe), 15 + 10·s XP de rol, +10 + 10·s reputación; Élite y
Legendario dan además Fragmentos de alma (1 + s/2), y a veces un vale de armería (Élite, 1/3) o un emblema de
maestría (Legendario, 1/4). Activos: 2 (3 desde Veterano, 300 de reputación).
