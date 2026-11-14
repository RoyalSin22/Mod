# 11 · Diseño de T3: el Templo del Custodio

**Estado: decisiones aprobadas y T3 autorizada (2026-11-14). T3a implementada; T3b a T3d, pendientes.** Se apoya en lo que el código tiene hoy (leído el 2026-11-14).

## 1. Punto de partida (VERIFICADO)

| Pieza actual | Dónde | Qué hace |
|---|---|---|
| Templo de 15×15 (radio 7) | `world/TempleFeature` (un `Feature`) | Vestíbulo con palancas y placas, tabique con puerta sellada y santuario con Relicario |
| Puzle | `temple/TempleRules` (puro, con pruebas) | Palancas (3–4) y placas (0–4) según el anillo de Amenaza |
| Controlador | `temple/TempleSealBlockEntity` (`Stage`: LEVERS, PLATES, AWAKENED, CLEARED) | Guarda el estado, abre la puerta y llama al guardián |
| Guardián | `temple/Temples.spawnGuardian` | Hace aparecer al **Custodio** como campeón; +25 % de vida por anillo |
| Recompensa | `TempleKeyItem` + `ReliquaryBlockEntity` | Una llave que abre el Relicario de ese templo |
| Regeneración | `temple/TempleRecurrenceRules` | Tras resolverse: 3 días mínimo, probabilidad creciente hasta el 25 % |
| Colocación | `placed_feature/temple.json`: 1 de cada 400 chunks; todos los biomas del Overworld | `world/ModWorldGen` |
| Autómata Antiguo | `boss/AutomatonEntity`, invocado con un sello de jefe | No está ligado a ningún templo |

Regla técnica que condiciona el diseño: un `Feature` solo puede escribir bloques dentro de la región de 3×3 chunks
que rodea a su chunk. Con el origen en cualquier posición del chunk, **el radio seguro es ≤ 16 bloques**. Las
entidades no se crean durante la generación (arreglo de la v56): aparecen después, en el hilo del servidor.

## 2. Decisiones del redactor

| # | Decisión | Motivo |
|---|---|---|
| 1 | **Seguir usando un `Feature`** (no un `Structure` con piezas), con radio 15 (31×31) | Reutiliza `StructureBuilder` y la comprobación de terreno; evita un tipo de estructura nuevo |
| 2 | **Dos niveles:** superficie (atrio y puzle) y subsuelo (antesala y cámara del Custodio) | Da espacio para el diseño sin salirse de los 31×31; la profundidad no tiene el límite de chunks |
| 3 | **El Autómata Antiguo está en el atrio**, dormido, y despierta al cruzar el umbral | Es el subjefe del autor y aparece dentro del templo |
| 4 | **El puzle solo funciona cuando el Autómata ha caído** | Respeta el orden: subjefe → puzle → sala del Custodio |
| 5 | **Nuevo bloque «Puerta del Custodio»** que se abre con el Ancla del Velo (se consume) | El Ancla ya existe desde T2 |
| 6 | **Los templos antiguos no se tocan**: siguen con su flujo y su llave única | Sin migración ni riesgo para mundos guardados |
| 7 | **Los templos nuevos sustituyen al antiguo en los chunks sin generar**: `ModWorldGen` añade el feature nuevo en lugar del viejo | Un solo tipo de templo en generación nueva. El feature viejo sigue registrado |
| 8 | Frecuencia **1 de cada 400 chunks** (la actual) | No cambio balance sin playtest. La regeneración existente aporta más templos |
| 9 | El anillo de Amenaza sigue escalando: tamaño del puzle, paleta y vida del Autómata y del Custodio | Reutiliza `TempleRules` y `Palette` |
| 10 | Sin campos de configuración nuevos, salvo que el autor los pida | Las cifras viven en reglas puras con pruebas |

## 3. Plano (PROPUESTA)

```
Nivel 0 · superficie (31×31)              Nivel −1 · subsuelo (bajo el santuario)
 ┌──────────────────────────────┐          ┌────────────────────────┐
 │  Sala del puzle (palancas,   │          │  Cámara del Custodio   │
 │  placas, puerta sellada)     │          │        (15×15)         │
 │              ▼ escalera      │ ─ baja ─▶│  ┌─ Puerta del Custodio │
 ├──────────────────────────────┤          │  Antesala (con la ranura │
 │  Atrio · Autómata dormido    │          │  del Ancla)             │
 │  ▲ entrada sur               │          └────────────────────────┘
 └──────────────────────────────┘          (T4: 4 cámaras de recompensa alrededor)
```

