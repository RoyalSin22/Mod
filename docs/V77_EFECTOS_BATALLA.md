# V77 — Efectos de batalla (fase 1: Bersérker)

Rehace los efectos de las habilidades con una coreografía propia por rol: sonido, partículas con física, cámara y pantalla.
La fase 1 cubre las cuatro activas base del Bersérker (Furia, Grito de guerra, Salto demoledor y Torbellino). Los demás roles y
las activas nuevas siguen con el efecto genérico de `CastFx` hasta que se les haga su guion.

## Arquitectura

| Capa | Pieza | Qué hace |
|---|---|---|
| Servidor | `ability/<Rol>Fx` (p. ej. `BerserkerFx`) | Decide cuándo y dónde; reproduce los sonidos y manda **un** evento. Sustituye al sello y remate genéricos de `CastFx` (`CastFx.onCast(..., abilityId)` solo hace el gesto de la mano si el guion existe) |
| Red | `network/FxEventPayload`, `ability/FxEvents` | Un paquete compacto por efecto (id, entidad, punto, dirección, color, potencia, semilla), solo a jugadores a menos de 48 bloques. El cliente descarta ids desconocidos y valores no finitos |
| Cliente | `client/fx/FxClient` | Recibe el evento, aplica calidad de partículas e intensidad y llama a la coreografía del rol |
| Cliente | `client/fx/<Rol>ClientFx` | Dibuja el efecto. Todo sale de la semilla del evento: todos ven el mismo desorden |
| Cliente | `ChunkParticle` | Trozo de bloque con gravedad, rebote (hasta 3), giro, rodadura y encogimiento. Usa la textura real del suelo |
| Cliente | `FxTimeline` | Calendario de pistas por tick (ondas que se expanden, grietas que avanzan). Una pista que falla no tumba el cliente |
| Cliente | `ScreenDynamics` | Pulso de FOV, retroceso suave de cámara, sacudida con ruido continuo y viñeteado de color |
| Cliente | `mixin/GameRendererMixin` (`getFov`, `require = 0`) y `CameraMixin` | Aplican FOV y desplazamiento de cámara. Solo visual: no cambian hacia dónde mira el jugador ni el FOV de la mano |
| Puro | `logic/FxMath`, `logic/FxIds` | Envolventes, atenuación por distancia, ruido suave, geometría de conos y anillos, recuento de partículas (JUnit en `FxMathTest`) |
| Sonido | `tools/audio/gen_sounds.py` | Seis sonidos sintetizados: `berserker_roar`, `_war_cry`, `_slam`, `_whoosh`, `_heartbeat` y `_leap` (no editar los `.ogg` a mano) |

## Configuración del jugador (cliente)

- `fxIntensity` en `config/aptitudes-client.json` (0 a 1,5, por defecto 1): escala partículas, FOV, retroceso, sacudida y viñeteado.
  0 apaga los efectos. Aún no tiene control en pantalla.
- `screenEffects` apaga la parte de cámara y pantalla (FOV, retroceso, sacudida, viñeteado y destellos).
- El ajuste de partículas del juego (Todas / Reducidas / Mínimas) reduce el número de partículas a 100 % / 50 % / 20 %.

## Cómo añadir un rol

1. Añade los ids a `FxIds` (siguiente número libre) y sus casos en `FxClient.dispatch`.
2. Crea `ability/<Rol>Fx` con `handles(id)` y un método por habilidad que reproduzca sonidos y llame a `FxEvents.on/at`.
3. Enlaza esos métodos en el código de cada habilidad y añade el rol a la condición de `CastFx.onCast(..., abilityId)`.
4. Crea `client/fx/<Rol>ClientFx` con la coreografía. Usa `FxClient.n(base)` para contar partículas, `ScreenDynamics` para la cámara
   y atenúa por distancia con `FxMath.falloff`.
5. Sonidos nuevos: función en `gen_sounds.py`, constante en `ModSounds`, entrada en `sounds.json`, subtítulos en los idiomas
   y en `tools/lang/ui_catalog.py`.

## Límites conocidos

- **Las poses del cuerpo no se han visto en partida.** Ver la sección siguiente.
- **No verificado en partida.** Compila y las matemáticas están probadas, pero el aspecto, la mezcla de sonido y el rendimiento
  deben juzgarse jugando. Los sonidos son síntesis por programa (medidos, no escuchados).
- Las activas nuevas (v67) y los demás roles siguen con el efecto genérico.

## Animaciones del cuerpo (V78, Player Animation Library)

Dependencia obligatoria aprobada por el autor: Player Animation Library 1.1.6+mc.1.21.1 (MIT, `repo.redlance.org`). Una sola capa
(`aptitudes:body`, prioridad 1500) reproduce lo que disparan los efectos de batalla, también sobre los demás jugadores. La librería
apila capas, así que no pisa a otros mods de animación.

| Habilidad | Animación | Duración |
|---|---|---|
| Furia | `berserker_rage`: se agacha, explota hacia arriba y ruge con los brazos abiertos | 1,5 s |
| Grito de guerra | `berserker_war_cry`: brazos arriba, cabeza atrás y golpe seco hacia delante | 1,1 s |
| Salto demoledor (despegue) | `berserker_leap`: se encoge y se estira con los brazos arriba; se mantiene hasta el aterrizaje | 0,9 s (se mantiene) |
| Salto demoledor (aterrizaje) | `berserker_slam`: machaca con los brazos y se incorpora (con fundido de 3 ticks) | 1,0 s |
| Torbellino | `berserker_whirl`: un giro completo con los brazos extendidos, uno por cada giro | 0,5 s |

- Los archivos los genera `tools/models/gen_player_animations.py` (poses en grados, con la convención del modelo vanilla que usa la
  librería: sin invertir signos). No se editan a mano; para otro estilo se puede sustituir por archivos de Blockbench en el mismo
  directorio sin tocar el código.
- Opción de cliente `playerAnimations` (por defecto activa) en `config/aptitudes-client.json`.
- **Para ver y ajustar las poses:** `/aptfx anim <nombre>` las reproduce sobre tu personaje (mira tu cuerpo con F5) y `/aptfx stop`
  las corta. Solo es un comando de cliente.
- **Verificado:** `PlayerAnimationResourcesTest` carga los archivos con el cargador real de PAL (nombres, duraciones y huesos).
- **No verificado:** el aspecto de las poses. El signo de los giros laterales (roll) de brazos y piernas y la dirección del giro del
  Torbellino se dedujeron del código de la librería y del modelo vanilla, pero no se han visto en el juego: si algún brazo se cruza
  en vez de abrirse, basta invertir el signo en el generador y regenerar.
- La primera persona la maneja PAL por su cuenta (sus mixins `firstPerson`); tampoco se ha comprobado el resultado.
