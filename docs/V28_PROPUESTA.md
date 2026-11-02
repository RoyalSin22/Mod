# v28–v30 — Combos, Grupos y Contratos del gremio

Estado: **aprobado con cambios** por el autor (ver «Cambios del autor»). v28 en implementación.

## Cambios del autor (prevalecen sobre el texto de abajo)

1. **Combos**: aprobados; **solo contra criaturas** (no cuentan en PvP).
2. **Grupos**: una **tecla abre un menú de grupo** con los jugadores conectados y un botón **Invitar** en cada
   uno (los comandos quedan como alternativa). El **líder elige el porcentaje de XP compartida** (0–50 %, de 5 en
   5). La XP compartida y la **bonificación por miembro llegan a todos los miembros conectados, estén donde
   estén** (no solo a los cercanos).
3. **Contratos**: se usan desde un **bloque nuevo, «Tablón de contratos»** (receta y textura propias). Además
   de la reputación, la evolución del rol principal sube la dificultad: con **Maestro** aparecen contratos
   de **Élite** y con **Definitivo**, **Legendarios** (objetivos mayores y mejores recompensas).
 Orden sugerido: **v28 combos** (el más acotado), **v29
grupos** (los contratos lo aprovechan), **v30 contratos**. Cada versión con su commit y tu autorización.

---

## v28 — Sistema de combos

| Aspecto | Propuesta |
|---|---|
| Qué cuenta | Golpes cuerpo a cuerpo directos a criaturas (no a aliados, invocaciones ni miembros del grupo) |
| Ventana | El combo sigue si el siguiente golpe llega en **2,5 s**; si no, se reinicia. Recibir un golpe de más de 6 de daño también lo rompe |
| Bonificación | **+3 % de daño por golpe** de combo (máximo +15 % a partir de 5), dentro del tope global de daño (×6) |
| Remate | Al llegar a **5 golpes**, el siguiente golpe es un **remate** según la categoría del rol principal (espera de 4 s entre remates) |
| Recurso de rol | Cada golpe de combo carga +2 del recurso del rol (el **Combo** del Asesino, ×2) |
| HUD | Contador «×5» junto a la mira que crece y cambia de color (blanco → oro → rojo); destello al rematar |

Remates por categoría (daño = 6 + 4 % del combo acumulado):

| Categoría | Remate |
|---|---|
| Marcial | **Golpe sísmico**: daño en área (3 bloques) y lentitud |
| Sombra | **Ejecución**: +50 % de daño si el objetivo está por debajo del 40 % de vida, y sangrado (Marchitamiento) |
| Arcana | **Descarga arcana**: rayo que salta a 2 enemigos cercanos |
| Guardián | **Golpe sagrado**: daño, y te cura el 30 % del daño a ti y a tu grupo cercano |
| Oficio | **Sobrecarga**: chispa eléctrica que encadena a 3 enemigos y los aturde brevemente |

Configuración: `combosEnabled`, `comboWindowSeconds` (2,5), `comboDamagePerHit` (0,03), `comboMaxBonus` (0,15),
`comboFinisherHits` (5). Lógica pura `ComboCounter` con pruebas.

---

## v29 — Grupos (party)

| Aspecto | Propuesta |
|---|---|
| Formar grupo | `/party invite <jugador>` → mensaje con **[Aceptar] [Rechazar]** clicables (caduca en 60 s); `/party leave`, `/party kick <jugador>` (líder), `/party list`. Máximo **5** miembros |
| Sin fuego amigo | Los miembros no pueden herirse entre sí (ni con habilidades). El líder puede activarlo con `/party pvp` |
| XP compartida | Al matar una criatura, los miembros a menos de 32 bloques reciben el **25 %** de la XP de combate y de rol del que la mató (no se resta al que mata). **+5 % de XP por cada miembro cercano** (máximo +20 %) |
| Habilidades de apoyo | Curas, auras, escudos y bailes del Bardo, Paladín, Herbolista, etc. priorizan al grupo: si estás en un grupo, afectan **solo a tu grupo** (no a extraños); sin grupo, como ahora |
| HUD | Marcos de grupo a la izquierda: nombre, icono del rol, barra de vida y efectos importantes; se atenúan si el miembro está lejos |
| Persistencia | El grupo se guarda en el mundo (sobrevive a reinicios y desconexiones); un miembro desconectado aparece en gris |
| Combos | Los remates de Guardián curan al grupo cercano |

Configuración: `partyMaxSize` (5), `partyXpShare` (0,25), `partyXpBonusPerMember` (0,05),
`partyShareRadius` (32). Lógica pura del reparto de XP con pruebas.

---

## v30 — Contratos del gremio

| Aspecto | Propuesta |
|---|---|
| Dónde | **Mayús + clic derecho** en la Mesa del gremio abre el **Tablón de contratos** (clic normal sigue siendo la fabricación) |
| Oferta | 3 contratos nuevos **cada día de Minecraft** por jugador; puedes tener **2 activos** a la vez (3 con rango Veterano); abandonar es gratis |
| Tipos | **Caza** (matar N de un tipo de criatura), **Entrega** (llevar N minerales, cultivos o peces), **Pesca** (N peces), **Encantamiento** (N objetos), **Oficio** (ganar N de XP en una habilidad), **Forja** (fabricar una pieza de armadura en la Mesa del gremio) |
| Dificultad | Fácil, Normal y Difícil, según el rango de gremio |
| Recompensas | Esmeraldas, Fragmentos de alma, XP del rol principal y **reputación del gremio**; los difíciles pueden dar Vales de armería o Emblemas de maestría |
| Reputación | Rangos **Aprendiz → Oficial (100) → Veterano (300) → Maestro (700)**: más contratos, contratos difíciles y recompensas exclusivas |
| Grupo | Los contratos de Caza avanzan también con las muertes de tu grupo cercano |
| Seguridad | Todo lo valida el servidor (aceptar, entregar y cobrar); la oferta diaria es fija por jugador y día (no se puede «tirar» de nuevo cerrando la pantalla) |
| Persistencia | En el jugador (sobrevive a la muerte y a los reinicios) |

Configuración: `contractsPerDay` (3), `maxActiveContracts` (2), recompensas base y umbrales de reputación.
Lógica pura de generación, progreso y reputación con pruebas.

---

## Riesgos y decisiones abiertas

- Los tres sistemas añaden pantallas o HUD: se comprobará que caben con escala de interfaz 4.
- La XP compartida se suma al rendimiento decreciente existente (no permite granjas en grupo).
- Ninguna de estas mecánicas introduce linajes, razas, pactos ni herencias.
