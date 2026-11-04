# Plan de trabajo para Codex (tareas pendientes)

Plan para avanzar con Codex cuando Claude no esté disponible. **Una tarea por vez, una rama por tarea**, en este
orden (cada una parte del `main` que ya incluye la anterior). Cada tarea tiene su prompt listo para pegar:
copia el **bloque común** y, debajo, el bloque de la tarea.

| Orden | Tarea | Rama | Tamaño | Toca código compartido |
|---|---|---|---|---|
| 1 | T1 — Iconos de S1–S3 y árbol con iconos | `codex/iconos-s1-s3` | Pequeño | Poco (`RolesScreen`) |
| 2 | T2 — Pruebas de guardado de contratos y grupos | `codex/pruebas-guardado` | Pequeño | No (solo pruebas) |
| 3 | T3 — Grimorio actualizado | `codex/grimorio-v46` | Pequeño | No (textos) |
| 4 | T4 — v44 Tutorial guiado | `codex/v44-tutorial` | Medio | Poco |
| 5 | T5 — v42 Alquimia del Herbolista | `codex/v42-alquimia` | Medio-grande | Sí (Herbolista) |
| 6 | T6 — v43 Maestría y niveles de amenaza | `codex/v43-maestria-amenaza` | Grande | Sí (XP y criaturas) |
| 7 | T7 — Preparar la publicación 1.4.0 | `codex/publicar-1.4.0` | Pequeño | No |
| 8 | T8 — v45 Traducciones pt_br, fr_fr, de_de | `codex/v45-traducciones` | Grande, mecánico | No (textos) |

**Antes de empezar T1**: compila y prueba en partida la v46 (`docs/TESTING.md`, «v46.0» y «v46.1»). Si algo
falla, primero se corrige.

---

## Cómo integrar una rama en `main` tú mismo (sin Claude)

Solo cuando Codex informe `BUILD SUCCESSFUL` **y** hayas probado en partida lo que indica su informe. En `cmd`,
en tu carpeta del proyecto (`C:\Users\Johan\Desktop\Gits\MOD\Mod`):

```bash
git fetch origin
```
```bash
git checkout main
```
```bash
git pull origin main
```
```bash
git merge --ff-only origin/codex/<rama>
```
```bash
gradlew.bat clean build
```
```bash
git push origin main
```

- Si `--ff-only` falla («Not possible to fast-forward»), `main` avanzó mientras Codex trabajaba: pídele a Codex
  «Haz rebase de tu rama sobre origin/main, resuelve conflictos conservando lo de ambos, compila y para», haz tú
  el commit/push desde su worktree y repite.
- Si la compilación falla tras el merge, **no hagas push**: pega el error a Codex.

---

## Bloque común (pegar al principio de cada prompt)

```text
Eres un agente de código en el mod de Minecraft «Aptitudes» (Fabric 1.21.1, Java 21). Antes de tocar nada,
lee AGENTS.md completo y docs/CONTEXTO_CODEX.md. Responde en español.

GIT: autorizo «git pull origin main», crear la rama indicada desde main y «git add». NO hagas commit ni push
ni toques main: yo hago el commit desde tu worktree. Divide el trabajo en las fases indicadas y PARA al final
de cada fase con un informe (archivos, resultado REAL de .\gradlew.bat build, ruta del worktree y rama) para
que yo haga el commit antes de seguir. Nunca uses reset, clean, checkout ni restore para descartar cambios.

REGLAS: respeta las exclusiones de AGENTS.md §3 (sin linajes, razas, pactos ni herencias; tampoco en nombres
ni comentarios). No cambies ids de registro ni valores por defecto de balance existentes. Enums y listas
guardadas: solo añadir al final. Toda petición del cliente se valida en el servidor. Cada campo de
configuración nuevo: valor por defecto seguro, límite en validated(...) y fila en docs/CONFIGURATION.md.
Idiomas: cada clave en en_us y es_es y copia es_es a es_mx, es_ar, es_cl, es_ec, es_uy y es_ve, SIN
reformatear (2 espacios); un % literal en un texto con argumentos es %%. Fórmulas en clases puras con pruebas
JUnit (no crees pruebas con el nombre de una existente). No añadas dependencias ni mixins salvo que la tarea
lo diga. Nunca digas que compila sin haberlo ejecutado.

AUTORREVISIÓN antes de cada informe (errores reales de versiones anteriores):
- ¿Se puede repetir una recompensa (cobrar, reclamar, aceptar) más de una vez? Debe ser imposible.
- ¿Se consumen objetos sin dar lo prometido? Todo o nada.
- ¿Se resta o duplica XP por error? ¿Cuenta la XP de comandos de administrador o de creativo?
- ¿Qué pasa en creativo, espectador, al morir (copyOnDeath), al reconectar y con el servidor reiniciado?
- ¿Y en grupo (party) y con dos jugadores? ¿Puede afectar a aliados o a otros jugadores?
- ¿Hay valores del cliente o de la configuración sin acotar (desbordamiento, negativos, NaN)?
- ¿Algún texto quedó sin traducir o con la clave a la vista?
Al final: entrada en CHANGELOG.md («Sin publicar»), línea en docs/ROADMAP_APTITUDES.md, pasos de prueba en
docs/TESTING.md y fila en la tabla de AGENTS.md §2 si creas un sistema nuevo.
```

