#!/usr/bin/env python3
"""Pinta las UV del atlas legado del Coloso (capa y hombreras, v65e).

El geo es de Blockbench (fuente de verdad), así que las UV las fija el JSON. Este script solo escribe en las
cuatro zonas listadas abajo y se niega a pintar si alguna ya tiene píxeles (para no pisar arte existente).
Tamaños en píxeles: uw = 2·(d + w), uh = d + h, redondeados como en gen_boss_models.pack().

Uso:
  python3 tools/textures/paint_faceless_additions.py <copia de faceless_colossus.png> --legacy
  python3 tools/textures/paint_faceless_additions.py <textures/entity/faceless_colossus.png> --check
"""
import importlib.util
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
GEN = ROOT / 'tools' / 'models' / 'gen_boss_models.py'

# (u, v) de la UV del cubo, tamaño (w, h, d) del cubo y material. Coinciden con el JSON de la rama.
ADDITIONS = [
    ((0, 130), (2, 20, 1.2), 'crimson'),     # capa, panel lateral izquierdo (borde rojo)
    ((8, 130), (2, 20, 1.2), 'crimson'),     # capa, panel lateral derecho
    ((0, 152), (0.4, 10.4, 2), 'dark_gold'),  # correa de la hombrera derecha
    ((4, 152), (0.4, 10.4, 2), 'dark_gold'),  # correa de la hombrera izquierda
]


def load_generator():
    spec = importlib.util.spec_from_file_location('gen_boss_models', GEN)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def rect_of(uv, size):
    w, h, d = (int(round(v)) for v in size)
    return uv[0], uv[1], 2 * (d + w), d + h


def paint(img, gen, check_empty):
    px = img.load()
    for uv, size, mat in ADDITIONS:
        u, v, uw, uh = rect_of(uv, size)
        if check_empty and any(px[x, y][3] for x in range(u, u + uw) for y in range(v, v + uh)):
            raise SystemExit(f'La zona {uv} ya tiene píxeles; no se pinta para no pisar arte existente')
        r, g, b = gen.MATERIALS[mat]
        for y in range(v, v + uh):
            for x in range(u, u + uw):
                # Línea superior más clara (el borde recibe la luz), salvo en el rojo de la capa: aclararlo lo haría
                # brillar en la máscara.
                k = 1.25 if y == v and mat != 'crimson' else 1.0
                px[x, y] = (min(255, int(r * k)), min(255, int(g * k)), min(255, int(b * k)), 255)
    return img


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    path = Path(argv[1])
    if any(path.with_name(name).exists() for name in ('faceless_colossus_phase2.png', 'faceless_colossus_phase2_glowmask.png')):
        raise SystemExit('El atlas reconstruido usa otras UV. Esta herramienta solo admite el atlas legado.')
    if '--check' not in argv and '--legacy' not in argv:
        raise SystemExit('Indica --legacy para pintar sobre una copia del atlas antiguo.')
    gen = load_generator()
    img = Image.open(path).convert('RGBA')
    if '--check' in argv:
        expected = paint(img.copy(), gen, check_empty=False)
        same = Image.open(path).convert('RGBA').tobytes() == expected.tobytes()
        print('OK' if same else f'DESACTUALIZADA {path}')
        return 0 if same else 1
    paint(img, gen, check_empty=True).save(path)
    print(f'pintadas {len(ADDITIONS)} zonas en {path}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
