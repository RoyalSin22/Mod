#!/usr/bin/env python3
"""Glifos de rol para textos y tooltips (v62, K4 fase 2).

Recorta el sello «core» de cada rol del atlas del árbol (gen_tree_atlas.py) y crea una fuente de mapa de bits:
el carácter U+E000 + ordinal del rol (orden de Role.java) dibuja su icono dentro de cualquier texto.
Vuelve a ejecutarlo si cambia el atlas o se añade un rol (siempre al final del enum).

Uso: python3 tools/textures/gen_role_glyphs.py src/main/resources/assets/aptitudes [--check]
"""
import argparse
import json
import re
from io import BytesIO
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ROLE_JAVA = ROOT / 'src/main/java/dev/aptitudes/role/Role.java'
TILE = 16
FIRST = 0xE000


def roles():
    text = ROLE_JAVA.read_text(encoding='utf-8')
    found = re.findall(r'^\s*([A-Z_]+)\(RoleCategory\.', text, re.MULTILINE)
    if len(found) < 20:
        raise SystemExit('No se encontraron los roles en Role.java')
    return [name.lower() for name in found]


def build(assets):
    atlas_dir = assets / 'textures/gui/tree'
    index = json.loads((atlas_dir / 'atlas.json').read_text(encoding='utf-8'))
    atlas = Image.open(atlas_dir / 'atlas.png').convert('RGBA')
    columns = atlas.width // TILE
    keys = roles()
    sheet = Image.new('RGBA', (TILE * len(keys), TILE), (0, 0, 0, 0))
    for i, key in enumerate(keys):
        slot = index.get(f'role/{key}/core')
        if slot is None:
            raise SystemExit(f'El atlas no tiene el sello de {key}')
        x, y = slot % columns * TILE, slot // columns * TILE
        sheet.paste(atlas.crop((x, y, x + TILE, y + TILE)), (i * TILE, 0))
    out = BytesIO()
    sheet.save(out, format='PNG', optimize=False)
    chars = ''.join(chr(FIRST + i) for i in range(len(keys)))
    # 16 px dibujados a 8 de alto: a escala de interfaz 2 se ven píxel a píxel.
    font = {'providers': [{'type': 'bitmap', 'file': 'aptitudes:font/role_icons.png',
                           'height': 8, 'ascent': 7, 'chars': [chars]}]}
    return {
        assets / 'textures/font/role_icons.png': out.getvalue(),
        assets / 'font/role_icons.json': (json.dumps(font, indent=2) + '\n').encode('utf-8'),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('assets', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    stale = []
    for path, data in build(args.assets).items():
        if args.check:
            if not path.is_file() or path.read_bytes() != data:
                stale.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
    if stale:
        raise SystemExit('Glifos ausentes o desactualizados:\n' + '\n'.join(stale))
    print(f'{len(roles())} glifos de rol {"comprobados" if args.check else "generados"}')


if __name__ == '__main__':
    main()
