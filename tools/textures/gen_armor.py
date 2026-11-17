#!/usr/bin/env python3
"""
Generador de texturas de las armaduras de rol (v25): 80 iconos (16×16) y 40 capas del modelo (64×32).

Tres estilos según el peso del rol (RoleArmor.weight):
  HEAVY  → placas de metal tintadas, remaches, ribetes dorados, visera, hombreras y cinturón con hebilla
  MEDIUM → cuero con escamas del color del rol y ribete fino
  LIGHT  → tela con capucha (cara abierta), túnica con fajín, puños dorados y botas de cuero

Capas del modelo: mapa UV estándar de Minecraft (64×32). layer_1 = casco, peto (con brazos) y botas;
layer_2 = grebas (cintura y piernas).

Uso:  python3 tools/textures/gen_armor.py <carpeta textures> [--preview salida.png]
      (<carpeta textures> = src/main/resources/assets/aptitudes/textures)
"""
import random
import sys

from PIL import Image

sys.path.insert(0, __file__.rsplit('/', 1)[0])
from gen_items import ramp, render, ROLE_COLORS  # noqa: E402

HEAVY = {'berserker', 'lancer', 'paladin'}
MEDIUM = {'swordsman', 'engineer', 'druid', 'necromancer', 'weapon_summoner', 'tamer'}

GOLD = '#e0b43a'
STEEL = '#9aa3ad'
LEATHER = '#6b4a35'
DARK = '#2b2d38'


def weight(role):
    return 'HEAVY' if role in HEAVY else 'MEDIUM' if role in MEDIUM else 'LIGHT'


def mix(a, b, t):
    ha, hb = a.lstrip('#'), b.lstrip('#')
    ca = [int(ha[i:i + 2], 16) for i in (0, 2, 4)]
    cb = [int(hb[i:i + 2], 16) for i in (0, 2, 4)]
    return '#%02x%02x%02x' % tuple(int(ca[i] * (1 - t) + cb[i] * t) for i in range(3))


# ----------------------------------------------------------------------------- Iconos (16×16)

