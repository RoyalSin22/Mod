#!/usr/bin/env python3
"""Modelos, animaciones y texturas de prueba de los jefes animados (v63, C4) para GeckoLib.

Genera, a partir de una descripción de huesos y cubos en este archivo:
  geo/entity/<jefe>.geo.json, animations/entity/<jefe>.animation.json y textures/entity/<jefe>.png.
Son la base jugable; K5 puede sustituirlos por modelos hechos en Blockbench (mismos nombres de hueso y de
animación, ver docs/V63_JEFES.md). Arte original.

Uso: python3 tools/models/gen_boss_models.py src/main/resources/assets/aptitudes [--check] [--preview hoja.png]
"""
import argparse
import json
import random
from io import BytesIO
from pathlib import Path

from PIL import Image

TEX = 128

# ------------------------------------------------------------------ Coloso Sin Rostro
# (hueso, padre, pivote, rotación inicial, [(origen, tamaño, material)])
COLOSSUS_BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('right_leg', 'root', [-5, 24, 0], None, [([-8.5, 0, -4], [7, 24, 8], 'greave')]),
    ('left_leg', 'root', [5, 24, 0], None, [([1.5, 0, -4], [7, 24, 8], 'greave')]),
    ('body', 'root', [0, 24, 0], None, [([-9.5, 24, -5.5], [19, 3, 11], 'leather'),
                                        ([-9, 27, -5], [18, 17, 10], 'plate')]),
    ('head', 'body', [0, 44, 0], None, [([-5, 44, -5], [10, 10, 10], 'helm'),
                                        ([-1, 54, -4], [2, 2, 8], 'crest')]),
    ('right_arm', 'body', [-12, 41, 0], None, [([-17, 37, -5], [8, 7, 10], 'plate'),
                                               ([-16, 21, -3], [6, 16, 6], 'iron')]),
    ('left_arm', 'body', [12, 41, 0], None, [([9, 37, -5], [8, 7, 10], 'plate'),
                                             ([10, 21, -3], [6, 16, 6], 'iron')]),
    # Espadón en la mano derecha, apuntando al frente y abajo.
    ('sword', 'right_arm', [-13, 23, 0], [-60, 0, 0], [([-14, 19, -1], [2, 8, 2], 'leather'),
                                                       ([-16.5, 17, -2], [7, 2, 4], 'iron'),
                                                       ([-15, -9, -1], [4, 26, 2], 'blade')]),
]

MATERIALS = {
    'plate': (58, 61, 72), 'iron': (96, 100, 112), 'greave': (46, 48, 58), 'helm': (70, 73, 84),
    'leather': (92, 64, 44), 'crest': (176, 66, 58), 'blade': (178, 186, 196),
    'brass': (176, 132, 60), 'gauntlet': (150, 124, 62), 'core': (235, 70, 60), 'shield': (84, 96, 120),
}

