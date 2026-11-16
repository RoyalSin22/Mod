"""V75: arte externo de las armas de evolución (Maestro), p. ej. generado con ChatGPT.

Las imágenes entregadas se normalizan con ``import_weapon_art.py`` y se guardan como PNG de 16×16 en
``tools/textures/source/weapons/<rol>_weapon_master.png``. Si existe ese archivo, ``gen_items.py`` lo usa en lugar del diseño por
plantilla (``evolved_weapons.py``, que queda como respaldo) y ``gen_weapon_models.py`` extruye el modelo 3D de la mano a
partir de él. Este módulo solo usa Pillow: sin Minecraft ni dependencias del resto de generadores.
"""
from collections import Counter
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/weapons'
SIZE = 16
MAX_COLORS = 24
MIN_OPAQUE, MAX_OPAQUE = 24, 200
MIN_SPAN = 10
SUFFIX = '_weapon_master'


def path_for(name):
    return SOURCE / f'{name}.png'


def override(name):
    """Imagen 16×16 RGBA del arte externo de ``name``, o ``None`` si no hay (se usa entonces la plantilla)."""
    p = path_for(name)
    if not p.is_file():
        return None
    img = Image.open(p).convert('RGBA')
    return img if img.size == (SIZE, SIZE) else None


def validate(img):
    """Devuelve ``(errores, avisos)`` de una imagen ya normalizada."""
    errors, warnings = [], []
    if img.size != (SIZE, SIZE):
        return [f'tamaño {img.size[0]}×{img.size[1]}; debe ser {SIZE}×{SIZE}'], warnings
    img = img.convert('RGBA')
    px = img.load()
    opaque = [(x, y) for y in range(SIZE) for x in range(SIZE) if px[x, y][3] > 0]
    partial = [(x, y) for x, y in opaque if px[x, y][3] != 255]
    if partial:
        errors.append(f'{len(partial)} píxeles semitransparentes (solo se admite 0 % o 100 % de opacidad)')
    if not (MIN_OPAQUE <= len(opaque) <= MAX_OPAQUE):
        errors.append(f'{len(opaque)} píxeles opacos; esperados entre {MIN_OPAQUE} y {MAX_OPAQUE}')
    colors = {px[x, y][:3] for x, y in opaque}
    if len(colors) > MAX_COLORS:
        errors.append(f'{len(colors)} colores; máximo {MAX_COLORS}')
    if opaque:
        xs, ys = [p[0] for p in opaque], [p[1] for p in opaque]
        if max(max(xs) - min(xs), max(ys) - min(ys)) + 1 < MIN_SPAN:
            errors.append(f'el arma ocupa menos de {MIN_SPAN} píxeles de largo')
        upper_right = any(x >= 8 and y < 8 for x, y in opaque)
        lower_left = any(x < 8 and y >= 8 for x, y in opaque)
        if not (upper_right and lower_left):
            warnings.append('la silueta no cruza la diagonal: el arma debe ir de la empuñadura (abajo a la izquierda) a la punta (arriba a la derecha)')
        if min(xs) == 0 or min(ys) == 0 or max(xs) == SIZE - 1 or max(ys) == SIZE - 1:
            pass  # tocar el borde es normal en armas diagonales de vanilla
    return errors, warnings


def remove_background(img, tolerance):
    """Si la imagen no tiene transparencia, quita el fondo: color más frecuente de las esquinas y relleno desde el borde."""
    img = img.convert('RGBA')
    px = img.load()
    w, h = img.size
    if any(px[x, y][3] < 255 for x in range(w) for y in range(h)):
        return img
    corners = Counter(px[x, y][:3] for x, y in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)))
    bg = corners.most_common(1)[0][0]

    def near(c):
        return max(abs(c[i] - bg[i]) for i in range(3)) <= tolerance

    seen = set()
    stack = [(x, y) for x in range(w) for y in (0, h - 1)] + [(x, y) for y in range(h) for x in (0, w - 1)]
    while stack:
        x, y = stack.pop()
        if (x, y) in seen or not (0 <= x < w and 0 <= y < h):
            continue
        seen.add((x, y))
        if not near(px[x, y]):
            continue
        px[x, y] = (0, 0, 0, 0)
        stack.extend(((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))
    return img


def normalize(img, colors=16, tolerance=40, fit=True, flip=False, rotate=0):
    """Imagen cualquiera → 16×16 RGBA con bordes duros y paleta reducida."""
    img = remove_background(img, tolerance)
    if flip:
        img = img.transpose(Image.FLIP_LEFT_RIGHT)
    if rotate:
        img = img.rotate(rotate, expand=True)
    bbox = img.getchannel('A').point(lambda a: 255 if a >= 128 else 0).getbbox()
    if bbox is None:
        raise ValueError('la imagen está vacía (todo transparente)')
    img = img.crop(bbox)
    w, h = img.size
    target = SIZE - 1 if fit else SIZE
    scale = target / max(w, h)
    nw, nh = max(1, round(w * scale)), max(1, round(h * scale))
    small = img.convert('RGBa').resize((nw, nh), Image.BOX).convert('RGBA')
    canvas = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    canvas.paste(small, ((SIZE - nw) // 2, (SIZE - nh) // 2))
    px = canvas.load()
    rgb = Image.new('RGB', (SIZE, SIZE), (0, 0, 0))
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255 if a >= 128 else 0)
            rgb.putpixel((x, y), (r, g, b) if a >= 128 else (0, 0, 0))
    quant = rgb.quantize(colors=max(2, colors), method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert('RGB')
    qp = quant.load()
    for y in range(SIZE):
        for x in range(SIZE):
            if px[x, y][3]:
                px[x, y] = qp[x, y] + (255,)
            else:
                px[x, y] = (0, 0, 0, 0)
    return canvas


def luminance(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def depth_map(img, dark=85):
    """Grosor 3D por píxel para el modelo de la mano: 1 de base y 2 en lo oscuro (empuñadura, contorno, madera, cuero)."""
    px = img.convert('RGBA').load()
    return [[0 if px[x, y][3] < 255 else (2 if luminance(px[x, y]) < dark else 1) for x in range(SIZE)] for y in range(SIZE)]
