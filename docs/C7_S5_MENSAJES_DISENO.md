# C7 (S5) — Mensajes en el suelo: diseño

Estado: **propuesta de Claude (2026-11-25), con decisiones del autor del mismo día (§10).** No hay código. Lo que
sigue abierto va marcado con ❓.

Origen: `docs/IDEAS_v47_v55.md` (S5: «notas cortas de frases predefinidas, sin texto libre, que otros jugadores leen
y valoran»), dentro de C7 en `docs/PLAN_CLAUDE.md`.

## 1. Objetivo y límites

- Un jugador deja en el suelo una nota formada **solo con piezas de un catálogo** (plantilla + palabra). Los demás
  la ven, la leen **en su idioma** y pueden valorarla como útil.
- **Sin texto libre**: no hay nada que moderar, y la nota se traduce sola porque se guarda como ids, no como texto.
- **Inspiración, no copia** (§0 de las ideas): la mecánica de notas valoradas es genérica. No se toman las frases,
  plantillas, iconos ni nombres de ningún juego. El catálogo (§3) es original y habla del mundo del mod
  (Hoguera, Niebla, Eclipse, Custodio…).
- Exclusiones de `AGENTS.md` §3: el catálogo no contiene linajes, razas, pactos ni herencias. Una prueba lo comprueba.

## 2. Experiencia de juego

1. **Escribir.** Con la **Tiza de ceniza** (objeto nuevo, receta en §10) en la mano, clic derecho sobre la cara
   superior de un bloque sólido. Se abre una pantalla pequeña (≤ 426×240): lista de plantillas a la izquierda y
   palabras por categoría a la derecha, con la vista previa en el idioma del jugador. Al confirmar, el servidor valida y
   coloca la nota. Se gasta una tiza.
2. **Ver.** Cada nota es un **glifo de ceniza tenue** en el suelo, dibujado por el cliente (no es un bloque ni una
   entidad). Brilla un poco más cuanto más valorada esté.
3. **Leer.** Si miras un glifo a ≤ 4 bloques, aparece una línea en el HUD con la frase traducida, el autor ❓ y las
   valoraciones. No hace falta hacer clic.
4. **Valorar.** Mientras se muestra la línea, una tecla (por defecto **V**, ❓) la marca como «útil». Una vez por
   jugador y nota, nunca la tuya.
5. **Borrar.** El autor la borra con Mayús + clic derecho y la tiza en la mano. Un operador puede borrar con un
   comando (§8).

## 3. Catálogo de frases

Una nota es `plantilla` + `palabra`. Las dos se guardan **por clave de texto**, nunca por índice (regla de
persistencia de `AGENTS.md` §5), así que se pueden añadir piezas al final sin romper mundos.

**Plantillas** (12, borrador; el texto final irá en en_us y es_es y en los generadores de pt/fr/de):

| Clave | es_es (borrador) |
|---|---|
| `danger` | Cuidado: %s |
| `ahead` | Más adelante, %s |
| `need` | Aquí hará falta %s |
| `try` | Prueba con %s |
| `weak` | Punto débil: %s |
| `hidden` | Hay algo oculto: %s |
| `rest` | Descansa antes de %s |
| `no` | No sigas: %s |
| `good` | Merece la pena: %s |
| `remember` | Recuerda %s |
| `beware_night` | De noche, %s |
| `thanks` | Gracias por %s |

**Palabras** (unas 60, por categorías):

| Categoría | Ejemplos (borrador) |
|---|---|
| Criaturas del mod | el Osario Quebrado, el Errante Velado, el Autómata Antiguo, un élite, un campeón |
| Lugares | la Hoguera, el templo, la cripta, la Niebla de almas, el foso, una trampa |
| Objetos | una llave, el Ancla del Velo, un frasco de Estus, un escudo, antorchas |
| Acciones | parar el golpe, esquivar, retroceder, ir en grupo, saltar, esperar |
| Conceptos | el Eclipse, la Lluvia de brasas, el fuego, la magia, el veneno, la oscuridad |

El catálogo vive en una clase pura (`social/MessageCatalog`) con prueba: claves únicas, `%s` exacto en cada
plantilla, todas las combinaciones con texto en los 11 idiomas y ningún término excluido. ❓ ¿Prefieres que los
textos de las palabras mantengan el artículo (como arriba) o que la plantilla lo ponga?

## 4. Datos y persistencia

- **Fuente de verdad en el servidor**: `social/GroundMessageState` (`PersistentState`, uno por dimensión, como
  `StructureIndex`). Cada nota: `id` (UUID), `pos` (BlockPos), `author` (UUID), `authorName` (para mostrarla sin
  consultar perfiles), `template`, `word`, `created` (tiempo del mundo), `raters` (conjunto de UUID, con tope).
- **Límites**, validados en `validated(...)` y documentados en `docs/CONFIGURATION.md`:

| Opción ❓ | Por defecto ❓ | Rango | Para qué |
|---|---|---|---|
| `groundMessagesEnabled` | true | — | Interruptor general |
| `groundMessagesPerPlayer` | 10 | [1, 100] | Notas vivas por jugador; al pasar el tope se borra la más antigua |
| `groundMessagesPerChunk` | 4 | [1, 32] | Evita llenar una zona |
| `groundMessageLifetimeDays` | 30 | [0, 365] | Días de juego hasta que caduca (0 = no caduca) |
| `groundMessageMinSpacing` | 2 | [0, 8] | Distancia mínima entre dos notas, en bloques |

