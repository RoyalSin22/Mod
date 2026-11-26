# V89 — Templo del Custodio desértico

Estado: implementado en `codex/templo-desierto`; falta validación visual y de recorrido en Minecraft. Elección del autor: propuesta B, cisterna central y espiral larga.

## Huella y construcción

- Huella natural de **112×112 bloques** (`x,z = −48…63`), 7×7 chunks; cotas respecto a la superficie `y = −43…+8`, 52 niveles con cripta y 44 sin ella. El origen está en el chunk designado de una región de 32×32 chunks.
- `DesertTempleLayout` es puro: una celda depende solo de `(x,y,z, conCripta)`. `DesertTempleFeature` valida el sitio una vez por origen y cada chunk construye únicamente sus columnas. El controlador se configura cuando se genera **su** chunk; el Relicario se enlaza cuando se genera **el suyo**. Ninguno escribe en el chunk vecino.
- Solo desierto o badlands en toda la huella. Rechaza desnivel mayor de 4, agua/lava preexistente en el volumen principal, falta de profundidad y candidatos de pirámide vanilla a menos de seis chunks. Si el volumen de la cripta tiene fluidos o no cabe, genera el templo sin ella. La arena no se usa para suelos ni techos: son de arenisca lisa, tallada o cortada; en badlands cambia a arenisca roja y terracota.
- Una región ofrece **como máximo un candidato entre 1024 chunks**; la frecuencia real es menor porque exige 112×112 del bioma admitido, superficie llana, subsuelo seco y separación de pirámides. No hay todavía medición en varias semillas, así que no se da una frecuencia real numérica. Las Tres Torres conservan su región de 24×24 y deberían seguir siendo más comunes.
- Presupuesto máximo del plano: 49 chunks × 256 columnas × 52 cotas = **652 288 evaluaciones de celda** con cripta; solo las celdas no nulas escriben bloques. La validación de terreno usa muestras del generador y una caché por mundo/origen. Medir el tiempo de generación en partida sigue pendiente.

## Plantas y recorrido

`E` entrada, `A` Autómata, `P` prueba, `C` Custodio, `R` Relicario, `M` cámaras de metal, `X` salida posterior, `↓` descenso. Los espacios son esquemáticos.

```text
Superficie (y 0…8)
     pozo de luz          obelisco      obelisco
       ○                     │              │
                arco roto — E ⇣ escalera hundida
       X cerrado hasta vencer al Custodio

Nivel 1 (suelo y −10)
  capilla ─── corredor norte ─── ↓ NE
     │                            │
  catacumbas     cisterna      espejos / taller
     │          ═ puentes ═        │
  barracones ── reja 1 ── A atrio ┘
                           ↑ entrada

Nivel 2 (suelo y −22)
  sombras ──────────────── llegada NE
  forja        cisterna       │
    │         ≈ agua ≈       P (2 oleadas)
  reja 2 ← corredor sur ────┘
    ↓ SO

Nivel 3 (suelo y −34)
  ↓ SO ─ corredor norte ─ M oro ─┐
  M cobre  [miradores]  M hierro│
       ┌──────── C ────────┐    │
       │      R, cripta ↓   │    │
       └─ Puerta + Ancla ──┘← corredor sur
         M diamante          │
  X sellada ← salida con escalera hasta superficie

Cripta opcional (suelo y −42)
  trampilla con Llave del Velo → 3 oleadas → Osario campeón → cofre
```

La entrada en `z≈62` baja a `z≈52`; el Autómata despierta al acercarse al atrio `(0,−9,40)`. Su derrota abre la reja 1 en el lado oeste del atrio. El corredor recorre barracones, catacumbas y capilla hacia el descenso NE. En el nivel 2 se pasa por el ala oriental y la cámara de pruebas `(20…40,10…28)`; sus dos oleadas abren la reja 2 del descenso SO. El nivel 3 rodea el santuario por norte, este y sur, y termina en la Puerta del Custodio `(0,−33,13)`, que consume el Ancla del Velo. Tras el combate se abre el Relicario y una salida con escalera en `z≈−38`; antes de la derrota tiene rejas en ambos extremos. No hay atajo de ida.

Los dos pozos de luz atraviesan desde la superficie hasta el nivel 1. La cisterna central comunica visualmente los niveles 1 y 2, tiene agua somera en el nivel 2, puentes secos y dos miradores de vidrio sobre el santuario. Las estatuas sin rostro están en el atrio, las catacumbas y la antesala; no añaden poderes ni mecánicas.

## Cuartos, encuentros y botín

| Cuarto | Nivel | Interior y encuentro | Botín |
|---|---:|---|---|
| Atrio | 1 | Autómata Antiguo, estatuas | Abre reja 1 al caer |
| Barracones | 1 | Camas de terracota y corredor | Cofre modesto |
| Catacumbas | 1 | Nichos y campeón husk opcional, aparece cerca del jugador | Cofre oculto |
| Capilla | 1 | Altar y campeón husk opcional | Cofre modesto |
| Espejos | 1 | Vidrios, reflejos y pozo de luz | Cofre oculto |
| Talleres | 1 | Horno y estancias de trabajo | Cofre modesto |
| Cisterna | 1–2 | Puentes, agua somera y miradores | Exploración, sin premio obligatorio |
| Sombras | 2 | Nave oscura | Cofre oculto |
| Forja | 2 | Horno y almacén | Cofre modesto |
| Pruebas | 2 | Dos oleadas existentes | Abre reja 2 |
| Santuario | 3 | Custodio, Puerta con Ancla | Relicario después del jefe |
| Cuatro cámaras | 3 | Puertas de cobre, hierro, oro y diamante con sus llaves actuales | Tablas existentes de cada metal |
| Cripta | 4 opcional | Llave del Velo, oleadas y clímax existentes | Cofre original al terminar |

Los cofres de suministro contienen comida y materiales modestos. Los ocultos tienen materiales algo mejores; no contienen objetos de poder nuevos. El campeón opcional usa el sistema `elite/` existente. No se generan criaturas al crear chunks.

## Compatibilidad y pruebas en partida

Los layouts guardados **0–4** permanecen legibles y sin cambio de ID. Los nuevos son 5 (sin cripta) y 6 (con cripta); no se reordenan valores de `Flow`. Se conserva el puzle antiguo de palancas y placas **solo para templos ya generados** con layouts 0–2; no se usa en V89 y no se retiró para preservar esos mundos.

Probar en un mundo nuevo: localizar y colocar en desierto y badlands; comprobar que no aparece en otros biomas, bajo agua, en laderas >4 ni sobre pirámides; visitar los 49 chunks en orden variable; recorrer las dos rampas y verificar ambas rejas, Autómata, dos oleadas, Ancla, Custodio, Relicario, cuatro llaves y cripta opcional; volver a entrar tras guardar/cargar; comprobar botín y los dos campeones; verificar que la salida solo se abre después del Custodio. Medir tiempo de generación y frecuencia en varias semillas. Estas comprobaciones manuales están pendientes.
