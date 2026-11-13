# 03 · Los veinte gremios

Estado: la existencia de **20 roles en 5 categorías** es **VERIFICADO EN CÓDIGO** (`role/Role.java`,
`role/RoleCategory.java`). Los **nombres de gremio** son **PROPUESTA** literaria: no son identificadores técnicos
ni sustituyen a los roles. Los ids de rol reales no cambian.

## Correspondencia con el catálogo real

Los **20 ids y las 5 categorías coinciden** con el borrador. Los **nombres oficiales** no siempre: siete roles
se llaman en el juego de otra manera que en el borrador (columna «¿Nombre coincide?»). El lore usa siempre el
**nombre oficial del juego** cuando habla del rol y el nombre de gremio cuando habla de la tradición histórica.

| Familia (categoría) | Id de rol | Nombre oficial ES | Nombre oficial EN | Nombre en el contexto narrativo | Gremio histórico (PROPUESTA) | ¿Nombre coincide? |
|---|---|---|---|---|---|---|
| Marcial (`MARTIAL`) | `berserker` | Bersérker | Berserker | Bersérker | Hermandad del Ímpetu | Sí |
| Marcial (`MARTIAL`) | `lancer` | Lancero | Lancer | Lancero | Orden de la Lanza Inquebrantable | Sí |
| Marcial (`MARTIAL`) | `swordsman` | Duelista | Duelist | Espadachín | Escuela del Filo Eterno | **No** |
| Marcial (`MARTIAL`) | `monk` | Monje | Monk | Monje | Claustro del Pulso Interior | Sí |
| Arcano (`ARCANE`) | `mage` | Arcanista | Arcanist | Mago | Círculo del Saber Arcano | **No** |
| Arcano (`ARCANE`) | `necromancer` | Nigromante | Necromancer | Nigromante | Custodios del Último Eco | Sí |
| Arcano (`ARCANE`) | `elementalist` | Chamán | Shaman | Elementalista | Concilio de las Fuerzas Primarias | **No** |
| Arcano (`ARCANE`) | `gunslinger` | Mago pistolero | Gunslinger Mage | Pistolero arcano | Orden del Disparo Rúnico | **No** |
| Sombra (`SHADOW`) | `assassin` | Asesino | Assassin | Asesino | Cofradía del Paso Silente | Sí |
| Sombra (`SHADOW`) | `shapeshifter` | Cambiaformas | Shapeshifter | Cambiaformas | Sociedad de las Mil Formas (renombrado; el contexto decía «Herederos…») | Sí |
| Sombra (`SHADOW`) | `hunter` | Cazador | Hunter | Cazador | Hermandad de la Huella | Sí |
| Sombra (`SHADOW`) | `warlock` | Brujo abisal | Abyssal Warlock | Brujo | Círculo de los Secretos Velados | **No** |
| Guardián (`GUARDIAN`) | `herbalist` | Boticario | Apothecary | Herbolista | Colegio de la Raíz Serena | **No** |
| Guardián (`GUARDIAN`) | `paladin` | Paladín | Paladin | Paladín | Orden del Escudo del Alba | Sí |
| Guardián (`GUARDIAN`) | `druid` | Druida | Druid | Druida | Custodios del Bosque Antiguo | Sí |
| Guardián (`GUARDIAN`) | `bard` | Bardo | Bard | Bardo | Coro de la Memoria Viva | Sí |
| Oficio (`CRAFT`) | `engineer` | Ingeniero | Engineer | Ingeniero | Colegio de los Artífices | Sí |
| Oficio (`CRAFT`) | `merchant` | Mercader táctico | Tactical Merchant | Mercader | Liga de las Rutas Doradas | **No** |
| Oficio (`CRAFT`) | `weapon_summoner` | Invocador de armas | Weapon Summoner | Invocador de armas | Forja de las Armas Vinculadas | Sí |
| Oficio (`CRAFT`) | `tamer` | Domador | Tamer | Domador | Compañía de la Manada (renombrado; el contexto decía «Pacto…») | Sí |

### Lectura de la tabla

- «Gremio histórico» es la institución antigua; «rol» es lo que juega el jugador hoy. El rol **hereda el oficio**,
  no la pertenencia: no existe ningún sistema de ascendencia (ver exclusión en README).
