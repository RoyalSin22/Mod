# Aptitudes — Mod de habilidades y atributos (Fabric 1.21.1)

Sistema RPG con tres capas que se combinan:

- **8 habilidades** que suben **por uso**: Minería, Excavación, Tala, Agricultura, Combate, Tiro con arco, Defensa y Agilidad. Cada una da una bonificación pasiva por nivel.
- **Progresión estilo Souls**: 8 atributos (Vigor, Mente, Aguante, Fuerza, Destreza, Inteligencia, Fe y Suerte) que se suben **en una Hoguera**, pagando **niveles de XP vanilla**. Al morir, tu XP queda en el suelo y puedes recuperarla.
- **Maná + recurso de rol**: todas las habilidades gastan Maná; cada rol tiene además un recurso propio (Furia, Sangre, Chi…) que **potencia** sus habilidades.
- **14 roles** (clases) en **5 categorías**: Bersérker, Mago, Asesino, Herbolista, Ingeniero, Lancero, Espadachín, Paladín, Nigromante, Druida, Mercader, Monje, Elementalista y **Cambiaformas**. Eliges uno **principal** y uno **secundario**; cada rol tiene un **árbol** con 2 pasivas y 2 habilidades activas, que se usan con teclas y gastan **energía**.
- **Evolución de roles**: Base → **Maestro** → **Definitivo** (nivel + prueba + ítem), con títulos propios, una **habilidad característica** al llegar a Maestro (tecla `Z`) y una **definitiva** al llegar a Definitivo (tecla `R`).
- **16 habilidades únicas** (tecla `H`; catálogo con `U`): cada una tiene **un solo dueño en todo el servidor**.
- **17 ítems nuevos** con receta (uno por rol, el Ingeniero tiene dos, y 2 ítems de evolución).
- **GUI** rediseñada (`K` atributos/habilidades, `J` roles, `U` únicas, `G` cambiar elemento), **HUD** de energía, enfriamientos y elemento activo, **comandos** y **configuración JSON**.

## Documentación técnica

- [docs/ROADMAP_APTITUDES.md](docs/ROADMAP_APTITUDES.md): estado real, cambios y pendientes.
- [docs/BALANCE.md](docs/BALANCE.md): fórmulas, límites y decisiones de balance.
- [docs/CONFIGURATION.md](docs/CONFIGURATION.md): todas las opciones y su validación.
- [docs/TESTING.md](docs/TESTING.md): pruebas automatizadas y manuales.

## Requisitos

| Componente | Versión |
|---|---|
| Minecraft | 1.21.1 |
| Java (JDK) | 21 |
| Fabric Loader | ≥ 0.16.0 |
| Fabric API | 0.116.6+1.21.1 (cualquier versión para 1.21.1 debería servir) |
| Gradle | 8.14.3 (incluido mediante el wrapper) |

> Las versiones están en `gradle.properties`. Si alguna no se resuelve, consulta las vigentes en <https://fabricmc.net/develop>.

## Compilar y probar

```bash
./gradlew build          # JAR en build/libs/aptitudes-1.0.0.jar
./gradlew runClient      # lanza el cliente de desarrollo
./gradlew runServer      # lanza un servidor dedicado de desarrollo
```

Para instalarlo, copia el JAR en `.minecraft/mods/` junto con Fabric API. **Servidor y clientes** deben tener el mod para usar la GUI.

## Mecánicas

### Habilidades: cómo se gana XP y qué bonificación dan

| Habilidad | Fuente de XP | Bonificación pasiva por nivel (por defecto) |
|---|---|---|
| Minería | Piedra base (1) y minerales (10). Sin XP con Toque de seda | +2 % de velocidad en bloques de pico |
| Excavación | Bloques de pala (1) | +2 % de velocidad en bloques de pala |
| Tala | Troncos (4) | +2 % de velocidad en bloques de hacha |
| Agricultura | Cultivos maduros (5) | +1 % de probabilidad de cosecha doble |
| Combate | Muertes cuerpo a cuerpo (vida máx. de la víctima × 0,5) | +1 % de daño de ataque (sobre el total) |
| Tiro con arco | Muertes con proyectil (vida máx. × 0,6) | +1,5 % de daño de flecha |
| Defensa | Daño recibido de entidades (× 1,0) | +0,1 de dureza de armadura |
| Agilidad | Cada 10 bloques esprintados (2) | +0,2 % de velocidad de movimiento |
| Pesca | Cada pez capturado (12) | +0,02 de suerte, +0,4 % de captura doble |
| Encantamiento | Cada objeto encantado en la mesa (25) | +1 % de reembolso de 1 nivel, +0,4 de maná máximo |

XP necesaria para pasar del nivel *L* al *L+1*: `xpBase · (L+1)^xpExponent` (20 · (L+1)^1,35 por defecto). El nivel máximo por defecto es 50.

### Atributos estilo Souls (valor de cada punto, por defecto)

| Atributo | Efecto por punto |
|---|---|
| Vigor | +1 de vida máxima |
| Mente | +5 de maná máximo y +0,05 de maná/s |
| Aguante | +0,5 de armadura |
| Fuerza | +0,25 de daño cuerpo a cuerpo |
| Destreza | +1 % de velocidad de ataque |
| Inteligencia | +2 % de daño de hechizos, explosiones y ataques a distancia de habilidades |
| Fe | −1 % de enfriamiento (junto con Mente clara y Presteza, máximo −60 %) |
| Suerte | +0,2 de suerte |

Máximo 99 puntos por atributo. Las 8 habilidades por uso siguen subiendo y dando sus bonificaciones, pero ya **no dan puntos** (`pointsPerSkillLevel = 0`).

### Hoguera y subida de nivel

- **La Hoguera** se fabrica con `·S· / BCB` (S = espada de hierro, B = piedra, C = fogata). Al descansar en ella (clic derecho) recuperas toda la vida y el maná, fija tu **punto de reaparición** y se abre la pantalla de subida de nivel.
- **Subir de nivel:** los puntos que prepares por encima de los libres se **compran como niveles de alma**. Cada nivel cuesta `1 + nivel / 3` niveles de XP vanilla (máximo 30, configurable). La pantalla muestra el coste y la XP que tienes antes de confirmar.
- **Mancha de XP:** al morir, **toda** tu XP (no solo los 100 puntos de vanilla) queda en el lugar de la muerte, marcada con llamas de alma que solo tú ves. Si vuelves a ese punto la recuperas; si mueres antes, la mancha anterior se pierde. Se desactiva con `keepInventory` o con `soulsBloodstain = false`.

## Roles

**Principal + secundario.** Ocupar una ranura vacía es gratis; sustituir un rol ya elegido activa un enfriamiento (`roleChangeCooldownMinutes`, 30 min de juego por defecto). El progreso de cada rol se conserva aunque deje de estar activo, pero solo los dos roles activos ganan XP y aplican sus nodos. El secundario gana el 50 % de la XP (`secondaryRoleXpMultiplier`).

**Árbol.** Cada rol tiene 4 nodos con 3 rangos cada uno:

```
[Pasiva A] (nv 1)  ──►  [Pasiva B] (nv 8)
[Activa A] (nv 3)  ──►  [Activa B] (nv 12)
```

Cada rango cuesta 1 punto del rol y exige 5 niveles más que el anterior. Puntos por rol = `1 + nivel / 3` (11 puntos al nivel 30 para 12 rangos posibles, así que siempre hay que elegir).

**Habilidades activas.** Ranuras `V` y `B` para las activas A y B del rol principal; `N` y `M` para las del secundario (se pueden cambiar en Controles). La energía base es 100 y regenera 2 por segundo. Si una habilidad no tiene objetivo o no hay espacio para usarla, **no** gasta energía ni entra en enfriamiento.

