# V90 — Lluvia de ideas: la Torre en ruinas (élites y campeones) y los Campamentos

Estado: **ideas para elegir, sin decisiones ni código.** Pedido del autor del 2026-11-26: rediseñar «las torres de los élites y campeones» y «los campamentos».
Las dos son estructuras **menores y frecuentes** (frente a las mayores: Tres Torres y Templo del Custodio). Código: `world/TowerFeature`, `world/CampFeature`, `world/StructureBuilder`.

## 1. Estado actual (comprobado en `main`)

### Torre en ruinas (`TowerFeature`)
- 7×7 octogonal, **4 pisos** de 5 de alto, escalera de mano continua, ventanas de barrotes, almenas.
- Cofre por piso (`tower_floor`, `tower_upper`) y **mejor cofre en la azotea** (`tower_top`: armaduras de rol históricamente, herramientas de alma, accesorios).
- **2 generadores de monstruos** (`spawner`: zombi/esqueleto en el piso 2; araña/esqueleto en el piso 3).
- **Jefe en la azotea:** Centinela de la torre (esqueleto Wither, `BossKind.TOWER_SENTINEL`, tres fases).
- Colocación: 1 de cada 240 chunks (`rarity_filter`), cualquier bioma de la Superficie, no usa cuadrícula de regiones.

### Campamentos (`CampFeature`)
- 11×11, **3 tipos** al azar: **cazadores** (verde), **brujos** (negro y morado) y **bandidos** (rojo); dos tiendas en A de lana, hoguera central.
- 2 cofres (`camp_hunter|warlock|bandit`), un detalle propio, **un campeón** (`BossKind.CAMP_CHAMPION`) y 1–2 enemigos de guardia.
- Colocación: 1 de cada 200 chunks, cualquier bioma.

### Lo que se ve flojo (hipótesis, el autor lo confirma)
Las dos son **planas y repetidas**: una torre idéntica siempre, un campamento de dos tiendas siempre igual; poca historia, poca variedad por bioma y el combate es «llega, mata, abre cofre»;
los generadores de monstruos permiten **granjas**; no hay relación con el anillo de Amenaza ni con los élites que ya existen.

## 2. Ideas para la Torre en ruinas (élites y campeones)

| Eje | Ideas |
|---|---|
| **Silueta** | Variantes: torre redonda, cuadrada con contrafuertes, torre inclinada o a medias derrumbada; con **muralla baja y patio** (hasta ~25×25 caben en el `Feature` actual, sin motor por columna) |
| **Interior** | Pisos distintos: armería, dormitorio, taller, sala de guardia, biblioteca pequeña; **plataformas y galerías** a media altura; sótano con calabozo |
| **Combate** | Sustituir generadores por **encuentros del controlador** (una oleada por piso, una sola vez, despiertan al entrar), como en la fortaleza; **élites con armadura visible** por planta y el Centinela como campeón final |
| **Dificultad** | Por **anillo de Amenaza** (paleta y número de élites, ya existe `ThreatRules`): torre de piedra cerca de la aparición, de pizarra y piedra negra lejos |
| **Jefe** | Mantener al Centinela (3 fases) o añadir **tipos de campeón** por variante (arquero, mago, bruto) con el sistema `elite/` |
| **Botín** | Cofres repartidos por pisos y uno oculto; el mejor, en la azotea; sin objetos de poder nuevos |
| **Entorno** | Torres **por bioma** (nevada, de pantano, de jungla, de montaña) con materiales propios |
| **Historia** | Carteles o notas de guardias (lo escribe Codex); estandartes rotos; relación con la orden de la Vigilia o con la Niebla, sin linajes |

## 3. Ideas para los Campamentos

| Eje | Ideas |
|---|---|
| **Más tipos** | Además de cazadores, brujos y bandidos: **mercaderes asaltados** (carros volcados), **soldados desertores**, **cultistas del Velo**, **buscadores de reliquias** (excavación con tiendas) |
| **Layout** | Variantes de forma: círculo de tiendas, **empalizada con puerta**, campamento en un claro, junto a una cascada o un camino; entre 2 y 6 tiendas, carro, corral, establo |
| **Vida** | Una «escena»: cocina, almacén, hoguera de reunión, tiendas con camas y cofres, tablón con cartel; animales atados o jaulas **sin recompensa de poder** |
| **Combate** | 2–5 enemigos con **un capitán campeón**; emboscada al abrir un cofre importante; centinelas en puntos altos; **alarma** si el campeón te ve (los demás se unen) |
| **Dificultad** | Por **anillo de Amenaza**: más lejos, más enemigos, élites con armadura visible y campeón más duro |
| **Por bioma** | Paleta y objetos distintos en desierto, nieve, selva, pantano, llanura y montaña |
| **Botín** | Tablas por tipo (ya existen `camp_*`) y **un cofre oculto** o «caja fuerte» |
| **Contratos** | Posible enlace con el tablón de contratos (`contract/`): contrato de «limpiar un campamento», sin poder nuevo |

## 4. Restricciones técnicas

1. **Sin motor nuevo:** ambas caben en el `Feature` (radio 15); no necesitan el plano por columna de las estructuras grandes. Pueden crecer hasta ~25×25 y mantener el sistema actual.
2. **Frecuencia:** hoy 1/240 y 1/200 chunks. **No cambiar sin preguntar** (balance); decidir si quieres más o menos y si pasan a cuadrícula de regiones (`/aptitudes locate` ya las encuentra por el índice).
3. **Sin granjas:** reemplazar los generadores de monstruos por encuentros del controlador (una sola vez por planta o escena); si se quiere conservar algún generador, que sea de uso limitado.
4. **Jefes y botín:** conservar `BossKind.TOWER_SENTINEL` y `CAMP_CHAMPION`, las tablas `tower_*` y `camp_*` y los ids de registro; solo se añaden valores al final.
5. **Sin linajes, razas ni pactos** (`AGENTS.md` §3); «orden», «cultistas» y «desertores» son ambientación.
6. **Rendimiento:** no generar enemigos al crear el chunk; que despierte cerca un controlador o `BossManager`.
7. **Estado:** como todo lo reciente, nada de esto se ha probado en partida.

## 5. Decisiones para el autor

1. ¿Qué es lo que **más no te gusta** hoy de cada una (forma, interior, combate, botín, repetición)?
2. **Torre:** ¿tamaño? (7×7 actual; propuesta **~15×15 con patio** o hasta ~25×25), ¿variantes por bioma y por anillo de Amenaza?
3. **Torre:** ¿se quitan los generadores de monstruos y se sustituyen por encuentros únicos con élites?
4. **Campamentos:** ¿qué tipos nuevos quieres (mercaderes asaltados, desertores, cultistas, buscadores)?
5. **Campamentos:** ¿1–6 tiendas con variantes de forma y escena (empalizada, claro, carro)?
6. ¿Dificultad por anillo de Amenaza en las dos?
7. ¿Se tocan las frecuencias (1/240 y 1/200) o se mantienen?
8. ¿Cofres ocultos en ambas?
9. ¿Enlace de los campamentos con el tablón de contratos?