Se rodea todo el subsuelo con un **casco de piedra** para que cuevas, agua o lava no lo abran. Si dentro del volumen
a excavar hay agua o lava, el feature no se coloca (`return false`).

## 4. Flujo y estado (PROPUESTA)

```
DORMIDO ──pisa el umbral──▶ AUTÓMATA ──cae──▶ PUZLE ──resuelto──▶ ESCALERA ──baja──▶ PUERTA CERRADA
                                                                                       │ Ancla del Velo (se consume)
                                                                                       ▼
                                                            CUSTODIO ──cae──▶ LIMPIO (T4: 4 llaves)
```

- `Stage` se guarda como número: **solo se añaden valores al final**. Los templos antiguos leen `layout = 0` y
  conservan su comportamiento.
- Campos nuevos al final del NBT del controlador: `layout`, `automaton_defeated`, `door_open`.
- El Autómata y el Custodio aparecen **en tiempo de ejecución** (nunca durante la generación), con etiqueta del
  templo para que el servidor los reconozca tras un reinicio.
- La Puerta del Custodio decide todo en el servidor: sin Ancla muestra un aviso y no cambia nada; con Ancla la
  consume (también en creativo), quita los bloques de la puerta y hace aparecer al Custodio.

## 5. Piezas de código (a implementar en T3)

| Pieza | Descripción | Pruebas |
|---|---|---|
| `temple/TempleFlowRules` (puro) | Transiciones permitidas, cuándo funciona el puzle, cuándo se abre la puerta, vida del Autómata por anillo, radio de activación | `TempleFlowRulesTest` (JUnit) |
| `temple/CustodianDoorBlock` | Bloque nuevo con `onUseWithItem` y el consumo del Ancla | Prueba de recursos (blockstate, modelo, textos) |
| `TempleSealBlockEntity` | Campos nuevos y estados nuevos al final | Compatibilidad con NBT antiguo |
| `world/CustodianTempleFeature` | Feature nuevo, con `placed_feature` y `configured_feature` propios | Prueba manual con `/place feature` |
| `temple/Temples` | Aparición del Autómata y del Custodio con su etiqueta | — |
| `world/ModWorldGen` | Cambia `temple` por el feature nuevo en la generación nueva | — |

## 6. Fases dentro de T3 (un commit por fase)

| Fase | Contenido |
|---|---|
| **T3a** (hecha) | `TempleFlowRules` y sus pruebas, `CustodianDoorBlock`, campos nuevos del controlador y recursos del bloque (textura, modelo, textos) |
| **T3b** (hecha, sin probar en partida) | `CustodianTempleFeature`: plano de dos niveles, casco, escalera y comprobaciones de terreno |
| **T3c** | Autómata en el atrio (aparición, activación, puzle bloqueado hasta su muerte) y Custodio al abrir la puerta |
| **T3d** | Cambio en `ModWorldGen`, compatibilidad con templos antiguos, documentación, `CHANGELOG`, guion de prueba |

## 7. Riesgos

- **Escritura fuera de la región de generación:** error «far chunk». Mitigación: radio 15 y límites comprobados.
- **Terreno irregular:** un radio de 15 es exigente. Cimientos hacia abajo y tolerancia de altura; si no es
  adecuado, no se coloca.
- **Dos jefes seguidos:** muerte entre el Autómata y el Custodio. El puzle y las etapas se conservan en el bloque.
- **Mundo existente:** los chunks ya generados conservan el templo antiguo; los nuevos tendrán el nuevo.
- **Rendimiento:** unos 31×31×20 bloques por templo; se escribe una sola vez al generar.
- **A verificar antes de escribir:** cómo arranca y termina `AutomatonEntity` fuera del sello de jefe
  (`boss/SummonState`, `BossManager`, arena de `BossArenas`), y la API de `/place feature` para las pruebas.

## 8. Qué necesito del autor

1. **Autorizar T3** (la tanda más grande y arriesgada). Propongo hacerla en las cuatro fases de arriba.
2. Confirmar o corregir las decisiones 1–10 de la sección 2.
3. Para T4 (después): contenido de las cuatro cámaras de recompensa.
