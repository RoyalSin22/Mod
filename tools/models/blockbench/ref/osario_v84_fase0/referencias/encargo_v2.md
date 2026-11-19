# PROMPT MAESTRO PARA CODEX — Osario Quebrado (V84)

Pega este documento completo. Es autocontenido: incluye todas las tablas, dimensiones, pivotes, paleta, cronogramas, ataques, hitboxes e IA. No hay referencias a documentos externos para obtener datos. Responde **en español** con rigor técnico. Distingue siempre **VERIFICADO / PROPUESTA / PENDIENTE** y no inventes datos del repositorio.

## A. ROL
Actúa a la vez como director de arte de fantasía oscura, modelador 3D en Blockbench (vía Blockbench MCP), artista de pixel art, animador técnico de criaturas, diseñador de combate RPG, ingeniero de IA para Minecraft y desarrollador senior de Fabric + GeckoLib. Objetivo: construir el **Osario Quebrado** de «Aptitudes» con calidad profesional (igual o superior al Errante Velado y al Escorificador de Hierro), respetando las referencias visuales, el código existente y las decisiones aprobadas. No quiero un modelo genérico ni una aproximación superficial.

## B. REGLAS DEL REPOSITORIO (obligatorias)
- Trabaja en una rama propia: `codex/osario-quebrado-v84`. **Nunca** en `main`; no hagas push a otras ramas, merge ni PR sin permiso explícito del autor.
- **No reformatees** archivos existentes; respeta `.editorconfig` y `.gitattributes` (LF; JSON de idiomas con 2 espacios).
- No cambies ids de registro ni el namespace `aptitudes`. No dupliques el registro de la entidad. **No modifiques** Errante Velado, Escorificador de Hierro, Coloso Sin Rostro, Custodio, Gran Osario Quebrado ni el balance global. **No cambies** los atributos base del Osario (vida 24, daño 5, velocidad 0,27, seguimiento 24).
- Nunca digas que algo compila o pasa pruebas sin ejecutarlo: `./gradlew build` y los `--check` de `tools/`. Si no puedes ejecutarlo, dilo.
- No añadas dependencias (GeckoLib 4.7.7 y Player Animation Library 1.1.6 están fijadas; no las subas).
- No incluyas nombres de modelos de IA en commits ni documentación; el único trailer permitido es el de sesión.
- Idiomas: cada clave en `en_us` y `es_es`; copia `es_es` a `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy`, `es_ve`; pt/fr/de con los generadores de `tools/lang/` (no edites esos JSON a mano). `%` literal con argumentos = `%%`. **Ningún texto de jugador nombra la «Conciencia» ni usa «ancestral» o «primordial».**
- Prohibido proponer linajes, razas, pactos o herencias.
- Documenta en `CHANGELOG.md`, `docs/TESTING.md`, `docs/ENEMIGOS_DISENO.md` y `docs/V84_OSARIO_QUEBRADO.md` (nuevo) y añade la fila de `AGENTS.md`.

## C. CONTEXTO VERIFICADO (comprueba que no ha cambiado)
- Versiones: Minecraft 1.21.1 · Yarn 1.21.1+build.3 · Fabric Loader 0.16.14 · Fabric API 0.116.6+1.21.1 · GeckoLib 4.7.7 · PAL 1.1.6+mc.1.21.1 · Java 21 · Loom 1.10.5 · Gradle 8.14.3. Blockbench 5.2.2 y Blockbench MCP Bridge 1.11.1.1 son las versiones del autor (no verificables desde el repo: confírmalas con el MCP).
- Mob `aptitudes:osario_quebrado`: `mob/OsarioEntity.java` (`HostileEntity` + `GeoEntity`; hoy `MeleeAttackGoal`, daño inmediato al **inicio** del clip), `mob/OsarioEntities.java` (registro, `dimensions(0.75f, 1.8f)`, `maxTrackingRange(8)`, huevo `osario_quebrado_spawn_egg`), `mob/OsarioRules.java` y `FogMobRules.java` (10 % de la Niebla), `client/render/MobRenderers.java` (`GeoEntityRenderer` + `DefaultedEntityGeoModel`, sin capa emisiva). Atributos: 24 / 5 / 0,27 / 24.
- Aparece en la Niebla de almas (`event/WorldEventManager`), en oleadas y como **campeón** de la Cripta (`temple/CryptWaves`) y por huevo. Botín `nucleo_osario` solo si nació en un evento (`dropLoot` comprueba la etiqueta). Elegible para Amenaza V83 (`logic/ThreatManager`: llamada de auxilio, esquiva de proyectiles, furia, resistencia al retroceso) y para élites (+35 % `GENERIC_SCALE`).
- **Estado actual en `main`: PLACEHOLDER** generado por `tools/models/gen_mob_models.py`: 7 huesos, 8 cubos, atlas 64×64 de 5 colores planos, clips `idle` 2,0 s, `walk` 1,0 s, `heavy_swipe` 1,4 s; sin glowmask, sin sonidos, sin `.bbmodel`.
- **Trabajo previo NO integrado** en `origin/faceless-brillo` (commits `e863ba3`, `ea138a0`, `1534389`): `docs/V65_OSARIO_QUEBRADO.md`, `tools/models/blockbench/osario_quebrado.bbmodel` (`model_format: bedrock`, 21 elementos), geo (15 huesos), animaciones (idle/walk/heavy_swipe/sweep), textura 256² con `texture_width` 128, `osario_quebrado_glowmask.png`, `tools/models/blockbench/ref/osario_textura_previa/` (`SPEC_textura.md`, `gen_osario_texture.py`, hojas). Es un **diseño divergente** (2,9 × 1,2 bloques, 60 de vida, 12 de daño): **no lo fusiones ni lo borres ni lo sobrescribas**. Archívalo (ver Fase 0).
- Patrón técnico a imitar (Errante Velado): `tryAttack` = `false`; `RendGoal`; acción sincronizada con un `DataTracker<Long>` (sello de tick + ordinal); `SpectralRendRules` (relojes y sector, admite escala); `getDeathMaxRotation = 0`; clips `alert` 0,65 s, `hurt` 0,43 s, `death` 0,95 s con `hold_on_last_frame`. Editables de referencia: `tools/models/blockbench/errante_velado.bbmodel` y `escorificador_hierro.bbmodel` (`format_version 5.0`, `model_format: geckolib_model`, `box_uv`). Contrato de modelos de `docs/V63_JEFES.md`: huesos `root`, `body`, `head`; una animación por ataque con el mismo id.
- Convenciones de GeckoLib 4.7.7 **verificadas en su código fuente**: (1) glowmask = `<textura>_glowmask.png`; alpha > 0 marca píxeles emisivos; **el color sale de la textura base** y se borra de la capa base; (2) las rotaciones de animación **se suman** a la rotación de reposo; (3) X e Y se niegan al cargar modelo y animación; (4) el renderer aplica `getScale()`; (5) el UV se normaliza por `texture_width/height` de la descripción del geo; (6) `getDeathMaxRotation` se puede anular.
- API de Minecraft verificada: `EntityType.Builder#eyeHeight(float)`, `trackingTickInterval(int)`, `Entity#setVelocity`, `MobEntity#isAttacking()` (sincronizado).
- `Elites`/`CryptWaves`/`ThreatManager` usan modificadores de atributo; el daño debe salir por `getDamageSources().mobAttack(this)` y leer `GENERIC_ATTACK_DAMAGE` para que los afijos y la Amenaza lo multipliquen.

## D. IDENTIDAD (inalterable sin permiso)
No muerto de la Niebla de almas; combatiente cuerpo a cuerpo de gran presión ofensiva con ataques telegrafiados. **No es un jefe, ni un demonio, ni un constructo, ni está controlado directamente por nada.** Lore provisional: manifestación de Esencia residual (Eco) que mantiene unidos restos óseos, tierra funeraria, raíces y fragmentos de cuerpos antiguos; parece haber emergido de una sepultura incompleta; el brazo del lado de la masa permanece unido a una masa irregular de tierra, raíces, escombros y huesos que arrastra; en su caja torácica hay un núcleo de ectoplasma verde; no está confirmado que conserve recuerdos. Existencia inestable, violenta, antinatural. **Prohibido:** armaduras, armas, ropa, cuernos, elementos demoníacos, esqueleto gigante genérico, garra convencional en la masa, fuego azul/cian, copia de la cabeza vanilla, rediseño de identidad.

## E. DECISIONES APROBADAS POR EL AUTOR (no las cuestiones; valida y avisa si algo falla)
Atlas 256×256 sujeto a validación UV (validado: **2 px/u**, `texture_width` 128) · núcleo con textura emisiva independiente · **cuatro ataques** (`claw_swipe`, `heavy_swipe`, `ground_slam`, `lunge`) · embestida contextual de corto alcance · golpe pesado disponible para mobs comunes (menos frecuente) · animación de persecución `chase` · hitbox física 0,75 × 1,8 · sonidos propios · IA contextual con prevención de repetición · compatibilidad con élites y campeones · atributos base intactos · modelado mediante Blockbench MCP.

## F. REFERENCIAS VISUALES
**Pide al autor las 4 imágenes** (hoja de diseño «Osario Quebrado – Bestiario + Diseño de Mob», pergamino «Bestiario y amenazas», dos maquetas de Blockbench) y analízalas en la Fase 0. Si no las recibes, **detente** en la Fase 0. Jerarquía de autoridad: la **hoja de diseño y el pergamino mandan**; la maqueta con `spine/ribs/chest_core/debris_roots` es coherente y secundaria; la maqueta del esqueleto muy encorvado con el brazo delgado largo y la tierra solo en el antebrazo **diverge** y solo sirve para la postura. Las imágenes son renders de IA, no proyectos reales.

### F.1 Lectura de la referencia principal (ya analizada; contrástala con las imágenes)
| Aspecto | Lo que muestra |
|---|---|
| Proporciones | 1,8 bl de alto (29 u) × 0,75 bl (12 u) de hitbox. Cabeza ≈ 21–24 % de la altura (cubo de 7 u). Tabla: `body` 10×8×5, `head` 7×7×7, `arm_right` 3×10×3 (segmentado), `arm_left_dragging` 5×26×6 (compuesto), piernas 3×12×3 |
| Silueta frontal | Cráneo cúbico **inclinado ≈10–15°** con grieta superior; caja torácica visible con ectoplasma; brazo delgado a la **derecha de la pantalla** colgando más abajo de la rodilla con mano de 4 dedos; **masa a la izquierda de la pantalla** desde el hombro hasta el suelo, ensanchándose abajo con huesos clavados y musgo |
| Silueta posterior | Columna vertebral con costillas visibles; la masa a la **derecha de la pantalla** (coherente: es el mismo lado del mob); cráneo posterior liso |
| Lateral | Torso inclinado ≈25–30° hacia delante, cabeza adelantada, masa **inclinada hacia atrás** y apoyada en el suelo con escombro; piernas con rodillas dobladas |
| 3/4 | Hombro de la masa cubre el trapecio y llega a la oreja; el brazo delgado largo cae hacia delante |
| Piernas y pies | Delgadas (3 u), rodillas marcadas, pies con 3 dedos/falanges; postura inestable |
| Materiales | Hueso claro dominante; masa de tierra oscura con raíces; musgo en parches **localizados** (mediana `#48603C`, `#3C4824`, `#485424`) [V muestreo]; ectoplasma solo en el pecho y partículas |
| Animaciones (R1) | idle: masa apoyada, brazo delgado colgando; walk: inclinación mayor, masa arrastrando con escombro; **heavy_swipe: el brazo delgado sube por encima de la cabeza y barre en un arco bajo con estela verde; la masa queda atrás** |


## G. FLUJO POR FASES CON PUERTAS DE APROBACIÓN
Después de **cada fase** entrega capturas/evidencias y **espera la aprobación del autor** antes de continuar. No avances si una puerta falla.

