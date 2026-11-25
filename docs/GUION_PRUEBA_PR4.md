# Guion de prueba del PR #4 (rama `claude/practical-feynman-id8op1`)

Nada de lo de este PR se ha probado en un juego real. Este guion está ordenado **por riesgo**: lo que más probablemente falla va primero.
**Regla de oro: para en el primer fallo de un bloque y cuéntamelo**; lo siguiente suele depender de él. Tiempo total ≈ 60 min; el bloque A solo, ≈ 15.

## 0. Preparación (5 min)

```bash
.\gradlew.bat runClient
```

- Mundo **nuevo**, creativo para empezar, **dificultad Normal** (en Pacífico no aparecen jefes ni criaturas) y trucos activados. Eres operador.
- Abre el registro: `run/logs/latest.log`. Antes de nada busca `ERROR` o `Exception` con «aptitudes»: **sin errores al arrancar** ya es la primera prueba (registro de bloques, features y logros).
- Anota la **semilla** (`/seed`): la necesito si algo falla en la generación.

---

## A. Las Tres Torres Señoriales (lo más arriesgado)

| # | Haz esto | Esperado | Si falla, dime |
|---|---|---|---|
| A1 | `/aptitudes place three_towers` | A 24–96 bloques aparece un patio con **tres torres** de piedra musgosa (dos de 11×11 al sur, una de 13×13 al norte) con almenas y una **empuñadura de espada** de pizarra en cada cima | El error exacto del registro; captura de lo que se generó |
| A2 | `/aptitudes locate three_towers` | Da coordenadas | Lo que responde |
| A3 | Mira el **cimiento y el patio** desde fuera | Sin columnas flotando; el terreno irregular queda rellenado; sin hierba ni árboles dentro del patio | Captura |
| A4 | Entra por el **sur** de una torre vasalla (puerta 3×3) | Se entra sin saltar; hay antorchas de alma en los muros | Captura de la puerta |
| A5 | Sube **caminando** hasta la sala superior (3 plantas + sala del jefe) | Las escaleras suben sin saltar; alternan muro norte y sur | **Orientación de las escaleras** (mi punto más débil): captura de dónde se corta |
| A6 | Cambia a **supervivencia** y repite A5 en otra torre | Igual | — |
| A7 | La **Torre del Señor**: ¿tiene la puerta sur sellada? | Piedra sellada 3×3, **irrompible** | Si se puede romper |
| A8 | En el centro del patio, bloque **Piedra de la Vigilia** | Irrompible en supervivencia | — |

### A9. Combate (en supervivencia, con armadura y arma decentes; usa `/gamemode survival`)

| # | Haz esto | Esperado |
|---|---|---|
| A9a | Pisa **cada planta de combate** de una torre vasalla | Salen 4 puntos de criaturas **élite con armadura completa visible**, **una sola vez por planta**. Salir y volver **no** repite la oleada |
| A9b | Entra en la sala superior de una torre vasalla | Despierta un **Guardián Desmontado** (aviso en barra de acción). Antes no existe |
| A9c | Sal a media pelea y vuelve | El Guardián sigue ahí, **sin duplicarse** |
| A9d | Vence al primero | «Ha caído un guardián. Quedan 1.» La puerta del Señor **sigue sellada** |
| A9e | Vence al segundo | Se abre la puerta del Señor (sonido + aviso) |
| A9f | Entra en la sala alta de la Torre del Señor | Despierta el **Coloso Sin Rostro**. **¿Cabe? ¿Sus ataques atraviesan paredes? ¿Es jugable en espacio cerrado?** |
| A9g | Vence al Coloso | **Un cofre por jugador cercano** (hasta 5) con una **Plantilla de ascensión** cada uno + Corazones de campeón, fragmentos y diamantes. Logro «Fin de la Vigilia» |
| A9h | Sal, entra, y **reinicia el servidor** | No reaparece nada. La puerta sigue abierta. Los cofres siguen |

Atajo para no repetir: en creativo con `/kill @e[type=aptitudes:dismantled_guardian]` cuenta como derrota (es lo esperado).

**Qué quiero saber del Coloso:** si el combate funciona, qué ataque te parece injusto en la sala, y si hace falta la niebla de arena (`BossArenas`).

---

## B. Eventos y Eclipse (≈ 15 min)

### B1. Eclipse: Behelit y Astrolabio (código del otro chat + mi `DISABLED`)

```
/give @s aptitudes:resonador_del_umbral 3
/give @s aptitudes:sunset_astrolabe
```

Clic derecho sobre una **Hoguera** (`aptitudes:bonfire`) en el Mundo Normal. **Los objetos no tienen textura todavía**: se verán como «modelo ausente», es lo esperado.

| # | Esperado |
|---|---|
| B1a | Behelit: empieza el Eclipse y se consume **uno** |
| B1b | Con el Eclipse en curso, otro Behelit: **no se consume**, avisa «ya hay otro evento» |
| B1c | `/aptitudes eclipse stop`, y usa otro enseguida: **no se consume**, avisa de los **días** que faltan (≈3) |
| B1d | Pantalla de configuración (`/aptitudes config`): `eclipseSummonCooldownDays` = 0 → se puede usar de inmediato |
| B1e | `eclipseEnabled` = false: avisa y **no se consume** (mi veredicto `DISABLED`) |
| B1f | Astrolabio como **operador**: funciona. Como jugador normal sin grupo (`/op` fuera, o `/deop`): rechaza y **no se consume**. Como **líder de un grupo**: funciona |
| B1g | En el Nether: rechaza (solo Mundo Normal) |

