# Pruebas de Aptitudes

## Pruebas automatizadas (nuevas)

Iconos de Sendas (fase 1; Python 3 + Pillow, sin dependencias Java nuevas):

```sh
python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node --preview build/previews/node-icons-phase1.png
python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node --check
```

En Windows puede usarse `python` si el alias `python3` no está disponible. `--check` es de solo lectura:
exige los 240 PNG RGBA de 16×16 y falla ante archivos faltantes, ilegibles o con tamaño/modo incorrecto.
El generador exige los 20 roles en `paths(...)` y `deeper(...)`, sin duplicados ni efectos desconocidos.
Revisar en la hoja ×4 los colores A/B/C, las muescas opuestas de S5x/S5y y el marco doble dorado de S6
(mismo glifo que S4, con destello adicional). Los recursos aún no están integrados en las pantallas.

Pruebas unitarias de las fórmulas en Java puro, en `src/test/java`. Gradle usa **JUnit Platform** con dos
motores: Jupiter (JUnit 5) y Vintage (JUnit 4), así que se pueden escribir pruebas nuevas con JUnit 5 sin
reescribir las existentes (JUnit 4):

| Clase | Qué comprueba |
|---|---|
| `souls/SoulsMathTest` | Niveles de alma por punto, coste de nivel (creciente y con tope), validación de peticiones de subida de nivel, **regresión del desbordamiento de enteros** (`{MAX, MAX, 50}`) |
| `logic/AttributeScalingTest` | LINEAR = original; SOFTCAP idéntico hasta 40; SATURATION con pendiente inicial ≈ 1 y asíntota; monotonía y nunca por encima del lineal; umbrales inválidos |
| `role/RoleRulesTest` | Puntos de árbol (31 a nivel 30), crecimiento de XP, intervalo 0 sin división por cero, niveles enormes sin desbordamiento |
| `data/ProgressRulesTest` | Curva de XP de habilidades positiva y creciente; configuración extrema limitada al rango de `int` |
| `config/AptitudesConfigValidationTest` | Valores por defecto intactos; negativos, NaN e infinitos vuelven al defecto; límites (probabilidades ≤ 1, divisores y ventanas ≥ 1, nivel máximo ≤ 1000); `attributeScaling` inválido → LINEAR y umbrales ordenados |
| `logic/PlacedBlockLogTest` | Bloque colocado se consume una vez; mundos independientes; tope que olvida el más antiguo; tope inválido ≥ 1 |
| `ability/CooldownBookTest` | Cuenta atrás y caducidad; sobrevive a guardar/cargar; `tryStart`; valores corruptos no bloquean para siempre; tope de entradas |
| `world/TemporaryBlockScheduleTest` | Caducidad por tiempo de juego (también tras reinicio); quitar/consultar; tope por mundo |
| `morph/MorphSizeRulesTest` | Mob pequeño conserva su tamaño; mob enorme acotado; mínimos con valores diminutos o inválidos |
| `logic/ComboCounterTest` | Cadena dentro de la ventana y caducidad; remate con umbral y espera; bonificación con tope; contador acotado |
| `role/LoadoutCodecTest` | Formato antiguo de 2 bits leído igual; formato nuevo con 8 activas; índices fuera de rango |
| `role/EvolutionPointsTest` | Puntos extra por evolución (+10/+5), constructor antiguo sin extra, negativos ignorados, sin desbordamiento |
| `logic/XpDiminishingTest` | Multiplicador (completo hasta el umbral, decrece hasta el suelo), redondeo, ventana deslizante por jugador y fuente, granja frente a juego normal, memoria acotada |
| `party/PartyRulesTest` | Cuota de XP en pasos válidos, aritmética acotada, bonificación por integrantes conectados y valores corruptos |
| `config/PartyConfigValidationTest` | Valores predeterminados y límites seguros de los cuatro campos de grupo |
| `contract/ContractRulesTest` | Ofertas fijas por jugador y día, oferta destacada, límites por reputación y evolución, tipos por dificultad, objetivos alcanzables y entregas completas |
| `achievement/AchievementCatalogTest` | Árbol de logros (ids únicos, padres declarados), JSON de avances coherentes con el enum y sin sobrantes, traducciones en los 8 idiomas |
| `event/SoulFogRulesTest` | Pesos que suman 100 por clima, clima por agua y temperatura, distribución exacta de las tiradas, variantes por clima y tiradas fuera de rango |
| `event/StrongEventRulesTest` | Pesos del Eclipse por fase y del Nether por tamaño (suman 100 y siguen las tiradas), bonificación y armadura acotadas, material por fase, tamaños crecientes en potencia/caída/botín/restos, criaturas por tamaño y probabilidad |
| `role/PathRulesTest` | Ids de los escalones, niveles aprobados y multiplicador de S6, exclusividad de sendas, bifurcación (una rama; S6 acepta cualquiera), migración (conserva la de más rangos; empate A) y rangos corruptos |
| `role/KeystoneRulesTest` | Tabla de piedras angulares (3 distintas por rol, Legión solo con esbirros), Sobrecarga cada 4.ª, Baluarte (umbral y espera), esperas acotadas y progreso, valores acotados |
| `config/ConfigMigrationTest` | El nivel máximo antiguo (30) pasa a 50 una sola vez; los valores elegidos a mano se respetan |