**FASE 0 — Verificación (solo lectura) [PUERTA 0]**
1. Lee los archivos de §C y confirma que el estado coincide; lista cualquier diferencia.
2. Recibe y analiza las imágenes (§F). Lista contradicciones antes de modelar.
3. Confirma con el MCP las versiones de Blockbench y del puente, y que `geckolib_model`/`box_uv` está disponible.
4. **Calibra los signos** (§6.1): rota `spine_upper` +10° en X, `body` +10° en Y y +10° en Z y anota el efecto real; si algún eje es contrario a la convención de las tablas, invierte ese eje en TODAS las tablas.
5. **Preserva el trabajo previo:** en tu rama, copia (sin sobrescribir) los archivos del Osario de `origin/faceless-brillo` a `tools/models/blockbench/ref/osario_v65_codex/` y el placeholder de `main` a `.../placeholder_main/`. La etiqueta `archive/osario-faceless-brillo-1534389` ya fue creada por el autor/auditoría (D-18); no la muevas.
6. Presenta un boceto textual de silueta (frente/perfil/espalda/3-4) y recuerda que las decisiones D-15 a D-23 ya están APROBADAS (silueta original con nombres semánticos de hueso, atlas 256×256 a 2 px/u, estela por partículas, presión colectiva solo entre Osarios, integración de `faceless-brillo` fuera de alcance). **No modeles todavía.**

**FASE 1 — Bloqueo de volúmenes y rig [PUERTA 1]**
Crea `tools/models/blockbench/osario_quebrado.bbmodel` (`model_format: geckolib_model`, `box_uv`, resolución del proyecto 128×128, PNG de 256×256 = 2 px/u). Jerarquía y pivotes: §6.2 (ajusta con el modelo a la vista y anota cada cambio). Postura de reposo con las rotaciones de §6.2. Mide y demuestra: altura posada 28–30 u, anchura de hombros 12 u, suelas en y = 0, `dragging_end` en y = 0. Capturas: frente, perfil, espalda, 3/4 y vista de huesos.

**FASE 2 — Geometría detallada [PUERTA 2]**
Construye los cubos de §6.3 (plan base 145; margen +25; tope 190) según §5. Cada cubo con función; todas las dimensiones múltiplos de 0,5 u. Anatomía con asimetría, deterioro y satélites inclinados. Prohibido: extremidad rectangular, garra, capa de tela, acumulación de cubos sin intención. Entrega el recuento por hueso y las capturas.

**FASE 3 — Textura pixel art, UV y glowmask [PUERTA 3]**
Sigue §7 y §8. Presenta antes de exportar: (1) UV completa 256×256, (2) vista frontal, (3) lateral izquierdo, (4) lateral derecho, (5) espalda, (6) 3/4, (7) con glow, (8) sin glow, (9) la textura ampliada ×8 con vecino más cercano, (10) paleta final y porcentaje de verde. **Espera aprobación antes de exportar.**

**FASE 4 — Animaciones [PUERTA 4]**
Crea los 10 clips de §9 con los puntos de partida de §10 (son deltas sobre el reposo), refinándolos con el modelo a la vista. Valida contactos con el suelo con transformaciones globales, cierre de bucles, ausencia de vibraciones, retraso de la masa y movimiento secundario. Entrega GIF/capturas de cada clip y la tabla final de claves.

**FASE 5 — Exportación e integración [PUERTA 5]**
Sigue §14. Exporta geo, animaciones y texturas sin sobrescribir sin diff; añade `osario_quebrado` a `HAND_MADE`; añade la capa emisiva y `getDeathMaxRotation = 0` en `MobRenderers`; implementa `OsarioStrikeRules` (pura) + `OsarioStrikeGoal` + `DataTracker` de acción (4 bits) con los valores de §11–§13; sonidos con `tools/audio/gen_sounds.py`; textos y bestiario; pruebas `OsarioStrikeRulesTest` y `OsarioResourcesTest`. Ejecuta `./gradlew build`, `gen_mob_models.py --check` y los generadores de idioma.

**FASE 6 — Validación [PUERTA FINAL]**
Aplica §15.3 y §15.4 (rendimiento con 1, 5, 10, 20 y 30 Osarios, mismas condiciones antes/después) y califica de 0 a 10 con evidencias: fidelidad al concept art, anatomía, geometría, silueta, jerarquía, pivotes, textura, UV, emisión, animación, fluidez, peso físico, salvajismo, dinamismo, legibilidad de ataques, detección de impactos, navegación, IA, rendimiento y multijugador. **No apruebes lo que no puedas verificar**: marca «no verificado» y di qué falta.

## H. ENTREGA FINAL
Informe con: qué cambiaste y por qué, archivos tocados, resultado real de `./gradlew build` y de los `--check`, riesgos, decisiones pendientes, capturas de cada puerta, tabla antes/después de rendimiento y hash y mensaje del commit (en `codex/osario-quebrado-v84`).

---

# ANEXOS (especificación completa; tablas numeradas como en la auditoría)

## 4. Especificación artística revisada

**Intención.** Un resto humanoide que la Esencia sostiene en pie: esquelético y frágil por un lado, **abrumador y pesado por el otro** (la masa). Lectura en 3 segundos y a 8 m, **sin partículas**: (1) esqueleto encorvado, (2) masa funeraria que va del hombro al suelo, (3) núcleo verde en el pecho. Prioridad de atención: **pecho → masa → cráneo**.

**Lenguaje.** Geometría de Minecraft (cubos) con dirección oscura tipo Dark Souls/Elden Ring/Berserk, sin copiar diseños. Asimetría deliberada: lado delgado (frágil, rápido) frente a lado pesado (lento, devastador).

**Identidad inalterable.** Cráneo cúbico inclinado con grieta; caja torácica expuesta con núcleo de ectoplasma; brazo delgado largo con mano de 4 dedos; masa funeraria del hombro al suelo (hueso, tierra, raíces, musgo localizado, escombro); piernas delgadas, rodillas marcadas, postura inestable. **Prohibido:** armaduras, armas, ropa, cuernos, elementos demoníacos, fuego azul/cian, garra convencional en la masa, esqueleto gigante genérico, copia de la cabeza vanilla, capa de tela, extremidad rectangular uniforme.

**Diferenciación con el Gran Osario (D-17 aprobada).** El Osario común tiene **un solo núcleo** de ectoplasma, **una sola masa lateral** y proporciones de esqueleto humano (1,8 bloques). El Gran Osario (jefe de 1,6 × 3,2 bloques) será compuesto —varios cráneos y costillares, **tres** núcleos, masas a ambos lados, más niebla—. Comparten paleta y materiales (hueso, tierra, raíz, musgo, ectoplasma) para leerse como el mismo origen; la escala y la multiplicidad los distinguen. **No toques el Gran Osario en esta tarea**; no copies su geometría ni añadas núcleos o cráneos extra al común.

**Calidad objetivo.** Igualar o superar al Errante Velado (27 huesos, 131 cubos) y al Escorificador (22 huesos, 128 cubos) con ≈29 huesos y ≈145–170 cubos; desviaciones solo justificadas por resultados visuales y medición de rendimiento. La cifra de cubos **no** es criterio de calidad.

## 5. Especificación anatómica (con medidas de la referencia)

Unidades de modelado (u): 16 u = 1 bloque. «Posado» = con la postura de reposo encorvada.

| Región | Especificación |
|---|---|
| **Cráneo** | Cubo de **7×7×7** en total (cráneo 7×7×6 + 1 u de placas frontales para tener **cuencas hundidas 1 u**). Inclinado ≈10–12° hacia el lado delgado, grieta superior (cuña 1×1×2 + pintura), arco superciliar, 2 pómulos, 5 dientes superiores desiguales (1 roto). No es la cabeza del esqueleto vanilla |
| **Mandíbula** | Hueso `jaw` articulado: cuerpo 5×1,5×4, 2 ramas 1×2,5×1, 5 dientes inferiores desiguales. Reposo ligeramente abierta (+6°) |
| **Caja torácica** | **10×8×5 u** (referencia). 6 pares de costillas individualizadas: cada costilla = arco lateral (1×1×3,5) + tramo frontal (2,5×1×1) con 8–12° de giro; longitudes y separaciones distintas; la 2.ª izquierda y la 5.ª derecha rotas/más cortas. Esternón de 3 cubos. Hueco central de ≈4×6×3 u para el núcleo. Prohibido «un cubo con textura de costillas» |
| **Columna** | 6 vértebras de 2×1,5×2,5 u (2 lumbares y 4 torácicas) más disco y espolón (8 cubos) y 2 cervicales de 1,5³, con desplazamientos de ±0,5 u. Curvatura total en reposo ≈22° hacia delante; visible desde atrás |
| **Pelvis** | 2 alas ilíacas 3×2,5×4 (giros −6°/+3°), sacro 2×2,5×3, pubis 4×1×2 y 2 fragmentos de fractura (ala izquierda cortada en cuña) |
| **Brazo óseo delgado** (`bone_*`, lado +X) | Cadena clavícula+escápula → húmero 1,5×5,5×1,5 → antebrazo (radio 1×5,5×1 + cúbito 1×5,5×1 + codo 2×1,5×2) → mano (palma 3×1,5×1 + carpos 2×1×1) → **4 dedos** (1×2,5×1; 1×3×1; 1×2,5×1; pulgar 1×2×1). Largo: la mano cuelga por debajo de la rodilla (y ≈ 6,6) |
| **Masa funeraria** (`mass_shoulder`, `dragging_*`, lado −X) | **4 segmentos** + anclaje: hombro-masa (6×4×6 con raíces), `dragging_upper` 5×8×6, `dragging_middle` 6×8×6,5, `dragging_lower` 7×6×7, `dragging_end` base 8×2×8 apoyada en el suelo. Longitud total ≈ **26 u** (referencia). Cada segmento rompe la caja con 2–4 satélites inclinados 10–30° (fémur incrustado, costillas ajenas, cráneo pequeño, vértebras en cadena, raíces, grumos). Musgo ≤ 10 % de la superficie. Más ancha abajo; inclinada hacia atrás; **toca y = 0** en reposo. Más `debris_roots` (5 raíces colgantes) y `debris_chips` (6 fragmentos sueltos) con movimiento secundario |
| **Piernas** | Fémur 2×6×2 + trocánter; tibia 2×6×2 + peroné 1×5×1 + rótula 2,5×2×2,5; pie: talón 2,5×1,5×2,5 + metatarso 2,5×1×3 + 3 falanges 1×1×1,5. La derecha 0,5 u más corta y con la rodilla girada hacia fuera (cojera sutil) |
| **Núcleo** | Cruz de ectoplasma: núcleo 3×3×1,5 + 2 barras 1×2×1 y 2×1×1 + 2 esquirlas 1³. Pulso con escala del hueso `chest_core` |

**Postura de reposo (posada).** Altura del borde superior del cráneo **29 ± 1 u** medida con el modelo en reposo; anchura de hombros 12 u; anchura visual total ≈22 u (masa hasta X ≈ −13); profundidad visual ≈20 u. Pies con la suela en y = 0 (±0,1); `dragging_end` apoyado en y = 0.

## 6. Jerarquía, pivotes y cubos

### 6.1 Nombres de hueso y ejes (D-15 aprobada: silueta original + nombres semánticos)
- **Se conserva la silueta original** (masa a la izquierda de la pantalla en la vista frontal = **−X** del modelo; brazo delgado a la derecha de la pantalla = **+X**).
- **Los huesos asimétricos llevan nombres semánticos, sin lado:** brazo óseo delgado = `bone_shoulder`, `bone_upper_arm`, `bone_forearm`, `bone_hand`, `bone_fingers`; masa funeraria = `mass_shoulder`, `dragging_upper`, `dragging_middle`, `dragging_lower`, `dragging_end`, `debris_roots`, `debris_chips`. Los huesos simétricos (costillas, piernas) usan la **convención anatómica del proyecto** (como Errante, Escorificador y Coloso): `*_left` = **+X**, `*_right` = **−X**. Así no hay contradicción con las demás criaturas.
- **Alias de las referencias** (solo documentación, no son huesos): `arm_right` = cadena `bone_*`; `arm_left_dragging` = cadena `mass_shoulder` + `dragging_*` (así lo etiquetan la hoja de diseño y el pergamino). El código no referencia nombres de hueso.
- Ejes del modelo: +Y arriba, **frente = −Z**, pies en y = 0.
- **Calibración obligatoria de signos al empezar la Fase 0/1** (los signos dependen de Blockbench): rota `spine_upper` +10° en X y comprueba si el pecho se inclina hacia delante; rota `body` +10° en Y y observa hacia qué lado gira el frente; rota `body` +10° en Z y observa hacia qué lado cae la parte superior. Este documento define las rotaciones por su **efecto**: **X+ = inclinar/balancear hacia delante**, **Y+ = girar el frente hacia el lado de la masa (−X)**, **Z+ = inclinar la parte alta hacia el lado de la masa (−X)**. Si Blockbench da el signo contrario en algún eje, **invierte ese eje en TODAS las tablas** (modelo y animaciones). GeckoLib niega X e Y al cargar tanto el modelo como las animaciones (`BakedModelFactory`, `BakedAnimationsAdapter`), de modo que ambos archivos siguen la misma convención de Blockbench.
- **Semántica de las claves:** las rotaciones de animación **se suman** a la rotación de reposo del hueso (`AnimationProcessor`: `setRotX(valor + initialSnapshot.getRotX())`) → **todas las tablas de §10 son deltas** (0 = reposo). No repetir la rotación de reposo dentro de los clips. Las posiciones (P) y escalas (S) también son relativas al reposo.

