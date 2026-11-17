#!/usr/bin/env python3
"""
Generador de las animaciones del cuerpo del jugador (V78, Player Animation Library).

Uso:  python3 tools/models/gen_player_animations.py <carpeta assets/aptitudes> [--check]
Escribe <carpeta>/player_animations/berserker.json. Con --check no escribe: falla si el archivo no coincide.

Convención de PAL (leída de su código): los ángulos están en GRADOS y se aplican tal cual a las partes del modelo vanilla
(sin invertir signos). En el modelo vanilla, pitch NEGATIVO levanta un brazo o una pierna hacia delante, pitch positivo
inclina el torso hacia delante y la cabeza hacia abajo; el roll de un brazo derecho positivo lo separa del cuerpo (el
izquierdo, negativo). La posición Y es hacia arriba (positivo sube) y va en píxeles de modelo (1/16 de bloque).
Huesos: head, torso, body (raíz de todo el cuerpo), right_arm, left_arm, right_leg, left_leg.
Las animaciones terminan en reposo (0) salvo las que se mantienen en el último fotograma hasta que otra las sustituye.
"""
import json
import os
import sys

NEUTRAL = [0, 0, 0]


def kf(vec, easing="easeoutquad"):
    return {"vector": [round(float(v), 3) for v in vec], "easing": easing}


def track(frames):
    """frames: {tiempo_s: vector | (vector, curva)} -> bloque de keyframes de Bedrock."""
    out = {}
    for t in sorted(frames):
        v = frames[t]
        vec, ease = (v if isinstance(v, tuple) else (v, "easeoutquad"))
        out[f"{t:.3f}".rstrip("0").rstrip(".") if t else "0.0"] = kf(vec, ease)
    return out


def bone(rot=None, pos=None):
    b = {}
    if rot:
        b["rotation"] = track(rot)
    if pos:
        b["position"] = track(pos)
    return b


def animation(length, loop, bones):
    return {"loop": loop, "animation_length": length, "bones": bones}


def mirror(vec):
    """Pose del brazo/pierna izquierdos a partir de la del derecho: el roll y el giro cambian de signo."""
    return [vec[0], -vec[1], -vec[2]]


def mirrored(frames):
    return {t: ((mirror(v[0]), v[1]) if isinstance(v, tuple) else mirror(v)) for t, v in frames.items()}


