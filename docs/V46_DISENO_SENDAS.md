# v46 — Nivel 50 y Sendas exclusivas (diseño para aprobar)

Estado: **aprobado** por el autor (2026-11-04). **v46.0 implementada** (nivel 50, exclusividad, S4–S6 con
bifurcación, migración, vista de sendas). **v46.1 implementada** (piedras angulares, sonidos e indicador en el
HUD). Ajustes en la v46.1: Tempestad salta también en el 15 % de los golpes y Espectro da +5 % de esquiva (si no,
no funcionarían en las sendas sin crítico o sin esquiva); Cambiaformas B se llama «Metamorfosis Afortunada». Decisiones: nivel máximo de rol
**50**, **sendas exclusivas**, estructura de **7 escalones con bifurcación**, las **10 plantillas** de piedra
angular, el ejemplo del Bersérker como patrón para los 20 roles, **reembolso automático** de la senda con menos
rangos para quien tenga dos y **curva de XP sin cambios**.

## 1. Situación actual (medida en el código)

| Aspecto | Hoy | Dónde |
|---|---|---|
| Nivel máximo de rol | 30 (`maxRoleLevel`) | `AptitudesConfig` |
| Puntos de árbol | 1 + nivel (con `rolePointsEveryLevels = 3`) → **31** en el nivel 30, +10 Maestro, +5 Definitivo = **46** | `RoleRules.earnedPoints` |
| Sendas | 3 por rol (A, B, C) de 3 nodos (niveles 10/14/20, 3 rangos), cada nodo = un `PathEffect`; **se pueden seguir 2** | `RoleTree.PATHS`, `MAX_PATHS = 2` |
| Coste total del árbol | 14 nodos base × 3 + legendaria 1 = 43; 2 sendas = 18; filas de evolución 15 → **76 rangos para 46 puntos** (60 %) | — |
| Evoluciones | Maestro en el nivel 20; Definitivo en el 30 | `Evolution` |
| Curva de XP | `30 × (nivel+1)^1,4` → 45 590 XP hasta el 30 | `RoleRules.xpToNext` |

## 2. Reglas propuestas

| Aspecto | Propuesta | Motivo |
|---|---|---|
| Nivel máximo | **50** (`maxRoleLevel = 50`) | Decisión del autor |
| Curva de XP | **Sin cambios**: el nivel 30 cuesta lo mismo que hoy; del 30 al 50 hacen falta 107 400 XP más (≈ 2,4 veces todo lo anterior) | No alterar la progresión ya probada; el tramo 30–50 es el contenido de final de juego |
| Puntos | La misma fórmula: **51** en el nivel 50 + 15 por evoluciones = **66** | Sin campos nuevos |
| Evoluciones | Maestro 20 y Definitivo 30, **sin cambios** | Ya probado en partida |
| Sendas | **Exclusivas**: el primer rango en una senda la elige y bloquea las otras dos de ese rol (`MAX_PATHS = 1`) | Decisión del autor; identidad de cada build |
| Profundidad | Cada senda pasa de 3 a **7 escalones** (ver §3), con una **bifurcación** interna y una **piedra angular** en el nivel 50 | «Ramificar más el árbol» |
| Coste con la nueva senda | 43 + 19 (senda completa) + 15 = **77 rangos para 66 puntos** (86 %) | Sigue habiendo que elegir, pero el nivel 50 casi completa una build |
| Cambiar de senda | Con `/roles respec` (si `allowSelfRespec`) o el Ritual de renacimiento, igual que hoy | Sin sistema nuevo |

## 3. Estructura de una senda

```
nivel 10  [S1] ─┐                         (existente, 3 rangos)
nivel 14  [S2]  │                         (existente, 3 rangos)
nivel 20  [S3]  │                         (existente, 3 rangos)
nivel 26  [S4]  │                         NUEVO, 3 rangos
nivel 32  [S5x]  o  [S5y]                 NUEVO: bifurcación, se elige UNA (3 rangos)
nivel 40  [S6]                            NUEVO, 3 rangos; exige S5x o S5y
nivel 50  [S7] PIEDRA ANGULAR             NUEVO, 1 rango, efecto único de la senda
```

