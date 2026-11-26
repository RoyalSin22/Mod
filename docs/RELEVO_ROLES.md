# Relevo: efectos de batalla de los 20 roles y plan de identidad (V77 → V100)

Documento para retomar el trabajo en otro chat o con Codex sin perder contexto. Fecha: 2026-11-25. Léelo junto con `AGENTS.md`.

## 1. Dónde está todo

- **Rama de trabajo:** `claude/aptitudes-lore-audit-htenjq` (todo subido; último commit `c9821ca`). Árbol limpio.
- **`main`** está en `b2359b2`; la rama lo contiene y lleva **41 commits por delante**. **No hay PR abierto ni nada integrado en `main`**
  (`AGENTS.md`: nunca abrir PR ni fusionar sin autorización explícita del autor).
- **Verificación local en este momento:** `./gradlew build` correcto con **915 pruebas**, `check_lang.py`, `gen_interface_lang.py --check` y
  `gen_player_animations.py --check` correctos. GitHub Actions no se puede usar (minutos agotados): se verifica en local.

## 2. Qué se hizo

| Versión | Contenido |
|---|---|
| V77–V78 (+78.1) | Efectos de batalla del Bersérker y Player Animation Library (PAL) para animar el cuerpo en primera persona. Probado por el autor: «las animaciones están bien». |
| V83 | Mago |
| V84 | Asesino |
| V85 | Herbolista |
| V86 | Ingeniero |
| V87 | Lancero, Espadachín, Paladín |
| V88 | Nigromante, Druida, Mercader táctico, Monje |
| V89 | Mago pistolero, Invocador de armas, Cambiaformas |
| V90 | Cazador, Bardo, Brujo abisal, Domador |
| V91 | Elementalista (un estilo por cada uno de los 14 elementos) |
| V93 (1.ª entrega) | Bus de eventos de combate y telemetría (`CombatEvents`, `CombatTelemetry`, `/aptitudes telemetry`) |

**Todo lo de V83–V91 está implementado pero sin ver en partida.** El autor solo ha probado el Bersérker. Las animaciones y sonidos
(sintetizados; solo medidos con duración, pico y espectro, nunca oídos) pueden necesitar ajustes.

Receta para añadir efectos a un rol nuevo o a más activas: `docs/V77_EFECTOS_BATALLA.md` (una sección por fase, con tablas de cada rol).

## 3. Qué falta, en orden

El plan completo, con hallazgos y decisiones, está en `docs/PLAN_IDENTIDAD_ROLES.md`. Resumen del orden:

1. **V92 – Validación y accesibilidad** *(bloqueada por el autor)*: el autor pasa la hoja de pruebas en partida a los 20 roles; después, separar
   las opciones de cliente (partículas, temblor, FOV, viñeteado, volumen, efectos de otros jugadores, alto contraste) y distinguir
   señales funcionales de adorno. Hoy solo existe `fxIntensity`.
2. **V93 (resto):** curación efectiva y esmeraldas consumidas en la telemetría; migrar los `*Kit` al bus; persistir si hace falta.
3. ~~**V94 – Presupuesto de invocaciones**~~ **Hecha el 2026-11-26** (ver `PLAN_IDENTIDAD_ROLES.md` §7): `summonBudget` y daño autónomo atribuido.
4. ~~**V95 – Feedback compartido**~~ **Hecha el 2026-11-26** (ver `PLAN_IDENTIDAD_ROLES.md` §8). Pendiente de ella: «recurso completo» y «definitiva lista» (HUD).
5. **V96 – Efectos (1.ª tanda hecha el 2026-11-26: firmas y definitivas de los 5 pilotos; piedras angulares y los 20 roles hechos; evoluciones y nodos extra hechos; falta revisarlos en partida) para firmas, definitivas y piedras angulares**, y luego evoluciones y los 10 nodos extra. Hoy solo las activas base tienen
   guion propio; el resto usa el efecto genérico (`CastFx`).
6. **V97 – Posturas de combate** (piloto con Bersérker, Lancero, Monje y Mago).
7. **V98 – Identidad mecánica en pilotos** (Bersérker, Elementalista, Cambiaformas, Paladín, Ingeniero): primero un inventario de los recursos
   que **ya existen** como nodos N1–N10; no crear 20 barras nuevas.
8. **V99 – Equilibrio** con datos.
9. **V100 – Activas en PvP** (empieza **después de V93 y con datos**).

Pendientes pequeños detectados:
- Activas con efecto genérico a propósito: Propulsores y Torreta (Ingeniero), Espinas venenosas (Herbolista), Embestida (Lancero).
- Verificar en el código la pregunta 2 de ChatGPT: qué puede usar el rol secundario (pasivas, firma, evoluciones).
- `PlayerAnimIds.ALL` se corrigió en V90; mantenerlo al día al añadir animaciones (la prueba lo exige).

## 4. Decisiones del autor (vinculantes)

1. **Todos los roles deben estar bien equilibrados en solitario**, también los de apoyo.
2. **PvP siempre activo** y **las activas SÍ pueden golpear a jugadores (opción B)**. Hoy no lo hacen (`Targeting.isHostileImpactTarget`
   excluye `PlayerEntity`). La opción de PvP con activas nace **activada** y la protección tras reaparecer es para **todos los jugadores**.
   Debe poder apagarse en caliente desde la pantalla de configuración. Diseño en `docs/PLAN_IDENTIDAD_ROLES.md` §5.
