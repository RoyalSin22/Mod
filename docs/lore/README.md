# Biblia narrativa de Aptitudes (documentación de lore)

> **Esta carpeta es documentación.** No cambia mecánicas, entidades, eventos, habilidades, recetas ni interfaz.
> **Fase 2a (datos narrativos):** los 23 textos de las Colecciones I y II, con las correcciones A-01…A-10, están en
> `tools/lang/lore_catalog.py` (es/en), sus metadatos en `tools/lore/lore_entries.json` y las claves
> `lore.aptitudes.*` en los archivos de idioma (`python3 tools/lang/gen_lore_lang.py [--check]`).
> pt_br, fr_fr y de_de llevan el texto en inglés como respaldo hasta traducirse. **Nada de esto está conectado al
> Grimorio**: los textos existen como claves de idioma, pero ninguna pantalla los muestra todavía. La integración
> es un encargo posterior.

## Para qué sirve

Fija, en un solo sitio, la historia del universo de Aptitudes y su relación con lo que **ya existe en el código**,
para que los textos futuros (Grimorio, bestiario, inscripciones, tooltips) salgan de una fuente coherente.
**El código es la fuente de verdad técnica**: el lore se adapta a las mecánicas, nunca al revés.

## Índice

| Archivo | Contenido |
|---|---|
| [01_COSMOLOGIA.md](01_COSMOLOGIA.md) | Edad Indivisa, Gran Separación, Velo, Esencia, Ecos, Prisión Primordial |
| [02_CRONOLOGIA.md](02_CRONOLOGIA.md) | Catorce períodos en orden relativo, sin años |
| [03_GREMIOS.md](03_GREMIOS.md) | Los veinte gremios y su correspondencia con los roles reales |
| [04_DOCTRINAS_Y_SENDAS.md](04_DOCTRINAS_Y_SENDAS.md) | Las tres doctrinas y la ficha de las 60 sendas reales |
| [05_FACCIONES_Y_PERSONAJES.md](05_FACCIONES_Y_PERSONAJES.md) | Facciones históricas y autores atribuidos |
| [06_EVENTOS_Y_ARCOS.md](06_EVENTOS_Y_ARCOS.md) | Niebla, templo, Eclipse, Lluvia de brasas y los dos arcos |
| [07_BESTIARIO_TEMPLOS_ARTEFACTOS.md](07_BESTIARIO_TEMPLOS_ARTEFACTOS.md) | Criaturas, jefes, templos y artefactos |
| [09_CADENA_DE_PROGRESION.md](09_CADENA_DE_PROGRESION.md) | Cadena aprobada Niebla → templo → Behelit → Eclipse y su estado en el código |
| [10_PLAN_DE_IMPLEMENTACION.md](10_PLAN_DE_IMPLEMENTACION.md) | Plan por tandas (T0–T7 y pista de arte); propuesta, sin autorizar |
| [11_DISENO_T3_TEMPLO.md](11_DISENO_T3_TEMPLO.md) | Diseño propuesto de T3 (Templo del Custodio); sin autorizar |
| [08_PREPARACION_GRIMORIO.md](08_PREPARACION_GRIMORIO.md) | Cómo encajaría el lore en el Grimorio real (sin implementar) |
| [COLECCION_I_CORPUS.md](COLECCION_I_CORPUS.md) | Textos del autor de la Colección I (15 entradas, sin cambios) |
| [AUDITORIA_COLECCION_I.md](AUDITORIA_COLECCION_I.md) | Revisión de esa colección frente al canon y al código |
| [COLECCION_II_CORPUS.md](COLECCION_II_CORPUS.md) | Textos del autor de la Colección II (5 criaturas, 3 eventos, sin cambios) |
| [AUDITORIA_COLECCION_II.md](AUDITORIA_COLECCION_II.md) | Revisión de esa colección frente al bestiario y los eventos reales |
| [CONTRADICCIONES.md](CONTRADICCIONES.md) | Diferencias entre el lore y el código |
| [DECISIONES_PENDIENTES.md](DECISIONES_PENDIENTES.md) | Decisiones tomadas y abiertas |

## Estados (se usan en todos los archivos)

| Etiqueta | Significado |
|---|---|
| **ESTABLECIDO** | Dirección elegida expresamente por el autor |
| **PROPUESTA** | Nombre, hecho o explicación pendiente de aprobación; no es canon |
| **VERIFICADO EN CÓDIGO** | Comprobado en el repositorio (con el archivo) |
| **PENDIENTE DE IMPLEMENTAR** | Aprobado, pero todavía no existe técnicamente |
| **NO VERIFICADO** | Falta inspeccionarlo |

Una propuesta de lore **nunca** debe leerse como mecánica implementada.

## Convención de identificadores (para la Fase 2)

Los identificadores narrativos serán estables, en minúsculas y con la forma `lore.<área>.<slug>`:

`cosmology`, `chronology`, `guild`, `doctrine`, `faction`, `figure`, `event`, `bestiary`, `temple`, `artifact`,
`arc1`, `arc2`. Ejemplo: `lore.cosmology.great_separation`.

- Los **nombres provisionales** (gremios, facciones, artefacto) **no** se convierten en claves públicas hasta
  que el autor los apruebe; el slug describe el concepto, no el nombre literario.
- Las claves de roles, sendas, objetos y entidades **no se inventan**: se reutilizan las reales
  (`path.aptitudes.<rol>_<a|b|c>`, `role.aptitudes.<rol>`, `bestiary.aptitudes.<id>.tip`).
- Cada pieza llevará, como mínimo: id, título, categoría, texto, estado, dependencias, condición de desbloqueo
  prevista, recursos relacionados, referencias cruzadas y notas.

## Reglas heredadas del proyecto que afectan al lore

- **Exclusión absoluta (`AGENTS.md` §3):** no hay linajes, razas, pactos, herencias ni ascendencia como mecánica,
  ni de forma indirecta. Por eso dos gremios y una facción se renombraron respecto al borrador (ver
  [CONTRADICCIONES.md](CONTRADICCIONES.md), C-05).
- La **Esencia** y los **Ecos** son explicaciones narrativas. No son una barra, un recurso ni un sistema nuevo.
- No se renombran roles, sendas, habilidades ni ids de registro.
- Idiomas: la Fase 2 será **español e inglés**; el resto (pt_br, fr_fr, de_de) más adelante.

## Qué no cubre esta fase

Sin NPC, entidades, estructuras, bloques, worldgen, nuevos eventos ni cambios de mecánica. Sin tocar el Grimorio,
los idiomas, los modelos ni los generadores. No se actualizan `CHANGELOG.md` ni `docs/ROADMAP_APTITUDES.md`
porque no cambia el mod; sí se hará cuando haya contenido integrado.