ICONS = {
    ('HEAVY', 'helmet'): [
        "................",
        "................",
        ".....GGGGGG.....",
        "....AaaGAAAA....",
        "...AaAAGAAAAA...",
        "...AaAAGAAAAA...",
        "...AAKKKKKKAA...",
        "...AAAAJAAAAA...",
        "...AAA.....AA...",
        "...AA.......A...",
        "...GG.......G...",
        "................",
        "................",
        "................",
        "................",
        "................"],
    ('HEAVY', 'chestplate'): [
        "................",
        "..GGG......GGG..",
        ".GAaAG....GAAAG.",
        ".GAaAAGGGGAAAAG.",
        ".GAAAAAAAAAAAAG.",
        "..GAAAAJJAAAAG..",
        "...AAAAJJAAAA...",
        "...AAAAAAAAAA...",
        "...AAAAAAAAAA...",
        "...LLLLGGLLLL...",
        "...AAAAAAAAAA...",
        "...AAAAAAAAAA...",
        "....AAAAAAAA....",
        "................",
        "................",
        "................"],
    ('HEAVY', 'leggings'): [
        "................",
        "...LLLLGGLLLL...",
        "...AaAAAAAAAA...",
        "...AaAAAAAAAA...",
        "...AAAAAAAAAA...",
        "...AAAAG.AAAA...",
        "...AAAA..AAAA...",
        "...GAAA..AAAG...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...GGGG..GGGG...",
        "................",
        "................",
        "................"],
    ('HEAVY', 'boots'): [
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "...GGGG..GGGG...",
        "...AaAA..AAAA...",
        "...AaAA..AAAA...",
        "...AAAA..AAAA...",
        "..AAAAA..AAAAA..",
        ".AAAAAA..AAAAAA.",
        ".SSSSSS..SSSSSS.",
        "................",
        "................",
        "................"],
    ('MEDIUM', 'helmet'): [
        "................",
        "................",
        ".....AAAAAA.....",
        "....AaAAAAAA....",
        "...AaALAALAAA...",
        "...AALAALAALA...",
        "...LLLLLLLLLL...",
        "...LLL....LLL...",
        "...LL......LL...",
        "...LL......LL...",
        "...GG......GG...",
        "................",
        "................",
        "................",
        "................",
        "................"],
    ('MEDIUM', 'chestplate'): [
        "................",
        "..LLL......LLL..",
        ".LAaAL....LAAAL.",
        ".LAaALGGGGLAAAL.",
        ".LLLAAAAAAAALLL.",
        "..LALAALAALALL..",
        "...AALAJJALAA...",
        "...LAALAALAAL...",
        "...AALAALAALA...",
        "...LLLLGGLLLL...",
        "...ALAALAALAA...",
        "...LAALAALAAL...",
        "....LLLLLLLL....",
        "................",
        "................",
        "................"],
    ('MEDIUM', 'leggings'): [
        "................",
        "...LLLLGGLLLL...",
        "...AaLAALAALA...",
        "...LaALAALAAL...",
        "...AALAALAALA...",
        "...LAAL..AALL...",
        "...AALA..ALAA...",
        "...LAAL..AALL...",
        "...AALA..ALAA...",
        "...LAAL..AALL...",
        "...AALA..ALAA...",
        "...LLLL..LLLL...",
        "...GGGG..GGGG...",
        "................",
        "................",
        "................"],
    ('MEDIUM', 'boots'): [
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "...GGGG..GGGG...",
        "...LaLL..LLLL...",
        "...LALL..LALL...",
        "...LLAL..LLAL...",
        "..LLLLL..LLLLL..",
        ".LLLLLL..LLLLLL.",
        ".KKKKKK..KKKKKK.",
        "................",
        "................",
        "................"],
    ('LIGHT', 'helmet'): [
        "................",
        "......AAAA......",
        ".....AaaAAA.....",
        "....AaAAAAAA....",
        "...AaAAAAAAAA...",
        "...AAAKKKKAAA...",
        "...AAK....KAA...",
        "...AAK....KAA...",
        "...AAAK..KAAA...",
        "....AAAAAAAA....",
        "....GG....GG....",
        "................",
        "................",
        "................",
        "................",
        "................"],
    ('LIGHT', 'chestplate'): [
        "................",
        "..AAA......AAA..",
        ".AAaAA....AAAAA.",
        ".AAaAAAGGAAAAAA.",
        ".GAAAAAGGAAAAAG.",
        "..AAAAAGGAAAAA..",
        "...AAAAJJAAAA...",
        "...AAAAGGAAAA...",
        "...LLLLLLLLLL...",
        "...AAAAGGAAAA...",
        "...AAAAGGAAAA...",
        "...AAAAGGAAAA...",
        "...AAAAAAAAAA...",
        "................",
        "................",
        "................"],
    ('LIGHT', 'leggings'): [
        "................",
        "...LLLLLLLLLL...",
        "...AaAAJAAAAA...",
        "...AaAAAAAAAA...",
        "...AAAAAAAAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...AAAA..AAAA...",
        "...GGGG..GGGG...",
        "...AAAA..AAAA...",
        "................",
        "................",
        "................"],
    ('LIGHT', 'boots'): [
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "...AAAA..AAAA...",
        "...LaLL..LLLL...",
        "...LaLL..LLLL...",
        "...LLLL..LLLL...",
        "..LLLLL..LLLLL..",
        ".LLLLLL..LLLLLL.",
        ".KKKKKK..KKKKKK.",
        "................",
        "................",
        "................"],
}



