# v67 — Árbol de habilidades unificado y 10 habilidades nuevas por rol (diseño para aprobar)

Estado:

- **Diseño aprobado por el autor (2026-11-13).**
- **F1 implementada:** árbol unificado.
- **F7 implementada (2026-11-14):** Bardo, Brujo abisal y Domador; ver `docs/V67_F7_DISENO.md`.
- **F6 implementada (2026-11-13):** Cambiaformas, Mago pistolero, Invocador de armas y Cazador; ver `docs/V67_F6_DISENO.md`.
- **F5 implementada (2026-11-13):** Druida, Mercader táctico, Monje y Chamán; ver `docs/V67_F5_DISENO.md`.
- **F4 implementada (2026-11-13):** Lancero, Espadachín, Paladín y Nigromante; ver `docs/V67_F4_DISENO.md`.
- **F3 implementada (2026-11-13):** Mago, Asesino, Herbolista e Ingeniero; ver `docs/V67_F3_DISENO.md`.
- **F2 implementada (2026-11-13):** las 10 habilidades del Bersérker; ver §8. Iconos encargados en
  `docs/PROMPT_ARTE_ICONOS_BERSERKER.md` (hasta entonces, objetos vanilla de respaldo).
- **Puntos decididos:** opción b (Maestro 15 y Definitivo 10): **76** puntos a nivel 50 con las dos evoluciones (antes
  66).

## 1. Decisiones del autor (2026-11-13)

| Decisión | Valor |
|---|---|
| Estructura | **Un solo árbol** unificado: Talentos, Sendas, Maestro y Definitivo en un lienzo |
| Contenido nuevo | **+10 nodos por rol** (200 en total) |
| Tipo | **Habilidades únicas** programadas a medida para cada rol |
| Ubicación | **Repartidos** por el árbol |
| Puntos | **Más puntos** de árbol |

Restricciones que se mantienen:

- Nada de linajes, razas ni pactos (`AGENTS.md` §3).
- Los ids nuevos se añaden **al final** de la lista de cada rol y nunca se renombran ni se reordenan.
- Toda acción se valida en el servidor.

## 2. Árbol unificado

Un único lienzo arrastrable con zoom (como la vista actual de Talentos), con el kit «Berserker»:

- panel de piedra;
- marcos de nodo por categoría y estado;
- conectores del kit;
- cabeceras de sección;
- ficha de detalle a la derecha.

Las pestañas Talentos, Sendas, Maestro y Definitivo pasan a ser **accesos directos** que desplazan el lienzo a su
sección. La tecla C centra la vista.

Secciones, de arriba abajo; los nodos nuevos llevan las marcas N1–N10:

| # | Sección | Nivel | Contenido |
|---|---|---|---|
| 1 | Núcleo | 1 | Insignia del rol |
| 2 | Talentos | 1–24 | Carriles Pasivas y Activas, entrenamiento (vida, daño, presteza, eficiencia, maestría) y legendario. **N1–N4** como ramas nuevas de cada carril |
| 3 | Sendas | 10–50 | Las tres sendas en columnas S1–S7 con bifurcación (la geometría actual, ya probada) |
| 4 | Veterano *(nueva)* | 34–38 | **N5–N7**: puente entre las sendas y la evolución |
| 5 | Maestro | Evolución Maestro | Firma, arma y m1–m5 |
| 6 | Ascenso *(nueva)* | 42–48 | **N8–N10**: puente entre Maestro y Definitivo (exige Maestro) |
| 7 | Definitivo | Evolución Definitiva | Definitiva, arma y u1–u5 |

Cada sección lleva una cabecera del kit con su nombre y el nivel o la evolución que exige. Si está bloqueada, la
cabecera se atenúa y muestra el requisito. Un minimapa lateral, una columna de 7 marcas, permite saltar a cada sección.

**Geometría:** una clase pura nueva (`logic/UnifiedTreeLayout`) con pruebas JUnit, que compruebe que:

- ningún nodo se solapa;
- cada sección queda debajo de la anterior;
- los conectores unen centros;
- se puede desplazar entero a cualquier escala.

## 3. Puntos de árbol

> **Corrección (2026-11-13):** la primera versión de esta sección usaba una fórmula equivocada (1 + nivel / 3). La
> real es `RoleRules.earnedPoints`: **1 + nivel × 3 / `rolePointsEveryLevels`**. Con el valor por defecto (3) se gana un
> punto por nivel. El cambio de puntos queda **pendiente de que el autor lo confirme** con estas cifras.

