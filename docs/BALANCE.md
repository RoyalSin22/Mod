# Balance de Aptitudes — fórmulas, límites y decisiones

Documento de referencia de las fórmulas de progresión. Cada cambio indica fórmula anterior, nueva,
justificación, valores de ejemplo e impacto, como pide la política del proyecto.

## 1. Atributos (Souls): escalado de puntos

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
  −10 % entra en el tope global de reducción de esperas (60 %). Con la 10.ª habilidad, los puntos de
  atributo que se pueden ganar vuelven a subir en `maxSkillLevel × pointsPerSkillLevel`.
- **Pesca (v15).** XP por pez (12), pasivas de suerte y captura doble, hitos 10–50. Lee la estadística
  vanilla `FISH_CAUGHT` una vez por segundo (sin mixins). Efecto global: con una habilidad más, los puntos
  de atributo que se pueden ganar suben en `maxSkillLevel × pointsPerSkillLevel`; el tope por atributo
  (`maxPointsPerAttribute`) no cambia.

## 5. Árbol de roles

- Puntos: `1 + nivel·3 / rolePointsEveryLevels` → 31 a nivel 30, frente a 69 rangos posibles en
  23 nodos (+1 legendario): obliga a elegir.
- Sendas: 3 por rol, solo se pueden seguir 2 (validado en servidor y persistido en los rangos).
- Rol secundario: gana XP al 50 % (`secondaryRoleXpMultiplier`) y sus activas ocupan las teclas N/M.