Ejecución: `.\gradlew.bat test` (también se ejecutan en `.\gradlew.bat build`).

### Prueba en partida (autor, 2026-11-03)

`main` en `6c709f9` compilado y probado en partida por el autor: **v15–v19 y v29–v41 ✔ según lo esperado**
(guías `docs/PLAYTEST_v15-v19.md` y `docs/PLAYTEST_v29-v41.md`). Defecto encontrado: la pantalla de ajustes
del HUD solapa el texto de escala con el primer botón y la ayuda con Restablecer/Hecho.

### Resultado real en este bloque

En el entorno de desarrollo (sin acceso a los repositorios de Minecraft/Fabric) se compilaron las
clases probadas y las pruebas con el JDK 21 y JUnit 4.13.2 local, y se ejecutaron con `JUnitCore`:

```
JUnit version 4.13.2
OK (17 tests)
```

La primera ejecución dio 2 fallos que eran **errores de las propias pruebas** (expectativas mal
calculadas), no del código; se corrigieron y se documentan en el informe del bloque.

**No verificado aquí:** `gradlew build`/`test` completos (requiere descargar Minecraft y Fabric).
`RoleRules`/`ProgressRules` se compilaron localmente con un *stub* mínimo de `AptitudesConfig`
(fuera del repositorio); en Gradle se usa la clase real.

## Comprobaciones estáticas usadas en cada cambio

- `javac` de todo el código (sin las librerías de Minecraft): detecta errores de sintaxis y de tipos
  que no dependen de Minecraft. **No** detecta errores de API de Minecraft ni `switch` incompletos.
- Script de imports: busca clases usadas sin importar.
- Validación JSON de todos los recursos y comprobación de que toda clave de traducción usada en Java
  existe en `es_es` y `en_us`, y de que no hay `%` inválidos.

## Pruebas manuales recomendadas (no automatizables aquí)

Protocolo detallado, con comandos y resultados esperados para la v15–v19: [PLAYTEST_v15-v19.md](PLAYTEST_v15-v19.md).

1. Servidor dedicado + 2 clientes: subir de nivel en la Hoguera, desconectar y reconectar → atributos
   iguales y sin modificadores duplicados (`/attribute @s minecraft:generic.max_health get`).
2. Morir y reaparecer: roles, árbol, accesorios y almas se conservan.
3. `legendaryExclusive = true`: dos jugadores intentan aprender la misma legendaria → solo uno.
4. `attributeScaling = SOFTCAP`: con más de 40 puntos, la pantalla y el efecto real crecen más despacio.
5. Romper bloques con niveles altos de Minería: sin bloques fantasma.
6. Pesca: pescar peces sube Pesca (toast de XP); la basura no. `/aptitudes admin` para subir a 50 y
   comprobar Gracia del delfín al nadar, Poder del conducto en el agua y tesoros.
7. Rendimiento decreciente: matar más de 20 mobs del mismo tipo en un minuto → el toast de XP baja;
   cambiar de tipo de mob → XP completa.
8. Pantalla K: las 10 tarjetas (5×2) caben sin salirse del panel (escalas de GUI 2, 3 y automática).
9. `legendaryExclusive = true` con 2 jugadores: A aprende la legendaria de su rol → en el árbol de B
   (mismo rol) el botón Mejorar queda inactivo y la ficha dice «La tiene A». A pierde el nodo (reinicio
   del árbol) → B puede aprenderla sin reabrir el juego.
