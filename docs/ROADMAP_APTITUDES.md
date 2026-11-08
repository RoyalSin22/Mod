# Roadmap de Aptitudes

v55 fases 1 y 2 preparadas en `codex/v55-talentos-datapack`. v53 está integrada en `main`. La publicación sigue aplazada.
Versión objetivo verificada: **Minecraft 1.21.1 · Fabric Loader 0.16.14 · Fabric API 0.116.6 · Loom 1.10 · Java 21**.

## Estado real

### v55 — Talentos por datapack
- Fase 1. Catálogo opcional, recarga por revisión y [ejemplo del Espadachín](DATAPACK_TALENTOS.md).
- Fase 2. Vista en J, compras validadas por revisión y rango, puntos compartidos y reembolso al perder
  requisitos. La plantilla piloto da +2 % de velocidad por rango, hasta +6 % entre ambos roles activos.

### v53 — Atlas de talentos (integrado en main)
- Fase 1. Cuatro entradas en J y ficha adaptable. Sendas reúne S1–S7 y la ficha de S1 muestra su requisito en Talentos.
- Fase 2. Paletas y grabados para los 20 roles, con pestañas que distinguen selección y foco en ambas vistas.
  Pruebas visuales en partida pendientes.

### v51 — Mesa y Tablón con más espacio (integrados en main)
- Mesa con zonas separadas y Tablón con fichas para ofertas y contratos activos.
- Lienzo unificado y corrección del rótulo de sendas exclusivas. Maqueta y propuestas de asedio, datapack y roles
  en [V51_DISENO_Y_FASES.md](V51_DISENO_Y_FASES.md).

### v50 — HUD e iconos del árbol (integrados en main)
- Atlas con 570 iconos propios para los nodos del árbol, los sellos de rol y las piedras angulares
  del HUD. Los 480 PNG de Sendas siguen siendo los mismos.
- Barra con teclas y costes más legibles, dos filas auxiliares y ajuste al área visible. La vista previa
  usa la misma geometría. Pruebas en partida pendientes.
- Siguientes fases del bloque: animaciones de habilidades, árbol extensible por datapack y encuentros
  en estructuras.

### v49 — Puestos del gremio y la Hoguera (integrados en main)
- Modelos y texturas propios, llamas animadas, panel 3×3 exclusivo del gremio y cabecera de descanso.
- La Mesa se fabrica en la mesa de trabajo. Las demás recetas de Aptitudes, incluidas la Hoguera
  y el Grimorio, usan la Mesa del gremio. El grimorio muestra la estación leída de la receta.
- Pendientes las pruebas en partida de la interfaz, la fabricación y la convivencia con Reforja y
  Templado tras integrar las dos ramas.

### v47 — Eclipse y experiencia del mod (fase 1: asalto implementado)
- Oleadas configurables 4/8 por jugador, mínimos de élites y campeones, unidades con armadura completa
  visible y sin equipo como botín. Se conservan probabilidades y multiplicadores anteriores fuera de las garantías.
- Presupuesto por proximidad, con o sin grupo, y topes de zona/mundo; turnos rotativos, control del espacio
  y chunks cargados. Recuento de criaturas restaurado al arrancar y finalización idempotente sin pagos
  a jugadores muertos, creativo o espectador. El Heraldo depende de encuentros próximos, no de un grupo lejano.
- Siete opciones acotadas, reglas puras con JUnit y texto del grimorio en los once idiomas. Pruebas en partida
  pendientes. El autor pidió parar tras esta fase; diseño y límites en `docs/V47_ECLIPSE.md`.
- Fases posteriores solicitadas: Reforja/Templado, bloques y Mesa del gremio con menús propios, HUD/iconos,
  animaciones de habilidades y encuentros en estructuras. Se permite evaluar una biblioteca de animación;
  ninguna se añade aquí. El piloto del árbol por datapack se documenta en v55.

### v45 — Traducciones (T8, fases 1–3 implementadas)
- Portugués de Brasil, francés y alemán: archivos completos de 3512 claves, en el orden de `en_us`,
  con 1386 textos de interfaz/objetos, 46 del grimorio y 2080 de nodos/sendas traducidos por idioma.
  Cobertura de las 3512 claves, incluidos nodos base, legendarios, evolucionados y las 60 sendas S1–S7.
- Catálogo reproducible y rótulos de los dos generadores ampliados; no se modifican inglés ni español.
  El comprobador detecta duplicados, claves ausentes/sobrantes, orden y marcadores `%`; CI lo ejecuta.
- Los 23 capítulos incluyen valores y mecánicas de sendas, piedras angulares, logros, eventos y alquimia.
  Generador del grimorio que conserva las demás fases; párrafo de evolución traducido en su generador.
  Cabeceras y cuerpos medidos con glifos vanilla en el ancho mínimo; no cambia `GuideScreen`.
- Los generadores de evolución y sendas escriben los tres idiomas y admiten `--check`, con números,
  efectos y plantillas compartidos con sus tablas previas. Catálogo explícito para nodos base y
  generador conjunto que exige cobertura exacta. Regenerar conserva los ocho idiomas anteriores;
  el párrafo de evolución del grimorio se mantiene al día en el generador, sin duplicarlo.
- Traducción inicial pendiente de revisión con hablantes nativos y comprobación visual en el juego.
  Integración de fases 2–3 autorizada por el autor tras validación local y CI. No se publica en plataformas.