def build():
    anims = {}

    # ---- Furia: se agacha, explota hacia arriba con el pecho fuera y ruge con los brazos abiertos.
    right_arm = {0: NEUTRAL, 0.18: ([25, 0, 6], "easeinoutsine"), 0.36: [-35, 0, 55], 0.95: ([-30, 0, 50], "linear"), 1.5: ([0, 0, 0], "easeinoutsine")}
    anims["berserker_rage"] = animation(1.5, False, {
        "body": bone(pos={0: NEUTRAL, 0.18: [0, -2.5, 0], 0.36: [0, 0.8, 0], 0.9: ([0, 0.4, 0], "linear"), 1.5: ([0, 0, 0], "easeinoutsine")}),
        "torso": bone(rot={0: NEUTRAL, 0.18: ([28, 0, 0], "easeinoutsine"), 0.36: [-18, 0, 0], 0.95: ([-14, 0, 0], "linear"), 1.5: ([0, 0, 0], "easeinoutsine")}),
        "head": bone(rot={0: NEUTRAL, 0.18: ([18, 0, 0], "easeinoutsine"), 0.36: [-34, 0, 0], 0.5: ([-30, 5, 0], "linear"), 0.62: ([-34, -5, 0], "linear"),
                          0.74: ([-30, 5, 0], "linear"), 0.86: ([-34, -4, 0], "linear"), 1.5: ([0, 0, 0], "easeinoutsine")}),
        "right_arm": bone(rot=right_arm),
        "left_arm": bone(rot=mirrored(right_arm)),
        "right_leg": bone(rot={0: NEUTRAL, 0.18: ([-15, 0, 8], "easeinoutsine"), 0.36: [-6, 0, 8], 0.95: ([-6, 0, 8], "linear"), 1.5: ([0, 0, 0], "easeinoutsine")}),
        "left_leg": bone(rot={0: NEUTRAL, 0.18: ([10, 0, -8], "easeinoutsine"), 0.36: [6, 0, -8], 0.95: ([6, 0, -8], "linear"), 1.5: ([0, 0, 0], "easeinoutsine")}),
    })

    # ---- Grito de guerra: brazos arriba, cabeza atrás y golpe seco hacia delante.
    shout_arm = {0: NEUTRAL, 0.2: [-150, 0, 20], 0.5: ([-155, 0, 24], "linear"), 0.58: ([-40, 0, 5], "easeinquad"), 0.7: ([-40, 0, 5], "linear"), 1.1: ([0, 0, 0], "easeinoutsine")}
    anims["berserker_war_cry"] = animation(1.1, False, {
        "body": bone(pos={0: NEUTRAL, 0.2: [0, 0.5, 0], 0.58: [0, -1.5, 0], 1.1: ([0, 0, 0], "easeinoutsine")}),
        "torso": bone(rot={0: NEUTRAL, 0.2: [-12, 0, 0], 0.5: ([-14, 0, 0], "linear"), 0.58: ([22, 0, 0], "easeinquad"), 0.7: ([22, 0, 0], "linear"), 1.1: ([0, 0, 0], "easeinoutsine")}),
        "head": bone(rot={0: NEUTRAL, 0.2: [-28, 0, 0], 0.5: ([-30, 0, 0], "linear"), 0.58: ([10, 0, 0], "easeinquad"), 1.1: ([0, 0, 0], "easeinoutsine")}),
        "right_arm": bone(rot=shout_arm),
        "left_arm": bone(rot=mirrored(shout_arm)),
        "right_leg": bone(rot={0: NEUTRAL, 0.2: [-6, 0, 6], 0.7: ([-6, 0, 6], "linear"), 1.1: ([0, 0, 0], "easeinoutsine")}),
        "left_leg": bone(rot={0: NEUTRAL, 0.2: [6, 0, -6], 0.7: ([6, 0, -6], "linear"), 1.1: ([0, 0, 0], "easeinoutsine")}),
    })

    # ---- Salto: se encoge, se estira en el aire con los brazos arriba y se queda así hasta el aterrizaje.
    leap_arm = {0: NEUTRAL, 0.12: ([35, 0, 8], "easeinoutsine"), 0.28: [-170, 0, 15]}
    anims["berserker_leap"] = animation(0.9, "hold_on_last_frame", {
        "body": bone(pos={0: NEUTRAL, 0.12: [0, -3, 0], 0.28: [0, 0.5, 0]}),
        "torso": bone(rot={0: NEUTRAL, 0.12: ([30, 0, 0], "easeinoutsine"), 0.28: [15, 0, 0]}),
        "head": bone(rot={0: NEUTRAL, 0.12: ([-10, 0, 0], "easeinoutsine"), 0.28: [-15, 0, 0]}),
        "right_arm": bone(rot=leap_arm),
        "left_arm": bone(rot=mirrored(leap_arm)),
        "right_leg": bone(rot={0: NEUTRAL, 0.12: ([-30, 0, 4], "easeinoutsine"), 0.28: [-25, 0, 4]}),
        "left_leg": bone(rot={0: NEUTRAL, 0.12: ([20, 0, -4], "easeinoutsine"), 0.28: [20, 0, -4]}),
    })

    # ---- Aterrizaje: parte de la pose del salto, machaca con los brazos y se incorpora.
    slam_arm = {0: [-170, 0, 15], 0.12: ([-20, 0, 0], "easeinquad"), 0.45: ([-20, 0, 0], "linear"), 1.0: ([0, 0, 0], "easeinoutsine")}
    anims["berserker_slam"] = animation(1.0, False, {
        "body": bone(pos={0: [0, 0.5, 0], 0.12: ([0, -4, 0], "easeinquad"), 0.45: ([0, -4, 0], "linear"), 1.0: ([0, 0, 0], "easeinoutsine")}),
        "torso": bone(rot={0: [15, 0, 0], 0.12: ([38, 0, 0], "easeinquad"), 0.45: ([38, 0, 0], "linear"), 1.0: ([0, 0, 0], "easeinoutsine")}),
        "head": bone(rot={0: [-15, 0, 0], 0.12: ([20, 0, 0], "easeinquad"), 0.45: ([20, 0, 0], "linear"), 1.0: ([0, 0, 0], "easeinoutsine")}),
        "right_arm": bone(rot=slam_arm),
        "left_arm": bone(rot=mirrored(slam_arm)),
        "right_leg": bone(rot={0: [-25, 0, 4], 0.12: ([-18, 0, 10], "easeinquad"), 0.45: ([-18, 0, 10], "linear"), 1.0: ([0, 0, 0], "easeinoutsine")}),
        "left_leg": bone(rot={0: [20, 0, -4], 0.12: ([14, 0, -10], "easeinquad"), 0.45: ([14, 0, -10], "linear"), 1.0: ([0, 0, 0], "easeinoutsine")}),
    })

    # ---- Torbellino: un giro completo en medio segundo con los brazos extendidos (se dispara tres veces seguidas).
    whirl_arm = {0: [-80, 0, 70], 0.5: [-80, 0, 70]}
    anims["berserker_whirl"] = animation(0.5, False, {
        "body": bone(rot={0: ([0, 0, 0], "linear"), 0.5: ([0, 360, 0], "linear")}, pos={0: [0, -1, 0], 0.5: [0, -1, 0]}),
        "torso": bone(rot={0: [10, 0, 0], 0.5: [10, 0, 0]}),
        "right_arm": bone(rot=whirl_arm),
        "left_arm": bone(rot=mirrored(whirl_arm)),
        "right_leg": bone(rot={0: [-10, 0, 10], 0.5: [-10, 0, 10]}),
        "left_leg": bone(rot={0: [10, 0, -10], 0.5: [10, 0, -10]}),
    })
    return {"format_version": "1.8.0", "animations": anims}


def render():
    return json.dumps(build(), indent=2, ensure_ascii=False) + "\n"


def main():
    args = [a for a in sys.argv[1:] if a != "--check"]
    check = "--check" in sys.argv[1:]
    if len(args) != 1:
        print(__doc__)
        sys.exit(1)
    path = os.path.join(args[0], "player_animations", "berserker.json")
    text = render()
    if check:
        current = open(path, encoding="utf-8").read() if os.path.exists(path) else None
        if current != text:
            sys.exit("player_animations/berserker.json no coincide con el generador: ejecútalo sin --check")
        print("player_animations/berserker.json correcto")
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)
    print(path)


if __name__ == "__main__":
    main()
