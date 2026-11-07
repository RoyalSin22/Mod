# Publicar Aptitudes (Modrinth / CurseForge)

Preparación de **Aptitudes 1.4.0**, con fecha **2026-11-06**. Los pasos marcados **(autor)** requieren
tu cuenta o una decisión tuya. Este trabajo prepara el JAR y los textos; la publicación la hace el autor.
La publicación está aplazada por decisión del autor; integrar las ramas en Git no publica el mod.

## 1. Antes de compilar la versión

- [x] T1–T6 integradas en `main`; T6 en `1be8a15`, tras GitHub Actions aprobada.
- [x] Pruebas en partida de T4–T6 confirmadas por el autor el 2026-11-06. Historial de pruebas y guiones
  de regresión en `docs/TESTING.md`; v15–v19 y v29–v41 ya constaban como probadas por el autor.
- [x] `mod_version=1.4.0` en `gradle.properties`, sin cambiar Minecraft, Java ni dependencias.
- [x] Notas de 1.4.0 fechadas el 2026-11-06; novedades de T8 separadas en «Sin publicar».
- [x] Build de la base T7: `.\gradlew.bat build` → `BUILD SUCCESSFUL`; 217 pruebas sin fallos,
  errores ni omitidas. JAR con versión interna 1.4.0; clases y recursos idénticos a T6 salvo ese metadato.
- [x] T7 integrada en `main` en `e76b62f` tras GitHub Actions aprobada, con autorización del autor.
  T8 se valida por fases aparte; no se ha publicado en Modrinth/CurseForge.
- [x] T8 fase 1: build y 217 pruebas JUnit aprobados, 15 pruebas del comprobador y validación de los
  tres idiomas. Integrada en `main` en `8c42ba3`; el JAR añade solo esos tres JSON a la base T7.
- [x] T8 fase 2: `BUILD SUCCESSFUL in 13s`, 217 pruebas JUnit y 23 pruebas Python aprobadas.
  Traducidas las 46 claves del grimorio por idioma; regeneración, marcadores y ancho comprobados.
  Frente al JAR de fase 1 solo cambian los tres JSON; revisión visual y con hablantes nativos pendiente.
- [x] T8 fase 3: `BUILD SUCCESSFUL in 42s`, 217 pruebas JUnit y 35 pruebas Python aprobadas.
  Cobertura de las 3512 claves por idioma; ambos generadores de nodos admiten los tres idiomas y `--check`.
  El JAR conserva las clases, IDs, balance y recursos anteriores, salvo los tres idiomas nuevos.
  La revisión en partida y con hablantes nativos sigue pendiente.
- [ ] **(Autor)** Elegir el tipo de publicación. Si queda alguna etapa seleccionada sin probar en partida,
  usar **Beta** y describir qué falta; no presentar una comprobación estática como prueba en partida.

## 2. Archivo a subir

- `build/libs/aptitudes-1.4.0.jar` (no el `aptitudes-1.4.0-sources.jar`).
- Requisitos que hay que indicar: Minecraft **1.21.1**, Fabric Loader **≥ 0.16.0**, **Fabric API para 1.21.1**, Java **21**.
- Compilación y pruebas con Fabric Loader **0.16.14** y Fabric API **0.116.6+1.21.1**. Mod Menu es opcional.
- Entorno: necesario en **cliente y servidor** (tiene pantallas, HUD, mixins de cliente y lógica de servidor).

## 3. Página del proyecto (autor)

- [ ] Nombre, resumen corto y descripción (puedes partir de la introducción de `README.md`).
- [ ] Icono: `src/main/resources/assets/aptitudes/icon.png` (128×128, generado con `tools/textures/gen_icon.py`).
- [ ] Capturas: pantalla K, vista de sendas y piedra angular, Hoguera, grupo, tablón de contratos,
  avances «Aptitudes», un evento y el capítulo «Alquimia» del grimorio.