- **S4, S5x, S5y y S6** son datos: cada uno aplica un `PathEffect` existente (robo de vida, crítico, ejecución,
  espinas, reducción, esquiva, maná, recurso, regeneración, efectos al golpear, esbirros, antiempuje, velocidad,
  vida, velocidad de ataque, suerte), con el valor por rango de siempre (S6 ×1,5, como las filas de Maestro).
- **Bifurcación (S5x / S5y)**: dos variantes de la misma senda; al aprender una, la otra queda bloqueada
  (misma regla que las sendas, un nivel más abajo). Así una misma senda da dos builds.
- **Piedra angular (S7)**: 1 rango, nivel 50. Efecto con mecánica propia elegido de **10 plantillas** con
  parámetros (como las activas de la v23): 60 piedras angulares sin escribir 60 clases.

### Plantillas de piedra angular

| Plantilla | Efecto (valores por defecto) | Encaja con |
|---|---|---|
| CARNICERÍA | Al matar: cura 10 % de la vida máx. y +20 % de velocidad de ataque 5 s | Sendas de robo de vida y ejecución |
| BALUARTE | Al bajar del 30 % de vida: 8 de absorción y Resistencia I 6 s (espera 60 s) | Sendas defensivas |
| SOBRECARGA | Cada 4.ª activa no gasta maná y reduce a la mitad su espera | Sendas de maná y recurso |
| DETONACIÓN | Al matar: explosión de 4 de daño en 3 bloques (no rompe bloques ni daña aliados) | Sendas de daño en área |
| CONTAGIO | Los efectos al golpear (veneno, lentitud, fuego, marchitar) se propagan a 2 enemigos cercanos al morir el objetivo | Sendas de efectos |
| LEGIÓN | +1 esbirro máximo; los esbirros roban el 10 % del daño que hacen | Sendas de esbirros |
| ESPECTRO | Tras esquivar, el siguiente golpe en 3 s es crítico seguro | Sendas de esquiva |
| FORTUNA | +1 tirada de botín de las criaturas y 10 % de esmeralda al matar élites | Sendas de suerte |
| TEMPESTAD | Los críticos lanzan una chispa a un enemigo cercano (3 de daño, espera 1 s) | Sendas de crítico |
| SANTUARIO | Cada 5 s cura 1 corazón a los aliados del grupo a 8 bloques | Sendas de apoyo y regeneración |

Todas respetan los topes globales (reducción de daño ≤ 40 %, `maxDamageMultiplier`), no dañan aliados
(`PartyManager.isAlly`) y sus números van a la configuración.

## 4. Ejemplo completo: Bersérker

| Senda | S1–S3 (hoy) | S4 | S5x / S5y | S6 | Piedra angular |
|---|---|---|---|---|---|
| A — Carnicero | robo de vida, crítico, ejecución | vel. ataque | **Sangría** (robo de vida) / **Verdugo** (ejecución) | crítico ×1,5 | CARNICERÍA |
| B — Coloso | espinas, antiempuje, reducción | vida | **Muralla** (reducción) / **Púas** (espinas) | vida ×1,5 | BALUARTE |
| C — Torbellino | vel. ataque, velocidad, regeneración | esquiva | **Vendaval** (velocidad) / **Furia** (vel. ataque) | regeneración ×1,5 | TEMPESTAD |

Los otros 19 roles siguen el mismo patrón: tabla completa en §10.

## 5. Interfaz (pantalla de roles, J)

- La cuadrícula ya tiene zoom, arrastre y desplazamiento (v19): se amplía hacia abajo.
- Las sendas se alargan en sus columnas (3, 4, 5); la bifurcación ocupa dos medias columnas. La legendaria y
  las filas de evolución bajan de fila (las posiciones son solo de dibujo: no se guardan).
- Al elegir senda, las otras dos se cubren con el velo y el candado de la v23 («Senda bloqueada: ya sigues
  *Carnicero*»). El nombre de la senda elegida aparece como título del rol en la pantalla de roles y en la K.
- Cada senda con su color (borde de nodos y líneas).

### Vista de sendas (aprobada a partir de dos maquetas del autor, 2026-11-04)

Un botón **«Sendas»** en la pantalla de roles abre una vista propia (se desplaza si no cabe en 426×240), que
combina las dos maquetas:

