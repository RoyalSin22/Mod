#!/usr/bin/env python3
"""Genera las ilustraciones de cabecera del grimorio (v61, G4): 384×34 px por capítulo y página.

Los capítulos se leen de GuideScreen.java (chapter("id", …)); se añaden las páginas propias del grimorio.
Estilo del mod: fondo nocturno degradado, marco dorado, constelación determinista y un motivo
silueteado con el color del capítulo. Arte original generado por programa (sin recursos externos).

Uso: python3 tools/textures/gen_guide_headers.py src/main/resources/assets/aptitudes/textures/gui/guide [--check]
"""
import argparse
import math
import random
import re
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw

W, H = 384, 34
ROOT = Path(__file__).resolve().parents[2]
SCREEN = ROOT / 'src/client/java/dev/aptitudes/client/screen/GuideScreen.java'
EXTRA_PAGES = ('bestiary', 'progress', 'bookmarks')

GOLD = (230, 195, 106)
GOLD_DARK = (156, 122, 53)

# Color de acento y motivo por página; las desconocidas usan un acento derivado del nombre y un libro.
THEMES = {
    'start': ((214, 180, 110), 'book'), 'keys': ((150, 170, 210), 'key'),
    'bonfire': ((255, 140, 50), 'flame'), 'attributes': ((120, 220, 140), 'rune'),
    'skills': ((180, 200, 220), 'pickaxe'), 'roles': ((200, 150, 255), 'crown'),
    'tree': ((110, 200, 120), 'tree'), 'evolution': ((255, 210, 90), 'crown'),
    'weapons': ((220, 220, 235), 'sword'), 'elements': ((90, 200, 255), 'gem'),
    'armor': ((170, 180, 200), 'shield'), 'soul_items': ((120, 230, 255), 'gem'),
    'accessories': ((255, 200, 120), 'ring'), 'elites': ((255, 90, 90), 'skull'),
    'eclipse': ((220, 40, 50), 'eclipse'), 'structures': ((170, 160, 140), 'tower'),
    'contracts': ((230, 200, 150), 'scroll'), 'guild': ((200, 160, 90), 'hammer'),
    'drops': ((160, 210, 120), 'skull'), 'paths': ((200, 120, 255), 'star'),
    'achievements': ((255, 215, 0), 'star'), 'alchemy': ((200, 110, 230), 'flask'),
    'bestiary': ((200, 70, 70), 'skull'), 'progress': ((120, 200, 255), 'clock'),
    'bookmarks': ((240, 190, 80), 'ribbon'),
}


def chapter_ids():
    text = SCREEN.read_text(encoding='utf-8')
    ids = re.findall(r'chapter\("([a-z_]+)"', text)
    if len(ids) < 10:
        raise SystemExit('No se encontraron los capítulos en GuideScreen.java')
    return list(dict.fromkeys(ids + list(EXTRA_PAGES)))


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def accent_for(name):
    if name in THEMES:
        return THEMES[name]
    rnd = random.Random(name)
    return (rnd.randint(120, 255), rnd.randint(120, 255), rnd.randint(120, 255)), 'book'


def background(accent, rnd):
    img = Image.new('RGBA', (W, H))
    px = img.load()
    top, bottom = (13, 15, 24), (27, 31, 48)
    for y in range(H):
        row = mix(top, bottom, y / (H - 1))
        for x in range(W):
            # Izquierda más oscura (legibilidad del título); a la derecha, un velo del color del capítulo.
            t = max(0.0, (x - 120) / (W - 120))
            c = mix(row, mix(row, accent, 0.22), t * t)
            px[x, y] = c + (255,)
    # Constelación determinista.
    for _ in range(36):
        x, y = rnd.randrange(6, W - 6), rnd.randrange(4, H - 4)
        b = rnd.randint(70, 170)
        px[x, y] = mix(px[x, y][:3], (b + 60, b + 60, b + 80), 0.8) + (255,)
    return img


def frame(img, accent):
    d = ImageDraw.Draw(img)
    d.line([(0, 0), (W - 1, 0)], fill=GOLD_DARK + (255,))
    d.line([(0, H - 1), (W - 1, H - 1)], fill=GOLD_DARK + (255,))
    d.line([(0, 2), (W - 1, 2)], fill=mix(GOLD_DARK, (20, 22, 34), 0.55) + (255,))
    d.line([(0, H - 3), (W - 1, H - 3)], fill=mix(accent, (20, 22, 34), 0.45) + (255,))
    # Remates en rombo cada 48 px en la línea inferior.
    for x in range(24, W, 48):
        d.polygon([(x, H - 5), (x + 2, H - 3), (x, H - 1), (x - 2, H - 3)], fill=GOLD + (255,))


