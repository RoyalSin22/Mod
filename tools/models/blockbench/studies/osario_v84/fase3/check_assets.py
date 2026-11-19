"""Comprueba los entregables de autoría; no genera ni retoca imágenes."""
from __future__ import annotations

import base64
from collections import Counter, defaultdict
import hashlib
from io import BytesIO
import json
from pathlib import Path
import sys

from PIL import Image

STUDY = Path(__file__).resolve().parent
ADJUSTED = "--adjusted" in sys.argv
HERE = STUDY / "ajuste" if ADJUSTED else STUDY
MODELS = STUDY.parents[2]
SNAPSHOT = STUDY / "osario_texturizado_fase3.bbmodel"
ACTIVE = HERE / "osario_texturizado_ajustado.bbmodel" if ADJUSTED else SNAPSHOT


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def embedded(data):
    return Image.open(BytesIO(base64.b64decode(data.split(",", 1)[1]))).convert("RGBA")


def rgba_pixels(im):
    return im.get_flattened_data() if hasattr(im, "get_flattened_data") else im.getdata()


def geometry(model):
    # Únicamente la UV puede diferir de la entrega aprobada de geometría.
    uv_fields = {"faces", "uv_offset", "box_uv", "autouv"}
    return [{k: v for k, v in e.items() if k not in uv_fields} for e in model["elements"]]


def rig(model):
    editor_fields = {"selected", "primary_selected", "isOpen", "_static"}
    return [{k: v for k, v in g.items() if k not in editor_fields} for g in model["groups"]]


def luminance(color):
    c = [int(color[i:i + 2], 16) / 255 for i in (1, 3, 5)]
    c = [v / 12.92 if v <= .04045 else ((v + .055) / 1.055) ** 2.4 for v in c]
    return sum(v * w for v, w in zip(c, [.2126, .7152, .0722]))


current = read(ACTIVE)
previous_path = STUDY.parent / "fase2/osario_geometria_fase2.bbmodel"
previous = read(previous_path)
phase1 = STUDY.parent / "fase1/osario_quebrado_aprobado.bbmodel"
assert digest(previous_path) == "be471e254f11ec116d254327b3e4c337da4310f523feafbc70b0f446fba2f52d"
assert digest(phase1) == "06105c0eb8cb265f4c5dba9e7e18af692902f2cca15ad5ffd8314103df9fd9c1"
if ADJUSTED:
    original = read(SNAPSHOT)
    assert current["outliner"] == original["outliner"]
    assert {a["name"] for a, b in zip(current["elements"], original["elements"]) if a != b} == {
        "manubrium", "sternum", "ecto_core", "ecto_vertical", "ecto_horizontal", "ecto_shard_high", "ecto_shard_low"
    }
    assert {a["name"] for a, b in zip(rig(current), rig(original)) if a != b} == {"chest_core"}
    adjusted = {e["name"]: e for e in current["elements"]}
    for name, width in {"manubrium": 1, "sternum": .5}.items():
        assert abs(adjusted[name]["to"][0] - adjusted[name]["from"][0] - width) < 1e-5
    joins = read(HERE / "holgura_y_uniones.json")
    assert all(not case["overlaps"] for case in joins["pulse"])
    contacts = dict(dict(joins["joints"])["sternum"])
    assert all(contacts[name] > .01 for name in ["manubrium", "xiphoid", "rib_left_3_front", "rib_left_4_front"])
else:
    assert geometry(current) == geometry(previous), "Cambió un volumen aprobado"
    assert rig(current) == rig(previous), "Cambió el rig aprobado"
assert current["outliner"] == previous["outliner"], "Cambió la jerarquía"
assert len(current["elements"]) == 169 and len(current["groups"]) == 30
assert not current.get("animations")

base = Image.open(HERE / "osario_quebrado.png").convert("RGBA")
mask = Image.open(HERE / "osario_quebrado_glowmask.png").convert("RGBA")
assert base.size == mask.size == (256, 256)
tex, = current["textures"]
assert (tex["width"], tex["height"]) == (256, 256)
assert (tex["uv_width"], tex["uv_height"]) == (128, 128)
assert current["resolution"] == {"width": 128, "height": 128}
assert base.tobytes() == embedded(tex["source"]).tobytes()
assert len(tex["layers"]) == 5 and tex["layers_enabled"]
composite = Image.new("RGBA", base.size)
layer_counts = []
for layer in tex["layers"]:
    assert layer["visible"] and layer["opacity"] == 100
    assert layer["offset"] == [0, 0] and layer["scale"] == [1, 1]
    im = embedded(layer["data_url"])
    assert im.size == base.size
    layer_counts.append(sum(p[3] != 0 for p in rgba_pixels(im)))
    composite = Image.alpha_composite(composite, im)
assert composite.tobytes() == base.tobytes()

palette = read(HERE / "paleta.json")
allowed = {tuple(bytes.fromhex(h[1:])) for colors in palette.values() for h in colors}
pixels = list(rgba_pixels(base))
used = Counter(p[:3] for p in pixels if p[3])
assert len(allowed) == len(used) == 25 and set(used) == allowed
assert {p[3] for p in pixels} == {0, 255}
assert all(p == (0, 0, 0, 0) for p in pixels if not p[3])
assert used[tuple(bytes.fromhex("C9FFF0"))] == 6

