#!/usr/bin/env python3
"""
Arte de 32×32 de objetos que no son armas (C3). ``gen_items.py`` lo usa antes que la plantilla de 16×16:
``override(nombre)`` devuelve la imagen RGBA de 32×32, o ``None`` si el objeto no tiene versión grande.

Hoy: el Astrolabio del Ocaso (disco de bronce ennegrecido, aro de suspensión y un sol eclipsado: el núcleo negro
queda desplazado abajo a la izquierda y deja una corona de brasa arriba a la derecha). Se dibuja con geometría y
la misma luz que el resto del mod (arriba-izquierda), para que sea reproducible: no se edita el PNG a mano.

Uso suelto (vista previa):  python3 tools/textures/hd_items.py salida.png
"""
import math
import sys
from PIL import Image

SIZE = 32

# Bronce: [contorno, sombra, base, luz, brillo]
BRONZE = [(0x2a, 0x18, 0x08), (0x6b, 0x3f, 0x14), (0xa8, 0x65, 0x1c), (0xd0, 0x8a, 0x30), (0xf0, 0xb8, 0x58)]
GROOVE = (0x4a, 0x2a, 0x0c)
CORE = [(0x08, 0x05, 0x0c), (0x0d, 0x09, 0x14), (0x1a, 0x14, 0x24), (0x2a, 0x20, 0x3c)]
# Corona, del borde del núcleo hacia afuera
CORONA = [(0xff, 0xf0, 0xb0), (0xff, 0xb3, 0x47), (0xff, 0x7a, 0x1f), (0xc4, 0x43, 0x0c), (0x7a, 0x2a, 0x08)]


def _bronze(nx, ny):
    """Tono de bronce según la normal (luz arriba-izquierda): -1 = sombra profunda … 1 = brillo."""
    t = (-nx - ny) / math.sqrt(2)
    if t > 0.62:
        return BRONZE[4]
    if t > 0.2:
        return BRONZE[3]
    if t > -0.3:
        return BRONZE[2]
    if t > -0.7:
        return BRONZE[1]
    return BRONZE[0]


def astrolabe():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    px = img.load()
    cx, cy, radius = 15.5, 19.5, 12.5
    ring_cx, ring_cy = 15.5, 4.5

    # Aro de suspensión (anillo con hueco).
    for y in range(SIZE):
        for x in range(SIZE):
            d = math.hypot(x - ring_cx, y - ring_cy)
            if 2.3 < d <= 4.6:
                px[x, y] = _bronze((x - ring_cx) / max(d, 0.1), (y - ring_cy) / max(d, 0.1)) + (255,)
    # Cuello que une el aro con el disco.
    for y in range(8, 10):
        for x in range(14, 18):
            px[x, y] = BRONZE[2 if x < 16 else 1] + (255,)

    # Disco.
    core_cx, core_cy, core_r = cx - 1.6, cy + 1.6, 6.6
    for y in range(SIZE):
        for x in range(SIZE):
            d = math.hypot(x - cx, y - cy)
            if d > radius:
                continue
            nx, ny = (x - cx) / max(d, 0.1), (y - cy) / max(d, 0.1)
            if d > 10.5:  # borde de bronce
                color = _bronze(nx, ny)
                angle = math.degrees(math.atan2(y - cy, x - cx)) % 360
                if 11.0 < d <= 12.0 and min(angle % 30, 30 - angle % 30) < 2.2:
                    color = BRONZE[0]  # doce marcas grabadas, como las de un reloj de sol
            elif d > 9.4:  # ranura oscura entre el borde y la corona
                color = GROOVE
            else:
                cd = math.hypot(x - core_cx, y - core_cy)
                if cd <= core_r:
                    t = (-(x - core_cx) - (y - core_cy)) / (math.sqrt(2) * max(cd, 0.1))
                    color = CORE[3] if (cd > core_r - 1.3 and t > 0.55) else CORE[2] if cd > core_r - 2.6 and t > 0.1 else CORE[1]
                    if cd <= core_r - 3.5:
                        color = CORE[0] if (x + y) % 5 else CORE[1]
                else:
                    u = ((x - core_cx) - (y - core_cy)) / (math.sqrt(2) * max(cd, 0.1))  # +1 = arriba a la derecha
                    beyond = cd - core_r + max(0.0, 0.35 - u) * 5.5  # la corona se apaga en el lado tapado
                    k = 0 if beyond < 1.0 else 1 if beyond < 2.6 else 2 if beyond < 4.6 else 3 if beyond < 6.6 else 4
                    color = CORONA[k]
            px[x, y] = color + (255,)

    # Destellos de la corona.
    for (x, y) in ((20, 13), (21, 14), (19, 12)):
        px[x, y] = (255, 255, 255, 255)
    px[22, 15] = (255, 240, 176, 255)

    # Contorno oscuro (vecinos ortogonales).
    src = img.copy().load()
    for y in range(SIZE):
        for x in range(SIZE):
            if src[x, y][3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < SIZE and 0 <= ny < SIZE and src[nx, ny][3]:
                    px[x, y] = BRONZE[0] + (255,)
                    break
    return img


ITEMS = {'sunset_astrolabe': astrolabe}


def override(name):
    """Imagen de 32×32 de ``name``, o ``None`` si ese objeto solo tiene la plantilla de 16×16."""
    make = ITEMS.get(name)
    return make() if make else None


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else 'astrolabe_hd.png'
    astrolabe().save(out)
    print(out)
