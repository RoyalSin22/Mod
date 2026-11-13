"""Monta las hojas de validación del Osario Quebrado a partir de las capturas de Blockbench.

Uso: python make_sheets.py <carpeta_capturas> <carpeta_salida>
La carpeta de capturas debe tener, con fondo transparente:
  lit_north.png, lit_east.png, lit_iso.png        modelo con la textura base e iluminación normal
  glow_north.png, glow_east.png, glow_iso.png     modelo en negro con solo los píxeles que brillan, sin sombreado
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

SRC = Path(sys.argv[1])
OUT = Path(sys.argv[2])
OUT.mkdir(parents=True, exist_ok=True)
VIEWS = [("north", "Frontal"), ("east", "Lateral"), ("iso", "3/4")]
BG = (16, 18, 24, 255)
NIGHT = 0.28          # luz ambiente de una noche de Minecraft (aprox.)


def crop_box(images):
    """Recorte común a todas las imágenes de una vista: caja del modelo con margen."""
    box = None
    for im in images:
        b = im.getchannel("A").getbbox()
        if b:
            box = b if box is None else (min(box[0], b[0]), min(box[1], b[1]), max(box[2], b[2]), max(box[3], b[3]))
    m = 24
    w, h = images[0].size
    return (max(0, box[0] - m), max(0, box[1] - m), min(w, box[2] + m), min(h, box[3] + m))


def darken(im):
    px = im.load()
    out = Image.new("RGBA", im.size)
    op = out.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            op[x, y] = (int(r * NIGHT), int(g * NIGHT), int(b * NIGHT * 1.15), a)
    return out


def with_glow(dark, glow):
    """Sobre el modelo a oscuras, los píxeles del glowmask a plena luz (sin halo: Minecraft no lo dibuja)."""
    out = dark.copy()
    op = out.load()
    gp = glow.load()
    n = 0
    for y in range(glow.height):
        for x in range(glow.width):
            r, g, b, a = gp[x, y]
            if a > 0 and max(r, g, b) > 48:
                op[x, y] = (r, g, b, 255)
                n += 1
    return out, n


def on_bg(im):
    bg = Image.new("RGBA", im.size, BG)
    bg.alpha_composite(im)
    return bg


def sheet(rows, labels, name):
    """rows: lista de filas; cada fila es una lista de imágenes ya recortadas."""
    pad, head = 12, 26
    col_w = [max(r[i].width for r in rows) for i in range(len(rows[0]))]
    row_h = [max(im.height for im in r) for r in rows]
    W = sum(col_w) + pad * (len(col_w) + 1)
    H = sum(row_h) + (pad + head) * len(rows) + pad
    out = Image.new("RGBA", (W, H), BG)
    d = ImageDraw.Draw(out)
    y = pad
    for ri, r in enumerate(rows):
        d.text((pad, y + 6), labels[ri], fill=(215, 220, 230, 255))
        y += head
        x = pad
        for ci, im in enumerate(r):
            out.alpha_composite(on_bg(im), (x + (col_w[ci] - im.width) // 2, y + (row_h[ri] - im.height) // 2))
            x += col_w[ci] + pad
        y += row_h[ri] + pad
    out.save(OUT / name)
    return out.size


lit, dark, glowed = [], [], []
for key, _ in VIEWS:
    a = Image.open(SRC / f"lit_{key}.png").convert("RGBA")
    g = Image.open(SRC / f"glow_{key}.png").convert("RGBA")
    box = crop_box([a, g])
    a, g = a.crop(box), g.crop(box)
    dk = darken(a)
    gl, n = with_glow(dk, g)
    lit.append(a)
    dark.append(dk)
    glowed.append(gl)
    on_bg(a).save(OUT / f"vista_{key}_luz.png")
    on_bg(dk).save(OUT / f"vista_{key}_oscuro_sin_brillo.png")
    on_bg(gl).save(OUT / f"vista_{key}_oscuro_con_brillo.png")
    print(key, "pixeles de pantalla que brillan:", n)

names = "  |  ".join(v for _, v in VIEWS)
print(sheet([lit], [f"Con luz de dia: {names}"], "hoja_vistas_luz.png"))
print(sheet([dark, glowed], [f"A oscuras SIN brillo: {names}", f"A oscuras CON brillo (glowmask a plena luz): {names}"],
            "hoja_brillo.png"))
