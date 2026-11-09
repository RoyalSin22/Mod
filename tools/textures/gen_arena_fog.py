#!/usr/bin/env python3
"""Genera la textura animada del muro de niebla de las arenas."""
import argparse
from io import BytesIO
from math import sin, tau
from pathlib import Path

from PIL import Image


def fog():
    size, frames = 16, 8
    image = Image.new("RGBA", (size, size * frames))
    for frame in range(frames):
        phase = frame / frames
        for y in range(size):
            for x in range(size):
                a = sin(tau * (y / size + phase + 0.18 * sin(tau * x / size)))
                b = sin(tau * ((x + 2 * y) / size - phase))
                mist = 0.5 + 0.35 * a + 0.15 * b
                pixel = (int(86 + 90 * mist), int(108 + 100 * mist),
                         int(155 + 90 * mist), int(110 + 95 * mist))
                image.putpixel((x, y + frame * size), pixel)
    buf = BytesIO()
    image.save(buf, format="PNG")
    return buf.getvalue()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("folder", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    path = args.folder / "arena_fog.png"
    data = fog()
    if args.check:
        if not path.is_file() or path.read_bytes() != data:
            raise SystemExit(f"Textura ausente o desactualizada: {path}")
    else:
        path.write_bytes(data)


if __name__ == "__main__":
    main()