| Rol | XP de rol | Pasiva A | Activa A | Pasiva B | Activa B | Ítem |
|---|---|---|---|---|---|---|
| Bersérker | Golpes con hacha | Piel dura (resistencia al empuje, dureza) | Furia (Fuerza + Velocidad) | Sed de sangre (+daño según la vida que falta) | Grito de guerra (empuja, debilita, ralentiza) | Hacha de bersérker |
| Mago | Bastón arcano y hechizos | Afinidad arcana (+energía y regeneración) | Bola de fuego (impacto en área, no quema bloques) | Mente clara (−enfriamiento de todas las habilidades) | Parpadeo (teletransporte corto) | Bastón arcano (rayo, gasta energía) |
| Asesino | Golpes con daga | Sombra (agacharse más rápido; invisible al agacharse desde el rango 2) | Desvanecerse (invisibilidad; los mobs te pierden) | Puñalada (+daño por la espalda) | Paso sombrío (te teletransporta tras el objetivo) | Daga (ataque muy rápido) |
| Herbolista | Cosechar cultivos, flores y bayas | Savia vital (regeneración pasiva) | Bálsamo (cura en área a jugadores) | Inmunidad herbal (veneno, hambre, descomposición) | Espinas venenosas (veneno en área) | Saquito de hierbas (cura, consumible) |
| Ingeniero | Redstone y mecanismos, llave, pistola de plasma | Ingeniería (+alcance con bloques, +velocidad de rotura, +armadura) | Propulsores (impulso) | Batería (+energía, regeneración, salto y caída segura) | Torreta (gólem de hierro temporal) | Llave inglesa + Pistola de plasma |
| Lancero | Golpes con lanza o tridente | Alcance (+alcance de ataque) | Embestida (carga con daño) | Carga (+daño al esprintar) | Barrido (daño circular) | Lanza (+1 de alcance) |
| Espadachín | Golpes con espada | Filo (+daño con espadas) | Danza de espadas (Prisa + Velocidad) | Parada (probabilidad de anular golpes) | Contraataque (anula y devuelve el siguiente golpe) | Katana (rápida) |
| Paladín | Martillo sagrado, dañar no-muertos, bloquear con escudo | Castigo sagrado (+daño a no-muertos) | Escudo divino (Absorción + Resistencia) | Aura protectora (Resistencia a aliados cercanos) | Juicio (daño en área, ×2 a no-muertos; cura aliados) | Martillo sagrado |
| Nigromante | Tomo, drenaje y daño de sus esbirros | Comunión oscura (energía y vida por muertes cercanas) | Alzar esbirros (zombis/esqueletos aliados) | Drenaje vital (robo de vida, también de esbirros) | Toque mortal (drenaje en área) | Tomo nigromántico (rayo que roba vida) |
| Druida | Plantar retoños, criar animales, usar el cayado | Hijo del bosque (bonos en bosques) | Raíces (inmoviliza) | Crecimiento (los cultivos cercanos crecen solos) | Forma de oso | Cayado de roble (abona 3×3) |
| Mercader | Comerciar con aldeanos | Regateo (Héroe de la aldea permanente) | Ojo del tasador (Suerte + revelar criaturas) | Bolsillos profundos (esmeralda extra al comerciar) | Soborno (1 esmeralda: los enemigos dejan de atacar) | Sello del gremio (repone ofertas de un aldeano) |
| Monje | Golpes a mano vacía | Puños de hierro (+daño desarmado) | Palma de chi (golpe y empujón) | Esquiva (evita ataques) | Meditación (regeneración, resistencia y energía) | Vendas de puño (mano secundaria) |
| Elementalista | Hechizos elementales | Afinidad elemental (pasiva del elemento activo) | Proyectil elemental | Dominio elemental (pasivas de los otros elementos dominados) | Estallido elemental | Orbe elemental (cambia de elemento) |

### Elementalista

Domina **hasta 3 de 9 elementos**: el 1.º al elegir el rol, el 2.º al nivel 10 y el 3.º al nivel 20. Se eligen en la pantalla de roles y la elección es permanente: solo se liberan reiniciando el árbol (`/roles respec elementalist`). Con `G` (o el Orbe) cambias el **elemento activo**, que determina el efecto de los nodos.

| Elemento | Proyectil | Estallido | Pasiva |
|---|---|---|---|
| Fuego | Daño + quemadura | Quema en área | Resistencia al fuego |
| Hielo | Ralentiza y congela | Congelación en área | Inmune a la congelación |
| Rayo | Daño en cadena a r enemigos | Rayos (solo visuales) sobre 1+r enemigos | +3 % de velocidad/rango |
| Tierra | Lanza al objetivo por los aires | Lanza y ralentiza en área | +1 de armadura/rango |
| Viento | Gran empuje | Empuje + levitación; te da Velocidad | +3 bloques de caída segura/rango |
| Agua | Daño que te cura | Cura a los aliados y empuja | Respiración acuática (+ nado rápido desde el rango 2) |
| Planta | Veneno | Raíces + veneno | Regeneración sobre tierra u hojas |
| Magma | Daño alto + fuego + empuje | Daño de fuego alto | Inmune a bloques de magma, +1 de dureza/rango |
| Sombra | Descomposición + ceguera | Ceguera y debilidad; te vuelves invisible | Visión nocturna en la oscuridad |

### Nigromante: esbirros

- Hasta **4 esbirros** a la vez, durante 45 s. Al superar el máximo, desaparece el más antiguo.
- **Escalan con el nivel** de Nigromante: +0,6 de vida, +0,15 de daño y +0,25 de armadura por nivel.
- Llevan casco (no arden al sol), no recogen ni sueltan objetos y no dan experiencia.
- **Nunca atacan a jugadores** ni a otras invocaciones. Tampoco a aldeanos ni animales: solo a monstruos o a mobs que estén atacando.
- Persiguen a quien ataca a su dueño, o al hostil más cercano. Si se alejan más de 28 bloques, se teletransportan junto al dueño.
- Desaparecen si el dueño se desconecta o al reiniciar el servidor.

Los valores exactos por rango aparecen en el tooltip de cada nodo (pantalla de roles) y están en `Abilities.java` y `RolePassives.java`.

**Recetas** (14 ítems; las texturas son placeholder de 16×16 generadas por código, en `assets/aptitudes/textures/item/`):

| Ítem | Receta |
|---|---|
| Hacha de bersérker | `III / ISI / ·S·` (I = lingote de hierro, S = palo) |
| Bastón arcano | `·LA / ·SL / S··` (L = lapislázuli, A = fragmento de amatista) |
| Daga | `I / S` |
| Saquito de hierbas (×2) | Sin forma: cuero + 2 flores pequeñas + semillas de trigo |
| Llave inglesa | `I·I / ·I· / ·I·` |
| Pistola de plasma | `RGR / III / I··` (R = redstone, G = polvo de piedra luminosa) |
| Lanza | `··I / ·S· / S··` |
| Katana | `··I / ·I· / NS·` (N = pepita de oro) |
| Martillo sagrado | `GIG / GSG / ·S·` (G = lingote de oro) |
| Tomo nigromántico | `BSB / BKB / BRB` (B = hueso, S = arena de almas, K = libro, R = carne podrida) |
| Cayado de roble | `·LB / ·SL / S··` (L = hojas, B = polvo de hueso) |
| Sello del gremio | `·G· / GEG / ·G·` (E = esmeralda) |
| Vendas de puño | `WLW` (W = lana, L = cuero) |
| Orbe elemental | `BAM / LEP / SIG` (polvo de blaze, amatista, crema de magma, lapislázuli, perla de ender, fragmento de prismarina, bola de nieve, saco de tinta, polvo de piedra luminosa) |

## Evolución de roles