10. Colocar un tronco y romperlo → sin toast de XP de Tala; talar un árbol natural → XP normal.
    Plantar un brote, dejar que crezca y talarlo → XP normal (el tronco de la base no cuenta como colocado).
11. Colocar redstone sobre un bloque donde no se puede (p. ej., en el aire con clic en agua) → sin XP de
    Ingeniero; colocarla bien → XP. Colocar y romper la misma pieza 40 veces en un minuto → la XP baja.
12. Lanzar la definitiva, salir y volver a entrar → la espera sigue (HUD incluido). Repetir cerrando el
    servidor: la espera continúa donde estaba.
13. Levantar el Muro elemental y cerrar el servidor antes de 10 s → al volver, el muro desaparece en los
    segundos que le quedaban. Picar un bloque del muro → no suelta nada ni da XP.
16. Árbol v23: un rol base muestra al final del árbol las filas «Maestro» y «Definitivo» bloqueadas (velo,
    candado, ficha «Bloqueado…»). `/roles admin settier J <rol> 1` → la fila Maestro brilla y se puede aprender
    (2 rangos, puntos +10). Equipa una activa nueva (índice 5-8) y úsala; sal y entra: sigue equipada.
    `/roles admin settier J <rol> 0` → los nodos evolucionados vuelven a 0.
17. Mundo guardado antes de la v23 con activas equipadas en V/B → siguen siendo las mismas tras actualizar.
18. Animaciones v24: lanza una activa de cada categoría (Bersérker, Mago, Asesino, Paladín, Ingeniero) →
    gesto del brazo, sello que gira bajo los pies y remate propio con sonido; otro jugador cerca también lo ve.
    En el HUD la ranura destella y el icono rebota. En el árbol, aprender un rango hace latir el nodo.
19. Colisión v26: transfórmate en murciélago o pollo → cabes por un hueco de 1 bloque de alto y la cámara
    baja; F3+B muestra una caja pequeña. Transfórmate en un mob grande → caja de 1,2 de ancho como máximo.
    Otro jugador ve la caja correcta. `logs/latest.log` no debe tener el aviso «La colisión de las
    transformaciones no se aplicó».
20. Mesa del gremio v27: en la mesa de trabajo, el patrón de una pieza de armadura de rol no da nada.
    Clic derecho en la Mesa del gremio → cuadrícula 3×3; el mismo patrón da la pieza; mayús+clic fabrica
    varias; al cerrar, los ingredientes vuelven al inventario. Un aldeano sigue tomándola como bloque de
    trabajo. En el grimorio, la receta dice «Mesa del gremio».
21. Atributos v27 (con un `config/aptitudes.json` nuevo): 10 puntos en Vigor dan +7 de vida (antes +10).
22. Combos v28: golpea zombis seguidos → «×2…×5» junto a la mira y más daño; el 6.º golpe hace el remate de tu
    categoría y aparece «¡REMATE!». Espera 3 s → el contador desaparece. Recibe un golpe fuerte → se rompe.
    Contra otro jugador no cuenta. Con el Asesino, el recurso Combo sube el doble.
14. Escala de interfaz 4 (o ventana pequeña): abrir la pantalla de roles (J) → aparece una barra a la
    derecha; rueda fuera del árbol o RePág/AvPág hasta ver «Hecho»; el zoom del árbol no cambia.
15. En el Nether, sobornar a un piglin (Soborno) → deja de atacar en menos de medio segundo. Con el
    camuflaje del Cambiaformas, los piglins brutos y hoglins no te persiguen.

# v32 — runas de armas (verificación manual)

1. Fabrica las cuatro runas en la Mesa del gremio; verifica que no se fabriquen en mesa de trabajo.
2. Sostén un arma de rol y una runa en manos opuestas, usa la Mesa del gremio y confirma consumo y mensaje de ranura.
3. Engarza una segunda runa diferente; una tercera y un duplicado deben rechazarse. Cierra/abre el mundo y verifica persistencia.
4. Prueba Fuego, Robo de vida, Aceleración y Fuerza; comprueba magnitudes y enfriamiento indicados en `docs/BALANCE.md`.
5. Confirma que una espada vanilla no admite runas.

