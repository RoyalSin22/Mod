#!/usr/bin/env python3
"""G5 (v67): del arte de referencia del grimorio (imágenes grandes) a las piezas reducidas del kit.

Paso previo y puntual (no se ejecuta en CI): lee las seis imágenes originales y escribe las piezas ya reducidas en
`tools/textures/source/ui/guide/`, que luego valida y copia `gen_guide_ui.py`. Los originales (de 1254 px o más) no se
guardan en el repositorio: se pasan por argumento.

Reglas de reducción
- Lanczos con alfa premultiplicado, alfa binario (0 o 255) y paleta de 32 colores como máximo por pieza.
- Ventana, panel y retrato son marcos de 9 partes. El centro de cada borde del original lleva adornos (ojo, libro,
  cintas) que se deformarían al estirarlos, así que cada marco se compone con las cuatro esquinas del original y un
  tramo limpio del borde superior que se repite (girado para los laterales). El ojo y el libro van aparte, como
  emblemas que el juego dibuja encima. El centro es un color plano (el del interior del original).
- Cinta: solo la banda; remates (calavera y dragón con su banderín) y un tramo central limpio que se repite.
- Divisor: de 5 partes (remate, tramo, rombo central, tramo, remate).
- Esquina: una pieza de 12×13 (el juego la voltea con las otras tres variantes).

Uso: python3 tools/textures/prepare_guide_art.py <carpeta con las imágenes originales> [--preview hoja.png]
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'tools/textures/source/ui/guide'
COLORS = 32

SOURCES = {  # pieza -> fragmento del nombre del archivo original
    'window': 'p.m-1.png', 'panel': 'p.m-2.png', 'portrait': 'p.m-3.png',
    'ribbon': 'p.m-4.png', 'divider': 'p.m-5.png', 'corner': 'p.m-6.png',
}

# Parámetros por marco: escala sobre el original recortado, esquina, ancho del tramo de borde y tramo limpio del
# original (en fracción del ancho) del que se toma. `eye` y `book` son las cajas de los emblemas (fracciones).
FRAMES = {
    'window': dict(scale=0.052, corner=14, tile=6, span=(0.245, 0.340), eye=(0.30, 0.0, 0.70, 0.125),
                   book=(0.34, 0.865, 0.66, 1.0)),
    'panel': dict(scale=0.032, corner=8, tile=4, span=(0.245, 0.340), eye=(0.30, 0.0, 0.70, 0.125),
                  book=(0.34, 0.90, 0.66, 1.0)),
    'portrait': dict(scale=0.045, corner=14, tile=6, span=(0.26, 0.340), eye=(0.30, 0.0, 0.70, 0.17),
                     book=(0.34, 0.84, 0.66, 1.0)),
}


def load(folder: Path, piece: str) -> Image.Image:
    matches = [p for p in folder.glob('*.png') if p.name.endswith(SOURCES[piece])]
    if len(matches) != 1:
        raise SystemExit(f'No encuentro el original de «{piece}» ({SOURCES[piece]}) en {folder}')
    image = Image.open(matches[0]).convert('RGBA')
    box = image.getchannel('A').point(lambda v: 255 if v > 16 else 0).getbbox()
    return image.crop(box)


def shrink(image: Image.Image, width: int, height: int, cut: int = 128) -> Image.Image:
    premul = image.convert('RGBa').resize((max(1, width), max(1, height)), Image.LANCZOS).convert('RGBA')
    premul.putalpha(premul.getchannel('A').point(lambda v: 255 if v >= cut else 0))
    return premul


def quantize(image: Image.Image, colors: int = COLORS) -> Image.Image:
    """Paleta reducida sobre los píxeles opacos; el alfa vuelve a ser 0 o 255."""
    alpha = image.getchannel('A').point(lambda v: 255 if v >= 128 else 0)
    flat = Image.new('RGB', image.size, (0, 0, 0))
    flat.paste(image.convert('RGB'), mask=alpha)
    pal = flat.quantize(colors=colors, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert('RGB')
    out = pal.convert('RGBA')
    out.putalpha(alpha)
    return out


def interior_color(image: Image.Image) -> tuple:
    """Color más frecuente del interior (el 40 % central) del marco ya reducido."""
    w, h = image.size
    box = image.crop((int(w * 0.30), int(h * 0.30), int(w * 0.70), int(h * 0.70)))
    counts = {}
    for px in box.get_flattened_data() if hasattr(box, 'get_flattened_data') else box.getdata():
        counts[px] = counts.get(px, 0) + 1
    return max(counts, key=counts.get)


def frame(folder: Path, piece: str):
    cfg = FRAMES[piece]
    src = load(folder, piece)
    w, h = max(1, round(src.width * cfg['scale'])), max(1, round(src.height * cfg['scale']))
    small = shrink(src, w, h)
    c, t = cfg['corner'], cfg['tile']
    x0 = round(w * cfg['span'][0])
    top = small.crop((x0, 0, x0 + t, c))
    bottom = small.crop((x0, h - c, x0 + t, h))
    left = top.transpose(Image.ROTATE_90)       # el borde exterior (arriba) pasa a la izquierda
    right = left.transpose(Image.FLIP_LEFT_RIGHT)
    size = 2 * c + t
    out = Image.new('RGBA', (size, size), interior_color(small))
    out.paste(small.crop((0, 0, c, c)), (0, 0))
    out.paste(small.crop((w - c, 0, w, c)), (size - c, 0))
    out.paste(small.crop((0, h - c, c, h)), (0, size - c))
    out.paste(small.crop((w - c, h - c, w, h)), (size - c, size - c))
    out.paste(top, (c, 0))
    out.paste(bottom, (c, size - c))
    out.paste(left, (0, c))
    out.paste(right, (size - c, c))

    def emblem(box):
        l, u, r, d = box
        return small.crop((round(w * l), round(h * u), round(w * r), round(h * d)))

    return quantize(out), quantize(emblem(cfg['eye'])), quantize(emblem(cfg['book']))


def ribbon(folder: Path) -> Image.Image:
    """Remates de 19 px (calavera o dragón con su banderín) y un tramo central limpio de 10 px que se repite."""
    src = load(folder, 'ribbon')
    band = src.crop((0, round(src.height * 0.17), src.width, round(src.height * 0.80)))
    height, cap, tile = 14, 19, 10
    small = shrink(band, round(band.width * height / band.height), height)
    x0 = round(small.width * 0.225)
    out = Image.new('RGBA', (2 * cap + tile, height))
    out.paste(small.crop((0, 0, cap, height)), (0, 0))
    out.paste(small.crop((x0, 0, x0 + tile, height)), (cap, 0))
    out.paste(small.crop((small.width - cap, 0, small.width, height)), (cap + tile, 0))
    return quantize(out)


def divider(folder: Path) -> Image.Image:
    src = load(folder, 'divider')
    height = 7  # cabe entre secciones del libro; la línea es fina, con un umbral de alfa bajo no desaparece
    return quantize(shrink(src, round(src.width * height / src.height), height, cut=24))


def corner(folder: Path) -> Image.Image:
    src = load(folder, 'corner')
    return quantize(shrink(src, 12, round(12 * src.height / src.width)))


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    folder = Path(argv[1])
    OUT.mkdir(parents=True, exist_ok=True)
    pieces = {}
    for name in FRAMES:
        body, eye, book = frame(folder, name)
        pieces[f'{name}.png'] = body
        if name == 'window':
            pieces['emblema_ojo.png'], pieces['emblema_libro.png'] = eye, book
    pieces['ribbon.png'] = ribbon(folder)
    pieces['divider.png'] = divider(folder)
    pieces['corner.png'] = corner(folder)
    for name, image in pieces.items():
        image.save(OUT / name)
        print(f'{name}: {image.width}×{image.height}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