### 6.2 Jerarquía y pivotes (valores de partida; el pivote final se ajusta con el modelo a la vista y se anota cada cambio)
Aceptación medible: altura posada 28–30 u; suelas y `dragging_end` en y = 0; sin intersecciones involuntarias; cada pivote en la articulación real.

| Hueso | Padre | Pivote [x, y, z] | Reposo (°) [x, y, z] | Función |
|---|---|---|---|---|
| `root` | — | [0, 0, 0] | — | anclaje al suelo |
| `pelvis` | root | [0, 12, 0] | [4, 0, 0] | cadera, balanceo |
| `body` | pelvis | [0, 14.2, 0] | [8, 0, 0] | lumbar (se llama `body` por el contrato V63 y por las referencias) |
| `spine_upper` | body | [0, 16.3, -0.3] | [10, 0, 0] | dorsal, torsión |
| `chest` | spine_upper | [0, 16.8, -0.6] | [0, 0, 0] | contenedor de caja torácica |
| `ribs_left` | chest | [2.5, 20.5, -1.0] | [0, 0, 0] | costillas +X (izquierda anatómica), respiración |
| `ribs_right` | chest | [-2.5, 20.5, -1.0] | [0, 0, 0] | costillas −X (derecha anatómica), respiración |
| `chest_core` | chest | [0, 20.8, -2.4] | [0, 0, 0] | núcleo, pulso (escala) |
| `neck` | chest | [0, 24.6, -1.5] | [18, 0, 0] | cuello |
| `head` | neck | [0, 25.6, -3.2] | [-10, 0, 8] | cráneo (sigue la mirada); inclinado ≈10° hacia el lado delgado |
| `jaw` | head | [0, 26.2, -3.6] | [6, 0, 0] | mandíbula |
| `bone_shoulder` | chest | [5.6, 23.2, -0.8] | [0, 0, 0] | clavícula del brazo delgado (+X) |
| `bone_upper_arm` | bone_shoulder | [5.8, 23.0, -0.8] | [-14, 0, -6] | húmero |
| `bone_forearm` | bone_upper_arm | [5.9, 16.8, -1.2] | [-26, 0, 0] | antebrazo |
| `bone_hand` | bone_forearm | [6.0, 10.8, -1.6] | [-8, 0, 0] | muñeca/mano |
| `bone_fingers` | bone_hand | [6.0, 9.2, -1.8] | [-18, 0, 0] | dedos |
| `mass_shoulder` | chest | [-5.8, 23.0, -0.6] | [0, 0, 0] | anclaje de la masa (−X) |
| `dragging_upper` | mass_shoulder | [-6.4, 22.4, -0.6] | [-12, 0, 5] | masa, tramo 1 |
| `dragging_middle` | dragging_upper | [-7.2, 14.6, 0.2] | [-6, 0, 0] | masa, tramo 2 |
| `dragging_lower` | dragging_middle | [-8.0, 6.8, 0.8] | [-4, 0, 0] | masa, tramo 3 |
| `dragging_end` | dragging_lower | [-8.4, 2.0, 1.2] | [10, 0, 0] | base apoyada en el suelo |
| `debris_roots` | dragging_middle | [-8.2, 12.5, 1.5] | [-10, 0, 6] | raíces colgantes (secundario) |
| `debris_chips` | dragging_lower | [-8.6, 5.5, 2.0] | [0, 0, 0] | fragmentos sueltos (secundario) |
| `thigh_left` | pelvis | [2.8, 11.1, 0] | [14, 0, 0] | muslo +X (izquierda anatómica; 0,5 u más corto) |
| `shin_left` | thigh_left | [2.9, 5.6, 0.8] | [-26, 0, 0] | tibia +X |
| `foot_left` | shin_left | [3.0, 1.4, 0.3] | [12, 0, 0] | pie +X |
| `thigh_right` | pelvis | [-2.8, 11.6, 0] | [22, 0, 0] | muslo −X (derecha anatómica) |
| `shin_right` | thigh_right | [-2.9, 5.8, 0.9] | [-34, 0, 0] | tibia −X |
| `foot_right` | shin_right | [-3.0, 1.5, 0.4] | [12, 0, 0] | pie −X |

29 huesos (28 con geometría). Cada hueso tiene una función visual o de animación. Alias de las referencias: `arm_right` = cadena `bone_*`; `arm_left_dragging` = `mass_shoulder` + `dragging_*`.

### 6.3 Presupuesto de cubos (plan base: **145 cubos**, margen +25; tope 190)
Todas las dimensiones son múltiplos de 0,5 u (= 1 texel a 2 px/u). `material` indica el tratamiento de textura.

| Hueso | Cubos | Detalle (nombre ancho×alto×profundo · material) |
|---|---|---|
| `pelvis` | 6 | ilium_l 3×2.5×4 hueso; ilium_r 3×2.5×4 hueso; sacrum 2×2.5×3 hueso; pubis 4×1×2 hueso; hip_chip_l 1×1×2 hueso; hip_chip_r 1×1.5×1.5 hueso |
| `body` | 3 | lumbar_1 2×1.5×2.5 hueso; lumbar_2 2×1.5×2.5 hueso; disc 1×1×1.5 hueso |
| `spine_upper` | 5 | thor_1 2×1.5×2.5 hueso; thor_2 2×1.5×2.5 hueso; thor_3 2×1.5×2.5 hueso; thor_4 2×1.5×2.5 hueso; spur 1×1×1.5 hueso |
| `chest` | 3 | manubrium 2×2×1 hueso; sternum 1.5×3.5×1 hueso; xiphoid 1×1×0.5 hueso |
| `ribs_left` | 12 | rib1_side 1×1×3.5 hueso; rib1_front 2.5×1×1 hueso; rib2_side 1×1×3.5 hueso; rib2_front 2.5×1×1 hueso; rib3_side 1×1×3.5 hueso; rib3_front 2.5×1×1 hueso; rib4_side 1×1×3.5 hueso; rib4_front 2.5×1×1 hueso; rib5_side 1×1×3.5 hueso; rib5_front 2.5×1×1 hueso; rib6_side 1×1×3.5 hueso; rib6_front 2.5×1×1 hueso |
| `ribs_right` | 12 | rib1_side 1×1×3.5 hueso; rib1_front 2.5×1×1 hueso; rib2_side 1×1×3.5 hueso; rib2_front 2.5×1×1 hueso; rib3_side 1×1×3.5 hueso; rib3_front 2.5×1×1 hueso; rib4_side 1×1×3.5 hueso; rib4_front 2.5×1×1 hueso; rib5_side 1×1×3.5 hueso; rib5_front 2.5×1×1 hueso; rib6_side 1×1×3.5 hueso; rib6_front 2.5×1×1 hueso |
| `chest_core` | 5 | core 3×3×1.5 ecto; halo_v 1×2×1 ecto; halo_h 2×1×1 ecto; shard_a 1×1×1 ecto; shard_b 1×1×1 ecto |
| `neck` | 2 | cerv_1 1.5×1.5×1.5 hueso; cerv_2 1.5×1.5×1.5 hueso |
| `head` | 14 | cranium 7×7×6 hueso; brow 7×1.5×1 hueso; bar_l 2×3.5×1 hueso; bar_r 2×3.5×1 hueso; bridge 1.5×3.5×1 hueso; lower_plate 7×1.5×1 hueso; crack_chip 1×1×2 hueso; cheek_l 1.5×1.5×1.5 hueso; cheek_r 1.5×1.5×1.5 hueso; tooth_u0 1×1×1 hueso; tooth_u1 1×1×1 hueso; tooth_u2 1×1×1 hueso; tooth_u3 1×1×1 hueso; tooth_u4 1×1×1 hueso |
| `jaw` | 8 | mandible 5×1.5×4 hueso; ramus_l 1×2.5×1 hueso; ramus_r 1×2.5×1 hueso; tooth_l0 1×1×1 hueso; tooth_l1 1×1×1 hueso; tooth_l2 1×1×1 hueso; tooth_l3 1×1×1 hueso; tooth_l4 1×1×1 hueso |
| `bone_shoulder` | 2 | clavicle 3×1×1 hueso; scapula 2×2×1 hueso |
| `bone_upper_arm` | 2 | humerus 1.5×5.5×1.5 hueso; knob 2×1.5×2 hueso |
| `bone_forearm` | 3 | radius 1×5.5×1 hueso; ulna 1×5.5×1 hueso; elbow 2×1.5×2 hueso |
| `bone_hand` | 2 | palm 3×1.5×1 hueso; carpals 2×1×1 hueso |
| `bone_fingers` | 4 | f1 1×2.5×1 hueso; f2 1×3×1 hueso; f3 1×2.5×1 hueso; thumb 1×2×1 hueso |
| `mass_shoulder` | 4 | shoulder_mass 6×4×6 tierra; clavicle_exposed 3×1×1 hueso; root_s1 1×3×1 raiz; root_s2 1×3×1 raiz |
| `dragging_upper` | 6 | mass_upper 5×8×6 tierra; femur_embedded 1×7×1 hueso; root_a 1×5×1 raiz; root_b 1×4×1 raiz; moss_clump 2×1×2 musgo/tierra; chunk_u 2×2×2 tierra |
| `dragging_middle` | 7 | mass_middle 6×8×6.5 tierra; rib_emb_a 1×5×1 hueso; rib_emb_b 1×5×1 hueso; bone_chip 1×3×1 hueso; root_c 1×6×1 raiz; mud_lump 3×3×3 tierra; moss_patch 2×1×2.5 musgo/tierra |
| `dragging_lower` | 7 | mass_lower 7×6×7 tierra; skull_small 3×3×3 hueso; vert_a 1.5×1.5×1.5 hueso; vert_b 1.5×1.5×1.5 hueso; vert_c 1.5×1.5×1.5 hueso; root_d 1×5×1 raiz; clump_l 3×2×3 tierra |
| `dragging_end` | 7 | base_slab 8×2×8 tierra; root_toe_a 1×1×4 raiz; root_toe_b 1×1×4 raiz; root_toe_c 1×1×4 raiz; bone_tip 1×1×4 hueso; chunk_e1 2×1.5×2 tierra; chunk_e2 2×1.5×2 tierra |
| `debris_roots` | 5 | droot0 1×4×1 raiz; droot1 1×4×1 raiz; droot2 1×4×1 raiz; droot3 1×4×1 raiz; droot4 1×4×1 raiz |
| `debris_chips` | 6 | chip_b1 1×2×1 hueso; chip_b2 1×2×1 hueso; chip_s1 1.5×1.5×1.5 tierra; chip_s2 1.5×1.5×1.5 tierra; chip_m1 1×1×1 tierra; chip_m2 1×1×1 tierra |
| `thigh_left` | 2 | femur 2×6×2 hueso; trochanter 2×1.5×2 hueso |
| `shin_left` | 3 | tibia 2×6×2 hueso; fibula 1×5×1 hueso; knee 2.5×2×2.5 hueso |
| `foot_left` | 5 | heel 2.5×1.5×2.5 hueso; meta 2.5×1×3 hueso; toe_a 1×1×1.5 hueso; toe_b 1×1×1.5 hueso; toe_c 1×1×1.5 hueso |
| `thigh_right` | 2 | femur 2×6×2 hueso; trochanter 2×1.5×2 hueso |
| `shin_right` | 3 | tibia 2×6×2 hueso; fibula 1×5×1 hueso; knee 2.5×2×2.5 hueso |
| `foot_right` | 5 | heel 2.5×1.5×2.5 hueso; meta 2.5×1×3 hueso; toe_a 1×1×1.5 hueso; toe_b 1×1×1.5 hueso; toe_c 1×1×1.5 hueso |

Resumen: **145 cubos**, 28 huesos con geometría; 97 cubos con dimensión mínima ≤ 1 u; 7 cubos ≥ 3 u. Todo cubo nuevo debe tener una razón visual (grieta, fractura, satélite, desgaste); no rellenar para llegar a una cifra.

## 7. Plan UV

**Decisión técnica:** PNG de **256×256** con `texture_width = texture_height = 128` en el geo (= **2 px por unidad**, 32 px por bloque). Caja UV (`box_uv: true`), una isla propia por cubo.

