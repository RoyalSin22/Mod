# Aptitudes — rediseño del árbol y próximas fases

## Referencia del árbol

La captura del Domador mezcla tres recuadros grandes, conexiones en ángulo recto y dos bandas de evolución.
La propuesta usa un lienzo de pizarra con rutas más separadas, una ficha para el nodo seleccionado y
pestañas para ver una parte del árbol a la vez. La división en pestañas es visual. El diseño debe seguir
mostrando el requisito de cualquier nodo aunque esté en otra pestaña.

```text
Tipo: ui-mockup. Crea una maqueta frontal de alta fidelidad para el árbol de habilidades de Aptitudes, un mod RPG de Minecraft. Usa la captura adjunta como referencia del sistema actual y rediseña por completo la distribución visual. La idea es un atlas de talentos tallado en pizarra, con píxel nítido, latón envejecido, jade apagado y destellos de ámbar. Ventana horizontal de proporción aproximada 426×240, centrada sobre un mundo Minecraft muy desenfocado y oscurecido. Barra superior con sello del Domador, nivel y puntos disponibles. A la izquierda, selector estrecho de roles con iconos de píxel y una fila seleccionada. En el centro, un lienzo oscuro único con el sello del rol arriba y ramas escalonadas hacia abajo, conexiones finas que llegan al borde de cada nodo, separación generosa, tres sendas que se distinguen por forma y color. Muestra una bifurcación de elección exclusiva y su piedra angular final. A la derecha, una ficha pequeña del nodo seleccionado con rango, requisito y un botón de acción. En un borde del lienzo, pestañas discretas «Talentos», «Sendas», «Maestro» y «Definitivo». Distingue aprendido, disponible y bloqueado por silueta, borde y brillo. Haz que la tipografía pixelada y los controles se lean a escala 4. Usa muy poco texto, sin párrafos ni etiquetas superpuestas. Evita grandes recuadros de color, ornamentación que compita con los nodos, cursivas, logotipos y marcas de agua. Resultado: una sola pantalla de videojuego plausible como referencia visual, no una ilustración ni un póster.
```

Al trasladar la maqueta al mod, conserva los sellos de v50 y los iconos de Sendas. Verifica el foco,
la navegación con zoom y ratón, los textos de los once idiomas y la legibilidad en 426×240. Los estados
aprendido, disponible y bloqueado se distinguen por forma y borde además del color.

## Eclipse — propuesta de asedio

1. **Persecución (implementada en v52).** Las unidades marcadas por el evento revisan su objetivo a intervalos cortos
   y escalonados. Siguen al jugador elegible si lo ven y buscan durante un tiempo breve en su última
   posición conocida si lo pierden. Cuando cambia de dimensión, muere o entra en creativo/espectador,
   descartan ese objetivo. Respeta el filtro común de Soborno y camuflaje, el alcance del encuentro
   y los límites de entidades. Las criaturas no cargan chunks para perseguir.
2. **Escalada.** Si una unidad de asedio permanece atascada en Totalidad, puede intentar una subida
   corta con bloques temporales de `TemporaryBlocks`. Se propone un límite por encuentro, una espera
   entre intentos y desaparición al finalizar o caducar. El servidor comprueba el espacio libre,
   la colisión, el suelo de apoyo, los chunks cargados y `mobGriefing`.
3. **Rotura.** Como ajuste opcional, una unidad de asedio puede abrir paso en un bloque de una lista
   de barreras débiles. El valor seguro deja la rotura desactivada. El servidor valida el permiso de
   modificación, excluye contenedores y puestos, evita roturas en cadena y aplica un presupuesto.
   Si el servidor no puede verificar la protección de una zona, esa unidad se limita a buscar otra ruta.
4. **Validación.** Prueba paredes, desniveles, agua, bordes de chunks, dos jugadores juntos y separados,
   muerte, desconexión, reinicio y final del Eclipse. Verifica que los trabajos de asedio caduquen y
   que el comportamiento no cree premios ni objetos adicionales.

## Árbol ampliable y revisión de roles

1. Define un datapack piloto que extienda un rol existente con una rama opcional y una plantilla de
   habilidad ya implementada. Usa claves con espacio de nombres para los nodos añadidos. Valida
   referencias, ciclos, tamaños y valores antes de activar el catálogo. Si falla la recarga, conserva
   el catálogo válido anterior e informa del motivo.
2. Sincroniza el catálogo y su revisión desde el servidor. El servidor sigue comprobando requisitos,
   compra, coste, objetivos y enfriamiento. La primera fase conserva el orden de las ranuras de activas
   y las claves guardadas. Ampliar las ranuras requiere un formato de guardado con versión.
3. Revisa primero los roles que compiten por la misma función. Compara en solitario y en grupo cuándo
   conviene equipar cada activa. Ajusta primero sus decisiones de juego. Si se decide retirar un rol,
   define antes cómo conservar o reasignar los puntos, las habilidades equipadas y las armas existentes.

La escalada, la rotura, el catálogo por datapack y la revisión de roles quedan propuestos.
