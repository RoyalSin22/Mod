# Prompt para ChatGPT: brainstorming de mejoras por rol

Copia desde «CONTEXTO» hasta el final y pégalo en ChatGPT. Está escrito para que no necesite ver el código.
Después trae sus respuestas y las convertimos en tareas (una versión por rol o por tema, ver `AGENTS.md`).

---

## CONTEXTO

Soy el autor de **Aptitudes**, un mod de Minecraft **1.21.1** para **Fabric** (Java 21). Es un RPG de progresión con estética de
fantasía oscura, inspirado en los sistemas de los juegos tipo Souls y los RPG de acción. Quiero que actúes como **diseñador de juego
senior y diseñador de sistemas de combate** y me ayudes a hacer un brainstorming para decidir qué falta y qué mejorar en cada rol.

### Cómo funciona el mod (resumen)
- **Habilidades por uso** (10: minería, excavación, tala, agricultura, combate, arco, defensa, agilidad, pesca, encantamiento) con hitos.
- **Atributos estilo Souls** que se suben en una Hoguera con almas; frascos de curación mejorables; mancha de XP al morir.
- **20 roles.** El jugador elige uno principal y otro secundario (el secundario gana XP más despacio). Cada rol tiene:
  - un **árbol de talentos** de unos 23 nodos (pasivas y activas), más 10 nodos «extra» (N1–N10) añadidos después;
  - **2 ranuras de activas** equipadas (hasta 16 activas por rol entre base y extras);
  - **energía/maná** y recursos propios de rol;
  - **Sendas exclusivas** a nivel 50 (una por rol, con bifurcaciones y una **piedra angular** final);
  - **evoluciones** (filas «Maestro» y «Definitiva»), un arma de evolución propia, una **característica de firma**, una **definitiva**
    y una **legendaria** opcional;
  - sinergias entre roles (resonancia de grupo, runas, elementos) y un **set de armadura único** (Armadura Ascendida) con el aspecto del rol.
- **Eventos del mundo:** Eclipse, Niebla de almas, Lluvia de brasas; jefes con fases (Gran Osario, Coloso, Guardián, Autómata); templos
  subterráneos con puzles; contratos del gremio; grupos con reparto de XP; logros; grimorio con bestiario y crónicas.
- **Dificultad escalable** por anillos de amenaza (0–V) y élites/campeones.

### Los 20 roles (categoría y fantasía)
| Rol | Categoría | Idea |
|---|---|---|
| Bersérker | Marcial | Furia, golpes pesados, riesgo/recompensa |
| Lancero | Marcial | Alcance, cargas, embestidas, salto del dragón |
| Espadachín | Marcial | Duelos, iaido, cortes rápidos |
| Monje | Marcial | Puños, patadas, ki |
| Mago | Arcano | Proyectil arcano, blink, control del tiempo, misiles |
| Nigromante | Arcano | Esbirros, lanza de hueso, drenaje de almas |
| Elementalista | Arcano | 14 elementos (domina hasta 3) y 10 fusiones |
| Mago pistolero | Arcano | Revólver arcano, ráfagas, rebotes, ojo de halcón |
| Asesino | Sombra | Sigilo, daga, paso de sombra, cuchillas |
| Cambiaformas | Sombra | Se transforma en mobs y usa sus poderes |
| Cazador | Sombra | Marcas, trampas, flechas, halcón |
| Brujo abisal | Sombra | Maldiciones, diablillos, perdición |
| Herbolista | Guardián | Curación, lluvia sanadora, esporas, alquimia |
| Paladín | Guardián | Consagración, martillo, provocación |
| Druida | Guardián | Manada de lobos, espinas, naturaleza |
| Bardo | Guardián | Canciones de apoyo en área, notas que aturden |
| Ingeniero | Oficio | Garfio, granadas, torretas, propulsores |
| Mercader táctico | Oficio | Monedas como munición, guardia dorada, economía |
| Invocador de armas | Oficio | Espadas espectrales que orbitan y se disparan |
| Domador | Oficio | Mascotas potenciadas, órdenes, estampida |