```
┌ [icono] Espadachín · Duelista ──────── [◄][ A ][ B ][ C ][►] ── Nivel de rol 40/50 ┐
│ Nv.10  ● S1  Filo templado (crítico)      │  PIEDRA ANGULAR                        │
│ Nv.14  ● S2  …                            │  Corte Eterno — Carnicería             │
│ Nv.20  ● S3  …                            │  Al matar: cura 10 % y +20 % vel. 5 s │
│ Nv.26  ● S4  …                            │  (valores reales del servidor)         │
│ Nv.32  ● S5x …   ╱╲   ○ S5y … (gris)      │                                        │
│ Nv.40  ● S6  …                            │  «Una senda para quienes nunca se      │
│ Nv.50  🔒 S7 Piedra angular               │   detienen.»                           │
└───────────────────────────────────────────┴────────────────────────────────────────┘
```

| Elemento | De qué maqueta | Cómo se hace en Minecraft |
|---|---|---|
| Pestañas A / B / C con la senda elegida resaltada | ChatGPT | Botones de la GUI; las sendas bloqueadas, con candado |
| Raíl de niveles a la izquierda (Nv. 10 … 50) | ChatGPT | Texto; dorado si ya se alcanzó el nivel |
| Escalones en vertical y bifurcación en dos nodos lado a lado | Ambas | Nodos de 20×20 con el icono de objeto del efecto (`NodeIcons`), no arte pintado |
| Líneas iluminadas en el camino aprendido; la rama no elegida en gris | Gemini | Líneas de color de la senda; gris si está bloqueada |
| Tooltip con el valor real («+15 %») | Gemini | El tooltip actual de los nodos |
| Panel derecho de la piedra angular con parámetros y una frase | ChatGPT | Texto; los parámetros salen de la configuración sincronizada |
| «Nivel de rol 40/50» arriba a la derecha | Gemini | Texto |

Las imágenes de las maquetas son ilustraciones: en el juego la vista usa el estilo actual del mod (paneles
oscuros, bordes dorados, iconos de objetos), sin fondos pintados.

**Más adelante (aprobado por el autor como idea futura):** iconos propios para los 300 nodos nuevos de las
sendas, generados por programa como las texturas de objetos (`tools/textures/`), en lugar de los iconos de
objetos de Minecraft.

## 6. Datos y compatibilidad

| Riesgo | Solución |
|---|---|
| Ids nuevos | `<rol>_a4`, `<rol>_a5x`, `<rol>_a5y`, `<rol>_a6`, `<rol>_a7` (y `_b…`, `_c…`). Los existentes (`_a1…_c3`) no cambian. Se guardan por clave: mundos antiguos válidos |
| **Jugadores con 2 sendas empezadas** | Al entrar por primera vez con la v46: se conserva la senda con más rangos (empate: A, luego B), se **devuelven** los puntos de la otra y se avisa en el chat. Alternativa: un respec gratis de sendas |
| Nivel guardado > nuevo máximo | `setLevel` ya acota al máximo; subir el máximo no rompe nada |
| Activas equipadas | Las sendas no añaden activas: `LoadoutCodec` no cambia |
| Textos | ≈ 600 claves (título y descripción de 300 nodos) en 8 idiomas: generadas con un script como `tools/lang/gen_evolved_lang.py` |
| Cliente/servidor | Validación en el servidor (`RoleManager.upgrade`): senda bloqueada, bifurcación bloqueada, nivel y puntos |

## 7. Código previsto

| Pieza | Tipo |
|---|---|
| `role/PathRules` (puro): elección de senda, bifurcación, escalones y migración | Nuevo, con `role/PathRulesTest` |
| `role/Keystone` + `ability/Keystones`: 10 plantillas con parámetros | Nuevo |
| `RoleTree`: datos de S4–S7 y posiciones | Ampliación |
| `RoleManager.upgrade`: reglas de exclusividad y bifurcación | Ampliación |
| `RolesScreen`: filas nuevas, velo de sendas, título y colores | Ampliación |
| Configuración: `maxRoleLevel = 50` y valores de las piedras angulares | Cambio de un valor por defecto (aprobado) + campos nuevos |
| Grimorio: capítulo de Sendas actualizado | Textos |

## 8. Plan de entrega

