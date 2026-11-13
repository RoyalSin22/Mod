#!/usr/bin/env python3
"""Piezas de la pantalla del árbol de la dirección de arte (v66): del arte fuente al recurso del juego.

Lee `tools/textures/source/ui/` y escribe `textures/gui/tree_ui/<grupo>/<pieza>.png`:
  paneles, cabeceras, etiquetas, maestria, definitiva y botones: copia exacta (ya están al tamaño final,
      con píxel lógico de 2×2 px; el script comprueba el formato y la rejilla).
  nodos: marcos de nodo (categoría × estado). La fuente es de 64×64; se escribe a 24×24 (tamaño del nodo en pantalla).
No edites los PNG de salida.

`--check` acepta una diferencia de hasta 3 niveles por canal (el reescalado de los nodos puede variar con Pillow).

Uso:
  python3 tools/textures/gen_tree_ui.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_tree_ui.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/ui'
OUT = Path('tree_ui')
TOLERANCE = 3
NODE_SIZE = 24

CATEGORIES = ('marcial', 'arcano', 'sombra', 'guardian', 'oficio')
STATES = ('bloqueado', 'disponible', 'comprado', 'elegido', 'descartado')

# grupo -> (nombre de archivo -> tamaño esperado)
COPIED = {
    'paneles': {'panel_principal_9slice.png': (192, 128), 'panel_detalle_9slice.png': (112, 160)},
    'cabeceras': {f'senda_{n}.png': (96, 24) for n in ('sangre', 'acero', 'trueno')},
    'etiquetas': {f'etiqueta_s{i}.png': (24, 14) for i in range(1, 8)},
    'maestria': {'maestria_20_vacias.png': (248, 24), 'maestria_20_llenas.png': (248, 24)},
    'definitiva': {'definitiva_bloqueada.png': (48, 48), 'definitiva_desbloqueada.png': (48, 48)},
    'botones': {f'{b}_{s}.png': (80, 18) for b in ('cerrar', 'generico', 'reiniciar')
                for s in ('normal', 'pulsado', 'desactivado')},
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
    check_grid(image, name)
    return image


def outputs():
    result = {}
    for group, files in COPIED.items():
        for name, size in files.items():
            result[OUT / group / name] = load(SOURCE / group / name, size, f'{group}/{name}')
    for category in CATEGORIES:
        for state in STATES:
            name = f'{category}_{state}.png'
            image = load(SOURCE / 'nodos' / name, (64, 64), f'nodos/{name}')
            logical = image.resize((32, 32), Image.NEAREST)  # píxel lógico exacto
            result[OUT / 'node' / name] = logical.resize((NODE_SIZE, NODE_SIZE), Image.LANCZOS)
    return result


def close(a: Image.Image, b: Image.Image) -> bool:
    return a.size == b.size and all(abs(x - y) <= TOLERANCE for x, y in zip(a.tobytes(), b.tobytes()))


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
            if not path.is_file() or not close(Image.open(path).convert('RGBA'), image):
                stale.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            image.save(path)
    if stale:
        print('Piezas del árbol ausentes o desactualizadas:\n' + '\n'.join(stale))
        return 1
    print(f'{len(produced)} piezas del árbol {"comprobadas" if checking else "generadas"}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