- **Renombrados por la exclusión de `AGENTS.md` §3 (decisión delegada en el criterio del redactor):**
  - Domador: «Pacto de la Manada» → **Compañía de la Manada** (evita «pacto»).
  - Cambiaformas: «Herederos de las Mil Formas» → **Sociedad de las Mil Formas** (evita «herederos»).
  - La facción «Herederos de los Sellos» → **Vigilantes de los Sellos** (ver 05).
- Nombres que ya chocan entre sí y conviene vigilar: «Custodios del Último Eco» (Nigromante) y «Custodios del
  Bosque Antiguo» (Druida) comparten «Custodios»; «Colegio de…» aparece dos veces. Es una decisión estética
  menor (CONTRADICCIONES C-11).
- Colisiones con nombres de senda ya existentes: el rol `mage` se llama **Arcanista** en el juego y su senda A
  también (`path.aptitudes.mage_a`); `swordsman` se llama **Duelista** y su senda A también. El lore debe
  distinguirlas («el rol Arcanista» frente a «la senda Arcanista»), ver C-09.

## Fichas de gremio (lo mínimo común)

Cada gremio comparte la estructura: **Disciplina**, **Familia**, **Rol real**, **Doctrinas** (sus tres sendas,
en 04), **Posición ante el Primer Eclipse**, **Estado**.

| Gremio | Disciplina (resumen) | Posición ante el Primer Eclipse (PROPUESTA) |
|---|---|---|
| Hermandad del Ímpetu | Fuerza, resistencia, impulso, dominio del riesgo | Sin implicación directa |
| Orden de la Lanza Inquebrantable | Precisión, distancia, defensa de posiciones | Sin implicación directa |
| Escuela del Filo Eterno | Técnica, movimiento, ritmo | Sin implicación directa |
| Claustro del Pulso Interior | Disciplina corporal, concentración, control de la Esencia | Parte de sus estudiosos interesados en el Velo |
| Círculo del Saber Arcano | Técnicas arcanas y aplicaciones | Algunos investigadores colaboran con el Cónclave |
| Custodios del Último Eco | Manifestaciones de Esencia ligadas a la muerte | **No** se les considera culpables como colectivo |
| Concilio de las Fuerzas Primarias | Fuerzas elementales y sus relaciones | Sin implicación directa |
| Orden del Disparo Rúnico | Instrumentos de precisión + técnicas arcanas | Sin implicación directa |
| Cofradía del Paso Silente | Observación, movilidad, infiltración, ejecución | Sin implicación directa |
| Sociedad de las Mil Formas | Transformación y adaptación corporal | Sin implicación directa |
| Hermandad de la Huella | Rastreo, observación, supervivencia | Sin implicación directa |
| Círculo de los Secretos Velados | Fenómenos sobrenaturales y saber poco convencional | Sus capacidades **no** dependen de la Conciencia |
| Colegio de la Raíz Serena | Materiales naturales, preparados | Sin implicación directa |
| Orden del Escudo del Alba | Protección de comunidades, disciplina, responsabilidad | Participa en el Gran Sellado (PROPUESTA) |
| Custodios del Bosque Antiguo | Seres vivos, naturaleza, equilibrio | Sin implicación directa |
| Coro de la Memoria Viva | Expresión, coordinación, inspiración, memoria histórica | Fuente de testimonios (autores de documentos) |
| Colegio de los Artífices | Mecanismos y dispositivos | Constructores de los constructos del templo (PROPUESTA) |
| Liga de las Rutas Doradas | Intercambio, administración, circulación de recursos | Sin implicación directa |
| Forja de las Armas Vinculadas | Esencia, voluntad y manifestación de armamento | Sin implicación directa |
| Compañía de la Manada | Cooperación entre aventureros y criaturas | Sin implicación directa |

Todas las posiciones son **PROPUESTA**; ninguna afirma un hecho de mecánica.

## Relación con el juego (VERIFICADO)

Hoguera (`souls/`), Mesa del gremio (`world/GuildCrafting`, `GuildDeskBlock`) y el aldeano del gremio existen. El
lore los presenta como **instituciones supervivientes** de la Edad de los Gremios; no añade funciones.
