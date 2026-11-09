# Rediseño visual de jefes (dirección de arte)

Referencia: hoja «Aptitudes — Dirección de arte — Enemigos» (fantasía oscura, estilo voxel de Minecraft, Blockbench +
GeckoLib). Este documento traduce esa hoja a especificaciones de implementación. **Aplicado en v65** (ver secciones 4–6).

## 1. Diferencias con el estado actual (pendientes de decisión)

| Tema | Hoja de arte | Código actual | Estado |
|---|---|---|---|
| Altura del Coloso Sin Rostro | 3,8 b | hitbox 1,6 × 3,8 (`BossEntities`) | Aplicado en v65 por decisión del autor. |
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

## 4. Decisiones del autor (aplicadas en v65)

1. El Coloso Sin Rostro mide **3,8 b** (hitbox 1,6 × 3,8, ojos a 3,4 b). Revisar alcance y arenas en partida.
2. La esfera devuelta es **cian** y sirve de indicador de aturdimiento.
3. Se implementan **geometría y textura** de los cuatro enemigos.

## 5. Implementado (v65)

- **Coloso:** materiales netherita, pizarra, oro oscuro, roca agrietada y energía roja (`ember`); cresta alargada hasta
  ~3,8 b. Hitbox en `BossEntities`.
- **Guardián:** escudo carmesí con cruz dorada (`crimson`) y cuerpo de piedra lisa (`smooth_stone`).
- **Autómata:** piedra tallada (`carved`), bronce, cobre (`copper`), ojo ámbar (`amber`) y dos esferas auxiliares en los
  hombros.
- **Esfera:** ítems propios `automaton_orb` (bronce y núcleo ámbar) y `automaton_orb_deflected` (cian). `AutomatonOrbEntity.getStack()`
  elige según haya sido devuelta. Texturas en `tools/textures/gen_items.py`, nombres en 11 idiomas.
- Generador: `python3 tools/models/gen_boss_models.py src/main/resources/assets/aptitudes` (`--check` correcto).

## 6. Pendiente

1. Validar en partida: altura del Coloso (alcance, columnas, arenas), legibilidad de las piezas del Guardián, ojo del
   Autómata y color cian de la esfera devuelta.
2. Ajustar las texturas si la lectura a distancia no basta (las UV de la textura de 256×256 son pequeñas).
