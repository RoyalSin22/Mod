#!/usr/bin/env python3
"""Genera las texturas de la Mesa del gremio y la Hoguera (16 px por cara)."""

import argparse
from io import BytesIO
from pathlib import Path
from random import Random

from PIL import Image, ImageColor, ImageDraw

BRASS = ("#4c3225", "#8f5a31", "#c48743", "#e9b975")
JADE = ("#143033", "#265453", "#418474", "#8dc0a1")
WOOD = ("#231c21", "#35262a", "#493136", "#594044")
STONE = ("#24242e", "#33323c", "#44424d", "#565461")
EMBER = ("#7b312a", "#bd532f", "#f48b42", "#ffbe64")


def plate(seed, palette, grain=False):
    rng = Random(seed)
    image = Image.new("RGB", (16, 16))
    for y in range(16):
        for x in range(16):
            base = (x // 4 + y // 5) % 2 if grain else 0
            image.putpixel((x, y), ImageColor.getrgb(palette[min(3, base + rng.choices(range(4), (1, 7, 5, 2))[0])]))
    return image


def bevel(draw, box, edge, light, shade):
    x0, y0, x1, y1 = box
    draw.rectangle(box, outline=edge)
    draw.line((x0 + 1, y0 + 1, x1 - 1, y0 + 1), fill=light)
    draw.line((x0 + 1, y0 + 1, x0 + 1, y1 - 1), fill=light)
    draw.line((x0 + 1, y1 - 1, x1 - 1, y1 - 1), fill=shade)
    draw.line((x1 - 1, y0 + 2, x1 - 1, y1 - 1), fill=shade)


def desk_top():
    image = plate(4901, WOOD, grain=True)
    draw = ImageDraw.Draw(image)
    draw.rectangle((2, 2, 13, 13), fill=JADE[0])
    bevel(draw, (1, 1, 14, 14), BRASS[0], BRASS[2], BRASS[1])
    for x in (3, 7, 11):
        for y in (3, 7, 11):
            draw.rectangle((x, y, x + 2, y + 2), fill=JADE[1])
            draw.line((x, y, x + 2, y), fill=JADE[2])
    draw.polygon(((8, 6), (10, 8), (8, 10), (6, 8)), fill=JADE[3])
    draw.point((8, 8), fill=BRASS[3])
    return image


def desk_side():
    image = plate(4902, WOOD, grain=True)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 15, 2), fill=BRASS[1])
    draw.line((0, 0, 15, 0), fill=BRASS[3])
    draw.line((0, 3, 15, 3), fill=WOOD[0])
    draw.rectangle((3, 5, 12, 12), fill=JADE[0])
    bevel(draw, (2, 4, 13, 13), BRASS[0], WOOD[3], WOOD[0])
    draw.line((5, 8, 10, 8), fill=JADE[1])
    draw.polygon(((7, 6), (10, 8), (7, 11), (5, 8)), fill=BRASS[2])
    draw.point((7, 8), fill=BRASS[3])
    for x in (1, 14):
        draw.line((x, 5, x, 13), fill=WOOD[0])
        draw.point((x, 1), fill=BRASS[3])
    draw.line((0, 14, 15, 14), fill=BRASS[1])
    draw.line((0, 15, 15, 15), fill=WOOD[0])
    return image


def bonfire_stone():
    image = plate(4904, STONE)
    draw = ImageDraw.Draw(image)
    for y in (3, 8, 13):
        draw.line((0, y, 15, y), fill=STONE[0])
        for x in (2, 9) if y == 8 else (5, 12):
            draw.line((x, y - 3, x, y - 1), fill=STONE[0])
    for x in (3, 8, 12):
        draw.point((x, 10), fill=EMBER[0])
        draw.point((x + 1, 11), fill=EMBER[1])
    return image


def bonfire_coal():
    image = plate(4905, STONE)
    draw = ImageDraw.Draw(image)
    for x, y, width in ((3, 4, 3), (9, 2, 2), (7, 7, 3), (12, 9, 2), (4, 12, 3), (10, 13, 2)):
        draw.line((x, y, x + width, y), fill=EMBER[0], width=2)
        draw.line((x, y, x + width - 1, y), fill=EMBER[1])
        draw.point((x + 1, y), fill=EMBER[2])
    return image


def bonfire_flame():
    image = Image.new("RGBA", (16, 64))
    for frame, shift in enumerate((0, 1, 0, -1)):
        tile = Image.new("RGBA", (16, 16))
        draw = ImageDraw.Draw(tile)
        draw.polygon(((3, 15), (3, 12), (5, 8), (6, 5), (7, 9), (9 + shift, 1),
                      (11, 5), (10, 9), (13, 7 + shift), (13, 11), (12, 15)), fill=EMBER[1])
        draw.polygon(((4, 15), (5, 11), (7, 13), (9 + shift, 5), (10, 8),
                      (9, 13), (12, 10), (11, 15)), fill=EMBER[2])
        draw.polygon(((6, 15), (7, 12), (8, 13), (9 + shift, 9), (10, 15)), fill=EMBER[3])
        draw.point((5 + shift, 3 + frame % 2), fill=EMBER[3])
        image.paste(tile, (0, frame * 16))
    return image


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("folder", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    outputs = {
        "guild_desk_top.png": desk_top(),
        "guild_desk_side.png": desk_side(),
        "guild_desk_bottom.png": plate(4903, WOOD, grain=True),
        "bonfire_stone.png": bonfire_stone(),
        "bonfire_coal.png": bonfire_coal(),
        "bonfire_flame.png": bonfire_flame(),
    }
    files = {"bonfire_flame.png.mcmeta": b'{\n  "animation": { "frametime": 3 }\n}\n'}
    for name, image in outputs.items():
        buf = BytesIO()
        image.save(buf, format="PNG")
        files[name] = buf.getvalue()

    for name, data in files.items():
        path = args.folder / name
        if args.check:
            if not path.exists() or path.read_bytes() != data:
                raise SystemExit(f"Textura ausente o desactualizada: {path}")
        else:
            path.write_bytes(data)
    print(f"{len(files)} recursos de puestos comprobados" if args.check else f"{len(files)} recursos de puestos generados")


if __name__ == "__main__":
    main()