# Animaciones: {nombre: (duración, bucle, {hueso: {'rotation'|'position': {t: [x, y, z]}}})}
COLOSSUS_ANIMS = {
    'idle': (3.0, True, {
        'body': {'rotation': {0: [0, 0, 0], 1.5: [2, 0, 0], 3.0: [0, 0, 0]}},
        'head': {'rotation': {0: [0, 0, 0], 1.5: [-2, 0, 0], 3.0: [0, 0, 0]}},
        'left_arm': {'rotation': {0: [0, 0, 0], 1.5: [0, 0, -3], 3.0: [0, 0, 0]}},
    }),
    'walk': (1.2, True, {
        'right_leg': {'rotation': {0: [25, 0, 0], 0.6: [-25, 0, 0], 1.2: [25, 0, 0]}},
        'left_leg': {'rotation': {0: [-25, 0, 0], 0.6: [25, 0, 0], 1.2: [-25, 0, 0]}},
        'left_arm': {'rotation': {0: [15, 0, 0], 0.6: [-15, 0, 0], 1.2: [15, 0, 0]}},
        'body': {'position': {0: [0, 0, 0], 0.3: [0, -0.6, 0], 0.6: [0, 0, 0], 0.9: [0, -0.6, 0], 1.2: [0, 0, 0]}},
    }),
    # Tajo: carga 0,9 s (18 ticks) y golpe.
    'sweep': (1.4, False, {
        'right_arm': {'rotation': {0: [0, 0, 0], 0.9: [-80, 70, 0], 1.05: [-85, -80, 0], 1.4: [0, 0, 0]}},
        'body': {'rotation': {0: [0, 0, 0], 0.9: [0, 30, 0], 1.05: [0, -30, 0], 1.4: [0, 0, 0]}},
    }),
    # Golpe descendente: carga 1,4 s (28 ticks).
    'slam': (2.0, False, {
        'right_arm': {'rotation': {0: [0, 0, 0], 1.4: [-170, 0, 0], 1.55: [-20, 0, 0], 2.0: [0, 0, 0]}},
        'left_arm': {'rotation': {0: [0, 0, 0], 1.4: [-170, 0, 0], 1.55: [-20, 0, 0], 2.0: [0, 0, 0]}},
        'body': {'rotation': {0: [0, 0, 0], 1.4: [-10, 0, 0], 1.55: [20, 0, 0], 2.0: [0, 0, 0]}},
    }),
    # Embestida: se agacha 0,8 s (16 ticks) y sale disparado.
    'charge': (1.4, False, {
        'body': {'rotation': {0: [0, 0, 0], 0.8: [25, 0, 0], 1.4: [0, 0, 0]}},
        'right_arm': {'rotation': {0: [0, 0, 0], 0.8: [-40, 0, 0], 1.4: [0, 0, 0]}},
        'head': {'rotation': {0: [0, 0, 0], 0.8: [-20, 0, 0], 1.4: [0, 0, 0]}},
    }),
    # Cambio de fase: sacudida (la armadura se agrieta).
    'phase_two': (1.5, False, {
        'body': {'rotation': {0: [0, 0, 0], 0.25: [0, 0, 6], 0.5: [0, 0, -6], 0.75: [0, 0, 5],
                              1.0: [0, 0, -4], 1.5: [0, 0, 0]}},
        'head': {'rotation': {0: [0, 0, 0], 0.5: [-25, 0, 0], 1.5: [0, 0, 0]}},
    }),
}

# ------------------------------------------------------------------ Guardián Desmontado
# Piezas: yelmo (cabeza), escudo (brazo izquierdo) y guanteletes (ambos brazos). Solo el núcleo (pecho) brilla.
GUARDIAN_BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('left_leg', 'root', [2, 12, 0], None, [([-0.5, 0, -2.5], [4, 12, 5], 'iron')]),
    ('right_leg', 'root', [-2, 12, 0], None, [([-3.5, 0, -2.5], [4, 12, 5], 'iron')]),
    ('body', 'root', [0, 12, 0], None, [([-5, 12, -3], [10, 12, 6], 'plate'),
                                        ([-4, 21, -2.5], [8, 4, 5], 'brass'),
                                        ([-1.5, 14, -3.4], [3, 3, 1], 'core')]),
    ('head', 'body', [0, 24, 0], None, [([-3, 24, -3], [6, 8, 6], 'helm'),
                                        ([-0.5, 30, -3.5], [1, 3, 1], 'core')]),
    ('left_arm', 'body', [5, 22, 0], None, [([5, 12, -1.5], [3, 12, 3], 'iron'),
                                            ([4, 18, -4], [5, 8, 1.5], 'shield')]),
    ('right_arm', 'body', [-5, 22, 0], None, [([-8, 12, -1.5], [3, 12, 3], 'iron'),
                                              ([-9, 11, -1.5], [4, 4, 3], 'gauntlet')]),
]

GUARDIAN_ANIMS = {
    'idle': (3.0, True, {
        'body': {'rotation': {0: [0, 0, 0], 1.5: [1, 0, 0], 3.0: [0, 0, 0]}},
        'head': {'rotation': {0: [0, 0, 0], 1.5: [-3, 0, 0], 3.0: [0, 0, 0]}},
    }),
    'walk': (1.2, True, {
        'right_leg': {'rotation': {0: [25, 0, 0], 0.6: [-25, 0, 0], 1.2: [25, 0, 0]}},
        'left_leg': {'rotation': {0: [-25, 0, 0], 0.6: [25, 0, 0], 1.2: [-25, 0, 0]}},
    }),
    # Tajo a distancia: el brazo con el guantelete adelanta 0,9 s y golpea.
    'volley': (1.5, False, {
        'right_arm': {'rotation': {0: [0, 0, 0], 0.9: [-100, 0, 0], 1.1: [-100, 0, 0], 1.5: [0, 0, 0]}},
    }),
    # Giro del anillo: el cuerpo da una vuelta mientras se carga (1,2 s).
    'ring': (1.7, False, {
        'body': {'rotation': {0: [0, 0, 0], 1.2: [0, 360, 0], 1.7: [0, 0, 0]}},
    }),
    # Caída sobre el objetivo: el cuerpo se inclina hacia delante.
    'dive': (1.8, False, {
        'body': {'rotation': {0: [0, 0, 0], 1.0: [60, 0, 0], 1.3: [60, 0, 0], 1.8: [0, 0, 0]}},
    }),
    'phase_two': (1.5, False, {
        'head': {'rotation': {0: [0, 0, 0], 0.25: [0, 25, 0], 0.5: [0, -25, 0], 0.75: [0, 20, 0], 1.5: [0, 0, 0]}},
    }),
}