### 1.4.0 — Preparación de publicación (T7)
- Versión 1.4.0, notas para jugadores con fecha 2026-11-06, descripción pública y limitaciones actualizadas.
- T4–T6 probadas en partida según confirmación del autor el 2026-11-06; T6 integrada tras CI aprobada.
- T7 integrada en `main` (`e76b62f`) tras CI aprobada; no publicada en Modrinth/CurseForge por decisión del autor.

### v43 — Maestría y amenaza (T6, fases 1 y 2 implementadas)
- Maestría de las diez habilidades y los veinte roles: XP nueva al máximo, curva pura, +0,5 % por punto
  con tope inicial +10 %. Campo opcional `mastery` en el progreso por claves, compatible con datos previos.
- Pasivas de habilidad escaladas en servidor y predicción cliente; eficiencia energética por rol. El
  reparto deja de omitir al atacante al máximo y concede cada parte una sola vez. XP de comandos y modos
  creativo/espectador excluida. K conserva 430×262 y diez tarjetas (5×2), con números dorados y tooltip.
- Maestría: cuatro opciones validadas, ocho idiomas y pruebas de reglas/codec/configuración.
- Amenaza: anillos 0–V desde la aparición del Mundo Normal, siete opciones validadas, vida idempotente,
  daño acotado, tirada única de botín y una sola tirada natural de élite. Instantánea por criatura;
  exclusiones para jefes, invocaciones, eventos y nombres previos. HUD S2C al cambiar de anillo.
  Reglas, codec y límites probados; pruebas en partida confirmadas por el autor el 2026-11-06.

### v42 — Alquimia (T5, fases 1 y 2 implementadas)
- Cuatro pociones registradas en 1.21.1 y recetas con `FabricBrewingRecipeRegistryBuilder`:
  Maná, Frenesí, Velo del eclipse y Vigor del bosque. Pétalos nuevos extraíbles del saquito, autorizados
  por el autor; se conservan los objetos y usos existentes. Iconos de efectos generados y ocho idiomas.
- Recuperación de energía acotada, modificador de Frenesí idempotente y Velo solo contra criaturas
  marcadas por los eventos; reglas puras con pruebas. Pruebas en partida confirmadas por el autor el 2026-11-06.
- Fase 2: +50 % de velocidad y duración para Herbolista, XP decreciente persistente, siete opciones
  validadas y capítulo «Alquimia» que consulta el registro de destilado. Acceso a un campo del contador,
  sin mixins nuevos. Un único beneficiario por elaboración, exclusión de creativo/espectador y limpieza
  de soportes al descargar chunks. Pruebas en partida de ambas fases confirmadas por el autor el 2026-11-06.

### Implementado e integrado en main
- Habilidades por uso (10: Pesca desde la v15, Encantamiento desde la v21) con hitos cada 10 niveles.
- Atributos Souls (8) comprados en la Hoguera con XP vanilla; afinidad por rol; mancha de XP al morir.
- 20 roles en 5 categorías, principal + secundario, árbol de 23 nodos + legendario, 3 Sendas (2 elegibles),
  4 activas por rol (2 equipadas), mecánica propia, 2 reactivas, recurso de rol, evolución Maestro/Definitivo
  con característica (Z) y definitiva (R).
- Legendarias (antes "únicas"): nodo final del árbol, tecla H. Exclusividad por servidor **opcional**
  (`legendaryExclusive`, ver "Decisiones").
- Sets de armadura por rol, objetos de alma, accesorios (2 anillos + amuleto), élites y campeones,
  estructuras (torres, santuarios, campamentos), aldeano de gremio.
- HUD configurable, efectos visuales, árbol arrastrable con zoom.

### Pedidos del usuario implementados en el bloque v14 (compilado)
- **Grimorio de Aptitudes**: guía con 18 capítulos (Hoguera, atributos, habilidades, roles, árbol,
  evolución, armas, Elementalista, armaduras, objetos de alma, accesorios, élites, estructuras, gremio,
  recetas, botín, teclas). Se entrega una vez al entrar; receta libro + pepita de oro.
- **5 elementos nuevos** (Luz, Vacío, Veneno, Cristal, Sonido; 14 en total) con proyectil, estallido,
  pasiva y muro, y **10 fusiones** que se activan con el Estallido si dominas la pareja.
- **Armas de evolución**: 40 armas (hoja o foco según el rol) que se entregan al evolucionar a Maestro y
  a Definitivo; casillas en la fila superior del árbol con botón Reclamar validado en el servidor
  (sin duplicados: comprueba inventario y cofre de Ender).
- **Efectos de impacto**: estallido del color del rol al golpear con habilidades, disparos y armas de rol;
  fogonazo e impacto en las armas de disparo.

### v15 — Pesca y rendimiento decreciente (implementado)
- **Pesca**: novena habilidad por uso. Sin mixins: se lee la estadística vanilla `FISH_CAUGHT`.
  Pasivas: suerte y captura doble. Hitos: Paciencia (XP vanilla), Aletas (Gracia del delfín),
  Tesoro hundido (5 %), Hijo del mar (Poder del conducto), Señor de las mareas (10 %, Corazón del mar).
  Pantalla K: 5 filas de tarjetas de 40 px.
- **Rendimiento decreciente de XP** por fuente repetida (Combate, Tiro con arco, Defensa y Pesca),
  configurable y activo por defecto. Lógica pura en `XpDiminishing` con 5 pruebas.

