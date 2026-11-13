#!/usr/bin/env python3
"""Piezas de la pantalla K (personaje: atributos y habilidades) de la dirección de arte (v67): del arte fuente al recurso.

Lee `tools/textures/source/k_ui/<grupo>/<pieza>.png` (34 piezas) y escribe `textures/gui/k_ui/<grupo>/<pieza>.png`
con copia exacta: ya están al tamaño final, con píxel lógico de 2×2 px. El script comprueba tamaño, rejilla y número
de colores (máximo 24) y se niega a copiar lo que no cumpla. No edites los PNG de salida.

Uso:
  python3 tools/textures/gen_k_ui.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_k_ui.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/k_ui'
OUT = Path('k_ui')
MAX_COLORS = 24

# grupo -> (nombre de archivo -> tamaño esperado)
PIECES = {
    'barras': {f'xp_{n}.png': (160, 6) for n in ('vacia', 'llena', 'maxima')},
    'botones': {f'{b}_{s}.png': (80, 18) for b in ('confirmar', 'deshacer', 'cerrar')
                for s in ('normal', 'pulsado', 'desactivado')},
    'cabeceras': {'cabecera_seccion.png': (140, 12)},
    'filas': {f'fila_{n}.png': (140, 16) for n in ('normal', 'seleccionada', 'alterna')},
    'flechas': {f'{d}_{s}.png': (8, 10) for d in ('mas', 'menos') for s in ('activa', 'desactivada')},
    'hitos': {'hito_apagado.png': (8, 8), 'hito_encendido.png': (8, 8)},
    'hoguera': {'emblema_hoguera.png': (48, 48)},
    'paneles': {'k_panel_principal_9slice.png': (192, 128), 'k_panel_hoguera_9slice.png': (192, 128),
                'k_panel_columna_9slice.png': (64, 64), 'k_panel_detalle_9slice.png': (64, 64)},
    'pestanas': {f'pestana_{n}.png': (96, 22) for n in ('inactiva', 'hover', 'activa')},
    'tarjetas': {f'tarjeta_{n}.png': (200, 38) for n in ('normal', 'hover', 'maxima', 'subida')},
}


def check_grid(image: Image.Image, name: str) -> None:
    """El píxel lógico es de 2×2 px: cada bloque de 2×2 debe ser de un solo color."""
    px = image.load()
    for y in range(0, image.height - 1, 2):
        for x in range(0, image.width - 1, 2):
            if len({px[x + i, y + j] for i in (0, 1) for j in (0, 1)}) != 1:
                raise SystemExit(f'{name}: la rejilla de 2×2 px no es uniforme en ({x}, {y})')


def load(path: Path, size, name: str) -> Image.Image:
    if not path.is_file():
        raise SystemExit(f'Falta el arte fuente {path}')
    with Image.open(path) as raw:
        image = raw.convert('RGBA')
    if image.size != size:
        raise SystemExit(f'{name}: se espera {size}, no {image.size}')
    colors = len({image.getpixel((x, y)) for y in range(image.height) for x in range(image.width)})
    if colors > MAX_COLORS:
        raise SystemExit(f'{name}: {colors} colores (máximo {MAX_COLORS})')
    check_grid(image, name)
    return image


def outputs():
    result = {}
    for group, files in PIECES.items():
        for name, size in files.items():
            result[OUT / group / name] = load(SOURCE / group / name, size, f'{group}/{name}')
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