| Nivel de evolución | Requisitos | Efecto |
|---|---|---|
| **Maestro** | Nivel 20 + prueba del rol + **Emblema de maestría** (se consume) | +1 al rango efectivo de todos los nodos (pueden superar el rango 3) y nuevo título |
| **Definitivo** | Ser Maestro, nivel 30, haber derrotado al **Wither, al Dragón o al Warden** con el rol activo (cuenta a menos de 96 bloques) + **Núcleo definitivo** | +2 al rango efectivo, título y **habilidad definitiva** (tecla `R`; 80 de energía; 3 min de espera) |

Se evoluciona con el botón **Evolucionar** de la pantalla de roles (su tooltip muestra los requisitos con ✔/✘) o con `/roles evolve <rol>`. Si los dos roles activos son Definitivos, `R` usa la definitiva del principal.

| Rol | Maestro | Definitivo | Prueba de Maestro | Habilidad definitiva |
|---|---|---|---|---|
| Bersérker | Señor de la Guerra | Avatar de la Ira | 150 muertes con hacha | Ira imparable: 8 s sin poder morir, Fuerza II, Velocidad II |
| Mago | Archimago | Soberano Arcano | 300 usos de habilidades | Singularidad: agujero negro que atrae y daña durante 5 s |
| Asesino | Maestro de las Sombras | Segador Silencioso | 150 muertes con daga | Ejecución sombría: salta tras 5 enemigos; mata a los que tienen menos del 30 % de vida |
| Herbolista | Alquimista Verde | Guardián del Edén | 500 cosechas | Jardín del Edén: zona de regeneración y purificación |
| Ingeniero | Maestro Ingeniero | Arquitecto del Mañana | 200 usos de habilidades | Bombardeo orbital: 12 impactos que solo dañan a enemigos |
| Lancero | Lancero Real | Lanza del Cielo | 150 muertes con lanza o tridente | Lanza celestial: salto y caída con onda de choque |
| Espadachín | Maestro de la Espada | Santo de la Espada | 150 muertes con espada | Mil cortes: 15 tajos en 3 s |
| Paladín | Cruzado | Rey Eterno | 150 no-muertos | Reino eterno: zona donde los aliados no pueden morir |
| Nigromante | **Noble de Sangre** | **Progenitor Carmesí** | 250 almas (muertes cercanas) | Marea carmesí: drenaje en área + ejército completo y enfurecido |
| Druida | Archidruida | Espíritu del Bosque | 300 acciones (plantar, criar, abonar) | Furia de la naturaleza: 3 lobos espirituales + raíces |
| Mercader | Magnate | Señor de la Fortuna | 200 comercios | Casino de la fortuna: ruleta con 6 resultados ponderados |
| Monje | Gran Maestro | Iluminado | 150 muertes a puño | Nirvana: 10 s intocable por criaturas |
| Elementalista | Archimago Elemental | Señor de los Elementos | 300 usos de habilidades | Convergencia elemental: estallido de todos tus elementos |

### Artes de sangre (Nigromante)

Las artes de sangre se integran en la evolución del Nigromante (Nigromante → Noble de Sangre → Progenitor Carmesí):

- **Magia de sangre (Maestro):** si te falta energía para una habilidad del Nigromante, pagas la diferencia con vida (1 de vida por cada 10 de energía; no permite bajar de 2 de vida).
- **Sangrado (Maestro):** tus ataques aplican Descomposición I durante 3 s.
- **Inmortalidad carmesí (Definitivo):** una vez cada 5 minutos, un golpe mortal te deja con la mitad de la vida y Resistencia IV breve.

**Recetas de evolución:** Emblema de maestría `GDG / DED / GDG` (oro, diamante, bloque de esmeralda) · Núcleo definitivo `EBE / DND / EBE` (fragmento de eco, aliento de dragón, bloque de diamante, estrella del Nether).

## Cambios de equilibrio y diseño (tras la primera prueba)

**Idioma.** Además de `es_es` se incluyen `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy` y `es_ve`. Minecraft busca el archivo exacto del idioma elegido y, si no lo encuentra, muestra el inglés.

**Atributos más lentos.**
- **1 punto de atributo cada 3 niveles de alma** (`soulLevelsPerPoint`).
- Los niveles son más baratos: `1 + nivel/6` niveles de XP, con un máximo de 10.
- En la Hoguera, al preparar un punto, la pantalla calcula los niveles de alma necesarios y su coste.

**Afinidad de rol.** Cada rol tiene 2 atributos afines, marcados con ★ en la pantalla. Cada punto en ellos rinde **+50 %** si es tu rol principal y **+25 %** si es el secundario.

| Rol | Afines | Rol | Afines |
|---|---|---|---|
| Bersérker | Fuerza, Vigor | Paladín | Fe, Aguante |
| Mago | Mente, Inteligencia | Nigromante | Inteligencia, Mente |
| Asesino | Destreza, Suerte | Druida | Fe, Mente |
| Herbolista | Fe, Vigor | Mercader | Suerte, Mente |
| Ingeniero | Aguante, Inteligencia | Monje | Destreza, Vigor |
| Lancero | Fuerza, Aguante | Elementalista | Inteligencia, Fe |
| Espadachín | Destreza, Fuerza | Cambiaformas | Destreza, Mente |

**Habilidades por uso = pasivas defensivas.** Cada habilidad da varias bonificaciones que crecen con su nivel (valores al nivel 50):

| Habilidad | Pasivas |
|---|---|
| Minería | +100 % de velocidad de picado, **+5 de armadura** |
| Excavación | +100 % con pala, **+50 % de resistencia al empuje** |
| Tala | +100 % con hacha, **+10 de vida máxima** |
| Agricultura | 50 % de cosecha doble, +2 de suerte |
| Combate | +50 % de daño cuerpo a cuerpo |
| Tiro con arco | +75 % de daño de flechas |
| Defensa | **+3 de armadura, +5 de dureza, +50 % de resistencia a explosiones** |
| Agilidad | +10 % de velocidad, +3 bloques de caída segura |
| Pesca | +1 de suerte, 20 % de captura doble |
| Encantamiento | 50 % de reembolso de nivel (100 % con el hito 50), +20 de maná máximo |

La pestaña *Habilidades* muestra las 10 como tarjetas animadas (5 filas × 2): barra de XP que se llena, brillo dorado si subieron de nivel desde la última vez, resumen de pasivas en una línea y ficha completa (pasivas e hitos) al pasar el ratón.

**Sendas excluyentes.** Cada rol tiene **2 sendas de 3 nodos** (niveles 10, 14 y 20). Al aprender un nodo de una senda, la otra queda bloqueada; para cambiar hay que reiniciar el árbol. Cada nodo aplica un efecto: robo de vida, crítico, ejecución, espinas, reducción de daño, esquiva, regeneración de vida o de maná, carga de recurso, efectos al golpear (veneno, lentitud, fuego, descomposición), poder de esbirros, antiempuje, velocidad, vida, velocidad de ataque o suerte.

| Rol | Senda A | Senda B |
|---|---|---|
| Bersérker | Carnicero (robo de vida, crítico, ejecución) | Coloso (espinas, antiempuje, reducción) |
| Mago | Piromante (crítico, quemar, ejecución) | Erudito (maná, recurso, reducción) |
| Asesino | Verdugo (crítico, ejecución, veneno) | Fantasma (esquiva, velocidad, lentitud) |
| Herbolista | Sanador (regeneración, vida, reducción) | Envenenador (veneno, lentitud, ejecución) |
| Ingeniero | Artillero (crítico, ejecución, maná) | Acorazado (reducción, antiempuje, vida) |
| Lancero | Dragón (crítico, velocidad, lentitud) | Falange (espinas, antiempuje, reducción) |
| Espadachín | Duelista (esquiva, crítico, vel. de ataque) | Kensei (robo de vida, ejecución, lentitud) |
| Paladín | Templario (ejecución, robo de vida, quemar) | Guardián (reducción, espinas, regeneración) |
| Nigromante | Señor de esbirros (esbirros, recurso, maná) | Hematomante (robo de vida, descomposición, vida) |
| Druida | Guardián salvaje (vida, reducción, regeneración) | Cazador lunar (crítico, velocidad, veneno) |
| Mercader | Tesorero (recurso, maná, suerte) | Mercenario (crítico, robo de vida, ejecución) |
| Monje | Puño de hierro (crítico, vel. de ataque, lentitud) | Loto (esquiva, regeneración, maná) |
| Elementalista | Catalizador (crítico, ejecución, quemar) | Conductor (maná, recurso, reducción) |
| Cambiaformas | Depredador (crítico, robo de vida, velocidad) | Mimo (esquiva, recurso, maná) |

