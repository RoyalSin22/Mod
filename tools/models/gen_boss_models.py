#!/usr/bin/env python3
"""Modelos, animaciones y texturas de los jefes animados (v63, GeckoLib).

Genera, a partir de las descripciones de este archivo:
  geo/entity/<jefe>.geo.json, animations/entity/<jefe>.animation.json y textures/entity/<jefe>.png.
Las coordenadas de cada cubo son absolutas en el modelo (píxeles de 1/16 de bloque, origen en los pies). Los
huesos y las animaciones son el contrato de docs/V63_JEFES.md: K5 puede sustituir los modelos por otros
hechos en Blockbench con los mismos nombres de hueso y de animación. Arte original.

Uso: python3 tools/models/gen_boss_models.py src/main/resources/assets/aptitudes [--check] [--preview hoja.png]
"""
import argparse
import json
import random
from io import BytesIO
from pathlib import Path

from PIL import Image

TEX = 256

MATERIALS = {
    'plate': (60, 63, 76), 'iron': (98, 102, 116), 'greave': (48, 50, 62), 'helm': (72, 75, 88),
    'dark': (30, 32, 40), 'leather': (96, 66, 46), 'cloth': (100, 30, 38), 'bone': (214, 204, 176),
    'spike': (122, 118, 130), 'brass': (182, 138, 64), 'gauntlet': (154, 128, 66), 'shield': (84, 98, 124),
    'core': (240, 74, 62), 'crest': (178, 68, 60), 'blade': (182, 190, 200),
    # Rediseño v65 (hoja de arte): Coloso, Guardián y Autómata.
    'netherite': (58, 54, 68), 'slate': (78, 84, 94), 'dark_gold': (150, 116, 56),
    'cracked': (104, 100, 96), 'ember': (255, 84, 48), 'smooth_stone': (156, 156, 152),
    'crimson': (128, 30, 42), 'carved': (128, 124, 116), 'bronze': (158, 108, 56),
    'copper': (96, 160, 144), 'amber': (255, 166, 52),
}

# Materiales con relieve en la textura: remaches (plate, iron, helm…), tejido (cloth), rejilla (dark), etc.
RIVETED = {'plate', 'iron', 'helm', 'greave', 'shield', 'gauntlet', 'netherite'}
# Materiales que brillan desde dentro: el centro es más intenso y el borde se aclara.
CORE_LIKE = {'core', 'ember', 'amber'}


def box(x, y, z, w, h, d, mat):
    """Cubo por su esquina mínima y su tamaño (formato de (origen, tamaño, material))."""
    return ([x, y, z], [w, h, d], mat)


