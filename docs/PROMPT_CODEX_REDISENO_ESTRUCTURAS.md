# Encargo para Codex — Rediseño de la Torre y del Templo (y retirada del Sello, la Llave y la Piedra sellada)

Lee antes `AGENTS.md` (reglas del proyecto, política de Git, exclusiones) y este documento completo. Responde al autor en **español**.
Aquí se decide **qué se pide**; el estado real está en el código, y los datos de abajo se comprobaron contra `main` (`50f10cf`).

## 0. Reparto del trabajo (para no pisarse)

| Quién | Qué | Rama |
|---|---|---|
| **Claude (primero)** | **Retirar del mod** la *Piedra sellada*, el *Sello del templo* y la *Llave del templo* (§2) y dejar el flujo del templo en «jefe y cofre directos». Es lógica y registros: no lo toques tú. | `claude/retirar-sellos` (una versión por rama) |
| **Codex (después)** | **Rediseñar la forma** de las Tres Torres Señoriales y del Templo del Custodio (§3–§5), sobre el código que deje Claude. | `codex/rediseno-estructuras` |

Empieza solo cuando la rama de Claude esté integrada en `main`. Si no lo está, avisa al autor: las dos ramas tocan
`CustodianTempleLayout`, `ThreeTowersFeature` y `TowersLayout`, y mezclarlas da conflictos.

## 1. Lo que NO gusta y lo que se quiere (el autor lo completa antes de lanzar el encargo)

> **Pedido del autor para la torre (2026-11-26):** la estructura deja de ser «tres torres en un patio» y pasa a ser una **fortaleza medieval-gótica
> en ruinas** (por las batallas que ha sufrido) de la que **destacan tres torres**.

- **Escala:** la **distancia entre las torres es mucho mayor** (ya no caben en 31×31; ver §4.1, que se acuerda con el autor).
- **Torres:** la **Torre principal, la más alta**, alberga la batalla con el **jefe** (Coloso Sin Rostro). Las **otras dos** son **torres de vigilancia** con un **subjefe** cada una (Guardián Desmontado).
- **Interior variado:** cuartos distintos repartidos por toda la fortaleza (biblioteca, baño, dormitorios, comedor, etc.), **plataformas**, puentes, patios,
  **combates con enemigos** por el camino, y decoración **medieval y gótica** (arcos apuntados, vidrieras, contrafuertes, gárgolas, candelabros, estandartes rotos).
- **Ruinas:** muros derrumbados, tejados hundidos, escombros y vegetación; los daños deben **verse** y a la vez dejar un recorrido jugable.
- **Botín:** **gran botín** al vencer al jefe de la torre principal (más que el actual: hoy un cofre por jugador con Plantilla de ascensión).
- **Pendiente del autor:** qué no te gusta del **templo** y qué quieres en él (silueta, tamaño, interior, recorrido); y referencias visuales. Si no lo escribe, Codex no lo inventa.
- **Decisiones ya tomadas** (tabla en `docs/V88_BRAINSTORM_FORTALEZA.md` §9): escala **muy grande (~128×128, tope 160×160)**; imprescindibles: dormitorios, barbacana, sala de estudio, sala de botín, cofres ocultos repartidos, monasterio o lugar de rezo y ala de pociones y encantamientos; mini-jefe opcional en el monasterio (élite o campeón, sin poder nuevo); botín principal tras el Coloso y cofres repartidos por toda la estructura. **Método de generación elegido: A, plano puro por columna** (`FortressLayout` evaluado por chunk; origen por región; sin `Structure` propia). Con esta escala, mide el coste de generación y propón el reparto de chunks, el relleno del cimiento y el tope de desnivel antes de programar.
- Lluvia de ideas de partida (Claude): `docs/V88_BRAINSTORM_FORTALEZA.md`.

## 2. Lo que Claude retira antes (contrato: Codex lo da por hecho)

Tras la rama de Claude, **ya no existen**: el bloque `sealed_stone` (Piedra sellada), el bloque `temple_seal` con su entidad de bloque
(Sello del templo), el objeto `temple_key` (Llave del templo) y todo lo que dependa de ellos (texturas, modelos, textos, pruebas).
Se pueden romper los mundos de prueba: no hay compatibilidad con esos ids.

Cómo queda el flujo del templo: **sin puzle y sin llave; el guardián/Custodio custodia el Relicario y, al vencerlo, se abre
directamente** (el Vaso de vida, las llaves de cámara de metal, la cripta y el Ancla del Velo se conservan tal como estén; lo que
cambia lo concreta Claude en su rama y lo documenta en `docs/V87_RETIRADA_SELLOS.md`). Las puertas entre fases ya no usan
Piedra sellada: **qué usan en su lugar es parte de tu propuesta de §6** (p. ej. una reja o un portón que el controlador abre; si
hace falta un bloque nuevo, la textura va con `tools/textures/gen_temple_blocks.py` y su nombre y estilo deben encajar con la
dirección de arte oscura del mod).

## 3. Estado actual de las dos estructuras (datos comprobados)

### 3.1 Las Tres Torres Señoriales (`dungeon/TowersLayout`, `world/ThreeTowersFeature`, `dungeon/TowersControllerBlockEntity`)