| Magnitud | Valor |
|---|---|
| Área UV total (plan base, 2 px/u) | **22 742 px² = 34,7 %** de 256² |
| Empaquetado por estantes con 1 px de margen | ocupa 118 de 256 filas |
| Reparto por material (px² a 2 px/u) | hueso ≈ 13 200 · tierra ≈ 7 470 · raíz ≈ 1 400 · ecto ≈ 320 · musgo/tierra ≈ 220 |
| Texeles de alto del mob | 58 (el Errante tiene 29 y el Escorificador 52, a 1 px/u) |
| Alternativas | 256² a 1 px/u (8,6 % de uso, sin ganancia) · 128² a 1 px/u (34,5 %, mitad de detalle) |

**Zonas (guía, 256×256):** cráneo+mandíbula (x 0–96, y 0–64) · costillas/esternón/columna/núcleo (x 96–256, y 0–96) · brazo delgado y piernas (x 0–96, y 64–120) · masa y restos (x 96–256, y 96–200) · reserva y detalle (resto). Mantener **todo cubo del mismo material agrupado** para facilitar la lectura de fibras y el sombreado.

**Reglas UV:** 1 px de margen entre islas; sin solapes involuntarios; **sin espejos** (ni siquiera en costillas: cada lado se pinta distinto); orientación de las fibras de raíz según el eje largo del cubo; densidad uniforme de 2 px/u (la `scale` de textura de Blockbench no se usa para compensar); comprobar que **ninguna dimensión de cubo exija medio píxel**. **Capa emisiva:** `osario_quebrado_glowmask.png` del mismo tamaño; alpha > 0 solo en los píxeles del núcleo (cruz 3×3 u + halo de 1 px, ≈ 60–90 px) y, opcional, 1–2 px tenues en cada cuenca. **El color del brillo se pinta en la textura base** (`#4DE2A6` y `#7FFFD4`, núcleo `#C9FFF0` en 3–6 px); la máscara solo da la intensidad (alpha 255 en el centro, 160 en el halo). Con la capa desactivada, el núcleo debe leerse igual.

**Acabado pixel art (obligatorio):** sin ruido aleatorio como acabado, sin colores planos, sin degradados suaves ni antialiasing, sin texturas fotográficas, sin patrones idénticos repetidos, sin píxeles flotantes, sin costuras UV incorrectas. Revisar en resolución nativa **y** ampliada ×8 con vecino más cercano.

**Tratamiento por material.**
- *Hueso:* ≥ 6 tonos; grietas de 1–2 px de largo variable; esquinas desgastadas (1 px menos de luz); marfil que vira del claro al envejecido; suciedad localizada en uniones; cavidades oscuras (cuencas, interior de costillas); suturas craneales; manchas antiguas; diferencia entre cara externa (más clara) e interna (más oscura).
- *Tierra:* grumos de 2–4 px, sedimentos en bandas, cavidades, motas minerales (1 px claro), diferencias de humedad (zona húmeda más oscura y fría); transición gradual hacia las raíces.
- *Raíz:* fibras **direccionales** siguiendo el eje del cubo, nudos de 2×2 px, fracturas, corteza agrietada.
- *Musgo:* agrupaciones de 3–8 px con 3 niveles de crecimiento (denso/seco/oscuro); en hombro de la masa, juntas húmedas y parte baja de huesos; **nunca** en la cara ni en las cuencas; ≤ 10 % de la masa.
- *Ectoplasma:* núcleo luminoso definido (`#7FFFD4`→`#C9FFF0` en el centro), cuerpo `#4DE2A6`, borde `#2FB985`/`#1E8F6A`; intensidad decreciente por **agrupaciones** de píxeles; contraste con las cavidades óseas; sin ocultar la anatomía.

## 8. Paleta definitiva

Puntos del autor en **negrita**; el resto son rampas derivadas. **El hex escrito manda sobre el color renderizado de las imágenes** (que es más saturado).

| Material | Tonos (luz → sombra) |
|---|---|
| Hueso | `#F0EEDD` · **`#D9D6C3`** · **`#B7B5A6`** · **`#8F8B7C`** · `#5F5B50` · `#3B372F` (cavidad) |
| Tierra / barro | `#6B5642` · **`#4A3B2E`** · `#382C22` · `#2A211A` · `#5A4838` (húmeda) |
| Raíz / madera | `#8A6B4F` · **`#6B4E3A`** · `#4D3828` · `#33261C` |
| Grietas / oscuro | **`#2F221F`** · `#1A1210` |
| Musgo (localizado) | `#5C7142` (seco) · `#4A5F35` (base) · `#3B4D28` (oscuro) |
| Ectoplasma | `#C9FFF0` (3–6 px) · **`#7FFFD4`** · **`#4DE2A6`** · `#2FB985` · `#1E8F6A` (borde) |

Reglas: verde luminoso ≤ 4 % de los píxeles; evitar cian (`#65D6D0` es del Velo/Errante); contraste hueso–cavidad ≥ 4:1 (`#B7B5A6` frente a `#3B372F`); el verde se limita al núcleo, a pocos píxeles de grietas de la masa y a las partículas.

## 9. Catálogo de animaciones

Los tiempos son los propuestos por el autor; se comprobó su viabilidad. Un tick = 0,05 s; las duraciones se ajustan a múltiplos de 0,05 s. Marcador `impact` = tick del daño.

| Clip | Duración | Ticks | Bucle | Impacto | Notas de viabilidad |
|---|---|---|---|---|---|
| `idle` | 3,2 s | 64 | sí | — | reposo largo, irregular; evitar bucle perceptible: tres ritmos distintos (respiración, cabeceo, mandíbula) |
| `walk` | 1,2 s | 24 | sí | — | contactos en 0 y 0,6 s; la masa arrastra con retraso |
| `chase` | 0,85 s | 17 | sí | — | contactos en 0 y 0,425 s; se elige con `isAttacking()` y movimiento (histéresis ≥ 10 ticks) |
| `alert` | 0,65 s | 13 | no | — | al fijar objetivo por cualquier vía (incluida la llamada de auxilio V83) |
| `claw_swipe` | 0,90 s | 18 | no | **t10** (0,50 s) | brazo delgado, corto |
| `heavy_swipe` | 1,40 s | 28 | no | **t19** (0,95 s) | brazo delgado en alto + torsión; **conserva el id** |
| `ground_slam` | 1,80 s | 36 | no | **t23** (1,15 s) | masa levantada y estampada |
| `lunge` | 1,35 s | 27 | no | primer contacto en t14–t21 | dash de 7 ticks (0,70–1,05 s) |
| `hurt` | 0,40 s | 8 | no | — | no se reproduce en aviso/impacto de ataque |
| `death` | 0,95 s | 19 | no (`hold_on_last_frame`) | — | < 20 ticks (retirada vanilla); `getDeathMaxRotation = 0` como el Errante |

Reglas generales: transferencia de peso en cada acción (pelvis y pies), anticipación, aceleración (`ease-in`) hacia el impacto, desaceleración (`ease-out`) tras él, seguimiento y sobreoscilación controlada (≤ 15 %), arcos en brazos (nunca líneas rectas), movimiento secundario en `dragging_*`, `debris_*` y `bone_fingers` con **regla de retraso: cada hijo copia la curva de su padre desplazada +0,10–0,15 s y con ×1,25 de amplitud**. **Nada de interpolación lineal en idle/walk/chase.** Sin vibraciones ni saltos: comprobar que el primer y el último fotograma de los bucles coinciden. Contacto con el suelo: tras cada clip, ningún vértice de pie, masa o cuerpo por debajo de y = 0 salvo la muerte final apoyada; calcular con transformaciones globales (posición de pivote + rotación acumulada de la cadena), no por hueso aislado.
## 10. Cronogramas de keyframes (deltas sobre el reposo)

**Notación.** `R` = rotación en grados `[x, y, z]`; `P` = posición en unidades `[x, y, z]`; `S` = escala (multiplicador). Cada celda es `tiempo(s): valor`. **Todo valor es un delta**: 0 = pose de reposo (§6.2). Efecto de los signos (calibrar en la Fase 1, §6.1): **X+ = hacia delante · Y+ = el frente gira hacia el lado de la masa (−X) · Z+ = la parte alta cae hacia el lado de la masa (−X)**. Huesos no listados en un clip = sin claves (heredan). Interpolación: `smooth`/`catmull` en bucles; `ease-in` hacia el impacto; `ease-out` tras él; lineal solo en el tramo de impacto de 1–2 ticks. Estas curvas son el **punto de partida**: Codex debe refinarlas viendo el modelo y documentar cada cambio.

### 10.1 `idle` — 3,2 s, bucle (64 ticks)
Respiración irregular, cabeza ladeada, mandíbula, cambio de peso, masa reactiva con retraso. Claves en 0 · 1,05 · 1,75 · 2,65 · 3,2 (el último = el primero).
| Hueso | Canal | Valores |
|---|---|---|
| pelvis | R | 0:[0,0,0] · 1.05:[-1,0,1.5] · 1.75:[0,0,0] · 2.65:[-1,0,-1.5] · 3.2:[0,0,0] |
| pelvis | P | 0:[0,0,0] · 1.05:[0,0.2,0] · 1.75:[0,0,0] · 2.65:[0,0.15,0] · 3.2:[0,0,0] |
| body | R | 0:[0,0,0] · 1.05:[1.5,0,-1] · 1.75:[0,0,0] · 2.65:[1,0,1] · 3.2:[0,0,0] |
| spine_upper | R | 0:[0,0,0] · 1.05:[2,2,0] · 1.75:[0,0,0] · 2.65:[1.5,-2,0] · 3.2:[0,0,0] |
| chest | R | 0:[0,0,0] · 1.05:[1,0,0] · 1.75:[0,0,0] · 2.65:[0.6,0,0] · 3.2:[0,0,0] |
| ribs_left / ribs_right | P | 0:[0,0,0] · 1.05:[±0.25,0,-0.2] · 1.75:[0,0,0] · 2.65:[±0.15,0,-0.1] · 3.2:[0,0,0] (se abren al inspirar) |
| chest_core | S | 0:1 · 0.6:1.12 · 1.2:1 · 2.0:1.08 · 2.7:1 · 3.2:1 (pulso irregular) |
| neck | R | 0:[0,0,0] · 1.05:[3,0,-2] · 1.75:[0,0,0] · 2.65:[2,0,-3] · 3.2:[0,0,0] |
| head | R | 0:[0,0,0] · 1.05:[-2,-5,2] · 1.75:[0,0,0] · 2.65:[-4,6,3] · 3.2:[0,0,0] |
| jaw | R | 0:[0,0,0] · 1.3:[7,0,0] · 1.5:[0,0,0] · 2.9:[4,0,0] · 3.2:[0,0,0] |
| bone_upper_arm | R | 0:[0,0,0] · 1.05:[-3,0,1.5] · 1.75:[0,0,0] · 2.65:[-2,0,1] · 3.2:[0,0,0] |
| bone_forearm | R | 0:[0,0,0] · 1.05:[-4,0,0] · 1.75:[0,0,0] · 2.65:[-3,0,0] · 3.2:[0,0,0] |
| bone_hand | R | 0:[0,0,0] · 1.2:[-5,0,0] · 2.0:[0,0,0] · 2.8:[2,0,0] · 3.2:[0,0,0] |
| bone_fingers | R | 0:[0,0,0] · 1.4:[-8,0,0] · 1.7:[0,0,0] · 2.9:[-4,0,0] · 3.2:[0,0,0] (espasmo) |
| dragging_upper | R | 0:[0,0,0] · 1.2:[2,0,0] · 1.9:[0,0,0] · 2.8:[1.5,0,0] · 3.2:[0,0,0] (retraso 0,15 s) |
| dragging_middle | R | 0:[0,0,0] · 1.35:[2.5,0,0] · 2.05:[0,0,0] · 2.95:[2,0,0] · 3.2:[0,0,0] |
| dragging_lower | R | 0:[0,0,0] · 1.5:[3,0,0] · 2.2:[0,0,0] · 3.1:[2,0,0] · 3.2:[0,0,0] |
| dragging_end | R | 0:[0,0,0] · 1.5:[2,0,0] · 2.2:[0,0,0] · 3.1:[1,0,0] · 3.2:[0,0,0] (P y siempre 0) |
| debris_roots | R | 0:[0,0,0] · 1.5:[0,0,6] · 2.4:[0,0,-5] · 3.2:[0,0,0] |
| debris_chips | P | 0:[0,0,0] · 1.8:[0,0.2,0] · 2.4:[0,0,0] · 3.2:[0,0,0] |
| thigh_left / thigh_right | R | left 0:[0,0,0] · 1.05:[1,0,0] · 3.2:[0,0,0]; right 0:[0,0,0] · 2.65:[-1,0,0] · 3.2:[0,0,0] (peso alterno) |

