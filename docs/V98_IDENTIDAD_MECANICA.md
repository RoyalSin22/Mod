# V98 — Identidad mecánica de los cinco pilotos (inventario y primera entrega)

Fecha: 2026-11-26. Alcance: Bersérker, Elementalista, Cambiaformas, Paladín e Ingeniero. Regla del plan: **no crear barras nuevas**; auditar
lo que ya existe y solo cubrir huecos reales. Este documento es el inventario (hecho leyendo el código y los textos de los nodos, sin jugar) y
registra lo que se cambió.

## 1. Cómo se muestra hoy una mecánica

- Cada rol tiene **una mecánica base** (nodo fila 0 central) que el HUD ya dibuja junto a la barra de habilidades (`RoleMechanics.sync`, `MechanicPayload`,
  `AbilityHud.renderMechanics`): pips de acumulación o un estado con nombre. Sincroniza cada segundo y al cambiar.
- Los **recursos de los nodos N1–N10** viven en los `*Kit` (estado en memoria) y, hasta V98, **no se veían** en ningún sitio (salvo los de los seis
  roles de `NewRoles`). El jugador no sabía cuántas cicatrices llevaba, si tenía Ira justa cargada o si Presa fácil estaba lista.

## 2. Inventario por rol

| Rol | Mecánica base (HUD) | Recursos de nodos | ¿Se veían? | Hueco detectado |
|---|---|---|---|---|
| Bersérker | Frenesí (acumulaciones) | Cicatrices N1, Sangre hirviente N2 (bajo 50 % de vida), Juramento N7, Avatar N8, Eco de batalla N9 (ventana de 3 s) | No | La identidad «riesgo/recompensa» existe (N1, N2, N7) pero no se leía. Las cicatrices ya se ven. |
| Elementalista | Reacción elemental (ventana tras cambiar con G) | Cambio de marea N1, Eco N2, Armonía N9 (3 elementos en 10 s), Forma N8 | No | La Armonía exige llevar la cuenta de elementos de memoria. Ya se ve. |
| Cambiaformas | Instinto (acumulaciones solo transformado) | Presa fácil N7, Tercer cambio N9, Furia N8; N1/N2 solo transformado | No | Estados «listo» invisibles. Ya se ven. En humano el rol tiene poca identidad (casi todo exige estar transformado). |
| Paladín | Luz sagrada (5 cargas) | Ira justa N9 (carga por daño recibido), Oración N5 (agachado 2 s), Vínculo N7 | No | La carga de Ira justa no se veía. Ya se ve. |
| Ingeniero | Sobrecarga (calor/Sobremarcha) | Minas N3 (máx. 3), Dron N4, Domo N6, Exotraje N8, Torre Tesla N10 | No | Cuántas minas hay puestas no se veía. Ya se ve. |

## 3. Qué cambia en V98 (1.ª entrega)

Solo **visibilidad**, sin tocar ninguna cifra ni mecánica: `ExtraKits.hud(...)` pide a cada kit sus recursos activos y `RoleMechanics.sync` los añade
al indicador existente (mismo `MechanicPayload`, mismo dibujo, sin textos nuevos: usa el icono y el nombre del propio nodo). Un recurso solo aparece
mientras vale algo, para no llenar la barra.

| Rol | Se muestra | Cómo |
|---|---|---|
| Bersérker | Cicatrices (N1) | Pips: cicatrices activas / máximo del rango |
| Elementalista | Armonía elemental (N9) | Elementos distintos de la ventana (0–3); 3 llenos mientras dura el bonus |
| Cambiaformas | Presa fácil (N7), Tercer cambio (N9) | Un pip encendido cuando el efecto está listo |
| Paladín | Ira justa (N9) | Cinco tramos del máximo de su rango |
| Ingeniero | Minas de proximidad (N3) | Pips: minas puestas / 3 |

## 4. Lo que no se hace todavía (y por qué)

- **No se añade ninguna barra, recurso ni cifra nueva.** Sin datos de partida ni de telemetría no hay forma de saber qué huecos duelen.
- Siguen sin mostrarse: Oración y Vínculo del Paladín, el Exotraje, el Dron, el Domo y la Torre Tesla del Ingeniero, el Cambio de marea y la
  Forma del Elementalista, y la Furia del Cambiaformas (conviene un indicador de tiempo restante, no de pips).
- **Riesgo de solo (decisión del autor: todos bien en solitario):** del Paladín, *Triunfo compartido* (N2) y *Vínculo de escudo* (N7) están pensados
  para aliados, y *Oración* (N5) también. En solitario solo aportan lo que le toca al propio jugador; hay que medirlo con `/aptitudes telemetry` antes de
  tocar nada.
- **Cambiaformas en humano:** N1, N2, Instinto y el resto de bonificaciones exigen estar transformado. Posible hueco de identidad; se decidirá con datos.
- **Ingeniero e invocaciones:** dron, minas y torre cuentan para el presupuesto de V94 (`summonBudget`); comprobar en partida que no se retiran solos.

## 5. Pendiente

1. Que el autor juegue los cinco pilotos y confirme si los pips son legibles y si falta algún recurso (los contadores de tiempo no están).
2. Con la telemetría (V93) comparar a los cinco con la línea base `VANILLA` antes de añadir mecánicas (V99).
3. Repetir este inventario con los otros 15 roles (los seis de `NewRoles` ya tienen sus recursos en el HUD).