# ------------------------------------------------------------------ Coloso Sin Rostro
# Gigante con armadura de placas, capa, hombreras con púas, casco sin rostro y espadón.
COLOSSUS_BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('right_leg', 'root', [-5, 24, 0], None, [
        box(-9, 0, -4, 8, 22, 8, 'netherite'), box(-9.5, 0, -4.5, 9, 3, 9, 'slate'),
        box(-8.5, 15, -4.6, 7, 4, 1, 'netherite'), box(-8, 4, -4.6, 6, 6, 1, 'cloth')]),
    ('left_leg', 'root', [5, 24, 0], None, [
        box(1, 0, -4, 8, 22, 8, 'netherite'), box(0.5, 0, -4.5, 9, 3, 9, 'slate'),
        box(1.5, 15, -4.6, 7, 4, 1, 'netherite'), box(2, 4, -4.6, 6, 6, 1, 'cloth')]),
    ('body', 'root', [0, 24, 0], None, [
        box(-9.5, 24, -5.5, 19, 3, 11, 'leather'), box(-9, 27, -5, 18, 17, 10, 'netherite'),
        box(-6, 31, -5.6, 12, 2, 1, 'cracked'), box(-1.5, 34, -5.7, 3, 3, 1, 'ember'),
        box(-1, 24, -5.7, 2, 2, 1, 'dark_gold'), box(-6, 42, -3, 12, 2, 6, 'cracked')]),
    ('cape', 'body', [0, 40, 5], None, [box(-7, 20, 5, 14, 22, 1, 'cloth'), box(-7, 20, 5.8, 3, 3, 0.4, 'slate')]),
    ('head', 'body', [0, 44, 0], None, [
        box(-5, 44, -5, 10, 10, 10, 'netherite'), box(-4, 47, -5.4, 8, 2, 0.6, 'slate'),
        box(-0.5, 52, -4, 1, 9, 8, 'crest'), box(-7, 50, -2, 2, 4, 2, 'bone'), box(5, 50, -2, 2, 4, 2, 'bone')]),
    ('right_arm', 'body', [-12, 41, 0], None, [
        box(-17, 37, -5, 8, 7, 10, 'netherite'), box(-18, 35, -5.5, 10, 2, 11, 'cracked'),
        box(-18, 44, -3, 2, 3, 2, 'spike'), box(-14, 45, -3, 2, 3, 2, 'spike'), box(-10, 44, -3, 2, 3, 2, 'spike'),
        box(-16, 21, -3, 6, 16, 6, 'cracked'), box(-16.5, 15, -3.5, 7, 6, 7, 'gauntlet')]),
    ('left_arm', 'body', [12, 41, 0], None, [
        box(9, 37, -5, 8, 7, 10, 'netherite'), box(8, 35, -5.5, 10, 2, 11, 'cracked'),
        box(16, 44, -3, 2, 3, 2, 'spike'), box(12, 45, -3, 2, 3, 2, 'spike'), box(8, 44, -3, 2, 3, 2, 'spike'),
        box(10, 21, -3, 6, 16, 6, 'cracked'), box(9, 17, -4, 8, 8, 7, 'shield'), box(9.5, 15, -3, 7, 4, 5, 'gauntlet')]),
    # Espadón en la mano derecha, apuntando al frente y abajo.
    ('sword', 'right_arm', [-13, 23, 0], [-60, 0, 0], [
        box(-14, 19, -1, 2, 8, 2, 'leather'), box(-16.5, 17, -2, 7, 2, 4, 'cracked'),
        box(-14.5, 16, -1.5, 3, 2, 3, 'dark_gold'), box(-15, -9, -1, 4, 26, 2, 'blade'),
        box(-14.5, -8, -1.6, 1, 24, 0.4, 'cracked')]),
]

