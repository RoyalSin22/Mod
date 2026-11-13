# Auditoría de la Colección II

Texto fuente: [COLECCION_II_CORPUS.md](COLECCION_II_CORPUS.md) (sin cambios). Solo documentación: no integra nada.

## Resultado

| Criterio | Estado |
|---|---|
| 8 entradas (5 criaturas + 3 eventos) | APROBADO |
| Criaturas con entrada de bestiario real | OBSERVADO: 4 de 5 existen; el **Custodio no** (C-12) |
| Desbloqueo «primera derrota» compatible con el código | APROBADO para 4 criaturas (ver §2) |
| Eventos separados del contador de derrotas | APROBADO como regla; **sin infraestructura** para crónicas de eventos |
| Afirmaciones de modelo/ataques sin verificar (regla 5) | APROBADO: los textos no describen ataques |
| Spoilers frente a las normas de la Colección I | OBSERVADO (§3) |

## 1. Entradas y estado en código

| Entrada | Entrada de bestiario real | Entidad | Estado |
|---|---|---|---|
| BESTIARY-001 Errante Velado | `errante_velado` | `mob/ErranteEntity` | VERIFICADO |
| BESTIARY-002 Osario Quebrado | `osario_quebrado` | `mob/OsarioEntity` | VERIFICADO; el autor lo deja «por verificar/ampliar» |
| BESTIARY-003 Autómata Antiguo | `ancient_automaton` | `boss/AutomatonEntity` | VERIFICADO |
| BESTIARY-004 Custodio | **no existe** | `mob/CustodioEntity` (guardián del sello) | NO hay entrada: añadirla es **cambio de código** (D-10) |
| BESTIARY-005 Escorificador | `escorificador_hierro` | `mob/EscorificadorEntity` | VERIFICADO |
| EVENT-001/002/003 | — (los eventos no están en `BestiaryRules`) | `WorldEventManager`, `EclipseManager` | Sin crónicas por jugador |

## 2. Desbloqueo: qué hace hoy el código (VERIFICADO)

`guide/GrimoireManager.onKill`: al morir una criatura a manos de un jugador, `BestiaryRules.unlockedBy(...)`
registra la entrada para el **que mata y para los miembros del grupo cercanos** (`PartyManager.onlineMembers`).
Cada muerte incrementa un contador por entrada. Por tanto, «primera derrota» = el contador pasa de 0 a 1.

Consecuencias para el lore:

- El desbloqueo de las entradas 001, 002, 003 y 005 **ya funciona**; solo falta mostrar el texto narrativo.
- Un jugador del grupo desbloquea sin haber golpeado: el lore debe tolerarlo («derrota» incluye al grupo).
- Derrota por muerte no jugador (lava, otro mob) **no** desbloquea; coherente con «derrota».

## 3. Los textos nuevos frente a los textos ya existentes

El bestiario ya tiene un texto práctico por criatura (`bestiary.aptitudes.<id>.tip`), p. ej. Osario:
«Hueso y tierra que no descansan. Arrastra un brazo y carga al verte.» Esos son **consejos de combate**.
**El lore no debe reemplazarlos.** Propuesta: clave separada `bestiary.aptitudes.<id>.lore`, mostrada junto al
`.tip` (decisión de interfaz del encargo del Grimorio). Regla 4 del autor (estadísticas y drops reales) ya la
cumple el bestiario existente.

## 4. Observaciones por entrada