1. **v46.0** — Nivel 50, sendas exclusivas, migración, S4–S6 con bifurcación y la interfaz.
2. **v46.1** — Las 10 plantillas de piedra angular y las 60 asignaciones.
3. **v45** (traducciones) después de esto, para no traducir dos veces.

## 9. Decisiones (cerradas)

1. Estructura de 7 escalones con bifurcación: **sí**.
2. Las 10 plantillas de piedra angular: **sí**.
3. El Bersérker como patrón para los 20 roles: **sí**.
4. Migración: **reembolso automático** de la senda con menos rangos, con aviso en el chat.
5. Curva de XP: **sin cambios**.

## 10. Las 60 sendas (propuesta de otra IA, filtrada)

Base: propuesta generada con el prompt de diseño (2026-11-04), revisada aquí. Se conservan los **nombres de
senda que ya existen en el juego** (los jugadores los conocen) y se toman de la propuesta los nombres de los
nodos nuevos, sus efectos y las piedras angulares. Los valores numéricos de las piedras angulares son los de
§3 (configurables), no los de la propuesta.

**Correcciones aplicadas a la propuesta:**

| Problema | Corrección |
|---|---|
| Brujo B: «Pacto de Sangre» (exclusión: pactos) | **Festín Sombrío** |
| Invocador de armas A: Legión, pero el rol no tiene esbirros (sus armas son proyectiles), y el nombre repetía el de S6 | **Tempestad: Tormenta de Filos** (la senda es de crítico y velocidad de ataque) |
| Bersérker C: Sobrecarga | **Tempestad: Tormenta de Hachas**, como en el ejemplo aprobado (§4) |
| Fortuna: «+20 % de botín de criaturas y **cofres**» | Solo criaturas (los cofres se pueden explotar) |
| Cazador A S6 «Instinto Depredador» repetía un nodo del Druida | **Instinto de Acecho** |
| Senda nombres nuevos (Desgarrador, Tejedor de Maná…) | Se mantienen los existentes |

Comprobado por script: 60 sendas, efectos solo de la lista de `PathEffect`, S5x ≠ S5y, tres piedras angulares
distintas en cada rol y ninguna palabra excluida.

