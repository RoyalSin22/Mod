# Auditoría de la Colección I

Texto fuente: [COLECCION_I_CORPUS.md](COLECCION_I_CORPUS.md) (conservado sin cambios). Esta auditoría **no
edita** los textos del autor ni integra nada: solo compara con el canon (`docs/lore/`) y con el código.

> **Resolución (Fase 2a):** A-01 a A-06 aplicadas en `tools/lang/lore_catalog.py` (ver D-14 en `DECISIONES_PENDIENTES.md`). A-01 se ajustó: «primer Eclipse» es el histórico, así que el desbloqueo de LORE-013 es «inicio del Eclipse (primera vez que el jugador lo invoca)»; A-02 se mantuvo como «finalización del Eclipse». Esta auditoría conserva el análisis original.

## Resultado

| Criterio | Estado |
|---|---|
| 15 entradas, orden cronológico coherente con `02_CRONOLOGIA.md` | APROBADO |
| Respeta las normas editoriales (secretos reservados, sin hipótesis como hecho) | OBSERVADO (ver 8, 9 y 13) |
| Coherencia con las decisiones del autor | **OBSERVADO: 3 diferencias** (A-01, A-02, A-03) |
| Condiciones de desbloqueo viables hoy en el código | NO VERIFICADO en su mayoría (tabla 3) |
| Integración en Grimorio / idiomas | No realizada (no autorizada) |

## 1. Diferencias que requieren decisión

| Id | Entrada | Problema | Propuesta (no aplicada: no toco tu texto) |
|---|---|---|---|
| A-01 | LORE-013 | «**único** Eclipse»: contradice D-01 (el Eclipse se repite cada 10 días con sacrificio). Además el texto «la llama consumió la reliquia» describe el **primer** sacrificio | Cambiar «único Eclipse» por «**primer** Eclipse del mundo» en el desbloqueo. El texto narrativo puede quedar igual |
| A-02 | LORE-014 | «finalización del Eclipse» se dispararía **en cada** Eclipse | Desbloqueo solo al terminar el **primer** Eclipse completado de ese jugador. Con Eclipses posteriores, nada nuevo se desbloquea |
| A-03 | LORE-015 | Usa «**Herederos** de los Sellos»; ese nombre se cambió a «**Vigilantes de los Sellos**» por la exclusión de `AGENTS.md` §3 (C-05) | Sustituir el nombre, o aprobar «Herederos» como excepción (decisión tuya) |
| A-04 | LORE-003 | «Elegir una senda **no significaba rechazar las demás**» contradice la regla vigente: las sendas son **exclusivas** (aprender una bloquea las otras dos; cambiar exige reiniciar el árbol, `role/PathRules`) | Reformular en 003: elegir una senda *sí* implica renunciar a las otras en esa vida; la verdadera maestría requiere profundidad. El lore se adapta a la mecánica |
| A-05 | LORE-003 y LORE-004 | 003 dice que la **Concordia** reconoció las tres doctrinas; 004 dice que fue el **Concilio de las Tres Vías**. Son instituciones distintas en el canon (05 y 02) | Unificar: el Concilio (evento) las reconoció en el marco de la Concordia (institución). Basta una frase |
| A-06 | LORE-008 | «Todavía ignoraban que **alguien** esperaba al otro lado» (revelación media) adelanta al lector que existe una entidad antes que 009 (alta), contra «conservar los secretos» | Moverla a 009 o suavizarla («algo»). Opcional |

## 2. Mapa de identificadores provisional

Los códigos `LORE-nnn` son del autor. Para la Fase 2 propongo claves estables y descriptivas (sin nombres
literarios provisionales). **Sin crear ninguna clave aún.**

| LORE | Clave propuesta | Área |
|---|---|---|
| 001 | `lore.cosmology.essence_within` | Orígenes |
| 002 | `lore.guild.twenty_paths` | Gremios |
| 003 | `lore.doctrine.three_doctrines` | Doctrinas |
| 004 | `lore.chronology.council_three_ways` | Cronología |
| 005 | `lore.faction.concordance` | Facciones |
| 006 | `lore.cosmology.echoes` | Cosmología |
| 007 | `lore.cosmology.veil_frontier` | Cosmología |
| 008 | `lore.faction.permanence` | Facciones |
| 009 | `lore.chronology.first_answer` | Cronología |
| 010 | `lore.artifact.sealed_artifact` | Artefactos |
| 011 | `lore.event.first_eclipse` | Eventos |
| 012 | `lore.temple.last_mandate` | Templos |
| 013 | `lore.event.current_eclipse` | Eventos |
| 014 | `lore.event.silent_fracture` | Eventos |
| 015 | `lore.arc2.unfinished_oath` | Segundo arco |

(Claves en estilo `lore.aptitudes.<id>.title/.body` en los idiomas; ver 08.)

## 3. Condiciones de desbloqueo frente al código

Ninguna condición se implementa aquí. «Hook» = punto del código donde engancharse sin duplicar sistemas.

