"""Vistas Pillow del mismo volumen que exportFortressSchem deja en build/.

Uso: python tools/fortress/preview.py [directorio-build]
"""

from collections import deque
import gzip
from io import BytesIO
from pathlib import Path
import struct
import sys

from PIL import Image, ImageDraw

SIZE = 128
BOTTOM = -3
TOP = 62
HEIGHT = TOP - BOTTOM + 1
TILE = 8
ROUTE = [(0, 62), (-34, 42), (-39, 17), (-39, -13), (-43, -40),
         (43, 41), (28, -36)]
LABELS = [("Entrada", 0, 59), ("Patio", 0, 11), ("Dormitorios", -34, 42),
          ("Estudio", -39, 17), ("Capilla", -39, -13), ("Alquimia", 34, 15),
          ("Vigía O", -43, -40), ("Vigía E", 43, 41), ("Mariscal", 28, -36),
          ("Tesoro", 28, -58)]


def load(build: Path):
    palette = {}
    for line in (build / "fortress.palette").read_text(encoding="utf-8").splitlines():
        if line and not line.startswith("#"):
            index, state = line.split("\t", 1)
            palette[int(index)] = state
    voxels = (build / "fortress.voxels").read_bytes()
    if len(voxels) != SIZE * SIZE * HEIGHT:
        raise ValueError("fortress.voxels no coincide con el volumen de FortressLayout")
    validate_schem(build / "fortress.schem", palette, voxels)
    return palette, voxels


def validate_schem(path, palette, voxels):
    stream = BytesIO(gzip.decompress(path.read_bytes()))

    def number(format_):
        return struct.unpack(">" + format_, stream.read(struct.calcsize(format_)))[0]

    def string():
        return stream.read(number("H")).decode("utf-8")

    def payload(kind):
        if kind == 2:
            return number("h")
        if kind == 3:
            return number("i")
        if kind == 7:
            return stream.read(number("i"))
        if kind == 9:
            inner, count = number("B"), number("i")
            return [payload(inner) for _ in range(count)]
        if kind == 10:
            values = {}
            while (tag := number("B")) != 0:
                name = string()
                values[name] = payload(tag)
            return values
        if kind == 11:
            return [number("i") for _ in range(number("i"))]
        raise ValueError(f"Etiqueta NBT no esperada: {kind}")

    if number("B") != 10 or string() != "":
        raise ValueError("Raíz NBT inválida")
    root = payload(10)["Schematic"]
    if (root["Version"], root["DataVersion"], root["Width"], root["Height"], root["Length"]) != (3, 3955, 128, 66, 128):
        raise ValueError("Versión o dimensiones .schem inesperadas")
    if root["Offset"] != [-64, -3, -64]:
        raise ValueError("Origen relativo .schem incorrecto")
    if {state: index for index, state in palette.items()} != root["Blocks"]["Palette"]:
        raise ValueError("La paleta .schem no coincide con la vista previa")
    encoded = root["Blocks"]["Data"]
    decoded = bytearray()
    value = shift = 0
    for byte in encoded:
        value |= (byte & 0x7f) << shift
        if byte & 0x80:
            shift += 7
            if shift > 28:
                raise ValueError("VarInt .schem demasiado largo")
        else:
            decoded.append(value)
            value = shift = 0
    if shift or decoded != voxels:
        raise ValueError("Los bloques .schem no coinciden con la vista previa")


def cell(voxels, x, y, z):
    if not (-64 <= x <= 63 and -64 <= z <= 63 and BOTTOM <= y <= TOP):
        return 0
    return voxels[((y - BOTTOM) * SIZE + (z + 64)) * SIZE + x + 64]


def color(state):
    if state.endswith(":air"):
        return (29, 35, 42)
    if "calcite" in state:
        return (218, 214, 195)
    if "blackstone" in state:
        return (56, 53, 64)
    if "deepslate" in state:
        return (76, 85, 99)
    if "mossy" in state:
        return (94, 116, 86)
    if "stone_brick" in state:
        return (143, 145, 141)
    if "andesite" in state:
        return (173, 172, 160)
    if "cobble" in state:
        return (106, 108, 101)
    if "planks" in state:
        return (100, 71, 52)
    if "bed" in state:
        return (151, 57, 57)
    if "glass" in state:
        return (77, 124, 151)
    if "lantern" in state:
        return (242, 182, 89)
    if "chest" in state:
        return (205, 137, 66)
    if "wool" in state:
        return (148, 79, 160)
    if "iron_bars" in state or "chain" in state:
        return (87, 97, 109)
    if "moss" in state or "carpet" in state:
        return (92, 126, 85)
    if "controller" in state:
        return (90, 120, 137)
    if "bookshelf" in state:
        return (151, 112, 75)
    return (132, 119, 103)


def shortest_path(palette, voxels, start, goal):
    def walkable(x, z):
        if not (-64 <= x <= 63 and -64 <= z <= 63):
            return False
        floor = palette[cell(voxels, x, 0, z)]
        feet = palette[cell(voxels, x, 1, z)]
        head = palette[cell(voxels, x, 2, z)]
        free = lambda state: state == "minecraft:air" or state.endswith("_carpet")
        return floor != "minecraft:air" and free(feet) and free(head)

    queue = deque([start])
    previous = {start: None}
    while queue:
        point = queue.popleft()
        if point == goal:
            route = []
            while point is not None:
                route.append(point)
                point = previous[point]
            return route[::-1]
        x, z = point
        for neighbor in ((x + 1, z), (x - 1, z), (x, z + 1), (x, z - 1)):
            if neighbor not in previous and walkable(*neighbor):
                previous[neighbor] = point
                queue.append(neighbor)
    return []