---

## T1 — Iconos de S1–S3 y árbol con iconos

```text
TAREA T1 (rama codex/iconos-s1-s3).
Fase 1: amplía tools/textures/gen_node_icons.py para generar también S1, S2 y S3 de las 60 sendas
(ids <rol>_<a|b|c><1|2|3>, efectos de paths(...) en RoleTree.java): 180 iconos más, mismo estilo; S1–S3 con
un marco más sencillo que S4 (por ejemplo, solo esquinas) para que se note la progresión. --check debe
validar 480 iconos. Hoja --preview con todo. No cambies los 300 existentes (compruébalo píxel a píxel).
Fase 2: en RolesScreen, los nodos de senda del árbol (S1–S3 en la cuadrícula) se dibujan con
NodeIconRenderer.draw(...) en lugar del icono de objeto, sin cambiar la distribución. Nada más en esa pantalla.
```

## T2 — Pruebas de guardado de contratos y grupos

```text
TAREA T2 (rama codex/pruebas-guardado). Solo archivos de prueba nuevos (no cambies código principal salvo
que encuentres un error real; si lo encuentras, descríbelo en el informe antes de corregirlo).
- contract/ContractDataCodecTest: ida y vuelta de ContractData.CODEC con JsonOps (ofertas aceptadas,
  progreso, reputación, contratos completados) y datos corruptos (índices fuera de rango, negativos, listas
  más largas que el máximo) que deben acotarse como hace el constructor.
- party/PartyStateNbtTest: guardar y cargar PartyState (NbtCompound) con varios grupos, líder, opciones y
  nombres; un NBT vacío o incompleto no debe fallar.
Si alguna clase no se puede probar sin arrancar Minecraft, explica por qué y prueba lo que sí sea puro.
```

## T3 — Grimorio actualizado

```text
TAREA T3 (rama codex/grimorio-v46). Actualiza los textos del grimorio (client/screen/GuideScreen y claves
guide.aptitudes.*) para la v46: capítulo nuevo «Sendas y piedras angulares» (exclusividad, 7 escalones,
bifurcación, piedras angulares con las 10 plantillas y sus valores por defecto, vista «Sendas»), capítulo
«Logros» (pestaña Aptitudes, cómo se obtienen) y actualiza el capítulo del Eclipse/eventos con la v41.1–v41.2
(Niebla solo no muertos, meteoritos grandes y criaturas del Nether, Eclipse con armadura). Cada capítulo debe
caber en las páginas existentes (comprueba el ancho). Lee los valores de docs/BALANCE.md; no copies recetas.
```

## T4 — v44 Tutorial guiado

```text
TAREA T4 (rama codex/v44-tutorial). Implementa la v44 según docs/PLAN_v40_v45.md («v44 — Tutorial guiado»):
6 pasos (grimorio, Hoguera y subir un nivel, elegir rol principal, usar una activa, combo de 5, fabricar en la
Mesa del gremio), cada uno con una recompensa pequeña (esmeraldas o XP de rol; nunca objetos de evolución).
- Fase 1: regla pura tutorial/TutorialRules (orden, paso siguiente, recompensa, omitido) + prueba
  tutorial/TutorialRulesTest; attachment «tutorial» con copyOnDeath; ganchos en los sistemas existentes
  (SoulsManager.levelUp, RoleManager.setRole, AbilityManager.execute tras éxito, Combos, GuildCrafting).
  Cada paso se completa una sola vez; la recompensa nunca se repite. Un jugador que ya hizo algo antes de la
  v44 (rol elegido, nivel de alma > 0) no debe quedarse atascado: esos pasos se marcan al entrar.
- Fase 2: aviso discreto en el HUD con el paso actual (desactivable en config/aptitudes-client.json y en
  Mayús + O), comando /aptitudes tutorial [status|skip], textos en 8 idiomas y documentación.
```

## T5 — v42 Alquimia del Herbolista

