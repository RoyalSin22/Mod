# 04 · Las tres doctrinas y las 60 sendas

Estado: nombres, ids y efectos de cada senda son **VERIFICADO EN CÓDIGO** (`role/RoleTree.java`: `paths(...)` y
`deeper(...)`; `role/KeystoneRules.java`; nombres en `es_es.json`, claves `path.aptitudes.<rol>_<a|b|c>`). La
columna «Filosofía narrativa» y todo el texto de origen son **PROPUESTA** y **no cambian ninguna habilidad**.

## Principios

1. La formación del rol es la base común.
2. Las sendas son especializaciones; cada personaje elige **una senda exclusiva por rol** (regla vigente, `role/PathRules`).
3. La progresión es el perfeccionamiento de su doctrina.
4. La bifurcación **S5x/S5y** son dos alternativas dentro de la misma senda (solo se puede aprender una).
5. La **piedra angular S7** (nivel 50, un rango) culmina la doctrina.
6. La **maestría** viene del entrenamiento y la experiencia, no de una entidad.
7. **Ninguna** doctrina obtiene sus poderes de la Conciencia Ancestral.

## Origen histórico (PROPUESTA)

El **Concilio de las Tres Vías** reconoció las grandes tradiciones que ya practicaban los maestros de cada
gremio y las documentó para conservarlas. Las diferencias entre doctrinas eran **debates de método** (cómo
aprender y perfeccionarse), no enfrentamientos militares. Cada gremio terminó con tres doctrinas, A, B y C, que
hoy son las tres sendas del rol.

## Significado de los hitos (valores por defecto; son configurables)

| Escalón | Nivel de rol | Rangos | Lectura narrativa (PROPUESTA) | Fuente |
|---|---|---|---|---|
| S1–S3 | 10 · 14 · 20 | 3 | Aprendizaje formativo de la doctrina | `PathRules`, capítulo `paths` |
| S4 | 26 | 3 | Primera maestría propia de la senda | idem |
| S5x / S5y | 32 | 3 | Dos caminos dentro de la doctrina; elegir uno renuncia al otro | idem |
| S6 | 40 | 3 | Repite el efecto de S4 con potencia ×1,5: la maestría se consolida | idem |
| S7 | 50 | 1 | Piedra angular: la culminación de la doctrina | `KeystoneRules` |

(Los niveles de la tabla vienen de la descripción del capítulo `guide.aptitudes.paths.body`; los valores reales
dependen de la configuración. **VERIFICADO** para la tabla de textos, **NO VERIFICADO** para los valores
configurados en `AptitudesConfig`.)

## Significado narrativo de las diez piedras angulares (PROPUESTA)

| Plantilla | Efecto real (resumen) | Significado narrativo |
|---|---|---|
| Carnicería (`carnage`) | Al matar: cura y da Prisa | Quien termina un combate sale reforzado |
| Baluarte (`bulwark`) | Con poca vida: absorción y resistencia | La disciplina del que aguanta cuando todo se pierde |
| Tempestad (`tempest`) | Críticos lanzan una chispa | El golpe perfecto que se prolonga más allá del cuerpo |
| Sobrecarga (`overload`) | Cada N-ésima habilidad cuesta cero | Dominio tan grande que el esfuerzo desaparece |
| Espectro (`specter`) | Tras esquivar, el siguiente golpe es crítico | Estar donde no se espera |
| Detonación (`detonation`) | Al matar: explosión | El fin de un enemigo, ruido del final |
| Contagio (`contagion`) | Los efectos pasan a otros enemigos | Lo que se siembra se propaga |
| Santuario (`sanctuary`) | Cura periódica al grupo | La doctrina vuelta cuidado colectivo |
| Fortuna (`fortune`) | Botín extra | El oficio que siempre saca provecho |
| Legión (`legion`) | +1 esbirro máximo, esbirros curan | El saber de mandar sobre lo que acompaña |

## Fichas de las 60 sendas

Leyenda de columnas: **S1–S3** efectos de los escalones iniciales; **S4 / S6** efecto del escalón S4 (S6 lo
repite ×1,5); **S5x / S5y** efectos de la bifurcación; **Piedra angular** plantilla del S7. Los **nombres de
efecto** son los del juego (`path_effect.aptitudes.*`).

