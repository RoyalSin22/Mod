#!/usr/bin/env python3
"""
Genera el modelo de bloque de la Hoguera (V81): un círculo de piedras irregulares, lecho de brasas, troncos cruzados,
la espada clavada con espiral de hierro y tres llamas animadas. Es el mismo formato que exporta Blockbench, así que
se puede abrir y retocar allí (File → Open Model) y volver a guardar sobre `models/block/bonfire.json`.

Uso:  python3 tools/models/gen_bonfire_model.py src/main/resources/assets/aptitudes/models/block [--check]
"""
import json
import random
import sys

STONE, COAL = '#stone', '#coal'


def faces(tex, only=None):
    names = only or ('down', 'up', 'north', 'south', 'west', 'east')
    return {n: {'texture': tex} for n in names}


def box(x0, y0, z0, x1, y1, z1, tex, angle=0, axis='y', origin=None):
    e = {'from': [x0, y0, z0], 'to': [x1, y1, z1], 'faces': faces(tex)}
    if angle:
        e['rotation'] = {'origin': origin or [8, y0, 8], 'axis': axis, 'angle': angle}
    return e


def plane_pair(x0, y0, z0, x1, y1, z1, angle, axis_x):
    """Llama: dos caras opuestas sin sombra con la textura animada."""
    names = ('north', 'south') if axis_x else ('east', 'west')
    e = {'from': [x0, y0, z0], 'to': [x1, y1, z1],
         'faces': {n: {'texture': '#fire', 'uv': [0, 0, 16, 16]} for n in names}, 'shade': False}
    if angle:
        e['rotation'] = {'origin': [8, y0, 8], 'axis': 'y', 'angle': angle}
    return e


def build():
    rng = random.Random(81)
    el = []
    el.append(box(0.5, 0, 0.5, 15.5, 1, 15.5, COAL))                      # lecho de ceniza
    # Anillo de piedras: altura, anchura y giro variables (sin dejar huecos grandes).
    ring = []
    for i in range(14):
        t = i / 14 * 6.2832
        import math
        cx, cz = 8 + 6.4 * math.cos(t), 8 + 6.4 * math.sin(t)
        w, d = rng.uniform(2.6, 3.8), rng.uniform(2.4, 3.6)
        h = rng.choice((2.5, 3, 3.5, 4.5, 5.5))
        ring.append((cx, cz, w, d, h, rng.choice((-22.5, 0, 22.5, 45))))
    for cx, cz, w, d, h, ang in ring:
        x0, x1 = max(0, cx - w / 2), min(16, cx + w / 2)
        z0, z1 = max(0, cz - d / 2), min(16, cz + d / 2)
        el.append(box(round(x0, 2), 0, round(z0, 2), round(x1, 2), h, round(z1, 2), STONE, ang, 'y', [round(cx, 2), 0, round(cz, 2)]))
    el.append(box(3, 1, 3, 13, 2.2, 13, COAL))                           # brasas
    # Troncos cruzados apoyados.
    el.append(box(3, 1.8, 6.6, 13, 3.4, 8.4, '#log', 0))
    el.append(box(6.6, 1.8, 3, 8.4, 3.4, 13, '#log', 0))
    el.append(box(4, 3.2, 7.2, 12, 4.6, 8.4, '#log', 22.5, 'y', [8, 3.2, 8]))
    el.append(box(7.2, 3.2, 4, 8.4, 4.6, 12, '#log', -22.5, 'y', [8, 3.2, 8]))
    # Espada clavada con espiral (hoja, guarda, empuñadura, pomo y tres anillos de la espiral).
    o = [8, 2, 8]
    el.append(box(7.2, 2, 7.5, 8.8, 12.5, 8.5, '#blade', -22.5, 'z', o))
    el.append(box(5.5, 12.5, 7.2, 10.5, 13.6, 8.8, '#blade', -22.5, 'z', o))
    el.append(box(7.5, 13.6, 7.5, 8.5, 15.6, 8.5, '#hilt', -22.5, 'z', o))
    el.append(box(7.2, 15.6, 7.2, 8.8, 16, 8.8, '#blade', -22.5, 'z', o))
    for y in (5, 7.5, 10):
        el.append(box(6.4, y, 7.1, 9.6, y + 0.8, 8.9, '#coil', -22.5, 'z', o))
    # Llamas: tres cruces de planos con alturas y giros distintos.
    el.append(plane_pair(2, 2, 8, 14, 15, 8, 45, True))
    el.append(plane_pair(8, 2, 2, 8, 15, 14, 45, False))
    el.append(plane_pair(4, 2, 8, 12, 11, 8, 0, True))
    el.append(plane_pair(8, 2, 4, 8, 11, 12, 0, False))
    el.append(plane_pair(5, 2, 8, 11, 8, 8, 22.5, True))
    return {
        'parent': 'minecraft:block/block',
        'ambientocclusion': False,
        'textures': {
            'particle': 'aptitudes:block/bonfire_stone', 'stone': 'aptitudes:block/bonfire_stone',
            'coal': 'aptitudes:block/bonfire_coal', 'fire': 'aptitudes:block/bonfire_flame',
            'blade': 'minecraft:block/iron_block', 'hilt': 'minecraft:block/dark_oak_planks',
            'log': 'minecraft:block/dark_oak_log', 'coil': 'minecraft:block/chiseled_stone_bricks',
        },
        'elements': el,
    }


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    path = sys.argv[1].rstrip('/') + '/bonfire.json'
    text = json.dumps(build(), indent=2) + '\n'
    if '--check' in sys.argv:
        try:
            ok = open(path, encoding='utf-8').read() == text
        except OSError:
            ok = False
        print('Hoguera al día' if ok else 'Hoguera desactualizada')
        sys.exit(0 if ok else 1)
    open(path, 'w', encoding='utf-8').write(text)
    print('Hoguera generada:', len(build()['elements']), 'elementos')


if __name__ == '__main__':
    main()
