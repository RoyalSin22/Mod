# v47 — Asalto del Eclipse

Primera fase del bloque pedido por el autor: mejorar el Eclipse y parar con informe.
Base: `main` en `1d2f27b`. No cambia la versión pública del mod ni añade dependencias o mixins.

## Comportamiento implementado

- Oleadas por jugador de supervivencia: 4 en Penumbra y 8 en Totalidad; ninguna en Retirada.
  Intervalo, multiplicadores, probabilidades y recompensa conservan sus valores anteriores.
- Refuerzos con al menos una élite por oleada; en Totalidad, también un campeón. Las garantías
  se desactivan si su probabilidad correspondiente es cero y solo cuentan apariciones exitosas.
- Élites y campeones humanoides con armadura completa por defecto: malla/hierro según fase y
  diamante para campeones. El equipo del evento no se suelta. Las probabilidades anteriores siguen
  aplicándose a las demás unidades.
- Los refuerzos buscan al jugador alrededor del que aparecieron, respetando los filtros existentes
  de objetivos; no se aumenta el multiplicador de daño ni se altera el tope del jugador.
- Presupuesto local compartido entre jugadores próximos, pertenezcan o no al mismo grupo;
  límite adicional por zona y por criaturas cargadas del Mundo Normal. Orden rotativo de jugadores
  para que el primer conectado no acapare el presupuesto.
- El Heraldo cuenta en el tope mundial. Si está justo lleno puede sustituir una unidad ordinaria,
  disolviéndola sin XP ni botín. No reemplaza otros Heraldos ni elimina criaturas antiguas cuando
  la carga de chunks ya deja el mundo por encima del presupuesto.
- Solo posiciones válidas, chunks cargados y espacio suficiente para la criatura. El Heraldo sigue
  siendo el jefe del evento; sus encuentros deben corresponder a jugadores próximos, no a miembros
  de un grupo situados en otra región o dimensión.
- Finalización idempotente: detener o finalizar dos veces no vuelve a pagar. Sin pagos a muertos,
  creativo o espectador. Se conserva el registro persistente de muertes durante el evento.

## Validación

Reglas de presupuesto, composición, límites y equipo en una clase pura con JUnit. Validar las
opciones al cargar, conservar los datos anteriores y ejecutar el build real. Las pruebas en partida
se documentan por separado; no se presentan como ejecutadas por el agente.

## Fases posteriores solicitadas

Reforja y Templado, aspecto y menús de bloques, Mesa del gremio, HUD, iconos del árbol, animaciones
y encuentros en estructuras se diseñarán después del informe de esta fase. El autor permite evaluar
una biblioteca adicional para animaciones; aún no se elige ni se añade ninguna.

Un datapack puede aportar datos y recetas cuando el mod disponga de cargadores compatibles. El árbol
actual está definido en Java: hacerlo configurable por datapack requiere validación en el servidor,
migración y sincronización propias. El aspecto de iconos, modelos y animaciones pertenece a recursos
del cliente y código de renderizado; un datapack por sí solo no rediseña la pantalla.
