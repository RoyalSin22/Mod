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

- **Sin animación del cuerpo del jugador en tercera persona.** Requiere una biblioteca de animación o un mixin al modelo; no se
  añadió ninguna dependencia nueva. La sensación de movimiento se logra con cámara, FOV y partículas.
- **No verificado en partida.** Compila y las matemáticas están probadas, pero el aspecto, la mezcla de sonido y el rendimiento
  deben juzgarse jugando. Los sonidos son síntesis por programa (medidos, no escuchados).
- Las activas nuevas (v67) y los demás roles siguen con el efecto genérico.