| | Ahora (`rolePointsEveryLevels` = 3) | Propuesta inicial (= 2) |
|---|---|---|
| Puntos a nivel 50 | 51 | 76 |
| Más Maestro (10) y Definitivo (5) | **66** | **91** (+25) |
| Rangos que suman los 10 nodos nuevos | — | 22 |

Se cambia el valor por defecto de `rolePointsEveryLevels` de 3 a 2. Hay una migración en `config/ConfigMigration`, con
el mismo patrón que `maxRoleLevel`: solo cambia los archivos que conservan el 3 por defecto.

Un jugador con el árbol ya repartido recibe los puntos nuevos al entrar, porque se recalculan por nivel. No pierde
nada.

## 4. Rol piloto: Berserker (para validar el estilo y el poder)

Ya tiene:

- **Activas:** Furia, Grito de guerra, Salto aplastante, Torbellino.
- **Pasivas:** Piel dura, Sed de sangre, Frenesí, Inquebrantable, Carnicería.
- **Evolución:** Tajo sísmico, Embestida brutal, Terremoto, Grito del titán.

Las nuevas buscan **mecánicas que el rol no tiene**: proyectil con retorno, arrastre, sangrado acumulable,
transformación con contrapartida, acumulación al recibir daño y ecos de habilidades.

| Id | Nombre | Sección | Tipo | Nivel | Rangos | Coste | Efecto |
|---|---|---|---|---|---|---|---|
| `berserker_n1` | Cicatrices | Talentos (Pasivas) | Pasiva | 6 | 3 | — | Al recibir un golpe de más del 15 % de tu vida máxima, ganas 1 cicatriz durante 20 s (máx. 3/4/5): −3 % de daño recibido por cicatriz |
| `berserker_n2` | Sangre hirviente | Talentos (Pasivas, tras Sed de sangre) | Pasiva | 16 | 3 | — | Bajo el 50 % de vida, tus golpes prenden fuego al enemigo 2/3/4 s |
| `berserker_n3` | Hacha arrojadiza | Talentos (Activas) | Activa | 9 | 3 | 25 maná, 14 s | Lanzas el hacha, que atraviesa enemigos (5/6,5/8 de daño y sangrado de 1/s durante 4 s) y vuelve a tu mano |
| `berserker_n4` | Cadena de sangre | Talentos (Activas, tras Grito de guerra) | Activa | 20 | 3 | 30 maná, 18 s | Enganchas al enemigo señalado (hasta 12 bloques) y lo arrastras hasta ti; tu siguiente golpe antes de 3 s hace +50/75/100 % de daño |
| `berserker_n5` | Rugido intimidante | Veterano | Pasiva | 34 | 2 | — | Quien te golpee a menos de 4 bloques recibe Debilidad I/II durante 3 s (una vez cada 5 s por enemigo) |
| `berserker_n6` | Desgarro | Veterano | Activa | 36 | 2 | 45 maná, 30 s | Tres tajos rápidos en cono corto; cada uno añade una carga de sangrado (máx. 3; 1,5/2 de daño por segundo y carga durante 5 s) |
| `berserker_n7` | Juramento de sangre | Veterano | Pasiva | 38 | 1 | — | Al activar Furia sacrificas el 10 % de tu vida actual: Furia dura un 50 % más y da +15 % de robo de vida |
| `berserker_n8` | Avatar de la guerra | Ascenso | Activa | 42 | 2 | 60 maná, 45 s | Durante 10/14 s: +20 % de tamaño, +30 % de daño, inmunidad al empuje y a la Lentitud. Al terminar, Debilidad I durante 5 s |
| `berserker_n9` | Eco de batalla | Ascenso | Pasiva | 45 | 2 | — | Tras usar Torbellino o Tajo sísmico, tu siguiente golpe antes de 3 s lanza una onda frontal de 4 bloques (4/6 de daño) |
| `berserker_n10` | Ragnarök | Ascenso | Activa | 48 | 1 | 80 maná, 90 s | Saltas al punto señalado (hasta 16 bloques); al caer abres una fisura en línea de 10 bloques: 14 de daño, lanza a los enemigos y deja suelo ardiente 4 s |

