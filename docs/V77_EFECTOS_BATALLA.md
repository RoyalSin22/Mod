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

## Fase 4: Herbolista (V85)

Identidad de bosque: verdes y dorado, hojas, gotas y esporas. Cubre las tres activas con efecto visible; Espinas venenosas conserva el efecto genérico.

| Activa | Efecto | Animación (primera persona) | Golpe |
|---|---|---|---|
| Bálsamo (`balm`) | Motas que suben a las manos al recoger; onda de hojas por el suelo hasta el radio y surtidor de luz; cada aliado recibe un remolino de hojas y corazones | `herbalist_balm`: recoge a la cintura y abre las palmas al frente | tick 4 |
| Lluvia sanadora (`healing_rain`) | Nube sobre el centro, gotas, motas de curación y un círculo luminoso que gira durante los 5 s; viñeteado verde suave | `herbalist_rain`: alza los brazos al cielo, los sostiene y los baja despacio | tick 5 |
| Nube de esporas (`spore_cloud`) | Vaina con arco desde la mano; estallido y niebla verde-violeta del tamaño del radio durante 6 s | `herbalist_spore`: tiro por debajo del hombro | tick 3 |

- Tres sonidos sintetizados (`herbalist_balm`, `herbalist_rain`, `herbalist_spore`).
- Las partículas y el sonido genéricos que las tres activas dibujaban en el servidor se sustituyen por el evento único.
- **Verificado:** compilación y pruebas, incluida la carga de `herbalist.json` con el cargador real de PAL. **No verificado en partida:** aspecto, momento y rendimiento.

## Fase 5: Ingeniero (V86)

Identidad de taller: naranja y cian, chispas, cable y esquirlas de hierro. Cubre Garfio y Granada de pulso; Propulsores y Torreta conservan el efecto genérico.

| Activa | Efecto | Animación (primera persona) | Golpe |
|---|---|---|---|
| Garfio (`grapple`) | Chispas en la muñeca, cable de chispas hasta el anclaje y estallido de metal; el que lo lanza nota tirón de FOV y de cámara | `engineer_grapple`: encoge el brazo y lo dispara al frente y arriba | tick 3 |
| Granada de pulso (`pulse_grenade`) | Vuelo en arco con humo y chispas, luz que parpadea cada vez más rápido y estallido eléctrico a los 20 ticks (cuando se aplica el daño) | `engineer_grenade`: tiro de revés por encima del hombro | tick 3 |

- Tres sonidos sintetizados (`engineer_grapple`, `engineer_throw`, `engineer_blast`). El estallido suena en el servidor al aplicar el daño.
- Las partículas y sonidos genéricos que ambas activas dibujaban en el servidor se sustituyen por el evento único.
- **No verificado en partida:** aspecto, sincronía del parpadeo con la explosión y rendimiento.

## Fase 6: Lancero, Espadachín y Paladín (V87)

| Rol | Activa | Efecto | Animación | Golpe |
|---|---|---|---|---|
| Lancero | Salto del dragón (`dragon_leap`) | Ráfaga de aire y escombros al despegar, estela de aliento de dragón, aterrizaje con onda violeta y escombros con física; el evento del aterrizaje se adelanta 2 ticks para que su golpe caiga con el daño | `lancer_leap` y `lancer_slam` | tick 3 y 2 |
| Lancero | Estocada perforante (`piercing_thrust`) | Línea recta de viento con chispas y tajos hasta el final; solo si acierta | `lancer_thrust` | tick 3 |
| Espadachín | Iaido (`iaido`) | Estela blanca de acero con tajos cada dos bloques y destello al final | `swordsman_iaido` | tick 3 |
| Espadachín | Mil cortes (`thousand_cuts`) | Un evento por tajo: arco, chispas y destello; el quinto sacude la cámara; el tono sube con cada golpe | `swordsman_cuts` | tick 2 |
| Paladín | Consagración (`consecration`) | Círculo dorado que se abre, ocho pilares de luz y un pulso por segundo mientras dura | `paladin_consecrate` | tick 5 |
| Paladín | Martillo de la justicia (`hammer_of_justice`) | Rayo dorado, columna de luz, anillo y chispas sagradas; solo si acierta | `paladin_hammer` | tick 4 |

