# Roadmap de Aptitudes

Estado sincronizado con el código de `main` (la v15 está en `feature/xp-diminishing-fishing`).
Versión objetivo verificada: **Minecraft 1.21.1 · Fabric Loader 0.16.14 · Fabric API 0.116.6 · Loom 1.10 · Java 21**.

## Estado real

### Implementado (compilado en el PC del autor hasta la v15 incluida)
- Habilidades por uso (9, con Pesca desde la v15) con hitos cada 10 niveles.
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

Decisiones: sin mixins (se compara el bloque antes y después del clic); las plantas no se registran
(un brote se convierte en el tronco de un árbol natural y los cultivos son el juego normal de Agricultura);
registro en memoria con tope de 100 000 posiciones por mundo (FIFO).
- ~~La pantalla del árbol no conoce `legendaryExclusive`~~ **Corregido (v15.1):** la sincronización de
  legendarias incluye el modo exclusivo; el botón Mejorar se desactiva y la ficha del nodo dice quién la
  tiene o que ya tienes otra. Al reservar o liberar una legendaria se avisa a todos los jugadores. El
  servidor sigue validando (`claimLegend`).
- `UniqueState` puede contener dueños antiguos de antes de la v12: si se activa `legendaryExclusive`,
  liberar con `/unique revoke <legendaria>` los que sobren.
- Clases grandes (`RolesScreen` 850 líneas, `NewRoles` 900): dividir solo cuando se toquen por otro motivo.
