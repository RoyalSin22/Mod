#!/usr/bin/env python3
"""Atlas de iconos del árbol y del HUD. Los 480 PNG de Sendas se generan aparte."""

import argparse
from io import BytesIO
import json
from pathlib import Path
import re

from PIL import Image, ImageDraw

from gen_node_icons import EFFECT_COLORS, GLYPHS, KEYSTONE_COLORS, KEYSTONE_GLYPHS, MATERIALS, glyph, paint

ROLE_DIR = Path(__file__).resolve().parents[2] / 'src/main/java/dev/aptitudes/role'
SIZE = 512
TILE = 16


def symbol(rows):
    """Centra un sello de 5×5 a dos píxeles por punto."""
    template = [row.replace('x', 'AA').replace('.', '..') for row in rows.split()]
    return glyph(*(row for row in template for _ in range(2)))


SEALS = {role: symbol(rows) for role, rows in {
    'berserker': '..xxx x.xxx .xxxx ..x.. .x...',
    'mage': '..x.. ..x.. xxxxx .xxx. .x.x.',
    'assassin': '....x ...xx ..xx. .xx.. xx...',
    'herbalist': '.x... .xx.x ..xxx ..x.. ..x..',
    'engineer': 'x.x.x .xxx. xx.xx .xxx. x.x.x',
    'lancer': '....x ...xx ..xxx .x... x....',
    'swordsman': '....x ...xx x.xx. .xx.. x..x.',
    'paladin': 'xxxxx xx.xx x...x .x.x. ..x..',
    'necromancer': '.xxx. xxxxx x.x.x .xxx. .x.x.',
    'druid': '..x.. .xxx. xx.xx ..x.. .xxx.',
    'merchant': '.xxx. xx.xx x.x.x xx.xx .xxx.',
    'monk': 'x.x.x xxxxx .xxxx .xxxx ..xx.',
    'elementalist': '..x.. .x.x. x...x .x.x. ..x..',
    'shapeshifter': 'x...x xxxxx xx.xx .xxx. ..x..',
    'gunslinger': '..xxx .xxxx xx... xx... x....',
    'weapon_summoner': 'x...x xx.xx .xxx. .xxx. xx.xx',
    'hunter': '.x... x.x.. x..x. x.x.x .x.x.',
    'bard': '..xxx ..x.x ..x.. xxx.. xxx..',
    'warlock': '...x. .x.x. .xxxx xxx.x .xxx.',
    'tamer': 'x.x.x x...x ..x.. .xxx. .xxx.',
}.items()}

SIGNS = {name: symbol(rows) for name, rows in {
    'blade': '...xx ...xx x.xx. .xx.. x..x.',
    'bolt': '...x. ..xx. .xxx. ..x.. .x...',
    'beam': '....x ...x. x.x.x .x.x. x.x..',
    'zone': '.x.x. .x.x. ..x.. x.x.x .xxx.',
    'vortex': '.xxxx x...x x.xxx x.... .xxx.',
    'heal': '..x.. ..x.. xxxxx ..x.. ..x..',
    'aim': '.xxx. x.x.x xx.xx x.x.x .xxx.',
    'aura': '..x.. .x.x. x.x.x .x.x. ..x..',
    'smoke': '..xx. .xxxx xxx.x xxxxx .xxx.',
    'time': 'xxxxx .x.x. ..x.. .x.x. xxxxx',
    'chain': '...xx ..x.x .xxx. x.x.. xx...',
    'flask': '.xxx. ..x.. .x.x. xxxxx .xxx.',
    'wing': '....x ..xxx .xxx. .xx.. x....',
    'music': '..xxx ..x.x ..x.. xxx.. xxx..',
    'coin': '.xxx. xx.xx x.x.x xx.xx .xxx.',
    'sprout': '.x... .xx.x ..xxx ..x.. ..x..',
    'skull': '.xxx. xxxxx x.x.x .xxx. .x.x.',
    'spark': '..x.. x.x.x .xxx. x.x.x ..x..',
    'dash': '.xx.. ..xx. xxxxx ..xx. .xx..',
}.items()}

