# V70 — Templo del Custodio hondo: tres pisos subterráneos, sin puzle

Estado: **aprobado por el autor e implementado en la rama `claude/cripta-niebla`; pendiente de prueba en partida.**

## 1. Decisiones del autor (cerradas)

| Tema | Decisión |
|---|---|
| Puzle | Se quita (sin palancas ni placas). Cada piso termina en una sala de combate. |
| Exterior | Foso abierto con rampa; el resto del templo está bajo tierra. |
| Pisos | 3 pisos + la Cripta de la Niebla debajo (cuarto nivel opcional). |
| Compatibilidad | Solo los templos nuevos usan el diseño (layout 3 y 4). Los de layout 0, 1 y 2 conservan su flujo y no se tocan. |

## 2. Plano (31×31, radio 15; y = 0 es el suelo de la superficie)

| Nivel | Qué hay | Cotas |
|---|---|---|
| Superficie | Foso: patio de 15×8 (x ±7, z −2…5) y rampa de 5 de ancho (z 6…15) con escalones que bajan al norte; braseros, columnas rotas | suelo del patio y −10 |
| Piso 1 — Atrio | Arco en el muro norte del patio, sala x ±13 × z −13…−4 con columnas, braseros, luces en el techo, alfombra; **Autómata Antiguo** | suelo −10, techo −3 |
| Piso 2 — Sala de pruebas | Mismo hueco bajo el atrio, marco de arena, nichos con linternas; escalera (este) desde el Piso 1 | suelo −17 |
| Piso 3 — Cámara del Custodio | Escalera central, antesala, Puerta del Custodio, cámara con columnas y braseros, Relicario y las cuatro cámaras de recompensa (el subsuelo del templo antiguo, desplazado −17) | suelo −25 |
| Cripta de la Niebla | Cuarto nivel opcional (trampilla, oleadas y clímax de la V69), desplazado −17 | suelo −34, fondo −36 |

Cobertura: techo del Piso 1 en y −3, tierra en −2 y césped en −1; nada más sobresale del terreno salvo el foso, la rampa, los
braseros y las columnas.

## 3. Flujo (`TempleFlowRules.Flow`)

`DORMANT → AUTOMATON → TRIAL → DOOR_LOCKED → CUSTODIAN → CLEARED` (`TRIAL` se añadió al final del enum: se guarda como número).

1. **Piso 1:** el Autómata despierta al entrar en el atrio. Al caer, la piedra sellada de la escalera este se rompe.
2. **Piso 2:** con jugadores en la sala salen **2 oleadas** de guardianes (zombis y esqueletos sin botín; tamaños 4 y 6, +2 por
   jugador adicional, ×(1 + 0,25 × anillo), ×`cryptWaveSizePercent`; tope 12 vivas). Sin jugadores vivos durante
   `cryptIdleResetSeconds` la oleada se reinicia. Al vencer la segunda se rompe la piedra sellada de la escalera central.
3. **Piso 3:** la Puerta del Custodio sigue pidiendo el Ancla del Velo; el Custodio, las cuatro cámaras y el Relicario no cambian.
4. **Cripta:** como en la V69 (Llave del Velo, trampilla, tres oleadas y clímax), un nivel más abajo.

## 4. Piezas

`temple/CustodianTempleLayout` (`buildDeep`, `buildUnderground(r, dy)`, `buildCrypt(r, dy)`; celdas `SOIL`, `GRASS`, `STAIR_SOUTH`,
`BRAZIER`), `temple/TempleFlowRules` (`LAYOUT_DEEP` = 3, `LAYOUT_DEEP_CRYPT` = 4, `Flow.TRIAL`, `isDeep`, `afterTrialCleared`),
`temple/TempleSealBlockEntity` (`serverTickTrial`, `setupDeep`; NBT `gate_one`, `trial_*`), `world/CustodianTempleFeature` (siempre
genera el templo hondo; sin cripta si su volumen toca fluidos). Sin configuración nueva: la prueba reutiliza `cryptWaveSizePercent`
y `cryptIdleResetSeconds`.

## 5. Límites y riesgos

- Los guardianes de la prueba son provisionales (zombis y esqueletos); el balance es una estimación.
- Un templo no se genera si el terreno tiene más de 4 de desnivel, si la rampa empieza en el agua o si no cabe bajo el fondo del mundo.
- Los templos de las versiones anteriores siguen en los mundos ya generados; solo los nuevos son hondos.
