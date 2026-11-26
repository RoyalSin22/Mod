# V90 — Diseño: Torre en ruinas y Campamentos rediseñados

Estado: **diseño con las decisiones del autor del 2026-11-26; sin código.** Parte de `docs/V90_BRAINSTORM_TORRES_Y_CAMPAMENTOS.md` (estado actual y banco de ideas).
Esto es **un rediseño, no un reemplazo**: las dos estructuras siguen siendo las mismas del mod, con mejor aspecto y una generación más acorde al resto del juego.

## 1. Decisiones del autor

| Tema | Decisión |
|---|---|
| Alcance | **Rediseñar**, no quitar ni cambiar su papel. Se conservan: ids de las `Feature` (`tower`, `camp`), `BossKind.TOWER_SENTINEL` y `CAMP_CHAMPION`, las tablas de botín `tower_*` y `camp_*`, `/aptitudes locate` y `place` |
| Frecuencias | **Se mantienen** (1 de cada 240 chunks la torre, 1 de cada 200 el campamento). No se tocan |
| Torre | **~20×20 con patio**, con **diversos cuartos y pisos**. Los **generadores de monstruos se quedan**, junto a los **encuentros con élites y campeones** |
| Campamentos | **Más grandes**, con **estructuras y enemigos** repartidos, **variantes de forma** |
| Dificultad | **Por anillo de Amenaza** en las dos |
| Botín | **Un cofre oculto** en cada una (además de los repartidos) |
| Contratos | **Enlace de los campamentos con el tablón de contratos** |
| Generación | «Más acorde al mod»: ver §2 |

## 2. Qué significa «generación más acorde al mod»

Interpretación (confírmala): que se comporten como las estructuras nuevas, no como las de la primera época.

| Aspecto | Hoy | Diseño |
|---|---|---|
| **Dificultad** | Fija | **Anillo de Amenaza** del lugar (`ThreatRules.ring`, como `CustodianTempleFeature.ring`): más lejos de la aparición, más y mejores élites, campeón más duro y paleta más oscura |
| **Paleta** | Siempre piedra y madera de abeto | Paleta por **anillo** (piedra → pizarra profunda → piedra negra) y por **bioma** (nieve, pantano, jungla, desierto, montaña) |
| **Forma** | Siempre igual | **Variantes** elegidas con la semilla del chunk, de modo que dos torres o dos campamentos casi nunca sean idénticos |
| **Terreno** | Cimiento y poco más | Cimiento y rampas que se funden con el terreno, ruinas más naturales; no se genera en pendientes mayores que el tope actual ni con fluidos (`StructureBuilder.suitable`) |
| **Coherencia** | Cofres fijos | Cofres repartidos por cuartos, uno oculto, y botín **sin objetos de poder nuevos**; las tablas existentes se reutilizan |
| **Sistema** | `locate/place` ya funcionan | Se mantienen; cada `Feature` sigue llamando a `StructureIndex.record` |

## 3. Cómo lo vamos a diseñar (método)

Mismo patrón que las estructuras recientes, pero **sin motor por columna**: ambas caben en el `Feature` (radio 15).

1. **Plano puro** por estructura, sin clases de Minecraft y con JUnit: `dungeon/TowerCompoundLayout` y `dungeon/CampLayout` (nombres orientativos), con un `enum Cell` y una función `cell(x, y, z)` por variante.
2. **`Feature`** que traduce cada celda a bloques con la paleta del anillo y del bioma, y coloca cofres, generadores y enemigos con `StructureBuilder`.
3. **Pruebas:** que todas las celdas queden dentro del radio, que cada variante sea **alcanzable a pie** (búsqueda de caminos como en `DeepTempleLayoutTest` y `TowersLayoutTest`: suben las escaleras, se llega a cada cofre y al jefe), que sea **determinista** y que haya los cofres, generadores y salidas esperados.
4. **Recursos:** tablas de botín nuevas solo si hacen falta (los cofres repartidos reutilizan `tower_floor`, `tower_upper`, `tower_top` y `camp_*`; el oculto puede usar la del cofre principal).
5. **Textos y documentación:** claves en `en_us` y `es_es` con los generadores de idiomas; `CHANGELOG.md` y `docs/TESTING.md`.

Fases: 1) propuestas con esquemas ASCII (el autor elige), 2) planos puros y pruebas, 3) `Feature` y paletas, 4) encuentros, jefes y botín, 5) enlace con contratos, 6) textos, 7) prueba en partida.

## 4. Torre en ruinas — diseño

**Huella:** ~20×20 (radio 10, dentro del límite de 15). Un **patio amurallado** con la **torre** en un lado.

```text
   N  ┌────────────────────┐   muralla baja con brechas
      │ establo   cuerpo de │   cuerpo de guardia (anexo)
      │ arruinado guardia   │
      │       ┌──────┐      │
      │ pozo  │TORRE │ huerto│   torre ~9×9, 5 pisos
      │       └──────┘      │
   S  └──── portón roto ────┘
```

