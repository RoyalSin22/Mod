#!/usr/bin/env python3
"""Iconos originales de Sendas, dibujados por capas de pixel art (Pillow).

Uso: python3 tools/textures/gen_node_icons.py <textures/gui/node> [--preview hoja.png]
     python3 tools/textures/gen_node_icons.py <textures/gui/node> --check

Solo escribe los PNG en la carpeta indicada y, si se pide, la hoja --preview.
--check es de solo lectura: no regenera ni repara los archivos que comprueba.
Las plantillas usan letras de material; las minúsculas son reflejos y * es blanco.
"""

import argparse
import colorsys
from pathlib import Path
import re

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / 'src/main/java/dev/aptitudes/role/RoleTree.java'
ROLES = (
    'berserker', 'mage', 'assassin', 'herbalist', 'engineer', 'lancer',
    'swordsman', 'paladin', 'necromancer', 'druid', 'merchant', 'monk',
    'elementalist', 'gunslinger', 'weapon_summoner', 'hunter', 'bard',
    'warlock', 'tamer', 'shapeshifter',
)
PATH_COLORS = {'a': '#E57373', 'b': '#64B5F6', 'c': '#81C784'}
STEPS = ('4', '5x', '5y', '6')
MATERIALS = {
    'B': '#DED7BD',  # hueso y colmillos
    'W': '#98704B',  # mangos y cuero
    'G': '#E8BC52',  # oro
    'K': '#464052',  # huecos y acero oscuro
    'I': '#C3D8E2',  # acero y alas
}
EFFECT_COLORS = {
    'LIFESTEAL': '#DF4655', 'CRIT': '#FFD16B', 'EXECUTE': '#C2CED9',
    'THORNS': '#9EB576', 'DAMAGE_REDUCTION': '#739ED4', 'DODGE': '#B6A5ED',
    'MANA_REGEN': '#409FEA', 'RESOURCE_GAIN': '#DFA64B', 'REGEN': '#EE6D86',
    'ON_HIT_POISON': '#8FCB4C', 'ON_HIT_SLOW': '#86DCEC',
    'ON_HIT_FIRE': '#F28A38', 'ON_HIT_WITHER': '#87749D',
    'MINION_POWER': '#DBD1B5', 'KNOCKBACK': '#77A3B4', 'SPEED': '#83C9AF',
    'MAX_HEALTH': '#E3536C', 'ATTACK_SPEED': '#CADDE8', 'LUCK': '#64C779',
}


def glyph(*rows):
    """Núcleo de 10×10 centrado en una plantilla de letras de 16×16."""
    if len(rows) != 10 or any(len(row) != 10 for row in rows):
        raise ValueError('Cada glifo necesita 10 filas de 10 letras')
    return ('................',) * 3 + tuple('...' + r + '...' for r in rows) + (
        '................',) * 3