Campos de la ficha pedida en el encargo y su estado:

| Campo | Estado |
|---|---|
| Identificador real, nombre oficial, rol, categoría | VERIFICADO |
| Habilidades implementadas (efectos S1–S6) | VERIFICADO |
| Piedra angular | VERIFICADO |
| Filosofía narrativa | PROPUESTA |
| Origen histórico | PROPUESTA (común a todas: ver arriba) |
| Posibles contradicciones | ver CONTRADICCIONES (C-09 para nombres duplicados) |

> Nota: el nombre del **nodo** que aparece en el árbol de habilidades (títulos de las habilidades activas del
> rol) no se ha leído aquí. Los efectos listados son los **efectos de senda** (`PathEffect`), no las activas.

### Bersérker · `berserker` · categoría `MARTIAL` · Hermandad del Ímpetu

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Carnicero** | `path.aptitudes.berserker_a` | Robo de vida · Golpe crítico (×1,5) · Ejecución (+daño a enemigos bajo el 35 % de vida) | Robo de vida | Golpe crítico (×1,5) / Ejecución (+daño a enemigos bajo el 35 % de vida) | Carnicería (`carnage`) | Vive de la sangre que derrama: cada golpe devuelve el vigor que costó darlo. |
| **B · Coloso** | `path.aptitudes.berserker_b` | Espinas (devuelve daño cuerpo a cuerpo) · Resistencia al empuje · Reducción de daño | Reducción de daño | Espinas (devuelve daño cuerpo a cuerpo) / Resistencia al empuje | Baluarte (`bulwark`) | Aprende a ser el muro que no cede; aguantar el golpe es el modo de ganar. |
| **C · Torbellino** | `path.aptitudes.berserker_c` | Velocidad de ataque · Velocidad · Regeneración de vida (cada 3 s) | Velocidad de ataque | Velocidad / Regeneración de vida (cada 3 s) | Tempestad (`tempest`) | Convierte el ritmo en arma: golpear de nuevo antes de que el rival se reponga. |

### Arcanista · `mage` · categoría `ARCANE` · Círculo del Saber Arcano

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Arcanista** | `path.aptitudes.mage_a` | Golpe crítico (×1,5) · Ejecución (+daño a enemigos bajo el 35 % de vida) · Probabilidad de marchitar | Golpe crítico (×1,5) | Ejecución (+daño a enemigos bajo el 35 % de vida) / Probabilidad de marchitar | Tempestad (`tempest`) | Concentra el poder en pocas descargas decisivas y no desperdicia ninguna. |
| **B · Erudito** | `path.aptitudes.mage_b` | Regeneración de maná (por segundo) · Carga del recurso de rol · Reducción de daño | Regeneración de maná (por segundo) | Carga del recurso de rol / Reducción de daño | Sobrecarga (`overload`) | El saber es la reserva: quien entiende el flujo del maná casi no lo agota. |
| **C · Cronomante** | `path.aptitudes.mage_c` | Probabilidad de ralentizar · Esquiva · Regeneración de maná (por segundo) | Probabilidad de ralentizar | Esquiva / Regeneración de maná (por segundo) | Espectro (`specter`) | Estira el instante: frena al rival y se desliza entre sus golpes. |

### Asesino · `assassin` · categoría `SHADOW` · Cofradía del Paso Silente

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Verdugo** | `path.aptitudes.assassin_a` | Golpe crítico (×1,5) · Ejecución (+daño a enemigos bajo el 35 % de vida) · Probabilidad de envenenar | Ejecución (+daño a enemigos bajo el 35 % de vida) | Golpe crítico (×1,5) / Probabilidad de envenenar | Espectro (`specter`) | Un solo golpe, bien elegido, basta; la precisión entendida como ejecución. |
| **B · Fantasma** | `path.aptitudes.assassin_b` | Esquiva · Velocidad · Probabilidad de ralentizar | Esquiva | Velocidad / Probabilidad de ralentizar | Detonación (`detonation`) | Nunca está donde cae el golpe; la huida es parte del ataque. |
| **C · Sombra carmesí** | `path.aptitudes.assassin_c` | Probabilidad de marchitar · Robo de vida · Velocidad de ataque | Probabilidad de marchitar | Robo de vida / Velocidad de ataque | Contagio (`contagion`) | Marchita y desangra; cada herida abierta es un recado que se contagia. |