### Propuestas (evaluadas, no implementadas)
- Activar `attributeScaling = SOFTCAP` por defecto tras probarlo en el servidor.
- Pesca: un hito que acorte el tiempo de picada requeriría un mixin en `FishingBobberEntity`; se descartó
  en v15 para no añadir mixins.

## Cambios de este bloque (auditoría)

| Prioridad | Cambio | Estado |
|---|---|---|
| 1 Seguridad | Desbordamiento de enteros en la subida de nivel de la Hoguera (`{MAX, MAX, 50}` reasignaba puntos gratis) | Corregido + prueba de regresión |
| 1 Integridad | Modificadores de esbirros: quitar antes de añadir (idempotente) | Corregido |
| 1 Integridad | Auditoría de modificadores del jugador (identificadores estables, quitar+añadir) | Verificado, sin fallos |
| 1 Sincronización | Velocidad de rotura cliente/servidor (bloques fantasma) | Verificado correcto |
| 1 Exclusiones | Conceptos de "pacto" (Brujo, Nigromante) retematizados sin cambiar mecánicas; ids internos conservados | Hecho |
| 2 Configuración | Validación general de números (negativos/NaN → defecto con aviso en el log) | Hecho |
| 2 Balance | `attributeScaling` (LINEAR por defecto, SOFTCAP, SATURATION) | Hecho, desactivado por defecto |
| 2 Balance | `maxDamageMultiplier` = 6 (remates de ×29,5 medidos) | Hecho |
| 2 Balance | Desviar proyectiles: tope 50 % | Hecho |
| 6 Rendimiento | Estado del HUD de mecánicas: como mucho una vez por tick y jugador | Hecho |
| Pruebas | 17 pruebas unitarias JUnit 4 + `testImplementation` en Gradle | Hecho, 17/17 OK localmente |

## Decisiones

- **Exclusividad de legendarias.** La política pide conservar la exclusividad por servidor si existía.
  Existía hasta la v11, pero en la v12 el usuario eligió explícitamente convertirlas en nodos de árbol
  sin límite. Se respeta esa decisión por defecto y se añade `legendaryExclusive` para recuperar la regla
  de forma autoritativa (servidor, almacén persistente `UniqueState`, una por jugador y un dueño por
  legendaria, liberación al perder el nodo). Sin liberación por inactividad ni transferencia por muerte.
- **Ids internos con "pact"** (`pact_talisman`, `demonic_pact`, `dark_pact`) se mantienen solo para no romper
  objetos y rangos guardados; todo lo visible está retematizado.
- **Fórmulas principales:** no se cambian por defecto; las alternativas quedan como opciones documentadas.

## Problemas conocidos / pendientes
- Hasta la v15 todo compila en el PC del autor; falta probar en partida (ver docs/TESTING.md).
- ~~La marca del grimorio podría perderse al morir~~ **Corregido (v15.2):** se copia en
  `ServerPlayerEvents.COPY_FROM`, así que nunca se entrega un segundo libro.
- Pruebas JUnit 5 de Codex: siguen fuera del repositorio hasta que se compartan.

### v16 — limitaciones conocidas resueltas
| Área | Antes | Ahora |
|---|---|---|
| Defensa (XP) | Daba XP con golpes bloqueados por el escudo y en ticks de invulnerabilidad | `AFTER_DAMAGE`: solo daño recibido de verdad (daño base, antes de armadura) |
| Invocaciones | Un zombi esbirro bajo el agua junto a su dueño podía acabar como ahogado hostil | Se disuelve al empezar la conversión (`isConvertingInWater`), sin mixin |
| Bombardeo orbital | Explosión real: dañaba al lanzador, jugadores, aldeanos y esbirros | Impactos que solo dañan y empujan a enemigos (7 + 1,5·rango en 3 bloques) |

### v17 — anti-granjas de colocar y romper
| Área | Antes | Ahora |
|---|---|---|
| XP de habilidades | Colocar y romper troncos, tierra o piedra daba XP sin límite | Los bloques colocados por jugadores no dan XP al romperse (`PlacementTracker` + `PlacedBlockLog`, puro y con 4 pruebas) |
| Hitos | Veta rica (Minería 30) duplicaba minerales colocados con Toque de seda | Los bloques colocados no activan extras de hitos |
| XP de Ingeniero/Druida | Se daba al hacer clic con el objeto, aunque la colocación fallara | Solo con colocación confirmada al final del tick; Ingeniero con rendimiento decreciente |

### v18 — persistencia de esperas y muros temporales
| Área | Antes | Ahora |
|---|---|---|
| Tiempos de espera | En memoria con `server.getTicks()`: se perdían al reiniciar **y al desconectarse** (salir y entrar recargaba la definitiva de 180 s) | `CooldownBook` en un Data Attachment (`aptitudes:cooldowns`, `copyOnDeath`) con tiempo de juego absoluto del mundo principal; 5 pruebas |
| Legendaria e Inmortalidad carmesí | Mapas propios en memoria | Mismo almacén persistente (claves `legendary` y `crimson_immortality`); la espera de la legendaria también se ve en el HUD tras reconectar |
| Muro elemental | Programado en memoria: si el servidor se cerraba, el muro quedaba para siempre; sus bloques (cobre, amatista, obsidiana llorosa…) se podían picar | `TemporaryBlocks` (estado persistente por mundo) + `TemporaryBlockSchedule` (puro, 4 pruebas): retirada a su hora tras reinicios, sin drops ni XP al romperlos; hojas persistentes |

