# Pruebas de Aptitudes

## Pruebas automatizadas (nuevas)

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

Ejecución: `.\gradlew.bat test` (también se ejecutan en `.\gradlew.bat build`).

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
