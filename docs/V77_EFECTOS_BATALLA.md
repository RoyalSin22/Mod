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

**Primera persona (V78.1).** Por defecto PAL deja las animaciones «transparentes» en primera persona (modo `NONE`), así que el jugador no
veía ninguna. `PlayerAnims.register()` activa `THIRD_PERSON_MODEL` mientras suena una animación (se dibujan los brazos y objetos del modelo
desde la cámara) y devuelve las manos vanilla en reposo. En primera persona solo se ven los brazos: los brazos deben ir hacia delante o
arriba (pitch entre −35° y −170°) para entrar en pantalla.

| Habilidad | Animación | Duración | Golpe |
|---|---|---|---|
| Furia | `berserker_rage`: se encoge, explota con los brazos abiertos (sobrepaso), congela el rugido y tiembla | 1,0 s | tick 4 |
| Grito de guerra | `berserker_war_cry`: brazos arriba cargando, golpe seco hacia delante con congelado y rebote | 0,8 s | tick 8 |
| Salto demoledor (despegue) | `berserker_leap`: se hunde y sale disparado con los brazos arriba; se mantiene hasta el aterrizaje | 0,3 s (se mantiene) | — |
| Salto demoledor (aterrizaje) | `berserker_slam`: sube más (preparación), machaca, queda clavado y se incorpora con rebote (fundido de 3 ticks) | 0,75 s | tick 3 |
| Torbellino | `berserker_whirl`: un giro completo con brazos que se lanzan hacia fuera en cada vuelta | 0,5 s | — |

- **Estructura común:** preparación → golpe con sobrepaso → congelado de ~0,08 s en el fotograma de impacto → asentamiento con rebote. El
  brazo izquierdo espeja al derecho con 0,03–0,05 s de retraso (acción solapada).
- **Sincronía:** las partículas y la cámara de la Furia, el Grito y el aterrizaje se retrasan `PlayerAnimIds.IMPACT_TICKS` ticks para caer
  en el golpe; el estruendo del aterrizaje suena en ese tick. Una prueba comprueba que haya una clave del brazo derecho en cada tick.
- Los archivos los genera `tools/models/gen_player_animations.py` (grados, convención del modelo vanilla de PAL, sin invertir signos).
  No se editan a mano. Se pueden sustituir por archivos de Blockbench en el mismo directorio sin tocar el código.
- Opción de cliente `playerAnimations` (por defecto activa) en `config/aptitudes-client.json`.
- **Para ver y ajustar las poses:** `/aptfx anim <nombre>` las reproduce sobre tu personaje y `/aptfx stop` las corta.
- Para encargar las de otro rol a Codex: `docs/PROMPT_ANIMACIONES_CODEX.md`.
- **Verificado:** `PlayerAnimationResourcesTest` carga los archivos con el cargador real de PAL (nombres, duraciones, huesos y golpes).
- **No verificado:** el aspecto de las poses en primera persona. La causa de que no se vieran era el modo `NONE`; el arreglo se ha
  comprobado en el código de la librería, pero no se ha visto en partida. El signo del roll de los brazos sigue siendo una deducción.

## Fase 2: Mago (V83)

Mismo sistema (un evento por efecto, cliente que dibuja, animación PAL, golpe sincronizado), con identidad arcana: violeta y turquesa,
destellos, runas y cristal en vez de escombros y brasas.

| Activa | Efecto | Animación (primera persona) | Golpe |
|---|---|---|---|
| Proyectil arcano (`fireball`) | La magia se concentra en la mano durante la carga; rayo helicoidal hasta el blanco; estallido con runas, destellos y **fragmentos de amatista con gravedad** | `mage_bolt`: la mano derecha va al pecho a cargar, la izquierda guía, lanzamiento con sobrepaso y recogida (0,6 s) | tick 4 |
| Salto arcano (`blink`) | Implosión en el origen, estela hasta el destino y estallido al reaparecer; la cámara estira el campo de visión | `mage_blink`: cruza los brazos hacia dentro y los abre de golpe (0,55 s) | tick 3 |
| Distorsión temporal (`time_warp`) | Dos anillos de runas que giran en sentidos contrarios, manecilla de reloj que barre el círculo y motas de luz congeladas; viñeteado turquesa que late | `mage_time_warp`: brazos arriba, golpe de palmas hacia delante y empuje (0,9 s) | tick 6 |
| Misiles arcanos (`arcane_missiles`) | Un misil por evento, con arco de Bézier y estallido; el brazo alterna | `mage_missile_r` / `mage_missile_l`: un empujón corto por misil (0,32 s) | tick 3 |

- Seis sonidos sintetizados (`mage_charge`, `mage_bolt`, `mage_blink`, `mage_time`, `mage_missile`, `mage_impact`), brillantes y con cuerpo.
  Cada uno suena en el tick de impacto de su animación (el chasquido del salto está en el segundo 0,15 del propio audio).
- **Cambio de comportamiento:** el proyectil arcano ya no dibuja línea ni suena si no hay objetivo (antes sí, aunque fallara); en ese caso
  solo aparece el aviso de que no tuvo efecto.
- Las animaciones salen de `tools/models/gen_player_animations.py`, que ahora escribe un archivo por rol (`berserker.json`, `mage.json`).
- Pieza nueva: `FxClient.handPos` (posición aproximada de la mano, para que la carga y el misil salgan de ella).
- **No verificado en partida:** el aspecto, el momento exacto de las partículas respecto a la mano y el rendimiento de los fragmentos.

## Fase 3: Asesino (V84)

Identidad de sombra: acero oscuro, humo negro, tajos y esquirlas de hierro. Mismo sistema (un evento por efecto, cliente que dibuja,
animación PAL, golpe sincronizado).

| Activa | Efecto | Animación (primera persona) | Golpe |
|---|---|---|---|
| Desaparecer (`vanish`) | El humo se recoge al cruzar los brazos y estalla en el barrido de capa; jirones que suben mientras dura; la cámara se cierra en túnel oscuro | `assassin_vanish`: brazos cruzados ante el rostro y barrido de capa | tick 3 |
| Paso de sombra (`shadow_step`) | Nube en el origen, posimágenes de humo a lo largo del recorrido, estocada con tajo y chispas al reaparecer; estirón de FOV | `assassin_step`: se enrosca hacia atrás y se lanza con una estocada | tick 3 |
| Abanico de cuchillas (`fan_of_knives`) | Destellos que se reúnen en el pecho, ocho tajos que se abren hasta el radio, una cuchilla por objetivo que vuela girando y se clava | `assassin_fan`: brazos cruzados que se lanzan abiertos | tick 4 |
| Shuriken (`shuriken`) | Brillo en la mano, estrella de cuatro puntas que gira por el aire con estela de sombra, clavada con esquirlas de hierro | `assassin_shuriken`: brazo sobre el hombro y latigazo | tick 3 |

- Cinco sonidos sintetizados (`assassin_vanish`, `assassin_slash`, `assassin_step`, `assassin_throw`, `assassin_impact`), oscuros y secos.
- **Cambio de comportamiento:** las cuatro activas dibujan y suenan solo si tienen éxito (antes el abanico y el shuriken pintaban la línea
  y sonaban aunque fallaran).
- `PlayerAnimIds.IMPACT_TICKS` pasa a `Map.ofEntries` (superaba las 10 entradas de `Map.of`).
- **Verificado:** compilación y 908 pruebas, incluida la carga de `assassin.json` con el cargador real de PAL.
- **No verificado en partida:** el aspecto, el momento exacto de las partículas respecto a la mano, el rendimiento y la mezcla de sonido.