# v33 — jefes propios (verificación manual)

1. En una torre nueva, llega a la azotea y derrota al Centinela; revisa barra, nombre y su núcleo.
2. En un santuario de Hoguera nuevo, lucha con el Oráculo. En cada tipo de campamento nuevo, aparece el Campeón con la variante correspondiente.
3. Reduce la vida de un jefe por debajo de 2/3 y 1/3: cada fase debe activarse una sola vez, incluso tras salir y volver a cargar el chunk.
4. En fase final, observa el aviso/partículas antes del golpe de área; el Oráculo aplica lentitud. Comprueba alcance y tiempos de 3–4 s.
5. En la Mesa del gremio, aplica cada núcleo una vez a una hoja o foco de evolución: consume el material y sube +0,5 daño. Duplicados y núcleos adicionales se rechazan; bonificación máxima +1,5.
6. Reinicia el mundo y confirma que no reaparecen jefes ya vencidos y que el progreso/núcleos de armas persiste.

# v34 — el Eclipse (verificación manual)

1. `/aptitudes eclipse` muestra los días hasta el próximo. `/aptitudes eclipse start` (operador): mensaje,
   sonido, el cielo enrojece poco a poco y aparece la barra «Eclipse · Penumbra».
2. A los ~2,5 min (Totalidad): sol con anillo carmesí, ceniza, relámpagos sin daño y oleadas cada 30 s. Las
   criaturas llevan casco oscuro y no arden. No más de 12 vivas alrededor de cada jugador.
3. A mitad del evento aparece el Heraldo del Eclipse con barra de jefe. Con dos jugadores juntos, un solo
   Heraldo con más vida; separados (>64 bloques), uno cada uno. Suelta el Núcleo del eclipse y fragmentos.
4. Fabrica la Runa del Eclipse en la Mesa del gremio y engárzala: más daño contra élites (no contra criaturas normales).
5. Muere durante el evento: al terminar no recibes la recompensa de superviviente; otro jugador que no murió sí.
6. `/aptitudes eclipse stop`: las criaturas del eclipse se disuelven, el cielo vuelve a la normalidad, sin recompensas.
7. Eclipse natural: con `/time set` ponte en el día 9 al atardecer (18:00): aviso con campana. Al amanecer del
   día 10 empieza solo. Reinicia el servidor a mitad: el evento continúa y las criaturas cargadas siguen.
8. En el Nether durante un eclipse: sin cielo rojo ni HUD; al volver, el estado se restablece.
9. Mayús + O → «Cielo del Eclipse» desactivado: cielo normal, pero viñeta y ceniza visibles.
10. Dificultad Pacífica: hay cielo y avisos, pero no aparecen criaturas ni Heraldo.

# v35 — Runas+ (verificación manual)

1. Fabrica Escarcha, Trueno y Guardia en la Mesa del gremio; no se fabrican en la mesa de trabajo.
2. Escarcha: el objetivo queda con Lentitud II 3 s y salen copos de nieve.
3. Trueno: contra un grupo de zombis, a veces salta una chispa a 2 cercanos (3 de daño). Nunca a otro jugador,
   a tu lobo ni a tus esbirros. No se encadena sin fin.
4. Guardia: corazones dorados al golpear, hasta 6 de absorción; una manzana de oro no se reduce.
5. Robo de vida: el arma ya no «parpadea» en la mano al curar.
6. Quitar runa: Mayús + clic derecho en la Mesa del gremio con el arma y la otra mano vacía. Sin esmeraldas →
   aviso; con 4 → se quita la última, sonido de afilador y mensaje. Con Fuerza o Aceleración, el daño o la
   velocidad del tooltip vuelven al valor sin runa. El foco no dispara al hacerlo.
7. Sin agacharse, la Mesa sigue abriendo la fabricación y engarzando runas como antes.
8. Cambia `runeThunderChance` a 1.0 y `/aptitudes admin reload`: la descarga sale en cada golpe (con su espera).

# v36 — anti-granjas persistente (verificación manual)

1. Coloca piedra, rómpela: sin XP de Minería (como antes).
2. Coloca piedra, guarda y sal del mundo, vuelve a entrar y rómpela: **sigue sin dar XP** (antes sí daba).
3. Reinicia el servidor dedicado y repite: tampoco da XP.
4. Con dos partidas distintas en el mismo cliente: un bloque colocado en la primera no afecta a la misma
   posición en la segunda.

