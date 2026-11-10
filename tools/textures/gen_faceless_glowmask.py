#!/usr/bin/env python3
"""Genera la máscara de brillo del Coloso Sin Rostro (v65).

Lee la textura pintada `faceless_colossus.png` y deja solo los píxeles rojos
saturados (grietas de netherita) en una máscara RGBA del mismo tamaño; el resto
queda transparente. GeckoLib (AutoGlowingGeoLayer) la usa para dibujar las
grietas a plena luz sin tocar la textura base.

Uso:
  python3 tools/textures/gen_faceless_glowmask.py <directorio entity>
  python3 tools/textures/gen_faceless_glowmask.py <directorio entity> --check
"""
import sys
from pathlib import Path

from PIL import Image

BASE = 'faceless_colossus.png'
MASK = 'faceless_colossus_glowmask.png'


def is_crack(r: int, g: int, b: int, a: int) -> bool:
    # Rojo dominante y saturado: las grietas de la hoja de arte (no el borde rojo de la capa).
    return a > 0 and r >= 150 and r > g * 2 and r > b * 2


def build(base: Image.Image) -> Image.Image:
    src = base.convert('RGBA')
    out = Image.new('RGBA', src.size, (0, 0, 0, 0))
    px_in, px_out = src.load(), out.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = px_in[x, y]
            if is_crack(r, g, b, a):
                px_out[x, y] = (r, g, b, 255)
    return out


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print(__doc__)
        return 2
    entity = Path(argv[1])
    mask = build(Image.open(entity / BASE))
    target = entity / MASK
    if '--check' in argv:
        if not target.exists():
            print(f'FALTA {target}')
            return 1
        same = Image.open(target).convert('RGBA').tobytes() == mask.tobytes()
        print('OK' if same else f'DESACTUALIZADA {target}')
        return 0 if same else 1
    mask.save(target)
    print(f'escrito {target}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
