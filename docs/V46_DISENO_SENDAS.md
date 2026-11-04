# v46 — Nivel 50 y Sendas exclusivas (diseño para aprobar)

Estado: **aprobado** por el autor (2026-11-04); pendiente de implementar. Decisiones: nivel máximo de rol
**50**, **sendas exclusivas**, estructura de **7 escalones con bifurcación**, las **10 plantillas** de piedra
angular, el ejemplo del Bersérker como patrón para los 20 roles, **reembolso automático** de la senda con menos
rangos para quien tenga dos y **curva de XP sin cambios**.

## 1. Situación actual (medida en el código)

| Aspecto | Hoy | Dónde |
|---|---|---|
| Nivel máximo de rol | 30 (`maxRoleLevel`) | `AptitudesConfig` |
| Puntos de árbol | 1 + nivel (con `rolePointsEveryLevels = 3`) → **31** en el nivel 30, +10 Maestro, +5 Definitivo = **46** | `RoleRules.earnedPoints` |
| Sendas | 3 por rol (A, B, C) de 3 nodos (niveles 10/14/20, 3 rangos), cada nodo = un `PathEffect`; **se pueden seguir 2** | `RoleTree.PATHS`, `MAX_PATHS = 2` |
| Coste total del árbol | 14 nodos base × 3 + legendaria 1 = 43; 2 sendas = 18; filas de evolución 15 → **76 rangos para 46 puntos** (60 %) | — |
| Evoluciones | Maestro en el nivel 20; Definitivo en el 30 | `Evolution` |
| Curva de XP | `30 × (nivel+1)^1,4` → 45 590 XP hasta el 30 | `RoleRules.xpToNext` |

## 2. Reglas propuestas

| Aspecto | Propuesta | Motivo |
|---|---|---|
| Nivel máximo | **50** (`maxRoleLevel = 50`) | Decisión del autor |
| Curva de XP | **Sin cambios**: el nivel 30 cuesta lo mismo que hoy; del 30 al 50 hacen falta 107 400 XP más (≈ 2,4 veces todo lo anterior) | No alterar la progresión ya probada; el tramo 30–50 es el contenido de final de juego |
| Puntos | La misma fórmula: **51** en el nivel 50 + 15 por evoluciones = **66** | Sin campos nuevos |
| Evoluciones | Maestro 20 y Definitivo 30, **sin cambios** | Ya probado en partida |
| Sendas | **Exclusivas**: el primer rango en una senda la elige y bloquea las otras dos de ese rol (`MAX_PATHS = 1`) | Decisión del autor; identidad de cada build |
| Profundidad | Cada senda pasa de 3 a **7 escalones** (ver §3), con una **bifurcación** interna y una **piedra angular** en el nivel 50 | «Ramificar más el árbol» |
| Coste con la nueva senda | 43 + 19 (senda completa) + 15 = **77 rangos para 66 puntos** (86 %) | Sigue habiendo que elegir, pero el nivel 50 casi completa una build |
| Cambiar de senda | Con `/roles respec` (si `allowSelfRespec`) o el Ritual de renacimiento, igual que hoy | Sin sistema nuevo |

## 3. Estructura de una senda

```
nivel 10  [S1] ─┐                         (existente, 3 rangos)
nivel 14  [S2]  │                         (existente, 3 rangos)
nivel 20  [S3]  │                         (existente, 3 rangos)
nivel 26  [S4]  │                         NUEVO, 3 rangos
nivel 32  [S5x]  o  [S5y]                 NUEVO: bifurcación, se elige UNA (3 rangos)
nivel 40  [S6]                            NUEVO, 3 rangos; exige S5x o S5y
nivel 50  [S7] PIEDRA ANGULAR             NUEVO, 1 rango, efecto único de la senda
```

- **S4, S5x, S5y y S6** son datos: cada uno aplica un `PathEffect` existente (robo de vida, crítico, ejecución,
  espinas, reducción, esquiva, maná, recurso, regeneración, efectos al golpear, esbirros, antiempuje, velocidad,
  vida, velocidad de ataque, suerte), con el valor por rango de siempre (S6 ×1,5, como las filas de Maestro).
- **Bifurcación (S5x / S5y)**: dos variantes de la misma senda; al aprender una, la otra queda bloqueada
  (misma regla que las sendas, un nivel más abajo). Así una misma senda da dos builds.
- **Piedra angular (S7)**: 1 rango, nivel 50. Efecto con mecánica propia elegido de **10 plantillas** con
  parámetros (como las activas de la v23): 60 piedras angulares sin escribir 60 clases.

### Plantillas de piedra angular

