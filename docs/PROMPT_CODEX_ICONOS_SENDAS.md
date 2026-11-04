# Prompt para Codex — Iconos propios de los nodos de las Sendas (v46)

> Copia todo lo que hay debajo de la línea en una tarea nueva de Codex, con el repositorio en `main`
> actualizado (incluye la v46.0, commit `6da74d4` o posterior).

---

Eres un agente de código trabajando en el mod de Minecraft **Aptitudes** (Fabric 1.21.1, Java 21). Antes de
tocar nada, **lee `AGENTS.md` completo** y `docs/V46_DISENO_SENDAS.md` (§3, §5 y §10). Responde en español.

## Git (obligatorio)

- Autorizo: `git pull origin main`, crear la rama **`codex/iconos-sendas`** desde `main` y `git add`.
- **No hagas commit ni push** ni toques `main`: el autor hará el commit desde tu worktree. Al terminar cada fase
  dime la **ruta del worktree** y la **rama** para que lo haga antes de seguir (si se acaba el uso a mitad, lo
  ya hecho no se pierde).
- No uses `reset`, `clean`, `checkout` ni `restore` para descartar cambios. No reformatees archivos existentes.

## Objetivo

Crear **300 iconos de 16×16 px** en pixel art para los nodos nuevos de las Sendas, generados **por programa**
(como `tools/textures/gen_items.py`), y una clase de cliente que los dibuje con respaldo al icono actual.

Los 300 iconos son, para cada uno de los 20 roles y cada una de sus 3 sendas (A, B, C):

| Escalón | Id del nodo | Cuántos | Qué representa |
|---|---|---|---|
| S4 | `<rol>_<a|b|c>4` | 60 | Su efecto (`PathEffect`) |
| S5x | `<rol>_<a|b|c>5x` | 60 | Su efecto; rama izquierda de la bifurcación |
| S5y | `<rol>_<a|b|c>5y` | 60 | Su efecto; rama derecha de la bifurcación |
| S6 | `<rol>_<a|b|c>6` | 60 | El efecto de S4, versión potenciada |
| S7 | `<rol>_<a|b|c>7` | 60 | La **piedra angular** (llega en la v46.1; el icono se crea ya) |

`<rol>` es la clave del rol en minúsculas (`berserker`, `mage`, `assassin`, `herbalist`, `engineer`, `lancer`,
`swordsman`, `paladin`, `necromancer`, `druid`, `merchant`, `monk`, `elementalist`, `gunslinger`,
`weapon_summoner`, `hunter`, `bard`, `warlock`, `tamer`, `shapeshifter`).

## Fase 1 — Generador e iconos S4, S5x, S5y y S6 (240)

1. Crea `tools/textures/gen_node_icons.py` (Python 3 + Pillow, el mismo estilo que `gen_items.py`: plantillas de
   letras 16×16, rampa de sombreado con luz arriba-izquierda y contorno oscuro).
   - Uso: `python3 tools/textures/gen_node_icons.py src/main/resources/assets/aptitudes/textures/gui/node [--preview hoja.png]`
   - Escribe `<id>.png` (RGBA, 16×16) en esa carpeta. No modifica nada fuera de ella.
   - **Lee los efectos de `src/main/java/dev/aptitudes/role/RoleTree.java`** (las llamadas `paths(...)` y
     `deeper(...)`), igual que hace `tools/lang/gen_path_lang.py`: S4 = primer efecto de `deeper`, S5x = segundo,
     S5y = tercero, S6 = el de S4. Si no encuentra los 20 roles, que falle con un mensaje claro.
2. Composición de cada icono (todo por capas en el script):
   - **Glifo central por efecto** (19 glifos, uno por `PathEffect`): robo de vida (gota roja con colmillo),
     crítico (estrella de impacto), ejecución (hacha sobre calavera pequeña), espinas (púas), reducción de daño
     (escudo), esquiva (silueta con estela), regeneración de maná (gota azul), recurso del rol (anillo de carga),
     regeneración de vida (corazón con brillo), veneno (gota verde), lentitud (copo de nieve), fuego (llama),
     marchitar (calavera oscura), esbirros (huesos cruzados), antiempuje (ancla), velocidad (bota alada),
     vida máxima (corazón con «+»), velocidad de ataque (dos hojas cruzadas), suerte (trébol).
   - **Marco por escalón**: S4 marco cuadrado simple; S5x marco con muesca a la izquierda; S5y con muesca a la
     derecha; S6 marco doble con esquinas doradas.
   - **Color de la senda** en el marco: A `#E57373`, B `#64B5F6`, C `#81C784` (los mismos que la interfaz).
   - S6 se distingue de S4 a simple vista (marco doble y un destello extra), aunque el glifo sea el mismo.