### 10.2 `walk` — 1,2 s, bucle (24 ticks; contactos en 0 y 0,6 s)
Claves en 0 · 0,3 · 0,6 · 0,9 · 1,2. Marcha asimétrica, torsión del torso, masa arrastrando con retraso.
| Hueso | Canal | Valores |
|---|---|---|
| root | P | 0:[0,0,0] · 0.3:[0,0.5,0] · 0.6:[0,0,0] · 0.9:[0,0.5,0] · 1.2:[0,0,0] |
| pelvis | R | 0:[0,6,3] · 0.6:[0,-6,-3] · 1.2:[0,6,3] |
| body | R | 0:[0,-4,3] · 0.6:[0,4,-3] · 1.2:[0,-4,3] |
| spine_upper | R | 0:[0,-6,2] · 0.3:[3,0,0] · 0.6:[0,6,-2] · 0.9:[3,0,0] · 1.2:[0,-6,2] |
| head | R | 0:[0,5,0] · 0.3:[2,0,0] · 0.6:[0,-5,0] · 0.9:[2,0,0] · 1.2:[0,5,0] |
| thigh_left | R | 0:[30,0,0] · 0.3:[3,0,0] · 0.6:[-24,0,0] · 0.9:[8,0,0] · 1.2:[30,0,0] |
| shin_left | R | 0:[10,0,0] · 0.3:[8,0,0] · 0.6:[12,0,0] · 0.9:[42,0,0] · 1.2:[10,0,0] |
| foot_left | R | 0:[-8,0,0] · 0.3:[0,0,0] · 0.6:[20,0,0] · 0.9:[5,0,0] · 1.2:[-8,0,0] |
| thigh_right | R | 0:[-24,0,0] · 0.3:[8,0,0] · 0.6:[30,0,0] · 0.9:[3,0,0] · 1.2:[-24,0,0] |
| shin_right | R | 0:[12,0,0] · 0.3:[42,0,0] · 0.6:[10,0,0] · 0.9:[8,0,0] · 1.2:[12,0,0] |
| foot_right | R | 0:[20,0,0] · 0.3:[5,0,0] · 0.6:[-8,0,0] · 0.9:[0,0,0] · 1.2:[20,0,0] |
| bone_upper_arm | R | 0:[18,0,0] · 0.6:[-22,0,0] · 1.2:[18,0,0] (contrafase con thigh_right) |
| bone_forearm | R | 0:[-6,0,0] · 0.3:[-10,0,0] · 0.6:[-22,0,0] · 0.9:[-12,0,0] · 1.2:[-6,0,0] |
| bone_hand, bone_fingers | R | regla de retraso 0,12 s sobre bone_forearm, ×1,25 |
| mass_shoulder | R | 0:[0,3,0] · 0.6:[0,-3,0] · 1.2:[0,3,0] |
| dragging_upper | R | 0:[3,4,0] · 0.3:[-2,0,0] · 0.6:[3,-4,0] · 0.9:[-2,0,0] · 1.2:[3,4,0] (con retraso de 0,15 s sobre `body`) |
| dragging_middle/lower | R | regla de retraso (+0,12 s por tramo, ×1,25) |
| dragging_end | R/P | R x: 0:[0] · 0.3:[-4] · 0.6:[0] · 0.9:[-4] · 1.2:[0]; **P y siempre 0**; P z: 0:0 · 0.6:+1 · 1.2:0 (arrastre) |
| debris_roots, debris_chips | R/P | oscilación ±8° con retraso 0,3 s; chips P y +0,3 en el paso |

### 10.3 `chase` — 0,85 s, bucle (17 ticks; contactos en 0 y 0,425 s)
Más inclinado y rápido, sin perder peso. Claves en 0 · 0,2125 · 0,425 · 0,6375 · 0,85.
| Hueso | Canal | Valores |
|---|---|---|
| root | P | 0:[0,0,0] · 0.2125:[0,0.9,0] · 0.425:[0,0,0] · 0.6375:[0,0.9,0] · 0.85:[0,0,0] |
| pelvis | R | 0:[3,8,4] · 0.425:[3,-8,-4] · 0.85:[3,8,4] |
| body | R | 0:[8,-6,4] · 0.425:[8,6,-4] · 0.85:[8,-6,4] (inclinación adicional de +8°) |
| spine_upper | R | 0:[6,-9,2] · 0.425:[6,9,-2] · 0.85:[6,-9,2] |
| head | R | 0:[-8,6,0] · 0.2125:[-6,0,0] · 0.425:[-8,-6,0] · 0.85:[-8,6,0] (mira al objetivo) |
| jaw | R | 0:[10,0,0] · 0.2125:[16,0,0] · 0.425:[10,0,0] · 0.6375:[16,0,0] · 0.85:[10,0,0] |
| thigh_left | R | 0:[40,0,0] · 0.2125:[4,0,0] · 0.425:[-32,0,0] · 0.6375:[12,0,0] · 0.85:[40,0,0] |
| shin_left | R | 0:[14,0,0] · 0.2125:[10,0,0] · 0.425:[16,0,0] · 0.6375:[56,0,0] · 0.85:[14,0,0] |
| foot_left | R | 0:[-10,0,0] · 0.425:[26,0,0] · 0.6375:[6,0,0] · 0.85:[-10,0,0] |
| thigh_right | R | 0:[-32,0,0] · 0.2125:[12,0,0] · 0.425:[40,0,0] · 0.6375:[4,0,0] · 0.85:[-32,0,0] |
| shin_right | R | 0:[16,0,0] · 0.2125:[56,0,0] · 0.425:[14,0,0] · 0.6375:[10,0,0] · 0.85:[16,0,0] |
| foot_right | R | 0:[26,0,0] · 0.2125:[6,0,0] · 0.425:[-10,0,0] · 0.85:[26,0,0] |
| bone_upper_arm | R | 0:[34,0,0] · 0.425:[-30,0,0] · 0.85:[34,0,0] |
| bone_forearm | R | 0:[-10,0,0] · 0.425:[-34,0,0] · 0.85:[-10,0,0] |
| dragging_upper | R | 0:[6,6,0] · 0.2125:[-6,0,0] · 0.425:[6,-6,0] · 0.6375:[-6,0,0] · 0.85:[6,6,0] (retraso 0,10 s) |
| dragging_middle/lower | R | regla de retraso (+0,10 s, ×1,3) |
| dragging_end | P/R | **la masa se despega ≤ 0,8 u en el vuelo y vuelve a rozar en cada contacto**: P y 0:[0] · 0.2125:[0.8] · 0.425:[0] · 0.6375:[0.8] · 0.85:[0]; R x 0:[0] · 0.2125:[-8] · 0.425:[0] |
| debris_roots, debris_chips | R/P | ±12° con retraso 0,25 s; chips P y +0,5 |

### 10.4 `alert` — 0,65 s (13 ticks)
Cabeza arriba, cuello tenso, mandíbula abierta, núcleo reacciona, se prepara la postura de combate. Claves en 0 · 0,15 · 0,3 · 0,45 · 0,65.
| Hueso | Canal | Valores |
|---|---|---|
| pelvis | P | 0:[0,0,0] · 0.15:[0,0.4,0] · 0.3:[0,0,0] · 0.45:[0,-0.6,0] · 0.65:[0,-0.3,0] |
| body | R | 0:[0,0,0] · 0.15:[-6,0,0] · 0.3:[-4,0,0] · 0.45:[10,0,0] · 0.65:[6,0,0] |
| neck | R | 0:[0,0,0] · 0.15:[-8,0,0] · 0.3:[-6,0,0] · 0.45:[0,0,0] · 0.65:[0,0,0] |
| head | R | 0:[0,0,0] · 0.15:[-14,0,0] · 0.3:[-10,0,0] · 0.45:[-6,0,0] · 0.65:[0,0,0] |
| jaw | R | 0:[0,0,0] · 0.15:[24,0,0] · 0.3:[24,0,0] · 0.45:[10,0,0] · 0.65:[4,0,0] |
| chest_core | S | 0:1 · 0.15:1.35 · 0.3:1.2 · 0.45:1.1 · 0.65:1 |
| bone_upper_arm | R | 0:[0,0,0] · 0.15:[0,0,14] · 0.3:[0,0,12] · 0.45:[0,0,6] · 0.65:[0,0,0] |
| bone_fingers | R | 0:[0,0,0] · 0.15:[-14,0,0] · 0.45:[-6,0,0] · 0.65:[0,0,0] |
| dragging_upper | R | 0:[0,0,0] · 0.15:[8,0,0] · 0.3:[6,0,0] · 0.45:[0,0,0] · 0.65:[0,0,0] (retraso 0,1 s) |
| thigh_left / thigh_right | R | 0:[0,0,0] · 0.45:[10,0,0] · 0.65:[6,0,0] (se agacha) |

### 10.5 `claw_swipe` — 0,90 s (18 ticks), impacto en 0,50 s (t10)
Brazo delgado: anticipación (0–0,35 s), acción (0,35–0,50 s), seguimiento (0,50–0,70 s), recuperación (0,70–0,90 s). Torsión de torso, peso hacia delante.
| Hueso | Canal | Valores |
|---|---|---|
| pelvis | R | 0:[0,0,0] · 0.35:[0,-10,0] · 0.5:[0,14,0] · 0.7:[0,10,0] · 0.9:[0,0,0] |
| pelvis | P | 0:[0,0,0] · 0.35:[0,-0.4,0] · 0.5:[0,-0.2,-0.6] · 0.9:[0,0,0] |
| body | R | 0:[0,0,0] · 0.12:[0,-8,0] · 0.35:[0,-20,0] · 0.5:[6,26,0] · 0.7:[4,22,0] · 0.9:[0,0,0] |
| spine_upper | R | 0:[0,0,0] · 0.35:[0,-14,0] · 0.5:[5,18,0] · 0.7:[3,14,0] · 0.9:[0,0,0] |
| head | R | 0:[0,0,0] · 0.35:[-4,8,0] · 0.5:[-6,-10,0] · 0.9:[0,0,0] (sigue al objetivo) |
| jaw | R | 0:[0,0,0] · 0.35:[10,0,0] · 0.5:[22,0,0] · 0.9:[0,0,0] |
| bone_shoulder | R | 0:[0,0,0] · 0.35:[10,0,12] · 0.5:[-10,0,0] · 0.9:[0,0,0] |
| bone_upper_arm | R | 0:[0,0,0] · 0.12:[28,0,8] · 0.35:[70,0,25] · 0.5:[-65,0,0] · 0.62:[-75,0,0] · 0.9:[0,0,0] |
| bone_forearm | R | 0:[0,0,0] · 0.35:[-20,0,0] · 0.5:[-55,0,0] · 0.9:[0,0,0] |
| bone_hand | R | 0:[0,0,0] · 0.35:[10,0,0] · 0.5:[-15,0,0] · 0.9:[0,0,0] |
| bone_fingers | R | 0:[0,0,0] · 0.35:[-25,0,0] (abiertos) · 0.5:[-5,0,0] (garra) · 0.9:[0,0,0] |
| dragging_upper | R | 0:[0,0,0] · 0.35:[0,-8,0] · 0.5:[0,10,0] · 0.7:[0,6,0] · 0.9:[0,0,0] (contrapeso con retraso 0,1 s) |
| thigh_left / thigh_right | R | left 0:[0] · 0.35:[10] · 0.5:[22] · 0.9:[0]; right 0:[0] · 0.35:[-14] · 0.5:[-24] · 0.9:[0] (paso de apoyo) |