# v37 — sonidos propios (verificación manual)

1. Activa los subtítulos (Opciones → Accesibilidad). `/aptitudes eclipse start`: suena «El sol se apaga»;
   en la totalidad, «Totalidad del Eclipse»; a mediodía, «Llega el Heraldo».
2. Atardecer del día anterior a un eclipse: «Campana lejana del Eclipse».
3. Remate de combo: el golpe común suena junto al sonido de la categoría.
4. Engarzar una runa: campanilla. Quitarla (Mayús + clic en la Mesa): raspado y chasquido.
5. Con el volumen de «Criaturas hostiles» a 0 no se oyen los sonidos del Eclipse (categoría Hostil).

# v39 — interfaz con escala grande (verificación manual)

1. Opciones → Gráficos → Escala de interfaz **4** con ventana 1920×1080, y **3** con ventana 1280×720.
2. Pantalla K (tecla K): si no cabe, aparece una barra dorada a la derecha; la rueda del ratón mueve la
   ventana y los botones Confirmar/Reiniciar siguen funcionando en su sitio. Con escala 2 no hay barra.
3. Pantalla de roles, grimorio, Mesa del gremio y ajustes del HUD (Mayús + O): nada se sale ni se solapa.
4. Durante un Eclipse, engarza una runa: el aviso de la barra de acción ya no queda tapado por la barra del Eclipse.

# v40 — pantalla de configuración (verificación manual)

1. Mundo de un jugador (con trucos): `/aptitudes config` → «Servidor · puedes editar». Busca `eclipse`.
2. Cambia `eclipseIntervalDays` a 5 (✔): mensaje «Configuración: eclipseIntervalDays = 5»; `config/aptitudes.json`
   lo guarda; `/aptitudes eclipse` cuenta con el nuevo intervalo.
3. Escribe 9999 en `eclipseMaxMobsPerPlayer`: se aplica 40 (el tope). Escribe «abc» en un número: aviso en rojo.
4. Botón de un booleano (`combosEnabled`): cambia Sí/No al momento.
5. Servidor con un jugador no operador: la pantalla está en solo lectura y los botones desactivados.
6. Cambia `runeThunderChance` a 0.5: el tooltip de un arma con Trueno muestra «50 %».
7. Pestaña ⚙ en la pantalla K abre la configuración; «Hecho» vuelve a la pantalla K.
8. Con Mod Menu instalado: Mods → Aptitudes → Configurar abre la misma pantalla. Sin Mod Menu, el juego arranca igual.

# v41 — eventos rotativos (verificación manual)

1. `/aptitudes event`: «Próximo evento: X, dentro de N días».
2. `/aptitudes event start soul_fog`: cielo gris azulado, almas y ceniza blanca, barra «Niebla de almas»;
   oleadas pequeñas de no muertos a pie (zombis, esqueletos y sus variantes; sin fantasmas) que sueltan Fragmentos de alma. `/aptitudes event stop`: se disuelven.
3. `/aptitudes event start ember_rain`: cielo anaranjado; cada ~25 s una estela de fuego cae cerca y explota
   sin romper bloques, dejando lingotes. Recibir el impacto hace daño.
4. `/aptitudes event start eclipse`: igual que antes (sol con anillo rojo).
5. Con un evento en curso, `start` de otro → «Ya hay un evento en curso».
6. Pon `worldEventWeightEmberRain = 0` en la configuración (v40): `/aptitudes event` nunca anuncia la Lluvia de brasas.
7. Calendario natural: ve al día 9 al atardecer (`/time set`): la campana anuncia el evento elegido y al día
   siguiente empieza ese (la Niebla a las 18:00).
8. Mundo con Eclipses anteriores (v34–v40): carga sin errores y el calendario continúa.

# v29 — grupos (verificación manual)

1. Con dos clientes, pulsa `P`, invita desde la lista y comprueba que el segundo recibe botones clicables Aceptar y
   Rechazar. Rechaza una invitación; repite y acepta. Confirma que caduca tras 60 s.
2. Forma un grupo de cinco; una sexta invitación debe rechazarse. Comprueba el líder marcado, salir y expulsar.
   Reinicia el servidor y verifica que el grupo y sus opciones siguen guardados.