### Qué se ha hecho ya (para no repetirlo)
Acabo de rehacer los **efectos de batalla** de las activas base de **los 20 roles**: partículas con física, cámara y pantalla dinámicas
(FOV, retroceso, temblor, viñeteado), sonido sintetizado propio, y **animaciones del cuerpo del jugador en primera persona** sincronizadas
con el impacto. Todo está implementado pero **aún no probado en partida**.

### Lo que sé que queda flojo o sin cubrir
- Solo las **activas base** (2–4 por rol) tienen esos efectos. Las activas «extra» (N1–N10), las **firmas**, las **definitivas**, las
  **piedras angulares**, las **evoluciones Maestro/Definitiva** y las **pasivas** conservan efectos genéricos.
- Algunas activas base quedaron con efecto genérico a propósito (p. ej. Propulsores y Torreta del Ingeniero, Espinas venenosas del Herbolista).
- No hay animaciones de **reposo/carrera/caminar** por rol ni posturas de combate; solo gestos de lanzamiento.
- No sé si el **equilibrio** entre roles es justo; no hay datos de partida.

### Restricciones de diseño (importantes)
- **No propongas** linajes, razas, pactos, herencias raciales o de linaje, ni árboles basados en ascendencia, ni nada que lo sugiera.
- Todo debe funcionar en **servidor autoritativo** (el cliente solo dibuja) y en **multijugador**.
- Sin mods extra salvo los ya usados (Fabric API, GeckoLib para jefes, Player Animation Library para el cuerpo del jugador).
- La interfaz tiene límites: la pantalla de habilidades tiene 10 tarjetas fijas y el árbol cabe en un lienzo con desplazamiento; evita
  propuestas que exijan pantallas nuevas enormes.
- El arte de iconos y armas lo hago con una IA de imágenes (necesito prompts), el sonido es sintetizado por código.

---

## LO QUE QUIERO QUE HAGAS

Para **cada uno de los 20 roles**, en este orden y con este formato (una ficha por rol, sin saltarte ninguno):

1. **Fantasía y promesa:** en una frase, qué debería sentir quien juega este rol, y si lo que describo arriba lo cumple.
2. **Huecos de identidad:** qué falta para que el rol sea reconocible a simple vista (silueta, sonido, recurso propio, ritmo de combate).
3. **5 mejoras concretas**, ordenadas por impacto/esfuerzo, cada una con: nombre, qué hace el jugador, qué ve/oye, y a qué categoría
   pertenece (mecánica, feedback visual/sonoro, progresión, sinergia, calidad de vida, equilibrio).
4. **1 mecánica de recurso o postura única** que haga el rol jugable de forma distinta a los demás (sin romper los límites de arriba).
5. **Sinergia:** con qué otros 2 roles combina bien como principal + secundario y por qué (y qué combinación sería rota).
6. **Riesgos de equilibrio** que ves y cómo medirlos en partida.

Después de las 20 fichas:

- **Lo común a todos los roles** que conviene hacer una sola vez (p. ej. un sistema de feedback de «combo/impacto», posturas de reposo,
  una pasada de equilibrio, un tutorial por rol, accesibilidad: reducir efectos, daltonismo, volumen).
- **Un ranking de los 10 cambios** con mejor relación impacto/esfuerzo en todo el mod.
- **Una lista de 10 preguntas** que debería responder yo (el autor) antes de implementar, para que no se asuma nada.
- Una **hoja de pruebas en partida** (checklist) para validar rápido que los efectos y animaciones nuevas funcionan bien.

### Reglas para tu respuesta
- Sé específico y concreto; evita consejos genéricos de diseño de juegos.
- No asumas datos que no te he dado; si te falta información, pregúntame **antes** de extenderte (haz preguntas concretas, máximo 8).
- Señala contradicciones o riesgos que veas en mi planteamiento.
- Usa tablas cuando compares opciones.
- Responde en **español**.