# ----------------------------------------------------------------------------- Identidad de cada rol (V79)
# Cada rol lleva un emblema 6×5 en el pecho (y en la espalda del modelo), un adorno de casco propio y un ribete de color propio.
# '#' = color del rol aclarado, '+' = ribete del rol, 'K' = oscuro. Son los mismos dibujos en el icono y en la capa del modelo.
TRIM = {
    'berserker': '#d8cdb0', 'mage': '#c9d1e6', 'assassin': '#b3314a', 'herbalist': '#d9c27a', 'engineer': '#cf7f3e',
    'lancer': '#d3dbe6', 'swordsman': '#e0b43a', 'paladin': '#fff0a8', 'necromancer': '#7fe0a8', 'druid': '#b8925a',
    'merchant': '#e8c24a', 'monk': '#f3d06a', 'elementalist': '#bfe9ff', 'shapeshifter': '#e6b8ff', 'gunslinger': '#c9a15a',
    'weapon_summoner': '#e8e2c8', 'hunter': '#c9b27a', 'bard': '#ffd9a0', 'warlock': '#ff7aa8', 'tamer': '#e6d3a8',
}
EMBLEM = {
    'berserker': ["..+##.", "..+###", "..+##.", "..+...", "..+..."],
    'mage': ["..##..", ".#++#.", "#+##+#", ".#++#.", "..##.."],
    'assassin': ["..##..", "..##..", ".####.", "..++..", "..++.."],
    'herbalist': ["...##.", "..####", ".#+##.", "#+#...", "+....."],
    'engineer': ["#.##.#", "######", "##++##", "######", "#.##.#"],
    'lancer': ["..##..", ".####.", "#.##.#", "..++..", "..++.."],
    'swordsman': ["..##..", "..##..", "..##..", "######", "..++.."],
    'paladin': ["..##..", "######", "..##..", "..##..", "..++.."],
    'necromancer': ["######", "#K##K#", "######", ".#++#.", ".#..#."],
    'druid': ["..##..", ".####.", "######", "..++..", "..++.."],
    'merchant': [".####.", "#++++#", "#+##+#", "#++++#", ".####."],
    'monk': [".####.", "#....#", "#.++.#", "#....#", ".####."],
    'elementalist': ["#.##.#", ".####.", "..++..", ".####.", "#.##.#"],
    'shapeshifter': ["#.##.#", "#.##.#", ".####.", "..++..", ".#..#."],
    'gunslinger': ["..##..", ".####.", ".####.", ".++++.", ".++++."],
    'weapon_summoner': ["#....#", ".#..#.", "..++..", ".#..#.", "#....#"],
    'hunter': ["....#.", "...##.", "######", "...##.", "....#."],
    'bard': ["..####", "..#..#", "..#..#", ".##.##", ".##.##"],
    'warlock': ["..##..", ".#KK#.", "#K##K#", ".#KK#.", "..##.."],
    'tamer': ["#.##.#", "#.##.#", "..##..", ".####.", "#.##.#"],
}
# Adorno de la frente del casco: 3 filas × 8 columnas; la fila de «WRAP» se repite alrededor de la cabeza.
BROW = {
    'horns': (["+......+", "++....++", ".+....+."], None),
    'star': (["...##...", "..####..", "...##..."], None),
    'mask': (["........", "KKKKKKKK", "KKKKKKKK"], 1),
    'leaf': (["..+..+..", ".+#++#+.", "..+..+.."], None),
    'goggles': (["........", "K##KK##K", "K##KK##K"], 1),
    'spike': (["...+....", "..+++...", "...+...."], None),
    'band': (["........", "########", "........"], 1),
    'halo': (["++++++++", "........", "........"], 0),
    'skull': (["..####..", ".#K##K#.", "..#KK#.."], None),
    'antler': ([".+.++.+.", "+.+..+.+", "..+..+.."], None),
    'crown': (["+.+.+.+.", "++++++++", "........"], 1),
    'gem': (["........", "...++...", "...##..."], None),
    'ears': (["#......#", "##....##", "........"], None),
    'hat': (["########", "++++++++", "........"], 1),
    'runes': (["#.#.#.#.", ".#.#.#.#", "........"], None),
    'notes': (["..#..#..", ".##.##..", "........"], None),
    'eye': (["...##...", "..#KK#..", "...##..."], None),
    'fang': (["........", "K.K..K.K", "..K..K.."], None),
}
CREST = {
    'berserker': 'horns', 'mage': 'star', 'assassin': 'mask', 'herbalist': 'leaf', 'engineer': 'goggles', 'lancer': 'spike',
    'swordsman': 'band', 'paladin': 'halo', 'necromancer': 'skull', 'druid': 'antler', 'merchant': 'crown', 'monk': 'band',
    'elementalist': 'gem', 'shapeshifter': 'ears', 'gunslinger': 'hat', 'weapon_summoner': 'runes', 'hunter': 'antler',
    'bard': 'notes', 'warlock': 'eye', 'tamer': 'fang',
}
# Píxeles extra que sobresalen de la silueta del icono del casco (cuernos, astas, picos…).
CREST_ICON_EXTRA = {
    'horns': [(3, 1), (3, 2), (12, 1), (12, 2), (3, 0), (12, 0)], 'antler': [(4, 1), (6, 0), (9, 0), (11, 1), (5, 2), (10, 2)],
    'ears': [(4, 1), (11, 1), (4, 2), (11, 2)], 'spike': [(7, 0), (8, 0), (7, 1), (8, 1)], 'star': [(7, 1), (8, 1)],
    'crown': [(5, 1), (7, 1), (9, 1), (11, 1)], 'halo': [(5, 1), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1)],
}


def dark_main(role, main):
    return mix(main, '#14161f', 0.22)


