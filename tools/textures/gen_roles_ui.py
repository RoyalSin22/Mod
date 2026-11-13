#!/usr/bin/env python3
"""Piezas de la pantalla de roles (J) de la dirección de arte (kit «fantasía oscura v2»): del arte fuente al recurso.

Lee `tools/textures/source/ui/roles/` y escribe `textures/gui/roles_ui/<pieza>.png` como copia exacta (las piezas ya
están a su tamaño final, con píxel lógico de 2×2 px). Comprueba el tamaño, la rejilla de 2×2, el máximo de 24 colores y
que la transparencia sea solo 0 o 255. Botones, pestañas y filas son 9-slice con bordes de 6 px. No edites los PNG de
salida.

Excepción: las fichas de categoría (`categoria_*`) son de **48×32 a doble densidad** (el juego las dibuja a 24×16) y con
detalle de 1 texel, así que no siguen la rejilla de 2×2 y admiten hasta 48 colores. Encargo:
`docs/PROMPT_ARTE_FICHAS_CATEGORIA.md`.

Uso:
  python3 tools/textures/gen_roles_ui.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_roles_ui.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/ui/roles'
OUT = Path('roles_ui')
MAX_COLORS = 24
CATEGORIES = ('marcial', 'arcano', 'sombra', 'guardian', 'oficio')

PIECES = {
    **{f'boton_largo_{s}.png': (48, 18) for s in ('normal', 'pulsado', 'desactivado')},
    **{f'pestana_{s}.png': (48, 16) for s in ('normal', 'hover', 'activa')},
    **{f'fila_{s}.png': (48, 18) for s in ('normal', 'seleccionada')},
    **{f'categoria_{c}_{s}.png': (48, 32) for c in CATEGORIES for s in ('normal', 'hover', 'activa')},
    'divisor_horizontal.png': (48, 6),
    'divisor_base.png': (48, 6),
    'divisor_rombo.png': (8, 6),
    'titulo_franja.png': (96, 10),
}


def check(image: Image.Image, name: str) -> None:
    px = image.load()
    detailed = name.startswith('categoria_')  # doble densidad, detalle de 1 texel
    for y in range(0, 0 if detailed else image.height - 1, 2):
        for x in range(0, image.width - 1, 2):
            if len({px[x + i, y + j] for i in (0, 1) for j in (0, 1)}) != 1:
                raise SystemExit(f'{name}: la rejilla de 2×2 px no es uniforme en ({x}, {y})')
    colors = {px[x, y] for y in range(image.height) for x in range(image.width)}
    limit = 48 if detailed else MAX_COLORS
    if len(colors) > limit:
        raise SystemExit(f'{name}: {len(colors)} colores (máximo {limit})')
    if {c[3] for c in colors} - {0, 255}:
        raise SystemExit(f'{name}: solo se admite transparencia 0 o 255')


def outputs():
    result = {}
    for name, size in PIECES.items():
        path = SOURCE / name
        if not path.is_file():
            raise SystemExit(f'Falta el arte fuente {path}')
        with Image.open(path) as raw:
            image = raw.convert('RGBA')
        if image.size != size:
            raise SystemExit(f'{name}: se espera {size}, no {image.size}')
        check(image, name)
        result[OUT / name] = image
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
        print('Piezas de la pantalla de roles ausentes o desactualizadas:\n' + '\n'.join(stale))
        return 1
    print(f'{len(produced)} piezas de la pantalla de roles {"comprobadas" if checking else "generadas"}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
