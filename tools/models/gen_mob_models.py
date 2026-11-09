#!/usr/bin/env python3
"""v65: enemigo nuevo de la Niebla de almas, el Osario Quebrado (GeckoLib, 64×64).

Genera geometría, animaciones y una textura de prueba (colores planos por pieza). Uso:
  python3 tools/models/gen_mob_models.py src/main/resources/assets/aptitudes            # escribe
  python3 tools/models/gen_mob_models.py src/main/resources/assets/aptitudes --check    # compara
"""
import argparse
import json
import sys
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw

TEX = 64
NAME = 'osario_quebrado'

BONE_COLOR = '#B7B5A6'
EARTH = '#4A3B2E'
ECTO = '#4DE2A6'
SKULL = '#D9D6C3'

# (nombre, padre, pivote, rotación, [(origen, tamaño, uv, color)])
BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('body', 'root', [0, 14, 0], None, [
        ([-4, 14, -2], [8, 10, 4], [0, 0], BONE_COLOR),
        ([-1.5, 18, -2.5], [3, 3, 1], [36, 0], ECTO),
    ]),
    ('head', 'body', [0, 24, 0], None, [
        ([-3.5, 22, -3.5], [7, 7, 7], [0, 16], SKULL),
    ]),
    ('arm_right', 'body', [-4, 22, 0], None, [
        ([-6, 12, -1.5], [2, 12, 2], [24, 0], BONE_COLOR),
    ]),
    ('arm_left_dragging', 'body', [4, 22, 0], None, [
        ([4, 0, -2], [3, 22, 3], [40, 0], EARTH),
        ([5, 0, 2], [4, 2, 2], [40, 30], BONE_COLOR),
    ]),
    ('leg_left', 'root', [2, 12, 0], None, [
        ([1, 0, -1.5], [3, 12, 3], [0, 40], BONE_COLOR),
    ]),
    ('leg_right', 'root', [-2, 12, 0], None, [
        ([-4, 0, -1.5], [3, 12, 3], [12, 40], BONE_COLOR),
    ]),
]

# Animaciones: duración (s), bucle, huesos → canal → {tiempo: [x, y, z]}
ANIMS = {
    'idle': (2.0, True, {
        'body': {'rotation': {0: [0, 0, 0], 1.0: [2, 0, 0], 2.0: [0, 0, 0]}},
        'arm_right': {'rotation': {0: [0, 0, 0], 1.0: [-6, 0, 0], 2.0: [0, 0, 0]}},
    }),
    'walk': (1.0, True, {
        'leg_left': {'rotation': {0: [-20, 0, 0], 0.5: [20, 0, 0], 1.0: [-20, 0, 0]}},
        'leg_right': {'rotation': {0: [20, 0, 0], 0.5: [-20, 0, 0], 1.0: [20, 0, 0]}},
    }),
    # Barrido del brazo largo (tiempo de aviso aproximado 18 ticks + 10).
    'heavy_swipe': (1.4, False, {
        'body': {'rotation': {0: [0, 0, 0], 0.6: [0, 0, -18], 0.9: [0, 0, 16], 1.4: [0, 0, 0]}},
        'arm_left_dragging': {'rotation': {0: [0, 0, 0], 0.6: [-40, 0, 0], 0.9: [30, 0, 0], 1.4: [0, 0, 0]}},
    }),
}


def pack(bones):
    """Cubos de todos los huesos, con el color de cada uno."""
    out = []
    for name, _, _, _, cubes in bones:
        for origin, size, uv, color in cubes:
            out.append((name, origin, size, uv, color))
    return out


def geometry():
    by_bone = {}
    for name, origin, size, uv, _ in pack(BONES):
        by_bone.setdefault(name, []).append({'origin': origin, 'size': size, 'uv': uv})
    out_bones = []
    for name, parent, pivot, rotation, _ in BONES:
        bone = {'name': name, 'pivot': pivot}
        if parent:
            bone['parent'] = parent
        if rotation:
            bone['rotation'] = rotation
        if name in by_bone:
            bone['cubes'] = by_bone[name]
        out_bones.append(bone)
    return {
        'format_version': '1.12.0',
        'minecraft:geometry': [{
            'description': {'identifier': f'geometry.{NAME}', 'texture_width': TEX, 'texture_height': TEX,
                            'visible_bounds_width': 3, 'visible_bounds_height': 3, 'visible_bounds_offset': [0, 1, 0]},
            'bones': out_bones,
        }],
    }


def animations():
    out = {}
    for name, (length, loop, bones) in ANIMS.items():
        data = {'animation_length': length, 'bones': {}}
        if loop:
            data['loop'] = True
        for bone, channels in bones.items():
            data['bones'][bone] = {ch: {f'{t:.2f}'.rstrip('0').rstrip('.') or '0': v for t, v in keys.items()}
                                   for ch, keys in channels.items()}
        out[f'animation.{NAME}.{name}'] = data
    return {'format_version': '1.8.0', 'animations': out}


def texture():
    img = Image.new('RGBA', (TEX, TEX), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for _, _, _, uv, color in pack(BONES):
        u, v = uv
        draw.rectangle([u, v, u + 15, v + 15], fill=color)
    buf = BytesIO()
    img.save(buf, format='PNG', optimize=False)
    return buf.getvalue()


def files(assets):
    return {
        assets / f'geo/entity/{NAME}.geo.json': (json.dumps(geometry(), indent=1) + '\n').encode(),
        assets / f'animations/entity/{NAME}.animation.json': (json.dumps(animations(), indent=1) + '\n').encode(),
        assets / f'textures/entity/{NAME}.png': texture(),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('assets', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    wanted = files(args.assets)
    if args.check:
        bad = [str(p) for p, data in wanted.items() if not p.exists() or p.read_bytes() != data]
        if bad:
            print('Distinto del generador:', *bad, sep='\n  ')
            return 1
        print('1 mob: modelo, animaciones y textura comprobados')
        return 0
    for path, data in wanted.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    print('1 mob: modelo, animaciones y textura generados')
    return 0


if __name__ == '__main__':
    sys.exit(main())