**HUD nuevo, junto a la hotbar** (tecla `O` para ocultarlo):
- **A la derecha**, las habilidades `V B · N M · Z R · H`. Cada una muestra su **propio icono**, la tecla encima, el coste de maná, el enfriamiento (barrido y segundos) y un candado si no está aprendida. Se tiñe de azul si falta maná y lleva un marco dorado parpadeante si tu recurso la va a potenciar.
- **A la izquierda**, la barra de Maná con su número y las barras de los recursos de rol con su nombre.
- En pantallas estrechas, la barra de habilidades sube por encima de la hotbar.

## Maná y recursos de rol

**Maná:** es el recurso común que gastan todas las habilidades (antes "energía"). Lo aumentan Mente, Afinidad arcana, Batería y los entrenamientos de maná.

**Recurso de rol:** cada rol tiene uno propio (0-100). Se carga con **toda la XP de ese rol** y, en algunos roles, por otras vías:

| Rol | Recurso | Carga extra | Se disipa |
|---|---|---|---|
| Bersérker | Furia | Recibir golpes (×3) | Sí |
| Mago | Concentración | +2/s mientras no te hieren | No |
| Asesino | Combo | — | Sí |
| Herbolista | Savia | — | No |
| Ingeniero | Carga | — | No |
| Lancero | Impulso | +3/s esprintando | Sí |
| Espadachín | Ritmo | — | Sí |
| Paladín | Devoción | Recibir golpes | No |
| Nigromante | Sangre | +5 por cada muerte cercana | No |
| Druida | Verdor | +1/s en bosques | No |
| Mercader | Oro | — | No |
| Monje | Chi | +4/s meditando (agachado e inmóvil) | Sí |
| Elementalista | Resonancia | — | No |
| Cambiaformas | Esencia | +2/s transformado | No |

**Potenciar:** al usar una habilidad del rol con el recurso a **50 o más**, se gastan 50 y la habilidad sale **potenciada (+1 rango)**. Funciona también con la característica y la definitiva. En el HUD, la barra se recuadra en blanco cuando está lista. Los recursos "de combate" se disipan 4 por segundo tras 5 s sin cargarlos.

## Árbol de 9 nodos por rol

A los 4 nodos propios de cada rol se suman 5:

| Nodo | Nivel | Efecto por rango |
|---|---|---|
| Entrenamiento I y II | 5 y 15 | Bonificación de estadística temática (vida, daño, maná, velocidad…) |
| Presteza | 6 | −10 % de enfriamiento en las activas del rol |
| Eficiencia | 10 | −10 % de maná en las activas del rol |
| Maestría | 18 | +1 rango efectivo en las activas del rol |

**Puntos de rol:** `1 + nivel × 2 / 3`, es decir, 21 al nivel 30 para 27 rangos posibles, así que hay que elegir. El rango efectivo de una activa (rango + evolución + Maestría + potenciar) se limita a 6.

**En el grafo:**
- Entrenamientos debajo de las pasivas.
- Presteza y Eficiencia debajo de las activas.
- Maestría junto a la activa B.

**HUD opcional:** la tecla `O` lo muestra u oculta, y la preferencia se guarda en `config/aptitudes-client.json`.

## Interfaz

- **Estilo:** todas las pantallas comparten un estilo oscuro con cabeceras doradas (`UiStyle`). Se pasa de una a otra con pestañas o con `Tab` / `Mayús+Tab`.
- **Atributos (`K`):** los puntos se **preparan** (‹ ›, o ←→ con el teclado; Mayús = ×5) y la columna *Estadísticas base* muestra `actual → nuevo` antes de confirmar. Los valores se miden en el servidor (equipo, efectos y otros mods incluidos, también el daño de ataque) y la vista previa usa la fórmula real de Minecraft. Los puntos se aplican con **Confirmar** y se descartan con **Reiniciar**.
- **Roles (`J`):**
  - A la izquierda, pestañas de **categoría**, los roles de esa categoría y la ficha del rol.
  - A la derecha, el **árbol como grafo**: el núcleo en el centro, las pasivas a la izquierda, las activas a la derecha, y la característica y la definitiva arriba. La forma del marco indica el tipo de nodo.
  - Clic en un nodo → descripción, rango, coste y botón **Mejorar**.
  - El Elementalista y el Cambiaformas tienen además una vista de elementos o de formas.
- **Únicas (`U`):** 16 tarjetas con el dueño de cada una (o *Libre*), tu progreso en cada gesta y sus efectos.
- **HUD:** energía, ranuras `V B N M`, después `Z` (característica) y `R` (definitiva), y `H` (única). Las ranuras especiales solo aparecen cuando se tienen.

## Categorías de roles

| Categoría | Roles |
|---|---|
| Marcial | Bersérker, Lancero, Espadachín, Monje |
| Arcana | Mago, Nigromante, Elementalista |
| Sombra | Asesino, Cambiaformas |
| Guardianes | Herbolista, Paladín, Druida |
| Oficios | Ingeniero, Mercader |

**Sinergia:** si el principal y el secundario son de la **misma** categoría, obtienes *Especialización* (+15 % de XP de rol). Si son de categorías **distintas**, obtienes *Versatilidad* (+15 de energía máxima).

## Habilidades características (Maestro, tecla `Z`)

| Rol | Característica |
|---|---|
| Paladín | **Bastión sagrado**: 10 s sin recibir ningún daño, +50 de energía, vida extra III, Absorción III, Resistencia II y curación total |
| Bersérker | **Grito del Juicio**: los mobs a 8 bloques pierden la mitad de su vida actual; tú, Fuerza III y Velocidad II |
| Nigromante | **Ejército de sangre**: 4 esbirros con Fuerza II y Resistencia II. Pasiva de Maestro: esbirros más resistentes (+50 % de vida, Resistencia) y su daño te cura un 25 % |
| Druida | **Renacer del bosque** (pasiva): al morir revives como con un tótem, una vez cada 5 minutos |
| Mercader | **Señor del mercado** (pasiva): Héroe de la aldea V permanente y Suerte |
| Monje | **Bendición del maestro**: cura totalmente a los aliados a 10 bloques y les da Fuerza, Velocidad, Resistencia y Regeneración II |
| Elementalista | **Bastión elemental**: muro temporal de tu elemento principal y su dominio durante 10 s |
| Ingeniero | **Baliza táctica**: el primer uso fija un punto de TP; el segundo te lleva allí con blindaje (Absorción IV + Resistencia II) |
| Mago | **Lluvia de meteoros**: 8 impactos (no rompen bloques) |
| Asesino | **Marca de muerte**: el objetivo recibe el triple de tu daño durante 10 s |
| Herbolista | **Elixir de vida**: purifica a los aliados y les da Regeneración III y vida extra |
| Lancero | **Carga imparable**: 3 s de embestidas siendo intocable |
| Espadachín | **Corte dimensional**: tajo de 14 bloques que hace 20 de daño a todos |
| Cambiaformas | **Caos de formas** (troll): los jugadores cercanos se convierten en gallinas durante 10 s |

## Cambiaformas

