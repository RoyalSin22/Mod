# Encargo: animaciones del cuerpo del jugador por rol (para Codex)

Contexto: el mod usa **Player Animation Library (PAL) 1.1.6+mc.1.21.1** (dependencia obligatoria, V78) para animar el cuerpo del jugador al
lanzar habilidades. El Bersérker ya tiene sus cinco animaciones (referencia: `tools/models/gen_player_animations.py`). Este encargo es hacer
lo mismo para otro rol, **sin tocar nada fuera de lo indicado**. Lee antes `AGENTS.md`, `docs/V77_EFECTOS_BATALLA.md` y este archivo.

## Lo más importante: el jugador juega en PRIMERA PERSONA
Ahí solo se ven **los brazos y lo que sostienen**. El cuerpo, las piernas y la cabeza no se ven; solo se notan por cómo mueven los hombros.
- Un brazo que cuelga (pitch 0) queda **fuera de pantalla**. Para que se vea, el brazo debe ir hacia delante o arriba: pitch entre −35° y −170°.
- Toda la expresividad tiene que estar en los brazos (amplitud grande, brazos separados del cuerpo con roll) y en el torso (inclinación).
- `PlayerAnims.register()` ya activa el modo de primera persona de PAL (`THIRD_PERSON_MODEL`) mientras suena una animación: no lo cambies.

## Cómo se escriben
- Se generan con `python3 tools/models/gen_player_animations.py src/main/resources/assets/aptitudes` (admite `--check`; el CI lo ejecuta).
  **No edites a mano el JSON.** Añade las animaciones del rol nuevo al generador (puede ser un archivo nuevo por rol si prefieres
  `player_animations/<rol>.json`; en ese caso amplía el generador, la prueba y `PlayerAnimIds`).
- Convención (leída del código de PAL, sin invertir signos): ángulos en **grados** en el JSON; pitch NEGATIVO levanta un brazo hacia delante;
  roll positivo del brazo derecho lo separa del cuerpo (el izquierdo, negativo); posición Y positiva sube (píxeles de modelo, 1/16 de bloque).
  Huesos: `head`, `torso`, `body` (raíz), `right_arm`, `left_arm`, `right_leg`, `left_leg`. Curvas: `easeoutquad`, `easeoutback`,
  `easeinquint`, `easeinoutsine`, `easeoutelastic`, `easeoutbounce`, `linear`… (la curva de una clave es la del tramo que LLEGA a ella).
- Los ids de animación van en `logic/PlayerAnimIds` y se disparan desde la coreografía del rol (`client/fx/<Rol>ClientFx`), como en
  `BerserkerClientFx`. Si la animación tiene un golpe, declara su tick en `PlayerAnimIds.IMPACT_TICKS` y en `IMPACT_TICKS` del generador
  (la prueba comprueba que haya una clave del brazo derecho en ese instante) y retrasa los efectos de cámara y partículas ese número de ticks.

## Calidad que se pide (en este orden)
1. **Preparación (anticipación)**: antes del golpe, el cuerpo va un instante al lado contrario (0,1–0,2 s). Que se lea qué va a pasar.
2. **Impacto visual**: golpe rápido con `easeinquint` o `easeoutback` (sobrepaso) y **congelado de ~0,08 s** en el fotograma de impacto
   (dos claves iguales, la segunda con `linear`).
3. **Fluidez orgánica**: nada se mueve a la vez. Brazo izquierdo con retraso de 0,03–0,05 s respecto al derecho (`lagged_mirror`), asimetría
   leve, torso y cabeza con desfase. Sin poses idénticas a ambos lados.
4. **Presencia y espacio**: amplitudes grandes; que ocupe la pantalla. Mejor exagerar que quedarse corto.
5. **Viva y cinematográfica**: un pequeño temblor o respiración durante las fases sostenidas, y el asentamiento final con rebote
   (`easeoutback`), no un corte seco. La cámara (`ScreenDynamics`) y las partículas caen en el impacto.
6. **Rápida**: ninguna animación de activa dura más de ~1 s; el jugador pidió que no se vean lentas.

## Cómo comprobarlo
- `./gradlew build` (incluye `PlayerAnimationResourcesTest`, que carga los archivos con el cargador real de PAL) y
  `python3 tools/models/gen_player_animations.py src/main/resources/assets/aptitudes --check`.
- En el juego, `/aptfx anim <nombre>` reproduce la animación sobre tu personaje sin lanzar la habilidad (mira tus brazos en primera persona
  y con F5 el cuerpo entero). **El aspecto solo se juzga jugando**: di claramente qué no pudiste ver.

## Reglas del proyecto que aplican
- Respuestas y documentación en español. Un cambio de versión por rama. Commits convencionales con solo el trailer de sesión.
- Nada de dependencias nuevas, ni mixins nuevos, ni tocar `build.gradle`/CI sin permiso del autor.
- Actualiza `CHANGELOG.md` y `docs/V77_EFECTOS_BATALLA.md`. Si algo no lo pudiste probar, dilo.