| Elemento | Propuesta |
|---|---|
| **Patio** | Portón roto, pozo, restos de establo y cuerpo de guardia, escombros, huerto abandonado; combate abierto con **élites** de guardia |
| **Torre** | ~9×9, **5 pisos** de 5 de alto más sótano; escaleras alternadas y **galerías a media altura** (plataformas) |
| **Pisos y cuartos** | **Sótano:** calabozo y bodega. **P0:** sala de guardia. **P1:** armería. **P2:** dormitorios. **P3:** taller o biblioteca pequeña. **P4:** sala del vigía con balcón. **Azotea:** el Centinela y el mejor cofre |
| **Generadores** | Se **quedan**: en P2 y P3 como ahora, con el tipo según el anillo (zombi, esqueleto, araña…) y valores moderados (retardo, enemigos cercanos); no se quitan |
| **Encuentros** | **Élites fijos con armadura visible** por piso (`Elites.makeElite`), número por anillo; el **Centinela** (3 fases) sigue siendo el campeón final; un **campeón** opcional en el patio en anillos altos |
| **Botín** | Un cofre por piso (`tower_floor`/`tower_upper`), el mejor en la azotea (`tower_top`) y **un cofre oculto** tras un muro de ladrillos agrietados (se rompe con herramientas normales; pista sonora o visual) |
| **Variantes** | Forma de la torre: cuadrada con contrafuertes, **redonda**, a medias **derrumbada**; patio con o sin muralla completa |
| **Paleta** | Anillo 0–I: piedra y musgo. II–III: pizarra profunda. IV–V: piedra negra y oro; bioma: piedra nevada, ladrillos de pantano, madera de jungla… |

## 5. Campamentos — diseño

**Huella:** hasta **~29×29** (el máximo del `Feature`), con 2–6 tiendas según variante; sigue habiendo tres tipos (cazadores, brujos, bandidos).

| Variante de forma | Descripción |
|---|---|
| **Círculo de tiendas** | Tiendas en anillo alrededor de una hoguera central; cuatro entradas |
| **Fortín de empalizada** | Muro de madera con puerta, **torre de vigía** de madera, patio interior |
| **Línea de camino** | Tiendas y carros alineados junto a un sendero; barricadas en los extremos |
| **Terraza** | Campamento sobre una pequeña elevación con rampa y plataformas |

| Zona | Cazadores | Brujos | Bandidos |
|---|---|---|---|
| Tiendas grandes con interior | camas, cofre, pieles | camas, alquimia, cofre | literas, armas, cofre |
| **Estructura propia** | ahumadero, secadero, polígono de tiro | círculo de piedras con caldero y alquimia (sin pactos) | barricadas, botín apilado, **jaula** |
| Vida del campamento | cocina, leña, corral | huerto de hongos, velas | cocina, barriles, mesa de mapas |
| **Estructura de vigilancia** | torre de vigía (arqueros) | atalaya de piedra negra | torre de vigía con ballesta |

**Enemigos:** un grupo de **3 a 8** según el anillo (guardias + élites con armadura visible), **un capitán campeón** (`BossKind.CAMP_CHAMPION`) y **alarma**: si el capitán o un vigía te ve, el resto se une. Un **cofre oculto** (enterrado bajo tierra suelta o tras una pared de lona doble) con pista visual. Todo con botín por tipo (`camp_*`), sin poder nuevo.

## 6. Enlace con los contratos

- **Contrato «Limpiar un campamento»:** nueva oferta del tablón (`ContractRules.Kind`, **añadida al final del enum**: se guarda como número). Se completa al vencer al **capitán campeón** de un campamento.
- **Cambio necesario:** marcar al capitán con su **tipo de campamento** (etiqueta de la entidad: cazadores, brujos o bandidos) para que el contrato pueda ser «Limpiar un campamento de bandidos». `BossManager` o un gancho en la muerte de `CAMP_CHAMPION` avisa al gestor de contratos.
- **Recompensa:** la de un contrato del mismo nivel (esmeraldas, fragmentos de alma, reputación); sin objetos de poder nuevos.
- **Textos y pruebas:** claves en los 5 idiomas y `ContractRulesTest` ampliado (progreso, guardado con compatibilidad hacia atrás).
- **Reparto:** puede hacerlo Codex junto a los campamentos, o Claude después, en una fase aparte (toca `contract/` y el tablón).

## 7. Riesgos y vigilancias

1. **Generadores y granjas:** se quedan por decisión del autor; para limitar las granjas, valores moderados de aparición y de enemigos cercanos, y que el botín mayor siga siendo el del cofre.
2. **Tamaño del campamento:** 29×29 es el tope del `Feature` (radio 15). Más grande exigiría el motor por columna.
3. **Coste de generación:** más bloques por estructura; son frecuentes (1/240 y 1/200), así que hay que **medir** el tiempo de generación y el número de enemigos por zona.
4. **Enemigos al generar:** se colocan al generar (como hoy con `b.mob` y `b.boss`); si se comprobara que pesa, pasar a despertarlos al acercarse un jugador.
5. **Escaleras y accesos:** lo más probable que falle (como en las Tres Torres): se cubre con la búsqueda de caminos de las pruebas y se comprueba en partida.
6. **Sin probar en partida**, como todo lo reciente.
7. **Exclusiones** (`AGENTS.md` §3): nada de linajes, razas ni pactos; «ritual» de los brujos es solo ambientación.

## 8. Qué queda por decidir

1. ¿Los **generadores** conservan el tipo actual por piso (zombi/esqueleto y araña/esqueleto) o cambian con el anillo?
2. ¿La **torre redonda** y la **derrumbada** son variantes imprescindibles o bastan dos formas?
3. **Campamentos:** ¿alguno de los **tipos nuevos** (mercaderes asaltados, desertores, cultistas del Velo, buscadores) o solo los tres actuales con más variedad?
4. **Alarma del campamento:** ¿sí (si el capitán ve al jugador, los demás se unen) o los enemigos actúan por separado?
5. **Contrato:** ¿«Limpiar un campamento» por tipo (cazadores, brujos, bandidos) o genérico?
6. **Cofre oculto:** ¿pista visual (bloque distinto) y sonora, o sin pista?
7. ¿Prefieres que el **contrato** lo haga Codex con los campamentos o lo hago yo después, en una fase aparte?