COLOSSUS_ANIMS = {
    'idle': (3.0, True, {
        'body': {'rotation': {0: [0, 0, 0], 1.5: [2, 0, 0], 3.0: [0, 0, 0]}},
        'head': {'rotation': {0: [0, 0, 0], 1.5: [-2, 0, 0], 3.0: [0, 0, 0]}},
        'left_arm': {'rotation': {0: [0, 0, 0], 1.5: [0, 0, -3], 3.0: [0, 0, 0]}},
        'cape': {'rotation': {0: [0, 0, 0], 1.5: [4, 0, 0], 3.0: [0, 0, 0]}},
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
# Armadura vacía con las piezas separadas: yelmo, escudo y guanteletes; el pecho muestra el núcleo.
GUARDIAN_BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('left_leg', 'root', [2, 12, 0], None, [
        box(-0.5, 0, -2.5, 4, 12, 5, 'iron'), box(-1, 0, -3, 5, 3, 6, 'dark'), box(-0.5, 6, -2.8, 4, 3, 1, 'plate')]),
    ('right_leg', 'root', [-2, 12, 0], None, [
        box(-3.5, 0, -2.5, 4, 12, 5, 'iron'), box(-4, 0, -3, 5, 3, 6, 'dark'), box(-3.5, 6, -2.8, 4, 3, 1, 'plate')]),
    ('body', 'root', [0, 12, 0], None, [
        box(-5, 12, -3, 10, 4, 6, 'smooth_stone'), box(-4, 16, -2, 8, 1, 4, 'dark'), box(-5, 17, -3, 10, 7, 6, 'smooth_stone'),
        box(-4, 21, -2.5, 8, 4, 5, 'brass'), box(-1.5, 14, -3.4, 3, 3, 1, 'core'), box(-3, 22, -3.4, 6, 1, 1, 'iron'),
        box(-6, 12, -1, 1, 8, 1, 'iron'), box(5, 12, 1, 1, 8, 1, 'iron')]),
    ('head', 'body', [0, 24, 0], None, [
        box(-3, 24, -3, 6, 8, 6, 'helm'), box(-2, 27, -3.4, 4, 2, 0.6, 'dark'),
        box(-0.5, 30, -3.5, 1, 3, 1, 'core'), box(-3.5, 31, -2, 1, 2, 4, 'brass'), box(2.5, 31, -2, 1, 2, 4, 'brass')]),
    ('left_arm', 'body', [5, 22, 0], None, [
        box(5, 12, -1.5, 3, 12, 3, 'iron'), box(4, 18, -4, 5, 8, 1.5, 'crimson'), box(5, 16, -3, 4, 4, 4, 'crimson'),
        box(5.5, 21, -4.2, 2, 2, 0.4, 'core')]),
    ('right_arm', 'body', [-5, 22, 0], None, [
        box(-8, 12, -1.5, 3, 12, 3, 'iron'), box(-9, 11, -1.5, 4, 4, 3, 'gauntlet'),
        box(-10, 8, -1, 1, 4, 1, 'gauntlet'), box(-8.5, 8, -1, 1, 4, 1, 'gauntlet'), box(-7.2, 8, -1, 1, 4, 1, 'gauntlet')]),
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
# Constructo de bronce y piedra: pistones en las piernas, anillos de unión, lente en el pecho y cañón en el brazo.
AUTOMATON_BONES = [
    ('root', None, [0, 0, 0], None, []),
    ('left_leg', 'root', [2.5, 12, 0], None, [
        box(0.5, 0, -2, 4, 12, 4, 'copper'), box(1, 4, -2.6, 3, 8, 1, 'bronze'), box(0, 0, -3, 6, 2, 6, 'dark')]),
    ('right_leg', 'root', [-2.5, 12, 0], None, [
        box(-4.5, 0, -2, 4, 12, 4, 'copper'), box(-4, 4, -2.6, 3, 8, 1, 'bronze'), box(-5, 0, -3, 6, 2, 6, 'dark')]),
    ('body', 'root', [0, 12, 0], None, [
        box(-5, 12, -4, 10, 12, 8, 'carved'), box(-3, 22, -3, 6, 4, 6, 'bronze'),
        box(-5.5, 14, -4.2, 11, 1, 0.6, 'bronze'), box(-5.5, 20, -4.2, 11, 1, 0.6, 'bronze'),
        box(-3, 16, -4.3, 6, 3, 0.5, 'dark'), box(-1, 17, -4.5, 2, 2, 0.5, 'amber'),
        box(6, 25, -1, 3, 3, 3, 'amber'), box(-9, 25, -1, 3, 3, 3, 'amber')]),
    ('head', 'body', [0, 24, 0], None, [
        box(-3, 24, -3, 6, 6, 6, 'carved'), box(-1.5, 26, -3.4, 3, 3, 1, 'amber'), box(-2, 25.5, -3.6, 4, 4, 0.4, 'bronze'),
        box(-0.5, 30, -0.5, 1, 4, 1, 'bronze')]),
    ('left_arm', 'body', [5, 22, 0], None, [
        box(5, 14, -1.5, 4, 10, 3, 'copper'), box(4, 21, -2, 5, 4, 4, 'bronze'), box(5, 9, -2, 4, 5, 4, 'carved')]),
    ('right_arm', 'body', [-5, 22, 0], None, [
        box(-9, 14, -1.5, 4, 10, 3, 'copper'), box(-9, 21, -2, 5, 4, 4, 'bronze'),
        box(-11, 11, -3, 3, 3, 8, 'dark'), box(-11.5, 11.5, -4, 4, 4, 1, 'bronze'), box(-9, 8, -2, 4, 4, 4, 'bronze')]),
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


def detail(mat, xx, yy, fw, fh, edge, base, rnd):
    """Detalle por material: remaches, tejido, rejilla, veta, núcleo luminoso y puntadas."""
    if mat in RIVETED and not edge and xx % 4 == 2 and yy % 4 == 2:
        return 30  # remache
    if mat == 'cloth' and (xx + yy) % 2 == 0:
        return -8  # tejido
    if mat == 'dark' and xx % 3 == 0:
        return -10  # rejilla
    if mat in ('brass', 'bronze') and yy % 3 == 0:
        return 26  # estría
    if mat == 'cracked' and (xx * 3 + yy * 7) % 11 == 0:
        return -18  # grieta
    if mat == 'leather' and xx % 5 == 0 and not edge:
        return -14  # puntada
    if mat == 'bone' and rnd.random() < 0.06:
        return -22  # grieta
    if mat in CORE_LIKE:
        cx, cy = (fw - 1) / 2, (fh - 1) / 2
        d = ((xx - cx) ** 2 + (yy - cy) ** 2) ** 0.5 / max(1, (cx + cy) / 2)
        return int(60 * (1 - min(1, d)))  # gradiente: el centro brilla más
    if mat in ('shield', 'crimson') and (abs(xx - (fw - 1) / 2) < 1 or abs(yy - (fh - 1) / 2) < 1):
        return 22  # cruz del escudo
    if mat == 'blade' and xx in (0, fw - 1):
        return 0  # filo (el color se cambia abajo)
    return 0


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
                    n = rnd.randint(-5, 5)
                    edge = xx in (0, fw - 1) or yy in (0, fh - 1)
                    k = light * (0.8 if edge else 1.0)
                    extra = detail(mat, xx, yy, fw, fh, edge, base, rnd)
                    c = [max(0, min(255, int(ch * k) + n + extra)) for ch in base]
                    if mat == 'blade' and xx in (0, fw - 1):
                        c = [200, 70, 60]  # filo rojizo
                    if mat in CORE_LIKE and edge:
                        c = [min(255, ch + 40) for ch in c]  # aro brillante alrededor del núcleo
                    px[fx + xx, fy + yy] = tuple(c) + (255,)
    buf = BytesIO()
    img.save(buf, format='PNG', optimize=False)
    return buf.getvalue(), img


# B6: aturdimiento por postura (todos los jefes). Un disparo de 2 s: la cabeza se sacude y el cuerpo se hunde; el
# aturdimiento dura 5 s y después el jefe vuelve a su idle. Huesos comunes a los tres modelos: body y head.
STUN_ANIM = (2.0, False, {
    'body': {'rotation': {0: [0, 0, 0], 0.5: [12, 0, 0], 1.5: [12, 0, 0], 2.0: [0, 0, 0]}},
    'head': {'rotation': {0: [0, 0, 0], 0.4: [-30, 0, 0], 0.8: [-10, 0, 0], 1.2: [-30, 0, 0], 2.0: [0, 0, 0]}},
})


def files(assets):
    out = {}
    for boss, (bones, anims) in BOSSES.items():
        anims = {**anims, 'stun': STUN_ANIM}
        out[assets / f'geo/entity/{boss}.geo.json'] = (json.dumps(geometry(boss, bones), indent=1) + '\n').encode()
        out[assets / f'animations/entity/{boss}.animation.json'] = (json.dumps(animations(anims), indent=1) + '\n').encode()
        out[assets / f'textures/entity/{boss}.png'] = texture(boss, bones)[0]
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('assets', type=Path)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--preview', type=Path, help='Hoja de texturas de los tres jefes (solo para revisar)')
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
        sheet = Image.new('RGBA', (TEX * 3, TEX), (40, 42, 54, 255))
        for i, (boss, (bones, _)) in enumerate(BOSSES.items()):
            sheet.paste(texture(boss, bones)[1], (i * TEX, 0), texture(boss, bones)[1])
        sheet.resize((sheet.width * 2, sheet.height * 2), Image.NEAREST).save(args.preview)
    if stale:
        raise SystemExit('Recursos de jefes ausentes o desactualizados:\n' + '\n'.join(stale))
    print(f'{len(BOSSES)} jefe(s): modelo, animaciones y textura {"comprobados" if args.check else "generados"}')


if __name__ == '__main__':
    main()
