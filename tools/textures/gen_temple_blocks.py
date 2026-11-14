#!/usr/bin/env python3
"""Texturas de los bloques del Templo sellado (v64, K6): piedra sellada, Sello y Relicario; T3: Puerta del Custodio; T4: puertas de las cámaras (16×16, arte original).

Uso: python3 tools/textures/gen_temple_blocks.py src/main/resources/assets/aptitudes/textures/block [--check]
"""
import argparse
import math
import random
from io import BytesIO
from pathlib import Path

from PIL import Image

TEAL = (95, 179, 179)
GLOW = (180, 255, 240)
GOLD = (224, 180, 58)
GOLD_DARK = (150, 110, 35)


def stone(seed, base=(46, 52, 60)):
    """Sillares oscuros con juntas y ruido determinista."""
    rnd = random.Random(seed)
    img = Image.new('RGBA', (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            n = rnd.randint(-7, 7)
            joint = y in (0, 8) or (x in (0, 8) if y < 8 else x in (4, 12))
            c = tuple(max(0, min(255, v + n - (18 if joint else 0))) for v in base)
            px[x, y] = c + (255,)
    return img


def sealed_stone():
    img = stone('sealed')
    px = img.load()
    # Runa vertical luminosa y dos trazos.
    for y in range(3, 13):
        px[7, y] = TEAL + (255,)
        px[8, y] = GLOW + (255,) if y in (5, 10) else TEAL + (255,)
    for x in range(4, 12):
        if x not in (7, 8):
            px[x, 7] = TEAL + (255,)
    for x, y in ((5, 4), (10, 4), (5, 11), (10, 11)):
        px[x, y] = GLOW + (255,)
    return img


def seal_top():
    img = stone('seal_top', (40, 46, 56))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.6 < d < 6.8:
                px[x, y] = TEAL + (255,)
            elif 2.6 < d < 3.4:
                px[x, y] = GLOW + (255,)
    for k in range(8):
        a = k * math.tau / 8
        x, y = round(7.5 + 4.5 * math.cos(a)), round(7.5 + 4.5 * math.sin(a))
        px[x, y] = GLOW + (255,)
    px[7, 7] = px[8, 8] = GOLD + (255,)
    px[7, 8] = px[8, 7] = GOLD_DARK + (255,)
    return img


def seal_side():
    img = stone('seal_side', (40, 46, 56))
    px = img.load()
    for x in range(16):
        px[x, 1] = TEAL + (255,)
        if x % 4 == 1:
            px[x, 3] = GLOW + (255,)
    return img


def reliquary(face):
    img = stone('reliquary_' + face, (52, 40, 44))
    px = img.load()
    for i in range(16):
        for j in (0, 15):
            px[i, j] = GOLD_DARK + (255,)
            px[j, i] = GOLD_DARK + (255,)
    if face == 'top':
        for y in range(5, 11):
            for x in range(5, 11):
                edge = x in (5, 10) or y in (5, 10)
                px[x, y] = (GOLD if edge else (194, 24, 91)) + (255,)
        px[7, 7] = px[6, 6] = (255, 143, 171, 255)
    elif face == 'side':
        for x in range(3, 13):
            px[x, 6] = GOLD + (255,)
        for y in range(7, 11):
            px[7, y] = px[8, y] = GOLD + (255,)
        px[7, 9] = px[8, 9] = (30, 20, 22, 255)  # cerradura
    return img


def custodian_door():
    """T3: Puerta del Custodio. Piedra oscura con marco dorado y una cerradura luminosa en el centro."""
    img = stone('custodian_door', (44, 46, 54))
    px = img.load()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            px[x, y] = GOLD_DARK + (255,)
    for i in range(1, 15):
        for (x, y) in ((i, 1), (i, 14), (1, i), (14, i)):
            if (x + y) % 2 == 0:
                px[x, y] = GOLD + (255,)
    # Cerradura: círculo con ranura inferior.
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 6.0)
            if d < 2.6:
                px[x, y] = GLOW + (255,)
            elif d < 3.4:
                px[x, y] = TEAL + (255,)
    for y in range(8, 13):
        px[7, y] = GLOW + (255,)
        px[8, y] = GLOW + (255,)
        px[6, y] = TEAL + (255,)
        px[9, y] = TEAL + (255,)
    return img


METALS = {
    'copper': (199, 123, 75),
    'iron': (201, 206, 214),
    'gold': GOLD,
    'diamond': (77, 226, 208),
}


def chamber_door(metal):
    """T4: puerta de una cámara de recompensa. Piedra oscura, marco del metal de su llave y cerradura circular."""
    color = METALS[metal]
    dark = tuple(int(v * 0.55) for v in color)
    img = stone('chamber_door_' + metal, (42, 44, 52))
    px = img.load()
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            px[x, y] = dark + (255,)
        for (x, y) in ((i, 1), (i, 14), (1, i), (14, i)):
            px[x, y] = color + (255,)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 6.5)
            if d < 2.4:
                px[x, y] = color + (255,)
            elif d < 3.2:
                px[x, y] = dark + (255,)
    for y in range(8, 13):
        px[7, y] = color + (255,)
        px[8, y] = color + (255,)
    return img


def files():
    out = {
        'sealed_stone.png': sealed_stone(),
        'custodian_door.png': custodian_door(),
        'chamber_door_copper.png': chamber_door('copper'),
        'chamber_door_iron.png': chamber_door('iron'),
        'chamber_door_gold.png': chamber_door('gold'),
        'chamber_door_diamond.png': chamber_door('diamond'),
        'temple_seal_top.png': seal_top(),
        'temple_seal_side.png': seal_side(),
        'reliquary_top.png': reliquary('top'),
        'reliquary_side.png': reliquary('side'),
        'reliquary_bottom.png': reliquary('bottom'),
    }
    encoded = {}
    for name, img in out.items():
        buf = BytesIO()
        img.save(buf, format='PNG', optimize=False)
        encoded[name] = buf.getvalue()
    return encoded


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('folder', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    stale = []
    for name, data in files().items():
        path = args.folder / name
        if args.check:
            if not path.is_file() or path.read_bytes() != data:
                stale.append(str(path))
        else:
            path.write_bytes(data)
    if stale:
        raise SystemExit('Texturas ausentes o desactualizadas:\n' + '\n'.join(stale))
    print(f'{len(files())} texturas del templo {"comprobadas" if args.check else "generadas"}')


if __name__ == '__main__':
    main()
