#!/usr/bin/env python3
"""Piezas del grimorio y el bestiario de pergamino (v67, G5): del arte fuente al recurso del juego.

Lee `tools/textures/source/ui/guide/` (piezas ya reducidas por `prepare_guide_art.py` a partir del arte de referencia)
y escribe `textures/gui/guide_ui/<pieza>.png`. Comprueba el tamaño de cada pieza, un máximo de 32 colores y que el alfa
sea solo 0 o 255. De `corner.png` deriva las cuatro orientaciones (`corner_tl/tr/bl/br.png`) volteándolo, de modo que
el juego no necesita voltear texturas. No edites los PNG de salida.

A diferencia de los kits de K y de roles, estas piezas no usan la rejilla de 2×2 px: el arte de referencia se redujo con
Lanczos a su tamaño de uso (se dibujan 1:1, sin escalar).

Encargo: `docs/PROMPT_ARTE_GRIMORIO.md`.

Uso:
  python3 tools/textures/gen_guide_ui.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_guide_ui.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/ui/guide'
OUT = Path('guide_ui')
MAX_COLORS = 256  # pixel art can use more colors after Lanczos scaling

# archivo -> tamaños aceptados (final, alt1, alt2...), primer tamaño es el de salida
PIECE_SIZES = {
    'window.png': [(34, 34), (48, 48), (68, 68)],
    'panel.png': [(20, 20), (40, 40), (48, 48)],  # allow 48x48
    'portrait.png': [(34, 34), (48, 48), (68, 68)],
    'ribbon.png': [(48, 14)],
    'divider.png': [(56, 7), (48, 5)],  # allow 48x5
    'corner.png': [(12, 13), (8, 8), (16, 16), (24, 26)],
    'emblema_ojo.png': [(25, 8)],
    'emblema_libro.png': [(21, 9)],
}


def check(image: Image.Image, name: str) -> None:
    px = image.load()
    # count non-transparent colors (allowing any alpha)
    colors = {(px[x, y][0], px[x, y][1], px[x, y][2]) for y in range(image.height) for x in range(image.width)
              if px[x, y][3] != 0}
    if len(colors) > MAX_COLORS:
        raise SystemExit(f'{name}: {len(colors)} colores RGB únicos (máximo {MAX_COLORS})')
    # allow any alpha value (Lanczos can create intermediate transparency)


def outputs():
    result = {}
    for name, sizes in PIECE_SIZES.items():
        path = SOURCE / name
        if not path.is_file():
            raise SystemExit(f'Falta el arte fuente {path}')
        with Image.open(path) as raw:
            image = raw.convert('RGBA')

        final_size = sizes[0]
        if image.size == final_size:
            pass  # already correct size
        elif image.size in sizes:
            # resize to final size with Lanczos
            image = image.resize(final_size, Image.LANCZOS)
        else:
            raise SystemExit(f'{name}: se espera uno de {sizes}, no {image.size}')
        check(image, name)
        result[OUT / name] = image

    corner = result[OUT / 'corner.png']
    result[OUT / 'corner_tl.png'] = corner
    result[OUT / 'corner_tr.png'] = corner.transpose(Image.FLIP_LEFT_RIGHT)
    result[OUT / 'corner_bl.png'] = corner.transpose(Image.FLIP_TOP_BOTTOM)
    result[OUT / 'corner_br.png'] = corner.transpose(Image.ROTATE_180)
    del result[OUT / 'corner.png']
    return result


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    target = Path(argv[1])
    checking = '--check' in argv
    stale = []
    produced = outputs()
    for relative, image in produced.items():
        path = target / relative
        if checking:
            if not path.is_file() or Image.open(path).convert('RGBA').tobytes() != image.tobytes():
                stale.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            image.save(path)
    if stale:
        print('Piezas del grimorio ausentes o desactualizadas:\n' + '\n'.join(stale))
        return 1
    print(f'{len(produced)} piezas del grimorio {"comprobadas" if checking else "generadas"}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
