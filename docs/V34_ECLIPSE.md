# v34 — El Eclipse (evento del mundo)

Estado: **implementado** en la rama `feature/v34-eclipse` (pendiente de compilar en el PC del autor y de
probar en partida; ver `docs/TESTING.md`, sección v34).
Depende de: v32 (runas) y v33 (jefes), ya en `main`. Las integraciones con grupos, contratos y logros
(v29–v31) se añaden cuando esas versiones estén en `main`.

## Decisiones del autor

| Pregunta | Decisión |
|---|---|
| Cielo | **Teñido de rojo** con mixins de cliente (`require = 0`), con un sol negro rodeado de un anillo carmesí |
| Amenaza | Élites, campeones y un **jefe** que aparecen a **horas fijas** del evento |
| Frecuencia | **Cada 10 días** de juego |
| Dificultad | **Morir es fácil** (estilo Souls) |
| Por defecto | **Activado** |

**Límites de diseño (obligatorios):**

- **Inspiración estética, no copia.** Ni nombres, ni personajes, ni objetos de ninguna obra existente.
  Todo nombre, textura y texto es propio.
- **Exclusiones del proyecto.** El evento **no** incluye sacrificios, intercambios de vidas o almas por
  poder, marcas de sacrificio ni pactos, ni directa ni indirectamente. Es un evento de supervivencia y
  combate.

## 1. Calendario

Un día de Minecraft dura 24 000 ticks: `timeOfDay` 0 = 6:00, 6000 = 12:00, 12 000 = 18:00.

- Día del eclipse: `día % eclipseIntervalDays == 0` y `día >= eclipseFirstDay` (día = `timeOfDay
  total / 24 000`). Valores por defecto: intervalo 10 y primer eclipse el día 10, para dar margen a los
  mundos nuevos.
- Es un **eclipse solar**: ocurre **de día**, de 6:00 a 18:00 del día del eclipse.
  - Durante el día no se puede dormir, así que no se puede saltar el evento.
  - Las criaturas hostiles naturales no aparecen de día, así que el evento controla sus propias oleadas.
- **Aviso** al atardecer anterior (18:00): mensaje en el chat («El sol del mañana amanecerá
  oscurecido…») y un sonido grave para todos los jugadores del Mundo Normal.

| Fase | Hora (ticks) | Qué pasa |
|---|---|---|
| **Penumbra** | 6:00–9:00 (0–3000) | El cielo se tiñe poco a poco. Primeras oleadas, pequeñas, de élites |
| **Totalidad** | 9:00–15:00 (3000–9000) | Sol negro con anillo carmesí, cielo rojo oscuro, ceniza. Oleadas cada `eclipseWaveIntervalSeconds` con muchos élites y campeones |
| **Heraldo** | 12:00 (6000) | Aparece el **Heraldo del Eclipse** (jefe) cerca de cada grupo de jugadores |
| **Retirada** | 15:00–18:00 (9000–12 000) | Sin oleadas nuevas. Lo que queda sigue atacando. El cielo se aclara |
| **Fin** | 18:00 | Las criaturas del eclipse que queden se disuelven (partículas) y los supervivientes reciben recompensa |

**Robustez:**

- Si un administrador cambia la hora (`/time set`), la fase se recalcula con la hora nueva. Si la hora
  sale del rango del evento, el evento termina limpiamente.
- Si `doDaylightCycle` está desactivado, el evento termina como mucho a los 12 000 ticks de tiempo de
  juego desde su inicio (tope de seguridad).
- Con dificultad **Pacífica** no aparecen criaturas; el resto del evento (cielo, avisos) sí ocurre.

## 2. Amenaza («morir es fácil»)

- **Oleadas** alrededor de **cada jugador** del Mundo Normal (no en el Nether ni el End):
  - A 16–32 bloques, en suelo válido y sin aparecer a la vista directa del jugador si es posible.
  - Tipos: zombi, esqueleto, araña, creeper, vindicador y bruja (lista configurable más adelante).
  - Tope de criaturas del eclipse vivas por jugador: `eclipseMaxMobsPerPlayer`, por rendimiento.
- **Criaturas del eclipse** (etiqueta `aptitudes.eclipse`):
  - Vida × `eclipseMobHealthMultiplier` y daño × `eclipseMobDamageMultiplier`, con `AttributeApplier.set`
    o los modificadores de `Elites`, siempre con ids estables.
  - Probabilidad de élite `eclipseEliteChance` y de campeón `eclipseChampionChance`, con
    `Elites.makeElite`.
  - **Casco oscuro irrompible** (cuero teñido, sin soltarlo) para que los no muertos no ardan de día. Se
    descartó la resistencia al fuego: anularía la Runa de Fuego y los ataques ígneos de los jugadores. Al
    terminar, las criaturas se disuelven.
