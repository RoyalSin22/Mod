#!/usr/bin/env python3
"""C7 (S5): genera el glifo de ceniza de las notas en el suelo (32×32, con transparencia).

Uso:  python3 tools/textures/gen_ground_glyph.py src/main/resources/assets/aptitudes/textures/misc [--check]

Dibujo original: un anillo de ceniza con cuatro trazos hacia dentro y un punto central, en gris claro con bordes
difuminados. El cliente lo tiñe y le sube el brillo según las valoraciones. No edites el PNG a mano.
"""
import math
import sys
from pathlib import Path

from PIL import Image

SIZE = 32
NAME = 'ground_glyph.png'


def coverage(x, y):
    """Intensidad (0–1) del trazo en el centro del píxel (x, y)."""
    cx = cy = (SIZE - 1) / 2
    dx, dy = x - cx, y - cy
    r = math.hypot(dx, dy)
    a = 0.0
    a = max(a, 1.0 - abs(r - 12.5) / 1.6)  # anillo
    a = max(a, 1.0 - r / 2.6)  # punto central
    angle = math.atan2(dy, dx)
    for k in range(4):  # cuatro trazos hacia dentro, en diagonal
        target = math.pi / 4 + k * math.pi / 2
        diff = abs((angle - target + math.pi) % (2 * math.pi) - math.pi)
        if 4.5 <= r <= 10.5:
            a = max(a, 1.0 - (diff * r) / 1.3)
    # Grano de ceniza determinista para que no parezca un vector.
    grain = ((x * 73 + y * 151) % 17) / 17.0
    return max(0.0, min(1.0, a)) * (0.75 + 0.25 * grain)


def build():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    px = img.load()
    for y in range(SIZE):
        for x in range(SIZE):
            c = coverage(x, y)
            if c <= 0.04:
                continue
            shade = int(205 + 40 * c)
            px[x, y] = (shade, shade - 6, shade - 14, int(255 * c))
    return img


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    out = Path(sys.argv[1]) / NAME
    img = build()
    if '--check' in sys.argv:
        if not out.exists() or Image.open(out).convert('RGBA').tobytes() != img.tobytes():
            print(f'{out} no coincide con el generador; ejecuta sin --check')
            sys.exit(1)
        print(f'{out} al día')
        return
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out)
    print(f'{out} generado')


if __name__ == '__main__':
    main()