Decisiones: tope de espera aceptado de 1 día real (datos corruptos o mundos con otro reloj no bloquean
para siempre), máximo 256 esperas por jugador y 4096 bloques temporales por mundo; un bloque temporal
solo se retira si en su posición sigue el mismo bloque.

### v28 — sistema de combos
Golpes cuerpo a cuerpo encadenados a criaturas (ventana 2,5 s): +3 % de daño por golpe (máx. +15 %, dentro
del tope ×6). Con 5 golpes, el siguiente es un remate según la categoría del rol principal (golpe sísmico,
ejecución, descarga en cadena, golpe sagrado que cura, sobrecarga eléctrica), con espera de 4 s. Un golpe
recibido de más de 6 de daño rompe el combo. Cada golpe carga el recurso de rol (+2; Asesino +4). Sin combos
en PvP. HUD: «×N» junto a la mira y «¡REMATE!». `ability/Combos` + `logic/ComboCounter` (puro, 4 pruebas).
Contratos (v30): ver `docs/V28_PROPUESTA.md`.

### v29 — grupos (implementación en la rama `codex/v29-grupos-v2`)

- Menú en `P`, invitaciones clicables con expiración de 60 s y comandos `/party`.
- Grupos de hasta cinco miembros persistidos en `PartyState` del Mundo Normal; el líder configura cuota de XP,
  fuego amigo y expulsiones.
- XP de combate y rol de combate compartida a miembros conectados, con bonificación de hasta 20 %; reglas puras
  de límites en `PartyRules`.
- La regla de aliados protege el fuego amigo, limita curas/aura a jugadores del grupo y alimenta el escalado de
  oleadas y Heraldos. HUD indica rol, vida y estado de conexión/dimensión.
- Build de esta rama pendiente de confirmación en el entorno (la base v41 está verificada por el autor).

### v27 — Mesa del gremio y atributos
- **Armaduras de rol solo en la Mesa del gremio**: tipo de receta `aptitudes:guild_crafting`
  (`GuildShapedRecipe`, mismo JSON que una receta con forma), cuadrícula 3×3 propia (`GuildDeskScreenHandler`
  y `GuildDeskScreen`) al hacer clic derecho en la mesa (`GuildDeskBlock`, mismo id). Las 80 recetas de
  armadura cambian de tipo; la mesa de trabajo ya no las fabrica. El grimorio indica «Mesa del gremio».
- **Atributos −30 %** por punto (ver `docs/BALANCE.md`). SOFTCAP sigue opcional.
- Corrección de documentación: con `pointsPerSkillLevel = 0` (defecto), Pesca y Encantamiento no cambiaron
  los puntos de atributo.

### v26 — colisión de las transformaciones
Mixin `PlayerEntity#getBaseDimensions` (con `require = 0`: si no existiera el método, el juego arranca igual
y se avisa una vez en el registro) + `morph/MorphSize` (servidor: forma guardada; cliente: forma recibida) +
`MorphSizeRules` (puro, 3 pruebas: ancho 0,3–1,2, alto 0,35–2,6, ojos dentro del cuerpo). Recalculo al
transformarse, destransformarse y entrar; el cliente corrige cada tick a los jugadores que aparecen después.

### v25 — texturas de las armaduras de rol
`tools/textures/gen_armor.py`: 80 iconos (16×16) y 40 capas del modelo puesto (64×32, mapa UV estándar).
Estilo según el peso del rol: pesada (placas tintadas, visera, cresta, remaches, gema en el pecho,
hombreras y cinturón con hebilla), media (cuero con escamas del color del rol) y ligera (capucha con la cara
abierta, túnica con franja dorada, fajín y puños). Semilla fija: las texturas salen iguales en cada
ejecución. Con esto, todas las texturas de objetos del mod están rehechas.

### v24 — animaciones de habilidades
Para TODAS las habilidades (antiguas y evolucionadas), centralizado en `CastFx.onCast`: gesto del brazo
visible para todos, sello rúnico animado (anillo y estrella de 6 puntas que giran y siguen al jugador) y un
remate por categoría de rol con sonido (marcial: tajo en arco; arcana: orbes que orbitan; sombra: humo y
estela; guardián: pilar de luz; oficio: chispas y engranaje). HUD: la ranura destella con el color del rol y
el icono rebota al lanzar. Árbol: al aprender un rango, el nodo late en dorado y emite una onda.

### v23 — el árbol crece al evolucionar
+5 nodos con Maestro y +5 con Definitivo en cada rol (200 en total): 120 pasivas sobre `PathEffect`
(×1,5 por rango en Maestro, ×3 en Definitivo) y 80 activas sobre 10 plantillas (`EvolvedAbilities`). Filas
bloqueadas visibles con candado hasta evolucionar; +10/+5 puntos de talento; 8 activas por rol con un
formato de equipamiento de 3 bits compatible con los mundos guardados (`LoadoutCodec`). Detalle en
`docs/V23_DISENO_ARBOL.md`.

### v22 — texturas de armas y objetos clave
86 texturas nuevas de 16×16 generadas con `tools/textures/gen_items.py`: plantillas a mano por arquetipo
(espada, mandoble, katana, daga, hacha, lanza, martillo, bastones, focos, armas de fuego, tomo, anillos,
amuletos, frascos…), rampas de 5 tonos por material con desplazamiento de matiz, luz arriba-izquierda,
contorno selectivo, brillos y halos. Armas de evolución con el color de su rol: Maestro con guarda dorada,
Definitivo con mandoble de guarda alada o foco doble con destellos. Pendiente: las 80 piezas de armadura.