def motif(img, kind, accent, cx, cy, alpha):
    """Silueta del motivo en una capa aparte, fundida con transparencia."""
    layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    c = accent + (alpha,)
    hi = mix(accent, (255, 255, 255), 0.45) + (alpha,)
    if kind == 'flame':
        d.polygon([(cx, cy - 13), (cx + 8, cy), (cx + 6, cy + 10), (cx - 6, cy + 10), (cx - 8, cy)], fill=c)
        d.polygon([(cx, cy - 5), (cx + 4, cy + 3), (cx, cy + 9), (cx - 4, cy + 3)], fill=hi)
    elif kind == 'eclipse':
        d.ellipse([cx - 13, cy - 13, cx + 13, cy + 13], fill=c)
        d.ellipse([cx - 10, cy - 10, cx + 10, cy + 10], fill=(8, 8, 12, 255))
        for k in range(12):
            a = k * math.tau / 12
            d.line([(cx + 15 * math.cos(a), cy + 15 * math.sin(a)), (cx + 18 * math.cos(a), cy + 18 * math.sin(a))], fill=c)
    elif kind == 'sword':
        d.polygon([(cx - 14, cy + 9), (cx + 11, cy - 10), (cx + 13, cy - 8), (cx - 12, cy + 11)], fill=hi)
        d.line([(cx - 9, cy + 1), (cx - 3, cy + 8)], fill=c, width=2)
        d.line([(cx - 14, cy + 11), (cx - 10, cy + 7)], fill=c, width=3)
    elif kind == 'shield':
        d.polygon([(cx - 10, cy - 11), (cx + 10, cy - 11), (cx + 10, cy + 1), (cx, cy + 12), (cx - 10, cy + 1)], fill=c)
        d.line([(cx, cy - 9), (cx, cy + 9)], fill=hi, width=2)
        d.line([(cx - 8, cy - 3), (cx + 8, cy - 3)], fill=hi, width=2)
    elif kind == 'tower':
        d.rectangle([cx - 6, cy - 6, cx + 6, cy + 13], fill=c)
        for k in (-6, -2, 2):
            d.rectangle([cx + k, cy - 10, cx + k + 2, cy - 6], fill=c)
        d.rectangle([cx - 1, cy + 6, cx + 1, cy + 13], fill=(8, 8, 12, 255))
    elif kind == 'gem':
        d.polygon([(cx, cy - 12), (cx + 10, cy - 3), (cx, cy + 12), (cx - 10, cy - 3)], fill=c)
        d.polygon([(cx, cy - 12), (cx + 4, cy - 3), (cx, cy + 6), (cx - 4, cy - 3)], fill=hi)
    elif kind == 'skull':
        d.ellipse([cx - 10, cy - 12, cx + 10, cy + 6], fill=c)
        d.rectangle([cx - 6, cy + 3, cx + 6, cy + 11], fill=c)
        d.ellipse([cx - 7, cy - 5, cx - 2, cy], fill=(8, 8, 12, 255))
        d.ellipse([cx + 2, cy - 5, cx + 7, cy], fill=(8, 8, 12, 255))
        for k in (-4, 0, 4):
            d.line([(cx + k, cy + 7), (cx + k, cy + 11)], fill=(8, 8, 12, 255))
    elif kind == 'star':
        pts = []
        for k in range(10):
            r = 13 if k % 2 == 0 else 5
            a = -math.pi / 2 + k * math.pi / 5
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
        d.polygon(pts, fill=c)
        d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=hi)
    elif kind == 'crown':
        d.polygon([(cx - 13, cy + 8), (cx - 13, cy - 6), (cx - 6, cy + 1), (cx, cy - 10), (cx + 6, cy + 1),
                   (cx + 13, cy - 6), (cx + 13, cy + 8)], fill=c)
        for k in (-8, 0, 8):
            d.ellipse([cx + k - 1, cy + 3, cx + k + 1, cy + 5], fill=hi)
    elif kind == 'key':
        d.ellipse([cx - 14, cy - 6, cx - 4, cy + 4], outline=c, width=3)
        d.line([(cx - 4, cy - 1), (cx + 13, cy - 1)], fill=c, width=3)
        d.line([(cx + 8, cy), (cx + 8, cy + 6)], fill=c, width=2)
        d.line([(cx + 12, cy), (cx + 12, cy + 4)], fill=c, width=2)
    elif kind == 'rune':
        d.ellipse([cx - 12, cy - 12, cx + 12, cy + 12], outline=c, width=2)
        d.line([(cx, cy - 9), (cx, cy + 9)], fill=hi, width=2)
        d.line([(cx, cy - 2), (cx + 6, cy - 8)], fill=hi, width=2)
        d.line([(cx, cy + 3), (cx - 6, cy - 3)], fill=hi, width=2)
    elif kind == 'pickaxe':
        d.arc([cx - 14, cy - 12, cx + 14, cy + 8], 200, 340, fill=hi, width=3)
        d.line([(cx, cy - 8), (cx - 2, cy + 13)], fill=c, width=3)
    elif kind == 'tree':
        d.ellipse([cx - 12, cy - 13, cx + 12, cy + 5], fill=c)
        d.rectangle([cx - 2, cy + 2, cx + 2, cy + 13], fill=mix(accent, (90, 60, 30), 0.6) + (alpha,))
    elif kind == 'ring':
        d.ellipse([cx - 11, cy - 8, cx + 11, cy + 12], outline=c, width=3)
        d.polygon([(cx, cy - 13), (cx + 4, cy - 8), (cx, cy - 4), (cx - 4, cy - 8)], fill=hi)
    elif kind == 'scroll':
        d.rectangle([cx - 11, cy - 9, cx + 11, cy + 9], fill=c)
        d.ellipse([cx - 14, cy - 11, cx - 8, cy + 11], fill=hi)
        d.ellipse([cx + 8, cy - 11, cx + 14, cy + 11], fill=hi)
        for k in (-4, 0, 4):
            d.line([(cx - 6, cy + k), (cx + 6, cy + k)], fill=(40, 30, 20, alpha))
    elif kind == 'hammer':
        d.rectangle([cx - 12, cy - 11, cx + 6, cy - 3], fill=hi)
        d.line([(cx - 3, cy - 3), (cx - 3, cy + 13)], fill=c, width=3)
    elif kind == 'flask':
        d.rectangle([cx - 3, cy - 13, cx + 3, cy - 6], fill=hi)
        d.polygon([(cx - 3, cy - 6), (cx + 3, cy - 6), (cx + 11, cy + 11), (cx - 11, cy + 11)], fill=c)
        d.polygon([(cx - 7, cy + 4), (cx + 7, cy + 4), (cx + 11, cy + 11), (cx - 11, cy + 11)], fill=hi)
    elif kind == 'clock':
        d.ellipse([cx - 12, cy - 12, cx + 12, cy + 12], outline=c, width=3)
        d.line([(cx, cy), (cx, cy - 8)], fill=hi, width=2)
        d.line([(cx, cy), (cx + 6, cy + 3)], fill=hi, width=2)
    elif kind == 'ribbon':
        d.polygon([(cx - 8, cy - 13), (cx + 8, cy - 13), (cx + 8, cy + 13), (cx, cy + 6), (cx - 8, cy + 13)], fill=c)
        d.line([(cx - 5, cy - 9), (cx + 5, cy - 9)], fill=hi)
    else:  # book
        d.rectangle([cx - 13, cy - 10, cx - 1, cy + 10], fill=c)
        d.rectangle([cx + 1, cy - 10, cx + 13, cy + 10], fill=c)
        for k in (-5, -1, 3):
            d.line([(cx - 10, cy + k), (cx - 4, cy + k)], fill=hi)
            d.line([(cx + 4, cy + k), (cx + 10, cy + k)], fill=hi)
    img.alpha_composite(layer)