3. Con el líder a 25 % y luego 50 % de XP compartida, mata criaturas cuerpo a cuerpo y con arco. Los miembros
   conectados en otras dimensiones reciben XP de Combate/Tiro con arco y del rol de combate; el atacante conserva
   su XP. Comprueba que `xpDiminishing` se aplica antes del reparto y que la XP recibida no se reparte otra vez.
4. En un grupo de cinco conectados, comprueba el máximo de +20 %; desconecta integrantes y confirma que el extra
   disminuye en pasos de 5 %. Cambia los campos `party*` desde la configuración del servidor y comprueba los límites.
5. Con fuego amigo desactivado, golpea a un compañero con ataque, arco, habilidad y runa. No debe recibir daño;
   activa `/party pvp` y repite. Prueba también las invocaciones del grupo.
6. Lanza curas y auras con y sin grupo: con grupo no deben afectar a jugadores ajenos; sin grupo deben conservar
   el comportamiento anterior. Verifica el HUD en la misma dimensión, en otra dimensión y tras desconectar.
7. En Eclipse, confirma un Heraldo por grupo y escalado de vida hasta cinco integrantes; sin grupo, los jugadores
   a menos de 64 bloques siguen compartiendo un Heraldo. En Niebla de almas comprueba oleadas y tope según grupo.

# v46.1 — Piedras angulares (verificación manual)

Preparación: `/roles admin setlevel J <rol> 50`, aprende la senda hasta S6 y la piedra angular S7 en la vista de
sendas. Activa los subtítulos para oír cada sonido.

1. Encima de la barra de habilidades aparece el icono de la plantilla (borde morado).
2. Carnicería: al matar, corazones y Prisa II. Detonación: al matar junto a un grupo, los cercanos reciben daño;
   no se encadena sin fin y no daña a compañeros de grupo ni rompe bloques.
3. Baluarte: baja del 30 % de vida → absorción y Resistencia; el icono se oscurece 60 s.
4. Sobrecarga: la 4.ª habilidad no gasta maná y su espera es la mitad; el icono late en dorado antes de ella.
5. Contagio (Herbolista B): envenena a un zombi y mátalo junto a otros: los cercanos quedan envenenados.
6. Legión (Nigromante A): 5 esbirros a la vez; al golpear, curan al jugador.
7. Espectro: tras una esquiva (nube), el icono late; el siguiente golpe es crítico.
8. Fortuna: matar criaturas suelta más botín de media; las élites a veces sueltan esmeralda.
9. Tempestad: chispas a enemigos cercanos al hacer críticos (y a veces sin crítico).
10. Santuario: en grupo, cada 5 s te curas tú y los compañeros cercanos.
11. `/aptitudes config`: cambia un campo `keystone*` (p. ej., `keystoneOverloadEvery = 2`) y comprueba el efecto.

# v46.0 — Nivel 50 y Sendas exclusivas (verificación manual)

1. Arranque con un `config/aptitudes.json` antiguo (con `"maxRoleLevel": 30` y sin `configRevision`): al cargar
   pasa a 50 y aparece `"configRevision": 46`. Con `"maxRoleLevel": 40` se queda en 40.
2. Mundo anterior con un rol que tenga dos sendas empezadas: al entrar, mensaje dorado «Sendas exclusivas: en
   … conservas la senda … y recuperas N puntos»; la senda con más rangos sigue aprendida y los puntos vuelven.
3. Pantalla de roles (J): botón «Sendas» abajo. En la vista: pestañas A/B/C, raíl de niveles (dorado los
   alcanzados), escalones S1–S6 con la bifurcación lado a lado y la piedra angular con candado.
4. Sin senda elegida: al aprender S1 de A aparece la confirmación; al aceptar, B y C se ven bloqueadas (velo y
   candado) en la vista y en el árbol. Intentarlo por comando (`/roles learn <rol>_b1`) da «Ya sigues otra senda».
5. `/roles admin setlevel J <rol> 50`: aprende S4; en la bifurcación elige S5x (confirmación); S5y queda
   bloqueada («Ya elegiste la otra rama»). S6 se puede aprender con S5x **o** S5y.
6. Tooltip de S6: valor ×1,5 del de S4 (p. ej., Bersérker A: robo de vida +3 % por rango). Comprueba en combate
   que el efecto suma (más curación al golpear).