### v19 — interfaz y mobs con IA Brain
| Área | Antes | Ahora |
|---|---|---|
| Pantalla de roles | Mínimo 322 px de alto: con escala 4 en 1080p (270 px útiles) los botones inferiores quedaban fuera | Desplazamiento vertical (rueda fuera del árbol, RePág/AvPág) con barra; conserva zoom y posición del árbol |
| Mobs con IA Brain (piglins, brutos, hoglins, zoglins, warden) | Ignoraban el Soborno y el camuflaje: no usan `setTarget` | `BrainTargetFilter`: cada 5 ticks borra `ATTACK_TARGET` (y `ANGRY_AT` contra esa víctima) si `shouldBlockTarget` protege al objetivo |

Decisión: sin mixin sobre el cerebro (no sabe a qué entidad pertenece y un nombre equivocado impediría
arrancar). Coste: un barrido de 32 bloques por jugador cada 5 ticks, solo mobs con memoria de objetivo.

Decisiones v17: sin mixins (se compara el bloque antes y después del clic); las plantas no se registran
(un brote se convierte en el tronco de un árbol natural y los cultivos son el juego normal de Agricultura);
registro en memoria con tope de 100 000 posiciones por mundo (FIFO).
- ~~La pantalla del árbol no conoce `legendaryExclusive`~~ **Corregido (v15.1):** la sincronización de
  legendarias incluye el modo exclusivo; el botón Mejorar se desactiva y la ficha del nodo dice quién la
  tiene o que ya tienes otra. Al reservar o liberar una legendaria se avisa a todos los jugadores. El
  servidor sigue validando (`claimLegend`).
- `UniqueState` puede contener dueños antiguos de antes de la v12: si se activa `legendaryExclusive`,
  liberar con `/unique revoke <legendaria>` los que sobren.
- Clases grandes (`RolesScreen` 850 líneas, `NewRoles` 900): dividir solo cuando se toquen por otro motivo.

### v32 — Runas para armas (implementado en rama local)
- Cuatro runas fabricables en la Mesa del gremio y engarzables al hacer clic en la mesa con un arma de rol en una mano y una runa en la otra.
- Dos ranuras persistentes por arma; una runa de cada tipo como máximo. Efectos: Fuego (ignición 3 s), Robo de vida (1 salud/4 s), Aceleración (+10 % ataque; focos −2 ticks) y Fuerza (+1 daño).
- Prueba pura: `rune/RuneRulesTest`. Build y pruebas pendientes.

### v33 — Jefes propios (implementado en rama local)
- Tres arquetipos de jefe aparecen en las estructuras existentes: Centinela de la Torre (torre), Oráculo del Santuario (santuario) y Campeón del Campamento (variantes de cacería, brujos y bandidos).
- Cada uno usa tres fases persistentes por umbrales de salud, avisos de transición y ataques de área en fase final. Reutiliza la entidad base y barra de campeón vanilla/élite; no registra entidades nuevas.
- Cada jefe suelta un núcleo distinto. En la Mesa del gremio, cada material se aplica una vez a un arma de evolución y suma +0,5 daño; máximo +1,5 con los tres.

### v34 — El Eclipse (evento del mundo)
- Eclipse solar cada 10 días (desde el día 10), de 6:00 a 18:00 en el Mundo Normal, con aviso la tarde anterior.
  Fases: Penumbra, Totalidad (oleadas cada 30 s, 50 % élites y 10 % campeones), Heraldo a mediodía y Retirada.
- Criaturas del eclipse con +50 % de vida y daño, casco oscuro irrompible (no arden de día) y tope de 12 por
  jugador. Se disuelven al terminar o si se cargan fuera del evento.
- Heraldo del Eclipse: nuevo `BossKind.ECLIPSE_HERALD` (al final del enum), uno por grupo de jugadores (64
  bloques), con vida ×1–×4 según el grupo. Suelta el Núcleo del eclipse (cuarto núcleo; el tope sigue en 3 por arma).
- Recompensas: Fragmentos del eclipse, Runa del Eclipse (`RuneKind.ECLIPSE`, al final del enum; +15 % de daño
  contra élites, campeones y jefes) y XP de rol y Combate para los supervivientes.
- Cielo rojo con mixins de cliente (`require = 0`, sin `@Shadow`): cielo, nubes, niebla y sol eclipsado;
  viñeta, ceniza y relámpagos visuales sin mixins. Opción de cliente `eclipseSkyEffects`.
- Comando `/aptitudes eclipse [status|next|start|stop]` (iniciar y detener: permiso 2).
- Lógica pura `event/EclipseRules` con `event/EclipseRulesTest` (8 pruebas). Especificación: `docs/V34_ECLIPSE.md`.
- Integrado con grupos (v29), contratos (v30) y logros (v31); «Templado en la
  Hoguera» (trofeos ganados en combate), más adelante.

### v35 — Runas+
- Tres runas nuevas al final de `RuneKind`: Escarcha (Lentitud), Trueno (descarga en cadena a monstruos) y
  Guardia (absorción). Recetas en la Mesa del gremio; texturas generadas con `gen_items.py`.
- Quitar la última runa: Mayús + clic derecho en la Mesa del gremio con el arma en la mano y la otra vacía
  (`UseBlockCallback`, porque al agacharse con objetos el juego no llama a `onUse`). Cuesta esmeraldas y la runa
  se pierde; se retira también su modificador de atributo.