def floor_plan(build, palette, voxels, floor):
    image = Image.new("RGB", (SIZE * TILE + 300, SIZE * TILE + 100), (24, 29, 36))
    draw = ImageDraw.Draw(image)
    ox, oz = 50, 60
    for z in range(-64, 64):
        for x in range(-64, 64):
            block = palette[cell(voxels, x, floor, z)]
            if block == "minecraft:air":
                block = palette[cell(voxels, x, floor - 1, z)]
                if block == "minecraft:air":
                    continue
                c = tuple(max(25, channel // 2) for channel in color(block))
            else:
                c = color(block)
            px, pz = ox + (x + 64) * TILE, oz + (z + 64) * TILE
            draw.rectangle((px, pz, px + TILE - 1, pz + TILE - 1), fill=c)
    if floor == 1:
        for start, goal in zip(ROUTE, ROUTE[1:]):
            path = shortest_path(palette, voxels, start, goal)
            if path:
                draw.line([(ox + (x + 64) * TILE + 4, oz + (z + 64) * TILE + 4)
                           for x, z in path], fill=(249, 205, 101), width=3)
        for name, x, z in LABELS:
            px, pz = ox + (x + 64) * TILE, oz + (z + 64) * TILE
            draw.ellipse((px - 4, pz - 4, px + 4, pz + 4), fill=(255, 228, 145))
            draw.text((px + 8, pz - 10), name, fill=(255, 241, 196), stroke_width=2, stroke_fill=(24, 29, 36))
    else:
        for name, x, z in LABELS[-4:]:
            draw.text((ox + (x + 64) * TILE + 8, oz + (z + 64) * TILE - 10), name,
                      fill=(255, 241, 196), stroke_width=2, stroke_fill=(24, 29, 36))
    draw.text((50, 20), f"Tres Torres V91 | planta y={floor} | norte arriba", fill=(238, 227, 202))
    draw.text((1085, 70), "Leyenda", fill=(238, 227, 202))
    for i, (name, c) in enumerate([("Piedra", (143, 145, 141)), ("Pizarra", (76, 85, 99)),
                                   ("Madera", (100, 71, 52)), ("Cofre", (205, 137, 66)),
                                   ("Marca de cofre", (148, 79, 160))]):
        draw.rectangle((1085, 100 + i * 25, 1100, 115 + i * 25), fill=c)
        draw.text((1110, 99 + i * 25), name, fill=(222, 219, 207))
    if floor == 1:
        draw.text((1085, 255), "Dorado: ruta a pie", fill=(249, 205, 101))
        draw.text((1085, 278), "Mariscal: abierto", fill=(222, 219, 207))
    path = build / f"fortress_plan_y{floor}.png"
    image.save(path)
    return path


def isometric(build, palette, voxels):
    top = {}
    for z in range(-64, 64):
        for x in range(-64, 64):
            for y in range(TOP, BOTTOM - 1, -1):
                index = cell(voxels, x, y, z)
                if palette[index] != "minecraft:air":
                    top[x, z] = (y, color(palette[index]))
                    break
    scale_x, scale_z, scale_y = 7, 4, 6
    points = [(900 + (x - z) * scale_x, 330 + (x + z) * scale_z - y * scale_y)
              for (x, z), (y, _) in top.items()]
    min_x, min_y = min(p[0] for p in points), min(p[1] for p in points)
    max_x, max_y = max(p[0] for p in points), max(p[1] for p in points)
    image = Image.new("RGB", (max_x - min_x + 120, max_y - min_y + 360), (25, 30, 37))
    draw = ImageDraw.Draw(image)
    for x, z in sorted(top, key=lambda p: p[0] + p[1]):
        y, c = top[x, z]
        px = 60 + 900 + (x - z) * scale_x - min_x
        py = 80 + 330 + (x + z) * scale_z - y * scale_y - min_y
        south = top.get((x, z + 1), (BOTTOM,))[0]
        east = top.get((x + 1, z), (BOTTOM,))[0]
        if south < y:
            drop = min(35, y - south) * scale_y
            draw.polygon([(px, py + scale_z), (px - scale_x, py),
                          (px - scale_x, py + drop), (px, py + scale_z + drop)],
                         fill=tuple(max(10, v * 3 // 5) for v in c))
        if east < y:
            drop = min(35, y - east) * scale_y
            draw.polygon([(px, py + scale_z), (px + scale_x, py),
                          (px + scale_x, py + drop), (px, py + scale_z + drop)],
                         fill=tuple(max(10, v * 4 // 5) for v in c))
        draw.polygon([(px, py - scale_z), (px + scale_x, py),
                      (px, py + scale_z), (px - scale_x, py)], fill=c)
    draw.text((40, 25), "Tres Torres | vista isométrica del plano V91", fill=(238, 227, 202))
    path = build / "fortress_isometric.png"
    image.save(path)
    return path


def main():
    build = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build")
    palette, voxels = load(build)
    paths = [floor_plan(build, palette, voxels, y) for y in (1, 7, 13, 19, 25)]
    paths.append(isometric(build, palette, voxels))
    for path in paths:
        print(path)


if __name__ == "__main__":
    main()