- Siete sonidos sintetizados (`lancer_leap`, `lancer_thrust`, `lancer_slam`, `swordsman_draw`, `swordsman_cut`, `paladin_consecrate`, `paladin_hammer`).
- Embestida (`lunge`) y las pasivas conservan su efecto genérico.
- **No verificado en partida:** aspecto, sincronía del aterrizaje con el daño y del iaido con el desplazamiento, y rendimiento.

## Fase 7: Nigromante, Druida, Mercader táctico y Monje (V88)

| Rol | Activa | Efecto | Animación | Golpe |
|---|---|---|---|---|
| Nigromante | Lanza de hueso (`bone_spear`) | Alma y polvo óseo se forman en la mano; línea de polvo con astillas de hueso y estallido de almas; solo si acierta | `necromancer_spear` | tick 4 |
| Nigromante | Sifón de almas (`soul_siphon`) | Pulso que se abre y vuelve hacia dentro; un hilo de alma por enemigo hasta el nigromante; solo si hay enemigos | `necromancer_siphon` | tick 4 |
| Druida | Manada (`summon_wolves`) | Ola de hojas y motas de luz; destello y anillo de hojas bajo cada lobo | `druid_wolves` | tick 5 |
| Druida | Tormenta de espinas (`thorn_storm`) | Zarzas en tres espirales hasta el radio, composta y temblor; solo si hay enemigos | `druid_thorns` | tick 4 |
| Mercader | Lanzamiento de moneda (`coin_toss`) | Estela dorada-esmeralda, destello y monedas de oro que saltan con gravedad | `merchant_coin` | tick 3 |
| Mercader | Guardia dorada (`golden_guard`) | Domo de motas doradas que se cierra, monedas que giran y viñeteado dorado | `merchant_guard` | tick 4 |
| Monje | Patada voladora (`flying_kick`) | Estela de ki en el recorrido; explosión y anillo de choque si golpea | `monk_kick` | tick 3 |
| Monje | Cien puños (`hundred_fists`) | Un evento por golpe: destello y anillo de choque cada vez más cerrado; el octavo sacude la cámara | `monk_fists` | tick 2 |

- Ocho sonidos sintetizados (`necromancer_spear`, `necromancer_siphon`, `druid_howl`, `druid_thorns`, `merchant_coin`, `merchant_guard`, `monk_kick`, `monk_fist`).
- Las partículas y sonidos que esas activas dibujaban en el servidor se sustituyen por los eventos nuevos.
- **No verificado en partida:** aspecto, sincronía y rendimiento (la Manada con tres lobos y los Cien puños son lo más cargado).

## Fase 8: Mago pistolero, Invocador de armas y Cambiaformas (V89)

| Rol | Activa | Efecto | Animación | Golpe |
|---|---|---|---|---|
| Mago pistolero | Disparo arcano (`arcane_shot`) | Fogonazo, rayo cian-blanco con destellos e impacto con chispas; solo si acierta | `gunslinger_shot` | tick 2 |
| Mago pistolero | Disparo en abanico (`fan_the_hammer`) | Un evento por disparo, más agudo y corto; la animación solo con el primero | `gunslinger_shot` | — |
| Mago pistolero | Rebote (`ricochet`) | Un rayo dorado por eslabón de la cadena, de objetivo en objetivo | `gunslinger_shot` | — |
| Mago pistolero | Ojo de halcón (`deadeye`) | Mira de cuatro marcas que se cierra sobre cada objetivo, túnel dorado y campo de visión cerrado; los disparos certeros sacuden la cámara | `gunslinger_deadeye` | tick 4 |
| Invocador | Andanada y Lluvia de espadas | Espadas espectrales con estela azul, tajo y esquirlas; en la lluvia caen desde 6 bloques | `weapon_summon` | tick 4 |
| Invocador | Espadón espectral (`spectral_greatsword`) | Arco enorme de luz y tajos a lo largo del recorrido; solo si acierta | `weapon_greatsword` | tick 4 |
| Invocador | Prisión de espadas (`blade_prison`) | Cinco filos en círculo que se cierran durante 30 ticks | `weapon_summon` | tick 4 |
| Cambiaformas | Abalanzarse (`pounce`) | Estela violeta, tres zarpazos paralelos y chispas | `shapeshifter_pounce` | tick 3 |
| Cambiaformas | Aullido de la bestia (`beast_howl`) | Tres anillos sónicos escalonados, onda y temblor | `shapeshifter_howl` | tick 5 |

