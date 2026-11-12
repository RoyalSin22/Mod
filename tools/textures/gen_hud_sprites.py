#!/usr/bin/env python3
"""Sprites del HUD de la dirección de arte (v66): del arte fuente al recurso del juego.

Fase 1: marcos de las casillas de habilidad. Fase 2: esfera de maná (energía). Lee `tools/textures/source/hud/casillas/<categoria>_<estado>.png`
(44×44, ilustraciones suaves, no pixel art con rejilla) y escribe `textures/gui/hud/slot/<categoria>_<estado>.png` a
20×20, el tamaño de la casilla del HUD (`AbilityHud.SLOT`), de modo que la disposición no cambia. La esfera de
maná (`esferas/energia_<vacia|llena>.png`, 56×56) se escribe a 28×28 en `textures/gui/hud/orb/`.
Categorías: marcial, arcano, sombra, guardian, oficio. Estados: lista, espera, sin_energia, seleccionada, definitiva.
No edites los PNG de salida.

`--check` acepta una diferencia de hasta 3 niveles por canal: el reescalado puede variar entre versiones de Pillow.

Uso:
  python3 tools/textures/gen_hud_sprites.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_hud_sprites.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/hud'

CATEGORIES = ('marcial', 'arcano', 'sombra', 'guardian', 'oficio')
STATES = ('lista', 'espera', 'sin_energia', 'seleccionada', 'definitiva')
SLOT_SIZE = 20
ORB_SIZE = 28
ORB_STATES = ('vacia', 'llena')
TOLERANCE = 3


def outputs():
    result = {}
    for category in CATEGORIES:
        for state in STATES:
            path = SOURCE / 'casillas' / f'{category}_{state}.png'
            if not path.is_file():
                raise SystemExit(f'Falta el arte fuente {path}')
            with Image.open(path) as raw:
                image = raw.convert('RGBA')
            if image.size != (44, 44):
                raise SystemExit(f'{path.name}: se espera 44×44, no {image.size}')
            result[Path('hud/slot') / f'{category}_{state}.png'] = image.resize((SLOT_SIZE, SLOT_SIZE), Image.LANCZOS)
    for state in ORB_STATES:
        path = SOURCE / 'esferas' / f'energia_{state}.png'
        if not path.is_file():
            raise SystemExit(f'Falta el arte fuente {path}')
        with Image.open(path) as raw:
            image = raw.convert('RGBA')
        if image.size != (56, 56):
            raise SystemExit(f'{path.name}: se espera 56×56, no {image.size}')
        result[Path('hud/orb') / f'energia_{state}.png'] = image.resize((ORB_SIZE, ORB_SIZE), Image.LANCZOS)
    return result


def close(a: Image.Image, b: Image.Image) -> bool:
    if a.size != b.size:
        return False
    return all(abs(x - y) <= TOLERANCE for x, y in zip(a.tobytes(), b.tobytes()))


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    target = Path(argv[1])
    checking = '--check' in argv
    stale = []
    for relative, image in outputs().items():
        path = target / relative
        if checking:
            if not path.is_file() or not close(Image.open(path).convert('RGBA'), image):
                stale.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            image.save(path)
    if stale:
        print('Sprites del HUD ausentes o desactualizados:\n' + '\n'.join(stale))
        return 1
    print(f'{len(CATEGORIES) * len(STATES)} marcos de casilla y {len(ORB_STATES)} esferas de maná '
          f'{"comprobados" if checking else "generados"}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