# Familias de iconos anteriores. Los nodos que comparten efecto comparten un símbolo.
FAMILIES = {
    'blade': r'SWORD|AXE|TRIDENT|MACE|SHEARS|GRINDSTONE',
    'aim': r'SPYGLASS|TARGET|BOW|COMPASS|ENDER_EYE',
    'beam': r'ARROW|END_ROD|BLAZE_ROD',
    'bolt': r'FIREWORK|FIRE_CHARGE|GUNPOWDER|WIND_CHARGE',
    'time': r'CLOCK|SUGAR|DAYLIGHT',
    'chain': r'CHAIN|TRIPWIRE|LEAD|FISHING_ROD|PISTON',
    'flask': r'BOTTLE|BUCKET|POTION',
    'wing': r'FEATHER|RABBIT_FOOT|PHANTOM|BOOTS|SADDLE',
    'music': r'MUSIC|NOTE_BLOCK|GOAT_HORN|JUKEBOX|BELL',
    'coin': r'EMERALD|GOLD_(BLOCK|INGOT|NUGGET)|BUNDLE|PAPER',
    'sprout': r'ROOTS|SAPLING|PETALS|OAK_LOG|SPORE|FERN|BAMBOO|LILY_PAD|CACTUS|BERRIES|FUNGUS|NETHER_WART',
    'skull': r'HEAD|SKULL|BONE|ROTTEN_FLESH|WITHER_ROSE',
    'smoke': r'DYE|OBSIDIAN|SLIME|SOUL_TORCH',
    'spark': r'PRISMARINE|AMETHYST|LAPIS|REDSTONE|GLOWSTONE|NETHER_STAR|BLAZE_POWDER',
    'aura': r'BEACON|CONDUIT|HEART_OF_THE_SEA|SOUL_LANTERN',
    'heal': r'APPLE|CARROT|MELON|GHAST_TEAR|COOKED|BEEF|MUTTON',
}
TEMPLATES = {
    'NOVA': 'spark', 'BEAM': 'beam', 'CONE': 'blade', 'DASH': 'dash', 'ZONE': 'zone',
    'VORTEX': 'vortex', 'AURA': 'aura', 'HEAL': 'heal', 'SHIELD': 'DAMAGE_REDUCTION', 'SUMMON': 'skull',
}


def source(name):
    return re.sub(r'/\*.*?\*/|//[^\n]*', '', (ROLE_DIR / f'{name}.java').read_text(encoding='utf-8'), flags=re.S)


