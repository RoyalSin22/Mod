#!/usr/bin/env python3
"""v65: enemigos nuevos con modelo GeckoLib (Osario Quebrado, Errante Velado y Custodio de las Cuatro Llaves).

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

TELA = '#34383D'
HUESO = '#B7B5A6'
TIERRA = '#4A3B2E'
ECTO = '#4DE2A6'
ALMA = '#65D6D0'
SKULL = '#D9D6C3'
ARENISCA = '#C9B88A'
COBRE = '#5FA59A'
LUZ = '#FFF3C4'
ESCORIA = '#5A5550'
HIERRO = '#3A3D48'
FUNDIDO = '#FF8A3D'

# (nombre, padre, pivote, [(origen, tamaño, uv, color)])
OSARIO = {
    'name': 'osario_quebrado', 'tex': 64,
    'bounds': (3, 1),
    'bones': [
        ('root', None, [0, 0, 0], []),
        ('body', 'root', [0, 14, 0], [
            ([-4, 14, -2], [8, 10, 4], [0, 0], HUESO),
            ([-1.5, 18, -2.5], [3, 3, 1], [36, 0], ECTO),
        ]),
        ('head', 'body', [0, 24, 0], [([-3.5, 22, -3.5], [7, 7, 7], [0, 16], SKULL)]),
        ('arm_right', 'body', [-4, 22, 0], [([-6, 12, -1.5], [2, 12, 2], [24, 0], HUESO)]),
        ('arm_left_dragging', 'body', [4, 22, 0], [
            ([4, 0, -2], [3, 22, 3], [40, 0], TIERRA),
            ([5, 0, 2], [4, 2, 2], [40, 30], HUESO),
        ]),
        ('leg_left', 'root', [2, 12, 0], [([1, 0, -1.5], [3, 12, 3], [0, 40], HUESO)]),
        ('leg_right', 'root', [-2, 12, 0], [([-4, 0, -1.5], [3, 12, 3], [12, 40], HUESO)]),
    ],
    'anims': {
        'idle': (2.0, True, {
            'body': {'rotation': {0: [0, 0, 0], 1.0: [2, 0, 0], 2.0: [0, 0, 0]}},
            'arm_right': {'rotation': {0: [0, 0, 0], 1.0: [-6, 0, 0], 2.0: [0, 0, 0]}},
        }),
        'walk': (1.0, True, {
            'leg_left': {'rotation': {0: [-20, 0, 0], 0.5: [20, 0, 0], 1.0: [-20, 0, 0]}},
            'leg_right': {'rotation': {0: [20, 0, 0], 0.5: [-20, 0, 0], 1.0: [20, 0, 0]}},
        }),
        'heavy_swipe': (1.4, False, {
            'body': {'rotation': {0: [0, 0, 0], 0.6: [0, 0, -18], 0.9: [0, 0, 16], 1.4: [0, 0, 0]}},
            'arm_left_dragging': {'rotation': {0: [0, 0, 0], 0.6: [-40, 0, 0], 0.9: [30, 0, 0], 1.4: [0, 0, 0]}},
        }),
    },
}

ERRANTE = {
    'name': 'errante_velado', 'tex': 64,
    'bounds': (3, 2),
    'bones': [
        ('root', None, [0, 0, 0], []),
        ('body', 'root', [0, 14, 0], [
            ([-3.5, 14, -2], [7, 10, 4], [0, 0], TELA),
            ([-1.5, 18, -2.2], [3, 3, 1], [30, 0], ALMA),
        ]),
        ('head', 'body', [0, 24, 0], [([-3, 22, -3], [6, 6, 6], [0, 16], TELA)]),
        ('veil', 'head', [0, 24, -3], [([-4, 21, -4.5], [8, 11, 1], [0, 30], TELA)]),
        ('arm_left', 'body', [3.5, 22, 0], [([3, 10, -1.5], [2, 12, 2], [24, 0], HUESO)]),
        ('arm_right', 'body', [-3.5, 22, 0], [([-5, 8, -1.5], [2, 14, 2], [24, 30], HUESO)]),
        ('leg_left', 'root', [2, 12, 0], [([1, 0, -1.25], [2.5, 12, 2.5], [0, 40], TELA)]),
        ('leg_right', 'root', [-2, 12, 0], [([-3.5, 0, -1.25], [2.5, 12, 2.5], [12, 40], TELA)]),
    ],
    'anims': {
        'idle': (2.0, True, {
            'body': {'rotation': {0: [0, 0, 0], 1.0: [3, 0, 0], 2.0: [0, 0, 0]}},
            'veil': {'rotation': {0: [0, 0, 0], 1.0: [6, 0, 0], 2.0: [0, 0, 0]}},
        }),
        'walk': (1.0, True, {
            'leg_left': {'rotation': {0: [-22, 0, 0], 0.5: [22, 0, 0], 1.0: [-22, 0, 0]}},
            'leg_right': {'rotation': {0: [22, 0, 0], 0.5: [-22, 0, 0], 1.0: [22, 0, 0]}},
        }),
        # Rasgado espectral (aviso ~18 ticks + 10): el torso se echa atrás y el brazo largo barre.
        'rasgado': (1.4, False, {
            'body': {'rotation': {0: [0, 0, 0], 0.6: [-10, 0, 0], 0.9: [14, 0, 0], 1.4: [0, 0, 0]}},
            'arm_right': {'rotation': {0: [0, 0, 0], 0.6: [-60, 0, 0], 0.9: [70, 0, 0], 1.4: [0, 0, 0]}},
        }),
    },
}

ESCORIFICADOR = {
    'name': 'escorificador_hierro', 'tex': 64,
    'bounds': (3, 2),
    'bones': [
        ('root', None, [0, 0, 0], []),
        ('body', 'root', [0, 12, 0], [
            ([-4, 12, -2.5], [8, 7, 5], [0, 0], ESCORIA),
            ([-5, 19, -3], [10, 5, 6], [0, 14], HIERRO),
            ([-2, 24, 1], [4, 6, 3], [46, 22], ESCORIA),
        ]),
        ('grille', 'body', [0, 15.5, -3], [([-2.5, 14, -3.2], [5, 3, 1], [34, 0], FUNDIDO)]),
        ('head', 'body', [0, 24, 0], [([-3.5, 24, -3], [7, 7, 6], [0, 27], HIERRO)]),
        ('arm_left', 'body', [5, 22, 0], [
            ([4, 12, -1.5], [3, 10, 3], [34, 8], HIERRO),
            ([4, 8, -1.5], [3, 3, 3], [48, 8], FUNDIDO),
        ]),
        ('arm_right', 'body', [-5, 22, 0], [
            ([-7, 12, -1.5], [3, 10, 3], [34, 22], HIERRO),
            ([-7, 8, -1.5], [3, 3, 3], [48, 16], FUNDIDO),
        ]),
        ('leg_left', 'root', [2, 12, 0], [([1, 0, -1.5], [3, 12, 3], [0, 36], ESCORIA)]),
        ('leg_right', 'root', [-2, 12, 0], [([-4, 0, -1.5], [3, 12, 3], [12, 36], ESCORIA)]),
    ],
    'anims': {
        'idle': (2.0, True, {
            'body': {'rotation': {0: [0, 0, 0], 1.0: [2, 0, 0], 2.0: [0, 0, 0]}},
        }),
        'walk': (1.0, True, {
            'leg_left': {'rotation': {0: [-20, 0, 0], 0.5: [20, 0, 0], 1.0: [-20, 0, 0]}},
            'leg_right': {'rotation': {0: [20, 0, 0], 0.5: [-20, 0, 0], 1.0: [20, 0, 0]}},
        }),
        # Golpe aplastante: la rejilla se enciende (señal) y los brazos caen de golpe.
        'slam': (1.5, False, {
            'grille': {'scale': {0: [1, 1, 1], 0.5: [1.35, 1.35, 1.35], 1.0: [1.35, 1.35, 1.35], 1.5: [1, 1, 1]}},
            'arm_left': {'rotation': {0: [0, 0, 0], 0.7: [-150, 0, 0], 0.9: [-150, 0, 0], 1.1: [0, 0, 0]}},
            'arm_right': {'rotation': {0: [0, 0, 0], 0.7: [-150, 0, 0], 0.9: [-150, 0, 0], 1.1: [0, 0, 0]}},
        }),
    },
}

CUSTODIO = {
    'name': 'custodio_cuatro_llaves', 'tex': 128,
    'bounds': (4, 3),
    'bones': [
        ('root', None, [0, 0, 0], []),
        ('body', 'root', [0, 12, 0], [
            ([-6, 12, -3], [12, 12, 6], [0, 0], ARENISCA),
            ([-2, 16, -3.6], [4, 4, 1], [60, 0], LUZ),
            ([-1.5, 20.5, -3.9], [3, 2, 1], [0, 40], COBRE),
            ([-1.5, 14, -3.9], [3, 2, 1], [24, 40], COBRE),
            ([2, 16.5, -3.9], [1, 3, 1], [48, 40], COBRE),
            ([-3, 16.5, -3.9], [1, 3, 1], [60, 40], COBRE),
        ]),
        ('head', 'body', [0, 24, 0], [
            ([-4, 24, -4], [8, 7, 8], [0, 64], ARENISCA),
            ([-2.5, 31, -2.5], [5, 4, 5], [40, 64], COBRE),
            ([-3, 26.5, -4.05], [2, 1, 0.5], [60, 64], LUZ),
            ([1, 26.5, -4.05], [2, 1, 0.5], [60, 66], LUZ),
        ]),
        ('arm_left', 'body', [6, 20, 0], [([6, 8, -2], [3, 12, 4], [0, 96], ARENISCA)]),
        ('arm_right', 'body', [-6, 20, 0], [([-9, 8, -2], [3, 12, 4], [24, 96], ARENISCA)]),
        ('leg_left', 'root', [3, 12, 0], [([1, 0, -2], [4, 12, 4], [48, 96], ARENISCA)]),
        ('leg_right', 'root', [-3, 12, 0], [([-5, 0, -2], [4, 12, 4], [64, 96], ARENISCA)]),
    ],
    'anims': {
        'idle': (2.0, True, {
            'body': {'rotation': {0: [0, 0, 0], 1.0: [0, 3, 0], 2.0: [0, 0, 0]}},
        }),
        'walk': (1.2, True, {
            'leg_left': {'rotation': {0: [-12, 0, 0], 0.6: [12, 0, 0], 1.2: [-12, 0, 0]}},
            'leg_right': {'rotation': {0: [12, 0, 0], 0.6: [-12, 0, 0], 1.2: [12, 0, 0]}},
        }),
        # Golpe en cruz (aviso ~20 ticks + 10): los brazos se cruzan y se abren.
        'keys_turn': (1.5, False, {
            'arm_left': {'rotation': {0: [0, 0, 0], 0.7: [40, 0, -30], 1.0: [-30, 0, 20], 1.5: [0, 0, 0]}},
            'arm_right': {'rotation': {0: [0, 0, 0], 0.7: [40, 0, 30], 1.0: [-30, 0, -20], 1.5: [0, 0, 0]}},
        }),
    },
}

SPECS = [OSARIO, ERRANTE, CUSTODIO, ESCORIFICADOR]


def geometry(spec):
    by_bone = {}
    for name, _, _, cubes in spec['bones']:
        for origin, size, uv, _ in cubes:
            by_bone.setdefault(name, []).append({'origin': origin, 'size': size, 'uv': uv})
    out_bones = []
    for name, parent, pivot, _ in spec['bones']:
        bone = {'name': name, 'pivot': pivot}
        if parent:
            bone['parent'] = parent
        if name in by_bone:
            bone['cubes'] = by_bone[name]
        out_bones.append(bone)
    tex = spec['tex']
    w, h = spec['bounds']
    return {
        'format_version': '1.12.0',
        'minecraft:geometry': [{
            'description': {'identifier': f"geometry.{spec['name']}", 'texture_width': tex, 'texture_height': tex,
                            'visible_bounds_width': w, 'visible_bounds_height': h, 'visible_bounds_offset': [0, 1, 0]},
            'bones': out_bones,
        }],
    }


def animations(spec):
    out = {}
    for name, (length, loop, bones) in spec['anims'].items():
        data = {'animation_length': length, 'bones': {}}
        if loop:
            data['loop'] = True
        for bone, channels in bones.items():
            data['bones'][bone] = {ch: {f'{t:.2f}'.rstrip('0').rstrip('.') or '0': v for t, v in keys.items()}
                                   for ch, keys in channels.items()}
        out[f"animation.{spec['name']}.{name}"] = data
    return {'format_version': '1.8.0', 'animations': out}


def texture(spec):
    tex = spec['tex']
    img = Image.new('RGBA', (tex, tex), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for _, _, _, cubes in spec['bones']:
        for origin, size, uv, color in cubes:
            u, v = uv
            w = int(2 * (size[0] + size[2]))
            h = int(size[1] + size[2])
            draw.rectangle([u, v, min(tex - 1, u + w - 1), min(tex - 1, v + h - 1)], fill=color)
    buf = BytesIO()
    img.save(buf, format='PNG', optimize=False)
    return buf.getvalue()


def files(assets):
    out = {}
    for spec in SPECS:
        name = spec['name']
        out[assets / f'geo/entity/{name}.geo.json'] = (json.dumps(geometry(spec), indent=1) + '\n').encode()
        out[assets / f'animations/entity/{name}.animation.json'] = (json.dumps(animations(spec), indent=1) + '\n').encode()
        out[assets / f'textures/entity/{name}.png'] = texture(spec)
    return out


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
        print(f'{len(SPECS)} mobs: modelos, animaciones y texturas comprobados')
        return 0
    for path, data in wanted.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    print(f'{len(SPECS)} mobs: modelos, animaciones y texturas generados')
    return 0


if __name__ == '__main__':
    sys.exit(main())
