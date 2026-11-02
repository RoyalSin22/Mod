# v23 — El árbol crece al evolucionar

Estado: **implementado** (aprobado por el autor con dos cambios: filas **visibles pero bloqueadas** antes de
evolucionar y **+10 / +5** puntos extra). Pendiente: compilar y probar en partida.

Ajustes respecto a la propuesta:
- Todos los nodos de Maestro exigen **nivel 20** (el 2.º rango, 25): con el máximo de rol en 30, un nodo
  a nivel 28 no podría tener rango 2.
- Filas **bloqueadas visibles** (velo, candado y «Bloqueado: evoluciona el rol a…»); al desbloquearse, el
  borde de la fila late en dorado la primera vez que se ve (el árbol «se expande»).
- Puntos extra: **+10 con Maestro y +5 con Definitivo** (`masterBonusPoints`, `ultimateBonusPoints`).
- Las pasivas evolucionadas usan el rango aprendido **sin** el extra por evolución (si no, un nodo de
  Definitivo valdría ×9 en vez de ×3).

## 1. Reglas

| Aspecto | Propuesta | Motivo |
|---|---|---|
| Nodos nuevos por rol | **+5 con Maestro** y **+5 con Definitivo**: 3 pasivas + 2 activas en cada caso | Lo pedido por el autor; 200 nodos en 20 roles |
| Visibilidad | Las dos filas se ven **bloqueadas** (velo y candado) hasta alcanzar la evolución; al desbloquearse, la fila brilla en dorado la primera vez | Decisión del autor |
| Niveles | Maestro: todos a nivel 20 (2.º rango a nivel 25). Definitivo: todos a nivel 30 (el máximo) | El nivel máximo de rol es 30 |
| Rangos | Nodos de Maestro: **2 rangos** (el 2.º exige +5 niveles). Nodos de Definitivo: **1 rango**, pero más potente | Con el máximo en 30, un nodo a nivel 30 no puede tener rango 2 |
| Puntos de talento | **+10 al llegar a Maestro y +5 a Definitivo** (configurables: `masterBonusPoints`, `ultimateBonusPoints`) | Decisión del autor: alcanza para completar las filas nuevas |
| Validación | El servidor exige la evolución, el nivel y los puntos, como en los demás nodos; el cliente solo muestra | Autoridad del servidor |
| Activas | Cada rol pasa de 4 a 8 activas; se siguen equipando 2 por rol (V/B y N/M) | Más opciones, no más teclas |
| Datos guardados | Las activas equipadas pasan de 2 bits a 3 bits por ranura con un **bit de formato**: lo guardado antes se sigue leyendo igual | Compatibilidad con mundos existentes |

## 2. Arquitectura (sin código por nodo)

- **Pasivas evolucionadas** → reutilizan los `PathEffect` existentes (robo de vida, crítico, ejecución,
  espinas, reducción de daño, esquiva, maná, recurso, regeneración, efectos al golpear, poder de esbirros,
  antiempuje, velocidad, vida, velocidad de ataque y suerte). Valor por rango: **×1,5** del de senda en
  Maestro (2 rangos = 3 rangos de senda) y **×3** en Definitivo (1 rango). Los topes globales siguen
  vigentes (reducción de daño ≤ 40 %, multiplicador de daño ≤ 6).
- **Activas evolucionadas** → 10 **plantillas** con parámetros (daño, radio, efecto, color, partículas,
  sonido): cada activa es una línea de datos, no una clase nueva. Esto también prepara la **v24**: cada
  plantilla tendrá su animación de lanzamiento.

| Plantilla | Qué hace |
|---|---|
| NOVA | Explosión alrededor del jugador: daño + efecto de estado |
| BEAM | Rayo en línea recta (hasta 24 bloques): daño a todos los que atraviesa |
| CONE | Barrido en cono frontal: daño + empuje |
| DASH | Embestida hacia delante: daña a lo que atraviesa |
| ZONE | Lluvia de impactos sobre el punto señalado durante unos segundos |
| VORTEX | Atrae a los enemigos a un punto y los daña |
| AURA | Mejora al jugador y a los aliados cercanos (efectos de estado) |
| HEAL | Cura al jugador y a los aliados cercanos y quita efectos negativos |
| SHIELD | Absorción y resistencia temporales |
| SUMMON | Invoca aliados temporales (lobos, esqueletos, vexes…) con las reglas de los esbirros |