faces = read(HERE / "caras_pintadas.json")
layout = read(HERE / "uv_layout.json")
owner = [-1] * 65536
patterns = defaultdict(list)
for i, f in enumerate(faces):
    x, y, w, h = f["rect"]
    assert all(isinstance(v, int) for v in (x, y, w, h))
    assert 0 <= x < x + w <= 256 and 0 <= y < y + h <= 256
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            p = yy * 256 + xx
            assert owner[p] == -1, "Solape UV"
            owner[p] = i
            assert pixels[p][3] == 255, "Cara con un píxel transparente"
    if w >= 4 and h >= 4 and w * h >= 24:
        patterns[(w, h, base.crop((x, y, x + w, y + h)).tobytes())].append((f["element"], f["face"]))
assert len(faces) == 1014 and len(layout["islands"]) == 169
assert all((o >= 0) == (p[3] == 255) for o, p in zip(owner, pixels))
duplicates = [items for items in patterns.values() if len(items) > 1]
assert not duplicates, "Patrones idénticos en caras de tamaño significativo"
assert all(not e.get("mirror_uv", False) for e in current["elements"])
assert all(f.get("texture") == 0 for e in current["elements"] for f in e["faces"].values())
for i, a in enumerate(layout["islands"]):
    x, y, w, h = a["rectangle"]
    for b in layout["islands"][i + 1:]:
        bx, by, bw, bh = b["rectangle"]
        assert x + w < bx or bx + bw < x or y + h < by or by + bh < y, "Margen menor de 1 píxel"

emissive = Counter()
for i, p in enumerate(rgba_pixels(mask)):
    assert p[3] in (0, 160, 255)
    if p[3]:
        assert p[:3] == (255, 255, 255)
        assert owner[i] >= 0 and faces[owner[i]]["parent"] == "chest_core"
        emissive[p[3]] += 1
    else:
        assert p == (0, 0, 0, 0), "RGB residual en máscara apagada"
assert sum(emissive.values()) == 84
green = sum(used[tuple(bytes.fromhex(h[1:]))] for h in palette["ecto"])
moss = sum(used[tuple(bytes.fromhex(h[1:]))] for h in palette["moss"])
assert green / 65536 <= .04
assert green / sum(used.values()) <= .04
for i, p in enumerate(pixels):
    if p[:3] in {tuple(bytes.fromhex(h[1:])) for h in palette["moss"]}:
        assert faces[owner[i]]["parent"] not in ("head", "jaw")

enlarged = Image.open(HERE / "textura_x8.png").convert("RGBA")
assert enlarged.size == (2048, 2048)
raw = base.tobytes()
expected = b"".join((b"".join(raw[(y * 256 + x) * 4:(y * 256 + x + 1) * 4] * 8 for x in range(256))) * 8 for y in range(256))
assert enlarged.tobytes() == expected, "La ampliación no es NEAREST exacta"
surface = read(HERE / "musgo_superficie.json")
assert surface["moss_exposed_sample_pct"] < 10
inspection = read(HERE / "uv_inspection.json")
assert all(inspection["texel_density"][v] == 2 for v in ("min", "median", "max"))
assert not [f for f in inspection["findings"] if f["level"] in ("warning", "error")]
contrast = (luminance("#B7B5A6") + .05) / (luminance("#3B372F") + .05)
assert contrast >= 4

result = {
    "geometry_and_rig_unchanged": not ADJUSTED,
    "cubes": 169, "bones": 30, "animations": 0,
    "box_uv_cubes": sum(e["box_uv"] for e in current["elements"]),
    "explicit_uv_cubes": sum(not e["box_uv"] for e in current["elements"]),
    "faces": len(faces), "texel_density": 2, "atlas": [256, 256], "logical": [128, 128],
    "painted_pixels": sum(used.values()), "actual_atlas_face_occupancy_pct": sum(used.values()) / 65536 * 100,
    "palette_colors": len(used), "highlight_pixels_C9FFF0": 6,
    "ectoplasm_pixels": green, "ectoplasm_atlas_pct": green / 65536 * 100,
    "ectoplasm_painted_pct": green / sum(used.values()) * 100,
    "moss_pixels": moss, "moss_exposed_sample_pct": surface["moss_exposed_sample_pct"],
    "emissive_pixels": sum(emissive.values()), "mask_alpha_counts": dict(emissive),
    "emissive_atlas_pct": sum(emissive.values()) / 65536 * 100,
    "bone_cavity_palette_contrast": contrast,
    "editable_layers": len(tex["layers"]), "layer_painted_pixels": layer_counts,
    "composite_matches_base_png": True, "embedded_texture_matches_base_png": True,
    "nearest_x8_exact": True, "uv_overlaps": 0, "minimum_island_gap_physical_px": 1,
    "transparent_face_pixels": 0, "large_identical_face_patterns": duplicates,
    "duplicate_scan_threshold": "width >= 4, height >= 4, area >= 24 pixels; same orientation and dimensions",
    "sha256": {p.relative_to(MODELS).as_posix(): digest(p) for p in [ACTIVE, previous_path, phase1, HERE / "osario_quebrado.png", HERE / "osario_quebrado_glowmask.png"]},
    "artistic_acceptance": "Authorized thorax adjustment applied" if ADJUSTED else "Original delivery before the authorized adjustment",
    "game_validation": "Not run; no active resources changed in this phase"
}
(HERE / "asset_checks.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(json.dumps(result, ensure_ascii=False, indent=2))