7. Reasignar el árbol (`/roles respec <rol>` si está permitido) libera la senda y la rama.
8. Escala de interfaz 3 y 4: la vista de sendas cabe o se desplaza con la rueda; nada se solapa.

# v41.2 — Eclipse y Lluvia de brasas más fuertes (verificación manual)

1. `/aptitudes event start eclipse` en supervivencia. En la Penumbra: zombis y esqueletos con algunas piezas de
   malla (y el casco oscuro); ninguna se suelta al matarlos.
2. `/time set 3000` durante el evento (Totalidad): piezas de hierro, esqueletos wither en las oleadas y criaturas
   que aguantan más golpes y apenas retroceden. Un campeón con piezas de diamante.
3. `/aptitudes event start ember_rain`: la mayoría de meteoritos son como antes; algunos tienen estela más gruesa,
   bramido de aviso y una explosión mayor que no rompe bloques. Alrededor quedan bloques de magma que
   desaparecen a los 30 s sin dejar agujeros. Más lingotes en el suelo.
4. Tras los impactos aparecen cubos de magma y blazes; en los grandes, piglins brutos y hoglins que **no** se
   convierten en zombis pasados 15 s; en los enormes, esqueletos wither. `/aptitudes event stop`: se disuelven.
5. `/aptitudes config`: `eclipseMobArmor = 0`, `eclipseArmorChance = 0`, `eclipseTotalityBonus = 0`,
   `emberRainLargeChance = 0`, `emberRainHugeChance = 0` y `emberRainMobChance = 0` → comportamiento de la v41.1.

# v41.1 — HUD y Niebla de no muertos (verificación manual)

1. Mayús + O con escala de interfaz 2, 3 y 4: el título, «Escala: N %» (entre − y +), los 6 botones, la ayuda y
   Restablecer/Hecho no se solapan.
2. `/aptitudes event start soul_fog` en una llanura: zombis, zombis aldeanos y esqueletos; ningún fantasma.
3. Repite junto a un río o en la playa (ahogados y esqueletos pantanosos), en un desierto (momias) y en una
   llanura nevada (errantes).

# v31 — logros (verificación manual)

Prueba automática: `achievement/AchievementCatalogTest` (enum, JSON y traducciones coherentes).

1. Entra en un mundo nuevo y abre los avances (L): aparece la pestaña «Aptitudes» con el grimorio de icono y los
   17 logros bloqueados (en gris los que dependen de otro).
2. Descansa en una Hoguera, elige un rol, haz un remate de combo y engarza una runa: un aviso por cada uno, y su
   mensaje en el chat.
3. `/aptitudes event start soul_fog` y espera al final: «A través de la bruma». Si lo detienes con `stop`, no cuenta.
4. Forma un grupo: el logro lo reciben quien invita y quien acepta.
5. Mundo anterior a la v31 con un rol en Maestro y un contrato cobrado: al entrar se conceden «Vocación», «Maestro
   del oficio» y «Firmado y sellado» sin hacer nada más.
6. Con un datapack que borre un avance de la pestaña, el juego no falla (el logro simplemente no se concede).

# v30 — contratos del gremio (verificación manual)

1. Fabrica un Tablón (3 papel arriba, 3 tablones en medio y 1 tablón abajo en el centro) y úsalo: 3 ofertas con
   objetivo y recompensa legibles; cerrar y abrir no las cambia.
2. Acepta una de Caza y mata a la criatura indicada: progreso en la barra; al completarla, aviso «¡Contrato
   completado!». Cóbrala: esmeraldas, XP de rol y reputación. Esa oferta ya no se puede volver a aceptar hoy.
3. Entrega sin llevar suficiente: aviso y **no** se quita nada. Con la cantidad completa: se entrega.
4. Pasa al día siguiente (`/time add 24000`): ofertas nuevas; el contrato que tenías en curso sigue igual.
5. En grupo: la Caza de un miembro avanza el contrato del otro (conectado).
6. Con el rol en Maestro: aparecen ofertas Élite; en Definitivo, Legendarias (Jefe o Evento).
7. `/aptitudes event start soul_fog` y espera al final (o detenlo con `stop`: no cuenta); un contrato de Evento
   de Niebla avanza al terminar el evento con éxito.