### 10.6 `heavy_swipe` — 1,40 s (28 ticks), impacto en 0,95 s (t19)
Brazo delgado **por encima de la cabeza** y barrido en arco bajo con estela verde (partículas). Anticipación 0–0,80 s, impacto 0,80–0,95 s, seguimiento 0,95–1,15 s, recuperación 1,15–1,40 s.
| Hueso | Canal | Valores |
|---|---|---|
| root | P | 0:[0,0,0] · 0.8:[0,-0.3,0.4] · 0.95:[0,-0.2,-0.8] · 1.4:[0,0,0] |
| pelvis | R | 0:[0,0,0] · 0.25:[0,-6,0] · 0.8:[0,-10,0] · 0.95:[0,20,0] · 1.15:[0,24,0] · 1.4:[0,0,0] |
| body | R | 0:[0,0,0] · 0.25:[0,-18,0] · 0.8:[-4,-32,0] · 0.95:[12,48,0] · 1.15:[10,62,0] · 1.4:[0,0,0] |
| spine_upper | R | 0:[0,0,0] · 0.25:[0,-22,0] · 0.8:[-6,-38,0] · 0.95:[10,42,0] · 1.15:[8,50,0] · 1.4:[0,0,0] |
| chest | R | 0:[0,0,0] · 0.8:[-6,0,0] · 0.95:[6,0,0] · 1.4:[0,0,0] |
| head | R | 0:[0,0,0] · 0.8:[-6,20,0] · 0.95:[-4,-10,0] · 1.4:[0,0,0] |
| jaw | R | 0:[0,0,0] · 0.8:[26,0,0] · 0.95:[10,0,0] · 1.4:[0,0,0] (grito) |
| chest_core | S | 0:1 · 0.8:1.3 · 0.95:1.1 · 1.4:1 |
| bone_shoulder | R | 0:[0,0,0] · 0.25:[0,0,10] · 0.8:[-20,0,18] · 0.95:[10,0,0] · 1.4:[0,0,0] |
| bone_upper_arm | R | 0:[0,0,0] · 0.25:[110,0,30] · 0.8:[140,0,35] · 0.95:[-40,0,-10] · 1.15:[-70,0,-10] · 1.4:[0,0,0] |
| bone_forearm | R | 0:[0,0,0] · 0.25:[-10,0,0] · 0.8:[-25,0,0] · 0.95:[-45,0,0] · 1.15:[-30,0,0] · 1.4:[0,0,0] |
| bone_hand | R | 0:[0,0,0] · 0.8:[-20,0,0] · 0.95:[-20,0,0] · 1.4:[0,0,0] |
| bone_fingers | R | 0:[0,0,0] · 0.8:[-28,0,0] (abiertos) · 0.95:[-4,0,0] · 1.4:[0,0,0] |
| dragging_upper | R | 0:[0,0,0] · 0.8:[0,14,0] · 0.95:[0,6,0] · 1.1:[0,-22,0] (la masa se balancea al contrario) · 1.4:[0,0,0] (retraso 0,12 s) |
| dragging_middle/lower | R | regla de retraso (+0,12 s, ×1,25) |
| dragging_end | P/R | P y 0 en todo el clip; el arrastre se desplaza P x ∓1 según el giro del torso |
| debris_roots, debris_chips | R/P | 0.95:[0,0,±20] / [0,+2,0] (salen disparados) · 1.4:[0] |
| thigh_left | R | 0:[0] · 0.8:[10] · 0.95:[18] · 1.4:[0] |
| thigh_right | R | 0:[0] · 0.8:[-18] · 0.95:[-26] · 1.4:[0] (pivota sobre el pie derecho) |

### 10.7 `ground_slam` — 1,80 s (36 ticks), impacto en 1,15 s (t23)
La **masa** se levanta sobre el hombro y se estampa delante. Anticipación 0–0,90 s (arqueo y elevación), impacto 0,90–1,15 s, rebote/recuperación 1,15–1,80 s. Alcance horizontal del extremo de la masa en el impacto ≈ 1,5 bl (hombro adelantado 5 u + brazo a ≈49° de la vertical).
| Hueso | Canal | Valores |
|---|---|---|
| pelvis | P | 0:[0,0,0] · 0.3:[0,-1.0,0] · 0.9:[0,-0.4,0.6] · 1.15:[0,-1.4,-1.2] · 1.45:[0,-0.6,-0.4] · 1.8:[0,0,0] |
| body | R | 0:[0,0,0] · 0.3:[10,0,0] · 0.9:[-14,0,0] · 1.15:[34,0,0] · 1.45:[14,0,0] · 1.8:[0,0,0] |
| spine_upper | R | 0:[0,0,0] · 0.9:[-10,0,0] · 1.15:[22,0,0] · 1.45:[8,0,0] · 1.8:[0,0,0] |
| head | R | 0:[0,0,0] · 0.9:[-20,0,0] · 1.15:[14,0,0] · 1.45:[4,0,0] · 1.8:[0,0,0] |
| jaw | R | 0:[0,0,0] · 0.9:[28,0,0] · 1.15:[6,0,0] · 1.8:[0,0,0] |
| chest_core | S | 0:1 · 0.3:1.25 · 0.9:1.35 · 1.15:1.1 · 1.8:1 |
| mass_shoulder | R | 0:[0,0,0] · 0.3:[0,0,-6] · 0.9:[0,0,-20] · 1.15:[0,0,-4] · 1.8:[0,0,0] |
| dragging_upper | R | 0:[0,0,0] · 0.3:[-8,0,0] · 0.9:[100,0,0] · 1.15:[-35,0,0] (ver nota) · 1.45:[-30,0,0] · 1.8:[0,0,0] |
| dragging_middle | R | 0:[0,0,0] · 0.9:[25,0,0] · 1.15:[-8,0,0] · 1.8:[0,0,0] (retraso 0,10 s) |
| dragging_lower | R | 0:[0,0,0] · 0.9:[20,0,0] · 1.15:[-10,0,0] · 1.8:[0,0,0] (retraso 0,20 s) |
| dragging_end | R/P | R x 0:[0] · 0.9:[-15] · 1.15:[+22] · 1.8:[0]; en 1.15 la base toca y = 0 (**P y ajustado para que el vértice más bajo = 0**); rebote P y +0.5 en 1.25 |
| bone_upper_arm | R | 0:[0,0,0] · 0.9:[-60,0,40] · 1.15:[20,0,10] · 1.8:[0,0,0] (equilibrio) |
| thigh_left / thigh_right | R | 0:[0] · 0.3:[20] · 0.9:[10] · 1.15:[28] · 1.8:[0] (ambos; flexión de apoyo) |
| shin_left / shin_right | R | 0:[0] · 0.3:[30] · 1.15:[40] · 1.8:[0] |
| debris_roots, debris_chips | R/P | 1.15:[0,0,±25] / [0,+2.5,0] (salpican) · 1.8:[0] |
> **Nota `dragging_upper` en 1,15 s:** es un delta sobre el reposo (−12° en X); el valor debe dejar **el extremo de la masa en el suelo delante del mob** (comprobarlo con transformaciones globales). Si no llega, ajustar `dragging_upper` (±15°) y `body` (±6°) hasta tocar y = 0 sin atravesarlo.

### 10.8 `lunge` — 1,35 s (27 ticks): anticipación 0–0,70 s · dash 0,70–1,05 s (7 ticks) · frenado 1,05–1,35 s
El desplazamiento real lo hace el servidor (§11); la animación aporta inclinación y zancada. Claves en 0 · 0,35 · 0,70 · 0,875 · 1,05 · 1,35.
| Hueso | Canal | Valores |
|---|---|---|
| pelvis | P | 0:[0,0,0] · 0.35:[0,-0.8,0.3] · 0.7:[0,-1.2,0.6] · 0.875:[0,-0.6,-0.4] · 1.05:[0,-0.8,-0.2] · 1.35:[0,0,0] |
| body | R | 0:[0,0,0] · 0.35:[8,0,0] · 0.7:[16,0,0] · 0.875:[30,0,0] · 1.05:[20,0,0] · 1.35:[0,0,0] |
| spine_upper | R | 0:[0,0,0] · 0.7:[12,0,0] · 0.875:[20,0,0] · 1.05:[14,0,0] · 1.35:[0,0,0] |
| head | R | 0:[0,0,0] · 0.7:[-14,0,0] · 0.875:[-10,0,0] · 1.35:[0,0,0] |
| jaw | R | 0:[0,0,0] · 0.7:[25,0,0] · 0.875:[30,0,0] · 1.05:[8,0,0] · 1.35:[0,0,0] (rugido) |
| chest_core | S | 0:1 · 0.7:1.3 · 0.875:1.15 · 1.35:1 |
| bone_upper_arm | R | 0:[0,0,0] · 0.35:[30,0,10] · 0.7:[50,0,15] · 0.875:[-60,0,0] · 1.05:[-40,0,0] · 1.35:[0,0,0] |
| bone_forearm | R | 0:[0,0,0] · 0.7:[-20,0,0] · 0.875:[-50,0,0] · 1.35:[0,0,0] |
| bone_fingers | R | 0:[0,0,0] · 0.7:[-24,0,0] · 0.875:[-6,0,0] · 1.35:[0,0,0] |
| thigh_left | R | 0:[0] · 0.7:[28] · 0.77:[45] · 0.875:[-30] · 0.98:[40] · 1.05:[20] · 1.35:[0] (zancada alterna rápida) |
| thigh_right | R | 0:[0] · 0.7:[26] · 0.77:[-40] · 0.875:[38] · 0.98:[-34] · 1.05:[18] · 1.35:[0] |
| shin_left / shin_right | R | 0:[0] · 0.7:[40] · 0.875:[50] · 1.05:[30] · 1.35:[0] |
| dragging_upper | R | 0:[0,0,0] · 0.35:[-14,0,0] · 0.7:[-30,0,0] · 0.875:[-35,0,0] · 1.05:[18,0,0] · 1.35:[0,0,0] (la masa va atrás y luego adelante por inercia) |
| dragging_middle/lower | R | regla de retraso (+0,12 s, ×1,3) |
| dragging_end | P | 0.875:[0,1.0,0] (rozando/levantando escombro) · 1.05:[0,0,0] |
| debris_roots, debris_chips | R/P | ±15° / +0,6 con retraso 0,25 s |

### 10.9 `hurt` — 0,40 s (8 ticks)
Reacción breve que **no** destruye la continuidad: no se reproduce durante aviso o impacto de un ataque. Claves en 0 · 0,08 · 0,2 · 0,4.
| Hueso | Canal | Valores |
|---|---|---|
| body | R | 0:[0,0,0] · 0.08:[-8,0,0] · 0.2:[3,0,0] · 0.4:[0,0,0] |
| spine_upper | R | 0:[0,0,0] · 0.08:[-6,0,0] · 0.4:[0,0,0] |
| head | R | 0:[0,0,0] · 0.08:[-14,10,0] · 0.2:[4,-3,0] · 0.4:[0,0,0] |
| jaw | R | 0:[0,0,0] · 0.08:[18,0,0] · 0.2:[5,0,0] · 0.4:[0,0,0] |
| chest_core | S | 0:1 · 0.08:0.8 · 0.2:1.1 · 0.4:1 |
| bone_upper_arm | R | 0:[0,0,0] · 0.08:[10,0,0] · 0.2:[-4,0,0] · 0.4:[0,0,0] |
| dragging_upper | R | 0:[0,0,0] · 0.08:[10,0,0] · 0.2:[-4,0,0] · 0.4:[0,0,0] |
| debris_chips | P | 0:[0,0,0] · 0.08:[0,0.6,0] · 0.4:[0,0,0] |

### 10.10 `death` — 0,95 s (19 ticks), `hold_on_last_frame`
Pérdida de cohesión: se apaga el núcleo, las piernas se pliegan, el cuerpo cae hacia delante de rodillas con el torso ≈ horizontal, la masa descansa en el suelo. Sin desprender geometría (no existe sistema para ello). Claves en 0 · 0,2 · 0,45 · 0,7 · 0,95.
| Hueso | Canal | Valores |
|---|---|---|
| pelvis | P | 0:[0,0,0] · 0.2:[0,-0.4,0] · 0.45:[0,-2.5,0.5] · 0.7:[0,-5,1] · 0.95:[0,-6.5,1.4] |
| thigh_left / thigh_right | R | 0:[0] · 0.2:[10] · 0.45:[45] · 0.7:[70] · 0.95:[78] (pliegan la rodilla) |
| shin_left / shin_right | R | 0:[0] · 0.2:[-10] · 0.45:[-60] · 0.7:[-90] · 0.95:[-100] (ajustar para apoyar el pie en y = 0) |
| foot_left / foot_right | R | 0:[0] · 0.7:[20] · 0.95:[30] |
| body | R | 0:[0,0,0] · 0.2:[16,0,0] · 0.45:[38,0,0] · 0.7:[55,0,0] · 0.95:[62,0,6] |
| spine_upper | R | 0:[0,0,0] · 0.45:[20,0,0] · 0.95:[40,0,-4] |
| head | R | 0:[0,0,0] · 0.2:[24,0,0] · 0.7:[40,0,6] · 0.95:[44,0,12] |
| jaw | R | 0:[0,0,0] · 0.2:[30,0,0] · 0.95:[40,0,0] (queda caída) |
| chest_core | S/P | S 0:1 · 0.2:0.9 · 0.45:0.6 · 0.7:0.35 · 0.95:0.2; P y 0:0 · 0.45:-1 · 0.95:-3 (desciende y se apaga) |
| ribs_left / ribs_right | P | 0:[0,0,0] · 0.45:[0,-0.5,0] · 0.95:[0,-1.2,0] (aflojan) |
| bone_upper_arm | R | 0:[0,0,0] · 0.45:[20,0,0] · 0.95:[35,0,10] (cuelga inerte) |
| dragging_upper | R | 0:[0,0,0] · 0.7:[10,0,0] · 0.95:[25,0,0] (sigue tocando el suelo según baja el hombro) |
| dragging_middle/lower | R | 0.95:[10,0,0] y [8,0,0]; `dragging_end` apoyada, P y ajustado para vértice mínimo = 0 |
| debris_roots, debris_chips | R/P | caen: R z ±30 en 0.95; chips P y −0,5 hasta apoyar en el suelo |
**Comprobación final:** ningún vértice por debajo de y = −0,2 u; la caja del cuerpo no sobresale del suelo; el cadáver ocupa menos de 1,4 × 1,4 bloques.