### Boticario · `herbalist` · categoría `GUARDIAN` · Colegio de la Raíz Serena

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Sanador** | `path.aptitudes.herbalist_a` | Regeneración de vida (cada 3 s) · Vida máxima · Reducción de daño | Regeneración de vida (cada 3 s) | Vida máxima / Reducción de daño | Santuario (`sanctuary`) | La planta como remedio: sostener vivos a los suyos. |
| **B · Envenenador** | `path.aptitudes.herbalist_b` | Probabilidad de envenenar · Probabilidad de ralentizar · Ejecución (+daño a enemigos bajo el 35 % de vida) | Probabilidad de envenenar | Probabilidad de ralentizar / Ejecución (+daño a enemigos bajo el 35 % de vida) | Contagio (`contagion`) | La misma planta, en dosis equivocada: el remedio vuelto veneno. |
| **C · Alquimista** | `path.aptitudes.herbalist_c` | Carga del recurso de rol · Regeneración de maná (por segundo) · Suerte | Carga del recurso de rol | Regeneración de maná (por segundo) / Suerte | Fortuna (`fortune`) | Mezclas y preparados: obtener más de cada recurso con paciencia y ojo. |

### Ingeniero · `engineer` · categoría `CRAFT` · Colegio de los Artífices

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Artillero** | `path.aptitudes.engineer_a` | Golpe crítico (×1,5) · Ejecución (+daño a enemigos bajo el 35 % de vida) · Regeneración de maná (por segundo) | Golpe crítico (×1,5) | Ejecución (+daño a enemigos bajo el 35 % de vida) / Regeneración de maná (por segundo) | Detonación (`detonation`) | Ingeniería como artillería: potencia concentrada y alcance. |
| **B · Acorazado** | `path.aptitudes.engineer_b` | Reducción de daño · Resistencia al empuje · Vida máxima | Reducción de daño | Resistencia al empuje / Vida máxima | Baluarte (`bulwark`) | Ingeniería como blindaje: el mecanismo que protege a quien lo lleva. |
| **C · Sobrecargado** | `path.aptitudes.engineer_c` | Velocidad de ataque · Velocidad · Carga del recurso de rol | Velocidad de ataque | Velocidad / Carga del recurso de rol | Sobrecarga (`overload`) | Ingeniería forzada al límite: más cadencia, más riesgo, más ritmo. |

### Lancero · `lancer` · categoría `MARTIAL` · Orden de la Lanza Inquebrantable

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Dragón** | `path.aptitudes.lancer_a` | Golpe crítico (×1,5) · Velocidad · Probabilidad de ralentizar | Golpe crítico (×1,5) | Velocidad / Probabilidad de ralentizar | Tempestad (`tempest`) | La lanza como símbolo de velocidad y alcance: golpear primero y desde lejos. |
| **B · Falange** | `path.aptitudes.lancer_b` | Espinas (devuelve daño cuerpo a cuerpo) · Resistencia al empuje · Reducción de daño | Espinas (devuelve daño cuerpo a cuerpo) | Resistencia al empuje / Reducción de daño | Baluarte (`bulwark`) | La lanza como parte de una línea: sostener la posición ante todo. |
| **C · Empalador** | `path.aptitudes.lancer_c` | Robo de vida · Vida máxima · Ejecución (+daño a enemigos bajo el 35 % de vida) | Robo de vida | Vida máxima / Ejecución (+daño a enemigos bajo el 35 % de vida) | Carnicería (`carnage`) | La lanza como punta que se clava: castigar al que ya está herido. |