| Entrada | Observación | Tipo |
|---|---|---|
| 001 | La línea «**Dato de implementación conocido**…» es una nota de trabajo: **no debe llegar al jugador** | Corregir al integrar |
| 001 | «Restos de vestimentas… velo espectral» coincide con la especificación visual (`docs/ENEMIGOS_DISENO.md`: tela carbonizada, tiras de tela, brillo cian). El `.tip` actual habla de «sudario de niebla de almas». No hay contradicción | OK |
| 002 | La nota «verificar modelo…» también es de trabajo. La referencia visual (hueso, tierra, raíces, núcleo verde) permite ampliar después sin inventar | Corregir al integrar |
| 003 | Atribuye las máquinas a los ingenieros de la Primera Era: coherente con Colegio de los Artífices (03, PROPUESTA) | OK |
| 004 | Afirma «cuatro llaves» y «cámaras interiores» como hecho: NO VERIFICADO (C-13). Además requiere crear la entrada de bestiario | Bloqueado por código |
| 004 | «Algunas puertas fueron cerradas por razones muy distintas a un tesoro» anticipa LORE-012 con desbloqueo equivalente | OK (misma revelación) |
| 005 | «Pertenece a las amenazas de las **irrupciones**»: en el juego también aparece de forma natural en el Nether (`docs/ENEMIGOS_DISENO.md`, peso 10 de 100 en la Lluvia). El texto lo deja entender como exclusivo de las irrupciones | Matizar |
| 005 | «**prisión primordial**» aparece en una entrada de bajo nivel (primera derrota en el Nether). Adelanta que existe una prisión | Spoiler (ver §5) |
| EVENT-001 | Cita la «**Conciencia Ancestral**» por nombre en el primer encuentro con la Niebla | Spoiler (ver §5) |
| EVENT-001 | Los testimonios contradictorios (antes/después de los gremios) respetan «no convertir hipótesis en hechos» | OK |
| EVENT-002 | Atribuye el fenómeno a portales; el código lo activa por calendario (C-04). El texto es general y no afirma el gatillo | OK, sin cambiar eventos |
| EVENT-003 | «**único** Eclipse»: contradice D-01 (se repite con sacrificio), igual que A-01 | Decisión (ver A-07) |
| EVENT-003 | Dice «el sacrificio de una **reliquia** antigua»: con Eclipses posteriores el objeto sacrificado no está definido (D-02) | Matizar («una ofrenda») |
| EVENT-003 | Duplica el contenido de LORE-011 y LORE-013 y se desbloquea en el mismo momento | Redundancia a decidir |

## 5. Secretos y normas editoriales

La Colección I reservaba: no revelar el origen de la prisión antes del segundo arco y no decir todo de entrada.

| Revelación | Dónde | Cuándo se ve | Riesgo |
|---|---|---|---|
| Existencia de una «prisión primordial» | BESTIARY-005 | Primera derrota del Escorificador (jugador temprano en el Nether) | **Alto**: da más información que LORE-007 (media) |
| Nombre «Conciencia Ancestral» | EVENT-001 | Primera Niebla (inicio de partida) | **Alto**: el nombre del enemigo final se conoce mucho antes del Primer Eclipse |
| Término «Prisionero» | EVENT-003 | Primer Eclipse | Bajo: coincide con LORE-013 |

Propuestas (no aplicadas): en 005 sustituir «la prisión primordial» por «un lugar del Velo que ningún
investigador alcanzó»; en EVENT-001 sustituir «la Conciencia Ancestral» por «aquello que los gremios temieron»
(o dejar la frase solo desbloqueada tras LORE-009).

## 6. Decisiones y diferencias nuevas

| Id | Pregunta | Propuesta |
|---|---|---|
| A-07 | EVENT-003 dice «único Eclipse» y «reliquia» | «Primer Eclipse» y «una ofrenda» (no cambia la historia) |
| A-08 | Notas de trabajo dentro de 001 y 002 | Retirarlas al integrar (se guardan en la documentación) |
| A-09 | Spoilers de 005 y EVENT-001 (§5) | Aplicar las sustituciones propuestas o fijar un desbloqueo posterior |
| A-10 | 005: matizar que también habita en el Nether | Añadir «y los territorios del Nether» |
| D-10 | Entrada de bestiario del Custodio | Cambio de código (`BestiaryRules` + lang + prueba): encargo aparte |
| D-15 | ¿Dónde viven las crónicas de eventos? | No hay estructura. Opciones: capítulo del Grimorio nuevo con estado de lectura por jugador, o reutilizar el estado de lectura de las Colecciones de lore. **No decidido; no se implementa** |
| D-16 | «Primer encuentro documentado» y «primera participación» | Sin definición en el código: hace falta decidir (presencia durante el evento, haber sido atacado, haber sobrevivido…) |

## 7. Relación con la Colección I

- 006 «Huella de los Caídos» (Ecos, Niebla) complementa a EVENT-001; ambos se desbloquean por la Niebla.
  Conviene que EVENT-001 **no** se lea antes que 006, o que el uso de «Ecos» sea consistente.
- LORE-012 y BESTIARY-004 se desbloquean con la derrota del Custodio: revelación redundante en dos pantallas.
- LORE-013/014 y EVENT-003 cubren el mismo evento: se recomienda que EVENT-003 sea la crónica «objetiva» y
  LORE-013/014 la consecuencia narrativa.