| Plantilla | Efecto (valores por defecto) | Encaja con |
|---|---|---|
| CARNICERÍA | Al matar: cura 10 % de la vida máx. y +20 % de velocidad de ataque 5 s | Sendas de robo de vida y ejecución |
| BALUARTE | Al bajar del 30 % de vida: 8 de absorción y Resistencia I 6 s (espera 60 s) | Sendas defensivas |
| SOBRECARGA | Cada 4.ª activa no gasta maná y reduce a la mitad su espera | Sendas de maná y recurso |
| DETONACIÓN | Al matar: explosión de 4 de daño en 3 bloques (no rompe bloques ni daña aliados) | Sendas de daño en área |
| CONTAGIO | Los efectos al golpear (veneno, lentitud, fuego, marchitar) se propagan a 2 enemigos cercanos al morir el objetivo | Sendas de efectos |
| LEGIÓN | +1 esbirro máximo; los esbirros roban el 10 % del daño que hacen | Sendas de esbirros |
| ESPECTRO | Tras esquivar, el siguiente golpe en 3 s es crítico seguro | Sendas de esquiva |
| FORTUNA | +1 tirada de botín de las criaturas y 10 % de esmeralda al matar élites | Sendas de suerte |
| TEMPESTAD | Los críticos lanzan una chispa a un enemigo cercano (3 de daño, espera 1 s) | Sendas de crítico |
| SANTUARIO | Cada 5 s cura 1 corazón a los aliados del grupo a 8 bloques | Sendas de apoyo y regeneración |

Todas respetan los topes globales (reducción de daño ≤ 40 %, `maxDamageMultiplier`), no dañan aliados
(`PartyManager.isAlly`) y sus números van a la configuración.

## 4. Ejemplo completo: Bersérker

| Senda | S1–S3 (hoy) | S4 | S5x / S5y | S6 | Piedra angular |
|---|---|---|---|---|---|
| A — Carnicero | robo de vida, crítico, ejecución | vel. ataque | **Sangría** (robo de vida) / **Verdugo** (ejecución) | crítico ×1,5 | CARNICERÍA |
| B — Coloso | espinas, antiempuje, reducción | vida | **Muralla** (reducción) / **Púas** (espinas) | vida ×1,5 | BALUARTE |
| C — Torbellino | vel. ataque, velocidad, regeneración | esquiva | **Vendaval** (velocidad) / **Furia** (vel. ataque) | regeneración ×1,5 | TEMPESTAD |

Los otros 19 roles siguen el mismo patrón; la tabla completa (60 sendas, 240 nodos de datos y 60 piedras
angulares) irá en este documento cuando apruebes el patrón y las plantillas.

## 5. Interfaz (pantalla de roles, J)

- La cuadrícula ya tiene zoom, arrastre y desplazamiento (v19): se amplía hacia abajo.
- Las sendas se alargan en sus columnas (3, 4, 5); la bifurcación ocupa dos medias columnas. La legendaria y
  las filas de evolución bajan de fila (las posiciones son solo de dibujo: no se guardan).
- Al elegir senda, las otras dos se cubren con el velo y el candado de la v23 («Senda bloqueada: ya sigues
  *Carnicero*»). El nombre de la senda elegida aparece como título del rol en la pantalla de roles y en la K.
- Cada senda con su color (borde de nodos y líneas).

## 6. Datos y compatibilidad

| Riesgo | Solución |
|---|---|
| Ids nuevos | `<rol>_a4`, `<rol>_a5x`, `<rol>_a5y`, `<rol>_a6`, `<rol>_a7` (y `_b…`, `_c…`). Los existentes (`_a1…_c3`) no cambian. Se guardan por clave: mundos antiguos válidos |
| **Jugadores con 2 sendas empezadas** | Al entrar por primera vez con la v46: se conserva la senda con más rangos (empate: A, luego B), se **devuelven** los puntos de la otra y se avisa en el chat. Alternativa: un respec gratis de sendas |
| Nivel guardado > nuevo máximo | `setLevel` ya acota al máximo; subir el máximo no rompe nada |
| Activas equipadas | Las sendas no añaden activas: `LoadoutCodec` no cambia |
| Textos | ≈ 600 claves (título y descripción de 300 nodos) en 8 idiomas: generadas con un script como `tools/lang/gen_evolved_lang.py` |
| Cliente/servidor | Validación en el servidor (`RoleManager.upgrade`): senda bloqueada, bifurcación bloqueada, nivel y puntos |

## 7. Código previsto

| Pieza | Tipo |
|---|---|
| `role/PathRules` (puro): elección de senda, bifurcación, escalones y migración | Nuevo, con `role/PathRulesTest` |
| `role/Keystone` + `ability/Keystones`: 10 plantillas con parámetros | Nuevo |
| `RoleTree`: datos de S4–S7 y posiciones | Ampliación |
| `RoleManager.upgrade`: reglas de exclusividad y bifurcación | Ampliación |
| `RolesScreen`: filas nuevas, velo de sendas, título y colores | Ampliación |
| Configuración: `maxRoleLevel = 50` y valores de las piedras angulares | Cambio de un valor por defecto (aprobado) + campos nuevos |
| Grimorio: capítulo de Sendas actualizado | Textos |

## 8. Plan de entrega

1. **v46.0** — Nivel 50, sendas exclusivas, migración, S4–S6 con bifurcación y la interfaz.
2. **v46.1** — Las 10 plantillas de piedra angular y las 60 asignaciones.
3. **v45** (traducciones) después de esto, para no traducir dos veces.

## 9. Decisiones (cerradas)

1. Estructura de 7 escalones con bifurcación: **sí**.
2. Las 10 plantillas de piedra angular: **sí**.
3. El Bersérker como patrón para los 20 roles: **sí**.
4. Migración: **reembolso automático** de la senda con menos rangos, con aviso en el chat.
5. Curva de XP: **sin cambios**.