### Duelista · `swordsman` · categoría `MARTIAL` · Escuela del Filo Eterno

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Duelista** | `path.aptitudes.swordsman_a` | Esquiva · Golpe crítico (×1,5) · Velocidad de ataque | Golpe crítico (×1,5) | Velocidad de ataque / Esquiva | Espectro (`specter`) | El duelo como arte: ritmo y precisión contra un solo rival. |
| **B · Kensei** | `path.aptitudes.swordsman_b` | Robo de vida · Ejecución (+daño a enemigos bajo el 35 % de vida) · Probabilidad de ralentizar | Robo de vida | Ejecución (+daño a enemigos bajo el 35 % de vida) / Probabilidad de ralentizar | Carnicería (`carnage`) | El camino del filo: sangre fría, acabar al enemigo debilitado. |
| **C · Guardián de acero** | `path.aptitudes.swordsman_c` | Reducción de daño · Espinas (devuelve daño cuerpo a cuerpo) · Vida máxima | Reducción de daño | Espinas (devuelve daño cuerpo a cuerpo) / Vida máxima | Baluarte (`bulwark`) | La espada que también guarda: resistir hasta que llegue el momento. |

### Paladín · `paladin` · categoría `GUARDIAN` · Orden del Escudo del Alba

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Templario** | `path.aptitudes.paladin_a` | Ejecución (+daño a enemigos bajo el 35 % de vida) · Robo de vida · Probabilidad de quemar | Ejecución (+daño a enemigos bajo el 35 % de vida) | Golpe crítico (×1,5) / Probabilidad de quemar | Detonación (`detonation`) | La fe como espada: devolver al mundo el mal que se le hace. |
| **B · Guardián** | `path.aptitudes.paladin_b` | Reducción de daño · Espinas (devuelve daño cuerpo a cuerpo) · Regeneración de vida (cada 3 s) | Reducción de daño | Espinas (devuelve daño cuerpo a cuerpo) / Regeneración de vida (cada 3 s) | Baluarte (`bulwark`) | La fe como escudo: proteger a la comunidad aguantando por ella. |
| **C · Cruzado** | `path.aptitudes.paladin_c` | Golpe crítico (×1,5) · Velocidad de ataque · Carga del recurso de rol | Golpe crítico (×1,5) | Velocidad de ataque / Carga del recurso de rol | Santuario (`sanctuary`) | La fe como causa: avanzar sin pausa, golpe tras golpe, por un fin común. |

### Nigromante · `necromancer` · categoría `ARCANE` · Custodios del Último Eco

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Señor de esbirros** | `path.aptitudes.necromancer_a` | Poder de los esbirros · Carga del recurso de rol · Regeneración de maná (por segundo) | Poder de los esbirros | Carga del recurso de rol / Regeneración de maná (por segundo) | Legión (`legion`) | Los ecos como compañía: dirigir a quienes ya no tienen voz propia. |
| **B · Hematomante** | `path.aptitudes.necromancer_b` | Robo de vida · Probabilidad de marchitar · Vida máxima | Robo de vida | Probabilidad de marchitar / Vida máxima | Contagio (`contagion`) | Los ecos como sangre: tomar lo que queda de la vida de la víctima. |
| **C · Plaga** | `path.aptitudes.necromancer_c` | Probabilidad de envenenar · Probabilidad de ralentizar · Ejecución (+daño a enemigos bajo el 35 % de vida) | Probabilidad de envenenar | Probabilidad de ralentizar / Ejecución (+daño a enemigos bajo el 35 % de vida) | Detonación (`detonation`) | Los ecos como pestilencia: la muerte que se extiende por sí sola. |

### Druida · `druid` · categoría `GUARDIAN` · Custodios del Bosque Antiguo

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Guardián salvaje** | `path.aptitudes.druid_a` | Vida máxima · Reducción de daño · Regeneración de vida (cada 3 s) | Vida máxima | Reducción de daño / Regeneración de vida (cada 3 s) | Santuario (`sanctuary`) | El bosque como hogar: resistir y regenerarse junto a lo que se protege. |
| **B · Cazador lunar** | `path.aptitudes.druid_b` | Golpe crítico (×1,5) · Velocidad · Probabilidad de envenenar | Golpe crítico (×1,5) | Velocidad / Probabilidad de envenenar | Carnicería (`carnage`) | La luna como guía: cazar rápido y con veneno bajo su luz. |
| **C · Espino** | `path.aptitudes.druid_c` | Espinas (devuelve daño cuerpo a cuerpo) · Resistencia al empuje · Probabilidad de ralentizar | Espinas (devuelve daño cuerpo a cuerpo) | Resistencia al empuje / Probabilidad de ralentizar | Contagio (`contagion`) | La espina como defensa: quien ataca al druida se lastima al hacerlo. |