- Valores de todas las runas configurables (17 campos con límites).
- Las esperas de las runas pasan a memoria por jugador: antes se escribían en el arma en cada golpe y el
  objeto se «re-equipaba» en la mano.
- Prueba pura nueva: `rune/RuneBalanceTest` (6 pruebas).

### v36 — Anti-granjas persistente
- `logic/PlacedBlocksState` (`PersistentState` por mundo, `long[]` en orden de inserción) guarda el registro de
  bloques colocados; se carga con `ServerWorldEvents.LOAD` y se olvida en `UNLOAD` (tras guardar), para no
  mezclar partidas distintas abiertas en el mismo cliente (antes el registro estático se compartía).
- `PlacedBlockLog` gana `snapshot`, `load` y `clear`; prueba nueva `logic/PlacedBlockPersistenceTest` (3 pruebas).
- Limitación que sigue: un bloque movido con pistón deja de estar marcado.

### v37 — Sonidos propios
- 7 sonidos originales generados por síntesis con `tools/audio/gen_sounds.py` (numpy + ffmpeg/libvorbis,
  mono 44,1 kHz, semilla fija: el resultado es idéntico en cada ejecución). Registro en `sound/ModSounds`,
  declaración en `assets/aptitudes/sounds.json` y subtítulos traducidos.
- Uso: aviso, inicio y totalidad del Eclipse y Heraldo (categoría Hostil), remate de combo (además del sonido
  de cada categoría) y engarzar/quitar runas (antes, sin sonido / afilador vanilla).
- Pendiente opcional: sustituirlos por grabaciones propias manteniendo los mismos nombres de archivo.

### v38 — Versión publicable
- Versión `1.3.7` (`gradle.properties`, decisión del autor); `CHANGELOG.md` con el historial; icono 128×128 generado
  (`tools/textures/gen_icon.py`); autor, icono y enlaces en `fabric.mod.json`.
- Guía de publicación: `docs/PUBLICACION.md` (incluye decisiones del autor: licencia y visibilidad del repositorio).

### v39 — Interfaz con escala grande
Revisión estática de tamaños (Minecraft garantiza al menos 320×240 de GUI; casos reales: 1080p a escala 4 →
480×270, 768p a escala 3 → 455×256, 720p a escala 3 → 426×240).

| Pantalla / HUD | Tamaño | Resultado |
|---|---|---|
| Pantalla K (`AptitudesScreen`) | 430×262 fijo | No cabía en alto con 768p/720p a escala 3 ni con poco margen a 1080p/4 → **desplazamiento vertical** con la rueda y barra dorada, como la pantalla de roles |
| Roles (`RolesScreen`) | mín. 440×322 | Ya se desplaza (v19) |
| Grimorio (`GuideScreen`) | mín. 380×236 | Índice de 22 capítulos con filas de 9 px en la ventana mínima (T3); cuerpos con ajuste y desplazamiento; en ancho < 380 se recorta un poco |
| Mesa del gremio, accesorios | 176×166 | Caben |
| Ajustes del HUD | 8 filas (≈ 176 px) | Caben en 240 |
| HUD del Eclipse | centrado abajo | Estaba a la altura de la barra de acción (h − 72) y tapaba los avisos de runas → **subido a h − 92** |
| HUD de combos | junto a la mira | Sin cambios |

Pendiente: comprobarlo en el juego (ver `docs/TESTING.md`, v39).

### v40 — Pantalla de configuración
- `client/screen/ConfigScreen`: todas las opciones (lista generada a partir de `AptitudesConfig`, con buscador
  y desplazamiento), pestaña ⚙ en la pantalla K, `/aptitudes config` y Mod Menu (opcional, `modCompileOnly`
  11.0.3, entrypoint `modmenu`).
- Autoridad del servidor: `ConfigEditPayload` (C2S) → permiso 2 → `ConfigFieldRules.parse` → `validated` →
  guardar → `ConfigSyncPayload` (S2C) a todos. Prueba pura: `config/ConfigFieldRulesTest` (4 pruebas).
- `ClientServerConfig`: la configuración del servidor en el cliente; los tooltips de las runas la usan.
- Limitaciones: las opciones se muestran con su nombre técnico (descritas en `docs/CONFIGURATION.md`); traducir
  sus nombres queda para la v45.

### v41 — Eventos rotativos
- `event/WorldEventManager`: cada fecha de evento elige uno al azar (pesos configurables, sin repetir el
  anterior, reproducible con la semilla del mundo; `event/WorldEventRules` con 5 pruebas) y da el aviso de la
  tarde anterior para cualquier evento. El Eclipse solo empieza si la rotación lo eligió; su estado guardado no cambia.
- **Niebla de almas** (noche) y **Lluvia de brasas** (día), con estado propio (`WorldEventState`).
- Cliente: colores de cielo, niebla, nubes y viñeta por evento; partículas propias; el sol eclipsado solo en el Eclipse.
  `EclipsePayload` lleva el tipo de evento.
- Comando `/aptitudes event [status|start <evento>|stop]`; `/aptitudes eclipse` informa del próximo evento.

### v44 — Tutorial guiado (T4, fases 1 y 2 implementadas)
- `tutorial/TutorialRules`: seis pasos con claves estables, siguiente objetivo, observaciones fuera de orden,
  cobro único en orden y omisión irreversible. `TutorialData.CODEC` conserva observaciones, cobros y omisión.
