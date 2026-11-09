#!/usr/bin/env python3
"""Modelos 3D de las armas en la mano (v62, K4 fase 1).

Cada arma se «extruye» a partir de su plantilla de tools/textures/gen_items.py: cada píxel opaco del icono se
convierte en un bloque cuyo grosor depende del material (hoja fina, guarda, empuñadura y gemas con volumen).
Así el modelo usa la misma textura que el icono, conserva su orientación (las transformaciones de mano de
vanilla encajan) y el arte sigue siendo el original del mod. El cliente muestra el icono 2D en el inventario
y este modelo en la mano (dev.aptitudes.client.model.HandheldModels).

Salida: models/item/<arma>_3d.json (formato de modelo de bloque de Minecraft, editable en Blockbench).
Uso:  python3 tools/models/gen_weapon_models.py src/main/resources/assets/aptitudes/models/item [--check]
      [--preview hoja.png]
"""
import argparse
import importlib.util
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
_spec = importlib.util.spec_from_file_location('gen_items', ROOT / 'tools/textures/gen_items.py')
gen_items = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(gen_items)

# Armas de rol con modelo en la mano (además de las 40 de evolución, que se añaden abajo).
ROLE_WEAPONS = ('berserker_axe', 'dagger', 'katana', 'spear', 'arcane_staff', 'oak_staff', 'holy_hammer',
                'plasma_gun', 'arcane_revolver', 'wrench', 'hunting_knife', 'lute')

# Grosor (en píxeles de modelo) por material; las plantillas pueden ajustarlo.
THICKNESS = {'A': 1, 'S': 1, 'G': 2, 'L': 2, 'W': 2, 'J': 3, 'D': 2, 'K': 2, 'R': 2, 'C': 2, 'B': 2, 'V': 2, 'Y': 2, 'F': 2}
BY_TEMPLATE = {
    'great_sword': {'G': 3, 'J': 3},
    'focus_master': {'J': 4, 'G': 2},
    'focus_ultimate': {'J': 4, 'G': 3, 'D': 2},
    'staff_orb': {'J': 4},
    'staff_leaf': {'V': 3, 'J': 3},
    'hammer': {'A': 5, 'L': 5, 'G': 3, 'J': 3},
    'axe': {'A': 1},
    'gun': {'D': 3, 'A': 2, 'L': 3, 'J': 2},
    'revolver': {'S': 2, 'G': 2, 'L': 3},
    'wrench': {'S': 2},
    'lute': {'A': 4, 'K': 2, 'G': 4, 'W': 2},
    'spear': {'A': 1, 'G': 2},
}

# Transformaciones de item/generated + item/handheld de vanilla (el modelo tiene la misma disposición que el icono).
DISPLAY = {
    'thirdperson_righthand': {'rotation': [0, -90, 55], 'translation': [0, 4.0, 0.5], 'scale': [0.85, 0.85, 0.85]},
    'thirdperson_lefthand': {'rotation': [0, 90, -55], 'translation': [0, 4.0, 0.5], 'scale': [0.85, 0.85, 0.85]},
    'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'firstperson_lefthand': {'rotation': [0, 90, -25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 13, 7], 'scale': [1, 1, 1]},
    'fixed': {'rotation': [0, 180, 0], 'scale': [1, 1, 1]},
    'gui': {'rotation': [30, 225, 0], 'scale': [0.625, 0.625, 0.625]},
}
NEIGHBOURS = ((1, 0), (-1, 0), (0, 1), (0, -1))


def weapons():
    names = list(ROLE_WEAPONS)
    names += [n for n in gen_items.ITEMS if n.endswith(('_weapon_master', '_weapon_ultimate'))]
    missing = [n for n in names if n not in gen_items.ITEMS]
    if missing:
        raise SystemExit(f'Armas sin plantilla en gen_items.py: {missing}')
    return names


