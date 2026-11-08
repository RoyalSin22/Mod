# v48 — Reforja y Templado

Los talleres usan las hojas y focos de evolución, la Mesa del
gremio, la Hoguera y los materiales que ya entregan élites y campeones.

## Qué hace el jugador

1. En la **Mesa del gremio**, abrir «Reforjar arma». Colocar una hoja o un foco de evolución, elegir
   un afijo y ver el efecto, el coste y la cantidad disponible de cada ingrediente.
2. Confirmar la operación. La misma arma vuelve a quedar en su ranura con el afijo elegido. Se
   puede cambiar de afijo pagando otra Reforja. Si ya tiene ese afijo, la confirmación se deshabilita.
3. En la **Hoguera**, descansar y abrir «Templar arma» desde la pantalla de atributos. Insertar un
   arma reforjada y los trofeos. El menú compara el efecto actual con el siguiente rango.
4. Confirmar el Templado y recoger el arma. El avance pertenece al objeto y se conserva si se cambia
   de afijo. Los dos afijos sirven tanto para hojas como para focos.

## Costes y efectos

| Operación | Coste | Resultado |
|---|---|---|
| Reforjar o cambiar de afijo | 1 Esencia de élite + 8 Fragmentos de alma | Aplica el afijo elegido. La primera Reforja empieza en rango 0 |
| Templar I | 1 Corazón de campeón + 12 Fragmentos de alma | Sube de rango 0 a I |
| Templar II | 2 Corazones de campeón + 16 Fragmentos de alma | Sube de I a II, que es el máximo |

| Afijo | Rango 0 | Templado I | Templado II |
|---|---:|---:|---:|
| Reserva | −2 % de coste de energía | −4 % | −6 % |
| Impulso | +5 % de recurso de rol por impacto válido del arma | +10 % | +15 % |

- **Reserva** se aplica al coste calculado de las activas equipadas, incluida la fila de evolución,
  cuando el arma del mismo rol está en la mano principal. Entra después de Maestría y antes de
  comprobar si alcanza la energía. Si el coste previo es de al menos 1, el descuento tiene un suelo
  de 1. Un coste inferior a 1 conserva su valor previo. Sobrecarga sigue activando su gratuidad
  habitual. El afijo se limita a las activas equipadas y excluye el disparo propio de los focos.
- **Impulso** mejora la ganancia propia de la hoja o del disparo del foco tras un impacto válido
  contra un enemigo de IA. Con la ganancia base de 3, los importes son 3,15 / 3,30 / 3,45 antes de
  aplicar las reglas existentes de recurso. Exige rol activo y daño aceptado, con la comprobación
  de aliados que ya usa el mod. Se calcula sobre el arma del golpe y el tope del recurso se aplica
  por la vía habitual de `RoleResources.gain`.
- Los costes son cantidades fijas. El primer pase de equilibrio mide cuánto
  tardan en conseguirse los trofeos y cuántas activas adicionales permite cada opción, en solitario
  y en grupo. Los campeones del Eclipse dan una vía adicional para conseguir Corazones de campeón.

## Menús y convivencia con el juego

- La cuadrícula 3×3 de la Mesa tiene un botón «Reforjar arma». Se puede cambiar de pantalla con
  la cuadrícula y el cursor vacíos. Los atajos con arma y runa o núcleo siguen siendo parte del uso
  normal del bloque. La Hoguera mantiene su pantalla de atributos y desde ahí ofrece el Templado.
- Los dos talleres comparten un diseño de **arma + dos ranuras de ingredientes**, selector de
  afijo solo en la Mesa y una comparación «Actual → Resultado». Los requisitos y la causa de
  bloqueo se muestran junto a «Confirmar». La vista previa es un dibujo. La ranura de arma
  contiene el único objeto real.
- Los talleres miden 340×238 con el inventario debajo. Comprobar escala 4,
  contraste, orden de foco y ancho de los textos en los once idiomas. La pantalla K conserva
  sus diez tarjetas. El acabado visual usa los modelos y la paleta de los bloques existentes
  como punto de partida para el rediseño posterior de la Mesa y la Hoguera.

