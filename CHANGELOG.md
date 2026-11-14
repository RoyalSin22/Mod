# Registro de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versiones con
[Versionado Semántico](https://semver.org/lang/es/). Las «vNN» son las etapas internas de desarrollo.

## [Sin publicar]


### Seguridad
- **v66 — Auditoría de red y configuración.** Se revisaron todos los payloads cliente → servidor (12 antes de retirar los pedidos, 11 después): todos validan su entrada en el
  servidor y los de forja, frascos y grimorio ya tenían sus propios controles. Cambios: límite de frecuencia por jugador y
  acción (`network/RequestLimiter`) para estadísticas, accesorios, contratos e invitaciones de grupo; `Accessories.open`
  exige jugador vivo y no espectador; la compra de niveles de alma acumula el coste en `long`; y 40 campos numéricos de
  la configuración reciben un máximo (antes solo se exigía no negativo y finito), sin cambiar ningún valor por defecto.
  `docs/CONFIGURATION.md` documenta los 42 campos que faltaban y `AGENTS.md` lista las 86 clases de prueba reales.

### Retirado
- **v67 — Vistas antiguas del árbol.** Se borran `TreeScreen`, `PathsScreen` y `SpriteButton` (sin acceso desde J desde
  el árbol unificado) y la opción de cliente `newTreeScreen`; un `aptitudes-client.json` que la tenga la ignora.
- **v66 — Coloso de Hierro.** Se retira el mob de prueba de modelos: entidad, huevo de aparición, renderizador, geometría,
  animaciones, texturas, partículas y textos. Los mundos que lo tuvieran lo descartan al cargar. El Coloso Sin Rostro no cambia.
- **v65 — Talentos por datapack.** Se retira el piloto de la v55: `role/datapack/*`, los payloads
  `TalentCatalogPayload` y `TalentPurchasePayload`, la pantalla «Talentos del pack», el ejemplo `docs/examples/talentos-piloto`,
  sus pruebas y las siete claves de texto. El campo `datapack_ranks` deja de guardarse: las partidas antiguas lo
  ignoran y los puntos gastados en esas ramas vuelven a estar disponibles.

### Añadido
- **v67 (F7) — 30 habilidades nuevas: Bardo, Brujo abisal y Domador.** Con esta tanda los 20 roles tienen sus 10
  habilidades nuevas (5 pasivas y 5 activas) en Talentos, Veterano y Ascenso; diseño aprobado en
  `docs/V67_F7_DISENO.md`.
  - **Bardo:** Armonía de grupo, Estribillo pegadizo, Nota cortante, Arrullo, Coro, Estruendo, Pulso del compás, Marcha
    de victoria, Contrapunto y Gran final.
  - **Brujo abisal:** Sed de maldiciones, Sacrificio de sangre, Mal de ojo, Cadenas de sombra, Ansia de alma, Nube de
    pestilencia, Desesperación, Forma demoníaca, Cosecha oscura y Fosa abisal.
  - **Domador:** Pelaje denso, Frenesí de caza, Convocar a la manada, Zarpazos coordinados, Cuidado veterano, Manto
    protector, Olfato de la manada, Furia animal, Vínculo de sangre y Llamada del rey.
  - **Código:** `BardKit`, `WarlockKit` y `TamerKit` con sus reglas puras (`*Rules`), enganchados solo en
    `ability/ExtraKits`; sin ganchos nuevos en el resto del juego. El Domador usa las mascotas que ya consulta el rol
    (`NewRoles.pets`).
  - **Textos:** 60 claves nuevas en los 5 idiomas; iconos provisionales con objetos vanilla y encargo de arte en
    `docs/PROMPT_ARTE_ICONOS_F7.md`. Sin opciones de configuración nuevas (usa `extraSkillDamageScale`).
  - **Pruebas:** `BardRulesTest`, `WarlockRulesTest`, `TamerRulesTest` y `RoleExtrasTest` ampliada a los veinte roles.
  - **Diferencias con el diseño aprobado:** ver §6 de `docs/V67_F7_DISENO.md` (por ejemplo, Sacrificio de sangre exige
    tener Corrupción).
- **v67 (F6) — 40 habilidades nuevas: Cambiaformas, Mago pistolero, Invocador de armas y Cazador.** Cada rol tiene 10
  (5 pasivas y 5 activas) en Talentos, Veterano y Ascenso, igual que en F2–F5; diseño aprobado en
  `docs/V67_F6_DISENO.md`.
  - **Cambiaformas:** Metabolismo animal, Sangre fría animal, Mordisco salvaje, Cambio relámpago, Piel de repuesto,
    Zarpazo múltiple, Presa fácil, Furia de la bestia, Tercer cambio y Estampida primigenia.
  - **Mago pistolero:** Manos firmes, Cabeza fría, Bala aturdidora, Granada de pólvora, Presión, Disparo a través,
    Sin remordimientos, Doble pistola, Pulso de tirador y Ejecución.
  - **Invocador de armas:** Espadas afiladas, Equilibrio, Espada boomerang, Muro de espadas, Hoja de repuesto, Danza de
    acero, Filo perfecto, Armería viva, Eco de acero y Juicio de acero.
  - **Cazador:** Mira certera, Presa herida, Red de caza, Flecha explosiva, Trampero experto, Disparo en abanico,
    Cazador paciente, Vista de halcón, Botín de caza y Cacería final.
  - **Código:** `ShapeshifterKit`, `GunslingerKit`, `WeaponSummonerKit` y `HunterKit` con sus reglas puras (`*Rules`),
    enganchados en `ability/ExtraKits`. Estos roles ya tenían su estado en `NewRoles` (espadas flotantes, marcas,
    tambor), así que se añadieron accesores (`bladeCount`, `isMarkedBy`, `markTarget`…) y ganchos de una línea en
    `NewRoles` (daño de espadas, tope y ritmo de espadas, Eco de acero, duración de trampas y bala explosiva).
  - **Textos:** 80 claves nuevas en los 5 idiomas; iconos provisionales con objetos vanilla y encargo de arte en
    `docs/PROMPT_ARTE_ICONOS_F6.md`. Sin opciones de configuración nuevas (usa `extraSkillDamageScale`).
  - **Pruebas:** `ShapeshifterRulesTest`, `GunslingerRulesTest`, `WeaponSummonerRulesTest`, `HunterRulesTest` y
    `RoleExtrasTest` ampliada a los diecisiete roles con datos.
  - **Diferencias con el diseño aprobado:** ver §7 de `docs/V67_F6_DISENO.md` (por ejemplo, Juicio de acero hace 6 de
    daño por espada y Botín de caza da experiencia).
- **v67 (F5) — 40 habilidades nuevas: Druida, Mercader táctico, Monje y Chamán.** Cada rol tiene 10 (5 pasivas y 5
  activas) en Talentos, Veterano y Ascenso, igual que en F2–F4; diseño aprobado en `docs/V67_F5_DISENO.md`.
  - **Druida:** Hierba alta, Zarpas desgarradoras, Látigo de lianas, Rebrote, Letargo, Rugido del oso, Roble firme,
    Forma de huargo, Flujo estacional y Despertar del bosque.
  - **Mercader táctico:** Monedero, Dividendos, Moneda rebotona, Subasta, Crédito, Caravana, Libro mayor, Inversión,
    Cacería de recompensas y Gran liquidación.
  - **Monje:** Manos vacías, Combinación, Patada giratoria, Onda de chi, Cuerpo sereno, Palma explosiva, Punto de
    presión, Postura del tigre, Eco de puño y Palma del vacío.
  - **Chamán:** Cambio de marea, Eco del elemento, Lanza elemental, Muro elemental, Reacción en cadena, Tótem elemental,
    Fusión estable, Forma elemental, Armonía elemental y Elementos desatados.
  - **Código:** `DruidKit`, `MerchantKit`, `MonkKit` y `ElementalistKit` con sus reglas puras (`*Rules`), enganchados solo
    en `ability/ExtraKits`. Sin ganchos nuevos en el resto del juego, salvo uno: Monedero y Crédito del Mercader se
    enganchan en `AbilitiesExtra.consume` (y `Abilities.consumeOne` lo usa), el único sitio donde las activas gastan
    esmeraldas. El Chamán aplica los efectos de sus 14 elementos con un conmutador propio (`applyElement`).
  - **Textos:** 80 claves nuevas en los 5 idiomas; iconos provisionales con objetos vanilla y encargo de arte en
    `docs/PROMPT_ARTE_ICONOS_F5.md`. Sin opciones de configuración nuevas (usa `extraSkillDamageScale`).
  - **Pruebas:** `DruidRulesTest`, `MerchantRulesTest`, `MonkRulesTest`, `ElementalistRulesTest` y `RoleExtrasTest`
    ampliada a los trece roles con datos.
  - **Diferencias con el diseño aprobado:** ver §7 de `docs/V67_F5_DISENO.md` (por ejemplo, Reacción en cadena y Fusión
    estable se ajustan porque las fusiones del Chamán son instantáneas y no cuestan maná).
- **v67 (F4) — 40 habilidades nuevas: Lancero, Espadachín, Paladín y Nigromante.** Cada rol tiene 10 (5 pasivas y 5
  activas) en Talentos, Veterano y Ascenso, igual que en F2 y F3; diseño aprobado en `docs/V67_F4_DISENO.md`.
  - **Lancero:** Mantener la distancia, Punta clavada, Jabalina, Muro de picas, Ensartar, Abanico de picas, Primer
    contacto, Lanza ancestral, Doble estocada y Ráfaga de jabalinas.
  - **Espadachín:** Duelo, Filo penetrante, Tajo ascendente, Molinete, Racha de victorias, Salto de espadachín, Voluntad
    de acero, Estado de flujo, Desenvainado y Corte final.
  - **Paladín:** Fe contra la oscuridad, Triunfo compartido, Rayo de luz, Golpe de escudo, Oración, Muro de luz, Vínculo
    de escudo, Estandarte sagrado, Ira justa y Columna de luz.
  - **Nigromante:** Orden de ataque, Banquete, Mano de huesos, Sacrificio, Aura putrefacta, Tierra maldita, Resurgir,
    Frenesí necrótico, Vínculo de huesos y Tormenta de almas.
  - **Código:** un kit por rol (`LancerKit`, `SwordsmanKit`, `PaladinKit`, `NecromancerKit`) con sus reglas puras
    (`*Rules`), enganchados solo en `ability/ExtraKits`. Tres ganchos nuevos: muertes causadas por esbirros
    (`RoleEvents`, `ExtraKits.onMinionKill`), daño recibido por esbirros (`RolePassives`, `ExtraKits.minionIncoming`) y
    daño posterior al golpe con el objetivo (`ExtraKits.afterOutgoing`); Voluntad de acero y Resurgir pueden anular un
    golpe mortal.
  - **Textos:** 80 claves nuevas en los 5 idiomas; iconos provisionales con objetos vanilla y encargo de arte en
    `docs/PROMPT_ARTE_ICONOS_F4.md`. Sin opciones de configuración nuevas (usa `extraSkillDamageScale`).
  - **Pruebas:** `LancerRulesTest`, `SwordsmanRulesTest`, `PaladinRulesTest`, `NecromancerRulesTest` y `RoleExtrasTest`
    ampliada a los nueve roles con datos.
  - **Diferencias con el diseño aprobado:** ver §7 de `docs/V67_F4_DISENO.md` (por ejemplo, el aturdimiento y la mano
    de huesos se implementan con lentitud máxima, y Ira justa se carga por porcentaje de vida perdida).
- **v67 (F3) — 40 habilidades nuevas: Mago, Asesino, Herbolista e Ingeniero.** Cada rol tiene 10 (5 pasivas y 5 activas)
  repartidas en Talentos, Veterano y Ascenso, como el Bersérker; diseño aprobado en `docs/V67_F3_DISENO.md`.
  - **Mago:** Plenitud arcana, Escarcha latente, Rayo encadenado, Escudo especular, Concatenación, Tormenta de granizo,
    Reserva de emergencia, Forma de arcano puro, Maná cristalizado y Nova estelar.
  - **Asesino:** Punto débil, Eslabón, Humo cegador, Corte relámpago, Sombra persistente, Cadena de sombras, Veneno
    concentrado, Velo de muerte, Cosecha silenciosa y Noche eterna.
  - **Herbolista:** Primavera, Polinización, Semilla explosiva, Raíz curativa, Corteza viva, Plaga de langostas,
    Antídoto universal, Floración colosal, Raíz profunda y Lluvia de semillas vivas.
  - **Ingeniero:** Aislante, Reciclaje, Mina de proximidad, Dron de apoyo, Calor residual, Domo protector, Modo ahorro,
    Exotraje, Cadena de montaje y Torre Tesla.
  - **Código:** un kit por rol (`MageKit`, `AssassinKit`, `HerbalistKit`, `EngineerKit`) con sus reglas puras
    (`*Rules`) y un único punto de enganche, `ability/ExtraKits`, que sustituye al cableado directo del Bersérker.
    Nuevas utilidades: `CooldownBook.reduce` y `AbilityManager.reduceCooldown` (recortar una espera y avisar al HUD).
    Concatenación y Forma de arcano puro tocan el coste de maná (`AbilityManager`); Calor residual engancha el fin de
    Sobrecarga.
  - **Textos:** 80 claves nuevas en los 5 idiomas; iconos provisionales con objetos vanilla y encargo de arte en
    `docs/PROMPT_ARTE_ICONOS_F3.md`. Sin opciones de configuración nuevas (usa `extraSkillDamageScale`).
  - **Pruebas:** `MageRulesTest`, `AssassinRulesTest`, `HerbalistRulesTest`, `EngineerRulesTest`,
    `CooldownBookReduceTest` y `RoleExtrasTest` ampliada a los cinco roles con datos.
  - **Diferencias con el diseño aprobado:** ver §7 de `docs/V67_F3_DISENO.md` (por ejemplo, Reciclaje no cuenta las
    muertes de la torreta).
- **v67 (F2) — 10 habilidades nuevas del Bersérker.**
  - **Talentos:** Cicatrices (los golpes fuertes reducen el daño siguiente), Sangre hirviente (bajo la mitad de vida,
    tus golpes queman), Hacha arrojadiza (atraviesa, desangra y vuelve) y Cadena de sangre (arrastra al enemigo y
    carga el siguiente golpe).
  - **Veterano (nueva sección, niveles 34–38):** Rugido intimidante, Tajos desgarradores (sangrado acumulable) y
    Juramento de sangre (Furia más larga y con robo de vida a cambio de vida).
  - **Ascenso (nueva sección, exige Maestro, niveles 42–48):** Avatar de la guerra (más grande y fuerte, sin empuje
    ni Lentitud), Eco de batalla (onda tras Torbellino o Tajo sísmico) y Ragnarök (salto y fisura ardiente).
  - Opción `extraSkillDamageScale` para el daño de las habilidades nuevas. Textos en los 5 idiomas. Iconos
    provisionales con objetos vanilla; encargo de arte en `docs/PROMPT_ARTE_ICONOS_BERSERKER.md`.
  - Equipamiento de activas con 4 bits por ranura (hasta 16 activas por rol); los formatos guardados anteriores se
    siguen leyendo.
  - Pruebas: `BerserkerRulesTest`, `RoleExtrasTest`, `LoadoutCodecV4Test` y dos casos nuevos en `UnifiedTreeLayoutTest`.
- **v67 (F1) — Árbol de habilidades unificado.**
  - **Un solo lienzo** en la pantalla de roles (J), con el kit «Berserker»: Talentos, Sendas (tres columnas S1–S7 con
    los conectores del kit, bifurcación y unión), Maestro, Definitivo y Maestría, cada sección con su cabecera.
  - **Pestañas como accesos directos:** Talentos, Sendas, Maestro y Definitivo llevan a su sección y se resaltan
    según la sección visible. Hay un minimapa lateral para saltar entre secciones.
  - **Navegación:** la rueda desplaza el árbol y Ctrl + rueda hace zoom.
  - **Confirmaciones:** aprender la primera senda o una rama de la bifurcación pide confirmación, como en la vista
    anterior.
  - **Botón «Reiniciar»** en el pie de la pantalla (antes solo estaba en la vista de sendas).
  - **Geometría pura con pruebas:** `logic/UnifiedTreeLayout`. Las secciones Veterano y Ascenso están preparadas y
    aparecerán cuando tengan habilidades (F2 en adelante).
  - **Textos nuevos** en los 5 idiomas.
  - **Puntos de árbol:** al evolucionar, Maestro da 15 (antes 10) y Definitivo 10 (antes 5): 76 puntos a nivel 50 con
    las dos evoluciones (antes 66). Los archivos de configuración con los valores antiguos por defecto se migran solos
    (`ConfigMigration`, revisión 67); un valor elegido a mano se respeta. Detalle en `docs/BALANCE.md`.

  Sin validar en partida.
- **v66 — Fichas de categoría con emblema (pantalla de roles).** Las cinco fichas pasan a ser emblemas detallados
  (espada y hacha, estrella arcana, luna y daga, escudo con brote, engranaje y martillo) con marco de remaches y tres
  estados: normal, hover (nuevo) y activa. Se dibujan a doble densidad: piezas de 48×32 en el hueco de 24×16, nítidas a
  escala de interfaz 2 y 4. De las tres versiones recibidas se eligió la más legible a tamaño real.
  `gen_roles_ui.py` las valida sin la rejilla de 2×2 y con un máximo de 48 colores. Encargo en
  `docs/PROMPT_ARTE_FICHAS_CATEGORIA.md`. Sin validar en partida.
- **v66 — Correcciones tras la prueba en partida (interfaz y HUD).**
  - **HUD:** se quita la barra de vida de arriba a la izquierda (duplicaba los corazones). El aguante, los recursos de
    rol y el elemento activo pasan a un bloque de píldoras finas sobre la barra de hambre, de su mismo ancho y por
    encima de las burbujas de aire. La opción `soulsBars` se llama ahora «Barras de estado».
  - **Esfera de maná:** pasa a 14×14 (la de 28 a la mitad, exacta en píxel), con el maná actual dentro y sin texto al
    lado.
  - **Barra de jefes:** usa el panel y el canal del kit de K y una barra de brasa derivada de la de XP
    (`gen_k_ui.py`, pieza `barras/vida_jefe.png`).
  - **Ficha de detalle del árbol:** zona de lectura dentro del marco de piedra; los botones Equipar usan el kit.
  - **Pantalla K:** coste sin cortar, punteado de las cabeceras tras el título, textos de la ficha y de las
    estadísticas dentro de sus paneles, hitos en columna fija y pasivas que se reducen hasta el 75 % antes de cortarse.
  - **Talentos, Maestro y Definitivo:** panel de piedra, marcos de nodo del kit por categoría, conectores de 2 px en la
    paleta del kit, títulos de carril sobre placa y filete con rombo; la vista y la ficha quedan dentro del marco.
  - **Configuración del HUD:** panel, pestañas y botones del kit de K; interruptores encendidos y apagados con piezas
    distintas.
  - **Arte por encargar:** emblemas de categoría (`docs/PROMPT_ARTE_FICHAS_CATEGORIA.md`).

  Sin validar en partida.
- **v66 — Árbol redistribuido con los conectores del kit «Berserker».** Los escalones pasan a estar cada 32 px (nodo de
  24 + 8 px de hueco) y los conectores del kit sustituyen a las líneas de 2 px: verticales entre S1–S4 y S6–S7,
  bifurcación de S4 a S5x/S5y (las opciones se separan a ±22 px para caer bajo las patas de la pieza) y unión de S5 en
  S6 (la bifurcación volteada, generada por `gen_tree_ui.py`). Cada tramo se enciende si el escalón al que lleva está
  aprendido; en la bifurcación y la unión, solo la mitad de la rama elegida. Los engarces de la pieza quedan ocultos
  tras los nodos. La ventana mide 344 px de alto (240 del panel + cuatro hiladas de 26 px: cabe sin desplazar a escala
  3 en 1080p) y el relleno vertical del panel usa una franja múltiplo de la hilada para no descuadrar las juntas. Nuevas
  pruebas en `TreeScreenLayoutTest` (alineación de conectores con los centros de los nodos, engarces ocultos,
  bifurcación dentro de su columna, fase del ladrillo). Sin validar en partida.
- **v66 — Pantalla de roles (J) con el kit «fantasía oscura v2».** Botones largos (normal, pulsado y desactivado) en
  todas las acciones, pestañas Talentos/Sendas/Maestro/Definitivo en tres estados, filas de la lista de roles (la
  seleccionada con realce dorado; el icono y el nombre se apartan 4 px), fichas de las cinco categorías por color (el
  nombre sigue en el tooltip: la abreviatura no cabía en el hueco de 6 px), filete con rombo central en las cabeceras
  y franja bajo el título. Todas las piezas son 9-slice con bordes de 6 px y tramos repetidos (reutiliza
  `KScreenLayout.tiles`, con pruebas); el rombo del filete se dibuja aparte para no deformarse. Arte fuente en
  `tools/textures/source/ui/roles/`; generador `tools/textures/gen_roles_ui.py` (admite `--check`). Distribución,
  controles y validaciones del servidor sin cambios; sin las piezas se usa el dibujo anterior. La vista de sendas de
  respaldo (`PathsScreen`) conserva sus pestañas. Sin validar en partida.
- **v66 — Kit «Berserker» en la pantalla del árbol.** Sustituye al kit anterior (salvo los marcos de nodo, que se
  conservan): panel de piedra con grietas y remaches, ficha de detalle, cabeceras de Sangre, Acero y Trueno, etiquetas
  S1–S7, habilidad definitiva, barra de maestría de 20 casillas (320×18) y botones Equipar (antes «genérico»),
  Reiniciar y Cerrar, los tres con icono a la izquierda. Los paneles se estiran desde el centro: cada mitad queda anclada
  a su borde y el hueco se rellena con la franja anterior al centro, más allá de los adornos (40 px), para que las
  grietas no se repitan (`TreeScreenLayout.anchoredTiles`, con pruebas). La maestría baja a su propia fila bajo los
  botones, sin solaparse en ningún ancho. Sin validar en partida.
- **v66 — Pantalla K con la dirección de arte.** Atributos y Habilidades usan el kit de 34 piezas
  (`docs/PROMPT_ARTE_PANTALLA_K.md`): panel principal (o de Hoguera, con su emblema en la cabecera), pestañas en tres
  estados ajustadas al texto, columnas hundidas, ficha de detalle, filas de atributo y flechas de reparto, cabeceras de
  sección, botones de confirmar/deshacer/cerrar, y tarjetas de habilidad en cuatro estados (normal, hover, máxima y
  recién subida, que parpadea) con hitos y barra de XP recortada por el progreso. Se conservan la ventana de 430×262,
  las 10 tarjetas en 5 × 2, las pestañas, los controles y todas las validaciones del servidor. Los paneles son 9-slice
  con esquinas de 12 px y las pestañas y botones repiten su tramo central (nunca se estiran). Geometría pura con
  pruebas (`logic/KScreenLayout`); arte fuente en `tools/textures/source/ui/k/`; generador `tools/textures/gen_k_ui.py`
  (admite `--check`). Si el paquete de recursos no trae las piezas, se usa el dibujo anterior. Sin validar en partida.
- **v66 — Pantalla del árbol de habilidades con la nueva dirección de arte.** `TreeScreen` muestra las tres sendas del rol en
  columnas con sus siete escalones (S5 con las dos opciones de la bifurcación), cabecera con icono, nivel, puntos y experiencia,
  barra de maestría de 20 gemas, habilidad definitiva y ficha del escalón elegido, con paneles 9-slice, cabeceras de senda,
  marcos de nodo por categoría y estado (bloqueado, disponible, comprado, elegido y descartado), etiquetas S1–S7 y botones del
  kit. Aprender, la confirmación de senda y de rama, y las ranuras de equipar usan el mismo flujo y las mismas validaciones del
  servidor que antes. La geometría es una clase pura con pruebas (`logic/TreeScreenLayout`). Arte fuente en
  `tools/textures/source/ui/`; generador `tools/textures/gen_tree_ui.py` (admite `--check`). Con `newTreeScreen = false` en
  `aptitudes-client.json`, o si el paquete de recursos no trae las piezas, se usa la vista de sendas anterior. Incluye el
  botón «Reiniciar»: devuelve todos los puntos del árbol del rol a cambio de niveles de experiencia de Minecraft
  (`treeRespecEnabled`, `treeRespecLevelsBase`, `treeRespecLevelsPerPoint`, `treeRespecLevelsMax`; el modo creativo no paga).
  El coste se calcula con `role/RespecRules` (pura, con pruebas); el servidor lo valida y lo cobra
  (`RoleActionPayload.RESPEC_TREE`, con límite de frecuencia), y el cliente solo muestra el coste y pide confirmación. El
  nivel y la evolución del rol no cambian. Coste por defecto aprobado por el autor: 20 niveles fijos (base 20, 0 por punto,
  máximo 20), sea cual sea el número de puntos gastados. Sin validar en partida.
- **v66 — Ajustes visuales tras la prueba en partida (árbol, Maestro y Definitivo).** Los nodos bloqueados del árbol enseñan su
  icono atenuado con un candado pequeño (el candado del marco lo tapaba), los nombres largos de las sendas se parten en dos
  líneas en vez de cortarse, el texto de «Reiniciar» y «Volver» ya no pisa el icono del botón y el texto desactivado se lee
  mejor. En `RolesScreen`, las pestañas Maestro y Definitivo reparten las mejoras a distancia fija y centradas, con más zoom
  (medios pasos para conservar el píxel). Sin validar en partida.
- **v66 — HUD: marcos de las casillas de habilidad.** Cada casilla de la barra de habilidades usa un marco de la nueva
  dirección de arte según la categoría del rol (marcial, arcano, sombra, guardián u oficio) y su estado: lista, en espera,
  sin energía, seleccionada al lanzar y definitiva. 20×20 px, el tamaño actual de la casilla, así que la disposición no
  cambia. Arte fuente en `tools/textures/source/hud/casillas/` (ilustraciones suaves de 44×44, no pixel art con rejilla);
  generador `tools/textures/gen_hud_sprites.py` (admite `--check`). Si el paquete de recursos no trae el marco, o la
  casilla está vacía, se mantiene el dibujo anterior. Sin validar en partida.
- **v66 — HUD: esfera de maná.** El maná se muestra con una esfera de 28×28 px junto a la hotbar (a la izquierda, donde
  estaba la barra) que se llena de forma continua; el texto «Maná a/b» queda a su izquierda. Sustituye solo a la barra o
  píldora de maná, en el modo clásico y en el de barras Souls: la vida y el hambre no cambian. Sin el sprite se mantiene
  la barra anterior. Pendiente de la misma entrega: barras de experiencia y de jefe, indicadores de combo y trance y
  avisos de evento. Sin validar en partida.
- **v66 — Iconos de rol de la dirección de arte.** Los 20 roles usan los iconos de la nueva dirección de arte en la pantalla
  de roles, las sendas, el grimorio y el HUD (la insignia principal): 16×16 para el tamaño del árbol y 32×32 para usos
  grandes. Arte fuente en `tools/textures/source/roles/`; generador `tools/textures/gen_role_icons.py` (admite `--check`).
  Si el paquete de recursos no trae el icono, se usa el sello anterior. Los tooltips y las insignias de firma y definitiva
  siguen con los sellos anteriores. Sin validar en partida.
- **v65 — Tablón por niveles.** Los contratos de Oficial y Veterano pagan más esmeraldas, fragmentos y más vales. Tres
  encargos nuevos: caza en el Nether, criaturas nuevas (Osario, Errante, Escorificador y Custodio) y expedición. Sin
  validar en partida.
- **v65 — Enemigos nuevos.** El Errante Velado aparece en la Niebla de almas (10 % de las apariciones, junto al
  Osario Quebrado con otro 10 %) y tiene huevo de aparición y entrada en el bestiario. El Custodio de las Cuatro
  Llaves sustituye al guardián vanilla de los templos sellados. El generador conserva los recursos del Custodio
  y deja fuera los del Errante, que se mantienen en su proyecto independiente de Blockbench.
- **v65 — Errante Velado.** Modelo y atlas de 256 × 256, seis animaciones, siete sonidos originales y Rasgado
  Espectral con aviso visible, daño en arco y Debilidad I. Los Errantes de la Niebla se retiran por turnos al
  cerrar el evento. Las muertes válidas dan una tirada por un Jirón de Velo Espectral.
- **v65 — Rediseño de jefes (hoja de arte).** El Coloso Sin Rostro tiene una caja de colisión de 3,8 bloques de alto y adopta netherita,
  pizarra y energía roja. El Guardián Desmontado tiene escudo carmesí con cruz dorada y cuerpo de piedra lisa. El
  Autómata Antiguo usa piedra tallada, bronce, cobre, ojo ámbar y dos esferas auxiliares. La esfera del Autómata tiene
  sprite propio: bronce y núcleo ámbar, y cian al ser devuelta (indicador de aturdimiento). Sin validar en partida.
- **v65 — Coloso Sin Rostro reconstruido.** Modelo de Blockbench de 3,4 bloques visuales con espadón y capa
  articulados, siete animaciones y dos atlas pintados de 512 × 512. La armadura agrietada y su emisión se
  seleccionan según la fase persistente del servidor. Conserva su caja de colisión y sus ataques.
- **v65 — Trance de combate (F2; valores ajustados, ver «Cambiado»).** La sintonía se llena con golpes críticos a criaturas (+10) y con habilidades (+15).
  Al llegar a 100 se entra en trance durante 10 s con +20 % de daño saliente. Se muestra en la barra de acción. Sin
  validar en partida.
- **v65 — Pedidos del gremio (F4, servidor; retirado, ver «Cambiado»).** Estado por mundo (`OrderBookState`), acciones de servidor (`OrderBook`:
  publicar, entregar, recoger y cancelar) y reglas puras con pruebas (`OrderBookRules`).
- **v65 — Pedidos del gremio (F4, pantalla; retirado, ver «Cambiado»).** El tablón tiene una página «Pedidos» con tres filas por página, botón
  de publicar con el objeto de la mano y selector de nivel. Entregar, recoger y cancelar pasan por el servidor
  (`OrderViewPayload`, `OrderActionPayload`). La lista se pagina de tres en tres. Sin validar en partida.
- **v65 — Mancha de XP (F3).** Al volver a morir antes de recuperar la mancha, se pierde un 25 % y el resto se suma a
  la nueva mancha, en el lugar de la muerte. Antes se perdía entera. Sin validar en partida.
- **v65 — Expediciones de Amenaza (F5, primera parte).** La ficha de expedición inicia un recorrido de cuatro módulos
  (combate, élite, jefe, evento o recolección de fragmentos) dentro de 48 bloques, con límite de 15 minutos y
  recompensa provisional. Los miembros de la party en la zona comparten la expedición. La ficha está en la pestaña
  creativa. Cada expedición tiene dos de seis modificadores (niebla, lluvia de brasas, oleada élite, hambre, jefe
  aturdido, botín abundante). Sin validar en partida.

- **v65 — Escorificador de Hierro.** Nuevo modelo de Blockbench de 3,25 bloques de altura visual, rejilla de cinco
  barras, chimenea hueca y textura de 256×256. Usa los clips `idle`, `walk` y `slam`. Aparece en el Nether y en
  la Lluvia de brasas, con huevo y entrada en el bestiario. La sincronización visual de `slam` queda pendiente de
  validación en partida.
- **v65 — Huevos y Osario.** Los huevos del Osario Quebrado y del Errante Velado aparecen en la pestaña creativa de
  Aptitudes. El Osario emite ectoplasma y polvo de tierra alrededor de los pies. Sin validar en partida.

- **v65 — Fase 3 de los jefes.** El Guardián Desmontado acelera `volley` y `dive` (enfriamientos de 30 y 50 ticks).
  El Autómata Antiguo lanza dos esferas en abanico y su rayo barre un cono de 60°. Sin validar en partida.

- **v65 — Ruinas de batalla del Coloso.** Al invocar al Coloso con el sello, se levantan ocho columnas rotas (20 min) y
  una sala circular de 21 bloques y 4 bloques de alto que se cierra con niebla mientras se lucha dentro. Si el
  terreno no deja cerrarla, los participantes lo ven en el chat. Sin validar en partida.
- **v63 — Jefes animados.** El mod requiere ahora **GeckoLib 4.7.7**. Tres jefes animados: el Coloso Sin
  Rostro (tajo en arco, golpe descendente con onda y, con la armadura agrietada, embestida), el Guardián
  Desmontado (piezas que hay que romper en orden; con las tres caídas, el núcleo recibe más daño) y el
  Autómata Antiguo (esferas que se pueden devolver para aturdirlo, pisotón y, en fase 2, un rayo que se corta
  en los bloques). De momento se invocan con sus huevos; sus estructuras llegarán después. Tienen página en el
  bestiario.
- **v64 — Templos sellados y Vasos de vida.** Un templo de 15×15 aparece en el Mundo Normal con el
  aspecto y el puzle de su anillo de Amenaza: imita con las palancas las luces sobre la puerta sellada
  y, desde el anillo II, pisa las placas en el orden de sus velas. Al abrirse despierta el Guardián del
  templo, cuya llave abre el Relicario: cada jugador recibe un Vaso de vida (+1 corazón permanente,
  hasta 5, `lifeVesselMax`). Un vaso no se puede usar dos veces ni copiar. Dos logros nuevos.
- **v55 — Talentos por datapack (retirado en v65).** El pack de ejemplo añade Paso ligero y Paso firme al Espadachín.
  Con el pack activo, abre **Talentos del pack** desde J para consultar y aprender la rama con puntos
  del rol. Cada rango da +2 % de velocidad de movimiento, hasta +6 % entre ambos roles activos.
  Al retirar el pack o perder un requisito, recuperas los puntos de los rangos afectados.
- **v48 — Reforja y Templado.** Aplica Reserva o Impulso a una hoja o foco de evolución en la Mesa del
  gremio. Templa el arma hasta el rango II en la Hoguera con Corazones de campeón y Fragmentos de alma.
  Los talleres muestran el efecto actual, el resultado y los ingredientes requeridos antes de confirmar.
  El arma conserva su rango al cambiar de afijo y mantiene sus runas, núcleos y encantamientos.
- **v45 — Traducciones, T8 fase 1**: traducción inicial de interfaz y objetos a portugués de Brasil
  (`pt_br`), francés (`fr_fr`) y alemán (`de_de`), que conviene revisar con hablantes nativos.
  Cada idioma contiene las 3512 claves de inglés en el mismo orden: 1386 traducidas en esta fase;
  grimorio y nodos/sendas (2126 claves) conservan el inglés hasta las fases 2 y 3.
  Los rótulos de interfaz generados se mantienen en sus generadores. Nuevo comprobador de duplicados,
  claves, orden y marcadores de formato, con pruebas de errores y ejecución en GitHub Actions.
- **v45 — Traducciones, T8 fase 2**: traducción inicial de los 23 capítulos del grimorio a los tres
  idiomas, que conviene revisar con hablantes nativos. Incluye sendas, las diez piedras angulares,
  logros, contratos, eventos y alquimia; las recetas se consultan en el juego.
  Catálogo y generador reproducibles, con el párrafo de evolución en su generador original, comprobación
  de ancho con la fuente de Minecraft y pruebas de conservación de las demás traducciones.
  Acumulado: 1432 claves traducidas por idioma; los 2080 textos de nodos/sendas esperan la fase 3.
- **v45 — Traducciones, T8 fase 3**: traducción inicial de los nodos base, legendarios, evolucionados
  y sendas completas S1–S7 a portugués de Brasil, francés y alemán, que conviene revisar con hablantes
  nativos. Las 3512 claves de cada idioma están cubiertas, con el mismo orden y marcadores que el inglés.
  Los dos generadores originales incorporan los tres idiomas y `--check`; se comprueban los números,
  las bifurcaciones y las piedras angulares. Inglés y los siete archivos de español no cambian.
  El generador de evolución reconoce el párrafo vigente del grimorio y evita duplicar el antiguo al regenerar.

### Cambiado
- **v67 — Descripciones de habilidades en estilo llano (5 idiomas).** Primero qué hace, hablándole al jugador; las
  condiciones, con palabras; al final las cifras de cada rango («Rango 1 / 2 / 3: +2 % / +4 % / +6 %»). Se quitan las
  coletillas («Las sendas son exclusivas…», «Bifurcación…», «Potencia S4», «Se suma a las Sendas», «Los valores son los
  de la configuración por defecto», el prefijo «MECÁNICA:»). Abarca 300 nodos base (`tools/lang/base_desc.py`, nueva
  fuente única con `--apply` y `--check` en CI), 480 de senda, 200 de evolución y las 10 piedras angulares.
- **v67 — Versión según el número de commits.** `build.gradle` calcula la versión con `git rev-list --count HEAD`
  (123 → 1.2.3; 240 → 2.4.0). Sin historial (ZIP o clon superficial) usa `mod_version` de `gradle.properties`, que pasa
  a 2.4.0. El CI clona el historial completo (`fetch-depth: 0`).
- **v67 — Revisión de mixins.** Los 10 mixins coinciden con Yarn 1.21.1 (`docs/REVISION_MIXINS.md`). El dibujo de una
  forma (Cambiaformas) ya no cierra el juego si el renderizador de esa criatura falla: se registra una vez y se dibuja
  el jugador normal.
- **v65 — Ajustes tras la prueba en partida.** Se retira el sistema de pedidos del gremio (no encaja con un servidor de
  amigos). Los pasos del tutorial ya no dan esmeraldas. El trance va más lento: +5 crítico, +8 habilidad, 6 s y +10 % de
  daño por defecto. Las texturas de los jefes ya están en `main`.
- **v62 — Armas y objetos.** Las 40 armas de evolución y 12 armas de rol tienen volumen en la mano, en
  el suelo y en los marcos; el inventario conserva su icono plano. Las texturas ganan destellos en
  metales y gemas y veta en madera y cuero. Los tooltips muestran el sello del rol, el daño por
  segundo de las armas cuerpo a cuerpo, las cifras de los disparos y la rareza. Los objetos de rol
  comunes pasan a poco comunes (solo cambia el color del nombre).
- **v61 — Grimorio.** Nueva pestaña Bestiario: cada criatura o jefe abre su página al vencerla, con
  modelo, estadísticas, debilidades, consejos y botín; el grupo cercano también la desbloquea.
  Pestaña Progreso con logros, bestiario, jefes, escalones de senda y piedras angulares. Buscador
  global (capítulos, objetos, criaturas descubiertas y roles) y marcadores con ☆. En Sendas, cada rol
  muestra sus tres sendas con iconos y piedra angular. Con el grimorio en el inventario, U sobre un
  objeto de Aptitudes abre su ficha. Si hay un paso de tutorial pendiente, una pestaña lleva a su
  capítulo. Los capítulos estrenan ilustraciones de cabecera.
- **v60 — Arenas de jefe.** La niebla rodea al Centinela, al Oráculo y al Campeón en estructuras
  nuevas cuando entras a luchar. Al sellarse la arena suena un tema original. La niebla se retira
  al vencer al jefe o si no queda nadie combatiendo dentro.
- **v60 — Mejora de frascos.** Descansa en una Hoguera y abre Frascos. Usa Fragmentos de alma y
  un núcleo de jefe para añadir una carga y aumentar la recuperación de Estus o Éter. La mejora
  queda guardada en el propio frasco.
- **v59 — Editor del HUD.** Mayús + O ofrece tres disposiciones para la barra de habilidades y un
  modo minimalista. Las barras de vida, maná y XP y los avisos cambian con transiciones suaves.
  La tarjeta de XP se ilumina al subir de nivel. Puedes desactivar estas animaciones.
- **v59 — Barra de jefes.** Los jefes y campeones de Aptitudes comparten un panel centrado cerca de la
  parte inferior. Muestra su vida, la fase de los jefes y un recuento si hay más enemigos cerca.
- **v57 — Atlas y Sendas.** Las dos opciones de la bifurcación tienen filas separadas y el rango
  queda alineado a la derecha. La ficha envuelve los requisitos y el lienzo de Talentos despeja
  el fondo y usa conectores más finos.
- **v56 — Encuentros en estructuras.** Los jefes dan un segundo de aviso antes del golpe final.
  Sal del círculo del Centinela, cruza al centro seguro del anillo del Oráculo o esquiva de lado
  el cono del Campeón. Una pared sólida detiene el golpe.
- **v54 — Animaciones de lanzamiento.** Cinco remates 3D por categoría de rol, desde el tajo marcial
  hasta el engranaje de Oficio. Se ven en primera o tercera persona según el ángulo y también los
  ven los jugadores cercanos. El interruptor Animaciones 3D está en Mayús + O.
- **v53 — Atlas de talentos.** En J, abre Talentos, Sendas, Maestro o Definitivo desde la cabecera
  del rol. Los sellos, iconos y acentos identifican cada rol. La ficha muestra el rango y los
  requisitos del nodo seleccionado y el cursor revela la descripción completa.
  Los 20 roles tienen paleta y emblema propios en el atlas y en Sendas. Las pestañas distinguen
  selección y foco, y las de Sendas muestran el nombre completo al pasar el cursor.
- **v52 — Persecución del Eclipse.** Las criaturas del evento y su Heraldo buscan jugadores visibles
  a su alrededor. Si pierden de vista al objetivo, intentan llegar al último punto en el que lo vieron
  durante un máximo de seis segundos. Soborno y camuflaje siguen protegiendo al jugador.
- **v51 — Mesa y Tablón del gremio.** Separamos la cuadrícula, el resultado y el inventario de la Mesa.
  El Tablón muestra ofertas y contratos activos en columnas, con una fila de acciones por ficha.
  Al pasar el cursor se pueden leer el objetivo y la recompensa completos. El árbol usa un lienzo común e indica que se elige una senda.
- **v50 — HUD e iconos del árbol.** El árbol y la barra de habilidades comparten iconos propios.
  Los sellos identifican el rol, las activas muestran sus marcas y los nodos evolucionados distinguen
  Maestro y Definitivo. La barra separa piedras angulares y mecánicas, muestra teclas y costes con
  fondo de contraste y se ajusta al espacio disponible incluso al arrastrarla en Mayús + O.
- **v47 — Asalto del Eclipse, fase 1**: oleadas de 4 criaturas por jugador en Penumbra y 8 en Totalidad,
  con élites y campeones garantizados cuando sus probabilidades están activadas y hay espacio/presupuesto.
  Equipo completo visible en unidades de élite, sin soltarlo al morir; conservan los multiplicadores previos.
  Presupuestos compartidos por proximidad, tope por zona y por mundo, turnos rotativos y búsqueda del jugador.
  El Heraldo se organiza por encuentros próximos, incluso con grupos dispersos; al llegar al límite puede
  sustituir una unidad ordinaria sin botín. No aparece sobre el jugador si falta un emplazamiento seguro.
  Finalización protegida contra cobros repetidos, exclusión de creativo/espectador y recuento restaurado
  tras reiniciar. Siete opciones nuevas validadas y capítulo del grimorio actualizado en once idiomas.
- **v49 — Mesa del gremio y Hoguera.** La Mesa del gremio se fabrica en la mesa de trabajo y reúne
  el resto de las recetas de fabricación de Aptitudes, incluidas la Hoguera y el Grimorio. La Mesa
  estrena un panel 3×3 de madera, latón y jade. La Hoguera incorpora un brasero de piedra, llamas
  animadas y una cabecera propia al descansar. El grimorio indica qué mesa requiere cada receta.

### Corregido
- **v56 — Generación de jefes.** Torres, santuarios y campamentos guardan sus jefes con el terreno al
  generarse. Esto corrige el bloqueo que podía detener el mundo e impedir abrir o romper bloques.
- **v58 — Cambiaformas.** El murciélago bate las alas al verse en tercera persona. La gallina
  agita las alas al caer y el slime se estira durante el salto. El poder del murciélago da un
  vuelo corto que se dirige con la mirada.
- **Aldeano del gremio.** Un aldeano sin trabajo ahora se convierte en Maestro del gremio al encontrar una Mesa
  del gremio. Vanilla solo ofrece como puesto de trabajo los tipos de su etiqueta
  `minecraft:acquirable_job_site`; el mod la amplía con un dato de etiqueta, sin mixins.
- **Modelos de los jefes.** Los tres jefes tienen más piezas y detalle (placas, remaches, capa, púas,
  pistones, lente y cañón). La textura pasa a 256 px.

## [1.4.0] — 2026-11-06

Más opciones para jugar en grupo y seguir progresando: contratos, logros, sendas hasta el nivel 50,
piedras angulares, alquimia, maestría y zonas de amenaza. Incluye un tutorial guiado y el grimorio actualizado.

### Añadido
- **Grupos**: abre el menú con P, invita a tus compañeros, comparte XP y presta apoyo a aliados.
  El grupo conserva sus miembros, opciones y nombres al reiniciar el mundo; el fuego amigo es configurable.
- **Contratos del gremio**: tres ofertas diarias por jugador, con encargos de caza, entrega, pesca,
  encantamiento, oficio, forja, eventos y jefes. Completa contratos para ganar recompensas y reputación.
- **17 logros** en la pestaña «Aptitudes» de los avances, con reconocimiento al entrar de los objetivos
  de estado que ya habías conseguido.
- **Roles hasta el nivel 50** y una senda exclusiva por rol, elegida entre tres. Siete escalones con una
  bifurcación de elección única y una piedra angular final; la vista «Sendas» permite consultar la progresión.
  Si tenías dos sendas, se conserva la de más rangos y se devuelven los puntos de la otra.
- **Piedras angulares**: 60 nodos finales con diez plantillas de efectos, sonidos e indicador en el HUD.
  Carnicería, Baluarte, Sobrecarga, Detonación, Contagio, Legión, Espectro, Fortuna, Tempestad y Santuario.
- **Maestría**: la XP nueva que sobra al máximo mejora las pasivas de habilidades y la eficiencia
  energética de roles. Cada punto aporta +0,5 %, hasta +10 % por defecto. K muestra los puntos en dorado;
  conserva sus diez tarjetas. La XP administrativa y los modos creativo/espectador no generan maestría.
- **Amenaza**: los hostiles nuevos del Mundo Normal se fortalecen por anillos de 1000 bloques desde la
  aparición del mundo, hasta V. Por anillo: +15 % de vida base, +10 % de daño, más probabilidad de élite
  y 10 % de probabilidad de una tirada extra de botín normal por baja de supervivencia. El HUD avisa al
  cambiar de anillo. Se excluyen jefes, invocaciones, criaturas de eventos y nombres previos.
- **Alquimia del Herbolista**: pociones de Maná, Frenesí, Velo del eclipse y Vigor del bosque; pétalos
  extraíbles del saquito de hierbas. Un Herbolista elegible acelera el destilado un 50 % y amplía su
  duración un 50 %. La XP por destilar tiene rendimiento decreciente; no se acumulan ampliaciones.
- **Tutorial de seis pasos**, con una esmeralda por paso y cobro único. El recordatorio se puede ocultar
  en Mayús + O; `/aptitudes tutorial status` consulta el progreso y `skip` lo omite de forma irreversible.
- **Grimorio actualizado**, con 23 capítulos, sendas, piedras angulares, logros y alquimia; consulta las
  recetas reales del juego. Los nodos de senda tienen iconos propios en el árbol y en la vista de sendas.
- **Configuración en el juego** para operadores, desde `/aptitudes config`, K o Mod Menu opcional.
  Los cambios pasan por validación del servidor y se sincronizan con los clientes.

### Cambiado
- **Eventos rotativos**: el Eclipse se alterna con la Niebla de almas y la Lluvia de brasas, sin repetir
  el evento anterior. La Niebla solo trae no muertos a pie, con variantes según el bioma.
- **Eclipse reforzado**: armadura y equipo en las oleadas, +25 % de vida y daño durante la Totalidad y
  esqueletos wither. La Lluvia de brasas incorpora meteoritos grandes/enormes y criaturas del Nether.
- **Interfaz**: K se desplaza si no cabe; el indicador del Eclipse deja libre la barra de acción.
  Textos nuevos en inglés y español, con seis variantes regionales idénticas al español de España.
  Portugués, francés y alemán quedan para una actualización posterior.

### Corregido
- El nombre guardado del líder del grupo se conserva al cargar, aunque no esté conectado.
- En Mayús + O, el texto de escala y la ayuda ya no se superponen a los botones.

### Validación
- T4–T6 probadas en partida según confirmación del autor el 2026-11-06; v15–v19 y v29–v41, el 2026-11-03.
  Las pruebas de regresión y limitaciones siguen documentadas en `docs/TESTING.md` y `README.md`.

## [1.3.7] — 2026-11-02

Primera versión preparada para publicar. Las etapas v34–v37 aún no se han probado en partida
(ver `docs/TESTING.md`).

### Añadido
- **v37 — Sonidos propios**: aviso, inicio y totalidad del Eclipse, llegada del Heraldo, remate de combo y
  engarzar/quitar runas (sintetizados por programa, con subtítulos).
- **v36 — Anti-granjas persistente**: el registro de bloques colocados se guarda con cada mundo.
- **v35 — Runas+**: runas de Escarcha, Trueno y Guardia; quitar la última runa en la Mesa del gremio
  (Mayús + clic derecho, cuesta esmeraldas); valores de las runas configurables.
- **v34 — El Eclipse**: evento cada 10 días con cielo rojo, oleadas de élites y campeones, el Heraldo del
  Eclipse y recompensas (Fragmentos, Núcleo y Runa del Eclipse). Comando `/aptitudes eclipse`.
- **v33 — Jefes propios** con fases en torres, santuarios y campamentos; sus núcleos mejoran armas de evolución.
- **v32 — Runas** engarzables en armas de rol (dos por arma).
- **v28 — Combos** de combate con bonificación de daño y remates por categoría de rol.
- **v27 — Mesa del gremio**: las armaduras de rol se fabrican en ella.
- **v26** — Colisión de las transformaciones ajustada al tamaño de la forma.
- **v25** — Texturas de las 80 piezas de armadura de rol.
- **v24** — Animaciones de lanzamiento e impacto de todas las habilidades.
- **v23** — El árbol crece al evolucionar: filas Maestro y Definitivo (200 nodos nuevos).
- **v22** — Texturas nuevas de armas y objetos clave.
- **v20–v21** — Grimorio interactivo con recetas reales; habilidad de Encantamiento; pantalla K animada.
- **v19** — Desplazamiento en la pantalla de roles; filtro de objetivos para criaturas con IA Brain.
- **v18** — Tiempos de espera persistentes y muros temporales seguros.
- **v17** — Anti-granjas de colocar y romper bloques.
- **v15** — Habilidad de Pesca y rendimiento decreciente de XP por fuente repetida.
- **v14** — Grimorio, elementos con fusiones y armas de evolución.
- **v10–v13** — Árbol ramificado, 20 roles, legendarias, estructuras, aldeano del gremio, élites, campeones
  y accesorios.

### Cambiado
- **v27** — Valores de los atributos −30 % por defecto.
- **v35** — Las esperas de las runas viven en memoria (el arma ya no se re-equipa al curar).
- **v37** — Los sonidos del Eclipse usan la categoría «Criaturas hostiles».

### Corregido
- Auditoría de seguridad: desbordamiento de enteros en la Hoguera, validación de la configuración y tope
  global del multiplicador de daño.
- v16: la Defensa solo da XP por daño recibido de verdad; los esbirros no se convierten en ahogados; el
  Bombardeo orbital no hiere a aliados.
- v15.1–v15.2: bloqueo de legendarias exclusivas en el árbol; la marca del grimorio sobrevive a la muerte.

### Pendiente
- Decidir si `attributeScaling = SOFTCAP` pasa a ser el valor por defecto.
