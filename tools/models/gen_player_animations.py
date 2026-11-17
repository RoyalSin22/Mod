#!/usr/bin/env python3
"""
Generador de las animaciones del cuerpo del jugador (V78, Player Animation Library).

Uso:  python3 tools/models/gen_player_animations.py <carpeta assets/aptitudes> [--check]
Escribe <carpeta>/player_animations/<rol>.json (berserker.json y mage.json). Con --check no escribe: falla si algún archivo no coincide.

Convención de PAL (leída de su código): los ángulos están en GRADOS y se aplican tal cual a las partes del modelo vanilla
(sin invertir signos). En el modelo vanilla, pitch NEGATIVO levanta un brazo o una pierna hacia delante, pitch positivo
inclina el torso hacia delante y la cabeza hacia abajo; el roll de un brazo derecho positivo lo separa del cuerpo (el
izquierdo, negativo). La posición Y es hacia arriba (positivo sube) y va en píxeles de modelo (1/16 de bloque).
Huesos: head, torso, body (raíz de todo el cuerpo), right_arm, left_arm, right_leg, left_leg.
Las animaciones terminan en reposo (0) salvo las que se mantienen en el último fotograma hasta que otra las sustituye.

PRIMERA PERSONA (V78.1): el jugador solo ve sus brazos, así que el movimiento que cuenta es el de los brazos: un brazo que cuelga
(pitch 0) queda fuera de pantalla; con pitch entre −35 y −170 entra en la vista. El torso y la cabeza se notan por cómo mueven los
hombros. Cada animación sigue la misma estructura: preparación (anticipación) → golpe con sobrepaso (`easeoutback`/`easeinquint`) →
congelado de ~0,08 s en el fotograma de impacto (dos claves iguales) → asentamiento. El brazo izquierdo repite al derecho
espejado y con un pequeño retraso para que el movimiento no sea simultáneo (acción solapada).
El fotograma de impacto de cada animación está en IMPACT_TICKS y debe coincidir con logic/PlayerAnimIds.IMPACT_TICKS (lo comprueba
PlayerAnimationResourcesTest): los efectos de pantalla y las partículas del cliente se retrasan esos ticks para caer en el golpe.
"""
import json
import os
import sys

NEUTRAL = [0, 0, 0]
OUT = "easeoutquad"
BACK = "easeoutback"
SMASH = "easeinquint"
SOFT = "easeinoutsine"
HOLD = "linear"

# Fotograma de impacto de cada animación, en ticks (20 por segundo). Debe coincidir con PlayerAnimIds.IMPACT_TICKS.
IMPACT_TICKS = {"berserker_rage": 4, "berserker_war_cry": 8, "berserker_slam": 3, "mage_bolt": 4, "mage_blink": 3,
                "mage_time_warp": 6, "mage_missile_r": 3, "mage_missile_l": 3, "assassin_vanish": 3, "assassin_step": 3,
                "assassin_fan": 4, "assassin_shuriken": 3}


def kf(vec, easing=OUT):
    return {"vector": [round(float(v), 3) for v in vec], "easing": easing}


def key(t):
    return f"{t:.3f}".rstrip("0").rstrip(".") if t else "0.0"


def track(frames):
    """frames: {tiempo_s: vector | (vector, curva)} -> bloque de keyframes de Bedrock. La curva es la del tramo que LLEGA a la clave."""
    out = {}
    for t in sorted(frames):
        v = frames[t]
        vec, ease = (v if isinstance(v, tuple) else (v, OUT))
        out[key(t)] = kf(vec, ease)
    return out


def bone(rot=None, pos=None):
    b = {}
    if rot:
        b["rotation"] = track(rot)
    if pos:
        b["position"] = track(pos)
    return b


def mirror(vec, roll_scale=1.0):
    """Pose del brazo/pierna izquierdos a partir de la derecha: el giro y el roll cambian de signo."""
    return [vec[0], -vec[1], -vec[2] * roll_scale]


def lagged_mirror(frames, lag, roll_scale=1.0):
    """Espeja y retrasa `lag` s todas las claves intermedias; la primera y la última conservan su tiempo (así el brazo izquierdo
    sale y termina a la vez que el derecho, pero llega más tarde a cada pose: acción solapada)."""
    times = sorted(frames)
    out = {}
    for t in times:
        v = frames[t]
        vec, ease = (v if isinstance(v, tuple) else (v, OUT))
        new_t = t if t in (times[0], times[-1]) else t + lag
        out[new_t] = (mirror(vec, roll_scale), ease)
    return out


