# Plan v40–v45 (propuesta para aprobar)

Decisiones del autor (2026-11-02):

- Pantalla de configuración **propia + Mod Menu opcional**: se aprueba esta **única excepción** a la regla de
  «no añadir dependencias» (Mod Menu solo para compilar, no obligatorio para jugar).
- Eventos rotativos: **uno al azar cada 10 días**, sin repetir el anterior.
- Progresión: **maestría** y **niveles de amenaza**; la **reforja se pospone** (irá con el Templado en la Hoguera).
- Experiencia de juego: **tutorial guiado**, **configuración del servidor visible en el cliente** y
  **traducciones a portugués (Brasil), francés y alemán**.
- Alquimia del Herbolista.

Exclusiones de siempre: sin linajes, razas, pactos, herencias ni sacrificios. Todos los nombres y recursos son
propios.

## Orden propuesto

| Versión | Contenido | Por qué en este orden |
|---|---|---|
| v40 | Pantalla de configuración + configuración del servidor visible en el cliente | Base para ajustar todo lo que venga después |
| v41 | Eventos rotativos (Eclipse + 2 eventos nuevos) | Reutiliza el sistema del Eclipse |
| v42 | Alquimia del Herbolista | Independiente; contenido rápido |
| v43 | Maestría y niveles de amenaza | Cambia el balance: mejor con la configuración ya visible |
| v44 | Tutorial guiado | Al final, cuando el contenido está estable |
| v45 | Traducciones pt_br, fr_fr, de_de | Al final: traducir una sola vez los textos ya estables |

## v40 — Pantalla de configuración

- **Pantalla propia** (`client/screen/ConfigScreen`) con pestañas: Cliente (HUD, efectos, Eclipse), Juego
  (XP, combos, runas, Eclipse/eventos, élites) y Avanzado (escalado de atributos, topes).
- Se abre con `/aptitudes config`, con un botón en la pantalla K y desde **Mod Menu** si está instalado.
- **Opciones del cliente**: se guardan en `aptitudes-client.json`, como ahora.
- **Opciones del servidor**: solo las puede cambiar un operador (permiso 2). El cliente envía la petición
  (`ConfigEditPayload`, C2S) y **el servidor la valida** con `AptitudesConfig.validated(...)`, la guarda y la
  reenvía a todos. Un jugador sin permiso las ve en solo lectura.
- **Sincronización** (`ConfigSyncPayload`, S2C) al entrar y tras cada cambio: los tooltips (runas, Eclipse)
  muestran los valores reales del servidor y no los de por defecto.
- **Mod Menu**:
  - `modCompileOnly` (no se incluye en el `.jar` ni es obligatorio);
  - repositorio `https://maven.terraformersmc.com/releases/`;
  - la versión para 1.21.1 se fija al implementarlo;
  - entrada `"modmenu"` en `fabric.mod.json`.

  Es el único cambio permitido en `build.gradle`.
- La pantalla cabe en 426×240 o se desplaza (regla de la v39).

## v41 — Eventos rotativos

- `event/WorldEventManager` generaliza el Eclipse.
  - Cada 10 días (desde el día 10) se elige un evento al azar, sin repetir el anterior. El orden se fija con
    una semilla del mundo y el número de evento: es reproducible y no se puede forzar recargando.
  - El estado actual del Eclipse (`aptitudes_eclipse`) se sigue leyendo, para no romper mundos guardados.
- **Niebla de almas** (de 18:00 a 6:00, noche):
  - niebla gris azulada, con el mismo método de tintado que el Eclipse;
  - espectros: criaturas de alma (vex y fantasmas reforzados);
  - las almas que sueltan dan Fragmentos de alma extra.
- **Lluvia de brasas** (de día):
  - caen meteoritos pequeños (sin destruir bloques, con `uniqueGriefing` respetado) que dejan un núcleo
    mineral que se puede recoger;
  - en el Nether, criaturas ígneas.
- Configuración: `worldEventsEnabled`, `worldEventIntervalDays` (10) y un peso por evento (0 = desactivado).
- Comando: `/aptitudes event status|start <evento>|stop`.
- Pruebas puras: `event/WorldEventRulesTest` (elección sin repetir, pesos y calendario).

## v42 — Alquimia del Herbolista

- Recetas de soporte de pociones propias (registro de pociones de 1.21.1):
  - **Maná**: recupera energía;
  - **Frenesí**: +velocidad de ataque;
  - **Velo del eclipse**: −daño de las criaturas de evento;
  - **Vigor del bosque**: regeneración y saturación.
- Ingredientes del mod (pétalos de la Bolsa de hierbas, Fragmentos del eclipse, Esencia de élite).
- El **Herbolista** destila más rápido y sus pociones duran +50 %; gana XP de rol al destilar.
- Grimorio: capítulo «Alquimia» con las recetas leídas del juego.

## v43 — Progresión: maestría y niveles de amenaza

- **Maestría**:
  - al llegar al nivel máximo de una habilidad o de un rol, la XP extra suma **puntos de maestría**
    (curva propia, en una clase pura con pruebas);
  - cada punto da un +0,5 % pequeño a esa habilidad o rol (por ejemplo, Minería → velocidad de minado),
    con un tope (configurable, por defecto +10 %);
  - se ve en la pantalla K (un número dorado en la tarjeta, sin tarjetas nuevas).
- **Niveles de amenaza**:
  - las criaturas hostiles ganan vida y daño según la **distancia al punto de aparición del mundo**, por
    anillos (por defecto, cada 1000 bloques, hasta el nivel V);
  - mejores botines y más probabilidad de élite;
  - se muestra un aviso en el HUD al cambiar de anillo;
  - `threatEnabled` (activado), con tamaño del anillo, tope y multiplicadores configurables;
  - pruebas puras: `logic/ThreatRulesTest`.
- Persistencia: maestría en el attachment de progreso (claves nuevas; los datos antiguos siguen siendo válidos).

## v44 — Tutorial guiado

- El aldeano del gremio (y el grimorio) proponen 6 pasos con recompensa pequeña:
  1. Fabricar o encontrar el grimorio.
  2. Encender una Hoguera y subir un nivel.
  3. Elegir rol principal.
  4. Usar una activa.
  5. Hacer un combo de 5.
  6. Fabricar algo en la Mesa del gremio.
- Se guarda por jugador (attachment `tutorial`). Se puede omitir con `/aptitudes tutorial skip`.
- Aviso discreto en el HUD con el paso actual; se desactiva en la configuración del cliente.

## v45 — Traducciones

- `pt_br`, `fr_fr` y `de_de` a partir de `en_us` (≈2 600 claves cada una).
- Las traduzco yo, por lotes (interfaz y objetos primero; grimorio y nodos después). Para los 200 nodos
  evolucionados, el generador `tools/lang/gen_evolved_lang.py` se amplía con los tres idiomas.
- **Recomendación**: que un hablante nativo revise cada idioma antes de publicar. Se anotará en el CHANGELOG
  como «traducción inicial».

## Pospuesto

- Reforja de armas (afijos) y Templado en la Hoguera (trofeos ganados en combate), juntos más adelante.
- Integraciones con grupos, contratos y logros cuando la v29–v31 de Codex estén en `main`.