- Dungeon **de superficie**, dentro del radio 15 del `Feature` (31×31 de huella). Región de generación: 24×24 chunks (más frecuente que el Templo).
- Dos torres vasallas (Oeste y Este, 11×11, tres plantas de combate y sala superior con un **Guardián Desmontado**) y la **Torre del Señor**
  (13×13, cuatro plantas y sala alta con el **Coloso Sin Rostro**). Patio central, escaleras en zigzag por los muros norte y sur,
  empuñaduras de espada de pizarra como remate. Puerta sur del Señor sellada hasta vencer a los dos Guardianes.
- Cotas: `FLOOR_H = 6`, `TOP_Y = 44`; torres vasallas con jefe en y 18 y techo en 26; Torre del Señor con jefe en y 24 y techo en 34.
- Estado del controlador, que **nunca retrocede**: dormido → vasallos (0/2, 1/2) → Señor abierto → Coloso vivo → vencido. Recompensa: un cofre por jugador
  cercano (hasta 5) con Plantilla de ascensión; logro `three_towers`. Una sola vez por estructura.
- Dificultad alta: élites con armadura visible por planta y jefes escalados por anillo de Amenaza (`TowersFlowRules`).
- **Sin probar en partida** (el autor aún no ha entrado): puede haber fallos de generación y de escaleras que él te contará.

### 3.2 Templo del Custodio (`temple/CustodianTempleLayout`, `world/CustodianTempleFeature`, `temple/TempleSealBlockEntity`)

- Subterráneo y hondo (`layout` 3, y 4 con cripta): foso abierto con rampa, **Piso 1** (atrio del Autómata Antiguo, subjefe),
  **Piso 2** (sala de pruebas con 2 oleadas), **Piso 3** (Puerta del Custodio, el Custodio, Relicario y 4 cámaras de metal) y la **Cripta de la Niebla**
  como cuarto nivel opcional. 31×31, radio 15; cotas −10, −17, −25, −34.
- La Puerta del Custodio consume el Ancla del Velo. Más raro que las torres (región 32×32).
- Los templos antiguos (`layout` 0–2) siguen generados en mundos viejos: **no se tocan**.

## 4. Restricciones técnicas (no negociables)

1. **Un `Feature` solo puede escribir en un radio de 15 desde su centro (31×31).** La fortaleza nueva **no cabe**: hay que proponer cómo generar una estructura grande (ver §3 de `docs/V88_BRAINSTORM_FORTALEZA.md`: plano puro evaluado por columna, o `Structure` propia) y **acordarlo con el autor antes de programar**. El Templo conserva el límite.
2. La forma vive en una clase **pura** (sin Minecraft) con JUnit (`TowersLayoutTest`, `CustodianTempleLayoutTest`, `DeepTempleLayoutTest`, `CryptLayoutTest`
   o sus equivalentes). Una prueba nueva no puede llamar a métodos que no existen; no rompas las que hay: si una prueba codifica una cota que cambias a propósito, actualízala y dilo.
3. Los **estados del controlador no retroceden** y se guardan como número: solo se añaden valores al **final** del enum.
4. **No cambies ids de registro** de jefes, objetos de recompensa, logros ni del bestiario; no renombres.
5. Nada de linajes, razas, pactos ni herencias (AGENTS §3). «Orden», «Mariscal» y «vasallos» son ambientación (D-39).
6. Los jefes existen **dormidos** y **no** se invocan con sellos (D-37): no reintroduzcas ningún objeto de invocación.
7. Mantén `/aptitudes locate|place three_towers` y `custodian_temple` funcionando (`StructureLocateRules`), y que ninguna estructura se genere
   si el terreno no cabe (sin fluidos, desnivel ≤ 4 en el templo; cimiento relleno en las torres).
8. El **Coloso Sin Rostro** es grande: la sala que le diseñes debe **caber** y no dejar que sus ataques crucen paredes; es la duda principal del autor.
9. Compila y pasa **todas** las pruebas (`./gradlew build`) y `python3 tools/lang/check_lang.py`; si añades textos, en `en_us` y `es_es` y copia a las demás variantes con los generadores (`AGENTS.md` §5).
10. No reformatees archivos que no cambias. Una versión por rama. Commit y push a tu rama; **ni merge a `main` ni PR sin autorización del autor**.

## 5. Qué entregar

1. **Primero (sin tocar código):** el §6, para que el autor elija.
2. Con la opción elegida: el nuevo plano en las clases puras, el `Feature` y, si cambia, el controlador (sin romper §4.3), más las pruebas.
3. `docs/V88_REDISENO_ESTRUCTURAS.md` con el plano final (cotas por piso, accesos, qué sala es cada cosa) y qué probar en partida.
4. Informe final de `AGENTS.md` §8, **sin afirmar que compila** lo que no hayas ejecutado.

## 6. Primer paso: propuestas para elegir (obligatorio antes de programar)

Entrega, para **la torre** y **el templo** por separado, **dos o tres propuestas** cada una, con:

- Silueta y huella (cabe en 31×31) en un esquema ASCII por planta o corte.
- Recorrido del jugador, de la entrada al jefe y al cofre, y dónde están las puertas entre fases sin Piedra sellada.
- Qué cambia respecto a §3 (y qué se conserva) y por qué responde a lo que no gusta (§1).
- Riesgos: generación en terreno irregular, tamaño de la sala del Coloso, escaleras, tiempo de recorrido.
- Una **recomendación** razonada.

Espera la elección del autor antes de escribir una sola línea de código.