3. `--preview` genera una hoja con todos los iconos en cuadrícula, ampliados ×4, con el id debajo, para revisarla.
4. `--check` comprueba que existen los 240 (o 300 tras la fase 2) PNG de 16×16 RGBA y termina con error si falta
   alguno.

**Para y dame el informe de la fase 1** (archivos, hoja de vista previa, ruta del worktree y rama).

## Fase 2 — Piedras angulares S7 (60)

- Marco grande en forma de emblema, morado `#7E57C2` con borde dorado, el color de la senda en una franja.
- **Glifo por plantilla** (10): Carnicería (colmillo con gota), Baluarte (torre almenada), Sobrecarga (rayo
  sobre engranaje), Detonación (estallido), Contagio (esporas), Legión (tres calaveras pequeñas), Espectro
  (silueta translúcida), Fortuna (moneda), Tempestad (rayo en zigzag), Santuario (halo con cruz).
- La plantilla de cada senda está en la tabla de `docs/V46_DISENO_SENDAS.md` §10 (columna «Piedra angular»,
  antes de los dos puntos). Cópiala en el script como un diccionario `(rol, senda) -> plantilla` con las 60
  entradas y comprueba que ninguna falta. Ten en cuenta que esa tabla nombra los roles en español; usa las claves
  en inglés de arriba.

**Para y dame el informe de la fase 2.**

## Fase 3 — Clase de cliente (sin tocar las pantallas)

Crea `src/client/java/dev/aptitudes/client/screen/NodeIconRenderer.java` con:

```java
/** Dibuja el icono propio del nodo (textures/gui/node/<id>.png) o, si no existe, el icono de objeto actual. */
public static void draw(DrawContext ctx, RoleNode node, int x, int y)
/** Igual, para la piedra angular S7 de una senda (path 1–3), que aún no es un RoleNode. */
public static void drawKeystone(DrawContext ctx, Role role, int path, int x, int y)
```

- Identificador: `Aptitudes.id("textures/gui/node/" + id + ".png")`.
- Comprueba si existe con `MinecraftClient.getInstance().getResourceManager().getResource(id).isPresent()` y
  guarda el resultado en caché (`Map<String, Boolean>`).
- Respaldo: `ctx.drawItem(NodeIcons.of(node), x, y)`; para la piedra angular, un `ItemStack` de
  `Items.NETHER_STAR`.
- Usa el método `drawTexture` de `DrawContext` **con la firma exacta de Yarn 1.21.1** (compruébalo; no inventes
  sobrecargas).
- **No modifiques** `PathsScreen`, `RolesScreen`, `RoleTree`, `PathRules`, `NodeIcons` ni los archivos de idioma:
  la integración en las pantallas la hace después Claude, en otra rama, para no chocar con la v46.1.

## Compilación y entrega

- Ejecuta `.\gradlew.bat build` y pega el **resultado real** (no digas que compila si no lo ejecutaste).
- Ejecuta `python3 tools/textures/gen_node_icons.py ... --check`.
- Añade una entrada a `CHANGELOG.md` («Sin publicar» → «Añadido»), una fila a la tabla de `AGENTS.md` §2
  (iconos generados, no editar a mano los PNG) y una línea en `docs/ROADMAP_APTITUDES.md`.
- Informe final: archivos creados, resultado real de `gradlew build`, ruta de la hoja de vista previa, ruta del
  worktree y rama, y el mensaje de commit propuesto:
  `feat: iconos propios de los nodos de las sendas (generados)`.

## Restricciones

- Arte **original**: nada copiado de otros juegos, mods o paquetes de recursos.
- Respeta las exclusiones de `AGENTS.md` §3 (también en nombres de variables y comentarios).
- No añadas dependencias Java. Pillow solo se usa en el script.
- Respeta `.editorconfig` (LF; Java con tabulación).