- **Mundo de un jugador**: sirve como apuntes propios. ❓ Opcional: notas «del mundo» escritas por el mod en
  estructuras (pistas de templo y cripta). Darían valor sin otros jugadores, pero son trabajo aparte.

## 5. Red y autoridad del servidor

| Paquete | Sentido | Contenido | Validación en el servidor |
|---|---|---|---|
| `GroundMessageSyncPayload` | S2C | Notas cercanas (≤ 48 bloques): id, pos, plantilla, palabra, autor, valoraciones, «ya la valoré» | — |
| `GroundMessageWritePayload` | C2S | pos, plantilla, palabra | Lleva tiza en la mano; bloque a ≤ 5 bloques y a la vista; claves del catálogo; límites de §4; `RequestLimiter` (1 por segundo) |
| `GroundMessageRatePayload` | C2S | id | Existe; a ≤ 6 bloques; no es tuya; no la valoraste ya; `RequestLimiter` |
| `GroundMessageDeletePayload` | C2S | id | Eres el autor (o operador); a ≤ 6 bloques |

- **Sincronización**: cada 40 ticks, por jugador, se compara el conjunto de notas cercanas con el último enviado y
  solo se manda la diferencia, como `KeystonePayload` (`LAST_SENT`). Al cambiar de dimensión o reconectar se envía
  completo. Coste: unos 40 bytes por nota; con los topes de §4 es despreciable.
- Hilos: el estado solo se toca en el hilo del servidor. Los paquetes C2S se procesan con `server.execute`.

## 6. Cliente

- `client/social/ClientGroundMessages`: copia local de las notas recibidas. Se vacía al desconectar.
- Dibujo: `WorldRenderEvents.AFTER_ENTITIES`, como `CastAnimations`. Un cuadrado plano con la textura del glifo
  (generada con un script de `tools/textures`, sin editar PNG a mano), con un brillo que crece con las valoraciones.
- Lectura: raycast del cliente contra las notas cercanas (≤ 4 bloques) y línea del HUD.
- Configuración del cliente: `ClientConfig.showGroundMessages` (true) para ocultarlas solo para ti.

## 7. Valoraciones y recompensa

Decisión del autor: **opción B** (recompensa material al autor). Cifras propuestas ❓, pendientes de aprobar:

- Cuando alguien valora tu nota y estás conectado y vivo: **+2 de vida** (un corazón) y **+5 XP de Combate**
  (pasa por el rendimiento decreciente normal, `XpDiminishing`).
- **Freno contra granjas** (cuentas secundarias o amigos que se valoran entre sí):
  - Como mucho **5 valoraciones con recompensa por autor y día de juego** (`groundMessageRewardsPerDay`). Las
    siguientes cuentan para el contador, pero no dan nada.
  - Una misma persona que valora te da recompensa **una vez por día de juego**, aunque valore varias notas tuyas.
  - Sin recompensa si quien valora está en tu grupo (`PartyManager`) o si la nota tiene menos de 1 minuto.
- Si no estás conectado, la valoración se guarda pero la recompensa se pierde (no se acumula).
- Logro «Cronista de la ceniza» al acumular 10 valoraciones (con o sin recompensa).

Opciones nuevas: `groundMessageRewardHeal` (2,0; [0, 20]), `groundMessageRewardXp` (5; [0, 100]),
`groundMessageRewardsPerDay` (5; [0, 50]; 0 = sin recompensa).

## 8. Comandos y administración

- `/aptitudes messages near [radio]`: lista las notas cercanas (operador).
- `/aptitudes messages clear <radio|jugador>`: borra notas (operador). Mismo estilo que `locate` y `place` de V77.

## 9. Archivos previstos

- Puros con JUnit: `social/MessageCatalog`, `social/GroundMessageRules` (límites, espaciado, caducidad, quién puede
  valorar o borrar, diferencia de sincronización).
- Servidor: `social/GroundMessageState`, `social/GroundMessages` (registro, paquetes, sincronización), objeto
  `item/AshChalkItem`, cuatro paquetes en `network/`, comando en `command/AptitudesCommand`.
- Cliente: `client/social/ClientGroundMessages`, `client/screen/GroundMessageScreen`, dibujo y línea del HUD, tecla V.
- Recursos: textos en los 11 idiomas (plantillas, palabras, pantalla, avisos, opciones), textura de la tiza
  (`gen_items.py`) y del glifo, receta, logro si se elige A.
- Docs: `CONFIGURATION.md`, `TESTING.md`, `CHANGELOG.md`, `ROADMAP_APTITUDES.md`, capítulo del grimorio ❓.

Estimación: tarea mediana (del orden de C3). Sin mixins ni dependencias nuevas.

## 10. Decisiones

Tomadas por el autor (2026-11-25):

1. **Representación**: glifo dibujado por el cliente, con los datos en el servidor.
2. **Tiza de ceniza**: receta barata en la Mesa del gremio (1 carbón vegetal + 1 polvo de hueso + 1 fragmento de
   alma = 4 tizas); cada nota gasta una.
3. **Recompensa por valoración**: opción B, con las cifras y frenos de §7 pendientes de aprobar.
4. **Notas del mundo** en estructuras: más adelante, en una fase posterior que reutilice este sistema.

Abiertas ❓:

5. Cifras de §7 (curación, XP, tope diario y exclusión del grupo).
6. **Autor visible**: mostrar su nombre (propuesta: sí, con una opción de servidor para ocultarlo).
7. **Catálogo**: ¿vale el borrador de §3 como base, o prefieres dar tú las plantillas?