- **Formas:** Creeper, Enderman, Araña, Blaze, Murciélago, Slime, Gallina, Gólem de hierro y Bruja se desbloquean **derrotándolos** (entre 3 y 10 veces). El Aldeano se desbloquea con el nivel 5 de Cambiaformas y el Warden solo con la definitiva.
- **Usar las formas:** se elige la forma en la pantalla de roles (vista *Formas*). **Transformarse** la activa o desactiva, y gasta energía mientras dura. **Poder de la forma** usa la habilidad del mob.
- **Visibilidad:** los demás jugadores **te ven como el mob**, sin tu nombre encima. La invisibilidad también oculta el disfraz.
- **Bromista:** con rango 1 los monstruos te ignoran si pareces uno de ellos; con rango 2 emites los sonidos del mob.
- **Trolleo sin límites:** la explosión del Creeper rompe bloques (`morphExplosionsBreakBlocks`, activado por defecto) y la Forma primordial (Warden) aplica Oscuridad a los jugadores cercanos.
- **Limitación:** la caja de colisión sigue siendo la del jugador (un murciélago recibe golpes como un jugador). Solo ven la transformación los clientes que tienen el mod.

## Habilidades únicas

**Reglas:**
- Cada habilidad existe **una sola vez en el servidor** y cada jugador puede tener **una**.
- La obtiene el **primero** en completar su gesta, y se anuncia a todo el servidor.
- Pensado para un servidor entre amigos: **por defecto nunca se liberan** (`uniqueReleaseAfterDays = 0`). Si se pone un número de días, una habilidad cuyo dueño no se conecte en ese tiempo vuelve a quedar libre.
- El registro de dueños se guarda en el mundo (`data/aptitudes_unique.dat`).

| Habilidad | Gesta | Pasiva / Activa |
|---|---|---|
| Rueda de la Adaptación | Recibir 12 tipos de daño distintos | Resistencia creciente al tipo de daño repetido / inmunidad al último tipo |
| Corona de la Codicia | Llevar 32 bloques de esmeralda | Esmeraldas al matar / robar efectos beneficiosos |
| Archivo del Vacío | 6 roles a nivel 10 | Memoriza la última habilidad usada cerca / la lanza |
| Plaga Fúngica | 50 muertes sobre micelio | Golpes infecciosos / nube de esporas |
| Parásito | 20 muertes con ≤3 corazones | Robo de vida / drenaje invisible |
| Fuego del Averno | 600 s en lava | Inmune al fuego / nova infernal |
| Rey Elegido | Derrotar al Wither y al Dragón | +4 corazones y aura / Decreto real |
| Señor del Espacio-Tiempo | 200 perlas de Ender | Historial / retroceder 5 s |
| Dominio de la Negación | 50 cubos de leche | Inmune a efectos negativos / zona que impide a otros usar habilidades |
| Genetista | 200 animales criados | Crecimiento ×10 / mutar animales |
| Espejo Perfecto | Desbloquear las 10 formas | Copiar a cualquier mob durante 30 s |
| Explotador | Minería nivel 40 | Mineral doble / botín doble |
| Pendragón | Espadachín o Paladín Definitivo | +4 de armadura y +10 % de daño con espada / Excalibur |
| Señor de los Cielos | 100 km en élitros | Sin daño de caída / 12 s de vuelo |
| Quietud Eterna | 30 min agachado | Resistencia quieto / detener el tiempo 5 s |
| Heraldo de la Muerte | Morir 50 veces | Los no-muertos no atacan / sentencia (50 % de la vida) |

Comandos: `/unique` (lista) · `/unique admin give <jugador> <habilidad>` · `/unique admin revoke <habilidad>`.

## v14: grimorio, elementos y fusiones, armas de evolución y efectos de impacto

- **Grimorio de Aptitudes** (libro + pepita de oro; se regala al entrar): guía con 18 capítulos.
- **Elementalista**: 14 elementos (nuevos: Luz, Vacío, Veneno, Cristal, Sonido) y 10 **fusiones**
  (Tormenta ígnea, Electrocución, Ventisca, Erupción, Plaga, Eclipse, Singularidad cristalina,
  Resonancia, Prisma, Choque térmico) que se añaden al Estallido si dominas los dos elementos.
- **Armas de evolución**: al evolucionar a Maestro y a Definitivo recibes un arma nueva del rol
  (hoja de diamante/netherita o foco mágico). Aparecen en la fila superior del árbol; si la pierdes,
  botón **Reclamar**.
- **Efectos**: estallidos del color del rol al impactar con habilidades, disparos y armas de rol.

## v13: élites, campeones y accesorios

### Élites y campeones
- **Élite** (5 % de los monstruos, `eliteChance` en la config): 1-2 modificadores, ×2 de vida,
  nombre visible. Suelta Fragmentos de alma, **Esencia de élite** (50 %) y más XP.
- **Campeón** (10 % de las élites, y siempre uno en la azotea de cada torre): 3 modificadores,
  ×4 de vida y **barra de jefe**. Suelta un **Corazón de campeón**, esencias y una pieza de armadura
  de rol; se anuncia en el chat quién lo derrota.
- Modificadores: Veloz, Blindado, Vampírico, Brutal, Regenerante, Explosivo (sin romper bloques),
  Gélido, Escurridizo (se teletransporta al recibir golpes) y Gigante.

### Accesorios (tecla **Y**)
2 ranuras de anillo y 1 de amuleto; se conservan al morir.

| Anillo | Efecto | Amuleto | Efecto |
|---|---|---|---|
| Fuerza | +1,5 de daño | Guardián | +3 de armadura |
| Vitalidad | +4 de vida | Fénix | te salva de la muerte (cada 10 min) |
| Celeridad | +8 % velocidad | Arcano | −10 % enfriamientos |
| Maná | +30 de maná | Arboleda | regeneración fuera de combate |
| Vampiro | robo de vida 5 % | Trofeo del cazador | +25 % de daño a élites |
| Fortuna | +2 de suerte | | |

Recetas: anillo = 4 pepitas de oro + gema + Esencia de élite; amuleto = cuerda + Esencia + gema
(el del Fénix usa un Corazón de campeón). También aparecen en los cofres de las torres.

## v12: legendarias en el árbol, estructuras y aldeano de gremio

### Habilidades legendarias (antes "únicas")
Cada rol tiene un **nodo legendario** al final de su árbol (fila 6, centro; nivel 25, exige la
mecánica). Se aprende con 1 punto y se activa con **H**. Sin límite por servidor ni gestas.
Si tienes las dos, cuenta la del rol principal.

| Rol | Legendaria | Rol | Legendaria |
|---|---|---|---|
| Bersérker | Rueda de la Adaptación | Mercader | Corona de la Codicia |
| Mago | Archivo del Vacío | Herbolista | Plaga Fúngica |
| Asesino | Parásito | Brujo | Fuego del Averno |
| Paladín | Rey Elegido | Ingeniero | Señor del Espacio-Tiempo |
| Bardo | Dominio de la Negación | Domador | Genetista |
| Cambiaformas | Espejo Perfecto | Cazador | Explotador |
| Espadachín | Pendragón | Elementalista | Señor de los Cielos |
| Monje | Quietud Eterna | Nigromante | Heraldo de la Muerte |
| Lancero | **Atravesacielos** (nueva) | Druida | **Árbol del Mundo** (nueva) |
| Mago pistolero | **La Última Bala** (nueva) | Invocador de armas | **Puerta del Arsenal** (nueva) |

`/unique give <jugador> <legendaria>` concede el nodo; la pantalla de Únicas desaparece.

### Estructuras (Overworld)
| Estructura | Rareza (1 de cada N chunks) | Contenido |
|---|---|---|
| Torre en ruinas | 240 | 4 pisos, cofre por piso, generadores (zombi/esqueleto/araña), cofre de azotea con armaduras de rol y herramientas de alma |
| Santuario de Hoguera | 150 | Hoguera encendida, cofre con fragmentos, frascos y a veces una Ofrenda |
| Campamento | 200 | Cazadores (pillagers), brujos (brujas) o bandidos (vindicadores); botín temático |