| Rol | Senda | Idea | S4 (26) | S5x (32) | S5y (32) | S6 (40, ×1,5 de S4) | Piedra angular (50) |
|---|---|---|---|---|---|---|---|
| Bersérker | A Carnicero | Se fortalece al herir y rematar | Sed Carmesí (robo de vida) | Filo Voraz (crítico) | Remate Brutal (ejecución) | Hambre Roja (robo de vida) | Carnicería: Furia Insaciable |
| Bersérker | B Coloso | Convierte el daño recibido en contraataque | Piel de Hierro (reducción de daño) | Muro Espinoso (espinas) | Paso Inamovible (antiempuje) | Fortaleza Brutal (reducción de daño) | Baluarte: Último Bastión |
| Bersérker | C Torbellino | Presión ofensiva constante | Ritmo Salvaje (vel. ataque) | Carrera Sangrienta (velocidad) | Pulso Vital (regeneración) | Torbellino Salvaje (vel. ataque) | Tempestad: Tormenta de Hachas |
| Mago | A Arcanista | Daño arcano concentrado | Ruptura Arcana (crítico) | Decreto Mortal (ejecución) | Sello Marchito (marchitar) | Colapso Arcano (crítico) | Tempestad: Tormenta Arcana |
| Mago | B Erudito | Combate prolongado con reservas | Fuente Interior (maná) | Flujo Infinito (recurso) | Manto Arcano (reducción de daño) | Manantial Místico (maná) | Sobrecarga: Flujo Perpetuo |
| Mago | C Cronomante | Controla el ritmo y evita golpes | Cadena Gélida (lentitud) | Velo Espectral (esquiva) | Reserva Oculta (maná) | Prisión Temporal (lentitud) | Espectro: Reflejo Arcano |
| Asesino | A Verdugo | Golpes precisos y letales | Marca Mortal (ejecución) | Corte Perfecto (crítico) | Toxina Letal (veneno) | Sentencia Final (ejecución) | Espectro: Muerte Silenciosa |
| Asesino | B Fantasma | Velocidad y debilitamiento | Paso Umbrío (esquiva) | Viento Oscuro (velocidad) | Hoja Entorpecedora (lentitud) | Desplazamiento Sombrío (esquiva) | Detonación: Ruptura Sombría |
| Asesino | C Sombra carmesí | Desgaste y recuperación | Herida Profunda (marchitar) | Sed del Vacío (robo de vida) | Danza Mortal (vel. ataque) | Herida Abismal (marchitar) | Contagio: Peste Silenciosa |
| Herbolista | A Sanador | Recuperación y fortaleza natural | Savia Vital (regeneración) | Corteza Antigua (vida máx.) | Piel Vegetal (reducción de daño) | Savia Eterna (regeneración) | Santuario: Jardín Vital |
| Herbolista | B Envenenador | Venenos y debilitamiento | Extracto Letal (veneno) | Polen Paralizante (lentitud) | Destilado Mortal (ejecución) | Veneno Concentrado (veneno) | Contagio: Floración Tóxica |
| Herbolista | C Alquimista | Recursos y fortuna | Flujo Alquímico (recurso) | Reserva Espiritual (maná) | Recolección Afortunada (suerte) | Flujo Refinado (recurso) | Fortuna: Cosecha Dorada |
| Ingeniero | A Artillero | Precisión y daño concentrado | Calibración Letal (crítico) | Protocolo de Ejecución (ejecución) | Núcleo de Impacto (maná) | Mira Perfeccionada (crítico) | Detonación: Núcleo Explosivo |
| Ingeniero | B Acorazado | Blindaje y estabilidad | Blindaje Reforzado (reducción de daño) | Anclaje Magnético (antiempuje) | Chasis Reforzado (vida máx.) | Blindaje Integral (reducción de daño) | Baluarte: Fortaleza Industrial |
| Ingeniero | C Sobrecargado | Velocidad operativa y energía | Engranaje Rápido (vel. ataque) | Propulsión (velocidad) | Reactor Interno (recurso) | Engranaje Supremo (vel. ataque) | Sobrecarga: Reactor Central |
| Lancero | A Dragón | Movilidad y ataques precisos | Punta Mortal (crítico) | Carga Impetuosa (velocidad) | Punta Entorpecedora (lentitud) | Estocada Perfecta (crítico) | Tempestad: Lanza Relámpago |
| Lancero | B Falange | Resiste y castiga al que se acerca | Púa Defensiva (espinas) | Postura Firme (antiempuje) | Armadura de Guerra (reducción de daño) | Púa de Acero (espinas) | Baluarte: Formación Imbatible |
| Lancero | C Empalador | Recupera vida y remata | Sed del Guerrero (robo de vida) | Coraza Vital (vida máx.) | Estocada Final (ejecución) | Sed del Empalador (robo de vida) | Carnicería: Cosecha de Guerra |
| Espadachín | A Duelista | Duelos, críticos y golpes seguidos | Corte Magistral (crítico) | Danza de Acero (vel. ataque) | Paso del Duelista (esquiva) | Corte Supremo (crítico) | Espectro: Réplica Perfecta |
| Espadachín | B Kensei | Presión, recuperación y remates | Hambre de Acero (robo de vida) | Ejecución Marcial (ejecución) | Hoja Entorpecedora (lentitud) | Hambre de Sangre (robo de vida) | Carnicería: Danza Carmesí |
| Espadachín | C Guardián de acero | Aguanta el desgaste | Guardia de Acero (reducción de daño) | Contraataque Espinoso (espinas) | Vitalidad Marcial (vida máx.) | Guardia Absoluta (reducción de daño) | Baluarte: Juramento de Acero |
| Paladín | A Templario | Golpes sagrados devastadores | Sentencia Divina (ejecución) | Filo Radiante (crítico) | Llama Purificadora (fuego) | Veredicto Celestial (ejecución) | Detonación: Juicio Radiante |
| Paladín | B Guardián | Defiende al grupo y aguanta | Escudo Consagrado (reducción de daño) | Espinas Sagradas (espinas) | Gracia Restauradora (regeneración) | Escudo Divino (reducción de daño) | Baluarte: Fortaleza Consagrada |
| Paladín | C Cruzado | Ataques veloces y apoyo | Golpe Radiante (crítico) | Cadencia Sagrada (vel. ataque) | Fe Inquebrantable (recurso) | Golpe Celestial (crítico) | Santuario: Gracia Compartida |
| Nigromante | A Señor de esbirros | Los esbirros son su fuerza | Legión Marchita (esbirros) | Reserva Funeraria (recurso) | Maná Sepulcral (maná) | Legión Ascendente (esbirros) | Legión: Ejército Sepulcral |
| Nigromante | B Hematomante | Absorbe vitalidad | Drenaje Funesto (robo de vida) | Peste Necrótica (marchitar) | Carne Imperecedera (vida máx.) | Drenaje Abismal (robo de vida) | Contagio: Maldición Funeraria |
| Nigromante | C Plaga | Propaga enfermedades | Peste Venenosa (veneno) | Marcha Entorpecedora (lentitud) | Decreto de Muerte (ejecución) | Peste Eterna (veneno) | Detonación: Sepulcro Profanado |
| Druida | A Guardián salvaje | Fortaleza y regeneración | Raíces Vigorosas (vida máx.) | Corteza Protectora (reducción de daño) | Savia Renovadora (regeneración) | Corazón de Roble (vida máx.) | Santuario: Bosque Protector |
| Druida | B Cazador lunar | Persigue presas con críticos | Instinto Depredador (crítico) | Carrera Feral (velocidad) | Colmillo Tóxico (veneno) | Instinto Primordial (crítico) | Carnicería: Caza Salvaje |
| Druida | C Espino | Controla el espacio | Zarzas Defensivas (espinas) | Raíces Inamovibles (antiempuje) | Polen Entorpecedor (lentitud) | Zarzas Letales (espinas) | Contagio: Zarzal Invasor |
| Mercader | A Tesorero | Optimiza recursos | Capital Arcano (recurso) | Reserva Comercial (maná) | Ojo de Mercader (suerte) | Capital Exponencial (recurso) | Fortuna: Mercado Dorado |
| Mercader | B Mercenario | Críticos y remates oportunistas | Apuesta Mortal (crítico) | Sangre de Riesgo (robo de vida) | Cobro Final (ejecución) | Apuesta Maestra (crítico) | Detonación: Quiebra Explosiva |
| Mercader | C Contrabandista | Evasión y movilidad | Paso Clandestino (esquiva) | Fuga Veloz (velocidad) | Piel de Negociador (reducción de daño) | Paso Invisible (esquiva) | Espectro: Escape Calculado |
| Monje | A Puño de hierro | Críticos y velocidad | Golpe Trascendente (crítico) | Cadencia Interior (vel. ataque) | Paso Ligero (esquiva) | Puño Absoluto (crítico) | Tempestad: Puños del Trueno |
| Monje | B Loto | Concentración y combate largo | Respiración Profunda (regeneración) | Reflejo Sereno (esquiva) | Concentración Interior (maná) | Respiración Eterna (regeneración) | Santuario: Serenidad Compartida |
| Monje | C Puño del dragón | Absorción, impacto y remates | Drenaje Marcial (robo de vida) | Impacto Sísmico (antiempuje) | Golpe Final (ejecución) | Drenaje Absoluto (robo de vida) | Carnicería: Camino del Destructor |
| Elementalista | A Catalizador | Devastación con fuego | Núcleo Ígneo (fuego) | Colapso Elemental (crítico) | Sentencia Abrasadora (ejecución) | Llama Primordial (fuego) | Detonación: Cataclismo Ígneo |
| Elementalista | B Conductor | Sostiene el poder elemental | Corriente Arcana (maná) | Sobrecarga Elemental (recurso) | Manto Energético (reducción de daño) | Corriente Primordial (maná) | Sobrecarga: Corazón Elemental |
| Elementalista | C Tempestad | Control y distancia | Toque Glacial (lentitud) | Paso Boreal (esquiva) | Viento Elemental (velocidad) | Toque Glacial Supremo (lentitud) | Tempestad: Tormenta Cristalina |
| Pistolero | A Pistolero | Disparos precisos | Mira Letal (crítico) | Proyectil Final (ejecución) | Cadencia de Precisión (vel. ataque) | Mira Absoluta (crítico) | Tempestad: Munición Fulminante |
| Pistolero | B Tirador arcano | Energía y presión | Cámara Energética (maná) | Reactor de Munición (recurso) | Desplazamiento Táctico (esquiva) | Cámara Sobrecargada (maná) | Sobrecarga: Arsenal Inagotable |
| Pistolero | C Forajido | Movilidad y desgaste | Disparo Drenante (robo de vida) | Carrera Balística (velocidad) | Proyectil Entorpecedor (lentitud) | Disparo Voraz (robo de vida) | Espectro: Bala Fantasma |
| Invocador de armas | A Espadas danzantes | Presión con armas invocadas | Filo Manifestado (vel. ataque) | Impacto Espectral (crítico) | Drenaje del Arsenal (robo de vida) | Arsenal Desatado (vel. ataque) | Tempestad: Tormenta de Filos |
| Invocador de armas | B Bastión de acero | Armamento protector | Armadura Manifestada (reducción de daño) | Reflejo de Acero (espinas) | Núcleo Defensivo (vida máx.) | Armadura Perpetua (reducción de daño) | Baluarte: Forja Imperturbable |
| Invocador de armas | C Forja rúnica | Recursos y remates | Flujo de Armas (recurso) | Reserva Manifestada (maná) | Ejecutor Espectral (ejecución) | Flujo de Armas Supremo (recurso) | Sobrecarga: Núcleo de Invocación |
| Cazador | A Francotirador | Debilidades y ejecución | Instinto Letal (ejecución) | Disparo Certero (crítico) | Trampa Venenosa (veneno) | Instinto de Acecho (ejecución) | Contagio: Marca del Rastreador |
| Cazador | B Trampero | Movilidad y evasión | Paso Silencioso (esquiva) | Carrera del Viento (velocidad) | Flecha Entorpecedora (lentitud) | Paso del Rastreador (esquiva) | Fortuna: Botín del Explorador |
| Cazador | C Bestia | Vitalidad e impacto | Instinto Sanguinario (robo de vida) | Constitución Salvaje (vida máx.) | Impacto de Caza (antiempuje) | Instinto de Supervivencia (robo de vida) | Carnicería: Trofeo Supremo |
| Bardo | A Juglar | Energía y fortuna | Resonancia Vital (recurso) | Melodía Arcana (maná) | Nota Afortunada (suerte) | Resonancia Superior (recurso) | Santuario: Sinfonía Vital |
| Bardo | B Trovador de guerra | Cadencia y ataques ardientes | Acorde Crítico (crítico) | Ritmo Acelerado (vel. ataque) | Nota Ígnea (fuego) | Acorde Perfecto (crítico) | Tempestad: Sinfonía del Trueno |
| Bardo | C Cantor sagrado | Sostiene al grupo | Melodía Restauradora (regeneración) | Himno Protector (reducción de daño) | Voz Vital (vida máx.) | Melodía Eterna (regeneración) | Baluarte: Última Estrofa |
| Brujo | A Maldito | Maldiciones y ejecución | Marca Marchita (marchitar) | Toxina Oscura (veneno) | Veredicto Sombrío (ejecución) | Marca Abismal (marchitar) | Contagio: Maldición Infinita |
| Brujo | B Sangre maldita | La vitalidad como arma | Drenaje Oscuro (robo de vida) | Carne Impía (vida máx.) | Renovación Umbría (regeneración) | Drenaje Profano (robo de vida) | Carnicería: Festín Sombrío |
| Brujo | C Abismo | Concentra recursos | Golpe del Abismo (crítico) | Reserva Abismal (maná) | Canalización Oscura (recurso) | Golpe del Vacío (crítico) | Sobrecarga: Abismo Inagotable |
| Domador | A Jauría | Primera línea con sus bestias | Piel de Bestia (reducción de daño) | Constitución Feral (vida máx.) | Rugido de Guerra (antiempuje) | Piel Primordial (reducción de daño) | Legión: Manada Suprema |
| Domador | B Salvaje | Ataques coordinados | Instinto Alfa (crítico) | Ataque Coordinado (vel. ataque) | Colmillo Voraz (robo de vida) | Instinto Alfa Supremo (crítico) | Carnicería: Cacería Dominante |
| Domador | C Pastor | Supervivencia y recursos | Vínculo Vital (regeneración) | Instinto Afortunado (suerte) | Reserva Animal (recurso) | Vínculo Eterno (regeneración) | Santuario: Refugio de la Manada |
| Cambiaformas | A Depredador | Persigue y elimina | Instinto Letal (crítico) | Hambre Salvaje (robo de vida) | Carrera Feral (velocidad) | Instinto Supremo (crítico) | Espectro: Reflejo Depredador |
| Cambiaformas | B Mimo | Movilidad y energía | Reflejos Salvajes (esquiva) | Instinto Adaptativo (recurso) | Flujo Metamórfico (maná) | Reflejos Primordiales (esquiva) | Fortuna: Metamorfosis Afortunada |
| Cambiaformas | C Coloso bestial | Absorbe impactos | Constitución Monstruosa (vida máx.) | Piel Acorazada (reducción de daño) | Espinas Naturales (espinas) | Constitución Primordial (vida máx.) | Baluarte: Forma Imparable |