# Arte original: las siluetas y sus reflejos se definen aquí, nunca en los PNG.
GLYPHS = {
    # Gota roja y colmillo de hueso en su borde derecho.
    'LIFESTEAL': glyph(
        '....a.....', '...aAA.Bb.', '...aAA.BB.', '..aAAAA.B.', '..aAAAA.B.',
        '.aAAAAAA..', '.aAAAAAA..', '.AAAAAAA..', '..AAAAA...', '...AAA....'),
    # Estrella asimétrica con núcleo luminoso.
    'CRIT': glyph(
        '....a.....', '.a..a..A..', '..aAaAA...', '...aAA....', 'aaa*AAAAAA',
        '..AAAAA...', '..AA.AAA..', '.AA...AA..', '.......AA.', '........A.'),
    # Hacha de hoja ancha sobre una pequeña calavera.
    'EXECUTE': glyph(
        '...W.AaA..', '...WaaAAA.', '...WAAAAA.', '...W.AAA..', '...W......',
        '...W.BbBB.', '...W.BKBK.', '...W.BBBB.', '..WW..BB..', '..W...BB..'),
    'THORNS': glyph(
        '....a.....', '.a..a..a..', '.aA.a.AA..', '..AAAAA...', 'a..AAA..AA',
        'aAAAAAAA..', '..AAAAA...', '.AAA.AAA..', '.A..A..AA.', '....A.....'),
    'DAMAGE_REDUCTION': glyph(
        '.aaaaAAA..', '.aIIIAAA..', '.aIIAAAA..', '.aIAAAAA..', '.aAAAAAA..',
        '.AAAAAAA..', '..AAAAA...', '..AAAAA...', '...AAA....', '....A.....'),
    # Silueta inclinada con tres estelas separadas.
    'DODGE': glyph(
        '......aa..', '......AA..', '..........', 'AA...AAA..', '....aAAAA.',
        'AAA.AAA.A.', '....AAA...', 'AA..A.AA..', '...AA..AA.', '..AA....A.'),
    'MANA_REGEN': glyph(
        '....a.....', '...aa.....', '...aAA....', '..aaAAA...', '..aAAAA...',
        '.aaAAAAA..', '.aAA*AAA..', '.aAAAAAA..', '..AAAAA...', '...AAA....'),
    # Anillo de carga incompleto, con tres cargas brillantes.
    'RESOURCE_GAIN': glyph(
        '...aaa....', '..aa.AA...', '.aA...AA..', '.a.....A..', '.a..GG....',
        '.A..Gg....', '.A.....A..', '.AA...AA..', '..AA.AA...', '...AAA....'),
    # Corazón y destello en la esquina superior derecha.
    'REGEN': glyph(
        '........*.', '.aa..aa.*.', 'aAAAaAAA**', 'aAAAAAAA*.', 'aAAAAAAA..',
        '.AAAAAA...', '..AAAA....', '...AA.....', '..........', '..........'),
    # Gota de veneno con burbujas, distinta de la gota de maná.
    'ON_HIT_POISON': glyph(
        '....a...a.', '..a.aa....', '....aA....', '...aAAA...', '...aAAA...',
        '..aAAAAA..', '..aAKAAA..', '..AAAKAA..', '...AAAA...', '....AA....'),
    'ON_HIT_SLOW': glyph(
        '.A..a..A..', '..A.a.A...', 'A..AAA..A.', '.AAAAAAA..', '...AAA....',
        'aaaa*AAAAA', '...AAA....', '.AAAAAAA..', 'A..AAA..A.', '..A.A.A...'),
    'ON_HIT_FIRE': glyph(
        '.....a....', '....aA....', '....aAA...', '..a.AAA...', '..aAAAA.A.',
        '.aAAAAAAA.', '.aAAGGAAA.', '.AAAGgAA..', '..AAGAA...', '...AAA....'),
    'ON_HIT_WITHER': glyph(
        '...aaa....', '..aAAAA...', '.aAAAAAA..', '.aAKAAKA..', '.AAKAAKA..',
        '..AAKAA...', '...AAA....', '..AKAKA...', '...A.A....', '..A...A...'),
    'MINION_POWER': glyph(
        'aA......aA', 'AAA....AAA', '.AAA..AAA.', '..AAAAAA..', '...AAAA...',
        '...AAAA...', '..AAAAAA..', '.AAA..AAA.', 'AAA....AAA', 'AA......AA'),
    'KNOCKBACK': glyph(
        '....aa....', '...a..A...', '....AA....', '....AA....', '..AAAAAA..',
        '....AA....', 'A...AA...A', 'AA..AA..AA', '.AAAAAAAA.', '..AAAAAA..'),
    # Bota con ala de tres plumas.
    'SPEED': glyph(
        'IiI.......', '.IIiI.AA..', '..IIIIAA..', '...IIAAA..', '..II.AAA..',
        '.....AAA..', '.....AAAA.', '....aAAAAA', '....AAAAAA', '...WWWWWWW'),
    'MAX_HEALTH': glyph(
        '.aa..aa...', 'aAAAaAAA..', 'aAAAAAAA..', 'aAAAAAAA..', '.AAAAAA...',
        '..AAAA..B.', '...AA..BBB', '........B.', '..........', '..........'),
    'ATTACK_SPEED': glyph(
        'aA......aA', '.aA....aA.', '..aA..aA..', '...AAAA...', '....AA....',
        '...AAAA...', '..AA..AA..', '.GWW..WWG.', '..W....W..', '.W......W.'),
    'LUCK': glyph(
        '..aa..aa..', '.aAAaaAAA.', '.aAAAAAAA.', '..AAAAAA..', '...AAAA...',
        '..AAAAAA..', '.aAAAAAAA.', '.AAAAAAA..', '....AW....', '.....WW...'),
}