3. **Las esmeraldas siguen siendo economía real** (el Mercader gasta esmeraldas de verdad). Ninguna habilidad puede generar esmeraldas.
4. **El combate vanilla debe seguir siendo competitivo**: la línea base `VANILLA` de la telemetría sirve para comprobarlo.
5. Exclusiones absolutas de `AGENTS.md` (nada de linajes, razas, pactos ni ascendencia) también para las ideas de ChatGPT.

## 5. Cosas que conviene saber (aprendidas en esta sesión)

- **Casillas de activas:** 4 en total, 2 por rol (`slot < 2 ? principal : secundario`).
- **Misma categoría de rol principal y secundario:** permitida y con bonus de XP (`SPECIALIZATION_XP_BONUS`).
- **Cada activa pasa por `AbilityManager.cast...`**; ahí viven el coste, el alcance de combate (V93) y `CastFx.onCast`. Las activas con guion
  propio se registran en `CastFx.onCast(…, abilityId)` (lista `*Fx.handles`), para no duplicar el efecto genérico.
- **Efectos de batalla:** un evento por efecto (`FxEvents` → `FxEventPayload` con ids de `FxIds`, hoy hasta el 63), el cliente dibuja en
  `client/fx/*ClientFx`, y el sonido cae en el tick de impacto de la animación (`PlayerAnimIds.IMPACT_TICKS`, que debe coincidir con
  `IMPACT_TICKS` de `tools/models/gen_player_animations.py`).
- **Animaciones PAL:** se generan con `tools/models/gen_player_animations.py` (no editar los JSON); en primera persona hace falta el modo
  `THIRD_PERSON_MODEL` (ya cableado en `PlayerAnims`).
- **Sonidos:** `tools/audio/gen_sounds.py <ruta> [nombres]` (se pueden regenerar solo algunos); registrar en `ModSounds`, `sounds.json`,
  los 11 idiomas (subtítulos `subtitles.aptitudes.*`) y `tools/lang/ui_catalog.py`; luego `gen_interface_lang.py`.
- **Idiomas:** añadir las claves sin reformatear; pt/fr/de salen de `ui_catalog.py` con `gen_interface_lang.py`.
- **Hay un `LivingEntityMixin`** que ya modifica el daño entrante (`RolePassives.modifyOutgoingDamage`…): es el sitio previsto para escalar el
  daño de habilidad contra jugadores en V100, sin mixin nuevo. Hace falta marcar el «daño de habilidad» (el alcance de V93 sirve de base).
- **Los estados de los kits están en memoria** y se pierden al reiniciar el servidor (ya documentado en cada `*Kit`).
- **Un error recurrente a evitar:** al escribir scripts que modifican archivos, comprobar que cada cambio se aplicó (una parada a media
  ejecución dejó una vez la documentación sin una sección).

## 6. Cómo retomar

**Comprobación inicial** (en el clon del autor, rama `claude/aptitudes-lore-audit-htenjq`):

```
./gradlew build
python3 tools/lang/check_lang.py
python3 tools/lang/gen_interface_lang.py --check
python3 tools/models/gen_player_animations.py src/main/resources/assets/aptitudes --check
```

**Prompt para pegar en el nuevo chat o en Codex:**

> Lee `AGENTS.md` y `docs/RELEVO_ROLES.md`, y después `docs/PLAN_IDENTIDAD_ROLES.md`. Trabajamos en la rama
> `claude/aptitudes-lore-audit-htenjq` (no abras PR ni fusiones sin mi permiso). Los efectos de batalla de los 20 roles (V83–V91) están
> implementados pero sin probar en partida; la telemetría de combate (V93) está hecha en su primera entrega. Respeta las decisiones de
> §4 de `RELEVO_ROLES.md`. Primero ejecuta la comprobación inicial y dime el resultado real. Después continúa con
> **[V94 / V92 / lo que yo indique]**, una versión por tarea, con pruebas y documentación (`CHANGELOG.md`, `AGENTS.md`, el plan).
> No digas que algo funciona sin haberlo ejecutado y avisa de lo que no se pueda verificar sin el juego.

**Si el autor ya probó roles:** pegar sus resultados (P0–P3 por rol) al principio del nuevo chat y empezar por corregir lo crítico antes de
añadir nada nuevo.

## 7. Documentos relacionados

| Documento | Para qué |
|---|---|
| `docs/V77_EFECTOS_BATALLA.md` | Diseño y tabla de efectos, animaciones y sonidos por rol |
| `docs/PLAN_IDENTIDAD_ROLES.md` | Auditoría de ChatGPT cruzada con el código, versiones V92–V100, decisiones, estado de V93 |
| `docs/PROMPT_BRAINSTORM_ROLES.md` | Prompt usado para el brainstorming con ChatGPT |
| `docs/PROMPT_ANIMACIONES_CODEX.md` | Encargo para delegar animaciones de otros roles a Codex |
| `AGENTS.md` | Reglas del proyecto, política de Git, tabla de sistemas existentes |
