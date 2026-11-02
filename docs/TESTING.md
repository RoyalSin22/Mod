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
8. Pantalla K: las 9 tarjetas caben sin salirse del panel (escalas de GUI 2, 3 y automática).
9. `legendaryExclusive = true` con 2 jugadores: A aprende la legendaria de su rol → en el árbol de B
   (mismo rol) el botón Mejorar queda inactivo y la ficha dice «La tiene A». A pierde el nodo (reinicio
   del árbol) → B puede aprenderla sin reabrir el juego.
