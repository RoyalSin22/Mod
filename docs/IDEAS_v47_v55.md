# Ideas y hoja de ruta (v59 en adelante)

> **Nota (2026-11-08):** mientras se escribía este documento, Codex integró en `main` las versiones **v47–v58**
> (Asalto y Persecución del Eclipse, Reforja y Templado, Mesa y Hoguera nuevas, HUD e iconos del árbol, atlas de
> talentos, remates 3D, **talentos por datapack**, avisos de los jefes y animaciones del Cambiaformas). Por eso la
> numeración de §10 empieza en **v59**, y algunas ideas ya están hechas en parte (marcadas «✔ v5x» abajo). El
> nombre del archivo (`IDEAS_v47_v55.md`) se mantiene para no romper enlaces.

Estado: **aprobado con cambios** por el autor (2026-11-08). Nada implementado todavía. Lo que sigue pendiente de
decisión va marcado con ❓.

**Decisiones del autor (2026-11-08):**
- Criterio de §0 (inspiración, nunca copia): **aprobado**.
- §2: **descartados** el viaje entre Hogueras (S1) y los Ciclos/Nuevo Juego+ (S8); el resto, aprobado.
- §3 (jefes inspirados) y §4–§8: **aprobados**.
- **GeckoLib aprobado** como dependencia (§6, A3). playerAnimator: no por ahora.
- Estamina y rodar (S4): **todavía no** (más adelante).
- Orden de §10: **aprobado** (ajustado a los descartes).
- Nueva idea del autor: **activar el Eclipse desde la Hoguera** con un objeto raro (§2b), reformulada para
  cumplir §0 y las exclusiones; detalles ❓ pendientes.

## 0. Límites (no negociables)

- **Inspiración sí, copia no.** Se toman mecánicas, ritmo y atmósfera de los juegos de acción oscura (Souls), del
  manga de fantasía oscura (Berserk), de los RPG de acción vistosos (Kingdom Hearts) y de las aventuras de
  mazmorras (Zelda). **Nunca** sus nombres, personajes, objetos icónicos, símbolos, música, diálogos ni arte
  (p. ej., nada de espadas, llaves, marcas o criaturas reconocibles). Todo nombre y diseño es original, como el
  Eclipse (v34). Motivo: derechos de autor y poder publicar el mod.
- Exclusiones de `AGENTS.md` §3: sin linajes, razas, pactos, herencias ni sacrificios (el «Templado en la
  Hoguera» sigue siendo con trofeos, no con sacrificio).
- Sin dependencias nuevas salvo aprobación expresa ❓ (ver §6).

---

## 1. Lo que falta cerrar antes de crecer (prioridad máxima)

| # | Tarea | Quién |
|---|---|---|
| P1 | Compilar `main` (`1d2f27b`) y **probar en partida v42–v46** (Alquimia, tutorial, Maestría, Amenaza, sendas, piedras angulares, idiomas) con `docs/TESTING.md` | Autor |
| P2 | Revisión a fondo de T5 (Alquimia) y T6 (Maestría y Amenaza) | Claude |
| P3 | Publicar la **1.4.0** (Modrinth/CurseForge) | Autor |
| P4 | Activar GitHub Actions (`.github/workflows/build.yml` ya existe) para compilar cada push | Autor ❓ |
| P5 | Borrar ~40 ramas remotas ya integradas | Autor (o Claude con permiso) |
| P6 | Revisión nativa de pt_br, fr_fr y de_de | Comunidad |

---

## 2. Mecánicas de estilo Souls (originales)

| # | Mecánica | Qué es | Encaje con lo existente | Riesgo |
|---|---|---|---|---|
| ~~S1~~ | ~~Viaje entre Hogueras~~ | **Descartado por el autor** | — | — |
| S2 | **Mejora de frascos** | Fragmentos de alma + Núcleos suben cargas y curación de los frascos de Estus/Éter (con tope) | Frascos, jefes | Bajo |
| S3 | **Parada y contraataque** | Bloquear con escudo en una ventana corta (≈0,25 s) desvía el golpe, aturde y abre un «golpe de gracia» con daño extra | Combos, `AFTER_DAMAGE` | Medio (balance) |
| S4 | **Rodar con estamina** ❓ | Tecla de rodar con invulnerabilidad breve; barra de estamina que también gastan sprint y golpes cargados | Barras Souls del HUD | **Alto**: cambia todo el combate; configurable y desactivable |
| S5 | **Mensajes en el suelo** | Notas cortas de frases predefinidas (sin texto libre: sin moderación) que otros jugadores leen y valoran | Multijugador | Bajo |
| S6 | **Ecos de muerte** | Una silueta reproduce los últimos segundos antes de la muerte de otro jugador | Mancha de XP | Medio (red) |
| S7 | **Arenas de jefe con muro de niebla** | Al entrar en la sala de un jefe, un muro temporal cierra la arena; barra de jefe grande con nombre; tema musical propio | Jefes v33, `TemporaryBlocks` | Bajo |
| ~~S8~~ | ~~Ciclos (Nuevo Juego+)~~ | **Descartado por el autor** | — | — |