### Mercader táctico · `merchant` · categoría `CRAFT` · Liga de las Rutas Doradas

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Tesorero** | `path.aptitudes.merchant_a` | Carga del recurso de rol · Regeneración de maná (por segundo) · Suerte | Carga del recurso de rol | Regeneración de maná (por segundo) / Suerte | Fortuna (`fortune`) | La rentabilidad como virtud: sacar más de cada recurso y cada ocasión. |
| **B · Mercenario** | `path.aptitudes.merchant_b` | Golpe crítico (×1,5) · Robo de vida · Ejecución (+daño a enemigos bajo el 35 % de vida) | Golpe crítico (×1,5) | Robo de vida / Ejecución (+daño a enemigos bajo el 35 % de vida) | Detonación (`detonation`) | El contrato como espada: se paga por la fuerza y se cobra con ella. |
| **C · Contrabandista** | `path.aptitudes.merchant_c` | Esquiva · Velocidad · Reducción de daño | Esquiva | Velocidad / Reducción de daño | Espectro (`specter`) | La ruta discreta: esquivar peajes, riesgos y miradas. |

### Monje · `monk` · categoría `MARTIAL` · Claustro del Pulso Interior

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Puño de hierro** | `path.aptitudes.monk_a` | Golpe crítico (×1,5) · Velocidad de ataque · Probabilidad de ralentizar | Golpe crítico (×1,5) | Velocidad de ataque / Esquiva | Tempestad (`tempest`) | Disciplina de golpes breves y encadenados; el cuerpo como herramienta. |
| **B · Loto** | `path.aptitudes.monk_b` | Esquiva · Regeneración de vida (cada 3 s) · Regeneración de maná (por segundo) | Regeneración de vida (cada 3 s) | Esquiva / Regeneración de maná (por segundo) | Santuario (`sanctuary`) | Calma interior: equilibrio, serenidad y recuperación. |
| **C · Puño del dragón** | `path.aptitudes.monk_c` | Robo de vida · Resistencia al empuje · Ejecución (+daño a enemigos bajo el 35 % de vida) | Robo de vida | Resistencia al empuje / Ejecución (+daño a enemigos bajo el 35 % de vida) | Carnicería (`carnage`) | El golpe que sacude hasta al que lo recibe de lejos: fuerza concentrada y desplazamiento. |

### Chamán · `elementalist` · categoría `ARCANE` · Concilio de las Fuerzas Primarias

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Catalizador** | `path.aptitudes.elementalist_a` | Golpe crítico (×1,5) · Ejecución (+daño a enemigos bajo el 35 % de vida) · Probabilidad de quemar | Probabilidad de quemar | Golpe crítico (×1,5) / Ejecución (+daño a enemigos bajo el 35 % de vida) | Detonación (`detonation`) | El elemento como catalizador: prender lo que ya estaba a punto. |
| **B · Conductor** | `path.aptitudes.elementalist_b` | Regeneración de maná (por segundo) · Carga del recurso de rol · Reducción de daño | Regeneración de maná (por segundo) | Carga del recurso de rol / Reducción de daño | Sobrecarga (`overload`) | El elemento como corriente: canalizar con eficiencia, sin agotar la fuente. |
| **C · Tempestad** | `path.aptitudes.elementalist_c` | Probabilidad de ralentizar · Esquiva · Velocidad | Probabilidad de ralentizar | Esquiva / Velocidad | Tempestad (`tempest`) | La tormenta como método: entorpecer, escabullirse y volver a atacar. |