Las tablas de botín están en `data/aptitudes/loot_table/chests/` y la rareza en
`data/aptitudes/worldgen/placed_feature/` (editables con un datapack).

### Aldeano de gremio
Coloca una **Mesa del gremio** (oro + esmeralda + tablones) junto a un aldeano sin profesión.
Vende Fragmentos de alma, frascos de Estus y Éter, materiales de rol, **Vales de armería**
(dan una pieza del set de tu rol principal, primero las que te faltan), Ofrendas de renacimiento,
Emblemas de maestría y Núcleos definitivos.

## v11: 6 roles nuevos, árbol rediseñado y barras nuevas

### Roles nuevos (20 en total, 4 por categoría)
| Rol | Categoría | Ítem | Mecánica | Activas |
|---|---|---|---|---|
| Mago pistolero | Arcana | Revólver arcano | Tambor arcano (6.ª bala explosiva) | Disparo arcano, Vaciar el tambor, Rebote, Ojo de la muerte |
| Invocador de armas | Oficio | Sello de armas | Arsenal flotante (espadas que orbitan) | Andanada de espadas, Lluvia de espadas, Espadón espectral, Prisión de espadas |
| Cazador | Sombras | Cuchillo de caza | La cacería (presa acumulada) | Marca del cazador, Trampa, Lluvia de flechas, Ataque del halcón |
| Bardo | Guardianes | Laúd | Crescendo (4 notas → final) | Canción del valor, Disonancia, Balada protectora, Réquiem |
| Brujo | Sombras | Talismán maldito | Corrupción (vida → daño mágico) | Maldición de agonía, Rayo del abismo, Invocar diablillos, Condena |
| Domador | Oficio | Silbato del domador | Compañero (bonus si tu mascota ataca) | ¡A por él!, Vínculo vital, Estampida, Rugido alfa |

Cada uno tiene 23 nodos, 3 Sendas, característica, definitiva, recurso propio, set de armadura con
bonificación y prueba de evolución. XP: Cazador con flechas y cuchillo; Domador con el daño de sus
mascotas; Bardo con el Laúd; el resto con su ítem y sus habilidades.

### Árbol
Tres zonas (Pasivas · Sendas · Activas), conectores en ángulo recto, núcleo/característica/definitiva
arriba en el centro y el nombre de cada senda sobre su columna. Las líneas se iluminan en dorado
al aprender y en gris claro cuando el nodo ya está disponible.

### Barras
Icono pixel art + barra redondeada con degradado y brillo, valor dentro de la barra; la vida late en
rojo con menos del 25 %; los recursos de rol parpadean cuando pueden potenciar una habilidad.

## Cambios v10 (tras la prueba de la v8)

- **Textos**: las etiquetas largas se recortan con "…" en lugar de pisar los valores; nivel y puntos
  del rol en dos líneas; columna de estadísticas más ancha.
- **Árbol**: se arrastra con el ratón (clic en vacío o botón derecho), la rueda acerca/aleja y `C` lo
  centra. La ventana crece con la pantalla (hasta 760×480).
- **Cambio de rol**: ya no hay espera de 30 min. Ocupar una ranura vacía o intercambiar principal y
  secundario es gratis; **sustituir** un rol exige quemar una **Ofrenda de renacimiento** en una Hoguera
  (clic derecho con ella en la mano). Receta: 3 Fragmentos de alma + lingote de oro + vela.
  `roleChangeRequiresRitual` en `config/aptitudes.json` lo desactiva.
- **Mago arcano**: sin fuego ni hielo. Proyectil arcano, Distorsión temporal, Lluvia rúnica
  (característica) y Senda Arcanista (crítico, ejecución, marchitar).
- **HUD**: barras estilo Souls arriba a la izquierda (vida con trozo retrasado y absorción, maná,
  aguante, recursos), indicador de la mecánica del rol sobre la barra de habilidades, ranuras con
  esquinas doradas y destello al quedar listas. **Mayús + O** abre la configuración: arrastrar la
  barra, escala 50–200 %, y activar/desactivar cada elemento.

## Árbol ramificado y mecánicas de rol (v9, fase 1)

### Árbol con requisitos (23 nodos por rol)
```
fila 0:          [Pasiva A]       [MECÁNICA]        [Activa A]
fila 1:  [Entr.1]      [Pasiva B]           [Presteza]     [Activa B]
fila 2:      [Reactiva 1]  [A1]  [B1]  [C1]   [Eficiencia]
fila 3:  [Entr.2]          [A2]  [B2]  [C2]                [Activa C]
fila 4:      [Reactiva 2]  [A3]  [B3]  [C3]   [Maestría]
fila 5:                                                    [Activa D]
```
- Cada nodo exige al menos 1 rango de su padre (línea en el árbol).
- **3 Sendas** por rol que nacen de la mecánica; solo puedes seguir **2**.
- Puntos de árbol: 1 + 3 cada 3 niveles (31 al nivel 30) para 69 rangos posibles.

### 4 activas por rol, 2 equipadas
Cada rol tiene 4 activas (A, B, C, D). En la ficha de una activa aprendida, los botones
**Ran. 1 / Ran. 2** la equipan en las teclas V/B (principal) o N/M (secundario).
En el árbol, el número en la esquina indica la ranura.

### Mecánica propia y pasivas reactivas
| Rol | Mecánica | Reactivas |
|---|---|---|
| Bersérker | Frenesí (golpes seguidos acumulan daño) | Inquebrantable, Carnicería |
| Mago | Cargas arcanas (hechizo gratis cada N) | Escudo de maná, Eco arcano |
| Asesino | Ritmo letal (3.er golpe = remate) | Huida en humo, Desvanecerse |
| Herbolista | Floración (quieto = regeneración en área) | Antídoto, Piel espinosa |
| Ingeniero | Sobrecarga (calor → modo potenciado) | Escudo automático, Reparación de campo |
| Lancero | Ímpetu (esprintar carga el golpe) | Afianzarse, Caída del dragontino |
| Espadachín | Kata (3.er golpe barre) | Reflejos, Ráfaga |
| Paladín | Luz sagrada (5 cargas → explosión) | Intercesión, Luz purificadora |
| Nigromante | Cosecha de almas | Escudo de hueso, Estallido de cadáver |
| Druida | Ciclo natural (día cura / noche daño) | Piel de corteza, Zarzas |
| Mercader | Interés compuesto (esmeraldas) | Seguro, Recompensa |
| Monje | Posturas Tigre / Grulla | Desviar, Paz interior |
| Elementalista | Reacción (cambiar de elemento potencia) | Guarda elemental, Muerte resonante |
| Cambiaformas | Instinto (transformado acumula daño) | Escurridizo, Devorar |

Activas nuevas: Salto demoledor, Torbellino, Nova de escarcha, Misiles arcanos, Abanico de cuchillas,
Shuriken, Lluvia sanadora, Nube de esporas, Garfio, Granada de pulso, Salto del dragón, Estocada
perforante, Iaido, Mil cortes, Consagración, Martillo de la justicia, Lanza de hueso, Sifón de almas,
Manada, Tormenta de espinas, Lanzamiento de moneda, Guardia dorada, Patada voladora, Cien puños,
Ráfaga elemental, Aura elemental, Abalanzarse, Aullido de la bestia.

### Hoja de ruta
- **Fase 2**: nuevos elementos (Luz, Vacío, Veneno, Cristal, Sonido) y fusiones entre elementos.
- **Fase 3**: armas mejoradas, accesorios, consumibles, materiales y enemigos élite/jefes.
- **Fase 4**: sinergias entre rol principal y secundario.

