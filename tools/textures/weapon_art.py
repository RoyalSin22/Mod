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
MIN_ASPECT = 0.3  # lado corto / lado largo mínimo de la pieza de un arma (ver shape_problem y split_auto)


def limits(size):
    """Límites de validación según la densidad: (máx. colores, mín. y máx. de píxeles opacos, largo mínimo)."""
    k = size // 16
    return {16: 24, 32: 40, 64: 56}[size], (24 if k == 1 else 16 * k * k), 200 * k * k, 10 * k


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
    """Si la imagen no tiene transparencia de verdad, quita el fondo: color más frecuente del borde y relleno desde el borde.

    Las imágenes de los generadores suelen traer esquinas redondeadas transparentes con el fondo magenta opaco: eso no cuenta como
    «ya transparente». Solo se respeta la transparencia si afecta a más de una quinta parte de la imagen."""
    img = img.convert('RGBA')
    px = img.load()
    w, h = img.size
    clear = sum(1 for x in range(w) for y in range(h) if px[x, y][3] < 128)
    if clear > 0.2 * w * h:
        return img
    border = [(x, y) for x in range(w) for y in (0, h - 1)] + [(x, y) for y in range(h) for x in (0, w - 1)]
    colors = Counter(px[x, y][:3] for x, y in border if px[x, y][3] >= 128)
    if not colors:
        return img
    # Fondos: el color dominante del borde (a veces un marco negro de la captura) y el dominante de toda la imagen (el magenta).
    # Un fondo saturado (magenta) admite la tolerancia completa; uno gris o negro solo una muy pequeña, para no comerse los
    # contornos oscuros del arma.
    def tol_for(c):
        return tolerance if max(c) - min(c) > 100 else min(tolerance, 10)

    backgrounds = [(colors.most_common(1)[0][0],)]
    overall = Counter(px[x, y][:3] for x in range(0, w, 2) for y in range(0, h, 2) if px[x, y][3] >= 128)
    top, count = overall.most_common(1)[0]
    if count * 4 > (w // 2) * (h // 2) * 0.25 and top != backgrounds[0][0]:
        backgrounds.append((top,))
    backgrounds = [(c[0], tol_for(c[0])) for c in backgrounds]

    def near(c):
        return c[3] < 128 or any(max(abs(c[i] - bg[i]) for i in range(3)) <= t for bg, t in backgrounds)

    seen = set()
    stack = list(border)
    while stack:
        x, y = stack.pop()
        if (x, y) in seen or not (0 <= x < w and 0 <= y < h):
            continue
        seen.add((x, y))
        if not near(px[x, y]):
            continue
        px[x, y] = (0, 0, 0, 0)
        stack.extend(((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))
    # Huecos cerrados del arma (el interior de un anillo de cuentas, un asa…): el magenta que quedó dentro no está conectado al
    # borde. Un fondo saturado se quita en toda la imagen; los grises y negros solo por conexión, para no comerse contornos.
    saturated = [(bg, t) for bg, t in backgrounds if max(bg) - min(bg) > 100]
    if saturated:
        for y in range(h):
            for x in range(w):
                c = px[x, y]
                if c[3] and any(max(abs(c[i] - bg[i]) for i in range(3)) <= t for bg, t in saturated):
                    px[x, y] = (0, 0, 0, 0)
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
    for y in range(size):
        for x in range(size):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 255 if a >= 128 else 0)
    canvas = defringe(canvas, rings=2 if size >= 32 else 1)
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


def depth_map(img, thickness=1):
    """Grosor 3D por píxel para el modelo de la mano (V76.1): uniforme, como los objetos de vanilla. Antes variaba con la
    luminosidad (el contorno y la madera más gruesos) y la hoja quedaba llena de bultos; con un grosor constante la
    superficie es lisa y el relieve lo da la propia textura."""
    img = img.convert('RGBA')
    px = img.load()
    n = img.size[0]
    return [[0 if px[x, y][3] < 255 else thickness for x in range(n)] for y in range(n)]


def bevel_depth_map(img, min_units=0.5, max_units=1.5, per_cell=0.25, step=0.25):
    """Grosor 3D biselado (V76.2): crece desde el borde de la silueta hacia el interior, como un objeto tallado.

    ``min_units``: grosor en la orilla; sube ``per_cell`` unidades por cada píxel que se entra, hasta ``max_units``; se
    redondea a múltiplos de ``step`` para que los bloques contiguos se puedan fundir. Las hojas quedan con una arista central, los
    mangos redondeados y las gemas abombadas. Distancia de ajedrez (8 vecinos) a la transparencia más cercana."""
    img = img.convert('RGBA')
    px = img.load()
    n = img.size[0]
    inf = 10 ** 6
    dist = [[0 if px[x, y][3] < 255 else inf for x in range(n)] for y in range(n)]
    from collections import deque
    queue = deque((x, y) for y in range(n) for x in range(n) if dist[y][x] == 0)
    # Los bordes de la imagen cuentan como vacío.
    for x in range(n):
        for y in (0, n - 1):
            if dist[y][x] == inf:
                dist[y][x] = 1
                queue.append((x, y))
    for y in range(n):
        for x in (0, n - 1):
            if dist[y][x] == inf:
                dist[y][x] = 1
                queue.append((x, y))
    while queue:
        x, y = queue.popleft()
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                qx, qy = x + dx, y + dy
                if (dx or dy) and 0 <= qx < n and 0 <= qy < n and dist[qy][qx] > dist[y][x] + 1:
                    dist[qy][qx] = dist[y][x] + 1
                    queue.append((qx, qy))
    out = [[0.0] * n for _ in range(n)]
    for y in range(n):
        for x in range(n):
            if dist[y][x] == 0:
                continue
            t = min(max_units, min_units + per_cell * (dist[y][x] - 1))
            out[y][x] = round(round(t / step) * step, 4)
    return out


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


def split_auto(img, tolerance=40, min_area=0.0005, join=0.03, report=None):
    """Detecta las armas de una imagen sin fijarse en cuadrículas (V75): quita el fondo, une las piezas cercanas de cada arma y
    devuelve una imagen RGBA recortada por arma, ordenadas por filas (arriba abajo) y de izquierda a derecha.

    ``min_area``: fracción mínima de la imagen que debe ocupar una pieza para contar (descarta motas).
    ``join``: distancia (fracción del lado menor) a la que dos piezas se consideran la misma arma.
    Las piezas pegadas al borde de la imagen se descartan (restos del marco o armas cortadas, que no sirven); si se pasa un
    diccionario en ``report`` se anota cuántas en ``report['edge']``.
    """
    img = remove_background(img, tolerance)
    w, h = img.size
    scale = min(1.0, 320 / max(w, h))
    sw, sh = max(1, round(w * scale)), max(1, round(h * scale))
    alpha = img.getchannel('A').point(lambda a: 255 if a >= 128 else 0).resize((sw, sh), Image.BOX).point(lambda a: 255 if a >= 64 else 0)
    r = max(1, round(min(sw, sh) * join))
    mask = alpha.load()
    # Etiquetado por relleno con puentes de radio r (cada píxel opaco une a los vecinos opacos dentro de r).
    pts = {(x, y) for y in range(sh) for x in range(sw) if mask[x, y]}
    label, groups = {}, []
    for start in sorted(pts):
        if start in label:
            continue
        stack, comp = [start], []
        label[start] = len(groups)
        while stack:
            x, y = stack.pop()
            comp.append((x, y))
            for dy in range(-r, r + 1):
                for dx in range(-r, r + 1):
                    q = (x + dx, y + dy)
                    if q in pts and q not in label:
                        label[q] = len(groups)
                        stack.append(q)
        groups.append(comp)
    total = sw * sh
    groups = [g for g in groups if len(g) >= total * min_area]
    # Restos del marco o de la captura: piezas pegadas al borde que además son estrechas o mucho más pequeñas que las demás.
    # (Un arma que toca el borde pero tiene cuerpo se conserva: el generador a veces la deja muy cerca del límite.)
    m = max(2, round(min(sw, sh) * 0.03))
    biggest = max((len(g) for g in groups), default=0)

    def stray(g):
        xs, ys = [p[0] for p in g], [p[1] for p in g]
        touches = min(xs) <= m or min(ys) <= m or max(xs) >= sw - 1 - m or max(ys) >= sh - 1 - m
        bw, bh = max(xs) - min(xs) + 1, max(ys) - min(ys) + 1
        return touches and (min(bw, bh) / max(bw, bh) < MIN_ASPECT or len(g) < 0.2 * biggest)

    edge = [g for g in groups if stray(g)]
    if report is not None:
        report['edge'] = len(edge)
    groups = [g for g in groups if g not in edge]
    boxes = []
    for g in groups:
        xs, ys = [p[0] for p in g], [p[1] for p in g]
        boxes.append((min(xs), min(ys), max(xs) + 1, max(ys) + 1, g))
    if not boxes:
        return []
    # Orden de lectura: se agrupan en filas por solapamiento vertical (aunque las armas tengan tamaños muy distintos) y dentro
    # de cada fila se ordenan de izquierda a derecha.
    boxes.sort(key=lambda b: b[1])
    rows = []
    for b in boxes:
        for row in rows:
            ry0, ry1 = min(x[1] for x in row), max(x[3] for x in row)
            overlap = min(b[3], ry1) - max(b[1], ry0)
            if overlap >= 0.3 * min(b[3] - b[1], ry1 - ry0):
                row.append(b)
                break
        else:
            rows.append([b])
    rows.sort(key=lambda row: min(x[1] for x in row))
    ordered = [b for row in rows for b in sorted(row, key=lambda b: b[0])]
    crops = []
    full = img.load()
    for x0, y0, x1, y1, comp in ordered:
        pad = r
        fx0, fy0 = max(0, int((x0 - pad) / scale)), max(0, int((y0 - pad) / scale))
        fx1, fy1 = min(w, int((x1 + pad) / scale) + 1), min(h, int((y1 + pad) / scale) + 1)
        part = img.crop((fx0, fy0, fx1, fy1))
        keep = {(px // 1, py // 1) for px, py in comp}
        pp = part.load()
        for y in range(part.size[1]):
            for x in range(part.size[0]):
                if pp[x, y][3] and (int((x + fx0) * scale), int((y + fy0) * scale)) not in keep:
                    # píxel opaco que no pertenece a esta arma (vecina): se borra
                    sx, sy = int((x + fx0) * scale), int((y + fy0) * scale)
                    near = any((sx + dx, sy + dy) in keep for dx in (-1, 0, 1) for dy in (-1, 0, 1))
                    if not near:
                        pp[x, y] = (0, 0, 0, 0)
        crops.append(part)
    return crops


def shape_problem(img, tolerance=40):
    """Texto del problema si la pieza no parece un arma en diagonal (astilla estrecha o raya), o ``None``.

    Un arma en diagonal de 45° ocupa un cuadro casi cuadrado; un bastón largo, algo más estrecho. Una pieza cuyo cuadro mide menos
    de ``MIN_ASPECT`` en su lado corto respecto al largo es una astilla (el generador dibujó una forma demasiado fina) o está
    vertical u horizontal: al reducirla se vería como una raya."""
    box = remove_background(img, tolerance).getchannel('A').point(lambda a: 255 if a >= 128 else 0).getbbox()
    if box is None:
        return 'la imagen está vacía (todo transparente)'
    w, h = box[2] - box[0], box[3] - box[1]
    if min(w, h) / max(w, h) < MIN_ASPECT:
        return (f'la pieza mide {w}×{h}: demasiado estrecha para un arma en diagonal (¿una astilla, una línea o un arma '
                f'vertical/horizontal?). Pide esa arma de nuevo con cuerpo y en diagonal a 45°')
    return None


JOIN_CANDIDATES = (0.03, 0.02, 0.015, 0.01, 0.045, 0.06, 0.08, 0.1, 0.13)


def split_auto_expect(img, expected, tolerance=40, join=None):
    """Como ``split_auto`` pero busca el radio de unión que da exactamente ``expected`` armas (hojas con armas de piezas sueltas,
    como espadas flotantes, o con armas muy juntas). Devuelve ``(recortes, join usado, informe)``; si ninguno cuadra, los del radio
    por defecto."""
    candidates = (join,) if join is not None else JOIN_CANDIDATES
    first = None
    for j in candidates:
        report = {}
        crops = split_auto(img, tolerance=tolerance, join=j, report=report)
        if first is None:
            first = (crops, j, report)
        if len(crops) == expected:
            return crops, j, report
    return first


def is_fringe(c):
    """¿Parece un píxel de borde contaminado por el fondo magenta? Oscuro y con rojo y azul casi iguales y verde muy bajo
    (mezcla de un contorno oscuro con #FF00FF). Los morados intencionados tienen el azul claramente por encima del rojo."""
    r, g, b = c[:3]
    return r >= 28 and b >= 28 and g <= 34 and abs(r - b) <= 26 and g < 0.45 * min(r, b) + 6


def defringe(img, rings=2, outline=0.36):
    """Quita el tinte magenta del contorno (V76.1): los píxeles de borde contaminados por el fondo se rehacen como un contorno
    oscuro derivado del color vecino sano más cercano (``outline`` = fracción de su brillo), o se borran si no tienen ninguno.
    Se aplica a los ``rings`` píxeles más externos, que es donde el generador mezcla el magenta."""
    img = img.convert('RGBA')
    px = img.load()
    n = img.size[0]
    for _ in range(rings):
        changes = {}
        for y in range(n):
            for x in range(n):
                c = px[x, y]
                if c[3] < 255 or not is_fringe(c):
                    continue
                if not any(not (0 <= x + dx < n and 0 <= y + dy < n) or px[x + dx, y + dy][3] == 0
                           for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, -1), (1, -1), (-1, 1))):
                    continue  # solo píxeles de la orilla; el interior intencionado no se toca
                neighbours = []
                for dy in range(-2, 3):
                    for dx in range(-2, 3):
                        qx, qy = x + dx, y + dy
                        if (dx or dy) and 0 <= qx < n and 0 <= qy < n and px[qx, qy][3] == 255 and not is_fringe(px[qx, qy]):
                            neighbours.append(px[qx, qy][:3])
                if neighbours:
                    avg = [sum(v[i] for v in neighbours) / len(neighbours) for i in range(3)]
                    changes[(x, y)] = tuple(max(0, min(255, round(a * outline))) for a in avg) + (255,)
                else:
                    changes[(x, y)] = (0, 0, 0, 0)
        for (x, y), c in changes.items():
            px[x, y] = c
    return img