## 3. Los 200 nodos

Formato: *Pasivas* (efecto) · *Activas* (plantilla y efecto).

| Rol | Maestro — pasivas | Maestro — activas | Definitivo — pasivas | Definitivo — activas |
|---|---|---|---|---|
| Bersérker | Sed de sangre (robo de vida), Piel de hierro (reducción de daño), Furia ciega (vel. ataque) | Tajo sísmico (CONE, lentitud), Embestida brutal (DASH) | Hambre de guerra (ejecución), Inquebrantable (vida), Tormenta de acero (crítico) | Terremoto (NOVA, lanza por los aires), Grito del titán (AURA: fuerza y resistencia) |
| Mago | Flujo arcano (maná), Mente afilada (crítico), Escudo de maná (reducción) | Rayo prismático (BEAM), Nova de escarcha (NOVA, lentitud) | Archimago (recurso), Dominio del tiempo (velocidad), Sobrecarga (ejecución) | Lluvia de meteoros (ZONE, fuego), Singularidad (VORTEX) |
| Asesino | Golpe certero (crítico), Paso sombrío (esquiva), Hoja envenenada (veneno) | Danza de cuchillas (NOVA), Salto de sombra (DASH) | Marca de muerte (ejecución), Velo nocturno (velocidad), Sangría (robo de vida) | Tormenta de dagas (CONE), Eclipse (AURA: invisibilidad y velocidad) |
| Herbolista | Savia viva (regeneración), Raíces profundas (vida), Espinas tóxicas (veneno) | Flor curativa (HEAL), Nube de esporas (ZONE, veneno) | Árbol de la vida (regeneración), Corteza (reducción), Polen dorado (suerte) | Jardín sagrado (HEAL mayor), Enredadera (VORTEX, lentitud) |
| Ingeniero | Blindaje (reducción), Engranajes (vel. ataque), Sobrecalentado (fuego) | Pulso eléctrico (NOVA, lentitud), Mortero (ZONE) | Núcleo reforzado (vida), Precisión (crítico), Batería (recurso) | Rayo láser (BEAM), Escudo de energía (SHIELD) |
| Lancero | Postura firme (antiempuje), Perforar (crítico), Carga (velocidad) | Estocada (DASH), Lanza giratoria (NOVA) | Furia del dragón (ejecución), Falange (reducción), Viento cortante (vel. ataque) | Lluvia de lanzas (ZONE), Rayo celeste (BEAM) |
| Espadachín | Filo templado (crítico), Parada (esquiva), Ritmo (vel. ataque) | Corte cruzado (CONE), Iai (DASH) | Kensei (ejecución), Sangre fría (robo de vida), Mente serena (reducción) | Mil cortes (NOVA), Onda de espada (BEAM) |
| Paladín | Fe (regeneración), Égida (reducción), Castigo (fuego) | Martillo justiciero (NOVA), Bendición (HEAL) | Santo (vida), Juicio (ejecución), Luz radiante (espinas) | Consagración (ZONE), Escudo divino (SHIELD) |
| Nigromante | Legión (esbirros), Drenaje (robo de vida), Plaga (marchitar) | Alzar caídos (SUMMON: zombis), Nube pestilente (ZONE, marchitar) | Señor de la muerte (esbirros), Sangre negra (vida), Toque gélido (lentitud) | Ejército eterno (SUMMON: esqueletos), Abrazo de la tumba (VORTEX) |
| Druida | Pelaje grueso (vida), Instinto (velocidad), Garras (crítico) | Zarpazo (CONE), Luz lunar (HEAL) | Espíritu salvaje (regeneración), Corteza antigua (reducción), Zarzas (espinas) | Estampida (SUMMON: lobos), Furia natural (ZONE) |
| Mercader | Fortuna (suerte), Regateo (recurso), Bolsillo lleno (maná) | Lluvia de oro (ZONE), Guardaespaldas (SUMMON: lobo) | Monopolio (suerte), Mercenario (crítico), Seguro (reducción) | Golpe de fortuna (NOVA), Escudo de monedas (SHIELD) |
| Monje | Puño veloz (vel. ataque), Loto (regeneración), Chi (esquiva) | Palma de choque (CONE, empuje), Paso del viento (DASH) | Iluminación (maná), Cuerpo de diamante (reducción), Golpe vital (crítico) | Mil palmas (NOVA), Meditación (HEAL) |
| Elementalista | Catalizador (crítico), Conductor (maná), Ignición (fuego) | Prisma elemental (BEAM), Tormenta (ZONE) | Avatar (recurso), Resonancia (lentitud), Núcleo elemental (reducción) | Cataclismo (NOVA), Ojo del huracán (VORTEX) |
| Cambiaformas | Mimetismo (esquiva), Depredador (crítico), Piel cambiante (vida) | Zarpa fantasma (DASH), Rugido (NOVA) | Forma perfecta (robo de vida), Camaleón (velocidad), Quimera (espinas) | Manada espectral (SUMMON: lobos), Desgarro (CONE) |
| Pistolero | Gatillo fácil (vel. ataque), Ojo de halcón (crítico), Rodar (esquiva) | Ráfaga (CONE), Bala perforante (BEAM) | As de las pistolas (ejecución), Forajido (velocidad), Munición arcana (maná) | Lluvia de balas (ZONE), Disparo final (BEAM mayor) |
| Invocador de armas | Arsenal (recurso), Filo rúnico (crítico), Bastión (reducción) | Espadas gemelas (CONE), Escudo de espadas (SHIELD) | Forja eterna (vel. ataque), Hoja ancestral (ejecución), Armadura viva (espinas) | Tormenta de espadas (ZONE), Lanza de luz (BEAM) |
| Cazador | Rastreo (velocidad), Tiro certero (crítico), Trampa (lentitud) | Disparo múltiple (CONE), Salto evasivo (DASH) | Depredador ápice (ejecución), Camuflaje (esquiva), Bestia interior (robo de vida) | Lluvia de flechas (ZONE), Compañero fiel (SUMMON: lobo) |
| Bardo | Armonía (regeneración), Compás (vel. ataque), Inspiración (recurso) | Acorde vibrante (NOVA, lentitud), Himno (HEAL) | Virtuoso (maná), Balada eterna (vida), Disonancia (lentitud) | Sinfonía (AURA: velocidad, fuerza, regeneración), Crescendo final (NOVA mayor) |
| Brujo | Maldición (marchitar), Sangre maldita (robo de vida), Abismo (maná) | Rayo del vacío (BEAM), Círculo maldito (ZONE) | Condena (ejecución), Alma oscura (vida), Corrupción (veneno) | Portal del abismo (VORTEX), Diablillos (SUMMON: vexes) |
| Domador | Vínculo (vida), Manada (esbirros), Instinto salvaje (velocidad) | Llamada salvaje (SUMMON: lobos), Embestida (DASH) | Rey bestia (esbirros), Piel gruesa (reducción), Colmillo (crítico) | Rugido alfa (AURA), Estampida (CONE) |

