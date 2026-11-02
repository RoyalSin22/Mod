# Publicar Aptitudes (Modrinth / CurseForge)

Lista de comprobación para publicar una versión. Los pasos marcados **(autor)** requieren tu cuenta o una
decisión tuya.

## 1. Antes de compilar la versión

- [ ] `main` compila (`.\gradlew.bat build`) y las pruebas pasan.
- [ ] Pruebas en partida de las etapas nuevas (`docs/TESTING.md`). Mientras falten, publicar como **Beta**.
- [ ] Número de versión en `gradle.properties` (`mod_version`), con [Versionado Semántico](https://semver.org/lang/es/):
  - versión actual: `1.3.7` (decisión del autor);
  - después: `1.3.8` (correcciones), `1.4.0` (contenido nuevo compatible) y `2.0.0` (cambios que rompen
    mundos guardados o la configuración). Mientras haya etapas sin probar en partida, márcala como **Beta**
    en la página de publicación.
- [ ] Entrada nueva en `CHANGELOG.md` con la fecha.

## 2. Archivo a subir

- `build/libs/aptitudes-<versión>.jar` (no el `-sources.jar`).
- Requisitos que hay que indicar: Minecraft **1.21.1**, Fabric Loader **≥ 0.16.0**, **Fabric API**, Java 21.
- Entorno: necesario en **cliente y servidor** (tiene pantallas, HUD, mixins de cliente y lógica de servidor).

## 3. Página del proyecto (autor)

- [ ] Nombre, resumen corto y descripción (puedes partir de la introducción de `README.md`).
- [ ] Icono: `src/main/resources/assets/aptitudes/icon.png` (128×128, generado con `tools/textures/gen_icon.py`).
- [ ] Capturas: pantalla K, árbol de roles, Hoguera, un combo, el Eclipse con el cielo rojo, la Mesa del gremio.
- [ ] Categorías sugeridas: Aventura, Equipamiento, Mobs, Juego de rol.
- [x] Licencia: MIT (`LICENSE`, `Copyright (c) 2026 RoyalSin22`).
- [x] Enlaces: código fuente e incidencias en `fabric.mod.json` (`github.com/RoyalSin22/mod`, repositorio público).

## 4. Contenido de terceros

- Texturas, sonido e icono son **propios**: se generan con los scripts de `tools/` (sin recursos de otros
  juegos ni mods). Si añades recursos de terceros, comprueba su licencia y cítalos aquí.
- Inspiración estética (p. ej., el Eclipse): sin nombres, personajes ni objetos de obras existentes.