# ------------------------------------------------------------------ Autómata Antiguo
AUTOMATON_BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('left_leg', 'root', [2.5, 12, 0], None, [([0.5, 0, -2], [4, 12, 4], 'iron')]),
    ('right_leg', 'root', [-2.5, 12, 0], None, [([-4.5, 0, -2], [4, 12, 4], 'iron')]),
    ('body', 'root', [0, 12, 0], None, [([-5, 12, -4], [10, 12, 8], 'plate'),
                                        ([-3, 22, -3], [6, 4, 6], 'brass')]),
    ('head', 'body', [0, 24, 0], None, [([-3, 24, -3], [6, 6, 6], 'plate'),
                                        ([-1, 27, -3.5], [2, 2, 1], 'core')]),
    ('left_arm', 'body', [5, 22, 0], None, [([5, 14, -1.5], [4, 10, 3], 'iron')]),
    ('right_arm', 'body', [-5, 22, 0], None, [([-9, 14, -1.5], [4, 10, 3], 'iron'),
                                              ([-9, 8, -2], [4, 4, 4], 'brass')]),
]

AUTOMATON_ANIMS = {
    'idle': (3.0, True, {
        'head': {'rotation': {0: [0, 0, 0], 1.5: [0, 25, 0], 3.0: [0, 0, 0]}},
    }),
    'walk': (1.2, True, {
        'right_leg': {'rotation': {0: [25, 0, 0], 0.6: [-25, 0, 0], 1.2: [25, 0, 0]}},
        'left_leg': {'rotation': {0: [-25, 0, 0], 0.6: [25, 0, 0], 1.2: [-25, 0, 0]}},
    }),
    # Esfera: el brazo derecho se levanta 1 s (20 ticks) y suelta.
    'orb': (1.5, False, {
        'right_arm': {'rotation': {0: [0, 0, 0], 1.0: [-120, 0, 0], 1.2: [-120, 0, 0], 1.5: [0, 0, 0]}},
    }),
    # Pisotón: ambos brazos caen (0,9 s) y se levantan.
    'stomp': (1.6, False, {
        'left_arm': {'rotation': {0: [0, 0, 0], 0.9: [-150, 0, 0], 1.1: [-150, 0, 0], 1.6: [0, 0, 0]}},
        'right_arm': {'rotation': {0: [0, 0, 0], 0.9: [-150, 0, 0], 1.1: [-150, 0, 0], 1.6: [0, 0, 0]}},
        'body': {'position': {0: [0, 0, 0], 0.9: [0, -1, 0], 1.6: [0, 0, 0]}},
    }),
    # Rayo: el torso gira (1,5 s) apuntando.
    'beam': (2.2, False, {
        'body': {'rotation': {0: [0, 0, 0], 0.6: [0, -30, 0], 1.5: [0, 30, 0], 2.2: [0, 0, 0]}},
        'right_arm': {'rotation': {0: [0, 0, 0], 0.6: [-90, 0, 0], 1.8: [-90, 0, 0], 2.2: [0, 0, 0]}},
    }),
    'phase_two': (1.5, False, {
        'body': {'rotation': {0: [0, 0, 0], 0.25: [0, 0, 6], 0.5: [0, 0, -6], 0.75: [0, 0, 5], 1.5: [0, 0, 0]}},
    }),
}

BOSSES = {
    'faceless_colossus': (COLOSSUS_BONES, COLOSSUS_ANIMS),
    'dismantled_guardian': (GUARDIAN_BONES, GUARDIAN_ANIMS),
    'ancient_automaton': (AUTOMATON_BONES, AUTOMATON_ANIMS),
}


