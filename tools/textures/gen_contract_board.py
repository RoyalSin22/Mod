#!/usr/bin/env python3
"""Genera la textura pixel art del tablón de contratos (16×16, semilla fija)."""
import random
import sys
from pathlib import Path
from PIL import Image, ImageDraw

SEED = 30030
def main():
    if len(sys.argv) != 2:
        raise SystemExit("Uso: python tools/textures/gen_contract_board.py <carpeta textures/block>")
    rng = random.Random(SEED)
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    wood = [(105, 64, 35, 255), (126, 77, 42, 255), (145, 91, 50, 255), (83, 49, 29, 255)]
    draw.rectangle((2, 1, 13, 14), fill=wood[0], outline=(50, 31, 20, 255))
    for y in (3, 7, 11):
        draw.line((3, y, 12, y), fill=wood[1 + rng.randrange(2)])
        draw.point((rng.randint(3, 12), y + 1), fill=wood[3])
    papers = [(4, 3, 8, 8), (8, 4, 12, 9), (5, 9, 10, 13)]
    for x1, y1, x2, y2 in papers:
        draw.rectangle((x1, y1, x2, y2), fill=(219, 204, 164, 255), outline=(68, 48, 33, 255))
        draw.point((x1 + 1, y1 + 2), fill=(96, 61, 42, 255))
        draw.line((x1 + 1, y1 + 3, x2 - 1, y1 + 3), fill=(145, 89, 60, 255))
    for x, y in ((6, 3), (10, 4), (7, 9)):
        draw.point((x, y), fill=(157, 45, 36, 255))
        draw.point((x, y + 1), fill=(88, 32, 27, 255))
    image.save(Path(sys.argv[1]) / "contract_board.png")

if __name__ == "__main__": main()