- Seis sonidos sintetizados (`gunslinger_shot`, `gunslinger_mark`, `weapon_greatsword`, `weapon_prison`, `shapeshifter_pounce`, `shapeshifter_howl`); las espadas sueltas reutilizan `swordsman_cut`.
- Convención de `power` en los eventos de disparo y espada: bits 0-1 = variante, bit 4 (16) = inicia la animación (así solo anima el primero de una ráfaga).
- `NewRoles` expone ahora `muzzle` y `orbit`, y `lineDamage` admite `particle == null` (sin trazo).
- **No verificado en partida:** aspecto, sincronía de ráfagas y rendimiento (la Lluvia de espadas y el Espadón son lo más cargado).

## Fase 9: Cazador, Bardo, Brujo abisal y Domador (V90)

| Rol | Activa | Efecto | Animación | Golpe |
|---|---|---|---|---|
| Cazador | Marca (`hunters_mark`) | Sello verde que se cierra sobre la presa con cruz de luz | `hunter_mark` | tick 3 |
| Cazador | Trampa de oso (`bear_trap`) | Disco de dientes con polvo al colocarla; al cerrarse, chispas, astillas y explosión | `hunter_trap` | tick 3 |
| Cazador | Lluvia de flechas (`arrow_volley`) | Un evento por flecha que cae en diagonal desde 8 bloques; la primera anima | `hunter_volley` | tick 4 |
| Cazador | Golpe de halcón (`falcon_strike`) | Un picado por golpe con estela de nubes, plumas y zarpazo | `hunter_falcon` | tick 4 |
| Bardo | Valor, Escudo y Réquiem | Espirales de notas y anillo de luz: dorado, azul o violeta | `bard_song` | tick 4 |
| Bardo | Disonancia (`dissonance`) | Onda de notas rotas con chispas rojas y estampido; solo si hay enemigos | `bard_dissonance` | tick 3 |
| Brujo | Maldición de agonía (`curse_of_agony`) | Hilo carmesí ondulante hasta la víctima y un latido por cada golpe del veneno | `warlock_curse` | tick 3 |
| Brujo | Rayo abismal (`abyssal_bolt`) | Línea violeta con llamas de alma y estallido; solo si acierta | `warlock_bolt` | tick 3 |
| Brujo | Diablillos (`summon_imps`) | Anillos de azufre y llamas que se abren desde el suelo | `warlock_imps` | tick 5 |
| Brujo | Perdición (`doom`) | Sello de almas durante 5 s y, a los 5 s, columna de almas con estampido | `warlock_doom` | tick 4 |
| Domador | Azuzar, Vínculo vital y Rugido del alfa | Anillo de polvo rojo, verde o dorado y una aureola en cada mascota; el rugido añade estampido | `tamer_command` | tick 4 |
| Domador | Estampida (`stampede`) | Franja de tres carriles con nubes y escombros físicos; solo si acierta | `tamer_stampede` | tick 3 |

- Once sonidos sintetizados (`hunter_shot`, `hunter_trap`, `hunter_falcon`, `bard_song`, `bard_dissonance`, `warlock_curse`, `warlock_bolt`, `warlock_imps`, `warlock_doom`, `tamer_command`, `tamer_stampede`).
- Se corrige además `PlayerAnimIds.ALL`, al que faltaban tres animaciones de V89 (`gunslinger_deadeye`, `weapon_summon`, `weapon_greatsword`).
- **Queda sin cubrir** el Elementalista (efectos por elemento).
- **No verificado en partida:** aspecto, sincronía y rendimiento (la Lluvia de flechas y las espirales del Bardo son lo más cargado).