def animation(loop, bones):
    """La duración es la de la última clave de cualquier hueso."""
    length = 0.0
    for b in bones.values():
        for channel in b.values():
            length = max(length, max(float(t) for t in channel))
    return {"loop": loop, "animation_length": round(length, 3), "bones": bones}


def limbs(right_arm, left_lag=0.04, left_roll=0.92, **others):
    """Brazos con acción solapada y el resto de huesos tal cual."""
    b = {"right_arm": bone(rot=right_arm), "left_arm": bone(rot=lagged_mirror(right_arm, left_lag, left_roll))}
    for name, (rot, pos) in others.items():
        b[name] = bone(rot=rot, pos=pos)
    return b


def build_berserker():
    anims = {}

    # ---- Furia (1,0 s): se encoge hacia atrás, EXPLOTA hacia delante con los brazos abiertos (sobrepaso), se queda congelado un
    # instante en el rugido y tiembla con la furia contenida antes de relajarse.
    rage_arm = {0: NEUTRAL, 0.10: ([34, -12, 8], OUT), 0.20: ([-64, 10, 80], BACK), 0.27: ([-64, 10, 80], HOLD),
                0.38: ([-50, 6, 66], OUT), 0.46: ([-55, 8, 73], SOFT), 0.54: ([-48, 4, 64], SOFT), 0.62: ([-55, 8, 73], SOFT),
                0.70: ([-50, 6, 68], SOFT), 1.0: (NEUTRAL, OUT)}
    anims["berserker_rage"] = animation(False, limbs(
        rage_arm, 0.04, 0.92,
        torso=({0: NEUTRAL, 0.10: [28, 0, -4], 0.20: ([-24, -4, 3], BACK), 0.27: ([-24, -4, 3], HOLD), 0.38: [-17, 0, 0],
                0.54: ([-20, 2, 0], SOFT), 0.70: ([-17, -2, 0], SOFT), 1.0: (NEUTRAL, OUT)}, None),
        head=({0: NEUTRAL, 0.10: [20, 0, 0], 0.20: ([-32, 6, 0], BACK), 0.27: ([-32, 6, 0], HOLD), 0.38: [-28, -5, 0],
               0.46: ([-33, 5, 0], SOFT), 0.54: ([-27, -5, 0], SOFT), 0.62: ([-32, 4, 0], SOFT), 0.70: ([-28, 0, 0], SOFT),
               1.0: (NEUTRAL, OUT)}, None),
        body=(None, {0: NEUTRAL, 0.10: [0, -3, 0], 0.20: ([0, 1.4, 0], BACK), 0.27: ([0, 1.4, 0], HOLD), 0.38: [0, 0.3, 0],
                     0.70: ([0, 0.3, 0], HOLD), 1.0: (NEUTRAL, OUT)}),
        right_leg=({0: NEUTRAL, 0.10: [-18, 0, 10], 0.20: [-8, 0, 12], 0.70: ([-8, 0, 12], HOLD), 1.0: (NEUTRAL, OUT)}, None),
        left_leg=({0: NEUTRAL, 0.10: [10, 0, -10], 0.20: [6, 0, -12], 0.70: ([6, 0, -12], HOLD), 1.0: (NEUTRAL, OUT)}, None),
    ))

    # ---- Grito de guerra (0,8 s): sube los brazos y se carga con un temblor, y de golpe los lanza hacia delante (golpe en el tick 8),
    # con un congelado en el impacto y un retroceso con rebote.
    shout_arm = {0: NEUTRAL, 0.14: ([-158, 0, 22], OUT), 0.24: ([-166, 0, 27], SOFT), 0.32: ([-160, 0, 24], SOFT),
                 0.40: ([-36, -6, 6], SMASH), 0.48: ([-36, -6, 6], HOLD), 0.56: ([-50, 0, 13], OUT), 0.80: (NEUTRAL, BACK)}
    anims["berserker_war_cry"] = animation(False, limbs(
        shout_arm, 0.03, 0.9,
        torso=({0: NEUTRAL, 0.14: [-20, 0, 0], 0.32: ([-24, 3, 0], SOFT), 0.40: ([32, -4, 0], SMASH), 0.48: ([32, -4, 0], HOLD),
                0.56: [26, 0, 0], 0.80: (NEUTRAL, OUT)}, None),
        head=({0: NEUTRAL, 0.14: [-32, 0, 0], 0.32: ([-37, 0, 0], SOFT), 0.40: ([16, 0, 0], SMASH), 0.48: ([16, 0, 0], HOLD),
               0.80: (NEUTRAL, OUT)}, None),
        body=(None, {0: NEUTRAL, 0.14: [0, 1, 0], 0.32: ([0, 1.5, 0], SOFT), 0.40: ([0, -2.5, 0], SMASH), 0.48: ([0, -2.5, 0], HOLD),
                     0.80: (NEUTRAL, OUT)}),
        right_leg=({0: NEUTRAL, 0.14: [-6, 0, 8], 0.48: ([-10, 0, 10], HOLD), 0.80: (NEUTRAL, OUT)}, None),
        left_leg=({0: NEUTRAL, 0.14: [6, 0, -8], 0.48: ([8, 0, -10], HOLD), 0.80: (NEUTRAL, OUT)}, None),
    ))

    # ---- Salto (se mantiene en el último fotograma): se hunde para coger impulso y sale disparado con los brazos arriba (sobrepaso).
    leap_arm = {0: NEUTRAL, 0.08: ([42, 0, 10], OUT), 0.18: ([-152, 0, 22], BACK), 0.30: ([-138, 0, 30], OUT)}
    anims["berserker_leap"] = animation("hold_on_last_frame", limbs(
        leap_arm, 0.03, 0.92,
        torso=({0: NEUTRAL, 0.08: [32, 0, 0], 0.18: ([-10, 0, 0], BACK), 0.30: [10, 0, 0]}, None),
        head=({0: NEUTRAL, 0.08: [-8, 0, 0], 0.18: [-22, 0, 0], 0.30: [-14, 0, 0]}, None),
        body=(None, {0: NEUTRAL, 0.08: [0, -4, 0], 0.18: ([0, 1.2, 0], BACK), 0.30: [0, 0.6, 0]}),
        right_leg=({0: NEUTRAL, 0.08: [-32, 0, 5], 0.18: [-14, 0, 6], 0.30: [-20, 0, 6]}, None),
        left_leg=({0: NEUTRAL, 0.08: [24, 0, -5], 0.18: [18, 0, -6], 0.30: [22, 0, -6]}, None),
    ))

    # ---- Aterrizaje (0,75 s): arranca en la pose del salto, sube un poco más (preparación), machaca (golpe en el tick 3), se queda
    # clavado en el suelo un instante y se incorpora con un rebote.
    slam_arm = {0: [-138, 0, 30], 0.07: ([-170, 0, 16], OUT), 0.15: ([-22, 0, 2], SMASH), 0.23: ([-22, 0, 2], HOLD),
                0.31: ([-8, 0, 4], OUT), 0.75: (NEUTRAL, BACK)}
    anims["berserker_slam"] = animation(False, limbs(
        slam_arm, 0.03, 0.92,
        torso=({0: [10, 0, 0], 0.07: [-8, 0, 0], 0.15: ([44, 0, 0], SMASH), 0.23: ([44, 0, 0], HOLD), 0.31: [30, 0, 0],
                0.75: (NEUTRAL, OUT)}, None),
        head=({0: [-14, 0, 0], 0.07: [-24, 0, 0], 0.15: ([26, 0, 0], SMASH), 0.23: ([26, 0, 0], HOLD), 0.75: (NEUTRAL, OUT)}, None),
        body=(None, {0: [0, 0.6, 0], 0.07: [0, 1.5, 0], 0.15: ([0, -5, 0], SMASH), 0.23: ([0, -5, 0], HOLD), 0.31: [0, -3, 0],
                     0.75: (NEUTRAL, BACK)}),
        right_leg=({0: [-20, 0, 6], 0.15: ([-20, 0, 13], SMASH), 0.23: ([-20, 0, 13], HOLD), 0.75: (NEUTRAL, OUT)}, None),
        left_leg=({0: [22, 0, -6], 0.15: ([16, 0, -13], SMASH), 0.23: ([16, 0, -13], HOLD), 0.75: (NEUTRAL, OUT)}, None),
    ))

    # ---- Torbellino (0,5 s por giro, se encadenan tres): giro continuo con los brazos que se lanzan hacia fuera en cada vuelta.
    whirl_arm = {0: [-70, 0, 60], 0.08: ([-88, 0, 86], BACK), 0.25: ([-80, 0, 74], SOFT), 0.5: ([-70, 0, 60], SOFT)}
    whirl = limbs(
        whirl_arm, 0.05, 0.95,
        torso=({0: [16, 0, 0], 0.08: [22, 0, 6], 0.25: ([14, 0, -4], SOFT), 0.5: ([16, 0, 0], SOFT)}, None),
        right_leg=({0: [-10, 0, 10], 0.5: [-10, 0, 10]}, None),
        left_leg=({0: [10, 0, -10], 0.5: [10, 0, -10]}, None),
    )
    whirl["body"] = bone(rot={0: ([0, 0, 0], "linear"), 0.5: ([0, 360, 0], "linear")},
                         pos={0: [0, -1.5, 0], 0.08: [0, -2.2, 0], 0.5: ([0, -1.5, 0], SOFT)})
    anims["berserker_whirl"] = animation(False, whirl)
    return {"format_version": "1.8.0", "animations": anims}