```text
┌ Reforjar arma ────────────────────────────────────┐
│ [Arma] [Esencia] [Fragmentos]   Reserva | Impulso │
│ Actual · Afijo Reserva, rango I                   │
│ Resultado · Impulso, rango I (+10 % de recurso)   │
│ Coste · Esencia 1/1 · Fragmentos 8/8  [Confirmar] │
├───────────────────────────────────────────────────┤
│ Inventario y barra rápida                         │
└───────────────────────────────────────────────────┘
```

En Templado, las dos ranuras muestran Corazones y Fragmentos. El selector da paso al rango siguiente.

## Contrato técnico

1. Guardar en el componente `CUSTOM_DATA` del arma un compuesto independiente, por ejemplo
   `aptitudes_forge`, con clave estable del afijo y rango de Templado. Ausencia equivale a arma
   normal. Afijo desconocido, tipo NBT incorrecto o rango fuera de 0–2 se consideran datos inválidos
   y desactivan el efecto. Copiar el compuesto raíz antes de escribir para conservar runas, núcleos
   y otros datos. El arma sigue siendo un objeto único con sus encantamientos, desgaste y nombre.
2. Dar a cada pantalla un `ScreenHandler` con inventario de sesión y contexto del bloque real.
   Abrir la Mesa desde su handler actual. Para la Hoguera, validar el último descanso, la dimensión,
   la distancia y que el bloque siga presente en un chunk cargado. Revalidar esos datos al operar.
   Comprobar jugador vivo en supervivencia o aventura, rol activo y evolución correspondiente al
   arma, tanto al preparar la operación como al confirmarla.
3. El servidor calcula coste, rango, afijo y resultado a partir de sus ranuras. La confirmación
   lleva el identificador de pantalla y una revisión de un solo uso emitida por el servidor.
   Cambiar el arma, las cantidades o la opción invalida la revisión. En el hilo del servidor,
   comprobar la operación entera, preparar las copias resultantes, invalidar la revisión y
   reemplazar arma e ingredientes en sus ranuras en una sola transacción lógica. Cerrar devuelve
   los contenidos reales una sola vez por la vía habitual de inventarios de pantalla.
4. Centralizar las fórmulas en reglas puras. Calcular las bonificaciones desde el arma usada
   y pasar por el cobro de `AbilityManager` o la ganancia de `RoleResources` según el afijo.
   Las mejoras no se convierten en modificadores permanentes del jugador. La recuperación de
   armas desde el árbol entrega el arma base, como hasta ahora. El rango permanece en el objeto
   original si se vende, se guarda en un cofre o cambia de manos.
5. Añadir tooltips y mensajes a los once idiomas con los catálogos del repositorio. Una operación
   rechazada conserva arma y materiales. Las operaciones de taller se pagan con ingredientes y
   no conceden XP de rol, nivel de alma, contratos ni logros.

## Pruebas de aceptación de la implementación

- Reglas puras con JUnit: perfiles, dos rangos, costes exactos, techo de rango, mínimo de coste
  positivo y entradas inválidas. Incluir objetos antiguos y componentes con otros datos.
- Servidor y cliente: cambiar la selección, sustituir el arma, retirar ingredientes y repetir la
  confirmación anterior. Verificar cobro exacto, revisión inválida y ausencia de resultados extra.
  Probar clic rápido, cierre con inventario lleno, muerte, desconexión, modo creativo y rotura o
  sustitución del bloque. Dos jugadores junto al mismo bloque operan con inventarios independientes.
- Probar hojas y focos con rol activo e inactivo, coste cero y Sobrecarga, impactos rechazados y
  aliados. Verificar que encantar, reparar, mover y recargar el arma conservan su afijo conforme
  al comportamiento normal de componentes. Probar que un arma reclamada después de perder la
  anterior nace sin mejoras.
- Probar descanso, Ofrenda, cuadrícula, runas y núcleos por sus entradas actuales. Validar el build
  y los generadores de idioma antes de entregar la rama.