```text
TAREA T5 (rama codex/v42-alquimia). Implementa la v42 según docs/PLAN_v40_v45.md («v42 — Alquimia del
Herbolista»): 4 pociones propias registradas con el registro de pociones de 1.21.1 (Maná, Frenesí, Velo del
eclipse, Vigor del bosque) con recetas de destilado (FabricBrewingRecipeRegistryBuilder de Fabric API) e
ingredientes del mod existentes (pétalos de la Bolsa de hierbas, Fragmentos del eclipse, Esencia de élite).
- Fase 1: efectos de estado propios si hacen falta (Maná: recupera energía con AbilityManager; Velo del
  eclipse: −daño de criaturas con la etiqueta de Eclipse/eventos; acotado), pociones, recetas, texturas de
  los efectos generadas por programa (sigue el estilo de tools/textures), textos.
- Fase 2: bonificación del Herbolista (destilado más rápido si un Herbolista está cerca o abre el soporte; +50 %
  de duración de las pociones que destila; XP de rol al destilar, con rendimiento decreciente para evitar
  granjas), valores configurables, capítulo «Alquimia» del grimorio leyendo las recetas del juego, docs.
Valida en el servidor; ninguna poción puede apilar efectos sin límite ni saltarse el tope global de daño.
```

## T6 — v43 Maestría y niveles de amenaza

```text
TAREA T6 (rama codex/v43-maestria-amenaza). Implementa la v43 según docs/PLAN_v40_v45.md.
- Fase 1 (Maestría): regla pura logic/MasteryRules (puntos por XP sobrante al nivel máximo, curva propia,
  +0,5 % por punto con tope configurable, por defecto +10 %) + logic/MasteryRulesTest. Guardado en el
  attachment de progreso con claves nuevas (los datos antiguos siguen valiendo). Aplica el bonus a cada
  habilidad (p. ej., Minería → velocidad de minado) y a los roles (nivel 50). La XP de comandos no cuenta.
  Pantalla K: número dorado en la tarjeta, SIN tarjetas nuevas (debe seguir en 5×2 y 430×262).
- Fase 2 (Amenaza): regla pura logic/ThreatRules (anillo por distancia al punto de aparición del mundo, por
  defecto cada 1000 bloques hasta el V; multiplicadores de vida/daño, botín y probabilidad de élite) +
  logic/ThreatRulesTest. Aplica al aparecer criaturas hostiles en el Mundo Normal (sin tocar jefes, esbirros,
  criaturas de eventos ya escaladas ni criaturas con nombre). Aviso en el HUD al cambiar de anillo.
  threatEnabled (activado) y parámetros configurables y acotados. Modificadores de atributo con
  AttributeApplier o ids estables, quitando el anterior.
```

## T7 — Preparar la publicación 1.4.0

```text
TAREA T7 (rama codex/publicar-1.4.0). Solo cuando las tareas anteriores que el autor quiera publicar estén en
main y probadas. Sube mod_version a 1.4.0 en gradle.properties; en CHANGELOG.md convierte «Sin publicar» en
«[1.4.0] — <fecha de hoy>» (deja un «Sin publicar» vacío arriba) con un resumen claro para jugadores;
actualiza docs/PUBLICACION.md (descripción pública con grupos, contratos, logros, eventos, nivel 50, sendas y
piedras angulares) y la tabla de «Limitaciones conocidas» del README. Compila y adjunta el nombre del .jar.
No publiques nada en Modrinth/CurseForge: eso lo hace el autor.
```

## T8 — v45 Traducciones

```text
TAREA T8 (rama codex/v45-traducciones). Al final, cuando ya no se añadan claves. Crea pt_br.json, fr_fr.json y
de_de.json a partir de en_us.json (todas las claves, mismo orden, 2 espacios, sin claves de más ni de menos).
Respeta los %s/%1$s y los %% exactamente. Los textos generados se amplían en sus generadores en lugar de a mano:
tools/lang/gen_evolved_lang.py y tools/lang/gen_path_lang.py (añade los tres idiomas). Hazlo por fases
(interfaz y objetos; grimorio; nodos de rol con los generadores) y añade un script de comprobación que
verifique que los 3 idiomas tienen exactamente las claves de en_us y los mismos marcadores %. Marca en el
CHANGELOG que es una «traducción inicial» que conviene revisar con hablantes nativos.
```

---

## Lo que queda para Claude (cuando vuelva a tener uso)

- Revisar las ramas de Codex que no estén en `main` (sobre todo T5 y T6, que tocan combate y progresión).
- Balance tras la prueba en partida de la v46 (piedras angulares, Fortuna en granjas, Eclipse en la Totalidad).
- Decisiones de diseño pendientes: Reforja + Templado en la Hoguera, sinergias senda ↔ activas, resonancia de
  grupo.