def ramp(base):
    """Cinco tonos: contorno, sombra, base, luz y reflejo (luz arriba-izquierda)."""
    rgb = tuple(int(base[i:i + 2], 16) / 255 for i in (1, 3, 5))
    h, s, v = colorsys.rgb_to_hsv(*rgb)
    result = []
    for hue, sat, value in ((.025, 1.1, .25), (.015, 1.05, .62),
                            (0, 1, 1), (-.01, .8, 1.2), (-.02, .45, 1.4)):
        color = colorsys.hsv_to_rgb((h + hue) % 1, min(1, s * sat), min(1, v * value))
        result.append(tuple(round(c * 255) for c in color) + (255,))
    return result


def paint(template, materials, outline=True):
    """Capa RGBA sin suavizado; contorno ortogonal oscuro, de un solo píxel."""
    if len(template) != 16 or any(len(row) != 16 for row in template):
        raise ValueError('Plantilla distinta de 16×16')
    palettes = {letter: ramp(color) for letter, color in materials.items()}
    pixels = {(x, y): letter for y, row in enumerate(template)
              for x, letter in enumerate(row) if letter != '.'}
    layer = Image.new('RGBA', (16, 16))
    if outline:
        for x, y in pixels:
            for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
                xx, yy = x + dx, y + dy
                if 0 <= xx < 16 and 0 <= yy < 16 and (xx, yy) not in pixels:
                    layer.putpixel((xx, yy), (18, 20, 29, 255))
    for (x, y), letter in pixels.items():
        if letter == '*':
            color = (255, 249, 220, 255)
        else:
            # Luz en bordes superiores/izquierdos, sombra abajo/derecha.
            lit = (x - 1, y) not in pixels or (x, y - 1) not in pixels
            shade = 3 if lit else (1 if x + y > 17 else 2)
            color = palettes[letter.upper()][4 if letter.islower() else shade]
        layer.putpixel((x, y), color)
    return layer


def frame(step):
    """Marco de letras: muesca lateral para S5, doble aro y cantoneras en S6."""
    grid = [['.'] * 16 for _ in range(16)]
    for i in range(2, 14):
        grid[1][i] = grid[i][1] = 'a'
        grid[14][i] = grid[i][14] = 'A'
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        grid[y][x] = 'A'
    if step in ('5x', '5y'):
        edge, inset = (1, 2) if step == '5x' else (14, 13)
        for y in range(6, 10):
            grid[y][edge] = '.'
            grid[y][inset] = 'a' if step == '5x' else 'A'
    if step == '6':
        for i in range(3, 13):
            grid[2][i] = grid[i][2] = 'a'
            grid[13][i] = grid[i][13] = 'A'
        for x, y, dx, dy in ((1, 1, 1, 1), (14, 1, -1, 1),
                              (1, 14, 1, -1), (14, 14, -1, -1)):
            grid[y][x] = 'g'
            grid[y][x + dx] = grid[y + dy][x] = 'G'
        # Destello extra sobre el borde; no tapa la silueta central.
        for x, y in ((11, 0), (10, 1), (11, 1), (12, 1), (11, 2)):
            grid[y][x] = '*'
    return tuple(''.join(row) for row in grid)


def render(effect, path, step):
    icon = Image.new('RGBA', (16, 16))
    ImageDraw.Draw(icon).rectangle((2, 2, 13, 13), fill=(27, 30, 43, 255))
    border = frame(step)
    colors = {'A': PATH_COLORS[path], 'G': '#E8BC52'}
    icon.alpha_composite(paint(border, colors))
    icon.alpha_composite(paint(GLYPHS[effect], {**MATERIALS, 'A': EFFECT_COLORS[effect]}))
    icon.alpha_composite(paint(border, colors, outline=False))
    return icon


