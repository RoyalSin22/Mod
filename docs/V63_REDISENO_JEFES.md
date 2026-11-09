# Rediseño visual de jefes (dirección de arte)

Referencia: hoja «Aptitudes — Dirección de arte — Enemigos» (fantasía oscura, estilo voxel de Minecraft, Blockbench +
GeckoLib). Este documento traduce esa hoja a especificaciones de implementación. **Aún no está aplicado en código.**

## 1. Diferencias con el estado actual (pendientes de decisión)

| Tema | Hoja de arte | Código actual | Estado |
|---|---|---|---|
| Altura del Coloso Sin Rostro | 3,8 b | hitbox 1,6 × 3,4 (`BossEntities`) | **Discrepancia.** Cambiar el hitbox altera el combate (alcance, colisiones con columnas). Decidir antes de modelar. |
| Guardián Desmontado | 3,0 b | 1,2 × 3,0 | Coincide |
| Autómata Antiguo | 2,8 b | 1,4 × 2,8 | Coincide |
| Esfera del Autómata | normal y devuelta (cian, aturde) | 0,4 × 0,4, devolución ya implementada | Coincide en mecánica; el color devuelto es visual |

## 2. Especificación por enemigo

### Coloso Sin Rostro (`faceless_colossus`)
- **Silueta:** guerrero gigante con casco cerrado (sin rostro), hombros anchos y espadón colosal.
- **Paleta:** netherita (armadura), pizarra (base), oro oscuro (detalles), roca agrietada (ruina) y energía roja
  (interna, que se intensifica en Fase 2).
- **Lectura:** las grietas con energía roja son el indicador de fase; deben verse desde el frente y de tres cuartos.

### Guardián Desmontado (`dismantled_guardian`)
- **Silueta:** armadura vacía y ritual con piezas flotantes y separadas (yelmo, guanteletes, escudo) que orbitan un núcleo.
- **Paleta:** hierro oscuro (piezas), oro (ornamentos), tela ritual carmesí (estandarte), piedra lisa (estructuras) y
  luz arcana (núcleo).
- **Lectura:** la pieza activa (el escudo en la hoja) debe destacar. Es la regla de orden de `PieceBossRules`.

### Autómata Antiguo (`ancient_automaton`)
- **Silueta:** constructo de piedra tallada y bronce, con un gran ojo central de energía ámbar y dos esferas auxiliares
  que flotan a los lados (lanzaderas de orbes).
- **Paleta:** piedra tallada (cuerpo), bronce (placas), cobre oxidado (detalles), engranaje (mecanismo) y ojo cargado
  (energía, ámbar).
- **Lectura:** el ojo debe cambiar de brillo antes del rayo (aviso de 30 t en `beam`).

### Esfera del Autómata (`automaton_orb`)
- **Normal:** esfera mecánica de piedra y bronce con núcleo naranja.
- **Devuelta:** la misma esfera con brillo azul cian. Al impactar aturde al Autómata, así que el cambio de color debe
  ser inmediato al devolverla.

## 3. Paleta de referencia

Los códigos exactos se fijarán al pintar las texturas (256×256). Los nombres de material de la hoja son los que deben
conservar las zonas UV: netherita, pizarra, oro oscuro, roca agrietada, energía roja, hierro oscuro, oro, tela ritual,
piedra lisa, luz arcana, piedra tallada, bronce, cobre oxidado, engranaje y ojo cargado.

## 4. Preguntas abiertas para el autor

1. ¿El Coloso debe medir **3,8 b** (hoja) o mantener **3,4 b** (hitbox actual)? Si cambia, hay que revisar alcance y
   arenas.
2. ¿El color de la esfera devuelta (cian) se mantiene como indicador de aturdimiento?
3. ¿Se implementa primero solo la geometría (generador `tools/models/gen_boss_models.py`) o también las texturas?

## 5. Próximos pasos

1. Resolver la pregunta 1 (altura del Coloso).
2. Actualizar `tools/models/gen_boss_models.py` con los huesos y paletas de la sección 2.
3. Regenerar y verificar con `--check`; pintar texturas por zona.
4. Validar en partida la lectura de fases, piezas, ojo y esferas.
