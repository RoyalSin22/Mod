# 08 · Preparación para el Grimorio (solo diseño; nada se integra)

**Esta fase no toca el Grimorio.** Aquí se deja anotado cómo encajaría el lore en lo que ya existe, para el
encargo posterior. No se afirma que nada esté integrado.

## Arquitectura real (VERIFICADO EN CÓDIGO)

| Pieza | Dónde |
|---|---|
| Pantalla y 23 capítulos fijos | `src/client/java/dev/aptitudes/client/screen/GuideScreen.java` (lista `CHAPTERS`) |
| Textos de capítulo | Idiomas: `guide.aptitudes.<capítulo>.title` / `.body`, generados por `tools/lang/gen_guide_lang.py` desde `guide_catalog.py` |
| Bestiario | `guide/BestiaryRules` (catálogo cerrado, puro, con prueba); texto `bestiary.aptitudes.<id>.tip` |
| Persistencia por jugador | Attachment `BESTIARY` (`copyOnDeath`); `guide/GrimoireManager`; S2C `GrimoirePayload`, C2S `GrimoireRequestPayload` |
| Marcadores | `GuideRules.validBookmark`: formato `chapter|beast|item|role:<id>` |
| Comprobaciones de idioma | `tools/lang/check_lang.py`, `test_*.py`, CI |

## Qué necesitaría el lore (hipótesis para el encargo posterior)

| Necesidad | Reutiliza | Observación |
|---|---|---|
| Texto por entrada | Claves de idioma nuevas `lore.aptitudes.<id>.title/.body` | Mismo patrón que `guide.*`; es/en primero |
| Capítulos de historia | Nuevos capítulos al final de `CHAPTERS` | El orden de capítulos es estable; solo se añade al final |
| Descubrimiento progresivo | **No existe hoy** para capítulos (solo bestiario por derrota) | Hará falta un estado de lectura por jugador: decidir si ampliar el attachment `BESTIARY` o crear otro. **NO VERIFICADO**; no se decide aquí |
| Estado global (Eclipse) | `EclipseState`, `WorldEventState` | No se duplica: el Grimorio solo lo lee |
| Marcadores y buscador | Existentes | Pueden indexar el lore sin cambios de formato |

## Propuesta de agrupación del contenido (PROPUESTA)

| Grupo | Piezas | Desbloqueo previsto (PROPUESTA) |
|---|---|---|
| Historia del mundo | Cosmología, Gran Separación | Pistas en estructuras o hitos de progreso |
| La Primera Era | Cronología, Gremios, Tres Doctrinas | Progreso de rol y de nivel de senda |
| Facciones | Seis facciones y tres autores | Hallazgos en ruinas y templos |
| Relatos de eventos | Niebla, Lluvia, Eclipse | Primera vez que el jugador vive el evento |
| Registros de templos | Templo, Custodio, Autómata | Derrotar al jefe / abrir el sello |
| Conciencia Ancestral | Prisión, influencia | Tras el primer Eclipse (arco 2) |
| Segundo arco | Fractura Silenciosa | Tras el primer Eclipse |

Reglas:

1. **No revelar toda la historia al empezar.** La guía práctica (mecánicas, crafteos, roles) sigue accesible según
   sus reglas actuales.
2. El bestiario conserva su desbloqueo por derrota de criaturas originales.
3. Los registros históricos pueden tener condiciones de descubrimiento distintas a las del bestiario.
4. Sin duplicar fuentes de verdad ni persistencia paralela.