def crest_pixels(role):
    """[(columna 0-7, fila 0-2, carácter)] del adorno del rol."""
    rows, _ = BROW[CREST[role]]
    return [(x, y, c) for y, row in enumerate(rows) for x, c in enumerate(row) if c != '.']


def palette(role):
    color = ROLE_COLORS[role]
    w = weight(role)
    if w == 'HEAVY':
        main = mix(color, STEEL, 0.35)   # placas de metal tintadas
    elif w == 'MEDIUM':
        main = mix(color, LEATHER, 0.15)
    else:
        main = color                     # tela
    return {'A': dark_main(role, main), 'S': STEEL, 'L': LEATHER, 'G': TRIM[role], 'J': color if w != 'HEAVY' else '#ffffff',
            'K': DARK}


def icon(role, piece):
    pal = palette(role)
    if weight(role) == 'HEAVY':
        pal['J'] = ROLE_COLORS[role]
    img = render(ICONS[(weight(role), piece)], pal)
    decorate_icon(img, role, piece, pal)
    return img


def _pix(pal, ch, role):
    if ch == '#':
        return ramp(mix(ROLE_COLORS[role], '#ffffff', 0.25))[3]
    if ch == '+':
        return ramp(TRIM[role])[3]
    return ramp(DARK)[1]


def decorate_icon(img, role, piece, pal):
    """Emblema del rol en el peto y adorno propio en el casco (V79)."""
    px = img.load()
    if piece == 'chestplate':
        for dy, row in enumerate(EMBLEM[role]):
            for dx, ch in enumerate(row):
                if ch != '.':
                    px[5 + dx, 4 + dy] = _pix(pal, ch, role)
    elif piece == 'helmet':
        kind = CREST[role]
        for x, y, ch in crest_pixels(role):
            ix, iy = 4 + x, 2 + y
            if px[ix, iy][3] and not (weight(role) == 'LIGHT' and iy >= 5):
                px[ix, iy] = _pix(pal, ch, role)
        for ix, iy in CREST_ICON_EXTRA.get(kind, []):
            px[ix, iy] = ramp(TRIM[role])[2 if iy else 3]


# ----------------------------------------------------------------------------- Capas del modelo (64×32)

def faces_box(u, v, w, h, d):
    """Caras de un cubo en el mapa UV de Minecraft: {nombre: (x, y, ancho, alto)}."""
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h),
    }


HEAD = faces_box(0, 0, 8, 8, 8)
BODY = faces_box(16, 16, 8, 12, 4)
ARM = faces_box(40, 16, 4, 12, 4)
LEG = faces_box(0, 16, 4, 12, 4)


class Painter:
    def __init__(self, role, seed):
        self.img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
        self.px = self.img.load()
        self.rng = random.Random(seed)
        self.ramps = {}
        self.pal = palette(role)
        self.pal['J'] = ROLE_COLORS[role]

    def r(self, mat):
        if mat not in self.ramps:
            self.ramps[mat] = ramp(self.pal.get(mat, mat))
        return self.ramps[mat]

    def face(self, rect, mat, rows=None):
        """Rellena una cara con volumen: arriba luz, abajo sombra, bordes oscuros y algo de textura."""
        x0, y0, w, h = rect
        ramp_ = self.r(mat)
        for dy in range(h):
            if rows is not None and dy not in rows:
                continue
            for dx in range(w):
                tone = 2
                if dy == 0 or dx == 0:
                    tone = 3
                if dy == h - 1 or dx == w - 1:
                    tone = 1
                if tone == 2 and self.rng.random() < 0.06:  # textura sutil
                    tone = self.rng.choice((1, 3))
                self.px[x0 + dx, y0 + dy] = ramp_[tone]

    def dot(self, x, y, mat, tone=2):
        self.px[x, y] = self.r(mat)[tone]

    def hline(self, x0, x1, y, mat, tone=2):
        for x in range(x0, x1 + 1):
            self.dot(x, y, mat, tone)

    def clear(self, x, y):
        self.px[x, y] = (0, 0, 0, 0)