## 11. Matriz de los cuatro ataques

Base [V]: vida 24, daño 5 (`GENERIC_ATTACK_DAMAGE`), velocidad 0,27, seguimiento 24. **No se cambian.** Los multiplicadores actúan sobre el atributo vigente (que ya incluye afijos de élite y la Amenaza V83). Daño: `target.damage(getDamageSources().mobAttack(this), atributo × mult)` en el **tick de impacto** (nunca al terminar la animación). `s` = escala de la entidad (`getScale()`: 1,0 común; 1,35 élite «giant»); alcances y radios se multiplican por `s`.

| Campo | `claw_swipe` Zarpazo esquelético | `heavy_swipe` Barrido sepulcral | `ground_slam` Impacto del osario | `lunge` Embestida quebrada |
|---|---|---|---|---|
| Propósito | presionar a corta distancia, castigar el retroceso corto | castigar al que se queda cerca y circula | control de zona: golpea a quien rodea o se queda a un lado | cerrar distancia y castigar al que huye en línea recta |
| Miembro | brazo delgado | brazo delgado (en alto) + torsión | masa funeraria | cuerpo entero (+ garra) |
| Duración total | 18 t | 28 t | 36 t | 27 t |
| Anticipación hasta el impacto | 10 t | 19 t | 23 t | dash t14–t21 (inicio de contacto en t14) |
| Señal de aviso adicional (partículas+sonido) | t4 | t8 | t10 | t4 |
| Tick de impacto | t10 | t19 | t23 | primer solape de la cápsula (t14–t21) |
| Ventana activa | t10–t11 | t19–t21 | t23–t24 | durante el dash |
| Recuperación (hasta fin de clip) | 7 t | 7 t | 12 t | 6 t |
| Enfriamiento (empieza al **terminar** la acción) | 24 t | 70 t | **200 t común / 120 t élite-campeón** | 160 t |
| Daño | ×0,9 | ×1,3 | ×1,6 | ×1,0 |
| Alcance (desde el centro del mob) | 2,0 | 2,7 | disco de radio 2,1 centrado a 1,5 delante | cápsula de ≈3,0 b de recorrido, radio 0,55 |
| Forma | sector 60° (±30°) | sector 160° (±80°) | **disco desplazado** (volumen circular, no sector) | **cápsula barrida** a lo largo del dash |
| Banda vertical (pies = 0) | 0,2–1,7 | 0,0–1,7 | 0,0–1,4 | 0,0–1,8 |
| Retroceso | 0,35 horizontal | 0,6 + 0,15 vertical | 0,5 radial + 0,3 vertical (lanza) | 0,8 hacia delante; se detiene el dash al golpear |
| Activación | objetivo a ≤ 2,0 con línea de visión; mirando ≤ 40° | objetivo a ≤ 2,7; peso mayor si el objetivo circula | objetivo a ≤ 2,4; común: peso bajo; élite/campeón: peso alto; más peso si ≥ 2 jugadores a ≤ 3 b | objetivo a 3,2–6,5 b, \|Δy\| ≤ 1,2, camino libre (barrido de caja), no en agua, ≥ 40 t persiguiendo, enfriamiento listo |
| Cancelación | objetivo inválido antes de t7 → recuperación abreviada sin daño; después **no se cancela** | antes de t14, igual | antes de t16, igual | abortar si choca (`horizontalCollision`), sin suelo delante, objetivo inválido o agua: frena, sin daño |
| Interrupción | no (sin `hurt` en aviso/impacto) | no | no | no |
| Efectos visuales | polvo + 2–3 fragmentos de hueso | estela verde (arco de partículas), tierra | onda de polvo a ras de suelo + `BLOCK` de tierra | polvo en el arranque y en el frenado, líneas de velocidad discretas |
| Efectos sonoros | roce de hueso + chasquido | arrastre de raíces + golpe sordo | golpe grave + grava | rugido corto + zancadas |
| Contrajuego | retroceder 2 bloques o pasar por detrás | alejarse > 2,8 b o esquivar en lateral | salir del radio o **saltar** (el impacto es a ras: banda hasta 1,4 → un salto alto la supera solo si el jugador está ≥ 1,4 por encima) | moverse en lateral en el aviso (la dirección se fija al empezar el dash); bloquear con escudo |
| Multijugador | todos los válidos en el sector, **una vez** | ídem | ídem (todos en el disco) | cápsula: cada jugador una vez; el dash se detiene en el primero |

**Estela ectoplasmática del `heavy_swipe` (D-21 aprobada): solo partículas** (GeckoLib no genera estelas de geometría). Servidor, desde el tick de aviso (t8) hasta el impacto (t19): arco de 8–12 partículas de polvo de color `#4DE2A6` (tamaño 0,8–1,2) más 2–3 `SOUL`/`END_ROD` repartidas sobre el sector de ±80° a la altura de la mano; máximo **14 partículas por ejecución**, respetando la opción de partículas del cliente; sin ellas el golpe se debe leer igual por la pose del brazo.

**Parámetros de la embestida (`lunge`)** `[P]`: velocidad horizontal 0,42 bloques/tick durante 7 ticks (≈ 2,9 b; el rozamiento del suelo la reduce); dirección **fijada en el tick 14** hacia la posición del objetivo en ese momento (sin guiado); gravedad normal; `setVelocity` cada tick (no se acumula); sin vuelo ni salto; se aborta si `horizontalCollision`, sin bloque sólido bajo el borde delantero, o si se entra en fluido. Sin teletransporte, sin atravesar bloques. Tras el dash: frenado forzado (velocidad ×0,4) durante 3 ticks. Si hay desincronía visible en pruebas: (1) `trackingTickInterval(1)` solo para este mob; (2) compensar con el desplazamiento de `pelvis`/`body` en la animación; (3) reducir a 6 ticks.

**Cuándo empieza cada enfriamiento:** el del ataque y el **global de 20 ticks** empiezan al **terminar la acción** (fin de recuperación), no al empezar el aviso; si el aviso se cancela, el enfriamiento empieza en el momento de la cancelación con el 50 % de su valor.

## 12. Arquitectura de hitboxes

| Capa | Definición |
|---|---|
| **Hitbox física** | `EntityType.Builder#dimensions(0.75f, 1.8f)` (sin cambios) + `eyeHeight(1.45f)`; navegación y colisiones estándar; cabe por puertas de 1 bloque y techos de 2 |
| **Hurtbox** | = hitbox física (Minecraft no usa huesos). **La masa sobresale ≈ 0,3 bloques por lado: es solo visual**; los proyectiles y golpes del jugador que apunten a la masa fuera de la caja **no impactan**. Mitigación de diseño: la masa se estrecha hacia el hombro y su parte ancha queda baja (cerca del suelo); no ampliar la caja |
| **Volumen ofensivo** | Clase pura `OsarioStrikeRules` (sin Minecraft) calcada de `SpectralRendRules` [V]: `contains(x, y, z, fwdX, fwdZ, scale)` para sectores (claw/heavy) y discos desplazados (slam: centro a 1,5 b delante, radio 2,1), `contact(AABB…)` (punto más cercano del polígono al recortar la caja del objetivo) y cápsula barrida para `lunge` |
| **Orientación** | El yaw se **fija al empezar el aviso** (`attackYaw`, como el Errante); el jugador puede esquivar circulando |
| **Ventana y tick** | Relojes `warn`/`strike`/`end` (tabla §11), derivados de un **sello de tick** guardado en `DataTracker` (`now<<4 \| ordinal`), igual que el Errante [V]; el cliente reproduce el clip desde ese tick |
| **Impactos duplicados** | `Set<UUID>` por ejecución (un golpe por jugador y por acción) |
| **Obstáculos** | `unobstructed(world, target, point)` (raycast del centro del Osario al punto de contacto); sin daño a través de bloques sólidos; paredes y espacios estrechos: el volumen se recorta por la línea de visión |
| **Escalado** | Todas las distancias × `getScale()` (`SpectralRendRules` ya admite `scale`); GeckoLib aplica la misma escala al modelo [V] |
| **Latencia** | El daño ocurre en el servidor en el tick de impacto; el cliente va por delante/detrás ≤ 1–2 ticks (3 en el peor caso por el intervalo de seguimiento). **No se garantiza sincronización perfecta**: se mide en la Fase 6 con 100 ms y 200 ms de latencia simulada |
| **Objetivo muere/desconecta/cambia de dimensión** | Objetivo inválido = `isRemoved() \|\| !isAlive() \|\| getWorld() != mob.getWorld() \|\| creativo/espectador`: en aviso → recuperación abreviada sin daño; en el impacto → no se evalúa a ese jugador pero sí a los demás; en `lunge` → frena y entra en recuperación; el objetivo pasa a ser el siguiente válido (`ActiveTargetGoal`) |

## 13. IA y transiciones

**Patrón.** Reutilizar el del Errante [V]: `tryAttack` devuelve `false`; un `OsarioStrikeGoal` (controles MOVE + LOOK) sustituye a `MeleeAttackGoal`; acción sincronizada con `DataTracker<Long>` (sello de tick + ordinal de 4 bits: NONE, ALERT, CLAW, HEAVY, SLAM, LUNGE, HURT, DEATH); `setAttacking(true/false)` según haya objetivo (para la animación `chase`). No se crea una máquina de estados nueva más allá del `Action` y de las reglas puras.

**Estados y transiciones.**
```
IDLE/WANDER (goals vanilla) ──objetivo nuevo (cualquier vía)──▶ ALERT (13 t)
ALERT ──fin──▶ CHASE (navegación; animación chase si isAttacking && se mueve)
CHASE ──elige ataque (reglas) y puede──▶ ATTACK_WINDUP (aviso, yaw fijado)
ATTACK_WINDUP ──tick de impacto──▶ ATTACK_ACTIVE (volumen, un golpe/jugador)
ATTACK_ACTIVE ──fin de ventana──▶ ATTACK_RECOVERY (7–12 t; no se cancela)
ATTACK_RECOVERY ──fin──▶ CHASE
CUALQUIER ──daño (si no hay aviso/impacto en curso)──▶ HURT (8 t) ──▶ estado anterior
CUALQUIER ──vida ≤ 0──▶ DEATH
CHASE ──sin objetivo válido 40 t──▶ WANDER
```
No hay estado `STAGGER` (no existe aturdimiento). La alerta también se reproduce cuando V83 («llamada de auxilio» de `ThreatManager`) fija el objetivo desde fuera.

