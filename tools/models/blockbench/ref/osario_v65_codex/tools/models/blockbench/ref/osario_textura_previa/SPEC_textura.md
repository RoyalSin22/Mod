# Encargo del autor: textura del Osario Quebrado (texto literal)

Actúa como artista de texturas pixel art para Minecraft Java 1.21.1 / GeckoLib / Blockbench.
Tu tarea es texturizar el modelo del enemigo "Osario Quebrado" del mod Aptitudes.

## Alcance
Trabaja solo el texturizado, no cambies la geometría ni los nombres de huesos.
Debes pintar sobre la plantilla UV regenerada del modelo actual, no sobre un PNG suelto ni sobre la plantilla vieja.

## Reglas generales
* Resolución de textura: 256×256 px.
* La plantilla del proyecto usa 2 px por unidad de modelo. Referencia: 1 bloque = 16 unidades = 32 px.
* Antes de pintar, regenerar la plantilla UV, porque la geometría del brazo y de las piernas cambió y las UV antiguas ya no coinciden.
* Pintar sobre la plantilla UV actual del proyecto.
* Mantener estilo pixel art tipo Minecraft, con sombreado por píxel, clusters limpios y buena lectura a distancia.
* No usar degradados suaves tipo aerógrafo ni ruido excesivo.
* El musgo no va en la cara ni en las órbitas.
* El brillo solo va en: ojos, pecho, brazo arrastrado.
* Lo ideal es generar dos mapas: 1. textura base / albedo; 2. textura de brillo separada (glowmask), con el resto en negro o transparente según el pipeline.
* El PNG antiguo de 64×64 debe guardarse solo como respaldo fuera del repo.

## Huesos y tamaños aproximados (unidades)
* `head`: `h_skull`, `h_eye_l`, `h_eye_r` — cráneo 12×10×12; ojos 2×2 (brillo)
* `chest`: `b_chest_up`, `b_spine`, `b_core` — pecho 14×6×10; columna 3×4×2; núcleo 5×5×1 (brillo)
* `body`: `b_torso` — torso bajo 14×10×10
* `arm_right`: `r_upper`, `r_fore`, `r_hand` — 5×14×5; 4×12×4; 3×6×3
* `arm_left_dragging` (cadena `drag_*`): `l_upper`, `l_elbow`, `l_fore`, `l_hand`, `l_tip` — 8×12×8; 7×10×7; 6×8×6; 4×4×4; 2×4×3
  * el brazo completo mide aprox. 38 unidades de largo
  * la punta (`l_tip`) lleva brillo
  * puede haber pequeñas grietas o venas ectoplásmicas en `l_fore`, pero no brillo masivo en toda la masa de tierra
* `leg_left` / `leg_right`: muslo, espinilla, pie — 4×8×4; 4×9×4; 6×3×8

## Paleta base
* Hueso: claro `#D9D6C3`, envejecido `#B7B5A6`, sombra `#8F8B7C`
* Tierra / raíz / oscuro: tierra `#4A3B2E`, raíz o madera `#6B4E3A`, oscuro `#2F221F`
* Musgo: `#4A5F35`, `#5C7142`, `#3B4D28`
* Ectoplasma: `#4DE2A6`, núcleo `#7FFFD4`

## Dirección visual
El Osario Quebrado debe leerse como: 1. esqueleto encorvado, 2. pecho con ectoplasma verde brillante,
3. brazo izquierdo arrastrado, pesado y corrupto, cubierto de tierra, raíces y huesos al descubierto.

## Jerarquía visual
Prioridad de atención: 1. pecho, 2. brazo arrastrado, 3. cráneo.

## Reglas por parte
* Cabeza: hueso envejecido con grietas finas y sombras en juntas; ojos con brillo; sin musgo en la cara ni en las órbitas.
* Pecho: costillas y columna bien legibles; núcleo brillante en el centro; leve contaminación verdosa en costillas cercanas al núcleo.
* Torso bajo: más discreto que el pecho; desgaste y suciedad leves.
* Brazo derecho: brazo funcional de hueso más limpio; algo de desgaste, pero sin protagonismo.
* Brazo izquierdo arrastrado: debe ser la zona más compleja tras el pecho; mezcla de tierra, raíces, madera podrida y hueso expuesto;
  la punta debe leerse claramente; brillo solo en la punta y, si conviene, en pequeñas grietas ectoplásmicas localizadas;
  no convertir toda la masa del brazo en una superficie brillante.
* Piernas: hueso desgastado y claro; suciedad más concentrada en tibias y pies; mantener buena lectura.
* Musgo: usarlo como acento; puede ir en raíces, hombro del brazo arrastrado y zonas húmedas; no debe dominar el modelo.

## Reglas de pixel art
* sombreado por bloques de color;
* highlights de 1–2 píxeles;
* grietas finas de 1 píxel;
* sombras más oscuras en juntas e interiores;
* evitar ruido aleatorio excesivo;
* cada cara debe leerse bien incluso a distancia media.

## Glowmask
Generar un mapa de brillo separado. Solo deben aparecer en el glowmask: ojos, núcleo del pecho, punta del brazo arrastrado,
opcionalmente pequeñas grietas ectoplásmicas del antebrazo arrastrado. Todo lo demás debe quedar en negro o transparente
según el sistema de render del proyecto.

## Validación antes de exportar
Antes de exportar o sustituir archivos, mostrar: 1. UV completa 256×256, 2. preview frontal, 3. preview lateral, 4. preview 3/4,
5. preview con glow activado, 6. preview sin glow, 7. paleta final usada. Esperar aprobación antes de exportar.

---

# Notas del pintor (contexto para quien revise)

* La geometría no se puede tocar. En el modelo, por decisión del autor, el brazo largo y grueso (`arm_left_dragging`) queda a la
  IZQUIERDA de la pantalla en la vista frontal, y el brazo fino (`arm_right`) a la derecha.
* El cubo del núcleo (`b_core`) está sobre la cara frontal del torso bajo (`b_torso`), justo debajo del pecho; el pecho se inclina 15° hacia delante.
* Paleta usada: las 11 de la paleta base más 2 derivadas que NO brillan: `#2E9E78` (ectoplasma apagado: halo y bordes) y
  `#A9C4AD` (hueso teñido de verde).
* Pipeline de brillo del proyecto: GeckoLib `AutoGlowingGeoLayer` con `<nombre>_glowmask.png`; en el mod el glowmask es transparente
  salvo los píxeles que brillan, que llevan el mismo color que la textura base.
* La textura se genera con `gen_osario_texture.py` (pinta cara a cara sobre `uv_layout.json`, volcado de las UV de Blockbench).
  En cajas con UV de caja, las cuatro caras laterales van seguidas en la textura: este, norte (frente), oeste, sur (espalda).
  En la cara este, u=0 es la espalda y u máximo el frente; la oeste es al revés.