### Cambiaformas · `shapeshifter` · categoría `SHADOW` · Sociedad de las Mil Formas (renombrado; el contexto decía «Herederos…»)

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Depredador** | `path.aptitudes.shapeshifter_a` | Golpe crítico (×1,5) · Robo de vida · Velocidad | Golpe crítico (×1,5) | Robo de vida / Velocidad | Espectro (`specter`) | La forma del cazador: rapidez y colmillo ante todo. |
| **B · Mimo** | `path.aptitudes.shapeshifter_b` | Esquiva · Carga del recurso de rol · Regeneración de maná (por segundo) | Esquiva | Carga del recurso de rol / Regeneración de maná (por segundo) | Fortuna (`fortune`) | La forma imitada: pasar desapercibido hasta que conviene dejar de hacerlo. |
| **C · Coloso bestial** | `path.aptitudes.shapeshifter_c` | Vida máxima · Reducción de daño · Espinas (devuelve daño cuerpo a cuerpo) | Vida máxima | Reducción de daño / Espinas (devuelve daño cuerpo a cuerpo) | Baluarte (`bulwark`) | La forma del coloso: tamaño y aguante por encima de la astucia. |

### Mago pistolero · `gunslinger` · categoría `ARCANE` · Orden del Disparo Rúnico

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Pistolero** | `path.aptitudes.gunslinger_a` | Golpe crítico (×1,5) · Velocidad de ataque · Ejecución (+daño a enemigos bajo el 35 % de vida) | Golpe crítico (×1,5) | Ejecución (+daño a enemigos bajo el 35 % de vida) / Velocidad de ataque | Tempestad (`tempest`) | La puntería como oficio: cada disparo cuenta, el ritmo es la clave. |
| **B · Tirador arcano** | `path.aptitudes.gunslinger_b` | Regeneración de maná (por segundo) · Carga del recurso de rol · Esquiva | Regeneración de maná (por segundo) | Carga del recurso de rol / Esquiva | Sobrecarga (`overload`) | La balística rúnica: aprovechar el flujo arcano de cada cartucho. |
| **C · Forajido** | `path.aptitudes.gunslinger_c` | Robo de vida · Velocidad · Probabilidad de ralentizar | Robo de vida | Velocidad / Probabilidad de ralentizar | Espectro (`specter`) | El disparo del forajido: moverse, ralentizar y no dejar que se acerquen. |

### Invocador de armas · `weapon_summoner` · categoría `CRAFT` · Forja de las Armas Vinculadas

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Espadas danzantes** | `path.aptitudes.weapon_summoner_a` | Golpe crítico (×1,5) · Velocidad de ataque · Robo de vida | Velocidad de ataque | Golpe crítico (×1,5) / Robo de vida | Tempestad (`tempest`) | Las armas vinculadas como enjambre: muchas hojas, una voluntad. |
| **B · Bastión de acero** | `path.aptitudes.weapon_summoner_b` | Reducción de daño · Espinas (devuelve daño cuerpo a cuerpo) · Vida máxima | Reducción de daño | Espinas (devuelve daño cuerpo a cuerpo) / Vida máxima | Baluarte (`bulwark`) | El arsenal como fortaleza: armas que protegen a quien las invoca. |
| **C · Forja rúnica** | `path.aptitudes.weapon_summoner_c` | Carga del recurso de rol · Regeneración de maná (por segundo) · Ejecución (+daño a enemigos bajo el 35 % de vida) | Carga del recurso de rol | Regeneración de maná (por segundo) / Ejecución (+daño a enemigos bajo el 35 % de vida) | Sobrecarga (`overload`) | La forja de runas: sacar más del vínculo entre acero y voluntad. |

### Cazador · `hunter` · categoría `SHADOW` · Hermandad de la Huella

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Francotirador** | `path.aptitudes.hunter_a` | Golpe crítico (×1,5) · Ejecución (+daño a enemigos bajo el 35 % de vida) · Probabilidad de ralentizar | Ejecución (+daño a enemigos bajo el 35 % de vida) | Golpe crítico (×1,5) / Probabilidad de envenenar | Contagio (`contagion`) | La paciencia del tirador: esperar, apuntar, rematar. |
| **B · Trampero** | `path.aptitudes.hunter_b` | Probabilidad de envenenar · Esquiva · Velocidad | Esquiva | Velocidad / Probabilidad de ralentizar | Fortuna (`fortune`) | El terreno como arma: trampas, venenos y rapidez para moverse en él. |
| **C · Bestia** | `path.aptitudes.hunter_c` | Robo de vida · Vida máxima · Resistencia al empuje | Robo de vida | Vida máxima / Resistencia al empuje | Carnicería (`carnage`) | El cazador que adopta la bestia: fuerza y resistencia ante la presa. |