## Fase 11: Firmas y definitivas de los roles piloto (V96, 1.ª tanda)

`GrandFx` (servidor) + `GrandClientFx` (cliente); ids 67 (firma) y 68 (definitiva), `power` = ordinal del rol, `dx` = variante (elemento activo del Elementalista).
Animaciones `grand_signature` (tick 5) y `grand_ultimate` (tick 8) en `grand.json`; sonidos `grand_berserker|elementalist|shapeshifter|paladin|engineer` y `grand_rumble` (solo definitivas).

| Rol | Motivo visual | Sonido |
|---|---|---|
| Bersérker | Lava y llamas que suben del suelo | Rugido grave con tambor |
| Elementalista | Lluvia de destellos con el color del elemento | Acorde de campanas |
| Cambiaformas | Esporas y mutación sobre el cuerpo | Burbujeo y gruñido |
| Paladín | Halo y cera dorada ascendente | Campana mayor y coro |
| Ingeniero | Chispas eléctricas dispersas | Servo, engranaje y descarga |

## Fase 12: Posturas de combate (V97, piloto)

| Rol (arma) | Pose |
|---|---|
| Bersérker (hacha) | Agachado y adelantado, hacha baja, mano izquierda en guardia |
| Lancero (lanza) | De lado, lanza nivelada al frente, mano de atrás en el asta |
| Monje (vendas) | Puños arriba, guardia alta y cuerpo bajo |
| Mago (báculo) | Báculo erguido junto al cuerpo, mano libre abierta y cuerpo ligeramente flotante |

Precedencia: muerte y reacción (vanilla) > habilidad (capa 1500) > postura (capa 1100) > locomoción. Ver `StanceRules`.

### V96, 5.ª tanda: activas de los nodos extra

`role/ExtraFxShapes` asigna a cada activa `<rol>_n3|n4|n6|n8|n10` una de las diez formas de la 4.ª tanda; N8 y N10 usan la intensidad del Definitivo.
Orden de la tabla (N3, N4, N6, N8, N10), por rol: Bersérker rayo/vórtice/cono/aura/nova; Mago rayo/escudo/zona/aura/nova; Asesino nova/embestida/rayo/aura/aura;
Herbolista zona/curación/vórtice/curación/zona; Ingeniero zona/invocación/escudo/aura/invocación; Lancero rayo/zona/cono/rayo/zona; Espadachín cono/nova/embestida/aura/rayo;
Paladín rayo/cono/escudo/aura/zona; Nigromante rayo/aura/zona/aura/vórtice; Druida cono/curación/nova/aura/zona; Mercader rayo/zona/invocación/aura/nova;
Monje nova/cono/rayo/aura/vórtice; Elementalista rayo/zona/invocación/aura/vórtice; Cambiaformas embestida/aura/cono/aura/embestida; Mago pistolero rayo/zona/rayo/aura/rayo;
Invocador de armas rayo/escudo/nova/invocación/zona; Cazador zona/rayo/cono/aura/rayo; Bardo rayo/aura/nova/aura/nova; Brujo rayo/vórtice/zona/aura/vórtice; Domador invocación/aura/escudo/aura/invocación.

### V96, 4.ª tanda: activas de Maestro y Definitivo

Id 70 (`FxIds.EVOLVED`), `power` = ordinal de `EvolvedTree.Template`, `dx` = nivel (0 Maestro, 1 Definitivo), `dy` = rango. Una animación común `evolved_cast` (tick 3) y un sonido por plantilla (`ModSounds.EVOLVED`).

| Plantilla | Capa del cliente |
|---|---|
| Nova | Destello y dos anillos que se expanden; sacudida |
| Rayo | Línea de partículas de 24 bloques desde la mano |
| Cono | Abanico de partículas de 70° |
| Embestida | Estela de nube y FOV hacia delante |
| Zona | Anillo en el punto señalado y lluvia de motas |
| Vórtice | Espiral que se cierra |
| Aura | Anillo grande, motas que suben y viñeteado |
| Curación | Anillo de aldeano feliz y brillos |
| Escudo | Anillo de cera, destello y FOV hacia atrás |
| Invocación | Llama de almas y almas que suben |

