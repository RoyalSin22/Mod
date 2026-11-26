"""Verifica el editable guardado, los tiempos, los contactos y las pruebas de Fase 4."""
from pathlib import Path
import hashlib
import json
import math

from PIL import Image

FOLDER = Path(__file__).resolve().parent
WORKING = FOLDER.parents[2] / "osario_quebrado.bbmodel"
SAVED = FOLDER / "osario_animado_fase4.bbmodel"
THORAX = FOLDER.parent / "fase3/ajuste/osario_texturizado_ajustado.bbmodel"


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


def check():
    assert WORKING.read_bytes() == SAVED.read_bytes()
    source, animated = read(THORAX), read(SAVED)
    for field in ("elements", "groups", "outliner", "textures", "resolution"):
        assert source[field] == animated[field], field
    groups = {g["uuid"]: g["name"] for g in animated["groups"]}
    assert len(groups) == 30 and len(animated["elements"]) == 169
    measured = read(FOLDER / "validacion_clips.json")["clips"]
    table = read(FOLDER / "claves_finales.json")
    expected = {"idle": (3.2, "loop"), "walk": (1.2, "loop"), "chase": (.85, "loop"),
                "alert": (.65, "once"), "claw_swipe": (.9, "once"), "heavy_swipe": (1.4, "once"),
                "ground_slam": (1.8, "once"), "lunge": (1.35, "once"), "hurt": (.4, "once"),
                "death": (.95, "hold")}
    found = set()
    for animation in animated["animations"]:
        name = animation["name"].removeprefix("animation.osario_quebrado.")
        assert name not in found and (animation["length"], animation["loop"]) == expected[name]
        found.add(name)
        assert set(animation["animators"]) <= set(groups)
        total = 0
        for uuid, entry in animation["animators"].items():
            if not entry.get("keyframes"):
                continue
            channels = table[name]["bones"][groups[uuid]]
            for k in entry["keyframes"]:
                values = [float(k["data_points"][0][axis]) for axis in "xyz"]
                assert all(math.isfinite(n) for n in values)
                assert k["interpolation"] == "catmullrom" and 0 <= k["time"] <= animation["length"]
                assert [k["time"], *values] in channels[k["channel"]]
                if groups[uuid] == "root" and k["channel"] == "position":
                    assert values[0] == values[2] == 0
                total += 1
            if animation["loop"] == "loop":
                for channel, keys in channels.items():
                    assert keys[0][0] == 0 and keys[-1][0] == animation["length"]
                    deltas = [a-b for a, b in zip(keys[0][1:], keys[-1][1:])]
                    if channel == "rotation":
                        deltas = [d - 360 * round(d / 360) for d in deltas]
                    assert all(abs(d) < 1e-4 for d in deltas), (name, groups[uuid], channel)
        assert total == measured[name]["keyframes"]
        assert measured[name]["floor"]["y"] >= -.012, name
        with Image.open(FOLDER / f"{name}.png") as contact_sheet:
            contact_sheet.verify()
    assert found == set(expected)
    assert measured["chase"]["max_grounded_mass_y"] <= .8
    for n, times in {"walk": ["0", "0.6"], "chase": ["0", "0.425"]}.items():
        for time in times:
            assert max(map(abs, measured[n]["markers"][time]["foot"].values())) <= .07
    for n,t in {"claw_swipe":"0.5","heavy_swipe":"0.95","lunge":"0.875"}.items():
        assert measured[n]["markers"][t]["positions"]["bone_hand"][2] < -9
    slam = measured["ground_slam"]["markers"]["1.15"]
    assert abs(slam["mass"][0][1]) < .1 and slam["mass"][0][2] < -24
    last = measured["death"]["markers"]["0.95"]
    size = [b-a for a,b in zip(*last["bounds"])]
    assert size[0] <= 22.4 and size[2] <= 22.4 and abs(last["mass"][0][1]) < .1
    print(f"{len(found)} clips correctos, {sum(x['samples'] for x in measured.values())} poses muestreadas")
    print(f"Claves: {sum(x['keyframes'] for x in measured.values())}")
    print(f"Menor Y medida: {min(x['floor']['y'] for x in measured.values()):.5f} u")
    print(f"Punta en el impacto: y={slam['mass'][0][1]:.4f} u, z={slam['mass'][0][2]:.3f} u")
    print(f"Huella de muerte: {size[0]/16:.3f} × {size[2]/16:.3f} bloques")
    print(f"SHA-256: {hashlib.sha256(SAVED.read_bytes()).hexdigest()}")


if __name__ == "__main__":
    check()