def build_mage():
    anims = {}

    # ---- Rayo arcano (0,6 s): lleva la mano derecha al pecho para cargar mientras la izquierda guía, lanza hacia delante con sobrepaso
    # (tick 4), se queda un instante y recoge. Gesto preciso y contenido: menos ancho que el del Bersérker, más limpio.
    bolt_r = {0: NEUTRAL, 0.08: ([-42, -8, 24], OUT), 0.14: ([-50, -6, 28], SOFT), 0.20: ([-98, 4, 4], BACK), 0.28: ([-98, 4, 4], HOLD),
              0.36: ([-86, 2, 9], OUT), 0.60: (NEUTRAL, SOFT)}
    bolt_l = {0: NEUTRAL, 0.08: ([-72, 12, -30], OUT), 0.14: ([-76, 10, -32], SOFT), 0.22: ([-62, 6, -22], BACK),
              0.30: ([-62, 6, -22], HOLD), 0.60: (NEUTRAL, SOFT)}
    anims["mage_bolt"] = animation(False, {
        "right_arm": bone(rot=bolt_r),
        "left_arm": bone(rot=bolt_l),
        "torso": bone(rot={0: NEUTRAL, 0.08: [8, 0, -4], 0.20: ([-7, 3, 0], BACK), 0.28: ([-7, 3, 0], HOLD), 0.60: (NEUTRAL, SOFT)}),
        "head": bone(rot={0: NEUTRAL, 0.08: [5, 0, 0], 0.20: ([-5, 0, 0], BACK), 0.28: ([-5, 0, 0], HOLD), 0.60: (NEUTRAL, SOFT)}),
        "body": bone(pos={0: NEUTRAL, 0.08: [0, -1, 0], 0.20: ([0, 0.4, 0], BACK), 0.28: ([0, 0.4, 0], HOLD), 0.60: (NEUTRAL, SOFT)}),
    })

    # ---- Salto arcano (0,55 s): cruza los brazos hacia dentro mientras se agacha y los abre de golpe al reaparecer (tick 3).
    blink_r = {0: NEUTRAL, 0.08: ([-72, -32, -16], OUT), 0.15: ([-34, 10, 64], BACK), 0.23: ([-34, 10, 64], HOLD),
               0.30: ([-30, 6, 50], OUT), 0.55: (NEUTRAL, SOFT)}
    anims["mage_blink"] = animation(False, limbs(
        blink_r, 0.03, 0.95,
        torso=({0: NEUTRAL, 0.08: [16, 0, 0], 0.15: ([-12, 0, 0], BACK), 0.23: ([-12, 0, 0], HOLD), 0.55: (NEUTRAL, SOFT)}, None),
        head=({0: NEUTRAL, 0.08: [8, 0, 0], 0.15: ([-8, 0, 0], BACK), 0.23: ([-8, 0, 0], HOLD), 0.55: (NEUTRAL, SOFT)}, None),
        body=(None, {0: NEUTRAL, 0.08: [0, -2.2, 0], 0.15: ([0, 1.2, 0], BACK), 0.23: ([0, 1.2, 0], HOLD), 0.55: (NEUTRAL, SOFT)}),
    ))

    # ---- Distorsión temporal (0,9 s): abre los brazos hacia arriba, los cierra con un golpe de palmas hacia delante (tick 6) y los
    # mantiene un instante empujando el tiempo antes de soltar.
    warp_r = {0: NEUTRAL, 0.14: ([-122, 0, 50], OUT), 0.24: ([-128, 0, 54], SOFT), 0.30: ([-86, -12, 6], SMASH),
              0.38: ([-86, -12, 6], HOLD), 0.50: ([-98, -4, 12], OUT), 0.64: ([-94, -6, 10], SOFT), 0.90: (NEUTRAL, SOFT)}
    anims["mage_time_warp"] = animation(False, limbs(
        warp_r, 0.02, 0.95,
        torso=({0: NEUTRAL, 0.14: [-6, 0, 0], 0.30: ([10, 0, 0], SMASH), 0.38: ([10, 0, 0], HOLD), 0.90: (NEUTRAL, SOFT)}, None),
        head=({0: NEUTRAL, 0.14: [-10, 0, 0], 0.30: ([6, 0, 0], SMASH), 0.38: ([6, 0, 0], HOLD), 0.90: (NEUTRAL, SOFT)}, None),
        body=(None, {0: NEUTRAL, 0.14: [0, 0.6, 0], 0.30: ([0, -1.2, 0], SMASH), 0.38: ([0, -1.2, 0], HOLD), 0.90: (NEUTRAL, SOFT)}),
    ))

    # ---- Misiles arcanos: un empujón corto por misil, alternando brazo (se dispara una animación por cada misil).
    punch_r = {0: NEUTRAL, 0.07: ([-52, -6, 20], OUT), 0.15: ([-102, 4, 6], SMASH), 0.21: ([-102, 4, 6], HOLD), 0.32: (NEUTRAL, SOFT)}
    guide_l = {0: NEUTRAL, 0.07: ([-30, 6, -12], OUT), 0.15: ([-42, 4, -16], OUT), 0.21: ([-42, 4, -16], HOLD), 0.32: (NEUTRAL, SOFT)}
    torso_r = {0: NEUTRAL, 0.07: [4, -5, 0], 0.15: ([-3, 5, 0], SMASH), 0.21: ([-3, 5, 0], HOLD), 0.32: (NEUTRAL, SOFT)}
    anims["mage_missile_r"] = animation(False, {
        "right_arm": bone(rot=punch_r), "left_arm": bone(rot=guide_l), "torso": bone(rot=torso_r)})
    anims["mage_missile_l"] = animation(False, {
        "left_arm": bone(rot=lagged_mirror(punch_r, 0, 1.0)), "right_arm": bone(rot=lagged_mirror(guide_l, 0, 1.0)),
        "torso": bone(rot=lagged_mirror(torso_r, 0, 1.0))})
    return {"format_version": "1.8.0", "animations": anims}