**Selección contextual de ataque** (determinista con pesos; se evalúa al terminar la recuperación y al llegar a distancia útil). `d` = distancia horizontal centro–objetivo / `s`.
| Condición | claw | heavy | slam (común) | slam (élite/campeón) | lunge |
|---|---|---|---|---|---|
| d < 1,7 | 55 | 25 | 8 | 30 | 0 |
| 1,7 ≤ d < 3,0 | 10 (solo si d < 2,0) | 55 | 6 | 22 | 0 (si d < 3,2) |
| 3,2 ≤ d ≤ 6,5 | 0 | 0 | 0 | 0 | 45 (solo con las condiciones de §11) |
Modificadores: ×0 si su enfriamiento no terminó; **×0,35 si fue el último ataque; ×0 si ya se repitió dos veces seguidas**; heavy +20 si el objetivo circula (velocidad angular alta); slam +25 si hay ≥ 2 jugadores a ≤ 3 b del Osario; lunge ×1,4 si el objetivo se aleja (distancia creciente ≥ 4 t); **dirección relativa:** si el objetivo está en el hemisferio trasero (> 100° del frente), primero se reorienta (giro ≤ 30°/t) y solo ataca con slam/heavy cuando queda dentro de ±60°; **tiempo de persecución:** lunge exige ≥ 40 t; **obstáculos:** sin línea de visión no se ataca (se reposiciona con `startMovingTo`, reintento cada 8 t); **otros jugadores:** el objetivo no cambia durante aviso/impacto; el sector y el disco golpean a todos los válidos. Desempate por semilla del mob (no `random` puro).

**Presión colectiva (Niebla de Almas) — D-22 aprobada: limitar sin modificar otros mobs.** La regla vive **solo en el Osario**. Sistema de **fichas por objetivo entre Osarios**: como mucho **3 Osarios** pueden estar en `ATTACK_WINDUP/ACTIVE` sobre el mismo jugador (el registro de fichas es estado en memoria del servidor, limpiado al descargar la entidad y al parar el servidor); los demás Osarios hacen `CHASE`, rodean a ≥ 2 b o esperan 10 t. Los **Errantes Velados y los demás mobs no se cuentan ni se modifican** (no se toca su código ni su comportamiento). Para la regla de densidad se **leen** (solo lectura) las criaturas con la etiqueta de evento de la Niebla en 8 bloques: con ≥ 6 se multiplican por 0,5 los pesos de `slam` y `lunge` del Osario. Evita impactos inevitables apilados sin alterar al resto del bestiario.

**Interacción con la Amenaza V83 y los afijos.** El anillo III+ puede enfurecer (Velocidad y Fuerza tras bajar del 35 %): en furia, la recuperación se reduce un 20 % y los pesos de heavy/lunge suben ×1,25. La esquiva de proyectiles (V83) no afecta a los ataques del Osario. Élites y campeones: `slam` con el enfriamiento corto y más peso; el campeón de la Cripta mantiene `Elites.makeElite` y su bonus de vida. **Sin IA ni poderes extraordinarios del Eclipse.**

**Reglas anti-abuso.** Un único ataque activo; nunca se inicia otro durante recuperación o enfriamiento global (20 t); ninguna acción se cancela para empezar otra salvo `DEATH`; el aviso solo se aborta por objetivo inválido (§12); el sello de tick impide reproducir dos veces el mismo clip.
## 14. Integración con Fabric y GeckoLib 4.7.7

| Pieza | Acción | Estado hoy `[V]` |
|---|---|---|
| `mob/OsarioEntity` | Quitar `MeleeAttackGoal`; `tryAttack` → `false`; añadir `OsarioStrikeGoal`, `DataTracker<Long> ACTION` (sello de tick + 4 bits), `setAttacking`, `hurt` sin interrumpir ataques, muerte con `death` (≤ 19 t), sonidos; **no tocar** atributos (24 / 5 / 0,27 / 24) ni `dropLoot` (el botín solo si nació en un evento) | existe; usa `MeleeAttackGoal` y daño inmediato |
| `mob/OsarioEntities` | Conservar id `aptitudes:osario_quebrado`, huevo y `maxTrackingRange(8)`; añadir `.eyeHeight(1.45f)`; **no duplicar el registro** | existe; `dimensions(0.75f, 1.8f)` |
| `mob/OsarioStrikeRules` (nueva, pura) + `OsarioStrikeRulesTest` | relojes, sectores, disco desplazado, cápsula, selección de ataque (pesos, repetición, fichas) | no existe |
| `client/render/MobRenderers` | Para el Osario: `addRenderLayer(new AutoGlowingGeoLayer<>(renderer))` y `getDeathMaxRotation() = 0` (patrón del Errante); mantener `withScale(1.0f)` | sin capa emisiva |
| Modelo | `tools/models/blockbench/osario_quebrado.bbmodel` (`model_format: geckolib_model`, `box_uv`, `format_version 5.0`, resolución 128×128 con PNG de 256) → exportar `geo/entity/osario_quebrado.geo.json` (`geometry.osario_quebrado`, `texture_width/height = 128`, `visible_bounds_width 2.5`, `visible_bounds_height 2.4`, `offset [0,1.0,0]`) | geo placeholder (7 huesos, 64²) |
| Animaciones | `animations/entity/osario_quebrado.animation.json` con los 10 clips de §9 (`animation.osario_quebrado.<nombre>`), `idle/walk/chase` en bucle, `death` con `hold_on_last_frame` | 3 clips |
| Texturas | `textures/entity/osario_quebrado.png` (256², RGBA) + `osario_quebrado_glowmask.png` (256², mismo tamaño) | PNG 64² plano |
| Controladores | `move` (idle/walk/chase por `isMoving()` + `isAttacking()` con histéresis ≥ 10 t; transición 5) y `action` (transición 2; clips disparados por el `DataTracker`, no por `triggerAnim`, para sincronizar desde el sello de tick como el Errante) | `move` + `action` con `triggerAnim` |
| Generador | Añadir `'osario_quebrado'` a `HAND_MADE` en `tools/models/gen_mob_models.py` y comprobar `--check` | no está en `HAND_MADE` |
| Sonidos | 9 eventos sintetizados con `tools/audio/gen_sounds.py`: `ambient`, `alert`, `hurt`, `death`, `claw`, `heavy`, `slam_charge`+`slam_hit`, `lunge`, y paso/arrastre; `sound/ModSounds`, `sounds.json`, subtítulos (`subtitles.aptitudes.osario.*`) en en/es (+ variantes) y generadores pt/fr/de. Sin sonidos de otros juegos | no hay sonidos propios del Osario |
| Idiomas | Revisar `bestiary.aptitudes.osario_quebrado.tip/lore` (el «carga al verte» pasa a ser cierto con la embestida; unificar con «duelo antiguo» / «sepultura incompleta»); **ningún texto de jugador nombra la Conciencia ni usa «ancestral/primordial»** (regla V68 §2); mantener los generadores de `tools/lang` | existen |
| Pruebas | `mob/OsarioStrikeRulesTest` (delante/detrás/lateral, alturas, fuera de alcance, escala, caja grande, impacto único, orden de ticks, selección sin repetición triple, fichas), `mob/OsarioResourcesTest` (huesos, clips y duraciones, PNG y glowmask 256² iguales, `texture_width` 128, UV dentro del atlas, dimensiones múltiplo de 0,5 u, sin huesos huérfanos). No repetir nombres de pruebas existentes | no existen |
| Documentos | `docs/V84_OSARIO_QUEBRADO.md` (nuevo), `docs/ENEMIGOS_DISENO.md` (estado), `docs/TESTING.md`, `CHANGELOG.md`, `AGENTS.md` (fila) | — |

**No tocar:** Errante Velado, Escorificador de Hierro, Coloso Sin Rostro, Custodio, Gran Osario Quebrado, ids, namespace, atributos base, balance global.

**Convenciones de GeckoLib 4.7.7 verificadas** [V]: (1) glowmask = `<textura>_glowmask.png`, alpha > 0 marca píxeles emisivos, el color sale de la textura base y se borra de la capa base; (2) rotaciones de animación **se suman** a la de reposo; (3) X e Y se niegan al cargar modelo y animación; (4) el renderer aplica `getScale()`; (5) el UV se normaliza por `texture_width/height` de la descripción; (6) `getDeathMaxRotation` existe y se puede anular.

## 15. Protocolo de validación

### 15.1 Fases con puertas de aprobación
| Fase | Entrega | Criterio de aprobación |
|---|---|---|
| 0 | Verificación de estado (solo lectura), imágenes, versión de Blockbench/MCP, calibración de signos | informe sin diferencias o con las diferencias listadas |
| 1 | Bloqueo de volúmenes y rig (29 huesos, pivotes) | altura posada 28–30 u, suelas y masa en y = 0, silueta reconocible en las 4 vistas |
| 2 | Geometría detallada (≈145–170 cubos) | fidelidad anatómica (§5), sin cubos sin función, sin intersecciones involuntarias |
| 3 | Textura 256² + glowmask | §7, §8; capturas frente, espalda, ambos laterales y 3/4; con y sin glow; ampliada ×8 NEAREST |
| 4 | Animaciones (10 clips) | tablas §10 refinadas, contactos con el suelo, bucles cerrados, sin vibraciones |
| 5 | Integración (Java, renderer, sonidos, textos, pruebas) | `./gradlew build` y `--check` en verde |
| 6 | Pruebas en partida y rendimiento | §15.2–15.4 |

### 15.2 Pruebas automáticas
`./gradlew build` (JUnit), `python3 tools/models/gen_mob_models.py src/main/resources/assets/aptitudes --check`, generadores de idioma y `check_lang.py`, `OsarioStrikeRulesTest`, `OsarioResourcesTest`. Verificación de los clips con el cargador de GeckoLib en una prueba de recursos si es posible; si no, validar nombres y duraciones leyendo el JSON.

### 15.3 Pruebas en partida (lista)
Comandos: `/summon aptitudes:osario_quebrado`, huevo del creativo, `/aptitudes event start soul_fog`, élite y campeón de la Cripta, `/gamemode` creativo/espectador, `/tick query`.
| Prueba | Aprobado si |
|---|---|
| Silueta a 8 m, sin partículas | se reconoce esqueleto + masa + núcleo |
| idle/walk/chase | pies y masa no atraviesan el suelo; la masa arrastra con retraso |
| Cada ataque | anticipación visible ≥ 0,5 s; daño en el tick de impacto (±1–2 ticks) |
| Jugador delante/detrás/lateral | impacto solo dentro del volumen |
| Dos o más jugadores | cada uno golpeado una sola vez |
| Pared entre ambos | sin daño |
| Alturas | coherentes con la banda vertical |
| `lunge` | sin teletransporte, sin atravesar paredes, se detiene en colisión y sin suelo, sin desincronía visible; también a 100 y 200 ms de latencia |
| Muerte | `death` completa, sin giro vanilla, cadáver sin clip |
| Élite «giant» (×1,35) | hitbox, modelo, alcances y animación escalan juntos |
| Objetivo muere/se desconecta/cambia de dimensión durante la acción | sin daño ni bloqueo; vuelve a `CHASE`/`WANDER` |
| V83 (llamada de auxilio, furia, esquiva) | alerta al recibir objetivo externo; sin conflictos |
| Niebla de Almas con presión colectiva | como mucho 3 atacando al mismo jugador |
| Capa emisiva activada/desactivada | el núcleo se lee igual |
| Sonidos | cada evento con volumen maestro al 50 % |

### 15.4 Procedimiento de comparación de rendimiento (antes y después)
Mismas condiciones en las dos pasadas (placeholder de `main` frente al modelo nuevo): mundo plano fijo con semilla/archivo guardado, dificultad normal, misma cámara fija a 12 bloques con vista 3/4, distancia de renderizado 12, simulación 10, FPS ilimitados, vsync apagado, resolución 1280×720, mismo equipo y mismos JVM args, sin otros mods salvo los dependientes, 60 s de calentamiento y 120 s de medición por caso.
| Medida | Cómo |
|---|---|
| FPS y tiempo por frame (media, p1 y p0,1) | F3 + captura de 120 s; opcionalmente `/debug start` / `/debug stop` |
| MSPT | `/tick query` (media de 120 s) con el servidor dedicado, sin jugadores cercanos y con uno |
| Memoria | F3 (heap) al final de cada caso; además tamaño de PNG y número de quads |
| Coste de partículas | mismo caso con las partículas del mob activas y con `particles = minimal`; contar con `/particle` si hace falta |
| Coste de renderizado | caso 1: sin IA (`NoAI:1b`) en cuadrícula, animando `idle`; caso 2: con capa emisiva activa y sin ella |
| Coste de navegación e IA | caso 3: `NoAI:0b` con un jugador creativo estático a 15 bloques; caso 4: persiguiendo; MSPT por separado |
Tamaños de grupo: **1, 5, 10, 20 y 30** Osarios, repetidos para cada caso. Registrar en una tabla antes/después y la diferencia en %; **no se asigna nota por usar pocos cubos**: la nota depende de las mediciones. Umbral de partida `[P]`: con 30 Osarios, el modelo nuevo no debe costar más de un 15 % de FPS ni más de 0,5 ms de MSPT que 30 Errantes Velados en las mismas condiciones (además de compararse con el placeholder).