def entries():
    roles = {key.lower(): '#' + color for key, color in re.findall(
        r'(\w+)\(RoleCategory\.\w+,\s*0x([0-9A-Fa-f]{6})', source('Role'))}
    if roles.keys() != SEALS.keys():
        raise ValueError('Los sellos deben cubrir todos los roles')

    tree = source('RoleTree')
    base = {}
    for role, block in re.findall(r'\bdefine\(Role\.(\w+),([^;]+)\);', tree):
        base[role.lower()] = re.findall(r'(passive|active)Node\("([a-z_0-9]+)"', block)
    evolved = {role.lower(): re.sub(r'\ba\((\w+),\s*[\w.]+\)', r'\1', row).split(',')
               for role, row in re.findall(r'\brole\(Role\.(\w+),\s*0x[\da-fA-F]+,([^;]+)\);', source('EvolvedTree'))}
    if base.keys() != roles.keys() or evolved.keys() != roles.keys():
        raise ValueError('Cobertura incompleta del árbol')
    items = dict(re.findall(r'\bput\("([a-z_0-9]+)",\s*Items\.(\w+)\)', source('NodeIcons')))

    catalog = {}
    for role, tint in roles.items():
        seal = SEALS[role]
        for badge in ('core', 'signature', 'ultimate'):
            catalog[f'role/{role}/{badge}'] = (seal, tint, badge, 0)
        catalog[f'{role}_legend'] = (seal, '#AA80E1', 'legend', 0)
        if len(base[role]) != 9 or len(evolved[role]) != 10:
            raise ValueError(f'Número de nodos inesperado en {role}')
        active = 0
        for kind, key in base[role]:
            item = items.get(key, '')
            family = next((family for family, match in FAMILIES.items() if re.search(match, item)), None)
            mark = 0
            if kind == 'active':
                active += 1
                mark = active
            tile = GLYPHS['DAMAGE_REDUCTION'] if re.search(r'SHIELD|CHESTPLATE|HELMET|ARMOR', item) else SIGNS.get(family, seal)
            catalog[key] = (tile, tint, kind, mark)
        for suffix, sign in (('stat1', 'heal'), ('stat2', 'aura'), ('haste', 'time'),
                             ('efficiency', 'bolt'), ('mastery', 'spark')):
            catalog[f'{role}_{suffix}'] = (SIGNS[sign], tint, 'passive', 0)
        for index, token in enumerate(evolved[role]):
            tier = 'm' if index < 5 else 'u'
            token = token.strip()
            template = TEMPLATES.get(token, token)
            tile = GLYPHS.get(template) or SIGNS[template]
            catalog[f'{role}_{tier}{index % 5 + 1}'] = (
                tile, EFFECT_COLORS.get(token, tint), tier, index % 5 - 2 if index % 5 >= 3 else 0)
    for name, template in KEYSTONE_GLYPHS.items():
        catalog['keystone/' + name.lower()] = (template, KEYSTONE_COLORS[name], 'legend', 0)
    if len(catalog) != len(roles) * 28 + len(KEYSTONE_GLYPHS):
        raise ValueError('Hay identificadores de icono duplicados o ausentes')
    return catalog


def render(tile, tint, frame, mark):
    r, g, b = (int(tint[i:i + 2], 16) for i in (1, 3, 5))
    light = '#' + ''.join(f'{max(82, min(230, c + 35)):02x}' for c in (r, g, b))
    ink = Image.new('RGBA', (16, 16))
    draw = ImageDraw.Draw(ink)
    if frame in ('core', 'signature', 'ultimate', 'legend'):
        draw.polygon(((5, 0), (10, 0), (15, 5), (15, 10), (10, 15), (5, 15), (0, 10), (0, 5)),
                     fill=(18, 20, 30, 255), outline=light)
        if frame in ('signature', 'ultimate', 'legend'):
            gold = '#e7ba73' if frame != 'legend' else '#e3b3ff'
            draw.line((5, 0, 10, 0), fill=gold)
            draw.line((5, 15, 10, 15), fill=gold)
            if frame == 'ultimate':
                draw.point(((0, 7), (0, 8), (15, 7), (15, 8)), fill=gold)
    elif frame in ('m', 'u'):
        draw.line((2, 1, 5, 1), fill='#a3c8ce' if frame == 'm' else '#e7ba73')
        if frame == 'u':
            draw.line((10, 1, 13, 1), fill='#e7ba73')
    ink.alpha_composite(paint(tile, {**MATERIALS, 'A': light}))
    for bit in range(mark):
        draw.line((2 + bit * 3, 14, 3 + bit * 3, 14), fill=light)
    return ink


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('output', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    atlas = Image.new('RGBA', (SIZE, SIZE))
    indices = {}
    for index, (key, icon) in enumerate(entries().items()):
        indices[key] = index
        atlas.paste(render(*icon), (index % (SIZE // TILE) * TILE, index // (SIZE // TILE) * TILE))
    encoded = BytesIO()
    atlas.save(encoded, format='PNG')
    files = {'atlas.png': encoded.getvalue(), 'atlas.json': (json.dumps(indices, indent=2) + '\n').encode()}
    if not args.check:
        args.output.mkdir(parents=True, exist_ok=True)
    for name, data in files.items():
        path = args.output / name
        if args.check:
            if not path.exists() or path.read_bytes() != data:
                parser.exit(1, f'Atlas ausente o desactualizado: {path}\n')
        else:
            path.write_bytes(data)
    print(f'{len(indices)} iconos del árbol y HUD ' + ('comprobados' if args.check else 'generados'))


if __name__ == '__main__':
    main()