Reparto de plantillas: Carnicería 8, Baluarte 8, Tempestad 8, Sobrecarga 6, Espectro 6, Detonación 6, Contagio 6, Santuario 6, Fortuna 4, Legión 2.

### Ideas extra de la propuesta

| Idea | Decisión |
|---|---|
| Color por tipo de senda (ofensiva, defensiva, arcana, natural) | **Sí** (v46.0, bordes y líneas en la pantalla de roles) |
| Título de especialización junto al rol | **Sí** (ya previsto en §5) |
| Confirmación visual al elegir la bifurcación del nivel 32 | **Sí**, como diálogo de confirmación (la elección es permanente salvo respec) |
| Efecto visual de cada piedra angular | **Sí** (v46.1, con `CastFx`/partículas existentes) |
| Descripción narrativa de cada senda | **Sí**: una línea por senda en el grimorio y el tooltip |
| Resumen de la build (senda, variante, piedra angular) | **Sí**, en el panel lateral de la pantalla de roles |
| Sinergias senda ↔ activas | **Más adelante**: crea un sistema paralelo; se valora tras probar la v46 |
| Emblema que evoluciona en cada escalón | **No por ahora** (60 × 7 emblemas de arte) |

### Tercera propuesta (Quillbot, solo la senda A de cada rol)

