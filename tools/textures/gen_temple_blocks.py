#!/usr/bin/env python3
"""Texturas de los bloques del Templo sellado (v64, K6): piedra sellada, Sello y Relicario (16×16, arte original).

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


def files():
    out = {
        'sealed_stone.png': sealed_stone(),
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