## 2b. Convocar el Eclipse en la Hoguera (idea del autor, reformulada) ❓

Idea original: «sacrificar» en la Hoguera un objeto parecido al behelit para iniciar el Eclipse. **No se puede tal
cual**: el behelit es un objeto icónico de Berserk (§0) y el sacrificio/pacto está en las exclusiones (§0,
`AGENTS.md` §3). Se conserva la **mecánica** con un objeto y una ficción originales:

- **Objeto**: «Astrolabio del Ocaso» (nombre provisional): un disco de bronce ennegrecido con un sol eclipsado
  grabado. Nada de huevos, rostros ni ojos.
- **Uso**: clic derecho en una Hoguera encendida → el astrolabio **se rompe** y, tras un aviso de 60 s (campana del
  Eclipse), empieza un Eclipse forzado. Es una **convocación**, no una ofrenda: no se entrega nada a nadie.
- **Obtención** (elige ❓): botín raro del Heraldo y de los jefes de torre/santuario/campamento; cofre de las
  estructuras grandes; y/o trueque caro con el aldeano del gremio (nivel maestro, pocas unidades).

**Dinámicas que hay que decidir antes de programar ❓**

| # | Pregunta | Riesgo si no se decide | Propuesta |
|---|---|---|---|
| E1 | En multijugador, un jugador desencadena un evento que afecta a **todo el servidor** | Grifeo: alguien lo activa cuando otros no quieren | Votación: los jugadores conectados del Mundo Normal tienen 60 s para aceptar; empieza si acepta la mayoría (en un jugador, inmediato). Configurable: `eclipseSummonMode` = `vote` / `anyone` / `ops` |
| E2 | ¿Cada cuánto se puede convocar? | Eclipses seguidos = granja de Núcleos y Runas | Espera global de 3 días de juego entre eventos (naturales o convocados) |
| E3 | ¿Afecta al calendario de 10 días? | Dos Eclipses casi seguidos | El convocado **sustituye** al siguiente evento natural (el calendario se reinicia) |
| E4 | ¿Mismas recompensas? | Farmeo si el objeto es fácil de conseguir | Mismas recompensas; el objeto es raro (≈5 % jefes, 15 % Heraldo, 1 trueque por aldeano cada 7 días) |
| E5 | ¿Cualquier hora? | El Eclipse está pensado de día (6:00–18:00) | Empieza en el siguiente amanecer; de día, en 60 s con reloj propio (como `/aptitudes eclipse start`) |
| E6 | ¿Dimensiones y estados? | Activarlo desde el Nether o durante otro evento | Solo Mundo Normal, sin otro evento en curso y no en Pacífico |
| E7 | ¿Logro? | — | Logro nuevo «Quien llama a la noche» (convocar un Eclipse y sobrevivirlo) |
---

## 3. Jefes y criaturas inspirados (diseños originales)

Cada jefe: 2–3 fases con etiquetas (como v33), ataques telegrafiados, arena con muro (S7), núcleo propio y logro.

| Inspiración (solo tono) | Jefe / criatura original | Idea de combate | Dónde |
|---|---|---|---|
| Fantasía oscura medieval | **El Coloso Sin Rostro** | Guerrero gigante con espadón; en la fase 2 su armadura se agrieta y gana velocidad pero recibe más daño | Ruinas de batalla (estructura nueva) |
| Fantasía oscura medieval | **Jauría de la Noche Roja** | Manada de bestias sombrías que solo aparecen en el Eclipse y huyen al amanecer | Eclipse v34 |
| Fantasía oscura medieval | **La Doncella de Hierro Negro** | Duelista rápida con paradas: castiga atacar sin pausa (enseña S3) | Torre del Eclipse |
| RPG de acción vistoso | **Enjambre de Umbrales** | Criaturas pequeñas que surgen de charcos de sombra, se hunden y reaparecen; drop: «Fragmento de umbral» | Niebla de almas / anillos IV–V |
| RPG de acción vistoso | **El Guardián Desmontado** | Armadura cuyas piezas flotan y atacan por separado; hay que romperlas en orden | Santuario profundo |
| RPG de acción vistoso | **Tejedor de Ecos** | Mago que crea copias; solo la verdadera proyecta sombra | Santuario profundo |
| Aventura de mazmorras | **El Autómata Antiguo** | Constructo con rayo cargado; se le aturde devolviéndole proyectiles | Templo mecánico |
| Aventura de mazmorras | **La Serpiente del Lago Hundido** | Jefe acuático con fases dentro y fuera del agua | Templo del agua |

