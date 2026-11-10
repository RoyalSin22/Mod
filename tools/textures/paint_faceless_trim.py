#!/usr/bin/env python3
"""Repinta las UV del hueso «trim» (biseles y remaches del pecho) en la textura pintada del Coloso (v65c).

La textura del Coloso es pintada a mano y gen_boss_models.py NO la regenera. Este script solo
escribe en los rectángulos que ocupan los cubos del hueso «trim» (siempre al final del empaquetado,
así que el resto de la textura no cambia). Cada cubo se pinta con el color de su material y una
línea de luz en el borde superior, para que el bisel y el remache se lean como relieve.

Uso:
  python3 tools/textures/paint_faceless_trim.py <textures/entity/faceless_colossus.png>
  python3 tools/textures/paint_faceless_trim.py <textures/entity/faceless_colossus.png> --check
"""
import importlib.util
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
GEN = ROOT / 'tools' / 'models' / 'gen_boss_models.py'


def load_generator():
    spec = importlib.util.spec_from_file_location('gen_boss_models', GEN)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def trim_rects(gen):
    rects = []
    for name, origin, size, mat, uv in gen.pack(gen.COLOSSUS_BONES):
        if name != 'trim':
            continue
        w, h, d = (int(round(v)) for v in size)
        rects.append((uv[0], uv[1], 2 * (d + w), d + h, mat))
    return rects


def paint(img, rects, gen):
    px = img.load()
    for u, v, uw, uh, mat in rects:
        r, g, b = gen.MATERIALS[mat]
        for y in range(v, min(img.height, v + uh)):
            for x in range(u, min(img.width, u + uw)):
                # Línea superior más clara: el borde recibe la luz y el remache se lee como relieve.
                k = 1.25 if y == v else 1.0
                px[x, y] = (min(255, int(r * k)), min(255, int(g * k)), min(255, int(b * k)), 255)
    return img


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    path = Path(argv[1])
    gen = load_generator()
    rects = trim_rects(gen)
    img = Image.open(path).convert('RGBA')
    painted = paint(img.copy(), rects, gen)
    if '--check' in argv:
        same = Image.open(path).convert('RGBA').tobytes() == painted.tobytes()
        print('OK' if same else f'DESACTUALIZADA {path}')
        return 0 if same else 1
    painted.save(path)
    print(f'repintados {len(rects)} rectángulos en {path}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
