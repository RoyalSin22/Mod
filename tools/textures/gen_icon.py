#!/usr/bin/env python3
"""
Generador del icono del mod (v38): 128×128, diseño propio.

Un emblema circular oscuro con anillo dorado, un anillo carmesí interior (guiño al Eclipse) y una punta de
flecha que sube en escalones (las aptitudes que crecen con el uso).

Uso:  python3 tools/textures/gen_icon.py <carpeta assets/aptitudes>
Escribe <carpeta>/icon.png. Semilla y geometría fijas: el resultado es siempre el mismo. Requiere Pillow.
"""
import math
import os
import sys
from PIL import Image, ImageDraw, ImageFilter

S = 128
SS = 4  # supermuestreo para bordes suaves


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    N = S * SS
    c = N / 2
    img = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def circle(r, fill=None, outline=None, width=0):
        d.ellipse((c - r, c - r, c + r, c + r), fill=fill, outline=outline, width=width)

    # Halo dorado suave
    glow = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse((c - 60 * SS, c - 60 * SS, c + 60 * SS, c + 60 * SS), fill=(224, 180, 58, 90))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(6 * SS)))

    circle(58 * SS, fill=(28, 22, 34, 255))                        # disco
    circle(58 * SS, outline=(224, 180, 58, 255), width=5 * SS)       # anillo dorado
    circle(47 * SS, outline=(150, 22, 30, 255), width=3 * SS)        # anillo carmesí

    # Marcas del anillo (12, como horas)
    for i in range(12):
        a = i * math.pi / 6
        r1, r2 = 50 * SS, 54 * SS
        d.line((c + r1 * math.cos(a), c + r1 * math.sin(a), c + r2 * math.cos(a), c + r2 * math.sin(a)),
               fill=(224, 180, 58, 255), width=2 * SS)

    # Escalones que suben (tres barras crecientes)
    bars = [(-26, 14, 10), (-8, 2, 10), (10, -12, 10)]  # (x, y superior, ancho) en px del icono
    for x, top, w in bars:
        d.rounded_rectangle((c + x * SS, c + top * SS, c + (x + w) * SS, c + 26 * SS),
                            radius=2 * SS, fill=(224, 180, 58, 255))

    # Punta de flecha sobre la barra más alta
    tip = [(c + 4 * SS, c - 14 * SS), (c + 15 * SS, c - 34 * SS), (c + 26 * SS, c - 14 * SS)]
    d.polygon(tip, fill=(255, 226, 120, 255))
    d.line((c - 30 * SS, c + 30 * SS, c + 26 * SS, c + 30 * SS), fill=(150, 22, 30, 255), width=3 * SS)

    out = img.resize((S, S), Image.LANCZOS)
    path = os.path.join(sys.argv[1], 'icon.png')
    out.save(path, optimize=True)
    print(path)


if __name__ == '__main__':
    main()
