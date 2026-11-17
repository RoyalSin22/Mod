# V80 — Armadura Ascendida

**Decisión del autor:** una única armadura, mejora de la netherita, que sustituye a las 20 armaduras de rol (borrado completo,
mundos de prueba) y mejora las habilidades del rol elegido. Se obtiene en un bloque de mejora (la mesa de herrería).

## Obtención
- Receta `smithing_transform` por pieza: **Plantilla de ascensión** + pieza de netherita + **Corazón de campeón** → pieza Ascendida
  (conserva encantamientos). Cuatro piezas = 4 plantillas + 4 corazones.
- La plantilla: Vale de armería (canje; aldeano del gremio nivel 3 por 32 esmeraldas + 4 fragmentos de alma, contratos, cofres del
  Custodio y la cripta), cofres de torres y campamentos (peso 2–8 en lugar de las antiguas piezas de rol) y 10 % de los campeones.

## Efecto
| Fuente | Efecto |
|---|---|
| Material | Defensa 4/7/9/4, dureza 4, retroceso 0,15, durabilidad ×42, encantabilidad 20 |
| Set completo (común) | −10 % de coste de maná/energía, −10 % de esperas, +10 % de daño no directo (habilidades, magia), +4 % cuerpo a cuerpo |
| Set completo + rol principal | Los 20 efectos por rol de `RoleArmorSets` (Bersérker +10 % daño, Mago −25 % maná, etc.) |

Reglas puras en `item/AscendedRules` (JUnit). Los efectos por rol se evalúan contra el **rol principal** (antes: el del set puesto).

## Visual
Textura única de netherita violeta con oro brasa y runas del Velo (`tools/textures/gen_ascended_armor.py`, `--check`). No hay
variación visible por rol (el modelo vanilla no permite teñir por jugador sin renderer propio); el rol se muestra en el aviso al
activarse y en las partículas. Ampliable a modelo 3D con renderer propio.

## Pendiente
- Revisión en partida (equilibrio de las cifras comunes) y, si se quiere, modelo 3D propio.
- Mejora de armas y otros objetos en el mismo bloque («armas, etc.»): fuera de este alcance.