### V96, 3.ª tanda: firmas y definitivas de los otros 15 roles

Mismo evento (ids 67–68) y mismos gestos; solo cambian el motivo de partículas y el sonido (`ModSounds.GRAND_BY_ROLE[ordinal del rol]`).

| Rol | Sonido (timbre) | Partícula |
|---|---|---|
| Mago | Campana y barrido | Varilla del End que cae |
| Asesino | Soplo y golpe grave | Humo a ras de suelo |
| Herbolista | Campanillas | Aldeano feliz que sube |
| Lancero | Golpe seco | Nube radial |
| Espadachín | Filo (shing) | Barrido |
| Nigromante | Bordón grave | Llama de almas |
| Druida | Madera y hojas | Aldeano feliz que sube |
| Mercader táctico | Monedas | Brillo |
| Monje | Gong | Cera que sube |
| Mago pistolero | Disparo | Crítico radial |
| Invocador de armas | Espadas en cascada | Barrido que cae |
| Cazador | Grito de halcón | Cohete que cae |
| Bardo | Laúd | Notas |
| Brujo abisal | Bordón oscuro | Humo |
| Domador | Cuerno | Nube radial |

### V96, 2.ª tanda: piedras angulares

Id 69 (`FxIds.KEYSTONE`), `power` = ordinal de `KeystoneRules.Template`. Se dibuja por plantilla, no por rol, y el color es el del rol principal.

| Plantilla | Guion |
|---|---|
| Matanza | Motas rojas que suben al pecho y anillo de esporas |
| Baluarte | Doble anillo dorado y destello; cámara y viñeteado locales |
| Sobrecarga | Espiral de chispas |
| Detonación | Destello, anillo de llamas al radio real y lava; sacudida leve |
| Contagio | Línea de motas e tinta entre víctima y receptor |
| Legión | Sin disparo (pasiva) |
| Espectro | Runas y nube donde esquivó |
| Fortuna | Destellos dorados que saltan |
| Tempestad | Rayo quebrado hasta el segundo objetivo |
| Santuario | Pulso de luz al radio real y motas verdes |

## Fase 10: Elementalista (V91)

Los efectos se enganchan en `ElementEffects.bolt` y `burst`, no en cada activa, así que el Proyectil, el Estallido, la Ráfaga (3 proyectiles),
el Aura (4 estallidos), la Firma y la Definitiva comparten guion. El servidor manda el ordinal del elemento y el color sale de `Element.color()`.

| Elemento | Estela del proyectil | Impacto |
|---|---|---|
| Fuego / Magma | Llama y humo / lava y llama | Explosión |
| Hielo | Copos de nieve | Esquirlas de hielo con gravedad |
| Rayo | Zigzag con chispas; los saltos de la cadena son eventos aparte | Destello y chispas |
| Tierra | Escombros del suelo con física | Escombros que saltan |
| Viento | Espiral de nubes | Tajo de aire |
| Agua | Salpicaduras y gotas | Burbujas |
| Planta | Hojas y brillos verdes | Compost |
| Sombra / Vacío | Tinta y humo / portal inverso | Humo denso / estampido |
| Luz / Cristal | Varillas de luz / brillo y amatista | Destello / esquirlas de amatista |
| Veneno / Sonido | Baba y estornudos / notas | Gotas de baba / estampido |

- Ocho sonidos por familia (`element_fire`, `ice`, `lightning`, `earth`, `wind`, `water`, `magic`, `dark`), con tono distinto para los pares.
- **Cambio de comportamiento:** el Proyectil elemental ya no dibuja línea ni suena si no hay objetivo; las partículas genéricas del Estallido y del
  impacto se sustituyen por el evento. La evolución de Fuego a Magma conserva su anillo de lava (`ElementEffects.evolutionFx`).
- **No verificado en partida:** aspecto de los 14 estilos, tonos de los sonidos y rendimiento (el Estallido con la Definitiva lanza hasta tres a la vez).