def pack(bones):
    """Reparte las UV en «estantes» de la textura (cada cubo usa el desplegado estándar de Bedrock)."""
    x = y = shelf = 0
    out = []
    for name, *_ , cubes in bones:
        for origin, size, mat in cubes:
            w, h, d = (int(round(v)) for v in size)
            uw, uh = 2 * (d + w), d + h
            if x + uw > TEX:
                x, y, shelf = 0, y + shelf, 0
            if y + uh > TEX:
                raise SystemExit(f'La textura de {TEX}×{TEX} no basta para {name}')
            out.append((name, origin, size, mat, [x, y]))
            x += uw
            shelf = max(shelf, uh)
    return out


def geometry(boss, bones):
    cubes = pack(bones)
    by_bone = {}
    for name, origin, size, mat, uv in cubes:
        by_bone.setdefault(name, []).append({'origin': origin, 'size': size, 'uv': uv})
    out_bones = []
    for name, parent, pivot, rotation, _ in bones:
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
            'description': {'identifier': f'geometry.{boss}', 'texture_width': TEX, 'texture_height': TEX,
                            'visible_bounds_width': 5, 'visible_bounds_height': 5, 'visible_bounds_offset': [0, 2, 0]},
            'bones': out_bones,
        }],
    }


def fmt_time(t):
    return f'{float(t):.2f}'.rstrip('0').rstrip('.') if t else '0.0'


def animations(anims):
    out = {}
    for name, (length, loop, bones) in anims.items():
        data = {'animation_length': length, 'bones': {}}
        if loop:
            data['loop'] = True
        for bone, channels in bones.items():
            data['bones'][bone] = {ch: {fmt_time(t): v for t, v in keys.items()} for ch, keys in channels.items()}
        out[name] = data
    return {'format_version': '1.8.0', 'animations': out}


def texture(boss, bones):
    rnd = random.Random('tex-' + boss)
    img = Image.new('RGBA', (TEX, TEX), (0, 0, 0, 0))
    px = img.load()
    for name, origin, size, mat, (u, v) in pack(bones):
        w, h, d = (int(round(s)) for s in size)
        base = MATERIALS[mat]
        faces = [  # (x, y, ancho, alto, luz)
            (u + d, v, w, d, 1.18), (u + d + w, v, w, d, 0.7),
            (u, v + d, d, h, 0.85), (u + d, v + d, w, h, 1.0),
            (u + d + w, v + d, d, h, 0.85), (u + 2 * d + w, v + d, w, h, 0.9),
        ]
        for fx, fy, fw, fh, light in faces:
            for yy in range(fh):
                for xx in range(fw):
                    n = rnd.randint(-6, 6)
                    edge = xx in (0, fw - 1) or yy in (0, fh - 1)
                    k = light * (0.82 if edge else 1.0)
                    c = [max(0, min(255, int(ch * k) + n)) for ch in base]
                    if mat in ('plate', 'greave', 'helm') and (xx + 2 * yy) % 7 == 0 and not edge:
                        c = [min(255, ch + 22) for ch in c]  # remaches
                    if mat == 'blade' and xx in (0, fw - 1):
                        c = [200, 70, 60]  # filo rojizo
                    px[fx + xx, fy + yy] = tuple(c) + (255,)
    buf = BytesIO()
    img.save(buf, format='PNG', optimize=False)
    return buf.getvalue(), img


def files(assets):
    out = {}
    for boss, (bones, anims) in BOSSES.items():
        out[assets / f'geo/entity/{boss}.geo.json'] = (json.dumps(geometry(boss, bones), indent=1) + '\n').encode()
        out[assets / f'animations/entity/{boss}.animation.json'] = (json.dumps(animations(anims), indent=1) + '\n').encode()
        out[assets / f'textures/entity/{boss}.png'] = texture(boss, bones)[0]
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('assets', type=Path)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--preview', type=Path)
    args = parser.parse_args()
    stale = []
    for path, data in files(args.assets).items():
        if args.check:
            if not path.is_file() or path.read_bytes() != data:
                stale.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
    if args.preview:
        texture('faceless_colossus', COLOSSUS_BONES)[1].resize((TEX * 4, TEX * 4), Image.NEAREST).save(args.preview)
    if stale:
        raise SystemExit('Recursos de jefes ausentes o desactualizados:\n' + '\n'.join(stale))
    print(f'{len(BOSSES)} jefe(s): modelo, animaciones y textura {"comprobados" if args.check else "generados"}')


if __name__ == '__main__':
    main()