### B2. Niebla de almas por el tablón (mi cambio)

El tablón es el bloque `aptitudes:contract_board`. La oferta «**Convocar la Niebla de almas**» sale al azar entre las 3 del día (dificultad Normal o más). Para verla: `/time add 24000` y reabre el tablón hasta que salga; **puede tardar varios días de juego**.

| # | Esperado |
|---|---|
| B2a | Aceptar la oferta: empieza la Niebla para todos y el aldeano del gremio **ya no la inicia** (Mayús + clic solo abre el comercio) |
| B2b | Con la Niebla en curso, aceptar otra oferta igual: **no se acepta**, mensaje del aldeano de siempre |
| B2c | Tras terminar, intentar de nuevo: «los muertos se han ido, pero volverán» |
| B2d | `worldEventWeightSoulFog` = 0: la oferta sale pero no se acepta y avisa de que está desactivada |

Si no te sale la oferta en un rato razonable, salta B2 y dímelo: es la parte más cara de probar sin comandos.

### B3. Migración de configuración
Cierra el juego, edita `run/config/aptitudes.json`: pon `"worldEventWeightSoulFog": 0, "worldEventWeightEmberRain": 0, "configRevision": 83`, arranca: **ambos pasan a 1**.

---

## C. Parada y contraataque (S3, activada por defecto) — 10 min

Supervivencia, un escudo, un zombi (`/summon minecraft:zombie`).

| # | Esperado |
|---|---|
| C1 | Alza el escudo **justo** cuando va a golpearte: «¡Parada!», partículas y sonido agudo; el zombi queda **inmóvil ~2 s** |
| C2 | Golpéalo en 3 s: «¡Golpe de gracia!» y **×1,5** de daño; un segundo golpe ya no |
| C3 | Alzar el escudo **con antelación**: sin parada |
| C4 | Un **esqueleto** (flecha) bloqueado: sin parada |
| C5 | Un campeón, un Wither, otro jugador: **sin** parada ni aturdimiento |

**Aquí es donde dependo de una suposición:** que `AFTER_DAMAGE` marque `blocked` cuando el escudo bloquea. Si C1 **nunca** se dispara, es eso.

---

## D. Mensajes en el suelo (S5) — 5 min

```
/aptitudes message list
/aptitudes message danger
```

| # | Esperado |
|---|---|
| D1 | Aparece una nota «Peligro adelante [+0]» **siempre de cara a ti** |
| D2 | Con otro jugador (o tu otra cuenta): clic derecho en la nota → «Valorado: +1», el texto pasa a `[+1]`; segundo clic: «Ya has valorado…»; en la tuya: «No puedes valorar tu propio mensaje» |
| D3 | Cuarta nota seguida: rechaza (límite 3). Segunda en menos de 30 s: avisa de la espera |
| D4 | `/aptitudes message remove` retira la tuya más cercana |
| D5 | Reinicia el servidor: la nota y sus votos siguen |

## E. Ecos de muerte (S6, **desactivados por defecto**) — 5 min

Actívalos: `deathEchoesEnabled = true`. Con dos jugadores: el primero corre unos segundos y muere (`/kill`).

| # | Esperado |
|---|---|
| E1 | Aparece una **cabeza con su skin, brillante**, donde murió |
| E2 | Si el otro jugador está a < 24 bloques, la cabeza **repite el recorrido** (a saltos, ~10 pasos/s), se detiene 2 s y repite. Al alejarse, se detiene |
| E3 | A los 120 s desaparece. Con 7 muertes seguidas, la más antigua se retira |
| E4 | Reinicia con un eco vivo: **no** queda ninguna cabeza fantasma |

---

## F. Regresión rápida de C1/C2 (lo que más puede romper sin que lo notes) — 10 min

| # | Haz esto | Esperado |
|---|---|---|
| F1 | Abre la pantalla de roles (**J**) y la de habilidades (**K**) | Se abren y se ven igual que antes |
| F2 | Elige rol, gasta puntos en una **senda con efecto** (p. ej. crítico) y golpea | El efecto se nota. Gasta **otro** punto: cambia **sin reiniciar** (cachés de C2) |
| F3 | Cambia de rol principal, y haz «Reiniciar» del árbol | Ningún efecto de senda ni piedra angular queda «pegado» |
| F4 | Muere y reaparece | Los efectos y el árbol siguen bien |
| F5 | En grupo de 2: mata criaturas | La XP de grupo se reparte (sinergias Y2) |
| F6 | `/aptitudes synergy status` | Responde sin errores |

---

## Qué copiarme (aunque sea a medias)

```
Bloque y paso: A5
Qué hice / qué esperaba / qué pasó:
Error del registro (líneas con "aptitudes", "three_towers" o Exception):
Captura (si es visual):
Coordenadas (/aptitudes locate three_towers) y semilla:
```

No hace falta que lo redactes bien. **Lo más valioso:** el resultado de A1 (¿genera?), A5 (¿suben las escaleras?) y A9f (¿es jugable el Coloso?).