def build_assassin():
    anims = {}

    # ---- Desaparecer (0,7 s): cruza los brazos ante el rostro mientras se encoge (preparación) y los abre de un barrido de capa
    # en el tick 3, quedándose un instante agachado antes de erguirse.
    vanish_r = {0: NEUTRAL, 0.07: ([-78, -34, -10], OUT), 0.15: ([-52, 26, 78], SMASH), 0.24: ([-52, 26, 78], HOLD),
                0.36: ([-40, 12, 40], SOFT), 0.70: (NEUTRAL, SOFT)}
    anims["assassin_vanish"] = animation(False, limbs(
        vanish_r, 0.02, 0.95,
        torso=({0: NEUTRAL, 0.07: [20, 0, 0], 0.15: ([6, 8, 0], SMASH), 0.24: ([6, 8, 0], HOLD), 0.70: (NEUTRAL, SOFT)}, None),
        head=({0: NEUTRAL, 0.07: [14, 0, 0], 0.15: ([-4, 0, 0], BACK), 0.24: ([-4, 0, 0], HOLD), 0.70: (NEUTRAL, SOFT)}, None),
        body=(None, {0: NEUTRAL, 0.07: [0, -2.6, 0], 0.15: ([0, -1.8, 0], SMASH), 0.24: ([0, -1.8, 0], HOLD), 0.70: (NEUTRAL, SOFT)}),
    ))

    # ---- Paso de sombra (0,5 s): se enrosca hacia atrás con el arma baja y se lanza con una estocada al frente en el tick 3.
    step_r = {0: NEUTRAL, 0.07: ([28, -10, 22], OUT), 0.15: ([-104, 4, 4], SMASH), 0.23: ([-104, 4, 4], HOLD), 0.50: (NEUTRAL, SOFT)}
    anims["assassin_step"] = animation(False, limbs(
        step_r, 0.03, 0.9,
        torso=({0: NEUTRAL, 0.07: [-6, -14, 0], 0.15: ([22, 10, 0], SMASH), 0.23: ([22, 10, 0], HOLD), 0.50: (NEUTRAL, SOFT)}, None),
        head=({0: NEUTRAL, 0.07: [-4, -8, 0], 0.15: ([-8, 4, 0], BACK), 0.23: ([-8, 4, 0], HOLD), 0.50: (NEUTRAL, SOFT)}, None),
        body=(None, {0: NEUTRAL, 0.07: [0, -2.0, 0], 0.15: ([0, -1.4, 0], SMASH), 0.23: ([0, -1.4, 0], HOLD), 0.50: (NEUTRAL, SOFT)}),
    ))

    # ---- Abanico de cuchillas (0,75 s): cruza los brazos sobre el pecho cargando las cuchillas y los lanza abiertos en el tick 4.
    fan_r = {0: NEUTRAL, 0.10: ([-66, -38, -18], OUT), 0.15: ([-70, -40, -22], SOFT), 0.20: ([-84, 14, 82], SMASH),
             0.30: ([-84, 14, 82], HOLD), 0.42: ([-70, 8, 60], SOFT), 0.75: (NEUTRAL, SOFT)}
    anims["assassin_fan"] = animation(False, limbs(
        fan_r, 0.015, 0.96,
        torso=({0: NEUTRAL, 0.10: [10, 0, 0], 0.20: ([-6, 0, 0], SMASH), 0.30: ([-6, 0, 0], HOLD), 0.75: (NEUTRAL, SOFT)}, None),
        head=({0: NEUTRAL, 0.10: [8, 0, 0], 0.20: ([-4, 0, 0], BACK), 0.30: ([-4, 0, 0], HOLD), 0.75: (NEUTRAL, SOFT)}, None),
        body=(None, {0: NEUTRAL, 0.10: [0, -1.4, 0], 0.20: ([0, 0.6, 0], BACK), 0.30: ([0, 0.6, 0], HOLD), 0.75: (NEUTRAL, SOFT)}),
    ))

    # ---- Shuriken (0,6 s): arma el brazo por encima del hombro, el otro guía al frente, y lo lanza con un latigazo en el tick 3.
    shu_r = {0: NEUTRAL, 0.07: ([-150, 6, 38], OUT), 0.15: ([-78, -4, 6], SMASH), 0.23: ([-78, -4, 6], HOLD), 0.55: (NEUTRAL, SOFT)}
    shu_l = {0: NEUTRAL, 0.07: ([-70, 14, -24], OUT), 0.15: ([-58, 8, -18], SOFT), 0.23: ([-58, 8, -18], HOLD), 0.55: (NEUTRAL, SOFT)}
    anims["assassin_shuriken"] = animation(False, {
        "right_arm": bone(rot=shu_r), "left_arm": bone(rot=shu_l),
        "torso": bone(rot={0: NEUTRAL, 0.07: [-4, -14, 0], 0.15: ([6, 12, 0], SMASH), 0.23: ([6, 12, 0], HOLD), 0.55: (NEUTRAL, SOFT)}),
        "head": bone(rot={0: NEUTRAL, 0.07: [-4, -6, 0], 0.15: ([2, 4, 0], BACK), 0.23: ([2, 4, 0], HOLD), 0.55: (NEUTRAL, SOFT)}),
    })
    return {"format_version": "1.8.0", "animations": anims}


FILES = {"berserker.json": build_berserker, "mage.json": build_mage, "assassin.json": build_assassin}


def render(name):
    return json.dumps(FILES[name](), indent=2, ensure_ascii=False) + "\n"


def main():
    args = [a for a in sys.argv[1:] if a != "--check"]
    check = "--check" in sys.argv[1:]
    if len(args) != 1:
        print(__doc__)
        sys.exit(1)
    for name in FILES:
        path = os.path.join(args[0], "player_animations", name)
        text = render(name)
        if check:
            current = open(path, encoding="utf-8").read() if os.path.exists(path) else None
            if current != text:
                sys.exit(f"player_animations/{name} no coincide con el generador: ejecútalo sin --check")
            print(f"player_animations/{name} correcto")
            continue
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(text)
        print(path)


if __name__ == "__main__":
    main()