def java_effects(source=JAVA):
    """Lee paths/deeper y los alias reales de PathEffect; exige los 20 roles completos."""
    src = source.read_text(encoding='utf-8')
    src = re.sub(r'/\*.*?\*/|//[^\n]*', '', src, flags=re.S)
    aliases = dict(re.findall(r'\b(\w+)\s*=\s*PathEffect\.(\w+)', src))
    result = {}
    for method in ('paths', 'deeper'):
        pattern = (rf'\b{method}\s*\(\s*Role\.(\w+)\s*,\s*'
                   r'of\(([^)]*)\)\s*,\s*of\(([^)]*)\)\s*,\s*of\(([^)]*)\)\s*\)')
        roles = {}
        for match in re.finditer(pattern, src):
            role = match[1].lower()
            if role in roles:
                raise ValueError(f'RoleTree: rol duplicado en {method}: {role}')
            paths = []
            for group in match.groups()[1:]:
                effects = []
                for token in group.split(','):
                    token = token.strip()
                    effect = (token.removeprefix('PathEffect.') if token.startswith('PathEffect.')
                              else aliases.get(token))
                    if effect not in GLYPHS:
                        raise ValueError(f'RoleTree: efecto sin glifo en {role}/{method}: {token}')
                    effects.append(effect)
                if len(effects) != 3:
                    raise ValueError(f'RoleTree: se esperaban 3 efectos en {role}/{method}')
                paths.append(effects)
            roles[role] = paths
        if set(roles) != set(ROLES):
            missing = ', '.join(sorted(set(ROLES) - roles.keys())) or 'ninguno'
            extra = ', '.join(sorted(roles.keys() - set(ROLES))) or 'ninguno'
            raise ValueError(f'RoleTree: {method} necesita los 20 roles; faltan: {missing}; sobran: {extra}')
        result[method] = roles
    return result['deeper']


def catalog(effects):
    return [(f'{role}_{path}{step}', effect, path, step)
            for role in ROLES for path, values in zip('abc', effects[role])
            for step, effect in zip(STEPS, (*values, values[0]))]


def check(output, entries):
    """Verifica los archivos existentes sin escribir ni regenerar PNG."""
    errors = []
    for node_id, _, _, _ in entries:
        path = output / f'{node_id}.png'
        try:
            with Image.open(path) as img:
                if img.format != 'PNG' or img.size != (16, 16) or img.mode != 'RGBA':
                    errors.append(f'{path.name}: se requiere PNG RGBA 16×16, '
                                  f'es {img.format} {img.mode} {img.size}')
                img.load()
        except (OSError, ValueError) as exc:
            errors.append(f'{path.name}: {exc}')
    if errors:
        raise ValueError('Comprobación fallida:\n' + '\n'.join(errors))
    print(f'OK: {len(entries)} PNG de 16×16 RGBA (20 roles × 3 sendas × 4 escalones).')


def preview(output, entries, destination):
    """Una fila por rol: A/B/C, cada una con S4, S5x, S5y y S6; ampliación ×4."""
    font = ImageFont.load_default(size=11)
    cell_w = max(100, max(round(font.getlength(entry[0])) for entry in entries) + 12)
    cell_h, header = 96, 40
    sheet = Image.new('RGB', (cell_w * 12, header + cell_h * 20), '#151923')
    draw = ImageDraw.Draw(sheet)
    draw.text((10, 8), 'APTITUDES | SENDAS | S4 / S5x / S5y / S6 | 16x16 -> x4',
              font=font, fill='#E8BC52')
    for index, (node_id, _, path, step) in enumerate(entries):
        x, y = index % 12 * cell_w, header + index // 12 * cell_h
        if index % 4 == 0:
            draw.line((x, y, x, y + cell_h - 6), fill=PATH_COLORS[path])
        with Image.open(output / f'{node_id}.png') as icon:
            enlarged = icon.resize((64, 64), Image.Resampling.NEAREST)
            sheet.paste(enlarged, (x + (cell_w - 64) // 2, y + 2), enlarged)
        draw.text((x + (cell_w - font.getlength(node_id)) / 2, y + 72), node_id,
                  font=font, fill='#F0EADF')
    destination.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(destination)
    print(f'Vista previa ×4: {destination}')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('output', type=Path, help='Carpeta de texturas gui/node')
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--preview', type=Path, metavar='HOJA.png')
    mode.add_argument('--check', action='store_true', help='Solo comprueba; no escribe archivos')
    args = parser.parse_args()
    try:
        entries = catalog(java_effects())
        if args.check:
            check(args.output, entries)
        else:
            # Evita que --preview sobrescriba un icono cuando cae en la carpeta de salida.
            if args.preview and args.preview.resolve() in {
                    (args.output / f'{entry[0]}.png').resolve() for entry in entries}:
                raise ValueError('--preview no puede sobrescribir un icono del catálogo')
            args.output.mkdir(parents=True, exist_ok=True)
            for node_id, effect, path, step in entries:
                render(effect, path, step).save(args.output / f'{node_id}.png')
            check(args.output, entries)
            if args.preview:
                preview(args.output, entries, args.preview)
    except (OSError, ValueError) as exc:
        parser.exit(1, f'Error: {exc}\n')


if __name__ == '__main__':
    main()
