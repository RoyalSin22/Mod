"""V75: arte externo de las armas de evolución (Maestro), p. ej. generado con ChatGPT.

Las imágenes entregadas se normalizan con ``import_weapon_art.py`` y se guardan como PNG de 64×64 (o 32×32 / 16×16) en
``tools/textures/source/weapons/<rol>_weapon_master.png``. Si existe ese archivo, ``gen_items.py`` lo usa en lugar del diseño por
plantilla (``evolved_weapons.py``, que queda como respaldo) y ``gen_weapon_models.py`` extruye el modelo 3D de la mano a
partir de él. Este módulo solo usa Pillow: sin Minecraft ni dependencias del resto de generadores.
"""
from collections import Counter
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'tools/textures/source/weapons'
SIZES = (16, 32, 64)
DEFAULT_SIZE = 64  # alta densidad: el modelo 3D de la mano usa bloques de ¼ de píxel; el icono de inventario se reduce a 32×32
GUI_SIZE = 32
SIZE = DEFAULT_SIZE
SUFFIX = '_weapon_master'


def limits(size):
    """Límites de validación según la densidad: (máx. colores, mín. y máx. de píxeles opacos, largo mínimo)."""
    k = size // 16
    return {16: 24, 32: 40, 64: 56}[size], 24 * k * k, 200 * k * k, 10 * k


def path_for(name):
    return SOURCE / f'{name}.png'


def override(name):
    """Imagen 16×16 RGBA del arte externo de ``name``, o ``None`` si no hay (se usa entonces la plantilla)."""
    p = path_for(name)
    if not p.is_file():
        return None
    img = Image.open(p).convert('RGBA')
    return img if img.size in ((n, n) for n in SIZES) else None


def validate(img):
    """Devuelve ``(errores, avisos)`` de una imagen ya normalizada (16×16 o 32×32)."""
    errors, warnings = [], []
    if img.size not in ((n, n) for n in SIZES):
        return [f'tamaño {img.size[0]}×{img.size[1]}; debe ser 16×16, 32×32 o 64×64'], warnings
    size = img.size[0]
    max_colors, min_opaque, max_opaque, min_span = limits(size)
    img = img.convert('RGBA')
    px = img.load()
    opaque = [(x, y) for y in range(size) for x in range(size) if px[x, y][3] > 0]
    partial = [(x, y) for x, y in opaque if px[x, y][3] != 255]
    if partial:
        errors.append(f'{len(partial)} píxeles semitransparentes (solo se admite 0 % o 100 % de opacidad)')
    if not (min_opaque <= len(opaque) <= max_opaque):
        errors.append(f'{len(opaque)} píxeles opacos; esperados entre {min_opaque} y {max_opaque}')
    colors = {px[x, y][:3] for x, y in opaque}
    if len(colors) > max_colors:
        errors.append(f'{len(colors)} colores; máximo {max_colors}')
    if opaque:
        xs, ys = [p[0] for p in opaque], [p[1] for p in opaque]
        if max(max(xs) - min(xs), max(ys) - min(ys)) + 1 < min_span:
            errors.append(f'el arma ocupa menos de {min_span} píxeles de largo')
        half = size // 2
        upper_right = any(x >= half and y < half for x, y in opaque)
        lower_left = any(x < half and y >= half for x, y in opaque)
        if not (upper_right and lower_left):
            warnings.append('la silueta no cruza la diagonal: el arma debe ir de la empuñadura (abajo a la izquierda) a la punta (arriba a la derecha)')
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


def normalize(img, colors=24, tolerance=40, fit=True, flip=False, rotate=0, size=DEFAULT_SIZE):
    """Imagen cualquiera → ``size``×``size`` (16, 32 o 64) RGBA con bordes duros y paleta reducida."""
    if size not in SIZES:
        raise ValueError(f'tamaño {size} no admitido; usa 16, 32 o 64')
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
    target = size - 1 if fit else size
    scale = target / max(w, h)
    nw, nh = max(1, round(w * scale)), max(1, round(h * scale))
    small = img.convert('RGBa').resize((nw, nh), Image.BOX).convert('RGBA')
    canvas = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    canvas.paste(small, ((size - nw) // 2, (size - nh) // 2))
    px = canvas.load()
    rgb = Image.new('RGB', (size, size), (0, 0, 0))
    for y in range(size):
        for x in range(size):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255 if a >= 128 else 0)
            rgb.putpixel((x, y), (r, g, b) if a >= 128 else (0, 0, 0))
    quant = rgb.quantize(colors=max(2, colors), method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert('RGB')
    qp = quant.load()
    for y in range(size):
        for x in range(size):
            if px[x, y][3]:
                px[x, y] = qp[x, y] + (255,)
            else:
                px[x, y] = (0, 0, 0, 0)
    return canvas


def luminance(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def depth_map(img, dark=85):
    """Grosor 3D por píxel para el modelo de la mano: 1 de base y 2 en lo oscuro (empuñadura, contorno, madera, cuero)."""
    img = img.convert('RGBA')
    px = img.load()
    n = img.size[0]
    return [[0 if px[x, y][3] < 255 else (2 if luminance(px[x, y]) < dark else 1) for x in range(n)] for y in range(n)]


def downscale(img, size=GUI_SIZE):
    """Versión reducida (caja) con bordes duros, p. ej. el icono de inventario de un arte de 64×64."""
    img = img.convert('RGBA')
    if img.size[0] <= size:
        return img
    small = img.convert('RGBa').resize((size, size), Image.BOX).convert('RGBA')
    px = small.load()
    for y in range(size):
        for x in range(size):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255 if a >= 128 else 0)
    return small
