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


def palette(role):
    color = ROLE_COLORS[role]
    w = weight(role)
    if w == 'HEAVY':
        main = mix(color, STEEL, 0.35)   # placas de metal tintadas
    elif w == 'MEDIUM':
        main = mix(color, LEATHER, 0.15)
    else:
        main = color                     # tela
    return {'A': main, 'S': STEEL, 'L': LEATHER, 'G': GOLD, 'J': color if w != 'HEAVY' else '#ffffff',
            'K': DARK}


def icon(role, piece):
    pal = palette(role)
    if weight(role) == 'HEAVY':
        pal['J'] = ROLE_COLORS[role]
    return render(ICONS[(weight(role), piece)], pal)


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
    return p.img


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