**Referencia de poder:** las activas nuevas rinden por debajo de Terremoto (10 + 1,5 por rango) y de la definitiva. Las
pasivas cuestan puntos que compiten con las sendas. Todas las cifras serán configurables con el mismo patrón que los
campos `keystone*`.

## 5. Coste técnico por habilidad única

Cada habilidad necesita:

1. **Datos:** el nodo, su requisito y el nivel en `RoleTree`, al final de la lista.
2. **Lógica:** en `ability/`. Las reglas numéricas van en una clase pura con pruebas JUnit, y los efectos de
   lanzamiento e impacto se apoyan en `CastFx`, sin duplicarlos.
3. **Configuración:** valores con límites en `validated(...)` y su fila en `docs/CONFIGURATION.md`.
4. **Textos:** nombre y descripción en es/en, copia a las variantes `es_*` y pt/fr/de con el catálogo.
5. **Icono:** 200 iconos nuevos. Se encargan al generador de arte con el mismo formato que los de senda; el prompt
   se prepara aparte.

## 6. Plan por fases

| Fase | Contenido | Entregable |
|---|---|---|
| F1 | Árbol unificado con los nodos actuales y las secciones Veterano y Ascenso preparadas; puntos 1 + nivel / 2 con migración | Pantalla nueva y pruebas de geometría |
| F2 | Las 10 habilidades del Berserker (piloto) y el prompt de iconos | Berserker jugable con su árbol completo |
| F3–F6 | Hechas (Mago, Asesino, Herbolista, Ingeniero; Lancero, Espadachín, Paladín, Nigromante; Druida, Mercader, Monje, Chamán; Cambiaformas, Pistolero, Invocador de armas, Cazador) | — |
| F7 | Hecha (Bardo, Brujo abisal y Domador) | — |
| F8 | Balance tras prueba en partida y revisión de textos en los 5 idiomas | Ajustes |

## 7. Qué necesito que apruebes

1. La estructura del árbol unificado (sección 2), con las secciones nuevas **Veterano** y **Ascenso**.
2. La fórmula de puntos 1 + nivel / 2 (36 puntos a nivel 50).
3. Las 10 habilidades del Berserker como **plantilla de estilo y poder** para los demás roles: nombres, mecánicas y
   cifras.
4. El plan por fases: empezar por F1 mientras se valida el diseño del piloto.

## 8. F2 implementada: Bersérker

Datos en `role/RoleExtras` (al final de la lista del rol; sus activas, en los índices 8–12), cifras en
`ability/BerserkerRules` (puro, JUnit) y comportamiento en `ability/BerserkerKit`.

**Cambios respecto a la tabla de §4**, para que ninguna línea de Talentos cruce otro nodo (las líneas bajan siempre del
padre al hijo):

| Nodo | Diseño | Implementado |
|---|---|---|
| N1 Cicatrices | Nivel 6, carril de pasivas | Nivel 8, tras Sed de sangre (casilla 2,3) |
| N2 Sangre hirviente | Tras Sed de sangre | Tras Cicatrices (casilla 2,4) |
| N3 Hacha arrojadiza | Carril de activas | Tras Presteza (casilla 6,3) |
| N4 Cadena de sangre | Tras Grito de guerra | Tras Hacha arrojadiza (casilla 6,5) |
| N6 Desgarro | Solo sangrado | Nombre «Tajos desgarradores» (el Cambiaformas ya tiene «Desgarro») y 2,5 / 3 de daño por tajo además del sangrado |

Veterano (N5–N7) y Ascenso (N8–N10) son filas centradas en su sección (`UnifiedTreeLayout.bridgeNode`), sin
requisitos entre ellas; Ascenso exige Maestro (`requiredTier`), así que al perder la evolución se devuelven los
puntos como en las filas de Maestro.

**Equipamiento:** con 13 activas el índice ya no cabía en 3 bits. `LoadoutCodec` escribe ahora 4 bits por ranura con
una marca nueva y sigue leyendo los dos formatos anteriores. Una versión anterior del mod leería mal un equipamiento
guardado con esta (no se puede volver atrás sin reequipar).

**Estado en memoria:** cicatrices, ventanas de Cadena y Eco, Juramento, sangrados y fisuras se pierden al reiniciar el
servidor; los atributos de Avatar son temporales y no se guardan con el jugador.

**Balance:** `extraSkillDamageScale` (1 por defecto) multiplica el daño de todas estas habilidades.

