# V89 — Lluvia de ideas: el Templo del Custodio (subterráneo, de desierto, en ruinas)

Estado: **ideas para elegir, sin decisiones ni código.** Pedido del autor del 2026-11-26. Complementa `docs/PROMPT_CODEX_REDISENO_ESTRUCTURAS.md` y comparte método con
`docs/V88_BRAINSTORM_FORTALEZA.md` (plano puro por columna). La mecánica de jefes y recompensas (Autómata, sala de pruebas, Puerta del Custodio con Ancla del Velo,
Custodio, Relicario, cuatro cámaras y cripta) se conserva salvo que se diga lo contrario; el lore lo escribe Codex.

## 1. Concepto (lo que pidió el autor)

Un templo **subterráneo, de desierto, en ruinas**, con **estructura de templo de desierto** (arenisca y derivados), **escala grande**, **muchos cuartos** con interior
distinto cada uno, **plataformas** y **botín repartido** entre varios cuartos. Desde fuera solo se ve una entrada medio enterrada entre las dunas; el templo es la parte de abajo.

## 2. Decisiones técnicas que condicionan todo (a decidir)

| Tema | Situación | Propuesta |
|---|---|---|
| **Solo en desiertos** | Hoy sale en cualquier bioma de la Superficie (`foundInOverworld`) y por cuadrícula de regiones de 32×32 chunks (un chunk designado por región). Si solo vale el desierto, las regiones sin desierto se quedan sin templo | Filtro de bioma (desierto) **en la colocación**; el índice y `/aptitudes locate` solo buscan donde hay desierto. **Rareza real = rareza de la región × cobertura de desierto.** Por la decisión D-35 («el Custodio es más raro que las torres») probablemente baste la región de 32; si en una semilla hay poco desierto, `locate` avisará «no hay» |
| **Escala** | Hoy 31×31 (radio 15, límite del `Feature`) y ~36 de profundidad con cripta | **Grande, ~96×96 de huella y hasta ~45 de profundidad** (rango a discutir 80–128). Exige el **mismo método que la fortaleza**: `TempleLayout` puro evaluado por columna, cada chunk escribe su parte. Conviene **construir el motor una sola vez** y que lo usen la fortaleza y el templo |
| **Desierto vanilla** | Los templos del desierto de Minecraft (pirámides) ya existen y tienen su propio arte y botín | Que el Custodio **no se solape** con una pirámide vanilla (distancia mínima) y que su entrada se lea **distinta**: no una pirámide, sino una escalera hundida entre ruinas y estatuas |
| **Arena que cae** | La arena y la grava caen si pierden el apoyo; el techo de un templo subterráneo no puede ser de arena | Techos, suelos y muros de **arenisca** (lisa, tallada, cortada, roja) y terracota; la arena solo como **montones decorativos apoyados** |
| **Terreno** | Las dunas son irregulares y bajo ellas puede haber cuevas, acuíferos y lava | Mismos controles que hoy: no se genera si hay fluidos o no cabe; la superficie se reconstruye sobre la entrada; huecos bajo tierra solo si el volumen está libre o es roca |
| **Coste** | Un volumen subterráneo grande son muchas colocaciones de bloques por chunk | Escribir solo lo que cambia (muros y aire interior), sin rellenar toda la roca; medir el tiempo de generación (como pide el encargo de la fortaleza) |

## 3. Plano propuesto

Cuatro «pisos» como ahora, pero ensanchados en alas. Cada piso es un nivel (no una sola sala) con varias salas, pasillos y verticalidad:

| Nivel | Qué es | Ideas de cuartos |
|---|---|---|
| **Superficie** | Entrada entre dunas | escalinata hundida, arco roto, estatuas truncadas, un obelisco, pozo de luz; dos o tres «bocas» falsas que dan a cámaras cerradas |
| **Nivel 1 — Vestíbulos y Atrio** | Acceso y primera pelea | **Atrio del Autómata** (ya existe), salón de columnas, sala de guardia, establos de cámaras, cisterna seca, patio hundido a cielo abierto por un derrumbe |
| **Nivel 2 — Alas** | Exploración opcional con botín repartido | **Biblioteca-archivo**, **sala de la cisterna** (agua, plataformas y puente), **catacumbas** (nichos, sarcófagos, élite o campeón opcional), **sala de los espejos o la luz** (rayos de sol por pozos), **taller o forja** de los guardianes, **barracones**, **sala de pruebas** (ya existe, 2 oleadas), **cámara de la sombra** (oscura, antorchas de alma) |
| **Nivel 3 — Santuario** | Final del recorrido | Puerta del Custodio (Ancla del Velo), antesala, **cámara del Custodio** (amplia, con plataformas laterales y columnas), **Relicario** y **las cuatro cámaras de metal** repartidas en alas distintas |
| **Nivel 4 — Cripta** | Opcional (Llave del Velo) | como hoy: trampilla, tres oleadas, clímax (Osario campeón) y cofre; solo se generaría si el volumen lo permite |

Las **plataformas** serían losas y puentes sobre fosos y cisternas, balcones sobre salas hundidas, galerías superiores en la biblioteca y la cámara del Custodio, y escaleras de caracol en los pozos de luz.

## 4. Lenguaje visual: «templo de desierto en ruinas»