def banner(name):
    accent, kind = accent_for(name)
    rnd = random.Random('aptitudes-guide-' + name)
    img = background(accent, rnd)
    # Motivo principal tenue en el centro-derecha y eco pequeño al final del ancho máximo.
    motif(img, kind, accent, 182, H // 2, 70)
    motif(img, kind, accent, 300, H // 2, 40)
    frame(img, accent)
    out = BytesIO()
    img.convert('RGB').save(out, format='PNG', optimize=False)
    return out.getvalue()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('folder', type=Path)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--preview', type=Path, help='Hoja con todas las cabeceras (solo para revisar)')
    args = parser.parse_args()
    names = chapter_ids()
    failed = []
    if not args.check:
        args.folder.mkdir(parents=True, exist_ok=True)
    for name in names:
        data = banner(name)
        path = args.folder / f'{name}.png'
        if args.check:
            if not path.is_file() or path.read_bytes() != data:
                failed.append(str(path))
        else:
            path.write_bytes(data)
    if args.preview:
        sheet = Image.new('RGB', (W, (H + 2) * len(names)), (0, 0, 0))
        for i, name in enumerate(names):
            sheet.paste(Image.open(BytesIO(banner(name))), (0, i * (H + 2)))
        sheet.save(args.preview)
    if failed:
        raise SystemExit('Cabeceras ausentes o desactualizadas:\n' + '\n'.join(failed))
    print(f'{len(names)} cabeceras del grimorio {"comprobadas" if args.check else "generadas"}')


if __name__ == '__main__':
    main()
