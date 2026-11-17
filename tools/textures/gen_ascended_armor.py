#!/usr/bin/env python3
"""
Generador de la Armadura Ascendida (V80): un único set, mejora de la netherita.

Salida (carpeta `textures` de los recursos):
  item/ascended_{helmet,chestplate,leggings,boots}.png   iconos 16x16
  item/ascended_template.png                              Plantilla de ascensión 16x16
  models/armor/ascended_layer_1.png, ascended_layer_2.png capas del modelo 64x32 (mapa UV estándar)

Diseño: netherita violeta oscura con ribetes de oro brasa y runas del Velo (turquesa luminoso). Cuernos y cresta en el
casco, hombreras pesadas, núcleo rúnico en el peto, rodilleras y botas con punta dorada.

Uso:  python3 tools/textures/gen_ascended_armor.py <carpeta textures> [--preview salida.png] [--check]
"""
import random
import sys

from PIL import Image

sys.path.insert(0, __file__.rsplit('/', 1)[0])
from gen_items import ramp, render  # noqa: E402

COLORS = {'A': '#3b3144', 'G': '#e0a53a', 'R': '#5fe6d6', 'K': '#15111b', 'S': '#6c6178'}

ICONS = {
    'helmet': [
        "................",
        ".G............G.",
        ".GG...GGGG...GG.",
        "..GGAAAAAAAAGG..",
        "..GAAAARRAAAAG..",
        "..AAAAARRAAAAA..",
        "..AAKKKKKKKKAA..",
        "..AAKRKKKKRKAA..",
        "..AAKK....KKAA..",
        "..AAA......AAA..",
        "..GG........GG..",
        "................",
        "................",
        "................",
        "................",
        "................",
    ],
    'chestplate': [
        "................",
        ".GGG........GGG.",
        "GAAAGGGGGGGGAAAG",
        "GAsAAAAARRAAAsAG",
        "GAAAAAARRRRAAAAG",
        ".GAAAAARRRRAAAG.",
        "..AAAAAARRAAAA..",
        "..AAGAAAAAAGAA..",
        "..AAAGGGGGGAAA..",
        "..AAARRKKRRAAA..",
        "..AAAAAAAAAAAA..",
        "...AAAAAAAAAA...",
        "....AAAAAAAA....",
        "................",
        "................",
        "................",
    ],
    'leggings': [
        "................",
        "..GGGGGGGGGGGG..",
        "..AAAAAAAAAAAA..",
        "..AAAAGKKGAAAA..",
        "..AAAAA..AAAAA..",
        "..AAGAA..AAGAA..",
        "..AAAAA..AAAAA..",
        "..AARAA..AARAA..",
        "..AAAAA..AAAAA..",
        "..AAAAA..AAAAA..",
        "..GAAAA..AAAAG..",
        "..GGAAA..AAAGG..",
        "................",
        "................",
        "................",
        "................",
    ],
    'boots': [
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "..GGAA....AAGG..",
        "..AAAA....AAAA..",
        "..AARA....ARAA..",
        "..AAAA....AAAA..",
        ".GAAAAA..AAAAAG.",
        ".GGGAAA..AAAGGG.",
        "................",
        "................",
        "................",
        "................",
    ],
    'template': [
        "................",
        "....GGGGGGGG....",
        "...GAAAAAAAAG...",
        "..GAAAARRAAAAG..",
        "..GAAARRRRAAAG..",
        "..GAARRGGRRAAG..",
        "..GAAARRRRAAAG..",
        "..GAAAARRAAAAG..",
        "..GAAAGGGGAAAG..",
        "..GAAAAAAAAAAG..",
        "..GAAAGAAGAAAG..",
        "...GAAAAAAAAG...",
        "....GGGGGGGG....",
        "................",
        "................",
        "................",
    ],
}


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
    def __init__(self, seed):
        self.img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
        self.px = self.img.load()
        self.rng = random.Random(seed)
        self.ramps = {m: ramp(c) for m, c in COLORS.items()}

    def face(self, rect, mat, rows=None):
        x0, y0, w, h = rect
        r = self.ramps[mat]
        for dy in range(h):
            if rows is not None and dy not in rows:
                continue
            for dx in range(w):
                tone = 2
                if dy == 0 or dx == 0:
                    tone = 3
                if dy == h - 1 or dx == w - 1:
                    tone = 1
                if tone == 2 and self.rng.random() < 0.10:
                    tone = self.rng.choice((1, 3))
                self.px[x0 + dx, y0 + dy] = r[tone]

    def dot(self, x, y, mat, tone=2):
        self.px[x, y] = self.ramps[mat][tone]

    def hline(self, x0, x1, y, mat, tone=2):
        for x in range(x0, x1 + 1):
            self.dot(x, y, mat, tone)

    def vline(self, x, y0, y1, mat, tone=2):
        for y in range(y0, y1 + 1):
            self.dot(x, y, mat, tone)

    def clear(self, x, y):
        self.px[x, y] = (0, 0, 0, 0)


