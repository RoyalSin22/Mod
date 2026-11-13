#!/usr/bin/env python3
"""Piezas de la pantalla K (Atributos y Habilidades) de la dirección de arte: del arte fuente al recurso del juego.

Lee `tools/textures/source/ui/k/` y escribe `textures/gui/k_ui/<grupo>/<pieza>.png` como copia exacta (las piezas ya
están a su tamaño final, con píxel lógico de 2×2 px). El script comprueba el tamaño, la rejilla de 2×2, el máximo de
24 colores y que la transparencia sea solo 0, 220 (relleno de los paneles) o 255. No edites los PNG de salida.

Encargo del arte: `docs/PROMPT_ARTE_PANTALLA_K.md`.

Uso:
  python3 tools/textures/gen_k_ui.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_k_ui.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/ui/k'
OUT = Path('k_ui')
MAX_COLORS = 24
ALPHAS = {0, 220, 255}


def _states(prefix, states, size):
    return {f'{prefix}_{s}.png': size for s in states}


# grupo -> (nombre de archivo -> tamaño esperado)
PIECES = {
    'paneles': {'k_panel_principal_9slice.png': (192, 128), 'k_panel_hoguera_9slice.png': (192, 128),
                'k_panel_columna_9slice.png': (64, 64), 'k_panel_detalle_9slice.png': (64, 64)},
    'pestanas': _states('pestana', ('inactiva', 'hover', 'activa'), (96, 22)),
    'tarjetas': _states('tarjeta', ('normal', 'hover', 'maxima', 'subida'), (200, 38)),
    'barras': _states('xp', ('vacia', 'llena', 'maxima'), (160, 6)),
    'hitos': _states('hito', ('apagado', 'encendido'), (8, 8)),
    'filas': _states('fila', ('normal', 'seleccionada', 'alterna'), (140, 16)),
    'flechas': {**_states('menos', ('activa', 'desactivada'), (8, 10)),
                **_states('mas', ('activa', 'desactivada'), (8, 10))},
    'cabeceras': {'cabecera_seccion.png': (140, 12)},
    'botones': {f'{b}_{s}.png': (80, 18) for b in ('confirmar', 'deshacer', 'cerrar')
                for s in ('normal', 'pulsado', 'desactivado')},
    'hoguera': {'emblema_hoguera.png': (48, 48)},
}


def check(image: Image.Image, name: str) -> None:
    px = image.load()
    for y in range(0, image.height - 1, 2):
        for x in range(0, image.width - 1, 2):
            if len({px[x + i, y + j] for i in (0, 1) for j in (0, 1)}) != 1:
                raise SystemExit(f'{name}: la rejilla de 2×2 px no es uniforme en ({x}, {y})')
    colors = {px[x, y] for y in range(image.height) for x in range(image.width)}
    if len(colors) > MAX_COLORS:
        raise SystemExit(f'{name}: {len(colors)} colores (máximo {MAX_COLORS})')
    alphas = {c[3] for c in colors} - ALPHAS
    if alphas:
        raise SystemExit(f'{name}: transparencias no permitidas {sorted(alphas)}')


def outputs():
    result = {}
    for group, files in PIECES.items():
        for name, size in files.items():
            path = SOURCE / group / name
            if not path.is_file():
                raise SystemExit(f'Falta el arte fuente {path}')
            with Image.open(path) as raw:
                image = raw.convert('RGBA')
            if image.size != size:
                raise SystemExit(f'{group}/{name}: se espera {size}, no {image.size}')
            check(image, f'{group}/{name}')
            result[OUT / group / name] = image
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
        print('Piezas de la pantalla K ausentes o desactualizadas:\n' + '\n'.join(stale))
        return 1
    print(f'{len(produced)} piezas de la pantalla K {"comprobadas" if checking else "generadas"}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