def layer1(role):
    w = weight(role)
    p = Painter(role, sum(map(ord, role)))  # semilla fija: mismas texturas en cada ejecución
    # Casco: las 6 caras de la cabeza
    for name, rect in HEAD.items():
        p.face(rect, 'A')
    fx, fy = HEAD['front'][0], HEAD['front'][1]
    if w == 'HEAVY':
        p.hline(fx + 1, fx + 6, fy + 4, 'K', 1)          # rendija de la visera
        p.hline(fx + 1, fx + 6, fy + 3, 'G', 3)
        for y in range(0, 4):                            # cresta central dorada
            p.dot(fx + 3, fy + y, 'G', 3)
            p.dot(fx + 4, fy + y, 'G', 2)
        tx, ty = HEAD['top'][0], HEAD['top'][1]
        for y in range(8):
            p.dot(tx + 3, ty + y, 'G', 3)
            p.dot(tx + 4, ty + y, 'G', 2)
    elif w == 'MEDIUM':
        for y in range(1, 7, 2):                         # escamas
            for x in range((y // 2) % 2, 8, 2):
                p.dot(fx + x, fy + y, 'L', 1)
        p.hline(fx, fx + 7, fy + 7, 'G', 2)
        for x in range(2, 6):                            # abertura de la cara
            for y in range(4, 7):
                p.dot(fx + x, fy + y, 'K', 0)
    else:
        for x in range(1, 7):                            # capucha: cara abierta (transparente)
            for y in range(2, 8):
                p.clear(fx + x, fy + y)
        p.hline(fx + 1, fx + 6, fy + 1, 'K', 1)
        p.dot(fx, fy + 7, 'G', 3)
        p.dot(fx + 7, fy + 7, 'G', 3)
    # Peto: torso y brazos
    for rect in BODY.values():
        p.face(rect, 'A')
    for rect in ARM.values():
        p.face(rect, 'A')
    bx, by = BODY['front'][0], BODY['front'][1]
    if w == 'HEAVY':
        p.hline(bx, bx + 7, by, 'G', 3)                  # cuello
        p.dot(bx + 3, by + 3, 'J', 3); p.dot(bx + 4, by + 3, 'J', 2)
        p.dot(bx + 3, by + 4, 'J', 2); p.dot(bx + 4, by + 4, 'J', 1)
        for x in (bx + 1, bx + 6):                       # remaches
            p.dot(x, by + 2, 'S', 4)
            p.dot(x, by + 7, 'S', 4)
        p.hline(bx, bx + 7, by + 10, 'L', 2)             # cinturón con hebilla
        p.dot(bx + 3, by + 10, 'G', 3); p.dot(bx + 4, by + 10, 'G', 3)
        for name in ('front', 'back', 'right', 'left'):  # hombreras doradas
            ax, ay, aw, _ = ARM[name]
            p.hline(ax, ax + aw - 1, ay, 'G', 3)
            p.hline(ax, ax + aw - 1, ay + 1, 'A', 3)
        for name in ('front', 'back', 'right', 'left'):
            ax, ay, aw, ah = ARM[name]
            p.hline(ax, ax + aw - 1, ay + ah - 1, 'S', 2)  # guantelete
    elif w == 'MEDIUM':
        for y in range(1, 10, 2):
            for x in range((y // 2) % 2, 8, 2):
                p.dot(bx + x, by + y, 'L', 1)
        p.dot(bx + 3, by + 4, 'J', 3); p.dot(bx + 4, by + 4, 'J', 2)
        p.hline(bx, bx + 7, by + 10, 'L', 1)
        p.dot(bx + 4, by + 10, 'G', 3)
        for name in ('front', 'back', 'right', 'left'):
            ax, ay, aw, ah = ARM[name]
            p.hline(ax, ax + aw - 1, ay, 'L', 2)
            p.hline(ax, ax + aw - 1, ay + ah - 1, 'G', 2)
    else:
        for y in range(0, 12):                           # túnica con franja central dorada
            p.dot(bx + 3, by + y, 'G', 3 if y % 2 == 0 else 2)
            p.dot(bx + 4, by + y, 'G', 2)
        p.dot(bx + 3, by + 3, 'J', 3); p.dot(bx + 4, by + 3, 'J', 2)
        p.hline(bx, bx + 7, by + 7, 'L', 2)              # fajín
        for name in ('front', 'back', 'right', 'left'):
            ax, ay, aw, ah = ARM[name]
            p.hline(ax, ax + aw - 1, ay + ah - 1, 'G', 3)  # puños
            p.hline(ax, ax + aw - 1, ay + ah - 2, 'A', 1)
    # Botas: parte baja de las piernas (6 filas) y suela
    boot_mat = 'A' if w == 'HEAVY' else 'L'
    for name in ('front', 'back', 'right', 'left'):
        p.face(LEG[name], boot_mat, rows=range(6, 12))
        lx, ly, lw, lh = LEG[name]
        p.hline(lx, lx + lw - 1, ly + 6, 'G' if w != 'MEDIUM' else 'L', 3)
        p.hline(lx, lx + lw - 1, ly + lh - 1, 'K' if w != 'HEAVY' else 'S', 1)
    p.face(LEG['bottom'], 'K')
    decorate_layer(p, role)
    return p.img


def decorate_layer(p, role):
    """Emblema del rol en el pecho y la espalda y adorno del casco alrededor de la cabeza (V79)."""
    def put(x, y, ch, only_opaque=False):
        if only_opaque and not p.px[x, y][3]:
            return
        if ch == '#':
            p.px[x, y] = p.r('J')[3]
        elif ch == '+':
            p.px[x, y] = p.r('G')[3]
        else:
            p.px[x, y] = p.r('K')[1]
    for face in ('front', 'back'):
        bx, by = BODY[face][0], BODY[face][1]
        for dy, row in enumerate(EMBLEM[role]):
            for dx, ch in enumerate(row):
                if ch != '.':
                    put(bx + 1 + dx, by + 1 + dy, ch)
    rows, wrap = BROW[CREST[role]]
    fx, fy = HEAD['front'][0], HEAD['front'][1]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.':
                put(fx + x, fy + y, ch, True)
    if wrap is not None:
        ch = next((c for c in rows[wrap] if c != '.'), None)
        for face in ('right', 'left', 'back'):
            hx, hy, hw, _ = HEAD[face]
            for x in range(hw):
                put(hx + x, hy + wrap, ch, True)


def layer2(role):
    w = weight(role)
    p = Painter(role, sum(map(ord, role)) * 7)
    # Cintura: parte baja del torso (filas 6-11 de las caras)
    for name in ('front', 'back', 'right', 'left'):
        p.face(BODY[name], 'A', rows=range(6, 12))
    bx, by = BODY['front'][0], BODY['front'][1]
    p.hline(bx, bx + 7, by + 6, 'L', 2)                   # cinturón
    p.dot(bx + 3, by + 6, 'G', 3); p.dot(bx + 4, by + 6, 'G', 3)
    # Piernas completas
    for rect in LEG.values():
        p.face(rect, 'A')
    for name in ('front', 'back', 'right', 'left'):
        lx, ly, lw, lh = LEG[name]
        if w == 'HEAVY':
            p.hline(lx, lx + lw - 1, ly + 4, 'G', 3)      # rodillera
            p.dot(lx + 1, ly + 5, 'S', 4)
        elif w == 'MEDIUM':
            for y in range(1, 11, 2):
                for x in range((y // 2) % 2, lw, 2):
                    p.dot(lx + x, ly + y, 'L', 1)
        else:
            p.hline(lx, lx + lw - 1, ly + 9, 'G', 3)      # ribete del bajo
    return p.img


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    root = sys.argv[1].rstrip('/')
    preview = sys.argv[sys.argv.index('--preview') + 1] if '--preview' in sys.argv else None
    icons, layers = {}, {}
    for role in ROLE_COLORS:
        for piece in ('helmet', 'chestplate', 'leggings', 'boots'):
            img = icon(role, piece)
            img.save(f'{root}/item/{role}_{piece}.png')
            icons[f'{role}_{piece}'] = img
        l1, l2 = layer1(role), layer2(role)
        l1.save(f'{root}/models/armor/{role}_layer_1.png')
        l2.save(f'{root}/models/armor/{role}_layer_2.png')
        layers[role] = (l1, l2)
    if preview:
        roles = list(ROLE_COLORS)
        sc = 4
        row_h = 32 * sc + 8
        sheet = Image.new('RGBA', (64 * sc * 2 + 16 * sc * 4 + 48, row_h * len(roles)), (58, 60, 72, 255))
        for i, role in enumerate(roles):
            x = 0
            for l in layers[role]:
                big = l.resize((64 * sc, 32 * sc), Image.NEAREST)
                sheet.paste(big, (x, i * row_h), big)
                x += 64 * sc + 8
            for piece in ('helmet', 'chestplate', 'leggings', 'boots'):
                big = icons[f'{role}_{piece}'].resize((16 * sc, 16 * sc), Image.NEAREST)
                sheet.paste(big, (x, i * row_h), big)
                x += 16 * sc + 8
        sheet.save(preview)
    print(f'{len(icons)} iconos y {len(layers) * 2} capas generados en {root}')


if __name__ == '__main__':
    main()
