#!/usr/bin/env python3
"""
Generador de la textura del sol eclipsado (v34): disco oscuro con una corona carmesí irregular.

Diseño propio. El cielo de Minecraft dibuja el sol con mezcla aditiva: el negro no se suma, así que el
centro deja ver el cielo (rojo oscuro durante el eclipse) y lo que destaca es el anillo y sus llamaradas.

Uso:  python3 tools/textures/gen_eclipse.py <carpeta textures>
Escribe <carpeta>/environment/eclipse_sun.png (32×32). Semilla fija: el resultado es siempre el mismo.
Requiere Pillow.
"""
import math
import os
import random
import sys
from PIL import Image

SIZE = 32
SEED = 34_034
CENTER = (SIZE - 1) / 2.0
DISC = 7.0        # radio del disco oscuro
RING = 9.0        # radio del anillo brillante
FLARE = 15.0      # alcance máximo de las llamaradas


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    rng = random.Random(SEED)
    # Llamaradas: 9 picos de longitud y anchura aleatorias (fijas por la semilla).
    flares = [(rng.uniform(0, 2 * math.pi), rng.uniform(0.35, 1.0), rng.uniform(0.18, 0.40)) for _ in range(9)]
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    px = img.load()
    for y in range(SIZE):
        for x in range(SIZE):
            dx, dy = x - CENTER, y - CENTER
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            if r <= DISC:
                px[x, y] = (0, 0, 0, 255)
                continue
            # Anillo: máximo en RING, cae hacia dentro y hacia fuera.
            ring = max(0.0, 1.0 - abs(r - RING) / 2.2)
            # Llamaradas: suman brillo en su dirección, decreciendo con la distancia.
            flare = 0.0
            for ang, length, width in flares:
                d = math.atan2(math.sin(a - ang), math.cos(a - ang))
                if abs(d) < width:
                    reach = RING + length * (FLARE - RING)
                    if r <= reach:
                        flare = max(flare, (1.0 - abs(d) / width) * (1.0 - (r - RING) / max(0.5, reach - RING)))
            glow = max(0.0, 1.0 - (r - DISC) / (FLARE - DISC)) * 0.25
            v = min(1.0, ring + flare * 0.8 + glow)
            if v <= 0.02:
                continue
            # Del rojo profundo al rojo anaranjado en el borde interior del anillo.
            hot = max(0.0, 1.0 - abs(r - (DISC + 1.2)) / 1.5)
            red = int(140 + 115 * v)
            green = int(10 + 70 * hot * v)
            blue = int(14 + 20 * v)
            px[x, y] = (min(255, red), min(255, green), min(255, blue), int(255 * v))
    out = os.path.join(sys.argv[1], 'environment')
    os.makedirs(out, exist_ok=True)
    img.save(os.path.join(out, 'eclipse_sun.png'))
    print('eclipse_sun.png generado')


if __name__ == '__main__':
    main()