def depth_map(name):
    """Grosor por píxel (0 = vacío) a partir de la plantilla y del icono ya dibujado."""
    template, colors = gen_items.ITEMS[name]
    grid = [row.ljust(16, '.')[:16] for row in gen_items.T[template]]
    image = gen_items.render(gen_items.T[template], colors)
    thick = dict(THICKNESS)
    thick.update(BY_TEMPLATE.get(template, {}))

    def material(x, y):
        if 0 <= x < 16 and 0 <= y < 16 and grid[y][x].isalpha():
            return grid[y][x].upper()
        return None

    depth = [[0] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if image.getpixel((x, y))[3] < 255:
                continue  # halos y destellos translúcidos: solo en el icono
            m = material(x, y)
            if m is None:  # contorno: hereda el material vecino, igual que gen_items.render
                for dx, dy in NEIGHBOURS:
                    m = material(x + dx, y + dy)
                    if m:
                        break
            if m:
                depth[y][x] = thick.get(m, 2)
    return depth


def r(v):
    return round(v, 4)


def model(name):
    depth = depth_map(name)
    elements = []
    for y in range(16):
        x = 0
        while x < 16:
            t = depth[y][x]
            if not t:
                x += 1
                continue
            x1 = x
            while x1 < 16 and depth[y][x1] == t:
                x1 += 1
            z0, z1 = 8 - t / 2, 8 + t / 2
            faces = {
                'north': {'uv': [x1, y, x, y + 1], 'texture': '#layer0'},
                'south': {'uv': [x, y, x1, y + 1], 'texture': '#layer0'},
                'east': {'uv': [x1 - 1, y, x1, y + 1], 'texture': '#layer0'},
                'west': {'uv': [x, y, x + 1, y + 1], 'texture': '#layer0'},
            }
            # Arriba/abajo solo si algún vecino de la fila contigua es más fino (si no, la cara queda oculta).
            if any(y == 0 or depth[y - 1][i] < t for i in range(x, x1)):
                faces['up'] = {'uv': [x, y, x1, y + 1], 'texture': '#layer0'}
            if any(y == 15 or depth[y + 1][i] < t for i in range(x, x1)):
                faces['down'] = {'uv': [x, y, x1, y + 1], 'texture': '#layer0'}
            elements.append({'from': [x, 15 - y, r(z0)], 'to': [x1, 16 - y, r(z1)], 'faces': faces})
            x = x1
    return {
        '__comment': 'Generado por tools/models/gen_weapon_models.py a partir de gen_items.py; no editar a mano.',
        'textures': {'layer0': f'aptitudes:item/{name}', 'particle': f'aptitudes:item/{name}'},
        'gui_light': 'front',
        'elements': elements,
        'display': DISPLAY,
    }


def render_json(name):
    """JSON compacto: un bloque por línea (diffs pequeños; Blockbench lo abre igual)."""
    data = model(name)
    lines = ['{']
    for i, (key, value) in enumerate(data.items()):
        comma = ',' if i < len(data) - 1 else ''
        if key in ('elements', 'display'):
            items = value if key == 'elements' else [{k: v} for k, v in value.items()]
            body = ',\n'.join('    ' + (json.dumps(it, separators=(',', ':')) if key == 'elements'
                                       else json.dumps(it, separators=(',', ':'))[1:-1]) for it in items)
            open_, close = ('[', ']') if key == 'elements' else ('{', '}')
            lines.append(f'  {json.dumps(key)}: {open_}\n{body}\n  {close}{comma}')
        else:
            lines.append(f'  {json.dumps(key)}: {json.dumps(value, separators=(",", ":"))}{comma}')
    lines.append('}')
    text = '\n'.join(lines) + '\n'
    assert json.loads(text) == data
    return text


def preview(names, out):
    """Vista isométrica simple (solo para revisar volúmenes; el juego usa su propio renderizador)."""
    from PIL import Image, ImageDraw
    cell, scale = 120, 6
    cols = 8
    rows = math.ceil(len(names) / cols)
    sheet = Image.new('RGB', (cols * cell, rows * cell), (40, 42, 54))
    for i, name in enumerate(names):
        template, colors = gen_items.ITEMS[name]
        tex = gen_items.render(gen_items.T[template], colors)
        depth = depth_map(name)
        draw = ImageDraw.Draw(sheet)
        ox, oy = (i % cols) * cell + 20, (i // cols) * cell + 14
        # Proyección oblicua: el fondo (z) se desplaza hacia arriba a la derecha; se pinta de atrás adelante.
        for layer in range(8, -1, -1):
            for y in range(16):
                for x in range(16):
                    t = depth[y][x]
                    if not t or layer >= t:
                        continue
                    c = tex.getpixel((x, y))
                    shade = 1.0 if layer == 0 else 0.62
                    col = tuple(int(c[k] * shade) for k in range(3))
                    px = ox + x * scale + layer * 2
                    py = oy + y * scale - layer * 2
                    draw.rectangle([px, py, px + scale - 1, py + scale - 1], fill=col)
    sheet.save(out)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('folder', type=Path)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--preview', type=Path)
    args = parser.parse_args()
    names = weapons()
    stale = []
    for name in names:
        path = args.folder / f'{name}_3d.json'
        data = render_json(name)
        if args.check:
            if not path.is_file() or path.read_text(encoding='utf-8') != data:
                stale.append(str(path))
        else:
            path.write_text(data, encoding='utf-8', newline='\n')
    if args.preview:
        preview(names, args.preview)
    if stale:
        raise SystemExit('Modelos 3D ausentes o desactualizados:\n' + '\n'.join(stale))
    print(f'{len(names)} modelos 3D {"comprobados" if args.check else "generados"} en {args.folder}')


if __name__ == '__main__':
    main()