Todos los nombres respetan las exclusiones (sin linajes, razas, pactos ni herencias).

## 4. Interfaz (pantalla de roles)

- Dos filas nuevas bajo el árbol actual, con una cabecera dorada **«Maestro»** y otra **«Definitivo»**.
- Las filas ocultas no ocupan sitio; al aparecer, el lienzo crece y se puede desplazar como hasta ahora.
- Iconos de los nodos: los de su plantilla (activas) o de su efecto (pasivas).
- Las fichas muestran el valor real por rango y la plantilla de la activa.

## 5. Balance

- +10 puntos con Maestro (5 nodos × 2 rangos = 10 posibles) y +5 con Definitivo (5 posibles): las filas
  nuevas se pueden completar sin quitar puntos del árbol antiguo.
- Las activas usan los mismos topes que las actuales (daño ≤ ×6, esperas con tope del 60 %, maná).
- Daño base de las plantillas: entre el de las activas C y D actuales del rol.

## 6. Plan de implementación (tras la aprobación)

1. `RoleNode`: evolución requerida y rangos máximos por nodo.
2. `RoleTree`: filas 7 (Maestro) y 8 (Definitivo); tabla de pasivas y activas evolucionadas.
3. `PathEffects`: sumar las pasivas evolucionadas.
4. `EvolvedAbilities`: 10 plantillas y registro de las 80 activas.
5. `RoleData`: formato de equipamiento de 3 bits con bit de versión, con prueba.
6. `RoleManager` y `RoleRules`: validación de la evolución y puntos extra, con pruebas.
7. `RolesScreen`: filas nuevas, animación de despliegue y fichas.
8. Textos: 200 nombres y descripciones en inglés y en español (8 archivos).
9. Documentación y protocolo de prueba.
