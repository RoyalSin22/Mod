# Protocolo de prueba en partida (v15 → v19)

Pruebas manuales de lo añadido desde la v15. **No están ejecutadas**: este documento es la guía para
hacerlas. Rellena la columna «Resultado» y devuelve la tabla final (o los fallos) para corregirlos.

## Preparación (una vez)

1. Compila `main` (`.\gradlew.bat build`) y copia el `.jar` de `build\libs\` (el que no termina en
   `-sources`) a la carpeta `mods` de una instancia con Fabric Loader 0.16.14 y Fabric API 0.116.6.
2. Crea un mundo **Creativo con trucos**, o usa `.\gradlew.bat runClient` desde el proyecto.
3. Para las pruebas de reinicio y de 2 jugadores: `.\gradlew.bat runServer` (acepta el EULA en
   `run\eula.txt`, pon `online-mode=false` en `run\server.properties`) y conecta dos clientes.
4. Comandos útiles (requieren permisos de operador):
   - `/gamemode survival` y `/gamemode creative`: algunas pruebas de XP **no dan XP en creativo**.
   - `/aptitudes admin setlevel <jugador> <habilidad> <nivel>`
   - `/roles primary <rol>`, `/roles admin setlevel <jugador> <rol> <nivel>`,
     `/roles admin settier <jugador> <rol> <0-2>` (1 = Maestro, 2 = Definitivo),
     `/roles learn <nodo>`, `/roles use <1-7>` (5 = definitiva, 7 = característica),
     `/roles admin clearcooldown <jugador>`.

Abreviatura: **J** = tu nombre de jugador (o `@s`).

---

## A. Pesca y XP (v15)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| A1 | Supervivencia. Pesca hasta sacar un **pez** (bacalao, salmón…). | Aviso de XP de **Pesca** (+12). La basura y los tesoros **no** dan XP. | |
| A2 | `/aptitudes admin setlevel J fishing 50`. Pantalla **K**. | Aparece la 9.ª tarjeta, «Pesca», con 5 hitos dorados; **ninguna tarjeta se sale del panel** (prueba con escala de interfaz 2, 3 y Auto). | |
| A3 | Con Pesca 50, nada bajo el agua. Luego quédate quieto dentro del agua. | Gracia del delfín al nadar; Poder del conducto dentro del agua. | |
| A4 | Pesca unos 20 peces con Pesca 50. | Alguna captura doble (≈20 %), +2-4 de XP vanilla por pez y algún tesoro marino (≈10 %). | |
| A5 | Mata **más de 20 zombis** en menos de un minuto (huevos de generación + espada). | Desde el 21.º, el aviso de XP de Combate **baja**. Si cambias a esqueletos, la XP vuelve a ser completa. | |

## B. Legendarias exclusivas (v15.1), 2 jugadores

Antes: pon `"legendaryExclusive": true` en `config/aptitudes.json` y usa `/aptitudes admin reload`
(o reinicia).

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| B1 | Jugador A y B: `/roles primary mage` y `/roles admin setlevel <A/B> mage 30`. A aprende la legendaria del Mago en el árbol (J). | A la aprende. | |
| B2 | B abre el árbol del Mago y pasa el ratón por la legendaria. | Botón **Mejorar inactivo**; la ficha dice «**La tiene A**». | |
| B3 | A: `/roles respec mage` (o `/roles admin reset A`). B, sin cerrar el juego, vuelve a abrir el árbol. | B ya puede aprenderla. | |

## C. Grimorio (v15.2)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| C1 | Jugador nuevo entra al mundo. | Recibe **un** Grimorio de Aptitudes. | |
| C2 | Muere (`/kill`), reaparece, sale del mundo y vuelve a entrar. | **No** recibe un segundo grimorio. | |

## D. Defensa, esbirros y Bombardeo orbital (v16)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| D1 | Supervivencia con escudo. Deja que un zombi te golpee **bloqueando** con el escudo. | **Sin** XP de Defensa. Sin escudo: sí hay XP. | |
| D2 | `/roles primary necromancer`, nivel 30, aprende (`/roles learn raise_minions`) y equipa en el árbol «Alzar esbirros». Invócalos y métete con ellos bajo el agua unos 30 s. | Los zombis esbirros **desaparecen con burbujas** antes de convertirse; **nunca** aparece un ahogado hostil. | |
| D3 | `/roles primary engineer`, `/roles admin settier J engineer 2`, `/roles use 5` apuntando a un grupo con un aldeano, otro jugador y monstruos. | Los monstruos reciben daño y empuje; **tú, el aldeano y el otro jugador no**. No se rompen bloques. | |

## E. Anti-granjas de colocar y romper (v17)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| E1 | Supervivencia. Coloca un tronco y rómpelo. | **Sin** XP de Tala. Un tronco de un árbol natural: XP normal. | |
| E2 | Planta un brote, hazlo crecer con polvo de hueso y tala el tronco de la base. | XP **normal** de Tala; con Tala 30 y agachado con hacha, se tala el árbol entero. | |
| E3 | `/aptitudes admin setlevel J mining 30`. Coloca mineral de diamante (cógelo con Toque de seda) y rómpelo con un pico normal 20 veces. | Sin XP de Minería y **nunca** un diamante extra por «Veta rica». | |
| E4 | `/roles primary engineer`. Coloca redstone sobre un bloque válido; luego intenta colocarla donde no se puede (p. ej., sobre cristal). | XP de rol solo en la colocación válida. Colocar y romper la misma pieza 40 veces en un minuto: la XP baja hasta casi 0. | |

## F. Esperas y muros persistentes (v18), con `runServer`

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| F1 | Lanza la definitiva (`/roles use 5`, con un rol Definitivo). Sal del servidor y vuelve a entrar. | La espera **sigue** (HUD incluido); no se puede volver a lanzar. | |
| F2 | Lanza la definitiva y **cierra el servidor** (`stop`). Arráncalo y entra. | La espera continúa donde estaba (unos segundos menos por el tiempo de arranque). | |
| F3 | Con una legendaria activa (tecla H), sal y vuelve a entrar. | El HUD muestra la espera de la legendaria (antes salía a 0). | |
| F4 | `/roles primary elementalist`, nivel 30, `/roles admin settier J elementalist 1`, elige un elemento (J → vista de elementos) y lanza la característica (`/roles use 7`): aparece un muro. Pica un bloque del muro. | El bloque **desaparece sin soltar nada** y sin XP. | |
| F5 | Levanta el muro y **cierra el servidor** antes de 10 s. Vuelve a arrancar y entra. | El muro desaparece en los segundos que le quedaban. | |
| F6 | Muro con elemento Planta: espera 10 s cerca. | Las hojas no se descomponen ni sueltan brotes; desaparecen a los 10 s. | |

## G. Interfaz y mobs con IA Brain (v19)

| # | Pasos | Esperado | Resultado |
|---|---|---|---|
| G1 | Opciones → Gráficos → Escala de interfaz **4** (o ventana pequeña). Abre los roles (J). | Barra dorada a la derecha; la rueda **fuera del árbol** o RePág/AvPág desplazan la ventana hasta ver «Hecho»; el zoom del árbol no cambia. Pista inferior: «Rueda: zoom · RePág/AvPág: ventana». | |
| G2 | Escala Auto en pantalla grande. | Sin barra; la pantalla se ve como antes. | |
| G3 | Nether, supervivencia, sin oro. `/roles primary merchant`, nivel 30, aprende `appraiser_eye` y `bribe` (`/roles learn <nodo>`), equipa «Soborno» en el árbol y úsalo junto a piglins que te atacan. | Dejan de atacar en menos de medio segundo y no vuelven mientras dure el soborno. | |
| G4 | `/roles primary shapeshifter`, nivel 30, aprende `mimicry` y `trickster`, transfórmate en un mob hostil y acércate a piglins brutos y hoglins. | No te persiguen. | |

---

## Cómo devolver los resultados

Pega las filas con **resultado distinto del esperado** junto con:
- qué hiciste exactamente y qué pasó;
- si apareció algún error en el chat o en `logs/latest.log` (las líneas con `[Aptitudes]` o `Exception`).

Con eso se corrige en la siguiente versión. Si todo sale bien, basta con «todo OK».