## Equipo, objetos y efectos visuales (v8)

### Sets de armadura por rol
14 sets × 4 piezas. Cualquiera puede llevarlos por su defensa; la **bonificación de set completo**
solo se activa si ese rol es tu principal o secundario.

| Peso | Roles | Defensa (casco/peto/grebas/botas) | Dureza |
|---|---|---|---|
| Pesado | Bersérker, Lancero, Paladín | 3/8/6/3 (≈ diamante) | 2 |
| Medio | Espadachín, Ingeniero, Druida, Nigromante | 2/6/5/2 | 1 |
| Ligero | resto | 2/5/4/2 | 0 |

Receta: patrón de armadura vanilla con el **material del rol** (hierro, amatista, membrana, panal, cobre,
prismarina, cuarzo, oro, hueso, musgo, esmeralda, lana, vara de breeze, piel de conejo) y un
**Fragmento de alma** en el hueco central.

### Armas de rol
El ítem característico del rol gana +1 % de daño por nivel del rol, +10 % en Maestro y +20 % en Definitivo.

### Objetos de alma
| Objeto | Efecto | Obtención |
|---|---|---|
| Fragmento de alma | Material | 5 % de monstruos (+1 % por suerte), 4 de jefes, 1 eco → 2 |
| Frasco de Estus | 5 cargas: cura 8 + Regeneración | botella + 2 fragmentos + polvo de blaze + oro |
| Frasco de Éter | 5 cargas: 40 % de maná | botella + 2 fragmentos + lapislázuli + amatista |
| Pico/Pala de alma | Agachado: 3×3 | herramienta de diamante + 3 fragmentos |
| Hacha de alma | Agachado: tala el árbol (64) | idem |
| Azada de alma | Cosecha y replanta 3×3 | idem |

Los frascos se rellenan al descansar en una Hoguera.

### Hitos de habilidades (niveles 10/20/30/40/50)
Cada habilidad desbloquea 5 ventajas (visión nocturna minando, replantado, tala de árboles, críticos que
curan, flechas perforantes, Segunda oportunidad…). Se ven en la pantalla de habilidades (rombos dorados
y tooltip). Al subir de nivel: título en pantalla, círculo rúnico de partículas y aviso especial en los hitos.

### Efectos visuales
- Círculos rúnicos, espirales y ondas expansivas de color al lanzar habilidades.
- Destello de color en los bordes de la pantalla y temblor de cámara en características, definitivas y únicas.
- Números de daño flotantes (blanco, ámbar ≥ 8, rojo ≥ 15).
- Tarjetas de XP de habilidad en el lado derecho con barra de progreso.
- Se desactivan en `config/aptitudes-client.json` (`screenEffects`, `damageNumbers`) o en el servidor
  (`damageNumbers`, `levelUpTitles` en `config/aptitudes.json`).

## Comandos

| Comando | Permiso | Descripción |
|---|---|---|
| `/aptitudes` (alias `/skills`) | Todos | Resumen propio |
| `/aptitudes allocate <atributo> [cantidad]` | Todos | Asignar puntos |
| `/aptitudes respec` | Todos si `allowSelfRespec`; si no, solo OP | Devolver todos los puntos |
| `/aptitudes admin show <jugador>` | OP (2) | Ver el progreso de otro jugador |
| `/aptitudes admin setlevel <jugador> <habilidad> <nivel>` | OP | Fijar el nivel |
| `/aptitudes admin addxp <jugador> <habilidad> <cantidad>` | OP | Dar XP |
| `/aptitudes admin addpoints <jugador> <cantidad>` | OP | Puntos extra (admite valores negativos) |
| `/aptitudes admin respec <jugador>` / `reset <jugador>` | OP | Devolver los puntos / borrar todo el progreso |
| `/aptitudes admin reload` | OP | Recargar `config/aptitudes.json` y reaplicarla a todos los jugadores |
| `/roles` | Todos | Resumen de roles |
| `/roles primary <rol>` / `secondary <rol>` | Todos | Elegir roles (con el enfriamiento de cambio) |
| `/roles learn <nodo>` | Todos | Subir un rango |
| `/roles use <1-4>` | Todos | Usar la habilidad de una ranura (útil para macros) |
| `/roles respec <rol>` | Igual que `/aptitudes respec` | Devolver los puntos del árbol de un rol |
| `/roles admin setlevel\|addxp <jugador> <rol> <n>` | OP | Nivel / XP de rol |
| `/roles evolve <rol>` | Todos | Evolucionar a Maestro o Definitivo |
| `/roles admin settier <jugador> <rol> <0-2>` | OP | Fijar el nivel de evolución (útil para probar) |
| `/roles admin reset\|clearcooldown <jugador>` | OP | Borrar todos los roles / quitar el enfriamiento de cambio |

Las claves de habilidad y atributo van en inglés y en minúsculas (`mining`, `strength`…); el comando las autocompleta.

## Configuración (`config/aptitudes.json`)

Se genera en el primer arranque. Los campos que falten toman su valor por defecto y el archivo se reescribe para añadir los campos nuevos. Si el JSON está mal formado, se registra el error, se usan los valores por defecto y **el archivo no se sobrescribe**.

Grupos de campos: progresión (`maxSkillLevel`, `xpBase`, `xpExponent`, `pointsPerSkillLevel`, `startingPoints`, `maxPointsPerAttribute`, `allowSelfRespec`), comportamiento (`awardXpInCreative`, `awardXpForPlayerKills`, `showXpInActionBar`, `levelUpMessages`), fuentes de XP (`miningXp*`, `woodcuttingXpLog`, …), pasivas (`breakSpeedPerLevel`, `combatDamagePerLevel`, …) y valor por punto (`strengthAttackDamagePerPoint`, …).

## Arquitectura

```
src/main/java/dev/aptitudes/            (común: cliente + servidor)
├── Aptitudes.java                      ModInitializer: registra todo
├── config/AptitudesConfig.java         POJO + carga/guardado con Gson
├── data/
│   ├── Skill.java, PlayerAttribute.java   enums con su atributo vanilla y su escalado
│   ├── PlayerProgress.java             estado del jugador + Codec (persistencia)
│   ├── ProgressRules.java              reglas que se sincronizan al cliente
│   └── ModAttachments.java             Fabric Data Attachment API (NBT del jugador)
├── logic/
│   ├── ProgressManager.java            ÚNICA vía de mutación: XP, niveles, puntos, sincronización
│   ├── AttributeApplier.java           progreso → EntityAttributeModifier
│   ├── SkillEffects.java               velocidad de rotura (cálculo idéntico en ambos lados)
│   └── XpEvents.java                   eventos de Fabric API → XP
├── network/                            payloads S2C (sync) y C2S (allocate)
├── sync/ClientCache.java               copia de los datos en el cliente (sin clases de cliente)
├── role/
│   ├── Role.java, RoleNode.java, RoleTree.java   definición de roles y árboles
│   ├── Element.java                              elementos del Elementalista
│   ├── Evolution.java                            niveles de evolución, pruebas y títulos
│   ├── RoleData.java, RoleRules.java             estado persistente y reglas sincronizadas
│   ├── RoleManager.java                          ÚNICA vía de mutación de roles
│   └── RoleEvents.java                           XP de rol, torretas, tick
├── ability/
│   ├── AbilityManager.java             ranuras, energía, enfriamientos
│   ├── Abilities.java                  las 16 activas
│   ├── RolePassives.java               las 16 pasivas (atributos, tick, daño)
│   ├── Ultimates.java                  las 13 habilidades definitivas
│   ├── ElementEffects.java             proyectil, estallido y pasivas de los 9 elementos
│   ├── Minions.java, MinionBrain.java  creación y comportamiento de los esbirros
│   ├── AbilityState.java, Targeting.java   invocaciones, soborno, contraataque y selección de objetivos
├── item/                               ModItems + bastón/pistola, saquito, llave
├── mixin/PlayerEntityMixin.java        velocidad de rotura
├── mixin/LivingEntityMixin.java        multiplicador de daño saliente y robo de vida
├── mixin/MobEntityMixin.java           filtro de objetivos (esbirros, torretas, soborno)
├── mixin/MerchantEntityMixin.java      evento de comercio (Mercader)
└── command/AptitudesCommand.java, RoleCommand.java
src/client/java/dev/aptitudes/client/   (solo cliente)
├── AptitudesClient.java                teclas K/J/V/B/N/M, receptores de red
├── ClientRoleState.java                copia local de roles, energía y enfriamientos
├── hud/AbilityHud.java                 barra de energía y ranuras
└── screen/AptitudesScreen.java, RolesScreen.java
```

