# Contradicciones y diferencias entre el lore y el código

El código manda. Cada fila indica qué hay, qué dice el lore y cómo se trata.

| Id | Diferencia | Código | Lore | Tratamiento |
|---|---|---|---|---|
| C-01 | Eclipse repetible | `eclipseIntervalDays = 10`, `eclipseFirstDay = 10`; rota con los otros eventos por calendario | «Único por mundo» → **decisión del autor:** el jugador, al cumplir ciertos objetivos, lo invoca **cada vez que tenga material para sacrificar en la Hoguera** (espera de 10 días). El «Primer Eclipse» es el histórico de la Primera Era | Lore adaptado (06). El sacrificio es **PENDIENTE DE IMPLEMENTAR**; no se afirma en el Grimorio hasta que exista |
| C-02 | Niebla de almas | Evento de calendario rotativo (18:00, 12 000 ticks) | Primer evento, iniciado por el aldeano del gremio, global, en cualquier momento, de un día de Minecraft, cada 10 días | **Diseño aprobado** (09). Implementación PENDIENTE; el controlador no se modifica en esta tarea |
| C-03 | Sacrificio en la Hoguera / artefacto | No existe el sacrificio ni el objeto | El Behelit (nombre de trabajo «Resonador del Umbral») se sacrifica en la Hoguera; se consume; se consigue en la cámara de diamante del Custodio | **Diseño aprobado** (09). PENDIENTE DE IMPLEMENTAR |
| C-04 | Lluvia de brasas | Calendario rotativo (6:00) | Se activa cuando un jugador enciende un portal al Nether en el Overworld, con una espera de 5 días | **Diseño aprobado** (09). Implementación PENDIENTE |
| C-05 | Nombres con exclusión | `AGENTS.md` §3 prohíbe pactos, razas, linajes y herencias | «Pacto de la Manada», «Herederos de las Mil Formas», «Herederos de los Sellos» | **Resuelto** por criterio delegado: Compañía de la Manada, Sociedad de las Mil Formas, Vigilantes de los Sellos |
| C-06 | Esencia / almas | Objeto `elite_essence` («Esencia de élite») y sistema de almas (`souls/`) | La Esencia es solo narrativa, no un recurso | El lore no la presenta como recurso; los Ecos no se fusionan con las almas |
| C-07 | Custodio / Guardián / Autómata | Tres entidades distintas; el **Custodio** es el guardián del sello del templo | «Autómata y Custodio protegen mecanismos y cámaras» | Los tres se mantienen separados (07) |
| C-08 | Material del Osario | Bestiario muestra solo `soul_shard`; sin loot table encontrada | Material por definir | **Aplazado por el autor** (D-05) |
| C-09 | Nombres de rol y senda duplicados | Rol `mage` = «Arcanista» y senda `mage_a` = «Arcanista»; rol `swordsman` = «Duelista» y senda `swordsman_a` = «Duelista» | Nombres del borrador: Mago, Espadachín | El lore usa los oficiales del juego y distingue «rol» de «senda» |
| C-10 | Nombres oficiales de rol | Siete roles con otro nombre en el juego (Arcanista, Boticario, Duelista, Mercader táctico, Chamán, Mago pistolero, Brujo abisal) | Mago, Herbolista, Espadachín, Mercader, Elementalista, Pistolero arcano, Brujo | Se conservan los ids y los nombres oficiales; no se renombra (ver 03) |
| C-11 | Repetición de prefijos de gremio | — | «Custodios…» ×2, «Colegio…» ×2, «Círculo…» ×2 | Aceptado de momento; se puede variar en la Fase 2 |
| C-12 | Bestiario sin Custodio | El Custodio no tiene entrada de bestiario | El Custodio es parte de la narrativa del templo | Decisión futura: ¿añadirle entrada? (cambio de código, fuera de esta tarea) |
| C-13 | Cuatro llaves | Una `temple_key` por templo y un Relicario | Cuatro llaves (cobre, hierro, oro, diamante) en una sola tirada del Custodio y cuatro cámaras | **Diseño aprobado** (09). NO IMPLEMENTADO |
| C-14 | Corrección a mi informe de la Fase 0 | — | — | En la Fase 0 dije que los nombres coincidían: coinciden **ids y categorías**, no todos los nombres (C-10) |