- **Heraldo del Eclipse:**
  - Nuevo `BossKind.ECLIPSE_HERALD`, **añadido al final del enum**.
  - Aparece con `BossManager.spawn(...)`. Ya reutiliza fases, barra y escalado de campeón.
  - Uno por grupo de jugadores: los jugadores a menos de 64 bloques entre sí cuentan como un grupo.
  - Su vida escala con el número de jugadores del grupo, con tope.
  - Base propuesta: un wither esqueleto con armadura oscura. Nombre traducible
    `boss.aptitudes.eclipse_herald`.
- Las muertes de jugadores siguen las reglas actuales: mancha de XP de la Hoguera, sin castigos extra.

## 3. Recompensas

| Recompensa | Fuente |
|---|---|
| **Fragmento del eclipse** (`aptitudes:eclipse_fragment`, objeto nuevo) | Élite: 1. Campeón: 3. Heraldo: 8. Las criaturas normales del eclipse: 10 % de probabilidad |
| **Runa del Eclipse** (`RuneKind.ECLIPSE`, **al final del enum**) | Receta con fragmentos. Efecto: +15 % de daño contra élites, campeones y jefes (valor en `RuneRules`, con prueba) |
| **Núcleo del eclipse** (`aptitudes:eclipse_core`) | Lo suelta el Heraldo. Mejora un arma de evolución con `BossManager.upgrade`. Cuenta para el tope de 3 núcleos (`BossRules.MAX_WEAPON_CORES`); hay que añadirlo a `BossRules.isCore` |
| **Superviviente del eclipse** | Al terminar, cada jugador del Mundo Normal que no murió durante el evento recibe XP del rol principal y de Combate (cantidades configurables) |

## 4. Cielo y ambiente (cliente)

Mixins de **cliente** nuevos, **cada inyector con `require = 0`**. Si un nombre no coincide, el juego
arranca igual y solo se pierde el efecto. **Verifica cada firma en Yarn `1.21.1+build.3`.**

| Efecto | Objetivo propuesto | Cómo |
|---|---|---|
| Color del cielo | `ClientWorld#getSkyColor(Vec3d, float)` → `Vec3d` | `@Inject(at = RETURN)`: mezclar hacia carmesí oscuro (≈ `0x5A0A0A`) según la intensidad |
| Niebla | `BackgroundRenderer#render(...)`: el color que pasa a `RenderSystem.clearColor` | `@ModifyArgs` o equivalente, mezcla hacia rojo oscuro |
| Nubes | `ClientWorld#getCloudsColor(float)` | Mezcla hacia granate |
| Sol eclipsado | `WorldRenderer#renderSky(...)`: la textura del sol (`textures/environment/sun.png`) | Sustituir la textura del sol por `aptitudes:textures/environment/eclipse_sun.png` (disco negro con anillo carmesí) **solo durante el eclipse** |

- **Intensidad** de 0 a 1: sube en la Penumbra, vale 1 en la Totalidad y baja en la Retirada. La
  calcula una función pura con la hora (en `EclipseRules`) y el cliente la interpola.
- **Sin mixins también se ve el evento:** viñeta carmesí en el HUD (`HudRenderCallback`), partículas
  de ceniza alrededor del jugador, relámpagos solo visuales en la Totalidad y un sonido ambiental grave.
- **Accesibilidad:** opción de cliente `eclipseSkyEffects` (en `ClientConfig`) para desactivar el tinte
  del cielo y los destellos; algunos jugadores son sensibles a los destellos.
- **Textura del sol** generada por script (`tools/textures/gen_eclipse.py`, semilla fija), no a mano.
  Diseño propio: disco negro, anillo de corona roja e irregular y un leve halo. El sol se dibuja con mezcla
  aditiva: el disco negro no tapa, deja ver el cielo carmesí, y lo que se ve es el anillo.
- **Mixins sin `@Shadow`:** un campo sombreado que no existiera tumbaría el arranque aunque los inyectores
  tengan `require = 0`. La niebla se tiñe modificando los argumentos de `RenderSystem.clearColor` y
  reescribiendo el color de niebla del shader tras `applyFogColor`.