### Decisiones de diseño

1. **Servidor autoritativo.** El cliente solo envía *peticiones* (asignar N puntos). El servidor valida el índice, la cantidad (como máximo 100 por paquete), los puntos disponibles y el máximo por atributo, y después reenvía el estado completo. La GUI nunca modifica el estado local.
2. **Puntos derivados, no almacenados.** `disponibles = iniciales + Σniveles·puntosPorNivel + bonus − asignados`. Así, los comandos de admin o los cambios de configuración no pueden desincronizar un contador. Si el resultado sale negativo (por ejemplo, al bajar `pointsPerSkillLevel`), se devuelven todos los puntos asignados y se avisa al jugador.
3. **Modificadores de atributos vanilla** con un `Identifier` fijo por fuente. La operación es idempotente (quitar y volver a añadir), y el combate, la vida o la velocidad se integran con el resto del juego y con otros mods sin código adicional. Son **persistentes** para que, al reconectar, la vida máxima ya esté aplicada cuando vanilla restaura la vida actual.
4. **Persistencia con Data Attachments** (`copyOnDeath`): sin archivos propios ni gestión de guardado manual. Se serializa con **claves de texto**, de modo que añadir o reordenar habilidades no rompe las partidas existentes.
5. **Sincronizar las reglas, no solo los datos.** La velocidad de minado la predice el cliente y la valida el servidor; si difieren, el bloque "reaparece". Por eso el servidor envía `breakSpeedPerLevel` y el cliente nunca usa su configuración local.
6. **Eventos antes que mixins.** Solo hay 4 mixins, cada uno en un punto que Fabric API 1.21.1 no cubre: velocidad de rotura, modificación de daño, elección de objetivo de los mobs y comercio completado. Los dos primeros multiplican el valor original y se componen bien con otros mods.
7. **Source sets separados** (`splitEnvironmentSourceSets`): es imposible referenciar por error clases de cliente desde el código que se ejecuta en un servidor dedicado.
8. **Los roles reutilizan los mismos patrones** que las habilidades: un gestor único (`RoleManager`), puntos derivados de los rangos (`1 + nivel/3 − rangos`), sincronización autoritativa y modificadores de atributos con id fijo por nodo.
9. **Habilidades de área solo contra hostiles** (monstruos o mobs que te tienen como objetivo), para no herir a aldeanos, mascotas ni jugadores por accidente.
10. **Invocaciones seguras** (torretas y esbirros): mueren sin soltar botín (`ALLOW_DEATH`), se registran *antes* de aparecer y, si quedaron guardadas en disco tras un reinicio, se retiran al cargarse.
11. **Esbirros sin reescribir la IA vanilla.** Solo se les elige el objetivo cada 10 ticks y se filtra `setTarget`, en lugar de sustituir sus metas de IA. Es más compatible y menos frágil.
12. **El Sello del gremio usa `UseEntityCallback`**, porque el aldeano abre su pantalla de comercio antes de que el ítem reciba `useOnEntity`.

### Limitaciones conocidas

| Limitación | Impacto | Posible mejora |
|---|---|---|
| Los textos del servidor son claves traducibles | Un cliente sin el mod ve las claves en vez del texto | Usar Server Translations API |
| Cliente vanilla en un servidor con el mod | Sin GUI. El minado se ve más lento en el cliente, aunque el servidor lo acepta | — |
| Mobs con IA de "Brain" (piglins, hoglins, warden) | Se corrigen cada 5 ticks (v19): entre dos revisiones pueden iniciar un ataque; el warden, muy agresivo, puede llegar a golpear | Mixin sobre sus tareas de ataque si hiciera falta |
| El rango efectivo puede llegar a 5 con la evolución | Algunos valores (esquiva 35 %, Mente clara −50 %) pueden quedar fuertes | Ajustar tras probar; Mente clara está limitada a −60 % |
| Fuego del Averno y explosiones del Creeper modifican bloques | Cambios permanentes del mundo (según `uniqueGriefing`) | — |
| Las transformaciones no cambian la caja de colisión | Un "murciélago" sigue ocupando lo que un jugador | Cambiar las dimensiones del jugador (mixin de `getDimensions`) |
| Quietud Eterna usa `NoAI` | Se repara al cargar, pero otro mod que lea `NoAI` podría verse afectado | — |
| Las texturas son placeholder | Aspecto básico | Sustituir los PNG en `assets/aptitudes/textures/item/` |
| No se ha compilado en el entorno donde se generó | Puede requerir algún ajuste menor de mappings | Ejecutar `./gradlew build` |

**Resueltas en la v16:** la Defensa solo da XP por daño realmente recibido (no por golpes bloqueados con
escudo ni ignorados por invulnerabilidad); un zombi esbirro que empieza a convertirse en ahogado se
disuelve antes de volverse hostil; el Bombardeo orbital ya no daña al lanzador, a jugadores, aldeanos ni
esbirros.

**Resueltas en la v17 (anti-granjas de colocar y romper):** romper un bloque colocado por un jugador no
da XP de habilidad ni activa los extras de los hitos (p. ej., Veta rica ya no duplica minerales colocados);
la XP de Ingeniero y de Druida por colocar solo cuenta colocaciones confirmadas, y la del Ingeniero tiene
rendimiento decreciente. Sin mixins (`logic/PlacementTracker`). El registro vive en memoria (100 000
posiciones por mundo) y se pierde al reiniciar; un bloque movido con pistón deja de estar marcado.

**Resueltas en la v18 (persistencia):** los tiempos de espera de habilidades, definitivas, firmas,
legendaria e Inmortalidad carmesí se guardan con el jugador (tiempo de juego del mundo principal): ya no
se reinician al reiniciar el servidor ni al salir y volver a entrar. Los bloques del Muro elemental se
guardan por mundo con su hora de caducidad: se retiran aunque el servidor se cierre con el muro levantado,
y romperlos no suelta objetos ni da XP. Limitación: una explosión que destruya el muro sí puede soltar
algún bloque (comportamiento vanilla de las explosiones).

**Resueltas en la v19:** la pantalla de roles (mínimo 322 px de alto) se desplaza con la rueda fuera del
árbol o con RePág/AvPág cuando no cabe (p. ej., escala de interfaz 4 en 1080p) y muestra una barra; el zoom
y la posición del árbol se conservan. Los mobs con IA de tipo Brain respetan el Soborno, el camuflaje del
Cambiaformas y el Heraldo de la Muerte (`BrainTargetFilter`, sin mixins).

### Siguientes pasos sugeridos

- Pasar los árboles de roles a **datapacks** (JSON) para editarlos sin recompilar.
- Recursos distintos por rol (Furia para el Bersérker, Maná para el Mago) en lugar de una energía común.
- Más nodos por rol (por ejemplo, una habilidad "definitiva" al nivel 25).
- Tests: `PlayerProgress`/`ProgressRules` son puros y testeables con JUnit; para la integración, Fabric GameTest.
- HUD opcional con una barra de XP de la última habilidad entrenada.