- [ ] Categorías sugeridas: Aventura, Equipamiento, Mobs, Juego de rol.
- [x] Licencia: MIT (`LICENSE`, `Copyright (c) 2026 RoyalSin22`).
- [x] Enlaces: código fuente e incidencias en `fabric.mod.json` (`github.com/RoyalSin22/mod`, repositorio público).

### Resumen público

Habilidades que mejoran con el uso, veinte roles y sendas hasta el nivel 50. Forma un grupo, cumple
contratos del gremio y afronta eventos del mundo con tus compañeros.

### Descripción pública

**Aptitudes** añade progresión RPG a Minecraft sin salir de tu mundo de supervivencia. Entrena diez
habilidades jugando: Minería, Excavación, Tala, Agricultura, Combate, Tiro con arco, Defensa, Agilidad,
Pesca y Encantamiento. Mejora tus atributos en la Hoguera y combina un rol principal con uno secundario.

- **Veinte roles y evoluciones.** Aprende talentos y equipa habilidades; evoluciona a Maestro y Definitivo,
  con armas, armaduras, características y definitivas propias. El nivel máximo inicial de rol es 50.
- **Sendas y piedras angulares.** Escoge una de tres sendas por rol, recorre siete escalones y elige una
  rama en la bifurcación. Al final te espera una piedra angular. Sus diez plantillas ofrecen efectos de
  ataque, defensa, invocación y apoyo; la vista «Sendas» muestra los requisitos y tu progreso.
- **Grupos.** Abre el menú con P, invita a tus compañeros y configura el reparto de XP. Hay apoyo a aliados,
  protección de grupo configurable e indicadores de tus compañeros; miembros y opciones se guardan.
- **Contratos del gremio.** Consulta tus tres ofertas diarias en el tablón, completa encargos y gana
  recompensas y reputación. Hay caza, entregas, pesca, encantamiento, fabricación, eventos y jefes.
- **Logros.** La pestaña «Aptitudes» de los avances reúne 17 objetivos que acompañan tu progreso.
  Los objetivos de estado ya conseguidos se reconocen al entrar.
- **Eventos del mundo.** El Eclipse, la Niebla de almas y la Lluvia de brasas se alternan. Combate oleadas
  con armadura, no muertos propios de cada bioma y criaturas del Nether entre meteoritos de varios tamaños.
- **Alquimia del Herbolista.** Destila Maná, Frenesí, Velo del eclipse y Vigor del bosque. Un Herbolista
  elegible acelera el soporte y prolonga las pociones; el grimorio consulta las recetas del juego.
- **Maestría y Amenaza.** Sigue aprovechando la XP nueva al máximo. Al explorar lejos de la aparición del
  mundo, los hostiles nuevos se fortalecen por anillos hasta V y ofrecen mejores oportunidades de botín.
- **Ayuda dentro del juego.** Un tutorial de seis pasos y un grimorio de 23 capítulos explican los sistemas.
  Ajusta el HUD con Mayús + O; los operadores pueden editar las reglas del servidor desde su pantalla.

También incluye combos, runas, accesorios, élites, campeones y jefes. La exclusividad de las habilidades
legendarias es opcional. Disponible en inglés y español, con seis variantes regionales de español.
La candidata de T8 añade una traducción inicial a portugués de Brasil, francés y alemán: las 3512 claves,
con interfaz, objetos, los 23 capítulos del grimorio, nodos de rol y las 60 sendas completas. Conviene
revisarla en partida y con hablantes nativos antes de presentarla como una traducción revisada.

Instala Aptitudes y Fabric API para Minecraft 1.21.1 tanto en el cliente como en el servidor, con Java 21.
Consulta la tabla «Limitaciones conocidas» del README para el filtro de IA, cambios de bloques y alcance
de los sistemas de Amenaza y destilado. El autor decide cuándo subir esta versión a Modrinth o CurseForge.

## 4. Contenido de terceros

- Texturas, sonido e icono son **propios**: se generan con los scripts de `tools/` (sin recursos de otros
  juegos ni mods). Si añades recursos de terceros, comprueba su licencia y cítalos aquí.
- Inspiración estética (p. ej., el Eclipse): sin nombres, personajes ni objetos de obras existentes.