**Templos (mazmorras de puzle)**: estructuras grandes por anillo de Amenaza con palancas, placas, puertas, una
**llave de jefe** propia del templo y un cofre final con un **Vaso de vida** (+1 corazón permanente, tope 5,
guardado por jugador). Las herramientas de los roles (gancho del Ingeniero, salto del Lancero) abren atajos.

---

## 4. Grimorio

| # | Mejora |
|---|---|
| G1 | **Bestiario**: una página por criatura y jefe que se desbloquea al vencerla (ataques, debilidades, botín) |
| G2 | **Buscador** y marcadores; enlaces desde el tutorial y desde los tooltips («ver en el grimorio») |
| G3 | **Progreso**: porcentaje de logros, sendas y jefes completados |
| G4 | Ilustraciones de cabecera por capítulo (arte generado por programa, estilo del mod) |
| G5 | Páginas de cada senda con su árbol mini (iconos de v46.2) y su piedra angular |

## 5. HUD e interfaz

| # | Mejora |
|---|---|
| H1 | **Tema visual unificado** (marcos, fuentes de números, colores) en todos los HUD y pantallas — ✔ en parte v49–v53 (Mesa, Tablón, HUD y atlas); falta el resto |
| H2 | **Barra de jefe estilo Souls** abajo, con nombre y fase; una sola barra para varios jefes |
| H3 | **Modo minimalista** (el modo daltónico se descartó por decisión del autor, 2026-11-08) |
| H4 | Presets de disposición del HUD (clásico, centrado, compacto) en el editor actual (Mayús + O) |
| H5 | Animaciones de interfaz: aparición de avisos, llenado de barras, latido al subir de nivel |

## 6. Gráficos, animaciones 3D y objetos

| # | Opción | Qué permite | Coste | Dependencia |
|---|---|---|---|---|
| A1 | **Modelos 3D de armas en mano** (JSON de Blockbench; en el inventario sigue el icono 2D) | Espadas, lanzas y bastones con volumen | Medio | **Ninguna** |
| A2 | **Modelos de criaturas en Java** exportados de Blockbench (animación por código) | Jefes y criaturas propias sin librerías | Alto | Ninguna |
| A3 | **GeckoLib** (librería de animación de modelos) ❓ | Jefes y criaturas con animaciones complejas hechas en Blockbench | Medio | Sí (obligatoria para jugar) |
| A4 | **Animaciones del jugador** con playerAnimator ❓ | Gestos al lanzar habilidades, golpes de combo, rodar | Medio | Sí |
| A5 | **Mejores partículas y estelas** (formas propias, estelas de arma en los remates) — ✔ en parte v54 (remates 3D) | Más impacto visual sin dependencias | Bajo | Ninguna |
| A6 | **Rediseño de texturas de objetos**: más detalle, rareza por color de nombre, tooltips con estadísticas e icono de rol | Objetos más legibles y valiosos | Medio | Ninguna |

Recomendación: empezar por **A1 + A5 + A6** (sin dependencias). Si se quiere el salto a jefes animados de verdad,
aprobar **A3 (GeckoLib)**, que es el estándar en Fabric; A4 solo si las animaciones del jugador son prioridad.

## 7. Sinergias

| # | Sinergia |
|---|---|
| Y1 | **Senda ↔ activas**: la senda elegida modifica una activa del rol (más radio, otro efecto de estado, carga extra) |
| Y2 | **Resonancia de grupo**: +15 % si dos miembros comparten plantilla de piedra angular |
| Y3 | **Elementos ↔ runas**: una runa del mismo elemento que el activo del Elementalista potencia la fusión |
| Y4 | **Conjuntos de armadura de rol ↔ sendas**: bonificación extra si la armadura coincide con la senda |
| Y5 | **Combos ↔ piedras angulares**: el remate de combo dispara la piedra angular (con espera) |

## 8. Rendimiento (revisión del código)

| # | Hallazgo | Arreglo |
|---|---|---|
| R1 | `PathEffects.all(player)` recorre **todas** las especificaciones de senda (420, cada una con `activeRank`) en cada golpe y cada segundo por jugador | Caché por jugador invalidada al cambiar rangos o roles |
| R2 | `Keystones.active(player)` se calcula en cada golpe, esquiva, muerte y daño | Caché por jugador (misma invalidación) |
| R3 | `RoleTree.keystone(id)` recorre los 20 roles por llamada | Mapa id → piedra angular construido una vez |
| R4 | Revisar con **spark** (perfilador) en un servidor con 5–10 jugadores y eventos activos | Medir antes de optimizar más |

