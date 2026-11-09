# Enemigos nuevos: especificación visual (referencia aprobada por el autor)

Fuente: imagen de diseños comparativos (Errante Velado, Campeón de élite, Cascorio Incandescente y Custodio de las
Cuatro Llaves). Esta especificación recoge lo que se ve en la imagen; las medidas son aproximadas y se afinan al modelar.

## Errante Velado (Niebla de almas, enemigo nuevo)

- **Tamaño:** 1,85 bloques de alto y 0,75 de ancho.
- **Materiales:** tela carbonizada (cuerpo y jirones colgantes), alma (brillo cian en el pecho y la cabeza) y hueso (brazos
  y piernas largos).
- **Silueta:** encorvado, con tiras de tela que cuelgan del torso y del cráneo. Brazo derecho más largo que el izquierdo,
  que termina en garra.
- **Textura:** 64×64.

## Custodio de las Cuatro Llaves (templo sellado, enemigo nuevo)

- **Tamaño:** 2,35 bloques de alto y 1,15 de ancho.
- **Materiales:** arenisca (cuerpo), cobre oxidado (bordes y placas) y luz ritual (núcleo y ojos brillantes).
- **Silueta:** constructo de bloques con una corona alta. Cuatro placas en cruz sobre el pecho, con un núcleo
  luminoso en el centro.
- **Textura:** 128×128.

## Decisiones sobre las diferencias (autor)

1. **Cascorio Incandescente: descartado.** La Lluvia de brasas usa criaturas del Nether vanilla.
2. **Marcas de campeón sólidas: descartadas.** Las marcas de campeón se dibujan con partículas, como ya está aprobado.

El autor tiene una nueva imagen para las ideas que se descartan. Pendiente de recibirla y de decidir si se usa para
otro enemigo o para las marcas con partículas.

## Nueva referencia del autor (bestiario y amenazas)

### Osario Quebrado (Niebla de almas, enemigo nuevo)

- **Tamaño:** 1,8 bloques de alto.
- **Piezas:** `root` (eje, que sostiene el cuerpo), `body`, `head` (cráneo), `arm_left_dragging` (brazo largo de tierra
  y huesos que arrastra por el suelo).
- **Materiales:** hueso claro, tierra oscura y raíces; núcleo de ectoplasma verde `#4DE2A6` en el pecho.
- **Señal:** el núcleo verde y las partículas que se desprenden del brazo al atacar.

### Campeón de élite (referencia visual de los afijos)

- Se mantiene como lo aprobado: **marcas con partículas**, no objetos sólidos.
- La imagen sugiere un anillo de runas sobre la cabeza y símbolos flotantes en colores de afijo: fuego (rojo), fuerza
  (ámbar `#FFB000`) y jade (`#2ECC71`), además de escarcha (cian).

### Escorificador de Hierro (Lluvia de brasas, criatura nueva)

- **Tamaño:** 1,8 bloques de alto.
- **Piezas:** `body` (torso de hierro y roca con rejilla incandescente), `head`, `arms` (brazos pesados con bloques de
  hierro fundido al final) y una chimenea en la espalda.
- **Señal:** la rejilla del pecho se enciende antes de un golpe aplastante.
- Se añade a las criaturas del Nether de la Lluvia de brasas.

### Descartado

- **El Engranaje del Templo** (imagen del autor): descartado.

### Propuesta pendiente: Wither en la Lluvia de brasas

- Aparecería con una probabilidad muy baja.
- El Wither vanilla destruye bloques al atacar, así que no puede aparecer libremente en el mundo.
- **Opciones:** (a) que aparezca solo dentro de una arena de jefe (`world/BossArenas`), donde no daña el terreno; (b) que
  aparezca en el mundo, pero sin daño a bloques, lo que exige un cambio de comportamiento en el jefe.
- **Descartado por el autor.** No aparecerá en la Lluvia de brasas.

## Estado del Osario Quebrado (implementado, sin validar en partida)

- Modelo, animaciones y textura de prueba: `tools/models/gen_mob_models.py` (64×64, `--check` correcto).
- Entidad `mob/OsarioEntity` (combate cuerpo a cuerpo y animaciones de reposo y caminar), registro `mob/OsarioEntities`,
  renderizador `client/render/MobRenderers`.
- Aparece en la Niebla de almas con un 10 % de las apariciones (`mob/OsarioRules`, 5 pruebas).
- Nombre en los 11 idiomas.
- **Pendiente:** la animación `heavy_swipe` aún no se dispara desde el ataque; huevo de aparición; entrada en el
  bestiario; y las partículas de ectoplasma y de tierra. Probar en partida la aparición y el combate.
