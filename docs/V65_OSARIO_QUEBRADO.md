# v65 · Osario Quebrado (rediseño)

Documento de trabajo para rediseñar el Osario Quebrado. Sustituye el modelo actual (64×64, 7 huesos, 3 animaciones)
manteniendo su registro (`aptitudes:osario_quebrado`) y su aparición en la Niebla de almas. El id no cambia.

## 1. Decisiones (confirmadas con el autor)

| Tema | Decisión |
|---|---|
| Papel | Enemigo común de la Niebla de almas, muy fuerte. No es jefe: no usa `AnimatedBossEntity`, ni fases, ni arena. |
| Tamaño | Grande y alto. Propuesta: **2,6 bloques de alto y 1,2 de ancho** (≈ 42 × 20 unidades). Antes: 1,8 × 0,75. |
| Textura | **256×256** (antes 64×64). Más píxeles para el detalle de huesos y musgo. |
| Animaciones | `idle`, `walk`, `heavy_swipe` y **`sweep`** (nueva). |
| Color | Hueso envejecido, tierra, musgo verde y ectoplasma. Brillo en **ojos, pecho y brazo arrastrado**. |
| Glow | Emisivo solo en ojos, pecho y brazo. No en el resto. |

Puntos abiertos (ver §8): equilibrio de vida y daño, y si `sweep` sale en el combate normal o en un ataque aparte.

## 2. Paleta

| Uso | Hex |
|---|---|
| Hueso claro | `#D9D6C3` |
| Hueso envejecido | `#B7B5A6` |
| Sombra de hueso | `#8F8B7C` |
| Tierra | `#4A3B2E` |
| Raíz / madera | `#6B4E3A` |
| Oscuro (detalle, juntas) | `#2F221F` |
| Musgo base (sombra, zonas densas) | `#4A5F35` |
| Musgo seco (zonas expuestas a la luz) | `#5C7142` |
| Musgo oscuro (uniones con la tierra, grietas) | `#3B4D28` |
| Ectoplasma (glow) | `#4DE2A6` |
| Ectoplasma claro (núcleo del glow) | `#7FFFD4` |

El musgo se pinta sobre la tierra y en la parte baja de los huesos. No se usa en la cara.

## 3. Huesos (jerarquía)

Unidades de Blockbench: 1 bloque = 16 unidades. Todos los pivotes son los de la tabla.

| Hueso | Pivote | Contenido | Tamaño aprox. (u) |
|---|---|---|---|
| `root` | `0, 0, 0` | Anclaje en el suelo | — |
| `body` | `0, 24, 0` | Columna, caja torácica, núcleo de ectoplasma | 20 × 22 × 12 |
| `head` | `0, 40, 0` | Cráneo con órbitas y mandíbula suelta; ojos con glow | 12 × 12 × 12 |
| `arm_right` | `-8, 34, 0` | Brazo de ataque (húmero, cúbito, mano); sweep | 5 × 22 × 5 |
| `arm_left_dragging` | `8, 34, 0` | Brazo arrastrado; tierra, raíces, musgo; glow | 7 × 30 × 7 |
| `leg_left` | `4, 14, 0` | Fémur, tibia, pie | 4 × 14 × 4 |
| `leg_right` | `-4, 14, 0` | Fémur, tibia, pie | 4 × 14 × 4 |

Notas sobre el texto original (`osario.txt`) que corregimos aquí:
- El texto ponía el pivote de `body` en `y=14` y el de `head` en `y=24`. Un cráneo sobre el mismo pivote que el torso no
  se mueve bien. Ahora `head` cuelga de `body` a `y=40`.
- Las unidades del texto (29 px de alto) no encajan con 2,6 bloques. Usa el tamaño de la tabla.

## 4. Animaciones

| Nombre | Duración | Bucle | Qué hace |
|---|---|---|---|
| `idle` | 2,0 s | sí | Respiración lenta, cabeceo leve, el brazo arrastrado oscila 0,2 u. |
| `walk` | 1,0 s | sí | Paso pesado y cojo. El brazo arrastrado roza el suelo y la pierna corta se desfasa. |
| `heavy_swipe` | 1,4 s | no | Golpe grande. 0–0,6 s: el cuerpo se echa atrás y el brazo derecho sube. 0,6–0,9 s: impacto hacia delante. 0,9–1,4 s: vuelve. |
| `sweep` | 0,6 s | no | Barrido rápido y bajo con el brazo arrastrado. Sin anticipación. |

Las claves son **deltas** sobre la rotación de reposo del hueso, no ángulos absolutos (regla de Blockbench del proyecto).
Interpolación `smooth` en `idle` y `walk`; `linear` en el impacto de `heavy_swipe`.

## 5. Cambios en Java