| LORE | Condición del autor | Hook existente | Estado |
|---|---|---|---|
| 001 | Conocimiento inicial / primera consulta | Ninguno (el Grimorio no guarda lectura de capítulos) | NO VERIFICADO |
| 002 | Primera consulta al catálogo de roles | Capítulo `roles` de `GuideScreen`; sin estado de lectura | NO VERIFICADO |
| 003 | Acceso a la especialización de un rol | Progreso de senda (`RoleManager`, `PathRules`; S1 al nivel 10) | Hook plausible, sin lore conectado |
| 004 | «Descubrimiento histórico intermedio» | Ninguno definido | **Condición sin definir** |
| 005 | Documentos de la Primera Era | No existen documentos coleccionables | PENDIENTE |
| 006 | Investigaciones de la Niebla de Almas | Bestiario por jugador: derrotar al Errante Velado u Osario Quebrado (`BestiaryRules`, attachment `BESTIARY`) | Hook viable, sin conectar |
| 007 | Documentos antiguos del templo | No existen documentos | PENDIENTE |
| 008 | Archivos del Primer Eclipse | No existen | PENDIENTE |
| 009 | Cámara de la reliquia / registro avanzado | `ReliquaryBlockEntity` (abrir/saquear) | Hook plausible |
| 010 | Cámara de diamante | No verificado que exista | NO VERIFICADO |
| 011 | Revelación vinculada al artefacto | El artefacto no existe | PENDIENTE |
| 012 | Derrota del Custodio / acceso a cámaras | `CustodioEntity` es el guardián (`Temples.spawnGuardian`); sin entrada de bestiario | Hook plausible |
| 013 | Inicio del Eclipse | `EclipseManager` (inicio); sacrificio en Hoguera pendiente | Parcial |
| 014 | Fin del Eclipse | `EclipseManager.end(..., completed)` distingue fin completado | Hook viable |
| 015 | Final del Santuario de las Dos Verdades | La estructura no existe | PENDIENTE |

Observaciones transversales:

- **Por jugador, no global.** 013 y 014 nacen de un estado **de mundo** (el Eclipse). Hay que decidir quién
  recibe el desbloqueo (¿solo quienes estaban presentes?, ¿todos los conectados?) para que un descubrimiento
  individual no altere el estado global ni se pierda a quien estaba desconectado (D-13).
- **Estado de lectura**: hoy no existe para capítulos. Es el principal trabajo de código del Grimorio
  (08): sin él, 001–005 no pueden «aparecer progresivamente».

## 4. Contenido que roza o aclara hechos del canon

| Tema | Entrada | Lectura |
|---|---|---|
| Primer Eclipse histórico | 011 | Coincide con el período 8. Dice que el fenómeno «terminó antes de que… consiguiera atravesarlo» ⇒ coherente con «no escapa» |
| Gran Sellado | 010 y 012 | Coincide con el período 9. Atribuye el sellado a «los últimos guardianes»; la **Orden de los Cuatro Sellos** aparece solo en 012 como categoría |
| Origen de la prisión | 007, 014, 015 | 015 dice que el confinamiento nació con la separación natural: es el **desenlace** del segundo arco, y su desbloqueo está al final del arco. Correcto |
| Término «el Prisionero» | 013 | Término nuevo del autor (título). El canon usa «Conciencia Ancestral». Se trata como apodo narrativo del texto (**PROPUESTA**) |
| «Cuatro llaves» y «cuatro cámaras» | 010 y 012 | NO VERIFICADO en código (C-13). Las entradas lo **afirman como hecho histórico** |
| Hoguera | 013 y 014 | Concuerda con el sacrificio y con la Hoguera existente |
| «Superar obstáculos» | 013 | Concuerda con persecución avanzada (VERIFICADO: `EclipsePursuit`); no afirma romper bloques |

## 5. Normas editoriales: cumplimiento

| Norma | Resultado |
|---|---|
| No desbloquear todo al empezar | Cumplible si se define el estado de lectura (ver §3) |
| No revelar el origen de la prisión antes del segundo arco | APROBADO: solo 015 lo hace; 007 deja el misterio |
| No atribuir todos los fenómenos al Prisionero | APROBADO: 013 aclara que «utilizaba aquello que ya habitaba en el mundo» |
| No convertir hipótesis en hechos | OBSERVADO: 006 afirma la existencia de los Ecos como observación de los investigadores; el texto lo enmarca como estudio (OK). 010/012 afirman hechos sobre las llaves y cámaras (ver §4) |
| No sustituir guías prácticas | APROBADO |
| No implementar condiciones sin verificar | Cumplido: nada implementado |

## 6. Trabajo estimado para la Fase 2 (si se autoriza)

1. Resolver A-01…A-06 (edición de texto por el autor o aprobación de mi propuesta).
2. Crear `tools/lang/lore_catalog.py` con los 15 textos (es) y su **traducción al inglés** (a redactar y que
   revises); generador con `--check`, patrón de `guide_catalog.py`.
3. Mantener `%`/`%%` y los 7 locales en español idénticos (`AGENTS.md`).
4. Dejar fuera de Fase 2 el estado de lectura y los desbloqueos (código Java: encargo de Grimorio).