- Attachment `aptitudes:tutorial` inmutable, con `copyOnDeath`. `TutorialManager` solo acepta eventos del
  servidor; revisa el grimorio y los cobros pendientes cada 20 ticks, sin conceder XP ni repartir al grupo.
- Ganchos en `SoulsManager.levelUp`, `RoleManager.setRole`, activas ordinarias tras éxito, `Combos` al llegar
  a cinco y `GuildCrafting` tras retirar el resultado de una receta válida y consumir los ingredientes.
- Primera entrada: reconoce nivel de alma > 0 y rol principal previos. Inventario lleno: conserva el cobro
  pendiente. Las acciones nuevas y los pagos excluyen creativo/espectador y jugadores muertos.
- Fase 2: HUD desde datos personales S2C, interruptor `showTutorial` en archivo y Mayús + O,
  comandos propios `status|skip` sin destinatarios externos, omisión irreversible y textos en ocho idiomas.
  Vista pura probada; presentación y ciclos completos con jugadores pendientes de verificar en partida.
  Pruebas en partida de muerte, reinicio, inventario lleno y Mayús + clic pendientes.

### T3 — Grimorio actualizado a v46
- Dos capítulos añadidos al final del índice: «Sendas y piedras angulares» y «Logros»; 22 en total.
  Explican exclusividad, siete escalones, bifurcación, las diez piedras con valores de `docs/BALANCE.md`
  y los 17 logros del catálogo. «Árbol de talentos» refleja nivel 50 y hasta 66 puntos por defecto.
- «Eventos del mundo» conserva la clave `eclipse` y describe v41.1–v41.2: solo no muertos en la Niebla,
  meteoritos pequeños/grandes/enormes y criaturas del Nether, armadura y Totalidad del Eclipse.
- Textos en inglés y las siete variantes de español. Índice de 9–12 px por fila según la altura;
  títulos medidos con la fuente vanilla y cuerpo ajustado/desplazable. Revisión visual en partida pendiente.

### T2 — Pruebas de guardado de contratos y grupos
- `contract/ContractDataCodecTest`: ida y vuelta JSON con el codec real; conserva ofertas, progreso,
  reputación, contadores y marcas diarias; valida límites al cargar y evita repetir cobros tras recargar.
- `party/PartyStateNbtTest`: escritura y carga NBT de varios grupos, líderes, nombres y opciones;
  tolerancia a campos ausentes, porcentajes acotados, máximo de cinco miembros y pertenencia única.
- Error reproducido por la prueba de ida y vuelta y corregido en `PartyState`: el nombre guardado del
  líder se omitía al cargar por figurar ya como miembro; se conserva sin admitir miembros duplicados.
- No sustituye las pruebas en partida de muerte (`copyOnDeath`), reconexión y reinicio del servidor.

### v46.2 — Iconos propios de las Sendas (Codex) integrados
- T1 implementada: fase 1, 180 PNG S1–S3 desde `paths(...)`, marco de esquinas y catálogo/`--check`/hoja de 480 iconos; los 300 anteriores conservan sus píxeles. Fase 2, `RolesScreen` dibuja S1–S3 con `NodeIconRenderer.draw` sin cambiar la distribución; comprobación visual en partida pendiente.
- 300 iconos generados (`tools/textures/gen_node_icons.py`, revisados: regenerados aquí, idénticos píxel a píxel;
  tabla de plantillas igual a `KeystoneRules`) y `client/screen/NodeIconRenderer` (respaldo al icono de objeto).
- `PathsScreen` dibuja los escalones con `NodeIconRenderer.draw` (S1–S3 disponen de PNG desde T1 fase 1).

### v46.1 — Piedras angulares

- Iconos propios de Sendas, fases 1–3: generador y 300 PNG S4/S5x/S5y/S6/S7, con 10 glifos S7, diccionario de las 60 piedras angulares de §10, vista previa y `--check`; `client/screen/NodeIconRenderer` dibuja con respaldo y caché invalidada al recargar recursos; pendiente la integración en pantallas.
- `role/KeystoneRules` (puro) + `role/KeystoneRulesTest` (5 pruebas): tabla rol/senda → plantilla, Sobrecarga,
  Baluarte, esperas y valores acotados. `PathRules.Step.S7` (nivel 50, 1 rango); `PathRulesTest` ampliada.
- `RoleTree`: nodo S7 de tipo `PATH` con 1 rango (sin tipo nuevo para no romper los `switch` sobre
  `RoleNode.Kind`), al final de la lista; `keystone(id)` y exclusividad/requisito del S7.
- `ability/Keystones`: ganchos en muertes (Carnicería, Detonación, Contagio, Fortuna), daño recibido (Baluarte),
  daño de esbirros (Legión), `AbilityManager.execute` (Sobrecarga), `PathEffects` (Espectro y Tempestad),
  `Minions.spawn` (Legión) y un tick cada 10 (Santuario y HUD). Estados en memoria, como las runas.
- `KeystonePayload` + `client/hud/KeystoneHud`; iconos por plantilla en `NodeIcons.keystone`.
- 10 sonidos (`tools/audio/gen_sounds.py`, ahora acepta nombres para generar solo los nuevos), 19 campos
  `keystone*` y textos (`tools/lang/gen_path_lang.py`).