## 9. Addon y datapack (contenido por datos)

| Fase | Qué se vuelve configurable por **datapack** (`data/<ns>/aptitudes/...`) | Valor |
|---|---|---|
| D0 | ✔ v55: **talentos adicionales por datapack** (rama de ejemplo para el Espadachín) | Base para D1–D3 |
| D1 | **Valores de balance**: efectos de senda, piedras angulares, runas, eventos | Los servidores ajustan sin recompilar |
| D2 | **Sendas**: efectos de S1–S6, plantilla de S7, nombres | Paquetes de sendas alternativas |
| D3 | **Roles y árbol completo** (nodos, requisitos, posiciones) | Addons que añaden roles |
| D4 | **API Java** (entrypoint `aptitudes`) para que otros mods registren habilidades y plantillas | Ecosistema de addons |

Requisitos técnicos: los datos se sincronizan al cliente al entrar; los ids siguen guardándose por clave
(compatibles con mundos existentes); validación con mensajes claros en el registro si un datapack es incorrecto.
Las **animaciones e iconos** ya se pueden cambiar con un **resource pack** (texturas, sonidos y, con A1, modelos).

---

## 10. Propuesta de orden (aprobada; renumerada desde v59)

| Versión | Contenido | Quién |
|---|---|---|
| v59 | Rendimiento R1–R3 + resto de HUD (H2 barra de jefe, H3 minimalista, H4 presets) | Claude (R) / Codex (H) |
| v60 | Souls «suaves»: S2 mejora de frascos, S7 arenas con niebla, §2b convocar el Eclipse (cuando se decida) | Codex / Claude (§2b) |
| v61 | Grimorio G1–G5 (bestiario y buscador) | Claude ✔ (2026-11-09) |
| v62 | Objetos y gráficos A1, A6 (sin dependencias) | Claude ✔ (2026-11-09) |
| v63 | Primer bloque de jefes originales (Coloso Sin Rostro, Guardián Desmontado, Autómata Antiguo) + estructuras, con **GeckoLib** | Claude (diseño) + Codex |
| v64 | Templos de puzle y Vasos de vida | Codex |
| v65 | Sinergias Y1–Y5 | Claude |
| v66 | Datapack D1–D2 (sobre la base de talentos de v55) | Claude |
| v67 | Combate avanzado S3 (parada), mensajes en el suelo (S5) y ecos de muerte (S6); S4 (rodar con estamina) si se confirma | Claude |

## 10b. Decisiones del 2026-11-08 (segunda ronda)

- §2b **aprobado** con el objeto original (Astrolabio del Ocaso): E1 lo autorizan **los operadores o el líder del
  grupo** (party: quien lo creó; confirmado por el autor), E2 espera de **3 días de juego**,
  E4 obtención como se propuso, E5 hora como se propuso, E6 **solo Mundo Normal**, E7 logro «Quien llama a la
  noche». E3 (calendario): el convocado sustituye al siguiente evento natural (propuesta por defecto).
- **Nueva**: Lluvia de brasas **local** al encender un portal al Nether nuevo (P1–P6 aprobadas): solo portales en
  una posición nueva, radio 64 alrededor del portal, 5 minutos, espera global de 3 días, solo Mundo Normal, no
  altera el calendario, mismas recompensas por meteorito y sin logro de superviviente.
- Planes de trabajo: `docs/PLAN_CLAUDE.md` (Claude) y `docs/PLAN_CODEX_2.md` (Codex).

## 11. GeckoLib (aprobado): implicaciones

- Pasa a ser **dependencia obligatoria** para jugar (cliente y servidor), como Fabric API. Versión fijada para
  1.21.1 (rama 4.x de Fabric); se declara en `fabric.mod.json` (`depends`) y en `build.gradle`
  (`modImplementation`). Licencia MIT.
- Excepción a «sin dependencias» que hay que anotar en `AGENTS.md` §1 y `docs/CONTEXTO_CODEX.md`, igual que Mod Menu.
- Se usa solo para criaturas y jefes nuevos (v51+). Las criaturas existentes no cambian.
- Los modelos y animaciones se hacen en Blockbench (plugin de GeckoLib): arte original.

## 12. Decisiones cerradas (2026-11-08)

1. Estamina y rodar (S4): **todavía no**; se retomará más adelante.
2. Convocar el Eclipse (§2b): objeto original aprobado; E1 = operadores o líder del grupo (quien lo creó); E2–E7
   como en §10b.
