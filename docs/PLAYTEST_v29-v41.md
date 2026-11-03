# Protocolo de prueba en partida (v29 → v41)

**Resultado (2026-11-03): ✔ todo según lo esperado** — probado en partida por el autor (v29–v41, con
`main` en `6c709f9`). Único defecto visto: la pantalla de ajustes del HUD (Mayús + O) solapa textos y botones
(no es de la v29–v41; pendiente de corregir).

Guía del **bloque 1** (verificar antes de publicar la 1.4.0). Rellena la columna
«Resultado» con ✔, ✘ o una nota, y devuelve la tabla (o solo los fallos, con captura o el texto del error).
Los pasos detallados de cada versión están en `docs/TESTING.md`; aquí van en orden de riesgo.

## 0. Compilación y arranque (obligatorio, primero)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| 0.1 | `git pull origin main` y `gradlew.bat clean build` (`clean` solo borra `build\`). | `BUILD SUCCESSFUL` tras compilar de verdad (más de unos segundos) y todas las pruebas en verde. Si falla, copia las líneas `error:` completas. | |
| 0.2 | `gradlew.bat runClient`. Crea un mundo **Creativo con trucos**. | El juego llega al mundo sin cerrarse. | |
| 0.3 | Abre `run\logs\latest.log` y busca `aptitudes` junto a `ERROR`, `Couldn't parse`, `Mixin` o `advancement`. | Ningún error del mod (los avisos de otros mods o de Mojang no cuentan). | |
| 0.4 | Pulsa **K**, **J** (roles), **Y** (accesorios), **P** (grupo), abre el grimorio y `/aptitudes config`. | Todas las pantallas abren y cierran sin cerrar el juego. | |

Si 0.1–0.4 fallan, **para aquí** y envíame el error: el resto depende de que arranque.

**Comandos útiles** (operador). J = tu nombre o `@s`:
- `/gamemode survival` (la XP no cuenta en creativo), `/time set 1000`, `/time add 24000`.
- `/aptitudes admin setlevel J <habilidad> <nivel>`, `/roles primary <rol>`,
  `/roles admin settier J <rol> 1` (Maestro) o `2` (Definitivo).
- `/aptitudes event start soul_fog|ember_rain|eclipse`, `/aptitudes event stop`.
- Jefes sin buscar estructuras: `/place feature aptitudes:tower`, `aptitudes:bonfire_shrine` o `aptitudes:camp`.

## 1. v31 — Logros (código nuevo, sin compilar antes)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| 1.1 | Mundo nuevo, pulsa **L** (avances). | Pestaña «Aptitudes» con el grimorio de icono y fondo de pizarra; 17 logros bloqueados. | |
| 1.2 | Supervivencia. Coloca una Hoguera y descansa en ella. | Aviso «Llama encendida» y mensaje en el chat. | |
| 1.3 | `/roles primary <rol>`. | «Vocación». | |
| 1.4 | Golpea criaturas hasta el remate de combo. | «Cadena intacta». | |
| 1.5 | Engarza una runa en un arma de rol en la Mesa del gremio. | «Forjador de runas». | |
| 1.6 | `/roles admin settier J <rol> 1`, sal del mundo y vuelve a entrar. | «Maestro del oficio» al entrar (revisión retroactiva). | |
| 1.7 | `/aptitudes admin setlevel J mining 50`, sal y vuelve a entrar. | «Oficio de una vida». | |
| 1.8 | `/aptitudes event start soul_fog` y espera a que termine (no uses `stop`). | «A través de la bruma». Con `stop` no se concede. | |
| 1.9 | `/place feature aptitudes:tower`, derrota al Centinela. | «Azote de lo salvaje». | |

## 2. v30 — Contratos del gremio (reescrito, sin compilar antes)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| 2.1 | Fabrica un Tablón de contratos y úsalo. | Pantalla con 3 ofertas legibles (sin claves como `contract.aptitudes…`), rango y reputación arriba. | |
| 2.2 | Cierra y abre el tablón. | Las mismas 3 ofertas. | |
| 2.3 | Acepta una oferta. | Pasa a «Activos»; el botón Aceptar de esa oferta queda desactivado. | |
| 2.4 | Completa una de Caza (mata la criatura indicada en supervivencia). | La barra avanza; aviso «¡Contrato completado!». | |
| 2.5 | Cobra el contrato. | Esmeraldas, XP de rol y reputación; logro «Firmado y sellado». No se puede volver a aceptar hoy. | |
| 2.6 | Acepta una de Entrega sin llevar suficiente y pulsa Entregar. | Aviso y **no** se quita nada. Con la cantidad completa sí se entrega. | |
| 2.7 | `/time add 24000` y abre el tablón. | Ofertas nuevas; el contrato en curso sigue igual. | |
| 2.8 | Inventario lleno al cobrar. | La recompensa cae al suelo, no se pierde. | |

## 3. v29 — Grupos (necesita 2 jugadores: `gradlew.bat runServer` + 2 clientes)

Preparación: acepta el EULA en `run\eula.txt` y pon `online-mode=false` en `run\server.properties`.

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| 3.1 | J1 pulsa **P** e invita a J2. | J2 ve Aceptar/Rechazar clicables. Al aceptar, los dos reciben «La unión hace la fuerza». | |
| 3.2 | J1 mata criaturas cerca de J2. | J1 conserva su XP; J2 recibe la parte compartida. | |
| 3.3 | J1 golpea a J2 con espada y arco. | Sin daño. Con `/party pvp`, sí. | |
| 3.4 | Reinicia el servidor. | El grupo sigue. HUD de grupo visible. | |
| 3.5 | J1 con un contrato de Caza; J2 mata la criatura. | El contrato de J1 avanza. | |

## 4. v32–v41 (ya compilado antes; prueba rápida)

| # | Versión | Pasos | Esperado | Resultado |
|---|---|---|---|---|
| 4.1 | v32/v35 | Engarza dos runas distintas; intenta una tercera; Mayús + clic derecho en la Mesa con 4 esmeraldas. | Tercera rechazada; la última se quita y se cobran las esmeraldas. | |
| 4.2 | v33 | Baja la vida de un jefe por debajo de 2/3 y 1/3. | Cada fase se activa una vez; suelta su núcleo. | |
| 4.3 | v34 | `/aptitudes event start eclipse`. | Cielo rojo, barra del Eclipse, oleadas y Heraldo a mitad. | |
| 4.4 | v36 | Coloca piedra, sal y entra, rómpela. | Sin XP de Minería. | |
| 4.5 | v37 | Subtítulos activados durante el Eclipse. | Se ven los sonidos propios. | |
| 4.6 | v39 | Escala de interfaz 4 y pantalla K. | Barra de desplazamiento; nada se sale. | |
| 4.7 | v40 | `/aptitudes config`, cambia `eclipseIntervalDays` a 5. | Se guarda en `config\aptitudes.json`. | |
| 4.8 | v41 | `/aptitudes event start ember_rain`. | Meteoritos que no rompen bloques y dejan lingotes. | |

## 5. Pendiente de la v15–v16

Puntos C y D de `docs/PLAYTEST_v15-v19.md` (grimorio tras la muerte, Defensa, esbirros y Bombardeo orbital).

## Cómo devolver los resultados

Pega las filas con ✘ (o la tabla completa) y, para cada fallo, el texto exacto del error o del log. Con eso
corrijo en una rama propia y te pido autorización antes de cualquier commit.