| Archivo | Cambio |
|---|---|
| `mob/OsarioEntities.java` | `dimensions(1.2f, 2.6f)`. El id se mantiene. Huevo: sin cambios. |
| `mob/OsarioEntity.java` | Añadir `sweep` al controlador `action` (ya existe `heavy_swipe`). Decidir cuándo se dispara (§8). |
| `mob/OsarioEntity.java` (atributos) | Pendiente de decisión (§8): vida, daño, velocidad. |
| `client/render/MobRenderers.java` | Pasar la capa de brillo (glowmask) del `osario_quebrado`, como hace el Coloso. |
| `geo/entity/osario_quebrado.geo.json` | Reemplazar: 256×256, 7 huesos, identificador `geometry.osario_quebrado`. |
| `animations/entity/osario_quebrado.animation.json` | Añadir `sweep`. Revisar `idle`, `walk`, `heavy_swipe` con los nuevos huesos. |
| `textures/entity/osario_quebrado.png` | Nueva textura 256×256. Mantener la antigua como respaldo fuera del repo. |
| `textures/entity/osario_quebrado_glowmask.png` | Nueva. Solo ojos, pecho y brazo arrastrado. |
| `tools/models/blockbench/osario_quebrado.bbmodel` | Proyecto fuente. El geo se exporta desde aquí. |

Regla del proyecto: `OsarioRules` y las pruebas JUnit no cambian salvo que cambie la probabilidad de aparición en la Niebla.

## 6. Prompt para la textura (IA de imagen o referencia)

```
Textura de un esqueleto encorvado estilo voxel, 256×256 píxeles, vista de la UV desplegada. Huesos color hueso
envejecido (#B7B5A6 a #D9D6C3), con grietas finas, juntas oscuras (#2F221F) y uniones. El brazo izquierdo arrastrado
cubierto de tierra marrón (#4A3B2E, #6B4E3A) y raíces. Musgo verde en tres tonos (#4A5F35, #5C7142, #3B4D28) sobre la
tierra y la parte baja de los huesos, nunca en la cara. Ectoplasma verde brillante (#4DE2A6, núcleo #7FFFD4) solo en
las órbitas del cráneo, el pecho y el brazo arrastrado. Estilo pixel art con detalle alto, iluminación suave, sin fondo.
```

Prompt negativo: `fondo, texto, bordes difuminados, gradiente, luz fuera de los huesos, musgo en el rostro`.

## 7. Glowmask

Solo píxeles de `#4DE2A6` y `#7FFFD4` en ojos, pecho y brazo arrastrado. Todo lo demás, negro transparente.

## 8. Decisiones pendientes (necesito que confirmes)

1. **Vida, daño y velocidad.** El actual tiene 24 de vida, 5 de daño y 0,27 de velocidad. Como enemigo "extremadamente
   fuerte" propongo 60 de vida, 12 de daño y velocidad 0,22. No cambio el balance sin tu visto bueno.
2. **¿`sweep` sale en el combate normal o en un ataque aparte?** Propuesta: `sweep` en el golpe normal cuerpo a cuerpo, y
   `heavy_swipe` como ataque cargado con enfriamiento de 8 s.
3. **Textura 256×256 o 128×128.** Propongo 256 porque pediste más píxeles. Con 128 el detalle de huesos se pierde a
   2,6 bloques de altura.

## 9. Orden de trabajo

1. Confirmar §8.
2. Modelar en Blockbench con la tabla de §3 (sin guardar hasta validar).
3. `validate_model` en reposo y en cada animación; corregir intersecciones de brazos y hombros.
4. Pintar textura 256×256 y glowmask.
5. Exportar geo, animaciones y textura a `src/main/resources`.
6. Java: §5. Ejecutar `./gradlew build` y revisar el juego en la Niebla.
7. Actualizar `CHANGELOG.md`, `docs/CONFIGURATION.md` si hay vida o daño configurables, y el bestiario.

## Estado real

- **Hecho en código:** tamaño 1,2 × 2,6 en `OsarioEntities`; vida 60, daño 12, velocidad 0,22 en `OsarioEntity`;
  `heavy_swipe` con enfriamiento de 160 ticks (8 s) y `sweep` como golpe normal; animación `sweep` añadida al JSON;
  escala provisional 1,45 en `MobRenderers` para el modelo antiguo. `./gradlew build` pasa, con pruebas.
- **Pendiente:** modelo de 7 huesos en la nueva escala, textura 256×256, glowmask. Hasta entonces el modelo
  antiguo se ve escalado (no es la versión final).
- **Pendiente de revisión en partida:** sincronizar `sweep` con el impacto del brazo y comprobar la animación de
  `heavy_swipe` (la del JSON es la antigua, con el brazo arrastrado).