## 5. Arquitectura

| Pieza | Responsabilidad |
|---|---|
| `event/EclipseRules` (**pura**, sin Minecraft) | `isEclipseDay(day, interval, firstDay)`, `phase(timeOfDay)`, `intensity(timeOfDay)`, `waveSize(players, phase)`, `heraldHealth(groupSize)`, multiplicadores con tope. Todo con `long` y límites |
| `event/EclipseState` (`PersistentState` del Mundo Normal) | Activo o no, día del eclipse en curso, Heraldo ya generado, tick de inicio y jugadores que murieron durante el evento |
| `event/EclipseManager` | Tick del servidor (cada 20 ticks): fase, aviso, oleadas, Heraldo, fin y recompensas. Registra los eventos de muerte de criaturas (fragmentos) y de jugadores |
| `network/EclipsePayload` (S2C) | `phase` y `intensity`. Se envía al entrar, al cambiar de fase y cada 5 s durante el evento |
| `client/ClientEclipse` + mixins + `client/hud/EclipseHud` | Estado del cliente, cielo, viñeta, partículas y sonido |
| Comando `/aptitudes eclipse start\|stop\|status\|next` | Nivel de permiso 2. `next` dice cuántos días faltan |

**Pruebas:** `event/EclipseRulesTest`, con nombre nuevo, que solo llame a métodos de `EclipseRules`
que existan. Casos mínimos:

- días 0, 9, 10, 20 y 25 con intervalo 10 y primer día 10;
- fases en los límites (0, 2999, 3000, 6000, 8999, 9000, 11 999, 12 000);
- la intensidad, siempre en [0, 1];
- los multiplicadores y la vida del Heraldo, que nunca superan sus topes, ni con entradas extremas o
  negativas.

## 6. Configuración (`AptitudesConfig`, con límites en `validated(...)` y filas en `docs/CONFIGURATION.md`)

| Campo | Defecto | Límites |
|---|---|---|
| `eclipseEnabled` | `true` | — |
| `eclipseIntervalDays` | 10 | 1–365 |
| `eclipseFirstDay` | 10 | 1–3650 |
| `eclipseWaveIntervalSeconds` | 30 | 10–300 |
| `eclipseMaxMobsPerPlayer` | 12 | 0–40 |
| `eclipseMobHealthMultiplier` | 1.5 | 1.0–5.0 |
| `eclipseMobDamageMultiplier` | 1.5 | 1.0–5.0 |
| `eclipseEliteChance` | 0.5 | 0.0–1.0 |
| `eclipseChampionChance` | 0.1 | 0.0–1.0 |
| `eclipseHeraldEnabled` | `true` | — |
| `eclipseSurvivorRoleXp` | 300 | 0–100 000 |
| `eclipseSurvivorCombatXp` | 200 | 0–100 000 |

El tope global `maxDamageMultiplier` limita el daño que hacen los jugadores; el de las criaturas del eclipse
lo limita su propio campo (máximo ×5).

## 7. Textos e interfaz

- Claves nuevas en `en_us` y `es_es`, copiadas a las 6 variantes. **Sin reformatear** los JSON (2
  espacios).
- Grimorio: capítulo «El Eclipse» (calendario, fases, recompensas). La receta de la runa se lee del
  juego, no se copia en el texto.
- HUD durante el evento: nombre de la fase y una barra de progreso fina arriba, que no tape la barra del
  jefe.

## 8. Integraciones pendientes (cuando estén en `main`)

- **Grupos (v29):** las oleadas y el Heraldo escalan con el grupo, y la recompensa de superviviente se
  aplica a todo el grupo.
- **Contratos (v30):** «Sobrevive a un eclipse» y «Derrota al Heraldo del Eclipse» (Legendario).
- **Logros (v31):** primer eclipse sobrevivido, Heraldo derrotado, Runa del Eclipse engarzada.

## 9. Riesgos

| Riesgo | Mitigación |
|---|---|
| Rendimiento con muchos jugadores | Tope por jugador, tick cada 20 ticks y oleadas escalonadas |
| Mixins de renderizado frágiles entre versiones | `require = 0`; el evento sigue siendo visible con la viñeta y las partículas |
| Duelo injusto para un jugador nuevo | El primer eclipse es el día 10; existe `eclipseEnabled` y el comando `stop` |
| Criaturas del eclipse persistentes tras el evento | Se disuelven al terminar y al cargar el mundo si el evento ya no está activo |