### Bardo · `bard` · categoría `GUARDIAN` · Coro de la Memoria Viva

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Juglar** | `path.aptitudes.bard_a` | Carga del recurso de rol · Regeneración de maná (por segundo) · Suerte | Carga del recurso de rol | Regeneración de maná (por segundo) / Suerte | Santuario (`sanctuary`) | La canción como ahorro y buen comercio: aprovechar cada nota. |
| **B · Trovador de guerra** | `path.aptitudes.bard_b` | Golpe crítico (×1,5) · Velocidad de ataque · Probabilidad de quemar | Golpe crítico (×1,5) | Velocidad de ataque / Probabilidad de quemar | Tempestad (`tempest`) | La canción de guerra: ritmo y valor para el combate. |
| **C · Cantor sagrado** | `path.aptitudes.bard_c` | Regeneración de vida (cada 3 s) · Reducción de daño · Vida máxima | Regeneración de vida (cada 3 s) | Reducción de daño / Vida máxima | Baluarte (`bulwark`) | La canción que cura: consuelo y aguante para todo el grupo. |

### Brujo abisal · `warlock` · categoría `SHADOW` · Círculo de los Secretos Velados

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Maldito** | `path.aptitudes.warlock_a` | Probabilidad de marchitar · Probabilidad de envenenar · Ejecución (+daño a enemigos bajo el 35 % de vida) | Probabilidad de marchitar | Probabilidad de envenenar / Ejecución (+daño a enemigos bajo el 35 % de vida) | Contagio (`contagion`) | El secreto vedado: marchitar y envenenar a distancia. |
| **B · Sangre maldita** | `path.aptitudes.warlock_b` | Robo de vida · Vida máxima · Regeneración de vida (cada 3 s) | Robo de vida | Vida máxima / Regeneración de vida (cada 3 s) | Carnicería (`carnage`) | El precio de la sangre: obtener vida de lo que se cobra al otro. |
| **C · Abismo** | `path.aptitudes.warlock_c` | Golpe crítico (×1,5) · Regeneración de maná (por segundo) · Carga del recurso de rol | Golpe crítico (×1,5) | Regeneración de maná (por segundo) / Carga del recurso de rol | Sobrecarga (`overload`) | El abismo de lo desconocido: conocimiento que se consume al usarse. |

### Domador · `tamer` · categoría `CRAFT` · Compañía de la Manada (renombrado; el contexto decía «Pacto…»)

| Senda | Id (clave de idioma) | S1–S3 | S4 / S6 | S5x / S5y | Piedra angular (S7) | Filosofía narrativa (PROPUESTA) |
|---|---|---|---|---|---|---|
| **A · Jauría** | `path.aptitudes.tamer_a` | Resistencia al empuje · Vida máxima · Reducción de daño | Reducción de daño | Vida máxima / Resistencia al empuje | Legión (`legion`) | La jauría: cooperar hasta que el grupo actúa como un cuerpo. |
| **B · Salvaje** | `path.aptitudes.tamer_b` | Golpe crítico (×1,5) · Velocidad de ataque · Robo de vida | Golpe crítico (×1,5) | Velocidad de ataque / Robo de vida | Carnicería (`carnage`) | La manada salvaje: dejar que la criatura y su domador golpeen juntos. |
| **C · Pastor** | `path.aptitudes.tamer_c` | Regeneración de vida (cada 3 s) · Suerte · Carga del recurso de rol | Regeneración de vida (cada 3 s) | Suerte / Carga del recurso de rol | Santuario (`sanctuary`) | El pastor: cuidar, curar y dar más de lo que se recibe. |