def layer1():
    p = Painter(80)
    for rect in HEAD.values():
        p.face(rect, 'A')
    fx, fy = HEAD['front'][0], HEAD['front'][1]
    p.hline(fx, fx + 7, fy, 'G', 3)                       # diadema dorada
    p.hline(fx + 1, fx + 6, fy + 4, 'K', 1)               # rendija de la visera
    p.hline(fx + 1, fx + 6, fy + 5, 'K', 1)
    p.dot(fx + 2, fy + 4, 'R', 4); p.dot(fx + 5, fy + 4, 'R', 4)   # ojos rúnicos
    p.dot(fx + 3, fy + 2, 'R', 3); p.dot(fx + 4, fy + 2, 'R', 3)   # gema de la frente
    p.vline(fx + 3, fy + 6, fy + 7, 'G', 2); p.vline(fx + 4, fy + 6, fy + 7, 'G', 2)  # barbote
    tx, ty = HEAD['top'][0], HEAD['top'][1]
    for y in range(8):                                    # cresta central
        p.dot(tx + 3, ty + y, 'G', 3); p.dot(tx + 4, ty + y, 'G', 2)
    for name in ('right', 'left'):                        # cuernos pintados en los laterales
        hx, hy, hw, hh = HEAD[name]
        p.hline(hx, hx + hw - 1, hy, 'G', 3)
        p.hline(hx + 1, hx + hw - 2, hy + 1, 'G', 2)
        p.dot(hx + hw // 2, hy + 3, 'R', 3)
    bx, by = HEAD['back'][0], HEAD['back'][1]
    p.hline(bx, bx + 7, by, 'G', 3)
    p.vline(bx + 3, by + 1, by + 7, 'R', 2); p.vline(bx + 4, by + 1, by + 7, 'R', 3)

    for rect in BODY.values():
        p.face(rect, 'A')
    for rect in ARM.values():
        p.face(rect, 'A')
    bx, by = BODY['front'][0], BODY['front'][1]
    p.hline(bx, bx + 7, by, 'G', 3)                       # gorjal
    p.hline(bx + 1, bx + 6, by + 1, 'G', 2)
    for dx, dy in ((3, 3), (4, 3), (2, 4), (3, 4), (4, 4), (5, 4), (3, 5), (4, 5)):   # núcleo rúnico
        p.dot(bx + dx, by + dy, 'R', 3 if dy < 5 else 2)
    p.dot(bx + 3, by + 4, 'R', 4); p.dot(bx + 4, by + 4, 'R', 4)
    for dy in (7, 8):                                     # costillas
        p.hline(bx + 1, bx + 6, by + dy, 'K', 1)
    p.hline(bx, bx + 7, by + 10, 'G', 2)                  # cinturón
    p.dot(bx + 3, by + 10, 'R', 3); p.dot(bx + 4, by + 10, 'R', 3)
    bbx, bby = BODY['back'][0], BODY['back'][1]
    p.hline(bbx, bbx + 7, bby, 'G', 3)
    for dy in range(1, 11):                               # runa vertical con alas
        p.dot(bbx + 3, bby + dy, 'R', 3); p.dot(bbx + 4, bby + dy, 'R', 3)
    for dx in (1, 2, 5, 6):
        p.dot(bbx + dx, bby + 3 + (dx % 3), 'G', 2)
    p.hline(bbx, bbx + 7, bby + 10, 'G', 2)
    for name in ('front', 'back', 'right', 'left'):       # hombreras y puños
        ax, ay, aw, ah = ARM[name]
        p.hline(ax, ax + aw - 1, ay, 'G', 3)
        p.hline(ax, ax + aw - 1, ay + 1, 'G', 2)
        p.hline(ax, ax + aw - 1, ay + ah - 1, 'G', 2)
    ax, ay = ARM['front'][0], ARM['front'][1]
    p.dot(ax + 1, ay + 5, 'R', 3); p.dot(ax + 2, ay + 5, 'R', 3)
    for name in ('front', 'back', 'right', 'left'):       # botas
        p.face(LEG[name], 'A', rows=range(6, 12))
        lx, ly, lw, lh = LEG[name]
        p.hline(lx, lx + lw - 1, ly + 6, 'G', 3)
        p.hline(lx, lx + lw - 1, ly + lh - 1, 'G', 1)
    lx, ly = LEG['front'][0], LEG['front'][1]
    p.dot(lx + 1, ly + 8, 'R', 3); p.dot(lx + 2, ly + 8, 'R', 3)
    p.face(LEG['bottom'], 'K')
    return p.img


def layer2():
    p = Painter(81)
    for name in ('front', 'back', 'right', 'left'):
        p.face(BODY[name], 'A', rows=range(6, 12))
    bx, by = BODY['front'][0], BODY['front'][1]
    p.hline(bx, bx + 7, by + 6, 'G', 2)
    p.dot(bx + 3, by + 6, 'R', 3); p.dot(bx + 4, by + 6, 'R', 3)
    for rect in LEG.values():
        p.face(rect, 'A')
    for name in ('front', 'back', 'right', 'left'):
        lx, ly, lw, lh = LEG[name]
        p.hline(lx, lx + lw - 1, ly + 4, 'G', 3)          # rodillera
        p.hline(lx, lx + lw - 1, ly + 5, 'G', 1)
        p.hline(lx, lx + lw - 1, ly + 10, 'G', 2)         # bajo
    for name in ('front', 'back'):
        lx, ly = LEG[name][0], LEG[name][1]
        p.vline(lx + 1, ly + 1, ly + 3, 'R', 3)           # runa del muslo
        p.vline(lx + 2, ly + 7, ly + 9, 'R', 2)
    return p.img


def build():
    icons = {f'ascended_{name}': render(rows, COLORS) for name, rows in ICONS.items() if name != 'template'}
    icons['ascended_template'] = render(ICONS['template'], COLORS)
    return icons, layer1(), layer2()


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    root = sys.argv[1].rstrip('/')
    icons, l1, l2 = build()
    if '--check' in sys.argv:
        bad = []
        for name, img in icons.items():
            if _read(f'{root}/item/{name}.png') != img.tobytes():
                bad.append(f'item/{name}.png')
        for name, img in (('ascended_layer_1', l1), ('ascended_layer_2', l2)):
            if _read(f'{root}/models/armor/{name}.png') != img.tobytes():
                bad.append(f'models/armor/{name}.png')
        if bad:
            print('Desactualizado:', ', '.join(bad))
            sys.exit(1)
        print('Armadura Ascendida al día')
        return
    for name, img in icons.items():
        img.save(f'{root}/item/{name}.png')
    l1.save(f'{root}/models/armor/ascended_layer_1.png')
    l2.save(f'{root}/models/armor/ascended_layer_2.png')
    if '--preview' in sys.argv:
        out = sys.argv[sys.argv.index('--preview') + 1]
        sc = 10
        sheet = Image.new('RGBA', (64 * sc * 2 + 20, 32 * sc + 16 * sc + 30), (58, 60, 72, 255))
        for i, layer in enumerate((l1, l2)):
            big = layer.resize((64 * sc, 32 * sc), Image.NEAREST)
            sheet.paste(big, (i * (64 * sc + 20), 0), big)
        x = 0
        for name in ('ascended_helmet', 'ascended_chestplate', 'ascended_leggings', 'ascended_boots', 'ascended_template'):
            big = icons[name].resize((16 * sc, 16 * sc), Image.NEAREST)
            sheet.paste(big, (x, 32 * sc + 10), big)
            x += 16 * sc + 10
        sheet.save(out)
    print(f'{len(icons)} iconos y 2 capas generados en {root}')


def _read(path):
    try:
        return Image.open(path).convert('RGBA').tobytes()
    except OSError:
        return None


if __name__ == '__main__':
    main()