- Ajuste de diseño: Tempestad salta también en el 15 % de los golpes y Espectro da +5 % de esquiva, para que
  funcionen en las sendas sin crítico o sin esquiva. Cambiaformas B: «Metamorfosis Afortunada» (antes repetía el
  nombre de un nodo del Domador).

### v46.0 — Nivel 50 y Sendas exclusivas
- `role/PathRules` (puro) + `role/PathRulesTest` (7 pruebas): escalones, exclusividad, bifurcación (S6 acepta
  cualquiera de las dos ramas) y migración.
- `RoleTree`: escalones S4, S5x, S5y y S6 de las 60 sendas (`deeper(...)`), **añadidos al final** de la lista de
  nodos de cada rol (hay código que usa el índice: la mecánica es el 18); sin posición en la cuadrícula.
  `PathSpec` lleva el escalón y su multiplicador (S6 ×1,5, aplicado en `PathEffects`).
- `RoleManager.upgrade`: requisito con `RoleTree.prerequisiteMet`, `PATH_LOCKED` (otra senda) y
  `BRANCH_LOCKED` (otra rama). `ensureConsistent`: reembolso automático de las sendas sobrantes con aviso.
- Configuración: `maxRoleLevel` 50 y `configRevision` (`config/ConfigMigration` + `ConfigMigrationTest`): los
  archivos anteriores con 30 pasan a 50.
- Cliente: `screen/PathsScreen` (vista de sendas, 426×232, desplazable) y botón «Sendas» en `RolesScreen`.
- Textos: `tools/lang/gen_path_lang.py` (240 nodos, 60 frases de senda, interfaz; lee los efectos de
  `RoleTree.java`).

### v41.2 — Eclipse y Lluvia de brasas más fuertes
- `event/StrongEventRules` (puro) + `event/StrongEventRulesTest` (5 pruebas): criaturas del Eclipse por fase,
  bonificación de la Totalidad, armadura por fase, tamaño de meteorito, potencia, caída, botín, restos y
  criaturas del Nether.
- `EclipseManager`: armadura (modificador `aptitudes:eclipse/armor` y piezas visibles sin soltar),
  resistencia al empuje en la Totalidad (`aptitudes:eclipse/knockback`), esqueletos wither.
- `WorldEventManager`: meteoritos con tamaño, magma temporal con `TemporaryBlocks` (nunca sustituye bloques),
  piglins y hoglins inmunes a la zombificación, tope por zona.
- 6 campos de configuración nuevos (`eclipseTotalityBonus`, `eclipseMobArmor`, `eclipseArmorChance`,
  `emberRainLargeChance`, `emberRainHugeChance`, `emberRainMobChance`).

### v41.1 — Corrección del HUD y Niebla de no muertos
- `HudConfigScreen`: distribución calculada por filas (título, escala entre − y +, 6 interruptores, ayuda y
  Restablecer/Hecho); antes las posiciones eran fijas desde el centro y se solapaban.
- Niebla de almas: `event/SoulFogRules` (puro) elige la criatura por clima del bioma (agua, cálido, frío,
  templado); solo zombis, esqueletos y sus variantes (sin fantasmas). `event/SoulFogRulesTest` (5 pruebas).

### v31 — Logros
- Pestaña de avances «Aptitudes» (18 avances: la raíz y 17 logros) en `data/aptitudes/advancement/aptitudes/`,
  generados por `tools/advancements/gen_advancements.py`. Criterio `minecraft:impossible` («done») que concede el
  servidor con `achievement/Achievements.grant`; la raíz usa `minecraft:tick` para que la pestaña aparezca al entrar.
- Catálogo puro `achievement/Achievement` (ids, padres, marcos, iconos y umbrales) y
  `achievement/AchievementCatalogTest` (3 pruebas: árbol, JSON y traducciones en los 8 idiomas).
- Ganchos: `SoulsManager.rest/levelUp`, `RoleManager.setRole/evolve`, `Combos` (remate), `RuneSocketing.socket`,
  `BossManager.onDeath` (rematador y grupo conectado), fin con éxito de `WorldEventManager`/`EclipseManager`,
  `PartyManager.accept` (los dos jugadores), `ContractManager.claim` y `ProgressManager` (nivel 50).
- Retroactivo: al entrar se revisan rol, evolución, nivel de alma, habilidades y contratos (`Achievements.checkState`).
- Los ids de los logros forman parte del progreso guardado: no se renombran ni se borran.

### v30 — Contratos del gremio (Codex, revisado y corregido)
- Bloque `aptitudes:contract_board`, pantalla (420×230), payloads `ContractActionPayload` (C2S) y
  `ContractViewPayload` (S2C), attachment `aptitudes:contracts` con `copyOnDeath`.
- `ContractRules` (puro): ofertas deterministas por jugador y día (la primera, de la dificultad máxima), tipos por
  dificultad (Jefe solo Legendario; Evento desde Difícil), objetivos alcanzables, rangos de reputación.
  `contract/ContractRulesTest` (7 pruebas).
- Correcciones de la revisión: un contrato aceptado guarda la oferta completa (no cambia al subir la reputación
  ni caduca al cambiar de día); cada oferta solo se acepta una vez al día (antes se podía aceptar y cobrar sin
  límite); las entregas no quitan objetos si no llegas a la cantidad; objetivos de Evento, Jefe y Forja
  alcanzables; la Caza solo cuenta criaturas objetivo; el Oficio no cuenta XP de comandos; textos traducibles y
  nombres de objetivo en la pantalla.
