#!/usr/bin/env python3
"""Iconos de rol de la dirección de arte (v66): del arte fuente al recurso del juego.

Lee los 20 PNG de `tools/textures/source/roles/` (64×64, rejilla exacta de 2×2 px, es decir, 32×32 reales) y escribe:
  textures/gui/role/<rol>.png        32×32, para tamaños grandes.
  textures/gui/role/small/<rol>.png  16×16, para el tamaño del árbol, el HUD y el grimorio (promedio de 2×2 ponderado
                                     por el alfa, para que el borde transparente del medallón no se ensucie).
Los nombres en español del arte fuente se traducen a las claves de `Role.java`. No edites los PNG de salida.

Uso:
  python3 tools/textures/gen_role_icons.py src/main/resources/assets/aptitudes/textures/gui
  python3 tools/textures/gen_role_icons.py src/main/resources/assets/aptitudes/textures/gui --check
"""
import re
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/roles'
ROLE_JAVA = ROOT / 'src/main/java/dev/aptitudes/role/Role.java'

# nombre del arte fuente -> clave del rol (Role.java)
NAMES = {
    'berserker': 'berserker', 'mago': 'mage', 'asesino': 'assassin', 'herbolista': 'herbalist',
    'ingeniero': 'engineer', 'lancero': 'lancer', 'espadachin': 'swordsman', 'paladin': 'paladin',
    'nigromante': 'necromancer', 'druida': 'druid', 'mercader': 'merchant', 'monje': 'monk',
    'elementalista': 'elementalist', 'cambiaformas': 'shapeshifter', 'pistolero_arcano': 'gunslinger',
    'invocador_de_armas': 'weapon_summoner', 'cazador': 'hunter', 'bardo': 'bard', 'brujo': 'warlock',
    'domador': 'tamer',
}


def role_keys():
    text = ROLE_JAVA.read_text(encoding='utf-8')
    found = re.findall(r'^\s*([A-Z_]+)\(RoleCategory\.', text, re.MULTILINE)
    return [name.lower() for name in found]


def to_32(image: Image.Image, name: str) -> Image.Image:
    if image.size != (64, 64):
        raise SystemExit(f'{name}: se espera 64×64, no {image.size}')
    px = image.load()
    out = Image.new('RGBA', (32, 32))
    out_px = out.load()
    for y in range(32):
        for x in range(32):
            block = {px[2 * x + i, 2 * y + j] for i in (0, 1) for j in (0, 1)}
            if len(block) != 1:
                raise SystemExit(f'{name}: la rejilla de 2×2 px no es uniforme en ({x}, {y})')
            out_px[x, y] = block.pop()
    return out


def to_16(image: Image.Image) -> Image.Image:
    px = image.load()
    out = Image.new('RGBA', (16, 16))
    out_px = out.load()
    for y in range(16):
        for x in range(16):
            cells = [px[2 * x + i, 2 * y + j] for i in (0, 1) for j in (0, 1)]
            alpha = sum(c[3] for c in cells)
            if alpha == 0:
                out_px[x, y] = (0, 0, 0, 0)
                continue
            r, g, b = (round(sum(c[k] * c[3] for c in cells) / alpha) for k in range(3))
            out_px[x, y] = (r, g, b, round(alpha / 4))
    return out


def outputs():
    keys = role_keys()
    missing = sorted(set(keys) - set(NAMES.values()))
    if len(keys) != len(NAMES) or missing:
        raise SystemExit(f'Los roles de Role.java no coinciden con NAMES: faltan {missing}')
    result = {}
    for source_name, key in NAMES.items():
        path = SOURCE / f'{source_name}.png'
        if not path.is_file():
            raise SystemExit(f'Falta el arte fuente {path}')
        with Image.open(path) as raw:
            large = to_32(raw.convert('RGBA'), source_name)
        result[Path('role') / f'{key}.png'] = large
        result[Path('role/small') / f'{key}.png'] = to_16(large)
    return result


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
            if not path.is_file() or Image.open(path).convert('RGBA').tobytes() != image.tobytes():
                stale.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            image.save(path)
    if stale:
        print('Iconos de rol ausentes o desactualizados:\n' + '\n'.join(stale))
        return 1
    print(f'{len(NAMES)} iconos de rol {"comprobados" if checking else "generados"}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
