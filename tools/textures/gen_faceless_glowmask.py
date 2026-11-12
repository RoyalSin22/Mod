#!/usr/bin/env python3
"""Comprueba las máscaras pintadas del Coloso Sin Rostro o genera la máscara del atlas legado.

Lee la textura pintada `faceless_colossus.png` y deja solo los píxeles rojos
saturados (grietas de netherita) en una máscara RGBA del mismo tamaño; el resto
queda transparente. GeckoLib (AutoGlowingGeoLayer) la usa para dibujar las
grietas a plena luz sin tocar la textura base.

Uso:
  python3 tools/textures/gen_faceless_glowmask.py <directorio entity> --check
  python3 tools/textures/gen_faceless_glowmask.py <directorio de un atlas legado> --legacy
"""
import sys
from pathlib import Path

from PIL import Image

BASE = 'faceless_colossus.png'
MASK = 'faceless_colossus_glowmask.png'
PHASE2 = 'faceless_colossus_phase2.png'
PHASE2_MASK = 'faceless_colossus_phase2_glowmask.png'


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


def check_painted(directory: Path) -> int:
    for name in (BASE, MASK, PHASE2, PHASE2_MASK):
        path = directory / name
        if not path.is_file():
            print(f'FALTA {path}')
            return 1
        with Image.open(path) as image:
            if image.format != 'PNG' or image.mode != 'RGBA' or image.size != (512, 512):
                print(f'{path}: se espera PNG RGBA de 512 × 512')
                return 1
            image.load()
            if name in (MASK, PHASE2_MASK):
                alpha = image.getchannel('A').histogram()
                if not alpha[0] or not alpha[255] or any(alpha[1:255]):
                    print(f'{path}: se espera una máscara no vacía con alfa binario')
                    return 1
    print('OK: las dos fases tienen textura y máscara pintadas')
    return 0


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print(__doc__)
        return 2
    entity = Path(argv[1])
    checking = '--check' in argv
    if (entity / PHASE2).exists() or (entity / PHASE2_MASK).exists():
        if checking:
            return check_painted(entity)
        print('El atlas reconstruido usa máscaras pintadas. Compruébalas con --check.')
        return 1
    if not checking and '--legacy' not in argv:
        print('Indica --legacy para generar una máscara sobre una copia del atlas antiguo.')
        return 2
    with Image.open(entity / BASE) as image:
        mask = build(image)
    target = entity / MASK
    if checking:
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
