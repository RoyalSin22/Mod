# Roadmap de Aptitudes

Estado sincronizado con el código de `main` (la v15 está en `feature/xp-diminishing-fishing`).
Versión objetivo verificada: **Minecraft 1.21.1 · Fabric Loader 0.16.14 · Fabric API 0.116.6 · Loom 1.10 · Java 21**.

## Estado real

### Implementado (compilado en el PC del autor hasta la v15 incluida)
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
Grupos (v29) y contratos (v30): ver `docs/V28_PROPUESTA.md`.

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
