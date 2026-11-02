# Flujo de Git con Codex (app de escritorio) y la terminal

Guía para que el trabajo de Codex llegue siempre a GitHub. Motivo: las tareas v29–v31 se perdieron
porque sus cambios vivían en worktrees de Codex sin commit y las ramas subidas estaban vacías.

## Cómo trabaja la app de Codex

Cada tarea puede ejecutarse en un **worktree**: una carpeta aparte, ligada al repositorio, en
`C:\Users\Johan\.codex\worktrees\<tarea>\Mod`. Los cambios **no** aparecen en tu carpeta principal
(`C:\Users\Johan\Desktop\Gits\MOD\Mod`) ni en GitHub Desktop si este tiene abierta la carpeta principal.

Ver los worktrees (desde la carpeta principal):

```bat
git worktree list
```

## Flujo por versión

1. **Una tarea de Codex = una versión = una rama** (`codex/vNN-tema`).
2. Codex termina y entrega el informe (archivos, resumen, build, mensaje de commit y **ruta del worktree**).
3. **Copia de seguridad** (opcional, recomendable en versiones grandes):

   ```bat
   robocopy "C:\Users\Johan\.codex\worktrees\<tarea>\Mod" "C:\Users\Johan\Desktop\Gits\respaldo-<tarea>" /E /XD .gradle build run
   ```

4. **Compilar y probar** en el worktree:

   ```bat
   cd /d "C:\Users\Johan\.codex\worktrees\<tarea>\Mod"
   gradlew.bat build
   ```

5. Si sale `BUILD SUCCESSFUL`, **commit y push** desde ahí:

   ```bat
   git add -A
   git status
   git commit -m "feat: vNN - tema"
   git push -u origin codex/vNN-tema
   ```

   Si la rama sale como `HEAD detached`, crea una rama nueva antes del commit:
   `git switch -c codex/vNN-tema-trabajo`.

6. **Comprobar** que la rama tiene el commit en GitHub (el hash no debe ser el de `main`):

   ```bat
   git ls-remote origin codex/vNN-tema
   ```

7. En GitHub, pestaña **Actions**: el build de la rama debe tener ✔.
8. **Solo entonces** cierra o archiva la tarea en la app de Codex.

## Errores frecuentes

| Mensaje | Causa | Solución |
|---|---|---|
| `nothing to commit, working tree clean` | Estás en la carpeta principal y el trabajo está en el worktree | Ve a la ruta de `git worktree list` |
| `'<rama>' is already checked out at …` | Git no deja la misma rama abierta en dos carpetas | Usa otra rama (`-trabajo`) o trabaja en la carpeta donde ya está abierta |
| `Unable to create '.git/index.lock'` / `Permission denied` | El entorno aislado de Codex no puede escribir en `.git`, o quedó un bloqueo | Haz el commit tú desde `cmd`; si persiste, cierra Codex y borra `.git\index.lock` |
| `Author identity unknown` | Git sin nombre ni correo | `git config --global user.name "RoyalSin22"` y `git config --global user.email "135181788+RoyalSin22@users.noreply.github.com"` |
| Miles de líneas cambiadas en los JSON de idioma | Alguien reformateó el archivo | No hagas commit: pide que se añadan las claves sin cambiar la sangría (2 espacios) |

## GitHub Desktop

Sirve para **revisar** diferencias e historial. Para ver el trabajo de Codex, añade el worktree con
**File → Add local repository** y elige la carpeta del worktree. **No uses «Discard changes»** en un
worktree de Codex: borra su trabajo. Para commit y push, la terminal es la vía recomendada.

## Formato (lo fijan `.gitattributes` y `.editorconfig`)

- Finales de línea LF en todos los archivos de texto (salvo `.bat`).
- Java y Gradle con tabulación; Python con 4 espacios; JSON de idioma con 2 espacios.
- No se reformatean archivos existentes.