- **Materiales:** arenisca lisa, cortada, tallada y roja; terracota (glaseada para detalles); bloques de oro como adorno; cobre oxidado en cámaras; vigas de madera oscura; piedra tallada en las puertas.
- **Ruinas legibles:** techos hundidos que dejan caer **haces de luz y arena**, columnas caídas, muros con brechas, escombros de arenisca, telarañas, montones de arena apoyados, huesos.
- **Motivo:** el **sol** y el **Velo** (coherente con el Eclipse del mod): discos y relieves solares tallados, un sol «tapado» en la cámara final. **Sin ojos, rostros ni pactos** (exclusión de `AGENTS.md` §3).
- **Luz:** pozos de luz natural arriba y luz azul de «alma» solo en las salas de jefe y mini-jefe; el resto en penumbra con braseros apagados.
- **Dificultad ambiental sin mecánicas nuevas:** pasillos estrechos, fosos, plataformas sobre vacío con caída controlada al piso inferior (con daño de caída normal).

## 5. Botín repartido (sin poder nuevo)

| Dónde | Qué |
|---|---|
| Cuartos del Nivel 1–2 | cofres modestos (materiales, comida, pergaminos, oro, esmeraldas) |
| **Cofres ocultos** | tras muros agrietados o plataformas de difícil acceso, botín mayor |
| Biblioteca | pergaminos/lore legible (notas) y un cofre de materiales de encantamiento |
| Catacumbas (élite/campeón opcional) | botín mayor y Corazón de campeón |
| Cuatro cámaras de metal | como hoy (llaves del Custodio; cofre por metal) |
| **Relicario** | Vaso de vida por jugador (+1 corazón), se abre solo al caer el Custodio |
| Cripta | como hoy |

Todo sin objetos nuevos de poder; el balance no cambia sin preguntar.

## 6. Ideas extra (para elegir)

1. **Un recorrido «en espiral»**: cada ala termina en un mirador al Nivel 3, así el santuario se ve desde lejos y orienta al jugador.
2. **Cisterna central**: un gran pozo circular con plataformas, escaleras y puentes que conecta todos los niveles (columna vertebral del templo y buena foto).
3. **Alas con tema**: Archivo (conocimiento), Barracones (guerra), Catacumbas (muerte), Taller (autómata), Sala solar (luz). Cada una con 2–4 cuartos y un cofre.
4. **Mini-jefe opcional en las catacumbas**: élite/campeón del sistema `elite/`, sin poder nuevo, con su cofre.
5. **Atajos que se desbloquean**: tras vencer al Autómata o la sala de pruebas se abre una reja (Reja de la Vigilia, interina) que acorta el regreso a la superficie.
6. **Salida rápida tras el Custodio**: un ascensor/escalera que sube a la superficie para no tener que rehacer todo el templo.
7. **Estatuas del «Custodio»** en el camino (sin rostro) que anticipan al jefe (lore: Codex).
8. **Tabla de recorridos**: camino corto (directo al jefe, ~5 min) frente a camino completo (todas las alas, ~25–30 min).

## 7. Riesgos y vigilancias

1. **Rareza y desierto:** el filtro de bioma cambia la frecuencia real; hay que medirla en varias semillas (`/aptitudes locate`) antes de fijar la región.
2. **Escala:** como la fortaleza, necesita el motor de generación por columna; no cabe en el `Feature` actual (§2).
3. **Rendimiento:** presupuesto de mobs por sala y que solo despierten cerca; no generar enemigos al crear el chunk.
4. **Cripta y fluidos:** la cripta sigue condicionada a que haya volumen libre; con un templo grande aumentan las probabilidades de tocar acuíferos o cuevas.
5. **Compatibilidad:** los templos viejos ya generados no se rediseñan; solo los nuevos.
6. **Sin probar en partida:** el diseño hondo actual (V70) tampoco se ha jugado; los resultados de las pruebas pueden cambiar este plano.

## 8. Decisiones del autor (2026-11-26)

| Tema | Decisión |
|---|---|
| Escala | **Grande** (~96×96, hasta ~45 de profundidad; rango 80–128 que concretará Codex) |
| Biomas | **Desierto y badlands** (arenisca y arenisca roja; terracota natural de las mesas como paleta). Pendiente medir la rareza real en varias semillas |
| Alas y cuartos imprescindibles | **Cisterna** (con plataformas y puentes), **catacumbas**, **sala de espejos**, **pozos** de luz, **talleres y forjas**, **barracones**, **cámara de pruebas** (la sala de pruebas actual), **cámara de sombras**, **puentes**, **capilla** y similares (archivo/biblioteca queda como opción de Codex) |
| Mini-jefes | **Opcionales** (élites o campeones del sistema `elite/`, sin poder nuevo), en catacumbas y capilla; botín propio |
| Cámaras de metal | **Juntas junto al Custodio**, como hoy (Nivel 3) |
| Recorrido | **Camino largo en espiral**, con **estatuas** (sin rostro) que anticipan al Custodio y miradores al santuario. El «camino corto» y la «salida rápida» tras el Custodio **no se confirmaron**: quedan como pregunta abierta |
| Motor de generación | **El recomendado**: plano puro por columna, **compartido con la fortaleza** (se construye una sola vez) |

Interpretaciones a confirmar: «sala de empejos» = **sala de espejos** (la «sala de los espejos o la luz» de §3) y «pozos» = **pozos de luz**.

## 9. Pregunta abierta

¿Quieres además un atajo (camino corto) y una salida rápida a la superficie tras vencer al Custodio, o el recorrido es solo el largo en espiral?