Útil como **referencia de valores** para la v46.1 (Santuario 4 % de vida cada 5 s; Tempestad 3 chispas en 3
bloques con el 20 % del daño; Detonación de radio 4 con el 60 % del daño del arma). No se usa para los nodos:
cubre 20 de las 60 sendas, cambia los efectos S1–S3 ya existentes en el juego y repite la exclusión de pactos
(«Pacto de Ceniza», Brujo).

### Segunda propuesta (Gemini), comparada

Se comparó una segunda propuesta generada con el mismo prompt. **No se usa como base**:

| Motivo | Ejemplos |
|---|---|
| Viola la exclusión de pactos | «Pacto de Sangre» y «Pacto Inmortal» (Nigromante), senda «Pacto Arcano» y «Pacto de Maná» (Brujo) |
| Roza la de razas | «Festín del Licántropo» (Asesino) |
| Piedras angulares desequilibradas | Carnicería con +40 % de robo de vida y +50 % de vel. de ataque; Baluarte con 10 corazones de absorción y −40 % de daño durante 10 s |
| Plantillas que no encajan | Legión en el Invocador de armas (sin esbirros); Contagio en una senda del Espadachín sin efectos al golpear |
| Erratas | «Sed Infinitat», «Tocinero», «Encrochador», «Aegis Spectral», «tTécnica»… |

Se adoptan tres de sus ideas extra:

| Idea | Decisión |
|---|---|
| Sonido propio de cada plantilla de piedra angular | **Sí** (v46.1, con `tools/audio/gen_sounds.py`) |
| Indicador en el HUD del estado de la piedra angular (lista / en espera / activa) | **Sí** (v46.1) |
| «Resonancia de grupo»: +15 % si dos miembros del grupo comparten plantilla | **Más adelante**, tras probar el balance |
| Aura de partículas permanente, brillo en el arma desde S6, efecto garantizado tras usar una activa | **No**: coste de rendimiento o sistema paralelo |
